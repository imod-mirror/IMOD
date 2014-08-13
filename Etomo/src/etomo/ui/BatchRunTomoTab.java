package etomo.ui;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2014</p>
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
public final class BatchRunTomoTab {
  public static final String rcsid = "$Id:$";
  public static final BatchRunTomoTab BATCH = new BatchRunTomoTab(0, "Batch Parameters");
  public static final BatchRunTomoTab STACKS = new BatchRunTomoTab(1, "Image Stacks");
  public static final BatchRunTomoTab DATASET = new BatchRunTomoTab(2, "Dataset Values");
  public static final BatchRunTomoTab RUN = new BatchRunTomoTab(3, "Run");

  public static final int SIZE = RUN.index + 1;

  public static final BatchRunTomoTab DEFAULT = BATCH;

  private final int index;
  private final String title;

  private BatchRunTomoTab(final int index, final String title) {
    this.index = index;
    this.title = title;
  }

  public static BatchRunTomoTab getInstance(final int index) {
    if (index == BATCH.index) {
      return BATCH;
    }
    if (index == STACKS.index) {
      return STACKS;
    }
    if (index == DATASET.index) {
      return DATASET;
    }
    if (index == RUN.index) {
      return RUN;
    }
    return DEFAULT;
  }

  public String getTitle() {
    return title;
  }

  public int getIndex() {
    return index;
  }
}
