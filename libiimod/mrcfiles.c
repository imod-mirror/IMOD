/*
 *  mrcfiles.c -- Reading and writing mrc files; high level io functions.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 *
 *  $Id$
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <time.h>
#include <math.h>
#include "iimage.h"
#include "b3dutil.h"

/* These defines are OK since all I/O in file is to MRC files */
#if defined(WIN32_BIGFILE) || defined(MAC103_BIGFILE)
#define fseek b3dFseek 
#define fread b3dFread 
#define fwrite b3dFwrite 
#define rewind b3dRewind
#endif

/* 1/16/05: Back off limits for the purposes of this file, forget FLT_MIN */
#ifndef FLT_MAX
#define FLT_MAX 1.E+37F
#endif

/*
 * Header functions: DOC_SECTION HEADER
 */

/*!
 * Reads an MRC header into [hdata] from the file with pointer [fin].  
 * Determines whether byte-swapping is necessary by requiring that {nx}, {ny},
 * and {nz} are positive, that one of them is < 65536, and that {mapc}, {mapr},
 * and {maps} be between 0 and 4.  Leaves the file pointer [fin] in the {fp} 
 * member of [hdata].  Returns -1 for I/O error, or 1 if these requirements are
 * not met or if the mode or number of labels are inappropriate.  However, if [fin] is
 * a file pointer in the list of opened ImodImageFiles, it calls 
 * @@iimage.html#iiFillMrcHeader@ instead since the header has already been read.
 */
int mrc_head_read(FILE *fin, MrcHeader *hdata)
{
  int i;
  int retval = 0;
  int datasize;
  ImodImageFile *iiFile;

  if (!fin)
    return(-1);

  iiFile = iiLookupFileFromFP(fin);
  if (iiFile) {
    return iiFillMrcHeader(iiFile, hdata);
  }

  b3dRewind(fin);
     
  if (fread(hdata, 4, 56, fin) != 56){
    b3dError(stderr, "ERROR: mrc_head_read - reading header data.\n");
    return(-1);
  }
  hdata->swapped = 0;
  hdata->iiuFlags = 0;

  /* Test for byte-swapped data with image size and the map numbers and 
     mark data as swapped if it fails */
  if (mrc_test_size(hdata))
    hdata->swapped = 1;

  /* DNM 7/30/02: test for old style header and rearrange origin info */
  if (hdata->cmap[0] != 'M' || hdata->cmap[1] != 'A' || 
      hdata->cmap[2] != 'P') {
    memcpy(&hdata->zorg, &hdata->cmap[0], 4);
    memcpy(&hdata->xorg, &hdata->stamp[0], 4);
    memcpy(&hdata->yorg, &hdata->rms, 4);
    hdata->rms = -1.;
    if (hdata->swapped)
      mrc_swap_floats(&hdata->rms, 1);
    mrc_set_cmap_stamp(hdata);
    hdata->iiuFlags |= IIUNIT_OLD_STYLE;
  }

  if (hdata->swapped) {
    mrc_swap_header(hdata);

    /* Test that this swapping makes values acceptable */
    /* Let calling program issue error message */
    if (mrc_test_size(hdata))
      return(1);
  }
          
  /* Set other run-time data and adjust min/max/mean up for signed bytes */
  hdata->headerSize = 1024;
  hdata->sectionSkip = 0;
  hdata->yInverted = 0;
  hdata->headerSize += hdata->next;
  hdata->bytesSigned = readBytesSigned(hdata->imodStamp, hdata->imodFlags, hdata->mode,
                                       hdata->amin, hdata->amax);
  if (hdata->bytesSigned) {
    hdata->amin += 128.;
    hdata->amax += 128.;
    hdata->amean += 128.;
  }

  /* If not an IMOD file, clear out the flags, otherwise retain them */
  if (hdata->imodStamp != IMOD_MRC_STAMP)
    hdata->imodFlags = 0;

  /* Invert origin coming in if this flag is set (added for 4.7 release) */
  if (hdata->imodStamp == IMOD_MRC_STAMP && (hdata->imodFlags & MRC_FLAGS_INV_ORIGIN)) {
    hdata->xorg *= -1;
    hdata->yorg *= -1;
    hdata->zorg *= -1;
  }

  for ( i = 0; i < MRC_NLABELS; i ++){
    if (fread(hdata->labels[i], MRC_LABEL_SIZE, 1, fin) == 0) {  
      b3dError(stderr, "ERROR: mrc_head_read - reading label %d.\n", i);
      hdata->labels[i][MRC_LABEL_SIZE] = 0;
      return(-1);
    }
    hdata->labels[i][MRC_LABEL_SIZE] = 0;
    if (i < hdata->nlabl)
      fixTitlePadding(hdata->labels[i]);
  }

  if ((hdata->mode > 31) || (hdata->mode < 0)) {
    b3dError(stderr, "ERROR: mrc_head_read - bad file mode %d.\n", hdata->mode);
    return(1);
  }
  if (hdata->nlabl > MRC_NLABELS) {
    b3dError(stderr, "ERROR: mrc_head_read - impossible number of "
             "labels, %d.\n", hdata->nlabl);
    return(1);
  }

  /* 12/31/13: If the map indexes are wrong just fix them */
  if ((hdata->mapc + 2) / 3 != 1 || (hdata->mapr + 2) / 3 != 1 ||
      (hdata->maps + 2) / 3 != 1 || hdata->mapc == hdata->mapr || 
      hdata->mapr == hdata->maps || hdata->mapc == hdata->maps) {
    hdata->mapc = 1;
    hdata->mapr = 1;
    hdata->maps = 1;
    hdata->iiuFlags |= IIUNIT_BAD_MAPCRS;
  }
    
  /* 12/9/12: To match what is done in irdhdr when mx or xlen is zero; 
     also if Z pixel size is 0, set Z cell to match X pixel size */
  if (!hdata->mx || hdata->xlen < 1.e-5) {
    hdata->mx = hdata->my = hdata->mz = 1;
    hdata->xlen = hdata->ylen = hdata->zlen = 1.;
  }
  if (hdata->zlen < 1.e-5)
    hdata->zlen = hdata->mz * hdata->xlen / hdata->mx;

  /* DNM 6/10/04: Workaround to FEI goof in which nints was set to # of bytes, 4 * nreal*/
  if (hdata->nint == 128 && hdata->nreal == 32 && 
      (hdata->next == 131072 || strstr(hdata->labels[0], "Fei ") ==  hdata->labels[0])) {
    hdata->nint = 0;
    hdata->iiuFlags |= IIUNIT_NINT_BUG;
  }

  /* DNM 7/2/02: This calculation is won't work for big files and is
     a bad idea anyway, so comment out the test below */
  datasize = hdata->nx * hdata->ny * hdata->nz;
  switch(hdata->mode){
  case MRC_MODE_BYTE:
    break;
  case MRC_MODE_SHORT:
  case MRC_MODE_USHORT:
    datasize *= 2;
    break;
  case MRC_MODE_FLOAT:
  case MRC_MODE_COMPLEX_SHORT:
    datasize *= 4;
    break;
  case MRC_MODE_COMPLEX_FLOAT:
    datasize *= 8;
    break;
  case MRC_MODE_RGB:
    datasize *= 3;
    break;
  default:
    b3dError(stderr, "ERROR: mrc_head_read - bad file mode %d.\n",
             hdata->mode);
    return(1);
  }

  /* fseek(fin, 0, 2);
     filesize = ftell(fin); */
  b3dRewind(fin);

  /* if ((filesize - datasize) < 0)
     return(0);
     if ((filesize - datasize) > 512)
     return(0); */

  hdata->fp = fin;

  return(retval);
}

/*!
 * Tests whether the string is null-terminated before its end and pads it with space to 
 * its end
 */
void fixTitlePadding(char *label)
{
  int len;
  label[MRC_LABEL_SIZE] = 0x00;
  len = strlen(label);
  if (len < MRC_LABEL_SIZE)
    memset(&label[len], ' ', MRC_LABEL_SIZE - len);
}

/*!
 * Tests the image size and map entries in MRC header [hdata] to see if they
 * are within allowed ranges: {nx}, {ny}, and {nz} all positive, and at least 
 * one of them less than 65536; and map values between 0 and 3.  Returns 0 if
 * that is the case, 1 if not.
 */
