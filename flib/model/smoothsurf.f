* * * * * * * SMOOTHSURF * * * * * * *
c	  
c	  This program will smooth a surface defined by model contours,
c	  adjusting the positions of points in each contour as needed.  At each
c	  point on the surface, it fits a 3-D polynomial to all points within
c	  a defined range of Z-levels and within a specified distance of the
c	  central point.  That point's position is then replaced by the fitted
c	  position from the polynomial.  After this surface smoothing
c	  operation, each contour is independently smoothed by local fitting to
c	  2-D (ordinary) polynomials.
c	  
c	  Inputs to the program:
c	  
c	  Name of input model file
c	  
c	  Name of output model file
c	  
c	  List of IMOD objects to smooth, or Return to smooth all objects.
c	  Ranges may be entered, e.g. 1-3,7,9-11.
c
c	  Order of 3-D polynomials to fit to the surface.  Note that a second-
c	  order polynomial has 5 terms and a third-order has 9 terms.  The
c	  polynomial should not have too high an order for the number of points
c	  being fit.
c	  
c	  Number of levels in Z (i.e., number of different contours) to
c	  include in the fit.
c	  
c	  Limiting distance in 3-D for points to be included in the fit.
c	  
c	  Polynomial order for smoothing of individual contours, or zero to
c	  omit this smoothing.
c	  
c	  NOTE: The program has not been tested with a variety of models.  With
c	  the particular models tested so far, good results were obtained with 
c	  third-order 3-D polynomials fitted to 7 levels in Z and including
c	  points within a distance of 15 pixels.  Order 2 was good for
c	  smoothing of individual contours.
c	  
c	  David Mastronarde, 9/9/97
c
c	  $Author$
c
c	  $Date$
c
c	  $Revision$
c
c	  $Log$
c
	include 'model.inc'
	parameter (idim=1000,limpt=10000,idzlim=16)
C   
	REAL*4 xt(limpt),yt(limpt),zt(limpt),pt(limpt,3),p_new(2,max_pt)
	equivalence (xt,pt),(yt,pt(1,2)),(zt,pt(1,3))
C   
	CHARACTER*80 FILin,filout
c	  
	logical readw_or_imod,failed
	include 'statsize.inc'
	real*4 xr(msiz,idim), sx(msiz), xm(msiz), sd(msiz)
     &	    , ss(msiz,msiz), ssd(msiz,msiz), d(msiz,msiz), r(msiz,msiz)
     &	    , b(msiz), b1(msiz)
	real*4 vect(msiz),slop(msiz)
	integer*4 izobj(max_obj_num)
	integer*4 iobjbest(-idzlim:idzlim),iptbest(-idzlim:idzlim)
	integer*4 iobjdo(256)
	integer*4 getimodhead
c
	minpts=4
	tolcross=4.
	closethresh=1.
c
81	write(*,'(1x,a,$)')'Name of original model file: '
	read(5,'(a)')filin
	if(.not.readw_or_imod(filin))go to 81
	print *,n_point,' points,',max_mod_obj,' contours'
c
	write(*,'(1x,a,$)')'Name of output model file: '
	read(5,'(a)')filout
c	  
	print *,'Enter list of objects to smooth (ranges OK),',
     &	    ' or Return for all'
	nobjdo=0
	call rdlist(5,iobjdo,nobjdo)

	write(*,'(1x,a,$)')'Order of 3-D polynomial to fit: '
	read(5,*)iorder
	write(*,'(1x,a,$)')
     &	    '# of levels in Z to include in fit: '
	read(5,*)nzfit
	write(*,'(1x,a,$)')'Overall distance limit for fit: '
	read(5,*)distlim
	write(*,'(1x,a,$)') 'Polynomial order for single contour '//
     &	    'smoothing (0 for none): '
	read(5,*)iorder2
c	  
c	  unflip data if it was flipped
c
	ierr=getimodhead(xyscal,zscale,xofs,yofs,zofs,ifflip)
	if (ierr.ne.0)then
	  print *,'Error getting imod header, assuming model not ',
     &	      'flipped'
	  ifflip=0
	endif
	if(ifflip.ne.0)then
	  do i=1,n_point
	    tmp=p_coord(2,i)
	    p_coord(2,i)=p_coord(3,i)
	    p_coord(3,i)=tmp
	  enddo
	endif
c	  
c	  set minimum separation between points to get at least minpts
c	  each side of center
c
	sepmin=distlim/minpts
