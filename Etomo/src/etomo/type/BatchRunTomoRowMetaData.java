package etomo.type;

import java.util.Properties;

/**
 * <p>Description: Meta data for a row of the BatchRunTomo Interface table.</p>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoRowMetaData {
  private static final String GROUP_KEY = "row";
  private static final String ROW_NUMBER_KEY = "RowNumber";
  private static final String DATASET_STATUS_KEY = "DatasetStatus";
  private static final String ENDING_STEP_KEY = "EndingStep";

  private final EtomoNumber rowNumber = new EtomoNumber(ROW_NUMBER_KEY);
  private final EtomoBoolean2 dual = new EtomoBoolean2("dual");
  private final StringProperty bskip = new StringProperty("bskip");
  private final EtomoBoolean2 run = new EtomoBoolean2("Run");
  private final StringProperty origStack = new StringProperty("OrigStack");
  private final EtomoBoolean2 etomoEnabled = new EtomoBoolean2("Etomo.Enabled");

  private BatchRunTomoDatasetMetaData datasetMetaData = null;
  private BatchRunTomoDatasetStatus datasetStatus = null;
  private EndingStep endingStep = null;

  private final String stackID;

  BatchRunTomoRowMetaData(final String stackID) {
    this.stackID = stackID;
  }

  public String toString() {
    return rowNumber.toString();
  }

  /**
   * This function is used to decide whether to load the instance associated with stackID.
   *
   * @return
   */
  public static boolean isRowNumberNull(final Properties props, String prepend,
    final String stackID) {
    prepend = createPrepend(prepend, stackID);
    EtomoNumber number = new EtomoNumber(ROW_NUMBER_KEY);
    number.load(props, prepend);
    return number.isNull();
  }

  private static String getGroupKey(final String stackID) {
    return GROUP_KEY + "." + stackID;
  }

  private static String createPrepend(String prepend, final String stackID) {
    if (prepend == null || prepend.matches("\\s*")) {
      return getGroupKey(stackID);
    }
    prepend = prepend.trim();
    if (prepend.endsWith(".")) {
      return prepend + getGroupKey(stackID);
    }
    return prepend + "." + getGroupKey(stackID);
  }

  private String getGroupKey() {
    return getGroupKey(stackID);
  }

  private String createPrepend(String prepend) {
    return createPrepend(prepend, stackID);
  }

  public void load(final Properties props, String prepend) {
    // reset
    rowNumber.reset();
    dual.reset();
    bskip.reset();
    run.reset();
    datasetStatus = null;
    endingStep = null;
    origStack.reset();
    etomoEnabled.reset();
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    rowNumber.load(props, prepend);
    dual.load(props, prepend);
    bskip.load(props, prepend);
    run.load(props, prepend);
    datasetStatus =
      BatchRunTomoDatasetStatus
        .getInstance(props.getProperty(group + DATASET_STATUS_KEY));
    endingStep = EndingStep.getInstance(props.getProperty(group + ENDING_STEP_KEY));
    origStack.load(props, prepend);
    etomoEnabled.load(props, prepend);
    if (BatchRunTomoDatasetMetaData.exists(props, prepend)) {
      if (datasetMetaData == null) {
        datasetMetaData = new BatchRunTomoDatasetMetaData();
      }
      datasetMetaData.load(props, prepend);
    }
  }

  public void store(final Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    rowNumber.store(props, prepend);
    if (!rowNumber.isNull()) {
      dual.store(props, prepend);
      bskip.store(props, prepend);
      run.store(props, prepend);
      if (datasetStatus != null) {
        props.setProperty(group + DATASET_STATUS_KEY, datasetStatus.getKey());
      }
      else {
        props.remove(group + DATASET_STATUS_KEY);
      }
      if (endingStep != null) {
        props.setProperty(group + ENDING_STEP_KEY, endingStep.getValue().toString());
      }
      else {
        props.remove(group + ENDING_STEP_KEY);
      }
      origStack.store(props, prepend);
      etomoEnabled.store(props, prepend);
      if (datasetMetaData != null) {
        datasetMetaData.store(props, prepend);
      }
    }
  }

  public void setEtomoEnabled(final boolean input) {
    etomoEnabled.set(input);
  }

  public boolean isEtomoEnabled() {
    return etomoEnabled.is();
  }

  public void setOrigStack(final String input) {
    origStack.set(input);
  }

  public void setEndingStep(final EndingStep input) {
    endingStep = input;
  }

  public void setDatasetStatus(final BatchRunTomoDatasetStatus input) {
    datasetStatus = input;
  }

  public String getOrigStack() {
    return origStack.toString();
  }

  public EndingStep getEndingStep() {
    return endingStep;
  }

  public BatchRunTomoDatasetStatus getDatasetStatus() {
    return datasetStatus;
  }

  public String getStackID() {
    return stackID;
  }

  public void setDatasetDialog(final boolean input) {
    if (input && datasetMetaData == null) {
      datasetMetaData = new BatchRunTomoDatasetMetaData();
    }
    if (datasetMetaData != null) {
      datasetMetaData.setDataset(input);
    }
  }

  public boolean isDatasetDialog() {
    return datasetMetaData != null;
  }

  public BatchRunTomoDatasetMetaData getDatasetMetaData() {
    if (datasetMetaData == null) {
      datasetMetaData = new BatchRunTomoDatasetMetaData();
    }
    return datasetMetaData;
  }

  public void setRowNumber(final String input) {
    rowNumber.set(input);
  }

  public int getRowNumber() {
    return rowNumber.getInt();
  }

  public boolean isRowNumberNull() {
    return rowNumber.isNull();
  }

  public void setDual(final boolean input) {
    dual.set(input);
  }

  public boolean isDual() {
    return dual.is();
  }

  public void setBskip(final String input) {
    bskip.set(input);
  }

  public String getBskip() {
    return bskip.toString();
  }

  public void setRun(final boolean input) {
    run.set(input);
  }

  public boolean isRun() {
    return run.is();
  }
}
