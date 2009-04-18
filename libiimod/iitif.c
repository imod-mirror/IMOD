/*
 *    iitif.c    - specific routines for tiff-type ImodImageFile's
 *
 *    Authors:  James Kremer and David Mastronarde
 *
 *   Copyright (C) 1995-2006 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.
 *
 *  $Id$
 *  Log at end of file
 */

/************************************************************************** 
This software uses the tiff library which has the following copyright:
Copyright (c) 1988-1996 Sam Leffler
Copyright (c) 1991-1996 Silicon Graphics, Inc.

Permission to use, copy, modify, distribute, and sell this software and
its documentation for any purpose is hereby granted without fee, provided
that (i) the above copyright notices and this permission notice appear in
all copies of the software and related documentation, and (ii) the names of
Sam Leffler and Silicon Graphics may not be used in any advertising or
publicity relating to the software without the specific, prior written
permission of Sam Leffler and Silicon Graphics.

THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND,
EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY
WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.

IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR
ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF
LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
OF THIS SOFTWARE.

Additional documentation is at <ftp://ftp.sgi.com/graphics/tiff/doc>
****************************************************************************/

#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include "imodconfig.h"
#ifdef NOTIFFLIBS
#include "notiffio.h"
#else
#include "tiffio.h"
#endif

#include "iimage.h"
#include "b3dutil.h"

#ifdef _WIN32
#define vsnprintf _vsnprintf
#endif

int tiffReopen(ImodImageFile *inFile);
void tiffDelete(ImodImageFile *inFile);
static int ReadSection(ImodImageFile *inFile, char *buf, int inSection,
                       int byte);
static void copyLine(unsigned char *bdata, unsigned char *obuf, int xout,
                     int byte, int pixsize, int samples, float slope, 
                     float offset, int doscale, unsigned char *map);
static TIFF *openWithoutBMode(ImodImageFile *inFile);
static int setMatchingDirectory(ImodImageFile *inFile, int dirnum);
static void closeWithError(ImodImageFile *inFile, char *message);
typedef void (*TIFFWarningHandler)(const char *module, const char *fmt,
                                   va_list ap);

static TIFFWarningHandler oldHandler = NULL;
static void warningHandler(const char *module, const char *fmt, va_list ap);

