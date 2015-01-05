package etomo.type;

import java.io.File;
import java.util.Properties;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoDatasetMetaData {
  public static final String rcsid = "$Id:$";

  private static final String GROUP_KEY = "dataset";
  private static final String HEADER_KEY = "header";

  private final EtomoBoolean2 dataset = new EtomoBoolean2();
  private final StringProperty modelFile = new StringProperty("ModelFile");
  private final EtomoBoolean2 enableStretching = new EtomoBoolean2("enableStretching");
  private final EtomoBoolean2 localAlignments = new EtomoBoolean2("LocalAlignments");
  private final EtomoNumber gold = new EtomoNumber(EtomoNumber.Type.DOUBLE, "gold");
  private final EtomoNumber targetNumberOfBeads =
      new EtomoNumber(EtomoNumber.Type.INTEGER, "TargetNumberOfBeads");
  private final StringProperty localAreaTargetSize =
      new StringProperty("LocalAreaTargetSize");
  private final StringProperty sizeOfPatchesXandY =
      new StringProperty("SizeOfPatchesXandY");
  private final EtomoBoolean2 lengthOfPieces = new EtomoBoolean2("LengthOfPieces");
  private final StringProperty defocus = new StringProperty("defocus");
  private final EtomoBoolean2 autoFitRangeAndStep =
      new EtomoBoolean2("autoFitRangeAndStep");
  private final EtomoNumber autoFitRange =
      new EtomoNumber(EtomoNumber.Type.DOUBLE, "autoFitRange");
  private final EtomoBoolean2 fitEveryImage = new EtomoBoolean2("fitEveryImage");
  private final EtomoNumber autoFitStep =
      new EtomoNumber(EtomoNumber.Type.DOUBLE, "autoFitStep");
  private final StringProperty leaveIterations = new StringProperty("LeaveIterations");
  private final EtomoBoolean2 scaleToInteger = new EtomoBoolean2("ScaleToInteger");
  private final EtomoNumber thickness = new EtomoNumber("THICKNESS");
  private final EtomoNumber binnedThickness = new EtomoNumber("binnedThickness");
  private final EtomoNumber extraThickness = new EtomoNumber("extraThickness");
  private final EtomoNumber fallbackThickness = new EtomoNumber("fallbackThickness");

  private PanelHeaderSettings header = null;

  BatchRunTomoDatasetMetaData() {
    dataset.set(true);
  }

  public static String createPrepend(String prepend) {
    if (prepend == null || prepend.matches("\\s*")) {
      return GROUP_KEY;
    }
    prepend = prepend.trim();
    if (prepend.endsWith(".")) {
      return prepend + GROUP_KEY;
    }
    return prepend + "." + GROUP_KEY;
  }

  static boolean exists(final Properties props, String prepend) {
    prepend = createPrepend(prepend);
    EtomoBoolean2 bool = new EtomoBoolean2();
    bool.set(props.getProperty(prepend));
    return bool.is();
  }

  void reset() {
    dataset.reset();
    if (header != null) {
      header.reset();
    }
    modelFile.reset();
    enableStretching.reset();
    localAlignments.reset();
    gold.reset();
    targetNumberOfBeads.reset();
    localAreaTargetSize.reset();
    sizeOfPatchesXandY.reset();
    lengthOfPieces.reset();
    defocus.reset();
    autoFitRangeAndStep.reset();
    autoFitRange.reset();
    fitEveryImage.reset();
    autoFitStep.reset();
    leaveIterations.reset();
    scaleToInteger.reset();
    thickness.reset();
    binnedThickness.reset();
    extraThickness.reset();
    fallbackThickness.reset();
  }

  public void load(final Properties props, String prepend) {
    // reset
    reset();
    // load
    prepend = createPrepend(prepend);
    dataset.set(props.getProperty(prepend));
    if (!dataset.is()) {
      return;
    }
    header = PanelHeaderSettings.load(header, HEADER_KEY, props, prepend);
    modelFile.load(props, prepend);
    enableStretching.load(props, prepend);
    localAlignments.load(props, prepend);
    gold.load(props, prepend);
    targetNumberOfBeads.load(props, prepend);
    localAreaTargetSize.load(props, prepend);
    sizeOfPatchesXandY.load(props, prepend);
    lengthOfPieces.load(props, prepend);
    defocus.load(props, prepend);
    autoFitRangeAndStep.load(props, prepend);
    autoFitRange.load(props, prepend);
    fitEveryImage.load(props, prepend);
    autoFitStep.load(props, prepend);
    leaveIterations.load(props, prepend);
    scaleToInteger.load(props, prepend);
    thickness.load(props, prepend);
    binnedThickness.load(props, prepend);
    extraThickness.load(props, prepend);
    fallbackThickness.load(props, prepend);
  }

  public void store(Properties props, String prepend) {
    if (!dataset.is()) {
      remove(props, prepend);
    }
    else {
      prepend = createPrepend(prepend);
      if (header != null) {
        header.store(props, prepend);
      }
      props.setProperty(prepend, dataset.toString());
      modelFile.store(props, prepend);
      enableStretching.store(props, prepend);
      localAlignments.store(props, prepend);
      gold.store(props, prepend);
      targetNumberOfBeads.store(props, prepend);
      localAreaTargetSize.store(props, prepend);
      sizeOfPatchesXandY.store(props, prepend);
      lengthOfPieces.store(props, prepend);
      defocus.store(props, prepend);
      autoFitRangeAndStep.store(props, prepend);
      autoFitRange.store(props, prepend);
      fitEveryImage.store(props, prepend);
      autoFitStep.store(props, prepend);
      leaveIterations.store(props, prepend);
      scaleToInteger.store(props, prepend);
      thickness.store(props, prepend);
      binnedThickness.store(props, prepend);
      extraThickness.store(props, prepend);
      fallbackThickness.store(props, prepend);
    }
  }

  public void remove(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    if (header != null) {
      header.remove(props, prepend);
    }
    props.remove(prepend);
    modelFile.remove(props, prepend);
    enableStretching.remove(props, prepend);
    localAlignments.remove(props, prepend);
    gold.remove(props, prepend);
    targetNumberOfBeads.remove(props, prepend);
    localAreaTargetSize.remove(props, prepend);
    sizeOfPatchesXandY.remove(props, prepend);
    lengthOfPieces.remove(props, prepend);
    defocus.remove(props, prepend);
    autoFitRangeAndStep.remove(props, prepend);
    autoFitRange.remove(props, prepend);
    fitEveryImage.remove(props, prepend);
    autoFitStep.remove(props, prepend);
    leaveIterations.remove(props, prepend);
    scaleToInteger.remove(props, prepend);
    thickness.remove(props, prepend);
    binnedThickness.remove(props, prepend);
    extraThickness.remove(props, prepend);
    fallbackThickness.remove(props, prepend);
  }

  public ConstPanelHeaderSettings getHeader() {
    return header;
  }

  public void setHeader(final ConstPanelHeaderSettings input) {
    if (input == null) {
      return;
    }
    if (header == null) {
      header = new PanelHeaderSettings(HEADER_KEY);
    }
    header.set(input);
  }

  public void setDataset(final boolean input) {
    dataset.set(input);
  }

  public void setFitEveryImage(final boolean input) {
    fitEveryImage.set(input);
  }

  public boolean isFitEveryImage() {
    return fitEveryImage.is();
  }

  public void setLocalAlignments(final boolean input) {
    localAlignments.set(input);
  }

  public boolean isLocalAlignments() {
    return localAlignments.is();
  }

  public void setAutoFitRange(final String input) {
    autoFitRange.set(input);
  }

  public String getAutoFitRange() {
    return autoFitRange.toString();
  }

  public void setAutoFitStep(final String input) {
    autoFitStep.set(input);
  }

  public String getAutoFitStep() {
    return autoFitStep.toString();
  }

  public void setLengthOfPieces(final boolean input) {
    lengthOfPieces.set(input);
  }

  public boolean isLengthOfPieces() {
    return lengthOfPieces.is();
  }

  public void setDefocus(final String input) {
    defocus.set(input);
  }

  public String getDefocus() {
    return defocus.toString();
  }

  public void setThickness(final String input) {
    thickness.set(input);
  }

  public String getThickness() {
    return thickness.toString();
  }

  public void setBinnedThickness(final String input) {
    binnedThickness.set(input);
  }

  public String getBinnedThickness() {
    return binnedThickness.toString();
  }

  public void setExtraThickness(final String input) {
    extraThickness.set(input);
  }

  public String getExtraThickness() {
    return extraThickness.toString();
  }

  public void setFallbackThickness(final String input) {
    fallbackThickness.set(input);
  }

  public String getFallbackThickness() {
    return fallbackThickness.toString();
  }

  public void setGold(final String input) {
    gold.set(input);
  }

  public String getGold() {
    return gold.toString();
  }

  public void setLeaveIterations(final String input) {
    leaveIterations.set(input);
  }

  public String getLeaveIterations() {
    return leaveIterations.toString();
  }

  public void setLocalAreaTargetSize(final String input) {
    localAreaTargetSize.set(input);
  }

  public String getLocalAreaTargetSize() {
    return localAreaTargetSize.toString();
  }

  public void setModelFile(final File input) {
    if (input != null) {
      modelFile.set(input.getAbsolutePath());
    }
    else {
      modelFile.reset();
    }
  }

  public String getModelFile() {
    return modelFile.toString();
  }

  public void setSizeOfPatchesXandY(final String input) {
    sizeOfPatchesXandY.set(input);
  }

  public String getSizeOfPatchesXandY() {
    return sizeOfPatchesXandY.toString();
  }

  public void setTargetNumberOfBeads(final String input) {
    targetNumberOfBeads.set(input);
  }

  public String getTargetNumberOfBeads() {
    return targetNumberOfBeads.toString();
  }

  public boolean isTargetNumberOfBeads() {
    return targetNumberOfBeads.is();
  }

  public void setAutoFitRangeAndStep(final boolean input) {
    autoFitRangeAndStep.set(input);
  }

  public boolean isAutoFitRangeAndStep() {
    return autoFitRangeAndStep.is();
  }

  public void setEnableStretching(final boolean input) {
    enableStretching.set(input);
  }

  public boolean isEnableStretching() {
    return enableStretching.is();
  }

  public void setScaleToInteger(final boolean input) {
    scaleToInteger.set(input);
  }

  public boolean isScaleToInteger() {
    return scaleToInteger.is();
  }
}
