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
* <p> Revision 1.1.2.4  2004/10/22 21:01:20  sueh
* <p> bug# 520 Moved common code to EtomoSimpleType.  Added lessThen
* <p> and greaterOrEqual.
* <p>
* <p> Revision 1.1.2.3  2004/10/22 03:22:14  sueh
* <p> bug# 520 added getNumber().  Added greaterThen and lessThen.
* <p>
* <p> Revision 1.1.2.2  2004/10/21 02:49:13  sueh
* <p> bug# 520 Added equals, isSetAndNotDefault, and toString.  Changed
* <p> toString to getString.  Removed isDefault.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 17:59:48  sueh
* <p> bug# 520 The const part of EtomoInteger.
* <p> </p>
*/
public abstract class ConstEtomoInteger extends EtomoSimpleType implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  public static final int unsetValue = Integer.MIN_VALUE;
  
  protected int value = unsetValue;
  protected int defaultValue = unsetValue;
  protected int recommendedValue = unsetValue;
  protected int resetValue = unsetValue;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public ConstEtomoInteger() {
    super();
  }

  public ConstEtomoInteger(String name) {
    super(name);
  }
  
  public EtomoSimpleType setDefault(int defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }
  
  public void store(Properties props) {
    props.setProperty(name, Integer.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Integer.toString(value));
  }
  
  public String getString() {
    return getString(false);
  }
  public String getString(boolean useDefault) {
    if (isSet()) {
      return Integer.toString(value);
    }
    if (resetValue != Integer.MIN_VALUE) {
      return Integer.toString(resetValue);
    }
    if (useDefault && defaultValue != Integer.MIN_VALUE) {
      return Integer.toString(defaultValue);
    }
    return "";
  }
  
  public int get() {
    return get(false);
  }
  public int get(boolean useDefault) {
    if (isSet()) {
      return value;
    }
    if (resetValue != Integer.MIN_VALUE) {
      return resetValue;
    }
    if (useDefault && defaultValue != Integer.MIN_VALUE) {
      return defaultValue;
    }
    return unsetValue;
  }
  
  public  Number getNumber() {
    return getNumber(false);
  }
  public  Number getNumber(boolean useDefault) {
    if (isSet()) {
      return new Integer(value);
    }
    if (resetValue != Integer.MIN_VALUE) {
      return new Integer(resetValue);
    }
    if (useDefault && defaultValue != Integer.MIN_VALUE) {
      return new Integer(defaultValue);
    }
    return new Integer(unsetValue);
  }
  
  public EtomoSimpleType getNegation() {
    return new EtomoInteger(value * -1);
  }

  public boolean isSetAndNotDefault() {
    return isSet() && (defaultValue == unsetValue || value != defaultValue);
  }
  
  public boolean isSet() {
    return value != unsetValue;
  }
  
  public boolean equals(int value) {
    return this.value == value;
  }
  
  public boolean greaterOrEqual(ConstEtomoInteger that) {
    return isSet() && that.isSet() && value >= that.value;
  }
  
  public boolean lessThen(int value) {
    return isSet() && this.value < value;
  }
  
  protected String paramString() {
    return super.paramString() + ",\nunsetValue=" + unsetValue + ",\nvalue="
        + value + ",\ndefaultValue=" + defaultValue + ",\nrecommendedValue="
        + recommendedValue + ",\nresetValue=" + resetValue;
  }
  
}
