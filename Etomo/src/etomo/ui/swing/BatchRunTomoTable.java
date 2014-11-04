package etomo.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import etomo.BatchRunTomoManager;
import etomo.comscript.BatchruntomoParam;
import etomo.logic.DatasetTool;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.StackFileFilter;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoMetaData;
import etomo.type.DuplicateException;
import etomo.type.NotLoadedException;
import etomo.type.TableReference;
import etomo.type.UserConfiguration;
import etomo.ui.BatchRunTomoTab;
import etomo.ui.PreferredTableSize;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2013</p>
 * <p/>
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 *
 * @author $Author$
 * @version $Revision$
 *          <p/>
 *          <p> $Log$ </p>
 */
final class BatchRunTomoTable
    implements Viewable, Highlightable, Expandable, ActionListener {
  public static final String rcsid = "$Id:$";

  private static final String STACK_TITLE = "Stack";
  private static final int MAX_HEADER_ROWS = 3;
  private static final int NUM_STACKS_HEADER_ROWS = MAX_HEADER_ROWS;
  private static final int NUM_DATASET_HEADER_ROWS = 1;
  private static final int NUM_RUN_HEADER_ROWS = 2;

  private final JPanel pnlRoot = new JPanel();
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
  private final HeaderCell[] hc3dmod = new HeaderCell[NUM_STACKS_HEADER_ROWS];
  private final HeaderCell hc3dmodB = new HeaderCell("B");
  // dataset tab
  private final HeaderCell[] hcEditDataset = new HeaderCell[NUM_DATASET_HEADER_ROWS];
  // run tab
  private final HeaderCell[] hcStatus = new HeaderCell[NUM_RUN_HEADER_ROWS];
  private final HeaderCell[] hcRun = new HeaderCell[NUM_RUN_HEADER_ROWS];
  private final HeaderCell[] hcEtomo = new HeaderCell[NUM_RUN_HEADER_ROWS];
  private final MultiLineButton btnAdd = new MultiLineButton("Add Stack(s)");
  private final MultiLineButton btnCopyDown = new MultiLineButton("Copy Down");
  private final MultiLineButton btnDelete = new MultiLineButton("Delete");
  private final JPanel pnlStackButtons = new JPanel();

  private final RowList rowList;
  private final BatchRunTomoManager manager;
  private final Viewport viewport;
  private final ExpandButton btnStack;
  private final PreferredTableSize preferredTableSize =
      new PreferredTableSize(DatasetColumn.TOTAL);

  private File currentDirectory = null;
  private BatchRunTomoTab curTab = null;

  private BatchRunTomoTable(final BatchRunTomoManager manager,
      final Expandable expandable, final TableReference tableReference) {
    this.manager = manager;
    rowList = new RowList(this, tableReference);
    viewport = new Viewport(this, 20, null, null, null, "BatchRunTomo");
    btnStack = ExpandButton.getInstance(this, expandable, ExpandButton.Type.MORE);
  }

  static BatchRunTomoTable getInstance(final BatchRunTomoManager manager,
      final BatchRunTomoDialog dialog, final TableReference tableReference) {
    BatchRunTomoTable instance = new BatchRunTomoTable(manager, dialog, tableReference);
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
    btnStack.setName(STACK_TITLE);
    // all table tabs
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
    hc3dmod[0] = new HeaderCell("Open");
    hc3dmod[1] = new HeaderCell("Stack");
    hc3dmod[2] = new HeaderCell("A");
    // dataset tab
    hcEditDataset[0] = new HeaderCell("Specific Values");
    // run tab
    hcStatus[0] = new HeaderCell();
    hcStatus[1] = new HeaderCell("Status");
    hcRun[0] = new HeaderCell();
    hcRun[1] = new HeaderCell("Run");
    hcEtomo[0] = new HeaderCell("Open");
    hcEtomo[1] = new HeaderCell("Dataset");
    // preferred width of the dataset view
    preferredTableSize.addColumn(DatasetColumn.NUMBER.index, hcNumber[2]);
    preferredTableSize.addColumn(DatasetColumn.STACK.index, hcStack[2], btnStack);
    preferredTableSize.addColumn(DatasetColumn.EDIT_DATASET.index, hcEditDataset[0]);
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.add(pnlView);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlRoot.add(pnlStackButtons);
    // View
    pnlView.setLayout(new BoxLayout(pnlView, BoxLayout.X_AXIS));
    pnlView.add(viewport.getPagingPanel());
    pnlView.add(pnlTable);
    // stack Buttons
    pnlStackButtons.setLayout(new BoxLayout(pnlStackButtons, BoxLayout.X_AXIS));
    pnlStackButtons.add(Box.createHorizontalGlue());
    pnlStackButtons.add(btnAdd.getComponent());
    pnlStackButtons.add(Box.createHorizontalGlue());
    pnlStackButtons.add(btnCopyDown.getComponent());
    pnlStackButtons.add(Box.createHorizontalGlue());
    pnlStackButtons.add(btnDelete.getComponent());
    pnlStackButtons.add(Box.createHorizontalGlue());
    // Table
    pnlTable.setLayout(layout);
    pnlTable.setBorder(LineBorder.createBlackLineBorder());
    // constraints
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridheight = 1;
    constraints.weighty = 1.0;
    // align
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
    viewport.adjustViewport(-1);
    updateDisplay();
  }

  int getPreferredWidth() {
    return preferredTableSize.getPreferredWidth();
  }

  private void rebuildTable() {
    // remove table
    rowList.removeAll();
    pnlTable.removeAll();
    // header
    constraints.weightx = 0.0;
    int numRows;
    if (curTab == BatchRunTomoTab.STACKS) {
      numRows = NUM_STACKS_HEADER_ROWS;
      for (int i = 0; i < numRows; i++) {
        addStandardHeaders(i, numRows);
        constraints.gridwidth = 1;
        hcDualAxis[i].add(pnlTable, layout, constraints);
        hcMontage[i].add(pnlTable, layout, constraints);
        // exclude views has A and B columns
        if (i < numRows - 1) {
          constraints.gridwidth = 2;
        }
        hcExcludeViews[i].add(pnlTable, layout, constraints);
        constraints.gridwidth = 1;
        if (i == numRows - 1) {
          hcExcludeViewsB.add(pnlTable, layout, constraints);
        }
        hcBoundaryModel[i].add(pnlTable, layout, constraints);
        hcTwoSurfaces[i].add(pnlTable, layout, constraints);
        if (i < numRows - 1) {
          constraints.gridwidth = GridBagConstraints.REMAINDER;
        }
        hc3dmod[i].add(pnlTable, layout, constraints);
        if (i == numRows - 1) {
          constraints.gridwidth = GridBagConstraints.REMAINDER;
          hc3dmodB.add(pnlTable, layout, constraints);
        }
      }
    }
    else if (curTab == BatchRunTomoTab.DATASET) {
      numRows = NUM_DATASET_HEADER_ROWS;
      for (int i = 0; i < numRows; i++) {
        addStandardHeaders(i, numRows);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 100.0;
        hcEditDataset[i].add(pnlTable, layout, constraints);
        constraints.weightx = 0.0;
      }
    }
    else if (curTab == BatchRunTomoTab.RUN) {
      numRows = NUM_RUN_HEADER_ROWS;
      for (int i = 0; i < numRows; i++) {
        addStandardHeaders(i, numRows);
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
    pnlStackButtons.setVisible(curTab == BatchRunTomoTab.STACKS);
  }

  private void addStandardHeaders(int index, int numRows) {
    // Use the bottom rows of the standard headers
    if (numRows < MAX_HEADER_ROWS) {
      int shift = MAX_HEADER_ROWS - numRows;
      index += shift;
      numRows += shift;
    }
    // the stacks tab rows are highlightable
    if (curTab == BatchRunTomoTab.STACKS) {
      constraints.gridwidth = 2;
    }
    else {
      constraints.gridwidth = 1;
    }
    int newPreferredWidth = 0;
    hcNumber[index].add(pnlTable, layout, constraints);
    // The stack header has a button on the bottom row
    if (index == numRows - 1) {
      constraints.gridwidth = 1;
    }
    else {
      constraints.gridwidth = 2;
    }
    constraints.weightx = 10.0;
    int width = 0;
    hcStack[index].add(pnlTable, layout, constraints);
    if (curTab == BatchRunTomoTab.DATASET) {
      // Save the maximum width of the stack column - this is the minimum width of the
      // stack in each row.
      width = hcStack[index].getPreferredWidth();
    }
    constraints.weightx = 0.0;
    if (index == numRows - 1) {
      btnStack.add(pnlTable, layout, constraints);
    }
  }

  private void addListeners() {
    btnAdd.addActionListener(this);
    btnCopyDown.addActionListener(this);
    btnDelete.addActionListener(this);
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

  public void setParameters(final BatchRunTomoMetaData metaData) {
    rowList.setParameters(metaData);
  }

  public void getParameters(final BatchRunTomoMetaData metaData) {
    rowList.getParameters(metaData);
  }

  public void getParameters(final BatchruntomoParam param) {
    rowList.getParameters(param);
  }

  void saveAutodocs() {
    rowList.saveAutodocs();
  }

  void loadAutodocs() {
    rowList.loadAutodocs();
  }

  /**
   * Check each field to see if it has been changed from its checkpoint.  If it has
   * changed, then back up its current value.
   *
   * @return true if any field has been changed from its checkpoint
   */
  boolean backupIfChanged() {
    return rowList.backupIfChanged();
  }

  void applyValues(final UserConfiguration userConfiguration,
      final DirectiveFileCollection directiveFileCollection,
      final boolean retainUserValues) {
    rowList.applyValues(userConfiguration, directiveFileCollection, retainUserValues);
  }

  private void updateDisplay() {
    int size = rowList.size();
    boolean enable = size > 0 && rowList.isHighlighted();
    btnDelete.setEnabled(enable);
    int index = rowList.getHighlightedIndex();
    btnCopyDown.setEnabled(index != -1 && index < size - 1);
  }

  void msgTabChanged(final BatchRunTomoTab tab) {
    if (tab == BatchRunTomoTab.STACKS || tab == BatchRunTomoTab.DATASET ||
        tab == BatchRunTomoTab.RUN) {
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
        // Remove matching B stacks and set dual to true for the A stack
        List<DatasetTool.StackInfo> filteredStackList =
            DatasetTool.removeMatchingBStacks(stackList);
        rowList.add(filteredStackList);
      }
    }
    else if (actionCommand.equals(btnDelete.getActionCommand())) {
      if (rowList.delete()) {
        rebuildTable();
        updateDisplay();
        UIHarness.INSTANCE.pack(manager);
      }
    }
    else if (actionCommand.equals(btnCopyDown.getActionCommand())) {
      rowList.copyDown();
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

  static final class DatasetColumn {
    static final DatasetColumn NUMBER = new DatasetColumn(0);
    static final DatasetColumn STACK = new DatasetColumn(1);
    static final DatasetColumn EDIT_DATASET = new DatasetColumn(2);

    static final int TOTAL = 3;
    private final int index;

    private DatasetColumn(final int index) {
      this.index = index;
    }

    int getIndex() {
      return index;
    }
  }

  private class RowList {
    private final List<BatchRunTomoRow> list = new ArrayList<BatchRunTomoRow>();

    private final TableReference tableReference;
    private final BatchRunTomoTable table;

    private BatchRunTomoRow initialValueRow = null;

    private RowList(final BatchRunTomoTable table, final TableReference tableReference) {
      this.tableReference = tableReference;
      this.table = table;
    }

    private void add(final List<DatasetTool.StackInfo> stackInfoList) {
      if (stackInfoList == null) {
        return;
      }
      int firstIndex = list.size();
      Iterator<DatasetTool.StackInfo> iterator = stackInfoList.iterator();
      boolean overridePrevRow = false;
      boolean prevDualAxisSet = false;
      boolean prevDualAxis = false;
      List<String> notAdded = new ArrayList<String>();
      boolean fileAdded = false;
      while (iterator.hasNext()) {
        DatasetTool.StackInfo stackInfo = iterator.next();
        Iterator<File> stackIterator = stackInfo.iterator();
        overridePrevRow = stackInfo.isDualAxis();
        while (stackIterator.hasNext()) {
          File stack = stackIterator.next();
          // See if there is an ID for this stack.
          String absPath = stack.getAbsolutePath();
          String stackID = tableReference.getID(absPath);
          if (stackID != null) {
            // Check for duplicate files
            if (rowExists(stackID)) {
              notAdded.add(absPath);
              continue;
            }
          }
          else {
            try {
              stackID = tableReference.put(absPath);
            }
            catch (DuplicateException e) {
              e.printStackTrace();
              continue;
            }
            catch (NotLoadedException e) {
              e.printStackTrace();
              if (!fileAdded) {
                return;
              }
              else {
                continue;
              }
            }
          }
          int index = list.size();
          BatchRunTomoRow prevRow = null;
          if (index > 0) {
            prevRow = list.get(index - 1);
          }
          else {
            prevRow = initialValueRow;
          }
          if (overridePrevRow) {
            if (!prevDualAxisSet) {
              prevDualAxis = prevRow.isDualAxis();
              prevDualAxisSet = true;
            }
          }
          BatchRunTomoRow row = BatchRunTomoRow
              .getInstance(table, pnlTable, layout, constraints, index + 1, stack,
                  prevRow, overridePrevRow, overridePrevRow, manager, stackID,
                  preferredTableSize);
          row.expandStack(btnStack.isExpanded());
          list.add(row);
          fileAdded = true;
          row.display(viewport, curTab);
        }
      }
      if (fileAdded) {
        viewport.adjustViewport(firstIndex);
        rowList.removeAll();
        rowList.display(viewport);
        UIHarness.INSTANCE.pack(manager);
        updateDisplay();
      }
      // Pop up a warning if there where any duplicate files.
      if (!notAdded.isEmpty()) {
        StringBuilder warning = new StringBuilder();
        warning.append("The stack table already contains ");
        Iterator<String> i = notAdded.iterator();
        if (i.hasNext()) {
          warning.append(i.next());
        }
        while (i.hasNext()) {
          String absPath = i.next();
          warning.append(", ");
          warning.append((!i.hasNext() ? " and " : ""));
          warning.append(absPath);
        }
        warning.append(".");
        UIHarness.INSTANCE
            .openMessageDialog(manager, warning.toString(), "Unable to Add File(s)");
      }
    }

    private void load(BatchRunTomoMetaData metaData) {
      int firstIndex = list.size();
      // entry<uniqueString, ID>
      Iterator<Entry<String, String>> iterator = tableReference.uniqueStringIDiterator();
      boolean fileAdded = false;
      while (iterator.hasNext()) {
        Entry<String, String> entry = iterator.next();
        String stackID = entry.getValue();
        if (metaData.isDisplay(stackID)) {
          int index = list.size();
          BatchRunTomoRow row = BatchRunTomoRow
              .getInstance(table, pnlTable, layout, constraints, index + 1,
                  new File(entry.getKey()), null, false, false, manager, stackID,
                  preferredTableSize);
          row.expandStack(btnStack.isExpanded());
          list.add(row);
          fileAdded = true;
          row.display(viewport, curTab);
        }
      }
      if (fileAdded) {
        viewport.adjustViewport(firstIndex);
        rowList.removeAll();
        rowList.display(viewport);
        UIHarness.INSTANCE.pack(manager);
        updateDisplay();
      }
    }

    private boolean rowExists(final String stackID) {
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).equalsStackID(stackID)) {
          return true;
        }
      }
      return false;
    }

    /**
     * @return true if a row was deleted
     */
    private boolean delete() {
      int index = getHighlightedIndex();
      if (index != -1) {
        BatchRunTomoRow row = list.get(index);
        if (row == null) {
          return false;
        }
        if (UIHarness.INSTANCE
            .openYesNoDialog(manager, "Delete the highlighted row?", AxisID.ONLY)) {
          row.remove();
          row.delete();
          list.remove(index);
          viewport.adjustViewport(index);
        }
        for (int i = index; i < list.size(); i++) {
          list.get(i).setNumber(i + 1);
        }
        // Highlight the row after the deleted row, or the previous one if at the end of
        // the
        // table.
        if (index == list.size()) {
          index--;
        }
        highlight(index);
        return true;
      }
      return false;
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

    private int getHighlightedIndex() {
      for (int i = 0; i < list.size(); i++) {
        BatchRunTomoRow row = list.get(i);
        if (row.isHighlighted()) {
          return i;
        }
      }
      return -1;
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
     *
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

    private void applyValues(final UserConfiguration userConfiguration,
        final DirectiveFileCollection directiveFileCollection,
        final boolean retainUserValues) {
      if (initialValueRow == null) {
        initialValueRow = BatchRunTomoRow.getDefaultsInstance();
      }
      initialValueRow.setValues(userConfiguration);
      initialValueRow.setValues(directiveFileCollection);
      for (int i = 0; i < list.size(); i++) {
        list.get(i)
            .applyValues(userConfiguration, directiveFileCollection, retainUserValues);
      }
    }

    public void setParameters(final BatchRunTomoMetaData metaData) {
      load(metaData);
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setParameters(metaData);
      }
    }

    public void getParameters(final BatchRunTomoMetaData metaData) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).getParameters(metaData);
      }
    }

    public void getParameters(final BatchruntomoParam param) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).getParameters(param);
      }
    }

    void saveAutodocs() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).saveAutodoc();
      }
    }

    void loadAutodocs() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).loadAutodoc();
      }
    }
  }
}
