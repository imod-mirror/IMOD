package etomo.storage.autodoc;

/**
* <p>Description:</p>
*
* <p>Copyright: Copyright © 2002, 2003</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
*
* @author $$Author$$
*
* @version $$Revision$$
*
* <p> $$Log$
* <p> $Revision 1.1  2003/12/31 01:31:14  sueh
* <p> $bug# 372 simple iterator for sections, based on section type
* <p> $$ </p>
*/

public final class SectionLocation {
  public static final String rcsid = "$$Id$$";
  String type = null;
  int index = -1;
  
  SectionLocation(String type, int index) {
    this.type = type;
    this.index = index;
  }
  
  String getType() {
    return type;
  }
  
  int getIndex() {
    return index;
  }
  
  void setIndex(int index) {
    this.index = index;
  }
}