int iiTIFFCheck(ImodImageFile *inFile)
{
  TIFF* tif;
  FILE *fp;
  b3dUInt16 buf;
  int dirnum = 1;
  uint32 val;
  uint16 bits, samples, photometric, sampleformat, planarConfig;
  uint16 bitsIm, samplesIm, photoIm, formatIm, planarIm;
  int nxim, nyim, formatDef;
  int defined, i, j, mismatch = 0, err = 0;
  double minmax;
  b3dUInt16 *redp, *greenp, *bluep;

  if (!inFile) 
    return IIERR_BAD_CALL;
  fp = inFile->fp;
  if (!fp)
    return IIERR_BAD_CALL;

  rewind(fp);
  if (fread(&buf, sizeof(b3dUInt16), 1, fp) < 1)
    err = IIERR_IO_ERROR;
  if (!err && (buf != 0x4949) && (buf != 0x4d4d))
    err = IIERR_NOT_FORMAT;
  if (!err && fread(&buf, sizeof(b3dUInt16), 1, fp) < 1)
    err = IIERR_IO_ERROR;
  if (!err && (buf != 0x002a) && (buf != 0x2a00))
    err = IIERR_NOT_FORMAT;
  if (err) {
    if (err == IIERR_IO_ERROR)
      b3dError(stderr, "ERROR: iiTIFFCheck - Reading file %s\n", 
               inFile->filename);
    return err;
  }

  /* Close file now, but reopen it if there is a TIFF failure */
  fclose(fp);
  tif = openWithoutBMode(inFile);
  if (!tif){
    inFile->fp = fopen(inFile->filename, inFile->fmode);
    b3dError(stderr, "ERROR: iiTIFFCheck - Calling TIFFOpen on file %s\n",
             inFile->filename);
    return(IIERR_IO_ERROR);
  }
    
  inFile->header = (char *)tif;
  inFile->nx = inFile->ny = 0;
  inFile->multipleSizes = 0;
  inFile->planesPerImage = 1;
  inFile->contigSamples = 1;

  /* Read each directory of the file, get properties and count usable images */
  do {
    TIFFGetField(tif, TIFFTAG_IMAGEWIDTH, &nxim);
    TIFFGetField(tif, TIFFTAG_IMAGELENGTH, &nyim);
    TIFFGetField(tif, TIFFTAG_BITSPERSAMPLE, &bitsIm);
    TIFFGetField(tif, TIFFTAG_PHOTOMETRIC, &photoIm);

    /* DNM 11/18/01: field need not be defined, set a default */
    defined = TIFFGetField(tif, TIFFTAG_SAMPLESPERPIXEL, &samplesIm);
    if (!defined)
      samplesIm = 1;

    TIFFGetField(tif, TIFFTAG_PLANARCONFIG, &planarIm);
    if (!defined)
      photoIm = PLANARCONFIG_CONTIG;

    defined = TIFFGetField(tif, TIFFTAG_SAMPLEFORMAT, &formatIm);

    /* If this is a bigger image, it is a new standard, so set all the
       properties and reset to one directory */
    if ((float)nxim * (float)nyim > (float)inFile->nx * (float)inFile->ny) {
      inFile->nx = nxim;
      inFile->ny = nyim;
      bits = bitsIm;
      photometric = photoIm;
      planarConfig = planarIm;
      samples = samplesIm;
      formatDef = defined;
      sampleformat = formatIm;
      
      if (dirnum)
        inFile->multipleSizes = 1;
      dirnum = 1;
    } else if (nxim == inFile->nx && nyim == inFile->ny) {
      dirnum++;

      /* If size matches, check that everything matches */
      if (bitsIm != bits || photoIm != photometric || planarIm != planarConfig
          || samples != samplesIm || defined != formatDef || 
          (defined && formatIm != sampleformat)) {
        mismatch = 1;
        break;
      }
    } else if (dirnum)
      inFile->multipleSizes = 1;
  } while (TIFFReadDirectory(tif));

  TIFFSetDirectory(tif, 0);

  /* Don't know how to get the multiple bit entries from libtiff, so can't test
     if they are all 8 */
  if (mismatch || 
      !(((bits == 8 || bits ==16 || bits == 32) && 
         photometric < PHOTOMETRIC_RGB) ||
        (samples == 3 && photometric == PHOTOMETRIC_RGB && bits == 8) || 
        (photometric == PHOTOMETRIC_PALETTE && bits == 8))) {
    closeWithError(inFile, "ERROR: iiTIFFCheck - Unsupported type of TIFF "
                   "file\n");
    return(IIERR_NO_SUPPORT);
  }

  inFile->nz     = dirnum;
  inFile->file   = IIFILE_TIFF;
  inFile->format = IIFORMAT_LUMINANCE;
  if (photometric < PHOTOMETRIC_RGB) {
    if (planarConfig == PLANARCONFIG_SEPARATE)
      inFile->planesPerImage = samples;
    else
      inFile->contigSamples = samples;
    inFile->nz = dirnum * samples;
  }

  /* 11/22/08: define this for all types, not just for 3-sample data */
  inFile->readSection = tiffReadSection;
  inFile->readSectionByte = tiffReadSectionByte;

  if (bits == 8) {
    inFile->type   = IITYPE_UBYTE;
    inFile->amin  = 0;
    inFile->amean  = 128;
    inFile->amax   = 255;
    inFile->mode   = MRC_MODE_BYTE;
    if (samples == 3) {
      inFile->format = IIFORMAT_RGB;
      inFile->mode   = MRC_MODE_RGB;
      inFile->readSectionByte = NULL;
    } else if (photometric == 3) {

      /* For palette images, define as colormap, better send byte reading
         to routine that will ignore any scaling, get the colormap and 
         convert it to bytes */
      inFile->format = IIFORMAT_COLORMAP;
      inFile->readSectionByte = tiffReadSection;
      inFile->colormap = (unsigned char *)malloc(3 * 256 * dirnum);
      if (!inFile->colormap) {
        closeWithError(inFile, "ERROR: iiTIFFCheck - Getting memory for "
                       "colormap\n");
        return(IIERR_MEMORY_ERR);
      }
      for (j = 0; j < dirnum; j++) {
        if (setMatchingDirectory(inFile, j)) {
          closeWithError(inFile, "ERROR: iiTIFFCheck - getting directory for "
                         "colormap\n");
          return(IIERR_IO_ERROR);
        }

        TIFFGetField(tif, TIFFTAG_COLORMAP, &redp, &greenp, &bluep);
        for (i = 0; i < 256; i++) {
          inFile->colormap[j*768 + i] = (unsigned char)(redp[i] >> 8);
          inFile->colormap[j*768 + i + 256] = (unsigned char)(greenp[i] >> 8);
          inFile->colormap[j*768 + i + 512] = (unsigned char)(bluep[i] >> 8);
        }
      }
      TIFFSetDirectory(tif, 0);
    }
  } else {
    /* If there is a field specifying signed numbers, set up for signed;
       otherwise set up for unsigned */
    defined = TIFFGetField(tif, TIFFTAG_SAMPLEFORMAT, &sampleformat);
    if (defined && sampleformat == SAMPLEFORMAT_INT) {
      inFile->type   = IITYPE_SHORT;
      inFile->amean  = 0;
      inFile->amin   = -32767;
      inFile->amax   = 32767;
      inFile->mode   = MRC_MODE_SHORT;
    } else if ((defined && sampleformat == SAMPLEFORMAT_IEEEFP) || bits == 32){
      inFile->type   = IITYPE_FLOAT;
      inFile->amean  = 128.;
      inFile->amin   = 0;
      inFile->amax   = 255.;
      inFile->mode   = MRC_MODE_FLOAT;
    } else {
      inFile->type   = IITYPE_USHORT;
      inFile->amean  = 32767;
      inFile->amin   = 0;
      inFile->amax   = 65535;
      inFile->mode   = MRC_MODE_USHORT;   /* Why was this SHORT for both? */
    }
  }
  
  /* Use min and max from file if defined (better be there for float) */
  if (TIFFGetField(tif, TIFFTAG_SMINSAMPLEVALUE, &minmax))
    inFile->amin = minmax;
  if (TIFFGetField(tif, TIFFTAG_SMAXSAMPLEVALUE, &minmax))
    inFile->amax = minmax;

  inFile->smin   = inFile->amin;
  inFile->smax   = inFile->amax;
  inFile->headerSize = 8;
  inFile->sectionSkip = 0;
  inFile->fp = (FILE *)tif;    
  inFile->cleanUp = tiffDelete;
  inFile->reopen = tiffReopen;
  inFile->close = tiffClose;
  return(0);
}

