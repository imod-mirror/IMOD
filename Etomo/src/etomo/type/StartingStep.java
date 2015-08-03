package etomo.type;

/**
 * <p>Description: Instances correspond to the endingStep parameters in
 * batchruntomo.  An enumerated type for radio buttons.</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class StartingStep implements EnumeratedType {
  private static final StartingStep FINE_ALIGNMENT = new StartingStep(0,
    Step.FINE_ALIGNMENT);
  private static final StartingStep ALIGNED_STACK_GENERATION = new StartingStep(1,
    Step.ALIGNED_STACK_GENERATION);
  private static final StartingStep CTF_CORRECTION = new StartingStep(2,
    Step.CTF_CORRECTION);

  private final int index;
  private final Step step;

  private StartingStep(final int index, final Step step) {
    this.index = index;
    this.step = step;
  }

  public static StartingStep getInstance(final String stepValue) {
    return getInstance(Step.getInstance(stepValue));
  }

  public static StartingStep getInstance(final int index) {
    if (index == FINE_ALIGNMENT.index) {
      return FINE_ALIGNMENT;
    }
    if (index == ALIGNED_STACK_GENERATION.index) {
      return ALIGNED_STACK_GENERATION;
    }
    if (index == CTF_CORRECTION.index) {
      return CTF_CORRECTION;
    }
    return null;
  }

  private static StartingStep getInstance(final Step step) {
    if (step == FINE_ALIGNMENT.step) {
      return FINE_ALIGNMENT;
    }
    if (step == ALIGNED_STACK_GENERATION.step) {
      return ALIGNED_STACK_GENERATION;
    }
    if (step == CTF_CORRECTION.step) {
      return CTF_CORRECTION;
    }
    return null;
  }

  public boolean isDefault() {
    return this == CTF_CORRECTION;
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
}
