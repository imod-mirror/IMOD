package etomo.ui;

import java.io.File;

import javax.swing.JPanel;

import etomo.EtomoDirector;
import etomo.JoinManager;
import etomo.type.AxisID;
import etomo.type.JoinMetaData;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2002, 2003, 2004</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$
* <p> Revision 1.1.2.2  2004/09/15 22:42:21  sueh
* <p> bug# 520 added castManger(), overrode createAxisPanelA and B
* <p>
* <p> Revision 1.1.2.1  2004/09/08 20:09:02  sueh
* <p> bug# 520 MainPanel for Join
* <p> </p>
*/

public class MainJoinPanel extends MainPanel {
  public static  final String  rcsid =  "$Id$";
  
  /**
   * @param joinManager
   */
  public MainJoinPanel(JoinManager joinManager) {
    super(joinManager);
  }
  
  protected void createAxisPanelA(AxisID axisID) {
    axisPanelA = new JoinProcessPanel((JoinManager) manager, axisID);
  }

  protected void createAxisPanelB() {
  }
  
  /**
   * Open the setup panel
   */
  public void openPanel(JPanel panel) {
    scrollA.add(panel);
    revalidate();
    EtomoDirector.getMainFrame().pack();
  }

  /**
   * Set the status bar with the file name of the data parameter file
   */
  public void updateDataParameters(File paramFile, JoinMetaData joinMetaData) {
    StringBuffer buffer = new StringBuffer();
    if (joinMetaData == null) {
      buffer.append("No data set loaded");
    }
    else {
      if (paramFile == null) {
        buffer.append("Data file: NOT SAVED");
      }
      else {
        buffer.append("Data file: " + paramFile.getAbsolutePath());
      }
      buffer.append("   Axis type: ");
      buffer.append(joinMetaData.getAxisType().toString());
    }
    statusBar.setText(buffer.toString());
  }
}
