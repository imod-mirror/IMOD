package etomo.comscript;

import etomo.BaseManager;
import etomo.BatchRunTomoManager;
import etomo.type.AxisID;
import etomo.type.FileType;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2014</p>
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
public final class BatchRunTomoComScriptManager extends BaseComScriptManager {
  public static final String rcsid = "$Id:$";

  private final BaseManager manager;

  private ComScript scriptBatchRunTomo = null;

  public BatchRunTomoComScriptManager(final BatchRunTomoManager manager) {
    super(manager);
    this.manager = manager;
  }

  public boolean loadBatchRunTomo(AxisID axisID, boolean required) {
    scriptBatchRunTomo = loadComScript(
        FileType.BATCH_RUN_TOMO_COMSCRIPT.getFileName(manager, axisID), axisID, true,
        required, false, false);
    return scriptBatchRunTomo != null;
  }
  
  public void saveBatchRunTomo(BatchruntomoParam param, AxisID axisID) {
    modifyCommand(scriptBatchRunTomo, param, "batchruntomo", axisID, false, false);
  }
  
  public BatchruntomoParam getBatchRunTomoParam(AxisID axisID, CommandMode mode) {
    BatchruntomoParam param = new BatchruntomoParam(manager, axisID, mode);
    initialize(param, scriptBatchRunTomo, "batchruntomo", axisID, false, false);
    return param;
  }
}
