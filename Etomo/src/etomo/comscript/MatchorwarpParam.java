package etomo.comscript;

import etomo.type.EtomoNumber;
import etomo.type.ScriptParameter;
import etomo.type.StringParameter;
import etomo.util.DatasetFiles;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2002 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 * 
 * <p> $Log$
 * <p> Revision 3.4  2010/04/28 15:59:50  sueh
 * <p> bug# 1344 Reformatted.
 * <p>
 * <p> Revision 3.3  2006/09/19 21:59:52  sueh
 * <p> bug# 928 Added residualFile, vectormodel, and clipsize.
 * <p>
 * <p> Revision 3.2  2004/04/12 16:49:27  sueh
 * <p> bug# 409 changed interface class CommandParam
 * <p>
 * <p> Revision 3.1  2004/03/06 03:46:55  sueh
 * <p> bug# 380 added useLinearInterpolation
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:00  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.5  2003/07/25 22:54:14  rickg
 * <p> CommandParam method name changes
 * <p>
 * <p> Revision 2.4  2003/06/25 22:16:29  rickg
 * <p> changed name of com script parse method to parseComScript
 * <p>
 * <p> Revision 2.3  2003/03/20 17:23:23  rickg
 * <p> Comment update
 * <p>
 * <p> Revision 2.2  2003/03/06 05:53:28  rickg
 * <p> Combine interface in progress
 * <p>
 * <p> Revision 2.1  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p> </p>
 */

public final class MatchorwarpParam implements ConstMatchorwarpParam, CommandParam {
  protected static final String RESIDUAL_FILE_KEY = "-residualfile";
  protected static final String VECTOR_MODEL_KEY = "-vectormodel";
  protected static final String RESIDUAL_FILE_DEFAULT = "patch.resid";
  protected static final String VECTOR_MODEL_DEFAULT = DatasetFiles.PATCH_VECTOR_MODEL;
  protected static final int CLIP_SIZE_DEFAULT = 600;

  private final StringParameter sizeXYZorVolume = new StringParameter("SizeXYZorVolume");
  private final ScriptParameter refineLimit = new ScriptParameter(
    EtomoNumber.Type.DOUBLE, "RefineLimit");
  protected String residualFile = null;
  protected String vectormodel = null;
  protected final EtomoNumber clipsize = new EtomoNumber("-clipsize");
  protected boolean useRefinelimit = false;
  protected String warpLimit = "";
  protected String modelFile = "";
  protected String patchFile = "";
  protected String solveFile = "";
  protected String refineFile = "";
  protected String inverseFile = "";
  protected String warpFile = "";
  protected String tempDir = "";
  protected int xLowerExclude = 0;
  protected int xUpperExclude = 0;
  protected int zLowerExclude = 0;
  protected int zUpperExclude = 0;
  protected boolean trial = false;
  protected String inputFile = "";
  protected String outputFile = "";
  protected boolean useLinearInterpolation = false;
  String structurecrit = "";
  String extentfit = "";

  public void parseComScriptCommand(final ComScriptCommand scriptCommand)
    throws BadComScriptException, FortranInputSyntaxException, InvalidParameterException {
    initializeDefaults();
    if (!scriptCommand.isKeywordValuePairs()) {
      parseComScriptCommandForBackwardsCompatibility(scriptCommand);
    }
    else {
      sizeXYZorVolume.parse(scriptCommand);
      refineLimit.parse(scriptCommand);
    }
  }

  public void updateComScriptCommand(final ComScriptCommand scriptCommand)
    throws BadComScriptException {
    scriptCommand.useKeywordValue();
    sizeXYZorVolume.updateComScript(scriptCommand);
    refineLimit.updateComScript(scriptCommand);
  }

  /**
   * Reset the state of the object to it initial defaults
   */
  public void initializeDefaults() {
    sizeXYZorVolume.reset();
    refineLimit.reset();
    useRefinelimit = false;
    residualFile = null;
    vectormodel = null;
    clipsize.reset();
    warpLimit = "";
    modelFile = "";
    patchFile = "";
    solveFile = "";
    refineFile = "";
    inverseFile = "";
    warpFile = "";
    tempDir = "";
    xLowerExclude = 0;
    xUpperExclude = 0;
    zLowerExclude = 0;
    zUpperExclude = 0;
    trial = false;
    inputFile = "";
    outputFile = "";
    useLinearInterpolation = false;
    structurecrit = "";
    extentfit = "";
  }

