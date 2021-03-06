package etomo.ui.swing;

import etomo.FrontPageManager;
import etomo.type.AxisID;
import etomo.type.InterfaceType;

/**
* <p>Description: </p>
* 
 * <p>Copyright: Copyright 2008 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
* 
* <p> $Log$
* <p> Revision 1.1  2010/11/13 16:07:34  sueh
* <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
* <p>
* <p> Revision 1.2  2010/07/02 03:18:35  sueh
* <p> bug# 1388 Constructing super class with popupChunkWarnings equal to true.
* <p>
* <p> Revision 1.1  2009/10/27 20:42:18  sueh
* <p> bug# 1275 Required class for FrontPageManager.
* <p> </p>
*/
final class FrontPageProcessPanel extends AxisProcessPanel {
  FrontPageProcessPanel(final FrontPageManager manager) {
    super(AxisID.ONLY, manager, true, true, InterfaceType.FRONT_PAGE);
    initializePanels();
  }

  void createProcessControlPanel() {}
}
