package etomo.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import etomo.ApplicationManager;
import etomo.BaseManager;
import etomo.ToolsManager;
import etomo.comscript.ConstWarpVolParam;
import etomo.comscript.FlattenWarpParam;
import etomo.comscript.WarpVolParam;
import etomo.storage.LogFile;
import etomo.storage.TomogramFileFilter;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.ConstMetaData;
import etomo.type.DialogType;
import etomo.type.EtomoAutodoc;
import etomo.type.EtomoBoolean2;
import etomo.type.FileType;
import etomo.type.ImageFileType;
import etomo.type.MetaData;
import etomo.type.PanelId;
import etomo.type.Run3dmodMenuOptions;
import etomo.util.FrontEndLogic;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2009</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 1.1  2010/02/17 05:01:48  sueh
 * <p> bug# 1301 Incorporated flatten warp panel into FlattenVolumePanel.  Give this panel two panel ids so there can be two instances of it.
 * <p>
 * <p> Revision 1.7  2010/01/12 22:09:01  sueh
 * <p> bug# 1206 Added SmoothingAssessmentPanel.done.
 * <p>
 * <p> Revision 1.6  2009/12/19 01:15:15  sueh
 * <p> bug# 1294 Change getFileTypeForSurfaceModel to getInputFileType.
 * <p>
 * <p> Revision 1.5  2009/11/20 17:12:02  sueh
 * <p> bug# 1282 Naming all the file choosers by constructing a FileChooser
 * <p> instance instead of a JFileChooser instance.  Added isMenuSaveEnabled to
 * <p> allow a save function to have the same limits as the save menu option.
 * <p>
 * <p> Revision 1.4  2009/10/01 18:50:38  sueh
 * <p> bug# 1239 Added getFlattenWarpDisplay.
 * <p>
 * <p> Revision 1.3  2009/09/01 03:18:25  sueh
 * <p> bug# 1222
 * <p>
 * <p> Revision 1.2  2009/06/11 16:51:57  sueh
 * <p> bug# 1221 Sending the process panel to the process function in the
 * <p> manager wrapped in a ProcessDisplay interface.  Implemented
 * <p> WarpVolDisplay.
 * <p>
 * <p> Revision 1.1  2009/06/05 02:11:23  sueh
 * <p> bug# 1219 Panel that can run warpvol using a file called flatten.com.
 * <p> </p>
 */
