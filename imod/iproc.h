/*  IMOD VERSION 2.50
 *
 *  $Id$
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2001 by Boulder Laboratory for 3-Dimensional Fine    *
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
    Revision 3.8  2004/11/07 23:04:47  mast
    Changes for thread and FFT stuff

    Revision 3.7  2004/11/04 23:30:55  mast
    Changes for rounded button style

    Revision 3.6  2004/01/22 19:10:12  mast
    Added slot for real button press

    Revision 3.5  2004/01/05 18:03:50  mast
    renamed vw to vi

    Revision 3.4  2003/09/16 02:10:55  mast
    Added working array because displayed image data cannot be used directly

    Revision 3.3  2003/02/10 20:41:56  mast
    Merge Qt source

    Revision 3.2.2.2  2003/01/27 00:30:07  mast
    Pure Qt version and general cleanup

    Revision 3.2.2.1  2003/01/23 19:57:06  mast
    Qt version

    Revision 3.2  2002/12/01 15:34:41  mast
    Changes to get clean compilation with g++

*/

#ifndef BD_IPROC_H_
#define BD_IPROC_H_

#define PROC_BACKGROUND 0
#define PROC_FOREGROUND 255

#include "dialog_frame.h"
class QWidgetStack;
class QListBox;
class QVBoxLayout;
class QLabel;
class QSpinBox;

typedef struct ViewInfo ImodView;

#ifdef QT_THREAD_SUPPORT
#include <qthread.h>

class IProcThread : public QThread
{
 public:
  IProcThread() {};
  ~IProcThread() {};

 protected:
  void run();
};
#endif

class IProcWindow : public DialogFrame
{
  Q_OBJECT

 public:
  IProcWindow(QWidget *parent, const char *name = NULL);
  ~IProcWindow() {};
  bool mRunningProc;
  void limitFFTbinning();

  public slots:
  void buttonClicked(int which);
  void buttonPressed(int which);
  void edgeSelected(int which);
  void filterSelected(int which);
  void filterHighlighted(int which);
  void threshChanged(int which, int value, bool dragging);
  void fourFiltChanged(int which, int value, bool dragging);
  void binningChanged(int val);
  void subsetChanged(bool state);

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );
  void fontChange( const QFont & oldFont );
  void timerEvent(QTimerEvent *e);

 private:
  QWidgetStack *mStack;
  QListBox *mListBox;
  void apply();
  void startProcess();
  void finishProcess();
  int mTimerID;
#ifdef QT_THREAD_SUPPORT
  QThread *mProcThread;
#endif
};

typedef struct
{
  IProcWindow   *dia;
  ImodView      *vi;        /* image data to model                       */
  unsigned char *iwork;     /* Image data processing buffer.             */
  unsigned char *isaved;     /* buffer for saving original data.         */

  int           idatasec;   /* data section. */
  int           idatatime;  /* time value of section */
  int           procnum;
  int           modified;   /* flag that section data are modified */

  int           threshold;  /* Parameters for individual filters */
  int           edge;
  float         radius1;
  float         radius2;
  float         sigma1;
  float         sigma2;
  int           fftBinning;
  bool          fftSubset;
  float         fftScale;
  float         fftXrange;
  float         fftYrange;
  QSpinBox      *fftBinSpin;
  QLabel        *fftLabel1;
  QLabel        *fftLabel2;
} ImodIProc;


typedef struct
{
  char *name;         /* Name of index */
  void (*cb)();       /* callback to do action */
  /* function to make widget */
  void (*mkwidget)(IProcWindow *, QWidget *, QVBoxLayout *); 
  char *label;
} ImodIProcData;

int inputIProcOpen(ImodView *vw);
int iprocRethink(ImodView *vw);
bool iprocBusy(void);

#endif /* BD_IPROC_H_ */
