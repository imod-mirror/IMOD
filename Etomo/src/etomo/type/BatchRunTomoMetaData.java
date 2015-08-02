package etomo.type;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import etomo.ui.LogProperties;
import etomo.util.DatasetFiles;

/**
 * <p>Description: Main dialog for the batchruntomo interface.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoMetaData extends BaseMetaData {
  public static final String NEW_TITLE = "Batch Run Tomo";
  private static final String ENDING_STEP_KEY = "EndingStep";
  private static final String EARLIEST_RUN_KEY = "EarliestRun";
  private static final String STATUS_KEY = "Status";
  private static final String STARTING_STEP_KEY = "StartingStep";

  private StringProperty rootName = new StringProperty("RootName");
  private StringProperty deliverToDirectory = new StringProperty("DeliverToDirectory");
  private StringProperty inputDirectiveFile = new StringProperty("InputDirectiveFile");
  // Key is stackID
  private final OrderedHashMap<String, BatchRunTomoRowMetaData> rowMetaDataMap =
    new OrderedHashMap<String, BatchRunTomoRowMetaData>();
  // metadata for the global dataset dialog
  private final BatchRunTomoDatasetMetaData datasetMetaData =
    new BatchRunTomoDatasetMetaData();
  private final PanelHeaderSettings datasetTableHeader = new PanelHeaderSettings(
    "datasetTableHeader");
  private EtomoBoolean2 useExistingAlignment = new EtomoBoolean2("UseExistingAlignment");
  private EtomoBoolean2 useEndingStep = new EtomoBoolean2(ENDING_STEP_KEY + ".Use");
  private EtomoBoolean2 useStartingStep = new EtomoBoolean2(STARTING_STEP_KEY + ".Use");

  private final TableReference tableReference;

  private EndingStep earliestRunEndingStep = null;
  private BatchRunTomoStatus status = BatchRunTomoStatus.DEFAULT;
  private EndingStep endingStep = null;
  private StartingStep startingStep = null;

  public BatchRunTomoMetaData(final LogProperties logProperties,
    final TableReference tableReference) {
    super(logProperties);
    this.tableReference = tableReference;
    axisType = AxisType.SINGLE_AXIS;
    fileExtension = DataFileType.BATCH_RUN_TOMO.extension;
  }

  public void setName(final String rootName) {
    this.rootName.set(rootName);
  }

  public boolean isRootNameNull() {
    return rootName.isEmpty();
  }

  public String getDatasetName() {
    return rootName.toString();
  }

  public String getRootName() {
    return rootName.toString();
  }

  public void setRootName(final String input) {
    rootName.set(input);
  }

  public boolean isValid() {
    return validate() == null;
  }

  public static String getNewFileTitle() {
    return NEW_TITLE;
  }

  /**
   * returns null if valid
   *
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
    return DatasetFiles.getBatchRunTomoDataFileName(rootName.toString());
  }

  String getGroupKey() {
    return "meta";
  }

  public String getName() {
    if (rootName == null) {
      return NEW_TITLE;
    }
    return rootName.toString();
  }

  /**
   * Better quality createPrepend.  Parent createPrepend cannot be improved without
   * breaking backwards compatibility.
   */
  public String createPrepend(String prepend) {
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
    rootName.reset();
    deliverToDirectory.reset();
    inputDirectiveFile.reset();
    datasetTableHeader.reset();
    earliestRunEndingStep = null;
    status = null;
    useExistingAlignment.reset();
    useEndingStep.reset();
    rowMetaDataMap.clear();
    endingStep = null;
    startingStep = null;
    useStartingStep.reset();
    // load
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    rootName.load(props, prepend);
    deliverToDirectory.load(props, prepend);
    inputDirectiveFile.load(props, prepend);
    datasetTableHeader.load(props, prepend);
    datasetMetaData.load(props, prepend);
    tableReference.load(props, prepend);
    earliestRunEndingStep =
      EndingStep.getInstance(props.getProperty(group + ENDING_STEP_KEY + "."
        + EARLIEST_RUN_KEY));
    Iterator<String> iterator = tableReference.idIterator();
    status = BatchRunTomoStatus.getInstance(props.getProperty(group + STATUS_KEY));
    useExistingAlignment.load(props, prepend);
    useEndingStep.load(props, prepend);
    endingStep = EndingStep.getInstance(props.getProperty(group + ENDING_STEP_KEY));
    startingStep = StartingStep.getInstance(props.getProperty(group + STARTING_STEP_KEY));
    useStartingStep.load(props, prepend);
    while (iterator.hasNext()) {
      String stackID = iterator.next();
      if (!BatchRunTomoRowMetaData.isRowNumberNull(props, prepend, stackID)) {
        BatchRunTomoRowMetaData rowMetaData = new BatchRunTomoRowMetaData(stackID);
        rowMetaData.load(props, prepend);
        rowMetaDataMap.put(rowMetaData.getRowNumber(), stackID, rowMetaData);
      }
    }
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    rootName.store(props, prepend);
    deliverToDirectory.store(props, prepend);
    inputDirectiveFile.store(props, prepend);
    datasetTableHeader.store(props, prepend);
    datasetMetaData.store(props, prepend);
    tableReference.store(props, prepend);
    if (earliestRunEndingStep != null) {
      props.setProperty(group + ENDING_STEP_KEY + "." + EARLIEST_RUN_KEY,
        earliestRunEndingStep.getValue().toString());
    }
    else {
      props.remove(group + ENDING_STEP_KEY + "." + EARLIEST_RUN_KEY);
    }
    if (status != null) {
      props.setProperty(group + STATUS_KEY, status.getText().toString());
    }
    else {
      props.remove(group + STATUS_KEY);
    }
    useExistingAlignment.store(props, prepend);
    useEndingStep.store(props, prepend);
    if (endingStep != null) {
      props.setProperty(group + ENDING_STEP_KEY, endingStep.getValue().toString());
    }
    else {
      props.remove(group + ENDING_STEP_KEY);
    }
    if (startingStep != null) {
      props.setProperty(group + STARTING_STEP_KEY, startingStep.getValue().toString());
    }
    else {
      props.remove(group + STARTING_STEP_KEY);
    }
    useStartingStep.store(props, prepend);
    Iterator<BatchRunTomoRowMetaData> iterator = rowMetaDataMap.values().iterator();
    while (iterator.hasNext()) {
      iterator.next().store(props, prepend);
    }
  }

  public void setUseStartingStep(final boolean input) {
    useStartingStep.set(input);
  }

  public void setUseEndingStep(final boolean input) {
    useEndingStep.set(input);
  }

  public void setStartingStep(final StartingStep input) {
    startingStep = input;
  }

  public void setEndingStep(final EndingStep input) {
    endingStep = input;
  }

  public boolean isUseStartingStep() {
    return useStartingStep.is();
  }

  public boolean isUseEndingStep() {
    return useEndingStep.is();
  }

  public StartingStep getStartingStep() {
    return startingStep;
  }

  public EndingStep getEndingStep() {
    return endingStep;
  }

  public void setUseExistingAlignment(final boolean input) {
    useExistingAlignment.set(input);
  }

  public boolean isUseExistingAlignment() {
    return useExistingAlignment.is();
  }

  public void setStatus(final BatchRunTomoStatus input) {
    status = input;
  }

  public void setEarliestRunEndingStep(final EndingStep input) {
    earliestRunEndingStep = input;
  }

  public BatchRunTomoStatus getStatus() {
    return status;
  }

  public EndingStep getEarliestRunEndingStep() {
    return earliestRunEndingStep;
  }

  public OrderedHashMap.ReadOnlyArray<BatchRunTomoRowMetaData> getOrderedRows() {
    return rowMetaDataMap.orderedValues();
  }

  /**
   * Gets the rowMetaData for this stackID.  If it doesn't exist, create it and add it to
   * the map.  Do not add the ordinal at this point.  The order of the rows only matters
   * when they are being loaded into the table.
   * @param stackID
   * @return
   */
  public BatchRunTomoRowMetaData getRowMetaData(final String stackID) {
    BatchRunTomoRowMetaData rowMetaData = rowMetaDataMap.get(stackID);
    if (rowMetaData == null) {
      rowMetaData = new BatchRunTomoRowMetaData(stackID);
      rowMetaDataMap.put(stackID, rowMetaData);
    }
    return rowMetaData;
  }

  public BatchRunTomoDatasetMetaData getDatasetMetaData() {
    return datasetMetaData;
  }

  public boolean isRowNumberNull(final String stackID) {
    BatchRunTomoRowMetaData rowMetaData = rowMetaDataMap.get(stackID);
    return rowMetaData == null || rowMetaData.isRowNumberNull();
  }

  public ConstPanelHeaderSettings getDatasetTableHeader() {
    return datasetTableHeader;
  }

  public void setDatasetTableHeader(final ConstPanelHeaderSettings input) {
    datasetTableHeader.set(input);
  }

  public String getDeliverToDirectory() {
    return deliverToDirectory.toString();
  }

  public void setDeliverToDirectory(final File input) {
    if (input != null) {
      deliverToDirectory.set(input.getAbsolutePath());
    }
    else {
      deliverToDirectory.reset();
    }
  }

  public String getInputDirectiveFile() {
    return inputDirectiveFile.toString();
  }

  public void setInputDirectiveFile(final File input) {
    if (input != null) {
      inputDirectiveFile.set(input.getAbsolutePath());
    }
    else {
      inputDirectiveFile.reset();
    }
  }
}