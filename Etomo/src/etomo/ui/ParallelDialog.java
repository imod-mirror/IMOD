package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import etomo.BaseManager;
import etomo.ParallelManager;
import etomo.comscript.ParallelParam;
import etomo.comscript.ProcesschunksParam;
import etomo.type.AxisID;
import etomo.type.BaseScreenState;
import etomo.type.DialogType;
import etomo.type.ParallelMetaData;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
public final class ParallelDialog implements AbstractParallelDialog {
  public static final String rcsid = "$Id$";

  private static final DialogType DIALOG_TYPE = DialogType.PARALLEL;

  private final ImageIcon iconFolder = new ImageIcon(ClassLoader
      .getSystemResource("images/openFile.gif"));
  private final SpacedPanel pnlRoot = new SpacedPanel();
  private final SpacedPanel pnlProcessName = new SpacedPanel();
  private final JButton btnChunkComscript = new JButton(iconFolder);
  private final LabeledTextField ltfProcessName = new LabeledTextField(
      "Process name: ");
  private final MultiLineButton btnRunProcess = MultiLineButton
      .getToggleButtonInstance("Run Parallel Process");

  private final ParallelActionListener actionListener;
  private final ParallelManager manager;
  private final AxisID axisID;

  private File workingDir = null;

  private ParallelDialog(ParallelManager manager, AxisID axisID) {
    actionListener = new ParallelActionListener(this);
    this.manager = manager;
    this.axisID = axisID;
    //process name panel
    pnlProcessName.setBoxLayout(BoxLayout.X_AXIS);
    ltfProcessName.setTextPreferredWidth(125);
    pnlProcessName.add(ltfProcessName);
    btnChunkComscript.setPreferredSize(FixedDim.folderButton);
    pnlProcessName.add(btnChunkComscript);
    //root panel
    pnlRoot.setBorder(new BeveledBorder("Parallel Process").getBorder());
    pnlRoot.setBoxLayout(BoxLayout.Y_AXIS);
    pnlRoot.add(pnlProcessName);
    btnRunProcess.setSize();
    pnlRoot.add(btnRunProcess);
    pnlRoot.alignComponentsX(Component.CENTER_ALIGNMENT);
    setToolTipText();
  }

  public static ParallelDialog getInstance(ParallelManager manager,
      AxisID axisID) {
    ParallelDialog instance = new ParallelDialog(manager, axisID);
    instance.addListeners();
    return instance;
  }

  private void addListeners() {
    btnChunkComscript.addActionListener(new ChunkComscriptActionListener(this));
    btnRunProcess.addActionListener(actionListener);
  }

  public Container getContainer() {
    return pnlRoot.getContainer();
  }

  public DialogType getDialogType() {
    return DIALOG_TYPE;
  }

  public File getWorkingDir() {
    return workingDir;
  }

  public void done() {
    btnRunProcess.removeActionListener(actionListener);
  }

  public void setParameters(BaseScreenState screenState) {
    btnRunProcess.setButtonState(screenState.getButtonState(btnRunProcess
        .createButtonStateKey(DIALOG_TYPE)));
  }

  public void setParameters(ParallelMetaData metaData) {
    ltfProcessName.setText(metaData.getRootName());
  }

  public void getParameters(BaseScreenState screenState) {
    screenState.setButtonState(btnRunProcess.getButtonStateKey(), btnRunProcess
        .getButtonState());
  }

  public void getParameters(ParallelParam param) {
    ProcesschunksParam processchunksParam = (ProcesschunksParam) param;
    processchunksParam.setRootName(ltfProcessName.getText());
  }

  public void getParameters(ParallelMetaData metaData) {
    metaData.setRootName(ltfProcessName.getText());
  }

  public void updateDisplay(boolean setupMode) {
    ltfProcessName.setEditable(setupMode);
    btnChunkComscript.setEnabled(setupMode);
  }

  public final boolean usingParallelProcessing() {
    return true;
  }

  void action(ActionEvent event) {
    String command = event.getActionCommand();
    if (command.equals(btnRunProcess.getText())) {
      manager.processchunks(btnRunProcess);
    }
  }

  void chunkComscriptAction() {
    File chunkComscript = BaseManager.chunkComscriptAction(pnlRoot
        .getContainer());
    if (chunkComscript != null) {
      try {
        String comFileName = chunkComscript.getName();
        ltfProcessName.setText(comFileName.substring(0, comFileName
            .lastIndexOf("-001")));
        workingDir = chunkComscript.getParentFile();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void setToolTipText() {
    ltfProcessName
        .setToolTipText("The process name is based on the name of the first comscript (-001.com or -001-sync.com).");
    btnChunkComscript
        .setToolTipText("Selects the first comscript (-001.com or -001-sync.com).");
    btnRunProcess.setToolTipText("Runs the process.");
  }

  private class ParallelActionListener implements ActionListener {
    ParallelDialog adaptee;

    ParallelActionListener(ParallelDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.action(event);
    }
  }

  private class ChunkComscriptActionListener implements ActionListener {
    ParallelDialog adaptee;

    ChunkComscriptActionListener(ParallelDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.chunkComscriptAction();
    }
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.16  2007/08/29 21:49:17  sueh
 * <p> bug# 1041 In chunkComscriptAction, calling BaseManager.chunkComscriptAction.
 * <p>
 * <p> Revision 1.15  2007/08/10 17:05:31  mast
 * <p> Set the process name properly if starting file is -001-sync.com
 * <p>
 * <p> Revision 1.14  2007/02/09 00:50:54  sueh
 * <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 * <p> classes.
 * <p>
 * <p> Revision 1.13  2006/11/07 22:50:00  sueh
 * <p> bug# 954 Added setToolTipText().
 * <p>
 * <p> Revision 1.12  2006/07/28 19:57:10  sueh
 * <p> bug# 868 Changed AbstractParallelDialog.isParallel to
 * <p> usingParallelProcessing because isParallel is too similar to a standard get
 * <p> function.
 * <p>
 * <p> Revision 1.11  2006/04/06 20:52:26  sueh
 * <p> bug# 840 When getting the name of the parallel process, don't strip any
 * <p> dash but the last one off.
 * <p>
 * <p> Revision 1.10  2006/03/28 00:55:47  sueh
 * <p> bug# 437 Change getButtonStateKey(DialogType) to
 * <p> createButtonStateKey(DialogType).
 * <p>
 * <p> Revision 1.9  2006/03/20 18:05:32  sueh
 * <p> bug# 835 Dialog handling generic parallel processes.
 * <p> </p>
 */
