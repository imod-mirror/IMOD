package etomo.process;

import java.io.FileNotFoundException;

import etomo.BaseManager;
import etomo.comscript.Command;
import etomo.storage.LogFile;
import etomo.type.AxisID;
import etomo.type.FileType;

public final class OutfileComScriptProcess extends ComScriptProcess {
  private final BaseProcessManager processManager;
  private final DetachedProcessMonitor processMonitor;

  public OutfileComScriptProcess(final BaseManager manager, final String comScript,
    final BaseProcessManager processManager, final AxisID axisID,
    final DetachedProcessMonitor processMonitor, final Command command,
    final FileType fileType) {
    super(manager, comScript, processManager, axisID, processMonitor, command, fileType);
    this.processManager = processManager;
    this.processMonitor = processMonitor;
  }

  /**
   * Need this so the right msgComScriptDone function can be called.
   */
  void runMsgComScriptDone(final int exitValue) {
    try {
      // Wait for the monitor to complete.
      while (processMonitor.isProcessRunning()) {
        Thread.sleep(100);
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    processManager.msgComScriptDone(this, exitValue, getNonBlocking());
  }

  /**
   * Not not parse the log file for warnings.
   */
  void parse(String name, boolean mustExist) throws LogFile.LockException,
    FileNotFoundException {}
}
