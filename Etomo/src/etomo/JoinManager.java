package etomo;

import java.awt.Dimension;
import java.io.File;
import java.util.Vector;

import etomo.comscript.FlipyzParam;
import etomo.comscript.MakejoincomParam;
import etomo.process.ImodManager;
import etomo.process.ImodProcess;
import etomo.process.JoinProcessManager;
import etomo.process.SystemProcessException;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.BaseMetaData;
import etomo.type.ConstJoinMetaData;
import etomo.type.JoinMetaData;
import etomo.type.JoinProcessTrack;
import etomo.type.ProcessName;
import etomo.type.SlicerAngles;
import etomo.ui.JoinDialog;
import etomo.ui.MainJoinPanel;
import etomo.ui.MainPanel;

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
* <p> Revision 1.1.2.14  2004/10/14 03:24:33  sueh
* <p> bug# 520 Added open join samples in Imod.  Using the Make Samples as
* <p> a signal somewhat like exiting Setup successfully.  In this case I only
* <p> need to call imodManager.setMetaData and enable the other join tabs.
* <p>
* <p> Revision 1.1.2.13  2004/10/14 02:26:38  sueh
* <p> bug# 520 Added setWorkingDir() to set the propertyUserDir.
* <p>
* <p> Revision 1.1.2.12  2004/10/11 01:59:23  sueh
* <p> bug# 520 moved responsibility for mainPanel, metaData, processTrack,
* <p> and progressManager to child classes.  Used abstract functions to use
* <p> these variables in the base classes.  This is more reliable and doesn't
* <p> require casting.
* <p>
* <p> Revision 1.1.2.11  2004/10/08 15:44:52  sueh
* <p> bug# 520 Fixed Make Samples functionality.  Used startNextProcess to
* <p> call startjoin.com.  Used BackgroundProcess to call makejoincom
* <p>
* <p> Revision 1.1.2.10  2004/10/06 01:26:11  sueh
* <p> bug# 520 Changed Make Join button to Make Samples.  Added flip().
* <p>
* <p> Revision 1.1.2.9  2004/10/01 21:00:44  sueh
* <p> bug# 520 Added getMetaData()
* <p>
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
  private MainJoinPanel mainPanel;
  private JoinMetaData metaData;
  private JoinProcessManager processMgr;
  private JoinProcessTrack processTrack;
  
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
    processMgr = new JoinProcessManager(this);
  }
  
  protected void createProcessTrack() {
    processTrack = new JoinProcessTrack();
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
  
  protected void createMetaData() {
    metaData = new JoinMetaData();
  }
  
  public void setWorkingDir(String workingDir) {
    propertyUserDir = workingDir;
  }
  
  /**
   * Open 3dmod to view join samples
   */
  public void imodOpenJoinSamples() {
    try {
      imodManager.open(ImodManager.JOIN_SAMPLES_KEY);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(),
        "Can't open " + ImodManager.JOIN_SAMPLES_KEY + " 3dmod ");
    }
  }
  
  /**
   * Open 3dmod to view join samples
   */
  public void imodOpenJoinSampleAverages() {
    try {
      imodManager.open(ImodManager.JOIN_SAMPLE_AVERAGES_KEY);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      mainPanel.openMessageDialog(except.getMessage(),
        "Can't open " + ImodManager.JOIN_SAMPLE_AVERAGES_KEY + " 3dmod ");
    }
  }

  /**
   * Open 3dmod to view a file
   */
  public int imodOpenFile(String imodKey, File file, int imodIndex) {
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

  public void makejoincom() {
    mainPanel.startProgressBar("Makejoincom", AxisID.ONLY);
    nextProcess = "startjoin";
    isDataParamDirty = true;
    if (!joinDialog.getMetaData(metaData)) {
      mainPanel.openMessageDialog(joinDialog.getInvalidReason(), "Invalid Data");
      mainPanel.stopProgressBar(AxisID.ONLY);
      return;
    }
    if (!metaData.isValid(true)) {
      mainPanel.openMessageDialog(metaData.getInvalidReason(), "Invalid Data");
      mainPanel.stopProgressBar(AxisID.ONLY);
      return;
    }
    imodManager.setMetaData(metaData);
    joinDialog.setEnabledTabs(true);
    MakejoincomParam makejoincomParam = new MakejoincomParam(metaData);
    try {
      threadNameA = processMgr.makejoincom(makejoincomParam);
    }
    catch (SystemProcessException except) {
      joinDialog.abortAddSection();
      except.printStackTrace();
      mainPanel.openMessageDialog("Can't run makejoincom\n"
        + except.getMessage(), "SystemProcessException");
      mainPanel.stopProgressBar(AxisID.ONLY);
      return; 
    }
  }
  
  public void startjoin() {
    mainPanel.startProgressBar("Startjoin", AxisID.ONLY);
    nextProcess = "";
    try {
      threadNameA = processMgr.startjoin();
    }
    catch (SystemProcessException except) {
      joinDialog.abortAddSection();
      except.printStackTrace();
      mainPanel.openMessageDialog("Can't run startjoin.com\n"
        + except.getMessage(), "SystemProcessException");
      mainPanel.stopProgressBar(AxisID.ONLY);
      return; 
    }
  }
  
  public void flip(File tomogram, File workingDir) {
    mainPanel.startProgressBar("Flipping " + tomogram.getName(), AxisID.ONLY);
    FlipyzParam flipyzParam = new FlipyzParam(tomogram, workingDir);
    try {
      threadNameA = processMgr.flipyz(flipyzParam);
    }
    catch (SystemProcessException except) {
      joinDialog.abortAddSection();
      except.printStackTrace();
      mainPanel.openMessageDialog("Can't run clip flipyz\n"
        + except.getMessage(), "SystemProcessException");
      mainPanel.stopProgressBar(AxisID.ONLY);
      return; 
    }
  }

  
  public void addSection(File tomogram) {
    joinDialog.addSection(tomogram);
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
    mainPanel.updateDataParameters(paramFile, metaData);
  }
  
  protected void updateDialog(ProcessName processName, AxisID axisID) {
    
  }
  
  public ConstJoinMetaData getMetaData() {
    return (ConstJoinMetaData) metaData;
  }
  
  /**
   * Start the next process specified by the nextProcess string
   */
  protected void startNextProcess(AxisID axisID) {
    if (nextProcess.equals("startjoin")) {
      startjoin();
      return;
    }
  }
  
  protected AxisType getAxisType() {
    return metaData.getAxisType();
  }
  
  public BaseMetaData getBaseMetaData() {
    return (BaseMetaData) metaData;
  }
  
  protected boolean isMetaDataValid(boolean fromScreen) {
    if (!metaData.isValid(false)) {
      mainPanel.openMessageDialog(metaData.getInvalidReason(),
        ".edf file error");
      return false;
    }
    return true;
  }
  
  protected void setMetaData(ImodManager imodManager) {
  }
  
  protected void storeMetaData(Storable[] storable, int index) {
    storable[index] = metaData;
  }
  
  
  protected void openMessageDialog(String[] message, String title) {
    mainPanel.openMessageDialog(message, title);
  }
  
  protected void openMessageDialog(String message, String title) {
    mainPanel.openMessageDialog(message, title);
  }
  
  protected void setMainPanelSize() {
    mainPanel.setSize(new Dimension(userConfig.getMainWindowWidth(),
      userConfig.getMainWindowHeight()));
  }
  protected void setDividerLocation() {
    if (isDualAxis()) {
      mainPanel.setDividerLocation(0.51);
    }
  }
  
  public void packMainWindow() {
    mainFrame.repaint();
    mainPanel.fitWindow();
  }
  
  public MainPanel getMainPanel() {
    return mainPanel;
  }
  
  protected void stopProgressBar(AxisID axisID) {
    mainPanel.stopProgressBar(axisID);
  }
  
  protected void storeProcessTrack(Storable[] storable, int index) {
    storable[index] = processTrack;
  }
  
  protected void resetProcessTrack() {
    processTrack.resetModified();
  }
  
  protected boolean isProcessTrackModified() {
    return processTrack.isModified();
  }
  
  /**
   * Interrupt the currently running thread for this axis
   * 
   * @param axisID
   */
  public void kill(AxisID axisID) {
    processMgr.kill(axisID);
  }
  
  protected boolean isMetaDataValid(File paramFile) {
    if (!metaData.isValid(paramFile)) {
      mainPanel.openMessageDialog(metaData.getInvalidReason(),
        ".edf file error");
      return false;
    }
    return true;
  }

}
