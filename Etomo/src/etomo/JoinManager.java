package etomo;

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
* <p> $Log$ </p>
*/
public class JoinManager extends BaseManager {
  public static  final String  rcsid =  "$Id$";
  
  public JoinManager(String paramFileName) {
    super(paramFileName);
    if (!test) {
      mainFrame.pack();
      mainFrame.show();
    }
  }
  
  protected void createComScriptManager() {
    
  }
  
  protected void createMainFrame() {
    
  }
  
  protected void createProcessManager() {
    
  }
}
