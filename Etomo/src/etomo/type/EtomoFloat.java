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
public class EtomoFloat implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  private static final float unsetValue = Float.NaN;
  
  private float value = unsetValue;
  private float defaultValue = unsetValue;
  private float recommendedValue = unsetValue;
  private float resetValue = unsetValue;
  
  private String name;
  private String description = null;
  private String invalidReason = null;
  
  EtomoFloat(String name) {
    this.name = name;
    description = name;
  }
  
  public void setDefaultValue(float value) {
    defaultValue = value;
    setResetValue();
  }
  
  public void setRecommendedValue(float value) {
    recommendedValue = value;
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
  
  /**
   * Stores value in props.  If value is unset, removes value from props as long
   * as value wa
   */
  public void store(Properties props) {
    props.setProperty(name, Double.toString(value));
  }
  
  public void store(Properties props, String prepend) {
    props.setProperty(prepend + "." + name, Float.toString(value));
  }
  
  public void load(Properties props) {
    value = Float.parseFloat(props.getProperty(name, Float
        .toString(resetValue)));
  }
  
  public void load(Properties props, String prepend) {
    value = Float.parseFloat(props.getProperty(prepend + "." + name, Float
        .toString(resetValue)));
  }
  
  public String set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        float parsedValue = Float.parseFloat(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is an double.";
      }
    }
    return invalidReason;
  }
  
  public String set(Float value) {
    this.value = value.floatValue();
    return null;
  }
  
  public String set(float value) {
    this.value = value;
    return null;
  }
  
  public void reset() {
    value = resetValue;
  }
  
  public void unset() {
    value = unsetValue;
  }
  
  private void setResetValue() {
    if (!Double.isNaN(recommendedValue)) {
      resetValue = recommendedValue;
    }
    else if (!Double.isNaN(defaultValue)) {
      resetValue = defaultValue;
    }
    else {
      resetValue = unsetValue;
    }
  }
  
  public String toString() {
    if (Double.isNaN(value)) {
      return "";
    }
    return Double.toString(value);
  }
  
  public float get() {
    if (Float.isNaN(value)) {
      return resetValue;
    }
    return value;
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isSet() {
    return !Double.isNaN(value);
  }
  
  public boolean isDefault() {
    return !Float.isNaN(defaultValue) && value == defaultValue;
  }
}
