package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import etomo.EtomoDirector;
import etomo.JoinManager;
import etomo.comscript.FinishjoinParam;
import etomo.type.AxisID;
import etomo.type.ConstEtomoInteger;
import etomo.type.ConstJoinMetaData;
import etomo.type.EtomoInteger;
import etomo.type.EtomoSimpleType;
import etomo.type.JoinMetaData;

/**
 * <p>Description: The dialog box for creating the fiducial model(s).</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 1.1.2.25  2004/11/08 22:26:15  sueh
 * <p> Bug# 520 On Join tab:  Moved finish join functionality to the left of the
 * <p> table by changing the orientation of pnlJoin.  Moved Finish Join button and
 * <p> open in 3dmod button into pnlFinishJoin.
 * <p>
 * <p> Revision 1.1.2.24  2004/10/30 02:36:30  sueh
 * <p> bug# 520 Added getRootName().
 * <p>
 * <p> Revision 1.1.2.23  2004/10/29 01:20:48  sueh
 * <p> bug# 520 Removed working directory from meta data.  Getting working
 * <p> directory from constructor.
 * <p>
 * <p> Revision 1.1.2.22  2004/10/28 22:15:15  sueh
 * <p> bug# 520 Keep the text associated with the Alignment ref section
 * <p> checkbox from getting grayed out.
 * <p>
 * <p> Revision 1.1.2.21  2004/10/28 17:09:12  sueh
 * <p> bug# 520 Adding revert to empty.  Putting revert buttons in a box to the
 * <p> right.  Making button text available for message boxes.
 * <p>
 * <p> Revision 1.1.2.20  2004/10/25 23:14:03  sueh
 * <p> bug# 520 Set default size in X, Y when changing to the join tab.  Fixed
 * <p> spinners not initializing in setMetaData by setting numSections before
 * <p> initializing.
 * <p>
 * <p> Revision 1.1.2.19  2004/10/22 21:08:07  sueh
 * <p> bug# 520 Changed offsetInX, Y to shiftInX, Y.
 * <p>
 * <p> Revision 1.1.2.18  2004/10/22 03:26:45  sueh
 * <p> bug# 520 Reducing the number of ConstJoinMetaData functions by
 * <p> passing EtomoInteger, EtomoFloat, etc and using their get() and
 * <p> getString() functions.
 * <p>
 * <p> Revision 1.1.2.17  2004/10/21 02:58:46  sueh
 * <p> bug# 520 Implemented buttons, added enableMidas to be used after
 * <p> xfalign is run.
 * <p>
 * <p> Revision 1.1.2.16  2004/10/18 19:11:15  sueh
 * <p> bug# 520 Added a call to JoinManager.midasSample().
 * <p>
 * <p> Revision 1.1.2.15  2004/10/18 18:11:10  sueh
 * <p> bug# 520 Passing fields to and from meta data.  Added call to xfalign().
 * <p> Moved validation of workingDir and rootName to ConstJoinMetaData.
 * <p>
 * <p> Revision 1.1.2.14  2004/10/15 00:46:31  sueh
 * <p> bug# 520 Added setMetaData()
 * <p>
 * <p> Revision 1.1.2.13  2004/10/14 17:23:11  sueh
 * <p> bug# 520 Open sample averages.
 * <p>
 * <p> Revision 1.1.2.12  2004/10/14 03:31:38  sueh
 * <p> bug# 520 Disabled Align and Join tabs until Make Samples is run.
 * <p> Otherwise ImodManager is not initialized with meta data.  Added
 * <p> functionality for Open Samples in 3dmod button.  Added invalidReason.
 * <p> Did a validation check when loading meta data.
 * <p>
 * <p> Revision 1.1.2.11  2004/10/14 02:28:58  sueh
 * <p> bug# 520 Fixed action().  Setting working directory in join manager when
 * <p> Make Samples is pressed.
 * <p>
 * <p> Revision 1.1.2.10  2004/10/13 23:12:27  sueh
 * <p> bug# 520 Added align and join ui components.
 * <p>
 * <p> Revision 1.1.2.9  2004/10/11 02:13:46  sueh
 * <p> bug# 520 Using a variable called propertyUserDir instead of the "user.dir"
 * <p> property.  This property would need a different value for each manager.
 * <p> This variable can be retrieved from the manager if the object knows its
 * <p> manager.  Otherwise it can retrieve it from the current manager using the
 * <p> EtomoDirector singleton.  If there is no current manager, EtomoDirector
 * <p> gets the value from the "user.dir" property.
 * <p>
 * <p> Revision 1.1.2.8  2004/10/08 16:30:53  sueh
 * <p> bug# 520 Changed the name of the function retrieveData(JoinMetaData)
 * <p> to getMetaData.  Retrieve now only refers to pulling data off the screen
 * <p> and into storage owned by the ui object.  Edded getInvalidReason().
 * <p>
 * <p> Revision 1.1.2.7  2004/10/06 02:23:20  sueh
 * <p> bug# 520 Changed Make Join button to Make Samples.  Removed Use
 * <p> Density Reference Section checkbox.  Added a function to get the
 * <p> working directory File.  Added addSection(File) to control adding a
 * <p> tomogram file to the table from the outside.  Added abortAddSection() to
 * <p> signal that adding the section had failed.  These are necessary when a
 * <p> tomogram must be flipped because the signal that the flip is finished
 * <p> comes from outside.
 * <p>
 * <p> Revision 1.1.2.6  2004/10/01 19:58:55  sueh
 * <p> bug# 520 Moved working dir and root name above section table.
 * <p>
 * <p> Revision 1.1.2.5  2004/09/29 19:34:05  sueh
 * <p> bug# 520 Added retrieveData() to retrieve data from the screen.
 * <p>
 * <p> Revision 1.1.2.4  2004/09/23 23:37:46  sueh
 * <p> bug# 520 Converted to DoubleSpacedPanel and SpacedPanel.  Added
 * <p> MakeJoin panel.
 * <p>
 * <p> Revision 1.1.2.3  2004/09/21 18:00:42  sueh
 * <p> bug# 520 Moved the buttons that affected section table rows to
 * <p> SectionTablePanel.  Placed a X axis panel called pnlSetupTab behind the
 * <p> Y axis Setup panel to create rigid areas on the left and right of the Setup
 * <p> border.
 * <p>
 * <p> Revision 1.1.2.2  2004/09/15 22:40:07  sueh
 * <p> bug# 520 added create panel functions
 * <p> </p>
 */
