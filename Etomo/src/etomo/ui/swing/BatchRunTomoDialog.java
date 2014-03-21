package etomo.ui.swing;

import java.awt.Container;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.DialogType;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2013</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public final class BatchRunTomoDialog implements Expandable {
  public static final String rcsid = "$Id:$";

  public static final DialogType DIALOG_TYPE = DialogType.BATCH_RUN_TOMO;
  private static final String DELIVER_TO_DIRECTORY_NAME = "Deliver to";

  private final JPanel pnlRoot = new JPanel();
  private final PanelHeader hdrBatchSetup = PanelHeader.getInstance(
      "Batch Setup Parameter", this, DIALOG_TYPE);
  private final JPanel pnlBatchSetupBody = new JPanel();
  private final CheckBox cbDeliverToDirectory = new CheckBox();
  private final BatchRunTomoTable table;

  private final TemplatePanel templatePanel;
  private final FileTextField2 ftfInputDirectiveFile;
  FileTextField2 ftfDeliverToDirectory;

  private BatchRunTomoDialog(final BaseManager manager, final AxisID axisID) {
    templatePanel = TemplatePanel.getInstance(manager, axisID, null, "Templates", null);
    ftfInputDirectiveFile = FileTextField2.getInstance(manager,
        "Starting batch directive file: ");
    ftfDeliverToDirectory = FileTextField2.getInstance(manager, DELIVER_TO_DIRECTORY_NAME
        + ": ");
    table = BatchRunTomoTable.getInstance(manager);
  }

  public static BatchRunTomoDialog getInstance(final BaseManager manager,
      final AxisID axisID) {
    BatchRunTomoDialog instance = new BatchRunTomoDialog(manager, axisID);
    instance.createPanel();
    instance.addListeners();
    instance.addtooltips();
    return instance;
  }

  private void createPanel() {
    // init
    JPanel pnlBatchSetup = new JPanel();
    cbDeliverToDirectory.setName(DELIVER_TO_DIRECTORY_NAME);
    // root panel
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(new BeveledBorder("Batch Run Tomo").getBorder());
    pnlRoot.add(pnlBatchSetup);
    pnlRoot.add(table.getComponent());
    // BatchSetup
    pnlBatchSetup.setLayout(new BoxLayout(pnlBatchSetup, BoxLayout.Y_AXIS));
    pnlBatchSetup.add(hdrBatchSetup.getContainer());
    pnlBatchSetup.add(pnlBatchSetupBody);
    pnlBatchSetupBody.setLayout(new BoxLayout(pnlBatchSetupBody, BoxLayout.Y_AXIS));
    pnlBatchSetupBody.add(templatePanel.getComponent());
    pnlBatchSetupBody.add(ftfInputDirectiveFile.getRootPanel());
  }

  private void addListeners() {
    ftfInputDirectiveFile.addResultListener(new BatchRunTomoResultListener(this));
  }

  public Container getContainer() {
    return pnlRoot;
  }

  public void setParameters(final BatchRunTomoMetaData metaData) {
  }

  private void processResult(final Object resultOrigin) {
    if (ftfInputDirectiveFile == resultOrigin) {

    }
  }

  public void expand(final ExpandButton button) {
    if (hdrBatchSetup.equalsOpenClose(button)) {
      pnlBatchSetupBody.setVisible(button.isExpanded());
    }
  }

  /**
   * An expand call from ProcessDialog.btnAdvanced.
   * @param button
   */
  public void expand(final GlobalExpandButton button) {
  }

  private void addtooltips() {
  }

  private static final class BatchRunTomoResultListener implements ResultListener {
    private final BatchRunTomoDialog dialog;

    BatchRunTomoResultListener(BatchRunTomoDialog dialog) {
      this.dialog = dialog;
    }

    public void processResult(final Object resultOrigin) {
      dialog.processResult(resultOrigin);
    }
  }
}
