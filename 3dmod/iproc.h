/*
 *  iproc.h - declarations for IProcWindow class and processing window
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 *
 *  $Id$
 */

#ifndef BD_IPROC_H_
#define BD_IPROC_H_

#define PROC_BACKGROUND 0
#define PROC_FOREGROUND 255

#include "mrcslice.h"
#include "dialog_frame.h"
//Added by qt3to4:
#include <QCloseEvent>
#include <QTimerEvent>
#include <QLabel>
#include <QKeyEvent>
#include <vector>
class QStackedWidget;
class QListWidget;
class QVBoxLayout;
class QLabel;
class QSpinBox;
class QPushButton;
class ToolEdit;
class QDoubleSpinBox;
class QListWidgetItem;

typedef struct ViewInfo ImodView;

#include <qthread.h>

typedef struct
{
  int           procNum;
  int           threshold;  /* Parameters for individual filters */
  bool          threshGrow;
  bool          threshShrink;
  int           edge;
  float         kernelSigma;
  bool          rescaleSmooth;
  float         radius1;
  float         radius2;
  float         sigma1;
  float         sigma2;
  int           fftBinning;
  bool          fftSubset;
  bool          median3D;
  int           medianSize;
  int           andfIterations;
  int           andfIterDone;
  double        andfK;
  double        andfLambda;
  int           andfStopFunc;
} IProcParam;

class IProcThread : public QThread
{
 public:
  IProcThread() {};
  ~IProcThread() {};

 protected:
  void run();
};

class IProcWindow : public DialogFrame
{
  Q_OBJECT

 public:
  IProcWindow(QWidget *parent, const char *name = NULL);
  ~IProcWindow() {};
  bool mRunningProc;
  void (*mCallback)();
  void limitFFTbinning();
  void apply(bool useStack = false);

  public slots:
  void buttonClicked(int which);
  void buttonPressed(int which);
  void autoApplyToggled(bool state);
  void edgeSelected(int which);
  void filterSelected(QListWidgetItem *item);
  void filterHighlighted(int which);
  void threshChanged(int which, int value, bool dragging);
  void fourFiltChanged(int which, int value, bool dragging);
  void binningChanged(int val);
  void kernelChanged(double val);
  void scaleSmthToggled(bool state);
  void subsetChanged(bool state);
  void growChanged(bool state);
  void shrinkChanged(bool state);
  void medSizeChanged(int val);
  void med3DChanged(bool state);
  void andfIterChanged(int val);
  void andfFuncClicked(int val);
  void andfKEntered();
  void reportFreqClicked();

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );
  void changeEvent(QEvent *e);
  void timerEvent(QTimerEvent *e);

 private:
  QStackedWidget *mStack;
  QListWidget *mListBox;
  void startProcess();
  void finishProcess();
  void manageListSize();
  int mTimerID;
  QThread *mProcThread;
  std::vector<IProcParam> mParamStack;
  int mUseStackInd;
};

typedef struct
{
  IProcWindow   *dia;
  ImodView      *vi;        /* image data to model                       */
  unsigned char *iwork;     /* Image data processing buffer.             */
  unsigned char *isaved;     /* buffer for saving original data.         */
  float         **andfImage; /* Double buffers for aniso diff */
  float         **andfImage2;
  Istack         medianVol;

  int           idataSec;   /* data section. */
  int           idataTime;  /* time value of section */
  int           modified;   /* flag that section data are modified */
  bool          autoApply;  /* Apply automatically when changing ssection */
  int           rangeLow;   /* Low and high range values when image mapped to slice */
  int           rangeHigh;
  QDoubleSpinBox *kernelSpin;
  float         fftScale;
  float         fftXrange;
  float         fftYrange;
  QSpinBox      *fftBinSpin;
  QLabel        *fftLabel1;
  QLabel        *fftLabel2;
  QLabel        *fftLabel3;
  QPushButton   *freqButton;
  int           fftXcen;
  int           fftYcen;
  ToolEdit      *andfKEdit;
  ToolEdit      *andfLambdaEdit;
  QLabel        *andfScaleLabel;
  QLabel        *andfDoneLabel;
  
} ImodIProc;

typedef struct
{
  const char *name;         /* Name of index */
  void (*cb)();       /* callback to do action */
  /* function to make widget */
  void (*mkwidget)(IProcWindow *, QWidget *, QVBoxLayout *); 
  const char *label;
} ImodIProcData;

int inputIProcOpen(ImodView *vw);
int iprocRethink(ImodView *vw);
bool iprocBusy(void);
void iprocUpdate(void);
void iprocCallWhenFree(void (*func)());

#endif /* BD_IPROC_H_ */
