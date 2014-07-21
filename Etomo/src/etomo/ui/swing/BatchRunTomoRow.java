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
  private static final URL ETOMO_ICON_URL = ClassLoader
      .getSystemResource("images/etomoicon.png");
  private static final URL ETOMO_DISABLED_ICON_URL = ClassLoader
      .getSystemResource("images/etomoicon-disabled.png");

  private final CheckBoxCell cbcBoundaryModel = new CheckBoxCell();
  private final CheckBoxCell cbcDualAxis = new CheckBoxCell();
  private final CheckBoxCell cbcMontage = new CheckBoxCell();
  private final FieldCell fcExcludeViews = FieldCell.getEditableInstance();
  private final CheckBoxCell cbcTwoSurfaces = new CheckBoxCell();
  private final FieldCell fcStatus = FieldCell.getIneditableInstance();
  private final CheckBoxCell cbcRun = new CheckBoxCell();
  private final ButtonCell bcEtomo = new ButtonCell(new ImageIcon(ETOMO_ICON_URL));
  private final MinibuttonCell mbc3dmod = new MinibuttonCell(new ImageIcon(IMOD_ICON_URL));

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
    mbc3dmod.setDisabledIcon(new ImageIcon(IMOD_DISABLED_ICON_URL));
    bcEtomo.setDisabledIcon(new ImageIcon(ETOMO_DISABLED_ICON_URL));
    if (initialValueRow != null) {
      cbcDualAxis.setSelected(initialValueRow.cbcDualAxis.isSelected());
      cbcMontage.setSelected(initialValueRow.cbcMontage.isSelected());
      cbcTwoSurfaces.setSelected(initialValueRow.cbcTwoSurfaces.isSelected());
    }
    else {
      cbcDualAxis.setSelected(true);
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
    fcExcludeViews.remove();
    cbcTwoSurfaces.remove();
    fcStatus.remove();
    cbcRun.remove();
    bcEtomo.remove();
    mbc3dmod.remove();
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
        cbcBoundaryModel.add(panel, layout, constraints);
        cbcDualAxis.add(panel, layout, constraints);
        cbcMontage.add(panel, layout, constraints);
        fcExcludeViews.add(panel, layout, constraints);
        cbcTwoSurfaces.add(panel, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        mbc3dmod.add(panel, layout, constraints);
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
    fcExcludeViews.setHighlight(highlight);
    cbcTwoSurfaces.setHighlight(highlight);
  }

  boolean isHighlighted() {
    return hbRow.isHighlighted();
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
    setValues(directiveFileCollection, false);
  }

  void setFieldHighlightValues(final DirectiveFileCollection directiveFileCollection) {
    setValues(directiveFileCollection, true);
  }

  private void setValues(final DirectiveFileCollection directiveFileCollection,
      final boolean setFieldHighlighValue) {
    setValue(directiveFileCollection, DirectiveDef.DUAL, setFieldHighlighValue,
        cbcDualAxis);
    setValue(directiveFileCollection, DirectiveDef.MONTAGE, setFieldHighlighValue,
        cbcMontage);
    if (!setValue(directiveFileCollection, DirectiveDef.TWO_SURFACES,
        setFieldHighlighValue, cbcTwoSurfaces)
        && directiveFileCollection.contains(DirectiveDef.SURFACES_TO_ANALYZE,
            setFieldHighlighValue)) {
      EtomoNumber number = new EtomoNumber();
      number.set(directiveFileCollection.getValue(DirectiveDef.SURFACES_TO_ANALYZE,
          setFieldHighlighValue));
      boolean value = number != null && number.equals(2);
      if (!setFieldHighlighValue) {
        cbcTwoSurfaces.setSelected(value);
      }
      else {
        cbcTwoSurfaces.setFieldHighlightValue(value);
      }
    }
  }

  private boolean setValue(final DirectiveFileCollection directiveFileCollection,
      final DirectiveDef directiveDef, final boolean setFieldHighlightValue,
      final CheckBoxCell cell) {
    if (directiveFileCollection.contains(directiveDef, setFieldHighlightValue)) {
      boolean value = directiveFileCollection.isValue(directiveDef,
          setFieldHighlightValue);
      if (!setFieldHighlightValue) {
        cell.setSelected(value);
      }
      else {
        cell.setFieldHighlightValue(value);
      }
      return true;
    }
    return false;
  }

  void setValues(final UserConfiguration userConfiguration) {
    cbcDualAxis.setSelected(!userConfiguration.getSingleAxis());
    cbcMontage.setSelected(userConfiguration.getMontage());
  }
}
