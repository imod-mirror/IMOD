package etomo.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

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
  
  private JPanel rootPanel;
  private ExpandButton btnExpandSections;
  private ArrayList rows = new ArrayList();
  private GridBagLayout layout = new GridBagLayout();
  private GridBagConstraints constraints = new GridBagConstraints();
  
  /**
   * Creates the panel and table.
   *
   */
  SectionTablePanel() {
    createRootPanel();
  }
  
  /**
   * Creates the panel and table.  Adds the header rows.  Adds SectionTableRows
   * to rows to create each row.
   *
   */
  private void createRootPanel() {
    rootPanel = new JPanel();
    rootPanel.setBorder(LineBorder.createBlackLineBorder());
    rootPanel.setLayout(layout);
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
    btnExpandSections = addExpandButton();
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
    rows.add(new SectionTableRow(this, 1));
    rows.add(new SectionTableRow(this, 2));
    rows.add(new SectionTableRow(this, 3));
    rows.add(new SectionTableRow(this, 4));
  }
  
  /**
   * Informs this panel that a row is highlighting.  Only one row may be
   * highlighted at once, so it turns off highlighting on all the other rows.
   * @param rowNumber
   */
  void highlighting(int rowNumber) {
    for (int i = 0; i < rows.size(); i++) {
      if (i != rowNumber - 1) {
        ((SectionTableRow)rows.get(i)).setHighlight(false);
      }
    }
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
   * Add a header cell with a preferred width and set the text to value.
   * @param value
   * @param width
   */
  void addHeader(String text, int width) {
    JButton cell = createHeader(text);
    Dimension size = cell.getPreferredSize();
    size.width = width;
    cell.setPreferredSize(size);
    addToTable(cell);
  }
  
  /**
   * Add a JComponent to the table.
   * @param cell
   */
  private void addToTable(JComponent cell) {
    layout.setConstraints(cell, constraints);
    rootPanel.add(cell);
  }
  
  /**
   * Create a field and add it to the table.
   * @return The created field
   */
  JTextField addField() {
    JTextField cell = new JTextField();
    addToTable(cell);
    return cell;
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
   * Create a multi line toggle button.  Set the border to raised bevel to make
   * it 3D.  Set its preferred widthl
   * @param value
   * @param width
   * @return button created
   */
  MultiLineToggleButton addToggleButton(String text, int width) {
    MultiLineToggleButton button = new MultiLineToggleButton(text);
    button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    Dimension size = button.getPreferredSize();
    size.width = width;
    button.setPreferredSize(size);
    addToTable(button);
    return button;
  }
  
  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
  }

  Container getContainer() {
    return rootPanel;
  }
}
