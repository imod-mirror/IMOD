package etomo.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.type.AxisID;
import etomo.util.Utilities;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2002, 2003, 2004</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public abstract class BaseProcessManager {
  public static  final String  rcsid =  "$Id$";
  
  protected BaseManager manager = null;
  SystemProcessInterface threadAxisA = null;
  SystemProcessInterface threadAxisB = null;
  Thread processMonitorA = null;
  Thread processMonitorB = null;
  
  private HashMap killedList = new HashMap();
  
  protected abstract void comScriptPostProcess(ComScriptProcess script, int exitValue);
  
  public BaseProcessManager(BaseManager manager) {
    this.manager = manager;
  }
  
  /**
   * Start a managed command script for the specified axis
   * @param command
   * @param processMonitor
   * @param axisID
   * @return
   * @throws SystemProcessException
   */
  protected ComScriptProcess startComScript(
    String command,
    Runnable processMonitor,
    AxisID axisID)
    throws SystemProcessException {
    return startComScript(
      new ComScriptProcess(command, this, axisID, null),
      command,
      processMonitor,
      axisID);
  }
  
  /**
   * Start a managed command script for the specified axis
   * @param command
   * @param processMonitor
   * @param axisID
   * @param watchedFileName watched file to delete
   * @return
   * @throws SystemProcessException
   */
  protected ComScriptProcess startComScript(ComScriptProcess comScriptProcess, 
    String command,
    Runnable processMonitor,
    AxisID axisID)
    throws SystemProcessException {
    // Make sure there isn't something going on in the current axis
    isAxisBusy(axisID);

    // Run the script as a thread in the background
    comScriptProcess.setWorkingDirectory(new File(System
      .getProperty("user.dir")));
    comScriptProcess.setDebug(EtomoDirector.isDebug());
    comScriptProcess.setDemoMode(EtomoDirector.isDemo());
    comScriptProcess.start();

    // Map the thread to the correct axis
    mapAxisThread(comScriptProcess, axisID);

    if (EtomoDirector.isDebug()) {
      System.err.println("Started " + command);
      System.err.println("  Name: " + comScriptProcess.getName());
    }

    Thread processMonitorThread = null;
    // Replace the process monitor with a DemoProcessMonitor if demo mode is on
    if (EtomoDirector.isDemo()) {
      processMonitor = new DemoProcessMonitor(manager, axisID, command,
        comScriptProcess.getDemoTime());
    }

    //  Start the process monitor thread if a runnable process is provided
    if (processMonitor != null) {
      // Wait for the started flag within the comScriptProcess, this ensures
      // that log file has already been moved
      while (!comScriptProcess.isStarted() && !comScriptProcess.isError()) {
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException e) {
          break;
        }
      }
      processMonitorThread = new Thread(processMonitor);
      processMonitorThread.start();
      mapAxisProcessMonitor(processMonitorThread, axisID);
    }

    return comScriptProcess;
  }
  
  /**
   * Check to see if specified axis is busy, throw a system a
   * ProcessProcessException if it is.
   * 
   * @param axisID
   * @throws SystemProcessException
   */
  protected void isAxisBusy(AxisID axisID) throws SystemProcessException {
    // Check to make sure there is not another process already running on this
    // axis.
    if (axisID == AxisID.SECOND) {
      if (threadAxisB != null) {
        throw new SystemProcessException(
          "A process is already executing in the current axis");
      }
    }
    else {
      if (threadAxisA != null) {
        throw new SystemProcessException(
          "A process is already executing in the current axis");
      }
    }
  }

  /**
   * Save the process thread reference for the appropriate axis
   * 
   * @param thread
   * @param axisID
   */
  protected void mapAxisThread(SystemProcessInterface thread, AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      threadAxisB = thread;
    }
    else {
      threadAxisA = thread;
    }
  }
  
  /**
   * Save the process monitor thread reference for the appropriate axis
   * 
   * @param processMonitor
   * @param axisID
   */
  private void mapAxisProcessMonitor(Thread processMonitor, AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      processMonitorB = processMonitor;
    }
    else {
      processMonitorA = processMonitor;
    }
  }
  
  /**
   * Kill the thread for the specified axis
   */
  public void kill(AxisID axisID) {
    String processID = "";
    SystemProcessInterface thread = null;
    if (axisID == AxisID.SECOND) {
      thread = threadAxisB;
    }
    else {
      thread = threadAxisA;

    }
    if (thread != null) {
      processID = thread.getShellProcessID();
    }
    
    killProcessGroup(processID);
    killProcessAndDescendants(processID);
    
    thread.notifyKill();

    /*
    //  Loop over killing the children until there are none left
    if (!processID.equals("")) {
      String[] children;
      while ((children = getChildProcessList(processID)) != null) {
        String killCommand = "kill ";
        for (int i = 0; i < children.length; i++) {
          killCommand = killCommand + children[i] + " ";
        }

        SystemProgram kill = new SystemProgram(killCommand);
        kill.run();
      }

      SystemProgram killShell = new SystemProgram("kill " + processID);
      killShell.run();
    }*/
  }

  protected void killProcessGroup(String processID) {
    if (processID == null || processID.equals("")) {
      return;
    }
    long pid = Long.parseLong(processID);
    if (pid == 0 || pid == 1) {
      return;
    }
    long groupPid = pid * -1;
    String groupProcessID = Long.toString(groupPid);
    kill("-19", groupProcessID);
    kill("-9", groupProcessID);
  }
  
  /**
   * Recursively kill all the descendents of a process and then kill the
   * process.  Function assumes that the process will continue spawning while
   * the descendant processes are being killed.  Function attempts to stop
   * spawning with a Stop signal.  The Stop signal may not work in all cases and
   * OS's, so the function refreshes the list of child processes until there are
   * no more child processes.  The function avoids getting stuck on an
   * unkillable process by recording each PID it sent a "kill -9" to.
   * 
   * The algorithm:
   * 1. Stop the root process.
   * 2. Go down to a leaf, stopping each process encountered.
   * 3. Kill the leaf.
   * 4. Go up to the parent of the killed leaf.
   * 5. If the parent is now a leaf, kill it and continue from step 4.
   * 6. If the parent is not a leaf, continue from step 2.
   * 
   * @param processID
   */
  protected void killProcessAndDescendants(String processID) {
    if (processID == null || processID.equals("")) {
      return;
    }
    //try to prevent process from spawning with a SIGSTOP signal
    kill("-19", processID);

    //kill all decendents of process before killing process
    String[] childProcessIDList = null;
    do {
      //get unkilled child processes
      childProcessIDList = getChildProcessList(processID);
      if (childProcessIDList != null) {
        for (int i = 0; i < childProcessIDList.length; i++) {
          killProcessAndDescendants(childProcessIDList[i]);
        }
      }
    } while (childProcessIDList != null);
    //there are no more unkilled child processes so kill process with a SIGKILL
    //signal
    kill("-9", processID);
    //record killed process
    killedList.put(processID, "");
  }

  private void kill(String signal, String processID) {
    SystemProgram killShell = new SystemProgram("kill " + signal + " " + processID);
    killShell.run();
    //System.out.println("kill " + signal + " " + processID + " at " + killShell.getRunTimestamp());
    Utilities.debugPrint("kill " + signal + " " + processID + " at " + killShell.getRunTimestamp());
  }

  /**
   * Return a the PIDs of child processes for the specified parent process.  A
   * new ps command is run each time this function is called so that the most
   * up-to-date list of child processes is used.  Only processes the have not
   * already received a "kill -9" signal are returned.
   * 
   * @param processID
   * @return A PID of a child process or null
   */
  private String[] getChildProcessList(String processID) {
    Utilities.debugPrint("in getChildProcessList: processID=" + processID);
    //ps -l: get user processes on this terminal
    SystemProgram ps = new SystemProgram("ps axl");
    ps.run();
    //System.out.println("ps axl date=" +  ps.getRunTimestamp());
    //  Find the index of the Parent ID and ProcessID
    String[] stdout = ps.getStdOutput();
    String header = stdout[0].trim();
    String[] labels = header.split("\\s+");
    int idxPID = -1;
    int idxPPID = -1;
    int idxCMD = -1;
    int idxPGID = -1;
    int found = 0;
    for (int i = 0; i < labels.length; i++) {
      if (labels[i].equals("PID")) {
        idxPID = i;
        found++;
      }
      if (labels[i].equals("PPID")) {
        idxPPID = i;
        found++;
      }
      if (labels[i].equals("CMD") || labels[i].equals("COMMAND")) {
        idxCMD = i;
        found++;
      }
      if (labels[i].equals("PGID")) {
        idxPGID = i;
      }
      if (found >= 3) {
        break;
      }
    }
    //  Return null if the PID or PPID fields are not found
    if (idxPPID == -1 || idxPID == -1) {
      return null;
    }

    // Walk through the process list finding the PID of the children
    ArrayList childrenPID = new ArrayList();
    String[] fields;
    //System.out.println(stdout[0]);
    for (int i = 1; i < stdout.length; i++) {
      //System.out.println(stdout[i]);
      fields = stdout[i].trim().split("\\s+");
      if (fields[idxPPID].equals(processID)
        && !killedList.containsKey(fields[idxPID])) {
        if (idxCMD != -1) {
          Utilities.debugPrint(
          "child found:PID="
            + fields[idxPID]
            + ",PPID="
            + fields[idxPPID]
            + ",name="
            + fields[idxCMD]);
        }
        childrenPID.add(fields[idxPID]);
      }
    }

    // If there are no children return null
    if (childrenPID.size() == 0) {
      return null;
    }

    // Connvert the ArrayList into a String[]
    String[] children = (String[]) childrenPID.toArray(new String[childrenPID
      .size()]);
    return children;
  }
  
  /**
   * Return a PID of a child process for the specified parent process.  A new
   * ps command is run each time this function is called so that the most
   * up-to-date list of child processes is used.  Only processes the have not
   * already received a "kill -9" signal are returned.
   * 
   * @param processID
   * @return A PID of a child process or null
   */
  protected String getChildProcess(String processID) {
    Utilities.debugPrint("in getChildProcess: processID=" + processID);
    //ps -l: get user processes on this terminal
    SystemProgram ps = new SystemProgram("ps axl");
    ps.run();

    //  Find the index of the Parent ID and ProcessID
    String[] stdout = ps.getStdOutput();
    String header = stdout[0].trim();
    String[] labels = header.split("\\s+");
    int idxPID = -1;
    int idxPPID = -1;
    int idxCMD = -1;
    int found = 0;
    for (int i = 0; i < labels.length; i++) {
      if (labels[i].equals("PID")) {
        idxPID = i;
        found++;
      }
      if (labels[i].equals("PPID")) {
        idxPPID = i;
        found++;
      }
      if (labels[i].equals("CMD") || labels[i].equals("COMMAND")) {
        idxCMD = i;
        found++;
      }
      if (found >= 3) {
        break;
      }
    }
    //  Return null if the PID or PPID fields are not found
    if (idxPPID == -1 || idxPID == -1) {
      return null;
    }

    // Walk through the process list finding the PID of the children
    String[] fields;
    for (int i = 1; i < stdout.length; i++) {
      fields = stdout[i].trim().split("\\s+");
      if (fields[idxPPID].equals(processID)
        && !killedList.containsKey(fields[idxPID])) {
        if (idxCMD != -1) {
          Utilities.debugPrint(
            "child found:PID="
              + fields[idxPID]
              + ",PPID="
              + fields[idxPPID]
              + ",name="
              + fields[idxCMD]);
        }
        return fields[idxPID];
      }
    }
    return null;
  }

  /**
   * A message specifying that a com script has finished execution
   * 
   * @param script
   *          the ComScriptProcess execution object that finished
   * @param exitValue
   *          the exit value for the com script
   */
  public void msgComScriptDone(ComScriptProcess script, int exitValue) {
    System.err.println("msgComScriptDone:scriptName=" + script.getScriptName()
      + ",processName=" + script.getProcessName());
    if (exitValue != 0) {
      String[] stdError = script.getStdError();
      String[] combined;
      //    Is the last string "Killed"
      if (stdError == null) {
        stdError = new String[0];
      }
      if (stdError != null && stdError.length > 0
        && stdError[stdError.length - 1].trim().equals("Killed")) {
        combined = new String[1];
        combined[0] = "<html>Terminated: " + script.getScriptName();
      }
      else {
        String[] message = script.getErrorMessage();
        combined = new String[message.length + stdError.length + 5];
        int j = 0;
        combined[j++] = "<html>Com script failed: " + script.getScriptName();
        combined[j++] = "  ";
        combined[j++] = "<html><U>Log file errors:</U>";

        for (int i = 0; i < message.length; i++, j++) {
          combined[j] = message[i];
        }
        combined[j++] = "  ";
        combined[j++] = "<html><U>Standard error output:</U>";
        for (int i = 0; i < stdError.length; i++, j++) {
          combined[j] = stdError[i];
        }
      }
      manager.getMainPanel().openMessageDialog(combined,
          script.getScriptName() + " terminated");
    }
    else {
      comScriptPostProcess(script, exitValue);

      String[] warningMessages = script.getWarningMessage();
      String[] dialogMessage;
      if (warningMessages != null && warningMessages.length > 0) {
        dialogMessage = new String[warningMessages.length + 2];
        dialogMessage[0] = "Com script: " + script.getScriptName();
        dialogMessage[1] = "<html><U>Warnings:</U>";
        int j = 2;
        for (int i = 0; i < warningMessages.length; i++) {
          dialogMessage[j++] = warningMessages[i];
        }
        manager.getMainPanel().openMessageDialog(dialogMessage,
            script.getScriptName()
          + " warnings");
      }

    }

    //  Null out the correct thread
    // Interrupt the process monitor and nulll out the appropriate references
    if (threadAxisA == script) {
      if (processMonitorA != null) {
        processMonitorA.interrupt();
        processMonitorA = null;
      }
      threadAxisA = null;
    }
    if (threadAxisB == script) {
      if (processMonitorB != null) {
        processMonitorB.interrupt();
        processMonitorB = null;
      }
      threadAxisB = null;
    }

    //  Inform the app manager that this process is complete
    manager.processDone(script.getName(), exitValue,
      script.getProcessName(), script.getAxisID());
  }
}
