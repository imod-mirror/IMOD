package etomo.ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.comscript.ProcesschunksParam;
import etomo.comscript.SplittiltParam;
import etomo.process.LoadAverageMonitor;
import etomo.process.ParallelProcessMonitor;
import etomo.type.AxisID;
import etomo.type.ConstEtomoNumber;
import etomo.type.DialogType;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
public final class ParallelPanel implements ParallelProgressDisplay, Expandable, LoadAverageDisplay {
  public static final String rcsid = "$Id$";

  private JPanel rootPanel;
  private final SpacedPanel bodyPanel;
  private ProcessorTable processorTable;
  private MultiLineButton btnResume = new MultiLineButton("Resume");
  private MultiLineButton btnPause = new MultiLineButton("Pause");
  private MultiLineButton btnSaveDefaults = new MultiLineButton("Save As Defaults");
  private LabeledTextField ltfCPUsSelected = new LabeledTextField(
      "CPUs selected: ");
  private LabeledTextField ltfChunksFinished = new LabeledTextField(
      "Chunks finished: ");
  private LabeledSpinner nice;
  private ArrayList randomRestarts = new ArrayList();
  private ArrayList randomSuccesses = new ArrayList();
  private Random random = new Random(new Date().getTime());

  private AxisID axisID = null;
  private ParallelDialog container = null;
  private DialogType dialogType = null;
  private ParallelPanelActionListener actionListener = new ParallelPanelActionListener(
      this);
  private PanelHeader header;
  private final BaseManager manager;
  private boolean visible = false;
  private boolean pauseEnabled = false;
  private LoadAverageMonitor loadAverageMonitor = null;
  private ParallelProcessMonitor parallelProcessMonitor = null;
  private final JPanel tablePanel = new JPanel();
  
