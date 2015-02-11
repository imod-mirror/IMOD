! * * * * * DENSMATCH * * * * * *
!
! DENSMATCH scales the density values in one volume so that its mean
! and standard deviation match that of another volume.  To determine
! the mean and S.D. for each volume, it samples up to 100000 pixels in
! the central eighth of each volume (central half in X, Y, and Z) .  It
! can write the scaled values either to a new file or back into the
! file of the volume being scaled.  It can also simply report the
! scaling factors without scaling the data.
!
! See man page for details.
!
! David Mastronarde, November 1995
!
! $Id$
!
program densmatch
  implicit none
  integer idim, MAX_SAMPLES
  parameter (idim = 1000000, MAX_SAMPLES = 100000)
  integer*4 nx, ny, nz, ifXminMax, ifYminMax, ifZminMax, ifMinMax(3)
  integer*4 ixStart, iyStart, izStart, nxUse, nyUse, nzUse, ixyzStart(3), nxyzUse(3)
  integer*4 numSampX, numSampY, numSampZ, numSampXyz(3)
  real*4 dxSample, dySample, dzSample, dxyzSample(3)
  integer*4 nxyz(3), mxyz(3)
  real*4 array(idim)
  !
  character*320 inFile, outFile
  !
  equivalence (nx, nxyz(1)), (ny, nxyz(2)), (nz, nxyz(3))
  equivalence (ifXminMax, ifMinMax(1)), (ifYminMax, ifMinMax(2)), (ifZminMax, ifMinMax(3))
  equivalence (ixStart, ixyzStart(1)), (iyStart, ixyzStart(2)), (izStart, ixyzStart(3))
  equivalence (nxUse, nxyzUse(1)), (nyUse, nxyzUse(2)), (nzUse, nxyzUse(3))
  equivalence (numSampX,numSampXyz(1)), (numSampY,numSampXyz(2)), (numSampZ,numSampXyz(3))
  equivalence (dxSample,dxyzSample(1)), (dySample,dxyzSample(2)), (dzSample,dxyzSample(3))
  real*4 average(2), stanDev(2)
  !
  character dat * 9, tim * 8, errstr * 9
  character*80 titlech
  logical report
  integer*4 iunitOut, ierr, iunit, mode, idelSample, ndat, jz, iz
  integer*4 jy, iy, jx, ix, i, maxLines, numChunks, iChunk, numLines, iyLine
  integer*4 iShift(3)
  integer*4 iStart(3), iEnd(3)
  real*4 dmin, dmax, dmean, sem, scaleFac, addFac, tsum, tmin, tmax, tmean
  real*8 dsum8, dsumSq8, tsum8, tsumSq8
  !
  logical pipInput
  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetString, PipGetInteger, PipGetFloat, PipGetLogical
  integer*4 PipGetInOutFile, PipGetTwoIntegers, PipGetThreeIntegers
  !
  ! fallbacks from ../../manpages/autodoc2man -3 2  densmatch
  !
  integer numOptions
  parameter (numOptions = 9)
  character*(40 * numOptions) options(1)
  options(1) = &
      'reference:ReferenceFile:FN:@scaled:ScaledFile:FN:@'// &
      'output:OutputFile:FN:@report:ReportOnly:B:@'// &
      'xminmax:XMinAndMax:IP:@yminmax:YMinAndMax:IP:@'// &
      'zminmax:ZMinAndMax:IP:@offset:OffsetRefToScaledXYZ:FT:@'// &
      'help:usage:B:'
  outFile = ' '
  report = .false.
  do i = 1, 3
    ifMinMax(i) = 0
    iShift(i) = 0
    iStart(i) = 0
  enddo
  !
  ! Pip startup: set error, parse options, check help, set flag if used
  !
  call PipReadOrParseOptions(options, numOptions, 'densmatch', 'ERROR: DENSMATCH - ', &
      .true., 1, 2, 1, numOptArg, numNonOptArg)
  pipInput = numOptArg + numNonOptArg > 0
  !
  ! Get input and output files
  !
  if (PipGetInOutFile('ReferenceFile', 1, 'Name of reference volume', inFile) .ne. 0) &
      call exitError('NO REFERENCE FILE WAS SPECIFIED')
  call imopen(1, inFile, 'ro')
  !
  if (PipGetInOutFile('ScaledFile', 2, 'Name of volume to be scaled', inFile) .ne. 0) &
      call exitError('NO FILE WAS SPECIFIED TO BE SCALED')
  call imopen(2, inFile, 'old')
  !
  ierr = PipGetInOutFile('OutputFile', 3, 'Name of output file,'// &
      ' or Return to rewrite file to be scaled', outFile)
  !
  iunitOut = 2
  if (outFile .ne. ' ') then
    call imopen(3, outFile, 'new')
    iunitOut = 3
  endif
  if (pipInput) then
    ierr = PipGetLogical('ReportOnly', report)
    ifXminMax = 1 - PipGetTwoIntegers('XMinAndMax', iStart(1), iEnd(1))
    ifYminMax = 1 - PipGetTwoIntegers('YMinAndMax', iStart(2), iEnd(2))
    ifZminMax = 1 - PipGetTwoIntegers('ZMinAndMax', iStart(3), iEnd(3))
    if (PipGetThreeIntegers('OffsetRefToScaledXYZ', iShift(1), iShift(2), &
        iShift(3)) == 0 .and. ifXminMax + ifYminMax + ifZminMax == 0) &
        call exitError('YOU MUST ENTER MIN AND MAX X, Y, OR Z IF YOU ENTER OFFSETS')
  endif
  call PipDone()
  !
  ! sample each volume to find mean and SD
  !
  do iunit = 1, 2
    call irdhdr(iunit, nxyz, mxyz, mode, dmin, dmax, dmean)
    if (nx >= idim) call exitError('IMAGE TOO LARGE IN X FOR ARRAY')
    do i = 1, 3
      nxyzUse(i) = max(1, nxyz(i) / 2)
      ixyzStart(i) = nxyz(i) / 4
      if (ifMinMax(i) .ne. 0) then
        ixyzStart(i) = iStart(i)
        nxyzUse(i) = iEnd(i) + 1 - iStart(i)
        if (iunit == 2) ixyzStart(i) = ixyzStart(i) + iShift(i)
        errstr = ' '
        if (ixyzStart(i) < 0 .or. ixyzStart(i) >= nxyz(i)) errstr = 'STARTING '
        if (ixyzStart(i) + nxyzUse(i) <= 0 .or. &
            ixyzStart(i) + nxyzUse(i) > nxyz(i)) errstr = 'ENDING '
        if (errstr .ne. ' ') then
          write(*,'(/,a,a,a,a,i2)') 'ERROR: DENSMATCH - ', errstr, &
              char(ichar('W') + i), ' COORDINATE OUT OF RANGE IN VOLUME #', &
              iunit
          call exit(1)
        endif
      endif
    enddo
    idelSample = (((float(nxUse) * nyUse) * nzUse) / MAX_SAMPLES)**.3333 + 1.
    !
    ! Make sure there are at least 10 samples in each direction
    do i = 1, 3
      numSampXyz(i) = max((nxyzUse(i) - 1) / idelSample + 1, min(nxyzUse(i), 10))
      if (numSampXyz(i) == 1) then
        dxyzSample(i) = 1
        if (ifMinMax(i) .ne. 0) ixyzStart(i) = (iEnd(i) + 1 + iStart(i)) / 2
      else
        dxyzSample(i) = (nxyzUse(i) - 1.) / (numSampXyz(i) - 1.)
      endif
      ! write(*,'(a,i2,a,i2,a,i5,a,i5,a,i4,a,f9.2)') 'vol', iun, ' axis', i, &
      ! ' start', ixyzs(i), ' use', nxyzuse(i), ' #samp', nsxyz(i), ' dsamp', &
      ! dxyzsamp(i)
    enddo
    ndat = 0
    dsum8 = 0.
    dsumSq8 = 0.
    do jz = 1, numSampZ
      iz = izStart + (jz - 1) * dzSample
      tsum8 = 0.
      tsumSq8 = 0.
      do jy = 1, numSampY
        iy = iyStart + (jy - 1) * dySample
        call iiuSetPosition(iunit, iz, iy)
        call irdlin(iunit, array,*99)
        do jx = 1, numSampX
          ix = 1 + ixStart + (jx - 1) * dxSample
          ndat = ndat + 1
          tsum8 = tsum8 + array(ix)
          tsumSq8 = tsumSq8 + array(ix)**2
        enddo
      enddo
      dsum8 = dsum8 + tsum8
      dsumSq8 = dsumSq8 + tsumSq8
    enddo
    call sums_to_avgsd8(dsum8, dsumSq8, ndat, 1, average(iunit), stanDev(iunit), sem)
    write(6, 103) iunit, average(iunit), stanDev(iunit)
