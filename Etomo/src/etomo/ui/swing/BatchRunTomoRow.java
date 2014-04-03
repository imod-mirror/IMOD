package etomo.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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
  private final CheckBoxCell cbcDualAxis = new CheckBoxCell();
  private final CheckBoxCell cbcMontage = new CheckBoxCell();
  private final FieldCell fcExcludeViews = FieldCell.getEditableInstance();

  private final JPanel panel;
  private final GridBagLayout layout;
  private final GridBagConstraints constraints;
  private final HighlighterButton hbRow;
  private final FieldCell fcStack;
  private final FileButtonCell fbcStack;
  private final FieldCell fcBoundaryModel;
  private final FileButtonCell fbcBoundaryModel;

  private BatchRunTomoRow(final String propertyUserDir, final BatchRunTomoTable table,
      final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number) {
    this.panel = panel;
    this.layout = layout;
    this.constraints = constraints;
    hcNumber.setText(number);
    hbRow = HighlighterButton.getInstance(this, table);
    fcStack = FieldCell.getExpandableInstance(propertyUserDir);
    fbcStack = FileButtonCell.getInstance((CurrentDirectory) null);
    fcBoundaryModel = FieldCell.getExpandableInstance(propertyUserDir);
    fbcBoundaryModel = FileButtonCell.getInstance((CurrentDirectory) null);
  }

  static BatchRunTomoRow getInstance(final String propertyUserDir,
      final BatchRunTomoTable table, final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints, final int number) {
    BatchRunTomoRow instance = new BatchRunTomoRow(propertyUserDir, table, panel, layout,
        constraints, number);
    instance.addListeners();
    return instance;
  }

  private void addListeners() {
    fbcStack.setActionTarget(fcStack);
    fbcBoundaryModel.setActionTarget(fcBoundaryModel);
  }

  void remove() {
    hcNumber.remove();
    hbRow.remove();
    fcStack.remove();
    fbcStack.remove();
    cbcDualAxis.remove();
    cbcMontage.remove();
    fcExcludeViews.remove();
    fcBoundaryModel.remove();
    fbcBoundaryModel.remove();
  }

  void display() {
    constraints.gridwidth = 1;
    hcNumber.add(panel, layout, constraints);
    hbRow.add(panel, layout, constraints);
    fcStack.add(panel, layout, constraints);
    fbcStack.add(panel, layout, constraints);
    cbcDualAxis.add(panel, layout, constraints);
    cbcMontage.add(panel, layout, constraints);
    fcExcludeViews.add(panel, layout, constraints);
    fcBoundaryModel.add(panel, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    fbcBoundaryModel.add(panel, layout, constraints);
  }

  void display(final int index, final Viewport viewport) {
    if (viewport.inViewport(index)) {
      display();
    }
  }

  public void highlight(final boolean highlight) {
    fcStack.setHighlight(highlight);
    fbcStack.setHighlight(highlight);
    cbcDualAxis.setHighlight(highlight);
    cbcMontage.setHighlight(highlight);
    fcExcludeViews.setHighlight(highlight);
    fcBoundaryModel.setHighlight(highlight);
    fbcBoundaryModel.setHighlight(highlight);
  }

  boolean isHighlighted() {
    return hbRow.isHighlighted();
  }
}
