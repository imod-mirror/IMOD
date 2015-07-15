package etomo.type;

/**
 * <p>Description: Bundles status with an identifier.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class StatusChangeEvent {
  private final Status status;
  private final String identifier;

  public StatusChangeEvent(final Status status, final String identifier) {
    this.status = status;
    this.identifier = identifier;
  }

  public Status getStatus() {
    return status;
  }

  public String getIdentifier() {
    return identifier;
  }
}
