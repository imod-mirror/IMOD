package etomo;

import java.io.File;

import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.ui.JoinDialog;
import etomo.ui.MainJoinPanel;

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
* <p> Revision 1.1.2.4  2004/09/13 16:41:18  sueh
* <p> bug# 520 added isNewManager stub function
* <p>
* <p> Revision 1.1.2.3  2004/09/08 19:28:25  sueh
* <p> bug# 520 update call to BaseMAnager()
* <p>
* <p> Revision 1.1.2.2  2004/09/07 17:55:15  sueh
* <p> bug# 520 moved mainFrame show responsibility to EtomoDirector
* <p>
* <p> Revision 1.1.2.1  2004/09/03 21:03:27  sueh
* <p> bug# 520 adding place holders for create functions for now
* <p> </p>
*/
public class JoinManager extends BaseManager {
  public static  final String  rcsid =  "$Id$";
  
  //  Process dialog references
  private JoinDialog joinDialog = null;
  
  //convenience variable set equals to mainPanel
  //use through castMainPanel
  private MainJoinPanel mainJoinPanel = null; 
  
  public JoinManager(String paramFileName) {
    super();
    openJoinDialog();
  }
  
  public boolean isNewManager() {
    return true;
  }

  protected void createComScriptManager() {
    
  }
  
  protected void createProcessManager() {
    
  }
  
  public void openNewDataset() {
    
  }
  
  public void openExistingDataset(File paramFile) {
    
  }
  
  /**
   * Open the join dialog
   */
  public void openJoinDialog() {
    openProcessingPanel();
    if (joinDialog == null) {
      joinDialog = new JoinDialog(this);
    }
    mainPanel.showProcess(joinDialog.getContainer(), AxisID.ONLY);
  }
  
  /**
   *  
   */
  public void doneJoinDialog(AxisID axisID) {
    if (joinDialog == null) {
      mainPanel.openMessageDialog(
        "Can not update join without an active join dialog",
        "Program logic error");
      return;
    }
    isDataParamDirty = true;
    joinDialog = null;
  }

  
  protected void createMainPanel() {
    mainPanel = new MainJoinPanel(this);
  }
  
  private MainJoinPanel castMainPanel() {
    if (mainPanel == null) {
      throw new NullPointerException();
    }
    if (mainJoinPanel == null) {
      mainJoinPanel = (MainJoinPanel) mainPanel;
    }
    return mainJoinPanel;
  }
  
  /**
   * Open the main window in processing mode
   */
  private void openProcessingPanel() {
    mainPanel.showProcessingPanel(AxisType.SINGLE_AXIS);
    setPanel();
  }
}
