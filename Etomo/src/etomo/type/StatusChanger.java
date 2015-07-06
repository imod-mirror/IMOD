package etomo.type;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public interface StatusChanger {
  public void addStatusChangeListener(StatusChangeListener listener);

  public void removeStatusChangeListener(StatusChangeListener listener);
}
