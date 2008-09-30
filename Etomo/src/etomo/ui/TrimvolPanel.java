package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;

import etomo.ApplicationManager;
import etomo.comscript.TrimvolParam;
import etomo.process.ImodManager;
import etomo.process.ImodProcess;
import etomo.type.AxisID;
import etomo.type.DialogType;
import etomo.type.InvalidEtomoNumberException;
import etomo.type.ReconScreenState;
import etomo.type.Run3dmodMenuOptions;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2002</p>
 * 
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 3.30  2008/07/15 21:23:58  sueh
 * <p> bug# 1127 Placing the rubberband button for scaling in the trimvol panel
 * <p> instead of the rubberband panel because it includes Z, which is in a radio
 * <p> button in the trimvol panel.
 * <p>
 * <p> Revision 3.29  2008/05/28 02:52:12  sueh
 * <p> bug# 1111 Add a dialogType parameter to the ProcessSeries
 * <p> constructor.  DialogType must be passed to any function that constructs
 * <p> a ProcessSeries instance.
 * <p>
 * <p> Revision 3.28  2008/05/13 23:09:14  sueh
 * <p> bug# 847 Adding a right click menu for deferred 3dmods to some
 * <p> process buttons.
 * <p>
 * <p> Revision 3.27  2008/05/03 00:57:57  sueh
 * <p> bug# 847 Passing null for ProcessSeries to process funtions.
 * <p>
 * <p> Revision 3.26  2008/02/28 21:19:46  sueh
 * <p> bug# 1085 Added setting Z to setXYMinAndMax.  Implemented
 * <p> RubberbandContainer.
 * <p>
 * <p> Revision 3.25  2007/11/06 20:33:08  sueh
 * <p> bug# 1047 Generalize TripvolPanel.
 * <p>
 * <p> Revision 3.24  2007/08/08 15:08:27  sueh
 * <p> bug# 834 Sharing fields labels.
 * <p>
 * <p> Revision 3.23  2007/03/07 21:16:57  sueh
 * <p> bug# 981 Turned RadioButton into a wrapper rather then a child of JRadioButton,
 * <p> because it is getting more complicated.
 * <p>
 * <p> Revision 3.22  2007/02/09 00:55:11  sueh
 * <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 * <p> classes.
 * <p>
 * <p> Revision 3.21  2006/10/20 21:48:13  sueh
 * <p> bug# 946  Adding a warning to the reorientation box.
 * <p>
 * <p> Revision 3.20  2006/08/16 22:42:26  sueh
 * <p> bug# 912 Rubberband panel border label is no longer optional.
 * <p>
 * <p> Revision 3.19  2006/08/16 18:52:26  sueh
 * <p> bug# 912 Making the rubberband panel generic.
 * <p>
 * <p> Revision 3.18  2006/08/14 18:34:35  sueh
 * <p> bug#  890 Validating section scale min and max, and fixed scale min and max.
 * <p>
 * <p> Revision 3.17  2006/07/21 19:19:32  sueh
 * <p> bug# 848 Moved dimensions that have to be adjusted for font size from
 * <p> FixedDim to UIParameters.
 * <p>
 * <p> Revision 3.16  2006/06/28 23:29:59  sueh
 * <p> bug# 881 Added pnlScaleRubberband.
 * <p>
 * <p> Revision 3.15  2006/06/27 23:47:59  sueh
 * <p> bug# 879 Placed swapYZ and rotateX into a labeled panel.
 * <p>
 * <p> Revision 3.14  2006/06/27 17:55:37  sueh
 * <p> bug# 897 Simplifying rotate x label.
 * <p>
 * <p> Revision 3.13  2006/06/27 17:49:38  sueh
 * <p> bug# 879 Make the swap yz check box a radio button.  Added a rotate in x radio
 * <p> button and a do not change radio button.
 * <p>
 * <p> Revision 3.12  2006/01/31 21:01:36  sueh
 * <p> bug# 521 Managing the trimvol button in ProcessResultDisplayFactory.
 * <p> Made trimvol a toggle button.
 * <p>
 * <p> Revision 3.11  2006/01/04 00:05:47  sueh
 * <p> bug# 675 Converted JCheckBox's to CheckBox.  Converted
 * <p> JRadioButton's to RadioButton.
 * <p>
 * <p> Revision 3.10  2005/11/14 22:35:38  sueh
 * <p> bug# 762 Made scaleAction().
 * <p>
 * <p> Revision 3.9  2005/08/12 00:01:38  sueh
 * <p> bug# 711  Change enum Run3dmodMenuOption to
 * <p> Run3dmodMenuOptions, which can turn on multiple options at once.
 * <p> This allows ImodState to combine input from the context menu and the
 * <p> pulldown menu.  Prevent context menu from popping up when button is
 * <p> disabled.  Get rid of duplicate code by running the 3dmods from a private
 * <p> function called run3dmod(String, Run3dmodMenuOptions).  It can be
 * <p> called from run3dmod(Run3dmodButton, Run3dmodMenuOptions) and the
 * <p> action function.
 * <p>
 * <p> Revision 3.8  2005/08/10 20:48:56  sueh
 * <p> bug# 711 Moved button sizing to MultiLineButton.  SetSize() sets the
 * <p> standard button size.
 * <p>
 * <p> Revision 3.7  2005/08/09 21:11:46  sueh
 * <p> bug# 711  Implemented Run3dmodButtonContainer:  added run3dmod().
 * <p> Changed 3dmod buttons to Run3dmodButton.  No longer inheriting
 * <p> MultiLineButton from JButton.
 * <p>
 * <p> Revision 3.6  2005/04/25 21:41:51  sueh
 * <p> bug# 615 Passing the axis where a command originates to the message
 * <p> functions so that the message will be popped up in the correct window.
 * <p> This requires adding AxisID to many objects.
 * <p>
 * <p> Revision 3.5  2005/03/24 17:55:44  sueh
 * <p> bug# 621 Set a preferred text width for fields that where too small.
 * <p>
 * <p> Revision 3.4  2004/05/13 20:13:51  sueh
 * <p> bug# 33 change setXYMinAndMax() so it can ignore non rubberband data
 * <p>
 * <p> Revision 3.3  2004/05/07 19:53:23  sueh
 * <p> bug# 33 getting coordinates info in the right order, getting only
 * <p> the correct kind of data
 * <p>
 * <p> Revision 3.2  2004/05/06 20:25:16  sueh
 * <p> bug# 33 added getCoordinates button, moved fullvol button to the top
 * <p> of the dialog, added setXYMinAndMax() to set field values
 * <p>
 * <p> Revision 3.1  2004/01/30 22:45:34  sueh
 * <p> bug# 356 Changing buttons with html labels to
 * <p> MultiLineButton and MultiLineToggleButton
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 1.12  2003/10/29 20:49:11  rickg
 * <p> Bug# 308 Tooltips
 * <p>
 * <p> Revision 1.11  2003/10/20 23:25:41  rickg
 * <p> Bug# 253 Added convert to bytes checkbox
 * <p>
 * <p> Revision 1.10  2003/10/16 17:05:10  rickg
 * <p> Bug# 305 Label changes, backup file filter
 * <p>
 * <p> Revision 1.9  2003/09/08 05:48:16  rickg
 * <p> Method name change for opening the complete volume
 * <p>
 * <p> Revision 1.8  2003/04/28 23:25:25  rickg
 * <p> Changed visible imod references to 3dmod
 * <p>
 * <p> Revision 1.7  2003/04/17 23:08:38  rickg
 * <p> Initial revision
 * <p>
 * <p> Revision 1.6  2003/04/16 22:18:17  rickg
 * <p> Trimvol in progress
 * <p>
 * <p> Revision 1.5  2003/04/16 00:14:40  rickg
 * <p> Trimvol in progress
 * <p>
 * <p> Revision 1.4  2003/04/14 23:57:44  rickg
 * <p> In progress
 * <p>
 * <p> Revision 1.3  2003/04/14 04:31:31  rickg
 * <p> In progres
 * <p>
 * <p> Revision 1.2  2003/04/10 23:42:51  rickg
 * <p> In progress
 * <p>
 * <p> Revision 1.1  2003/04/09 23:37:20  rickg
 * <p> In progress
 * <p> </p>
 */

