package etomo.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.comscript.BatchruntomoParam;
import etomo.comscript.IntermittentCommand;
import etomo.comscript.LoadAverageParam;
import etomo.storage.CpuAdoc;
import etomo.storage.Network;
import etomo.storage.Node;
import etomo.type.AxisID;
import etomo.type.ConstEtomoVersion;
import etomo.type.ProcessingMethod;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2010 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 *          <p/>
 *          <p> $Log$
 *          <p> Revision 1.3  2011/07/18 22:44:44  sueh
 *          <p> Bug# 1515 Removed isSelectOnlyRow - no longer needed.
 *          <p>
 *          <p> Revision 1.2  2011/02/22 18:07:08  sueh
 *          <p> bug# 1437 Reformatting.
 *          <p>
 *          <p> Revision 1.1  2011/02/03 06:13:39  sueh
 *          <p> bug# 1422 Child of ProcessorTable that makes a ProcessorTable display
 *          <p> CPUs.
 *          <p> </p>
 */
class CpuTable extends ProcessorTable {
  private final String PREPEND = ".Cpu";

  private final HeaderCell header1Load = new HeaderCell("Load Average");
  private final HeaderCell header1CPUUsage = new HeaderCell("CPU Usage");
  private final HeaderCell header1Users = new HeaderCell("Users");
  private final HeaderCell header2Load1 = new HeaderCell("1 Min.");
  private final HeaderCell header2Load5 = new HeaderCell("5 Min.");
  private final HeaderCell header2CPUUsage = new HeaderCell();
  private final HeaderCell header2Users = new HeaderCell();

  private final boolean usersColumn;

  CpuTable(final BaseManager manager, final ParallelPanel parent, final AxisID axisID,
      final boolean runnable) {
    super(manager, parent, axisID, false, runnable);
    usersColumn =
        CpuAdoc.INSTANCE.isUsersColumn(manager, axisID, manager.getPropertyUserDir());
  }

  boolean isCpuTable() {
    return true;
  }

  boolean isGpuTable() {
    return false;
  }

  String getStorePrepend() {
    return getGroupKey() + PREPEND;
  }

  String getLoadPrepend(ConstEtomoVersion version) {
    if (version.ge("1.1")) {
      return getGroupKey() + PREPEND;
    }
    return "ProcessorTable";
  }

  final int getSize() {
    return Network.getNumComputers(manager, axisID, manager.getPropertyUserDir());
  }

  void getParameters(final ProcessingMethod method, final BatchruntomoParam param) {
    if (method == ProcessingMethod.PP_CPU) {
      param.resetCPUMachineList();
      getParameters(param);
    }
  }

  final ButtonGroup getButtonGroup() {
    return null;
  }

  final Node getNode(final int index) {
    return Network.getComputer(manager, index, axisID, manager.getPropertyUserDir());
  }

  ProcessorTableRow createProcessorTableRow(final ProcessorTable processorTable,
      final Node node, final int numRowsInTable) {
    return ProcessorTableRow
        .getComputerInstance(processorTable, node, node.getNumber(), numRowsInTable);
  }

  String getHeader1ComputerText() {
    return "Computer";
  }

  String getNoCpusSelectedErrorMessage() {
    return "At least one computer must be selected.";
  }

  final void addHeader1Load(final JPanel tablePanel, final GridBagLayout layout,
      final GridBagConstraints constraints, final ColumnName lastColumnName) {
    if (Utilities.isWindowsOS()) {
      if (lastColumnName == ColumnName.LOAD) {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
      }
      else {
        constraints.gridwidth = 1;
      }
      header1CPUUsage.add(tablePanel, layout, constraints);
    }
    else {
      if (lastColumnName == ColumnName.LOAD) {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
      }
      else {
        constraints.gridwidth = 2;
      }
      header1Load.add(tablePanel, layout, constraints);
      constraints.gridwidth = 1;
    }
  }

  final void addHeader1Users(final JPanel tablePanel, final GridBagLayout layout,
      final GridBagConstraints constraints, final ColumnName lastColumnName) {
    if (Utilities.isWindowsOS()) {
      return;
    }
    if (lastColumnName == ColumnName.USERS) {
      constraints.gridwidth = GridBagConstraints.REMAINDER;
    }
    header1Users.add(tablePanel, layout, constraints);
  }

  final void addHeader2Load(final JPanel tablePanel, final GridBagLayout layout,
      final GridBagConstraints constraints, final ColumnName lastColumnName) {
    if (Utilities.isWindowsOS()) {
      if (lastColumnName == ColumnName.LOAD) {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
      }
      header2CPUUsage.add(tablePanel, layout, constraints);
    }
    else {
      header2Load1.add(tablePanel, layout, constraints);
      if (lastColumnName == ColumnName.LOAD) {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
      }
      header2Load5.add(tablePanel, layout, constraints);
    }
  }

  final void addHeader2Users(final JPanel tablePanel, final GridBagLayout layout,
      final GridBagConstraints constraints, final ColumnName lastColumnName) {
    if (Utilities.isWindowsOS()) {
      return;
    }
    if (lastColumnName == ColumnName.USERS) {
      constraints.gridwidth = GridBagConstraints.REMAINDER;
    }
    header2Users.add(tablePanel, layout, constraints);
  }

  final boolean useColumn(final ColumnName columnName) {
    if (columnName == ColumnName.USERS) {
      return usersColumn && !isSecondary();
    }
    return super.useColumn(columnName);
  }

  final IntermittentCommand getIntermittentCommand(final String computer) {
    return LoadAverageParam.getInstance(computer, manager);
  }

  final void setHeaderLoadToolTipText() {
    if (Utilities.isWindowsOS()) {
      header1CPUUsage.setToolTipText(
          "The CPU usage (0 to number of CPUs) averaged over one second.");
    }
    else {
      header1Load.setToolTipText("Represents how busy each computer is.");
      header2Load1.setToolTipText("The load averaged over one minute.");
      header2Load5.setToolTipText("The load averaged over five minutes.");
    }
  }

  final void setHeaderUsersToolTipText() {
    if (!Utilities.isWindowsOS()) {
      String text = "The number of users logged into the computer.";
      header1Users.setToolTipText(text);
      header2Users.setToolTipText(text);
    }
  }

  boolean isExcludeNode(final Node node) {
    return false;
  }

  final boolean isNiceable() {
    return true;
  }

  void initRow(ProcessorTableRow row) {
  }
}