int tiffReopen(ImodImageFile *inFile)
{
  TIFF* tif;
  tif = openWithoutBMode(inFile);
  if (!tif)
    return 1;
  inFile->headerSize = 8;
  inFile->sectionSkip = 0;
  inFile->header = (char *)tif;    
  inFile->fp = (FILE *)tif;    
  return 0;
}

void tiffClose(ImodImageFile *inFile)
{
  TIFF* tif = (TIFF *)inFile->header;
  if (tif)
    TIFFClose(tif);
  inFile->header = NULL;
  inFile->fp = NULL;
}

void tiffDelete(ImodImageFile *inFile)
{
  tiffClose(inFile);
}

/* Get the value for a field that returns a single value */
int tiffGetField(ImodImageFile *inFile, int tag, void *value)
{
  TIFF *tif;
  if (!inFile)
    return -1;
  if (!inFile->header)
    iiReopen(inFile);
  tif = (TIFF *)inFile->header;
  if (!tif)
    return -1;
  return TIFFGetField(tif, (ttag_t)tag, value);
}
/* Get the value for a field that returns the address of an array.  The count
   argument seems to be required but does not seem to return the count */
int tiffGetArray(ImodImageFile *inFile, int tag, int *count, void *value)
{
  TIFF *tif;
  if (!inFile)
    return -1;
  if (!inFile->header)
    iiReopen(inFile);
  tif = (TIFF *)inFile->header;
  if (!tif)
    return -1;
  return TIFFGetField(tif, (ttag_t)tag, count, value);
}

