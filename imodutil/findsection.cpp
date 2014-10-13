/*
 *  findsection.cpp - Find section boundaries for various purposes
 *
 *  Author: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 2014 by the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 *
 *  $Id$
 */
#include <stdlib.h>
#include <math.h>
#include "cppdefs.h"
#include "iiunit.h"
#include "parse_params.h"
#include "hvemtypes.h"
#include "imodel.h"
#include "findsection.h"

#define MAX_DEFAULT_SCALES 12
static int getSlice(int *iz, int *fdata, float *buffer);

/* 
 * Main entry
 */
int main( int argc, char *argv[])
{
  FindSect fs;
  fs.main(argc, argv);
  exit(0);
}

/*
 * Class constructor
 */
FindSect::FindSect()
{
  // Initializations
  mDebugOutput = 0;
  mNumThicknesses = 0;
  mFitPitchSeparately = 0;
  mScanBlockSize = -1;

  // Parameters
  // 1: Minimum # of points for using robust fit to get pitch line on one surface
  mMinForRobustPitch = 6;

  // findColumnMidpoint parameters
  // 3: Number of edge MADN's above edge median that maximum value must be to proceed
  mColMaxEdgeDiffCrit = 2.;
  // 4: Fraction of maximum - edge difference to achieve
  mFracColMaxEdgeDiff = 0.5;
  // 5: Number of edge MADNs above edge to achieve as well
  mCritEdgeMADN = 3.;
  // 6: Number of box medians that need to be above those criteria
  mNumHighInsideCrit = 3;

  // fitColumnBoundaries parameters
  // 7: Number of center MADN's below the center median for inside median to be too low
  mColumnToCenMADNCrit = 5.;
  // 8: Fraction of inside - edge median difference that it must fall toward edge median
  mMaxFalloffFrac = 0.3f;
  // 9, 10: Low and high limits of range of fractions of inside - edge median difference 
  // to fit
  mLowFitFrac = 0.2f;
  mHighFitFrac = 0.8f;
  // 11: Fraction of inside - edge median difference at which to save boundary
  mBoundaryFrac = 0.5f;
  // 12: Fraction of difference at which to estimate extra boundary distance for pitch 
  // output
  mPitchBoundaryFrac = 0.25f;
  // 13: Minimum fraction of boxes in column that must yield boundaries
  mMinFracBoundsInCol = 0.5f;

  // checkBlockThicknesses parameters
  // 14: Criterion fraction of median thickness for considering block too thin
  mTooThinCrit = 0.5f;
  // 15: Drop a boundary if it is this much farther from local mean than other boundary is
  mFartherFromMeanCrit = 2.;
  // 16: Drop a boundary if its difference from the mean is this fraction of median 
  // thickness
  mMeanDiffThickFrac = 0.35f;

  // Robust fitting parameters
  // 17: K-factor for the weighting function
  mKfactor = 4.68;
  // 18: Maximum change in weights for terminatiom
  mMaxChange = 0.02;
  // 19: Maximum change in weights for terminating on an oscillation
  mMaxOscill = 0.05;
  // 20: Maximum iterations
  mMaxIter = 30;
}

/*
 * Class main entry
 */