public class JoinDialog implements ContextMenu {
  public static final String rcsid = "$Id$";

  public static final int SETUP_TAB = 0;
  public static final int ALIGN_TAB = 1;
  public static final int JOIN_TAB = 2;
  
  public static final String REFINE_AUTO_ALIGNMENT_TEXT = "Refine Auto Alignment";
  public static final String MIDAS_TEXT = "Midas";
  public static final String FINISH_JOIN_TEXT = "Finish Join";
  public static final String WORKING_DIRECTORY_TEXT = "Working directory";
  public static final String GET_MAX_SIZE_TEXT = "Get Max Size and Shift";
  public static final String TRIAL_JOIN_TEXT = "Trial Join";
  
  static final String OPEN_BINNED_BY = "Open binned by ";
  
  private static final String OPEN_IN_3DMOD = "Open in 3dmod";
  private static final String IN_X_AND_Y = "in X and Y";
  
  private static ImageIcon iconFolder = new ImageIcon(ClassLoader
      .getSystemResource("images/openFile.gif"));
  private static Dimension dimButton = UIParameters.getButtonDimension();
  private static Dimension dimSpinner = UIParameters.getSpinnerDimension();

  private JPanel rootPanel;
  private JTabbedPane tabPane;
  private DoubleSpacedPanel pnlSetup;
  private SectionTablePanel pnlSectionTable;
  private DoubleSpacedPanel pnlAlign;
  private DoubleSpacedPanel pnlJoin;
  private SpacedPanel setupPanel1;
  private SpacedPanel alignPanel1;
  private SpacedPanel alignPanel2;
  private DoubleSpacedPanel pnlXfalign;
  private DoubleSpacedPanel pnlFinishJoin;
  
  private JButton btnWorkingDir;
  private MultiLineToggleButton btnMakeSamples;
  private MultiLineButton btnOpenSample;
  private MultiLineButton btnOpenSampleAverages;
  private MultiLineButton btnInitialAutoAlignment;
  private MultiLineButton btnMidas;
  private MultiLineButton btnRefineAutoAlignment;
  private MultiLineButton btnRevertToMidas;
  private MultiLineButton btnRevertToEmpty;
  private MultiLineButton btnGetMaxSize;
  private MultiLineButton btnTrialJoin;
  private MultiLineButton btnOpenTrialIn3dmod;
  private MultiLineButton btnGetSubarea;
  private MultiLineButton btnFinishJoin;
  private MultiLineButton btnOpenIn3dmod;


