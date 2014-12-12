package etomo.comscript;

import etomo.BaseManager;
import etomo.BatchRunTomoManager;
import etomo.type.AxisID;
import etomo.type.FileType;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoComScriptManager extends BaseComScriptManager {
  private final BaseManager manager;

  private ComScript scriptBatchRunTomo = null;

  public BatchRunTomoComScriptManager(final BatchRunTomoManager manager) {
    super(manager);
    this.manager = manager;
  }

  public void loadBatchRunTomo(final AxisID axisID) {
    scriptBatchRunTomo =
        loadComScript(FileType.BATCH_RUN_TOMO_COMSCRIPT.getFileName(manager, axisID),
            axisID, true, true, false, false);
  }
  public void loadBatchRunTomo(final AxisID axisID,final String rootName) {
    scriptBatchRunTomo =
        loadComScript(FileType.BATCH_RUN_TOMO_COMSCRIPT.getFileName(rootName, axisID),
            axisID, true, true, false, false);
  }
  public boolean isBatchRunTomoLoaded() {
    return scriptBatchRunTomo != null;
  }

  public void saveBatchRunTomo(final BatchruntomoParam param, final AxisID axisID) {
    modifyCommand(scriptBatchRunTomo, param, "batchruntomo", axisID, false, false);
  }

  public BatchruntomoParam getBatchRunTomoParam(final AxisID axisID,
      final boolean doValidation) {
    BatchruntomoParam param =
        BatchruntomoParam.getInstance(manager, axisID, doValidation);
    initialize(param, scriptBatchRunTomo, "batchruntomo", axisID, false, false);
    return param;
  }
}
