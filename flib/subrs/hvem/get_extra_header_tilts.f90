! $Id$
! !
! Returns tilt angles from an extra header written by SerialEM or in the
! Agard format.  It relies on the Z values of the sections in the file
! as listed in [izpiece], which should be obtained first with
! @get_extra_header_pieces .
! ^  [array] = array of extra header data
! ^  [nbsym] = number of bytes of data there
! ^  [nbyte] = number of bytes per section
! ^  [iflags] = flags for type of data present
! ^  [nz] = number of sections or pieces
! ^  [tilt] = array for tilt angles
! ^  [ntilt] = # of tilt angles returned (or highest # if there are gaps)
! ^  [maxtilts] = size of TILT array
! ^  [izpiece] = Z value of each section in file
! !
subroutine get_extra_header_tilts(array, numExtraBytes, nbytes, iflags, nz, tilt, &
    numTilts, maxTilts, izPiece)
  implicit none
  integer*1 array(*)
  real*4 tilt(*)
  integer*4 izPiece(*)
  integer*4 numExtraBytes, nbytes, iflags, nz, numTilts, maxTilts

  call get_extra_header_items(array, numExtraBytes, nbytes, iflags, nz, 1, tilt, &
      tilt, numTilts, maxTilts, izPiece)
  return
end subroutine get_extra_header_tilts

! !
! Returns values of a defined type from an extra header written by
! SerialEM; will also return tilt angles from a header in the
! Agard format.  It relies on the Z values of the sections in the file
! as listed in [izpiece], which should be obtained first with
! @get_extra_header_pieces .
! ^  [array] = array of extra header data
! ^  [nbsym] = number of bytes of data there
! ^  [nbyte] = number of bytes per section
! ^  [iflags] = flags for type of data present
! ^  [nz] = number of sections or pieces
! ^  [itype] = type of data to retrieve: 1 for tilt angle, 3 for stage
! position, 4 for magnification, 5 for intensity, 6 for exposure dose
! ^  [val1], [val2] = arrays for one or two values to be returned
! ^  [nvals] = # of values returned (or highest # if there are gaps)
! ^  [maxvals] = size of [val1] and [val2] arrays
! ^  [izpiece] = Z value of each section in file
! !
subroutine get_extra_header_items(array, numExtraBytes, nbytes, iflags, nz, &
    itype, val1, val2, numVals, maxVals, izPiece)
  implicit none
  integer*1 array(*)
  real*4 val1(*), val2(*)
  integer*4 izPiece(*)
  integer*4 numExtraBytes, nbytes, iflags, nz, numVals, maxVals, itype
  integer*2 temp, temp2
  logical nbytes_and_flags, shorts
  integer*4 i, ind, numByteSkip, ival, numFlags
  integer*4 nbytes_per_item(32)
  real*4 SEMshortsToFloat
  !
  numVals = 0
  if (numExtraBytes == 0) return
  !
  call b3dHeaderItemBytes(numFlags, nbytes_per_item)
  !
  shorts = nbytes_and_flags(nbytes, iflags)
  if (shorts) then
    !
    ! if data are packed as shorts, then test for the bit corresponding
    ! to itype.  Skip nbyte between sections and advance starting index
    ! for each entry prior to the desired one
    !
    if (mod(iflags / 2**(itype-1), 2) == 0 .or. nbytes == 0) return
    numByteSkip = nbytes
    ind = 1
    do i = 1, itype - 1
      if (mod(iflags / 2**(i - 1), 2) .ne. 0) ind = ind + nbytes_per_item(i)
    enddo
  else
    !
    ! otherwise, tilt angle is the first float; need to skip over ints
    !
    if (iflags == 0 .or. itype > 1) return
    numByteSkip = 4 * (nbytes + iflags)
    ind = 1 + 4 * nbytes
  endif
  !
  do i = 1, nz
    ival = izPiece(i) + 1
    if (ival < 1) then
      write(*,'(/,a,a)') 'ERROR: GET_EXTRA_HEADER_ITEMS', &
          ' - VALUE ARRAY NOT DESIGNED FOR NEGATIVE Z VALUES'
      call exit(1)
    endif
    if (ival > maxVals) then
      write(*,'(/,a,a)') 'ERROR: GET_EXTRA_HEADER_ITEMS', &
          ' - ARRAY NOT BIG ENOUGH FOR DATA'
      call exit(1)
    endif
    numVals = max(numVals, ival)
    if (shorts) then
      call move(temp, array(ind), 2)
      if (itype == 1) then
        val1(ival) = temp / 100.                ! Tilt angle * 100
      elseif (itype == 3) then
        val1(ival) = temp / 25.             !Stage X and Y * 25.
        call move(temp, array(ind + 2), 2)
        val2(ival) = temp / 25.
      elseif (itype == 4) then
        val1(ival) = temp * 100.            ! Magnification / 100
      elseif (itype == 5) then
        val1(ival) = temp / 25000.          ! Intensity * 25000.
      elseif (itype == 6) then
        call move(temp2, array(ind + 2), 2)
        val1(ival) = SEMshortsToFloat(temp, temp2) ! Exposure dose
      endif
    else
      call move(val1(ival), array(ind), 4)
    endif
    ind = ind + numByteSkip
    if (ind > numExtraBytes) return
  enddo
  return
