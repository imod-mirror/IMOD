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
import etomo.logic.DatasetTool;
import etomo.process.BaseProcessManager;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.LogFile;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.BatchRunTomoRowMetaData;
import etomo.type.EtomoNumber;
import etomo.type.Run3dmodMenuOptions;
import etomo.type.UserConfiguration;
import etomo.ui.BatchRunTomoTab;
import etomo.ui.PreferredTableSize;

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
final class BatchRunTomoRow implements Highlightable, Run3dmodButtonContainer {
  public static final String rcsid = "$Id:$";

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

  private final CheckBoxCell cbcBoundaryModel = new CheckBoxCell();
  private final CheckBoxCell cbcDualAxis = new CheckBoxCell();
  private final CheckBoxCell cbcMontage = new CheckBoxCell();
  private final FieldCell fcSkip = FieldCell.getEditableInstance();
  private final FieldCell fcbskip = FieldCell.getEditableInstance();
  private final CheckBoxCell cbcTwoSurfaces = new CheckBoxCell();
  private final FieldCell fcEditDataset = FieldCell.getIneditableInstance();
  private final FieldCell fcStatus = FieldCell.getIneditableInstance();
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
  // When overridePrevRow is true, overrideDualAxis will replace prevRow dual axis.
  private final boolean overridePrevRow;
  private final boolean overrideDualAxis;
  // Always save prevRow dual axis in prevRowDualAxis.
  private final boolean prevRowDualAxis;
  private final String stackID;
  private final PreferredTableSize preferredTableSize;

  private int imodIndexA = -1;
  private int imodIndexB = -1;
  private BatchRunTomoDatasetDialog datasetDialog = null;
  private BatchRunTomoRowMetaData metaData = null;

  private BatchRunTomoRow(final String propertyUserDir, final BatchRunTomoTable table,
      final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number, final File stack,
      final BatchRunTomoRow prevRow, final boolean overridePrevRow,
      final boolean overrideDualAxis, final BatchRunTomoManager manager,
      final String stackID, final PreferredTableSize preferredTableSize) {
    this.panel = panel;
    this.layout = layout;
    this.constraints = constraints;
    this.manager = manager;
    this.overridePrevRow = overridePrevRow;
    this.overrideDualAxis = overrideDualAxis;
    this.stackID = stackID;
    this.preferredTableSize = preferredTableSize;
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
    prevRowDualAxis = cbcDualAxis.isSelected();
    if (overridePrevRow) {
      cbcDualAxis.setSelected(overrideDualAxis);
    }
    cbcRun.setSelected(true);
    mbcEtomo.setEnabled(false);
    updateDisplay();
  }

  static BatchRunTomoRow getInstance(final String propertyUserDir,
      final BatchRunTomoTable table, final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number, final File stack,
      final BatchRunTomoRow prevRow, final boolean overridePrevRow,
      final boolean overrideDualAxis, final BatchRunTomoManager manager,
      final String stackID, final PreferredTableSize datasetWidth) {
    BatchRunTomoRow instance = new BatchRunTomoRow(propertyUserDir, table, panel, layout,
        constraints, number, stack, prevRow, overridePrevRow, overrideDualAxis, manager,
        stackID, datasetWidth);
    instance.addListeners();
    return instance;
  }

  static BatchRunTomoRow getDefaultsInstance() {
    BatchRunTomoRow instance = new BatchRunTomoRow(null, null, null, null, null, -1,
        null, null, false, false, null, null, null);
    return instance;
  }

  void copy(final BatchRunTomoRow prevRow) {
    if (prevRow != null) {
      cbcDualAxis.setSelected(prevRow.cbcDualAxis.isSelected());
      cbcMontage.setSelected(prevRow.cbcMontage.isSelected());
      cbcTwoSurfaces.setSelected(prevRow.cbcTwoSurfaces.isSelected());
    }
  }

  boolean isDualAxis() {
    return cbcDualAxis.isSelected();
  }

