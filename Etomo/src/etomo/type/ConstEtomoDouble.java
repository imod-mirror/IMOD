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
public abstract class ConstEtomoDouble implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  protected static final double unsetValue = Double.NaN;
  
  protected double value = unsetValue;
  protected double defaultValue = unsetValue;
  protected double recommendedValue = unsetValue;
  protected double resetValue = unsetValue;
  
  protected String name;
  protected String description = null;
  protected String invalidReason = null;
  
  public abstract void load(Properties props);  
  public abstract void load(Properties props, String prepend);
  
  public void store(Properties props) {
    props.setProperty(name, Double.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Double.toString(value));
  }
  
  public String toString() {
    if (Double.isNaN(value)) {
      return "";
    }
    return Double.toString(value);
  }
  
  public double get() {
    if (Double.isNaN(value)) {
      return resetValue;
    }
    return value;
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSet() {
    return !Double.isNaN(value);
  }
  
  public boolean isDefault() {
    return !Double.isNaN(defaultValue) && value == defaultValue;
  }
}
