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
#ifndef IMOD_MOVIECON_H
#define IMOD_MOVIECON_H

#ifndef IMODP_H
typedef struct ViewInfo ImodView;
#endif

#ifdef __cplusplus
extern "C" {
#endif

/* imod_moviecon.c dialog */
int imcGetIncrement(ImodView *vw, int xyzt);
void imcGetStartEnd(ImodView *vw, int xyzt, int *stout, int *endout);
void imodMovieConDialog(ImodView *vw);
float imcGetInterval(void);
void imcSetMovierate(ImodView *vw, int newrate);
void imcResetAll(ImodView *vw);
int imcGetLoopMode(ImodView *vw);
int imcGetSnapshot(ImodView *vw);
void imcStartTimer(void);
void imcReadTimer(void);

  void imcHelp();
  void imcClosing();
  void imcResetPressed();
  void imcSliderChanged(int which, int value);
  void imcAxisSelected(int which);
  void imcExtentSelected(int which);
  void imcSnapSelected(int which);;
  void imcRateEntered(float value);
  void imcIncrementRate(int dir);


#ifdef __cplusplus
}
#endif

#endif
