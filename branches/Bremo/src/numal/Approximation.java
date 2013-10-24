package numal;

import numal.*;

public class Approximation extends Object {


public static void newton(int n, double x[], double f[])
{
	int k,i,im1;
	double xim1,fim1;

	im1=0;
	for (i=1; i<=n; i++) {
		fim1=f[im1];
		xim1=x[im1];
		for (k=i; k<=n; k++) f[k]=(f[k]-fim1)/(x[k]-xim1);
		im1=i;
	}
}


public static void ini(int n, int m, int s[])
{
	int i,j,k,l;
	double pin2,temp;

	pin2=Math.atan(1.0)*2.0/n;
	k=1;
	l=n-1;
	j=s[0]=0;
	s[n]=m;
	while (k < l) {
		temp=Math.sin(k*pin2);
		i=(int)(temp*temp*m);
		j = s[k] = ((i <= j) ? j+1 : i);
		s[l]=m-j;
		l--;
		k++;
	}
	if (l*2 == n) s[l]=m/2;
}


public static void sndremez(int n, int m, int s[], double g[],
                            double em[])
{
	int s0,sn,sjp1,i,j,k,up,low,nm1;
	double max,msjp1,hi,hj,he,abse,h,temp1,temp2;
	int itmp[] = new int[1];
  int jtmp[] = new int[1];

	s0=sjp1=s[0];
	he=em[0];
	low=s0+1;
	max=msjp1=abse=Math.abs(he);
	nm1=n-1;
	for (j=0; j<=nm1; j++) {
		up=s[j+1]-1;
		h=Basic.infnrmvec(low,up,itmp,g);
		i=itmp[0];
		if (h > max) max=h;
		if (h > abse)
			if (he*g[i] > 0.0) {
				s[j] = (msjp1 < h) ? i : sjp1;
				sjp1=s[j+1];
				msjp1=abse;
			} else {
				s[j]=sjp1;
				sjp1=i;
				msjp1=h;
			}
		else {
			s[j]=sjp1;
			sjp1=s[j+1];
			msjp1=abse;
		}
		he = -he;
		low=up+2;
	}
	sn=s[n];
	s[n]=sjp1;
  hi=Basic.infnrmvec(0,s0-1,itmp,g);
	i=itmp[0];
  hj=Basic.infnrmvec(sn+1,m,jtmp,g);
	j=jtmp[0];
	if (j > m) j=m;
	if (hi > hj) {
		if (hi > max) max=hi;
		temp1 = (g[i] == 0.0) ? 0.0 : ((g[i] > 0.0) ? 1.0 : -1.0);
		temp2 = (g[s[0]]==0.0) ? 0.0 : ((g[s[0]]>0.0) ? 1.0 : -1.0);
		if (temp1 == temp2) {
			if (hi > Math.abs(g[s[0]])) {
				s[0]=i;
				if (g[j]/g[s[n]] > 1.0) s[n]=j;
			}
		}
		else {
			if (hi > Math.abs(g[s[n]])) {
				s[n] = (g[j]/g[s[nm1]] > 1.0) ? j : s[nm1];
				for (k=nm1; k>=1; k--) s[k]=s[k-1];
				s[0]=i;
			}
		}
	} else {
		if (hj > max) max=hj;
		temp1 = (g[j] == 0.0) ? 0.0 : ((g[j] > 0.0) ? 1.0 : -1.0);
		temp2 = (g[s[n]]==0.0) ? 0.0 : ((g[s[n]]>0.0) ? 1.0 : -1.0);
		if (temp1 == temp2) {
			if (hj > Math.abs(g[s[n]])) {
				s[n]=j;
				if (g[i]/g[s[0]] > 1.0) s[0]=i;
			}
		} else
			if (hj > Math.abs(g[s[0]])) {
				s[0] = (g[i]/g[s[1]] > 1.0) ? i : s[1];
				for (k=1; k<=nm1; k++) s[k]=s[k+1];
				s[n]=j;
			}
	}
	em[1]=max;
}


public static void minmaxpol(int n, int m, double y[], double fy[],
                             double co[], double em[])
{
	int np1,k,pomk,count,cnt,j,mi,sjm1,sj,s0,up;
  double e,abse,abseh;
	int s[] = new int[n+2];
	double x[] = new double[n+2];
	double b[] = new double[n+2];
	double coef[] = new double[n+2];
	double g[] = new double[m+1];

  sj=0;
	np1=n+1;
	ini(np1,m,s);
	mi=(int)em[2];
	abse=0.0;
	count=1;
	do {
		pomk=1;
		for (k=0; k<=np1; k++) {
			x[k]=y[s[k]];
			coef[k]=fy[s[k]];
			b[k]=pomk;
			pomk = -pomk;
		}
    newton(np1,x,coef);
		newton(np1,x,b);
		em[0]=e=coef[np1]/b[np1];
    Basic.elmvec(0,n,0,coef,b,-e);
    Algebraic_eval.newgrn(n,x,coef);
		s0=sjm1=s[0];
		g[s0]=e;
		for (j=1; j<=np1; j++) {
			sj=s[j];
			up=sj-1;
      for (k=sjm1+1; k<=up; k++)
        g[k]=fy[k]-Algebraic_eval.pol(n,y[k],coef);
			g[sj] = e = -e;
			sjm1=sj;
		}
    for (k=s0-1; k>=0; k--) g[k]=fy[k]-Algebraic_eval.pol(n,y[k],coef);
    for (k=sj+1; k<=m; k++) g[k]=fy[k]-Algebraic_eval.pol(n,y[k],coef);
		sndremez(np1,m,s,g,em);
		abseh=abse;
		abse=Math.abs(e);
		cnt=count;
		count++;
	} while (count <= mi && abse > abseh);
	em[2]=mi;
	em[3]=cnt;
  Basic.dupvec(0,n,0,co,coef);
}

}