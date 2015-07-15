package etomo.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.comscript.BatchruntomoParam;
import etomo.storage.LogFile;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.BatchRunTomoStatus;
import etomo.type.ConstEtomoNumber;
import etomo.type.EnumeratedType;
import etomo.type.EtomoAutodoc;
import etomo.type.EtomoNumber;
import etomo.type.Status;
import etomo.type.StatusChangeEvent;
import etomo.type.StatusChangeListener;
import etomo.type.StatusChanger;

/**
 * <p>Description: Batchruntomo StartingStep and EndingStep. </p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
final class BatchRunTomoStepPanel implements ActionListener, StatusChangeListener {
  private static final int STEP_PAIRS = 3;
  private final JPanel pnlRoot = new JPanel();
  private final CheckBox cbEndingStep = new CheckBox("Stop after");
  private final ButtonGroup bgEndingStep = new ButtonGroup();
  private final RadioButton rbEndingStep[] = new RadioButton[STEP_PAIRS];
  private final CheckBox cbStartingStep = new CheckBox("Start from");
  private final ButtonGroup bgStartingStep = new ButtonGroup();
  private final RadioButton rbStartingStep[] = new RadioButton[STEP_PAIRS];

  private final BaseManager manager;
  private final AxisID axisID;

  private final Step earliestEndingStep = null;
  private Status status = null;

  private BatchRunTomoStepPanel(final BaseManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
  }

  static BatchRunTomoStepPanel
    getInstance(final BaseManager manager, final AxisID axisID) {
    BatchRunTomoStepPanel instance = new BatchRunTomoStepPanel(manager, axisID);
    instance.createPanel();
    instance.setTooltips();
    instance.addListeners();
    return instance;
  }

  private void createPanel() {
    // panels
    JPanel pnlEndingStep = new JPanel();
    JPanel pnlStartingStep = new JPanel();
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.X_AXIS));
    pnlRoot.setBorder(new EtchedBorder("Subset of steps to run").getBorder());
    pnlRoot.add(pnlEndingStep);
    pnlRoot.add(pnlStartingStep);
    // EndingStep
    pnlEndingStep.setLayout(new BoxLayout(pnlEndingStep, BoxLayout.Y_AXIS));
    pnlEndingStep.add(cbEndingStep);
    // StartingStep
    pnlStartingStep.setLayout(new BoxLayout(pnlStartingStep, BoxLayout.Y_AXIS));
    pnlStartingStep.add(cbStartingStep);
    // Step
    Step step;
    for (int i = 0; i < STEP_PAIRS; i++) {
      step = Step.getEndingInstance(i);
      if (step != null) {
        // init
        rbEndingStep[i] = new RadioButton(step.getLabel(), step, bgEndingStep);
        if (step.isDefaultEndingInstance()) {
          rbEndingStep[i].setSelected(true);
        }
        // EndingStep
        pnlEndingStep.add(rbEndingStep[i].getComponent());
      }
      step = Step.getStartingInstance(i);
      if (step != null) {
        // init
        rbStartingStep[i] = new RadioButton(step.getLabel(), step, bgStartingStep);
        if (step.isDefaultStartingInstance()) {
          rbStartingStep[i].setSelected(true);
        }
        // StartingStep
        pnlStartingStep.add(rbStartingStep[i].getComponent());
      }
    }
    updateDisplay();
  }

  private void addListeners() {
    cbEndingStep.addActionListener(this);
    cbStartingStep.addActionListener(this);
    for (int i = 0; i < STEP_PAIRS; i++) {
      rbEndingStep[i].addActionListener(this);
      rbStartingStep[i].addActionListener(this);
    }
  }

  void msgStatusChangerAvailable(final StatusChanger changer) {
    changer.addStatusChangeListener(this);
  }

  Component getComponent() {
    return pnlRoot;
  }

  public void actionPerformed(ActionEvent e) {
    updateDisplay();
  }

  public void statusChanged(final Status status) {
    this.status = status;
    boolean editable =
      status == null || status == BatchRunTomoStatus.OPEN
        || status == BatchRunTomoStatus.STOPPED;
    cbStartingStep.setEditable(editable);
    for (int i = 0; i < rbStartingStep.length; i++) {
      rbStartingStep[i].setEditable(editable);
    }
    cbEndingStep.setEditable(editable);
    for (int i = 0; i < rbStartingStep.length; i++) {
      rbEndingStep[i].setEditable(editable);
    }
  }

  public void statusChanged(final StatusChangeEvent statusChangeEvent) {
    //No response to dataset-level events
  }

  private void updateDisplay() {
    // Starting Step - dependent on defined stop point
    boolean startingStepSelectionChanged = false;
    RadioButton.RadioButtonModel startingStepModel = null;
    boolean enableStartingStep = cbStartingStep.isSelected();
    if (!enableStartingStep) {
      // Checkbox is not checked - yeay - just disable everything
      for (int i = 0; i < STEP_PAIRS; i++) {
        rbStartingStep[i].setEnabled(false);
      }
    }
    else {
      // Rule:
      // Disable past the defined stop point (paired with run to)
      // Don't jump ahead when restarting
      // defined stop point:
      // earliest completion status of all run checked datasets (disabled or enabled)
      int maxEnabled = STEP_PAIRS;
      // earliestEndingStep is the defined stop point
      if (earliestEndingStep != null && earliestEndingStep.isEndingStep()) {
        maxEnabled = earliestEndingStep.endingStepIndex + 1;
      }
      for (int i = 0; i < maxEnabled; i++) {
        rbStartingStep[i].setEnabled(true);
      }
      for (int i = maxEnabled; i < STEP_PAIRS; i++) {
        rbStartingStep[i].setEnabled(false);
      }
      if (maxEnabled < STEP_PAIRS && maxEnabled > 0) {
        // At least one radio button was disabled
        // Rule:
        // Move the selection to the first enabled one
        // Move it only if the selected button is disabled
        startingStepModel = (RadioButton.RadioButtonModel) bgStartingStep.getSelection();
        if (startingStepModel != null && !startingStepModel.isEnabled()) {
          // The selected radio button is now disabled - move it
          rbStartingStep[maxEnabled - 1].setSelected(true);
          startingStepSelectionChanged = true;
        }
      }
    }
    // Ending Step - dependent on Starting Step
    boolean enableEndingStep = cbEndingStep.isSelected();
    if (!enableEndingStep) {
      // Checkbox is not checked - yeay - just disable everything
      for (int i = 0; i < STEP_PAIRS; i++) {
        rbEndingStep[i].setEnabled(false);
      }
    }
    else {
      // Rule:
      // disable up to checked & enabled start from (can't go backwards)
      // End has to be at least one more then start
      int enabledStartIndex = 0;
      if (enableStartingStep) {
        // Get the starting step model if it has changed or wasn't already retrieved
        if (startingStepModel == null || startingStepSelectionChanged) {
          startingStepModel =
            (RadioButton.RadioButtonModel) bgStartingStep.getSelection();
        }
        if (startingStepModel != null && startingStepModel.isEnabled()) {
          enabledStartIndex =
            ((Step) startingStepModel.getEnumeratedType()).startingStepIndex + 1;
        }
      }
      for (int i = 0; i <= enabledStartIndex - 1; i++) {
        rbEndingStep[i].setEnabled(false);
      }
      for (int i = enabledStartIndex; i < STEP_PAIRS; i++) {
        rbEndingStep[i].setEnabled(true);
      }
      if (enabledStartIndex > 0 && enabledStartIndex < STEP_PAIRS) {
        // some radio buttons where disabled
        // Move the selection to the first enabled one
        // I think this should read "last enabled one"
        // Move it only if the selected button is disabled
        RadioButton.RadioButtonModel endingStepModel =
          (RadioButton.RadioButtonModel) bgEndingStep.getSelection();
        if (endingStepModel != null && !endingStepModel.isEnabled()) {
          // The selected radio button is now disabled - move it
          rbEndingStep[enabledStartIndex].setSelected(true);
        }
      }
    }
  }

  void setParameters(final BatchruntomoParam param) {
    Step step = Step.getInstance(param.getEndingStep());
    if (step != null && step.isEndingStep()) {
      rbEndingStep[step.endingStepIndex].setSelected(true);
    }
    else {
      cbEndingStep.setSelected(false);
    }
    step = Step.getInstance(param.getStartingStep());
    if (step != null && step.isStartingStep()) {
      rbStartingStep[step.startingStepIndex].setSelected(true);
    }
    else {
      cbStartingStep.setSelected(false);
    }
    updateDisplay();
  }

  void getParameters(final BatchruntomoParam param) {
    RadioButton.RadioButtonModel radioButtonModel;
    param.resetEndingStep();
    if (cbEndingStep.isEnabled() && cbEndingStep.isSelected()) {
      radioButtonModel = (RadioButton.RadioButtonModel) bgEndingStep.getSelection();
      if (radioButtonModel != null && radioButtonModel.isEnabled()) {
        param.setEndingStep(radioButtonModel.getEnumeratedType().getValue());
      }
    }
    param.resetStartingStep();
    if (cbStartingStep.isEnabled() && cbStartingStep.isSelected()) {
      radioButtonModel = (RadioButton.RadioButtonModel) bgStartingStep.getSelection();
      if (radioButtonModel != null && radioButtonModel.isEnabled()) {
        param.setStartingStep(radioButtonModel.getEnumeratedType().getValue());
      }
    }
  }

  private void setTooltips() {
    ReadOnlyAutodoc autodoc = null;
    try {
      autodoc =
        AutodocFactory.getInstance(manager, AutodocFactory.BATCH_RUN_TOMO, axisID, false);
    }
    catch (FileNotFoundException except) {
      except.printStackTrace();
    }
    catch (IOException except) {
      except.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    String endingStepTooltip =
      EtomoAutodoc.getTooltip(autodoc, BatchruntomoParam.ENDING_STEP_TAG);
    String startingStepToolip =
      EtomoAutodoc.getTooltip(autodoc, BatchruntomoParam.STARTING_STEP_TAG);
    cbEndingStep.setToolTipText(endingStepTooltip);
    cbStartingStep.setToolTipText(startingStepToolip);
    for (int i = 0; i < STEP_PAIRS; i++) {
      rbEndingStep[i].setToolTipText(endingStepTooltip);
      rbStartingStep[i].setToolTipText(startingStepToolip);
    }
  }

  private static final class Step implements EnumeratedType {
    private static final Step FINE_ALIGNMENT = new Step("6", "Fine alignment", 0, 0);
    private static final Step POSITIONING = new Step("7", "Positioning", 1, -1);
    private static final Step ALIGNED_STACK_GENERATION = new Step("8", "Aligned stack",
      -1, 1);
    private static final Step GOLD_DETECTION_3D = new Step("10", "Ctf/gold detection", 2,
      -1);
    private static final Step CTF_CORRECTION = new Step("11", "Ctf correction", -1, 2);

    private final EtomoNumber value;
    private final String label;
    private final int endingStepIndex, startingStepIndex;

    private Step(final String value, final String label, final int endingStepIndex,
      final int startingStepIndex) {
      this.label = label;
      this.endingStepIndex = endingStepIndex;
      this.startingStepIndex = startingStepIndex;
      if (value.indexOf('.') == -1) {
        this.value = new EtomoNumber();
      }
      else {
        this.value = new EtomoNumber(EtomoNumber.Type.DOUBLE);
      }
      this.value.set(value);
    }

    private static Step getInstance(final String value) {
      if (value == null) {
        return null;
      }
      if (FINE_ALIGNMENT.value.equals(value)) {
        return FINE_ALIGNMENT;
      }
      if (POSITIONING.value.equals(value)) {
        return POSITIONING;
      }
      if (ALIGNED_STACK_GENERATION.value.equals(value)) {
        return ALIGNED_STACK_GENERATION;
      }
      if (GOLD_DETECTION_3D.value.equals(value)) {
        return GOLD_DETECTION_3D;
      }
      if (CTF_CORRECTION.value.equals(value)) {
        return CTF_CORRECTION;
      }
      return null;
    }

    private static Step getEndingInstance(final int index) {
      if (index == -1) {
        return null;
      }
      if (FINE_ALIGNMENT.endingStepIndex == index) {
        return FINE_ALIGNMENT;
      }
      if (POSITIONING.endingStepIndex == index) {
        return POSITIONING;
      }
      if (ALIGNED_STACK_GENERATION.endingStepIndex == index) {
        return ALIGNED_STACK_GENERATION;
      }
      if (GOLD_DETECTION_3D.endingStepIndex == index) {
        return GOLD_DETECTION_3D;
      }
      if (CTF_CORRECTION.endingStepIndex == index) {
        return CTF_CORRECTION;
      }
      return null;
    }

    private static Step getStartingInstance(final int index) {
      if (index == -1) {
        return null;
      }
      if (FINE_ALIGNMENT.startingStepIndex == index) {
        return FINE_ALIGNMENT;
      }
      if (POSITIONING.startingStepIndex == index) {
        return POSITIONING;
      }
      if (ALIGNED_STACK_GENERATION.startingStepIndex == index) {
        return ALIGNED_STACK_GENERATION;
      }
      if (GOLD_DETECTION_3D.startingStepIndex == index) {
        return GOLD_DETECTION_3D;
      }
      if (CTF_CORRECTION.startingStepIndex == index) {
        return CTF_CORRECTION;
      }
      return null;
    }

    private boolean isDefaultEndingInstance() {
      return this == GOLD_DETECTION_3D;
    }

    private boolean isDefaultStartingInstance() {
      return this == CTF_CORRECTION;
    }

    private boolean isEndingStep() {
      return endingStepIndex != -1;
    }

    private boolean isStartingStep() {
      return startingStepIndex != -1;
    }

    public ConstEtomoNumber getValue() {
      return value;
    }

    public String toString() {
      return getLabel();
    }

    public String getLabel() {
      return label;
    }

    public boolean isDefault() {
      return false;
    }
  }
}