int mrc_test_size(MrcHeader *hdata)
{
  if (hdata->nx <= 0 || hdata->ny <= 0 || hdata->nz <= 0 || 
      (hdata->nx > 65535 && hdata->ny > 65535 && hdata->nz > 65535) ||
      hdata->mapc < 0 || hdata->mapc > 4 ||
      hdata->mapr < 0 || hdata->mapr > 4 ||
      hdata->maps < 0 || hdata->maps > 4)
    return 1;
  return 0;
}

/*!
 * Write the MRC header in [hdata] to the file with pointer [fout].  First, it looks up
 * [fout] in the list of opened ImodImageFiles and if it finds that it belongs to a 
 * non-MRC file, it calls @@iimage.html#iiSyncFromMrcHeader@ and returns 0.  Otherwise, 
 * it returns 1 for error.
 */
int mrc_head_write(FILE *fout, MrcHeader *hdata)
{
  int i;
  ImodImageFile *iiFile;
  MrcHeader hcopy;

  if (!fout)
    return(1);
  iiFile = iiLookupFileFromFP(fout);
  if (iiFile && iiFile->file != IIFILE_MRC) {
    if (iiFile->file != IIFILE_RAW)
      iiSyncFromMrcHeader(iiFile, hdata);
    return iiWriteHeader(iiFile);
  }

  /* Set the IMOD stamp and flags and clear out old creator field when writing */
  hdata->imodStamp = IMOD_MRC_STAMP;
  setOrClearFlags((b3dUInt32 *)(&hdata->imodFlags), MRC_FLAGS_SBYTES, hdata->bytesSigned);
  hdata->creatid = 0;
  hdata->blank[0] = 0;
  hdata->blank[1] = 0;

  /* DNM 7/20/11: copy the header regardless, clamp byte min/max to limits, shift 
     min/max/mean for unsigned output, and swap if needed */
  hcopy = *hdata;
  if (!hdata->mode) {
    hcopy.amin = B3DMAX(0., hcopy.amin);
    hcopy.amax = B3DMIN(255., hcopy.amax);
    if (hdata->bytesSigned) {
      hcopy.amin -= 128.;
      hcopy.amax -= 128.;
      hcopy.amean -= 128.;
    }
  }
  if (hdata->swapped)
    mrc_swap_header(&hcopy);

  rewind(fout);
     
  if (fwrite(&hcopy, 56, 4, fout) != 4) {
    b3dError(stderr, "ERROR: mrc_head_write - writing header to file\n");
    return 1;
  }
     
  for( i = 0; i < MRC_NLABELS; i++) {
    if (fwrite(hdata->labels[i], MRC_LABEL_SIZE, 1,fout) != 1) {
      b3dError(stderr, "ERROR: mrc_head_write - writing header to file\n");
      return 1;
    }
  }
     
  return(0);
}


/*!
 * Adds [label] to the header [hdata] or replaces the last one if there are
 * already 10 labels.  The label will be truncated at 55 characters or padded
 * with spaces to that length, and a standard date-time stamp will be added.
 * Returns 0.
 */
int mrc_head_label(MrcHeader *hdata, const char *label)
{
  struct tm *tmp;
  char *outlab;
  int i, endoflabel = FALSE;
  time_t time_tval;
  int datelen = 25;

  if (hdata->nlabl >= MRC_NLABELS)
    hdata->nlabl--;
  outlab = hdata->labels[hdata->nlabl];
  for(i = 0; i < MRC_LABEL_SIZE - datelen; i++){
    if (label[i] && !endoflabel)
        outlab[i] = label[i];
    else
      endoflabel = TRUE;
    if (endoflabel)
      outlab[i] = ' ';
  }

  time_tval = time(NULL);
  tmp = localtime(&time_tval);
  strftime(&outlab[i], datelen, " %d-%b-%y  %H:%M:%S    ", tmp);
  
  hdata->nlabl++;
  return(0);
}

/*!
 * Copies all labels from header [hin] to header [hout].  Returns 0.
 */
int mrc_head_label_cp(MrcHeader *hin, MrcHeader *hout)
{
  int i, j;
     
  for (i = 0; i < hin->nlabl; i++){
    for(j = 0; j <= MRC_LABEL_SIZE; j++){
      hout->labels[i][j] = hin->labels[i][j];
    }
  }
  hout->nlabl = hin->nlabl;
  return(0);
}

/*!
 * Copy extra header data from MRC file whose header is in [hin] to MRC file whose header 
 * is in [hout], swapping data if necessary after determining whether it consists of
 * short integers or floats and ints.  Returns 1 if [hin] or [hout] is NULL or the output
 * file is swapped, 2 for seek errors, 3 for memory allocation error, 4 for read or write 
 * errors.
 */
int mrcCopyExtraHeader(MrcHeader *hin, MrcHeader *hout)
{
  int i, ind, nsecs;
  unsigned char *extdata;

  if (!hin || !hout || hout->swapped)
    return 1;
  if (!hin->next)
    return 0;
  if (fseek(hin->fp, 1024, SEEK_SET) || fseek(hout->fp, 1024, SEEK_SET))
    return 2;
  extdata = B3DMALLOC(unsigned char, hin->next);
  if (!extdata)
    return 3;
  if (fread(extdata, 1, hin->next, hin->fp) != hin->next) {
    free(extdata);
    return 4;
  }
  if (hin->swapped) {
    if (extraIsNbytesAndFlags(hin->nint, hin->nreal)) {
      mrc_swap_shorts((b3dInt16 *)extdata, hin->next / 2);
    } else {
      nsecs = hin->next / (4 * (hin->nint + hin->nreal));
      ind = 0;
      for (i = 0; i < nsecs; i++) {
        if (hin->nint)
          mrc_swap_longs((b3dInt32 *)extdata + ind, hin->nint);
        ind += 4 * hin->nint;
        if (hin->nreal)
          mrc_swap_floats((b3dFloat *)extdata + ind, hin->nreal);
        ind += 4 * hin->nreal;
      }
    }
  }
  if (fwrite(extdata, 1, hin->next, hout->fp) != hin->next) {
    free(extdata);
    return 4;
  }

  hout->next = hin->next;
  hout->headerSize = 1024 + hin->next;
  hout->nint = hin->nint;
  hout->nreal = hin->nreal;
  free(extdata);
  return 0;
}

/*!
 * Fills in the header structure [hdata] to default settings for the given
 * file size [x], [y], [z] and the given [mode].  Returns 0.
*/
int mrc_head_new(MrcHeader *hdata,
                 int x, int y, int z, int mode)
{
  hdata->nx = x;
  hdata->ny = y;
  hdata->nz = z;
  hdata->mode = mode;

  hdata->nxstart = 0;
  hdata->nystart = 0;
  hdata->nzstart = 0;
  hdata->mx = hdata->nx;
  hdata->my = hdata->ny;
  hdata->mz = hdata->nz;
  hdata->xlen = hdata->nx;
  hdata->ylen = hdata->ny;
  hdata->zlen = hdata->nz;
  hdata->alpha = 90;
  hdata->beta  = 90;
  hdata->gamma = 90;
  hdata->mapc  = 1;
  hdata->mapr  = 2;
  hdata->maps  = 3;

  /* 1/16/05: use FLT_MAX instead of INT_MIN/INT_MAX for non-sgi */
  hdata->amin  = FLT_MAX;
  hdata->amax  = -FLT_MAX;
  hdata->amean = 0;
  hdata->ispg  = 0;

  hdata->next    = 0;
  hdata->creatid = 0;   /* 7/13/11: changed to 0  for compatibility with CCP4 */
  hdata->nversion = 0;
  hdata->nint    = 0;
  hdata->nreal   = 0;
  hdata->sub     = 0;   /* 7/13/11: changed these two from 1 to 0 */
  hdata->zfac    = 0;
  hdata->min2    = 0.0f;
  hdata->max2    = 0.0f;
  hdata->min3    = 0.0f;
  hdata->max3    = 0.0f;
  hdata->imodStamp = IMOD_MRC_STAMP;
  hdata->imodFlags = MRC_FLAGS_BAD_RMS_NEG;
  hdata->iiuFlags = 0;

  hdata->idtype = 0;
  hdata->lens = 0;
  hdata->nd1 = 0;
  hdata->nd2 = 0;
  hdata->vd1 = 0;
  hdata->vd2 = 0;
     
  for(x = 0; x < 10; x++)  /* 7/13/11: This should be cleared out */
    hdata->blank[x] = 0;
  for(x = 0; x < 16; x++)
    hdata->blank2[x] = 0;
  for(x = 0; x < 6; x++)
    hdata->tiltangles[x] = 0.0f;
  hdata->rms = -1.;
  /* 7/20/11: get rid of old header stuff */
  hdata->zorg = 0.0f;
  hdata->xorg = 0.0f;
  hdata->yorg = 0.0f;
  hdata->nlabl = 0;

  /* We can't clear these values because it crashes clip 
   * and imod image load.
   */
  /*     hdata->fp = 0;    */
  /*     hdata->li = NULL; */

  mrcInitOutputHeader(hdata);
  hdata->pathname = NULL;
  hdata->filedesc = NULL;
  hdata->userData = NULL;
     
  return(0);

}