void FindSect::main( int argc, char *argv[])
{
  char *progname = imodProgName(argv[0]);
  int boxSize[MAX_MBS_SCALES][3];
  int boxStart[MAX_MBS_SCALES][3], startCoord[3], endCoord[3], nxyz[3], mxyz[3];
  int numOptArgs, numNonOptArgs, numTomos, ind, ifFlip, ixyz, ierr, iz, mode, scl;
  int numBinnings, numSpacings, numSizes, tomo, funcData[5], inUnitBase = 3, nxyzTmp[3];
  int edgeExtent[3], centerExtent[3], starts[4], ends[4], ixyCen[2];
  int loop, numStat, ixBox, iyBox, size, edgeBoxes, cenBoxes, botTop, numBound, numGood;
  int yInd, numXblocks, numYblocks, iyBlock, ixBlock, major, minor, maxZeroWgt, numFit;
  int cenStarts[MAX_MBS_SCALES][3], cenEnds[MAX_MBS_SCALES][3], patchLim[2];
  int maxEdgeBoxes, maxCenBoxes, maxColumnBuf, zRange, rem, boxNum, izInside[2], izMid;
  int numIter, wgtColIn, wgtColOut, numSamples, maxPitchFit, samBlockSpace, tomoSeq;
  int maxBlocks, numTomoOpt, numBinEntries, minBoxes, maxPixels;
  float dmin, dmax, dmean, tmin, tmax, tmean, insideMed, lastRatio, ratioDiff, lastDiff;
  float pixelDelta[3], newCell[6], origin[3], newOrigin[3], curTilt[3];
  float ratio, fitConst[2], xx, yy, xySpacing, madnFac, numBoxPerBlock;
  float fitSD[MAX_FIT_COL], fitMean[MAX_FIT_COL], fitSolution[MAX_FIT_COL];
  int defaultScales[MAX_DEFAULT_SCALES] = {1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64};
  float axisRotation, tiltXvert[4], tiltYvert[4], rotMat[3][2];
  int ifReconArea, nxSeries, nySeries, sign, surfaceLim[2], combineLim[2];
  double meanSum;
  float edgeDenMeans[MAX_MBS_SCALES], cenDenMeans[MAX_MBS_SCALES];
  int minRange[3] = {0, 0, 0};
  char *filename;
  char *volRoot = NULL;
  char *pointRoot = NULL;
  char *pitchName = NULL;
  char *surfaceName = NULL;
  Imod *pitchModel;
  int sampleExtent = 0;
  int numPitchPairs = 0;
  int bufferStartInds[MAX_MBS_SCALES];
  MrcHeader *inHeader;
  float *writeSrc, *writeBuf, *smoothBound;

  // values used to set default center and edge areas
  float borderFrac = 0.05f;
  float edgeThickFrac = 0.025f;
  float edgeAreaFrac = 0.5f;
  float cenThickFrac = 0.1f;
  float cenAreaFrac = 0.33f;

  // Parameters
  // 21: Fraction that the difference between distinguishability of center from edge
  // points must improve to adopt a higher scaling for analysis
  float cenEdgeRatioDiffCrit = 0.33f;
  // 22: Threshold weight from robust fit for including a point in the final smoothing fit
  float wgtThresh = 0.2f;
  // 23: Threshold weight from robust fit for counting a point as "good"
  float goodThresh = 0.6f;
  // 24: Fraction for percentile of positions included in auto-combine Z limits
  float combineHighPctl = 0.1f;
  // 25: Percentile of positions less than combineOutsidePix outside the Z limits
  float combineLowPctl = 0.01f;
  // 26: Number of pixels to back off from the low-percentile of positions
  int combineOutsidePix = 20;

  // Fallbacks from    ../manpages/autodoc2man 2 1 findsection
  int numOptions = 23;
  const char *options[] = {
    "tomo:TomogramFile:FNM:", "surface:SurfaceModel:FN:", "pitch:TomoPitchModel:FN:",
    "separate:SeparatePitchLineFits:B:", "samples:NumberOfSamples:I:",
    "extent:SampleExtentInY:I:", "scales:NumberOfDefaultScales:I:",
    "binning:BinningInXYZ:ITM:", "size:SizeOfBoxesInXYZ:ITM:",
    "spacing:SpacingInXYZ:ITM:", "block:BlockSize:I:", "xminmax:XMinAndMax:IP:",
    "yminmax:YMinAndMax:IP:", "zminmax:ZMinAndMax:IP:", "flipped:ThickDimensionIsY:I:",
    "axis:AxisRotationAngle:F:", "tilt:TiltSeriesSizeXY:IP:",
    "edge:EdgeExtentInXYZ:IT:", "center:CenterExtentInXYZ:IT:",
    "control:ControlValue:FPM:", "volume:VolumeRootname:CH:", "point:PointRootname:CH:",
    "debug:DebugOutput:I:"};
  
  // Startup with fallback
  PipReadOrParseOptions(argc, argv, options, numOptions, progname, 
                        2, 1, 1, &numOptArgs, &numNonOptArgs, imodUsageHeader);

  // Get input files and make sure multiple files are same size
  ierr = PipNumberOfEntries("TomogramFile", &numTomoOpt);
  numTomos = numTomoOpt + numNonOptArgs;
  if (!numTomos)
    exitError("No input tomogram file(s) specified");

  for (ind = 0; ind < numTomos; ind++) {
    if (ind < numTomoOpt)
      PipGetString("TomogramFile", &filename);
    else
      PipGetNonOptionArg(ind - numTomoOpt, &filename);
    if (iiuOpen(inUnitBase + ind, filename, "RO"))
      exit(1);
    free(filename);
    iiuRetBasicHead(inUnitBase + ind, nxyzTmp, mxyz, &mode, &dmin, &dmax, &dmean);
    if (ind) {
      if (nxyzTmp[0] != nxyz[0] || nxyzTmp[1] != nxyz[1] || nxyzTmp[2] != nxyz[2])
        exitError("All sample volumes must be the same size in X, Y, and Z");
    } else {
      nxyz[0] = nxyzTmp[0];
      nxyz[1] = nxyzTmp[1];
      nxyz[2] = nxyzTmp[2];
    }
  }

  // Set the index for the thickness axis
  mThickInd = b3dZ;
  ifFlip = -1;
  PipGetInteger("ThickDimensionIsY", &ifFlip);
  if (numTomos > 1 && !ifFlip)
    exitError("Multiple tomograms must have thickness in Y dimension");
  if (ifFlip > 0 || numTomos > 1 || (ifFlip < 0 && nxyz[b3dY] < nxyz[b3dZ]))
    mThickInd = b3dY;
  yInd = 3 - mThickInd;

  // Allow any parameter to be set
  PipNumberOfEntries("ControlValue", &ixyz);
  for (ind = 0; ind < ixyz; ind++) {
    PipGetTwoFloats("ControlValue", &xx, &yy);
    switch (B3DNINT(xx)) {
      SET_CONTROL_INT(1, mMinForRobustPitch);
      SET_CONTROL_FLOAT(3, mColMaxEdgeDiffCrit);
      SET_CONTROL_FLOAT(4, mFracColMaxEdgeDiff);
      SET_CONTROL_FLOAT(5, mCritEdgeMADN);
      SET_CONTROL_INT(6, mNumHighInsideCrit);
      SET_CONTROL_FLOAT(7, mColumnToCenMADNCrit);
      SET_CONTROL_FLOAT(8, mMaxFalloffFrac);
      SET_CONTROL_FLOAT(9, mLowFitFrac);
      SET_CONTROL_FLOAT(10, mHighFitFrac);
      SET_CONTROL_FLOAT(11, mBoundaryFrac);
      SET_CONTROL_FLOAT(12, mPitchBoundaryFrac);
      SET_CONTROL_FLOAT(13, mMinFracBoundsInCol);
      SET_CONTROL_FLOAT(14, mTooThinCrit);
      SET_CONTROL_FLOAT(15, mFartherFromMeanCrit);
      SET_CONTROL_FLOAT(16, mMeanDiffThickFrac);
      SET_CONTROL_FLOAT(17, mKfactor);
      SET_CONTROL_FLOAT(18, mMaxChange);
      SET_CONTROL_FLOAT(19, mMaxOscill);
      SET_CONTROL_INT(20, mMaxIter);
      SET_CONTROL_FLOAT(21, cenEdgeRatioDiffCrit);
      SET_CONTROL_FLOAT(22, wgtThresh);
      SET_CONTROL_FLOAT(23, goodThresh);
      SET_CONTROL_FLOAT(24, combineHighPctl);
      SET_CONTROL_FLOAT(25, combineLowPctl);
      SET_CONTROL_INT(26, combineOutsidePix);
    }
  }
  PipGetInteger("DebugOutput", &mDebugOutput);

  // Get the numbers of binnings, sizes, and spacings
  numBinnings = 1;
  ierr = PipGetInteger("NumberOfDefaultScales", &numBinnings);
  PipNumberOfEntries("BinningInXYZ", &numBinEntries);
  if (!ierr && numBinEntries)
    exitError("You cannot enter both -scalings and -binning");
  if (numBinnings > MAX_DEFAULT_SCALES)
    exitError("There are only %d default scalings available", MAX_DEFAULT_SCALES);
  numBinnings = B3DMAX(numBinnings, numBinEntries);
  if (numBinnings > MAX_MBS_SCALES)
    exitError("Too many binnings for array sizes; only %d allowed", MAX_MBS_SCALES);
  
  PipNumberOfEntries("SpacingInXYZ", &numSpacings);
  if (PipNumberOfEntries("SizeOfBoxesInXYZ", &numSizes) || !numSizes)
    exitError("Size of boxes must be entered for at least the first binning");
  if (numSizes > numBinnings || numSpacings > numBinnings)
    exitError("It makes no sense to enter more sizes or spacings than binnings");

  // Get the entries for each binning.  When there is no size entry, scale it down from
  // the last size entered.  When there is no spacing, scale it down from the last spacing
  // entered.  When there are no spacings at all, assign it the box size divided by 2
  for (scl = 0; scl < numBinnings; scl++) {

    // Binning, read in the 3 values or take the default isotropic binning
    if (scl < numBinEntries) {
      PipGetThreeIntegers("BinningInXYZ", &mBinning[scl][0], &mBinning[scl][1], 
                          &mBinning[scl][2]); 
    } else {
      for (ixyz = 0; ixyz < 3; ixyz++)
        mBinning[scl][ixyz] = defaultScales[scl];
    }

    // Size : read in the size or scale down the last size
    if (scl < numSizes) {
      PipGetThreeIntegers("SizeOfBoxesInXYZ", &boxSize[scl][0], &boxSize[scl][1],
                          &boxSize[scl][2]);
    } else {
      for (ixyz = 0; ixyz < 3; ixyz++) {
        boxSize[scl][ixyz] = B3DNINT(((float)boxSize[numSizes - 1][ixyz] *
                                      mBinning[numSizes - 1][ixyz]) /mBinning[scl][ixyz]);
        B3DCLAMP(boxSize[scl][ixyz], 1, nxyz[ixyz] / mBinning[scl][ixyz]);
      }
      if (mDebugOutput)
        printf("Scale %d  box size %d %d %d\n", scl + 1, boxSize[scl][0], boxSize[scl][1],
               boxSize[scl][2]);
    }
    for (ixyz = 0; ixyz < 3; ixyz++)
      minRange[ixyz] = B3DMAX(minRange[ixyz], boxSize[scl][ixyz] * mBinning[scl][ixyz]);

    // Spacing: read it in, or set up initial one, or scale down the last one
    if (scl < numSpacings) {
      PipGetThreeIntegers("SpacingInXYZ", &mBoxSpacing[scl][0], &mBoxSpacing[scl][1],
                          &mBoxSpacing[scl][2]);
    } else if (!scl) {
      for (ixyz = 0; ixyz < 3; ixyz++)
        mBoxSpacing[scl][ixyz] = B3DMAX(1, boxSize[scl][ixyz] / 2);
      numSpacings = 1;
    } else {  
      for (ixyz = 0; ixyz < 3; ixyz++)
        mBoxSpacing[scl][ixyz] = 
          B3DMAX(1, B3DNINT(((float)mBoxSpacing[numSpacings - 1][ixyz] *
                             mBinning[numSpacings - 1][ixyz]) / mBinning[scl][ixyz]));
      if (mDebugOutput)
        printf("Scale %d  box spacing %d %d %d\n", scl + 1, mBoxSpacing[scl][0],
               mBoxSpacing[scl][1], mBoxSpacing[scl][2]);
    }
  }
  
  // Get output file names and check for validity
  PipGetString("VolumeRootname", &volRoot);
  PipGetString("PointRootname", &pointRoot);
  PipGetString("SurfaceModel", &surfaceName);
  PipGetBoolean("SeparatePitchLineFits", &mFitPitchSeparately);
  PipGetString("TomoPitchModel", &pitchName);
  if (surfaceName && numTomos > 1)
    exitError("You cannot output a surface model with multiple input tomograms");
  ierr = PipGetInteger("NumberOfSamples", &numSamples);
  if (!ierr && numTomos > 1)
    exitError("You cannot specify sampling with multiple input tomograms");
  if (ierr && pitchName && numTomos == 1)
    exitError("You must specify the number of samples for a boundary model from a "
              "single tomogram");
  ierr = PipGetInteger("SampleExtentInY", &sampleExtent);
  if (!ierr && numTomos > 1)
    exitError("You cannot specify sample extent with multiple tomograms"); 
  if (!ierr && !pitchName)
    exitError("You cannot specify sample extent unless outputting a model for "
              "tomopitch"); 

  borderFrac = surfaceName ? 0.025f : 0.05f;

  // Initialize coordinate limits with a border on the other two axes; get limits
  for (ind = 0; ind < 3; ind++) {
    startCoord[ind] = ind == mThickInd ? 0 : B3DNINT(borderFrac * nxyz[ind]);
    if (nxyz[ind] - 2 * startCoord[ind] < minRange[ind])
      startCoord[ind] = (nxyz[ind] - minRange[ind]) / 2;
    endCoord[ind] = nxyz[ind] - 1 - startCoord[ind];
    mBinning[0][ind] = 1;
  }
  PipGetTwoIntegers("XMinAndMax", &startCoord[0], &endCoord[0]);
  PipGetTwoIntegers("YMinAndMax", &startCoord[1], &endCoord[1]);
  PipGetTwoIntegers("ZMinAndMax", &startCoord[2], &endCoord[2]);
  for (ind = 0; ind < 3; ind++)
    if (startCoord[ind] < 0 || endCoord[ind] >= nxyz[ind])
      exitError("Starting or ending %c coordinate outside of range for volume",
                'X' + ind);

  if (mDebugOutput)
    printf("Analyzing X %d %d  Y %d %d  Z %d %d\n", startCoord[0], endCoord[0],
           startCoord[1], endCoord[1], startCoord[2], endCoord[2]);

  // Get rotation angle and stack size and make a boundary of reconstructable region
  nxSeries = nxyz[b3dX];
  nySeries = nxyz[yInd];
  ierr = PipGetTwoIntegers("TiltSeriesSizeXY", &nxSeries, &nySeries);
  ifReconArea = 1 - PipGetFloat("AxisRotationAngle", &axisRotation);
  if (!ierr && !ifReconArea)
    exitError("Axis rotation angle must also be entered with tilt series size");
  if (ifReconArea) {
    tiltXvert[0] = nxyz[b3dX] / 2. - nxSeries / 2.;
    tiltYvert[0] = nxyz[yInd] / 2. - nySeries / 2.;
    tiltXvert[1] = nxyz[b3dX] / 2. + nxSeries / 2.;
    tiltYvert[1] = tiltYvert[0];
    tiltXvert[2] = tiltXvert[1];
    tiltYvert[2] = nxyz[yInd] / 2. + nySeries / 2.;
    tiltXvert[3] = tiltXvert[0];
    tiltYvert[3] = tiltYvert[2];
    rotMat[0][0] = cos(axisRotation * RADIANS_PER_DEGREE);
    rotMat[1][0] = sin(axisRotation * RADIANS_PER_DEGREE);
    rotMat[0][1] = -rotMat[1][0];
    rotMat[1][1] = rotMat[0][0];
    rotMat[2][0] = rotMat[2][1] = 0.;
    for (ind = 0; ind < 4; ind++) 
      xfApply(&rotMat[0][0], nxyz[b3dX] / 2., nxyz[yInd] / 2., tiltXvert[ind],
              tiltYvert[ind], &tiltXvert[ind], &tiltYvert[ind], 2);
  }

  // set up and get the extent of edge and center analysis
  for (ixyz = 0; ixyz < 3; ixyz++) {
    size = endCoord[ixyz] + 1 - startCoord[ixyz];
    if (ixyz == mThickInd) {
      edgeExtent[ixyz] = B3DMAX(1, B3DNINT(edgeThickFrac * size));
      centerExtent[ixyz] = B3DMAX(1, B3DNINT(cenThickFrac * size));
    } else {
      edgeExtent[ixyz] = B3DMAX(1, B3DNINT(edgeAreaFrac * size));
      centerExtent[ixyz] = B3DMAX(1, B3DNINT(cenAreaFrac * size));
    }
  }
  PipGetThreeIntegers("EdgeExtentInXYZ", &edgeExtent[0], &edgeExtent[1], &edgeExtent[2]);
  PipGetThreeIntegers("CenterExtentInXYZ", &centerExtent[0], &centerExtent[1],
                      &centerExtent[2]);
  if (mDebugOutput)
    printf("Extents edge %d %d %d  center %d %d %d\n", edgeExtent[0], edgeExtent[1],
           edgeExtent[2], centerExtent[0], centerExtent[1], centerExtent[2]);

  // Set up the computation
  ierr = multiBinSetup(mBinning, boxSize, mBoxSpacing, numBinnings, startCoord, endCoord,
                       boxStart, mNumBoxes, bufferStartInds, mStatStartInds);
  if (ierr)
    exitError("%s", ierr == 1 ? "Coordinate limits are not usable" : 
              "Box size is too large for binned volume size");

  // Set default scanning size if not entered, then make bigger if # of boxes is limited 
  // in one direction, to give equivalent number of boxes in block
  PipGetInteger("BlockSize", &mScanBlockSize);
  if (mScanBlockSize < 0)
    mScanBlockSize = surfaceName ? 200. : 100.;
  minBoxes = B3DMIN(mNumBoxes[0][b3dX], mNumBoxes[0][yInd]);
  maxPixels = B3DMAX(boxSize[0][b3dX] * mBinning[0][b3dX], 
                     boxSize[0][yInd] * mBinning[0][yInd]);
  numBoxPerBlock = (float)mScanBlockSize / maxPixels;
  if (minBoxes < numBoxPerBlock)
    mScanBlockSize = maxPixels * B3DNINT(numBoxPerBlock * numBoxPerBlock / minBoxes);

  // Get sizes for center and edge samples and make sure array will be big enough
  maxEdgeBoxes = 0;
  maxCenBoxes = 0;
  maxPitchFit = 0;
  maxBlocks = 0;
  for (scl = 0; scl < numBinnings; scl++) {
    edgeBoxes = 2;
    cenBoxes = 1;
    for (ixyz = 0; ixyz < 3; ixyz++) {
      edgeBoxes *= B3DMAX(1, B3DNINT(((float)edgeExtent[ixyz] / mBinning[scl][ixyz] - 
                                      boxSize[scl][ixyz]) / mBoxSpacing[scl][ixyz] + 1.));
      numStat = B3DNINT(((float)centerExtent[ixyz] / mBinning[scl][ixyz] - 
                         boxSize[scl][ixyz]) / mBoxSpacing[scl][ixyz] + 1.);
      B3DCLAMP(numStat, 1, mNumBoxes[scl][ixyz]);
      cenStarts[scl][ixyz] = (mNumBoxes[scl][ixyz] - numStat) / 2;
      cenEnds[scl][ixyz] = cenStarts[scl][ixyz] + numStat - 1;
      cenBoxes *= numStat;
    }
    invertYifFlipped(cenStarts[scl][1], cenEnds[scl][1], &mNumBoxes[scl][0]);
    maxEdgeBoxes = B3DMAX(maxEdgeBoxes, edgeBoxes);
    maxCenBoxes = B3DMAX(maxCenBoxes, cenBoxes);
    if (mDebugOutput)
      printf("%d cen s-e x %d %d  y %d %d  z %d %d\n", scl, cenStarts[scl][0],
             cenEnds[scl][0], cenStarts[scl][1], cenEnds[scl][1], cenStarts[scl][2],
             cenEnds[scl][2]);

    setupBlocks(mNumBoxes[scl][b3dX], scl, b3dX);
    setupBlocks(mNumBoxes[scl][yInd], scl, yInd);
    maxBlocks = B3DMAX(maxBlocks, (mNumBlocks[scl][b3dX] + 2) * 
                       (mNumBlocks[scl][yInd] + 2));

    // fitColumnBoundaries needs space for a block of values on each surface and for
    // two fitting arrays that could be an unknown fraction of the Z extent
    maxColumnBuf = B3DMAX(maxColumnBuf, 2 * mNumBoxes[scl][mThickInd] + 4 *
                          (mNumInBlock[scl][b3dX] + 3) * (mNumInBlock[scl][yInd] + 3));

    // Make sure there is enough space for fitting lines for pitch model
    if (pitchName) {
      if (numTomos > 1) {
        size = mNumBlocks[scl][yInd];
      } else if (!sampleExtent) {
        size = 1;
      } else {
        size = mBinning[scl][yInd] * (mNumInBlock[scl][yInd] * mBoxSpacing[scl][yInd] +
                                      boxSize[scl][yInd]);
        size = B3DMAX(1, B3DNINT((float)sampleExtent / size));
      }
      maxPitchFit = B3DMAX(maxPitchFit, size * mNumBlocks[scl][b3dX]);
    }
  }

  size = b3dIMax(6, bufferStartInds[numBinnings], 2 * maxEdgeBoxes, 2 * maxCenBoxes, 
                 B3DMAX(maxCenBoxes, maxColumnBuf) + maxColumnBuf, 3 * maxPitchFit, 
                 maxBlocks);
  mBuffer = B3DMALLOC(float, size);
  mMeans = B3DMALLOC(float, mStatStartInds[numBinnings]);
  mSDs = B3DMALLOC(float, mStatStartInds[numBinnings]);
  mThicknesses = B3DMALLOC(float, maxBlocks);
  if (!mBuffer || !mMeans || !mSDs || !mThicknesses)
    exitError("Allocating arrays for image data and statistics");

  // Set up a tomopitch model
  if (pitchName) {
    pitchModel = imodNew();
    if (!pitchModel || imodNewObject(pitchModel))
      exitError("Creating model for tomopitch output");
    pitchModel->obj[0].extra[IOBJ_EX_PNT_LIMIT] = 2;
    pitchModel->obj[0].flags = IMOD_OBJFLAG_OPEN | IMOD_OBJFLAG_PLANAR;
    if (numTomos > 1)
      pitchModel->obj[0].flags |= IMOD_OBJFLAG_TIME;
    pitchModel->xmax = nxyz[0];
    pitchModel->ymax = nxyz[1];
    pitchModel->zmax = nxyz[2];
  }

  // START LOOP ON TOMOGRAMS
  mNumExtraSum = 0;
  mExtraPitchSum = 0.;
  for (tomoSeq = 0; tomoSeq < numTomos; tomoSeq++) {

    // Do tomograms from center outward
    tomo = numTomos / 2;
    if (tomoSeq % 2)
      tomo -= (tomoSeq + 1) / 2;
    else
      tomo += tomoSeq / 2;
    if (numTomos > 1)
      printf("\nAnalyzing tomogram # %d\n", tomo + 1);
    printf("              center                   edge           distinct-\n"
           "scale  boxes  median  MADN     boxes  median  MADN      ness\n");

    funcData[0] = inUnitBase + tomo;
    funcData[1] = startCoord[0];
    funcData[2] = endCoord[0];
    funcData[3] = startCoord[1];
    funcData[4] = endCoord[1];

    ierr = multiBinStats(mBinning, boxSize, mBoxSpacing, numBinnings, startCoord,
                         endCoord, boxStart, mNumBoxes, bufferStartInds, mStatStartInds,
                         mBuffer, mMeans, mSDs, funcData, getSlice);
    if (ierr)
      exitError("Reading data from file");

    // Write data
    if (volRoot) {
      writeSrc = mMeans;
      iiuRetDelta(inUnitBase + tomo, pixelDelta);
      iiuRetOrigin(inUnitBase + tomo, &origin[0], &origin[1], &origin[2]);
      iiuRetTilt(inUnitBase + tomo, curTilt);
      for (loop = 0; loop < 2; loop++) {
        for (scl = 0; scl < numBinnings; scl++) {

          // Adjust origin and pixel size
          for (ixyz = 0; ixyz < 3; ixyz++) {
            newCell[ixyz] = mNumBoxes[scl][ixyz] * pixelDelta[ixyz] * 
              mBinning[scl][ixyz] * mBoxSpacing[scl][ixyz]; 
            newOrigin[ixyz] = origin[ixyz] - pixelDelta[ixyz] * 
              (startCoord[ixyz] + boxStart[scl][ixyz] * mBinning[scl][ixyz]);
            newCell[ixyz + 3] = 90.;
          }
          sprintf((char *)mBuffer, "%s%d-scale%d.%s", volRoot, tomo, scl,
                  loop ? "SDs" : "means");
          iiuOpen(1, (char *)mBuffer, "NEW");
          iiuCreateHeader(1, &mNumBoxes[scl][0], &mNumBoxes[scl][0], 2, NULL, 0);
          iiuAltCell(1, newCell);
          iiuAltOrigin(1, newOrigin[0], newOrigin[1], newOrigin[2]);
          iiuAltTilt(1, curTilt);
          dmin = 1.e37;
          dmax = -dmin;
          dmean = 0;
          for (iz = 0; iz < mNumBoxes[scl][b3dZ]; iz++) {
            writeBuf = writeSrc + mStatStartInds[scl] + iz *  mNumBoxes[scl][b3dY] * 
              mNumBoxes[scl][b3dX];
            iiuWriteSection(1, (char *)writeBuf);
            arrayMinMaxMean(writeBuf, mNumBoxes[scl][b3dX], mNumBoxes[scl][b3dY], 0,
                            mNumBoxes[scl][b3dX] - 1, 0 , mNumBoxes[scl][b3dY] - 1, &tmin,
                            &tmax, &tmean);
            dmin = B3DMIN(dmin, tmin);
            dmax = B3DMAX(dmax, tmax);
            dmean += tmean / mNumBoxes[scl][b3dZ];
          }
          iiuWriteHeaderStr(1, "FINDSECTION", 0, dmin, dmax, dmean);
          iiuClose(1);
        }          
        writeSrc = mSDs;
      } 
    }

    // Evaluate statistics of edge first
    for (scl = 0; scl < numBinnings; scl++) {
      for (ixyz = 0; ixyz < 3; ixyz++) {
        numStat = B3DNINT(((float)edgeExtent[ixyz] / mBinning[scl][ixyz] - 
                           boxSize[scl][ixyz]) / mBoxSpacing[scl][ixyz] + 1.);
        B3DCLAMP(numStat, 1, mNumBoxes[scl][ixyz]);

        // For thickness, get inclusive limits of excluded region
        if (ixyz == mThickInd)
          numStat = mNumBoxes[scl][ixyz] - numStat;
        starts[ixyz] = (mNumBoxes[scl][ixyz] - numStat) / 2;
        ends[ixyz] = starts[ixyz] + numStat - 1;
      }
      invertYifFlipped(starts[1], ends[1], &mNumBoxes[scl][0]);

      // Set the limits in the thickness dimension, save excluded region
      starts[3] = starts[mThickInd];
      ends[3] = ends[mThickInd];
      starts[mThickInd] = 0;
      ends[mThickInd] = starts[3] - 1;
      numStat = 0;
      meanSum = 0.;
      if (mDebugOutput)
        printf("edge boxes %d %d %d %d %d %d %d %d\n", starts[0], ends[0], starts[1], 
               ends[1], starts[2], ends[2], starts[3], ends[3]);
      addBoxesToSample(starts[0], ends[0], starts[1], ends[1], starts[2], ends[2], scl,
                       numStat, meanSum);
      starts[mThickInd] = ends[3] + 1;
      ends[mThickInd] = mNumBoxes[scl][mThickInd] - 1;
      if (mDebugOutput)
        printf("AND  boxes %d %d %d %d %d %d\n", starts[0], ends[0], starts[1], ends[1],
               starts[2], ends[2]);
      addBoxesToSample(starts[0], ends[0], starts[1], ends[1], starts[2], ends[2], scl,
                       numStat, meanSum);
      edgeDenMeans[scl] = meanSum / numStat;
        
      rsFastMedian(mBuffer, numStat, &mBuffer[numStat], &mEdgeMedians[scl]);
      rsFastMADN(mBuffer, numStat, mEdgeMedians[scl], &mBuffer[numStat], 
                 &mEdgeMADNs[scl]);
      /*printf("cen: %6.1f %5.1f  %6.1f %5.1f   edge: %6.1f %5.1f  %6.1f %5.1f", 
             edgCenMean[0], edgCenSD[0], medians[scl][0], MADNs[scl][0],
             edgCenMean[1], edgCenSD[1], medians[scl][1], MADNs[scl][1]); */
      if (mDebugOutput)
        printf("edge: %d  %6.1f %5.1f\n", numStat, mEdgeMedians[scl], mEdgeMADNs[scl]);
    /*  for (loop = 0; loop < 4; loop++)
        printf("  %.3f",(medians[scl][0] - (1. + 0.5 * loop) * MADNs[scl][0] - 
        medians[scl][1]) / MADNs[scl][1]);*/

      //printf("   %.3f", (edgCenMean[0] - 2. * edgCenSD[0] - edgCenMean[1])/edgCenSD[1]);
    }
    if (mDebugOutput)
      printf("\n");

    for (scl = 0; scl < numBinnings; scl++) {
      
      // Get the block size and number of blocks for center sampling
      setupBlocks(cenEnds[scl][b3dX] + 1 - cenStarts[scl][b3dX], scl, b3dX);
      setupBlocks(cenEnds[scl][yInd] + 1 - cenStarts[scl][yInd], scl, yInd);
      zRange = cenEnds[scl][mThickInd] + 1 - cenStarts[scl][mThickInd];

      // Loop on the blocks
      numStat = 0;
      meanSum = 0.;
      /*printf("%d: %d x blocks of %d+   %d y blocks of %d+\n", scl,mNumBlocks[scl][b3dX],
        mNumInBlock[scl][0], mNumBlocks[scl][yInd], mNumInBlock[scl][yInd]);*/
      for (iyBox = 0; iyBox < mNumBlocks[scl][yInd]; iyBox++) {
        for (ixBox = 0; ixBox < mNumBlocks[scl][b3dX]; ixBox++) {

          // Get the range of boxes in the block
          for (ixyz = 0; ixyz <= yInd; ixyz += yInd) {
            boxNum = ixyz ? iyBox : ixBox;
            rem = (cenEnds[scl][ixyz] + 1 - cenStarts[scl][ixyz]) % mNumBlocks[scl][ixyz];
            starts[ixyz] = cenStarts[scl][ixyz] + boxNum * mNumInBlock[scl][ixyz] + 
              (boxNum <= rem ? boxNum : rem);
            ends[ixyz] = starts[ixyz] + mNumInBlock[scl][ixyz] + (boxNum < rem ? 0 : -1);
          }
          /*printf("block %d %d  x %d %d y %d %d\n", ixBox, iyBox, starts[0], ends[0], 
            starts[yInd], ends[yInd]);  */
          
          // Get the midpoint and ranges inside and add boxes
          if (!findColumnMidpoint(starts[0], ends[0], starts[yInd], ends[yInd], scl,
                                  &mBuffer[maxCenBoxes], izInside, izMid, insideMed)) {
            starts[mThickInd] = B3DMAX(izInside[0], izMid - zRange / 2);
            ends[mThickInd] = B3DMIN(izInside[1], starts[mThickInd] + zRange - 1);
            addBoxesToSample(starts[0], ends[0], starts[1], ends[1], starts[2], ends[2], 
                             scl, numStat, meanSum);
          }
        }
      }
      if (numStat < 10)
        exitError("Too few boxes in center sample where boundaries of section could "
                  "be detected");
      cenDenMeans[scl] = meanSum / numStat;

      rsFastMedian(mBuffer, numStat, &mBuffer[numStat], &mCenMedians[scl]);
      rsFastMADN(mBuffer, numStat, mCenMedians[scl], &mBuffer[numStat], 
                 &mCenMADNs[scl]);
      madnFac = (mCenMedians[0] - mEdgeMedians[0]) / mCenMADNs[0];
      B3DCLAMP(madnFac, 1., 2.);
      printf(" %2d  %7.0f  %6.1f %5.1f   %7.0f  %6.1f %5.1f  %8.2f\n", scl,
             cenDenMeans[scl], mCenMedians[scl], mCenMADNs[scl], edgeDenMeans[scl], 
             mEdgeMedians[scl], mEdgeMADNs[scl], 
             (mCenMedians[scl] - madnFac * mCenMADNs[scl] - mEdgeMedians[scl]) / 
             mEdgeMADNs[scl]);
    }
    
    // Next, pick the best binning
    mBestScale = 0;
    lastRatio = 0.;
    ratioDiff = 0.;
    for (scl = 0; scl < numBinnings; scl++) {
      ratio = (mCenMedians[scl] - madnFac * mCenMADNs[scl] - mEdgeMedians[scl]) / 
        mEdgeMADNs[scl];
      if (scl) {

        // Compute ratio change per step since the last best scale
        ratioDiff = (ratio - lastRatio) / (scl - mBestScale);
        if (ratioDiff < cenEdgeRatioDiffCrit * lastDiff)
          continue;
        mBestScale = scl;
      } 
      lastRatio = ratio;
      lastDiff = ratioDiff;
    }
    if (numBinnings > 1)
      printf("Selected scaling # %d as the best one for analysis\n", mBestScale + 1);
    
    // Now find a boundary position in each block
    // Set up the blocking from scratch and loop on the blocks
    scl = mBestScale;
    setupBlocks(mNumBoxes[scl][b3dX], scl, b3dX);
    setupBlocks(mNumBoxes[scl][yInd], scl, yInd);
    numXblocks = mNumBlocks[scl][b3dX];
    numYblocks = mNumBlocks[scl][yInd];
    mBoundaries = B3DMALLOC(float, 2 * numXblocks * numYblocks);
    mBlockCenters = B3DMALLOC(float, 2 * numXblocks * numYblocks);
    smoothBound = B3DMALLOC(float, 2 * numXblocks * numYblocks);
    if (!mBoundaries || !mBlockCenters || !smoothBound)
      exitError("Allocating array for boundary positions");

    for (iyBlock = 0; iyBlock < numYblocks; iyBlock++) {
      for (ixBlock = 0; ixBlock < numXblocks; ixBlock++) {
        ind = 2 * (iyBlock * numXblocks + ixBlock);

        // Get the range of boxes in the block
        for (ixyz = 0; ixyz <= yInd; ixyz += yInd) {
          boxNum = ixyz ? iyBlock : ixBlock;
          rem = mNumBoxes[scl][ixyz] % mNumBlocks[scl][ixyz];
          starts[ixyz] = boxNum * mNumInBlock[scl][ixyz] + (boxNum <= rem ? boxNum : rem);
          ends[ixyz] = starts[ixyz] + mNumInBlock[scl][ixyz] + (boxNum < rem ? 0 : -1);

          // And get the unbinned center coordinate of the block
          mBlockCenters[ind + B3DMIN(1, ixyz)] = startCoord[ixyz] + mBinning[scl][ixyz] *
            (((starts[ixyz] + ends[ixyz]) * mBoxSpacing[scl][ixyz] + boxSize[scl][ixyz]) /
             2. + boxStart[scl][ixyz]);
        }
        
        // Get the boundaries and scale to unbinned coordinates there too
        if (ifReconArea && !InsideContour(tiltXvert, tiltYvert, 4, mBlockCenters[ind], 
                                          mBlockCenters[ind + 1])) {
          mBoundaries[ind] = mBoundaries[ind + 1] = -1.;
        } else {
          fitColumnBoundaries(starts[0], ends[0], starts[yInd], ends[yInd],
                              &mBoundaries[ind]);
        }
        for (loop = 0; loop < 2; loop++)
          if (mBoundaries[ind + loop] > 0.)
            mBoundaries[ind + loop] = startCoord[mThickInd] + mBinning[scl][mThickInd] *
              (mBoundaries[ind + loop] * mBoxSpacing[scl][mThickInd] + 
               0.5 * boxSize[scl][mThickInd] + boxStart[scl][mThickInd]);
      }
    }
    if (mNumExtraSum)
      mExtraForPitch = mExtraPitchSum / mNumExtraSum;
    if (mDebugOutput)
      printf("Mean extra distance for tomopitch lines = %.1f\n", mExtraForPitch);

    // Eliminate points that give a bad thickness if possible
    checkBlockThicknesses();

    inHeader = iiuMrcHeader(inUnitBase + tomo, "findsection", 1, 0);
    if (pointRoot) {
      sprintf((char *)mBuffer, "%s%d-colbound.mod", pointRoot, tomo);
      dumpPointModel(mBoundaries, numXblocks * numYblocks, (char *)mBuffer, inHeader);
    }

    // Now for fitting/smoothing, loop on every block, both surfaces
    for (botTop = 0; botTop < 2; botTop++) {
      for (iyBlock = 0; iyBlock < numYblocks; iyBlock++) {
        for (ixBlock = 0; ixBlock < numXblocks; ixBlock++) {
          ixyCen[0] = ixBlock;
          ixyCen[1] = iyBlock;
          ind = 2 * (iyBlock * numXblocks + ixBlock) + botTop;
          smoothBound[ind] = -1.;
          if (mBoundaries[ind] < 0.)
            continue;

          // Try for a square 5 x 5 region first, but if there are not 2 good rows of
          // data at least, drop back to 7 x 2 region
          getFittingRegion(ixyCen, 5, 5, botTop, starts, ends, numBound, xySpacing);
          if (mGoodRowCol[0] < 2 || mGoodRowCol[1] < 2)
            getFittingRegion(ixyCen, 7, 2, botTop, starts, ends, numBound, xySpacing);

          // Set up major and minor axis 
          wgtColIn = -1;
          major = 0;
          if (ends[0] - starts[0] < ends[1] - starts[1])
            major = 1;
          minor = 1 - major;

          // If there are enough points for robust fit, set the variable list
          if (numBound >= 6) {
            buildVariableList(major, minor, numBound, numBound);
            maxZeroWgt = numBound / 6;

            // Load matrix and do the fit; if it works, record weight column
            loadFittingMatrix(ixyCen, starts, ends, botTop, xySpacing, 0., -1, -1,
                              numFit);
            ierr = robustRegress(&mFitMat[0][0], MAX_FIT_DATA, 0, mNumVars, numFit, 1, 
                                 fitSolution, MAX_FIT_COL, &fitConst[0], fitMean, fitSD, 
                                 mFitWork, mKfactor, &numIter, mMaxIter, maxZeroWgt, 
                                 mMaxChange, mMaxOscill);
            if (mDebugOutput > 1)
              printf("block %d %d bt %d fr %d %d %d %d err %d iter %d  c %.1f\n", ixBlock,
                     iyBlock, botTop, starts[0], ends[0], starts[1], ends[1],
                     ierr, numIter, fitConst[0]);
            if (!ierr)
              wgtColIn = mNumVars + 1;
          }

          numFit = numGood = numBound;
          
          if (wgtColIn > 0) {
            
            // If there are weights, count up the number of points still included in
            // numBound, and # with weights above a higher threshold in numGood
            numBound = numGood = 0;
            for (ixyz = 0; ixyz < numFit; ixyz++) {
              if (mFitMat[wgtColIn][ixyz] >= wgtThresh)
                numBound++;
              if (mFitMat[wgtColIn][ixyz] >= goodThresh)
                numGood++;
            }
            if (mDebugOutput > 1)
              printf("total %d  retain %d  good %d\n", numFit, numBound, numGood);
          }

          // Set up variables for final fit with possible weighting, with requirements on
          // number to fit as well as number with higher weighting; load and fit
          smoothBound[ind] = mBoundaries[ind];
          if (numBound >= 5) {
            buildVariableList(major, minor, numBound, numGood);
            wgtColOut = mNumVars + 1;
            loadFittingMatrix(ixyCen, starts, ends, botTop, xySpacing, wgtThresh,
                              wgtColIn, wgtColOut, numFit);
            ierr = multRegress(&mFitMat[0][0], MAX_FIT_DATA, 0, mNumVars, numFit, 1,
                               wgtColIn, fitSolution, MAX_FIT_COL, &fitConst[0], fitMean,
                               fitSD, mFitWork);
            if (mDebugOutput > 1)
              printf("block %d %d bt %d fr %d %d %d %d err %d xm %.1f %.1f %.1f b %f %f "
                     "c %.1f\n", ixBlock, iyBlock, botTop, starts[0], ends[0], starts[1],
                     ends[1], ierr, fitMean[0], fitMean[1], fitMean[2], fitSolution[0], 
                     fitSolution[1], fitConst[0]);
            if (!ierr)
              smoothBound[ind] = fitConst[0];
          }

        }
      }
    }

    if (pointRoot) {
      sprintf((char *)mBuffer, "%s%d-smooth.mod", pointRoot, tomo);
      dumpPointModel(smoothBound, numXblocks * numYblocks, (char *)mBuffer, inHeader);
    }

    if (surfaceName)
      makeSurfaceModel(smoothBound, surfaceName, inHeader, nxyz);

    // For outputting tomopitch model, first assign the header for first tomo
    if (pitchName) {
      if (!tomo)
        imodSetRefImage(pitchModel, inHeader);

      // Add to model for sample tomograms
      if (numTomos > 1) {
        if (!addToPitchModel(pitchModel, smoothBound, 0, numYblocks - 1, tomo + 1))
          numPitchPairs++;
      } else {

        // Otherwise figure out number of blocks in sample extent
        if (sampleExtent) {
          size = (mBlockCenters[2 * numXblocks * (numYblocks - 1) + 1] - mBlockCenters[1])
            / (float)B3DMAX(numYblocks - 1, 1);
          size = B3DMAX(1, B3DNINT((float)sampleExtent / size));
        } else {
          size = 1;
        }

        // Limit number of samples if needed
        if (numYblocks / size < numSamples) {
          numSamples = numYblocks / size;
          printf("WARNING: With a block size of %d, there can be only %d samples\n",
                 mScanBlockSize, numSamples);
        }

        // Indent by one block if there is enough extra stuff
        ind = 0;
        if (size * numSamples < (numYblocks - 2 * size) / 2)
          ind = size;

        // Get spacing between samples and # requiring spacing + 1
        samBlockSpace = (numYblocks - 2 * ind - size) / B3DMAX(1, numSamples - 1);
        rem = (numYblocks - 2 * ind - size) % B3DMAX(1, numSamples - 1);

        // Loop on the samples
        for (loop = 0; loop < numSamples; loop++) {
          if (!addToPitchModel(pitchModel, smoothBound, ind, ind + size - 1, 0))
            numPitchPairs++;
          ind += samBlockSpace + (loop < rem ? 1 : 0);
        }
      }
    }

    iiuClose(inUnitBase + tomo);
  }

  // Finish tomopitch model
  if (pitchName) {
    FILE *fp = fopen(pitchName, "wb");
    if (!fp || imodWrite(pitchModel, fp))
      exitError("Opening or writing model %s", pitchName);
    fclose(fp);
    if (numPitchPairs < 2)
      exitError("Only one pair of lines was placed in the model for tomopitch");
    if (numPitchPairs < numTomos || (numTomos == 1 && numPitchPairs < numSamples))
      printf("WARNING: %s - A pair of lines was found for only %d of the %d samples %s\n",
             progname, numPitchPairs, numTomos > 1 ? numTomos : numSamples,
             numTomos > 1 ? "tomograms" : "positions");
  }
  
  // For single tomogram, get the median on each surface and output Z limits
  if (numTomos == 1) {
    for (loop = 0; loop < 2; loop++) {
      patchLim[loop] = -2;
      numStat = 0;
      sign = 2 * loop - 1;
      for (ind = 0; ind < numXblocks * numYblocks; ind++) {
        if (smoothBound[2 * ind + loop] >= 0.)
          mBuffer[numStat++] = smoothBound[2 * ind + loop];
      }
      if (numStat > 3) {
        rsSortFloats(mBuffer, numStat);
        rsMedianOfSorted(mBuffer, numStat, &insideMed);
        
        // Determine the integer slice that the median plus extra amount occurs in
        patchLim[loop] = B3DNINT(insideMed + sign * mExtraForPitch - 0.5);
        B3DCLAMP(patchLim[loop], 0, nxyz[mThickInd] - 1);

        // Determine the same for absolute limits of surfaces
        if (loop)
          surfaceLim[loop] = B3DNINT(mBuffer[numStat - 1] + mExtraForPitch - 0.5);
        else
          surfaceLim[loop] = B3DNINT(mBuffer[0] - mExtraForPitch - 0.5);
        B3DCLAMP(surfaceLim[loop], 0, nxyz[mThickInd] - 1);

        // Do combination of the more extreme of a "high" percentile limit, and a
        // very low percentile limit with some number pixels allowed to be outside
        ratio = combineLowPctl * (1 - loop) + loop * (1. - combineLowPctl);
        rsPercentileOfSorted(mBuffer, numStat, ratio, &insideMed);
        ratio = combineHighPctl * (1 - loop) + loop * (1. - combineHighPctl);
        rsPercentileOfSorted(mBuffer, numStat, ratio, &yy);
        if (loop)
          combineLim[loop] = B3DNINT(B3DMAX(yy, insideMed - combineOutsidePix) +
                                     mExtraForPitch - 0.5);
        else
          combineLim[loop] = B3DNINT(B3DMIN(yy, insideMed + combineOutsidePix) -
                                     mExtraForPitch - 0.5);
      }
    }

    // Output results if any
    if (patchLim[0] >= 0  && patchLim[1] >= 0) {
      invertYifFlipped(patchLim[0], patchLim[1], nxyz);
      printf("Median Z values of surfaces, numbered from 1, are: %d  %d\n", 
             patchLim[0] + 1, patchLim[1] + 1);
      invertYifFlipped(combineLim[0], combineLim[1], nxyz);
      printf("Z limits for autopatchfit combine, numbered from 1, are: %d  %d\n", 
             combineLim[0] + 1, combineLim[1] + 1);
      invertYifFlipped(surfaceLim[0], surfaceLim[1], nxyz);
      printf("Absolute limits of surfaces, numbered from 1, are: %d  %d\n", 
             surfaceLim[0] + 1, surfaceLim[1] + 1);
    } else
      printf("Too few surface points to determine summary Z values for surface\n");
  }

  exit(0);
}

