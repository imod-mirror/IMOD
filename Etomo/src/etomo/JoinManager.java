package etomo;

import java.io.File;
import java.util.Vector;

import etomo.process.ImodProcess;
import etomo.process.SystemProcessException;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.SlicerAngles;
import etomo.ui.JoinDialog;
import etomo.ui.MainJoinPanel;

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
* <p> Revision 1.1.2.6  2004/09/21 17:43:41  sueh
* <p> bug# 520 add imodTomogram and imodRemoveTomogram
* <p>
* <p> Revision 1.1.2.5  2004/09/15 22:34:34  sueh
* <p> bug# 520 casting  base manager when necessary.  Added JoinDialog
* <p>
* <p> Revision 1.1.2.4  2004/09/13 16:41:18  sueh
* <p> bug# 520 added isNewManager stub function
* <p>
* <p> Revision 1.1.2.3  2004/09/08 19:28:25  sueh
* <p> bug# 520 update call to BaseMAnager()
* <p>
* <p> Revision 1.1.2.2  2004/09/07 17:55:15  sueh
* <p> bug# 520 moved mainFrame show responsibility to EtomoDirector
* <p>
* <p> Revision 1.1.2.1  2004/09/03 21:03:27  sueh
* <p> bug# 520 adding place holders for create functions for now
* <p> </p>
*/
public class JoinManager extends BaseManager {
  public static  final String  rcsid =  "$Id$";
  
  //  Process dialog references
  private JoinDialog joinDialog = null;
  
  //convenience variable set equals to mainPanel
  //use through castMainPanel
  private MainJoinPanel mainJoinPanel = null; 
  
  public JoinManager(String paramFileName) {
    super();
    openJoinDialog();
  }
  
  public boolean isNewManager() {
    return true;
  }

  protected void createComScriptManager() {
    
  }
  
  protected void createProcessManager() {
    
  }
  
  public void openNewDataset() {
    
  }
  
  public void openExistingDataset(File paramFile) {
    
  }
  
  /**
   * Open the join dialog
   */
  public void openJoinDialog() {
    openProcessingPanel();
    if (joinDialog == null) {
      joinDialog = new JoinDialog(this);
    }
    mainPanel.showProcess(joinDialog.getContainer(), AxisID.ONLY);
  }
  
  /**
   *  
   */
  public void doneJoinDialog(AxisID axisID) {
    if (joinDialog == null) {
      mainPanel.openMessageDialog(
        "Can not update join without an active join dialog",
        "Program logic error");
      return;
    }
    isDataParamDirty = true;
    joinDialog = null;
  }

  
  protected void createMainPanel() {
    mainPanel = new MainJoinPanel(this);
  }
  
  private MainJoinPanel castMainPanel() {
    if (mainPanel == null) {
      throw new NullPointerException();
    }
    if (mainJoinPanel == null) {
      mainJoinPanel = (MainJoinPanel) mainPanel;
    }
    return mainJoinPanel;
  }
  
  /**
   * Open 3dmod to view a file
   */
  public int imodOpen(String imodKey, File file, int imodIndex) {
    try {
      if (imodIndex == -1) {
        imodIndex = imodManager.newImod(imodKey, file);
      }
      imodManager.open(imodKey, file, imodIndex);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(),
        "Can't open " + imodKey + " 3dmod with imodIndex=" + imodIndex);
    }
    return imodIndex;
  }
  
  public void imodRemove(String imodKey, int imodIndex) {
    if (imodIndex == -1) {
      return;
    }
    try {
      imodManager.delete(imodKey, imodIndex);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(),
        "Can't delete " + imodKey + " 3dmod with imodIndex=" + imodIndex);
    }
  }
  
  public SlicerAngles imodGetSlicerAngles(String imodKey, int imodIndex) {
    Vector results = null;
    try {
      if (imodIndex == -1) {
        mainPanel.openMessageDialog("The is no open " + imodKey
            + " 3dmod for the highlighted row.", "No 3dmod");
      }
      results = imodManager.getSlicerAngles(imodKey, imodIndex);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(),
          "Can't get rotation angles from " + imodKey
              + " 3dmod.");
    }
    Vector messageArray = new Vector();
    SlicerAngles slicerAngles = null;
    if (results == null) {
      messageArray.add("Unable to retrieve slicer angles.");
      messageArray.add("The " + imodKey + " may not be open in 3dmod.");
    }
    else {
      slicerAngles = new SlicerAngles();
      boolean foundResultLine1 = false;
      boolean foundResult = false;
      String result = null;
      for (int i = 0; i < results.size(); i++) {
        result = (String) results.get(i);
        if (result.indexOf(ImodProcess.IMOD_SEND_EVENT_STRING) != -1
            || result.indexOf(ImodProcess.ERROR_STRING) != -1
            || result.indexOf(ImodProcess.WARNING_STRING) != -1) {
          messageArray.add(result);
        }
        else if (!foundResultLine1 && !foundResult
            && result.equals(ImodProcess.SLICER_ANGLES_RESULTS_STRING1)) {
          foundResultLine1 = true;
        }
        else if (foundResultLine1 && !foundResult
            && result.equals(ImodProcess.SLICER_ANGLES_RESULTS_STRING2)) {
          foundResult = true;
        }
        else if (foundResult && !slicerAngles.isComplete()) {
          try {
            slicerAngles.add(result);
          }
          catch (NumberFormatException e) {
            messageArray.add(result);
          }
        }
        else {
          messageArray.add(result);
        }
      }
      if (!slicerAngles.isComplete()) {
        messageArray.add("Unable to retrieve slicer angles from " + imodKey
            + " 3dmod.");
        if (!slicerAngles.isEmpty()) {
          messageArray.add("slicerAngles=" + slicerAngles);
        }
      }
    }
    if (messageArray.size() > 0) {
      String[] messages = (String[]) messageArray
          .toArray(new String[messageArray.size()]);
      mainPanel.openMessageDialog(messages, "Slicer Angles");
    }
    return slicerAngles;
  }

  
  /**
   * Open the main window in processing mode
   */
  private void openProcessingPanel() {
    mainPanel.showProcessingPanel(AxisType.SINGLE_AXIS);
    setPanel();
  }
}