  private LabeledTextField ltfWorkingDir;
  private LabeledTextField ltfRootName;
  private LabeledTextField ltfSigmaLowFrequency;
  private LabeledTextField ltfCutoffHighFrequency;
  private LabeledTextField ltfSigmaHighFrequency;
  private LabeledTextField ltfSizeInX;
  private LabeledTextField ltfSizeInY;
  private LabeledTextField ltfShiftInX;
  private LabeledTextField ltfShiftInY;
  private JRadioButton rbFullLinearTransformation;
  private JRadioButton rbRotationTranslationMagnification;
  private JRadioButton rbRotationTranslation;
  private JCheckBox cbUseAlignmentRefSection;
  private JSpinner spinAlignmentRefSection;
  private LabeledSpinner spinDensityRefSection;
  private LabeledSpinner spinTrialBinning;
  private LabeledSpinner spinOpenBinnedBy;
  private LabeledSpinner spinOpenTrialBinnedBy;
  private LabeledSpinner spinUseEveryNSections;
  
  private int numSections = 0;
  private int curTab = SETUP_TAB;
  private boolean alignTabEnabled = false;
  private boolean joinTabEnabled = false;
  private String invalidReason = null;

  private JoinActionListener joinActionListener = new JoinActionListener(this);
  private WorkingDirActionListener workingDirActionListener = new WorkingDirActionListener(
      this);
  private UseAlignmentRefSectionActionListener useAlignmentRefSectionActionListener = new UseAlignmentRefSectionActionListener(this);

  private final AxisID axisID;
  private final JoinManager joinManager;

  public JoinDialog(JoinManager joinManager) {
    this(joinManager, null);
  }
  
  public JoinDialog(JoinManager joinManager, String workingDirName) {
    axisID = AxisID.ONLY;
    this.joinManager = joinManager;
    createRootPanel(workingDirName);
    joinManager.packMainWindow();
  }

  private void createRootPanel(String workingDirName) {
    rootPanel = new JPanel();
    //  Mouse adapter for context menu
    GenericMouseAdapter mouseAdapter = new GenericMouseAdapter(this);
    rootPanel.addMouseListener(mouseAdapter);
    createTabPane(workingDirName);
    rootPanel.add(tabPane);
  }

  private void createTabPane(String workingDirName) {
    tabPane = new JTabbedPane();
    TabChangeListener tabChangeListener = new TabChangeListener(this);
    tabPane.addChangeListener(tabChangeListener);
    tabPane.setBorder(new BeveledBorder("Join").getBorder());
    createSetupPanel(workingDirName);
    tabPane.addTab("Setup", pnlSetup.getContainer());
    createAlignPanel();
    tabPane.addTab("Align", pnlAlign.getContainer());
    createJoinPanel();
    tabPane.addTab("Join", pnlJoin.getContainer());
    setEnabledTabs();
  }
  
  public void setEnabledTabs(boolean enabled) {
    alignTabEnabled = enabled;
    joinTabEnabled = enabled;
    setEnabledTabs();
  }
  
  private void setEnabledTabs() {
    tabPane.setEnabledAt(1, alignTabEnabled);
    tabPane.setEnabledAt(2, joinTabEnabled);
  }
  
  private void addPanelComponents(int tab) {
    if (tab == SETUP_TAB) {
      addSetupPanelComponents();
    }
    else if (tab == ALIGN_TAB) {
      addAlignPanelComponents();
    }
    else if (tab == JOIN_TAB) {
      addJoinPanelComponents();
    }
  }
  
  private void removePanelComponents(int tab) {
    if (tab == SETUP_TAB) {
      pnlSetup.removeAll();
    }
    else if (tab == ALIGN_TAB) {
      pnlAlign.removeAll();
    }
    else if (tab == JOIN_TAB) {
      pnlJoin.removeAll();
    }
  }
  
  void changeTab(ChangeEvent event){
    removePanelComponents(curTab);
    curTab = tabPane.getSelectedIndex();
    addPanelComponents(curTab);
    if (EtomoDirector.getInstance().getUserConfiguration().isAutoFit()) {
      joinManager.getMainPanel().fitWindow();
    }
  }

