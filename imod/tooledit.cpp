// A simple subclass of QLineEdit that sends a return pressed signal
// when it loses focus
// It can also be set to a fixed width in columns
#include "tooledit.h"
ToolEdit::ToolEdit( QWidget * parent, int columns, const char * name)
  : QLineEdit(parent, name)
{
  int i, width;
  QString str;
  if (columns) {
    for (i= 0; i < columns; i++)
      str += "8";
    // Need to add 1.5 columns
    width = ((2 * columns + 3) * fontMetrics().width(str) ) / (2 * columns);
    setFixedWidth(width);
  }
}

ToolEdit::~ToolEdit()
{
}

void ToolEdit::focusOutEvent(QFocusEvent *event)
{
  emit lostFocus();
}
