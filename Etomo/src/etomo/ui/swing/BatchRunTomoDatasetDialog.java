package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.logic.SeedingMethod;
import etomo.logic.TrackingMethod;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.type.DataFileType;
import etomo.type.DialogType;
import etomo.type.EtomoNumber;
import etomo.ui.Field;
import etomo.ui.FieldType;
import etomo.ui.TextFieldInterface;

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
final class BatchRunTomoDatasetDialog implements ActionListener, Expandable {
  public static final String rcsid = "$Id:$";

  private static final String DERIVE_THICKNESS_LABEL = "Thickness from Intergold spacing plus: ";

  private static BatchRunTomoDatasetDialog GLOBAL_INSTANCE = null;

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
      FieldType.INTEGER, "with fallback (unbinned pixels): ");
  private final List<Field> fieldList = new ArrayList<Field>();
  private final MultiLineButton btnOk = new MultiLineButton("OK");
  private final MultiLineButton btnRevertToGlobal = new MultiLineButton(
      "Revert to Global");
  private final JPanel pnlRootBody = new JPanel();

  private final FileTextField2 ftfDistort;
  private final FileTextField2 ftfGradient;
  private final FileTextField2 ftfModelFile;
  private final JDialog dialog;
  private final PanelHeader phRootHeader;
  private final BaseManager manager;
  private final File datasetFile;

  private boolean debug = false;

  private BatchRunTomoDatasetDialog(final BaseManager manager, final File datasetFile,
      final boolean global) {
    this.manager = manager;
    this.datasetFile = datasetFile;
    ftfDistort = FileTextField2.getAltLayoutInstance(manager, "Image distortion file: ");
    ftfGradient = FileTextField2.getAltLayoutInstance(manager, "Mag gradient file: ");
    ftfModelFile = FileTextField2.getAltLayoutInstance(manager,
        "Manual replacement model: ");

    if (global) {
      dialog = null;
    }
    else {
      dialog = new JDialog();
    }
    phRootHeader = PanelHeader.getInstance("Global Dataset Values", this,
        DialogType.BATCH_RUN_TOMO);
  }

  static BatchRunTomoDatasetDialog getGlobalInstance(final BaseManager manager) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager, null,
        true);
    instance.createPanel();
    instance.addListeners();
    GLOBAL_INSTANCE = instance;
    return instance;
  }

  static BatchRunTomoDatasetDialog getIndividualInstance(final BaseManager manager,
      final File datasetFile) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager,
        datasetFile, false);
    instance.createPanel();
    instance.copyFromGlobal();
    instance.addListeners();
    instance.setVisible(true);
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
    JPanel pnlButtons = null;
    if (dialog != null) {
      pnlButtons = new JPanel();
    }
    JPanel pnlRemoveXrays = new JPanel();
    JPanel pnlEnableStretching = new JPanel();
    JPanel pnlLocalAlignments = new JPanel();
    // init
    ftfGradient.setPreferredWidth(272);
    btnModelFile.setToPreferredSize();
    btnRevertToGlobal.setToPreferredSize();
    btnOk.setToPreferredSize();
    // directives
    ftfModelFile.setDirectiveDef(DirectiveDef.MODEL_FILE);
    ltfLocalAreaTargetSize.setDirectiveDef(DirectiveDef.LOCAL_AREA_TARGET_SIZE);
    ltfTargetNumberOfBeads.setDirectiveDef(DirectiveDef.TARGET_NUMBER_OF_BEADS);
    ltfSizeOfPatchesXandY.setDirectiveDef(DirectiveDef.SIZE_OF_PATCHES_X_AND_Y);
    ltfLeaveIterations.setDirectiveDef(DirectiveDef.LEAVE_ITERATIONS);
    rtfThickness.setDirectiveDef(DirectiveDef.THICKNESS);
    // field list
    fieldList.add(ftfDistort);
    fieldList.add(ftfGradient);
    fieldList.add(cbRemoveXrays);
    fieldList.add(ftfModelFile);
    fieldList.add(rbTrackingMethodSeed);
    fieldList.add(rbTrackingMethodRaptor);
    fieldList.add(rbTrackingMethodPatchTracking);
    fieldList.add(rbFiducialless);
    fieldList.add(ltfGold);
    fieldList.add(ltfLocalAreaTargetSize);
    fieldList.add(ltfTargetNumberOfBeads);
    fieldList.add(ltfSizeOfPatchesXandY);
    fieldList.add(lsContourPieces);
    fieldList.add(cbEnableStretching);
    fieldList.add(cbLocalAlignments);
    fieldList.add(lsBinByFactor);
    fieldList.add(cbCorrectCTF);
    fieldList.add(ltfDefocus);
    fieldList.add(rbFitEveryImage);
    fieldList.add(rtfAutoFitRangeAndStep);
    fieldList.add(ltfAutoFitStep);
    fieldList.add(rbUseSirtFalse);
    fieldList.add(rbUseSirtTrue);
    fieldList.add(rbDoBackprojAlso);
    fieldList.add(ltfLeaveIterations);
    fieldList.add(cbScaleToInteger);
    fieldList.add(rtfThickness);
    fieldList.add(rtfBinnedThickness);
    fieldList.add(rbDeriveThickness);
    fieldList.add(tfExtraThickness);
    fieldList.add(ltfFallbackThickness);
    // defaults
    setDefaults();
    // dialog
    if (dialog != null) {
      dialog.add(pnlRoot);
    }
    // title
    if (datasetFile != null) {
      String path = datasetFile.getAbsolutePath();
      int index = path.lastIndexOf(DataFileType.RECON.extension);
      String key;
      if (index != -1) {
        key = path.substring(0, index);
      }
      else {
        key = path;
      }
      int titleLen = 53;
      int keyLen = key.length();
      if (keyLen <= titleLen) {
        dialog.setTitle(key);
      }
      else {
        dialog.setTitle("..." + key.substring(keyLen - titleLen - 3));
      }
    }
    // root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(BorderFactory.createEtchedBorder());
    if (dialog == null) {
      pnlRoot.add(phRootHeader.getContainer());
      pnlRoot.add(Box.createRigidArea(FixedDim.x0_y2));
    }
    pnlRoot.add(pnlRootBody);
    // root body
    pnlRootBody.setLayout(new BoxLayout(pnlRootBody, BoxLayout.Y_AXIS));
    pnlRootBody.add(ftfDistort.getRootPanel());
    pnlRootBody.add(ftfGradient.getRootPanel());
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlRootBody.add(pnlRemoveXrays);
    pnlRootBody.add(pnlModelFile);
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRootBody.add(pnlTrackingMethod);
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y3));
    pnlRootBody.add(pnlGold);
    pnlRootBody.add(pnlSizeOfPatchesXandY);
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlRootBody.add(pnlEnableStretching);
    pnlRootBody.add(pnlLocalAlignments);
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlRootBody.add(pnlBinByFactor);
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRootBody.add(pnlCorrectCTF);
    pnlRootBody.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRootBody.add(pnlReconstruction);
    if (pnlButtons != null) {
      pnlRootBody.add(pnlButtons);
    }
    // RemoveXrays
    pnlRemoveXrays.setLayout(new BoxLayout(pnlRemoveXrays, BoxLayout.X_AXIS));
    pnlRemoveXrays.add(cbRemoveXrays);
    pnlRemoveXrays.add(Box.createHorizontalGlue());
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
    // EnableStretching
    pnlEnableStretching.setLayout(new BoxLayout(pnlEnableStretching, BoxLayout.X_AXIS));
    pnlEnableStretching.add(cbEnableStretching);
    pnlEnableStretching.add(Box.createHorizontalGlue());
    // LocalAlignments
    pnlLocalAlignments.setLayout(new BoxLayout(pnlLocalAlignments, BoxLayout.X_AXIS));
    pnlLocalAlignments.add(cbLocalAlignments);
    pnlLocalAlignments.add(Box.createHorizontalGlue());
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
    // buttons
    if (pnlButtons != null) {
      pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
      pnlButtons.add(Box.createHorizontalGlue());
      pnlButtons.add(btnOk.getComponent());
      pnlButtons.add(Box.createRigidArea(FixedDim.x20_y0));
      pnlButtons.add(btnRevertToGlobal.getComponent());
    }
    // align
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlReconstructionType, Component.LEFT_ALIGNMENT);
    // update
    updateDisplay();
    // display
    if (dialog != null) {
      dialog.pack();
    }
  }

  void setVisible(final boolean visible) {
    dialog.setVisible(visible);
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
    btnOk.addActionListener(this);
  }

  Component getComponent() {
    return pnlRoot;
  }

  public void expand(final ExpandButton button) {
    if (dialog != null) {
      // individual instance doesn't need a panel header
      return;
    }
    pnlRootBody.setVisible(button.isExpanded());
    UIHarness.INSTANCE.pack(manager);
  }

  public void expand(final GlobalExpandButton button) {
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
  }

  /**
   * Check isDifferentFromCheckpoint on all data entry fields
   * @return true if any field's isDifferentFromCheckpoint function returned true
   */
  boolean backupIfChanged() {
    boolean changed = false;
    Iterator<Field> iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        Field field = iterator.next();
        if (field.isDifferentFromCheckpoint(true)) {
          field.backup();
          changed = true;
        }
      }
    }
    return changed;
  }

  /**
   * Set the fields to their default values.
   */
  void useDefaultValues() {
    Iterator<Field> iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().useDefaultValue();
      }
    }
  }

  /**
   * Move any backed up values into the field, and delete the backup.
   */
  void restoreFromBackup() {
    Iterator<Field> iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().restoreFromBackup();
      }
    }
  }

  void checkpoint() {
    Iterator<Field> iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().checkpoint();
      }
    }
  }

  void clear() {
    Iterator<Field> iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().clear();
      }
    }
    iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().clearFieldHighlightValue();
      }
    }
    setDefaults();
  }

  private void copyFromGlobal() {
    if (GLOBAL_INSTANCE == null) {
      return;
    }
    Iterator<Field> iterator = fieldList.iterator();
    Iterator<Field> globalIterator = GLOBAL_INSTANCE.fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().copy(globalIterator.next());
      }
    }
    updateDisplay();
    // set checkpoint from global
    ftfDistort.checkpoint(GLOBAL_INSTANCE.ftfDistort);
    cbRemoveXrays.checkpoint(GLOBAL_INSTANCE.cbRemoveXrays);
    ftfModelFile.checkpoint(GLOBAL_INSTANCE.ftfModelFile);
    rbTrackingMethodSeed.checkpoint(GLOBAL_INSTANCE.rbTrackingMethodSeed);
    rbTrackingMethodRaptor.checkpoint(GLOBAL_INSTANCE.rbTrackingMethodRaptor);
    rbTrackingMethodPatchTracking
        .checkpoint(GLOBAL_INSTANCE.rbTrackingMethodPatchTracking);
    rbFiducialless.checkpoint(GLOBAL_INSTANCE.rbFiducialless);
    ltfGold.checkpoint(GLOBAL_INSTANCE.ltfGold);
    ltfLocalAreaTargetSize.checkpoint(GLOBAL_INSTANCE.ltfLocalAreaTargetSize);
    ltfTargetNumberOfBeads.checkpoint(GLOBAL_INSTANCE.ltfTargetNumberOfBeads);
    ltfSizeOfPatchesXandY.checkpoint(GLOBAL_INSTANCE.ltfSizeOfPatchesXandY);
    lsContourPieces.checkpoint(GLOBAL_INSTANCE.lsContourPieces);
    cbEnableStretching.checkpoint(GLOBAL_INSTANCE.cbEnableStretching);
    cbLocalAlignments.checkpoint(GLOBAL_INSTANCE.cbLocalAlignments);
    lsBinByFactor.checkpoint(GLOBAL_INSTANCE.lsBinByFactor);
    cbCorrectCTF.checkpoint(GLOBAL_INSTANCE.cbCorrectCTF);
    ltfDefocus.checkpoint(GLOBAL_INSTANCE.ltfDefocus);
    rbFitEveryImage.checkpoint(GLOBAL_INSTANCE.rbFitEveryImage);
    rtfAutoFitRangeAndStep.checkpoint(GLOBAL_INSTANCE.rtfAutoFitRangeAndStep);
    ltfAutoFitStep.checkpoint(GLOBAL_INSTANCE.ltfAutoFitStep);
    rbUseSirtFalse.checkpoint(GLOBAL_INSTANCE.rbUseSirtFalse);
    rbUseSirtTrue.checkpoint(GLOBAL_INSTANCE.rbUseSirtTrue);
    rbDoBackprojAlso.checkpoint(GLOBAL_INSTANCE.rbDoBackprojAlso);
    ltfLeaveIterations.checkpoint(GLOBAL_INSTANCE.ltfLeaveIterations);
    cbScaleToInteger.checkpoint(GLOBAL_INSTANCE.cbScaleToInteger);
    rtfThickness.checkpoint(GLOBAL_INSTANCE.rtfThickness);
    rtfBinnedThickness.checkpoint(GLOBAL_INSTANCE.rtfBinnedThickness);
    rbDeriveThickness.checkpoint(GLOBAL_INSTANCE.rbDeriveThickness);
    tfExtraThickness.checkpoint(GLOBAL_INSTANCE.tfExtraThickness);
    ltfFallbackThickness.checkpoint(GLOBAL_INSTANCE.ltfFallbackThickness);
    // set field highlight from global
    ftfDistort.setFieldHighlightValue(GLOBAL_INSTANCE.ftfDistort);
    cbRemoveXrays.setFieldHighlightValue(GLOBAL_INSTANCE.cbRemoveXrays);
    ftfModelFile.setFieldHighlightValue(GLOBAL_INSTANCE.ftfModelFile);
    rbTrackingMethodSeed.setFieldHighlightValue(GLOBAL_INSTANCE.rbTrackingMethodSeed);
    rbTrackingMethodRaptor.setFieldHighlightValue(GLOBAL_INSTANCE.rbTrackingMethodRaptor);
    rbTrackingMethodPatchTracking
        .setFieldHighlightValue(GLOBAL_INSTANCE.rbTrackingMethodPatchTracking);
    rbFiducialless.setFieldHighlightValue(GLOBAL_INSTANCE.rbFiducialless);
    ltfGold.setFieldHighlightValue(GLOBAL_INSTANCE.ltfGold);
    ltfLocalAreaTargetSize.setFieldHighlightValue(GLOBAL_INSTANCE.ltfLocalAreaTargetSize);
    ltfTargetNumberOfBeads.setFieldHighlightValue(GLOBAL_INSTANCE.ltfTargetNumberOfBeads);
    ltfSizeOfPatchesXandY.setFieldHighlightValue(GLOBAL_INSTANCE.ltfSizeOfPatchesXandY);
    lsContourPieces.setFieldHighlightValue(GLOBAL_INSTANCE.lsContourPieces);
    cbEnableStretching.setFieldHighlightValue(GLOBAL_INSTANCE.cbEnableStretching);
    cbLocalAlignments.setFieldHighlightValue(GLOBAL_INSTANCE.cbLocalAlignments);
    lsBinByFactor.setFieldHighlightValue(GLOBAL_INSTANCE.lsBinByFactor);
    cbCorrectCTF.setFieldHighlightValue(GLOBAL_INSTANCE.cbCorrectCTF);
    ltfDefocus.setFieldHighlightValue(GLOBAL_INSTANCE.ltfDefocus);
    rbFitEveryImage.setFieldHighlightValue(GLOBAL_INSTANCE.rbFitEveryImage);
    rtfAutoFitRangeAndStep.setFieldHighlightValue(GLOBAL_INSTANCE.rtfAutoFitRangeAndStep);
    ltfAutoFitStep.setFieldHighlightValue(GLOBAL_INSTANCE.ltfAutoFitStep);
    rbUseSirtFalse.setFieldHighlightValue(GLOBAL_INSTANCE.rbUseSirtFalse);
    rbUseSirtTrue.setFieldHighlightValue(GLOBAL_INSTANCE.rbUseSirtTrue);
    rbDoBackprojAlso.setFieldHighlightValue(GLOBAL_INSTANCE.rbDoBackprojAlso);
    ltfLeaveIterations.setFieldHighlightValue(GLOBAL_INSTANCE.ltfLeaveIterations);
    cbScaleToInteger.setFieldHighlightValue(GLOBAL_INSTANCE.cbScaleToInteger);
    rtfThickness.setFieldHighlightValue(GLOBAL_INSTANCE.rtfThickness);
    rtfBinnedThickness.setFieldHighlightValue(GLOBAL_INSTANCE.rtfBinnedThickness);
    rbDeriveThickness.setFieldHighlightValue(GLOBAL_INSTANCE.rbDeriveThickness);
    tfExtraThickness.setFieldHighlightValue(GLOBAL_INSTANCE.tfExtraThickness);
    ltfFallbackThickness.setFieldHighlightValue(GLOBAL_INSTANCE.ltfFallbackThickness);
  }

  private void setDefaults() {
    rbUseSirtFalse.setSelected(true);
  }

  void setValues(final DirectiveFileCollection directiveFileCollection) {
    setValues(directiveFileCollection, false);
  }

  void setFieldHighlightValues(final DirectiveFileCollection directiveFileCollection) {
    setValues(directiveFileCollection, true);
  }

  /**
   * Set values from the directive file collection.  Only change fields that exist in
   * directive file collection.
   * @param directiveFileCollection
   */
  private void setValues(final DirectiveFileCollection directiveFileCollection,
      final boolean setFieldHighlightValue) {
    setValue(directiveFileCollection, DirectiveDef.DISTORT, setFieldHighlightValue,
        ftfDistort);
    setValue(directiveFileCollection, DirectiveDef.GRADIENT, setFieldHighlightValue,
        ftfGradient);
    setValue(directiveFileCollection, DirectiveDef.REMOVE_XRAYS, setFieldHighlightValue,
        cbRemoveXrays);
    setValue(directiveFileCollection, DirectiveDef.MODEL_FILE, setFieldHighlightValue,
        ftfModelFile);
    if (directiveFileCollection.contains(DirectiveDef.TRACKING_METHOD)) {
      TrackingMethod trackingMethod = TrackingMethod.getInstance(directiveFileCollection
          .getValue(DirectiveDef.TRACKING_METHOD));
      if (trackingMethod == TrackingMethod.SEED) {
        if (directiveFileCollection.contains(DirectiveDef.SEEDING_METHOD)) {
          SeedingMethod seedingMethod = SeedingMethod.getInstance(directiveFileCollection
              .getValue(DirectiveDef.SEEDING_METHOD));
          if (seedingMethod == SeedingMethod.AUTO_FID_SEED
              || seedingMethod == SeedingMethod.BOTH) {
            if (!setFieldHighlightValue) {
              rbTrackingMethodSeed.setSelected(true);
            }
            else {
              rbTrackingMethodSeed.setFieldHighlightValue(true);
            }
          }
        }
      }
      else if (trackingMethod == TrackingMethod.RAPTOR) {
        if (!setFieldHighlightValue) {
          rbTrackingMethodRaptor.setSelected(true);
        }
        else {
          rbTrackingMethodRaptor.setFieldHighlightValue(true);
        }
      }
      else if (trackingMethod == TrackingMethod.PATCH_TRACKING) {
        if (!setFieldHighlightValue) {
          rbTrackingMethodPatchTracking.setSelected(true);
        }
        else {
          rbTrackingMethodPatchTracking.setFieldHighlightValue(true);
        }
      }
    }
    setValue(directiveFileCollection, DirectiveDef.FIDUCIALLESS, setFieldHighlightValue,
        rbFiducialless);
    setValue(directiveFileCollection, DirectiveDef.LOCAL_AREA_TARGET_SIZE,
        setFieldHighlightValue, ltfLocalAreaTargetSize);
    setValue(directiveFileCollection, DirectiveDef.TARGET_NUMBER_OF_BEADS,
        setFieldHighlightValue, ltfTargetNumberOfBeads);
    setValue(directiveFileCollection, DirectiveDef.SIZE_OF_PATCHES_X_AND_Y,
        setFieldHighlightValue, ltfSizeOfPatchesXandY);
    setValue(directiveFileCollection, DirectiveDef.CONTOUR_PIECES,
        setFieldHighlightValue, lsContourPieces);
    setValue(directiveFileCollection, DirectiveDef.ENABLE_STRETCHING,
        setFieldHighlightValue, cbEnableStretching);
    setValue(directiveFileCollection, DirectiveDef.LOCAL_ALIGNMENTS,
        setFieldHighlightValue, cbLocalAlignments);
    setValue(directiveFileCollection, DirectiveDef.BIN_BY_FACTOR_FOR_ALIGNED_STACK,
        setFieldHighlightValue, lsBinByFactor);
    setValue(directiveFileCollection, DirectiveDef.CORRECT_CTF, setFieldHighlightValue,
        cbCorrectCTF);
    setValue(directiveFileCollection, DirectiveDef.DEFOCUS, setFieldHighlightValue,
        ltfDefocus);
    if (directiveFileCollection.contains(DirectiveDef.AUTO_FIT_RANGE_AND_STEP)) {
      EtomoNumber number = new EtomoNumber(EtomoNumber.Type.DOUBLE);
      number.set(directiveFileCollection.getValue(DirectiveDef.AUTO_FIT_RANGE_AND_STEP,
          DirectiveFile.AUTO_FIT_STEP_INDEX));
      if (number.equals(0)) {
        if (!setFieldHighlightValue) {
          rbFitEveryImage.setSelected(true);
        }
        else {
          rbFitEveryImage.setFieldHighlightValue(true);
        }
      }
      else {
        String range = directiveFileCollection.getValue(
            DirectiveDef.AUTO_FIT_RANGE_AND_STEP, DirectiveFile.AUTO_FIT_RANGE_INDEX);
        if (!setFieldHighlightValue) {
          rtfAutoFitRangeAndStep.setSelected(true);
          rtfAutoFitRangeAndStep.setText(range);
          ltfAutoFitStep.setText(number.toString());
        }
        else {
          rtfAutoFitRangeAndStep.setFieldHighlightValue(true);
          rtfAutoFitRangeAndStep.setFieldHighlightValue(range);
          ltfAutoFitStep.setFieldHighlightValue(number.toString());
        }
      }
    }
    boolean useSirt = false;
    boolean containsUseSirt = false;
    if (directiveFileCollection.contains(DirectiveDef.USE_SIRT, setFieldHighlightValue)) {
      containsUseSirt = true;
      useSirt = directiveFileCollection.isValue(DirectiveDef.USE_SIRT,
          setFieldHighlightValue);
    }
    boolean doBackprojAlso = false;
    boolean containsDoBackprojAlso = false;
    if (directiveFileCollection.contains(DirectiveDef.DO_BACKPROJ_ALSO,
        setFieldHighlightValue)) {
      containsDoBackprojAlso = true;
      doBackprojAlso = directiveFileCollection.isValue(DirectiveDef.DO_BACKPROJ_ALSO,
          setFieldHighlightValue);
    }
    // Set values based on useSirt doBackProjAlso booleans.
    if (useSirt && doBackprojAlso) {
      if (!setFieldHighlightValue) {
        rbDoBackprojAlso.setSelected(true);
      }
      else {
        rbDoBackprojAlso.setFieldHighlightValue(true);
      }
    }
    else if (useSirt) {
      if (!setFieldHighlightValue) {
        rbUseSirtTrue.setSelected(true);
      }
      else {
        rbUseSirtTrue.setFieldHighlightValue(true);
      }
      if (containsDoBackprojAlso) {
        rbDoBackprojAlso.setFieldHighlightValue(false);
      }
    }
    else {
      rbUseSirtFalse.setSelected(true);
      if (containsUseSirt) {
        rbUseSirtTrue.setFieldHighlightValue(false);
      }
    }
    setValue(directiveFileCollection, DirectiveDef.LEAVE_ITERATIONS,
        setFieldHighlightValue, ltfLeaveIterations);
    setValue(directiveFileCollection, DirectiveDef.SCALE_TO_INTEGER,
        setFieldHighlightValue, cbScaleToInteger);
    // Derive thickness from log when thickness is not specified.
    // Priority of directives
    // 1. THICKNESS
    // 2. binnedThickness
    // 3. fallbackThickness (causes derived thickness to be checked)
    boolean thickness = false;
    if (setValue(directiveFileCollection, DirectiveDef.THICKNESS, setFieldHighlightValue,
        rtfThickness)) {
      thickness = true;
      if (!setFieldHighlightValue) {
        rtfThickness.setSelected(true);
      }
      else {
        rtfThickness.setFieldHighlightValue(true);
      }
    }
    boolean binnedThickness = false;
    if (setValue(directiveFileCollection, DirectiveDef.BINNED_THICKNESS,
        setFieldHighlightValue, rtfBinnedThickness)) {
      if (!thickness) {
        binnedThickness = true;
        if (!setFieldHighlightValue) {
          rtfBinnedThickness.setSelected(true);
        }
        else {
          rtfBinnedThickness.setFieldHighlightValue(true);
        }
      }
    }
    boolean useFieldHighlight = setValue(directiveFileCollection,
        DirectiveDef.EXTRA_THICKNESS, setFieldHighlightValue, tfExtraThickness)
        || setValue(directiveFileCollection, DirectiveDef.FALLBACK_THICKNESS,
            setFieldHighlightValue, ltfFallbackThickness);
    if (!thickness && !binnedThickness) {
      if (!setFieldHighlightValue) {
        rbDeriveThickness.setSelected(true);
      }
      else if (useFieldHighlight) {
        rbDeriveThickness.setFieldHighlightValue(true);
      }
    }
    updateDisplay();
  }

  private boolean setValue(final DirectiveFileCollection directiveFileCollection,
      final DirectiveDef directiveDef, final boolean setFieldHighlightValue,
      final TextFieldInterface textField) {
    if (directiveFileCollection.contains(directiveDef, setFieldHighlightValue)) {
      String value = directiveFileCollection.getValue(directiveDef,
          setFieldHighlightValue);
      if (!setFieldHighlightValue) {
        textField.setText(value);
      }
      else {
        textField.setFieldHighlightValue(value);
      }
      return true;
    }
    return false;
  }

  private void setValue(final DirectiveFileCollection directiveFileCollection,
      final DirectiveDef directiveDef, final boolean setFieldHighlightValue,
      final CheckBox checkBox) {
    if (directiveFileCollection.contains(directiveDef, setFieldHighlightValue)) {
      boolean value = directiveFileCollection.isValue(directiveDef,
          setFieldHighlightValue);
      if (!setFieldHighlightValue) {
        checkBox.setSelected(value);
      }
      else {
        checkBox.setFieldHighlightValue(value);
      }
    }
  }

  private void setValue(final DirectiveFileCollection directiveFileCollection,
      final DirectiveDef directiveDef, final boolean setFieldHighlightValue,
      final RadioButton radioButton) {
    if (directiveFileCollection.contains(directiveDef, setFieldHighlightValue)) {
      boolean value = directiveFileCollection.isValue(directiveDef,
          setFieldHighlightValue);
      if (!setFieldHighlightValue) {
        if (value) {
          radioButton.setSelected(true);
        }
      }
      else {
        radioButton.setFieldHighlightValue(value);
      }
    }
  }

  String getRevertToGlobalActionCommand() {
    if (btnRevertToGlobal != null) {
      return btnRevertToGlobal.getActionCommand();
    }
    return null;
  }

  void addRevertToGlobalActionListener(final ActionListener listener) {
    btnRevertToGlobal.addActionListener(listener);
  }

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (btnOk != null && actionCommand.equals(btnOk.getActionCommand())) {
      dialog.setVisible(false);
    }
    else if (actionCommand.equals(cbRemoveXrays.getActionCommand())
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
