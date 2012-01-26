/*
 *  Main class for processchunks - an application to process command files on
 *  multiple machines.
 *
 *  Author: Sue Held
 *
 *  Copyright (C) 2010,2011 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 *
 *  $Id: processchunks.cpp,v e5ab12a1256a 2011/08/23 05:58:24 sueh 
 */

#include "processchunks.h"
#include "parse_params.h"
#include <signal.h>
#include <locale.h>
#include <QTimer>
#include <QSet>
#include <QDirIterator>

#ifndef _WIN32
#include <sys/select.h>
#include <sys/time.h>
#endif

static const int arraySize = 2;
//Using a shorter sleep time then the processchunks script and not adjusting
//the sleep depending on the number of machines.
static const int sleepMillisec = 100;
static const int maxLocalByNum = 32;
//converting old timeout counter to milliseconds
static const int runProcessTimeout = 30 * 2 * 1000;
static const int numOptions = 14;
static const int checkFileReconnectReset = 10;

static char *commandName = "processchunks";
using namespace std;
/* Fallbacks from    ../manpages/autodoc2man 2 0 processchunks
 * cd manpages
 make autodoc2man
 */
static char
    *options[] =
        {
            ":help:B:Print usage message",
            ":r:B:Resume, retaining existing finished files (the default is to remove all log files and redo everything)",
            ":s:B:Run a single command file named root_name or root_name.com",
            ":g:B:Go process, without asking for confirmation after probing machines",
            ":n:I:Set the \"nice\" value to the given number (default 18, range 0-19).  No effect when running on a queue.",
            ":w:FN:Full path to working directory on remote machines",
            ":d:I:Drop a machine after given number of failures in a row (default 5)",
            ":e:I:Quit after the given # of processing errors for a chunk (default 5)",
            ":c:FN:Check file \"name\" for commands P, Q, and D (default processchunks.input)",
            ":q:I:Run on cluster queue with given maximum # of jobs at once",
            ":Q:CH:Machine name to use for the queue (default queue)",
            //":m:I:Milliseconds to pause before killing each process (default 50)",
            ":P:B:Output process ID",
            ":v:B:Verbose.",
            ":V:CH:?|?,2|class[,function[,...]][,2]|2  Verbose instructions:  case insensitive, matches from the end of the class or function name." };
static char *queueNameDefault = "queue";
Processchunks *processchunksInstance;

int main(int argc, char **argv) {
  Processchunks pc(argc, argv);
  setlocale(LC_NUMERIC, "C");
  processchunksInstance = &pc;
  pc.printOsInformation();
  pc.loadParams(argc, argv);
  pc.setup();
  if (!pc.askGo()) {
    return 0;
  }
  exit(pc.startLoop());
}

Processchunks::Processchunks(int &argc, char **argv) :
  QCoreApplication(argc, argv) {
  //initialize member variables
  mOutStream = new QTextStream(stdout);
  mRemoteDir = NULL;
  mNice = 18;
  mMillisecSleep = 50;
  mDropCrit = 5;
  mMaxChunkErr = 5;
  mRetain = 0;
  mVerbose = 0;
  mJustGo = 0;
  mSkipProbe = false;
  mQueue = 0;
  mSingleFile = 0;
  mSshOpts.append("-o PreferredAuthentications=publickey");
  mSshOpts.append("-o StrictHostKeyChecking=no");
  mRootName = NULL;
  mQueueName = queueNameDefault;
  mCheckFile = NULL;
  mCopyLogIndex = -1;
  mAns = ' ';
  mTimerId = 0;
  mKill = false;
  mKillCounter = 0;
  mLsProcess = new QProcess(this);
  mVmstocsh = new QProcess(this);
  mDecoratedClassName = typeid(*this).name();
  mNumKills = 0;
  mMachineListSize = 0;
  mNumMachinesDropped = 0;
  mMaxKills = 0;
  mCheckFileReconnect = checkFileReconnectReset;
}

Processchunks::~Processchunks() {
  if (mRootName != NULL) {
    free(mRootName);
  }
  delete mLsProcess;
  delete mRemoteDir;
  delete mCheckFile;
  delete mComFileJobs;
  delete mVmstocsh;
  delete mOutStream;
  mOutStream = NULL;
  delete[] mMachineList;
  mSaveCheckFileLines.clear();
}

void Processchunks::printOsInformation() {
  printf("\nIMPORTANT:  Ctrl-C does not work with this version of processchunks.  Use ");
#ifndef _WIN32
  printf("<Esc> <Enter> or ");
#endif
  printf("the -c option (-c defaults to processchunks.input).\n\n");
}

//Print usage statement
//Not implementing $IMOD_ALL_MACHINES since no one seems to have used it.
void processchunksUsageHeader(char *pname) {
  printf("\nUsage: %s [Options] machine_list root_name\nWill process multiple command "
    "files on multiple processors or machines\nmachine_list is a list of "
    "available machines, separated by commas.\nList machines names multiple "
    "times to gain access to multiple CPUs on a machine.\nRoot_name is "
    "the base name of the command files, omitting -nnn.com\n\n", pname);
  imodUsageHeader(pname);
}

//Loads parameters, does error checking, returns zero if parameters are correct.
//Prints an error message to stdout and returns non-zero if the parameters are
//incorrect.
void Processchunks::loadParams(int &argc, char **argv) {
  int numOptArgs, numNonOptArgs;
  PipReadOrParseOptions(argc, argv, options, numOptions, commandName, 2, 2, 0,
      &numOptArgs, &numNonOptArgs, processchunksUsageHeader);
  PipGetBoolean("r", &mRetain);
  PipGetBoolean("s", &mSingleFile);
  PipGetBoolean("g", &mJustGo);
  PipGetInteger("n", &mNice);
  //PipGetInteger("m", &mMillisecSleep);
  char *remoteDir = NULL;
  PipGetString("w", &remoteDir);
  if (remoteDir != NULL) {
    mRemoteDir = new QString(remoteDir);
  }
  PipGetInteger("d", &mDropCrit);
  PipGetInteger("e", &mMaxChunkErr);
  char *checkFile = NULL;
  PipGetString("c", &checkFile);
  if (checkFile != NULL) {
    mCheckFile = new QFile(checkFile);
    free(checkFile);
  }
  else {
    mCheckFile = new QFile("processchunks.input");
  }
  PipGetBoolean("v", &mVerbose);
  if (mVerbose) {
    char *verboseClassFunctions = NULL;
    //Set verbose instructions.
    PipGetString("V", &verboseClassFunctions);
    if (verboseClassFunctions != NULL) {
      QString param(verboseClassFunctions);
      QStringList paramList = param.trimmed().split(",", QString::SkipEmptyParts);
      if (!paramList.isEmpty()) {
        //Set verbosity level.
        bool ok;
        int temp = paramList.at(paramList.size() - 1).toInt(&ok);
        if (ok) {
          mVerbose = temp;
          paramList.removeAt(paramList.size() - 1);
        }
        bool help = false;
        if (!paramList.isEmpty()) {
          //Set the verbose class.
          mVerboseClass = paramList.at(0);
          paramList.removeAt(0);
          if (mVerboseClass == "?") {
            help = true;
            //If the param is a question mark, print messages from Processchunks::isVerbose.
            mVerboseClass = "processchunks";
            mVerboseFunctionList.append("isverbose");
          }
        }
        if (!help && !paramList.isEmpty()) {
          //Set the verbose function list.
          mVerboseFunctionList = paramList;
        }
      }
    }
  }
  if (!PipGetInteger("q", &mQueue)) {
    mSkipProbe = true;
    mJustGo = 1;
  }
  PipGetString("Q", &mQueueName);
  int returnPid = 0;
  PipGetBoolean("P", &returnPid);
  if (returnPid) {
    mSkipProbe = true;
    pidToStderr();
  }
  char *cpuList = NULL;
  PipGetNonOptionArg(0, &cpuList);
  if (cpuList != NULL) {
    mCpuList = cpuList;
    free(cpuList);
  }
  PipGetNonOptionArg(1, &mRootName);
  //Error check
  if (mRetain && mSingleFile) {
    exitError("You cannot use the retain option with a single command file");
  }
}

