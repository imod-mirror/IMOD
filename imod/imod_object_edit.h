/*  IMOD VERSION 2.7.8
 *
 *  $Id$
 *
 *  Original Author: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-1998 by Boulder Laboratory for 3-Dimensional Fine    *
 *   Structure ("BL3DFS") and the Regents of the University of Colorado.     *
 *                                                                           *
 *   BL3DFS reserves the exclusive rights of preparing derivative works,     *
 *   distributing copies for sale, lease or lending and displaying this      *
 *   software and documentation.                                             *
 *   Users may reproduce the software and documentation as long as the       *
 *   copyright notice and other notices are preserved.                       *
 *   Neither the software nor the documentation may be distributed for       *
 *   profit, either in original form or in derivative works.                 *
 *                                                                           *
 *   THIS SOFTWARE AND/OR DOCUMENTATION IS PROVIDED WITH NO WARRANTY,        *
 *   EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTY OF          *
 *   MERCHANTABILITY AND WARRANTY OF FITNESS FOR A PARTICULAR PURPOSE.       *
 *                                                                           *
 *   This work is supported by NIH biotechnology grant #RR00592,             *
 *   for the Boulder Laboratory for 3-Dimensional Fine Structure.            *
 *   University of Colorado, MCDB Box 347, Boulder, CO 80309                 *
 *****************************************************************************/
/*  $Author$

$Date$

$Revision$

$Log$
Revision 1.1.2.1  2002/12/05 16:30:22  mast
First addition to archive


*/

#ifndef IMOD_OBJECT_EDIT_H
#define IMOD_OBJECT_EDIT_H

#ifdef __cplusplus
extern "C" {
#endif
  /* This can't be here because this header will be read by Qt files */
  /* void ioew_sgicolor_cb(Widget w, XtPointer client, XtPointer call); */
  void ioew_help(void);
  void ioew_quit(void);
  void ioew_closing(void);
  void ioew_draw(int state);
  void ioew_fill(int state);
  void ioew_ends(int state);
  void ioew_linewidth(int value);
  void ioew_open(int value);
  void ioew_surface(int value);
  void ioew_pointsize(int value);
  void ioew_nametext(const char *name);
  void ioew_symbol(int value);
  void ioew_symsize(int value);
  void ioew_time(int state);
  int imod_object_edit_draw(void);
  int  imod_object_edit();

#ifdef __cplusplus
}
#endif

#endif /* IMOD_OBJECT_EDIT_H */
