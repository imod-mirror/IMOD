package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.comscript.TrimvolParam;
import etomo.comscript.XYParam;
import etomo.process.ImodProcess;
import etomo.type.AxisID;
import etomo.type.ParallelMetaData;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
public final class RubberbandPanel {
  public static final String rcsid = "$Id$";

  private final BaseManager manager;

  private final JPanel pnlRubberband = new JPanel();
  private final JPanel pnlRange = new JPanel();
  private final LabeledTextField ltfXMin = new LabeledTextField("X min: ");
  private final LabeledTextField ltfXMax = new LabeledTextField("X max: ");
  private final LabeledTextField ltfYMin = new LabeledTextField("Y min: ");
  private final LabeledTextField ltfYMax = new LabeledTextField("Y max: ");
  private final LabeledTextField ltfZMin = new LabeledTextField("Z min: ");
  private final LabeledTextField ltfZMax = new LabeledTextField("Z max: ");
  private final MultiLineButton btnRubberband;
  private final String imodKey;
  private final String borderLabel;
  private final String buttonLabel;
  private final String xMinTooltip;
  private final String xMaxTooltip;
  private final String yMinTooltip;
  private final String yMaxTooltip;
  private final String zMinTooltip;
  private final String zMaxTooltip;
  private final Run3dmodButton run3dmodButton;

  private RubberbandPanel(BaseManager manager, String imodKey,
      String borderLabel, String buttonLabel, String xMinTooltip,
      String xMaxTooltip, String yMinTooltip, String yMaxTooltip,
      String zMinTooltip, String zMaxTooltip, Run3dmodButton run3dmodButton) {
    this.imodKey = imodKey;
    this.borderLabel = borderLabel;
    this.buttonLabel = buttonLabel;
    this.xMinTooltip = xMinTooltip;
    this.xMaxTooltip = xMaxTooltip;
    this.yMinTooltip = yMinTooltip;
    this.yMaxTooltip = yMaxTooltip;
    this.zMinTooltip = zMinTooltip;
    this.zMaxTooltip = zMaxTooltip;
    this.run3dmodButton = run3dmodButton;
    this.manager = manager;
    pnlRubberband.setBorder(new EtchedBorder(borderLabel).getBorder());
    btnRubberband = new MultiLineButton(buttonLabel);
    btnRubberband.setSize();
    pnlRubberband.setLayout(new BoxLayout(pnlRubberband, BoxLayout.Y_AXIS));
    JPanel pnlButtons = null;
    if (run3dmodButton != null) {
      pnlButtons = new JPanel();
      run3dmodButton.setSize();
      pnlButtons.add(run3dmodButton.getComponent());
      pnlButtons.add(btnRubberband.getComponent());
      pnlRange.setLayout(new GridLayout(3, 2, 4, 2));
    }
    else {
      pnlRange.setLayout(new GridLayout(2, 2, 4, 2));
    }
    pnlRange.add(ltfXMin.getContainer());
    pnlRange.add(ltfXMax.getContainer());
    pnlRange.add(ltfYMin.getContainer());
    pnlRange.add(ltfYMax.getContainer());
    if (run3dmodButton != null) {
      pnlRange.add(ltfZMin.getContainer());
      pnlRange.add(ltfZMax.getContainer());
      pnlRubberband.add(pnlButtons);
    }
    pnlRubberband.add(pnlRange);
    if (run3dmodButton == null) {
      pnlRubberband.add(Box.createRigidArea(FixedDim.x0_y5));
      btnRubberband.setAlignmentX(Component.CENTER_ALIGNMENT);
      pnlRubberband.add(btnRubberband.getComponent());
    }
    setToolTipText();
  }

  static RubberbandPanel getInstance(BaseManager manager, String imodKey,
      String borderLabel, String buttonLabel, String xMinTooltip,
      String xMaxTooltip, String yMinTooltip, String yMaxTooltip) {
    RubberbandPanel instance = new RubberbandPanel(manager, imodKey,
        borderLabel, buttonLabel, xMinTooltip, xMaxTooltip, yMinTooltip, "",
        "", yMaxTooltip, null);
    instance.addListeners();
    return instance;
  }

