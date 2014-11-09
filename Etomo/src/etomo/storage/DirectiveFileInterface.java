package etomo.storage;

/**
 * <p>Description: Interface for DirectiveFile and DirectiveFileCollection.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public interface DirectiveFileInterface {

  public boolean contains(DirectiveDef directiveDef);

  public String getValue(DirectiveDef directiveDef);

  public String getValue(DirectiveDef directiveDef, int index);

  public String getValue(DirectiveDef directiveDef, boolean templatesOnly);

  public boolean contains(DirectiveDef directiveDef, boolean templatesOnly);

  public boolean isValue(DirectiveDef directiveDef, boolean templatesOnly);

  public boolean isValue(DirectiveDef directiveDef);

  public void setDebug(boolean input);
}