void tiffSuppressErrors(void)
{
  TIFFSetErrorHandler(NULL);
}


void tiffSuppressWarnings(void)
{
  TIFFSetWarningHandler(NULL);
}

static void warningHandler(const char *module, const char *fmt, va_list ap)
{
  char buffer[100];
  vsnprintf(buffer, 99, fmt, ap);
  va_end(ap);
  if (!strstr(buffer, "unknown field with tag") && oldHandler)
    oldHandler(module, fmt, ap);
}

void tiffFilterWarnings(void)
{
  oldHandler = TIFFSetWarningHandler(warningHandler);
}

/* Mode 'b' means something completely different for TIFF, so strip it */
static TIFF *openWithoutBMode(ImodImageFile *inFile)
{
  TIFF *tif;
  int len, stripped = 0;
  char *tmpmode = inFile->fmode;
  if (!tmpmode)
    return NULL;
  len = strlen(tmpmode);
  if (!len)
    return NULL;

  if (inFile->fmode[len - 1] == 'b') {
    stripped = 1;
    tmpmode = strdup(inFile->fmode);
    if (!tmpmode)
      return NULL;
    tmpmode[len - 1] = 0x00;
  }
  tif = TIFFOpen(inFile->filename, tmpmode);
  if (stripped)
    free(tmpmode);
  return tif;
}

/* Find the directory at the given number that matches the proper size */
static int setMatchingDirectory(ImodImageFile *inFile, int dirnum)
{
  int nx, ny, dir = 0;
  TIFF *tif = (TIFF *)inFile->header;
  TIFFSetDirectory(tif, 0);
  do {
    TIFFGetField(tif, TIFFTAG_IMAGEWIDTH, &nx);
    TIFFGetField(tif, TIFFTAG_IMAGELENGTH, &ny);
    if (nx == inFile->nx && ny == inFile->ny) {
      if (dir == dirnum)
        return 0;
      else
        dir++;
    }
  } while (TIFFReadDirectory(tif));
  return 1;
}

static void closeWithError(ImodImageFile *inFile, char *message)
{
  TIFFClose((TIFF *)inFile->header);
  inFile->fp = fopen(inFile->filename, inFile->fmode);
  b3dError(stderr, message);
}

/* DNM 12/24/00: Got this working for bytes, shorts, and RGBs, for whole
   images or subsets, and using maps for scaling */
/* DNM 11/18/01: Added ability to read tiles, made tiffReadSection and 
   tiffReadSectionByte call a common routine to reduce duplicate code */

