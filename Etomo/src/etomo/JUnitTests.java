package etomo;

import java.io.File;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestSuite;
import etomo.comscript.ComScriptTests;
import etomo.process.ProcessTests;
import etomo.type.TypeTests;
import etomo.util.UtilTests;

/**
* <p>Description: Collection of all test suites</p>
* 
* <p>Copyright: Copyright (c) 2005</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
*/
public class JUnitTests {
  public static  final String  rcsid =  "$Id$";
  
  public static final File TEST_ROOT_DIR = new File(EtomoDirector.getInstance()
      .getCurrentTestManager().getPropertyUserDir(), "JUnitTests");
  
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for etomo");
    
    TestSuite testSuite;
    Enumeration tests;
    Object test;
    
    //$JUnit-BEGIN$
    //$JUnit-END$
    
    testSuite = (TestSuite) TypeTests.suite();
    tests = testSuite.tests();
    while (tests.hasMoreElements()) {
      test = tests.nextElement();
      if (test instanceof Test) {
        suite.addTest((Test) test);
      }
    }
   
    testSuite = (TestSuite) ComScriptTests.suite();
    tests = testSuite.tests();
    while (tests.hasMoreElements()) {
      test = tests.nextElement();
      if (test instanceof Test) {
        suite.addTest((Test) test);
      }
    }

    testSuite = (TestSuite) ProcessTests.suite();
    tests = testSuite.tests();
    while (tests.hasMoreElements()) {
      test = tests.nextElement();
      if (test instanceof Test) {
        suite.addTest((Test) test);
      }
    }

    testSuite = (TestSuite) UtilTests.suite();
    tests = testSuite.tests();
    while (tests.hasMoreElements()) {
      test = tests.nextElement();
      if (test instanceof Test) {
        suite.addTest((Test) test);
      }
    }
    
    return suite;
  }
}
/**
* <p> $Log$ </p>
*/