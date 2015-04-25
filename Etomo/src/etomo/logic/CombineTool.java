package etomo.logic;

import etomo.BaseManager;
import etomo.type.FileType;

/**
 * <p>Description: Logic for combine. </p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class CombineTool {
  public static boolean isInvertYLimits(final BaseManager manager) {
    return FileType.ORIG_COMS_DIR.exists(manager, null);
  }
}
