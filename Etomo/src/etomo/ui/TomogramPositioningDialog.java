package etomo.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import etomo.ApplicationManager;
import etomo.comscript.ConstTiltParam;
import etomo.comscript.ConstTiltalignParam;
import etomo.comscript.TiltParam;
import etomo.comscript.TiltalignParam;
import etomo.type.AxisID;

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
 * <p> Revision 2.4  2003/06/05 21:07:12  rickg
 * <p> Label change to match log file
 * <p>
 * <p> Revision 2.3  2003/05/23 22:13:47  rickg
 * <p> Removed any extensions from log file labels in context menu
 * <p>
 * <p> Revision 2.2  2003/04/28 23:25:25  rickg
 * <p> Changed visible imod references to 3dmod
 * <p>
 * <p> Revision 2.1  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.7.2.1  2003/01/24 18:43:37  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.7  2002/12/19 17:45:22  rickg
 * <p> Implemented advanced dialog state processing
 * <p> including:
 * <p> default advanced state set on start up
 * <p> advanced button management now handled by
 * <p> super class
 * <p>
 * <p> Revision 1.6  2002/12/19 00:30:26  rickg
 * <p> app manager and root pane moved to super class
 * <p>
 * <p> Revision 1.5  2002/11/14 21:18:37  rickg
 * <p> Added anchors into the tomoguide
 * <p>
 * <p> Revision 1.4  2002/10/17 22:40:29  rickg
 * <p> Added fileset name to window title
 * <p> this reference removed applicationManager messages
 * <p>
 * <p> Revision 1.3  2002/10/07 22:31:18  rickg
 * <p> removed unused imports
 * <p> reformat after emacs trashed it
 * <p>
 * <p> Revision 1.2  2002/09/19 21:37:57  rickg
 * <p> Removed stdout messages
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class TomogramPositioningDialog
  extends ProcessDialog
  implements ContextMenu {
  public static final String rcsid =
    "$Id$";

  private JPanel panelPosition = new JPanel();
  private BeveledBorder border = new BeveledBorder("Tomogram Positioning");

  private LabeledTextField ltfSampleTomoThickness =
    new LabeledTextField("Sample tomogram thickness: ");
  private JToggleButton buttonSample =
    new JToggleButton("<html><b>Create Sample Tomograms</b>");

  private JToggleButton buttonCreateBoundary =
    new JToggleButton("<html><b>Create Boundary Models</b>");

  private JToggleButton buttonTomopitch =
    new JToggleButton("<html><b>Compute Z Shift & Pitch Angles</b>");

  private LabeledTextField ltfTiltAngleOffset =
    new LabeledTextField("Total angle offset: ");
  private LabeledTextField ltfTiltAxisZShift =
    new LabeledTextField("Total Z shift: ");
  private JToggleButton buttonAlign =
    new JToggleButton("<html><b>Create Final Alignment</b>");

  public TomogramPositioningDialog(ApplicationManager appMgr, AxisID axisID) {
    super(appMgr, axisID);
    fixRootPanel(rootSize);

    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
    buttonExecute.setText("Done");

    ltfSampleTomoThickness.setTextPreferredSize(new Dimension(50, 20));
    buttonSample.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonSample.setPreferredSize(FixedDim.button2Line);
    buttonSample.setMaximumSize(FixedDim.button2Line);

    buttonCreateBoundary.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonCreateBoundary.setPreferredSize(FixedDim.button2Line);
    buttonCreateBoundary.setMaximumSize(FixedDim.button2Line);

    buttonTomopitch.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonTomopitch.setPreferredSize(FixedDim.button2Line);
    buttonTomopitch.setMaximumSize(FixedDim.button2Line);

    buttonAlign.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonAlign.setPreferredSize(FixedDim.button2Line);
    buttonAlign.setMaximumSize(FixedDim.button2Line);

    // Bind the buttons to the action listener
    TomogramPositioningActionListener tomogramPositioningActionListener =
      new TomogramPositioningActionListener(this);
    buttonSample.addActionListener(tomogramPositioningActionListener);
    buttonCreateBoundary.addActionListener(tomogramPositioningActionListener);
    buttonTomopitch.addActionListener(tomogramPositioningActionListener);
    buttonAlign.addActionListener(tomogramPositioningActionListener);

    //  Create the primary panels
    panelPosition.setBorder(border.getBorder());
    panelPosition.setLayout(new BoxLayout(panelPosition, BoxLayout.Y_AXIS));

    panelPosition.add(ltfSampleTomoThickness.getContainer());
    panelPosition.add(Box.createRigidArea(FixedDim.x0_y10));
    panelPosition.add(buttonSample);
    panelPosition.add(Box.createRigidArea(FixedDim.x0_y10));
    panelPosition.add(buttonCreateBoundary);
    panelPosition.add(Box.createRigidArea(FixedDim.x0_y10));
    panelPosition.add(buttonTomopitch);
    panelPosition.add(Box.createRigidArea(FixedDim.x0_y10));
    panelPosition.add(ltfTiltAngleOffset.getContainer());
    panelPosition.add(Box.createRigidArea(FixedDim.x0_y10));
    panelPosition.add(ltfTiltAxisZShift.getContainer());
    panelPosition.add(Box.createRigidArea(FixedDim.x0_y10));
    panelPosition.add(buttonAlign);

    //  Create dialog content pane
    rootPanel.add(panelPosition);
    rootPanel.add(Box.createVerticalGlue());
    rootPanel.add(Box.createRigidArea(FixedDim.x0_y10));
    rootPanel.add(panelExitButtons);
    rootPanel.add(Box.createRigidArea(FixedDim.x0_y10));

    //  Mouse adapter for context menu
    GenericMouseAdapter mouseAdapter = new GenericMouseAdapter(this);
    rootPanel.addMouseListener(mouseAdapter);

    // Set the default advanced dialog state
    updateAdvanced();
  }

  //  Set the tilt.com parameters that are editable in this dialog
  public void setTiltParams(ConstTiltParam tiltParam) {
    ltfSampleTomoThickness.setText(tiltParam.getThickness());
  }

  // Get the tilt.com parameters that 
  public void getTiltParams(TiltParam tiltParam) throws NumberFormatException {
    try {
      tiltParam.setThickness(
        Integer.parseInt(ltfSampleTomoThickness.getText()));
    }
    catch (NumberFormatException except) {
      String message = "Axis " + axisID.getExtension() + except.getMessage();
      throw new NumberFormatException(message);
    }
  }

  public void setAlignParams(ConstTiltalignParam tiltalignParam) {
    ltfTiltAngleOffset.setText(tiltalignParam.getTiltAngleOffset());
    ltfTiltAxisZShift.setText(tiltalignParam.getTiltAxisZShift());
  }

  public void getAlignParams(TiltalignParam tiltalignParam)
    throws NumberFormatException {
    try {
      tiltalignParam.setTiltAngleOffset(ltfTiltAngleOffset.getText());
      tiltalignParam.setTiltAxisZShift(ltfTiltAxisZShift.getText());
    }
    catch (NumberFormatException except) {
      throw new NumberFormatException(except.getMessage());
    }
  }

  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
    String[] manPagelabel = { "tomopitch", "newst", "3dmod", "tilt" };
    String[] manPage =
      { "tomopitch.html", "newst.html", "3dmod.html",  "tilt.html" };

    String[] logFileLabel;
    String[] logFile;
    logFileLabel = new String[2];
		logFileLabel[0] = "tomopitch";
    logFileLabel[1] = "sample";

    logFile = new String[2];
		logFile[0] = "tomopitch" + axisID.getExtension() + ".log";
    logFile[1] = "sample" + axisID.getExtension() + ".log";

    ContextPopup contextPopup =
      new ContextPopup(
        rootPanel,
        mouseEvent,
        "GENERATING THE TOMOGRAM",
        manPagelabel,
        manPage,
        logFileLabel,
        logFile);
  }

  //  Button action handler methods

  void buttonAction(ActionEvent event) {
    String command = event.getActionCommand();

    if (command.equals(buttonSample.getActionCommand())) {
      applicationManager.createSample(axisID);
    }

    else if (command.equals(buttonCreateBoundary.getActionCommand())) {
      applicationManager.imodSample(axisID);
    }

    else if (command.equals(buttonTomopitch.getActionCommand())) {
      applicationManager.tomopitch(axisID);
    }

    else if (command.equals(buttonAlign.getActionCommand())) {
      applicationManager.finalAlign(axisID);
    }

  }

  //  Action function overides for buttons
  public void buttonCancelAction(ActionEvent event) {
    super.buttonCancelAction(event);
    applicationManager.doneTomogramPositioningDialog(axisID);
  }

  public void buttonPostponeAction(ActionEvent event) {
    super.buttonPostponeAction(event);
    applicationManager.doneTomogramPositioningDialog(axisID);
  }

  public void buttonExecuteAction(ActionEvent event) {
    super.buttonExecuteAction(event);
    applicationManager.doneTomogramPositioningDialog(axisID);
  }

  public void buttonAdvancedAction(ActionEvent event) {
    super.buttonAdvancedAction(event);
    updateAdvanced();
  }

  void updateAdvanced() {
    applicationManager.packMainWindow();
  }
}

//
//  Action listener adapters
//
class TomogramPositioningActionListener implements ActionListener {

  TomogramPositioningDialog adaptee;

  TomogramPositioningActionListener(TomogramPositioningDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent event) {
    adaptee.buttonAction(event);
  }
}
