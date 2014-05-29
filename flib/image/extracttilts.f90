!*************EXTRACTTILTS.f**********************************************
!
! EXTRACTTILTS will extract tilt angles or other per-section
! information from the header of an image file, if they are present,
! and produce a file with a list of the values.
!
! See man page for more details
!
! David Mastronarde, 1/2/00
!
! $Id$
!
program extracttilts
  implicit none
  integer*4 maxExtra, maxTilts, maxPiece
  integer*4 nxyz(3), mxyz(3)
  real*4, allocatable :: tilt(:), val2(:), array(:)
  integer*4, allocatable :: ixPiece(:), iyPiece(:), izPiece(:)
  !
  character*320 inFile, outFile, metafile
  character*16 typeText(9) /'tilt angle', ' ', 'stage position', &
      'magnification', 'intensity value', 'exposure dose', 'pixel spacing', &
      'defocus', 'exposure time'/
  !
  integer*4 nz, ierr, ifTilt, ifMag, ifStage, numPieces, i, nbytes, iflags
  integer*4 maxz, iunitOut, lenText, mode, itype, ifC2, numTilts, numExtraBytes
  integer*4 numTiltOut, ifDose, ifWarn, ifAll, ifCamExp, ifDefocus, ifPixel, numFound
  integer*4 ifAddMdoc, indAdoc, iTypeAdoc, numSect, montage, ifMdoc
  integer*4 AdocOpenImageMetadata
  real*4 dmin, dmax, dmean
  equivalence (nz, nxyz(3))
  !
  logical pipinput
  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetBoolean, PipGetInOutFile, PipGetString
  !
  ! fallbacks from ../../manpages/autodoc2man -3 2  extracttilts
  !
  integer numOptions
  parameter (numOptions = 15)
  character*(40 * numOptions) options(1)
  options(1) = &
      'input:InputFile:FN:@output:OutputFile:FN:@'// &
      'mdoc:MdocMetadataFile:B:@other:OtherMetadataFile:FN:@'// &
      'tilts:TiltAngles:B:@stage:StagePositions:B:@'// &
      'mag:Magnifications:B:@intensities:Intensities:B:@'// &
      'exp:ExposureDose:B:@camera:CameraExposure:B:@'// &
      'pixel:PixelSpacing:B:@defocus:Defocus:B:@'// &
      'warn:WarnIfTiltsSuspicious:B:@all:AllPieces:B:@help:usage:B:'
  !
  outFile = ' '
  ifMag = 0
  ifStage = 0
  ifC2 = 0
  ifTilt = 0
  ifDose = 0
  ifCamExp = 0
  ifPixel = 0
  ifDefocus = 0
  ifWarn = 0
  numPieces = 0
  ifAll = 0
  ifMdoc = 0
  metafile = ' '
  !
  ! Pip startup: set error, parse options, check help, set flag if used
  !
  call PipReadOrParseOptions(options, numOptions, 'extracttilts', &
      'ERROR: EXTRACTTILTS - ', .true., 1, 1, 1, numOptArg, numNonOptArg)
  pipinput = numOptArg + numNonOptArg > 0

  if (PipGetInOutFile('InputFile', 1, 'Image input file', inFile) &
      .ne. 0) call exitError('NO INPUT IMAGE FILE SPECIFIED')
  ierr = PipGetInOutFile('OutputFile', 2, &
      'Name of output file, or return to print out values', outFile)
  !
  if (pipinput) then
    ierr = PipGetBoolean('AllPieces', ifAll)
    ierr = PipGetBoolean('TiltAngles', ifTilt)
    ierr = PipGetBoolean('Magnifications', ifMag)
    ierr = PipGetBoolean('StagePositions', ifStage)
    ierr = PipGetBoolean('Intensities', ifC2)
    ierr = PipGetBoolean('ExposureDose', ifDose)
    ierr = PipGetBoolean('CameraExposure', ifCamExp)
    ierr = PipGetBoolean('Defocus', ifDefocus)
    ierr = PipGetBoolean('PixelSpacing', ifPixel)
    ierr = PipGetBoolean('MdocMetadataFile', ifMdoc)
    ierr = PipGetBoolean('WarnIfTiltsSuspicious', ifWarn)
    ifAddMdoc = PipGetString('OtherMetadataFile', metafile)
    ierr = ifTilt + ifMag + ifStage + ifC2 + ifDose + ifDefocus + ifCamExp + ifPixel
    if (ierr == 0) ifTilt = 1
    if (ierr > 1) call exitError('YOU MUST ENTER ONLY ONE OPTION FOR DATA TO EXTRACT')
    if (ifTilt .ne. 0) itype = 1
    if (ifStage .ne. 0) itype = 3
    if (ifMag .ne. 0) itype = 4
    if (ifC2 .ne. 0) itype = 5
    if (ifDose .ne. 0) itype = 6
    if (ifPixel .ne. 0) itype = 7
    if (ifDefocus .ne. 0) itype = 8
    if (ifCamExp .ne. 0) itype = 9
    if (itype > 6 .and. ifMdoc == 0 .and. metafile == ' ') &
        call exitError('THIS KIND OF INFORMATION CAN '// &
        'BE OBTAINED ONLY FROM A METADATA FILE')
  else
    ifTilt = 1
    itype = 1
  endif

  call PipDone()

  call imopen(1, inFile, 'RO')
  call irdhdr(1, nxyz, mxyz, mode, dmin, dmax, dmean)
  !
  call irtnbsym(1, numExtraBytes)
  maxExtra = numExtraBytes + 1024
  maxPiece = nz + 1024
  maxTilts = maxPiece
  allocate(tilt(maxTilts), val2(maxTilts), array(maxExtra / 4), &
      ixPiece(maxPiece), iyPiece(maxPiece), izPiece(maxPiece), stat = ierr)

  call memoryError(ierr, 'ARRAYS FOR EXTRA HEADER DATA')

  call irtsym(1, numExtraBytes, array)
  call irtsymtyp(1, nbytes, iflags)
  !
  ! Open the metafile and check for errors
  if (ifMdoc .ne. 0 .or. metafile .ne. ' ') then
    if (metafile == ' ') metafile = inFile
    indAdoc = AdocOpenImageMetadata(metafile, ifAddMdoc, montage, &
        numSect, iTypeAdoc)
    if (indAdoc == -1) call exitError( &
        'OPENING OR READING THE METADATA FILE')
    if (indAdoc == -2) call exitError( &
        'THE METADATA FILE DOES NOT EXIST')
    if (indAdoc == -3) call exitError('THE AUTODOC FILE IS NOT '// &
        'A RECOGNIZED FORM OF IMAGE METADATA FILE')
    if (numSect .ne. nz) call exitError('THE IMAGE AND METADATA FILES'// &
        ' DO NOT HAVE THE SAME NUMBER OF SECTIONS')
  endif
  !
  ! Get piece coordinates unless doing "all"
  if (ifAll == 0) then
    if (ifMdoc == 0 .and. metafile == ' ') then
      call get_extra_header_pieces (array, numExtraBytes, nbytes, iflags, &
          nz, ixPiece, iyPiece, izPiece, numPieces, maxPiece)
    else
      if (montage .ne. 0) then
        call get_metadata_pieces(indAdoc, iTypeAdoc, nz, ixPiece, iyPiece, &
            izPiece, maxPiece, numPieces)
        if (numPieces < nz) call exitError('THE METADATA FILE DOES NOT '// &
            'HAVE PIECE COORDINATES FOR EVERY SECTION')
      endif
    endif
  endif
  if (numPieces == 0) then
    do i = 1, nz
      izPiece(i) = i - 1
    enddo
    maxz = nz
  else
    maxz = 0
    do i = 1, numPieces
      maxz = max(maxz, izPiece(i) + 1)
    enddo
  endif
  !
  ! set up a marker value for empty slots
  !
  do i = 1, maxz
    tilt(i) = -999.
  enddo
  !
  if (ifMdoc == 0 .and. metafile == ' ') then
    call get_extra_header_items (array, numExtraBytes, nbytes, iflags, nz, itype, &
        tilt, val2, numTilts, maxTilts, izPiece)
    if (numTilts == 0) then
      write(*,'(a,a,a)') 'ERROR: EXTRACTTILTS - No ', trim(typeText(itype)), &
          ' information in this image file'
      call exit(1)
    endif
  else
    call get_metadata_items(indAdoc, iTypeAdoc, nz, itype, tilt, val2, &
        numTilts, numFound, maxTilts, izPiece)
    if (numFound .ne. nz) then
      write(*,'(a,a,a)') 'ERROR: EXTRACTTILTS - ', trim(typeText(itype)), &
          ' is missing for all or some sections in metadata file'
      call exit(1)
    endif
  endif
  !
  ! pack the tilts down
  !
  numTiltOut = 0
  do i = 1, numTilts
    if (tilt(i) .ne. -999.) then
      numTiltOut = numTiltOut + 1
      tilt(numTiltOut) = tilt(i)
      val2(numTiltOut) = val2(i)
    endif
  enddo
  !
  iunitOut = 6
  if (outFile .ne. ' ') then
    call dopen(1, outFile, 'new', 'f')
    iunitOut = 1
  endif

  if (ifTilt .ne. 0) write(iunitOut, '(f7.2)') (tilt(i), i = 1, numTiltOut)
  if (ifMag .ne. 0) write(iunitOut, '(i7)') (nint(tilt(i)), i = 1, numTiltOut)
  if (ifC2 .ne. 0) write(iunitOut, '(f8.5)') (tilt(i), i = 1, numTiltOut)
  if (ifStage .ne. 0)  write(iunitOut, '(2f9.2)') (tilt(i), val2(i), i = 1, numTiltOut)
  if (ifDose .ne. 0) write(iunitOut, '(f13.5)') (tilt(i), i = 1, numTiltOut)
  if (ifDefocus .ne. 0 .or. ifPixel .ne. 0 .or. ifCamExp .ne. 0) &
      write(iunitOut, '(f11.3)') (tilt(i), i = 1, numTiltOut)

  if (iunitOut == 1) then
    close(1)
    print *,numTiltOut, ' ', trim(typeText(itype)), 's output to file'
  endif

  if (ifTilt > 0 .and. ifWarn > 0) then
    numTilts = 0
    do i = 1, numTiltOut
      if (abs(tilt(i)) < 0.1) numTilts = numTilts + 1
      if (abs(tilt(i)) > 95.) ifMag = ifMag + 1
    enddo
    if (numTiltOut > 2 .and. numTilts > numTiltOut / 2) &
        write(*,103) numTilts, 'near zero'
103 format('WARNING: extracttilts - ',i4, ' of the extracted tilt angles are ',a)
    if (ifMag > 0) write(*,103) ifMag, 'greater than 95 degrees'
  endif

  call imclose(1)
  !
  call exit(0)
end program extracttilts
