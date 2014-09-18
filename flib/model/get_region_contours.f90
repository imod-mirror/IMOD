! GET_REGION_CONTOURS will read the MODELFILE as a small model, and
! extract contours.  It first determines whether the contours lie in
! X/Y or X/Z planes, setting IFFLIP to 1 if they are in Y/Z planes.
! It extracts the planar points into XVERT, YVERT, where INDVERT is an
! index to the start of each contour, NVERT contains the number of
! points in each contour, ZCONT is the contour Y or Z value, NCONT is
! the number of contours.  LIMCONT and LIMVERT specify the limiting
! dimensions for contours and vertices.  Pass LIMCONT as <= 0 on an initial call
! to have them returned with the needed sizes.  If IMUNIT is greater than 0
! the model is scaled to the index coordinates of the image file open
! on that unit
!
! If contours are not planar in either direction it issues a warning
! using PROGNAME as the program name and and adopts the flip value
! from the model header, which is not necessarily correct.
!
! $Id$
!
subroutine get_region_contours(modelFile, progname, xVerts, yVerts, numVerts, &
    indVert, contourZ,  numConts, ifFlip, limCont, limVert, imUnit)
  implicit none
  include 'smallmodel.inc90'
  character*(*) modelFile, progname
  real*4 xVerts(*), yVerts(*), contourZ(*)
  integer*4 numVerts(*), indVert(*), numConts, ifFlip, limCont, limVert, imUnit
  integer*4 ip, ipt, iobj, iy, iz, indY, indZ, indCur, ierr, getImodHead
  logical*4 exist, readSmallMod, planarInZ, planarInY
  real*4 xyScale, zScale, xOffset, yOffset, zOffset
  !
  exist = readSmallMod(modelFile)
  if (.not.exist) call exitError('ERROR READING MODEL')
  if (imUnit > 0) then
    call scaleModelToImage(imUnit, 0)
  else
    call scale_model(0)
  endif
  numConts = 0
  !
  ! figure out if all contours are coplanar Z or Y
  ! Loop through contours until both planar flags go false or all done
  !
  iobj = 1
  planarInZ = .true.
  planarInY = .true.
  do while (iobj <= max_mod_obj .and. (planarInZ .or. planarInY))
    if (npt_in_obj(iobj) >= 3) then
      ip = 2
      ipt = abs(object(ibase_obj(iobj) + 1))
      iy = nint(p_coord(2, ipt))
      iz = nint(p_coord(3, ipt))
      do while (ip <= npt_in_obj(iobj) .and. (planarInZ .or. planarInY))
        ipt = abs(object(ibase_obj(iobj) + ip))
        if (nint(p_coord(2, ipt)) .ne. iy) planarInY = .false.
        if (nint(p_coord(3, ipt)) .ne. iz) planarInZ = .false.
        ip = ip + 1
      enddo
    endif
    iobj = iobj + 1
  enddo
  !
  ! Set flip flag or fallback to what is in model header
  !
  if (planarInZ .and. .not. planarInY) then
    ifFlip = 0
  else if (planarInY .and. .not. planarInZ) then
    ifFlip = 1
  else
    ierr = getImodHead(xyScale, zScale, xOffset, yOffset, zOffset, ifFlip)
    if (limCont > 0) write(*,'(/,a,a,a)') 'WARNING: ', progname,  &
        ' - CONTOURS NOT ALL COPLANAR, USING HEADER FLIP FLAG'
  endif
  indY = 2
  indZ = 3
  if (ifFlip .ne. 0) then
    indY = 3
    indZ = 2
  endif
  !
  ! If limCont <= 0, add up number of contours and points and return
  if (limCont <= 0) then
    limCont = 0
    limVert = 0
    do iobj = 1, max_mod_obj
      if (npt_in_obj(iobj) >= 3) then
        limCont = limCont + 1
        limVert = limVert + npt_in_obj(iobj)
      endif
    enddo
    return
  endif
  !
  ! Load planar data into x/y arrays
  !
  indCur = 0
  do iobj = 1, max_mod_obj
    if (npt_in_obj(iobj) >= 3) then
      numConts = numConts + 1
      if (numConts > limCont) call exitError('TOO MANY CONTOURS IN MODEL')
      numVerts(numConts) = npt_in_obj(iobj)
      if (indCur + numVerts(numConts) > limVert) call exitError( &
          'TOO MANY POINTS IN CONTOURS')
      do ip = 1, numVerts(numConts)
        ipt = abs(object(ibase_obj(iobj) + ip))
        xVerts(ip + indCur) = p_coord(1, ipt)
        yVerts(ip + indCur) = p_coord(indY, ipt)
      enddo
      contourZ(numConts) = p_coord(indZ, ipt)
      indVert(numConts) = indCur + 1
      indCur = indCur + numVerts(numConts)
    endif
  enddo
  if (progname .ne. 'FILLTOMO') &
      print *,numConts, ' contours available for deciding which patches to analyze'
  return
end subroutine get_region_contours


! summarizeDrops outputs a summary of residuals dropped as outliers,
! breaking them into 10 bins from the minimum to either the maximum or
! 5 SDs above the mean, whichever is less.  DROPSUM has the residuals,
! NLISTD is the number of values, and meanText is text to indicate if
! they are mean residuals or not.
!
subroutine summarizeDrops(dropSum, numListDrop, meanText)
  implicit none
  character*(*) meanText
  real*4 dropSum(*)
  integer*4 numListDrop, i, j, inBin
  real*4 dropMin, dropMax, dropAvg, dropSD, dropSEM, binLow, binHigh, dropBin
  !
  dropMin = 1.e10
  dropMax = 0.
  do i = 1, numListDrop
    dropMin = min(dropMin, dropSum(i))
    dropMax = max(dropMax, dropSum(i))
  enddo
  call avgsd(dropSum, numListDrop, dropAvg, dropSD, dropSEM)
  dropBin = (min(dropMax, dropAvg + 5 * dropSD) - dropMin) / 10.
  !
  do j = 1, 10
    binLow = dropMin + (j - 1) * dropBin
    binHigh = binLow + dropBin
    if (j == 10) binHigh = dropMax + 0.001
    inBin = 0
    do i = 1, numListDrop
      if (dropSum(i) >= binLow .and. dropSum(i) < binHigh) &
          inBin = inBin + 1
    enddo
    if (inBin > 0) write(*,109) inBin, meanText, binLow, binHigh
109 format(i8,' with ',a,'residuals in',f10.2,' -',f10.2)
  enddo
  return
end subroutine summarizeDrops
