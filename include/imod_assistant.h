/*
 *  imod_assistant.h - declarations for imod_assistant.cpp
 */

/*  $Author$

    $Date$

    $Revision$

    $Log$
    Revision 1.1  2004/12/04 02:05:39  mast
    Added to libdiaqt

    Revision 1.2  2004/11/24 18:30:16  mast
    Add adp file on startup

    Revision 1.1  2004/11/22 00:21:46  mast
    Addition to program

*/
#ifndef IMOD_ASSISTANT_H
#define IMOD_ASSISTANT_H

#include <qobject.h>
#include <qstring.h>
#include "dllexport.h"
class QAssistantClient;

class DLL_IM_EX ImodAssistant : public QObject
{
  Q_OBJECT

public:
  ImodAssistant(const char *path, const char *adpFile, bool absolute = false,
                bool reportErrors = true);
  ~ImodAssistant();
  int showPage(const char *page, bool absolute = false);

 signals:
  void error(const QString &msg);

public slots:
  void assistantError(const QString &msg);
  
private:
  QString mPath;
  QString mAdp;
  QAssistantClient *mAssistant;
  bool mReportErrors;
};

#endif
