package etomo.ui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2002, 2003, 2004</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public class JoinDialog {
  public static  final String  rcsid =  "$Id$";
  
  private JTabbedPane tabPane = new JTabbedPane();
  private JPanel pnlSetup = new JPanel();
  private JPanel pnlAlign = new JPanel();
  private JPanel pnlFinal = new JPanel();
  
  JoinDialog() {
    tabPane.setBorder(new EtchedBorder("Join").getBorder());
    //  Create the tabs
    createSetupTab();
    createAlignTab();
    createFinalTab();
  }
  
  private void createSetupTab() {
    tabPane.addTab("Setup", pnlSetup);
  }
  
  private void createAlignTab() {
    tabPane.addTab("Align", pnlAlign);
  }
  
  private void createFinalTab() {
    tabPane.addTab("Final", pnlFinal);
  }

}
