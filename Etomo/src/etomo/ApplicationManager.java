package etomo;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import etomo.comscript.BadComScriptException;
import etomo.comscript.BeadtrackParam;
import etomo.comscript.CCDEraserParam;
import etomo.comscript.ComScriptManager;
import etomo.comscript.CombineParams;
import etomo.comscript.ConstTiltalignParam;
import etomo.comscript.FortranInputSyntaxException;
import etomo.comscript.MatchorwarpParam;
import etomo.comscript.NewstParam;
import etomo.comscript.Patchcrawl3DParam;
import etomo.comscript.SolvematchmodParam;
import etomo.comscript.SolvematchshiftParam;
import etomo.comscript.TiltParam;
import etomo.comscript.TiltalignParam;
import etomo.comscript.TiltxcorrParam;
import etomo.comscript.TransferfidParam;
import etomo.comscript.TrimvolParam;
import etomo.process.ImodManager;
import etomo.process.ProcessManager;
import etomo.process.ProcessState;
import etomo.process.SystemProcessException;
import etomo.process.SystemProgram;
import etomo.storage.ParameterStore;
import etomo.storage.Storable;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.AxisTypeException;
import etomo.type.DialogExitState;
import etomo.type.MetaData;
import etomo.type.ProcessTrack;
import etomo.type.UserConfiguration;
import etomo.ui.AlignmentEstimationDialog;
import etomo.ui.CoarseAlignDialog;
import etomo.ui.ContextPopup;
import etomo.ui.FiducialModelDialog;
import etomo.ui.MainFrame;
import etomo.ui.PostProcessingDialog;
import etomo.ui.PreProcessingDialog;
import etomo.ui.ProcessDialog;
import etomo.ui.SettingsDialog;
import etomo.ui.SetupDialog;
import etomo.ui.TomogramCombinationDialog;
import etomo.ui.TomogramGenerationDialog;
import etomo.ui.TomogramPositioningDialog;
import etomo.util.InvalidParameterException;

/**
 * <p>Description: Provides the main entry point, handles high level message 
 *  processing, management of other high-level</p>
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
 * <p> Revision 2.42  2003/05/15 22:24:24  rickg
 * <p> Reordered method sequence in opening processing panel to
 * <p> prevent slider from taking up all of the window.
 * <p>
 * <p> Revision 2.41  2003/05/15 20:13:05  rickg
 * <p> Fixed PLAF for windows
 * <p>
 * <p> Revision 2.40  2003/05/15 19:39:44  rickg
 * <p> Look and feel handling
 * <p>
 * <p> Revision 2.39  2003/05/14 23:22:51  rickg
 * <p> Exit if no IMOD_DIR is defined.  We can't run any of the non com scripts
 * <p>
 * <p> Revision 2.38  2003/05/14 21:45:27  rickg
 * <p> New trimvol constructor for windows
 * <p>
 * <p> Revision 2.37  2003/05/14 14:36:08  rickg
 * <p> Temporary change to volcombine
 * <p>
 * <p> Revision 2.36  2003/05/13 19:58:22  rickg
 * <p> TransferfidParams constructed with IMODDirectory File
 * <p>
 * <p> Revision 2.35  2003/05/10 19:12:56  rickg
 * <p> OS independent path implementation
 * <p>
 * <p> Revision 2.34  2003/05/10 18:01:56  rickg
 * <p> Fixes to get IMOD_DIR home and current working directory
 * <p> in a OS agnostic manner
 * <p>
 * <p> Revision 2.33  2003/05/09 23:25:36  rickg
 * <p> Working change to get env vars from all OSs
 * <p>
 * <p> Revision 2.32  2003/05/09 17:52:59  rickg
 * <p> include this in ImodManager constructor, needed for fiducial model calls
 * <p>
 * <p> Revision 2.31  2003/05/08 23:18:45  rickg
 * <p> Added --debug option, off by default
 * <p>
 * <p> Revision 2.30  2003/05/08 20:14:30  rickg
 * <p> Don't set main window location to (0,0) confuses SGI
 * <p>
 * <p> Revision 2.29  2003/05/08 19:58:53  rickg
 * <p> Work around for bug in File.getParent
 * <p>
 * <p> Revision 2.28  2003/05/07 23:04:29  rickg
 * <p> System property user.dir now defines the working directory
 * <p> Home is now read from the System properties
 * <p>
 * <p> Revision 2.27  2003/04/30 18:48:51  rickg
 * <p> Changed matchcheck* to a single imod instance
 * <p>
 * <p> Revision 2.26  2003/04/28 23:25:25  rickg
 * <p> Changed visible imod references to 3dmod
 * <p>
 * <p> Revision 2.25  2003/04/24 17:46:54  rickg
 * <p> Changed fileset name to dataset name
 * <p>
 * <p> Revision 2.24  2003/04/17 23:11:26  rickg
 * <p> Added cancel handling from post processing dialog
 * <p>
 * <p> Revision 2.23  2003/04/16 22:49:49  rickg
 * <p> Trimvol implmentation
 * <p>
 * <p> Revision 2.22  2003/04/16 00:13:54  rickg
 * <p> Trimvol in progress
 * <p>
 * <p> Revision 2.21  2003/04/14 23:57:18  rickg
 * <p> Trimvol management changes
 * <p>
 * <p> Revision 2.20  2003/04/10 23:40:03  rickg
 * <p> Initial exit function handling of imod and other processes
 * <p> Initial openPostProcessingDialog
 * <p>
 * <p> Revision 2.19  2003/03/26 00:52:25  rickg
 * <p> Added button to convert patch_vector.mod to patch.out
 * <p>
 * <p> Revision 2.18  2003/03/22 00:40:35  rickg
 * <p> slovematchmod label change
 * <p>
 * <p> Revision 2.17  2003/03/20 21:18:55  rickg
 * <p> Added matchshift results button/access
 * <p>
 * <p> Revision 2.16  2003/03/20 16:58:42  rickg
 * <p> Added methods: imodMatchedToTomgram, matchorwarpTrial
 * <p> Added trial mode handling to matchorwarp
 * <p>
 * <p> Revision 2.15  2003/03/19 00:23:04  rickg
 * <p> Added imod patch vector model pass through
 * <p>
 * <p> Revision 2.14  2003/03/18 23:56:54  rickg
 * <p> ComScript method name changes
 * <p> Apropriate loading of combine scripts
 * <p> Added pass through method to open matching models
 * <p> Done not longer executes combine
 * <p> Updated combine related methods to match new
 * <p> combination dialog
 * <p>
 * <p> Revision 2.13  2003/03/18 17:03:15  rickg
 * <p> Combine development in progress
 * <p>
 * <p> Revision 2.12  2003/03/18 15:01:31  rickg
 * <p> Combine development in progress
 * <p>
 * <p> Revision 2.11  2003/03/18 00:32:32  rickg
 * <p> combine development in progress
 * <p>
 * <p> Revision 2.10  2003/03/07 07:22:49  rickg
 * <p> combine layout in progress
 * <p>
 * <p> Revision 2.9  2003/03/06 05:53:28  rickg
 * <p> Combine interface in progress
 * <p>
 * <p> Revision 2.8  2003/03/06 01:19:17  rickg
 * <p> Combine changes in progress
 * <p>
 * <p> Revision 2.7  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p>
 * <p> Revision 2.6  2003/02/24 23:27:21  rickg
 * <p> Added process interrupt method
 * <p>
 * <p> Revision 2.5  2003/01/30 00:43:32  rickg
 * <p> Blank second axis panel when done with tomogram generation
 * <p>
 * <p> Revision 2.4  2003/01/29 15:22:58  rickg
 * <p> Updated logic for combine step
 * <p>
 * <p> Revision 2.3  2003/01/28 20:42:53  rickg
 * <p> Bug fix: save current dialog state when running align.com
 * <p>
 * <p> Revision 2.2  2003/01/28 00:15:29  rickg
 * <p> Main window now remembers its size
 * <p>
 * <p> Revision 2.1  2003/01/27 18:12:41  rickg
 * <p> Fixed bug from single window transition in positioning dialog
 * <p> opening function
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.36.2.1  2003/01/24 18:27:46  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.36  2003/01/10 20:46:34  rickg
 * <p> Added ability to view 3D fiducial models
 * <p>
 * <p> Revision 1.35  2003/01/10 18:39:58  rickg
 * <p> Using existing com scripts now gives the correct
 * <p> process state
 * <p>
 * <p> Revision 1.34  2003/01/10 18:33:16  rickg
 * <p> Added test parameter filename to command line args
 * <p>
 * <p> Revision 1.33  2003/01/08 21:04:38  rickg
 * <p> More descriptive error dialog when the are not
 * <p> available for combining.
 * <p>
 * <p> Revision 1.32  2003/01/07 00:30:16  rickg
 * <p> Added imodViewResidual method
 * <p>
 * <p> Revision 1.31  2003/01/06 04:53:16  rickg
 * <p> Set default parameters for transferfid panel and handle
 * <p> new backwards flag for b to a
 * <p>
 * <p> Revision 1.30  2003/01/04 00:41:00  rickg
 * <p> Implemented transferfid method
 * <p>
 * <p> Revision 1.29  2002/12/19 00:35:20  rickg
 * <p> Implemented persitent advanced state handling
 * <p>
 * <p> Revision 1.27  2002/12/11 21:26:31  rickg
 * <p> Added font setting into user prefs setting
 * <p>
 * <p> Revision 1.26  2002/12/11 05:39:00  rickg
 * <p> Added basic font change method
 * <p>
 * <p> Revision 1.25  2002/12/11 00:39:48  rickg
 * <p> Basic handling of settings dialog
 * <p> added setUserPreferences method
 * <p>
 * <p> Revision 1.24  2002/12/09 04:18:50  rickg
 * <p> Better handling of current working directory, user.dir and
 * <p> metaData always agree now.
 * <p>
 * <p> Revision 1.23  2002/12/05 01:21:02  rickg
 * <p> Added isAdvanced stub
 * <p>
 * <p> Revision 1.22  2002/11/21 19:24:38  rickg
 * <p> Set user.dir to current working directory
 * <p>
 * <p> Revision 1.21  2002/10/29 18:22:04  rickg
 * <p> Simplified rawstack open checking
 * <p>
 * <p> Revision 1.20  2002/10/25 19:30:43  rickg
 * <p> Modifies several catches to explicilty specify exception
 * <p>
 * <p> Revision 1.19  2002/10/24 19:52:55  rickg
 * <p> Added command line --demo argument
 * <p>
 * <p> Revision 1.18  2002/10/22 21:38:24  rickg
 * <p> ApplicationManager now controls both demo and debug
 * <p> modes
 * <p>
 * <p> Revision 1.17  2002/10/17 22:47:35  rickg
 * <p> process dialogs are now managed attributes
 * <p> setVisible calls changed to show
 * <p> unused variable dialogFinshed removed
 * <p>
 * <p> Revision 1.16  2002/10/17 16:23:04  rickg
 * <p> Added private method to update the dependent tilt parameters when the
 * <p> align.com parameters are changed
 * <p>
 * <p> Revision 1.15  2002/10/16 17:37:18  rickg
 * <p> Construct a imodManager when a new data set is opened
 * <p>
 * <p> Revision 1.14  2002/10/14 22:44:27  rickg
 * <p> Added combine to execute section of doneCombine
 * <p>
 * <p> Revision 1.13  2002/10/14 19:04:18  rickg
 * <p> openMessageDialog made public
 * <p>
 * <p> Revision 1.12  2002/10/10 23:40:33  rickg
 * <p> refactored createCombineScripts to setupCombineScripts
 * <p>
 * <p> Revision 1.11  2002/10/10 19:16:19  rickg
 * <p> Get HOME and IMOD_DIR environement variables during
 * <p> initialization instead of each time they requested.  Also
 * <p> exit if they are not available.
 * <p>
 * <p> Revision 1.10  2002/10/09 04:29:17  rickg
 * <p> Implemented calls to updateCombineCom
 * <p>
 * <p> Revision 1.9  2002/10/09 00:04:37  rickg
 * <p> Added default patch boundary logic
 * <p> still needs work on getting combine parameters at the correct times
 * <p>
 * <p> Revision 1.8  2002/10/07 22:20:21  rickg
 * <p> removed unused imports
 * <p>
 * <p> Revision 1.7  2002/09/30 23:44:43  rickg
 * <p> Started implementing updateCombineCom
 * <p>
 * <p> Revision 1.6  2002/09/30 22:01:29  rickg
 * <p> Added check to verify dual axis for combination
 * <p>
 * <p> Revision 1.5  2002/09/20 18:56:09  rickg
 * <p> Added private message and yes/no dialog methods
 * <p> Check to see if the raw stack and coarsely aligned stacks should be
 * <p> closed by the user
 * <p>
 * <p> Revision 1.4  2002/09/19 22:57:56  rickg
 * <p> Imod mangement is now handled through the ImodManager
 * <p>
 * <p> Revision 1.3  2002/09/17 23:44:56  rickg
 * <p> Adding ImodManager, in progress
 * <p>
 * <p> Revision 1.2  2002/09/13 21:29:57  rickg
 * <p> Started updating for ImodManager
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */

