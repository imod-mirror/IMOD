*       * * * * * * TILTXCORR * * * * * *
*       
*       TILTXCORR uses cross-correlation to find an initial translational
c       alignment between successive images of a tilt series.  For a given
c       pair of images, it stretches the image with the larger tilt angle
c       perpendicular to the tilt axis, by an amount equal to the ratio of
c       the cosines of the two tilt angles (cosine stretch).  The stretched
c       image is correlated with the other image, and the position of the
c       peak of the correlation indicates the relative shift between the
c       images.  There are options to use only a subset of the image, to
c       pad the image with a border before correlating, and to taper the
c       image intensities down to the average level over some boundary
c       region.  The latter feature is particularly important for getting
c       reliable correlation peaks.  The program also has an option to
c       correlate each image with the sum of already-aligned images at lower
C       tilts, a method developed by Christian Renkin. 
c
c       For further details, see the man page.
C       
c       $Id$
c       Log at end
c       
      implicit none
      integer idim,idim2,lenTemp,limpatch,limbound
      parameter (idim=4300,idim2=idim*idim,lenTemp=1000000)
      parameter (limpatch = 100000, limbound = 1000)
      include 'smallmodel.inc'
      integer*4 NX,NY,NZ,nxs,nys,nzs
      COMMON //NX,NY,NZ,nxs,nys,nzs
C       
      integer*4 NXYZ(3),MXYZ(3),nxyzs(3)
      real*4 title(20),tmprray(lenTemp), delta(3), origin(3)
      real*4 ctfp(8193),sumray(idim2),crray(idim2)
      real*4 array(idim2),brray(idim2)
C       
      EQUIVALENCE (NX,NXYZ),(nxs,nxyzs)
      common /bigarr/ array,sumray,brray,crray,tmprray
c       
      character*320 filin,plfile,imfilout,ptfilout,xffilout
      character*1000 listString
      real*4 fs(2,3),fsinv(2,3),funit(2,3)
      character*9 dat
      character*8 tim
      character*80 titlech
      character*70 titstr
      character*10 fltrdp/' '/

      real*4, allocatable :: f(:,:,:), tilt(:), dxpre(:), dypre(:)
      integer*4, allocatable :: ixpclist(:),iypclist(:),izpclist(:)
      integer*4, allocatable :: listz(:), listSkip(:)
      real*4 patchCenX(limpatch), patchCenY(limpatch)
      real*4, allocatable :: xmodel(:,:), ymodel(:,:), xbound(:), ybound(:)
      real*4, allocatable :: xtfsBound(:), ytfsBound(:)
      integer*4, allocatable :: iobjFlags(:)
      integer*4 iobjBound(limbound),indBound(limbound)
      integer*4 numInBound(limbound)
      real*4 xtfsBmin(limbound), xtfsBmax(limbound), ytfsBmin(limbound)
      real*4 ytfsBmax(limbound)
      real*4 dmin2,dmax2,dmean2,dmean3,rotangle,deltap,radexcl,cosStrMaxTilt
      integer*4 i,npclist,nview,minxpiece,nxpieces,nxoverlap,minypiece, numBest, indBest
      integer*4 nypieces,nyoverlap,ifimout,nxpad,nypad,ifexclude,mode
      integer*4 nxtrim,nytrim,nxuse,nyuse,nxbord,nybord,nxtap,nytap
      integer*4 izst,iznd,kk,nout,izlast,izcur,idir,iztmp, imagesBinned
      real*4 dmsum,dmax,dmin,stretch,streak,xpeak,ypeak,usdx,usdy
      real*4 dmean, radius1, radius2, sigma1, sigma2, tiltAtMin, cosview
      integer*4 iv,iview,kti,isout, ierr, ivStart, ivEnd, loopDir, ifReadXfs
      integer*4 iloop, nloops, minTilt, ifAbsStretch, ivRef, ifLeaveAxis, ivRefBase
      integer*4 nbin,maxbinsize,nxusebin,nyusebin, ifcumulate, ifNoStretch, maxTrackGap
      integer*4 ixst, ixnd, iyst, iynd, ivCur, maxBinning, iz, ivSkip
      integer*4 ixBoxCur, iyBoxCur, ixBoxRef,iyBoxRef,lenContour,minContOverlap
      integer*4 ixstCen, ixndCen, iystCen, iyndCen, iter,niter
      integer*4 laptot, laprem, j, ivbase, lapbase, numCont, nxPatch, nyPatch
      real*4 xBoxOfs, yBoxOfs, cosphi, sinphi, x0, y0, xshift, yshift
      real*4 usemin, usemax, usemean, cumXshift, cumYshift, cumXrot, xAdjust
      real*4 angleOffset,cumXcenter,cumYcenter,xModOffset,yModOffset
      real*4 xpeakcum,ypeakcum,xpeaktmp,ypeaktmp, xpeakFrac,ypeakFrac,yOverlap
      real*4 peakFracTol,xFromCen,yFromCen,cenx, ceny, basetilt,xOverlap,yval
      integer*4 numPatches, numXpatch, numYpatch, ind,ixBoxStart,iyBoxStart
      integer*4 ixBoxForAdj, iyBoxForAdj, ipatch, numPoints, numBound,iobj
      integer*4 ipnt, ipt, numInside, iobjSeed, imodObj, imodCont, ix, iy
      integer*4 limitShiftX, limitShiftY, numSkip, lastNotSkipped,lenConts, nFillTaper
      real*4 critInside, cosRatio, peakVal, peakLast, xpeakLast, yPeakLast
      real*4 boundXmin, boundXmax, boundYmin, boundYmax, fracXover, fracYover
      real*4 fracOverMax, critNonBlank, fillTaperFrac
      real*8 wallmask, walltime, wallstart, wallinterp, wallfft

      logical*4 tracking, verbose, breaking, taperCur, taperRef
      integer*4 niceframe, newimod,putImodMaxes,putModelName,numberInList, taperAtFill
      logical inside
      real*4 cosd, sind

      logical pipinput
      integer*4 numOptArg, numNonOptArg
      integer*4 PipGetInteger,PipGetBoolean, PipGetLogical
      integer*4 PipGetString,PipGetFloat, PipGetTwoIntegers, PipGetTwoFloats
      integer*4 PipGetInOutFile, ifpip
c       
c       fallbacks from ../../manpages/autodoc2man -2 2  tiltxcorr
c       
      integer numOptions
      parameter (numOptions = 43)
      character*(40 * numOptions) options(1)
      options(1) =
     &    'input:InputFile:FN:@piece:PieceListFile:FN:@output:OutputFile:FN:@'//
     &    'rotation:RotationAngle:F:@first:FirstTiltAngle:F:@'//
     &    'increment:TiltIncrement:F:@tiltfile:TiltFile:FN:@angles:TiltAngles:FAM:@'//
     &    'offset:AngleOffset:F:@radius1:FilterRadius1:F:@radius2:FilterRadius2:F:@'//
     &    'sigma1:FilterSigma1:F:@sigma2:FilterSigma2:F:@exclude:ExcludeCentralPeak:B:@'//
     &    'shift:ShiftLimitsXandY:IP:@border:BordersInXandY:IP:@xminmax:XMinAndMax:IP:@'//
     &    'yminmax:YMinAndMax:IP:@boundary:BoundaryModel:FN:@'//
     &    'objbound:BoundaryObject:I:@binning:BinningToApply:I:@'//
     &    'leaveaxis:LeaveTiltAxisShifted:B:@pad:PadsInXandY:IP:@'//
     &    'taper:TapersInXandY:IP:@views:StartingEndingViews:IP:@skip:SkipViews:LI:@'//
     &    'break:BreakAtViews:LI:@cumulative:CumulativeCorrelation:B:@'//
     &    'absstretch:AbsoluteCosineStretch:B:@nostretch:NoCosineStretch:B:@'//
     &    'iterate:IterateCorrelations:I:@size:SizeOfPatchesXandY:IP:@'//
     &    'number:NumberOfPatchesXandY:IP:@overlap:OverlapOfPatchesXandY:IP:@'//
     &    'seed:SeedModel:FN:@objseed:SeedObject:I:@length:LengthAndOverlap:IP:@'//
     &    'prexf:PrealignmentTransformFile:FN:@imagebinned:ImagesAreBinned:I:@'//
     &    'test:TestOutput:FN:@verbose:VerboseOutput:B:@param:ParameterFile:PF:@'//
     &    'help:usage:B:'
c       
c       set defaults here where not dependent on image size
c       
      ifimout=0
      ifexclude=0
      nxtrim=0
      nytrim=0
      sigma1 = 0.
      sigma2 = 0.
      radius1 = 0.
      radius2 = 0.
      rotangle = 0.
      imfilout = ' '
      ptfilout = ' '
      xffilout = ' '
      ifcumulate = 0
      ifNoStretch = 0
      ifAbsStretch = 0
      ifLeaveAxis = 0
      angleOffset = 0.
      maxbinsize=1180
      maxBinning = 8
      cosStrMaxTilt = 82.
      nbin = 0
      ifpip = 0
      niter = 1
      peakFracTol = 0.015
      lenContour = 0
      minContOverlap = 0
      tracking = .false.
      verbose = .false.
      critInside = 0.75
      limitShiftX = 1000000
      limitShiftY = 1000000
      fracXover = 0.33
      fracYover = 0.33
      fracOverMax = 0.8
      wallmask = 0.
      wallinterp = 0.
      wallfft = 0.
      numSkip = 0
      breaking = .false.
      imagesBinned = 1
      ifReadXfs = 0
      critNonBlank = 0.7
      fillTaperFrac = 0.1
      maxTrackGap = 5
