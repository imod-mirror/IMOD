package etomo.ui;

import etomo.type.ConstEtomoNumber;
import etomo.type.EtomoNumber;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2014</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
 @version $Revision$
* 
* <p> $Log$ </p>
*/
public final class FieldSetting  implements FieldSettingInterface{
  public final String rcsid = "$Id:$";

  private boolean set = false;
  private boolean bValue = false;
  private String sValue = null;
  private boolean bool = false;

  public FieldSetting() {
  }

  public boolean equals(final boolean input) {
    return set && bValue == input;
  }

  public boolean equals(final String input, final FieldType fieldType) {
    if (!set) {
      return false;
    }
    // Treating two nulls as equals
    if (sValue == null && input == null) {
      return true;
    }
    // One is null - not equals
    if (sValue == null || input == null) {
      return false;
    }
    // Ignore whitespace
    input = input.trim();
    // Strings are identical - equals
    if (sValue.equals(input)) {
      return true;
    }
    // Compare as a number if both are numbers
    if (EtomoNumber.isValid(sValue) && EtomoNumber.isValid(input)) {
      EtomoNumber.Type type = null;
      if (fieldType == FieldType.FLOATING_POINT) {
        type = EtomoNumber.Type.DOUBLE;
      }
      else if (fieldType == FieldType.INTEGER) {
        type = EtomoNumber.Type.LONG;
      }
      EtomoNumber nValue = new EtomoNumber(type);
      nValue.set(sValue);
      // Treating two nulls of the same type as equals
      if (nValue.isNull() && nValue.isNull(input)) {
        return true;
      }
      if (!nValue.isValid() && type == EtomoNumber.Type.LONG) {
        // User may have entered a number that does not match the field's type.
        nValue = new EtomoNumber(EtomoNumber.Type.DOUBLE);
        nValue.set(sValue);
        // Treating two nulls of the same type as equals
        if (nValue.isNull() && nValue.isNull(input)) {
          return true;
        }
      }
      // Do a numeric comparison
      return nValue.equals(input);
    }
    // Not numeric and not the same string - equals
    return false;
  }

  public boolean equals(final Number input, final FieldType fieldType) {
    if (!set) {
      return false;
    }
    // Treating two nulls as equals
    if (sValue == null && input == null) {
      return true;
    }
    // One is null - not equals
    if (sValue == null || input == null) {
      return false;
    }
    return equals(input.toString(), fieldType);
  }

  public void set(final boolean input) {
    set = true;
    bool = true;
    bValue = input;
    sValue = null;
  }

  /**
   * Sets checkoint (trims whitespace).
   * @param input
   */
  public void set(final String input) {
    set = true;
    bool = false;
    sValue = input;
    // Ignore whitespace
    if (sValue != null) {
      sValue = sValue.trim();
    }
    bValue = false;
  }

  public void set(final int input) {
    set(String.valueOf(input));
  }

  public void set(final double input) {
    set(String.valueOf(input));
  }

  public void set(final ConstEtomoNumber input) {
    bool = false;
    if (input == null) {
      set = true;
      sValue = null;
      bValue = false;
    }
    else {
      set(input.toString());
    }
  }

  public void reset() {
    set = false;
    bValue = false;
    sValue = null;
    bool = false;
    next = null;
  }

  /**
   * Does not copy the next link.
   * @param input
   */
  public void copy(final FieldSetting input) {
    set = input.set;
    bValue = input.bValue;
    sValue = input.sValue;
    bool = input.bool;
  }
  

  public boolean isSet() {
    return set;
  }

  public boolean isValue() {
    return bValue;
  }

  public String getValue() {
    return sValue;
  }
  
  public boolean isBoolean() {
    return bool;
  }
}
