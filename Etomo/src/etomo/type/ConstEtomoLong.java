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
* <p> Revision 1.1.2.1  2004/10/21 02:49:33  sueh
* <p> bug# 520 Const object for EtomoLong.
* <p> </p>
*/
public abstract class ConstEtomoLong implements Storable, EtomoSimpleType {
  public static  final String  rcsid =  "$Id$";
  
  public static final long unsetValue = Long.MIN_VALUE;
  
  protected long value = unsetValue;
  protected long defaultValue = unsetValue;
  protected long recommendedValue = unsetValue;
  protected long resetValue = unsetValue;
  
  protected String name;
  protected String description = null;
  protected String invalidReason = null;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public void store(Properties props) {
    props.setProperty(name, Long.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Long.toString(value));
  }
  
  public String getString() {
    if (value == Long.MIN_VALUE) {
      return "";
    }
    return Long.toString(value);
  }
  
  public long get() {
    if (value == Long.MIN_VALUE) {
      return resetValue;
    }
    return value;
  }
  
  public Number getNumber() {
    if (value == Long.MIN_VALUE) {
      return new Long(resetValue);
    }
    return new Long(value);
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSetAndNotDefault() {
    return value != Long.MIN_VALUE && (defaultValue == Long.MIN_VALUE || value != defaultValue);
  }
  
  public boolean isSet() {
    return value != Long.MIN_VALUE;
  }
  
  public boolean equals(long value) {
    return this.value == value;
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