/*
 * Write out a model of points, either the raw column boundaries or the smoothed 
 * boundaries
 */
void FindSect::dumpPointModel(float *boundaries, int numPts,
                              const char *filename, MrcHeader *inHeader)
{
  Imod *imod = imodNew();
  if (!imod || imodNewObject(imod) || imodNewContour(imod) || imodNewObject(imod) || 
      imodNewContour(imod))
    exitError("Setting up model");
  bool flipped = mThickInd == 1;
  int pt, ind;
  imodSetRefImage(imod, inHeader);
  for (pt = 0; pt < numPts; pt++) {
    for (ind = 0; ind < 2; ind++)
      if (boundaries[2 * pt + ind] >= 0 && !imodPointAppendXYZ
          (imod->obj[ind].cont, mBlockCenters[2 * pt],
           flipped ? boundaries[2 * pt + ind] : mBlockCenters[2 * pt + 1],
           flipped ? mBlockCenters[2 * pt + 1] : boundaries[2 * pt + ind]))
        exitError("Adding point to model");
  }
  imod->obj[0].flags = IMOD_OBJFLAG_SCAT | IMOD_OBJFLAG_PNT_ON_SEC;
  imod->obj[0].pdrawsize = 4;
  imod->obj[1].flags = IMOD_OBJFLAG_SCAT | IMOD_OBJFLAG_PNT_ON_SEC;
  imod->obj[1].pdrawsize = 4;
  FILE *fp = fopen(filename, "wb");
  if (!fp || imodWrite(imod, fp))
    exitError("Opening or writing model %s", filename);
  fclose(fp);
  imodDelete(imod);
}