end subroutine get_extra_header_items

! !
! Converts the two short integers [low] and [ihigh] stored by SerialEM
! for a floating point number back into the number
! !
real*4 function SEMshortsToFloat(low, ihigh)
  implicit none
  integer*2 low, ihigh
  integer*4 iexp, ival, ivalSign, iexpSign
  iexpSign = 1
  ivalSign = 1
  if (low < 0) then
    low = -low
    ivalSign = -1
  endif
  if (ihigh < 0) then
    ihigh = -ihigh
    iexpSign = -1
  endif
  ival = low * 256 + mod(ihigh, 256)
  iexp = ihigh / 256
  SEMshortsToFloat = ivalSign * (ival * 2.**(iexp * iexpSign))
  return
end function SEMshortsToFloat

! !
! Returns values of a defined type from an image metadata file written by
! SerialEM or an HDF file in which such data have been incorporated.  It simply calls
! @get_metadata_by_key with a specific key defined by the value of [itypeData].  See
! @get_metadata_by_key for a full description; the arguments special to this call are:
! ^  [iTypeData] = type of data to retrieve: 1 for tilt angle, 3 for
! stage position, 4 for magnification, 5 for intensity, 6 for exposure
! dose, 7 for pixel size, 8 for defocus, 9 for exposure time
! ^  [val1], [val2] = arrays for one or two values to be returned
! !
subroutine get_metadata_items(indAdoc, iTypeAdoc, nz, iTypeData, val1, &
    val2, numVals, numFound, maxVals, izPiece)
  implicit none
  real*4 val1(*), val2(*), val3
  integer*4 izPiece(*)
  integer*4 nz, numVals, maxVals, indAdoc, iTypeAdoc, iTypeData, numFound
  integer*4 i, ind, ival, itmp
  character*20 valString
  character*20 keys(9) /'TiltAngle', 'N', 'StagePosition', 'Magnification', &
      'Intensity', 'ExposureDose', 'PixelSpacing', 'Defocus', &
      'ExposureTime'/
  integer*4 iwhich(9) /2, 1, 3, 1, 2, 2, 2, 2, 2/

  numVals = 0
  numFound = 0
  if (iTypeAdoc < 1 .or. iTypeAdoc > 9) return

  call get_metadata_by_key(indAdoc, iTypeAdoc, nz, keys(iTypeData), iwhich(iTypeData), &
      val1, val2, val3, valString, numVals, numFound, maxVals, izPiece)
  return
end subroutine get_metadata_items

