package etomo.ui;

import java.io.File;
import java.io.IOException;

import etomo.ApplicationManager;
import etomo.Arguments;
import etomo.EtomoDirector;
import etomo.comscript.FortranInputSyntaxException;
import etomo.logic.DatasetTool;
import etomo.logic.SeedingMethod;
import etomo.logic.TrackingMethod;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.DirectiveDef;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.ConstEtomoNumber;
import etomo.type.ConstMetaData;
import etomo.type.DataFileType;
import etomo.type.DialogExitState;
import etomo.type.DirectiveFileType;
import etomo.type.MetaData;
import etomo.type.UserConfiguration;
import etomo.type.ViewType;
import etomo.ui.swing.SetupDialogExpert;
import etomo.ui.swing.UIHarness;
import etomo.util.DatasetFiles;
import etomo.util.InvalidParameterException;
import etomo.util.MRCHeader;

/**
 * <p>Description: Class to handle the tomogram reconstruction setup, with or without a
 * front-end.  Handles headless, directive-based automation as well as an interface based
 * setup - with or without parameter-based automation.</p>
 * <p/>
 * <p>Copyright: Copyright 2012 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class SetupReconUIHarness {
  public static final String rcsid = "$Id:$";

  private static final String NO_GUI_ERROR_TITLE = "No GUI";
  private static final String NO_GUI_ERROR_MESSAGE = "GUI not found.  To run automation "
    + "without the GUI, use the " + Arguments.DIRECTIVE_TAG + " option.";

  private final ApplicationManager manager;
  private final AxisID axisID;

  private SetupDialogExpert expert = null;
  private DirectiveFileCollection directiveFileCollection = null;
  private boolean setFEIPixelSize = false;

  public SetupReconUIHarness(final ApplicationManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
  }

  /**
   * Runs the doAutomation function in the dialog expert, or handles an automation
   * directive file.
   */
  public void doAutomation() {
    if (!EtomoDirector.INSTANCE.getArguments().isDirective()) {
      if (expert != null) {
        expert.doAutomation();
      }
      else {
        UIHarness.INSTANCE.openMessageDialog(manager, NO_GUI_ERROR_MESSAGE,
          NO_GUI_ERROR_TITLE);
      }
      return;
    }
    // Headless automation using directives
    directiveFileCollection = new DirectiveFileCollection(manager, axisID);
    DirectiveFile batchDirectiveFile = DirectiveFile.getArgInstance(manager, axisID);
    directiveFileCollection.setup(batchDirectiveFile);
    if (!doDirectiveAutomation()) {
      UIHarness.INSTANCE.exit(axisID, 1);
    }
    else {
      UIHarness.INSTANCE.exit(axisID, 0);
    }
  }

  private boolean doDirectiveAutomation() {
    if (directiveFileCollection == null) {
      return false;
    }
    initializeFields(manager.getConstMetaData(),
      EtomoDirector.INSTANCE.getUserConfiguration());
    AxisType axisType = AxisType.SINGLE_AXIS;
    if (directiveFileCollection.isValue(DirectiveDef.DUAL)) {
      axisType = AxisType.DUAL_AXIS;
    }
    if (!DatasetTool.validateDatasetName(manager, null, axisID, new File(
      getPropertyUserDir()), directiveFileCollection.getValue(DirectiveDef.NAME),
      DataFileType.RECON, axisType, true)) {
      return false;
    }
    if (directiveFileCollection.isValue(DirectiveDef.SCAN_HEADER)
      && (!directiveFileCollection.contains(DirectiveDef.PIXEL) || !directiveFileCollection
        .contains(DirectiveDef.ROTATION))) {
      scanHeaderAction(directiveFileCollection);
    }
    if (manager.doneSetupDialog(true)) {
      return true;
    }
    return false;
  }

  /**
   * Called by the manager when not headless.
   *
   * @return
   */
  public SetupDialogExpert getSetupDialogExpert() {
    if (EtomoDirector.INSTANCE.getArguments().isHeadless()) {
      UIHarness.INSTANCE.openMessageDialog(manager, NO_GUI_ERROR_MESSAGE,
        NO_GUI_ERROR_TITLE);
      return null;
    }
    if (expert == null) {
      File distortionDir =
        DatasetFiles.getDistortionDir(manager, manager.getPropertyUserDir(), axisID);
      expert =
        SetupDialogExpert.getInstance(manager, this, distortionDir != null
          && distortionDir.exists());
    }
    return expert;
  }

  public void freeDialog() {
    expert = null;
  }

  private SetupReconInterface getSetupReconInterface() {
    if (directiveFileCollection != null) {
      return directiveFileCollection;
    }
    if (expert != null) {
      return expert.getSetupReconInterface();
    }
    UIHarness.INSTANCE.openMessageDialog(manager, NO_GUI_ERROR_MESSAGE,
      NO_GUI_ERROR_TITLE);
    return null;
  }

  public DialogExitState getExitState() {
    if (directiveFileCollection != null) {
      return DialogExitState.EXECUTE;
    }
    if (expert != null) {
      return expert.getExitState();
    }
    UIHarness.INSTANCE.openMessageDialog(manager, NO_GUI_ERROR_MESSAGE,
      NO_GUI_ERROR_TITLE);
    return null;
  }

  /**
   * This is functionality is mostly duplicated by the validate dataset functions in the
   * logic package.  Not worth duplicating for headless automation.
   *
   * @return
   */
  public boolean checkForSharedDirectory() {
    if (expert != null) {
      return expert.checkForSharedDirectory();
    }
    return false;
  }

  public File getWorkingDirectory() {
    if (directiveFileCollection != null) {
      return new File(getPropertyUserDir());
    }
    if (expert != null) {
      return expert.getWorkingDirectory();
    }
    UIHarness.INSTANCE.openMessageDialog(manager, NO_GUI_ERROR_MESSAGE,
      NO_GUI_ERROR_TITLE);
    return null;
  }

  public MetaData getMetaData() {
    SetupReconInterface setupInterface = getSetupReconInterface();
    if (setupInterface == null) {
      return null;
    }
    MetaData metaData = new MetaData(manager, manager.getLogProperties());
    metaData.setAxisType(getAxisType());
    // The dataset name needs to be set after the axis type so the metadata object
    // modifies the file ending correctly (if a file name is used).
    metaData.setDatasetName(setupInterface.getDataset());
    return metaData;
  }

  public AxisType getAxisType() {
    SetupReconInterface setupInterface = getSetupReconInterface();
    if (setupInterface == null) {
      return null;
    }
    if (setupInterface.isSingleAxisSelected()) {
      return AxisType.SINGLE_AXIS;
    }
    else {
      return AxisType.DUAL_AXIS;
    }
  }

  private ViewType getViewType(final SetupReconInterface setupInterface) {
    if (setupInterface.isSingleViewSelected()) {
      return ViewType.SINGLE_VIEW;
    }
    else {
      return ViewType.MONTAGE;
    }
  }

  /**
   * Get the directory in which the user wants to create the dataset.
   *
   * @return
   */
  public String getPropertyUserDir() {
    if (directiveFileCollection != null
      && directiveFileCollection.contains(DirectiveDef.DATASET_DIRECTORY)) {
      return directiveFileCollection.getValue(DirectiveDef.DATASET_DIRECTORY);
    }
    else if (expert != null) {
      File dir = expert.getDir();
      if (dir != null) {
        return dir.getAbsolutePath();
      }
    }
    return manager.getPropertyUserDir();
  }

  public boolean scanHeaderAction(final SetupReconInterface setupInterface) {
    MRCHeader header = readMRCHeader(getStackFileName(setupInterface));
    if (header == null) {
      return false;
    }
    // Set the image rotation if available
    ConstEtomoNumber imageRotation = header.getImageRotation();
    if (!imageRotation.isNull()) {
      setupInterface.setImageRotation(imageRotation.toString());
    }
    // set the pixel size if available
    double xPixelSize = header.getXPixelSize().getDouble();
    double yPixelSize = header.getYPixelSize().getDouble();
    if (Double.isNaN(xPixelSize) || Double.isNaN(yPixelSize)) {
      UIHarness.INSTANCE.openMessageDialog(manager,
        "Pixel size is not defined in the image file header", "Pixel size is missing",
        AxisID.ONLY);
      return false;
    }
    if (xPixelSize != yPixelSize) {
      UIHarness.INSTANCE.openMessageDialog(manager,
        "X & Y pixels sizes are different, don't know what to do",
        "Pixel sizes are different", AxisID.ONLY);
      return false;
    }
    if (xPixelSize == 1.0) {
      UIHarness.INSTANCE.openMessageDialog(manager,
        "Pixel size is not defined in the image file header", "Pixel size is missing",
        AxisID.ONLY);
      return false;
    }
    xPixelSize = xPixelSize / 10.0;
    setupInterface.setPixelSize(Math.round(xPixelSize * 1000000.0) / 1000000.0);
    // set binning
    int binning = header.getBinning();
    if (binning == Integer.MIN_VALUE) {
      binning = 1;
    }
    setupInterface.setBinning(binning);
    ConstEtomoNumber twodir = header.getTwodir();
    if (!twodir.isNull()) {
      setupInterface.setTwodir(AxisID.FIRST, twodir.getDouble());
    }
    // B stack
    String bStack = getBAxisStackFileName(setupInterface);
    if (bStack != null) {
      header = readMRCHeader(bStack);
      if (header != null) {
        twodir = header.getTwodir();
        if (!twodir.isNull()) {
          setupInterface.setTwodir(AxisID.SECOND, twodir.getDouble());
        }
      }
    }
    return true;
  }

  /**
   * Construction and read an MRCHeader object.
   *
   * @return the MRCHeader object
   */
  private MRCHeader readMRCHeader(final String stackFileName) {
    // Run header on the dataset to the extract whatever information is
    // available
    if (stackFileName == null) {
      return null;
    }
    MRCHeader header =
      MRCHeader.getInstance(getPropertyUserDir(), stackFileName, AxisID.ONLY);
    try {
      if (!header.read(manager)) {
        UIHarness.INSTANCE.openMessageDialog(manager, "File does not exist.",
          "Entry Error", AxisID.ONLY);
        return null;
      }
    }
    catch (InvalidParameterException except) {
      UIHarness.INSTANCE.openMessageDialog(manager, except.getMessage(),
        "Invalid Parameter Exception", AxisID.ONLY);
    }
    catch (IOException except) {
      UIHarness.INSTANCE.openMessageDialog(manager, except.getMessage(), "IO Exception",
        AxisID.ONLY);
    }
    return header;
  }

  /**
   * Get the A or only stack name using dialog.getDataset()
   *
   * @return
   */
  private String getStackFileName(final SetupReconInterface setupInterface) {
    // Get the dataset name from the UI object
    String datasetName = setupInterface.getDataset();
    if (datasetName == null || datasetName.equals("")) {
      UIHarness.INSTANCE.openMessageDialog(manager, "Dataset name has not been entered",
        "Missing dataset name", AxisID.ONLY);
      return null;
    }
    // Add the appropriate extension onto the filename if necessary
    if (!datasetName.endsWith(DatasetTool.STANDARD_DATASET_EXT)
      && !datasetName.endsWith(DatasetTool.ALTERNATE_DATASET_EXT)) {
      String datasetNameSt;
      if (setupInterface.isDualAxisSelected()) {
        datasetNameSt = datasetName + "a" + DatasetTool.STANDARD_DATASET_EXT;
      }
      else {
        datasetNameSt = datasetName + DatasetTool.STANDARD_DATASET_EXT;
      }
      if (new File(datasetNameSt).exists()) {
        return datasetNameSt;
      }
      String datasetNameMrc;
      if (setupInterface.isDualAxisSelected()) {
        datasetNameMrc = datasetName + "a" + DatasetTool.ALTERNATE_DATASET_EXT;
      }
      else {
        datasetNameMrc = datasetName + DatasetTool.ALTERNATE_DATASET_EXT;
      }
      if (new File(datasetNameMrc).exists()) {
        return datasetNameMrc;
      }
      return datasetNameSt;
    }
    return datasetName;
  }

  /**
   * Get the B  stack name using dialog.getDataset()
   *
   * @return
   */
  private String getBAxisStackFileName(final SetupReconInterface setupInterface) {
    if (!setupInterface.isDualAxisSelected()) {
      return null;
    }
    // Get the dataset name from the UI object
    String datasetName = setupInterface.getDataset();
    if (datasetName == null || datasetName.equals("")) {
      return null;
    }
    // Add the appropriate extension onto the filename if necessary
    if (!datasetName.endsWith(DatasetTool.STANDARD_DATASET_EXT)
      && !datasetName.endsWith(DatasetTool.ALTERNATE_DATASET_EXT)) {
      String datasetNameSt;
      datasetNameSt = datasetName + "b" + DatasetTool.STANDARD_DATASET_EXT;
      if (new File(datasetNameSt).exists()) {
        return datasetNameSt;
      }
      String datasetNameMrc;
      datasetNameMrc = datasetName + "b" + DatasetTool.ALTERNATE_DATASET_EXT;
      if (new File(datasetNameMrc).exists()) {
        return datasetNameMrc;
      }
      return datasetNameSt;
    }
    int index = datasetName.lastIndexOf("a");
    if (index == -1) {
      return null;
    }
    return datasetName.substring(0, index) + "b" + datasetName.substring(index + 1);
  }

  public boolean isValid() {
    SetupReconInterface setupInterface = getSetupReconInterface();
    if (setupInterface == null) {
      return false;
    }
    String errorMessageTitle = "Setup Dialog Error";
    String datasetText = setupInterface.getDataset();
    String panelErrorMessage;
    if (datasetText.equals("")) {
      UIHarness.INSTANCE.openMessageDialog(manager, "Dataset name has not been entered.",
        errorMessageTitle, AxisID.ONLY);
      return false;
    }
    File dataset = new File(datasetText);
    String datasetFileName = dataset.getName();
    if (datasetFileName.equals("a" + DatasetTool.STANDARD_DATASET_EXT)
      || datasetFileName.equals("b" + DatasetTool.STANDARD_DATASET_EXT)
      || datasetFileName.equals(".")) {
      UIHarness.INSTANCE.openMessageDialog(manager, "The name " + datasetFileName
        + " cannot be used as a dataset name.", errorMessageTitle, AxisID.ONLY);
      return false;
    }
    // validate image distortion field file name
    // optional
    // file must exist
    String distortionFileText = setupInterface.getDistortionFile();
    if (distortionFileText != null && !distortionFileText.equals("")) {
      File distortionFile = new File(distortionFileText);
      if (!distortionFile.exists()) {
        String distortionFileName = distortionFile.getName();
        UIHarness.INSTANCE.openMessageDialog(manager, "The image distortion field file "
          + distortionFileName + " does not exist.", errorMessageTitle, AxisID.ONLY);
        return false;
      }
    }
    // validate mag gradient field file name
    // optional
    // file must exist
    String magGradientFileText = setupInterface.getMagGradientFile();
    if (magGradientFileText != null && !magGradientFileText.equals("")) {
      File magGradientFile = new File(magGradientFileText);
      if (!magGradientFile.exists()) {
        String magGradientFileName = magGradientFile.getName();
        UIHarness.INSTANCE
          .openMessageDialog(manager, "The mag gradients correction file "
            + magGradientFileName + " does not exist.", errorMessageTitle, AxisID.ONLY);
        return false;
      }
    }
    if (!setupInterface.validateTiltAngle(AxisID.FIRST, errorMessageTitle)) {
      return false;
    }
    if (!setupInterface.validateTiltAngle(AxisID.SECOND, errorMessageTitle)) {
      return false;
    }
    return DatasetTool.validateViewType(setupInterface.isSingleViewSelected()
      ? ViewType.SINGLE_VIEW : ViewType.MONTAGE, getPropertyUserDir(),
      getStackFileName(setupInterface), manager, null, AxisID.ONLY);
  }

  public boolean isDirectiveDrivenAutomation() {
    return directiveFileCollection != null;
  }

  public DirectiveFileCollection getDirectiveFileCollection() {
    if (directiveFileCollection != null) {
      return directiveFileCollection;
    }
    return expert.getDirectiveFileCollection();
  }

  public MetaData getFields(final boolean doValidation) {
    SetupReconInterface setupInterface = getSetupReconInterface();
    if (setupInterface == null) {
      return null;
    }
    try {
      MetaData metaData = getMetaData();
      AxisType axisType = getAxisType();
      metaData.setBackupDirectory(setupInterface.getBackupDirectory());
      metaData.setDistortionFile(setupInterface.getDistortionFile());
      metaData.setMagGradientFile(setupInterface.getMagGradientFile());
      metaData.setDefaultParallel(setupInterface
        .isParallelProcessSelected(getPropertyUserDir()));
      metaData.setDefaultGpuProcessing(setupInterface
        .isGpuProcessingSelected(getPropertyUserDir()));
      metaData.setAdjustedFocusA(setupInterface.isAdjustedFocusSelected(AxisID.FIRST));
      metaData.setAdjustedFocusB(setupInterface.isAdjustedFocusSelected(AxisID.SECOND));
      metaData.setViewType(getViewType(setupInterface));
      String currentField = "";
      currentField = "Image Rotation";
      metaData.setImageRotation(
        setupInterface.getImageRotation(AxisID.FIRST, doValidation), AxisID.FIRST);
      if (!metaData.getImageRotation(AxisID.FIRST).isValid()) {
        UIHarness.INSTANCE.openMessageDialog(manager, currentField + " must be numeric.",
          "Setup Dialog Error", AxisID.ONLY);
        return null;
      }
      try {
        currentField = "Pixel Size";
        metaData.setPixelSize(setupInterface.getPixelSize(doValidation));
        currentField = "Fiducial Diameter";
        metaData.setFiducialDiameter(setupInterface.getFiducialDiameter(doValidation));
        if (axisType == AxisType.DUAL_AXIS) {
          metaData.setImageRotation(
            setupInterface.getImageRotation(AxisID.SECOND, doValidation), AxisID.SECOND);
        }
        currentField = "Axis A starting and step angles";
        if (!setupInterface.getTiltAngleFields(AxisID.FIRST,
          metaData.getTiltAngleSpecA(), doValidation)) {
          return null;
        }
        currentField = "Axis B starting and step angles";
        if (!setupInterface.getTiltAngleFields(AxisID.SECOND,
          metaData.getTiltAngleSpecB(), doValidation)) {
          return null;
        }
      }
      catch (NumberFormatException e) {
        UIHarness.INSTANCE.openMessageDialog(manager, currentField + " must be numeric.",
          "Setup Dialog Error", AxisID.ONLY);
        return null;
      }
      metaData.setBinning(setupInterface.getBinning());
      metaData.setExcludeProjections(
        setupInterface.getExcludeList(AxisID.FIRST, doValidation), AxisID.FIRST);
      metaData.setExcludeProjections(
        setupInterface.getExcludeList(AxisID.SECOND, doValidation), AxisID.SECOND);
      metaData.setIsTwodir(AxisID.FIRST, setupInterface.isTwodir(AxisID.FIRST));
      metaData.setIsTwodir(AxisID.SECOND, setupInterface.isTwodir(AxisID.SECOND));
      metaData.setTwodir(AxisID.FIRST,
        setupInterface.getTwodir(AxisID.FIRST, doValidation));
      metaData.setTwodir(AxisID.SECOND,
        setupInterface.getTwodir(AxisID.SECOND, doValidation));
      if (axisType == AxisType.DUAL_AXIS) {
        File bStack =
          DatasetFiles.getStack(getPropertyUserDir(), metaData, AxisID.SECOND);
        metaData.setBStackProcessed(bStack.exists());
      }
      metaData.setSetFEIPixelSize(setFEIPixelSize);
      DirectiveFileCollection directiveFileCollection =
        setupInterface.getDirectiveFileCollection();
      DirectiveFile directiveFile =
        directiveFileCollection.getDirectiveFile(DirectiveFileType.SCOPE);
      if (directiveFile != null) {
        metaData.setOrigScopeTemplate(directiveFile.getFile());
        saveDirectiveFile(directiveFile, metaData);
      }
      directiveFile = directiveFileCollection.getDirectiveFile(DirectiveFileType.SYSTEM);
      if (directiveFile != null) {
        metaData.setOrigSystemTemplate(directiveFile.getFile());
        saveDirectiveFile(directiveFile, metaData);
      }
      directiveFile = directiveFileCollection.getDirectiveFile(DirectiveFileType.USER);
      if (directiveFile != null) {
        metaData.setOrigUserTemplate(directiveFile.getFile());
        saveDirectiveFile(directiveFile, metaData);
      }
      if (directiveFileCollection != null) {
        saveDirectiveFile(
          directiveFileCollection.getDirectiveFile(DirectiveFileType.BATCH), metaData);
      }
      String value = directiveFileCollection.getValue(DirectiveDef.PATCH_SIZE);
      if (value != null) {
        metaData.setPatchTypeOrXYZ(value);
      }
      value = directiveFileCollection.getValue(DirectiveDef.EXTRA_TARGETS);
      if (value != null) {
        metaData.setExtraResidualTargets(value);
      }
      value = directiveFileCollection.getValue(DirectiveDef.FINAL_PATCH_SIZE);
      if (value != null) {
        metaData.setAutoPatchFinalSize(value);
      }
      value = directiveFileCollection.getValue(DirectiveDef.WEDGE_REDUCTION);
      if (value != null) {
        metaData.setWedgeReductionFraction(value);
      }
      value = directiveFileCollection.getValue(DirectiveDef.LOW_FROM_BOTH_RADIUS);
      if (value != null) {
        metaData.setLowFromBothRadius(value);
      }
      return metaData;
    }
    catch (FieldValidationFailedException e) {
      return null;
    }
  }

  private void saveDirectiveFile(final DirectiveFile directiveFile,
    final MetaData metaData) {
    if (directiveFile == null) {
      return;
    }
    AxisType axisType = getAxisType();
    if (directiveFile.contains(DirectiveDef.USE_ALIGNED_STACK, AxisID.FIRST)) {
      metaData.setTrackRaptorUseRawStack(directiveFile.isValue(
        DirectiveDef.USE_ALIGNED_STACK, AxisID.FIRST));
    }
    if (directiveFile.contains(DirectiveDef.NUMBER_OF_MARKERS, AxisID.FIRST)) {
      metaData.setTrackRaptorMark(directiveFile.getValue(DirectiveDef.NUMBER_OF_MARKERS,
        AxisID.FIRST));
    }
    saveDirectiveFile(directiveFile, metaData, AxisID.FIRST);
    saveDirectiveFile(directiveFile, metaData, AxisID.SECOND);
  }

  private void saveDirectiveFile(final DirectiveFile directiveFile,
    final MetaData metaData, final AxisID axisID) {
    if (directiveFile.contains(DirectiveDef.FIDUCIALLESS, axisID)) {
      boolean value = directiveFile.isValue(DirectiveDef.FIDUCIALLESS, axisID);
      metaData.setFiducialess(axisID, value);
      metaData.setFiducialessAlignment(axisID, value);
    }
    if (directiveFile.contains(DirectiveDef.SEEDING_METHOD, axisID)) {
      SeedingMethod seedingMethod =
        SeedingMethod.getInstance(directiveFile.getValue(DirectiveDef.SEEDING_METHOD,
          axisID));
      if (seedingMethod == SeedingMethod.MANUAL) {
        metaData.setTrackSeedModelManual(true, axisID);
      }
      // If both is set, assume that autofidseed was done after manual.
      else if (seedingMethod == SeedingMethod.AUTO_FID_SEED
        || seedingMethod == SeedingMethod.BOTH) {
        metaData.setTrackSeedModelAuto(true, axisID);
      }
      else if (seedingMethod == SeedingMethod.TRANSFER_FID) {
        metaData.setTrackSeedModelTransfer(true, axisID);
      }
    }
    if (directiveFile.contains(DirectiveDef.TRACKING_METHOD, axisID)) {
      metaData.setTrackMethod(axisID, TrackingMethod.toMetaDataValue(directiveFile
        .getValue(DirectiveDef.TRACKING_METHOD, axisID)));
    }
    if (directiveFile.contains(DirectiveDef.SIZE_IN_X_AND_Y, axisID)) {
      try {
        metaData.setSizeToOutputInXandY(axisID,
          directiveFile.getValue(DirectiveDef.SIZE_IN_X_AND_Y, axisID));
      }
      catch (FortranInputSyntaxException e) {
        File file = directiveFile.getFile();
        UIHarness.INSTANCE.openMessageDialog(manager, "Invalid directive file"
          + (file != null ? ": " + file.getAbsolutePath() : "")
          + ".  Invalid directive: " + DirectiveDef.SIZE_IN_X_AND_Y.toString() + ".  "
          + e.getMessage(), "Invalid Directive");
      }
    }
    if (directiveFile.contains(DirectiveDef.BIN_BY_FACTOR_FOR_ALIGNED_STACK, axisID)) {
      metaData.setStackBinning(axisID,
        directiveFile.getValue(DirectiveDef.BIN_BY_FACTOR_FOR_ALIGNED_STACK, axisID));
    }
    if (directiveFile.contains(DirectiveDef.AUTO_FIT_RANGE_AND_STEP, axisID)) {
      try {
        metaData.setStackCtfAutoFitRangeAndStep(axisID,
          directiveFile.getValue(DirectiveDef.AUTO_FIT_RANGE_AND_STEP, axisID));
      }
      catch (FortranInputSyntaxException e) {
        UIHarness.INSTANCE.openMessageDialog(manager, "Invalid directive file: "
          + directiveFile.getFile().getAbsolutePath() + ".  Invalid directive: "
          + DirectiveDef.AUTO_FIT_RANGE_AND_STEP.toString() + ".  " + e.getMessage(),
          "Invalid Directive");
      }
    }
    if (directiveFile.contains(DirectiveDef.BINNING_FOR_GOLD_ERASING, axisID)) {
      metaData.setStack3dFindBinning(axisID,
        directiveFile.getValue(DirectiveDef.BINNING_FOR_GOLD_ERASING, axisID));
    }
    // GoldErasingThickness overrides the .com file
    if (directiveFile.contains(DirectiveDef.THICKNESS_FOR_GOLD_ERASING, axisID)) {
      metaData.setStack3dFindThickness(axisID,
        directiveFile.getValue(DirectiveDef.THICKNESS_FOR_GOLD_ERASING, axisID));
    }
    if (directiveFile.contains(DirectiveDef.WHOLE_TOMOGRAM, axisID)) {
      metaData.setWholeTomogramSample(axisID,
        directiveFile.isValue(DirectiveDef.WHOLE_TOMOGRAM, axisID));
    }
    if (directiveFile.contains(DirectiveDef.USE_SIRT, axisID)) {
      metaData.setGenBackProjection(axisID,
        !directiveFile.isValue(DirectiveDef.USE_SIRT, axisID));
    }
    if (directiveFile.contains(DirectiveDef.THICKNESS_FOR_POSITIONING, axisID)) {
      metaData.setSampleThickness(axisID,
        directiveFile.getValue(DirectiveDef.THICKNESS_FOR_POSITIONING, axisID));
    }
    if (directiveFile.contains(DirectiveDef.BIN_BY_FACTOR_FOR_POSITIONING, axisID)) {
      metaData.setPosBinning(axisID,
        directiveFile.getValue(DirectiveDef.BIN_BY_FACTOR_FOR_POSITIONING, axisID));
    }
    if (directiveFile.contains(DirectiveDef.TARGET_MEASUREMENT_RATIO)) {
      metaData.setTargetMeasurementRatio(axisID,
        directiveFile.getValue(DirectiveDef.TARGET_MEASUREMENT_RATIO, axisID));
    }
    if (directiveFile.contains(DirectiveDef.MIN_MEASUREMENT_RATIO)) {
      metaData.setMinMeasurementRatio(axisID,
        directiveFile.getValue(DirectiveDef.MIN_MEASUREMENT_RATIO, axisID));
    }
    if (directiveFile.contains(DirectiveDef.ORDER_OF_RESTRICTIONS)) {
      metaData.setOrderOfRestrictions(axisID,
        directiveFile.getValue(DirectiveDef.ORDER_OF_RESTRICTIONS, axisID));
    }
    if (directiveFile.contains(DirectiveDef.SKIP_BEAM_TILT_WITH_ONE_ROT)) {
      metaData.setSkipBeamTiltWithOneRot(axisID,
        directiveFile.isValue(DirectiveDef.SKIP_BEAM_TILT_WITH_ONE_ROT, axisID));
    }
  }

  public void initializeFields(ConstMetaData metaData, UserConfiguration userConfig) {
    SetupReconInterface setupInterface = getSetupReconInterface();
    if (setupInterface != null) {
      setupInterface.initTiltAngleFields(AxisID.FIRST, metaData.getTiltAngleSpecA(),
        userConfig);
      setupInterface.initTiltAngleFields(AxisID.SECOND, metaData.getTiltAngleSpecB(),
        userConfig);
    }
    if (expert != null) {
      expert.initializeFields(metaData, userConfig);
    }
    setFEIPixelSize = userConfig.isSetFEIPixelSize();
  }
}
