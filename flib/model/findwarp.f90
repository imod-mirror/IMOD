! * * * * * FINDWARP * * * * * *
!
! FINDWARP will solve for a series of general 3-dimensional linear
! transformations that can then be used by WARPVOL to align two
! volumes to each other.  It performs a series of multiple linear
! regression on subsets of the displacements between the volumes
! determined at a matrix of positions (patches) .
!
! See man page for details
!
! $Id$
!
program findwarp
  implicit none
  integer LIMTARG
  parameter (LIMTARG = 100)
  real*4 firstAmat(3,3), firstDelta(3), a(3,3), delXYZ(3), cenSaveMin(3), cenSaveMax(3)
  real*4 devXYZmax(3), cenLocal(3), amatTmp(3,3), delTmp(3), cenXYZsum(3)
  integer*4 nxyzVol(3)
  real*4 debugXYZ(3), dxLocal, dyLocal, dzLocal
  integer*4, allocatable :: indDropped(:), numTimesDropped(:), idrop(:)
  real*4, allocatable :: xVerts(:), yVerts(:), contourZ(:), dropSum(:), fitMat(:,:)
  integer*4, allocatable :: indVertStart(:), numVerts(:), listPositions(:,:)
  real*4, allocatable :: amatSave(:,:,:), cenToSave(:,:), cenXYZ(:,:), vecXYZ(:,:)
  real*4, allocatable :: delXYZsave(:,:), residSum(:), devMeanAuto(:)
  logical, allocatable :: solved(:), exists(:)
  integer*4, allocatable :: numXautoFit(:), numYautoFit(:), numZautoFit(:), inDiag2(:)
  integer*4, allocatable :: inRowX(:), inRowY(:), inRowZ(:), numResid(:), inDiag1(:)
  character*320 filename, residFile
  real*4 cenXYZin(3), vecXYZin(3), targetResid(LIMTARG)
  integer*4 numXYZpatch(3), indXYZ(3), numZpatchUse, limPatch, limAxis, limDiag, limFit
  integer*4 numXfit, numYfit, numZfit, numXYXfit(3), numXpatchUse, numYpatchUse
  integer*4 numXfitIn, numYfitIn, numZfitIn, numXYZfitIn(3), numXYZpatchUse(3)
  integer*4 numXYZfit(3)
  equivalence (numXpatchTot, numXYZpatch(1)), (numYpatchTot, numXYZpatch(2)), &
      (numZpatchTot, numXYZpatch(3))
  equivalence (numXfit, numXYXfit(1)), (numYfit, numXYXfit(2)), (numZfit, numXYXfit(3))
  equivalence (numXpatchUse, numXYZpatchUse(1)), (numYpatchUse, numXYZpatchUse(2)), &
      (numZpatchUse, numXYZpatchUse(3))
  equivalence (numXfitIn, numXYZfitIn(1)), (numYfitIn, numXYZfitIn(2)), &
      (numZfitIn, numXYZfitIn(3))
  equivalence (numXfit, numXYZfit(1)), (numYfit, numXYZfit(2)), (numZfit, numXYZfit(3))
  integer*4 numData, numPosInFile, numXpatchTot, numYpatchTot, numZpatchTot, i, j, ind
  integer*4 numConts, ierr, indY, indZ, numTarget, indTarget, intCenPos, k, itmp
  integer*4 numXoffset, numYoffset, numZoffset, ifSubset, ifLocalSlabs, ifdebug
  real*4 ratioMin, ratioMax, fracDrop, probCrit, absProbCrit, elimMinResid
  integer*4 ifAuto, numAuto, ix, iy, iz, indAuto, limVert, limCont, matColDim
  integer*4 numLocalDone, numXexclHigh, numYexclhigh, numZexclHigh
  integer*4 ifDiddle, numXlocal, numYlocal, numZlocal, numZero, numDevSum
  integer*4 indUse, nlistDropped, numDropTot, locX, locY, locZ, lx, ly, lz, ifUse
  real*4 devMeanMin, devMeanSum, devMaxSum, devMaxMax
  real*4 dzMin, dz, devMax, devMean, devSD, discount
  real*4 devMeanAvg, devMaxAvg, devMeanMax, determMean
  integer*4 icontMin, icont, indv, indLcl, ipntMax, maxDrop, numLowDeterm
  integer*4 ifInDrop, numDrop, ifFlip, indPatch, indLocal, icolFixed, nyDiag
  character*5 rowSlabText(2) /'rows ', 'slabs'/
  character*5 rowSlabCapText(2) /'ROWS ', 'SLABS'/
  character*1 yzText(2) /'Y', 'Z'/
  logical*4 debugHere
  integer*4 numberInList
  !
  logical pipInput
  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetInteger, PipGetTwoIntegers
  integer*4 PipGetString, PipGetFloat, PipGetFloatArray, PipGetTwoFloats
  integer*4 PipGetInOutFile, PipGetThreeFloats
  !
  ! fallbacks from ../../manpages/autodoc2man -2 2  findwarp
  !
  integer numOptions
  parameter (numOptions = 19)
  character*(40 * numOptions) options(1)

  indPatch(ix, iy, iz) = ix + (iy - 1) * numXpatchTot + (iz - 1) * numXpatchTot * &
      numYpatchTot
  indLocal(ix, iy, iz) = ix + (iy - 1) * numXlocal + (iz - 1) * numXlocal * numYlocal

  options(1) = &
      'patch:PatchFile:FN:@output:OutputFile:FN:@region:RegionModel:FN:@'// &
      'volume:VolumeOrSizeXYZ:FN:@initial:InitialTransformFile:FN:@'// &
      'residual:ResidualPatchOutput:FN:@target:TargetMeanResidual:FA:@'// &
      'measured:MeasuredRatioMinAndMax:FP:@xskip:XSkipLeftAndRight:IP:@'// &
      'yskip:YSkipLowerAndUpper:IP:@zskip:ZSkipLowerAndUpper:IP:@'// &
      'rowcol:LocalRowsAndColumns:IP:@slabs:LocalSlabs:I:@'// &
      'maxfrac:MaxFractionToDrop:F:@minresid:MinResidualToDrop:F:@'// &
      'prob:CriterionProbabilities:FP:@'// &
      'discount:DiscountIfZeroVectors:F:@param:ParameterFile:PF:@'// &
      'help:usage:B:'
  !
  matColDim = 20
  numXoffset = 0
  numYoffset = 0
  numZoffset = 0
  numXexclHigh = 0
  numYexclhigh = 0
  numZexclHigh = 0
  ifSubset = 0
  ifDebug = 0
  ratioMin = 4.0
  ratioMax = 20.0
  fracDrop = 0.1
  probCrit = 0.01
  absProbCrit = 0.002
  elimMinResid = 0.5
  ifLocalSlabs = 0
  residFile = ' '
  discount = 0.
  limFit = 40000
  numAuto = 0
  !
  call PipReadOrParseOptions(options, numOptions, 'findwarp', &
      'ERROR: FINDWARP - ', .true., 3, 1, 1, numOptArg, numNonOptArg)
  pipInput = numOptArg + numNonOptArg > 0
  !
  ! Open patch file and figure out sampled positions
  !
  if (PipGetInOutFile('PatchFile', 1, &
      'Name of file with correlation positions and results', filename) .ne. 0) &
      call exitError('NO INPUT PATCH FILE SPECIFIED')

  call dopen(1, filename, 'old', 'f')
  read(1,*) numData
  allocate(listPositions(numData + 4,3), stat = ierr)
  call memoryError(ierr, 'ARRAY FOR POSITION LIST')

  numXpatchTot = 0
  numYpatchTot = 0
  numZpatchTot = 0
  do i = 1, numData
    read(1,*) (cenXYZin(j), j = 1, 3)
    do j = 1, 3
      intCenPos = nint(cenXYZin(j))
      if (numberInList(intCenPos, listPositions(1, j), numXYZpatch(j), 0) == 0) then
        numXYZpatch(j) = numXYZpatch(j) + 1
        listPositions(numXYZpatch(j), j) = intCenPos
      endif
    enddo
  enddo
  !
  ! sort the position lists
  !
  do i = 1, 3
    do j = 1, numXYZpatch(i) - 1
      do k = j, numXYZpatch(i)
        if (listPositions(j, i) > listPositions(k, i)) then
          itmp = listPositions(j, i)
          listPositions(j, i) = listPositions(k, i)
          listPositions(k, i) = itmp
        endif
      enddo
    enddo
  enddo
  !
  ! Allocate arrays based on full set of patch positions
  limPatch = numXpatchTot * numYpatchTot * numZpatchTot + 10
  limAxis = maxval(numXYZpatch) + 10
  limDiag = 2 * limAxis
  allocate(amatSave(3,3,limPatch), cenToSave(3,limPatch), delXYZsave(3,limPatch), &
      residSum(limPatch), solved(limPatch), exists(limPatch), cenXYZ(limPatch,3), &
      vecXYZ(limPatch,3), inRowX(limAxis), inRowY(limAxis), inRowZ(limAxis), &
      numResid(limPatch), inDiag1(0:limDiag), inDiag2(0:limDiag), indDropped(limPatch), &
      numTimesDropped(limPatch), dropSum(limPatch), stat = ierr)
  call memoryError(ierr, 'ARRAYS FOR PATCHES')
  !
  numXpatchUse = numXpatchTot
  numYpatchUse = numYpatchTot
  numZpatchUse = numZpatchTot
  print *,'Number of patches in X, Y and Z is:', numXpatchTot, numYpatchTot, numZpatchTot
  rewind(1)
  read(1,*) numData
  !
  if (.not. pipInput) print *,'Enter NX, NY, NZ or name of file ', &
      'for tomogram being matched to'
  call get_nxyz(pipInput, 'VolumeOrSizeXYZ', 'FINDWARP', 1, nxyzVol)
  !
  ! mark positions as nonexistent and fill cx from list positions
  !
  do k = 1, numZpatchTot
    do j = 1, numYpatchTot
      do i = 1, numXpatchTot
        ind = indPatch(i, j, k)
        exists(ind) = .false.
        cenXYZ(ind, 1) = listPositions(i, 1)
        cenXYZ(ind, 2) = listPositions(j, 2)
        cenXYZ(ind, 3) = listPositions(k, 3)
      enddo
    enddo
  enddo
  !
  ! read each line, look up positions in list and store in right place
  !
  do i = 1, numData
    !
    ! these are center coordinates and location of the second volume
    ! relative to the first volume
    !
    read(1,*) (cenXYZin(j), j = 1, 3), (vecXYZin(j), j = 1, 3)
    do j = 1, 3
      intCenPos = nint(cenXYZin(j))
      k = 1
      do while(k <= numXYZpatch(j) .and. intCenPos > listPositions(k, j))
        k = k + 1
      enddo
      indXYZ(j) = k
    enddo
    ind = indPatch(indXYZ(1), indXYZ(2), indXYZ(3))
    exists(ind) = .true.
    do j = 1, 3
      cenXYZ(ind, j) = cenXYZin(j)
      vecXYZ(ind, j) = vecXYZin(j)
    enddo
  enddo
  !
  numPosInFile = numData
  close(1)
  deallocate(listPositions, stat=ierr)
  !
  ! get patch region model, which should give flip value; otherwise set flip from patches
  !
  if (pipInput) then
    filename = ' '
    ierr = PipGetString('RegionModel', filename)
  else
    write(*,'(1x,a,/,a,$)') 'Enter name of model file with contour enclosing area to ' &
        //'use,', ' or Return to use all patches: '
    read(*,'(a)') filename
  endif
  numConts = 0
  if (filename .ne. ' ') then
    limCont = -1
    limVert = -1
    call get_region_contours(filename, 'FINDWARP',delTmp, delTmp, ix, &
        iy, ierr, numConts, ifFlip, limCont, limVert, 0)
    limVert = limVert + 10
    limCont = limCont + 4
    allocate(xVerts(limVert), yVerts(limVert), contourZ(limCont),  &
        indVertStart(limCont), numVerts(limCont), stat = ierr)
    call memoryError(ierr, 'ARRAYS FOR BOUNDARY CONTOURS')
    call get_region_contours(filename, 'FINDWARP', xVerts, yVerts, numVerts, &
        indVertStart, contourZ, numConts, ifFlip, limCont, limVert, 0)
  else
    ifFlip = 0
    if (numYpatchTot < numZpatchTot) ifFlip = 1
  endif
  !
  ! Set indexes to Y-extent (row) and thickness (slab) variables
  indY = 2
  if (ifFlip .ne. 0) indY = 3
  indZ = 5 - indY
  numXYZfitIn(indZ) = numXYZpatchUse(indZ)
  numXYZfit(indZ) = numXYZpatchUse(indZ)
  !
  ! aspectmax=3.
  !
