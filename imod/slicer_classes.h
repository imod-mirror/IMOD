/*   slicer_classes.h  -  declarations for slicer_classes.cpp
 *
 *   Copyright (C) 1995-2003 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
*/
#ifndef SLICER_CLASSES_H
#define SLICER_CLASSES_H

#define MAX_SLICER_TOGGLES 2

#include <qmainwindow.h>
#include <qspinbox.h>
#include <qtoolbar.h>
#include <qgl.h>

class QToolButton;
class ToolEdit;
class QLabel;
class QSignalMapper;
class QSlider;
class QTimer;
class MultiSlider;
class QComboBox;

typedef struct Super_slicer SlicerStruct;
class SlicerGL;
class SlicerCube;
class FloatSpinBox;
class HotToolBar;

class SlicerWindow : public QMainWindow
{
  Q_OBJECT

 public:
  SlicerWindow(SlicerStruct *slicer, float maxAngles[], bool rgba, 
            bool doubleBuffer, bool enableDepth, QWidget * parent = 0,
            const char * name = 0, 
	    WFlags f = WType_TopLevel | WDestructiveClose) ;
  ~SlicerWindow() {};
  SlicerGL *mGLw;
  SlicerCube *mCube;
  HotToolBar *mToolBar;
  HotToolBar *mToolBar2;

  public slots:
    void zoomUp();
  void zoomDown();
  void help();
  void newZoom();
  void angleChanged(int which, int value, bool dragging);
  void toggleClicked(int index);
  void setToggleState(int index, int state);
  void setZoomText(float zoom);
  void setModelThickness(float depth);
  void setImageThickness(int depth);
  void imageThicknessChanged(int depth);
  void modelThicknessChanged(int depth);
  void showslicePressed();
  void zScaleSelected(int item);
  void setAngles(float *angles);
  void toolKeyPress(QKeyEvent *e) {keyPressEvent(e);};
  void toolKeyRelease(QKeyEvent *e) {keyReleaseEvent(e);};

 protected:
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );
  void closeEvent ( QCloseEvent * e );
  
 private:
  void setupToggleButton(QToolBar *toolBar, QSignalMapper *mapper, 
			 int index);
  
  SlicerStruct *mSlicer;
  QToolButton *mToggleButs[MAX_SLICER_TOGGLES];
  int mToggleStates[MAX_SLICER_TOGGLES];
  ToolEdit *mZoomEdit;
  QSpinBox *mImageBox;
  FloatSpinBox *mModelBox;
  MultiSlider *mSliders;
  QComboBox *mZscaleCombo;
};

class SlicerGL : public QGLWidget
{
  Q_OBJECT

 public:
  SlicerGL(SlicerStruct *slicer, QGLFormat format, QWidget * parent = 0,
        const char * name = 0);
  ~SlicerGL() {};
 
protected:
  void initializeGL() {};
  void paintGL();
  void resizeGL( int wdth, int hght );
  void mousePressEvent(QMouseEvent * e );
  void mouseReleaseEvent ( QMouseEvent * e );

 private:
  SlicerStruct *mSlicer;
  bool mMousePressed;
};

// The cube drawing class
class SlicerCube : public QGLWidget
{
  Q_OBJECT

 public:
  SlicerCube(SlicerStruct *slicer, QGLFormat format, 
	     QWidget * parent = 0, const char * name = 0);
  ~SlicerCube() {};
 
protected:
  void initializeGL() {};
  void paintGL();
  void resizeGL( int wdth, int hght );

 private:
  SlicerStruct *mSlicer;
};

// A floating spin button class
class FloatSpinBox : public QSpinBox
{
  Q_OBJECT
    public:
  FloatSpinBox( int minValue, int maxValue, int step = 10, 
		QWidget * parent = 0, const char * name = 0);
  ~FloatSpinBox() {};

  QString mapValueToText( int value );
  int mapTextToValue( bool *ok );
};
  

// A toolbar class that will pass on keys
class HotToolBar : public QToolBar
{
  Q_OBJECT
 public:
  HotToolBar( QMainWindow * parent = 0, const char * name = 0) 
    : QToolBar(parent, name) { };
  ~HotToolBar() {}

 signals:
  void keyPress(QKeyEvent *e);
  void keyRelease(QKeyEvent *e);

 protected:
  void keyPressEvent ( QKeyEvent * e ) {emit keyPress(e);};
  void keyReleaseEvent ( QKeyEvent * e ) {emit keyRelease(e);};
};

#endif     // SLICER_CLASSES_H
