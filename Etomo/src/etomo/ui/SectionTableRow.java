package etomo.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.plaf.ColorUIResource;

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
* <p> Revision 1.1.2.1  2004/09/17 21:48:41  sueh
* <p> bug# 520 Handles row display, state, and data.  Can highlight all of its
* <p> fields.  Can expand the section field
* <p> </p>
*/
public class SectionTableRow {
  public static final String rcsid = "$Id$";

  private static ColorUIResource textFieldForeground = UIUtilities
      .getDefaultUIColor("TextField.foreground");
  private static ColorUIResource textFieldBackground = UIUtilities
      .getDefaultUIColor("TextField.background");
  private static ColorUIResource textFieldSelectedForeground = UIUtilities
      .getDefaultUIColor("TextField.selectionForeground");
  private static ColorUIResource textFieldSelectedBackground = UIUtilities
      .getDefaultUIColor("TextField.selectionBackground");
  private int rowNumber = -1;
  private boolean sectionExpanded = false;
  private SectionTablePanel table = null;
  private JButton rowNumberHeader = null;
  private int imodIndex = -1;
  private MultiLineToggleButton highlighterButton = null;
  private File sectionFile = null;
  private JTextField section = null;
  private JTextField sampleBottomStart = null;
  private JTextField sampleBottomEnd = null;
  private JTextField sampleTopStart = null;
  private JTextField sampleTopEnd = null;
  private JTextField finalStart = null;
  private JTextField finalEnd = null;
  private JTextField rotationAngleX = null;
  private JTextField rotationAngleY = null;
  private JTextField rotationAngleZ = null;
  private SectionTableRowActionListener actionListener = new SectionTableRowActionListener(
      this);
  
  /**
   * Create colors, fields, and buttons.  Add the row to the table
   * @param table
   * @param rowNumber
   */
  public SectionTableRow(SectionTablePanel table, int rowNumber, File tomogram,
      boolean sectionExpanded) {
    this.rowNumber = rowNumber;
    this.table = table;
    sectionFile = tomogram;
    this.sectionExpanded = sectionExpanded;

    if (textFieldForeground == null) {
      textFieldForeground = new ColorUIResource(0, 0, 0);
    }
    if (textFieldBackground == null) {
      textFieldBackground = new ColorUIResource(255, 255, 255);
    }
    if (textFieldSelectedForeground == null) {
      textFieldSelectedForeground = new ColorUIResource(0, 0, 0);
    }
    if (textFieldSelectedBackground == null) {
      textFieldSelectedBackground = new ColorUIResource(204, 204, 255);
    }

    //add row to table
    create();
  }
  
  void create() {
    GridBagConstraints constraints = table.getConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    rowNumberHeader = table.addHeader(Integer.toString(rowNumber),
        FixedDim.rowNumberWidth);
    constraints.weightx = 0.0;
    highlighterButton = table.addToggleButton("=>", FixedDim.highlighterWidth);
    highlighterButton.addActionListener(actionListener);
    constraints.gridwidth = 2;
    section = table.addField();
    setSectionText();
    constraints.gridwidth = 1;
    sampleBottomStart = table.addField();
    sampleBottomEnd = table.addField();
    sampleTopStart = table.addField();
    sampleTopEnd = table.addField();
    finalStart = table.addField();
    finalEnd = table.addField();
    rotationAngleX = table.addField();
    rotationAngleY = table.addField();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    rotationAngleZ = table.addField();
  }
  
  void remove() {
    table.removeFromTable(rowNumberHeader);
    table.removeFromTable(highlighterButton);
    table.removeFromTable(section);
    table.removeFromTable(sampleBottomStart);
    table.removeFromTable(sampleBottomEnd);
    table.removeFromTable(sampleTopStart);
    table.removeFromTable(sampleTopEnd);
    table.removeFromTable(finalStart);
    table.removeFromTable(finalEnd);
    table.removeFromTable(rotationAngleX);
    table.removeFromTable(rotationAngleY);
    table.removeFromTable(rotationAngleZ);
  }
  
  void add() {
    GridBagConstraints constraints = table.getConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    table.addHeader(rowNumberHeader, Integer.toString(rowNumber),
        FixedDim.rowNumberWidth);
    constraints.weightx = 0.0;
    table.addToggleButton(highlighterButton, "=>", FixedDim.highlighterWidth);
    highlighterButton.addActionListener(actionListener);
    constraints.gridwidth = 2;
    table.addField(section);
    constraints.gridwidth = 1;
    table.addField(sampleBottomStart);
    table.addField(sampleBottomEnd);
    table.addField(sampleTopStart);
    table.addField(sampleTopEnd);
    table.addField(finalStart);
    table.addField(finalEnd);
    table.addField(rotationAngleX);
    table.addField(rotationAngleY);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    table.addField(rotationAngleZ);
  }
  
  /**
   * Toggle section between absolute path when expand is true, and name when
   * expand is false.
   * @param expand
   */
  void expandSection(boolean expand) {
    sectionExpanded = expand;
    if (sectionFile == null) {
      return;
    }
    setSectionText();
  }
  
  private void setSectionText() {
    if (sectionExpanded) {
      section.setText(sectionFile.getAbsolutePath());
    }
    else {
      section.setText(sectionFile.getName());
    }
  }
  
  void setRowNumber(int rowNumber) {
    this.rowNumber = rowNumber;
    rowNumberHeader.setText("<html><b>" + Integer.toString(rowNumber) + "</b>");
  }
  
  void setImodIndex(int imodIndex) {
    this.imodIndex = imodIndex;
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
    if (highlighterButton.isSelected()) {
      table.highlighting(rowNumber);
      setColors(textFieldSelectedForeground, textFieldSelectedBackground);
    }
    else {
      setColors(textFieldForeground, textFieldBackground);
    }
  }
  
  boolean isHighlighted() {
    return highlighterButton.isSelected();
  }
  
  File getSectionFile() {
    return sectionFile;
  }
  
  String getSectionText() {
    return section.getText();
  }
  
  int getImodIndex() {
    return imodIndex;
  }
  
  /**
   * Change the colors of all the fields in the row to foreground and
   * background.
   * @param foreground
   * @param background
   */
  private void setColors(Color foreground, Color background) {
    section.setForeground(foreground);
    section.setBackground(background);
    
    sampleBottomStart.setForeground(foreground);
    sampleBottomStart.setBackground(background);
    
    sampleBottomEnd.setForeground(foreground);
    sampleBottomEnd.setBackground(background);
    
    sampleTopStart.setForeground(foreground);
    sampleTopStart.setBackground(background);
    
    sampleTopEnd.setForeground(foreground);
    sampleTopEnd.setBackground(background);
    
    finalStart.setForeground(foreground);
    finalStart.setBackground(background);
    
    finalEnd.setForeground(foreground);
    finalEnd.setBackground(background);
    
    rotationAngleX.setForeground(foreground);
    rotationAngleX.setBackground(background);
    
    rotationAngleY.setForeground(foreground);
    rotationAngleY.setBackground(background);
    
    rotationAngleZ.setForeground(foreground);
    rotationAngleZ.setBackground(background);
  }
  
  /**
   * Handle button actions
   * @param event
   */
  private void buttonAction(ActionEvent event) {
    String command = event.getActionCommand();

    if (command.equals(highlighterButton.getActionCommand())) {
      highlight();
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
