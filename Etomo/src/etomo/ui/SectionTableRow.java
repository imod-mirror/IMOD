package etomo.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
* <p> $Log$ </p>
*/
public class SectionTableRow {
  public static  final String  rcsid =  "$Id$";
  
  private static ColorUIResource textFieldForeground = UIUtilities.getDefaultUIColor("TextField.foreground");
  private static ColorUIResource textFieldBackground = UIUtilities.getDefaultUIColor("TextField.background");
  private static ColorUIResource textFieldSelectedForeground = UIUtilities.getDefaultUIColor("TextField.selectionForeground");
  private static ColorUIResource textFieldSelectedBackground = UIUtilities.getDefaultUIColor("TextField.selectionBackground");
  private int rowNumber = -1;
  private SectionTablePanel table = null;
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
  
  /**
   * Create colors, fields, and buttons.  Add the row to the table
   * @param table
   * @param rowNumber
   */
  public SectionTableRow(SectionTablePanel table, int rowNumber) {
    this.rowNumber = rowNumber;
    this.table = table;

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
    SectionTableRowActionListener actionListener = new SectionTableRowActionListener(
        this);

    //add row to table
    GridBagConstraints constraints = table.getConstraints();
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    table.addHeader(Integer.toString(rowNumber), FixedDim.numericWidth);
    constraints.weightx = 0.0;
    highlighterButton = table.addToggleButton("=>", FixedDim.highlighterWidth);
    highlighterButton.addActionListener(actionListener);
    constraints.gridwidth = 2;
    section = table.addField();
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
  
  /**
   * Toggle section between absolution path when expand is true, and name when
   * expand is false.
   * @param expand
   */
  public void expandSection(boolean expand) {
    if (sectionFile == null) {
      return;
    }
    if (expand) {
      section.setText(sectionFile.getAbsolutePath());
    }
    else {
      section.setText(sectionFile.getName());
    }
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