c	  
c	  uncross/straighten out start and end points, keep track of Z of
c	  object
c
	do iobj=1,max_mod_obj
	  ninobj=npt_in_obj(iobj)
	  if(ninobj.gt.2)then
	    ibase=ibase_obj(iobj)
	    ipnt1=object(ibase+1)
	    izobj(iobj)=nint(p_coord(3,ipnt1))
	    call uncrosscont(p_coord,3,object,ibase,ninobj,ninobj,20.,
     &		tolcross*2.)
	  else
	    izobj(iobj)=-10000
	  endif
	enddo
c	  
c	  make copy into new coordinates
c
	do i=1,n_point
	  p_new(1,i)=p_coord(1,i)
	  p_new(2,i)=p_coord(2,i)
	enddo
c
	do iobj=1,max_mod_obj
	  ninobj=npt_in_obj(iobj)
	  ibase=ibase_obj(iobj)
	  ifonlist=0
	  if(nobjdo.eq.0)then
	    ifonlist=1
	  else
	    imodobj=256-obj_color(2,iobj)
	    do ichek=1,nobjdo
	      if(imodobj.eq.iobjdo(ichek))ifonlist=1
	    enddo
	  endif
	  if(ninobj.gt.2.and.ifonlist.eq.1)then
	    do ipt=1,ninobj
	      ipnt=object(ipt+ibase)
c		
c		consider this point in this object; look for the closest point
c		in objects of the same color (i.e. contours of the same object)
c		within the z range and the distance limit
c
	      xx=p_coord(1,ipnt)
	      yy=p_coord(2,ipnt)
	      do i=-nzfit,nzfit
		iobjbest(i)=0
	      enddo
	      iobjbest(0)=iobj
	      iptbest(0)=ipt
	      ifspan=0
	      idzlo=-nzfit/2
	      idzhi=idzlo+nzfit-1
	      idzmin=0
	      idzmax=0
	      loop=1
	      do while(ifspan.eq.0.and.loop.le.2)
		do idz=idzlo,idzhi
		  if(iobjbest(idz).eq.0)then
		    dzsq=idz**2
		    iz=izobj(iobj)+idz
		    distmin=distlim**2
		    do jobj=1,max_mod_obj
		      if(izobj(jobj).eq.iz.and.
     &			  obj_color(2,jobj).eq.obj_color(2,iobj))then
			jbase=ibase_obj(jobj)
			do jpt=1,npt_in_obj(jobj)
			  jpnt=object(jpt+jbase)
			  dx=xx-p_coord(1,jpnt)
			  if(abs(dx).le.distlim)then
			    dy=yy-p_coord(2,jpnt)
			    if(abs(dy).le.distlim)then
			      distsq=dx**2+dy**2+dzsq
			      if(distsq.lt.distmin)then
				distmin = distsq
				iobjbest(idz) = jobj
				iptbest(idz) = jpt
			      endif
			    endif
			  endif
			enddo
		      endif
		    enddo
		  endif
c		    
c		    if closest point is in an object of 1 or 2 points, ignore
c		    
		  if(iobjbest(idz).ne.0)then
		    if(npt_in_obj(iobjbest(idz)).le.2)then
		      iobjbest(idz)=0
		    else
		      idzmin=min(idzmin,idz)
		      idzmax=max(idzmax,idz)
		    endif
		  endif
		enddo
c		  
c		  if didn't get the full range in both directions in Z, redo
c		  the search at farther Z values to find a full span of Z
c		  values that includes the point in question near its middle
c
		if(loop.eq.1)then
		  if(idzmin.eq.idzlo.and.idzmax.eq.idzhi)then
		    ifspan=1
		  else
		    if(idzmax.lt.idzhi)idzlo=idzmax-(nzfit-1)
		    if(idzmin.gt.-nzfit/2)idzhi=idzmin+nzfit-1
		  endif
		endif
		loop=loop+1
	      enddo
c		
c		use next and previous point to define an angle to rotate to
c		the horizontal
c		
	      iplas=object(ibase+indmap(ipt-1,ninobj))
	      ipnex=object(ibase+indmap(ipt+1,ninobj))
	      dx=p_coord(1,ipnex)-p_coord(1,iplas)
	      dy=p_coord(2,ipnex)-p_coord(2,iplas)
	      dlen=sqrt(dx**2+dy**2)
	      if(dlen.gt.1..and.idzmin.lt.0.and.idzmax.gt.0)then
		sinth=-dy/dlen
		costh=dx/dlen
		mzfit=0
		nfit=0