public class ApplicationManager {
  public static final String rcsid =
    "$Id$";

  private boolean debug = false;
  private boolean demo = false;

  private boolean isDataParamDirty = false;
  private String homeDirectory;
  private File IMODDirectory;

  private UserConfiguration userConfig = new UserConfiguration();
  private MetaData metaData = new MetaData();
  private File paramFile = null;
  // advanced dialog state for this instance, this gets set upon startup from
  // the user configuration and can be modified for this instance by either
  // the option or advanced menu items
  private boolean isAdvanced = false;

  //  Process dialog references
  private SetupDialog setupDialog = null;
  private PreProcessingDialog preProcDialogA = null;
  private PreProcessingDialog preProcDialogB = null;
  private CoarseAlignDialog coarseAlignDialogA = null;
  private CoarseAlignDialog coarseAlignDialogB = null;
  private FiducialModelDialog fiducialModelDialogA = null;
  private FiducialModelDialog fiducialModelDialogB = null;
  private AlignmentEstimationDialog fineAlignmentDialogA = null;
  private AlignmentEstimationDialog fineAlignmentDialogB = null;
  private TomogramPositioningDialog tomogramPositioningDialogA = null;
  private TomogramPositioningDialog tomogramPositioningDialogB = null;
  private TomogramGenerationDialog tomogramGenerationDialogA = null;
  private TomogramGenerationDialog tomogramGenerationDialogB = null;
  private TomogramCombinationDialog tomogramCombinationDialog = null;
  private PostProcessingDialog postProcessingDialog = null;
  private SettingsDialog settingsDialog = null;

  //  This object controls the reading and writing of David's com scripts
  private ComScriptManager comScriptMgr = new ComScriptManager(this);

  //  The ProcessManager manages the execution of com scripts
  private ProcessManager processMgr = new ProcessManager(this);
  private ProcessTrack processTrack = new ProcessTrack();

  // Control variable for process execution
  private String nextProcess = "";
  private String threadNameA = "none";
  private String threadNameB = "none";

  // imodManager manages the opening and closing closing of imod(s), message 
  // passing for loading model
  private ImodManager imodManager;

  private MainFrame mainFrame;
  private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

  /**
   * 
   */
  public ApplicationManager(String[] args) {
    //  Initialize the program settings
    String testParamFilename = initProgram(args);

    //  Initialize the static application manager reference for the
    //  context popup
    ContextPopup initContextPopup = new ContextPopup(this);

    //  Create a new main window and wait for an event from the user
    mainFrame = new MainFrame(this);
    mainFrame.setMRUFileLabels(userConfig.getMRUFileList());

    // Open the etomo data file if one was found on the command line
    if (!testParamFilename.equals("")) {
      File etomoDataFile = new File(testParamFilename);
      if (openTestParamFile(etomoDataFile)) {
        openProcessingPanel();
      }
      else {
        openSetupDialog();
      }
    }
    else {
      openSetupDialog();
    }
    mainFrame.pack();
    mainFrame.show();
  }

  /**
   * 
   */
  public static void main(String[] args) {
    new ApplicationManager(args);
  }

  /**
  * Open the setup dialog
  */
  public void openSetupDialog() {

    //  Open the dialog in the appropriate mode for the current state of
    //  processing
    if (setupDialog == null) {
      setupDialog = new SetupDialog(this);
      setupDialog.initializeFields(metaData);
    }
    mainFrame.openSetupPanel(setupDialog);
    Dimension frameSize = mainFrame.getSize();
    mainFrame.setLocation(
      (screenSize.width - frameSize.width) / 2,
      (screenSize.height - frameSize.height) / 2);
  }

  /**
   * Close message from the setup dialog window
   */
  public void doneSetupDialog() {
    if (setupDialog == null) {
      openMessageDialog(
        "Can not update metadata parameters without an active setup dialog",
        "Program logic error");
    }

    //  Get the selected exit button
    DialogExitState exitState = setupDialog.getExitState();

    if (exitState != DialogExitState.CANCEL) {

      // Set the current working directory for the application saving the
      // old user.dir property until the meta data is valid
      String oldUserDir = System.getProperty("user.dir");
      System.setProperty(
        "user.dir",
        setupDialog.getWorkingDirectory().getAbsolutePath());

      metaData = setupDialog.getFields();

      if (metaData.isValid()) {
        mainFrame.updateDataParameters(null, metaData);
        processTrack.setSetupState(ProcessState.INPROGRESS);

        isDataParamDirty = true;

        //  Initialize a new IMOD manager
        imodManager = new ImodManager(this, metaData);
      }
      else {
        String[] errorMessage = new String[2];
        errorMessage[0] = "Setup Parameter Error";
        errorMessage[1] = metaData.getInvalidReason();
        openMessageDialog(errorMessage, "Setup Parameter Error");
        System.setProperty("user.dir", oldUserDir);
        return;
      }

      // This is really the method to use the existing com scripts
      if (exitState == DialogExitState.POSTPONE) {
        metaData.setComScriptCreated(true);
        processTrack.setSetupState(ProcessState.COMPLETE);
      }
      else {
        try {
          processMgr.setupComScripts(metaData);
          processTrack.setSetupState(ProcessState.COMPLETE);
          metaData.setComScriptCreated(true);
        }
        catch (BadComScriptException except) {
          except.printStackTrace();
          openMessageDialog(except.getMessage(), "Can't run copytomocoms");
        }
        catch (IOException except) {
          openMessageDialog(
            "Can't run copytomocoms\n" + except.getMessage(),
            "Copytomocoms IOException");
        }
      }
    }

    //  Switch the main window to the procesing panel
    openProcessingPanel();

    //  Free the dialog
    setupDialog = null;

  }

  /**
   * Open the main window in processing mode
   */
  public void openProcessingPanel() {
    mainFrame.showProcessingPanel(metaData.getAxisType());
    mainFrame.updateAllProcessingStates(processTrack);
    mainFrame.pack();
    //  Resize to the users preferrred window dimensions
    mainFrame.setSize(
      new Dimension(
        userConfig.getMainWindowWidth(),
        userConfig.getMainWindowHeight()));

    mainFrame.doLayout();
    mainFrame.validate();
    if (isDualAxis()) {
      mainFrame.setDividerLocation(0.51);
    }
  }

  /**
   * Open the pre-processing dialog
   */
  public void openPreProcDialog(AxisID axisID) {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }

    // TODO: When a panel is overwriten by another should it be nulled and
    // closed or left and and reshown when needed?
    // Problem with stale data for align and tilt info since they are on
    // multiple panels
    // Check to see if the dialog panel is already open
    if (showIfExists(preProcDialogA, preProcDialogB, axisID)) {
      return;
    }

    PreProcessingDialog preProcDialog = new PreProcessingDialog(this, axisID);
    if (axisID == AxisID.SECOND) {
      preProcDialogB = preProcDialog;
    }
    else {
      preProcDialogA = preProcDialog;
    }

    // Load the required ccderaser{|a|b}.com files
    // Fill in the parameters and set it to the appropriate state
    comScriptMgr.loadEraser(axisID);
    preProcDialog.setCCDEraserParams(comScriptMgr.getCCDEraserParam(axisID));

