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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import etomo.JoinManager;
import etomo.process.ImodManager;
import etomo.storage.TomogramFileFilter;
import etomo.type.JoinMetaData;
import etomo.type.SlicerAngles;

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
  public static  final String  rcsid =  "$Id$";
  
  private static final Dimension buttonDimension = UIParameters
  .getButtonDimension();
  
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
  
  private ArrayList rows = new ArrayList();
  
  private GridBagLayout layout = new GridBagLayout();
  private GridBagConstraints constraints = new GridBagConstraints();
  private SectionTableActionListener sectionTableActionListener = new SectionTableActionListener(this);
  
  private final JoinManager joinManager;
  private final JoinDialog joinDialog;
  
  /**
   * Creates the panel and table.
   *
   */
  SectionTablePanel(JoinDialog joinDialog, JoinManager joinManager) {
    this.joinDialog = joinDialog;
    this.joinManager = joinManager;
    createRootPanel();
    enableTableButtons(false);
    enableRowButtons(false);
  }
  
  private void createRootPanel() {
    //create rootPanel in X axis to make room at the border
    rootPanel = new DoubleSpacedPanel(false, FixedDim.x5_y0, FixedDim.x0_y5,
        BorderFactory.createEtchedBorder());
    createTablePanel();
    rootPanel.add(pnlTable);
    createButtonsPanel();
    rootPanel.add(pnlButtons);
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
    //Table constraints
    constraints.fill = GridBagConstraints.BOTH;
    //Header
    //First row
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    constraints.gridwidth = 2;
    addHeader("Order");
    constraints.weighty = 0.0;
    constraints.gridwidth = 1;
    addHeader("Sections", FixedDim.sectionsWidth);
    constraints.weightx = 0.0;
    btnExpandSections = addExpandButton();
    constraints.weightx = 1.0;
    constraints.gridwidth = 4;
    addHeader("Sample Slices");
    constraints.gridwidth = 2;
    addHeader("Final");
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    addHeader("Rotation Angles");
    //second row
    constraints.weightx = 0.0;
    constraints.gridwidth = 2;
    addHeader();
    addHeader();
    addHeader("Bottom");
    addHeader("Top");
    constraints.gridwidth = 2;
    addHeader();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    addHeader();
    //Third row
    constraints.gridwidth = 2;
    addHeader();
    addHeader();
    constraints.gridwidth = 1;
    addHeader("Start", FixedDim.numericWidth);
    addHeader("End", FixedDim.numericWidth);
    addHeader("Start", FixedDim.numericWidth);
    addHeader("End", FixedDim.numericWidth);
    addHeader("Start", FixedDim.numericWidth);
    addHeader("End", FixedDim.numericWidth);
    addHeader("X", FixedDim.numericWidth);
    addHeader("Y", FixedDim.numericWidth);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    addHeader("Z", FixedDim.numericWidth);
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
    pnlBinning.setLayout(new BoxLayout(pnlBinning.getContainer(), BoxLayout.X_AXIS));
    SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
    spinBinning = new LabeledSpinner("Open binned by ", spinnerModel);
    spinBinning.setTextMaxmimumSize(UIParameters.dimSpinner);
    pnlBinning.add(spinBinning.getContainer());
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
  
  /**
   * Informs this panel that a row is highlighting.  Only one row may be
   * highlighted at once, so it turns off highlighting on all the other rows.
   * @param rowNumber
   */
  void highlighting(int rowNumber, boolean highlightTurnedOn) {
    if (btnMoveSectionUp.isEnabled() != highlightTurnedOn) {
      enableRowButtons(highlightTurnedOn);
    }
    if (!highlightTurnedOn) {
      return;
    }
    for (int i = 0; i < rows.size(); i++) {
      if (i != rowNumber - 1) {
        ((SectionTableRow)rows.get(i)).setHighlight(false);
      }
    }
  }
  
  private void enableRowButtons(boolean enable) {
    btnMoveSectionUp.setEnabled(enable);
    btnMoveSectionDown.setEnabled(enable);
    btnDeleteSection.setEnabled(enable);
    btnOpen3dmod.setEnabled(enable);
    btnGetAngles.setEnabled(enable);
  }
  
  private void enableTableButtons(boolean enable) {
    btnExpandSections.setEnabled(enable);
  }
  
  /**
   * Implements the Exandable interface.  Matches the expand button parameter
   * and performs the expand/contract operation.  Expands the section in each
   * row.
   * @param expandButton
   */
  public void expand(ExpandButton expandButton) {
    if (!expandButton.equals(btnExpandSections)) {
      throw new IllegalStateException("Unknown expand button," + expandButton);
    }
    boolean expand = btnExpandSections.isExpanded();
    for (int i = 0; i < rows.size(); i++) {
      ((SectionTableRow)rows.get(i)).expandSection(expand);
    }
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
    //updateRootPanel();
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
    //updateRootPanel();
    repaint();
  }
  
  private void addSection() {
    //  Open up the file chooser in the working directory
    JFileChooser chooser = new JFileChooser(new File(System
      .getProperty("user.dir")));
    TomogramFileFilter tomogramFilter = new TomogramFileFilter();
    chooser.setFileFilter(tomogramFilter);
    chooser.setPreferredSize(new Dimension(400, 400));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(rootPanel.getContainer());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File tomogram = chooser.getSelectedFile();
      addSection(tomogram);
      joinManager.packMainWindow();
    }
  }
  
  private void addSection(File tomogram) {
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
    rows.add(new SectionTableRow(this, rows.size() + 1, tomogram, btnExpandSections
        .isExpanded()));
    int newTableSize = rows.size();
    if (newTableSize == 1) {
      enableTableButtons(true);
    }
    joinDialog.setNumSections(newTableSize);
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
    if (!joinManager.getMainPanel().openYesNoDialog("Really remove " + row.getSectionText())) {
      return;
    }
    rows.remove(rowIndex);
    if (rows.size() == 0) {
      enableTableButtons(false);
      enableRowButtons(false);
    }
    joinManager
        .imodRemove(ImodManager.TOMOGRAM_KEY, row.getImodIndex());
    row.remove();
    renumberTable(rowIndex);
    joinDialog.setNumSections(rows.size());
    repaint();
  }
  
  private void imodSection() {
    int rowIndex = getHighlightedRowIndex();
    if (rowIndex == -1) {
      return;
    }
    SectionTableRow row = (SectionTableRow) rows.get(rowIndex);
    row.setImodIndex(joinManager.imodOpen(ImodManager.TOMOGRAM_KEY, row
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
   * Get the rows ArrayList index of the highlighted row.
   * @return
   */
  private int getHighlightedRowIndex() {
    for (int i = 0; i < rows.size(); i++) {
      if (((SectionTableRow)rows.get(i)).isHighlighted()) {
        return i;
      }
    }
    joinManager.getMainPanel().openMessageDialog(
        "Please highlight a row.",
        "Highlight a Row");
    return -1;
  }
  
  /**
   * Renumber the table starting from the row in the ArrayList at startIndex.
   * @param startIndex
   */
  private void renumberTable(int startIndex) {
    for (int i = startIndex; i < rows.size(); i++) {
      ((SectionTableRow)rows.get(i)).setRowNumber(i + 1);
    }
  }
  
  /**
   * Remove the rows from the table starting from the row in the ArrayList at
   * startIndex.
   * @param startIndex
   */
  private void removeRowsFromTable(int startIndex) {
    for (int i = startIndex; i < rows.size(); i++) {
      ((SectionTableRow)rows.get(i)).remove();
    }
  }
  
  /**
   * Add rows in the ArrayList to the table starting from the row in the 
   * ArrayList at startIndex.
   * @param startIndex
   */
  private void addRowsToTable(int startIndex) {
    for (int i = startIndex; i < rows.size(); i++) {
      ((SectionTableRow)rows.get(i)).add();
    }
  }
  
  public void retrieveData(JoinMetaData joinMetaData) {
    joinMetaData.resetSectionTableData();
    for (int i = 0; i < rows.size(); i++) {
      joinMetaData.setSectionTableData(((SectionTableRow) rows.get(i)).getData());
    }
  }
  /**
   * 
   * @return constraints
   */
  GridBagConstraints getConstraints() {
    return constraints;
  }
  
  /**
   * Add a header cell to the table and set the text.
   * @param text
   */
  private void addHeader(String text) {
    JButton cell = createHeader(text);
    addToTable(cell);
  }
  
  /**
   * Create a header cell:  Create a JButton and wrap its text
   * in an HTML bold tag to bold it and keep the text from changing color when
   * the button is disabled.  Set the border to etched to keep it flat.  Disable
   * it.
   * @param value
   * @return
   */
  private JButton createHeader(String text) {
    String htmlText = "<html><b>" + text + "</b>";
    JButton cell = new JButton(htmlText);
    cell.setBorder(BorderFactory.createEtchedBorder());
    cell.setEnabled(false);
    return cell;
  }
  
  /**
   * Add a blank header cell to the table.
   *
   */
  private void addHeader() {
    addHeader("");
  }
  
  /**
   * Add an existing header cell with a preferred width and set the text.
   * @param value
   * @param width
   * @return the added header
   */
  JButton addHeader(JButton cell, String text, int width) {
    Dimension size = cell.getPreferredSize();
    size.width = width;
    cell.setPreferredSize(size);
    addToTable(cell);
    return cell;
  }
  
  /**
   * Add a header cell with a preferred width and set the text.
   * @param value
   * @param width
   * @return the created header
   */
  JButton addHeader(String text, int width) {
    JButton cell = createHeader(text);
    return addHeader(cell, text, width);
  }
  
  /**
   * Add a JComponent to the table.
   * @param cell
   */
  private void addToTable(JComponent cell) {
    layout.setConstraints(cell, constraints);
    pnlTable.add(cell);
  }
  
  /**
   * Remove a JComponent from the table.
   * @param cell
   */
  void removeFromTable(JComponent cell) {
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
   * Add an existing text field to the table.
   * @return The added field
   */
  JTextField addField(JTextField cell) {
    addToTable(cell);
    return cell;
  }
  
  /**
   * Create a text field and add it to the table.
   * @return The created field
   */
  JTextField addField() {
    JTextField cell = new JTextField();
    return addField(cell);
  }
  
  /**
   * Create an expand button and add it to the table.
   * @return the expand button
   */
  private ExpandButton addExpandButton() {
    ExpandButton button = new ExpandButton(this);
    addToTable(button);
    return button;
  }
  
  /**
   * Add a multi line toggle button to the table.  Set the border to raised bevel to make
   * it 3D.  Set its preferred width.
   * @param value
   * @param width
   * @return button created
   */
  MultiLineToggleButton addToggleButton(MultiLineToggleButton button, String text, int width) {
    button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    Dimension size = button.getPreferredSize();
    size.width = width;
    button.setPreferredSize(size);
    addToTable(button);
    return button;
  }
  
  /**
   * Create a multi line toggle button.  Set the border to raised bevel to make
   * it 3D.  Set its preferred width.  Add it to the table.
   * @param value
   * @param width
   * @return button created
   */
  MultiLineToggleButton addToggleButton(String text, int width) {
    MultiLineToggleButton button = new MultiLineToggleButton(text);
    return addToggleButton(button, text, width);
  }
  
  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
  }

  Container getContainer() {
    return rootPanel.getContainer();
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
