/*   imod_info_cb.h  -  declarations for info-window related functions in
 *                      imod_info_cb.cpp
 *
 *   Copyright (C) 1995-2003 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

    $Date$

    $Revision$

    $Log$
    Revision 1.1.2.1  2003/01/13 01:04:51  mast
    Initial creation

*/
#ifndef IMOD_INFO_CB_H
#define IMOD_INFO_CB_H
#ifdef __cplusplus
extern "C" {
#endif

#ifndef IMODP_H
  typedef struct ViewInfo ImodView;
#endif

  void imodInfoNewOCP(int which, int value, int edited);
  void imodInfoNewXYZ(int *values);
  void imodInfoNewBW(int which, int value, int dragging);
  void imodInfoFloat(int state);
  void imodInfoMMSelected(int mode);
  void imodInfoCtrlPress(int pressed);
  void imod_info_setobjcolor(void);
  void imod_info_setocp(void);
  void imod_info_setxyz(void);
  void imod_info_setbw(int black, int white);
  int imod_info_bwfloat(ImodView *vw, int section, int time);
  void imod_info_float_clear(int section, int time);
  int imod_open(FILE *mfin);
  void show_status(char *info);
  void imod_show_info(char *info, int line);
  void imod_info_msg(char *top, char *bot);
  void imod_info_forbid(void);
  void imod_info_enable(void);
  int imod_info_input(void);
  void imod_set_mmode(int mode);
  void imod_draw_window(void);
  void imod_imgcnt(char *string);
 
#ifdef __cplusplus
}
#endif

#endif
