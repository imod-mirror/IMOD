package etomo.util;

import junit.framework.Test;
import junit.framework.TestSuite;

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
 * <p> Revision 1.1  2002/10/03 19:16:10  rickg
 * <p> Initial revision
 * <p> </p>
 */
public class AllTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(AllTests.class);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for etomo.util");
    //$JUnit-BEGIN$
    suite.addTest(new TestSuite(CircularBufferTest.class));
    suite.addTest(new TestSuite(MRCHeaderTest.class));
    //$JUnit-END$
    return suite;
  }
}