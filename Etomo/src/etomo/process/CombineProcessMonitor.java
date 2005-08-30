package etomo.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import etomo.ApplicationManager;
import etomo.EtomoDirector;
import etomo.comscript.CombineComscriptState;
import etomo.type.AxisID;
import etomo.type.ProcessEndState;
import etomo.util.Utilities;


/**
 * <p>
 * Description: Provides a threadable class to execute IMOD com scripts in the
 * background.  An instance of this class can be run only once.
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2004</p>
 * 
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $$Author$$
 * 
 * @version $$Revision$$
 * 
 * <p> $$Log$
 * <p> $Revision 1.14  2005/08/27 22:25:19  sueh
 * <p> $bug# 532 Add empty getErrorMessage() to implement ProcessMonitor.
 * <p> $This is used by ProcesschunksProcessMonitor.
 * <p> $
 * <p> $Revision 1.13  2005/08/22 16:20:10  sueh
 * <p> $bug# 532 Added getStatusString() to implement ProcessMonitor.  The
 * <p> $status string is used to add more information to the progress bar when
 * <p> $the process ends.  It is currently being used only for pausing
 * <p> $processchunks.
 * <p> $
 * <p> $Revision 1.12  2005/08/15 18:09:57  sueh
 * <p> $bug# 532 Added kill and paused functions to implement ProcessMonitor.
 * <p> $Pause is invalid for this monitor.
 * <p> $
 * <p> $Revision 1.11  2005/08/04 19:43:51  sueh
 * <p> $bug# 532 Added empty setProcess() to implement ProcessMonitor.
 * <p> $
 * <p> $Revision 1.10  2005/07/26 18:09:18  sueh
 * <p> $bug# 701 Implementing ProcessMonitor, which extends Runnable.
 * <p> $Added a ProcessEndState member variable.  Set it to DONE or FAILED
 * <p> $when detect the end of the process.
 * <p> $
 * <p> $Revision 1.9  2005/04/25 20:44:53  sueh
 * <p> $bug# 615 Passing the axis where a command originates to the message
 * <p> $functions so that the message will be popped up in the correct window.
 * <p> $This requires adding AxisID to many objects.
 * <p> $
 * <p> $Revision 1.8  2005/01/26 23:41:20  sueh
 * <p> $bug# 83 Added matchvol1 and matchorwarp process monitors.
 * <p> $
 * <p> $Revision 1.7  2004/11/19 23:19:07  sueh
 * <p> $bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 * <p> $
 * <p> $Revision 1.6.2.3  2004/10/11 02:02:48  sueh
 * <p> $bug# 520 Using a variable called propertyUserDir instead of the "user.dir"
 * <p> $property.  This property would need a different value for each manager.
 * <p> $This variable can be retrieved from the manager if the object knows its
 * <p> $manager.  Otherwise it can retrieve it from the current manager using the
 * <p> $EtomoDirector singleton.  If there is no current manager, EtomoDirector
 * <p> $gets the value from the "user.dir" property.
 * <p> $
 * <p> $Revision 1.6.2.2  2004/10/08 15:56:41  sueh
 * <p> $bug# 520 Since EtomoDirector is a singleton, made all functions and
 * <p> $member variables non-static.
 * <p> $
 * <p> $Revision 1.6.2.1  2004/09/03 21:08:47  sueh
 * <p> $bug# 520 calling isSelfTest from EtomoDirector
 * <p> $
 * <p> $Revision 1.6  2004/08/28 00:47:16  sueh
 * <p> $bug# 508 setting "processRunning = true" only in the constructor.
 * <p> $A kill monitor call from an error that happens very early
 * <p> $(like combine.com is already running) was being ignored when
 * <p> $processRunning was turned back on.
 * <p> $
 * <p> $Revision 1.5  2004/08/24 20:31:18  sueh
 * <p> $bug# 508 make kill() interrupt the thread which is executing
 * <p> $the run function
 * <p> $
 * <p> $Revision 1.4  2004/08/23 23:35:45  sueh
 * <p> $bug# 508 made this class more like LogFileProcessMonitor.
 * <p> $Calling interrupt on child monitors and this monitor to make
 * <p> $run() complete faster
 * <p> $
 * <p> $Revision 1.3  2004/08/20 21:41:53  sueh
 * <p> $bug# 508 CombineComscriptState match string is now static.
 * <p> $Improved selfTest()
 * <p> $
 * <p> $Revision 1.2  2004/08/19 20:09:01  sueh
 * <p> $bug# 508 Made finding the .com file names more robust.  After 
 * <p> $finding the string "running" or "Running", find a string that 
 * <p> $matched a regular expression generated by 
 * <p> $CombineComscriptState.
 * <p> $Changed:
 * <p> $getCurrentSection()
 * <p> $setCurrentChildCommand(String comscriptName)
 * <p> $
 * <p> $Revision 1.1  2004/08/19 01:59:11  sueh
 * <p> $bug# 508 Watches combine.com.  Runs monitors for child .com
 * <p> $processes that have monitors.  For other child .com processes, starts
 * <p> $a progress bar and displays the process name.  Uses the combine.log
 * <p> $file to figure out when child process is running.  Uses
 * <p> $CombineComscriptState to figure out which child .com processes are
 * <p> $valid.  Also uses CombineComscriptState to know which dialog pane
 * <p> $to tell ApplicationManager to set.  Does not inherit
 * <p> $LogFileProcessMonitor.  Figures out when the process ends by
 * <p> $watching the combine.log or by setKill() being called by another object.
 * <p> $Provides information to other objects about the status of the combine
 * <p> $process.
 * <p> $$ </p>
 */
