package etomo.storage;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.process.SystemProgram;
import etomo.type.AxisID;
import etomo.type.EtomoNumber;
import etomo.util.EnvironmentVariable;

/**
 * <p>Description: Represents the queues and computers available in the local
 * network.  Uses the cpu.adoc file, the IMOD_PROCESSORS environment variable,
 * and the Settings dialog (saved to the .etomo file).</p>
 * 
 * <p>Copyright: Copyright 2009 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 * 
 * <p> $Log$
 * <p> Revision 1.3  2011/02/22 04:51:43  sueh
 * <p> bug# 1437 Reformatting.
 * <p>
 * <p> Revision 1.2  2010/02/17 04:49:31  sueh
 * <p> bug# 1301 Using the manager instead of the manager key do pop up
 * <p> messages.
 * <p>
 * <p> Revision 1.1  2010/01/11 23:56:58  sueh
 * <p> bug# 1299 Removed responsibility anything other then cpu.adoc from
 * <p> CpuAdoc.  Placed responsibility for information about the network in the
 * <p> Network class.
 * <p> </p>
 */
public final class Network {
  private Network() {}

  /**
   * Gets the host name from the current host computer.
   * Calls hostname.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static String getLocalHostName(BaseManager manager, AxisID axisID,
    String propertyUserDir) {
    SystemProgram hostname =
      new SystemProgram(manager, propertyUserDir, new String[] { "python",
        BaseManager.getIMODBinPath() + "b3dhostname" }, axisID);
    hostname.run();
    String[] stdout = hostname.getStdOutput();
    if (stdout == null || stdout.length < 1) {
      return null;
    }
    return stdout[0];
  }

  /**
   * Parallel processing is set outside of Etomo if the cpu.adoc exists, or if
   * the IMOD_PROCESSORS environment variable is set.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static boolean isParallelProcessingSetExternally(BaseManager manager,
    AxisID axisID, String propertyUserDir) {
    if (CpuAdoc.INSTANCE.exists()) {
      return true;
    }
    return EnvironmentVariable.INSTANCE.exists(manager, propertyUserDir,
      "IMOD_PROCESSORS", axisID);
  }

  /**
   * Parallel processing is enabled if the cpu.adoc contains one or more
   * Computer or Queue sections, or if the IMOD_PROCESSORS environment variable
   * is set, or if parallel processing was turned on in the Settings dialog.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static boolean isParallelProcessingEnabled(BaseManager manager, AxisID axisID,
    String propertyUserDir) {
    if (!CpuAdoc.INSTANCE.isComputerListEmpty() || !CpuAdoc.INSTANCE.isQueueListEmpty()) {
      return true;
    }
    EtomoNumber imodProcessors = new EtomoNumber();
    imodProcessors.set(EnvironmentVariable.INSTANCE.getValue(manager, propertyUserDir,
      "IMOD_PROCESSORS", axisID));
    if (!imodProcessors.isNull() && imodProcessors.isValid()) {
      return true;
    }
    return EtomoDirector.INSTANCE.getUserConfiguration().isParallelProcessing();
  }

  /**
   * GPU processing is set outside of Etomo if the cpu.adoc exists.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static boolean isGpuProcessingSetExternally() {
    return CpuAdoc.INSTANCE.exists();
  }

  /**
   * Returns true if there are non-local-only GPU entries in the computer list.
   * @param manager
   * @param axisID
   * @param propertyUserDir
   * @return
   */
  public static boolean isNonLocalOnlyGpuProcessingEnabled() {
    return !CpuAdoc.INSTANCE.isGpuComputerListEmpty(null);
  }

