package etomo;

import java.io.File;
import java.util.ArrayList;

import etomo.type.UserConfiguration;
import etomo.ui.MainFrame;
import etomo.util.HashedArray;
import etomo.util.UniqueKey;

/**
 * <p>
 * Description: Directs ApplicationManager and JoinManager through BaseManager.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Organization: Boulder Laboratory for 3-Dimensional Electron Microscopy of
 * Cells (BL3DEM), University of Colorado
 * </p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p>
 * $Log$
 * Revision 1.1.2.4  2004/09/13 16:40:41  sueh
 * bug# 520 Finding manager by key because there can be duplicate names.
 * Using a etomo.util.HashedArray to store managers because they may
 * have duplicate names and they need to because accessed by index and
 * \key.  Making a set of openTomogram and OpenJoin functions to creating
 * new ApplicationManagers and JoinManagers.  Adding calls to functions
 * that create the Window menu items and check the current menu item to
 * EtomoDirector.  Add public functions to access the manager list.  Add
 * public functions to set the current manager, close the current manager,
 * rename the current manager, and exit the program.
 *
 * Revision 1.1.2.3  2004/09/09 17:32:42  sueh
 * bug# 520 Allow retrieval of manager by .edf file name or by order by
 * adding an ArrayList of .edf file names.  Call MainFrame.createMenus after
 * manager list is created (remove call from MainFrame()).  Add access
 * and create functions for the manager list.
 *
 * Revision 1.1.2.2  2004/09/07 17:52:52  sueh
 * bug# 520 moved MainFrame and UserConfiguration to EtomoDirector
 *
 * Revision 1.1.2.1  2004/09/03 20:59:07  sueh
 * bug# 520 transfering constructor code from ApplicationManager.  Allowing
 * multiple ApplicationManagers and JoinManagers
 *
 * </p>
 */

public class EtomoDirector {
  public static final String rcsid = "$Id$";
  
  private static EtomoDirector theEtomoDirector = null;
  private static boolean debug = false;
  private static boolean demo = false;
  private static boolean test = false;
  private static boolean selfTest = false;
  private HashedArray managerList = null;
  private UniqueKey currentManagerKey = null;
  private static MainFrame mainFrame = null;
  private static UserConfiguration userConfig = null;
  private static final String newTomogramName = "Setup Tomogram";
  private static final String newJoinName = "New Join";

  public static void main(String[] args) {
    createInstance(args);
  }

  public synchronized static EtomoDirector createInstance(String[] args) {
    if (theEtomoDirector == null) {
      theEtomoDirector = new EtomoDirector(args);
    }
    return theEtomoDirector;
  }

  public static EtomoDirector getInstance() {
    if (theEtomoDirector == null) {
      throw new IllegalStateException();
    }
    return theEtomoDirector;
  }

  private EtomoDirector(String[] args) {
    theEtomoDirector = this;
    createUserConfiguration();
    if (!test) {
      createMainFrame();
    }
    ArrayList paramFileNameList = parseCommandLine(args);
    int paramFileNameListSize = paramFileNameList.size();
    String paramFileName = null;
    managerList = new HashedArray();
    ApplicationManager appMgr = null;
    //if no param file is found bring up AppMgr.SetupDialog
    if (paramFileNameListSize == 0) {
      openTomogram(true);
    }
    else {
      boolean makeCurrent;
      for (int i = 0; i < paramFileNameListSize; i++) {
        makeCurrent = false;
        paramFileName = (String) paramFileNameList.get(i);
        UniqueKey managerKey = null;
        if (i == 0) {
          makeCurrent = true;
        }
        if (paramFileName.endsWith(".edf")) {
          managerKey = openTomogram(paramFileName, makeCurrent);
        }
        else if (paramFileName.endsWith(".ejf")) {
          managerKey = openJoin(paramFileName, makeCurrent);
        }
      }
    }
    if (!test) {
      mainFrame.createMenus();
      mainFrame.setWindowMenuLabels(managerList);
      mainFrame.setCurrentManager((BaseManager) managerList
          .get(currentManagerKey));
      mainFrame.selectWindowMenuItem(currentManagerKey);
      mainFrame.setMRUFileLabels(userConfig.getMRUFileList());
      mainFrame.pack();
      mainFrame.show();
    }
  }
  
  public BaseManager getCurrentManager() {
    return (BaseManager) managerList.get(currentManagerKey);
  }
  
  public UniqueKey getManagerKey(int index) {
    return managerList.getKey(index);
  }
  
  public synchronized void setCurrentManager(UniqueKey managerKey) {
    BaseManager newCurrentManager = (BaseManager) managerList.get(managerKey);
    if (newCurrentManager == null) {
      throw new NullPointerException("managerKey=" + managerKey); 
    }
    currentManagerKey = managerKey;
    if (!test) {
      mainFrame.setWindowMenuLabels(managerList);
      mainFrame.setCurrentManager(newCurrentManager);
      mainFrame.selectWindowMenuItem(currentManagerKey);
    }
  }
  
