package etomo.type;


/**
 * <p>Description: Interface for eTomo-compatible plugins.  External plugins must have a
 * public default constructor.</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public interface Plugin {
  public String getTitle();

  public String getDescription();

  public String getKey();

  public PluginNiche getPluginNiche();

  public String getVersion();
}
