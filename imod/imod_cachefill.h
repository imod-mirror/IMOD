#ifndef IMOD_CACHEFILL_H
#define IMOD_CACHEFILL_H

#include "dialog_frame.h"

#ifndef IMODP_H
typedef struct ViewInfo ImodView;
#endif

class QHButtonGroup;
class QVButtonGroup;
class QRadioButton;
class QCheckBox;

/* imod_cachefill.c */
int icfGetAutofill(void);
unsigned char *icfDoAutofill(ImodView *vw, int cz);
void imodCacheFillDialog(ImodView *vw);
void imodCacheFill(ImodView *vw);

class ImodCacheFill : public DialogFrame
{
  Q_OBJECT

 public:
  ImodCacheFill(QWidget *parent, const char *name = NULL);
  ~ImodCacheFill() {};

  public slots:
  void buttonPressed(int which);
  void fractionSelected(int which);
  void balanceSelected(int which);
  void overlapSelected(int which);
  void autoToggled(bool state);

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );

 private:
  QHButtonGroup *mFillGroup;
  QVButtonGroup *mBalanceGroup;
  QHButtonGroup *mOverlapGroup;
  QRadioButton *mOverlapRadio[3];
  QCheckBox *mAutoCheck;

};

#endif
