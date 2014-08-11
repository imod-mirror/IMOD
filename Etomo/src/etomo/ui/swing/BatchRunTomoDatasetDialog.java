package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.logic.TrackingMethod;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.type.AxisID;
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
      FieldType.INTEGER, "with fallback (unbinned pixels): ");
  private final List<Field> fieldList = new ArrayList<Field>();
  private final MultiLineButton btnOk = new MultiLineButton("OK");
  private final MultiLineButton btnRevertToGlobal = new MultiLineButton(
      "Revert to Global");

  private final FileTextField2 ftfDistort;
  private final FileTextField2 ftfGradient;
  private final FileTextField2 ftfModelFile;
  private final JDialog dialog;
  private final String key;
  private final BatchRunTomoDialog datasetDialogMap;

  private BatchRunTomoDatasetDialog(final BaseManager manager, final String key,
      final BatchRunTomoDialog datasetDialogMap) {
    ftfDistort = FileTextField2.getAltLayoutInstance(manager, "Image distortion file: ");
    ftfGradient = FileTextField2.getAltLayoutInstance(manager, "Mag gradient file: ");
    ftfModelFile = FileTextField2.getAltLayoutInstance(manager,
        "Manual replacement model: ");
    this.key = key;
    this.datasetDialogMap = datasetDialogMap;
    if (key == null) {
      dialog = null;
    }
    else {
      dialog = new JDialog();
    }
  }

  static BatchRunTomoDatasetDialog getGlobalInstance(final BaseManager manager) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager, null,
        null);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  static BatchRunTomoDatasetDialog getIndividualInstance(final BaseManager manager,
      final String key, final BatchRunTomoDialog datasetDialogMap) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager, key,
        datasetDialogMap);
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
    JPanel pnlButtons = null;
    if (dialog != null) {
      pnlButtons = new JPanel();
    }
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
    if (key != null) {
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
    if (pnlButtons != null) {
      pnlRoot.add(pnlButtons);
    }
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

  void setVisible() {
    dialog.setVisible(true);
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
    btnRevertToGlobal.addActionListener(this);
    btnOk.addActionListener(this);
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
    setDefaults();
  }

  void copy(final BatchRunTomoDatasetDialog copyFrom) {
    Iterator<Field> iterator = fieldList.iterator();
    Iterator<Field> fromIterator = copyFrom.fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().copy(fromIterator.next());
      }
    }
    updateDisplay();
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
        if (!setFieldHighlightValue) {
          rbTrackingMethodSeed.setSelected(true);
        }
        else {
          rbTrackingMethodSeed.setFieldHighlightValue(true);
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
    if (directiveFileCollection.contains(DirectiveDef.USE_SIRT, setFieldHighlightValue)) {
      useSirt = directiveFileCollection.isValue(DirectiveDef.USE_SIRT,
          setFieldHighlightValue);
    }
    boolean doBackprojAlso = false;
    if (directiveFileCollection.contains(DirectiveDef.DO_BACKPROJ_ALSO,
        setFieldHighlightValue)) {
      doBackprojAlso = directiveFileCollection.isValue(DirectiveDef.DO_BACKPROJ_ALSO,
          setFieldHighlightValue);
    }
    if (!useSirt) {
      // no default for rbUseSirtFalse
      if (!setFieldHighlightValue) {
        rbUseSirtFalse.setSelected(true);
      }
    }
    else if (!doBackprojAlso) {
      if (!setFieldHighlightValue) {
        rbUseSirtTrue.setSelected(true);
      }
      else {
        rbUseSirtTrue.setFieldHighlightValue(true);
      }
    }
    else if (!setFieldHighlightValue) {
      rbDoBackprojAlso.setSelected(true);
    }
    else {
      rbDoBackprojAlso.setFieldHighlightValue(true);
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

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (actionCommand.equals(btnRevertToGlobal.getActionCommand())) {
      if (datasetDialogMap != null) {
        if (UIHarness.INSTANCE
            .openYesNoDialog(
                null,
                "Data in this window will be lost.  Revert to global dataset data for this stack?",
                AxisID.ONLY)) {
          datasetDialogMap.removeDatasetDialog(key);
          dialog.setVisible(false);
        }
      }
    }
    else if (actionCommand.equals(btnOk.getActionCommand())) {
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
