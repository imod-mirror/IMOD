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
public abstract class ConstJoinMetaData extends BaseMetaData {
  public static  final String  rcsid =  "$Id$";
  
  private static final String latestRevisionNumber = "1.0";
  
  protected static final String groupString = "Join";
  protected static final String sectionTableDataSizeString = "sectionTableDataSize";
  
  protected ArrayList sectionTableData;
  protected boolean useDensityRefSection;
  protected int densityRefSection;
  protected String workingDir;
  protected String rootName;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public ArrayList getSectionTableData() {
    return sectionTableData;
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    
    props.setProperty(group + revisionNumberString, latestRevisionNumber);
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
      if (!((SectionTableRowData) sectionTableData.get(i)).equals(sectionTableData.get(i))) {
        return false;
      }
    }
    return true;
  }
  
  public String getRootName() {
    return rootName;
  }
}
