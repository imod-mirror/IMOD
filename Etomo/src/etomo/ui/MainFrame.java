package etomo.ui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import etomo.ApplicationManager;
import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.storage.EtomoFileFilter;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 3.12.2.3  2004/09/09 17:39:03  sueh
 * <p> bug# 520 Add the Window menu.  Add current window list menu item.
 * <p> Move call to createMenus out of MainFrame() because the list of managers
 * <p> hasn't been built when MainFrame is created.  Create
 * <p> setCurrentWindowLabels() which created a new current window menu list.
 * <p>
 * <p> Revision 3.12.2.2  2004/09/08 22:37:34  sueh
 * <p> bug# 520 removed fields that only belong in MainPanel
 * <p>
 * <p> Revision 3.12.2.1  2004/09/07 17:59:47  sueh
 * <p> bug# 520 encapsulated mainPanel
 * <p>
 * <p> Revision 3.12  2004/07/24 01:53:52  sueh
 * <p> bug# 513 make sure that packAxis() won't fail if it is called
 * <p> when the Setup dialog is running.
 * <p>
 * <p> Revision 3.11  2004/07/24 01:48:04  sueh
 * <p> bug# 513 making fitWindow() public so that app manager can
 * <p> call it
 * <p>
 * <p> Revision 3.10  2004/07/23 23:00:00  sueh
 * <p> bug# 517, bug# 513 comments, renamed functions for clarity,
 * <p> removed code in setDividerPostion() which was making
 * <p> fitWindow() fail when the frame was taller then the screen,
 * <p> moved tooSmall() from AxisPanelPRocess and renamed it
 * <p> isFitScreenError.
 * <p>
 * <p> Revision 3.9  2004/07/23 00:08:16  sueh
 * <p> bug# 517 Don't use setSize() anymore because the layout
 * <p> manager doesn't reliably fix the layout after this call.
 * <p> To get the layout manager to limit the size of the dialogs
 * <p> to the viewable area, and a new rootPanel with a BoxLayout,
 * <p> make it the root of the mainPanel.
 * <p>
 * <p> Revision 3.8  2004/07/21 21:08:33  sueh
 * <p> bug# 512 added options menu items Axis A, Axis B, and Both
 * <p> Axes.  Removed options menu item Advanced
 * <p>
 * <p> Revision 3.7  2004/07/16 23:01:03  sueh
 * <p> bug# 501 sending System.out prints only when debug is set
 * <p>
 * <p> Revision 3.6  2004/07/16 22:05:13  sueh
 * <p> bug# 501 adjusting divider to fix problem with
 * <p> JsplitPane.resetToPreferedSizes() that happens when
 * <p> etomo is too wide for the screen
 * <p>
 * <p> Revision 3.5  2004/05/19 23:17:14  sueh
 * <p> bug# 425 fixing single axis bug
 * <p>
 * <p> Revision 3.4  2004/05/19 23:11:47  sueh
 * <p> bug# 425 if the window is too tall for the screen, resize it
 * <p>
 * <p> Revision 3.3  2004/05/15 01:42:12  sueh
 * <p> bug# 415 MainFrame is already calling System.exit(), so don't
 * <p> do EXIT_ON_CLOSE when X button is pressed.
 * <p>
 * <p> Revision 3.2  2004/04/28 22:38:14  sueh
 * <p> bug# 268 if a panel was hidden, set the divider location to
 * <p> continue to hide it
 * <p>
 * <p> Revision 3.1  2004/02/13 17:39:03  sueh
 * <p> bug# 268 during fit window, if the split bar is all the way to
 * <p> one side, hide the hidden side, resize as usual, then show it
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.21  2003/11/04 22:02:17  sueh
 * <p> bug329 MainFrame: setMRUFileLabels(): Don't display blank
 * <p> MRU entries
 * <p>
 * <p> Revision 2.20  2003/11/04 20:56:11  rickg
 * <p> Bug #345 IMOD Directory supplied by a static function from ApplicationManager
 * <p>
 * <p> Revision 2.19  2003/11/03 19:36:09  sueh
 * <p> bug266 EtomoFileFilter:  added an implements clause to the class def to allow the use
 * <p> of this file with File.listFiles(FileFilter)
 * <p>
 * <p> Revision 2.18  2003/10/30 20:29:55  rickg
 * <p> Bug# 341 3dmod and eTomo users guide
 * <p>
 * <p> Revision 2.17  2003/10/15 17:27:05  sueh
 * <p> Bug266 added a file filter to getTestParamFilename()
 * <p>
 * <p> Revision 2.16  2003/09/30 03:13:38  rickg
 * <p> bug248 File / open now calls the correct function in the
 * <p> AppManager (as does the MRU list opens)
 * <p>
 * <p> Revision 2.15  2003/09/30 02:15:09  rickg
 * <p> Added message dialogs that were originally in the
 * <p> ApplicationManager
 * <p>
 * <p> Revision 2.14  2003/06/10 05:14:53  rickg
 * <p> *** empty log message ***
 * <p>
 * <p> Revision 2.13  2003/05/27 08:54:18  rickg
 * <p> Determinant progress bar now takes a string
 * <p>
 * <p> Revision 2.12  2003/05/23 14:22:38  rickg
 * <p> Progress bar determinant delegate methods
 * <p> axisPanel mapping method
 * <p>
 * <p> Revision 2.11  2003/05/19 22:10:03  rickg
 * <p> Added new to file menu
 * <p> Added tomography guide and imod guide to help menu
 * <p> Restructured action handlers
 * <p>
 * <p> Revision 2.10  2003/05/19 04:54:18  rickg
 * <p> Added mnemonics for menus
 * <p>
 * <p> Revision 2.9  2003/05/15 22:25:14  rickg
 * <p> created separate setDividerLocation method to correctly update
 * <p> panel
 * <p>
 * <p> Revision 2.8  2003/05/15 20:21:22  rickg
 * <p> Added extra validation call hopefully to make sure divider gets rendered
 * <p> correctly
 * <p>
 * <p> Revision 2.7  2003/05/08 19:58:25  rickg
 * <p> Addd post processing state update on an updateAll... call
 * <p>
 * <p> Revision 2.6  2003/05/07 17:49:12  rickg
 * <p> System property user.dir now defines the working directory
 * <p> Updated status bar
 * <p>
 * <p> Revision 2.5  2003/04/24 17:46:54  rickg
 * <p> Changed fileset name to dataset name
 * <p>
 * <p> Revision 2.4  2003/03/20 17:42:18  rickg
 * <p> Comment update
 * <p>
 * <p> Revision 2.3  2003/01/29 15:18:19  rickg
 * <p> Added combine state setter
 * <p>
 * <p> Revision 2.2  2003/01/28 00:15:43  rickg
 * <p> Main window now remembers its size
 * <p>
 * <p> Revision 2.1  2003/01/27 23:51:23  rickg
 * <p> Added a split pane manager to the mane window for dual
 * <p> axis layout
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.9.2.2  2003/01/24 18:43:37  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.9.2.1  2003/01/11 00:45:27  rickg
 * <p> Added app icon
 * <p>
 * <p> Revision 1.9  2002/12/18 20:45:32  rickg
 * <p> Advanced menu implemented
 * <p>
 * <p> Revision 1.8  2002/12/11 21:28:29  rickg
 * <p> Implemented repaint method, doesn't work well
 * <p>
 * <p> Revision 1.7  2002/12/11 00:37:26  rickg
 * <p> Added handler for options/settings menu
 * <p>
 * <p> Revision 1.6  2002/12/09 04:42:29  rickg
 * <p> Automatically add .edf extenstion when doing save as or
 * <p> save with not existing filename
 * <p>
 * <p> Revision 1.5  2002/12/09 04:16:11  rickg
 * <p> Added EDF file filter to open dialog
 * <p>
 * <p> Revision 1.4  2002/11/19 05:32:55  rickg
 * <p> Label spelling correction
 * <p>
 * <p> Revision 1.3  2002/11/14 21:18:37  rickg
 * <p> Added anchors into the tomoguide
 * <p>
 * <p> Revision 1.2  2002/10/07 22:31:18  rickg
 * <p> removed unused imports
 * <p> reformat after emacs trashed it
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class MainFrame extends JFrame implements ContextMenu {
  public static final String rcsid =
    "$Id$";

  private JPanel rootPanel;
  private MainPanel mainPanel = null;
  
  //convenience variable
  private EtomoDirector etomoDirector = EtomoDirector.getInstance();

  //  Menu bar
  private final int nMRUFileMax = 10;
  private JMenuBar menuBar = new JMenuBar();

  private JMenu menuFile = new JMenu("File");
  private JMenuItem menuFileNew = new JMenuItem("New", KeyEvent.VK_N);
  private JMenuItem menuFileOpen = new JMenuItem("Open...", KeyEvent.VK_O);
  private JMenuItem menuFileSave = new JMenuItem("Save", KeyEvent.VK_S);
  private JMenuItem menuFileSaveAs = new JMenuItem("Save As", KeyEvent.VK_A);
  private JMenuItem menuFileExit = new JMenuItem("Exit", KeyEvent.VK_X);
  private JMenuItem[] menuMRUList = new JMenuItem[nMRUFileMax];

  private JMenu menuOptions = new JMenu("Options");
  private JMenuItem menuAxisA = new JMenuItem("Axis A", KeyEvent.VK_A);
  private JMenuItem menuAxisB = new JMenuItem("Axis B", KeyEvent.VK_B);
  private JMenuItem menuAxisBoth = new JMenuItem("Both Axes", KeyEvent.VK_2);
  private JMenuItem menuSettings = new JMenuItem("Settings", KeyEvent.VK_S);
  private JMenuItem menuFitWindow = new JMenuItem("Fit Window", KeyEvent.VK_F);
  
  private JMenu menuWindow = new JMenu("Window");
  private JMenuItem[] menuCurrentList = null;
  
  private JMenu menuHelp = new JMenu("Help");
  private JMenuItem menuTomoGuide =
    new JMenuItem("Tomography Guide", KeyEvent.VK_T);
  private JMenuItem menuImodGuide = new JMenuItem("Imod Users Guide", KeyEvent.VK_I);
  private JMenuItem menu3dmodGuide = new JMenuItem("3dmod Users Guide", KeyEvent.VK_3);
	private JMenuItem menuEtomoGuide = new JMenuItem("Etomo Users Guide", KeyEvent.VK_E);
  private JMenuItem menuHelpAbout = new JMenuItem("About", KeyEvent.VK_A);  
  
  //manager object
  private BaseManager currentManager;
  GenericMouseAdapter mouseAdapter = null;

  /**
   * Main window constructor.  This sets up the menus and status line.
   */
  public MainFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

    ImageIcon iconEtomo =
      new ImageIcon(ClassLoader.getSystemResource("images/etomo.png"));
    setIconImage(iconEtomo.getImage());

    setTitle("eTomo");
    
    rootPanel = (JPanel) getContentPane();
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));

    //  add the context menu to all of the main window objects
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  }
  
  public void setCurrentManager(BaseManager currentManager) {
    this.currentManager = currentManager;
    if (mainPanel != null) {
      rootPanel.remove(mainPanel);
    }
    mainPanel = currentManager.getMainPanel();
    rootPanel.add(mainPanel);
    mainPanel.addMouseListener(mouseAdapter);
    pack();
  }
  
  //  Right mouse button context menu
  public void popUpContextMenu(MouseEvent mouseEvent) {
    ContextPopup contextPopup = new ContextPopup(mainPanel, mouseEvent, "");
  }

  /**
   * Set the MRU etomo data file list.  This fills in the MRU menu items
   * on the File menu
   */
  public void setMRUFileLabels(String[] mRUList) {
    for (int i = 0; i < mRUList.length; i++) {
      if (i == nMRUFileMax) {
        return;
      }
      if (mRUList[i].equals("")) {
        menuMRUList[i].setVisible(false);
      }
      else {
        menuMRUList[i].setText(mRUList[i]);
        menuMRUList[i].setVisible(true);
      }
    }
    for (int i = mRUList.length; i < nMRUFileMax; i++) {
      menuMRUList[i].setVisible(false);
    }
  }
  
  
  /**
   * Set the open etomo data file list.  This fills in the current window items
   * on the Window menu
   */
  public void setCurrentWindowLabels() {
    //remove old list
    if (menuCurrentList != null) {
      for (int i = 0; i < menuCurrentList.length; i++) {
        menuWindow.remove(menuCurrentList[i]);
      }
    }
    int numCurrentWindows = etomoDirector.getManagerListSize();
    JMenuItem[] menuCurrentList = new JMenuItem[numCurrentWindows];
    System.out.println("numCurrentWindows=" + numCurrentWindows);
    CurrentWindowListActionListener currentWindowListActionListener =
      new CurrentWindowListActionListener(this);
    for (int i = 0; i < numCurrentWindows; i++) {
      System.out.println("etomoDirector.getManagerName(i)=" + etomoDirector.getManagerName(i));
      menuCurrentList[i] = new JMenuItem();
      menuCurrentList[i].addActionListener(currentWindowListActionListener);
      String managerName = etomoDirector.getManagerName(i);
      if (managerName.equals("")) {
        menuCurrentList[i].setVisible(false);
      }
      else {
        menuCurrentList[i].setText(managerName);
        menuCurrentList[i].setVisible(true);
      }
      menuWindow.add(menuCurrentList[i]);
    }
  }


  public boolean getTestParamFilename() {
    //  Open up the file chooser in current working directory
    File workingDir = new File(System.getProperty("user.dir"));
    JFileChooser chooser =
      new JFileChooser(workingDir);
    EtomoFileFilter edfFilter = new EtomoFileFilter();
    chooser.setFileFilter(edfFilter);
    chooser.setDialogTitle("Save etomo data file");
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    chooser.setPreferredSize(FixedDim.fileChooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    File[] edfFiles = workingDir.listFiles(edfFilter);
    if (edfFiles.length == 0) {
      File defaultFile = new File(workingDir, currentManager.getMetaData().getDatasetName() + ".edf");
      chooser.setSelectedFile(defaultFile);
    }
    int returnVal = chooser.showSaveDialog(this);

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      return false;
    }
    // If the file does not already have an extension appended then add an edf
    // extension
    File edfFile = chooser.getSelectedFile();
    String fileName = chooser.getSelectedFile().getName();
    if (fileName.indexOf(".") == -1) {
      edfFile = new File(chooser.getSelectedFile().getAbsolutePath() + ".edf");

    }
    currentManager.setTestParamFile(edfFile);
    return true;
  }

  /**
   * Handle File menu actions
   * @param event
   */
  private void menuFileAction(ActionEvent event) {
    if (event.getActionCommand().equals(menuFileNew.getActionCommand())) {
      currentManager.openNewDataset();
    }

    if (event.getActionCommand().equals(menuFileOpen.getActionCommand())) {
      currentManager.openExistingDataset(null);
    }

    if (event.getActionCommand().equals(menuFileSave.getActionCommand())) {
      //  Check to see if there is a current parameter file chosen
      //  if not open a dialog box to select the name
      boolean haveTestParamFilename = true;
      if (currentManager.getTestParamFile() == null) {
        haveTestParamFilename = getTestParamFilename();
      }
      if (haveTestParamFilename) {
        currentManager.saveTestParamFile();
      }
    }

    if (event.getActionCommand().equals(menuFileSaveAs.getActionCommand())) {
      boolean haveTestParamFilename = getTestParamFilename();
      if (haveTestParamFilename) {
        currentManager.saveTestParamFile();
      }
    }

    if (event.getActionCommand().equals(menuFileExit.getActionCommand())) {
      //  Check to see if we need to save any data
      if (currentManager.exitProgram()) {
        System.exit(0);
      }
    }
  }
  
  private void menuWindowAction(ActionEvent event) {
  }

  /**
   * Open the specified MRU EDF file
   * @param event
   */
  private void menuFileMRUListAction(ActionEvent event) {
    currentManager.openExistingDataset(new File(event.getActionCommand()));
  }
  
  /**
   * Open the specified window
   * @param event
   */
  private void currentWindowListAction(ActionEvent event) {
    etomoDirector.setCurrentManager(event.getActionCommand());
  }

  /**
   * checks for a bug in windows that causes MainFrame.fitScreen() to move the
   * divider almost all the way to the left
   * @return
   */
  protected boolean isFitScreenError(AxisProcessPanel axisPanel) {
    if (axisPanel.getWidth() <= 16) {
      return true;
    }
    return false;
  }

  /**
   * Handle the options menu events
   * @param event
   */
  private void menuOptionsAction(ActionEvent event) {
    String command = event.getActionCommand();
    if (command.equals(menuSettings.getActionCommand())) {
      currentManager.openSettingsDialog();
    }
    else if (command.equals(menuAxisA.getActionCommand())) {
      mainPanel.setDividerLocation(1);
    }
    else if (command.equals(menuAxisB.getActionCommand())) {
      mainPanel.setDividerLocation(0);
    }
    else if (command.equals(menuAxisBoth.getActionCommand())) {
      mainPanel.setDividerLocation(.5);
    }
    else if (command.equals(menuFitWindow.getActionCommand())) {
      mainPanel.fitWindow();
    }
  }
  
  private void menuHelpAction(ActionEvent event) {

		// Get the URL to the IMOD html directory
		String imodURL = "";
		try {
			imodURL =
				ApplicationManager.getIMODDirectory().toURL().toString() + "/html/";
		}
		catch (MalformedURLException except) {
			except.printStackTrace();
			System.err.println("Malformed URL:");
			System.err.println(ApplicationManager.getIMODDirectory().toString());
			return;
		}
  	
    if (event.getActionCommand().equals(menuTomoGuide.getActionCommand())) {
      HTMLPageWindow manpage = new HTMLPageWindow();
      manpage.openURL(imodURL + "tomoguide.html");
      manpage.setVisible(true);
    }

    if (event.getActionCommand().equals(menuImodGuide.getActionCommand())) {
      HTMLPageWindow manpage = new HTMLPageWindow();
      manpage.openURL(imodURL + "guide.html");
      manpage.setVisible(true);
    }

		if (event.getActionCommand().equals(menu3dmodGuide.getActionCommand())) {
			HTMLPageWindow manpage = new HTMLPageWindow();
			manpage.openURL(imodURL + "3dmodguide.html");
			manpage.setVisible(true);
		}

		if (event.getActionCommand().equals(menuEtomoGuide.getActionCommand())) {
			HTMLPageWindow manpage = new HTMLPageWindow();
			manpage.openURL(imodURL + "UsingEtomo.html");
			manpage.setVisible(true);
		}


    if (event.getActionCommand().equals(menuHelpAbout.getActionCommand())) {
      MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      dlg.setLocation(
        (frmSize.width - dlgSize.width) / 2 + loc.x,
        (frmSize.height - dlgSize.height) / 2 + loc.y);
      dlg.setModal(true);
      dlg.show();
    }
  }

  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent event) {
    super.processWindowEvent(event);
    if (event.getID() == WindowEvent.WINDOW_CLOSING) {
      menuFileExit.doClick();
    }
  }

  /**
   * Open a Yes or No question dialog
   * @param message
   * @return boolean True if the Yes option was selected
   */
  public boolean openYesNoDialog(String[] message) {
    try {
      int answer =
        JOptionPane.showConfirmDialog(
          this,
          message,
          "Etomo question",
          JOptionPane.YES_NO_OPTION);

      if (answer == JOptionPane.YES_OPTION) {
        return true;
      }
      else {
        return false;
      }
    }
    catch (HeadlessException except) {
      except.printStackTrace();
      return false;
    }
  }

  /**
   * Open a Yes, No or Cancel question dialog
   * @param message
   * @return int state of the users select
   */
  public int openYesNoCancelDialog(String[] message) {
    return JOptionPane.showConfirmDialog(
      this,
      message,
      "Etomo question",
      JOptionPane.YES_NO_CANCEL_OPTION);
  }

  /**
   * Open a message dialog
   * @param message
   * @param title
   */
  public void openMessageDialog(Object message, String title) {
    JOptionPane.showMessageDialog(
      this,
      message,
      title,
      JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Open a File Chooser dialog with an EDF filter, if the user selects
   * or names a file return a File object wiht that slected, otherwise
   * return null.
   * @return A File object specifiying the selected file or null if none
   * was selected. 
   */
  public File openEtomoDataFileDialog() {
    //  Open up the file chooser in current working directory
    JFileChooser chooser =
      new JFileChooser(new File(System.getProperty("user.dir")));
    EtomoFileFilter edfFilter = new EtomoFileFilter();
    chooser.setFileFilter(edfFilter);

    chooser.setDialogTitle("Open etomo data file");
    chooser.setPreferredSize(FixedDim.fileChooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }
    return null;
  }

  //  TODO Need a way to repaint the existing font
  public void repaintWindow() {
    repaintContainer(this);
    this.repaint();
  }

  private void repaintContainer(Container container) {
    Component[] comps = container.getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof Container) {
        Container cont = (Container) comps[i];
        repaintContainer(cont);
      }
      comps[i].repaint();
    }
  }

  /**
   * Create the menus for the main window
   */
  public void createMenus() {
    //  Mnemonics for the main menu bar
    menuFile.setMnemonic(KeyEvent.VK_F);
    menuOptions.setMnemonic(KeyEvent.VK_O);
    menuHelp.setMnemonic(KeyEvent.VK_H);

    //  Accelerators
    menuSettings.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

    menuAxisA.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
    menuAxisB.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
    menuAxisBoth.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
      
    menuFitWindow.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));

    //  Bind the menu items to their listeners
    FileActionListener fileActionListener = new FileActionListener(this);
    menuFileNew.addActionListener(fileActionListener);
    menuFileOpen.addActionListener(fileActionListener);
    menuFileSave.addActionListener(fileActionListener);
    menuFileSaveAs.addActionListener(fileActionListener);
    menuFileExit.addActionListener(fileActionListener);

    OptionsActionListener optionsActionListener =
      new OptionsActionListener(this);
    menuSettings.addActionListener(optionsActionListener);
    menuFitWindow.addActionListener(optionsActionListener);
    menuAxisA.addActionListener(optionsActionListener);
    menuAxisB.addActionListener(optionsActionListener);
    menuAxisBoth.addActionListener(optionsActionListener);
    
    WindowActionListener windowActionListener =
      new WindowActionListener(this);
    setCurrentWindowLabels(); 
    
    HelpActionListener helpActionListener = new HelpActionListener(this);
    menuTomoGuide.addActionListener(helpActionListener);
    menuImodGuide.addActionListener(helpActionListener);
		menu3dmodGuide.addActionListener(helpActionListener);
		menuEtomoGuide.addActionListener(helpActionListener);
    menuHelpAbout.addActionListener(helpActionListener);

    //  File menu
    menuFile.add(menuFileNew);
    menuFile.add(menuFileOpen);
    menuFile.add(menuFileSave);
    menuFile.add(menuFileSaveAs);
    menuFile.add(menuFileExit);
    menuFile.addSeparator();

    //  Initialize all of the MRU file menu items
    FileMRUListActionListener fileMRUListActionListener =
      new FileMRUListActionListener(this);
    for (int i = 0; i < nMRUFileMax; i++) {
      menuMRUList[i] = new JMenuItem();
      menuMRUList[i].addActionListener(fileMRUListActionListener);
      menuMRUList[i].setVisible(false);
      menuFile.add(menuMRUList[i]);
    }

    // Options menu
    menuOptions.add(menuSettings);
    menuOptions.add(menuAxisA);
    menuOptions.add(menuAxisB);
    menuOptions.add(menuAxisBoth);
    menuOptions.add(menuFitWindow);

    // Help menu
    menuHelp.add(menuTomoGuide);
    menuHelp.add(menuImodGuide);
		menuHelp.add(menu3dmodGuide);
		menuHelp.add(menuEtomoGuide);
    menuHelp.add(menuHelpAbout);

    //  Construct menu bar
    menuBar.add(menuFile);
    menuBar.add(menuOptions);
    menuBar.add(menuWindow);
    menuBar.add(menuHelp);
    setJMenuBar(menuBar);
  }

  //  File menu action listener
  class FileActionListener implements ActionListener {
    MainFrame adaptee;

    FileActionListener(MainFrame adaptee) {
      this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent event) {
      adaptee.menuFileAction(event);
    }
  }

  //  MRU file list action listener
  class FileMRUListActionListener implements ActionListener {
    MainFrame adaptee;

    FileMRUListActionListener(MainFrame adaptee) {
      this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e) {
      adaptee.menuFileMRUListAction(e);
    }
  }

  //  MRU file list action listener
  class CurrentWindowListActionListener implements ActionListener {
    MainFrame adaptee;

    CurrentWindowListActionListener(MainFrame adaptee) {
      this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e) {
      adaptee.currentWindowListAction(e);
    }
  }

  // Options file action listener
  class OptionsActionListener implements ActionListener {
    MainFrame adaptee;

    OptionsActionListener(MainFrame adaptee) {
      this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e) {
      adaptee.menuOptionsAction(e);
    }
  }

  // Options file action listener
  class WindowActionListener implements ActionListener {
    MainFrame adaptee;

    WindowActionListener(MainFrame adaptee) {
      this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e) {
      adaptee.menuWindowAction(e);
    }
  }

  // Help file action listener
  class HelpActionListener implements ActionListener {
    MainFrame adaptee;

    HelpActionListener(MainFrame adaptee) {
      this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e) {
      adaptee.menuHelpAction(e);
    }
  }

}
