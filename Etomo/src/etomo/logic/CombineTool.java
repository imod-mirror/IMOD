package etomo.logic;

import etomo.ApplicationManager;
import etomo.type.AxisID;
import etomo.type.FileType;
import etomo.type.MetaData;
import etomo.util.Imodinfo;

/**
 * <p>Description: Logic for tomogram combination</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public class CombineTool {
  public static boolean
    getInitialVolumeMatchingInitValue(final ApplicationManager manager) {
    MetaData metaData = manager.getMetaData();
    return metaData.isFiducialessAlignment(AxisID.FIRST)
      || metaData.isFiducialessAlignment(AxisID.SECOND)
      || new Imodinfo(FileType.FIDUCIAL_MODEL).isPatchTracking(manager, AxisID.FIRST);
  }
}
