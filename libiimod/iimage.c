/*
 *    iimage.c    - general routines for manipulating ImodImageFile's
 *
 *    Authors:  James Kremer and David Mastronarde
 *
 *   Copyright (C) 1995-2007 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.
 *
 *  $Id$
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "iimage.h"
#include "ilist.h"
#include "b3dutil.h"

static char *defMode = "rb";

/* The resident check list */
static Ilist *checkList = NULL;
static int readWriteSection(ImodImageFile *inFile, char *buf, int inSection, 
                            iiSectionFunc func);

/* Initialize check list: if it does not exist, allocate it and place TIFF and
   MRC functions on the list */
static int initCheckList()
{
  IIFileCheckFunction func;
  if (checkList)
    return 0;
  checkList = ilistNew(sizeof(IIFileCheckFunction), 6);
  if (!checkList)
    return 1;
  func = iiTIFFCheck;
  ilistAppend(checkList, &func);
  func = iiMRCCheck;
  ilistAppend(checkList, &func);
  func = iiLikeMRCCheck;
  ilistAppend(checkList, &func);
  return 0;
}

/*!
 * Adds the given function to the end of the list of functions that iiOpen will
 * call to check a file.  The format of the function is
 * ^  int iiFormatCheck(ImodImageFile *inFile)
 */
void iiAddCheckFunction(IIFileCheckFunction func)
{
  if (initCheckList())
    return;
  ilistAppend(checkList, &func);
}

/*!
 * Insert the given function into the list of functions that iiOpen will
 * call to check a file at position [index] in the list.  The format of the
 * function is
 * ^  int iiFormatCheck(ImodImageFile *inFile)
 */
void iiInsertCheckFunction(IIFileCheckFunction func, int index)
{
  if (initCheckList())
    return;
  if (index < ilistSize(checkList))
    ilistInsert(checkList, &func, index);
  else
    ilistAppend(checkList, &func);
}

/*!
 * Frees the checking list to avoid memory leaks
 */
void iiDeleteCheckList()
{
  if (!checkList)
    return;
  ilistDelete(checkList);
  checkList = NULL;
}

/*!
 * Creates a new image file structure and initialize it to default or
 * null values.  Returns 1 for error.
*/ 
ImodImageFile *iiNew()
{
  ImodImageFile *ofile = (ImodImageFile *)malloc(sizeof(ImodImageFile));
     
  if (!ofile) 
    return NULL;
  memset(ofile, 0, sizeof(ImodImageFile));
  ofile->xscale = ofile->yscale = ofile->zscale = 1.0f;
  ofile->slope  = 1.0f;
  ofile->smax   = 255;
  ofile->axis   = 3;
  ofile->mirrorFFT = 0;
  ofile->anyTiffPixSize = 0;
  ofile->format = IIFILE_UNKNOWN;
  ofile->fp     = NULL;
  ofile->readSection     = NULL;
  ofile->readSectionByte = NULL;
  ofile->readSectionUShort = NULL;
  ofile->cleanUp         = NULL;
  ofile->reopen          = NULL;
  ofile->close           = NULL;
  ofile->writeSection    = NULL;
  ofile->colormap        = NULL;

  /* DNM 2/26/03: set upper right to -1 for later replacement */
  ofile->llx =  0;
  ofile->lly =  0;
  ofile->llz =  0;
  ofile->urx = -1;
  ofile->ury = -1;
  ofile->urz = -1;
  ofile->nx = 0;
  ofile->ny = 0;
  ofile->nz = 0;
  return(ofile);
}

/*!
 * Initializes the image file structure [i] for the given size and other
 * characteristics 
 */
int iiInit(ImodImageFile *i, int xsize, int ysize, int zsize, 
           int file, int format, int type)
{
  if (!i)     return(-1);
  i->nx     = xsize;
  i->ny     = ysize;
  i->nz     = zsize;
  i->file   = file;
  i->format = format;
  i->type   = type;
  return(0);
}

