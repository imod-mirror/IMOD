package etomo.process;

import etomo.BatchRunTomoManager;
import etomo.comscript.BatchruntomoParam;
import etomo.type.AxisID;
import etomo.type.CurrentArrayList;
import etomo.type.FileType;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2014 - 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
public final class BatchRunTomoProcessManager extends BaseProcessManager {
  private static final AxisID AXID_ID = AxisID.ONLY;

  private final BatchRunTomoManager manager;

  public BatchRunTomoProcessManager(final BatchRunTomoManager manager) {
    super(manager);
    this.manager = manager;
  }

  /**
   * @param param
   * @param runKeys - array of keys to the rows involved with this run
   */
  public String batchruntomo(final BatchruntomoParam param,
    final CurrentArrayList<String> runKeys) throws SystemProcessException {
    if (param == null) {
      return null;
    }
    String command = FileType.BATCH_RUN_TOMO_COMSCRIPT.getFileName(manager, AXID_ID);
    BatchRunTomoProcessMonitor monitor =
      new BatchRunTomoProcessMonitor(manager, AXID_ID, true, runKeys);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startOutfileComScript(command, monitor, AXID_ID, param,
        FileType.BATCH_RUN_TOMO_COMSCRIPT);
    return comScriptProcess.getName();
  }
}