/*!
 * Initialize the header in [hdata] for a new output file by eliminating extra header 
 * data, setting the MAP stamp, setting the swapped member to 0, and setting the 
 * bytesSigned member appropriately.
 */
void mrcInitOutputHeader(MrcHeader *hdata)
{
  hdata->swapped = 0;
  mrc_set_cmap_stamp(hdata);
  hdata->headerSize = 1024;
  hdata->sectionSkip = 0;
  hdata->yInverted = 0;
  hdata->imodFlags = MRC_FLAGS_BAD_RMS_NEG;
  hdata->rms = -1;
  hdata->bytesSigned = writeBytesSigned();
  hdata->next = 0;
  hdata->nint = 0;
  hdata->nreal = 0;
  hdata->nversion = 0;
}

/* DNM 12/25/00: Scale is defined as ratio of sample to cell, so change the
   nx, ny, nz below to mx, my, mz.  But also return 0 instead of 1 if cell
   size is zero; so that the mrc_set_scale will fix both cell and sample */
/* DNM 9/13/02: Invert these to correspond to all other usage */
/*!
 * Computes the pixel size or scale values in the MRC header [h] and returns
 * them in [xs], [ys], and [zs].
 */
void mrc_get_scale(MrcHeader *h, float *xs, float *ys, float *zs)
{
  *xs = *ys = *zs = 0.0f;
  if (h->xlen)
    *xs = h->xlen/(float)h->mx;
  if (h->ylen)
    *ys = h->ylen/(float)h->my;
  if (h->zlen)
    *zs = h->zlen/(float)h->mz;
}

/* DNM 12/25/00: change this 1) if 0 scale comes in, set both cell and sample
   sizes to image sizes; 2) compute xlen as mx/x scale, not nx/ x scale, etc */
/*!
 * Sets values in the MRC header [h] so that the pixel size or scale is [x], 
 * [y], and [z].  If [x] is nonzero, it sets the {xlen} element to [h] to be 
 * [x] times the {mx} element; otherwise it sets {xlen} and {mx} equal
 * to {nx}.  [y] and [z] are treated similarly.
 */
void mrc_set_scale(MrcHeader *h,
                   double x, double y, double z)
{
  if (!x) {
    h->xlen = h->nx;
    h->mx = h->nx;
  } else
    h->xlen = h->mx * x;

  if (!y) {
    h->ylen = h->ny;
    h->my = h->ny;
  } else
    h->ylen = h->my * y;

  if (!z) {
    h->zlen = h->nz;
    h->mz = h->nz;
  } else
    h->zlen = h->mz * z;

}

/*!
 * Copies the scale values, current tilt angles, and origin values from the
 * MRC header [hin] to header [hout].
 */
void mrc_coord_cp(MrcHeader *hout, MrcHeader *hin)
{
  float xs, ys, zs;

  mrc_get_scale(hin, &xs, &ys, &zs);
  mrc_set_scale(hout, xs, ys, zs);
  hout->tiltangles[3] = hin->tiltangles[3];
  hout->tiltangles[4] = hin->tiltangles[4];
  hout->tiltangles[5] = hin->tiltangles[5];
  hout->xorg = hin->xorg;
  hout->yorg = hin->yorg;
  hout->zorg = hin->zorg;
}


/*! 
 * Determines the min, max and mean values of the byte data in [idata] and 
 * places them into header [hdata].  [idata] must be an array of pointers to 
 * {hdata->nz} planes of data.  Returns -1 for error.
 */
int mrc_byte_mmm( MrcHeader *hdata, unsigned char **idata)
{

  int i, j, k;

  double min, max, mean;

  mean = 0;
  min = idata[0][0];
  max = idata[0][0];

  if (hdata == NULL)
    return (-1);

  if (idata == NULL)
    return(-1);

  for (k = 0; k < hdata->nz; k++)
    for (j = 0; j < hdata->ny; j++)
      for(i = 0; i < hdata->nx; i++){
        if (idata[k][i + (j * hdata->nx)] > max)
          max = idata[k][i + (j * hdata->nx)];

        if (idata[k][i + (j * hdata->nx)] < min)
          min = idata[k][i + (j * hdata->nx)];

        mean += idata[k][i + (j * hdata->nx)];


      }

  mean = mean / (double)(hdata->nx * hdata->ny * hdata->nz);

  hdata->amin = min;
  hdata->amean = mean;
  hdata->amax = max;
  return(0);
}

/*!
 * Swaps each section of the header [hdata] as appropriate for its data type.
 */
void mrc_swap_header(MrcHeader *hdata)
{
  mrc_swap_longs(&hdata->nx, 10);
  mrc_swap_floats(&hdata->xlen, 6);
  mrc_swap_longs(&hdata->mapc, 3);
  mrc_swap_floats(&hdata->amin, 3);
  /* 1/12/12: removed nsymbt, made ispg 4 bytes */
  mrc_swap_longs(&hdata->ispg, 1);
  mrc_swap_longs(&hdata->next, 1);
  mrc_swap_shorts(&hdata->creatid, 1);
  mrc_swap_longs(&hdata->nversion, 1);
  mrc_swap_shorts(&hdata->nint, 4);
  mrc_swap_floats(&hdata->min2, 4);
  mrc_swap_longs(&hdata->imodStamp, 2);
  mrc_swap_shorts(&hdata->idtype, 6);
  mrc_swap_floats(&hdata->tiltangles[0], 6);
#ifdef OLD_STYLE_HEADER
  mrc_swap_shorts(&hdata->nwave, 6);
  mrc_swap_floats(&hdata->zorg, 3);
#else
  mrc_swap_floats(&hdata->xorg, 3);
  mrc_swap_floats(&hdata->rms, 1);
#endif
  mrc_swap_longs(&hdata->nlabl, 1);
}

/* DNM 7/30/02: set cmap and stamp correctly for a new header or
   for converting an old style header on reading in */
void mrc_set_cmap_stamp(MrcHeader *hdata)
{
#ifdef B3D_LITTLE_ENDIAN
  int littleEnd = hdata->swapped ? 0 : 1;
#else
  int littleEnd = hdata->swapped ? 1 : 0;
#endif
  hdata->cmap[0] = 'M';
  hdata->cmap[1] = 'A';
  hdata->cmap[2] = 'P';
  hdata->cmap[3] = ' ';
  /* The CCP4-style stamp has 4 4-bit codes for double, float, int, and char 
     formats, in order of high then low bits within each byte.  1 and 4 refer 
     to IEEE big-endian and little-endian, respectively */
  if (littleEnd) {
    hdata->stamp[0] = 16 * 4 + 4;
    hdata->stamp[1] = 16 * 4 + 1;
  } else {
    hdata->stamp[0] = 16 * 1 + 1;
    hdata->stamp[1] = 16 * 1 + 1;
  }
  hdata->stamp[2] = 0;
  hdata->stamp[3] = 0;
}

/*
 * Reading functions: DOC_SECTION READ_WRITE
 */

/*!
 * Allocates and returns one plane of data at the coordinate given by [slice]
 * along the axis given by [axis], which must be one of x, X, y, Y, z, or Z.  
 * Reads from the file with pointer [fin] according to the header in [hdata] 
 * and swaps bytes if necessary.  Calls @mrc_read_slice.  Returns NULL for errors.
 */