103 format(' Volume',i2,': mean =',f12.4,',  SD =',f12.4)
  enddo
  !
  ! scale second volume to match first
  !
  scaleFac = stanDev(1) / stanDev(2)
  addFac = average(1) - average(2) * scaleFac
  !
  if (report) then
    write(*,102) scaleFac, addFac
102 format('Scale factors to multiply by then add:', 2g14.6)
    call exit(0)
  endif
  !
  if (iunitOut == 3) call iiuTransHeader(3, 2)
  tsum = 0.
  tmin = 1.e30
  tmax = -1.e30
  maxLines = idim / nx
  numChunks = (ny + maxLines - 1) / maxLines
  do iz = 1, nz
    iyLine = 0
    do iChunk = 1, numChunks
      numLines = min(maxLines, ny - (iChunk - 1) * maxLines)
      ! print *,'chunk', ichunk, ',  iy', iyl, iyl + numLines - 1

      call iiuSetPosition(2, iz - 1, iyLine)
      call irdsecl(2, array, numLines,*99)
      if (mode .ne. 0) then
        do i = 1, nx * numLines
          array(i) = scaleFac * array(i) + addFac
        enddo
      else
        do i = 1, nx * numLines
          array(i) = min(255., max(0., scaleFac * array(i) + addFac))
        enddo
      endif
      call iclden(array, nx, numLines, 1, nx, 1, numLines, dmin, dmax, dmean)
      tmin = min(tmin, dmin)
      tmax = max(tmax, dmax)
      tsum = tsum + dmean * numLines
      call iiuSetPosition(iunitOut, iz - 1, iyLine)
      call iiuWriteLines(iunitOut, array, numLines)
      iyLine = iyLine + numLines
    enddo
  enddo
  tmean = tsum / (nz * ny)
  call date(dat)
  call time(tim)
  !
  write(titlech, 3000) dat, tim
  call iiuWriteHeaderStr(iunitOut, titlech, 1, tmin, tmax, tmean)
  call iiuClose(iunitOut)
  call exit(0)
3000 format ( 'DENSMATCH: Scaled volume to match another',t57,a9,2x, a8)
99 call exitError('READING FILE')
end program densmatch
