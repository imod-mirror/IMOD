package etomo;

import java.io.File;

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
  
  public JoinManager(String paramFileName) {
    super();
  }
  
  public boolean isNewManager() {
    return false;
  }

  protected void createComScriptManager() {
    
  }
  
  protected void createProcessManager() {
    
  }
  
  public void openNewDataset() {
    
  }
  
  public void openExistingDataset(File paramFile) {
    
  }
  
  protected void createMainPanel() {
  }
  
}
