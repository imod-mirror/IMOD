c	  GET_TILT_ANGLES will read in tilt angles by the user's method of
c	  choice.  NVIEW should be the number of views, if known, or 0 if it
c	  not known, in which case the routine will return the number in this
c	  variable.  NUNIT should contain the number of a free logical unit.
c	  Tilt angles are returned in TILT.
c	  LIMTILT should contain the dimensions of TILT.
c	  IFPIP should be set to 0 for interactive input, or nonzero for
c	  input via PIP.  In the later case, the program must define the
c	  options FirstTiltAngle, TiltIncrement, TiltFile, and TiltAngles.
c	  If a TiltFile is entered, or if TiltAngles are entered, they 
c	  supercede entries of FirstTiltAngle and TiltIncrement.
c	  
c	  $Author$
c
c	  $Date$
c
c	  $Revision$
c
c	  $Log$
c
	subroutine get_tilt_angles(nview,nunit,tilt, limtilt, ifpip)
	implicit none
	integer*4 nview, nunit, ifpip, limtilt
	real*4 tilt(*)
	character*120 filename
	integer*4 nvin, i, ierr, ierr2, numLines, index, ninLine
	real*4 tiltstr, tiltinc
	logical startIncOK
	integer*4 PipGetFloat,PipGetString, PipNumberOfEntries
	integer*4 PipGetFloatArray
c	  
	if (ifpip .ne. 0) then
	  ierr = PipGetFloat('FirstTiltAngle', tiltstr)
	  ierr2 = PipGetFloat('TiltIncrement', tiltinc)
	  startIncOK = ierr .eq. 0 .and. ierr2 .eq. 0

	  ierr = PipGetString('TiltFile', filename)
	  ierr2 = PipNumberOfEntries('TiltAngles', numLines)

	  if (ierr .gt. 0 .and. numLines .eq. 0) then
	    if (.not. startIncOK) call gta_errorexit('NO TILT ANGLES'//
     &		' SPECIFIED BY START AND INCREMENT, FILE, OR '//
     &		'INDIVIDUAL VALUES')
	    do i=1,nview
	      tilt(i)=tiltstr+(i-1)*tiltinc
	    enddo
	    return
	  endif

	  if (numLines .gt. 0) then
	    if (ierr .eq. 0) call gta_errorexit('YOU CANNOT SPECIFY '//
     &		'BOTH A TILT ANGLE FILE AND INDIVIDUAL ENTRIES')
	    index = 0
	    do i = 1, numLines
	      ninLine = 0
	      ierr = PipGetFloatArray('TiltAngles', tilt(index + 1), ninLine,
     &		  limtilt - index)
	    enddo
	    if (nview .eq. 0) nview = index
	    if (index .ne. nview) then 
	      print *
	      print *,'ERROR: GET_TILT_ANGLES -',nview,
     &		  ' ANGLES EXPECTED BUT ONLY', index,' ENTERED'
	      call exit(1)
	    endif
	    return
	  endif
	else
	  if(nview.eq.0)then
	    write(*,'(1x,a,/,a,/,a,$)')'Enter the # of tilt angles to '//
     &		'specify them by starting and increment angle,',
     &		'     - the # of angles to specify each individual value,'
     &		,'    or 0 to read angles from a file: '
	    read(5,*)nvin
	    nview = abs(nvin)
	  else
	    write(*,'(1x,a,/,a,/,a,$)')'Enter 1 to '//
     &		'specify starting and increment angle,',
     &		'     -1 to specify each individual value,'
     &		,'    or 0 to read angles from a file: '
	    read(5,*)nvin
	  endif
	  if (nview .gt. limtilt) call gta_errorexit(
     &	      'TOO MANY VIEWS FOR TILT ANGLE ARRAY')
	  if(nvin.gt.0)then
	    write(*,'(1x,a,$)')'Starting and increment angle: '
	    read(5,*)tiltstr,tiltinc
	    do i=1,nview
	      tilt(i)=tiltstr+(i-1)*tiltinc
	    enddo
	    return
	  elseif(nvin.lt.0)then
	    print *,'Enter all tilt angles'
	    read(5,*,err=40,end=30)(tilt(i),i=1,nview)
	    return
	  else
	    write(*,'(1x,a,$)')'Name of file with tilt angles: '
	    read(5,'(a)')filename
	  endif
	endif

	call dopen(nunit,filename,'ro','f')
	if(nview.eq.0)then
10	  read(nunit,*,end=20,err=40)tilt(nview+1)
	  nview=nview+1
	  if (nview .gt. limtilt) call gta_errorexit(
     &	      'TOO MANY VIEWS FOR TILT ANGLE ARRAY')
	  go to 10
	else
	  read(nunit,*,err=40,end=30)(tilt(i),i=1,nview)
	endif
20	close(nunit)
	return
30	call gta_errorexit(
     &	    'end of file or input reached reading tilt angles')
40	call gta_errorexit('error reading tilt angles')
	end

	subroutine gta_errorexit(message)
	character*(*) message
	print *
	print *,'ERROR: GET_TILT_ANGLES - ',message
	call exit(1)
	end
	