//Setup mSshOpts, mCpuArray, mProcessArray, mHostRoot, mRemoteDir.  Probe
//machines.
void Processchunks::setup() {
  setupSshOpts();
  //Get current directory if the -w option was not used
  if (mRemoteDir == NULL) {
    mRemoteDir = new QString(mCurrentDir.absolutePath().toLatin1().data());
  }
  QStringList machineNameList;
  int *numCpusList = initMachineList(machineNameList);
  setupHostRoot();
  setupComFileJobs();
  probeMachines(machineNameList);
  setupMachineList(machineNameList, numCpusList);
  machineNameList.clear();
  delete[] numCpusList;
}

//Setup mFlags.  Find first not-done log file.  Delete log files and
//miscellaneous files.  Listen for ctrl-C.  Run event loop.
int Processchunks::startLoop() {
  int i;
  //Prescan logs for done ones to find first undone one, or back up unfinished
  mNumDone = 0;
  mFirstUndoneIndex = -1;
  ProcessHandler process;
  process.setup(*this);
  for (i = 0; i < mSizeJobArray; i++) {
    process.setJob(i);
    if (process.logFileExists(false)) {
      if (process.isChunkDone()) {
        //If it was done and we are resuming, set flag it is done, count
        if (mRetain) {
          process.setFlag(CHUNK_DONE);
          mNumDone++;
        }
      }
      else if (!mRetain) {
        //If it was not done and we are restarting, back up the old log
        process.backupLog();
      }
    }
    //If resuming and this is the first undone one, keep track of that
    if (mRetain && mFirstUndoneIndex == -1 && process.getFlag() != CHUNK_DONE) {
      mFirstUndoneIndex = i;
    }
    //OLD:remove logs if not restarting
    //remove logs if not resuming
    if (!mRetain) {
      process.removeFiles();
    }
    process.invalidateJob();
  }
  if (mFirstUndoneIndex == -1) {
    mFirstUndoneIndex = 0;
  }

  if (!mSingleFile || mSkipProbe) {
    *mOutStream << mNumDone << " OF " << mSizeJobArray << " DONE SO FAR " << endl;
  }
  //Initialize variables needed by the timer event function
  mLastNumDone = 0;
  mPausing = false;
  mSyncing = 0;
  mAnyDone = false;
  mNextSyncIndex = mSizeJobArray + 2 - 1;

  // Change from script: base this on number of CPU's not # of machines
  mHoldCrit = (mMachineListSize + 1) / 2;
  //Error messages from inside the event loop must using QApplication functionality
  PipDone();
  startTimers();
  signal(SIGINT, SIG_IGN);
#ifndef _WIN32
  signal(SIGHUP, SIG_IGN);
#endif
  return exec();
}

void Processchunks::startTimers() {
  //Make sure there isn't already a timer going
  if (mTimerId != 0) {
    killTimer(mTimerId);
    mTimerId = 0;
  }
  //The timer event function should be called immediately and then put on a timer
  QTimer::singleShot(0, this, SLOT(timerEvent()));
  if (mQueue) {
    //Must look at files instead of stdout/err.  Prevent program from being a hog.
    mTimerId = startTimer((2 + mNumCpus / 100) * 1000);
  }
  else {
    mTimerId = startTimer(1000);
  }
}