void *mrc_mread_slice(FILE *fin, MrcHeader *hdata, int slice, char axis)
{
  unsigned char *buf = NULL;
  int dsize, csize, bsize;

  switch (axis)
    {
    case 'x':
    case 'X':
      bsize = hdata->ny * hdata->nz;
      break;
               
    case 'y':
    case 'Y':
      bsize = hdata->nx * hdata->nz;
      break;
               
    case 'z':
    case 'Z':
      bsize = hdata->nx * hdata->ny;
      break;

    default:
      b3dError(stderr, "ERROR: mrc_mread_slice - axis error.\n");
      return(NULL);
    }

  if (mrc_getdcsize(hdata->mode, &dsize, &csize)){
    b3dError(stderr, "ERROR: mrc_mread_slice - unknown mode.\n");
    return(NULL);
  }
  buf = (unsigned char *)malloc(dsize * csize * bsize);
  if (!buf){
    b3dError(stderr, "ERROR: mrc_mread_slice - couldn't get memory.\n");
    return(NULL);
  }

  if (!mrc_read_slice(buf, fin, hdata, slice, axis))
    return((void *)buf);

  free(buf);
  return(NULL);
}

/*!
 * Reads one plane of data into the buffer [buf] at the coordinate given by 
 * [slice] along the axis given by [axis], which must be one of x, X, y, Y, z,
 * or Z.  Reads from the file with pointer [fin] according to the header in 
 * [hdata] and swaps bytes if necessary.  Should work with planes > 4 GB on
 * 64-bit systems.  If the axis is Y or Z, it calls @@mrcfiles.html#mrcReadSection@ and
 * returns its return value; thus it can read from all file types for Z planes and
 * from file types other than TIFF for Y planes.  Can read X planes only from MRC-like
 * files; in that case it returns -1 for errors.
 */
int mrc_read_slice(void *buf, FILE *fin, MrcHeader *hdata, int slice, char axis)
{
  unsigned char *data = NULL;
  int dsize, csize, sxsize, sysize;
  b3dInt16 *sbuf = (b3dInt16 *)buf;
  b3dFloat *fbuf = (b3dFloat *)buf;
  char *sbbuf = (char *)buf;
  int dcsize;
  int j,k;
  IloadInfo li;
  ImodImageFile *iiFile;
  FILE *fpSave =  hdata->fp;

  mrc_init_li(&li, NULL);
  mrc_init_li(&li, hdata);
  if (axis == 'z' || axis == 'Z' || axis == 'y' || axis == 'Y') {
    if (axis == 'y' || axis == 'Y')
      li.axis = 2;
    hdata->fp = fin;
    j = mrcReadSection(hdata, &li, buf, slice);
    hdata->fp = fpSave;
    return j;
  }

  iiFile = iiLookupFileFromFP(fin);
  if (iiFile && iiFile->file != IIFILE_MRC && iiFile->file != IIFILE_RAW) {
    b3dError(stderr, "ERROR: mrc_read_slice - Cannot read X slice from non-MRC-like "
             "file\n");
    return(-1);
  }

  rewind(fin);
  fseek(fin, hdata->headerSize, SEEK_SET);
  data = (unsigned char *)buf;

  if (mrc_getdcsize(hdata->mode, &dsize, &csize)){
    b3dError(stderr, "ERROR: mrc_read_slice - unknown mode.\n");
    return(-1);
  }
  dcsize = dsize * csize;

  /* slowest loading  use z or y if possible. */
  if (axis == 'x' || axis == 'X') {
    sxsize = hdata->ny;
    sysize = hdata->nz;
    if (slice >= hdata->nx)
      return(-1);
    fseek( fin, slice * dcsize, SEEK_CUR);
    for(k = 0; k < hdata->nz; k++){
      for (j = 0; j < hdata->ny; j++){
        if (fread(data, dcsize, 1, fin) != 1){
          b3dError(stderr, "ERROR: mrc_read_slice x - fread error.\n");
          return(-1);
        }
        data += dcsize;
        fseek(fin, dcsize * (hdata->nx - 1), SEEK_CUR);
      }
      if (hdata->sectionSkip)
        fseek(fin, hdata->sectionSkip, SEEK_CUR);
    }
  } else {
    b3dError(stderr, "ERROR: mrc_read_slice - axis error.\n");
    return(-1);
  }

  /* swap bytes if necessary */
  if (hdata->swapped)
    switch (hdata->mode){
    case MRC_MODE_SHORT:
    case MRC_MODE_USHORT:
    case MRC_MODE_COMPLEX_SHORT:
      for (j = 0; j < sysize; j++) {
        mrc_swap_shorts(sbuf, sxsize * csize);
        sbuf += sxsize * csize;
      }
      break;

    case MRC_MODE_FLOAT:
    case MRC_MODE_COMPLEX_FLOAT:
      for (j = 0; j < sysize; j++) {
        mrc_swap_floats(fbuf, sxsize * csize);
        fbuf += sxsize * csize;
      }
      break;

    default:
      break;
    }

  /* shift signed bytes up if necessary */
  if (!hdata->mode && hdata->bytesSigned) {
    data = (unsigned char *)buf;
    for (j = 0; j < sysize; j++)
      for (k = 0; k < sxsize; k++)
        *data++ = (unsigned char)(*sbbuf++ + 128);
  }

  fflush(fin);
  return(0);
}

/*!
 * Reads a whole Z slice at Z value [slice] from the MRC file whose header is
 * is in [hdata], and returns it into [buf].  Works only for real data (byte,
 * integer, float).  Simply calls @mrcReadZFloat .  Returns 1 for an illegal 
 * request, 2 for a memory error, or 3 for an error reading the file.
 */
int mrcReadFloatSlice(b3dFloat *buf, MrcHeader *hdata, int slice)
{
  IloadInfo li;
  mrc_init_li(&li, NULL);
  li.xmin = 0;
  li.xmax = hdata->nx - 1;
  li.ymin = 0;
  li.ymax = hdata->ny - 1;
  return (mrcReadZFloat(hdata, &li, buf, slice));
}


/*!
 * Reads a file into an array of unsigned bytes and returns an array of 
 * pointers to the planes of data.  [fin] is the pointer to the
 * file, [hdata] is the MRC header structure, [li] is an @@IloadInfo structure@
 * specifying the limits and scaling of the load, and [func] is a function to
 * receive a status string after each slice.  If [li] is NULL, sensible 
 * defaults are used.  Scaling is set from the {smin}, {smax}, {black}, 
 * {white} and {ramptype} members of [li] using @mrcContrastScaling , where 
 * {ramptype} can be MRC_RAMP_LIN, MRC_RAMP_LOG, or MRC_RAMP_EXP, but the
 * latter two should be used only for integer and float input data.
 * Data memory is allocated with
 * @mrcGetDataMemory and should be freed with @mrcFreeDataMemory .  Should work
 * with planes > 4 GB on 64-bit systems.  Will work for non-MRC-like files since it
 * calls @@mrcfiles.html#mrcReadZByte@.  Returns NULL for error.  
 */