/*!
 * Tries to open an image file with name [filename] and with the fopen mode 
 * [mode] (e.g. "rb"), using the file format check functions on the list.
 * If [filename] is NULL or an empty string, then it assigns stdin to the file
 * pointer.  Returns NULL for error; it and all checking routines should call 
 * b3dError with their error strings.
 */
ImodImageFile *iiOpen(const char *filename, const char *mode)
{
  ImodImageFile *ofile;
  IIFileCheckFunction *checkFunc;
  int i, err = 0;

  if ((ofile = iiNew()) == NULL) 
    return NULL;
  if (filename && filename[0])
    ofile->fp = fopen(filename, mode);
  else
    ofile->fp = stdin;

  if (ofile->fp == NULL || initCheckList()) {
    b3dError(stderr, "ERROR: iiOpen - Opening file %s\n", filename);
    iiDelete(ofile);
    return(NULL);
  }
  ofile->filename = strdup(filename);
  ofile->fmode = strdup(mode);
    
  /* Try to open the file with each of the check functions in turn 
   * until one succeeds
   */
  ofile->format = IIFILE_UNKNOWN;
  for (i = 0; i < ilistSize(checkList); i++) {
    checkFunc = (IIFileCheckFunction *)ilistItem(checkList, i);

    /* If file was closed and couldn't reopen, bail out */
    if (ofile->fp == NULL) {
      b3dError(stderr, "ERROR: iiOpen - %s could not be reopened\n", filename);
      break;
    }

    if (!(err = (*checkFunc)(ofile))) {
      ofile->state = IISTATE_READY;
      return ofile;
    }
    
    if (err != IIERR_NOT_FORMAT)
      break;
  }
  
  if (err == IIERR_NOT_FORMAT)
    b3dError(stderr, "ERROR: iiOpen - %s has unknown format.\n", filename);
  iiDelete(ofile);
  return NULL;
}

/*!
 * Reopen a file that has already been opened and analyzed 
 */
int  iiReopen(ImodImageFile *inFile)
{
  IIFileCheckFunction *checkFunc;
  int i;

  if (!inFile)
    return -1;
  if (inFile->fp)
    return 1;
  if (!inFile->fmode)
    inFile->fmode = defMode;
  if (inFile->reopen) {
    if ((*inFile->reopen)(inFile))
      return 2;
    inFile->state = IISTATE_READY;
    return 0;
  }

  inFile->fp = fopen(inFile->filename, inFile->fmode);
  if (!inFile->fp)
    return 2;

  if (inFile->state != IISTATE_NOTINIT) {
    inFile->state = IISTATE_READY;
    return 0;
  }

  /* If the file is not initted yet, treat it as unknown and deal with from scratch */
  inFile->format = IIFILE_UNKNOWN;
  for (i = 0; i < ilistSize(checkList); i++) {
    checkFunc = (IIFileCheckFunction *)ilistItem(checkList, i);
    if (!(*checkFunc)(inFile)) {
      inFile->state = IISTATE_READY;
      return 0;
    }
  }
  return -1;
}

/*!
 * Sets the scaling min and max ({smin} and {smax} in the image file structure
 * [inFile] and computes the scaling {slope} and {offset} that will map {smin} to
 * and {smax} to [scaleMax] by scaling with value * slope + offset.
 * Uses the input values [inMin] and [inMax], or the file min and max if these
 * values are equal.  Returns 0.
 */
 /* 1/3/04: change from double to float for arguments */
int  iiSetMM(ImodImageFile *inFile, float inMin, float inMax, float scaleMax)
{
  float range;

  /* DNM: only modify the existing smin, smax if incoming data is useful, and
     set the min and the max to 0, 255 if they are still equal */

  if (inMin != inMax) {
    inFile->smin = inMin;
    inFile->smax = inMax;
  }

  if (inFile->smin == inFile->smax){
    inFile->smin = 0;
    inFile->smax = 255;
  }

  /* DNM 1/7/04: do not modify smin/smax if complex scaling, just get it
     right for the slope and offset 
     Also, use new routine */
  inMin = inFile->smin;
  inMax = inFile->smax;

  /* DNM 2/16/01: set scaling properly for complex mode, the same as for
     full-file reads with mrc_read_byte */
  if (inFile->format == IIFORMAT_COMPLEX)
    mrcComplexSminSmax(inMin, inMax, &inMin, &inMax);

  range = inMax - inMin;
  inFile->slope = scaleMax / range;

  inFile->offset = -inMin * inFile->slope;

  /* printf("iiSetMM %g %g -> %g %g\n",
     inMin, inMax, inFile->slope, inFile->offset); */

  return(0);
}

