package etomo.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import etomo.BatchRunTomoManager;
import etomo.EtomoDirector;
import etomo.comscript.BatchruntomoParam;
import etomo.logic.BatchTool;
import etomo.logic.DatasetTool;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.DirectiveFileInterface;
import etomo.storage.LogFile;
import etomo.storage.autodoc.Autodoc;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.BatchRunTomoRowMetaData;
import etomo.type.EtomoNumber;
import etomo.type.Run3dmodMenuOptions;
import etomo.type.UserConfiguration;
import etomo.ui.BatchRunTomoTab;
import etomo.ui.PreferredTableSize;
import etomo.util.Utilities;

/**
 * <p>Description: A row of the BatchRunTomo table.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
final class BatchRunTomoRow implements Highlightable, Run3dmodButtonContainer {
  private static final URL IMOD_ICON_URL =
      ClassLoader.getSystemResource("images/b3dicon.png");
  private static final URL IMOD_DISABLED_ICON_URL =
      ClassLoader.getSystemResource("images/b3dicon-disabled.png");
  private static final URL IMOD_PRESSED_ICON_URL =
      ClassLoader.getSystemResource("images/b3dicon-pressed.png");
  private static final URL ETOMO_ICON_URL =
      ClassLoader.getSystemResource("images/etomoicon.png");
  private static final URL ETOMO_DISABLED_ICON_URL =
      ClassLoader.getSystemResource("images/etomoicon-disabled.png");
  private static final URL ETOMO_PRESSED_ICON_URL =
      ClassLoader.getSystemResource("images/etomoicon-pressed.png");
  private static final String EDIT_DATASET_VALUE = "   Set";

  private final CheckBoxCell cbcBoundaryModel = new CheckBoxCell();
  private final CheckBoxCell cbcDual = new CheckBoxCell();
  private final CheckBoxCell cbcMontage = new CheckBoxCell();
  private final FieldCell fcSkip = FieldCell.getEditableInstance();
  private final FieldCell fcbskip = FieldCell.getEditableInstance();
  private final CheckBoxCell cbcTwoSurfaces = new CheckBoxCell();
  private final FieldCell fcEditDataset = FieldCell.getIneditableInstance();
  private final FieldCell fcStatus = FieldCell.getIneditableInstance();
  private final CheckBoxCell cbcRun = new CheckBoxCell();
  private final MinibuttonCell mbcEtomo =
      MinibuttonCell.getInstance(new ImageIcon(ETOMO_ICON_URL));
  private final MinibuttonCell mbc3dmodA =
      MinibuttonCell.getRun3dmodInstance(new ImageIcon(IMOD_ICON_URL), this);
  private final MinibuttonCell mbc3dmodB =
      MinibuttonCell.getRun3dmodInstance(new ImageIcon(IMOD_ICON_URL), this);
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
    fcStack = FieldCell.getExpandableInstance(null);
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
      preferredTableSize
          .addColumn(BatchRunTomoTable.DatasetColumn.NUMBER.getIndex(), hcNumber);
      preferredTableSize
          .addColumn(BatchRunTomoTable.DatasetColumn.STACK.getIndex(), fcStack);
      preferredTableSize
          .addColumn(BatchRunTomoTable.DatasetColumn.EDIT_DATASET.getIndex(),
              bcEditDataset, fcEditDataset);
    }
    // init
    setDefaults();
    copy(prevRow);
    // When overridePrevRow is true, overrideDual will replace prevRow dual axis.
    if (overridePrevRow) {
      cbcDual.setSelected(dual);
    }
    cbcRun.setSelected(true);
    mbcEtomo.setEnabled(false);
    //directives
    cbcDual.setDirectiveDef(DirectiveDef.DUAL);
    cbcMontage.setDirectiveDef(DirectiveDef.MONTAGE);
    fcSkip.setDirectiveDef(DirectiveDef.SKIP);
    fcbskip.setDirectiveDef(DirectiveDef.SKIP);
    cbcTwoSurfaces.setDirectiveDef(DirectiveDef.TWO_SURFACES);
    updateDisplay();
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
    return new BatchRunTomoRow(null, null, null, null, -1, null, null, false, false, null,
        null, null);
  }

  void copy(final BatchRunTomoRow prevRow) {
    if (prevRow != null) {
      cbcDual.setSelected(prevRow.cbcDual.isSelected());
      cbcMontage.setSelected(prevRow.cbcMontage.isSelected());
      cbcTwoSurfaces.setSelected(prevRow.cbcTwoSurfaces.isSelected());
    }
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
        imodIndexA = manager.imod(
            DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.FIRST, dual),
            AxisID.FIRST, imodIndexA, cbcBoundaryModel.isSelected(), dual,
            run3dmodMenuOptions);
      }
      else if (actionCommand.equals(mbc3dmodB.getActionCommand())) {
        // The model is only opened for the A axis
        imodIndexB = manager.imod(
            DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.SECOND, dual),
            AxisID.SECOND, imodIndexB, run3dmodMenuOptions);
      }
      else if (actionCommand.equals(mbcEtomo.getActionCommand())) {
        EtomoDirector.INSTANCE.openTomogram(DatasetTool.getDatasetFile(
            DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.FIRST, dual),
            dual), true, null, mbcEtomo);
      }
      else if (actionCommand.equals(cbcBoundaryModel.getActionCommand()) &&
          cbcBoundaryModel.isSelected()) {
        // The model is only opened for the A axis
        if (imodIndexA != -1) {
          manager.imodModel(AxisID.FIRST, imodIndexA, fcStack.getContractedValue(), dual);
        }
      }
      else if (actionCommand.equals(bcEditDataset.getActionCommand())) {
        if (datasetDialog == null) {
          datasetDialog = BatchRunTomoDatasetDialog.getRowInstance(manager, DatasetTool
              .getDatasetFile(DatasetTool
                      .getStackFile(fcStack.getExpandedValue(), AxisID.FIRST, dual),
                  dual), this);
          fcEditDataset.setValue("   Set");
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
    cbcTwoSurfaces.remove();
    fcEditDataset.remove();
    fcStatus.remove();
    cbcRun.remove();
    mbcEtomo.remove();
    mbc3dmodA.remove();
    mbc3dmodB.remove();
    bcEditDataset.remove();
  }

  void delete() {
    if (metaData != null) {
      metaData.setDisplay(false);
    }
    deleteDataset();
  }

  void deleteDataset() {
    if (datasetDialog != null) {
      datasetDialog.setVisible(false);
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
        cbcTwoSurfaces.add(panel, layout, constraints);
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
    cbcTwoSurfaces.setHighlight(highlight);
    fcEditDataset.setHighlight(highlight);
    fcStatus.setHighlight(highlight);
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
      datasetDialog = BatchRunTomoDatasetDialog.getSavedInstance(manager, this);
      datasetDialog.setParameters(rowMetaData.getDatasetMetaData());
    }
  }

  public void getParameters(final BatchRunTomoMetaData metaData) {
    BatchRunTomoRowMetaData rowMetaData = metaData.getRowMetaData(stackID);
    this.metaData = rowMetaData;
    rowMetaData.setDisplay(true);
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
    param.addDirectiveFile(new File(stack, getBatchDirectiveFileName()));
    String rootName = DatasetTool.getDatasetName(stack.getName(), cbcDual.isSelected());
    param.addRootName(rootName, deliverToDirectory, cbcDual.isSelected(), errMsg);
    if (!param.addCurrentLocation(stack.getParent(), !deliverToDirectory, errMsg)) {
      errMsg.append(": " + stack.getAbsolutePath() + ".  ");
    }
  }

  private String getBatchDirectiveFileName() {
    return manager.getName() + "_" +
        DatasetTool.getDatasetName(fcStack.getContractedValue(), cbcDual.isSelected()) +
        ".adoc";
  }

  void loadAutodoc() {
    DirectiveFile directiveFile = DirectiveFile.getInstance(manager, null,
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
    if (directiveFile.containsValue(DirectiveDef.RAW_BOUNDARY_MODEL_FOR_SEED_FINDING) ||
        directiveFile.containsValue(DirectiveDef.RAW_BOUNDARY_MODEL_FOR_PATCH_TRACKING)) {
      cbcBoundaryModel.setSelected(true);
    }
    if (datasetDialog != null) {
      datasetDialog.setValues(directiveFile);
    }
  }

  void saveAutodoc(final TemplatePanel templatePanel, final Autodoc graftedBaseAutodoc) {
    File stack = new File(fcStack.getExpandedValue());
    File file = new File(stack.getParent(), getBatchDirectiveFileName());
    try {
      if (file.exists()) {
        Utilities.deleteFile(file, manager, null);
      }
      Autodoc autodoc = AutodocFactory.getWritableAutodocInstance(manager, file);
      BatchTool.saveFieldToAutodoc(cbcDual, autodoc);
      BatchTool.saveFieldToAutodoc(cbcMontage, autodoc);
      BatchTool.saveFieldToAutodoc(fcSkip, autodoc);
      BatchTool.saveFieldToAutodoc(fcbskip, AxisID.SECOND, AxisType.DUAL_AXIS, autodoc);
      if (cbcBoundaryModel.isSelected() && BatchTool.needInAutodoc(cbcBoundaryModel)) {
        String boundaryModelName =
            BatchTool.getBoundaryModelName(stack.getName(), cbcDual.isSelected());
        autodoc.addNameValuePairAttribute(
            DirectiveDef.RAW_BOUNDARY_MODEL_FOR_SEED_FINDING.getDirective(null, null),
            boundaryModelName);
        autodoc.addNameValuePairAttribute(
            DirectiveDef.RAW_BOUNDARY_MODEL_FOR_PATCH_TRACKING.getDirective(null, null),
            boundaryModelName);
      }
      BatchTool.saveFieldToAutodoc(cbcTwoSurfaces, autodoc);
      if (templatePanel != null) {
        templatePanel.saveAutodoc(autodoc);
      }
      if (datasetDialog != null) {
        datasetDialog.saveAutodoc(autodoc);
      }
      else {
        BatchRunTomoDatasetDialog globalDatasetDialog =
            BatchRunTomoDatasetDialog.getGlobalInstance();
        if (globalDatasetDialog != null) {
          globalDatasetDialog.saveAutodoc(autodoc);
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
    if (cbcTwoSurfaces.isDifferentFromCheckpoint(true)) {
      cbcTwoSurfaces.backup();
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
    cbcTwoSurfaces.clear();
    setDefaults();
    // no default values to apply to table
    // Apply settings values
    setValues(userConfiguration);
    // Apply the directive collection values
    setValues(directiveFileCollection);
    // checkpoint template/starting batch
    cbcDual.checkpoint();
    cbcMontage.checkpoint();
    cbcTwoSurfaces.checkpoint();
    // If the user wants to retain their values, apply backed up values and then delete
    // them.
    if (retainUserValues) {
      cbcDual.restoreFromBackup();
      cbcMontage.restoreFromBackup();
      cbcTwoSurfaces.restoreFromBackup();
      updateDisplay();
    }
    // no field highlight values to set in table
    // Dataset dialog
    if (datasetDialog != null) {
      datasetDialog.applyValues(retainUserValues, directiveFileCollection);
    }
  }

  void setNumber(final int input) {
    hcNumber.setText(input);
  }

  private void setDefaults() {
    cbcDual.setSelected(true);
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
    if (!setValue(directiveFiles, DirectiveDef.TWO_SURFACES, cbcTwoSurfaces) &&
        directiveFiles.contains(DirectiveDef.SURFACES_TO_ANALYZE)) {
      EtomoNumber number = new EtomoNumber();
      number.set(directiveFiles.getValue(DirectiveDef.SURFACES_TO_ANALYZE));
      cbcTwoSurfaces.setSelected(number.equals(2));
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
