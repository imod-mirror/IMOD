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
final class BatchRunTomoTable implements Viewable, Highlightable, Expandable {
  public static final String rcsid = "$Id:$";

  private static final String STACK_TITLE = "Stack";
  private static final int NUM_HEADER_ROWS = 2;

  private final JPanel pnlRoot = new JPanel();
  private final RowList rowList = new RowList(this);
  private final JPanel pnlTable = new JPanel();
  private final GridBagLayout layout = new GridBagLayout();
  private final GridBagConstraints constraints = new GridBagConstraints();
  private final HeaderCell[] hcNumber = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcStack = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcDualAxis = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcMontage = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcExcludeViews = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcBoundaryModel = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcTwoSurfaces = new HeaderCell[NUM_HEADER_ROWS];
  private final MultiLineButton btnAdd = new MultiLineButton("Add Stack(s)");
  private final MultiLineButton btnCopy = new MultiLineButton("Copy Down");
  private final MultiLineButton btnDelete = new MultiLineButton("Delete");
  private final MultiLineButton btnEditDataset = new MultiLineButton(
      "Set Dataset Specific Data");
  private final ExpandButton btnStack = ExpandButton.getInstance(this,
      ExpandButton.Type.MORE);
  private final HeaderCell[] hcStatus = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcRun = new HeaderCell[NUM_HEADER_ROWS];
  private final HeaderCell[] hcEtomo = new HeaderCell[NUM_HEADER_ROWS];
  private final JPanel pnlButtons = new JPanel();
  private final HeaderCell[] hc3dmod = new HeaderCell[NUM_HEADER_ROWS];

  private final BaseManager manager;
  private final Viewport viewport;

  private File currentDirectory = null;
  private BatchRunTomoTab curTab = null;

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
    btnAdd.setToPreferredSize();
    btnCopy.setToPreferredSize();
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
    // align
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
    viewport.adjustViewport(-1);
    updateDisplay();
  }

  private void rebuildTable() {
    // init
    hcNumber[0] = new HeaderCell("#");
    hcNumber[1] = new HeaderCell();
    hcStack[0] = new HeaderCell(STACK_TITLE);
    hcStack[1] = new HeaderCell();
    hcDualAxis[0] = new HeaderCell("Dual");
    hcDualAxis[1] = new HeaderCell("Axis");
    hcMontage[0] = new HeaderCell("Montage");
    hcMontage[1] = new HeaderCell();
    hcExcludeViews[0] = new HeaderCell("Exclude");
    hcExcludeViews[1] = new HeaderCell("Views");
    hcBoundaryModel[0] = new HeaderCell("Boundary");
    hcBoundaryModel[1] = new HeaderCell("Model");
    hcTwoSurfaces[0] = new HeaderCell("Beads on");
    hcTwoSurfaces[1] = new HeaderCell("Two Surfaces");
    hcStatus[0] = new HeaderCell("Status");
    hcStatus[1] = new HeaderCell();
    hcRun[0] = new HeaderCell("Run");
    hcRun[1] = new HeaderCell();
    hcEtomo[0] = new HeaderCell("Open");
    hcEtomo[1] = new HeaderCell("Dataset");
    hc3dmod[0] = new HeaderCell("Open");
    hc3dmod[1] = new HeaderCell("Stack");
    // remove table
    rowList.removeAll();
    pnlTable.removeAll();
    // header
    for (int i = 0; i < NUM_HEADER_ROWS; i++) {
      if (curTab == BatchRunTomoTab.STACKS) {
        constraints.gridwidth = 2;
      }
      else {
        constraints.gridwidth = 1;
      }
      hcNumber[i].add(pnlTable, layout, constraints);
      constraints.gridwidth = i + 1;
      hcStack[i].add(pnlTable, layout, constraints);
      if (i == 0) {
        btnStack.add(pnlTable, layout, constraints);
      }
      constraints.gridwidth = 1;
      if (curTab == BatchRunTomoTab.STACKS) {
        hcBoundaryModel[i].add(pnlTable, layout, constraints);
        hcDualAxis[i].add(pnlTable, layout, constraints);
        hcMontage[i].add(pnlTable, layout, constraints);
        hcExcludeViews[i].add(pnlTable, layout, constraints);
        hcTwoSurfaces[i].add(pnlTable, layout, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        hc3dmod[i].add(pnlTable, layout, constraints);
      }
      else {
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

  void setFieldHighlightValues(final DirectiveFileCollection directiveFileCollection) {
    rowList.setFieldHighlightValues(directiveFileCollection);
  }

  void setValues(final UserConfiguration userConfiguration) {
    rowList.setValues(userConfiguration);
  }

  void checkpoint() {
    rowList.checkpoint();
  }

  private void updateDisplay() {
    boolean enable = rowList.size() > 0;
    boolean highlighted = rowList.isHighlighted();
    btnCopy.setEnabled(enable && highlighted);
    btnDelete.setEnabled(enable && highlighted);
    btnEditDataset.setEnabled(enable && highlighted);
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
            table, pnlTable, layout, constraints, index + 1, stackList[i], prevRow);
        row.expandStack(btnStack.isExpanded());
        list.add(row);
        viewport.adjustViewport(index);
        row.display(viewport, curTab);
      }
      UIHarness.INSTANCE.pack(manager);
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

    /**
     * Check each field to see if it has been changed from its checkpoint.  If it has
     * changed, then back up its current value.
     * @return true if any field has been changed from its checkpoint
     */
    boolean backupIfChanged() {
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
    void restoreFromBackup() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).restoreFromBackup();
      }
    }

    void setValues(final DirectiveFileCollection directiveFileCollection) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setValues(directiveFileCollection);
      }
    }
    void setFieldHighlightValues(final DirectiveFileCollection directiveFileCollection) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setFieldHighlightValues(directiveFileCollection);
      }
    }
    void setValues(final UserConfiguration userConfiguration) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).setValues(userConfiguration);
      }
    }

    void checkpoint() {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).checkpoint();
      }
    }
  }
}