static int ReadSection(ImodImageFile *inFile, char *buf, int inSection,
                       int byte)
{
  int nstrip, si, plane, sampOffset;
  int xout, xcopy;
  int xsize = inFile->nx;
  int ysize = inFile->ny;
  int samples = inFile->contigSamples;
  int row;
  int i, pixel, xstart, xend, ystart, yend, y, ofsin, ofsout;
  int doscale;
  float slope = inFile->slope;
  float offset = inFile->offset;
  int outmin = 0;
  int outmax = 255;
  int stripsize;
  int xmin, xmax, ymin, ymax;
  int pixsize = 1;
  int movesize = 1;
  unsigned char *obuf;
  unsigned char *tmp = NULL;
  unsigned char *bdata;
  b3dUInt16 *usdata;
  b3dFloat *fdata;
  unsigned char *map = NULL;
  int freeMap = 0;
  uint32 rowsperstrip;
  int nread;
  int tilesize, tilewidth, tilelength, xtiles, ytiles, xti, yti;
     
  TIFF* tif = (TIFF *)inFile->header;
  if (inFile->axis == 2)
    return -1;
  if (byte && (inFile->format != IIFORMAT_LUMINANCE))
    return -1;
  if (!tif)
    iiReopen(inFile);
  tif = (TIFF *)inFile->header;
  if (!tif)
    return -1;

  /* set the dimensions to read in */
  /* DNM 2/26/03: replace upper right only if negative */
  xmin   = inFile->llx;
  ymin   = inFile->lly;
  if (inFile->urx < 0)
    xmax = inFile->nx-1;
  else
    xmax = inFile->urx;
  if (inFile->ury < 0)
    ymax = inFile->ny-1;
  else
    ymax = inFile->ury;
  xout = xmax + 1 - xmin;
  doscale = (offset <= -1.0 || offset >= 1.0 || 
             slope < 0.995 || slope > 1.005);
     
  row = inSection / (inFile->planesPerImage * samples);
  setMatchingDirectory(inFile, row);
  plane = inSection % inFile->planesPerImage;
  sampOffset = inSection % samples;
  if (byte) {
    if (inFile->type == IITYPE_SHORT) {
      pixsize = 2;
      map = get_short_map(slope, offset, outmin, outmax, MRC_RAMP_LIN, 0, 1);
      freeMap = 1;
    } else if (inFile->type == IITYPE_USHORT) {
      pixsize = 2;
      map = get_short_map(slope, offset, outmin, outmax, MRC_RAMP_LIN, 0, 0);
      freeMap = 1;
    } else if (inFile->type == IITYPE_FLOAT) {
      pixsize = 4;
    } else if (doscale)
      map = get_byte_map(slope, offset, outmin, outmax);
  } else {
    if (inFile->format == IIFORMAT_RGB)
      pixsize = 3;
    else if (inFile->type == IITYPE_SHORT || inFile->type == IITYPE_USHORT)
      pixsize = 2;
    else if (inFile->type == IITYPE_FLOAT)
      pixsize = 4;
    movesize = pixsize;
  }

  if (freeMap && !map)
    return -1;

  if (TIFFGetField(tif, TIFFTAG_ROWSPERSTRIP, &rowsperstrip)) {

    /* if data are in strips, get strip size and memory for it */
    stripsize = TIFFStripSize(tif);
    tmp = (unsigned char *)malloc(stripsize);
    if (!tmp) {
      if (freeMap)
        free(map);
      return -1;
    }
               
    nstrip = TIFFNumberOfStrips(tif) / inFile->planesPerImage;
    /* printf("%d %d %d %d\n", stripsize, rowsperstrip, nstrip, 
       pixsize); */

    for (si = 0 ; si < nstrip; si++){
               
      /* Compute starting and ending Y values to use in each strip */
      ystart = ysize - 1 - (rowsperstrip * (si + 1) - 1);
      yend = ysize - 1 - (rowsperstrip * si);
      if (ymin > ystart)
        ystart = ymin;
      if (ymax < yend)
        yend = ymax;
      if (ystart > yend)
        continue;
               
      /* Read the strip if necessary */
      nread = TIFFReadEncodedStrip(tif, si + plane * nstrip, tmp, stripsize);
      /* printf("%d %d %d %d\n", nread, si, ystart, yend); */
      for (y = ystart; y <= yend; y++) {

        /* for each y, compute back to row, and get offsets into
           input and output arrays */
        row = ysize - 1 - y - rowsperstrip * si;
        ofsin = samples * pixsize * (row * xsize + xmin) + sampOffset *pixsize;
        ofsout = movesize * (y - ymin) * xout;
        obuf = (unsigned char *)buf + ofsout;
        bdata = tmp + ofsin;
        copyLine(bdata, obuf, xout, byte, pixsize, samples, slope, offset,
                 doscale, map);
      }    
    }
  } else {

    /* Otherwise make sure there are tiles, if not return with error */
    if (TIFFGetField(tif, TIFFTAG_TILEWIDTH, &tilewidth)) {
      tilesize = TIFFTileSize(tif);
      tmp = (unsigned char *)malloc(tilesize);
    }
    if (!tmp) {
      if (freeMap)
        free(map);
      return -1;
    }
    TIFFGetField(tif, TIFFTAG_TILELENGTH, &tilelength);
    xtiles = (xsize + tilewidth - 1) / tilewidth;
    ytiles = (ysize + tilelength - 1) / tilelength;
               
    /* printf("%d %d %d %d %d %d\n", tilesize, tilewidth, tilelength, 
       xtiles, ytiles, pixsize); */

    for (yti = 0; yti < ytiles; yti++) {
      for (xti = 0; xti < xtiles; xti++) {
                    
        /* Compute starting and ending Y then X values to use in 
           this tile */
        ystart = ysize - 1 - (tilelength * (yti + 1) - 1);
        yend = ysize - 1 - (tilelength * yti);
        if (ymin > ystart)
          ystart = ymin;
        if (ymax < yend)
          yend = ymax;
        if (ystart > yend)
          continue;

        xstart = xti * tilewidth;
        xend = xstart + tilewidth - 1;
        if (xmin > xstart)
          xstart = xmin;
        if (xmax < xend)
          xend = xmax;
        if (xstart > xend)
          continue;
                    
        /* Read the tile if necessary */
        si = xti + yti * xtiles + plane * xtiles * ytiles;
        nread = TIFFReadEncodedTile(tif, si, tmp, tilesize);
        xcopy = xend + 1 - xstart;
        /* printf("%d %d %d %d\n", nread, si, ystart, yend); */
        for (y = ystart; y <= yend; y++) {
                         
          /* for each y, compute back to row, and get offsets 
             into input and output arrays */
          row = ysize - 1 - y - tilelength * yti;
          ofsin = pixsize * samples *
            (row * tilewidth + xstart - xti * tilewidth) + sampOffset *pixsize;
          ofsout = movesize * 
            ((y - ymin) * xout + xstart - xmin);
          obuf = (unsigned char *)buf + ofsout;
          bdata = tmp + ofsin;
          copyLine(bdata, obuf, xcopy, byte, pixsize, samples, slope, offset,
                 doscale, map);
        }
      }               
    }
  }
  free (tmp);
  if (freeMap)
    free(map);

  return 0;
}

