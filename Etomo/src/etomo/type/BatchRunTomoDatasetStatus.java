package etomo.type;

/**
* <p>Description: The state of a dataset in the BatchRunTomo interface.  The status is
* passed by BatchRunTomoMonitor (in StatusChangeIndexedEvent).  BatchRunTomoRow responds
* to it.</p>
* 
* <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
public final class BatchRunTomoDatasetStatus implements Status {
  public static final BatchRunTomoDatasetStatus DONE = new BatchRunTomoDatasetStatus(
    "Done", false);
  public static final BatchRunTomoDatasetStatus FAILED = new BatchRunTomoDatasetStatus(
    "Failed", false);
  public static final BatchRunTomoDatasetStatus FAILING = new BatchRunTomoDatasetStatus(
    "Running", true);
  public static final BatchRunTomoDatasetStatus KILLED = new BatchRunTomoDatasetStatus(
    "Killed", false);
  public static final BatchRunTomoDatasetStatus RUNNING = new BatchRunTomoDatasetStatus(
    "Running", true);
  public static final BatchRunTomoDatasetStatus STARTING = new BatchRunTomoDatasetStatus(
    "Running", true);
  public static final BatchRunTomoDatasetStatus STOPPED = new BatchRunTomoDatasetStatus(
    "Stopped", false);

  private final String text;
  private final boolean active;

  private BatchRunTomoDatasetStatus(final String text, final boolean active) {
    this.text = text;
    this.active = active;
  }

  public static BatchRunTomoDatasetStatus getInstance(final String text) {
    if (text == null) {
      return null;
    }
    if (DONE.text.equals(text)) {
      return DONE;
    }
    if (FAILED.text.equals(text)) {
      return FAILED;
    }
    if (FAILING.text.equals(text)) {
      return FAILING;
    }
    if (KILLED.text.equals(text)) {
      return KILLED;
    }
    if (RUNNING.text.equals(text)) {
      return RUNNING;
    }
    if (STARTING.text.equals(text)) {
      return STARTING;
    }
    if (STOPPED.text.equals(text)) {
      return STOPPED;
    }
    return null;
  }

  public boolean isActive() {
    return active;
  }

  public String getText() {
    return text;
  }

  public String toString() {
    return text;
  }

  public boolean equals(final String input) {
    return text.equals(input);
  }
}
