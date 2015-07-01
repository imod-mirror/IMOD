package etomo.process;

import etomo.BaseManager;
import etomo.comscript.Command;
import etomo.type.AxisID;
import etomo.type.FileType;

public final class OutfileComScriptProcess extends ComScriptProcess {
  public OutfileComScriptProcess(final BaseManager manager, final String comScript,
    final BaseProcessManager processManager, final AxisID axisID,
    final ProcessMonitor processMonitor, final Command command, final FileType fileType) {
    super(manager, comScript, processManager, axisID, processMonitor, command, fileType);
  }
}