public class CombineProcessMonitor implements BackgroundComScriptMonitor {
  public static final String rcsid = "$$Id$$";
  public static final String COMBINE_LABEL = "Combine";
  private static final long SLEEP = 100;
  
  private ApplicationManager applicationManager = null;
  private AxisID axisID = null;
  private BufferedReader logFileReader = null;
  private long sleepCount = 0;
  private ProcessEndState endState = null;
  
  //if processRunning is false at any time before the process ends, it can
  //cause wait loops to end prematurely.  This is because the wait loop can
  //start very repidly for a background process.
  //See BackgroundSystemProgram.waitForProcess().
  private boolean processRunning = true;
  
  private File logFile = null;
  private LogFileProcessMonitor childMonitor = null;
  Thread childThread = null;
  private boolean success = false;
  private CombineComscriptState combineComscriptState = null;
  private int currentCommandIndex = CombineComscriptState.NULL_INDEX;
  
  private static final int CONSTRUCTED_STATE = 1;
  private static final int WAITED_FOR_LOG_STATE = 2;
  private static final int RAN_STATE = 3;
  private boolean selfTest = false;
  private Thread runThread = null;
  
  /**
   * @param applicationManager
   * @param axisID
   */
  public CombineProcessMonitor(ApplicationManager applicationManager,
    AxisID axisID, CombineComscriptState combineComscriptState) {
    this.applicationManager = applicationManager;
    this.axisID = axisID;
    this.combineComscriptState = combineComscriptState;
    selfTest = EtomoDirector.getInstance().isSelfTest();
    runSelfTest(CONSTRUCTED_STATE);
  }
  
  public AxisID getAxisID() {
    return axisID;
  }

  /**
   * returns false if the process has stopped, after giving run() a chance to
   * finish
   */
  public boolean isProcessRunning() {
    if (!processRunning) {
      //give run a chance to finish
      try {
        Thread.sleep(SLEEP);
      }
      catch (InterruptedException e) {
      } 
      return false;
    }
    return true;
  }
  
  /**
   * true if finished successfully
   */
  public boolean isSuccessful() {
    return success;
  }
  
  /**
   * called when the process is killed by the user
   */
  public void kill() {
    setProcessEndState(ProcessEndState.KILLED);
    endMonitor();
  }
  
  /**
   * set end state
   * @param endState
   */
  public synchronized final void setProcessEndState(ProcessEndState endState) {
    this.endState = ProcessEndState.precedence(this.endState, endState);
  }
  
  public final ProcessEndState getProcessEndState() {
    return endState;
  }
  
  private void initializeProgressBar() {
    applicationManager.startProgressBar(COMBINE_LABEL, axisID);
    return;
  }

