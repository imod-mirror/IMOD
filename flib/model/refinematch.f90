! * * * * * REFINEMATCH * * * * * *
!
! REFINEMATCH will solve for a general 3-dimensional linear
! transformation to align two volumes to each other.  It performs
! multiple linear regression on the  displacements between the volumes
! determined at a matrix of positions.  The displacements must be
! contained in a file with the following form:
!
! Number of displacements
! One line for each displacement consisting of the X, Y, and Z
! .  coordinates in the first volume, then the displacements in X, Y
! .  and Z involved in moving from the first to the second volume
!
! See man page for details
!
! David Mastronarde, 1995
!
! $Id$
!
program refinematch
  implicit none
  integer IDIM, LIMVERT, MATCOLS
  parameter (IDIM = 100000, LIMVERT = 100000, MATCOLS = 20)
  real*4 fitMat(MATCOLS,IDIM), ccc(IDIM)
  real*4 cx(3), dx(3), a(3,3), delXYZ(3), devXYZ(3)
  real*4 devXYZmax(3), fitCenter(3), cxlast(3), freinp(20)
  integer*4 nxyz(3), idrop(IDIM)
  logical inside, oneLayer(3), haveCCC
  real*4 xVerts(LIMVERT), yVerts(LIMVERT), contourZ(IDIM)
  integer*4 indVert(IDIM), numVerts(IDIM)
  character*320 filename
  !
  integer*4 numConts, ierr, ifFlip, i, indY, indZ, indcur, iobj, numData, numFit
  real*4 fracDrop, cccRes
  integer*4 ifUse, icont, icontMin, j, ind, maxDrop, ndrop, ipntMax, ip, ipt, numFields
  real*4 dzMin, critProb, elimMin, absProbCrit, devMean, devSD, devMax, stopLim, dz
  integer*4 icolFixed

  logical pipInput
  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetInteger
  integer*4 PipGetString, PipGetFloat, PipGetTwoFloats
  integer*4 PipGetInOutFile
  !
  ! fallbacks from ../../manpages/autodoc2man -3 2  refinematch
  !
  integer numOptions
  parameter (numOptions = 11)
  character*(40 * numOptions) options(1)
  options(1) = &
      'patch:PatchFile:FN:@region:RegionModel:FN:@'// &
      'volume:VolumeOrSizeXYZ:FN:@output:OutputFile:FN:@'// &
      'residual:ResidualPatchOutput:FN:@limit:MeanResidualLimit:F:@'// &
      'maxfrac:MaxFractionToDrop:F:@minresid:MinResidualToDrop:F:@'// &
      'prob:CriterionProbabilities:FP:@param:ParameterFile:PF:@'// &
      'help:usage:B:'
  !
  fracDrop = 0.1
  critProb = 0.01
  elimMin = 0.5
  absProbCrit = 0.002
  haveCCC = .false.
  !
  call PipReadOrParseOptions(options, numOptions, 'refinematch', &
      'ERROR: REFINEMATCH - ', .true., 3, 1, 1, numOptArg, numNonOptArg)
  pipInput = numOptArg + numNonOptArg > 0
  !
  ! Open patch file
  !
  if (PipGetInOutFile('PatchFile', 1, &
      'Name of file with correlation positions and results', filename) .ne. 0) &
      call exitError('NO INPUT PATCH FILE SPECIFIED')
  call dopen(1, filename, 'old', 'f')

  if (.not. pipInput) print *,'Enter file name or NX, NY, NZ of tomogram being matched to'
  call get_nxyz(pipInput, 'VolumeOrSizeXYZ', 'REFINEMATCH', 1, nxyz)

  if (pipInput) then
    filename = ' '
    ierr = PipGetString('RegionModel', filename)
    ierr = PipGetFloat('MaxFractionToDrop', fracDrop)
    ierr = PipGetFloat('MinResidualToDrop', elimMin)
    ierr = PipGetTwoFloats('CriterionProbabilities', critProb, absProbCrit)
    if (PipGetFloat('MeanResidualLimit', stopLim) > 0) call exitError( &
        'YOU MUST ENTER A MEAN RESIDUAL LIMIT')
  else
    write(*,'(1x,a,/,a,$)') &
        'Enter name of model file with contour enclosing area to ' &
        //'use,', ' or Return to use all patches: '
    read(*,'(a)') filename
  endif

  numConts = 0
  if (filename .ne. ' ') then
    call get_region_contours(filename, 'REFINEMATCH', xVerts, yVerts, numVerts, &
        indVert, contourZ, numConts, ifFlip, IDIM, LIMVERT, 0)
  else
    ifFlip = 0
    if (nxyz(2) < nxyz(3)) ifFlip = 1
  endif
  indY = 2
  if (ifFlip .ne. 0) indY = 3
  indZ = 5 - indY
  !
  read(1,*) numData
  if (numData > IDIM) call exitError('TOO MANY POINTS FOR DATA ARRAYS')

  do j = 1, 3
    oneLayer(j) = .true.
    cxlast(j) = 0.
  enddo
  numFit = 0
  do i = 1, numData
    ccc(i) = 0.
    !
    ! these are center coordinates and location of the second volume
    ! relative to the first volume
    !
    read(1, '(a)') filename
    call frefor(filename, freinp, numFields)
    do j = 1, 3
      cx(j) = freinp(j)
      dx(j) = freinp(j + 3)
      ! read(1,*) (cx(j), j=1, 3), (dx(j), j=1, 3)
      ! do j = 1, 3
      if (i > 1 .and. cx(j) .ne. cxlast(j)) oneLayer(j) = .false.
      cxlast(j) = cx(j)
    enddo
    if (numFields > 6) then
      haveCCC = .true.
      ccc(i) = freinp(7)
    endif

    ifUse = 1
    if (numConts > 0) then
      ifUse = 0
      !
      ! find nearest contour in Z and see if patch is inside it
      !
      dzMin = 100000.
      do icont = 1, numConts
        dz = abs(cx(indZ) - contourZ(icont))
        if (dz < dzMin) then
          dzMin = dz
          icontMin = icont
        endif
      enddo
      ind = indVert(icontMin)
      if (inside(xVerts(ind), yVerts(ind), numVerts(icontMin), cx(1), cx(indY))) &
          ifUse = 1~/checkout/IMOD-4.7/flib/model/refinematch -pat eric5_autopatch.out -out eric5ref.xf -vol eric5a.rec -lim 0.3 -res eric5_respatch.out -red eric5_redpatch.out -reg eric5_region.mod
    endif
    !
    if (ifUse > 0) then
      numFit = numFit + 1
      ! write(*,'(3f6.0)') cx(1), cx(3), cx(2)
            haveCCC = .true.
            ccc(nfill) = freinp(7)
          endif
      do j = 1, 3
        !
        ! the regression requires coordinates of second volume as
        ! independent variables (columns 1-3), those in first volume
        ! as dependent variables (stored in 5-7), to obtain
        ! transformation to get from second to first volume
        ! cx+dx in second volume matches cx in first volume
        !
        fitMat(j + 4, numFit) = cx(j) - 0.5 * nxyz(j)
        fitMat(j, numFit) = fitMat(j + 4, numFit) + dx(j)
      enddo
    endif
  enddo

  close(1)
  numData = numFit
  if (numData < 4) call exitError('TOO FEW DATA POINTS FOR FITTING')
  print *,numData, ' data points will be used for fit'
  !
  if (.not.pipInput) then
    write(*,'(1x,a,$)') 'Mean residual above which to STOP and '// &
        'exit with an error: '
    read(5,*) stopLim
  endif
  !
  icolFixed = 0
  do i = 1, 3
    if (icolFixed .ne. 0 .and. oneLayer(i)) call exitError( &
        'CANNOT FIT TO PATCHES THAT EXTEND IN ONLY ONE DIMENSION')
    if (oneLayer(i)) icolFixed = i
  enddo
  if (icolFixed > 0) print *,'There is only one layer of patches', &
      ' in the ', char(ichar('W') + icolFixed), ' dimension'
  !
  maxDrop = nint(fracDrop * numData)
  call solve_wo_outliers(fitMat, MATCOLS, numData, 3, icolFixed, maxDrop, critProb, &
      absProbCrit, elimMin, idrop, ndrop, a, delXYZ, fitCenter, devMean, devSD, devMax, &
      ipntMax, devXYZmax)
  !
  if (ndrop .ne. 0) then
    write(*,104) ndrop