void Processchunks::timerEvent(QTimerEvent */*timerEvent*/) {
  if (mKill) {
    killSignal();
    return;
  }
  //Handle the regular timer.
  if (escapeEntered()) {
    handleInterrupt();
    return;
  }
  int i, cpuIndex;
  if (mNumDone >= mSizeJobArray) {
    cleanupAndExit();
    return;
  }
  if (readCheckFile()) {
    return;
  }
  //Count failures and assignments
  int assignTot = 0;
  int failTot = 0;
  int minFail = mDropCrit;
  int failCount = 0;
  int chunkErrTot = 0;
  int numCpus = 0;
  bool noChunks = false;
  for (i = 0; i < mMachineListSize; i++) {
    // Change from script: neither chunkErrTot nor failTot is incremented for each cpu,
    // only per machine
    failCount = mMachineList[i].getFailureCount();
    if (failCount != 0) {
      failTot++;
    }
    if (failCount < minFail) {
      minFail = failCount;
    }
    if (mMachineList[i].isChunkErred()) {
      chunkErrTot++;
    }
    numCpus = mMachineList[i].getNumCpus();
    for (cpuIndex = 0; cpuIndex < numCpus; cpuIndex++) {
      if (mMachineList[i].isJobValid(cpuIndex)) {
        assignTot++;
      }
    }
  }
  if (exitIfDropped(minFail, failTot, assignTot)) {
    return;
  }

  //Loop on machines and CPUs, if they have an assignment check if it is done
  i = -1;
  bool loopDone = false;
  while (++i < mMachineListSize && !loopDone) {
    numCpus = mMachineList[i].getNumCpus();
    for (cpuIndex = 0; cpuIndex < numCpus; cpuIndex++) {
      ProcessHandler *process = mMachineList[i].getProcessHandler(cpuIndex);
      int jobIndex = -1;
      bool dropout = false;
      if (process->isJobValid()) {
        jobIndex = process->getAssignedJobIndex();
        QString dropMess;
        QString checkPid;
        QString errorMess;
        if (process->isComProcessDone()) {
          //Handle the comscript ran and finished
          //OLD:If the log is present and the .csh is gone, it has exited
          //If the log is present and the process's finished signal has been caught
          if (process->isChunkDone()) {
            //If mSingleFile is true, set loopDone to end outer loop, and break
            //out of inner loop.
            loopDone = handleChunkDone(mMachineList[i], process, jobIndex);
            if (loopDone) {
              break;
            }
          }
          else {
            if (!mQueue && process->isPausing()) {
              // DNM: changed from return to continue.  When 30 jobs crash right away,
              // it can take a long time to get through them if you have to get through
              // successive pauses on each one
              continue;
            }
            //otherwise set flag to redo it
            dropout = true;
            if (!process->isLogFileEmpty()) {
              if (!handleLogFileError(errorMess, mMachineList[i], process)) {
                return;
              }
            }
            else if (!mQueue) {
              //OLD: If log is zero length, check for something in .pid
              //If the com script issues a PID to standard error and nothing
              //to standard out, it can't run the first real command in the
              //file.
              if (process->isPidInStderr()) {
                if (!handleError(NULL, mMachineList[i], process)) {
                  return;
                }
              }
            }
          }
        }
        else {
          handleComProcessNotDone(dropout, dropMess, mMachineList[i], process);
        }
        //if failed, remove the assignment, mark chunk as to be done,
        //skip this machine on this round
        if (dropout) {
          handleDropOut(noChunks, dropMess, mMachineList[i], process, errorMess);
        }
        //OLD:Clean up .ssh and .pid if no longer assigned
        //For queue only:  clean up .job and .qid if no longer assigned
        if (!process->isJobValid() && mQueue) {
          process->removeProcessFiles();
        }
      }
      //Drop a machine if it has failed more than given number of times
      //Institute hold on any failed machine if no chunks are done and
      //machine failure count is above criterion
      failCount = mMachineList[i].getFailureCount();
      if (failCount >= mDropCrit || mPausing || (failCount && !mAnyDone && failTot
          >= mHoldCrit)) {
        dropout = true;
      }
      /*If the current machine is unassigned and has not been dropped, find
       next com to do and run it.  Move current log out of way so non-existence
       of log can be sign of nothing having started.  Skip if no chunks are
       available*/
      if (!mMachineList[i].isDropped() && !process->isJobValid() && !dropout && !noChunks
          && mSyncing != 2) {
        jobIndex = mFirstUndoneIndex;
        bool foundChunks = false;
        int undoneIndex = -1;
        while (jobIndex < mSizeJobArray && !process->isJobValid()) {
          int runFlag;
          bool chunkOk;
          if (!checkChunk(runFlag, noChunks, undoneIndex, foundChunks, chunkOk,
              mMachineList[i], jobIndex, chunkErrTot)) {
            break;
          }
          if ((runFlag == CHUNK_SYNC || runFlag == CHUNK_NOT_DONE) && chunkOk) {
            runProcess(mMachineList[i], process, jobIndex);
          }
          jobIndex++;
        }
        //If no chunks were found in that loop set the nochunks flag
        if (!foundChunks) {
          noChunks = true;
        }
        if (undoneIndex > mFirstUndoneIndex) {
          mFirstUndoneIndex = undoneIndex;
        }
      }
    }
  }
  if (mNumDone > mLastNumDone) {
    *mOutStream << mNumDone << " OF " << mSizeJobArray << " DONE SO FAR" << endl;
  }
  mLastNumDone = mNumDone;
  //Old:  if we have finished up to the sync file, then allow the loop to run it
  if (mNumDone - 1 >= mNextSyncIndex - 1) {
    QString endComName = QString("%1-finish.com").arg(mRootName);
    if (mComFileJobs->getComFileName(mNextSyncIndex) == endComName) {
      *mOutStream << "ALL DONE - going to run " << endComName << " to reassemble" << endl;
    }
    //Set syncing flag to 1 to get it started
    mSyncing = 1;
    mFirstUndoneIndex = mNextSyncIndex;
    mNextSyncIndex = mSizeJobArray + 2 - 1;
    noChunks = false;
  }
  if (mSingleFile && mNumDone > 0) {
    cleanupAndExit();
    return;
  }
}

void Processchunks::cleanupAndExit(int exitCode) {
  int i;
  if (mTimerId != 0) {
    killTimer(mTimerId);
    mTimerId = 0;
  }
  if (exitCode == 0) {
    //Etomo is looking for "to reassemble"
    QString endComName = QString("%1.com").arg(mRootName);
    if (!mCurrentDir.exists(endComName)) {
      *mOutStream << "ALL DONE - nothing to reassemble" << endl;
    }
    //Etomo is looking for this line too
    *mOutStream << "Finished reassembling" << endl;
  }
  if (mCheckFile != NULL) {
    mCheckFile->close();
    if (exitCode == 0 && mCurrentDir.exists(mCheckFile->fileName())) {
      mCurrentDir.remove(mCheckFile->fileName());
    }
  }
  if (exitCode != 0) {
    for (i = 0; i < mMachineListSize; i++) {
      mMachineList[i].killQProcesses();
    }
  }
  *mOutStream << "exitCode:" << exitCode << endl;
  exit(exitCode);
}

int Processchunks::escapeEntered() {
#ifndef _WIN32
  static int numChar = 0;
  static int gotEsc = 0;
  fd_set readfds, writefds, exceptfds;
  struct timeval timeout;
  unsigned char charin;

  FD_ZERO(&readfds);
  FD_ZERO(&writefds);
  FD_ZERO(&exceptfds);
  FD_SET(fileno(stdin), &readfds);
  timeout.tv_sec = 0;
  timeout.tv_usec = 0;
  while (select(1, &readfds, &writefds, &exceptfds, &timeout) > 0) {
    if (!read(fileno(stdin), &charin, 1))
      return 0;
    if (charin == '\n') {
      if (numChar == 1 && gotEsc == 1) {
        numChar = 0;
        gotEsc = 0;
        return 1;
      }
      numChar = 0;
      gotEsc = 0;
    }
    else {
      numChar++;
      if (charin == 27)
        gotEsc = 1;
    }
  }
#endif
  return 0;
}

void Processchunks::handleInterrupt() {
  *mOutStream << mSizeJobArray - mNumDone << " chunks are still undone" << endl;
  QString command;
  while (mAns != 'Q' && mAns != 'C' && mAns != 'P' && !(mAns == 'D' && !mQueue)) {
    mAns = ' ';
    command.clear();
    *mOutStream
        << "Enter Q to kill all jobs and quit, P to finish running jobs then exit,"
        << endl;
    if (!mQueue) {
      *mOutStream << " D machine_list to kill jobs and drop given machines," << endl;
    }
    *mOutStream << " or C to continue waiting: " << endl;
    mOutStream->flush();
    QTextStream inStream(stdin);
    inStream >> command;
    command = command.trimmed().toUpper();
    mAns = command.at(0).toLatin1();
    if (mAns == 'D' && !mQueue) {
      inStream >> command;
      command = command.trimmed();
      if (command.isEmpty()) {
        *mOutStream << endl << "Entry error: missing machine list" << endl;
        mAns = ' ';
      }
    }
  }
  if (mAns == 'D') {
    QStringList dropList = command.split(",", QString::SkipEmptyParts);
    killProcesses(&dropList);
  }
  else {
    killProcesses();
  }
}

//Handle mAns:  killing, pausing, dropping machines, and exiting as required
void Processchunks::killProcesses(QStringList *dropList) {
  int i;
  if (dropList != NULL) {
    mDropList = *dropList;
  }
  mPausing = false;
  if (mAns == 'P') {
    mPausing = true;
  }
  if (mAns == 'C' || (mAns == 'P' && !mQueue) || (mAns == 'D' && mQueue)) {
    mDropList.clear();
    mAns = ' ';
    //Continue with timer loop
    return;
  }
  killTimer(mTimerId);
  //Slow down the timer for killing
  mKill = true;
  //killProcessOnNextMachine();
  //Run startKill on machines.  Increment mNumMachinesDropped for each matching
  //machine on the drop list.
  for (i = 0; i < mMachineListSize; i++) {
    bool activeMachine = !mMachineList[i].isDropped();
    mMachineList[i].startKill();
    if (activeMachine && mMachineList[i].isDropped()) {
      mNumMachinesDropped++;
    }
  }
  killSignal();
  mTimerId = startTimer(1000);
}

