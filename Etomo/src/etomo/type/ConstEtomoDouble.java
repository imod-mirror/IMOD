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
* <p> Revision 1.1.2.7  2004/10/30 02:31:23  sueh
* <p> bug# 520 Fixed getNegation so that it copies the entire class to a new
* <p> instances and negates all values.
* <p>
* <p> Revision 1.1.2.6  2004/10/29 22:10:53  sueh
* <p> bug# 520 Added remove() to remove value from the meta data file.
* <p>
* <p> Revision 1.1.2.5  2004/10/25 23:04:23  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p>
* <p> Revision 1.1.2.4  2004/10/22 21:00:03  sueh
* <p> bug# 520 Moved common code to EtomoSimpleType
* <p>
* <p> Revision 1.1.2.3  2004/10/22 03:21:36  sueh
* <p> bug# 520 added getNumber().
* <p>
* <p> Revision 1.1.2.2  2004/10/21 02:48:28  sueh
* <p> bug# 520 Added equals, isSetAndNotDefault, and toString.  Changed
* <p> toString to getString.  Removed isDefault.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 17:59:23  sueh
* <p> bug# 520 The const part of EtomoDouble.
* <p> </p>
*/
public abstract class ConstEtomoDouble extends EtomoSimpleType implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  protected static final double unsetValue = Double.NaN;
  
  protected double value = unsetValue;
  protected double defaultValue = unsetValue;
  protected double recommendedValue = unsetValue;
  protected double resetValue = unsetValue;
  protected double ceilingValue = unsetValue;
  
  public abstract void load(Properties props);  
  public abstract void load(Properties props, String prepend);
  
  protected ConstEtomoDouble() {
    super();
  }
  
  protected ConstEtomoDouble(String name) {
    super(name);
  }
  
  public EtomoSimpleType setDefault(double defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }
  
  public void setRecommendedValue(double recommendedValue) {
    this.recommendedValue = recommendedValue;
    setResetValue();
  }
  
  public void setDescription(String description) {
    if (description != null) {
      this.description = description;
    }
    else {
      name = description;
    }
  }
  
  private void setResetValue() {
    if (!Double.isNaN(recommendedValue)) {
      resetValue = recommendedValue;
    }
    else {
      resetValue = unsetValue;
    }
  }
  
  public void store(Properties props) {
    props.setProperty(name, Double.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Double.toString(value));
  }
  
  public void remove(Properties props, String prepend) {
    props.remove(prepend + "." + name);
  }
  
  public String getString() {
    return getString(false);
  }
  public String getString(boolean useDefault) {
    if (isSet()) {
      return Double.toString(value);
    }
    if (!Double.isNaN(resetValue)) {
      return Double.toString(resetValue);
    }
    if (useDefault && !Double.isNaN(defaultValue)) {
      return Double.toString(defaultValue);
    }
    return "";
  }
  
  public double get() {
    return get(false);
  }
  public double get(boolean useDefault) {
    if (isSet()) {
      return value;
    }
    if (!Double.isNaN(resetValue)) {
      return resetValue;
    }
    if (useDefault && !Double.isNaN(defaultValue)) {
      return defaultValue;
    }
    return unsetValue;
  }
  
  public Number getNumber() {
    return getNumber(false);
  }
  public Number getNumber(boolean useDefault) {
    if (isSet()) {
      return new Double(value);
    }
    if (!Double.isNaN(resetValue)) {
      return new Double(resetValue);
    }
    if (useDefault && !Double.isNaN(defaultValue)) {
      return new Double(defaultValue);
    }
    return new Double(unsetValue);
  }
  
  public EtomoSimpleType getNegation() {
    EtomoDouble that = new EtomoDouble();
    that.set(this);
    if (that.isSet()) {
      that.value *= -1;
    }
    if (!Double.isNaN(that.recommendedValue)) {
      that.recommendedValue *= -1;
    }
    if (!Double.isNaN(that.defaultValue)) {
      that.defaultValue *= -1;
    }
    return that;
  }
  
  public boolean isSetAndNotDefault() {
    return isSet() && (Double.isNaN(defaultValue) || value != defaultValue);
  }
  
  public boolean isSet() {
    return !Double.isNaN(value);
  }
  
  public boolean equals(double value) {
    return (!isSet() && Double.isNaN(value)) || this.value == value;
  }
  
  protected String paramString() {
    return super.paramString() + ",\nunsetValue=" + unsetValue + ",\nvalue="
        + value + ",\ndefaultValue=" + defaultValue + ",\nrecommendedValue="
        + recommendedValue + ",\nresetValue=" + resetValue;
  }
}
