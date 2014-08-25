package etomo.type;

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
public final class BatchRunTomoDatasetMetaData implements HeaderMetaData {
  public static final String rcsid = "$Id:$";

  private static final String GROUP_KEY = "dataset";

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
  private final EtomoNumber autoFitRange = new EtomoNumber("autoFitRange");
  private final EtomoBoolean2 fitEveryImage = new EtomoBoolean2("fitEveryImage");
  private final EtomoNumber autoFitStep = new EtomoNumber("autoFitStep");

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
  }
}
