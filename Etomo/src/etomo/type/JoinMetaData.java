package etomo.type;

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
* <p> $Log$ </p>
*/
public class JoinMetaData extends ConstJoinMetaData {
  public static  final String  rcsid =  "$Id$";
  
  public JoinMetaData() {
    reset();
  }
  
  private void reset() {
    revisionNumber = "";
    sectionTableData = null;
    useDensityRefSection = false;
    densityRefSection = 0;
    workingDir = null;
    rootName = null;
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
    
    revisionNumber = props.getProperty(group + revisionNumberString, "1.0");
    
    int sectionTableRowsSize = Integer.parseInt(props.getProperty(group
        + sectionTableDataSizeString, "-1"));
    if(sectionTableRowsSize <= 0) {
      return;
    }
    
    if (sectionTableData == null) {
      sectionTableData = new ArrayList(sectionTableRowsSize);
    }
    for (int i = 0; i < sectionTableRowsSize; i++) {
      SectionTableRowData row = new SectionTableRowData();
      row.load(props, prepend);
      sectionTableData.add(row.getRowIndex(), row);
    }
  }
  
  public void setUseDensityRefSection(boolean useDensityRefSection) {
    this.useDensityRefSection = useDensityRefSection;
  }
  
  public void setDensityRefSection(Object densityRefSection) {
    this.densityRefSection = ((Integer) densityRefSection).intValue();;
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
    sectionTableData.add(row);
  }
}
