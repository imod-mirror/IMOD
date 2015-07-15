package etomo.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import etomo.BatchRunTomoManager;
import etomo.EtomoDirector;
import etomo.comscript.BatchruntomoParam;
import etomo.logic.AutodocAttributeRetriever;
import etomo.logic.BatchTool;
import etomo.logic.DatasetTool;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.DirectiveFileInterface;
import etomo.storage.LogFile;
import etomo.storage.autodoc.Autodoc;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.BatchRunTomoDatasetStatus;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.BatchRunTomoRowMetaData;
import etomo.type.BatchRunTomoStatus;
import etomo.type.EtomoAutodoc;
import etomo.type.FileType;
import etomo.type.Run3dmodMenuOptions;
import etomo.type.Status;
import etomo.type.StatusChangeListener;
import etomo.type.StatusChanger;
import etomo.type.UserConfiguration;
import etomo.ui.BatchRunTomoTab;
import etomo.ui.FieldDisplayer;
import etomo.ui.PreferredTableSize;
import etomo.ui.SharedStrings;
import etomo.util.Utilities;

/**
 * <p>Description: A row of the BatchRunTomo table.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
final class BatchRunTomoRow implements Highlightable, Run3dmodButtonContainer,
  StatusChangeListener {
  private static final String SURFACES_TO_ANALYZE_TWO = "2";
  private static final String SURFACES_TO_ANALYZE_ONE = "1";
  private static final URL IMOD_ICON_URL = ClassLoader
    .getSystemResource("images/b3dicon.png");
  private static final URL IMOD_DISABLED_ICON_URL = ClassLoader
    .getSystemResource("images/b3dicon-disabled.png");
  private static final URL IMOD_PRESSED_ICON_URL = ClassLoader
    .getSystemResource("images/b3dicon-pressed.png");
  private static final URL ETOMO_ICON_URL = ClassLoader
    .getSystemResource("images/etomoicon.png");
  private static final URL ETOMO_DISABLED_ICON_URL = ClassLoader
    .getSystemResource("images/etomoicon-disabled.png");
  private static final URL ETOMO_PRESSED_ICON_URL = ClassLoader
    .getSystemResource("images/etomoicon-pressed.png");
  private static final String EDIT_DATASET_VALUE = "   Set";

  private final CheckBoxCell cbcBoundaryModel = new CheckBoxCell();
  private final CheckBoxCell cbcDual = new CheckBoxCell();
  private final CheckBoxCell cbcMontage = new CheckBoxCell();
  private final FieldCell fcSkip = FieldCell.getEditableInstance();
  private final FieldCell fcbskip = FieldCell.getEditableInstance();
  private final CheckBoxCell cbcSurfacesToAnalyze = new CheckBoxCell();
  private final FieldCell fcEditDataset = FieldCell.getIneditableInstance();
  private final FieldCell fcStatus = FieldCell.getIneditableInstance();
  private final FieldCell fcEndingStep = FieldCell.getIneditableInstance();
  private final CheckBoxCell cbcRun = new CheckBoxCell();
  private final MinibuttonCell mbcEtomo = MinibuttonCell.getInstance(new ImageIcon(
    ETOMO_ICON_URL));
  private final MinibuttonCell mbc3dmodA = MinibuttonCell.getRun3dmodInstance(
    new ImageIcon(IMOD_ICON_URL), this);
  private final MinibuttonCell mbc3dmodB = MinibuttonCell.getRun3dmodInstance(
    new ImageIcon(IMOD_ICON_URL), this);
  private final HeaderCell hcNumber = new HeaderCell();
  private final ButtonCell bcEditDataset = ButtonCell.getToggleInstance("Open");
  private final ActionListener listener = new RowListener(this);

  private final JPanel panel;
  private final GridBagLayout layout;
  private final GridBagConstraints constraints;
  private final HighlighterButton hbRow;
  private final FieldCell fcStack;
  private final BatchRunTomoManager manager;
  private final String stackID;

  private int imodIndexA = -1;
  private int imodIndexB = -1;
  private BatchRunTomoDatasetDialog datasetDialog = null;
  private BatchRunTomoRowMetaData metaData = null;
  private BatchRunTomoStatus status = null;
  private Vector<StatusChanger> statusChangers = null;
  private boolean debug = false;

  private BatchRunTomoRow(final BatchRunTomoTable table, final JPanel panel,
    final GridBagLayout layout, final GridBagConstraints constraints, final int number,
    final File stack, final BatchRunTomoRow prevRow, final boolean overridePrevRow,
    final boolean dual, final BatchRunTomoManager manager, final String stackID,
    final PreferredTableSize preferredTableSize) {
    this.panel = panel;
    this.layout = layout;
    this.constraints = constraints;
    this.manager = manager;
    this.stackID = stackID;
    hcNumber.setText(number);
    hbRow = HighlighterButton.getInstance(this, table);
    fcStack = FieldCell.getExpandableIneditableInstance(null);
    fcStack.setValue(stack);
    // icons
    if (IMOD_DISABLED_ICON_URL != null) {
      mbc3dmodA.setDisabledIcon(new ImageIcon(IMOD_DISABLED_ICON_URL));
    }
    if (IMOD_PRESSED_ICON_URL != null) {
      mbc3dmodA.setPressedIcon(new ImageIcon(IMOD_PRESSED_ICON_URL));
    }
    if (IMOD_DISABLED_ICON_URL != null) {
      mbc3dmodB.setDisabledIcon(new ImageIcon(IMOD_DISABLED_ICON_URL));
    }
    if (IMOD_PRESSED_ICON_URL != null) {
      mbc3dmodB.setPressedIcon(new ImageIcon(IMOD_PRESSED_ICON_URL));
    }
    if (ETOMO_DISABLED_ICON_URL != null) {
      mbcEtomo.setDisabledIcon(new ImageIcon(ETOMO_DISABLED_ICON_URL));
    }
    if (ETOMO_PRESSED_ICON_URL != null) {
      mbcEtomo.setPressedIcon(new ImageIcon(ETOMO_PRESSED_ICON_URL));
    }
    // preferred width
    if (preferredTableSize != null) {
      preferredTableSize.addColumn(BatchRunTomoTable.DatasetColumn.NUMBER.getIndex(),
        hcNumber);
      preferredTableSize.addColumn(BatchRunTomoTable.DatasetColumn.STACK.getIndex(),
        fcStack);
      preferredTableSize.addColumn(
        BatchRunTomoTable.DatasetColumn.EDIT_DATASET.getIndex(), bcEditDataset,
        fcEditDataset);
    }
    // init
    setDefaults();
    copy(prevRow);
    setTooltips(prevRow);
    // When overridePrevRow is true, overrideDual will replace prevRow dual axis.
    if (overridePrevRow) {
      cbcDual.setSelected(dual);
    }
    cbcRun.setSelected(true);
    // mbcEtomo.setEnabled(false);
    // directives
    cbcDual.setDirectiveDef(DirectiveDef.DUAL);
    cbcMontage.setDirectiveDef(DirectiveDef.MONTAGE);
    cbcSurfacesToAnalyze.setDirectiveDef(DirectiveDef.SURFACES_TO_ANALYZE);
    fcSkip.setDirectiveDef(DirectiveDef.SKIP);
    fcbskip.setDirectiveDef(DirectiveDef.SKIP);
    updateDisplay();
    statusChanged(status);
  }

  static BatchRunTomoRow getInstance(final BatchRunTomoTable table, final JPanel panel,
    final GridBagLayout layout, final GridBagConstraints constraints, final int number,
    final File stack, final BatchRunTomoRow prevRow, final boolean overridePrevRow,
    final boolean dual, final BatchRunTomoManager manager, final String stackID,
    final PreferredTableSize datasetWidth) {
    BatchRunTomoRow instance =
      new BatchRunTomoRow(table, panel, layout, constraints, number, stack, prevRow,
        overridePrevRow, dual, manager, stackID, datasetWidth);
    instance.addListeners();
    return instance;
  }

  static BatchRunTomoRow getDefaultsInstance() {
    return new BatchRunTomoRow(null, null, null, null, -1, null, null, false, false,
      null, null, null);
  }

  void copy(final BatchRunTomoRow prevRow) {
    if (prevRow != null) {
      cbcDual.setSelected(prevRow.cbcDual.isSelected());
      cbcMontage.setSelected(prevRow.cbcMontage.isSelected());
      cbcSurfacesToAnalyze.setSelected(prevRow.cbcSurfacesToAnalyze.isSelected());
    }
    updateDisplay();
  }

  boolean isDual() {
    return cbcDual.isSelected();
  }

  private void addListeners() {
    // give each listened to field an unique action command
    mbc3dmodA.setActionCommand(mbc3dmodA.toString());
    mbc3dmodB.setActionCommand(mbc3dmodB.toString());
    cbcDual.setActionCommand(cbcDual.toString());
    mbcEtomo.setActionCommand(mbcEtomo.toString());
    cbcBoundaryModel.setActionCommand(cbcBoundaryModel.toString());
    bcEditDataset.setActionCommand(bcEditDataset.toString());
    // set listeners
    mbc3dmodA.addActionListener(listener);
    mbc3dmodB.addActionListener(listener);
    cbcDual.addActionListener(listener);
    mbcEtomo.addActionListener(listener);
    cbcBoundaryModel.addActionListener(listener);
    bcEditDataset.addActionListener(listener);
  }

  void msgStatusChangerAvailable(final StatusChanger changer) {
    changer.addStatusChangeListener(this);
    if (datasetDialog != null) {
      datasetDialog.msgStatusChangerAvailable(changer);
    }
    // datasetDialog can be added and deleted
    if (statusChangers == null) {
      synchronized (this) {
        if (statusChangers == null) {
          statusChangers = new Vector<StatusChanger>();
        }
      }
    }
    statusChangers.add(changer);
  }

  /**
   * Opens 3dmod on the A axis
   * @param modelFileType
   * @return the file created from modelFileType
   */
  File imod(final FileType modelFileType) {
    return imod(modelFileType, AxisID.FIRST, cbcDual.isSelected(), null);
  }

  /**
   * Opens 3dmod on the A axis
   * @param modelFile
   */
  void imod(final File modelFile) {
    imod(modelFile, AxisID.FIRST, cbcDual.isSelected(), null);
  }

  /**
   * Opens 3dmod.
   * @param modelFileType
   * @param axisID
   * @param dual
   * @param run3dmodMenuOptions
   * @return the file created from modelFileType
   */
  private File imod(final FileType modelFileType, final AxisID axisID,
    final boolean dual, final Run3dmodMenuOptions run3dmodMenuOptions) {
    File stack = new File(fcStack.getExpandedValue());
    if (modelFileType != null) {
      File modelFile =
        new File(stack.getParentFile(), BatchTool.getModelFileName(modelFileType,
          stack.getName(), dual));
      imod(modelFile, axisID, dual, run3dmodMenuOptions);
      return modelFile;
    }
    // No model is required
    imod(axisID, dual, run3dmodMenuOptions);
    return null;
  }

  /**
   * Opens 3dmod without a model.
   * @param axisID
   * @param dual
   * @param run3dmodMenuOptions
   */
  private void imod(final AxisID axisID, final boolean dual,
    final Run3dmodMenuOptions run3dmodMenuOptions) {
    imod((File) null, axisID, dual, run3dmodMenuOptions);
  }

  /**
   * Opens 3dmod, with a model if modelFile is set.
   * @param modelFile
   * @param axisID
   * @param dual
   * @param run3dmodMenuOptions
   */
  private void imod(final File modelFile, final AxisID axisID, final boolean dual,
    final Run3dmodMenuOptions run3dmodMenuOptions) {
    File stackFile = DatasetTool.getStackFile(fcStack.getExpandedValue(), axisID, dual);
    if (axisID == AxisID.SECOND) {
      imodIndexB =
        manager.imod(stackFile, axisID, imodIndexB, modelFile, dual, run3dmodMenuOptions);
    }
    else {
      imodIndexA =
        manager.imod(stackFile, axisID, imodIndexA, modelFile, dual, run3dmodMenuOptions);
    }
  }

  public void action(final String actionCommand,
    final Deferred3dmodButton deferred3dmodButton,
    final Run3dmodMenuOptions run3dmodMenuOptions) {
    if (actionCommand == null) {
      return;
    }
    if (actionCommand.equals(cbcDual.getActionCommand())) {
      updateDisplay();
    }
    else {
      boolean dual = cbcDual.isSelected();
      if (actionCommand.equals(mbc3dmodA.getActionCommand())) {
        FileType modelFile = null;
        if (cbcBoundaryModel.isSelected()) {
          modelFile = FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL;
        }
        imod(modelFile, AxisID.FIRST, dual, run3dmodMenuOptions);
      }
      else if (actionCommand.equals(mbc3dmodB.getActionCommand())) {
        // The model is only opened for the A axis
        imod(AxisID.SECOND, dual, run3dmodMenuOptions);
      }
      else if (actionCommand.equals(mbcEtomo.getActionCommand())) {
        EtomoDirector.INSTANCE.openTomogram(
          DatasetTool.getDatasetFile(
            DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.FIRST, dual),
            dual), true, null, mbcEtomo);
      }
      else if (actionCommand.equals(cbcBoundaryModel.getActionCommand())
        && cbcBoundaryModel.isSelected()) {
        File stack = new File(fcStack.getExpandedValue());
        // The model is only opened for the A axis
        if (imodIndexA != -1) {
          manager.imodModel(AxisID.FIRST, imodIndexA, stack.getParentFile(),
            stack.getName(), FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL, dual);
        }
      }
      else if (actionCommand.equals(bcEditDataset.getActionCommand())) {
        if (datasetDialog == null) {
          datasetDialog =
            BatchRunTomoDatasetDialog.getRowInstance(manager, DatasetTool.getDatasetFile(
              DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.FIRST, dual),
              dual), this);
          fcEditDataset.setValue("   Set");
          if (statusChangers != null) {
            for (int i = 0; i < statusChangers.size(); i++) {
              datasetDialog.msgStatusChangerAvailable(statusChangers.get(i));
            }
          }
        }
        else {
          datasetDialog.setVisible(true);
          bcEditDataset.setSelected(true);
        }
      }
    }
  }

  void remove() {
    hcNumber.remove();
    hbRow.remove();
    fcStack.remove();
    cbcBoundaryModel.remove();
    cbcDual.remove();
    cbcMontage.remove();
    fcSkip.remove();
    fcbskip.remove();
    cbcSurfacesToAnalyze.remove();
    fcEditDataset.remove();
    fcStatus.remove();
    fcEndingStep.remove();
    cbcRun.remove();
    mbcEtomo.remove();
    mbc3dmodA.remove();
    mbc3dmodB.remove();
    bcEditDataset.remove();
  }

  void delete() {
    if (metaData != null) {
      metaData.setRowNumber(null);
    }
    deleteDataset();
  }

  void deleteDataset() {
    if (datasetDialog != null) {
      datasetDialog.setVisible(false);
      datasetDialog.delete();
      datasetDialog = null;
      bcEditDataset.setSelected(false);
      fcEditDataset.setValue("");
    }
  }

  private void updateDisplay() {
    boolean dual = cbcDual.isSelected();
    fcbskip.setEnabled(dual);
    mbc3dmodB.setEnabled(dual);
  }

  public void statusChanged(final Status status) {
    if (status instanceof BatchRunTomoStatus) {
      this.status = (BatchRunTomoStatus) status;
      boolean open = status == null || status == BatchRunTomoStatus.OPEN;
      cbcBoundaryModel.setEditable(open);
      cbcMontage.setEditable(open);
      fcSkip.setEditable(open);
      fcbskip.setEditable(open);
      cbcSurfacesToAnalyze.setEditable(open);
      mbcEtomo.setEditable(open);
      mbc3dmodA.setEditable(open);
      mbc3dmodB.setEditable(open);
      bcEditDataset.setEditable(open);
      if (open) {
        cbcRun.setEditable(true);
      }
      else {
        cbcRun.setEditable(false);
      }
    }
    else if (status instanceof BatchRunTomoDatasetStatus) {
      fcStatus.setValue(status.toString());
    }
  }

  void display(final Viewport viewport, final BatchRunTomoTab tab) {
    // See if index is in the viewport
    if (viewport.inViewport(hcNumber.getInt() - 1)) {
      constraints.gridwidth = 1;
      hcNumber.add(panel, layout, constraints);
      if (tab == BatchRunTomoTab.STACKS) {
        hbRow.add(panel, layout, constraints);
      }
      constraints.gridwidth = 2;
      fcStack.add(panel, layout, constraints);
      constraints.gridwidth = 1;
      if (tab == BatchRunTomoTab.STACKS) {
        cbcDual.add(panel, layout, constraints);
        cbcMontage.add(panel, layout, constraints);
        fcSkip.add(panel, layout, constraints);
        fcbskip.add(panel, layout, constraints);
        cbcBoundaryModel.add(panel, layout, constraints);
        cbcSurfacesToAnalyze.add(panel, layout, constraints);
        mbc3dmodA.add(panel, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        mbc3dmodB.add(panel, layout, constraints);
      }
      else if (tab == BatchRunTomoTab.DATASET) {
        bcEditDataset.add(panel, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        fcEditDataset.add(panel, layout, constraints);
      }
      else {
        fcStatus.add(panel, layout, constraints);
        fcEndingStep.add(panel, layout, constraints);
        cbcRun.add(panel, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        mbcEtomo.add(panel, layout, constraints);
      }
    }
  }

  void expandStack(final boolean expanded) {
    fcStack.expand(expanded);
  }

  public void highlight(final boolean highlight) {
    fcStack.setHighlight(highlight);
    cbcBoundaryModel.setHighlight(highlight);
    cbcDual.setHighlight(highlight);
    cbcMontage.setHighlight(highlight);
    fcSkip.setHighlight(highlight);
    fcbskip.setHighlight(highlight);
    cbcSurfacesToAnalyze.setHighlight(highlight);
    fcEditDataset.setHighlight(highlight);
    fcStatus.setHighlight(highlight);
    fcEndingStep.setHighlight(highlight);
    cbcRun.setHighlight(highlight);
  }

  boolean equalsStackID(final String stackID) {
    return this.stackID.equals(stackID);
  }

  public void setParameters(final BatchRunTomoMetaData metaData) {
    BatchRunTomoRowMetaData rowMetaData = metaData.getRowMetaData(stackID);
    this.metaData = rowMetaData;
    cbcDual.setValue(rowMetaData.isDual());
    fcbskip.setValue(rowMetaData.getBskip());
    cbcRun.setValue(rowMetaData.isRun());
    boolean isDatasetDialog = rowMetaData.isDatasetDialog();
    bcEditDataset.setSelected(isDatasetDialog);
    if (isDatasetDialog) {
      fcEditDataset.setValue(EDIT_DATASET_VALUE);
    }
    else {
      fcEditDataset.setValue();
    }
    if (isDatasetDialog) {
      datasetDialog = BatchRunTomoDatasetDialog.getSavedRowInstance(manager, this);
      datasetDialog.setParameters(rowMetaData.getDatasetMetaData());
      if (statusChangers != null) {
        for (int i = 0; i < statusChangers.size(); i++) {
          datasetDialog.msgStatusChangerAvailable(statusChangers.get(i));
        }
      }
    }
    updateDisplay();
  }

  public void getParameters(final BatchRunTomoMetaData metaData) {
    BatchRunTomoRowMetaData rowMetaData = metaData.getRowMetaData(stackID);
    this.metaData = rowMetaData;
    rowMetaData.setRowNumber(hcNumber.getText());
    rowMetaData.setDual(cbcDual.isSelected());
    rowMetaData.setBskip(fcbskip.getValue());
    rowMetaData.setRun(cbcRun.isSelected());
    rowMetaData.setDatasetDialog(datasetDialog != null);
    if (datasetDialog != null) {
      datasetDialog.getParameters(rowMetaData.getDatasetMetaData());
    }
  }

  public void getParameters(final BatchruntomoParam param,
    final boolean deliverToDirectory, final StringBuilder errMsg) {
    if (!cbcRun.isSelected()) {
      return;
    }
    File stack = new File(fcStack.getExpandedValue());
    param.addDirectiveFile(new File(stack.getParent(), getBatchDirectiveFileName()));
    String rootName = DatasetTool.getDatasetName(stack.getName(), cbcDual.isSelected());
    param.addRootName(rootName, deliverToDirectory, cbcDual.isSelected(), errMsg);
    if (!param.addCurrentLocation(stack.getParent(), !deliverToDirectory, errMsg)) {
      errMsg.append(": " + stack.getAbsolutePath() + ".  ");
    }
  }

  private String getBatchDirectiveFileName() {
    return manager.getName() + "_"
      + DatasetTool.getDatasetName(fcStack.getContractedValue(), cbcDual.isSelected())
      + ".adoc";
  }

  void loadAutodoc() {
    DirectiveFile directiveFile =
      DirectiveFile.getInstance(manager, null,
        new File(new File(fcStack.getExpandedValue()).getParent(),
          getBatchDirectiveFileName()), true);
    setValues(directiveFile);
    DirectiveDef directiveDef = DirectiveDef.SKIP;
    if (directiveFile.contains(directiveDef)) {
      fcSkip.setValue(directiveFile.getValue(directiveDef));
    }
    if (directiveFile.contains(directiveDef, AxisID.SECOND)) {
      fcbskip.setValue(directiveFile.getValue(directiveDef, AxisID.SECOND));
    }
    if (directiveFile.containsValue(DirectiveDef.RAW_BOUNDARY_MODEL_FOR_SEED_FINDING)
      || directiveFile.containsValue(DirectiveDef.RAW_BOUNDARY_MODEL_FOR_PATCH_TRACKING)) {
      cbcBoundaryModel.setSelected(true);
    }
    if (datasetDialog != null) {
      datasetDialog.setValues(directiveFile);
    }
  }

  boolean saveAutodoc(final TemplatePanel templatePanel,
    final Autodoc graftedBaseAutodoc, final boolean doValidation,
    final File deliverToDirectory, final FieldDisplayer fieldDisplayer) {
    File stack = new File(fcStack.getExpandedValue());
    File originalLocation = stack.getParentFile();
    File file = new File(originalLocation, getBatchDirectiveFileName());
    try {
      if (file.exists()) {
        Utilities.deleteFile(file, manager, null);
      }
      Autodoc autodoc = AutodocFactory.getWritableAutodocInstance(manager, file);
      BatchTool.saveFieldToAutodoc(cbcDual, autodoc);
      BatchTool.saveFieldToAutodoc(cbcMontage, autodoc);
      BatchTool.saveFieldToAutodoc(fcSkip, autodoc);
      BatchTool.saveFieldToAutodoc(fcbskip, AxisID.SECOND, AxisType.DUAL_AXIS, autodoc);
      if (cbcBoundaryModel.isSelected()) {
        String boundaryModelName =
          BatchTool.getModelFileName(FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL,
            stack.getName(), cbcDual.isSelected());
        // Validation: make sure the boundary model file exists
        if (doValidation
          && !new File(originalLocation, boundaryModelName).exists()
          && (deliverToDirectory == null || !(new File(new File(deliverToDirectory,
            DatasetTool.getDatasetName(stack.getName(), cbcDual.isSelected())),
            boundaryModelName)).exists())) {
          UIHarness.INSTANCE.openMessageDialog(manager, cbcBoundaryModel, "Row# "
            + hcNumber.getText() + ":  Missing boundary model file - "
            + boundaryModelName, "Missing File", fieldDisplayer);
          return false;
        }
        if (BatchTool.needInAutodoc(cbcBoundaryModel)) {
          autodoc.addNameValuePairAttribute(
            DirectiveDef.RAW_BOUNDARY_MODEL_FOR_SEED_FINDING.getDirective(null, null),
            boundaryModelName);
          autodoc.addNameValuePairAttribute(
            DirectiveDef.RAW_BOUNDARY_MODEL_FOR_PATCH_TRACKING.getDirective(null, null),
            boundaryModelName);
        }
      }
      BatchTool.saveFieldToAutodoc(cbcSurfacesToAnalyze, cbcSurfacesToAnalyze
        .isSelected() ? SURFACES_TO_ANALYZE_TWO : SURFACES_TO_ANALYZE_ONE, autodoc);
      if (templatePanel != null) {
        templatePanel.saveAutodoc(autodoc);
      }
      if (datasetDialog != null) {
        if (!datasetDialog.saveAutodoc(autodoc, doValidation, null)) {
          return false;
        }
      }
      else {
        BatchRunTomoDatasetDialog globalDatasetDialog =
          BatchRunTomoDatasetDialog.getGlobalInstance();
        if (globalDatasetDialog != null) {
          // The global is validated when the main .adoc file is saved
          if (!globalDatasetDialog.saveAutodoc(autodoc, false, null)) {
            return false;
          }
        }
      }
      autodoc.graftMergeGlobal(graftedBaseAutodoc);
      // Warning: After merging DO NOT write to autodoc because the grafted areas are
      // being shared with other autodocs.
      autodoc.write();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    return true;
  }

  boolean isHighlighted() {
    return hbRow.isHighlighted();
  }

  void selectHighlightButton() {
    hbRow.setSelected(true);
  }

  /**
   * Check isDifferentFromCheckpoint on all data entry fields that are loaded from
   * directive files.
   *
   * @return true if any field's isDifferentFromCheckpoint function returned true
   */
  boolean backupIfChanged() {
    boolean changed = false;
    if (cbcDual.isDifferentFromCheckpoint(true)) {
      cbcDual.backup();
      changed = true;

    }
    if (cbcMontage.isDifferentFromCheckpoint(true)) {
      cbcMontage.backup();
      changed = true;
    }
    if (cbcSurfacesToAnalyze.isDifferentFromCheckpoint(true)) {
      cbcSurfacesToAnalyze.backup();
      changed = true;
    }
    if (datasetDialog != null && datasetDialog.backupIfChanged()) {
      changed = true;
    }
    return changed;
  }

  void applyValues(final boolean retainUserValues,
    final UserConfiguration userConfiguration,
    final DirectiveFileCollection directiveFileCollection) {
    // to apply values and highlights, start with a clean slate
    cbcDual.clear();
    cbcMontage.clear();
    cbcSurfacesToAnalyze.clear();
    setDefaults();
    // no default values to apply to table
    // Apply settings values
    setValues(userConfiguration);
    // Apply the directive collection values
    setValues(directiveFileCollection);
    // checkpoint template/starting batch
    cbcDual.checkpoint();
    cbcMontage.checkpoint();
    cbcSurfacesToAnalyze.checkpoint();
    // If the user wants to retain their values, apply backed up values and then delete
    // them.
    if (retainUserValues) {
      cbcDual.restoreFromBackup();
      cbcMontage.restoreFromBackup();
      cbcSurfacesToAnalyze.restoreFromBackup();
      updateDisplay();
    }
    // no field highlight values to set in table
    // Dataset dialog
    if (datasetDialog != null) {
      datasetDialog.applyValues(retainUserValues, directiveFileCollection);
    }
    updateDisplay();
  }

  void setNumber(final int input) {
    hcNumber.setText(input);
  }

  private void setDefaults() {
    cbcDual.setSelected(true);
    updateDisplay();
  }

  /**
   * Set values from the directive file collection - only for directives that are present.
   *
   * @param directiveFiles - templates and the base directive file,
   *                       or a single directive file
   */
  void setValues(final DirectiveFileInterface directiveFiles) {
    setValue(directiveFiles, DirectiveDef.DUAL, cbcDual);
    setValue(directiveFiles, DirectiveDef.MONTAGE, cbcMontage);
    if (directiveFiles.contains(cbcSurfacesToAnalyze.getDirectiveDef())) {
      String surfacesToAnalyze =
        directiveFiles.getValue(DirectiveDef.SURFACES_TO_ANALYZE);
      cbcSurfacesToAnalyze.setSelected(surfacesToAnalyze != null
        && surfacesToAnalyze.equals(SURFACES_TO_ANALYZE_TWO));
    }
    else {
      cbcSurfacesToAnalyze.setSelected(false);
    }
  }

  private boolean setValue(final DirectiveFileInterface directiveFiles,
    final DirectiveDef directiveDef, final CheckBoxCell cell) {
    if (directiveFiles.contains(directiveDef)) {
      cell.setSelected(directiveFiles.isValue(directiveDef));
      return true;
    }
    return false;
  }

  void setValues(final UserConfiguration userConfiguration) {
    cbcDual.setSelected(!userConfiguration.getSingleAxis());
    cbcMontage.setSelected(userConfiguration.getMontage());
    updateDisplay();
  }

  private void setTooltips(final BatchRunTomoRow prevRow) {
    if (prevRow == null) {
      cbcBoundaryModel.setToolTipText(AutodocAttributeRetriever.INSTANCE
        .getTooltip(DirectiveDef.RAW_BOUNDARY_MODEL_FOR_SEED_FINDING));
      cbcBoundaryModel.addTooltip(AutodocAttributeRetriever.INSTANCE
        .getTooltip(DirectiveDef.RAW_BOUNDARY_MODEL_FOR_PATCH_TRACKING));
      cbcDual.setToolTipText(AutodocAttributeRetriever.INSTANCE.getTooltip(cbcDual
        .getDirectiveDef()));
      cbcMontage.setToolTipText(AutodocAttributeRetriever.INSTANCE.getTooltip(cbcMontage
        .getDirectiveDef()));
      fcSkip.setToolTipText(AutodocAttributeRetriever.INSTANCE.getTooltip(fcSkip
        .getDirectiveDef()));
      fcbskip.setToolTipText(AutodocAttributeRetriever.INSTANCE.getTooltip(fcbskip
        .getDirectiveDef()));
      cbcSurfacesToAnalyze.setToolTipText(AutodocAttributeRetriever.INSTANCE
        .getTooltip(cbcSurfacesToAnalyze.getDirectiveDef()));
      ReadOnlyAutodoc autodoc = null;
      try {
        autodoc =
          AutodocFactory.getInstance(manager, AutodocFactory.BATCH_RUN_TOMO, AxisID.ONLY,
            false);
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
      fcEndingStep.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
        BatchruntomoParam.ENDING_STEP_TAG));
    }
    else {
      cbcBoundaryModel.setTooltip(prevRow.cbcBoundaryModel);
      cbcDual.setTooltip(prevRow.cbcDual);
      cbcMontage.setTooltip(prevRow.cbcMontage);
      fcSkip.setTooltip(prevRow.fcSkip);
      fcbskip.setTooltip(prevRow.fcbskip);
      cbcSurfacesToAnalyze.setTooltip(prevRow.cbcSurfacesToAnalyze);
      fcEndingStep.setTooltip(prevRow.fcEndingStep);
    }
    fcEditDataset.setToolTipText(SharedStrings.EDIT_DATASET_TOOLTIP);
    fcStatus.setToolTipText("Completion status of the dataset");
    cbcRun.setToolTipText("This dataset will be included in the batchruntomo run");
    mbcEtomo.setToolTipText("Opens a tab in eTomo contain this dataset");
    mbc3dmodA.setToolTipText(SharedStrings.IMOD_A_TOOLTIP);
    mbc3dmodB.setToolTipText(SharedStrings.IMOD_B_TOOLTIP);
    bcEditDataset.setToolTipText("Use dataset-specific values.");
    fcStack.setToolTipText(SharedStrings.STACK_TOOLTIP);
  }

  private static final class RowListener implements ActionListener {
    private final BatchRunTomoRow row;

    private RowListener(final BatchRunTomoRow row) {
      this.row = row;
    }

    public void actionPerformed(final ActionEvent event) {
      row.action(event.getActionCommand(), null, null);
    }
  }
}
