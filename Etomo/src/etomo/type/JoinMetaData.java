package etomo.type;

import java.util.ArrayList;
import java.util.Properties;

import etomo.EtomoDirector;

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
* <p> Revision 1.1.2.3  2004/10/08 16:23:40  sueh
* <p> bug# 520 Make sure  sectionTableData exists before it is used.
* <p>
* <p> Revision 1.1.2.2  2004/10/06 02:14:13  sueh
* <p> bug# 520 Removed Use density reference checkbox.  Changed string
* <p> default to "", since their default when coming from store() is "".  Added
* <p> variables to the load() function.
* <p>
* <p> Revision 1.1.2.1  2004/09/29 19:28:03  sueh
* <p> bug# 520 Meta data for serial sections.  Non-const class implements load
* <p> and set functions.
* <p> </p>
*/
public class JoinMetaData extends ConstJoinMetaData {
  public static final String rcsid = "$Id$";

  public JoinMetaData() {
    reset();
  }

  private void reset() {
    revisionNumber = "";
    sectionTableData = null;
    densityRefSection = defaultDensityRefSection;
    workingDir = "";
    rootName = "";
  }

  /**
   *  Get the objects attributes from the properties object.
   */
  public void load(Properties props) {
    load(props, "");
  }

  public void load(Properties props, String prepend) {
    reset();
    prepend = createPrepend(prepend);
    String group = prepend + ".";

    revisionNumber = props.getProperty(group + revisionNumberString, latestRevisionNumber);
    workingDir = props.getProperty(group + workingDirString, "");
    rootName = props.getProperty(group + rootNameString, "");
    int sectionTableRowsSize = Integer.parseInt(props.getProperty(group
        + sectionTableDataSizeString, "-1"));
    densityRefSection = Integer.parseInt(props.getProperty(group
        + densityRefSectionString, Integer.toString(defaultDensityRefSection)));
    if (sectionTableRowsSize < 1) {
      return;
    }
    sectionTableData = new ArrayList(sectionTableRowsSize);
    for (int i = 0; i < sectionTableRowsSize; i++) {
      SectionTableRowData row = new SectionTableRowData(i + 1);
      row.load(props, prepend);
      int rowIndex = row.getRowIndex();
      if (rowIndex < 0) {
        EtomoDirector.getInstance().getMainFrame().openMessageDialog(
            "Invalid row index: " + rowIndex, "Corrupted .ejf file");
      }
      sectionTableData.add(row.getRowIndex(), row);
    }
  }

  public void setDensityRefSection(Object densityRefSection) {
    this.densityRefSection = ((Integer) densityRefSection).intValue();
  }

  public void setWorkingDir(String workingDir) {
    this.workingDir = workingDir;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }

  public void resetSectionTableData() {
    sectionTableData = null;
  }

  public void setSectionTableData(ConstSectionTableRowData row) {
    if (sectionTableData == null) {
      sectionTableData = new ArrayList();
    }
    sectionTableData.add(row);
  }
}