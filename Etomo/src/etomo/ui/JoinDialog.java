package etomo.ui;

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
 * <p> $Log$ </p>
 */
public class JoinDialog implements ContextMenu {
  public static final String rcsid =
    "$Id$";

  private JPanel rootPanel;
  private JTabbedPane tabPane;
  private JPanel pnlSetup;
  private JPanel pnlAlign;
  private JPanel pnlJoin;
  private JPanel pnlSections;
  private SectionTablePanel pnlSectionTable;
  
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
    tabPane.addTab("Setup", pnlSetup);
    createAlignPanel();
    tabPane.addTab("Align", pnlAlign);
    createJoinPanel();
    tabPane.addTab("Join", pnlJoin);
  }
  
  private void createSetupPanel() {
    pnlSetup = new JPanel();
    createSectionsPanel();
    pnlSetup.add(pnlSections);
  }
  
  private void createAlignPanel() {
    pnlAlign = new JPanel();
  }
  
  private void createJoinPanel() {
    pnlJoin = new JPanel();
  }
  
  private void createSectionsPanel() {
    pnlSections = new JPanel();
    pnlSections.setBorder(new EtchedBorder("").getBorder());
    pnlSectionTable = new SectionTablePanel();
    pnlSections.add(pnlSectionTable.getContainer());
  }
  
  public Container getContainer() {
    return rootPanel;
  }

  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
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
    }
  }
}
