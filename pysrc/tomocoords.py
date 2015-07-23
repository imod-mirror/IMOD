#!/usr/bin/python
# tomocoords.py module - used by subtomosetup and rawtiltcoords
#
# Author: David Mastronarde
#
# $Id$
#

yzRatioCrit = 2.

import os, sys
from pip import *
from imodpy import *

# Do initial option startup and get the options that are common between the programs
def getCommonOptions(options, progname):
   (opts, nonopts) = PipReadOrParseOptions(sys.argv, options, progname, 1, 0, 1)

   # Get the options
   rootName = PipGetString('RootName', '')
   if not rootName:
      exitError('A root name must be entered')
   volName = PipGetString('VolumeModeled', '')
   if not volName:
      exitError('The name of the volume on which points were picked must be entered')

   # Get position file and determine if it is model or point file
   centerFile = PipGetString('CenterPositionFile', '')
   if not centerFile:
      exitError('You must enter a model or point file name with -center')
   pointFile = ''
   modelFile = ''
   try:
      infoLines = runcmd('imodinfo -h "' + centerFile + '"', inStderr = 'stdout')
      for line in infoLines:
         if 'Error (-1) reading' in line or 'Model has no objects' in line:
            pointFile = centerFile
            break
   except:
      pointFile = centerFile

   if not pointFile:
      modelFile = centerFile
      
   objList = PipGetString('ObjectsToUse', '')
   if objList and pointFile:
      exitError('You cannot enter a list of objects with a point file')

   comFile = PipGetString('CommandFile', 'tilt.com')
   reorientIn = PipGetInteger('ReorientionType', 0)
   enteredOrient = 1 - PipGetErrNo()
   return (rootName, volName, centerFile, pointFile, modelFile, objList, comFile, \
       reorientIn, enteredOrient)