104 format(i3,' patches dropped by outlier elimination:')
    if (ndrop <= 10) then
      print *,'    patch position     residual'
      do i = 1, ndrop
        write(*,106) (fitMat(j, numData + i - ndrop), j = 1, 4)
106     format(3f7.0,f9.2)
      enddo
    else
      do i = 1, ndrop
        contourZ(i) = fitMat(4, numData + i - ndrop)
      enddo
      call summarizeDrops(contourZ, ndrop, ' ')
    endif
  endif
  !
  write(*,101) devMean, devMax
101 format(/,' Mean residual',f8.3,',  maximum',f8.3,/)
  !
  print *,'Refining transformation:'
  write(*,102) ((a(i, j), j = 1, 3), delXYZ(i), i = 1, 3)
102 format(3f10.6,f10.3)
  !
  if (pipInput) then
    !
    ! For patch residual output, painfully recompute the original data!
    ! First make a proper index from original to ordered rows
    do i = 1, numData
      fitMat(6, nint(fitMat(5, i))) = i
    enddo
    if (PipGetString('ResidualPatchOutput', filename) == 0) then
      call dopen(1, filename, 'new', 'f')
      write(1, '(i7,a)') numData, ' positions'
      do i = 1, numData
        write(1, 110) (nint(fitMat(j + 11, i) + 0.5 * nxyz(j)), j = 1, 3), &
            ((fitMat(j, i) - fitMat(j + 4, i)), j = 8, 10), fitMat(4, nint(fitMat(6, i)))
110     format(3i6,3f9.2,f10.2)
      enddo
      close(1)
    endif
    !
    ! For reduced vectors, put out the residual vector stored in cols 15-17
    ! or ordered data
    if (PipGetString('ReducedVectorOutput', filename) == 0) then
      call dopen(1, filename, 'new', 'f')
      write(1, '(i7,a)') numData, ' positions'
      do i = 1, numData
        ind = nint(fitMat(6, i))
        cccRes = fitMat(4, ind)
        if (haveCCC) cccRes = ccc(i)
        write(1, 111) (nint(fitMat(j + 11, i) + 0.5 * nxyz(j)), j = 1, 3), &
            (fitMat(j, ind), j = 15, 17), cccRes
111     format(3i6,3f9.2,f12.4)
      enddo
      close(1)
    endif
    filename = ' '
    ierr = PipGetString('OutputFile', filename)
  else
    print *,'Enter name of file to place transformation in, or Return for none'
    read(5, '(a)') filename
  endif
  if (filename .ne. ' ') then
    call dopen(1, filename, 'new', 'f')
    write(1, 102) ((a(i, j), j = 1, 3), delXYZ(i), i = 1, 3)
    close(1)
  endif
  if (devMean > stopLim) then
    write(*,'(/,a)') 'REFINEMATCH - MEAN RESIDUAL TOO HIGH;'// &
        ' EITHER RAISE THE LIMIT OR USE WARPING'
    call exit(2)
  endif
  call exit(0)
end program refinematch
