package etomo.ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import etomo.ApplicationManager;
import etomo.comscript.ConstSolvematchParam;
import etomo.comscript.SolvematchParam;
import etomo.comscript.CombineParams;
import etomo.type.FiducialMatch;

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
 * <p> Revision 3.10  2004/06/13 17:03:23  rickg
 * <p> Solvematch mid change
 * <p>
 * <p> Revision 3.9  2004/05/11 21:47:13  sueh
 * <p> bug# 302 removing print statements
 * <p>
 * <p> Revision 3.8  2004/05/11 20:57:07  sueh
 * <p> bug# 302
 * <p>
 * <p> Revision 3.7  2004/05/11 20:53:22  sueh
 * <p> bug# 302 adding InitialCombineValues interface
 * <p> standardizing synchronization
 * <p>
 * <p> Revision 3.6  2004/05/05 22:24:09  sueh
 * <p> bug# 416 moving binned by 2 checkbox to above matching models
 * <p> button
 * <p>
 * <p> Revision 3.5  2004/05/03 22:26:10  sueh
 * <p> bug# 416 Adding Bin By 2 checkbox.  Passing tab identifier to
 * <p> imodMatchingModel so that checkbox settings can be copied between
 * <p> Setup and Initial tabs when the Matching Models button is pressed.
 * <p>
 * <p> Revision 3.4  2004/03/22 23:22:49  sueh
 * <p> bug# 250 synchronize tab with combineParams, matchorwarp does affect Setup
 * <p> tab
 * <p>
 * <p> Revision 3.3  2004/03/01 23:59:54  sueh
 * <p> bug# 250 add getCombineParams()
 * <p> Call combine and restart functions with tab information
 * <p>
 * <p> Revision 3.2  2004/02/27 20:01:58  sueh
 * <p> bug# 250 renamed setMatchingModels() to setUseMatchingModels()
 * <p> added getUseMatchingModels()
 * <p>
 * <p> Revision 3.1  2004/01/30 22:45:13  sueh
 * <p> bug# 356 Changing buttons with html labels to
 * <p> MultiLineButton and MultiLineToggleButton
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 1.16  2003/11/05 19:56:58  rickg
 * <p> Bug# 300 Selecting matching models on setup patch now
 * <p> selects matching models on initial page
 * <p>
 * <p> Revision 1.15  2003/10/29 17:23:02  rickg
 * <p> Bug# 301 Tooltips
 * <p>
 * <p> Revision 1.14  2003/10/21 23:43:42  rickg
 * <p> Changed imod buttons to non multiline
 * <p>
 * <p> Revision 1.13  2003/10/20 20:25:59  rickg
 * <p> Bug# 228 added Restart at matchvol1 button
 * <p>
 * <p> Revision 1.12  2003/10/15 22:46:41  rickg
 * <p> Button size change
 * <p> Label changes
 * <p>
 * <p> Revision 1.11  2003/10/15 19:15:52  rickg
 * <p> Bug# 299 Moved buttons up
 * <p>
 * <p> Revision 1.10  2003/06/05 04:42:37  rickg
 * <p> Label change for view match shift results
 * <p>
 * <p> Revision 1.9  2003/04/28 23:25:25  rickg
 * <p> Changed visible imod references to 3dmod
 * <p>
 * <p> Revision 1.8  2003/03/20 21:19:08  rickg
 * <p> Added matchshift results button/access
 * <p>
 * <p> Revision 1.7  2003/03/20 17:47:21  rickg
 * <p> Initial implementation of panel
 * <p>
 * <p> Revision 1.6  2003/03/18 23:41:07  rickg
 * <p> Restructured for both model and non model based combines
 * <p>
 * <p> </p>
 */
public class InitialCombinePanel implements ContextMenu, InitialCombineFields {
  public static final String rcsid = "$Id$";

  private TomogramCombinationDialog tomogramCombinationDialog;
  private ApplicationManager applicationManager;

  private JPanel pnlRoot = new JPanel();

  private SolvematchPanel pnlSolvematch;

  private JPanel pnlButton = new JPanel();
  private MultiLineButton btnMatchcheck = new MultiLineButton(
    "<html><b>View Match Check Volume</b>");
  private MultiLineButton btnRestart = new MultiLineButton(
    "<html><b>Restart Combine</b>");
  private MultiLineToggleButton btnMatchvolRestart = new MultiLineToggleButton(
    "<html><b>Restart at Matchvol1</b>");

