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
* <p> Revision 1.1.2.4  2004/10/25 23:05:38  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p>
* <p> Revision 1.1.2.3  2004/10/22 21:01:35  sueh
* <p> bug# 520 Moved common code to EtomoSimpleType
* <p>
* <p> Revision 1.1.2.2  2004/10/22 03:22:25  sueh
* <p> bug# 520 added getNumber().
* <p>
* <p> Revision 1.1.2.1  2004/10/21 02:49:33  sueh
* <p> bug# 520 Const object for EtomoLong.
* <p> </p>
*/
public abstract class ConstEtomoLong extends EtomoSimpleType implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  public static final long unsetValue = Long.MIN_VALUE;
  
  protected long value = unsetValue;
  protected long defaultValue = unsetValue;
  protected long recommendedValue = unsetValue;
  protected long resetValue = unsetValue;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public ConstEtomoLong() {
    super();
  }

  public ConstEtomoLong(String name) {
    super(name);
  }
  
  public EtomoSimpleType setDefault(long defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }
  
  public void store(Properties props) {
    props.setProperty(name, Long.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Long.toString(value));
  }
  
  public void remove(Properties props, String prepend) {
    props.remove(prepend + "." + name);
  }
  
  public String getString() {
    return getString(false);
  }
  public String getString(boolean useDefault) {
    if (isSet()) {
      return Long.toString(value);
    }
    if (resetValue != Long.MIN_VALUE) {
      return Long.toString(resetValue);
    }
    if (useDefault && defaultValue != Long.MIN_VALUE) {
      return Long.toString(defaultValue);
    }
    return "";
  }
  
  public long get() {
    return get(false);
  }
  public long get(boolean useDefault) {
    if (isSet()) {
      return value;
    }
    if (resetValue != Long.MIN_VALUE) {
      return resetValue;
    }
    if (useDefault && defaultValue != Long.MIN_VALUE) {
      return defaultValue;
    }
    return unsetValue;
  }
  
  public  Number getNumber() {
    return getNumber(false);
  }
  public  Number getNumber(boolean useDefault) {
    if (isSet()) {
      return new Long(value);
    }
    if (resetValue != Long.MIN_VALUE) {
      return new Long(resetValue);
    }
    if (useDefault && defaultValue != Long.MIN_VALUE) {
      return new Long(defaultValue);
    }
    return new Long(unsetValue);
  }
  
  public EtomoSimpleType getNegation() {
    return new EtomoFloat(value * -1);
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
  
  protected String paramString() {
    return super.paramString() + ",\nunsetValue=" + unsetValue + ",\nvalue="
        + value + ",\ndefaultValue=" + defaultValue + ",\nrecommendedValue="
        + recommendedValue + ",\nresetValue=" + resetValue;
  }
  
}
