/* Written by Dan Heller and Paula Ferguson.  
 * Copyright 1994, O'Reilly & Associates, Inc.
 * Permission to use, copy, and modify this program without
 * restriction is hereby granted, as long as this copyright
 * notice appears in each copy of the program source code.
 * This program is freely distributable without licensing fees and
 * is provided without guarantee or warrantee expressed or implied.
 * This program is -not- in the public domain.
 */

/*
 * Modified for IMOD by James Kremer. 
 * Prints to the imod information window.
 *
 */
/*  $Author$

$Date$

$Revision$

$Log$
Revision 3.1  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

*/

/* error_test.c -- test the error handlers and wprint() routine
 */
#include <stdio.h>
/*#include <varargs.h>  */
#include <stdarg.h>
#include <qtextedit.h>
#include <qapplication.h>

extern int Imod_debug;

#ifdef __cplusplus
extern "C" {
#endif
  void wprint(char *fmt, ...);
#ifdef __cplusplus
}
#endif

static QTextEdit *Wprint_text_output = NULL;

#ifdef X_APP_EXISTS
static int
x_error(Display *dpy, XErrorEvent  *err_event)
{
  char                buf[256];

  XGetErrorText (dpy, err_event->error_code, buf, (sizeof buf));

  wprint("X Error: <%s>\n", buf);
  return 0;
}

static void
xt_error(char *message)
{
  static int  error_in_here = False;
     
  if (error_in_here){
    fprintf(stderr, "Xt Error: %s\n", message);
  }else{
    error_in_here = True;
    wprint ("Xt Error: %s\n", message);
  }
  error_in_here = False;
}
#endif

void wprintWidget(QTextEdit *edit)
{
#ifdef X_APP_EXISTS
  if (!Imod_debug){
    /* catch Xt errors */
    XtAppSetErrorHandler (app, xt_error);
    XtAppSetWarningHandler (app, xt_error);
         
    /* and Xlib errors */
    XSetErrorHandler (x_error);
  }
#endif
     
  Wprint_text_output = edit;
  edit->setReadOnly(true);
}

/*VARARGS*/
void wprint(char *fmt, ...)
{
  char msgbuf[1000];
  va_list args;
  bool nopos = false;
  int i, len;

  if (!Wprint_text_output)
    return;

  va_start (args, fmt);
  /*     fmt = va_arg (args, char *); */

  len = strlen(fmt);
  for(i = 0; i < len; i++)
    if (fmt[i] == 0x07)
      QApplication::beep();

  if (fmt[strlen(fmt) - 1] == '\r'){
    nopos = true;
  }

#ifndef NO_VPRINTF
  (void) vsprintf (msgbuf, fmt, args);
#else /* !NO_VPRINTF */
  {
    FILE foo;
    foo._cnt = 256;
    foo._base = foo._ptr = msgbuf; /* (unsigned char *) ?? */
    foo._flag = _IOWRT+_IOSTRG;
    (void) _doprnt (fmt, args, &foo);
    *foo._ptr = '\0'; /* plant terminating null character */
  }
#endif /* NO_VPRINTF */
  va_end (args);
     
  QString str = msgbuf;
  if (nopos)
    Wprint_text_output->setText(str);
  else
    Wprint_text_output->append(str);
  Wprint_text_output->scrollToBottom();
}


