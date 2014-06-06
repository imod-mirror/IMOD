package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.logic.TrackingMethod;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.type.ConstEtomoNumber;
import etomo.ui.FieldType;

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
final class BatchRunTomoDatasetDialog implements ActionListener {
  public static final String rcsid = "$Id:$";

  private static final String AUTO_FIT_RANGE_AND_STEP_LABEL = "Autofit range and step: ";

  private final JPanel pnlRoot = new JPanel();
  private final CheckBox cbRemoveXrays = new CheckBox("Remove X-rays");
  private final CheckBox cbEnableStretching = new CheckBox(
      "Enable distortion (stretching) in alignment");
  private final CheckBox cbLocalAlignments = new CheckBox("Use local alignments");
  private final MultiLineButton btnModelFile = new MultiLineButton("Make in 3dmod");
  private final ButtonGroup bgTrackingMethod = new ButtonGroup();
  private final RadioButton rbTrackingMethodSeed = new RadioButton("Autoseed and track",
      TrackingMethod.SEED, bgTrackingMethod);
  private final RadioButton rbTrackingMethodRaptor = new RadioButton("Raptor and track",
      TrackingMethod.RAPTOR, bgTrackingMethod);
  private final RadioButton rbTrackingMethodPatchTracking = new RadioButton(
      "Patch tracking", TrackingMethod.PATCH_TRACKING, bgTrackingMethod);
  private final RadioButton rbFiducialless = new RadioButton("Fiducialless",
      TrackingMethod.PATCH_TRACKING, bgTrackingMethod);
  private final LabeledTextField ltfGold = new LabeledTextField(FieldType.FLOATING_POINT,
      "Bead size (nm): ");
  private final LabeledTextField ltfLocalAreaTargetSize = new LabeledTextField(
      FieldType.INTEGER_PAIR, "Local tracking area size: ");
  private LabeledTextField ltfTargetNumberOfBeads = new LabeledTextField(
      FieldType.INTEGER, "Target number of beads: ");
  private final LabeledTextField ltfSizeOfPatchesXandY = new LabeledTextField(
      FieldType.INTEGER_PAIR, "Patch tracking size: ");
  private final LabeledSpinner lsContourPieces = LabeledSpinner.getInstance(
      "Break contours into pieces: ", 1, 1, 10, 1);
  private final LabeledSpinner lsBinByFactor = LabeledSpinner.getInstance(
      "Aligned stack binning: ", 1, 1, 8, 1);
  private final CheckBox cbCorrectCTF = new CheckBox("Correct CTF");
  private final LabeledTextField ltfDefocus = new LabeledTextField(
      FieldType.INTEGER_PAIR, "Defocus: ");
  private final ButtonGroup bgAutofit = new ButtonGroup();
  private final RadioButton rbFitEveryImage = new RadioButton("Fit every image",
      bgAutofit);
  private final RadioButton rbAutoFitRangeAndStep = new RadioButton(
      "Autofit range and step: ", bgAutofit);
  private final TextField tfAutoFitRange = new TextField(FieldType.FLOATING_POINT,
      AUTO_FIT_RANGE_AND_STEP_LABEL, null);
  private final TextField tfAutoFitStep = new TextField(FieldType.FLOATING_POINT,
      AUTO_FIT_RANGE_AND_STEP_LABEL, null);
  private final ButtonGroup bgUseSirt = new ButtonGroup();
  private final RadioButton rbUseSirtFalse = new RadioButton("Back-projection", bgUseSirt);
  private final RadioButton rbUseSirtTrue = new RadioButton("SIRT", bgUseSirt);
  private final RadioButton rbDoBackprojAlso = new RadioButton("Both", bgUseSirt);
  private final LabeledTextField ltfLeaveIterations = new LabeledTextField(
      FieldType.STRING, "Leave iterations: ");
  private final CheckBox cbScaleToInteger = new CheckBox("Scale to integers");
  private final ButtonGroup bgThickness = new ButtonGroup();
  private final RadioTextField rtfThicknessPixels = RadioTextField.getInstance(
      FieldType.INTEGER, "Thickness total (unbinned pixels): ", bgThickness);
  private final RadioTextField rtfBinnedThickness = RadioTextField.getInstance(
      FieldType.INTEGER, "Thickness total   (binned pixels): ", bgThickness);
  private final RadioTextField rtfThicknessNm = RadioTextField.getInstance(
      FieldType.INTEGER, "Thickness total (nm): ", bgThickness);
  private final RadioTextField rtfThicknessSpacingPlus = RadioTextField.getInstance(
      FieldType.INTEGER, "Thickness from Intergold spacing plus: ", bgThickness);
  private final LabeledTextField ltfThicknessSpacingFallback = new LabeledTextField(
      FieldType.INTEGER, "with fallback: ");
  private final JLabel lThicknessSpacingFallback = new JLabel(" unbinned pixels");