/*
 * Put out a model of the smoothed boundaries as open contours along the surface,
 * usable for flattenwarp
 */
void FindSect::makeSurfaceModel(float *boundaries, const char *filename,
                                MrcHeader *inHeader, int *nxyz)
{
  Imod *imod = imodNew();
  Icont *cont;
  if (!imod || imodNewObject(imod))
    exitError("Setting up model");
  bool flipped = mThickInd == 1;
  int numXblocks = mNumBlocks[mBestScale][b3dX];
  int numYblocks = mNumBlocks[mBestScale][3 - mThickInd];
  int pt, ind, iy, ix;
  float zRound;
  imodSetRefImage(imod, inHeader);
  imod->xmax = nxyz[0];
  imod->ymax = nxyz[1];
  imod->zmax = nxyz[2];
  
  // Loop on levels in Y, and on bottom and top surfaces
  for (iy = 0; iy < numYblocks; iy++) {
    for (ind = 0; ind < 2; ind++) {
      cont = NULL;

      // Loop across
      for (ix = 0; ix < numXblocks; ix++) {
        pt = iy * numXblocks + ix;
        if (boundaries[2 * pt + ind] >= 0) {

          // Add contour only when the first point is found
          if (!cont) {
            if (imodNewContour(imod) || !(cont = imodContourGet(imod)))
              exitError("Adding contour to model");
          }
          zRound = B3DNINT(mBlockCenters[2 * pt + 1]);
          if (!imodPointAppendXYZ(cont, mBlockCenters[2 * pt],
                                  flipped ? boundaries[2 * pt + ind] : zRound,
                                  flipped ? zRound : boundaries[2 * pt + ind]))
            exitError("Adding point to model");
        }
      }
    }
  }
  imod->obj[0].flags = IMOD_OBJFLAG_OPEN;
  imod->obj[0].symsize = 7;
  imod->obj[0].symbol = IOBJ_SYM_CIRCLE;
  
  FILE *fp = fopen(filename, "wb");
  if (!fp || imodWrite(imod, fp))
    exitError("Opening or writing model %s", filename);
  fclose(fp);
  imodDelete(imod);
}

