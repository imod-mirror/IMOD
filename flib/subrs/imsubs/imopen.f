C       *IMOPEN
C       
C       Open file NAME with qualities ATBUTE ('RO' 'OLD' 'NEW' SCRATCH')
C       and associate with stream ISTREAM 
C       (ISTREAM = # between 1 & 20 ; MAX of  20 files opened at any time)
c
c       $Id$
c       
      SUBROUTINE IMOPEN(ISTREAM,NAME,ATBUTE)
      use imsubs
      implicit none
      CHARACTER*(*) NAME,ATBUTE
      character*7 at2
      CHARACTER*320 FULLNAME
      integer*4 istream
      real*4 buf(3)
      integer*4 intbuf(3)
      equivalence (buf,intbuf)
      logical mrctyp,spityp
      integer*4 j,i,nsam,lenrec,nfilsz,ier,intflip
      integer*4 imodGetEnv
C       
C       Check for valid unit number
C       
      IF (ISTREAM .GT. maxstream) THEN
        WRITE(6,1000)
1000    FORMAT(//' ERROR: IMOPEN - Invalid STREAM number,',
     &      ' cannot open file!!!'//)
        call exit(1)
      END IF
      numopen = numopen + 1
      if (numopen .gt. maxunit) then
        write(6,1100)maxunit
1100    format(//,' ERROR: IMOPEN: - No More than',i4,
     &      ' files can be opened, cannot open file!!',//)
        call exit(1)
      endif
c       
c       Determine whether to write a brief header
c       
      if (ifBrief .lt. 0) then
        j = imodGetEnv('IMOD_BRIEF_HEADER', fullname)
        ifBrief = 0
        if (j .eq. 0) ifBrief = 1
      endif
C       
C       Open file
C       
      call strupcase(at2,atbute)
      CALL QOPEN(LSTREAM(ISTREAM),NAME,AT2)
      J = LSTREAM(ISTREAM)
      FLAG(J) = .TRUE.
      NOCON(J) = .FALSE.
c       
c       DNM 7/30/02: initialize flip here for all open units
c       
      mrcflip(j) = .false.
      spider(j)= .false.
c       
c       if it an existing file, check if it's a SPIDER file or a flipped file
c       
      if(at2(1:2).eq.'RO'.or.at2(1:3).eq.'OLD')then
        CALL QSEEK(J,1,1,1,1,1)
        CALL QREAD(J,buf,NBW3,IER)
        IF (IER .NE. 0) then
          print *
          print *, 'ERROR: IMOPEN - ERROR READING FILE'
          call exit(1)
        endif
c         
c         DNM 11/10/05: Require only one dimension less than 65536
c         
        spityp = .false.
        mrctyp = intbuf(1) .gt. 0 .and. intbuf(2) .gt. 0 .and.
     &      intbuf(3) .gt. 0 .and. (intbuf(1) .lt. 65536 .or.
     &      intbuf(2) .lt. 65536 .or. intbuf(3) .lt. 65536)
        if (.not.mrctyp) then
          call convert_longs(intbuf,3)
          mrctyp = intbuf(1) .gt. 0 .and. intbuf(2) .gt. 0 .and.
     &        intbuf(3) .gt. 0 .and. (intbuf(1) .lt. 65536 .or.
     &        intbuf(2) .lt. 65536 .or. intbuf(3) .lt. 65536)
          call convert_longs(intbuf,3)
          mrcflip(j)=mrctyp
        endif
        if(.not.mrctyp)
     &      spityp=abs(buf(1)).gt.0.5.and. buf(2).gt.0.5.and.
     &      buf(3).gt.0.5.and. ( abs(buf(1)).lt.65536..or.
     &      buf(2).lt.65536..or. buf(3).lt.65536.)
        spider(j)=spityp.and..not.mrctyp
        if(.not.(mrctyp.or.spityp))then
          print *
          print *, 'ERROR: IMOPEN - THIS FILE IS NOT ',
     &        'RECOGNIZABLE AS MRC OR SPIDER IMAGE FILE'
          call exit(1)
        endif
      endif
c       
c       if SPIDER file, open a second time on unit 30+j, for direct access
c       
      if(spider(j))then
        INQUIRE(FILE=NAME,RECL=NSAM)
        LENREC = NSAM/4
c         
c         CER change VAXism TYPE='OLD' to STATUS='OLD' for g77
c         alse delete RECORDTYPE='FIXED',SHARED,READONLY
c         
        OPEN(UNIT=30+j,FILE=NAME,STATUS='OLD', ACCESS='DIRECT',FORM=
     &      'UNFORMATTED', RECL=LENREC)
        ncrs(1,j)=lenrec
      endif
C       
C       Get and print file name
C       DNM: QINQUIRE returns nothing if J>5, then write crashes, so just skip it
c       DNM 10/14/07: That is no longer the case, make it unconditional
C       DNM: for unix, change len(at2) to len(atbute)
c       DNM 9/21/06: flush so etomo can know about renames being done
      CALL QINQUIRE(J,FULLNAME,NFILSZ)
      IF (AT2(1:len(atbute)) .EQ. 'NEW' .OR. AT2 .EQ. 'SCRATCH') THEN
        if (print)WRITE(6,2000) AT2(1:len(atbute)),ISTREAM,
     &      trim(FULLNAME)
        call flush(6)
      ELSE
        if (print)WRITE(6,2100) AT2(1:len(atbute)),ISTREAM,
     &      trim(FULLNAME),nfilsz
      ENDIF
2000  FORMAT(/,1x,A,' image file on unit',I4,' : ',A)
2100  FORMAT(/,1x,A,' image file on unit',I4,' : ',A, '     Size= ',I10,' K')
      if(spider(j).and.print .and. ifBrief .le. 0)write(6,2200)
2200  format(/,20x,'This is a SPIDER file.')
      if(mrcflip(j).and.print .and. ifBrief .le. 0)write(6,2300)
2300  format(/,20x,'This is a byte-swapped file.')
C       
      RETURN
C       
C       
C       *IMCLOSE
C       
C       Close file on stream ISTREAM. This frees up this channel for
C       further use.
C       
      ENTRY IMCLOSE(ISTREAM)
      J=LSTREAM(ISTREAM)
      if(spider(j))close(30+j)
      CALL QCLOSE(LSTREAM(ISTREAM))
      numopen = numopen - 1
      if (numopen .lt. 0) numopen = 0
      RETURN
      END
