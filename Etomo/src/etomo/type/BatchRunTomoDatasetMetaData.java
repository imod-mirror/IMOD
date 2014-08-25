package etomo.type;

import java.io.File;
import java.util.Properties;

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
public final class BatchRunTomoDatasetMetaData implements HeaderMetaDataInterface {
  public static final String rcsid = "$Id:$";

  private static final String GROUP_KEY = "dataset";

  private final HeaderMetaData headerMetaData = new HeaderMetaData();
  private final StringProperty modelFile = new StringProperty("ModelFile");
  private final EtomoBoolean2 enableStretching = new EtomoBoolean2("enableStretching");
  private final EtomoBoolean2 localAlignments = new EtomoBoolean2("LocalAlignments");
  private final EtomoNumber gold = new EtomoNumber(EtomoNumber.Type.DOUBLE, "gold");
  private final EtomoNumber targetNumberOfBeads = new EtomoNumber(
      EtomoNumber.Type.INTEGER, "TargetNumberOfBeads");
  private final StringProperty localAreaTargetSize = new StringProperty(
      "LocalAreaTargetSize");
  private final StringProperty sizeOfPatchesXandY = new StringProperty(
      "SizeOfPatchesXandY");
  private final EtomoNumber contourPieces = new EtomoNumber("contourPieces");
  private final StringProperty defocus = new StringProperty("defocus");
  private final EtomoBoolean2 autoFitRangeAndStep = new EtomoBoolean2(
      "autoFitRangeAndStep");
  private final EtomoNumber autoFitRange = new EtomoNumber(EtomoNumber.Type.DOUBLE,
      "autoFitRange");
  private final EtomoBoolean2 fitEveryImage = new EtomoBoolean2("fitEveryImage");
  private final EtomoNumber autoFitStep = new EtomoNumber(EtomoNumber.Type.DOUBLE,
      "autoFitStep");
  private final StringProperty leaveIterations = new StringProperty("LeaveIterations");
  private final EtomoBoolean2 scaleToInteger = new EtomoBoolean2("ScaleToInteger");
  private final EtomoNumber extraThickness = new EtomoNumber("extraThickness");
  private final EtomoNumber fallbackThickness = new EtomoNumber("fallbackThickness");

  private final String stackID;

  BatchRunTomoDatasetMetaData(final String stackID) {
    this.stackID = stackID;
  }

  private String getGroupKey() {
    return GROUP_KEY + "." + stackID;
  }

  private String createPrepend(String prepend) {
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
    // reset
    modelFile.reset();
    enableStretching.reset();
    localAlignments.reset();
    gold.reset();
    targetNumberOfBeads.reset();
    localAreaTargetSize.reset();
    sizeOfPatchesXandY.reset();
    contourPieces.reset();
    defocus.reset();
    autoFitRangeAndStep.reset();
    autoFitRange.reset();
    fitEveryImage.reset();
    autoFitStep.reset();
    leaveIterations.reset();
    scaleToInteger.reset();
    extraThickness.reset();
    fallbackThickness.reset();
    // load
    prepend = createPrepend(prepend);
    modelFile.load(props, prepend);
    enableStretching.load(props, prepend);
    localAlignments.load(props, prepend);
    gold.load(props, prepend);
    targetNumberOfBeads.load(props, prepend);
    localAreaTargetSize.load(props, prepend);
    sizeOfPatchesXandY.load(props, prepend);
    contourPieces.load(props, prepend);
    defocus.load(props, prepend);
    autoFitRangeAndStep.load(props, prepend);
    autoFitRange.load(props, prepend);
    fitEveryImage.load(props, prepend);
    autoFitStep.load(props, prepend);
    leaveIterations.load(props, prepend);
    scaleToInteger.load(props, prepend);
    extraThickness.load(props, prepend);
    fallbackThickness.load(props, prepend);
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    modelFile.store(props, prepend);
    enableStretching.store(props, prepend);
    localAlignments.store(props, prepend);
    gold.store(props, prepend);
    targetNumberOfBeads.store(props, prepend);
    localAreaTargetSize.store(props, prepend);
    sizeOfPatchesXandY.store(props, prepend);
    contourPieces.store(props, prepend);
    defocus.store(props, prepend);
    autoFitRangeAndStep.store(props, prepend);
    autoFitRange.store(props, prepend);
    fitEveryImage.store(props, prepend);
    autoFitStep.store(props, prepend);
    leaveIterations.store(props, prepend);
    scaleToInteger.store(props, prepend);
    extraThickness.store(props, prepend);
    fallbackThickness.store(props, prepend);
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

  public void setContourPieces(final Number input) {
    contourPieces.set(input);
  }

  public String getContourPieces() {
    return contourPieces.toString();
  }

  public void setDefocus(final String input) {
    defocus.set(input);
  }

  public String getDefocus() {
    return defocus.toString();
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
