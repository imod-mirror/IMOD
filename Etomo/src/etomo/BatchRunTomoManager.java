package etomo;

import java.io.File;
import java.io.IOException;

import etomo.comscript.BatchRunTomoComScriptManager;
import etomo.comscript.BatchruntomoParam;
import etomo.logic.DatasetTool;
import etomo.process.BaseProcessManager;
import etomo.process.BatchRunTomoProcessManager;
import etomo.process.ImodManager;
import etomo.process.ProcessData;
import etomo.process.ProcessMessages;
import etomo.process.ProcessOutputStrings;
import etomo.process.SystemProcessException;
import etomo.storage.LogFile;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.BaseMetaData;
import etomo.type.BaseScreenState;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.CurrentArrayList;
import etomo.type.DataFileType;
import etomo.type.DialogType;
import etomo.type.FileType;
import etomo.type.InterfaceType;
import etomo.type.ProcessEndState;
import etomo.type.ProcessName;
import etomo.type.Run3dmodMenuOptions;
import etomo.type.StatusChanger;
import etomo.type.TableReference;
import etomo.ui.swing.BatchRunTomoDialog;
import etomo.ui.swing.MainBatchRunTomoPanel;
import etomo.ui.swing.MainPanel;
import etomo.ui.swing.ParallelPanel;
import etomo.ui.swing.UIHarness;
import etomo.util.Utilities;

