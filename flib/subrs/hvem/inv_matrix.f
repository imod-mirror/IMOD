	SUBROUTINE inv_matrix(M1,M2)

	REAL*4 M1(3,3),M2(3,3),LEN

C>>>>>>>>>>>>>>>>>>>>>>>>EXECUTION STARTS HERE<<<<<<<<<<<<<<<<<<<<<<<<<<<
	Len = m1(1,1)*m1(2,2)*m1(3,3)+m1(2,1)*m1(3,2)*m1(1,3)+
     &  m1(1,2)*m1(2,3)*m1(3,1)-m1(1,3)*m1(2,2)*m1(3,1)-m1(2,1)*
     &  m1(1,2)*m1(3,3)-m1(1,1)*m1(3,2)*m1(2,3)

	M2(1,1) = (M1(2,2)*M1(3,3)-M1(3,2)*M1(2,3))/len
	M2(2,1) = (M1(3,1)*M1(2,3)-M1(2,1)*M1(3,3))/len
	M2(3,1) = (M1(2,1)*M1(3,2)-M1(3,1)*M1(2,2))/len
	M2(1,2) = (M1(3,2)*M1(1,3)-M1(1,2)*M1(3,3))/len
	M2(2,2) = (M1(1,1)*M1(3,3)-M1(3,1)*M1(1,3))/len
	M2(3,2) = (M1(3,1)*M1(1,2)-M1(3,2)*M1(1,1))/len
	M2(1,3) = (M1(1,2)*M1(2,3)-M1(2,2)*M1(1,3))/len
	M2(2,3) = (M1(2,1)*M1(1,3)-M1(1,1)*M1(2,3))/len
	M2(3,3) = (M1(2,2)*M1(1,1)-M1(2,1)*M1(1,2))/len
 	RETURN                          
	END