/*
 * Fit two lines separately or together with the same slope to the points on bottom and
 * tpoi surfaces in the given range of blocks in Y.  Use robust fitting if there are
 * enough points.  Returns 1 if there are inadequate points or a fitting error.
 */
int FindSect::addToPitchModel(Imod *imod, float *boundaries, int yStart, int yEnd, 
                               int time)
{
  Icont *cont;
  float fitSD[MAX_FIT_COL], fitMean[MAX_FIT_COL], fitSolution[MAX_FIT_COL];
  bool flipped = mThickInd == 1;
  int numXblocks = mNumBlocks[mBestScale][b3dX];
  int pt, ind, iy, ix, err, numPts = 0, numBot, numFit, maxZeroWgt, indFit, numIter;
  int maxPts = 2 * numXblocks * (yEnd + 1 - yStart);
  float zRound, yVal, yShift[2];
  float *xfit = mBuffer;
  float *yfit = &mBuffer[maxPts];
  float *zfit = &mBuffer[2 * maxPts];
  float slopes[2], intercepts[2], xmin[2], xmax[2];
  int minOnSide = mFitPitchSeparately ? 2 : 3;
  
  // Loop on bottom and top surfaces
  for (ind = 0; ind < 2; ind++) {
    xmin[ind] = 1.e37;
    xmax[ind] = -1.e37;
    numBot = numPts;

    // Load the points into arrays
    for (iy = yStart; iy <= yEnd; iy++) {
      for (ix = 0; ix < numXblocks; ix++) {
        pt = iy * numXblocks + ix;
        if (boundaries[2 * pt + ind] >= 0) {
          xfit[numPts] = mBlockCenters[2 * pt];
          xmin[ind] = B3DMIN(xmin[ind], xfit[numPts]);
          xmax[ind] = B3DMAX(xmax[ind], xfit[numPts]);
          yfit[numPts] = boundaries[2 * pt + ind];
          zfit[numPts] = ind;
          numPts++;
        }
      }
    }
  }
  if ((mFitPitchSeparately && numPts < 5) || numBot < minOnSide || 
      numPts - numBot < minOnSide)
    return 1;

  // Do fit to each surface separately or to both at once, set up slope/intercept for
  // top in the latter case
  err = 1;
  if (mFitPitchSeparately) {
    numFit = numBot;
    indFit = 0;
    for (ind = 0; ind < 2; ind++) {

      // Use robust fit if there are enough points
      if (numFit >= mMinForRobustPitch && numFit <= MAX_FIT_DATA) {
        for (ix = 0; ix < numFit; ix++) {
          mFitMat[0][ix] = xfit[ix + indFit];
          mFitMat[1][ix] = yfit[ix + indFit];
        }
        maxZeroWgt = numFit / 6;
        err = robustRegress(&mFitMat[0][0], MAX_FIT_DATA, 0, 1, numFit, 1, 
                            fitSolution, MAX_FIT_COL, &intercepts[ind], fitMean, fitSD, 
                            mFitWork, mKfactor, &numIter, mMaxIter, maxZeroWgt, 
                            mMaxChange, mMaxOscill);
        slopes[ind] = fitSolution[0];
      } 

      // Otherwise, or if there was an error in the robust fit, do standard fit
      if (err) {
        lsFit(&xfit[indFit], &yfit[indFit], numFit, &slopes[ind], &intercepts[ind],
              &zRound);
      }
      numFit = numPts - numBot;
      indFit = numBot;
    }
  } else {

    // Require 2 more points for robust fit
    if (numPts >= mMinForRobustPitch + 2 && numPts <= MAX_FIT_DATA) {
      for (ix = 0; ix < numPts; ix++) {
        mFitMat[0][ix] = xfit[ix];
        mFitMat[1][ix] = zfit[ix];
        mFitMat[2][ix] = yfit[ix];
      }
      maxZeroWgt = numPts / 6;
      err = robustRegress(&mFitMat[0][0], MAX_FIT_DATA, 0, 2, numPts, 1, 
                         fitSolution, MAX_FIT_COL, &intercepts[0], fitMean, fitSD, 
                         mFitWork, mKfactor, &numIter, mMaxIter, maxZeroWgt, 
                         mMaxChange, mMaxOscill);
      slopes[0] = fitSolution[0];
      zRound = fitSolution[1];
    }

    // Do regular fit if robust not tried or failed
    if (err) {
      lsFit2(xfit, zfit, yfit, numPts, &slopes[0], &zRound, &intercepts[0]);
    }
    slopes[1] = slopes[0];
    intercepts[1] = intercepts[0] + zRound;
  }

  // Find a shift equal to maximum residual on each side
  yShift[0] = yShift[1] = 0.;
  for (pt = 0; pt < numPts; pt++) {
    ind = pt < numBot ? 0 : 1;
    yVal = yfit[pt] - (xfit[pt] * slopes[ind] + intercepts[ind]);
    if ((ind && yVal > yShift[ind])|| (!ind && yVal < yShift[ind]))
      yShift[ind] = yVal;
  }

  // Make the lines
  for (ind = 0; ind < 2; ind++) {
    yShift[ind] += (2 * ind - 1) * mExtraForPitch;
    if (imodNewContour(imod) || !(cont = imodContourGet(imod)))
      exitError("Adding contour to model");
    zRound = B3DNINT((mBlockCenters[2 * yStart * numXblocks + 1] + 
                      mBlockCenters[2 * yEnd * numXblocks + 1]) / 2.);
    yVal = xmin[ind] * slopes[ind] + intercepts[ind] + yShift[ind];
    if (!imodPointAppendXYZ(cont, xmin[ind], flipped ? yVal : zRound,
                            flipped ? zRound : yVal))
      exitError("Adding point to model");
    yVal = xmax[ind] * slopes[ind] + intercepts[ind] + yShift[ind];
    if (!imodPointAppendXYZ(cont, xmax[ind], flipped ? yVal : zRound,
                            flipped ? zRound : yVal))
      exitError("Adding point to model");
    cont->time = time;
  }
  return 0;
}

