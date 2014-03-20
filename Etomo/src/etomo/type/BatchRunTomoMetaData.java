package etomo.type;

import etomo.ui.LogProperties;
import etomo.util.DatasetFiles;

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
public final class BatchRunTomoMetaData extends BaseMetaData {
  public static final String rcsid = "$Id:$";

  private static final DialogType DIALOG_TYPE = DialogType.BATCH_RUN_TOMO;

  public static final String NEW_TITLE = "Batch Run Tomo";
  static final String GROUP_KEY = "BatchRunTomo";

  private String rootName = null;

  public BatchRunTomoMetaData(final LogProperties logProperties) {
    super(logProperties);
    axisType = AxisType.SINGLE_AXIS;
    fileExtension = DataFileType.BATCH_RUN_TOMO.extension;
  }

  public String getDatasetName() {
    return rootName;
  }

  public boolean isValid() {
    return validate() == null;
  }

  /**
   * returns null if valid
   * @return error message if invalid
   */
  public String validate() {
    if (rootName == null) {
      return "Missing root name.";
    }
    return null;
  }

  public String getMetaDataFileName() {
    if (rootName == null) {
      return null;
    }
    return DatasetFiles.getBatchRunTomoDataFileName(rootName);
  }

  String getGroupKey() {
    return GROUP_KEY;
  }

  public String getName() {
    if (rootName == null) {
      return NEW_TITLE;
    }
    return rootName;
  }
}
