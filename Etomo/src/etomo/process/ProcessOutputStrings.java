package etomo.process;

final class ProcessOutputStrings {
  //batchruntomo
  public static final String BRT_DATASET_TAG = "Starting data set";
  public static final String BRT_ETOMO_TAG = "starting eTomo with log in";
  public static final String BRT_STEP_TAG = "(running ";
  public static final String BRT_COM_TAG = ".com";
  public static final String BRT_STEP_SUCCESS_TAG = "Successfully finished";
  public static final String BRT_DATASET_SUCCESS_TAG = "Completed dataset";
  public static final String BRT_TIME_STAMP_TAG = " at ";
  public static final String BRT_SUCCESS_TAG = "SUCCESSFULLY COMPLETED";
  public static final String BRT_BATCH_RUN_TOMO_ERROR_TAG = "ERROR: batchruntomo -";
  public static final String BRT_ALT_ERROR_TAG = "ABORT SET:";
  public static final String[] BRT_LOG_TAGS = new String[] { "Final align -",
    "Starting axis B", "AUTOPATCHFIT - Refinematch found a good ",
    "AUTOPATCHFIT - Findwarp found a good " };
  public static final String BRT_KILLING_TAG = "RECEIVED SIGNAL TO QUIT, JUST EXITING";
  public static final String BRT_PAUSED_TAG = "Exiting after finishing dataset as requested";
  public static final String BRT_ID_TAG = "Beginning to process directive file";
}
