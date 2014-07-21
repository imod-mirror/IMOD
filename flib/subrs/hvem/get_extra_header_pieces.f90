! $Id$
! !
! Returns piece coordinates from the extra header written by SerialEM
! ^  [array] = array of extra header data
! ^  [nbsym] = number of bytes of data there
! ^  [nbyte] = number of bytes per section
! ^  [iflags] = flags for type of data present
! ^  [nz] = number of pieces in the file
! ^  [ixpiece], [iypiece], [izpiece] = arrays in which coordinates are returned
! ^  [npiece] = number of coordinates returned (should equal [nz])
! ^  [maxpiece] = size of [piece] arrays
! !
subroutine get_extra_header_pieces (array, numExtraBytes, nbytes, iflags, nz, &
    ixPiece, iyPiece, izPiece, numPieces, maxPiece)
  implicit none
  include 'endian.inc'
  integer*1 array(*)
  integer*4 ixPiece(*), iyPiece(*), izPiece(*)
  integer*4 numExtraBytes, nbytes, iflags, nz, numPieces, maxPiece
  integer*4 i4temp
  integer*2 i2temp(2), izTemp
  equivalence (i2temp, i4temp)
  logical nbytes_and_flags, shorts, allZero
  integer*4 i, ind, numByteSkip, ind_piece
  real*4 xtemp, ytemp, ztemp, crit

  crit = 1.e-5
  numPieces = 0
  if (numExtraBytes == 0) return
  if (nz > maxPiece) then
    write(*,'(/,a,a)') 'ERROR: GET_EXTRA_HEADER_PIECES ', &
        '- ARRAYS NOT LARGE ENOUGH FOR PIECE LISTS'
    call exit(1)
  endif

  shorts = nbytes_and_flags(nbytes, iflags)
  if (shorts) then
    !
    ! if data are packed as shorts, see if the montage flag is set then
    ! set starting index based on whether there are tilt angles too
    !
    if (mod(iflags / 2, 2) == 0 .or. nbytes == 0) return
    ind = 1
    if (mod(iflags, 2) .ne. 0) ind = 3
    i4temp = 0
    do i = 1, nz
      if (ind > numExtraBytes) return
      call move(i2temp(lowByte), array(ind), 2)
      ixPiece(i) = i4temp
      call move(i2temp(lowByte), array(ind + 2), 2)
      iyPiece(i) = i4temp
      call move(izTemp, array(ind + 4), 2)
      izPiece(i) = izTemp
      ind = ind + nbytes
      numPieces = i
    enddo
  else
    !
    ! otherwise the coordinates MIGHT be in the reals, at the position
    ! given by ind_piece
    ! make sure there are enough reals in header, set up to skip ints
    ! and reals
    ! The code is ready to go, but disable it for now
    !
    ind_piece = 999
    if (iflags < ind_piece + 2) return
    numByteSkip = 4 * (nbytes + iflags)
    ind = 1 + 4 * (nbytes + ind_piece-1)
    allZero = .true.
    do i = 1, nz
      if (ind > numExtraBytes) then
        if (allZero) numPieces = 0
        return
      endif
      call move(xtemp, array(ind), 4)
      ixPiece(i) = nint(xtemp)
      call move(ytemp, array(ind + 4), 4)
      iyPiece(i) = nint(ytemp)
      call move(ztemp, array(ind + 8), 4)
      izPiece(i) = nint(ztemp)
      !
      ! keep track of whether anything is nonzero
      !
      if (ixPiece(i) .ne. 0 .or. iyPiece(i) .ne. 0 .or. izPiece(i) .ne. 0) &
          allZero = .false.
      !
      ! if any number is not close enough to being an integer, it
      ! cannot be piece coordinates
      !
      if (abs(ixPiece(i) - xtemp) > crit * abs(xtemp) .or. &
          abs(iyPiece(i) - ytemp) > crit * abs(ytemp) .or. &
          abs(izPiece(i) - ztemp) > crit * abs(ztemp)) then
        numPieces = 0
        return
      endif
      ind = ind + numByteSkip
      numPieces = i
    enddo
    !
    ! if no numbers were non-zero, just return zero pieces
    !
    if (allZero) numPieces = 0
  endif
  return
end subroutine get_extra_header_pieces

! !
! Returns piece coordinates from a metadata autodoc file written by
! SerialEM.  The file should be opened first with
! @@autodoc.html#AdocOpenImageMetadata@, or information about the autodoc in an HDF file 
! should be obtained with @@autodoc.html#AdocGetImageMetaInfo@.
! ^  [index] = index of autodoc
! ^  [itype] = type of metadata file: 1 for one file, 2 for image series, 3 for autodoc
! ^  [nz] = number of pieces in the file
! ^  [ixpiece], [iypiece], [izpiece] = arrays in which coordinates are
! returned
! ^  [npiece] = number of coordinates returned (should equal [nz])
! ^  [maxpiece] = size of [piece] arrays
! ^  [nfound] = number of sections piece coordinates were found for
! !
subroutine get_metadata_pieces(index, itype, nz, ixPiece, iyPiece, izPiece, &
    maxPiece, numFound)
  implicit none
  integer*4 ixPiece(*), iyPiece(*), izPiece(*)
  integer*4 maxPiece, index, itype, nz, numFound
  integer*4 i, ind
  character*20 globalName, sectNames(3) /'ZValue', 'Image', 'ZValue'/
  integer*4 AdocSetCurrent, AdocGetThreeIntegers, AdocGetStandardNames
  integer*4 AdocLookupByNameValue
  numFound= 0
  if (AdocGetStandardNames(globalName, sectNames(1)) == 0) &
      sectNames(3) = sectNames(1)

  if (nz > maxPiece) then
    write(*,'(/,a,a)') 'ERROR: GET_METADATA_PIECES ', &
        '- ARRAYS NOT LARGE ENOUGH FOR PIECE LISTS'
    call exit(1)
  endif
  if (AdocSetCurrent(index) .ne. 0) then
    write(*,'(/,a,a)') 'ERROR: GET_METADATA_PIECES - FAILED TO SET AUTODOC INDEX'
    call exit(1)
  endif
  do i = 1, nz
    ind = i
    if (itype == 3) then
      ind = AdocLookupByNameValue(sectNames(itype), i - 1)
      if (ind < 0) cycle
    endif
    if (AdocGetThreeIntegers(sectNames(itype), ind, 'PieceCoordinates', &
        ixPiece(i), iyPiece(i), izPiece(i)) == 0) numFound = numFound + 1
  enddo
  return
end subroutine get_metadata_pieces

