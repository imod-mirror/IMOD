/*   imodview.h  -  header file for private imodview functions
 *
 *   Copyright (C) 1995-2003 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
Revision 1.1  2003/10/01 05:01:29  mast
Initial creation; functions pulled from imodP.h

*/
#ifndef IMODVIEWP_H
#define IMODVIEWP_H

int ivwReopen(ImodImageFile *inFile);
unsigned char **ivwGetCurrentSection(ImodView *iv);
unsigned char **ivwMakeLinePointers(ImodView *iv, unsigned char *data,
                                    int xsize, int ysize, int mode);
int ivwSetupFastAccess(ImodView *vi, unsigned char ***outImdata,
                       int inNullvalue, int *cacheSum);
int ivwInitCache(ImodView *vi);

void ivwBindMouse(ImodView *vw);

int  ivwScale(ImodView *vw);
int  ivwFlip(ImodView *vw);
void ivwInit(ImodView *vi);
int  ivwPointVisible(ImodView *vw, Ipoint *pnt);

int  imodImageFileDesc(FILE *fin);
int  ivwLoadImage(ImodView *iv);
void ivwFlushCache(ImodView *vi);
int  ivwSetScale(ImodView *vi);
void ivwMultipleFiles(ImodView *iv, char *argv[], int firstfile, 
		      int lastimage);

void ivwTransModel(ImodView *iv);
void ivwSetModelTrans(ImodView *iv);
void ivwFlipModel(ImodView *iv);
void ivwCheckWildFlag(Imod *imod);
void ivwScaleDepth8(ImodView *iv, ivwSlice *tempSlicePtr);
void ivwReadZ(ImodView *iv, unsigned char *buf, int cz);
void memreccpy
(unsigned char *tb,             /* copy data to buffer */
 unsigned char *fb,             /* copy data from buffer */
 int xcpy, int ycpy, int psize, /* amount/size of data to copy. */
 int tskip, int tox, int toy,   /* to buffer offsets, skip. */
 int fskip, int fox, int foy);   /* from buffer offsets, skip. */

#endif
