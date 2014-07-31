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
import etomo.storage.DirectiveFileCollection;
import etomo.storage.StackFileFilter;
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
final class BatchRunTomoTable implements Viewable, Highlightable, Expandable,
    ActionListener {
  public static final String rcsid = "$Id:$";

  private static final String STACK_TITLE = "Stack";
  private static final int NUM_STACKS_HEADER_ROWS = 3;
  private static final int NUM_RUN_HEADER_ROWS = 2;

  private final JPanel pnlRoot = new JPanel();
  private final RowList rowList = new RowList(this);
  private final JPanel pnlTable = new JPanel();
  private final GridBagLayout layout = new GridBagLayout();
  private final GridBagConstraints constraints = new GridBagConstraints();
  // Both tabs
  private final HeaderCell[] hcNumber = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell[] hcStack = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  // stacks tab
  private final HeaderCell[] hcDualAxis = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell[] hcMontage = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell[] hcExcludeViews = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell hcExcludeViewsB = new HeaderCell("from B");
  private final HeaderCell[] hcBoundaryModel = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell[] hcTwoSurfaces = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell[] hcEditDataset = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell[] hc3dmod = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell hc3dmodB = new HeaderCell("B");
  // run tab
  private final HeaderCell[] hcStatus = new HeaderCell[NUM_RUN_HEADER_ROWS];
  private final HeaderCell[] hcRun = new HeaderCell[NUM_RUN_HEADER_ROWS];
  private final HeaderCell[] hcEtomo = new HeaderCell[NUM_RUN_HEADER_ROWS];
  private final MultiLineButton btnAdd = new MultiLineButton("Add Stack(s)");
  private final MultiLineButton btnCopyDown = new MultiLineButton("Copy Down");
  private final MultiLineButton btnDelete = new MultiLineButton("Delete");
  private final MultiLineButton btnEditDataset = new MultiLineButton(
      "Set Dataset Specific Values");
  private final ExpandButton btnStack = ExpandButton.getInstance(this,
      ExpandButton.Type.MORE);

  private final JPanel pnlButtons = new JPanel();

  private final BaseManager manager;
  private final Viewport viewport;
  private final ActionListener editDatasetListener;

  private File currentDirectory = null;
  private BatchRunTomoTab curTab = null;

  private BatchRunTomoTable(final BaseManager manager,
      final ActionListener editDatasetListener) {
    this.manager = manager;
    this.editDatasetListener = editDatasetListener;
    viewport = new Viewport(this, 5, null, null, null, "BatchRunTomo");
  }

  static BatchRunTomoTable getInstance(final BaseManager manager,
      final ActionListener editDatasetListener) {
    BatchRunTomoTable instance = new BatchRunTomoTable(manager, editDatasetListener);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  private void createPanel() {
    // init
    JPanel pnlView = new JPanel();
    btnAdd.setToPreferredSize();
    btnCopyDown.setToPreferredSize();
    btnDelete.setToPreferredSize();
    btnEditDataset.setToPreferredSize();
    btnStack.setName(STACK_TITLE);
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.add(pnlView);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRoot.add(pnlButtons);
    // View
    pnlView.setLayout(new BoxLayout(pnlView, BoxLayout.X_AXIS));
    pnlView.add(viewport.getPagingPanel());
    pnlView.add(pnlTable);
    // Buttons
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnAdd.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnCopyDown.getComponent());
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
    // align
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
    viewport.adjustViewport(-1);
    updateDisplay();
  }

  private void rebuildTable() {
    // init
    // both tabs
    hcNumber[0] = new HeaderCell();
    hcNumber[1] = new HeaderCell();
    hcNumber[2] = new HeaderCell("#");
    hcStack[0] = new HeaderCell();
    hcStack[1] = new HeaderCell();
    hcStack[2] = new HeaderCell(STACK_TITLE);
    // stacks tab
    hcDualAxis[0] = new HeaderCell();
    hcDualAxis[1] = new HeaderCell("Dual");
    hcDualAxis[2] = new HeaderCell("Axis");
    hcMontage[0] = new HeaderCell();
    hcMontage[1] = new HeaderCell();
    hcMontage[2] = new HeaderCell("Montage");
    hcExcludeViews[0] = new HeaderCell("Exclude");
    hcExcludeViews[1] = new HeaderCell("Views");
    hcExcludeViews[2] = new HeaderCell("from A");
    hcBoundaryModel[0] = new HeaderCell();
    hcBoundaryModel[1] = new HeaderCell("Boundary");
    hcBoundaryModel[2] = new HeaderCell("Model");
    hcTwoSurfaces[0] = new HeaderCell("Beads");
    hcTwoSurfaces[1] = new HeaderCell("on Two");
    hcTwoSurfaces[2] = new HeaderCell("Surfaces");
    hcEditDataset[0] = new HeaderCell("Dataset");
    hcEditDataset[1] = new HeaderCell("Specific");
    hcEditDataset[2] = new HeaderCell("Values");
    hc3dmod[0] = new HeaderCell("Open");
    hc3dmod[1] = new HeaderCell("Stack");
    hc3dmod[2] = new HeaderCell("A");
    // rub tab
    hcStatus[0] = new HeaderCell();
    hcStatus[1] = new HeaderCell("Status");
    hcRun[0] = new HeaderCell();
    hcRun[1] = new HeaderCell("Run");
    hcEtomo[0] = new HeaderCell("Open");
    hcEtomo[1] = new HeaderCell("Dataset");
    // remove table
    rowList.removeAll();
    pnlTable.removeAll();
    // header
    int max;
    if (curTab == BatchRunTomoTab.STACKS) {
      max = NUM_STACKS_HEADER_ROWS;
      for (int i = 0; i < max; i++) {
        addStandardHeaders(i, max);
        constraints.gridwidth = 1;
        hcDualAxis[i].add(pnlTable, layout, constraints);
        hcMontage[i].add(pnlTable, layout, constraints);
        // exclude views has A and B columns
        if (i < max - 1) {
          constraints.gridwidth = 2;
        }
        hcExcludeViews[i].add(pnlTable, layout, constraints);
        constraints.gridwidth = 1;
        if (i == max - 1) {
          hcExcludeViewsB.add(pnlTable, layout, constraints);
        }
        hcBoundaryModel[i].add(pnlTable, layout, constraints);
        hcTwoSurfaces[i].add(pnlTable, layout, constraints);
        hcEditDataset[i].add(pnlTable, layout, constraints);
        if (i < max - 1) {
          constraints.gridwidth = GridBagConstraints.REMAINDER;
        }
        hc3dmod[i].add(pnlTable, layout, constraints);
        if (i == max - 1) {
          constraints.gridwidth = GridBagConstraints.REMAINDER;
          hc3dmodB.add(pnlTable, layout, constraints);
        }
      }
    }
    else if (curTab == BatchRunTomoTab.RUN) {
      max = NUM_RUN_HEADER_ROWS;
      for (int i = 0; i < max; i++) {
        // Use the last two rows of the standard headers
        addStandardHeaders(i + 1, max + 1);
        constraints.gridwidth = 1;
        hcStatus[i].add(pnlTable, layout, constraints);
        hcRun[i].add(pnlTable, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        hcEtomo[i].add(pnlTable, layout, constraints);
      }
    }
    // rows
    rowList.removeAll();
    rowList.display(viewport);
    // buttons
    pnlButtons.setVisible(curTab == BatchRunTomoTab.STACKS);
  }

  private void addStandardHeaders(final int index, final int max) {
    // the stacks tab rows are highlightable
    if (curTab == BatchRunTomoTab.STACKS) {
      constraints.gridwidth = 2;
    }
    else {
      constraints.gridwidth = 1;
    }
    hcNumber[index].add(pnlTable, layout, constraints);
    // The stack header has a button on the bottom row
    if (index == max - 1) {
      constraints.gridwidth = 1;
    }
    else {
      constraints.gridwidth = 2;
    }
    hcStack[index].add(pnlTable, layout, constraints);
    if (index == max - 1) {
      btnStack.add(pnlTable, layout, constraints);
    }
  }

  private void addListeners() {
    btnAdd.addActionListener(this);
    btnCopyDown.addActionListener(this);
    btnDelete.addActionListener(this);
    btnEditDataset.addActionListener(editDatasetListener);
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
  }

  /**
   * Check each field to see if it has been changed from its checkpoint.  If it has
   * changed, then back up its current value.
   * @return true if any field has been changed from its checkpoint
   */
  boolean backupIfChanged() {
    return rowList.backupIfChanged();
  }

  /**
   * Move any backed up values into the field, and delete the backup.
   */
  void restoreFromBackup() {
    rowList.restoreFromBackup();
  }

  void setValues(final DirectiveFileCollection directiveFileCollection) {
    rowList.setValues(directiveFileCollection);
  }

  void clear() {
    rowList.clear();
  }

  void setValues(final UserConfiguration userConfiguration) {
    rowList.setValues(userConfiguration);
  }

  void checkpoint() {
    rowList.checkpoint();
  }

  private void updateDisplay() {
    int size = rowList.size();
    boolean enable = size > 0 && rowList.isHighlighted();
    btnDelete.setEnabled(enable);
    btnEditDataset.setEnabled(enable);
    int index = rowList.getHighlightedIndex();
    btnCopyDown.setEnabled(index != -1 && index < size - 1);
  }

  void msgTabChanged(final BatchRunTomoTab tab) {
    if (tab == BatchRunTomoTab.STACKS || tab == BatchRunTomoTab.RUN) {
      if (tab != curTab) {
        curTab = tab;
        rebuildTable();
      }
    }
  }

  public void msgViewportPaged() {
    rowList.removeAll();
    rowList.display(viewport);
    UIHarness.INSTANCE.pack(manager);
  }

  public void highlight(final boolean highlight) {
    updateDisplay();
  }

  public void actionPerformed(final ActionEvent actionEvent) {
    if (actionEvent == null) {
      return;
    }
    String actionCommand = actionEvent.getActionCommand();
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
    else if (actionCommand.equals(btnDelete.getActionCommand())) {
      rowList.delete();
      rebuildTable();
      updateDisplay();
      UIHarness.INSTANCE.pack(manager);
    }
    else if (actionCommand.equals(btnCopyDown.getActionCommand())) {
      rowList.copyDown();
    }
  }

  String getEditDatasetActionCommand() {
    return btnEditDataset.getActionCommand();
  }

  String getHighlightedKey() {
    return rowList.getKey();
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

  private final class RowList {
    private final List<BatchRunTomoRow> list = new ArrayList<BatchRunTomoRow>();

    private final BatchRunTomoTable table;

    private BatchRunTomoRow initialValueRow = null;

    private RowList(final BatchRunTomoTable table) {
      this.table = table;
    }

    private void add(final File[] stackList) {
      if (stackList == null) {
        return;
      }
      int firstIndex = list.size();
      for (int i = 0; i < stackList.length; i++) {
        int index = list.size();
        BatchRunTomoRow prevRow = null;
        if (index > 0) {
          prevRow = list.get(index - 1);
        }
        BatchRunTomoRow row = BatchRunTomoRow.getInstance(manager.getPropertyUserDir(),
            table, pnlTable, layout, constraints, index + 1, stackList[i],
            (prevRow != null ? prevRow : initialValueRow));
        row.expandStack(btnStack.isExpanded());
        list.add(row);
        row.display(viewport, curTab);
      }
      viewport.adjustViewport(firstIndex);
      rowList.removeAll();
      rowList.display(viewport);
      UIHarness.INSTANCE.pack(manager);
      updateDisplay();
    }

    private void delete() {
      int index = getHighlightedIndex();
      if (index != -1) {
        BatchRunTomoRow row = list.get(index);
        if (row == null) {
          return;
        }
        row.remove();
        list.remove(index);
        viewport.adjustViewport(index);
      }
      for (int i = index; i < list.size(); i++) {
        list.get(i).setNumber(i + 1);
      }
      // Highlight the row after the deleted row, or the previous one if at the end of the
      // table.
      if (index == list.size()) {
        index--;
      }
      highlight(index);
    }

    private void highlight(final int index) {
      if (index < 0 || index >= list.size()) {
        return;
      }
      list.get(index).selectHighlightButton();
    }

    private void expandStack(final boolean expanded) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).expandStack(expanded);
      }
    }

    private void removeAll() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).remove();
      }
    }

    private void display(Viewport viewport) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).display(viewport, curTab);
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

    private void setEditDataset() {
      int index = getHighlightedIndex();
      if (index != -1) {
        list.get(index).setEditDataset();
      }
    }

    private int getHighlightedIndex() {
      for (int i = 0; i < list.size(); i++) {
        BatchRunTomoRow row = list.get(i);
        if (row.isHighlighted()) {
          return i;
        }
      }
      return -1;
    }

    private String getKey() {
      int index = getHighlightedIndex();
      if (index != -1 && index < list.size()) {
        return list.get(index).getExpandedStack();
      }
      return null;
    }

    private void copyDown() {
      int index = getHighlightedIndex();
      if (index != -1 && index < list.size() - 1) {
        list.get(index + 1).copy(list.get(index));
      }
    }

    /**
     * Check each field to see if it has been changed from its checkpoint.  If it has
     * changed, then back up its current value.
     * @return true if any field has been changed from its checkpoint
     */
    private boolean backupIfChanged() {
      boolean changed = false;
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).backupIfChanged()) {
          changed = true;
        }
      }
      return changed;
    }

    /**
     * Move any backed up values into the field, and delete the backup.
     */
    private void restoreFromBackup() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).restoreFromBackup();
      }
    }

    private void setValues(final DirectiveFileCollection directiveFileCollection) {
      if (initialValueRow == null) {
        initialValueRow = BatchRunTomoRow.getDefaultsInstance();
      }
      initialValueRow.setValues(directiveFileCollection);
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setValues(directiveFileCollection);
      }
    }

    private void clear() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).clear();
      }
    }

    private void setValues(final UserConfiguration userConfiguration) {
      if (initialValueRow == null) {
        initialValueRow = BatchRunTomoRow.getDefaultsInstance();
      }
      initialValueRow.setValues(userConfiguration);
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setValues(userConfiguration);
      }
    }

    private void checkpoint() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).checkpoint();
      }
    }
  }
}