  /**
   * get each .com file run by combine.com
   * @throws NumberFormatException
   * @throws IOException
   */
  private void getCurrentSection()
    throws NumberFormatException, IOException {
    String line;
    String matchString = CombineComscriptState.getComscriptMatchString();
    while ((line = logFileReader.readLine()) != null) {
      int index = -1;
      if ((line.indexOf("running ") != -1 || line.indexOf("Running ") != -1) &&
          line.matches(matchString)) {
        String[] fields = line.split("\\s+");
        for (int i = 0; i < fields.length; i++) {
          if (fields[i].matches(matchString)) {
            String comscriptName = fields[i];
            setCurrentChildCommand(comscriptName);
            runCurrentChildMonitor();
          }          
        }
      }
      else if (line.startsWith("ERROR:")) {
        setProcessEndState(ProcessEndState.FAILED);
        endMonitor();
      }
      else if (
        line.startsWith(CombineComscriptState.getSuccessText())) {
        success = true;
        setProcessEndState(ProcessEndState.DONE);
        endMonitor();

      }
    }
  }
  
  /**
   * get current .com file run by combine.com
   * run the monitor associated with the current .com file, if these is one
   * @param comscriptName
   */
  private void setCurrentChildCommand(String comscriptName) {
    String childCommandName =
      comscriptName.substring(0, comscriptName.indexOf(".com"));
    applicationManager.progressBarDone(axisID, ProcessEndState.DONE);

    if (childCommandName.equals(combineComscriptState.getCommand(
        CombineComscriptState.PATCHCORR_INDEX))) {
      endCurrentChildMonitor();
      applicationManager.showPane(CombineComscriptState.COMSCRIPT_NAME,
          CombineComscriptState.getDialogPane(
          CombineComscriptState.PATCHCORR_INDEX));
      childMonitor = new PatchcorrProcessWatcher(applicationManager, axisID);
    }
    else if (childCommandName.equals(combineComscriptState.getCommand(
        CombineComscriptState.MATCHVOL1_INDEX))) {
      endCurrentChildMonitor();
      applicationManager.showPane(CombineComscriptState.COMSCRIPT_NAME,
          CombineComscriptState.getDialogPane(
          CombineComscriptState.MATCHVOL1_INDEX));
      childMonitor = new Matchvol1ProcessMonitor(applicationManager, axisID);
    }
    else if (childCommandName.equals(combineComscriptState.getCommand(
        CombineComscriptState.MATCHORWARP_INDEX))) {
      endCurrentChildMonitor();
      applicationManager.showPane(CombineComscriptState.COMSCRIPT_NAME,
          CombineComscriptState.getDialogPane(
          CombineComscriptState.MATCHORWARP_INDEX));
      childMonitor = new MatchorwarpProcessMonitor(applicationManager, axisID);
    }
    else if (
      childCommandName.equals(combineComscriptState.getCommand(
          CombineComscriptState.VOLCOMBINE_INDEX))) {
      endCurrentChildMonitor();
      applicationManager.showPane(CombineComscriptState.COMSCRIPT_NAME,
          CombineComscriptState.getDialogPane(
          CombineComscriptState.VOLCOMBINE_INDEX));
      childMonitor = new VolcombineProcessMonitor(applicationManager, axisID);
    }
    else {
      startProgressBar(childCommandName);
    }
  }
  
  /**
   * run the monitor associated with the current .com file run by combine.com
   *
   */
  private void runCurrentChildMonitor() {
    if (childMonitor == null) {
      return;
    }
    childMonitor.setLastProcess(false);
    childThread = new Thread(childMonitor);
    childThread.start();
  }
  
  /**
   * stop the current monitor associated with the current .com file run by
   * combine.com
   *
   */
  private void endCurrentChildMonitor() {
    if (childMonitor != null) {
      childMonitor.haltProcess(childThread);
    }
    childMonitor = null;
    childThread = null;
  }

  /**
   * end this monitor
   *
   */
  private void endMonitor() {
    endCurrentChildMonitor();
    applicationManager.progressBarDone(axisID, endState);
    if (runThread != null) {
      runThread.interrupt();
      runThread = null;
    }
    processRunning = false;
  }
  
