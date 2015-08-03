package etomo.process;

import java.io.File;
import java.io.FileNotFoundException;

import etomo.util.TestUtilites;
import junit.framework.TestCase;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2012 - 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
* 
* <p> $Log$ </p>
*/
public class ProcessMessagesTest extends TestCase {
  public void testWarnings() {
    ProcessMessages messages = ProcessMessages.getInstance(null);
    File file = new File(TestUtilites.INSTANCE.getUnitTestData(), "aligna.log");
    try {
      messages.addProcessOutput(file);
    }
    catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    messages.print(ProcessMessages.MessageType.WARNING);
    assertTrue("2 messages should be found",
      messages.size(ProcessMessages.MessageType.WARNING) == 2);
    assertTrue(
      "first message shouldn't contain extra lines",
      messages.get(ProcessMessages.MessageType.WARNING, 0).endsWith(
        "Minimization error #3 - Iteration limit exceeded"));
  }

  public void testErrors() {
    ProcessMessages messages = ProcessMessages.getInstance(null);
    File file = new File(TestUtilites.INSTANCE.getUnitTestData(), "testErrors.log");
    try {
      messages.addProcessOutput(file);
    }
    catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    messages.print(ProcessMessages.MessageType.WARNING);
    assertTrue(
      "3 errors should be found - prnstr('ERROR: & log.write('ERROR: should be ignored",
      messages.size(ProcessMessages.MessageType.ERROR) == 3);
    assertTrue(
      "ERROR: message should not contain extra lines when MultiLineMessages is off",
      messages.get(ProcessMessages.MessageType.ERROR, 0).endsWith("A. error line"));
    assertTrue(
      "Errno: message should not contain extra lines when MultiLineMessages is off",
      messages.get(ProcessMessages.MessageType.ERROR, 1).endsWith("C. error line"));
    assertTrue(
      "Traceback message should always contain extra lines",
      messages.get(ProcessMessages.MessageType.ERROR, 2).indexOf("F. second error line") != -1);
  }
}
