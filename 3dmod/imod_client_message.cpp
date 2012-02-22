/*  
 *  imod_client_message.c   Used to handle X client messages - 
 *                             now QClipboard changes or stdin messages
 *
 *  Original author: David Mastronarde   email: mast@colorado.edu
 *  $Id$
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#ifndef _WIN32
#include <sys/select.h>
#include <sys/time.h>
#endif
#include <qdir.h>
#include <qregexp.h>
#include <qtimer.h>
#include <qapplication.h>
#include <qclipboard.h>
#include "imod.h"
#include "imod_display.h"
#include "dia_qtutils.h"
#include "imodv.h"
#include "imodv_objed.h"
#include "imodv_input.h"
#include "imodv_window.h"
#include "imod_io.h"
#include "xcramp.h"
#include "imod_info_cb.h"
#include "imod_info.h"
#include "imod_input.h"
#include "imod_client_message.h"
#include "imodplug.h"
#include "imod_workprocs.h"
#include "control.h"
#include "xzap.h"
#include "zap_classes.h"
#include "sslice.h"

//  Module variables
static int message_action = MESSAGE_NO_ACTION;
static QStringList messageStrings;
static int message_stamp = -1;

#define STDIN_INTERVAL  50
#define MAX_LINE 256
static char threadLine[MAX_LINE];
static bool gotLine = false;
static int lineLen;

#if defined(_WIN32) && defined(QT_THREAD_SUPPORT)
static QMutex mutex;
static StdinThread *stdThread = NULL;
#endif

static int readLine(char *line);

ImodClipboard::ImodClipboard(bool useStdin)
  : QObject()
{
  QClipboard *cb = QApplication::clipboard();
  //cb->setSelectionMode(false);
  mHandling = false;
  mExiting = false;
  mDisconnected = false;
  mClipHackTimer = NULL;
  mStdinTimer = NULL;
  mUseStdin = useStdin;

  if (useStdin) {
#if defined(_WIN32) && defined(QT_THREAD_SUPPORT)
    stdThread = new StdinThread();
    stdThread->start();
#endif
    mStdinTimer = new QTimer(this);
    connect(mStdinTimer, SIGNAL(timeout()), this, SLOT(stdinTimeout()));
    mStdinTimer->start(STDIN_INTERVAL);
  } else {

    // If the hack is needed, start a timer to check the clipboard
#ifdef CLIPBOARD_TIMER_HACK
    mClipHackTimer = new QTimer(this);
    connect(mClipHackTimer, SIGNAL(timeout()), this, SLOT(clipHackTimeout()));
    mSavedClipboard = cb->text();
    mClipHackTimer->start(CLIPBOARD_TIMER_HACK);
#else
    
    // Otherwise just connect to signal for changes
    connect(cb, SIGNAL(dataChanged()), this, SLOT(clipboardChanged()));
#endif

  }
}

/*
 * Functions to disconnect on Windows using stadard input
 * First request the disconnect, which should send a blank line
 */
void ImodClipboard::startDisconnect()
{
#if defined(_WIN32) && defined(QT_THREAD_SUPPORT)
  if (mUseStdin && stdThread->isRunning())
    imodPrintStderr("REQUEST STOP LISTENING\n");
#endif
}

// Then wait until the thread exits or give error
int ImodClipboard::waitForDisconnect()
{
#if defined(_WIN32) && defined(QT_THREAD_SUPPORT)
  int timeout = 5000;
  if (mUseStdin && stdThread->isRunning()) {
    if (stdThread->wait(timeout))
      return 0;
    dia_err("3dmod is stuck listening for input and is unable to terminate"
            " cleanly\nYou need to kill it in Task Manager or with Ctrl C");
    stdThread->terminate();
  }
  return 1;
#else
  return 0;
#endif
}


/*
 * Clipboard change event slot and timeout for performing actions
 */
void ImodClipboard::clipboardChanged()
{
  if (Imod_debug) {
    QClipboard *cb = QApplication::clipboard();
    //cb->setSelectionMode(false);
    QString text = cb->text();
    imodPrintStderr("imodHCM in clipboardChanged - clipboard = %s\n", 
                    LATIN1(text));
    if (ImodInfoWin)
      wprint("clipboardChanged = %s\n", LATIN1(text));
  }
  // If already handling a change or the message is not for us, return
  if (mHandling || !handleMessage())
    return;

  // Otherwise create and start a timer to execute the action
  // Set flag because event comes in twice in Windows
  mHandling = true;
  QTimer::singleShot(10, this, SLOT(clipTimeout()));
}