c		  
c		  for each Z level, start at the closest point and add points
c		  within the distance limit or until X starts to fold back
c		  toward the central point
c
		do idz=idzmin,idzmax
		  if(iobjbest(idz).ne.0)then
		    mzfit=mzfit+1
		    jobj=iobjbest(idz)
		    jbase=ibase_obj(jobj)
		    xlas=-1.e10
		    jpt=iptbest(idz)
		    ninj=npt_in_obj(jobj)
		    do idir=1,-1,-2
		      iftoofar=0
		      do while(iftoofar.eq.0.and.nfit.lt.idim)
			jpnt=object(jbase+jpt)
			dx=p_coord(1,jpnt)-xx
			dy=p_coord(2,jpnt)-yy
			xrot=costh*dx-sinth*dy
			yrot=sinth*dx+costh*dy
			if(idir*(xrot-xlas).lt.0.)then
			  iftoofar=1
			else
			  dist=sqrt(dx**2+dy**2+idz**2)
			  ifsave=1
			  if(dist.gt.distlim)then
c			      
c			      if go past the distance limit, at a point within
c			       the limit if is far from the last point
c
			    if(dist-distlas.gt.0.1*distlim)then
			      frac=(distlim-distlas)/(dist-distlas)
			      xrot=xlas+frac*(xrot-xlas)
			      yrot=ylas+frac*(yrot-ylas)
			    else
			      ifsave=0
			    endif
			    iftoofar=1
			  endif
			  if(ifsave.eq.1)then
			    nfit=nfit+1
			    xt(nfit)=xrot
			    yt(nfit)=yrot
			    zt(nfit)=idz
			    if(jpt.eq.iptbest(idz))then
			      xcen=xrot
			      ycen=yrot
			      distcen=dist
			    else
c				
c				also add points to maintain a maximum
c				separation between point = sepmin
c
			      sep=sqrt((xrot-xlas)**2+(yrot-ylas)**2)
			      ninsert=sep/sepmin
			      do j=1,ninsert
				frac=j/(ninsert+1.)
				if(nfit.lt.idim)then
				  nfit=nfit+1
				  xt(nfit)=xlas+frac*(xrot-xlas)
				  yt(nfit)=ylas+frac*(yrot-ylas)
				  zt(nfit)=idz
				endif
			      enddo
			    endif
			  endif
			  xlas=xrot
			  ylas=yrot
			  distlas=dist
			  jpt=indmap(jpt+idir,ninj)
			endif
		      enddo
		      jpt=indmap(iptbest(idz)-1,ninj)
		      xlas=xcen
		      ylas=ycen
		      distlas=distcen
		    enddo
		  endif
		enddo
c		  
c		  figure out a valid order for the fit and do the fit
c
		norder=0
		do while(norder.lt.iorder.and.norder.lt.mzfit-1.and.
     &		    (norder+1)*(norder+4).le.nfit)
		  norder=norder+1
		enddo
		if(norder.ge.0)then
		  nindep=norder*(norder+3)/2	!# of independent variables
		  do i=1,nfit
		    call polyterm(xt(i),zt(i),norder,xr(1,i))
		    xr(nindep+1,i)=yt(i)
		  enddo
		  call multr(xr,nindep+1,nfit,sx,ss,ssd,d,r,xm,sd,b,b1,
     &		      c1, rsq ,fra)
		  ynew=c1
c		    
c		    back rotate the fitted point to get the new value
c
		  p_new(1,ipnt)=sinth*ynew+xx
		  p_new(2,ipnt)=costh*ynew+yy
		endif
	      endif
	    enddo
	  endif
	enddo
c	  
c	  now treat each individual contour
c
	do iobj=1,max_mod_obj
	  ninobj=npt_in_obj(iobj)
	  if(ninobj.gt.2)then
	    ibase=ibase_obj(iobj)
c	  
c	      smoothing the same way as above: take each point as a center
c	    
	    do iptcen=1,ninobj
	      ipc=object(ibase+iptcen)
	      xmid=p_new(1,ipc)
	      ymid=p_new(2,ipc)
	      if(iorder2.gt.0)then
		iplas=object(ibase+indmap(iptcen-1,ninobj))
		ipnex=object(ibase+indmap(iptcen+1,ninobj))
		dx=p_new(1,ipnex)-p_new(1,iplas)
		dy=p_new(2,ipnex)-p_new(2,iplas)
		dlen=sqrt(dx**2+dy**2)
		if(dlen.gt.1.)then
		  sinth=-dy/dlen
		  costh=dx/dlen
		  nfit=0
		  xlas=-1.e10
		  jpt=iptcen
