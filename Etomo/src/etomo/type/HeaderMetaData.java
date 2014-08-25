package etomo.type;

import java.util.HashSet;
import java.util.Iterator;
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
public class HeaderMetaData {
  public static final String rcsid = "$Id:$";

  private HashSet keys = null;

  /**
   * Get a button state out of the local Properties object and return it as a
   * boolean.  If the state is not available, return defaultState.
   * @param key
   * @param defaultState
   * @return
   */
  public final boolean getButtonState(final String key, final boolean defaultState,
      final Properties props, final String prepend) {
    if (key == null) {
      return defaultState;
    }
    if (keys == null) {
      keys = new HashSet();
    }
    keys.add(key);
    if (props == null) {
      return defaultState;
    }
    EtomoState buttonState = new EtomoState(key);
    buttonState.load(props, prepend);
    if (buttonState.isNull()) {
      return defaultState;
    }
    return buttonState.is();
  }

  /**
   * Store the values in localProperties in props.
   * @param props
   * @param prepend
   */
  void store(final Properties props, final String prepend,
      final Properties localProperties, final String localPrepend) {
    if (keys == null || localProperties == null) {
      // nothing to store
      return;
    }
    synchronized (this) {
      Iterator i = keys.iterator();
      String key = null;
      while (i.hasNext()) {
        key = (String) i.next();
        // get the value from the local property using the local prepend
        String state = localProperties.getProperty(localPrepend + '.' + key);
        if (state != null) {
          // store the value in props using the modified prepend parameter
          props.setProperty(prepend + '.' + key, state);
        }
      }
    }
  }
}
