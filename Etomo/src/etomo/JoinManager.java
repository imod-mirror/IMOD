package etomo;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import etomo.comscript.BadComScriptException;
import etomo.process.ImodProcess;
import etomo.process.JoinProcessManager;
import etomo.process.SystemProcessException;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.ConstJoinMetaData;
import etomo.type.JoinMetaData;
import etomo.type.JoinProcessTrack;
import etomo.type.ProcessName;
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
* <p> Revision 1.1.2.8  2004/09/29 17:42:26  sueh
* <p> bug# 520 Casting mainPanel and other members from BaseManager to
* <p> private local variables in the create functions.  Removed
* <p> openNewDataset() and openExistingDataset().  This functionality is
* <p> handled in EtomoDirector.  Added startJoin().  Added setTestParamFile()
* <p> implementation.
* <p>
* <p> Revision 1.1.2.7  2004/09/22 22:03:53  sueh
* <p> bug# 520 Made the imod functions more general by passing in the
* <p> ImodManager key.  Added imodGetSlicerAngles.
* <p>
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
  
  //variables cast from base class variables
  //initialized in create function
  private MainJoinPanel mainJoinPanel;
  private JoinMetaData joinMetaData;
  private JoinProcessManager joinProcessMgr;
  
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
    baseProcessMgr = new JoinProcessManager(this);
    joinProcessMgr = (JoinProcessManager) baseProcessMgr;
  }
  
  protected void createProcessTrack() {
    baseProcessTrack = new JoinProcessTrack();
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
    mainJoinPanel = (MainJoinPanel) mainPanel;
  }
  
  protected void createBaseMetaData() {
    baseMetaData = new JoinMetaData();
    joinMetaData = (JoinMetaData) baseMetaData;
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
   * Place the data from the screen in the meta data object.  Run the 
   * makejoincom script.  Run startjoin.com.
   */
  public void startJoin() {
    mainPanel.startProgressBar("Starting join", AxisID.ONLY);
    isDataParamDirty = true;
    joinDialog.retrieveData(joinMetaData);
    try {
      joinProcessMgr.startJoin(joinMetaData);
    }
    catch (BadComScriptException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(),
          "Can't run makejoincom or startjoin.com");
      return;
    }
    catch (IOException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog("Can't run makejoincom or startjoin.com\n"
        + except.getMessage(), "IOException");
      return;
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog("Can't run makejoincom or startjoin.com\n"
        + except.getMessage(), "SystemProcessException");
      return; 
    }
    mainPanel.stopProgressBar(AxisID.ONLY);
  }

  /**
   * Open the main window in processing mode
   */
  private void openProcessingPanel() {
    mainPanel.showProcessingPanel(AxisType.SINGLE_AXIS);
    setPanel();
  }
  
  /**
   * Set the data set parameter file. This also updates the mainframe data
   * parameters.
   * @param paramFile a File object specifying the data set parameter file.
   */
  public void setTestParamFile(File paramFile) {
    this.paramFile = paramFile;
    //  Update main window information and status bar
    mainJoinPanel.updateDataParameters(paramFile, joinMetaData);
  }
  
  protected void updateDialog(ProcessName processName, AxisID axisID) {
    
  }
  
  protected void startNextProcess(AxisID axisID) {
    
  }
  
  public ConstJoinMetaData getMetaData() {
    return (ConstJoinMetaData) baseMetaData;
  }
}
