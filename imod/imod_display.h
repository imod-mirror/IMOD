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

$Log$
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

#ifdef __cplusplus
}
#endif

#endif
