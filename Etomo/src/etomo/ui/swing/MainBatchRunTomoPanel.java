package etomo.ui.swing;

import etomo.BaseManager;
import etomo.process.ProcessState;
import etomo.storage.DataFileFilter;
import etomo.type.AxisID;
import etomo.type.InterfaceType;

/**
* <p>Description: </p>
* 
 * <p>Copyright: Copyright 2013 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
* 
* <p> $Log$ </p>
*/
public final class MainBatchRunTomoPanel extends MainPanel {
  private final BaseManager manager;

  private BatchRunTomoProcessPanel axisPanelA = null;

  public MainBatchRunTomoPanel(final BaseManager manager) {
    super(manager);
    this.manager = manager;
  }

  void addAxisPanelA() {
    scrollA.add(axisPanelA.getContainer());
  }

  void addAxisPanelB() {}

  void createAxisPanelA(final AxisID axisID) {
    axisPanelA = new BatchRunTomoProcessPanel(manager, InterfaceType.BATCH_RUN_TOMO);
  }

  public void setState(final ProcessState processState, final AxisID axisID,
    final AbstractParallelDialog batchRunTomoDialog) {}

  void createAxisPanelB() {}

  AxisProcessPanel getAxisPanelA() {
    return axisPanelA;
  }

  AxisProcessPanel getAxisPanelB() {
    return null;
  }

  DataFileFilter getDataFileFilter() {
    return null;
  }

  boolean hideAxisPanelA() {
    return axisPanelA.hide();
  }

  boolean hideAxisPanelB() {
    return true;
  }

  boolean isAxisPanelANull() {
    return axisPanelA == null;
  }

  boolean isAxisPanelBNull() {
    return true;
  }

  AxisProcessPanel mapBaseAxis(final AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      return null;
    }
    return axisPanelA;
  }

  void resetAxisPanels() {
    axisPanelA = null;
  }

  public void saveDisplayState() {}

  void showAxisPanelA() {
    axisPanelA.show();
  }

  void showAxisPanelB() {}
}
