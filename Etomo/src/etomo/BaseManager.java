package etomo;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import etomo.comscript.ComScriptManager;
import etomo.process.BaseProcessManager;
import etomo.process.ImodManager;
import etomo.process.SystemProcessException;
import etomo.storage.ParameterStore;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.BaseMetaData;
import etomo.type.BaseProcessTrack;
import etomo.type.ConstMetaData;
import etomo.type.ProcessName;
import etomo.type.UserConfiguration;
import etomo.ui.MainFrame;
import etomo.ui.MainPanel;
import etomo.ui.UIParameters;
import etomo.util.Utilities;

/**
* <p>Description: Base class for ApplicationManager and JoinManager</p>
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
* <p> Revision 1.1.2.8  2004/10/01 20:58:02  sueh
* <p> bug# 520 Changed getMetaDAta() to getBaseMetaData() so it can return
* <p> the abstract base class for objects that don't know which type of manager
* <p> they are using.
* <p>
* <p> Revision 1.1.2.7  2004/09/29 17:37:09  sueh
* <p> bug# 520 Using BaseMetaData, BaseProcessTrack, and
* <p> BaseProcessManager.  Moved processDone() from app mgr to base mgr.
* <p> Created abstract startNextProcess() and
* <p> updateDialog(ProcessName, AxisID).  Removed resetState(),
* <p> openNewDataset() and openExistingDataset().  Managers will not be
* <p> reset and this functionality will be handled by EtomoDirector.
* <p>
* <p> Revision 1.1.2.6  2004/09/15 22:33:39  sueh
* <p> bug# 520 call openMessageDialog in mainPanel instead of mainFrame.
* <p> Move packMainWindow and setPanel from ApplicationMAnager to
* <p> BaseManager.
* <p>
* <p> Revision 1.1.2.5  2004/09/13 16:26:46  sueh
* <p> bug# 520 Adding abstract isNewManager.  Each manager would have a
* <p> different way to tell whether they had a file open.
* <p>
* <p> Revision 1.1.2.4  2004/09/09 17:28:38  sueh
* <p> bug# 520 MRU file labels already being set from EtomoDirector
* <p>
* <p> Revision 1.1.2.3  2004/09/08 19:27:19  sueh
* <p> bug# 520 putting initialize UI parameters into a separate function
* <p>
* <p> Revision 1.1.2.2  2004/09/07 17:51:00  sueh
* <p> bug# 520 getting mainFrame and userConfig from EtomoDirector, moved
* <p> settings dialog to BaseManager,  moved backupFiles() to BaseManager,
* <p> moved exitProgram() and processing variables to BaseManager, split
* <p> MainPanel off from MainFrame
* <p>
* <p> Revision 1.1.2.1  2004/09/03 20:37:24  sueh
* <p> bug# 520 Base class for ApplicationManager and JoinManager.  Transfering
* <p> constructor functionality from AppMgr
* <p> </p>
*/
public abstract class BaseManager {
  public static  final String  rcsid =  "$Id$";
  
  //protected static variables
  protected static boolean test = false;
  protected static MainFrame mainFrame = EtomoDirector.getInstance()
      .getMainFrame();
  protected static UserConfiguration userConfig = EtomoDirector.getInstance()
      .getUserConfiguration();
  
  //protected variables
  protected boolean loadedTestParamFile = false;
  protected BaseMetaData baseMetaData = null;
  protected BaseProcessTrack baseProcessTrack = null;
  // imodManager manages the opening and closing closing of imod(s), message
  // passing for loading model
  protected ImodManager imodManager = null;
  //  This object controls the reading and writing of David's com scripts
  protected ComScriptManager comScriptMgr = null;
  //FIXME paramFile may not have to be visible
  protected File paramFile = null;
  //  The ProcessManager manages the execution of com scripts
  protected BaseProcessManager baseProcessMgr = null;
  protected boolean isDataParamDirty = false;
  // Control variable for process execution
  // FIXME: this going to need to expand to handle both axis
  protected String nextProcess = "";

  protected String threadNameA = "none";

  protected String threadNameB = "none";
  
  protected boolean backgroundProcessA = false;
  protected String backgroundProcessNameA = null;
  protected MainPanel mainPanel = null;
  protected String workingDirName = null;
  private static boolean debug = false;

  protected abstract void createComScriptManager();
  protected abstract void createProcessManager();
  protected abstract void createMainPanel();
  protected abstract void createBaseMetaData();
  protected abstract void createProcessTrack();
  protected abstract void updateDialog(ProcessName processName, AxisID axisID);
  protected abstract void startNextProcess(AxisID axisID);

  //FIXME needs to be public?
  public abstract boolean isNewManager();
  public abstract void setTestParamFile(File paramFile);
  
