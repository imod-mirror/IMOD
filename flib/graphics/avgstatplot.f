* * * * * AVGSTATPLOT * * * * *
c	  
c	  AVGSTATPLOT is an interactive program for displaying and plotting the
c	  output of the program IMAVGSTAT.
c	  
c	  This output consists of mean, standard deviation, and standard error
c	  of the mean for all of the summing areas in a series of different
c	  data sets.  The summing areas were derived from a set of summing
c	  regions specified by a WIMP model; each summing region was divided
c	  into one or more summing areas.  In a single plot, you can include
c	  any collection of summing regions for any collection of data sets.
c	  When you select a summing region, points for all of the areas within
c	  that region are plotted, connected by lines.  There are no lines
c	  connecting the different summing regions for a data set, but those
c	  different regions will all appear with the same symbol type.
c	  
c	  Symbol types are selected by number, but the numbers have different
c	  meanings for symbols on the parallax and on the laser printer.
c	  Symbols 1 to 8 will appear on the parallax as the standard WIMP
c	  modeling symbols: filled and open circle, square, and triangle, X
c	  and +.  Symbols 1 to 19 are available for plots on the laser printer
c	  and yield the standard symbols generated by IMSYMB.
c
c	  Each of the data sets included in a plot may be rescaled
c	  independently; i.e. a particular linear scaling may be applied to all
c	  of the points in a data set, a different scaling may be applied to
c	  all points in another set, etc.  It is also possible to apply the
c	  same scaling, or the same form of scaling, to all data sets without
c	  entering values for each set separately.  Scaling may be specified
c	  in four ways:  1) One may directly specify a factor to multiply by
c	  and a factor to add.  4)  One may specify that the values for a set
c	  are all to be divided by the value for a specified area of that set.
c	  3) One may specify that a given set should have its values shifted
c	  (without any multiplication) so that the mean of a particular
c	  collection of summing regions matches the corresponding mean for
c	  some other data set.  4) One may do a least-squares linear
c	  regression between the data points of the set being scaled and the
c	  corresponding data points of some other set, and use the
c	  coefficients of the regression to determine the scaling factors.
c	  The data points used for regression are the means from the summing
c	  areas within a particular collection of summing regions.
c
c	  If you are displaying only one region, and it has more than 20
c	  summing areas, then you have two options.  First, you are allowed
c	  to select a subset of the areas for display.  Second, you may average
c	  together successive segments of areas.  This is useful for obtaining
c	  an average density tracing for a periodic, repeating structure.
c
c	  Entries to the program are now described in order as they are first
c	  encountered.  After looking at one graph, one may loop back to a
c	  variety of different points in order to change different parameters.
c	  
c	  Name of statistics file output by IMAVGSTAT
c	  
c	  0 for plots on the parallax, or 1 for plots only on the terminal.
c	  .  Enter 0 if the parallax is free, otherwise enter 1.  Note that if
c	  .  you need to use terminal plots, you will need to specify that
c	  .  option each time that you do a plot.
c	  
c	  List of numbers of the sets to include in the graph.  Sets are
c	  .  numbered from 1.  You can enter ranges separated by commas,
c	  .  e.g. 1-3,7-9
c	  
c	  List of symbol types for these sets.  Ranges may be entered, but the
c	  .  total number of types specified must equal the total number of
c	  .  sets.
c	  
c	  List of numbers of the regions to include in the plot.  Ranges are OK
c	  
c	  IF you enter only one region, and that region has more than 20 areas,
c	  .  then make the following two entries:
c	  
c	  .  Starting and ending areas to include in display, or / for all.
c	  
c	  .  / for no averaging of areas; or the interval over which to average
c	  .     areas (i.e. the period of the repeat, which need not be an
c	  .     integer value), the number of areas to roll (shift) the display
c	  .     (+ or - to shift to the right or left), and the number of areas
c	  .     to add to the display by replication.  Such areas will be added
c	  .     symmetrically, half to the beginning and half to the end of
c	  .     the display.  For example, if there are 10 repeats in 564
c	  .     areas, enter 56.4,0,0 the first time and examine the display.
c	  .     If you find that the structure that you wish to appear in the
c	  .     middle of the display (area 29 of 56) is located to the left,
c	  .     say in area 20, then you need to shift by 9.  If you want to
c	  .     display 1.5 repeats, then you need to add 28 areas to the
c	  .     display.  Thus, on a second time through, enter 56.4,9,28.
c	  
c	  Enter a small positive value for error bars whose size is the
c	  .  standard error of the mean times that that value; or a negative
c	  .  value for error bars that are that value times the standard
c	  .  deviation; or a large positive value for error bars showing
c	  .  confidence limits with that percentage of confidence; or 0 for
c	  .  no error bars.
c	  
c	  0 to plot the means of the summing areas, or 1 to plot the integrals,
c	  .  which are the means times the number of pixels.
c	  
c	  List of numbers of sets to rescale - ranges may be entered, or just
c	  .  Return for no rescaling, or enter / to select either all sets or
c	  .  the sets selected last time, as indicated by the prompt.
c	  
c	  IF you select rescaling, first enter 0 to specify scaling separately
c	  .  for each set, or 1 to apply the similar scaling to all sets.
c
c	  IF you select rescaling, next make the following entries for each
c	  .  set that you specified for rescaling:
c	  
c	  .  0 to specify scaling factors directly, 999 to divide values by the
c	  .    value in one area, or the number of another data set, if you
c	  .    wish to regress this set against the other set, or the negative
c	  .    of the number of another set, if you wish to shift this set to
c	  .    have the same mean as that set.
c	  
c	  .  IF you entered 0, next enter the factor to multiply by, and the
c	  .    amount to add after multiplication
c	  
c	  .  IF you entered 999, next enter the region number, and the number
c	  .    of the area within that region, to divide by.
c
c	  .  BUT, IF you entered a set number, next enter a list of the numbers
c	  .    of the regions to use for comparing the two data sets.
c	  
c	  Amount to offset each data set from the last in the X direction (as a
c	  .  fraction of distance between successive summing areas.
c	  
c	  After the last entry, you enter the subroutine BSPLT, whose operation
c	  is described elsewhere.
c	  
c	  When you return from BSPLT, enter one of the following:
c	  1 to loop back to the entry of the number of SEM's or SD's for error
c	  .  bars
c	  2 to loop back to entering the list of regions to plot
c	  3 to loop back to entering the list of data sets and their symbols
c	  4 to loop all the way back and read a new data file
c	  5 to plot the current metacode file on the workstation screen
c	  6 to plot the current metacode file on the laser printer
c	  7 to type values to screen or output to file in tabular format
c	  8 to exit
c	  
c	  If you plot the current metacode file (GMETA.DAT), that file will
c	  be closed and new plots will be placed in a new version of the file.
c	  Thus, if you plot the file on the workstation screen, be sure to plot
c	  it on the printer before generating any new plots, unless you don't
c	  want any printout of it.
c	  
c	  If you elect option 7 to type values in tabular format, you will get
c	  the scaled values just displayed in the last graph.  Enter a file
c	  name to have the table printed into a file, or Return to have it
c	  types on the screen.  If the file already exists, the table will be
c	  appended to the file.
c	  
c	  
c	  David Mastronarde  1/23/90
c
c	  $Author$
c
c	  $Date$
c
c	  $Revision$
c
c	  $Log$

	call plax_initialize('avgstatplot')
	call exit(0)
	end

	subroutine realgraphicsmain()
	parameter (limarea=5000,limset=400,limdat=100000)
	real*4 avg(limarea,limset),sd(limarea,limset)
     &	    ,sem(limarea,limset),xx(limdat),yy(limdat),semadd(limdat),
     &	    sclfac(limset),scladd(limset),xxft(limarea),yyft(limarea)
	integer*4 nsumarea(limarea),indregion(limarea),ngx(limdat),
     &	    nsymb(500),kset(500),ktype(500),kreg(500),nregress(limset),
     &	    nreguse(limset),ireguse(limarea,limset),isrescl(limset),
     &	    npixarea(limarea),normreg(limset),normarea(limset),
     &	    nsampl(limset)
	character*80 statname
	character*23 defsetxt,fmt
	character*8 outregarea(37)
c	real*8 g01caf
c
5	write(*,'(1x,a,$)')'Name of statistics file: '
	read(*,'(a)')statname
	close(1)
	call dopen(1,statname,'ro','f')
c
	write(*,'(1x,a,$)')
     &	    '0 for plots on parallax, 1 for terminal only: '
	read(*,*)iffil
	call grfopn(iffil)
c
	read(1,*)nregion
	read(1,*)(nsumarea(i),i=1,nregion)
c
	ntotarea=0
	do i=1,nregion
	  indregion(i)=ntotarea+1
	  ntotarea=ntotarea+nsumarea(i)
	enddo
c
	read(1,*)ntotin
	if(ntotin.ne.ntotarea)print *,
     &	    'Warning - number of areas does not add up correctly'
	read(1,*)(npixarea(i),i=1,ntotin)
	read(1,*)nsets
c
	write(*,101)nsets,nregion,ntotarea,(nsumarea(i),i=1,nregion)
101	format(i5,' data sets',/,i5,' summing regions with a total of',
     &	    i5,' summing areas',/,' Number of areas in these regions:',
     &	    /,(20i4))
c
	do iset=1,nsets
	  read(1,*)nsampl(iset)
	  do iarea=1,ntotarea
	    read(1,*)idum,avg(iarea,iset),sd(iarea,iset)
     &		,sem(iarea,iset)
	  enddo
	enddo
c	  
10	print *,'Enter list of numbers of sets to plot (ranges ok)'
	call rdlist(5,kset,nsetplot)
	ntytot=0
	do while(ntytot.lt.nsetplot)
	  write(*,'(a,i3,a)')' Enter list of symbol types for',
     &	      nsetplot-ntytot, ' sets (ranges ok)'
	  call rdlist(5,ktype(ntytot+1),ntyadd)
	  ntytot=ntytot+ntyadd
	enddo
	nrescale=nsetplot
	defsetxt='all sets)'
	do i=1,nsetplot
	  isrescl(i)=kset(i)
	enddo
c
15	print *,'Enter list of numbers of regions to plot (ranges ok)'
	call rdlist(5,kreg,nregplot)
c	  
	ifsubset=0
	if(nregplot.eq.1.and.nsumarea(kreg(1)).gt.20)then
	  ifsubset=1
	  istrsub=1
	  iendsub=nsumarea(kreg(1))
	  write(*,'(1x,a,$)')
     &	      'Beginning and ending areas to plot, or / for all: '
	  read(5,*)istrsub,iendsub
	  istrsub=max(1,istrsub)
	  iendsub=min(nsumarea(kreg(1)),iendsub)
	  avgspace=0
	  write(*,'(1x,a,/,a,$)')'Enter interval to average over'//
     &	      ' (fractions OK), # of areas to roll average right,',
     &	      '  and # of areas to replicate symmetrically'//
     &	      ' (or / for no averaging): '
	  read(5,*)avgspace,nrollavg,nreplic
	endif
c	  
20	write(*,'(1x,a,/,a,$)')'For error bars, enter a small positive'
     &	    //' # for that # of SEM, a negative # for',
     &	    ' that # of SD, or a large positive # for '//
     &	    'confidence limits at that % level: '
	read(*,*)facsem
c	  
	write(*,'(1x,a,$)')'0 to use means, 1 to use integrals: '
	read(*,*)integral
	print *,'Enter list of numbers of sets to rescale'
	print *,'     (ranges OK, Return for none, / for ',defsetxt
	call rdlist(5,isrescl,nrescale)
	defsetxt='same sets as last time)'
	if(nrescale.gt.0.and.(nrescale.gt.1.or.isrescl(1).ne.0))then
	  write(*,'(1x,a,$)')'0 to specify each independently, 1 to '
     &	      //'use same specification for all: '
	  read(*,*)ifsamescl
	  nspecify=nrescale
	  if(ifsamescl.ne.0)nspecify=1
	  do ires=1,nspecify
	    write(*,'(1x,a,i3,a,/,a,/,a,$)')'For set #',isrescl(ires),
     &		', enter 0 to specify scaling, 999 to divide by one'//
     &		' area,','  or # of other set to compare with',
     &		'     (+ set # for regression, - set # '//
     &		'to shift to same mean): '
	    read(*,*)nregress(ires)
	    if(nregress(ires).eq.0)then
	      write(*,'(1x,a,$)')'Factors to multiply by, then add: '
	      read(*,*)sclfac(ires),scladd(ires)
	    elseif(nregress(ires).eq.999)then
	      write(*,'(1x,a,$)')'Region #, and # of area within'//
     &		  ' region to divide by: '
	      read(*,*)normreg(ires),normarea(ires)
	    else
	      print *,'Enter list of regions to use for comparing sets'//
     &		  ' (ranges OK)'
	      call rdlist(5,ireguse(1,ires),nreguse(ires))
	    endif
	  enddo
	else
	  nrescale=0
	endif
c
	fracofs=0
	if(nsetplot.gt.1)then
	  write(*,'(1x,a,$)')
     &	      'Fraction to offset sets from each other in X: '
	  read(*,*)fracofs
	endif
c	  
	nx=0
	ngrps=0
	ymax=0.
	do iset=1,nsetplot
	  xar=(iset-nsetplot/2)*fracofs
	  jset=kset(iset)
	  ires=0
	  sclf=1.
	  scla=0.
	  do i=1,nrescale
	    if(jset.eq.isrescl(i))ires=i
	  enddo
	  if(ires.gt.0)then
	    if(ifsamescl.ne.0)ires=1
	    if(nregress(ires).eq.0)then
	      sclf=sclfac(ires)
	      scla=scladd(ires)
	    elseif(nregress(ires).eq.999)then
	      scla=0.
	      indar=indregion(normreg(ires))+normarea(ires)-1
	      sclf=1./avg(indar,jset)
	      if(integral.ne.0)sclf=sclf/npixarea(indar)
	    else
	      npnts=0
	      do ireg=1,nreguse(ires)
		jreg=ireguse(ireg,ires)
		indstr=indregion(jreg)
		if(ifsubset.eq.0)then
		  indend=indstr+nsumarea(jreg)-1
		else
		  indend=indstr+iendsub-1
		  indstr=indstr+istrsub-1
		endif
		do indar=indstr,indend
		  npnts=npnts+1
		  yyft(npnts)=avg(indar,abs(nregress(ires)))
		  xxft(npnts)=avg(indar,jset)
		enddo
	      enddo
	      if(npnts.gt.0)then
		if(nregress(ires).gt.0)then
		  call lsfit(xxft,yyft,npnts,sclf,scla,rho)
		else
		  sclf=1.
		  call avgsd(xxft,npnts,xxavg,xxsd,xxsem)
		  call avgsd(yyft,npnts,yyavg,yysd,yysem)
		  scla=yyavg-xxavg
		endif
	      endif
	      write(*,103)jset,npnts,rho,sclf,scla
103	      format(' Set #',i3,', n=',i4,', r=',f6.3,', multiply by',
     &		  f9.5,' and add',f7.2)
	    endif
	  endif
c
	  do ireg=1,nregplot
	    ngrps=ngrps+1
	    nsymb(ngrps)=ktype(iset)
	    jreg=kreg(ireg)
	    indstr=indregion(jreg)
	    if(ifsubset.eq.0)then
	      indend=indstr+nsumarea(jreg)-1
	    else
	      indend=indstr+iendsub-1
	      indstr=indstr+istrsub-1
	    endif
	    do indar=indstr,indend
	      xar=xar+1.
	      nx=nx+1
	      if(facsem.gt.30.)then
		ifail=0
c		tcrit=g01caf(dble((1.+0.01*facsem)/2.),nsampl(jset)-1,ifail)
		tcrit=tvalue((1.+0.01*facsem)/2.,nsampl(jset)-1)
		semadd(nx)=sclf*tcrit*sem(indar,jset)
	      elseif(facsem.ge.0)then
		semadd(nx)=sclf*facsem*sem(indar,jset)
	      else
		semadd(nx)=-sclf*facsem*sd(indar,jset)
	      endif
	      xx(nx)=xar
	      yy(nx)=sclf*avg(indar,jset)+scla
	      if(integral.ne.0)then
		yy(nx)=yy(nx)*npixarea(indar)
		semadd(nx)=semadd(nx)*npixarea(indar)
	      endif
	      ngx(nx)=ngrps
	      ymax=max(ymax,abs(yy(nx)))
	    enddo
	    if(avgspace.gt.0.)then
	      navgspace=nint(avgspace)
	      ntotval=indend+1-indstr
	      ipt=nx+1-ntotval
	      do iavg=1,navgspace
		navlim=1+ntotval/avgspace
		navg=0
		do itmp=1,navlim
		  indyy=ipt+nint((itmp-1)*avgspace)
		  if(indyy.le.nx)then
		    yyft(itmp)=yy(indyy)
		    navg=navg+1
		  endif
		enddo
		call avgsd(yyft,navg,spacavg,spacsd,spacsem)
		yy(ipt)=spacavg
		if(facsem.ge.0)semadd(ipt)=facsem*spacsem
		if(facsem.lt.0)semadd(ipt)=-facsem*spacsd
		ipt=ipt+1
	      enddo
	      do iavg=1,navgspace
		ipt=nx+iavg-ntotval
		xxft(iavg)=semadd(ipt)
		yyft(iavg)=yy(ipt)
	      enddo
	      ifrom=navgspace-nreplic/2-nrollavg
	      if(ifrom.lt.1)ifrom=ifrom+navgspace
	      if(ifrom.lt.1)ifrom=ifrom+navgspace
	      if(ifrom.gt.navgspace)ifrom=ifrom-navgspace
	      do iavg=1,navgspace+nreplic
		ipt=nx+iavg-ntotval
		yy(ipt)=yyft(ifrom)
		semadd(ipt)=xxft(ifrom)
		ifrom=mod(ifrom,navgspace)+1
	      enddo
	      nx=nx+navgspace+nreplic-ntotval
	    endif
	  enddo
	enddo
	call errplt(xx,yy,ngx,nx,nsymb,ngrps,semadd,0,0)
30	write(*,'(1x,a,/,a,$)')'Respecify error bars (1), regions (2)'//
     &	    ', data sets (3), or input file (4),',
     &	    ' Plot metacode on screen (5) or printer (6),'//
     &	    ' type values (7), or exit (8): '
	read(*,*)iopt
	if(iopt.lt.1)go to 30
	go to (20,15,10,5,35,35,40,60),iopt
	go to 30
c
35	call pltout(6-iopt)
	go to 30
40	print *,'Enter file name to store values in, or Return',
     &	    ' to type values on screen'
	read(*,'(a)')statname
	if(statname.eq.' ')then
	  iout=6
	else
	  iout=9
	  ifappend=0
	  close(9)
	  open(9,file=statname,err=40,status='unknown')
42	  read(9,'(a4)',end=44)statname
	  if(ifappend.eq.0)print *,'Appending to existing file...'
	  ifappend=1
	  go to 42
	endif
44	write(iout,'(a,20x,a)')' Set','Region-area'
	nareaout=0
	do ireg=1,nregplot
	  do iarea=1,nsumarea(jreg)
	    nareaout=nareaout+1
	    write(outregarea(nareaout),'(i5,''-'',i2)')kreg(ireg),iarea
	  enddo
	enddo
	write(iout,'(5x,9a8)')(outregarea(i),i=1,nareaout)
	write(iout,*)
	ipow=max(0.,min(4.,alog10(10.*ymax)))
	fmt='(i4,(t7,9f8.'//char(52-ipow)//'))'
	nperset=nx/nsetplot
	ibase=0
	do iset=1,nsetplot
	  write(iout,fmt)kset(iset),(yy(i),i=ibase+1,ibase+nperset)
	  ibase=ibase+nperset
	enddo
	write(iout,'(/)')
	go to 30
60	call plxoff
	call imexit()
	end