  private final FileTextField2 ftfDistort;
  private final FileTextField2 ftfGradient;
  private final FileTextField2 ftfModelFile;

  private BatchRunTomoDatasetDialog(final BaseManager manager) {
    ftfDistort = FileTextField2.getAltLayoutInstance(manager, "Image distortion file: ");
    ftfGradient = FileTextField2.getAltLayoutInstance(manager, "Mag gradient file: ");
    ftfModelFile = FileTextField2.getAltLayoutInstance(manager,
        "Manual replacement model: ");
  }

  static BatchRunTomoDatasetDialog getInstace(final BaseManager manager) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  private void createPanel() {
    // init
    JPanel pnlModelFile = new JPanel();
    JPanel pnlTrackingMethod = new JPanel();
    JPanel pnlGold = new JPanel();
    JPanel pnlSizeOfPatchesXandY = new JPanel();
    JPanel pnlBinByFactor = new JPanel();
    JPanel pnlCorrectCTF = new JPanel();
    JPanel pnlReconstruction = new JPanel();
    JPanel pnlReconstructionType = new JPanel();
    JPanel pnlThickness = new JPanel();
    JPanel pnlThicknessSpacingFallback = new JPanel();
    JPanel pnlAutoFitRangeAndStep = new JPanel();
    ftfGradient.setPreferredWidth(272);
    btnModelFile.setToPreferredSize();
    rbUseSirtFalse.setSelected(true);
    rtfThicknessPixels.setSelected(true);
    // Dataset
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(new EtchedBorder("Dataset Parameters").getBorder());
    pnlRoot.add(ftfDistort.getRootPanel());
    pnlRoot.add(ftfGradient.getRootPanel());
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRoot.add(cbRemoveXrays);
    pnlRoot.add(pnlModelFile);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRoot.add(pnlTrackingMethod);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y3));
    pnlRoot.add(pnlGold);
    pnlRoot.add(pnlSizeOfPatchesXandY);
    pnlRoot.add(cbEnableStretching);
    pnlRoot.add(cbLocalAlignments);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlRoot.add(pnlBinByFactor);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRoot.add(pnlCorrectCTF);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRoot.add(pnlReconstruction);
    // ModelFile
    pnlModelFile.setLayout(new BoxLayout(pnlModelFile, BoxLayout.X_AXIS));
    pnlModelFile.add(ftfModelFile.getRootPanel());
    pnlModelFile.add(Box.createRigidArea(FixedDim.x5_y0));
    pnlModelFile.add(btnModelFile.getComponent());
    pnlModelFile.add(Box.createHorizontalGlue());
    // TrackingMethod
    pnlTrackingMethod.setLayout(new GridLayout(2, 2, 0, 0));
    pnlTrackingMethod.setBorder(new EtchedBorder("Alignment Method").getBorder());
    pnlTrackingMethod.add(rbTrackingMethodSeed.getComponent());
    pnlTrackingMethod.add(rbTrackingMethodPatchTracking.getComponent());
    pnlTrackingMethod.add(rbTrackingMethodRaptor.getComponent());
    pnlTrackingMethod.add(rbFiducialless.getComponent());
    // Gold
    pnlGold.setLayout(new GridLayout(2, 2, 15, 0));
    pnlGold.add(ltfGold.getComponent());
    pnlGold.add(ltfTargetNumberOfBeads.getComponent());
    pnlGold.add(ltfLocalAreaTargetSize.getComponent());
    // SizeOfPatchesXandY
    pnlSizeOfPatchesXandY.setLayout(new GridLayout(1, 2, 15, 0));
    pnlSizeOfPatchesXandY.add(ltfSizeOfPatchesXandY.getComponent());
    pnlSizeOfPatchesXandY.add(lsContourPieces.getContainer());
    // BinByFactor
    pnlBinByFactor.setLayout(new BoxLayout(pnlBinByFactor, BoxLayout.X_AXIS));
    pnlBinByFactor.add(lsBinByFactor.getContainer());
    pnlBinByFactor.add(Box.createHorizontalGlue());
    // CorrectCTF
    pnlCorrectCTF.setLayout(new GridLayout(2, 2, 30, 0));
    pnlCorrectCTF.add(cbCorrectCTF);
    pnlCorrectCTF.add(pnlAutoFitRangeAndStep);
    pnlCorrectCTF.add(ltfDefocus.getComponent());
    pnlCorrectCTF.add(rbFitEveryImage.getComponent());
    // AutoFitRangeAndStep
    pnlAutoFitRangeAndStep.setLayout(new BoxLayout(pnlAutoFitRangeAndStep,
        BoxLayout.X_AXIS));
    pnlAutoFitRangeAndStep.add(rbAutoFitRangeAndStep.getComponent());
    pnlAutoFitRangeAndStep.add(tfAutoFitRange.getComponent());
    pnlAutoFitRangeAndStep.add(tfAutoFitStep.getComponent());
    // Reconstruction
    pnlReconstruction.setLayout(new BoxLayout(pnlReconstruction, BoxLayout.X_AXIS));
    pnlReconstruction.setBorder(new EtchedBorder("Reconstruction").getBorder());
    pnlReconstruction.add(pnlReconstructionType);
    pnlReconstruction.add(Box.createRigidArea(FixedDim.x12_y0));
    pnlReconstruction.add(pnlThickness);
    // ReconstructionType
    pnlReconstructionType
        .setLayout(new BoxLayout(pnlReconstructionType, BoxLayout.Y_AXIS));
    pnlReconstructionType.add(rbUseSirtFalse.getComponent());
    pnlReconstructionType.add(rbUseSirtTrue.getComponent());
    pnlReconstructionType.add(rbDoBackprojAlso.getComponent());
    pnlReconstructionType.add(Box.createRigidArea(FixedDim.x0_y6));
    pnlReconstructionType.add(ltfLeaveIterations.getComponent());
    pnlReconstructionType.add(cbScaleToInteger);
    // Thickness
    pnlThickness.setLayout(new BoxLayout(pnlThickness, BoxLayout.Y_AXIS));
    pnlThickness.add(rtfThicknessPixels.getContainer());
    pnlThickness.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlThickness.add(rtfBinnedThickness.getContainer());
    pnlThickness.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlThickness.add(rtfThicknessNm.getContainer());
    pnlThickness.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlThickness.add(rtfThicknessSpacingPlus.getContainer());
    pnlThickness.add(pnlThicknessSpacingFallback);
    // ThicknessSpacingFallback
    pnlThicknessSpacingFallback.setLayout(new BoxLayout(pnlThicknessSpacingFallback,
        BoxLayout.X_AXIS));
    pnlThicknessSpacingFallback.add(Box.createRigidArea(FixedDim.x40_y0));
    pnlThicknessSpacingFallback.add(ltfThicknessSpacingFallback.getComponent());
    pnlThicknessSpacingFallback.add(lThicknessSpacingFallback);
    // align
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlReconstructionType, Component.LEFT_ALIGNMENT);
    // update
    updateDisplay();
  }

  private void addListeners() {
    cbRemoveXrays.addActionListener(this);
    rbTrackingMethodSeed.addActionListener(this);
    rbTrackingMethodRaptor.addActionListener(this);
    rbTrackingMethodPatchTracking.addActionListener(this);
    rbFiducialless.addActionListener(this);
    cbCorrectCTF.addActionListener(this);
    rbUseSirtFalse.addActionListener(this);
    rbUseSirtTrue.addActionListener(this);
    rbDoBackprojAlso.addActionListener(this);
    rtfThicknessPixels.addActionListener(this);
    rtfBinnedThickness.addActionListener(this);
    rtfThicknessNm.addActionListener(this);
    rtfThicknessSpacingPlus.addActionListener(this);
  }

  Component getComponent() {
    return pnlRoot;
  }

  private void updateDisplay() {
    boolean removeXrays = cbRemoveXrays.isSelected();
    ftfModelFile.setEnabled(removeXrays);
    btnModelFile.setEnabled(removeXrays);
    boolean fiducialless = rbFiducialless.isSelected();
    cbEnableStretching.setEnabled(!fiducialless);
    cbLocalAlignments.setEnabled(!fiducialless);
    boolean beadTracking = rbTrackingMethodSeed.isSelected()
        || rbTrackingMethodRaptor.isSelected();
    ltfGold.setEnabled(beadTracking);
    ltfTargetNumberOfBeads.setEnabled(beadTracking);
    ltfLocalAreaTargetSize.setEnabled(beadTracking);
    boolean patchTracking = rbTrackingMethodPatchTracking.isSelected();
    ltfSizeOfPatchesXandY.setEnabled(patchTracking);
    lsContourPieces.setEnabled(patchTracking);
    boolean ctf = cbCorrectCTF.isSelected();
    ltfDefocus.setEnabled(ctf);
    rbAutoFitRangeAndStep.setEnabled(ctf);
    rbFitEveryImage.setEnabled(ctf);
    boolean autoFitRangeAndStep = ctf && rbAutoFitRangeAndStep.isSelected();
    tfAutoFitRange.setEnabled(autoFitRangeAndStep);
    tfAutoFitStep.setEnabled(autoFitRangeAndStep);
    boolean sirt = rbUseSirtTrue.isSelected() || rbDoBackprojAlso.isSelected();
    ltfLeaveIterations.setEnabled(sirt);
    cbScaleToInteger.setEnabled(sirt);
    boolean thicknessSpacing = rtfThicknessSpacingPlus.isSelected();
    ltfThicknessSpacingFallback.setEnabled(thicknessSpacing);
    lThicknessSpacingFallback.setEnabled(thicknessSpacing);
  }

  /**
   * Check isDifferentFromCheckpoint on all data entry fields
   * @return true if any field's isDifferentFromCheckpoint function returned true
   */
  boolean backupIfChanged() {
    boolean changed = false;
    if (ftfDistort.isDifferentFromCheckpoint(true)) {
      ftfDistort.backup();
      changed = true;
    }
    if (ftfGradient.isDifferentFromCheckpoint(true)) {
      ftfGradient.backup();
      changed = true;
    }
    if (cbRemoveXrays.isDifferentFromCheckpoint(true)) {
      cbRemoveXrays.backup();
      changed = true;
    }
    if (ftfModelFile.isDifferentFromCheckpoint(true)) {
      ftfModelFile.backup();
      changed = true;
    }
    if (rbTrackingMethodSeed.isDifferentFromCheckpoint(true)) {
      rbTrackingMethodSeed.backup();
      changed = true;
    }
    if (rbTrackingMethodRaptor.isDifferentFromCheckpoint(true)) {
      rbTrackingMethodRaptor.backup();
      changed = true;
    }
    if (rbTrackingMethodPatchTracking.isDifferentFromCheckpoint(true)) {
      rbTrackingMethodPatchTracking.backup();
      changed = true;
    }
    if (rbFiducialless.isDifferentFromCheckpoint(true)) {
      rbFiducialless.backup();
      changed = true;
    }
    if (ltfGold.isDifferentFromCheckpoint(true)) {
      ltfGold.backup();
      changed = true;
    }
    if (ltfLocalAreaTargetSize.isDifferentFromCheckpoint(true)) {
      ltfLocalAreaTargetSize.backup();
      changed = true;
    }
    if (ltfTargetNumberOfBeads.isDifferentFromCheckpoint(true)) {
      ltfTargetNumberOfBeads.backup();
      changed = true;
    }
    if (ltfSizeOfPatchesXandY.isDifferentFromCheckpoint(true)) {
      ltfSizeOfPatchesXandY.backup();
      changed = true;
    }
    if (lsContourPieces.isDifferentFromCheckpoint(true)) {
      lsContourPieces.backup();
      changed = true;
    }
    if (lsBinByFactor.isDifferentFromCheckpoint(true)) {
      lsBinByFactor.backup();
      changed = true;
    }
    if (cbCorrectCTF.isDifferentFromCheckpoint(true)) {
      cbCorrectCTF.backup();
      changed = true;
    }
    if (ltfDefocus.isDifferentFromCheckpoint(true)) {
      ltfDefocus.backup();
      changed = true;
    }
    if (rbFitEveryImage.isDifferentFromCheckpoint(true)) {
      rbFitEveryImage.backup();
      changed = true;
    }
    if (rbAutoFitRangeAndStep.isDifferentFromCheckpoint(true)) {
      rbAutoFitRangeAndStep.backup();
      changed = true;
    }
    if (tfAutoFitRange.isDifferentFromCheckpoint(true)) {
      tfAutoFitRange.backup();
      changed = true;
    }
    if (tfAutoFitStep.isDifferentFromCheckpoint(true)) {
      tfAutoFitStep.backup();
      changed = true;
    }
    if (rbUseSirtFalse.isDifferentFromCheckpoint(true)) {
      rbUseSirtFalse.backup();
      changed = true;
    }
    if (rbUseSirtTrue.isDifferentFromCheckpoint(true)) {
      rbUseSirtTrue.backup();
      changed = true;
    }
    if (rbDoBackprojAlso.isDifferentFromCheckpoint(true)) {
      rbDoBackprojAlso.backup();
      changed = true;
    }
    if (ltfLeaveIterations.isDifferentFromCheckpoint(true)) {
      ltfLeaveIterations.backup();
      changed = true;
    }
    if (cbScaleToInteger.isDifferentFromCheckpoint(true)) {
      cbScaleToInteger.backup();
      changed = true;
    }
    if (rtfThicknessPixels.isDifferentFromCheckpoint(true)) {
      rtfThicknessPixels.backup();
      changed = true;
    }
    if (rtfBinnedThickness.isDifferentFromCheckpoint(true)) {
      rtfBinnedThickness.backup();
      changed = true;
    }
    if (rtfThicknessNm.isDifferentFromCheckpoint(true)) {
      rtfThicknessNm.backup();
      changed = true;
    }
    if (rtfThicknessSpacingPlus.isDifferentFromCheckpoint(true)) {
      rtfThicknessSpacingPlus.backup();
      changed = true;
    }
    if (ltfThicknessSpacingFallback.isDifferentFromCheckpoint(true)) {
      ltfThicknessSpacingFallback.backup();
      changed = true;
    }
    return changed;
  }

  /**
   * Set values from the directive file collection.  Only set fields from directives that
   * are in a least one of the directive files.
   * @param directiveFileCollection
   */
  void setValues(final DirectiveFileCollection directiveFileCollection) {
    if (directiveFileCollection.contains(DirectiveDef.DISTORT)) {
      ftfDistort.setText(directiveFileCollection.getDistortionFile());
    }
    if (directiveFileCollection.contains(DirectiveDef.GRADIENT)) {
      ftfGradient.setText(directiveFileCollection.getMagGradientFile());
    }
    if (directiveFileCollection.contains(DirectiveDef.REMOVE_XRAYS)) {
      cbRemoveXrays.setSelected(directiveFileCollection
          .isValue(DirectiveDef.REMOVE_XRAYS));
    }
    if (directiveFileCollection.contains(DirectiveDef.MODEL_FILE)) {
      ftfModelFile.setText(directiveFileCollection.getValue(DirectiveDef.MODEL_FILE));
    }
    if (directiveFileCollection.contains(DirectiveDef.TRACKING_METHOD)) {
      TrackingMethod trackingMethod = TrackingMethod.getInstance(directiveFileCollection
          .getValue(DirectiveDef.TRACKING_METHOD));
      if (trackingMethod == TrackingMethod.SEED) {
        rbTrackingMethodSeed.setSelected(true);
      }
      else if (trackingMethod == TrackingMethod.RAPTOR) {
        rbTrackingMethodRaptor.setSelected(true);
      }
      else if (trackingMethod == TrackingMethod.PATCH_TRACKING) {
        rbTrackingMethodPatchTracking.setSelected(true);
      }
    }
    if (directiveFileCollection.contains(DirectiveDef.FIDUCIALLESS)) {
      rbFiducialless.setSelected(directiveFileCollection
          .isValue(DirectiveDef.FIDUCIALLESS));
    }
    if (directiveFileCollection.contains(DirectiveDef.GOLD)) {
      ltfGold.setText(directiveFileCollection.getFiducialDiameter(false));
    }
    if (directiveFileCollection.contains(DirectiveDef.LOCAL_AREA_TARGET_SIZE)) {
      ltfLocalAreaTargetSize.setText(directiveFileCollection
          .getValue(DirectiveDef.LOCAL_AREA_TARGET_SIZE));
    }
    if (directiveFileCollection.containsTargetNumberOfBeads()) {
      ltfTargetNumberOfBeads.setText(directiveFileCollection.getTargetNumberOfBeads());
    }
    if (directiveFileCollection.containsSizeOfPatchesXandY()) {
      ltfSizeOfPatchesXandY.setText(directiveFileCollection.getSizeOfPatchesXandY());
    }
    if (directiveFileCollection.containsContourPieces()) {
      lsContourPieces.setText(directiveFileCollection.getContourPieces());
    }
    if (directiveFileCollection.containsBinByFactor()) {
      lsBinByFactor.setText(directiveFileCollection.getBinByFactor());
    }
    if (directiveFileCollection.containscbCorrectCTF()) {
      cbCorrectCTF.setSelected(directiveFileCollection.iscbCorrectCTF());
    }
    if (directiveFileCollection.containsDefocus()) {
      ltfDefocus.setText(directiveFileCollection.getDefocus());
    }
    if (directiveFileCollection.containsAutoFitRangeAndStep) {
      ConstEtomoNumber number = directiveFileCollection.getAutoFitStep();
      if (directiveFileCollection.getAutoFitStep().equals(0)) {
        rbFitEveryImage.setSelected(true);
      }
      else {
        rbAutoFitRangeAndStep.setSelected(true);
        tfAutoFitRange.setText(directiveFileCollection.getAutoFitRange());
        tfAutoFitStep.setText(number);
      }
    }
    boolean useSirt = false;
    if (directiveFileCollection.containsUseSirt()) {
      useSirt = directiveFileCollection.isUseSirt();
    }
    boolean doBackprojAlso = false;
    if (directiveFileCollection.containsDoBackprojAlso()) {
      doBackprojAlso = directiveFileCollection.isDoBackprojAlso();
    }
    if (!useSirt) {
      rbUseSirtFalse.setSelected(true);
    }
    else if (!doBackprojAlso) {
      rbUseSirtTrue.setSelected(true);
    }
    else {
      rbDoBackprojAlso.setSelected(true);
    }
    if (directiveFileCollection.containsLeaveIterations) {
      ltfLeaveIterations.setText(directiveFileCollection.getLeaveIterations());
    }
    if (directiveFileCollection.containsScaleToInteger()) {
      cbScaleToInteger.setSelected(directiveFileCollection.isScaleToInteger());
    }
    if (directiveFileCollection.containsTiltThickness()) {
      // ??
    }
    if (directiveFileCollection.containsBinnedThickness()) {
      String string = directiveFileCollection.getBinnedThickness();
      rtfBinnedThickness.setSelected(string != null && !string.equals(""));
      rtfBinnedThickness.setText(string);
    }
  }

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (actionCommand.equals(cbRemoveXrays.getActionCommand())
        || actionCommand.equals(rbTrackingMethodSeed.getActionCommand())
        || actionCommand.equals(rbTrackingMethodRaptor.getActionCommand())
        || actionCommand.equals(rbTrackingMethodPatchTracking.getActionCommand())
        || actionCommand.equals(rbFiducialless.getActionCommand())
        || actionCommand.equals(cbCorrectCTF.getActionCommand())
        || actionCommand.equals(rtfThicknessPixels.getActionCommand())
        || actionCommand.equals(rtfBinnedThickness.getActionCommand())
        || actionCommand.equals(rtfThicknessNm.getActionCommand())
        || actionCommand.equals(rtfThicknessSpacingPlus.getActionCommand())
        || actionCommand.equals(rbUseSirtFalse.getActionCommand())
        || actionCommand.equals(rbUseSirtTrue.getActionCommand())
        || actionCommand.equals(rbDoBackprojAlso.getActionCommand())) {
      updateDisplay();
    }
  }
}
