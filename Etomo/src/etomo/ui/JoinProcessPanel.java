package etomo.ui;

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
* <p> Revision 1.1.2.1  2004/09/08 20:06:09  sueh
* <p> bug# 520 AxisProcessPanel for Join
* <p> </p>
*/


public class JoinProcessPanel extends AxisProcessPanel {
  public static  final String  rcsid =  "$Id$";
  
  /**
   * @param appManager
   * @param axis
   */
  public JoinProcessPanel(JoinManager joinManager, AxisID axis) {
    super(joinManager, axis);
    createProcessControlPanel();
    initializePanels();
  }
}