/*
 * Determine number of non-overlapping blocks and number of analyzed points in block for
 * a given scale index and axis
 */
void FindSect::setupBlocks(int numBoxes, int sclInd, int ixyz)
{
  mNumInBlock[sclInd][ixyz] = mScanBlockSize / 
    (mBoxSpacing[sclInd][ixyz] * mBinning[sclInd][ixyz]);
  B3DCLAMP(mNumInBlock[sclInd][ixyz], 1, numBoxes);
  mNumBlocks[sclInd][ixyz] = numBoxes / mNumInBlock[sclInd][ixyz];
  mNumInBlock[sclInd][ixyz] = numBoxes / mNumBlocks[sclInd][ixyz];
}

/*
 * For the range of boxes indicated, and the scaling binInd, add the SD values to 
 * the list in mBuffer and accumulate the sum of means in meanSum.
 */
void FindSect::addBoxesToSample(int startX, int endX, int startY, int endY, int startZ,
                                int endZ, int binInd, int &numStat, double &meanSum)
{
  int boxInd;
  for (int izBox = startZ; izBox <= endZ; izBox++) {
    for (int iyBox = startY; iyBox <= endY; iyBox++) {
      for (int ixBox = startX; ixBox <= endX; ixBox++) {
        boxInd = mStatStartInds[binInd] + (izBox * mNumBoxes[binInd][b3dY] + iyBox) * 
          mNumBoxes[binInd][b3dX] + ixBox;
        mBuffer[numStat++] = mSDs[boxInd];
        meanSum += mMeans[boxInd];
      }
    }
  }  
}

/*
 * Adjust a range in Y to apply to a rotated volume if the data are flipped; this
 * gives consistency between results from original volumes in flipped orientation and
 * volume rotated with rotx.
 */
void FindSect::invertYifFlipped(int &start, int &end, int *dims)
{
  if (mThickInd == b3dY) {
    int tmp = start;
    start = dims[1] - 1 - end;
    end = dims[1] - 1 - tmp;
  }
}

/*
 * For a column of boxes through the thickness dimension with the given box extents in X 
 * and Y, find the midpoint of the region with structure by looking from each edge for the
 * Z value where the SD goes above criterion (returned in izInside).  The middle of those
 * values is returned in izMid, and the median of SD values between that range is 
 * returned in insideMedian.  Return value is 1 if the maximum median in the column is
 * not different enough from edge; 2 if there are too few median SD's above a criterion;
 * or 3 if the inside positions end up being crossed.
 */
