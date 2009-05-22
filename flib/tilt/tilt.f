C       -----------------------------------------------------------------
C       
C       TILT......A program for reconstructing a three-dimensional object
C       from a series of two-dimensional projections.
C       The projections are assumed to arise from rotation about
C       a fixed tilt axis.
c       
c       See man page for details
C       
c       $Id$
c       Log at end of file
c       
      implicit none
      include 'tilt.inc'
      integer*4 nxyztmp(3),nxyzst(3)
      data nxyzst/0.,0.,0./
      character*20 radtxt1/'Radial weighting'/
      character*18 radtxt2/'   function'/
c       
      integer*4 interhsave,nsliceout,memBigCrit,memBigOutLim,nslice,nprj2
      integer*4 inloadstr,inloadend,lastready,lastcalc,idir,nextfreevs
      integer*4 lvsstart,lvsend,nvsinring,nplanes,ni,loadlimit,LSLICEout
      integer*4 lsstart,lsend,lsmin,lsmax,lslice,needstart,needend,itryend
      integer*4 itry,ifenough,laststart,lastend,ixleft,nxassay,minslice,i
      integer*4 maxslice,iassay,ixsam,iv,iy,ind1,ind2,ind3,ind4,iyp,nalready
      real*4 dmin,dmax,ycenfix,abssal,tanalpha,dxassay,dxtmp,xx,yy,zz
      real*4 xp,yp,xp2,yp2,xp3,yp3,xp4,yp4,f1,f2,f3,f4,valmin,xsum,stmean
      integer*4 ibase,lstart,nv,ISTART,NL,iyload,nsum,ix,ipad,ioffset
      integer*4 iringstart,mode,ixassay
      real*4 endmean,f,unscmin,unscmax,recscale,recflevl,DMEAN,pixelTot
      real*4 xprojf, xprojz, yprojf, yprojz, xproj, yproj
      real*8 dtot8

      TMASK = -1.E+30
      maxSTACK=limstack
      interhsave=20
      nsliceout=0
      memBigCrit = 20000000
      memBigOutLim = 10
      DTOT8=0.
      DMIN=1.E30
      DMAX=-1.E30
      debug = .false.
C       
C       Open files and read control data
      CALL INPUT
      NSLICE=(JSLICE-ISLICE)/idelslice+1
      valmin = 1.e-3 * (pmax - pmin)
      edgeFill = pmean
      if (iflog .ne. 0) edgeFill = alog10(max(valmin, pmean + baselog))
      edgeFill = edgeFill * zeroWeight
      
      if (debug)  print *,'iflog=',iflog,' scale=',scale,'  edgefill=',edgefill
c       
c       recompute items not in common
c       
      if(fastbp)then
        nprj2=nprj+npad
        radtxt1='Fast back projection'
        radtxt2='   working storage'
      else
        NPRJ2=NPRJ+2+npad
      endif
c       
c       initialize variables for loaded slices and for ring buffer of
c       vertical slices
c       
      inloadstr=0
      inloadend=0
      lastready=0
      lastcalc=0
      idir=sign(1,idelslice)
      nextfreevs=1
      lvsstart=-1
      lvsend=-1
      nvsinring=0
      ycenfix=ycen
      abssal=abs(sal(1))
C       
C       Calculate and report stack loading
c       
      NPLANES=(maxSTACK-NBASE-ipextra+1)/IPLANE
      if(ifalpha.eq.0.and.nxwarp.eq.0)then
        NPLANES=MIN(NPLANES,NSLICE)
      else
        NPLANES=MIN(NPLANES,MPRJ)
      endif       
      NI=NPLANES*IPLANE
      if (.not. recReproj) then
        WRITE(6,900)maxSTACK,radtxt1,imap-1,radtxt2,ITHWID
        if(ifalpha.lt.0)write(6,902)nvertneed,nvertneed*ithick*iwide
        write(6,903)NPLANES,NI
        if (ipextra.ne.0)write(6,901)ipextra
        call maskprep
      endif
      if (debug) print *,'slicen',slicen,', imap',imap,', nbase',nbase
      if(ifalpha.ge.0)then
        loadlimit=jslice
      else
        if(idir.gt.0)then
          loadlimit=slicen+(jslice-slicen)*cal(1)+yoffset*sal(1)+
     &        0.5*ithickout*abssal+2.
        else
          loadlimit=slicen+(jslice-slicen)*cal(1)+yoffset*sal(1)-
     &        0.5*ithickout*abssal-1.
        endif
      endif
C       
C       Main loop over slices perpendicular to tilt axis
C       ------------------------------------------------
      LSLICEout=ISLICE
      DO while (idir * LSLICEout .le. idir * JSLICE)
c         
c         get limits for slices that are needed: the slice itself for regular
c         work, or required vertical slices for new-style X tilting
c         
        if (debug) print *,'working on',lsliceout
        if(ifalpha.ge.0)then
          lsstart=lsliceout
          lsend=lsliceout
        else
          tanalpha=sal(1)/cal(1)
          lsmin=slicen+(lsliceout-slicen)*cal(1)+yoffset*sal(1)-
     &        0.5*ithickout*abssal-1.
          lsmax=slicen+(lsliceout-slicen)*cal(1)+yoffset*sal(1)+
     &        0.5*ithickout*abssal+2.
          if (debug) print *,'need slices',lsmin,lsmax
          lsmin=max(1,lsmin)
          lsmax=min(mprj,lsmax)
          if(idir.gt.0)then
            lsstart=lsmin
            if(lsmin.ge.lvsstart.and.lsmin.le.lvsend)lsstart=lvsend+1
            lsend=lsmax
          else
            lsstart=lsmax
            if(lsmax.le.lvsstart.and.lsmax.ge.lvsend)lsstart=lvsend-1
            lsend=lsmin
          endif
        endif
c         
c         loop on needed vertical slices
c         
        if (debug) print *,'looping to get',lsstart,lsend
        
        do lslice = lsstart, lsend, idir
C           
C           Load stack with as many lines from projections as will
C           fit into the remaining space. The stack is constructed as
C           follows: 
C           (Radial weighting function or FBP working storage)--->
C           (current output plane) --->
C           (vertical reconstructed planes for new X-axis tilting) --->
C           (first slice,first view)....(first slice,last view)--->
C           ..................................................--->
C           (last slice,first view )....(last slice,last view)
C           (Extra space for another set of slices, if cosine stretching)
C           After each view two additional elements are inserted for
C           the transform. 
C...............don't load too many slices 
C           ..not more than NPLANES..not beyond eof.
c           
c           Enter loading procedures unless the load is already set for the
c           current slice
c           
          if(inloadstr.eq.0.or.idir*lslice.gt.idir*lastready)then
            needstart=0
            needend=0
            itryend=0
            lastready=0
            itry=lslice
            ifenough=0
c             
c             loop on successive output slices to find what input slices are
c             needed for them; until all slices would be loaded or there
c             would be no more room
c             or until the big memory criterion is exceeded and the limit
c             on number of output slices for that case is reached
            do while(itry.gt.0.and.itry.le.mprj.and.ifenough.eq.0.and.
     &          idir*itry.le.idir*loadlimit.and.
     &          idir*(itryend-needstart)+1.le.nplanes .and.
     &          (nbase + ipextra + idir*(itryend-needstart) * iplane .le.
     &          memBigCrit .or. idir *(itry - lslice) .le. memBigOutLim))
              if(ifalpha.le.0.and.nxwarp.eq.0)then
c                 
c                 regular case is simple: just need the current slice
c                 
                laststart=itry
                lastend=itry
              elseif(itry.ne.lastcalc)then
c                 
c                 for old-style X-tilt or local alignment, determine what
c                 slices are needed by sampling 
c                 set up sample points: left and right if no warp,
c                 or half the warp spacing 
c                 
                if(nxwarp.eq.0)then
                  nxassay=2
                  dxassay=iwide-1
                else
                  dxtmp=idxwarp/2
                  nxassay=max(2.,iwide/dxtmp+1.)
                  dxassay=(iwide-1.)/(nxassay-1.)
                endif
c                   
c                 sample top and bottom at each position
c                 
                minslice=mprj+1
                maxslice=0
                do iassay=1,nxassay
                  ixassay = nint(1+(iassay-1)*dxassay)
                  do iv=1,nviews
                    if (.not. recReproj) then
                      ixsam=nint(ixassay-xcen+xcenin+delxx)
                      if(nxwarp.ne.0) then
                        call localProjFactors(ixassay, itry, iv, xprojf,
     &                      xprojz, yprojf, yprojz)
                      endif
                      do iy=1,ithick,ithick-1
c                         
c                         for each position, find back-projection location
c                         transform if necessary, and use to get min and
c                         max slices needed to get this position
c                         
                        xx=ixsam-xcen
                        yy=itry-slicen
                        zz=iy-ycen
                        xp=xx*cbet(iv)+yy*sal(iv)*sbet(iv)+
     &                      zz*(cal(iv)*sbet(iv)+xzfac(iv))+ xcenin+delxx
                        yp=yy*cal(iv)-zz*(sal(iv)-yzfac(iv))+slicen
                        if(nxwarp.ne.0)then
                          xp = xprojf + xprojz * zz
                          yp = yprojf + yprojz * zz
                        endif
                        iyp=max(1.,yp)
                        minslice=min(minslice,iyp)
                        maxslice=max(maxslice,min(mprj,iyp+1))
c                        if (debug) print *,xx,yy,zz,iyp,minslice,maxslice
                      enddo
                    else
c                       
c                       Projections: get Y coordinate in original projection
c                       if local, get the X coordinate in reconstruction too
c                       then get the refinement 
                      xproj = ixassay + xprjOffset
                      yproj = itry + yprjOffset
                      do iy=1,ithickReproj,ithickReproj-1
                        zz = iy + minYreproj - 1 - ycen
                        yy = (yproj + zz*(sal(iv)-yzfac(iv))-slicen) / cal(iv)
     &                      + slicen
                        if (nxwarp.ne.0) then
                          xx = (xproj - yy*sal(iv)*sbet(iv) - zz * (cal(iv)*
     &                        sbet(iv) +xzfac(iv)) - xcenin-delxx) / cbet(iv)
     &                        + xcen
                          call findProjectingPoint(xproj, yproj, zz, iv, xx,
     &                        yy)
                        endif
                        iyp = max(1., yy - yprjOffset)
                        minslice=min(minslice,iyp)
                        maxslice=max(maxslice,min(mprj,iyp+1))
                      enddo
                    endif                      
                  enddo
                enddo
c                 
c                 set up starts and ends as appropriate for direction
c                 
                lastcalc=itry
                if(idir.gt.0)then
                  laststart=minslice
                  lastend=maxslice
                else
                  laststart=maxslice
                  lastend=minslice
                endif
              endif
c               
c               values here must work in single-slice case too
c               if this is the first time, set needstart
c               if this load still fits, set needend and lastready
c               
              itryend=lastend
              if(needstart.eq.0)needstart=laststart
              if(idir*(itryend-needstart)+1.le.nplanes)then
                needend=itryend
                lastready=itry
              else
                ifenough=1
              endif
              itry=itry+idelslice
            enddo
            if (debug) print *,'itryend,needstart,needend,lastready',itryend,
     &           needstart,needend,lastready
            if(needend.eq.0) call exitError
     &          ('INSUFFICIENT STACK SPACE TO DO A SINGLE SLICE')
c             
c             if some are already loaded, need to shift them down
c             
            nalready=max(0,idir*(inloadend-needstart)+1)
            if(inloadend.eq.0)nalready=0
            ioffset=idir*(needstart-inloadstr)*iplane
            do i=nbase,nbase+nalready*iplane-1
              array(i)=array(i+ioffset)
            enddo
            if(nalready.ne.0.and.debug)print *,'shifting',needstart,inloadend
c             
c             load the planes in one plane high if stretching
c             
            IBASE=NBASE+nalready*iplane+ipextra
            lstart=needstart+idir*nalready
C             
            if (debug) print *,'loading',lstart,needend
            if (.not. recReproj) then
              DO NV=1,NVIEWS
                ISTART=IBASE
                DO NL=lstart,needend,idelslice
C                   Position to read from projection NV at record NL
                  iyload=max(0,min(mprj-1,nl-1))
                  CALL IMPOSN(1,mapuse(NV)-1,iyload)
                  CALL IRDLIN(1,ARRAY(ISTART),*999)
c                   Take log if requested
c                   3/31/04: limit values to .001 time dynamic range
                  if(iflog.ne.0)then
                    do ix=istart,istart+nprj-1
                      array(ix)=alog10(max(valmin,array(ix)+baselog))
                    enddo
                  else
                    do ix=istart,istart+nprj-1
                      array(ix) = array(ix) * expWeight(nv)
                    enddo
                  endif
c                   
                  if(.not.fastbp)then
c                     
c                     pad with taper between start and end of line
c                     
                    nsum=0
                    xsum=0.
                    do ix=istart,istart+min(2,nprj-1)
                      nsum=nsum+1
                      xsum=xsum+array(ix)
                    enddo
                    stmean=xsum/nsum
                    if(nsum.eq.0)print *,'stmean bogus'
                    nsum=0
                    xsum=0.
                    do ix=istart+max(0,nprj-3),istart+nprj-1
                      nsum=nsum+1
                      xsum=xsum+array(ix)
                    enddo
                    if(nsum.eq.0)print *,'ndmean bogus'
                    endmean=xsum/nsum
                    do ipad=1,npad
                      f=ipad/(npad+1.)
                      array(istart+nprj+ipad-1)=f*stmean+(1.-f)*endmean
                    enddo
                  endif
c                   
                  ISTART=ISTART+IPLANE
                enddo
                IBASE=IBASE+NPRJ2
              enddo
c               
            else
c               
c               Load reconstruction for projections
              DO NL=lstart,needend,idelslice
                call imposn(1, nl - 1, 0)
                call irdpas(1, array(ibase), maxXload + 1 - minXload,
     &              ithickReproj, minXload - 1, maxXload - 1, minYreproj - 1,
     &              maxYreproj - 1, *999)
c                 
c                 Undo the scaling that was used to write, and apply a
c                 scaling that will make the data close for projecting
                do i = 0, ithwid - 1
                  array(i + ibase) = (array(i + ibase) / scale - flevl) /
     &                filterScale
                enddo
                ibase = ibase + iplane
              enddo
            endif
            if(.not.fastbp .and.  .not.recReproj)then
              IBASE=NBASE+nalready*iplane
              DO  NL=lstart,needend,idelslice
                call transform(ibase)
                ibase=ibase+iplane
              enddo
            endif
            inloadstr=needstart
            inloadend=needend
            nstack=nbase+iplane*((inloadend-inloadstr)/idelslice+1)-1
          END IF
C           
C           Stack is full.
c           
          ISTART=NBASE+IPLANE*(lslice-inloadstr)/idelslice
          if (.not.recReproj) then
C             Process all views for current slice
            
C             Zero array and set background mask if required
            CALL ZEROM
c           
c             If new-style X tilt, set  the Y center based on the slice
c             number, and adjust the y offset slightly
c             
            if(ifalpha.lt.0)ycen=ycenfix+(cal(1)-1.)*yoffset
     &          -nint(tanalpha*(lslice-slicen))
c             
c             print *,nbase,lslice,inloadstr
c             print *,'projecting',lslice,' at',istart,', ycen =',ycen
            CALL PROJECT(ISTART,lslice)
c             
c             move vertical slice into ring buffer, adjust ring variables
c             
            if(ifalpha.lt.0)then
c               print *,'moving slice to ring position',nextfreevs
              ioffset=ithwid+(nextfreevs-1)*ithick*iwide
              do i=imap,imap+ithick*iwide-1
                array(i+ioffset)=array(i)
              enddo
              if(nvsinring.lt.nvertneed)then
                if(nvsinring.eq.0)lvsstart=lslice
                nvsinring=nvsinring+1
              else
                lvsstart=lvsstart+idir
              endif
              lvsend=lslice
              nextfreevs=nextfreevs+1
              if(nextfreevs.gt.nvertneed)nextfreevs=1
            endif
          endif
        enddo
c         
        if (.not.recReproj) then
          if(ifalpha.lt.0)then
c             
c             interpolate output slice from vertical slices
c             
            iringstart=1
            if(nvsinring.eq.nvertneed)iringstart=nextfreevs
c             print *,'composing',lsliceout,' from',lvsstart,lvsend,iringstart
            call compose(lsliceout,lvsstart,lvsend,idir,iringstart)
          endif
C           
C           Write out current slice
          CALL DUMP(LSLICEout,DMIN,DMAX,DTOT8)
c           DNM 10/22/03:  Can't use flush in Windows/Intel because of sample.com
c           call flush(6)
c           
c           write out header periodically, restore writing position
c           
          if(perp.and.interhsave.gt.0.and..not.reproj .and.
     &        minTotSlice.le.0)then
            nsliceout=nsliceout+1
            nxyztmp(1)=iwide
            nxyztmp(2)=ithickout
            nxyztmp(3)=nsliceout
            if(mod(nsliceout,interhsave).eq.1)then 
              call ialsiz(2,nxyztmp,nxyzst)
              DMEAN=DTOT8/(float(NSLICEout)*IWIDE*ITHICK)
              CALL IWRHDR(2,TITLE,-1,DMIN,DMAX,DMEAN)
              call parWrtPosn(2,nsliceout,0)
            endif
          endif
          lsliceOut = lsliceOut + idelslice
        else
c           
c           Reproject all ready slices to minimize file mangling
          call reprojectRec(lsliceOut, lastReady, inloadstr, inloadend, DMIN,
     &        DMAX,DTOT8)
          lsliceOut = lastReady + 1
        endif
      enddo
C       
C       End of main loop
C-----------------
C       
C       Close files
      CALL IMCLOSE(1)
      pixelTot = float(NSLICE)*IWIDE*ITHICK
      if (reproj.or.recReproj) pixelTot = float(NSLICE)*IWIDE*nreproj
      DMEAN=DTOT8/pixelTot
      if (minTotSlice.le.0) then
        if(perp.and.interhsave.gt.0.and..not.(reproj.or.recReproj))then
          nxyztmp(3)=nslice
          call ialsiz(2,nxyztmp,nxyzst)
        endif
        CALL IWRHDR(2,TITLE,1,DMIN,DMAX,DMEAN)
        if (.not.(reproj.or.recReproj)) then
          WRITE(6,930)'reconstruction'
        else
          WRITE(6,930)'reprojection'
        endif
        CALL IRDHDR(2,nxyztmp,nxyzst,MODE,DMIN,DMAX,DMEAN)
      else
        write(*,'(a,3g15.7,f15.0)')'Min, max, mean, # pixels=',dmin,dmax,dmean,
     &      pixelTot
      endif
      CALL IMCLOSE(2)
      if(fastbp)then
        scale=scale/1000.
        flevl=flevl/1000.
      endif
      if (.not.(reproj.or.recReproj)) then
        unscmin=dmin/scale-flevl
        unscmax=dmax/scale-flevl
        recscale=nviews*nreplic*235./(unscmax-unscmin)
        recflevl=(10.*(unscmax-unscmin)/235.-unscmin)/(nviews*nreplic)
        write(6,905)recflevl,recscale
        recscale=nviews*nreplic*30000./(unscmax-unscmin)
        recflevl=(-15000.*(unscmax-unscmin)/30000.-unscmin)/(nviews*nreplic)
        write(6,906)recflevl,recscale
        WRITE(6,910)NSLICE
      endif
      call exit(0)
999   WRITE(6,920)mapuse(NV),nL
      call exit(1)
C       
C       
900   FORMAT(//' STACK LOADING'
     &    /' -------------'
     &    //' Total stack size           ',I11/
     &    /,1x,a20,'         ',I9/,a,//,
     &    ' Output slice                 ',I9,/)
