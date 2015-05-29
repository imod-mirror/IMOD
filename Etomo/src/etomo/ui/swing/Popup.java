package etomo.ui.swing;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import etomo.EtomoDirector;
import etomo.logic.PopupTool;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.UITestActionType;
import etomo.type.UITestSubjectType;
import etomo.ui.UIComponent;
import etomo.util.Utilities;

/**
 * <p>Description: Opens a popup dialog.  Contains the result of the popup.</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class Popup {
  private final Component component;
  private final String message;
  private final CheckBox checkBox;
  private final String title;
  private final int optionType;

  private int result = JOptionPane.NO_OPTION;

  private Popup(final UIComponent uiComponent, final String title, final String message,
    final String checkBoxTitle, final int optionType) {
    if (uiComponent != null) {
      SwingComponent swingComponent = uiComponent.getUIComponent();
      component = swingComponent != null ? swingComponent.getComponent() : null;
    }
    else {
      component = null;
    }
    this.title = title;
    this.message = message;
    this.optionType = optionType;
    if (checkBoxTitle != null) {
      checkBox = new CheckBox(checkBoxTitle);
    }
    else {
      checkBox = null;
    }
  }

  /**
   * Use this instance by passing it to UIHarness.openPopup.
   * @return
   */
  public static Popup getYesNoInstance(final UIComponent uiComponent, final String title,
    final String message, final String checkBoxMessage) {
    return new Popup(uiComponent, title, message, checkBoxMessage,
      JOptionPane.YES_NO_OPTION);
  }

  /**
   * Creates and pops-up the dialog.
   */
  void open() {
    // Wrap the message
    ArrayList<String> wrappedMessage = PopupTool.wrapMessage(null, message, null);
    Object[] messageArray;
    if (wrappedMessage == null || wrappedMessage.isEmpty()) {
      messageArray = null;
    }
    else if (wrappedMessage.size() == 1) {
      messageArray = new Object[] { wrappedMessage.get(0) };
    }
    else {
      messageArray = wrappedMessage.toArray();
    }
    JOptionPane pane;
    // Create the pane.
    if (checkBox == null) {
      pane = new JOptionPane(messageArray, JOptionPane.QUESTION_MESSAGE, optionType);
    }
    else {
      pane =
        new JOptionPane(new Object[] { messageArray,
          Box.createRigidArea(FixedDim.x0_y10), checkBox }, JOptionPane.QUESTION_MESSAGE,
          optionType);
    }
    // Build and display the dialog.
    JDialog dialog = pane.createDialog(component, title);
    if (component != null) {
      // Adjust the location of the dialog so it will be entirely visible.
      Point location = dialog.getLocation();
      location.y =
        PopupTool.adjustLocationY(location.y, component.getHeight(), dialog.getHeight());
      dialog.setLocation(location);
    }
    // Give the dialog a name for uitest
    String name = Utilities.convertLabelToName(title);
    pane.setName(name);
    printName(name);
    // Display dialog
    dialog.setVisible(true);
    dialog.dispose();
    // Get the selected value
    Object value = pane.getValue();
    if (value == null) {
      result = JOptionPane.CLOSED_OPTION;
    }
    else if (value instanceof Integer) {
      result = ((Integer) value).intValue();
    }
  }

  private final void printName(String name) {
    if (EtomoDirector.INSTANCE.getArguments().isPrintNames()) {
      // print waitfor popup name/value pair
      StringBuilder builder =
        new StringBuilder(UITestActionType.WAIT.toString()
          + AutodocTokenizer.SEPARATOR_CHAR + UITestSubjectType.POPUP.toString()
          + AutodocTokenizer.SEPARATOR_CHAR + name + ' '
          + AutodocTokenizer.DEFAULT_DELIMITER + ' ');
      // if there are options, then print a popup name/value pair
      if (optionType == JOptionPane.YES_NO_OPTION) {
        builder.append("Yes,No");
      }
      System.out.println(builder);
    }
  }

  void log() {
    System.err.println("Popup:" + message);
    System.err.println();
    System.err.flush();
    if (EtomoDirector.INSTANCE.isTestFailed()) {
      result = JOptionPane.YES_OPTION;
    }
  }

  public boolean isYes() {
    return result == JOptionPane.YES_OPTION;
  }

  public boolean isCheckboxSelected() {
    return checkBox != null && checkBox.isSelected();
  }
}