unsigned char **mrc_read_byte(FILE *fin, 
                              MrcHeader *hdata, 
                              IloadInfo *li,
                              void (*func)(const char *))
{
  int k;
  IloadInfo liLocal;
  size_t xysize;               /* Size of each image.       */
  int xsize, ysize, zsize;  /* Size needed to be loaded. */
  char statstr[128];            /* message sent to callback function. */
  unsigned char **idata;        /* image data to return. */
  FILE *fpSave;                 /* Save and restore fp in header in case it differs */

  /* check input */
  if (!fin)
    return(NULL);
  if (!hdata)
    return(NULL);
  
  if (!li) {
    li = &liLocal;
    mrc_init_li(li, NULL);
    mrc_init_li(li, hdata);
  }

  fpSave = hdata->fp;
  hdata->fp = fin;
  xsize = li->xmax - li->xmin + 1;
  ysize = li->ymax - li->ymin + 1;
  zsize = li->zmax - li->zmin + 1;
  xysize = (size_t)xsize * (size_t)ysize;

  /*************************************/
  /* Calculate color map ramp scaling. */
     
  mrcContrastScaling(hdata, li->smin, li->smax, li->black, li->white, li->ramp,
                     &li->slope, &li->offset);

  /********************/
  /* Print some info. */
  if (func != ( void (*)(const char *) ) NULL){
    if (zsize > 1)
      sprintf(statstr, "Image size %d x %d, %d sections.\n", 
              xsize, ysize, zsize);
    else{
      sprintf(statstr,"Image size %d x %d.\n",xsize, ysize);
    }
    (*func)(statstr);
  }     

  /* Get the data memory */
  idata = mrcGetDataMemory(li, xysize, zsize, 1);
  if (!idata) {
    hdata->fp = fpSave;
    return NULL;
  }
   
  k = 0;
  if (func != ( void (*)(const char *) ) NULL){
    sprintf(statstr, "\nReading Image # %3.3d", k + 1); 
    (*func)(statstr);
  }

  /* Loop on sections */
  for (k = 0; k < zsize; k++){
    if (func != ( void (*)(const char *) ) NULL){
      sprintf(statstr, "\rReading Image # %3.3d", k + 1); 
      (*func)(statstr);
    }

    if (mrcReadZByte(hdata, li, idata[k], k + li->zmin)) {
      mrcFreeDataMemory(idata, li->contig, zsize);
      hdata->fp = fpSave;
      return NULL;
    }
  }
     
  sprintf(statstr, "\n");
  if (func != ( void (*)(const char *) ) NULL)
    (*func)(statstr);

  hdata->fp = fpSave;
  return(idata);
}  

/*!
 * Computes scaling of data to bytes with potentially two levels of scaling.
 * The first level of scaling maps [smin] and [smax] to 0 to 255; if these
 * two values are equal, then the file min and max in [hdata] are used instead.
 * The second level of scaling maps [black] and [white] in these scaled values
 * to 0 to 255 to mimic the effect of black and white sliders in 3dmod.
 * [ramptype] can be MRC_RAMP_LIN, MRC_RAMP_LOG, or MRC_RAMP_EXP.  The factors
 * for scaling by pixel * slope + offset are returned in [slope] and [offset].
 * 
 */
void mrcContrastScaling(MrcHeader *hdata, float smin, float smax, int black,
                        int white, int ramptype, float *slope, float *offset)
{
  float min, max, rscale;
  int range;

  /* DNM 2/16/01: eliminate special treatment of byte mode in which black 
     and white were set to min and max while min and max were set to 0, 255,
     in order to allow double scaling in mrcbyte */

  /* set max and min. */
  max = hdata->amax;
  min = hdata->amin;

  if (smin != smax){
    max = smax;
    min = smin;
  }

  if (ramptype == MRC_RAMP_LOG){
    min = (float)log((double)min);
    max = (float)log((double)max);
  }
  if (ramptype == MRC_RAMP_EXP){
    min = (float)exp((double)min);
    max = (float)exp((double)max);
  }
  if (hdata->mode == MRC_MODE_COMPLEX_FLOAT || 
      hdata->mode == MRC_MODE_COMPLEX_SHORT)
    mrcComplexSminSmax(min, max, &min, &max);

  /* range in colormap */
  range = white - black + 1;
  if (!range) range = 1;

  /* range scale */
  rscale = 256.0 / (float)range;
     
  /* calculate slope */
  if ((max - min) != 0)
    *slope = 255.0 / (max - min);
  else
    *slope = 1.0;
     
  *slope *= rscale;
     
  /* calculate offset */
  *offset = -(( ((float)black / 255.0) * (max - min)) + min) * *slope;
}


/*
 * Write image data functions
 */

/*!
 * Writes byte, short, or float image data to the file with pointer [fout] 
 * according to the dimensions and mode in header [hdata], and starting from the 
 * beginning of the file.  [data] must be an
 * array of pointers to {hdata->nz} planes of data.  Simply calls @mrc_write_slice and
 * returns its return value.  An earlier version was used by mrcbyte
 * until 5/30/08; this is currently unused.
 */
int mrc_write_idata(FILE *fout, MrcHeader *hdata, void *data[])
{
  int k, j = 0;

  for (k = 0; k < hdata->nz; k++) {
    j = mrc_write_slice(data[k], fout, hdata, k, 'Z');
    if (j) 
      return(j);
  }
  return(0);
}

/*!
 * Writes one plane of data from the buffer [buf] at the coordinate given by 
 * [slice] along the axis given by [axis], which must be one of x, X, y, Y, z,
 * or Z.  Writes to the file with pointer [fout] according to the header in 
 * [hdata] and swaps bytes if necessary.  Should handle planes > 4 GB on 64-bit
 * systems.  When writing to a Z slice, it calls @@mrcfiles.html#mrcWriteZ@ and
 * returns its return value.  Otherwise, it will only work with MRC files and
 * returns -1 for errors.
 */
int mrc_write_slice(void *buf, FILE *fout, MrcHeader *hdata, int slice, char axis)
{
  int dsize, csize, retval = 0;
  size_t slicesize, sxsize, sysize;
  int j,k, dcsize, nx, ny;
  unsigned char *data = NULL;
  unsigned char *dataOrig = NULL;
  b3dInt16 *sbuf;
  b3dFloat *fbuf;
  int bytesSigned = (!hdata->mode && hdata->bytesSigned) ? 1 : 0;
  FILE *fpSave = hdata->fp;
  IloadInfo li;
  ImodImageFile *iiFile;

  if (!buf || slice < 0)
    return(-1);

  if (axis == 'Z' || axis == 'z') {
    mrc_init_li(&li, NULL);
    mrc_init_li(&li, hdata);
    hdata->fp = fout;
    j = mrcWriteZ(hdata, &li, buf, slice);
    hdata->fp = fpSave;
    return j;
  }

  iiFile = iiLookupFileFromFP(fout);
  if (iiFile && iiFile->file != IIFILE_MRC && iiFile->file != IIFILE_RAW) {
    b3dError(stderr, "ERROR: mrc_write_slice - Cannot write X or Y slice to non-MRC-like "
             "file\n");
    return(-1);
  }

  rewind(fout);
  fseek(fout, hdata->headerSize, SEEK_SET);
  data = (unsigned char *)buf;
  nx = hdata->nx;
  ny = hdata->ny;

  if (mrc_getdcsize(hdata->mode, &dsize, &csize)){
    b3dError(stderr, "ERROR: mrc_write_slice - unknown mode.\n");
    return(-1);
  }
  dcsize = dsize * csize;

  /* find out the actual size of the data in case swapped, and to get
     some error checks out of the way before getting memory */
  switch (axis){
  case 'x':
  case 'X':
    if (slice >= nx)
      return(-1);
    sxsize = ny;
    sysize = hdata->nz;
    break;
          
  case 'y':
  case 'Y':
    if (slice >= ny)
      return(-1);
    sxsize = nx;
    sysize = hdata->nz;
    break;
          
  default:
    b3dError(stderr, "ERROR: mrc_write_slice - axis error.\n");
    return(-1);
  }
  slicesize = sxsize * sysize;

  /* if swapped,  get memory, copy slice, and swap it in one gulp */
  if ((hdata->swapped && dsize > 1) || bytesSigned) {
    data = malloc(slicesize * dcsize);
    dataOrig = data;
    if (!data) {
      b3dError(stderr, "ERROR: mrc_write_slice - failure to allocate memory.\n");
      return(-1);
    }
    if (bytesSigned) {
      b3dShiftBytes((unsigned char *)buf, (char *)data, sxsize, sysize, 1, 1);
    } else {
      memcpy(data, buf, slicesize * dcsize);
      sbuf = (b3dInt16 *)data;
      fbuf = (b3dFloat *)data;
      for (j = 0; j < sysize; j++) {
        if (dsize == 2) {
          mrc_swap_shorts(sbuf, sxsize * csize);
          sbuf += sxsize * csize;
        } else {
          mrc_swap_floats(fbuf, sxsize * csize);
          fbuf += sxsize * csize;
        }
      }
    }
  }
     
  switch (axis) {
  case 'x':
  case 'X':
    fseek( fout, slice * dcsize, SEEK_CUR);
    for(k = 0; k < hdata->nz && !retval; k++) {
      for (j = 0; j < ny; j++) {
        if (fwrite(data, dcsize, 1, fout) != 1) {
          b3dError(stderr, "ERROR: mrc_write_slice x - fwrite error.\n");
          retval = -1;
          break;
        }
        data += dcsize;
        fseek(fout, dcsize * (nx - 1), SEEK_CUR);
      }
    }
    break;
      
  case 'y':
  case 'Y':
    /* fseek( fout, slice * nx * dcsize, SEEK_CUR);*/
    mrcHugeSeek(fout, 0, 0, slice, 0, nx, ny, dcsize, SEEK_CUR);
    for (k = 0; k < hdata->nz; k++) {
      if (fwrite(data, dcsize, nx, fout) != nx) {
        b3dError(stderr, "ERROR: mrc_write_slice y - fwrite error.\n");
        retval = -1;
        break;
      }
      data += dcsize * nx;
      /* fseek(fout, dcsize * (xysize - nx), SEEK_CUR); */
      mrcHugeSeek(fout, 0, 0, ny - 1, 0, nx, ny, dcsize, SEEK_CUR);
    }
    break;
    
  default:
    b3dError(stderr, "ERROR: mrc_write_slice - axis error.\n");
    retval = -1;
  }
  if ((hdata->swapped && dsize > 1) || bytesSigned)
    free(dataOrig);
  return(retval);
}