8 if (pipInput) then
    ifAuto = PipGetTwoIntegers('LocalRowsAndColumns', numXfitIn, numXYZfitIn(indY))
  else
    write(*,'(1x,a,$)') '1 to find best warping automatically, 0 '// &
        'to proceed interactively: '
    read(5,*) ifAuto
  endif
  !
  if (ifAuto .ne. 0) then
    !
    ! initialize auto 1: get target and ratio parameters
    !
    if (pipInput) then
      numTarget = 0
      if (PipGetFloatArray('TargetMeanResidual', targetResid, numTarget, &
          LIMTARG) > 0) call exitError( &
          'TARGET MEAN RESIDUAL MUST BE ENTERED FOR AUTOMATIC FITS')
      ierr = PipGetTwoFloats('MeasuredRatioMinAndMax', ratioMin, ratioMax)
    else
      write(*,'(1x,a,$)') 'One or more mean residuals to achieve: '
      read(5, '(a)') filename
      call frefor(filename, targetResid, numTarget)
      write(*,'(1x,a,/,a,2f5.1,a,$)') 'Minimum and maximum ratio of '// &
          'measurements to unknowns to test', '  (/ for' &
          , ratioMin, ratioMax, '): '
      read(5,*) ratioMin, ratioMax
    endif
    ! write(*,'(1x,a,f5.0,a,$)') &
    ! 'Maximum aspect ratio to allow in an area to be fit (/ for' &
    ! , aspectmax, '): '
    ! read(5,*) aspectmax
  endif
  !
  ! Get rows, columns, slabs to exclude and check entries
  !
  if (pipInput) then
    ierr = PipGetTwoIntegers('XSkipLeftAndRight', numXoffset, numXexclHigh) + &
        PipGetTwoIntegers('YSkipLowerAndUpper', numYoffset, numYexclhigh) + &
        PipGetTwoIntegers('ZSkipLowerAndUpper', numZoffset, numZexclHigh)
    ifSubset = 3 - ierr
    ierr = PipGetFloat('MaxFractionToDrop', fracDrop)
    ierr = PipGetFloat('MinResidualToDrop', elimMinResid)
    ierr = PipGetTwoFloats('CriterionProbabilities', probCrit, absProbCrit)
    ierr = PipGetString('ResidualPatchOutput', residFile)
    ierr = PipGetFloat('DiscountIfZeroVectors', discount)
    ifDebug = 1 - PipGetThreeFloats('DebugAtXYZ', debugXYZ(1), debugXYZ(2), &
        debugXYZ(3))
  else
    write(*,'(1x,a,$)') '0 to include all positions, or 1 to '// &
        'exclude rows or columns of patches: '
    read(5,*) ifSubset
  endif
  !
  if (ifSubset .ne. 0) then