//Loops through the machine list each time the counter goes off and mKill is on.
//Sends kill signals and timeout instructions, decides when the kill is done,
//cleans up, and responds to the mAns request.
void Processchunks::killSignal() {
  int i;
  bool killDone = true;
  for (i = 0; i < mMachineListSize; i++) {
    //Send the machine a kill signal each time the timer goes off
    mMachineList[i].killSignal();
    //See if the kill is done
    if (!mMachineList[i].isKillFinished()) {
      killDone = false;
    }
  }
  //If killDone was not turned off during the loop, then the kill is done
  if (killDone) {
    //clean up kill
    //Kill the timer to clean up.  It will go back on for D and P.
    if (mTimerId != 0) {
      killTimer(mTimerId);
      mTimerId = 0;
    }
    //Reset kill variables and tell the machines to reset their kill variables.
    mDropList.clear();
    mKill = false;
    for (i = 0; i < mMachineListSize; i++) {
      mMachineList[i].resetKill();
    }
    //Handle error
    if (mAns == 'E') {
      if (!mSyncing) {
        *mOutStream << "ERROR: A CHUNK HAS FAILED " << mMaxChunkErr << " times" << endl;
      }
      else {
        *mOutStream << "ERROR: A START, FINISH, OR SYNC CHUNK HAS FAILED" << endl;
      }
      cleanupAndExit(4);
      return;
    }
    //Handle drop and pause by resuming processesing
    if ((mAns == 'D' && mMachineListSize > mNumMachinesDropped) || mAns == 'P') {
      mAns = ' ';
      *mOutStream << "Resuming processing" << endl;
      startTimers();
      return;
    }
    //If not returning to the timer loop, then exit the program.
    *mOutStream << endl
        << "When you rerun with a different set of machines, be sure to use" << endl
        << "the -r flag to retain the existing results" << endl;
    cleanupAndExit(2);
    return;
  }
}

bool Processchunks::askGo() {
  if (mJustGo) {
    return true;
  }
  *mOutStream << "Enter Y to proceed with the current set of machines: ";
  mOutStream->flush();
  char answer;
  QTextStream inStream(stdin);
  inStream >> answer;
  if (answer == 'Y' || answer == 'y') {
    return true;
  }
  return false;
}

//Change mSshOpts if version is recent enough
void Processchunks::setupSshOpts() {
  QProcess ssh(this);
  const QString command("ssh");
  const QStringList params("-V");
  ssh.start(command, params);
  if (ssh.waitForFinished()) {
    int version = extractVersion(ssh.readAllStandardError());
    if (version == -1) {
      version = extractVersion(ssh.readAllStandardOutput());
    }
    //Check if version if >= to ssh version 3.9
    if (version >= 309) {
      mSshOpts.prepend("-o ConnectTimeout=5 ");
    }
  }
}

//Setup mMachineList with the queue name or the values in mCpuList.
int *Processchunks::initMachineList(QStringList &machineNameList) {
  int i, j;
  //Not implementing $IMOD_ALL_MACHINES since no one seems to have used it.
  if (!mQueue) {
    //Setup up machine names from mCpuList
    const QStringList cpuArray = mCpuList.split(",", QString::SkipEmptyParts);
    /*The number of chunk (.com file) processes that this program can run at
     one time equals the size of the cpuArray.  The CPU limit is necessary
     because of the OS's application-level 1024 process pipe limit (this
     program uses a pipe limit of 1012 for safety).  There are 4 pipes per
     chunk process because stdout is going to a file (if it wasn't, there would
     be 6 per process). Keeping the number chunk process pipes under the pipe
     limit leaves room for running vmstocsh and killing processes.*/
#ifndef _WIN32
    const int numCpusLimit = 240;
#else
    //Handling this message: QWinEventNotifier: Cannot have more than 62 enabled at one time
    //The message appears around the time the 63rd chunk is run, so there is one
    //QWinEventNotifier per process.
    const int numCpusLimit = 56;
#endif
    int numCpus = cpuArray.size();
    if (numCpus > numCpusLimit) {
      *mOutStream << "WARNING:the number of CPUs exceeds limit (" << numCpusLimit
          << ").  CPU list will be truncated." << endl;
      numCpus = numCpusLimit;
    }
    //set max kills allowed to run at the same time, leaving room for misc processes
#ifndef _WIN32
    //1024 pipe limit
    mMaxKills = (1012 - (4 * numCpus)) / 6;
#else
    //62 whatsit limit
    mMaxKills = 60 - numCpus;
#endif
    /*Now handling mixed up names (as in bear,shrek,bear) without extra probes
     and MachineHandler instances.  Identical machine names are always
     consolidated into one MachineHandler instance and the MachineHandler
     instance order is based on where a machine name first appeared in the
     list.*/
    //Consolidate list of machines and add up CPUs.
    int *numCpusList = new int[numCpus];
    QString machineName;
    for (i = 0; i < numCpus; i++) {
      const QString cpuMachine = cpuArray.at(i);
      if (!machineNameList.contains(cpuMachine)) {
        machineNameList.append(cpuMachine);
        numCpusList[machineNameList.size() - 1] = 1;
      }
      else {
        //Increment the number of CPUs in an existing machine.
        machineName = machineNameList.last();
        if (machineName == cpuMachine) {
          numCpusList[machineNameList.size() - 1]++;
        }
        else {
          //Machine names are mixed up (as in bear,bebop,bear).  Find the
          //correct machine name.
          for (j = 0; j < machineNameList.size(); j++) {
            machineName = machineNameList[j];
            if (machineName == cpuMachine) {
              numCpusList[j]++;
              break;
            }
          }
        }
      }
    }
    if (machineNameList.size() < 1) {
      exitError("No machines specified");
    }
    //OLD:Translate a single number into a list of localhost entries
    //Set the single number as the number of CPUs in the one instance of MachineHandler.
    if (machineNameList.size() == 1) {
      bool ok;
      const long localByNum = machineNameList[0].toLong(&ok);
      if (ok) {
        if (localByNum > maxLocalByNum) {
          exitError("You cannot run more than %d chunks on localhost by "
            "entering a number", maxLocalByNum);
        }
        machineNameList[0] = "localhost";
        numCpusList[0] = localByNum;
      }
    }
    return numCpusList;
  }
  else {
    return NULL;
  }
}

