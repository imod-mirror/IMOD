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
* <p> Revision 1.1.2.1  2004/10/21 02:48:57  sueh
* <p> bug# Const object for EtomoFloat.
* <p> </p>
*/
public abstract class ConstEtomoFloat implements Storable, EtomoSimpleType {
  public static  final String  rcsid =  "$Id$";
  
  protected static final float unsetValue = Float.NaN;
  
  protected float value = unsetValue;
  protected float defaultValue = unsetValue;
  protected float recommendedValue = unsetValue;
  protected float resetValue = unsetValue;
  
  protected String name;
  protected String description = null;
  protected String invalidReason = null;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public void store(Properties props) {
    props.setProperty(name, Float.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Float.toString(value));
  }
  
  public String getString() {
    if (Float.isNaN(value)) {
      return "";
    }
    return Float.toString(value);
  }
  
  public float get() {
    if (Float.isNaN(value)) {
      return resetValue;
    }
    return value;
  }
  
  public Number getNumber() {
    if (Float.isNaN(value)) {
      return new Float(resetValue);
    }
    return new Float(value);
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSetAndNotDefault() {
    return !Float.isNaN(value) && (Float.isNaN(defaultValue) || value != defaultValue);
  }
  
  public boolean isSet() {
    return !Float.isNaN(value);
  }
  
  public boolean equals(float value) {
    return (Float.isNaN(this.value) && Float.isNaN(value)) || this.value == value;
  }
   
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  
  protected String paramString() {
    return ",\nname=" + name + ",\ndescription=" + description
        + ",\nunsetValue=" + unsetValue + ",\nvalue=" + value
        + ",\ndefaultValue=" + defaultValue + ",\nrecommendedValue="
        + recommendedValue + ",\nresetValue=" + resetValue
        + ",\ninvalidReason=" + invalidReason;
  } 
}