  public UniqueKey openTomogram(String etomoDataFileName, boolean makeCurrent) {
    ApplicationManager manager;
    if (etomoDataFileName == null || etomoDataFileName == newTomogramName) {
      manager = new ApplicationManager("");
    }
    else {
      manager = new ApplicationManager(etomoDataFileName);
    }
    UniqueKey managerKey;
    if (manager.isNewManager()) {
      managerKey = managerList.add(newTomogramName, manager);
    }
    else {
      managerKey = managerList.add(manager.getMetaData().getDatasetName(), manager);
    }
    if (makeCurrent) {
      setCurrentManager(managerKey);
    }
    return managerKey;
  }
  
  public UniqueKey openJoin(boolean makeCurrent) {
    return openJoin(newJoinName, makeCurrent);
  }
  
  public UniqueKey openJoin(File etomoJoinFile, boolean makeCurrent) {
    if (etomoJoinFile == null) {
      return openJoin(makeCurrent);
    }
    return openJoin(etomoJoinFile.getAbsolutePath(), makeCurrent);
  }
  
  public UniqueKey openJoin(String etomoJoinFileName, boolean makeCurrent) {
    JoinManager manager;
    if (etomoJoinFileName == null || etomoJoinFileName == newJoinName) {
      manager = new JoinManager("");
    }
    else {
      manager = new JoinManager(etomoJoinFileName);
    }
    UniqueKey managerKey;
    if (manager.isNewManager()) {
      managerKey = managerList.add(newJoinName, manager);
    }
    else {
      managerKey = managerList.add(manager.getMetaData().getDatasetName(), manager);
    }
    if (makeCurrent) {
      setCurrentManager(managerKey);
    }
    return managerKey;
  }
  
  public UniqueKey openTomogram(boolean makeCurrent) {
    return openTomogram(newTomogramName, makeCurrent);
  }
  
  public UniqueKey openTomogram(File etomoDataFile, boolean makeCurrent) {
    if (etomoDataFile == null) {
      return openTomogram(makeCurrent);
    }
    return openTomogram(etomoDataFile.getAbsolutePath(), makeCurrent);
  }


  public boolean closeCurrentManager() {
    BaseManager currentManager = getCurrentManager();
    if (!currentManager.exitProgram()) {
      return false;
    }
    managerList.remove(currentManagerKey);
    currentManagerKey = null;
    if (managerList.size() == 0) {
      if (!test) {
        mainFrame.setWindowMenuLabels(managerList);
        mainFrame.setCurrentManager(null);
      }
      return true;
    }
    setCurrentManager(managerList.getKey(0));
    return true;
  }
  
  public boolean exitProgram() {
    while (managerList.size() != 0) {
      if (!closeCurrentManager()) {
        return false;
      }
    }
    return true;
  }
  
  public void renameCurrentManager(String managerName) {
    currentManagerKey = managerList.rekey(currentManagerKey, managerName);
    if (!test) {
      mainFrame.setWindowMenuLabels(managerList);
      mainFrame.selectWindowMenuItem(currentManagerKey);
    }
  }
  
  private static void createMainFrame() {
    mainFrame = new MainFrame();
  }
  
  public static MainFrame getMainFrame() {
    if (mainFrame == null) {
      throw new NullPointerException();
    }
    return mainFrame;
  }
  
  public static UserConfiguration getUserConfiguration() {
    if (userConfig == null) {
      throw new NullPointerException();
    }
    return userConfig;
  }
  
  private static void  createUserConfiguration() {
    userConfig = new UserConfiguration();
  }
  
  /**
   * Parse the command line. This method will return a non-empty string if there
   * is a etomo data .
   * 
   * @param The
   *          command line arguments
   * @return A string that will be set to the etomo data filename if one is
   *         found on the command line otherwise it is "".
   */
  private ArrayList parseCommandLine(String[] args) {
    ArrayList paramFileNameList = new ArrayList();
    //  Parse the command line arguments
    for (int i = 0; i < args.length; i++) {
      // Filename argument should be the only one not beginning with at least
      // one dash
      if (!args[i].startsWith("-")) {
        paramFileNameList.add(args[i]);
      }
      if (args[i].equals("--debug")) {
        debug = true;
      }
      if (args[i].equals("--demo")) {
        demo = true;
      }
      if (args[i].equals("--test")) {
        test = true;
      }
      if (args[i].equals("--selftest")) {
        selfTest = true;
      }
    }
    return paramFileNameList;
  }

  public static boolean isDebug() {
    return debug;
  }

  public static boolean isSelfTest() {
    return selfTest;
  }

  public static boolean isDemo() {
    return demo;
  }

  public static boolean isTest() {
    return test;
  }
  
  public int getManagerListSize() {
    return managerList.size();
  }

}