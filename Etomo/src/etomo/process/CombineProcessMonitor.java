package etomo.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import etomo.ApplicationManager;
import etomo.comscript.CombineComscriptState;
import etomo.type.AxisID;
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
public class CombineProcessMonitor implements Runnable, BackgroundProcessMonitor {
  public static final String rcsid = "$$Id$$";
  public static final String COMBINE_LABEL = "Combine";
  private static final long SLEEP = 100;
  private static final double timeoutMinutes = 1;
  private static final double timeoutCount = timeoutMinutes * 60 * 1000 / SLEEP;
  
  private ApplicationManager applicationManager = null;
  private AxisID axisID = null;
  private BufferedReader logFileReader = null;
  private long sleepCount = 0;
  
  //if processRunning is false at any time before the process ends, it can
  //cause wait loops to end prematurely.  This is because the wait loop can
  //start very repidly for a background process.
  //See BackgroundSystemProgram.waitForProcess().
  private boolean processRunning = true;
  
  private File logFile = null;
  private boolean standardLogFileName = true;
  private LogFileProcessMonitor childMonitor = null;
  
  //status variables - more then one could be set at a time because some of them
  //can be set on different threads
  private boolean done = false; //success
  private boolean error = false;
  private boolean killed = false;
  private boolean timedOut = false;
  private boolean ioException = false;
  
  private CombineComscriptState combineComscriptState = null;
  private int currentCommandIndex = CombineComscriptState.NULL_INDEX;
  
  private static final int CONSTRUCTED_STATE = 1;
  private static final int WAITED_FOR_LOG_STATE = 2;
  private static final int RAN_STATE = 3;
  private boolean selfTest = false;
  
  
  /**
   * @param applicationManager
   * @param axisID
   */
  public CombineProcessMonitor(ApplicationManager applicationManager,
    AxisID axisID, CombineComscriptState combineComscriptState) {
    this.applicationManager = applicationManager;
    this.axisID = axisID;
    this.combineComscriptState = combineComscriptState;
    selfTest = ApplicationManager.isSelfTest();
    runSelfTest(CONSTRUCTED_STATE);
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
  public boolean isDone() {
    return done;
  }
  
  /**
   * called when the process is killed by the user
   */
  public void setKilled(boolean killed) {
    endMonitor();
    this.killed = true;
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
    String matchString = combineComscriptState.getComscriptMatchString();
    while ((line = logFileReader.readLine()) != null) {
      int index = -1;
      System.out.println("line=" + line);
      if ((line.indexOf("running ") != -1 || line.indexOf("Running ") != -1) &&
          line.matches(matchString)) {
        String[] fields = line.split("\\s+");
        for (int i = 0; i < fields.length; i++) {
          System.out.println("fields[i]=" + fields[i]);
          if (fields[i].matches(matchString)) {
            String comscriptName = fields[i];
            setCurrentChildCommand(comscriptName);
            runCurrentChildMonitor();
          }          
        }
      }
      else if (line.startsWith("ERROR:")) {
        error = true;
        endMonitor();
      }
      else if (
        line.startsWith(CombineComscriptState.getSuccessText())) {
        done = true;
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
    System.out.println("comscriptName=" + comscriptName);
    String childCommandName =
      comscriptName.substring(0, comscriptName.indexOf(".com"));
    applicationManager.progressBarDone(axisID);

    if (childCommandName.equals(combineComscriptState.getCommand(
        CombineComscriptState.PATCHCORR_INDEX))) {
      endCurrentChildMonitor();
      applicationManager.showPane(CombineComscriptState.COMSCRIPT_NAME,
          CombineComscriptState.getDialogPane(
          CombineComscriptState.PATCHCORR_INDEX));
      childMonitor = new PatchcorrProcessWatcher(applicationManager, axisID);
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
    Thread childThread = new Thread(childMonitor);
    childThread.start();
  }
  
  /**
   * stop the current monitor associated with the current .com file run by
   * combine.com
   *
   */
  private void endCurrentChildMonitor() {
    if (childMonitor != null) {
      childMonitor.setProcessRunning(false);
    }
    childMonitor = null;
  }

  /**
   * end this monitor
   *
   */
  private void endMonitor() {
    endCurrentChildMonitor();
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
    if (!processRunning) {
      throw new IllegalStateException(
          "Cannot run this instance more then once");
    }
    initializeProgressBar();
    //  Instantiate the logFile object
    String logFileName;
    logFileName = CombineComscriptState.COMSCRIPT_NAME + axisID.getExtension() + ".log";
    logFile = new File(System.getProperty("user.dir"), logFileName);

    try {
      //  Wait for the log file to exist
      waitForLogFile();
      initializeProgressBar();

      while (processRunning && sleepCount <= timeoutCount) {
        try {
          Thread.sleep(SLEEP);
        }
        catch (InterruptedException e) {
        } 
        getCurrentSection();
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
      ioException = true;
      endMonitor();
    }
    
    if (processRunning) {
      timedOut = true;
      endMonitor();
    }
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
    applicationManager.progressBarDone(axisID);
    runSelfTest(RAN_STATE);
  }

  /**
   * Wait for the process to start and the appropriate log file to be created 
   * @return a buffered reader of the log file
   */
  private void waitForLogFile() throws FileNotFoundException {
    if (logFile == null) {
      throw new NullPointerException("logFile");
    }
    boolean newLogFile = false;
    while (!newLogFile &&  sleepCount <= timeoutCount) {
      // Check to see if the log file exists that signifies that the process
      // has started
      if (logFile.exists()) {
        newLogFile = true;
        sleepCount = 0;
      }
      else {
        try {
          Thread.sleep(SLEEP);
          sleepCount++;
        }
        catch (InterruptedException e) {
        }
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
              + "axisID is null");
        }               
        if (combineComscriptState == null) {
          throw new NullPointerException(stateString
              + "combineComscriptState is null");
        }          
        if (!processRunning) {
          throw new IllegalStateException(stateString 
              + "processRunning must be true");
        }               
        if (done || error || killed || timedOut) {
          throw new IllegalStateException(stateString 
              + "status variables must be false.  done=" + done + ",error=" + 
              error + ",killed=" + killed + ",timedOut=" + timedOut);
        }               
            
        break;

      case WAITED_FOR_LOG_STATE :
        stateString = "After waitForLogFile():  ";  
        if (logFile.exists() && sleepCount != 0) {
          throw new IllegalStateException(stateString 
              + "log file exists but sleepCount was not reset.  sleepCount=" 
              + sleepCount);
        }               
        if (!logFile.exists() && sleepCount < timeoutCount) {
          throw new IllegalStateException(stateString
              + "incorrect timeout.  sleepCount=" + sleepCount + ",timeoutCound="
              + timeoutCount);
        }              
            
        break;

      case RAN_STATE :
        stateString = "After run():  ";  
        if (processRunning) {
          throw new IllegalStateException(stateString 
              + "processRunning is true");
        }               
        if (!done && !error && !killed && !timedOut && !ioException) {
          throw new IllegalStateException(stateString
              + "no status variable is set.  done=" + done + ",error=" 
              + error + ",killed=" + killed + ",timedOut=" + timedOut
               + ",ioException=" + ioException);
        } 
        if (timedOut && sleepCount < timeoutCount) {
          throw new IllegalStateException(stateString
              + "timeout was set true without timing out.  sleepCount=" 
              + sleepCount + ",timeoutCound=" + timeoutCount);
        }             

        break;
       
      default :
        throw new IllegalStateException("state=" + state);
    }
  }

}
