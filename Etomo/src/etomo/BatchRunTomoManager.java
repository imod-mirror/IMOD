package etomo;

import java.io.File;
import java.io.IOException;

import etomo.logic.DatasetTool;
import etomo.process.BaseProcessManager;
import etomo.process.BatchRunTomoProcessManager;
import etomo.process.ImodManager;
import etomo.process.SystemProcessException;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.BaseMetaData;
import etomo.type.BaseScreenState;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.DialogType;
import etomo.type.FileType;
import etomo.type.InterfaceType;
import etomo.type.Run3dmodMenuOptions;
import etomo.ui.swing.BatchRunTomoDialog;
import etomo.ui.swing.MainBatchRunTomoPanel;
import etomo.ui.swing.MainPanel;
import etomo.util.Utilities;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2013</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public final class BatchRunTomoManager extends BaseManager {
  public static final String rcsid = "$Id:$";

  private static final AxisID AXIS_ID = AxisID.ONLY;

  private final BaseScreenState screenState = new BaseScreenState(AXIS_ID,
      AxisType.SINGLE_AXIS);

  private final BatchRunTomoMetaData metaData;

  private BatchRunTomoProcessManager processMgr;
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
    metaData = new BatchRunTomoMetaData(getLogProperties());
    processMgr = new BatchRunTomoProcessManager(this);
    initializeUIParameters(paramFileName, AXIS_ID);
    if (!EtomoDirector.INSTANCE.getArguments().isHeadless()) {
      openProcessingPanel();
      mainPanel.setStatusBarText(paramFile, metaData, logWindow);
      openBatchRunTomoDialog();
    }
  }

  /**
   * MUST run reconnect for all axis
   */
  private void openProcessingPanel() {
    mainPanel.showProcessingPanel(AxisType.SINGLE_AXIS);
    setPanel();
    reconnect(axisProcessData.getSavedProcessData(AxisID.ONLY), AxisID.ONLY, false);
  }

  public void openBatchRunTomoDialog() {
    if (dialog == null) {
      dialog = BatchRunTomoDialog.getInstance(this, AXIS_ID);
    }
    if (paramFile != null && metaData.isValid()) {
      dialog.setParameters(metaData);
    }
    mainPanel.showProcess(dialog.getContainer(), AXIS_ID);
    String actionMessage = Utilities.prepareDialogActionMessage(
        DialogType.BATCH_RUN_TOMO, AxisID.ONLY, null);
    if (actionMessage != null) {
      System.err.println(actionMessage);
    }
  }

  /**
   * Open imod with an optional model file.
   * @param stack - the file to open
   * @param axisID
   * @param imodIndex
   * @param boundaryModel
   * @param datasetName
   * @param dualAxis
   * @param menuOptions
   * @return
   */
  public int imod(final File stack, final AxisID axisID, int imodIndex,
      final boolean boundaryModel, final boolean dualAxis, Run3dmodMenuOptions menuOptions) {
    if (!stack.exists()) {
      uiHarness.openMessageDialog(this,stack.getAbsolutePath()+" does not exist.","Run 3dmod failed");
      return imodIndex;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    AxisType axisType = dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS;
    String datasetName = DatasetTool.getDatasetName(stack.getName(), dualAxis);
    try {
      if (boundaryModel) {
        imodIndex = imodManager.open(key, stack, axisID, imodIndex,
            FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL.getFileName(datasetName, axisType,
                axisID), true, menuOptions);
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
   * Open imod
   * @return the vector index of the 3dmod instance
   */
  public int imod(final File stack, final AxisID axisID, int imodIndex,
      Run3dmodMenuOptions menuOptions) {
    if (!stack.exists()) {
      uiHarness.openMessageDialog(this,stack.getAbsolutePath()+" does not exist.","Run 3dmod failed");
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
  public void imodModel(final AxisID axisID, final int imodIndex, final String stackName,
      final boolean dualAxis) {
    if (imodIndex == -1) {
      return;
    }
    String key = ImodManager.BATCH_RUN_TOMO_STACK_KEY;
    AxisType axisType = dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS;
    String datasetName = DatasetTool.getDatasetName(stackName, dualAxis);
    try {
      imodManager.openModel(key, axisID, imodIndex,
          FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL.getFileName(datasetName, axisType,
              axisID), true);
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
    Storable[] storables = new Storable[2 + offset];
    int index = offset;
    storables[index++] = metaData;
    storables[index++] = screenState;
    return storables;
  }
}