void ImodClipboard::clipTimeout()
{
  // If exiting flag is set, then finally quit
  if (mExiting) {
    if (ImodvClosed || !Imodv->standalone)
      imod_quit();
    else
      imodvQuit();

  } else {
    
    // Otherwise, execute the message
    if (executeMessage()) {

      // If it returns true, set the exiting flag and start another timer
      // 100 ms is too short on SGI
      mExiting = true;
      QTimer::singleShot(200, this, SLOT(clipTimeout()));
    } 
    mHandling = false;
  }
}

// The hack for Windows: periodically check for clipboard changes
void ImodClipboard::clipHackTimeout()
{
  QClipboard *cb = QApplication::clipboard();
  //cb->setSelectionMode(false);
  if (cb->text() == mSavedClipboard)
    return;
  mSavedClipboard = cb->text();
  clipboardChanged();
}

// Timer to check for stdin messages
void ImodClipboard::stdinTimeout()
{
  QString text;
  if (mHandling)
    return;

#ifdef _WIN32
  // See if the thread got a line
  // Copy the line while we have the mutex
  bool gotOne;
  mutex.lock();
  gotOne = gotLine;
  gotLine = false;
  if (gotOne)
     text = threadLine;
  mutex.unlock();
  if (!gotOne)
    return;
#else
  fd_set readfds, writefds, exceptfds;
  struct timeval timeout;

  FD_ZERO(&readfds);
  FD_ZERO(&writefds);
  FD_ZERO(&exceptfds);
  FD_SET(fileno(stdin), &readfds);
  timeout.tv_sec = 0;
  timeout.tv_usec = 0;
  if (select(1, &readfds, &writefds, &exceptfds, &timeout) <= 0)
    return;

  lineLen = readLine(threadLine);
  text = threadLine;
#endif

  if (!lineLen) {
    mStdinTimer->stop();
    disconnect(mStdinTimer);
    //delete mStdinTimer;
    //mStdinTimer = NULL;
    sendResponse(1);
    mDisconnected = true;
    return;
  }

  messageStrings = text.split(" ", QString::SkipEmptyParts);

  // Start timer to execute message just as for clipboard
  mHandling = true;
  QTimer::singleShot(10, this, SLOT(clipTimeout()));
}

// Parse the message, see if it is for us, and save in local variables
bool ImodClipboard::handleMessage()
{
  int newStamp;
  if (!ImodInfoWin && ImodvClosed)
    return false;

  // get the text from the clipboard
  QClipboard *cb = QApplication::clipboard();
  //cb->setSelectionMode(false);
  QString text = cb->text();
  if (Imod_debug) {
    imodPrintStderr("imodHCM in handleMessage = %s\n", LATIN1(text));
    wprint("handleMessage = %s\n", LATIN1(text));
  }                   

  // Return if text is empty
  if (text.isEmpty())
    return false;

  // Split the string, ignoring multiple spaces, and return false if fewer
  // than 3 elements
  messageStrings = text.split(" ", QString::SkipEmptyParts);
  if (messageStrings.count() < 3)
    return false;

  // Return false if this is not our info window ID
  if (messageStrings[0].toInt() != ourWindowID())
    return false;

  // If we see the same message again, send the last response again
  newStamp = messageStrings[1].toInt();
  if (newStamp == message_stamp) {
    sendResponse(-1);
    return false;
  }
  message_stamp = newStamp;
  return true;
}