int FindSect::findColumnMidpoint(int startX, int endX, int startY, int endY, int binInd,
                                 float *buffer, int *izInside, int &izMid,
                                 float &insideMedian)
{
  float medSD, edgeDiff, edgeCrit, sdMax = -1.;
  int numXYbox = (endY + 1 - startY) * (endX + 1 - startX);
  int boxInd, loop, dir, yStride = 1, zStride = 1;
  int ixBox, iyBox, izBox, zStart, zEnd, numAbove;

  if (mThickInd == b3dZ)
    zStride = mNumBoxes[binInd][b3dY];
  else
    yStride = mNumBoxes[binInd][b3dY];

  // First find a maximum in the column, taking the median of values across each plane
  for (izBox = 0; izBox < mNumBoxes[binInd][mThickInd]; izBox++) {
    dir = 0;
    for (iyBox = startY; iyBox <= endY; iyBox++) {
      for (ixBox = startX; ixBox <= endX; ixBox++) {
        boxInd = mStatStartInds[binInd] + (izBox * zStride + iyBox * yStride) * 
          mNumBoxes[binInd][b3dX] + ixBox;
        buffer[dir++] = mSDs[boxInd];
      }
    }
    rsFastMedianInPlace(buffer, numXYbox, &medSD);
    sdMax = B3DMAX(sdMax, medSD);
    buffer[numXYbox + izBox] = medSD;
  }

  // If difference from edge is below criterion, return error
  edgeDiff = sdMax - mEdgeMedians[binInd];
  if (edgeDiff < mColMaxEdgeDiffCrit * mEdgeMADNs[binInd])
    return 1;
  
  // Criterion is maximum of a number of MADNs above edge and a fraction of max-edge diff
  edgeCrit = mEdgeMedians[binInd] + B3DMAX(edgeDiff * mFracColMaxEdgeDiff, 
                                           mCritEdgeMADN * mEdgeMADNs[binInd]);

  // From each direction, find an edge above the criterion
  zStart = 0;
  zEnd = mNumBoxes[binInd][mThickInd];
  for (loop = 0; loop < 2; loop++) {
    dir = 1 - 2 * loop;
    numAbove = 0;
    for (izBox = zStart; izBox != zEnd; izBox += dir) {
      if (buffer[numXYbox + izBox] >= edgeCrit) {
        numAbove++;
        if (numAbove >= mNumHighInsideCrit) {
          izInside[loop] = izBox;
          break;
        }
      }
    }
    if (numAbove < mNumHighInsideCrit)
      return 2;
    zStart = mNumBoxes[binInd][mThickInd] - 1;
    zEnd = -1;
  }

  //printf("sdMax  %f  edgeCrit %f  %d  %d\n", sdMax, edgeCrit, izInside[0], izInside[1]);
  if (izInside[0] > izInside[1])
    return 3;
  izMid = (izInside[0] + izInside[1]) / 2;
  rsFastMedianInPlace(&buffer[numXYbox + izInside[0]], izInside[1] + 1 - izInside[0], 
                      &insideMedian);
  return 0;
}

/*
 * Finds boundaries of a column defined by the range of boxes in X and Y, looking outward
 * from middle, and fits a line to the falling phase to find the boundary at a set level.
 */
void FindSect::fitColumnBoundaries(int startX, int endX, int startY, int endY,
                                    float *boundary)
{
  int boxInd, loop, ind, dir, yStride = 1, zStride = 1, numExtra = 0;
  int izMid, izInside[2], numInCol[2];
  float insideMed, fallCrit, lowFitCrit, highFitCrit, boundaryLevel, pitchLevel;
  float lastSD, slope, intercept, ro, extraMed;
  int ixBox, iyBox, izBox, iz, izAdd, fitStart, numFit;
  int scl = mBestScale;
  int zRange = mNumBoxes[scl][mThickInd];
  int maxInCol = (endX + 1 - startX) * (endY + 1 - startY);
  float *xfit = mBuffer + 4 * maxInCol;
  float *yfit = xfit + zRange;

  if (mThickInd == b3dZ)
    zStride = mNumBoxes[scl][b3dY];
  else
    yStride = mNumBoxes[scl][b3dY];
  boundary[0] = boundary[1] = -1.;

  // Find midpoint and inside median, reject if it is too low
  if (findColumnMidpoint(startX, endX, startY, endY, scl, mBuffer, izInside, izMid,
                         insideMed))
    return;
  if (insideMed < mCenMedians[scl] - mColumnToCenMADNCrit * mCenMADNs[scl]) {
    if (mDebugOutput > 1)
      printf("skipping %d %d low median %f\n", startX, startY, insideMed);
    return;
  }

  // Set various criteria
  fallCrit = mEdgeMedians[scl] + mMaxFalloffFrac * (insideMed - mEdgeMedians[scl]);
  lowFitCrit = mEdgeMedians[scl] + mLowFitFrac * (insideMed - mEdgeMedians[scl]);
  highFitCrit = mEdgeMedians[scl] + mHighFitFrac * (insideMed - mEdgeMedians[scl]);
  boundaryLevel = mEdgeMedians[scl] + mBoundaryFrac * 
    (insideMed - mEdgeMedians[scl]);
  pitchLevel = mEdgeMedians[scl] + mPitchBoundaryFrac *
    (insideMed - mEdgeMedians[scl]);

  numInCol[0] = numInCol[1] = 0;
  for (iyBox = startY; iyBox <= endY; iyBox++) {
    for (ixBox = startX; ixBox <= endX; ixBox++) {
      for (loop = 0; loop < 2; loop++) {
        dir = 2 * loop - 1;
        numFit = 0;
        fitStart = -1;
        for (izBox = izMid; izBox >= 0 && izBox < zRange; izBox += dir) {
          
          boxInd = mStatStartInds[scl] + (izBox * zStride + iyBox * yStride) * 
            mNumBoxes[scl][b3dX] + ixBox;
          if (mSDs[boxInd] < fallCrit) {

            // Going below the fall criterion triggers various checks
            // First, if the starting point is below it, skip this box column
            if (izBox == izMid)
              break;

            // If this point is below the low fit criterion or is higher than the
            // the last, start fit on previous point, unless it is above the high
            // criterion; in which case start on this one
            if (mSDs[boxInd] < lowFitCrit || mSDs[boxInd] > lastSD) {
              fitStart = izBox - dir;
              if (lastSD > highFitCrit)
                fitStart = izBox;
              
              // But if we are at end of range, start fit on this point
            } else if (izBox == 0 || izBox == zRange - 1) {
              fitStart = izBox;
            }
            
            if (fitStart >= 0) {
              
              // Add points to fit arrays until it goes above the high crit
              for (iz = fitStart; iz != izMid; iz -= dir) {
                ind = mStatStartInds[scl] + (iz * zStride + iyBox * yStride) *
                  mNumBoxes[scl][b3dX] + ixBox;
                if (mSDs[ind] > highFitCrit)
                  break;
                xfit[numFit] = iz;
                yfit[numFit++] = mSDs[ind];
              }
              
              // If there is only one point, add one that is out of the fit range
              // in the other direction if possible
              if (numFit == 1) {
                iz = B3DNINT(xfit[0]);
                izAdd = -1;
                if (yfit[0] < 0.5 * (lowFitCrit + highFitCrit)) {
                  if (iz - dir != izMid)
                    izAdd = iz - dir;
                } else {
                  if (iz + dir >= 0 && iz + dir < zRange)
                    izAdd = iz + dir;
                }
                if (izAdd >= 0) {
                  ind = mStatStartInds[scl] + 
                    (izAdd * zStride + iyBox * yStride) * mNumBoxes[scl][b3dX] + ixBox;
                  xfit[numFit] = izAdd;
                  yfit[numFit++] = mSDs[ind];
                }
              }
              
              // Do the fit if at least 2 points and get Z value at boundary level
              if (numFit >= 2) {
                lsFit(xfit, yfit, numFit, &slope, &intercept, &ro);
                mBuffer[loop * maxInCol + numInCol[loop]++] = 
                  (boundaryLevel - intercept) / slope;
                mBuffer[2 * maxInCol + numExtra++] = 
                  fabs((double)(boundaryLevel - pitchLevel) / slope);
              }
              break;
            }    
          }
          lastSD = mSDs[boxInd];
        }
      }
    }
  }

  // Get median of boundary values if there are enough of them
  for (loop = 0; loop < 2; loop++)
    if (numInCol[loop] >= B3DNINT(B3DMAX(1., mMinFracBoundsInCol * maxInCol)))
      rsFastMedianInPlace(&mBuffer[loop * maxInCol], numInCol[loop], &boundary[loop]);

  // Get the median of extra Z values if there are enough, and add to sum
  if (numExtra >= B3DNINT(B3DMAX(1., mMinFracBoundsInCol * 2. * maxInCol))) {
    mNumExtraSum++;
    rsFastMedianInPlace(&mBuffer[2 * maxInCol], numExtra, &extraMed);
    mExtraPitchSum += extraMed * mBinning[scl][mThickInd];
  }
}

/*
 * Tries to identify blocks that have insufficient thickness and eliminate one or
 * both boundaries
 */
void FindSect::checkBlockThicknesses()
{
  int scl = mBestScale;
  int yInd = 3 - mThickInd;
  int numXblocks = mNumBlocks[scl][b3dX];
  int numYblocks = mNumBlocks[scl][yInd];
  int ixBlock, iyBlock, ind, ixyCen[2], starts[2], ends[2], botTop, numBound, ix, iy, jj;
  float thickMedian, thickMADN, xySpacing, meanBound, meanDiff[2], tmp1, tmp2;

  // Add thicknesses to cumulative collection and get a median/MADN
  for (iyBlock = 0; iyBlock < numYblocks; iyBlock++) {
    for (ixBlock = 0; ixBlock < numXblocks; ixBlock++) {
      ind = 2 * (iyBlock * numXblocks + ixBlock);
      if (mBoundaries[ind] >= 0. && mBoundaries[ind + 1] >= 0.)
        mThicknesses[mNumThicknesses++] = mBoundaries[ind + 1] - mBoundaries[ind];
    }
  }
  if (mNumThicknesses < 7)
    return;

  rsFastMedian(mThicknesses, mNumThicknesses, mBuffer, &thickMedian);
  rsFastMADN(mThicknesses, mNumThicknesses, thickMedian, mBuffer, &thickMADN);
  if (mDebugOutput)
    PRINT3(thickMedian, thickMADN, mNumThicknesses);

  // Look for outlier thicknesses
  for (iyBlock = 0; iyBlock < numYblocks; iyBlock++) {
    for (ixBlock = 0; ixBlock < numXblocks; ixBlock++) {
      ind = 2 * (iyBlock * numXblocks + ixBlock);
      if (mBoundaries[ind] < 0. || mBoundaries[ind + 1] < 0. ||
          (mBoundaries[ind + 1] - mBoundaries[ind]) / thickMedian > mTooThinCrit)
        continue;

      if (mDebugOutput)
        printf("block too thin %d %d  %.1f\n", ixBlock, iyBlock, 
               mBoundaries[ind + 1] - mBoundaries[ind]);
      ixyCen[0] = ixBlock;
      ixyCen[1] = iyBlock;

      // Find a sampling region and extract boundaries for ones not involved in an
      // outlier thickness
      for (botTop = 0; botTop < 2; botTop++) {
        getFittingRegion(ixyCen, 5, 5, botTop, starts, ends, numBound, xySpacing);
        if (mGoodRowCol[0] < 2 || mGoodRowCol[1] < 2)
          getFittingRegion(ixyCen, 7, 2, botTop, starts, ends, numBound, xySpacing);
        numBound = 0;
        for (ix = starts[0]; ix <= ends[0]; ix++) {
          for (iy = starts[1]; iy <= ends[1]; iy++) {
            jj = 2 * (iy * numXblocks + ix);
            if (mBoundaries[jj + botTop] >= 0. && 
                (mBoundaries[jj + 1 - botTop] < 0. ||
                 (mBoundaries[jj + 1] - mBoundaries[jj]) / thickMedian > mTooThinCrit))
              mBuffer[numBound++] = mBoundaries[jj + botTop];
          }
        }
        meanDiff[botTop] = -1.;
        if (numBound > 2) {
          if (numBound > 5)
            rsFastMedianInPlace(mBuffer, numBound, &meanBound);
          else
            avgSD(mBuffer, numBound, &meanBound, &tmp1, &tmp2);
          meanDiff[botTop] = fabs(mBoundaries[ind + botTop] - meanBound);
        }
      }

      // Evaluate the difference from the local mean for each boundary
      // Drop it if it is sufficiently larger than the difference for the other boundary
      // or if it is bigger than a fraction of the median thickness
      if (meanDiff[0] >= 0. && meanDiff[1] >= 0.) {
        if (meanDiff[0] > mFartherFromMeanCrit * meanDiff[1] ||
            meanDiff[0] > mMeanDiffThickFrac * thickMedian) {
          if (mDebugOutput)
            printf("Dropping bottom %.1f diffs %.1f %.1f\n", mBoundaries[ind],
                   meanDiff[0], meanDiff[1]);
          mBoundaries[ind] = -1.;
        } 
        if (meanDiff[1] > mFartherFromMeanCrit * meanDiff[0] ||
            meanDiff[1] > mMeanDiffThickFrac * thickMedian) {
          if (mDebugOutput)
            printf("Dropping top %.1f diffs %.1f %.1f\n", mBoundaries[ind + 1],
                   meanDiff[0], meanDiff[1]);
          mBoundaries[ind + 1] = -1.;
        }
      }
    }
  }
}
        
