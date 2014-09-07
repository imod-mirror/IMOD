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
public final class TextFieldSetting implements FieldSettingInterface {
  public final String rcsid = "$Id:$";

  private boolean set = false;
  private String value = null;

  public TextFieldSetting() {
  }

  public boolean equals(final String input, final FieldType fieldType) {
    if (!set) {
      return false;
    }
    // Treating two nulls as equals
    if (value == null && input == null) {
      return true;
    }
    // One is null - not equals
    if (value == null || input == null) {
      return false;
    }
    // Ignore whitespace
    input = input.trim();
    // Strings are identical - equals
    if (value.equals(input)) {
      return true;
    }
    // Compare as a number if both are numbers
    if (EtomoNumber.isValid(value) && EtomoNumber.isValid(input)) {
      EtomoNumber.Type type = null;
      if (fieldType == FieldType.FLOATING_POINT) {
        type = EtomoNumber.Type.DOUBLE;
      }
      else if (fieldType == FieldType.INTEGER) {
        type = EtomoNumber.Type.LONG;
      }
      EtomoNumber nValue = new EtomoNumber(type);
      nValue.set(value);
      // Treating two nulls of the same type as equals
      if (nValue.isNull() && nValue.isNull(input)) {
        return true;
      }
      if (!nValue.isValid() && type == EtomoNumber.Type.LONG) {
        // User may have entered a number that does not match the field's type.
        nValue = new EtomoNumber(EtomoNumber.Type.DOUBLE);
        nValue.set(value);
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
    if (value == null && input == null) {
      return true;
    }
    // One is null - not equals
    if (value == null || input == null) {
      return false;
    }
    return equals(input.toString(), fieldType);
  }

  /**
   * Sets checkoint (trims whitespace).
   * @param input
   */
  public void set(final String input) {
    set = true;
    value = input;
    // Ignore whitespace
    if (value != null) {
      value = value.trim();
    }
  }

  public void set(final int input) {
    set(String.valueOf(input));
  }

  public void set(final double input) {
    set(String.valueOf(input));
  }

  public void set(final ConstEtomoNumber input) {
    if (input == null) {
      set = true;
      value = null;
    }
    else {
      set(input.toString());
    }
  }

  public void reset() {
    set = false;
    value = null;
  }

  /**
   * Does not copy the next link.
   * @param input
   */
  public void copy(final TextFieldSetting input) {
    set = input.set;
    value = input.value;
  }

  public boolean isSet() {
    return set;
  }

  public String getValue() {
    return value;
  }

  public boolean isBoolean() {
    return false;
  }
}
