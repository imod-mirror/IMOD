package etomo.type;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
public final class BatchRunTomoStatus implements Status {
  public static final BatchRunTomoStatus OPEN = new BatchRunTomoStatus();
  public static final BatchRunTomoStatus RUNNING = new BatchRunTomoStatus();
  public static final BatchRunTomoStatus KILLED_PAUSED = new BatchRunTomoStatus();
  public static final BatchRunTomoStatus STOPPED = new BatchRunTomoStatus();

  private BatchRunTomoStatus() {}
}
