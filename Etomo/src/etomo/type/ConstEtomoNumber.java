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
public abstract class ConstEtomoNumber implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  public static final int DOUBLE_TYPE = -1;
  public static final int FLOAT_TYPE = -2;
  public static final int INTEGER_TYPE = -3;
  public static final int LONG_TYPE = -4;
  
  private static final double doubleNullValue = Double.NaN;
  private static final float floatNullValue = Float.NaN;
  private static final int integerNullValue = Integer.MIN_VALUE;
  private static final long longNullValue = Long.MIN_VALUE; 
  
  protected int type;
  protected String name;
  protected String description = null;
  protected String invalidReason = null;
  protected Number value;
  protected Number defaultValue;
  protected Number recommendedValue;
  protected Number resetValue;
  protected Number ceilingValue;

  protected ConstEtomoNumber(int type) {
    this.type = type;
    name = super.toString();
    description = name;
    initialize();
  }
  
  protected ConstEtomoNumber(int type, String name) {
    this.type = type;
    this.name = name;
    description = name;
    initialize();
  }
  
  protected ConstEtomoNumber(int type, int initialValue) {
    this.type = type;
    name = super.toString();
    description = name;
    initialize(initialValue);
  }
  
  protected ConstEtomoNumber(ConstEtomoNumber that) {
    type = that.type;
    name = that.name;
    description = that.description;
    value = newNumber(that.value);
    defaultValue = newNumber(that.defaultValue);
    recommendedValue = newNumber(that.recommendedValue);
    resetValue = newNumber(that.resetValue);
    ceilingValue = newNumber(that.ceilingValue);
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isValid() {
    return invalidReason == null;
  }
  
  public String getInvalidReason() {
    return invalidReason;
  }
  
  public String classInfoString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  
  protected String paramString() {
    return ",\nname=" + name + ",\ndescription=" + description
        + ",\ninvalidReason=" + invalidReason + ",\nvalue="
        + value + ",\ndefaultValue=" + defaultValue + ",\nrecommendedValue="
        + recommendedValue + ",\nresetValue=" + resetValue;
  }
  
  public ConstEtomoNumber setCeiling(int ceilingValue) {
    this.ceilingValue = newNumber(ceilingValue);
    return this;
  }
  
  public ConstEtomoNumber setDefault(int defaultValue) {
    this.defaultValue = newNumber(defaultValue);
    return this;
  }
  
  public ConstEtomoNumber setDefault(String defaultValueString) {
    StringBuffer invalidBuffer = new StringBuffer();
    Number defaultValue = newNumber(defaultValueString, invalidBuffer);
    if (invalidBuffer.length() == 0 && !isNull(defaultValue)) {
      this.defaultValue = defaultValue;
    }
    return this;
  }
  
  public void setRecommendedValue(int recommendedValue) {
    this.recommendedValue = newNumber(recommendedValue);
    setResetValue();
  }
  
  public void setRecommendedValue(double recommendedValue) {
    this.recommendedValue = newNumber(recommendedValue);
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
  
  public void store(Properties props) {
    props.setProperty(name, value.toString());
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, value.toString());
  }
  
  public void remove(Properties props, String prepend) {
    props.remove(prepend + "." + name);
  }
  
  public String toString() {
    return toString(false);
  }
  public String toString(boolean useDefault) {
    if (!isNull()) {
      return value.toString();
    }
    if (!isNull(resetValue)) {
      return resetValue.toString();
    }
    if (useDefault && !isNull(defaultValue)) {
      return defaultValue.toString();
    }
    return "";
  }
  
  public int getInteger() {
    return getInteger(false);
  }
  
  public int getInteger(boolean useDefault) {
    if (!isNull()) {
      return value.intValue();
    }
    if (!isNull(resetValue)) {
      return resetValue.intValue();
    }
    if (useDefault && !isNull(defaultValue)) {
      return defaultValue.intValue();
    }
    return integerNullValue;
  }
  
  public long getLong() {
    if (!isNull()) {
      return value.longValue();
    }
    if (!isNull(resetValue)) {
      return resetValue.longValue();
    }
    return longNullValue;
  }
  
  public Number getNumber() {
    return getNumber(false);
  }
  public Number getNumber(boolean useDefault) {
    if (!isNull()) {
      return newNumber(value);
    }
    if (!isNull(resetValue)) {
      return newNumber(resetValue);
    }
    if (useDefault && !isNull(defaultValue)) {
      return newNumber(defaultValue);
    }
    return newNumber();
  }
  
  public ConstEtomoNumber getNegation() {
    EtomoNumber that = new EtomoNumber(this);
    that.value = newNumberMultiplied(value, -1);
    that.recommendedValue = newNumberMultiplied(recommendedValue, -1);
    that.defaultValue = newNumberMultiplied(defaultValue, -1);
    return that;
  }
  
  public boolean isSetAndNotDefault() {
    return !isNull() && (isNull(defaultValue) || !value.equals(defaultValue));
  }
  
  public boolean isNull() {
    return isNull(value);
  }
  
  public boolean equals(ConstEtomoNumber that) {
    return equals(value, that.value);
  }
  
  public boolean equals(int value) {
    return equals(this.value, value);
  }
  
  private void initialize() {
    value = newNumber();
    defaultValue = newNumber();
    recommendedValue = newNumber();
    resetValue = newNumber();
    ceilingValue = newNumber();
  }
  
  private void initialize(int initialValue) {
    value = newNumber(initialValue);
    defaultValue = newNumber();
    recommendedValue = newNumber();
    resetValue = newNumber();
    ceilingValue = newNumber();
  }
  
  private void setResetValue() {
    if (!isNull(recommendedValue)) {
      resetValue = recommendedValue;
    }
    else {
      resetValue = newNumber();
    }
  }
  
  protected Number newNumber() {
    switch (type) {
      case DOUBLE_TYPE:
        return new Double(doubleNullValue);
      case FLOAT_TYPE:
        return new Float(floatNullValue);
      case INTEGER_TYPE:
        return new Integer(integerNullValue);
      case LONG_TYPE:
        return new Long(longNullValue);
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  protected Number newNumber(Number value) {
    switch (type) {
      case DOUBLE_TYPE:
        return new Double(value.doubleValue());
      case FLOAT_TYPE:
        return new Float(value.floatValue());
      case INTEGER_TYPE:
        return new Integer(value.intValue());
      case LONG_TYPE:
        return new Long(value.longValue());
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  protected Number newNumber(String value, StringBuffer invalidBuffer) {
    try {
      switch (type) {
      case DOUBLE_TYPE:
        return new Double(value);
      case FLOAT_TYPE:
        return new Float(value);
      case INTEGER_TYPE:
        return new Integer(value);
      case LONG_TYPE:
        return new Long(value);
      default:
        throw new IllegalStateException("type=" + type);
      }
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
      invalidBuffer.append("Invalid value:  " + value);
      return newNumber();
    }
  }
  
  protected Number newNumber(int value) {
    switch (type) {
      case DOUBLE_TYPE:
        return new Double(new Integer(value).doubleValue());
      case FLOAT_TYPE:
        return new Float(new Integer(value).floatValue());
      case INTEGER_TYPE:
        return new Integer(value);
      case LONG_TYPE:
        return new Long(new Integer(value).longValue());
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  protected Number newNumber(double value) {
    switch (type) {
      case DOUBLE_TYPE:
        return new Double(value);
      case FLOAT_TYPE:
        return new Float(new Double(value).floatValue());
      case INTEGER_TYPE:
        return new Integer(new Double(value).intValue());
      case LONG_TYPE:
        return new Long(new Double(value).intValue());
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  private Number newNumberMultiplied(Number number, int multiple) {
    if (isNull(number)) {
      return number;
    }
    switch (type) {
    case DOUBLE_TYPE:
      return new Double(number.doubleValue() * multiple);
    case FLOAT_TYPE:
      return new Float(number.floatValue() * multiple);
    case INTEGER_TYPE:
      return new Integer(number.intValue() * multiple);
    case LONG_TYPE:
      return new Long(number.longValue() * multiple);
    default:
      throw new IllegalStateException("type=" + type);
    }
  }
  
  protected boolean isNull(Number value) {
    switch (type) {
      case DOUBLE_TYPE:
        return Double.isNaN(value.doubleValue());
      case FLOAT_TYPE:
        return Float.isNaN(value.floatValue());
      case INTEGER_TYPE:
        return value.intValue() == integerNullValue;
      case LONG_TYPE:
        return value.longValue() == longNullValue;
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  protected boolean gt(Number number, Number compValue) {
    switch (type) {
      case DOUBLE_TYPE:
        return number.doubleValue() > compValue.doubleValue();
      case FLOAT_TYPE:
        return number.floatValue() > compValue.floatValue();
      case INTEGER_TYPE:
        return number.intValue() > compValue.intValue();
      case LONG_TYPE:
        return number.longValue() > compValue.longValue();
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  protected boolean equals(Number number, Number compValue) {
    switch (type) {
      case DOUBLE_TYPE:
        return number.doubleValue() == compValue.doubleValue();
      case FLOAT_TYPE:
        return number.floatValue() == compValue.floatValue();
      case INTEGER_TYPE:
        return number.intValue() == compValue.intValue();
      case LONG_TYPE:
        return number.longValue() == compValue.longValue();
      default:
        throw new IllegalStateException("type=" + type);
    }
  }
  
  protected boolean equals(Number number, int compValue) {
    switch (type) {
      case DOUBLE_TYPE:
        return number.doubleValue() == compValue;
      case FLOAT_TYPE:
        return number.floatValue() == compValue;
      case INTEGER_TYPE:
        return number.intValue() == compValue;
      case LONG_TYPE:
        return number.longValue() == compValue;
      default:
        throw new IllegalStateException("type=" + type);
    }
  }

}
