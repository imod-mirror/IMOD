package etomo.ui;

import etomo.type.EtomoBoolean2;

import java.util.Properties;

/**
 * <p>Description: Three state boolean setting (not set, on, and off).  NOT thread-safe.
 * .</p>
 * <p/>
 * <p>Copyright: Copyright 2014</p>
 * <p/>
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 *
 * @author $Author$
 * @version $Revision$
 */
public final class BooleanFieldSetting implements FieldSettingInterface {
  public static final String rcsid =
      "$Id$";

  private boolean set = false;
  private boolean value = false;

  public BooleanFieldSetting() {
  }

  public boolean isBoolean() {
    return true;
  }
  public boolean isText(){return false;}

  public BooleanFieldSetting getBooleanSetting() {
    return this;
  }

  public TextFieldSetting getTextSetting() {
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

  public void copy(final FieldSettingInterface input) {
    BooleanFieldSetting setting = null;
    if (input != null) {
      setting = input.getBooleanSetting();
    }
    if (setting != null) {
      set = setting.isSet();
      value = setting.isValue();
      return;
    }
    reset();
  }

  public boolean isSet() {
    return set;
  }

  public boolean isValue() {
    return value;
  }
}
