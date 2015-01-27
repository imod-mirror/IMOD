package etomo.storage.autodoc;

import etomo.storage.LogFile;

import java.io.IOException;

/**
 * <p>Description:Allows a statement to be part of another statement list, without being
 * removed from its original statement list.  GraftedStatement uses functionality from
 * from its super-class Statement to attach to a statement list.
 * </p>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public class GraftedStatement extends Statement {
  private final Statement statement;

  public GraftedStatement(final Statement previousStatement, final Statement statement) {
    super(previousStatement);
    this.statement = statement;
  }

  /**
   * Removes the original statement from its original list and from the list it was
   * grafted onto.
   * @return
   */
  WritableStatement remove() {
    statement.remove();
    return super.remove();
  }

  public String toString(){
    return statement.toString()+ " (grafted)";
  }

  void write(LogFile file, LogFile.WriterId writerId)
      throws LogFile.LockException, IOException {
    statement.write(file, writerId);
  }

  void print(int level) {
    statement.print(level);
  }

  public Statement.Type getType() {
    return statement.getType();
  }

  public String getString() {
    return statement.getString();
  }

  public int sizeLeftSide() {
    return statement.sizeLeftSide();
  }

  public String getLeftSide(int index) {
    return statement.getLeftSide(index);
  }

  public String getRightSide() {
    return statement.getRightSide();
  }

  public ReadOnlySection getSubsection() {
    return statement.getSubsection();
  }

  public String getLeftSide() {
    return statement.getLeftSide();
  }
}
