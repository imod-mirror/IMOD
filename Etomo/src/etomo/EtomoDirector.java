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
  private String currentParamFileName = null;
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

  public BaseManager getCurrentManager() {
    return (BaseManager) managerList.get(currentParamFileName);
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
    managerList = new HashMap();
    //if no param file is found bring up AppMgr.SetupDialog
    if (paramFileNameListSize == 0) {
      paramFileName = "";
      currentParamFileName = paramFileName;
      managerList.put(paramFileName, new ApplicationManager(paramFileName));
    }
    else {
      for (int i = 0; i < paramFileNameListSize; i++) {
        paramFileName = (String) paramFileNameList.get(i);
        if (i == 0) {
          currentParamFileName = paramFileName;
        }
        if (paramFileName.endsWith(".edf")) {
          managerList.put(paramFileName, new ApplicationManager(paramFileName));
        }
        else if (paramFileName.endsWith(".ejf")) {
          managerList.put(paramFileName, new JoinManager(paramFileName));
        }
      }
    }
    if (!test) {
      mainFrame.setCurrentManager(getCurrentManager());
      mainFrame.setMRUFileLabels(userConfig.getMRUFileList());
      mainFrame.pack();
      mainFrame.show();
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

}