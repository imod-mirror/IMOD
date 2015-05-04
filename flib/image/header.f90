!*************HEADER.F**********************************************
!
! A SMALL PROGRAM TO READ THE HEADER ON AN IMAGE FILE
!
!************************************************************************
!
! $Id$
!
program header
  implicit none
  integer maxextra, ntypes
  parameter (maxextra = 2000000, ntypes = 6)
  real*4 array(maxextra/4)
  integer*4 nxyz(3), mxyz(3), mode, nbsym, numInt, numReal, ierr, i, j
  integer*4 labels(20,10), nlabel
  character*4 feichar
  logical nbytes_and_flags
  !
  character*320 inFile
  !
  character*17 typeName(ntypes) /'Tilt angles', 'Piece coordinates', &
      'Stage positions', 'Magnifications', 'Intensities', 'Exposure doses'/
  character*25 extractCom(ntypes) /'extracttilts', 'extractpieces', &
      'extracttilts -stage', 'extracttilts -mag', 'extracttilts -int', &
      'extracttilts -exp'/
  character*16 computed /'(not computed)'/
  integer*4 numInputFiles, numFileIn, ifBrief, iBinning, iflags, ifImod, ivolume, imUnit
  integer*4 numVolumes
  logical*4 doSize, doMode, doMin, doMax, doMean, silent, doPixel, doOrigin, doRMS
  real*4 dmin, dmax, dmean, pixel, tiltaxis, delta(3), rms
  integer*4 iiuRetNumVolumes, iiuVolumeOpen
  logical pipinput
  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetLogical, PipGetString, PipGetNonOptionArg, PipGetInteger
  !
  ! fallbacks from ../../manpages/autodoc2man -3 2  header
  !
  integer numOptions
  parameter (numOptions = 12)
  character*(40 * numOptions) options(1)
  options(1) = &
      'input:InputFile:FNM:@size:Size:B:@mode:Mode:B:@pixel:PixelSize:B:@'// &
      'origin:Origin:B:@minimum:Minimum:B:@maximum:Maximum:B:@mean:Mean:B:@'// &
      'rms:RootMeanSquare:B:@volume:VolumeNumber:I:@brief:Brief:B:@help:usage:B:'
  !
  ! defaults
  !
  doSize = .false.
  doMode = .false.
  doMin = .false.
  doMax = .false.
  doMean = .false.
  doPixel = .false.
  doOrigin = .false.
  doRMS = .false.
  numFileIn = 1
  ifBrief = 0
  ivolume = -1
  imUnit = 1
  !
  ! Pip startup: set error, parse options, check help, set flag if used
  ! But turn off the entry printing first!
  call PipEnableEntryOutput(0)
  call PipReadOrParseOptions(options, numOptions, 'header', &
      'ERROR: HEADER - ', .true., 1, 2, 0, numOptArg, numNonOptArg)
  pipinput = numOptArg + numNonOptArg > 0
  !
  if (pipinput) then
    ierr = PipGetLogical('Size', doSize)
    ierr = PipGetLogical('Mode', doMode)
    ierr = PipGetLogical('Max', doMax)
    ierr = PipGetLogical('Min', doMin)
    ierr = PipGetLogical('Mean', doMean)
    ierr = PipGetLogical('RootMeanSquare', doRMS)
    ierr = PipGetLogical('PixelSize', doPixel)
    ierr = PipGetLogical('Origin', doOrigin)
    ierr = PipGetInteger('Brief', ifBrief)
    ierr = PipGetInteger('VolumeNumber', ivolume)
    call PipNumberOfEntries('InputFile', numInputFiles)
    numFileIn = numInputFiles + numNonOptArg
    if (numFileIn == 0) &
        call exitError('No input file specified')
  endif

  silent = doSize .or. doMode .or. doMin .or. doMax .or. doMean .or. doRMS .or. &
      doPixel .or. doOrigin
  if (silent) call iiuAltPrint(0)
  call ialBrief(ifBrief)

  do i = 1, numFileIn
    !
    ! get the next filename
    !
    if (pipinput) then
      if (i <= numInputFiles) then
        ierr = PipGetString('InputFile', inFile)
      else
        ierr = PipGetNonOptionArg(i - numInputFiles, inFile)
      endif
    else
      write(*,'(1x,a,$)') 'Name of input file: '
      read(5, '(a)') inFile
    endif
    !
    call iiAllowMultiVolume(1)
    call imopen(1, inFile, 'RO')
    numVolumes = iiuRetNumVolumes(1)
    if (iVolume > max(1, numVolumes)) call exitError( &
        'The volume number entered is higher than the number of volumes in the file')
    if (numVolumes > 1) then
      if (iVolume < 0) then
        write(*,'(a,i4,a)') 'This is the header for the first of', numVolumes, &
            ' volumes, use -vol # to see others'
      else if (iVolume > 1) then
        imUnit = 11
        if (iiuVolumeOpen(imUnit, 1, iVolume - 1) .ne. 0)  &
            call exitError('Opening additional volume in file')
      endif
    endif

    call irdhdr(imUnit, nxyz, mxyz, mode, dmin, dmax, dmean)
    !
    if (silent) then
      if (doSize) write(*, '(3i8)') (nxyz(j), j = 1, 3)
      if (doMode) write(*, '(i4)') mode
      if (doPixel) then
        call iiuRetDelta(imUnit, delta)
        write(*, '(3g11.4)') (delta(j), j = 1, 3)
      endif
      if (doOrigin) then
        call iiuRetOrigin(imUnit, delta(1), delta(2), delta(3))
        write(*, '(3g11.4)') (delta(j), j = 1, 3)
      endif
      if (doMin) write(*, '(g13.5)') dmin
      if (doMax) write(*, '(g13.5)') dmax
      if (doMean) write(*, '(g13.5)') dmean
      if (doRMS) then
        call iiuRetImodFlags(imUnit, iflags, ifImod)
        call iiuRetMRCVersion(imUnit, j)
        call iiuRetRMS(imUnit, rms)
        if (rms > 0 .or. (rms == 0. .and. (j > 0 .or.  &
            (ifImod .ne. 0 .and. btest(iflags, 3))))) computed = ' '
        write(*, '(g13.5,a)') rms, trim(computed)
      endif
    else
      call iiuRetNumExtended(imUnit, nbsym)
      if (nbsym > 0 .and. nbsym <= maxextra) then
        call iiuRetExtendedData(imUnit, nbsym, array)
        call iiuRetExtendedType(imUnit, numInt, numReal)
        if (.not. nbytes_and_flags(numInt, numReal) .and. numReal >= 12) then
          tiltaxis = array(numInt + 11)
          if (tiltaxis >= -360. .and. tiltaxis <= 360.) then
            if (tiltaxis < -180.) tiltaxis = tiltaxis + 360.
            if (tiltaxis > 180.) tiltaxis = tiltaxis - 360.
            call irtlab(imUnit, labels, nlabel)
            write(feichar, '(a4)') labels(1, 1)
            if (feichar == 'Fei ') then
              write(*,101) - tiltaxis, ' (Corrected sign)'
            else
              write(*,101) tiltaxis
            endif
