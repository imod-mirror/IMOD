package etomo.type;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

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
* <p> Revision 1.1.2.2  2004/10/01 19:45:26  sueh
* <p> bug# 520 Define a new join string that will go in the menu.  Set a file
* <p> extension value.
* <p>
* <p> Revision 1.1.2.1  2004/09/29 19:17:41  sueh
* <p> bug# 520 The const part of the JoinMetaData class.  Implements
* <p> storable with abstract load functions.  Contains member variables and
* <p> get functions.
* <p> </p>
*/
public abstract class ConstJoinMetaData extends BaseMetaData {
  public static final String rcsid = "$Id$";

  protected static final String latestRevisionNumber = "1.0";
  private static final String newJoinTitle = "New Join";

  protected static final String groupString = "Join";
  protected static final String sectionTableDataSizeString = "SectionTableDataSize";
  protected static final String densityRefSectionString = "DensityRefSection";
  protected static final String workingDirString = "WorkingDirString";
  protected static final String rootNameString = "RootName";

  protected static final int defaultDensityRefSection = 1;

  protected ArrayList sectionTableData;
  protected int densityRefSection;
  protected String workingDir;
  protected String rootName;

  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);

  public ConstJoinMetaData() {
    fileExtension = "ejf";
  }

  public ArrayList getSectionTableData() {
    return sectionTableData;
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";

    props.setProperty(group + revisionNumberString, latestRevisionNumber);
    props.setProperty(group + workingDirString, workingDir);
    props.setProperty(group + rootNameString, rootName);
    props.setProperty(group + densityRefSectionString, Integer
        .toString(densityRefSection));
    props.setProperty(group + sectionTableDataSizeString, Integer
        .toString(sectionTableData.size()));

    if (sectionTableData != null) {
      for (int i = 0; i < sectionTableData.size(); i++) {
        ((SectionTableRowData) sectionTableData.get(i)).store(props, prepend);
      }
    }
  }

  protected static String createPrepend(String prepend) {
    if (prepend == "") {
      return groupString + ".";
    }
    return prepend + "." + groupString;
  }

  public boolean isValid() {
    return true;
  }

  public boolean isValid(boolean fromScreen) {
    return true;
  }

  public boolean isValidForMakeSamples() {
    if (workingDir == null || !workingDir.matches("\\S+")) {
      invalidReason = "The working directory is not set";
      return false;
    }
    File dir = new File(workingDir);
    if (dir == null || !dir.exists()) {
      invalidReason = "The directory," + workingDir + ", does not exist.";
      return false;
    }
    if (!dir.isDirectory()) {
      invalidReason = "The working directory," + workingDir
          + ", must be a directory.";
      return false;
    }
    if (!dir.canWrite()) {
      invalidReason = "The working directory," + workingDir
          + ", must be writable.";
      return false;
    }
    if (rootName == null || !rootName.matches("\\S+")) {
      invalidReason = "The root name is not set.";
      return false;
    }
    int sectionTableDataSize = sectionTableData.size();
    for (int i = 0; i < sectionTableDataSize; i++) {
      SectionTableRowData row = (SectionTableRowData) sectionTableData.get(i);
      if (!row.isValidForMakeSamples(sectionTableDataSize)) {
        invalidReason = row.getInvalidReason();
      }
    }

    return true;
  }

  public boolean equals(Object object) {
    if (!super.equals(object)) {
      return false;
    }

    if (!(object instanceof ConstJoinMetaData))
      return false;
    ConstJoinMetaData that = (ConstJoinMetaData) object;

    if ((sectionTableData == null && that.sectionTableData != null)
        || (sectionTableData != null && that.sectionTableData == null)) {
      return false;
    }
    if (sectionTableData.size() != that.sectionTableData.size()) {
      return false;
    }

    for (int i = 0; i < sectionTableData.size(); i++) {
      if (!((SectionTableRowData) sectionTableData.get(i))
          .equals(sectionTableData.get(i))) {
        return false;
      }
    }
    return true;
  }

  public String getRootName() {
    return rootName;
  }

  public String getMetaDataFileName() {
    if (rootName.equals("")) {
      return null;
    }
    return rootName + "." + fileExtension;
  }

  public String getName() {
    if (rootName.equals("")) {
      return newJoinTitle;
    }
    return rootName;
  }

  public static String getNewFileTitle() {
    return newJoinTitle;
  }
}