package etomo.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

/**
* <p>Description: </p>
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
* <p> Revision 1.1.2.1  2004/09/15 22:47:26  sueh
* <p> bug# 520 creates the Sections table for JoinDialog.
* <p> </p>
*/
public class SectionTablePanel implements ContextMenu {
  public static  final String  rcsid =  "$Id$";
  
  JPanel rootPanel;
  MultiLineButton btnExpandSections;
  MultiLineButton btnHighlighter;
  
  GridBagLayout layout = new GridBagLayout();
  GridBagConstraints constraints = new GridBagConstraints();
  
  SectionTablePanel() {
    createRootPanel();
  }
  
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
    addSquareMultiLineButton(btnExpandSections, ">");
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
    addDataRow(1);
    addDataRow(2);
  }
  
  private void addDataRow(int rowNumber) {
    constraints.gridwidth = 1;
    addHeader(Integer.toString(rowNumber), FixedDim.rowNumberWdith);
    addMultiLineButton(btnHighlighter, "=>", FixedDim.highlighterWidth);
    constraints.gridwidth = 2;
    addField();
    constraints.gridwidth = 1;
    addField();
    addField();
    addField();
    addField();
    addField();
    addField();
    addField();
    addField();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    addField();
  }
  
  private void addHeader(String value) {
    JButton cell = createHeader(value);
    addToTable(cell);
  }
  
  private JButton createHeader(String value) {
    String htmlValue = "<html><b>" + value + "</b>";
    JButton cell = new JButton(htmlValue);
    cell.setBorder(BorderFactory.createEtchedBorder());
    cell.setEnabled(false);
    System.out.println("value=" + value + ",size=" + cell.getPreferredSize());
    return cell;
  }
  
  private void addHeader() {
    addHeader("");
  }
  
  private void addHeader(String value, int width) {
    JButton cell = createHeader(value);
    Dimension size = cell.getPreferredSize();
    size.width = width;
    cell.setPreferredSize(size);
    addToTable(cell);
  }
  
  private void addToTable(JComponent cell) {
    layout.setConstraints(cell, constraints);
    rootPanel.add(cell);
  }
  
  private void addField() {
    JTextField cell = new JTextField();
    addToTable(cell);
  }
  
  private void addSquareMultiLineButton(JButton button, String value) {
    button = new MultiLineButton(value);
    button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    Dimension size = button.getPreferredSize();
    if (size.width < size.height) {
      size.width = size.height;
    }
    button.setPreferredSize(size);
    layout.setConstraints(button, constraints);
    rootPanel.add(button);
  }
  
  private void addMultiLineButton(JButton button, String value, int width) {
    button = new MultiLineButton(value);
    button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    Dimension size = button.getPreferredSize();
    size.width = width;
    button.setPreferredSize(size);
    layout.setConstraints(button, constraints);
    rootPanel.add(button);
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