  /**
   * @deprecated
   * @param scriptCommand
   * @throws BadComScriptException
   * @throws FortranInputSyntaxException
   * @throws InvalidParameterException
   */
  public void parseComScriptCommandForBackwardsCompatibility(
    final ComScriptCommand scriptCommand) throws BadComScriptException,
    FortranInputSyntaxException, InvalidParameterException {
    // TODO error checking - throw exceptions for bad syntax
    String[] cmdLineArgs = scriptCommand.getCommandLineArgs();

    for (int i = 0; i < cmdLineArgs.length - 2; i++) {
      if (cmdLineArgs[i].startsWith("-siz")) {
        i++;
        sizeXYZorVolume.set(cmdLineArgs[i]);
      }

      if (cmdLineArgs[i].startsWith("-refinel")) {
        i++;
        refineLimit.set(cmdLineArgs[i]);
        useRefinelimit = true;
      }
      if (cmdLineArgs[i].startsWith(RESIDUAL_FILE_KEY)) {
        i++;
        residualFile = cmdLineArgs[i];
      }
      if (cmdLineArgs[i].startsWith(VECTOR_MODEL_KEY)) {
        i++;
        vectormodel = cmdLineArgs[i];
      }
      if (cmdLineArgs[i].startsWith(clipsize.getName())) {
        i++;
        clipsize.set(cmdLineArgs[i]);
      }
      if (cmdLineArgs[i].startsWith("-warpl")) {
        i++;
        warpLimit = cmdLineArgs[i];
      }
      if (cmdLineArgs[i].startsWith("-m")) {
        i++;
        modelFile = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-p")) {
        i++;
        patchFile = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-so")) {
        i++;
        solveFile = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-refinef")) {
        i++;
        refineFile = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-i")) {
        i++;
        inverseFile = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-warpf")) {
        i++;
        warpFile = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-te")) {
        i++;
        tempDir = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-xl")) {
        i++;
        xLowerExclude = Integer.parseInt(cmdLineArgs[i]);
      }

      if (cmdLineArgs[i].startsWith("-xu")) {
        i++;
        xUpperExclude = Integer.parseInt(cmdLineArgs[i]);
      }

      if (cmdLineArgs[i].startsWith("-zl")) {
        i++;
        zLowerExclude = Integer.parseInt(cmdLineArgs[i]);
      }

      if (cmdLineArgs[i].startsWith("-zu")) {
        i++;
        zUpperExclude = Integer.parseInt(cmdLineArgs[i]);
      }

      if (cmdLineArgs[i].startsWith("-l")) {
        i++;
        useLinearInterpolation = true;
      }

      if (cmdLineArgs[i].startsWith("-tr")) {
        trial = true;
      }

      if (cmdLineArgs[i].startsWith("-st")) {
        i++;
        structurecrit = cmdLineArgs[i];
      }

      if (cmdLineArgs[i].startsWith("-e")) {
        i++;
        extentfit = cmdLineArgs[i];
      }
    }

    inputFile = cmdLineArgs[cmdLineArgs.length - 2];
    outputFile = cmdLineArgs[cmdLineArgs.length - 1];

    // Backwards compatibility (before 3.8.25):
    // Update so that the user can look at the .resid file.
    if (residualFile == null && vectormodel == null && clipsize.isNull()) {
      residualFile = RESIDUAL_FILE_DEFAULT;
      vectormodel = VECTOR_MODEL_DEFAULT;
      clipsize.set(CLIP_SIZE_DEFAULT);
    }
  }

  /**
   * Sets the inverseFile.
   * @param inverseFile The inverseFile to set
   */
  public void setInverseFile(final String inverseFile) {
    this.inverseFile = inverseFile;
  }

  /**
   * Sets the modelFile.
   * @param modelFile The modelFile to set
   */
  public void setModelFile(final String modelFile) {
    this.modelFile = modelFile;
  }

  /**
   * Set the default patch region model file
   * @param patchFile
   */
  public void setDefaultModelFile() {
    modelFile = getDefaultPatchRegionModel();
  }

  /**
   * Sets the patchFile.
   * @param patchFile The patchFile to set
   */
  public void setPatchFile(final String patchFile) {
    this.patchFile = patchFile;
  }

  /**
   * Sets the refineLimit.
   * @param refineLimit The refineLimit to set
   */
  public void setRefineLimit(final String input) {
    refineLimit.set(refineLimit);
  }

  /**
   * Sets the solveFile.
   * @param solveFile The solveFile to set
   */
  public void setSolveFile(final String solveFile) {
    this.solveFile = solveFile;
  }

