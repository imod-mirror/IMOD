package etomo.type;

import java.util.Properties;

import etomo.storage.Storable;

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
* <p> Revision 1.1.2.1  2004/09/29 19:15:08  sueh
* <p> bug# 520 Added base class for ConstMetaData and ConstJoinMetaData.
* <p> Implements Storable with abstract class.  Implements store(Properties),
* <p> since this function is generic and suitable for a const class.
* <p> </p>
*/
public abstract class BaseMetaData implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  protected static final String revisionNumberString = "RevisionNumber";
  protected static String fileExtension;

  protected String revisionNumber;
  protected AxisType axisType = AxisType.NOT_SET;
  protected String invalidReason = "";
  
  public abstract boolean isValid();
  public abstract boolean isValid(boolean fromScreen);
  public abstract void store(Properties props, String prepend);
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  public abstract String getMetaDataFileName();
  public abstract String getName();
  
  public void store(Properties props) {
    store(props, "");
  }
  
  public String getRevisionNumber() {
    return revisionNumber;
  }
  
  public AxisType getAxisType() {
    return axisType;
  }
  
  public String getInvalidReason() {
    return invalidReason;
  }
  
  public String getFileExtension() {
    return fileExtension;
  }
  
  public boolean equals(Object object) {
    if (!(object instanceof BaseMetaData))
      return false;
    BaseMetaData that = (BaseMetaData) object;
    
    // Ignore revision number, we are more concerned about the functional
    // content of the object

    if (axisType != that.axisType) {
      return false;
    }
    
    return true;
  }
}