  private void createSetupPanel(String workingDirName) {
    pnlSetup = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5);
    //first component
    setupPanel1 = new SpacedPanel(FixedDim.x5_y0);
    setupPanel1
        .setLayout(new BoxLayout(setupPanel1.getContainer(), BoxLayout.X_AXIS));
    ltfWorkingDir = new LabeledTextField(WORKING_DIRECTORY_TEXT + ": ");
    ltfWorkingDir.setText(workingDirName);
    setupPanel1.add(ltfWorkingDir);
    btnWorkingDir = new JButton(iconFolder);
    btnWorkingDir.setPreferredSize(FixedDim.folderButton);
    btnWorkingDir.setMaximumSize(FixedDim.folderButton);
    btnWorkingDir.addActionListener(workingDirActionListener);
    setupPanel1.add(btnWorkingDir);
    //second component
    ltfRootName = new LabeledTextField("Root name for output file: ");
    //third component    
    pnlSectionTable = new SectionTablePanel(this, joinManager, curTab);
    //fourth component
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinDensityRefSection = new LabeledSpinner(
        "Reference section for density matching: ", spinnerModel);
    spinDensityRefSection.setTextMaxmimumSize(dimSpinner);
    spinDensityRefSection.setEnabled(false);
    //fifth component
    btnMakeSamples = new MultiLineToggleButton("Make Samples");
    btnMakeSamples.addActionListener(joinActionListener);
    UIUtilities.setButtonSize(btnMakeSamples, dimButton);
    btnMakeSamples.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnMakeSamples.setEnabled(false);
  }
  
  private void addSetupPanelComponents() {
    pnlSetup.add(setupPanel1);
    pnlSetup.add(ltfRootName);
    pnlSetup.add(pnlSectionTable.getRootPanel());
    pnlSectionTable.setCurTab(SETUP_TAB);
    pnlSectionTable.displayCurTab();
    pnlSetup.add(spinDensityRefSection);
    pnlSetup.add(btnMakeSamples);
  }
  
  private void createAlignPanel() {
    pnlAlign = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5);
    //second component
    alignPanel1 = new SpacedPanel(FixedDim.x5_y0);
    alignPanel1.setLayout(new BoxLayout(alignPanel1.getContainer(), BoxLayout.X_AXIS));
    btnOpenSample = new MultiLineButton("Open Sample in 3dmod");
    btnOpenSample.addActionListener(joinActionListener);
    btnOpenSampleAverages = new MultiLineButton("Open Sample Averages in 3dmod");
    btnOpenSampleAverages.addActionListener(joinActionListener);
    alignPanel1.addMultiLineButton(btnOpenSample);
    alignPanel1.addMultiLineButton(btnOpenSampleAverages);
    //third component
    pnlXfalign = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5, new EtchedBorder("Auto Alignment Parameters").getBorder(), false);
    ltfSigmaLowFrequency = new LabeledTextField("Sigma for low-frequency filter: ");
    pnlXfalign.add(ltfSigmaLowFrequency);
    ltfCutoffHighFrequency = new LabeledTextField("Cutoff for high-frequency filter: ");
    pnlXfalign.add(ltfCutoffHighFrequency);
    ltfSigmaHighFrequency = new LabeledTextField("Sigma for high-frequency filter: ");
    pnlXfalign.add(ltfSigmaHighFrequency);
    ButtonGroup bgSearchFor = new ButtonGroup();
    rbFullLinearTransformation = new JRadioButton("Full linear transformation");
    rbRotationTranslationMagnification = new JRadioButton("Rotation/translation/magnification");
    rbRotationTranslation = new JRadioButton("Rotation/translation");
    bgSearchFor.add(rbFullLinearTransformation);
    bgSearchFor.add(rbRotationTranslationMagnification);
    bgSearchFor.add(rbRotationTranslation);
    pnlXfalign.add(new JLabel("Search For:"));
    pnlXfalign.add(rbFullLinearTransformation, false);
    pnlXfalign.add(rbRotationTranslationMagnification, false);
    pnlXfalign.add(rbRotationTranslation);
    //fourth component
    alignPanel2 = new SpacedPanel(FixedDim.x5_y0);
    alignPanel2.setLayout(new BoxLayout(alignPanel2
        .getContainer(), BoxLayout.X_AXIS));
    SpacedPanel alignPanel2A = new SpacedPanel(FixedDim.x0_y5);
    alignPanel2A.setLayout(new BoxLayout(alignPanel2A
        .getContainer(), BoxLayout.Y_AXIS));
    btnInitialAutoAlignment = new MultiLineButton("Initial Auto Alignment");
    btnInitialAutoAlignment.addActionListener(joinActionListener);
    alignPanel2A.addMultiLineButton(btnInitialAutoAlignment);
    btnMidas = new MultiLineButton(MIDAS_TEXT);
    btnMidas.addActionListener(joinActionListener);
    alignPanel2A.addMultiLineButton(btnMidas);
    btnRefineAutoAlignment = new MultiLineButton(REFINE_AUTO_ALIGNMENT_TEXT);
    btnRefineAutoAlignment.addActionListener(joinActionListener);
    alignPanel2A.addMultiLineButton(btnRefineAutoAlignment);
    alignPanel2.add(alignPanel2A);
    DoubleSpacedPanel alignPanel2B = new DoubleSpacedPanel(false, FixedDim.x5_y0,
        FixedDim.x0_y5, BorderFactory.createEtchedBorder());
    btnRevertToMidas = new MultiLineButton("Revert Auto Alignment to Midas");
    btnRevertToMidas.addActionListener(joinActionListener);
    alignPanel2B.addMultiLineButton(btnRevertToMidas);
    btnRevertToEmpty= new MultiLineButton("Revert to No Transforms");
    btnRevertToEmpty.addActionListener(joinActionListener);
    alignPanel2B.addMultiLineButton(btnRevertToEmpty);
    alignPanel2.add(alignPanel2B);
  }
  
  private void addAlignPanelComponents() {
    //first component
    pnlAlign.add(pnlSectionTable.getRootPanel());
    pnlSectionTable.setCurTab(ALIGN_TAB);
    pnlSectionTable.displayCurTab();
    //second component
    pnlAlign.add(alignPanel1);
    //third component
    pnlAlign.add(pnlXfalign);
    //fourth component
    pnlAlign.add(alignPanel2);
  }

  private void createJoinPanel() {
    pnlJoin = new DoubleSpacedPanel(true, FixedDim.x5_y0, FixedDim.x0_y5);
    //second component
    createFinishJoinPanel();
  }
  
  private void addJoinPanelComponents() {
    //first component
    pnlJoin.add(pnlSectionTable.getRootPanel());
    pnlSectionTable.setCurTab(JOIN_TAB);
    pnlSectionTable.displayCurTab();
    //second component
    pnlJoin.add(pnlFinishJoin);
  }
  
  private void createFinishJoinPanel() {
    pnlFinishJoin = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5, BorderFactory.createEtchedBorder());
    pnlFinishJoin.setComponentAlignmentX(Component.CENTER_ALIGNMENT);
    //first component
    JPanel finishJoinPanel1 = new JPanel();
    finishJoinPanel1.setLayout(new BoxLayout(finishJoinPanel1, BoxLayout.X_AXIS));
    cbUseAlignmentRefSection = new JCheckBox("Reference section for alignment: ");
    cbUseAlignmentRefSection.addActionListener(useAlignmentRefSectionActionListener);
    finishJoinPanel1.add(cbUseAlignmentRefSection);
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinAlignmentRefSection = new JSpinner();
    spinAlignmentRefSection.setModel(spinnerModel);
    spinAlignmentRefSection.setMaximumSize(dimSpinner);
    spinAlignmentRefSection.setEnabled(false);
    finishJoinPanel1.add(spinAlignmentRefSection);
    pnlFinishJoin.add(finishJoinPanel1);
    //second component
    btnGetMaxSize = new MultiLineButton(GET_MAX_SIZE_TEXT);
    btnGetMaxSize.addActionListener(joinActionListener);
    pnlFinishJoin.addMultiLineButton(btnGetMaxSize);
    //third component
    SpacedPanel finishJoinPanel2 = new SpacedPanel(FixedDim.x5_y0);
    finishJoinPanel2.setLayout(new BoxLayout(finishJoinPanel2.getContainer(), BoxLayout.X_AXIS));
    ltfSizeInX = new LabeledTextField("Size in X: ");
    finishJoinPanel2.add(ltfSizeInX);
    ltfSizeInY = new LabeledTextField("Y: ");
    finishJoinPanel2.add(ltfSizeInY);
    pnlFinishJoin.add(finishJoinPanel2);
    //fourth component
    SpacedPanel finishJoinPanel3 = new SpacedPanel(FixedDim.x5_y0);
    finishJoinPanel3.setLayout(new BoxLayout(finishJoinPanel3.getContainer(), BoxLayout.X_AXIS));
    ltfShiftInX = new LabeledTextField("Shift in X: ");
    finishJoinPanel3.add(ltfShiftInX);
    ltfShiftInY = new LabeledTextField("Y: ");
    finishJoinPanel3.add(ltfShiftInY);
    pnlFinishJoin.add(finishJoinPanel3);
    //fifth component
    createTrialJoinPanel();
    //sixth component
    btnFinishJoin = new MultiLineButton(FINISH_JOIN_TEXT);
    btnFinishJoin.addActionListener(joinActionListener);
    pnlFinishJoin.addMultiLineButton(btnFinishJoin);
    //seventh component
    spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
    spinOpenBinnedBy = new LabeledSpinner(OPEN_BINNED_BY, spinnerModel);
    btnOpenIn3dmod = new MultiLineButton(OPEN_IN_3DMOD);
    btnOpenIn3dmod.addActionListener(joinActionListener);
    pnlFinishJoin.add(createOpen3dmodPanel(spinOpenBinnedBy, btnOpenIn3dmod));
  }
  
  private void createTrialJoinPanel() {
    DoubleSpacedPanel pnlTrialJoin = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5, new EtchedBorder("Trial Join").getBorder(), false);
    pnlTrialJoin.setComponentAlignmentX(Component.CENTER_ALIGNMENT);
    //first component
    JPanel trialJoinPanel1 = new JPanel();
    trialJoinPanel1.setLayout(new BoxLayout(trialJoinPanel1, BoxLayout.X_AXIS));
    int zMax = pnlSectionTable.getZMax();
    SpinnerModel spinnerModel = new SpinnerNumberModel(zMax < 1 ? 1
        : zMax < 10 ? zMax : 10, 1, zMax < 1 ? 1 : zMax, 1);
    spinUseEveryNSections = new LabeledSpinner("Use every ", spinnerModel);
    spinUseEveryNSections.setTextMaxmimumSize(dimSpinner);
    trialJoinPanel1.add(spinUseEveryNSections.getContainer());
    trialJoinPanel1.add(new JLabel("sections"));
    pnlTrialJoin.add(trialJoinPanel1);
    //second component
    spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
    spinTrialBinning = new LabeledSpinner(
        "Binning in X and Y: ", spinnerModel);
    spinTrialBinning.setTextMaxmimumSize(dimSpinner);
    pnlTrialJoin.add(spinTrialBinning);
    //third component
    btnTrialJoin = new MultiLineButton(TRIAL_JOIN_TEXT);
    btnTrialJoin.addActionListener(joinActionListener);
    pnlTrialJoin.addMultiLineButton(btnTrialJoin);
    //fourth component
    btnOpenTrialIn3dmod = new MultiLineButton("Open Trial in 3dmod");
    btnOpenTrialIn3dmod.addActionListener(joinActionListener);
    spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
    spinOpenTrialBinnedBy = new LabeledSpinner(
        OPEN_BINNED_BY, spinnerModel);
    pnlTrialJoin.add(createOpen3dmodPanel(spinOpenTrialBinnedBy, btnOpenTrialIn3dmod));
    //fifth component
    btnGetSubarea = new MultiLineButton("Get Subarea");
    btnGetSubarea.addActionListener(joinActionListener);
    pnlTrialJoin.addMultiLineButton(btnGetSubarea);
    pnlFinishJoin.add(pnlTrialJoin);
  }
  
  SpacedPanel createOpen3dmodPanel(LabeledSpinner spinner, MultiLineButton button) {
    SpacedPanel open3dmodPanel = new SpacedPanel(FixedDim.x0_y5, true);
    open3dmodPanel.setLayout(new BoxLayout(open3dmodPanel.getContainer(),
        BoxLayout.Y_AXIS));
    open3dmodPanel.setBorder(BorderFactory.createEtchedBorder());
    //spinner panel
    SpacedPanel spinnerPanel = new SpacedPanel(FixedDim.x5_y0, true);
    spinnerPanel.setLayout(new BoxLayout(spinnerPanel.getContainer(),
        BoxLayout.X_AXIS));
    spinner.setTextMaxmimumSize(dimSpinner);
    spinnerPanel.add(spinner);
    spinnerPanel.add(new JLabel(IN_X_AND_Y));
    open3dmodPanel.add(spinnerPanel);
    //add button
    open3dmodPanel.setComponentAlignmentX(Component.CENTER_ALIGNMENT);
    open3dmodPanel.addMultiLineButton(button);
    return open3dmodPanel;
  }

  void setNumSections(int numSections) {
    this.numSections = numSections;
    //setup
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinDensityRefSection.setModel(spinnerModel);
    spinDensityRefSection.setEnabled(numSections > 0);
    btnMakeSamples.setEnabled(numSections >= 2);
    //align
    spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinAlignmentRefSection.setModel(spinnerModel);
    //every n sections
    int zMax = pnlSectionTable.getZMax();
    spinnerModel = new SpinnerNumberModel(zMax < 1 ? 1 : zMax < 10 ? zMax : 10,
        1, zMax < 1 ? 1 : zMax, 1);
    spinUseEveryNSections.setModel(spinnerModel);
  }
  
  public ConstEtomoInteger getSizeInX() {
    EtomoInteger sizeInX = new EtomoInteger();
    sizeInX.set(ltfSizeInX.getText());
    return sizeInX;
  }
  
  public ConstEtomoInteger getSizeInY() {
    EtomoInteger sizeInY = new EtomoInteger();
    sizeInY.set(ltfSizeInY.getText());
    return sizeInY;
  }
  
  public ConstEtomoInteger getShiftInX() {
    EtomoInteger shiftInX = new EtomoInteger();
    shiftInX.set(ltfShiftInX.getText());
    return shiftInX;
  }
  
  public ConstEtomoInteger getShiftInY() {
    EtomoInteger shiftInY = new EtomoInteger();
    shiftInY.set(ltfShiftInY.getText());
    return shiftInY;
  }

  public void setSizeInX(EtomoSimpleType sizeInX) {
    ltfSizeInX.setText(sizeInX.getString(true));
  }
  
  public void setSizeInY(EtomoSimpleType sizeInY) {
    ltfSizeInY.setText(sizeInY.getString(true));
  }
  
  public void setShiftInX(EtomoSimpleType shiftInX) {
    ltfShiftInX.setText(shiftInX.getString(true));
  }
  
  public void setShiftInY(EtomoSimpleType shiftInY) {
    ltfShiftInY.setText(shiftInY.getString(true));
  }
  
  public String getInvalidReason() {
    if (invalidReason != null) {
      return invalidReason;
    }
    return pnlSectionTable.getInvalidReason();
  }
  
  public void getMetaData(JoinMetaData metaData) { 
    metaData.setRootName(ltfRootName.getText());
    metaData.setDensityRefSection(spinDensityRefSection.getValue());
    metaData.setSigmaLowFrequency(ltfSigmaLowFrequency.getText());
    metaData.setCutoffHighFrequency(ltfCutoffHighFrequency.getText());
    metaData.setSigmaHighFrequency(ltfSigmaHighFrequency.getText());
    metaData.setFullLinearTransformation(rbFullLinearTransformation.isSelected());
    metaData.setRotationTranslationMagnification(rbRotationTranslationMagnification.isSelected());
    metaData.setRotationTranslation(rbRotationTranslation.isSelected());
    metaData.setUseAlignmentRefSection(cbUseAlignmentRefSection.isSelected());
    metaData.setAlignmentRefSection(spinAlignmentRefSection.getValue());
    metaData.setSizeInX(ltfSizeInX.getText());
    metaData.setSizeInY(ltfSizeInY.getText());
    metaData.setShiftInX(ltfShiftInX.getText());
    metaData.setShiftInY(ltfShiftInY.getText());
    pnlSectionTable.getMetaData(metaData);
  }
  
  public void setMetaData(ConstJoinMetaData metaData) {
    pnlSectionTable.setMetaData(metaData);
    pnlSectionTable.enableTableButtons(ltfWorkingDir.getText());
    ltfRootName.setText(metaData.getRootName());
    spinDensityRefSection.setValue(metaData.getDensityRefSection());
    ltfSigmaLowFrequency.setText(metaData.getSigmaLowFrequency().getString(true));
    ltfCutoffHighFrequency.setText(metaData.getCutoffHighFrequency().getString(true));
    ltfSigmaHighFrequency.setText(metaData.getSigmaHighFrequency().getString(true));
    rbFullLinearTransformation.setSelected(metaData.isFullLinearTransformation());
    rbRotationTranslationMagnification.setSelected(metaData.isRotationTranslationMagnification());
    rbRotationTranslation.setSelected(metaData.isRotationTranslation());
    cbUseAlignmentRefSection.setSelected(metaData.isUseAlignmentRefSection());
    useAlignmentRefSectionAction();
    spinAlignmentRefSection.setValue(metaData.getAlignmentRefSection().getNumber());
    ltfSizeInX.setText(metaData.getSizeInX().getString(true));
    ltfSizeInY.setText(metaData.getSizeInY().getString(true));
    ltfShiftInX.setText(metaData.getShiftInX().getString(true));
    ltfShiftInY.setText(metaData.getShiftInY().getString(true));
  }
  
  public void setEnabledWorkingDir(boolean enable) {
    ltfWorkingDir.setEnabled(enable);
    btnWorkingDir.setEnabled(enable);
  }
  
  public void setEnabledRootName(boolean enable) {
    ltfRootName.setEnabled(enable);
  }

  public Container getContainer() {
    return rootPanel;
  }

  String getWorkingDirName() {
    return ltfWorkingDir.getText();
  }
  
  public File getWorkingDir() {
    return new File(ltfWorkingDir.getText());
  }
  
  public String getRootName() {
    return ltfRootName.getText();
  }
  
  public void abortAddSection() {
    pnlSectionTable.setEnabledAddSection(true);
  }
  
  public void enableMidas() {
    btnMidas.setEnabled(true);
  }
  
  public void addSection(File tomogram) {
    pnlSectionTable.addSection(tomogram);
  }

  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
  }

  /**
   * Handle actions
   * @param event
   */
  private void action(ActionEvent event) {
    String command = event.getActionCommand();
    if (command.equals(btnMakeSamples.getActionCommand())) {
      joinManager.setWorkingDir(ltfWorkingDir.getText());
      joinManager.makejoincom();
    }
    else if (command.equals(btnOpenSample.getActionCommand())) {
      joinManager.imodOpenJoinSamples();
    }
    else if (command.equals(btnOpenSampleAverages.getActionCommand())) {
      joinManager.imodOpenJoinSampleAverages();
    }
    else if (command.equals(btnInitialAutoAlignment.getActionCommand())) {
      btnMidas.setEnabled(false);
      joinManager.xfalignInitial();
    }
    else if (command.equals(btnMidas.getActionCommand())) {
      joinManager.midasSample();
    }
    else if (command.equals(btnRefineAutoAlignment.getActionCommand())) {
      btnMidas.setEnabled(false);
      joinManager.xfalignRefine();
    }
    else if (command.equals(btnRevertToMidas.getActionCommand())) {
      joinManager.revertXfFileToMidas();
    }
    else if (command.equals(btnRevertToEmpty.getActionCommand())) {
      joinManager.revertXfFileToEmpty();
    }
    else if (command.equals(btnFinishJoin.getActionCommand())) {
      joinManager.runFinishjoin(FinishjoinParam.FINISH_JOIN_MODE, FINISH_JOIN_TEXT);
    }
    else if (command.equals(btnOpenIn3dmod.getActionCommand())) {
      joinManager.imodOpenJoin();
    }
    else if (command.equals(btnGetMaxSize.getActionCommand())) {
      joinManager.runFinishjoin(FinishjoinParam.MAX_SIZE_MODE, GET_MAX_SIZE_TEXT);
    }
    else if (command.equals(btnTrialJoin.getActionCommand())) {
      joinManager.runFinishjoin(FinishjoinParam.TRIAL_MODE, TRIAL_JOIN_TEXT);
    }
    else if (command.equals(btnOpenTrialIn3dmod.getActionCommand())) {
      
    }
    else if (command.equals(btnGetSubarea.getActionCommand())) {
      
    }
    else {
      throw new IllegalStateException("Unknown command " + command);
    }
  }

  private void workingDirAction() {
    //  Open up the file chooser in the working directory
    JFileChooser chooser = new JFileChooser(new File(joinManager.getPropertyUserDir()));
    chooser.setPreferredSize(FixedDim.fileChooser);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = chooser.showOpenDialog(rootPanel);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File workingDir = chooser.getSelectedFile();
      try {
        ltfWorkingDir.setText(workingDir.getAbsolutePath());
      }
      catch (Exception excep) {
        excep.printStackTrace();
      }
      this.pnlSectionTable.enableTableButtons(ltfWorkingDir.getText());
    }
  }
  
  private void useAlignmentRefSectionAction() {
    spinAlignmentRefSection.setEnabled(cbUseAlignmentRefSection.isSelected());
  }

  //
  //  Action listener adapters
  //
  class JoinActionListener implements ActionListener {

    JoinDialog adaptee;

    JoinActionListener(JoinDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.action(event);
    }
  }

  class WorkingDirActionListener implements ActionListener {

    JoinDialog adaptee;

    WorkingDirActionListener(JoinDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.workingDirAction();
    }
  }
  
  class UseAlignmentRefSectionActionListener implements ActionListener {

    JoinDialog adaptee;

    UseAlignmentRefSectionActionListener(JoinDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.useAlignmentRefSectionAction();
    }
  }

  /**
   * Connect tab state changes to the appropriate dialog method
   */
  class TabChangeListener implements ChangeListener {
    JoinDialog adaptee;
    public TabChangeListener(JoinDialog dialog) {
      adaptee = dialog;
    }
    
     public void stateChanged(ChangeEvent event) {
       adaptee.changeTab(event);
     }
  }
}