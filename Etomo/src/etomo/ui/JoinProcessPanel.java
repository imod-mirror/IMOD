package etomo.ui;

import java.awt.event.ActionEvent;

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
* <p> Revision 1.3  2005/04/16 01:55:45  sueh
* <p> bug# 615 Add empty showBothAxis() to implement abstract function.
* <p>
* <p> Revision 1.2  2004/11/19 23:56:48  sueh
* <p> bug# 520 merging Etomo_3-4-6_JOIN branch to head.
* <p>
* <p> Revision 1.1.2.4  2004/10/11 02:14:13  sueh
* <p> bug# 520 moved responsibility for the manager member variable to child
* <p> classes.  Used abstract functions to use this variable in the base class.
* <p> This is more reliable and doesn't require casting.
* <p>
* <p> Revision 1.1.2.3  2004/09/29 19:34:29  sueh
* <p> bug# 520 updated comment
* <p>
* <p> Revision 1.1.2.2  2004/09/15 22:36:45  sueh
* <p> bug# 520 calling createPrcoessControlPanel and initializePanels in
* <p> constructor
* <p>
* <p> Revision 1.1.2.1  2004/09/08 20:06:09  sueh
* <p> bug# 520 AxisProcessPanel for Join
* <p> </p>
*/


public class JoinProcessPanel extends AxisProcessPanel {
  public static  final String  rcsid =  "$Id$";
  /**
   * @param joinManager
   * @param axis
   */
  public JoinProcessPanel(JoinManager joinManager, AxisID axis) {
    super(axis, joinManager);
    createProcessControlPanel();
    initializePanels();
  }
  
  void showBothAxis() {
  }
  
  /**
   * 
   * @param event
   */
  protected void buttonKillAction(ActionEvent event) {
    manager.kill(axisID);
  }
}
