package etomo.storage;

/**
 * <p>Description: Interface for DirectiveFile and DirectiveFileCollection.</p>
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
public interface DirectiveFileInterface {

  public boolean contains(DirectiveDef directiveDef);

  public String getValue(DirectiveDef directiveDef);

  public String getValue(DirectiveDef directiveDef, int index);

  public String getValue(DirectiveDef directiveDef, boolean templateOnly);

  public boolean contains(DirectiveDef directiveDef, boolean templateOnly);

  public boolean isValue(DirectiveDef directiveDef, boolean templateOnly);

  public boolean isValue(DirectiveDef directiveDef);

  public void setDebug(boolean input);
}
