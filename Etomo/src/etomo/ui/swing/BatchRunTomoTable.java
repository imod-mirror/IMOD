package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import etomo.BaseManager;

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
final class BatchRunTomoTable implements Viewable, Highlightable {
  public static final String rcsid = "$Id:$";

  private final JPanel pnlRoot = new JPanel();
  private final RowList rowList = new RowList(this);
  private final JPanel pnlTable = new JPanel();
  private final GridBagLayout layout = new GridBagLayout();
  private final GridBagConstraints constraints = new GridBagConstraints();
  private final HeaderCell hcNumber = new HeaderCell("#");
  private final HeaderCell hcStack = new HeaderCell("Stack");
  private final HeaderCell hcDualAxis = new HeaderCell("Dual Axis");
  private final HeaderCell hcMontage = new HeaderCell("Montage");
  private final HeaderCell hcExcludeViews = new HeaderCell("Exclude Views");
  private final HeaderCell hcBoundaryModel = new HeaderCell("Boundary Model");
  private final MultiLineButton btnAdd = new MultiLineButton("Add");

  private final BaseManager manager;
  private final Viewport viewport;

  private File currentDirectory = null;

  private BatchRunTomoTable(final BaseManager manager) {
    this.manager = manager;
    viewport = new Viewport(this, 5, null, null, null, "BatchRunTomo");
  }

  static BatchRunTomoTable getInstance(final BaseManager manager) {
    BatchRunTomoTable instance = new BatchRunTomoTable(manager);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  private void createPanel() {
    // init
    JPanel pnlView = new JPanel();
    JPanel pnlButtons = new JPanel();
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.add(pnlView);
    pnlRoot.add(pnlButtons);
    // View
    pnlView.setLayout(new BoxLayout(pnlView, BoxLayout.Y_AXIS));
    pnlView.add(viewport.getPagingPanel());
    pnlView.add(pnlTable);
    // Buttons
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));
    pnlButtons.add(btnAdd.getComponent());
    // Table
    pnlTable.setLayout(layout);
    pnlTable.setBorder(LineBorder.createBlackLineBorder());
    // constraints
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridheight = 1;
    constraints.weighty = 1.0;
    constraints.weightx = 1.0;
    // header
    constraints.gridwidth = 2;
    hcNumber.add(pnlTable, layout, constraints);
    hcStack.add(pnlTable, layout, constraints);
    constraints.gridwidth = 1;
    hcDualAxis.add(pnlTable, layout, constraints);
    hcMontage.add(pnlTable, layout, constraints);
    hcExcludeViews.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hcBoundaryModel.add(pnlTable, layout, constraints);
  }

  private void addListeners() {
    btnAdd.addActionListener(new BatchRunTomoListener(this));
  }

  Component getComponent() {
    return pnlRoot;
  }

  private void updateDisplay() {
    boolean enable = rowList.size() > 0;
    boolean highlighted = rowList.isHighlighted();
  }

  public void msgViewportPaged() {
    rowList.removeAll();
    rowList.display(viewport);
    UIHarness.INSTANCE.pack(manager);
  }

  public void highlight(final boolean highlight) {
    updateDisplay();
  }

  private void action(final String actionCommand) {
    if (actionCommand == null) {
      return;
    }
    if (actionCommand.equals(btnAdd.getActionCommand())) {
      rowList.add();
    }
  }

  public int size() {
    return 0;
  }

  private static final class BatchRunTomoListener implements ActionListener {
    private final BatchRunTomoTable table;

    private BatchRunTomoListener(final BatchRunTomoTable table) {
      this.table = table;
    }

    public void actionPerformed(final ActionEvent actionEvent) {
      table.action(actionEvent.getActionCommand());
    }
  }

  private final class RowList {
    private final List<BatchRunTomoRow> list = new ArrayList<BatchRunTomoRow>();

    private final BatchRunTomoTable table;

    private RowList(final BatchRunTomoTable table) {
      this.table = table;
    }

    private void add() {
      int index = list.size();
      BatchRunTomoRow row = BatchRunTomoRow.getInstance(manager.getPropertyUserDir(),
          table, pnlTable, layout, constraints, index);
      list.add(row);
      row.display();
      viewport.adjustViewport(index);
      UIHarness.INSTANCE.pack(manager);
    }

    private void removeAll() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).remove();
      }
    }

    private void display(Viewport viewport) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).display(i, viewport);
      }
    }

    private int size() {
      return list.size();
    }

    private boolean isHighlighted() {
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).isHighlighted()) {
          return true;
        }
      }
      return false;
    }
  }
}
