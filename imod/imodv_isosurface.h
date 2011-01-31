//Added by qt3to4:
#include <QLabel>
#include <QKeyEvent>
#include <QCloseEvent>
/*  imodv_isosurface.h  -  declarations for imodv_isosurface.cpp
 *
 *  $Id$
 *
 * $Log$
 * Revision 4.11  2011/01/21 17:36:57  mast
 * changes for masking, outer limits, etc.
 *
 * Revision 4.10  2010/03/30 02:22:31  mast
 * Added to thread limit comment
 *
 * Revision 4.9  2009/01/15 16:33:18  mast
 * Qt 4 port
 *
 * Revision 4.8  2008/11/07 23:48:53  xiongq
 * seperate threshold for each stack
 *
 * Revision 4.7  2008/10/02 16:27:00  xiongq
 * add small piece filter, binning, and local XYZ functions
 *
 * Revision 4.6  2008/05/27 18:20:15  mast
 * Limited to 4 threads
 *
 *
 */

#ifndef IMODV_ISOSURFACE_H
#define IMODV_ISOSURFACE_H

typedef struct __imodv_struct ImodvApp;
typedef unsigned int Index;
extern void smooth_vertex_positions(float *varray, Index nv,
    const Index *tarray, Index nt,
    float smoothing_factor, int smoothing_iterations);

// Based on timing tests in May 2008, confirmed on Nehalem March 2010
#define MAX_THREADS 4

/* Image Control functions. */
void imodvIsosurfaceEditDialog(ImodvApp *a, int state);
bool imodvIsosurfaceUpdate(void);

#include "imodel.h"
#include "dialog_frame.h"
#include <qspinbox.h>
#include <vector>

class MultiSlider;
class QCheckBox;
class QLabel;
class QSlider;
class QLineEdit;
//class QSpinBox;
class QPushButton;
class HistWidget;
struct ViewInfo;
class IsoThread;
class Surface_Pieces;

typedef struct {
  int ix, iy, iz;
} IsoPoint3D;

typedef struct {
  int trans;
  unsigned char r, g, b, dummy;
} IsoColor;

typedef struct {
  float x, y, z;
  float size;
  int colorInd;
  bool drawn;
} IsoPaintPoint;


class ImodvIsosurface : public DialogFrame
{
  Q_OBJECT

 public:
  ImodvIsosurface(struct ViewInfo *vi, QWidget *parent, const char *name = NULL) ;
  ~ImodvIsosurface();
  void updateCoords(bool setLocal);
  void setBoundingBox();
  void setBoundingObj();
  void setViewCenter();
  int getCurrStackIdx();
  void setIsoObj(bool fillPaint);
  void fillAndProcessVols(bool setThresh);
  void resizeToContours(bool draw);
  int getBinning();
  bool fillPaintVol();
  void paintMesh();
  void managePaintObject();

  int mLocalX;
  int mLocalY;
  int mLocalZ;
  int mMaskObj;
  int mMaskCont;
  int mMaskPsize;
  int mCurrTime;
  int mBoxOrigin[3];
  int mBoxEnds[3];
  unsigned char *mVolume;
  int mCurrStackIdx; // wihich stack is current;
  std::vector<float> mStackThresholds;
  float mThreshold; //threshold for the current stack;
  std::vector<int> mStackOuterLims;
  int mOuterLimit;
  int mBinBoxSize[3];
  int mSubZEnds[MAX_THREADS+1];
  unsigned char *mBinVolume;
  int mNThreads;
  int mLastObjsize;


  public slots:
    void viewIsoToggled(bool state) ;
    void viewModelToggled(bool state) ;
    void viewBoxingToggled(bool state);
    void centerVolumeToggled(bool state);
    void deletePiecesToggled(bool state);
    void linkXYZToggled(bool state);
    void closeFacesToggled(bool state);
    void histChanged(int, int, bool );
    void iterNumChanged(int);
    void binningNumChanged(int);
    void sliderMoved(int which, int value, bool dragging);
    void buttonPressed(int which);
    void showRubberBandArea();
    void numOfTrianglesChanged(int);
    void maskSelected(int which);
    void areaFromContClicked();
    void paintObjToggled(bool state);
    void paintObjChanged(int value);

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );
  void fontChange( const QFont & oldFont );

 private:
  float fillVolumeArray();
  void  fillBinVolume();
  void removeOuterPixels();
  void smoothMesh(Imesh *, int);
  void filterMesh(bool);
  bool allocArraysIfNeeded();
  int maskWithContour(Icont *inCont, int iz);
  void closeBoxFaces();
  void applyMask();
  void showDefinedArea(float x0, float x1, float y0, float y1, bool draw);
  int findClosestZ(int iz, int *listz, int zlsize, int **contatz, int *numatz, int zmin,
                   int &otherSide);
  void addToNeighborList(IsoPoint3D **neighp, int &numNeigh, int &maxNeigh,
                         int ix, int iy, int iz);
  void dumpVolume(char *filename);
  void setFontDependentWidths();

  bool mCtrlPressed;
  struct ViewInfo *mVi;
  int mExtraObjNum;
  int mCurrMax;
  IsoThread *threads[MAX_THREADS];

  Imesh * mOrigMesh;
  Imesh * mFilteredMesh;
  bool mMadeFiltered;
  QCheckBox *mViewIso, *mViewModel, *mViewBoxing, *mCenterVolume;
  QCheckBox *mLinkXYZ, *mDeletePieces, *mPaintCheck; 
  MultiSlider *mSliders;
  HistWidget *mHistPanel;
  MultiSlider *mHistSlider;
  QPushButton *mUseRubber, *mSizeContours;
  QSpinBox *mSmoothBox;
  QSpinBox *mBinningBox;
  QSpinBox *mPiecesBox;
  QSpinBox *mPaintObjSpin;

  int mBoxObjNum;
  int mBinBoxEnds[3];
  int mBoxSize[3];
  int mPaintSize[3];

  unsigned char *mTrueBinVol;
  unsigned char *mPaintVol;
  float mMedian;
  int mVolMin, mVolMax;
  int mInitNThreads;
  Surface_Pieces *mSurfPieces;

  std::vector<IsoColor> mColorList;
  std::vector<IsoPaintPoint> mPaintPoints;

};

#endif
