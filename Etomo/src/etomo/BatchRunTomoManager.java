package etomo;

import java.io.File;
import java.io.IOException;

import etomo.comscript.BatchRunTomoComScriptManager;
import etomo.comscript.BatchruntomoParam;
import etomo.logic.BatchTool;
import etomo.logic.DatasetTool;
import etomo.process.BaseProcessManager;
import etomo.process.BatchRunTomoProcessManager;
import etomo.process.ImodManager;
import etomo.process.ProcessState;
import etomo.process.SystemProcessException;
import etomo.storage.LogFile;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.BaseMetaData;
import etomo.type.BaseScreenState;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.DataFileType;
import etomo.type.DialogType;
import etomo.type.FileType;
import etomo.type.InterfaceType;
import etomo.type.MetaData;
import etomo.type.ProcessEndState;
import etomo.type.ProcessName;
import etomo.type.Run3dmodMenuOptions;
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
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
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
  private final BaseScreenState screenState =
      new BaseScreenState(AXIS_ID, AxisType.SINGLE_AXIS);

  private final BatchRunTomoMetaData metaData;

  private final BatchRunTomoProcessManager processMgr;
  private MainBatchRunTomoPanel mainPanel;

  private BatchRunTomoDialog dialog = null;

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
      uiHarness.openMessageDialog(this, "The directory, " + rootDirPath +
              ", cannot be used because it ends with a space.", "Unusable Directory Name",
          AxisID.ONLY);
      return false;
    }
    propertyUserDir = rootDirPath;
    System.err.println("propertyUserDir: " + propertyUserDir);
    metaData.setRootName(rootName);
    String errorMessage = metaData.validate();
    if (errorMessage != null) {
      UIHarness.INSTANCE
          .openMessageDialog(this, errorMessage, "Batchruntomo Dialog error", AXIS_ID);
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
    reconnect(axisProcessData.getSavedProcessData(AXIS_ID), AXIS_ID, false);
  }

  public void openBatchRunTomoDialog() {
    if (dialog == null) {
      dialog = BatchRunTomoDialog.getInstance(this, AXIS_ID, tableReference);
    }
    dialog.setParameters(userConfig);
    boolean useProgressBar = false;
    //Don't load data from files if this is a new dataset
    if (paramFile != null) {
      uiHarness.INSTANCE.pack(AXIS_ID,this);
      useProgressBar = true;
      mainPanel.startProgressBar("Loading Files", AXIS_ID);
      if (metaData.isValid()) {
        dialog.setParameters(metaData);
      }
      comScriptManager.loadBatchRunTomo(AXIS_ID);
      dialog.setParameters(comScriptManager.getBatchRunTomoParam(AXIS_ID, false));
      dialog.loadAutodocs();
    }
    dialog.msgDirectivesChanged(paramFile == null, paramFile != null);
    dialog.addListeners();
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

  public boolean save() throws LogFile.LockException, IOException {
    mainPanel.startProgressBar("Saving Files", AXIS_ID);
    Utilities.timestamp("save", "start");
    super.save();
    mainPanel.done();
    saveBatchRunTomoDialog(false);
    mainPanel.stopProgressBar(AXIS_ID);
    return true;
  }

  public void run() {
    mainPanel.startProgressBar("Saving Files for Run", AXIS_ID);
    Utilities.timestamp("save for run", "start");
    ProcessEndState processEndState = ProcessEndState.DONE;
    try {
      super.save();
      if (!saveBatchRunTomoDialog(true)) {
        processEndState = ProcessEndState.FAILED;
      }
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      processEndState = ProcessEndState.FAILED;
    }
    catch (IOException e) {
      e.printStackTrace();
      processEndState = ProcessEndState.FAILED;
    }
    mainPanel.stopProgressBar(AXIS_ID, processEndState);
  }

  private boolean saveBatchRunTomoDialog(final boolean doValidation) {
    if (dialog == null) {
      return false;
    }
    if (paramFile == null) {
      //Set the param file
      if (dialog.isParamFileModifiable() &&
          setNewParamFile(dialog.getRootDir(), dialog.getRootName())) {
        dialog.msgParamFileSet();
      }
      else {
        //setting the param file failed
        return false;
      }
    }
    dialog.getParameters(userConfig);
    dialog.getParameters(metaData);
    dialog.saveAutodocs();
    saveStorables(AXIS_ID);
    savePreferences(AXIS_ID, userConfig);
    if (updateBatchRunTomo(doValidation) != null) {
      Utilities.timestamp("save", "end");
      return true;
    }
    return false;
  }

  private BatchruntomoParam updateBatchRunTomo(final boolean doValidation) {
    if (!comScriptManager.isBatchRunTomoLoaded()) {
      BaseProcessManager.touch(
          FileType.BATCH_RUN_TOMO_COMSCRIPT.getFile(this, AXIS_ID).getAbsolutePath(),
          this);
      comScriptManager.loadBatchRunTomo(AXIS_ID);
    }
    BatchruntomoParam param =
        comScriptManager.getBatchRunTomoParam(AXIS_ID, doValidation);
    if (dialog == null) {
      return null;
    }
    dialog.getParameters(param);
    ParallelPanel parallelPanel = getMainPanel().getParallelPanel(AXIS_ID);
    if (parallelPanel != null && !parallelPanel.getParameters(param)) {
      return null;
    }
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
      final boolean boundaryModel, final boolean dualAxis,
      Run3dmodMenuOptions menuOptions) {
    if (!stack.exists()) {
      uiHarness.openMessageDialog(this, stack.getAbsolutePath() + " does not exist.",
          "Run 3dmod failed");
      return imodIndex;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    try {
      if (boundaryModel) {
        imodIndex = imodManager.open(key, stack, axisID, imodIndex,
            BatchTool.getBoundaryModelName(stack.getName(), dualAxis), true, menuOptions);
      }
      else {
        imodIndex = imodManager.open(key, stack, axisID, imodIndex, menuOptions);
      }
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      uiHarness.openMessageDialog(this, except.getMessage(), "AxisType problem", axisID);
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      uiHarness
          .openMessageDialog(this, except.getMessage(), "Problem opening " + key, axisID);
    }
    catch (IOException e) {
      e.printStackTrace();
      uiHarness.openMessageDialog(this, e.getMessage(), "IO Exception", axisID);
    }
    return imodIndex;
  }

  /**
   * Open imod
   *
   * @return the vector index of the 3dmod instance
   */
  public int imod(final File stack, final AxisID axisID, int imodIndex,
      Run3dmodMenuOptions menuOptions) {
    if (!stack.exists()) {
      uiHarness.openMessageDialog(this, stack.getAbsolutePath() + " does not exist.",
          "Run 3dmod failed");
      return imodIndex;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    try {
      imodIndex = imodManager.open(key, stack, axisID, imodIndex, menuOptions);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      uiHarness.openMessageDialog(this, except.getMessage(), "AxisType problem", axisID);
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      uiHarness
          .openMessageDialog(this, except.getMessage(), "Problem opening " + key, axisID);
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
  public void imodModel(final AxisID axisID, final int imodIndex, final String stackName,
      final boolean dualAxis) {
    if (imodIndex == -1) {
      return;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    AxisType axisType = dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS;
    String datasetName = DatasetTool.getDatasetName(stackName, dualAxis);
    try {
      imodManager.openModel(key, axisID, imodIndex, FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL
          .getFileName(datasetName, axisType, axisID), true);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      uiHarness.openMessageDialog(this, except.getMessage(), "AxisType problem", axisID);
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      uiHarness
          .openMessageDialog(this, except.getMessage(), "Problem opening " + key, axisID);
    }
    catch (IOException e) {
      e.printStackTrace();
      uiHarness.openMessageDialog(this, e.getMessage(), "IO Exception", axisID);
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
    if (metaData == null) {
      return MetaData.getNewFileTitle();
    }
    return metaData.getName();
  }

  Storable[] getStorables(final int offset) {
    Storable[] storables = new Storable[1 + offset];
    int index = offset;
    storables[index++] = metaData;
    return storables;
  }
}