c       
c       Pip startup: set error, parse options, check help, set flag if used
c       
      call PipReadOrParseOptions(options, numOptions, 'tiltxcorr',
     &    'ERROR: TILTXCORR - ', .true., 3, 1, 1, numOptArg,
     &    numNonOptArg)
      pipinput = numOptArg + numNonOptArg .gt. 0

      if (PipGetInOutFile('InputFile', 1, 'Image input file', filin)
     &    .ne. 0) call exitError('NO INPUT FILE SPECIFIED')
      CALL IMOPEN(1,FILIN,'RO')
      CALL IRDHDR(1,NXYZ,MXYZ,MODE,DMIN2,DMAX2,DMEAN2)
      izst=1
      iznd=nz

      allocate(f(2,3,nz), tilt(nz), ixpclist(nz),iypclist(nz),izpclist(nz), listz(nz),
     &    listSkip(nz), stat=ierr)
      call memoryError(ierr, 'ARRAYS FOR VIEWS')
C       
      if (pipinput) then
        ifpip = 1
        plfile = ' '
        ierr = PipGetString('PieceListFile', plfile)
        ierr = PipGetLogical('VerboseOutput', verbose)
      else
        write(*,'(1x,a,$)')'Piece list file if there is one,'//
     &      ' otherwise Return: '
        read(*,101)plfile
101     format(a)
      endif
      call read_piece_list2(plfile,ixpclist,iypclist,izpclist,npclist, nz)
c       
c       if no pieces, set up mocklist
c       
      if(npclist.eq.0)then
        do i=1,nz
          ixpclist(i)=0
          iypclist(i)=0
          izpclist(i)=i-1
        enddo
        npclist=nz
      endif
      if(npclist.ne.nz)then
        write(*,'(/,a,i5,a,i5)')'ERROR: TILTXCORR - Piece list should have an '//
     &      'entry for each image; nz =', nz,', # in piece list =',npclist
        call exit(1)
      endif
      call fill_listz(izpclist,npclist,listz,nview)
      call checklist(ixpclist,npclist,1,nx,minxpiece ,nxpieces,nxoverlap)
      call checklist(iypclist,npclist,1,ny,minypiece ,nypieces,nyoverlap)
      if(nxpieces*nypieces.gt.1)call exitError(
     &    'Program will not work with montages; '//
     &    'blend images into single frames first')

      if(nview.ne.nz.or.listz(1).ne.0.or.listz(nview).ne.nz-1)then
        write(*,'(/,a,i5)')'ERROR: TILTXCORR - The piece list should specify',
     &      ' all Z values from 0 to' ,nz-1
        call exit(1)
      endif
c       
c       Get the output file
      if (PipGetInOutFile('OutputFile', 2, 'Output file for transforms',
     &    xffilout) .ne. 0) call exitError('NO OUTPUT FILE SPECIFIED')
c       
      call get_tilt_angles(nview,3,tilt, nz, ifpip)
      if(nview.ne.nz)then
        write(*,'(/,a,i5,a,i5,a)')'ERROR: TILTXCORR - There must be a tilt angle for'
     &      //' each image: nz =', nz,', but there are',nview, ' tilt angles'
        call exit(1)
      endif
c       
c       DNM 4/28/02: figure out binning now, and fix this to send setctf
c       approximately correct nx and ny instead of nxpad and nypad which
c       don't yet exist
c       DNM 7/11/03: better fix is to wait to get ctf until pad size is known
c       DNM 11/1/5/03: and wait to get binning so big padding can be used
c       
      if (pipinput) then
        if (PipGetString('TestOutput', imfilout) .eq. 0) then
          CALL IMOPEN(3,imfilout,'NEW')
          CALL ITRHDR(3,1)
          ifimout=1
        endif
        ierr = PipGetFloat('RotationAngle', rotangle)
        ierr = PipGetFloat('FilterRadius1', radius1)
        ierr = PipGetFloat('FilterRadius2', radius2)
        ierr = PipGetFloat('FilterSigma1', sigma1)
        ierr = PipGetFloat('FilterSigma2', sigma2)
        ierr = PipGetBoolean('ExcludeCentralPeak', ifexclude)
        ierr = PipGetTwoIntegers('BordersInXandY', nxtrim, nytrim)
        ierr = PipGetTwoIntegers('ShiftLimitsXandY', limitShiftX, limitShiftY)
        ierr = PipGetInteger('IterateCorrelations', niter)
        niter = max(1, min(6, niter))
        ierr = PipGetTwoIntegers('LengthAndOverlap', lenContour,minContOverlap)
        if (lenContour .gt. 0) lenContour = max(3, lenContour)
        minContOverlap = min(max(1, minContOverlap), lenContour - 2)
        ixst = nxtrim
        ixnd = nx - 1 - nxtrim
        iyst = nytrim
        iynd = ny - 1 - nytrim
        ierr = PipGetTwoIntegers('XMinAndMax', ixst, ixnd)
        ierr = PipGetTwoIntegers('YMinAndMax', iyst, iynd)
        if (PipGetInteger('BinningToApply', nbin) .eq. 0) then
          if (nbin .le. 0 .or. nbin .gt. maxbinning) call exitError
     &        ('THE ENTERED VALUE FOR BINNING IS OUT OF RANGE')
        endif

        ierr = PipGetBoolean('CumulativeCorrelation', ifcumulate)
        ierr = PipGetBoolean('NoCosineStretch', ifNoStretch)
        ierr = PipGetBoolean('AbsoluteCosineStretch', ifAbsStretch)
        ierr = PipGetBoolean('LeaveTiltAxisShifted', ifLeaveAxis)
        ierr = PipGetFloat('AngleOffset', angleOffset)
        do iv = 1, nview
          tilt(iv) = tilt(iv) + angleOffset
        enddo

c         Get skip or breaking list
        ierr = PipGetString('SkipViews', listString)
        iz = PipGetString('BreakAtViews', listString)
        if (iz + ierr .eq. 0) call exitError(
     &      'YOU CANNOT BOTH SKIP VIEWS AND BREAK AT VIEWS')
        if (ierr + iz .eq. 1) then
          call parselist2(listString, listSkip, numSkip, nz)
          breaking = iz .eq. 0
        endif
      else
        write(*,'(1x,a,$)') 'Rotation angle FROM vertical TO the tilt axis: '
        read(5,*)rotangle

        print *,'Enter filter parameters to filter the correlation, or / for no filter'
        WRITE(6,1100)
1100    FORMAT(' Sigma1, Sigma2, Radius1, Radius2: ',$)
        READ(5,*) SIGMA1,SIGMA2,RADIUS1,RADIUS2
c         
        write(*,'(1x,a,$)')'1 to exclude central correlation peak due'
     &      //' to fixed pattern noise, 0 not to: '
        read(5,*)ifexclude
c         
        write(*,'(1x,a,$)')'Amounts to trim off each side in X and Y (/ for 0,0):'
        read(5,*)nxtrim,nytrim
        ixst = nxtrim
        ixnd = nx - 1 - nxtrim
        iyst = nytrim
        iynd = ny - 1 - nytrim
      endif

      radexcl=0.
      if(ifexclude.eq.1) radexcl=1.1

      if(ixst.lt.0.or.iyst.lt.0.or.ixnd.ge.nx.or.iynd.ge.ny.or.
     &    ixnd - ixst .lt. 24 .or. iynd - iyst .lt. 24) call exitError(
     &    'Impossible amount to trim by or incorrect coordinates')

      nxuse = ixnd + 1 - ixst
      nyuse = iynd + 1 - iyst
      cosphi = cosd(rotangle)
      sinphi = sind(rotangle)
      numBound = 0
      if (pipinput) then
c           
c         Now check if boundary model and load it in
        if (PipGetString('BoundaryModel', filin) .eq. 0) then
          iobjSeed = 0
          ierr = PipGetInteger('BoundaryObject', iobjSeed)
          call getModelAndFlags('OPENING BOUNDARY MODEL FILE')
          numPoints = 0
          do iobj = 1, max_mod_obj
            call objtocont(iobj,obj_color,imodobj,imodcont)
c             
c             Use specified object, or any object with closed contours
            if ((iobjSeed .gt. 0 .and. imodobj .eq. iobjSeed) .or. 
     &          (iobjSeed .eq. 0 .and. iobjFlags(imodobj) .eq. 0) .and.
     &          npt_in_obj(iobj) .gt. 2) then
              ipnt = abs(object(1 + ibase_obj(iobj)))
              iv = nint(p_coord(3,ipnt)) + 1
              if (iv .ge. 1 .and. iv .le. nview) then
                numBound = numBound + 1
                if (numBound .gt. limbound) call exitError(
     &              'TOO MANY BOUNDARY CONTOURS FOR ARRAYS')
                iobjBound(numBound) = iobj
                numInBound(numBound) = npt_in_obj(iobj)
                indBound(numBound) = numPoints
                numPoints = numPoints + npt_in_obj(iobj)
              endif
            endif
          enddo
          if (numBound .eq. 0) call exitError(
     &        'NO QUALIFYING BOUNDARY CONTOURS FOUND IN MODEL')
