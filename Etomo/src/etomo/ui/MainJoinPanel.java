package etomo.ui;

import javax.swing.JPanel;

import etomo.EtomoDirector;
import etomo.JoinManager;
import etomo.type.AxisID;
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
* <p> $Log$
* <p> Revision 1.1.2.1  2004/09/08 20:09:02  sueh
* <p> bug# 520 MainPanel for Join
* <p> </p>
*/

public class MainJoinPanel extends MainPanel {
  public static  final String  rcsid =  "$Id$";
  
  //convenience variables set to super class member variables
  //use through cast functions
  private JoinManager joinManager = null;

  /**
   * @param joinManager
   */
  public MainJoinPanel(JoinManager joinManager) {
    super(joinManager);
  }
  
  protected void createAxisPanelA(AxisID axisID) {
    axisPanelA = new JoinProcessPanel((JoinManager) manager, axisID);
  }

  protected void createAxisPanelB() {
  }
  
  private JoinManager castManager() {
    if (manager == null) {
      throw new NullPointerException();
    }
    if (joinManager == null) {
      joinManager = (JoinManager) manager;
    }
    return joinManager;
  }
  
  /**
   * Open the setup panel
   */
  public void openPanel(JPanel panel) {
    scrollA.add(panel);
    revalidate();
    EtomoDirector.getMainFrame().pack();
  }


}
