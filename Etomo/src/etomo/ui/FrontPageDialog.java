package etomo.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

import etomo.EtomoDirector;
import etomo.type.AxisID;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2008</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$ </p>
 */

final class FrontPageDialog {
  public static final String rcsid = "$Id$";

  private final SpacedPanel pnlRoot = SpacedPanel.getInstance(true);
  private final MultiLineButton btnRecon = new MultiLineButton(
      "Tomographic Reconstruction");
  private final MultiLineButton btnJoin = new MultiLineButton("Join Tomograms");
  private final MultiLineButton btnNad = new MultiLineButton(
      "Nonlinear Anisotropic Diffusion");
  private final MultiLineButton btnGeneric = new MultiLineButton(
      "Generic Parallel Process");
  private final MultiLineButton btnPeet = new MultiLineButton("PEET");

  private FrontPageDialog() {
  }

  static FrontPageDialog getInstance() {
    FrontPageDialog instance = new FrontPageDialog();
    instance.createPanel();
    instance.setTooltips();
    instance.addListeners();
    return instance;
  }

  private void createPanel() {
    //local panels
    SpacedPanel pnlReconstruction = SpacedPanel.getInstance();
    SpacedPanel pnlParallelProcesses = SpacedPanel.getInstance();
    SpacedPanel pnlAveraging = SpacedPanel.getInstance();
    //initialize
    btnRecon.setSize();
    btnJoin.setSize();
    btnNad.setSize();
    btnGeneric.setSize();
    btnPeet.setSize();
    //root panel
    pnlRoot.setBoxLayout(BoxLayout.Y_AXIS);
    pnlRoot.add(pnlReconstruction);
    pnlRoot.add(pnlParallelProcesses);
    pnlRoot.add(pnlAveraging);
    //reconstruction panel
    pnlReconstruction.setBoxLayout(BoxLayout.X_AXIS);
    pnlReconstruction.add(btnRecon.getComponent());
    pnlReconstruction.add(btnJoin.getComponent());
    //parallel process panel
    pnlParallelProcesses.setBoxLayout(BoxLayout.X_AXIS);
    pnlParallelProcesses.add(btnNad.getComponent());
    pnlParallelProcesses.add(btnGeneric.getComponent());
    //averaging panel
    pnlAveraging.setBoxLayout(BoxLayout.X_AXIS);
    pnlAveraging.add(btnPeet.getComponent());
  }

  private void addListeners() {
    ActionListener actionListener = new FrontPageActionListener(this);
    btnRecon.addActionListener(actionListener);
    btnJoin.addActionListener(actionListener);
    btnNad.addActionListener(actionListener);
    btnGeneric.addActionListener(actionListener);
    btnPeet.addActionListener(actionListener);
  }

  Component getComponent() {
    return pnlRoot.getContainer();
  }

  private void action(ActionEvent actionEvent) {
    String actionCommand = actionEvent.getActionCommand();
    if (actionCommand.equals(btnRecon.getActionCommand())) {
      EtomoDirector.INSTANCE.openTomogram(true, AxisID.ONLY);
    }
    else if (actionCommand.equals(btnJoin.getActionCommand())) {
      EtomoDirector.INSTANCE.openJoin(true, AxisID.ONLY);
    }
    else if (actionCommand.equals(btnNad.getActionCommand())) {
      EtomoDirector.INSTANCE.openAnisotropicDiffusion(true, AxisID.ONLY);
    }
    else if (actionCommand.equals(btnGeneric.getActionCommand())) {
      EtomoDirector.INSTANCE.openGenericParallel(true, AxisID.ONLY);
    }
    else if (actionCommand.equals(btnPeet.getActionCommand())) {
      EtomoDirector.INSTANCE.openPeet(true, AxisID.ONLY);
    }
  }

  private void setTooltips() {
    btnRecon.setToolTipText("Start a new tomographic reconstruction.");
    btnJoin.setToolTipText("Stack tomograms.");
    btnNad.setToolTipText("Run a nonlinear anisotropic diffusion process on a "
        + "tomogram.");
    btnGeneric.setToolTipText("Run a generic parallel process.");
    btnPeet
        .setToolTipText("Start the interface for the PEET particle averaging "
            + "package.");
  }

  private static final class FrontPageActionListener implements ActionListener {
    private final FrontPageDialog listenee;

    private FrontPageActionListener(FrontPageDialog listenee) {
      this.listenee = listenee;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      listenee.action(actionEvent);
    }
  }
}
