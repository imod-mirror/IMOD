package etomo;

import java.util.ArrayList;
import java.util.HashMap;

import etomo.type.UserConfiguration;
import etomo.ui.MainFrame;

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
  private HashMap managerList = null;
  private ArrayList managerListOrder = null;
  private String currentManagerName = null;
  private static MainFrame mainFrame = null;
  private static UserConfiguration userConfig = null;

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
    createManagerList();
    ApplicationManager appMgr = null;
    //if no param file is found bring up AppMgr.SetupDialog
    if (paramFileNameListSize == 0) {
      paramFileName = "";
      currentManagerName = paramFileName;
      addManager(paramFileName, new ApplicationManager(paramFileName));
    }
    else {
      for (int i = 0; i < paramFileNameListSize; i++) {
        paramFileName = (String) paramFileNameList.get(i);
        if (i == 0) {
          currentManagerName = paramFileName;
        }
        if (paramFileName.endsWith(".edf")) {
          addManager(paramFileName, new ApplicationManager(paramFileName));
        }
        else if (paramFileName.endsWith(".ejf")) {
          addManager(paramFileName, new JoinManager(paramFileName));
        }
      }
    }
    if (!test) {
      mainFrame.createMenus();
      mainFrame.setCurrentManager(getCurrentManager());
      mainFrame.setMRUFileLabels(userConfig.getMRUFileList());
      mainFrame.pack();
      mainFrame.show();
    }
  }
  
  private void createManagerList() {
    managerList = new HashMap();
    managerListOrder = new ArrayList();
  }
  
  private void addManager(String key, BaseManager manager) {
    managerList.put(key, manager);
    managerListOrder.add(key);
  }
  
  public BaseManager getCurrentManager() {
    return (BaseManager) managerList.get(currentManagerName);
  }
  
  public BaseManager getManager(String key) {
    return (BaseManager) managerList.get(key);
  }

  public String getManagerName(int index) {
    return (String) managerListOrder.get(index);
  }
  
  public void setCurrentManager(String key) {
    currentManagerName = key;
    if (!test) {
      mainFrame.setCurrentManager(getCurrentManager());
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