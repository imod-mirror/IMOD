package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import etomo.JoinManager;
import etomo.type.AxisID;
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

  private static ImageIcon iconFolder = new ImageIcon(ClassLoader
      .getSystemResource("images/openFile.gif"));
  private static Dimension dimButton = UIParameters.getButtonDimension();
  private static Dimension dimSpinner = UIParameters.getSpinnerDimension();
  
  private JPanel rootPanel;
  private JTabbedPane tabPane;
  private DoubleSpacedPanel pnlSetup;
  private SectionTablePanel pnlSectionTable;
  private JPanel pnlAlign;
  private JPanel pnlJoin;

  private JButton btnWorkingDir;
  private MultiLineToggleButton btnMakeJoin;
  
  private JCheckBox cbUseDensityRefSection;
  private LabeledSpinner spinDensityRefSection;
  private LabeledTextField ltfWorkingDir;
  private LabeledTextField ltfRootName;
  
  private int numSections = 0;
  
  private JoinActionListener joinActionListener = new JoinActionListener(this);
  private UseDensityRefSectionActionListener useDensityRefSectionActionListener = new UseDensityRefSectionActionListener(
      this);
  private WorkingDirActionListener workingDirActionListener = new WorkingDirActionListener(
      this);

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
    tabPane.setBorder(new BeveledBorder("Join").getBorder());
    createSetupPanel();
    tabPane.addTab("Setup", pnlSetup.getContainer());
    createAlignPanel();
    tabPane.addTab("Align", pnlAlign);
    createJoinPanel();
    tabPane.addTab("Join", pnlJoin);
  }

  private void createSetupPanel() {
    pnlSetup = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5);
    //first component
    SpacedPanel pnlFirst = new SpacedPanel(FixedDim.x5_y0);
    pnlFirst.setLayout(new BoxLayout(pnlFirst.getContainer(),
        BoxLayout.X_AXIS));
    ltfWorkingDir = new LabeledTextField("Working Directory: ");
    pnlFirst.add(ltfWorkingDir.getContainer());
    btnWorkingDir = new JButton(iconFolder);
    btnWorkingDir.setPreferredSize(FixedDim.folderButton);
    btnWorkingDir.setMaximumSize(FixedDim.folderButton);
    btnWorkingDir.addActionListener(workingDirActionListener);
    pnlFirst.add(btnWorkingDir);
    pnlSetup.add(pnlFirst.getContainer());
    //second component
    ltfRootName = new LabeledTextField("Root name for output file: ");
    pnlSetup.add(ltfRootName.getContainer());
    //third component    
    pnlSectionTable = new SectionTablePanel(this, joinManager);
    pnlSetup.add(pnlSectionTable.getContainer());
    //fourth component
    JPanel pnlFourth = new JPanel();
    pnlFourth.setLayout(new BoxLayout(pnlFourth, BoxLayout.X_AXIS));
    cbUseDensityRefSection = new JCheckBox();
    cbUseDensityRefSection
        .addActionListener(useDensityRefSectionActionListener);
    pnlFourth.add(cbUseDensityRefSection);
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinDensityRefSection = new LabeledSpinner(
        "Reference section for density matching: ", spinnerModel);
    enableDensityRefSection();
    spinDensityRefSection.setTextMaxmimumSize(dimSpinner);
    pnlFourth.add(spinDensityRefSection.getContainer());
    pnlSetup.add(pnlFourth);
    //fifth component
    btnMakeJoin = new MultiLineToggleButton("Make Join");
    btnMakeJoin.addActionListener(joinActionListener);
    UIUtilities.setButtonSize(btnMakeJoin, dimButton);
    btnMakeJoin.setAlignmentX(Component.CENTER_ALIGNMENT);
    enableMakeJoin();
    pnlSetup.add(btnMakeJoin);
  }


  private void createAlignPanel() {
    pnlAlign = new JPanel();
  }

  private void createJoinPanel() {
    pnlJoin = new JPanel();
  }

  private void enableDensityRefSection() {
    spinDensityRefSection.setEnabled(cbUseDensityRefSection.isSelected()
        && numSections >= 1);
  }
  
  private void enableMakeJoin() {
    btnMakeJoin.setEnabled(numSections >= 2);
  }

  void setNumSections(int numSections) {
    this.numSections = numSections;
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinDensityRefSection.setModel(spinnerModel);
    enableDensityRefSection();
    enableMakeJoin();
  }
  
  public void retrieveData(JoinMetaData joinMetaData) {
    joinMetaData.setUseDensityRefSection(cbUseDensityRefSection.isSelected());
    joinMetaData.setDensityRefSection(spinDensityRefSection.getValue());
    joinMetaData.setWorkingDir(ltfWorkingDir.getText());
    joinMetaData.setRootName(ltfRootName.getText());
    pnlSectionTable.retrieveData(joinMetaData);
  }

  public Container getContainer() {
    return rootPanel;
  }
  
  String getWorkingDir() {
    return ltfWorkingDir.getText();
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
    if (command.equals(btnMakeJoin.getActionCommand())) {
      joinManager.startJoin();
    }
  }

  private void useDensityRefSectionAction() {
    enableDensityRefSection();
  }
  
  private void workingDirAction() {
    //  Open up the file chooser in the working directory
    JFileChooser chooser = new JFileChooser(new File(System
      .getProperty("user.dir")));
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

  class UseDensityRefSectionActionListener implements ActionListener {

    JoinDialog adaptee;

    UseDensityRefSectionActionListener(JoinDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.useDensityRefSectionAction();
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

}