901   format(' Stretching buffer            ',I9,/)
902   format(1x,i4,  ' Untilted slices         ',I9,/)
903   format(1X,I4,' Transposed projections  ',I9,/)
905   format(//' To scale output to bytes (10-245), use SCALE',f12.3, f12.5)
906   format(//' To scale output to -15000 to 15000, use SCALE',f12.3, f12.5)
910   FORMAT(//' Reconstruction of',I5,' slices complete.',
     &    //,1X,78('-'))
920   FORMAT(//' ERROR: TILT -  reading in view',I3,' for slice'
     &    ,I5,/)
930   FORMAT(//' Header on ',a,' file'/
     &    ' --------------------------------'//)
      END


C       --------------------------------------------------------------------
      SUBROUTINE RADWT(IRMAXin,IFALLin)
C       -----------------------------
C       
C       Set Radial Transform weighting
C       Linear ramp plus Gaussian fall off
      implicit none
      include 'tilt.inc'
      integer*4 IRMAXin,IFALLin
      integer*4 nweight, numWgtAngles
      real*4 wincr(20), wgtAngles(limview)
      COMMON /DENSWT/nweight,wincr,numWgtAngles,wgtAngles
      integer*4 nprj2,IEND,irmax,ifall,iv,iw,ibase,i,impbase
      real*4 stretch,avgint,atten,sumint,wsum,z,arg
      real*4 diffmin, diff, attensum,wgtAtten(limview)
      
c       
      nprj2=nprj+2+npad
      IEND=NPRJ2/2
      stretch=float(nprj+npad)/nprj
      irmax=nint(irmaxin*stretch)
      ifall=nint(ifallin*stretch)
      avgint = 1.
      attensum = 0.
      zeroWeight = 0.
      if(nweight.gt.0 .and. numWgtAngles.gt.1)then
        avgint=(wgtAngles(numWgtAngles)-wgtAngles(1))/(numWgtAngles-1)
        if (debug) write(6,401)
401      format(/' View  Angle Weighting')
      endif
c       
c       Set up the attenuations for the weighting angles
      do iv=1,numWgtAngles
        atten=1.
        if(nweight.gt.0. .and. numWgtAngles.gt.1)then
          sumint=0
          wsum=0.
          do iw=1,nweight
            if(iv-iw.gt.0)then
              wsum=wsum+wincr(iw)
              sumint=sumint+wincr(iw)*(wgtAngles(iv+1-iw)-
     &            wgtAngles(iv-iw))
            endif
            if(iv+iw.le.nviews)then
              wsum=wsum+wincr(iw)
              sumint=sumint+wincr(iw)*(wgtAngles(iv+iw)-
     &            wgtAngles(iv+iw-1))
            endif
          enddo
          atten=atten*(sumint/wsum)/avgint
        endif
        wgtAtten(iv) = atten
      enddo
C       
C       Set up linear ramp
      do iv = 1, nviews
c         
c         Get weighting from nearest weighting angle
        atten = 1.
        if(nweight.gt.0 .and. numWgtAngles.gt.1)then
          diffmin = 1.e10
          do iw = 1, numWgtAngles
            diff = abs(angles(iv) - wgtAngles(iw))
            if (diff .lt. diffmin) then
              diffmin = diff
              atten = wgtAtten(iw)
            endif
          enddo
          if (debug) write(6,402)iv,angles(iv),atten
402       format(i4,f8.2,f10.5)
        endif
c         
c         Take negative if subtracting
        if (nViewSubtract .gt. 0) then
          do i = 1, nViewSubtract
            if (ivSubtract(i) .eq. 0 .or. mapuse(iv) .eq. ivSubtract(i))
     &          atten = -atten
          enddo
        endif
c
        attensum = attensum + atten
        ibase=(iv-1)*nprj2
        DO  I=1,min(IRMAX,iend)
c           This was the basic filter
c           ARRAY(ibase+2*I-1)=atten*(I-1)
c           This is the mixture of the basic filter and a flat filter with
c           a scaling that would give approximately the same output magnitude 
          ARRAY(ibase+2*I-1)=atten*((1.-flatFrac)*(I-1) + flatFrac*filterScale)
c           
c           This 0.2 is what Kak and Slaney's weighting function gives at 0
          if (i.eq.1) ARRAY(ibase+2*I-1) = atten*0.2
c           
c           This is the SIRT filter, which divides the error equally among
c           the pixels on a ray.
          if (flatFrac .gt. 1) ARRAY(ibase+2*I-1) = atten * filterScale /
     &        (ithick / cbet(iv))
c           
c           And just the value and compute the mean zero weighting
          ARRAY(ibase+2*I)=ARRAY(ibase+2*I-1)
          if (i .eq. 1) zeroWeight = zeroWeight + ARRAY(ibase+2*I-1) / nviews
        enddo
      enddo
      if (debug) print *,'Mean weighting factor', attensum/nviews
C       
C       Set up Gaussian
      DO I=IRMAX+1,IEND
        ARG=FLOAT(I-IRMAX)/FLOAT(IFALL)
        atten=EXP(-ARG*ARG)
        ibase=0
        do iv=1,nviews
          Z=atten*array(ibase+2*irmax)
          ARRAY(ibase+2*I-1)=z
          ARRAY(ibase+2*I)=Z
          ibase=ibase+nprj2
        enddo
      enddo
      RETURN
      END
C       
C       
C       ---------------------------------------------------------------------
      SUBROUTINE MASKPREP
C       ----------------
C       
C       This subroutine prepares the limits of the slice width to be computed
c       if masking is used
      implicit none
      include 'tilt.inc'
      real*4 radlft,radrt,y, yy
      integer*4 i, ixlft, ixrt
C       
C       Compute left and right edges of unmasked area
      IF(MASK)THEN
        radlft=(nprj/2+delxx)**2
        radrt=(nprj/2-delxx)**2
        DO I=1,ITHICK
          Y=I-YCEN
          YY=min(Y*Y,radlft)
          ixlft=xcen+1.-sqrt(radlft-yy)
          masklft(i)=max(1,ixlft)
          YY=min(Y*Y,radrt)
          ixrt=xcen+sqrt(radrt-yy)
          maskrt(i)=min(iwide,ixrt)
        enddo
C-------------------------------------------------
C         If no mask
      ELSE
        DO I=1,ITHICK
          masklft(i)=1
          maskrt(i)=iwide
        enddo
      END IF
      RETURN
      END
C       
C       
C       ---------------------------------------------------------------------
      SUBROUTINE ZEROM
C       ----------------
C       
C       This subroutine zeros the slice and applies the mask if requested
      implicit none
      include 'tilt.inc'
      integer*4 index, i, j,iend
c       
c       Read in base slice and descale
      if (readBase) then
        call irdsec(3, array(imap))
        do i = imap, imap + ithick * iwide - 1
          array(i) = array(i) / baseScale - baseFlevl
        enddo
C       
C       Zero and apply mask
      ELSE IF(MASK)THEN
        INDEX=IMAP
        DO I=1,ITHICK
          DO J=1,masklft(i)-1
            ARRAY(INDEX)=TMASK
            INDEX=INDEX+1
          enddo
          do j=masklft(i),maskrt(i)
            ARRAY(INDEX)=0.
            INDEX=INDEX+1
          enddo
          DO J=maskrt(i)+1,iwide
            ARRAY(INDEX)=TMASK
            INDEX=INDEX+1
          enddo
        enddo
C-------------------------------------------------
C         Zero only
      ELSE
        IEND=IMAP+ITHWID-1
        DO I=IMAP,iend
          ARRAY(I)=0.   
        enddo
      END IF
      RETURN
      END


C       ---------------------------------------------------------------------
      SUBROUTINE TRANSFORM(ibase)
C       ----------------------------
C       
C       This subroutine applies a one-dimensional Fourier transform to
C       all views corresponding to a given slice, applies the radial 
C       weighting function and then applies an inverse Fourier transform.
C       
      implicit none
      include 'tilt.inc'
      integer*4 nprj2, istart, index, indrad, nv, i, ibfrom, ibto, ixp
      integer*4 ixpp1, ixpm1, ixpp2, ibase
      real*4 x, xp, dx, dxm1, v4, v5, v6, a, c, dennew, dxdxm1, diffmax
      real*4 fx1, fx2, fx3, fx4

      NPRJ2=NPRJ+2+npad
      istart=ibase+ipextra
C       
C       Apply forward Fourier transform
      CALL ODFFT(ARRAY(ISTART),NPRJ+npad,NVIEWS,0)      
C       
C       Apply Radial weighting
      INDEX=ISTART
      indrad=1
      DO  NV=1,NVIEWS
        DO I=1,NPRJ2
          ARRAY(INDEX)=ARRAY(INDEX)*ARRAY(indrad)
          indrad=indrad+1
          INDEX=INDEX+1
        enddo
      enddo
C       
C       Apply inverse transform
      CALL ODFFT(ARRAY(ISTART),NPRJ+npad,NVIEWS,1)
      if(ipextra.eq.0)return
c       
c       do cosine stretch and move down one plane
c       Use cubic interpolation a la cubinterp
c       
c       print *,'istart, ibase', istart,ibase
      do nv=1,nviews
        ibfrom=istart+(nv-1)*nprj2-1
        ibto=ibase+indstretch(nv)-1
c         print *,nv,ibfrom,ibto,nstretch(nv)
        diffmax=0.
        if(interpord.eq.1)then
c           
c           linear interpolation
c           
          do i=1,nstretch(nv)
            x=i/float(interpfac)+ofstretch(nv)
            xp=min(max(1.,x*cbet(nv)), float(nprj))
            IXP = XP
            DX = XP - IXP
            ixp = ixp + ibfrom
            IXPP1 = min(IXP + 1, nprj +ibfrom)
            dxm1 = dx-1.
            array(ibto+i)=-dxm1*array(ixp)+ dx*array(ixpp1)
          enddo
        else if(interpord.eq.2)then
c           
c           quadratic
c           
          do i=1,nstretch(nv)
            x=i/float(interpfac)+ofstretch(nv)
            xp=min(max(1.,x*cbet(nv)), float(nprj))
            IXP = nint(XP)
            DX = XP - IXP
            ixp = ixp + ibfrom
            IXPP1 = min(IXP + 1, nprj +ibfrom)
            IXPM1 = max(IXP - 1, 1 +ibfrom)
            V4 = ARRAY(IXPM1)
            V5 = ARRAY(IXP)
            V6 = ARRAY(IXPP1)
C             
            A = (V6 + V4)*.5 - V5
            C = (V6 - V4)*.5
C             
            dennew = A*DX*DX + C*DX + V5
c             dennew=min(dennew,max(v4,v5,v6))
c             dennew=max(dennew,min(v4,v5,v6))
            array(ibto+i)=dennew
          enddo
        else
c           
c           cubic
c           
          do i=1,nstretch(nv)
            x=i/float(interpfac)+ofstretch(nv)
            xp=min(max(1.,x*cbet(nv)), float(nprj))
            IXP = XP
            DX = XP - IXP
            ixp = ixp + ibfrom
            IXPP1 = min(IXP + 1, nprj +ibfrom)
            IXPM1 = max(IXP - 1, 1 +ibfrom)
            ixpp2 = min(ixp + 2, nprj+ibfrom)
            
            dxm1 = dx-1.
            dxdxm1=dx*dxm1
            fx1=-dxm1*dxdxm1
            fx4=dx*dxdxm1
            fx2=1+dx**2*(dx-2.)
            fx3=dx*(1.-dxdxm1)
            dennew=fx1*array(ixpm1)+fx2*array(ixp)+
     &          fx3*array(ixpp1)+fx4*array(ixpp2)
c             dennew=min(dennew,max(array(ixpm1),array(ixp),array(ixpp1),
c             &         array(ixpp2)))
c             dennew=max(dennew,min(array(ixpm1),array(ixp),array(ixpp1),
c             &         array(ixpp2)))
            array(ibto+i)=dennew
          enddo
        endif
      enddo

      RETURN
      END


C       ---------------------------------------------------------------------
      SUBROUTINE PROJECT(ISTART,lslice)
C       --------------------------
C       
C       This subroutine assembles one reconstructed slice perpendicular
C       to the tilt axis, using a back projection method.
C       
      implicit none
      include 'tilt.inc'
      real*4 xprojf(limwidth),xprojz(limwidth)
      real*4 yprojf(limwidth),yprojz(limwidth)
      integer*4 jstrt(3),jend(3)
      real*8 xproj8
      integer*4 nprj2,isshift,ifshift,ipdel,irep,IPOINT,NV,iv,INDEX,i,j
      real*4 fbpshift,ycenorig,CBETA,SBETA,zz,zpart,yy,yproj,YFRAC,omyfrac
      integer*4 jPROJ,jlft,jrt,iproj,ip1,ip2,ind,ipbase,ifytest
      integer*4 jtstlft,jtstrt,ISTART,lslice,jregion
      real*4 xlft,xrt,x,xfrac,omxfrac,zbot,ztop,xproj

      if(fastbp)then
        nprj2=nprj+npad
        isshift=istart+(nprj-nprjfbp)/2
        ifshift=0
        fbpshift=0.
        ycenorig=ithick/2+0.5
        if(ycenorig.ne.ycen)then
          ifshift=1
          fbpshift=ycenorig-ycen
        endif
        call fbp(array(isshift),nprj2,nprjfbp,nviews,iwide,ithick,
     &      ifshift,fbpshift,
     &      array(imap), array(ifbpiw),array(ifbprw),array(ifbpzw),
     &      array(ifbpwrk), array(ifbpzwrk))
        return
      endif

      NPRJ2=NPRJ+2+npad
c       slicen=mprj/2.+0.5
c       ipdel=idelslice*nprj2*nviews
      ipdel=idelslice*iplane
c       nbase=imap+ithwid
      
      if(nxwarp.eq.0)then
c         
c         Loop over replications
        do irep=1,nreplic
C           
C           Loop over all views
          IPOINT=ISTART-1
          DO NV=1,NVIEWS
C             
C             Set view angle
            iv=nv+(irep-1)*nviews  
            CBETA=Cbet(iv)
            SBETA=Sbet(iv)
C             
C             Loop over all points in output slice
            INDEX=IMAP
C             
            DO I=1,ITHICK
              ZZ=(I-YCEN)*compress(nv)
              if(ifalpha.le.0)then
                zPART=zz*SBETA+XCENin+DELXX
              else
c                 
c                 If x-axis tilting, find interpolation factor between the
c                 slices
c                 
                yy=lslice-slicen
                zpart= yy*sal(iv)*sbeta + zz*(cal(iv)*sbeta +xzfac(iv)) +
     &              xcenin+delxx
                yproj=yy*cal(iv) - zz*(sal(iv)-yzfac(iv)) + slicen
                jPROJ=YPROJ
                jproj=min(mprj-1,jproj)
                YFRAC=YPROJ-JPROJ
                omyfrac=1.-yfrac
              endif
c               
c               compute left and right limits that come from legal data
c               
              x = cbeta
              if (abs(cbeta) .lt. 0.001) x = sign(0.001, cbeta)
              xlft=(1.-zpart) / x + xcen
              xrt=(nprj-zpart) / x + xcen
              if (xrt .lt. xlft) then
                x = xlft
                xlft = xrt
                xrt = x
              endif
              jlft=xlft
              if(jlft.lt.xlft)jlft=jlft+1
              jlft=max(jlft,masklft(i))
              jrt=xrt
              if(jrt.eq.xrt)jrt=jrt-1
              jrt=min(jrt,maskrt(i))
c               
c               set up starting index and projection position
c               
              do ind = index + masklft(i) - 1, index + jlft - 2
                array(ind) = array(ind) + edgeFill
              enddo                
              index=index+(jlft-1)
              x=jlft-xcen
              if(interpfac.ne.0)then
c                 
c                 Computation with prestretched data
c                 
                XPROJ8=interpfac*(zPART/CBETA + X - ofstretch(iv))
                IPROJ=XPROJ8
                XFRAC=XPROJ8-IPROJ
                iproj=iproj + ipoint + indstretch(iv)
                omxfrac=1.-xfrac
                if(ifalpha.le.0)then
c                   
c                   interpolation in simple case of no x-axis tilt
c                   
                  DO ind=index,index+jrt-jlft
                    ARRAY(IND)=ARRAY(IND)+
     &                  omxfrac*ARRAY(IPROJ) +XFRAC*ARRAY(IPROJ+1) 
                    iproj=iproj+interpfac
                  enddo
                  index=index+jrt+1-jlft
                else
c                   
c                   If x-axis tilting, interpolate from two lines
c                   
                  ip1=iproj+(jproj-lslice)*ipdel
                  ip2=ip1+ipdel
                  if(yproj.ge.1..and.yproj.le.mprj.and.
     &                ip1.ge.nbase.and.ip2.ge.nbase.and.ip1.lt.nstack
     &                .and.ip2.lt.nstack)then
                    
                    DO ind=index,index+jrt-jlft
                      ARRAY(IND)=ARRAY(IND)+
     &                    omxfrac*(omyfrac*ARRAY(IP1)+yFRAC*ARRAY(IP2)) +
     &                    xfrac*(omyfrac*ARRAY(IP1+1)+yFRAC*ARRAY(IP2+1))
                      ip1=ip1+interpfac
                      ip2=ip2+interpfac
                    enddo
                  else
                    do ind = index, index + jrt+1-jlft-1
                      array(ind) = array(ind) + edgeFill
                    enddo                
                  endif
                  index=index+jrt+1-jlft
                endif
              else
c                 
c                 Computation direct from projection data
c                 
                XPROJ8=zPART+X*CBETA
                if(ifalpha.le.0)then
c                   
c                   interpolation in simple case of no x-axis tilt
c                   
                  call bpsumnox(array,index,ipoint,jrt+1-jlft,
     &                xproj8,cbeta)
                else
c                   
c                   If x-axis tilting
c                   
                  IPROJ=XPROJ8
                  ipbase=ipoint+(jproj-lslice)*ipdel
                  ip1=ipbase+iproj
                  ip2=ip1+ipdel
                  if(yproj.ge.1..and.yproj.le.mprj.and.
     &                ip1.ge.nbase.and.ip2.ge.nbase.and.ip1.lt.nstack
     &                .and.ip2.lt.nstack)then
                    
                    call bpsumxtilt(array,index,ipbase,ipdel,jrt+1-jlft,
     &                  xproj8,cbeta,yfrac,omyfrac)
                  else
                    do ind = index, index + jrt+1-jlft-1
                      array(ind) = array(ind) + edgeFill
                    enddo                
                    index=index+jrt+1-jlft
                  endif
                endif
              endif
              do ind = index, index + iwide - jrt - 1
                array(ind) = array(ind) + edgeFill
              enddo                
              index=index+iwide-jrt
            enddo
C             
C-------------------------------------------
C             
C             End of projection loop
            if(interpfac.eq.0)IPOINT=IPOINT+NPRJ2
          enddo
C           
C-------------------------------------------
C           
        enddo
      else
c         
c         LOCAL ALIGNMENTS  
c         
c         Loop over replications
        do irep=1,nreplic
C           
C           Loop over all views
          IPOINT=ISTART-1
          DO NV=1,NVIEWS
C             
C             Set view angle
            iv=nv+(irep-1)*nviews  
            CBETA=Cbet(iv)
            SBETA=Sbet(iv)
C             
c             precompute the factors for getting xproj and yproj all the
c             way across the slice
c             
            ifytest=0
            zbot=(1-ycen)*compress(nv)
            ztop=(ithick-ycen)*compress(nv)
            DO J=1,IWIDE
c               
c               get the fixed and z-dependent component of the
c               projection coordinates

              call localProjFactors(j, lslice, nv, xprojf(j),  xprojz(j),
     &            yprojf(j), yprojz(j))
c               
c               see if any y testing is needed in the inner loop by checking
c               yproj at top and bottom in Z
c               
              yproj=yprojf(j)+yprojz(j)*zbot
              jPROJ=YPROJ
              ip1=ipoint+(jproj-lslice)*ipdel+1
              ip2=ip1+ipdel
              if(ip1.le.nbase.or.ip2.le.nbase.or.ip1.ge.nstack
     &            .or.ip2.ge.nstack.or.jproj.lt.1.or.jproj.ge.mprj)
     &            ifytest=1
              yproj=yprojf(j)+yprojz(j)*ztop
              jPROJ=YPROJ
              ip1=ipoint+(jproj-lslice)*ipdel+1
              ip2=ip1+ipdel
              if(ip1.le.nbase.or.ip2.le.nbase.or.ip1.ge.nstack
     &            .or.ip2.ge.nstack.or.jproj.lt.1.or.jproj.ge.mprj)
     &            ifytest=1
            enddo
c             
c             walk in from each end until xproj is safely within bounds
c             to define region where no x checking is needed
c             
            jtstlft=0
            j=1
            do while(jtstlft.eq.0.and.j.lt.iwide)
              if(min(xprojf(j)+zbot*xprojz(j),
     &            xprojf(j)+ztop*xprojz(j)).ge.1)jtstlft=j
              j=j+1
            enddo
            if(jtstlft.eq.0)jtstlft=iwide
c             
            jtstrt=0
            j=iwide
            do while(jtstrt.eq.0.and.j.gt.1)
              if(max(xprojf(j)+zbot*xprojz(j),
     &            xprojf(j)+ztop*xprojz(j)).lt.nprj)jtstrt=j
              j=j-1
            enddo
            if(jtstrt.eq.0)jtstrt=1
            if(jtstrt.lt.jtstlft)then
              jtstrt=iwide/2
              jtstlft=jtstrt+1
            endif
c             
            INDEX=IMAP
C             
c             loop over the slice, outer loop on z levels
c             
            DO I=1,ITHICK
              ZZ=(I-YCEN)*compress(nv)
              jlft=max(jtstlft,masklft(i))
              jrt=min(jtstrt,maskrt(i))
              index=index+masklft(i)-1
c               
c               set up to do inner loop in three regions of X
c               
              jstrt(1)=masklft(i)
              jend(1)=jlft-1
              jstrt(2)=jlft
              jend(2)=jrt
              jstrt(3)=jrt+1
              jend(3)=maskrt(i)
              do jregion=1,3
                if(jregion.ne.2.or.ifytest.eq.1)then
c                   
c                   loop involving full testing - either left or right
c                   sides needing x testing, or anywhere if y testing
c                   needed
c                   
                  do j=jstrt(jregion),jend(jregion)
                    xproj=xprojf(j)+zz*xprojz(j)
                    yproj=yprojf(j)+zz*yprojz(j)
                    if(xproj.ge.1.and.xproj.le.nprj.and.
     &                  yproj.ge.1..and.yproj.le.mprj)then
c                       
                      IPROJ=XPROJ
                      iproj=min(nprj-1,iproj)
                      XFRAC=XPROJ-IPROJ
                      jPROJ=YPROJ
                      jproj=min(mprj-1,jproj)
                      YFRAC=YPROJ-JPROJ
c                       
                      ip1=ipoint+(jproj-lslice)*ipdel+iproj
                      ip2=ip1+ipdel
                      if(ip1.ge.nbase.and.ip2.ge.nbase.and.
     &                    ip1.lt.nstack .and.ip2.lt.nstack) then
                        ARRAY(INDEX)=ARRAY(INDEX)+
     &                      (1.-yfrac)*((1.-XFRAC)*ARRAY(IP1)
     &                      +XFRAC*ARRAY(IP1+1)) +
     &                      yfrac*((1.-XFRAC)*ARRAY(IP2)
     &                      +XFRAC*ARRAY(IP2+1))
                      else
                        ARRAY(INDEX)=ARRAY(INDEX)+edgeFill
                      endif
                    else
                      ARRAY(INDEX)=ARRAY(INDEX)+edgeFill
                    endif
                    index=index+1
                  enddo   
c                   
c                   loop for no x-testing and no y testing
c                   
                else
                  call bpsumlocal(array,index,zz,xprojf,xprojz,yprojf,
     &                yprojz,ipoint,ipdel,lslice,jstrt(jregion),
     &                jend(jregion))
                endif
              enddo                 
              index=index+iwide-maskrt(i)
            enddo
C-------------------------------------------
C             
C             End of projection loop
            IPOINT=IPOINT+NPRJ2
          enddo
        ENDDO
      endif
      RETURN
      END
C       
C-------------------------------------------------------------------------
c       
c       COMPOSE will interpolate the output slice LSLICEOUT from vertical
c       slices in the ring buffer, where LVSSTART and LVSEND are the starting
c       and ending slices in the ring buffer, IDIR is the direction of
c       reconstruction, and IRINGSTART is the position of LVSSTART in the
c       ring buffer.
c       
      subroutine compose(lsliceout,lvsstart,lvsend,idir,iringstart)
      implicit none
      include 'tilt.inc'
      integer*4 lsliceout,lvsstart,lvsend,idir,iringstart
      integer*4 ind1(4),ind2(4),ind3(4),ind4(4)
      real*4 tanalpha,vertcen,cenj,cenl,vsl,vycen,fx,vy,fy,f22,f23,f32,f33
      integer*4 ivsl,ifmiss,i,lvsl,iring,ibase,ivy,indcen,jnd5,jnd2,j,k
      real*4 fx1,fx2,fx3,fx4,fy1,fy2,fy3,fy4,v1,v2,v3,v4,f5,f2,f8,f4,f6
      integer*4 jnd8,jnd4,jnd6

      tanalpha=sal(1)/cal(1)
      vertcen=ithick/2+0.5
c       
c       loop on lines of data
c       
      do j=1,ithickout
        cenj=j-(ithickout/2+0.5)
        cenl=lsliceout-slicen
c         
c         calculate slice number and y position in vertical slices
c         
        vsl=cenl*cal(1)-cenj*sal(1)+slicen+yoffset*sal(1)
        vycen=cenl*sal(1)+cenj*cal(1)
        ivsl=vsl
        fx=vsl-ivsl
        ifmiss=0
c         
c         for each of 4 slices needed for cubic interpolation, initialize
c         data indexes at zero then see if slice exists in ring
c         
        do i=1,4
          ind1(i)=0
          ind2(i)=0
          ind3(i)=0
          ind4(i)=0
          lvsl=ivsl+i-2
          if(idir*(lvsl-lvsstart).ge.0.and.idir*(lvsend-lvsl).ge.0)then
c             
c             if slice exists, get base index for the slice, compute the
c             y index in the slice, then set the 4 data indexes if they
c             are within the slice
c             
            iring=idir*(lvsl-lvsstart)+iringstart
            if(iring.gt.nvertneed)iring=iring-nvertneed
            ibase=imap+ithwid+(iring-1)*ithick*iwide
            vy=vycen+vertcen-nint(tanalpha*(lvsl-slicen))
            ivy=vy
            fy=vy-ivy
            if(ivy-1.ge.1.and.ivy-1.le.ithick)ind1(i)=ibase+iwide*(ivy-2)
            if(ivy.ge.1.and.ivy.le.ithick)ind2(i)=ibase+iwide*(ivy-1)
            if(ivy+1.ge.1.and.ivy+1.le.ithick)ind3(i)=ibase+iwide*ivy
            if(ivy+2.ge.1.and.ivy+2.le.ithick)ind4(i)=ibase+iwide*(ivy+1)
          endif
          if(ind1(i).eq.0.or.ind2(i).eq.0.or.ind3(i).eq.0.or.
     &        ind4(i).eq.0)ifmiss=1
        enddo
        ibase=imap+(j-1)*iwide-1
        if(intordxtilt.gt.2.and.ifmiss.eq.0)then
c           
c           cubic interpolation if selected, and no data missing
c           
          fx1=2.*fx**2-fx**3-fx
          fx2=fx**3-2.*fx**2+1
          fx3=fx**2+fx-fx**3
          fx4=fx**3-fx**2
          fy1=2.*fy**2-fy**3-fy
          fy2=fy**3-2.*fy**2+1
          fy3=fy**2+fy-fy**3
          fy4=fy**3-fy**2
          do i=1,iwide
            v1=fx1*array(ind1(1))+fx2*array(ind1(2))+
     &          fx3*array(ind1(3))+fx4*array(ind1(4))
            v2=fx1*array(ind2(1))+fx2*array(ind2(2))+
     &          fx3*array(ind2(3))+fx4*array(ind2(4))
            v3=fx1*array(ind3(1))+fx2*array(ind3(2))+
     &          fx3*array(ind3(3))+fx4*array(ind3(4))
            v4=fx1*array(ind4(1))+fx2*array(ind4(2))+
     &          fx3*array(ind4(3))+fx4*array(ind4(4))
            array(ibase+i)=fy1*v1+fy2*v2+fy3*v3+fy4*v4
            do k=1,4
              ind1(k)=ind1(k)+1
              ind2(k)=ind2(k)+1
              ind3(k)=ind3(k)+1
              ind4(k)=ind4(k)+1
            enddo
          enddo
        elseif(intordxtilt.eq.2.and.ifmiss.eq.0)then
c           
c           quadratic interpolation if selected, and no data missing
c           shift to next column or row if fractions > 0.5
c           
          indcen=2
          if(fx.gt.0.5)then
            indcen=3
            fx=fx-1.
          endif
          if(fy.le.0.5)then
            jnd5=ind2(indcen)
            jnd2=ind1(indcen)
            jnd8=ind3(indcen)
            jnd4=ind2(indcen-1)
            jnd6=ind2(indcen+1)
          else
            fy=fy-1.
            jnd5=ind3(indcen)
            jnd2=ind2(indcen)
            jnd8=ind4(indcen)
            jnd4=ind3(indcen-1)
            jnd6=ind3(indcen+1)
          endif
c           
c           get coefficients and do the interpolation
c           
          f5=1.-fx**2-fy**2
          f2=(fy**2-fy)/2.
          f8=f2+fy
          f4=(fx**2-fx)/2.
          f6=f4+fx
          do i=1,iwide
            array(ibase+i)=f5*array(jnd5)+f2*array(jnd2)+
     &          f4*array(jnd4)+f6*array(jnd6)+f8*array(jnd8)
            jnd5=jnd5+1
            jnd2=jnd2+1
            jnd4=jnd4+1
            jnd6=jnd6+1
            jnd8=jnd8+1
          enddo
        else
c           
c           linear interpolation
c           
c           print *,j,ind2(2),ind2(3),ind3(2),ind3(3)
          if(ind2(2).eq.0.or.ind2(3).eq.0.or.ind3(2).eq.0.or.
     &        ind3(3).eq.0)then
c             
c             if there is a problem, see if it can be rescued by shifting
c             center back to left or below
c             
            if(fx.lt.0.02.and.ind2(1).ne.0.and.ind3(1).ne.0.and.
     &          ind2(2).ne.0.and.ind3(2).ne.0)then
              fx=fx+1
              ind2(3)=ind2(2)
              ind2(2)=ind2(1)
              ind3(3)=ind3(2)
              ind3(2)=ind3(1)
            elseif(fy.lt.0.02.and.ind1(2).ne.0.and.ind1(3).ne.0.and.
     &            ind2(2).ne.0.and.ind3(2).ne.0)then
              fy=fy+1
              ind3(2)=ind2(2)
              ind2(2)=ind1(2)
              ind3(3)=ind2(3)
              ind2(3)=ind1(3)
            endif
          endif
c           
c           do linear interpolation if conditions are right, otherwise fill
c           
          if(ind2(2).ne.0.and.ind2(3).ne.0.and.ind3(2).ne.0.and.
     &        ind3(3).ne.0)then
            f22=(1.-fy)*(1.-fx)
            f23=(1.-fy)*fx
            f32=fy*(1.-fx)
            f33=fy*fx
            do i=1,iwide
              array(ibase+i)=f22*array(ind2(2))+f23*array(ind2(3))+
     &            f32*array(ind3(2))+f33*array(ind3(3))
              ind2(2)=ind2(2)+1
              ind2(3)=ind2(3)+1
              ind3(2)=ind3(2)+1
              ind3(3)=ind3(3)+1
            enddo
          else
c             print *,'filling',j
            do i=1,iwide
              array(i+ibase)=edgeFill*nviews
            enddo
          endif
        endif
      enddo
      return
      end
C       
C-------------------------------------------------------------------------
      SUBROUTINE DUMP(LSLICE,DMIN,DMAX,DTOT8)
C       --------------------------------------
C       
      implicit none
      include 'tilt.inc'
      integer*4 lslice,nparextra,iend,index,i,j,iaryBase
      real*4 DMIN,DMAX,fill,projline(limwidth)
      real*8 dtot8,dtmp8
c       
      nparextra=100
      IEND=IMAP+ITHickout*iwide-1
C       
C       Scale
c       DNM simplified and fixed bug in getting min/max/mean
      dtmp8=0.
c       
c       DNM 9/23/04: incorporate reproj option
c       
      if(reproj)then
        DO I=IMAP,IEND
          IF(MASK.and.array(i).eq.tmask)THEN
C--------------mask
            ARRAY(I)=RMASK
          ELSE
C--------------Scale
            ARRAY(I)=(ARRAY(I)+FLEVL)*SCALE
          END IF
        enddo
c         
c         Fill value assumes edge fill value
c
        fill = (edgeFill + flevl) * scale
        do j = 1, nreproj
          i=(j-1) * iwide + 1
          call reproject(array(imap), iwide, ithick, iwide, sinReproj(j),
     &        cosReproj(j), xraystr(i), yraystr(i),
     &        nrayinc(i), nraymax(j), fill, projline)
          do i=1,iwide
            DMIN=AMIN1(PROJLINE(I),DMIN)
            DMAX=AMAX1(PROJLINE(I),DMAX)
            DTmp8=DTmp8+PROJLINE(I)
          enddo
                  i = (lslice - islice) / idelslice
          if (minTotSlice .gt. 0) i = lslice - minTotSlice
          call parWrtPosn(2, j - 1, i)
          call parWrtLin(2,projline)
        enddo
        dtot8=dtot8+dtmp8
        return
      endif
c       
      DO I=IMAP,IEND
        IF(MASK.and.array(i).eq.tmask)THEN
C--------------mask
          ARRAY(I)=RMASK
        ELSE
C--------------Scale
          ARRAY(I)=(ARRAY(I)+FLEVL)*SCALE
        END IF
        DMIN=AMIN1(ARRAY(I),DMIN)
        DMAX=AMAX1(ARRAY(I),DMAX)
        DTmp8=DTmp8+ARRAY(I)
C         
      enddo
      dtot8=dtot8+dtmp8
C       
C       Dump slice
      IF(PERP)THEN
C         ....slices correspond to sections of map
        CALL parWrtSEC(2,ARRAY(IMAP))
      ELSE
C         ....slices must be properly stored
C         Take each line of array and place it in the correct section
C         of the map.
        INDEX=IMAP
        DO J=1,ITHICKout
          CALL IMPOSN(2,J-1,(LSLICE-ISLICE)/idelslice)
          CALL IWRLIN(2,ARRAY(INDEX))
c           
c           DNM 2/29/01: partially demangle the parallel output by writing
c           up to 100 lines at a time in this plane
c           
          if(mod((LSLICE-ISLICE)/idelslice,nparextra).eq.0)then
            do i=1,min(nparextra-1,(jslice-lslice)/idelslice)
              CALL IWRLIN(2,ARRAY(INDEX))
            enddo
          endif
          INDEX=INDEX+IWIDE
        END DO
      END IF
C       
      RETURN
      END

C       -----------------------------------------------------------------
      SUBROUTINE INPUT()
C       ----------------
      
      implicit none
      include 'tilt.inc'
      integer limnum
      parameter (limnum = 100)
      integer*4 nweight, numWgtAngles
      real*4 wincr(20), wgtAngles(limview)
      COMMON /DENSWT/nweight,wincr,numWgtAngles,wgtAngles
C       
      integer*4 MPXYZ(3),NOXYZ(3),nrxyz(3)
      real*4 outilt(3),cell(6),dtor
      data outilt/90.,0.,0./
      data cell/0.,0.,0.,90.,90.,90./
      DATA DTOR/0.0174532/
      CHARACTER DAT*9,TIM*8
      real*4 delta(3)
      character*80 titlech
C       
      Character*1024 card
      CHARACTER*320 FILIN,FILOUT,recfile,basefile,boundfile
      integer*4 nfields,inum(limnum)
      real*4 XNUM(limnum)
c       
      integer*4 ivexcl(limview),ivreprj(limview)
      real*4 repinc(limview),angReproj(limview)
      include 'fbpswitch.inc'
      integer*4 mode,newangles,iftiltfile,nvuse,nvexcl
      real*4 delang,compfac,globalpha,xoffset,scalelocal,rrmax,rfall,xoffAdj
      integer*4 irmax,ifall,ncompress,nxfull,nyfull,ixsubset,iysubset
      integer*4 interpfbp,kti,indbase,ipos,idtype,lens
      integer*4 nd1,nd2,nv,nslice,indi,i,iex,nvorig,irep,iv
      real*4 vd1,vd2,dtheta,theta,thetanv,rmax,sdg,oversamp,scalescl
      integer*4 nprjp,nwidep,needwrk,needzwrk,neediw,needrw,needout,minsup
      integer*4 maxsup,nshift,nprj2,nsneed,ninp,nexclist,j,needzw,ind, nument
      integer*4 npadtmp,nprpad,ithicknew,nocosPlanes,ifZfac,localZfacs
      integer*4 ifThickIn,ifSliceIn,ifWidthIn,imageBinned,ifSubsetIn,ierr
      real*4 pixelLocal, dmint,dmaxt,dmeant, frac, origx, origy, origz
      integer*4 nViewsReproj, iwideReproj, k, ind1, ind2, ifExpWeight
      logical*4 adjustOrigin, projModel, readw_or_imod
      integer*4 licenseusfft,niceframe, parWrtInitialize
c
      integer*4 numOptArg, numNonOptArg
      integer*4 PipGetInteger,PipGetBoolean,PipGetLogical,PipGetTwoFloats
      integer*4 PipGetString,PipGetFloat, PipGetTwoIntegers,PipGetFloatArray
      integer*4 PipGetInOutFile,PipGetIntegerArray,PipNumberOfEntries
      integer*4 PipGetThreeFloats
c       
c       fallbacks from ../../manpages/autodoc2man -2 2  tilt
c       
      integer numOptions
      parameter (numOptions = 54)
      character*(40 * numOptions) options(1)
      options(1) =
     &    'input:InputProjections:FN:@output:OutputFile:FN:@'//
     &    'recfile:RecFileToReproject:FN:@:ProjectModel:FN:@'//
     &    ':BaseRecFile:FN:@:AdjustOrigin:B:@:ANGLES:FAM:@:BaseNumViews:I:@'//
     &    ':COMPFRACTION:F:@:COMPRESS:FAM:@:COSINTERP:IA:@:DENSWEIGHT:FA:@'//
     &    ':DONE:B:@:EXCLUDELIST2:LIM:@:FlatFilterFraction:F:@:FBPINTERP:I:@'//
     &    ':FULLIMAGE:IP:@:BoundaryInfoFile:FN:@:IMAGEBINNED:I:@'//
     &    ':INCLUDE:LIM:@:SubtractFromBase:LI:@:LOCALFILE:FN:@'//
     &    ':LOCALSCALE:F:@:LOG:F:@:MASK:F:@:MinMaxMean:IT:@:MODE:I:@'//
     &    ':OFFSET:FA:@:PARALLEL:B:@:PERPENDICULAR:B:@:RADIAL:FP:@'//
     &    ':REPLICATE:FPM:@:REPROJECT:FAM:@:SCALE:FP:@:SHIFT:FA:@:SLICE:FA:@'//
     &    ':SUBSETSTART:IP:@:THICKNESS:I:@:TILTFILE:FN:@:TITLE:CH:@'//
     &    ':TOTALSLICES:IP:@:ViewsToReproject:LI:@:WeightAngleFile:FN:@'//
     &    ':WeightFile:FN:@:WIDTH:I:@:XAXISTILT:F:@'//
     &    'xminmax:XMinAndMaxReproj:IP:@:XTILTFILE:FN:@:XTILTINTERP:I:@'//
     &    'yminmax:YMinAndMaxReproj:IP:@:ZFACTORFILE:FN:@'//
     &    'zminmax:ZMinAndMaxReproj:IP:@param:ParameterFile:PF:@help:usage:B:'
c       
      recReproj = .false.
      nViewsReproj = 0
c       
c       Pip startup: set error, parse options, check help, set flag if used
c       
      call PipSetSpecialFlags(1,1,1,2,0)
      call PipReadOrParseOptions(options, numOptions, 'tilt',
     &    'ERROR: TILT - ', .false., 0, 1, 1, numOptArg, numNonOptArg)

      if (PipGetInOutFile('InputProjections', 1, ' ', filin) .ne. 0)
     &    call exitError('NO INPUT FILE WITH PROJECTIONS SPECIFIED')
      if (PipGetInOutFile('OutputFile', 2, ' ', filout) .ne. 0)
     &    call exitError('NO OUTPUT FILE SPECIFIED')

C       
C       Open input projection file
      CALL IMOPEN(1,FILIN,'RO')
      CALL IRDHDR(1,NPXYZ,MPXYZ,MODE,PMIN,PMAX,PMEAN)
      call irtdat(1,idtype,lens,nd1,nd2,vd1,vd2)
      NVIEWS=NPXYZ(3)
c       
c       The approximate implicit scaling caused by the default radial filter
      filterScale = npxyz(1) / 2.2
c       
      if (nviews.gt.limview) call exitError('Too many images in tilt series.')
      newangles=0
      iftiltfile=0
c       
c       Get model file to project
      projModel = PipGetString('ProjectModel', recfile) .eq. 0
      if (projModel .and. .not.readw_or_imod(recfile)) call exitError(
     &    'READING MODEL FILE TO REPROJECT')
c       
c       Get entries for reprojection from rec file
      if (PipGetString('RecFileToReproject', recfile) .eq. 0) then
        if (projModel) call exitError(
     &      'YOU CANNOT USE -RecFileToReproject with -ProjectModel')
        recReproj = .true.
        projMean = pmean
        call imclose(1)
        call IMOPEN(1,recfile,'RO')
        CALL IRDHDR(1,NRXYZ,MPXYZ,MODE,PMIN,PMAX,PMEAN)
        minXreproj = 0
        minYreproj = 0
        minZreproj = 0
        maxXreproj = nrxyz(1) - 1
        maxYreproj = nrxyz(2) - 1
        maxZreproj = nrxyz(3) - 1
        ierr = PipGetTwoIntegers('XMinAndMaxReproj', minXreproj, maxXreproj)
        ierr = PipGetTwoIntegers('YMinAndMaxReproj', minYreproj, maxYreproj)
        ierr = PipGetTwoIntegers('ZMinAndMaxReproj', minZreproj, maxZreproj)
        if (minXreproj .lt. 0 .or. minYreproj .lt. 0 .or. maxXreproj .ge.
     &      nrxyz(1) .or. maxYreproj .ge. nrxyz(2)) call exitError(
     &      'Min or Max X, or Y coordinate to project is out of range')
        if (PipGetString('ViewsToReproj', card) .eq. 0) then
          call parselist(card, ivreprj, nViewsReproj)
          if (nViewsReproj .gt. limview) call exitError(
     &        'TOO MANY VIEWS IN LIST TO REPROJECT FOR ARRAYS')
        endif
        minXreproj = minXreproj + 1
        maxXreproj = maxXreproj + 1
        minYreproj = minYreproj + 1
        maxYreproj = maxYreproj + 1
        minZreproj = minZreproj + 1
        maxZreproj = maxZreproj + 1
      endif

      if (PipGetString('BaseRecFile', basefile) .eq. 0) then
        readBase = .true.
        if (PipGetInteger('BaseNumViews', numViewBase) .ne. 0) call exitError(
     &      'YOU MUST ENTER -BaseNumViews with -BaseRecFile')
        if (.not. recReproj .and. .not.projModel) then
          call imopen(3, basefile, 'RO')
          call irdhdr(3,NRXYZ,MPXYZ,MODE,dmint,dmaxt,dmeant)
        endif
      endif
c       
C-------------------------------------------------------------
C       Set up defaults:
C       
C...... Default slice is all rows in a projection plane
      ISLICE=1
      JSLICE=NPXYZ(2)
      idelslice=1
C...... and has the same number of columns.
      IWIDE=NPXYZ(1)
C...... Default is no mask and default mask is zero
      MASK=.FALSE.
      RMASK=0.
C...... Default is no scaling of output map
      FLEVL=0.
      SCALE=1.
C...... Default is no offset or rotation
      DELANG=0.
c...... Default is output mode 2
      newmode=2
c...... Start with no list of views to use or exclude
      nvuse=0
      nvexcl=0
c...... Default is no logarithms
      iflog=0
c...... Default is no replications
      nreplic=1
      repinc(1)=0.
C...... Default radial weighting parameters - no filtering
      irmax = npxyz(1) / 2 + 1
      ifall = 0
c...... Default overall and individual compression of 1; no alpha tilt
      ncompress=0
      compfac=1.
      do nv=1,nviews
        compress(nv)=1.
        alpha(nv)=0.
        xzfac(nv) = 0.
        yzfac(nv) = 0.
        expWeight(nv) = 1.
      enddo
      ifalpha=0
      globalpha=0.
      ifZfac = 0
c       
c...... Default weighting by density of adjacent views
      nweight=2
      do i=1,nweight
        wincr(i)=1./(i-0.5)
      enddo
      numWgtAngles = 0
c       
      xoffset=0
      yoffset=0
      delxx=0.
      nxwarp=0
      nywarp=0
      scalelocal=0.
      nxfull=0
      nyfull=0
      ixsubset=0
      iysubset=0
      ithick = 10
      imageBinned = 1
      ifThickIn = 0
      ifWidthIn = 0
      ifSliceIn = 0
      ifSubsetIn = 0
      ifExpWeight = 0
      minTotSlice = -1
      maxTotSlice = -1
c       
c...... Default double-width linear interpolation in cosine stretching
      interpfac=2
      interpord=1
      intordxtilt=1
      interpfbp=-1
      perp=.true.
      reproj=.false.
      nreproj = 0
      flatFrac = 0.
      adjustorigin = .false.
      nViewSubtract = 0
c       
c...... Default title
      CALL DATE(DAT)
      CALL TIME(TIM)
c       
      write(titlech,49) 'Tomographic reconstruction',dat,tim
      if (recReproj)write(titlech,49) 'Reprojection from tomogram',dat,tim
      read(titlech,'(20a4)')(title(kti),kti=1,20)
C       
C       

      if (PipGetString('TITLE', card) .eq. 0) then
        write(titlech,49) CARD(1:50),DAT,TIM
        read(titlech,'(20a4)')(TITLE(kti),kti=1,20)
        WRITE(6,101)TITLE
      endif
C       
      nfields = 0
      if (PipGetIntegerArray('SLICE', inum, nfields, limnum) .eq. 0) then
        IF(NFIELDS/2.NE.1)
     &      call exitError('Wrong number of fields on SLICE line')
        ISLICE=INUM(1)+1
        JSLICE=INUM(2)+1
        if(nfields.gt.2)idelslice=inum(3)
        ifSliceIn = 1
      endif
C       
      if (PipGetInteger('THICKNESS', ithick) .eq. 0) then
        if(ithick.gt.limmask)call exitError('Thickness too high for arrays')
        ifThickIn = 1
      endif
C       
      if (PipGetFloat('MASK', rmask) .eq. 0) then
        MASK=.TRUE.
        WRITE(6,401)RMASK
      endif
C       
      
      if (PipGetTwoFloats('RADIAL', rrmax, rfall) .eq. 0) then
        irmax=rrmax
        ifall=rfall
        if(irmax.eq.0)irmax=npxyz(1)*rrmax
        if(ifall.eq.0)ifall=npxyz(1)*rfall
        WRITE(6,501)IRMAX,IFALL
      endif
C       
      nfields = 0
      if (PipGetFloatArray('OFFSET', xnum, nfields, limnum) .eq. 0) then
        IF(NFIELDS.EQ.0 .OR. NFIELDS.GE.3)
     &      call exitError('Wrong number of fields on OFFSET line')
        IF(NFIELDS.EQ.2)DELXX=XNUM(2)
        DELANG=XNUM(1)
      endif
c
      if (PipGetTwoFloats('SCALE', flevl, scale) .eq. 0)
     &    WRITE(6,701)FLEVL,SCALE
C       
      if (PipGetLogical('PERPENDICULAR', PERP) .eq. 0)
     &    WRITE(6,801)
C       
      i = 0
      if (PipGetBoolean('PARALLEL', i) .eq. 0) then
        if (i .ne. 0) then
          PERP=.FALSE.
          WRITE(6,901)
        endif
      endif
c       
      if (PipGetInteger('MODE', newmode) .eq. 0) then
        if(newmode.lt.0.or.newmode.gt.15 .or. (newmode.gt.2.and.newmode.lt.9))
     &      call exitError('Illegal output mode')
        write(6,1001)newmode
      endif
C       
      ierr = PipNumberOfEntries('INCLUDE', nument)
      do j = 1, nument
        ierr = PipGetString('INCLUDE', card)
        call parselist(card,mapuse(nvuse+1),nexclist)
        if (nvuse+nexclist .gt. limview) call exitError(
     &      'TOO MANY INCLUDED VIEWS FOR ARRAYS')
        do i=nvuse+1,nvuse+nexclist
          if(mapuse(i).lt.1.or.mapuse(i).gt.nviews)call exitError(
     &        'Illegal view number in INCLUDE list')
        enddo
        nvuse=nvuse+nexclist
      enddo
C       
      ierr = PipNumberOfEntries('EXCLUDELIST2', nument)
      do j = 1, nument
        ierr = PipGetString('EXCLUDELIST2', card)
        call parselist(card,ivexcl(nvexcl+1),nexclist)
        if (nvexcl+nexclist .gt. limview) call exitError(
     &      'TOO MANY EXCLUDED VIEWS FOR ARRAYS')
        do i=nvexcl+1,nvexcl+nexclist
          if(ivexcl(i).lt.1.or.ivexcl(i).gt.nviews)call exitError(
     &        'Illegal view number in EXCLUDE list')
        enddo
        nvexcl=nvexcl+nexclist
      enddo
c       
      if(nvuse.gt.0 .and. nvexcl .gt. 0)call exitError(
     &    'Illegal to have both INCLUDE and EXCLUDE entries')
c       
      if (PipGetFloat('LOG', baselog) .eq. 0) then
        iflog=1
        write(6,1301)baselog
      endif
c       
      if (PipGetTwoFloats('REPLICATE', xnum(i), xnum(2)) .eq. 0) then
        inum(1) = nint(xnum(1))
        do i=1,inum(1)
          nreplic=nreplic+1
          repinc(nreplic)=xnum(2)*i
        enddo
        write(6,1401)inum(1),xnum(2)
      endif
c       
      ierr = PipNumberOfEntries('ANGLES', nument)
      do j = 1, nument
       nfields = 0
       ierr = PipGetFloatArray('ANGLES',  angles(newangles + 1), nfields,
     &      limview - newangles)
        newangles=newangles+nfields
      enddo
c
      ierr = PipNumberOfEntries('COMPRESS', nument)
      do j = 1, nument
        nfields = 0
        ierr = PipGetFloatArray('COMPRESS',  angles(ncompress + 1), nfields,
     &      limview - ncompress)
        ncompress=ncompress+nfields
      enddo
c       
      if (PipGetFloat('COMPFRACTION', compfac) .eq. 0)
     &    write(6,1701)compfac
c       
      nfields = 0
      if (PipGetFloatArray('DENSWEIGHT', xnum, nfields, limnum) .eq. 0) then
        nweight=nint(xnum(1))
        if(nweight.gt.0)then
          do i=1,nweight
            wincr(i)=1./(i-0.5)
          enddo
          if(nfields.eq.nweight+1)then
            do i=1,nweight
              wincr(i)=xnum(i+1)
            enddo
          elseif(nfields.ne.1)then
            call exitError('Wrong number of fields on DENSWEIGHT line')
          endif
          write(6,1801)nweight,(wincr(i),i=1,nweight)
        else
          write(6,1802)
        endif
      endif
c       
      if (PipGetString('TILTFILE', card) .eq. 0) then
        call dopen(3,card,'ro','f')
        read(3,*,err=2411,end=2411)(angles(i),i=1,nviews)
        close(3)
        iftiltfile=1
      endif
c       
      if (PipGetInteger('WIDTH', iwide) .eq. 0)  ifWidthIn = 1
c       
      nfields = 0
      if (PipGetFloatArray('SHIFT', xnum, nfields, limnum) .eq. 0) then
        if((nfields+1)/2.ne.1)
     &      call exitError('Wrong number of fields on SHIFT line')
        xoffset=xnum(1)
        if(nfields.eq.2)yoffset=xnum(2)
      endif
c       
      if (PipGetString('XTILTFILE', card) .eq. 0) then
        call dopen(3,card,'ro','f')
        read(3,*,err=2412,end=2412)(alpha(i),i=1,nviews)
        close(3)
        do i=1,nviews
          if(abs(alpha(i)).gt.1.e-5)ifalpha=2
        enddo
        if (ifalpha .eq. 2) write(6,2201)
      endif
c       
      if (PipGetString('WeightFile', card) .eq. 0) then
        ifExpWeight = 1
        call dopen(3,card,'ro','f')
        read(3,*,err=2414,end=2414)(expWeight(i),i=1,nviews)
        close(3)
      endif
c       
      boundFile = ' '
      ierr = PipGetString('BoundaryInfoFile', boundFile)
c       
      if (PipGetFloat('XAXISTILT', globalpha) .eq. 0) then
        write(6,2301)globalpha
        if(abs(globalpha).gt.1.e-5.and.ifalpha.eq.0)ifalpha=1
      endif
c       
c       
      if (PipGetString('LOCALFILE', card) .eq. 0) then
        call dopen(3,card,'ro','f')
        read(3,'(a)',err=2410,end=2410)titlech
c         read(3,*)nxwarp,nywarp,ixswarp,iyswarp,idxwarp,idywarp
        call frefor(titlech,delbeta,ninp)
        ifdelalpha=0
        if(ninp.gt.6)ifdelalpha=nint(delbeta(7))
        pixelLocal = 0.
        if (ninp .gt. 7) pixelLocal = delbeta(8)
        localZfacs = 0
        if (ninp .gt. 8) localZfacs = delbeta(9)
        nxwarp=nint(delbeta(1))
        nywarp=nint(delbeta(2))
        ixswarp=nint(delbeta(3))
        iyswarp=nint(delbeta(4))
        idxwarp=nint(delbeta(5))
        idywarp=nint(delbeta(6))
        if(nxwarp*nywarp.gt.limwpos.or.nxwarp*nywarp*nviews.gt.limwarp)
     &      call exitError(
     &      'ARRAY SIZE INSUFFICIENT FOR LOCAL TILT ALIGNMENT DATA')
        if (nxwarp .lt. 2 .or. nywarp .lt. 2) call exitError(
     &      'THERE MUST BE AT LEAST TWO LOCAL ALIGNMENT AREAS IN X AND IN Y')
        indbase=0
        do ipos=1,nxwarp*nywarp
          indwarp(ipos)=indbase
          read(3,*,err=2410,end=2410)(delbeta(i),i=indbase+1,indbase+nviews)
          if(ifdelalpha.gt.0)then
            read(3,*,err=2410,end=2410)(delalpha(i),i=indbase+1,indbase+nviews)
          else
            do i=indbase+1,indbase+nviews
              delalpha(i)=0.
            enddo
          endif
c         
c           Set z factors to zero, read in if supplied, then negate them
c           
          do i=indbase+1,indbase+nviews
            warpxzfac(i)=0.
            warpyzfac(i)=0.
          enddo
          if (localZfacs .gt. 0) read(3,*,err=2410,end=2410)(warpxzfac(i),
     &        warpyzfac(i), i=indbase+1,indbase+nviews)
          do i=indbase+1,indbase+nviews
            warpxzfac(i) = -warpxzfac(i)
            warpyzfac(i) = -warpyzfac(i)
          enddo
          do i=1,nviews
            call xfread(3,fw(1,1,i+indbase),2410,2410)
          enddo
          indbase=indbase+nviews
        enddo
        close(3)
        write(6,2401)
      endif
c       
      if (PipGetFloat('LOCALSCALE', scalelocal) .eq. 0) write(6,2501)scalelocal
c       
      ierr = PipGetTwoIntegers('FULLIMAGE', nxfull, nyfull)
      if (PipGetTwoIntegers('SUBSETSTART', ixsubset, iysubset) .eq. 0)
     &    ifSubsetIn = 1
c       
      nfields = 0
      if (PipGetIntegerArray('COSINTERP', inum, nfields, limnum) .eq. 0) then
        interpord=inum(1)
        if(nfields.gt.1)interpfac=inum(2)
        interpord=max(0,min(3,interpord))
        if(interpord.eq.0)interpfac=0
        if(interpfac.eq.0)then
          print *,'Cosine stretching is disabled'
        else
          write(6,2801)interpord,interpfac
        endif
      endif
c       
      if (PipGetInteger('FBPINTERP', interpfbp) .eq. 0) then
        if(interpfbp.gt.1)interpfbp=3
        if(interpfbp.le.0)then
          print *,'Fast back projection is disabled'
        else
          write(6,2901)interpfbp
        endif
      endif
c       
      if (PipGetInteger('XTILTINTERP', intordxtilt) .eq. 0) then
        intordxtilt=xnum(1)
        if(intordxtilt.gt.2)intordxtilt=3
        if(intordxtilt.le.0)then
          print *,'New-style X-tilting with vertical slices is disabled'
        else
          write(6,3001)intordxtilt
        endif
      endif
C       
c       violates original unless a blank entry is allowed
      ierr = PipNumberOfEntries('REPROJECT', nument)
      do j = 1, nument
        nfields = 0
        ierr = PipGetFloatArray('REPROJECT',  xnum, nfields, limnum)
        if (nfields.eq.0) then
          nfields = 1
          xnum(1) = 0.
        endif
        if (nfields + nreproj .gt. limreproj) call exitError(
     &      'TOO MANY REPROJECTION ANGLES FOR ARRAYS')
        do i = 1, nfields
          nreproj = nreproj + 1
          angReproj(nreproj) = xnum(i)
          cosReproj(nreproj) = cos(dtor * xnum(i))
          sinReproj(nreproj) = sin(dtor * xnum(i))
        enddo      
        if (j. eq. 1)WRITE(6,3101)
        reproj=.not.recReproj
      enddo
c       
      if (PipGetString('ZFACTORFILE', card) .eq. 0) then
        call dopen(3,card,'ro','f')
        read(3,*,err=2413,end=2413)(xzfac(i),yzfac(i),i=1,nviews)
        close(3)
        ifZfac = 1
        write(6,3201)
      endif
c       
      if (PipGetInteger('IMAGEBINNED', imageBinned) .eq. 0) then
        imageBinned = max(1,imageBinned)
        if (imageBinned .gt. 1) write(6,3301)imageBinned
      endif
c       
      if (PipGetTwoIntegers('TOTALSLICES', inum(1), inum(2)) .eq. 0) then
        minTotSlice = inum(1) + 1
        maxTotSlice = inum(2) + 1
      endif
c       
      ierr = PipGetFloat('FlatFilterFraction', flatFrac)
      flatFrac = max(0.,  flatFrac)
c       
      ierr = PipGetLogical('AdjustOrigin', adjustOrigin)
c       
      if (readBase .and. PipGetString('SubtractFromBase', card) .eq. 0) then
        call parselist(card, ivSubtract, nViewSubtract)
        if (nViewSubtract .gt. limview) call exitError(
     &      'TOO MANY VIEWS IN LIST TO SUBTRACT FOR ARRAYS')
      endif
c
      if (.not. recReproj) 
     &    ierr = PipGetThreeFloats('MinMaxMean', pmin, pmax, pmean)
c
      if (PipGetString('WeightAngleFile', card) .eq. 0) then
        call dopen(3,card,'ro','f')
313     read(3,*,err=2415,end=314) wgtAngles(numWgtAngles + 1)
        numWgtAngles = numWgtAngles + 1
        go to 313
c
c         Sort the angles
314     do i = 1, numWgtAngles - 1
          do j = i + 1, numWgtAngles
            if (wgtAngles(i) .gt. wgtAngles(j)) then
              dmint = wgtAngles(i)
              wgtAngles(i) = wgtAngles(j)
              wgtAngles(j) = dmint
            endif
          enddo
        enddo
      endif
c
      call PipDone()
c       
c       END OF OPTION READING
C       
999   WRITE(6,48)
      if(ifalpha.ne.0.and.abs(idelslice).ne.1)call exitError(
     &    'Cannot do X axis tilt with non-consecutive slices')
      if(nxwarp.ne.0.and.abs(idelslice).ne.1)call exitError(
     &    'Cannot do local alignments with non-consecutive slices')
      if(minTotSlice.gt.0.and. idelslice.ne.1)call exitError(
     &    'Cannot do chunk writing with non-consecutive slices')
      if(nxfull.eq.0.and.nyfull.eq.0.and.
     &    (ixsubset.ne.0.or.iysubset.ne.0))call exitError(
     &    'YOU MUST ENTER THE FULL IMAGE SIZE IF YOU HAVE A SUBSET')
      if (.not.perp .and. minTotSlice.gt.0) call exitError(
     &    'Cannot do chunk writing with parallel slices')
      if (nreproj .gt. 0 .and. nViewsReproj .gt. 0) call exitError(
     &    'You cannot enter both views and angles to reproject')
      if (projModel .and. (nreplic .gt. 1 .or. nreproj .gt. 0 .or.
     &    nViewsReproj .gt. 0)) call exitError('You cannot do projection '//
     &    'from a model with image reprojection or replication')
c       
c       scale dimensions down by binning then report them
c       
      if (imageBinned .gt. 1) then
        if (ifSliceIn .ne. 0 .and. (minTotSlice.le.0 .or. islice .gt. 0)) then
          islice = max(1, min(npxyz(2),
     &        (islice + imageBinned - 1) / imageBinned))
          jslice = max(1, min(npxyz(2), 
     &        (jslice + imageBinned - 1) / imageBinned))
        endif
        if (ifThickIn .ne. 0) ithick = ithick / imageBinned
        delxx = delxx / imageBinned
        nxfull = (nxfull + imageBinned - 1) / imageBinned
        nyfull = (nyfull + imageBinned - 1) / imageBinned
        ixsubset = ixsubset / imageBinned
        iysubset = iysubset / imageBinned
        xoffset = xoffset / imageBinned
        yoffset = yoffset / imageBinned
        if (ifWidthIn .ne. 0) iwide = iwide / imageBinned
        if (minTotSlice .ge. 0 .and. .not. recReproj) then
          minTotSlice = max(1, min(npxyz(2),
     &        (minTotSlice + imageBinned - 1) / imageBinned))
          maxTotSlice = max(1, min(npxyz(2),
     &        (maxTotSlice + imageBinned - 1) / imageBinned))
        endif
      endif
      if (recReproj) then
        if (debug)
     &      print *,minTotSlice,maxTotSlice,minZreproj,maxZreproj,nrxyz(3)
        if ((minTotSlice .le. 0 .and. (minZreproj .le. 0 .or. maxZreproj .gt.
     &      nrxyz(3))) .or. (minTotSlice .ge. 0 .and.
     &      maxTotSlice .gt. nrxyz(3))) call exitError(
     &      'Min or Max Z coordinate to project is out of range')

        if (.not.perp) call exitError(
     &      'Cannot reproject from reconstruction output with PARALLEL')
        if (idelslice .ne. 1) call exitError(
     &      'Cannot reprojection from reconstruction with a slice increment')
        if (iwide .ne. nrxyz(1) .or. jslice + 1 - islice .ne. nrxyz(3) .or.
     &      ithick .ne. nrxyz(2)) call exitError(
     &      'Dimensions of rec file do not match expected values')
      else
        if (ifSliceIn .ne. 0) WRITE(6,201)ISLICE,JSLICE,idelslice
        if (ifThickIn .ne. 0) WRITE(6,301)ITHICK
        if (delang .ne. 0. .or. delxx .ne. 0.) WRITE(6,601)DELANG,DELXX
        if (nxfull .ne. 0 .or. nyfull .ne. 0) write(6,2601)nxfull,nyfull
        if (ifSubsetIn .ne. 0) write(6,2701)ixsubset,iysubset
        if (ifWidthIn .ne. 0) WRITE(6,2001)IWIDE
        if (xoffset .ne. 0 .or. yoffset .ne. 0) WRITE(6,2101)yoffset,xoffset
        if (minTotSlice .gt. 0) write(6,3401)minTotSlice,maxTotSlice
      endif
c       
c       If NEWANGLES is 0, get angles from file header.  Otherwise check if angles OK
c       
      if(newangles.eq.0.and.iftiltfile.eq.0)then
c         
c         Tilt information is stored in stack header. Read into angles
c         array. All sections are assumed to be equally spaced. If not,
c         you need to set things up differently. In such a case, great
c         care should be taken, since missing views may have severe 
c         effects on the quality of the reconstruction.
c         
c         
c        call irtdat(1,idtype,lens,nd1,nd2,vd1,vd2)
c         
        if (idtype.ne.1) call exitError( ' Not tilt data.')
c         
        if (nd1.ne.2) call exitError(' Tilt axis not along Y.')
c         
        dtheta = vd1
        theta = vd2
c         
        DO 1105 NV=1,NVIEWS
          ANGLES(NV)=theta
          theta = theta + dtheta
1105    continue
c         
      else
        if(iftiltfile.eq.1.and.newangles.ne.0)then
          call exitError(
     &        'Tried to enter angles with both ANGLES and TILTFILE')
        elseif(iftiltfile.eq.1)then
          write(6,*)' Tilt angles were entered from a tilt file'
        elseif(newangles.eq.nviews)then
          write(6,*)' Tilt angles were entered with ANGLES card(s)'
        else
          call exitError('If using ANGLES, a value must be '//
     &        'entered for each view')
        endif
      endif
c       
      if(ncompress.gt.0)then
        if(ncompress.eq.nviews)then
          write(6,*)
     &        ' Compression values were entered with COMPRESS card(s)'
        else
          call exitError('If using COMPRESS, a value must be '//
     &        'entered for each view')
        endif
        do nv=1,nviews
          compress(nv)=1.+(compress(nv)-1.)/compfac
        enddo
      endif
c       
      if(globalpha.ne.0.)then
        do iv=1,nviews
          alpha(iv)=alpha(iv)-globalpha
        enddo
      endif
c       
      if (ifExpWeight .ne. 0) then
        if (iflog .eq. 0) write(6,*)
     &      ' Weighting factors were entered from a file'
        if (iflog .ne. 0) write(6,*) ' Weighting factors were entered '//
     &      'but will be ignored because log is being taken'
      endif
C       
c       if no INCLUDE cards, set up map to views, excluding any specified by
c       EXCLUDE cards
      if(nvuse.eq.0)then
        do i=1,nviews
          ierr = 0
          do iex=1,nvexcl
            if(i.eq.ivexcl(iex))ierr = 1
          enddo
          if (ierr .eq. 0) then
            nvuse=nvuse+1
            mapuse(nvuse)=i
          endif
        enddo
      endif
c       
c       Replace angles at +/-90 with 89.95 etc
      do i = 1, nvuse
        j = mapuse(i)
        if (abs(abs(angles(j)) - 90.) .lt. 0.05)
     &      angles(j) = sign(90. - sign(0.05, 90 - abs(angles(j))), angles(j))
      enddo
c       
c       If reprojecting from rec and no angles entered, copy angles in original
c       order
      if (recReproj .and. nreproj .eq. 0) then
        if (nViewsReproj .eq. 1 .and. ivreprj(1) .eq. 0) then
          nreproj = nviews
          do i = 1, nviews
            angReproj(i) = angles(i)
          enddo
        else if (nViewsReproj .gt. 0) then
          nreproj = nViewsReproj
          do i = 1, nreproj
            if (ivreprj(i) .lt. 1 .or. ivreprj(i) .gt. nviews) call exitError(
     &          'View number to reproject is out of range')
            angReproj(i) = angles(ivreprj(i))
          enddo
        else
c           
c           For default set of included views, order them by view number by
c           first ordering the mapuse array by view number
          do i=1,nvuse-1
            do j=i+1,nvuse
              if (mapuse(i) .gt. mapuse(j)) then
                indi=mapuse(i)
                mapuse(i)=mapuse(j)
                mapuse(j)=indi
              endif
            enddo
          enddo
          nreproj = nvuse
          do i = 1, nvuse
            angReproj(i) = angles(mapuse(i))
          enddo
        endif
      endif
c       
c       order the MAPUSE array by angle
      do i=1,nvuse-1
        do j=i+1,nvuse
          indi=mapuse(i)
          if(angles(indi).gt.angles(mapuse(j)))then
            mapuse(i)=mapuse(j)
            mapuse(j)=indi
            indi=mapuse(i)
          endif
        enddo
      enddo
C       
C       Open output map file
      call irtdel(1,delta)
      call irtorg(1,origx, origy, origz)
      if (.not. recReproj) then
        if((minTotSlice.le.0 .and. (islice.lt.1.or.jslice.lt.1))
     &      .or.islice.gt.npxyz(2).or. jslice.gt.npxyz(2)) call exitError(
     &      'SLICE NUMBERS OUT OF RANGE')
        NSLICE=(JSLICE-ISLICE)/idelslice+1
        if (minTotSlice.gt.0 .and. islice .lt. 1)
     &      nslice = maxTotSlice + 1 - minTotSlice
c         print *,'NSLICE',minTotSlice,maxTotSlice,islice,nslice
        if(nslice.le.0)call exitError( 'SLICE NUMBERS REVERSED')
        if(iwide.gt.limwidth.and.nxwarp.ne.0)call exitError('OUTPUT'//
     &      ' SLICE TOO WIDE FOR ARRAYS IF DOING LOCAL ALIGNMENTS')
c       
c         DNM 7/27/02: transfer pixel sizes depending on orientation of output
c         
        NOXYZ(1)=iwide
        cell(1)=iwide*delta(1)
        IF(PERP)THEN
          NOXYZ(2)=ITHICK
          NOXYZ(3)=NSLICE
          cell(2)=ithick*delta(1)
          cell(3)=abs(nslice*idelslice)*delta(2)
        ELSE
          NOXYZ(2)=NSLICE
          NOXYZ(3)=ITHICK
          cell(3)=ithick*delta(1)
          cell(2)=abs(nslice*idelslice)*delta(2)
        END IF
        if (reproj)then
          NOXYZ(2)=NSLICE
          NOXYZ(3)=nreproj
          cell(2)=abs(nslice*idelslice)*delta(2)
          cell(3)=delta(1)*nreproj
          if (iwide * nreproj .gt. limrays) call exitError(
     &        'TOO MANY REPROJECTIONS FOR ARRAYS WITH IMAGES THIS WIDE')
          if (iwide .gt. limwidth) call exitError(
     &        'OUTPUT TOO WIDE FOR REPROJECTION ARRAY')
          do i = 1, nreproj
            j=(i-1) * iwide + 1
            call set_projection_rays(sinReproj(i), cosReproj(i), iwide, ithick,
     &          iwide, xraystr(j), yraystr(j), nrayinc(j), nraymax(i))
          enddo
        endif
      else
        noxyz(1) = maxXreproj + 1 - minXreproj
        noxyz(2) = maxZreproj + 1 - minZreproj
        ithickReproj = maxYreproj + 1 - minYreproj
        if (minTotSlice.gt.0 .and. minZreproj .lt. 1)
     &      noxyz(2) = maxTotSlice + 1 - minTotSlice
        if (noxyz(1) .lt. 1 .or. noxyz(2) .lt. 1 .or. ithickReproj .lt. 1)
     &      call exitError('Min and max limits for output are reversed for '//
     &      'X, Y, or Z')
        noxyz(3) = nreproj
        cell(1) = noxyz(1)*delta(1)
        cell(2) = noxyz(2)*delta(1)
        cell(3) = delta(1)*nreproj
      endif
c       
c       Check compatibility of base rec file
      if ((readBase .and. .not. recReproj) .and. (nrxyz(1) .ne. iwide .or.
     &    nrxyz(2) .ne. ithick .or. nrxyz(3) .ne. nslice)) call exitError(
     &    'BASE REC FILE IS NOT THE SAME SIZE AS OUTPUT FILE')
c       
c       open old file if in chunk mode and there is real starting slice
c       otherwise open new file
c       
      if (.not.projModel) then
        if (minTotSlice .gt. 0 .and. ((.not.recReproj .and. islice .gt. 0) .or.
     &      (recReproj .and. minZreproj .gt. 0))) then
          CALL IMOPEN(2,FILOUT,'OLD')
          CALL IRDHDR(2,NOXYZ,MPXYZ,newmode,dmint,dmaxt,dmeant)
        else
          CALL IMOPEN(2,FILOUT,'NEW')
          CALL ICRHDR(2,NOXYZ,NOXYZ,newmode,title,0)
c           print *,'created',NOXYZ
        endif
        CALL ITRLAB(2,1)
        call ialcel(2,cell)
      endif
c       
c       if doing perpendicular slices, set up header info to make coordinates
c       congruent with those of tilt series
c       
      if (recReproj) then
        call ialorg(2, 0., 0., 0.)
        outilt(1) = 0.
        call ialtlt(2,outilt)
      else if(perp)then
        outilt(1)=sign(90,idelslice)
        if (adjustOrigin) then
c           
c           Full adjustment if requested
          origx = origx  - delta(1) * (npxyz(1) / 2 - iwide / 2 - xoffset)
          origz = origy - delta(1) * float(sign(max(0,islice-1),idelslice))
          if (minTotSlice .gt. 0 .and. islice .le. 0)
     &        origz = origy - delta(1) * (minTotSlice-1)
          origy = delta(1) * (ithick / 2 + yoffset)
        else
c           
c           Legacy origin.  All kinds of wrong.
          origx = cell(1)/2.+delxx
          origy = cell(2)/2.
          origz = float(sign(max(0,islice-1),-idelslice))
        endif

        if (.not.projModel) then
          call ialorg(2,origx, origy, origz)
          call ialtlt(2,outilt)
        endif
      endif
c       
c       Initialize parallel writing routines if bound file entered
      ierr = parWrtInitialize(boundFile, 5, noxyz(1), noxyz(2), noxyz(3))
      if (ierr. ne. 0) then
        write(*,'(a,i3)')'ERROR: TILT - INITIALIZING PARALLEL WRITE '//
     &      'BOUNDARY FILE, ERROR',ierr
        call exit(1)
      endif
c       
c       chunk mode starter run: write header and exit
c
      if (.not.projModel) then
        if (minTotSlice .gt. 0 .and. ((.not.recReproj .and. islice .le. 0) .or.
     &      (recReproj .and. minZreproj .le. 0))) then
          CALL IWRHDR(2,TITLE,1,PMIN,PMAX,PMEAN)
          CALL IMCLOSE(2)
          print *,'Exiting after setting up output file for chunk writing'
          call exit(0)
        endif
        if (minTotSlice .gt. 0 .and. .not.recReproj) then
          call parWrtPosn(2, islice - minTotSlice, 0)
          if (readBase .and. .not. recReproj)
     &        call imposn(3, islice - minTotSlice, 0)
        endif
      endif
c
c       If reprojecting, need to look up each angle in full list of angles and
c       find ones to interpolate from, then pack data into arrays that are
c       otherwise used for packing these factors down
      if (recReproj) then
        do i = 1, nreproj
          sbet(i) = angReproj(i)
          call lookupAngle(angReproj(i), angles, nviews, ind1, ind2, frac)
          cbet(i) = (1. - frac) * compress(ind1) + frac * compress(ind2)
          sal(i) = (1. - frac) * alpha(ind1) + frac * alpha(ind2)
          cal(i) = (1. - frac) * xzfac(ind1) + frac * xzfac(ind2)
          array(i) = (1. - frac) * yzfac(ind1) + frac * yzfac(ind2)
          array(i+nviews) = (1. - frac)*expWeight(ind1) + frac*expWeight(ind2)
        enddo
c         
c         Do the same thing with all the local data: pack it into the spot at
c         the top of the local data then copy it back into the local area
        if (nxwarp .gt. 0) then
          do i=1,nxwarp*nywarp
            do iv=1,nreproj
              call lookupAngle(angReproj(iv), angles, nviews, ind1, ind2, frac)
              ind1 = ind1 + indwarp(i)
              ind2 = ind2 + indwarp(i)
              delbeta(indbase+iv) = (1.-frac)*delbeta(ind1) +
     &            frac*delbeta(ind2)
              delalpha(indbase+iv) = (1.-frac)*delalpha(ind1) + 
     &            frac*delalpha(ind2)
              warpxzfac(indbase+iv) = (1.-frac)*warpxzfac(ind1) +
     &            frac*warpxzfac(ind2)
              warpyzfac(indbase+iv) = (1.-frac)*warpyzfac(ind1) +
     &            frac*warpyzfac(ind2)
              do j = 1, 2
                do k = 1, 3
                  fw(j,k,indbase+iv) = (1.-frac)*fw(j,k,ind1) +
     &                frac*fw(j,k,ind2)
                enddo
              enddo
            enddo
            do iv=1,nreproj
              ind1 = indbase+iv
              ind2 = indwarp(i) + iv
              delbeta(ind2) = delbeta(ind1)
              delalpha(ind2) = delalpha(ind1)
              warpxzfac(ind2) = warpxzfac(ind1)
              warpyzfac(ind2) = warpyzfac(ind1)
              do j = 1, 2
                do k = 1, 3
                  fw(j,k,ind2) = fw(j,k,ind1)
                enddo
              enddo
            enddo
          enddo
        endif
c         
c         Replace the mapuse array
        nvuse = nreproj
        do i = 1, nvuse
          mapuse(i) = i
        enddo
      else
c       
c       pack angles and other data down as specified by MAPUSE
c       Negate the z factors since things are upside down here
c       Note that local data is not packed but always referenced by mapuse
c       
        do i=1,nvuse
          sbet(i)=angles(mapuse(i))
          cbet(i)=compress(mapuse(i))
          sal(i)=alpha(mapuse(i))
          cal(i)=xzfac(mapuse(i))
          array(i)=yzfac(mapuse(i))
          array(i+nviews) = expWeight(mapuse(i))
        enddo
      endif
      do i=1,nvuse
        angles(i)=sbet(i)
        compress(i)=cbet(i)
        alpha(i)=sal(i)
        xzfac(i)=-cal(i)
        yzfac(i)=-array(i)
        expWeight(i) = array(i+nviews)
      enddo
      nvorig=nviews
      nviews=nvuse
c       
      WRITE(6,51)(ANGLES(NV),NV=1,NVIEWS)
      WRITE(6,52)
c       
c       Turn off cosine stretch for high angles
      if (angles(1) .lt. -80. .or. angles(nviews) .gt. 80.) then
        if (interpfac .gt. 0) write(*,662)
662     format(/,'Tilt angles are too high to use cosine stretching')
        interpfac = 0
        interpfbp = 0
      endif
c       
C       Set up trig tables - including all of the replications
c       Then convert angles to radians
c       
      do irep=1,nreplic
        DO  NV=1,NVIEWS
          iv=NV+(irep-1)*nviews
          thetanv=ANGLES(NV)+DELANG+repinc(irep)
          if(thetanv.gt.180.)thetanv=thetanv-360.
          if(thetanv.le.-180.)thetanv=thetanv+360.
          CBET(iv)=COS(thetanv*DTOR)
C           Take the negative of the sine to invert slices around new X
c           axis, unless slices are being output in inverse order
          SBET(iv)=sign(1,-idelslice)*SIN(thetanv*DTOR)
          cal(iv)=cos(alpha(nv)*dtor)
          sal(iv)=sign(1,-idelslice)*sin(alpha(nv)*dtor)
        enddo
      enddo
      do iv=1,nviews
        angles(iv)=dtor*sign(1,-idelslice)*(angles(iv)+delang)
      enddo
c       
c       If there are weighting angles, convert those the same way, otherwise
c       copy the main angles to weighting angles
      if (numWgtAngles .gt. 0) then
        do iv=1,numWgtAngles
          wgtAngles(iv)=dtor*sign(1,-idelslice)*(wgtAngles(iv)+delang)
        enddo
      else
        numWgtAngles = nviews
        do iv=1,nviews
          wgtAngles(iv)=angles(iv)
        enddo
      endif
c       
c       if fixed x axis tilt, set up to try to compute vertical planes
c       and interpolate output planes: adjust thickness that needs to 
c       be computed, and find number of vertical planes that are needed
c       
      if (ifZfac .gt. 0 .and. ifalpha .eq. 0) ifalpha = 1
      ithickout=ithick
      if(ifalpha.eq.1.and.nxwarp.eq.0.and.intordxtilt.gt.0 .and.
     &    ifZfac .eq. 0 .and. .not.recReproj)then
        ifalpha=-1
        ithickout=ithick
        ithick=ithick/cal(1)+4.5
        nvertneed=ithickout*abs(sal(1))+5.
      endif
C       
C       Set center of output plane and center of input for transformations
c       Allow the full size to be less than the aligned stack, with a negative
c       subset start
c       
      if (nxfull .eq. 0) nxfull = npxyz(1)
      if (nyfull .eq. 0) nyfull = npxyz(2)
      xcenin=nxfull/2.+0.5-ixsubset
      slicen=nyfull/2.+0.5-iysubset
      xoffAdj=xoffset-(npxyz(1)/2+ixsubset-nxfull/2)
      XCEN=IWIDE/2+0.5+delxx+xoffAdj
      YCEN=ITHICK/2+0.5+yoffset
c       
c       if doing warping, convert the angles to radians and set sign
c       Also cancel the z factors if global entry was not made
c       
      if(nxwarp.gt.0)then
        do i=1,nvorig*nxwarp*nywarp
          delbeta(i)=dtor*sign(1,-idelslice)*delbeta(i)
        enddo
        do iv=1,nviews
          do i=1,nxwarp*nywarp
            ind=indwarp(i)+mapuse(iv)
            cwarpb(ind)=cos(angles(iv)+delbeta(ind))
            swarpb(ind)=sin(angles(iv)+delbeta(ind))
            cwarpa(ind)=cos(dtor*(alpha(iv)+delalpha(ind)))
            swarpa(ind)=sign(1,-idelslice)*
     &          sin(dtor*(alpha(iv)+delalpha(ind)))
            if (ifZfac .eq. 0) then
              warpxzfac(ind)= 0.
              warpyzfac(ind)= 0.
            endif
          enddo
        enddo
c         
c         See if local scale was entered; if not see if it can be set from
c         pixel size and local align pixel size
        if (scalelocal .le. 0.) then
          scalelocal = 1.
          if (pixelLocal .gt. 0) then
            scalelocal = pixelLocal / delta(1)
            if (abs(scalelocal - 1.).gt.0.001) write(6,53)scaleLocal
          endif
        endif
c         
c         scale the x and y dimensions and shifts if aligned data were
c         shrunk relative to the local alignment solution
c         10/16/04: fixed to use mapuse to scale used views properly
c         
        if(scalelocal.ne.1.)then
          ixswarp=nint(ixswarp*scalelocal)
          iyswarp=nint(iyswarp*scalelocal)
          idxwarp=nint(idxwarp*scalelocal)
          idywarp=nint(idywarp*scalelocal)
          do iv=1,nviews
            do i=1,nxwarp*nywarp
              ind=indwarp(i)+mapuse(iv)
              fw(1,3,ind)=fw(1,3,ind)*scalelocal
              fw(2,3,ind)=fw(2,3,ind)*scalelocal
            enddo
          enddo
        endif
c         
c         if the input data is a subset in X or Y, subtract starting
c         coordinates from ixswarp and iyswarp
c         
        ixswarp=ixswarp-ixsubset
        iyswarp=iyswarp-iysubset
      endif
c       
c       Here is the place to project model points and exit
      if (projModel) call projectModel(filout, delta, nvorig, array,
     &    array(limstack/2))
c       
c       If reprojecting, set the pointers and return
      if (recReproj) then
        fastbp = .false.
        imap = 1
        minXload = minXreproj
        maxXload = maxXreproj
        if (nxwarp .ne. 0) then
          minXload = max(1, minXload - 100)
          maxXload = min(nrxyz(1), maxXload + 100)
        endif
        iwideReproj = maxXload + 1 - minXload
        ithwid = iwideReproj * ithickReproj
        iplane = ithwid
        if (nxwarp .ne. 0) iplane = ithwid + 4 * iwideReproj
c         
c         Get projection offsets and replace the slice limits
        xprjOffset = minXreproj - 1 + npxyz(1) / 2 - iwide / 2 - xoffset
        yprjOffset = minZreproj + islice - 2
        islice = minZreproj
        jslice = maxZreproj
        iwide = noxyz(1)
        mprj = nrxyz(3)
        pmean = (pmean / scale - flevl) / filterScale
        nbase = iwide + 1
        ipextra = 0
        npad = 0
        if (debug) print *,'scale: ', scale,flevl
        return
      endif
c       
c       If reading base, figure out total views being added and adjust scales
      if (readBase) then
        iv = nviews
        do j = 1, nviews
          k = 0
          do i = 1, nViewSubtract
            if (ivSubtract(i) .eq. 0 .or. mapuse(j) .eq. ivSubtract(i)) k = 1
          enddo
          iv = iv - 2 * k
        enddo
        baseScale = scale / (numViewBase * nreplic)
        baseFlevl = flevl * (numViewBase * nreplic)
        scale = scale / ((iv + numViewBase) * nreplic)
        flevl = flevl * ((iv + numViewBase) * nreplic)
        if (debug)  print *,'base: ', baseScale,baseFlevl
      else
        scale=scale/(nviews*nreplic)
        flevl=flevl*nviews*nreplic
      endif
      if (debug) print *,'scale: ', scale,flevl
c       
c       determine if fast bp can be used
c       
      fastbp=interpfbp.ne.0
      if(fastbp)then
        fastbp=licenseusfft().eq.0
        if(.not.fastbp)then
          write(*,'(/,a,/,a,/)')' No fast back projection: No '//
     &        'license to use USFFT libraries',
     &        ' available or specified'
        else
          fastbp=ifalpha.le.0.and.nxwarp.eq.0
          if(.not.fastbp)then
            write(*,'(/,a,/,a,/)')' No fast back projection is available'
     &          //' with old-style X-tilt,',' Z factors, or local alignments'
          else
            fastbp=.not.mask.and.delxx.eq.0..and.nreplic.eq.1.and.
     &          ncompress.eq.0.and.iwide.le.npxyz(1).and.xoffAdj.eq.0.
     &           .and..not.readBase
            if(.not.fastbp)write(*,'(/,a,/,a,/)')' No fast back '//
     &          'projection is available with shift, offset, mask,'
     &          //' replication',' base rec or compression options, or if'//
     &          ' output width > input width'
          endif
        endif
      endif
c       
c       Next check whether dimensions are below cutoff, unless the user has
c       entered an fbpinterp line
c       
      if(fastbp.and.interpfbp.lt.0)then
        do i=1,nfbplimits
          if (iwide.lt.limitfbp(1,i).and.ithick.lt.limitfbp(2,i)
     &        .and.nviews.lt.limitfbp(3,i)) fastbp = .false.
        enddo
        if(.not.fastbp)write(*,'(/,a,/,a,/)')' Fast back-projection'
     &      //' will not be used because it would probably be slower',
     &      ' for these values of width, thickness, and number'//
     &      ' of views'
      endif
      interpfbp = abs(interpfbp)
c       
c       If qualify for fast bp, set up working arrays and make sure there
c       is enough space.  Have to compute here items that are ordinarily
c       computed in main program
c       
      if(fastbp)then

        if(iwide.lt.nprj)then
c           
c           if subset in width, find out the min and max coordinates
c           actually needed and set the effective nprj for fbp from that
c           
          call fbpneed(angles,nprj,nviews,iwide,ithick, 1,
     &        array,array(1+3*nviews),nprjp,nwidep,needwrk,needzwrk,
     &        neediw,needrw,needzw, needout,minsup,maxsup)
          nshift=(nprj-(maxsup+1-minsup))/2
          nprjfbp=nprj-2*nshift
        else
          nprjfbp=nprj
        endif

        call fbpneed(angles,nprjfbp,nviews,iwide,ithick, 1,
     &      array,array(1+3*nviews),nprjp,nwidep,needwrk,needzwrk,
     &      neediw,needrw,needzw, needout,minsup,maxsup)

        fastbp=nprjp.gt.0
        if(fastbp)then
          npad=max(0,nprjp-nprj)
          nprj2=nprj+npad
          IPLANE=NPRJ2*NVIEWS
c           
          ifbpwrk=1
          ifbpzwrk=ifbpwrk + needwrk
          ifbpiw=ifbpzwrk + 2 * needzwrk
          ifbprw=ifbpiw + neediw
          ifbpzw=ifbprw + needrw
          imap=ifbpzw + 2 * needzw
          if(ifalpha.eq.0)then
            NBASE=IMAP+needout
          else
            nbase=imap+needout+nvertneed*iwide*ithick
          endif
          ithwid=needout
          fastbp=(limstack+1-nbase).ge.iplane
          if(.not.fastbp)write(*,'(/a/)')' Stack is not large enough'
     &        //' to use fast back projection'
        else
          write(*,'(/a/)')' No fast back projection: This program '//
     &        'was not built with USFFT libraries'
        endif

      endif
c       
c       if doing fast bp, get filter parameters, set sign, and initialize now
c       
      if(fastbp)then
        rmax=irmax/float(nprj)
        sdg=ifall/float(nprj)

c         uncomment to get outputs from fbpini
c         call prini(6,0) 
c         call getini 
        oversamp=float(nwidep)/iwide
        write(*,'(/,a,f5.2,/)')' Using fast back projection with '//
     &      'oversampling of',oversamp

        call fbpini(angles,nprjfbp,nviews,iwide,ithick, rmax,sdg,
     &      interpfbp,1,
     &      array(ifbpiw),array(ifbprw),array(ifbpzw),array(ifbpwrk))
        scalescl=1000.
        flevl=flevl/scalescl
        scale=scale*scalescl
      else

c         
c         Set up padding: 10% of X size or minimum of 16, max of 50
        npadtmp=min(50,2*max(8,npxyz(1)/20))
c        npadtmp = 2 * npxyz(1)
        nprpad=niceframe(2*((npxyz(1)+npadtmp)/2),2,19)
        npad=nprpad-npxyz(1)
        ITHWID=IWIDE*ITHICK
c         
c         next evaluate cosine stretch
c         
        ipextra=0
        NPRJ2=NPRJ+2+npad
        IPLANE=NPRJ2*NVIEWS
        IMAP=IPLANE+1
        if(ifalpha.ge.0)then
C           
C           anything but new-style X-axis tilt
C           
          NBASE=IMAP+ITHWID
          if(nxwarp.eq.0.and.interpfac.gt.0)then
            call set_cos_stretch(nsneed)
c             
c             set size of plane as max of loading size and stretched size
c             also set that an extra plane is needed
c             if there is not enough space for the planes needed, then
c             disable stretching and drop back to regular code
c             
            iplane=max(iplane,indstretch(nviews+1))
            ipextra=iplane
            if((maxSTACK-NBASE-ipextra+1)/IPLANE.lt.nsneed)then
              ipextra=0
              iplane=nprj2*nviews
              interpfac=0
              write(*,62)
62            format(/,'There is insufficient stack space ',
     &            'to use cosine stretching')
            endif
          endif

        else
c           
c           new-style X-axis tilt
c           
          nbase=imap+ithwid*(nvertneed+1)
          ipextra=0
          nsneed = 1
          ithicknew = ithick
          nocosPlanes = (maxStack-nbase+1) / iplane
c           print *,'nocosplanes',nocosplanes
c           
c           find out what cosine stretch adds if called for
c           
          if(interpfac.gt.0)then
            call set_cos_stretch(nsneed)
            iplane=max(iplane,indstretch(nviews+1))
            ipextra=iplane
          endif
c           
c           Does everything fit?  If not, drop back to old style tilting
c           
          if((maxSTACK-NBASE-ipextra+1)/IPLANE.lt.nsneed)then
            ifalpha=1
            ithick=ithickout
            ithwid=iwide*ithick
            YCEN=ITHICK/2+0.5+yoffset
            NBASE=IMAP+ITHWID
c             
c             re-evaluate cosine stretch with these conditions
c             
            ipextra=0
            iplane=nprj2*nviews
            if(interpfac.gt.0)then
              call set_cos_stretch(nsneed)
              iplane=max(iplane,indstretch(nviews+1))
              ipextra=iplane
c               print *,iplane,nsneed,maxSTACK-NBASE-ipextra+1
              if((maxSTACK-NBASE-ipextra+1)/IPLANE.lt.nsneed)then
c                 
c                 cosine stretch won't work.  Can we restore new-style?
c                 
                ipextra=0
                iplane=nprj2*nviews
                interpfac=0
                write(*,62)
                if (nocosPlanes.gt.0) then
                  ifalpha=-1
                  ithick=ithicknew
                  ithwid=iwide*ithick
                  YCEN=ITHICK/2+0.5+yoffset
                  NBASE=IMAP+ITHWID
                endif
              endif
            endif
          endif
          if (ifalpha .gt. 0)write(*,'(/,a)')'Insufficient stack space '//
     &        'for new-style X-axis tilting'
        endif
c         print *,interpfac,ipextra,ifalpha,nvertneed
c         
C         Set up radial weighting
        CALL RADWT(IRMAX,IFALL)
      endif
      RETURN
C       
2410  call exitError('READING LOCAL TILT ALIGNMENT DATA FROM FILE')
2411  call exitError('READING TILT ANGLES FROM FILE')
2412  call exitError('READING X-AXIS TILT ANGLES FROM FILE')
2413  call exitError('READING Z FACTORS FROM FILE')
2414  call exitError('READING WEIGHTING FACTORS FROM FILE')
2415  call exitError('READING ANGLES FOR WEIGHTING FROM FILE')
C       
C       
48    FORMAT(//,1X,78('-'))
49    FORMAT('TILT: ',a,t57,A9,2X,A8)
50    FORMAT(
     &    //,' THREE-D RECONSTRUCTION FROM SERIES OF PROJECTIONS '
     &    ,' ABOUT A COMMON TILT AXIS'/,1x,76('-')///)
51    FORMAT(/' Projection angles:'//(8F9.2))
52    FORMAT(//,1X,78('-'))
53    format(/,'Scaling of local alignments by ',f8.3,
     &    ' determined from pixel sizes')
101   FORMAT(/' Title:    ',20A4)
201   FORMAT(/' Rows',I4,' to',I4,', (at intervals of',i4,') of the'
     &    ,' projection planes will be reconstructed.')
301   FORMAT(/' Thickness of reconstructed slice is',I4,
     &    ' divisions.')
401   FORMAT(/' Mask applied to output slices:'
     &    ,' points lying outside the reconstructed domain set to',
     &    F6.0)
501   FORMAT(/' Radial weighting function parameters IRMAX =',I4,
     &    '  IWIDE =',I4)
601   FORMAT(/' Output map rotated by',F6.1,' degrees about tilt axis',
     &    ' with respect to tilt origin'/
     &    ' Tilt axis displaced by',F6.3,' grid units from centre'
     &    ,' of projection')
701   FORMAT(/' Output map densities incremented by',F8.2,
     &    ' and then multiplied by',F8.2)
801   FORMAT(/' Output map is sectioned perpendicular to the '
     &    ,'tilt axis')
901   FORMAT(/' Output map is sectioned parallel to the'
     &    ,' zero tilt projection')
1001  format(/' Data mode of output file is',i3)
1301  format(/' Taking logarithm of input data plus',f10.3)
1401  format(/' Replicate projection',i4,' times by',f6.1,' degrees')
1701  format(/' Compression was confined to',f6.3,
     &    ' of the distance over which it was measured')
1801  format(/' Weighting by tilt density computed to distance of'
     &    ,i3,' views',/,'  weighting factors:',(10f6.3))
1802  format(/' No weighting by tilt density')
2001  format(/,' Width of reconstruction is',i5,' pixels')
2101  format(/,' Output slice shifted up',f7.1,' and to right',f7.1,
     &    ' pixels')
2201  format(/,' Alpha tilting to be applied with angles from file')
2301  format(/,' Global alpha tilt of',f6.1,' will be applied')
2401  format(/,' Local tilt alignment information read from file')
2501  format(/,' Local alignment positions and shifts reduced by',f7.4)
2601  format(/,' Full aligned stack will be assumed to be',i6,' by',
     &    i6,' pixels')
2701  format(/,' Aligned stack will be assumed to be a subset ',
     &    'starting at',2i6)
2801  format(/,' Cosine stretching, if any, will have interpolation',
     &    ' order', i2,', sampling factor',i2)
2901  format(/,' Fast back projection, if any, will have ',
     &    'interpolation order', i2)
3001  format(/,' X-tilting with vertical slices, if any, will have ',
     &    'interpolation order', i2)
3101  format(/,' Output will be one or more reprojections')
3201  format(/,' Z-dependent shifts to be applied with factors from file'
     &    )
3301  format(/,' Dimensions and coordinates will be scaled down by a ',
     &    'factor of ',i2)
3401  format(/,' Computed slices are part of a total volume from slice',
     &    i6,' to',i6)
      END

      subroutine lookupAngle(proj, angles, nviews, ind1, ind2, frac)
      implicit none
      real*4 proj, angles(*), frac
      integer*4 nviews, ind1, ind2, i
      ind1 = 0
      ind2 = 0
      do i = 1, nviews
        if (proj .ge. angles(i)) then
          if (ind1 .eq. 0) ind1 = i
          if (angles(i) .gt. angles(ind1)) ind1 = i
        else
          if (ind2 .eq. 0) ind2 = i
          if (angles(i) .lt. angles(ind2)) ind2 = i
        endif
      enddo
      frac = 0.
      if (ind1 .eq. 0) then
        ind1 = ind2
      elseif (ind2 .eq. 0) then
        ind2 = ind1
      else
        frac = (proj - angles(ind1)) / (angles(ind2) - angles(ind1))
      endif
      return
      end
      
c       return indices to four local areas, and fractions to apply for each
c       at location ix, iy in view iv, where ix and iy are indexes in
c       the reconstruction adjusted to match coordinates of projections
c
      subroutine local_factors(ix,iy,iv,ind1,ind2,ind3,ind4,f1,f2,f3,f4)
c       
      implicit none
      include 'tilt.inc'
      integer*4 ix,iy,iv,ind1,ind2,ind3,ind4,ixt,ixpos,iyt,iypos
      real*4 f1,f2,f3,f4,fx,fy
c       
      ixt=min(max(ix-ixswarp,0),(nxwarp-1)*idxwarp)
      ixpos=min(ixt/idxwarp+1,nxwarp-1)
      fx=float(ixt-(ixpos-1)*idxwarp)/idxwarp
      iyt=min(max(iy-iyswarp,0),(nywarp-1)*idywarp)
      iypos=min(iyt/idywarp+1,nywarp-1)
      fy=float(iyt-(iypos-1)*idywarp)/idywarp

      ind1=indwarp(nxwarp*(iypos-1)+ixpos)+iv
      ind2=indwarp(nxwarp*(iypos-1)+ixpos+1)+iv
      ind3=indwarp(nxwarp*iypos+ixpos)+iv
      ind4=indwarp(nxwarp*iypos+ixpos+1)+iv
      f1=(1.-fy)*(1.-fx)
      f2=(1.-fy)*fx
      f3=fy*(1.-fx)
      f4=fy*fx
      return
      end


c       Compute local projection factors at a position in a column for view iv:
c       j is X index in the reconstruction, lslice is slice # in aligned stack
c
      subroutine localProjFactors(j, lslice, iv, xprojf, xprojz, yprojf,
     &    yprojz)
      implicit none
      include 'tilt.inc'
      integer*4 j, lslice, iv
      real*4 xprojf, xprojz, yprojf, yprojz
      integer*4 ind1,ind2,ind3,ind4,ixc
      real*4 f1,f2,f3,f4,xx,yy
      real*4 calf,salf,a11,a12,a21,a22,xadd,yadd,xalladd,yalladd
      real*4 calf2,salf2,a112,a122,a212,a222,xadd2,yadd2
      real*4 calf3,salf3,a113,a123,a213,a223,xadd3,yadd3
      real*4 calf4,salf4,a114,a124,a214,a224,xadd4,yadd4
      real*4 f1x,f2x,f3x,f4x,f1xy,f2xy,f3xy,f4xy
      real*4 f1y,f2y,f3y,f4y,f1yy,f2yy,f3yy,f4yy
      real*4 xp1f,xp1z,yp1f,xp2f,xp2z,yp2f,xp3f,xp3z,yp3f,xp4f,xp4z,yp4f
      real*4 cbeta,sbeta,cbeta2,sbeta2,cbeta3,sbeta3,cbeta4,sbeta4
c       
c       get transform and angle adjustment
c       
      ixc=nint(j-xcen+xcenin+delxx)
      call local_factors(ixc,lslice,mapuse(iv),ind1,ind2,ind3,ind4,f1,f2,f3,f4)
c       
c       get all the factors needed to compute a projection position
c       from the four local transforms
c       
      cbeta=cwarpb(ind1)
      sbeta=swarpb(ind1)
      calf=cwarpa(ind1)
      salf=swarpa(ind1)
      a11=fw(1,1,ind1)
      a12=fw(1,2,ind1)
      a21=fw(2,1,ind1)
      a22=fw(2,2,ind1)
      xadd=fw(1,3,ind1)+xcenin-xcenin*a11-slicen*a12
      yadd=fw(2,3,ind1)+slicen-xcenin*a21-slicen*a22
c       
      cbeta2=cwarpb(ind2)
      sbeta2=swarpb(ind2)
      calf2=cwarpa(ind2)
      salf2=swarpa(ind2)
      a112=fw(1,1,ind2)
      a122=fw(1,2,ind2)
      a212=fw(2,1,ind2)
      a222=fw(2,2,ind2)
      xadd2=fw(1,3,ind2)+xcenin-xcenin*a112-slicen*a122
      yadd2=fw(2,3,ind2)+slicen-xcenin*a212-slicen*a222
c       
      cbeta3=cwarpb(ind3)
      sbeta3=swarpb(ind3)
      calf3=cwarpa(ind3)
      salf3=swarpa(ind3)
      a113=fw(1,1,ind3)
      a123=fw(1,2,ind3)
      a213=fw(2,1,ind3)
      a223=fw(2,2,ind3)
      xadd3=fw(1,3,ind3)+xcenin-xcenin*a113-slicen*a123
      yadd3=fw(2,3,ind3)+slicen-xcenin*a213-slicen*a223
c       
      cbeta4=cwarpb(ind4)
      sbeta4=swarpb(ind4)
      calf4=cwarpa(ind4)
      salf4=swarpa(ind4)
      a114=fw(1,1,ind4)
      a124=fw(1,2,ind4)
      a214=fw(2,1,ind4)
      a224=fw(2,2,ind4)
      xadd4=fw(1,3,ind4)+xcenin-xcenin*a114-slicen*a124
      yadd4=fw(2,3,ind4)+slicen-xcenin*a214-slicen*a224
c       
      f1x=f1*a11
      f2x=f2*a112
      f3x=f3*a113
      f4x=f4*a114
      f1xy=f1*a12
      f2xy=f2*a122
      f3xy=f3*a123
      f4xy=f4*a124
c       fxfromy=f1*a12+f2*a122+f3*a123+f4*a124
      f1y=f1*a21
      f2y=f2*a212
      f3y=f3*a213
      f4y=f4*a214
      f1yy=f1*a22
      f2yy=f2*a222
      f3yy=f3*a223
      f4yy=f4*a224
c       fyfromy=f1*a22+f2*a222+f3*a223+f4*a224
      xalladd=f1*xadd+f2*xadd2+f3*xadd3+f4*xadd4
      yalladd=f1*yadd+f2*yadd2+f3*yadd3+f4*yadd4
c       
c       Each projection position is a sum of a fixed factor ("..f")
c       and a factor that multiplies z ("..z")
c       
      xx=j-xcen
      yy=lslice-slicen
      xp1f=xx*cbeta + yy*salf*sbeta + xcenin+delxx
      xp1z=calf*sbeta + warpxzfac(ind1)
      xp2f=xx*cbeta2 + yy*salf2*sbeta2 + xcenin+delxx
      xp2z=calf2*sbeta2 + warpxzfac(ind2)
      xp3f=xx*cbeta3 + yy*salf3*sbeta3 + xcenin+delxx
      xp3z=calf3*sbeta3 + warpxzfac(ind3)
      xp4f=xx*cbeta4 + yy*salf4*sbeta4 + xcenin+delxx
      xp4z=calf4*sbeta4 + warpxzfac(ind4)

      yp1f=yy*calf + slicen
      yp2f=yy*calf2 + slicen
      yp3f=yy*calf3 + slicen
      yp4f=yy*calf4 + slicen
c       
c       store the fixed and z-dependent component of the
c       projection coordinates
c       
      xprojf=f1x*xp1f+f2x*xp2f+f3x*xp3f+f4x*xp4f+
     &    f1xy*yp1f+f2xy*yp2f+f3xy*yp3f+f4xy*yp4f+xalladd
      xprojz=f1x*xp1z+f2x*xp2z+f3x*xp3z+f4x*xp4z-
     &    (f1xy*(salf-warpyzfac(ind1))+f2xy*(salf2-warpyzfac(ind2))+
     &    f3xy*(salf3-warpyzfac(ind3))+f4xy*(salf4-warpyzfac(ind4)))
      yprojf=f1y*xp1f+f2y*xp2f+f3y*xp3f+f4y*xp4f+
     &    f1yy*yp1f+f2yy*yp2f+f3yy*yp3f+f4yy*yp4f+yalladd
      yprojz=f1y*xp1z+f2y*xp2z+f3y*xp3z+f4y*xp4z-
     &    (f1yy*(salf-warpyzfac(ind1))+f2yy*(salf2-warpyzfac(ind2))+
     &    f3yy*(salf3-warpyzfac(ind3))+f4yy*(salf4-warpyzfac(ind4)))
      return
      end


c       This is the former code for assessing backprojection positions, 
c       new method using localProjFactors verified to give the same result
c       But this shows how the BP position can be computed directly
c$$$      call local_factors (ixsam,itry,mapuse(iv),ind1,ind2,
c$$$     &    ind3, ind4, f1,f2,f3,f4)
c$$$c       
c$$$c       for each position, find back-projection location
c$$$c       transform if necessary, and use to get min and
c$$$c       max slices needed to get this position
c$$$c       
c$$$      xx=ixsam-xcen
c$$$      yy=itry-slicen
c$$$      zz=iy-ycen
c$$$c       Global bp position
c$$$      xp=xx*cbet(iv)+yy*sal(iv)*sbet(iv)+ zz*(cal(iv)*sbet(iv)+xzfac(iv))+
c$$$     &    xcenin+delxx
c$$$      yp=yy*cal(iv)-zz*(sal(iv)-yzfac(iv))+slicen
c$$$c       Local position:
c$$$      xp=xx*cwarpb(ind1)+yy*swarpa(ind1)*swarpb(ind1)+
c$$$     &    zz*(cwarpa(ind1)*swarpb(ind1)+warpxzfac(ind1))+ xcenin+delxx
c$$$      yp=yy*cwarpa(ind1)- zz*(swarpa(ind1)-warpyzfac(ind1))+slicen
c$$$      call xfapply(fw(1,1,ind1),xcenin,slicen,xp,yp, xp,yp)
c$$$      xp2=xx*cwarpb(ind2)+yy*swarpa(ind2)*swarpb(ind2)+
c$$$     &    zz*(cwarpa(ind2)*swarpb(ind2)+warpxzfac(ind2))+ xcenin+delxx
c$$$      yp2=yy*cwarpa(ind2)- zz*(swarpa(ind2)-warpyzfac(ind2))+slicen
c$$$      call xfapply(fw(1,1,ind2),xcenin,slicen,xp2,yp2, xp2,yp2)
c$$$      xp3=xx*cwarpb(ind3)+yy*swarpa(ind3)*swarpb(ind3)+
c$$$     &    zz*(cwarpa(ind3)*swarpb(ind3)+warpxzfac(ind3))+ xcenin+delxx
c$$$      yp3=yy*cwarpa(ind3)- zz*(swarpa(ind3)-warpyzfac(ind3))+slicen
c$$$      call xfapply(fw(1,1,ind3),xcenin,slicen,xp3,yp3, xp3,yp3)
c$$$      xp4=xx*cwarpb(ind4)+yy*swarpa(ind4)*swarpb(ind4)+
c$$$     &    zz*(cwarpa(ind4)*swarpb(ind4)+warpxzfac(ind4))+ xcenin+delxx
c$$$      yp4=yy*cwarpa(ind4)- zz*(swarpa(ind4)-warpyzfac(ind4))+slicen
c$$$      call xfapply(fw(1,1,ind4),xcenin,slicen,xp4,yp4, xp4,yp4)
c$$$      xp=f1*xp+f2*xp2+f3*xp3+f4*xp4
c$$$      yp=f1*yp+f2*yp2+f3*yp3+f4*yp4


c       Finds the point at centered Z coordinate zz projecting to
c       xproj, yproj in view iv of original projections.  xx is X index in
c       reconstruction, yy is slice number in original projections
c                        
      subroutine findProjectingPoint(xproj, yproj, zz, iv, xx, yy)
      implicit none
      include 'tilt.inc'
      real*4 xproj, yproj, zz, xx, yy
      integer*4 iv, iter, ifdone, ixassay, iyassay
      real*4 xprojf11, xprojz11, yprojf11, yprojz11, xprojf21, xprojz21,
     &    yprojf21, yprojz21, xprojf12, xprojz12, yprojf12, yprojz12
      real*4 xp11, yp11, xp12, yp12, xp21, yp21, xerr, yerr, dxpx, dxpy, dypx
      real*4 dypy, fx, fy, den
      integer*4 myFloor
c
      iter = 1
      ifdone = 0
      do while (ifdone .eq. 0 .and. iter .le. 5)
        ixassay = myFloor(xx)
        iyassay = myFloor(yy)
        call localProjFactors(ixassay, iyassay, iv, xprojf11, xprojz11,
     &      yprojf11, yprojz11)
        call localProjFactors(ixassay + 1, iyassay, iv, xprojf21, xprojz21,
     &      yprojf21, yprojz21)
        call localProjFactors(ixassay, iyassay + 1, iv, xprojf12, xprojz12,
     &      yprojf12, yprojz12)
        xp11 = xprojf11 + xprojz11 * zz
        yp11 = yprojf11 + yprojz11 * zz
        xp21 = xprojf21 + xprojz21 * zz
        yp21 = yprojf21 + yprojz21 * zz
        xp12 = xprojf12 + xprojz12 * zz
        yp12 = yprojf12 + yprojz12 * zz
        xerr = xproj - xp11
        yerr = yproj - yp11
        dxpx = xp21 - xp11
        dxpy = xp12 - xp11
        dypx = yp21 - yp11
        dypy = yp12 - yp11
        den = dxpx * dypy - dxpy * dypx
        fx = (xerr * dypy - yerr * dxpy) / den
        fy = (dxpx * yerr - dypx * xerr) / den
        xx = ixassay + fx
        yy = iyassay + fy
        if (fx .gt. -0.1 .and. fx.lt.1.1 .and. fy .gt. -0.1 .and. fy.lt.1.1)
     &      ifdone = 1
        iter = iter + 1
      enddo
      return
      end

      integer*4 function myFloor(xx)
      if (xx .lt. 0) then
        myFloor = xx - 1.
      else
        myFloor = xx
      endif
      return
      end

c       
c       Compute space needed for cosine stretched data, returning the
c       number of needed input slices in NSNEED
c       
      subroutine set_cos_stretch(nsneed)
      implicit none
      include 'tilt.inc'
      integer*4 nsneed,lsmin,lsmax,iv,ix,iy,lslice
      real*4 tanal,xpmax,xpmin,zz,zpart,yy,xproj
c       make the indexes be bases, numbered from 0
c       
      indstretch(1)=0
      lsmin=min(jslice,islice)
      lsmax=max(jslice,islice)
      nsneed=1
      if(ifalpha.lt.0)then
c         
c         New-style X tilting: SET MINIMUM NUMBER OF INPUT SLICES HERE
c         
        nsneed=4
        lsmin=slicen+(lsmin-slicen)*cal(1)+yoffset*sal(1)-
     &      0.5*ithickout*abs(sal(1))-1.
        lsmax=slicen+(lsmax-slicen)*cal(1)+yoffset*sal(1)+
     &      0.5*ithickout*abs(sal(1))+2.
        tanal=sal(1)/cal(1)
        lsmin=max(1,lsmin)
        lsmax=min(lsmax,mprj)
      endif
      do iv=1,nviews
        xpmax=1
        xpmin=nprj
c         
c         find min and max position of 8 corners of reconstruction
c         
        do ix=1,iwide,iwide-1
          do iy=1,ithick,ithick-1
            do lslice=lsmin,lsmax,max(1,lsmax-lsmin)
              ZZ=(IY-YCEN)*compress(iv)
              if(ifalpha.lt.0) zz=compress(iv)*
     &            (iy-(ycen-nint(tanal*(lslice-slicen))))
              if(ifalpha.le.0)then
                zPART=zz*SBET(iv)+XCENin+DELXX
              else
                yy=lslice-slicen
                zpart= yy*sal(iv)*sbet(iv) + zz*(cal(iv)*sbet(iv)+xzfac(iv))+
     &              xcenin+delxx
              endif
              xproj=zpart+(ix-xcen)*cbet(iv)
              xpmin=max(1.,min(xpmin,xproj))
              xpmax=min(float(nprj),max(xpmax,xproj))
            enddo
          enddo
        enddo
c         print *,iv,xpmin,xpmax
c         
c         set up extent and offset of stretches
c         
        ofstretch(iv)=xpmin/cbet(iv)-1./interpfac
        nstretch(iv)=interpfac*(xpmax-xpmin)/cbet(iv)+2.
        indstretch(iv+1)=indstretch(iv)+nstretch(iv)
c         print *,iv,xpmin,xpmax,ofstretch(iv),nstretch(iv),indstretch(iv)
        if(ifalpha.gt.0)nsneed=max(nsneed,
     &      int(ithick*(abs(sal(iv))+abs(yzfac(iv)))+2))
      enddo
      return
      end


      subroutine reproject(array, nxs, nys, nxout, sinang, cosang, xraystr,
     &    yraystr, nrayinc, nraymax, fill, projline)
      implicit none
      integer*4 nxs, nys, nxout, nrayinc(*), nraymax
      real*4 array(nxs, nys), xraystr(*), yraystr(*), fill, projline(*)
      integer*4 ixout, iray, ixr,iyr, nraypts, idir
      real*4 sinang, cosang, rayfac, rayadd, xray, yray, pixtmp
      real *4 dx, dy, v2, v4, v6, v8, v5, a, b, c, d
c       
      do ixout = 1, nxout
        projLine(ixout) = fill
        nraypts=nrayinc(ixout)
        if(nraypts.gt.0)then
          pixtmp = 0.
          if (sinang.ne. 0.)then
            do iray=0,nraypts-1
              xray=xraystr(ixout)+iray*sinang
              yray=yraystr(ixout)+iray*cosang
              ixr=nint(xray)
              iyr=nint(yray)
              dx=xray-ixr
              dy=yray-iyr
              v2=array(ixr, iyr - 1)
              v4=array(ixr - 1, iyr)
              v5=array(ixr, iyr)
              v6=array(ixr + 1, iyr)
              v8=array(ixr, iyr + 1)
C                     
              A = (V6 + V4)*.5 - V5
              B = (V8 + V2)*.5 - V5
              C = (V6 - V4)*.5
              D = (V8 - V2)*.5
              pixtmp = pixtmp + A*DX*DX + B*DY*DY + C*DX + D*DY + V5
            enddo
          else
c             
c             vertical projection
c
            ixr=nint(xraystr(ixout))
            iyr=nint(yraystr(ixout))
            idir = sign(1., cosang)
            do iray=0,nraypts-1
              pixtmp = pixtmp+ array(ixr, iyr + idir * iray)
            enddo
          endif

          rayfac=1./nraymax
          rayadd=rayfac * (nraymax - nraypts) * fill
          projLine(ixout) = rayfac * pixtmp + rayadd
        endif
      enddo
      return
      end


      subroutine reprojectRec(lsStart, lsEnd, inloadstr, inloadend, DMIN,DMAX,
     &    DTOT8)
      implicit none
      include 'tilt.inc'
      integer*4 lsStart, lsEnd, inloadstr, inloadend
      real*4 dmin, dmax, warpDelz(2 * limwpos)
      integer*4 iv, ix, iy, iz, ixp, line, i, numz, kz, iys, ixnd, ixst, ind
      integer*4 ind1, ind2, ind3, ind4, load
      real*4 calf, salf, cbeta, sbeta, delz, delx, znum, fz, omfz, zz, xx, fx
      real*4 omfx, yy, fy, omfy, xproj, yproj, yslice, d11, d12, d21, d22
      real*4 dxdelz, f1, f2, f3, f4, xxgood, yygood, zzgood
      real*4 ytol, xprojMin, xprojMax, xjump, zjump, dely,diffxmax,diffymax
      integer*4 indbase, nxload, ndelz, ixc, lastZdone, iter, ifdone, ifout
      integer*4 ijump, njump
      real*8 sum, dtot8
      logical*4 tryjump
      real*4 reprojDelz
      integer*4 myFloor

      ytol = 0.05
      xjump = 5.
      nxload = maxXload + 1 - minXload
      do iv = 1, nviews
        if (nxwarp .eq. 0) then
c           
c           Get the delta z for this view
          calf = cal(iv)
          salf = sal(iv)
          cbeta = cbet(iv)
          sbeta = sbet(iv)
          delz = reprojDelz(sbeta, cbeta, salf, calf, xzfac(iv), yzfac(iv))
c          print *,sbeta, cbeta, salf, calf, xzfac(iv), yzfac(iv)
          delx = 1. / cbeta
c          print *,delx, delz
          do line = lsStart, lsEnd
            do i = 0, iwide - 1
              array(imap + i) = 0.
            enddo
c             
            znum = (ithickReproj - 1) / delz
            numz = znum
            if (znum - numz .ge. 0.1) numz = numz + 1
            do kz = 1, numz
              zz = 1 + (kz - 1) * delz
              iz = zz
              fz = zz - iz
              omfz = 1. - fz
c               
c               If Z is past the top, drop back one line and set up fractions
c               to take just a fraction of the top line
              if (zz .ge. numz) then
                zz = numz
                iz = numz - 1
                fz = omfz
                omfz = 0.
              endif
              zz = zz + minYreproj - 1 - ycen
c               
c               Get y slice for this z value
              yproj = line + yprjOffset
              yy = (yproj + zz * (salf - yzfac(iv)) - slicen) / calf
              yslice = yy + slicen - yprjOffset
              if (ifalpha .eq. 0) yslice = line
c              if (line.eq.591)print *,kz,zz,iz,fz,omfz,yproj,yy,yslice
              if (yslice .lt. inloadstr - ytol .or.
     &            yslice .gt. inloadend + ytol) then
c                 
c                 Really out of bounds, do fill
c                if (line.eq.591)print *,'Out of bounds, view, line, zz',iv, line, zz
                do i = 0, iwide - 1
                  array(imap + i) = array(imap + i) + pmean
                enddo
              else
c                 
c                 otherwise set up iy and interpolation factors
                iys = myFloor(yslice)
                if (iys .lt. inloadstr) then
                  iys = inloadstr
                  fy = 0.
                else if (iys .gt. inloadend) then
                  iys = inloadend - 1
                  fy = 1.
                else
                  fy = yslice - iys
                endif
                omfy = 1. - fy
c                 
c                 Now get starting X coordinate, fill to left
                xproj = 1 + xprjOffset
                xx = (xproj - (yy * salf * sbeta + zz * (calf * sbeta +
     &              xzfac(iv)) + xcenin+delxx)) / cbeta + xcen - (minXreproj-1)
                ixst = 1
                if (xx .lt. 1) then
                  ixst = (1. - xx) / delx + 1
                  do i = 0, ixst - 2
                    array(imap + i) = array(imap + i) + pmean
                  enddo
                  xx = xx + (ixst - 1) * delx
                endif
c                 
c                 get ending X coordinate, fill to right
                ixnd = iwide
                if (xx + (ixnd - ixst) * delx .gt. iwide) then
                  ixnd = iwide - (xx + (ixnd - ixst) * delx - iwide) / delx - 1
                  do i = ixnd, iwide - 1
                    array(imap + i) = array(imap + i) + pmean
                  enddo
                endif
c                 
c                 Add the line in: do simple 2x2 interpolation if no alpha
                indbase = nbase + iplane * (iys - inloadstr) + (iz - 1) *nxload
c                if (line.eq.591) print *,ixst,ixnd
                if (ifalpha .eq. 0) then
                  do i = ixst - 1, ixnd - 1
                    ix = xx
                    fx = xx - ix
                    omfx = 1. - fx
                    ind = indbase + ix - 1
                    array(imap+i) = array(imap+i) + omfz * omfx * array(ind) +
     &                  omfz * fx * array(ind + 1) +
     &                  fz * omfx * array(ind + nxload) +
     &                  fz * fx * array(ind + nxload + 1)
c                    if (line.eq.591.and.i.eq.164) print *,array(imap+i),array(ind),
c     &                  array(ind + 1),array(ind + nxload),array(ind + nxload + 1)
                    xx = xx + delx
                  enddo
                else
c                   
c                   Or do the full 3D interpolation if any variation in Y
                  do i = ixst - 1, ixnd - 1
                    ix = xx
                    fx = xx - ix
                    omfx = 1. - fx
                    d11 = omfx * omfy
                    d12 = omfx * fy
                    d21 = fx * omfy
                    d22 = fx * fy
                    ind = indbase + ix - 1
                    array(imap+i) = array(imap+i) + omfz * (d11 * array(ind)
     &                  + d12 * array(ind + iplane) + d21 * array(ind + 1)
     &                  + d22 * array(ind + iplane + 1))
     &                  + fz * (d11 * array(ind + nxload)
     &                  + d12 * array(ind + iplane + nxload)
     &                  + d21 * array(ind + 1 + nxload)
     &                  + d22 * array(ind + iplane + 1 + nxload))
                    xx = xx + delx
                  enddo
                endif
              endif
            enddo
            call writeReprojLine(iv, line, DMIN,DMAX,DTOT8)
          enddo
        else
c           
C           LOCAL ALIGNMENTS
c
c           first step: precompute all the x/yprojf/z  for all slices
          xprojMin = 10000000.
          xprojMax = 0.
          do load = inloadstr, inloadend
            indbase = nbase + iplane * (load - inloadstr) +
     &          ithickReproj * nxload
            iys = nint(load + yprjOffset - (minZreproj -1))
            do ix = 1, nxload
              ind = indbase + 4 * (ix - 1)
              call localProjFactors(ix + minXload - 1, iys, iv, array(ind),
     &            array(ind + 1), array(ind + 2), array(ind + 3))
              if (ix .eq. 1) xprojMin = min(xprojMin,
     &            array(ind) + (minYreproj - ycen) * array(ind+1),  array(ind)
     &            + (minYreproj + ithickReproj - ycen) * array(ind+1))
              if (ix .eq. nxload) xprojMax = max(xprojMax,
     &            array(ind) + (minYreproj - ycen) * array(ind+1),  array(ind)
     &            + (minYreproj + ithickReproj - ycen) * array(ind+1))
            enddo
          enddo
c          print *,'xprojmin, max', xprojMin, xprojMax
c
c           loop on lines to be done
          indbase = nbase - iplane * inloadstr + ithickReproj * nxload - 4
          do line = lsStart, lsEnd
c             
c             compute delta z as function of X across the loaded slice
c             which is not ideal since the data will not be coming from slice
            dxdelz = idxwarp / 2.
            ndelz = max(2., min((nxload - 1) / dxdelz, 2. * limwpos)) + 1
            dxdelz = (nxload - 1.) / (ndelz - 1.)
            iys = nint(line + yprjOffset - (minZreproj -1))
            do i = 1, ndelz
              xx = 1 + dxdelz * (i - 1)
              ixc=nint(xx + minXload - 1 -xcen+xcenin+delxx)
              call local_factors(ixc, iys, iv, ind1, ind2, ind3, ind4, f1, f2,
     &            f3, f4)
              warpDelz(i) = f1 * reprojDelz(swarpb(ind1), cwarpb(ind1),
     &            swarpa(ind1), cwarpa(ind1), warpxzfac(ind1), warpyzfac(ind1))
     &            + f2 * reprojDelz(swarpb(ind2), cwarpb(ind2),
     &            swarpa(ind2), cwarpa(ind2), warpxzfac(ind2), warpyzfac(ind2))
     &            + f3 * reprojDelz(swarpb(ind3), cwarpb(ind3),
     &            swarpa(ind3), cwarpa(ind3), warpxzfac(ind3), warpyzfac(ind3))
     &            + f4 * reprojDelz(swarpb(ind4), cwarpb(ind4),
     &            swarpa(ind4), cwarpa(ind4), warpxzfac(ind4), warpyzfac(ind4))
            enddo
c            print *,'got delz eg:',warpDelz(1), warpDelz(ndelz/2),
c     &          warpDelz(ndelz)
c            print *,iv,line, inloadstr,inloadend
c             
c             loop on pixels across line
            yproj = line + yprjOffset - (minZreproj -1)
            do ixp = 1, iwide
c
c               Get x projection coord, starting centered Z coordinate, and
c               approximate x and y coordinates 
c               X coordinate needs to be a loaded X index
c               Y coordinate is in slices of reconstruction
              xproj = ixp + xprjOffset
              zz = minYreproj - ycen
              sum = 0.
c              print *,ixp,xproj,yproj,xx,yy
              xx = (xproj - yy*sal(iv)*sbet(iv) - zz * (cal(iv)* sbet(iv) +
     &            xzfac(iv)) - xcenin-delxx) / cbet(iv) + xcen - (minXload - 1)
              yy = (yproj + zz*(sal(iv)-yzfac(iv))-slicen) / cal(iv) + slicen
     &            - yprjOffset + (minZreproj - 1)
c               
c               Move on ray up in Z
              lastZdone = 0
              tryjump = .true.
              diffxmax = 0
              diffymax = 0
              
              zjump = xjump * cbet(iv) / max(0.2, sbet(iv))
              do while (zz .lt. ithickReproj + minYreproj - ycen .and.
     &            lastZdone .eq. 0)
                if (xproj .lt. xprojMin - 5. .or. xproj .gt. xprojMax +5.) then
                  sum = sum + pmean
                else
                  call loadedProjectingPoint(xproj, yproj, zz, indbase, nxload,
     &                inloadstr, inloadend, xx, yy)
c                 
c                   If X or Y is out of bounds, fill with mean
                  if (yy .lt. inloadstr - ytol .or.
     &                yy .gt. inloadend + ytol .or. xx .lt. 1. .or.
     &                xx .ge. nxload) then
                    sum = sum + pmean
                  else
c                     
c                     otherwise, get x, y, z indexes, clamp y to limits, allow
c                     a fractional Z pixel at top of volume
                    ix = xx
                    fx = xx - ix
                    omfx = 1. - fx
                    yy = max(float(inloadstr), min(inloadend - 0.01, yy))
                    iy = yy
                    fy = yy - iy
                    omfy = 1. - fy
                    iz = max(1., zz + ycen)
                    fz = zz + ycen - iz
                    omfz = 1. - fz
                    if (iz .eq. ithickReproj) then
                      iz = iz - 1
                      fz = omfz
                      omfz = 0.
                      lastZdone = 1
                    endif
c                     
c                     Do the interpolation
                    d11 = omfx * omfy
                    d12 = omfx * fy
                    d21 = fx * omfy
                    d22 = fx * fy
                    ind = nbase + iplane * (iy - inloadstr) + (iz - 1) * nxload
     &                  + ix - 1 
                    sum = sum + omfz * (d11 * array(ind)
     &                  + d12 * array(ind + iplane) + d21 * array(ind + 1)
     &                  + d22 * array(ind + iplane + 1))
     &                  + fz * (d11 * array(ind + nxload)
     &                  + d12 * array(ind + iplane + nxload)
     &                  + d21 * array(ind + 1 + nxload)
     &                  + d22 * array(ind + iplane + 1 + nxload))
c                     
                    do while(tryjump)
c                       
c                       If jumping is OK, save the current position and compute
c                       how many steps can be jumped, stopping below the top
                      xxgood = xx
                      yygood = yy
                      zzgood = zz
                      ind = max(1., min(float(ndelz), xx / dxdelz))
                      delz = warpDelz(ind)
                      njump = zjump / delz
                      if (zz + zjump .gt. ithickReproj + minYreproj - ycen -
     &                    2) then
                        njump = (ithickReproj + minYreproj - ycen -2 -zz)/delz
                        tryjump = .false.
                      endif
                      if (njump .gt. 0) then
c                         
c                         Make the jump, find the projecting point;
c                         if it's out of bounds restore last point
                        zz = zz + njump * delz
                        xx = xx + njump * sbet(iv)
                        call loadedProjectingPoint(xproj, yproj, zz, indbase,
     &                      nxload, inloadstr, inloadend, xx, yy)
                        if (yy .lt. inloadstr .or. yy .gt. inloadend .or.
     &                      xx .lt. 1. .or. xx .ge. nxload) then
                          njump = 0
                          xx = xxgood
                          yy = yygood
                          zz = zzgood
                          tryjump = .false.
                        else
                          delx = (xx - xxgood) / njump
                          dely = (yy - yygood) / njump
                        endif
                      endif
c                       
c                       Loop on points from last one to final one
                      do ijump = 1, njump
                        xx = xxgood + ijump * delx
                        yy = yygood + ijump * dely
                        zz = zzgood + ijump * delz
                        ix = xx
                        fx = xx - ix
                        omfx = 1. - fx
                        iy = yy
                        fy = yy - iy
                        omfy = 1. - fy
                        iz = zz + ycen
                        fz = zz + ycen - iz
                        omfz = 1. - fz
                        d11 = omfx * omfy
                        d12 = omfx * fy
                        d21 = fx * omfy
                        d22 = fx * fy
                        ind = nbase + iplane * (iy - inloadstr) + (iz - 1) *
     &                      nxload + ix - 1 
                        sum = sum + omfz * (d11 * array(ind)
     &                      + d12 * array(ind + iplane) + d21 * array(ind + 1)
     &                      + d22 * array(ind + iplane + 1))
     &                      + fz * (d11 * array(ind + nxload)
     &                      + d12 * array(ind + iplane + nxload)
     &                      + d21 * array(ind + 1 + nxload)
     &                      + d22 * array(ind + iplane + 1 + nxload))
c                        fx = xx
c                        fy = yy
c                        call loadedProjectingPoint(xproj, yproj, zz, indbase,
c     &                      nxload, inloadstr, inloadend, fx, fy)
c                        diffxmax = max(diffxmax , abs(fx - xx))
c                        diffymax = max(diffymax , abs(fy - yy))
                      enddo
                    enddo
                  endif
                endif
c                 
c                 Adjust Z by local factor, move X approximately for next pixel
                ind = max(1., min(float(ndelz), xx / dxdelz))
                zz = zz + warpDelz(ind)
                xx = xx + sbet(iv)
              enddo
              array(imap+ixp-1) = sum
c              write (*,'(i5,2f10.4)')ixp,diffxmax,diffymax
            enddo
            call writeReprojLine(iv, line, DMIN,DMAX,DTOT8)
          enddo
        endif
      enddo
      end

c       Finds loaded point that projects to xproj, yproj at centered Z value
c       zz, using stored values for [y]zfac[fv].  Takes starting value in xx,yy
c       and returns found value.
c       X coordinate needs to be a loaded X index
c       Y coordinate is in slices of reconstruction
c
      subroutine loadedProjectingPoint(xproj, yproj, zz, indbase, nxload,
     &    inloadstr, inloadend, xx, yy)
      implicit none
      include 'tilt.inc'
      real*4 xproj, yproj, zz,xx, yy
      integer*4 nxload, inloadstr, inloadend, indbase
      integer*4 iter, ifdone, ind, ix, iy, ifout,i
      real*4 xp11, yp11, xp12, yp12, xp21, yp21, xerr, yerr, dypx, dxpy,dxpx
      real*4 dypy, den, fx, fy
      integer*4 myFloor

      iter = 0
      ifdone = 0
      do while (ifdone .eq. 0 .and. iter .lt. 5)
        ix = myFloor(xx)
        iy = myFloor(yy)
        ifout = 0
        if (ix .lt. 1 .or. ix .ge. nxload .or. iy .lt. inloadstr .or.
     &      iy .ge. inloadend) then
          ifout = 1
          ix = min(nxload - 1, max(1, ix))
          iy = min(inloadend - 1, max(inloadstr, iy))
        endif
        ind = indbase + iy * iplane + 4 * ix
        xp11 = array(ind) + array(ind+1) * zz
        yp11 = array(ind+2) + array(ind+3) * zz
        xp21 = array(ind+4) + array(ind+5) * zz
        yp21 = array(ind+6) + array(ind+7) * zz
        xp12 = array(ind+iplane) + array(ind+1+iplane) * zz
        yp12 = array(ind+2+iplane) + array(ind+3+iplane) * zz
c         write(*,101)'facs', (array(i), i=ind,ind+7)
c         write(*,101)'xps',xx,yy,zz,xp11,yp11,xp21,yp21,xp12,yp12
101     format(a,9f8.2)
        xerr = xproj - xp11
        yerr = yproj - yp11
        dxpx = xp21 - xp11
        dxpy = xp12 - xp11
        dypx = yp21 - yp11
        dypy = yp12 - yp11
        den = dxpx * dypy - dxpy * dypx
        fx = (xerr * dypy - yerr * dxpy) / den
        fy = (dxpx * yerr - dypx * xerr) / den
c         write(*,101)'dx,err,f',dxpx,dxpy,dypx,dypy,den,xerr,yerr,fx,fy
        xx = ix + fx
        yy = iy + fy
        if (fx .gt. -0.1 .and. fx.lt.1.1 .and. fy .gt. -0.1 .and.
     &      fy.lt.1.1) ifdone = 1
        if (ifout .ne. 0 .and. (iter .gt. 0 .or.  xx .lt. 0. .or.
     &      xx .gt. nxload + 1 .or. yy .lt. inloadstr - 1. .or.
     &      yy.gt. inloadend + 1.)) ifdone = 1
        iter = iter + 1
      enddo
      return
      end


c       Computes the change in Z that moves by 1 pixel along a projection
c       ray given the sines and cosines of alpha and beta and the z factors
c
      real*4 function reprojDelz(sbeta, cbeta, salf, calf, xzfac, yzfac)
      implicit none
      real*4 sbeta, cbeta, salf, calf, xzfac, yzfac,  dyfac
      dyfac = (salf - yzfac) / calf
      reprojDelz = 1. / sqrt(1. + dyfac**2 +
     &    ((dyfac * salf * sbeta + calf * sbeta + xzfac) / cbeta)**2)
      return
      end


c       Writes line LINE for view IV of a reprojection
c
      subroutine writeReprojLine(iv, line,DMIN,DMAX,DTOT8)
      implicit none
      include 'tilt.inc'
      integer*4 line, i, iyout, iv
      real*4 dmin,dmax,val
      real*8 dtot8
c             
c       Write the line after scaling.  Scale log data to give approximately
c       constant mean levels.  Descale non-log data by exposure weights
      if (iflog .ne. 0) then
        val = alog10(projMean + baselog) - ithickReproj * pmean / cbet(iv)
        if (debug) print *,iv,line,val
        do i = 0, iwide - 1
          array(imap + i) = 10**(array(imap + i) + val) - baselog
        enddo
      else
        do i = 0, iwide - 1
          array(imap + i) = array(imap + i) / expWeight(iv)
        enddo
      endif
      do i = 0, iwide - 1
        val = array(imap + i)
        if (debug .and. val .lt. dmin) print *,'min:',i,val
        if (debug .and. val .gt. dmax) print *,'max:',i,val
        dmin = min(dmin, val)
        dmax = max(dmax, val)
        dtot8 = dtot8 + val
      enddo
      iyout = line - islice
      if (minTotSlice .gt. 0) iyout = line - minTotSlice
      call parWrtPosn(2, iv - 1, iyout)
      call parWrtLin(2, array(imap))
      return
      end


c       Projects model points onto the included views
c
      subroutine projectModel(filout, delta, nvorig, coords, values)
      implicit none
      include 'tilt.inc'
      include 'model.inc'
      character*(*) filout
      real*4 delta(3), orig(3), coords(3,*), values(*)
      integer*4 mapnv(limview), nvorig, ibase, numPt, iobj, ipt, ip1, iv, nv
      real*4 value, rj, ri, rlslice, zz, yy, zpart, xproj, yproj
      integer*4 j, lslice, imodobj, imodcont, ierr, size
      real*4 fj, fls, f11, f12, f21, f22, xf11, xz11, yf11, yz11
      real*4 xf21, xz21, yf21, yz21,xf12, xz12, yf12, yz12,xf22, xz22, yf22
      real*4 yz22, xprojf, xprojz, yprojf, yprojz
      integer*4 getContValue, putImageRef, putContValue, putImodFlag
      integer*4 getScatSize, putScatSize
c
      call irtorg(1, orig(1), orig(2), orig(3))
      call scale_model(0)
      if (getScatSize(1, size) .ne. 0) size = 5
c       
c       get each point and its contour value into the arrays
      numPt = 0
      do iobj = 1, max_mod_obj
        call objtocont(iobj, obj_color, imodobj, imodcont)
        if (getContValue(imodobj, imodcont, value) .ne. 0) value = -1.
        ibase = ibase_obj(iobj)
        do ipt = 1, npt_in_obj(iobj)
          numPt = numPt + 1
          values(numPt) = value
          ip1=abs(object(ipt+ibase))
          coords(1, numPt) = p_coord(1, ip1)
          coords(2, numPt) = p_coord(2, ip1)
          coords(3, numPt) = p_coord(3, ip1)
        enddo
      enddo
c       
c       Start a new model
      call newimod()
      n_point = 0
      iobj = 0
      if (putImageRef(delta, orig) .ne. 0) call exitError(
     &    'Putting image reference information in output model')
c       
c       Build a map from views in file to ordered views in program
      do nv = 1, nvorig
        mapnv(nv) = 0
      enddo
      do nv = 1, nviews
        mapnv(mapuse(nv)) = nv
      enddo
c       
c       Loop on the points, start new contour for each
      do ipt = 1, numPt
        iobj = iobj + 1
        obj_color(1, iobj) = 1
        obj_color(2, iobj) = 255
        ierr = putContValue(1, iobj, values(ipt))
        ibase_obj(iobj) = n_point
        npt_in_obj(iobj) = 0
c         
c         Get real pixel coordinates in tomogram file
        rj = coords(1, ipt) + 0.5
        ri = coords(2, ipt) + 0.5
        rlslice = coords(3, ipt) + 0.5
c         
c         This may never be tesed but seems simple enough
        if (.not.perp) then
          ri = coords(3, ipt) + 0.5
          rlslice = coords(2, ipt) + 0.5
        endif
c         
c         Loop on the views in the file
        do nv = 1, nvorig
          iv = mapnv(nv)
          if (iv .gt. 0) then
            zz = (ri-ycen) * compress(iv)
            yy = rlslice-slicen
            if (nxwarp.eq.0) then
              zpart = yy*sal(iv)*sbet(iv) + zz*(cal(iv)*sbet(iv) +xzfac(iv)) +
     &            xcenin+delxx
              yproj = yy*cal(iv) - zz*(sal(iv)-yzfac(iv)) + slicen
              xproj = zpart+(rj-xcen)*cbet(iv)
            else
c               
c               local alignments
              j = rj
              fj = rj - j
              lslice = rlslice
              fls = rlslice - lslice
              f11 = (1.-fj) * (1.-fls)
              f12 = (1.-fj) * fls
              f21 = fj * (1.-fls)
              f22 = fj * fls
              call localProjFactors(j, lslice, iv, xf11, xz11, yf11, yz11)
              call localProjFactors(j+1, lslice, iv, xf21, xz21, yf21, yz21)
              call localProjFactors(j, lslice+1, iv, xf12, xz12, yf12, yz12)
              call localProjFactors(j+1, lslice+1, iv, xf22, xz22, yf22, yz22)
              xprojf = f11*xf11 + f12*xf12 + f21*xf21 + f22*xf22
              xprojz = f11*xz11 + f12*xz12 + f21*xz21 + f22*xz22
              yprojf = f11*yf11 + f12*yf12 + f21*yf21 + f22*yf22
              yprojz = f11*yz11 + f12*yz12 + f21*yz21 + f22*yz22
              xproj = xprojf + zz * xprojz
              yproj = yprojf + zz * yprojz
            endif
c             
c             Store model coordinates
            n_point = n_point + 1
            if (n_point .gt. max_pt) call exitError(
     &          'Too many projection points for small model arrays')
            npt_in_obj(iobj) = npt_in_obj(iobj) + 1
            object(n_point) = n_point
            p_coord(1, n_point) = xproj - 0.5
            p_coord(2, n_point) = yproj - 0.5
            p_coord(3, n_point) = nv - 1.
          endif
        enddo
      enddo
c       
c       Save model
      max_mod_obj = iobj
c
c       Set to open contour, show values etc., and show sphere on section only
      ierr = putImodFlag(1, 1)
      ierr = putImodFlag(1, 7)
      ierr = putImodFlag(1, 9)
      ierr = putScatSize(1, size)
      call scale_model(1)
      call write_wmod(filout)
      print *,n_point,' points written to output model'
      call exit(0)
      end

c       
c       $Log$
c       Revision 3.44  2009/02/16 06:22:30  mast
c       Modified to use new parallel write stuff
c
c       Revision 3.43  2008/12/12 16:40:21  mast
c       Fixes for 180 degree tilting: modify angles to be 0.05 degree away from
c       +/-90; disable cosine stretching of data above 80, and swap left and
c       right limits of valid backprojection when needed
c
c       Revision 3.42  2008/11/14 06:32:25  mast
c       Added projection from model
c
c       Revision 3.41  2008/11/02 14:45:38  mast
c       Added options for incremental reconstructions
c
c       Revision 3.40  2008/05/30 04:05:57  mast
c       Fixed scaling recommendation for 10 to 245, added one for -15000 to 15000
c
c       Revision 3.39  2007/12/06 20:43:16  mast
c       Added option for adjusting origin for all relevant changes
c
c       Revision 3.38  2007/09/08 20:57:58  mast
c       Fixed reading of SHIFT, REPROJECT, and some other entries
c
c       Revision 3.37  2007/07/19 02:46:41  mast
c       Removed debugging output
c
c       Revision 3.36  2007/07/17 15:20:07  mast
c       Fix int/float mismatch in min statement
c
c       Revision 3.35  2007/07/16 05:11:05  mast
c       Added reprojection from tomogram with local alignments, etc.
c
c       Revision 3.34  2007/06/22 05:04:34  mast
c       Converted to PIP
c
c       Revision 3.33  2007/03/08 23:50:28  mast
c       Give error if there are less than 2 local areas in each direction
c
c       Revision 3.32  2007/03/08 20:12:26  mast
c       Only put out message about x tilt angles from file if non-zero
c
c       Revision 3.31  2006/06/21 06:26:45  mast
c       Removed a debugging output
c
c       Revision 3.30  2006/06/20 22:10:59  mast
c       Added ability to reproject at multiple angles
c
c       Revision 3.29  2006/06/06 17:17:38  mast
c       Changes mmm/pixel output to formatted write to keep it on one line
c
c       Revision 3.28  2006/04/09 00:11:49  mast
c       Commented out debugging statement
c
c       Revision 3.27  2006/03/24 23:11:03  mast
c       Added ability for parallel runs to write directly to an existing
c       output file
c
c       Revision 3.26  2006/03/21 06:27:57  mast
c       Made it work with aligned stack bigger than "FULLIMAGE"
c
c       Revision 3.25  2005/12/09 04:43:27  mast
c       gfortran: .xor., continuation, format tab continuation or byte fixes
c
c       Revision 3.24  2005/10/08 20:10:06  mast
c       Fixed computation of ending slice with binning
c       
c       Revision 3.23  2005/06/07 22:12:42  mast
c       Added IMAGEBINNED option so dimensions can be scaled automatically
c       
c       Revision 3.22  2004/10/22 13:39:14  mast
c       Declared lnblnk for SGI
c       
c       Revision 3.21  2004/10/22 03:29:31  mast
c       Added z factor corrections and declarations for all routines
c       
c       Revision 3.20  2004/10/13 05:49:32  mast
c       Fixed bug in Y positioning when falling back to old-style X tilting,
c       Fixed fallback strategies to go to old-style with cosine stretch,
c       new-style without cosine stretch, then old-style w/o stretch and 
c       fixed bug in evaluating slices needed at that stage.
c       
c       Revision 3.19  2004/10/11 05:15:28  mast
c       Fixed integer truncation of pixel size from local file
c       
c       Revision 3.18  2004/09/24 18:24:52  mast
c       Incorporated reprojection capability from old code
c       
c       Revision 3.17  2004/07/19 04:10:54  mast
c       Needed to declare inum external for Intel/Windows
c       
c       Revision 3.16  2004/07/16 23:38:13  mast
c       Made it determine local scale from pixel sizes if present; fixed a bug
c       that was setting log base 0 after read the fullimage line; added
c       a EXCLUDELIST2 option
c       
c       Revision 3.15  2004/04/01 01:44:23  mast
c       Used input file range to avoid taking logs of very small numbers
c       
c       Revision 3.14  2003/12/09 00:11:49  mast
c       Have card reader accept blank lines in case sed in new sample.com
c       creates one
c       
c       Revision 3.13  2003/10/24 03:44:56  mast
c       took out flush call for Windows/Intel
c       
c       Revision 3.12  2003/10/16 20:38:32  mast
c       Adding to option documentation
c       
c       Revision 3.11  2003/08/02 22:36:49  mast
c       Revert from the version that padded thickness for x-axis tilting now
c       that fbp takes care of this.
c       Limit stack usage so that when loaded data is bigger than a certain
c       size, only enough is loaded to reconstruct 10 output slices.
c       
c       Revision 3.8  2003/04/29 23:33:54  mast
c       Set default for radial filter and increase thickness limit
c       
c       Revision 3.7  2002/07/28 00:03:40  mast
c       Made it preserve pixel spacings in output file
c       
c       Revision 3.6  2002/07/26 19:19:04  mast
c       Added machine-specific switch-points for not doing fast
c       backprojection
c       
c       Revision 3.5  2002/07/21 19:37:25  mast
c       Replaced STOP with call exit(1) and standardized error outputs
c       
c       Revision 3.4  2002/05/07 02:02:53  mast
c       Added EXCLUDELIST option
c       
c       Revision 3.3  2002/02/01 15:27:31  mast
c       Made it write extra data periodically with PARALLEL option to 
c       partially demangle the output file and prevent very slow reading
c       under Linux.
c       
c       Revision 1.2  2001/11/22 00:41:57  mast
c       Fixed computation of mean for files > 2 GPixels
c       