static void copyLine(unsigned char *bdata, unsigned char *obuf, int xout,
                     int byte, int pixsize, int samples, float slope, 
                     float offset, int doscale, unsigned char *map)
{
  b3dUInt16 *usdata;
  b3dFloat *fdata;
  int i, j;

  if (byte) {

    /* Converted data */
    if (pixsize == 1) {
      
      /* Bytes */
      if (samples == 1) {
        if (doscale)
          for (i = 0; i < xout; i++)
            *obuf++ = map[*bdata++];
        else
          memcpy(obuf, bdata, xout);
      } else {
        if (doscale) 
          for (i = 0; i < xout; i++) {
            *obuf++ = map[*bdata];
            bdata += samples;
          }
        else
          for (i = 0; i < xout; i++) {
            *obuf++ = *bdata;
            bdata += samples;
          }
      }
      
    } else if (pixsize == 2) {
      
      /* Integers */
      usdata = (b3dUInt16 *)bdata;
      if (samples == 1) {
        for (i = 0; i < xout; i++)
          *obuf++ = map[*usdata++];
      } else {
        for (i = 0; i < xout; i++) {
          *obuf++ = map[*usdata];
          usdata += samples;
        }
      }
      
    } else {
      
      /* Floats */
      fdata = (b3dFloat *)bdata;
      if (samples == 1) {
        for (i = 0; i < xout; i++)
                *obuf++ = slope * (*fdata++) + offset;
      } else {
        for (i = 0; i < xout; i++) {
          *obuf++ = slope * (*fdata) + offset;
          fdata += samples;
        }
      }
    }
  } else {
    
    /* Non-converted data */
    if (samples == 1) {
      memcpy(obuf, bdata, xout * pixsize);
    } else {
      for (i = 0; i < xout; i++) {
        for (j = 0; j < pixsize; j++)
          *obuf++ = *bdata++;
        bdata += (samples - 1) * pixsize;
      }
    }
  }
}    

int tiffReadSectionByte(ImodImageFile *inFile, char *buf, int inSection)
{ 
  return(ReadSection(inFile, buf, inSection, 1));
}