  public BaseManager() {
    workingDirName = System.getProperty("user.dir");
    createBaseMetaData();
    createProcessTrack();
    createProcessManager();
    createComScriptManager();
    createMainPanel();
    //  Initialize the program settings
    debug = EtomoDirector.getInstance().isDebug();
    test = EtomoDirector.getInstance().isTest();
    //imodManager should be created only once.
    createImodManager();
  }
  
  protected void initializeUIParameters(String paramFileName) {
    if (!test) {
      //  Initialize the static UIParameter object
      UIParameters uiparameters = new UIParameters();
      // Open the etomo data file if one was found on the command line
      if (!paramFileName.equals("")) {
        File etomoDataFile = new File(paramFileName);
        loadedTestParamFile = loadTestParamFile(etomoDataFile);
      }
    }
  }
  
  /**
   * Interrupt the currently running thread for this axis
   * 
   * @param axisID
   */
  public void kill(AxisID axisID) {
    baseProcessMgr.kill(axisID);
  }
  
  /**
   * A message asking the ApplicationManager to save the test parameter
   * information to a file.
   */
  public void saveTestParamFile() {
    try {
      backupFile(paramFile);
      ParameterStore paramStore = new ParameterStore(paramFile);
      Storable[] storable = new Storable[2];
      storable[0] = baseMetaData;
      storable[1] = baseProcessTrack;
      paramStore.save(storable);
      //  Update the MRU test data filename list
      userConfig.putDataFile(paramFile.getAbsolutePath());
      mainFrame.setMRUFileLabels(userConfig.getMRUFileList());
      // Reset the process track flag
      baseProcessTrack.resetModified();
    }
    catch (IOException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file save error";
      errorMessage[1] = "Could not save test parameter data to file:";
      errorMessage[2] = except.getMessage();
      mainPanel.openMessageDialog(errorMessage,
        "Test parameter file save error");
    }
    isDataParamDirty = false;
  }
  
  public String getWorkingDirName() {
    return workingDirName;
  }
  
  /**
   * Exit the program
   */
  public boolean exitProgram() {
    //  Check to see if any processes are still running
    ArrayList messageArray = new ArrayList();
    //handle background processes
    if (!threadNameA.equals("none") && backgroundProcessA) {
      messageArray.add("The " + backgroundProcessNameA
        + " process will continue to run after Etomo ends.");
      messageArray.add("Check " + backgroundProcessNameA
        + ".log for status.");
      messageArray.add(" ");
    }
    //handle regular processes
    if ((!threadNameA.equals("none") && !backgroundProcessA)
      || !threadNameB.equals("none")) {
      messageArray.add("There are still processes running.");
      messageArray.add("Exiting Etomo now may terminate those processes.");
    }
    if (messageArray.size() > 0) {
      messageArray.add("Do you still wish to exit the program?");
      if (!mainFrame.openYesNoDialog(
        (String[]) messageArray.toArray(new String[messageArray.size()]))) {
        return false;
      }
    }
    if (saveTestParamIfNecessary()) {
      //  Should we close the 3dmod windows
      try {
        if (imodManager.isOpen()) {
          String[] message = new String[3];
          message[0] = "There are still 3dmod programs running.";
          message[1] = "Do you wish to end these programs?";
          if (mainFrame.openYesNoDialog(message)) {
            imodManager.quit();
          }
        }
      }
      catch (AxisTypeException except) {
        except.printStackTrace();
        mainPanel.openMessageDialog(except.getMessage(), "AxisType problem");
      }
      catch (SystemProcessException except) {
        except.printStackTrace();
        mainPanel.openMessageDialog(except.getMessage(),
          "Problem closing 3dmod");
      }
      return true;
    }
    return false;
  }
  
  /**
   * Check if the current data set is a dual axis data set
   * @return true if the data set is a dual axis data set
   */
  public boolean isDualAxis() {
    if (baseMetaData.getAxisType() == AxisType.SINGLE_AXIS) {
      return false;
    }
    else {
      return true;
    }
  }

  protected void setPanel() {
    mainFrame.pack();
    //  Resize to the users preferrred window dimensions
    mainPanel.setSize(new Dimension(userConfig.getMainWindowWidth(),
      userConfig.getMainWindowHeight()));
    mainFrame.doLayout();
    mainFrame.validate();
    if (isDualAxis()) {
      mainPanel.setDividerLocation(0.51);
    }
  }
  
  //get functions
  
  /**
   * Return the absolute IMOD bin path
   * @return
   */
  public static String getIMODBinPath() {
    return EtomoDirector.getInstance().getIMODDirectory().getAbsolutePath()
      + File.separator + "bin" + File.separator;
  }
  
  /**
   * Return a reference to THE com script manager
   * @return
   */
  public ComScriptManager getComScriptManager() {
    return comScriptMgr;
  }
  
  /**
   *  
   */
  public void packMainWindow() {
    mainFrame.repaint();
    mainPanel.fitWindow();
  }
  