final class FlattenVolumePanel implements Run3dmodButtonContainer,
    WarpVolDisplay, FlattenWarpDisplay, SmoothingAssessmentParent {
  public static final String rcsid = "$Id$";

  private static final String OUTPUT_SIZE_Z_LABEL = "Output thickness in Z";
  private static final String FLATTEN_LABEL = "Flatten";
  static final String WARP_SPACING_X_LABEL = "Spacing in X";
  static final String WARP_SPACING_Y_LABEL = "and Y";
  private static final String LAMBDA_FOR_SMOOTHING_LABEL = "Smoothing factor";
  private static final String FLATTEN_WARP_LABEL = "Run Flattenwarp";

  private final SpacedPanel pnlRoot = SpacedPanel.getInstance();
  private final BinnedXY3dmodButton btnMakeSurfaceModel = new BinnedXY3dmodButton(
      "Make Surface Model", this);
  private final CheckBox cbOneSurface = new CheckBox(
      "Contours are all on one surface");
  private final LabeledTextField ltfWarpSpacingX = new LabeledTextField(
      WARP_SPACING_X_LABEL + ": ");
  private final LabeledTextField ltfWarpSpacingY = new LabeledTextField(" "
      + WARP_SPACING_Y_LABEL + ": ");
  private final LabeledTextField ltfLambdaForSmoothing = new LabeledTextField(
      LAMBDA_FOR_SMOOTHING_LABEL + ": ");
  ActionListener actionListener = new FlattenVolumeActionListener(this);
  private final ButtonGroup bgInputFile = new ButtonGroup();
  private final RadioButton rbInputFileTrimVol = new RadioButton(
      "Flatten the trimvol output", bgInputFile);
  private final RadioButton rbInputFileSqueezeVol = new RadioButton(
      "Flatten the squeezevol output", bgInputFile);
  private final CheckBox cbInterpolationOrderLinear = new CheckBox(
      "Linear interpolation");
  private final LabeledTextField ltfOutputSizeZ = new LabeledTextField(
      OUTPUT_SIZE_Z_LABEL + ": ");
  private final Run3dmodButton btnImodFlatten = Run3dmodButton
      .get3dmodInstance("Open Flattened Tomogram", this);
  private final FileTextField ftfTemporaryDirectory = new FileTextField(
      "Temporary directory:");
  private final FileTextField ftfInputFile = new FileTextField("Input file:");

  private final PanelId panelId;
  private final Run3dmodButton btnFlatten;
  private final AxisID axisID;
  private final BaseManager manager;
  private final ApplicationManager applicationManager;
  private final ToolsManager toolsManager;
  private final DialogType dialogType;
  private final SmoothingAssessmentPanel smoothingAssessmentPanel;
  private final MultiLineButton btnFlattenWarp;

  private FlattenVolumePanel(final ApplicationManager manager,
      final AxisID axisID, final DialogType dialogType) {
    this.manager = manager;
    applicationManager = manager;
    toolsManager = null;
    this.axisID = axisID;
    this.dialogType = dialogType;
    this.panelId = PanelId.POST_FLATTEN_VOLUME;
    btnFlatten = (Run3dmodButton) manager
        .getProcessResultDisplayFactory(axisID).getFlatten();
    btnFlattenWarp = (MultiLineButton) manager.getProcessResultDisplayFactory(
        axisID).getFlattenWarp();
    smoothingAssessmentPanel = SmoothingAssessmentPanel.getPostInstance(
        manager, axisID, dialogType, panelId, this);
  }

  private FlattenVolumePanel(final ToolsManager manager, final AxisID axisID,
      final DialogType dialogType) {
    this.manager = manager;
    applicationManager = null;
    toolsManager = manager;
    this.axisID = axisID;
    this.dialogType = dialogType;
    this.panelId = PanelId.TOOLS_FLATTEN_VOLUME;
    btnFlatten = Run3dmodButton.getDeferred3dmodInstance(FLATTEN_LABEL, this);
    btnFlattenWarp = new MultiLineButton(FLATTEN_WARP_LABEL);
    smoothingAssessmentPanel = SmoothingAssessmentPanel.getToolsInstance(
        manager, axisID, dialogType, panelId, this);
  }

  static FlattenVolumePanel getPostInstance(final ApplicationManager manager,
      final AxisID axisID, final DialogType dialogType) {
    FlattenVolumePanel instance = new FlattenVolumePanel(manager, axisID,
        dialogType);
    instance.createPanel();
    instance.setToolTipText();
    instance.addListeners();
    return instance;
  }

  static FlattenVolumePanel getToolsInstance(final ToolsManager manager,
      final AxisID axisID, final DialogType dialogType) {
    FlattenVolumePanel instance = new FlattenVolumePanel(manager, axisID,
        dialogType);
    instance.createPanel();
    instance.setToolTipText();
    instance.addListeners();
    return instance;
  }

  static MultiLineButton getFlattenWarpButton() {
    return new MultiLineButton(FLATTEN_WARP_LABEL);
  }

  static Run3dmodButton getFlattenDisplay(final DialogType dialogType) {
    return Run3dmodButton.getDeferredToggle3dmodInstance(FLATTEN_LABEL,
        dialogType);
  }

  private void addListeners() {
    btnMakeSurfaceModel.addActionListener(actionListener);
    btnFlattenWarp.addActionListener(actionListener);
    btnFlatten.addActionListener(actionListener);
    btnImodFlatten.addActionListener(actionListener);
    ftfInputFile.addActionListener(actionListener);
  }

  void done() {
    btnFlatten.removeActionListener(actionListener);
    smoothingAssessmentPanel.done();
  }

  private void createPanel() {
    JPanel pnlInputFile = new JPanel();
    JPanel pnlInterpolationOrder = new JPanel();
    SpacedPanel pnlFlatten = SpacedPanel.getInstance();
    SpacedPanel pnlFlattenWarp = SpacedPanel.getInstance();
    JPanel pnlOneSurface = new JPanel();
    JPanel pnlWarpSpacing = new JPanel();
    JPanel pnlFlattenWarpButtons = new JPanel();
    //initialize
    rbInputFileTrimVol.setSelected(true);
    btnFlattenWarp.setSize();
    btnFlatten.setContainer(this);
    btnFlatten.setDeferred3dmodButton(btnImodFlatten);
    btnFlatten.setSize();
    btnImodFlatten.setSize();
    ftfInputFile.setFieldEditable(false);
    ftfTemporaryDirectory.addAction(manager.getPropertyUserDir(),
        getComponent(), JFileChooser.DIRECTORIES_ONLY);
    //Root panel
    pnlRoot.setBoxLayout(BoxLayout.Y_AXIS);
    pnlRoot.setBorder(new BeveledBorder("Flatten Volume").getBorder());
    pnlRoot.setAlignmentX(Box.CENTER_ALIGNMENT);
    if (panelId == PanelId.POST_FLATTEN_VOLUME) {
      pnlRoot.add(pnlInputFile);
    }
    else if (panelId == PanelId.TOOLS_FLATTEN_VOLUME) {
      pnlRoot.add(ftfInputFile);
    }
    pnlRoot.add(pnlFlattenWarp);
    pnlRoot.add(pnlInterpolationOrder);
    pnlRoot.add(ltfOutputSizeZ.getContainer());
    pnlRoot.add(ftfTemporaryDirectory);
    pnlRoot.add(pnlFlatten);
    //Input file panel
    if (panelId == PanelId.POST_FLATTEN_VOLUME) {
      pnlInputFile.setLayout(new BoxLayout(pnlInputFile, BoxLayout.Y_AXIS));
      pnlInputFile.setBorder(new BeveledBorder("Set Input File").getBorder());
      pnlInputFile.setAlignmentX(Box.CENTER_ALIGNMENT);
      pnlInputFile.add(rbInputFileTrimVol.getComponent());
      pnlInputFile.add(rbInputFileSqueezeVol.getComponent());
    }
    //Flatten warp panel
    pnlFlattenWarp.setBoxLayout(BoxLayout.Y_AXIS);
    pnlFlattenWarp.setAlignmentX(Box.CENTER_ALIGNMENT);
    pnlFlattenWarp.add(btnMakeSurfaceModel.getContainer());
    pnlFlattenWarp.add(pnlOneSurface);
    pnlFlattenWarp.add(pnlWarpSpacing);
    pnlFlattenWarp.add(smoothingAssessmentPanel.getComponent());
    pnlFlattenWarp.add(ltfLambdaForSmoothing.getContainer());
    pnlFlattenWarp.add(pnlFlattenWarpButtons);
    //One surface panel
    pnlOneSurface.setLayout(new BoxLayout(pnlOneSurface, BoxLayout.Y_AXIS));
    pnlOneSurface.setAlignmentX(Box.CENTER_ALIGNMENT);
    pnlOneSurface.add(cbOneSurface);
    //Warp Spacing panel
    pnlWarpSpacing.setLayout(new BoxLayout(pnlWarpSpacing, BoxLayout.X_AXIS));
    pnlWarpSpacing.setAlignmentX(Box.CENTER_ALIGNMENT);
    pnlWarpSpacing.add(ltfWarpSpacingX.getContainer());
    pnlWarpSpacing.add(ltfWarpSpacingY.getContainer());
    //Flatten warp buttons panel
    pnlFlattenWarpButtons.setLayout(new BoxLayout(pnlFlattenWarpButtons,
        BoxLayout.Y_AXIS));
    pnlFlattenWarpButtons.setAlignmentX(Box.CENTER_ALIGNMENT);
    pnlFlattenWarpButtons.add(btnFlattenWarp.getComponent());
    //Interpolation order panel
    pnlInterpolationOrder.setLayout(new BoxLayout(pnlInterpolationOrder,
        BoxLayout.X_AXIS));
    pnlInterpolationOrder.setAlignmentX(Box.CENTER_ALIGNMENT);
    pnlInterpolationOrder.add(cbInterpolationOrderLinear);
    //Flatten panel
    pnlFlatten.setBoxLayout(BoxLayout.X_AXIS);
    pnlFlatten.setAlignmentX(Box.CENTER_ALIGNMENT);
    pnlFlatten.add(btnFlatten.getComponent());
    pnlFlatten.add(btnImodFlatten.getComponent());
  }

  public Component getComponent() {
    return pnlRoot.getContainer();
  }

  FlattenWarpDisplay getFlattenWarpDisplay() {
    return this;
  }

  /**
   * Sets values from the reconstruction metadata.  Not used by the tools
   * manager.
   * @param metaData
   */
  void setParameters(final ConstMetaData metaData) {
    rbInputFileTrimVol.setSelected(metaData.isPostFlattenWarpInputTrimVol());
    if (!rbInputFileTrimVol.isSelected()) {
      rbInputFileSqueezeVol.setSelected(true);
    }
    cbOneSurface.setSelected(metaData.isPostFlattenWarpContoursOnOneSurface());
    ltfWarpSpacingX.setText(metaData.getPostFlattenWarpSpacingInX());
    ltfWarpSpacingY.setText(metaData.getPostFlattenWarpSpacingInY());
    ltfLambdaForSmoothing.setText(metaData.getLambdaForSmoothing());
    smoothingAssessmentPanel.setParameters(metaData);
  }

  /**
   * Puts values into the reconstruction metadata.  Not used by the tools
   * manager.
   * @param metaData
   */
  void getParameters(final MetaData metaData) {
    metaData.setPostFlattenWarpInputTrimVol(rbInputFileTrimVol.isSelected());
    metaData.setPostFlattenWarpContoursOnOneSurface(cbOneSurface.isSelected());
    metaData.setPostFlattenWarpSpacingInX(ltfWarpSpacingX.getText());
    metaData.setPostFlattenWarpSpacingInY(ltfWarpSpacingY.getText());
    metaData.setLambdaForSmoothing(ltfLambdaForSmoothing.getText());
    smoothingAssessmentPanel.getParameters(metaData);
  }

  public boolean getParameters(final FlattenWarpParam param) {
    String errorMessage = param.setLambdaForSmoothing(ltfLambdaForSmoothing
        .getText());
    if (errorMessage != null) {
      UIHarness.INSTANCE.openMessageDialog(manager, "Error in "
          + LAMBDA_FOR_SMOOTHING_LABEL + ":  " + errorMessage, "Entry Error",
          axisID);
      return false;
    }
    param.setOneSurface(cbOneSurface.isSelected());
    errorMessage = param.setWarpSpacingX(ltfWarpSpacingX.getText());
    if (errorMessage != null) {
      UIHarness.INSTANCE.openMessageDialog(manager, "Error in "
          + WARP_SPACING_X_LABEL + ":  " + errorMessage, "Entry Error", axisID);
      return false;
    }
    errorMessage = param.setWarpSpacingY(ltfWarpSpacingY.getText());
    if (errorMessage != null) {
      UIHarness.INSTANCE.openMessageDialog(manager, "Error in "
          + WARP_SPACING_Y_LABEL + ":  " + errorMessage, "Entry Error", axisID);
      return false;
    }
    return true;
  }

  public boolean getParameters(final WarpVolParam param) {
    param.setInterpolationOrderLinear(cbInterpolationOrderLinear.isSelected());
    String errorMessage = param.setOutputSizeZ(ltfOutputSizeZ.getText());
    if (errorMessage != null) {
      UIHarness.INSTANCE.openMessageDialog(manager, "Error in "
          + OUTPUT_SIZE_Z_LABEL + ":  " + errorMessage, "Entry Error", axisID);
      return false;
    }
    //The model contains coordinates so it can match either input file.
    if (panelId == PanelId.POST_FLATTEN_VOLUME) {
      param.setInputFile(getInputFileType());
      param.setOutputFile(ImageFileType.FLATTEN_OUTPUT.getFileName(manager));
    }
    else if (panelId == PanelId.TOOLS_FLATTEN_VOLUME) {
      param.setInputFile(ftfInputFile.getFile());
      param.setOutputFile(FileType.FLATTEN_TOOL_OUTPUT.getFileName(manager));
    }
    param.setTemporaryDirectory(ftfTemporaryDirectory.getText());
    return true;
  }

  /**
   * Sets values from the param if this panel in post processing.
   * @param param
   */
  void setParameters(final ConstWarpVolParam param) {
    cbInterpolationOrderLinear.setSelected(param.isInterpolationOrderLinear());
    ltfOutputSizeZ.setText(param.getOutputSizeZ());
    ftfTemporaryDirectory.setText(param.getTemporaryDirectory());
  }

  public ImageFileType getInputFileType() {
    if (panelId == PanelId.POST_FLATTEN_VOLUME) {
      if (rbInputFileTrimVol.isSelected()) {
        return ImageFileType.TRIM_VOL_OUTPUT;
      }
      return ImageFileType.SQUEEZE_VOL_OUTPUT;
    }
    return null;
  }

  public boolean isOneSurface() {
    return cbOneSurface.isSelected();
  }

  public String getWarpSpacingX() {
    return ltfWarpSpacingX.getText();
  }

  public String getWarpSpacingY() {
    return ltfWarpSpacingY.getText();
  }

  public void action(final Run3dmodButton button,
      final Run3dmodMenuOptions run3dmodMenuOptions) {
    action(button.getActionCommand(), button.getDeferred3dmodButton(),
        run3dmodMenuOptions);
  }

  private void action(final String command,
      final Deferred3dmodButton deferred3dmodButton,
      final Run3dmodMenuOptions run3dmodMenuOptions) {
    //Reconstruction
    if (panelId == PanelId.POST_FLATTEN_VOLUME) {
      if (command.equals(btnFlatten.getActionCommand())) {
        applicationManager.flatten(btnFlatten, null, deferred3dmodButton,
            run3dmodMenuOptions, dialogType, axisID, this);
      }
      else if (command.equals(btnImodFlatten.getActionCommand())) {
        applicationManager.imodFlatten(run3dmodMenuOptions, axisID);
      }
      else if (command.equals(btnMakeSurfaceModel.getActionCommand())) {
        checkRotated(getInputFileType().getFile(manager));
        applicationManager.imodMakeSurfaceModel(run3dmodMenuOptions, axisID,
            btnMakeSurfaceModel.getBinningInXandY(), getInputFileType());
      }
      else if (command.equals(btnFlattenWarp.getActionCommand())) {
        applicationManager.flattenWarp(btnFlattenWarp, null,
            deferred3dmodButton, run3dmodMenuOptions, dialogType, axisID, this);
      }
      else {
        throw new IllegalStateException("Unknown command " + command);
      }
    }
    //Tools
    else if (panelId == PanelId.TOOLS_FLATTEN_VOLUME) {
      if (command.equals(ftfInputFile.getActionCommand())) {
        inputFileAction();
      }
      else if (command.equals(btnFlatten.getActionCommand())) {
        toolsManager.flatten(btnFlatten, null, deferred3dmodButton,
            run3dmodMenuOptions, dialogType, axisID, this);
      }
      else if (command.equals(btnImodFlatten.getActionCommand())) {
        toolsManager.imodFlatten(run3dmodMenuOptions, axisID);
      }
      else if (command.equals(btnMakeSurfaceModel.getActionCommand())) {
        toolsManager.imodMakeSurfaceModel(run3dmodMenuOptions, axisID,
            btnMakeSurfaceModel.getBinningInXandY(), ftfInputFile.getFile());
      }
      else if (command.equals(btnFlattenWarp.getActionCommand())) {
        toolsManager.flattenWarp(btnFlattenWarp, null, deferred3dmodButton,
            run3dmodMenuOptions, dialogType, axisID, this);
      }
      else {
        throw new IllegalStateException("Unknown command " + command);
      }
    }
    else {
      throw new IllegalStateException("Unknown panel ID " + panelId);
    }
  }

  /**
   * Set the input file.  In tools version this checks for conflicting dataset
   * names.  Also pops up a warning if the file was not rotated.
   */
  private void inputFileAction() {
    //  Open up the file chooser in the current working directory
    JFileChooser chooser = new FileChooser(new File(manager
        .getPropertyUserDir()));
    chooser.setPreferredSize(UIParameters.INSTANCE.getFileChooserDimension());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileFilter(new TomogramFileFilter());
    int returnVal = chooser.showOpenDialog(pnlRoot.getContainer());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (file == null || file.isDirectory() || !file.exists()) {
        return;
      }
      if (toolsManager != null
          && toolsManager.isConflictingDatasetName(axisID, file)) {
        return;
      }
      try {
        ftfInputFile.setText(file.getAbsolutePath());
        ftfInputFile.setButtonEnabled(false);
        toolsManager.setName(file);
        UIHarness.INSTANCE.pack(manager);
        checkRotated(file);
      }
      catch (Exception excep) {
        excep.printStackTrace();
      }
    }
  }

  private void checkRotated(File file) {
    EtomoBoolean2 rotated = FrontEndLogic.isRotated(manager, axisID, file);
    if (rotated == null) {
      UIHarness.INSTANCE.openMessageDialog(manager,
          "The MRC header of this file, " + file.getAbsolutePath()
              + ", is unreadable.", "Warning", axisID);
    }
    else if (!rotated.is()) {
      UIHarness.INSTANCE.openMessageDialog(manager, "This tomogram, "
          + file.getAbsolutePath()
          + ", looks like the volume hasn't been reoriented.   "
          + "Flattening won't work on a volume that hasn't been reoriented.",
          "Warning", axisID);
    }
  }

  private void setToolTipText() {
    ReadOnlyAutodoc flattenWarpAutodoc = null;
    ReadOnlyAutodoc warpVolAutodoc = null;
    try {
      flattenWarpAutodoc = AutodocFactory.getInstance(manager,
          AutodocFactory.FLATTEN_WARP, axisID);
      warpVolAutodoc = AutodocFactory.getInstance(manager,
          AutodocFactory.WARP_VOL, axisID);
    }
    catch (FileNotFoundException except) {
      except.printStackTrace();
    }
    catch (IOException except) {
      except.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    if (flattenWarpAutodoc != null) {
      ltfLambdaForSmoothing.setToolTipText(EtomoAutodoc.getTooltip(
          flattenWarpAutodoc, FlattenWarpParam.LAMBDA_FOR_SMOOTHING_OPTION));
      cbOneSurface.setToolTipText(EtomoAutodoc.getTooltip(flattenWarpAutodoc,
          FlattenWarpParam.ONE_SURFACE_OPTION));
      ltfWarpSpacingX.setToolTipText(EtomoAutodoc.getTooltip(
          flattenWarpAutodoc, FlattenWarpParam.WARP_SPACING_X_AND_Y_OPTION));
      ltfWarpSpacingY.setToolTipText(EtomoAutodoc.getTooltip(
          flattenWarpAutodoc, FlattenWarpParam.WARP_SPACING_X_AND_Y_OPTION));
    }
    if (warpVolAutodoc != null) {
      rbInputFileTrimVol.setToolTipText(EtomoAutodoc.getTooltip(warpVolAutodoc,
          WarpVolParam.INPUT_FILE_OPTION));
      rbInputFileSqueezeVol.setToolTipText(EtomoAutodoc.getTooltip(
          warpVolAutodoc, WarpVolParam.INPUT_FILE_OPTION));
      cbInterpolationOrderLinear.setToolTipText(EtomoAutodoc.getTooltip(
          warpVolAutodoc, WarpVolParam.INTERPOLATION_ORDER_OPTION));
      ltfOutputSizeZ.setToolTipText(EtomoAutodoc.getTooltip(warpVolAutodoc,
          WarpVolParam.OUTPUT_SIZE_X_Y_Z_OPTION));
      ftfTemporaryDirectory.setToolTipText(EtomoAutodoc.getTooltip(
          warpVolAutodoc, WarpVolParam.TEMPORARY_DIRECTORY_OPTION));
    }
    btnMakeSurfaceModel
        .setButtonToolTipText("Add contours to describe the location of the "
            + "sectioned material.");
    btnFlattenWarp.setToolTipText("Run flattenwarp.");
    btnFlatten.setToolTipText("Run warpvol.");
    btnImodFlatten.setToolTipText("Open warpvol output in 3dmod.");
  }

  private final class FlattenVolumeActionListener implements ActionListener {
    private final FlattenVolumePanel adaptee;

    private FlattenVolumeActionListener(final FlattenVolumePanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(final ActionEvent event) {
      adaptee.action(event.getActionCommand(), null, null);
    }
  }
}
