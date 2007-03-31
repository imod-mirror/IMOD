package etomo.ui;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JOptionPane;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.process.ProcessMessages;
import etomo.type.AxisID;
import etomo.util.UniqueKey;

/**
* <p>Description: Class to provide a public interface to the application
* frames.  Must allow objects in other classes to call any public or package
* level function in MainFrame.  If necessary, it can be modified to handle
* functions from SubFrame.  Must not generate any headless exceptions when JUnit
* is running.  Logs the text of all dialog messages to etomo_test.log when
* --test is used on the command line.</p>
* 
* <p>Copyright: Copyright (c) 2005</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
*/
public final class UIHarness {
  public static  final String  rcsid =  "$Id$";
  
  public static final UIHarness INSTANCE = new UIHarness();
  
  private boolean initialized = false;
  private boolean headless = false;
  private MainFrame mainFrame = null;
  private boolean verbose = false;
  private boolean log = false;
  
  private UIHarness() {
  }
  
  void setVerbose(boolean verbose) {
    this.verbose = verbose;
    if (isHead()) {
      mainFrame.setVerbose(verbose);
    }
  }
  
  void setLog(boolean log) {
    this.log = log;
  }
  
  void moveSubFrame() {
    if (isHead()) {
      mainFrame.moveSubFrame();
    }
  }
  
  void toFront(AxisID axisID) {
    if (isHead()) {
      mainFrame.toFront(axisID);
    }
  }
  
