/*   multislider.h  -  declarations for multislider.cpp
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

#ifndef MULTISLIDER_H
#define MULTISLIDER_H
#include <qobject.h>

class QSlider;
class QVBoxLayout;
class QLabel;

class MultiSlider : public QObject
{
  Q_OBJECT

 public:
  MultiSlider(QWidget *parent, int numSliders, char *titles[], int minVal = 0,
              int maxVal = 255);
  ~MultiSlider();

  void setValue(int slider, int value);
  void setRange(int slider, int minVal, int maxVal);
  QSlider *getSlider(int slider);
  QVBoxLayout *getLayout() {return mBigLayout;};
  

 signals:
  void sliderChanged(int slider, int value, bool dragging);

  public slots:
    void valueChanged(int value);
  void sliderActive(int which);
  void sliderPressed(int which) {mPressed[which] = true;};
  void sliderReleased(int which);


 private:
  void processChange();  // Process a changed value if both signals are in
  void displayValue(int slider, int value);

  int mNumSliders;     // Number of sliders
  int mNewValue;        // incoming new value
  int mActiveSlider;    // Slider that changed
  bool *mPressed;       // pressed flags
  QVBoxLayout *mBigLayout;
  QSlider **mSliders;
  QLabel **mLabels;
};
#endif
