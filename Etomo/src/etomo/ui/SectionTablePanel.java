package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import etomo.JoinManager;
import etomo.process.ImodManager;
import etomo.storage.TomogramFileFilter;
import etomo.type.ConstSectionTableRowData;
import etomo.type.JoinMetaData;
import etomo.type.SlicerAngles;
import etomo.util.InvalidParameterException;
import etomo.util.MRCHeader;

/**
* <p>Description: A panel containing the section table.  Implements Expandable
* so it can use ExpandButtons. </p>
* 
* <p>Copyright: Copyright (c) 2002, 2003, 2004</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$
* <p> Revision 1.1.2.12  2004/10/13 23:14:13  sueh
* <p> bug# 520 Allowed the components of the rootPanel and the table panel to
* <p> be removed and re-added.  This way the table can look different on
* <p> different tabs.
* <p>
* <p> Revision 1.1.2.11  2004/10/11 02:17:09  sueh
* <p> bug# 520 Using a variable called propertyUserDir instead of the "user.dir"
* <p> property.  This property would need a different value for each manager.
* <p> This variable can be retrieved from the manager if the object knows its
* <p> manager.  Otherwise it can retrieve it from the current manager using the
* <p> EtomoDirector singleton.  If there is no current manager, EtomoDirector
* <p> gets the value from the "user.dir" property.
* <p>
* <p> Revision 1.1.2.10  2004/10/08 16:36:00  sueh
* <p> bug# Using SectionTableRow.setRowNumber() to change the status of
* <p> sample slice numbers.
* <p>
* <p> Revision 1.1.2.9  2004/10/06 02:29:41  sueh
* <p> bug# 520 Fixed flip tomogram functionality.  If the user wants to flip the
* <p> tomogram, call JoinManager flip and exit.  Also disable the Add Section
* <p> button while the tomogram is being flipped.  When the flip process is
* <p> done, the process manager will call the function to add the section and
* <p> enable the Add Section button.
* <p>
* <p> Revision 1.1.2.8  2004/10/01 20:04:22  sueh
* <p> bug# 520 Moved fuctionality to create table headers and fields to
* <p> HeaderCell and FieldCell.  Fixed enable/disable of buttons.  Added
* <p> enable/disable of Add Section button.  Added functionality to Add
* <p> Section: checking for duplicate file paths and flipping tomogram if
* <p> necessary.  To do: add the flip command.
* <p>
* <p> Revision 1.1.2.7  2004/09/29 19:38:29  sueh
* <p> bug# 520 Added retrieveData() to retrieve data from the screen.
* <p>
* <p> Revision 1.1.2.6  2004/09/23 23:39:51  sueh
* <p> bug# 520 Converted to DoubleSpacedPanel and SpacedPanel.  Sized the
* <p> spinner.  Calling JoinDialog.setNumSections() when adding or deleting a
* <p> section.
* <p>
* <p> Revision 1.1.2.5  2004/09/22 22:14:43  sueh
* <p> bug# 520 Enabling and disabling buttons (enableRowButtons() and
* <p> enableTableButtons()).  Modified calls to work with the more genral
* <p> JoinManager.imod... functions.  Added get rotation angle functionality.
* <p>
* <p> Revision 1.1.2.4  2004/09/21 18:08:40  sueh
* <p> bug# 520 Moved buttons that affect the section table from JoinDialog to
* <p> this class.  Added move up, move down, add section, and delete section
* <p> buttons.  Added a binning spinnner and an open 3dmod button.  Added
* <p> functions to add existing fields to the table (such as
* <p> addHeader(JButton, text, width)).  Added removeFromTable and repaint.
* <p> To add, delete or move rows the grid bag layout, all effected rows and all
* <p> those below them must be removed and readded.
* <p>
* <p> Revision 1.1.2.3  2004/09/17 21:47:20  sueh
* <p> bug# 520 Added an array of rows.  Encapsulated each row into
* <p> SectionTableRow.  Encapsulated the expand button into ExpandButton.
* <p> Implemented row highlighting with highlighting(int), which is called by a
* <p> row that is turning on its highlighting.  Implemented expanding sections
* <p> by implementing Expandable with expand(ExpandButton) which tells
* <p> each row to expand its section display.  Factored cell creation code and
* <p> changed some cell creation fuctions to package level so they can be
* <p> called by SectionTableRow.
* <p>
* <p> Revision 1.1.2.2  2004/09/16 18:31:26  sueh
* <p> bug# 520 sized the fields, added a row number, reorganized
* <p> functions
* <p>
* <p> Revision 1.1.2.1  2004/09/15 22:47:26  sueh
* <p> bug# 520 creates the Sections table for JoinDialog.
* <p> </p>
*/
public class SectionTablePanel implements ContextMenu, Expandable {
  public static final String rcsid = "$Id$";

