package etomo.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import etomo.BaseManager;
import etomo.JoinManager;
import etomo.storage.LogFile;
import etomo.type.ConstJoinMetaData;
import etomo.type.JoinMetaData;
import etomo.type.JoinScreenState;

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
 * <p> Revision 1.1  2007/02/05 23:34:00  sueh
 * <p> bug# 962 Class representing the boundary table.
 * <p> </p>
 */
final class BoundaryTable {
  public static final String rcsid = "$Id$";

  private final JPanel rootPanel = new JPanel();
  private final JPanel pnlTable = new JPanel();
  private final GridBagLayout layout = new GridBagLayout();
  private final GridBagConstraints constraints = new GridBagConstraints();
  private final RowList rowList = new RowList();

  //header
  //first row
  private final HeaderCell header1Boundaries = new HeaderCell("Boundaries");
  private final HeaderCell header1Sections = new HeaderCell("Sections");
  private final HeaderCell header1BestGap = new HeaderCell("Best");
  private final HeaderCell header1Error = new HeaderCell("Error");
  private final HeaderCell header1Original = new HeaderCell("Original");
  private final HeaderCell header1Adjusted = new HeaderCell("Adjusted");
  //second row
  private final HeaderCell header2Boundaries = new HeaderCell();
  private final HeaderCell header2Sections = new HeaderCell();
  private final HeaderCell header2BestGap = new HeaderCell("Gap");
  private final HeaderCell header2MeanError = new HeaderCell("Mean");
  private final HeaderCell header2MaxError = new HeaderCell("Max");
  private final HeaderCell header2OriginalEnd = new HeaderCell();
  private final HeaderCell header2OriginalStart = new HeaderCell();
  private final HeaderCell header2AdjustedEnd = new HeaderCell();
  private final HeaderCell header2AdjustedStart = new HeaderCell();
  //third row
  private final HeaderCell header3Sections = new HeaderCell();
  private final HeaderCell header3BestGap = new HeaderCell();
  private final HeaderCell header3OriginalEnd = new HeaderCell("End");
  private final HeaderCell header3OriginalStart = new HeaderCell("Start");
  private final HeaderCell header3AdjustedEnd = new HeaderCell("End");
  private final HeaderCell header3AdjustedStart = new HeaderCell("Start");

  private final JoinManager manager;
  private final JoinDialog parent;
  private final JoinScreenState screenState;
  private final JoinMetaData metaData;

  private boolean rowChange = true;
  private JoinDialog.Tab tab = null;

  BoundaryTable(JoinManager manager, JoinDialog joinDialog) {
    this.manager = manager;
    screenState = manager.getScreenState();
    metaData = manager.getJoinMetaData();
    parent = joinDialog;
    //construct panels
    SpacedPanel pnlBorder = new SpacedPanel();
    //root panel
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
    rootPanel.add(pnlBorder.getContainer());
    //border pane
    pnlBorder.setBoxLayout(BoxLayout.Y_AXIS);
    pnlBorder.setBorder(new EtchedBorder("Boundary Table").getBorder());
    pnlBorder.add(pnlTable);
    //table panel
    pnlTable.setLayout(layout);
    pnlTable.setBorder(LineBorder.createBlackLineBorder());
    constraints.fill = GridBagConstraints.BOTH;
    header1BestGap.pad();
    header3OriginalEnd.pad();
    header3OriginalStart.pad();
    setToolTipText();
  }

  void setXfjointomoResult() throws LogFile.ReadException {
    rowList.setXfjointomoResult(manager);
  }

  /**
   * Updates and displays the table as necessary.  Does nothing if the tab has
   * not changed and rowChange is false.
   */
  void display() {
    JoinDialog.Tab oldTab = tab;
    tab = parent.getTab();
    if (oldTab == tab && !rowChange) {
      return;
    }
    rowList.removeDisplay();
    pnlTable.removeAll();
    addHeader();
    addRows();
    manager.getMainPanel().repaint();
  }

  /**
   * Causes the rows in the table to be deleted and recreated when the table is
   * displayed.
   */
  void msgRowChange() {
    rowChange = true;
    //when addRows() is called, it will load from screenState and metaData, so
    //they need to be empty if they are out of date.
    BoundaryRow.resetScreenState(screenState);
    BoundaryRow.resetMetaData(metaData);
  }

  void getScreenState() {
    BoundaryRow.resetScreenState(screenState);
    rowList.getScreenState(screenState);
  }

  void getMetaData() {
    BoundaryRow.resetMetaData(metaData);
    rowList.getMetaData(metaData);
  }

  Container getContainer() {
    return rootPanel;
  }

