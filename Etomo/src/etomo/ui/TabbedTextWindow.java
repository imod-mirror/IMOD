package etomo.ui;

import java.awt.Container;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.StyledEditorKit;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> </p>
 */
public class TabbedTextWindow extends JFrame {
  public static final String rcsid = "$Id$";

  private Container mainPanel;
  private JTabbedPane tabPane = new JTabbedPane();

  public TabbedTextWindow(String label) {
    mainPanel = getContentPane();
    mainPanel.add(tabPane);
    setTitle(label);
    //  TODO: make the window size setable in properties
    setSize(625, 800);
  }

  /**
   * Open the array of files
   * @param files
   * @throws IOException
   * @throws FileNotFoundException
   */
  public void openFiles(String[] files)
    throws IOException, FileNotFoundException {
    FileReader reader;
    int nFiles = files.length;
    for (int i = 0; i < files.length; i++) {

      JEditorPane editorPane = new JEditorPane();
      editorPane.setEditorKit(new StyledEditorKit());
      JScrollPane scrollPane = new JScrollPane(editorPane);
      File file = new File(files[i]);
      tabPane.add(file.getName(), scrollPane);
      reader = new FileReader(file);
      editorPane.read(reader, file);
      editorPane.setEditable(false);
    }
  }

}
