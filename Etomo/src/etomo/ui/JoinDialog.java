package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;

import etomo.JoinManager;
import etomo.type.AxisID;

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
 * <p> Revision 1.1.2.2  2004/09/15 22:40:07  sueh
 * <p> bug# 520 added create panel functions
 * <p> </p>
 */
public class JoinDialog implements ContextMenu {
  public static final String rcsid =
    "$Id$";

  private JPanel rootPanel;
  private JTabbedPane tabPane;
  private JPanel pnlSetupTab;
  private JPanel pnlSetup;
  private JPanel pnlAlignTab;
  private JPanel pnlAlign;
  private JPanel pnlJoinTab;
  private JPanel pnlJoin;
  private SectionTablePanel pnlSectionTable;
  private JoinActionListener joinActionListener = new JoinActionListener(this);
  
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
    createSetupTabPanel();
    tabPane.addTab("Setup", pnlSetupTab);
    createAlignTabPanel();
    tabPane.addTab("Align", pnlAlignTab);
    createJoinTabPanel();
    tabPane.addTab("Join", pnlJoinTab);
  }
  
  private void createSetupTabPanel() {
    pnlSetupTab = new JPanel();
    pnlSetupTab.setLayout(new BoxLayout(pnlSetupTab, BoxLayout.X_AXIS));
    pnlSetupTab.add(Box.createRigidArea(FixedDim.x5_y0));
    createSetupPanel();
    pnlSetupTab.add(pnlSetup);
    pnlSetupTab.add(Box.createRigidArea(FixedDim.x5_y0));
  }
  
  private void createSetupPanel() {
    pnlSetup = new JPanel();
    pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
    pnlSetup.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlSetup.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlSectionTable = new SectionTablePanel(joinManager);
    pnlSetup.add(pnlSectionTable.getContainer());
    pnlSetup.add(Box.createRigidArea(FixedDim.x0_y5));
  }
  
  private void createAlignTabPanel() {
    pnlAlignTab = new JPanel();
  }
  
  private void createJoinTabPanel() {
    pnlJoinTab = new JPanel();
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

  //
  //  Action listener adapters
  //
  class JoinActionListener implements ActionListener {

    JoinDialog adaptee;

    JoinActionListener(JoinDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      action(event);
    }
  }
}
