// A simple subclass of QLineEdit that sends a return pressed signal
// when it loses focus
#include "tooledit.h"
ToolEdit::ToolEdit( QWidget * parent, const char * name = 0 )
  : QLineEdit(parent, name)
{
}
ToolEdit::~ToolEdit()
{
}

void ToolEdit::focusOutEvent(QFocusEvent *event)
{
  emit lostFocus();
}
