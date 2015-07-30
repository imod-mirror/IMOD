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
import etomo.type.EndingStep;
import etomo.type.EtomoAutodoc;
import etomo.type.StartingStep;
import etomo.type.Status;
import etomo.type.StatusChangeEvent;
import etomo.type.StatusChangeListener;

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
  static final int STEP_PAIRS = 3;
  private final JPanel pnlRoot = new JPanel();
  private final CheckBox cbEndingStep = new CheckBox("Stop after");
  private final ButtonGroup bgEndingStep = new ButtonGroup();
  private final RadioButton rbEndingStep[] = new RadioButton[STEP_PAIRS];
  private final CheckBox cbStartingStep = new CheckBox("Start from");
  private final ButtonGroup bgStartingStep = new ButtonGroup();
  private final RadioButton rbStartingStep[] = new RadioButton[STEP_PAIRS];
  private final CheckBox cbDoNotUseExistingAlignment = new CheckBox(
    "Recompute fine alignment on restart");

  private final BaseManager manager;
  private final AxisID axisID;

  private EndingStep earliestRunEndingStep = null;
  private BatchRunTomoStatus status = BatchRunTomoStatus.OPEN;

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
    // init
    cbDoNotUseExistingAlignment.setSelected(true);
    // panels
    JPanel pnlEndingStep = new JPanel();
    JPanel pnlStartingStep = new JPanel();
    JPanel pnlStep = new JPanel();
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(new EtchedBorder("Subset of steps to run").getBorder());
    pnlRoot.add(pnlStep);
    pnlRoot.add(cbDoNotUseExistingAlignment.getComponent());
    // Step
    pnlStep.setLayout(new BoxLayout(pnlStep, BoxLayout.X_AXIS));
    pnlStep.add(pnlEndingStep);
    pnlStep.add(pnlStartingStep);
    // EndingStep
    pnlEndingStep.setLayout(new BoxLayout(pnlEndingStep, BoxLayout.Y_AXIS));
    pnlEndingStep.add(cbEndingStep.getComponent());
    // StartingStep
    pnlStartingStep.setLayout(new BoxLayout(pnlStartingStep, BoxLayout.Y_AXIS));
    pnlStartingStep.add(cbStartingStep.getComponent());
    // Step
    EndingStep endingStep;
    StartingStep startingStep;
    for (int i = 0; i < STEP_PAIRS; i++) {
      endingStep = EndingStep.getInstance(i);
      if (endingStep != null) {
        // init
        rbEndingStep[i] =
          new RadioButton(endingStep.getLabel(), endingStep, bgEndingStep);
        if (endingStep.isDefault()) {
          rbEndingStep[i].setSelected(true);
        }
      }
      startingStep = StartingStep.getInstance(i);
      if (startingStep != null) {
        rbStartingStep[i] =
          new RadioButton(startingStep.getLabel(), startingStep, bgStartingStep);
        if (startingStep.isDefault()) {
          rbStartingStep[i].setSelected(true);
        }
        // EndingStep
        pnlEndingStep.add(rbEndingStep[i].getComponent());
        // StartingStep
        pnlStartingStep.add(rbStartingStep[i].getComponent());
      }
    }
    // align
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    // update
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

  Component getComponent() {
    return pnlRoot;
  }

  public void actionPerformed(ActionEvent e) {
    updateDisplay();
  }

  public void statusChanged(final Status status) {
    if (status instanceof BatchRunTomoStatus) {
      this.status = (BatchRunTomoStatus) status;
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
      cbDoNotUseExistingAlignment.setEditable(editable);
    }
    else if (status == null || status instanceof EndingStep) {
      earliestRunEndingStep = (EndingStep) status;
      updateDisplay();
    }
  }

  public void statusChanged(final StatusChangeEvent statusChangeEvent) {
    // No response to dataset-level events
  }

  private void updateDisplay() {
    // Don't update the display while it is ineditable during the run.
    if (!cbStartingStep.isEditable()) {
      return;
    }
    // Starting Step - dependent on defined stop point
    boolean startingStepSelectionChanged = false;
    RadioButton.RadioButtonModel startingStepModel = null;
    boolean enableStartingStep = cbStartingStep.isSelected();
    cbDoNotUseExistingAlignment.setEnabled(enableStartingStep);
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
      // earliestRunStep is the defined stop point
      if (earliestRunEndingStep != null) {
        maxEnabled = earliestRunEndingStep.getIndex() + 1;
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
            ((StartingStep) startingStepModel.getEnumeratedType()).getIndex() + 1;
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
    EndingStep endingStep = EndingStep.getInstance(param.getEndingStep());
    if (endingStep != null) {
      rbEndingStep[endingStep.getIndex()].setSelected(true);
    }
    else {
      cbEndingStep.setSelected(false);
    }
    StartingStep startingStep = StartingStep.getInstance(param.getStartingStep());
    if (startingStep != null) {
      rbStartingStep[startingStep.getIndex()].setSelected(true);
    }
    else {
      cbStartingStep.setSelected(false);
    }
    cbDoNotUseExistingAlignment.setSelected(!param.isUseExistingAlignment());
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
    param.setUseExistingAlignment(!cbDoNotUseExistingAlignment.isSelected());
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
    cbDoNotUseExistingAlignment
      .setToolTipText("Unchecking this checkbox causes this affect:  "
        + EtomoAutodoc.getTooltip(autodoc, BatchruntomoParam.USE_EXISTING_ALIGNMENT_TAG));
  }
}