/*!
 * Closes an image file [inFile]
 */
void iiClose(ImodImageFile *inFile)
{
  if (inFile->close)
    (*inFile->close)(inFile);
  else if (inFile->fp != NULL) 
    fclose(inFile->fp);
  inFile->fp = NULL;
  if (inFile->state != IISTATE_NOTINIT)
    inFile->state = IISTATE_PARK;
}

/*!
 * Deletes an image file [inFile] after closing and calling any cleanup
 * functions 
 */
void iiDelete(ImodImageFile *inFile)
{
  if (!inFile) 
    return;
  iiClose(inFile);
  B3DFREE(inFile->filename);
  if (inFile->cleanUp)
    (*inFile->cleanUp)(inFile);
  B3DFREE(inFile->description);
  B3DFREE(inFile->colormap);
  if (inFile->fmode != defMode)
    B3DFREE(inFile->fmode);
  free(inFile);
}

/*!
 * Reads the section [inSection] from the file [inFile] as raw data into buffer
 * [buf].  Returns -1 for undefined reading function or failure to reopen file,
 * otherwise passes along return value of the reading function.
 */
int iiReadSection(ImodImageFile *inFile, char *buf, int inSection)
{
  return( readWriteSection(inFile, buf, inSection, inFile->readSection) );
}

/*!
 * Reads the section [inSection] from the file [inFile] as scaled byte data 
 * into buffer [buf].  Returns -1 for undefined reading function or failure 
 * to reopen file, otherwise passes along return value of the reading function.
 */
int iiReadSectionByte(ImodImageFile *inFile, char *buf, int inSection)
{
  return( readWriteSection(inFile, buf, inSection, inFile->readSectionByte) );
}

/*!
 * Reads the section [inSection] from the file [inFile] as scaled unsigned short data 
 * into buffer [buf].  Returns -1 for undefined reading function or failure 
 * to reopen file, otherwise passes along return value of the reading function.
 */
int iiReadSectionUShort(ImodImageFile *inFile, char *buf, int inSection)
{
  return( readWriteSection(inFile, buf, inSection, inFile->readSectionUShort) );
}

/*!
 * Write data in the buffer [buf] to section [inSection] of the file [inFile].
 * Returns -1 for undefined writing function or failure to reopen file; otherwise passes
 * along return value of the writing function.  Note that neither iimrc nor iitiff define
 * writing functions.
 */
int iiWriteSection(ImodImageFile *inFile, char *buf, int inSection)
{
  return( readWriteSection(inFile, buf, inSection, inFile->writeSection) );
}

/* The routine that does the work */
static int readWriteSection(ImodImageFile *inFile, char *buf, int inSection, 
                            iiSectionFunc func)
{
  if (!func) 
    return -1;
  if (!inFile->fp){
    if (iiReopen(inFile))
      return -1;
  }
  return( func(inFile, buf, inSection) );
}


/*!
 * Loads piece coordinates from an MRC file [inFile] of size [nx], [ny], [nz]
 * and places them in the LoadInfo structure [li].  If no coordinates are found
 * and [useMdoc] is non-zero, it then tries to load coordinates from a metadata
 * file named as the image filename plus .mdoc.  Returns 0 regardless of
 * whether there are piece coordinates or errors.
 */
int iiLoadPCoord(ImodImageFile *inFile, int useMdoc, IloadInfo *li, int nx,
                 int ny, int nz)
{
  int err;
  if (iiMRCCheck(inFile))
    return (0);
  iiMRCLoadPCoord(inFile, li, nx, ny, nz);
  if (!li->plist && useMdoc)
    err = iiPlistFromMetadata(inFile->filename, 1, li, nx, ny, nz);
  return 0;
}
