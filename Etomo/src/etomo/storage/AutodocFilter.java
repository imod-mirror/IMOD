package etomo.storage;

import java.io.File;

import etomo.storage.autodoc.AutodocFactory;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */

public class AutodocFilter extends javax.swing.filechooser.FileFilter implements
    java.io.FileFilter {
  public static final String rcsid = "$Id$";

  private final boolean excludeHidden;

  public AutodocFilter() {
    excludeHidden = false;
  }

  public AutodocFilter(final boolean excludeHidden) {
    this.excludeHidden = excludeHidden;
  }

  public boolean accept(File f) {
    if (!f.exists()) {
      System.err.println("Warning: " + f.getAbsolutePath() + " does not exist");
      return false;
    }
    if (f.isFile()) {
      String name = f.getName();
      return name.endsWith(AutodocFactory.EXTENSION)
          && (!excludeHidden || !name.startsWith("."));
    }
    return true;
  }

  public String getDescription() {
    return "Autodoc file";
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.1  2005/12/23 02:06:08  sueh
 * <p> bug# 675 Added a filter to find autodoc files.
 * <p> </p>
 */
