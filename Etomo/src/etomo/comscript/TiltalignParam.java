package etomo.comscript;

import etomo.type.AxisID;
import etomo.type.EtomoNumber;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 3.6  2005/01/05 19:47:35  sueh
 * <p> bug# 567 Changed setProjectionStretch() to accept booleans.  Bug# 578
 * <p> Added AxisID to the constructor.
 * <p>
 * <p> Revision 3.5  2004/12/30 19:49:26  sueh
 * <p> bug# 567 Removed OutputModelAndResidual when writing command file.
 * <p> Already converting it to OutputModelFile and OutputResidualFile.
 * <p>
 * <p> Revision 3.4  2004/12/29 23:44:00  sueh
 * <p> bug# 567 In ParamUtilities, added the FortranInputString to parse(String...) and
 * <p> parse(StringList...).
 * <p>
 * <p> Revision 3.3  2004/12/29 01:53:12  sueh
 * <p> bug# 567 Passing ints, doubles, and strings to set functions, instead of
 * <p> EtomoNumber.
 * <p>
 * <p> Revision 3.2  2004/12/29 00:01:20  sueh
 * <p> bug# 567 Placed the version of TiltalignParam for the old-style comscript
 * <p> into OldTiltalignParam.  This version updates and parses only the new
 * <p> parameters and converts from the old-style comscript to the new
 * <p> parameters.
 * <p>
 * <p> Revision 3.1  2004/04/12 16:50:58  sueh
 * <p> bug# 409 changed interface class CommandParam
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:00  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.7  2003/10/14 20:30:56  rickg
 * <p> Bug#279  Label layout and name changes
 * <p>
 * <p> Revision 2.6  2003/10/09 23:24:10  rickg
 * <p> Bug#279  Added integer set method for nFiducials
 * <p>
 * <p> Revision 2.5  2003/08/07 17:59:06  rickg
 * <p> Merged in tilt angle fix from beta2a branch
 * <p>
 * <p> Revision 2.4  2003/07/25 22:55:04  rickg
 * <p> CommandParam method name changes
 * <p>
 * <p> Revision 2.3  2003/06/25 22:16:29  rickg
 * <p> changed name of com script parse method to parseComScript
 * <p>
 * <p> Revision 2.2.2.1  2003/08/07 16:15:44  rickg
 * <p> Fixed tiltanglespec handling to include start and step
 * <p>
 * <p> Revision 2.2  2003/03/20 17:24:45  rickg
 * <p> Comment update
 * <p>
 * <p> Revision 2.1  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.6.2.1  2003/01/24 18:33:42  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.6  2002/12/18 19:13:57  rickg
 * <p> Added setters for metro factor and cycle limit
 * <p>
 * <p> Revision 1.5  2002/12/10 21:37:01  rickg
 * <p> changed reportStddevThreshold to residualThreshold
 * <p>
 * <p> Revision 1.4  2002/12/10 18:48:21  rickg
 * <p> changed names of comscript put and get methods to
 * <p> be more understandable
 * <p>
 * <p> Revision 1.3  2002/12/06 15:22:30  rickg
 * <p> Comment where to fix
 * <p>
 * <p> Revision 1.2  2002/10/07 22:24:17  rickg
 * <p> removed unused imports
 * <p> reformat after emacs messed it up
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class TiltalignParam extends ConstTiltalignParam implements CommandParam {
  public static final String rcsid = "$Id$";

  /**
   * Constructor for TiltalignParam.
   */
  public TiltalignParam(String datasetName, AxisID axisID) {
    super(datasetName, axisID);
  }

  /**
   * Get the parameters from the ComScriptCommand
   * @param scriptCommand the ComScriptCommand containg the tiltalign command
   * and parameters.
   */
  public void parseComScriptCommand(ComScriptCommand scriptCommand)
      throws BadComScriptException, InvalidParameterException,
      FortranInputSyntaxException {
    reset();
    if (!scriptCommand.isKeywordValuePairs()) {
      OldTiltalignParam oldParam = new OldTiltalignParam();
      oldParam.parseComScriptCommand(scriptCommand);
      convertToPIP(oldParam);
    }
    else {
      modelFile = scriptCommand.getValue(modelFileString);
      imageFile = scriptCommand.getValue(imageFileString);
      outputModelFile = scriptCommand.getValue(outputModelFileString);
      outputResidualFile = scriptCommand.getValue(outputResidualFileString);

      //Use OutputModelAndResidual if OutputModelFile or OutputResidualFile are blank
      //Convert OutputModelAndResidual to OutputModelFile and OutputResidualFile
      if (outputModelFile.matches("\\s*") || outputResidualFile.matches("\\s*")) {
        String outputModelAndResidual = scriptCommand
            .getValue(outputModelAndResidualString);
        if (outputModelFile.matches("\\s*")) {
          outputModelFile = outputModelAndResidual + modelFileExtension;
        }
        if (outputResidualFile.matches("\\s*")) {
          outputResidualFile = outputModelAndResidual + residualFileExtension;
        }
      }

      outputFidXYZFile = scriptCommand.getValue(outputFidXYZFileString);
      outputTiltFile = scriptCommand.getValue(outputTiltFileString);
      outputTransformFile = scriptCommand.getValue(outputTransformFileString);
      outputZFactorFile = scriptCommand.getValue(outputZFactorFileString);
      includeStartEndInc.parseString(scriptCommand
          .getValue(includeStartEndIncString));
      includeList.parseString(scriptCommand.getValue(includeListString));
      excludeList.parseString(scriptCommand.getValue(excludeListString));
      rotationAngle.set(scriptCommand);
      separateGroup.parseString(scriptCommand.getValues(separateGroupString));
      tiltAngleSpec.set(scriptCommand, firstTiltAngleShortString,
          tiltIncrementShortString, tiltFileShortString);
      angleOffset.set(scriptCommand);
      projectionStretch.set(scriptCommand);
      rotOption.set(scriptCommand);
      rotDefaultGrouping.set(scriptCommand);
      rotNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          rotNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      rotationFixedView.set(scriptCommand);
      localRotOption.set(scriptCommand);
      localRotDefaultGrouping.set(scriptCommand);
      localRotNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          localRotNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      tiltOption.set(scriptCommand);
      tiltDefaultGrouping.set(scriptCommand);
      tiltNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          tiltNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      localTiltDefaultGrouping.set(scriptCommand);
      localTiltNondefaultGroup = ParamUtilities.setParamIfPresent(
          scriptCommand, localTiltNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      magReferenceView.set(scriptCommand);
      magOption.set(scriptCommand);
      magDefaultGrouping.set(scriptCommand);
      magNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          magNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      localMagOption.set(scriptCommand);
      localMagDefaultGrouping.set(scriptCommand);
      localMagNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          localMagNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      xStretchOption.set(scriptCommand);
      xStretchDefaultGrouping.set(scriptCommand);
      xStretchNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          xStretchNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      localXStretchOption.set(scriptCommand);
      localXStretchDefaultGrouping.set(scriptCommand);
      localXStretchNondefaultGroup = ParamUtilities.setParamIfPresent(
          scriptCommand, localXStretchNondefaultGroupString,
          nondefaultGroupSize, nondefaultGroupIntegerType);
      skewOption.set(scriptCommand);
      skewDefaultGrouping.set(scriptCommand);
      skewNondefaultGroup = ParamUtilities.setParamIfPresent(scriptCommand,
          skewNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      localSkewOption.set(scriptCommand);
      localSkewDefaultGrouping.set(scriptCommand);
      localSkewNondefaultGroup = ParamUtilities.setParamIfPresent(
          scriptCommand, localSkewNondefaultGroupString, nondefaultGroupSize,
          nondefaultGroupIntegerType);
      residualReportCriterion.set(scriptCommand);
      surfacesToAnalyze.set(scriptCommand);
      metroFactor.set(scriptCommand);
      maximumCycles.set(scriptCommand);
      axisZShift.set(scriptCommand);
      localAlignments.set(scriptCommand);
      outputLocalFile = scriptCommand.getValue(outputLocalFileString);
      ParamUtilities.setParamIfPresent(scriptCommand,
          numberOfLocalPatchesXandYString, numberOfLocalPatchesXandY);
      ParamUtilities.setParamIfPresent(scriptCommand,
          minSizeOrOverlapXandYString, minSizeOrOverlapXandY);
      ParamUtilities.setParamIfPresent(scriptCommand,
          minFidsTotalAndEachSurfaceString, minFidsTotalAndEachSurface);
    }
    String invalidReason = validate();
    if (invalidReason != null && !invalidReason.matches("\\s*")) {
      throw new InvalidParameterException(invalidReason);
    }
  }

  /**
   * Convert from the old style script to PIP:
   * modelFile = modelFile
   * imageFile = imageFile
   * Not using imageParameters
   * outputModelFile = imodFiducialPosFile + .3dmod
   * outputResidualFile = imodFiducialPosFile + .resid
   * outputFidXYZFile = asciiFiducialPosFile
   * outputTiltFile = tiltAngleSolutionFile
   * outputTransformFile = transformSolutionFile
   * outputZFactorFile new.
   * Not using solutionType.
   * Not saving includeExcludeType.
   * includeStartEndInc = includeExcludeList if includeExcludeType == 1
   * includeList = includeExcludeList if includeExcludeType == 2
   * excludeList = includeExcludeList if includeExcludeType == 3
   * rotationAngle = initialImageRotation
   * separateGroup = separateViewGroups
   * tiltAngleSpec = tiltAngleSpec;
   * angleOffset = tiltAngleOffset
   * projectionStretch new (default = false)
   * rotOption = 1 if rotationAngleSolutionType == 0
   * rotOption = 1 if rotationAngleSolutionType > 0
   * rotOption = 0 if rotationAngleSolutionType == -2
   * rotDefaultGrouping new
   * rotNondefaultGroup new
   * rotationFixedView = rotationAngleSolutionType if rotationAngleSolutionType > 0
   * localRotOption = localRotationSolution.type
   * localRotDefaultGrouping = localRotationSolution.params.getInt(0)
   * localRotNondefaultGroup = localRotationSolution.additionalGroups
   * tiltOption = tiltAngleSolution.type
   * tiltDefaultGrouping = tiltAngleSolution.params.getInt(0)
   * tiltNondefaultGroup = tiltAngleSolution.additionalGroups
   * localTiltOption = localTiltSolution.type
   * localTiltDefaultGrouping = localTiltSolution.params.getInt(0)
   * localTiltNondefaultGroup = localTiltSolution.additionalGroups
   * magReferenceView = magnificationSolution.referenceView
   * magOption = magnificationSolution.type
   * magDefaultGrouping = magnificationSolution.params.getInt(0)
   * magNondefaultGroup = magnificationSolution.additionalGroups
   * localMagOption = localMagnificationSolution.type
   * localMagDefaultGrouping = localMagnificationSolution.params.getInt(0)
   * localMagNondefaultGroup = localMagnificationSolution.additionalGroups
   * xStretchOption = xstretchSolution.type
   * xStretchDefaultGrouping = xstretchSolution.params.getInt(0)
   * localXStretchOption = localXstretchSolution.type
   * localXStretchDefaultGrouping = localXstretchSolution.params.getInt(0)
   * localXStretchNondefaultGroup = localXstretchSolution.additionalGroups
   * skewOption = skewSolution.type
   * skewDefaultGrouping = skewSolution.params.getInt(0)
   * skewNondefaultGroup = skewSolution.additionalGroups
   * localSkewOption = localSkewSolution.type
   * localSkewDefaultGrouping = localSkewSolution.params.getInt(0)
   * localSkewNondefaultGroup = localSkewSolution.additionalGroups
   * residualReportCriterion = residualThreshold
   * surfacesToAnalyze = nSurfaceAnalysis
   * metroFactor = metroFactor
   * maximumCycles = cycleLimit
   * axisZShift = tiltAxisZShift
   * localAlignments = localAlignments
   * outputLocalFile = localTransformFile
   * numberOfLocalPatchesXandY = nLocalPatches
   * minSizeOrOverlapXandY = minLocalPatchSize
   * minFidsTotalAndEachSurface = minLocalFiducials
   *   
   * @param oldParam
   */
  private void convertToPIP(OldTiltalignParam oldParam)
      throws FortranInputSyntaxException {
    modelFile = oldParam.getModelFile();
    imageFile = oldParam.getImageFile();
    //OldTiltParam only looks for IMODFiducialPosFile.  It does not check for
    //the model file or the residual file
    outputModelFile = oldParam.getIMODFiducialPosFile() + modelFileExtension;
    outputResidualFile = oldParam.getIMODFiducialPosFile()
        + residualFileExtension;
    outputFidXYZFile = oldParam.getAsciiFiducialPosFile();
    outputTiltFile = oldParam.getTiltAngleSolutionFile();
    outputTransformFile = oldParam.getTransformSolutionFile();
    //Set ExcludeList
    int includeExcludeType = oldParam.getIncludeExcludeType();
    if (includeExcludeType == 1) {
      excludeList = oldParam.getIncludeExcludeList();
    }
    else if (includeExcludeType == 2) {
      excludeList = oldParam.getIncludeExcludeList();
    }
    else if (includeExcludeType == 3) {
      excludeList = oldParam.getIncludeExcludeList();
    }

    rotationAngle.set(oldParam.getInitialImageRotation());
    separateGroup = oldParam.getSeparateViewGroups();
    tiltAngleSpec.set(oldParam.getTiltAngleSpec());
    angleOffset.set(oldParam.getTiltAngleOffset());
    //Set RotationAngleSolutionType and RotationFixedView
    int rotationAngleSolutionType = oldParam.getRotationAngleSolutionType();
    if (rotationAngleSolutionType >= 0) {
      rotOption.set(1);
      if (rotationAngleSolutionType > 0) {
        rotationFixedView.set(rotationAngleSolutionType);
      }
    }
    else if (rotationAngleSolutionType == -2) {
      rotOption.set(0);
    }
    else {
      rotOption.set(rotationAngleSolutionType);
    }

    localRotNondefaultGroup = setSolution(localRotOption,
        localRotDefaultGrouping, null, oldParam.getLocalRotationSolution());
    tiltNondefaultGroup = setSolution(tiltOption, tiltDefaultGrouping, null,
        oldParam.getTiltAngleSolution());
    localTiltNondefaultGroup = setSolution(localTiltOption,
        localTiltDefaultGrouping, null, oldParam.getLocalTiltSolution());
    magNondefaultGroup = setSolution(magOption, magDefaultGrouping,
        magReferenceView, oldParam.getMagnificationSolution());
    localMagNondefaultGroup = setSolution(localMagOption,
        localMagDefaultGrouping, null, oldParam.getLocalMagnificationSolution());
    xStretchNondefaultGroup = setSolution(xStretchOption,
        xStretchDefaultGrouping, null, oldParam.getXstretchSolution());
    localXStretchNondefaultGroup = setSolution(localXStretchOption,
        localXStretchDefaultGrouping, null, oldParam.getLocalXstretchSolution());
    skewNondefaultGroup = setSolution(skewOption, skewDefaultGrouping, null,
        oldParam.getSkewSolution());
    localSkewNondefaultGroup = setSolution(localSkewOption,
        localSkewDefaultGrouping, null, oldParam.getLocalSkewSolution());
    residualReportCriterion.set(oldParam.getResidualThreshold());
    surfacesToAnalyze.set(oldParam.getNSurfaceAnalysis());
    metroFactor.set(oldParam.getMetroFactor());
    maximumCycles.set(oldParam.getCycleLimit());
    axisZShift.set(oldParam.getTiltAxisZShift());
    localAlignments.set(oldParam.getLocalAlignments());
    outputLocalFile = oldParam.getLocalTransformFile();
    numberOfLocalPatchesXandY = oldParam.getNLocalPatches();
    minSizeOrOverlapXandY = oldParam.getMinLocalPatchSize();
    minFidsTotalAndEachSurface = oldParam.getMinLocalFiducials();
    setOutputZFactorFile();
  }

  private FortranInputString[] setSolution(EtomoNumber option,
      EtomoNumber defaultGrouping, EtomoNumber referenceView,
      TiltalignSolution solution) throws FortranInputSyntaxException {
    if (solution == null) {
      return null;
    }
    if (option != null) {
      option.set(solution.type);
    }
    if (defaultGrouping != null && solution.params != null
        && solution.params.size() > 0 && !solution.params.isDefault(0)) {
      defaultGrouping.set(solution.params.getInt(0));
    }
    if (referenceView != null && solution.referenceView != null
        && solution.referenceView.size() > 0
        && !solution.referenceView.isDefault(0)) {
      referenceView.set(solution.referenceView.getInt(0));
    }
    return ParamUtilities.parse(solution.additionalGroups, nondefaultGroupIntegerType, nondefaultGroupSize);
  }

  /**
   * Update the script command with the
   */
  public void updateComScriptCommand(ComScriptCommand scriptCommand)
      throws BadComScriptException {
    String invalidReason = validate();
    if (invalidReason != null && !invalidReason.matches("\\s*")) {
      throw new BadComScriptException(invalidReason);
    }
    //  Switch to keyword/value pairs
    scriptCommand.useKeywordValue();

    ParamUtilities.updateScriptParameter(scriptCommand, modelFileString,
        modelFile);
    scriptCommand.deleteKey(outputModelAndResidualString);
    ParamUtilities.updateScriptParameter(scriptCommand, outputModelFileString,
        outputModelFile);
    ParamUtilities.updateScriptParameter(scriptCommand,
        outputResidualFileString, outputResidualFile);
    ParamUtilities.updateScriptParameter(scriptCommand, outputFidXYZFileString,
        outputFidXYZFile);
    ParamUtilities.updateScriptParameter(scriptCommand, outputTiltFileString,
        outputTiltFile);
    ParamUtilities.updateScriptParameter(scriptCommand,
        outputTransformFileString, outputTransformFile);
    ParamUtilities.updateScriptParameter(scriptCommand,
        outputZFactorFileString, outputZFactorFile);
    ParamUtilities.updateScriptParameter(scriptCommand,
        includeStartEndIncString, includeStartEndInc);
    ParamUtilities.updateScriptParameter(scriptCommand, includeListString,
        includeList);
    ParamUtilities.updateScriptParameter(scriptCommand, excludeListString,
        excludeList);
    rotationAngle.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand, separateGroupString,
        separateGroup);
    tiltAngleSpec.update(scriptCommand);
    angleOffset.update(scriptCommand);
    projectionStretch.update(scriptCommand);
    rotOption.update(scriptCommand);
    rotDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        rotNondefaultGroupString, rotNondefaultGroup);
    rotationFixedView.update(scriptCommand);
    tiltOption.update(scriptCommand);
    tiltDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        tiltNondefaultGroupString, tiltNondefaultGroup);
    magReferenceView.update(scriptCommand);
    magOption.update(scriptCommand);
    magDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        magNondefaultGroupString, magNondefaultGroup);
    xStretchOption.update(scriptCommand);
    xStretchDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        xStretchNondefaultGroupString, xStretchNondefaultGroup);
    skewOption.update(scriptCommand);
    skewDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        skewNondefaultGroupString, skewNondefaultGroup);
    residualReportCriterion.update(scriptCommand);
    surfacesToAnalyze.update(scriptCommand);
    metroFactor.update(scriptCommand);
    maximumCycles.update(scriptCommand);
    axisZShift.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        minSizeOrOverlapXandYString, minSizeOrOverlapXandY);
    ParamUtilities.updateScriptParameter(scriptCommand,
        minFidsTotalAndEachSurfaceString, minFidsTotalAndEachSurface);
    localRotOption.update(scriptCommand);
    localRotDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        localRotNondefaultGroupString, localRotNondefaultGroup);
    localTiltOption.update(scriptCommand);
    localTiltDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        localTiltNondefaultGroupString, localTiltNondefaultGroup);
    localMagOption.update(scriptCommand);
    localMagDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        localMagNondefaultGroupString, localMagNondefaultGroup);
    localXStretchOption.update(scriptCommand);
    localXStretchDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        localXStretchNondefaultGroupString, localXStretchNondefaultGroup);
    localSkewOption.update(scriptCommand);
    localSkewDefaultGrouping.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand,
        localSkewNondefaultGroupString, localSkewNondefaultGroup);
    localAlignments.update(scriptCommand);
    ParamUtilities.updateScriptParameter(scriptCommand, outputLocalFileString,
        outputLocalFile);
    ParamUtilities.updateScriptParameter(scriptCommand,
        numberOfLocalPatchesXandYString, numberOfLocalPatchesXandY);
  }

  public void initializeDefaults() {
    reset();
  }

  /**
   * @param angleOffset The angleOffset to set.
   */
  public void setAngleOffset(String angleOffset) {
    this.angleOffset.set(angleOffset);
  }

  /**
   * @param axisZShift The axisZShift to set.
   */
  public void setAxisZShift(double axisZShift) {
    this.axisZShift.set(axisZShift);
  }

  /**
   * @param excludeList The excludeList to set.
   */
  public void setExcludeList(String excludeList) {
    this.excludeList.parseString(excludeList);
  }

  /**
   * @param imageFile The imageFile to set.
   */
  public void setImageFile(String imageFile) {
    this.imageFile = imageFile;
  }

  /**
   * @param localAlignments The localAlignments to set.
   */
  public void setLocalAlignments(boolean localAlignments) {
    this.localAlignments.set(localAlignments);
  }

  /**
   * @param localMagDefaultGrouping The localMagDefaultGrouping to set.
   */
  public void setLocalMagDefaultGrouping(String localMagDefaultGrouping) {
    this.localMagDefaultGrouping.set(localMagDefaultGrouping);
  }

  /**
   * @param localMagNondefaultGroup The localMagNondefaultGroup to set.
   */
  public void setLocalMagNondefaultGroup(String localMagNondefaultGroup)
      throws FortranInputSyntaxException {
    this.localMagNondefaultGroup = ParamUtilities.parse(
        localMagNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param localMagOption The localMagOption to set.
   */
  public void setLocalMagOption(int localMagOption) {
    this.localMagOption.set(localMagOption);
  }

  /**
   * @param localRotDefaultGrouping The localRotDefaultGrouping to set.
   */
  public void setLocalRotDefaultGrouping(String localRotDefaultGrouping) {
    this.localRotDefaultGrouping.set(localRotDefaultGrouping);
  }

  /**
   * @param localRotNondefaultGroup The localRotNondefaultGroup to set.
   */
  public void setLocalRotNondefaultGroup(String localRotNondefaultGroup)
      throws FortranInputSyntaxException {
    this.localRotNondefaultGroup = ParamUtilities.parse(
        localRotNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param localRotOption The localRotOption to set.
   */
  public void setLocalRotOption(int localRotOption) {
    this.localRotOption.set(localRotOption);
  }

  /**
   * @param localSkewDefaultGrouping The localSkewDefaultGrouping to set.
   */
  public void setLocalSkewDefaultGrouping(String localSkewDefaultGrouping) {
    this.localSkewDefaultGrouping.set(localSkewDefaultGrouping);
  }

  /**
   * @param localSkewNondefaultGroup The localSkewNondefaultGroup to set.
   */
  public void setLocalSkewNondefaultGroup(String localSkewNondefaultGroup)
      throws FortranInputSyntaxException {
    this.localSkewNondefaultGroup = ParamUtilities.parse(
        localSkewNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param localSkewOption The localSkewOption to set.
   */
  public void setLocalSkewOption(int localSkewOption) {
    this.localSkewOption.set(localSkewOption);
  }

  /**
   * @param localTiltDefaultGrouping The localTiltDefaultGrouping to set.
   */
  public void setLocalTiltDefaultGrouping(String localTiltDefaultGrouping) {
    this.localTiltDefaultGrouping.set(localTiltDefaultGrouping);
  }

  /**
   * @param localTiltNondefaultGroup The localTiltNondefaultGroup to set.
   */
  public void setLocalTiltNondefaultGroup(String localTiltNondefaultGroup)
      throws FortranInputSyntaxException {
    this.localTiltNondefaultGroup = ParamUtilities.parse(
        localTiltNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param localTiltOption The localTiltOption to set.
   */
  public void setLocalTiltOption(int localTiltOption) {
    this.localTiltOption.set(localTiltOption);
  }

  /**
   * @param localXStretchDefaultGrouping The localXStretchDefaultGrouping to set.
   */
  public void setLocalXStretchDefaultGrouping(
      String localXStretchDefaultGrouping) {
    this.localXStretchDefaultGrouping.set(localXStretchDefaultGrouping);
  }

  /**
   * @param localXStretchNondefaultGroup The localXStretchNondefaultGroup to set.
   */
  public void setLocalXStretchNondefaultGroup(
      String localXStretchNondefaultGroup) throws FortranInputSyntaxException {
    this.localXStretchNondefaultGroup = ParamUtilities.parse(
        localXStretchNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param localXStretchOption The localXStretchOption to set.
   */
  public void setLocalXStretchOption(int localXStretchOption) {
    this.localXStretchOption.set(localXStretchOption);
  }

  /**
   * @param magDefaultGrouping The magDefaultGrouping to set.
   */
  public void setMagDefaultGrouping(String magDefaultGrouping) {
    this.magDefaultGrouping.set(magDefaultGrouping);
  }

  /**
   * @param magNondefaultGroup The magNondefaultGroup to set.
   */
  public void setMagNondefaultGroup(String magNondefaultGroup)
      throws FortranInputSyntaxException {
    this.magNondefaultGroup = ParamUtilities.parse(magNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param magOption The magOption to set.
   */
  public void setMagOption(int magOption) {
    this.magOption.set(magOption);
  }

  /**
   * @param magReferenceView The magReferenceView to set.
   */
  public void setMagReferenceView(String magReferenceView) {
    this.magReferenceView.set(magReferenceView);
  }

  /**
   * @param maximumCycles The maximumCycles to set.
   */
  public void setMaximumCycles(String maximumCycles) {
    this.maximumCycles.set(maximumCycles);
  }

  /**
   * @param metroFactor The metroFactor to set.
   */
  public void setMetroFactor(String metroFactor) {
    this.metroFactor.set(metroFactor);
  }

  /**
   * @param minFidsTotalAndEachSurface The minFidsTotalAndEachSurface to set.
   */
  public void setMinFidsTotalAndEachSurface(String minFidsTotalAndEachSurface)
      throws FortranInputSyntaxException {
    this.minFidsTotalAndEachSurface.validateAndSet(minFidsTotalAndEachSurface);
  }

  /**
   * @param minSizeOrOverlapXandY The minSizeOrOverlapXandY to set.
   */
  public void setMinSizeOrOverlapXandY(String minSizeOrOverlapXandY)
      throws FortranInputSyntaxException {
    this.minSizeOrOverlapXandY.validateAndSet(minSizeOrOverlapXandY);
  }

  /**
   * @param modelFile The modelFile to set.
   */
  public void setModelFile(String modelFile) {
    this.modelFile = modelFile;
  }

  /**
   * @param numberOfLocalPatchesXandY The numberOfLocalPatchesXandY to set.
   */
  public void setNumberOfLocalPatchesXandY(String numberOfLocalPatchesXandY)
      throws FortranInputSyntaxException {
    this.numberOfLocalPatchesXandY.validateAndSet(numberOfLocalPatchesXandY);
  }

  /**
   * @param outputFidXYZFile The outputFidXYZFile to set.
   */
  public void setOutputFidXYZFile(String outputFidXYZFile) {
    this.outputFidXYZFile = outputFidXYZFile;
  }

  /**
   * @param outputLocalFile The outputLocalFile to set.
   */
  public void setOutputLocalFile(String outputLocalFile) {
    this.outputLocalFile = outputLocalFile;
  }

  /**
   * @param outputModelFile The outputModelFile to set.
   */
  public void setOutputModelFile(String outputModelFile) {
    this.outputModelFile = outputModelFile;
  }

  /**
   * @param outputResidualFile The outputResidualFile to set.
   */
  public void setOutputResidualFile(String outputResidualFile) {
    this.outputResidualFile = outputResidualFile;
  }

  /**
   * @param outputTiltFile The outputTiltFile to set.
   */
  public void setOutputTiltFile(String outputTiltFile) {
    this.outputTiltFile = outputTiltFile;
  }

  /**
   * @param outputTransformFile The outputTransformFile to set.
   */
  public void setOutputTransformFile(String outputTransformFile) {
    this.outputTransformFile = outputTransformFile;
  }

  /**
   * This must called after skewOption, or localAlignment, and localSkewOption
   * have been set.
   * @param outputZFactorFile The outputZFactorFile to set.
   */
  public void setOutputZFactorFile() {
    if (useOutputZFactorFile()) {
      outputZFactorFile = datasetName + axisID.getExtension()
          + zFactorFileExtension;
    }
    else {
      outputZFactorFile = "";
    }
  }
   
  /**
   * This must called after skewOption, or localAlignment, and localSkewOption
   * have been set.
   * @return
   */
  public boolean useOutputZFactorFile() {
    return !skewOption.equals(FIXED_OPTION)
        || (localAlignments.is() && !localSkewOption.equals(FIXED_OPTION));
  }

  /**
   * @param projectionStretch The projectionStretch to set.
   */
  public void setProjectionStretch(boolean projectionStretch) {
    this.projectionStretch.set(projectionStretch);
  }

  /**
   * @param residualReportCriterion The residualReportCriterion to set.
   */
  public void setResidualReportCriterion(double residualReportCriterion) {
    this.residualReportCriterion.set(residualReportCriterion);
  }

  /**
   * @param rotationAngle The rotationAngle to set.
   */
  public void setRotationAngle(double rotationAngle) {
    this.rotationAngle.set(rotationAngle);
  }

  /**
   * @param rotationFixedView The rotationFixedView to set.
   */
  public void setRotationFixedView(int rotationFixedView) {
    this.rotationFixedView.set(rotationFixedView);
  }

  /**
   * @param rotDefaultGrouping The rotDefaultGrouping to set.
   */
  public void setRotDefaultGrouping(String rotDefaultGrouping) {
    this.rotDefaultGrouping.set(rotDefaultGrouping);
  }

  /**
   * @param rotNondefaultGroup The rotNondefaultGroup to set.
   */
  public void setRotNondefaultGroup(String rotNondefaultGroup) throws FortranInputSyntaxException {
    this.rotNondefaultGroup = ParamUtilities.parse(rotNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param rotOption The rotOption to set.
   */
  public void setRotOption(int rotOption) {
    this.rotOption.set(rotOption);
  }

  /**
   * @param separateGroup The separateGroup to set.
   */
  public void setSeparateGroup(String separateGroup) {
    this.separateGroup.parseString(separateGroup);
  }

  /**
   * @param skewDefaultGrouping The skewDefaultGrouping to set.
   */
  public void setSkewDefaultGrouping(String skewDefaultGrouping) {
    this.skewDefaultGrouping.set(skewDefaultGrouping);
  }

  /**
   * @param skewNondefaultGroup The skewNondefaultGroup to set.
   */
  public void setSkewNondefaultGroup(String skewNondefaultGroup)
      throws FortranInputSyntaxException {
    this.skewNondefaultGroup = ParamUtilities.parse(skewNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param skewOption The skewOption to set.
   */
  public void setSkewOption(int skewOption) {
    this.skewOption.set(skewOption);
  }

  /**
   * @param surfacesToAnalyze The surfacesToAnalyze to set.
   */
  public void setSurfacesToAnalyze(int surfacesToAnalyze) {
    this.surfacesToAnalyze.set(surfacesToAnalyze);
  }

  /**
   * @param tiltDefaultGrouping The tiltDefaultGrouping to set.
   */
  public void setTiltDefaultGrouping(String tiltDefaultGrouping) {
    this.tiltDefaultGrouping.set(tiltDefaultGrouping);
  }

  /**
   * @param tiltNondefaultGroup The tiltNondefaultGroup to set.
   */
  public void setTiltNondefaultGroup(String tiltNondefaultGroup)
      throws FortranInputSyntaxException {
    this.tiltNondefaultGroup = ParamUtilities.parse(tiltNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param tiltOption The tiltOption to set.
   */
  public void setTiltOption(int tiltOption) {
    this.tiltOption.set(tiltOption);
  }

  /**
   * @param stretchDefaultGrouping The xStretchDefaultGrouping to set.
   */
  public void setXStretchDefaultGrouping(String stretchDefaultGrouping) {
    xStretchDefaultGrouping.set(stretchDefaultGrouping);
  }

  /**
   * @param stretchNondefaultGroup The xStretchNondefaultGroup to set.
   */
  public void setXStretchNondefaultGroup(String stretchNondefaultGroup)
      throws FortranInputSyntaxException {
    xStretchNondefaultGroup = ParamUtilities
        .parse(stretchNondefaultGroup, true, nondefaultGroupSize);
  }

  /**
   * @param stretchOption The xStretchOption to set.
   */
  public void setXStretchOption(int stretchOption) {
    xStretchOption.set(stretchOption);
  }

}