/*!
 * Writes one Z slice of data at Z = [slice] from the buffer [buf] to file
 * [fout] according the header in [hdata].  If parallel writing has been 
 * initialized, lines will be written to a boundary file if appropriate.
 * Returns errors from writing the slice with @mrc_write_slice and also returns
 * other non-zero values from opening the boundary file, writing its header, 
 * or writing to the file.
 */
int parallelWriteSlice(void *buf, FILE *fout, MrcHeader *hdata, int slice)
{
  static MrcHeader hbound;
  static int dsize, csize, linesBound = -1;
  static int sections[2], startLines[2];
  static FILE *fpBound;
  int err, allsec, nfiles, ib;
  char *filename;

  err = mrc_write_slice(buf, fout, hdata, slice, 'Z');
  if (err)
    return err;
  if (linesBound < 0) {
    if (parWrtProperties(&allsec, &linesBound, &nfiles))
      linesBound = 0;
    if (!linesBound)
      return 0;
    mrc_head_new(&hbound, hdata->nx, linesBound, 2, hdata->mode);
    err = parWrtFindRegion(slice, 0, hdata->ny, &filename, sections, 
                           startLines);
    if (err) {
      b3dError(stdout, "ERROR: sliceWriteParallel - finding parallel writing"
               " region for slice %d (err %d)\n", slice, err);
      return err;
    }
    if (mrc_getdcsize(hdata->mode, &dsize, &csize)){
      b3dError(stdout, "ERROR: sliceWriteParallel - unknown mode.\n");
      return 1;
    }
    imodBackupFile(filename);
    fpBound = fopen(filename, "wb");
    if (!fpBound) {
      b3dError(stdout, "ERROR: sliceWriteParallel - opening boundary file %s"
               "\n", filename);
      return 1;
    }
    if (mrc_head_write(fpBound, &hbound))
      return 1;
  }

  if (!linesBound)
    return 0;
  for (ib = 0; ib < 2; ib++) {
    if (sections[ib] >= 0 && slice == sections[ib]) {
      fseek(fpBound, hbound.headerSize + ib * hbound.nx * linesBound * csize *
            dsize, SEEK_SET);
      filename = (char *)buf;
      filename += hbound.nx * startLines[ib] * csize * dsize;
      err = mrc_write_slice(filename, fpBound, &hbound, ib, 'Z');
      if (err)
        return err;
    }
  }
  return 0;
}


/*
 * Support functions: DOC_SECTION SUPPORT
 */

/*****************************/
/* Get memory for image data */
/* DNM 3/25/03: try to load contiguous if directed, then drop back to
   separate chunks to get the message about where it failed */
/* DNM 1/1/04: turn this into a routine for use in 3dmod alternate loading */
/*!
 * Allocates memory for [zsize] planes of data to contain [xysize] pixels at
 * [pixsize] bytes per pixel, and returns an array of pointers to the planes.
 * It attempts to allocate the data in contigous memory if [li] is non-NULL and
 * the {contig} element of [li] is non-zero.  If this fails it falls back to 
 * allocating planes separately, sets {contig} to 0, and issues a warning with
 * b3dError.  Should be able to allocate planes > 4GB on 64-bit systems.
 * Returns NULL for error.
 */
unsigned char **mrcGetDataMemory(IloadInfo *li, size_t xysize, int zsize,
                                 int pixsize)
{
  int contig = 0;   /* if true: load date into contiguous memory.         */
  unsigned char **idata;        /* image data to return. */
  unsigned char *bdata = NULL;
  int i;

  if (li)
    contig = li->contig;

  idata = (unsigned char **)malloc(zsize * sizeof(unsigned char *)); 
  if (!idata)
    return(NULL); 
  for (i = 0; i < zsize; i++)
    idata[i] = NULL;
     
  if (contig) {
    bdata = (unsigned char *)malloc(xysize * zsize * pixsize * 
                                    sizeof(unsigned char));
    if (!bdata) {
      b3dError(stderr, "WARNING: mrcGetDataMemory - "
               "Not enough contiguous memory to load image data.\n");
      if (li)
        li->contig = 0;
    } else {
      for (i = 0; i < zsize; i++)
        idata[i] = bdata + (xysize * i * pixsize);
      return (idata);
    }
  }

  for (i = 0; i < zsize; i++) {
    idata[i] = (unsigned char *)malloc(xysize * pixsize * 
                                       sizeof(unsigned char));
    if (!idata[i]) {
      b3dError(stderr, "ERROR: mrcGetDataMemory - Not enough memory"
               " for image data after %d sections.\n", i);

      mrcFreeDataMemory(idata, 0, zsize);
      return(NULL);
    }
  }
  return(idata);
}

/*!
 * Frees data memory allocated by @mrcGetDataMemory; [zsize] specifies the 
 * number of Z planes and [contig] indicates if the memory was allocated 
 * contiguously 
 */
void mrcFreeDataMemory(unsigned char **idata, int contig, int zsize)
{
  int i;
  if (contig)
    zsize = 1;
  for (i = 0; i < zsize; i++) {
    if (idata[i])
      free(idata[i]);
  }
  free(idata);
}

/*!
 * Returns a pointer to a lookup table of 256 scaled intensities, given a
 * scaling specified by index * [slope] + [offset].  Output values will be
 * truncated at [outmin] and [outmax].  If [outmax] is
 * <= 255 then the map will indeed be unsigned bytes, but if [outmax] is > 255 then
 * the map will be unsigned shorts instead.  If [bytesSigned] is nonzero, then the map 
 * will be wrapped around so that signed values read from file and used as unsigned values
 * when indexing the map will be shifted up by 128.
 */
unsigned char *get_byte_map(float slope, float offset, int outmin, int outmax, 
                            int bytesSigned)
{
  static unsigned char map[256];
  static b3dUInt16 smap[256];
  int i, ival, base = 0;
  float fpixel;
  if (bytesSigned)
    base = 128;

  for (i = 0; i < 256; i++) {
    fpixel = i;
    fpixel *= slope;
    fpixel += offset;
    ival = floor((double)fpixel + 0.5);
    if (ival < outmin)
      ival = outmin;
    if (ival > outmax)
      ival = outmax;
    if (outmax > 255)
      smap[(i+base) % 256] = ival;
    else
      map[(i+base) % 256] = ival;
  }
  if (outmax > 255)
    return ((unsigned char *)smap);
  else
    return (map);
}

/*!
 * Returns a pointer to a lookup table of scaled intensities for 65536 index
 * values, which can be either unsigned or signed short integers.  The
 * scaling will be index * [slope] + [offset] for [ramptype] = MRC_RAMP_LIN,
 * exp(index) * [slope] + [offset] for [ramptype] = MRC_RAMP_EXP, and
 * log(index) * [slope] + [offset] for [ramptype] = MRC_RAMP_LOG.
 * Output values will be truncated at [outmin] and [outmax].  If [outmax] is
 * <= 255 then the map will indeed be unsigned bytes, but if [outmax] is > 255 then
 * the map will be unsigned shorts instead.  Set [swapbytes] nonzero to for a table that 
 * swaps bytes, and [signedint] nonzero for signed integer indices.  The table is
 * allocated with {malloc} and should be freed by the caller.  Returns NULL for
 * error allocating memory.
 */
