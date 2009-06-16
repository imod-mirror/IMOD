package etomo.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import etomo.ApplicationManager;
import etomo.comscript.BlendmontParam;
import etomo.comscript.ConstNewstParam;
import etomo.comscript.FortranInputSyntaxException;
import etomo.comscript.NewstParam;
import etomo.storage.LogFile;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.ConstEtomoNumber;
import etomo.type.ConstMetaData;
import etomo.type.DialogType;
import etomo.type.EtomoAutodoc;
import etomo.type.MetaData;
import etomo.type.ProcessResultDisplayFactory;
import etomo.type.ReconScreenState;
import etomo.type.Run3dmodMenuOptions;
import etomo.type.ViewType;
import etomo.util.InvalidParameterException;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2009</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 1.3  2009/06/15 20:26:46  sueh
 * <p> bug# 1221 Reformatted.
 * <p>
 * <p> Revision 1.2  2009/06/12 19:50:09  sueh
 * <p> bug# 1221 Factored running newst, making it independent of the
 * <p> final aligned dialog and expert.
 * <p>
 * <p> Revision 1.1  2009/06/10 22:17:14  sueh
 * <p> bug# 1221 Factoring Newstack and blendmont into NewstackPanel.
 * <p>
 */
final class NewstackPanel implements Expandable, Run3dmodButtonContainer,
    FiducialessParams, BlendmontDisplay, NewstackDisplay {
  public static final String rcsid = "$Id$";

  static final String SIZE_TO_OUTPUT_IN_X_AND_Y_LABEL = "Size to output";

  private final JPanel pnlRoot = new JPanel();

  private final NewstackPanelActionListener actionListener = new NewstackPanelActionListener(
      this);
  private final PanelHeader header;
  private final SpacedPanel pnlBody = SpacedPanel.getInstance();
  private final CheckBox cbUseLinearInterpolation = new CheckBox(
      "Use linear interpolation");
  private final LabeledSpinner spinBinning = new LabeledSpinner(
      "Aligned image stack binning ", new SpinnerNumberModel(1, 1, 8, 1));
  private final CheckBox cbFiducialess = new CheckBox("Fiducialless alignment");
  private final LabeledTextField ltfRotation = new LabeledTextField(
      "Tilt axis rotation: ");
  private final LabeledTextField ltfSizeToOutputInXandY = new LabeledTextField(
      SIZE_TO_OUTPUT_IN_X_AND_Y_LABEL + " (X,Y - unbinned): ");
  private final Run3dmodButton btn3dmodFull = Run3dmodButton.get3dmodInstance(
      "View Full Aligned Stack", this);

  private final Run3dmodButton btnNewst;

  private final AxisID axisID;
  private final ApplicationManager manager;
  private final DialogType dialogType;

  private NewstackPanel(ApplicationManager manager, AxisID axisID,
      DialogType dialogType) {
    this.manager = manager;
    this.axisID = axisID;
    this.dialogType = dialogType;
    if (manager.getMetaData().getViewType() == ViewType.MONTAGE) {
      header = PanelHeader.getAdvancedBasicOnlyInstance("Blendmont", this,
          dialogType);
    }
    else {
      header = PanelHeader.getAdvancedBasicOnlyInstance("Newstack", this,
          dialogType);
    }
    ProcessResultDisplayFactory displayFactory = manager
        .getProcessResultDisplayFactory(axisID);
    btnNewst = (Run3dmodButton) displayFactory.getFullAlignedStack();
  }

  static NewstackPanel getInstance(ApplicationManager manager, AxisID axisID,
      DialogType dialogType) {
    NewstackPanel instance = new NewstackPanel(manager, axisID, dialogType);
    instance.createPanel();
    instance.addListeners();
    instance.setToolTipText();
    return instance;
  }

  private void addListeners() {
    cbFiducialess.addActionListener(actionListener);
    btnNewst.addActionListener(actionListener);
    btn3dmodFull.addActionListener(actionListener);
  }

  Component getComponent() {
    return pnlRoot;
  }

  private void createPanel() {
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(BorderFactory.createEtchedBorder());
    pnlRoot.add(header.getContainer());
    pnlRoot.add(pnlBody.getContainer());
    UIUtilities.alignComponentsX(pnlRoot, Component.LEFT_ALIGNMENT);
    JPanel pnlButtons = new JPanel();
    btnNewst.setContainer(this);
    btnNewst.setDeferred3dmodButton(btn3dmodFull);
    btnNewst.setSize();
    btn3dmodFull.setSize();
    //Body Panel
    pnlBody.setBoxLayout(BoxLayout.Y_AXIS);
    pnlBody.add(cbUseLinearInterpolation);
    pnlBody.add(spinBinning);
    pnlBody.add(cbFiducialess);
    pnlBody.add(ltfRotation);
    pnlBody.add(ltfSizeToOutputInXandY);
    pnlBody.add(pnlButtons);
    pnlBody.alignComponentsX(Component.LEFT_ALIGNMENT);
    //buttons
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
    //buttonPanel
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnNewst.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btn3dmodFull.getComponent());
    pnlButtons.add(Box.createHorizontalGlue());
    updateFiducialess();
  }

  void done() {
    btnNewst.removeActionListener(actionListener);
  }
  
   void setParameters(BlendmontParam blendmontParam) {
    cbUseLinearInterpolation.setSelected(blendmontParam.isLinearInterpolation());
  }
  
   void setParameters(ConstNewstParam newstParam) {
    cbUseLinearInterpolation.setSelected(newstParam.isLinearInterpolation());
  }
   
   final void setParameters(ReconScreenState screenState) {
     header.setState(screenState.getStackNewstHeaderState());
     btnNewst.setButtonState(screenState.getButtonState(btnNewst
         .getButtonStateKey()));
   }
   
    void getParameters(ReconScreenState screenState) {
     header.getState(screenState.getStackNewstHeaderState());
   }

  /**
   * The Metadata values that are from the setup dialog should not be overrided
   * by this dialog unless the Metadata values are empty.
   * @param metaData
   * @throws FortranInputSyntaxException
   */
  void getParameters(MetaData metaData) throws FortranInputSyntaxException {
    metaData.setSizeToOutputInXandY(axisID, ltfSizeToOutputInXandY.getText());
    metaData.setFinalStackBinning(axisID, getBinning());
  }

  public void getParameters(BlendmontParam blendmontParam)
      throws FortranInputSyntaxException, InvalidParameterException,
      IOException {
    blendmontParam
        .setLinearInterpolation(cbUseLinearInterpolation.isSelected());
    blendmontParam.setBinByFactor(getBinning());
    try {
      blendmontParam.convertToStartingAndEndingXandY(ltfSizeToOutputInXandY
          .getText(), manager.getMetaData().getImageRotation(axisID));
    }
    catch (FortranInputSyntaxException e) {
      e.printStackTrace();
      throw new FortranInputSyntaxException(
          NewstackPanel.SIZE_TO_OUTPUT_IN_X_AND_Y_LABEL + ":  "
              + e.getMessage());
    }
  }

  //  Copy the newstack parameters from the GUI to the NewstParam object
  public void getParameters(NewstParam newstParam)
      throws FortranInputSyntaxException {
    newstParam.setLinearInterpolation(cbUseLinearInterpolation.isSelected());
    int binning = getBinning();
    // Only explicitly write out the binning if its value is something other than
    // the default of 1 to keep from cluttering up the com script  
    if (binning > 1) {
      newstParam.setBinByFactor(binning);
    }
    else {
      newstParam.setBinByFactor(Integer.MIN_VALUE);
    }
    newstParam.setSizeToOutputInXandY(ltfSizeToOutputInXandY.getText(),
        getBinning(), manager.getMetaData().getImageRotation(axisID), manager);
  }
  
   void setParameters(ConstMetaData metaData) {
    spinBinning.setValue(metaData.getFinalStackBinning(axisID));
    ltfSizeToOutputInXandY.setText(metaData.getSizeToOutputInXandY(axisID).toString(
        true));
  }

  void setBinning(ConstEtomoNumber binning) {
    spinBinning.setValue(binning);
  }

  private int getBinning() {
    return ((Integer) spinBinning.getValue()).intValue();
  }

  void setFiducialessAlignment(boolean state) {
    cbFiducialess.setSelected(state);
    updateFiducialess();
  }

  public boolean isFiducialess() {
    return cbFiducialess.isSelected();
  }

  private void updateFiducialess() {
    ltfRotation.setEnabled(cbFiducialess.isSelected());
  }

  void setImageRotation(float tiltAxisAngle) {
    ltfRotation.setText(tiltAxisAngle);
  }

  public float getImageRotation() throws NumberFormatException {
    return Float.parseFloat(ltfRotation.getText());
  }

  void setAdvanced(boolean advanced) {
    header.setAdvanced(advanced);
  }

  boolean isAdvanced() {
    return header.isAdvanced();
  }

  private void updateAdvanced(boolean advanced) {
    ltfSizeToOutputInXandY.setVisible(advanced);
  }

  public void action(final Run3dmodButton button,
      final Run3dmodMenuOptions run3dmodMenuOptions) {
    action(button.getActionCommand(), button.getDeferred3dmodButton(),
        run3dmodMenuOptions);
  }

  /**
   * Executes the action associated with command.  Deferred3dmodButton is null
   * if it comes from the dialog's ActionListener.  Otherwise is comes from a
   * Run3dmodButton which called action(Run3dmodButton, Run3dmoMenuOptions).  In
   * that case it will be null unless it was set in the Run3dmodButton.
   * @param command
   * @param deferred3dmodButton
   * @param run3dmodMenuOptions
   */
  void action(final String command,
      final Deferred3dmodButton deferred3dmodButton,
      final Run3dmodMenuOptions run3dmodMenuOptions) {
    if (command.equals(btnNewst.getActionCommand())) {
      manager.newst(btnNewst, null, deferred3dmodButton, axisID,
          run3dmodMenuOptions, dialogType, this, this, this);
    }
    else if (command.equals(cbFiducialess.getActionCommand())) {
      updateFiducialess();
    }
    else if (command.equals(btn3dmodFull.getActionCommand())) {
      manager.imodFineAlign(axisID, run3dmodMenuOptions);
    }
  }

  public void expand(ExpandButton button) {
    if (header != null) {
      if (header.equalsAdvancedBasic(button)) {
        updateAdvanced(button.isExpanded());
      }
    }
    UIHarness.INSTANCE.pack(axisID, manager);
  }

  private void setToolTipText() {
    ReadOnlyAutodoc autodoc = null;
    try {
      autodoc = AutodocFactory.getInstance(AutodocFactory.NEWSTACK, axisID,
          manager.getManagerKey());
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
    if (autodoc != null) {
      ltfSizeToOutputInXandY.setToolTipText(EtomoAutodoc.getTooltip(autodoc,
          NewstParam.SIZE_TO_OUTPUT_IN_X_AND_Y));
    }
    cbUseLinearInterpolation
        .setToolTipText("Make aligned stack with linear instead of cubic "
            + "interpolation to  reduce noise.");
    spinBinning
        .setToolTipText("Set the binning for the aligned image stack and "
            + "tomogram.  With a binned tomogram, all of the thickness, position, "
            + "and size parameters below are still entered in unbinned pixels.");
    cbFiducialess.setToolTipText("Use cross-correlation alignment only.");
    ltfRotation
        .setToolTipText("Rotation angle of tilt axis for generating aligned "
            + "stack from " + "cross-correlation alignment only.");
    btnNewst
        .setToolTipText("Generate the complete aligned stack for input into the "
            + "tilt process." + "  This runs the newst.com script.");
    btn3dmodFull.setToolTipText("Open the complete aligned stack in 3dmod");
  }

  private static final class NewstackPanelActionListener implements
      ActionListener {
    private final NewstackPanel adaptee;

    private NewstackPanelActionListener(final NewstackPanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(final ActionEvent event) {
      adaptee.action(event.getActionCommand(), null, null);
    }
  }
}
