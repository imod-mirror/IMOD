package etomo.process;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.SwingUtilities;

import etomo.BatchRunTomoManager;
import etomo.process.ProcessMessages;
import etomo.storage.LogFile;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoCommand;
import etomo.type.BatchRunTomoDatasetStatus;
import etomo.type.BatchRunTomoStatus;
import etomo.type.CurrentArrayList;
import etomo.type.EndingStep;
import etomo.type.FileType;
import etomo.type.ProcessEndState;
import etomo.type.Status;
import etomo.type.StatusChangeEvent;
import etomo.type.StatusChangeListener;
import etomo.type.StatusChangeTaggedEvent;
import etomo.type.StatusChanger;
import etomo.type.Step;

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

  private final ProcessMessages messages;
  private final BatchRunTomoManager manager;
  private final AxisID axisID;
  private final CurrentArrayList<String> runKeys;
  private final boolean reconnect;
  private final ArrayList<String> stacks = new ArrayList<String>();
  private final ProcessData processData;

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
  private boolean stop = false;
  private boolean running = false;
  private SystemProcessInterface process = null;
  private Vector<StatusChangeListener> listeners = null;
  private String currentStep = null;
  private String currentDataset = null;
  private boolean willResume = false;
  private boolean interrupted = false;
  private boolean datasetRunning = false;
  private boolean endingStepSet = false;
  private boolean startingStepSet = false;
  private int nDatasets = 0;
  private boolean datasetFailed = false;
  private boolean datasetDelivered = false;
  private boolean datasetRenamed = false;
  private boolean live = true;// The program is running
  private int processedLineNumber = 0;
  private boolean halt = false;

  private BatchRunTomoProcessMonitor(final BatchRunTomoManager manager,
    final AxisID axisID, final CurrentArrayList<String> runKeys, ProcessData processData,
    final ProcessMessages messages, final boolean reconnect) {
    this.manager = manager;
    this.axisID = axisID;
    this.messages = messages;
    this.runKeys = runKeys;
    this.reconnect = reconnect;
    this.processData = processData;
    if (runKeys != null) {
      nDatasets = runKeys.size();
    }
  }

  static BatchRunTomoProcessMonitor getInstance(final BatchRunTomoManager manager,
    final AxisID axisID, final CurrentArrayList<String> runKeys, ProcessData processData,
    final ProcessMessages messages) {
    BatchRunTomoProcessMonitor instance =
      new BatchRunTomoProcessMonitor(manager, axisID, runKeys, processData, messages,
        false);
    return instance;
  }

  static BatchRunTomoProcessMonitor getReconnectInstance(
    final BatchRunTomoManager manager, final AxisID axisID,
    final ProcessData processData, final ProcessMessages messages) {
    BatchRunTomoProcessMonitor instance =
      new BatchRunTomoProcessMonitor(manager, axisID, processData.getKeyArray(),
        processData, messages, true);
    instance.live = false;
    instance.processedLineNumber = processData.getLineNumber();
    processData.resetLineNumber();
    if (instance.processedLineNumber > 0) {
      messages.hibernate();
    }
    return instance;
  }

  public void dumpState() {
    System.err.print("[updateProgressBar:" + updateProgressBar + ",useCommandsPipe:"
      + useCommandsPipe + ",\nprocessRunning:" + processRunning + ",pausing:" + pausing
      + ",\nkilling:" + killing + ",stop:" + stop + ",running:" + running
      + ",\nreconnect:" + reconnect + "]");
  }

  /**
   * Sets the process.
   */
  public void setProcess(final SystemProcessInterface process) {
    this.process = process;
    if (process != null) {
      process.setKeyArray(runKeys);
    }
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
    boolean newCollection = false;
    if (listeners == null) {
      synchronized (this) {
        if (listeners == null) {
          listeners = new Vector<StatusChangeListener>();
          newCollection = true;
        }
      }
    }
    if (!newCollection && listeners.contains(listener)) {
      return;
    }
    listeners.add(listener);
  }

  synchronized public void halt() {
    halt = true;
  }

  public void run() {
    running = true;
    manager.msgStatusChangerStarted(this);
    if (!reconnect) {
      manager.logMessage("Running batchruntomo");
    }
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
    sendStatusChanged(BatchRunTomoStatus.RUNNING);
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
          if (halt) {
            // Can halt since the updateState has returned. But all existing messages
            // must be processed before state is valid.
            messages.feedEndMessage();
            return;
          }
          if (!reconnect || live) {
            Thread.sleep(100);
          }
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
      endMonitor();
    }
    catch (IOException e) {
      e.printStackTrace();
      endMonitor(ProcessEndState.FAILED);
    }
    // Disable the use of the commands pipe.
    useCommandsPipe = false;
    running = false;
    if (listeners != null) {
      BatchRunTomoStatus status;
      if (pausing || killing) {
        status = BatchRunTomoStatus.KILLED_PAUSED;
        if (killing) {
          // runKeys should be pointing to the last completed dataset.
          runKeys.previous();
        }
      }
      else if (endingStepSet) {
        status = BatchRunTomoStatus.STOPPED;
      }
      else {
        status = BatchRunTomoStatus.OPEN;
      }
      sendStatusChanged(status);
    }
  }

  private void sendStatusChanged(final BatchRunTomoStatus status) {
    SwingUtilities.invokeLater(new StatusChangeEventSender(status));
  }

  private void sendStatusChanged(final BatchRunTomoDatasetStatus status) {
    SwingUtilities.invokeLater(new StatusChangeEventSender(new StatusChangeTaggedEvent(
      runKeys.getCurrent(), status)));
  }

  private void sendStatusChanged(final Status status) {
    SwingUtilities.invokeLater(new StatusChangeEventSender(new StatusChangeTaggedEvent(
      runKeys.getCurrent(), status)));
  }

  private void sendStatusChanged(final BatchRunTomoCommand status, final String string) {
    SwingUtilities.invokeLater(new StatusChangeEventSender(new StatusChangeTaggedEvent(
      runKeys.getCurrent(), string, status)));
  }

  public void msgLogFileRenamed() {}

  public void endMonitor(final ProcessEndState endState) {
    // Output file will use the success tag when it ends from a pause. Don't lose the
    // pause state unless overriding it with something other then Done.
    if (this.endState == null || (endState != null && endState != ProcessEndState.DONE)) {
      setProcessEndState(endState);
    }
    endMonitor();
  }

  public void endMonitor() {
    processRunning = false;// the only place that this should be changed
    closeProcessOutput();
    messages.feedEndMessage();
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
    return runKeys.getTotal() + " of " + nDatasets + " completed";
  }

  public boolean isProcessRunning() {
    return processRunning;
  }

  AxisID getAxisID() {
    return axisID;
  }

  public final String getPid() {
    return null;
  }

  synchronized void closeProcessOutput() {
    if (processOutput != null && processOutputReaderId != null
      && !processOutputReaderId.isEmpty()) {
      processOutput.closeRead(processOutputReaderId);
      processOutput = null;
    }
  }

  String readParameters(String line) throws LogFile.LockException, FileNotFoundException,
    IOException {
    while (processOutput != null
      && (line = processOutput.readLine(processOutputReaderId)) != null) {
      line = line.trim();
      messages.feedString(line);
      if (line.indexOf(ProcessOutputStrings.END_PARAMETERS_TAG) != -1) {
        break;
      }
      if (line.indexOf(ProcessOutputStrings.BRT_ENDING_STEP_PARAM_TAG) != -1) {
        endingStepSet = true;
      }
      if (line.indexOf(ProcessOutputStrings.BRT_STARTING_STEP_PARAM_TAG) != -1) {
        startingStepSet = true;
      }
    }
    line = processOutput.readLine(processOutputReaderId);
    if (line != null) {
      line = line.trim();
    }
    return line;
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
    int index2;
    while (!halt && processOutput != null
      && (line = processOutput.readLine(processOutputReaderId)) != null) {
      processData.incrementLineNumber();
      // Avoid processing output more then once (for reconnect).
      if (reconnect && processData.isGtLineNumber(processedLineNumber)) {
        messages.wake();
      }
      line = line.trim();
      // Handle parameter list at the top of the log
      if (line.indexOf(ProcessOutputStrings.START_PARAMETERS_TAG) != -1) {
        line = readParameters(line);
      }
      // Send all output to the ProcessMessages blocking queue, so each line can be
      // processed immediately.
      // Handle messages that need to be logged, and send other lines to ProcessMessages.
      boolean recognized = false;
      index = line.indexOf(ProcessOutputStrings.BRT_DATASET_TAG);
      if (index != -1) {
        recognized = true;
        index2 = line.indexOf(ProcessOutputStrings.BRT_TIME_STAMP_TAG, index);
        if (index2 != -1) {
          currentDataset =
            line.substring(index + ProcessOutputStrings.BRT_DATASET_TAG.length(), index2);
        }
        else {
          currentDataset =
            line.substring(index + ProcessOutputStrings.BRT_DATASET_TAG.length());
        }
        currentDataset = currentDataset.trim();
        // Send a linefeed and the dataset start message to the project log. Use the
        // ProcessMessages string feed so that messages get to the project log in the
        // right order.
        messages.feedNewline(ProcessMessages.MessageType.LOG);
        messages.feedMessage(line);
        return true;
      }
      else {
        for (int i = 0; i < ProcessOutputStrings.BRT_LOG_TAGS.length; i++) {
          if (line.indexOf(ProcessOutputStrings.BRT_LOG_TAGS[i]) != -1) {
            recognized = true;
            // Send output that users want to see to the project log.
            messages.feedMessage(ProcessMessages.MessageType.LOG, line);
            break;
          }
        }
        if (!recognized) {
          if (line.indexOf(ProcessOutputStrings.BRT_AXIS_B_TAG) != -1) {
            recognized = true;
            messages.feedMessage(ProcessMessages.MessageType.LOG, line);
          }
        }
      }
      if (recognized) {
        continue;
      }
      // Handle lines that don't automatically go to the project log.
      messages.feedString(line);
      if (line.indexOf(ProcessOutputStrings.BRT_STARTING_DATASET_TAG) != -1) {
        recognized = true;
        runKeys.next();
        datasetRunning = true;
        datasetFailed = false;
        datasetDelivered = false;
        datasetRenamed = false;
        if (startingStepSet) {
          sendStatusChanged(BatchRunTomoDatasetStatus.STARTING);
        }
        else {
          sendStatusChanged(BatchRunTomoDatasetStatus.RUNNING);
        }
        currentStep = null;
        return true;
      }
      // check for the real batchruntomo error message. Everything else will be logged.
      if (line.indexOf(ProcessOutputStrings.BRT_BATCH_RUN_TOMO_ERROR_TAG) != -1) {
        if (runKeys.getTotal() == 0) {
          endMonitor(ProcessEndState.FAILED);
        }
        else {
          endMonitor(ProcessEndState.DONE);

        }
        return true;
      }
      if (line.equals(ProcessOutputStrings.BRT_SUCCESS_TAG)) {
        endMonitor(ProcessEndState.DONE);
        return true;
      }
      if (line.equals(ProcessOutputStrings.BRT_KILLING_TAG)) {
        // A kill is asynchronous, so it could happen just after a dataset is completed.
        // In that case no dataset was killed.
        if (endState == null && datasetRunning) {
          sendStatusChanged(BatchRunTomoDatasetStatus.KILLED);
        }
        setProcessEndState(ProcessEndState.KILLED);
        return true;
      }
      if (line.equals(ProcessOutputStrings.BRT_PAUSED_TAG)) {
        endMonitor(ProcessEndState.PAUSED);
        return true;
      }
      if (line.startsWith(ProcessOutputStrings.BRT_ETOMO_TAG)) {
        currentStep = "eTomo setup";
        return true;
      }
      if (line.startsWith(ProcessOutputStrings.BRT_STEP_SUCCESS_TAG)) {
        currentStep += " - done";
        return true;
      }
      if (line.startsWith(ProcessOutputStrings.BRT_DATASET_SUCCESS_TAG)) {
        // Dataset succeeded
        currentStep = "done";
        datasetRunning = false;
        BatchRunTomoDatasetStatus status;
        if (datasetFailed) {
          status = BatchRunTomoDatasetStatus.FAILED;
        }
        else {
          runKeys.incrementTotal();
          if (endingStepSet) {
            status = BatchRunTomoDatasetStatus.STOPPED;
          }
          else {
            status = BatchRunTomoDatasetStatus.DONE;
          }
        }
        sendStatusChanged(status);
        return true;
      }
      if (line.indexOf(ProcessOutputStrings.BRT_ABORT_SET_TAG) != -1) {
        // Dataset failed
        currentStep = "failed";
        datasetRunning = false;
        datasetFailed = true;
        sendStatusChanged(BatchRunTomoDatasetStatus.FAILED);
        return true;
      }
      if (line.indexOf(ProcessOutputStrings.BRT_ABORT_AXIS_TAG) != -1) {
        datasetFailed = true;
        currentStep = "axis failed";
        sendStatusChanged(BatchRunTomoDatasetStatus.FAILING);
        return true;
      }
      index = line.indexOf(ProcessOutputStrings.BRT_STEP_TAG);
      if (index != -1) {
        index2 = line.indexOf(ProcessOutputStrings.BRT_COM_TAG, index);
        if (index2 != -1) {
          currentStep =
            line.substring(index + ProcessOutputStrings.BRT_STEP_TAG.length(), index2)
              .trim();
          return true;
        }
      }
      if (line.indexOf(ProcessOutputStrings.BRT_REACHED_STEP_TAG) != -1) {
        String stepValue = line.substring(line.lastIndexOf(" ")).trim();
        EndingStep endingStep = EndingStep.getInstance(stepValue);
        if (endingStep != null) {
          sendStatusChanged(endingStep);
          continue;
        }
        Step step = Step.getInstance(stepValue);
        if (step != null) {
          sendStatusChanged(step);
          continue;
        }
      }
      if (line.indexOf(ProcessOutputStrings.BRT_DELIVERED_TAG) != -1) {
        index2 = line.indexOf(ProcessOutputStrings.BRT_FILE_TAG);
        if (index2 != -1) {
          String file =
            line.substring(index2 + ProcessOutputStrings.BRT_FILE_TAG.length());
          if (file != null && !file.matches("\\s*")) {
            // Only use the first delivered message
            if (!datasetDelivered) {
              datasetDelivered = true;
              sendStatusChanged(BatchRunTomoCommand.DELIVERED, file.trim());
            }
            continue;
          }
        }
      }
      if (line.indexOf(ProcessOutputStrings.BRT_RENAMED_TAG) != -1) {
        index2 = line.indexOf(ProcessOutputStrings.BRT_FILE_TAG);
        if (index2 != -1) {
          String file =
            line.substring(index2 + ProcessOutputStrings.BRT_FILE_TAG.length());
          if (file != null && !file.matches("\\s*")) {
            // Only use the first delivered message
            if (!datasetRenamed) {
              datasetRenamed = true;
              sendStatusChanged(BatchRunTomoCommand.RENAMED, file.trim());
            }
            continue;
          }
        }
      }
    }
    if (processRunning && line == null) {
      if (interrupted) {
        endMonitor();
        return true;
      }
      else {
        // Waiting for something to complete - must have caught up with the process.
        live = true;
      }
    }
    return false;
  }

  void updateProgressBar() {
    updateProgressBar = false;
    setProgressBarTitle();
    manager.getMainPanel().setProgressBarValue(runKeys.getTotal(), getStatusString(),
      axisID);
  }

  private void initializeProgressBar() {
    setProgressBarTitle();
    if (reconnect) {
      manager.getMainPanel().setProgressBarValue(runKeys.getTotal(), "Reconnecting...",
        axisID);
    }
    else {
      manager.getMainPanel().setProgressBarValue(runKeys.getTotal(), "Starting...",
        axisID);
    }
  }

  public void useMessageReporter() {}

  private final void setProgressBarTitle() {
    StringBuilder title = new StringBuilder();
    if (processRunning) {
      if (killing) {
        title.append("killing ");
      }
      else if (pausing) {
        title.append("pausing ");
        if (willResume) {
          title.append("(will resume) ");
        }
      }
    }
    else if (killing) {
      title.append("killed ");
    }
    else if (pausing) {
      title.append("paused ");
      if (willResume) {
        title.append("(will resume) ");
      }
    }
    title.append(TITLE);
    if (currentDataset != null) {
      title.append(": " + currentDataset);
    }
    if (currentStep != null) {
      title.append(": " + currentStep);
    }
    manager.getMainPanel().setProgressBar(title.toString(), nDatasets, axisID, !killing);
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

  private final class StatusChangeEventSender implements Runnable {
    private final Status status;
    private final StatusChangeEvent event;

    private StatusChangeEventSender(Status status) {
      this.status = status;
      event = null;
    }

    private StatusChangeEventSender(StatusChangeEvent event) {
      status = null;
      this.event = event;
    }

    public void run() {
      if (listeners == null) {
        return;
      }
      int size = listeners.size();
      if (status != null) {
        for (int i = 0; i < size; i++) {
          listeners.get(i).statusChanged(status);
        }
      }
      else if (event != null) {
        for (int i = 0; i < size; i++) {
          listeners.get(i).statusChanged(event);
        }
      }
    }
  }
}
