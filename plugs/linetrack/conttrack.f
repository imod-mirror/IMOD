	subroutine conttrack(array,nx,ny,p_coord,ninobj,iptcur,maxlen,
     &	    inksize,inkern,sigma,h,ifdark,step,redtol,offset,limshift
     &	    ,inpool,infit)
	parameter (limkern=5,limksize=21,limpath=2000,ndlim=20,limfit=9)
	byte array(nx,ny)
	real*4 p_coord(3,maxlen),p_copy(2,limpath),xt(limfit),yt(limfit)
c
	real*4 orig(3),bkern(limkern*limksize**2)
	data orig/0.,0.,0./
C   
	real*4 theta(limkern),costh(limkern),sinth(limkern)
	real*4 xcen(limkern),ycen(limkern),psum(-ndlim:ndlim)
	integer*4 ixcn(limkern),iycn(limkern)
	integer*4 ixofs(limkern),iyofs(limkern),kbase(limkern)
	real*4 aprod(-ndlim:ndlim,-ndlim:ndlim,limkern)
	logical needprod(-ndlim:ndlim,-ndlim:ndlim,limkern)
	common /linmen/izmean,dmean
C	  
	trackthresh=3.
	closethresh=0.25
	tolcross=1.5*limshift
c
	npool=max(1,min(abs(inpool),limkern))
	nfit=min(infit,limfit)
	iorder=min(2,nfit-2)
	ksize=min(2*inksize+1,limksize)
C	  
	zmodel=p_coord(3,1)
	izmodel=nint(zmodel)
	if(izmean.ne.izmodel)then
	  call edgemean(array,nx,ny,dmean)
	  izmean=izmodel
	endif
	do k=1,limkern
	  kbase(k)=1+ksize**2*(k-1)
	enddo
c	  
c	  
c
	indkcen=npool/2+1
	wdenom=indkcen
	do iptcen=1,ninobj
	  ipdst=-npool/2
	  ipdnd=npool+ipdst-1
	  if(iptcen.ne.1)ipdst=ipdnd
	  do ipdel=ipdst,ipdnd
	    ipt=indmap(iptcen+ipdel,ninobj)
	    indk=indmap(indkcen+ipdel,npool)
	    call makecontkern(bkern(kbase(indk)),ksize,ksize,sigma,h,
     &		ifdark,p_coord,ipt,ninobj,theta(indk),sinth(indk),
     &		costh(indk), ixofs(indk),iyofs(indk))
	    xcen(indk)=p_coord(1,ipt)+orig(1)+0.5
	    ycen(indk)=p_coord(2,ipt)+orig(2)+0.5
c	    print *,'kernel',ipt,theta(indk),sinth(indk),costh(indk)
c	    print *,'origin',xcen(indk),ycen(indk)
	    if(offset.ne.0)then
	      xcen(indk)=xcen(indk)+sinth(indk)*offset
	      ycen(indk)=ycen(indk)-costh(indk)*offset
c	    print *,'offset',xcen(indk),ycen(indk)
	    endif
	    ixcn(indk)=nint(xcen(indk))
	    iycn(indk)=nint(ycen(indk))
	    do i=-ndlim,ndlim
	      do j=-ndlim,ndlim
		needprod(i,j,indk)=.true.
	      enddo
	    enddo
	  enddo
	  thetasrch=goodangle(theta(indkcen)+90.)
	  dxsrch=cosd(thetasrch)
	  dysrch=sind(thetasrch)
	  ndmax=0
	  pmax=-1.e10
	  moved=1
	  do while(moved.eq.1)
	    ndcur=ndmax
	    moved=0
	    do norminc=-1,1
	      nd=ndcur+norminc
	      if(abs(nd).le.limshift)then
		ipdst=-npool/2
		psum(nd)=0.
		do ipdel=ipdst,ipdnd
		  indk=indmap(indkcen+ipdel,npool)
		  xtry=xcen(indk)+nd*dxsrch
		  ytry=ycen(indk)+nd*dysrch
		  ixtry=xtry
		  iytry=ytry
		  do ixc=ixtry,ixtry+1
		    do iyc=iytry,iytry+1
		      ii=ixc-ixcn(indk)
		      jj=iyc-iycn(indk)
		      if(needprod(ii,jj,indk))then
			call kernprod(array,nx,ny,ixc,iyc,
     &			    bkern(kbase(indk)),ksize,ksize,ixofs(indk),
     &			    iyofs(indk),dmean,aprod(ii,jj,indk))
			needprod(ii,jj,indk)=.false.
		      endif
		    enddo
		  enddo
		  fx=xtry-ixtry
		  fy=ytry-iytry
		  idx=ixtry-ixcn(indk)
		  idy=iytry-iycn(indk)
		  prod=((1.-fx)*aprod(idx,idy,indk)+
     &		      fx*aprod(idx+1,idy,indk))*(1.-fy)+
     &		      ((1.-fx)*aprod(idx,idy+1,indk)+
     &		      fx*aprod(idx+1,idy+1,indk))*fy
		  if(inpool.gt.0)then
		    psum(nd)=psum(nd)+(1.-abs(ipdel)/wdenom)*prod
		  else
		    psum(nd)=psum(nd)+prod
		  endif
		enddo
		if(psum(nd).gt.pmax)then
		  moved=1
		  pmax=psum(nd)
		  ndmax=nd
		endif
	      endif
	    enddo
	  enddo