  /**
   * Start  a progress bar for the current .com file run by combine.com.
   * Used when there is no monitor available for the child process
   * @param childCommandName
   */
  private void startProgressBar(String childCommandName) {
    int commandIndex = CombineComscriptState.getCommandIndex(childCommandName);
    if (commandIndex == CombineComscriptState.NULL_INDEX) {
      //must be a command that is not monitored
      return;
    }
    endCurrentChildMonitor();
    applicationManager.showPane(CombineComscriptState.COMSCRIPT_NAME,
      CombineComscriptState.getDialogPane(commandIndex));
    applicationManager.startProgressBar(COMBINE_LABEL + ": " + childCommandName,
      axisID);
  }

  /**
   * Get log file.  Initialize progress bar.  Loop until processRunning is 
   * turned off or there is a timeout.  Call getCurrentSection for each loop.
   * After loop, turn off the monitor if that hasn't been done already.
   */
  public void run() {
    runThread = Thread.currentThread();
    initializeProgressBar();
    //  Instantiate the logFile object
    String logFileName = CombineComscriptState.COMSCRIPT_NAME + ".log";
    logFile = new File(applicationManager.getPropertyUserDir(), logFileName);

    try {
      //  Wait for the log file to exist
      waitForLogFile();
      initializeProgressBar();

      while (processRunning) {
        Thread.sleep(SLEEP);
        getCurrentSection();
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (InterruptedException e) {
      processRunning = false;
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    setProcessEndState(ProcessEndState.DONE);

    //  Close the log file reader
    try {
      Utilities
        .debugPrint("LogFileProcessMonitor: Closing the log file reader for "
            + logFile.getAbsolutePath());
      if (logFileReader != null) {
        logFileReader.close();
      }
    }
    catch (IOException e1) {
      e1.printStackTrace();
    }
    endMonitor();
    runSelfTest(RAN_STATE);
  }
  
  /**
   * Wait for the process to start and the appropriate log file to be created 
   * @return a buffered reader of the log file
   */
  private void waitForLogFile() throws InterruptedException, 
      FileNotFoundException {
    if (logFile == null) {
      throw new NullPointerException("logFile");
    }
    boolean newLogFile = false;
    while (!newLogFile) {
      // Check to see if the log file exists that signifies that the process
      // has started
      if (logFile.exists()) {
        newLogFile = true;
      }
      else {
        Thread.sleep(SLEEP);
      }
    }
    //  Open the log file
    logFileReader = new BufferedReader(new FileReader(logFile));
    runSelfTest(WAITED_FOR_LOG_STATE);
  }

  /**
   * Runs selfTest(int) when selfTest is set
   * @param selfTest
   * @param state
   */
  private void runSelfTest(int state) {
    if (!selfTest) {
      return;
    }
    selfTest(state);
  }
  
  /**
   * test for incorrect member variable settings.
   * @param state
   */
  public void selfTest(int state) {
    String stateString = null;
    switch (state) {
      case CONSTRUCTED_STATE :
        stateString = "After construction:  ";  
        if (axisID == null) {
          throw new NullPointerException(stateString 
              + "AxisID should not be null");
        }               
        if (combineComscriptState == null) {
          throw new NullPointerException(stateString
              + "CombineComscriptState should not be null");
        }          
        if (!processRunning) {
          throw new IllegalStateException(stateString 
              + "ProcessRunning must be true");
        }               
            
        break;

      case WAITED_FOR_LOG_STATE :
        stateString = "After waitForLogFile():  ";  
        if (logFile.exists() && sleepCount != 0) {
          throw new IllegalStateException(stateString 
              + "The sleepCount should be reset when the log file is found.  "
              + "sleepCount=" + sleepCount);
        }              
            
        break;

      case RAN_STATE :
        stateString = "After run():  ";  
        if (processRunning) {
          throw new IllegalStateException(stateString 
              + "ProcessRunning should be false.");
        }               

        break;
       
      default :
        throw new IllegalStateException("Unknown state.  state=" + state);
    }
  }
  
  public void setProcess(SystemProcessInterface process) {
    //process is not required
  }
  
  public void kill(SystemProcessInterface process, AxisID axisID) {
    endState = ProcessEndState.KILLED;
    process.signalKill(axisID);
  }
  
  public void pause(SystemProcessInterface process, AxisID axisID) {
    throw new IllegalStateException("can't pause a combine process");
  }
  
  public String getStatusString() {
    return null;
  }
  
  public final String getErrorMessage() {
    return null;
  }
}
