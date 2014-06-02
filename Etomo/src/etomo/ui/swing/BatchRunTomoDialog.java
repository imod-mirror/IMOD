package etomo.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.storage.DirectiveFileCollection;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.DialogType;
import etomo.type.DirectiveFileType;
import etomo.ui.BatchRunTomoTab;
import etomo.ui.FieldType;
import etomo.util.Utilities;

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
public final class BatchRunTomoDialog implements ActionListener, ResultListener,
    ChangeListener {
  public static final String rcsid = "$Id:$";

  public static final DialogType DIALOG_TYPE = DialogType.BATCH_RUN_TOMO;
  private static final String DELIVER_TO_DIRECTORY_NAME = "Move datasets to";

  private final JPanel pnlRoot = new JPanel();
  private final LabeledTextField ltfRootName = new LabeledTextField(FieldType.STRING,
      "Batchruntomo root name: ");
  private final CheckBox cbDeliverToDirectory = new CheckBox();
  private final LabeledTextField ltfEmailAddress = new LabeledTextField(FieldType.STRING,
      "Email notification: ");
  private final CheckBox cbUseCPUMachineList = new CheckBox("Parallel processing");
  private final CheckBox cbUseGPUMachineList = new CheckBox("GPU");
  private final TabbedPane tabbedPane = new TabbedPane();
  private final JPanel[] pnlTabs = new JPanel[BatchRunTomoTab.SIZE];
  private final JPanel pnlBatch = new JPanel();
  private final JPanel pnlStacks = new JPanel();
  private final JPanel pnlRun = new JPanel();
  private final JPanel pnlTable = new JPanel();
  private final MultiLineButton btnRun = new MultiLineButton("Run Batchruntomo");
  private final JPanel pnlRunButton = new JPanel();
  private final JPanel pnlParallelSettings = new JPanel();

  private final FileTextField2 ftfRootName;
  private final FileTextField2 ftfInputDirectiveFile;
  private final TemplatePanel templatePanel;
  private final FileTextField2 ftfDeliverToDirectory;
  private final BatchRunTomoTable table;
  private final BaseManager manager;
  private final AxisID axisID;
  private final BatchRunTomoDatasetDialog datasetDialog;
  private final DirectiveFileCollection directiveFileCollection;

  private BatchRunTomoTab curTab = null;
  private List<BatchRunTomoDatasetDialog> datasetLevelDialogList = new ArrayList<BatchRunTomoDatasetDialog>();

  private BatchRunTomoDialog(final BaseManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    ftfRootName = FileTextField2.getAltLayoutInstance(manager, "Location: ");
    ftfInputDirectiveFile = FileTextField2.getAltLayoutInstance(manager,
        "Starting directive file: ");
    ftfDeliverToDirectory = FileTextField2.getAltLayoutInstance(manager,
        DELIVER_TO_DIRECTORY_NAME + ": ");
    table = BatchRunTomoTable.getInstance(manager);
    datasetDialog = BatchRunTomoDatasetDialog.getInstace(manager);
    directiveFileCollection = new DirectiveFileCollection(manager, axisID);
    templatePanel = TemplatePanel.getBorderlessInstance(manager, axisID, null, null,
        null, directiveFileCollection);
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
    JPanel pnlRootName = new JPanel();
    JPanel pnlDeliverToDirectory = new JPanel();
    JPanel pnlTemplates = new JPanel();
    ftfRootName.setText(new File(System.getProperty("user.dir")).getAbsolutePath());
    ltfRootName.setText(Utilities.getDateTimeStampRootName());
    cbDeliverToDirectory.setName(DELIVER_TO_DIRECTORY_NAME);
    templatePanel.setTemplateColor();
    ftfInputDirectiveFile.setAbsolutePath(true);
    ftfInputDirectiveFile.setFieldEditable(false);
    ftfDeliverToDirectory.setFileSelectionMode(FileChooser.DIRECTORIES_ONLY);
    btnRun.setToPreferredSize();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    templatePanel.setParameters(EtomoDirector.INSTANCE.getUserConfiguration());
    // root panel
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(new BeveledBorder("Batchruntomo Interface").getBorder());
    pnlRoot.add(tabbedPane);
    // tabbedPane
    for (int i = 0; i < BatchRunTomoTab.SIZE; i++) {
      pnlTabs[i] = new JPanel();
      BatchRunTomoTab tab = BatchRunTomoTab.getInstance(i);
      tabbedPane.addTab(tab.getTitle(), pnlTabs[i]);
    }
    // Batch
    pnlBatch.setLayout(new BoxLayout(pnlBatch, BoxLayout.Y_AXIS));
    pnlBatch.setBorder(new EtchedBorder("Batch Setup Parameters").getBorder());
    pnlBatch.add(pnlDeliverToDirectory);
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y20));
    pnlBatch.add(ftfInputDirectiveFile.getRootPanel());
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y6));
    pnlBatch.add(pnlTemplates);
    pnlBatch.add(Box.createRigidArea(FixedDim.x0_y15));
    pnlBatch.add(pnlRootName);
    // Stacks
    pnlStacks.setLayout(new BoxLayout(pnlStacks, BoxLayout.Y_AXIS));
    pnlStacks.setBorder(BorderFactory.createEtchedBorder());
    // panel created on tab change
    // Run
    pnlRun.setLayout(new BoxLayout(pnlRun, BoxLayout.Y_AXIS));
    pnlRun.setBorder(BorderFactory.createEtchedBorder());
    // panel created on tab change
    // Table
    pnlTable.setLayout(new BoxLayout(pnlTable, BoxLayout.Y_AXIS));
    pnlTable.setBorder(new EtchedBorder("Datasets").getBorder());
    pnlTable.add(table.getComponent());
    // RunButton
    pnlRunButton.setLayout(new BoxLayout(pnlRunButton, BoxLayout.X_AXIS));
    pnlRunButton.add(Box.createHorizontalGlue());
    pnlRunButton.add(btnRun.getComponent());
    pnlRunButton.add(Box.createHorizontalGlue());
    // ParallelSettings
    pnlParallelSettings.setLayout(new BoxLayout(pnlParallelSettings, BoxLayout.Y_AXIS));
    pnlParallelSettings.setBorder(new EtchedBorder("Run Actions").getBorder());
    pnlParallelSettings.add(cbUseCPUMachineList);
    pnlParallelSettings.add(cbUseGPUMachineList);
    // RootName
    pnlRootName.setLayout(new BoxLayout(pnlRootName, BoxLayout.Y_AXIS));
    pnlRootName.setBorder(new EtchedBorder("Batchruntomo Project Files").getBorder());
    pnlRootName.add(ltfRootName.getComponent());
    pnlRootName.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlRootName.add(ftfRootName.getRootPanel());
    // Templates
    pnlTemplates.setLayout(new BoxLayout(pnlTemplates, BoxLayout.X_AXIS));
    pnlTemplates.add(templatePanel.getComponent());
    pnlTemplates.add(Box.createHorizontalGlue());
    // DeliverToDirectory
    pnlDeliverToDirectory
        .setLayout(new BoxLayout(pnlDeliverToDirectory, BoxLayout.X_AXIS));
    pnlDeliverToDirectory.add(cbDeliverToDirectory);
    pnlDeliverToDirectory.add(ftfDeliverToDirectory.getRootPanel());
    // align
    UIUtilities.alignComponentsX(pnlBatch, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
    processResult(ftfRootName);
    stateChanged(null);
    msgDirectivesChanged(true);
    updateDisplay();
  }

  private void addListeners() {
    cbDeliverToDirectory.addActionListener(this);
    ftfInputDirectiveFile.addResultListener(this);
    templatePanel.addActionListener(this);
    tabbedPane.addChangeListener(this);
  }

  public Container getContainer() {
    return pnlRoot;
  }

  public void setParameters(final BatchRunTomoMetaData metaData) {
  }

  /**
   * Handles any changes in the selection of the starting batch directive file and the
   * template files.
   * @param init
   */
  private void msgDirectivesChanged(final boolean init) {
    boolean retainUserValues = false;
    if (!init) {
      // see if the user has changed any values, and save values that the user has changed
      boolean changed = false;
      if (table.backupIfChanged()) {
        changed = true;
      }
      Iterator<BatchRunTomoDatasetDialog> iterator = datasetLevelDialogList.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().backupIfChanged()) {
          changed = true;
        }
      }
      if (datasetDialog.backupIfChanged()) {
        changed = true;
      }
      if (changed) {
        // ask the user whether they want to keep the values they changed
        retainUserValues = UIHarness.INSTANCE
            .openYesNoDialog(
                manager,
                "New batch directive/template values will be applied.  Keep your changed values?",
                axisID);
      }
    }
    // Go back to initial values
    table.setValues(directiveFileCollection);
    Iterator<BatchRunTomoDatasetDialog>   iterator = datasetLevelDialogList.iterator();
    while (iterator.hasNext()) {
      iterator.next().setValues(directiveFileCollection);
    }
    datasetDialog.setValues(directiveFileCollection);
    // apply the setup values, starting batch directive file and template files
    // checkpoint
    if (retainUserValues) {
      // apply saved user values
    }
    // delete saved user values
  }

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (templatePanel.equalsActionCommand(actionCommand)) {
      // refresh the shared directive file collection
      templatePanel.refreshDirectiveFileCollection();
      msgDirectivesChanged(false);
    }
    else if (actionCommand.equals(cbDeliverToDirectory.getActionCommand())) {
      updateDisplay();
    }
  }

  public void processResult(final Object object) {
    if (object == ftfRootName) {
      File rootLocation = ftfRootName.getFile();
      if (rootLocation != null) {
        table.setCurrentDirectory(rootLocation.getAbsolutePath());
      }
      else {
        table.setCurrentDirectory(null);
      }
    }
    else if (object == ftfInputDirectiveFile) {
      directiveFileCollection.setDirectiveFile(ftfInputDirectiveFile.getFile(),
          DirectiveFileType.BATCH);
      templatePanel.setParameters(directiveFileCollection
          .getDirectiveFile(DirectiveFileType.BATCH));
      msgDirectivesChanged(false);
    }
  }

  /**
   * Handle tab change event
   */
  public void stateChanged(final ChangeEvent event) {
    int curIndex;
    if (curTab == null) {
      tabbedPane.setSelectedIndex(BatchRunTomoTab.DEFAULT.getIndex());
      curTab = BatchRunTomoTab.DEFAULT;
      curIndex = curTab.getIndex();
    }
    else {
      pnlTabs[curTab.getIndex()].removeAll();
      curTab = BatchRunTomoTab.getInstance(tabbedPane.getSelectedIndex());
      curIndex = curTab.getIndex();
    }
    if (curTab == BatchRunTomoTab.BATCH) {
      pnlTabs[curIndex].add(pnlBatch);
    }
    else if (curTab == BatchRunTomoTab.STACKS) {
      pnlTabs[curIndex].add(pnlStacks);
      table.msgTabChanged(curTab);
      // create panel
      // stacks
      pnlStacks.add(pnlTable);
    }
    else if (curTab == BatchRunTomoTab.DATASET) {
      pnlTabs[curIndex].add(datasetDialog.getComponent());
    }
    else if (curTab == BatchRunTomoTab.RUN) {
      pnlTabs[curIndex].add(pnlRun);
      table.msgTabChanged(curTab);
      pnlRun.removeAll();
      // create panel
      // run
      pnlRun.add(pnlParallelSettings);
      pnlRun.add(Box.createRigidArea(FixedDim.x0_y5));
      pnlRun.add(ltfEmailAddress.getComponent());
      pnlRun.add(Box.createRigidArea(FixedDim.x0_y10));
      pnlRun.add(pnlTable);
      pnlRun.add(Box.createRigidArea(FixedDim.x0_y10));
      pnlRun.add(pnlRunButton);
      pnlRun.add(Box.createRigidArea(FixedDim.x0_y5));
      // align
      UIUtilities.alignComponentsX(pnlRun, Component.LEFT_ALIGNMENT);
    }
    UIHarness.INSTANCE.pack(axisID, manager);
  }

  private void updateDisplay() {
    ftfDeliverToDirectory.setEnabled(cbDeliverToDirectory.isSelected());
  }

  private void addtooltips() {
  }
}
