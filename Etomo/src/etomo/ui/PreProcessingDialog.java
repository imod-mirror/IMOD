package etomo.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import etomo.ApplicationManager;
import etomo.type.*;
import etomo.comscript.ConstCCDEraserParam;
import etomo.comscript.CCDEraserParam;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002 - 2006</p>
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
 * <p> Revision 3.10  2006/06/30 20:02:35  sueh
 * <p> bug# 877 Calling all the done dialog functions from the dialog.done() function,
 * <p> which is called by the button action functions and saveAction() in
 * <p> ProcessDialog.  Removed the button action function overides.  Set displayed to
 * <p> false after the done dialog function is called.
 * <p>
 * <p> Revision 3.9  2006/02/06 21:21:16  sueh
 * <p> bug# 521 Getting toggle buttons through ProcessResultDisplayFactory.
 * <p>
 * <p> Revision 3.8  2006/01/26 22:08:15  sueh
 * <p> bug# 401 For MultiLineButton toggle buttons:  save the state and keep
 * <p> the buttons turned on each they are run, unless the process fails or is
 * <p> killed.
 * <p>
 * <p> Revision 3.7  2006/01/03 23:43:10  sueh
 * <p> bug# 675 Converted JCheckBox's to CheckBox
 * <p>
 * <p> Revision 3.6  2005/08/04 20:15:14  sueh
 * <p> bug# 532  Centralizing fit window functionality by placing fitting functions
 * <p> in UIHarness.  Removing packMainWindow from the manager.  Sending
 * <p> the manager to UIHarness.pack() so that packDialogs() can be called.
 * <p>
 * <p> Revision 3.5  2005/06/17 00:33:19  sueh
 * <p> bug# 685 Changed buttonExecuteAction() so it calls its super function.
 * <p>
 * <p> Revision 3.4  2005/04/21 20:46:07  sueh
 * <p> bug# 615 Pass axisID to packMainWindow so it can pack only the frame
 * <p> that requires it.
 * <p>
 * <p> Revision 3.3  2005/04/16 02:01:02  sueh
 * <p> bug# 615 Moved the adding of exit buttons to the base class.
 * <p>
 * <p> Revision 3.2  2005/01/14 03:08:03  sueh
 * <p> bug# 511 Added DialogType to super constructor.
 * <p>
 * <p> Revision 3.1  2004/03/15 20:33:55  rickg
 * <p> button variable name changes to btn...
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.5  2003/10/30 01:43:44  rickg
 * <p> Bug# 338 Remapped context menu entries
 * <p>
 * <p> Revision 2.4  2003/10/20 20:08:37  sueh
 * <p> Bus322 corrected labels
 * <p>
 * <p> Revision 2.3  2003/07/08 20:49:52  rickg
 * <p> *** empty log message ***
 * <p>
 * <p> Revision 2.2  2003/05/08 00:16:30  rickg
 * <p> Added border for ccderaser panel
 * <p>
 * <p> Revision 2.1  2003/04/28 23:25:25  rickg
 * <p> Changed visible imod references to 3dmod
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.6.2.1  2003/01/24 18:43:37  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.6  2002/12/19 17:45:22  rickg
 * <p> Implemented advanced dialog state processing
 * <p> including:
 * <p> default advanced state set on start up
 * <p> advanced button management now handled by
 * <p> super class
 * <p>
 * <p> Revisiopn 1.5  2002/12/19 00:30:26  rickg
 * <p> app manager and root pane moved to super class
 * <p>
 * <p> Revision 1.4  2002/11/19 02:40:37  rickg
 * <p> Label spelling correction
 * <p>
 * <p> Revision 1.3  2002/10/17 22:40:02  rickg
 * <p> Added fileset name to window title
 * <p> this reference removed applicationManager messages
 * <p>
 * <p> Revision 1.2  2002/10/07 22:31:18  rickg
 * <p> removed unused imports
 * <p> reformat after emacs trashed it
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class PreProcessingDialog extends ProcessDialog {
  public static final String rcsid = "$Id$";

  private JLabel textDM2MRC = new JLabel(
      "No digital micrograph files detected:  ");
  private JPanel pnlDMConvert = new JPanel();
  private CheckBox cbUniqueHeaders = new CheckBox(
      "Digital micrograph files have unique headers");

  private JPanel pnlEraser = new JPanel();
  private CCDEraserPanel panelCCDEraser;

  public PreProcessingDialog(ApplicationManager appManager, AxisID axisID) {
    super(appManager, axisID, DialogType.PRE_PROCESSING);
    panelCCDEraser = new CCDEraserPanel(appManager, axisID, dialogType);

    fixRootPanel(rootSize);

    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

    //  Build the digital micrograph panel
    pnlDMConvert.setLayout(new BoxLayout(pnlDMConvert, BoxLayout.Y_AXIS));
    pnlDMConvert.setBorder(new BeveledBorder("Digital Micrograph Conversion")
        .getBorder());
    pnlDMConvert.add(textDM2MRC);
    pnlDMConvert.add(Box.createRigidArea(FixedDim.x20_y0));
    pnlDMConvert.add(cbUniqueHeaders);
    // applicationManager.isDigitalMicrographData();
    disableDM2MRC();

    //  Build the base panel
    btnExecute.setText("Done");
    pnlDMConvert.setAlignmentX(Component.CENTER_ALIGNMENT);
    rootPanel.add(pnlDMConvert);

    pnlEraser.setLayout(new BoxLayout(pnlEraser, BoxLayout.Y_AXIS));
    pnlEraser.setBorder(new BeveledBorder("CCD Eraser").getBorder());
    pnlEraser.add(panelCCDEraser.getContainer());

    rootPanel.add(pnlEraser);
    addExitButtons();

    //  Set the default advanced state for the window, this also executes
    updateAdvanced();
  }

  public static ProcessResultDisplay getFindXRaysDisplay() {
    return CCDEraserPanel.getFindXRaysDisplay(DialogType.PRE_PROCESSING);
  }

  public static ProcessResultDisplay getCreateFixedStackDisplay() {
    return CCDEraserPanel.getCreateFixedStackDisplay(DialogType.PRE_PROCESSING);
  }

  public static ProcessResultDisplay getUseFixedStackDisplay() {
    return CCDEraserPanel.getUseFixedStackDisplay(DialogType.PRE_PROCESSING);
  }

  /**
   * Set the parameters for the specified CCD eraser panel
   */
  public void setCCDEraserParams(ConstCCDEraserParam ccdEraserParams) {
    panelCCDEraser.setParameters(ccdEraserParams);
  }

  public final void setParameters(ReconScreenState screenState) {
    panelCCDEraser.setParameters(screenState);
  }

  /**
   * Get the input parameters from the dialog box.
   */
  public void getCCDEraserParams(CCDEraserParam ccdEraserParams) {
    panelCCDEraser.getParameters(ccdEraserParams);
  }

  public void buttonAdvancedAction(ActionEvent event) {
    super.buttonAdvancedAction(event);
    updateAdvanced();
  }

  /**
   * Update the dialog with the current advanced state
   */
  private void updateAdvanced() {
    panelCCDEraser.setAdvanced(isAdvanced);
    UIHarness.INSTANCE.pack(axisID, applicationManager);
  }

  private void disableDM2MRC() {
    cbUniqueHeaders.setEnabled(false);
    cbUniqueHeaders.setSelected(false);
    pnlDMConvert.setVisible(false);
  }

  protected void done() {
    if (applicationManager.donePreProcDialog(axisID)) {
      panelCCDEraser.done();
      setDisplayed(false);
    }
  }
}