package etomo.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import etomo.type.ConstSectionTableRowData;
import etomo.type.SectionTableRowData;
import etomo.type.SlicerAngles;

/**
* <p>Description: Manages the fields, buttons, state, and data of one row of
* SectionTablePanel.</p>
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
* <p> Revision 1.1.2.5  2004/10/01 20:07:11  sueh
* <p> bug# 520 Converted text fields to FieldCells.  Removed color control
* <p> (done in FieldCell).
* <p>
* <p> Revision 1.1.2.4  2004/09/29 19:45:01  sueh
* <p> bug# 520 View part of the section table row.  Contains section table row
* <p> screen fields.  Added SectionTableRowData member variable so hold,
* <p> store, and compare data from the screen.  Added displayData() to display
* <p> data on the screen.  Added retrieveData() to retrieve data from the
* <p> screen.  Added an equals function.  Disabled the sectionFile field.
* <p>
* <p> Revision 1.1.2.3  2004/09/22 22:17:30  sueh
* <p> bug# 520 Added set rotation angle functions.  When highlighting, tell the
* <p> panel when the highlight is being turned off as well as when its being
* <p> turned on (for button enable/disable).
* <p>
* <p> Revision 1.1.2.2  2004/09/21 18:12:04  sueh
* <p> bug# 520 Added remove(), to remove the row from the table.  Added
* <p> imodIndex - the vector index of the 3dmod in ImodManager.  Added
* <p> create(), to create the row in the table for the first time.  Added add(), to
* <p> added the rows back into the table.
* <p>
* <p> Revision 1.1.2.1  2004/09/17 21:48:41  sueh
* <p> bug# 520 Handles row display, state, and data.  Can highlight all of its
* <p> fields.  Can expand the section field
* <p> </p>
*/
public class SectionTableRow {
  public static final String rcsid = "$Id$";
  
  //data
  SectionTableRowData data = null;
  
  //state
  private int imodIndex = -1;
  private boolean sectionExpanded = false;
  
  //ui
  private SectionTablePanel table = null;
  private HeaderCell rowNumberHeader = null;
  private MultiLineToggleButton highlighterButton = null;
  private FieldCell section = null;
  private FieldCell sampleBottomStart = null;
  private FieldCell sampleBottomEnd = null;
  private FieldCell sampleTopStart = null;
  private FieldCell sampleTopEnd = null;
  private FieldCell finalStart = null;
  private FieldCell finalEnd = null;
  private FieldCell rotationAngleX = null;
  private FieldCell rotationAngleY = null;
  private FieldCell rotationAngleZ = null;
  private SectionTableRowActionListener actionListener = new SectionTableRowActionListener(
      this);
  
  /**
   * Create colors, fields, and buttons.  Add the row to the table
   * @param table
   * @param rowNumber
   */
  public SectionTableRow(SectionTablePanel table, int rowNumber, File tomogram,
      boolean sectionExpanded, int zMax) {
    data = new SectionTableRowData();
    data.setRowNumber(rowNumber);
    this.table = table;
    data.setSection(tomogram);
    data.setZMax(zMax);
    this.sectionExpanded = sectionExpanded;
  }
  
  void create() {
    GridBagLayout layout = table.getTableLayout();
    GridBagConstraints constraints = table.getTableConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    rowNumberHeader = new HeaderCell(table, data.getRowNumberString(),
        FixedDim.rowNumberWidth);
    rowNumberHeader.add();
    constraints.weightx = 0.0;
    highlighterButton = table.addToggleButton("=>", FixedDim.highlighterWidth);
    highlighterButton.addActionListener(actionListener);
    constraints.gridwidth = 2;
    section = new FieldCell(table);
    section.add();
    section.setEnabled(false);
    setSectionText();
    constraints.gridwidth = 1;
    sampleBottomStart = new FieldCell(table);
    sampleBottomStart.add();
    sampleBottomEnd = new FieldCell(table);
    sampleBottomEnd.add();
    sampleTopStart = new FieldCell(table);
    sampleTopStart.add();
    sampleTopEnd = new FieldCell(table);
    sampleTopEnd.add();
    finalStart = new FieldCell(table);
    finalStart.add();
    finalStart.setInUse(false);
    finalEnd = new FieldCell(table);
    finalEnd.add();
    finalEnd.setInUse(false);
    rotationAngleX = new FieldCell(table);
    rotationAngleX.add();
    rotationAngleY = new FieldCell(table);
    rotationAngleY.add();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    rotationAngleZ = new FieldCell(table);
    rotationAngleZ.add();
    displayData();
  }
  
  void remove() {
    rowNumberHeader.remove();
    table.removeCell(highlighterButton);
    section.remove();
    sampleBottomStart.remove();
    sampleBottomEnd.remove();
    sampleBottomEnd.remove();
    sampleTopStart.remove();
    sampleTopEnd.remove();
    finalStart.remove();
    finalEnd.remove();
    rotationAngleX.remove();
    rotationAngleY.remove();
    rotationAngleZ.remove();

  }
  
