package etomo.ui;

import java.awt.event.*;
import javax.swing.*;

import etomo.ApplicationManager;

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
 * <p> Revision 1.3  2002/10/17 22:39:55  rickg
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
public class PostProcessingDialog extends ProcessDialog {
  public static final String rcsid =
    "$Id$";

  public PostProcessingDialog(ApplicationManager appManager) {
    super(appManager);

    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
    this.setTitle(
      "eTomo Post-processing and Trimming: "
        + applicationManager.getFilesetName());

    rootPanel.add(Box.createVerticalGlue());
    rootPanel.add(Box.createRigidArea(FixedDim.x0_y10));
    rootPanel.add(panelExitButtons);
    rootPanel.add(Box.createRigidArea(FixedDim.x0_y10));

    //
    // Calcute the necessary window size
    //
    pack();
  }

  //
  //  Action function overides for buttons
  //
  public void buttonPostponeAction(ActionEvent event) {
    super.buttonPostponeAction(event);
  }

  public void buttonExecuteAction(ActionEvent event) {
    super.buttonExecuteAction(event);
  }
}
