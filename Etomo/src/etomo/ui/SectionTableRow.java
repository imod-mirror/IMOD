package etomo.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPanel;

import etomo.type.ConstEtomoInteger;
import etomo.type.ConstSectionTableRowData;
import etomo.type.EtomoInteger;
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
* <p> Revision 1.1.2.10  2004/10/22 03:28:33  sueh
* <p> bug# 520 Added Chunk, Reference section, and Current section to Align
* <p> tab.
* <p>
* <p> Revision 1.1.2.9  2004/10/15 00:52:26  sueh
* <p> bug# 520 Added toString().
* <p>
* <p> Revision 1.1.2.8  2004/10/13 23:15:36  sueh
* <p> bug# 520 Allowed the ui components of the row to be removed and re-
* <p> added.  This way the table can look different on different tabs.  Set the
* <p> state of fields based on the tab.
* <p>
* <p> Revision 1.1.2.7  2004/10/08 16:40:22  sueh
* <p> bug# Using setRowNumber() to change the status of sample slice
* <p> numbers.  Fixed retrieveData().  Changed getData() to return false when
* <p> retrieveData() fails.  Added getInvalidReason().
* <p>
* <p> Revision 1.1.2.6  2004/10/06 02:31:17  sueh
* <p> bug# 520 Added Z max
* <p>
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
  private int curTab = JoinDialog.SETUP_TAB;
  
  //ui
  SectionTablePanel table = null;
  private HeaderCell rowNumber = null;
  private MultiLineToggleButton highlighterButton = null;
  private FieldCell section = null;
  private FieldCell sampleBottomStart = null;
  private FieldCell sampleBottomEnd = null;
  private FieldCell sampleTopStart = null;
  private FieldCell sampleTopEnd = null;
  private HeaderCell chunk = null;
  private FieldCell referenceSectionStart = null;
  private FieldCell referenceSectionEnd = null;
  private FieldCell currentSectionStart = null;
  private FieldCell currentSectionEnd = null;
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
      boolean sectionExpanded, int zMax, int curTab) {
    this.table = table;
    data = new SectionTableRowData(rowNumber);
    data.setSection(tomogram);
    data.setZMax(zMax);
    this.sectionExpanded = sectionExpanded;
    this.curTab = curTab;
  }
  
  public SectionTableRow(SectionTablePanel table, SectionTableRowData data,
      boolean sectionExpanded, int curTab) {
    this.table = table;
    this.data = data;
    this.sectionExpanded = sectionExpanded;
    this.curTab = curTab;
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }

  protected String paramString() {
    return ",\ntable=" + table + ",\rowNumber=" + rowNumber
        + ",\nhighlighterButton=" + highlighterButton + ",\nsection=" + section
        + ",\nsampleBottomStart=" + sampleBottomStart + ",\nsampleBottomEnd="
        + sampleBottomEnd + ",\nsampleTopStart=" + sampleTopStart
        + ",\nsampleTopEnd=" + sampleTopEnd + ",\nfinalStart=" + finalStart
        + ",\nfinalEnd=" + finalEnd + ",\nrotationAngleX=" + rotationAngleX
        + ",\nrotationAngleY=" + rotationAngleY + ",\nrotationAngleZ="
        + rotationAngleZ + ",\nimodIndex=" + imodIndex + ",\nsectionExpanded="
        + sectionExpanded + ",\ncurTab=" + curTab + ",\nchunk=" + chunk
        + ",\nreferenceSectionStart=" + referenceSectionStart
        + ",\nreferenceSectionEnd=" + referenceSectionEnd
        + ",\ncurrentSectionStart=" + currentSectionStart
        + ",\ncurrentSectionEnd=" + currentSectionEnd + ",\ndata=" + data;
  } 
  
  void create() {
    rowNumber = new HeaderCell(data.getRowNumber().getString(),
        FixedDim.rowNumberWidth);
    highlighterButton = table.createToggleButton("=>", FixedDim.highlighterWidth);
    highlighterButton.addActionListener(actionListener);
    section = new FieldCell();
    section.setEnabled(false);
    setSectionText();
    sampleBottomStart = new FieldCell();
    sampleBottomEnd = new FieldCell();
    sampleTopStart = new FieldCell();
    sampleTopEnd = new FieldCell();
    chunk = new HeaderCell();
    referenceSectionStart = new FieldCell();
    referenceSectionStart.setEnabled(false);
    referenceSectionEnd = new FieldCell();
    referenceSectionEnd.setEnabled(false);
    currentSectionStart = new FieldCell();
    currentSectionStart.setEnabled(false);
    currentSectionEnd = new FieldCell();
    currentSectionEnd.setEnabled(false);
    finalStart = new FieldCell();
    finalEnd = new FieldCell();
    rotationAngleX = new FieldCell();
    rotationAngleY = new FieldCell();
    rotationAngleZ = new FieldCell();
    configureFields();
    displayData();
  }
  
  void configureFields() {
    int rowNumber = data.getRowNumber().get();
    boolean bottomInUse = rowNumber > 1;
    boolean topInUse = rowNumber < table.getTableSize();
    boolean finalInuse = curTab == JoinDialog.JOIN_TAB;
    boolean enableSamples = curTab == JoinDialog.SETUP_TAB;
    
    sampleBottomStart.setInUse(bottomInUse);
    sampleBottomEnd.setInUse(bottomInUse);
    
    sampleTopStart.setInUse(topInUse);
    sampleTopEnd.setInUse(topInUse);
    
    finalStart.setInUse(finalInuse);
    finalEnd.setInUse(finalInuse);

    sampleBottomStart.setEnabled(enableSamples);
    sampleBottomEnd.setEnabled(enableSamples);
    sampleTopStart.setEnabled(enableSamples);
    sampleTopEnd.setEnabled(enableSamples);
  }
  
  void remove() {
    if (curTab == JoinDialog.SETUP_TAB) {
      removeSetup();
    }
    else if (curTab == JoinDialog.ALIGN_TAB) {
      removeAlign();
    }
    else if (curTab == JoinDialog.JOIN_TAB) {
      removeJoin();
    }
  }
  
  private void removeSetup() {
    rowNumber.remove();
    table.removeCell(highlighterButton);
    section.remove();
    sampleBottomStart.remove();
    sampleBottomEnd.remove();
    sampleTopStart.remove();
    sampleTopEnd.remove();
    finalStart.remove();
    finalEnd.remove();
    rotationAngleX.remove();
    rotationAngleY.remove();
    rotationAngleZ.remove();
  }
  
  private void removeAlign() {
    rowNumber.remove();
    section.remove();
    sampleBottomStart.remove();
    sampleBottomEnd.remove();
    sampleTopStart.remove();
    sampleTopEnd.remove();
    chunk.remove();
    referenceSectionStart.remove();
    referenceSectionEnd.remove();
    currentSectionStart.remove();
    currentSectionEnd.remove();
  }
  
  private void removeJoin() {
    rowNumber.remove();
    table.removeCell(highlighterButton);
    section.remove();
    finalStart.remove();
    finalEnd.remove();
  }
  
  void setCurTab(int curTab) {
    this.curTab = curTab;
  }
  
  int displayCurTab(JPanel panel, int prevSlice, int prevSampleTop) {
    remove();
    add(panel);
    configureFields();
    //Set align display only fields
    if (curTab == JoinDialog.ALIGN_TAB) {
      ConstEtomoInteger rowNumber = data.getRowNumber();
      if (rowNumber.equals(1)) {
        chunk.setText("");
        referenceSectionStart.setText("");
        referenceSectionEnd.setText("");
        currentSectionStart.setText("");
        currentSectionEnd.setText("");
      }
      else {
        chunk.setText(rowNumber.getString());
        if (prevSampleTop > 0) {
          referenceSectionStart.setText(Integer.toString(prevSlice + 1));
          prevSlice += prevSampleTop;
          referenceSectionEnd.setText(Integer.toString(prevSlice));
        }
        else {
          referenceSectionStart.setText("");
          referenceSectionEnd.setText("");
        }
        int sampleBottom = getSampleBottom();
        if (sampleBottom > 0) {
          currentSectionStart.setText(Integer.toString(prevSlice + 1));
          prevSlice += sampleBottom;
          currentSectionEnd.setText(Integer.toString(prevSlice));
        }
        else {
          currentSectionStart.setText("");
          currentSectionEnd.setText("");
        }
      }
    }
    return prevSlice;
  }
  
  void add(JPanel panel) {
    if (curTab == JoinDialog.SETUP_TAB) {
      addSetup(panel);
    }
    else if (curTab == JoinDialog.ALIGN_TAB) {
      addAlign(panel);
    }
    else if (curTab == JoinDialog.JOIN_TAB) {
      addJoin(panel);
    }
  }
  
  private void addSetup(JPanel panel) {
    GridBagLayout layout = table.getTableLayout();
    GridBagConstraints constraints = table.getTableConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    rowNumber.add(panel, layout, constraints);
    constraints.weightx = 0.0;
    table.addCell(highlighterButton);
    constraints.gridwidth = 2;
    section.add(panel, layout, constraints);
    constraints.gridwidth = 1;
    sampleBottomStart.add(panel, layout, constraints);
    sampleBottomEnd.add(panel, layout, constraints);
    sampleTopStart.add(panel, layout, constraints);
    sampleTopEnd.add(panel, layout, constraints);
    finalStart.add(panel, layout, constraints);
    finalEnd.add(panel, layout, constraints);
    rotationAngleX.add(panel, layout, constraints);
    rotationAngleY.add(panel, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    rotationAngleZ.add(panel, layout, constraints);
  }
  
  private void addAlign(JPanel panel) {
    GridBagLayout layout = table.getTableLayout();
    GridBagConstraints constraints = table.getTableConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    rowNumber.add(panel, layout, constraints);
    constraints.weightx = 0.0;
    constraints.gridwidth = 2;
    section.add(panel, layout, constraints);
    constraints.gridwidth = 1;
    sampleBottomStart.add(panel, layout, constraints);
    sampleBottomEnd.add(panel, layout, constraints);
    sampleTopStart.add(panel, layout, constraints);
    sampleTopEnd.add(panel, layout, constraints);
    chunk.add(panel, layout, constraints);
    referenceSectionStart.add(panel, layout, constraints);
    referenceSectionEnd.add(panel, layout, constraints);
    currentSectionStart.add(panel, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    currentSectionEnd.add(panel, layout, constraints);
  }
  
  private void addJoin(JPanel panel) {
    GridBagLayout layout = table.getTableLayout();
    GridBagConstraints constraints = table.getTableConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    rowNumber.add(panel, layout, constraints);
    constraints.weightx = 0.0;
    table.addCell(highlighterButton);
    constraints.gridwidth = 2;
    section.add(panel, layout, constraints);
    constraints.gridwidth = 1;
    finalStart.add(panel, layout, constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    finalEnd.add(panel, layout, constraints);
  }
  
  private void displayData() {
    rowNumber.setText(data.getRowNumber().getString());
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
    if (!data.setSampleBottomStart(sampleBottomStart.getText())
        || !data.setSampleBottomEnd(sampleBottomEnd.getText())
        || !data.setSampleTopStart(sampleTopStart.getText())
        || !data.setSampleTopEnd(sampleTopEnd.getText())
        || !data.setFinalStart(finalStart.getText())
        || !data.setFinalEnd(finalEnd.getText())
        || !data.setRotationAngleX(rotationAngleX.getText())
        || !data.setRotationAngleY(rotationAngleY.getText())
        || !data.setRotationAngleZ(rotationAngleZ.getText())) {
      return false;
    }
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
  
  void setRowNumber(int rowNumber, boolean maxRow) {
    data.setRowNumber(rowNumber);
    this.rowNumber.setText("<html><b>" + Integer.toString(rowNumber) + "</b>");
    configureFields();
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
    table.msgHighlighting(data.getRowIndex(), highlighterButton.isSelected());
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
  
  int getSampleBottom() {
    EtomoInteger startInteger = new EtomoInteger();
    EtomoInteger endInteger = new EtomoInteger();
    startInteger.set(sampleBottomStart.getText());
    endInteger.set(sampleBottomEnd.getText());
    if (startInteger.isSet() && endInteger.isSet()) {
      int start = startInteger.get();
      int end = endInteger.get();
      return end - start + 1;
    }
    return 0;
  }
  
  int getSampleTop() {
    EtomoInteger startInteger = new EtomoInteger();
    EtomoInteger endInteger = new EtomoInteger();
    startInteger.set(sampleTopStart.getText());
    endInteger.set(sampleTopEnd.getText());
    if (startInteger.isSet() && endInteger.isSet()) {
      int start = startInteger.get();
      int end = endInteger.get();
      return end - start + 1;
    }
    return 0;
  }
  
  ConstSectionTableRowData getData() {
    if (!retrieveData()) {
      return null;
    }
    return data;
  }
  
  String getInvalidReason() {
    return data.getInvalidReason();
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