//Setup mMachineList with the queue name or the values in mCpuList.
void Processchunks::setupMachineList(QStringList &machineNameList, int *numCpusList) {
  int i;
  //Not implementing $IMOD_ALL_MACHINES since no one seems to have used it.
  if (mQueue) {
    mNumCpus = mQueue;
    mQueueParamList = mCpuList.split(QRegExp("\\s+"), QString::SkipEmptyParts);
    mQueueCommand = mQueueParamList.takeAt(0);
    mMachineListSize = 1;
    //OLD: For a queue, make a CPU list that is all the same name
    //For a queue, create a single MachineHandler instance.
    mMachineList = new MachineHandler[mMachineListSize];
    mMachineList[0].setup(*this, mQueueName, mQueue);
    //Parse mCpuList into mQueueComand and mQueueParamList
    if (mCpuList.isEmpty()) {
      exitError("Queue command doesn't exist.");
    }
  }
  else {
    mMachineListSize = 0;
    for (i = 0; i < machineNameList.size(); i++) {
      //When machines where probed, the failures where removed by setting their
      //name to "".  Ignore removed machines.
      if (!machineNameList[i].isEmpty()) {
        mMachineListSize++;
      }
    }
    //Setup machine list.  Do this only once after the number of CPUs is finalized.
    if (isVerbose(mDecoratedClassName, __func__)) {
      *mOutStream << "mMachineList:" << endl;
    }
    mNumCpus = 0;
    mMachineList = new MachineHandler[mMachineListSize];
    int newIndex = 0;
    for (i = 0; i < machineNameList.size(); i++) {
      if (!machineNameList[i].isEmpty()) {
        mMachineList[newIndex].setup(*this, machineNameList[i], numCpusList[i]);
        mNumCpus += numCpusList[i];
        if (isVerbose(mDecoratedClassName, __func__)) {
          *mOutStream << newIndex << ":" << mMachineList[newIndex].getName() << endl;
        }
        newIndex++;
      }
    }
    if (isVerbose(mDecoratedClassName, __func__)) {
      *mOutStream << endl;
    }
  }
}

//Setup mHostRoot
void Processchunks::setupHostRoot() {
  QProcess hostname(this);
  const QString command("hostname");
  hostname.start(command);
  if (hostname.waitForFinished()) {
    const QString temp(hostname.readAllStandardOutput());
    const int i = temp.indexOf(".");
    if (i != -1) {
      mHostRoot = temp.mid(0, i);
    }
    else {
      mHostRoot = temp;
    }
    if (isVerbose(mDecoratedClassName, __func__)) {
      *mOutStream << "mHostRoot:" << mHostRoot << endl;
    }
  }
  else {
    exitError("Unable to run the hostname command");
  }
}

/*Sets up ComFileJobs for single file or multi-file processing.  This
 functionality should be scaleable because the number of chunks can be very
 large.  The file name limitations max out the number of chunks at around
 100,000.  Coming close to this limit is not unrealistic, especially with PEET
 processing.*/
void Processchunks::setupComFileJobs() {
  int i;
  QStringList comFileArray;
  if (isVerbose(mDecoratedClassName, __func__)) {
    *mOutStream << "current path:" << QDir::currentPath() << endl;
  }
  //OLD:Make the list for a single file
  //For a single file, one element is added to comFileArray.
  if (mSingleFile) {
    QString rootName(mRootName);
    const int extIndex = rootName.lastIndexOf(".");
    if (extIndex != -1) {
      rootName = rootName.mid(0, extIndex);
    }
    rootName.append(".com");
    if (!mCurrentDir.exists(rootName)) {
      exitError("The single command file %s does not exist", rootName.toLatin1().data());
    }
    comFileArray.append(rootName);
  }
  else {
    //Build up lists in order -nnn, -nnnn, -nnnnn*, which should work both for
    //lists that are all 5 digits or lists that are 3, 4, 5 digits
    //Put -start.com on front and -finish.com on end
    //Add start com file
    int numNumericCommandFiles = 0;
    QString startComFile(mRootName);
    startComFile.append("-start.com");
    if (mCurrentDir.exists(startComFile)) {
      comFileArray.append(startComFile);
    }
    //Add numeric com files
    mCurrentDir.setSorting(QDir::Name);
    mCurrentDir.setFilter(QDir::Files);
    //Add -nnn com files
    QStringList filters;
    //?- is a special character and gets a warning about -trigraphs
    buildFilters("-???.com", "-??\?-sync.com", filters);
    QStringList list;
    //Using QDirIterator directly is a more scaleable solution then using
    //QDir::entryList.
    QDirIterator *dit = new QDirIterator(mCurrentDir.path(), filters);
    while (dit->hasNext()) {
      dit->next();
      list << dit->fileName();
    }
    delete dit;
    list.sort();
    cleanupList("-\\D{3,3}(-sync){0,1}\\.com", list);
    numNumericCommandFiles += list.size();
    comFileArray += list;
    //add -nnnn com files
    filters.clear();
    list.clear();
    buildFilters("-????.com", "-???\?-sync.com", filters);
    dit = new QDirIterator(mCurrentDir.path(), filters);
    while (dit->hasNext()) {
      dit->next();
      list << dit->fileName();
    }
    delete dit;
    list.sort();
    cleanupList("-\\D{4,4}(-sync){0,1}\\.com", list);
    numNumericCommandFiles += list.size();
    comFileArray += list;
    //add -nnnnn com files
    filters.clear();
    list.clear();
    buildFilters("-?????.com", "-????\?-sync.com", filters);
    dit = new QDirIterator(mCurrentDir.path(), filters);
    while (dit->hasNext()) {
      dit->next();
      list << dit->fileName();
    }
    delete dit;
    list.sort();
    cleanupList("-\\D{5,5}(-sync){0,1}\\.com", list);
    numNumericCommandFiles += list.size();
    comFileArray += list;
    filters.clear();
    list.clear();
    //Add finish com file
    QString finishComFile(mRootName);
    finishComFile.append("-finish.com");
    if (mCurrentDir.exists(finishComFile)) {
      comFileArray.append(finishComFile);
    }
    if (numNumericCommandFiles == 0) {
      exitError("There are no command files matching %s-nnn.com", mRootName);
    }
  }
  if (isVerbose(mDecoratedClassName, __func__)) {
    *mOutStream << "comFileArray:" << endl;
    for (i = 0; i < comFileArray.size(); i++) {
      *mOutStream << i << ":" << comFileArray.at(i) << endl;
    }
    *mOutStream << endl;
  }
  if (comFileArray.isEmpty()) {
    exitError("There are no command files matching %s-nnn.com", mRootName);
  }
  //Build mJobArray from comFileArray.
  //set up flag list and set up which chunk to copy the log from, the first
  //non-sync if any, otherwise just the first one.
  mSizeJobArray = comFileArray.size();
  mComFileJobs = new ComFileJobs(comFileArray, mSingleFile);
  comFileArray.clear();
  for (i = 0; i < mSizeJobArray; i++) {
    if (mComFileJobs->getFlag(i) != CHUNK_SYNC && mCopyLogIndex == -1) {
      //Setting mCopyLogIndex to the first non-sync log
      mCopyLogIndex = i;
    }
  }
  if (mCopyLogIndex == -1) {
    mCopyLogIndex = 0;
  }
}