int tiffReadSection(ImodImageFile *inFile, char *buf, int inSection)
{
  return(ReadSection(inFile, buf, inSection, 0));
}

/*
 * Open new file for writing
 */
int tiffOpenNew(ImodImageFile *inFile)
{
  TIFF *tif = TIFFOpen(inFile->filename, "w");
  if (!tif)
    return IIERR_IO_ERROR;
  inFile->header = (char *)tif;
  inFile->fp = (FILE *)tif;
  inFile->state = IISTATE_READY;
  inFile->cleanUp = tiffDelete; 
  inFile->close = tiffClose;
  return 0;
}

/*
 * Write next section to file with the given compression value; set inverted
 * non-zero if image is already inverted and does not need copying
 */
int tiffWriteSection(ImodImageFile *inFile, void *buf, int compression, 
                     int inverted)
{
  int stripTarget = 8192;
  uint32 rowsPerStrip, lineBytes, stripBytes;
  uint16 bits, samples, photometric, sampleformat;
  double dmin, dmax;
  int lines, linesDone, numStrips, strip, i;
  char *tmp;
  char *inbuf;
  time_t curtime;
  struct tm *tm;
  char datetime[40];

  TIFF *tif = (TIFF *)inFile->header;
  if (!(inFile->format == IIFORMAT_RGB || 
        (inFile->format == IIFORMAT_LUMINANCE && 
         (inFile->type == IITYPE_UBYTE || inFile->type == IITYPE_BYTE ||
          inFile->type == IITYPE_USHORT || inFile->type == IITYPE_SHORT ||
          inFile->type == IITYPE_FLOAT))))
    return(IIERR_NO_SUPPORT);
  if (inFile->state == IISTATE_BUSY)
    TIFFWriteDirectory(tif);
  inFile->state = IISTATE_READY;
  
  TIFFSetField(tif, TIFFTAG_IMAGEWIDTH, inFile->nx);
  TIFFSetField(tif, TIFFTAG_IMAGELENGTH, inFile->ny);
  TIFFSetField(tif, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
  TIFFSetField(tif, TIFFTAG_COMPRESSION, compression);
  TIFFSetField(tif, TIFFTAG_RESOLUTIONUNIT, RESUNIT_NONE);
  if (inFile->format == IIFORMAT_RGB) {
    samples = 3;
    photometric = PHOTOMETRIC_RGB;
    bits = 8;
    TIFFSetField(tif, TIFFTAG_BITSPERSAMPLE, bits, bits, bits);
  } else {
    
    samples = 1;
    photometric = PHOTOMETRIC_MINISWHITE;
    switch (inFile->type) {
    case IITYPE_BYTE:
      bits = 8;
      sampleformat = SAMPLEFORMAT_INT;
      break;
    case IITYPE_UBYTE:
      bits = 8;
      sampleformat = SAMPLEFORMAT_UINT;
      break;
    case IITYPE_SHORT:
      bits = 16;
      sampleformat = SAMPLEFORMAT_INT;
      break;
    case IITYPE_USHORT:
      bits = 16;
      sampleformat = SAMPLEFORMAT_UINT;
      break;
    case IITYPE_FLOAT:
      bits = 32;
      sampleformat = SAMPLEFORMAT_IEEEFP;
      break;
    }

    TIFFSetField(tif, TIFFTAG_BITSPERSAMPLE, bits);
    TIFFSetField(tif, TIFFTAG_SAMPLEFORMAT, sampleformat);
    if (bits > 8 && inFile->amax > inFile->amin) {
      dmin = inFile->amin;
      dmax = inFile->amax;
      TIFFSetField(tif, TIFFTAG_SMINSAMPLEVALUE, dmin);
      TIFFSetField(tif, TIFFTAG_SMAXSAMPLEVALUE, dmax);
    }
  }
  TIFFSetField(tif, TIFFTAG_PHOTOMETRIC, photometric);
  TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, samples);
  
  lineBytes = (samples * bits / 8) * inFile->nx;
  rowsPerStrip = B3DMAX(1, (stripTarget + lineBytes / 2) / lineBytes);

  /* For JPEG compression, rows must be multiple of 8 */
  if (compression == COMPRESSION_JPEG && rowsPerStrip % 8) {
    if (rowsPerStrip < 5 || rowsPerStrip % 8 > 4)
      rowsPerStrip = 8 * ((rowsPerStrip + 7) / 8);
    else
      rowsPerStrip = 8 * (rowsPerStrip / 8);
  }
  TIFFSetField(tif, TIFFTAG_ROWSPERSTRIP, rowsPerStrip);

  time(&curtime);
  tm = localtime(&curtime);
  sprintf(datetime, "%4d:%02d:%02d %02d:%02d:%02d", tm->tm_year + 1900, 
          tm->tm_mon, tm->tm_mday, tm->tm_hour, tm->tm_min, tm->tm_sec);
  TIFFSetField(tif, TIFFTAG_DATETIME, datetime);

  stripBytes = rowsPerStrip * lineBytes;
  numStrips = (inFile->ny + rowsPerStrip - 1) / rowsPerStrip;
  if (!inverted) {
    tmp = (char *)_TIFFmalloc(stripBytes);
    if (!tmp)
      return IIERR_MEMORY_ERR;
  }
  
  linesDone = 0;
  for (strip = 0; strip < numStrips; strip++) {
    lines = B3DMIN(rowsPerStrip, inFile->ny - linesDone);
    if (inverted) {
      tmp = (char *)buf + linesDone * lineBytes;
    } else {

      for (i = 0; i < lines; i++) {
        inbuf = (char *)buf + (inFile->ny - (linesDone + i + 1)) * lineBytes;
        memcpy(tmp + i * lineBytes, inbuf, lineBytes);
      }
    }
    if (TIFFWriteEncodedStrip(tif, strip, tmp, lineBytes * lines) < 0) {
      if (!inverted)
        free(tmp);
      return IIERR_IO_ERROR;
    }
    linesDone += lines;
  }
  inFile->state = IISTATE_BUSY;
  if (!inverted)
    _TIFFfree(tmp);
  return 0;
}
  
    

