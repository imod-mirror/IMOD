package etomo.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JPanel;

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

  private final HeaderCell hcNumber = new HeaderCell();
  private final CheckBoxCell cbcBoundaryModel = new CheckBoxCell();
  private final CheckBoxCell cbcDualAxis = new CheckBoxCell();
  private final CheckBoxCell cbcMontage = new CheckBoxCell();
  private final FieldCell fcExcludeViews = FieldCell.getEditableInstance();
  private final CheckBoxCell cbcTwoSurfaces = new CheckBoxCell();

  private final JPanel panel;
  private final GridBagLayout layout;
  private final GridBagConstraints constraints;
  private final HighlighterButton hbRow;
  private final FieldCell fcStack;

  private BatchRunTomoRow(final String propertyUserDir, final BatchRunTomoTable table,
      final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number, final File stack,
      final BatchRunTomoRow prevRow, final File currentDirectory) {
    this.panel = panel;
    this.layout = layout;
    this.constraints = constraints;
    hcNumber.setText(number);
    hbRow = HighlighterButton.getInstance(this, table);
    String currentAbsolutePath = null;
    if (currentDirectory != null) {
      currentAbsolutePath = currentDirectory.getAbsolutePath();
    }
    fcStack = FieldCell.getExpandableInstance(currentAbsolutePath);
    fcStack.setValue(stack);
    if (prevRow != null) {
      cbcDualAxis.setSelected(prevRow.cbcDualAxis.isSelected());
      cbcMontage.setSelected(prevRow.cbcMontage.isSelected());
      cbcTwoSurfaces.setSelected(prevRow.cbcTwoSurfaces.isSelected());
    }
  }

  static BatchRunTomoRow getInstance(final String propertyUserDir,
      final BatchRunTomoTable table, final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number, final File stack,
      final BatchRunTomoRow prevRow, final File currentDirectory) {
    return new BatchRunTomoRow(propertyUserDir, table, panel, layout, constraints,
        number, stack, prevRow, currentDirectory);
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
  }

  void display(final Viewport viewport) {
    if (viewport.inViewport(hcNumber.getInt())) {
      constraints.gridwidth = 1;
      hcNumber.add(panel, layout, constraints);
      hbRow.add(panel, layout, constraints);
      constraints.gridwidth = 2;
      fcStack.add(panel, layout, constraints);
      constraints.gridwidth = 1;
      cbcBoundaryModel.add(panel, layout, constraints);
      cbcDualAxis.add(panel, layout, constraints);
      cbcMontage.add(panel, layout, constraints);
      fcExcludeViews.add(panel, layout, constraints);
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      cbcTwoSurfaces.add(panel, layout, constraints);
    }
  }

  void expandStack(final boolean expanded) {
    fcStack.expand(expanded);
  }

  void setCurrentDirectory(final String currentAbsolutePath) {
    fcStack.setRootDir(currentAbsolutePath);
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
}
