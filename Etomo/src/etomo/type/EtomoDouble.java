package etomo.type;

import java.util.Properties;

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
public class EtomoDouble extends ConstEtomoDouble {
  public static  final String  rcsid =  "$Id$";
  
  EtomoDouble(String name) {
    this.name = name;
    description = name;
  }
  
  public void setDefaultValue(double value) {
    defaultValue = value;
    setResetValue();
  }
  
  public void setRecommendedValue(double value) {
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
  
  public void load(Properties props) {
    value = Double.parseDouble(props.getProperty(name, Double
        .toString(resetValue)));
  }
  
  public void load(Properties props, String prepend) {
    value = Double.parseDouble(props.getProperty(prepend + "." + name, Double
        .toString(resetValue)));
  }
  
  public String set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        double parsedValue = Double.parseDouble(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is an double.";
      }
    }
    return invalidReason;
  }
  
  public String set(Double value) {
    this.value = value.doubleValue();
    return null;
  }
  
  public String set(double value) {
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
  
}