c
c           Allocate point array and copy points to arrays
          allocate(xbound(numPoints), ybound(numPoints), stat=ierr)
          call memoryError(ierr, 'ARRAYS FOR BOUNDARY CONTOURS')
          boundXmin = 1.e10
          boundXmax = -1.e10
          boundYmin = 1.e10
          boundYmax = -1.e10
          do ix = 1, numBound
            iobj = iobjBound(ix)
            ipnt = abs(object(1 + ibase_obj(iobj)))
            iv = nint(p_coord(3,ipnt)) + 1
            do ipt = 1, npt_in_obj(iobj)
              ipnt = abs(object(ipt + ibase_obj(iobj)))
              call adjustCoord(tilt(iv), 0., p_coord(1,ipnt) - nx/2.,
     &            p_coord(2,ipnt) - ny/2., x0, y0)
              xbound(indBound(ix)+ipt) = x0 + nx/2.
              ybound(indBound(ix)+ipt) = y0 + ny/2.
              boundXmin = min(boundXmin, x0 + nx/2.)
              boundXmax = max(boundXmax, x0 + nx/2.)
              boundYmin = min(boundYmin, y0 + ny/2.)
              boundYmax = max(boundYmax, y0 + ny/2.)
            enddo
          enddo
          deallocate(iobjFlags)
        endif
c         
c         Get the view range and the minimum tilt view - needed for evaluating patches
        ierr = PipGetTwoIntegers('StartingEndingViews', izst, iznd)
        call findMinimumTiltView()
c         
c         Now check if doing patches and set up regular grid of them
        if (PipGetTwoIntegers('SizeOfPatchesXandY', nxPatch, nyPatch) .eq.0)
     &      then
          if (ifCumulate .ne. 0) call exitError(
     &        'YOU CANNOT USE CUMULATIVE CORRELATION WITH PATCH TRACKING')
          if (breaking) call exitError('YOU CANNOT BREAK AT VIEWS WITH PATCH TRACKING')
          tracking = .true.
          if (nxPatch .gt. nxuse .or. nyPatch .gt. nyuse)
     &        call exitError('PATCHES DO NOT FIT WITHIN TRIMMED AREA OF IMAGE')
          ierr =PipGetTwoIntegers('NumberOfPatchesXandY', numXpatch, numYpatch)
          ix = PipGetTwoFloats('OverlapOfPatchesXandY', fracXover, fracYover)
          iobjSeed = PipGetString('SeedModel', filin)
          if (ierr + iobjSeed + ix .lt. 2) call exitError('YOU MUST ENTER'//
     &        ' ONLY ONE OF THE -number, -overlap, OR -seed OPTIONS')
          if (iobjSeed .ne. 0) then
c             
c               Specify regular array of patches, either by number
            if (ierr .eq. 0) then
              if (numXpatch .lt. 1 .or. numYpatch .lt. 1) call exitError(
     &            'NUMBER OF PATCHES MUST BE POSITIVE')
            else
c               
c               Or by overlap factors
              if (fracXover .gt. fracOverMax .or.fracYover .gt. fracOverMax)
     &            call exitError('FRACTIONAL OVERLAP BETWEEN PATCHES IS TOO HIGH')
              xOverlap = fracXover * nxPatch
              yOverlap = fracYover * nyPatch
              numXpatch=max(1, nint((nxuse - xOverlap) / (nxPatch - xOverlap)))
              numYpatch=max(1, nint((nyuse - yOverlap) / (nyPatch - yOverlap)))
            endif
            numPatches = numXpatch * numYpatch
            if (numPatches .gt. limpatch) call exitError('TOO MANY PATCHES FOR ARRAYS')
            xOverlap = (numXpatch * nxPatch - nxuse) / max(1., numXpatch -1.)
            yOverlap = (numYpatch * nyPatch - nyuse) / max(1., numYpatch -1.)
            do j = 1, numYpatch
              yval = iyst + (j - 1) * (nyPatch - yOverlap) + 0.5 * nyPatch
              if (numYpatch .eq. 1) yval = (iynd + 1 + iyst) / 2.
              do i = 1, numXpatch
                ind = i + (j-1)*numXpatch
                patchCenX(ind) = ixst + (i - 1) * (nxPatch - xOverlap) +
     &              0.5 * nxPatch
                if (numXpatch .eq. 1) patchCenX(ind) = (ixnd + 1 + ixst) / 2.
                patchCenY(ind) = yval
              enddo
            enddo
          else
c
c             Or get a seed model to specify patches
            ierr = PipGetInteger('SeedObject', iobjSeed)
            call getModelAndFlags('OPENING SEED MODEL FILE')
            numPatches = 0
            do iobj = 1, max_mod_obj
              call objtocont(iobj,obj_color,imodobj,imodcont)
c               
c               Use specified object, or any object with scattered points
              if ((iobjSeed .gt. 0 .and. imodobj .eq. iobjSeed) .or. 
     &            (iobjSeed .eq. 0 .and. iobjFlags(imodobj) .eq. 2)) then
                do ipt = 1, npt_in_obj(iobj)
                  ipnt = abs(object(ipt + ibase_obj(iobj)))
                  iv = nint(p_coord(3,ipnt)) + 1