//Probe machines by running the "w" command.  Drop machines that don't respond.
void Processchunks::probeMachines(QStringList &machineNameList) {
  int i;
  //Remove the old checkfile
  if (mCheckFile != NULL) {
    if (mCheckFile->exists()) {
      mCheckFile->remove();
    }
  }
  //Windows processchunks only runs on the local machine.
  //probe machines and get all the verifications unless etomo is running it
  if (!mSkipProbe || !mJustGo) {
    *mOutStream << "Probing machine connections and loads..." << endl;
    QProcess w(this);
#ifndef _WIN32
    const QString localCommand("w");
#else
    const QString localCommand("imodwincpu");
#endif
    const QStringList localParams;
    const QString remoteCommand("ssh");
    QStringList remoteParams("-x");
    QStringList remoteWinParams("-x");
    QStringList unameParams("-x");
    for (i = 0; i < mSshOpts.size(); i++) {
      remoteParams.append(mSshOpts.at(i));
      remoteWinParams.append(mSshOpts.at(i));
      unameParams.append(mSshOpts.at(i));
    }
    remoteParams << "placeholder" << "hostname ; w";
    remoteWinParams << "placeholder" << "bash" << "--login" << "-c"
        << "\"hostname ; imodwincpu\"";
    unameParams << "placeholder" << "uname -s";

    //Probing the machines and building a new cpu array from the ones that
    //respond.
    bool status = -1;
    i = 0;
    if (isVerbose(mDecoratedClassName, __func__)) {
      *mOutStream << "machineNameList.size():" << machineNameList.size() << endl;
    }
    while (i < machineNameList.size()) {
      const QString machName = machineNameList[i];
      QByteArray output;
      if (machName == mHostRoot || machName == "localhost") {
        *mOutStream << machName << endl;
        status = runGenericProcess(output, w, localCommand, localParams, 1);
      }
      else {
        //Use uname to find out whether machName is a Windows system.  Use
        //imodwincpu instead of w for Windows systems.
        unameParams.replace(mSshOpts.size() + 1, machName);
        int unameStatus = runGenericProcess(output, w, remoteCommand, unameParams, 0);
        QStringList *params = &remoteParams;
        if (unameStatus == 0 && !output.isEmpty()) {
          QString unameOutput = output;
          if (unameOutput.contains("cygwin", Qt::CaseInsensitive)
              || unameOutput.contains("nt", Qt::CaseInsensitive)) {
            params = &remoteWinParams;
          }
        }
        params->replace(mSshOpts.size() + 1, machName);
        status = runGenericProcess(output, w, remoteCommand, *params, 2);
      }
      //status can also be set to 1 on the local machine if it times out.
      //No longer testing for 141 because no longer supporting SGI
      if (status != 0) {
        *mOutStream << "Dropping " << machName
            << " from list because it does not respond" << endl << endl;
        //Drops failed machine from the machine list
        machineNameList[i] = "";
      }
      i++;
    }
  }
}

//Look for commands in mCheckFile.  CheckFile is kept open so already processed
//commands are not read twice.
//Return true if a valid command is found in the check file
//Handle a deleted check file by closing and reopening the checkFile at intervals.
bool Processchunks::readCheckFile() {
  //Handle mCheckFile
  if (mCheckFile != NULL) {
    if (mCheckFile->exists()) {
      bool openedFile = false;
      if (!mCheckFile->isOpen()) {
        mCheckFile->open(QIODevice::ReadOnly);
        openedFile = true;
      }
      mCheckFileReconnect--;
      if (mCheckFile->isReadable()) {
        QTextStream stream(mCheckFile);
        QString comLine = stream.readLine();
        //Go past the lines in the file that have already been read.
        if (openedFile && !mSaveCheckFileLines.isEmpty()) {
          int i = 0;
          //Get the next line in the file while comLine is the same as the saved line.
          while (!comLine.isNull() && i < mSaveCheckFileLines.size() && comLine
              == mSaveCheckFileLines.at(i)) {
            comLine = stream.readLine();
            i++;
          }
          //Remove lines that are different - that's where it will start reading the new file.
          if (i == 0) {
            mSaveCheckFileLines.clear();
          }
          else {
            int j;
            for (j = i; j < mSaveCheckFileLines.size(); j++) {
              mSaveCheckFileLines.removeAt(j);
            }
          }
        }
        //Process the lines in the check file.
        while (!comLine.isNull()) {
          mSaveCheckFileLines.append(comLine);
          mAns = comLine.at(0).toUpper().toLatin1();
          if (mAns == 'D' && comLine.size() > 1) {
            //machine name(s) are required
            QStringList dropList = comLine.mid(1).trimmed().split(",",
                QString::SkipEmptyParts);
            killProcesses(&dropList);
            return true;
          }
          else if (mAns == 'P') {
            killProcesses();
            return true;
          }
          else if (mAns == 'Q') {
            killProcesses();
            return true;
          }
          else {
            *mOutStream << "BAD COMMAND IGNORED: " << comLine << endl;
          }
          comLine = stream.readLine();
        }
        if (mCheckFileReconnect <= 0) {
          mCheckFile->close();
          mCheckFileReconnect = checkFileReconnectReset;
        }
      }
    }
  }
  return false;
}

//Stop if all have now been dropped out or all have failed and none done
bool Processchunks::exitIfDropped(const int minFail, const int failTot,
    const int assignTot) {
  if (isVerbose(mDecoratedClassName, __func__, 2)) {
    *mOutStream << mDecoratedClassName << ":" << __func__ << ":minFail=" << minFail
        << ",failTot:" << failTot << ",assignTot:" << assignTot << ",mDropCrit:"
        << mDropCrit << ",mNumCpus:" << mNumCpus << ",mNumDone:" << mNumDone
        << ",mPausing:" << mPausing << ",mSyncing:" << mSyncing << ",mQueue:" << mQueue
        << ",mMachineListSize:" << mMachineListSize << endl;
  }
  if (minFail >= mDropCrit) {
    *mOutStream << "ERROR: ALL MACHINES HAVE BEEN DROPPED DUE TO FAILURES" << endl;
    if (!mQueue) {
      // DNM: this function does return, so we need to return with true if exiting
      cleanupAndExit(1);
      return true;
    }
    else {
      mAns = 'E';
      killProcesses();
    }
  }
  if (mPausing && assignTot == 0) {
    *mOutStream << "All previously running chunks are done - exiting as requested"
        << endl << "Rerun with -r to resume and retain existing results" << endl;
    cleanupAndExit(2);
    return true;
  }
  if (assignTot == 0 && mNumDone == 0) {
    // DNM: needed to compare with mMachineListSize not mNumCpus, but then the tests
    // for failure of first sync were not reached, so those tests are included in this 
    // one test, since they all took the same actions
    if (failTot == mMachineListSize && (!mSyncing || !mQueue || minFail == mQueue)) {
      *mOutStream << "ERROR: NO CHUNKS HAVE WORKED AND EVERY MACHINE HAS FAILED" << endl;
      cleanupAndExit(1);
      return true;
    }
  }
  return false;
}

