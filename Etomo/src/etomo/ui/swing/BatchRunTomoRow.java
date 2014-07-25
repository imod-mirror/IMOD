package etomo.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFileCollection;
import etomo.type.EtomoNumber;
import etomo.type.UserConfiguration;
import etomo.ui.BatchRunTomoTab;

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
final class BatchRunTomoRow implements Highlightable {
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
  private final FieldCell fcExcludeViewsA = FieldCell.getEditableInstance();
  private final FieldCell fcExcludeViewsB = FieldCell.getEditableInstance();
  private final CheckBoxCell cbcTwoSurfaces = new CheckBoxCell();
  private final FieldCell fcEditDataset = FieldCell.getIneditableInstance();
  private final FieldCell fcStatus = FieldCell.getIneditableInstance();
  private final CheckBoxCell cbcRun = new CheckBoxCell();
  private final MinibuttonCell bcEtomo = new MinibuttonCell(new ImageIcon(ETOMO_ICON_URL));
  private final MinibuttonCell mbc3dmodA = new MinibuttonCell(
      new ImageIcon(IMOD_ICON_URL));
  private final MinibuttonCell mbc3dmodB = new MinibuttonCell(
      new ImageIcon(IMOD_ICON_URL));

  private final JPanel panel;
  private final GridBagLayout layout;
  private final GridBagConstraints constraints;
  private final HighlighterButton hbRow;
  private final FieldCell fcStack;
  private final HeaderCell hcNumber = new HeaderCell();

  private BatchRunTomoRow(final String propertyUserDir, final BatchRunTomoTable table,
      final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number, final File stack,
      final BatchRunTomoRow initialValueRow) {
    this.panel = panel;
    this.layout = layout;
    this.constraints = constraints;
    hcNumber.setText(number);
    hbRow = HighlighterButton.getInstance(this, table);
    fcStack = FieldCell.getExpandableInstance(null);
    fcStack.setValue(stack);
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
      bcEtomo.setDisabledIcon(new ImageIcon(ETOMO_DISABLED_ICON_URL));
    }
    if (ETOMO_PRESSED_ICON_URL != null) {
      bcEtomo.setPressedIcon(new ImageIcon(ETOMO_PRESSED_ICON_URL));
    }
    setDefaults();
    if (initialValueRow != null) {
      cbcDualAxis.setSelected(initialValueRow.cbcDualAxis.isSelected());
      cbcMontage.setSelected(initialValueRow.cbcMontage.isSelected());
      cbcTwoSurfaces.setSelected(initialValueRow.cbcTwoSurfaces.isSelected());
    }
    cbcRun.setSelected(true);
    bcEtomo.setEnabled(false);
  }

  static BatchRunTomoRow getInstance(final String propertyUserDir,
      final BatchRunTomoTable table, final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number, final File stack,
      final BatchRunTomoRow initialValueRow) {
    return new BatchRunTomoRow(propertyUserDir, table, panel, layout, constraints,
        number, stack, initialValueRow);
  }

  static BatchRunTomoRow getDefaultsInstance() {
    return new BatchRunTomoRow(null, null, null, null, null, -1, null, null);
  }

  void remove() {
    hcNumber.remove();
    hbRow.remove();
    fcStack.remove();
    cbcBoundaryModel.remove();
    cbcDualAxis.remove();
    cbcMontage.remove();
    fcExcludeViewsA.remove();
    fcExcludeViewsB.remove();
    cbcTwoSurfaces.remove();
    fcEditDataset.remove();
    fcStatus.remove();
    cbcRun.remove();
    bcEtomo.remove();
    mbc3dmodA.remove();
    mbc3dmodB.remove();
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
        fcExcludeViewsA.add(panel, layout, constraints);
        fcExcludeViewsB.add(panel, layout, constraints);
        cbcBoundaryModel.add(panel, layout, constraints);
        cbcTwoSurfaces.add(panel, layout, constraints);
        fcEditDataset.add(panel, layout, constraints);
        mbc3dmodA.add(panel, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        mbc3dmodB.add(panel, layout, constraints);
      }
      else {
        fcStatus.add(panel, layout, constraints);
        cbcRun.add(panel, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        bcEtomo.add(panel, layout, constraints);
      }
    }
  }

  void expandStack(final boolean expanded) {
    fcStack.expand(expanded);
  }

  public void highlight(final boolean highlight) {
    fcStack.setHighlight(highlight);
    cbcBoundaryModel.setHighlight(highlight);
    cbcDualAxis.setHighlight(highlight);
    cbcMontage.setHighlight(highlight);
    fcExcludeViewsA.setHighlight(highlight);
    fcExcludeViewsB.setHighlight(highlight);
    cbcTwoSurfaces.setHighlight(highlight);
    fcEditDataset.setHighlight(highlight);
  }

  boolean isHighlighted() {
    return hbRow.isHighlighted();
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
    return changed;
  }

  void clear() {
    cbcDualAxis.clear();
    cbcMontage.clear();
    cbcTwoSurfaces.clear();
    setDefaults();
  }

  private void setDefaults() {
    cbcDualAxis.setSelected(true);
  }

  /**
   * Move any backed up values into the field, and delete the backup.
   */
  void restoreFromBackup() {
    cbcDualAxis.restoreFromBackup();
    cbcMontage.restoreFromBackup();
    cbcTwoSurfaces.restoreFromBackup();
  }

  void checkpoint() {
    cbcDualAxis.checkpoint();
    cbcMontage.checkpoint();
    cbcTwoSurfaces.checkpoint();
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
}
