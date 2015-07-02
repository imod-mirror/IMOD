package etomo.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import etomo.type.PluginNiche;

/**
 * <p>Description: Plugin for testing.</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class DummyPlugin implements Plugin, ActionListener {
  private final JPanel pnlRoot = new JPanel();
  private final CheckBox cbBrillig = new CheckBox("'Twas brillig,");
  private final CheckBox cbSlithy = new CheckBox("and the slithy");
  private final CheckBox cbToves = new CheckBox("toves");
  private final CheckBox cbGyre = new CheckBox("Did gyre");
  private final CheckBox cbGimble = new CheckBox("and gimble");
  private final CheckBox cbWabe = new CheckBox("in the wabe:");
  private final CheckBox cbMimsy = new CheckBox("All mimsy");
  private final CheckBox cbBorogoves = new CheckBox("where the borogoves,");
  private final CheckBox cbMomeRaths = new CheckBox("And the mome raths");
  private final CheckBox cbOutgrabe = new CheckBox("outgrabe");

  public DummyPlugin() {
    createPanel();
    addListeners();
  }

  private void createPanel() {
    JPanel pnl1 = new JPanel();
    JPanel pnl2 = new JPanel();
    JPanel pnl3 = new JPanel();
    JPanel pnl4 = new JPanel();
    // root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    pnlRoot.setBorder(BorderFactory.createEtchedBorder());
    pnlRoot.add(new JLabel(getDescription()));
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlRoot.add(pnl1);
    pnlRoot.add(pnl2);
    pnlRoot.add(pnl3);
    pnlRoot.add(pnl4);
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y5));
    // 1
    pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.X_AXIS));
    pnl1.add(cbBrillig);
    pnl1.add(cbSlithy);
    pnl1.add(cbToves);
    // 2
    pnl2.setLayout(new BoxLayout(pnl2, BoxLayout.X_AXIS));
    pnl2.add(Box.createRigidArea(FixedDim.x5_y0));
    pnl2.add(cbGyre);
    pnl2.add(cbGimble);
    pnl2.add(cbWabe);
    // 3
    pnl3.setLayout(new BoxLayout(pnl3, BoxLayout.X_AXIS));
    pnl3.add(cbMimsy);
    pnl3.add(cbBorogoves);
    // 4
    pnl4.setLayout(new BoxLayout(pnl4, BoxLayout.X_AXIS));
    pnl4.add(Box.createRigidArea(FixedDim.x5_y0));
    pnl4.add(cbMomeRaths);
    pnl4.add(cbOutgrabe);
  }

  private void addListeners() {
    cbBrillig.addActionListener(this);
    cbSlithy.addActionListener(this);
    cbToves.addActionListener(this);
    cbGyre.addActionListener(this);
    cbGimble.addActionListener(this);
    cbWabe.addActionListener(this);
    cbMimsy.addActionListener(this);
    cbBorogoves.addActionListener(this);
    cbMomeRaths.addActionListener(this);
    cbOutgrabe.addActionListener(this);
  }

  public void actionPerformed(final ActionEvent event) {
    if (event == null) {
      return;
    }
    String actionCommand = event.getActionCommand();
    if (cbToves.getActionCommand().equals(actionCommand)
      || cbBorogoves.getActionCommand().equals(actionCommand)
      || cbMomeRaths.getActionCommand().equals(actionCommand)) {
      updateDisplay();
    }
  }

  private void updateDisplay() {
    boolean toves = cbToves.isSelected();
    cbSlithy.setSelected(toves);
    cbGyre.setSelected(toves);
    cbGimble.setSelected(toves);
    cbWabe.setSelected(toves);
    cbMimsy.setSelected(cbBorogoves.isSelected());
    cbOutgrabe.setSelected(cbMomeRaths.isSelected());
  }

  public Component getComponent() {
    return pnlRoot;
  }

  public String getDescription() {
    return "Jabberwocky by Lewis Carroll";
  }

  public String getKey() {
    return "Jabberwocky";
  }

  public PluginNiche getPluginNiche() {
    return PluginNiche.TOMOGRAM_GENERATION;
  }

  public String getTitle() {
    return "Jabberwocky";
  }

  public String getVersion() {
    return "1.0";
  }

  public String getButtonTitle() {
    return "Twas brillig";
  }
}
