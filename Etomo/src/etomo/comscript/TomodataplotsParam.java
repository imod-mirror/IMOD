package etomo.comscript;

import java.util.ArrayList;
import java.util.List;

import etomo.BaseManager;
import etomo.TaskInterface;
import etomo.type.AxisID;
import etomo.type.FileType;
import etomo.type.ProcessName;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2013</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public final class TomodataplotsParam {
  public static final String rcsid = "$Id:$";

  private Task task = null;

  public void setTask(final TaskInterface input) {
    if (input instanceof Task) {
      task = (Task) input;
    }
    else {
      task = null;
    }
  }

  public List<String> getCommandArray(final BaseManager manager, final AxisID axisID) {
    List<String> command = new ArrayList<String>();
    command.add("python");
    command.add("-u");
    command.add(BaseManager.getIMODBinPath() + ProcessName.TOMODATAPLOTS.toString());
    command.add("-background");
    if (task != null) {
      if (task.typeOfDataToPlot != null) {
        command.add("-TypeOfDataToPlot");
        command.add(task.typeOfDataToPlot.value);
      }
      if (task.inputFile != null) {
        command.add("-InputFile");
        command.add(task.inputFile.getFileName(manager, axisID));
      }
      if (task.xaxisLabel != null) {
        command.add("-XaxisLabel");
        command.add(task.xaxisLabel);
      }
    }
    return command;
  }

  private static final class TypeOfDataToPlot {
    private static final TypeOfDataToPlot STATS_MIN_MAX = new TypeOfDataToPlot("13");
    private static final TypeOfDataToPlot BLEND_MEAN_MAX = new TypeOfDataToPlot("4");

    private final String value;

    private TypeOfDataToPlot(final String value) {
      this.value = value;
    }
  }

  public static final class Task implements TaskInterface {
    public static final Task STATS_MIN_MAX = new Task("Plot min/max",
        TomodataplotsParam.TypeOfDataToPlot.STATS_MIN_MAX, FileType.STATS_LOG,
        "View number in raw stack ");
    public static final Task STATS_FIXED_MIN_MAX = new Task("Plot fixed min/max",
        TomodataplotsParam.TypeOfDataToPlot.STATS_MIN_MAX, FileType.FIXED_STATS_LOG,
        "View number in fixed stack ");
    public static final Task BLEND_MEAN_MAX = new Task("plot edge errors",
        TomodataplotsParam.TypeOfDataToPlot.BLEND_MEAN_MAX, FileType.CROSS_CORRELATION_LOG,
        "View number in coarse aligned stack ");

    private final String label;
    private final TomodataplotsParam.TypeOfDataToPlot typeOfDataToPlot;
    private final FileType inputFile;
    private final String xaxisLabel;

    private Task(final String label,
        final TomodataplotsParam.TypeOfDataToPlot typeOfDataToPlot,
        final FileType inputFile, final String xaxisLabel) {
      this.label = label;
      this.typeOfDataToPlot = typeOfDataToPlot;
      this.inputFile = inputFile;
      this.xaxisLabel = xaxisLabel;
    }

    public boolean okToDrop() {
      return true;
    }

    public String toString() {
      return label;
    }
  }
}
