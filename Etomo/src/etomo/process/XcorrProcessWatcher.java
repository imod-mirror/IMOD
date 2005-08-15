package etomo.process;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2002, 2003, 2004, 2005</p>
 * 
 * <p>Organization: Boulder Laboratory for 3D Electron Microscopy (BL3dEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
import etomo.ApplicationManager;
import etomo.comscript.BlendmontParam;
import etomo.type.AxisID;
import etomo.type.ProcessEndState;

public class XcorrProcessWatcher implements ProcessMonitor {
  public static final String rcsid = "$Id$";

  private ApplicationManager applicationManager = null;
  private AxisID axisID = null;
  private boolean blendmont = false;
  private ProcessEndState endState = null;

  /**
   * Construct a xcorr process watcher
   * @param appMgr
   * @param id
   */
  public XcorrProcessWatcher(ApplicationManager applicationManager,
      AxisID axisID, boolean blendmont) {
    this.applicationManager = applicationManager;
    this.axisID = axisID;
    this.blendmont = blendmont;
  }

  public void run() {
    if (blendmont) {
      BlendmontProcessMonitor blendmontMonitor = new BlendmontProcessMonitor(
          applicationManager, axisID, BlendmontParam.XCORR_MODE);
      blendmontMonitor.setLastProcess(false);
      Thread blendmontThread = new Thread(blendmontMonitor);
      blendmontThread.start();
      while (!blendmontMonitor.isDone()) {
        try {
          Thread.sleep(100);
        }
        catch (Exception e) {
          setProcessEndState(ProcessEndState.DONE);
          //not expecting any exception here
          e.printStackTrace();
          //send an interrupt to the monitor so it can clean up
          blendmontThread.interrupt();
          return;
        }
      }
    }
    TiltxcorrProcessWatcher tiltxcorrMonitor = new TiltxcorrProcessWatcher(
        applicationManager, axisID, blendmont);
    Thread tiltxcorrThread = new Thread(tiltxcorrMonitor);
    tiltxcorrThread.start();
    while (!tiltxcorrMonitor.isDone()) {
      try {
        Thread.sleep(100);
      }
      catch (Exception e) {
        //only expecting interrupt here
        if (!(e instanceof InterruptedException)) {
          e.printStackTrace();
        }
        //send an interrupt to the monitor so it can clean up
        tiltxcorrThread.interrupt();
      }
    }
    setProcessEndState(ProcessEndState.DONE);
  }
  
  /**
   * set end state
   * @param endState
   */
  public synchronized final void setProcessEndState(ProcessEndState endState) {
    this.endState = ProcessEndState.precedence(this.endState, endState);
  }
  
  public synchronized final ProcessEndState getProcessEndState() {
    return endState;
  }
  
  public void setProcess(SystemProcessInterface process) {
    //process is not required
  }
  
  public void kill(SystemProcessInterface process, AxisID axisID) {
    endState = ProcessEndState.KILLED;
    process.signalKill(axisID);
  }
  
  public void pause(SystemProcessInterface process, AxisID axisID) {
    throw new IllegalStateException("can't pause an xcorr process");
  }
}
/**
 * <p> $Log$
 * <p> Revision 3.10  2005/08/04 19:52:53  sueh
 * <p> bug# 532 Added empty setProcess() to implement ProcessMonitor.
 * <p>
 * <p> Revision 3.9  2005/07/26 21:47:32  sueh
 * <p> bug# 701 Implementing ProcessMonitor, which extends Runnable.
 * <p> Added a ProcessEndState member variable.  Set it to DONE when the
 * <p> end of the process is detected.  In the future, will set the
 * <p> ProcessEndState variable to FAILED, if necessary to get the correct
 * <p> progress bar behavior.
 * <p>
 * <p> Revision 3.8  2005/03/11 01:35:08  sueh
 * <p> bug# 533 Setting BlendmontProcessMonitor.lastProcess to false, so it
 * <p> doesn't display "done".
 * <p>
 * <p> Revision 3.7  2005/03/09 22:31:26  sueh
 * <p> bug# 533 Catching the interrupt exception sent by ComScriptProcess.
 * <p> If it is sent while blendmont is running, there is a problem.  Pass the
 * <p> interrupt to the current monitor so that it can stop the progress bar.
 * <p>
 * <p> Revision 3.6  2005/03/09 18:11:15  sueh
 * <p> bug# 533 This class used to watch tiltxcorr in the xcorr script (see
 * <p> TiltxcorrProcessWatcher).  Now it watches the xcorr script.  First it uses
 * <p> BlendmontProcessMonitor to watch blendmont, then it uses
 * <p> TiltxcorrProcessWatcher to watch tiltxcorr.  It needs to know whether
 * <p> blendmont is going to run.
 * <p>
 * <p> Revision 3.5  2004/11/19 23:26:42  sueh
 * <p> bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 * <p>
 * <p> Revision 3.4.4.1  2004/09/29 19:12:32  sueh
 * <p> bug# 520 Removing pass-through function calls.
 * <p>
 * <p> Revision 3.4  2004/04/23 20:03:08  sueh
 * <p> bug# 83 allowing initializeProgressBar() to be called before
 * <p> nSections is set
 * <p>
 * <p> Revision 3.3  2004/03/16 21:53:40  sueh
 * <p> bug# 413  when last line in the log file found, starting incrementing waitingForExit
 * <p> counter
 * <p>
 * <p> Revision 3.2  2004/03/13 01:57:35  sueh
 * <p> bug# 413 fixed backward monitor by counting lines
 * <p> possible solution for LogFileProcessMonitor.run() infinite loop in comments.
 * <p>
 * <p> Revision 3.1  2003/11/26 23:38:03  rickg
 * <p> Changed name of logFileReader
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 1.7  2003/08/05 21:16:26  rickg
 * <p> Correctly set nSections.
 * <p> SystemProcessInterface object is no longer necessary
 * <p>
 * <p> Revision 1.6  2003/08/04 22:23:50  rickg
 * <p> Now derived from LogFileProcessMonitor
 * <p>
 * <p> Revision 1.5  2003/06/27 20:17:51  rickg
 * <p> Fixed javadoc header
 * <p> </p>
 */