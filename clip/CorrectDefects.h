// Declarations for CorrectDefects.cpp and the CameraDefects structure
//
//  $Id$
//
#ifndef CORRECT_DEFECTS_H
#include <vector>
#include <string>
typedef std::vector<short int> ShortVec;
typedef std::vector<unsigned short int> UShortVec;
typedef std::vector<int> IntVec;

struct CameraDefects
{
  int wasScaled;           // flag for whether data have been scaled: 1 if up, -1 if down
  int rotationFlip;        // Rotation - flip value of these coordinates relative to CCD
  int K2Type;              // Flag that camera is K2
  int usableTop;           // Usable area, defined by first and last good rows and columns
  int usableLeft;
  int usableBottom;
  int usableRight;
  UShortVec badColumnStart;  // First column of each entry
  ShortVec badColumnWidth;  // Number of adjacent bad columns
  UShortVec partialBadCol;   // the partial starting column
  ShortVec partialBadWidth;   // the number of columns
  UShortVec partialBadStartY;  // Start and end in Y
  UShortVec partialBadEndY;
  UShortVec badRowStart;  // First row of each entry
  ShortVec badRowHeight;  // Number of adjacent bad rows
  UShortVec partialBadRow;   // the partial starting row
  ShortVec partialBadHeight;   // the number of rows
  UShortVec partialBadStartX;  // Start and end in X
  UShortVec partialBadEndX;
  UShortVec badPixelX;    // Bad pixel X and Y coordinates
  UShortVec badPixelY;
  std::vector<char>pixUseMean;  // Flag for pixels to fill with mean, touch other defects
};

void CorDefCorrectDefects(CameraDefects *param, void *array, int type, int binning,
                        int top, int left, int bottom, int right);
void CorDefScaleDefectsForK2(CameraDefects *param, bool scaleDown);
void CorDefFlipDefectsInY(CameraDefects *param, int camSizeX, int camSizeY, int wasScaled);
void CorDefMergeDefectLists(CameraDefects &defects, unsigned short *xyPairs, 
                            int numPoints, int camSizeX, int camSizeY, int rotationFlip);
void CorDefMirrorCoords(int size, int binning, int &start, int &end);
void CorDefUserToRotFlipCCD(int operation, int binning, int &camSizeX, int &camSizeY, int &imSizeX,
                            int &imSizeY, int &top, int &left, int &bottom, int &right);
void CorDefRotFlipCCDtoUser(int operation, int binning, int &camSizeX, int &camSizeY, int &imSizeX,
                            int &imSizeY, int &top, int &left, int &bottom, int &right);
void CorDefRotateCoordsCW(int binning, int &camSizeX, int &camSizeY, int &imSizeX,
                          int &imSizeY, int &top, int &left, int &bottom, int &right);
void CorDefRotateCoordsCCW(int binning, int &camSizeX, int &camSizeY, int &imSizeX,
                           int &imSizeY, int &top, int &left, int &bottom, int &right);
void CorDefRotFlipCCDcoord(int operation, int camSizeX, int camSizeY, int &xx, int &yy);
void CorDefDefectsToString(CameraDefects &defects, std::string &strng, int camSizeX,
                           int camSizeY);
void CorDefAddBadColumn(int col, UShortVec &badColumnStart, ShortVec &badColumnWidth);
void CorDefAddPartialBadCol(int *values,  UShortVec &partialBadCol, 
                                  ShortVec &partialBadWidth, UShortVec &partialBadStartY, 
                                  UShortVec &partialBadEndY);
void CorDefRotateFlipDefects(CameraDefects &defects, int rotationFlip, int camSizeX,
                             int camSizeY);
void CorDefFindTouchingPixels(CameraDefects &defects, int camSizeX, int camSizeY, int wasScaled);
void CorDefSampleMeanSD(void *array, int type, int nx, int ny, float *mean, float *sd);
int CorDefParseDefects(const char *strng, int fromString, CameraDefects &defects, 
                        int &camSizeX, int &camSizeY);

#endif
