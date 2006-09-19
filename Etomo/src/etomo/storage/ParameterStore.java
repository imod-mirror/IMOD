package etomo.storage;

import java.io.*;
import java.util.*;

/*
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
 * <p> Revision 3.3  2006/09/13 23:30:09  sueh
 * <p> bug# 921 Preventing null pointer exception in save(Storable).
 * <p>
 * <p> Revision 3.2  2006/06/05 18:05:20  sueh
 * <p> bug# 766 Added save(Storable), to save a single Storable without overwriting the
 * <p> other Storables in the data file.
 * <p>
 * <p> Revision 3.1  2005/09/12 23:58:38  sueh
 * <p> bug# 532 Added save() to save a Storable class without overwriting
 * <p> preference entries from other Storable classes.  Added load(Storable) to
 * <p> load a single storable class.
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.2  2002/10/07 22:27:05  rickg
 * <p> removed unused imports
 * <p> reformat after emacs messed it up
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class ParameterStore {
  public static final String rcsid = "$Id$";

  private File paramFile;

  /**
   * Construct a ParameterStore using the File specified
   * @param paramFile a File object specifying where the parameters are stored
   * or to be stored.  <i>What happens when the file does not exist</i>
   */
  public ParameterStore(File paramFile) {
    this.paramFile = paramFile;
  }

  public void save(Storable storable) throws IOException {
    if (paramFile == null) {
      return;
    }
    //get the existing property values from paramFile
    FileInputStream inFile = new FileInputStream(paramFile);
    Properties props = new Properties();
    props.load(inFile);
    inFile.close();
    //let storable overwrite its values
    storable.store(props);
    //write the property values to paramFile
    FileOutputStream outFile = new FileOutputStream(paramFile);
    props.store(outFile, null);
    outFile.close();
  }

  /**
   * Save opens the given parameter file, collects the property key value
   * pairs from the array of storable objects and stores them to the file.
   * @param storableArray an array of storable objects that are iterated over
   * to collect the
   */
  public void save(Storable[] storableArray) throws IOException {
    //
    //  Open the parameter file
    //
    FileOutputStream outFile = new FileOutputStream(paramFile);

    //
    //  Collect the key/value pairs from the array of storable objects
    //
    Properties props = new Properties();
    for (int i = 0; i < storableArray.length; i++) {
      if (storableArray[i] != null) {
        storableArray[i].store(props);
      }
    }

    //
    //  Write out the key/value to the file
    //
    props.store(outFile, null);

    //
    //  Close the output stream
    //
    outFile.close();
  }

  /**
   * Save opens the given parameter file, collects the property key value
   * pairs from the array of storable objects and stores them to the file.
   * @param storableArray an array of storable objects that are iterated over
   * to collect the
   */
  public void save(Storable[] storableArray, int numberStorablesExpected)
      throws IOException {
    Properties props = null;
    if (storableArray.length < numberStorablesExpected) {
      //if not all the storables are being saved, don't overwrite the ones that
      //are not being saved.
      //
      //  Open the parameter file
      //
      FileInputStream inFile = new FileInputStream(paramFile);
      //
      //  Load the key/value pairs into the properties object
      //
      props = new Properties();
      props.load(inFile);
      inFile.close();
    }
    //
    //  Open the parameter file
    //
    FileOutputStream outFile = new FileOutputStream(paramFile);

    //
    //  Collect the key/value pairs from the array of storable objects
    //
    if (props == null) {
      props = new Properties();
    }
    for (int i = 0; i < storableArray.length; i++) {
      storableArray[i].store(props);
    }

    //
    //  Write out the key/value to the file
    //
    props.store(outFile, null);

    //
    //  Close the output stream
    //
    outFile.close();
  }

  /**
   * Load in the stored property key value pairs and send them to the
   * storable object.
   */
  public void load(Storable storable) throws IOException {
    if (paramFile == null) {
      return;
    }
    //
    //  Open the parameter file
    //
    FileInputStream inFile = new FileInputStream(paramFile);
    //
    //  Load the key/value pairs into the properties object
    //
    Properties props = new Properties();
    props.load(inFile);
    inFile.close();
    //
    //  Send the key/value pairs to the storable object
    //
    storable.load(props);
  }

  /**
   * Load in the stored property key value pairs and send them to the array of
   * storable objects.
   */
  public void load(Storable[] storableArray) throws IOException {
    //
    //  Open the parameter file
    //
    FileInputStream inFile = new FileInputStream(paramFile);
    //
    //  Load the key/value pairs into the properties object
    //
    Properties props = new Properties();
    props.load(inFile);
    inFile.close();

    //
    //  Send the key/value pairs to the array of storable objects
    //
    for (int i = 0; i < storableArray.length; i++) {
      if (storableArray[i] == null) {
        throw new NullPointerException("Storable index " + i
            + " cannot be null.");
      }
      storableArray[i].load(props);
    }
  }
}
