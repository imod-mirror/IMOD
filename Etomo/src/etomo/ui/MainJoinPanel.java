package etomo.ui;

import etomo.JoinManager;
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

public class MainJoinPanel extends MainPanel {
  public static  final String  rcsid =  "$Id$";
  
  JoinManager joinManager = null;
  JoinProcessPanel axisPanelA = null;
  /**
   * @param joinManager
   */
  public MainJoinPanel(JoinManager joinManager) {
    super(joinManager);
  }

}