c		    
c		    work from the center outward until get past limit or X
c		    folds back
c
		  do idir=1,-1,-2
		    iftoofar=0
		    do while(iftoofar.eq.0.and.nfit.lt.idim)
c			
c			rotate so tangent is horizontal
c
		      jpnt=object(jbase+jpt)
		      dx=p_new(1,jpnt)-xmid
		      dy=p_new(2,jpnt)-ymid
		      xrot=costh*dx-sinth*dy
		      yrot=sinth*dx+costh*dy
		      if(idir*(xrot-xlas).lt.0.)then
			iftoofar=1
		      else
			dist=sqrt(dx**2+dy**2)
			ifsave=1
			if(dist.gt.distlim)then
			  if(dist-distlas.gt.0.1*distlim)then
			    frac=(distlim-distlas)/(dist-distlas)
			    xrot=xlas+frac*(xrot-xlas)
			    yrot=ylas+frac*(yrot-ylas)
			  else
			    ifsave=0
			  endif
			  iftoofar=1
			endif
			if(ifsave.eq.1)then
			  nfit=nfit+1
			  xt(nfit)=xrot
			  yt(nfit)=yrot
			  if(jpt.eq.iptcen)then
			    xcen=xrot
			    ycen=yrot
			    distcen=dist
			  else
c			      
c			      add points to maintain maximum separation: this
c			      is superior to a fit to a fixed number of points
c
			    sep=sqrt((xrot-xlas)**2+(yrot-ylas)**2)
			    ninsert=sep/sepmin
			    do j=1,ninsert
			      frac=j/(ninsert+1.)
			      if(nfit.lt.idim)then
				nfit=nfit+1
				xt(nfit)=xlas+frac*(xrot-xlas)
				yt(nfit)=ylas+frac*(yrot-ylas)
			      endif
			    enddo
			  endif
			endif
			xlas=xrot
			ylas=yrot
			distlas=dist
			jpt=indmap(jpt+idir,ninobj)
		      endif
		    enddo
		    jpt=indmap(iptcen-1,ninobj)
		    xlas=xcen
		    ylas=ycen
		    distlas=distcen
		  enddo
c		    
c		    get order, set up and do fit, substitute fitted point
c
		  norder=min(iorder2,nfit-2)
		  if(norder.gt.0)then
		    do i=1,nfit
		      do j=1,norder
			xr(j,i)=xt(i)**j
		      enddo
		      xr(norder+1,i)=yt(i)
		    enddo
		    call multr(xr,norder+1,nfit,sx,ss,ssd,d,r,xm,sd,b,b1,
     &		      bint, rsq ,fra)
c		    call polyfit(xt,yt,nfit,norder,slop,bint)
		    xmid=sints*bint+xmid
		    ymid=costs*bint+ymid
		  endif
		endif
	      endif
	      p_coord(1,ipc)=xmid
	      p_coord(2,ipc)=ymid
	    enddo
c	  
c	      eliminate close points
c
	    call elimclose(p_coord,3,object,ibase,ninobj,closethresh,3)

	    npt_in_obj(iobj)=ninobj
c	  
c	  fix up points that are crossed
c	  
	    do ipt=1,ninobj 
	      call uncrosscont(p_coord,3,object,ibase,ninobj,ipt,30.,
     &		  tolcross)
	    enddo
	  endif
	enddo
c
c	  reflip data if it was unflipped
c
	if(ifflip.ne.0)then
	  do i=1,n_point
	    tmp=p_coord(2,i)
	    p_coord(2,i)=p_coord(3,i)
	    p_coord(3,i)=tmp
	  enddo
	endif
	call write_wmod(filout)
	call exit(0)
	end




c	  POLYTERM computes polynomial terms from x and y of order norder,
c	  puts in array vect.  The first set of terms is x and y.  Each next
c	  set is the previous set multipled by x, plus the last term of the
c	  previous set multiplied by y
c
	subroutine polyterm(x,y,norder,vect)
	real*4 vect(*)
	vect(1)=x
	vect(2)=y
	istr=1
	iend=2
	do iorder=2,norder
	  do i=istr,iend
	    vect(i+iorder)=vect(i)*x
	  enddo
	  istr=istr+iorder
	  vect(iend+iorder+1)=vect(iend)*y
	  iend=iend+iorder+1
	enddo
	return
	end