unsigned char *get_short_map(float slope, float offset, int outmin, int outmax,
                             int ramptype, int swapbytes, int signedint)
{
  int i, ival;
  b3dUInt16 index;
  float fpixel;
  int toShort = outmax > 255 ? 1 : 0;
  unsigned char *map = (unsigned char *)malloc(65536 * (toShort + 1));
  b3dUInt16 *smap = (b3dUInt16 *)map;
  if (!map) {
    b3dError(stderr, "ERROR: get_short_map - getting memory");
    return 0;
  }
  for (i = 0; i < 65536; i++) {
    fpixel = i;
    if (i > 32767 && signedint)
      fpixel = i - 65536;
    if (ramptype == MRC_RAMP_EXP)
      fpixel = (float)exp((double)fpixel);
    if (ramptype == MRC_RAMP_LOG)
      fpixel = (float)log((double)fpixel);
    fpixel *= slope;
    fpixel += offset;
    ival = floor((double)fpixel + 0.5);
    if (ival < outmin)
      ival = outmin;
    if (ival > outmax)
      ival = outmax;
    index = i;
    if (swapbytes)
      mrc_swap_shorts((b3dInt16 *)&index, 1);
    if (toShort)
      smap[index] = ival;
    else
      map[index] = ival;
  }
  return (map);
}

/*!
 * Returns a standard scaling factor for taking the log of complex data 
 * by log (1 + scale * value)
 */
float mrcGetComplexScale()
{
  return 5.0;
}

/*!
 * Computes the min and max for log scaling of complex numbers between [inMin] 
 * and [inMax] and returns them in [outMin] and [outMax]
 */
void mrcComplexSminSmax(float inMin, float inMax, float *outMin, 
                         float *outMax)
{
  float minSign = 1.;
  float kscale = mrcGetComplexScale();
  if (inMin < 0.) {
    minSign = -1.;
    inMin = -inMin;
  }
  *outMin = minSign * (float)(log((double)(1.0 + kscale * inMin)));
  *outMax = (float)log((double)(1.0 + kscale * inMax));
}

/*!
 * For a mirrored FFT whose full size is [nx] by [ny], computes where the
 * location [imageX], [imageY] in the image comes from in the file and returns
 * this location in [fileX], [fileY].
 */
void mrcMirrorSource(int nx, int ny, int imageX, int imageY, int *fileX,
                     int *fileY)
{
  *fileY = imageY;
  if (!imageX) {
    *fileX = nx / 2;
  } else if (imageX >= nx / 2) {
    *fileX = imageX - nx / 2;
  } else {
    *fileX = nx / 2 - imageX;
    *fileY = imageY ? ny - imageY : ny - 1;
  }
}



/*
 * Misc std I/O functions.
 */
/*!
 * Initialize the @@IloadInfo structure@ [li] with some sensible default values
 * if [hd] is NULL; otherwise it just calls @mrc_fix_li with the sizes defined
 * in the header [hd].  For proper initialization, this function must be called once with
 * a [hd] NULL, then again with a header if needed. Returns -1 if [hd] is NULL.
 */
int mrc_init_li(IloadInfo *li, MrcHeader *hd)
{

  if (li == NULL)
    return(-1);

  /* Init li for loading in values. */
  if (hd == NULL){
    li->xmin = -1;
    li->xmax = -1;
    li->ymin = -1;
    li->ymax = -1;
    li->zmin = -1;
    li->zmax = -1;
    li->padLeft = 0;
    li->padRight = 0;
    li->ramp = 0;
    li->black = 0;
    li->white = 255;
    li->axis = 3;
    li->mirrorFFT = 0;
    li->smin = li->smax = 0.0f;
    li->contig = 0;
    li->outmin = 0;
    li->outmax = 255;
    li->scale = 1.0f;
    li->offset = 0.0f;
    li->plist = 0;
    li->ramp = MRC_RAMP_LIN;
    /* Check li values and change to default for bad data. */
  }else{
    mrc_fix_li(li, hd->nx, hd->ny, hd->nz);

  }
  return(0);
}

/*!
 * Fixes the min and max loading parameters ({xmin}, {xmax}, etc) in the
 * @@IloadInfo structure@ * [li] based on the image dimensions given either in
 * [nx], [ny], [nz] or in the {px}, {py}, and {pz} elements of [li] if there is
 * a piece list.  Undefined parameters are changed to full-range values and 
 * values are fixed to be within 0 to the dimension - 1.
 */
int mrc_fix_li(IloadInfo *li, int nx, int ny, int nz)
{
  int mx, my, mz;
  mx = nx; my = ny; mz = nz;

  /* If piece list, use image size from pxyz and adjust the loading 
   coordinates by the piece list offsets */
  if (li->plist){
    mx = (int)li->px;
    my = (int)li->py;
    mz = (int)li->pz;
    if (li->xmin != -1)
      li->xmin -= (int)li->opx;
    if (li->xmax != -1)
      li->xmax -= (int)li->opx;
    if (li->ymin != -1)
      li->ymin -= (int)li->opy;
    if (li->ymax != -1)
      li->ymax -= (int)li->opy;
    if (li->zmin != -1)
      li->zmin -= (int)li->opz;
    if (li->zmax != -1)
      li->zmax -= (int)li->opz;
  }

  /*        printf("before: x (%d, %d), y (%d, %d), z (%d, %d)\n",
            li->xmin, li->xmax, li->ymin, li->ymax, li->zmin, li->zmax);
  */
  if (li->xmax < 0)
    if ((li->xmin > 0) && (li->xmin < mx)){
      li->xmax = (mx/2) + (li->xmin/2);
      li->xmin = li->xmax - li->xmin + 1;
    }
  if ((li->xmax < 0) || (li->xmax > (mx - 1)))
    li->xmax = mx - 1;
  if ( (li->xmin < 0) || (li->xmin > li->xmax))
    li->xmin = 0;

  if (li->ymax < 0)
    if ((li->ymin > 0) && (li->ymin < my)){
      li->ymax = (my/2) + (li->ymin/2);
      li->ymin = li->ymax - li->ymin + 1;
    }
  if ((li->ymax < 0) || (li->ymax > (my - 1)))
    li->ymax = my - 1;
  if ((li->ymin < 0)  || (li->ymin > li->ymax))
    li->ymin = 0;

  /* don't let z be to big. */
  if (li->zmax >= mz)
    li->zmax = mz - 1;
  if (li->zmin >= mz)
    li->zmin = mz - 1;

  /* don't let zmax be undefined or less than zmin. */
  if ((li->zmax < 0) || (li->zmax < li->zmin)) {
    if (li->zmin >= 0)
      li->zmax = li->zmin;
    else
      li->zmax = mz - 1;
  }

  if ((li->zmin < 0)  || (li->zmin > li->zmax))
    li->zmin = 0;

  /* 7/7/06: Removed zinc entries */

  if ( (li->white > 255) || (li->white < 1))
    li->white = 255;
  if ( (li->black < 0) || (li->black > li->white))
    li->black = 0;
  if ( (li->axis > 3) ||  (li->axis < 1))
    li->axis = 3;

  /*        printf(" x (%d, %d), y (%d, %d), z (%d, %d)\n",
            li->xmin, li->xmax, li->ymin, li->ymax, li->zmin, li->zmax);
  */
  return(0);
}


/* UNused 8/2/06 */
void mrc_liso(MrcHeader *hdata, IloadInfo *li)
{
  float min, max;
  float range, rscale;

  max = hdata->amax;
  min = hdata->amin;
  if (li->ramp == MRC_RAMP_LOG){
    min = (float)log((double)hdata->amin);
    max = (float)log((double)hdata->amax);
  }
  if (li->ramp == MRC_RAMP_EXP){
    min = (float)exp((double)hdata->amin);
    max = (float)exp((double)hdata->amax);
  }
  range = li->white - li->black + 1;
  if (!range)
    range = 1;
  rscale = 256.0 / (float)range;
  if ((max - min) != 0)
    li->slope = 255.0 / (max - min);
  else
    li->slope = 1.0;
     
  li->slope *= rscale;
  li->offset = -(( ((float)li->black / 255.0) * (max - min)) + min) 
    * li->slope;
}


