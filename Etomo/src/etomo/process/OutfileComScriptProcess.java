package etomo.process;

import java.io.FileNotFoundException;

import etomo.BaseManager;
import etomo.comscript.Command;
import etomo.storage.LogFile;
import etomo.type.AxisID;
import etomo.type.FileType;

public final class OutfileComScriptProcess extends ComScriptProcess {
  private final BaseProcessManager processManager;
  private final DetachedProcessMonitor monitor;

  public OutfileComScriptProcess(final BaseManager manager, final String comScript,
    final BaseProcessManager processManager, final AxisID axisID,
    final DetachedProcessMonitor monitor, final Command command,
    final FileType fileType) {
    super(manager, comScript, processManager, axisID, monitor, command, fileType);
    this.processManager = processManager;
    this.monitor = monitor;
  }

  /**
   * Need this so the right msgComScriptDone function can be called.
   */
  void runMsgComScriptDone(final int exitValue) {
    try {
      // Wait for the monitor to complete.
      while (monitor.isProcessRunning()) {
        Thread.sleep(100);
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    processManager.msgComScriptDone(this, exitValue, getNonBlocking());
  }

  ProcessMessages getMonitorProcessMessages() {
    if (monitor == null) {
      return null;
    }
    return monitor.getProcessMessages();
  }
  
  public final void kill(AxisID axisID) {
    monitor.kill(this, axisID);
  }
  
  public final void pause(AxisID axisID) {
    monitor.pause(this, axisID);
  }
  
  final String getStatusString() {
    return monitor.getStatusString();
  }

  /**
   * Do not parse the log file for warnings.
   */
  void parse(String name, boolean mustExist) throws LogFile.LockException,
    FileNotFoundException {}
}