/*
  $Log$
  Revision 3.16  2009/04/01 03:18:57  mast
  Use routine to prevent buffer overrun in warning handler

  Revision 3.15  2009/03/31 23:47:35  mast
  Added writing function and support for multiple samples as images either
  in contiguous bytes or separate planes and for multiple sizes in file

  Revision 3.14  2008/11/25 16:32:11  mast
  Visual C wants all declarations before executable statements

  Revision 3.13  2008/11/25 16:24:31  mast
  Made stripping of b mode fancier, switched back to non-allocated fmode

  Revision 3.12  2008/11/24 23:59:25  mast
  Changes for using from SerialEM: field-getting

  Revision 3.11  2008/05/23 22:15:22  mast
  Added float support and fixed assignment of min and max when it exists

  Revision 3.10  2007/06/13 17:12:07  sueh
  bug# 1019 In iiTIFFCheck and tiffReopen, setting inFile->sectionSkip to 0.

  Revision 3.9  2006/09/12 15:49:58  mast
  Added include

  Revision 3.8  2006/09/03 22:17:59  mast
  Reorganized and switched to IIERR codes

  Revision 3.7  2006/08/27 23:46:28  mast
  Added color map support
  
  Revision 3.6  2005/05/19 23:51:40  mast
  Made open routine reopen the file if it fails as a tiff
  
  Revision 3.5  2005/02/11 01:42:33  mast
  Warning cleanup: implicit declarations, main return type, parentheses, etc.
  
  Revision 3.4  2004/11/05 18:53:04  mast
  Include local files with quotes, not brackets
  
  Revision 3.3  2004/01/21 00:56:50  mast
  Stopped freeing map from byte_map
  
  Revision 3.2  2004/01/05 17:51:16  mast
  renamed imin/imax to smin/smax or outmin/outmax as appropriate, changed
  unsigned short to b3dUInt16
  
  Revision 3.1  2003/02/27 17:08:23  mast
  Set default upper coordinates to -1 rather than 0.
  
*/