c
	function goodangle(thin)
	theta=thin
	if(theta.lt.-180)theta=theta+360.
	if(theta.ge.180.)theta=theta-360.
	goodangle=theta
	return
	end



	subroutine uncrosscont(p_coord,nxyz,object,ibase,ninobj,ip4,ang,
     &	    tol)
	real*4 p_coord(nxyz,*)
	integer*4 object(*)
	if(ninobj.le.3)return
	ip3=indmap(ip4-1,ninobj)
	ip1=indmap(ip4+1,ninobj)
	ip2=indmap(ip4+2,ninobj)
	ipnt1=object(ibase+ip1)
	ipnt2=object(ibase+ip2)
	ipnt3=object(ibase+ip3)
	ipnt4=object(ibase+ip4)
	x1=p_coord(1,ipnt1)
	y1=p_coord(2,ipnt1)
	x2=p_coord(1,ipnt2)
	y2=p_coord(2,ipnt2)
	x3=p_coord(1,ipnt3)
	y3=p_coord(2,ipnt3)
	x4=p_coord(1,ipnt4)
	y4=p_coord(2,ipnt4)
	call point_to_line(x4,y4,x1,y1,x2,y2,tmin1,distsq1)
	call point_to_line(x1,y1,x3,y3,x4,y4,tmin2,distsq2)
	if(min(distsq1,distsq2).le.tol**2)then
	  tstrt=atan2d(y2-y1,x2-x1)
	  tend=atan2d(y4-y3,x4-x3)
	  tcon=atan2d(y1-y4,x1-x4)
	  halfdiff=0.5*goodangle(tstrt-tend)
	  tmid=goodangle(tend+halfdiff)
	  halfcrit=abs(halfdiff)+ang
	  if(abs(goodangle(tcon-tmid)).gt.halfcrit)then
	    fraclimst=1.-1./sqrt((x2-x1)**2+(y2-y1)**2)
	    fraclimnd=1.-1./sqrt((x4-x3)**2+(y4-y3)**2)
	    itry=1
c	    print *,'shifting point',ipnt4,' of',ninobj
	    do while(itry.le.10)
	      fracst=max(0.,itry*fraclimst/10.)
	      fracnd=max(0.,itry*fraclimnd/10.)
	      x1t=x1+fracst*(x2-x1)
	      x4t=x4+fracnd*(x3-x4)
	      y1t=y1+fracst*(y2-y1)
	      y4t=y4+fracnd*(y3-y4)
	      tcon=atan2d(y1t-y4t,x1t-x4t)
	      if(abs(goodangle(tcon-tmid)).le.halfcrit)itry=10
	      itry=itry+1
	    enddo
	    p_coord(1,ipnt1)=x1t
	    p_coord(2,ipnt1)=y1t
	    p_coord(1,ipnt4)=x4t
	    p_coord(2,ipnt4)=y4t
	  endif
	endif
	return
	end




	subroutine point_to_line(x0,y0,x1,y1,x2,y2,tmin,distsq)
	tmin=((x0-x1)*(x2-x1)+(y0-y1)*(y2-y1))/((x2-x1)**2+(y2-y1)**2)
	tmin=max(0.,min(1.,tmin))
	distsq=(x1+tmin*(x2-x1)-x0)**2+(y1+tmin*(y2-y1)-y0)**2
	return
	end



	subroutine elimclose(p_new,nxyz,object,ibase,ninobj,closethresh,
     &	    minpts)
	real*4 p_new(nxyz,*)
	integer*4 object(*)
	closesq=closethresh**2
	iseg=1
	idel=0
	do while(iseg.le.ninobj.and.ninobj.gt.minpts)
	  ip3=indmap(iseg+1,ninobj)
	  ipt3=object(ibase+ip3)
	  ipseg=object(ibase+iseg)
	  if((p_new(1,ipt3)-p_new(1,ipseg))**2+
     &	      (p_new(2,ipt3)-p_new(2,ipseg))**2 .lt. closesq)then
c	      print *,'eliminating segment',iseg,' of',ninobj
	    ip1=indmap(iseg-1,ninobj)
	    ip4=indmap(iseg+2,ninobj)
	    ipt1=object(ibase+ip1)
	    ipt4=object(ibase+ip4)
	    idel=iseg
	    if((p_new(1,ipt1)-p_new(1,ipseg))**2+
     &		(p_new(2,ipt1)-p_new(2,ipseg))**2 .gt.
     &		(p_new(1,ipt3)-p_new(1,ipt4))**2+
     &		(p_new(2,ipt3)-p_new(2,ipt4))**2)idel=ip3
	    do i=ibase+idel+1,ibase+ninobj
	      object(i-1)=object(i)
	    enddo
	    ninobj=ninobj-1
	  else
	    iseg=iseg+1
	  endif
	enddo
	return
	end