  private static final Dimension buttonDimension = UIParameters
      .getButtonDimension();
  private static final String flipWarning[] = { "Tomograms have to be flipped after generation",
  "in order to be in the right orientation for joining serial sections." };

  private DoubleSpacedPanel rootPanel;
  private JPanel pnlTable;
  private SpacedPanel pnlButtons;
  private SpacedPanel pnlImod;

  private ExpandButton btnExpandSections;
  private MultiLineButton btnMoveSectionUp;
  private MultiLineButton btnMoveSectionDown;
  private MultiLineButton btnAddSection;
  private MultiLineButton btnDeleteSection;
  private LabeledSpinner spinBinning;
  private MultiLineButton btnOpen3dmod;
  private MultiLineButton btnGetAngles;
  
  private HeaderCell hdrOrder;
  private HeaderCell hdrSections;
  private HeaderCell hdrSampleSlices;
  private HeaderCell hdrFinal;
  private HeaderCell hdrRotationAngles;
  private HeaderCell hdr1Row2;
  private HeaderCell hdr2Row2;
  private HeaderCell hdrBottom;
  private HeaderCell hdrTop;
  private HeaderCell hdr3Row2;
  private HeaderCell hdr4Row2;
  private HeaderCell hdr1Row3;
  private HeaderCell hdr2Row3;
  private HeaderCell hdrSampleSlicesBottomStart;
  private HeaderCell hdrSampleSlicesBottomEnd;
  private HeaderCell hdrSampleSlicesTopStart;
  private HeaderCell hdrSampleSlicesTopEnd;
  private HeaderCell hdrFinalStart;
  private HeaderCell hdrFinalEnd;
  private HeaderCell hdrRotationAnglesX;
  private HeaderCell hdrRotationAnglesY;
  private HeaderCell hdrRotationAnglesZ;

  private ArrayList rows = new ArrayList();

  private GridBagLayout layout = new GridBagLayout();
  private GridBagConstraints constraints = new GridBagConstraints();
  private SectionTableActionListener sectionTableActionListener = new SectionTableActionListener(
      this);

  private final JoinManager joinManager;
  private final JoinDialog joinDialog;
  
  private int curTab;

  /**
   * Creates the panel and table.
   *
   */
  SectionTablePanel(JoinDialog joinDialog, JoinManager joinManager, int curTab) {
    this.joinDialog = joinDialog;
    this.joinManager = joinManager;
    this.curTab = curTab;
    createRootPanel();
    addRootPanelComponents();
    enableTableButtons("");
    enableRowButtons(-1);
  }

