package etomo.ui.swing;

import etomo.BaseManager;
import etomo.type.AxisID;
import etomo.type.InterfaceType;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2012</p>
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
public final class SerialSectionsProcessPanel extends AxisProcessPanel {
  SerialSectionsProcessPanel(BaseManager manager) {
    super(AxisID.ONLY, manager, false, true, InterfaceType.SERIAL_SECTIONS);
    createProcessControlPanel();
    showBothAxis();
    initializePanels();
  }
  
  void showBothAxis() {
    setBackground(Colors.getBackgroundSerialSections());
  }
}
