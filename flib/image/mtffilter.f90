!*************mtffilter.f90********************************************
!
! Mtffilter can restore contrast in CCD camera images by multiplying
! them by the inverse of the camera's modulation transfer function (
! MTF) .  It can also apply a low pass filter to reduce high frequency
! noise.  One, the other, or both of these filters may be applied.
!
! For more details see the man page
!
! David Mastronarde, 3/30/04
!
! $Id$
!
  implicit none
  integer LIMMTF, LIMSTOCKCURVES, LIMSTOCKPOINTS
  parameter (LIMMTF = 8193)
  parameter (LIMSTOCKCURVES = 1, LIMSTOCKPOINTS = 36)
  !
  integer*4 nx, ny, nz, nxyz(3), mxyz(3), title(20), nxyzst(3)
  equivalence (nx, nxyz(1)), (ny, nxyz(2)), (nz, nxyz(3))
  real*4 ctfa(8193), xmtf(LIMMTF), ymtf(LIMMTF), cell(6), pixel(3), ctfb(8193)
  real*4, allocatable :: array(:)
  integer*4 numPtStock(LIMSTOCKCURVES)
  real*4 stock(2,LIMSTOCKPOINTS)
  data numPtStock/36/
  data stock/ 0.0085,  0.98585, &
      0.0221, 0.94238, &
      0.0357, 0.89398, &
      0.0493, 0.83569, &
      0.0629, 0.76320, &
      0.0765, 0.69735, &
      0.0901, 0.63647, &
      0.1037, 0.56575, &
      0.1173, 0.49876, &
      0.1310, 0.43843, &
      0.1446, 0.38424, &
      0.1582, 0.34210, &
      0.1718, 0.30289, &
      0.1854, 0.26933, &
      0.1990, 0.23836, &
      0.2126, 0.21318, &
      0.2262, 0.18644, &
      0.2398, 0.15756, &
      0.2534, 0.14863, &
      0.2670, 0.12485, &
      0.2806, 0.11436, &
      0.2942, 0.09183, &
      0.3078, 0.08277, &
      0.3214, 0.07021, &
      0.3350, 0.05714, &
      0.3486, 0.04388, &
      0.3622, 0.03955, &
      0.3759, 0.03367, &
      0.3895, 0.02844, &
      0.4031, 0.02107, &
      0.4167, 0.02031, &
      0.4303, 0.01796, &
      0.4439, 0.00999, &
      0.4575, 0.01103, &
      0.4711, 0.00910, &
      0.4898, 0.00741/

  !
  data nxyzst/0, 0, 0/
  !
  character*120 inFile, outFile
  character*9 dateStr
  character*8 timeStr
  character*70 titleStr
  character*80 titlech
  integer*4 modeMap(0:16) /1, 2, 0, 0, 0, 0, 3, 0,0,0,0,0,0,0,0,0,0/
  integer*4 modeMin(3) /0, -32768, 0/
  integer*4 modeMax(3) /256, 32768, 65536/
  integer*4 mode, numPad, nxPad, nyPad, imUnitOut, izStart, izEnd, numZdo, kti, nsize2
  integer*4 izOutBase, nsize, indStock, numMtf, i, im, kk, ixStart, iyStart, modPrint
  real*4 dminIn, dmaxIn, dmean, dmin, dmax, arraySize, xfac, scaleFac, delta
  real*4 ctfInvMax, radius1, sigma1, radius2, sigma2, s, dmeanSum, dmeanIn
  real*4 dmin2, dmax2, dmean2, atten, beta1, deltaRad, delx, dely, delz, xa, ya, za
  real*4 zaSq, yaSq, sigma1b, radius1b, base, pixSizeDelta(3)
  real*4 ampfac, ampPower, pixelSize, holeSize, voltage, focalLength, ampRadius
  real*4 wavelength
  integer*4 ind, j, ierr, indf, ix, iy, iz, izLow, izHigh, izRead, ibase, ixBase
  integer*4 nxDim, nzPad, nyzMax, ifCut, ifPhase, modeOut, modeTry
  integer (kind = 8) indWork, idim
  logical fftInput, filter3d
  integer*4 niceFrame, iiuReadSection
  !
  logical pipInput
  integer*4 numOptArg, numNonOptArg
  integer*4 PipGetInteger, PipGetBoolean, PipGetTwoIntegers, PipGetThreeFloats
  integer*4 PipGetString, PipGetFloat, PipGetInOutFile, PipGetTwoFloats
  integer*4 PipGetNonOptionArg, PipGetLogical
  !
  ! fallbacks from ../../manpages/autodoc2man -3 2  mtffilter
  !
  integer numOptions
  parameter (numOptions = 20)
  character*(40 * numOptions) options(1)
  options(1) = &
      'input:InputFile:FN:@output:OutputFile:FN:@zrange:StartingAndEndingZ:IP:@'// &
      'mode:ModeToOutput:I:@3dfilter:FilterIn3D:B:@lowpass:LowPassRadiusSigma:FP:@'// &
      'highpass:HighPassSigma:F:@radius1:FilterRadius1:F:@mtf:MtfFile:FN:@'// &
      'stock:StockCurve:I:@maxinv:MaximumInverse:F:@'// &
      'invrolloff:InverseRolloffRadiusSigma:FP:@xscale:XScaleFactor:F:@'// &
      'denscale:DensityScaleFactor:F:@amplifier:AmplifierFactorAndPower:FP:@'// &
      'cutoff:CutoffForAmplifier:F:@phase:PhasePlateParameters:FT:@'// &
      'pixel:PixelSize:F:@param:ParameterFile:PF:@help:usage:B:'

  outFile = ' '
  inFile = ' '
  radius1 = 0.12
  sigma1 = 0.05
  radius2 = 1.
  sigma2 = 0.
  sigma1b = 0.
  radius1b = 0.
  scaleFac = 1.
  xfac = 1.
  ctfInvMax = 4.
  filter3d = .false.
  !
  ! Pip startup: set error, parse options, do help output
  !
  call PipReadOrParseOptions(options, numOptions, 'mtffilter', &
      'ERROR: MTFFILTER - ', .false., 2, 1, 1, numOptArg, &
      numNonOptArg)
  !
  ! Open image files.
  !
  if (PipGetInOutFile('InputFile', 1, ' ', inFile) &
      .ne. 0) call exitError('NO INPUT FILE SPECIFIED')

  ierr = PipGetInOutFile('OutputFile', 2, ' ', outFile)
  !
  if (outFile == ' ') then
    call imopen(1, inFile, 'OLD')
  else
    call imopen(1, inFile, 'RO')
  endif
  call irdhdr(1, nxyz, mxyz, mode, dminIn, dmaxIn, dmeanIn)
  fftInput = mode == 3 .or. mode == 4
  if (.not. fftInput) ierr = PipGetLogical('FilterIn3D', filter3d)
  !
  ! get padded size and make sure it will fit
  !
  if (fftInput) then
    nxPad = (nx - 1) * 2
    nyPad = ny
    nzPad = nz
  else
    numPad = 40
    nxPad = niceFrame(nx + numPad, 2, 19)
    nyPad = niceFrame(ny + numPad, 2, 19)
    nzPad = niceFrame(nz + numPad, 2, 19)
  endif
  nxDim = nxPad + 2

  if (filter3d) then
    indWork = int(nxDim, kind = 8) * int(nyPad, kind = 8) * nzPad + 1
    idim = indWork + int(nxDim, kind = 8) * nzPad + 16
  else
    idim = nxDim * nyPad + 16
  endif
  if (idim > 2147483000.) &
      call exitError('PADDED VOLUME IS BIGGER THAN 2 GIGAPIXELS')
  allocate(array(idim), stat = ierr)
  if (ierr .ne. 0) call exitError('FAILED TO ALLOCATE MEMORY FOR IMAGE DATA')

  nyzMax = nyPad
  if (filter3d .or. fftInput) nyzMax = max(nyPad, nzPad)
  !
  imUnitOut = 1
  if (outFile .ne. ' ') then
    imUnitOut = 3
    call imopen(3, outFile, 'NEW')
    !
    call iiuTransHeader(3, 1)
  endif
  !
  izStart = 1
  izEnd = nz
  ierr = PipGetTwoIntegers('StartingAndEndingZ', izStart, izEnd)
  if (fftInput .and. ierr == 0) call exitError( &
      'FFT INPUT FILE IS ASSUMED TO BE 3D FFT, NO Z RANGE ALLOWED')
  if (filter3d .and. ierr == 0) call exitError('NO Z RANGE IS ALLOWED WITH 3D FILTERING')
  if (izStart > izEnd .or. izStart < 1 .or. izEnd > nz) call &
      exitError('ILLEGAL STARTING OR ENDING Z VALUES TO FILTER')
  numZdo = izEnd + 1 - izStart
  izOutBase = 1
  !
  ! take care of header if writing a new file
  !
  if (imUnitOut == 3) then
    izOutBase = izStart
    call iiuRetCell(1, cell)
    call iiuRetDelta(1, pixel)
    !
    ! change mxyz if it matches existing nz; set cell size to keep
    ! pixel spacing the same
    !
    if (mxyz(3) == nz) mxyz(3) = numZdo
    cell(3) = mxyz(3) * pixel(3)
    nz = numZdo
    call iiuAltSize(3, nxyz, nxyzst)
    call iiuAltSample(3, mxyz)
    call iiuAltCell(3, cell)
  endif
  !
  ! set up the ctf scaling as usual: but delta needs to be maximum
  ! frequency over ctf array size
  !
  nsize = min(8192, max(1024, 2 * max(nxPad, nyzMax)))
  arraySize = nsize
  nsize = nsize + 1
  delta = 0.71 / arraySize
  if (fftInput .or. filter3d) delta = 0.87 / arraySize
  !
  ! set default null mtf curve
  !
  indStock = 0
  outFile = ' '
  numMtf = 3
  do i = 1, 3
    xmtf(i) = (i - 1) * 0.25
    ymtf(i) = 1.
  enddo
  ierr = PipGetString('MtfFile', outFile)
  if (PipGetInteger('StockCurve', indStock) == 0) then
    if (indStock <= 0 .or. indStock > LIMSTOCKCURVES) call exitError( &
        'ILLEGAL NUMBER ENTERED FOR STOCK CURVE')
    if (outFile .ne. ' ') call exitError('YOU CANNOT ENTER BOTH'// &
        'AN MTF FILE AND A STOCK CURVE #')

    !
    ! if stock curve requested, find index and copy values
    !
    ind = 1
    do i = 1, indStock - 1
      ind = ind + numPtStock(i)
    enddo
    do i = 1, numPtStock(indStock)
      xmtf(i) = stock(1, ind)
      ymtf(i) = stock(2, ind)
      ind = ind + 1
    enddo
    numMtf = numPtStock(indStock)
  endif
  !
  if (outFile .ne. ' ') then
    !
    ! or read from file if one provided
    !
    call dopen(2, outFile, 'ro', 'f')
    numMtf = 0
