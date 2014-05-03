package etomo.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import etomo.BaseManager;
import etomo.logic.TrackingMethod;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.DialogType;
import etomo.ui.FieldType;
import etomo.util.Utilities;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2013</p>
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
public final class BatchRunTomoDialog implements ActionListener, ResultListener,
    ChangeListener {
  public static final String rcsid = "$Id:$";

  public static final DialogType DIALOG_TYPE = DialogType.BATCH_RUN_TOMO;
  private static final String DELIVER_TO_DIRECTORY_NAME = "Move datasets to";

  private final JPanel pnlRoot = new JPanel();
  private final LabeledTextField ltfRootName = new LabeledTextField(FieldType.STRING,
      "Batchruntomo root name: ");
  private final CheckBox cbDeliverToDirectory = new CheckBox();
  private final LabeledTextField ltfGold = new LabeledTextField(FieldType.FLOATING_POINT,
      "Bead size (nm): ");
  private final LabeledTextField ltfEmailAddress = new LabeledTextField(FieldType.STRING,
      "Email notification: ");
  private final CheckBox cbUseCPUMachineList = new CheckBox("Parallel processing");
  private final CheckBox cbUseGPUMachineList = new CheckBox("GPU");
  private final CheckBox cbRemoveXrays = new CheckBox("Remove X-rays");
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
  private LabeledTextField ltfTargetNumberOfBeads = new LabeledTextField(
      FieldType.INTEGER, "Target number of beads: ");
  private final CheckBox cbTwoSurfaces = new CheckBox(
      "Beads are on two distinct surfaces");
  private final LabeledTextField ltfLocalAreaTargetSize = new LabeledTextField(
      FieldType.INTEGER_PAIR, "Local tracking area size: ");
  private final LabeledTextField ltfSizeOfPatchesXandY = new LabeledTextField(
      FieldType.INTEGER_PAIR, "Patch tracking size: ");
  private final LabeledSpinner lsContourPieces = LabeledSpinner.getInstance(
      "Break contours into pieces: ", 1, 1, 10, 1);
  private final CheckBox cbEnableStretching = new CheckBox(
      "Enable distortion (stretching) in alignment");
  private final CheckBox cbLocalAlignments = new CheckBox("Use local alignments");
  private final LabeledSpinner lsBinByFactor = LabeledSpinner.getInstance(
      "Aligned stack binning: ", 1, 1, 8, 1);
  private final CheckBox cbCorrectCTF = new CheckBox("Correct CTF");
  private final LabeledTextField ltfDefocus = new LabeledTextField(
      FieldType.INTEGER_PAIR, "Defocus: ");
  private final ButtonGroup bgAutofit = new ButtonGroup();
  private final RadioTextField rtfAutoFitRangeAndStep = RadioTextField.getInstance(
      FieldType.FLOATING_POINT_PAIR, "Autofit range and step: ", bgAutofit);
  private final RadioButton rbFitEveryImage = new RadioButton("Fit every image",
      bgAutofit);
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
  private final ButtonGroup bgUseSirt = new ButtonGroup();
  private final RadioButton rbUseSirtFalse = new RadioButton("Back-projection", bgUseSirt);
  private final RadioButton rbUseSirtTrue = new RadioButton("SIRT", bgUseSirt);
  private final RadioButton rbDoBackprojAlso = new RadioButton("Both", bgUseSirt);
  private final JLabel lThicknessSpacingFallback = new JLabel(" unbinned pixels");
  private final LabeledTextField ltfLeaveIterations = new LabeledTextField(
      FieldType.STRING, "Leave iterations: ");
  private final CheckBox cbScaleToInteger = new CheckBox("Scale to integers");
  private final TabbedPane tabbedPane = new TabbedPane();
  private final JPanel[] pnlTabs = new JPanel[Tab.SIZE];
  private final JPanel pnlBatch = new JPanel();
  private final JPanel pnlStacks = new JPanel();
  private final JPanel pnlDataset = new JPanel();
  private final JPanel pnlRun = new JPanel();

  private final FileTextField2 ftfRootName;
  private final FileTextField2 ftfInputDirectiveFile;
  private final TemplatePanel templatePanel;
  private final FileTextField2 ftfDeliverToDirectory;
  private final BatchRunTomoTable table;
  private final FileTextField2 ftfDistort;
  private final FileTextField2 ftfGradient;
  private final FileTextField2 ftfModelFile;
  private final BaseManager manager;
  private final AxisID axisID;

  private Tab curTab = null;

  private BatchRunTomoDialog(final BaseManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    ftfRootName = FileTextField2.getAltLayoutInstance(manager, "Location: ");
    ftfInputDirectiveFile = FileTextField2.getAltLayoutInstance(manager,
        "Starting directive file: ");
    templatePanel = TemplatePanel
        .getBorderlessInstance(manager, axisID, null, null, null);
    ftfDeliverToDirectory = FileTextField2.getAltLayoutInstance(manager,
        DELIVER_TO_DIRECTORY_NAME + ": ");
    table = BatchRunTomoTable.getInstance(manager);
    ftfDistort = FileTextField2.getAltLayoutInstance(manager, "Image distortion file: ");
    ftfGradient = FileTextField2.getAltLayoutInstance(manager, "Mag gradient file: ");
    ftfModelFile = FileTextField2.getAltLayoutInstance(manager,
        "Manual replacement model: ");
  }

  public static BatchRunTomoDialog getInstance(final BaseManager manager,
      final AxisID axisID) {
    BatchRunTomoDialog instance = new BatchRunTomoDialog(manager, axisID);
    instance.createPanel();
    instance.addListeners();
    instance.addtooltips();
    instance.demo();
    return instance;
  }

  private void createPanel() {
    // init
    JPanel pnlTable = new JPanel();
    JPanel pnlRootName = new JPanel();
    JPanel pnlDeliverToDirectory = new JPanel();
    JPanel pnlTemplates = new JPanel();
    JPanel pnlParallelSettings = new JPanel();
    JPanel pnlModelFile = new JPanel();
    JPanel pnlTrackingMethod = new JPanel();
    JPanel pnlGold = new JPanel();
    JPanel pnlSizeOfPatchesXandY = new JPanel();
    JPanel pnlBinByFactor = new JPanel();
    JPanel pnlCorrectCTF = new JPanel();
    JPanel pnlReconstruction = new JPanel();
    JPanel pnlThicknessSpacingFallback = new JPanel();
    JPanel pnlThickness = new JPanel();
    JPanel pnlReconstructionType = new JPanel();
    ftfRootName.setText(".");
    ltfRootName.setText(Utilities.getDateTimeStampRootName());
    cbDeliverToDirectory.setName(DELIVER_TO_DIRECTORY_NAME);
    templatePanel.setTemplateColor();
    ftfInputDirectiveFile.setFieldEditable(false);
    btnModelFile.setToPreferredSize();
    ltfEmailAddress.setPreferredWidth(248);
    ftfGradient.setPreferredWidth(272);
    rtfThicknessPixels.setSelected(true);
    rbUseSirtFalse.setSelected(true);
    // root panel
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(new BeveledBorder("Batchruntomo Interface").getBorder());
    pnlRoot.add(tabbedPane);
    // tabbedPane
    for (int i = 0; i < Tab.SIZE; i++) {
      pnlTabs[i] = new JPanel();
      Tab tab = Tab.getInstance(i);
      tabbedPane.addTab(tab.title, pnlTabs[i]);
    }
    // Batch
    pnlBatch.setLayout(new BoxLayout(pnlBatch, BoxLayout.Y_AXIS));
    pnlBatch.setBorder(new EtchedBorder("Batch Setup Parameters").getBorder());
    pnlBatch.add(pnlDeliverToDirectory);
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlBatch.add(ftfInputDirectiveFile.getRootPanel());
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlBatch.add(pnlTemplates);
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlBatch.add(ltfEmailAddress.getComponent());
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlBatch.add(pnlRootName);
    // Stacks
    pnlStacks.setLayout(new BoxLayout(pnlStacks, BoxLayout.Y_AXIS));
    pnlStacks.setBorder(BorderFactory.createEtchedBorder());
    pnlStacks.add(pnlTable);
    pnlStacks.add(pnlParallelSettings);
    // Dataset
    pnlDataset.setLayout(new BoxLayout(pnlDataset, BoxLayout.Y_AXIS));
    pnlDataset.setBorder(new EtchedBorder("Dataset Parameters").getBorder());
    pnlDataset.add(ftfDistort.getRootPanel());
    pnlDataset.add(ftfGradient.getRootPanel());
    pnlDataset.add(cbRemoveXrays);
    pnlDataset.add(pnlModelFile);
    pnlDataset.add(pnlTrackingMethod);
    pnlDataset.add(pnlGold);
    pnlDataset.add(pnlSizeOfPatchesXandY);
    pnlDataset.add(cbEnableStretching);
    pnlDataset.add(cbLocalAlignments);
    pnlDataset.add(pnlBinByFactor);
    pnlDataset.add(pnlCorrectCTF);
    pnlDataset.add(pnlReconstruction);
    // Table
    pnlTable.setLayout(new BoxLayout(pnlTable, BoxLayout.Y_AXIS));
    pnlTable.setBorder(new EtchedBorder("Datasets").getBorder());
    pnlTable.add(table.getComponent());
    // ParallelSettings
    pnlParallelSettings.setLayout(new BoxLayout(pnlParallelSettings, BoxLayout.Y_AXIS));
    pnlParallelSettings.setBorder(new EtchedBorder("Run Actions").getBorder());
    pnlParallelSettings.add(cbUseCPUMachineList);
    pnlParallelSettings.add(cbUseGPUMachineList);
    // RootName
    pnlRootName.setLayout(new BoxLayout(pnlRootName, BoxLayout.Y_AXIS));
    pnlRootName.setBorder(new EtchedBorder("Batchruntomo Project Files").getBorder());
    pnlRootName.add(ltfRootName.getComponent());
    pnlRootName.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlRootName.add(ftfRootName.getRootPanel());
    // Templates
    pnlTemplates.setLayout(new BoxLayout(pnlTemplates, BoxLayout.X_AXIS));
    pnlTemplates.add(templatePanel.getComponent());
    pnlTemplates.add(Box.createHorizontalGlue());
    // DeliverToDirectory
    pnlDeliverToDirectory
        .setLayout(new BoxLayout(pnlDeliverToDirectory, BoxLayout.X_AXIS));
    pnlDeliverToDirectory.add(cbDeliverToDirectory);
    pnlDeliverToDirectory.add(ftfDeliverToDirectory.getRootPanel());
    // ModelFile
    pnlModelFile.setLayout(new BoxLayout(pnlModelFile, BoxLayout.X_AXIS));
    pnlModelFile.add(ftfModelFile.getRootPanel());
    pnlModelFile.add(Box.createRigidArea(FixedDim.x5_y0));
    pnlModelFile.add(btnModelFile.getComponent());
    pnlModelFile.add(Box.createHorizontalGlue());
    // TrackingMethod
    pnlTrackingMethod.setLayout(new BoxLayout(pnlTrackingMethod, BoxLayout.Y_AXIS));
    pnlTrackingMethod.setBorder(new EtchedBorder("Alignment Method").getBorder());
    pnlTrackingMethod.add(rbTrackingMethodSeed.getComponent());
    pnlTrackingMethod.add(rbTrackingMethodRaptor.getComponent());
    pnlTrackingMethod.add(rbTrackingMethodPatchTracking.getComponent());
    pnlTrackingMethod.add(rbFiducialless.getComponent());
    // Gold
    pnlGold.setLayout(new GridLayout(2, 2, 15, 0));
    pnlGold.add(ltfGold.getComponent());
    pnlGold.add(cbTwoSurfaces);
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
    pnlCorrectCTF.add(rtfAutoFitRangeAndStep.getContainer());
    pnlCorrectCTF.add(ltfDefocus.getComponent());
    pnlCorrectCTF.add(rbFitEveryImage.getComponent());
    // Reconstruction
    pnlReconstruction.setLayout(new BoxLayout(pnlReconstruction, BoxLayout.X_AXIS));
    pnlReconstruction.setBorder(new EtchedBorder("Reconstruction").getBorder());
    pnlReconstruction.add(pnlReconstructionType);
    pnlReconstruction.add(Box.createRigidArea(FixedDim.x10_y0));
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
    UIUtilities.alignComponentsX(pnlBatch, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlDataset, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlReconstructionType, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
    processResult(ftfRootName);
    stateChanged(null);
    updateDisplay();
  }

  private void addListeners() {
    cbDeliverToDirectory.addActionListener(this);
    cbRemoveXrays.addActionListener(this);
    rbTrackingMethodSeed.addActionListener(this);
    rbTrackingMethodRaptor.addActionListener(this);
    rbTrackingMethodPatchTracking.addActionListener(this);
    rbFiducialless.addActionListener(this);
    cbCorrectCTF.addActionListener(this);
    rtfThicknessPixels.addActionListener(this);
    rtfBinnedThickness.addActionListener(this);
    rtfThicknessNm.addActionListener(this);
    rtfThicknessSpacingPlus.addActionListener(this);
    rbUseSirtFalse.addActionListener(this);
    rbUseSirtTrue.addActionListener(this);
    rbDoBackprojAlso.addActionListener(this);
    templatePanel.addActionListener(this);
    tabbedPane.addChangeListener(this);
  }

  public Container getContainer() {
    return pnlRoot;
  }

  public void setParameters(final BatchRunTomoMetaData metaData) {
  }

  private void demo() {
    // templates
    templatePanel.demo();
    ltfSizeOfPatchesXandY.setText("300, 300");
    ltfSizeOfPatchesXandY.setTemplateColor(true);
    rbFiducialless.setTemplateColor(true);
    cbCorrectCTF.setTemplateColor(true);
    rbUseSirtFalse.setTemplateColor(true);
    rbUseSirtTrue.setTemplateColor(true);
    cbLocalAlignments.setSelected(true);
    cbLocalAlignments.setTemplateColor(true);
    rbTrackingMethodSeed.setTemplateColor(true);
    rbTrackingMethodPatchTracking.setTemplateColor(true);
    rbTrackingMethodRaptor.setTemplateColor(true);
    // base batch directives
    ftfInputDirectiveFile.setText("/home/sueh/Directives/batchBB.adoc");
    ltfGold.setText("10");
    cbTwoSurfaces.setSelected(true);
    ltfTargetNumberOfBeads.setText(30);
    cbEnableStretching.setSelected(true);
    rtfThicknessPixels.setText("84");
  }

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (templatePanel.equalsActionCommand(actionCommand)) {

    }
    else if (actionCommand.equals(cbDeliverToDirectory.getActionCommand())
        || actionCommand.equals(cbRemoveXrays.getActionCommand())
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

  public void processResult(final Object object) {
    File rootLocation = ftfRootName.getFile();
    if (rootLocation != null) {
      table.setCurrentDirectory(rootLocation.getAbsolutePath());
    }
    else {
      table.setCurrentDirectory(null);
    }
  }

  /**
   * Handle tab change event
   */
  public void stateChanged(final ChangeEvent event) {
    if (curTab == null) {
      tabbedPane.setSelectedIndex(Tab.DEFAULT.index);
      curTab = Tab.DEFAULT;
    }
    else {
      pnlTabs[curTab.index].removeAll();
      curTab = Tab.getInstance(tabbedPane.getSelectedIndex());
    }
    if (curTab == Tab.BATCH) {
      pnlTabs[curTab.index].add(pnlBatch);
    }
    else if (curTab == Tab.STACKS) {
      pnlTabs[curTab.index].add(pnlStacks);
    }
    else if (curTab == Tab.DATASET) {
      pnlTabs[curTab.index].add(pnlDataset);
    }
    else if (curTab == Tab.RUN) {
      pnlTabs[curTab.index].add(pnlRun);
    }
    UIHarness.INSTANCE.pack(axisID, manager);
  }

  private void updateDisplay() {
    ftfDeliverToDirectory.setEnabled(cbDeliverToDirectory.isSelected());
    boolean removeXrays = cbRemoveXrays.isSelected();
    ftfModelFile.setEnabled(removeXrays);
    btnModelFile.setEnabled(removeXrays);
    boolean beadTracking = rbTrackingMethodSeed.isSelected()
        || rbTrackingMethodRaptor.isSelected();
    ltfGold.setEnabled(beadTracking);
    ltfTargetNumberOfBeads.setEnabled(beadTracking);
    cbTwoSurfaces.setEnabled(beadTracking);
    ltfLocalAreaTargetSize.setEnabled(beadTracking);
    boolean patchTracking = rbTrackingMethodPatchTracking.isSelected();
    ltfSizeOfPatchesXandY.setEnabled(patchTracking);
    lsContourPieces.setEnabled(patchTracking);
    boolean fiducialless = rbFiducialless.isSelected();
    cbEnableStretching.setEnabled(!fiducialless);
    cbLocalAlignments.setEnabled(!fiducialless);
    boolean ctf = cbCorrectCTF.isSelected();
    ltfDefocus.setEnabled(ctf);
    rtfAutoFitRangeAndStep.setEnabled(ctf);
    rbFitEveryImage.setEnabled(ctf);
    boolean thicknessSpacing = rtfThicknessSpacingPlus.isSelected();
    ltfThicknessSpacingFallback.setEnabled(thicknessSpacing);
    lThicknessSpacingFallback.setEnabled(thicknessSpacing);
    boolean sirt = rbUseSirtTrue.isSelected() || rbDoBackprojAlso.isSelected();
    ltfLeaveIterations.setEnabled(sirt);
    cbScaleToInteger.setEnabled(sirt);
  }

  private void addtooltips() {
  }

  private static final class Tab {
    private static final Tab BATCH = new Tab(0, "Batch Parameters");
    private static final Tab STACKS = new Tab(1, "Image Stacks");
    private static final Tab DATASET = new Tab(2, "Global Dataset Values");
    private static final Tab RUN = new Tab(3, "Run");

    private static final int SIZE = RUN.index + 1;

    private static final Tab DEFAULT = BATCH;

    private final int index;
    private final String title;

    private Tab(final int index, final String title) {
      this.index = index;
      this.title = title;
    }

    private static Tab getInstance(final int index) {
      if (index == BATCH.index) {
        return BATCH;
      }
      if (index == STACKS.index) {
        return STACKS;
      }
      if (index == DATASET.index) {
        return DATASET;
      }
      if (index == RUN.index) {
        return RUN;
      }
      return DEFAULT;
    }
  }
}
