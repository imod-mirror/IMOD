package etomo.comscript;
/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */

public class ConstNewstParam {
  public static final String rcsid =
    "$Id$";

  protected String inputFile = "";

  protected String outputFile = "";

  protected boolean useTransformFile = false;
  protected String transformFile = "";

  protected boolean useSize = false;
  protected String size = "";

  protected boolean useOffset = false;
  protected String offset = "";

  public ConstNewstParam() {
  }

  public String getInputFile() {
    return inputFile;
  }

  public String getOutputFile() {
    return outputFile;
  }

  public String getTransformFile() {
    return transformFile;
  }

  public String getSize() {
    return size;
  }

  public String getOffset() {
    return offset;
  }
}
