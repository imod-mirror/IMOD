#ifndef TOOLEDIT_H
#define TOOLEDIT_H
#include <qlineedit.h>
class ToolEdit : public QLineEdit
{
  Q_OBJECT

 public:
  ToolEdit( QWidget * parent, int columns = 0, const char * name = 0 );
  ~ToolEdit();

 signals:
  void lostFocus();

 protected:
  void focusOutEvent(QFocusEvent *event);


};
#endif
