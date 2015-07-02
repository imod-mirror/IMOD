package etomo.process;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import etomo.BaseManager;
import etomo.process.ProcessMessages;
import etomo.storage.LogFile;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoStatus;
import etomo.type.EtomoNumber;
import etomo.type.FileType;
import etomo.type.ProcessEndState;
import etomo.type.Status;
import etomo.type.StatusChangeListener;
import etomo.type.StatusChanger;

/**
* <p>Description: Monitor for batchruntomo in BATCH mode.</p>
* 
* <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
public final class BatchRunTomoProcessMonitor implements OutfileProcessMonitor,
  StatusChanger {
  private static final String TITLE = "Batchruntomo";
  private static final String DATASET_TAG = "Starting data set";
  private static final String ETOMO_TAG = "starting eTomo with log in";
  private static final String STEP_TAG = "(running ";
  private static final String COM_TAG = ".com";
  private static final String STEP_SUCCESS_TAG = "Successfully finished";
  private static final String DATASET_SUCCESS_TAG = "Completed dataset";
  private static final String TIME_TAG = " in ";
  static final String SUCCESS_TAG = "SUCCESSFULLY COMPLETED";
  private static final String ENDING_STEP_SET_TAG = "EndingStepSet";
  private static final String NUMBER_DATASETS_TAG = "NumberDatasets";
  private static final String BATCH_RUN_TOMO_ERROR_TAG = "ERROR: batchruntomo -";
  private static final String ALT_ERROR_TAG = "ABORT SET:";

  private final EtomoNumber nDatasets = new EtomoNumber();
  private final EtomoNumber endingStepSet = new EtomoNumber(EtomoNumber.Type.BOOLEAN);

  private final ProcessMessages messages;

  private final BaseManager manager;
  private final AxisID axisID;
  private final boolean multiLineMessages;

  private boolean updateProgressBar = false;// turn on to changed the progress bar title
  private ProcessEndState endState = null;
  private LogFile commandsPipe = null;
  private LogFile.WriterId commandsPipeWriterId = null;
  private boolean useCommandsPipe = true;
  private LogFile processOutput = null;
  private LogFile.ReaderId processOutputReaderId = null;
  private boolean processRunning = true;
  private boolean pausing = false;
  private boolean killing = false;
  private boolean starting = true;
  private boolean stop = false;
  private boolean running = false;
  private boolean reconnect = false;
  private SystemProcessInterface process = null;
  private Vector<StatusChangeListener> listeners = null;
  private String currentStep = null;
  private String currentDataset = null;
  private int datasetsFinished = 0;
  private boolean willResume = false;
  private boolean interrupted = false;

  public void dumpState() {
    System.err.print("[updateProgressBar:" + updateProgressBar + ",useCommandsPipe:"
      + useCommandsPipe + ",\nprocessRunning:" + processRunning + ",pausing:" + pausing
      + ",\nkilling:" + killing + ",starting:" + starting + ",stop:" + stop + ",running:"
      + running + ",\nreconnect:" + reconnect + ",multiLineMessages:" + multiLineMessages
      + "]");
  }

  BatchRunTomoProcessMonitor(final BaseManager manager, final AxisID axisID,
    final boolean multiLineMessages, final int nDatasets, final boolean endingStepSet) {
    this.manager = manager;
    this.axisID = axisID;
    this.multiLineMessages = multiLineMessages;
    this.endingStepSet.set(endingStepSet);
    this.nDatasets.set(nDatasets);
    messages =
      ProcessMessages.getLoggedInstance(manager, multiLineMessages, true,
        BATCH_RUN_TOMO_ERROR_TAG, "ABORT SET:", false);
  }

  private BatchRunTomoProcessMonitor(final BaseManager manager, final AxisID axisID,
    final boolean multiLineMessages, final String nDatasets, final String endingStepSet) {
    this.manager = manager;
    this.axisID = axisID;
    this.multiLineMessages = multiLineMessages;
    this.endingStepSet.set(endingStepSet);
    this.nDatasets.set(nDatasets);
    messages =
      ProcessMessages.getLoggedInstance(manager, multiLineMessages, true,
        BATCH_RUN_TOMO_ERROR_TAG, "ABORT SET:", false);
  }

  public static BatchRunTomoProcessMonitor getReconnectInstance(
    final BaseManager manager, final AxisID axisID, final ProcessData processData,
    final boolean multiLineMessages) {
    BatchRunTomoProcessMonitor instance =
      new BatchRunTomoProcessMonitor(manager, axisID, multiLineMessages,
        processData.getDataPair(NUMBER_DATASETS_TAG),
        processData.getDataPair(ENDING_STEP_SET_TAG));
    instance.reconnect = true;
    return instance;
  }

  /**
   * Sets the process.
   */
  public void setProcess(final SystemProcessInterface process) {
    this.process = process;
  }

  public boolean isRunning() {
    return running;
  }

  public void stop() {
    stop = true;
  }

  public void addStatusChangeListener(final StatusChangeListener listener) {
    if (listener == null) {
      return;
    }
    boolean newElement = false;
    if (listeners == null) {
      synchronized (this) {
        if (listeners == null) {
          listeners = new Vector<StatusChangeListener>();
          newElement = true;
        }
      }
    }
    if (!newElement && listeners.contains(listener)) {
      return;
    }
    listeners.add(listener);
  }

  public void removeStatusChangeListener(final StatusChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listeners != null) {
      listeners.remove(listener);
    }
  }

  public void run() {
    manager.logMessage("Running batchruntomo");
    messages.startStringFeed();
    running = true;
    datasetsFinished = 0;
    try {
      /* Wait for processchunks or prochunks to delete .cmds file before enabling the Kill
       * Process button and Pause button. The main loop uses a sleep of 2000 millisecs.
       * This change pushes the first sleep back before the command buttons are turned on.
       * The monitor starts running before processchunks starts, so its easy to send a
       * command to a file which is not being watched and will be deleted by
       * processchunks. Not allowing commands to be sent for the period of the first sleep
       * also reduces the chance of a collision on Windows - where processchunks cannot
       * delete the command pipe file (.cmds file) because it is in use. */
      Thread.sleep(2000);
    }
    catch (InterruptedException e) {}
    // Get ready to respond to the Kill Process button and Pause button.
    useCommandsPipe = true;
    // Turn on the Kill Process button and Pause button.
    initializeProgressBar();
    if (listeners != null) {
      for (int i = 0; i < listeners.size(); i++) {
        listeners.get(i).statusChanged(BatchRunTomoStatus.RUNNING);
      }
    }
    try {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {}
      while (processRunning && !stop) {
        try {
          if (updateState() || updateProgressBar) {
            updateProgressBar();
          }
          Thread.sleep(100);
        }
        catch (LogFile.LockException e) {
          // File creation may be slow, so give this more tries.
          e.printStackTrace();
        }
        catch (FileNotFoundException e) {
          // File creation may be slow, so give this more tries.
          e.printStackTrace();
        }
        catch (InterruptedException e) {
          e.printStackTrace();
          interrupted = true;
        }
      }
      endMonitor(ProcessEndState.DONE);
    }
    catch (IOException e) {
      e.printStackTrace();
      endMonitor(ProcessEndState.FAILED);
    }
    // Disable the use of the commands pipe.
    useCommandsPipe = false;
    running = false;
    if (listeners != null) {
      Status status;
      if (pausing || killing) {
        status = BatchRunTomoStatus.KILLED_PAUSED;
      }
      else if (endingStepSet.is()) {
        status = BatchRunTomoStatus.STOPPED;
      }
      else {
        status = BatchRunTomoStatus.OPEN;
      }
      for (int i = 0; i < listeners.size(); i++) {
        listeners.get(i).statusChanged(status);
      }
    }
  }

  public void msgLogFileRenamed() {}

  public void endMonitor(final ProcessEndState endState) {
    setProcessEndState(endState);
    processRunning = false;// the only place that this should be changed
    closeProcessOutput();
    messages.stopStringFeed();
  }

  public String getLogFileName() {
    try {
      return getProcessOutputFileName();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      return "";
    }
  }

  public String getProcessOutputFileName() throws LogFile.LockException {
    createProcessOutput();
    return processOutput.getName();
  }

  /**
   * set end state
   * @param endState
   */
  public synchronized void setProcessEndState(final ProcessEndState endState) {
    this.endState = ProcessEndState.precedence(this.endState, endState);
  }

  public ProcessEndState getProcessEndState() {
    return endState;
  }

  public ProcessMessages getProcessMessages() {
    return messages;
  }

  public String getSubProcessName() {
    return null;
  }

  public void kill(final SystemProcessInterface process, final AxisID axisID) {
    try {
      writeCommand("Q");
      killing = true;
      updateProgressBar = true;
      if (starting) {
        // wait to see if processchunks is already starting chunks.
        try {
          Thread.sleep(2001);
        }
        catch (InterruptedException e) {}
        if (starting) {
          // processchunks hasn't started chunks and it won't because the "Q" has
          // been sent. So it is safe to kill it in the usual way.
          if (process != null) {
            process.signalKill(axisID);
          }
        }
      }
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void pause(final SystemProcessInterface process, final AxisID axisID) {
    try {
      writeCommand("F");
      pausing = true;
      updateProgressBar = true;
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isPausing() {
    return pausing && processRunning;
  }

  public void setWillResume() {
    willResume = true;
    setProgressBarTitle();
  }

  public String getStatusString() {
    return datasetsFinished + " of " + nDatasets + " completed";
  }

  public boolean isProcessRunning() {
    return processRunning && endState == null;
  }

  AxisID getAxisID() {
    return axisID;
  }

  public final String getPid() {
    return null;
  }

  boolean isStarting() {
    return starting;
  }

  synchronized void closeProcessOutput() {
    if (processOutput != null && processOutputReaderId != null
      && !processOutputReaderId.isEmpty()) {
      processOutput.closeRead(processOutputReaderId);
      processOutput = null;
    }
  }

  boolean updateState() throws LogFile.LockException, FileNotFoundException, IOException {
    createProcessOutput();
    if (processRunning
      && (processOutputReaderId == null || processOutputReaderId.isEmpty())) {
      processOutputReaderId = processOutput.openReader();
    }
    if (processOutputReaderId == null || processOutputReaderId.isEmpty()) {
      return false;
    }
    String line = null;
    int index;
    while (processOutput != null
      && (line = processOutput.readLine(processOutputReaderId)) != null) {
      line = line.trim();
      messages.feedString(line);
      // check for the real batchruntomo error message. Everything else will be logged.
      if (line.indexOf(BATCH_RUN_TOMO_ERROR_TAG) != -1) {
        endMonitor(ProcessEndState.FAILED);
        return true;
      }
      if (line.equals(SUCCESS_TAG)) {
        endMonitor(ProcessEndState.DONE);
        return true;
      }
      if (line.equals("When you rerun with a different set of machines, be sure to use")) {
        endMonitor(ProcessEndState.KILLED);
        return true;
      }
      if (line.equals("All previously running chunks are done - exiting as requested")) {
        endMonitor(ProcessEndState.PAUSED);
        return true;
      }
      if (line.startsWith(ETOMO_TAG)) {
        currentStep = "eTomo setup";
        return true;
      }
      if (line.startsWith(STEP_SUCCESS_TAG)) {
        index = line.indexOf(COM_TAG);
        currentStep += " - done";
        return true;
      }
      if (line.startsWith(DATASET_SUCCESS_TAG)) {
        index = line.indexOf(TIME_TAG);
        currentStep = "done";
        datasetsFinished++;
        return true;
      }
      index = line.indexOf(DATASET_TAG);
      if (index != -1) {
        currentDataset = line.substring(index + DATASET_TAG.length()).trim();
        currentStep = null;
        return true;
      }
      index = line.indexOf(STEP_TAG);
      if (index != -1) {
        int index2 = line.indexOf(COM_TAG, index);
        if (index2 != -1) {
          currentStep = line.substring(index + STEP_TAG.length(), index2).trim();
          return true;
        }
      }
    }
    // Wait until the entire log is processed before ending the monitor
    if (processRunning && interrupted && line == null) {
      endMonitor(ProcessEndState.DONE);
      return true;
    }
    return false;
  }

  void updateProgressBar() {
    updateProgressBar = false;
    setProgressBarTitle();
    manager.getMainPanel().setProgressBarValue(datasetsFinished, getStatusString(),
      axisID);
  }

  private void initializeProgressBar() {
    setProgressBarTitle();
    if (reconnect) {
      manager.getMainPanel().setProgressBarValue(datasetsFinished, "Reconnecting...",
        axisID);
    }
    else {
      manager.getMainPanel().setProgressBarValue(datasetsFinished, "Starting...", axisID);
    }
  }

  public void useMessageReporter() {}

  private final void setProgressBarTitle() {
    StringBuilder title = new StringBuilder(TITLE);
    if (currentDataset != null) {
      title.append(": " + currentDataset);
    }
    if (currentStep != null) {
      title.append(": " + currentStep);
    }
    if (processRunning) {
      if (killing) {
        title.append(" - killing:  exiting current dataset");
      }
      else if (pausing) {
        title.append(" - pausing:  finishing current dataset");
        if (willResume) {
          title.append(" - will resume");
        }
      }
    }
    else if (killing) {
      title.append(" - killed");
    }
    else if (pausing) {
      title.append(" - paused");
      if (willResume) {
        title.append(" - will resume");
      }
    }
    manager.getMainPanel().setProgressBar(title.toString(), nDatasets.getInt(), axisID,
      !killing);
  }

  /**
   * create commandsWriter if necessary, write command, add newline, flush
   * @param command
   * @throws IOException
   */
  private void writeCommand(final String command) throws LogFile.LockException,
    IOException {
    if (!useCommandsPipe) {
      return;
    }
    if (commandsPipe == null) {
      commandsPipe = LogFile.getInstance(FileType.CHECK_FILE.getFile(manager, axisID));
    }
    if (commandsPipeWriterId == null || commandsPipeWriterId.isEmpty()) {
      commandsPipeWriterId = commandsPipe.openWriter(true);
    }
    if (commandsPipeWriterId == null || commandsPipeWriterId.isEmpty()) {
      return;
    }
    commandsPipe.write(command, commandsPipeWriterId);
    commandsPipe.newLine(commandsPipeWriterId);
    commandsPipe.flush(commandsPipeWriterId);
    // Close writer after each write. If it is kept open, the file would not be writeable
    // from the command line in Windows.
    commandsPipe.closeWriter(commandsPipeWriterId);
    commandsPipeWriterId = null;
  }

  /**
   * Not responsible for backing up the process output file
   * @throws LogFile.LockException
   */
  private synchronized void createProcessOutput() throws LogFile.LockException {
    if (processOutput == null) {
      processOutput =
        LogFile.getInstance(FileType.BATCH_RUN_TOMO_LOG.getFile(manager, axisID));
    }
  }
}
