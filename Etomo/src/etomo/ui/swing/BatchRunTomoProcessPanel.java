package etomo.ui.swing;

import etomo.BaseManager;
import etomo.type.AxisID;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2013</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
final class BatchRunTomoProcessPanel extends AxisProcessPanel {
  public static final String rcsid = "$Id:$";
  
  BatchRunTomoProcessPanel(final BaseManager manager) {
    super(AxisID.ONLY, manager, true,false);
    createProcessControlPanel();
    showBothAxis();
    initializePanels();
  }
}
