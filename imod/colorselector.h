/*   colorselector.h  -  declarations for colorselector.cpp
 *
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
*/

#include <qwidget.h>

class MultiSlider;
class QFrame;

class ColorSelector : public QWidget
{
  Q_OBJECT

 public:
  ColorSelector(QWidget *parent, QString label, int red, int green, int blue, 
                const char *name = NULL, 
                WFlags fl =  Qt::WDestructiveClose | Qt::WType_Dialog);
  ~ColorSelector();

 signals:
  void newColor(int r, int g, int b);
  void done();
  void closing();
  void keyPress( QKeyEvent * e );
  void keyRelease( QKeyEvent * e );

  public slots:
    void donePressed();
    void restorePressed();
    void qtSelectorPressed();
    void sliderChanged(int which, int value, bool dragging);

 protected:
    void closeEvent ( QCloseEvent * e );
    void keyPressEvent ( QKeyEvent * e );
    void keyReleaseEvent ( QKeyEvent * e );

 private:
    void imposeColor(bool setSliders, bool emitSignal);
    bool mCtrlPressed;
    int mOriginalRGB[3];
    int mCurrentRGB[3];
    MultiSlider *mSliders;
    QFrame *mColorBox;
};
