package etomo.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
* <p> $Log$ </p>
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
    constraints.gridwidth = 1;
    add("Order");
    constraints.weighty = 0.0;
    add("Sections");
    addExandButton(btnExpandSections);
    constraints.gridwidth = 4;
    add("Sample Slices");
    constraints.gridwidth = 2;
    add("Final");
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    add("Rotation Angles");
    //second row
    constraints.weightx = 0.0;
    constraints.gridwidth = 1;
    add("");
    constraints.gridwidth = 2;
    add("");
    add("Bottom");
    add("Top");
    constraints.gridwidth = 2;
    add("");
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    add("");
    //Third row
    constraints.gridwidth = 1;
    add("");
    constraints.gridwidth = 2;
    add("");
    constraints.gridwidth = 1;
    add("Start");
    add("End");
    add("Start");
    add("End");
    add("Start");
    add("End");
    add("X");
    add("Y");
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    add("Z");
    //First data row
    constraints.gridwidth = 1;
    addHighlighterButton(btnHighlighter);
    constraints.gridwidth = 2;
    add();
    constraints.gridwidth = 1;
    add();
    add();
    add();
    add();
    add();
    add();
    add();
    add();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    add();
    //Second data row
    constraints.gridwidth = 1;
    addHighlighterButton(btnHighlighter);
    constraints.gridwidth = 2;
    add();
    constraints.gridwidth = 1;
    add();
    add();
    add();
    add();
    add();
    add();
    add();
    add();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    add();
  }
  
  private void add(String value) {
    String htmlValue = "<html><b>" + value + "</b>";
    JButton cell = new JButton(htmlValue);
    cell.setEnabled(false);
    layout.setConstraints(cell, constraints);
    rootPanel.add(cell);
  }
  
  private void add() {
    JTextField cell = new JTextField();
    layout.setConstraints(cell, constraints);
    rootPanel.add(cell);
  }
  
  private void addExandButton(JButton btnExpand) {
    btnExpand = new JButton(">");
    btnExpand.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    layout.setConstraints(btnExpand, constraints);
    rootPanel.add(btnExpand);
  }
  
  private void addHighlighterButton(JButton btnHighLighter) {
    btnHighLighter = new JButton("=>");
    btnHighLighter.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    layout.setConstraints(btnHighLighter, constraints);
    rootPanel.add(btnHighLighter);
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