  /**
   * Return the test parameter file as a File object
   * @return a File object specifying the data set parameter file.
   */
  //FIXME this may not have to be visible
  public File getTestParamFile() {
    return paramFile;
  }
  

  /**
   * A message asking the ApplicationManager to load in the information from the
   * test parameter file.
   * @param paramFile the File object specifiying the data parameter file.
   */
  protected boolean loadTestParamFile(File paramFile) {
    //FIXME this function may not have to be visible
    FileInputStream processDataStream;
    try {
      // Read in the test parameter data file
      ParameterStore paramStore = new ParameterStore(paramFile);
      Storable[] storable = new Storable[2];
      storable[0] = baseMetaData;
      storable[1] = baseProcessTrack;
      paramStore.load(storable);

      // Set the current working directory for the application, this is the
      // path to the EDF file.  The working directory is defined by the current
      // user.dir system property.
      // Uggh, stupid JAVA bug, getParent() only returns the parent if the File
      // was created with the full path
      File newParamFile = new File(paramFile.getAbsolutePath());
      workingDirName = newParamFile.getParent();
      setTestParamFile(newParamFile);
      // Update the MRU test data filename list
      userConfig.putDataFile(newParamFile.getAbsolutePath());
      //  Initialize a new IMOD manager
      imodManager.setMetaData((ConstMetaData) baseMetaData);
    }
    catch (FileNotFoundException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file read error";
      errorMessage[1] = "Could not find the test parameter data file:";
      errorMessage[2] = except.getMessage();
      mainPanel.openMessageDialog(errorMessage, "File not found error");
      return false;
    }
    catch (IOException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file read error";
      errorMessage[1] = "Could not read the test parameter data from file:";
      errorMessage[2] = except.getMessage();
      mainPanel.openMessageDialog(errorMessage,
        "Test parameter file read error");
      return false;
    }
    if (!baseMetaData.isValid(false)) {
      mainPanel.openMessageDialog(baseMetaData.getInvalidReason(),
        ".edf file error");
      return false;
    }
    return true;
  }
  
  
  protected void backupFile(File file) {
    if (file.exists()) {
      File backupFile = new File(file.getAbsolutePath() + "~");
      try {
        Utilities.renameFile(file, backupFile);
      }
      catch (IOException except) {
        System.err.println("Unable to backup file: " + file.getAbsolutePath()
          + " to " + backupFile.getAbsolutePath());
        mainPanel.openMessageDialog(except.getMessage(), "File Rename Error");
      }
    }
  }
  
  /**
   * If the current state needs to be saved the users is queried with a dialog
   * box.
   * @return True if either: the current state does not need to be saved, the
   * state is successfully saved, or the user chooses not to save the current
   * state by selecting no.  False is returned if the state can not be
   * successfully saved, or the user chooses cancel.
   */
  protected boolean saveTestParamIfNecessary() {
    // Check to see if the current dataset needs to be saved
    if (isDataParamDirty || baseProcessTrack.isModified()) {
      String[] message = {"Save the current data file ?"};
      int returnValue = mainFrame.openYesNoCancelDialog(message);
      if (returnValue == JOptionPane.CANCEL_OPTION) {
        return false;
      }
      if (returnValue == JOptionPane.NO_OPTION) {
        return true;
      }
      // If the selects Yes then try to save the current EDF file
      if (paramFile == null) {
        if (!mainFrame.getTestParamFilename()) {
          return false;
        }
      }
      // Be sure the file saving was successful
      saveTestParamFile();
      if (isDataParamDirty) {
        return false;
      }
    }
    return true;
  }

  private static boolean getTest() {
    return test;
  }
  
  //create functions
  
  private void createImodManager() {
    imodManager = new ImodManager();
  }
  
  public MainPanel getMainPanel() {
    return mainPanel;
  }
  
  /**
   * Notification message that a background process is done.
   * 
   * @param threadName
   *            The name of the thread that has finished
   */
  public void processDone(String threadName, int exitValue,
    ProcessName processName, AxisID axisID) {
    if (threadName.equals(threadNameA)) {
      mainPanel.stopProgressBar(AxisID.FIRST);
      threadNameA = "none";
      backgroundProcessA = false;
      backgroundProcessNameA = null;
    }
    else if (threadName.equals(threadNameB)) {
      mainPanel.stopProgressBar(AxisID.SECOND);
      threadNameB = "none";
    }
    else {
      mainPanel.openMessageDialog("Unknown thread finished!!!", "Thread name: "
        + threadName);
    }
    if (processName != null) {
      updateDialog(processName, axisID);
    }
    //  Start the next process if one exists and the exit value was equal zero
    if (!nextProcess.equals("")) {
      if (exitValue == 0) {
        startNextProcess(axisID);
      }
      else {
        nextProcess = "";
      }
    }
  }
  
  public BaseMetaData getBaseMetaData() {
    return baseMetaData;
  }

}