  /**
   * Returns true if GPU processing is enabled in the cpu.adoc section for the
   * current computer.  If cpu.adoc is not in use, returns true if GPU
   * processing has been turned on in the Settings dialog.  Otherwise returns
   * false.
   * Side effect:  May create Node.LOCAL_HOST_INSTANCE.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static boolean isLocalHostGpuProcessingEnabled(BaseManager manager,
    AxisID axisID, String propertyUserDir) {
    Node localHost;
    if (CpuAdoc.INSTANCE.exists()) {
      localHost = CpuAdoc.INSTANCE.getLocalHostComputer(manager, axisID, propertyUserDir);
    }
    else {
      if (Node.LOCAL_HOST_INSTANCE == null) {
        Node.createLocalInstance(manager, axisID, propertyUserDir);
      }
      localHost = Node.LOCAL_HOST_INSTANCE;
    }
    if (localHost == null) {
      return false;
    }
    // Get gpu from the section in cpu.adoc belonging to the current computer.
    return localHost.isGpu();
  }

  /**
   * Returns false if cpu.adoc is missing, or if the only gpu enabled computer in cpu.adoc
   * is the local host.
   * @param manager
   * @param axisID
   * @param propertyUserDir
   * @return
   */
  public static boolean isNonLocalHostGpuProcessingEnabled(final BaseManager manager,
    final AxisID axisID, final String propertyUserDir) {
    if (!CpuAdoc.INSTANCE.exists()) {
      return false;
    }
    return !CpuAdoc.INSTANCE.isGpuComputerListEmpty(CpuAdoc.INSTANCE
      .getLocalHostComputer(manager, axisID, propertyUserDir));
  }

  /**
   * If cpu.adoc is in use, returns true if it contains 1 or more Computer
   * sections.  If cpu.adoc is not in use, then check the IMOD_PROCESSORS
   * environment and then the parallel processing checkbox the Settings dialog. 
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static boolean hasComputers(BaseManager manager, AxisID axisID,
    String propertyUserDir) {
    if (CpuAdoc.INSTANCE.exists()) {
      return !CpuAdoc.INSTANCE.isComputerListEmpty();
    }
    if (EnvironmentVariable.INSTANCE.exists(manager, propertyUserDir, "IMOD_PROCESSORS",
      axisID) || EtomoDirector.INSTANCE.getUserConfiguration().isParallelProcessing()) {
      return true;
    }
    return false;
  }

  /**
   * Returns the number of Computer sections in cpu.adoc.  If cpu.adoc is not
   * in use, returns 1.  Will be 1 even when isParallelProcessingEnabled()
   * returns false.  This allows processes that require parallel processing to
   * be used when parallel processing wasn't set up.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static int getNumComputers() {
    if (CpuAdoc.INSTANCE.exists()) {
      return CpuAdoc.INSTANCE.getComputerListSize();
    }
    else {
      // Count Node.LOCAL_INSTANCE if cpu.adoc is missing or has no computers.
      return 1;
    }
  }

  /**
   * Returns a Computer section from cpu.adoc by index, or returns
   * Node.LOCAL_HOST_INSTANCE if cpu.adoc is not in use and the index is 0.
   * Otherwise returns null.
   * Side effect:  may create Node.LOCAL_HOST_INSTANCE.
   * @param index
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static Node getComputer(BaseManager manager, int index, AxisID axisID,
    String propertyUserDir) {
    if (CpuAdoc.INSTANCE.exists()) {
      return CpuAdoc.INSTANCE.getComputer(index);
    }
    if (index == 0) {
      if (Node.LOCAL_HOST_INSTANCE == null) {
        Node.createLocalInstance(manager, axisID, propertyUserDir);
      }
      return Node.LOCAL_HOST_INSTANCE;
    }
    return null;
  }

  /**
   * Returns true if there are Queue sections in the cpu.adoc.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static boolean hasQueues() {
    return !CpuAdoc.INSTANCE.isQueueListEmpty();
  }

  /**
   * Gets the number of Queue sections in cpu.adoc.
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static int getNumQueues() {
    return CpuAdoc.INSTANCE.getQueueListSize();
  }

  /**
   * Gets a Queue section from cpu.adoc by name, or returns null.
   * @param name
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static Node getQueue(String name) {
    return CpuAdoc.INSTANCE.getQueue(name);
  }

  /**
   * Gets a Queue section from cpu.adoc by index, or returns null.
   * @param index
   * @param axisID
   * @param propertyUserDir
   * @param managerKey
   * @return
   */
  public static Node getQueue(int index) {
    return CpuAdoc.INSTANCE.getQueue(index);
  }
}
