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
 * <p> Revision 2.1  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p> </p>
 */
public class ConstSolvematchmodParam {
  public static final String rcsid = "$Id$";
  protected String tofiducialCoordinatesFile;
  protected String fromfiducialCoordinatesFile;
  protected StringList fiducialMatchListA = new StringList(0);
  protected StringList fiducialMatchListB = new StringList(0);
  protected FortranInputString xAxistTilt = new FortranInputString(2);
  protected double residualThreshold;
  protected int nSurfaces;
  protected String outputTransformationFile;
  
}

