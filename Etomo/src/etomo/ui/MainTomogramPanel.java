package etomo.ui;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import etomo.ApplicationManager;
import etomo.process.ProcessState;
import etomo.type.AxisID;
import etomo.type.AxisType;
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
* <p> $Log$ </p>
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
   * Show the processing panel for the requested AxisType
   */
  public void showProcessingPanel(AxisType axisType) {
    //  Delete any existing panels
    axisPanelA = null;
    axisPanelB = null;

    panelCenter.removeAll();
    if (axisType == AxisType.SINGLE_AXIS) {
      createAxisPanelA(AxisID.ONLY);
      scrollA = new ScrollPanel();
      scrollA.add(axisPanelA.getContainer());
      scrollPaneA = new JScrollPane(scrollA);
      panelCenter.add(scrollPaneA);
    }
    else {
      createAxisPanelA(AxisID.FIRST);
      scrollA = new ScrollPanel();
      scrollA.add(axisPanelA.getContainer());
      scrollPaneA = new JScrollPane(scrollA);

      createAxisPanelB();
      scrollB = new ScrollPanel();
      scrollB.add(axisPanelB.getContainer());
      scrollPaneB = new JScrollPane(scrollB);
      splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneA, scrollPaneB);
      splitPane.setDividerLocation(0.5);
      splitPane.setOneTouchExpandable(true);
      panelCenter.add(splitPane);
    }
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
  
  private void createAxisPanelA(AxisID axisID) {
    tomogramAxisPanelA = null;
    axisPanelA = new TomogramProcessPanel(castManager(), axisID);
  }

  private void createAxisPanelB() {
    tomogramAxisPanelB = null;
    axisPanelB = new TomogramProcessPanel(castManager(), AxisID.SECOND);
  }
}
