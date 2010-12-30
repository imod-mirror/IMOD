#ifndef PROCESSCHUNKS_H
#define PROCESSCHUNKS_H

#include <QApplication>
#include <machinehandler.h>
#include <QList>
#include <QStringList>
#include <QDir>
#include <processhandler.h>
#include <comfilejob.h>

#ifndef __func__
#define __func__ __FUNCTION__
#endif

class ProcessHandler;
class ComFileJob;
class QTextStream;
class QProcess;
class QFile;
class MachineHandler;

class Processchunks: public QApplication {
Q_OBJECT

public:
  Processchunks(int &argc, char **argv);
  ~Processchunks();

  void printOsInformation();
  void loadParams(int &argc, char **argv);
  void setup();
  const bool askGo();
  void startLoop();
  void killProcessOnNextMachine();
  void msgKillProcessStarted(ProcessHandler *processHandler);
  void msgKillProcessDone(ProcessHandler *processHandler);
  void handleFileSystemBug();

  const bool isQueue();
  const QString &getQueueCommand();
  QDir &getCurrentDir();
  const QStringList &getQueueParamList();
  const bool isVerbose(const QString &verboseClass,
      const QString verboseFunction, const int verbosity = 1);
  QTextStream &getOutStream();
  const bool isSingleFile();
  const QString &getHostRoot();
  const QStringList &getSshOpts();
  const int getNice();
  const int getMillisecSleep();
  const char getAns();
  QStringList &getDropList();
  const int getDropCrit();
  const QString &getRemoteDir();
  void makeCshFile(ProcessHandler *process);

public slots:
  void timerEvent();

protected:
  void timerEvent(QTimerEvent *e);

private:
  const int extractVersion(const QString &versionString);
  void buildFilters(const char *reg, const char *sync, QStringList &filters);
  void cleanupList(const char *remove, QStringList &list);
  const int runGenericProcess(QByteArray &output, QProcess &process,
      const QString &command, const QStringList &params,
      const int numLinesToPrint);
  void setupSshOpts();
  void setupMachineList();
  void setupHostRoot();
  void setupProcessArray();
  void probeMachines();
  const bool readCheckFile();
  void exitIfDropped(const int minFail, const int failTot, const int assignTot);
  const bool handleChunkDone(MachineHandler *machine, ProcessHandler *process,
      const int jobIndex);
  const bool
  handleLogFileError(QString &errorMess, MachineHandler *machine,
      ProcessHandler *process);
  void handleComProcessNotDone(bool &dropout, QString &dropMess,
      MachineHandler *machine, ProcessHandler *process);
  void handleDropOut(bool &noChunks, QString &dropMess,
      MachineHandler *machine, ProcessHandler *process, QString &errorMess);
  const bool checkChunk(int &runFlag, bool &noChunks, int &undone,
      bool &foundChunks, bool &chunkOk, MachineHandler *machine,
      const int jobIndex, const int chunkErrTot);
  void runProcess(MachineHandler *machine, ProcessHandler *process,
      const int jobIndex);
  int escapeEntered();
  void handleInterrupt();
  void cleanupAndExit(int exitCode = 0);
  void killProcessTimeout();
  void killProcesses(QStringList *dropList = NULL);
  void startTimers();
  void cleanupKillProcesses(const bool timeout);
  const bool handleError(const QString *errorMess, MachineHandler *machine,
      ProcessHandler *process);
  const bool isVerbose(const QString &verboseClass,
      const QString verboseFunction, const int verbosity, const bool print);

  int mSizeJobArray;
  ComFileJob *mJobArray;
  QList<MachineHandler> mMachineList;
  QTextStream *mOutStream;

  //params
  int mRetain, mJustGo, mNice,mMillisecSleep, mDropCrit, mQueue, mSingleFile, mMaxChunkErr,
      mVerbose;
  bool mSkipProbe;
  char *mQueueName, *mRootName;
  QFile *mCheckFile;
  QString mCpuList, mVerboseClass, *mRemoteDir;//was curdir;
  QStringList mVerboseFunctionList;

  //setup
  int mCopyLogIndex, mNumCpus;
  QString mHostRoot, mQueueCommand, mDecoratedClassName;
  QStringList mSshOpts, mQueueParamList;
  QDir mCurrentDir;

  //loop
  int mNumDone, mLastNumDone, mHoldCrit, mTimerId, mFirstUndoneIndex,
      mNextSyncIndex, mSyncing;
  bool mPausing, mAnyDone;
  char mAns;

  //killing processes
  bool mKill, mAllKillProcessesHaveStarted;
  int mKillProcessMachineIndex, mKillCounter;
  QList<ProcessHandler*> mProcessesWithUnfinishedKillRequest;
  QStringList mDropList;

  //handling file system bug
  QProcess *mLsProcess, *mVmstocsh;
  QStringList mLsParamList;
};

#endif