    mainFrame.showProcess(preProcDialog.getContainer(), axisID);
  }

  /**
   * 
   */
  public void donePreProcDialog(AxisID axisID) {
    PreProcessingDialog preProcDialog;
    if (axisID == AxisID.SECOND) {
      preProcDialog = preProcDialogB;
    }
    else {
      preProcDialog = preProcDialogA;
    }

    if (preProcDialog == null) {
      openMessageDialog(
        "Can not update preprocessing parameters without an active "
          + "preprocessing dialog",
        "Program logic error");
      return;
    }

    //  Keep dialog box open until we get good info or it is cancelled
    DialogExitState exitState = preProcDialog.getExitState();

    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(axisID);
    }
    else {
      updateEraserCom(axisID);

      // If there are raw stack imod processes open ask the user if they
      // should be closed.
      try {
        if (imodManager.isRawStackOpen(AxisID.FIRST)
          || imodManager.isRawStackOpen(AxisID.SECOND)) {
          String[] message = new String[2];
          message[0] = "There are raw stack 3dmod processes open";
          message[1] = "Should they be closed?";
          boolean answer = openYesNoDialog(message);
          if (answer) {
            if (isDualAxis()) {
              imodManager.quitRawStack(AxisID.FIRST);
              imodManager.quitRawStack(AxisID.SECOND);
            }
            else {
              imodManager.quitRawStack(AxisID.ONLY);
            }
          }
        }
      }
      catch (AxisTypeException except) {
        except.printStackTrace();
        openMessageDialog(except.getMessage(), "AxisType problem");
      }
      catch (SystemProcessException except) {
        except.printStackTrace();
        openMessageDialog(except.getMessage(), "Problem closing raw stack");
      }

      if (exitState == DialogExitState.EXECUTE) {
        processTrack.setPreProcessingState(ProcessState.COMPLETE, axisID);
        mainFrame.setPreProcessingState(ProcessState.COMPLETE, axisID);
        //  Go to the coarse align dialog by default
        openCoarseAlignDialog(axisID);
      }
      else {
        processTrack.setPreProcessingState(ProcessState.INPROGRESS, axisID);
        mainFrame.setPreProcessingState(ProcessState.INPROGRESS, axisID);
        //  Go to the coarse align dialog by default
        mainFrame.showBlankProcess(axisID);
      }
    }

    //  Clean up the existing dialog
    if (axisID == AxisID.SECOND) {
      preProcDialogB = null;
    }
    else {
      preProcDialogA = null;
    }
    preProcDialog = null;
  }

  /**
   * Open 3dmod to create the erase model
   */
  public void imodErase(AxisID axisID) {
    String eraseModelName =
      metaData.getDatasetName() + axisID.getExtension() + ".erase";
    try {
      imodManager.modelRawStack(eraseModelName, axisID);
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "Can't open 3dmod on raw stack");
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Axis type problem in 3dmod erase");
    }
  }

  /**
   * Get the eraser{|a|b}.com script
   */
  private void updateEraserCom(AxisID axisID) {
    PreProcessingDialog preProcDialog;
    if (axisID == AxisID.SECOND) {
      preProcDialog = preProcDialogB;
    }
    else {
      preProcDialog = preProcDialogA;
    }

    //  Get the user input data from the dialog box.  The CCDEraserParam
    //  is first initialized from the currently loaded com script to
    //  provide deafault values for those not handled by the dialog box
    //  get function needs some error checking
    CCDEraserParam ccdEraserParam = new CCDEraserParam();
    ccdEraserParam = comScriptMgr.getCCDEraserParam(axisID);
    preProcDialog.getCCDEraserParams(ccdEraserParam);
    comScriptMgr.saveEraser(ccdEraserParam, axisID);

  }

  /**
   * Run the eraser script
   */
  public void eraser(AxisID axisID) {
    updateEraserCom(axisID);
    String threadName = processMgr.eraser(axisID);
    setThreadName(threadName, axisID);
    mainFrame.startProgressBar("Erasing pixels", axisID);
  }

  /**
   * Open the coarse alignment dialog
   */
  public void openCoarseAlignDialog(AxisID axisID) {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }
    if (showIfExists(coarseAlignDialogA, coarseAlignDialogB, axisID)) {
      return;
    }

    CoarseAlignDialog coarseAlignDialog = new CoarseAlignDialog(this, axisID);
    if (axisID == AxisID.SECOND) {
      coarseAlignDialogB = coarseAlignDialog;
    }
    else {
      coarseAlignDialogA = coarseAlignDialog;
    }

    //  Create the dialog box
    comScriptMgr.loadXcorr(axisID);
    coarseAlignDialog.setCrossCorrelationParams(
      comScriptMgr.getTiltxcorrParam(axisID));
    mainFrame.showProcess(coarseAlignDialog.getContainer(), axisID);
  }

  /**
   *  Get the parameters from the coarse align process dialog box
   */
  public void doneCoarseAlignDialog(AxisID axisID) {
    //  Set a reference to the correct object
    CoarseAlignDialog coarseAlignDialog;
    if (axisID == AxisID.SECOND) {
      coarseAlignDialog = coarseAlignDialogB;
    }
    else {
      coarseAlignDialog = coarseAlignDialogA;
    }

    if (coarseAlignDialog == null) {
      openMessageDialog(
        "Can not update coarse align without an active coarse align dialog",
        "Program logic error");
      return;
    }

    DialogExitState exitState = coarseAlignDialog.getExitState();

    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(axisID);
    }
    else {
      //  Get the user input data from the dialog box
      if (!updateXcorrCom(axisID)) {
        return;
      }
      if (exitState == DialogExitState.EXECUTE) {
        processTrack.setCoarseAlignmentState(ProcessState.COMPLETE, axisID);
        mainFrame.setCoarseAlignState(ProcessState.COMPLETE, axisID);
        //  Go to the fiducial model dialog by default
        openFiducialModelDialog(axisID);
      }
      else {
        processTrack.setCoarseAlignmentState(ProcessState.INPROGRESS, axisID);
        mainFrame.setCoarseAlignState(ProcessState.INPROGRESS, axisID);
        mainFrame.showBlankProcess(axisID);
      }
    }

    //  Clean up the existing dialog
    if (axisID == AxisID.SECOND) {
      coarseAlignDialogB = null;
    }
    else {
      coarseAlignDialogA = null;
    }
    coarseAlignDialog = null;
  }

  /**
   * Get the parameters from dialog box and run the cross correlation script
   */
  public void crossCorrelate(AxisID axisID) {

    // Get the parameters from the dialog box
    if (updateXcorrCom(axisID)) {
      String threadName = processMgr.crossCorrelate(axisID);
      setThreadName(threadName, axisID);
      mainFrame.startProgressBar("Cross-correlating raw stack", axisID);
    }
  }

  /**
   * Run the coarse alignment script
   */
  public void coarseAlign(AxisID axisID) {
    String threadName = processMgr.coarseAlign(axisID);
    setThreadName(threadName, axisID);
    mainFrame.startProgressBar("Creating coarse stack", axisID);
  }

  /**
   * Open 3dmod to view the coarsely aligned stack
   */
  public void imodAlign(AxisID axisID) {
    try {
      imodManager.openCoarseAligned(axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "Problem opening coarse stack");
    }
  }

  /**
   * Run midas on the raw stack
   */
  public void midasRawStack(AxisID axisID) {
    processMgr.midasRawStack(axisID);
  }

  /**
   * Get the required parameters from the dialog box and update the xcorr.com
   * script
   * @return true if successful in getting the parameters and saving the com
   * script
   */
  private boolean updateXcorrCom(AxisID axisID) {
    CoarseAlignDialog coarseAlignDialog;
    if (axisID == AxisID.SECOND) {
      coarseAlignDialog = coarseAlignDialogB;
    }
    else {
      coarseAlignDialog = coarseAlignDialogA;
    }

    try {
      TiltxcorrParam tiltXcorrParam = comScriptMgr.getTiltxcorrParam(axisID);
      coarseAlignDialog.getCrossCorrelationParams(tiltXcorrParam);
      comScriptMgr.saveXcorr(tiltXcorrParam, axisID);
    }
    catch (FortranInputSyntaxException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Xcorr Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      errorMessage[2] = "New value: " + except.getNewString();
      openMessageDialog(errorMessage, "Xcorr Parameter Syntax Error");
      return false;
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Xcorr Align Parameter Syntax Error";
      errorMessage[1] = axisID.getExtension();
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Xcorr Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * Open the fiducial model generation dialog
   */
  public void openFiducialModelDialog(AxisID axisID) {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }
    if (showIfExists(fiducialModelDialogA, fiducialModelDialogB, axisID)) {
      return;
    }

    // Create a new dialog panel and map it the generic reference
    FiducialModelDialog fiducialModelDialog =
      new FiducialModelDialog(this, axisID);
    if (axisID == AxisID.SECOND) {
      fiducialModelDialogB = fiducialModelDialog;
    }
    else {
      fiducialModelDialogA = fiducialModelDialog;
    }

    //  Load the required track{|a|b}.com files, fill in the dialog box params
    //  and set it to the appropriate state
    comScriptMgr.loadTrack(axisID);
    fiducialModelDialog.setBeadtrackParams(
      comScriptMgr.getBeadtrackParam(axisID));
    mainFrame.showProcess(fiducialModelDialog.getContainer(), axisID);
  }

  /**
   * 
   */
  public void doneFiducialModelDialog(AxisID axisID) {
    //  Set a reference to the correct object
    FiducialModelDialog fiducialModelDialog;
    if (axisID == AxisID.SECOND) {
      fiducialModelDialog = fiducialModelDialogB;
    }
    else {
      fiducialModelDialog = fiducialModelDialogA;
    }

    if (fiducialModelDialog == null) {
      openMessageDialog(
        "Can not update fiducial model without an active fiducial model dialog",
        "Program logic error");
      return;
    }

    DialogExitState exitState = fiducialModelDialog.getExitState();

    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(axisID);
    }
    else {
      //  Get the user input data from the dialog box
      if (!updateTrackCom(axisID)) {
        return;
      }
      if (exitState == DialogExitState.EXECUTE) {
        processTrack.setFiducialModelState(ProcessState.COMPLETE, axisID);
        mainFrame.setFiducialModelState(ProcessState.COMPLETE, axisID);
        openFineAlignmentDialog(axisID);
      }
      else {
        processTrack.setFiducialModelState(ProcessState.INPROGRESS, axisID);
        mainFrame.setFiducialModelState(ProcessState.INPROGRESS, axisID);
        mainFrame.showBlankProcess(axisID);
      }
    }

    //  Clean up the existing dialog
    if (axisID == AxisID.SECOND) {
      fiducialModelDialogB = null;
    }
    else {
      fiducialModelDialogA = null;
    }
    fiducialModelDialog = null;
  }

  /**
   * Open 3dmod with the seed model
   */
  public void imodSeedFiducials(AxisID axisID) {
    String seedModel =
      metaData.getDatasetName() + axisID.getExtension() + ".seed";
    try {
      imodManager.modelCoarseAligned(seedModel, axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on coarse aligned stack with model: " + seedModel);
    }
  }

  /**
   * Get the beadtrack parameters from the fiducial model dialog and run the
   * track com script
   */
  public void fiducialModelTrack(AxisID axisID) {
    if (updateTrackCom(axisID)) {
      String threadName = processMgr.fiducialModelTrack(axisID);
      setThreadName(threadName, axisID);
      mainFrame.startProgressBar("Tracking fiducials", axisID);
    }
  }

  /**
   * Open 3dmod with the new fidcuial model
   */
  public void imodFixFiducials(AxisID axisID) {
    String fiducialModel =
      metaData.getDatasetName() + axisID.getExtension() + ".fid";
    try {
      imodManager.modelCoarseAligned(fiducialModel, axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on coarse aligned stack with model: "
          + fiducialModel);
    }
  }

  /**
   * Update the specified track com script
   */
  private boolean updateTrackCom(AxisID axisID) {
    //  Set a reference to the correct object
    FiducialModelDialog fiducialModelDialog;
    if (axisID == AxisID.SECOND) {
      fiducialModelDialog = fiducialModelDialogB;
    }
    else {
      fiducialModelDialog = fiducialModelDialogA;
    }

    if (fiducialModelDialog == null) {
      openMessageDialog(
        "Can not update track?.com without an active fiducial model dialog",
        "Program logic error");
      return false;
    }

    try {
      BeadtrackParam beadtrackParam = comScriptMgr.getBeadtrackParam(axisID);
      fiducialModelDialog.getBeadtrackParams(beadtrackParam);
      comScriptMgr.saveTrack(beadtrackParam, axisID);

    }
    catch (FortranInputSyntaxException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Beadtrack Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      errorMessage[2] = "New value: " + except.getNewString();
      openMessageDialog(errorMessage, "Beadtrack Parameter Syntax Error");
      return false;
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Beadtrack Parameter Syntax Error";
      errorMessage[1] = axisID.getExtension();
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Beadtrack Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * Open the alignment estimation dialog
   */
  public void openFineAlignmentDialog(AxisID axisID) {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }
    if (showIfExists(fineAlignmentDialogA, fineAlignmentDialogB, axisID)) {
      return;
    }

    // Create a new dialog panel and map it the generic reference
    AlignmentEstimationDialog fineAlignmentDialog =
      new AlignmentEstimationDialog(this, axisID);
    if (axisID == AxisID.SECOND) {
      fineAlignmentDialogB = fineAlignmentDialog;
    }
    else {
      fineAlignmentDialogA = fineAlignmentDialog;
    }

    //  Load the required align{|a|b}.com files, fill in the dialog box params
    //  and set it to the appropriate state
    comScriptMgr.loadAlign(axisID);
    fineAlignmentDialog.setTiltalignParams(
      comScriptMgr.getTiltalignParam(axisID));

    //  Create a default transferfid object to populate the alignment dialog
    fineAlignmentDialog.setTransferFidParams(
      new TransferfidParam(getIMODDirectory()));
    mainFrame.showProcess(fineAlignmentDialog.getContainer(), axisID);
  }

  /**
   *
   */
  public void doneAlignmentEstimationDialog(AxisID axisID) {
    //  Set a reference to the correct object
    AlignmentEstimationDialog fineAlignmentDialog;
    if (axisID == AxisID.SECOND) {
      fineAlignmentDialog = fineAlignmentDialogB;
    }
    else {
      fineAlignmentDialog = fineAlignmentDialogA;
    }

    if (fineAlignmentDialog == null) {
      openMessageDialog(
        "Can not update align?.com without an active alignment dialog",
        "Program logic error");
      return;
    }

    DialogExitState exitState = fineAlignmentDialog.getExitState();

    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(axisID);
    }
    else {
      //  Get the user input data from the dialog box
      if (!updateAlignCom(axisID)) {
        return;
      }
      if (exitState == DialogExitState.POSTPONE) {
        processTrack.setFineAlignmentState(ProcessState.INPROGRESS, axisID);
        mainFrame.setFineAlignmentState(ProcessState.INPROGRESS, axisID);
        mainFrame.showBlankProcess(axisID);
      }
      else {
        processTrack.setFineAlignmentState(ProcessState.COMPLETE, axisID);
        mainFrame.setFineAlignmentState(ProcessState.COMPLETE, axisID);
        openTomogramPositioningDialog(axisID);

        // Check to see if the user wants to keep any coarse aligned imods
        // open
        try {
          if (imodManager.isCoarseAlignedOpen(AxisID.FIRST)
            || imodManager.isCoarseAlignedOpen(AxisID.SECOND)) {
            String[] message = new String[2];
            message[0] =
              "There are coarsely aligned stack 3dmod processes open";
            message[1] = "Should they be closed?";
            boolean answer = openYesNoDialog(message);
            if (answer) {
              if (isDualAxis()) {
                imodManager.quitCoarseAligned(AxisID.FIRST);
                imodManager.quitCoarseAligned(AxisID.SECOND);
              }
              else {
                imodManager.quitCoarseAligned(AxisID.ONLY);
              }
            }
          }
        }
        catch (AxisTypeException except) {
          except.printStackTrace();
          openMessageDialog(except.getMessage(), "AxisType problem");
        }
        catch (SystemProcessException except) {
          except.printStackTrace();
          openMessageDialog(
            except.getMessage(),
            "Problem closing coarse stack");
        }
      }
    }

    //  Clean up the existing dialog
    if (axisID == AxisID.SECOND) {
      fineAlignmentDialogB = null;
    }
    else {
      fineAlignmentDialogA = null;
    }
    fineAlignmentDialog = null;
  }

  /**
   * Execute the fine alignment script (align.com) for the appropriate axis
   * @param the AxisID identifying the axis to align.
   */
  public void fineAlignment(AxisID axisID) {
    if (!updateAlignCom(axisID)) {
      return;
    }
    String threadName = processMgr.fineAlignment(axisID);
    setThreadName(threadName, axisID);
    mainFrame.startProgressBar("Aligning stack", axisID);
  }

  /**
   * Open 3dmod with the new fidcuial model
   */
  public void imodViewResiduals(AxisID axisID) {
    String fiducialModel =
      metaData.getDatasetName() + axisID.getExtension() + ".resmod";
    try {
      imodManager.modelCoarseAligned(fiducialModel, axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on coarse aligned stack with model: "
          + fiducialModel);
    }
  }

  /**
   * Open 3dmodv with the new fidcuial model
   */
  public void imodView3DModel(AxisID axisID) {
    String fiducialModel =
      metaData.getDatasetName() + axisID.getExtension() + ".3dmod";
    imodManager.openFiducialModel(fiducialModel, axisID);
  }

  /**
   * Open 3dmod to view the coarsely aligned stack
   * @param axisID the AxisID to coarse align.
   */
  public void imodFineAlign(AxisID axisID) {
    try {
      imodManager.openFineAligned(axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on fine aligned stack");
    }

  }

  /**
   * Run transferfid
   */
  public void transferfid(AxisID sourceAxisID) {
    //  Set a reference to the correct object
    AlignmentEstimationDialog fineAlignmentDialog;
    if (sourceAxisID == AxisID.SECOND) {
      fineAlignmentDialog = fineAlignmentDialogB;
    }
    else {
      fineAlignmentDialog = fineAlignmentDialogA;
    }

    if (fineAlignmentDialog != null) {
      TransferfidParam transferfidParam =
        new TransferfidParam(getIMODDirectory());
      // Setup the default parameters depending upon the axis to transfer the
      // fiducials from
      String datasetName = metaData.getDatasetName();
      transferfidParam.setDatasetName(datasetName);
      if (sourceAxisID == AxisID.SECOND) {
        transferfidParam.setBToA(true);
      }

      // Get any user specified changes
      fineAlignmentDialog.getTransferFidParams(transferfidParam);
      String threadName = processMgr.transferFiducials(transferfidParam);
      setThreadName(threadName, sourceAxisID);
      mainFrame.startProgressBar("Transfering fiducuals", sourceAxisID);
    }
  }

  /**
   * updateAlignCom updates the align{|a|b}.com scripts with the parameters from
   * the alignment estimation dialog.  This also updates the local alignment
   * state of the appropriate tilt files.
   */
  private boolean updateAlignCom(AxisID axisID) {
    //  Set a reference to the correct object
    AlignmentEstimationDialog fineAlignmentDialog;
    if (axisID == AxisID.SECOND) {
      fineAlignmentDialog = fineAlignmentDialogB;
    }
    else {
      fineAlignmentDialog = fineAlignmentDialogA;
    }

    if (fineAlignmentDialog == null) {
      openMessageDialog(
        "Can not update align?.com without an active alignment dialog",
        "Program logic error");
      return false;
    }

    TiltalignParam tiltalignParam;

    try {
      tiltalignParam = comScriptMgr.getTiltalignParam(axisID);
      fineAlignmentDialog.getTiltalignParams(tiltalignParam);
      comScriptMgr.saveAlign(tiltalignParam, axisID);
      //  Update the tilt.com script with the dependent parameters
      updateTiltDependsOnAlign(tiltalignParam, axisID);

      mainFrame.setFineAlignmentState(ProcessState.INPROGRESS, axisID);
    }
    catch (FortranInputSyntaxException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Tiltalign Parameter Syntax Error";
      errorMessage[1] = except.getNewString();
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Tiltalign Parameter Syntax Error");
      return false;
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[2];
      errorMessage[0] = "Tiltalign Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      openMessageDialog(errorMessage, "Tiltalign Parameter Syntax Error");
      return false;
    }

    return true;
  }

  /**
   * Update the tilt parameters dependent on the align script
   * - local alignments file
   * - the exclude list
   */
  private void updateTiltDependsOnAlign(
    ConstTiltalignParam tiltalignParam,
    AxisID currentAxis) {

    comScriptMgr.loadTilt(currentAxis);
    TiltParam tiltParam = comScriptMgr.getTiltParam(currentAxis);

    String alignFileExtension = currentAxis.getExtension() + "local.xf";

    if (tiltalignParam.getLocalAlignments()) {
      tiltParam.setLocalAlignFile(getDatasetName() + alignFileExtension);
    }
    else {
      tiltParam.setLocalAlignFile("");
    }

    tiltParam.setExcludeList(tiltalignParam.getIncludeExcludeList());
    comScriptMgr.saveTilt(tiltParam, currentAxis);
  }

  /**
   * Open the tomogram positioning dialog
   */
  public void openTomogramPositioningDialog(AxisID axisID) {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }
    if (showIfExists(tomogramPositioningDialogA,
      tomogramPositioningDialogB,
      axisID)) {
      return;
    }

    // Create a new dialog panel and map it the generic reference
    TomogramPositioningDialog tomogramPositioningDialog =
      new TomogramPositioningDialog(this, axisID);
    if (axisID == AxisID.SECOND) {
      tomogramPositioningDialogB = tomogramPositioningDialog;
    }
    else {
      tomogramPositioningDialogA = tomogramPositioningDialog;
    }

    // Get the tilt{|a|b}.com and align{|a|b}.com parameters
    comScriptMgr.loadTilt(axisID);
    tomogramPositioningDialog.setTiltParams(comScriptMgr.getTiltParam(axisID));

    comScriptMgr.loadAlign(axisID);
    tomogramPositioningDialog.setAlignParams(
      comScriptMgr.getTiltalignParam(axisID));

    // Open the dialog panel
    mainFrame.showProcess(tomogramPositioningDialog.getContainer(), axisID);
  }

  /**
   * 
   */
  public void doneTomogramPositioningDialog(AxisID axisID) {
    //  Set a reference to the correct object
    TomogramPositioningDialog tomogramPositioningDialog;
    if (axisID == AxisID.SECOND) {
      tomogramPositioningDialog = tomogramPositioningDialogB;
    }
    else {
      tomogramPositioningDialog = tomogramPositioningDialogA;
    }

    if (tomogramPositioningDialog == null) {
      openMessageDialog(
        "Can not update sample.com without an active positioning dialog",
        "Program logic error");
      return;
    }

    DialogExitState exitState = tomogramPositioningDialog.getExitState();
    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(axisID);
    }
    else {
      boolean tiltFinished = updateSampleTiltCom(axisID);
      boolean alignFinished = updateAlignCom(tomogramPositioningDialog, axisID);
      if (!(tiltFinished & alignFinished)) {
        return;
      }
      if (exitState == DialogExitState.POSTPONE) {
        processTrack.setTomogramPositioningState(
          ProcessState.INPROGRESS,
          axisID);
        mainFrame.setTomogramPositioningState(ProcessState.INPROGRESS, axisID);
        mainFrame.showBlankProcess(axisID);
      }
      else {
        processTrack.setTomogramPositioningState(ProcessState.COMPLETE, axisID);
        mainFrame.setTomogramPositioningState(ProcessState.COMPLETE, axisID);
        openTomogramGenerationDialog(axisID);

        try {
          if (imodManager.isSampleOpen(AxisID.FIRST)
            || imodManager.isSampleOpen(AxisID.SECOND)) {
            String[] message = new String[2];
            message[0] = "There are sample reconstruction 3dmod processes open";
            message[1] = "Should they be closed?";
            boolean answer = openYesNoDialog(message);
            if (answer) {
              if (isDualAxis()) {
                imodManager.quitSample(AxisID.FIRST);
                imodManager.quitSample(AxisID.SECOND);
              }
              else {
                imodManager.quitSample(AxisID.FIRST);
              }
            }
          }
        }
        catch (AxisTypeException except) {
          except.printStackTrace();
          openMessageDialog(except.getMessage(), "AxisType problem");
        }
        catch (SystemProcessException except) {
          except.printStackTrace();
          openMessageDialog(
            except.getMessage(),
            "Problem closing sample reconstruction");
        }
      }
    }
    //  Clean up the existing dialog
    if (axisID == AxisID.SECOND) {
      tomogramPositioningDialogB = null;
    }
    else {
      tomogramPositioningDialogA = null;
    }
    tomogramPositioningDialog = null;
  }

  /**
   * Run the sample com script
   */
  public void createSample(AxisID axisID) {
    //  Get the user input data from the dialog box
    if (updateSampleTiltCom(axisID)) {
      String threadName = processMgr.createSample(axisID);
      setThreadName(threadName, axisID);
      mainFrame.startProgressBar("Creating sample tomogram", axisID);
    }
  }

  /**
   * 
   */
  public void imodSample(AxisID axisID) {
    try {
      imodManager.openSample(axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Problem opening sample reconstruction");
    }

  }

  /**
   * 
   */
  public void tomopitch(AxisID axisID) {
    String threadName = processMgr.tomopitch(axisID);
    setThreadName(threadName, axisID);
    mainFrame.startProgressBar("Finding sample position", axisID);
  }

  /**
   * 
   */
  public void finalAlign(AxisID axisID) {
    //  Set a reference to the correct object
    TomogramPositioningDialog tomogramPositioningDialog;
    if (axisID == AxisID.SECOND) {
      tomogramPositioningDialog = tomogramPositioningDialogB;
    }
    else {
      tomogramPositioningDialog = tomogramPositioningDialogA;
    }

    if (updateAlignCom(tomogramPositioningDialog, axisID)) {
      String threadName = processMgr.fineAlignment(axisID);
      setThreadName(threadName, axisID);
      mainFrame.startProgressBar("Calculating final alignment", axisID);
    }
  }

  /**
   * Update the tilt{|a|b}.com file with sample parameters for the specified
   * axis
   */
  private boolean updateSampleTiltCom(AxisID axisID) {
    //  Set a reference to the correct object
    TomogramPositioningDialog tomogramPositioningDialog;
    if (axisID == AxisID.SECOND) {
      tomogramPositioningDialog = tomogramPositioningDialogB;
    }
    else {
      tomogramPositioningDialog = tomogramPositioningDialogA;
    }

    // Make sure that we have an active positioning dialog
    if (tomogramPositioningDialog == null) {
      openMessageDialog(
        "Can not update sample.com without an active positioning dialog",
        "Program logic error");
      return false;
    }

    // Get the current tilt parameters, make any user changes and save the
    // parameters back to the tilt{|a|b}.com
    try {
      TiltParam tiltParam = comScriptMgr.getTiltParam(axisID);
      tomogramPositioningDialog.getTiltParams(tiltParam);
      comScriptMgr.saveTilt(tiltParam, axisID);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Tilt Parameter Syntax Error";
      errorMessage[1] = "Axis: " + axisID.getExtension();
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Tilt Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * updateAlignCom updates the align{|a|b}.com scripts with the parameters from
   * the tomogram positioning dialog.
   */
  private boolean updateAlignCom(
    TomogramPositioningDialog tomogramPositioningDialog,
    AxisID axisID) {

    TiltalignParam tiltalignParam;
    try {
      tiltalignParam = comScriptMgr.getTiltalignParam(axisID);
      tomogramPositioningDialog.getAlignParams(tiltalignParam);
      comScriptMgr.saveAlign(tiltalignParam, axisID);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Tiltalign Parameter Syntax Error";
      errorMessage[1] = "Axis: " + axisID.getExtension();
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Tiltalign Parameter Syntax Error");
      return false;
    }

    return true;
  }

  /**
   * Open the tomogram generation dialog
   */
  public void openTomogramGenerationDialog(AxisID axisID) {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }
    if (showIfExists(tomogramGenerationDialogA,
      tomogramGenerationDialogB,
      axisID)) {
      return;
    }

    // Create a new dialog panel and map it the generic reference
    TomogramGenerationDialog tomogramGenerationDialog =
      new TomogramGenerationDialog(this, axisID);
    if (axisID == AxisID.SECOND) {
      tomogramGenerationDialogB = tomogramGenerationDialog;
    }
    else {
      tomogramGenerationDialogA = tomogramGenerationDialog;
    }

    // Read in the tilt{|a|b}.com parameters and display the dialog panel
    comScriptMgr.loadTilt(axisID);
    tomogramGenerationDialog.setTiltParams(comScriptMgr.getTiltParam(axisID));

    mainFrame.showProcess(tomogramGenerationDialog.getContainer(), axisID);
  }

  /**
   * 
   */
  public void doneTomogramGenerationDialog(AxisID axisID) {
    //  Set a reference to the correct object
    TomogramGenerationDialog tomogramGenerationDialog;
    if (axisID == AxisID.SECOND) {
      tomogramGenerationDialog = tomogramGenerationDialogB;
    }
    else {
      tomogramGenerationDialog = tomogramGenerationDialogA;
    }

    if (tomogramGenerationDialog == null) {
      openMessageDialog(
        "Can not update tilt?.com without an active tomogram generation dialog",
        "Program logic error");
      return;
    }

    DialogExitState exitState = tomogramGenerationDialog.getExitState();

    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(axisID);
    }
    else {
      //  Get the user input data from the dialog box
      if (!updateTiltCom(axisID)) {
        return;
      }
      if (exitState == DialogExitState.POSTPONE) {
        processTrack.setTomogramGenerationState(
          ProcessState.INPROGRESS,
          axisID);
        mainFrame.setTomogramGenerationState(ProcessState.INPROGRESS, axisID);
        mainFrame.showBlankProcess(axisID);
      }
      else {
        processTrack.setTomogramGenerationState(ProcessState.COMPLETE, axisID);
        mainFrame.setTomogramGenerationState(ProcessState.COMPLETE, axisID);
        if (isDualAxis()) {
          openTomogramCombinationDialog();
          if (axisID == AxisID.SECOND) {
            mainFrame.showBlankProcess(axisID);
          }
        }
        else {
          openPostProcessingDialog();
        }
      }
    }

    //  Clean up the existing dialog
    if (axisID == AxisID.SECOND) {
      tomogramGenerationDialogB = null;
    }
    else {
      tomogramGenerationDialogA = null;
    }
    tomogramGenerationDialog = null;
  }

  /**
   *
   */
  private boolean updateTiltCom(AxisID axisID) {
    //  Set a reference to the correct object
    TomogramGenerationDialog tomogramGenerationDialog;
    if (axisID == AxisID.SECOND) {
      tomogramGenerationDialog = tomogramGenerationDialogB;
    }
    else {
      tomogramGenerationDialog = tomogramGenerationDialogA;
    }

    if (tomogramGenerationDialog == null) {
      openMessageDialog(
        "Can not update tilt?.com without an active tomogram generation dialog",
        "Program logic error");
      return false;
    }

    try {
      TiltParam tiltParam = comScriptMgr.getTiltParam(axisID);
      tomogramGenerationDialog.getTiltParams(tiltParam);
      comScriptMgr.saveTilt(tiltParam, axisID);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[3];
      errorMessage[0] = "Tilt Parameter Syntax Error";
      errorMessage[1] = "Axis: " + axisID.getExtension();
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Tilt Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   *
   */
  public void newst(AxisID axisID) {
    try {
      NewstParam newstParam;
      comScriptMgr.loadNewst(axisID);
      newstParam = comScriptMgr.getNewstComNewstParam(axisID);
      newstParam.setSize(",,");
      comScriptMgr.saveNewst(newstParam, axisID);

      String threadName = processMgr.newst(axisID);
      setThreadName(threadName, axisID);
      mainFrame.startProgressBar("Creating final stack", axisID);
    }
    catch (FortranInputSyntaxException except) {
      except.printStackTrace();
    }
  }

  /**
   * 
   */
  public void tilt(AxisID axisID) {
    if (updateTiltCom(axisID)) {
      String threadName = processMgr.tilt(axisID);
      setThreadName(threadName, axisID);
      mainFrame.startProgressBar("Calculating tomogram", axisID);
    }
  }

  /**
   * Open 3dmod to view the coarsely aligned stack
   * @param axisID the AxisID of the tomogram to open.
   */
  public void imodTomogram(AxisID axisID) {
    try {
      imodManager.openTomogram(axisID);
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod with the tomogram");
    }

  }

  /**
   * Open the tomogram combination dialog
   */
  public void openTomogramCombinationDialog() {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }

    // Verify that this process is applicable
    if (metaData.getAxisType() == AxisType.SINGLE_AXIS) {
      openMessageDialog(
        "This step is valid only for a dual axis tomogram",
        "Invalid tomogram combination selection");
      return;
    }

    if (tomogramCombinationDialog == null) {
      tomogramCombinationDialog = new TomogramCombinationDialog(this);
    }

    // Get the setupcombine parameters and set the default patch boundaries if
    // they have not already been set
    CombineParams combineParams =
      new CombineParams(metaData.getCombineParams());

    if (!combineParams.isPatchBoundarySet()) {
      String recFileName;
      if (combineParams.getMatchBtoA()) {
        recFileName = metaData.getDatasetName() + "a.rec";
      }
      else {
        recFileName = metaData.getDatasetName() + "b.rec";
      }
      try {
        combineParams.setDefaultPatchBoundaries(recFileName);
      }
      catch (InvalidParameterException except) {
        String[] detailedMessage = new String[4];
        detailedMessage[0] = "Unable to set default patch boundaries";
        detailedMessage[1] = "Are both tomograms computed and available?";
        detailedMessage[2] = "";
        detailedMessage[3] = except.getMessage();

        openMessageDialog(detailedMessage, "Invalid parameter: " + recFileName);
        //Close the dialog
        return;
      }
      catch (IOException except) {
        except.printStackTrace();
        openMessageDialog(except.getMessage(), "IO Error: " + recFileName);
        //Close the dialog
        return;
      }
    }

    // Fill in the dialog box params and set it to the appropriate state
    tomogramCombinationDialog.setCombineParams(combineParams);

    // If setupcombine has been run load the com scripts, otherwise disable the
    // apropriate panels in the tomogram combination dialog
    tomogramCombinationDialog.enableCombineTabs(
      metaData.getCombineParams().isScriptsCreated());

    if (metaData.getCombineParams().isScriptsCreated()) {
      if (metaData.getCombineParams().isModelBased()) {
        loadSolvematchMod();
      }
      else {
        loadSolvematchShift();
      }
      loadPatchcorr();
      loadMatchorwarp();
    }
    mainFrame.showProcess(
      tomogramCombinationDialog.getContainer(),
      AxisID.FIRST);
  }

  /**
   * Open the matching models in the 3dmod reconstruction instances  
   */
  public void imodMatchingModel() {
    try {
      imodManager.matchingModel(metaData.getDatasetName());
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on tomograms for matching models");
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
  }

  /**
   * Open the matchcheck results in 3dmod
   */
  public void imodMatchCheck() {
    try {
      imodManager.openMatchCheck();
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on matchcheck.mat or matchcheck.rec");
    }
  }

  /**
   * Open the patch region models in tomogram being matched to
   */
  public void imodPatchRegionModel() {
    try {
      if (metaData.getCombineParams().getMatchBtoA()) {
        imodManager.patchRegionModel(AxisID.FIRST);
      }
      else {
        imodManager.patchRegionModel(AxisID.SECOND);
      }
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on tomogram for patch region models");
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
  }

  /**
   * Open the patch vector models in 3dmod
   */
  public void imodPatchVectorModel() {
    try {
      imodManager.openPatchVectorModel();
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on tomogram for patch vector model");
    }
  }

  /**
   * Open the tomogram being matched to
   */
  public void imodMatchedToTomogram() {
    try {
      if (metaData.getCombineParams().getMatchBtoA()) {
        imodManager.openTomogram(AxisID.FIRST);
      }
      else {
        imodManager.openTomogram(AxisID.SECOND);
      }
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on tomogram being matched to");
    }
    catch (AxisTypeException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "AxisType problem");
    }
  }

  /**
   * Open the combined tomogram
   */
  public void imodCombinedTomogram() {
    try {
      imodManager.openCombinedTomogram();
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on combined tomogram");
    }
  }

  /**
   * Tomogram combination done method, move on to the post processing window
   */
  public void doneTomogramCombinationDialog() {
    if (tomogramCombinationDialog == null) {
      openMessageDialog(
        "Can not update combine.com without an active tomogram combination dialog",
        "Program logic error");
      return;
    }

    DialogExitState exitState = tomogramCombinationDialog.getExitState();
    if (exitState == DialogExitState.CANCEL) {
      mainFrame.showBlankProcess(AxisID.ONLY);
    }
    else {
      //  Get the user input data from the dialog box
      if (updateCombineCom()) {
        if (exitState == DialogExitState.POSTPONE) {
          processTrack.setTomogramCombinationState(ProcessState.INPROGRESS);
          mainFrame.setTomogramCombinationState(ProcessState.INPROGRESS);
          mainFrame.showBlankProcess(AxisID.ONLY);
        }
        else {
          processTrack.setTomogramCombinationState(ProcessState.COMPLETE);
          mainFrame.setTomogramCombinationState(ProcessState.COMPLETE);
          openPostProcessingDialog();
        }
      }
    }
  }

  /**
   * Run the setupcombine script with the current combine parameters stored in
   * metaData object.  updateCombineCom is called first to get the currect
   * parameters from the dialog.
   * @param tomogramCombinationDialog the calling dialog.
   */
  public void createCombineScripts() {
    if (!updateCombineCom()) {
      return;
    }

    try {
      processMgr.setupCombineScripts(metaData);
    }
    catch (BadComScriptException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "Can't run setupcombine");
      return;
    }

    catch (IOException except) {
      except.printStackTrace();
      openMessageDialog(
        "Can't run setupcombine\n" + except.getMessage(),
        "Setupcombine IOException");
      return;
    }

    // Reload the initial and final match paramaters from the newly created
    // scripts
    metaData.getCombineParams().setScriptsCreated(true);
    isDataParamDirty = true;

    loadSolvematchShift();
    loadPatchcorr();
    loadMatchorwarp();
    tomogramCombinationDialog.enableCombineTabs(true);

  }

  /**
   * Update the combine parameters from the calling dialog
   * @param tomogramCombinationDialog the calling dialog.
   * @return true if the combine parameters are valid false otherwise.  If the
   * combine parameters are invalid a message dialog describing the invalid
   * parameters is presented to the user.
   */
  private boolean updateCombineCom() {
    if (tomogramCombinationDialog == null) {
      openMessageDialog(
        "Can not update combine.com without an active tomogram combination dialog",
        "Program logic error");
      return false;
    }
    CombineParams combineParams = new CombineParams();
    try {
      tomogramCombinationDialog.getCombineParams(combineParams);
      if (!combineParams.isValid()) {
        openMessageDialog(
          combineParams.getInvalidReasons(),
          "Invlaid combine parameters");
        return false;
      }

    }
    catch (NumberFormatException except) {
      openMessageDialog(except.getMessage(), "Number format error");
      return false;
    }

    metaData.setCombineParams(combineParams);
    return true;
  }

  /**
   * Load the solvematchshift com script into the tomogram combination dialog
   */
  public void loadSolvematchShift() {
    comScriptMgr.loadSolvematchshift();
    tomogramCombinationDialog.setSolvematchshiftParams(
      comScriptMgr.getSolvematchshift());
  }

  /**
   * Update the solvematchshift.com script from the information in the tomogram
   * combination dialog box
   * @return boolean
   */
  public boolean updateSolvematchshiftCom() {
    //  Set a reference to the correct object
    if (tomogramCombinationDialog == null) {
      openMessageDialog(
        "Can not update solvematchshift.com without an active tomogram generation dialog",
        "Program logic error");
      return false;
    }

    try {
      comScriptMgr.loadSolvematchshift();
      SolvematchshiftParam solvematchshiftParam =
        comScriptMgr.getSolvematchshift();
      tomogramCombinationDialog.getSolvematchshiftParams(solvematchshiftParam);
      comScriptMgr.saveSolvematchshift(solvematchshiftParam);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[2];
      errorMessage[0] = "Solvematchshift Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      openMessageDialog(errorMessage, "Solvematchshift Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * Load the solvematchmod com script into the tomogram combination dialog
   */
  public void loadSolvematchMod() {
    comScriptMgr.loadSolvematchmod();
    tomogramCombinationDialog.setSolvematchmodParams(
      comScriptMgr.getSolvematchmod());
  }

  /**
   * Update the solvematchmod.com script from the information in the tomogram
   * combination dialog box
   * @return boolean
   */
  public boolean updateSolvematchmodCom() {
    //  Set a reference to the correct object
    if (tomogramCombinationDialog == null) {
      openMessageDialog(
        "Can not update solvematchmod.com without an active tomogram generation dialog",
        "Program logic error");
      return false;
    }

    try {
      comScriptMgr.loadSolvematchmod();
      SolvematchmodParam solvematchmodParam = comScriptMgr.getSolvematchmod();
      tomogramCombinationDialog.getSolvematchmodParams(solvematchmodParam);
      comScriptMgr.saveSolvematchmod(solvematchmodParam);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[2];
      errorMessage[0] = "Solvematchmod Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      openMessageDialog(errorMessage, "Solvematchmod Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * Load the patchcorr com script into the tomogram combination dialog
   */
  private void loadPatchcorr() {
    comScriptMgr.loadPatchcorr();
    tomogramCombinationDialog.setPatchcrawl3DParams(
      comScriptMgr.getPatchcrawl3D());
  }

  /**
   * Update the patchcorr.com script from the information in the tomogram
   * combination dialog box
   * @return boolean
   */
  private boolean updatePatchcorrCom() {
    //  Set a reference to the correct object
    if (tomogramCombinationDialog == null) {
      openMessageDialog(
        "Can not update patchcorr.com without an active tomogram generation dialog",
        "Program logic error");
      return false;
    }

    try {
      Patchcrawl3DParam patchcrawl3DParam = comScriptMgr.getPatchcrawl3D();
      tomogramCombinationDialog.getPatchcrawl3DParams(patchcrawl3DParam);
      comScriptMgr.savePatchcorr(patchcrawl3DParam);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[2];
      errorMessage[0] = "Patchcorr Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      openMessageDialog(errorMessage, "Patchcorr Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * Load the matchorwarp com script into the tomogram combination dialog
   */
  private void loadMatchorwarp() {
    comScriptMgr.loadMatchorwarp();
    tomogramCombinationDialog.setMatchorwarpParams(
      comScriptMgr.getMatchorwarParam());
  }

  /**
   * Update the matchorwarp.com script from the information in the tomogram
   * combination dialog box
   * @return boolean
   */
  private boolean updateMatchorwarpCom(boolean trialMode) {
    //  Set a reference to the correct object
    if (tomogramCombinationDialog == null) {
      openMessageDialog(
        "Can not update matchorwarp.com without an active tomogram generation dialog",
        "Program logic error");
      return false;
    }

    try {
      MatchorwarpParam matchorwarpParam = comScriptMgr.getMatchorwarParam();
      tomogramCombinationDialog.getMatchorwarpParams(matchorwarpParam);
      matchorwarpParam.setTrial(trialMode);
      comScriptMgr.saveMatchorwarp(matchorwarpParam);
    }
    catch (NumberFormatException except) {
      String[] errorMessage = new String[2];
      errorMessage[0] = "Matchorwarp Parameter Syntax Error";
      errorMessage[1] = except.getMessage();
      openMessageDialog(errorMessage, "Matchorwarp Parameter Syntax Error");
      return false;
    }
    return true;
  }

  /**
   * Initiate the combine process from the beginning
   */
  public void combine() {
    if (updateSolvematchshiftCom()
      && updatePatchcorrCom()
      && updateMatchorwarpCom(false)) {

      //  Set the next process to execute when this is finished   
      nextProcess = "matchvol1";
      String threadName = processMgr.solvematchshift();
      setThreadName(threadName, AxisID.FIRST);
      tomogramCombinationDialog.showPane("Initial Match");
      mainFrame.startProgressBar("Combine: solvematchshift", AxisID.FIRST);
    }
  }

  /**
   * Initiate the combine process using solvematchmod
   */
  public void modelCombine() {
    if (updateSolvematchmodCom()
      && updatePatchcorrCom()
      && updateMatchorwarpCom(false)) {

      //  Set the next process to execute when this is finished   
      nextProcess = "matchvol1";
      String threadName = processMgr.solvematchmod();
      setThreadName(threadName, AxisID.FIRST);
      tomogramCombinationDialog.showPane("Initial Match");
      mainFrame.startProgressBar("Combine: solvematchmod", AxisID.FIRST);
    }
  }

  /**
   * Execute the matchvol1 com script and put patchcorr in the execution queue 
   */
  private void matchvol1() {
    //  Set the next process to execute when this is finished   
    nextProcess = "patchcorr";
    String threadName = processMgr.matchvol1();
    setThreadName(threadName, AxisID.FIRST);
    tomogramCombinationDialog.showPane("Initial Match");
    mainFrame.startProgressBar("Combine: matchvol1", AxisID.FIRST);
  }

  /**
   * Initiate the combine process from patchcorr step
   */
  public void patchcorrCombine() {
    if (updatePatchcorrCom() && updateMatchorwarpCom(false)) {
      patchcorr();
    }
  }

  /**
   * Exececute the patchcorr com script and put matchorwarp in the execution
   * queue
   */
  private void patchcorr() {
    //  Set the next process to execute when this is finished   
    nextProcess = "matchorwarp";
    String threadName = processMgr.patchcorr();
    setThreadName(threadName, AxisID.FIRST);
    tomogramCombinationDialog.showPane("Final Match");
    mainFrame.startProgressBar("Combine: patchcorr", AxisID.FIRST);
  }

  /**
   * Initiate the combine process from matchorwarp step
   */
  public void matchorwarpCombine() {
    if (updateMatchorwarpCom(false)) {
      matchorwarp("volcombine");
    }
  }

  /**
   * Initiate the combine process from matchorwarp step
   */
  public void matchorwarpTrial() {
    if (updateMatchorwarpCom(true)) {
      matchorwarp("");
    }
  }

  /**
   * Exececute the matchorwarp com script and put the process identefied by next
   * in the execution queue
   */
  private void matchorwarp(String next) {
    //  Set the next process to execute when this is finished   
    nextProcess = next;

    String threadName = processMgr.matchorwarp();
    setThreadName(threadName, AxisID.FIRST);
    tomogramCombinationDialog.showPane("Final Match");
    mainFrame.startProgressBar("Combine: matchorwarp", AxisID.FIRST);
  }

  /**
   * Exececute the volcombine com script and clear the execution queue
   */
  public void volcombine() {
    //  Set the next process to execute when this is finished   
    nextProcess = "";

    String threadName = processMgr.volcombine();
    setThreadName(threadName, AxisID.FIRST);
    tomogramCombinationDialog.showPane("Final Match");
    mainFrame.startProgressBar("Combine: volcombine", AxisID.FIRST);
  }

  /**
   * Convert the patch.mod to patch.out
   *
   */
  public void modelToPatch() {
    try {
      processMgr.modelToPatch();
    }
    catch (SystemProcessException except) {
      String[] errorMessage = new String[2];
      errorMessage[0] = "Unable to convert patch_vector.mod to patch.out";
      errorMessage[1] = except.getMessage();
      openMessageDialog(errorMessage, "Patch vector model error");
      return;

    }

  }
  /**
   * Open the post processing dialog
   */
  public void openPostProcessingDialog() {
    //  Check to see if the com files are present otherwise pop up a dialog
    //  box informing the user to run the setup process
    if (!metaData.getComScriptCreated()) {
      setupRequestDialog();
      return;
    }

    //  Open the dialog in the appropriate mode for the current state of
    //  processing
    if (postProcessingDialog == null) {
      postProcessingDialog = new PostProcessingDialog(this);
    }

    // Rename the full reconstruction volume so that it does not get written
    // over
    if ((metaData.getAxisType() == AxisType.SINGLE_AXIS)
      && (!fullReconRename())) {
      String[] detailedMessage = new String[2];
      detailedMessage[0] = "Unable to rename the output from tilt to full.rec";
      detailedMessage[1] = "Does the reconstruction file exist yet?";
      openMessageDialog(detailedMessage, "File rename error");
      //Close the dialog
      return;
    }

    TrimvolParam trimvolParam = new TrimvolParam(IMODDirectory);
    String inputFile = "";
    if (metaData.getAxisType() == AxisType.SINGLE_AXIS) {
      inputFile = "full.rec";
    }
    else {
      inputFile = "sum.rec";
    }
    try {
      trimvolParam.setDefaultRange(inputFile);
    }
    catch (InvalidParameterException except) {
      String[] detailedMessage = new String[4];
      detailedMessage[0] = "Unable to set trimvol range";
      detailedMessage[1] = "Does the reconstruction file exist yet?";
      detailedMessage[2] = "";
      detailedMessage[3] = except.getMessage();

      openMessageDialog(detailedMessage, "Invalid parameter: " + inputFile);
      //Close the dialog
      return;
    }
    catch (IOException except) {
      except.printStackTrace();
      openMessageDialog(except.getMessage(), "IO Error: " + inputFile);
      //Close the dialog
      return;
    }

    postProcessingDialog.setTrimvolParams(trimvolParam);

    mainFrame.showProcess(postProcessingDialog.getContainer(), AxisID.ONLY);
  }

  /**
   * Rename the reconstruction output from tilt for the case of a single axis
   * tomogram
   */
  private boolean fullReconRename() {
    String workingDirectory = System.getProperty("user.dir");
    File fullReconstruction = new File(workingDirectory, "full.rec");
    File finalReconstuction =
      new File(workingDirectory, metaData.getDatasetName() + ".rec");

    //  If neither of the files exist return false
    if ((!fullReconstruction.exists()) && (!finalReconstuction.exists())) {
      return false;
    }

    // If dataset.rec exists and full.rec doesn't move dataset.rec to full.rec
    // otherwise just return true
    if (finalReconstuction.exists() && (!fullReconstruction.exists())) {
      return finalReconstuction.renameTo(fullReconstruction);
    }
    return true;
  }

  /**
   * Close the post processing dialog panel
   */
  public void donePostProcessing() {
    if (postProcessingDialog == null) {
      openMessageDialog(
        "Post processing dialog not open",
        "Program logic error");
      return;
    }

    DialogExitState exitState = postProcessingDialog.getExitState();
    if (exitState == DialogExitState.CANCEL) {
      postProcessingDialog = null;
    }
    else if (exitState == DialogExitState.POSTPONE) {
      processTrack.setPostProcessingState(ProcessState.INPROGRESS);
      mainFrame.setPostProcessingState(ProcessState.INPROGRESS);
    }
    else {
      processTrack.setPostProcessingState(ProcessState.COMPLETE);
      mainFrame.setPostProcessingState(ProcessState.COMPLETE);
      postProcessingDialog = null;
    }
    mainFrame.showBlankProcess(AxisID.ONLY);
  }

  /**
   * open the full volume in 3dmod
   */
  public void imodFullVolume() {
    try {
      imodManager.openFullVolume();
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on the full tomogram");
    }
  }

  /**
   * Open the trimmed volume in 3dmod
   */
  public void imodTrimmedVolume() {
    try {
      imodManager.openTrimmedVolume();
    }
    catch (SystemProcessException except) {
      except.printStackTrace();
      openMessageDialog(
        except.getMessage(),
        "Can't open 3dmod on the trimmed tomogram");
    }
  }

  /**
   *  Execute trimvol
   */
  public void trimVolume() {
    // Make sure that the post processing panel is open
    if (postProcessingDialog == null) {
      openMessageDialog(
        "Post processing dialog not open",
        "Program logic error");
      return;
    }
    // Get the trimvol parameters from the panel
    TrimvolParam trimvolParam = new TrimvolParam(IMODDirectory);
    postProcessingDialog.getTrimvolParams(trimvolParam);

    //  Set the appropriate input and output files
    String inputFile = "";
    if (metaData.getAxisType() == AxisType.SINGLE_AXIS) {
      inputFile = "full.rec";
    }
    else {
      inputFile = "sum.rec";
    }
    trimvolParam.setInputFile(inputFile);
    trimvolParam.setOutputFile(metaData.getDatasetName() + ".rec");

    // Start the trimvol process
    String threadName = processMgr.trimVolume(trimvolParam);
    setThreadName(threadName, AxisID.ONLY);
    mainFrame.startProgressBar("Trimming volume", AxisID.ONLY);
  }

  //
  //  Utility functions
  //

  private boolean showIfExists(
    ProcessDialog panelA,
    ProcessDialog panelB,
    AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      if (panelB == null) {
        return false;
      }
      else {
        mainFrame.showProcess(panelB.getContainer(), axisID);
        return true;
      }
    }
    else {
      if (panelA == null) {
        return false;
      }
      else {
        mainFrame.showProcess(panelA.getContainer(), axisID);
        return true;
      }
    }
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
   * Get the current advanced state
   */
  public boolean getAdvanced() {
    return isAdvanced;
  }

  /**
   * 
   */
  public void setAdvanced(boolean state) {
    isAdvanced = state;
  }

  /**
   * 
   */
  public void packMainWindow() {
    mainFrame.repaint();
  }

  /**
   * Return the dataset name.  This is the basename of the raw image stack and
   * the name used for the base of all intermediate and final data files.
   * @return a string containing the dataset name.
   */
  public String getDatasetName() {
    return metaData.getDatasetName();
  }

  /**
   * Open a new dataset starting wiht the setup dialog
   *
   */
  public void openNewDataset() {
    // Check to see if the current dataset needs to be saved
    if (isDataParamDirty || processTrack.isModified()) {
      int returnValue =
        JOptionPane.showConfirmDialog(
          mainFrame,
          "Save the current data file ?",
          "Save data file?",
          JOptionPane.YES_NO_CANCEL_OPTION);

      if (returnValue == JOptionPane.CANCEL_OPTION) {
        return;
      }

      if (returnValue == JOptionPane.YES_OPTION) {
        if (paramFile == null) {
          if (!mainFrame.getTestParamFilename()) {
            return;
          }
        }
        saveTestParamFile();
      }
    }
    if (isDataParamDirty) {
      return;
    }

    // Delete the objects associated with the current dataset
    metaData = new MetaData();
    paramFile = null;

    setupDialog = null;
    preProcDialogA = null;
    preProcDialogB = null;
    coarseAlignDialogA = null;
    coarseAlignDialogB = null;
    fiducialModelDialogA = null;
    fiducialModelDialogB = null;
    fineAlignmentDialogA = null;
    fineAlignmentDialogB = null;
    tomogramPositioningDialogA = null;
    tomogramPositioningDialogB = null;
    tomogramGenerationDialogA = null;
    tomogramGenerationDialogB = null;
    tomogramCombinationDialog = null;
    postProcessingDialog = null;
    settingsDialog = null;

    comScriptMgr = new ComScriptManager(this);
    processMgr = new ProcessManager(this);
    processTrack = new ProcessTrack();
    //  This will be created in the doneSetupDialog method
    imodManager = null;

    // Open the setup dialog
    openSetupDialog();

  }
  /**
   * A message asking the ApplicationManager to load in the information from the
   * test parameter file.
   * @param paramFile the File object specifiying the data parameter file.
   */
  public boolean openTestParamFile(File paramFile) {
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
      mainFrame.setMRUFileLabels(userConfig.getMRUFileList());

      //  Initialize a new IMOD manager
      imodManager = new ImodManager(this, metaData);
    }
    catch (FileNotFoundException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file read error";
      errorMessage[1] = "Could not find the test parameter data file:";
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "File not found error");
      return false;
    }
    catch (IOException except) {
      except.printStackTrace();
      String[] errorMessage = new String[3];
      errorMessage[0] = "Test parameter file read error";
      errorMessage[1] = "Could not read the test parameter data from file:";
      errorMessage[2] = except.getMessage();
      openMessageDialog(errorMessage, "Test parameter file read error");
      return false;
    }
    return true;
  }

  /**
   * A message asking the ApplicationManager to save the test parameter
   * information to a file.
   */
  public void saveTestParamFile() {
    try {
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
      openMessageDialog(errorMessage, "Test parameter file save error");
    }
    isDataParamDirty = false;
  }

  /**
   * Return the test parameter file as a File object
   * @return a File object specifying the data set parameter file.
   */
  public File getTestParamFile() {
    return paramFile;
  }

  /**
   * Set the data set parameter file.  This also updates the mainframe data
   * parameters.
   * @param paramFile a File object specifying the data set parameter file.
   */
  public void setTestParamFile(File paramFile) {
    this.paramFile = paramFile;

    //  Update main window information and status bar
    mainFrame.updateDataParameters(paramFile, metaData);
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
   * 
   */
  private void setupRequestDialog() {
    String[] message = new String[2];
    message[0] = "The setup process has not been completed";
    message[1] =
      "Complete the Setup process before opening other process dialogs";
    openMessageDialog(message, "Program Operation Error");
    return;
  }

  /**
   * 
   */
  private String initProgram(String[] args) {
    //  Parse the command line
    String testParamFilename = parseCommandLine(args);

    // Get the HOME directory environment variable to find the program
    // configuration file
    homeDirectory = System.getProperty("user.home");
    if (homeDirectory == "") {
      String[] message = new String[2];
      message[0] =
        "Can not find home directory! Unable to load user preferences";
      message[1] =
        "Set HOME environment variable and restart program to fix this problem";
      openMessageDialog(message, "Program Initialization Error");
      System.exit(1);
    }
    if (debug) {
      System.err.println("Home directory: " + homeDirectory);
      System.err.println(
        "Working directory: " + System.getProperty("user.dir"));
    }

    // Get the IMOD directory so we know where to find documentation
    // Check to see if is defined on the command line first with -D
    // Otherwise check to see if we can get it from the environment
    String imodDirectoryName = System.getProperty("IMOD_DIR");
    if (imodDirectoryName == null) {
      imodDirectoryName = getEnvironmentVariable("IMOD_DIR");
      if (imodDirectoryName == "") {
        String[] message = new String[3];
        message[0] = "Can not find IMOD directory!";
        message[1] =
          "Set IMOD_DIR environment variable and restart program to fix this problem";
        openMessageDialog(message, "Program Initialization Error");
        System.exit(-1);
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

    //  Create a File object specifying the user configuration file
    File userConfigFile = new File(homeDirectory, ".etomo");

    //  Make sure the config file exists, create it if it doesn't
    try {
      userConfigFile.createNewFile();
    }
    catch (IOException except) {
      System.err.println(
        "Could not create file:" + userConfigFile.getAbsolutePath());
      System.err.println(except.getMessage());
      return "";
    }

    // Load in the user configuration
    ParameterStore userParams = new ParameterStore(userConfigFile);
    Storable storable[] = new Storable[1];
    storable[0] = userConfig;
    try {
      userParams.load(storable);
    }
    catch (IOException except) {
      openMessageDialog(
        except.getMessage(),
        "IO Exception: Can't load user configuration"
          + userConfigFile.getAbsolutePath());
    }

    //  Set the user preferences
    setUserPreferences();

    return testParamFilename;
  }

  /**
   * Set the user preferences
   */
  private void setUserPreferences() {
    ToolTipManager.sharedInstance().setInitialDelay(
      userConfig.getToolTipsInitialDelay());
    ToolTipManager.sharedInstance().setDismissDelay(
      userConfig.getToolTipsDismissDelay());
    setUIFont(userConfig.getFontFamily(), userConfig.getFontSize());
    setLookAndFeel(userConfig.getNativeLookAndFeel());
    isAdvanced = userConfig.getAdvancedDialogs();
  }

  /**
   * Parse the command line.  This method will return a non-empty string if
   * there is a etomo data .
   * @param The command line arguments
   * @return A string that will be set to the etomo data filename if one is
   * found on the command line otherwise it is "".
   */
  private String parseCommandLine(String[] args) {
    String testParamFilename = "";

    //  Parse the command line arguments
    for (int i = 0; i < args.length; i++) {

      // Filename argument should be the only one not beginning with at least
      // one dash
      if (!args[i].startsWith("-")) {
        testParamFilename = args[i];
      }

      if (args[i].equals("--debug")) {
        debug = true;
      }

      if (args[i].equals("--demo")) {
        demo = true;
      }
    }
    return testParamFilename;
  }

  /**
   * Exit the program
   */
  public boolean exitProgram() {
    //  Check to see if any processes are still running
    if (!threadNameA.equals("none") || !threadNameB.equals("none")) {
      String[] message = new String[3];
      message[0] = "There are still processes running.";
      message[1] = "Exiting Etomo now may terminate those processes.";
      message[2] = "Do you still wish to exit the program?";
      int returnValue =
        JOptionPane.showConfirmDialog(
          mainFrame,
          message,
          "Processes are still running",
          JOptionPane.YES_NO_OPTION);

      if (returnValue == JOptionPane.NO_OPTION) {
        return false;
      }
    }

    //  Check to see the meta data / process info is dirty
    if (isDataParamDirty || processTrack.isModified()) {
      int returnValue =
        JOptionPane.showConfirmDialog(
          mainFrame,
          "Save the current data file ?",
          "Save data file?",
          JOptionPane.YES_NO_CANCEL_OPTION);

      if (returnValue == JOptionPane.CANCEL_OPTION) {
        return false;
      }
      if (returnValue == JOptionPane.YES_OPTION) {
        if (paramFile == null) {
          if (!mainFrame.getTestParamFilename()) {
            return false;
          }
        }
        saveTestParamFile();
      }
    }

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
      System.err.println(
        "IOException: Could not create file:"
          + userConfigFile.getAbsolutePath()
          + "\n"
          + except.getMessage());
      System.err.println(except.getMessage());
      return true;
    }

    ParameterStore userParams = new ParameterStore(userConfigFile);
    Storable storable[] = new Storable[1];
    storable[0] = userConfig;
    if (!userConfigFile.canWrite()) {
      openMessageDialog(
        "Change permissions of $HOME/.etomo to allow writing",
        "Unable to save user configuration file");
    }

    if (userConfigFile.canWrite()) {
      try {
        userParams.save(storable);
      }
      catch (IOException excep) {
        excep.printStackTrace();
        openMessageDialog(
          "IOException: unable to save user parameters\n" + excep.getMessage(),
          "Unable to save user parameters");
      }
    }
    return true;
  }

  /**
   * Sets the look and feel for the program.
   * @param nativeLookAndFeel set to true to use the host os look and feel,
   * false will use the Metal look and feel.
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
        lookAndFeelClassName =
          "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
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
      System.err.println(
        "Could not set " + lookAndFeelClassName + " look and feel");
    }
  }

  /**
   * 
   */
  public static void setUIFont(String fontFamily, int fontSize) {

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
        FontUIResource newFont =
          new FontUIResource(fontFamily, currentFont.getStyle(), fontSize);
        UIManager.put(key, newFont);
      }
    }
  }

  /**
   * Return the IMOD directory
   */
  public File getIMODDirectory() {
    return new File(IMODDirectory.getAbsolutePath());
  }

  /**
   * Return the users home directory environment variable HOME or an empty
   * string if it doesn't exist.
   */
  private String getHomeDirectory() {
    return homeDirectory;
  }

  /**
   * Return an environment variable value
   * @param varName
   * @return String
   */
  private String getEnvironmentVariable(String varName) {
    //  There is not a real good way to access the system environment variables
    //  since the primary method was deprecated
    SystemProgram readEnvVar;
    String osName = System.getProperty("os.name");

    if (osName.startsWith("Windows")) {
      readEnvVar = new SystemProgram("cmd.exe /C echo %" + varName + "%");
      try {
        readEnvVar.setDebug(debug);
        readEnvVar.run();
      }
      catch (Exception excep) {
        excep.printStackTrace();
        System.err.println(excep.getMessage());
        System.err.println(
          "Unable to run cmd command to find "
            + varName
            + " environment variable");

        return "";
      }
      String[] stderr = readEnvVar.getStdError();
      if (stderr.length > 0) {
        System.err.println("Error running 'cmd.exe' command");
        for (int i = 0; i < stderr.length; i++) {
          System.err.println(stderr[i]);
        }
      }

      // Return the first line from the command  
      String[] stdout = readEnvVar.getStdOutput();
      if (stdout.length > 0) {
        return stdout[0];
      }
    }

    //  Non windows environment
    else {

      readEnvVar = new SystemProgram("env");
      try {
        readEnvVar.setDebug(debug);
        readEnvVar.run();
      }
      catch (Exception excep) {
        excep.printStackTrace();
        System.err.println(excep.getMessage());
        System.err.println(
          "Unable to run env command to find "
            + varName
            + " environment variable");

        return "";
      }
      String[] stderr = readEnvVar.getStdError();
      if (stderr.length > 0) {
        System.err.println("Error running 'env' command");
        for (int i = 0; i < stderr.length; i++) {
          System.err.println(stderr[i]);
        }
      }

      // Search through the evironment string array to find the request
      // environment variable
      String searchString = varName + "=";
      int nChar = searchString.length();
      String[] stdout = readEnvVar.getStdOutput();
      for (int i = 0; i < stdout.length; i++) {
        if (stdout[i].indexOf(searchString) == 0) {
          return stdout[i].substring(nChar);
        }
      }
    }
    return "";
  }

  /**
   * Open a Yes or No question dialog
   * @param message
   * @return boolean
   */
  private boolean openYesNoDialog(String[] message) {
    try {
      int answer =
        JOptionPane.showConfirmDialog(
          mainFrame,
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
   * Open a message dialog
   * @param message
   * @param title
   */
  public void openMessageDialog(Object message, String title) {
    JOptionPane.showMessageDialog(
      mainFrame,
      message,
      title,
      JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Returns the debug state.
   * @return boolean
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Returns the demo state.
   * @return boolean
   */
  public boolean isDemo() {
    return demo;
  }

  /**
   * Run the specified command as a background process with a indeterminate
   * progress bar.
   */
  public void backgroundProcess(String commandLine) {
    processMgr.test(commandLine);
  }

  /**
   * Start the next process specified by the nextProcess string
   *
   */
  private void startNextProcess() {
    if (nextProcess.equals("matchvol1")) {
      matchvol1();
      return;
    }

    if (nextProcess.equals("patchcorr")) {
      patchcorr();
      return;
    }

    if (nextProcess.equals("matchorwarp")) {
      matchorwarp("volcombine");
      return;
    }

    if (nextProcess.equals("volcombine")) {
      volcombine();
      return;
    }
  }

  /**
   * Notification message of the percent complete of a process
   */
  public void processProgress(String threadName, double percent) {

  }

  /**
   * Notification message that a background process is done.
   * @param threadName The name of the thread that has finished
   */
  public void processDone(String threadName, int exitValue) {
    if (threadName.equals(threadNameA)) {
      mainFrame.stopProgressBar(AxisID.FIRST);
      threadNameA = "none";
      if (exitValue == 0 && !nextProcess.equals("")) {
        startNextProcess();
      }
    }
    else if (threadName.equals(threadNameB)) {
      mainFrame.stopProgressBar(AxisID.SECOND);
      threadNameB = "none";
    }
    else {
      openMessageDialog(
        "Unknown thread finished!!!",
        "Thread name: " + threadName);
    }
  }

  /**
   * Interrupt the currently running thread for this axis
   * @param axisID
   */
  public void interrupt(AxisID axisID) {
    processMgr.interrupt(axisID);
  }

  /**
   * Map the thread name to the correct axis
   * @param name The name of the thread to assign to the axis
   * @param axisID The axis of the thread to be mapped 
   */
  private void setThreadName(String name, AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      threadNameB = name;
    }
    else {
      threadNameA = name;
    }
  }
}