  private void createRootPanel() {
    //create rootPanel in X axis to make room at the border
    rootPanel = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5,
        BorderFactory.createEtchedBorder());
    createTablePanel();
    addTablePanelComponents();
    createButtonsPanel();
  }
  
  private void addRootPanelComponents() {
    if (curTab != JoinDialog.SETUP_TAB) {
    }
    rootPanel.add(pnlTable);
    if (curTab == JoinDialog.SETUP_TAB) {
      rootPanel.add(pnlButtons);
    }
  }

  /**
   * Creates the panel and table.  Adds the header rows.  Adds SectionTableRows
   * to rows to create each row.
   *
   */
  private void createTablePanel() {
    pnlTable = new JPanel();
    pnlTable.setBorder(LineBorder.createBlackLineBorder());
    pnlTable.setLayout(layout);
    //Header
    //First row
    hdrOrder = new HeaderCell("Order");
    hdrSections = new HeaderCell("Sections", FixedDim.sectionsWidth);
    btnExpandSections = new ExpandButton(this);
    hdrSampleSlices = new HeaderCell("Sample Slices");
    hdrFinal = new HeaderCell("Final");
    hdrRotationAngles = new HeaderCell("Rotation Angles");
    //second row
    hdr1Row2 = new HeaderCell();
    hdr2Row2 = new HeaderCell();
    hdrBottom = new HeaderCell("Bottom");
    hdrTop = new HeaderCell("Top");
    hdr3Row2 = new HeaderCell();
    hdr4Row2 = new HeaderCell();
    //Third row
    hdr1Row3 = new HeaderCell();
    hdr2Row3 = new HeaderCell();
    hdrSampleSlicesBottomStart = new HeaderCell("Start", FixedDim.numericWidth);
    hdrSampleSlicesBottomEnd = new HeaderCell("End", FixedDim.numericWidth);
    hdrSampleSlicesTopStart = new HeaderCell("Start", FixedDim.numericWidth);
    hdrSampleSlicesTopEnd = new HeaderCell("End", FixedDim.numericWidth);
    hdrFinalStart = new HeaderCell("Start", FixedDim.numericWidth);
    hdrFinalEnd = new HeaderCell("End", FixedDim.numericWidth);
    hdrRotationAnglesX = new HeaderCell("X", FixedDim.numericWidth);
    hdrRotationAnglesY = new HeaderCell("Y", FixedDim.numericWidth);
    hdrRotationAnglesZ = new HeaderCell("Z", FixedDim.numericWidth);
  }

  private void addTablePanelComponents() {
    //Table constraints
    constraints.fill = GridBagConstraints.BOTH;
    if (curTab == JoinDialog.SETUP_TAB) {
      addSetupTablePanelComponents();
    }
    else if (curTab == JoinDialog.ALIGN_TAB) {
      addAlignTablePanelComponents();
    }
    else if (curTab == JoinDialog.JOIN_TAB) {
      addJoinTablePanelComponents();
    }
  }
  /**
   * Creates the panel and table.  Adds the header rows.  Adds SectionTableRows
   * to rows to create each row.
   *
   */
  private void addSetupTablePanelComponents() {
    //Header
    //First row
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    constraints.gridwidth = 2;
    hdrOrder.add(pnlTable, layout, constraints);
    constraints.weighty = 0.0;
    constraints.gridwidth = 1;
    hdrSections.add(pnlTable, layout, constraints);
    constraints.weightx = 0.0;
    btnExpandSections.add(pnlTable, layout, constraints);
    constraints.weightx = 1.0;
    constraints.gridwidth = 4;
    hdrSampleSlices.add(pnlTable, layout, constraints);
    constraints.gridwidth = 2;
    hdrFinal.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hdrRotationAngles.add(pnlTable, layout, constraints);
    //second row
    constraints.weightx = 0.0;
    constraints.gridwidth = 2;
    hdr1Row2.add(pnlTable, layout, constraints);
    hdr2Row2.add(pnlTable, layout, constraints);
    hdrBottom.add(pnlTable, layout, constraints);
    hdrTop.add(pnlTable, layout, constraints);
    constraints.gridwidth = 2;
    hdr3Row2.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hdr4Row2.add(pnlTable, layout, constraints);
    //Third row
    constraints.gridwidth = 2;
    hdr1Row3.add(pnlTable, layout, constraints);
    hdr2Row3.add(pnlTable, layout, constraints);
    constraints.gridwidth = 1;
    hdrSampleSlicesBottomStart.add(pnlTable, layout, constraints);
    hdrSampleSlicesBottomEnd.add(pnlTable, layout, constraints);
    hdrSampleSlicesTopStart.add(pnlTable, layout, constraints);
    hdrSampleSlicesTopEnd.add(pnlTable, layout, constraints);
    hdrFinalStart.add(pnlTable, layout, constraints);
    hdrFinalEnd.add(pnlTable, layout, constraints);
    hdrRotationAnglesX.add(pnlTable, layout, constraints);
    hdrRotationAnglesY.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hdrRotationAnglesZ.add(pnlTable, layout, constraints);
  }
  
  private void addAlignTablePanelComponents() {
    //Header
    //First row
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    hdrOrder.add(pnlTable, layout, constraints);
    constraints.weighty = 0.0;
    hdrSections.add(pnlTable, layout, constraints);
    constraints.weightx = 0.0;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    btnExpandSections.add(pnlTable, layout, constraints);
  }
  
  private void addJoinTablePanelComponents() {
    //Header
    //First row
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    hdrOrder.add(pnlTable, layout, constraints);
    constraints.weighty = 0.0;
    constraints.gridwidth = 1;
    hdrSections.add(pnlTable, layout, constraints);
    constraints.weightx = 0.0;
    btnExpandSections.add(pnlTable, layout, constraints);
    constraints.weightx = 1.0;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hdrFinal.add(pnlTable, layout, constraints);
    //Second row
    constraints.gridwidth = 1;
    hdr1Row3.add(pnlTable, layout, constraints);
    constraints.gridwidth = 2;
    hdr2Row3.add(pnlTable, layout, constraints);
    constraints.gridwidth = 1;
    hdrFinalStart.add(pnlTable, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    hdrFinalEnd.add(pnlTable, layout, constraints);
  }



  private void createButtonsPanel() {
    pnlButtons = new SpacedPanel(FixedDim.x5_y0);
    pnlButtons.setLayout(new BoxLayout(pnlButtons.getContainer(),
        BoxLayout.X_AXIS));
    //first component
    SpacedPanel pnlFirst = new SpacedPanel(FixedDim.x0_y5);
    pnlFirst
        .setLayout(new BoxLayout(pnlFirst.getContainer(), BoxLayout.Y_AXIS));
    btnMoveSectionUp = new MultiLineButton("Move Section Up");
    UIUtilities.setButtonSize(btnMoveSectionUp, buttonDimension, true);
    btnMoveSectionUp.addActionListener(sectionTableActionListener);
    pnlFirst.add(btnMoveSectionUp);
    btnAddSection = new MultiLineButton("Add Section");
    UIUtilities.setButtonSize(btnAddSection, buttonDimension, true);
    btnAddSection.addActionListener(sectionTableActionListener);
    pnlFirst.add(btnAddSection);
    UIUtilities.setButtonSizeAll(pnlFirst.getContainer(), buttonDimension);
    pnlButtons.add(pnlFirst);
    //second component
    SpacedPanel pnlSecond = new SpacedPanel(FixedDim.x0_y5);
    pnlSecond.setLayout(new BoxLayout(pnlSecond.getContainer(),
        BoxLayout.Y_AXIS));
    btnMoveSectionDown = new MultiLineButton("Move Section Down");
    UIUtilities.setButtonSize(btnMoveSectionDown, buttonDimension, true);
    btnMoveSectionDown.addActionListener(sectionTableActionListener);
    pnlSecond.add(btnMoveSectionDown);
    btnDeleteSection = new MultiLineButton("Delete Section");
    UIUtilities.setButtonSize(btnDeleteSection, buttonDimension, true);
    btnDeleteSection.addActionListener(sectionTableActionListener);
    pnlSecond.add(btnDeleteSection);
    pnlButtons.add(pnlSecond);
    //third component
    createImodPanel();
    pnlButtons.add(pnlImod);
    //fourth component
    btnGetAngles = new MultiLineButton("Get Angles from Slicer");
    UIUtilities.setButtonSize(btnGetAngles, buttonDimension, true);
    btnGetAngles.addActionListener(sectionTableActionListener);
    pnlButtons.add(btnGetAngles);
  }

  private void createImodPanel() {
    pnlImod = new SpacedPanel(FixedDim.x0_y5, true);
    pnlImod.setLayout(new BoxLayout(pnlImod.getContainer(), BoxLayout.Y_AXIS));
    pnlImod.setBorder(BorderFactory.createEtchedBorder());
    //binning panel
    SpacedPanel pnlBinning = new SpacedPanel(FixedDim.x5_y0, true);
    pnlBinning.setLayout(new BoxLayout(pnlBinning.getContainer(),
        BoxLayout.X_AXIS));
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
    spinBinning = new LabeledSpinner("Open binned by ", spinnerModel);
    spinBinning.setTextMaxmimumSize(UIParameters.dimSpinner);
    pnlBinning.add(spinBinning);
    JLabel lblIn = new JLabel("in X and Y");
    pnlBinning.add(lblIn);
    pnlImod.add(pnlBinning);
    //3dmod button
    btnOpen3dmod = new MultiLineButton("Open in/Raise 3dmod");
    UIUtilities.setButtonSize(btnOpen3dmod, buttonDimension, true);
    btnOpen3dmod.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnOpen3dmod.setPreferredSize(buttonDimension);
    btnOpen3dmod.setMaximumSize(buttonDimension);
    btnOpen3dmod.addActionListener(sectionTableActionListener);
    pnlImod.add(btnOpen3dmod);
  }
  
  void setCurTab(int curTab) {
    this.curTab = curTab;
  }
  
  void displayCurTab() {
    rootPanel.removeAll();
    addRootPanelComponents();
    pnlTable.removeAll();
    addTablePanelComponents();
    setCurTabInRows();
    displayCurTabInRows();
  }
  
  int getTableSize() {
    return rows.size();
  }

  /**
   * Informs this panel that a row is highlighting.  Only one row may be
   * highlighted at once, so it turns off highlighting on all the other rows.
   * @param rowNumber
   */
  void msgHighlighting(int rowNumber, boolean highlightTurnedOn) {
    int highlightedRowIndex;
    if (highlightTurnedOn) {
      highlightedRowIndex = rowNumber - 1;
      for (int i = 0; i < rows.size(); i++) {
        if (i != rowNumber - 1) {
          ((SectionTableRow) rows.get(i)).setHighlight(false);
        }
      }
    }
    else {
      highlightedRowIndex = -1;
    }
    enableRowButtons(highlightedRowIndex);
  }

  private int getHighlightedRowIndex() {
    if (rows == null) {
      return -1;
    }
    for (int i = 0; i < rows.size(); i++) {
      if (((SectionTableRow) rows.get(i)).isHighlighted()) {
        return i;
      }
    }
    return -1;
  }

  private void enableRowButtons() {
    enableRowButtons(getHighlightedRowIndex());
  }

  private void enableRowButtons(int highlightedRowIndex) {
    int rowsSize = rows.size();
    if (rows != null && rowsSize == 0) {
      btnExpandSections.setEnabled(false);
      btnMoveSectionUp.setEnabled(false);
      btnMoveSectionDown.setEnabled(false);
      btnDeleteSection.setEnabled(false);
      btnOpen3dmod.setEnabled(false);
      btnGetAngles.setEnabled(false);
      return;
    }
    btnExpandSections.setEnabled(true);
    btnMoveSectionUp.setEnabled(highlightedRowIndex > 0);
    btnMoveSectionDown.setEnabled(highlightedRowIndex > -1
        && highlightedRowIndex < rowsSize - 1);
    btnDeleteSection.setEnabled(highlightedRowIndex > -1);
    btnOpen3dmod.setEnabled(highlightedRowIndex > -1);
    btnGetAngles.setEnabled(highlightedRowIndex > -1);
  }

  void enableTableButtons(String workingDir) {
    if (workingDir == null) {
      workingDir = joinDialog.getWorkingDirName();
    }
    btnAddSection.setEnabled(workingDir != null && workingDir.matches("\\S+"));
  }

  /**
   * Implements the Expandable interface.  Matches the expand button parameter
   * and performs the expand/contract operation.  Expands the section in each
   * row.
   * @param expandButton
   */
  public void expand(ExpandButton expandButton) {
    if (expandButton.equals(btnExpandSections)) {
      boolean expand = btnExpandSections.isExpanded();
      for (int i = 0; i < rows.size(); i++) {
        ((SectionTableRow) rows.get(i)).expandSection(expand);
      }
    }
    else {
      throw new IllegalStateException("Unknown expand button," + expandButton);
    }
  }

  public GridBagLayout getTableLayout() {
    return layout;
  }

  public void setEnabledAddSection(boolean enable) {
    btnAddSection.setEnabled(enable);
  }
  
  public GridBagConstraints getTableConstraints() {
    return constraints;
  }

  /**
   * Swap the highlighted row with the one above it.  Move it in the rows 
   * ArrayList.  Move it in the table by removing and adding the two involved
   * rows and everything below them.  Renumber the row numbers in the table.
   */
  private void moveSectionUp() {
    int rowIndex = getHighlightedRowIndex();
    if (rowIndex == -1) {
      return;
    }
    if (rowIndex == 0) {
      joinManager.getMainPanel().openMessageDialog(
          "Can't move the row up.  Its at the top.", "Wrong Row");
      return;
    }
    removeRowsFromTable(rowIndex - 1);
    Object rowMoveUp = rows.remove(rowIndex);
    Object rowMoveDown = rows.remove(rowIndex - 1);
    rows.add(rowIndex - 1, rowMoveUp);
    rows.add(rowIndex, rowMoveDown);
    addRowsToTable(rowIndex - 1);
    renumberTable(rowIndex - 1);
    enableRowButtons(rowIndex - 1);
    repaint();
  }

  /**
   * Swap the highlighted row with the one below it.  Move it in the rows 
   * ArrayList.  Move it in the table by removing and adding the two involved
   * rows and everything below them.  Renumber the row numbers in the table.
   */
  private void moveSectionDown() {
    int rowIndex = getHighlightedRowIndex();
    if (rowIndex == -1) {
      return;
    }
    if (rowIndex == rows.size() - 1) {
      joinManager.getMainPanel().openMessageDialog(
          "Can't move the row down.  Its at the bottom.", "Wrong Row");
      return;
    }
    removeRowsFromTable(rowIndex);
    Object rowMoveUp = rows.remove(rowIndex + 1);
    Object rowMoveDown = rows.remove(rowIndex);
    rows.add(rowIndex, rowMoveUp);
    rows.add(rowIndex + 1, rowMoveDown);
    addRowsToTable(rowIndex);
    renumberTable(rowIndex);
    enableRowButtons(rowIndex + 1);
    repaint();
  }

  private void addSection() {

    //  Open up the file chooser in the working directory
    JFileChooser chooser = new JFileChooser(new File(joinManager.getPropertyUserDir()));
    TomogramFileFilter tomogramFilter = new TomogramFileFilter();
    chooser.setFileFilter(tomogramFilter);
    chooser.setPreferredSize(new Dimension(400, 400));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(rootPanel.getContainer());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File tomogram = chooser.getSelectedFile();
      if (isDuplicate(tomogram)) {
        return;

      }
      MRCHeader header = new MRCHeader(tomogram.getAbsolutePath());
      if (!readHeader(header)) {
        return;
      }
      btnAddSection.setEnabled(false);
      if (flipSection(header, tomogram)) {
        joinManager.flip(tomogram, joinDialog.getWorkingDir());
        return;
      }
      addSection(tomogram);
      joinManager.packMainWindow();
    }
  }
  
  private boolean readHeader(MRCHeader header) {
    try {
      header.read();
    }
    catch (InvalidParameterException e) {
      e.printStackTrace();
      String msgInvalidParameterException[] = {
          "The header command returned an error (InvalidParameterException).",
          "This file may not contain a tomogram.",
          "Are you sure you want to open this file?" };
      if (!joinManager.getMainPanel().openYesNoDialog(
          msgInvalidParameterException)) {
        return false;
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      if (header.getNRows() == -1 || header.getNSections() == -1) {
        String msgIOException[] = {
            "The header command returned an error (IOException).",
            "Unable to tell if the tomogram is flipped.", flipWarning[0],
            flipWarning[1], "Are you sure you want to open this file?" };
        if (!joinManager.getMainPanel().openYesNoDialog(msgIOException)) {
          return false;
        }
      }
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
      if (header.getNRows() == -1 || header.getNSections() == -1) {
        String msgNumberFormatException[] = {
            "The header command returned an error (NumberFormatException).",
            "Unable to tell if the tomogram is flipped.", flipWarning[0],
            flipWarning[1], "Are you sure you want to open this file?" };
        if (!joinManager.getMainPanel().openYesNoDialog(
            msgNumberFormatException)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean flipSection(MRCHeader header, File tomogram) {
    if (header.getNRows() < header.getNSections()) {
      String msgFlipped[] = {
          "It looks like you didn't flip the tomogram in Post Processing",
          "bacause the tomogram is thicker in Z then it is long in Y.",
          flipWarning[0], flipWarning[1],
          "Shall I use the clip flipyz command to flip Y and Z?" };
      if (joinManager.getMainPanel().openYesNoDialog(msgFlipped)) {
        return true;
      }
    }
    return false;
  }

  private boolean isDuplicate(File section) {
    for (int i = 0; i < rows.size(); i++) {
      if (((SectionTableRow) rows.get(i)).equalsSection(section)) {
        String msgDuplicate = "The file, " + section.getAbsolutePath()
            + ", is already in the table.";
        joinManager.getMainPanel().openMessageDialog(msgDuplicate,
            "Add Section Failed");
        return true;
      }
    }
    return false;
  }

  void addSection(File tomogram) {
    btnAddSection.setEnabled(true);
    if (!tomogram.exists()) {
      joinManager.getMainPanel().openMessageDialog(
          tomogram.getAbsolutePath() + " does not exist.", "File Error");
      return;
    }
    if (!tomogram.isFile()) {
      joinManager.getMainPanel().openMessageDialog(
          tomogram.getAbsolutePath() + " is not a file.", "File Error");
      return;
    }
    MRCHeader header = new MRCHeader(tomogram.getAbsolutePath());
    try {
      header.read();
    }
    catch (InvalidParameterException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    SectionTableRow row = new SectionTableRow(this, rows.size() + 1, tomogram,
        btnExpandSections.isExpanded(), header.getNSections(), curTab);
    row.create();
    row.add(pnlTable);
    rows.add(row);
    if (rows.size() > 1) {
      ((SectionTableRow) rows.get(rows.size() - 2)).configureFields();
    }
    int newTableSize = rows.size();
    joinDialog.setNumSections(newTableSize);
    enableRowButtons();
    repaint();
  }
  
  /**
   * Delete the highlighted row.  Remove it in the rows ArrayList.
   * Remove it from the table.  Renumber the row numbers in the table.
   */
  private void deleteSection() {
    int rowIndex = getHighlightedRowIndex();
    if (rowIndex == -1) {
      return;
    }
    SectionTableRow row = (SectionTableRow) rows.get(rowIndex);
    if (!joinManager.getMainPanel().openYesNoDialog(
        "Really remove " + row.getSectionText())) {
      return;
    }
    rows.remove(rowIndex);
    joinManager.imodRemove(ImodManager.TOMOGRAM_KEY, row.getImodIndex());
    row.remove();
    renumberTable(rowIndex);
    if (rowIndex == 0) {
      ((SectionTableRow) rows.get(0)).configureFields();
    }
    else if (rowIndex == rows.size()) {
      ((SectionTableRow) rows.get(rows.size() - 1)).configureFields();
    }
    joinDialog.setNumSections(rows.size());
    enableRowButtons(-1);
    repaint();
  }

  private void imodSection() {
    int rowIndex = getHighlightedRowIndex();
    if (rowIndex == -1) {
      return;
    }
    SectionTableRow row = (SectionTableRow) rows.get(rowIndex);
    row.setImodIndex(joinManager.imodOpenFile(ImodManager.TOMOGRAM_KEY, row
        .getSectionFile(), row.getImodIndex()));
  }

  private void imodGetAngles() {
    int rowIndex = getHighlightedRowIndex();
    if (rowIndex == -1) {
      return;
    }
    SectionTableRow row = (SectionTableRow) rows.get(rowIndex);
    int imodIndex = row.getImodIndex();
    if (imodIndex == -1) {
      joinManager.getMainPanel().openMessageDialog(
          "Open in 3dmod and use the Slicer to change the angles.",
          "Open 3dmod");
      return;
    }
    SlicerAngles slicerAngles = joinManager.imodGetSlicerAngles(
        ImodManager.TOMOGRAM_KEY, imodIndex);
    if (slicerAngles == null || !slicerAngles.isComplete()) {
      return;
    }
    row.setRotationAngles(slicerAngles);
    repaint();
  }

  /**
   * Renumber the table starting from the row in the ArrayList at startIndex.
   * @param startIndex
   */
  private void renumberTable(int startIndex) {
    int rowsSize = rows.size();
    for (int i = startIndex; i < rowsSize; i++) {
      ((SectionTableRow) rows.get(i)).setRowNumber(i + 1, i + 1 == rowsSize);
    }
  }

  /**
   * Remove the rows from the table starting from the row in the ArrayList at
   * startIndex.
   * @param startIndex
   */
  private void removeRowsFromTable(int startIndex) {
    for (int i = startIndex; i < rows.size(); i++) {
      ((SectionTableRow) rows.get(i)).remove();
    }
  }

  /**
   * Add rows in the ArrayList to the table starting from the row in the 
   * ArrayList at startIndex.
   * @param startIndex
   */
  private void addRowsToTable(int startIndex) {
    for (int i = startIndex; i < rows.size(); i++) {
      ((SectionTableRow) rows.get(i)).add(pnlTable);
    }
  }
 
  private void setCurTabInRows() {
    for (int i = 0; i < rows.size(); i++) {
      ((SectionTableRow) rows.get(i)).setCurTab(curTab);
    }
  }
  
  private void displayCurTabInRows() {
    for (int i = 0; i < rows.size(); i++) {
      ((SectionTableRow) rows.get(i)).displayCurTab(pnlTable);
    }
  }
  

  public boolean getMetaData(JoinMetaData joinMetaData) {
    joinMetaData.resetSectionTableData();
    for (int i = 0; i < rows.size(); i++) {
      ConstSectionTableRowData rowData = ((SectionTableRow) rows.get(i)).getData();
      if (rowData == null) {
        return false;
      }
      joinMetaData.setSectionTableData(rowData);
    }
    return true;
  }
  
  public String getInvalidReason() {
    for (int i = 0; i < rows.size(); i++) {
      SectionTableRow row = (SectionTableRow) rows.get(i);
      String invalidReason = row.getInvalidReason();
      if (invalidReason != null) {
        return invalidReason;
      }
    }
    return null;
  }

  /**
   * Add a JComponent to the table.
   * @param cell
   */
  public void addCell(JComponent cell) {
    layout.setConstraints(cell, constraints);
    pnlTable.add(cell);
  }

  public void removeCell(JComponent cell) {
    pnlTable.remove(cell);
  }

  /**
   * Call mainPanel repaint.
   *
   */
  private void repaint() {
    joinManager.getMainPanel().repaint();
  }

  /**
   * Create a multi line toggle button.  Set the border to raised bevel to make
   * it 3D.  Set its preferred width.
   * @param value
   * @param width
   * @return button created
   */
  MultiLineToggleButton createToggleButton(String text, int width) {
    MultiLineToggleButton button = new MultiLineToggleButton(text);
    button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    Dimension size = button.getPreferredSize();
    size.width = width;
    button.setPreferredSize(size);
    return button;
  }

  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
  }

  Container getContainer() {
    return rootPanel.getContainer();
  }
  
  DoubleSpacedPanel getRootPanel() {
    return rootPanel;
  }

  /**
   * Handle actions
   * @param event
   */
  private void action(ActionEvent event) {
    String command = event.getActionCommand();

    if (command.equals(btnMoveSectionUp.getActionCommand())) {
      moveSectionUp();
    }
    else if (command.equals(btnMoveSectionDown.getActionCommand())) {
      moveSectionDown();
    }
    else if (command.equals(btnAddSection.getActionCommand())) {
      addSection();
    }
    else if (command.equals(btnDeleteSection.getActionCommand())) {
      deleteSection();
    }
    else if (command.equals(btnOpen3dmod.getActionCommand())) {
      imodSection();
    }
    else if (command.equals(btnGetAngles.getActionCommand())) {
      imodGetAngles();
    }
  }

  //
  //  Action listener adapters
  //
  class SectionTableActionListener implements ActionListener {

    SectionTablePanel adaptee;

    SectionTableActionListener(SectionTablePanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      action(event);
    }
  }
}