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
* <p> Revision 1.1.2.2  2004/10/21 02:48:28  sueh
* <p> bug# 520 Added equals, isSetAndNotDefault, and toString.  Changed
* <p> toString to getString.  Removed isDefault.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 17:59:23  sueh
* <p> bug# 520 The const part of EtomoDouble.
* <p> </p>
*/
public abstract class ConstEtomoDouble implements Storable, EtomoSimpleType {
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
  
  public String getString() {
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
  
  public Number getNumber() {
    if (Double.isNaN(value)) {
      return new Double(resetValue);
    }
    return new Double(value);
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSetAndNotDefault() {
    return !Double.isNaN(value) && (Double.isNaN(defaultValue) || value != defaultValue);
  }
  
  public boolean isSet() {
    return !Double.isNaN(value);
  }
  
  public boolean equals(double value) {
    return (Double.isNaN(this.value) && Double.isNaN(value)) || this.value == value;
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