10  i = numMtf + 1
    read(2,*,end = 20) xmtf(i), ymtf(i)
    numMtf = i
    go to 10
20  close(2)
  endif
  !
  ierr = PipGetFloat('XScaleFactor', xfac)
  do i = 1, numMtf
    xmtf(i) = xmtf(i) * xfac
    ! print *,xmtf(i), ymtf(i)
  enddo
  !
  ctfa(1) = 1.
  im = 1
  s = 0.
  do j = 2, nsize
    s = s + delta
    if (im < numMtf - 1 .and. s > xmtf(min(im + 1, numMtf))) im = im + 1
    ctfa(j) = max(0., ymtf(im) + (ymtf(im + 1) - ymtf(im)) * (s - xmtf(im)) &
        / (xmtf(im + 1) - xmtf(im)))
  enddo

  ! write (*,'(10f7.4)') (ctfa(j), j=1, nsize)

  ! Handle output mode entry
  modeOut = mode
  if (PipGetInteger('ModeToOutput', modeOut) == 0) then
    if (imUnitOut == 1) call exitError( &
        'YOU CANNOT ENTER -mode WHEN REWRITING TO THE INPUT FILE')
    if (fftInput) call exitError('YOU CANNOT ENTER -mode WITH AN FFT INPUT FILE')
    if (.not.((modeOut >= 0 .and. modeOut <= 2) .or. modeOut == 6)) call exitError( &
        'OUTPUT MODE MUST BE 0, 1, 2, or 6')
    call iiuAltMode(imUnitOut, modeOut)
  endif

  ierr = PipGetTwoFloats('InverseRolloffRadiusSigma', radius1, sigma1)
  ierr = PipGetFloat('MaximumInverse', ctfInvMax)
  ierr = PipGetFloat('DensityScaleFactor', scaleFac)
  ix = PipGetTwoFloats('LowPassRadiusSigma', radius2, sigma2)
  iy = PipGetFloat('FilterRadius1', radius1b)
  iz = PipGetFloat('HighPassSigma', sigma1b)
  ierr = PipGetTwoFloats('AmplifierFactorAndPower', ampfac, ampPower)
  ifCut = 1 - PipGetFloat('CutoffForAmplifier', ampRadius)
  ifPhase = 1 - PipGetThreeFloats('PhasePlateParameters', holeSize, voltage, &
      focalLength)
  if (ierr == 0 .or. ifCut .ne. 0 .or. ifPhase .ne. 0) then
    if (ix == 0 .or. iy == 0 .or. iz == 0) call exitError( &
        'YOU CANNOT ENTER PARAMETERS FOR BOTH A REGULAR AND AN AMPLIFIER FILTER')
    if (ierr .ne. 0 .or. (ifCut == 0 .and. ifPhase == 0)) call exitError( &
        'TO USE THE AMPLIFIER FILTER YOU MUST ENTER -amplifier AND '// &
        'EITHER -phase OR -cutoff')
    if (ifCut .ne. 0 .and. ifPhase .ne. 0) call exitError( &
        'YOU CANNOT ENTER BOTH -cutoff AND -phase FOR AMPLIFIER FILTER')
    if (ifPhase .ne. 0) then
      call iiuRetDelta(1, pixSizeDelta)
      pixelSize = pixSizeDelta(1) / 10.
      ierr = PipGetFloat('PixelSize', pixelSize)
      wavelength = 1.226 / sqrt(1000. * voltage * (1.0 + 0.9788e-3 * voltage))
      ampRadius = pixelSize * (holeSize / 2.0) / (wavelength * focalLength * 1e6)
    endif
    call amplifier(ampRadius, ampfac, ampPower, ctfb, nxPad, nyzMax, deltaRad, nsize2)
  else
    call setCtfNoScl(sigma1b, sigma2, radius1b, radius2, ctfb, nxPad, nyzMax, &
        deltaRad, nsize2)
  endif

  beta1 = 0.0
  if (sigma1 > 1.e-6) beta1 = -0.5 / sigma1**2

  modPrint = 0.025 / delta
  s = 0.
  print *
  print *,'The overall filter being applied is:'
  print *,'radius  multiplier'
  do j = 1, nsize
    atten = 1.
    if (s > radius1) atten = exp(beta1 * (s - radius1)**2)
    if (ctfa(j) < 0.01) then
      ctfa(j) = 1.
    else
      ctfa(j) = 1. +atten * (min(ctfInvMax, 1. / ctfa(j)) - 1.)
    endif
    if (deltaRad .ne. 0) then
      indf = s / deltaRad + 1.
      xa = s / deltaRad + 1. - indf
      ctfa(j) = ctfa(j) * ((1. -xa) * ctfb(indf) + xa * ctfb(indf + 1))
    endif
    if (mod(j, modPrint) == 1 .and. s <= 0.5) write(*,'(2f8.4)') s, ctfa(j)
    s = s + delta
    ctfa(j) = scaleFac * ctfa(j)
  enddo
  titleStr = 'MTFFILTER: Filtered by inverse of MTF'
  if (indStock == 0 .and. outFile == ' ') titleStr = 'MTFFILTER: Frequency filtered'
  !
  dmeanSum = 0.
  dmax = -1.e10
  dmin = 1.e10
  ixStart = (nxPad - nx) / 2
  iyStart = (nyPad - ny) / 2
  if (.not. filter3d) then
    delx = 0.5 / (nx - 1.)
    dely = 1. / ny
    delz = 1. / nz
    do kk = izStart, izEnd
      !
      ! print *,'reading section', kk
      call iiuSetPosition(1, kk - 1, 0)
      if (iiuReadSection(1, array) .ne. 0) call exitError('READING IMAGE FILE')
      if (fftInput) then
        !
        ! Filter the 3D fft - assuming the usual ordering
        !
        ind = 1
        za = delz * (kk - 1.) - 0.5
        zaSq = za**2
        do iy = 1, ny
          ya = dely * (iy - 1.) - 0.5
          yaSq = ya**2
          do ix = 1, nx
            xa = delx * (ix - 1.)
            s = sqrt(xa**2 + yaSq + zaSq)
            indf = s / delta + 1.5
            array(ind) = array(ind) * ctfa(indf)
            array(ind + 1) = array(ind + 1) * ctfa(indf)
            ind = ind + 2
          enddo
        enddo
        call iclcdn(array, nx, ny, 1, nx, 1, ny, dmin2, dmax2, dmean2)
      else
        !
        ! Do ordinary 2D filter with padding/tapering, FFT, filter, repack
        !
        call taperOutPad(array, nx, ny, array, nxPad + 2, nxPad, nyPad, 0, 0.)
        !
        ! print *,'taking fft'
        call todfft(array, nxPad, nyPad, 0)
        call filterPart(array, array, nxPad, nyPad, ctfa, delta)
        !
        ! print *,'taking back fft'
        call todfft(array, nxPad, nyPad, 1)

        ! print *,'repack, set density, write'
        call irepak(array, array, nxDim, nyPad, ixStart, ixStart + nx - 1, iyStart, &
            iyStart + ny - 1)
        call iclden(array, nx, ny, 1, nx, 1, ny, dmin2, dmax2, dmean2)
      endif
      !
      ! Write out lattice.
      !
      call iiuSetPosition(imUnitOut, kk - izOutBase, 0)
      call iiuWriteSection(imUnitOut, array)
      !
      dmax = max(dmax, dmax2)
      dmin = min(dmin, dmin2)
      dmeanSum = dmeanSum + dmean2
    enddo
  else
    !
    ! Taking 3d fft and filtering
    ! recast some variables to steal code from taperoutvol
    izStart = -((nzPad - nz) / 2)
    izEnd = izStart + nzPad - 1
    izLow = 0
    izHigh = nz - 1
    do iz = izStart, izEnd
      izRead = max(izLow, min(izHigh, iz))
      ibase = nxDim * nyPad * (iz - izStart)
      call iiuSetPosition(1, izRead, 0)
      if (iiuReadSection(1, array(ibase + 1)) .ne. 0) call exitError('READING IMAGE FILE')
      call taperOutPad(array(ibase + 1), nx, ny, array(ibase + 1), nxDim, nxPad, &
          nyPad, 1, dmeanIn)
      if (iz < izLow .or. iz > izHigh) then
        if (iz < izLow) then
          atten = float(iz - izStart) / (izLow - izStart)
        else
          atten = float(izEnd - iz) / (izEnd - izHigh)
        endif
        base = (1. -atten) * dmeanIn
        do iy = 1, nyPad
          ixBase = ibase + (iy - 1) * nxDim
          do i = ixBase + 1, ixBase + nxPad
            array(i) = base + atten * array(i)
          enddo
        enddo
      endif
    enddo
    !
    ! Take fft and filter and inverse fft
    call thrdfft(array, array(indWork), nxPad, nyPad, nzPad, 0)
    call fftFilter3D(array, nxDim / 2, nyPad, nzPad, ctfa, delta)
    call thrdfft(array, array(indWork), nxPad, nyPad, nzPad, -1)
    !
    ! repack and write
    do iz = izLow, izHigh
      call iiuSetPosition(imUnitOut, iz, 0)
      ibase = nxDim * nyPad * (iz - izStart) + 1
      call irepak(array(ibase), array(ibase), nxDim, nyPad, ixStart, ixStart + nx - 1, &
          iyStart, iyStart + ny - 1)
      call iclden(array(ibase), nx, ny, 1, nx, 1, ny, dmin2, dmax2, dmean2)
      call iiuWriteSection(imUnitOut, array(ibase))
      dmax = max(dmax, dmax2)
      dmin = min(dmin, dmin2)
      dmeanSum = dmeanSum + dmean2
    enddo
  endif
  !
  ind = modeMap(modeOut)
  if (ind > 0) then
    if (dmin < modeMin(ind) .or. dmax >= modeMax(ind)) then
      modeTry = 2
      if (dmin >= -32768 .and. dmax < 32768) modeTry = 1
      write(*, '(a,f7.0,a,f7.0,a,i5,a,i5,a, i2)') &
          'WARNING: MTFFILTER - THE MIN OR MAX OF THE FILTERED DATA (', dmin, ' - ', &
          dmax, ') IS OUTSIDE THE RANGE FOR THE OUTPUT DATA MODE (', modeMin(ind), &
          ' - ', modeMax(ind), '): USE -mode', modeTry
    endif
  endif
  dmean = dmeanSum / numZdo
  call b3dDate(dateStr)
  call time(timeStr)
  !
  write(titlech, 1500) titleStr, dateStr, timeStr
  read(titlech, '(20a4)') (title(kti), kti = 1, 20)