# Get the coordinates from a model or point file, read the headers of all relevant files,
# and work out the orientation with all the needed messages
def getPointsAndHeaders(modelFile, objList, pointFile, progname, stackName, aliName, \
                           volName, fullRec, enteredOrient, reorientIn, comFile):
   
   # Get the coordinate list
   pid = '.' + str(os.getpid())
   cleanList = []
   descrip = ''
   if modelFile:
      modConvert = modelFile
      descrip = 'temporary'

      # Extract objects
      if objList:
         modConvert = modelFile + '.obj' + pid
         cleanList.append(modConvert)
         try:
            runcmd(fmtstr('imodextract "{}" "{}" "{}"', objList, modelFile, modConvert))
         except ImodpyError:
            cleanupFiles(cleanList)
            exitFromImodError(progname)

      # Convert to point list, with -scale option to compensate for subset loading
      pointFile = modelFile + '.pt' + pid
      cleanList.append(pointFile)
      try:
         runcmd(fmtstr('model2point -scale -float "{}" "{}"', modConvert, pointFile))
      except ImodpyError:
         cleanupFiles(cleanList)
         exitFromImodError(progname)
         
   # Now read in the point file and process the lines
   pointLines = readTextFile(pointFile, descrip + ' point file', True)
   cleanupFiles(cleanList)
   if isinstance(pointLines, str):
      exitError(pointLines)

   pointList = []
   for line in pointLines:
      try:
         lsplit = line.split()
         if len(lsplit) < 3:
            exitError('There are not three values on the line in ' + descrip + \
                         ' point file: ' + line)
         pointList.append([float(lsplit[0]), float(lsplit[1]), float(lsplit[2])])
      except ValueError:
         exitError('Converting a value to a floating point number on the line in ' + \
                      descrip + ' point file: ' + line)

   # Get the file headers
   try:
      (nxRaw, nyRaw, nzRaw, mode, pixXraw, pixYraw, pixZraw) = getmrc(stackName)
      (nxAli, nyAli, nzAli, mode, pixXali, pixYali, pixZali, origXali, origYali, \
          origZali, dmin, dmax, dmean) = getmrc(aliName, True)
      (nxVol, nyVol, nzVol, mode, pixXvol, pixYvol, pixZvol, origXvol, origYvol, \
          origZvol, dmin, dmax, dmean) = getmrc(volName, True)
      if fullRec:
         (nxFull, nyFull, nzFull, mode, pixXfull, pixYfull, pixZfull, origXfull, \
             origYfull, origZfull, dmin, dmax, dmean) = getmrc(fullRec, True)
      else:
         (nxFull, nyFull, nzFull, pixXfull, pixYfull, pixZfull, origXfull, \
             origYfull, origZfull) = (0, 0, 0, 0, 0, 0, 0, 0, 0)

      headLines = runcmd('header "' + volName + '"')
      aliBinning = int(round(pixXali / pixXraw))
   except ImodpyError:
      exitFromImodError(progname)

   # Deduce post-processing of the volume: look for tilt angles and clip flipyz
   tiltXorig = -999.
   orientTitle = -2
   for line in headLines:
      if 'ilt angles' in line:
         lsplit = line.split();
         if len(lsplit) < 9:
            exitError('Tilt angles line in header output from ' + volName + \
                         ' has too few items')
         try:
            tiltXorig = float(lsplit[-6])
            tiltXcur = float(lsplit[-3])
         except ValueError:
            exitError('Converting tilt angles to floating point values in header ' +\
                         'output from ' + volName)
      if 'clip: flipyz' in line.lower():
         orientTitle = 1
      if 'clip: rotx' in line.lower():
         orientTitle = -1

   # Set orientation values based on size and angles
   orientSize = -2
   if nzVol < nyVol / yzRatioCrit:
      orientSize = 1
   if nyVol < nzVol / yzRatioCrit:
      orientSize = 0
         
   orientAngle = -2
   if tiltXorig == 90. and tiltXcur == 0.:
      orientAngle = -1
   elif tiltXorig == 0. and tiltXcur == 90.:
      orientAngle = orientSize

   # Give information only when orientation entered
   if enteredOrient:
      if reorient < 0 and orientTitle == 1:
         prnstr('Using specified reorientation even though title indicates rotation ' +\
                   'around X')
      if reorient < 0 and orientAngle != -1:
         prnstr('Using specified reorientation even though header angles indicate ' +\
                   ' rotation around X')
      if reorient >= 0 and orientTitle == -1:
         prnstr('Using specified reorientation even though title indicates swapping ' +\
                   'of Y and Z')
      if reorient >= 0 and orientAngle == -1:
         prnstr('Using specified reorientation even though header angles indicate ' +\
                   'swapping of Y and Z')
   else:

      # Otherwise look for consistency, inform of decision in almost all cases, warn of
      # possible inconsistency, exit if not conclusive
      if orientAngle == -1:
         reorient = -1
         if orientTitle == 1:
            prnstr('WARNING: ' + progname + ' - Assuming reorientation by rotation ' +\
                      'around  X because of header angles, but there is a title ' +\
                      ' indicating swapping of Y and Z')
         if orientSize == 0:
            prnstr('WARNING: ' + progname + ' - Assuming reorientation by rotation ' +\
                      'around X because of header angles, even though Z dimension is ' +\
                      'much bigger than Y')
      elif orientAngle == -2:
         exitError('You must enter -reorient to indicate reorientation type, because ' +\
                      'header angles are not consistent with known types')
      else:
         if orientTitle == -1:
            exitError('You must enter -reorient to indicate reorientation type, ' +\
                         'because header angles are not consistent with title ' +\
                         'indicating rotation around X')
         if orientTitle == 1:
            if orientSize == 0:
               exitError('You must enter -reorient to indicate reorientation type, ' +\
                            'because there is a title indicating swapping of Y and Z ' +\
                            'but the Z dimension is much bigger than Y')
            reorient = 1
            if orientSize == 1:
               prnstr('Assuming reorientation by swapping Y and Z as indicated by title')
            else:
               prnstr('WARNING: ' + progname + ' - Assuming reorientation by swapping ' +\
                         'Y and Z as indicated by title, but Y and Z dimensions do ' +\
                         'not clearly support this assumption')
         else:
            if orientSize < 0:
               exitError('You must enter -reorient to indicate reorientation type, ' +\
                            'because Y and Z dimensions do not clearly indicate ' +\
                            'orientation')
            reorient = orientSize
            if reorient:
               prnstr('Assuming reorientation by swapping Y and Z because of Y and Z ' +\
                         'dimensions, even though there is no flipyz title')
            else:
               prnstr('Assuming no reorientation because of Y and Z dimensions')
               
   comLines = readTextFile(comFile)
   return (pid, cleanList, pointList, aliBinning, reorient, comLines, \
              nxRaw, nyRaw, nzRaw, pixXraw, pixYraw, pixZraw, \
              nxAli, nyAli, nzAli, pixXali, pixYali, pixZali, origXali, origYali, \
              origZali, nxVol, nyVol, nzVol, pixXvol, pixYvol, pixZvol, origXvol, \
              origYvol, origZvol, nxFull, nyFull, nzFull, pixXfull, pixYfull, pixZfull, \
              origXfull, origYfull, origZfull)
