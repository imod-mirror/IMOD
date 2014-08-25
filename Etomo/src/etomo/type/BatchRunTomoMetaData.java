package etomo.type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import etomo.BatchRunTomoManager;
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
public final class BatchRunTomoMetaData extends BaseMetaData implements HeaderMetaData {
  public static final String rcsid = "$Id:$";

  public static final String NEW_TITLE = "Batch Run Tomo";

  private final PanelHeaderState datasetHeaderState = new PanelHeaderState(getGroupKey()
      + ".");
  // Key is stackID
  private final Map<String, BatchRunTomoRowMetaData> rowMetaDataMap = new HashMap<String, BatchRunTomoRowMetaData>();
  // metadata for the global dataset dialog
  private final BatchRunTomoDatasetMetaData datasetMetaData = new BatchRunTomoDatasetMetaData(
      TableReference.getBaseID(BatchRunTomoManager.STACK_REFERENCE_PREFIX));

  private final TableReference tableReference;

  private String rootName = null;

  public BatchRunTomoMetaData(final LogProperties logProperties,
      final TableReference tableReference) {
    super(logProperties);
    this.tableReference = tableReference;
    axisType = AxisType.SINGLE_AXIS;
    fileExtension = DataFileType.BATCH_RUN_TOMO.extension;
  }

  public void setName(final String rootName) {
    this.rootName = rootName;
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
    return "meta";
  }

  public String getName() {
    if (rootName == null) {
      return NEW_TITLE;
    }
    return rootName;
  }

  /**
   * Better quality createPrepend.  Parent createPrepend cannot be improved without
   * breaking backwards compatibility.
   */
  String createPrepend(String prepend) {
    if (prepend == null || prepend.matches("\\s*")) {
      return getGroupKey();
    }
    prepend = prepend.trim();
    if (prepend.endsWith(".")) {
      return prepend + getGroupKey();
    }
    return prepend + "." + getGroupKey();
  }

  public void load(final Properties props, String prepend) {
    super.load(props, prepend);
    // reset
    rowMetaDataMap.clear();
    // load
    prepend = createPrepend(prepend);
    tableReference.load(props, prepend);
    Iterator<String> iterator = tableReference.idIterator();
    while (iterator.hasNext()) {
      String stackID = iterator.next();
      if (BatchRunTomoRowMetaData.isDisplay(props, prepend, stackID)) {
        BatchRunTomoRowMetaData rowMetaData = new BatchRunTomoRowMetaData(stackID);
        rowMetaData.load(props, prepend);
        rowMetaDataMap.put(stackID, rowMetaData);
      }
    }
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    tableReference.store(props, prepend);
    Iterator<BatchRunTomoRowMetaData> iterator = rowMetaDataMap.values().iterator();
    while (iterator.hasNext()) {
      iterator.next().store(props, prepend);
    }
  }

  public BatchRunTomoRowMetaData getRowMetaData(final String stackID) {
    return rowMetaDataMap.get(stackID);
  }

  public BatchRunTomoDatasetMetaData getDatasetMetaData() {
    return datasetMetaData;
  }

  public boolean isDisplay(final String stackID) {
    BatchRunTomoRowMetaData rowMetaData = rowMetaDataMap.get(stackID);
    return rowMetaData != null && rowMetaData.isDisplay();
  }
}