10  if (.not. pipInput) then
      numXexclHigh = numXpatchTot - numXoffset - numXpatchUse
      write(*,'(1x,a,2i3,a,$)') '# of columns to exclude' &
          //' on the left and right in X (/ for', numXoffset, numXexclHigh, '): '
      read(5,*) numXoffset, numXexclHigh
    endif
    numXpatchUse = numXpatchTot - numXoffset - numXexclHigh
    if (numXpatchUse + numXoffset > numXpatchTot .or. numXpatchUse < 2) then
      if (ifAuto .ne. 0) call exitError( &
          'ILLEGAL ENTRY FOR NUMBER OF COLUMNS TO EXCLUDE IN X')
      print *,'Illegal entry'
      numXoffset = 0
      numXpatchUse = numXpatchTot
      go to 10
    endif

12  if (.not. pipInput) then
      numYexclHigh = numYpatchTot - numYoffset - numYpatchUse
      write(*,'(1x,a,a,a,2i3,a,$)') '# of ', rowSlabText(1 + ifFlip), ' to exclude' &
          //' on the bottom and top in Y (/ for', numYoffset, numYexclHigh, '): '
      read(5,*) numYoffset, numYexclHigh
    endif
    numYpatchUse = numYpatchTot - numYoffset - numYexclHigh
    if (numYpatchUse + numYoffset > numYpatchTot .or. numYpatchUse < 1 ) then
      if (ifAuto .ne. 0) call exitError('ILLEGAL ENTRY FOR NUMBER OF '// &
          rowSlabCapText(1 + ifFlip) //' TO EXCLUDE IN Y')
      print *,'Illegal entry'
      numYoffset = 0
      numYpatchUse = numYpatchTot
      go to 12
    endif

14  if (.not. pipInput) then
      numZexclHigh = numZpatchTot - numZoffset - numZpatchUse
      write(*,'(1x,a,a,a,2i3,a,$)') '# of ', rowSlabText(2 - ifFlip), ' to exclude' &
          //' on the bottom and top in Z (/ for', numZoffset, numZexclHigh, '): '
      read(5,*) numZoffset, numZexclHigh
    endif
    numZpatchUse = numZpatchTot - numZoffset - numZexclHigh
    if (numZpatchUse + numZoffset > numZpatchTot .or. numZpatchUse < 2) then
      if (ifAuto .ne. 0) call exitError( 'ILLEGAL ENTRY FOR NUMBER OF '// &
          rowSlabCapText(2 - ifFlip) //' TO EXCLUDE IN Z')
      print *,'Illegal entry'
      numZoffset = 0
      numZpatchUse = numZpatchTot
      go to 14
    endif
    print *,'Remaining # of patches in X, Y and Z is:', &
        numXpatchUse, numYpatchUse, numZpatchUse
  else
    numXpatchUse = numXpatchTot
    numYpatchUse = numYpatchTot
    numZpatchUse = numZpatchTot
    numXoffset = 0
    numYoffset = 0
    numZoffset = 0
  endif
  !
  ! Initialize nfit for slabs before checking on slabs
  if (pipInput .and. ifAuto .ne. 0) then
    numXYXfit(indZ) = numXYZpatchUse(indZ)
    numXYZfitIn(indZ) = numXYXfit(indZ)
  endif
  !
  ! Figure out if doing subsets of slabs but not for interactive when
  ! auto was already selected
  if (pipInput .or. ifAuto == 0) then
    if (numXYZpatchUse(indZ) > 2) then
      if (pipInput) then
        ifLocalSlabs = 1 - PipGetInteger('LocalSlabs', numXYXfit(indZ))
        if (ifLocalSlabs > 0) then
          if (numXYXfit(indZ) < 2 .or. numXYXfit(indZ) > numXYZpatch(indZ)) &
              call exitError('NUMBER OF LOCAL SLABS OUT OF ALLOWED RANGE')
          numXYZfitIn(indZ) = numXYXfit(indZ)
        endif
      else if (ifAuto == 0) then
        write(*,'(1x,a,a,a,a,a,$)') '0 to fit to all patches in ', yzText(2 - ifFlip), &
            ', or 1 to fit to subsets in ', yzText(2 - ifFlip), ': '
        read(5,*) ifLocalSlabs
      endif
    endif
  endif

  if (ifAuto .ne. 0) then
    !
    ! set up parameters for automatic finding: make list of possible
    ! nxfit, nyfit, nzfit values
    !
    indAuto = -1
    numXYZfit(indZ) = min(numXYZfit(indZ), numXYZpatchUse(indZ))
    call setAutoFits(numXpatchUse, numXYZpatchUse(indY), numXYZpatchUse(indZ), &
        ifLocalSlabs, numXYZfit(indZ), ratioMin, ratioMax, ix, iy, itmp, numAuto, &
        indAuto)
    if (numAuto == 0) then
      write(*,'(/,a,/,a)') 'ERROR: FINDWARP - NO FITTING PARAMETERS GIVE '// &
          'THE REQUIRED RATIO OF', ' ERROR: MEASUREMENTS TO UNKNOWNS - '// &
          'THERE ARE PROBABLY TOO FEW PATCHES'
      call exit(1)
    endif
    allocate(numXautoFit(indAuto), numYautoFit(indAuto), numZautoFit(indAuto), &
        devMeanAuto(indAuto), stat = ierr)
    call memoryError(ierr, 'ARRAYS FOR AUTOFITS')

    if (ifFlip > 0) then
      call setAutoFits(numXpatchUse, numZpatchUse, numYpatchUse, ifLocalSlabs, &
          numXYZfit(indZ), ratioMin, ratioMax, numXautoFit, numZautoFit, &
          numYautoFit, numAuto, indAuto)
    else
      call setAutoFits(numXpatchUse, numYpatchUse, numZpatchUse, ifLocalSlabs, &
          numXYZfit(indZ), ratioMin, ratioMax, numXautoFit, numYautoFit, &
          numZautoFit, numAuto, indAuto)
    endif
    !
    ! sort the list by size of area in inverted order
    !
    do i = 1, numAuto - 1
      do j = i + 1, numAuto
        if (numXautoFit(i) * numYautoFit(i) * numZautoFit(i) < &
            numXautoFit(j) * numYautoFit(j) * numZautoFit(j)) then
          itmp = numXautoFit(i)
          numXautoFit(i) = numXautoFit(j)
          numXautoFit(j) = itmp
          itmp = numYautoFit(i)
          numYautoFit(i) = numYautoFit(j)
          numYautoFit(j) = itmp
          itmp = numZautoFit(i)
          numZautoFit(i) = numZautoFit(j)
          numZautoFit(j) = itmp
        endif
      enddo
    enddo
    ! write(*,'(3i5)') (i, nfxauto(i), nfyauto(i), nfzauto(i), i=1, numAuto)
    !
    ! set up for first round and skip to set up this round's patches
    !
    indAuto = 1
    numXfit = -100
    numYfit = -100
    numZfit = -100
    numLocalDone = 0
    devMeanMin = 10000.
    indTarget = 1
    write(*,112) targetResid(1)
112 format(/,'Seeking a warping with mean residual below',f9.3)
    go to 20
  endif
  !
  ! Get parameter to control outlier elimination
  !
  if (.not.pipInput) then
    write(*,'(1x,a,$)') '1 to enter parameters to control outlier elimination, 0 not to: '
    read(5,*) ifDiddle
    if (ifDiddle .ne. 0) then
      write(*,'(1x,a,f5.2,a,$)') 'Maximum fraction of patches to eliminate (/ for', &
          fracDrop, '): '
      read(5,*) fracDrop
      write(*,'(1x,a,f5.2,a,$)') 'Minimum residual needed to do any elimination (/ for', &
          elimMinResid, '): '
      read(5,*) elimMinResid
      write(*,'(1x,a,f6.3,a,$)') 'Criterion probability for ' // &
          'candidates for elimination (/ for', probCrit, '): '
      read(5,*) probCrit
      write(*,'(1x,a,f6.3,a,$)') 'Criterion probability for enforced' &
          //' elimination (/ for', absProbCrit, '): '
      read(5,*) absProbCrit
    endif
  endif
  !
  ! Initialize for first time in or back after parameter setting
  !
  numXfit = -100
  numYfit = -100
  numZfit = -100
  numLocalDone = 0
  if (.not.pipInput) write(*,111)
111 format(/,' Enter 0 for the number of patches in X to loop', &
      ' back and find best fit ',/, &
      '  automatically, include a different subset of patches ', &
      /,'  or specify new outlier elimination parameters',/)
  !
  ! Get the input number of local patches unless doing auto
  !
20 if (ifAuto == 0 .and. ifLocalSlabs .ne. 0) then
    if (.not.pipInput) then
      if (numLocalDone == 0) then
        write(*,'(1x,a,$)') 'Number of local patches for fit in X, Y and Z: '
      else
        write(*,'(1x,a,/,a,$)') 'Number of local patches for fit in X, Y and Z,', &
            '    or / to redo and save last result: '
      endif
      read(5,*) numXfitIn, numYfitIn, numZfitIn
    endif
  elseif (ifAuto == 0) then
    if (.not.pipInput) then
      if (numLocalDone == 0) then
        write(*,'(1x,a,a,a,$)') &
            'Number of local patches for fit in X and ', yzText(1 + ifFlip), ': '
      else
        write(*,'(1x,a,a,a,/,a,$)') &
            'Number of local patches for fit in X and ', yzText(1 + ifFlip), ',', &
            '    or / to redo and save last result: '
      endif
      read(5,*) numXfitIn, numXYZfitIn(indY)
    endif
    numXYZfitIn(indZ) = numXYZpatchUse(indZ)
  else
    numXfitIn = numXautoFit(indAuto)
    numYfitIn = numYautoFit(indAuto)
    numZfitIn = numZautoFit(indAuto)
  endif
  !
  ! Loop back to earlier parameter entries on a zero entry
  if (numXfitIn == 0) go to 8
  !
  ! Save data and terminate on a duplicate entry
  if (numXfitIn == numXfit .and. numYfitIn == numYfit .and. numZfitIn == numZfit .and. &
      numLocalDone > 0) &
      call saveAndTerminate()
  !
  ! Otherwise set up number of locations to fit and check entries
  numXlocal = numXpatchUse + 1 - numXfitIn
  numYlocal = numYpatchUse + 1 - numYfitIn
  numZlocal = numZpatchUse + 1 - numZfitIn
  if (numXfitIn < 2 .or. numXYZfitIn(indY) < 2 .or. numXlocal < 1 .or. numZlocal < 1 &
      .or. numXYZfitIn(indZ) < 1 .or. numYlocal < 1) then
    if (ifAuto .ne. 0) call exitError('IMPROPER NUMBER TO INCLUDE IN FIT')
    print *,'Illegal entry, try again'
    go to 20
  endif
  !
  ! If arrays not allocated yet, do them to big size for interactive or current size
  ! for auto (which will be biggest) or one-shot run from PIP
  if (.not. allocated(fitMat)) then
    if (pipInput) limFit = numXfitIn * numYfitIn * numZfitIn + 10
    allocate(fitMat(matColDim, limFit), idrop(limFit), stat = ierr)
    call memoryError(ierr, 'ARRAYS FOR FITTING')
  endif
  if (numXfitIn * numYfitIn * numZfitIn > limFit) then
    if (ifAuto .ne. 0) call exitError('TOO MANY PATCHES FOR ARRAY SIZES')
    print *,'Too many patches for array sizes, try again'
    go to 20
  endif
  numXfit = numXfitIn
  numYfit = numYfitIn
  numZfit = numZfitIn
  !
  ! Do the fits finally
  !
  call fitLocalPatches()
  !
  ! check for auto control
  !
  if (ifAuto .ne. 0) then
    devMeanAvg = 10000.
    if (numDevSum > 0) devMeanAvg = devMeanSum / numDevSum
    devMeanMin = min(devMeanMin, devMeanAvg)
    devMeanAuto(indAuto) = devMeanAvg
    if (devMeanAvg <= targetResid(indTarget)) then
      !
      ! done: set nauto to zero to allow printing of results
      !
      numAuto = 0
      write(*,107) numXfit, numYfit, numZfit
107   format(/,'Desired residual achieved with fits to',i3,',',i3, &
          ', and',i3,' patches in X, Y, Z',/)
    else
      indAuto = indAuto + 1
      ! print *,iauto, ' Did fit to', nfitx, nfity, nfitz
      if (indAuto > numAuto) then
        !
        ! See if any other criteria have been met and loop back if so
        !
        do j = indTarget + 1, numTarget
          write(*,112) targetResid(j)
          do i = 1, numAuto
            if (devMeanAuto(i) <= targetResid(j)) then
              indTarget = j
              indAuto = i
              numXfit = -100
              numLocalDone = 0
              go to 20
            endif
          enddo
        enddo
        !
        ! write patch file if desired on last fit before error message
        !
        call outputPatchRes(residFile, numPosInFile, &
            numXpatchTot * numYpatchTot * numZpatchTot, exists, residSum, numResid, &
            indDropped, numTimesDropped, nlistDropped, cenXYZ, vecXYZ, LIMPATCH)
        if (discount > 0. .and. numLocalDone > 0 .and. numDevSum == 0) &
            call exitError('ALL FITS HAD TOO MANY ZERO VECTORS: RAISE '// &
            '-discount FRACTION OR SET IT TO ZERO')
        write(*,108) devMeanMin
108     format(/,'ERROR: FINDWARP - FAILED TO FIND A WARPING WITH A ', &
            'MEAN RESIDUAL BELOW',f9.3)
        call exit(2)
      endif
    endif
  endif

  if (nlistDropped > 0 .and. numAuto == 0) then
    write(*,105) nlistDropped, numDropTot
105 format(i5,' separate patches eliminated as outliers a total' &
        ,' of',i6,' times:')
    if (nlistDropped <= 10) then
      print *,'    patch position   # of times   mean residual'
      do i = 1, nlistDropped
        write(*,106) (cenXYZ(indDropped(i), j), j = 1, 3), numTimesDropped(i), &
            dropSum(i) / numTimesDropped(i)
106     format(3f7.0,i8,f12.2)
      enddo
    else
      do i = 1, nlistDropped
        dropSum(i) = dropSum(i) / numTimesDropped(i)
      enddo
      call summarizeDrops(dropSum, nlistDropped, 'mean ')
    endif
    write(*,*)
  endif
  !
  if (numLocalDone > 0 .and. numAuto == 0) then
    devMeanAvg = devMeanSum / max(1, numDevSum)
    devMaxAvg = devMaxSum / max(1, numDevSum)
    write(*,101) devMeanAvg, devMeanMax, devMaxAvg, devMaxMax
101 format('Mean residual has an average of',f8.3, &
        ' and a maximum of',f8.3,/ , &
        'Max  residual has an average of',f8.3,' and a maximum of' &
        ,f8.3)
    if (discount > 0.) write(*,'(/,a,i7,a,i7,a)') 'These averages are '// &
        'based on', numDevSum, ' of', numLocalDone, ' fits'
    !
    ! finish up after auto fits: this call is unneeded and is just here to make it clear
    if (ifAuto .ne. 0) call saveAndTerminate()
  elseif (ifAuto == 0) then
    print *,'No locations could be solved for'
  endif
  go to 20


CONTAINS

  ! fitLocalPatches does the fits to all the local patches 
  !
  subroutine fitLocalPatches()
    logical inside
    real*4 determ
    devMeanSum = 0.
    devMaxSum = 0.
    devMaxMax = 0.
    devMeanMax = 0.
    nlistDropped = 0
    numDropTot = 0
    numLocalDone = 0
    numDevSum = 0.
    determMean = 0.
    do i = 1, 3
      cenSaveMin(i) = 1.e30
      cenSaveMax(i) = -1.e30
    enddo
    do ind = 1, numXpatchTot * numYpatchTot * numZpatchTot
      numResid(ind) = 0
      residSum(ind) = 0
    enddo

    do locZ = 1, numZlocal
      do locY = 1, numYlocal
        do locX = 1, numXlocal
          numData = 0
          numZero = 0
          do i = 1, 3
            cenXYZsum(i) = 0.
          enddo
          !
          ! count up number in each row in each dimension and on each
          ! diagonal in the major dimensions
          !
          do i = 1, max(numXfit, numYfit, numZfit)
            inRowX(i) = 0
            inRowY(i) = 0
            inRowZ(i) = 0
          enddo
          nyDiag = numYfit
          if (ifFlip == 1) nyDiag = numZfit
          !
          ! Zero the parts of the arrays corresponding to corners, which
          ! won't be tested
          do i = 0, numXfit + nyDiag - 2
            inDiag1(i) = 0
            inDiag2(i) = 0
          enddo
          do lz = locZ + numZoffset, locZ + numZoffset + numZfit - 1
            do ly = locY + numYoffset, locY + numYoffset + numYfit - 1
              do lx = locX + numXoffset, locX + numXoffset + numXfit - 1
                ifUse = 0
                ind = indPatch(lx, ly, lz)
                if (exists(ind)) ifUse = 1
                do i = 1, 3
                  cenXYZsum(i) = cenXYZsum(i) + cenXYZ(ind, i) - 0.5 * nxyzVol(i)
                enddo
                if (numConts > 0 .and. ifUse > 0) then
                  ifUse = 0
                  !
                  ! find nearest contour in Z and see if patch is inside it
                  !
                  dzMin = 100000.
                  do icont = 1, numConts
                    dz = abs(cenXYZ(ind, indZ) - contourZ(icont))
                    if (dz < dzMin) then
                      dzMin = dz
                      icontMin = icont
                    endif
                  enddo
                  indv = indVertStart(icontMin)
                  if (inside(xVerts(indv), yVerts(indv), numVerts(icontMin), &
                      cenXYZ(ind, 1), cenXYZ(ind, indY))) ifUse = 1
                endif
                !
                if (ifUse > 0) then
                  numData = numData + 1
                  if (vecXYZ(ind, 1) == 0. .and. vecXYZ(ind, 2) == 0 .and. &
                      vecXYZ(ind, 3) == 0) numZero = numZero + 1
                  do j = 1, 3
                    !
                    ! the regression requires coordinates of second volume as
                    ! independent variables (columns 1-3), those in first
                    ! volume as dependent variables (stored in 5-7), to
                    ! obtain transformation to get from second to first
                    ! volume cx+dx in second volume matches cx in first
                    ! volume
                    !
                    fitMat(j + 4, numData) = cenXYZ(ind, j) - 0.5 * nxyzVol(j)
                    fitMat(j, numData) = fitMat(j + 4, numData) + vecXYZ(ind, j)
                  enddo
                  !
                  ! Solve_wo_outliers uses columns 8-17; save indexi in 18
                  ! Add to row counts and to diagonal counts
                  !
                  fitMat(18, numData) = ind
                  ix = lx + 1 - locX - numXoffset
                  iy = ly + 1 - locY - numYoffset
                  iz = lz + 1 - locZ - numZoffset
                  inRowX(ix) = inRowX(ix) + 1
                  inRowY(iy) = inRowY(iy) + 1
                  inRowZ(iz) = inRowZ(iz) + 1
                  if (ifFlip == 1) iy = iz
                  iz = (ix - iy) + nyDiag - 1
                  inDiag1(iz) = inDiag1(iz) + 1
                  iz = ix + iy - 2
                  inDiag2(iz) = inDiag2(iz) + 1
                endif
              enddo
            enddo
          enddo
          indLcl = indLocal(locX, locY, locZ)
          !
          ! Need regular array of positions, so use the xyzsum to get
          ! censave, not the cenloc values from the regression
          !
          debugHere = ifDebug .ne. 0
          do i = 1, 3
            cenToSave(i, indLcl) = cenXYZsum(i) / (numXfit * numYfit * numZfit)
            cenSaveMin(i) = min(cenSaveMin(i), cenToSave(i, indLcl))
            cenSaveMax(i) = max(cenSaveMax(i), cenToSave(i, indLcl))
            if (debugHere) &
                debugHere = abs(cenToSave(i, indLcl) - debugXYZ(i)) < 1.
          enddo
          !
          ! solve for this location if there are at least half of the
          ! normal number of patches present and if there are guaranteed
          ! to be at least 3 patches in a different row from the dominant
          ! one, even if the max are dropped from other rows
          ! But treat thickness differently: if there are not enough data
          ! on another layer, or if there is only one layer being fit,
          ! then set the appropriate column as fixed in the fits
          !
          solved(indLcl) = numData >= numXfit * numYfit * numZfit / 2
          maxDrop = nint(fracDrop * numData)
          icolFixed = 0
          do i = 1, max(numXfit, numYfit, numZfit)
            if (debugHere) print *,'in row', i, ':', inRowX(i), inRowY(i), inRowZ(i)
            if (ifFlip == 1) then
              if (inRowX(i) > numData - 3 - maxDrop .or. &
                  inRowZ(i) > numData - 3 - maxDrop) solved(indLcl) = .false.
              if (inRowY(i) > numData - 3 - maxDrop .or. numYfit == 1) icolFixed = 2
            else
              if (inRowX(i) > numData - 3 - maxDrop .or. &
                  inRowY(i) > numData - 3 - maxDrop) solved(indLcl) = .false.
              if (inRowZ(i) > numData - 3 - maxDrop .or. numZfit == 1) icolFixed = 3
            endif
          enddo
          do i = 1, numXfit + nyDiag - 3
            if (inDiag1(i) > numData - 3 - maxDrop .or. &
                inDiag2(i) > numData - 3 - maxDrop) solved(indLcl) = .false.
          enddo
          if (solved(indLcl)) then
            call solve_wo_outliers(fitMat, matColDim, numData, 3, icolFixed, maxDrop, &
                probCrit, absProbCrit, elimMinResid, idrop, numDrop, a, delXYZ, &
                cenLocal, devMean, devSD, devMax, ipntMax, devXYZmax)
            !
            if (debugHere) then
              do i = 1, numData
                write(*,'(8f9.2)') (fitMat(j, i), j = 1, 7)
              enddo
              print *,'cenloc', (cenLocal(i), i = 1, 3)
              print *,'censave', (cenToSave(i, indLcl), i = 1, 3)
              write(*,'(9f8.3)') ((a(i, j), i = 1, 3), j = 1, 3)
            endif
            !
            ! Accumulate information about dropped points
            !
            do i = 1, numDrop
              ifInDrop = 0
              do j = 1, nlistDropped
                if (nint(fitMat(18, idrop(i))) == indDropped(j)) then
                  ifInDrop = 1
                  numTimesDropped(j) = numTimesDropped(j) + 1
                  dropSum(j) = dropSum(j) + fitMat(4, numData + i - numDrop)
                endif
              enddo
              if (ifInDrop == 0 .and. ifInDrop < limFit) then
                nlistDropped = nlistDropped + 1
                indDropped(nlistDropped) = nint(fitMat(18, idrop(i)))
                numTimesDropped(nlistDropped) = 1
                dropSum(nlistDropped) = fitMat(4, numData + i - numDrop)
              endif
            enddo
            numDropTot = numDropTot + numDrop
            !
            ! if residual output asked for, accumulate info about all resids
            !
            if (residFile .ne. ' ') then
              do i = 1, numData
                ind = nint(fitMat(18, nint(fitMat(5, i))))
                numResid(ind) = numResid(ind) + 1
                residSum(ind) = residSum(ind) + fitMat(4, i)
              enddo
            endif
            !
            if (discount == 0. .or. float(numZero) / numData <= discount) &
                then
              devMeanSum = devMeanSum + devMean
              devMaxSum = devMaxSum + devMax
              numDevSum = numDevSum + 1
            endif
            devMeanMax = max(devMeanMax, devMean)
            devMaxMax = max(devMaxMax, devMax)
            !
            ! mark this location as solved and save the solution.
            !
            if (debugHere) write(*,'(6i4,3f8.1)') indLcl, locX, locY, locZ, numData, &
                numDrop, (delXYZ(i), i = 1, 3)
            do i = 1, 3
              delXYZsave(i, indLcl) = delXYZ(i)
              do j = 1, 3
                amatSave(i, j, indLcl) = a(i, j)
              enddo
              if (debugHere) write(*,122) (a(i, j), j = 1, 3), delXYZ(i)
122             format(3f10.6,f10.3)
            enddo
            numLocalDone = numLocalDone + 1
            determMean = determMean + abs(determ(a))
          elseif (debugHere) then
            print *,'Not solved', numData
          endif
        enddo
      enddo
    enddo
    determMean = determMean / max(1, numLocalDone)
    return
  end subroutine fitLocalPatches


  ! saveAndTerminate saves the results (getting initial file and output file) and exits
  !
  subroutine saveAndTerminate()
    real*4 determ
    if (numXlocal > 1 .or. numYlocal > 1 .or. numZlocal > 1) then
      !
      ! Eliminate locations with low determinants.  This is very
      ! conservative measure before observed failures were diagonal
      ! degeneracies in 2x2 fits
      numLowDeterm = 0
      do locZ = 1, numZlocal
        do locY = 1, numYlocal
          do locX = 1, numXlocal
            ind = indLocal(locX, locY, locZ)
            if (solved(ind)) then
              if (abs(determ(amatSave(1, 1, ind))) < 0.01 * determMean) then
                solved(ind) = .false.
                numLowDeterm = numLowDeterm + 1
              endif
            endif
          enddo
        enddo
      enddo
      !
      if (numLowDeterm > 0) write(*,'(/,i4,a)') numLowDeterm, &
          ' fits were eliminated due to low matrix determinant'
      !
      if (ifAuto .ne. 0) write(*,*)
      if (pipInput) then
        ierr = PipGetString('InitialTransformFile', filename)
      else
        print *,'Enter name of file with initial transformation, typically solve.xf', &
            '   (Return if none)'
        read(*,'(a)') filename
      endif
      if (filename .ne. ' ') then
        call dopen(1, filename, 'old', 'f')
        read(1,*) ((firstAmat(i, j), j = 1, 3), firstDelta(i), i = 1, 3)
        close(1)
      else
        do i = 1, 3
          do j = 1, 3
            firstAmat(i, j) = 0.
          enddo
          firstAmat(i, i) = 1.
          firstDelta(i) = 0.
        enddo
      endif
      !
      if (PipGetInOutFile('OutputFile', 2, &
          'Name of file to place warping transformations in', filename) == 0) then
        call dopen(1, filename, 'new', 'f')
        !
        ! Output new style header to allow missing data
        dxLocal = 1.
        dyLocal = 1.
        dzLocal = 1.
        if (numXlocal > 1) dxLocal = (cenSaveMax(1) - cenSaveMin(1)) / (numXlocal - 1)
        if (numYlocal > 1) dyLocal = (cenSaveMax(2) - cenSaveMin(2)) / (numYlocal - 1)
        if (numZlocal > 1) dzLocal = (cenSaveMax(3) - cenSaveMin(3)) / (numZlocal - 1)
        write(1, 104) numXlocal, numYlocal, numZlocal, (cenSaveMin(i), i = 1, 3), &
            dxLocal, dyLocal, dzLocal
104     format(i5,2i6,3f11.2,3f10.4)
        !
        do locZ = 1, numZlocal
          do locY = 1, numYlocal
            do locX = 1, numXlocal
              ind = indLocal(locX, locY, locZ)
              indUse = ind
              !
              ! if this location was solved, combine and invert
              if (solved(ind)) then
                call xfmult3d(firstAmat, firstDelta, amatSave(1, 1, indUse), &
                    delXYZsave(1, indUse), amatTmp, delTmp)
                call xfinv3d(amatTmp, delTmp, a, delXYZ)
                write(1, 103) (cenToSave(i, ind), i = 1, 3)
103             format(3f9.1)
                write(1, 102) ((a(i, j), j = 1, 3), delXYZ(i), i = 1, 3)
102             format(3f10.6,f10.3)
              endif
            enddo
          enddo
        enddo
        close(1)
      endif
    else
      !
      ! Save a single transform if fit to whole area; won't happen if auto
      !
      print *,'Enter name of file in which to place single refining transformation'
      read(5, '(a)') filename
      call dopen(1, filename, 'new', 'f')
      write(1, 102) ((a(i, j), j = 1, 3), delXYZ(i), i = 1, 3)
      close(1)
    endif

    call outputPatchRes(residFile, numPosInFile,  &
        numXpatchTot * numYpatchTot * numZpatchTot, exists, residSum, numResid, &
        indDropped, numTimesDropped, nlistDropped, cenXYZ, vecXYZ, LIMPATCH)

    call exit(0)
  end subroutine saveAndTerminate

end program findwarp


! Sets up a list of number of local patches for autofits, where each one has a ratio of 
! measured to unknown within the min and max range.  Call with limAuto <= 0 to just
! count up the number and return a good value for limAuto
!
subroutine setAutoFits(numXpatchUse, numZpatchUse, numYpatchUse, ifLocalInY, numYfit, &
    ratioMin, ratioMax, numXautoFit, numZautoFit, numYautoFit, numAuto, limAuto)
  implicit none
  integer*4 numXpatchUse, numZpatchUse, numYpatchUse, ifLocalInY, numYfit, numXautoFit(*)
  integer*4 numZautoFit(*), numYautoFit(*), numAuto, numInY, ix, iz, iy, limAuto
  real*4 ratioMin, ratioMax, ratio, ratioFac
  numAuto = 0
  numInY = numYpatchUse
  if (ifLocalInY .ne. 0) numInY = numYfit
  ratioFac = 4.0
  if (numYpatchUse == 1) ratioFac = 3.0
  do ix = numXpatchUse, 2, -1
    do iz = numZpatchUse , 2, -1
      do iy = numYpatchUse, numInY, -1
        ratio = ix * iz * iy / ratioFac
        ! aspect = float(ix) /iz
        ! if (aspect<1.) aspect=1./aspect
        if ((ix .ne. numXpatchUse .or. iz .ne. numZpatchUse) .and.  &
            ratio >= ratioMin .and. ratio <= ratioMax) then
          ! &                 aspect<=aspectmax) then
          numAuto = numAuto + 1
          if (limAuto > 0) then
            numXautoFit(numAuto) = ix
            numZautoFit(numAuto) = iz
            numYautoFit(numAuto) = iy
          endif
        endif
      enddo
    enddo
  enddo
  if (limAuto <= 0) limAuto = numAuto + 10
  return
end subroutine setAutoFits

!
! Output new patch file with mean residuals if requested
!
subroutine outputPatchRes(residFile, numPosInFile, numPatchTot, exists, residSum, &
    numResid, indDropped, numTimesDropped, nlistDropped, cenXYZ, vecXYZ, LIMPATCH)
  implicit none
  character*(*) residFile
  integer*4 numPosInFile, numPatchTot, LIMPATCH, numResid(*), ind, i, indDropped(*)
  integer*4 numTimesDropped(*), nlistDropped
  real*4 cenXYZ(LIMPATCH,3), vecXYZ(LIMPATCH,3), residSum(*), dist, dropFrac
  logical exists(*)
  if (residFile == ' ') return
  call dopen(1, residFile, 'new', 'f')
  write(1, '(i7,a)') numPosInFile, ' positions'
  do ind = 1, numPatchTot
    if (exists(ind)) then
      dropFrac = 0.
      do i = 1, nlistDropped
        if (ind == indDropped(i)) then
          dropFrac = float(numTimesDropped(i)) / max(numTimesDropped(i), 1, numResid(ind))
          exit
        endif
      enddo
      dist = residSum(ind) / max(1, numResid(ind))
      write(1, 110) (nint(cenXYZ(ind, i)), i = 1, 3), (vecXYZ(ind, i), i = 1, 3) &
          , dist, dropFrac
110   format(3i6,3f9.2,f10.2,f7.3)
    endif
  enddo
  close(1)
  return
end subroutine outputPatchRes

real*4 function determ(a)
  implicit none
  real * 4 a(3, 3)
  determ = a(1, 1) * a(2, 2) * a(3, 3) + a(2, 1) * a(3, 2) * a(1, 3) +  &
      a(1, 2) * a(2, 3) * a(3, 1) - a(1, 3) * a(2, 2) * a(3, 1) -  &
      a(2, 1) * a(1, 2) * a(3, 3) - a(1, 1) * a(3, 2) * a(2, 3)
  return
end function determ