1500 format(A,t57,a9,2X,a8)

  if (imUnitOut == 1 .and. numZdo < nz) then
    !
    ! if writing a subset back to same file, use the extreme of existing
    ! and new data, and just use old mean
    !
    dmin = min(dmin, dminIn)
    dmax = max(dmax, dmaxIn)
    call iiuWriteHeader(1, title, 1, dmin, dmax, dmeanIn)
  else
    call iiuWriteHeader(imUnitOut, title, 1, dmin, dmax, dmean)
  endif
  call imclose(1)
  if (imUnitOut == 3) call imclose(imUnitOut)
  !
  write(6, 500)
500 format(' PROGRAM EXECUTED TO END.')
  call exit(0)
end program


! Applies a filter to a 3D FFT that has been computed with thrdfft
! ARRAY is a complex array with dimensions NXDIM, NY, NZ so NXDIM
! has to be half of (real space size + 2)
!
subroutine fftFilter3D(array, nxDim, ny, nz, ctf, delta)
  implicit none
  integer*4 nxDim, ny, nz
  real*4 ctf(*), delta
  complex array(nxDim, ny, nz)
  integer*4 jx, jy, jz, indf
  real*4 delx, dely, delz, xa, ya, za, s
  !
  delx = 0.5 / (nxDim - 1.)
  dely = 1. / ny
  delz = 1. / nz
  do jz = 1, nz
    za = (jz - 1) * delz
    if (za > 0.5) za = 1. - za
    do jy = 1, ny
      ya = (jy - 1) * dely
      if (ya > 0.5) ya = 1. - ya
      do jx = 1, nxDim
        xa = (jx - 1) * delx
        s = sqrt(xa**2 + ya**2 + za**2)
        indf = s / delta + 1.5
        array(jx, jy, jz) = array(jx, jy, jz) * ctf(indf)
      enddo
    enddo
  enddo
  return
end subroutine fftFilter3D

subroutine amplifier(cutoff, ampfac, power, ctf, nx, ny, delta, nsize)
  implicit none
  integer*4 nx, ny, nsize, i
  real*4 cutoff, ampfac, power, delta, ctf(*), s
  nsize = min(8192, max(1024, 2 * nx, 2 * ny))
  delta = 1. / (0.71 * nsize)

  ! Functional form: (1 + (amp-1)*exp( -(r/cutoff) ^power)) / a"""
  s = 0.
  do i = 1, nsize
    ctf(i) = (1. + (ampfac - 1) * exp(-(s / cutoff)**power)) / ampfac
    if (ctf(i) < 1.e-6) ctf(i) = 0.
    s = s + delta
  enddo
  return
end subroutine amplifier