  /**
   * Sets the tempDir.
   * @param tempDir The tempDir to set
   */
  public void setTempDir(final String tempDir) {
    this.tempDir = tempDir;
  }

  /**
   * Sets the warpFile.
   * @param warpFile The warpFile to set
   */
  public void setWarpFile(final String warpFile) {
    this.warpFile = warpFile;
  }

  /**
   * Sets the warpLimit.
   * @param warpLimit The warpLimit to set
   */
  public void setWarpLimit(final String warpLimit) {
    this.warpLimit = warpLimit;
  }

  /**
   * Sets the xLowerExclude.
   * @param xLowerExclude The xLowerExclude to set
   */
  public void setXLowerExclude(final int xLowerExclude) {
    this.xLowerExclude = xLowerExclude;
  }

  /**
   * Sets the xUpperExclude.
   * @param xUpperExclude The xUpperExclude to set
   */
  public void setXUpperExclude(final int xUpperExclude) {
    this.xUpperExclude = xUpperExclude;
  }

  /**
   * Sets the zLowerExclude.
   * @param zLowerExclude The zLowerExclude to set
   */
  public void setZLowerExclude(final int zLowerExclude) {
    this.zLowerExclude = zLowerExclude;
  }

  /**
   * Sets the zUpperExclude.
   * @param zUpperExclude The zUpperExclude to set
   */
  public void setZUpperExclude(final int zUpperExclude) {
    this.zUpperExclude = zUpperExclude;
  }

  /**
   * Sets the inputFile.
   * @param inputFile The inputFile to set
   */
  public void setInputFile(final String inputFile) {
    this.inputFile = inputFile;
  }

  /**
   * Sets the outputFile.
   * @param outputFile The outputFile to set
   */
  public void setOutputFile(final String outputFile) {
    this.outputFile = outputFile;
  }

  /**
   * Sets the trial flag.
   * @param trial Specify the trial state
   */
  public void setTrial(final boolean trial) {
    this.trial = trial;
  }

  /**
   * Sets the useRefinelimit.
   * @param useRefinelimit The useRefinelimit to set
   */
  public void setUseRefinelimit(final boolean useRefinelimit) {
    this.useRefinelimit = useRefinelimit;
  }

  /**
   * Sets the refineFile.
   * @param refineFile The refineFile to set
   */
  public void setRefineFile(final String refineFile) {
    this.refineFile = refineFile;
  }

  public void setUseLinearInterpolation(final boolean useLinearInterpolation) {
    this.useLinearInterpolation = useLinearInterpolation;
  }

  /**
   * @return String
   */
  public String getInverseFile() {
    return inverseFile;
  }

  /**
   * @return String
   */
  public String getModelFile() {
    return modelFile;
  }

  public boolean isUseModelFile() {
    return !ParamUtilities.isEmpty(modelFile);
  }

  public String getPatchFile() {
    return patchFile;
  }

  public String getRefineLimit() {
    return refineLimit.toString();
  }

  /**
   * @return String
   */
  public String getSizeXYZorVolume() {
    return sizeXYZorVolume.toString();
  }

  /**
   * @return String
   */
  public String getSolveFile() {
    return solveFile;
  }

  /**
   * @return String
   */
  public String getTempDir() {
    return tempDir;
  }

  /**
   * @return String
   */
  public String getWarpFile() {
    return warpFile;
  }

  /**
   * @return String
   */
  public String getWarpLimit() {
    return warpLimit;
  }

  /**
   * @return int
   */
  public int getXLowerExclude() {
    return xLowerExclude;
  }

  /**
   * @return int
   */
  public int getXUpperExclude() {
    return xUpperExclude;
  }

  /**
   * @return int
   */
  public int getZLowerExclude() {
    return zLowerExclude;
  }

  /**
   * @return int
   */
  public int getZUpperExclude() {
    return zUpperExclude;
  }

  /**
   * @return String
   */
  public String getInputFile() {
    return inputFile;
  }

  /**
   * @return String
   */
  public String getOutputFile() {
    return outputFile;
  }

  /**
   * @return boolean
   */
  public boolean isTrial() {
    return trial;
  }

  /**
   * @return boolean
   */
  public boolean isUseRefinelimit() {
    return useRefinelimit;
  }

  /**
   * @return String
   */
  public String getRefineFile() {
    return refineFile;
  }

  public boolean isUseLinearInterpolation() {
    return useLinearInterpolation;
  }

  /**
   * Return the default patch region model file name
   * @return String
   */
  public static String getDefaultPatchRegionModel() {
    return "patch_region.mod";
  }
}