/*!
 * Gets x, y, and z loading limits for a MRC file whose header is in  [hdata]
 * and places them in the  @@IloadInfo structure@ * [li], using interactive
 * input from standard input.  Returns 1 for no errors.
 */
int get_loadinfo(MrcHeader *hdata, IloadInfo *li)
{
  char line[128];

  fflush(stdout);
  fflush(stdin);
  printf (" Enter (min x, max x). (return for default) >");
     
  fgetline(stdin, line, 127);
  if (line[0])
    sscanf(line, "%d%*c%d\n", &(li->xmin), &(li->xmax)); 
  else{
    li->xmin = 0;
    li->xmax = hdata->nx - 1;
  }

  printf (" Enter (min y, max y). (return for default)  >");
  fgetline(stdin, line, 127);
  if (line[0])
    sscanf(line, "%d%*c%d\n", &(li->ymin), &(li->ymax));
  else{
    li->ymin = 0;
    li->ymax = hdata->ny - 1;
  }

  printf (" Enter sections (low, high)  >");
  fgetline(stdin, line, 127);
  if (line[0])
    sscanf(line,"%d%*c%d\n", &(li->zmin), &(li->zmax));
  else{
    li->zmin = 0;
    li->zmax = hdata->nz - 1;
  }

  li->scale = 1;
     
  return(TRUE);
}



/*****************************************************************************/
/* Old function, need to auto read tilt data from header                     */
/*****************************************************************************/

int loadtilts(struct TiltInfo *ti, MrcHeader *hdata)
{
  char filename[128];
  int i,c;
  int tiltflag = 0;
  float tiltoff, tslope;
  FILE *fin = NULL;

  while(!tiltflag){
    printf("Do you wish to load a tilt info file? (y/n) >");
    switch (c = getchar())
      {
      case 'y':
      case 'Y':
        tiltflag = 1;
        break;
      case 'n':
      case 'N':
        tiltflag = 2;
        break;
      default:
        break;
      }
    c = getchar();
  }
  ti->tilt = (float *)malloc( hdata->nz * sizeof(float));
  if (!ti->tilt)
    return(0);

  if (tiltflag == 2){
    if (hdata->nz < 2)
      ti->tilt[0] = 0;
    else{
      tiltoff = -60.0;
      tslope = 120.0 / (hdata->nz - 1.0);
      for (i = 0; i < hdata->nz; i++)
        ti->tilt[i] = tiltoff + (i * tslope);
    }
    ti->axis_z = hdata->nz / 2;
    ti->axis_x = hdata->nx / 2;
  }

  if (tiltflag == 1){
    getfilename(filename, "Enter tilt info filename. >");
    fin = fopen(filename, "r");
    if (!fin){
      b3dError(stderr, "ERROR: loadtilts - Couldn't load %s.\n", filename);
      return(0);
    }
    for (i = 0; i < hdata->nz; i++)
      fscanf(fin, "%f", &(ti->tilt[i]));
    fscanf(fin, "%f", &(ti->axis_x));
    fscanf(fin, "%f", &(ti->axis_z));
  }

  return(1);

}  



/*****************************************************************************/
/* Function getfilename - gets a name of a file using stdio.                 */
/*                                                                           */
/* Returns length of filename.                                               */
/*****************************************************************************/

int getfilename(char *name, char *prompt)
{
  int c, i;
  
  printf("%s",prompt);
  fflush(stdout);
     
  for (i = 0; i < 255 && (c = getchar())!= EOF && c!='\n'; i++)
    name[i] = c;
  name[i] = '\0';
  return i;
}

/*!
 * For the given MRC file mode in [mode], returns the number of bytes of the
 * basic data element in [dsize] and the number of data channels in [csize].
 * Returns -1 for an unsupported or undefined mode.  Simply calls 
 * @@b3dutil.html#dataSizeForMode@ unless mode is SLICE_MODE_MAX.
 */
int mrc_getdcsize(int mode, int *dsize, int *csize)
{
  if (mode == SLICE_MODE_MAX)
    return -1;
  return dataSizeForMode(mode, dsize, csize);
}

/*!
 * Swaps the bytes in 16-bit integers in [data]; [amt] specifies the number of
 * values to swap.
 */
void mrc_swap_shorts(b3dInt16 *data, int amt)
{
  register unsigned char *ldata = (unsigned char *)data + (amt * 2);
  register unsigned char *ptr = (unsigned char *)data;
  register unsigned char tmp;

  while(ptr < ldata){
    tmp = *ptr;
    *ptr = ptr[1];
    ptr[1] = tmp;
    ptr+=2;
  }

}

/*!
 * Swaps the bytes in 32-bit integers in [data]; [amt] specifies the number of
 * values to swap.
 */
void mrc_swap_longs(b3dInt32 *data, int amt)
{
  register unsigned char *ldata = (unsigned char *)data + (amt * 4);
  register unsigned char *ptr = (unsigned char *)data;
  register unsigned char tmp;
  while(ptr < ldata){
    tmp = ptr[0];
    ptr[0] = ptr[3];
    ptr[3] = tmp;
    ptr++;
    tmp = *ptr;
    *ptr = ptr[1];
    ptr[1] = tmp;
    ptr+=3;
  }
}

#ifdef SWAP_IEEE_FLOATS

/* IEEE: use a copy of swap_longs to swap the bytes  */

/*!
 * Swaps the bytes in 32-bit floats in [data]; [amt] specifies the number of
 * values to swap.
 */
void mrc_swap_floats(b3dFloat *data, int amt)
{
  register unsigned char *ldata = (unsigned char *)data + (amt * 4);
  register unsigned char *ptr = (unsigned char *)data;
  register unsigned char tmp;
  while(ptr < ldata){
    tmp = ptr[0];
    ptr[0] = ptr[3];
    ptr[3] = tmp;
    ptr++;
    tmp = *ptr;
    *ptr = ptr[1];
    ptr[1] = tmp;
    ptr+=3;
  }
}

#else

#ifndef __vms

/* To convert floats from little-endian VMS to big-endian IEEE */

void mrc_swap_floats(b3dFloat *data, int amt)
{
  unsigned char exp, temp;
  int i;
  register unsigned char *ptr = (unsigned char *)data;
  register unsigned char *maxptr = (unsigned char *)data + (amt * 4);
     
  while (ptr < maxptr){

    if ((exp = (ptr[1] << 1) | (ptr[0] >> 7 & 0x01)) > 3 &&
        exp != 0)
      ptr[1] -= 1;
    else if (exp <= 3 && exp != 0)  /*must zero out the mantissa*/
      {
        /*we want manitssa 0 & exponent 1*/
        ptr[0] = 0x80;
        ptr[1] &= 0x80;
        ptr[2] = ptr[3] = 0;
      }
          
    temp = ptr[0];
    ptr[0] = ptr[1];
    ptr[1] = temp;
    temp = ptr[2];
    ptr[2] = ptr[3];
    ptr[3] = temp;
    ptr+=4;
  }
}

#else

/* If VMS: To convert floats from big-endian IEEE to little-endian VMS */

void mrc_swap_floats(fb3dFloat *data, int amt)
{
  unsigned char exp, temp;
  int i;
  register unsigned char *ptr = (unsigned char *)data;
  register unsigned char *maxptr = (unsigned char *)data + (amt * 4);
     
  while (ptr < maxptr){
    if ((exp = (ptr[0] << 1) | (ptr[1] >> 7 & 0x01)) < 253 && exp != 0)
      ptr[0] += 1;
    else if (exp >= 253) /*must also max out the exp & mantissa*/
      {
        /*we want manitssa all 1 & exponent 255*/
        ptr[0] |= 0x7F;
        ptr[1] = 0xFF;
        ptr[2] = ptr[3] = 0xFF;
      }
          
    temp = ptr[0];
    ptr[0] = ptr[1];
    ptr[1] = temp;
    temp = ptr[2];
    ptr[2] = ptr[3];
    ptr[3] = temp;
    ptr+=4;
  }
}

#endif
#endif

