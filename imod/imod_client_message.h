/*  IMOD VERSION 2.7.2
 *
 *  $Id$
 *
 *  Original Author: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-1998 by Boulder Laboratory for 3-Dimensional Fine    *
 *   Structure ("BL3DFS") and the Regents of the University of Colorado.     *
 *                                                                           *
 *   BL3DFS reserves the exclusive rights of preparing derivative works,     *
 *   distributing copies for sale, lease or lending and displaying this      *
 *   software and documentation.                                             *
 *   Users may reproduce the software and documentation as long as the       *
 *   copyright notice and other notices are preserved.                       *
 *   Neither the software nor the documentation may be distributed for       *
 *   profit, either in original form or in derivative works.                 *
 *                                                                           *
 *   THIS SOFTWARE AND/OR DOCUMENTATION IS PROVIDED WITH NO WARRANTY,        *
 *   EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTY OF          *
 *   MERCHANTABILITY AND WARRANTY OF FITNESS FOR A PARTICULAR PURPOSE.       *
 *                                                                           *
 *   This work is supported by NIH biotechnology grant #RR00592,             *
 *   for the Boulder Laboratory for 3-Dimensional Fine Structure.            *
 *   University of Colorado, MCDB Box 347, Boulder, CO 80309                 *
 *****************************************************************************/
/*  $Author$

$Date$

$Revision$

$Log$
Revision 3.1  2003/02/10 20:41:55  mast
Merge Qt source

Revision 3.0.2.1  2003/01/27 00:30:07  mast
Pure Qt version and general cleanup

Revision 3.0  2002/09/27 20:35:04  rickg
Initital version of code moved from imod_menu_cb.c

*/
/* imod_client_message.h */

#ifndef IMOD_CLIENT_MESSAGE_H
#define IMOD_CLIENT_MESSAGE_H

#include <qobject.h>
#include <qstring.h>
class QTimer;

class ImodClipboard : public QObject
{
  Q_OBJECT

 public:
  ImodClipboard();
  ~ImodClipboard() {};
  bool handleMessage();
  bool executeMessage();
  void sendResponse(int succeeded);


  QTimer *mClipTimer;
  QTimer *mClipHackTimer;

 public slots:
  void clipTimeout();
  void clipHackTimeout();
  void clipboardChanged();

 private:  
  bool mHandling;
  bool mExiting;
  QString mSavedClipboard;
};


#endif /* IMOD_CLIENT_MESSAGE_H */
