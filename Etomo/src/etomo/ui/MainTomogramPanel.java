package etomo.ui;

import java.io.File;

import etomo.ApplicationManager;
import etomo.EtomoDirector;
import etomo.process.ProcessState;
import etomo.type.AxisID;
import etomo.type.MetaData;
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
* <p> Revision 1.1.2.2  2004/09/15 22:46:52  sueh
* <p> bug# 520 Moved openSetupPanel back to this class.  Moved
* <p> showProcessingPanel() to this base class.  Created AxisProcessPanel
* <p> creation functions.
* <p>
* <p> Revision 1.1.2.1  2004/09/08 20:13:41  sueh
* <p> bug# 520 class contains tomogram specific functionality from MainPAnel,
* <p> which is its base class.  Casts member variables which are used as super
* <p> classes in MainPanel.
* <p> </p>
*/
public class MainTomogramPanel extends MainPanel {
  public static  final String  rcsid =  "$Id$";
  
  //variables cast from base class variables
  //initialized in create function
  private TomogramProcessPanel tomogramAxisPanelA;
  private TomogramProcessPanel tomogramAxisPanelB;
  
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

    tomogramAxisPanelA.setPreProcState(processTrack.getPreProcessingState(AxisID.ONLY));
    tomogramAxisPanelA.setCoarseAlignState(
      processTrack.getCoarseAlignmentState(AxisID.ONLY));
    tomogramAxisPanelA.setFiducialModelState(
      processTrack.getFiducialModelState(AxisID.ONLY));
    tomogramAxisPanelA.setFineAlignmentState(
      processTrack.getFineAlignmentState(AxisID.ONLY));
    tomogramAxisPanelA.setTomogramPositioningState(
      processTrack.getTomogramPositioningState(AxisID.ONLY));
    tomogramAxisPanelA.setTomogramGenerationState(
      processTrack.getTomogramGenerationState(AxisID.ONLY));
    tomogramAxisPanelA.setTomogramCombinationState(
      processTrack.getTomogramCombinationState());
    if (manager.isDualAxis()) {
      tomogramAxisPanelB.setPreProcState(
        processTrack.getPreProcessingState(AxisID.SECOND));
      tomogramAxisPanelB.setCoarseAlignState(
        processTrack.getCoarseAlignmentState(AxisID.SECOND));
      tomogramAxisPanelB.setFiducialModelState(
        processTrack.getFiducialModelState(AxisID.SECOND));
      tomogramAxisPanelB.setFineAlignmentState(
        processTrack.getFineAlignmentState(AxisID.SECOND));
      tomogramAxisPanelB.setTomogramPositioningState(
        processTrack.getTomogramPositioningState(AxisID.SECOND));
      tomogramAxisPanelB.setTomogramGenerationState(
        processTrack.getTomogramGenerationState(AxisID.SECOND));
    }
    tomogramAxisPanelA.setPostProcessingState(processTrack.getPostProcessingState());

  }

  /**
   * Convienence function to return a reference to the correct AxisProcessPanel
   * @param axisID
   * @return
   */
  private TomogramProcessPanel mapAndCastAxis(AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      return tomogramAxisPanelB;
    }
    return tomogramAxisPanelA;
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
    tomogramAxisPanelA.setTomogramCombinationState(state);
  }

  /**
   * 
   * @param state
   */
  public void setPostProcessingState(ProcessState state) {
    tomogramAxisPanelA.setPostProcessingState(state);
  }
  
  protected void createAxisPanelA(AxisID axisID) {
    axisPanelA = new TomogramProcessPanel((ApplicationManager) manager, axisID);
    tomogramAxisPanelA = (TomogramProcessPanel) axisPanelA;
    
  }

  protected void createAxisPanelB() {
    axisPanelB = new TomogramProcessPanel((ApplicationManager) manager, AxisID.SECOND);
    tomogramAxisPanelB = (TomogramProcessPanel) axisPanelB;
  }
  
  /**
   * Set the status bar with the file name of the data parameter file
   */
  public void updateDataParameters(File paramFile, MetaData metaData) {
    StringBuffer buffer = new StringBuffer();
    if (metaData == null) {
      buffer.append("No data set loaded");
    }
    else {
      if (paramFile == null) {
        buffer.append("Data file: NOT SAVED");
      }
      else {
        buffer.append("Data file: " + paramFile.getAbsolutePath());
      }

      buffer.append("   Source: ");
      buffer.append(metaData.getDataSource().toString());
      buffer.append("   Axis type: ");
      buffer.append(metaData.getAxisType().toString());
      buffer.append("   Tomograms: ");
      buffer.append(metaData.getSectionType().toString());
    }
    statusBar.setText(buffer.toString());
  }
}
