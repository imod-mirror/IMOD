package etomo.ui;

import etomo.ApplicationManager;
import etomo.EtomoDirector;
import etomo.process.ProcessState;
import etomo.type.AxisID;
import etomo.type.ProcessTrack;
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
* <p> Revision 1.1.2.1  2004/09/08 20:13:41  sueh
* <p> bug# 520 class contains tomogram specific functionality from MainPAnel,
* <p> which is its base class.  Casts member variables which are used as super
* <p> classes in MainPanel.
* <p> </p>
*/
public class MainTomogramPanel extends MainPanel {
  public static  final String  rcsid =  "$Id$";
  
  //convenience variables set to super class member variables
  //use through cast functions
  private ApplicationManager applicationManager = null;
  private TomogramProcessPanel tomogramAxisPanelA = null;
  private TomogramProcessPanel tomogramAxisPanelB = null;
  
  /**
   * @param appManager
   */
  public MainTomogramPanel(ApplicationManager appManager) {
    super(appManager);
  }
  
  /**
   * Update the state of all the process control panels
   * @param processTrack the process track object containing the state to be
   * displayed
   */
  public void updateAllProcessingStates(ProcessTrack processTrack) {
    if (axisPanelA == null) {
      return;
    }

    castAxisPanelA().setPreProcState(processTrack.getPreProcessingState(AxisID.ONLY));
    castAxisPanelA().setCoarseAlignState(
      processTrack.getCoarseAlignmentState(AxisID.ONLY));
    castAxisPanelA().setFiducialModelState(
      processTrack.getFiducialModelState(AxisID.ONLY));
    castAxisPanelA().setFineAlignmentState(
      processTrack.getFineAlignmentState(AxisID.ONLY));
    castAxisPanelA().setTomogramPositioningState(
      processTrack.getTomogramPositioningState(AxisID.ONLY));
    castAxisPanelA().setTomogramGenerationState(
      processTrack.getTomogramGenerationState(AxisID.ONLY));
    castAxisPanelA().setTomogramCombinationState(
      processTrack.getTomogramCombinationState());
    if (manager.isDualAxis()) {
      castAxisPanelB().setPreProcState(
        processTrack.getPreProcessingState(AxisID.SECOND));
      castAxisPanelB().setCoarseAlignState(
        processTrack.getCoarseAlignmentState(AxisID.SECOND));
      castAxisPanelB().setFiducialModelState(
        processTrack.getFiducialModelState(AxisID.SECOND));
      castAxisPanelB().setFineAlignmentState(
        processTrack.getFineAlignmentState(AxisID.SECOND));
      castAxisPanelB().setTomogramPositioningState(
        processTrack.getTomogramPositioningState(AxisID.SECOND));
      castAxisPanelB().setTomogramGenerationState(
        processTrack.getTomogramGenerationState(AxisID.SECOND));
    }
    castAxisPanelA().setPostProcessingState(processTrack.getPostProcessingState());

  }

  /**
   * Convienence function to return a reference to the correct AxisProcessPanel
   * @param axisID
   * @return
   */
  private TomogramProcessPanel mapAndCastAxis(AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      return castAxisPanelB();
    }
    return castAxisPanelA();
  }
  
  /**
   * Open the setup panel
   */
  public void openSetupPanel(SetupDialog setupDialog) {
    panelCenter.removeAll();
    panelCenter.add(setupDialog.getContainer());
    revalidate();
    EtomoDirector.getMainFrame().pack();
  }

  /**
   * Set the specified button as selected
   * @param axisID
   * @param name
   */
  public void selectButton(AxisID axisID, String name) {
    mapAndCastAxis(axisID).selectButton(name);
  }
  
  /**
   * 
   * @param state
   * @param axisID
   */
  public void setPreProcessingState(ProcessState state, AxisID axisID) {
    TomogramProcessPanel axisPanel = mapAndCastAxis(axisID);
    axisPanel.setPreProcState(state);
  }

  /**
   * 
   * @param state
   * @param axisID
   */
  public void setCoarseAlignState(ProcessState state, AxisID axisID) {
    TomogramProcessPanel axisPanel = mapAndCastAxis(axisID);
    axisPanel.setCoarseAlignState(state);
  }

  /**
   * 
   * @param state
   * @param axisID
   */
  public void setFiducialModelState(ProcessState state, AxisID axisID) {
    TomogramProcessPanel axisPanel = mapAndCastAxis(axisID);
    axisPanel.setFiducialModelState(state);
  }

  /**
   * 
   * @param state
   * @param axisID
   */
  public void setFineAlignmentState(ProcessState state, AxisID axisID) {
    TomogramProcessPanel axisPanel = mapAndCastAxis(axisID);
    axisPanel.setFineAlignmentState(state);
  }

  /**
   * 
   * @param state
   * @param axisID
   */
  public void setTomogramPositioningState(ProcessState state, AxisID axisID) {
    TomogramProcessPanel axisPanel = mapAndCastAxis(axisID);
    axisPanel.setTomogramPositioningState(state);
  }

  /**
   * 
   * @param state
   * @param axisID
   */
  public void setTomogramGenerationState(ProcessState state, AxisID axisID) {
    TomogramProcessPanel axisPanel = mapAndCastAxis(axisID);
    axisPanel.setTomogramGenerationState(state);
  }

  /**
   * 
   * @param state
   */
  public void setTomogramCombinationState(ProcessState state) {
    castAxisPanelA().setTomogramCombinationState(state);
  }

  /**
   * 
   * @param state
   */
  public void setPostProcessingState(ProcessState state) {
    castAxisPanelA().setPostProcessingState(state);
  }
  
  protected void createAxisPanelA(AxisID axisID) {
    tomogramAxisPanelA = null;
    axisPanelA = new TomogramProcessPanel(castManager(), axisID);
  }

  protected void createAxisPanelB() {
    tomogramAxisPanelB = null;
    axisPanelB = new TomogramProcessPanel(castManager(), AxisID.SECOND);
  }
  
  private ApplicationManager castManager() {
    if (manager == null) {
      throw new NullPointerException();
    }
    if (applicationManager == null) {
      applicationManager = (ApplicationManager) manager;
    }
    return applicationManager;
  }
  
  private TomogramProcessPanel castAxisPanelA() {
    if (tomogramAxisPanelA == null) {
      tomogramAxisPanelA = (TomogramProcessPanel) axisPanelA;
    }
    return tomogramAxisPanelA;
  }
  
  private TomogramProcessPanel castAxisPanelB() {
    if (tomogramAxisPanelB == null) {
      tomogramAxisPanelB = (TomogramProcessPanel) axisPanelB;
    }
    return tomogramAxisPanelB;
  }
}