public final class TrimvolPanel implements Run3dmodButtonContainer,
    RubberbandContainer {
  public static final String rcsid = "$Id$";

  private static final String SCALING_ERROR_TITLE = "Scaling Panel Error";
  private static final String FIXED_SCALE_MIN_LABEL = "black: ";
  private static final String FIXED_SCALE_MAX_LABEL = " white: ";
  private static final String SECTION_SCALE_MIN_LABEL = "Z min: ";
  private static final String SECTION_SCALE_MAX_LABEL = " Z max: ";
  static final String SWAP_YZ_LABEL = "Swap Y and Z dimensions";
  static final String REORIENTATION_GROUP_LABEL = "Reorientation:";

  private ApplicationManager applicationManager;

  private JPanel pnlTrimvol = new JPanel();

  private JPanel pnlRange = new JPanel();
  private LabeledTextField ltfXMin = new LabeledTextField("X min: ");
  private LabeledTextField ltfXMax = new LabeledTextField(" X max: ");
  private LabeledTextField ltfYMin = new LabeledTextField("Y min: ");
  private LabeledTextField ltfYMax = new LabeledTextField(" Y max: ");
  private LabeledTextField ltfZMin = new LabeledTextField("Z min: ");
  private LabeledTextField ltfZMax = new LabeledTextField(" Z max: ");

  private JPanel pnlScale = new JPanel();
  private JPanel pnlScaleFixed = new JPanel();
  private CheckBox cbConvertToBytes = new CheckBox("Convert to bytes");
  private RadioButton rbScaleFixed = new RadioButton(
      "Scale to match contrast  ");
  private LabeledTextField ltfFixedScaleMin = new LabeledTextField(
      FIXED_SCALE_MIN_LABEL);
  private LabeledTextField ltfFixedScaleMax = new LabeledTextField(
      FIXED_SCALE_MAX_LABEL);

  private RadioButton rbScaleSection = new RadioButton(
      "Find scaling from sections  ");
  private JPanel pnlScaleSection = new JPanel();
  private LabeledTextField ltfSectionScaleMin = new LabeledTextField(
      SECTION_SCALE_MIN_LABEL);
  private LabeledTextField ltfSectionScaleMax = new LabeledTextField(
      SECTION_SCALE_MAX_LABEL);

  private final JPanel pnlReorientationChoices = new JPanel();
  private final ButtonGroup bgReorientation = new ButtonGroup();
  private final RadioButton rbNone = new RadioButton("None");
  private final RadioButton rbSwapYZ = new RadioButton(SWAP_YZ_LABEL);
  private final RadioButton rbRotateX = new RadioButton("Rotate around X axis");
  private final JLabel lWarning1 = new JLabel("Warning:");
  private final JLabel lWarning2 = new JLabel("For serial joins, use");
  private final JLabel lWarning3 = new JLabel("the same reorientation");
  private final JLabel lWarning4 = new JLabel("method for each");
  private final JLabel lWarning5 = new JLabel("section.");

  private JPanel pnlButton = new JPanel();
  private Run3dmodButton btnImodFull = Run3dmodButton.get3dmodInstance(
      "3dmod Full Volume", this);
  private final Run3dmodButton btnTrimvol;
  private Run3dmodButton btnImodTrim = Run3dmodButton.get3dmodInstance(
      "3dmod Trimmed Volume", this);
  private MultiLineButton btnGetCoordinates = new MultiLineButton(
      "Get XYZ Volume Range From 3dmod");
  private JPanel pnlImodFull = new JPanel();
  private final ButtonListener buttonActonListener;
  private final RubberbandPanel pnlScaleRubberband;
  private final AxisID axisID;
  private final DialogType dialogType;

  /**
   * Default constructor
   */
  public TrimvolPanel(ApplicationManager appMgr, AxisID axisID,
      DialogType dialogType) {
    this.dialogType = dialogType;
    this.axisID = axisID;
    applicationManager = appMgr;
    pnlScaleRubberband = RubberbandPanel
        .getNoButtonInstance(
            appMgr,
            this,
            ImodManager.COMBINED_TOMOGRAM_KEY,
            "Scaling from sub-area:",
            "Get XYZ Sub-Area From 3dmod",
            "Minimum X coordinate on the left side to analyze for contrast range.",
            "Maximum X coordinate on the right side to analyze for contrast range.",
            "The lower Y coordinate to analyze for contrast range.",
            "The upper Y coordinate to analyze for contrast range.");
    btnTrimvol = (Run3dmodButton) appMgr.getProcessResultDisplayFactory(
        AxisID.ONLY).getTrimVolume();
    btnTrimvol.setContainer(this);
    btnTrimvol.setDeferred3dmodButton(btnImodTrim);

    //  Set the button sizes
    btnImodFull.setSize();
    btnTrimvol.setSize();
    btnImodTrim.setSize();
    btnGetCoordinates.setSize();

    //  Layout the range panel
    pnlRange.setLayout(new GridLayout(3, 2));
    pnlRange.setBorder(new EtchedBorder("Volume Range").getBorder());

    pnlRange.add(ltfXMin.getContainer());
    pnlRange.add(ltfXMax.getContainer());
    pnlRange.add(ltfYMin.getContainer());
    pnlRange.add(ltfYMax.getContainer());
    pnlRange.add(ltfZMin.getContainer());
    pnlRange.add(ltfZMax.getContainer());

    //  Layout the scale panel
    pnlScaleFixed.setLayout(new BoxLayout(pnlScaleFixed, BoxLayout.X_AXIS));

    pnlScaleFixed.add(rbScaleFixed.getComponent());
    pnlScaleFixed.add(ltfFixedScaleMin.getContainer());
    pnlScaleFixed.add(ltfFixedScaleMax.getContainer());

    pnlScaleSection.setLayout(new BoxLayout(pnlScaleSection, BoxLayout.X_AXIS));
    pnlScaleSection.add(rbScaleSection.getComponent());
    ltfSectionScaleMin.setTextPreferredWidth(UIParameters.INSTANCE
        .getFourDigitWidth());
    ltfSectionScaleMax.setTextPreferredWidth(UIParameters.INSTANCE
        .getFourDigitWidth());
    pnlScaleSection.add(ltfSectionScaleMin.getContainer());
    pnlScaleSection.add(ltfSectionScaleMax.getContainer());

    ButtonGroup bgScale = new ButtonGroup();
    bgScale.add(rbScaleFixed.getAbstractButton());
    bgScale.add(rbScaleSection.getAbstractButton());

    pnlScale.setLayout(new BoxLayout(pnlScale, BoxLayout.Y_AXIS));
    pnlScale.setBorder(new EtchedBorder("Scaling").getBorder());

    cbConvertToBytes.setAlignmentX(Component.RIGHT_ALIGNMENT);
    pnlScale.add(cbConvertToBytes);
    pnlScale.add(pnlScaleFixed);
    pnlScale.add(pnlScaleSection);
    pnlScale.add(pnlScaleRubberband.getComponent());
    pnlScale.add(pnlScaleRubberband.getRubberbandButtonComponent());

    pnlButton.setLayout(new BoxLayout(pnlButton, BoxLayout.X_AXIS));
    pnlButton.add(Box.createHorizontalGlue());
    pnlButton.add(btnTrimvol.getComponent());
    pnlButton.add(Box.createHorizontalGlue());
    pnlButton.add(btnImodTrim.getComponent());
    pnlButton.add(Box.createHorizontalGlue());

    pnlTrimvol.setLayout(new BoxLayout(pnlTrimvol, BoxLayout.Y_AXIS));
    pnlTrimvol.setBorder(new BeveledBorder("Volume Trimming").getBorder());

    pnlImodFull.setLayout(new BoxLayout(pnlImodFull, BoxLayout.X_AXIS));
    pnlImodFull.add(Box.createHorizontalGlue());
    pnlImodFull.add(btnImodFull.getComponent());
    pnlImodFull.add(Box.createHorizontalGlue());
    pnlImodFull.add(btnGetCoordinates.getComponent());
    pnlImodFull.add(Box.createHorizontalGlue());
    pnlTrimvol.add(pnlImodFull);
    pnlTrimvol.add(pnlRange);
    pnlTrimvol.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlTrimvol.add(pnlScale);
    pnlTrimvol.add(Box.createRigidArea(FixedDim.x0_y10));
    SpacedPanel pnlReorientation = SpacedPanel.getInstance();
    pnlReorientation.setBoxLayout(BoxLayout.X_AXIS);
    pnlReorientationChoices.setLayout(new BoxLayout(pnlReorientationChoices,
        BoxLayout.Y_AXIS));
    pnlReorientationChoices.setBorder(new EtchedBorder(
        REORIENTATION_GROUP_LABEL).getBorder());
    pnlReorientationChoices.setAlignmentX(Component.RIGHT_ALIGNMENT);
    bgReorientation.add(rbNone.getAbstractButton());
    bgReorientation.add(rbSwapYZ.getAbstractButton());
    bgReorientation.add(rbRotateX.getAbstractButton());
    rbNone.setAlignmentX(Component.LEFT_ALIGNMENT);
    rbSwapYZ.setAlignmentX(Component.LEFT_ALIGNMENT);
    rbRotateX.setAlignmentX(Component.LEFT_ALIGNMENT);
    pnlReorientationChoices.add(rbNone.getComponent());
    pnlReorientationChoices.add(rbSwapYZ.getComponent());
    pnlReorientationChoices.add(rbRotateX.getComponent());
    pnlReorientation.add(pnlReorientationChoices);
    //reorientation warning panel
    JPanel pnlReorientationWarning = new JPanel();
    pnlReorientationWarning.setLayout(new BoxLayout(pnlReorientationWarning,
        BoxLayout.Y_AXIS));
    pnlReorientationWarning.add(lWarning1);
    pnlReorientationWarning.add(lWarning2);
    pnlReorientationWarning.add(lWarning3);
    pnlReorientationWarning.add(lWarning4);
    pnlReorientationWarning.add(lWarning5);
    pnlReorientation.add(pnlReorientationWarning);
    //trimvol panel
    pnlTrimvol.add(pnlReorientation.getContainer());
    pnlTrimvol.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlTrimvol.add(pnlButton);
    pnlTrimvol.add(Box.createRigidArea(FixedDim.x0_y10));

    ScalingListener ScalingListener = new ScalingListener(this);
    rbScaleFixed.addActionListener(ScalingListener);
    rbScaleSection.addActionListener(ScalingListener);
    cbConvertToBytes.addActionListener(ScalingListener);

    buttonActonListener = new ButtonListener(this);
    btnImodFull.addActionListener(buttonActonListener);
    btnTrimvol.addActionListener(buttonActonListener);
    btnImodTrim.addActionListener(buttonActonListener);
    btnGetCoordinates.addActionListener(buttonActonListener);

    setToolTipText();
  }

  /**
   * Return the container of the panel
   * @return
   */
  public Container getContainer() {
    return pnlTrimvol;
  }

  /**
   * Set the panel values with the specified parameters
   * @param trimvolParam
   */
  void setParameters(TrimvolParam trimvolParam) {
    ltfXMin.setText(trimvolParam.getXMin());
    ltfXMax.setText(trimvolParam.getXMax());
    //  Y and Z  are swapped to present the user with Z as the depth domain
    ltfYMin.setText(trimvolParam.getZMin());
    ltfYMax.setText(trimvolParam.getZMax());
    ltfZMin.setText(trimvolParam.getYMin());
    ltfZMax.setText(trimvolParam.getYMax());
    if (trimvolParam.isSwapYZ()) {
      rbSwapYZ.setSelected(true);
    }
    else if (trimvolParam.isRotateX()) {
      rbRotateX.setSelected(true);
    }
    else {
      rbNone.setSelected(true);
    }

    cbConvertToBytes.setSelected(trimvolParam.isConvertToBytes());
    if (trimvolParam.isFixedScaling()) {
      ltfFixedScaleMin.setText(trimvolParam.getFixedScaleMin());
      ltfFixedScaleMax.setText(trimvolParam.getFixedScaleMax());
      rbScaleFixed.setSelected(true);
    }
    else {
      ltfSectionScaleMin.setText(trimvolParam.getSectionScaleMin());
      ltfSectionScaleMax.setText(trimvolParam.getSectionScaleMax());
      rbScaleSection.setSelected(true);
    }
    setScaleState();
    pnlScaleRubberband.setParameters(trimvolParam.getScaleXYParam());
  }

  /**
   * Get the parameter values from the panel 
   * @param trimvolParam
   */
  public boolean getParameters(TrimvolParam trimvolParam) {
    trimvolParam.setXMin(ltfXMin.getText());
    trimvolParam.setXMax(ltfXMax.getText());
    //  Y and Z  are swapped to present the user with Z as the depth domain
    trimvolParam.setYMin(ltfZMin.getText());
    trimvolParam.setYMax(ltfZMax.getText());
    trimvolParam.setZMin(ltfYMin.getText());
    trimvolParam.setZMax(ltfYMax.getText());
    trimvolParam.setSwapYZ(rbSwapYZ.isSelected());
    trimvolParam.setRotateX(rbRotateX.isSelected());

    trimvolParam.setConvertToBytes(cbConvertToBytes.isSelected());
    if (rbScaleFixed.isSelected()) {
      trimvolParam.setFixedScaling(true);
      try {
        trimvolParam.setFixedScaleMin(ltfFixedScaleMin.getText()).validate(
            SCALING_ERROR_TITLE, FIXED_SCALE_MIN_LABEL, axisID);
        trimvolParam.setFixedScaleMax(ltfFixedScaleMax.getText()).validate(
            SCALING_ERROR_TITLE, FIXED_SCALE_MAX_LABEL, axisID);
      }
      catch (InvalidEtomoNumberException e) {
        return false;
      }
    }
    else {
      trimvolParam.setFixedScaling(false);
      try {
        trimvolParam.setSectionScaleMin(ltfSectionScaleMin.getText()).validate(
            SCALING_ERROR_TITLE, SECTION_SCALE_MIN_LABEL, axisID);
        trimvolParam.setSectionScaleMax(ltfSectionScaleMax.getText()).validate(
            SCALING_ERROR_TITLE, SECTION_SCALE_MAX_LABEL, axisID);
      }
      catch (InvalidEtomoNumberException e) {
        return false;
      }
    }
    //get the xyParam and set the values in it
    pnlScaleRubberband.getParameters(trimvolParam.getScaleXYParam());
    return true;
  }

  public void setParameters(ReconScreenState screenState) {
    btnTrimvol.setButtonState(screenState.getButtonState(btnTrimvol
        .getButtonStateKey()));
  }

  public void setXYMinAndMax(Vector coordinates) {
    if (coordinates == null) {
      return;
    }
    int size = coordinates.size();
    if (size == 0) {
      return;
    }
    int index = 0;
    while (index < size) {
      if (ImodProcess.RUBBERBAND_RESULTS_STRING.equals((String) coordinates
          .get(index++))) {
        ltfXMin.setText((String) coordinates.get(index++));
        if (index >= size) {
          return;
        }
        ltfYMin.setText((String) coordinates.get(index++));
        if (index >= size) {
          return;
        }
        ltfXMax.setText((String) coordinates.get(index++));
        if (index >= size) {
          return;
        }
        ltfYMax.setText((String) coordinates.get(index++));
        if (index >= size) {
          return;
        }
        ltfZMin.setText((String) coordinates.get(index++));
        if (index >= size) {
          return;
        }
        ltfZMax.setText((String) coordinates.get(index++));
        if (index >= size) {
          return;
        }
      }
    }
  }

  public void setZMin(String zMin) {
    if (rbScaleSection.isSelected()) {
      ltfSectionScaleMin.setText(zMin);
    }
  }

  public void setZMax(String zMax) {
    if (rbScaleSection.isSelected()) {
      ltfSectionScaleMax.setText(zMax);
    }
  }

  /**
   * Enable/disable the appropriate text fields for the scale section
   *
   */
  private void setScaleState() {
    rbScaleFixed.setEnabled(cbConvertToBytes.isSelected());
    rbScaleSection.setEnabled(cbConvertToBytes.isSelected());
    boolean fixedState = cbConvertToBytes.isSelected()
        && rbScaleFixed.isSelected();
    ltfFixedScaleMin.setEnabled(fixedState);
    ltfFixedScaleMax.setEnabled(fixedState);
    boolean scaleState = cbConvertToBytes.isSelected()
        && rbScaleSection.isSelected();
    ltfSectionScaleMin.setEnabled(scaleState);
    ltfSectionScaleMax.setEnabled(scaleState);
    pnlScaleRubberband.setEnabled(scaleState);
  }

  /**
   * Call setScaleState when the radio buttons change
   * @param event
   */
  protected void scaleAction(ActionEvent event) {
    setScaleState();
  }

  public void action(final Run3dmodButton button,
      final Run3dmodMenuOptions run3dmodMenuOptions) {
    buttonAction(button.getActionCommand(), button.getDeferred3dmodButton(),
        run3dmodMenuOptions);
  }

  private void buttonAction(final String command,
      Deferred3dmodButton deferred3dmodButton,
      final Run3dmodMenuOptions run3dmodMenuOptions) {
    if (command == btnTrimvol.getActionCommand()) {
      applicationManager.trimVolume(btnTrimvol, null, deferred3dmodButton,
          run3dmodMenuOptions, dialogType);
    }
    if (command == btnGetCoordinates.getActionCommand()) {
      setXYMinAndMax(applicationManager.imodGetRubberbandCoordinates(
          ImodManager.COMBINED_TOMOGRAM_KEY, AxisID.ONLY));
    }
    else if (command == btnImodFull.getActionCommand()) {
      applicationManager.imodCombinedTomogram(run3dmodMenuOptions);
    }
    if (command == btnImodTrim.getActionCommand()) {
      applicationManager.imodTrimmedVolume(run3dmodMenuOptions);
    }
  }

  void done() {
    btnTrimvol.removeActionListener(buttonActonListener);
  }

  private void cbConvertToBytesAction(ActionEvent event) {
    boolean state = cbConvertToBytes.isSelected();
    rbScaleFixed.setEnabled(state);
    ltfFixedScaleMax.setEnabled(state);
    ltfFixedScaleMin.setEnabled(state);

    rbScaleSection.setEnabled(state);
    ltfSectionScaleMin.setEnabled(state);
    ltfSectionScaleMax.setEnabled(state);
  }

  /**
   * An inner class to manage the scale controls 
   */
  class ScalingListener implements ActionListener {
    TrimvolPanel listenee;

    ScalingListener(TrimvolPanel TrimvolPanel) {
      listenee = TrimvolPanel;
    }

    public void actionPerformed(ActionEvent event) {
      listenee.scaleAction(event);
    }
  }

  private final class ButtonListener implements ActionListener {
    private final TrimvolPanel listenee;

    private ButtonListener(final TrimvolPanel trimvolPanel) {
      listenee = trimvolPanel;
    }

    public void actionPerformed(final ActionEvent event) {
      listenee.buttonAction(event.getActionCommand(), null, null);
    }
  }

  /**
   * Initialize the tooltip text
   */
  private void setToolTipText() {
    ltfXMin
        .setToolTipText("The X coordinate on the left side to retain in the volume.");
    ltfXMax
        .setToolTipText("The X coordinate on the right side to retain in the volume.");
    ltfYMin.setToolTipText("The lower Y coordinate to retain in the volume.");
    ltfYMax.setToolTipText("The upper Y coordinate to retain in the volume.");
    ltfZMin.setToolTipText("The bottom Z slice to retain in the volume.");
    ltfZMax.setToolTipText("The top Z slice to retain in the volume.");
    cbConvertToBytes
        .setToolTipText("Scale densities to bytes with extreme densities truncated.");
    rbScaleFixed
        .setToolTipText("Set the scaling to match the contrast in a 3dmod display.");
    ltfFixedScaleMin
        .setToolTipText("Enter the black contrast slider setting (0-254) that gives the desired "
            + "contrast.");
    ltfFixedScaleMax
        .setToolTipText("Enter the white contrast slider setting (1-255) that gives the desired "
            + "contrast.");
    rbScaleSection
        .setToolTipText("Set the scaling based on the range of contrast in a subset of sections and XY volume.  "
            + "Exclude areas with extreme densities that can be truncated (gold "
            + "particles).");
    ltfSectionScaleMin
        .setToolTipText("Minimum Z section of the subset to analyze for contrast range.");
    ltfSectionScaleMax
        .setToolTipText("Maximum Z section of the subset to analyze for contrast range.");
    pnlReorientationChoices
        .setToolTipText("If the output volume is not reoriented, "
            + "the file will need to be flipped when loaded into 3dmod.");
    rbNone
        .setToolTipText("Do not change the orientation of the output volume.  "
            + "The file will need to be flipped when loaded into 3dmod.");
    rbSwapYZ
        .setToolTipText("Flip Y and Z in the output volume so that the file does not need to be "
            + "flipped when loaded into 3dmod.");
    rbRotateX
        .setToolTipText("Rotate the output volume by -90 degrees around the X axis, "
            + "by first creating a temporary trimmed volume with newstack then running \"clip rotx\" on this volume to create the final output file.  "
            + "The slices will look the same as with the -yz option but rotating instead of flipping will preserve the handedness of structures.");
    btnImodFull.setToolTipText("View the original, untrimmed volume in 3dmod.");
    btnGetCoordinates
        .setToolTipText("After pressing the 3dmod Full Volume button, press shift-B in the "
            + "ZaP window.  Create a rubberband around the volume range.  Then "
            + "press this button to retrieve X and Y coordinates.");
    btnTrimvol
        .setToolTipText("Trim the original volume with the parameters given above.");
    btnImodTrim.setToolTipText("View the trimmed volume.");
  }
}