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
import etomo.type.EtomoNumber;
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

  private static final String DERIVE_THICKNESS_LABEL = "Thickness from Intergold spacing plus: ";

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
  private final RadioTextField rtfAutoFitRangeAndStep = RadioTextField.getInstance(
      FieldType.FLOATING_POINT, "Autofit range ", bgAutofit);
  private final LabeledTextField ltfAutoFitStep = new LabeledTextField(
      FieldType.FLOATING_POINT, " and step ");
  private final ButtonGroup bgUseSirt = new ButtonGroup();
  private final RadioButton rbUseSirtFalse = new RadioButton("Back-projection", bgUseSirt);
  private final RadioButton rbUseSirtTrue = new RadioButton("SIRT", bgUseSirt);
  private final RadioButton rbDoBackprojAlso = new RadioButton("Both", bgUseSirt);
  private final LabeledTextField ltfLeaveIterations = new LabeledTextField(
      FieldType.STRING, "Leave iterations: ");
  private final CheckBox cbScaleToInteger = new CheckBox("Scale to integers");
  private final ButtonGroup bgThickness = new ButtonGroup();
  private final RadioTextField rtfThickness = RadioTextField.getInstance(
      FieldType.INTEGER, "Thickness total (unbinned pixels): ", bgThickness);
  private final RadioTextField rtfBinnedThickness = RadioTextField.getInstance(
      FieldType.INTEGER, "Thickness total   (binned pixels): ", bgThickness);
  private final RadioButton rbDeriveThickness = new RadioButton(DERIVE_THICKNESS_LABEL,
      bgThickness);
  private final TextField tfExtraThickness = new TextField(FieldType.INTEGER,
      DERIVE_THICKNESS_LABEL, null);
  private final LabeledTextField ltfFallbackThickness = new LabeledTextField(
      FieldType.INTEGER, "with fallback: ");
  private final JLabel lFallbackThickness = new JLabel(" unbinned pixels");

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
    // local panels
    JPanel pnlModelFile = new JPanel();
    JPanel pnlTrackingMethod = new JPanel();
    JPanel pnlGold = new JPanel();
    JPanel pnlSizeOfPatchesXandY = new JPanel();
    JPanel pnlBinByFactor = new JPanel();
    JPanel pnlCorrectCTF = new JPanel();
    JPanel pnlReconstruction = new JPanel();
    JPanel pnlReconstructionType = new JPanel();
    JPanel pnlThickness = new JPanel();
    JPanel pnlDeriveThickness = new JPanel();
    JPanel pnlFallbackThickness = new JPanel();
    JPanel pnlAutoFitRangeAndStep = new JPanel();
    //init
    ftfGradient.setPreferredWidth(272);
    btnModelFile.setToPreferredSize();
    ftfModelFile.setDirectiveDef(DirectiveDef.MODEL_FILE);
    //defaults
    rbUseSirtFalse.setSelected(true);
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
    pnlAutoFitRangeAndStep.add(rtfAutoFitRangeAndStep.getContainer());
    pnlAutoFitRangeAndStep.add(ltfAutoFitStep.getComponent());
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
    pnlThickness.add(rtfThickness.getContainer());
    pnlThickness.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlThickness.add(rtfBinnedThickness.getContainer());
    pnlThickness.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlThickness.add(pnlDeriveThickness);
    pnlThickness.add(pnlFallbackThickness);
    // DeriveThickness
    pnlDeriveThickness.setLayout(new BoxLayout(pnlDeriveThickness, BoxLayout.X_AXIS));
    pnlDeriveThickness.add(rbDeriveThickness.getComponent());
    pnlDeriveThickness.add(tfExtraThickness.getComponent());
    // FallbackThickness
    pnlFallbackThickness.setLayout(new BoxLayout(pnlFallbackThickness, BoxLayout.X_AXIS));
    pnlFallbackThickness.add(Box.createRigidArea(FixedDim.x40_y0));
    pnlFallbackThickness.add(ltfFallbackThickness.getComponent());
    pnlFallbackThickness.add(lFallbackThickness);
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
    rtfAutoFitRangeAndStep.addActionListener(this);
    rbFitEveryImage.addActionListener(this);
    rbUseSirtFalse.addActionListener(this);
    rbUseSirtTrue.addActionListener(this);
    rbDoBackprojAlso.addActionListener(this);
    rtfThickness.addActionListener(this);
    rtfBinnedThickness.addActionListener(this);
    rbDeriveThickness.addActionListener(this);
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
    rtfAutoFitRangeAndStep.setEnabled(ctf);
    rbFitEveryImage.setEnabled(ctf);
    boolean autoFitRangeAndStep = ctf && rtfAutoFitRangeAndStep.isSelected();
    ltfAutoFitStep.setEnabled(autoFitRangeAndStep);
    boolean sirt = rbUseSirtTrue.isSelected() || rbDoBackprojAlso.isSelected();
    ltfLeaveIterations.setEnabled(sirt);
    cbScaleToInteger.setEnabled(sirt);
    boolean deriveThickness = rbDeriveThickness.isSelected();
    tfExtraThickness.setEnabled(deriveThickness);
    ltfFallbackThickness.setEnabled(deriveThickness);
    lFallbackThickness.setEnabled(deriveThickness);
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
    if (rtfAutoFitRangeAndStep.isDifferentFromCheckpoint(true)) {
      rtfAutoFitRangeAndStep.backup();
      changed = true;
    }
    if (ltfAutoFitStep.isDifferentFromCheckpoint(true)) {
      ltfAutoFitStep.backup();
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
    if (rtfThickness.isDifferentFromCheckpoint(true)) {
      rtfThickness.backup();
      changed = true;
    }
    if (rtfBinnedThickness.isDifferentFromCheckpoint(true)) {
      rtfBinnedThickness.backup();
      changed = true;
    }
    if (rbDeriveThickness.isDifferentFromCheckpoint(true)) {
      rbDeriveThickness.backup();
      changed = true;
    }
    if (tfExtraThickness.isDifferentFromCheckpoint(true)) {
      tfExtraThickness.backup();
      changed = true;
    }
    if (ltfFallbackThickness.isDifferentFromCheckpoint(true)) {
      ltfFallbackThickness.backup();
      changed = true;
    }
    return changed;
  }
  
  void setDefaultValues() {
    ftfDistort.setDefaultValue();
    ftfGradient.setDefaultValue();
    cbRemoveXrays.setDefaultValue();
    ftfModelFile.setDefaultValue();
    rbTrackingMethodSeed.setDefaultValue();
    rbTrackingMethodRaptor.setDefaultValue();
    rbTrackingMethodPatchTracking.setDefaultValue();
    rbFiducialless.setDefaultValue();
    ltfGold.setDefaultValue();
    ltfLocalAreaTargetSize.setDefaultValue();
    ltfTargetNumberOfBeads.setDefaultValue();
    ltfSizeOfPatchesXandY.setDefaultValue();
    lsContourPieces.setDefaultValue();
    lsBinByFactor.setDefaultValue();
    cbCorrectCTF.setDefaultValue();
    ltfDefocus.setDefaultValue();
    rbFitEveryImage.setDefaultValue();
    rtfAutoFitRangeAndStep.setDefaultValue();
    ltfAutoFitStep.setDefaultValue();
    rbUseSirtFalse.setDefaultValue();
    rbUseSirtTrue.setDefaultValue();
    rbDoBackprojAlso.setDefaultValue();
    ltfLeaveIterations.setDefaultValue();
    cbScaleToInteger.setDefaultValue();
    rtfThickness.setDefaultValue();
    rtfBinnedThickness.setDefaultValue();
    rbDeriveThickness.setDefaultValue();
    tfExtraThickness.setDefaultValue();
    ltfFallbackThickness.setDefaultValue();
  }

  /**
   * Move any backed up values into the field, and delete the backup.
   */
  void restoreFromBackup() {
    ftfDistort.restoreFromBackup();
    ftfGradient.restoreFromBackup();
    cbRemoveXrays.restoreFromBackup();
    ftfModelFile.restoreFromBackup();
    rbTrackingMethodSeed.restoreFromBackup();
    rbTrackingMethodRaptor.restoreFromBackup();
    rbTrackingMethodPatchTracking.restoreFromBackup();
    rbFiducialless.restoreFromBackup();
    ltfGold.restoreFromBackup();
    ltfLocalAreaTargetSize.restoreFromBackup();
    ltfTargetNumberOfBeads.restoreFromBackup();
    ltfSizeOfPatchesXandY.restoreFromBackup();
    lsContourPieces.restoreFromBackup();
    lsBinByFactor.restoreFromBackup();
    cbCorrectCTF.restoreFromBackup();
    ltfDefocus.restoreFromBackup();
    rbFitEveryImage.restoreFromBackup();
    rtfAutoFitRangeAndStep.restoreFromBackup();
    ltfAutoFitStep.restoreFromBackup();
    rbUseSirtFalse.restoreFromBackup();
    rbUseSirtTrue.restoreFromBackup();
    rbDoBackprojAlso.restoreFromBackup();
    ltfLeaveIterations.restoreFromBackup();
    cbScaleToInteger.restoreFromBackup();
    rtfThickness.restoreFromBackup();
    rtfBinnedThickness.restoreFromBackup();
    rbDeriveThickness.restoreFromBackup();
    tfExtraThickness.restoreFromBackup();
    ltfFallbackThickness.restoreFromBackup();
  }

  void checkpoint() {
    ftfDistort.checkpoint();
    ftfGradient.checkpoint();
    cbRemoveXrays.checkpoint();
    ftfModelFile.checkpoint();
    rbTrackingMethodSeed.checkpoint();
    rbTrackingMethodRaptor.checkpoint();
    rbTrackingMethodPatchTracking.checkpoint();
    rbFiducialless.checkpoint();
    ltfGold.checkpoint();
    ltfLocalAreaTargetSize.checkpoint();
    ltfTargetNumberOfBeads.checkpoint();
    ltfSizeOfPatchesXandY.checkpoint();
    lsContourPieces.checkpoint();
    lsBinByFactor.checkpoint();
    cbCorrectCTF.checkpoint();
    ltfDefocus.checkpoint();
    rbFitEveryImage.checkpoint();
    rtfAutoFitRangeAndStep.checkpoint();
    ltfAutoFitStep.checkpoint();
    rbUseSirtFalse.checkpoint();
    rbUseSirtTrue.checkpoint();
    rbDoBackprojAlso.checkpoint();
    ltfLeaveIterations.checkpoint();
    cbScaleToInteger.checkpoint();
    rtfThickness.checkpoint();
    rtfBinnedThickness.checkpoint();
    rbDeriveThickness.checkpoint();
    tfExtraThickness.checkpoint();
    ltfFallbackThickness.checkpoint();
  }

  /**
   * Set values from the directive file collection.  Only change fields that exist in
   * directive file collection.
   * @param directiveFileCollection
   */
  void setValues(final DirectiveFileCollection directiveFileCollection) {
    if (directiveFileCollection.contains(DirectiveDef.DISTORT)) {
      ftfDistort.setText(directiveFileCollection.getValue(DirectiveDef.DISTORT));
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
    if (directiveFileCollection.contains(DirectiveDef.TARGET_NUMBER_OF_BEADS)) {
      ltfTargetNumberOfBeads.setText(directiveFileCollection
          .getValue(DirectiveDef.TARGET_NUMBER_OF_BEADS));
    }
    if (directiveFileCollection.contains(DirectiveDef.SIZE_OF_PATCHES_X_AND_Y)) {
      ltfSizeOfPatchesXandY.setText(directiveFileCollection
          .getValue(DirectiveDef.SIZE_OF_PATCHES_X_AND_Y));
    }
    if (directiveFileCollection.contains(DirectiveDef.CONTOUR_PIECES)) {
      lsContourPieces.setValue(directiveFileCollection
          .getValue(DirectiveDef.CONTOUR_PIECES));
    }
    if (directiveFileCollection.contains(DirectiveDef.BIN_BY_FACTOR_FOR_ALIGNED_STACK)) {
      lsBinByFactor.setValue(directiveFileCollection
          .getValue(DirectiveDef.BIN_BY_FACTOR_FOR_ALIGNED_STACK));
    }
    if (directiveFileCollection.contains(DirectiveDef.CORRECT_CTF)) {
      cbCorrectCTF.setSelected(directiveFileCollection.isValue(DirectiveDef.CORRECT_CTF));
    }
    if (directiveFileCollection.contains(DirectiveDef.DEFOCUS)) {
      ltfDefocus.setText(directiveFileCollection.getValue(DirectiveDef.DEFOCUS));
    }
    if (directiveFileCollection.contains(DirectiveDef.AUTO_FIT_RANGE_AND_STEP)) {
      EtomoNumber number = new EtomoNumber(EtomoNumber.Type.DOUBLE);
      number.set(directiveFileCollection.getValue(DirectiveDef.AUTO_FIT_RANGE_AND_STEP,
          DirectiveFile.AUTO_FIT_STEP_INDEX));
      if (number.equals(0)) {
        rbFitEveryImage.setSelected(true);
      }
      else {
        rtfAutoFitRangeAndStep.setSelected(true);
        rtfAutoFitRangeAndStep.setText(directiveFileCollection.getValue(
            DirectiveDef.AUTO_FIT_RANGE_AND_STEP, DirectiveFile.AUTO_FIT_RANGE_INDEX));
        ltfAutoFitStep.setText(number.toString());
      }
    }
    boolean useSirt = false;
    if (directiveFileCollection.contains(DirectiveDef.USE_SIRT)) {
      useSirt = directiveFileCollection.isValue(DirectiveDef.USE_SIRT);
    }
    boolean doBackprojAlso = false;
    if (directiveFileCollection.contains(DirectiveDef.DO_BACKPROJ_ALSO)) {
      doBackprojAlso = directiveFileCollection.isValue(DirectiveDef.DO_BACKPROJ_ALSO);
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
    if (directiveFileCollection.contains(DirectiveDef.LEAVE_ITERATIONS)) {
      ltfLeaveIterations.setText(directiveFileCollection
          .getValue(DirectiveDef.LEAVE_ITERATIONS));
    }
    if (directiveFileCollection.contains(DirectiveDef.SCALE_TO_INTEGER)) {
      cbScaleToInteger.setSelected(directiveFileCollection
          .isValue(DirectiveDef.SCALE_TO_INTEGER));
    }
    // Derive thickness from log when thickness is not specified.
    // Priority of directives
    // 1. THICKNESS
    // 2. binnedThickness
    // 3. fallbackThickness (causes derived thickness to be checked)
    if (directiveFileCollection.contains(DirectiveDef.THICKNESS)) {
      rtfThickness.setSelected(true);
      rtfThickness.setText(directiveFileCollection.getValue(DirectiveDef.THICKNESS));
    }
    if (directiveFileCollection.contains(DirectiveDef.BINNED_THICKNESS)) {
      if (!rtfThickness.isSelected()) {
        rtfBinnedThickness.setSelected(true);
      }
      rtfBinnedThickness.setText(directiveFileCollection
          .getValue(DirectiveDef.BINNED_THICKNESS));
    }
    if (directiveFileCollection.contains(DirectiveDef.EXTRA_THICKNESS)) {
      tfExtraThickness.setText(directiveFileCollection
          .getValue(DirectiveDef.EXTRA_THICKNESS));
    }
    if (directiveFileCollection.contains(DirectiveDef.FALLBACK_THICKNESS)) {
      ltfFallbackThickness.setText(directiveFileCollection
          .getValue(DirectiveDef.FALLBACK_THICKNESS));
      if (!rtfThickness.isSelected() && !rtfBinnedThickness.isSelected()) {
        rbDeriveThickness.setSelected(true);
      }
    }
    updateDisplay();
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
        || actionCommand.equals(rtfAutoFitRangeAndStep.getActionCommand())
        || actionCommand.equals(rbFitEveryImage.getActionCommand())
        || actionCommand.equals(rtfThickness.getActionCommand())
        || actionCommand.equals(rtfBinnedThickness.getActionCommand())
        || actionCommand.equals(rbDeriveThickness.getActionCommand())
        || actionCommand.equals(rbUseSirtFalse.getActionCommand())
        || actionCommand.equals(rbUseSirtTrue.getActionCommand())
        || actionCommand.equals(rbDoBackprojAlso.getActionCommand())) {
      updateDisplay();
    }
  }
}
