package etomo.ui;

import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import etomo.PeetManager;
import etomo.comscript.ParallelParam;
import etomo.comscript.ProcesschunksParam;
import etomo.type.AxisID;
import etomo.type.BaseMetaData;
import etomo.type.DialogType;
import etomo.type.PeetMetaData;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 1.1  2007/02/19 22:03:19  sueh
 * <p> bug# 964 Dialog for PEET interface.
 * <p> </p>
 */

public final class PeetDialog implements AbstractParallelDialog, Expandable {
  public static final String rcsid = "$Id$";

  static final String DIRECTORY_LABEL = "Directory";
  static final String OUTPUT_LABEL = "Output";

  private static final DialogType DIALOG_TYPE = DialogType.PEET;
  private final LabeledTextField ltfDirectory = new LabeledTextField(
      DIRECTORY_LABEL + ": ");
  private final LabeledTextField ltfOutput = new LabeledTextField(OUTPUT_LABEL
      + ": ");
  private final JPanel pnlSetup = new JPanel();
  private final JPanel pnlSetupBody = new JPanel();
  private final VolumeTable volumeTable;
  private final PeetManager manager;
  private final AxisID axisID;
  private JPanel rootPanel = null;
  private PanelHeader setupHeader = null;

  public PeetDialog(PeetManager manager, AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    volumeTable = new VolumeTable(manager);
  }

  public void setParameters(BaseMetaData metaData) {
  }

  public void updateDisplay(boolean propertyUserDirSet) {
    ltfDirectory.setEnabled(propertyUserDirSet);
    ltfOutput.setEnabled(propertyUserDirSet);
  }

  public Container getContainer() {
    create();
    return rootPanel;
  }

  public DialogType getDialogType() {
    return DIALOG_TYPE;
  }

  public void getParameters(ParallelParam param) {
    ProcesschunksParam processchunksParam = (ProcesschunksParam) param;
    processchunksParam.setRootName(ltfOutput.getText());
  }
  
  public void getParameters(PeetMetaData metaData) {
    metaData.setName(ltfOutput.getText());
  }

  public final boolean usingParallelProcessing() {
    return true;
  }

  public void expand(ExpandButton button) {
    if (setupHeader != null) {
      if (setupHeader.equalsOpenClose(button)) {
        pnlSetupBody.setVisible(button.isExpanded());
      }
    }
    UIHarness.INSTANCE.pack(axisID, manager);
  }
  
  public String getDirectory() {
    return ltfDirectory.getText();
  }

  private void create() {
    if (rootPanel != null) {
      return;
    }
    //setup
    pnlSetupBody.setLayout(new BoxLayout(pnlSetupBody, BoxLayout.Y_AXIS));
    pnlSetupBody.add(ltfDirectory.getContainer());
    pnlSetupBody.add(ltfOutput.getContainer());
    pnlSetupBody.add(volumeTable.getComponent());
    //setup header
    pnlSetup.setLayout(new BoxLayout(pnlSetup, BoxLayout.Y_AXIS));
    pnlSetup.setBorder(BorderFactory.createEtchedBorder());
    setupHeader = PanelHeader.getInstance("Setup", this, DIALOG_TYPE);
    pnlSetup.add(setupHeader.getContainer());
    pnlSetup.add(pnlSetupBody);
    //root
    rootPanel = new JPanel();
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
    rootPanel.add(pnlSetup);
  }
}
