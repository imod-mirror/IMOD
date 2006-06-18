/*
 *  imodqtassist.cpp - Runs Qt assistant and displays pages entered through
 *                     standard input
 *
 *  Author: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2004 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 */

/*  $Author$

    $Date$

    $Revision$

    Log at end of file
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef _WIN32
#include <sys/select.h>
#include <sys/time.h>
#endif
#include <qstring.h>
#include <qapplication.h>
#include "imod_assistant.h"
#include "imodqtassist.h"

static ImodAssistant *imodHelp;

#define TIMER_INTERVAL  50
#define MAX_LINE 1024
static char threadLine[MAX_LINE];
static bool gotLine = false;
static int lineLen;

#ifdef QT_THREAD_SUPPORT
static QMutex mutex;
static AssistantThread * assThread;
#endif

static int readLine(char *line);

// MAIN
int main(int argc, char *argv[])
{
  int retval, ind = 1;
  char *adp = NULL;
  bool absPath = false;
  bool keepBar = false;

  // Start the application 
  QApplication qapp(argc, argv);

  if (argc < 2) {
    fprintf(stderr, "Usage: imodqtassist [options] path_to_documents\n");
    fprintf(stderr, "   Options:\n");
    fprintf(stderr, "\t-a\tpath_to_documents is absolute, not relative to"
            " $IMOD_DIR\n");
    fprintf(stderr, "\t-p file\tName of adp (document profile) file\n");
    fprintf(stderr, "\t-k\tKeep the sidebar (do not hide it)\n");
    exit(1);
  }

  // Parse arguments
  while (argc - ind > 1) {
    if (argv[ind][0] == '-') {
      switch (argv[ind][1]) {

      case 'a':
        absPath = true;
        break;

      case 'k':
        keepBar = true;
        break;

      case 'p':
        adp = argv[++ind];
        break;

      default:
        fprintf(stderr, "ERROR: unknown argument %s\n", argv[ind]);
        exit(1);
      }
    } else {
      fprintf(stderr, "ERROR: too many arguments\n");
      exit(1);
    }
    ind++;
  }

  // start the help object
  imodHelp = new ImodAssistant(argv[ind], adp, NULL, absPath, keepBar);

  // Get a listener for the errors and timer
  AssistantListener *listener = new AssistantListener();
  QObject::connect(imodHelp, SIGNAL(error(const QString&)), listener,
                   SLOT(assistantError(const QString&)));

#ifdef QT_THREAD_SUPPORT

  // If using threads, start a thread to read stdin
  assThread = new AssistantThread();
  assThread->start();
#endif

  // Start timer to watch for input
  listener->startTimer(TIMER_INTERVAL);
  retval = qapp.exec();

  // Upon exit, delete the help object to close help, and kill thread if
  // needed
  delete imodHelp;

#ifdef QT_THREAD_SUPPORT
  if (assThread->running())
    assThread->terminate();
#endif
  return retval;
}

void AssistantListener::assistantError(const QString &msg)
{
  fprintf(stderr, "Error starting Qt Assistant: %s\n", msg.latin1());
  fflush(stderr);
  QApplication::exit(1);
}


void AssistantListener::timerEvent(QTimerEvent *e)
{
  char line[MAX_LINE];
  int err;

#ifdef QT_THREAD_SUPPORT

  // If there is thread support, see if the thread got a line
  // Copy the line while we have the mutex
  bool gotOne;
  mutex.lock();
  gotOne = gotLine;
  gotLine = false;
  if (gotOne)
    strcpy(line, threadLine);
  mutex.unlock();
  if (!gotOne)
    return;
#else
#ifdef _WIN32

  // If there is no thread support, Windows fallback is to process events
  // before and after.  This didn't work on second display and shouldn't
  // happen anyway
  qApp->processEvents();
#else

  // Non-windows fallback from threads uses the select operation on stdin
  bool firstTime = true;
  static fd_set readfds, writefds, exceptfds;
  struct timeval timeout;

  if (firstTime) {
    FD_ZERO(&readfds);
    FD_ZERO(&writefds);
    FD_ZERO(&exceptfds);
    FD_SET(fileno(stdin), &readfds);
    firstTime = false;
  }
  timeout.tv_sec = 0;
  timeout.tv_usec = 0;
  if (select(1, &readfds, &writefds, &exceptfds, &timeout) <= 0)
    return;
#endif

  // read line here in non-thread case
  lineLen = readLine(line);
#endif

  // Exit if blank line
  if (!lineLen) {
    QApplication::exit(0);
    return;
  }

  // Otherwise show page and report results
  err = imodHelp->showPage(line);
  if (err > 0)
    fprintf(stderr, "Page %s not found\n", line);
  else
    fprintf(stderr, "Page %s displayed\n", line);
  if (err < 0 && !mWarned) {
    fprintf(stderr, "WARNING: adp file not found\n");
    mWarned = true;
  }
    
  fflush(stderr);

#ifndef QT_THREAD_SUPPORT
#ifdef _WIN32
  qApp->processEvents();
#endif
#endif
}

#ifdef QT_THREAD_SUPPORT

// The thread loops on reading a line and sets the flag when it gets one
void AssistantThread::run()
{
  bool gotOne;
  while (1) {
    mutex.lock();
    gotOne = gotLine;
    mutex.unlock();

    // Do not go for another line until the main thread has cleared the flag
    if (gotOne)
      continue;
    
    lineLen = readLine(threadLine);
    mutex.lock();
    gotLine = true;
    mutex.unlock();
    if (!lineLen)
      return;
  }
}    
#endif

// Common routine to read a line, strip endings, and return length
static int readLine(char *line)
{
  fgets(line, MAX_LINE, stdin);
  if (line[MAX_LINE - 1])
    line[MAX_LINE - 1] = 0x00;
  while (line[strlen(line) - 1] == '\n' || line[strlen(line) - 1] == '\r')
    line[strlen(line) - 1] = 0x00;
  return strlen(line);
}

/*
    $Log$
    Revision 1.6  2005/11/19 16:58:30  mast
    Corrected print statement

    Revision 1.5  2004/12/24 02:20:46  mast
    Added usage statement, made warning come out only once


    Revision 1.4  2004/12/22 23:14:21  mast
    Handle adp not found error message

    Revision 1.3  2004/12/22 22:53:39  mast
    Needed to advance index in argument loop

    Revision 1.2  2004/12/22 05:58:49  mast
    Fixed bugs that showed up on SGI

    Revision 1.1  2004/12/22 05:49:02  mast
    Addition to package

*/