  void add() {
    GridBagLayout layout = table.getTableLayout();
    GridBagConstraints constraints = table.getTableConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    rowNumberHeader.add();
    constraints.weightx = 0.0;
    table.addToggleButton(highlighterButton, "=>", FixedDim.highlighterWidth);
    highlighterButton.addActionListener(actionListener);
    constraints.gridwidth = 2;
    section.add();
    constraints.gridwidth = 1;
    sampleBottomStart.add();
    sampleBottomEnd.add();
    sampleTopStart.add();
    sampleTopEnd.add();
    finalStart.add();
    finalEnd.add();
    rotationAngleX.add();
    rotationAngleY.add();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    rotationAngleZ.add();
  }
  
  private void displayData() {
    rowNumberHeader.setText(data.getRowNumberString());
    setSectionText();
    sampleBottomStart.setText(data.getSampleBottomStartString());
    sampleBottomEnd.setText(data.getSampleBottomEndString());
    sampleTopStart.setText(data.getSampleTopStartString());
    sampleTopEnd.setText(data.getSampleTopEndString());
    finalStart.setText(data.getFinalStartString());
    finalEnd.setText(data.getFinalEndString());
    rotationAngleX.setText(data.getRotationAngleXString());
    rotationAngleY.setText(data.getRotationAngleYString());
    rotationAngleZ.setText(data.getRotationAngleZString());
  }
  
  private boolean retrieveData() {
    if (!data.setSampleBottomStart(sampleBottomStart.getText())) {
      return false;
    }
    sampleBottomEnd.setText(data.getSampleBottomEndString());
    sampleTopStart.setText(data.getSampleTopStartString());
    sampleTopEnd.setText(data.getSampleTopEndString());
    finalStart.setText(data.getFinalStartString());
    finalEnd.setText(data.getFinalEndString());
    rotationAngleX.setText(data.getRotationAngleXString());
    rotationAngleY.setText(data.getRotationAngleYString());
    rotationAngleZ.setText(data.getRotationAngleZString());
    return true;
  }
  
  /**
   * Toggle section between absolute path when expand is true, and name when
   * expand is false.
   * @param expand
   */
  void expandSection(boolean expand) {
    sectionExpanded = expand;
    if (data.getSection() == null) {
      return;
    }
    setSectionText();
  }
  
  private void setSectionText() {
    if (sectionExpanded) {
      section.setText(data.getSectionAbsolutePath());
    }
    else {
      section.setText(data.getSectionName());
    }
  }
  
  void setRowNumber(int rowNumber) {
    data.setRowNumber(rowNumber);
    rowNumberHeader.setText("<html><b>" + Integer.toString(rowNumber) + "</b>");
  }
  
  void setImodIndex(int imodIndex) {
    this.imodIndex = imodIndex;
  }
  
  void setRotationAngles(SlicerAngles slicerAngles) {
    rotationAngleX.setText(slicerAngles.getXText());
    rotationAngleY.setText(slicerAngles.getYText());
    rotationAngleZ.setText(slicerAngles.getZText());
  }

  /**
   * Toggle the highlighter button based on the highlighted parameter.
   * Change the foreground and background for all the fields in the row.  Do
   * nothing of the highlighter button matches the highlight parameter.
   * @param highlighted
   */
  void setHighlight(boolean highlight) {
    if (highlight == highlighterButton.isSelected()) {
      return;
    }
    highlighterButton.setSelected(highlight);
    highlight();
  }
  
  /**
   * Change the foreground and background for all the fields in the row based
   * on whether the highlighter button is selected.
   *
   */
  private void highlight() {
    boolean highlight = highlighterButton.isSelected();
    section.setHighlighted(highlight);
    sampleBottomStart.setHighlighted(highlight);
    sampleBottomEnd.setHighlighted(highlight);
    sampleTopStart.setHighlighted(highlight);
    sampleTopEnd.setHighlighted(highlight);
    finalStart.setHighlighted(highlight);
    finalEnd.setHighlighted(highlight);
    rotationAngleX.setHighlighted(highlight);
    rotationAngleY.setHighlighted(highlight);
    rotationAngleZ.setHighlighted(highlight);
  }
  
  private void highlighterButtonAction() {
    table.msgHighlighting(data.getRowNumber(), highlighterButton.isSelected());
    highlight();
  }
  
  boolean isHighlighted() {
    return highlighterButton.isSelected();
  }
  
  File getSectionFile() {
    return data.getSection();
  }
  
  String getSectionText() {
    return section.getText();
  }
  
  int getImodIndex() {
    return imodIndex;
  }
  
  ConstSectionTableRowData getData() {
    retrieveData();
    return data;
  }
    
  public boolean equalsSection(File section) {
    if (data.getSectionAbsolutePath().equals(section.getAbsolutePath())) {
      return true;
    }
    return false;
  }
  
  public boolean equals(Object object) {
    if (!(object instanceof SectionTableRow))
      return false;

    SectionTableRow that = (SectionTableRow) object;
    retrieveData();
    
    if (!data.equals(that.data)) {
      return false;
    }
    return true;
  }

  
  /**
   * Handle button actions
   * @param event
   */
  private void buttonAction(ActionEvent event) {
    String command = event.getActionCommand();

    if (command.equals(highlighterButton.getActionCommand())) {
      highlighterButtonAction();
    }
  }

  
  /**
   *  Action listener for SectionTableRow
   */
  class SectionTableRowActionListener implements ActionListener {

    SectionTableRow adaptee;

    SectionTableRowActionListener(SectionTableRow adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.buttonAction(event);
    }
  }

}
