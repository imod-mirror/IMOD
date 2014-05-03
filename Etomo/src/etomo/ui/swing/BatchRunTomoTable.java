package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import etomo.BaseManager;
import etomo.storage.StackFileFilter;

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
final class BatchRunTomoTable implements Viewable, Highlightable, Expandable {
  public static final String rcsid = "$Id:$";

  private static final String STACK_TITLE = "Stack";

  private final JPanel pnlRoot = new JPanel();
  private final RowList rowList = new RowList(this);
  private final JPanel pnlTable = new JPanel();
  private final GridBagLayout layout = new GridBagLayout();
  private final GridBagConstraints constraints = new GridBagConstraints();
  private final HeaderCell hcNumber = new HeaderCell("#");
  private final HeaderCell hcStack = new HeaderCell(STACK_TITLE);
  private final HeaderCell hcDualAxis = new HeaderCell("Dual Axis");
  private final HeaderCell hcMontage = new HeaderCell("Montage");
  private final HeaderCell hcExcludeViews = new HeaderCell("Exclude Views");
  private final HeaderCell hcBoundaryModel = new HeaderCell("Boundary Model");
  private final HeaderCell hcTwoSurfaces = new HeaderCell("Beads on Two Surfaces");
  private final MultiLineButton btnAdd = new MultiLineButton("Add Stack(s)");
  private final MultiLineButton btnCopy = new MultiLineButton("Copy Down");
  private final MultiLineButton btnDelete = new MultiLineButton("Delete");
  private final MultiLineButton btnEditDataset = new MultiLineButton(
      "Set Dataset Specific Data");
  private final ExpandButton btnStack = ExpandButton.getInstance(this,
      ExpandButton.Type.MORE);

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
    btnAdd.setToPreferredSize();
    btnCopy.setToPreferredSize();
    btnDelete.setToPreferredSize();
    btnEditDataset.setToPreferredSize();
    btnStack.setName(STACK_TITLE);
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.add(pnlView);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y3));
    pnlRoot.add(pnlButtons);
    // View
    pnlView.setLayout(new BoxLayout(pnlView, BoxLayout.Y_AXIS));
    pnlView.add(viewport.getPagingPanel());
    pnlView.add(pnlTable);
    // Buttons
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnAdd.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnCopy.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnDelete.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnEditDataset.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
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
    constraints.gridwidth = 1;
    hcStack.add(pnlTable, layout, constraints);
    btnStack.add(pnlTable, layout, constraints);
    hcBoundaryModel.add(pnlTable, layout, constraints);
    hcDualAxis.add(pnlTable, layout, constraints);
    hcMontage.add(pnlTable, layout, constraints);
    hcExcludeViews.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hcTwoSurfaces.add(pnlTable, layout, constraints);
    // update
    viewport.adjustViewport(-1);
    updateDisplay();
  }

  private void addListeners() {
    btnAdd.addActionListener(new BatchRunTomoListener(this));
  }

  Component getComponent() {
    return pnlRoot;
  }

  void setCurrentDirectory(final String currentAbsolutePath) {
    if (currentAbsolutePath != null) {
      currentDirectory = new File(currentAbsolutePath);
    }
    else {
      currentDirectory = null;
    }
    rowList.setCurrentDirectory(currentAbsolutePath);
  }

  private void updateDisplay() {
    boolean enable = rowList.size() > 0;
    boolean highlighted = rowList.isHighlighted();
    btnCopy.setEnabled(enable && highlighted);
    btnDelete.setEnabled(enable && highlighted);
    btnEditDataset.setEnabled(enable && highlighted);
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
      JFileChooser chooser = new FileChooser(currentDirectory);
      chooser.setDialogTitle("Select a stack for each dataset");
      chooser.setFileFilter(new StackFileFilter());
      chooser.setMultiSelectionEnabled(true);
      chooser.setPreferredSize(UIParameters.INSTANCE.getFileChooserDimension());
      int returnVal = chooser.showOpenDialog(btnAdd.getComponent());
      File[] stackList = null;
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        stackList = chooser.getSelectedFiles();
      }
      if (stackList != null) {
        rowList.add(stackList);
      }
    }
  }

  public void expand(final ExpandButton button) {
    if (button == btnStack) {
      rowList.expandStack(btnStack.isExpanded());
    }
    UIHarness.INSTANCE.pack(manager);
  }

  public void expand(final GlobalExpandButton button) {
  }

  public int size() {
    return rowList.size();
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

    private void add(final File[] stackList) {
      if (stackList == null) {
        return;
      }
      for (int i = 0; i < stackList.length; i++) {
        int index = list.size();
        BatchRunTomoRow prevRow = null;
        if (index > 0) {
          prevRow = list.get(index - 1);
        }
        BatchRunTomoRow row = BatchRunTomoRow.getInstance(manager.getPropertyUserDir(),
            table, pnlTable, layout, constraints, index, stackList[i], prevRow,
            currentDirectory);
        row.expandStack(btnStack.isExpanded());
        list.add(row);
        viewport.adjustViewport(index);
        row.display(viewport);
      }
      UIHarness.INSTANCE.pack(manager);
    }

    private void expandStack(final boolean expanded) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).expandStack(expanded);
      }
    }

    private void setCurrentDirectory(final String currentAbsolutePath) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setCurrentDirectory(currentAbsolutePath);
      }
    }

    private void removeAll() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).remove();
      }
    }

    private void display(Viewport viewport) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).display(viewport);
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