// This function performs the action after the delay is up.  It returns
// true if the program is still to quit
bool ImodClipboard::executeMessage()
{
  int returnValue, arg;
  int succeeded = 1;
  static int checkSum = 0;
  int newCheck;
  QString convName;
  QDir *curdir;
  ZapFuncs *zap;
  int movieVal, xaxis, yaxis, zaxis, taxis;
  int objNum, type, symbol, symSize, ptSize, mode, mask, interval;
  bool props1;
  Imod *imod;
  Iobj *obj;
  int symTable[] = 
    { IOBJ_SYM_NONE, IOBJ_SYM_CIRCLE, IOBJ_SYM_SQUARE, IOBJ_SYM_TRIANGLE };

  // Number of arguments required - for backward compatibility, going to
  // model mode does not require one but should have one
  int requiredArgs[] = {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 5, 5, 0, 2, 4, 4, 3};
  int numArgs = messageStrings.count();

  // Loop on the actions in the list; set arg to numArgs to break loop
  for (arg = mUseStdin ? 0 : 2; arg < numArgs; arg++) {
    imod = App->cvi->imod;

    // Get the action, then check that there are enough values for it
    message_action = messageStrings[arg].toInt();
    if (message_action < sizeof(requiredArgs) / sizeof(int) &&
        arg + requiredArgs[message_action] >= numArgs) {
        imodPrintStderr("imodExecuteMessage: not enough values sent"
                " with action command %d\n", message_action);
        succeeded = 0;
        break;
    }

    if (Imod_debug) {
      wprint("Executing message action %d\n", message_action);
      imodPrintStderr("imodHCM in executeMessage: executing message action "
                      "%d\n", message_action);
      newCheck = imodChecksum(App->cvi->imod);
      wprint("Checksum before = %d\n", newCheck);
      if (checkSum && newCheck != checkSum)
        wprint("\aIT CHANGED SINCE LAST TIME\n");
      checkSum = newCheck;
    }

    if (ImodvClosed || !Imodv->standalone) {
      switch (message_action) {
        
      case MESSAGE_OPEN_MODEL:
      case MESSAGE_OPEN_KEEP_BW:
        curdir = new QDir();
        convName = curdir->cleanPath(messageStrings[++arg]);
        delete curdir;
    
        // Since this could open a dialog with an indefinite delay, just send
        // the OK signal now
        succeeded = -1;
        sendResponse(1);
        inputRaiseWindows();

        // DNM 6/3/04: switch to keeping BW values in the first place
        returnValue = openModel(LATIN1(convName), 
                                message_action == MESSAGE_OPEN_KEEP_BW, false);
        if(returnValue == IMOD_IO_SUCCESS) {
          wprint("%s loaded.\n", 
                 LATIN1(QDir::convertSeparators(QString(Imod_filename))));

        }
        else if(returnValue == IMOD_IO_SAVE_ERROR) {
          wprint("Error Saving Model. New model not loaded.\n");
          arg = numArgs;
        }
        else if(returnValue == IMOD_IO_SAVE_CANCEL) {
          wprint("Operation cancelled. New model not loaded.\n");
          arg = numArgs;
        }

        // The model does not exist yet.  Try creating a new model.
        else if(returnValue == IMOD_IO_DOES_NOT_EXIST) {
          returnValue = createNewModel(LATIN1(convName));
          if(returnValue == IMOD_IO_SUCCESS) {
        
            wprint("New model %s created.\n", 
                   LATIN1(QDir::convertSeparators(QString(Imod_filename))));
          }
          else {
            wprint("Could not create a new model %s.\n", 
                   LATIN1(messageStrings[arg]));
            arg = numArgs;
          }
        }
        else if(returnValue == IMOD_IO_NO_ACCESS_ERROR) {
          wprint("Error opening model. Check file permissions\n.");
          arg = numArgs;
        }
        else {
          wprint("Unknown return code, new model not loaded!!\n");
          arg = numArgs;
        }
        break;

      case MESSAGE_SAVE_MODEL:
        succeeded = -1;
        sendResponse(1);
        imod->blacklevel = App->cvi->black;
        imod->whitelevel = App->cvi->white;
        returnValue = SaveModel(imod);
        break;

      case MESSAGE_VIEW_MODEL:
        imod_autosave(imod);
        inputRaiseWindows();
        imodv_open();
        break;

      case MESSAGE_QUIT:
        arg = numArgs;
        break;

      case MESSAGE_RAISE_WINDOWS:
        inputRaiseWindows();
        break;

      case MESSAGE_MODEL_MODE:
        movieVal = 1;
        if (arg < numArgs - 1)
        movieVal = messageStrings[++arg].toInt();
        if (movieVal > 0)
          imod_set_mmode(IMOD_MMODEL);
        else {
          imod_set_mmode(IMOD_MMOVIE);
          if (movieVal < 0) {
            xaxis = (-movieVal) & 1 ? 1 : 0;
            yaxis = (-movieVal) & 2 ? 1 : 0;
            zaxis = (-movieVal) & 4 ? 1 : 0;
            taxis = (-movieVal) & 8 ? 1 : 0;
            imodMovieXYZT(App->cvi, xaxis, yaxis, zaxis, taxis);
          }
        }
        break;

      case MESSAGE_OPEN_BEADFIXER:
        imodPlugOpenByName("Bead Fixer");
        break;

      case MESSAGE_ONE_ZAP_OPEN:
        inputRaiseWindows();
        if (!imodDialogManager.windowCount(ZAP_WINDOW_TYPE) &&
            imodLoopStarted())
          imod_zap_open(App->cvi, 0);
        break;

      case MESSAGE_RUBBERBAND:
        zapReportRubberband();
        break;

      case MESSAGE_SLICER_ANGLES:
        slicerReportAngles();
        break;

      case MESSAGE_OBJ_PROPERTIES:
      case MESSAGE_NEWOBJ_PROPERTIES:
      case MESSAGE_OBJ_PROPS_2:
      case MESSAGE_NEWOBJ_PROPS_2:
        props1 = message_action == MESSAGE_OBJ_PROPERTIES ||
          message_action == MESSAGE_NEWOBJ_PROPERTIES;
        objNum = messageStrings[++arg].toInt();
        type = messageStrings[++arg].toInt();
        symbol = messageStrings[++arg].toInt();
        symSize = messageStrings[++arg].toInt();
        if (props1)
          ptSize = messageStrings[++arg].toInt();

        // Object is numbered from 1, so decrement and test for substituting
        // current object
        if (--objNum < 0)
          objNum = imod->cindex.object;
        if (objNum < 0 || objNum >= imod->objsize) {
          imodPrintStderr("imodExecuteMessage: illegal object # sent with "
                          "object property command\n");
          succeeded = 0;
          arg = numArgs;
          break;
        }
        obj = &imod->obj[objNum];

        // If object has contours, skip for NEWOBJ message
        if (obj->contsize && (message_action == MESSAGE_NEWOBJ_PROPERTIES ||
                              message_action == MESSAGE_NEWOBJ_PROPS_2))
          break;

        if (props1) {

          // Process the changes if not -1: object type
          if (type >= 0 && type < 3) {
            switch (type) {
            case 0:
              obj->flags &= ~(IMOD_OBJFLAG_OPEN | IMOD_OBJFLAG_SCAT);
              break;
            case 1:
              obj->flags |= IMOD_OBJFLAG_OPEN;
              obj->flags &= ~IMOD_OBJFLAG_SCAT;
              break;
            case 2:
              obj->flags |= IMOD_OBJFLAG_SCAT | IMOD_OBJFLAG_OPEN;
              break;
            }
          }
        
          // Symbol type and filled
          if (symbol >= 0) {
            if ((symbol & 7) < (sizeof(symTable) / sizeof(int)))
              obj->symbol = symTable[symbol & 7];
            utilSetObjFlag(obj, 0, (symbol & 8) != 0, IOBJ_SYMF_FILL);
          }
          
          // Symbol size, 3d point size
          if (symSize > 0)
            obj->symsize = symSize;
          if (ptSize >= 0)
            obj->pdrawsize = ptSize;
        } else {

          // Points per contour
          if (type >= 0)
            obj->extra[IOBJ_EX_PNT_LIMIT] = type;

          // Planar contours
          if (symbol >= 0)
            utilSetObjFlag(obj, 0, symbol != 0, IMOD_OBJFLAG_PLANAR);

          // Sphere on sec only
          if (symSize >= 0)
            utilSetObjFlag(obj, 0, symSize != 0, IMOD_OBJFLAG_PNT_ON_SEC);
        }        

        // The general draw updates object edit window, but need to call 
        // imodv object edit for it to update
        imodDraw(App->cvi, IMOD_DRAW_MOD);
        imodvObjedNewView();

        // If no contours and only 1 obj, set checksum to avoid save requests
        if (imod->objsize == 1 && !obj->contsize)
          imod->csum = imodChecksum(imod);
        break;

      case MESSAGE_GHOST_MODE:
        mode = messageStrings[++arg].toInt();
        mask = messageStrings[++arg].toInt();
        App->cvi->ghostmode = (App->cvi->ghostmode & ~mask) | (mode & mask);
        interval = messageStrings[++arg].toInt();
        if (interval >= 0)
          App->cvi->ghostdist = interval;
        imodDraw(App->cvi, IMOD_DRAW_MOD);
        break;
        
      case MESSAGE_ZAP_HQ_MODE:
        mode = messageStrings[++arg].toInt() != 0 ? 1 : 0;
        zap = getTopZapWindow(false);
        if (zap) {
          zap->stateToggled(ZAP_TOGGLE_RESOL, mode);
          zap->mQtWindow->setToggleState(ZAP_TOGGLE_RESOL, mode);
        } else
          zapSetNextOpenHQstate(mode);
        break;

      case MESSAGE_PLUGIN_EXECUTE:
        arg++;
        if (imodPlugMessage(App->cvi, &messageStrings, &arg)) {
          succeeded = 0;
          arg = numArgs;
          break;
        }
        break;

      default:
        imodPrintStderr("imodExecuteMessage: action %d not recognized\n"
                        , message_action);
        succeeded = 0;
        arg = numArgs;
      }
    } else {
      
      // Messages for 3dmodv
      switch (message_action) {
      case MESSAGE_QUIT:
        arg = numArgs;
        break;
        
      case MESSAGE_RAISE_WINDOWS:
        imodvInputRaise();
        break;
        
      default:
        imodPrintStderr("imodExecuteMessage: action %d not recognized by"
                        " 3dmodv\n" , message_action);
        succeeded = 0;
        arg = numArgs;
      }
    }
  }

  if (Imod_debug) {
    wprint("Checksum after = %d\n", newCheck);
    if (newCheck != checkSum)
      wprint("\aIT CHANGED IN THAT OPERATION\n");
    checkSum = newCheck;
  }

  // Now set the clipboard with the response
  if (succeeded >= 0)
    sendResponse(succeeded);
  return message_action == MESSAGE_QUIT;
}