  /**
   * Open a message dialog
   * @param message
   * @param title
   */
  public synchronized void openMessageDialog(String message, String title, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openMessageDialog", message, title, axisID);
      }
      mainFrame.displayMessage(message, title, axisID);
    }
    else {
      log("openMessageDialog", message, title, axisID);
    }
  }
  
  public synchronized void openInfoMessageDialog(String message, String title, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openMessageDialog", message, title, axisID);
      }
      mainFrame.displayInfoMessage(message, title, axisID);
    }
    else {
      log("openMessageDialog", message, title, axisID);
    }
  }
  
  /**
   * open one dialog and display all error messages in messages.
   * @param messages
   * @param title
   */
  public synchronized void openErrorMessageDialog(ProcessMessages message, String title, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        logError("openMessageDialog", message, title, axisID);
      }
      mainFrame.displayErrorMessage(message, title, axisID);
    }
    else {
      logError("openMessageDialog", message, title, axisID);
    }
  }
  
  /**
   * open one dialog and display all warning messages in messages.
   * @param messages
   * @param title
   */
  public synchronized void openWarningMessageDialog(ProcessMessages messages, String title, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        logWarning("openMessageDialog", messages, title, axisID);
      }
      mainFrame.displayWarningMessage(messages, title, axisID);
    }
    else {
      logWarning("openMessageDialog", messages, title, axisID);
    }
  }
  
  /**
   * Open a message dialog
   * @param message
   * @param title
   */
  public synchronized void openMessageDialog(String message, String title) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openMessageDialog", message, title);
      }
      mainFrame.displayMessage(message, title);
    }
    else {
      log("openMessageDialog", message, title);
    }
  }
  
  /**
   * Open a message dialog
   * @param message
   * @param title
   */
  public synchronized void openMessageDialog(String[] message, String title, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openMessageDialog", message, title, axisID);
      }
      mainFrame.displayMessage(message, title, axisID);
    }
    else {
      log("openMessageDialog", message, title, axisID);
    }
  }
  
  public synchronized int openYesNoCancelDialog(String[] message, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openYesNoCancelDialog", message, axisID);
      }
      return mainFrame.displayYesNoCancelMessage(message, axisID);
    }
    log("openYesNoCancelDialog", message, axisID);
    return JOptionPane.YES_OPTION;
  }
  
  public synchronized boolean openYesNoDialog(String message, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openYesNoDialog", message, axisID);
      }
      return mainFrame.displayYesNoMessage(message, axisID);
    }
    log("openYesNoDialog", message, axisID);
    return true;
  }
  
  public synchronized boolean openDeleteDialog(String[] message, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openDeleteDialog", message, axisID);
      }
      return mainFrame.displayDeleteMessage(message, axisID);
    }
    log("openDeleteDialog", message, axisID);
    return true;
  }
  
  public synchronized boolean openYesNoWarningDialog(String message, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openYesNoWarningDialog", message, axisID);
      }
      return mainFrame.displayYesNoWarningDialog(message, axisID);
    }
    log("openYesNoWarningDialog", message, axisID);
    return true;
  }
  
  public synchronized boolean openYesNoDialog(String[] message, AxisID axisID) {
    if (isHead() && !EtomoDirector.getInstance().isTestDone()) {
      if (log) {
        log("openYesNoDialog", message, axisID);
      }
      return mainFrame.displayYesNoMessage(message, axisID);
    }
    log("openYesNoDialog", message, axisID);
    return true;
  }
  
  public void showAxisA() {
    if (isHead()) {
      mainFrame.showAxisA();
    }
  }
  
  public void showAxisB() {
    if (isHead()) {
      mainFrame.showAxisB();
    }
  }
  
  public void showBothAxis() {
    if (isHead()) {
      mainFrame.showBothAxis();
    }
  }
  
  public void pack(BaseManager manager) {
    if (isHead()) {
      manager.packPanel();
      mainFrame.repaint();
      mainFrame.pack();
    }
  }
  
  public void pack(boolean force, BaseManager manager) {
    if (isHead()) {
      manager.packPanel();
      mainFrame.repaint();
      mainFrame.pack(force);
    }
  }
  
  public void pack(AxisID axisID, BaseManager manager) {
    if (isHead()) {
      manager.packPanel(axisID);
      mainFrame.repaint(axisID);
      mainFrame.pack(axisID);

    }
  }
  
  public void pack(AxisID axisID, boolean force, BaseManager manager) {
    if (isHead()) {
      manager.packPanel(axisID);
      mainFrame.repaint(axisID);
      mainFrame.pack(axisID, force);

    }
  }
  
  public void setEnabledNewTomogramMenuItem(boolean enable) {
    if (isHead()) {
      mainFrame.setEnabledNewTomogramMenuItem(enable);
    }
  }
  
  public void setMRUFileLabels(String[] mRUList) {
    if (isHead()) {
      mainFrame.setMRUFileLabels(mRUList);
    }
  }

  public boolean is3dmodStartupWindow() {
    if (!isHead()) {
      return false;
    }
    return mainFrame.isMenu3dmodStartupWindow();
  }
  
  public boolean is3dmodBinBy2() {
    if (!isHead()) {
      return false;
    }
    return mainFrame.isMenu3dmodBinBy2();
  }
  
  public void doLayout() {
    if (isHead()) {
      mainFrame.doLayout();
    }
  }
  
  public void validate() {
    if (isHead()) {
      mainFrame.validate();
    }
  }
  
  public void setVisible(boolean b) {
    if (isHead()) {
      mainFrame.setVisible(b);
    }
  }
  
  public Dimension getSize() {
    if (isHead()) {
      return mainFrame.getSize();
    }
    return new Dimension(0,0);
  }
  
  public Point getLocation() {
    if (isHead()) {
      return mainFrame.getLocation();
    }
    return new Point(0,0);
  }
  
  public void setCurrentManager(BaseManager currentManager,
      UniqueKey managerKey, boolean newWindow) {
    if (isHead()) {
      mainFrame.setCurrentManager(currentManager, managerKey, newWindow);
    }
  }
  
  public void setCurrentManager(BaseManager currentManager, UniqueKey managerKey) {
    if (isHead()) {
      mainFrame.setCurrentManager(currentManager, managerKey);
    }
  }
  
  public void selectWindowMenuItem(UniqueKey currentManagerKey) {
    if (isHead()) {
      mainFrame.selectWindowMenuItem(currentManagerKey);
    }
  }
  
  /**
   * If there is a head, tells mainFrame to select a window menu item base on
   * currentManagerKey.
   * @param currentManagerKey
   * @param newWindow
   */
  public void selectWindowMenuItem(UniqueKey currentManagerKey, boolean newWindow) {
    if (isHead()) {
      mainFrame.selectWindowMenuItem(currentManagerKey, newWindow);
    }
  }
  
  public void setEnabledNewJoinMenuItem(boolean enable) {
    if (isHead()) {
      mainFrame.setEnabledNewJoinMenuItem(enable);
    }
  }
  
  public void setEnabledNewParallelMenuItem(boolean enable) {
    if (isHead()) {
      mainFrame.setEnabledNewParallelMenuItem(enable);
    }
  }
  
  public void setEnabledNewPeetMenuItem(boolean enable) {
    if (isHead()) {
      mainFrame.setEnabledNewPeetMenuItem(enable);
    }
  }
  
  public void setEnabledDuplicatePeetMenuItem(boolean enable) {
    if (isHead()) {
      mainFrame.setEnabledDuplicatePeetMenuItem(enable);
    }
  }
  
  public void addWindow(BaseManager manager, UniqueKey managerKey) {
    if (isHead()) {
      mainFrame.addWindow(manager, managerKey);
    }
  }
  
  public void removeWindow(UniqueKey managerKey) {
    if (isHead()) {
      mainFrame.removeWindow(managerKey);
    }
  }
  
  public void renameWindow(UniqueKey oldKey, UniqueKey newKey) {
    if (isHead()) {
      mainFrame.renameWindow(oldKey, newKey);
    }
  }
  
  public void repaintWindow() {
    if (isHead()) {
      mainFrame.repaintWindow();
    }
  }
  
  /**
   * Initialize if necessary.  Instantiate mainFrame if headless is false.
   *
   */
  public void createMainFrame() {
    if (!initialized) {
      initialize();
    }
    if (!headless && mainFrame == null) {
      mainFrame = new MainFrame();
      mainFrame.setVerbose(verbose);
    }
  }

  /**
   * Initialize if necessary.
   * @return True, if mainFrame is not equal to null.
   */
  private boolean isHead() {
    if (!initialized) {
      initialize();
    }
    return mainFrame != null;
  }
  
  /**
   * Initialize headless, testLog, and logWriter.
   *
   */
  private void initialize() {
    initialized = true;
    EtomoDirector etomo = EtomoDirector.getInstance();
    headless = etomo.isHeadless();
  }
  
  /**
   * Log the parameters in testLog with logWriter.
   * @param function
   * @param message
   * @param axisID
   */
  private void log(String function, String message, AxisID axisID) {
    log(function, message, null, axisID);
  }
  
  /**
   * Log the parameters in testLog with logWriter.
   * @param function
   * @param message
   * @param title
   * @param axisID
   */
  private void log(String function, String message, String title) {
    log(function, message, title, AxisID.ONLY);
  }
  
  /**
   * Log the parameters in testLog with logWriter.
   * @param function
   * @param message
   * @param title
   * @param axisID
   */
  private void log(String function, String message, String title, AxisID axisID) {
    System.err.println();
    System.err.println(function + ", " + axisID + ", " + title + ":");
    System.err.println(message);
    System.err.flush();
  }
  
  private void logError(String function, ProcessMessages processMessages, String title, AxisID axisID) {
    System.err.println();
    System.err.println(function + ", " + axisID + ", " + title + ":");
    processMessages.printError();
    System.err.flush();
  }
  
  private void logWarning(String function, ProcessMessages processMessages, String title, AxisID axisID) {
    System.err.println();
    System.err.println(function + ", " + axisID + ", " + title + ":");
    processMessages.printWarning();
    System.err.flush();
  }
  
  /**
   * Log the parameters in testLog with logWriter.
   * @param function
   * @param message
   * @param axisID
   */
  private void log(String function, String[] message, AxisID axisID) {
    log(function, message, null, axisID);
  }
  
  /**
   * Log the parameters in testLog with logWriter.
   * @param function
   * @param message
   * @param title
   * @param axisID
   */
  private void log(String function, String[] message, String title,
      AxisID axisID) {
    System.err.println();
    if (title == null) {
      System.err.print(function + ", " + axisID + ":");
    }
    else {
      System.err.print(function + ", " + axisID + ", " + title + ":");
    }
    System.err.println();
    if (message != null) {
      for (int i = 0; i < message.length; i++) {
        System.err.println(message[i]);
      }
    }
    System.err.flush();
  }
}
/**
* <p> $Log$
* <p> Revision 1.24  2007/02/19 22:04:03  sueh
* <p> bug# 964 Added setEnabledNewPeetMenuItem.
* <p>
* <p> Revision 1.23  2006/06/06 18:14:59  sueh
* <p> bug# 766 Add a logging option, which always writes popup messages to the
* <p> error, and also pops them up.
* <p>
* <p> Revision 1.22  2006/04/25 19:37:07  sueh
* <p> bug# 787 Added testDone to EtomoDirector so that Etomo can exit
* <p> without popups when the UITest package fails or asks Etomo to exit.
* <p>
* <p> Revision 1.21  2006/03/20 18:08:05  sueh
* <p> bug# 835 Added menu option to create a new ParallelManager.
* <p>
* <p> Revision 1.20  2006/01/11 23:16:43  sueh
* <p> bug# 675 added setVerbose() and getCurrentPopupName().
* <p>
* <p> Revision 1.19  2005/12/23 02:24:20  sueh
* <p> bug# 675 Split the test option functionality into headless and test.
* <p>
* <p> Revision 1.18  2005/12/09 20:37:08  sueh
* <p> bug# Added an info message popup
* <p>
* <p> Revision 1.17  2005/12/08 00:59:12  sueh
* <p> bug# 504 Added openYesNoWarningDialog() which displays a yes/no
* <p> popup with No selected and a warning icon.
* <p>
* <p> Revision 1.16  2005/11/02 22:15:15  sueh
* <p> bug# 754 Integrating ProcessMessages.  Added functions logError,
* <p> logWarning, openErrorMessageDialog, openWarningMessageDialog.
* <p>
* <p> Revision 1.15  2005/09/22 21:34:42  sueh
* <p> bug# 532 Moved ApplicationManager.packDialogs functions to
* <p> BaseManager and renamed them packPanel.
* <p>
* <p> Revision 1.14  2005/08/12 00:21:26  sueh
* <p> bug# 711 changed StartUpWindow to StartupWindow.
* <p>
* <p> Revision 1.13  2005/08/12 00:02:43  sueh
* <p> bug# 711  Add is3dmodStartUpWindow() and 3dmodBinBy2() to get the
* <p> menu settings.
* <p>
* <p> Revision 1.12  2005/08/04 20:18:10  sueh
* <p> bug# 532  Centralizing fit window functionality by placing fitting functions
* <p> in UIHarness.  Removing packMainWindow from the manager.  Sending
* <p> the manager to UIHarness.pack() so that packDialogs() can be called.
* <p>
* <p> Revision 1.11  2005/07/01 21:26:47  sueh
* <p> bug# 619 Temporality getting the frame to use with the demo
* <p>
* <p> Revision 1.10  2005/06/21 00:49:13  sueh
* <p> bug# 522 Added comment
* <p>
* <p> Revision 1.9  2005/06/17 00:35:32  sueh
* <p> Removed unnecessary imports.
* <p>
* <p> Revision 1.8  2005/06/16 20:10:13  sueh
* <p> bug# 692 Log messages to the err log instead of a separate file.
* <p>
* <p> Revision 1.7  2005/06/01 21:29:17  sueh
* <p> bug# 667 Removing the Controller classes.  Trying make meta data and
* <p> app manager equals didn't work very well.  Meta data is created by and
* <p> managed by app mgr and the class structure should reflect that.
* <p>
* <p> Revision 1.6  2005/05/18 22:48:53  sueh
* <p> bug# 662 Added an openMessageDialog function which doesn't require
* <p> specifying the axisID (defaults to AxisID.ONLY).  Added
* <p> openDeleteDialog().
* <p>
* <p> Revision 1.5  2005/05/10 03:28:38  sueh
* <p> bug# 615 Do not create etomo_test.log unless --test is set.
* <p>
* <p> Revision 1.4  2005/04/27 02:20:10  sueh
* <p> bug# 615 Removed createMenus(), since it does not have to be visible.
* <p> In createMainFrame() make sure that mainFrame cannot be instantiated
* <p> more then once.
* <p>
* <p> Revision 1.3  2005/04/26 18:35:33  sueh
* <p> bug# 615 Fixed a bug in log().  LogWriter was not flushing to the file.
* <p>
* <p> Revision 1.2  2005/04/26 17:44:30  sueh
* <p> bug# 615 Made MainFrame a package-level class.  Added all functions
* <p> necessary to handle all MainFrame functionality through UIHarness.  This
* <p> makes Etomo more compatible with JUnit.
* <p>
* <p> Revision 1.1  2005/04/25 21:42:16  sueh
* <p> bug# 615 Passing the axis where a command originates to the message
* <p> functions so that the message will be popped up in the correct window.
* <p> This requires adding AxisID to many objects.  Move the interface for
* <p> popping up message dialogs to UIHarness.  It prevents headless
* <p> exceptions during a test execution.  It also allows logging of dialog
* <p> messages during a test.  It also centralizes the dialog interface and
* <p> allows the dialog functions to be synchronized to prevent dialogs popping
* <p> up in both windows at once.  All Frame functions will use UIHarness as a
* <p> public interface.
* <p> </p>
*/