  private void addListeners() {
    // give each listened to field an unique action command
    mbc3dmodA.setActionCommand(((Object) mbc3dmodA).toString());
    mbc3dmodB.setActionCommand(((Object) mbc3dmodB).toString());
    cbcDualAxis.setActionCommand(((Object) cbcDualAxis).toString());
    mbcEtomo.setActionCommand(((Object) mbcEtomo).toString());
    cbcBoundaryModel.setActionCommand(((Object) cbcBoundaryModel).toString());
    bcEditDataset.setActionCommand(((Object) bcEditDataset).toString());
    // set listeners
    mbc3dmodA.addActionListener(listener);
    mbc3dmodB.addActionListener(listener);
    cbcDualAxis.addActionListener(listener);
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
    if (actionCommand.equals(cbcDualAxis.getActionCommand())) {
      updateDisplay();
    }
    else {
      boolean dualAxis = cbcDualAxis.isSelected();
      if (actionCommand.equals(mbc3dmodA.getActionCommand())) {
        imodIndexA = manager.imod(
            DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.FIRST, dualAxis),
            AxisID.FIRST, imodIndexA, cbcBoundaryModel.isSelected(), dualAxis,
            run3dmodMenuOptions);
      }
      else if (actionCommand.equals(mbc3dmodB.getActionCommand())) {
        // The model is only opened for the A axis
        imodIndexB = manager
            .imod(DatasetTool.getStackFile(fcStack.getExpandedValue(), AxisID.SECOND,
                dualAxis), AxisID.SECOND, imodIndexB, run3dmodMenuOptions);
      }
      else if (actionCommand.equals(mbcEtomo.getActionCommand())) {
        EtomoDirector.INSTANCE.openTomogram(
            DatasetTool.getDatasetFile(DatasetTool.getStackFile(
                fcStack.getExpandedValue(), AxisID.FIRST, dualAxis), dualAxis), true,
            null, mbcEtomo);
      }
      else if (actionCommand.equals(cbcBoundaryModel.getActionCommand())
          && cbcBoundaryModel.isSelected()) {
        // The model is only opened for the A axis
        if (imodIndexA != -1) {
          manager.imodModel(AxisID.FIRST, imodIndexA, fcStack.getContractedValue(),
              dualAxis);
        }
      }
      else if (actionCommand.equals(bcEditDataset.getActionCommand())) {
        if (datasetDialog == null) {
          datasetDialog = BatchRunTomoDatasetDialog.getRowInstance(
              manager,
              DatasetTool.getDatasetFile(DatasetTool.getStackFile(
                  fcStack.getExpandedValue(), AxisID.FIRST, dualAxis), dualAxis));
          datasetDialog.addRevertToGlobalActionListener(listener);
          fcEditDataset.setValue("   Set");
        }
        else {
          datasetDialog.setVisible(true);
          bcEditDataset.setSelected(true);
        }
      }
      // Revert to Global button in the dataset dialog
      else if (datasetDialog != null
          && actionCommand.equals(datasetDialog.getRevertToGlobalActionCommand())) {
        if (UIHarness.INSTANCE
            .openYesNoDialog(
                manager,
                "Data in this window will be lost.  Revert to global dataset data for this stack?",
                AxisID.ONLY)) {
          deleteDataset();
        }
      }
    }
  }

  void remove() {
    hcNumber.remove();
    hbRow.remove();
    fcStack.remove();
    cbcBoundaryModel.remove();
    cbcDualAxis.remove();
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

  private void deleteDataset() {
    if (datasetDialog != null) {
      datasetDialog.setVisible(false);
      datasetDialog = null;
      bcEditDataset.setSelected(false);
      fcEditDataset.setValue("");
    }
  }

  private void updateDisplay() {
    boolean dual = cbcDualAxis.isSelected();
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
        cbcDualAxis.add(panel, layout, constraints);
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

  String getExpandedStack() {
    return fcStack.getExpandedValue();
  }

  public void highlight(final boolean highlight) {
    fcStack.setHighlight(highlight);
    cbcBoundaryModel.setHighlight(highlight);
    cbcDualAxis.setHighlight(highlight);
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
    fcbskip.setValue(rowMetaData.getBskip());
    boolean isDatasetDialog = rowMetaData.isDatasetDialog();
    bcEditDataset.setSelected(isDatasetDialog);
    if (isDatasetDialog) {
      datasetDialog = BatchRunTomoDatasetDialog.getSavedInstance(manager);
      datasetDialog.setParameters(rowMetaData.getDatasetMetaData());
    }
  }

  public void getParameters(final BatchRunTomoMetaData metaData) {
    BatchRunTomoRowMetaData rowMetaData = metaData.getRowMetaData(stackID);
    this.metaData = rowMetaData;
    rowMetaData.setDisplay(true);
    rowMetaData.setBskip(fcbskip.getValue());
    rowMetaData.setDatasetDialog(datasetDialog != null);
    if (datasetDialog != null) {
      datasetDialog.getParameters(rowMetaData.getDatasetMetaData());
    }
  }

  public void getParameters(final BatchruntomoParam param) {
    File stack = new File(fcStack.getExpandedValue());
    String rootName = DatasetTool.getDatasetName(stack.getName(),
        cbcDualAxis.isSelected());
    param.addDirectiveFile(rootName + ".adoc");
    param.addRootName(rootName);
    param.addCurrentLocation(stack.getParent());
  }

  private String getDirectiveFileName() {
    String rootName = DatasetTool.getDatasetName(
        new File(fcStack.getExpandedValue()).getName(), cbcDualAxis.isSelected());
    return rootName + ".adoc";
  }

  void saveAutodoc() {
    File stack = new File(fcStack.getExpandedValue());
    File file = new File(manager.getPropertyUserDir(), manager.getName() + "_"
        + DatasetTool.getDatasetName(stack.getName(), cbcDualAxis.isSelected()) + ".adoc");
    if (!file.exists()) {
      BaseProcessManager.touch(file.getAbsolutePath(), manager);
    }
    try {
      WritableAutodoc autodoc = AutodocFactory.getWritableInstance(manager, file, false);
      if (datasetDialog != null) {
        datasetDialog.saveAutodoc(autodoc);
      }
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

  void setEditDataset() {
    fcEditDataset.setValue(" Set");
  }

  /**
   * Check isDifferentFromCheckpoint on all data entry fields that are loaded from
   * directive files.
   * @return true if any field's isDifferentFromCheckpoint function returned true
   */
  boolean backupIfChanged() {
    boolean changed = false;
    if (cbcDualAxis.isDifferentFromCheckpoint(true)) {
      cbcDualAxis.backup();
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

  void applyValues(final UserConfiguration userConfiguration,
      final DirectiveFileCollection directiveFileCollection,
      final boolean retainUserValues) {
    // to apply values and highlights, start with a clean slate
    cbcDualAxis.clear();
    cbcMontage.clear();
    cbcTwoSurfaces.clear();
    setDefaults();
    // no default values to apply to table
    // Apply settings values
    setValues(userConfiguration);
    // Apply the directive collection values
    setValues(directiveFileCollection);
    // checkpoint
    cbcDualAxis.checkpoint();
    cbcMontage.checkpoint();
    cbcTwoSurfaces.checkpoint();
    // If the user wants to retain their values, apply backed up values and then delete
    // them.
    if (retainUserValues) {
      cbcDualAxis.restoreFromBackup();
      cbcMontage.restoreFromBackup();
      cbcTwoSurfaces.restoreFromBackup();
    }
    // no field highlight values to set in table
    // Dataset dialog
    if (datasetDialog != null) {
      datasetDialog.applyValues(userConfiguration, directiveFileCollection,
          retainUserValues);
    }
  }

  void setNumber(final int input) {
    hcNumber.setText(input);
  }

  private void setDefaults() {
    cbcDualAxis.setSelected(true);
  }

  /**
   * Set values from the directive file collection - only for directives that are present.
   * @param directiveFileCollection
   */
  void setValues(final DirectiveFileCollection directiveFileCollection) {
    setValue(directiveFileCollection, DirectiveDef.DUAL, cbcDualAxis);
    setValue(directiveFileCollection, DirectiveDef.MONTAGE, cbcMontage);
    if (!setValue(directiveFileCollection, DirectiveDef.TWO_SURFACES, cbcTwoSurfaces)
        && directiveFileCollection.contains(DirectiveDef.SURFACES_TO_ANALYZE)) {
      EtomoNumber number = new EtomoNumber();
      number.set(directiveFileCollection.getValue(DirectiveDef.SURFACES_TO_ANALYZE));
      cbcTwoSurfaces.setSelected(number != null && number.equals(2));
    }
  }

  private boolean setValue(final DirectiveFileCollection directiveFileCollection,
      final DirectiveDef directiveDef, final CheckBoxCell cell) {
    if (directiveFileCollection.contains(directiveDef)) {
      cell.setSelected(directiveFileCollection.isValue(directiveDef));
      return true;
    }
    return false;
  }

  void setValues(final UserConfiguration userConfiguration) {
    cbcDualAxis.setSelected(!userConfiguration.getSingleAxis());
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
