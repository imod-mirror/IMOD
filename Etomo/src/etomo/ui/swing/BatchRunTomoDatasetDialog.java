package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import etomo.logic.ConfigTool;
import etomo.logic.SeedingMethod;
import etomo.logic.TrackingMethod;
import etomo.process.BaseProcessManager;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.LogFile;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.BatchRunTomoDatasetMetaData;
import etomo.type.DataFileType;
import etomo.type.DialogType;
import etomo.type.EtomoNumber;
import etomo.type.FileType;
import etomo.type.UserConfiguration;
import etomo.ui.Field;
import etomo.ui.FieldType;

/**
* <p>Description: Contains parameters that are dataset values.  Can be used for all
* datasets and individual ones. </p>
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
  private final Spacer spaceModelFile = new Spacer(FixedDim.x5_y0);

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
      phRootHeader = PanelHeader.getInstance("Global Dataset Values", this,
          DialogType.BATCH_RUN_TOMO);
    }
    else {
      dialog = new JDialog();
      phRootHeader = null;
    }
  }

  static BatchRunTomoDatasetDialog getGlobalInstance(final BaseManager manager) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager, null,
        true);
    instance.createPanel();
    instance.addListeners();
    GLOBAL_INSTANCE = instance;
    return instance;
  }

  static BatchRunTomoDatasetDialog getRowInstance(final BaseManager manager,
      final File datasetFile) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager,
        datasetFile, false);
    instance.createPanel();
    instance.copyFromGlobal();
    instance.addListeners();
    instance.setVisible(true);
    return instance;
  }

  static BatchRunTomoDatasetDialog getSavedInstance(final BaseManager manager) {
    BatchRunTomoDatasetDialog instance = new BatchRunTomoDatasetDialog(manager, null,
        false);
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
    lsContourPieces.setDirectiveDef(DirectiveDef.CONTOUR_PIECES);
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
    fieldList.add(lsContourPieces);
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

  int getPreferredWidth() {
    return ftfModelFile.getPreferredWidth() + spaceModelFile.getPreferredWidth()
        + btnModelFile.getPreferredWidth();
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
   * Called when the template has changed
   * @param userConfiguration
   * @param directiveFileCollection
   * @param retainUserValues
   */
  void applyValues(final UserConfiguration userConfiguration,
      final DirectiveFileCollection directiveFileCollection,
      final boolean retainUserValues) {
    // to apply values and highlights, start with a clean slate
    Iterator<Field> iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        Field field = iterator.next();
        field.clear();
        field.clearFieldHighlight();
      }
    }
    // Apply default values
    setDefaults();
    iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        iterator.next().useDefaultValue();
      }
    }
    // No settings values to apply
    // Apply the directive collection values
    setValues(directiveFileCollection);
    iterator = fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        Field field = iterator.next();
        // checkpoint
        field.checkpoint();
        // If the user wants to retain their values, apply backed up values and then
        // delete
        // them.
        if (retainUserValues) {
          field.restoreFromBackup();
        }
      }
    }
    // Set new highlight values - batch directive file must be ignored
    setFieldHighlightValues(directiveFileCollection);
  }

  private void copyFromGlobal() {
    if (GLOBAL_INSTANCE == null) {
      return;
    }
    Iterator<Field> iterator = fieldList.iterator();
    Iterator<Field> globalIterator = GLOBAL_INSTANCE.fieldList.iterator();
    if (iterator != null) {
      while (iterator.hasNext()) {
        Field field = iterator.next();
        Field globalField = globalIterator.next();
        field.setValue(globalField);
        field.setCheckpoint(globalField.getCheckpoint());
        field.setFieldHighlight(globalField.getFieldHighlight());
      }
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
    lsContourPieces.setValue(metaData.getContourPieces());
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
    metaData.setContourPieces(lsContourPieces.getValue());
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
   * @param directiveFileCollection
   */
  void saveAutodoc(final FileType fileType) {
    if (!fileType.exists(manager, null)) {
      BaseProcessManager.touch(fileType.getFile(manager, null).getAbsolutePath(), null);
    }
    try {
      saveAutodoc(AutodocFactory.getWritableInstance(manager,
          fileType.getFile(manager, null), false));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
  }

  void saveAutodoc(final WritableAutodoc autodoc) {
    if (autodoc == null) {
      return;
    }
    try {
      saveAutodoc(ftfDistort, autodoc);
      saveAutodoc(ftfGradient, autodoc);
      saveAutodoc(cbRemoveXrays, autodoc);
      saveAutodoc(ftfModelFile, autodoc);
      if (rbTrackingMethodSeed.isSelected()) {
        if (rbTrackingMethodSeed.isEnabled() && needInAutodoc(rbTrackingMethodSeed)) {
          autodoc.addNameValuePair(DirectiveDef.TRACKING_METHOD.getDirective(null, null),
              TrackingMethod.SEED.getValue().toString());
          autodoc.addNameValuePair(DirectiveDef.SEEDING_METHOD.getDirective(null, null),
              SeedingMethod.BOTH.getValue());
        }
      }
      else if (rbTrackingMethodRaptor.isSelected()) {
        if (rbTrackingMethodRaptor.isEnabled() && needInAutodoc(rbTrackingMethodRaptor)) {
          autodoc.addNameValuePair(DirectiveDef.TRACKING_METHOD.getDirective(null, null),
              TrackingMethod.RAPTOR.getValue().toString());
        }
      }
      else if (rbTrackingMethodPatchTracking.isEnabled()
          && rbTrackingMethodPatchTracking.isSelected()) {
        if (rbTrackingMethodPatchTracking.isEnabled()
            && needInAutodoc(rbTrackingMethodPatchTracking)) {
          autodoc.addNameValuePair(DirectiveDef.TRACKING_METHOD.getDirective(null, null),
              TrackingMethod.PATCH_TRACKING.getValue().toString());
        }
      }
      saveAutodoc(rbFiducialless, autodoc);
      saveAutodoc(ltfGold, autodoc);
      saveAutodoc(ltfLocalAreaTargetSize, autodoc);
      saveAutodoc(ltfTargetNumberOfBeads, autodoc);
      saveAutodoc(ltfSizeOfPatchesXandY, autodoc);
      saveAutodoc(lsContourPieces, autodoc);
      saveAutodoc(cbEnableStretching, autodoc);
      saveAutodoc(cbLocalAlignments, autodoc);
      saveAutodoc(lsBinByFactor, autodoc);
      saveAutodoc(cbCorrectCTF, autodoc);
      saveAutodoc(ltfDefocus, autodoc);
      if (rtfAutoFitRangeAndStep.isSelected()) {
        if (rbTrackingMethodSeed.isEnabled() && needInAutodoc(rbTrackingMethodSeed)) {
          autodoc.addNameValuePair(
              DirectiveDef.AUTO_FIT_RANGE_AND_STEP.getDirective(null, null),
              rtfAutoFitRangeAndStep.getText() + "," + ltfAutoFitStep.getText());
        }
      }
      else if (rbFitEveryImage.isSelected()) {
        if (rbFitEveryImage.isEnabled() && needInAutodoc(rbFitEveryImage)) {
          autodoc.addNameValuePair(
              DirectiveDef.AUTO_FIT_RANGE_AND_STEP.getDirective(null, null), "0,0");
        }
      }
      if (rbUseSirtFalse.isSelected()) {
        if (rbUseSirtFalse.isEnabled() && needInAutodoc(rbUseSirtFalse)) {
          autodoc.addNameValuePair(DirectiveDef.USE_SIRT.getDirective(null, null), "0");
        }
      }
      else if (!saveAutodoc(rbUseSirtTrue, autodoc)) {
        if (saveAutodoc(rbDoBackprojAlso, autodoc)) {
          // Don't add useSirt if it is the default or in the templates
          if (!rbUseSirtTrue.equalsFieldHighlight(true)
              && !rbUseSirtTrue.equalsDefaultValue(true)) {
            autodoc.addNameValuePair(DirectiveDef.USE_SIRT.getDirective(null, null), "1");
          }
        }
      }
      saveAutodoc(ltfLeaveIterations, autodoc);
      saveAutodoc(cbScaleToInteger, autodoc);
      saveAutodoc(tfExtraThickness, autodoc);
      saveAutodoc(ltfFallbackThickness, autodoc);
      saveAutodoc(rtfThickness, autodoc);
      saveAutodoc(rtfBinnedThickness, autodoc);
      autodoc.write();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param field
   * @return true if the field does not match its default or its field highlight value
   */
  private boolean needInAutodoc(final Field field) {
    return !field.equalsDefaultValue() && !field.equalsFieldHighlight();
  }

  /**
   * Saves the field to the autodoc.  Returns if true if the field can be saved in the
   * autodoc.  If the field is not needed in the autodoc because it is set to default or
   * the same is a template value, it won't be saved, but this function will still return
   * true.
   * @param field
   * @param autodoc
   * @return true if field is savable
   */
  private boolean saveAutodoc(final Field field, final WritableAutodoc autodoc) {
    // Don't add directive values that are equal to default values, or directive
    // values that already exists in one of the templates. Values from templates
    // are used as field highlight values. See needInAutodoc.
    DirectiveDef directiveDef = field.getDirectiveDef();
    if (directiveDef != null) {
      String directive = directiveDef.getDirective(null, null);
      if (field.isEnabled()) {
        if (directiveDef.isBoolean() && field.isBoolean() && field.isSelected()) {
          // checkboxes and radio buttons
          if (needInAutodoc(field)) {
            autodoc.addNameValuePair(directiveDef.getDirective(null, null), "1");
          }
          return true;
        }
        else if (!directiveDef.isBoolean() && field.isText()) {
          if (!field.isBoolean() && !field.isEmpty()) {
            // text fields, file text fields, and spinners
            if (needInAutodoc(field)) {
              autodoc.addNameValuePair(directiveDef.getDirective(null, null),
                  field.getText());
            }
            return true;
          }
          else if (field.isBoolean() && field.isSelected()) {
            // radio text fields and checkbox text fields
            if (needInAutodoc(field)) {
              autodoc.addNameValuePair(directiveDef.getDirective(null, null),
                  field.getText());
            }
            return true;
          }
        }
      }
    }
    else {
      System.err.println("ERROR: " + field.getQuotedLabel() + " has a null directive.");
      Thread.dumpStack();
    }
    return false;
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
    setValue(ftfDistort, directiveFileCollection, setFieldHighlightValue);
    setValue(ftfGradient, directiveFileCollection, setFieldHighlightValue);
    setValue(cbRemoveXrays, directiveFileCollection, setFieldHighlightValue);
    setValue(ftfModelFile, directiveFileCollection, setFieldHighlightValue);
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
    setValue(rbFiducialless, directiveFileCollection, setFieldHighlightValue);
    setValue(ltfGold, directiveFileCollection, setFieldHighlightValue);
    setValue(ltfLocalAreaTargetSize, directiveFileCollection, setFieldHighlightValue);
    setValue(ltfTargetNumberOfBeads, directiveFileCollection, setFieldHighlightValue);
    setValue(ltfSizeOfPatchesXandY, directiveFileCollection, setFieldHighlightValue);
    setValue(lsContourPieces, directiveFileCollection, setFieldHighlightValue);
    setValue(cbEnableStretching, directiveFileCollection, setFieldHighlightValue);
    setValue(cbLocalAlignments, directiveFileCollection, setFieldHighlightValue);
    setValue(lsBinByFactor, directiveFileCollection, setFieldHighlightValue);
    setValue(cbCorrectCTF, directiveFileCollection, setFieldHighlightValue);
    setValue(ltfDefocus, directiveFileCollection, setFieldHighlightValue);
    if (directiveFileCollection.contains(DirectiveDef.AUTO_FIT_RANGE_AND_STEP)) {
      EtomoNumber step = new EtomoNumber(EtomoNumber.Type.DOUBLE);
      step.set(directiveFileCollection.getValue(DirectiveDef.AUTO_FIT_RANGE_AND_STEP,
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
        String range = directiveFileCollection.getValue(
            DirectiveDef.AUTO_FIT_RANGE_AND_STEP, DirectiveFile.AUTO_FIT_RANGE_INDEX);
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
      if (containsUseSirt) {
        rbUseSirtTrue.setFieldHighlight(false);
      }
    }
    setValue(ltfLeaveIterations, directiveFileCollection, setFieldHighlightValue);
    setValue(cbScaleToInteger, directiveFileCollection, setFieldHighlightValue);
    // Derive thickness from log when thickness is not specified.
    // Priority of directives
    // 1. THICKNESS
    // 2. binnedThickness
    // 3. fallbackThickness (causes derived thickness to be checked)
    boolean fallbackThickness = setValue(ltfFallbackThickness, directiveFileCollection,
        setFieldHighlightValue);
    boolean extraThickness = setValue(tfExtraThickness, directiveFileCollection,
        setFieldHighlightValue);
    boolean binnedThickness = setValue(rtfBinnedThickness, directiveFileCollection,
        setFieldHighlightValue);
    boolean thickness = setValue(rtfThickness, directiveFileCollection,
        setFieldHighlightValue);
    if (!setFieldHighlightValue) {
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
    }
    updateDisplay();
  }

  private boolean setValue(final Field field,
      final DirectiveFileCollection directiveFileCollection,
      final boolean setFieldHighlightValue) {
    DirectiveDef directiveDef = field.getDirectiveDef();
    if (directiveFileCollection.contains(directiveDef, setFieldHighlightValue)) {
      if (field.isText()) {
        String value = directiveFileCollection.getValue(directiveDef,
            setFieldHighlightValue);
        setValue(field, value, setFieldHighlightValue);
        if (field.isBoolean()) {
          setValue(field, true, setFieldHighlightValue);
        }
      }
      else {
        boolean value = directiveFileCollection.isValue(directiveDef,
            setFieldHighlightValue);
        setValue(field, value, setFieldHighlightValue);
      }
      return true;
    }
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
    // sanity check
    else {
      Thread.dumpStack();
      System.err.println("ERROR: Unknown account command:" + actionCommand);
    }
  }
}