c                   
c                   Get point down to zero degrees and make sure patch fits
                  if (iv .ge. 1 .and. iv .le. nview) then
                    call adjustCoord(tilt(iv), 0., p_coord(1,ipnt) - nx/2.,
     &                  p_coord(2,ipnt) - ny/2., x0, y0)
                    x0 = x0 + nx/2.
                    y0 = y0 + ny/2.
                    if (nint(x0) - nxPatch / 2 .ge. ixst .and.
     &                  nint(x0) + nxPatch / 2 .le. ixnd .and.
     &                  nint(y0) - nyPatch / 2 .ge. iyst .and.
     &                  nint(y0) + nyPatch / 2 .le. iynd) then

                      numPatches = numPatches + 1
                      if (numPatches .gt.limpatch) call exitError(
     &                    'TOO MANY POINTS IN SEED MODEL FOR ARRAYS')
                      patchCenX(numPatches) = x0
                      patchCenY(numPatches) = y0
                    endif
                  endif
                enddo
              endif
            enddo
            if (numPatches .eq. 0) call exitError('NO QUALIFYING POINTS FOUND'
     &          //' IN SEED MODEL; SPECIFY OBJECT OR MAKE IT SCATTERED POINTS')
            deallocate(iobjFlags)
          endif
c           
c           Now eliminate patches outside boundary model
          if (numBound .gt. 0) then
c             
c             Evaluate each patch for fraction inside boundary
            ind = 0
            do ipatch = 1, numPatches
              numInside = 0
              do ix = 1, 32
                x0 = patchCenX(ipatch) + nxPatch * (ix - 16.5) /32.
                do iy = 1, 32
                  y0 = patchCenY(ipatch) + nyPatch * (iy - 16.5) /32.
                  do iv = 1, numBound
                    if (inside(xbound(indBound(iv)+1), ybound(indBound(iv)+1),
     &                  numInBound(iv), x0, y0)) then
                      numInside = numInside + 1
                      exit
                    endif
                  enddo
                enddo
              enddo
c              print *, patchCenX(ipatch),patchCenY(ipatch),numInside / 1024.
c               
c               If enough points are inside, keep the patch
              if (numInside / 1024. .ge. critInside) then
                ind = ind + 1
                patchCenX(ind) = patchCenX(ipatch)
                patchCenY(ind) = patchCenY(ipatch)
              endif
            enddo
            numPatches = ind
            if (ind .eq. 0) call exitError(
     &          'NO PATCHES ARE SUFFICIENTLY INSIDE THE BOUNDARY CONTOUR(S)')
          endif
c         
c           Get prealign transforms, then eliminate patches that have too much blank area
c           on starting view 
          ifReadXfs = 1 - PipGetString('PrealignmentTransformFile', plfile)
          if (ifReadXfs .ne. 0) then
            ierr = PipGetInteger('ImagesAreBinned', imagesBinned)
            allocate(dxpre(nz), dypre(nz), stat=ierr)
            call memoryError(ierr, 'ARRAYS FOR PREALIGN TRANSFORMS')
            call dopen(3, plfile, 'ro', 'f')
            call xfrdall2(3, f, iv, nz, ierr)
            if (ierr .eq. 2) call exitError('READING TRANSFORM FILE')
            if (iv .ne. nz) call exitError(
     &          'NOT ENOUGH TRANSFORMS IN PREALIGN TRANSFORM FILE')
            dxpre(1:nz) = f(1,3,1:nz) / imagesBinned
            dypre(1:nz) = f(2,3,1:nz) / imagesBinned
c
c             Scan around the minimum tilt for the view with the most patches left
            numBest = 0
            do iv = minTilt - 2, minTilt + 2
              if (iv .ge. izst .and. iv .le. iznd .and.
     &            (breaking .or. numberInList(iv, listSkip, numSkip, 0) .eq. 0)) then
                call markUsablePatches(iv, ind)
                if (verbose) print *,'Usable patches for view: ', iv, ind
                if (ind .gt. numBest .or. (ind .eq. numBest .and.
     &              abs(iv - minTilt) .lt. abs(indBest - minTilt))) then
                  numBest = ind
                  indBest = iv
                endif
              endif
            enddo
c             
c             Mark them for real and copy the usable ones down
            minTilt = indBest
            call markUsablePatches(minTilt, ind)
            ind = 0
            do ipatch = 1, numPatches
              if (tmprray(ipatch) .gt. 0) then
                ind = ind + 1
                patchCenX(ind) = patchCenX(ipatch)
                patchCenY(ind) = patchCenY(ipatch)
              endif
            enddo
            numPatches = ind
            if (ind .eq. 0) call exitError(
     &          'NO PATCHES HAVE SUFFICIENT IMAGE DATA NEAR THE MINIMUM TILT VIEW')
          endif

          nxuse = nxPatch
          nyuse = nyPatch
        elseif (numBound .gt. 0) then
c           
c           For ordinary correlation with boundary model, adjust ixst etc
          if (boundXmin - 2. .gt. ixst) ixst = boundXmin - 2.
          if (boundXmax + 2. .lt. ixnd) ixnd = ceiling(boundXmax + 2.)
          if (boundYmin - 2. .gt. iyst) iyst = boundYmin - 2.
          if (boundYmax + 2. .lt. iynd) iynd = ceiling(boundYmax + 2.)
          nxuse = ixnd + 1 - ixst
          nyuse = iynd + 1 - iyst
          if (nxuse .lt. 24 .or. nyuse .lt. 24) call exitError(
     &        'REGION INSIDE BOUNDARY IS TOO SMALL')
          allocate(xtfsBound(numPoints), ytfsBound(numPoints), stat=ierr)
          call memoryError(ierr, 'TFS BOUNDARY ARRAYS')
          write(*,'(a,2i7,a,2i7)')'The area loaded will be X:',ixst,ixnd,' Y:', iyst,iynd
        endif
      endif
c       
c       Set up one patch if no tracking
      if (tracking) then
        print *,numPatches,' patches will be tracked'
      else
        numPatches = 1
        patchCenX(1) = (ixnd + 1 + ixst) / 2.
        patchCenY(1) = (iynd + 1 + iyst) / 2.
      endif
c
c       OK, back to main image operations
c       determine padding
c       
      nxbord = max(5,min(20,nint(0.05*nxuse)))
      nybord = max(5,min(20,nint(0.05*nyuse)))
      if (pipinput) then
        ierr = PipGetTwoIntegers('PadsInXandY', nxbord, nybord)
      else
        write(*,'(1x,a,2i4,a,$)') 'Amounts to pad images on each side '
     &      //'in X and Y (/ for',nxbord,nybord,'): '
        read(*,*)nxbord,nybord
      endif
c       
c       get a binning based on the padded size so that large padding is
c       possible
c       
      if (nbin .eq. 0) then
        nbin=(max(nxuse+2*nxbord,nyuse+2*nybord) + maxbinsize-1)/maxbinsize
c         
c         If the binning is bigger than 4, find the minimum binning needed 
c         to keep the used image within bounds
        if (nbin .gt. 4) then
          nbin = 0
          i = 4
          do while (i .le. maxbinning .and. nbin .eq. 0)
            if ((niceframe((nxuse+2*nxbord)/i,2,19) + 2) *
     &          niceframe((nyuse+2*nybord)/i,2,19) .lt. idim2) nbin = i
            i = i + 1
          enddo
          if (nbin .eq. 0) call exitError('IMAGE AREA TOO'//
     &        ' LARGE FOR ARRAYS; INCREASE THE BORDER TO TRIM OFF')
        endif
      else if ((niceframe((nxuse + 40)/nbin,2,19) + 2) *
     &      niceframe((nyuse + 40)/nbin,2,19) .gt. idim2) then
        call exitError('IMAGE AREA TOO'//
     &      ' LARGE; INCREASE THE BINNING OR THE BORDER TO TRIM OFF')
      endif
        
      nxusebin=nxuse/nbin
      nyusebin=nyuse/nbin

      nxpad=niceframe((nxuse+2*nxbord)/nbin,2,19)
      nypad=niceframe((nyuse+2*nybord)/nbin,2,19)
      if((nxpad+2)*nypad.gt.idim2) call exitError(
     &    'PADDED IMAGE TOO BIG, TRY LESS PADDING')

      write(*,'(/,a,i3,a,i5,a,i5)')' Binning is',nbin,
     &    ';  padded, binned size is',nxpad,' by', nypad
c       
c       Now that padded size exists, get the filter ctf
c       
      call setctfwsr(sigma1,sigma2,radius1,radius2,ctfp,nxpad,nypad,deltap)
c       
c       Set up tapering
c       
      nxtap = max(5,min(100,nint(0.1*nxuse)))
      nytap = max(5,min(100,nint(0.1*nyuse)))
      if (pipinput) then
        ierr = PipGetTwoIntegers('TapersInXandY', nxtap, nytap)
      else
        write(*,'(1x,a,2i4,a,$)') 'Widths over which to taper images'
     &      //' in X and Y (/ for',nxtap,nytap,'): '
        read(*,*)nxtap,nytap
      endif
      nxtap=nxtap/nbin
      nytap=nytap/nbin
      limitShiftX = (limitShiftX  + nbin / 2) / nbin
      limitShiftY = (limitShiftY  + nbin / 2) / nbin
      if (limitShiftX .le. 0 .or. limitShiftY .le. 0) call exitError(
     &    'SHIFT LIMITS MUST BE POSITIVE')
c       
c       Get view range in old sequential input (needed earlier when pip)
      if (.not. pipinput) then
        write(*,'(1x,a,$)') 'Starting and ending views to do (first is 1), or / for all: '
        read(*,*)izst,iznd
        call findMinimumTiltView()
      endif
c       
c       Get max tilt angle and check for appropriateness of cosine stretch
      usemax = 0.
      do iv = izst, iznd
        usemax = max(usemax, abs(tilt(iv)))
      enddo
      if (ifNoStretch .eq. 0 .and. usemax .gt. cosStrMaxTilt) call exitError
     &    ('MAXIMUM TILT ANGLE IS TOO HIGH TO USE COSINE STRETCHING')
c       
      do kk=1,nz
        call xfunit(f(1,1,kk),1.0)
      enddo
c       
      if (tracking) then
        nFillTaper = min(nxPatch / (4 * nbin), nyPatch / (4 * nbin), 100, max(10,
     &      nint(fillTaperFrac * (nxPatch + nyPatch) / 2.) / nbin))
        allocate(xmodel(numPatches, izst:iznd), ymodel(numPatches, izst:iznd),
     &      stat = ierr)
        call memoryError(ierr, 'ARRAYS FOR TRACKED POINTS')
        xmodel = -1.e10                         ! Vector operations
        ymodel = -1.e10
        if (lenContour .le. 0) lenContour = iznd + 1 - izst
        numCont = (iznd - izst) / (lenContour - minContOverlap) + 1
        if (numCont .gt. max_obj_num) call exitError('TOO MANY CONTOURS FOR MODEL ARRAYS')
        if (numCont * lenContour .gt. max_pt) call exitError(
     &      'TOO MANY TOTAL POINTS FOR MODEL ARRAYS')
      endif
c
      if(imfilout.ne.' ')then
        nout=iznd-izst
        if(ifimout.ne.0)nout=nout*3
        call ialsiz_sam_cel(3,nxpad,nypad,nout)
        dmsum=0.
        dmax=-1.e10
        dmin=1.e10
        nout = 0
      endif
c       
c       get centered starting and ending coordinates to which box offsets
c       will be added for loading
c       
      ixstCen = (nx - nxuse) / 2
      ixndCen = ixstCen + nxuse - 1
      iystCen = (ny - nyuse) / 2
      iyndCen = iystCen + nyuse - 1
c       
c       Report axis offset if leaving axis at box
      xBoxOfs = (ixnd + 1 + ixst - nx) / 2.
      yBoxOfs = (iynd + 1 + iyst - ny) / 2.
      if (ifLeaveAxis .ne. 0) write(*,'(/,a,f8.1,a)')
     &    ' The tilt axis is being left at a shift of',
     &    xBoxOfs * cosphi + yboxOfs * sinphi,' pixels from center'

c       print *,xBoxOfs,yBoxOfs,ixstCen,ixndCen,iystCen,iyndCen
c       
c       set up for one forward loop through data - modified by case below
c       
      nloops = 1
      loopDir = 1
c       
c       set up for first or only loop
c       
c      print *,mintilt, izst,iznd
      if (minTilt .ge. iznd) then
        ivStart = iznd - 1
        ivEnd = izst
        loopDir = -1
      else if (minTilt .lt. iznd .and. minTilt .gt. izst) then
        ivStart = minTilt + 1
        ivEnd = iznd
        nloops = 2
      else
        ivStart = izst + 1
        ivEnd = iznd
      endif

      do iloop = 1, nloops
        do i = 1, nxusebin * nyusebin
          sumray(i) = 0.
        enddo
        usdx = 0.
        usdy = 0.
        xpeak = 0.
        ypeak = 0.
        cumXshift = 0.
        cumYshift = 0.
        lastNotSkipped = minTilt
        call xfunit(funit,1.0)

        DO iview=ivStart, ivEnd, loopDir

          ivCur = iview
          ivRef = iview - loopDir
c           
c           Test for skipping of this view, or of skipping of previous view when going
c           backwards and breaking
          ivSkip = iview
          if (breaking .and. loopDir .lt. 0) ivSkip = iview + 1
c           
c           If view is on skip/break list, copy the previous cumulative transform
          if (numberInList(ivSkip, listSkip, numSkip, 0) .ne. 0) then
            call xfcopy(f(1,1,ivRef), f(1,1,ivCur))
            cycle
          endif
c           
c           Align to last one not skipped unless breaking alignment, then revise last
          if (numSkip .gt. 0 .and. .not. breaking) ivRef = lastNotSkipped
          lastNotSkipped = iview
          cosview = cosd(tilt(iview))
c           
c           get the stretch - if its less than 1., invert everything
c           unless doing cumulative or tracking, where it has to do in order
c           DNM 9/24/09: It seems like all kinds of things may not work if this
c           inversion ever happens...  9/23/11: basic correlation works but not tracking
          idir=1
          stretch = 1.
          if (ifNoStretch .eq. 0 .and. abs(cosview) .gt. 0.01) then
            stretch=cosd(tilt(ivRef))/cosview
            if (ifcumulate .ne. 0 .and. ifAbsStretch .ne. 0)
     &          stretch=cosd(tilt(minTilt))/cosview
          endif
          if (stretch .lt. 1. .and. ifcumulate .eq. 0 .and. .not. tracking) then
            idir=-1
            stretch=1./stretch
            iztmp = ivCur
            ivCur = ivRef
            ivRef = iztmp
            cosview = cosd(tilt(ivCur))
          endif
          if (verbose)
     &        print *,'idir, stretch, ivRef, ivCur', idir, stretch, ivRef, ivCur
c           
c           Loop on the patches
          ivRefBase  = ivRef
          do ipatch = 1, numPatches
            ivRef = ivRefBase
c             
c             Get box offset, first by tilt-foreshortened center position
c             rounded to nearest integer
            cenx = patchCenX(ipatch) - nx/2.
            ceny = patchCenY(ipatch) - ny/2.
            call adjustCoord(0., tilt(ivRef), cenx, ceny, x0, y0)
            ixBoxRef = max(-ixstCen, min(nx - 1 - ixndCen, nint(x0)))
            iyBoxRef = max(-iystCen, min(ny - 1 - iyndCen, nint(y0)))
            baseTilt = 0.
            if (iview .eq. ivStart) then
              ixBoxStart = ixBoxRef
              iyBoxStart = iyBoxRef
            endif
c             
c             If tracking, initialize model position on first time or get  reference
c             box from model point
            if (tracking) then
              if (iloop .eq. 1 .and. iview .eq. ivStart) then
                xmodel(ipatch, ivRef) = ixBoxRef + ixstCen + nxuse / 2.
                ymodel(ipatch, ivRef) = iyBoxRef + iystCen + nyuse / 2.
              else
c                 
c                 Back up reference to last point that was tracked unless it is too
c                 far back
                do while (xmodel(ipatch, ivRef) .lt. -1.e9 .and.
     &              abs(ivRef - ivCur) .le. maxTrackGap)
                  ivRef = ivRef - loopDir
                enddo
                if (xmodel(ipatch, ivRef) .lt. -1.e9) then
                  if (verbose) print *,'Stopping tracking of patch #',ipatch
                  cycle
                endif
                if (ivRef .ne. ivRefBase) then
                  if (verbose) print *,'Patch ',ipatch,' tracking to reference', ivRef
                  stretch = 1.
                  if (ifNoStretch .eq. 0 .and. abs(cosview) .gt. 0.01)
     &                stretch=cosd(tilt(ivRef))/cosview
                endif
c                 
c                 Center the reference box on the model point
                x0 = xmodel(ipatch, ivRef) - (ixstCen + nxuse / 2.)
                y0 = ymodel(ipatch, ivRef) - (iystCen + nyuse / 2.)
                ixBoxRef = max(-ixstCen, min(nx - 1 - ixndCen, nint(x0)))
                iyBoxRef = max(-iystCen, min(ny - 1 - iyndCen, nint(y0)))
                cenx = ixBoxRef
                ceny = iyBoxRef
                basetilt = tilt(ivRef)
              endif
            endif
c             
c             Now get box offset of current view by adjusting either the zero
c             degree position (so this will match old behavior of program)
c             or the box offset on reference view
            call adjustCoord(basetilt, tilt(ivCur), cenx, ceny, x0, y0)
            ixBoxCur = max(-ixstCen, min(nx - 1 - ixndCen, nint(x0)))
            iyBoxCur = max(-iystCen, min(ny - 1 - iyndCen, nint(y0)))
            if (verbose) print *,'Box offsets',ipatch,ixboxref,iyboxref,ixboxcur,iyboxcur
c           
c             Now that reference and stretch is fixed, get a limited cosine ratio to
c             use below, get the transforms and look up the Z's in file
            cosRatio = abs(cosview) / max(abs(cosview), abs(cosd(tilt(ivRef))), 1.e-6)
            call rotmagstr_to_amat(0.,1.,stretch,rotangle,fs)
            fs(1,3)=0.
            fs(2,3)=0.
            call xfinvert(fs,fsinv)
            do i=1,nz
              if(izpclist(i)+1.eq.ivRef)izlast=i-1
              if(izpclist(i)+1.eq.ivCur)izcur=i-1
            enddo
c           print *,izlast,izcur,stretch
c             
c             If tracking and prexg's are available, evaluate need for tapering and
c             skip the patch if it has below criterion image data
            taperRef = .false.
            taperCur = .false.
            if (ifReadXfs .ne. 0) then
              ix = min(nx -2, nx + nint(dxPre(ivRef)) - 2, ixstCen + ixBoxRef + nxPatch) -
     &            max(2, nint(dxPre(ivRef)) + 2, ixstCen + ixBoxRef)
              iy = min(ny -2, ny + nint(dyPre(ivRef)) - 2, iystCen + iyBoxRef + nyPatch) -
     &            max(2, nint(dyPre(ivRef)) + 2, iystCen + iyBoxRef)
              taperRef = ix .lt. nxPatch .or. iy .lt. nyPatch
              ix = min(nx -2, nx + nint(dxPre(ivCur)) - 2, ixstCen + ixBoxCur + nxPatch) -
     &            max(2, nint(dxPre(ivCur)) + 2, ixstCen + ixBoxCur)
              iy = min(ny -2, ny + nint(dyPre(ivCur)) - 2, iystCen + iyBoxCur + nyPatch) -
     &            max(2, nint(dyPre(ivCur)) + 2, iystCen + iyBoxCur)
              taperCur = ix .lt. nxPatch .or. iy .lt. nyPatch
              if (ix .lt. 0 .or. iy .lt. 0 .or.
     &            ix * iy .lt. critNonBlank * nxPatch * nyPatch) then
                if (verbose) print *,'Skipping tracking of patch', ipatch
                cycle
              endif
              if (verbose .and. taperRef) print *,'Tapering reference patch #', ipatch
              if (verbose .and. taperCur) print *,'Tapering current patch #', ipatch
                
            endif
            xpeakFrac = 0.
            ypeakFrac = 0.
c             
c             If correlating inside boundary contour, transform contours
            if (.not. tracking .and. numBound .gt. 0) then
              do ix = 1, numBound
                xtfsBmin(ix) = 1.e10
                xtfsBmax(ix) = -1.e10
                ytfsBmin(ix) = 1.e10
                ytfsBmax(ix) = -1.e10
                do j = 1, numInBound(ix)
                  ind = j + indBound(ix)
                  call adjustCoord(0., tilt(ivCur), xbound(ind) - nx/2.,
     &                ybound(ind) - ny/2., x0, y0)
                  xtfsBound(ind) = (x0 + nx / 2. - (ixstCen+ixBoxCur)) / nbin
                  ytfsBound(ind) = (y0 + ny / 2. - (iystCen+iyBoxCur)) / nbin
                  xtfsBmin(ix) = min(xtfsBmin(ix), xtfsBound(ind))
                  ytfsBmin(ix) = min(ytfsBmin(ix), ytfsBound(ind))
                  xtfsBmax(ix) = max(xtfsBmax(ix), xtfsBound(ind))
                  ytfsBmax(ix) = max(ytfsBmax(ix), ytfsBound(ind))
                enddo
c                print *,ix,xtfsBmin(ix),xtfsBmax(ix),ytfsBmin(ix),ytfsBmax(ix)
              enddo
            endif
c             
            do iter = 1,niter
c               
c               get "current" into array, stretch into brray, pad it
C               
              call irdbinned(1,izcur, array, nxusebin, nyusebin, ixstCen+ixBoxCur,
     &            iystCen+iyBoxCur, nbin, nxusebin, nyusebin, tmprray, lentemp, ierr)
              if (ierr .ne. 0) goto 99
              if (taperCur) then
                if (taperAtFill(array, nxusebin, nyusebin, nFillTaper, 1) .ne. 0) go to 97
              endif
c               
c               7/11/03: We have to feed the interpolation the right mean or
c               it will create a bad edge mean for padding
c               
              call iclden(array,nxusebin,nyusebin,1,nxusebin,1,nyusebin,
     &            usemin, usemax, usemean)
              if (.not. tracking .and. numBound .gt. 0) then
                wallstart = walltime()
                call maskOutsideBoundaries(array, nxusebin, nyusebin)
                wallmask = wallmask + walltime() - wallstart
              endif
              wallstart = walltime()
              call cubinterp(array,brray,nxusebin,nyusebin,nxusebin,nyusebin, fs,
     &            nxusebin/2., nyusebin/2.,xpeakFrac, ypeakFrac ,1., usemean, 0)
              wallinterp = wallinterp + walltime() - wallstart
              call taperinpad(brray,nxusebin,nyusebin,brray,nxpad+2,nxpad,
     &            nypad, nxtap, nytap)
c             
              call meanzero(brray,nxpad+2,nxpad,nypad)
c           
c               get "last" into array, just pad it there
c               
              call irdbinned(1,izlast, array, nxusebin, nyusebin, ixstCen+ixBoxRef,
     &            iystCen+iyBoxRef, nbin, nxusebin, nyusebin, tmprray, lentemp, ierr)
              if (ierr .ne. 0) goto 99
              if (taperRef) then
                if (taperAtFill(array, nxusebin, nyusebin, nFillTaper, 1) .ne. 0) go to 97
              endif
              if (ifcumulate .ne. 0) then
                if (iter .eq. 1) then
c             
c                   if accumulating, transform image by last shift, add it to
c                   sum array, then taper and pad into array
c             
                  if (ifAbsStretch .eq. 0) then 
                    call xfunit(funit,1.0)
                  else
                    usdx = xpeak
                    usdy = ypeak
                  endif
                  if (verbose)
     &                print *,'cumulating usdx,usdy,xpeak,ypeak', usdx,usdy,xpeak,ypeak
                  call iclden(array,nxusebin,nyusebin,1,nxusebin,1,nyusebin,
     &                usemin, usemax, usemean)
                  call cubinterp(array,crray,nxusebin,nyusebin,nxusebin, nyusebin,
     &                funit, nxusebin/2., nyusebin/2.,usdx,usdy ,1., usemean, 0)
                  do i = 1, nxusebin * nyusebin
                    sumray(i) = sumray(i) + crray(i)
                  enddo
                endif
                call taperinpad(sumray,nxusebin,nyusebin,array,nxpad+2,
     &              nxpad,nypad, nxtap, nytap)
              else
                call taperinpad(array,nxusebin,nyusebin,array,nxpad+2,
     &              nxpad,nypad, nxtap, nytap)
              endif
              if(ifimout.ne.0)then
                do isout=1,2
                  if(isout.eq.1)then
                    call irepak(crray,array,nxpad+2,nypad,
     &                  0,nxpad-1,0,nypad-1)
                  else
                    call irepak(crray,brray,nxpad+2,nypad,
     &                  0,nxpad-1,0,nypad-1)
                  endif
                  if(mode.ne.2)then
                    CALL IsetDN(crray,NXpad,NYpad,MODE,1,NXpad,1,NYpad,DMIN2,
     &                  DMAX2,DMEAN3)
                  else
                    CALL IclDeN(crray,NXpad,NYpad,1,NXpad,1,NYpad,DMIN2,DMAX2,
     &                  DMEAN3)
                  endif
                  CALL IWRSEC(3,crray)
                  nout = nout + 1
C               
                  DMAX = max(dmax,dmax2)
                  DMIN = min(dmin,dmin2)
                  DMsum = dmsum + dmean3
                enddo
              endif
              call meanzero(array,nxpad+2,nxpad,nypad)

C           
c               print *,'taking fft'
              wallstart = walltime()
              call todfft(array,nxpad,nypad,0)
              call todfft(brray,nxpad,nypad,0)
c               
c               multiply array by complex conjugate of brray, put back in array
c               
c               print *,'multiplying arrays'
              call conjugateProduct(array, brray, nxpad, nypad)
c               
              if(deltap.ne.0.)call filterpart(array,array,nxpad,nypad,ctfp,
     &            deltap)
c               print *,'taking back fft'
              call todfft(array,nxpad,nypad,1)
              wallfft = wallfft + walltime() - wallstart
c               
c               the factor here determines the balance between accepting a
c               spurious peak and rejecting a real peak because it falls in
c               the streak. The spurious peak could be as far as 0.5 out but
c               is much more likely to be within 0.25 of the maximum
c               displacement
c           
              streak=0.25*(stretch-1.0)*nxuse
              call peakfind(array,nxpad+2,nypad,xpeaktmp,ypeaktmp,peakval,
     &            radexcl, rotangle, streak, limitShiftX, limitShiftY)
              xpeakcum = xpeaktmp + xpeakFrac
              xpeakFrac = xpeakcum - nint(xpeakcum)
              ypeakcum = ypeaktmp + ypeakFrac
              ypeakFrac = ypeakcum - nint(ypeakcum)
              if (verbose) write(*,'(i3,2f8.2,g15.6,4f8.2)')iter,xpeakcum,ypeakcum,
     &            peakVal, xpeaktmp-nint(xpeaktmp),ypeaktmp-nint(ypeaktmp),
     &            xpeakFrac,ypeakFrac
c               
c               Skip out and restore last peak if the peak strength is less
              if (iter .gt. 1 .and. peakval .lt. peakLast) then
                xpeakcum = xpeakLast
                ypeakcum = ypeakLast
                peakVal = peakLast
                exit
              endif 
              xpeakLast = xpeakCum
              yPeakLast = ypeakCum
              peakLast = peakVal
c               
c               Skip out if we are close to 0 interpolation
c               (gfortran did not like exit on the if statement)
              if (sqrt((xpeaktmp - nint(xpeaktmp))**2 +
     &            (ypeaktmp - nint(ypeaktmp))**2) .lt. peakFracTol) then
                exit
              endif
            enddo
            xpeak = xpeakcum
            ypeak = ypeakcum
c           
c             DNM 5/2/02: only destretch the shift if the current view was
c             stretched.  Also, put the right sign out in the standard output
c           
            if (idir.gt.0)then
              call xfapply(fsinv,0.,0.,xpeak,ypeak,usdx,usdy)
            else
              usdx=xpeak
              usdy=ypeak
            endif
            if (verbose) print *,'peak usdx,usdy,xpeak,ypeak', usdx,usdy,xpeak,ypeak
c           
c             compensate for the box offsets of reference and current view,
c             where the reference is the starting view if accumulating
c             
            ixBoxForAdj = ixBoxRef
            iyBoxForAdj = iyBoxRef
            if (ifcumulate .ne. 0) then
              ixBoxForAdj = ixBoxStart
              iyBoxForAdj = iyBoxStart
            endif
               
c            ivRef = iview - loopDir
c            if (ifcumulate .ne. 0) ivRef = ivStart - loopDir
            xshift = idir*nbin*usdx + ixBoxForAdj - ixBoxCur
            yshift = idir*nbin*usdy + iyBoxForAdj - iyBoxCur
            if (tracking) then
c           
c               compensate the shift that is to be accumulated for the
c               difference between the center of the box and the model point
c               on reference view
              cumXcenter = xmodel(ipatch, ivRef)
              cumYcenter = ymodel(ipatch, ivRef)
              xModOffset = cumXcenter - (ixBoxRef + ixstCen + nxuse / 2.)
              yModOffset = cumYcenter - (iyBoxRef + iystCen + nyuse / 2.)
              cumXrot = xModOffset * cosphi + yModOffset * sinphi
              xAdjust = cumXrot *  (1. - cosRatio)
c               
c               Subtract adjusted shift to get new model point position
              xmodel(ipatch, iview) = cumXcenter - (xshift + xAdjust * cosphi)
              ymodel(ipatch, iview) = cumYcenter - (yshift + xAdjust * sinphi)
            endif
c           
c           if not leaving axis at center of box, compute amount that
c           current view must be shifted across tilt axis to be lined up at
c           center, then rotate that to get shifts in X and Y
c           This is not due to cosine stretch, it is due to difference in
c           the box offsets
            if (ifLeaveAxis .eq. 0) then
              xBoxOfs = (ixBoxCur - ixBoxForAdj) * cosphi +
     &            (iyBoxCur - iyBoxForAdj) * sinphi
              xshift = xshift + xBoxOfs * cosphi
              yshift = yshift + xBoxOfs * sinphi
            endif
c           
c             If not cumulative, adjust the shift to bring the tilt axis
c             to the true center from the center of last view
c             If last view was shifted to right to line up with center,
c             then the true center is to the left of the center of that view
c             and the tilt axis (and this view) must be shifted to the left
c             But base this on the cumulative shift of the center of the image
c             not of the box if axis not at center: so add on the cumulative
c             amount that would have been added due to box offsets in block
c             above
            if (ifcumulate .eq. 0 .and. ifNoStretch .eq. 0) then
              xFromCen = cumXshift
              yFromCen = cumYshift
              if (ifLeaveAxis .ne. 0) then
                xFromCen = xFromCen + (ixBoxRef - ixBoxStart)
                yFromCen = yFromCen + (iyBoxRef - iyBoxStart)
              endif
              cumXrot = xFromCen * cosphi + yFromCen * sinphi
              xAdjust = cumXrot *  (cosRatio - 1.)
              xshift = xshift + xAdjust * cosphi
              yshift = yshift + xAdjust * sinphi
c               
c               Add to cumulative shift and report and save the absolute shift
c               
              cumXshift = cumXshift + xshift
              cumYshift = cumYshift + yshift
              xshift = cumXshift
              yshift = cumYshift
            endif
          
            if (.not.tracking) write(*,111)'View',iview,', shifts',xshift,
     &          yshift,'      peak',peakVal
111         format(a,i4,a,2f10.2,a,g15.6)
c           
c           DNM 10/22/03: Only do flush for large stacks because of problem
c           inside shell scripts in Windows/Intel
c           
            if (iznd - izst .gt. 2) call flush(6)
            f(1,3,iview)=xshift
            f(2,3,iview)=yshift
            call xfcopy(fs, funit)
            if(imfilout.ne.' ')then
              call packcorr(crray,array,nxpad+2,nypad)
              if(mode.ne.2)then
                CALL IsetDN(crray,NXpad,NYpad,MODE,1,NXpad,1,NYpad,DMIN2,
     &              DMAX2,DMEAN3)
              else
                CALL IclDeN(crray,NXpad,NYpad,1,NXpad,1,NYpad,DMIN2,DMAX2,
     &              DMEAN3)
              endif
              CALL IWRSEC(3,crray)
              nout = nout + 1
              if (verbose) print *,'Correlation output at Z = ', nout
C             
              DMAX = max(dmax,dmax2)
              DMIN = min(dmin,dmin2)
              DMsum = dmsum + dmean3
            endif
          enddo
          if (tracking) write(*,111)'View',iview,' processed'
        enddo
c         
c         set up for second loop
c         
        ivStart = minTilt - 1
        ivEnd = izst
        loopDir = -1
      enddo
c       
      if(imfilout.ne.' ')then
        call ialsiz_sam_cel(3,nxpad,nypad,nout)
        dmean=dmsum/nout
        CALL DATE(DAT)
        CALL TIME(TIM)
c         
        if(deltap.ne.0.)fltrdp=', filtered'
        titstr='TILTXCORR: stack cosine stretch/correlated '// fltrdp
c         
        write(titlech,1500) titstr,DAT,TIM
        read(titlech,'(20a4)')(TITLE(kti),kti=1,20)
1500    FORMAT(A54,2x,A9,2X,A8)
        
        CALL IWRHDR(3,TITLE,1,DMIN,DMAX,DMEAN)
        call imclose(3)
      endif
c       
c       Now adjust transforms of skipped ones so they are always the same as the one below
      if (.not. breaking) then
        lastNotSkipped = -1
        do iv = 1, nview
          if (numberInList(iv, listSkip, numSkip, 0) .eq. 0) then
            lastNotSkipped = iv
          else if (lastNotSkipped .gt. 0) then
            call xfcopy(f(1,1,lastNotSkipped), f(1,1,iv))
          endif
        enddo
      endif
c       
c       Normal output
      if (.not. tracking) then
        call dopen(1, xffilout, 'new', 'f')
c       
c         If leaving axis at an offset box, output G transforms directly so that
c         the material in box at zero tilt will stay in box
        if (ifLeaveAxis .ne. 0) then
          do iv = 1,nz
            call xfwrite(1, f(1,1,iv))
          enddo
        else
c           
c           Anticipate what xftoxg will do, namely get to transforms with zero
c           mean, and shift tilt axis to be at middle in that case
c           
          iloop = 1
          cumXshift = 10.
          do while (iloop.le.10 .and. (cumXshift.gt.0.1 .or. cumYshift.gt.0.1)
     &        .and. ifNoStretch .eq. 0)
            iloop = iloop + 1
            cumXshift = 0.
            cumYshift = 0.
            do iv = 1, nz
              cumXshift = cumXshift - f(1,3,iv) / nz
              cumYshift = cumYshift - f(2,3,iv) / nz
            enddo
c             print *,iloop,', mean shift',cumXshift, cumYshift
c             
c             rotate the average shift to tilt axis vertical, adjust the X shift
c             to keep tilt axis in center and apply shift for all views
c           
            cumXrot = cumXshift * cosphi + cumYshift * sinphi
            yshift = -cumXshift * sinphi + cumYshift * cosphi
            do iv = 1,nz
              xAdjust = cumXrot * cosd(tilt(iv))
              f(1,3,iv) = f(1,3,iv) + xAdjust * cosphi - yshift * sinphi
              f(2,3,iv) = f(2,3,iv) + xAdjust * sinphi + yshift * cosphi
            enddo
          enddo
c           
c           convert from g to f transforms by taking differences
c           
          do iv = nz, 2, -1
            if (numberInList(iv, listSkip, numSkip, 0) .eq. 0) then
              f(1,3,iv) = f(1,3,iv) - f(1,3,iv - 1)
              f(2,3,iv) = f(2,3,iv) - f(2,3,iv - 1)
            else
              call xfunit(f(1,1,iv), 1.)
            endif
          enddo
          call xfunit(f(1,1,1), 1.)
          do iv=1,nz
            call xfwrite(1,f(1,1,iv),*96)
          enddo
        endif
        close(1)
      else
c         
c         Write Model of tracked points
        ierr = newimod()
        max_mod_obj = 0
        n_point = 0
c         
c         Go through patches and put points in model structure
        do ipatch = 1, numPatches
c           
c           Count model points in this patch and divide them up
          ipnt = 0
          ivbase = -1
          do iz = izst, iznd
            if (xmodel(ipatch, iz) .gt. -1.e9) then
              ipnt = ipnt + 1
              if (ivbase .lt. 0) ivbase = iz
            endif
          enddo
          lenConts = min(lenContour, ipnt)
          numCont = (ipnt - 1) / (lenConts - minContOverlap) + 1
          laptot = numCont * lenConts - ipnt
          lapbase = laptot / max(numCont - 1, 1)
          laprem = mod(laptot, max(numCont - 1, 1))
c           
c           Loop through each contour starting at base position
          do i = 1, numCont
            max_mod_obj = max_mod_obj + 1
            obj_color(1, max_mod_obj) = 1
            obj_color(2, max_mod_obj) = 255
            ibase_obj(max_mod_obj) = n_point
            npt_in_obj(max_mod_obj) = lenConts
            iv = ivbase
            ipnt = 0
            do j = 1, lenConts
              do while (xmodel(ipatch, iv) .lt. -1.e9 .and. iv .lt. iznd)
                iv = iv + 1
              enddo
              n_point = n_point + 1
              p_coord(1, n_point) = xmodel(ipatch, iv)
              p_coord(2, n_point) = ymodel(ipatch, iv)
              do iz = 1, nz
                if (izpclist(iz) + 1 .eq. iv) p_coord(3, n_point) = iz - 1
              enddo
              object(n_point) = n_point
              pt_label(n_point) = 0
              ipnt = ipnt + 1
c               
c               If hit point for start of overlap, record position
              if (i .gt. laprem .and. ipnt .eq. lenConts + 1 - lapbase .or.
     &            i .le. laprem .and. ipnt .eq. lenConts - lapbase) ivbase = iv
              iv = iv + 1
            enddo
          enddo
        enddo
c         
c         Set model properties: open contours, thicken current contour
        ierr = putModelName('Patch Tracking Model')
        call putImodFlag(1, 1)
        call putImodFlag(1, 10)
        call putSymType(1, 0)
        call putSymSize(1, 7)
        call putImodZscale(max(1., min(10., (0.3 * (nx + ny)) / nz)))
        call irtdel(1, delta)
        call irtorg(1,origin(1),origin(2),origin(3))
        call putImageRef(delta, origin)
        ierr = putImodMaxes(nx, ny, nz)
        call scaleModelToImage(1, 1)
        call write_wmod(xffilout)
      endif
      call imclose(1)
C       
c      write(*,'(3(a,f9.3))')'interpolation',wallinterp,'  fft',wallfft,
c     &    '  masking', wallmask
      WRITE(6,500)
500   FORMAT(' PROGRAM EXECUTED TO END.')
      call exit(0)
99    call exitError('END OF IMAGE WHILE READING')
97    call exitError('GETTING MEMORY FOR TAPERING FROM FILL AREA IN PATCH')
96    call exitError('ERROR WRITING TRANSFORMS TO FILE')

      CONTAINS

c       Adjusts centered coordinates XFROM, YFROM from an image at TILTFROM
c       to an image at TILTTO by rotating to the tilt axis vertical, 
c       adjusting the X coordinate by the ratio of cosines, and rotating back
c
      subroutine adjustCoord(tiltfrom, tiltto, xfrom, yfrom, xto, yto)
      implicit none
      real*4 tiltfrom, tiltto, xfrom, yfrom, xto, yto, xrot,yrot,costo, cosfrom
      real*4 tmpRatio
      real*4 tiltToLast/0./, tiltFromLast/0./, cosToLast/1./, cosFromLast/1./
      save tiltToLast, tiltFromLast, cosToLast, cosFromLast
      real*4 cosd
c
      if (tiltfrom .eq. tiltFromLast) then
        cosFrom = cosFromLast
      else
        cosFrom = cosd(tiltFrom)
        cosFromLast = cosFrom
        tiltFromLast = tiltfrom
      endif
      if (tiltto .eq. tiltToLast) then
        cosTo = cosToLast
      else
        cosTo = cosd(tiltTo)
        cosToLast = cosTo
        tiltToLast = tiltTo
      endif
      tmpRatio = abs(cosTo) / max(abs(cosFrom), 1.e-6)
c        
      xrot = xfrom * cosphi + yfrom * sinphi
      yrot = -xfrom * sinphi + yfrom * cosphi
      xrot = xrot * tmpRatio
      xto = xrot * cosphi - yrot * sinphi
      yto = xrot * sinphi + yrot * cosphi
      end subroutine adjustCoord


c       Loads a boundary or seed model, scales it, allocates iobjFlags to the
c       needed size and gets the flags.
c       iobjFlags must be deallocated after use
c       
      subroutine getModelAndFlags(errmess)
      implicit none
      character*(*) errmess
      logical exist, readSmallMod
      integer*4 getImodObjSize, getImodFlags, nobjTot
c
      exist=readSmallMod(filin)
      if(.not.exist) call exitError(errmess)
      nobjTot = getImodObjSize()
      allocate(iobjFlags(nobjTot), stat=ierr)
      call memoryError(ierr, 'OBJECT FLAG ARRAY')
      ierr = getImodFlags(iobjFlags, nobjTot)
      call scale_model(0)
      return
      end subroutine getModelAndFlags


c       Masks out the area outside of all boundary contours with the mean value
c
      subroutine maskOutsideBoundaries(ararg, nxarg, nyarg)
      implicit none
      integer*4 nxarg, nyarg
      real*4 ararg(nxarg, nyarg)
      logical outside
c
C$OMP PARALLEL DO
C$OMP& SHARED(nxarg, nyarg, numBound, xtfsBmin, xtfsBmax, ytfsBmin, ytfsBmax)
C$OMP& SHARED(ararg, numInBound, usemean, xtfsBound, ytfsBound)
c$OMP& PRIVATE(iy, ix, x0, y0, j, outside)
      do iy = 1, nyarg
        y0 = iy - 0.5
        do ix = 1, nxarg
          x0 = ix - 0.5
          outside = .true.
          do j = 1, numBound
            if (x0 .ge. xtfsBmin(j) .and. x0 .le. xtfsBmax(j) .and.
     &          y0 .ge. ytfsBmin(j) .and. y0 .le. ytfsBmax(j)) then
              if (inside(xtfsBound(indBound(j)+1), ytfsBound(indBound(j)+1),
     &            numInBound(j), x0, y0)) then
                outside = .false.
                exit
              endif
            endif
          enddo
          if (outside) ararg(ix,iy) = usemean
        enddo
      enddo
C$OMP END PARALLEL DO
c       
c       Taper if only one boundary
      if (numBound .eq. 1) then
        if (taperAtFill(ararg, nxarg,nyarg,16,1) .ne. 0)
     &      call exitError('GETTING MEMORY FOR TAPERING INSIDE BOUNDARY')
      endif
      return
      end subroutine maskOutsideBoundaries


c       find minimum tilt view that is not skipped and is in the range being done
c       
      subroutine findMinimumTiltView()
      izst = max(1,min(nz,izst))
      iznd = max(izst,min(nz,iznd))
      tiltAtMin = 10000.
      do iv = izst, iznd
        if ((breaking .or. numberInList(iv, listSkip, numSkip, 0) .eq. 0) .and.
     &      abs(tilt(iv)) .lt. abs(tiltAtMin)) then
          minTilt = iv
          tiltAtMin = tilt(iv)
        endif
      enddo
      if (tiltAtMin .gt. 9999.) call exitError(
     &    'ALL VIEWS IN THE RANGE BEING ALIGNED ARE IN THE LIST TO SKIP')
      end subroutine findMinimumTiltView


c       Count patches that have usable image area on the given view, return the number
c       and mark them in tmprray
c
      subroutine markUsablePatches(indTilt, numUsable)
      integer*4 indTilt, numUsable
      numUsable = 0
      do ipatch = 1, numPatches
        cenx = patchCenX(ipatch) - nx/2.
        ceny = patchCenY(ipatch) - ny/2.
        call adjustCoord(0., tilt(indTilt), cenx, ceny, x0, y0)
        ix = min(nx-1., nx + dxPre(indTilt), x0 + nx / 2. + nxPatch / 2.) -
     &      max(0., dxPre(indTilt), x0 + nx / 2. - nxPatch / 2.)
        iy = min(ny-1., ny + dyPre(indTilt), y0 + ny / 2. + nyPatch / 2.) -
     &      max(0., dyPre(indTilt), y0 + ny / 2. - nyPatch / 2.)
        tmprray(ipatch) = -1.
        if (ix .gt. 0 .and. iy .gt. 0 .and.
     &      ix * iy .gt. critNonBlank * nxPatch * nyPatch) then
          numUsable = numUsable + 1
          tmprray(ipatch) = 1.
        endif
      enddo
      return
      end subroutine markUsablePatches

      END


c       PEAKFIND finds the coordinates of the absolute peak, XPEAK, YPEAK
c       in the array ARRAY, which is dimensioned to nx+2 by ny.  It fits
c       a parabola in to the three points around the peak in X or Y and gets
c       a much better estimate of peak location.
c       
      subroutine peakfind(array,nxplus,nyrot,xpeak,ypeak,peak, radexcl,
     &    rotangle,streak, limitShiftX, limitShiftY)
      implicit none
      integer*4 nxplus,nyrot
      real*4 xpeak,ypeak,radexcl,rotangle,streak
      real*4 array(nxplus,nyrot)
      integer*4 nxrot,ix,iy,idx,idy,lower,ixpeak,iypeak,limitShiftX, limitShiftY
      real*4 peak,xrot,yrot,cx,y1,y2,y3,cy,costh,sinth
      real*4 cosd,sind
      integer*4 indmap
      real*8 parabolicFitPosition
c       
      nxrot=nxplus-2
c       
c       find peak
c       
      costh=cosd(-rotangle)
      sinth=sind(-rotangle)
      peak=-1.e30
      xpeak = 0.
      ypeak = 0.
      ixpeak = -1
      do iy=1,nyrot
        do ix=1,nxrot
          if(array(ix,iy).gt.peak)then
c             
c             first check if within limits
            idx=ix-1
            idy=iy-1
            if(idx.gt.nxrot/2)idx=idx-nxrot
            if(idy.gt.nyrot/2)idy=idy-nyrot
            if (abs(idx) .le. limitShiftX .and. abs(idy) .le. limitShiftY) then
c               
c               Then check if it outside the exclusion region
              xrot=idx*costh-idy*sinth
              yrot=idx*sinth+idy*costh
              if(abs(yrot).ge.radexcl.or.abs(xrot).ge.streak+radexcl)then
c               
c                 next check that point is actually a local peak
c               
                lower=0
                do idx =-1,1
                  do idy=-1,1
                    if(array(ix,iy).lt.array(indmap(ix+idx,nxrot),
     &                  indmap(iy+idy,nyrot)))lower=1
                  enddo
                enddo
                if(lower.eq.0)then
                  peak=array(ix,iy)
                  ixpeak=ix
                  iypeak=iy
                endif
              endif
            endif
          endif
        enddo
      enddo
c       print *,ixpeak,iypeak
c       
      if (ixpeak .gt. 0) then
c
c       simply fit a parabola to the two adjacent points in X or Y
c       
        y1=array(indmap(ixpeak-1,nxrot),iypeak)
        y2=peak
        y3=array(indmap(ixpeak+1,nxrot),iypeak)
        cx=parabolicFitPosition(y1, y2, y3)
c         print *,'X',y1,y2,y3,cx
        y1=array(ixpeak,indmap(iypeak-1,nyrot))
        y3=array(ixpeak,indmap(iypeak+1,nyrot))
        cy=parabolicFitPosition(y1, y2, y3)
c         print *,'Y',y1,y2,y3,cy
c       
c         return adjusted pixel coordinate minus 1
c       
        xpeak=ixpeak+cx-1.
        ypeak=iypeak+cy-1.
      endif
c       print *,xpeak,ypeak
      if(xpeak.gt.nxrot/2)xpeak=xpeak-nxrot
      if(ypeak.gt.nyrot/2)ypeak=ypeak-nyrot
      return
      end



      subroutine packcorr(crray,array,nxpadpl,nypad)
      implicit none
      integer*4 nxpadpl,nypad
      real*4 array(nxpadpl,nypad),crray(*)
      integer*4 nxpad,iout,ixin,iyin,ix,iy

      nxpad=nxpadpl-2
      iout=1
      ixin=nxpad/2+1
      iyin=nypad/2+1
      do iy=1,nypad
        do ix=1,nxpad
          crray(iout)=array(ixin,iyin)
          iout=iout+1
          ixin=ixin+1
          if(ixin.gt.nxpad)ixin=1
        enddo
        iyin=iyin+1
        if(iyin.gt.nypad)iyin=1
      enddo
      return
      end