c	  
c	  interpolate offset if not at edge of allowable shifts
c	  
	  cx=0.
	  if(abs(ndmax).lt.limshift)then
	    y1=psum(ndmax-1)
	    y3=psum(ndmax+1)
	    denom=2.*(y1+y3-2.*y2)
	    if(abs(denom).gt.-1.e6)cx=(y1-y3)/denom
	    if(abs(cx).gt.0.5)cx=sign(0.5,cx)
	  endif
	  cxnew=xcen(indkcen)+(ndmax+cx)*dxsrch
	  cynew=ycen(indkcen)+(ndmax+cx)*dysrch
c	  print *,'origin',cxnew,cynew
	  if(offset.ne.0)then
	    cxnew=cxnew-sinth(indkcen)*offset
	    cynew=cynew+costh(indkcen)*offset
c	  print *,'offset',cxnew,cynew
	  endif
	  p_copy(1,iptcen)=cxnew-orig(1)-0.5
	  p_copy(2,iptcen)=cynew-orig(2)-0.5
c
	  indkcen=indmap(indkcen+1,npool)
	enddo
c	  
c	  eliminate close points
c
	closesq=(step*closethresh)**2
	iseg=1
	idel=0
	do while(iseg.le.ninobj)
	  ip3=indmap(iseg+1,ninobj)
	  if((p_copy(1,ip3)-p_copy(1,iseg))**2+
     &	      (p_copy(2,ip3)-p_copy(2,iseg))**2 .lt. closesq)then
c	    print *,'eliminating segment',iseg,' of',ninobj
	    ip1=indmap(iseg-1,ninobj)
	    ip4=indmap(iseg+2,ninobj)
	    idel=iseg
	    if((p_copy(1,ip1)-p_copy(1,iseg))**2+
     &		(p_copy(2,ip1)-p_copy(2,iseg))**2 .gt.
     &		(p_copy(1,ip3)-p_copy(1,ip4))**2+
     &		(p_copy(2,ip3)-p_copy(2,ip4))**2)idel=ip3
	    do i=idel+1,ninobj
	      p_copy(1,i-1)=p_copy(1,i)
	      p_copy(2,i-1)=p_copy(2,i)
	    enddo
	    ninobj=ninobj-1
	  else
	    iseg=iseg+1
	  endif
	enddo
	do i=1,ninobj
	  p_coord(1,i)=p_copy(1,i)
	  p_coord(2,i)=p_copy(2,i)
	enddo
c	  
c	  track between points that are far apart
c
	threshsq=(step*trackthresh)**2
	iptcen=1
	ifadd=0
	do while (iptcen.lt.ninobj)
	  if((p_coord(1,iptcen)-p_coord(1,iptcen+1))**2+
     &	      (p_coord(2,iptcen)-p_coord(2,iptcen+1))**2 .ge.threshsq)
     &	      then
	    ninold=ninobj
	    call linetrack(array,nx,ny,p_coord,ninobj,iptcen,maxlen,
     &		inksize,inkern,sigma,h,ifdark,step,redtol,0,offset,
     &		0,iffail)
	    if(ninobj.gt.ninold)ifadd=1
	  endif
	  iptcen=iptcen+1
	enddo
c	  
c	  smoothing next
c	    
	do i=1,ninobj
	  p_copy(1,i)=p_coord(1,i)
	  p_copy(2,i)=p_coord(2,i)
	enddo
	do iptcen=1,ninobj
	  xmid=p_copy(1,iptcen)
	  ymid=p_copy(2,iptcen)
	  if(iorder.gt.0.and.nfit.gt.iorder.and.nfit.le.ninobj)then
	    do i=1,nfit
	      ipt=indmap(iptcen+i-1-nfit/2,ninobj)
	      xt(i)=p_copy(1,ipt)
	      yt(i)=p_copy(2,ipt)
	    enddo
	    dx=xt(nfit)-xt(1)
	    dy=yt(nfit)-yt(1)
	    dlen=sqrt(dx**2+dy**2)
	    costs=dx/dlen
	    sints=-dy/dlen
	    do i=1,nfit
	      xtmp=xt(i)-xmid
	      xt(i)=costs*xtmp-sints*(yt(i)-ymid)
	      yt(i)=sints*xtmp+costs*(yt(i)-ymid)
	    enddo
	    if(iorder.eq.1)then
	      call lsfit(xt,yt,nfit,slop1,bint,ro)
	    else
	      call quadfit(xt,yt,nfit,slop2,slop1,bint)
	    endif
	    xmid=sints*bint+xmid
	    ymid=costs*bint+ymid