  static RubberbandPanel getInstance(BaseManager manager, String imodKey,
      String borderLabel, String buttonLabel, String xMinTooltip,
      String xMaxTooltip, String yMinTooltip, String yMaxTooltip,
      String zMinTooltip, String zMaxTooltip, Run3dmodButton run3dmodButton) {
    RubberbandPanel instance = new RubberbandPanel(manager, imodKey,
        borderLabel, buttonLabel, xMinTooltip, xMaxTooltip, yMinTooltip,
        zMinTooltip, zMaxTooltip, yMaxTooltip, run3dmodButton);
    instance.addListeners();
    return instance;
  }

  private void addListeners() {
    btnRubberband.addActionListener(new RubberbandActionListener(this));
  }

  Component getComponent() {
    return pnlRubberband;
  }
  
  Container getContainer() {
    return pnlRubberband;
  }

  void buttonAction(ActionEvent event) {
    String command = event.getActionCommand();
    if (command == btnRubberband.getActionCommand()) {
      setXYMinAndMax(manager.imodGetRubberbandCoordinates(imodKey, AxisID.ONLY));
    }
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
        return;
      }
    }
  }

  void setEnabled(boolean enable) {
    ltfXMin.setEnabled(enable);
    ltfXMax.setEnabled(enable);
    ltfYMin.setEnabled(enable);
    ltfYMax.setEnabled(enable);
    btnRubberband.setEnabled(enable);
  }

  void setVisible(boolean visible) {
    pnlRubberband.setVisible(visible);
  }

  public void getParameters(XYParam xyParam) {
    xyParam.setXMin(ltfXMin.getText());
    xyParam.setXMax(ltfXMax.getText());
    xyParam.setYMin(ltfYMin.getText());
    xyParam.setYMax(ltfYMax.getText());
  }
  
  public void getParameters(TrimvolParam trimvolParam) {
    trimvolParam.setXMin(ltfXMin.getText());
    trimvolParam.setXMax(ltfXMax.getText());
    trimvolParam.setYMin(ltfYMin.getText());
    trimvolParam.setYMax(ltfYMax.getText());
    trimvolParam.setZMin(ltfZMin.getText());
    trimvolParam.setZMax(ltfZMax.getText());
  }
  
  public void getParameters(ParallelMetaData metaData) {
    metaData.setXMin(ltfXMin.getText());
    metaData.setXMax(ltfXMax.getText());
    metaData.setYMin(ltfYMin.getText());
    metaData.setYMax(ltfYMax.getText());
    metaData.setZMin(ltfZMin.getText());
    metaData.setZMax(ltfZMax.getText());
  }
  
  public void setParameters(ParallelMetaData metaData) {
    ltfXMin.setText(metaData.getXMin());
    ltfXMax.setText(metaData.getXMax());
    ltfYMin.setText(metaData.getYMin());
    ltfYMax.setText(metaData.getYMax());
    ltfZMin.setText(metaData.getZMin());
    ltfZMax.setText(metaData.getZMax());
  }

  public void setParameters(XYParam xyParam) {
    ltfXMin.setText(xyParam.getXMin());
    ltfXMax.setText(xyParam.getXMax());
    ltfYMin.setText(xyParam.getYMin());
    ltfYMax.setText(xyParam.getYMax());
  }

  private void setToolTipText() {
    String text;
    ltfXMin.setToolTipText(xMinTooltip);
    ltfXMax.setToolTipText(xMaxTooltip);
    ltfYMin.setToolTipText(yMinTooltip);
    ltfYMax.setToolTipText(yMaxTooltip);
    btnRubberband.setToolTipText("After pressing the " + buttonLabel
        + " button, " + "press shift-B in the ZaP window.  "
        + "Create a rubberband around the contrast range.  "
        + "Then press this button to retrieve X and Y coordinates.");
  }

  private static final class RubberbandActionListener implements ActionListener {
    RubberbandPanel adaptee;

    RubberbandActionListener(RubberbandPanel panel) {
      adaptee = panel;
    }

    public void actionPerformed(ActionEvent event) {
      adaptee.buttonAction(event);
    }
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.4  2007/02/09 00:52:25  sueh
 * <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 * <p> classes.
 * <p>
 * <p> Revision 1.3  2006/08/16 22:41:46  sueh
 * <p> bug# 912 Make panel less flexible
 * <p>
 * <p> Revision 1.2  2006/08/16 18:51:56  sueh
 * <p> bug# 912 Making panel generic so it can be used for all places where a
 * <p> rubberband is used.
 * <p>
 * <p> Revision 1.1  2006/06/28 23:29:36  sueh
 * <p> bug# 881 Panel to get X and Y scaling range using a 3dmod rubberband.
 * <p> </p>
 */