! !
! Returns values of almost any available type from an image metadata file written by
! SerialEM or an HDF file in which such data have been incorporated.  The metadata file
! should be opened first with @@autodoc.html#AdocOpenImageMetadata@, or 
! @@autodoc.html#AdocGetImageMetaInfo@ should be called on the autodoc of an HDF file.
! The routine relies on the Z values of the sections in the file as listed in [izpiece],
! which should be obtained first with @@get_metadata_pieces@.
! ^  [indAdoc] = autodoc index of metadata file
! ^  [iTypeAdoc] = type of metadata file: 1 for one file, 2 for series, 3 for other 
! autodoc
! ^  [nz] = number of sections or pieces
! ^  [key] = Complete name string for data to retrieve (case-sensitive)
! ^  [ivalType] = 1 for single integer, 2 for single float, 3 for two floats, 4 for
! three floats, or 0 for string
! ^  [val1], [val2], [val3] = arrays for one, two or three values to be returned.  [val2]
! and [val3] can be single floats if not needed, but when returning strings, [val1] is
! needed because it will be set to 0 for every index where a string is found.
! ^  [valString] = array for value strings to be returned
! ^  [nvals] = # of values returned (or highest # if there are gaps)
! ^  [nfound] = # of values actually found in metadata
! ^  [maxvals] = size of [val1] and [val2] arrays
! ^  [izpiece] = Z value of each section in file
! !
subroutine get_metadata_by_key(indAdoc, iTypeAdoc, nz, key, ivalType, val1, val2, val3, &
    valString, numVals, numFound, maxVals, izPiece)
  implicit none
  real*4 val1(*), val2(*), val3(*)
  integer*4 izPiece(*)
  integer*4 nz, numVals, maxVals, indAdoc, iTypeAdoc, ivalType,numFound
  character*(*) key, valString(*)
  integer*4 i, ind, ival, itmp
  character*20 globalName, sectNames(3) /'ZValue', 'Image', 'ZValue'/
  integer*4 AdocSetCurrent, AdocGetTwoFloats, AdocGetFloat, AdocGetInteger, AdocGetString
  integer*4 AdocLookupByNameValue, AdocGetStandardNames, AdocGetThreeFloats
  if (AdocGetStandardNames(globalName, sectNames(1)) == 0) &
      sectNames(3) = sectNames(1)
  !
  if (AdocSetCurrent(indAdoc) .ne. 0) then
    write(*,'(/,a,a)') 'ERROR: GET_METADATA_ITEMS - FAILED TO SET AUTODOC INDEX'
    call exit(1)
  endif

  numVals = 0
  numFound = 0
  do i = 1, nz
    ival = izPiece(i) + 1
    if (ival < 1) then
      write(*,'(/,a,a)') 'ERROR: GET_METADATA_ITEMS', &
          ' - VALUE ARRAY NOT DESIGNED FOR NEGATIVE Z VALUES'
      call exit(1)
    endif
    if (ival > maxVals) then
      write(*,'(/,a,a)') 'ERROR: GET_METADATA_ITEMS - ARRAY NOT BIG ENOUGH FOR DATA'
      call exit(1)
    endif

    ind = i
    if (iTypeAdoc == 3) then
      ind  = AdocLookupByNameValue(sectNames(iTypeAdoc), i - 1)
      if (ind < 0) cycle
    endif

    if (ivalType == 0) then
      if (AdocGetString(sectNames(iTypeAdoc), ind, key, valString(ival)) == 0) then
        val1(ival) = 0.
        numFound = numFound + 1
      endif
    else if (ivalType == 1) then
      if (AdocGetInteger(sectNames(iTypeAdoc), ind, key, itmp) == 0) then
        val1(ival) = itmp
        numFound = numFound + 1
      endif
    else if (ivalType == 2) then
      if (AdocGetFloat(sectNames(iTypeAdoc), ind, key, val1(ival)) == 0) &
          numFound = numFound + 1
    else if (ivalType == 3) then
      if (AdocGetTwoFloats(sectNames(iTypeAdoc), ind, key, &
          val1(ival), val2(ival)) == 0) numFound = numFound + 1
    else if (ivalType == 4) then
      if (AdocGetThreeFloats(sectNames(iTypeAdoc), ind, key, &
          val1(ival), val2(ival), val3(ival)) == 0) numFound = numFound + 1
    endif
    numVals = max(numVals, ival)
  enddo
  return
end subroutine get_metadata_by_key