101         format(10x,'Tilt axis rotation angle = ', f7.1, a)
          endif
          pixel = array(numInt + 12) * 1.e9
          call iiuRetDelta(imUnit, delta)
          call iiuRetImodFlags(imUnit, iflags, ifImod)
          if (pixel > 0.01 .and. pixel < 10000 .and. iand(iflags, 2) == 0) then
            do j = 3, 1, -1
              iBinning = nint(delta(j))
              if (abs(delta(j) - iBinning) > 1.e-6 .or. iBinning <= 0 .or. &
                  iBinning > 4) then
                iBinning = 0
                exit
              endif
            enddo
            if (iBinning == 1) then
              write(*,102) pixel
            else if (iBinning > 1 .and. iBinning < 5) then
              write(*,102) pixel * iBinning, ' (Assumed binning of', iBinning, ')'
            else
              write(*,104) pixel
            endif
102         format(10x,'Pixel size in nanometers =', g11.4, a, i2, a)
104         format(10x,'Original pixel size in nanometers =', g11.4)
          endif
        endif
        if (nbytes_and_flags(numInt, numReal) .and. .not.silent .and. ifBrief == 0) then
          write(*,'(/,a)') 'Extended header from SerialEM contains:'
          do j = 1, ntypes
            if (mod(numReal / 2**(j - 1), 2) .ne. 0) &
                write(*,103) typeName(j), trim(extractCom(j))
103         format(2x, a, ' - Extract with "',a,'"')
          enddo
        endif
      endif
    endif

    call iiuClose(imUnit)
    if (imUnit > 1) call iiuClose(1)
  enddo
  !
  call exit(0)
end program header
