package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.logic.BatchTool;
import etomo.logic.ConfigTool;
import etomo.logic.SeedingMethod;
import etomo.logic.TrackingMethod;
import etomo.process.BaseProcessManager;
import etomo.storage.*;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.*;
import etomo.ui.BooleanFieldSetting;
import etomo.ui.Field;
import etomo.ui.FieldType;
import etomo.ui.UIComponent;

/**
 * <p>Description: Contains parameters that are dataset values.  Can be used for all
 * datasets and individual ones. </p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
final class BatchRunTomoDatasetDialog
    implements ActionListener, Expandable, UIComponent, SwingComponent {
  private static final String DERIVE_THICKNESS_LABEL =
      "Thickness from Intergold spacing plus: ";
  private static final String LENGTH_OF_PIECES_DEFAULT = "-1";

  private static BatchRunTomoDatasetDialog GLOBAL_INSTANCE = null;

  private final JPanel pnlRoot = new JPanel();
  private final CheckBox cbRemoveXrays = new CheckBox("Remove X-rays");
  private final CheckBox cbEnableStretching =
      new CheckBox("Enable distortion (stretching) in alignment");
  private final CheckBox cbLocalAlignments = new CheckBox("Use local alignments");
  private final MultiLineButton btnModelFile = new MultiLineButton("Make in 3dmod");
  private final ButtonGroup bgTrackingMethod = new ButtonGroup();
  private final RadioButton rbTrackingMethodSeed =
      new RadioButton("Autoseed and track", TrackingMethod.SEED, bgTrackingMethod);
  private final RadioButton rbTrackingMethodRaptor =
      new RadioButton("Raptor and track", TrackingMethod.RAPTOR, bgTrackingMethod);
  private final RadioButton rbTrackingMethodPatchTracking =
      new RadioButton("Patch tracking", TrackingMethod.PATCH_TRACKING, bgTrackingMethod);
  private final RadioButton rbFiducialless =
      new RadioButton("Fiducialless", TrackingMethod.PATCH_TRACKING, bgTrackingMethod);
  private final LabeledTextField ltfGold =
      new LabeledTextField(FieldType.FLOATING_POINT, "Bead size (nm): ");
  private final LabeledTextField ltfLocalAreaTargetSize =
      new LabeledTextField(FieldType.INTEGER_PAIR, "Local tracking area size: ");
  private final LabeledTextField ltfTargetNumberOfBeads =
      new LabeledTextField(FieldType.INTEGER, "Target number of beads: ");
  private final LabeledTextField ltfSizeOfPatchesXandY =
      new LabeledTextField(FieldType.INTEGER_PAIR, "Patch tracking size: ");
  private final CheckBox cbLengthOfPieces = new CheckBox("Break contours into pieces: ");
  private final LabeledSpinner lsBinByFactor =
      LabeledSpinner.getInstance("Aligned stack binning: ", 1, 1, 8, 1);
  private final CheckBox cbCorrectCTF = new CheckBox("Correct CTF");
  private final LabeledTextField ltfDefocus =
      new LabeledTextField(FieldType.INTEGER_PAIR, "Defocus: ");
  private final ButtonGroup bgAutofit = new ButtonGroup();
  private final RadioButton rbFitEveryImage =
      new RadioButton("Fit every image", bgAutofit);
  private final RadioTextField rtfAutoFitRangeAndStep =
      RadioTextField.getInstance(FieldType.FLOATING_POINT, "Autofit range ", bgAutofit);
  private final LabeledTextField ltfAutoFitStep =
      new LabeledTextField(FieldType.FLOATING_POINT, " and step ");
  private final ButtonGroup bgUseSirt = new ButtonGroup();
  private final RadioButton rbUseSirtFalse =
      new RadioButton("Back-projection", bgUseSirt);
  private final RadioButton rbUseSirtTrue = new RadioButton("SIRT", bgUseSirt);
  private final RadioButton rbDoBackprojAlso = new RadioButton("Both", bgUseSirt);
  private final LabeledTextField ltfLeaveIterations =
      new LabeledTextField(FieldType.STRING, "Leave iterations: ");
  private final CheckBox cbScaleToInteger = new CheckBox("Scale to integers");
  private final ButtonGroup bgThickness = new ButtonGroup();
  private final RadioTextField rtfThickness = RadioTextField
      .getInstance(FieldType.INTEGER, "Thickness total (unbinned pixels): ", bgThickness);
  private final RadioTextField rtfBinnedThickness = RadioTextField
      .getInstance(FieldType.INTEGER, "Thickness total   (binned pixels): ", bgThickness);
  private final RadioButton rbDeriveThickness =
      new RadioButton(DERIVE_THICKNESS_LABEL, bgThickness);
  private final TextField tfExtraThickness =
      new TextField(FieldType.INTEGER, DERIVE_THICKNESS_LABEL, null);
  private final LabeledTextField ltfFallbackThickness =
      new LabeledTextField(FieldType.INTEGER, "with fallback (unbinned pixels): ");
  private final List<Field> fieldList = new ArrayList<Field>();
  private final MultiLineButton btnOk = new MultiLineButton("OK");
  private final MultiLineButton btnRevertToGlobal =
      new MultiLineButton("Revert to Global");
  private final JPanel pnlRootBody = new JPanel();
  private final Spacer spaceModelFile = new Spacer(FixedDim.x5_y0);

  private final FileTextField2 ftfDistort;
  private final FileTextField2 ftfGradient;
  private final FileTextField2 ftfModelFile;
  private final JDialog dialog;
  private final PanelHeader phRootHeader;
  private final BaseManager manager;
  private final File datasetFile;
  private final BatchRunTomoRow row;

  private String lengthOfPieces = LENGTH_OF_PIECES_DEFAULT;

  private BatchRunTomoDatasetDialog(final BaseManager manager, final File datasetFile,
      final boolean global, final BatchRunTomoRow row) {
    this.manager = manager;
    this.datasetFile = datasetFile;
    this.row = row;
    ftfDistort = FileTextField2.getAltLayoutInstance(manager, "Image distortion file: ");
    ftfGradient = FileTextField2.getAltLayoutInstance(manager, "Mag gradient file: ");
    ftfModelFile =
        FileTextField2.getAltLayoutInstance(manager, "Manual replacement model: ");

    if (global) {
      dialog = null;
      phRootHeader = PanelHeader
          .getInstance("Global Dataset Values", this, DialogType.BATCH_RUN_TOMO);
    }
    else {
      dialog = new JDialog();
      phRootHeader = null;
    }
  }

  static BatchRunTomoDatasetDialog getGlobalInstance(final BaseManager manager) {
    if (GLOBAL_INSTANCE != null) {
      return GLOBAL_INSTANCE;
    }
    BatchRunTomoDatasetDialog instance =
        new BatchRunTomoDatasetDialog(manager, null, true, null);
    instance.createPanel();
    instance.addListeners();
    GLOBAL_INSTANCE = instance;
    return instance;
  }

  static BatchRunTomoDatasetDialog getGlobalInstance() {
    return GLOBAL_INSTANCE;
  }

  static BatchRunTomoDatasetDialog getRowInstance(final BaseManager manager,
      final File datasetFile, final BatchRunTomoRow row) {
    BatchRunTomoDatasetDialog instance =
        new BatchRunTomoDatasetDialog(manager, datasetFile, false, row);
    instance.createPanel();
    instance.copyFromGlobal();
    instance.addListeners();
    instance.setVisible(true);
    return instance;
  }

  static BatchRunTomoDatasetDialog getSavedInstance(final BaseManager manager,
      final BatchRunTomoRow row) {
    BatchRunTomoDatasetDialog instance =
        new BatchRunTomoDatasetDialog(manager, null, false, row);
    instance.createPanel();
    instance.copyFromGlobal();
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
    JPanel pnlRemoveXrays = new JPanel();
    JPanel pnlEnableStretching = new JPanel();
    JPanel pnlLocalAlignments = new JPanel();
    // init
    ftfGradient.setPreferredWidth(272);
    btnModelFile.setToPreferredSize();
    btnRevertToGlobal.setToPreferredSize();
    btnOk.setToPreferredSize();
    ftfDistort.setOrigin(ConfigTool.getDistortionDir(manager, null));
    ftfGradient.setOrigin(ConfigTool.getDistortionDir(manager, null));
    // Set directive defs where there is a one-to-one correspondence between field and
    // directive. The directive def is used for setting the default in comparam
    // directives, and saving fields to autodoc files.
    //
    // IMPORTANT: Code currently assumes that all defaults (for comparam directive fields)
    // can be set generically.
    ftfDistort.setDirectiveDef(DirectiveDef.DISTORT);
    ftfGradient.setDirectiveDef(DirectiveDef.GRADIENT);
    cbRemoveXrays.setDirectiveDef(DirectiveDef.REMOVE_XRAYS);
    ftfModelFile.setDirectiveDef(DirectiveDef.MODEL_FILE);
    rbTrackingMethodSeed.setDirectiveDef(DirectiveDef.TRACKING_METHOD);// also affects
    // seedingMethod
    rbTrackingMethodRaptor.setDirectiveDef(DirectiveDef.TRACKING_METHOD);
    rbTrackingMethodPatchTracking.setDirectiveDef(DirectiveDef.TRACKING_METHOD);
    rbFiducialless.setDirectiveDef(DirectiveDef.FIDUCIALLESS);
    ltfGold.setDirectiveDef(DirectiveDef.GOLD);
    ltfLocalAreaTargetSize.setDirectiveDef(DirectiveDef.LOCAL_AREA_TARGET_SIZE);
    ltfTargetNumberOfBeads.setDirectiveDef(DirectiveDef.TARGET_NUMBER_OF_BEADS);
    ltfSizeOfPatchesXandY.setDirectiveDef(DirectiveDef.SIZE_OF_PATCHES_X_AND_Y);
    cbLengthOfPieces.setDirectiveDef(DirectiveDef.LENGTH_OF_PIECES);
    cbEnableStretching.setDirectiveDef(DirectiveDef.ENABLE_STRETCHING);
    cbLocalAlignments.setDirectiveDef(DirectiveDef.LOCAL_ALIGNMENTS);
    lsBinByFactor.setDirectiveDef(DirectiveDef.BIN_BY_FACTOR_FOR_ALIGNED_STACK);
    cbCorrectCTF.setDirectiveDef(DirectiveDef.CORRECT_CTF);
    ltfDefocus.setDirectiveDef(DirectiveDef.DEFOCUS);
    rtfAutoFitRangeAndStep.setDirectiveDef(DirectiveDef.AUTO_FIT_RANGE_AND_STEP);
    ltfAutoFitStep.setDirectiveDef(DirectiveDef.AUTO_FIT_RANGE_AND_STEP);
    rbFitEveryImage.setDirectiveDef(DirectiveDef.AUTO_FIT_RANGE_AND_STEP);
    rbUseSirtFalse.setDirectiveDef(DirectiveDef.USE_SIRT);
    rbUseSirtTrue.setDirectiveDef(DirectiveDef.USE_SIRT);
    rbDoBackprojAlso.setDirectiveDef(DirectiveDef.DO_BACKPROJ_ALSO);// also associated
    // with USE_SIRT
    ltfLeaveIterations.setDirectiveDef(DirectiveDef.LEAVE_ITERATIONS);
    cbScaleToInteger.setDirectiveDef(DirectiveDef.SCALE_TO_INTEGER);
    rtfThickness.setDirectiveDef(DirectiveDef.THICKNESS);
    rtfBinnedThickness.setDirectiveDef(DirectiveDef.BINNED_THICKNESS);
    // rbDeriveThickness is based on multiple directives
    tfExtraThickness.setDirectiveDef(DirectiveDef.EXTRA_THICKNESS);
    ltfFallbackThickness.setDirectiveDef(DirectiveDef.FALLBACK_THICKNESS);
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
    fieldList.add(cbLengthOfPieces);
    fieldList.add(cbEnableStretching);
    fieldList.add(cbLocalAlignments);
    fieldList.add(lsBinByFactor);
    fieldList.add(cbCorrectCTF);
    fieldList.add(ltfDefocus);
    fieldList.add(rtfAutoFitRangeAndStep);
    fieldList.add(ltfAutoFitStep);
    fieldList.add(rbFitEveryImage);
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
      int titleLen = 48;
      int keyLen = key.length();
      if (dialog != null) {
        if (keyLen <= titleLen) {
          dialog.setTitle(key);
        }
        else {
          dialog.setTitle("..." + key.substring(keyLen - titleLen - 3));
        }
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
    pnlModelFile.add(spaceModelFile.getComponent());
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
    pnlSizeOfPatchesXandY.add(cbLengthOfPieces);
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
    pnlAutoFitRangeAndStep
        .setLayout(new BoxLayout(pnlAutoFitRangeAndStep, BoxLayout.X_AXIS));
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

  int getPreferredWidth() {
    return ftfModelFile.getPreferredWidth() + spaceModelFile.getPreferredWidth() +
        btnModelFile.getPreferredWidth();
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
    btnRevertToGlobal.addActionListener(this);
  }

  public Component getComponent() {
    return pnlRoot;
  }

  public SwingComponent getUIComponent() {
    return this;
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
    boolean beadTracking =
        rbTrackingMethodSeed.isSelected() || rbTrackingMethodRaptor.isSelected();
    ltfGold.setEnabled(beadTracking);
    ltfTargetNumberOfBeads.setEnabled(beadTracking);
    ltfLocalAreaTargetSize.setEnabled(beadTracking);
    boolean patchTracking = rbTrackingMethodPatchTracking.isSelected();
    ltfSizeOfPatchesXandY.setEnabled(patchTracking);
    cbLengthOfPieces.setEnabled(patchTracking);
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
   *
   * @return true if any field's isDifferentFromCheckpoint function returned true
   */
  boolean backupIfChanged() {
    boolean changed = false;
    int len = fieldList.size();
    for (int i = 0; i < len; i++) {
      Field field = fieldList.get(i);
      if (field.isDifferentFromCheckpoint(true)) {
        field.backup();
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Called when the template has changed
   *
   * @param directiveFileCollection - templates and base directive file
   * @param retainUserValues        - put the backed-up values back after values have
   *                                been applied
   */
  void applyValues(final DirectiveFileCollection directiveFileCollection,
      final boolean retainUserValues) {
    // to apply values and highlights, start with a clean slate
    int len = fieldList.size();
    for (int i = 0; i < len; i++) {
      Field field = fieldList.get(i);
      field.clear();
      field.clearFieldHighlight();
    }
    // Apply default values
    setDefaults();
    len = fieldList.size();
    for (int i = 0; i < len; i++) {
      fieldList.get(i).
          useDefaultValue();
    }
    // No settings values to apply
    // Apply the directive collection values
    setValues(directiveFileCollection);
    len = fieldList.size();
    for (int i = 0; i < len; i++) {
      Field field = fieldList.get(i);
      // checkpoint
      field.checkpoint();
      // If the user wants to retain their values, apply backed up values and then
      // delete
      // them.
      if (retainUserValues) {
        field.restoreFromBackup();
      }
    }
    // Set new highlight values - batch directive file must be ignored
    setFieldHighlightValues(directiveFileCollection);
  }

  private void copyFromGlobal() {
    if (GLOBAL_INSTANCE == null) {
      return;
    }
    int len = fieldList.size();
    int lenGlobal = GLOBAL_INSTANCE.fieldList.size();
    for (int i = 0; i < len; i++) {
      if (i >= lenGlobal) {
        break;
      }
      Field field = fieldList.get(i);
      Field globalField = GLOBAL_INSTANCE.fieldList.get(i);
      field.setValue(globalField);
      field.setCheckpoint(globalField.getCheckpoint());
      field.setFieldHighlight(globalField.getFieldHighlight());
    }
    updateDisplay();
  }

  private void setDefaults() {
    rbUseSirtFalse.setSelected(true);
  }

  public void setParameters(final BatchRunTomoDatasetMetaData metaData) {
    if (phRootHeader != null) {
      phRootHeader.set(metaData.getHeader());
    }
    ftfModelFile.setText(metaData.getModelFile());
    cbEnableStretching.setSelected(metaData.isEnableStretching());
    cbLocalAlignments.setSelected(metaData.isLocalAlignments());
    ltfGold.setText(metaData.getGold());
    ltfTargetNumberOfBeads.setText(metaData.getTargetNumberOfBeads());
    ltfLocalAreaTargetSize.setText(metaData.getLocalAreaTargetSize());
    ltfSizeOfPatchesXandY.setText(metaData.getSizeOfPatchesXandY());
    cbLengthOfPieces.setSelected(metaData.isLengthOfPieces());
    ltfDefocus.setText(metaData.getDefocus());
    rtfAutoFitRangeAndStep.setSelected(metaData.isAutoFitRangeAndStep());
    rtfAutoFitRangeAndStep.setText(metaData.getAutoFitRange());
    rbFitEveryImage.setSelected(metaData.isFitEveryImage());
    ltfAutoFitStep.setText(metaData.getAutoFitStep());
    ltfLeaveIterations.setText(metaData.getLeaveIterations());
    cbScaleToInteger.setSelected(metaData.isScaleToInteger());
    rtfThickness.setText(metaData.getThickness());
    rtfBinnedThickness.setText(metaData.getBinnedThickness());
    tfExtraThickness.setText(metaData.getExtraThickness());
    ltfFallbackThickness.setText(metaData.getFallbackThickness());
  }

  public void getParameters(final BatchRunTomoDatasetMetaData metaData) {
    if (phRootHeader != null) {
      metaData.setHeader(phRootHeader);
    }
    metaData.setModelFile(ftfModelFile.getFile());
    metaData.setEnableStretching(cbEnableStretching.isSelected());
    metaData.setLocalAlignments(cbLocalAlignments.isSelected());
    metaData.setGold(ltfGold.getText());
    metaData.setTargetNumberOfBeads(ltfTargetNumberOfBeads.getText());
    metaData.setLocalAreaTargetSize(ltfLocalAreaTargetSize.getText());
    metaData.setSizeOfPatchesXandY(ltfSizeOfPatchesXandY.getText());
    metaData.setLengthOfPieces(cbLengthOfPieces.isSelected());
    metaData.setDefocus(ltfDefocus.getText());
    metaData.setAutoFitRangeAndStep(rtfAutoFitRangeAndStep.isSelected());
    metaData.setAutoFitRange(rtfAutoFitRangeAndStep.getText());
    metaData.setFitEveryImage(rbFitEveryImage.isSelected());
    metaData.setAutoFitStep(ltfAutoFitStep.getText());
    metaData.setLeaveIterations(ltfLeaveIterations.getText());
    metaData.setScaleToInteger(cbScaleToInteger.isSelected());
    metaData.setThickness(rtfThickness.getText());
    metaData.setBinnedThickness(rtfBinnedThickness.getText());
    metaData.setExtraThickness(tfExtraThickness.getText());
    metaData.setFallbackThickness(ltfFallbackThickness.getText());
  }

  /**
   * Set values from the directive file collection.  Only change fields that exist in
   * directive file collection.
   *
   * @param autodoc - a writable autodoc
   */
  void saveAutodoc(final WritableAutodoc autodoc) {
    if (autodoc == null) {
      return;
    }
    BatchTool.saveAutodoc(ftfDistort, autodoc);
    BatchTool.saveAutodoc(ftfGradient, autodoc);
    BatchTool.saveAutodoc(cbRemoveXrays, autodoc);
    BatchTool.saveAutodoc(ftfModelFile, autodoc);
    if (rbTrackingMethodSeed.isSelected()) {
      if (rbTrackingMethodSeed.isEnabled() &&
          BatchTool.needInAutodoc(rbTrackingMethodSeed)) {
        autodoc.addNameValuePair(DirectiveDef.TRACKING_METHOD.getDirective(null, null),
            TrackingMethod.SEED.getValue().toString());
        autodoc.addNameValuePair(DirectiveDef.SEEDING_METHOD.getDirective(null, null),
            SeedingMethod.BOTH.getValue());
      }
    }
    else if (rbTrackingMethodRaptor.isSelected()) {
      if (rbTrackingMethodRaptor.isEnabled() &&
          BatchTool.needInAutodoc(rbTrackingMethodRaptor)) {
        autodoc.addNameValuePair(DirectiveDef.TRACKING_METHOD.getDirective(null, null),
            TrackingMethod.RAPTOR.getValue().toString());
      }
    }
    else if (rbTrackingMethodPatchTracking.isEnabled() &&
        rbTrackingMethodPatchTracking.isSelected()) {
      if (BatchTool.needInAutodoc(rbTrackingMethodPatchTracking)) {
        autodoc.addNameValuePair(DirectiveDef.TRACKING_METHOD.getDirective(null, null),
            TrackingMethod.PATCH_TRACKING.getValue().toString());
      }
    }
    BatchTool.saveAutodoc(rbFiducialless, autodoc);
    BatchTool.saveAutodoc(ltfGold, autodoc);
    BatchTool.saveAutodoc(ltfLocalAreaTargetSize, autodoc);
    BatchTool.saveAutodoc(ltfTargetNumberOfBeads, autodoc);
    BatchTool.saveAutodoc(ltfSizeOfPatchesXandY, autodoc);
    if (cbLengthOfPieces.isSelected()) {
      boolean add = true;
      if (cbLengthOfPieces.isFieldHighlightSet()) {
        add = !cbLengthOfPieces.equalsFieldHighlight(lengthOfPieces);
      }
      else {
        add = !cbLengthOfPieces.equalsDefaultValue(lengthOfPieces);
      }
      if (add) {
        autodoc
            .addNameValuePair(cbLengthOfPieces.getDirectiveDef().getDirective(null, null),
                lengthOfPieces);
      }
    }
    BatchTool.saveAutodoc(cbEnableStretching, autodoc);
    BatchTool.saveAutodoc(cbLocalAlignments, autodoc);
    BatchTool.saveAutodoc(lsBinByFactor, autodoc);
    BatchTool.saveAutodoc(cbCorrectCTF, autodoc);
    BatchTool.saveAutodoc(ltfDefocus, autodoc);
    if (rtfAutoFitRangeAndStep.isSelected())

    {
      if (rbTrackingMethodSeed.isEnabled() &&
          BatchTool.needInAutodoc(rbTrackingMethodSeed)) {
        autodoc.addNameValuePair(
            DirectiveDef.AUTO_FIT_RANGE_AND_STEP.getDirective(null, null),
            rtfAutoFitRangeAndStep.getText() + "," + ltfAutoFitStep.getText());
      }
    }

    else if (rbFitEveryImage.isSelected())

    {
      if (rbFitEveryImage.isEnabled() && BatchTool.needInAutodoc(rbFitEveryImage)) {
        autodoc.addNameValuePair(
            DirectiveDef.AUTO_FIT_RANGE_AND_STEP.getDirective(null, null), "0,0");
      }
    }

    if (rbUseSirtFalse.isSelected())

    {
      if (rbUseSirtFalse.isEnabled() && BatchTool.needInAutodoc(rbUseSirtFalse)) {
        autodoc.addNameValuePair(DirectiveDef.USE_SIRT.getDirective(null, null), "0");
      }
    }

    else if (!BatchTool.saveAutodoc(rbUseSirtTrue, autodoc))

    {
      if (BatchTool.saveAutodoc(rbDoBackprojAlso, autodoc)) {
        // Don't add useSirt if it is the default or in the templates
        if (!rbUseSirtTrue.equalsFieldHighlight(true) &&
            !rbUseSirtTrue.equalsDefaultValue(true)) {
          autodoc.addNameValuePair(DirectiveDef.USE_SIRT.getDirective(null, null), "1");
        }
      }
    }

    BatchTool.saveAutodoc(ltfLeaveIterations, autodoc);
    BatchTool.saveAutodoc(cbScaleToInteger, autodoc);
    BatchTool.saveAutodoc(tfExtraThickness, autodoc);
    BatchTool.saveAutodoc(ltfFallbackThickness, autodoc);
    BatchTool.saveAutodoc(rtfThickness, autodoc);
    BatchTool.saveAutodoc(rtfBinnedThickness, autodoc);
  }

  void setValues(final DirectiveFileInterface directiveFiles) {
    setValues(directiveFiles, false);
  }

  private void setFieldHighlightValues(final DirectiveFileInterface directiveFiles) {
    setValues(directiveFiles, true);
  }

  /**
   * Set values from the directive file collection.  Only change fields that exist in
   * directive file collection.
   *
   * @param directiveFiles - templates and base directive file, or a single directive file
   */
  private void setValues(final DirectiveFileInterface directiveFiles,
      final boolean setFieldHighlightValue) {
    setValue(ftfDistort, directiveFiles, setFieldHighlightValue);
    setValue(ftfGradient, directiveFiles, setFieldHighlightValue);
    setValue(cbRemoveXrays, directiveFiles, setFieldHighlightValue);
    setValue(ftfModelFile, directiveFiles, setFieldHighlightValue);
    if (directiveFiles.contains(DirectiveDef.TRACKING_METHOD, setFieldHighlightValue)) {
      TrackingMethod trackingMethod = TrackingMethod
          .getInstance(directiveFiles.getValue(DirectiveDef.TRACKING_METHOD));
      if (trackingMethod == TrackingMethod.SEED) {
        if (directiveFiles
            .contains(DirectiveDef.SEEDING_METHOD, setFieldHighlightValue)) {
          SeedingMethod seedingMethod = SeedingMethod
              .getInstance(directiveFiles.getValue(DirectiveDef.SEEDING_METHOD));
          if (seedingMethod == SeedingMethod.AUTO_FID_SEED ||
              seedingMethod == SeedingMethod.BOTH) {
            if (!setFieldHighlightValue) {
              rbTrackingMethodSeed.setSelected(true);
            }
            else {
              rbTrackingMethodSeed.setFieldHighlight(true);
            }
          }
        }
      }
      else if (trackingMethod == TrackingMethod.RAPTOR) {
        if (!setFieldHighlightValue) {
          rbTrackingMethodRaptor.setSelected(true);
        }
        else {
          rbTrackingMethodRaptor.setFieldHighlight(true);
        }
      }
      else if (trackingMethod == TrackingMethod.PATCH_TRACKING) {
        if (!setFieldHighlightValue) {
          rbTrackingMethodPatchTracking.setSelected(true);
        }
        else {
          rbTrackingMethodPatchTracking.setFieldHighlight(true);
        }
      }
    }
    setValue(rbFiducialless, directiveFiles, setFieldHighlightValue);
    setValue(ltfGold, directiveFiles, setFieldHighlightValue);
    setValue(ltfLocalAreaTargetSize, directiveFiles, setFieldHighlightValue);
    setValue(ltfTargetNumberOfBeads, directiveFiles, setFieldHighlightValue);
    setValue(ltfSizeOfPatchesXandY, directiveFiles, setFieldHighlightValue);
    //LengthOfPieces is an integer directive, but it is being placed in a checkbox.  The
    //real text value should be saved.  If it is from the starting batch directive, it
    //must be added to the autodoc.
    if (directiveFiles
        .contains(cbLengthOfPieces.getDirectiveDef(), setFieldHighlightValue)) {
      //LengthOfPieces exists and is not overridden.
      lengthOfPieces = directiveFiles
          .getValue(cbLengthOfPieces.getDirectiveDef(), setFieldHighlightValue);
      if (!setFieldHighlightValue) {
        cbLengthOfPieces.setSelected(BooleanFieldSetting.stringToBoolean(lengthOfPieces));
      }
      else {
        cbLengthOfPieces.setFieldHighlight(lengthOfPieces);
      }
      //Make sure that lengthOfPieces has a value.  This may never happen
      //because an integer directive without a value is overriding matching directives in
      //previous templates, and will cause contains() to return false.
      if (lengthOfPieces == null) {
        lengthOfPieces = LENGTH_OF_PIECES_DEFAULT;
      }
    }
    setValue(cbEnableStretching, directiveFiles, setFieldHighlightValue);
    setValue(cbLocalAlignments, directiveFiles, setFieldHighlightValue);
    setValue(lsBinByFactor, directiveFiles, setFieldHighlightValue);
    setValue(cbCorrectCTF, directiveFiles, setFieldHighlightValue);
    setValue(ltfDefocus, directiveFiles, setFieldHighlightValue);
    if (directiveFiles
        .contains(DirectiveDef.AUTO_FIT_RANGE_AND_STEP, setFieldHighlightValue)) {
      EtomoNumber step = new EtomoNumber(EtomoNumber.Type.DOUBLE);
      step.set(directiveFiles.getValue(DirectiveDef.AUTO_FIT_RANGE_AND_STEP,
          DirectiveFile.AUTO_FIT_STEP_INDEX));
      if (step.equals(0)) {
        if (!setFieldHighlightValue) {
          rbFitEveryImage.setSelected(true);
        }
        else {
          rbFitEveryImage.setFieldHighlight(true);
        }
      }
      else {
        String range = directiveFiles.getValue(DirectiveDef.AUTO_FIT_RANGE_AND_STEP,
            DirectiveFile.AUTO_FIT_RANGE_INDEX);
        if (!setFieldHighlightValue) {
          rtfAutoFitRangeAndStep.setSelected(true);
          rtfAutoFitRangeAndStep.setText(range);
          ltfAutoFitStep.setText(step.toString());
        }
        else {
          rtfAutoFitRangeAndStep.setFieldHighlight(true);
          rtfAutoFitRangeAndStep.setFieldHighlight(range);
          ltfAutoFitStep.setFieldHighlight(step.toString());
        }
      }
    }
    boolean useSirt = false;
    boolean containsUseSirt = false;
    if (directiveFiles.contains(DirectiveDef.USE_SIRT, setFieldHighlightValue)) {
      containsUseSirt = true;
      useSirt = directiveFiles.isValue(DirectiveDef.USE_SIRT, setFieldHighlightValue);
    }
    boolean doBackprojAlso = false;
    boolean containsDoBackprojAlso = false;
    if (directiveFiles.contains(DirectiveDef.DO_BACKPROJ_ALSO, setFieldHighlightValue)) {
      containsDoBackprojAlso = true;
      doBackprojAlso =
          directiveFiles.isValue(DirectiveDef.DO_BACKPROJ_ALSO, setFieldHighlightValue);
    }
    // Set values based on useSirt doBackProjAlso booleans.
    if (useSirt && doBackprojAlso) {
      if (!setFieldHighlightValue) {
        rbDoBackprojAlso.setSelected(true);
      }
      else {
        rbDoBackprojAlso.setFieldHighlight(true);
      }
    }
    else if (useSirt) {
      if (!setFieldHighlightValue) {
        rbUseSirtTrue.setSelected(true);
      }
      else {
        rbUseSirtTrue.setFieldHighlight(true);
      }
      if (containsDoBackprojAlso) {
        rbDoBackprojAlso.setFieldHighlight(false);
      }
    }
    else {
      rbUseSirtFalse.setSelected(true);
      if (containsUseSirt && setFieldHighlightValue) {
        rbUseSirtTrue.setFieldHighlight(false);
      }
    }
    setValue(ltfLeaveIterations, directiveFiles, setFieldHighlightValue);
    setValue(cbScaleToInteger, directiveFiles, setFieldHighlightValue);
    // Derive thickness from log when thickness is not specified.
    // Priority of directives
    // 1. THICKNESS
    // 2. binnedThickness
    // 3. fallbackThickness (causes derived thickness to be checked)
    boolean fallbackThickness =
        setValue(ltfFallbackThickness, directiveFiles, setFieldHighlightValue);
    boolean extraThickness =
        setValue(tfExtraThickness, directiveFiles, setFieldHighlightValue);
    boolean binnedThickness =
        setValue(rtfBinnedThickness, directiveFiles, setFieldHighlightValue);
    boolean thickness = setValue(rtfThickness, directiveFiles, setFieldHighlightValue);
    // Make sure the derive thickness radio button is checked or its the field highlight
    // is set.
    if (!thickness && !binnedThickness) {
      if (!setFieldHighlightValue) {
        rbDeriveThickness.setSelected(true);
      }
      else if (fallbackThickness || extraThickness) {
        rbDeriveThickness.setFieldHighlight(true);
      }
    }
    updateDisplay();
  }

  private boolean setValue(final Field field, final DirectiveFileInterface directiveFiles,
      final boolean setFieldHighlightValue) {
    DirectiveDef directiveDef = field.getDirectiveDef();
    if (directiveFiles.contains(directiveDef, setFieldHighlightValue)) {
      if (field.isText()) {
        String value = directiveFiles.getValue(directiveDef, setFieldHighlightValue);
        setValue(field, value, setFieldHighlightValue);
        if (field.isBoolean()) {
          setValue(field, true, setFieldHighlightValue);
        }
      }
      else {
        boolean value = directiveFiles.isValue(directiveDef, setFieldHighlightValue);
        setValue(field, value, setFieldHighlightValue);
      }
      directiveFiles.setDebug(false);
      return true;
    }
    directiveFiles.setDebug(false);
    return false;
  }

  private void setValue(final Field field, final String value,
      final boolean setFieldHighlightValue) {
    if (!setFieldHighlightValue) {
      field.setValue(value);
    }
    else {
      field.setFieldHighlight(value);
    }
  }

  private void setValue(final Field field, final boolean value,
      final boolean setFieldHighlightValue) {
    if (!setFieldHighlightValue) {
      field.setValue(value);
    }
    else {
      field.setFieldHighlight(value);
    }
  }

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (actionCommand.equals(btnOk.getActionCommand())) {
      dialog.setVisible(false);
    }
    if (actionCommand.equals(btnRevertToGlobal.getActionCommand())) {
      if (UIHarness.INSTANCE.openYesNoDialog(this,
          "Data in this window will be lost.  Revert to global dataset data for this " +
              "stack?")) {
        dialog.setVisible(false);
        if (row != null) {
          row.deleteDataset();
        }
      }
    }
    else {
      updateDisplay();
    }
  }
}
