package etomo.process;

import etomo.logic.DatasetTool;
import etomo.storage.AutofidseedLog;
import etomo.storage.FlattenWarpLog;
import etomo.storage.LogFile;
import etomo.storage.TrackLog;
import etomo.storage.TransferFidLog;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.FileType;
import etomo.type.ProcessName;
import etomo.type.ProcessResultDisplay;
import etomo.type.ProcessingMethod;
import etomo.type.TomogramState;
import etomo.type.ViewType;
import etomo.ApplicationManager;
import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.ProcessSeries;
import etomo.ui.swing.TextPageWindow;
import etomo.ui.swing.UIHarness;
import etomo.util.DatasetFiles;
import etomo.util.InvalidParameterException;
import etomo.util.MRCHeader;
import etomo.util.Utilities;
import etomo.comscript.ArchiveorigParam;
import etomo.comscript.AutofidseedParam;
import etomo.comscript.BatchruntomoParam;
import etomo.comscript.BeadtrackParam;
import etomo.comscript.BlendmontParam;
import etomo.comscript.CCDEraserParam;
import etomo.comscript.ClipParam;
import etomo.comscript.CombineComscriptState;
import etomo.comscript.Command;
import etomo.comscript.CommandDetails;
import etomo.comscript.ConstCtfPhaseFlipParam;
import etomo.comscript.ConstFindBeads3dParam;
import etomo.comscript.ConstMTFFilterParam;
import etomo.comscript.ConstSplitCorrectionParam;
import etomo.comscript.ConstTiltParam;
import etomo.comscript.CtfPhaseFlipParam;
import etomo.comscript.ExtractpiecesParam;
import etomo.comscript.FlattenWarpParam;
import etomo.comscript.MakecomfileParam;
import etomo.comscript.ProcessDetails;
import etomo.comscript.ConstNewstParam;
import etomo.comscript.ConstSqueezevolParam;
import etomo.comscript.ConstTiltalignParam;
import etomo.comscript.ConstTiltxcorrParam;
import etomo.comscript.CopyTomoComs;
import etomo.comscript.ExtractmagradParam;
import etomo.comscript.ExtracttiltsParam;
import etomo.comscript.NewstParam;
import etomo.comscript.RestrictalignParam;
import etomo.comscript.RunraptorParam;
import etomo.comscript.SetupCombine;
import etomo.comscript.SirtsetupParam;
import etomo.comscript.SplitcombineParam;
import etomo.comscript.SplittiltParam;
import etomo.comscript.SqueezevolParam;
import etomo.comscript.TiltParam;
import etomo.comscript.TiltalignParam;
import etomo.comscript.TransferfidParam;
import etomo.comscript.TrimvolParam;
import etomo.comscript.WarpVolParam;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * Description: This object manages the execution of com scripts in the
 * background and the opening and sending messages to imod.  It also provides
 * an interface to executing some simple command sequences.
 * </p>
 * 
 * <p>Copyright: Copyright 2002 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version "$Id$
 * 
 * <p>
 * $Log$
 * Revision 3.162  2011/05/11 21:11:26  sueh
 * bug# 1482 In postProcess(String,AxisID) added the axisID to the comparisons.
 *
 * Revision 3.161  2011/05/10 16:50:45  sueh
 * big# 1482 In postProcess, handling getSubcommandProcessName returning a string.
 *
 * Revision 3.160  2011/05/03 02:44:41  sueh
 * bug# 1416 In postProcess(ComScriptProcess) added post-processing for sirtsetup.
 *
 * Revision 3.159  2011/04/04 16:53:09  sueh
 * bug# 1416 Added/modified reconnectTilt, sirtsetup, tilt.
 *
 * Revision 3.158  2011/02/26 04:21:30  sueh
 * bug# 1453 In setupComScripts functions return ProcessMessages.
 *
 * Revision 3.157  2011/02/24 23:36:06  sueh
 * bug# 1452 imageRotation needs to be double everywhere.
 *
 * Revision 3.156  2011/02/21 21:04:21  sueh
 * bug# 1437 Reformatting.
 *
 * Revision 3.155  2011/02/03 06:04:20  sueh
 * bug# 1422 Sending the processing method to parallel processing
 * functions so it can be used for reconnecting.
 *
 * Revision 3.154  2010/12/05 04:43:17  sueh
 * bug# 1416 Changed FinalAlignedStackExpert.setEnabledTiltPArameters to
 * setTiltState.
 *
 * Revision 3.153  2010/11/13 16:03:45  sueh
 * bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 *
 * Revision 3.152  2010/05/10 21:36:45  sueh
 * bug# 1372 In modelToPatch removing the imod2vatch command.
 *
 * Revision 3.151  2010/04/29 02:03:17  sueh
 * bug# 1359 Added AxisID to clipStats.
 *
 * Revision 3.150  2010/04/28 16:22:39  sueh
 * bug# 1344 Passing params whose processes output image files to the
 * start process functions.
 *
 * Revision 3.149  2010/03/19 21:58:08  sueh
 * bug# 1335 FlattenWarpLog no longer an n'ton.
 *
 * Revision 3.148  2010/03/12 04:07:54  sueh
 * bug# 1325 Turned on use button warnings in TomogramState.
 *
 * Revision 3.147  2010/03/03 04:55:35  sueh
 * bug# 1311 Removed unnecessary ProcessName references.
 *
 * Revision 3.146  2010/02/18 22:35:32  sueh
 * bug# 1315 When showing the log file, passing the File.
 *
 * Revision 3.145  2010/02/17 04:49:20  sueh
 * bug# 1301 Using the manager instead of the manager key do pop up
 * messages.
 *
 * Revision 3.144  2009/12/18 17:33:38  sueh
 * bug# 1291 In postProcess(BackgroundProcess) fixing a null pointer
 * exception.
 *
 * Revision 3.143  2009/12/11 20:51:33  sueh
 * bug# 1291 Allow showLogFile to display just the file name as the title.
 *
 * Revision 3.142  2009/12/11 17:27:47  sueh
 * bug# 1291 Added clipStats.  Popping up clipStats results in post
 * processing.  Generalized showTransferfidLogFile; changed it to
 * showLogFile.
 *
 * Revision 3.141  2009/12/08 02:42:03  sueh
 * bug# 1286 Implemented Loggable in parameter classes.
 *
 * Revision 3.140  2009/10/30 20:53:07  sueh
 * Removed unnecessary prints.
 *
 * Revision 3.139  2009/10/23 22:24:06  sueh
 * bug# 1275 Made touch() a static function in BaseProcessManager.
 *
 * Revision 3.138  2009/10/06 01:24:46  sueh
 * bug# 1246 In postProcess(BackgroundProcess) send flattenwarp output to the project log.
 *
 * Revision 3.137  2009/09/21 17:49:35  sueh
 * bug# 1267 Added tilt3dFind because it uses a child of the tilt monitor.
 * Added tilt3dFindReproject because is doesn't need a monitor.
 *
 * Revision 3.136  2009/09/01 03:17:56  sueh
 * bug# 1222
 *
 * Revision 3.135  2009/08/20 23:37:16  sueh
 * bug# 1255 Ported from 3.13.
 *
 * Revision 3.126.2.1  2009/08/20 22:45:07  sueh
 * bug# 1255 Checking the return value of CopyTomoComs.setup.
 *
 * Revision 3.134  2009/06/05 01:59:43  sueh
 * bug# 1219 Added flatten and flattenWarp.
 *
 * Revision 3.133  2009/05/02 01:07:51  sueh
 * bug# 1216 Added runraptor.
 *
 * Revision 3.132  2009/04/15 16:52:47  sueh
 * bug# 1190 Returning false and logging failure for major reconnection
 * failures.
 *
 * Revision 3.131  2009/03/23 17:08:50  sueh
 * bug# 1187 Moved taError logging to ApplicationManager.
 *
 * Revision 3.130  2009/03/17 00:43:42  sueh
 * bug# 1186 Pass managerKey to everything that pops up a dialog.
 *
 * Revision 3.129  2009/02/13 02:31:51  sueh
 * bug# 1176 Checking return value of MRCHeader.read.
 *
 * Revision 3.128  2009/02/10 22:08:16  sueh
 * bug# 1143 In postProcess(BackgroundProcess) save the size of the
 * trimvol input file.
 *
 * Revision 3.127  2009/02/04 23:26:53  sueh
 * bug# 1158 Changed id and exceptions classes in LogFile.
 *
 * Revision 3.126  2008/11/20 01:33:37  sueh
 * bug# 1147 Added ccdEraser, to run ccderaser as a command.
 *
 * Revision 3.125  2008/10/30 21:02:19  sueh
 * bug# 1145 In midasBlendStack, added -t option.
 *
 * Revision 3.124  2008/10/30 20:51:25  sueh
 * bug# 1145 In midasRawStack, added -t option.
 *
 * Revision 3.123  2008/10/27 18:06:28  sueh
 * bug# 1141 Added ctfCorrection, ctfPlotter, setupCtfCorrectionComScript,
 * setupCtfPlotterComScript, and splitCorrection.
 *
 * Revision 3.122  2008/09/26 18:55:21  sueh
 * bug# 1140 In modelToPatch, using imod2vatch for Windows.
 *
 * Revision 3.121  2008/05/03 00:42:33  sueh
 * bug# 847 Passing a ProcessSeries instance to all processes that use
 * process objects.  The goal is to pass then back to process done functions.
 *
 * Revision 3.120  2008/01/31 20:19:24  sueh
 * bug# 1055 throwing a FileException when LogFile.getInstance fails.
 *
 * Revision 3.119  2008/01/14 21:51:10  sueh
 * bug# 1050 Moved getRunningProcessData from ProcessManager to
 * BaseProcessManager.
 *
 * Revision 3.118  2007/12/26 22:14:52  sueh
 * bug# 1052 Moved argument handling from EtomoDirector to a separate class.
 *
 * Revision 3.117  2007/12/13 01:10:10  sueh
 * bug# 1056 Made tilt() always pass the param.  Doing post processing on Tilt;
 * setting AdjustOrigin for tilt and whole tomogram.
 *
 * Revision 3.116  2007/09/07 00:19:24  sueh
 * bug# 989 Using a public INSTANCE to refer to the EtomoDirector singleton
 * instead of getInstance and createInstance.
 *
 * Revision 3.115  2007/02/05 23:02:32  sueh
 * bug# 962 Handling remapmodel, xfmodel, and xftoxg.
 *
 * Revision 3.114  2006/11/15 19:59:35  sueh
 * bug# 872 Changed getParamFileStorableArray to getStorables in the managers.
 *
 * Revision 3.113  2006/10/16 22:43:41  sueh
 * bug# 933  Moved the code in msgAlignPostProcess to postProcess.  Only doing
 * align post process (reopening log) when the Computer Alignment button was
 * used to run Fine Alignment.
 *
 * Revision 3.112  2006/10/11 10:10:25  sueh
 * bug# 931 Added delete functionality to LogFile - changed BackupException to
 * FileException.
 *
 * Revision 3.111  2006/10/10 07:43:14  sueh
 * bug# 931 In handleTransferfidMessage(), close the log file writer on error.
 *
 * Revision 3.110  2006/10/10 05:12:40  sueh
 * bug# 931  runCommand():  Managing the optional log file with LogFile.
 *
 * Revision 3.109  2006/09/20 20:42:58  sueh
 * bug# 928 Adding combine to errorProcess, so that the combine dialog can check
 * for the existance of patch_vector.mod.
 *
 * Revision 3.108  2006/09/13 23:28:54  sueh
 * bug# 920 Moving some postProcessing to TomogramPositioningExpert.
 *
 * Revision 3.107  2006/08/11 23:50:59  sueh
 * bug# 816 PostProcess(ComScriptProcess):  calling msgAlignPostProcess()
 * when align succeeds.
 *
 * Revision 3.106  2006/08/03 21:29:06  sueh
 * bug# 769 Passing ProcessResultDisplay to reconnectTilt.
 *
 * Revision 3.105  2006/08/02 22:25:06  sueh
 * bug# 769 Added empty functions errorProcess and postProcess.  Added
 * getRunningProcessName() and reconnect().
 *
 * Revision 3.104  2006/07/19 20:10:39  sueh
 * bug# 902 PostProcess(BackgroundProcess):  Setting seeding done when
 * process is transfer fid.
 *
 * Revision 3.103  2006/07/19 15:22:20  sueh
 * format
 *
 * Revision 3.102  2006/07/05 23:24:35  sueh
 * set trimvolFlipped to true if rotate X is done.
 *
 * Revision 3.101  2006/06/09 16:55:39  sueh
 * bug# 869 setupCombineScripts(): save the state in TomogramState.
 *
 * Revision 3.100  2006/06/05 16:31:58  sueh
 * bug# 766 Added manager to the base class.  Passing the process name to the
 * processes.
 *
 * Revision 3.99  2006/05/23 21:05:37  sueh
 * bug# 617 Setting fidFileLastModified in TomogramState when track fiducials
 * succeeds.
 *
 * Revision 3.98  2006/05/22 22:49:55  sueh
 * bug# 577 popuping up dialog when copyTomoComs doesn't return 0
 *
 * Revision 3.97  2006/05/19 19:40:12  sueh
 * bug# 838 Copying align z shift and angle offset to sample z shift and angle offset
 * when sample is completed.
 *
 * Revision 3.96  2006/05/11 19:55:54  sueh
 * bug# 838 Add CommandDetails, which extends Command and
 * ProcessDetails.  Changed ProcessDetails to only contain generic get
 * functions.  Command contains all the command oriented functions.
 * Added post-processing to save that angle offset and z shift after align and
 * the x axis tilt after tilt.
 *
 * Revision 3.95  2006/04/07 17:55:38  sueh
 * setupCombineScripts:  processResultDisplay is null when dataflowtests
 * is run.
 *
 * Revision 3.94  2006/04/06 19:43:26  sueh
 * bug# 808 Using Fields to pass requests to the generic gets in
 * ProcessDetails.
 *
 * Revision 3.93  2006/03/16 01:52:41  sueh
 * bug#  830 Handling exitValue != 0 in setupComScripts()
 *
 * Revision 3.92  2006/02/22 19:06:56  sueh
 * bug# 817 ModelToPatch():  don't try to move patch.out to patch.out~ if
 * patch.out doesn't exist.
 *
 * Revision 3.91  2006/01/31 20:46:03  sueh
 * bug# 521 Made trim vol a toggle button and added ProcessResultDisplay
 * to process.
 *
 * Revision 3.90  2006/01/26 21:54:45  sueh
 * bug# 401 Added processResultDisplay parameters to all the functions associated
 * with toggle buttons.
 *
 * Revision 3.89  2006/01/20 20:56:39  sueh
 * updated copyright year
 *
 * Revision 3.88  2005/12/09 20:28:09  sueh
 * bug# 776 Added non-abstract super.postProcessing to handle
 * tomosnapshot
 *
 * Revision 3.87  2005/11/19 02:36:48  sueh
 * bug# 744 Moved functions only used by process manager post
 * processing and error processing from Commands to ProcessDetails.
 * This allows ProcesschunksParam to be passed to DetachedProcess
 * without having to add unnecessary functions to it.
 *
 * Revision 3.86  2005/11/02 21:58:17  sueh
 * bug# 754 Parsing errors and warnings inside ProcessMessages.
 *
 * Revision 3.85  2005/10/28 21:46:05  sueh
 * bug# 725 setComScripts() do not fail on error messages if exitValue is 0.
 * This means that copytomocoms succeeded but a process it called printed
 * an error message.
 *
 * Revision 3.84  2005/10/28 18:50:31  sueh
 * bug# 725 In setupComScripts, checking for error messages and returning
 * false if any are found.
 *
 * Revision 3.83  2005/10/27 00:30:33  sueh
 * bug# 725 Added functions:  extracttilts, extractpieces, and extractmagrad.
 * Each runs a backgroundProcess with forceNextProcess turned on.
 *
 * Revision 3.82  2005/09/29 18:39:28  sueh
 * bug# 532 Preventing Etomo from saving to the .edf or .ejf file over and
 * over during exit.  Added BaseManager.exiting and
 * saveIntermediateParamFile(), which will not save when exiting it true.
 * Setting exiting to true in BaseManager.exitProgram().  Moved call to
 * saveParamFile() to the child exitProgram functions so that the param file
 * is saved after all the done functions are run.
 *
 * Revision 3.81  2005/09/21 16:09:49  sueh
 * bug# 532 moved processchunks() from processManager to
 * BaseProcessManager.  This allows BaseManager to handle
 * processchunks.
 *
 * Revision 3.80  2005/09/16 17:49:35  sueh
 * Added splitcombine().
 *
 * Revision 3.79  2005/09/09 21:43:36  sueh
 * bug# 532 Handling null from stderr and stdout.
 *
 * Revision 3.78  2005/09/01 17:55:42  sueh
 * bug# 532  Change ProcesschunksParam.computerList() to machineList.
 *
 * Revision 3.77  2005/08/30 18:51:33  sueh
 * bug# 532 Added computerList to ProcesschunksProcessMonitor
 * constructor.
 *
 * Revision 3.76  2005/08/24 22:36:29  sueh
 * bug# 715 Passing the param to crossCorrelate() and preblend() so it can
 * be used in postProcess() and errorProcess().  Use TiltxcorrParam when
 * blendmont will not run.  Use BlendmontParam when blendmont will run.
 *
 * Revision 3.75  2005/08/22 17:06:51  sueh
 * bug# 532 Added the root name to the processchunks monitor.
 *
 * Revision 3.74  2005/08/15 18:24:24  sueh
 * bug# 532 deleted commented out code in processchunks().
 *
 * Revision 3.73  2005/08/04 19:47:32  sueh
 * bug# 532 Removed demo functions and added a monitor to
 * processchunks.
 *
 * Revision 3.72  2005/08/01 18:03:35  sueh
 * bug# 532 Added processchunks().
 *
 * Revision 3.71  2005/07/29 00:52:48  sueh
 * bug# 709 Going to EtomoDirector to get the current manager is unreliable
 * because the current manager changes when the user changes the tab.
 * Passing the manager where its needed.
 *
 * Revision 3.70  2005/07/26 21:40:14  sueh
 * bug# 532 removed unnecessary function: tiltPArallelProcess()
 *
 * Revision 3.69  2005/07/21 21:41:05  sueh
 * bug# 532 added splittilt
 *
 * Revision 3.68  2005/07/14 22:00:44  sueh
 * bug# 626 Passing BlendmontParam to blend() because BlendmontParam
 * can either be in blend mode or whole tomogram mode.
 *
 * Revision 3.67  2005/07/11 22:48:51  sueh
 * bug# 619 Added functions resumeTiltParallelProcessDemo and
 * tiltParallelProcessDemo.
 *
 * Revision 3.66  2005/05/18 22:35:45  sueh
 * bug# 662 Utilities.fileExists() is getting metaData from EtomoDirector,
 * instead of receiving it as a parameter.
 *
 * Revision 3.65  2005/05/10 17:33:03  sueh
 * bug# 660 Popping up warnings from setup combine.
 *
 * Revision 3.64  2005/05/10 16:58:12  sueh
 * bug# 660 Popping up warnings from copytomocoms.
 *
 * Revision 3.63  2005/04/26 17:36:43  sueh
 * bug# 615 Change the name of the UIHarness member variable to
 * uiHarness.
 *
 * Revision 3.62  2005/04/25 20:49:19  sueh
 * bug# 615 Passing the axis where a command originates to the message
 * functions so that the message will be popped up in the correct window.
 * This requires adding AxisID to many objects.
 *
 * Revision 3.61  2005/04/07 21:54:47  sueh
 * bug# 626 Added makeDistortionCorrectedStack to run undistort.com.
 * Added post processing to enable fixing edges with midas when undistort
 * succeeded.
 *
 * Revision 3.60  2005/03/11 01:34:28  sueh
 * bug# 533 Adding -q (quiet) option to midas call in midasEdges.
 *
 * Revision 3.59  2005/03/09 22:46:00  sueh
 * bug# 533 Added blend().
 *
 * Revision 3.58  2005/03/09 18:08:05  sueh
 * bug# 533 In crossCorrelate(), if blendmont will be run, use the
 * XcorrProcessWatcher instead of the TiltxcorrProcessWatcher.
 *
 * Revision 3.57  2005/03/08 18:31:00  sueh
 * bug# 533 Added midasBlendStack() to run midas with the .bl stack.
 *
 * Revision 3.56  2005/03/08 01:58:30  sueh
 * bug# 533 Adding midasEdges() and preblend().
 *
 * Revision 3.55  2005/02/17 19:26:04  sueh
 * bug# 606 Pass AxisID when setting and getting makeZFactors,
 * newstFiducialessAlignment, and usedLocalAlignments.
 *
 * Revision 3.54  2005/02/12 01:28:02  sueh
 * bug# 601 Calling handleTransferfidMessage() from errorProcess().
 *
 * Revision 3.53  2005/01/26 04:27:52  sueh
 * bug# 83 Adding mtffilter process monitor to mtffilter.
 *
 * Revision 3.52  2005/01/21 22:55:38  sueh
 * bug# 509 bug# 591  Converted transferfidParam.BToA to EtomoBoolean.
 *
 * Revision 3.51  2005/01/12 00:42:12  sueh
 * bug# 579 Set usedLocalAlignments in TomogramState after tiltalign.
 *
 * Revision 3.50  2005/01/08 01:52:32  sueh
 * bug# 578 Passed NewstParam to startComScript().  Added post
 * processing for tilt.  Only doing post processing on newst when it was
 * used to create the full aligned stack.
 *
 * Revision 3.49  2005/01/06 18:12:00  sueh
 * bug# 578 In postProcess(ComScriptProcess), setting
 * TomogramState.skewOption and xStretchOption when align is finished.
 *
 * Revision 3.48  2005/01/05 19:56:00  sueh
 * bug# 578 Moved startBackgroundComScript(String, Runnable, AxisID,
 * ComscriptState, String) and startComScript(String, Runnable, AxisID,
 * String) from ProcessManager to BaseProcessManager since they are
 * generic.  Added startComScript(Command, Runnable, AxisID) to handle
 * situations where postProcess(ComScriptProcess) needs to query the
 * command.  Added ConstTiltalignParam parameter to fineAlignment so
 * that it can be queried by postProcess().
 *
 * Revision 3.47  2004/12/16 02:26:20  sueh
 * bug# 564 Save flipped state in Squeezevol post processing.
 *
 * Revision 3.46  2004/12/14 21:38:39  sueh
 * bug# 572:  Removing state object from meta data and managing it with a
 * manager class.  All state variables saved after a process is run belong in
 * the state object.
 *
 * Revision 3.45  2004/12/09 04:57:40  sueh
 * bug# 565 Save meta data in functions which to not call a start... function
 * and may been preceded by a change in meta data.  Don't do this for
 * setupComScript() because it is followed by doneSetupDialog(), which
 * saves meta data.
 *
 * Revision 3.44  2004/12/08 21:24:59  sueh
 * bug# 564 In postProcess(BackgroundProcess) saved
 * TrimvolParam.swapYZ in metaData.state.
 *
 * Revision 3.43  2004/12/02 18:28:29  sueh
 * bug# 557 Added squeezeVolume().
 *
 * Revision 3.42  2004/11/24 01:03:00  sueh
 * bug# 520 ADded errorProcess(ComScriptProcess) and removed exitValue
 * parameter from postProcess(BackgroundProcess).
 *
 * Revision 3.41  2004/11/20 01:58:47  sueh
 * bug# 520 Passing exitValue to postProcess(BackgroundProcess).
 *
 * Revision 3.40  2004/11/19 23:24:22  sueh
 * bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 *
 * Revision 3.39.2.11  2004/11/12 22:55:13  sueh
 * bug# 520 Using overloading to simiplify the postProcess function names.
 *
 * Revision 3.39.2.10  2004/10/25 23:12:05  sueh
 * bug# 520 Added a call to backgroundErrorProcess() for post processing
 * when BackgroundProcess fails.
 *
 * Revision 3.39.2.9  2004/10/21 02:46:29  sueh
 * bug# 520 Added empty interactiveSystemProgramPostProcess().
 *
 * Revision 3.39.2.8  2004/10/18 19:10:24  sueh
 * bug# 520 Added getManager().  Moved startSystemProgramThread() to
 * base class.
 *
 * Revision 3.39.2.7  2004/10/11 02:04:22  sueh
 * bug# 520 Using a variable called propertyUserDir instead of the "user.dir"
 * property.  This property would need a different value for each manager.
 * This variable can be retrieved from the manager if the object knows its
 * manager.  Otherwise it can retrieve it from the current manager using the
 * EtomoDirector singleton.  If there is no current manager, EtomoDirector
 * gets the value from the "user.dir" property.
 *
 * Revision 3.39.2.6  2004/10/08 16:07:17  sueh
 * bug# 520 Since EtomoDirector is a singleton, made all functions and
 * member variables non-static.
 *
 * Revision 3.39.2.5  2004/10/06 01:48:00  sueh
 * bug# 520 Move StartBackgroundProcess() to base class.  Put non-
 * generic post processing for transferfid into backgroundPostProcess().
 *
 * Revision 3.39.2.4  2004/09/29 19:11:07  sueh
 * bug# 520 Added base class.  Moved functionality in common with
 * JoinProcessManager to base class.
 *
 * Revision 3.39.2.3  2004/09/15 22:35:09  sueh
 * bug# 520 call openMessageDialog in mainPanel instead of mainFrame
 *
 * Revision 3.39.2.2  2004/09/07 17:56:08  sueh
 * bug# 520 getting dataset name from metadata
 *
 * Revision 3.39.2.1  2004/09/03 21:13:07  sueh
 * bug# 520 calling functions from EtomoDirector instead of
 * ApplicationManager
 *
 * Revision 3.39  2004/08/30 18:43:55  sueh
 * bug# 508 Use notifyKill() to tell this object that
 * a kill has been requested.  This way kill() doesn't have to
 * check the type of the thread object.
 *
 * Revision 3.38  2004/08/28 00:59:26  sueh
 * bug# 508 In startComScript checking ComScriptProcess.isError() as
 * well as isStarted() in order to get out of a loop.
 * In msgComScriptDone handling stdError == null
 *
 * Revision 3.37  2004/08/26 01:10:55  sueh
 * bug# 508 removing sleep between kill ground and kill process and
 * descendents because it appears to be unnecessary.
 *
 * Revision 3.36  2004/08/25 23:04:16  sueh
 * bug# moved kill signal to a separate function kill(String,String)
 * sleep between kill group and kill process and descendents
 * (probably not necessary)
 *
 * Revision 3.35  2004/08/25 21:00:03  sueh
 * bug# 508 removing diagnostic prints and adding kill by
 * group (killProcessGroup)
 *
 * Revision 3.34  2004/08/25 18:35:53  sueh
 * bug# 508 print diagnostics during kill
 *
 * Revision 3.33  2004/08/24 20:44:09  sueh
 * bug# 508 change BackgroundProcessMonitor.kill() to
 * killMonitor()
 *
 * Revision 3.32  2004/08/24 20:41:55  sueh
 * bug# 508 running killProcessAndDescendants() causes a delay.  If
 * BackgroundComScriptProcess.kill() is called before
 * killProcessAndDescendents(), it pops up an message box immediately
 * and then pauses for 4 to 8 seconds before it displays the message and
 * OK button.  Calling kill() after killProcessAndDescendants() puts the
 * delay before the message box is popped up, which is more like the
 * behavior of other dialogs.  It also makes more sense to kill the monitor
 * after killing the processes.
 *
 * Revision 3.31  2004/08/23 23:41:48  sueh
 * bug# 508 passed watched file into BackgroundComScriptPRocess
 * the same way it is passed to ComScriptPRocess.  remove 
 * unnecessary watchedFileNAme parameter from the startComScript(
 * ComScriptProcess, String, Runnable, AxisID, String watchedFileNAme)
 *
 * Revision 3.30  2004/08/19 02:39:18  sueh
 * bug# 508 Passing CombineComscriptState to combine() so it can be
 * passed to BackgroundComScriptProcess.  In kill(), when the thread is
 * an instance of BackgroundComScriptProcess, call
 * BackgroundComScriptProcess.setKilled() to stop the process monitor.
 * Added:
 * combine(CombineComscriptState combineComscriptState)
 * startBackgroundComScript(String command,
 *     Runnable processMonitor, AxisID axisID,
 *     ComscriptState comscriptState)
 * Changed:
 * kill(AxisID axisID)
 * Deleted:
 * combine()
 * startBackgroundComScript(
 *     String command,
 *     Runnable processMonitor,
 *     AxisID axisID)
 *
 * Revision 3.29  2004/08/06 23:08:55  sueh
 * bug# 508 modified combine() to use BackgroundComScriptProcess.
 * Modified startComScript to accept a ComScriptProcess parameter,
 * so that it can be used with either ComScriptProcess or
 * BackgroundComScriptProcess.
 *
 * Revision 3.28  2004/08/02 23:50:18  sueh
 * bug# 519 improving error handling in
 * setupNonFiducialAlign
 *
 * Revision 3.27  2004/08/02 23:02:37  sueh
 * bug# 519 call ApplicationManager.makeRawtltFile() if .rawtlt
 * doesn't exist
 *
 * Revision 3.26  2004/07/20 20:06:20  sueh
 * bug# 405 changing to ps axl because mac requires ps x when
 * terminal has been exited and windows doesn't understand
 * ps -x, but it does understand ps x
 *
 * Revision 3.25  2004/07/14 18:43:27  sueh
 * bug# 405 go back to ps -l:  its faster and works for all the
 * ways to run etomo that we have tested
 *
 * Revision 3.24  2004/07/13 23:06:09  sueh
 * bug# 405 get a list of child processes instead of one.  This
 * will speed things up when there are sibling processes.
 *
 * Revision 3.23  2004/07/12 22:35:39  sueh
 * bug# 405 get the command name correctly for Linux and
 * Windows
 *
 * Revision 3.22  2004/07/07 00:22:38  sueh
 * bug# 405 in kill() recursively killing the descendants of a process
 * before killing the process
 *
 * Revision 3.21  2004/07/02 22:01:32  rickg
 * Bug #491 Fixed xf detection logic
 *
 * Revision 3.20  2004/07/02 16:50:39  sueh
 * bug# 490 in startComScript() constucting ComScriptProcess with
 * watchedFileName.  In patchcorr() sending the watched file
 * name to startComScript()
 *
 * Revision 3.19  2004/06/28 22:10:29  rickg
 * Bug #470 Moved the fiducial mode file copying to the same sections
 * where the fiducialless is handled.
 *
 * Revision 3.18  2004/06/25 23:13:46  rickg
 * Bug #470 Added generatePreXG method.
 *
 * Revision 3.17  2004/06/18 05:46:27  rickg
 * Shortened sleep while waited com script to start, this should
 * help but not totally solve the extremely short run com problem
 *
 * Revision 3.16  2004/06/17 23:55:51  rickg
 * Bug #460 moved getting of current time into FileSizeProcessMonitor on
 * instantiation
 *
 * Revision 3.15  2004/06/17 23:34:17  rickg
 * Bug #460 added script starting time to differentiate old data files
 *
 * Revision 3.14  2004/06/15 20:07:09  rickg
 * Bug #469 Rotation.xf was not getting the axisID inserted into the
 * filename.
 *
 * Revision 3.13  2004/06/02 23:45:56  rickg
 * Bug #391 added logic for backward compatibility in manage .xf
 * and .tlt files
 *
 * Revision 3.12  2004/06/02 20:41:55  rickg
 * Bug #187 catch ERROR: in stdout and added to error message
 *
 * Revision 3.11  2004/05/27 22:51:04  rickg
 * Bug #391 setupFiducialAlign will now remove .xf and .tlt if its
 * source files don't exist
 *
 * Revision 3.10  2004/05/25 23:21:55  rickg
 * Bug #391 midas now opens the image rotated by the tilt axis
 * angle
 *
 * Revision 3.9  2004/05/21 02:16:26  sueh
 * bug# 83 adding VolcombineProcessMonitor to volcombine()
 *
 * Revision 3.8  2004/04/26 00:21:35  rickg
 * Comment change
 *
 * Revision 3.7  2004/04/22 23:32:24  rickg
 * bug #391 Added processing for non fid aligne
 * Added runCommand private method
 *
 * Revision 3.6  2004/04/16 01:56:26  sueh
 * bug# 409 In msgComScriptDone: passed the script's processName and axisID to
 * ApplicationManager.processDone, simplified log generation.
 *
 * Revision 3.5  2004/03/29 20:57:58  sueh
 * bug# 409 added mtffilter()
 *
 * Revision 3.4  2004/03/22 23:47:31  sueh
 * bug# 83 Use PatchcorrProcessWatcher when running patchcorr.
 *
 * Revision 3.3  2004/03/11 00:00:33  sueh
 * bug# 61
 *
 * Revision 3.2  2003/11/26 23:39:14  rickg
 * Debug flag and getter changed to static in AppManager.
 *
 * Revision 3.1  2003/11/10 07:33:06  rickg
 * Bug# 353 Transferfid log window is now a standard log window
 *
 * Revision 3.0  2003/11/07 23:19:00  rickg
 * Version 1.0.0
 *
 * Revision 2.33  2003/11/04 20:56:11  rickg
 * Bug #345 IMOD Directory supplied by a static function from ApplicationManager
 *
 * Revision 2.32  2003/11/04 01:02:41  rickg
 * Bug #345 Explicitly set path to script using IMOD_DIR
 *
 * <p>
 * Revision 2.31 2003/10/27 23:56:33 rickg
 * <p>
 * Bug# 283 Open tomopitch log file in done message when
 * <p>
 * appropriate
 * <p>
 * <p>
 * Revision 2.30 2003/10/24 19:07:55 rickg
 * <p>
 * Bug# 333 Fixed null reference to stderr array
 * <p>
 * <p>
 * Revision 2.29 2003/10/09 23:11:12 sueh
 * <p>
 * bug251 fixed Kill Process and fixed prevention of two processes
 * <p>
 * on the same axis by shifting transferfid process to the correct
 * <p>
 * axis (the destination axis where the button was pushed).
 * <p>
 * <p>
 * Revision 2.28 2003/10/09 05:58:41 rickg
 * <p>
 * Forget to get transferfid command line from BackgroundProcess
 * <p>
 * <p>
 * Revision 2.27 2003/10/06 21:59:52 rickg
 * <p>
 * split mapAxisThread and mapAxisProcessMonitor
 * <p>
 * <p>
 * Revision 2.26 2003/10/05 21:55:08 rickg
 * <p>
 * Changed method order to clarify structure
 * <p>
 * <p>
 * Revision 2.25 2003/10/05 21:32:49 rickg
 * <p>
 * Bug# 256
 * <p>
 * Generalized BackgroundProcess starting
 * <p>
 * <p>
 * Revision 2.24 2003/10/01 18:16:54 rickg
 * <p>
 * Update demo mode to work with process monitor structure
 * <p>
 * <p>
 * Revision 2.23 2003/10/01 04:22:47 rickg
 * <p>
 * Moved the order of processing in msgComScriptDone to
 * <p>
 * keep monitor around till after the processDone method is called
 * <p>
 * <p>
 * Revision 2.22 2003/09/08 22:20:35 rickg
 * <p>
 * Throw an exception if a process is already running for the current
 * <p>
 * axis
 * <p>
 * <p>
 * Revision 2.21 2003/08/20 22:01:34 rickg
 * <p>
 * Explicitly wait for the comScriptProcess to start before starting the
 * <p>
 * process monitor thread.
 * <p>
 * <p>
 * Revision 2.20 2003/08/05 21:40:53 rickg
 * <p>
 * Implemented CCDEraserProcessMonitor
 * <p>
 * LogFileProcessMonitors now are passed to startComScriptProcess
 * <p>
 * comScript => comScriptProcess
 * <p>
 * <p>
 * Revision 2.19 2003/07/01 22:52:58 rickg
 * <p>
 * Start process monitor thread before comscript thread
 * <p>
 * <p>
 * Revision 2.18 2003/07/01 19:28:27 rickg
 * <p>
 * startComScript and mapThreadAxis now manage the
 * <p>
 * processMonitor
 * <p>
 * prenewst, newst and tilt switched fileSizeProcessMonitors
 * <p>
 * <p>
 * Revision 2.17 2003/06/27 20:17:19 rickg
 * <p>
 * Added process monitor class members and management
 * <p>
 * <p>
 * Revision 2.16 2003/06/05 21:11:34 rickg
 * <p>
 * Parse ERROR: from stdout
 * <p>
 * <p>
 * Revision 2.15 2003/06/05 16:52:37 rickg
 * <p>
 * Removed stdout messages
 * <p>
 * <p>
 * Revision 2.14 2003/06/05 04:40:15 rickg
 * <p>
 * thread references changed to SystemProcessInterface
 * <p>
 * mapped threads for transferfid and trimvol
 * <p>
 * <p>
 * Revision 2.13 2003/05/27 08:45:59 rickg
 * <p>
 * Added new method to generate the align logs
 * <p>
 * Added a section to the comscript done section for scipt
 * <p>
 * dependent post processing
 * <p>
 * <p>
 * Revision 2.12 2003/05/23 14:26:35 rickg
 * <p>
 * RunComscript renamed to ComScriptProcess
 * <p>
 * StartComScript method returns ComScriptProcess instead
 * <p>
 * of string name
 * <p>
 * Starting to implement determinant progress bar functions
 * <p>
 * <p>
 * Revision 2.11 2003/05/22 23:34:53 rickg
 * <p>
 * Kill process now kills by walking the process tree.
 * <p>
 * <p>
 * Revision 2.10 2003/05/21 22:56:23 rickg
 * <p>
 * Initial kill implementation
 * <p>
 * <p>
 * Revision 2.9 2003/05/13 16:56:04 rickg
 * <p>
 * Use whole command line for transferfid command ID, since
 * <p>
 * windows/cygwin needs a preceeding tcsh
 * <p>
 * <p>
 * Revision 2.8 2003/05/08 23:19:03 rickg
 * <p>
 * Standardized debug setting
 * <p>
 * <p>
 * Revision 2.7 2003/05/07 22:27:07 rickg
 * <p>
 * System property user.dir now defines the working directory
 * <p>
 * <p>
 * Revision 2.6 2003/04/24 17:46:54 rickg
 * <p>
 * Changed fileset name to dataset name
 * <p>
 * <p>
 * Revision 2.5 2003/04/16 00:14:26 rickg
 * <p>
 * Trimvol in progress
 * <p>
 * <p>
 * Revision 2.4 2003/03/26 00:52:42 rickg
 * <p>
 * Added button to convert patch_vector.mod to patch.out
 * <p>
 * <p>
 * Revision 2.3 2003/03/18 00:32:33 rickg
 * <p>
 * combine development in progress
 * <p>
 * <p>
 * Revision 2.2 2003/02/24 23:35:24 rickg
 * <p>
 * Added management of threads for each axis
 * <p>
 * Added interrupt method (doesn't work well with C shell scripts)
 * <p>
 * <p>
 * Revision 2.1 2003/01/29 20:46:40 rickg
 * <p>
 * Check debug mode for messages
 * <p>
 * <p>
 * Revision 2.0 2003/01/24 20:30:31 rickg
 * <p>
 * Single window merge to main branch
 * <p>
 * <p>
 * Revision 1.14.2.1 2003/01/24 18:36:17 rickg
 * <p>
 * Single window GUI layout initial revision
 * <p>
 * <p>
 * Revision 1.14 2003/01/08 05:00:13 rickg
 * <p>
 * Wrote transferfid log file and create stdout dialog box
 * <p>
 * <p>
 * Revision 1.13 2003/01/08 03:56:17 rickg
 * <p>
 * Mods in progress
 * <p>
 * <p>
 * Revision 1.12 2003/01/04 00:23:36 rickg
 * <p>
 * Added a test method.
 * <p>
 * TransferFiducials method reports command name
 * <p>
 * instead of thread name.
 * <p>
 * <p>
 * Revision 1.11 2003/01/03 00:56:00 rickg
 * <p>
 * Added msgBackgroundProcessDone method
 * <p>
 * <p>
 * Revision 1.10 2002/12/31 00:57:38 rickg
 * <p>
 * Added transferFiducials method
 * <p>
 * <p>
 * Revision 1.9 2002/12/30 20:38:11 rickg
 * <p>
 * msgComScriptDone now invokes appropriate error
 * <p>
 * or warning dialogs
 * <p>
 * <p>
 * Revision 1.8 2002/10/22 21:38:33 rickg
 * <p>
 * ApplicationManager now controls both demo and debug
 * <p>
 * modes
 * <p>
 * <p>
 * Revision 1.7 2002/10/14 22:45:07 rickg
 * <p>
 * Enabled debug output for com scripts
 * <p>
 * <p>
 * Revision 1.6 2002/10/14 19:02:37 rickg
 * <p>
 * Opens a dialog through the app manager if a process returns
 * <p>
 * an error
 * <p>
 * <p>
 * Revision 1.5 2002/10/10 23:39:56 rickg
 * <p>
 * refactored createCombineScripts to setupCombineScripts
 * <p>
 * <p>
 * Revision 1.4 2002/10/10 18:47:16 rickg
 * <p>
 * Enabled debugging output from the SystemProgram object
 * <p>
 * <p>
 * Revision 1.3 2002/10/07 22:26:31 rickg
 * <p>
 * removed unused imports
 * <p>
 * reformat after emacs messed it up
 * <p>
 * <p>
 * Revision 1.2 2002/09/19 21:43:30 rickg
 * <p>
 * Moved imod control to ImodManager/ImodProcess objects
 * <p>
 * <p>
 * Revision 1.1 2002/09/09 22:57:02 rickg
 * <p>
 * Initial CVS entry, basic functionality not including combining
 * <p>
 * </p>
 */
public class ProcessManager extends BaseProcessManager {
  // variables cast from base class variables
  // initialized in constructor
  private final ApplicationManager appManager;

  // save the transferfid command line so that we can identify when process is
  // complete.
  String transferfidCommandLine;

  public ProcessManager(ApplicationManager appMgr) {
    super(appMgr);
    appManager = appMgr;
  }

  public void
    setupCtfPlotterComScript(CtfPhaseFlipParam ctfPhaseFlipParam, AxisID axisID) {
    CopyTomoComs copyTomoComs = new CopyTomoComs(appManager);
    copyTomoComs.setCTFFiles(CopyTomoComs.CtfFilesValue.CTF_PLOTTER);
    copyTomoComs.setVoltage(ctfPhaseFlipParam.getVoltage());
    copyTomoComs.setSphericalAberration(ctfPhaseFlipParam.getSphericalAberration());
    setupComScripts(copyTomoComs, axisID, null);
  }

  public void setupCtfCorrectionComScript(AxisID axisID) {
    CopyTomoComs copyTomoComs = new CopyTomoComs(appManager);
    copyTomoComs.setCTFFiles(CopyTomoComs.CtfFilesValue.CTF_CORRECTION);
    setupComScripts(copyTomoComs, axisID, null);
  }

  public ProcessMessages setupComScripts(AxisID axisID, final CopyTomoComs param,
    final AxisType axisType) {
    if (EtomoDirector.INSTANCE.getArguments().isDebug()) {
      System.err.println("copytomocoms command line: " + param.getCommandLine());
    }
    appManager.saveStorables(axisID);
    return setupComScripts(param, axisID, axisType);
  }

  /**
   * Run the copytomocoms script.  Don't need to save meta data because
   * this function is run on exiting Setup dialog
   * 
   * @param metaData
   *          a read-only MetaData object containing the information to run the
   *          copytomocoms script
   */
  private ProcessMessages setupComScripts(CopyTomoComs copyTomoComs, AxisID axisID,
    final AxisType axisType) {
    if (!copyTomoComs.setup()) {
      return null;
    }
    int exitValue = copyTomoComs.run();
    // process messages
    ProcessMessages messages = copyTomoComs.getProcessMessages();
    if (!messages.isEmpty(ProcessMessages.MessageType.INFO)) {
      // smallest signed 16-bit integer (amount that needs to be added to make everything
      // positive).
      String infoValue = "32768";
      for (int i = 0; i < messages.size(ProcessMessages.MessageType.INFO); i++) {
        if (messages.get(ProcessMessages.MessageType.INFO, i).indexOf(infoValue) != -1) {
          appManager.getMetaData().setGenLog(AxisID.FIRST, infoValue);
          if (axisType != null && axisType == AxisType.DUAL_AXIS) {
            appManager.getMetaData().setGenLog(AxisID.SECOND, infoValue);
          }
        }
      }
    }
    if (!messages.isEmpty(ProcessMessages.MessageType.ERROR)) {
      StringBuffer errorMessage = new StringBuffer("Error running Copytomocoms");
      for (int i = 0; i < messages.size(ProcessMessages.MessageType.ERROR); i++) {
        errorMessage.append("\n" + messages.get(ProcessMessages.MessageType.ERROR, i));
      }
      UIHarness.INSTANCE.openMessageDialog(appManager, errorMessage.toString(),
        "Copytomocoms Error", axisID);
    }
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.WARNING); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.WARNING, i), "Copytomocoms Warning",
        axisID);
    }
    if (exitValue != 0) {
      UIHarness.INSTANCE.openMessageDialog(appManager, copyTomoComs.getStdErrorString(),
        "Copytomocoms Error", axisID);
      return null;
    }
    return messages;
  }

  public boolean batchruntomo(final AxisID axisID, final BatchruntomoParam param) {
    if (!param.setupValidationCommand()) {
      return false;
    }
    int exitValue = param.run();
    // process messages
    ProcessMessages messages = param.getProcessMessages();
    boolean err = !messages.isEmpty(ProcessMessages.MessageType.ERROR);
    if (err) {
      StringBuffer errorMessage =
        new StringBuffer(
          "The template validation has failed because of invalid directive(s)."
            + "\nBatchruntomo error message:");
      for (int i = 0; i < messages.size(ProcessMessages.MessageType.ERROR); i++) {
        errorMessage.append("\n" + messages.get(ProcessMessages.MessageType.ERROR, i));
      }
      UIHarness.INSTANCE.openMessageDialog(appManager, errorMessage.toString(),
        "Template Validation Error", axisID);
    }
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.WARNING); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.WARNING, i),
        "Template Validation Warning", axisID);
    }
    if (exitValue != 0) {
      UIHarness.INSTANCE.openMessageDialog(appManager, param.getStdErrorString(),
        "Template Validation Error", axisID);
      return false;
    }
    return !err;
  }

  public boolean makecomfile(final AxisID axisID, final MakecomfileParam param) {
    if (!param.setup()) {
      return false;
    }
    int exitValue = param.run();
    // process messages
    ProcessMessages messages = param.getProcessMessages();
    boolean err = !messages.isEmpty(ProcessMessages.MessageType.ERROR);
    if (err) {
      StringBuffer errorMessage = new StringBuffer("Error running Makecomfile");
      for (int i = 0; i < messages.size(ProcessMessages.MessageType.ERROR); i++) {
        errorMessage.append("\n" + messages.get(ProcessMessages.MessageType.ERROR, i));
      }
      UIHarness.INSTANCE.openMessageDialog(appManager, errorMessage.toString(),
        "Makecomfile Error", axisID);
    }
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.WARNING); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.WARNING, i), "Makecomfile Warning",
        axisID);
    }
    if (exitValue != 0) {
      UIHarness.INSTANCE.openMessageDialog(appManager, param.getStdErrorString(),
        "Makecomfile Error", axisID);
      return false;
    }
    return err;
  }

  /**
   * Erase the specified pixels
   * 
   * @param axisID
   *          the AxisID to erase.
   */
  public String eraser(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries, Command ccdEraserParam)
    throws SystemProcessException {
    // Create the process monitor
    CCDEraserProcessMonitor ccdEraserProcessMonitor =
      new CCDEraserProcessMonitor(appManager, axisID);

    // Create the required command string
    String command = "eraser" + axisID.getExtension() + ".com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, ccdEraserProcessMonitor, axisID, processResultDisplay,
        ccdEraserParam, processSeries);

    return comScriptProcess.getName();
  }

  public String goldEraser(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries, Command ccdEraserParam)
    throws SystemProcessException {
    // Create the process monitor
    CCDEraserProcessMonitor ccdEraserProcessMonitor =
      new CCDEraserProcessMonitor(appManager, axisID);

    // Create the required command string
    String command = "golderaser" + axisID.getExtension() + ".com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, ccdEraserProcessMonitor, axisID, processResultDisplay,
        ccdEraserParam, processSeries);

    return comScriptProcess.getName();
  }

  /**
   * Run clip stats
   */
  public String clipStats(ClipParam param, AxisID axisID,
    final ProcessSeries processSeries) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param, axisID, ProcessName.CLIP, processSeries);
    return backgroundProcess.getName();
  }

  private String getDatasetName() {
    return appManager.getMetaData().getDatasetName();
  }

  /**
   * Calculate the cross-correlation for the specified axis
   * 
   * @param axisID
   *          the AxisID to cross-correlate.
   */
  public String crossCorrelate(BlendmontParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the process monitor
    XcorrProcessWatcher xcorrProcessWatcher =
      new XcorrProcessWatcher(appManager, axisID, true);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, xcorrProcessWatcher, axisID, processResultDisplay,
        processSeries);
    return comScriptProcess.getName();
  }

  public String tiltxcorr(ConstTiltxcorrParam param, FileType comscriptfileType,
    AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries, final boolean runTiltxcorr,
    final boolean breakContours) throws SystemProcessException {
    // Create the required command string
    // Create the process monitor
    TiltxcorrProcessWatcher tiltxcorrProcessWatcher =
      new TiltxcorrProcessWatcher(appManager, axisID, comscriptfileType, runTiltxcorr,
        breakContours);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, tiltxcorrProcessWatcher, axisID, processResultDisplay,
        processSeries);
    return comScriptProcess.getName();
  }

  public String autofidseed(AutofidseedParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, null, axisID, processResultDisplay, processSeries);
    return comScriptProcess.getName();
  }

  public String makeDistortionCorrectedStack(AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required tiltalign command
    ProcessName processName =
      BlendmontParam.getProcessName(BlendmontParam.Mode.UNDISTORT);
    String command = processName.getComscript(axisID);
    // Start the com script in the background
    BlendmontProcessMonitor blendmontProcessMonitor =
      new BlendmontProcessMonitor(appManager, axisID, BlendmontParam.Mode.UNDISTORT);

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, blendmontProcessMonitor, axisID, processResultDisplay,
        processSeries, processName.resumable);
    return comScriptProcess.getName();
  }

  /**
   * Calculate the coarse alignment for the specified axis
   * 
   * @param axisID
   *          the identifyer of the axis to coarse align.
   */
  public String coarseAlign(NewstParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {

    // Create the required tiltalign command
    String command = "prenewst" + axisID.getExtension() + ".com";
    // Start the com script in the background
    PrenewstProcessMonitor prenewstProcessMonitor =
      new PrenewstProcessMonitor(appManager, axisID);

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, prenewstProcessMonitor, axisID, processResultDisplay,
        param, processSeries);
    return comScriptProcess.getName();
  }

  /**
   * run preblend comscript
   * @param axisID
   * @return
   * @throws SystemProcessException
   */
  public String preblend(BlendmontParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Start the com script in the background
    BlendmontProcessMonitor blendmontProcessMonitor =
      new BlendmontProcessMonitor(appManager, axisID, BlendmontParam.Mode.PREBLEND);

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, blendmontProcessMonitor, axisID, processResultDisplay,
        processSeries);
    return comScriptProcess.getName();
  }

  /**
   * run blend comscript
   * @param axisID
   * @return
   * @throws SystemProcessException
   */
  public String blend(BlendmontParam blendmontParam, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Start the com script in the background
    BlendmontProcessMonitor blendmontProcessMonitor =
      new BlendmontProcessMonitor(appManager, axisID, blendmontParam.getMode());

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(blendmontParam, blendmontProcessMonitor, axisID,
        processResultDisplay, processSeries);
    return comScriptProcess.getName();
  }

  /**
   * Generate the XG transform file for  
   * @param axisID
   * @throws SystemProcessException
   */
  public void generatePreXG(AxisID axisID) throws SystemProcessException,
    LogFile.LockException {
    String[] xftoxg = new String[5];
    xftoxg[0] = ApplicationManager.getIMODBinPath() + "xftoxg";
    xftoxg[1] = "-NumberToFit";
    xftoxg[2] = "0";
    xftoxg[3] = getDatasetName() + axisID.getExtension() + ".prexf";
    xftoxg[4] = getDatasetName() + axisID.getExtension() + ".prexg";
    runCommand(xftoxg, axisID, null);
  }

  /**
   * Run both xftoxg and xproduct to create the _nonfid.xf for specified axis.
   * 
   * @param axisID
   */
  public void generateNonFidXF(AxisID axisID) throws SystemProcessException,
    LogFile.LockException {
    String[] xfproduct = new String[4];
    xfproduct[0] = ApplicationManager.getIMODBinPath() + "xfproduct";
    xfproduct[1] = getDatasetName() + axisID.getExtension() + ".prexg";
    xfproduct[2] = "rotation" + axisID.getExtension() + ".xf";
    xfproduct[3] = getDatasetName() + axisID.getExtension() + "_nonfid.xf";

    runCommand(xfproduct, axisID, null);
  }

  /**
   * Setup the non fiducial alignment files
   * Copy the _nonfid.xf to .xf file
   * Copy the .rawtlt to the .tlt file
   * @param axisID
   */
  public void setupNonFiducialAlign(AxisID axisID) throws IOException,
    InvalidParameterException {
    String workingDirectory = appManager.getPropertyUserDir();
    String axisDataset = getDatasetName() + axisID.getExtension();

    File nonfidXF = new File(workingDirectory, axisDataset + "_nonfid.xf");
    File xf = new File(workingDirectory, axisDataset + ".xf");
    Utilities.copyFile(nonfidXF, xf);

    File rawtlt = new File(workingDirectory, axisDataset + ".rawtlt");
    File tlt = new File(workingDirectory, axisDataset + ".tlt");
    if (!rawtlt.exists()) {
      appManager.makeRawtltFile(axisID);
    }
    Utilities.copyFile(rawtlt, tlt);
  }

  /**
   * Setup the fiducial alignment files
   * If they exist copy the _fid.xf to .xf
   * Copy _fid.tlt to .tlt
   * @param axisID
   */
  public boolean setupFiducialAlign(AxisID axisID,
    final ProcessResultDisplay processResultDisplay) throws IOException {
    if (appManager.isAxisBusy(axisID, processResultDisplay)) {
      return false;
    }
    String workingDirectory = appManager.getPropertyUserDir();
    String axisDataset = getDatasetName() + axisID.getExtension();
    // Files to be managed
    File xf = new File(workingDirectory, axisDataset + ".xf");
    File fidXF = new File(workingDirectory, axisDataset + "_fid.xf");
    File nonfidXF = new File(workingDirectory, axisDataset + "_nonfid.xf");
    File tlt = new File(workingDirectory, axisDataset + ".tlt");
    File fidTlt = new File(workingDirectory, axisDataset + "_fid.tlt");
    File tltxf = new File(workingDirectory, axisDataset + ".tltxf");
    if (tltxf.exists()) {
      // Align{|a|b}.com shows evidence of being run
      if (Utilities.fileExists(appManager, "_fid.xf", axisID)) {
        // A recent align.com (or equivalent) has created the _fid.xf and
        // _fid.tlt (protected) transform and tilt files
        Utilities.copyFile(fidXF, xf);
        Utilities.copyFile(fidTlt, tlt);
      }
      else {
        // An older align.com that just wrote out an .xf and .tlt was run
        // if the nonfid.xf was run it overwrote the the original data
        // delete the xf and tlt so that an error occurs
        if (nonfidXF.exists()) {
          xf.delete();
          tlt.delete();
        }
        else {
          // No nonfid{|a|b}.xf exists so the .xf and .tlt came from align
          // create the protected copies
          Utilities.copyFile(xf, fidXF);
          Utilities.copyFile(tlt, fidTlt);
        }
      }
    }
    else {
      // Align has not been run, delete any .xf and .tlt file so that they
      // are not accidentally used
      xf.delete();
      tlt.delete();
    }
    return true;
  }

  /**
   * Run midas on the specified raw stack
   * 
   * @param axisID
   *          the AxisID to run midas on.
   */
  public void midasRawStack(AxisID axisID, double imageRotation) {

    // Construct the command line strings
    String[] commandArray = new String[3];

    String stack =
      getDatasetName() + axisID.getExtension() + DatasetTool.STANDARD_DATASET_EXT;
    String xform = getDatasetName() + axisID.getExtension() + ".prexf";

    String[] command =
      new String[] { ApplicationManager.getIMODBinPath() + "midas", "-a",
        String.valueOf(-1 * imageRotation), "-t",
        DatasetFiles.getRawTiltName(appManager, axisID), stack, xform };

    // Start the system program thread
    startSystemProgramThread(command, axisID, appManager);
  }

  public void midasBlendStack(AxisID axisID, double imageRotation) {

    // Construct the command line strings
    String[] commandArray = new String[3];

    // String options = "-a " + String.valueOf(-1 * imageRotation) + " ";
    String stack = getDatasetName() + axisID.getExtension() + ".bl";
    String xform = getDatasetName() + axisID.getExtension() + ".prexf";

    String[] command =
      new String[] { ApplicationManager.getIMODBinPath() + "midas", "-a",
        String.valueOf(-1 * imageRotation), "-t",
        DatasetFiles.getRawTiltName(appManager, axisID), stack, xform };

    // Start the system program thread
    startSystemProgramThread(command, axisID, appManager);
  }

  public void midasFixEdges(AxisID axisID) {

    // Construct the command line strings
    String[] commandArray = new String[3];
    String stackExtension;
    if (appManager.getMetaData().isDistortionCorrection()) {
      stackExtension = BlendmontParam.DISTORTION_CORRECTED_STACK_EXTENSION;
    }
    else {
      stackExtension = DatasetTool.STANDARD_DATASET_EXT;
    }
    // String options = "-p " + getDatasetName() + axisID.getExtension() + ".pl "
    // + "-b 0 -q ";
    String stack = getDatasetName() + axisID.getExtension() + stackExtension;
    String xform = getDatasetName() + axisID.getExtension() + ".ecd";

    String[] command =
      new String[] { ApplicationManager.getIMODBinPath() + "midas", "-p ",
        getDatasetName() + axisID.getExtension() + ".pl", "-b", "0", "-q", stack, xform };

    // Start the system program thread
    startSystemProgramThread(command, axisID, appManager);
  }

  /**
   * Run the appropriate track com file for the given axis ID
   * 
   * @param axisID
   *          the AxisID to run track.com on.
   */
  public String fiducialModelTrack(BeadtrackParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, null, axisID, processResultDisplay, processSeries);
    return comScriptProcess.getName();
  }

  /**
   * Run the appropriate align com file for the given axis ID
   * 
   * @param axisID
   *          the AxisID to run align.com on.
   */
  public String fineAlignment(ConstTiltalignParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    //
    // Create the required tiltalign command
    //
    String command = "align" + axisID.getExtension() + ".com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, null, axisID, processResultDisplay, processSeries);
    return comScriptProcess.getName();

  }

  /**
   * Generate the split align log file for the given axis ID
   * 
   * @param axisID
   */
  public void generateAlignLogs(AxisID axisID) {
    AlignLogGenerator alignLogGenerator = new AlignLogGenerator(appManager, axisID);

    try {
      alignLogGenerator.run();
    }
    catch (IOException except) {
      uiHarness.openMessageDialog(appManager, "Unable to create alignlog files",
        "Alignlog Error", axisID);
    }
  }

  /**
   * Copy the fiducial align files to the new protected names.  This is a
   * redundancy to handle existing com scripts, this functionality is also
   * present at the end of new align com scripts. 
   * @param axisID
   */
  public void copyFiducialAlignFiles(AxisID axisID) {
    String workingDirectory = appManager.getPropertyUserDir();
    String axisDataset = getDatasetName() + axisID.getExtension();

    try {
      if (Utilities.fileExists(appManager, ".xf", axisID)) {
        File xf = new File(workingDirectory, axisDataset + ".xf");
        File fidXF = new File(workingDirectory, axisDataset + "_fid.xf");
        Utilities.copyFile(xf, fidXF);
      }
      if (Utilities.fileExists(appManager, ".tlt", axisID)) {
        File tlt = new File(workingDirectory, axisDataset + ".tlt");
        File fidTlt = new File(workingDirectory, axisDataset + "_fid.tlt");
        Utilities.copyFile(tlt, fidTlt);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      uiHarness.openMessageDialog(appManager, "Unable to copy protected align files:",
        "Align Error", axisID);
    }

  }

  /**
   * Run the transferfid script
   */
  public String transferFiducials(TransferfidParam transferfidParam,
    ProcessResultDisplay processResultDisplay, ProcessSeries processSeries)
    throws SystemProcessException {
    AxisID axisID = AxisID.SECOND;
    // Run transferfid on the destination axis.
    if (transferfidParam.getBToA().is()) {
      axisID = AxisID.FIRST;
    }
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(transferfidParam.getCommand(), axisID, processResultDisplay,
        ProcessName.TRANSFERFID, processSeries);
    transferfidCommandLine = backgroundProcess.getCommandLine();
    return backgroundProcess.getName();
  }

  /**
   * Run the appropriate sample com file for the given axis ID
   * 
   * @param axisID
   *          the AxisID to run sample.com on.
   */
  public String createSample(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries, ConstTiltParam param)
    throws SystemProcessException {
    //
    // Create the required sample command
    //
    String command = ProcessName.SAMPLE + axisID.getExtension() + ".com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, null, axisID, processResultDisplay, param, processSeries);
    return comScriptProcess.getName();

  }

  /**
   * Run the appropriate tomopitch com file for the given axis ID
   * 
   * @param axisID
   *          the AxisID to run tomoptich on.
   */
  public String tomopitch(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries) throws SystemProcessException {
    //
    // Create the required tiltalign command
    //
    String command = "tomopitch" + axisID.getExtension() + ".com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, null, axisID, processResultDisplay, processSeries,
        ProcessName.TOMOPITCH.resumable);
    return comScriptProcess.getName();

  }

  /**
   * Run the appropriate newst com (or newst_3dfind.com) file for the given axis ID
   * 
   * @param axisID
   *          the AxisID to run newst on.
   */
  public String newst(ConstNewstParam newstParam, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries,
    ProcessName processName) throws SystemProcessException {
    // Start the com script in the background
    NewstProcessMonitor newstProcessMonitor =
      new NewstProcessMonitor(appManager, axisID, processName, newstParam);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(newstParam, newstProcessMonitor, axisID, processResultDisplay,
        processSeries);
    return comScriptProcess.getName();
  }

  public String findBeads3d(ConstFindBeads3dParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, null, axisID, processResultDisplay, processSeries);
    return comScriptProcess.getName();
  }

  /**
   * Run the appropriate mtffilter com file for the given axis ID
   * 
   * @param axisID
   *          the AxisID to run newst on.
   */
  public String mtffilter(ConstMTFFilterParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    String command = "mtffilter" + axisID.getExtension() + ".com";
    MtffilterProcessMonitor mtffilterProcessMonitor =
      new MtffilterProcessMonitor(appManager, axisID);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, mtffilterProcessMonitor, axisID, processResultDisplay,
        param, processSeries);
    return comScriptProcess.getName();
  }

  public void ctfPlotter(AxisID axisID, ProcessResultDisplay processResultDisplay)
    throws SystemProcessException {
    String command = ProcessName.CTF_PLOTTER.getComscript(axisID);
    // Start the com script in the background
    startNonBlockingComScript(command, axisID, processResultDisplay);
  }

  public String ctfCorrection(ConstCtfPhaseFlipParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    String command = ProcessName.CTF_CORRECTION.getComscript(axisID);
    CtfCorrectionMonitor monitor = new CtfCorrectionMonitor(appManager, axisID);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, monitor, axisID, processResultDisplay, param, processSeries);
    return comScriptProcess.getName();
  }

  public boolean reconnectTilt(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries) {
    try {
      TiltProcessMonitor monitor =
        TiltProcessMonitor.getReconnectInstance(appManager, axisID);
      ReconnectProcess process =
        ReconnectProcess.getInstance(appManager, this, monitor,
          axisProcessData.getSavedProcessData(axisID), axisID, processSeries);
      process.setProcessResultDisplay(processResultDisplay);
      Thread thread = new Thread(process);
      thread.start();
      axisProcessData.mapAxisThread(process, axisID);
      axisProcessData.mapAxisProcessMonitor(null, monitor, axisID);
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      UIHarness.INSTANCE.openMessageDialog(appManager,
        "Unable to reconnect to processchunks.\n" + e.getMessage(), "Reconnect Failure",
        axisID);
      return false;
    }
    return true;
  }

  public String sirtsetup(final AxisID axisID,
    final ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries,
    final SirtsetupParam param, final ProcessingMethod processingMethod)
    throws SystemProcessException {
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, null, axisID, processResultDisplay, processSeries,
        processingMethod);
    return comScriptProcess.getName();
  }

  /**
   * Run the appropriate tilt com file for the given axis ID
   * Use this for sampling a whole tomogram
   * @param axisID
   *          the AxisID to run tilt on.
   */
  public String tilt(final AxisID axisID,
    final ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries,
    final ConstTiltParam param, final String processTitle,
    final ProcessingMethod processingMethod) throws SystemProcessException {
    // Instantiate the process monitor
    TiltProcessMonitor tiltProcessMonitor =
      new TiltProcessMonitor(appManager, axisID, ProcessName.TILT);
    tiltProcessMonitor.setProcessTitle(processTitle);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, tiltProcessMonitor, axisID, processResultDisplay,
        processSeries, processingMethod);

    return comScriptProcess.getName();
  }

  /**
   * Run the appropriate tilt_3dfind com file for the given axis ID
   * @param axisID
   *          the AxisID to run tilt on.
   */
  public String tilt3dFind(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries, ConstTiltParam param, String processTitle,
    ProcessName processName, final ProcessingMethod processingMethod)
    throws SystemProcessException {
    // Instantiate the process monitor
    TiltProcessMonitor monitor =
      new Tilt3dFindProcessMonitor(appManager, axisID, processName, param);
    monitor.setProcessTitle(processTitle);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, monitor, axisID, processResultDisplay, processSeries,
        processingMethod);

    return comScriptProcess.getName();
  }

  /**
   * Run the appropriate tilt_3dfind com file in reproject mode for the given axis ID
   * @param axisID
   *          the AxisID to run tilt on.
   */
  public String tilt3dFindReproject(AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries,
    ConstTiltParam param, String processTitle, ProcessName processName)
    throws SystemProcessException {
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(param, null, axisID, processResultDisplay, processSeries);

    return comScriptProcess.getName();
  }

  /**
   * Run splittilt
   */
  public String splittilt(SplittiltParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param.getCommand(), axisID, processResultDisplay,
        ProcessName.SPLITTILT, processSeries);
    return backgroundProcess.getName();
  }

  public String splitCorrection(ConstSplitCorrectionParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param.getCommand(), axisID, processResultDisplay,
        ProcessName.SPLIT_CORRECTION, processSeries);
    return backgroundProcess.getName();
  }

  /**
   * Run extracttilts
   */
  public String extracttilts(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(new ExtracttiltsParam(appManager, axisID).getCommand(),
        axisID, true, processResultDisplay, processSeries, ProcessName.EXTRACTTILTS);
    return backgroundProcess.getName();
  }

  /**
   * Run extractpieces
   */
  public String extractpieces(AxisID axisID, ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(new ExtractpiecesParam(appManager, axisID).getCommand(),
        axisID, true, processResultDisplay, processSeries, ProcessName.EXTRACTPIECES);
    return backgroundProcess.getName();
  }

  /**
   * Run extractmagrad
   */
  public String extractmagrad(ExtractmagradParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param.getCommand(), axisID, true, processResultDisplay,
        processSeries, ProcessName.EXTRACTMAGRAD);
    return backgroundProcess.getName();
  }

  /**
   * Run splitcombine
   */
  public String splitcombine(ProcessResultDisplay processResultDisplay,
    final ProcessSeries processSeries) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(new SplitcombineParam().getCommand(), AxisID.ONLY,
        processResultDisplay, ProcessName.SPLITCOMBINE, processSeries);
    return backgroundProcess.getName();
  }

  /**
   * Execute the setupcombine script
   * 
   * @param combineParam
   *          A read-only object containing the parameters for setupcombine
   *          script
   */
  public boolean setupCombineScripts(ProcessResultDisplay processResultDisplay)
    throws IOException {
    SetupCombine setupCombine;
    try {
      setupCombine = SetupCombine.getInstance(appManager);
    }
    catch (SystemProcessException e) {
      uiHarness.openMessageDialog(appManager, e.getMessage(), "Setup Combine Error",
        AxisID.ONLY);
      return false;
    }
    appManager.saveStorables(AxisID.ONLY);
    int exitValue = setupCombine.run();
    ProcessMessages messages = setupCombine.getProcessMessages();
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.ERROR); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.ERROR, i), "Setup Combine Error",
        AxisID.ONLY);
    }
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.WARNING); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.WARNING, i), "Setup Combine Warning",
        AxisID.ONLY);
    }
    TomogramState state = appManager.getState();
    if (exitValue != 0) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        "Setup combine failed.  Exit value = " + exitValue, "Setup Combine Failed",
        AxisID.ONLY);
      if (processResultDisplay != null) {
        processResultDisplay.msgProcessFailed();
      }
      state.setCombineMatchMode(null);
      state.setCombineScriptsCreated(false);
      return false;
    }
    if (processResultDisplay != null) {
      processResultDisplay.msgProcessSucceeded();
    }
    state.setCombineMatchMode(setupCombine.getMatchMode());
    state.setCombineScriptsCreated(true);
    return true;
  }

  public boolean setupCombineOnlyMakeCombineCom() throws IOException {
    SetupCombine setupCombine;
    try {
      setupCombine = SetupCombine.getOnlyMakeCombineComInstance(appManager);
    }
    catch (SystemProcessException e) {
      uiHarness.openMessageDialog(appManager, e.getMessage(), "Setup Combine Error",
        AxisID.ONLY);
      return false;
    }
    int exitValue = setupCombine.run();
    ProcessMessages messages = setupCombine.getProcessMessages();
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.ERROR); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.ERROR, i), "Setup Combine Error",
        AxisID.ONLY);
    }
    for (int i = 0; i < messages.size(ProcessMessages.MessageType.WARNING); i++) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        messages.get(ProcessMessages.MessageType.WARNING, i), "Setup Combine Warning",
        AxisID.ONLY);
    }
    if (exitValue != 0) {
      UIHarness.INSTANCE.openMessageDialog(appManager,
        "Setup combine failed.  Copy combine.com from $IMOD_DIR/com.  Exit value = "
          + exitValue, "Setup Combine Failed", AxisID.ONLY);
      return false;
    }
    return true;
  }

  /**
   * Run the imod2patch command, don't save meta data because it doesn't change
   * for this command
   */
  public void modelToPatch(AxisID axisID) throws SystemProcessException,
    LogFile.LockException {
    LogFile patchOut =
      LogFile.getInstance(appManager.getPropertyUserDir(), DatasetFiles.PATCH_OUT);
    patchOut.backup();
    /* File patchOut = new File(appManager.getPropertyUserDir(), "patch.out"); if
     * (patchOut.exists()) { // Copy the old patch.out to patch.out~ String[] mv = { "mv",
     * "-f", "patch.out", "patch.out~" }; runCommand(mv, axisID); } */
    // Convert the new patchvector.mod
    String command = "imod2patch";
    String[] imod2patch =
      { command, DatasetFiles.PATCH_VECTOR_MODEL, DatasetFiles.PATCH_OUT };
    runCommand(imod2patch, axisID, patchOut);
  }

  /**
   * Run the combine com file
   * 
   * @param axisID
   *          the AxisID to run tilt on.
   */
  public String combine(CombineComscriptState combineComscriptState,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required combine command
    String comscript = CombineComscriptState.COMSCRIPT_NAME + ".com";

    CombineProcessMonitor combineProcessMonitor =
      new CombineProcessMonitor(appManager, AxisID.ONLY, combineComscriptState,
        processResultDisplay);

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startBackgroundComScript(comscript, combineProcessMonitor, AxisID.ONLY,
        combineComscriptState, CombineComscriptState.COMSCRIPT_WATCHED_FILE,
        processSeries, ProcessName.COMBINE.resumable);
    return comScriptProcess.getName();
  }

  /**
   * Run the solvematch com file
   * 
   * @return String
   */
  private String solvematch(final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required solvematch command
    String command = "solvematch.com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, null, AxisID.ONLY, processSeries,
        ProcessName.SOLVEMATCH.resumable);
    return comScriptProcess.getName();

  }

  /**
   * Run the matchvol1 com file
   * 
   * @return String
   */
  private String matchvol1(final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required matchvol1 command
    String command = "matchvol1.com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, null, AxisID.ONLY, processSeries,
        ProcessName.MATCHVOL1.resumable);
    return comScriptProcess.getName();

  }

  /**
   * Run the patchcorr com file
   * 
   * @return String
   */
  public String patchcorr(final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required patchcorr command
    String command = "patchcorr.com";
    // Create the process monitor
    PatchcorrProcessWatcher patchcorrProcessWatcher =
      new PatchcorrProcessWatcher(appManager, AxisID.FIRST);

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, patchcorrProcessWatcher, AxisID.ONLY, "patch.out",
        processSeries, ProcessName.PATCHCORR.resumable);
    return comScriptProcess.getName();

  }

  /**
   * Run the matchorwarp com file
   * 
   * @return String
   */
  public String matchorwarp(final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required matchorwarp command
    String command = "matchorwarp.com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, null, AxisID.ONLY, processSeries,
        ProcessName.MATCHORWARP.resumable);
    return comScriptProcess.getName();

  }

  /**
   * Run the volcombine com file
   * 
   * @return String
   */
  public String volcombine(final ProcessSeries processSeries)
    throws SystemProcessException {
    VolcombineProcessMonitor volcombineProcessMonitor =
      new VolcombineProcessMonitor(appManager, AxisID.ONLY);
    // Create the required volcombine command
    String command = "volcombine.com";

    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, volcombineProcessMonitor, AxisID.ONLY, processSeries,
        ProcessName.VOLCOMBINE.resumable);
    return comScriptProcess.getName();

  }

  /**
   * Run trimvol
   */
  public String trimVolume(TrimvolParam trimvolParam,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(trimvolParam, AxisID.ONLY, processResultDisplay,
        ProcessName.TRIMVOL, processSeries);
    return backgroundProcess.getName();
  }

  /**
   * Run archiveorig
   */
  public String archiveOrig(ArchiveorigParam param, final ProcessSeries processSeries)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param, AxisID.ONLY, true, ProcessName.ARCHIVEORIG,
        processSeries);
    return backgroundProcess.getName();
  }

  public String restrictalign(final RestrictalignParam param, final AxisID axisID,
    final ProcessSeries processSeries) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param, axisID, false, ProcessName.RESTRICTALIGN,
        processSeries);
    return backgroundProcess.getName();
  }

  /**
   * Run the appropriate flatten com file for the given axis ID
   */
  public String flatten(WarpVolParam param, AxisID axisID,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    // Create the required tilt command
    String command = ProcessName.FLATTEN.toString() + axisID.getExtension() + ".com";
    // Instantiate the process monitor
    Matchvol1ProcessMonitor monitor =
      Matchvol1ProcessMonitor.getFlattenInstance(appManager, axisID, null);
    // Start the com script in the background
    ComScriptProcess comScriptProcess =
      startComScript(command, monitor, axisID, processResultDisplay, param, processSeries);
    return comScriptProcess.getName();
  }

  public String flattenWarp(FlattenWarpParam param,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries,
    AxisID axisID) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param.getCommandArray(), axisID, processResultDisplay,
        param.getProcessName(), processSeries);
    return backgroundProcess.getName();
  }

  public String runraptor(RunraptorParam param,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries,
    AxisID axisID) throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(param.getCommandArray(), axisID, processResultDisplay,
        param.getProcessName(), processSeries);
    return backgroundProcess.getName();
  }

  /**
   * Run squeezevol
   */
  public String squeezeVolume(ConstSqueezevolParam squeezevolParam,
    ProcessResultDisplay processResultDisplay, final ProcessSeries processSeries)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess =
      startBackgroundProcess(squeezevolParam, AxisID.ONLY, processResultDisplay,
        ProcessName.SQUEEZEVOL, processSeries);
    return backgroundProcess.getName();
  }

  /**
   * Puts a log file into a window and displays it.
   * @param logFile
   */
  private void showLogFile(File logFile) {
    // Show a log file window to the user
    TextPageWindow logFileWindow = new TextPageWindow();
    logFileWindow.setVisible(logFileWindow.setFile(logFile));
  }

  /**
   * Execute the command and arguments in commandAarray immediately.
   * Add save meta data if meta data is changed for this function is called
   * @param commandArray
   * @throws SystemProcessException
   */
  private void runCommand(String[] commandArray, AxisID axisID, LogFile logFile)
    throws SystemProcessException, LogFile.LockException {
    SystemProgram systemProgram =
      new SystemProgram(appManager, appManager.getPropertyUserDir(), commandArray, axisID);
    systemProgram.setWorkingDirectory(new File(appManager.getPropertyUserDir()));
    LogFile.WritingId logWritingId = null;
    if (logFile != null) {
      logWritingId = logFile.openForWriting();
    }
    systemProgram.run();
    if (logFile != null) {
      logFile.closeForWriting(logWritingId);
    }
    if (systemProgram.getExitValue() != 0) {
      String message = "";
      // Copy any stderr output to the message
      String[] stderr = systemProgram.getStdError();
      if (stderr != null) {
        for (int i = 0; i < stderr.length; i++) {
          message = message + stderr[i] + "\n";
        }
      }

      // Also scan stdout for ERROR: lines
      String[] stdOutput = systemProgram.getStdOutput();
      boolean foundError = false;
      if (stdOutput != null) {
        for (int i = 0; i < stdOutput.length; i++) {
          if (!foundError) {
            int index = stdOutput[i].indexOf("ERROR:");
            if (index != -1) {
              foundError = true;
              message = message + stdOutput[i];
            }
          }
          else {
            message = message + stdOutput[i];
          }
        }
      }
      throw new SystemProcessException(message);
    }
  }

  void postProcess(final DetachedProcess process) {
    super.postProcess(process);
    try {
      if (process.getCommand().getCommandName()
        .equals(ProcessName.PROCESSCHUNKS.toString())) {
        CommandDetails commandDetails = process.getCommandDetails();
        if (commandDetails != null) {
          postProcess(commandDetails.getSubcommandProcessName(), process.getAxisID());
        }
      }
    }
    catch (NullPointerException e) {}
  }

  /**
   * Post process for processes that may or may not be done with processchunks.
   * @param processName
   * @param axisID
   */
  private void postProcess(String processName, AxisID axisID) {
    if (processName == null) {
      return;
    }
    if (ProcessName.TILT_3D_FIND.equals(processName, axisID)) {
      appManager.copyTilt3dFindReprojectCom(axisID);
    }
    else if (ProcessName.CTF_CORRECTION.equals(processName, axisID)) {
      appManager.getState().setUseCtfCorrectionWarning(axisID, true);
    }
  }

  void postProcess(ComScriptProcess script) {
    try {
      // Script specific post processing
      ProcessName processName = script.getProcessName();
      ProcessDetails processDetails = script.getProcessDetails();
      CommandDetails commandDetails = script.getCommandDetails();
      Command command = script.getCommand();
      TomogramState state = appManager.getState();
      AxisID axisID = script.getAxisID();
      if (processName == ProcessName.ALIGN) {
        generateAlignLogs(axisID);
        copyFiducialAlignFiles(axisID);
        state.setMadeZFactors(axisID,
          processDetails.getBooleanValue(TiltalignParam.Fields.USE_OUTPUT_Z_FACTOR_FILE));
        state.setUsedLocalAlignments(axisID,
          processDetails.getBooleanValue(TiltalignParam.Fields.LOCAL_ALIGNMENTS));
        appManager.setTiltState(script.getAxisID());
        state.setAlignAxisZShift(axisID,
          processDetails.getDoubleValue(TiltalignParam.Fields.AXIS_Z_SHIFT));
        state.setAlignAngleOffset(axisID,
          processDetails.getDoubleValue(TiltalignParam.Fields.ANGLE_OFFSET));
        appManager.postProcess(axisID, processName, processDetails,
          script.getProcessResultDisplay());
        appManager.logTiltAlignLogMessage(axisID);
      }
      else if (processName == ProcessName.ERASER) {
        if (command != null && command.getCommandMode() == CCDEraserParam.Mode.X_RAYS) {
          state.setUseFixedStackWarning(axisID, true);
        }
      }
      else if (processName == ProcessName.MTFFILTER) {
        state.setUseFilteredStackWarning(axisID, true);
      }
      else if (processName == ProcessName.CTF_CORRECTION) {
        state.setUseCtfCorrectionWarning(axisID, true);
      }
      else if (processName == ProcessName.TOMOPITCH) {
        appManager.setTomopitchOutput(axisID);
      }
      else if (processName == ProcessName.NEWST) {
        if (commandDetails.getCommandMode() == NewstParam.Mode.FULL_ALIGNED_STACK) {
          state.setNewstFiducialessAlignment(axisID,
            commandDetails.getBooleanValue(NewstParam.Field.FIDUCIALESS_ALIGNMENT));
          appManager.setTiltState(axisID);
          state.setStackUseLinearInterpolation(axisID,
            commandDetails.getBooleanValue(NewstParam.Field.USE_LINEAR_INTERPOLATION));
          state.setStackUserSizeToOutputInXandY(axisID,
            commandDetails.getString(NewstParam.Field.USER_SIZE_TO_OUTPUT_IN_X_AND_Y));
          state.setStackImageRotation(axisID,
            commandDetails.getEtomoNumber(NewstParam.Field.IMAGE_ROTATION));
        }
      }
      else if (processName == ProcessName.BLEND) {
        if (commandDetails.getCommandMode() == BlendmontParam.Mode.BLEND) {
          state.setStackUseLinearInterpolation(axisID,
            commandDetails.getBooleanValue(BlendmontParam.Field.LINEAR_INTERPOLATION));
          state
            .setStackUserSizeToOutputInXandY(axisID, commandDetails
              .getString(BlendmontParam.Field.USER_SIZE_TO_OUTPUT_IN_X_AND_Y));
          state.setStackImageRotation(axisID,
            commandDetails.getEtomoNumber(BlendmontParam.Field.IMAGE_ROTATION));
          state.setNewstFiducialessAlignment(axisID,
            commandDetails.getBooleanValue(BlendmontParam.Field.FIDUCIALESS));
        }
      }
      else if (processName == ProcessName.UNDISTORT) {
        appManager.setEnabledFixEdgesWithMidas(script.getAxisID());
      }
      else if (processName == ProcessName.XCORR) {
        setInvalidEdgeFunctions(script.getCommand(), true);
        if (commandDetails != null
          && commandDetails.getCommandMode() == BlendmontParam.Mode.XCORR) {
          state.setXcorrBlendmontWasRun(axisID, true);
        }
      }
      else if (processName == ProcessName.PREBLEND) {
        setInvalidEdgeFunctions(script.getCommand(), true);
      }
      else if (processName == ProcessName.SAMPLE) {
        appManager.tomogramPositioningPostProcess(script.getAxisID(), processDetails);
      }
      else if (processName == ProcessName.TILT) {
        if (commandDetails.getCommandMode() == TiltParam.Mode.WHOLE) {
          appManager.tomogramPositioningPostProcess(script.getAxisID(), processDetails);
        }
        if (commandDetails.getCommandMode() != TiltParam.Mode.SAMPLE) {
          state.setAdjustOrigin(script.getAxisID(),
            processDetails.getBooleanValue(TiltParam.Field.ADJUST_ORIGIN));
        }
      }
      else if (processName == ProcessName.NEWST_3D_FIND) {
        state.setStackUsingNewstOrBlend3dFindOutput(axisID, true);
      }
      else if (processName == ProcessName.BLEND_3D_FIND) {
        state.setStackUsingNewstOrBlend3dFindOutput(axisID, true);
      }
      else if (processName == ProcessName.GOLD_ERASER) {
        appManager.getState().setUseErasedStackWarning(script.getAxisID(), true);
      }
      else if (processName == ProcessName.TRACK) {
        File fiducialFile = DatasetFiles.getFiducialModelFile(appManager, axisID);
        if (fiducialFile.exists()) {
          state.setFidFileLastModified(axisID, fiducialFile.lastModified());
          appManager.logMessage(
            TrackLog.getInstance(appManager.getPropertyUserDir(), axisID), axisID);
        }
        else {
          state.resetFidFileLastModified(axisID);
        }
        state.setTrackLightBeads(axisID,
          commandDetails.getBooleanValue(BeadtrackParam.Field.LIGHT_BEADS));
      }
      else if (processName == ProcessName.SIRTSETUP) {
        if (processDetails.getBooleanValue(SirtsetupParam.Field.SUBAREA)) {
          state.setGenSirtsetupSubareaSize(script.getAxisID(),
            processDetails.getString(SirtsetupParam.Field.SUBAREA_SIZE));
          state.setGenSirtsetupyOffsetOfSubarea(script.getAxisID(),
            processDetails.getIntValue(SirtsetupParam.Field.Y_OFFSET_OF_SUBSET));
        }
        appManager.msgSirtsetupSucceeded(axisID);
      }
      else if (processName == ProcessName.AUTOFIDSEED) {
        appManager.msgAutofidseedSucceeded(axisID);
        appManager.logMessage(
          AutofidseedLog.getInstance(appManager.getPropertyUserDir(), axisID), axisID);
      }
      else {
        // For processes that can also be done with processchunks.
        if (processName != null) {
          postProcess(processName.toString(), axisID);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println("ERROR:  Unable to record state.");
    }
  }

  void errorProcess(ComScriptProcess script) {
    try {
      ProcessName processName = script.getProcessName();
      CommandDetails commandDetails = script.getCommandDetails();
      AxisID axisID = script.getAxisID();
      TomogramState state = appManager.getState();
      if (processName == ProcessName.XCORR) {
        setInvalidEdgeFunctions(script.getCommand(), false);
        if (commandDetails != null
          && commandDetails.getCommandMode() == BlendmontParam.Mode.XCORR) {
          // Do not allow tomodataplots -type 4 to unless you are sure that blendmont ran.
          state.setXcorrBlendmontWasRun(axisID, false);
        }
      }
      else if (processName == ProcessName.PREBLEND) {
        setInvalidEdgeFunctions(script.getCommand(), false);
      }
      else if (processName == ProcessName.COMBINE) {
        appManager.errorProcess(script.getAxisID(), processName,
          script.getProcessDetails());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println("ERROR:  Unable to record state.");
    }
  }

  private void setInvalidEdgeFunctions(Command command, boolean succeeded) {
    if (appManager.getConstMetaData().getViewType() == ViewType.MONTAGE
      && command.getCommandName().equals(BlendmontParam.COMMAND_NAME)
      && (command.getCommandMode() == BlendmontParam.Mode.XCORR || command
        .getCommandMode() == BlendmontParam.Mode.PREBLEND)) {
      appManager.getState().setInvalidEdgeFunctions(command.getAxisID(), !succeeded);
    }
  }

  void postProcess(BackgroundProcess process) {
    try {
      super.postProcess(process);
      if (process.getCommandLine().equals(transferfidCommandLine)) {
        writeLogFile(process, process.getAxisID(), DatasetFiles.TRANSFER_FID_LOG);
        // showTransferfidLogFile(process.getAxisID());
        appManager.getState().setSeedingDone(process.getAxisID(), true);
        appManager.logMessage(TransferFidLog.getInstance(appManager.getPropertyUserDir(),
          process.getAxisID()), process.getAxisID());
      }
      else if (process.getProcessName() == ProcessName.FLATTEN_WARP) {
        FlattenWarpLog flattenWarpLog = new FlattenWarpLog();
        flattenWarpLog.setLog(process.getStdOutput());
        appManager.logMessage(flattenWarpLog, process.getAxisID());
      }
      else if (process.getProcessName() == ProcessName.RUNRAPTOR) {
        appManager.getState().setUseRaptorResultWarning(true);
      }
      else if (process.getProcessName() == ProcessName.RESTRICTALIGN) {
        appManager.logMessageWithKeyword(process.getProcessName(),
          process.getStdOutput(), process.getAxisID());
      }
      else {
        String commandName = process.getCommandName();
        if (commandName == null) {
          return;
        }
        ProcessDetails processDetails = process.getProcessDetails();
        Command command = process.getCommand();
        TomogramState state = appManager.getState();
        if (commandName.equals(TrimvolParam.commandName)) {
          AxisID axisID = process.getAxisID();
          state.setTrimvolFlipped(processDetails
            .getBooleanValue(TrimvolParam.Fields.SWAP_YZ)
            || processDetails.getBooleanValue(TrimvolParam.Fields.ROTATE_X));
          MRCHeader mrcHeader =
            MRCHeader.getInstance(appManager.getPropertyUserDir(), TrimvolParam
              .getInputFileName(appManager.getBaseMetaData().getAxisType(), appManager
                .getBaseMetaData().getName()), AxisID.ONLY);
          try {
            if (mrcHeader.read(appManager)) {
              state.setPostProcTrimVolInputNColumns(mrcHeader.getNColumns());
              state.setPostProcTrimVolInputNRows(mrcHeader.getNRows());
              state.setPostProcTrimVolInputNSections(mrcHeader.getNSections());
            }
          }
          catch (IOException e) {
            e.printStackTrace();
          }
          catch (InvalidParameterException e) {
            e.printStackTrace();
          }
        }
        else if (commandName.equals(SqueezevolParam.COMMAND_NAME)) {
          appManager.getState().setSqueezevolFlipped(
            processDetails.getBooleanValue(SqueezevolParam.Fields.FLIPPED));
        }
        else if (commandName.equals(ArchiveorigParam.COMMAND_NAME)) {
          appManager.deleteOriginalStack(command, process.getStdOutput());
        }
        else if (command != null && command.getProcessName() == ProcessName.CLIP) {
          if (command.getCommandMode() == ClipParam.Mode.STATS) {
            String logFileName = command.getCommandInputFile().getName() + "_stats.log";
            writeLogFile(process, process.getAxisID(), logFileName);
            showLogFile(new File(appManager.getPropertyUserDir(), logFileName));
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println("ERROR:  Unable to record state.");
    }
  }

  void errorProcess(BackgroundProcess process) {
    try {
      if (process.getCommandLine().equals(transferfidCommandLine)) {
        writeLogFile(process, process.getAxisID(), DatasetFiles.TRANSFER_FID_LOG);
        showLogFile(new File(appManager.getPropertyUserDir(),
          DatasetFiles.TRANSFER_FID_LOG));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println("ERROR:  Unable to record state.");
    }
  }

  BaseManager getManager() {
    return appManager;
  }
}