package etomo;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import etomo.comscript.ComScriptManager;
import etomo.process.ImodManager;
import etomo.process.ProcessManager;
import etomo.process.SystemProcessException;
import etomo.storage.ParameterStore;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.ConstMetaData;
import etomo.type.MetaData;
import etomo.type.ProcessTrack;
import etomo.type.UserConfiguration;
import etomo.ui.MainFrame;
import etomo.ui.MainPanel;
import etomo.ui.SettingsDialog;
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
  protected static MainFrame mainFrame = EtomoDirector.getMainFrame();
  protected static UserConfiguration userConfig = EtomoDirector
  .getUserConfiguration();
  
  //protected variables
  protected boolean loadedTestParamFile = false;
  protected MetaData metaData = null;
  protected ProcessTrack processTrack = null;
  // imodManager manages the opening and closing closing of imod(s), message
  // passing for loading model
  protected ImodManager imodManager = null;
  //  This object controls the reading and writing of David's com scripts
  protected ComScriptManager comScriptMgr = null;
  //FIXME paramFile may not have to be visible
  protected File paramFile = null;
  //FIXME homeDirectory may not have to be visible
  protected String homeDirectory;
  //  The ProcessManager manages the execution of com scripts
  protected ProcessManager processMgr = null;
  protected boolean isDataParamDirty = false;
  // Control variable for process execution
  // FIXME: this going to need to expand to handle both axis
  protected String nextProcess = "";

  protected String threadNameA = "none";

  protected String threadNameB = "none";
  
  protected boolean backgroundProcessA = false;
  protected String backgroundProcessNameA = null;
  protected MainPanel mainPanel = null;
  
  //private static variables
  private static boolean debug = false;
  private static File IMODDirectory;
  private static File IMODCalibDirectory;
  
  //private variables
  
  // advanced dialog state for this instance, this gets set upon startup from
  // the user configuration and can be modified for this instance by either
  // the option or advanced menu items
  private boolean isAdvanced = false;
  private SettingsDialog settingsDialog = null;
  
  
  //abstract functions
  
  //create functions
  protected abstract void createComScriptManager();
  protected abstract void createProcessManager();
  protected abstract void createMainPanel();

  //FIXME needs to be public?
  public abstract void openNewDataset();
  public abstract void openExistingDataset(File paramFile);
  public abstract boolean isNewManager();
  
  public BaseManager() {
    createMetaData();
    createProcessTrack();
    createProcessManager();
    createComScriptManager();
    createMainPanel();
    //  Initialize the program settings
    debug = EtomoDirector.isDebug();
    test = EtomoDirector.isTest();
    initProgram();
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
    processMgr.kill(axisID);
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
      storable[0] = metaData;
      storable[1] = processTrack;
      paramStore.save(storable);
      //  Update the MRU test data filename list
      userConfig.putDataFile(paramFile.getAbsolutePath());
      mainFrame.setMRUFileLabels(userConfig.getMRUFileList());
      // Reset the process track flag
      processTrack.resetModified();
    }
    catch (IOException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file save error";
      errorMessage[1] = "Could not save test parameter data to file:";
      errorMessage[2] = except.getMessage();
      mainFrame.openMessageDialog(errorMessage,
        "Test parameter file save error");
    }
    isDataParamDirty = false;
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
      //  Save the current window size to the user config
      Dimension size = mainFrame.getSize();
      userConfig.setMainWindowWidth(size.width);
      userConfig.setMainWindowHeight(size.height);
      //  Write out the user configuration data
      File userConfigFile = new File(homeDirectory, ".etomo");
      //  Make sure the config file exists, create it if it doesn't
      try {
        userConfigFile.createNewFile();
      }
      catch (IOException except) {
        System.err.println("IOException: Could not create file:"
          + userConfigFile.getAbsolutePath() + "\n" + except.getMessage());
        System.err.println(except.getMessage());
        return true;
      }
      ParameterStore userParams = new ParameterStore(userConfigFile);
      Storable storable[] = new Storable[1];
      storable[0] = userConfig;
      if (!userConfigFile.canWrite()) {
        mainFrame.openMessageDialog(
          "Change permissions of $HOME/.etomo to allow writing",
          "Unable to save user configuration file");
      }
      if (userConfigFile.canWrite()) {
        try {
          userParams.save(storable);
        }
        catch (IOException excep) {
          excep.printStackTrace();
          mainFrame.openMessageDialog(
            "IOException: unable to save user parameters\n"
              + excep.getMessage(), "Unable to save user parameters");
        }
      }
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
        mainFrame.openMessageDialog(except.getMessage(), "AxisType problem");
      }
      catch (SystemProcessException except) {
        except.printStackTrace();
        mainFrame.openMessageDialog(except.getMessage(),
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
    if (metaData.getAxisType() == AxisType.SINGLE_AXIS) {
      return false;
    }
    else {
      return true;
    }
  }

  /**
   * Open up the settings dialog box
   */
  public void openSettingsDialog() {
    //  Open the dialog in the appropriate mode for the current state of
    //  processing
    if (settingsDialog == null) {
      settingsDialog = new SettingsDialog(this);
      settingsDialog.setParameters(userConfig);
      Dimension frmSize = mainFrame.getSize();
      Point loc = mainFrame.getLocation();
      settingsDialog.setLocation(loc.x, loc.y + frmSize.height);
      settingsDialog.setModal(false);
    }
    settingsDialog.show();
  }

  
  /**
   * Set the data set parameter file. This also updates the mainframe data
   * parameters.
   * @param paramFile a File object specifying the data set parameter file.
   */
  //FIXME this may not have to be visible
  public void setTestParamFile(File paramFile) {
    this.paramFile = paramFile;
    //  Update main window information and status bar
    mainPanel.updateDataParameters(paramFile, metaData);
  }
  
  //get functions
  
  /**
   * Return the absolute IMOD bin path
   * @return
   */
  public static String getIMODBinPath() {
    return getIMODDirectory().getAbsolutePath()
      + File.separator + "bin" + File.separator;
  }
  
  /**
   * Return the IMOD directory
   */
  static public File getIMODDirectory() {
    //  Return a copy of the IMODDirectory object
    return new File(IMODDirectory.getAbsolutePath());
  }
  
  /**
   * Return the IMOD calibration directory
   */
  static public File getIMODCalibDirectory() {
    //  Return a copy of the IMODDirectory object
    return new File(IMODCalibDirectory.getAbsolutePath());
  } 
  
  /**
   * Return a reference to THE com script manager
   * @return
   */
  public ComScriptManager getComScriptManager() {
    return comScriptMgr;
  }
  
  // FIXME: this is a temporary patch until we can transition the MetaData
  // object to a static object or singleton
  public ConstMetaData getMetaData() {
    return metaData;
  }
  
  /**
   * Get the current advanced state
   */
  public boolean getAdvanced() {
    return isAdvanced;
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
   * Reset the state of the application to the startup condition
   */
  protected void resetState() {
    //FIXME should EtomoDirectory handle reseting managers?
    // Delete the objects associated with the current dataset
    createMetaData();
    paramFile = null;
    createComScriptManager();
    createProcessManager();
    createProcessTrack();
    settingsDialog = null;
  }
  
  /**
   *  
   */
  public void getSettingsParameters() {
    if (settingsDialog != null) {
      settingsDialog.getParameters(userConfig);
      setUserPreferences();
      mainFrame.repaintWindow();
    }
  }
  
  /**
   *  
   */
  public void closeSettingsDialog() {
    if (settingsDialog != null) {
      settingsDialog.dispose();
    }
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
      storable[0] = metaData;
      storable[1] = processTrack;
      paramStore.load(storable);

      // Set the current working directory for the application, this is the
      // path to the EDF file.  The working directory is defined by the current
      // user.dir system property.
      // Uggh, stupid JAVA bug, getParent() only returns the parent if the File
      // was created with the full path
      File newParamFile = new File(paramFile.getAbsolutePath());
      System.setProperty("user.dir", newParamFile.getParent());
      setTestParamFile(newParamFile);
      // Update the MRU test data filename list
      userConfig.putDataFile(newParamFile.getAbsolutePath());
      //  Initialize a new IMOD manager
      imodManager.setMetaData(metaData);
    }
    catch (FileNotFoundException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file read error";
      errorMessage[1] = "Could not find the test parameter data file:";
      errorMessage[2] = except.getMessage();
      mainFrame.openMessageDialog(errorMessage, "File not found error");
      return false;
    }
    catch (IOException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file read error";
      errorMessage[1] = "Could not read the test parameter data from file:";
      errorMessage[2] = except.getMessage();
      mainFrame.openMessageDialog(errorMessage,
        "Test parameter file read error");
      return false;
    }
    if (!metaData.isValid(false)) {
      mainFrame.openMessageDialog(metaData.getInvalidReason(),
        ".edf file error");
      return false;
    }
    return true;
  }
  
  /**
   * Set the user preferences
   */
  protected void setUserPreferences() {
    //FIXME this function may not have to be visible
    ToolTipManager.sharedInstance().setInitialDelay(
      userConfig.getToolTipsInitialDelay());
    ToolTipManager.sharedInstance().setDismissDelay(
      userConfig.getToolTipsDismissDelay());
    setUIFont(userConfig.getFontFamily(), userConfig.getFontSize());
    setLookAndFeel(userConfig.getNativeLookAndFeel());
    isAdvanced = userConfig.getAdvancedDialogs();
  }

  //private functions
  
  /**
   *  
   */
  private void initProgram() {
    System.err.println("java.version:  " + System.getProperty("java.version"));
    System.err.println("java.vendor:  " + System.getProperty("java.vendor"));
    System.err.println("java.home:  " + System.getProperty("java.home"));
    System.err.println("java.vm.version:  "
      + System.getProperty("java.vm.version"));
    System.err.println("java.vm.vendor:  "
      + System.getProperty("java.vm.vendor"));
    System.err.println("java.vm.home:  " + System.getProperty("java.vm.home"));
    System.err.println("java.class.version:  "
      + System.getProperty("java.class.version"));
    System.err.println("java.class.path:  "
      + System.getProperty("java.class.path"));
    System.err.println("java.library.path:  "
      + System.getProperty("java.library.path"));
    System.err.println("java.io.tmpdir:  "
      + System.getProperty("java.io.tmpdir"));
    System.err.println("java.compiler:  " + System.getProperty("java.compiler"));
    System.err.println("java.ext.dirs:  " + System.getProperty("java.ext.dirs"));
    System.err.println("os.name:  " + System.getProperty("os.name"));
    System.err.println("os.arch:  " + System.getProperty("os.arch"));
    System.err.println("os.version:  " + System.getProperty("os.version"));
    System.err.println("user.name:  " + System.getProperty("user.name"));
    System.err.println("user.home:  " + System.getProperty("user.home"));
    System.err.println("user.dir:  " + System.getProperty("user.dir"));
    // Get the HOME directory environment variable to find the program
    // configuration file
    homeDirectory = System.getProperty("user.home");
    if (homeDirectory.equals("")) {
      String[] message = new String[2];
      message[0] = "Can not find home directory! Unable to load user preferences";
      message[1] = "Set HOME environment variable and restart program to fix this problem";
      mainFrame.openMessageDialog(message, "Program Initialization Error");
      System.exit(1);
    }
    // Get the IMOD directory so we know where to find documentation
    // Check to see if is defined on the command line first with -D
    // Otherwise check to see if we can get it from the environment
    String imodDirectoryName = System.getProperty("IMOD_DIR");
    if (imodDirectoryName == null) {
      imodDirectoryName = Utilities.getEnvironmentVariable("IMOD_DIR");
      if (imodDirectoryName.equals("")) {
        String[] message = new String[3];
        message[0] = "Can not find IMOD directory!";
        message[1] = "Set IMOD_DIR environment variable and restart program to fix this problem";
        mainFrame.openMessageDialog(message, "Program Initialization Error");
        System.exit(1);
      }
      else {
        if (debug) {
          System.err.println("IMOD_DIR (env): " + imodDirectoryName);
        }
      }
    }
    else {
      if (debug) {
        System.err.println("IMOD_DIR (-D): " + imodDirectoryName);
      }
    }
    IMODDirectory = new File(imodDirectoryName);

    // Get the IMOD calibration directory so we know where to find documentation
    // Check to see if is defined on the command line first with -D
    // Otherwise check to see if we can get it from the environment
    String imodCalibDirectoryName = System.getProperty("IMOD_CALIB_DIR");
    if (imodCalibDirectoryName == null) {
      imodCalibDirectoryName = Utilities.getEnvironmentVariable("IMOD_CALIB_DIR");
      if (!imodCalibDirectoryName.equals("")) {
        if (debug) {
          System.err.println("IMOD_CALIB_DIR (env): " + imodCalibDirectoryName);
        }
      }
    }
    else {
      if (debug) {
        System.err.println("IMOD_CALIB_DIR (-D): " + imodCalibDirectoryName);
      }
    }
    IMODCalibDirectory = new File(imodCalibDirectoryName);
    //  Create a File object specifying the user configuration file
    File userConfigFile = new File(homeDirectory, ".etomo");
    //  Make sure the config file exists, create it if it doesn't
    try {
      userConfigFile.createNewFile();
    }
    catch (IOException except) {
      System.err.println("Could not create file:"
        + userConfigFile.getAbsolutePath());
      System.err.println(except.getMessage());
    }
    // Load in the user configuration
    ParameterStore userParams = new ParameterStore(userConfigFile);
    Storable storable[] = new Storable[1];
    storable[0] = userConfig;
    try {
      userParams.load(storable);
    }
    catch (IOException except) {
      mainFrame.openMessageDialog(except.getMessage(),
        "IO Exception: Can't load user configuration"
          + userConfigFile.getAbsolutePath());
    }
    //  Set the user preferences
    setUserPreferences();
  }
  
  /**
   *  
   */
  private static void setUIFont(String fontFamily, int fontSize) {
    // sets the default font for all Swing components.
    // ex.
    //  setUIFont (new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,12));
    // Taken from: http://www.rgagnon.com/javadetails/java-0335.html
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof FontUIResource) {
        FontUIResource currentFont = (FontUIResource) value;
        FontUIResource newFont = new FontUIResource(fontFamily,
          currentFont.getStyle(), fontSize);
        UIManager.put(key, newFont);
      }
    }
  }
  
  /**
   * Sets the look and feel for the program.
   * 
   * @param nativeLookAndFeel
   *          set to true to use the host os look and feel, false will use the
   *          Metal look and feel.
   */
  private void setLookAndFeel(boolean nativeLookAndFeel) {
    String lookAndFeelClassName;

    //UIManager.LookAndFeelInfo plaf[] = UIManager.getInstalledLookAndFeels();
    //for(int i = 0; i < plaf.length; i++) {
    //  System.err.println(plaf[i].getClassName());
    //}
    String osName = System.getProperty("os.name");
    if (debug) {
      System.err.println("os.name: " + osName);
    }
    if (nativeLookAndFeel) {
      if (osName.startsWith("Mac OS X")) {
        lookAndFeelClassName = "apple.laf.AquaLookAndFeel";
        if (debug) {
          System.err.println("Setting AquaLookAndFeel");
        }
      }
      else if (osName.startsWith("Windows")) {
        lookAndFeelClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        if (debug) {
          System.err.println("Setting WindowsLookAndFeel");
        }
      }
      else {
        lookAndFeelClassName = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        if (debug) {
          System.err.println("Setting MotifLookAndFeel");
        }
      }
    }
    else {
      lookAndFeelClassName = UIManager.getCrossPlatformLookAndFeelClassName();
      if (debug) {
        System.err.println("Setting MetalLookAndFeel");
      }
    }
    try {
      UIManager.setLookAndFeel(lookAndFeelClassName);
    }
    catch (Exception excep) {
      System.err.println("Could not set " + lookAndFeelClassName
        + " look and feel");
    }
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
        mainFrame.openMessageDialog(except.getMessage(), "File Rename Error");
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
    if (isDataParamDirty || processTrack.isModified()) {
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

  /**
   * Return the users home directory environment variable HOME or an empty
   * string if it doesn't exist.
   */
  private String getHomeDirectory() {
    return homeDirectory;
  }

  private void setAdvanced(boolean state) {
    isAdvanced = state;
  }
  
  private static boolean getTest() {
    return test;
  }
  
  //create functions
  
  private void createImodManager() {
    imodManager = new ImodManager();
  }
  
  private void createMetaData() {
    metaData = new MetaData();
  }
  
  private void createProcessTrack() {
    processTrack = new ProcessTrack();
  }
  
  public MainPanel getMainPanel() {
    return mainPanel;
  }
  
}
