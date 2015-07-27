package etomo.type;

import java.util.ArrayList;

/**
* <p>Description: An array list with some state information:  a current index and another
* integer.</p>
* 
* <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
public final class CurrentArrayList<E> extends ArrayList<E> {
  private int currentIndex = -1;
  private int stateInt = -1;

  public CurrentArrayList() {
    super();
  }

  public CurrentArrayList(CurrentArrayList<E> input) {
    super(input);
    currentIndex = input.currentIndex;
    stateInt = input.stateInt;
  }

  public void clear() {
    super.clear();
  }

  /**
   * Sets current index to the size of the list minus one.
   */
  public void setCurrentIndexToLastElement() {
    if (isEmpty()) {
      currentIndex = -1;
    }
    else {
      // Treating null (possibly removed) elements as real elements.
      currentIndex = size() - 1;
    }
  }

  /**
   * Sets an index - will fix if it is not legal
   * @param input
   */
  public void setCurrentIndex(final int input) {
    if (isEmpty()) {
      currentIndex = -1;
    }
    else if (input < 0) {
      currentIndex = 0;
    }
    else if (input >= size()) {
      currentIndex = size() - 1;
    }
    else {
      currentIndex = input;
    }
  }

  public void setStateInt(final int input) {
    stateInt = input;
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  public int getStateInt() {
    return stateInt;
  }
}