c	    print *,nfit,iptcen,p_copy(1,iptcen),p_copy(2,iptcen),bint,xmid,ymid
	  endif
	  p_coord(1,iptcen)=xmid
	  p_coord(2,iptcen)=ymid
	enddo
c	  
c	  fix up points that are crossed
c	  
	do ipt=1,ninobj 
	  call uncrosscont(p_coord,ninobj,ipt,tolcross)
	enddo
	if(ifadd.ne.0.or.idel.ne.0)iptcur=ninobj-1
	return
	end



	subroutine makecontkern(array,nx,ny,sigma,h, ifdark,p_copy,ipt,
     &	    ninobj,theta, sinth,costh,ixofs,iyofs)
	real*4 array(nx,ny),p_copy(3,*)
	parameter (limrot=25)
	real*4 xt(-limrot:limrot),yt(-limrot:limrot)
	real*4 patht(-limrot:limrot)
	integer*4 irlim(-1:1)
c	  
	xcen=(nx+1)/2.
	ycen=(ny+1)/2.
	const=(0.75/h)
	if(ifdark.eq.0)const=-const
	ipnex=indmap(ipt+1,ninobj)
	iplas=indmap(ipt-1,ninobj)
	dx=p_copy(1,ipnex)-p_copy(1,iplas)
	dy=p_copy(2,ipnex)-p_copy(2,iplas)
	dlen=sqrt(dx**2+dy**2)
	sinth=dy/dlen
	costh=dx/dlen
	theta=atan2d(dy,dx)
	xmid=p_copy(1,ipt)
	ymid=p_copy(2,ipt)
	xt(0)=0.
	yt(0)=0.
	patht(0)=0.
	do idir=-1,1,2
	  irot=0
	  ifterm=0
	  iptr=ipt
	  pathlen=0.
	  do while (abs(irot).lt.limrot.and.ifterm.eq.0)
	    iptr=indmap(iptr+idir,ninobj)
	    xr=costh*(p_copy(1,iptr)-xmid)+sinth*(p_copy(2,iptr)-ymid)
	    yr=-sinth*(p_copy(1,iptr)-xmid)+costh*(p_copy(2,iptr)-ymid)
	    if(idir*xr.le.idir*xt(irot))then
	      ifterm=1
	    else
	      pathlen=pathlen+sqrt((xr-xt(irot))**2+(yr-yt(irot))**2)
	      irot=irot+idir
	      xt(irot)=xr
	      yt(irot)=yr
	      patht(irot)=pathlen
	      if(abs(xr).gt.xcen)ifterm=1
	    endif
	  enddo
	  irlim(idir)=irot
	enddo
c
	minx=nx
	miny=ny
	maxx=0
	maxy=0
	do iy=1,ny
	  do ix=1,nx
	    xx=ix-xcen
	    yy=iy-ycen
	    xr=xx*costh+yy*sinth
	    yr=-xx*sinth+yy*costh
	    idir=nint(sign(1.,xr))
	    irot=0
	    ifgot=0
	    do while(irot.ne.irlim(idir).and.ifgot.eq.0)
	      if(idir*xt(irot+idir).gt.idir*xr)then
		ifgot=1
		yint=yt(irot)+(xr-xt(irot))*(yt(irot+idir)-yt(irot))/
     &		    (xt(irot+idir)-xt(irot))
		ydiff=yr-yint
		pathlen=patht(irot)+
     &		    sqrt((xr-xt(irot))**2+(yr-yt(irot))**2)
	      endif
	      irot=irot+idir
	    enddo
	    if(abs(pathlen).lt.h.and.ifgot.eq.1)then
	      yrat=ydiff**2/sigma**2
	      array(ix,iy)=
     &		  const*(1.-pathlen**2/h**2)*(yrat-1.)*exp(-yrat/2.)
	      minx=min(minx,ix)
	      maxx=max(maxx,ix)
	      miny=min(miny,iy)
	      maxy=max(maxy,iy)
	    else
	      array(ix,iy)=0.
	    endif
	  enddo
	enddo
c	    
c	  get offsets from min and max nonzero pixels
c	  
	ixofs=min(minx-1,nx-maxx)
	iyofs=min(miny-1,ny-maxy)
c
c	    adjust to zero mean
c
	sum=0.
	npix=0
	do iy=1+iyofs,ny-iyofs
	  do ix=1+ixofs,nx-ixofs
	    sum=sum+array(ix,iy)
	    npix=npix+1
	  enddo
	enddo
	dofs=sum/npix
	do iy=1+iyofs,ny-iyofs
	  do ix=1+ixofs,nx-ixofs
	    array(ix,iy)=array(ix,iy)-dofs
	  enddo
	enddo
	return
	END
