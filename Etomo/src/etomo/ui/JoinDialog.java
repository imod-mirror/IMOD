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
  private DoubleSpacedPanel pnlMakeJoin;
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
  private WorkingDirActionListener workingDirActionListener = new WorkingDirActionListener(this);

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
    pnlSectionTable = new SectionTablePanel(this, joinManager);
    pnlSetup.add(pnlSectionTable.getContainer());
    createMakeJoinPanel();
    pnlSetup.add(pnlMakeJoin);
  }

  private void createMakeJoinPanel() {
    pnlMakeJoin = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5,
        BorderFactory.createEtchedBorder());
    //first component
    JPanel pnlFirst = new JPanel();
    pnlFirst.setLayout(new BoxLayout(pnlFirst, BoxLayout.X_AXIS));
    cbUseDensityRefSection = new JCheckBox();
    cbUseDensityRefSection
        .addActionListener(useDensityRefSectionActionListener);
    pnlFirst.add(cbUseDensityRefSection);
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1,
        numSections < 1 ? 1 : numSections, 1);
    spinDensityRefSection = new LabeledSpinner(
        "Reference section for density matching: ", spinnerModel);
    enableDensityRefSection();
    spinDensityRefSection.setTextMaxmimumSize(dimSpinner);
    pnlFirst.add(spinDensityRefSection.getContainer());
    pnlMakeJoin.add(pnlFirst);
    //second component
    SpacedPanel pnlSecond = new SpacedPanel(FixedDim.x5_y0);
    pnlSecond.setLayout(new BoxLayout(pnlSecond.getContainer(), BoxLayout.X_AXIS));
    ltfWorkingDir = new LabeledTextField("Working Directory: ");
    pnlSecond.add(ltfWorkingDir.getContainer());
    btnWorkingDir = new JButton(iconFolder);
    btnWorkingDir.setPreferredSize(FixedDim.folderButton);
    btnWorkingDir.setMaximumSize(FixedDim.folderButton);
    btnWorkingDir.addActionListener(workingDirActionListener);
    pnlSecond.add(btnWorkingDir);
    pnlMakeJoin.add(pnlSecond);
    //third component
    ltfRootName = new LabeledTextField("Root name for output file: ");
    pnlMakeJoin.add(ltfRootName.getContainer());
    //fourth component
    btnMakeJoin = new MultiLineToggleButton("Make Join");
    UIUtilities.setButtonSize(btnMakeJoin, dimButton);
    btnMakeJoin.setAlignmentX(Component.CENTER_ALIGNMENT);
    enableMakeJoin();
    pnlMakeJoin.add(btnMakeJoin);
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