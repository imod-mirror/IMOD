package etomo.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 1.1  2007/04/02 16:02:49  sueh
 * <p> bug# 964 A row of the iteration table, which contain per iteration PEET data.
 * <p> </p>
 */
final class IterationRow implements Highlightable{
  public static final String rcsid = "$Id$";

  private final HeaderCell number = new HeaderCell();
  private final FieldCell dPhiStart =  FieldCell.getEditableInstance();
  private final FieldCell dPhiIncrement =  FieldCell.getEditableInstance();
  private final FieldCell dThetaStart = FieldCell.getEditableInstance();
  private final FieldCell dThetaIncrement =  FieldCell.getEditableInstance();
  private final FieldCell dPsiStart =  FieldCell.getEditableInstance();
  private final FieldCell dPsiIncrement =  FieldCell.getEditableInstance();
  private final FieldCell searchRadius =  FieldCell.getEditableInstance();
  private final FieldCell hiCutoff= FieldCell.getEditableInstance();
  private final FieldCell hiCutoffSigma= FieldCell.getEditableInstance();
  private final FieldCell refThreshold =  FieldCell.getEditableInstance();
  
  private final int index;
  private final JPanel panel;
  private final GridBagLayout layout;
  private final GridBagConstraints constraints;
  private final HighlighterButton btnHighlighter;
  private final Highlightable parent;

  IterationRow(final int index, final Highlightable parent, final JPanel panel, final GridBagLayout layout,
      final GridBagConstraints constraints) {
    this.index = index;
    this.parent=parent;
    this.panel = panel;
    this.layout = layout;
    this.constraints = constraints;
    btnHighlighter = HighlighterButton.getInstance(this, parent);
    number.setText(String.valueOf(index + 1));
  }
  
  IterationRow(final int index, final IterationRow iterationRow) {
    this.index = index;
    this.parent=iterationRow.parent;
    this.panel = iterationRow.panel;
    this.layout = iterationRow.layout;
    this.constraints = iterationRow.constraints;
    btnHighlighter = HighlighterButton.getInstance(this, parent);
    number.setText(String.valueOf(index + 1));
    dPhiStart.setValue(iterationRow.dPhiStart.getValue());
    dPhiIncrement.setValue(iterationRow.dPhiIncrement.getValue());
    dThetaStart.setValue(iterationRow.dThetaStart.getValue());
    dThetaIncrement.setValue(iterationRow.dThetaIncrement.getValue());
    dPsiStart.setValue(iterationRow.dPsiStart.getValue());
    dPsiIncrement.setValue(iterationRow.dPsiIncrement.getValue());
    searchRadius.setValue(iterationRow.searchRadius.getValue());
    hiCutoff.setValue(iterationRow.hiCutoff.getValue());
    hiCutoffSigma.setValue(iterationRow.hiCutoffSigma.getValue());
    refThreshold.setValue(iterationRow.refThreshold.getValue());
  }
  
  public void highlight(final boolean highlight) {
    dPhiStart.setHighlight(highlight);
    dPhiIncrement.setHighlight(highlight);
    dThetaStart.setHighlight(highlight);
    dThetaIncrement.setHighlight(highlight);
    dPsiStart.setHighlight(highlight);
    dPsiIncrement.setHighlight(highlight);
    searchRadius.setHighlight(highlight);
    hiCutoff.setHighlight(highlight);
    hiCutoffSigma.setHighlight(highlight);
    refThreshold.setHighlight(highlight);
  }
  
  boolean isHighlighted() {
    return btnHighlighter.isHighlighted();
  }

  void display() {
    constraints.weightx = 0.0;
    constraints.weighty = 0.1;
    constraints.gridwidth = 1;
    number.add(panel,layout,constraints);
    btnHighlighter.add(panel, layout, constraints);
    constraints.weightx = 0.1;
    dPhiStart.add(panel,layout,constraints);
    dPhiIncrement.add(panel,layout,constraints);
    dThetaStart.add(panel,layout,constraints);
    dThetaIncrement.add(panel,layout,constraints);
    dPsiStart.add(panel,layout,constraints);
    dPsiIncrement.add(panel,layout,constraints);
    searchRadius.add(panel,layout,constraints);
    hiCutoff.add(panel,layout,constraints);
    hiCutoffSigma.add(panel,layout,constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    refThreshold.add(panel,layout,constraints);
  }
}