//Handle chunk done: deassign, get rid of chunk errors,
//When it is the first chunk done, issue drop messages
//copy the log for the first non-sync chunk
//Return true if all chunks are done
bool Processchunks::handleChunkDone(MachineHandler &machine, ProcessHandler *process,
    const int jobIndex) {
  int i;
  //If it is DONE, then set flag to done and deassign
  //Exonerate the machine from chunk errors if this chunk
  //gave a previous chunk error
  process->setFlag(CHUNK_DONE);
  process->invalidateJob();
  machine.setFailureCount(0);
  mSyncing = 0;
  if (process->getNumChunkErr() != 0) {
    machine.setChunkErred(false);
  }
  mNumDone++;
  *mOutStream << process->getComFileName() << " finished on " << machine.getName()
      << endl;
  process->printWarnings(machine.getName());
  if (mSingleFile) {
    if (!mSkipProbe) {
      cleanupAndExit();
    }
    return true;
  }
  //If this is the first one done, issue drop messages now
  //on ones that chunk errored and exceeded failure count
  if (!mAnyDone) {
    for (i = 0; i < mMachineListSize; i++) {
      if (mMachineList[i].getFailureCount() >= mDropCrit
          && mMachineList[i].isChunkErred()) {
        *mOutStream << "Dropping " << mMachineList[i].getName() << endl;
      }
    }
  }
  mAnyDone = true;
  //copy the log for the first non-sync chunk
  if (jobIndex == mCopyLogIndex) {
    QString rootLogName = QString("%1.log").arg(mRootName);
    //Backup the root log if it exists
    imodBackupFile(rootLogName.toLatin1().data());
    QFile rootLog(rootLogName);
    if (!rootLog.open(QIODevice::WriteOnly)) {
      handleFileSystemBug();
      if (!rootLog.open(QIODevice::WriteOnly)) {
        *mOutStream << "Warning: Unable to write copied chunk log " << rootLogName
            << endl;
        return false;
      }
    }
    QTextStream writeStream(&rootLog);
    writeStream
        << "THIS FILE IS JUST THE LOG FOR ONE CHUNK AND WAS COPIED BY PROCESSCHUNKS FROM "
        << process->getLogFileName() << endl;
    const QByteArray log = process->readAllLogFile();
    if (!log.isEmpty()) {
      writeStream << log.data();
    }
    rootLog.close();
  }
  return false;
}

//Looks for and print an error message in log file.
//If the chunk has errored too many times, set mAns to E and kill jobs
//Return false the chunk has errored too many times
bool Processchunks::handleLogFileError(QString &errorMess, MachineHandler &machine,
    ProcessHandler *process) {
  process->getErrorMessageFromLog(errorMess);
  return handleError(&errorMess, machine, process);
}

//Print an error message.
//If the chunk has errored too many times, set mAns to E and kill jobs
//Return false the chunk has errored too many times
bool Processchunks::handleError(const QString *errorMess, MachineHandler &machine,
    ProcessHandler *process) {
  int numErr;
  process->incrementNumChunkErr();
  numErr = process->getNumChunkErr();
  machine.setChunkErred(true);
  //Give up if the chunk errored too many  times: and
  //for a sync chunk that is twice or once if one machine
  if (numErr >= mMaxChunkErr || (mSyncing && (mMachineListSize == 1 || numErr >= 2))) {
    process->printTooManyErrorsMessage(numErr);
    if (errorMess != NULL && !errorMess->isEmpty()) {
      *mOutStream << *errorMess << endl;
    }
    mAns = 'E';
    process->invalidateJob();
    killProcesses();
    return false;
  }
  return true;
}

//Handle timeouts and missing qid files for queues
//Handle com never started and process ended - drop
//Handle ssh error - drop
//Handle com not started yet
//Handle log doen't exist and timeout - drop
void Processchunks::handleComProcessNotDone(bool &dropout, QString &dropMess,
    MachineHandler &machine, ProcessHandler *process) {
  if (mQueue && !process->qidFileExists()) {
    //For a queue, the qid file should be there
    dropout = true;
    dropMess = "it failed to be submitted to queue";
  }
  else if (!mQueue) {
    //Either there is no log file or the .csh is still present:
    //OLD:check the ssh file and accumulate timeout
    //OLD:If the ssh file is non empty check for errors there
    //Look for cd or ssh errors in stdout and stderr.  For a queue check the
    //.job file.
    if (process->getSshError(dropMess)) {
      //A cd or ssh error is very serious - stop using this machine.
      dropout = true;
      machine.setFailureCount(mDropCrit);
    }
    if (!dropout && process->isFinishedSignalReceived()) {
      dropout = true;
    }
    else if (!dropout) {
      //OLD:if log file doesn't exist, check the pid
      //OLD:and give up after timeout
      //Check for timeout
      if (process->isStartProcessTimedOut(runProcessTimeout)) {
        dropout = true;
      }
    }
  }
}

//remove the assignment, mark chunk as to be done,
//skip this machine on this round
void Processchunks::handleDropOut(bool &noChunks, QString &dropMess,
    MachineHandler &machine, ProcessHandler *process, QString &errorMess) {
  if (!machine.isDropped()) {
    *mOutStream << process->getComFileName() << " failed on " << machine.getName()
        << " - need to restart" << endl;
    if (!errorMess.isEmpty()) {
      *mOutStream << errorMess << endl;
    }
  }
  process->setFlagNotDone(mSingleFile);
  if (mSyncing) {
    mSyncing = 1;
  }
  process->invalidateJob();
  noChunks = false;
  machine.incrementFailureCount();
  if (!machine.isDropped() && machine.getFailureCount() >= mDropCrit) {
    if (errorMess.isEmpty()) {
      process->getErrorMessageFromOutput(errorMess);
      if (!errorMess.isEmpty()) {
        *mOutStream << errorMess << endl;
      }
    }
    if (!machine.isDropped()) {
      if (dropMess.isEmpty()) {
        dropMess = "it failed (with ";
        if (!machine.isChunkErred()) {
          dropMess.append("time out");
        }
        else {
          dropMess.append("chunk error");
        }
        dropMess.append(") %1 times in a row");
        dropMess = dropMess.arg(machine.getFailureCount());
      }
      if (!mAnyDone && machine.isChunkErred()) {
        *mOutStream << "Holding off on using ";
      }
      else {
        *mOutStream << "Dropping ";
      }
      *mOutStream << machine.getName() << " - " << dropMess << endl;
    }
  }
}

//See if a process can be run by the current machine
//Return false when need to break out of the loop
bool Processchunks::checkChunk(int &runFlag, bool &noChunks, int &undoneIndex,
    bool &foundChunks, bool &chunkOk, MachineHandler &machine, const int jobIndex,
    const int chunkErrTot) {
  runFlag = mComFileJobs->getFlag(jobIndex);
  if (isVerbose(mDecoratedClassName, __func__, 2)) {
    *mOutStream << mDecoratedClassName << ":" << __func__ << ":jobIndex:" << jobIndex
        << ",runFlag:" << runFlag << endl;
  }
  //But if the next com is a sync, record number and break loop
  if (runFlag == CHUNK_SYNC && !mSyncing) {
    mNextSyncIndex = jobIndex;
    if (!foundChunks) {
      noChunks = true;
    }
    return false;
  }
  if (undoneIndex == -1 && runFlag != CHUNK_DONE) {
    undoneIndex = jobIndex;
  }
  //If any chunks found set that flag
  if (runFlag == CHUNK_SYNC || runFlag == CHUNK_NOT_DONE) {
    foundChunks = true;
    //Skip a chunk if it has errored, if this machine has given chunk
    //error, and not all machines have done so
    // Change from script: chunkErrTot is based on number of machines not # of cpus
    chunkOk = true;
    if (mComFileJobs->getNumChunkErr(jobIndex) > 0 && machine.isChunkErred()
        && chunkErrTot < mMachineListSize) {
      chunkOk = false;
      if (mSyncing) {
        return false;
      }
    }
  }
  return true;
}

