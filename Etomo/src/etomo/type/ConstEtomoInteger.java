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
* <p> $Log$ </p>
*/
public abstract class ConstEtomoInteger implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  public static final int unsetValue = Integer.MIN_VALUE;
  
  protected int value = unsetValue;
  protected int defaultValue = unsetValue;
  protected int recommendedValue = unsetValue;
  protected int resetValue = unsetValue;
  
  protected String name;
  protected String description = null;
  protected String invalidReason = null;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public void store(Properties props) {
    props.setProperty(name, Integer.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Integer.toString(value));
  }
  
  public String toString() {
    if (value == Integer.MIN_VALUE) {
      return "";
    }
    return Integer.toString(value);
  }
  
  public int get() {
    if (value == Integer.MIN_VALUE) {
      return resetValue;
    }
    return value;
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSet() {
    return value != unsetValue;
  }
  
  public boolean isDefault() {
    return defaultValue != unsetValue && value == defaultValue;
  }
}
