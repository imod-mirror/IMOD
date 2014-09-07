package etomo.ui;

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
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public final class FieldSettingBundle implements FieldSettingInterface {
  public static final String rcsid = "$Id:$";
  
  private FieldSettingInterface boolSetting = null;
  private FieldSettingInterface textSetting = null;
  
  /**
   * Null parameter has no effect
   * @param setting
   */
  public void add(final FieldSettingInterface setting) {
    if (setting ==null) {
      return;
    }
    if (setting.isBoolean()) {
      boolSetting = setting;
    }
    else {
      textSetting = setting;
    }
  }
}
