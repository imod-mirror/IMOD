package etomo.logic;

import etomo.storage.DirectiveDef;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAutodoc;

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
public final class DefaultFinder {
  public static final String rcsid = "$Id:$";

  public static final DefaultFinder INSTANCE = new DefaultFinder();

  public ReadOnlyAutodoc progDefaultsAutodoc = null;

  private DefaultFinder() {
  }

  public String getDefaultValue(final DirectiveDef directiveDef) {
    if (progDefaultsAutodoc==null) {
      progDefaultsAutodoc = AutodocFactory.getComInstance(AutodocFactory.PROG_DEFAULTS);
    }
    if (progDefaultsAutodoc.sectionExists("Program")) {
      
    }
    return null;
  }
}
