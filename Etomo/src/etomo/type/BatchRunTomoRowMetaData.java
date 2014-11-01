package etomo.type;

import etomo.ui.BooleanFieldSetting;
import etomo.ui.FieldSettingInterface;

import java.util.Properties;

/**
 * <p>Description: Meta data for a row of the BatchRunTomo Interface table.</p>
 * <p/>
 * <p>Copyright: Copyright 2014</p>
 * <p/>
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 *
 * @author $Author$
 * @version $Revision$
 */
public final class BatchRunTomoRowMetaData {
  public static final String rcsid =
      "$Id$";

  private static final String GROUP_KEY = "row";
  private static final String DISPLAY_KEY = "display";

  private EtomoBoolean2 display = new EtomoBoolean2(DISPLAY_KEY);
  private final StringProperty bskip = new StringProperty("bskip");

  private BatchRunTomoDatasetMetaData datasetMetaData = null;

  private final String stackID;

  BatchRunTomoRowMetaData(final String stackID) {
    this.stackID = stackID;
  }

  /**
   * This function is used to decide whether to load the instance associated with stackID.
   *
   * @return
   */
  public static boolean isDisplay(final Properties props, String prepend,
      final String stackID) {
    prepend = createPrepend(prepend, stackID);
    EtomoBoolean2 display = new EtomoBoolean2(DISPLAY_KEY);
    display.load(props, prepend);
    return display.is();
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
    display.reset();
    bskip.reset();
    prepend = createPrepend(prepend);
    display.load(props, prepend);
    bskip.load(props, prepend);
    if (BatchRunTomoDatasetMetaData.exists(props, prepend)) {
      if (datasetMetaData == null) {
        datasetMetaData = new BatchRunTomoDatasetMetaData();
      }
      datasetMetaData.load(props, prepend);
    }
  }

  public void store(final Properties props, String prepend) {
    prepend = createPrepend(prepend);
    display.store(props, prepend);
    if (display.is()) {
      bskip.store(props, prepend);
      if (datasetMetaData != null) {
        datasetMetaData.store(props, prepend);
      }
    }
    else {
      // remove
      bskip.remove(props, prepend);
      // reset
      bskip.reset();
      if (datasetMetaData != null) {
        // remove
        datasetMetaData.remove(props, prepend);
        // reset
        datasetMetaData.reset();
        datasetMetaData = null;
      }
    }
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

  public void setDisplay(final boolean input) {
    display.set(input);
  }

  public boolean isDisplay() {
    return display.is();
  }

  public void setBskip(final String input) {
    bskip.set(input);
  }

  public String getBskip() {
    return bskip.toString();
  }
}
