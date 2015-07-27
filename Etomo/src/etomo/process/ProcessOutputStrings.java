package etomo.process;

/**
* <p>Description: Class to hold all strings that etomo needs to recognize that come from
* the output of IMOD applications.</p>
* 
* <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
final class ProcessOutputStrings {
  static final String START_PARAMETERS_TAG = "*** Entries to program batchruntomo ***";
  static final String END_PARAMETERS_TAG = "*** End of entries ***";
  // batchruntomo
  static final String BRT_DATASET_TAG = "Starting data set";
  static final String BRT_ETOMO_TAG = "starting eTomo with log in";
  static final String BRT_STEP_TAG = "(running ";
  static final String BRT_COM_TAG = ".com";
  static final String BRT_STEP_SUCCESS_TAG = "Successfully finished";
  static final String BRT_DATASET_SUCCESS_TAG = "Completed dataset";
  static final String BRT_TIME_STAMP_TAG = " at ";
  static final String BRT_SUCCESS_TAG = "SUCCESSFULLY COMPLETED";
  static final String BRT_BATCH_RUN_TOMO_ERROR_TAG = "ERROR: batchruntomo -";
  static final String BRT_ALT_ERROR_TAG = "ABORT SET:";
  static final String BRT_AXIS_B_TAG = "Starting axis B";
  static final String[] BRT_LOG_TAGS = new String[] { "Final align -",
    "AUTOPATCHFIT - Refinematch found a good ", "AUTOPATCHFIT - Findwarp found a good " };
  static final String BRT_KILLING_TAG = "RECEIVED SIGNAL TO QUIT, JUST EXITING";
  static final String BRT_PAUSED_TAG = "Exiting after finishing dataset as requested";
  static final String BRT_STARTING_DATASET_TAG = "Beginning to process directive file";
  static final String BRT_REACHED_STEP_TAG = "Reached step";
  static final String BRT_ABORT_SET_TAG = "ABORT SET:";
  static final String BRT_ABORT_AXIS_TAG = "ABORT AXIS:";
  static final String BRT_ENDING_STEP_PARAM_TAG = "EndingStep =";
  static final String BRT_STARTING_STEP_PARAM_TAG = "StartingStep =";
  static final String BRT_DELIVERED_TAG="Delivered stack";
  static final String BRT_RENAMED_TAG = "Renamed stack from";
  static final String BRT_FILE_TAG= "to:";
  static final String BRT_ABORT_TAG="ABORT";
}
