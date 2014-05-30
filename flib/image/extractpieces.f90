!*************EXTRACTPIECES**********************************************
!
! EXTRACTPIECES will extract piece coordinates from the header of
! an image file, if they are present, and produce a file with those
! coordinates (a piece list file) .
!
! David Mastronarde, 1/2/00
!
program extractpieces
  implicit none
  integer*4 nxyz(3), mxyz(3), nz, maxPiece, maxExtra, numExtraBytes, nbytes, iflags
  real*4, allocatable :: array(:)
  integer*4, allocatable :: ixPiece(:), iyPiece(:), izPiece(:)
  integer*4 mode, numPieces, i, ierr, ifAddMdoc, indAdoc, iTypeAdoc, numSect
  integer*4 montage
  real*4 dmin, dmax, dmean
  logical useMdoc
  !
  character*320 inFile, outFile, metaFile
  !
  equivalence (nz, nxyz(3))

  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetString, AdocOpenImageMetadata
  integer*4 PipGetInOutFile, PipParseInput, PipGetBoolean, PipGetLogical
  integer numOptions
  parameter (numOptions = 5)
  character*(120 * numOptions) options(1)
  options(1) = &
      'input:InputFile:FN:Name of input image file@'// &
      'output:OutputFile:FN:Name of output piece list file@'// &
      'mdoc:MdocMetadataFile:B:Take coordinates from metadata file named'// &
      ' inputfile.mdoc)@'// &
      'other:OtherMetadataFile:FN:Name of other metadata file to take '// &
      'coordinates from@'// &
      'help:usage:B:Print help output'

  metaFile = ' '
  inFile = ' '
  useMdoc = .false.
  nz = 0
  !
  call PipExitOnError(0, 'ERROR: EXTRACTPIECES - ')
  call PipAllowCommaDefaults(1)
  ierr = PipParseInput(options, numOptions, '@', numOptArg, numNonOptArg)
  if (PipGetBoolean('help', ierr) == 0) then
    call PipPrintHelp('extractpieces', 0, 1, 1)
    call exit(0)
  endif
  ifAddMdoc = PipGetString('OtherMetadataFile', metaFile)
  ierr = PipGetLogical('MdocMetadataFile', useMdoc)
  if (useMdoc .and. ifAddMdoc == 0) call exitError( &
      'YOU CANNOT ENTER BOTH -mdoc AND -other')
  if (PipGetInOutFile('InputFile', 1, 'Name of input image file', inFile) &
      .ne. 0 .and. ifAddMdoc .ne. 0) call exitError('NO INPUT FILE SPECIFIED')
  if (PipGetInOutFile('OutputFile', 2, 'Name of output piece list file', &
      outFile) .ne. 0) call exitError('NO OUTPUT FILE SPECIFIED')
  call PipDone()
  !
  if (inFile .ne. ' ') then
    call imopen(1, inFile, 'RO')
    call irdhdr(1, nxyz, mxyz, mode, dmin, dmax, dmean)
  endif
  !
  if (.not.useMdoc .and. ifAddMdoc .ne. 0) then
    !
    ! Get data from the image header
    call irtnbsym(1, numExtraBytes)
    call irtsymtyp(1, nbytes, iflags)
    maxExtra = numExtraBytes + 1024
    maxPiece = nz + 1024
    allocate(array(maxExtra / 4), ixPiece(maxPiece), iyPiece(maxPiece), &
        izPiece(maxPiece), stat = ierr)
    call memoryError(ierr, 'ARRAYS FOR EXTRA HEADER OR PIECE DATA')
    call irtsym(1, numExtraBytes, array)
    call get_extra_header_pieces(array, numExtraBytes, nbytes, iflags, nz, &
        ixPiece, iyPiece, izPiece, numPieces, maxPiece)
    if (numPieces == 0) &
        print *,'There are no piece coordinates in this image file'
  else
    !
    ! Or get data from the autodoc file
    if (metaFile == ' ') metaFile = inFile
    indAdoc = AdocOpenImageMetadata(metaFile, ifAddMdoc, montage, numSect, &
        iTypeAdoc)
    if (indAdoc > 0 .and. inFile == ' ') nz = numSect
    numPieces = 0
    !
    ! Thanks to a bug in SerialEM, Montage flag may be missing.  So just plow
    ! ahead regardless
    if (indAdoc > 0 .and. numSect == nz) then
      maxPiece = nz + 1024
      allocate(ixPiece(maxPiece), iyPiece(maxPiece), izPiece(maxPiece), &
          stat = ierr)
      call memoryError(ierr, 'ARRAYS FOR PIECE DATA')
      call get_metadata_pieces(indAdoc, iTypeAdoc, nz, ixPiece, iyPiece, &
          izPiece, maxPiece, numPieces)
    endif
    !
    ! Give lots of different error messages
    if (indAdoc < 0 .or. numPieces < nz) then
      if (indAdoc == -2) then
        print *,'The metadata file does not exist'
      else if (indAdoc == -3) then
        print *,'The autodoc file is not a recognized type of image'// &
            ' metadata file'
      else if (indAdoc == -1) then
        print *,'There was an error opening or reading the metadata file'
      else if (numPieces > 0 .or. numSect .ne. nz) &
          then
        print *,'The metadata file does not have piece coordinates for'// &
            ' every image in the file'
      else
        print *,'There are no piece coordinates in the metadata file'
      endif
    endif
  endif
  if (numPieces == nz) then
    call dopen(1, outFile, 'new', 'f')
    write(1, '(2i7,i5)') (ixPiece(i), iyPiece(i), izPiece(i), i = 1, numPieces)
    close(1)
    print *,numPieces, ' piece coordinates output to file'
  endif

  call imclose(1)
  !
  call exit(0)
end program extractpieces