//Build the .csh file and run the process
void Processchunks::runProcess(MachineHandler &machine, ProcessHandler *process,
    const int jobIndex) {
  //Lock the pipe counter until the process either starts or fails to start.
  //Since there is a delay between
  process->setJob(jobIndex);
  process->resetPausing();
  process->setFlag(CHUNK_ASSIGNED);
  process->backupLog();
  process->removeProcessFiles();
  *mOutStream << "Running " << process->getComFileName() << " on " << machine.getName()
      << " ..." << endl;
  makeCshFile(process);
  //If running a sync, set the syncing flag to 2
  if (mSyncing) {
    mSyncing = 2;
  }
  process->runProcess(machine);
}

//Extracts the first two numbers of a numeric version.  Multiples the first
//number by 100 and adds it to the second number.  Places the result in
//mVersion.
int Processchunks::extractVersion(const QString &versionString) {
  int iversion = -1;
  if (isVerbose(mDecoratedClassName, __func__)) {
    *mOutStream << "ssh sshOutput:" << versionString << endl;
  }
  const QRegExp regExp("[0-9]+\\.[0-9]+");
  const int i = regExp.indexIn(versionString, 0);
  const int len = regExp.matchedLength();
  if (i != -1 && len != -1) {
    QString version = versionString.mid(i, len);
    if (version != NULL) {
      const QStringList array = version.split(".", QString::SkipEmptyParts);
      if (!array.isEmpty()) {
        bool ok;
        iversion = array.at(0).toLong(&ok) * 100;
        if (!ok) {
          iversion = -1;
          return iversion;
        }
        if (array.size() > 1) {
          version += array.at(1).toLong(&ok);
          if (!ok) {
            version = -1;
          }
        }
      }
    }
  }
  if (isVerbose(mDecoratedClassName, __func__)) {
    *mOutStream << "ssh version:" << iversion << endl;
  }
  return iversion;
}

void Processchunks::buildFilters(const char *reg, const char *sync, QStringList &filters) {
  int i;
  QString filter1(mRootName);
  filter1.append(reg);
  filters.append(filter1);
  QString filter2(mRootName);
  filter2.append(sync);
  filters.append(filter2);
}

void Processchunks::cleanupList(const char *remove, QStringList &list) {
  int i;
  //Remove files that don't have digits after rootname-
  QString regExp(mRootName);
  regExp.append(remove);
  while ((i = list.indexOf(QRegExp(regExp))) != -1) {
    list.removeAt(i);
  }
}

//Runs process, outputs first numLinesToPrint lines, and returns the exit code
//If numLinesToPrint to is zero, no lines with be printed.
//Places stdout into the output parameter.
int Processchunks::runGenericProcess(QByteArray &output, QProcess &process,
    const QString &command, const QStringList &params, const int numLinesToPrint) {
  int i;
  process.start(command, params);
  if (process.waitForFinished()) {
    output = process.readAllStandardOutput();
    //Output first lines up to numLinesToPrint
    int startIndex = 0;
    int endIndex = -1;
    int temp;
    for (i = 0; i < numLinesToPrint; i++) {
      temp = endIndex;
      endIndex = output.indexOf('\n', endIndex + 1);
      startIndex = temp + 1;
      if (endIndex == -1) {
        //No more lines
        *mOutStream << output;
        break;
      }
      else {
        *mOutStream << output.mid(startIndex, endIndex - startIndex + 1);
      }
    }
    return process.exitCode();
  }
  return 1;
}

void Processchunks::handleFileSystemBug() {
  *mOutStream << "running ls" << endl;
  mLsProcess->start("ls", mLsParamList);
  mLsProcess->waitForFinished(10000);
}

//Return if csh file is made
void Processchunks::makeCshFile(ProcessHandler *process) {
  QString cshFileName = process->getCshFile();
  if (cshFileName.isEmpty()) {
    *mOutStream << "Warning: no .csh file name available " << endl;
    return;
  }
  QFile cshFile(cshFileName);
  mCurrentDir.remove(cshFile.fileName());
  QTextStream writeStream(&cshFile);
  if (!cshFile.open(QIODevice::WriteOnly)) {
    *mOutStream << "Warning: unable to open and create " << cshFileName << endl;
    return;
  }
  if (!mQueue) {
    writeStream << "nice +" << mNice << endl;
  }
  //convert and add CHUNK DONE to all files
  QString comFileName = process->getComFileName();
  mVmstocsh->setStandardInputFile(comFileName);
  //This does not work:
  //mVmstocsh->setStandardOutputFile(mCshFile->fileName(), QIODevice::Append);
  QString command("vmstocsh");
  QStringList paramList;
  paramList.append(process->getLogFileName());
  mVmstocsh->start(command, paramList);
  if (!mVmstocsh->waitForFinished()) {
    if (mVmstocsh->exitStatus() == QProcess::CrashExit) {
      *mOutStream << "Warning: vmstocsh conversion of " << comFileName
          << " failed with exit code " << mVmstocsh->exitCode() << " "
          << mVmstocsh->readAllStandardError().data() << endl;
    }
    else {
      *mOutStream << "Warning: vmstocsh conversion of " << comFileName
          << " timed out after 30 seconds: " << mVmstocsh->readAllStandardError().data()
          << endl;
    }
  }
  writeStream << mVmstocsh->readAllStandardOutput().data() << "echo CHUNK DONE >> "
      << process->getLogFileName() << endl;

  cshFile.close();
}

//Returns true if its parameters match the verbose member variables.  If print
//is true, will print this function's verbose message only if class and
//function match (uses the verbosity level from the calling function).
bool Processchunks::isVerbose(const QString &verboseClass, const QString verboseFunction,
    const int verbosity, const bool print) {
  int i;
  if (!mVerbose) {
    return false;
  }
  if (verbosity > mVerbose) {
    return false;
  }
  if (mVerboseClass.isEmpty()) {
    return true;
  }
  if (!mVerboseFunctionList.isEmpty() && print) {
    if (isVerbose(mDecoratedClassName, __func__, 1, false)) {
      *mOutStream << verboseClass << "," << verboseFunction << "," << verbosity << endl;
    }
  }
  if (!verboseClass.endsWith(mVerboseClass, Qt::CaseInsensitive)) {
    return false;
  }
  if (mVerboseFunctionList.isEmpty()) {
    return true;
  }
  for (i = 0; i < mVerboseFunctionList.size(); i++) {
    if (verboseFunction.endsWith(mVerboseFunctionList.at(i), Qt::CaseInsensitive)) {
      return true;
    }
  }
  return false;
}
