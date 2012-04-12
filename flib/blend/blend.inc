	integer maxsiz,limvar,ifastsiz,maxlinelength,limxypc,limnpc,maxbin
	integer limedge,limsect,memlim,limedgbf,ixgdim,iygdim,lmField,maxFields
        integer maxbsiz,idimc,maxPcNear, maxDistNear
	parameter (maxsiz=2600*2600*12)		!# of pixels
	parameter (limvar=3000)                 !# of variables for shift fits
c
c         With default padding (0.45) and aspectmax (2), idimc must be
c         (max overlap * 1.9) * (max overlap * 3.8)
c
	parameter (ifastsiz=32,maxlinelength=100000,maxbin=8,idimc=1520*3040)
        parameter (maxbsiz=(ifastsiz+maxbin)*maxlinelength)
        parameter (maxDistNear = 5)
        parameter (maxPcNear = (2*maxDistNear + 1) * (2*maxDistNear + 1))
	real*4 array(maxsiz), brray(maxbsiz)
        real*8 array8(maxsiz / 2)
        equivalence (array,array8)
	complex*8 xcray(idimc/2), xdray(idimc/2)
	common /bigarr/ array,brray,xcray,xdray
c
	parameter (limxypc=100,limnpc=100000,limedge=100000,limsect=1000)
	integer*4 nxyzin(3),nxyzout(3),nxin,nyin,nzin,nxout,nyout,nzout
	integer*4 nxyzbin(3),nxbin,nybin,nzbin
	integer*4 noverlap(2),nedge(2),nxoverlap,nyoverlap
	integer*4 ixpclist(limnpc),iypclist(limnpc) !piece coords in x,y
	integer*4 izpclist(limnpc),neglist(limnpc) !section #, negative #
	integer*4 iedgelower(limnpc,2),iedgeupper(limnpc,2)
	integer*4 ipiecelower(limedge,2),ipieceupper(limedge,2)
	integer*4 indent(2)			!minimum indent short & long
	integer*4 intgrid(2)			!grid interval short & long
	integer*4 iboxsiz(2)			!box size short & long
	real*4 edgelonear(2),edgehinear(2)	!limits for values near edges
	equivalence (nxin,nxyzin(1))
	equivalence (nxbin,nxyzbin(1))
	equivalence (nxout,nxyzout(1))
	equivalence (noverlap(1),nxoverlap)
	integer*4 npclist,minxpiece,minypiece,nxpieces,nypieces,interpOrder
	real*4 dmean
	common /pclist/nxin,nyin,nzin,nxout,nyout,nzout,nxbin,nybin,nzbin,
     &	    nxpieces,nypieces, nxoverlap,nyoverlap,npclist,minxpiece,
     &	    minypiece,interpOrder, dmean,nedge ,ixpclist,iypclist,
     &	    izpclist,neglist,iedgelower, iedgeupper ,ipiecelower,
     &	    ipieceupper,indent,intgrid,iboxsiz, edgelonear,edgehinear
c
	integer*4 inpiece(5)			!piece # of pieces point in
	integer*4 inedge(5,2)			!edge # of edges point is in
	integer*4 numedges(2)			!number of edges in x and y
	real*4 xinpiece(5),yinpiece(5)		!x,y coordinates within piece
	integer*4 inedlower(5,2),inedupper(5,2)	!list index of piece be&ab edge
	integer*4 mappiece(limxypc,limxypc)	!map of pieces in this section
	logical dogxforms,multng		!if doing g's, if negs in sect
	real*4 hxcen,hycen			!coord of center of input frame
	real*4 gxcen,gycen			!coord of output image center
	real*4 ginv(2,3),hinv(2,3,limnpc)	!inverse of g and h xforms
	integer*4 numpieces
        integer*4 numPcNear, idxPcNear(maxPcNear), idyPcNear(maxPcNear)
	common /edgcnt/ inpiece,inedge,numedges,xinpiece,yinpiece,
     &	    inedlower, inedupper,mappiece,dogxforms,multng,numpieces,
     &	    hxcen,hycen, gxcen,gycen, ginv,hinv
c	  
	parameter (memlim=256)
	integer*4 izmemlist(memlim),lastused(memlim),memIndex(limnpc)
	integer*4 npixin,maxload,jusecount,ilistz
	common /shuffl/ npixin,maxload,jusecount,ilistz,izmemlist,lastused,
     &	    memIndex
c
	parameter (limedgbf=20,ixgdim=256,iygdim=256)
	integer*4 ibufedge(limedge,2),iedgbflist(limedgbf)
     &	    ,ixybflist(limedgbf),lasedguse(limedgbf)
	integer*4 iunedge(2)
	integer*4 nxgrbf(limedgbf),nygrbf(limedgbf)
     &	    ,ixgrdstbf(limedgbf),iygrdstbf(limedgbf)
     &	    ,ixofsbf(limedgbf),iyofsbf(limedgbf)
     &	    ,intxgrbf(limedgbf),intygrbf(limedgbf)
	integer*4 intgrcopy(2)
	real*4 dxgrbf(ixgdim,iygdim,limedgbf)
     &	    ,dygrbf(ixgdim,iygdim,limedgbf)
     &	    ,ddengrbf(ixgdim,iygdim,limedgbf)
	integer*4 jusedgct,needbyteswap
	common /edgebf/ iunedge,jusedgct,needbyteswap,ibufedge,
     &	    iedgbflist, ixybflist,
     &	    lasedguse, nxgrbf ,nygrbf,ixgrdstbf,iygrdstbf, ixofsbf,
     &	    iyofsbf,intxgrbf,intygrbf,dxgrbf,dygrbf ,ddengrbf,intgrcopy
c
	parameter (lmField = 200, maxFields = 16)
	real*4 distDx(lmField,lmField),distDy(lmField,lmField)
	real*4 fieldDx(lmField,lmField,maxFields)
	real*4 fieldDy(lmField,lmField,maxFields)
	logical doFields,undistort,doMagGrad,focusAdjusted, doingEdgeFunc
	real*4 pixelMagGrad, axisRot
	real*4 tiltAngles(limsect), dmagPerUm(limsect), rotPerUm(limsect)
	integer*4 ixFieldStrt, iyFieldStrt, nxField, nyField, numMagGrad
        integer*4 numAngles
	real*4 xFieldIntrv, yFieldIntrv
	common /distrt/ doFields,undistort,doMagGrad,focusAdjusted,
     &	    doingEdgeFunc,pixelMagGrad, axisRot,ixFieldStrt, iyFieldStrt,
     &	    nxField, nyField, numMagGrad,numAngles,xFieldIntrv, yFieldIntrv,
     &	    distDx,distDy,fieldDx,fieldDy,tiltAngles,dmagPerUm,rotPerUm

	integer*4 ifDumpXY(2),nzOutXY(2),ipcBelowEdge
        real*4 taperFrac, padFrac, aspectMax, extraWidth
	real*4 radius1, radius2, sigma1, sigma2
	common /xcparm/ ifDumpXY,nzOutXY, taperFrac, padFrac, aspectMax,
     &	    radius1,radius2,sigma1,sigma2,ipcBelowEdge, extraWidth