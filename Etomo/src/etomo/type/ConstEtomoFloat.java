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
* <p> Revision 1.1.2.6  2004/10/30 02:31:40  sueh
* <p> bug# 520 Fixed getNegation so that it copies the entire class to a new
* <p> instances and negates all values.
* <p>
* <p> Revision 1.1.2.5  2004/10/29 22:11:04  sueh
* <p> bug# 520 Added remove() to remove value from the meta data file.
* <p>
* <p> Revision 1.1.2.4  2004/10/25 23:05:17  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p>
* <p> Revision 1.1.2.3  2004/10/22 21:00:28  sueh
* <p> bug# 520 Moved common code to EtomoSimpleType
* <p>
* <p> Revision 1.1.2.2  2004/10/22 03:21:48  sueh
* <p> bug# 520 added getNumber().
* <p>
* <p> Revision 1.1.2.1  2004/10/21 02:48:57  sueh
* <p> bug# Const object for EtomoFloat.
* <p> </p>
*/
public abstract class ConstEtomoFloat extends EtomoSimpleType implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  protected static final float unsetValue = Float.NaN;
  
  protected float value = unsetValue;
  protected float defaultValue = unsetValue;
  protected float recommendedValue = unsetValue;
  protected float resetValue = unsetValue;
  protected float ceilingValue = unsetValue;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  protected ConstEtomoFloat() {
    super();
  }

  protected ConstEtomoFloat(String name) {
    super(name);
  }
  
  public EtomoSimpleType setDefault(float defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }
  
  public void setRecommendedValue(float recommendedValue) {
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
    if (!Float.isNaN(recommendedValue)) {
      resetValue = recommendedValue;
    }
    else {
      resetValue = unsetValue;
    }
  }

  public void store(Properties props) {
    props.setProperty(name, Float.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Float.toString(value));
  }
  
  public void remove(Properties props, String prepend) {
    props.remove(prepend + "." + name);
  }
  
  public String getString() {
    return getString(false);
  }
  public String getString(boolean useDefault) {
    if (isSet()) {
      return Float.toString(value);
    }
    if (!Float.isNaN(resetValue)) {
      return Float.toString(resetValue);
    }
    if (useDefault && !Float.isNaN(defaultValue)) {
      return Float.toString(defaultValue);
    }
    return "";
  }
  
  public float get() {
    return get(false);
  }
  public float get(boolean useDefault) {
    if (isSet()) {
      return value;
    }
    if (!Float.isNaN(resetValue)) {
      return resetValue;
    }
    if (useDefault && !Float.isNaN(defaultValue)) {
      return defaultValue;
    }
    return unsetValue;
  }
  
  public  Number getNumber() {
    return getNumber(false);
  }
  public  Number getNumber(boolean useDefault) {
    if (isSet()) {
      return new Float(value);
    }
    if (!Float.isNaN(resetValue)) {
      return new Float(resetValue);
    }
    if (useDefault && !Float.isNaN(defaultValue)) {
      return new Float(defaultValue);
    }
    return new Float(unsetValue);
  }
  
  public EtomoSimpleType getNegation() {
    EtomoFloat that = new EtomoFloat();
    that.set(this);
    if (that.isSet()) {
      that.value *= -1;
    }
    if (!Float.isNaN(that.recommendedValue)) {
      that.recommendedValue *= -1;
    }
    if (!Float.isNaN(that.defaultValue)) {
      that.defaultValue *= -1;
    }
    return that;
  }
  
  public boolean isSetAndNotDefault() {
    return isSet() && (Float.isNaN(defaultValue) || value != defaultValue);
  }
  
  public boolean isSet() {
    return !Float.isNaN(value);
  }
  
  public boolean equals(float value) {
    return (!isSet() && Float.isNaN(value)) || this.value == value;
  }
  
  protected String paramString() {
    return super.paramString() + ",\nunsetValue=" + unsetValue + ",\nvalue="
        + value + ",\ndefaultValue=" + defaultValue + ",\nrecommendedValue="
        + recommendedValue + ",\nresetValue=" + resetValue;
  }

}
