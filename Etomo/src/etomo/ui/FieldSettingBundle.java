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

  private BooleanFieldSetting boolSetting = null;
  private TextFieldSetting textSetting = null;

  public BooleanFieldSetting getBooleanSetting() {
    return boolSetting;
  }

  public TextFieldSetting getTextSetting() {
    return textSetting;
  }

  public void addBooleanSetting(final FieldSettingInterface input) {
    boolSetting = input.getBooleanSetting();
  }

  public void addTextSetting(final FieldSettingInterface input) {
    textSetting = input.getTextSetting();
  }
}
