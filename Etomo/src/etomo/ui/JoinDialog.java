package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import etomo.JoinManager;
import etomo.type.AxisID;
import etomo.type.ConstJoinMetaData;
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
  private SpacedPanel pnlSetupComponent1;
  private SpacedPanel pnlAlignComponent1;
  private DoubleSpacedPanel pnlXfalign;
  private ButtonGroup bgSearchFor;
  private JLabel lblSearchFor;
  private DoubleSpacedPanel pnlFinishJoin;
  private SpacedPanel pnlJoinComponent1;
  private SpacedPanel pnlJoinComponent1A;
  private JPanel pnlFinishJoinComponent1;
  private SpacedPanel pnlFinishJoinComponent2;
  private SpacedPanel pnlFinishJoinComponent3;
  private JLabel lblInXAndY;
  
  private JButton btnWorkingDir;
  private MultiLineToggleButton btnMakeSamples;
  private MultiLineButton btnOpenSamples;
  private MultiLineButton btnOpenSampleAverages;
  private MultiLineButton btnInitialAutoAlignment;
  private MultiLineButton btnMidas;
  private MultiLineButton btnRefineAutoAlignment;
  private MultiLineButton btnRevertAutoAlignment;
  private MultiLineButton btnFinishJoin;
  private MultiLineButton btnOpenIn3dmod;

  private LabeledSpinner spinDensityRefSection;
  private LabeledTextField ltfWorkingDir;
  private LabeledTextField ltfRootName;
  private LabeledTextField ltfSigmaLowFrequency;
  private LabeledTextField ltfCutoffHighFrequency;
  private LabeledTextField ltfSigmaHighFrequency;
  private JRadioButton rbFullLinearTransformation;
  private JRadioButton rbRotationTranslationMagnification;
  private JRadioButton rbRotationTranslation;
  private JCheckBox cbUseAlignmentRefSection;
  private LabeledSpinner spinAlignmentRefSection;
  private LabeledTextField ltfSizeInX;
  private LabeledTextField ltfSizeInY;
  private LabeledTextField ltfOffsetInX;
  private LabeledTextField ltfOffsetInY;
  private LabeledSpinner spinOpenBinnedBy;
  
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
    axisID = AxisID.ONLY;
    this.joinManager = joinManager;
    createRootPanel();
    joinManager.packMainWindow();
  }

  private void createRootPanel() {
    rootPanel = new JPanel();
    //  Mouse adapter for context menu
    GenericMouseAdapter mouseAdapter = new GenericMouseAdapter(this);
    rootPanel.addMouseListener(mouseAdapter);
    createTabPane();
    rootPanel.add(tabPane);
  }

  private void createTabPane() {
    tabPane = new JTabbedPane();
    TabChangeListener tabChangeListener = new TabChangeListener(this);
    tabPane.addChangeListener(tabChangeListener);
    tabPane.setBorder(new BeveledBorder("Join").getBorder());
    createSetupPanel();
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
  }

  private void createSetupPanel() {
    pnlSetup = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5);
    //first component
    pnlSetupComponent1 = new SpacedPanel(FixedDim.x5_y0);
    pnlSetupComponent1
        .setLayout(new BoxLayout(pnlSetupComponent1.getContainer(), BoxLayout.X_AXIS));
    ltfWorkingDir = new LabeledTextField("Working Directory: ");
    pnlSetupComponent1.add(ltfWorkingDir);
    btnWorkingDir = new JButton(iconFolder);
    btnWorkingDir.setPreferredSize(FixedDim.folderButton);
    btnWorkingDir.setMaximumSize(FixedDim.folderButton);
    btnWorkingDir.addActionListener(workingDirActionListener);
    pnlSetupComponent1.add(btnWorkingDir);
    pnlSetup.add(pnlSetupComponent1);
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
    pnlSetup.add(pnlSetupComponent1);
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
    pnlAlignComponent1 = new SpacedPanel(FixedDim.x5_y0);
    pnlAlignComponent1.setLayout(new BoxLayout(pnlAlignComponent1.getContainer(), BoxLayout.X_AXIS));
    btnOpenSamples = new MultiLineButton("Open Samples in 3dmod");
    btnOpenSamples.addActionListener(joinActionListener);
    btnOpenSampleAverages = new MultiLineButton("Open Sample Averages in 3dmod");
    btnOpenSampleAverages.addActionListener(joinActionListener);
    pnlAlignComponent1.addMultiLineButton(btnOpenSamples);
    pnlAlignComponent1.addMultiLineButton(btnOpenSampleAverages);
    //third component
    pnlXfalign = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5, new EtchedBorder("Xfalign Parameters").getBorder(), false);
    ltfSigmaLowFrequency = new LabeledTextField("Sigma for low-frequency filter: ");
    pnlXfalign.add(ltfSigmaLowFrequency);
    ltfCutoffHighFrequency = new LabeledTextField("Cutoff for high-frequency filter: ");
    pnlXfalign.add(ltfCutoffHighFrequency);
    ltfSigmaHighFrequency = new LabeledTextField("Sigma for high-frequency filter: ");
    pnlXfalign.add(ltfSigmaHighFrequency);
    bgSearchFor = new ButtonGroup();
    rbFullLinearTransformation = new JRadioButton("Full linear transformation");
    rbRotationTranslationMagnification = new JRadioButton("Rotation/translation/magnification");
    rbRotationTranslation = new JRadioButton("Rotation/translation");
    bgSearchFor.add(rbFullLinearTransformation);
    bgSearchFor.add(rbRotationTranslationMagnification);
    bgSearchFor.add(rbRotationTranslation);
    lblSearchFor = new JLabel("Search For:");
    pnlXfalign.add(lblSearchFor);
    pnlXfalign.add(rbFullLinearTransformation, false);
    pnlXfalign.add(rbRotationTranslationMagnification, false);
    pnlXfalign.add(rbRotationTranslation);
    //fourth component
    btnInitialAutoAlignment = new MultiLineButton("Initial Auto Alignment");
    btnInitialAutoAlignment.addActionListener(joinActionListener);
    //fifth component
    btnMidas = new MultiLineButton("Midas");
    btnMidas.addActionListener(joinActionListener);
    //sixth component
    btnRefineAutoAlignment = new MultiLineButton("Refine Auto Alignment");
    btnRefineAutoAlignment.addActionListener(joinActionListener);
    //seventh component
    btnRevertAutoAlignment = new MultiLineButton("Revert Auto Alignment");
    btnRevertAutoAlignment.addActionListener(joinActionListener);
  }
  
  private void addAlignPanelComponents() {
    //first component
    pnlAlign.add(pnlSectionTable.getRootPanel());
    pnlSectionTable.setCurTab(ALIGN_TAB);
    pnlSectionTable.displayCurTab();
    //second component
    pnlAlign.add(pnlAlignComponent1);
    //third component
    pnlAlign.add(pnlXfalign);
    //fourth component
    pnlAlign.setComponentAlignmentX(Component.CENTER_ALIGNMENT);
    pnlAlign.addMultiLineButton(btnInitialAutoAlignment);
    //fifth component
    pnlAlign.addMultiLineButton(btnMidas);
    //sixth component
    pnlAlign.addMultiLineButton(btnRefineAutoAlignment);
    //seventh component
    pnlAlign.addMultiLineButton(btnRevertAutoAlignment);
  }

  private void createJoinPanel() {
    pnlJoin = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5);
    //second component
    createFinishJoinPanel();
    addFinishJoinPanelComponents();
    //third component
    pnlJoinComponent1 = new SpacedPanel(FixedDim.x0_y5, true);
    pnlJoinComponent1.setLayout(new BoxLayout(pnlJoinComponent1.getContainer(), BoxLayout.Y_AXIS));
    pnlJoinComponent1.setBorder(BorderFactory.createEtchedBorder());
    pnlJoinComponent1A = new SpacedPanel(FixedDim.x5_y0, true);
    pnlJoinComponent1A.setLayout(new BoxLayout(pnlJoinComponent1A.getContainer(), BoxLayout.X_AXIS));
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
    spinOpenBinnedBy = new LabeledSpinner(
        "Open binned by ", spinnerModel);
    spinOpenBinnedBy.setTextMaxmimumSize(dimSpinner);
    pnlJoinComponent1A.add(spinOpenBinnedBy);
    lblInXAndY = new JLabel("in X and Y");
    pnlJoinComponent1A.add(lblInXAndY);
    pnlJoinComponent1.add(pnlJoinComponent1A);
    pnlJoinComponent1.setComponentAlignmentX(Component.CENTER_ALIGNMENT);
    btnOpenIn3dmod = new MultiLineButton("Open in 3dmod");
    pnlJoinComponent1.addMultiLineButton(btnOpenIn3dmod);
  }
  
  private void addJoinPanelComponents() {
    //first component
    pnlJoin.add(pnlSectionTable.getRootPanel());
    pnlSectionTable.setCurTab(JOIN_TAB);
    pnlSectionTable.displayCurTab();
    //second component
    pnlJoin.add(pnlFinishJoin);
    //third component
    pnlJoin.add(pnlJoinComponent1);
  }
  
  private void createFinishJoinPanel() {
    pnlFinishJoin = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5, BorderFactory.createEtchedBorder());
    //first component
    pnlFinishJoinComponent1 = new JPanel();
    pnlFinishJoinComponent1.setLayout(new BoxLayout(pnlFinishJoinComponent1, BoxLayout.X_AXIS));
    cbUseAlignmentRefSection = new JCheckBox();
    cbUseAlignmentRefSection.addActionListener(useAlignmentRefSectionActionListener);
    pnlFinishJoinComponent1.add(cbUseAlignmentRefSection);
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinAlignmentRefSection = new LabeledSpinner(
        "Reference section for alignment: ", spinnerModel);
    spinAlignmentRefSection.setTextMaxmimumSize(dimSpinner);
    spinAlignmentRefSection.setEnabled(false);
    pnlFinishJoinComponent1.add(spinAlignmentRefSection.getContainer());
    //second component
    pnlFinishJoinComponent2 = new SpacedPanel(FixedDim.x5_y0);
    pnlFinishJoinComponent2.setLayout(new BoxLayout(pnlFinishJoinComponent2.getContainer(), BoxLayout.X_AXIS));
    ltfSizeInX = new LabeledTextField("Size in X: ");
    pnlFinishJoinComponent2.add(ltfSizeInX);
    ltfSizeInY = new LabeledTextField("Y: ");
    pnlFinishJoinComponent2.add(ltfSizeInY);
    //third component
    pnlFinishJoinComponent3 = new SpacedPanel(FixedDim.x5_y0);
    pnlFinishJoinComponent3.setLayout(new BoxLayout(pnlFinishJoinComponent3.getContainer(), BoxLayout.X_AXIS));
    ltfOffsetInX = new LabeledTextField("Offset in X: ");
    pnlFinishJoinComponent3.add(ltfOffsetInX);
    ltfOffsetInY = new LabeledTextField("Y: ");
    pnlFinishJoinComponent3.add(ltfOffsetInY);
    //fourth component
    btnFinishJoin = new MultiLineButton("Finish Join");
  }
  
  private void addFinishJoinPanelComponents() {
    //first component
    pnlFinishJoin.add(pnlFinishJoinComponent1);
    //second component
    pnlFinishJoin.add(pnlFinishJoinComponent2);
    //third component
    pnlFinishJoin.add(pnlFinishJoinComponent3);
    //fourth component
    pnlFinishJoin.setComponentAlignmentX(Component.CENTER_ALIGNMENT);
    pnlFinishJoin.addMultiLineButton(btnFinishJoin);
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
  }
  
  public String getInvalidReason() {
    if (invalidReason != null) {
      return invalidReason;
    }
    return pnlSectionTable.getInvalidReason();
  }
  
  public void getMetaData(JoinMetaData metaData) { 
    metaData.setWorkingDir(ltfWorkingDir.getText());
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
    metaData.setOffsetInX(ltfOffsetInX.getText());
    metaData.setOffsetInY(ltfOffsetInY.getText());
    pnlSectionTable.getMetaData(metaData);
  }
  
  public void setMetaData(ConstJoinMetaData metaData) {
    ltfWorkingDir.setText(metaData.getWorkingDir());
    ltfRootName.setText(metaData.getRootName());
    spinDensityRefSection.setValue(metaData.getDensityRefSection());
    ltfSigmaLowFrequency.setText(metaData.getSigmaLowFrequencyString());
    ltfCutoffHighFrequency.setText(metaData.getCutoffHighFrequencyString());
    ltfSigmaHighFrequency.setText(metaData.getSigmaHighFrequencyString());
    rbFullLinearTransformation.setSelected(metaData.isFullLinearTransformation());
    rbRotationTranslationMagnification.setSelected(metaData.isRotationTranslationMagnification());
    rbRotationTranslation.setSelected(metaData.isRotationTranslation());
    cbUseAlignmentRefSection.setSelected(metaData.isUseAlignmentRefSection());
    spinAlignmentRefSection.setValue(metaData.getAlignmentRefSection());
    ltfSizeInX.setText(metaData.getSizeInXString());
    ltfSizeInY.setText(metaData.getSizeInYString());
    ltfOffsetInX.setText(metaData.getOffsetInXString());
    ltfOffsetInY.setText(metaData.getOffsetInYString());
    
    pnlSectionTable.setMetaData(metaData);
    pnlSectionTable.enableTableButtons(ltfWorkingDir.getText());
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
  
  File getWorkingDir() {
    return new File(ltfWorkingDir.getText());
  }
  
  public void abortAddSection() {
    pnlSectionTable.setEnabledAddSection(true);
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
    else if (command.equals(btnOpenSamples.getActionCommand())) {
      joinManager.imodOpenJoinSamples();
    }
    else if (command.equals(btnOpenSampleAverages.getActionCommand())) {
      joinManager.imodOpenJoinSampleAverages();
    }
    else if (command.equals(btnInitialAutoAlignment.getActionCommand())) {
      joinManager.xfalign();
    }
    else if (command.equals(btnMidas.getActionCommand())) {
      //TODO
    }
    else if (command.equals(btnRefineAutoAlignment.getActionCommand())) {
      //TODO
    }
    else if (command.equals(btnRevertAutoAlignment.getActionCommand())) {
      //TODO
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