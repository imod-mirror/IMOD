package etomo.type;

/**
 * <p>Description: Instances correspond to the endingStep parameters in
 * batchruntomo.  An enumerated type for radio buttons.  Status: from
 * BatchRunTomoProcessMonitor to BatchRunTomoRow.</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class EndingStep implements EnumeratedType, Status {
  private static final EndingStep FINE_ALIGNMENT = new EndingStep(0, Step.FINE_ALIGNMENT);
  private static final EndingStep POSITIONING = new EndingStep(1, Step.POSITIONING);
  private static final EndingStep GOLD_DETECTION_3D = new EndingStep(2,
    Step.GOLD_DETECTION_3D);

  private final int index;
  private final Step step;

  private EndingStep(final int index, final Step step) {
    this.index = index;
    this.step = step;
  }

  public static EndingStep getInstance(final String stepValue) {
    return getInstance(Step.getInstance(stepValue));
  }

  public static EndingStep getInstanceFromText(final String stepText) {
    return getInstance(Step.getInstanceFromText(stepText));
  }

  public static EndingStep getInstance(final int index) {
    if (index == FINE_ALIGNMENT.index) {
      return FINE_ALIGNMENT;
    }
    if (index == POSITIONING.index) {
      return POSITIONING;
    }
    if (index == GOLD_DETECTION_3D.index) {
      return GOLD_DETECTION_3D;
    }
    return null;
  }

  private static EndingStep getInstance(final Step step) {
    if (step == FINE_ALIGNMENT.step) {
      return FINE_ALIGNMENT;
    }
    if (step == POSITIONING.step) {
      return POSITIONING;
    }
    if (step == GOLD_DETECTION_3D.step) {
      return GOLD_DETECTION_3D;
    }
    return null;
  }

  public boolean isDefault() {
    return this == GOLD_DETECTION_3D;
  }

  /**
   * @return true if this is the first ending step to be executed by batchruntomo.
   */
  public boolean isFirst() {
    return this == FINE_ALIGNMENT;
  }

  public String toString() {
    return step.toString();
  }

  public int getIndex() {
    return index;
  }

  public String getLabel() {
    return step.getLabel();
  }

  public ConstEtomoNumber getValue() {
    return step.getValue();
  }

  public String getText() {
    return step.getText();
  }

  public boolean le(final EndingStep input) {
    if (input == null) {
      return true;
    }
    return step.le(input.step);
  }

  public boolean lt(final EndingStep input) {
    if (input == null) {
      return true;
    }
    return step.lt(input.step);
  }
}