  /**
   * Default constructor
   * @param appMgr
   */
  public InitialCombinePanel(TomogramCombinationDialog parent,
    ApplicationManager appMgr) {
    tomogramCombinationDialog = parent;
    applicationManager = appMgr;

    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));

    ///  Create the solvematch panel
    pnlSolvematch = new SolvematchPanel(tomogramCombinationDialog, appMgr);

    //  Layout the button panel
    pnlButton.setLayout(new BoxLayout(pnlButton, BoxLayout.X_AXIS));
    pnlButton.add(Box.createHorizontalGlue());
    pnlButton.add(btnMatchcheck);
    pnlButton.add(Box.createHorizontalGlue());
    pnlButton.add(btnRestart);
    pnlButton.add(Box.createHorizontalGlue());
    pnlButton.add(btnMatchvolRestart);
    pnlButton.add(Box.createHorizontalGlue());
    UIUtilities.setButtonSizeAll(pnlButton, UIParameters.getButtonDimension());

    pnlRoot.add(pnlSolvematch.getContainer());
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlRoot.add(pnlButton);
    pnlRoot.add(Box.createVerticalGlue());

    //  Bind the UI objects to their ActionListeners
    ButtonActionListener buttonAction = new ButtonActionListener(this);
    btnRestart.addActionListener(buttonAction);
    btnMatchvolRestart.addActionListener(buttonAction);
    btnMatchcheck.addActionListener(buttonAction);

    // Mouse listener for context menu
    GenericMouseAdapter mouseAdapter = new GenericMouseAdapter(this);
    pnlRoot.addMouseListener(mouseAdapter);
    setToolTipText();
  }

  public Container getContainer() {
    return pnlRoot;
  }

  public void setAdvanced(boolean state) {

  }

  /**
   * Set the solvematch parameters from the ConstSolvematchParam object
   * @param solvematchParam
   */
  public void setSolvematchParams(ConstSolvematchParam solvematchParam) {
    pnlSolvematch.setParameters(solvematchParam);
  }

  /**
   * Get the solvematch parameters from the UI
   * @param solvematchsParam
   */
  public void getSolvematchParams(SolvematchParam solvematchParam) {
    pnlSolvematch.getParameters(solvematchParam);
  }

  /**
   * Get the combine parameters from the UI
   * @param combineParams
   */
  public void getCombineParameters(CombineParams combineParams) {
    pnlSolvematch.getParameters(combineParams);
  }

  // InitialiCombineFields interface pass-thru
  public FiducialMatch getSurfacesOrModels() {
    return pnlSolvematch.getSurfacesOrModels();
  }

  public void setSurfacesOrModels(FiducialMatch state) {
    pnlSolvematch.setSurfacesOrModels(state);
  }

  public boolean isBinBy2() {
    return pnlSolvematch.isBinBy2();
  }

  public void setBinBy2(boolean state) {
    pnlSolvematch.setBinBy2(state);
  }

  public void setFiducialMatchListA(String fiducialMatchListA) {
    pnlSolvematch.setFiducialMatchListA(fiducialMatchListA);
  }

  public String getFiducialMatchListA() {
    return pnlSolvematch.getFiducialMatchListA();
  }

  public void setFiducialMatchListB(String fiducialMatchListB) {
    pnlSolvematch.setFiducialMatchListB(fiducialMatchListB);
  }

  public String getFiducialMatchListB() {
    return pnlSolvematch.getFiducialMatchListB();
  }

  /**
   * Right mouse button context menu
   */
  public void popUpContextMenu(MouseEvent mouseEvent) {
    String[] manPagelabel = {"Solvematch", "Matchshifts"};
    String[] manPage = {"solvematch.html", "matchshifts.html"};
    String[] logFileLabel = {"Transferfid", "Solvematch"};
    String[] logFile = {"transferfid.log", "solvematch.log"};

    ContextPopup contextPopup = new ContextPopup(pnlRoot, mouseEvent,
      "Initial Problems in Combining", manPagelabel, manPage, logFileLabel,
      logFile);
  }

  /**
   * Respond to button actions
   * @param event
   */
  private void buttonAction(ActionEvent event) {
    //  Synchronize this panel with the others
    tomogramCombinationDialog.synchronize(TomogramCombinationDialog.lblInitial,
      true, TomogramCombinationDialog.ALL_FIELDS);

    String command = event.getActionCommand();
    if (command.equals(btnMatchcheck.getActionCommand())) {
      applicationManager.imodMatchCheck();
    }
    if (command.equals(btnRestart.getActionCommand())) {
      applicationManager.combine();
    }
    if (command.equals(btnMatchvolRestart.getActionCommand())) {
      applicationManager.matchvol1();
    }
  }

  /**
   * Button action listener inner class
   */
  class ButtonActionListener implements ActionListener {
    InitialCombinePanel listenee;

    ButtonActionListener(InitialCombinePanel initialCombinePanel) {
      listenee = initialCombinePanel;
    }

    public void actionPerformed(ActionEvent event) {
      listenee.buttonAction(event);
    }
  }

  /**
   * Initialize the tooltip text for the axis panel objects
   */
  private void setToolTipText() {
    String text;
    TooltipFormatter tooltipFormatter = new TooltipFormatter();

    text = "View the two volumes that are used for assessing whether Matchshifts "
      + "found the correct shifts between the volumes.";
    btnMatchcheck.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Restart the combine operation from the beginning with the parameters "
      + "specified here.";
    btnRestart.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Resume and make first matching volume, despite a small displacement "
      + "between the match check volumes";
    btnMatchvolRestart.setToolTipText(tooltipFormatter.setText(text).format());
  }
}