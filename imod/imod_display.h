/*   imodv_display.h  -  declarations for imodv_display.cpp
 *
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           
/*  $Author$

$Date$

$Log$
Revision 1.1.2.1  2002/12/14 05:46:23  mast
Initial creation

*/

#ifndef IMOD_DISPLAY_H
#define IMOD_DISPLAY_H

typedef struct imodglvisual {
  int doubleBuffer;
  int rgba;
  int colorBits;      // Color index depth, or r + b + g
  int depthBits;
  int validDirect;    // 1 for direct, -1 for invalid
  int dbRequested;
  int rgbaRequested;
  int depthEnabled;
} ImodGLVisual;

typedef struct imodglrequest {
  int doubleBuffer;
  int rgba;
  int colorBits;      // Color index depth, or r + b + g
  int depthBits;
} ImodGLRequest;

#ifdef __cplusplus
extern "C" {
#endif
  void imodAssessVisuals();
  void imodFindQGLFormat(ImodApp *ap, char **argv);
  ImodGLVisual *imodFindGLVisual(ImodGLRequest request);

#ifdef __cplusplus
}
#endif

#endif
