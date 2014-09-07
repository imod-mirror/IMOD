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
 @version $Revision:$
* 
* <p> $Log$ </p>
*/
public final class BooleanFieldSetting implements FieldSettingInterface {
  public final String rcsid = "$Id:$";

  private boolean set = false;
  private boolean value = false;

  public BooleanFieldSetting() {
  }
  
  BooleanFieldSetting getBooleanSetting() {
    return this;
  }
  
  TextFieldSetting getTextSetting() {
    return null;
  }

  public boolean equals(final boolean input) {
    return set && value == input;
  }

  public void set(final boolean input) {
    set = true;
    value = input;
  }

  public void reset() {
    set = false;
    value = false;
  }

  /**
   * Does not copy the next link.
   * @param input
   */
  public void copy(final BooleanFieldSetting input) {
    set = input.set;
    value = input.value;
  }

  public boolean isSet() {
    return set;
  }

  public boolean isValue() {
    return value;
  }

  public boolean isBoolean() {
    return true;
  }
}