/*
 * For a block whose X, Y center is in blockCen, finds a region to fit with desired size 
 * extentNum in the major direction, and maximum extent maxDepth in the other, for the
 * surface in botTop.   Returns starting and ending blocks in blockStart and blockEnd,
 * num of positions with data in numPts, and the spacing between blocks in xySpacing.
 */
void FindSect::getFittingRegion(int blockCen[], int extentNum, int maxDepth, int botTop,
                                int *blockStart, int *blockEnd, int &numPts, 
                                float &xySpacing)
{
  int scl = mBestScale;
  int yInd = 3 - mThickInd;
  int numXblocks = mNumBlocks[scl][b3dX];
  int numYblocks = mNumBlocks[scl][yInd];
  int stride[2], numBlk[2];
  int extentAxis = -1;
  float distance, spacing[2] = {0., 0.};
  bool shifted;
  int ixy, ind, other, inner, outer, end, start, depAxis, numGood;
  stride[0] = 2;
  stride[1] = 2 * numXblocks;

  // Find direction that defines the extent
  for (ixy = 0; ixy < 2; ixy++) {
    start = B3DMAX(0, blockCen[ixy] - 1);
    end = B3DMIN(mNumBlocks[scl][ixy * yInd] - 1, start + 2);
    start = B3DMAX(0, end - 2);
    if (start == end) {
      extentAxis = 1 - ixy;
      continue;
    }
    ind = blockCen[1 - ixy] * stride[1 - ixy] + ixy;
    spacing[ixy] = (mBlockCenters[end * stride[ixy] + ind] - 
                    mBlockCenters[start * stride[ixy] + ind]) / (end - start);
  }

  // If either axis is feasible, take the one with the bigger number of blocks if either
  // is limited, or the one with smaller spacing 
  if (extentAxis < 0) {
    if (numXblocks < extentNum || numYblocks < extentNum)
      extentAxis = numXblocks < numYblocks ? 1 : 0;
    else
      extentAxis = spacing[1] < 0.9 * spacing[0] ? 1 : 0;
  }
  depAxis = 1 - extentAxis;
  numBlk[extentAxis] = extentNum;
  xySpacing = B3DMAX(1., spacing[extentAxis]);

  // Get the number in the other direction: shoot for equal extent, limit by maximum
  if (!spacing[depAxis]) {
    numBlk[depAxis] = 1;
  } else {
    distance = spacing[extentAxis] * (extentNum - 1);
    numBlk[depAxis] = B3DNINT(1. + distance / spacing[depAxis]);
    numBlk[depAxis] = B3DMIN(numBlk[depAxis], maxDepth);
  }

  // Get the start and end in each direction
  for (ixy = 0; ixy < 2; ixy++) {
    blockStart[ixy] = B3DMAX(0, blockCen[ixy] - (numBlk[ixy] / 2));
    blockEnd[ixy] = B3DMIN(mNumBlocks[scl][ixy * yInd] - 1,
                           blockStart[ixy] + numBlk[ixy] - 1);
    blockStart[ixy] = B3DMAX(0, blockEnd[ixy] + 1 - numBlk[ixy]);
    numBlk[ixy] = blockEnd[ixy] + 1 - blockStart[ixy];
  }

  // Try to slide region if there are empty rows on one side
  for (ixy = 0; ixy < 2; ixy++) {
    shifted = false;
    other = 1 - ixy;
    while (!hasBoundaries(ixy, blockStart[ixy], blockStart[other], blockEnd[other], 
                          botTop) && 
           hasBoundaries(ixy, blockEnd[ixy] + 1, blockStart[other], blockEnd[other],
                         botTop) && blockStart[ixy] < blockCen[ixy]) {
      shifted = true;
      blockStart[ixy]++;
      blockEnd[ixy]++;
      if (mDebugOutput > 1)
        printf("Shifted + on axis %d\n", ixy);
    }
    while (!shifted && !hasBoundaries(ixy, blockEnd[ixy], blockStart[other],
                                      blockEnd[other], botTop) &&
           hasBoundaries(ixy, blockStart[ixy] - 1, blockStart[other], blockEnd[other],
                         botTop) && blockEnd[ixy] > blockCen[ixy]) {
      blockStart[ixy]--;
      blockEnd[ixy]--;
      if (mDebugOutput > 1)
        printf("Shifted - on axis %d\n", ixy);
    }
  }

  // Count the good rows and columns and total points
  for (ixy = 0; ixy < 2; ixy++) {
    other = 1 - ixy;

    // For counting in a direction, this determines number good in other direction
    // Loop on other direction
    mGoodRowCol[other] = 0;
    numPts = 0;
    for (outer = blockStart[other]; outer <= blockEnd[other]; outer++) {
      numGood = 0;

      // Loop on main direction and count ones with data
      for (inner = blockStart[ixy]; inner <= blockEnd[ixy]; inner++)
        if (mBoundaries[inner * stride[ixy] + outer * stride[other] + botTop] >= 0)
          numGood++;

      // The line is good if it has at least half of the blocks with data
      if (numGood >= B3DMAX(0.99, numBlk[ixy] / 2. - 0.1))
        mGoodRowCol[other]++;
      numPts += numGood;
    }
  }
}

/*
 * Tests whether there are any boundaries along one axis, at the given row or column,
 * between start and end on the other axis, on surface given by botTop
 */
bool FindSect::hasBoundaries(int axis, int rowCol, int start, int end, int botTop)
{
  int stride[2] = {2, 2};
  stride[1] = 2 * mNumBlocks[mBestScale][b3dX];
  if (rowCol < 0 || rowCol >= mNumBlocks[mBestScale][axis * (3 - mThickInd)])
    return false;
  for (int ind = start; ind <= end; ind++)
    if (mBoundaries[rowCol * stride[axis] + ind * stride[1 - axis] + botTop] >= 0)
      return true;
  return false;
}

/*
 *
 */
void FindSect::loadFittingMatrix(int *ixyCen, int *starts, int *ends, int botTop,
                                 float xySpacing, float wgtThresh, int wgtColIn,
                                 int wgtColOut, int &numFit)
{
  int ixBlock, iyBlock, cenInd, blkInd, ixy, var, indWgt = -1;
  numFit = 0;
  cenInd = 2 * (ixyCen[0] + ixyCen[1] * mNumBlocks[mBestScale][b3dX]);
  for (iyBlock = starts[1]; iyBlock <= ends[1]; iyBlock++) {
    for (ixBlock = starts[0]; ixBlock <= ends[0]; ixBlock++) {
      blkInd = 2 * (ixBlock + iyBlock * mNumBlocks[mBestScale][b3dX]);
      if (mBoundaries[blkInd + botTop] >= 0) {
        indWgt++;
        if (wgtColIn > 0 && mFitMat[wgtColIn][indWgt] < wgtThresh)
          continue;
        if (wgtColIn > 0 && wgtColOut > 0)
          mFitMat[wgtColOut][numFit] = mFitMat[wgtColIn][indWgt];
        for (var = 0; var < mNumVars; var++) {
          ixy = mVarList[var][0];
          mFitMat[var][numFit] = (mBlockCenters[blkInd + ixy] - 
                                  mBlockCenters[cenInd + ixy]) / xySpacing;
          ixy = mVarList[var][1];
          if (ixy >= 0)
            mFitMat[var][numFit] *= (mBlockCenters[blkInd + ixy] - 
                                         mBlockCenters[cenInd + ixy]) / xySpacing;
        }
        mFitMat[mNumVars][numFit] = mBoundaries[blkInd + botTop];
        numFit++;
      }
    }
  }
}

/*
 * Sets up indices for a polynomial fit appropriate to the number of points and
 * number of "good" points with higher weights.  A term can be a single variable (if
 * varlist[][1] is -1 or a product of two variables in varList[][0] and varList[1].
 */
void FindSect::buildVariableList(int major, int minor, int numBound, int numGood)
{
  mNumVars = 1;
  mVarList[0][0] = major;
  mVarList[0][1] = -1;
  if (numBound >= 10 && numGood > 8 && mGoodRowCol[minor] >= 2) {
    mVarList[1][0] = minor;
    mVarList[1][1] = -1;
    mNumVars = 2;
  }
  if (numBound >= 14 && numGood > 11 && mGoodRowCol[minor] >= 2) {
    mVarList[2][0] = major;
    mVarList[2][1] = major;
    mNumVars = 3;
  }
  if (numBound >= 20 && numGood > 17 && mGoodRowCol[minor] >= 4) {
    mVarList[3][0] = major;
    mVarList[3][1] = minor;
    mVarList[4][0] = minor;
    mVarList[4][1] = minor;
    mNumVars = 5;
  }
}

/*
 * Callback function to load a needed slice for multiBinStats
 */
static int getSlice(int *iz, int *fdata, float *buffer)
{
  iiuSetPosition(fdata[0], *iz, 0);
  return iiuReadSecPart(fdata[0], (char *)buffer, fdata[2] + 1 - fdata[1], fdata[1],
                        fdata[2], fdata[3], fdata[4]);
}

