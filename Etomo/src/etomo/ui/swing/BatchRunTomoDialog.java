package etomo.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import etomo.BatchRunTomoManager;
import etomo.EtomoDirector;
import etomo.ProcessingMethodMediator;
import etomo.comscript.BatchruntomoParam;
import etomo.logic.BatchTool;
import etomo.logic.UserEnv;
import etomo.storage.AutodocFilter;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.LogFile;
import etomo.storage.autodoc.Autodoc;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.BatchRunTomoStatus;
import etomo.type.DialogType;
import etomo.type.DirectiveFileType;
import etomo.type.EtomoAutodoc;
import etomo.type.FileType;
import etomo.type.ProcessingMethod;
import etomo.type.Status;
import etomo.type.StatusChangeEvent;
import etomo.type.StatusChangeListener;
import etomo.type.StatusChanger;
import etomo.type.TableReference;
import etomo.type.UserConfiguration;
import etomo.ui.BatchRunTomoTab;
import etomo.ui.FieldDisplayer;
import etomo.ui.FieldType;
import etomo.ui.FieldValidationFailedException;
import etomo.util.Utilities;

/**
 * <p>Description: Interface for batchruntomo.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoDialog implements ActionListener, ResultListener,
  ChangeListener, Expandable, ProcessInterface, StatusChanger, StatusChangeListener {
  private static final String DELIVER_TO_DIRECTORY_NAME = "Move datasets to";

  private final JPanel pnlRoot = new JPanel();
  private final LabeledTextField ltfRootName = new LabeledTextField(FieldType.STRING,
    "Batchruntomo root name: ");
  private final CheckBox cbDeliverToDirectory = new CheckBox();
  private final CheckTextField ctfEmailAddress = CheckTextField.getInstance(
    FieldType.STRING, "Email notification: ");
  private final CheckBox cbCPUMachineList = new CheckBox("Use multiple CPUs");
  private final ButtonGroup bgGPUMachineList = new ButtonGroup();
  private final RadioButton rbGPUMachineListOff = new RadioButton("No GPU",
    bgGPUMachineList);
  private final RadioButton rbGPUMachineListLocal = new RadioButton("Local GPU",
    bgGPUMachineList);
  private final RadioButton rbGPUMachineList = new RadioButton("Parallel GPUs",
    bgGPUMachineList);
  private final TabbedPane tabbedPane = new TabbedPane();
  private final JPanel[] pnlTabs = new JPanel[BatchRunTomoTab.SIZE];
  private final JPanel pnlBatch = new JPanel();
  private final JPanel pnlStacks = new JPanel();
  private final JPanel pnlDataset = new JPanel();
  private final JPanel pnlRun = new JPanel();
  private final JPanel pnlTable = new JPanel();
  private final MultiLineButton btnRun = new MultiLineButton("Run");
  private final JPanel pnlParallelSettings = new JPanel();
  private final UserConfiguration userConfiguration = EtomoDirector.INSTANCE
    .getUserConfiguration();
  private final JPanel pnlDatasetTableBody = new JPanel();
  private final JPanel pnlUntitledTable = new JPanel();
  private final DatasetFieldDisplayer datasetFieldDisplayer = new DatasetFieldDisplayer(
    this);
  private final StacksFieldDisplayer stacksFieldDisplayer =
    new StacksFieldDisplayer(this);
  private final MultiLineButton btnStartOver = new MultiLineButton("Start Over");
  private final MultiLineButton btnPause = new MultiLineButton("Pause");
  private final MultiLineButton btnResume = new MultiLineButton("Resume");

  private final FileTextField2 ftfRootDir;
  private final FileTextField2 ftfInputDirectiveFile;
  private final TemplatePanel templatePanel;
  private final FileTextField2 ftfDeliverToDirectory;
  private final BatchRunTomoTable table;
  private final BatchRunTomoManager manager;
  private final AxisID axisID;
  private final BatchRunTomoDatasetDialog datasetDialog;
  private final DirectiveFileCollection directiveFileCollection;
  private final PanelHeader phDatasetTable;
  private final ProcessingMethodMediator mediator;
  private final BatchRunTomoStepPanel stepPanel;

  private BatchRunTomoTab curTab = null;
  private Vector<StatusChangeListener> listeners = null;
  private BatchRunTomoStatus status = BatchRunTomoStatus.OPEN;

  private BatchRunTomoDialog(final BatchRunTomoManager manager, final AxisID axisID,
    final TableReference tableReference) {
    this.manager = manager;
    this.axisID = axisID;
    ftfRootDir = FileTextField2.getAltLayoutInstance(manager, "Location: ");
    ftfInputDirectiveFile =
      FileTextField2.getAltLayoutInstance(manager, "Starting directive file: ");
    ftfInputDirectiveFile.checkpoint();
    ftfDeliverToDirectory =
      FileTextField2.getAltLayoutInstance(manager, DELIVER_TO_DIRECTORY_NAME + ": ");
    table = BatchRunTomoTable.getInstance(manager, this, tableReference);
    datasetDialog = BatchRunTomoDatasetDialog.getGlobalInstance(manager, this);
    directiveFileCollection = new DirectiveFileCollection(manager, axisID);
    templatePanel =
      TemplatePanel.getBorderlessInstance(manager, axisID, null, null, null,
        directiveFileCollection, true);
    phDatasetTable = PanelHeader.getInstance("Datasets", this, DialogType.BATCH_RUN_TOMO);
    mediator = manager.getProcessingMethodMediator(axisID);
    stepPanel = BatchRunTomoStepPanel.getInstance(manager, axisID);
    mediator.register(this);
  }

  public static BatchRunTomoDialog getInstance(final BatchRunTomoManager manager,
    final AxisID axisID, final TableReference tableReference) {
    BatchRunTomoDialog instance = new BatchRunTomoDialog(manager, axisID, tableReference);
    instance.createPanel();
    return instance;
  }

  private void createPanel() {
    // local panels
    JPanel pnlRootName = new JPanel();
    JPanel pnlDeliverToDirectory = new JPanel();
    JPanel pnlTemplates = new JPanel();
    JPanel pnlDatasetTable = new JPanel();
    JPanel pnlEmail = new JPanel();
    JPanel pnlSettings = new JPanel();
    JPanel pnlRunButtons = new JPanel();
    // init
    templatePanel.setFieldHighlight();
    ftfInputDirectiveFile.setAbsolutePath(true);
    ftfInputDirectiveFile.setTextEntryPolicy(false);
    ftfInputDirectiveFile.setFileFilter(new AutodocFilter());
    ftfDeliverToDirectory.setAbsolutePath(true);
    ftfDeliverToDirectory.setFileSelectionMode(FileChooser.DIRECTORIES_ONLY);
    btnRun.setToPreferredSize();
    btnStartOver.setToPreferredSize();
    btnPause.setToPreferredSize();
    btnResume.setToPreferredSize();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    ftfRootDir.setAbsolutePath(true);
    ftfRootDir.setFileSelectionMode(FileChooser.DIRECTORIES_ONLY);
    ftfRootDir.setText(new File(System.getProperty("user.dir")).getAbsolutePath());
    ftfInputDirectiveFile.setOrigin(ftfRootDir.getFile());
    ftfInputDirectiveFile.setOriginReference(ftfRootDir);
    cbDeliverToDirectory.setName(DELIVER_TO_DIRECTORY_NAME);
    // Make sure that the machine lists from the batchruntomo .com file get loaded.
    cbCPUMachineList.setSelected(true);
    rbGPUMachineList.setSelected(true);
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
    // Dataset
    pnlDataset.setLayout(new BoxLayout(pnlDataset, BoxLayout.Y_AXIS));
    pnlDataset.setBorder(BorderFactory.createEtchedBorder());
    pnlDataset.add(datasetDialog.getComponent());
    pnlDataset.add(pnlDatasetTable);
    // Run
    pnlRun.setLayout(new BoxLayout(pnlRun, BoxLayout.Y_AXIS));
    pnlRun.setBorder(BorderFactory.createEtchedBorder());
    pnlRun.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlRun.add(pnlEmail);
    pnlRun.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRun.add(pnlSettings);
    pnlRun.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRun.add(pnlRunButtons);
    pnlRun.add(Box.createRigidArea(FixedDim.x0_y5));
    // DatasetTable
    pnlDatasetTable.setLayout(new BoxLayout(pnlDatasetTable, BoxLayout.Y_AXIS));
    pnlDatasetTable.setBorder(BorderFactory.createEtchedBorder());
    pnlDatasetTable.add(phDatasetTable.getContainer());
    pnlDatasetTable.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlDatasetTable.add(pnlDatasetTableBody);
    // DatasetTableBody
    pnlDatasetTableBody.setLayout(new BoxLayout(pnlDatasetTableBody, BoxLayout.X_AXIS));
    // panel created on tab change
    // Table
    pnlTable.setLayout(new BoxLayout(pnlTable, BoxLayout.Y_AXIS));
    pnlTable.setBorder(new EtchedBorder("Datasets").getBorder());
    // UntitledTable
    pnlUntitledTable.setLayout(new BoxLayout(pnlUntitledTable, BoxLayout.Y_AXIS));
    // Email
    pnlEmail.setLayout(new BoxLayout(pnlEmail, BoxLayout.X_AXIS));
    pnlEmail.add(ctfEmailAddress.getComponent());
    // pnlSettings
    pnlSettings.setLayout(new BoxLayout(pnlSettings, BoxLayout.X_AXIS));
    pnlSettings.add(pnlParallelSettings);
    pnlSettings.add(Box.createHorizontalGlue());
    pnlSettings.add(stepPanel.getComponent());
    pnlSettings.add(Box.createHorizontalGlue());
    // RunButton
    pnlRunButtons.setLayout(new BoxLayout(pnlRunButtons, BoxLayout.X_AXIS));
    pnlRunButtons.add(Box.createHorizontalGlue());
    pnlRunButtons.add(btnRun.getComponent());
    pnlRunButtons.add(Box.createHorizontalGlue());
    pnlRunButtons.add(btnPause.getComponent());
    pnlRunButtons.add(Box.createHorizontalGlue());
    pnlRunButtons.add(btnResume.getComponent());
    pnlRunButtons.add(Box.createHorizontalGlue());
    pnlRunButtons.add(btnStartOver.getComponent());
    pnlRunButtons.add(Box.createHorizontalGlue());
    // ParallelSettings
    pnlParallelSettings.setLayout(new BoxLayout(pnlParallelSettings, BoxLayout.Y_AXIS));
    pnlParallelSettings.setBorder(new EtchedBorder("Run Actions").getBorder());
    pnlParallelSettings.add(cbCPUMachineList.getComponent());
    pnlParallelSettings.add(rbGPUMachineListOff.getComponent());
    pnlParallelSettings.add(rbGPUMachineListLocal.getComponent());
    pnlParallelSettings.add(rbGPUMachineList.getComponent());
    // RootName
    pnlRootName.setLayout(new BoxLayout(pnlRootName, BoxLayout.Y_AXIS));
    pnlRootName.setBorder(new EtchedBorder("Batchruntomo Project Files").getBorder());
    pnlRootName.add(ltfRootName.getComponent());
    pnlRootName.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlRootName.add(ftfRootDir.getRootPanel());
    // Templates
    pnlTemplates.setLayout(new BoxLayout(pnlTemplates, BoxLayout.X_AXIS));
    pnlTemplates.add(templatePanel.getComponent());
    pnlTemplates.add(Box.createHorizontalGlue());
    // DeliverToDirectory
    pnlDeliverToDirectory
      .setLayout(new BoxLayout(pnlDeliverToDirectory, BoxLayout.X_AXIS));
    pnlDeliverToDirectory.add(cbDeliverToDirectory.getComponent());
    pnlDeliverToDirectory.add(ftfDeliverToDirectory.getRootPanel());
    // align
    UIUtilities.alignComponentsX(pnlBatch, Component.LEFT_ALIGNMENT);
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
    processResult(ftfRootDir);
    stateChanged(null);
    statusChanged(status);
    mediator.setMethod(this, getProcessingMethod());
  }

  private void addListeners() {
    templatePanel.addListeners();
    cbDeliverToDirectory.addActionListener(this);
    templatePanel.addActionListener(this);
    cbCPUMachineList.addActionListener(this);
    rbGPUMachineListOff.addActionListener(this);
    rbGPUMachineListLocal.addActionListener(this);
    rbGPUMachineList.addActionListener(this);
    btnRun.addActionListener(this);
    ftfInputDirectiveFile.addResultListener(this);
    tabbedPane.addChangeListener(this);
    table.setTableListener(datasetDialog);
    btnStartOver.addActionListener(this);
    ctfEmailAddress.addActionListener(this);
    btnResume.addActionListener(this);
    btnPause.addActionListener(this);
    // This dialog can set the global status to open.
    addStatusChangeListener(stepPanel);
    table.msgStatusChangerStarted(this);
    // The step panel needs to listen for changes to the earliestRunStep.
    table.addStatusChangeListenerToRowList(stepPanel);
  }

  /**
   * Add listeners for StartOver button
   */
  public void addStatusChangeListener(final StatusChangeListener listener) {
    if (listener == null) {
      return;
    }
    boolean newElement = false;
    if (listeners == null) {
      synchronized (this) {
        if (listeners == null) {
          listeners = new Vector<StatusChangeListener>();
          newElement = true;
        }
      }
    }
    if (!newElement && listeners.contains(listener)) {
      return;
    }
    listeners.add(listener);
  }

  public void msgStatusChangerStarted(final StatusChanger changer) {
    // Listen to the monitor.
    changer.addStatusChangeListener(this);
    changer.addStatusChangeListener(stepPanel);
    table.msgStatusChangerStarted(changer);
  }

  public Container getContainer() {
    return pnlRoot;
  }

  public void msgLoadDone() {
    addListeners();
  }

  public void setParameters(final BatchRunTomoMetaData metaData) {
    if (!metaData.isRootNameNull()) {
      ltfRootName.setText(metaData.getRootName());
      ltfRootName.setEditable(false);
      ftfRootDir.setText(manager.getPropertyUserDir());
      ftfRootDir.setEditable(false);
      ftfDeliverToDirectory.setText(metaData.getDeliverToDirectory());
      ftfInputDirectiveFile.setText(metaData.getInputDirectiveFile());
      ftfInputDirectiveFile.checkpoint();
      table.setParameters(metaData);
      datasetDialog.setParameters(metaData.getDatasetMetaData());
      phDatasetTable.set(metaData.getDatasetTableHeader());
    }
    else {
      ltfRootName.setText("batch" + Utilities.getDateTimeStampRootName());
    }
  }

  public boolean isParamFileModifiable() {
    return ltfRootName.isEditable();
  }

  public boolean isParamFileEmpty() {
    return ltfRootName.isEmpty() || ftfRootDir.isEmpty();
  }

  public void disableDatasetFields() {
    ltfRootName.setEditable(false);
    ftfRootDir.setEditable(false);
  }

  public void getParameters(final UserConfiguration userConfiguration) {
    userConfiguration.setUseEmailAddress(ctfEmailAddress.isSelected());
    userConfiguration.setEmailAddress(ctfEmailAddress.getText());
  }

  /**
   * Get environment parameters.
   */
  public void getParameters() {
    cbCPUMachineList.setSelected(UserEnv.isParallelProcessing(null, AxisID.ONLY, null));
    if (UserEnv.isGpuProcessing(null, AxisID.ONLY, null)) {
      rbGPUMachineListLocal.setSelected(true);
    }
    else {
      rbGPUMachineListOff.setSelected(true);
    }
  }

  public void setParameters(final UserConfiguration userConfiguration,
    final boolean newDataset) {
    if (newDataset) {
      templatePanel.setParameters(userConfiguration);
    }
    ctfEmailAddress.setSelected(userConfiguration.isUseEmailAddress());
    ctfEmailAddress.setText(userConfiguration.getEmailAddress());
  }

  public void getParameters(final BatchRunTomoMetaData metaData) {
    metaData.setDeliverToDirectory(ftfDeliverToDirectory.getFile());
    metaData.setInputDirectiveFile(ftfInputDirectiveFile.getFile());
    table.getParameters(metaData);
    datasetDialog.getParameters(metaData.getDatasetMetaData());
    metaData.setDatasetTableHeader(phDatasetTable);
  }

  public void setParameters(final BatchruntomoParam param) {
    cbDeliverToDirectory.setSelected(!param.isDeliverToDirectoryNull());
    if (cbDeliverToDirectory.isSelected()) {
      ftfDeliverToDirectory.setText(param.getDeliverToDirectory());
    }
    cbCPUMachineList.setSelected(!param.isCpuMachineListNull());
    if (param.isGpuMachineListNull()) {
      rbGPUMachineListOff.setSelected(true);
    }
    else if (param.gpuMachineListEquals(BatchruntomoParam.MACHINE_LIST_LOCAL_VALUE)) {
      rbGPUMachineListLocal.setSelected(true);
    }
    else {
      rbGPUMachineList.setSelected(true);
    }
    if (!param.isEmailAddressNull()) {
      ctfEmailAddress.setSelected(true);
      ctfEmailAddress.setText(param.getEmailAddress());
    }
    stepPanel.setParameters(param);
    updateDisplay();
  }

  public boolean getParameters(final BatchruntomoParam param, final boolean doValidation,
    final boolean forUpdate) {
    try {
      if (!forUpdate) {
        if (cbDeliverToDirectory.isSelected()) {
          param.setDeliverToDirectory(ftfDeliverToDirectory.getFile());
        }
        else {
          param.resetDeliverToDirectory();
        }
        if (!cbCPUMachineList.isSelected()) {
          param.setCPUMachineList(BatchruntomoParam.MACHINE_LIST_LOCAL_VALUE);
        }
        if (rbGPUMachineListOff.isSelected()) {
          param.resetGPUMachineList();
        }
        else if (rbGPUMachineListLocal.isSelected()) {
          param.setGPUMachineList(BatchruntomoParam.MACHINE_LIST_LOCAL_VALUE);
        }
        if (ctfEmailAddress.isSelected()) {
          param.setEmailAddress(ctfEmailAddress.getText(doValidation));
        }
        else {
          param.resetEmailAddress();
        }
      }
      StringBuilder errMsg = new StringBuilder();
      boolean deliverToDirectory = cbDeliverToDirectory.isSelected();
      table.getParameters(param, deliverToDirectory, errMsg);
      if (errMsg.length() > 0) {
        if (deliverToDirectory) {
          errMsg
            .append("\n\nEither change the name of the associated stacks, or go to the "
              + BatchRunTomoTab.BATCH.getQuotedLabel()
              + " tab and uncheck the "
              + ftfDeliverToDirectory.getQuotedLabel()
              + " check box.  Each dataset will be placed in the current location of its stack "
              + "file.");
        }
        else {
          errMsg
            .append("\n\nEither move these stacks, or go to the "
              + BatchRunTomoTab.BATCH.getQuotedLabel()
              + " tab and select a directory in the "
              + ftfDeliverToDirectory.getQuotedLabel()
              + " field.  Each dataset will be placed in its own directory under the directory "
              + "in this field.");
        }
        UIHarness.INSTANCE.openMessageDialog(manager, errMsg.toString(),
          "Datasets Cannot Share a Directory");
        return false;
      }
      if (!forUpdate) {
        stepPanel.getParameters(param);
      }
      return true;
    }
    catch (FieldValidationFailedException e) {
      return false;
    }
  }

  public void loadAutodocs() {
    // load global autodoc
    DirectiveFile directiveFile =
      DirectiveFile.getInstance(manager, axisID,
        FileType.BATCH_RUN_TOMO_GLOBAL_AUTODOC.getFile(manager, axisID), true);
    templatePanel.setParameters(directiveFile);
    datasetDialog.setValues(directiveFile);
    // load dataset autodocs
    table.loadAutodocs();
  }

  public boolean saveAutodocs(final boolean doValidation) {
    Autodoc graftedBaseAutodoc =
      BatchTool.graftDirectiveFiles(manager, ftfInputDirectiveFile.getFile(),
        templatePanel.getFiles());
    // save global autodoc
    File globalFile = FileType.BATCH_RUN_TOMO_GLOBAL_AUTODOC.getFile(manager, null);
    try {
      if (globalFile.exists()) {
        Utilities.deleteFile(globalFile, manager, axisID);
      }
      Autodoc autodoc = AutodocFactory.getWritableAutodocInstance(manager, globalFile);
      templatePanel.saveAutodoc(autodoc);
      if (!datasetDialog.saveAutodoc(autodoc, doValidation, datasetFieldDisplayer)) {
        return false;
      }
      autodoc.graftMergeGlobal(graftedBaseAutodoc);
      // Warning: After merging DO NOT write to autodoc because the grafted areas will be
      // shared with other autodocs.
      autodoc.write();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    // save dataset autodocs with the starting batch and default batch directive files
    // grafted on.
    return table.saveAutodocs(templatePanel, graftedBaseAutodoc, doValidation,
      (cbDeliverToDirectory.isSelected() ? ftfDeliverToDirectory.getFile() : null),
      stacksFieldDisplayer);
  }

  BatchRunTomoRow getFirstRow() {
    return table.getFirstRow();
  }

  /**
   * Handles any changes in the selection of the starting batch directive file and the
   * template files.
   *
   * @param init - true when this function is called during the creation of the dialog.
   */
  public void msgDirectivesChanged(final boolean init, boolean retainUserValues) {
    if (!init) {
      // See if the user has changed any values (and back up the changed values).
      boolean changed = false;
      if (table.backupIfChanged()) {
        changed = true;
      }
      if (datasetDialog.backupIfChanged()) {
        changed = true;
      }
      if (!retainUserValues && changed) {
        // Ask the user whether they want to keep the values they changed.
        retainUserValues =
          UIHarness.INSTANCE.openYesNoDialog(manager,
            "New batch directive/template values will be applied.  Keep your changed "
              + "values?", axisID);
      }
    }
    table.applyValues(retainUserValues, directiveFileCollection);
    datasetDialog.applyValues(retainUserValues, directiveFileCollection);
  }

  public void actionPerformed(final ActionEvent event) {
    String actionCommand = event.getActionCommand();
    if (actionCommand == null) {
      return;
    }
    if (templatePanel.equalsActionCommand(actionCommand)) {
      // refresh the shared directive file collection
      templatePanel.refreshDirectiveFileCollection();
      msgDirectivesChanged(false, false);
    }
    else if (actionCommand.equals(btnRun.getActionCommand())) {
      manager.batchruntomo(table.createRunKeys());
    }
    else if (actionCommand.equals(btnResume.getActionCommand())) {
      manager.resumeBatchruntomo(table.updateRunKeys());
    }
    else if (actionCommand.equals(cbCPUMachineList.getActionCommand())
      || actionCommand.equals(rbGPUMachineListOff.getActionCommand())
      || actionCommand.equals(rbGPUMachineListLocal.getActionCommand())
      || actionCommand.equals(rbGPUMachineList.getActionCommand())) {
      mediator.setMethod(this, getProcessingMethod(), getSecondaryProcessingMethod(),
        curTab == BatchRunTomoTab.RUN);
    }
    else if (actionCommand.equals(btnStartOver.getActionCommand())) {
      statusChanged(BatchRunTomoStatus.OPEN);
      if (listeners != null) {
        for (int i = 0; i < listeners.size(); i++) {
          listeners.get(i).statusChanged(status);
        }
      }
    }
    else if (actionCommand.equals(btnPause.getActionCommand())) {
      manager.pause(axisID);
    }
    else {
      updateDisplay();
    }
  }

  /**
   * Returns one of the two possible methods.  Always returns a processing method.
   */
  public ProcessingMethod getProcessingMethod() {
    if (cbCPUMachineList.isSelected()) {
      return ProcessingMethod.PP_CPU;
    }
    if (rbGPUMachineList.isSelected()) {
      return ProcessingMethod.PP_GPU;
    }
    if (rbGPUMachineListLocal.isSelected()) {
      return ProcessingMethod.LOCAL_GPU;
    }
    return ProcessingMethod.DEFAULT;
  }

  /**
   * Returns a processing method when there are two non-default methods in force,
   * otherwise returns null.
   */
  public ProcessingMethod getSecondaryProcessingMethod() {
    if (cbCPUMachineList.isSelected()) {
      // two non-default processing methods are in force
      if (rbGPUMachineList.isSelected()) {
        return ProcessingMethod.PP_GPU;
      }
      if (rbGPUMachineListLocal.isSelected()) {
        return ProcessingMethod.LOCAL_GPU;
      }
    }
    return null;
  }

  /**
   * No effect because queue is not available
   */
  public void disableGpu(final boolean disable) {}

  /**
   * No effect because the processing method is not used for running processes by etomo.
   */
  public void lockProcessingMethod(boolean lock) {}

  public String getRootName() {
    return ltfRootName.getText();
  }

  public File getRootDir() {
    return ftfRootDir.getFile();
  }

  public void processResult(final Object object) {
    if (object == ftfRootDir) {
      File rootLocation = ftfRootDir.getFile();
      if (rootLocation != null) {
        table.setCurrentDirectory(rootLocation.getAbsolutePath());
      }
      else {
        table.setCurrentDirectory(null);
      }
    }
    else if (object == ftfInputDirectiveFile
      && ftfInputDirectiveFile.isDifferentFromCheckpoint(false)) {
      ftfInputDirectiveFile.checkpoint();
      directiveFileCollection.setDirectiveFile(ftfInputDirectiveFile.getFile(),
        DirectiveFileType.BATCH);
      templatePanel.activateActions(false);
      templatePanel.clear();
      templatePanel.setParameters(directiveFileCollection
        .getDirectiveFile(DirectiveFileType.BATCH));
      templatePanel.activateActions(true);
      msgDirectivesChanged(false, false);
    }
  }

  public void expand(final ExpandButton button) {
    boolean expanded = button.isExpanded();
    if (button == phDatasetTable.getOpenCloseButton()) {
      pnlDatasetTableBody.setVisible(expanded);
    }
    UIHarness.INSTANCE.pack(manager);
  }

  public void pack() {
    // Prevent the table from expanding horizontally
    if (curTab == BatchRunTomoTab.DATASET) {
      pnlDatasetTableBody.removeAll();
      int datasetWidth = datasetDialog.getPreferredWidth();
      int tableWidth = table.getPreferredWidth();
      if (datasetWidth != 0 && tableWidth != 0 && datasetWidth > tableWidth) {
        int padding = (datasetWidth - tableWidth) / 2;
        pnlDatasetTableBody.add(Box.createHorizontalStrut(padding));
        pnlDatasetTableBody.add(pnlUntitledTable);
        pnlDatasetTableBody.add(Box.createHorizontalStrut(padding));
      }
      else {
        pnlDatasetTableBody.add(pnlUntitledTable);
      }
    }
  }

  public void expand(final GlobalExpandButton button) {}

  /**
   * Displays the dataset tab, if it is not displayed.
   */
  private void displayDataset() {
    if (curTab != BatchRunTomoTab.DATASET) {
      tabbedPane.setSelectedIndex(BatchRunTomoTab.DATASET.getIndex());
    }
  }

  /**
  * Displays the stack tab, if it is not displayed.
  */
  private void displayStacks() {
    if (curTab != BatchRunTomoTab.STACKS) {
      tabbedPane.setSelectedIndex(BatchRunTomoTab.STACKS.getIndex());
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
      pnlTable.removeAll();
      pnlUntitledTable.removeAll();
    }
    if (curTab == BatchRunTomoTab.BATCH) {
      pnlTabs[curIndex].add(pnlBatch);
    }
    else if (curTab == BatchRunTomoTab.STACKS) {
      pnlTabs[curIndex].add(pnlStacks);
      table.msgTabChanged(curTab);
      pnlStacks.add(pnlTable);
      pnlTable.add(table.getComponent());
    }
    else if (curTab == BatchRunTomoTab.DATASET) {
      pnlTabs[curIndex].add(pnlDataset);
      table.msgTabChanged(curTab);
      pnlUntitledTable.add(table.getComponent());
      UIUtilities.alignComponentsX(pnlDataset, Component.LEFT_ALIGNMENT);
    }
    else if (curTab == BatchRunTomoTab.RUN) {
      pnlTabs[curIndex].add(pnlRun);
      table.msgTabChanged(curTab);
      pnlRun.add(pnlTable);
      pnlTable.add(table.getComponent());
      UIUtilities.alignComponentsX(pnlRun, Component.LEFT_ALIGNMENT);
    }
    mediator.setMethod(this, getProcessingMethod(), getSecondaryProcessingMethod(),
      curTab == BatchRunTomoTab.RUN);
    UIHarness.INSTANCE.pack(axisID, manager);
  }

  private void updateDisplay() {
    ftfDeliverToDirectory.setEnabled(cbDeliverToDirectory.isSelected());
  }

  public void statusChanged(final Status status) {
    if (status == null || !(status instanceof BatchRunTomoStatus)) {
      return;
    }
    this.status = (BatchRunTomoStatus) status;
    boolean open = status == BatchRunTomoStatus.OPEN;
    ctfEmailAddress.setEditable(open);
    cbCPUMachineList.setEditable(open);
    rbGPUMachineListOff.setEditable(open);
    rbGPUMachineListLocal.setEditable(open);
    rbGPUMachineList.setEditable(open);
    btnRun.setEditable(open || status == BatchRunTomoStatus.STOPPED);
    ftfInputDirectiveFile.setEditable(open);
    cbDeliverToDirectory.setEditable(open);
    if (cbDeliverToDirectory.isEnabled()) {
      cbDeliverToDirectory.setEnabled(status != BatchRunTomoStatus.RUNNING);
    }
    ftfDeliverToDirectory.setEditable(open);
    templatePanel.setEditable(open);
    // Running - enable
    // pause
    btnPause.setEditable(status == BatchRunTomoStatus.RUNNING);
    // Killed/paused - enable:
    // resume
    // start over
    boolean killedPaused = status == BatchRunTomoStatus.KILLED_PAUSED;
    btnResume.setEditable(killedPaused);
    // Stopped - enable:
    // start over
    btnStartOver.setEditable(killedPaused || status == BatchRunTomoStatus.STOPPED);
    datasetDialog.statusChanged(this.status);
  }

  public void statusChanged(final StatusChangeEvent statusChangeEvent) {
    // No response to dataset-level or row-level events
  }

  private void setTooltips() {
    ReadOnlyAutodoc autodoc = null;
    try {
      autodoc =
        AutodocFactory.getInstance(manager, AutodocFactory.BATCH_RUN_TOMO, axisID, false);
    }
    catch (FileNotFoundException except) {
      except.printStackTrace();
    }
    catch (IOException except) {
      except.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    cbDeliverToDirectory.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.DELIVER_TO_DIRECTORY_TAG));
    ctfEmailAddress.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.EMAIL_ADDRESS_TAG));
    cbCPUMachineList.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.CPU_MACHINE_LIST_TAG));
    rbGPUMachineListLocal.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.GPU_MACHINE_LIST_TAG));
    rbGPUMachineList.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.GPU_MACHINE_LIST_TAG));
    ltfRootName.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.ROOT_NAME_TAG));
    ftfRootDir.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.ROOT_NAME_TAG));
    ftfDeliverToDirectory.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
      BatchruntomoParam.DELIVER_TO_DIRECTORY_TAG));
    rbGPUMachineListOff.setToolTipText("No GPU will be used.");
    btnRun.setToolTipText("Saves with validation and runs batchruntomo.");
    ftfInputDirectiveFile.setToolTipText("");
    btnStartOver.setToolTipText("Resets the current batchruntomo run");
    btnPause.setToolTipText("Finishes the current dataset and then stops.");
    btnResume.setToolTipText("Continues the current batchruntomo run.");
  }

  private static final class DatasetFieldDisplayer implements FieldDisplayer {
    final BatchRunTomoDialog displayer;

    private DatasetFieldDisplayer(final BatchRunTomoDialog displayer) {
      this.displayer = displayer;
    }

    public void display() {
      displayer.displayDataset();
    }
  }

  private static final class StacksFieldDisplayer implements FieldDisplayer {
    final BatchRunTomoDialog displayer;

    private StacksFieldDisplayer(final BatchRunTomoDialog displayer) {
      this.displayer = displayer;
    }

    public void display() {
      displayer.displayStacks();
    }
  }
}
