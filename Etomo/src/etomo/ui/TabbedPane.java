package etomo.ui;

import java.awt.Component;

import javax.swing.JTabbedPane;

import etomo.EtomoDirector;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.UITestField;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
final class TabbedPane extends JTabbedPane {
  public static final String rcsid = "$Id$";

  public void addTab(String title, Component component) {
    super.addTab(title, component);
    int tabCount = getTabCount();
    String name;
    if (tabCount < 0) {
      throw new IllegalStateException("tabCount="+tabCount);
    }
    if (tabCount == 1) {
      name = Utilities.convertLabelToName(title);
      setName(name);
    }
    else {
      name = getName();
    }
    if (EtomoDirector.getInstance().isPrintNames()) {
      System.out.println(UITestField.TABBED_PANE.toString()
          + AutodocTokenizer.SEPARATOR_CHAR + name
          + AutodocTokenizer.SEPARATOR_CHAR + (tabCount - 1) + " "
          + AutodocTokenizer.DEFAULT_DELIMITER + ' ');
    }
  }
}
/**
 * <p> $Log$ </p>
 */