/**
 * <p>Description: Manager for the interface for batchruntomo.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoManager extends BaseManager {
  private static final AxisID AXIS_ID = AxisID.ONLY;
  private static final String STACK_REFERENCE_PREFIX =
    DataFileType.BATCH_RUN_TOMO.extension.substring(1);

  private final TableReference tableReference =
    new TableReference(STACK_REFERENCE_PREFIX);
  private final BatchRunTomoComScriptManager comScriptManager =
    new BatchRunTomoComScriptManager(this);
  private final BaseScreenState screenState = new BaseScreenState(AXIS_ID,
    AxisType.SINGLE_AXIS);
  // Reuse the message string feed because starting it is too slow for reconnect, and
  // the reconnect finishes too fast for the string feed to complete.
  private final ProcessMessages messages = ProcessMessages.getLoggedInstance(this, true,
    true, ProcessOutputStrings.BRT_BATCH_RUN_TOMO_ERROR_TAG,
    ProcessOutputStrings.BRT_ABORT_TAG, false);

  private final BatchRunTomoMetaData metaData;

  private final BatchRunTomoProcessManager processMgr;
  private MainBatchRunTomoPanel mainPanel;

  private BatchRunTomoDialog dialog = null;
  // True if reconnect() has been run for the specified axis.
  private boolean reconnectRunA = false;
  private boolean reconnectRunB = false;

  public BatchRunTomoManager() {
    this("", null);
  }

  public BatchRunTomoManager(final String paramFileName) {
    this(paramFileName, null);
  }

  public BatchRunTomoManager(final String paramFileName, final DialogType dialogType) {
    super();
    metaData = new BatchRunTomoMetaData(getLogProperties(), tableReference);
    processMgr = new BatchRunTomoProcessManager(this);
    initializeUIParameters(paramFileName, AXIS_ID);
    if (!EtomoDirector.INSTANCE.getArguments().isHeadless()) {
      openProcessingPanel();
      mainPanel.setStatusBarText(paramFile, metaData, logWindow);
      openBatchRunTomoDialog();
      // Monitor is listened to by dialogs, so dialogs must exist before the monitor is
      // run.
      reconnect(axisProcessData.getSavedProcessData(AXIS_ID), AXIS_ID, false);
    }
    if (!loadedParamFile) {
      tableReference.setNew();
    }
  }

  public boolean setNewParamFile(final File rootDir, final String rootName) {
    if (loadedParamFile) {
      return true;
    }
    // set paramFile and propertyUserDir
    String rootDirPath = rootDir.getAbsolutePath();
    if (rootDirPath.endsWith(" ")) {
      uiHarness.openMessageDialog(this, "The directory, " + rootDirPath
        + ", cannot be used because it ends with a space.", "Unusable Directory Name",
        AxisID.ONLY);
      return false;
    }
    propertyUserDir = rootDirPath;
    System.err.println("propertyUserDir: " + propertyUserDir);
    metaData.setRootName(rootName);
    String errorMessage = metaData.validate();
    if (errorMessage != null) {
      UIHarness.INSTANCE.openMessageDialog(this, errorMessage,
        "Batchruntomo Dialog error", AXIS_ID);
      return false;
    }
    if (!setParamFile(new File(propertyUserDir, metaData.getMetaDataFileName()))) {
      return false;
    }
    EtomoDirector.INSTANCE.renameCurrentManager(metaData.getRootName());
    mainPanel.setStatusBarText(paramFile, metaData, logWindow);
    return true;
  }

  /**
   * MUST run reconnect for all axis
   */
  private void openProcessingPanel() {
    mainPanel.showProcessingPanel(AxisType.SINGLE_AXIS);
    setPanel();
  }

  public void openBatchRunTomoDialog() {
    if (dialog == null) {
      dialog = BatchRunTomoDialog.getInstance(this, AXIS_ID, tableReference);
    }
    dialog.setParameters(metaData);
    boolean useProgressBar = false;
    BatchruntomoParam param = null;
    if (paramFile != null) {
      comScriptManager.loadBatchRunTomo(AXIS_ID, dialog.getRootName());
      param = comScriptManager.getBatchRunTomoParam(AXIS_ID, false);
      useProgressBar = true;
      mainPanel.startProgressBar("Loading Files", AXIS_ID);
      // Load all parallel panel values - both tables must be open for this to happen.
      ParallelPanel parallelPanel = getMainPanel().getParallelPanel(AXIS_ID);
      if (parallelPanel != null) {
        parallelPanel.setParameters(param);
      }
    }
    // Load the UserEnv settings.
    dialog.getParameters();
    dialog.setParameters(userConfig, paramFile == null);
    if (paramFile != null) {
      dialog.setParameters(param);
      dialog.loadAutodocs();
    }
    dialog.msgDirectivesChanged(paramFile == null, paramFile != null);
    dialog.msgLoadDone();
    mainPanel.showProcess(dialog.getContainer(), AXIS_ID);
    uiHarness.updateFrame(this);
    String actionMessage =
      Utilities.prepareDialogActionMessage(DialogType.BATCH_RUN_TOMO, AXIS_ID, null);
    if (actionMessage != null) {
      System.err.println(actionMessage);
    }
    if (useProgressBar) {
      mainPanel.stopProgressBar(AXIS_ID);
    }
  }

  public void msgStatusChangerStarted(final StatusChanger changer) {
    dialog.msgStatusChangerStarted(changer);
  }

  /**
   * Call BaseManager.exitProgram(). Call saveDialog. Return the value of
   * BaseManager.exitProgram(). To guarantee that etomo can always exit, catch
   * all unrecognized Exceptions and Errors and return true.
   */
  public boolean exitProgram(AxisID axisID) {
    try {
      if (super.exitProgram(axisID)) {
        endThreads();
        saveParamFile();
        processMgr.haltMonitorThread();
        messages.stopStringFeed();
        return true;
      }
      return false;
    }
    catch (Throwable e) {
      e.printStackTrace();
      return true;
    }
  }

  public void pack() {
    if (dialog != null) {
      dialog.pack();
    }
  }

  public BaseScreenState getBaseScreenState(final AxisID axisID) {
    return screenState;
  }

  /**
   * Save on exit.
   */
  public boolean save() throws LogFile.LockException, IOException {
    if (dialog.isStatusKilledPaused()) {
      dialog.startOver();
    }
    mainPanel.startProgressBar("Saving Files", AXIS_ID);
    Utilities.timestamp("save", "start");
    super.save();
    mainPanel.done();
    saveBatchRunTomoDialog(false);
    mainPanel.stopProgressBar(AXIS_ID);
    return true;
  }

  public void batchruntomo(final CurrentArrayList<String> runKeys) {
    Utilities.timestamp("save for run", "start");
    ProcessEndState processEndState = ProcessEndState.DONE;
    try {
      super.save();
      BatchruntomoParam param = saveBatchRunTomoDialog(true);
      if (param == null) {
        return;
      }
      messages.startStringFeed();
      messages.clear();
      String threadName = null;
      try {
        threadName = processMgr.batchruntomo(param, runKeys, messages);
      }
      catch (SystemProcessException e) {
        e.printStackTrace();
        String[] message = new String[2];
        message[0] = "Can not execute batchruntomo comfile";
        message[1] = e.getMessage();
        uiHarness.openMessageDialog(this, message, "Unable to execute com script",
          AXIS_ID);
      }
      if (threadName != null) {
        setThreadName(threadName, AXIS_ID);
      }
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void resumeBatchruntomo(final CurrentArrayList<String> runKeys) {
    // Get the saved comscript
    comScriptManager.loadBatchRunTomo(AXIS_ID);
    BatchruntomoParam param = comScriptManager.getBatchRunTomoParam(AXIS_ID, true);
    // Add datasets that are still checked
    if (!dialog.getParameters(param, true, true)) {
      return;
    }
    comScriptManager.saveBatchRunTomo(param, AXIS_ID);
    // run batchruntomo
    String threadName = null;
    try {
      threadName = processMgr.batchruntomo(param, runKeys, messages);
    }
    catch (SystemProcessException e) {
      e.printStackTrace();
      String[] message = new String[2];
      message[0] = "Can not execute batchruntomo comfile";
      message[1] = e.getMessage();
      uiHarness.openMessageDialog(this, message, "Unable to execute com script", AXIS_ID);
    }
    if (threadName != null) {
      setThreadName(threadName, AXIS_ID);
    }
  }

  /**
   * Attempts to reconnect to a currently running process. Only run once per
   * axis. Only attempts one reconnect. Does not call super.reconnect because this
   * interface doesn't use the parallel process command.
   * 
   * @param axisID -
   *          axis of the running process.
   * @return true if a reconnect was attempted.
   */
  public boolean reconnect(final ProcessData processData, final AxisID axisID,
    final boolean multiLineMessages) {
    if (isReconnectRun(axisID)) {
      getProcessManager().unblockAxis(axisID);
      return false;
    }
    setReconnectRun(axisID);
    if (processData == null) {
      return false;
    }
    ProcessName processName = processData.getProcessName();
    if (processName == ProcessName.BATCHRUNTOMO) {
      if (processData.isOnDifferentHost()) {
        handleDifferentHost(processData, axisID);
        return false;
      }
      messages.startStringFeed();
      // ProcessData will only be saved if the process was running when etomo exited.
      // Even if the process is no long running, reconnect in order to update the display.
      System.err.println("\nAttempting to reconnect in Axis " + axisID.toString() + "\n"
        + processData);
      processMgr.reconnectBatchruntomo(processData, messages);
      setThreadName(processName.toString(), axisID);
      return true;
    }
    else {
      getProcessManager().unblockAxis(axisID);
    }
    return false;
  }

  private boolean isReconnectRun(AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      return reconnectRunB;
    }
    return reconnectRunA;
  }

  private void setReconnectRun(AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      reconnectRunB = true;
    }
    else {
      reconnectRunA = true;
    }
  }

  private BatchruntomoParam saveBatchRunTomoDialog(final boolean doValidation) {
    if (dialog == null) {
      return null;
    }
    if (paramFile == null
      && (!dialog.isParamFileModifiable() || !setNewParamFile(dialog.getRootDir(),
        dialog.getRootName()))) {
      // setting the param file failed
      return null;
    }
    loadedParamFile = true;
    dialog.disableDatasetFields();
    UIHarness.INSTANCE.setEnabledNewBatchRunTomoMenuItem(true);
    dialog.getParameters(userConfig);
    dialog.getParameters(metaData);
    if (!dialog.saveAutodocs(doValidation)) {
      return null;
    }
    BatchruntomoParam param = updateBatchRunTomo(doValidation);
    saveStorables(AXIS_ID);
    savePreferences(AXIS_ID, userConfig);
    Utilities.timestamp("save", "end");
    return param;
  }

  private BatchruntomoParam updateBatchRunTomo(final boolean doValidation) {
    if (!comScriptManager.isBatchRunTomoLoaded()) {
      BaseProcessManager.touch(FileType.BATCH_RUN_TOMO_COMSCRIPT.getFile(this, AXIS_ID)
        .getAbsolutePath(), this);
      comScriptManager.loadBatchRunTomo(AXIS_ID);
    }
    BatchruntomoParam param =
      comScriptManager.getBatchRunTomoParam(AXIS_ID, doValidation);
    if (dialog == null) {
      return null;
    }
    if (!dialog.getParameters(param, doValidation, false)) {
      return null;
    }
    ParallelPanel parallelPanel = getMainPanel().getParallelPanel(AXIS_ID);
    if (parallelPanel != null && !parallelPanel.getParameters(param)) {
      return null;
    }
    param.setSmtpServer(EtomoDirector.INSTANCE.getUserConfiguration().getSmtpServer());
    comScriptManager.saveBatchRunTomo(param, AXIS_ID);
    if (!param.isValid()) {
      return null;
    }
    return param;
  }

  public boolean isSetupDone() {
    return dialog != null && !dialog.isParamFileEmpty();
  }

  public boolean setParamFile() {
    if (loadedParamFile) {
      return true;
    }
    String rootName = dialog.getRootName();
    metaData.setName(rootName);
    paramFile =
      new File(dialog.getRootDir(), rootName + DataFileType.BATCH_RUN_TOMO.extension);
    if (!super.setParamFile(paramFile)) {
      return false;
    }
    if (!paramFile.exists()) {
      BaseProcessManager.touch(paramFile.getAbsolutePath(), this);
    }
    initializeUIParameters(paramFile, AxisID.ONLY, false);
    // Update main window information and status bar
    EtomoDirector.INSTANCE.renameCurrentManager(metaData.getName());
    mainPanel.setStatusBarText(paramFile, metaData, logWindow);
    return true;
  }

  /**
   * Open imod with an optional model file.
   *
   * @param stack         - the file to open
   * @param axisID
   * @param imodIndex
   * @param boundaryModel
   * @param dualAxis
   * @param menuOptions
   * @return
   */
  public int imod(final File stack, final AxisID axisID, int imodIndex,
    final File modelFile, final boolean dualAxis, Run3dmodMenuOptions menuOptions) {
    if (!stack.exists()) {
      uiHarness.openMessageDialog(this, stack.getAbsolutePath() + " does not exist.",
        "Run 3dmod failed");
      return imodIndex;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    try {
      imodIndex =
        imodManager.open(key, stack, axisID, imodIndex, modelFile, modelFile != null,
          menuOptions);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      uiHarness.openMessageDialog(this, except.getMessage(), "AxisType problem", axisID);
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      uiHarness.openMessageDialog(this, except.getMessage(), "Problem opening " + key,
        axisID);
    }
    catch (IOException e) {
      e.printStackTrace();
      uiHarness.openMessageDialog(this, e.getMessage(), "IO Exception", axisID);
    }
    return imodIndex;
  }

  /**
   * Open imod model in a running imod instance
   */
  public void imodModel(final AxisID axisID, final int imodIndex,
    final File stackLocation, final String stackName, final FileType modelFileType,
    final boolean dualAxis) {
    if (imodIndex == -1 || modelFileType == null || stackName == null) {
      return;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    AxisType axisType = dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS;
    String datasetName = DatasetTool.getDatasetName(stackName, dualAxis);
    File modelFile = modelFileType.getFile(stackLocation, datasetName, axisType, axisID);
    if (modelFile != null) {
      try {
        imodManager.openModel(key, axisID, imodIndex,
          Utilities.escapeSpaces(modelFile.getAbsolutePath(), true), true);
      }
      catch (AxisTypeException except) {
        except.printStackTrace();
        uiHarness
          .openMessageDialog(this, except.getMessage(), "AxisType problem", axisID);
      }
      catch (SystemProcessException except) {
        except.printStackTrace();
        uiHarness.openMessageDialog(this, except.getMessage(), "Problem opening " + key,
          axisID);
      }
      catch (IOException e) {
        e.printStackTrace();
        uiHarness.openMessageDialog(this, e.getMessage(), "IO Exception", axisID);
      }
    }
  }

  void createMainPanel() {
    if (!EtomoDirector.INSTANCE.getArguments().isHeadless()) {
      mainPanel = new MainBatchRunTomoPanel(this);
    }
  }

  public BaseMetaData getBaseMetaData() {
    return metaData;
  }

  public MainPanel getMainPanel() {
    return mainPanel;
  }

  public InterfaceType getInterfaceType() {
    return InterfaceType.BATCH_RUN_TOMO;
  }

  public BaseProcessManager getProcessManager() {
    return processMgr;
  }

  public String getName() {
    return metaData.getName();
  }

  Storable[] getStorables(final int offset) {
    Storable[] storables = new Storable[1 + offset];
    int index = offset;
    storables[index++] = metaData;
    return storables;
  }
}