  public ParallelPanel(BaseManager manager, AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    //initialize table
    processorTable = new ProcessorTable(this, axisID);
    //set listeners
    btnResume.addActionListener(actionListener);
    btnPause.addActionListener(actionListener);
    btnSaveDefaults.addActionListener(actionListener);
    //panels
    rootPanel = new JPanel();
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
    rootPanel.setBorder(BorderFactory.createEtchedBorder());
    tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.X_AXIS));
    bodyPanel = new SpacedPanel();
    bodyPanel.setBoxLayout(BoxLayout.Y_AXIS);
    SpacedPanel southPanel = new SpacedPanel();
    southPanel.setBoxLayout(BoxLayout.X_AXIS);
    //southPanel;
    southPanel.add(ltfCPUsSelected);
    SpinnerModel model = new SpinnerNumberModel(
        ProcesschunksParam.NICE_DEFAULT, ProcesschunksParam.NICE_FLOOR,
        ProcesschunksParam.NICE_CEILING, 1);
    nice = new LabeledSpinner("Nice: ", model);
    southPanel.add(nice);
    southPanel.add(btnPause);
    southPanel.add(btnResume);
    southPanel.add(btnSaveDefaults);
    //tablePanel
    buildTablePanel();
    //bodyPanel
    bodyPanel.addRigidArea();
    bodyPanel.add(tablePanel);
    bodyPanel.add(southPanel);
    //header
    header = PanelHeader.getExpandedMoreLessInstance(axisID, "Parallel Processing", this);
    //rootPanel
    rootPanel.add(header.getContainer());
    rootPanel.add(bodyPanel.getContainer());
    //configure fields
    //header.setOpen(true);
    ltfChunksFinished.setTextPreferredWidth(FixedDim.fourDigitWidth);
    ltfChunksFinished.setEditable(false);
    ltfCPUsSelected.setTextPreferredWidth(FixedDim.fourDigitWidth);
    ltfCPUsSelected.setEditable(false);
    processorTable.msgCPUsSelectedChanged();
    btnPause.setEnabled(pauseEnabled);
    manager.getMainPanel().setParallelProgressDisplay(this, axisID);
    processorTable.setRestartsError(ProcesschunksParam.DROP_VALUE);
    EtomoDirector.getInstance().loadPreferences(processorTable, axisID);
  }
  
  private final void buildTablePanel() {
    tablePanel.removeAll();
    tablePanel.add(Box.createHorizontalGlue());
    tablePanel.add(processorTable.getContainer());
    tablePanel.add(Box.createHorizontalGlue());
  }
  
  final void start() {
    processorTable.startGetLoadAverage(this);
  }
  
  final void stop() {
    processorTable.stopGetLoadAverage(this);
  }
  
  final void startGetLoadAverage(String computer) {
    manager.startGetLoadAverage(this, computer);
  }
  
  final void stopGetLoadAverage(String computer) {
    if (loadAverageMonitor != null) {
      manager.stopGetLoadAverage(this, computer);
    }
  }
  
  public final LoadAverageMonitor getLoadAverageMonitor() {
    if (loadAverageMonitor == null) {
      loadAverageMonitor = new LoadAverageMonitor(this);
    }
    return loadAverageMonitor;
  }
  
  public final void setLoadAverage(String computer, double load1, double load5, double load15) {
    processorTable.setLoadAverage(computer, load1, load5, load15);
  }
  
  public final void setPauseEnabled(boolean pauseEnabled) {
    this.pauseEnabled = pauseEnabled;
    if (btnPause == null) {
      return;
    }
    btnPause.setEnabled(pauseEnabled);
  }
  
  public final void msgCPUsSelectedChanged(int cpusSelected) {
    ltfCPUsSelected.setText(cpusSelected);
  }

  final void setDialogType(DialogType dialogType) {
    this.dialogType = dialogType;
  }

  final Container getRootPanel() {
    return rootPanel;
  }
  
  public final void setContainer(ParallelDialog container) {
    this.container = container;
  }

  private final void performAction(ActionEvent event) {
    String command = event.getActionCommand();
    if (command == btnResume.getText()) {
      container.resume();
    }
    else if (command == btnPause.getText()) {
      manager.pause(axisID);
    }
    else if (command == btnSaveDefaults.getText()) {
      manager.savePreferences(axisID, processorTable);
    }
  }

  final void setVisible(boolean visible) {
    this.visible = visible;
    rootPanel.setVisible(visible);
  }

  public final int getCPUsSelected() {
    return processorTable.getCPUsSelected();
  }

  public final void addRestart(String computer) {
    processorTable.addRestart(computer);
  }
  
  public final void msgDropped(String computer, String reason) {
    processorTable.msgDropped(computer, reason);
  }

  public final void addSuccess(String computer) {
    processorTable.addSuccess(computer);
  }
  
  final void resetResults() {
    processorTable.resetResults();
  }
  
  public final void msgInterruptingProcess() {
    btnResume.setEnabled(true);
  }
  
  final boolean getParameters(SplittiltParam param) {
    ConstEtomoNumber numMachines = param.setNumMachines(ltfCPUsSelected
        .getText());
    if (!numMachines.isValid()) {
      UIHarness.INSTANCE.openMessageDialog(ltfCPUsSelected.getLabel() + " "
          + numMachines.getInvalidReason() + "\n"
          + processorTable.getHelpMessage(), "Table Error", axisID);
      return false;
    }
    return true;
  }
  
  final void getParameters(ProcesschunksParam param) {
     param.setNice(nice.getValue());
     processorTable.getParameters(param);
  }
  
  final void pack() {
    if (!visible) {
      return;
    }
    processorTable.pack();
  }
  
  /**
   * set the visible boolean based on whether the panel is visible
   * the body panel setVisible function was called by the header panel
   */
  public final void expand(ExpandButton button) {
    if (header.equalsOpenClose(button)) {
      visible = button.isExpanded();
      bodyPanel.setVisible(visible);
    }
    else if (header.equalsMoreLess(button)) {
      if (processorTable == null) {
        return;
      }
      boolean expanded = button.isExpanded();
      btnSaveDefaults.setVisible(expanded);
      processorTable.setExpanded(expanded);
      buildTablePanel();
    }
    else {
      return;
    }
    UIHarness.INSTANCE.pack(axisID, manager);
  }
  
  public final void msgLoadAverageFailed(String computer, String reason) {
    if (parallelProcessMonitor != null) {
      parallelProcessMonitor.drop(computer);
    }
    processorTable.clearLoadAverage(computer, reason);
  }
  
  public void clearFailureReason(String computer) {
    processorTable.clearFailureReason(computer);
  }
  
  /**
   * sets parallelProcessMonitor with a monitor which is monitoring a parallel
   * process associated with this ParallelProgressDisplay
   * @param ParallelProcessMonitor
   */
  public final void setParallelProcessMonitor(ParallelProcessMonitor parallelProcessMonitor) {
    this.parallelProcessMonitor = parallelProcessMonitor;
  }

  private final class ParallelPanelActionListener implements ActionListener {
    ParallelPanel adaptee;

    ParallelPanelActionListener(ParallelPanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.performAction(event);
    }
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.16  2005/09/19 16:40:21  sueh
 * <p> bug# 532 Changed member variables ParallelDialog parent to container.
 * <p> Added setContainer() to change the container each time the panel is
 * <p> placed on a dialog.
 * <p>
 * <p> Revision 1.15  2005/09/13 00:00:38  sueh
 * <p> bug# 532 Added a call to BaseManager.loadPreferences() to load defaults
 * <p> into the processor table.  Implemented Save Defaults button by adding a
 * <p> call to BaseManager.savePreferences() in performAction().
 * <p>
 * <p> Revision 1.14  2005/09/10 01:54:49  sueh
 * <p> bug# 532 Added clearFailureReason() so that the failure reason can be
 * <p> cleared when a new connection to the computer is attempted.
 * <p>
 * <p> Revision 1.13  2005/09/09 21:47:17  sueh
 * <p> bug# 532 moved call to clearLoadAverage() to msgLoadAverageFailed().
 * <p>
 * <p> Revision 1.12  2005/09/01 18:35:20  sueh
 * <p> bug# 532 ParallelPanel is no longer a doubleton.  It needs to be manager
 * <p> level.
 * <p>
 * <p> Revision 1.11  2005/09/01 18:02:10  sueh
 * <p> bug# 532 Added clearLoadAverage() to clear the load averages when the
 * <p> load average command fails.  Added a drop reason.
 * <p>
 * <p> Revision 1.10  2005/08/30 19:21:23  sueh
 * <p> bug# 532 Added parallel process monitor.  Added loadAverageFailed() to
 * <p> handle the failure of a w command.
 * <p>
 * <p> Revision 1.9  2005/08/22 18:08:08  sueh
 * <p> bug# 532 Made ParallelPane a doubleton.  Added start and
 * <p> stopLoadAverages
 * <p>
 * <p> Revision 1.8  2005/08/04 20:13:54  sueh
 * <p> bug# 532  Removed demo functions.  Added pack().
 * <p>
 * <p> Revision 1.7  2005/08/01 18:11:31  sueh
 * <p> bug# 532 Changed ProcessorTableRow.signalRestart() to addRestart.
 * <p> Added nice spinner.  Added getParameters(ProcesschunksParam).
 * <p>
 * <p> Revision 1.6  2005/07/29 00:54:29  sueh
 * <p> bug# 709 Going to EtomoDirector to get the current manager is unreliable
 * <p> because the current manager changes when the user changes the tab.
 * <p> Passing the manager where its needed.
 * <p>
 * <p> Revision 1.5  2005/07/21 22:21:07  sueh
 * <p> bug# 532 Implementing setup and teardownParallelProgressDisplay() for
 * <p> ParallelProgressDisplay so that the pause button can function like a kill
 * <p> button.
 * <p>
 * <p> Revision 1.4  2005/07/11 23:12:20  sueh
 * <p> bug# 619 Removed the split, start, and kill buttons.  Split and start are
 * <p> now handled by the parent dialog.  Kill is handled by progress panel.
 * <p> Added randomization to be used by the demo monitor.  Added functions:
 * <p> buildRandomizerLists, getCpusSelected, resetResults,
 * <p> setEnabledResume, setVisible, signalRandomRestart,
 * <p> signalRandomSuccess, signalStartProgress.  Removed totalResults().
 * <p>
 * <p> Revision 1.3  2005/07/06 23:45:38  sueh
 * <p> bug# 619 Removed DoubleSpacedPanel and FormattedPanel.  Placed
 * <p> their functionality in SpacedPanel.  Simplified the construction of
 * <p> SpacedPanel.
 * <p>
 * <p> Revision 1.2  2005/07/01 23:04:26  sueh
 * <p> bug# 619 removed parent dialog from constructor
 * <p>
 * <p> Revision 1.1  2005/07/01 21:21:23  sueh
 * <p> bug# 619 Panel containing parallel processing
 * <p> </p>
 */