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
* <p> Revision 1.1.2.2  2004/10/21 02:49:13  sueh
* <p> bug# 520 Added equals, isSetAndNotDefault, and toString.  Changed
* <p> toString to getString.  Removed isDefault.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 17:59:48  sueh
* <p> bug# 520 The const part of EtomoInteger.
* <p> </p>
*/
public abstract class ConstEtomoInteger implements Storable, EtomoSimpleType {
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
  
  public String getString() {
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
  
  public Number getNumber() {
    if (value == Integer.MIN_VALUE) {
      return new Integer(resetValue);
    }
    return new Integer(value);
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSetAndNotDefault() {
    return value != Integer.MIN_VALUE && (defaultValue == Integer.MIN_VALUE || value != defaultValue);
  }
  
  public boolean isSet() {
    return value != Integer.MIN_VALUE;
  }
  
  public boolean equals(int value) {
    return this.value == value;
  }
  
  public boolean greaterThen(int value) {
    return this.value > value;
  }
  
  public boolean lessThen(int value) {
    return value != Integer.MIN_VALUE && this.value < value;
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
