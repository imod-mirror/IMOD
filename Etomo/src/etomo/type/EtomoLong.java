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
public class EtomoLong extends ConstEtomoLong {
  public static  final String  rcsid =  "$Id$";
  
  public EtomoLong(String name) {
    this.name = name;
    description = name;
  }
  
  public void setDefaultValue(long value) {
    defaultValue = value;
    setResetValue();
  }
  
  public void setRecommendedValue(long value) {
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
    value = Long.parseLong(props.getProperty(name, Long
        .toString(resetValue)));
  }
  public void load(Properties props, String prepend) {
    value = Long.parseLong(props.getProperty(prepend + "." + name, Long
        .toString(resetValue)));
  }
  
  public String set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        long parsedValue = Long.parseLong(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is an integer.";
      }
    }
    return invalidReason;
  }
  
  public String set(Long value) {
    this.value = value.intValue();
    return null;
  }
  
  public String set(long value) {
    System.out.println("value=" + value);
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
    if (recommendedValue != unsetValue) {
      resetValue = recommendedValue;
    }
    else if (defaultValue != unsetValue) {
      resetValue = defaultValue;
    }
    else {
      resetValue = unsetValue;
    }
  }
}
