package etomo.storage.autodoc;

import etomo.storage.LogFile;

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
 * 
 * <p> $Log$
 * <p> Revision 1.1  2007/04/09 20:32:28  sueh
 * <p> bug# 964 Changed NameValuePair to an abstract class called Statement and
 * <p> child classes representing name/value pair, comment, empty line, and
 * <p> subsection.  Made delimiter change an attribute of the name/value pair class.
 * <p> Added ReadOnlyStatement to provide a public interface for Statement classes.
 * <p> </p>
 */
final class EmptyLine extends Statement {
  public static final String rcsid = "$Id$";

  private static final Type TYPE = Statement.Type.EMPTY_LINE;
  
  private final WriteOnlyStatementList parent;
  
  EmptyLine(WriteOnlyStatementList parent,Statement previousStatement){
    super(previousStatement);
    this.parent=parent;
  }
  
  public Statement.Type getType(){
    return TYPE;
  }
  
  public String getString() {
    return "";
  }
  
  public int sizeLeftSide() {
    return 0;
  }
  
  public String getLeftSide(int index) {
    return null;
  }
  
  public String getRightSide() {
    return "";
  }

  void write(LogFile file, long writeId) throws LogFile.WriteException {
    file.newLine(writeId);
  }

  void print(int level) {
    Autodoc.printIndent(level);
    System.out.println("<empty-line>");
  }
}
