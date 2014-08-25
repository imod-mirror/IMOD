package etomo.type;

import java.util.Properties;

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
public final class BatchRunTomoDatasetMetaData implements HeaderMetaData {
  public static final String rcsid = "$Id:$";

  private static final String GROUP_KEY = "dataset";

  private final String stackID;

  BatchRunTomoDatasetMetaData(final String stackID) {
    this.stackID = stackID;
  }

  private String getGroupKey() {
    return GROUP_KEY + "." + stackID;
  }

  private String createPrepend(String prepend) {
    if (prepend == null || prepend.matches("\\s*")) {
      return getGroupKey();
    }
    prepend = prepend.trim();
    if (prepend.endsWith(".")) {
      return prepend + getGroupKey();
    }
    return prepend + "." + getGroupKey();
  }

  public void load(final Properties props, String prepend) {
    // reset
    // load
    prepend = createPrepend(prepend);
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
  }

}
