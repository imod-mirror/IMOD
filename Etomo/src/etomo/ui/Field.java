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
public interface Field {
  public static final String rcsid = "$Id:$";

  public boolean isDifferentFromCheckpoint(boolean alwaysCheck);

  public void backup();

  public void useDefaultValue();

  public void restoreFromBackup();

  public void checkpoint();

  public void clear();

  public void copy(Field copyFrom);

  public boolean isSelected();

  public String getText();
}
