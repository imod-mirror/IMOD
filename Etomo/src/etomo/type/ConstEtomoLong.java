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
