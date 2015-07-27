package etomo.type;

/**
 * <p>Description: Most instances correspond to the startingStep and endingStep parameters in
 * batchruntomo.  An enumerated type for two sets of radio buttons.  Can also be used as a
 * status (see StatusChangeListener).</p>
 * <p>This status is passed by BatchRunTomoMonitor (in StatusChangeTaggedEvent).
 * BatchRunTomoRow responds to StatusChangeTaggedEvent</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchRunTomoCommand implements Status {
  public static final BatchRunTomoCommand DELIVERED = new BatchRunTomoCommand();
  public static final BatchRunTomoCommand RENAMED = new BatchRunTomoCommand();

  public String getText() {
    return null;
  }
}