  private void setToolTipText() {
    String text = "Boundaries between sections.";
    header1Boundaries.setToolTipText(text);
    header2Boundaries.setToolTipText(text);
    text="The pairs of sections which define each boundary.";
    header1Sections.setToolTipText(text);
    header2Sections.setToolTipText(text);
    header3Sections.setToolTipText(text);
    text = "Describes how the final start and end values will change when the join is recreated, "
        + "with a positive gap adding slices and a negative gap removing slices at the corresponding boundary.";
    header1BestGap.setToolTipText(text);
    header2BestGap.setToolTipText(text);
    header3BestGap.setToolTipText(text);
    header1Error
        .setToolTipText("Deviations between transformed points extrapolated from above and below the corresponding boundary.");
    header2MeanError.setToolTipText("Mean deviations.");
    header2MaxError.setToolTipText("Maximum deviations.");
    text ="End and start values used to create the original join.";
    header1Original.setToolTipText(text);
    header2OriginalEnd.setToolTipText(text);
    header2OriginalStart.setToolTipText(text);
    header3OriginalEnd.setToolTipText("End values used to create the original join.");
    header3OriginalStart.setToolTipText("Start values used to create the original join.");
    text="End and start values which will be used to create the new join.";
    header1Adjusted.setToolTipText(text);
    header2AdjustedEnd.setToolTipText(text);
    header2AdjustedStart.setToolTipText(text);
    header3AdjustedEnd.setToolTipText("End values which will be used to create the new join.");
    header3AdjustedStart.setToolTipText("Start values which will be used to create the new join.");
  }

  private void addHeader() {
    if (tab == JoinDialog.Tab.MODEL) {
      addModelHeader();
    }
    else if (tab == JoinDialog.Tab.REJOIN) {
      addRejoinHeader();
    }
  }

  private void addRejoinHeader() {
    //Header
    //First row
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    header1Sections.add(pnlTable, layout, constraints);
    constraints.weightx = 0.1;
    constraints.gridwidth = 2;
    header1Original.add(pnlTable, layout, constraints);
    constraints.gridwidth = 1;
    header1BestGap.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    header1Adjusted.add(pnlTable, layout, constraints);
    //second row
    constraints.weightx = 0.0;
    constraints.gridwidth = 1;
    header2Sections.add(pnlTable, layout, constraints);
    constraints.weightx = 0.1;
    header2OriginalEnd.add(pnlTable, layout, constraints);
    header2OriginalStart.add(pnlTable, layout, constraints);
    header2BestGap.add(pnlTable, layout, constraints);
    header2AdjustedEnd.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    header2AdjustedStart.add(pnlTable, layout, constraints);
    //third row
    constraints.weightx = 0.0;
    constraints.gridwidth = 1;
    header3Sections.add(pnlTable, layout, constraints);
    constraints.weightx = 0.1;
    header3OriginalEnd.add(pnlTable, layout, constraints);
    header3OriginalStart.add(pnlTable, layout, constraints);
    header3BestGap.add(pnlTable, layout, constraints);
    header3AdjustedEnd.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    header3AdjustedStart.add(pnlTable, layout, constraints);
  }

  private void addModelHeader() {
    //Header
    //First row
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    header1Boundaries.add(pnlTable, layout, constraints);
    constraints.weightx = 0.1;
    header1BestGap.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    header1Error.add(pnlTable, layout, constraints);
    //second row
    constraints.weightx = 0.0;
    constraints.gridwidth = 1;
    header2Boundaries.add(pnlTable, layout, constraints);
    constraints.weightx = 0.1;
    header2BestGap.add(pnlTable, layout, constraints);
    header2MeanError.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    header2MaxError.add(pnlTable, layout, constraints);
  }

  /**
   * Displays the rows.  Updates the rows when rowChange is true.
   */
  private void addRows() {
    if (rowChange) {
      rowChange = false;
      rowList.clear();
      parent.getSectionTable().getMetaData(metaData);
      rowList.add(parent.getSectionTableSize(), metaData, screenState,
          pnlTable, layout, constraints);
    }
    rowList.display(tab);
  }

  /**
   * A list of BoundaryRow classes.  Has the functionality of an array and can
   * also run BoundaryRow functions on the whole list.
   */
  private static final class RowList {
    private final ArrayList list = new ArrayList();

    /**
     * Clears the list.
     */
    void clear() {
      list.clear();
    }

    /**
     * @return the list size
     */
    int size() {
      return list.size();
    }

    /**
     * Runs BoundaryRow.removeDisplay().
     */
    void removeDisplay() {
      for (int i = 0; i < list.size(); i++) {
        get(i).removeDisplay();
      }
    }

    /**
     * Runs BoundaryRow.display().
     */
    void display(JoinDialog.Tab tab) {
      for (int i = 0; i < list.size(); i++) {
        get(i).display(tab);
      }
    }

    void setXfjointomoResult(BaseManager manager) throws LogFile.ReadException {
      for (int i = 0; i < list.size(); i++) {
        get(i).setXfjointomoResult(manager);
      }
    }

    void getScreenState(JoinScreenState screenState) {
      for (int i = 0; i < list.size(); i++) {
        get(i).getScreenState(screenState);
      }
    }

    void getMetaData(JoinMetaData metaData) {
      for (int i = 0; i < list.size(); i++) {
        get(i).getMetaData(metaData);
      }
    }

    /**
     * Adds new BoundaryRow instances to the list.  The number of instances
     * added equals size - 1.  The number parameter in the BoundaryRow
     * constructor starts at 1.
     * @param size
     * @param panel
     * @param layout
     * @param constraints
     */
    void add(int size, ConstJoinMetaData metaData, JoinScreenState screenState,
        JPanel panel, GridBagLayout layout, GridBagConstraints constraints) {
      for (int i = 1; i < size; i++) {
        list.add(new BoundaryRow(i, metaData, screenState, panel, layout,
            constraints));
      }
    }

    /**
     * @param index
     * @return an element of the list
     */
    private BoundaryRow get(int index) {
      return (BoundaryRow) list.get(index);
    }
  }
}