// Send response to clipboard or standard error
void ImodClipboard::sendResponse(int succeeded)
{
  static int lastResponse = 0;
  QString str;
  if (succeeded < 0)
    succeeded = lastResponse;
  lastResponse = succeeded;
  if (mUseStdin) {
    imodPrintStderr("%s\n", succeeded ? "OK" : "ERROR");
  } else {
    str.sprintf("%u %s", ourWindowID(), succeeded ? "OK" : "ERROR");
    QClipboard *cb = QApplication::clipboard();
    //cb->setSelectionMode(false);
    cb->setText(str);
  }
}

unsigned int ImodClipboard::ourWindowID()
{
  if (ImodvClosed || !Imodv->standalone)
    return (unsigned int)ImodInfoWin->winId();
  return (unsigned int)Imodv->mainWin->winId();
}

#if defined(_WIN32) && defined(QT_THREAD_SUPPORT)

// The thread loops on reading a line and sets the flag when it gets one
void StdinThread::run()
{
  bool gotOne;
  bool quit;
  while (1) {
    mutex.lock();
    gotOne = gotLine;
    mutex.unlock();

    // Do not go for another line until the main thread has cleared the flag
    if (gotOne) {
      msleep(20);
      continue;
    }
    
    lineLen = readLine(threadLine);
    quit = (strlen(threadLine) == 1 && threadLine[0] == '4') || !lineLen;
    mutex.lock();
    gotLine = true;
    mutex.unlock();
    if (quit)
      break;
  }
}    
#endif

// Common routine to read a line, strip endings, and return length
static int readLine(char *line)
{
  if (!fgets(line, MAX_LINE, stdin)) {
    line[0] = 0x00;
    return 0;
  }
  if (line[MAX_LINE - 1])
    line[MAX_LINE - 1] = 0x00;
  while (strlen(line) > 0 && 
         (line[strlen(line) - 1] == '\n' || line[strlen(line) - 1] == '\r'))
    line[strlen(line) - 1] = 0x00;
  return strlen(line);
}
