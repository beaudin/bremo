package numal;

import numal.*;

public class Linear_algebra extends Object {


public static void dec(double a[][], int n, double aux[], int p[])
{
  int i,k,k1,pk,d;
  double r,s,eps;
  double v[] = new double[n+1];
  
  pk=0;
  r = -1.0;
  for (i=1; i<=n; i++) {
    s=Math.sqrt(Basic.mattam(1,n,i,i,a,a));
    if (s > r) r=s;
    v[i]=1.0/s;
  }
  eps=aux[2]*r;
  d=1;
  for (k=1; k<=n; k++) {
    r = -1.0;
    k1=k-1;
    for (i=k; i<=n; i++) {
      a[i][k] -= Basic.matmat(1,k1,i,k,a,a);
      s=Math.abs(a[i][k])*v[i];
      if (s > r) {
        r=s;
        pk=i;
      }
    }
    p[k]=pk;
    v[pk]=v[k];
    s=a[pk][k];
    if (Math.abs(s) < eps) break;
    if (s < 0.0) d = -d;
    if (pk != k) {
      d = -d;
      Basic.ichrow(1,n,k,pk,a);
    }
    for (i=k+1; i<=n; i++) a[k][i]=(a[k][i]-Basic.matmat(1,k1,k,i,a,a))/s;
  }
  aux[1]=d;
  aux[3]=k-1;
}


public static void gsselm(double a[][], int n, double aux[],
                          int ri[], int ci[])
{
  int ii[] = new int[1];
  int jj[] = new int[1];
  int i,j,p,q,r,r1,jpiv,rank,signdet;
  boolean partial;
  double crit,pivot,rgrow,max,aid,max1,eps;

  aux[5]=rgrow=Basic.absmaxmat(1,n,1,n,ii,jj,a);
  i = ii[0];
  j = jj[0];
  crit=n*rgrow*aux[4];
  eps=rgrow*aux[2];
  max=0.0;
  rank=n;
  signdet=1;
  partial = rgrow != 0;
  for (q=1; q<=n; q++)
    if (q != j) {
      aid=Math.abs(a[i][q]);
      if (aid > max) max=aid;
    }
  rgrow += max;
  for (r=1; r<=n; r++) {
    r1=r+1;
    if (i != r) {
      signdet = -signdet;
      Basic.ichrow(1,n,r,i,a);
    }
    if (j != r) {
      signdet = -signdet;
      Basic.ichcol(1,n,r,j,a);
    }
    ri[r]=i;
    ci[r]=j;
    pivot=a[r][r];
    if (pivot < 0.0) signdet = -signdet;
    if (partial) {
      max=max1=0.0;
      j=r1;
      Basic.rowcst(r1,n,r,a,1.0/pivot);
      for (p=r1; p<=n; p++) {
        Basic.elmrow(r1,n,p,r,a,a,-a[p][r]);
        aid=Math.abs(a[p][r1]);
        if (max < aid) {
          max=aid;
          i=p;
        }
      }
      for (q=r1+1; q<=n; q++) {
        aid=Math.abs(a[i][q]);
        if (max1 < aid) max1=aid;
      }
      aid=rgrow;
      rgrow += max1;
      if ((rgrow > crit) || (max < eps)) {
        partial=false;
        rgrow=aid;
        max=Basic.absmaxmat(r1,n,r1,n,ii,jj,a);
        i = ii[0];
        j = jj[0];
      }
    } else {
      if (max <= eps) {
        rank=r-1;
        if (pivot < 0.0) signdet = -signdet;
        break;
      }
      max = -1.0;
      Basic.rowcst(r1,n,r,a,1.0/pivot);
      for (p=r1; p<=n; p++) {
        jpiv=Basic.maxelmrow(r1,n,p,r,a,a,-a[p][r]);
        aid=Math.abs(a[p][jpiv]);
        if (max < aid) {
          max=aid;
          i=p;
          j=jpiv;
        }
      }
      if (rgrow < max) rgrow=max;
    }
  }
  aux[1]=signdet;
  aux[3]=rank;
  aux[7]=rgrow;
}


public static double onenrminv(double a[][], int n)
{
  int i,j;
  double norm,max,aid;
  double y[] = new double[n+1];
  
  norm=0.0;
  for (j=1; j<=n; j++) {
    for (i=1; i<=n; i++)
      y[i]=(i < j) ? 0 :
          ((i == j) ? 1.0/a[i][i] : -Basic.matvec(j,i-1,i,a,y)/a[i][i]);
    max=0.0;
    for (i=n; i>=1; i--) {
      aid = y[i] -= Basic.matvec(i+1,n,i,a,y);
      max += Math.abs(aid);
    }
    if (norm < max) norm=max;
  }
  return (norm);
}


public static void erbelm(int n, double aux[], double nrminv)
{
  double aid,eps;

  eps=aux[0];
  aid=(1.06*eps*(0.75*n+4.5)*(n*n)*aux[7]+aux[5]*aux[6])*nrminv;
  aux[11]=(2.0*aid >= (1.0-eps)) ? -1.0 : aid/(1.0-2.0*aid);
  aux[9]=nrminv;
}


public static void gsserb(double a[][], int n, double aux[],
                          int ri[], int ci[])
{
  gsselm(a,n,aux,ri,ci);
  if (aux[3] == n) erbelm(n,aux,onenrminv(a,n));
}


public static void gssnri(double a[][], int n, double aux[],
                          int ri[], int ci[])
{
  gsselm(a,n,aux,ri,ci);
  if (aux[3] == n) aux[9]=onenrminv(a,n);
}


public static double determ(double a[][], int n, int sign)
{
  int i;
  double det;

  det=1.0;
  for (i=1; i<=n; i++) det *= a[i][i];
  return (sign*Math.abs(det));
}


public static void sol(double a[][], int n, int p[], double b[])
{
  int k,pk;
  double r;

  for (k=1; k<=n; k++) {
    r=b[k];
    pk=p[k];
    b[k]=(b[pk]-Basic.matvec(1,k-1,k,a,b))/a[k][k];
    if (pk != k) b[pk]=r;
  }
  for (k=n; k>=1; k--) b[k] -= Basic.matvec(k+1,n,k,a,b);
}


public static void decsol(double a[][], int n, double aux[], double b[])
{
  int p[] = new int[n+1];

  dec(a,n,aux,p);
  if (aux[3] == n) sol(a,n,p,b);
}


public static void solelm(double a[][], int n, int ri[], int ci[],
                          double b[])
{
  int r,cir;
  double w;

  sol(a,n,ri,b);
  for (r=n; r>=1; r--) {
    cir=ci[r];
    if (cir != r) {
      w=b[r];
      b[r]=b[cir];
      b[cir]=w;
    }
  }
}


public static void gsssol(double a[][], int n, double aux[], double b[])
{
  int ri[] = new int[n+1];
  int ci[] = new int[n+1];

  gsselm(a,n,aux,ri,ci);
  if (aux[3] == n) solelm(a,n,ri,ci,b);
}


public static void gsssolerb(double a[][], int n,
                             double aux[], double b[])
{
  int ri[] = new int[n+1];
  int ci[] = new int[n+1];

  gsserb(a,n,aux,ri,ci);
  if (aux[3] == n) solelm(a,n,ri,ci,b);
}


public static void inv(double a[][], int n, int p[])
{
  int j,k,k1;
  double r;
  double v[] = new double[n+1];

  for (k=n; k>=1; k--) {
    k1=k+1;
    for (j=n; j>=k1; j--) {
      a[j][k1]=v[j];
      v[j] = -Basic.matmat(k1,n,k,j,a,a);
    }
    r=a[k][k];
    for (j=n; j>=k1; j--) {
      a[k][j]=v[j];
      v[j] = -Basic.matmat(k1,n,j,k,a,a)/r;
    }
    v[k]=(1.0-Basic.matmat(k1,n,k,k,a,a))/r;
  }
  Basic.dupcolvec(1,n,1,a,v);
  for (k=n-1; k>=1; k--) {
    k1=p[k];
    if (k1 != k) Basic.ichcol(1,n,k,k1,a);
  }
}


public static void decinv(double a[][], int n, double aux[])
{
  int p[] = new int[n+1];
  
  dec(a,n,aux,p);
  if (aux[3] == n) inv(a,n,p);
}


public static double inv1(double a[][], int n, int ri[], int ci[],
                          boolean withnorm)
{
  int l,k,k1;
  double aid,nrminv;

  inv(a,n,ri);
  nrminv=0.0;
  if (withnorm)
    for (l=1; l<=n; l++) nrminv += Math.abs(a[l][n]);
  for (k=n-1; k>=1; k--) {
    if (withnorm) {
      aid=0.0;
      for (l=1; l<=n; l++) aid += Math.abs(a[l][k]);
      if (nrminv < aid) nrminv=aid;
    }
    k1=ci[k];
    if (k1 != k) Basic.ichrow(1,n,k,k1,a);
  }
  return (nrminv);
}


public static void gssinv(double a[][], int n, double aux[])
{
  int ri[] = new int[n+1];
  int ci[] = new int[n+1];

  gsselm(a,n,aux,ri,ci);
  if (aux[3] == n) aux[9]=inv1(a,n,ri,ci,true);
}


public static void gssinverb(double a[][], int n, double aux[])
{
  int ri[] = new int[n+1];
  int ci[] = new int[n+1];

  gsselm(a,n,aux,ri,ci);
  if (aux[3] == n) erbelm(n,aux,inv1(a,n,ri,ci,true));
}


public static void itisol(double a[][], double lu[][], int n,
                          double aux[], int ri[], int ci[], double b[])
{
  int i,j,iter,maxiter;
  double maxerx,erx,nrmres,nrmsol,r,rr,dtemp;
  double res[] = new double[n+1];
  double sol[] = new double[n+1];

  maxerx=erx=aux[10];
  maxiter=(int) aux[12];
  Basic.inivec(1,n,sol,0.0);
  Basic.dupvec(1,n,0,res,b);
  iter=1;
  do {
    solelm(lu,n,ri,ci,res);
    erx=nrmsol=nrmres=0.0;
    for (i=1; i<=n; i++) {
      r=res[i];
      erx += Math.abs(r);
      rr=sol[i]+r;
      sol[i]=rr;
      nrmsol += Math.abs(rr);
    }
    erx /= nrmsol;
    for (i=1; i<=n; i++) {
      dtemp = -(double)b[i];
      for (j=1; j<=n; j++)
        dtemp += (double)a[i][j]*(double)sol[j];
      r = -dtemp;
      res[i]=r;
      nrmres += Math.abs(r);
    }
    iter++;
  } while ((iter <= maxiter) && (maxerx < erx));
  Basic.dupvec(1,n,0,b,sol);
  aux[11]=erx;
  aux[13]=nrmres;
}


public static void gssitisol(double a[][], int n, double aux[], double b[])
{
  int ri[] = new int[n+1];
  int ci[] = new int[n+1];
  double aa[][] = new double[n+1][n+1];

  Basic.dupmat(1,n,1,n,aa,a);
  gsselm(a,n,aux,ri,ci);
  if (aux[3] == n) itisol(aa,a,n,aux,ri,ci,b);
}


public static void itisolerb(double a[][], double lu[][], int n,
                   double aux[], int ri[], int ci[], double b[])
{
  int i;
  double nrmsol,nrminv,nrmb,alfa,tola,eps;

  eps=aux[0];
  nrminv=aux[9];
  tola=aux[5]*aux[6];
  nrmb=nrmsol=0.0;
  for (i=1; i<=n; i++) nrmb += Math.abs(b[i]);
  itisol(a,lu,n,aux,ri,ci,b);
  for (i=1; i<=n; i++) nrmsol += Math.abs(b[i]);
  alfa=1.0-(1.06*eps*aux[7]*(0.75*n+4.5)*n*n+tola)*nrminv;
  if (alfa < eps)
    aux[11] = -1.0;
  else {
    alfa=((aux[13]+aux[8]*nrmb)/nrmsol+tola)*nrminv/alfa;
    aux[11]=(1.0-alfa < eps) ? -1.0 : alfa/(1.0-alfa);
  }
}


public static void gssitisolerb(double a[][], int n,
                                double aux[], double b[])
{
  int ri[] = new int[n+1];
  int ci[] = new int[n+1];
  double aa[][] = new double[n+1][n+1];

  Basic.dupmat(1,n,1,n,aa,a);
  gssnri(a,n,aux,ri,ci);
  if (aux[3] == n) itisolerb(aa,a,n,aux,ri,ci,b);
}


public static void chldec2(double a[][], int n, double aux[])
{
  int k,j;
  double r,epsnorm;

  r=0.0;
  for (k=1; k<=n; k++)
    if (a[k][k] > r) r=a[k][k];
  epsnorm=aux[2]*r;
  for (k=1; k<=n; k++) {
    r=a[k][k]-Basic.tammat(1,k-1,k,k,a,a);
    if (r <= epsnorm) {
      aux[3]=k-1;
      return;
    }
    a[k][k]=r=Math.sqrt(r);
    for (j=k+1; j<=n; j++)
      a[k][j]=(a[k][j]-Basic.tammat(1,k-1,j,k,a,a))/r;
  }
  aux[3]=n;
}


public static void chldec1(double a[], int n, double aux[])
{
  int j,k,kk,kj,low,up;
  double r,epsnorm;

  r=0.0;
  kk=0;
  for (k=1; k<=n; k++) {
    kk += k;
    if (a[kk] > r) r=a[kk];
  }
  epsnorm=aux[2]*r;
  kk=0;
  for (k=1; k<=n; k++) {
    kk += k;
    low=kk-k+1;
    up=kk-1;
    r=a[kk]-Basic.vecvec(low,up,0,a,a);
    if (r <= epsnorm) {
      aux[3]=k-1;
      return;
    }
    a[kk]=r=Math.sqrt(r);
    kj=kk+k;
    for (j=k+1; j<=n; j++) {
      a[kj]=(a[kj]-Basic.vecvec(low,up,kj-kk,a,a))/r;
      kj +=j;
    }
  }
  aux[3]=n;
}


public static double chldeterm2(double a[][], int n)
{
  int k;
  double d;

  d=1.0;
  for (k=1; k<=n; k++) d *= a[k][k];
  return (d*d);
}


public static double chldeterm1(double a[], int n)
{
  int k,kk;
  double d;

  d=1.0;
  kk=0;
  for (k=1; k<=n; k++) {
    kk += k;
    d *= a[kk];
  }
  return (d*d);
}


public static void chlsol2(double a[][], int n, double b[])
{
  int i;

  for (i=1; i<=n; i++) b[i]=(b[i]-Basic.tamvec(1,i-1,i,a,b))/a[i][i];
  for (i=n; i>=1; i--) b[i]=(b[i]-Basic.matvec(i+1,n,i,a,b))/a[i][i];
}


public static void chlsol1(double a[], int n, double b[])
{
  int i,ii;

  ii=0;
  for (i=1; i<=n; i++) {
    ii += i;
    b[i]=(b[i]-Basic.vecvec(1,i-1,ii-i,b,a))/a[ii];
  }
  for (i=n; i>=1; i--) {
    b[i]=(b[i]-Basic.seqvec(i+1,n,ii+i,0,a,b))/a[ii];
    ii -= i;
  }
}


public static void chldecsol2(double a[][], int n,
                              double aux[], double b[])
{
  chldec2(a,n,aux);
  if (aux[3] == n) chlsol2(a,n,b);
}


public static void chldecsol1(double a[], int n, double aux[], double b[])
{
  chldec1(a,n,aux);
  if (aux[3] == n) chlsol1(a,n,b);
}


public static void chlinv2(double a[][], int n)
{
  int i,j,i1;
  double r;
  double u[] = new double[n+1];
  
  for (i=n; i>=1; i--) {
    r=1.0/a[i][i];
    i1=i+1;
    Basic.dupvecrow(i1,n,i,u,a);
    for (j=n; j>=i1; j--)
      a[i][j] = -(Basic.tamvec(i1,j,j,a,u)+Basic.matvec(j+1,n,j,a,u))*r;
    a[i][i]=(r-Basic.matvec(i1,n,i,a,u))*r;
  }
}


public static void chlinv1(double a[], int n)
{
  int i,ii,i1,j,ij;
  double r;
  double u[] = new double[n+1];
  
  ii=((n+1)*n)/2;
  for (i=n; i>=1; i--) {
    r=1.0/a[ii];
    i1=i+1;
    ij=ii+i;
    for (j=i1; j<=n; j++) {
      u[j]=a[ij];
      ij += j;
    }
    for (j=n; j>=i1; j--) {
      ij -= j;
      a[ij] = -Basic.symmatvec(i1,n,j,a,u)*r;
    }
    a[ii]=(r-Basic.seqvec(i1,n,ii+i,0,a,u))*r;
    ii -= i;
  }
}


public static void chldecinv2(double a[][], int n, double aux[])
{
  chldec2(a,n,aux);
  if (aux[3] == n) chlinv2(a,n);
}


public static void chldecinv1(double a[], int n, double aux[])
{
  chldec1(a,n,aux);
  if (aux[3] == n) chlinv1(a,n);
}


public static void decsym2(double a[][], int n, double tol,
                           int aux[], int p[], double detaux[])
{
  int i,j,m,ip1,ip2;
  boolean onebyone,sym;
  double det,s,t,alpha,lambda,sigma,aii,aip1,aip1i,temp;

  aux[3]=aux[4]=0;
  sym=true;
  i=0;
  while (sym && (i < n)) {
    i++;
    j=i;
    while (sym && (j < n)) {
      j++;
      sym = sym && (a[i][j] == a[j][i]);
    }
  }
  if (sym)
    aux[2]=1;
  else {
    aux[2]=0;
    aux[5]=n;
    return;
  }
  alpha=(1.0+Math.sqrt(17.0))/8.0;
  p[n]=n;
  i=1;
  while (i < n) {
    ip1=i+1;
    ip2=i+2;
    aii=Math.abs(a[i][i]);
    p[i]=i;
    lambda=Math.abs(a[i][ip1]);
    j=ip1;
    for (m=ip2; m<=n; m++)
      if (Math.abs(a[i][m]) > lambda) {
        j=m;
        lambda=Math.abs(a[i][m]);
      }
    t=alpha*lambda;
    onebyone=true;
    if (aii < t) {
      sigma=lambda;
      for (m=ip1; m<=j-1; m++)
        if (Math.abs(a[m][j]) > sigma) sigma=Math.abs(a[m][j]);
      for (m=j+1; m<=n; m++)
        if (Math.abs(a[j][m]) > sigma) sigma=Math.abs(a[j][m]);
      if (sigma*aii < lambda) {
        if (alpha*sigma < Math.abs(a[j][j])) {
          Basic.ichrow(j+1,n,i,j,a);
          Basic.ichrowcol(ip1,j-1,i,j,a);
          t=a[i][i];
          a[i][i]=a[j][j];
          a[j][j]=t;
          p[i]=j;
        } else {
          if (j > ip1) {
            Basic.ichrow(j+1,n,ip1,j,a);
            Basic.ichrowcol(ip2,j-1,ip1,j,a);
            t=a[i][i];
            a[i][i]=a[j][j];
            a[j][j]=t;
            t=a[i][j];
            a[i][j]=a[i][ip1];
            a[i][ip1]=t;
          }
          temp=a[i][ip1];
          det=a[i][i]*a[ip1][ip1]-temp*temp;
          aip1i=a[i][ip1]/det;
          aii=a[i][i]/det;
          aip1=a[ip1][ip1]/det;
          p[i]=j;
          p[ip1]=0;
          detaux[i]=1.0;
          detaux[ip1]=det;
          for (j=ip2; j<=n; j++) {
            s=aip1i*a[ip1][j]-aip1*a[i][j];
            t=aip1i*a[i][j]-aii*a[ip1][j];
            Basic.elmrow(j,n,j,i,a,a,s);
            Basic.elmrow(j,n,j,ip1,a,a,t);
            a[i][j]=s;
            a[ip1][j]=t;
          }
          aux[3]++;
          aux[4]++;
          i=ip2;
          onebyone=false;
        }
      }
    }
    if (onebyone) {
      if (tol < Math.abs(a[i][i])) {
        aii=a[i][i];
        detaux[i]=a[i][i];
        if (aii > 0.0)
          aux[3]++;
        else
          aux[4]++;
        for (j=ip1; j<=n; j++) {
          s = -a[i][j]/aii;
          Basic.elmrow(j,n,j,i,a,a,s);
          a[i][j]=s;
        }
      }
      i=ip1;
    }
  }
  if (i == n) {
    if (tol < Math.abs(a[n][n])) {
      if (a[n][n] > 0.0)
        aux[3]++;
      else
        aux[4]++;
    }
    detaux[n]=a[n][n];
  }
  aux[5]=n-aux[3]-aux[4];
}


public static double determsym2(double detaux[], int n, int aux[])
{
  int i;
  double det;

  if (aux[5] > 0)
    det=0.0;
  else {
    det=1.0;
    for (i=1; i<=n; i++) det *= detaux[i];
  }
  return (det);
}


public static void solsym2(double a[][], int n, double b[], int p[],
                           double detaux[])
{
  int i,ii,k,ip1,pi,pii;
  double det,temp,save;

  save=0.0;
  i=1;
  while (i < n) {
    ip1=i+1;
    pi=p[i];
    save=b[pi];
    if (p[ip1] > 0) {
      b[pi]=b[i];
      b[i]=save/a[i][i];
      Basic.elmvecrow(ip1,n,i,b,a,save);
      i=ip1;
    } else {
      temp=b[i];
      b[pi]=b[ip1];
      det=detaux[ip1];
      b[i]=(temp*a[ip1][ip1]-save*a[i][ip1])/det;
      b[ip1]=(save*a[i][i]-temp*a[i][ip1])/det;
      Basic.elmvecrow(i+2,n,i,b,a,temp);
      Basic.elmvecrow(i+2,n,ip1,b,a,save);
      i += 2;
    }
  }
  if (i == n) {
    b[i] /= a[i][i];
    i=n-1;
  } else
    i=n-2;
  while (i > 0) {
    if (p[i] == 0)
      ii=i-1;
    else
      ii=i;
    for (k=ii; k<=i; k++) {
      save=b[k];
      save += Basic.matvec(i+1,n,k,a,b);
      b[k]=save;
    }
    pii=p[ii];
    b[i]=b[pii];
    b[pii]=save;
    i=ii-1;
  }
}


public static void decsolsym2(double a[][], int n, double b[],
                              double tol, int aux[])
{
  int p[] = new int[n+1];
  double detaux[] = new double[n+1];
  
  decsym2(a,n,tol,aux,p,detaux);
  if (aux[5] == 0) solsym2(a,n,b,p,detaux);
}


public static void lsqortdec(double a[][], int n, int m, double aux[],
                             double aid[], int ci[])
{
  int j,k,kpiv;
  double beta,sigma,norm,w,eps,akk,aidk,temp;
  double sum[] = new double[m+1];
  
  norm=0.0;
  aux[3]=m;
  for (k=1; k<=m; k++) {
    w=sum[k]=Basic.tammat(1,n,k,k,a,a);
    if (w > norm) norm=w;
  }
  w=aux[5]=Math.sqrt(norm);
  eps=aux[2]*w;
  for (k=1; k<=m; k++) {
    sigma=sum[k];
    kpiv=k;
    for (j=k+1; j<=m; j++)
      if (sum[j] > sigma) {
        sigma=sum[j];
        kpiv=j;
      }
    if (kpiv != k) {
      sum[kpiv]=sum[k];
      Basic.ichcol(1,n,k,kpiv,a);
    }
    ci[k]=kpiv;
    akk=a[k][k];
    sigma=Basic.tammat(k,n,k,k,a,a);
    w=Math.sqrt(sigma);
    aidk=aid[k]=((akk < 0.0) ? w : -w);
    if (w < eps) {
      aux[3]=k-1;
      break;
    }
    beta=1.0/(sigma-akk*aidk);
    a[k][k]=akk-aidk;
    for (j=k+1; j<=m; j++) {
      Basic.elmcol(k,n,j,k,a,a,-beta*Basic.tammat(k,n,k,j,a,a));
      temp=a[k][j];
      sum[j] -= temp*temp;
    }
  }
}


public static void lsqdglinv(double a[][], int m, double aid[],
                             int ci[], double diag[])
{
  int j,k,cik;
  double w;

  for (k=1; k<=m; k++) {
    diag[k]=1.0/aid[k];
    for (j=k+1; j<=m; j++) diag[j] = -Basic.tamvec(k,j-1,j,a,diag)/aid[j];
    diag[k]=Basic.vecvec(k,m,0,diag,diag);
  }
  for (k=m; k>=1; k--) {
    cik=ci[k];
    if (cik != k) {
      w=diag[k];
      diag[k]=diag[cik];
      diag[cik]=w;
    }
  }
}


public static void lsqsol(double a[][], int n, int m, double aid[],
                          int ci[], double b[])
{
  int k,cik;
  double w;

  for (k=1; k<=m; k++)
    Basic.elmveccol(k,n,k,b,a,Basic.tamvec(k,n,k,a,b)/(aid[k]*a[k][k]));
  for (k=m; k>=1; k--) b[k]=(b[k]-Basic.matvec(k+1,m,k,a,b))/aid[k];
  for (k=m; k>=1; k--) {
    cik=ci[k];
    if (cik != k) {
      w=b[k];
      b[k]=b[cik];
      b[cik]=w;
    }
  }
}


public static void lsqortdecsol(double a[][], int n, int m,
                   double aux[], double diag[], double b[])
{
  int ci[] = new int[m+1];
  double aid[] = new double[m+1];
  
  lsqortdec(a,n,m,aux,aid,ci);
  if (aux[3] == m) {
    lsqdglinv(a,m,aid,ci,diag);
    lsqsol(a,n,m,aid,ci,b);
  }
}


public static void lsqinv(double a[][], int m, double aid[], int c[])
{
  int i,ci;
  double w;

  for (i=1; i<=m; i++) a[i][i]=aid[i];
  chlinv2(a,m);
  for (i=m; i>=1; i--) {
    ci=c[i];
    if (ci != i) {
      Basic.ichcol(1,i-1,i,ci,a);
      Basic.ichrow(i+1,ci-1,i,ci,a);
      Basic.ichrow(ci+1,m,i,ci,a);
      w=a[i][i];
      a[i][i]=a[ci][ci];
      a[ci][ci]=w;
    }
  }
}


public static void lsqdecomp(double a[][], int n, int m, int n1,
                             double aux[], double aid[], int ci[])
{
  int j,k,kpiv,nr,s;
  boolean fsum;
  double beta,sigma,norm,aidk,akk,w,eps,temp;
  double sum[] = new double[m+1];
  
  norm=0.0;
  aux[3]=m;
  nr=n1;
  fsum=true;
  for (k=1; k<=m; k++) {
    if (k == n1+1) {
      fsum=true;
      nr=n;
    }
    if (fsum)
      for (j=k; j<=m; j++) {
        w=sum[j]=Basic.tammat(k,nr,j,j,a,a);
        if (w > norm) norm=w;
      }
    fsum=false;
    eps=aux[2]*Math.sqrt(norm);
    sigma=sum[k];
    kpiv=k;
    for (j=k+1; j<=m; j++)
      if (sum[j] > sigma) {
        sigma=sum[j];
        kpiv=j;
      }
    if (kpiv != k) {
      sum[kpiv]=sum[k];
      Basic.ichcol(1,n,k,kpiv,a);
    }
    ci[k]=kpiv;
    akk=a[k][k];
    sigma=Basic.tammat(k,nr,k,k,a,a);
    w=Math.sqrt(sigma);
    aidk=aid[k]=((akk < 0.0) ? w : -w);
    if (w < eps) {
      aux[3]=k-1;
      break;
    }
    beta=1.0/(sigma-akk*aidk);
    a[k][k]=akk-aidk;
    for (j=k+1; j<=m; j++) {
      Basic.elmcol(k,nr,j,k,a,a,-beta*Basic.tammat(k,nr,k,j,a,a));
      temp=a[k][j];
      sum[j] -= temp*temp;
    }
    if (k == n1)
      for (j=n1+1; j<=n; j++)
        for (s=1; s<=m; s++) {
          nr = (s > n1) ? n1 : s-1;
          w=a[j][s]-Basic.matmat(1,nr,j,s,a,a);
          a[j][s] = (s > n1) ? w : w/aid[s];
        }
  }
}


public static void lsqrefsol(double a[][], double qr[][], int n,
                   int m, int n1, double aux[], double aid[], int ci[],
                   double b[], double ldx[], double x[], double res[])
{
  boolean startup;
  int i,j,k,s;
  double c1,nexve,ndx,ndr,d,corrnorm,dtemp;
  double f[] = new double[n+1];
  double g[] = new double[m+1];
  
  for (j=1; j<=m; j++) {
    s=ci[j];
    if (s != j) Basic.ichcol(1,n,j,s,a);
  }
  for (j=1; j<=m; j++) x[j]=g[j]=0.0;
  for (i=1; i<=n; i++) {
    res[i]=0.0;
    f[i]=b[i];
  }
  k=0;
  do {
    startup = (k <= 1);
    ndx=ndr=0.0;
    if (k != 0) {
      for (i=1; i<=n; i++) res[i] += f[i];
      for (s=1; s<=m; s++) {
        x[s] += g[s];
        dtemp=0.0;
        for (i=1; i<=n; i++)
          dtemp += (double)a[i][s]*(double)res[i];
        d=dtemp;
        g[s]=(-d-Basic.tamvec(1,s-1,s,qr,g))/aid[s];
      }
      for (i=1; i<=n; i++) {
        dtemp = (i > n1) ? res[i] : 0.0;
        for (s=1; s<=m; s++)
          dtemp += (double)a[i][s]*(double)x[s];
        f[i]=(double)b[i]-dtemp;
      }
    }
    nexve=Math.sqrt(Basic.vecvec(1,m,0,x,x)+Basic.vecvec(1,n,0,res,res));
    for (s=1; s<=n1; s++)
      Basic.elmveccol(s,n1,s,f,qr,
                      Basic.tamvec(s,n1,s,qr,f)/(qr[s][s]*aid[s]));
    for (i=n1+1; i<=n; i++)
      f[i] -= Basic.matvec(1,n1,i,qr,f);
    for (s=n1+1; s<=m; s++)
      Basic.elmveccol(s,n,s,f,qr,
                      Basic.tamvec(s,n,s,qr,f)/(qr[s][s]*aid[s]));
    for (i=1; i<=m; i++) {
      c1=f[i];
      f[i]=g[i];
      g[i] = (i > n1) ? c1-g[i] : c1;
    }
    for (s=m; s>=1; s--) {
      g[s]=(g[s]-Basic.matvec(s+1,m,s,qr,g))/aid[s];
      ndx += g[s]*g[s];
    }
    for (s=m; s>=n1+1; s--)
      Basic.elmveccol(s,n,s,f,qr,
                      Basic.tamvec(s,n,s,qr,f)/(qr[s][s]*aid[s]));
    for (s=1; s<=n1; s++)
      f[s] -= Basic.tamvec(n1+1,n,s,qr,f);
    for (s=n1; s>=1; s--)
      Basic.elmveccol(s,n1,s,f,qr,
                      Basic.tamvec(s,n1,s,qr,f)/(qr[s][s]*aid[s]));
    aux[7]=k;
    for (i=1; i<=n; i++) ndr += f[i]*f[i];
    corrnorm=Math.sqrt(ndx+ndr);
    k++;
  } while (startup || (corrnorm>aux[2]*nexve && k<=aux[6]));
  ldx[0]=Math.sqrt(ndx);
  for (s=m; s>=1; s--) {
    j=ci[s];
    if (j != s) {
      c1=x[j];
      x[j]=x[s];
      x[s]=c1;
      Basic.ichcol(1,n,j,s,a);
    }
  }
}


public static void solsvdovr(double u[][], double val[], double v[][],
                             int m, int n, double x[], double em[])
{
  int i;
  double min;
  double x1[] = new double[n+1];
  
  min=em[6];
  for (i=1; i<=n; i++)
    x1[i] = (val[i] <= min) ? 0.0 : Basic.tamvec(1,m,i,u,x)/val[i];
  for (i=1; i<=n; i++) x[i]=Basic.matvec(1,n,i,v,x1);
}


public static int solovr(double a[][], int m, int n,
                         double x[], double em[])
{
  int i;

  double val[] = new double[n+1];
  double v[][] = new double[n+1][n+1];
  i=qrisngvaldec(a,m,n,val,v,em);
  if (i == 0) solsvdovr(a,val,v,m,n,x,em);
  return i;
}


public static void solsvdund(double u[][], double val[], double v[][],
                             int m, int n, double x[], double em[])
{
  int i;
  double min;
  double x1[] = new double[n+1];
  
  min=em[6];
  for (i=1; i<=n; i++)
    x1[i] = (val[i] <= min) ? 0.0 : Basic.tamvec(1,n,i,v,x)/val[i];
  for (i=1; i<=m; i++)  x[i] = Basic.matvec(1,n,i,u,x1);
}


public static int solund(double a[][], int m, int n,
                         double x[], double em[])
{
  int i;
  double val[] = new double[n+1];
  double v[][] = new double[n+1][n+1];
  
  i=qrisngvaldec(a,m,n,val,v,em);
  if (i == 0) solsvdund(a,val,v,m,n,x,em);
  return i;
}


public static void homsolsvd(double u[][], double val[],
                             double v[][], int m, int n)
{
  int i,j;
  double x;

  for (i=n; i>=2; i--)
    for (j=i-1; j>=1; j--)
      if (val[i] > val[j]) {
        x=val[i];
        val[i]=val[j];
        val[j]=x;
        Basic.ichcol(1,m,i,j,u);
        Basic.ichcol(1,n,i,j,v);
      }
}


public static int homsol(double a[][], int m, int n,
                         double v[][], double em[])
{
  int i;
  double val[] = new double[n+1];
  
  i=qrisngvaldec(a,m,n,val,v,em);
  if (i == 0) homsolsvd(a,val,v,m,n);
  return i;
}


public static void psdinvsvd(double u[][], double val[],
                             double v[][], int m, int n, double em[])
{
  int i,j;
  double min,vali;
  double x[] = new double[n+1];
  
  min=em[6];
  for (i=1; i<=n; i++)
    if (val[i] > min) {
      vali=1.0/val[i];
      for (j=1; j<=m; j++) u[j][i] *= vali;
    } else
      for (j=1; j<=m; j++) u[j][i]=0.0;
  for (i=1; i<=m; i++) {
    for (j=1; j<=n; j++) x[j]=u[i][j];
    for (j=1; j<=n; j++) u[i][j]=Basic.matvec(1,n,j,v,x);
  }
}


public static int psdinv(double a[][], int m, int n, double em[])
{
  int i;
  double val[] = new double[n+1];
  double v[][] = new double[n+1][n+1];
  
  i=qrisngvaldec(a,m,n,val,v,em);
  if (i == 0) psdinvsvd(a,val,v,m,n,em);
  return i;
}


public static void decbnd(double a[], int n, int lw, int rw,
                          double aux[], double m[], int p[])
{
  int i,j,k,kk,kk1,pk,mk,ik,lw1,f,q,w,w1,w2,nrw,iw,sdet;
  double r,s,eps,min;
  double v[] = new double[n+1];
  
  f=lw;
  w1=lw+rw;
  w=w1+1;
  w2=w-2;
  iw=0;
  sdet=1;
  nrw=n-rw;
  lw1=lw+1;
  q=lw-1;
  for (i=2; i<=lw; i++) {
    q--;
    iw += w1;
    for (j=iw-q; j<=iw; j++) a[j]=0.0;
  }
  iw = -w2;
  q = -lw;
  for (i=1; i<=n; i++) {
    iw += w;
    if (i <= lw1) iw--;
    q += w;
    if (i > nrw) q--;
    v[i]=Math.sqrt(Basic.vecvec(iw,q,0,a,a));
  }
  eps=aux[2];
  min=1.0;
  kk = -w1;
  mk = -lw;
  if (f > nrw) w2 += nrw-f;
  for (k=1; k<=n; k++) {
    if (f < n) f++;
    ik = kk += w;
    mk += lw;
    s=Math.abs(a[kk])/v[k];
    pk=k;
    kk1=kk+1;
    for (i=k+1; i<=f; i++) {
      ik += w1;
      m[mk+i-k]=r=a[ik];
      a[ik]=0.0;
      r=Math.abs(r)/v[i];
      if (r > s) {
        s=r;
        pk=i;
      }
    }
    if (s < min) min=s;
    if (s < eps) {
      aux[3]=k-1;
      aux[5]=s;
      aux[1]=sdet;
      return;
    }
    if (k+w2 >= n) w2--;
    p[k]=pk;
    if (pk != k) {
      v[pk]=v[k];
      pk -= k;
      Basic.ichvec(kk1,kk1+w2,pk*w1,a);
      sdet = -sdet;
      r=m[mk+pk];
      m[mk+pk]=a[kk];
      a[kk]=r;
    } else
      r=a[kk];
    if (r < 0.0) sdet = -sdet;
    iw=kk1;
    lw1=f-k+mk;
    for (i=mk+1; i<=lw1; i++) {
      s = m[i] /= r;
      iw += w1;
      Basic.elmvec(iw,iw+w2,kk1-iw,a,a,-s);
    }
  }
  aux[3]=n;
  aux[5]=min;
  aux[1]=sdet;
}


public static double determbnd(double a[], int n, int lw,
                               int rw, int sgndet)
{
  int i,l;
  double p;

  l=1;
  p=1.0;
  lw += rw+1;
  for (i=1; i<=n; i++) {
    p=a[l]*p;
    l += lw;
  }
  return (Math.abs(p)*sgndet);
}


public static void solbnd(double a[], int n, int lw, int rw,
                          double m[], int p[], double b[])
{
  int f,i,k,kk,w,w1,w2,shift;
  double s;

  f=lw;
  shift = -lw;
  w1=lw-1;
  for (k=1; k<=n; k++) {
    if (f < n) f++;
    shift += w1;
    i=p[k];
    s=b[i];
    if (i != k) {
      b[i]=b[k];
      b[k]=s;
    }
    Basic.elmvec(k+1,f,shift,b,m,-s);
  }
  w1=lw+rw;
  w=w1+1;
  kk=(n+1)*w-w1;
  w2 = -1;
  shift=n*w1;
  for (k=n; k>=1; k--) {
    kk -= w;
    shift -= w1;
    if (w2 < w1) w2++;
    b[k]=(b[k]-Basic.vecvec(k+1,k+w2,shift,b,a))/a[kk];
  }
}


public static void decsolbnd(double a[], int n, int lw, int rw,
                             double aux[], double b[])
{
  int i,j,k,kk,kk1,pk,ik,lw1,f,q,w,w1,w2,iw,nrw,shift,sdet;
  double r,s,eps,min;
  double m[] = new double[lw+1];
  double v[] = new double[n+1];
  
  f=lw;
  sdet=1;
  w1=lw+rw;
  w=w1+1;
  w2=w-2;
  iw=0;
  nrw=n-rw;
  lw1=lw+1;
  q=lw-1;
  for (i=2; i<=lw; i++) {
    q--;
    iw += w1;
    for (j=iw-q; j<=iw; j++) a[j]=0.0;
  }
  iw = -w2;
  q = -lw;
  for (i=1; i<=n; i++) {
    iw += w;
    if (i <= lw1) iw--;
    q += w;
    if (i > nrw) q--;
    v[i]=Math.sqrt(Basic.vecvec(iw,q,0,a,a));
  }
  eps=aux[2];
  min=1.0;
  kk = -w1;
  if (f > nrw) w2 += nrw-f;
  for (k=1; k<=n; k++) {
    if (f < n) f++;
    ik = kk += w;
    s=Math.abs(a[kk])/v[k];
    pk=k;
    kk1=kk+1;
    for (i=k+1; i<=f; i++) {
      ik += w1;
      m[i-k]=r=a[ik];
      a[ik]=0.0;
      r=Math.abs(r)/v[i];
      if (r > s) {
        s=r;
        pk=i;
      }
    }
    if (s < min) min=s;
    if (s < eps) {
      aux[3]=k-1;
      aux[5]=s;
      aux[1]=sdet;
      return;
    }
    if (k+w2 >= n) w2--;
    if (pk != k) {
      v[pk]=v[k];
      pk -= k;
      Basic.ichvec(kk1,kk1+w2,pk*w1,a);
      sdet = -sdet;
      r=b[k];
      b[k]=b[pk+k];
      b[pk+k]=r;
      r=m[pk];
      m[pk]=a[kk];
      a[kk]=r;
    } else
      r=a[kk];
    iw=kk1;
    lw1=f-k;
    if (r < 0.0) sdet = -sdet;
    for (i=1; i<=lw1; i++) {
      s = m[i] /= r;
      iw += w1;
      Basic.elmvec(iw,iw+w2,kk1-iw,a,a,-s);
      b[k+i] -= b[k]*s;
    }
  }
  aux[3]=n;
  aux[5]=min;
  kk=(n+1)*w-w1;
  w2 = -1;
  shift=n*w1;
  for (k=n; k>=1; k--) {
    kk -= w;
    shift -= w1;
    if (w2 < w1) w2++;
    b[k]=(b[k]-Basic.vecvec(k+1,k+w2,shift,b,a))/a[kk];
  }
  aux[1]=sdet;
}


public static void dectri(double sub[], double diag[], double supre[],
                          int n, double aux[])
{
  int i,n1;
  double d,r,s,u,norm,norm1,tol;

  tol=aux[2];
  d=diag[1];
  r=supre[1];
  norm=norm1=Math.abs(d)+Math.abs(r);
  if (Math.abs(d) <= norm1*tol) {
    aux[3]=0.0;
    aux[5]=d;
    return;
  }
  u=supre[1]=r/d;
  s=sub[1];
  n1=n-1;
  for (i=2; i<=n1; i++) {
    d=diag[i];
    r=supre[i];
    norm1=Math.abs(s)+Math.abs(d)+Math.abs(r);
    diag[i] = d -= u*s;
    if (Math.abs(d) <= norm1*tol) {
      aux[3]=i-1;
      aux[5]=d;
      return;
    }
    u=supre[i]=r/d;
    s=sub[i];
    if (norm1 > norm) norm=norm1;
  }
  d=diag[n];
  norm1=Math.abs(d)+Math.abs(s);
  diag[n] = d -= u*s;
  if (Math.abs(d) <= norm1*tol) {
    aux[3]=n1;
    aux[5]=d;
    return;
  }
  if (norm1 > norm) norm=norm1;
  aux[3]=n;
  aux[5]=norm;
}


public static void dectripiv(double sub[], double diag[], double supre[],
              int n, double aid[], double aux[], boolean piv[])
{
  int i,i1,n1,n2;
  double d,r,s,u,t,q,v,w,norm,norm1,norm2,tol;

  tol=aux[2];
  d=diag[1];
  r=supre[1];
  norm=norm2=Math.abs(d)+Math.abs(r);
  n2=n-2;
  for (i=1; i<=n2; i++) {
    i1=i+1;
    s=sub[i];
    t=diag[i1];
    q=supre[i1];
    norm1=norm2;
    norm2=Math.abs(s)+Math.abs(t)+Math.abs(q);
    if (norm2 > norm) norm=norm2;
    if (Math.abs(d)*norm2 < Math.abs(s)*norm1) {
      if (Math.abs(s) <= tol*norm2) {
        aux[3]=i-1;
        aux[5]=s;
        return;
      }
      diag[i]=s;
      u=supre[i]=t/s;
      v=aid[i]=q/s;
      sub[i]=d;
      w = supre[i1] = -v*d;
      d=diag[i1]=r-u*d;
      r=w;
      norm2=norm1;
      piv[i]=true;
    } else {
      if (Math.abs(d) <= tol*norm1) {
        aux[3]=i-1;
        aux[5]=d;
        return;
      }
      u=supre[i]=r/d;
      d=diag[i1]=t-u*s;
      aid[i]=0.0;
      piv[i]=false;
      r=q;
    }
  }
  n1=n-1;
  s=sub[n1];
  t=diag[n];
  norm1=norm2;
  norm2=Math.abs(s)+Math.abs(t);
  if (norm2 > norm) norm=norm2;
  if (Math.abs(d)*norm2 < Math.abs(s)*norm1) {
    if (Math.abs(s) <= tol*norm2) {
      aux[3]=n2;
      aux[5]=s;
      return;
    }
    diag[n1]=s;
    u=supre[n1]=t/s;
    sub[n1]=d;
    d=diag[n]=r-u*d;
    norm2=norm1;
    piv[n1]=true;
  } else {
    if (Math.abs(d) <= tol*norm1) {
      aux[3]=n2;
      aux[5]=d;
      return;
    }
    u=supre[n1]=r/d;
    d=diag[n]=t-u*s;
    piv[n1]=false;
  }
  if (Math.abs(d) <= tol*norm2) {
    aux[3]=n1;
    aux[5]=d;
    return;
  }
  aux[3]=n;
  aux[5]=norm;
}


public static void soltri(double sub[], double diag[], double supre[],
                          int n, double b[])
{
  int i;
  double r;

  r = b[1] /= diag[1];
  for (i=2; i<=n; i++) r=b[i]=(b[i]-sub[i-1]*r)/diag[i];
  for (i=n-1; i>=1; i--) r = b[i] -= supre[i]*r;
}


public static void decsoltri(double sub[], double diag[],
          double supre[], int n, double aux[], double b[])
{
  dectri(sub,diag,supre,n,aux);
  if (aux[3] == n) soltri(sub,diag,supre,n,b);
}


public static void soltripiv(double sub[], double diag[], double supre[],
                   int n, double aid[], boolean piv[], double b[])
{
  int i,n1;
  double bi,bi1,r,s,t;

  n1=n-1;
  for (i=1; i<=n1; i++) {
    if (piv[i]) {
      bi=b[i+1];
      bi1=b[i];
    } else {
      bi=b[i];
      bi1=b[i+1];
    }
    r=b[i]=bi/diag[i];
    b[i+1]=bi1-sub[i]*r;
  }
  r = b[n] /= diag[n];
  t = b[n1] -= supre[n1]*r;
  for (i=n-2; i>=1; i--) {
    s=r;
    r=t;
    t = b[i] -= supre[i]*r + ((piv[i]) ? aid[i]*s : 0.0);
  }
}


public static void decsoltripiv(double sub[], double diag[],
            double supre[], int n, double aux[], double b[])
{
  int i,i1,n1,n2;
  double d,r,s,u,t,q,v,w,norm,norm1,norm2,tol,bi,bi1,bi2;
  boolean piv[] = new boolean[n+1];
  
  tol=aux[2];
  d=diag[1];
  r=supre[1];
  bi=b[1];
  norm=norm2=Math.abs(d)+Math.abs(r);
  n2=n-2;
  for (i=1; i<=n2; i++) {
    i1=i+1;
    s=sub[i];
    t=diag[i1];
    q=supre[i1];
    bi1=b[i1];
    norm1=norm2;
    norm2=Math.abs(s)+Math.abs(t)+Math.abs(q);
    if (norm2 > norm) norm=norm2;
    if (Math.abs(d)*norm2 < Math.abs(s)*norm1) {
      if (Math.abs(s) <= tol*norm2) {
        aux[3]=i-1;
        aux[5]=s;
        return;
      }
      u=supre[i]=t/s;
      b[i] = bi1 /= s;
      bi -= bi1*d;
      v=sub[i]=q/s;
      w = supre[i1] = -v*d;
      d=diag[i1]=r-u*d;
      r=w;
      norm2=norm1;
      piv[i]=true;
    } else {
      if (Math.abs(d) <= tol*norm1) {
        aux[3]=i-1;
        aux[5]=d;
        return;
      }
      u=supre[i]=r/d;
      b[i] = bi /= d;
      bi=bi1-bi*s;
      d=diag[i1]=t-u*s;
      piv[i]=false;
      r=q;
    }
  }
  n1=n-1;
  s=sub[n1];
  t=diag[n];
  norm1=norm2;
  bi1=b[n];
  norm2=Math.abs(s)+Math.abs(t);
  if (norm2 > norm) norm=norm2;
  if (Math.abs(d)*norm2 < Math.abs(s)*norm1) {
    if (Math.abs(s) <= tol*norm2) {
      aux[3]=n2;
      aux[5]=s;
      return;
    }
    u=supre[n1]=t/s;
    b[n1] = bi1 /= s;
    bi -= bi1*d;
    d=r-u*d;
    norm2=norm1;
  } else {
    if (Math.abs(d) <= tol*norm1) {
      aux[3]=n2;
      aux[5]=d;
      return;
    }
    u=supre[n1]=r/d;
    b[n1] = bi /= d;
    bi=bi1-bi*s;
    d=t-u*s;
  }
  if (Math.abs(d) <= tol*norm2) {
    aux[3]=n1;
    aux[5]=d;
    return;
  }
  aux[3]=n;
  aux[5]=norm;
  bi1=b[n]=bi/d;
  bi = b[n1] -= supre[n1]*bi1;
  for (i=n-2; i>=1; i--) {
    bi2=bi1;
    bi1=bi;
    bi = b[i] -= supre[i]*bi1 + ((piv[i]) ? sub[i]*bi2 : 0.0);
  }
}


public static void chldecbnd(double a[], int n, int w, double aux[])
{
  int j,k,jmax,kk,kj,w1,start;
  double r,eps,max;

  max=0.0;
  kk = -w;
  w1=w+1;
  for (j=1; j<=n; j++) {
    kk += w1;
    if (a[kk] > max) max=a[kk];
  }
  jmax=w;
  w1=w+1;
  kk = -w;
  eps=aux[2]*max;
  for (k=1; k<=n; k++) {
    if (k+w > n) jmax--;
    kk += w1;
    start=kk-k+1;
    r=a[kk]-Basic.vecvec(((k <= w1) ? start : kk-w),kk-1,0,a,a);
    if (r <= eps) {
      aux[3]=k-1;
      return;
    }
    a[kk]=r=Math.sqrt(r);
    kj=kk;
    for (j=1; j<=jmax; j++) {
      kj += w;
      a[kj]=(a[kj]-Basic.vecvec(((k+j <= w1) ? start : kk-w+j),
              kk-1,kj-kk,a,a))/r;
    }
  }
  aux[3]=n;
}


public static double chldetermbnd(double a[], int n, int w)
{
  int j,kk,w1;
  double p;

  w1=w+1;
  kk = -w;
  p=1.0;
  for (j=1; j<=n; j++) {
    kk += w1;
    p *= a[kk];
  }
  return (p*p);
}


public static void chlsolbnd(double a[], int n, int w, double b[])
{
  int k,imax,kk,w1;

  kk = -w;
  w1=w+1;
  for (k=1; k<=n; k++) {
    kk += w1;
    b[k]=(b[k]-Basic.vecvec(((k <= w1) ? 1 : k-w),k-1,kk-k,b,a))/a[kk];
  }
  imax = -1;
  for (k=n; k>=1; k--) {
    if (imax < w) imax++;
    b[k]=(b[k]-Basic.scaprd1(kk+w,w,k+1,1,imax,a,b))/a[kk];
    kk -= w1;
  }
}


public static void chldecsolbnd(double a[], int n, int w,
                                double aux[], double b[])
{
  chldecbnd(a,n,w,aux);
  if (aux[3] == n) chlsolbnd(a,n,w,b);
}


public static void decsymtri(double diag[], double co[],
                             int n, double aux[])
{
  int i,n1;
  double d,r,s,u,tol,norm,normr;

  s=0.0;
  tol=aux[2];
  d=diag[1];
  r=co[1];
  norm=normr=Math.abs(d)+Math.abs(r);
  if (Math.abs(d) <= normr*tol) {
    aux[3]=0.0;
    aux[5]=d;
    return;
  }
  u=co[1]=r/d;
  n1=n-1;
  for (i=2; i<=n1; i++) {
    s=r;
    r=co[i];
    d=diag[i];
    normr=Math.abs(s)+Math.abs(d)+Math.abs(r);
    diag[i] = d -= u*s;
    if (Math.abs(d) <= normr*tol) {
      aux[3]=i-1;
      aux[5]=d;
      return;
    }
    u=co[i]=r/d;
    if (normr > norm) norm=normr;
  }
  d=diag[n];
  normr=Math.abs(d)+Math.abs(r);
  diag[n] = d -= u*s;
  if (Math.abs(d) <= normr*tol) {
    aux[3]=n1;
    aux[5]=d;
    return;
  }
  if (normr > norm) norm=normr;
  aux[3]=n;
  aux[5]=norm;
}


public static void solsymtri(double diag[], double co[],
                             int n, double b[])
{
  int i;
  double r,s;

  r=b[1];
  b[1]=r/diag[1];
  for (i=2; i<=n; i++) {
    r=b[i]-co[i-1]*r;
    b[i]=r/diag[i];
  }
  s=b[n];
  for (i=n-1; i>=1; i--) s = b[i] -= co[i]*s;
}


public static void decsolsymtri(double diag[], double co[], int n,
                                double aux[], double b[])
{
  decsymtri(diag,co,n,aux);
  if (aux[3] == n) solsymtri(diag,co,n,b);
}


public static void conjgrad(LA_conjgrad_methods method,
                            double x[], double r[], int l, int n,
                            int iterate[], double norm2[])
{
  int i;
  double a,b,prr,rrp;
  double p[] = new double[n+1];
  double ap[] = new double[n+1];
  
  rrp=0.0;
  prr=1.0;
  iterate[0]=0;
  do {
    if (iterate[0] == 0) {
      method.matvec(x,p);
      for (i=l; i<=n; i++) p[i] = r[i] -= p[i];
      prr=Basic.vecvec(l,n,0,r,r);
    } else {
      b=rrp/prr;
      prr=rrp;
      for (i=l; i<=n; i++) p[i]=r[i]+b*p[i];
    }
    method.matvec(p,ap);
    a=prr/Basic.vecvec(l,n,0,p,ap);
    Basic.elmvec(l,n,0,x,p,a);
    Basic.elmvec(l,n,0,r,ap,-a);
    norm2[0]=rrp=Basic.vecvec(l,n,0,r,r);
    (iterate[0])++;
  } while (method.goon(iterate,norm2));
}


public static void eqilbr(double a[][], int n, double em[],
                          double d[], int inter[])
{
  int i,im,i1,p,q,j,t,count,exponent,ni;
  double c,r,eps,omega,factor,di;

  factor=1.0/(2.0*Math.log(2.0));
  eps=em[0];
  omega=1.0/eps;
  t=p=1;
  q=ni=i=n;
  count=((n+1)*n)/2;
  for (j=1; j<=n; j++) {
    d[j]=1.0;
    inter[j]=0;
  }
  i = (i < q) ? i+1 : p;
  while (count > 0 && ni > 0) {
    count--;
    im=i-1;
    i1=i+1;
    c=Math.sqrt(Basic.tammat(p,im,i,i,a,a)+Basic.tammat(i1,q,i,i,a,a));
    r=Math.sqrt(Basic.mattam(p,im,i,i,a,a)+Basic.mattam(i1,q,i,i,a,a));
    if (c*omega <= r*eps) {
      inter[t]=i;
      ni=q-p;
      t++;
      if (p != i) {
        Basic.ichcol(1,n,p,i,a);
        Basic.ichrow(1,n,p,i,a);
        di=d[i];
        d[i]=d[p];
        d[p]=di;
      }
      p++;
    } else
      if (r*omega <= c*eps) {
        inter[t] = -i;
        ni=q-p;
        t++;
        if (q != i) {
          Basic.ichcol(1,n,q,i,a);
          Basic.ichrow(1,n,q,i,a);
          di=d[i];
          d[i]=d[q];
          d[q]=di;
        }
        q--;
      } else {
        exponent=(int) (Math.log(r/c)*factor);
        if (Math.abs(exponent) > 1.0) {
          ni=q-p;
          c=Math.pow(2.0,exponent);
          r=1.0/c;
          d[i] *= c;
          for (j=1; j<=im; j++) {
            a[j][i] *=c;
            a[i][j] *= r;
          }
          for (j=i1; j<=n; j++) {
            a[j][i] *=c;
            a[i][j] *= r;
          }
        } else
          ni--;
      }
    i = (i < q) ? i+1 : p;
  }
}


public static void baklbr(int n, int n1, int n2, double d[],
                          int inter[], double vec[][])
{
  int i,j,k,p,q;
  double di;

  p=1;
  q=n;
  for (i=1; i<=n; i++) {
    di=d[i];
    if (di != 1)
      for (j=n1; j<=n2; j++) vec[i][j] *= di;
    k=inter[i];
    if (k > 0)
      p++;
    else
      if (k < 0) q--;
  }
  for (i=p-1+n-q; i>=1; i--) {
    k=inter[i];
    if (k > 0) {
      p--;
      if (k != p) Basic.ichrow(n1,n2,k,p,vec);
    } else {
      q++;
      if (-k != q) Basic.ichrow(n1,n2,-k,q,vec);
    }
  }
}


public static void eqilbrcom(double a1[][], double a2[][], int n, 
                             double em[], double d[], int inter[])
{
  int i,p,q,j,t,count,exponent,ni,im,i1;
  double c,r,eps,di;

  eps=em[0]*em[0];
  t=p=1;
  q=ni=i=n;
  count=(int) em[6];
  for (j=1; j<=n; j++) {
    d[j]=1.0;
    inter[j]=0;
  }
  i = (i < q) ? i+1 : p;
  while (count > 0 && ni > 0) {
    count--;
    im=i-1;
    i1=i+1;
    c=Basic.tammat(p,im,i,i,a1,a1)+Basic.tammat(i1,q,i,i,a1,a1)+
      Basic.tammat(p,im,i,i,a2,a2)+Basic.tammat(i1,q,i,i,a2,a2);
    r=Basic.mattam(p,im,i,i,a1,a1)+Basic.mattam(i1,q,i,i,a1,a1)+
      Basic.mattam(p,im,i,i,a2,a2)+Basic.mattam(i1,q,i,i,a2,a2);
    if (c/eps <= r) {
      inter[t]=i;
      ni=q-p;
      t++;
      if (p != i) {
        Basic.ichcol(1,n,p,i,a1);
        Basic.ichrow(1,n,p,i,a1);
        Basic.ichcol(1,n,p,i,a2);
        Basic.ichrow(1,n,p,i,a2);
        di=d[i];
        d[i]=d[p];
        d[p]=di;
      }
      p++;
    } else
      if (r/eps <= c) {
        inter[t] = -i;
        ni=q-p;
        t++;
        if (q != i) {
          Basic.ichcol(1,n,q,i,a1);
          Basic.ichrow(1,n,q,i,a1);
          Basic.ichcol(1,n,q,i,a2);
          Basic.ichrow(1,n,q,i,a2);
          di=d[i];
          d[i]=d[q];
          d[q]=di;
        }
        q--;
      } else {
        exponent=(int) Math.ceil(Math.log(r/c)*0.36067);
        if (Math.abs(exponent) > 1) {
          ni=q-p;
          c=Math.pow(2.0,exponent);
          d[i] *= c;
          for (j=1; j<=im; j++) {
            a1[j][i] *= c;
            a1[i][j] /= c;
            a2[j][i] *= c;
            a2[i][j] /= c;
          }
          for (j=i1; j<=n; j++) {
            a1[j][i] *= c;
            a1[i][j] /= c;
            a2[j][i] *= c;
            a2[i][j] /= c;
          }
        } else
          ni--;
      }
    i = (i < q) ? i+1 : p;
  }
  em[7]=em[6]-count;
}


public static void baklbrcom(int n, int n1, int n2, double d[],
                             int inter[], double vr[][], double vi[][])
{
  baklbr(n,n1,n2,d,inter,vr);
  baklbr(n,n1,n2,d,inter,vi);
}


public static void tfmsymtri2(double a[][], int n, double d[],
                              double b[], double bb[], double em[])
{
  int i,j,r,r1;
  double w,x,a1,b0,bb0,machtol,norm;

  norm=0.0;
  for (j=1; j<=n; j++) {
    w=0.0;
    for (i=1; i<=j; i++) w += Math.abs(a[i][j]);
    for (i=j+1; i<=n; i++) w += Math.abs(a[j][i]);
    if (w > norm) norm=w;
  }
  machtol=em[0]*norm;
  em[1]=norm;
  r=n;
  for (r1=n-1; r1>=1; r1--) {
    d[r]=a[r][r];
    x=Basic.tammat(1,r-2,r,r,a,a);
    a1=a[r1][r];
    if (Math.sqrt(x) <= machtol) {
      b0=b[r1]=a1;
      bb[r1]=b0*b0;
      a[r][r]=1.0;
    } else {
      bb0=bb[r1]=a1*a1+x;
      b0 = (a1 > 0.0) ? -Math.sqrt(bb0) : Math.sqrt(bb0);
      a1=a[r1][r]=a1-b0;
      w=a[r][r]=1.0/(a1*b0);
      for (j=1; j<=r1; j++)
        b[j]=(Basic.tammat(1,j,j,r,a,a)+Basic.matmat(j+1,r1,j,r,a,a))*w;
      Basic.elmveccol(1,r1,r,b,a,Basic.tamvec(1,r1,r,a,b)*w*0.5);
      for (j=1; j<=r1; j++) {
        Basic.elmcol(1,j,j,r,a,a,b[j]);
        Basic.elmcolvec(1,j,j,a,b,a[j][r]);
      }
      b[r1]=b0;
    }
    r=r1;
  }
  d[1]=a[1][1];
  a[1][1]=1.0;
  b[n]=bb[n]=0.0;
}


public static void baksymtri2(double a[][], int n, int n1, int n2,
                              double vec[][])
{
  int j,k;
  double w;

  for (j=2; j<=n; j++) {
    w=a[j][j];
    if (w < 0.0)
      for (k=n1; k<=n2; k++)
        Basic.elmcol(1,j-1,k,j,vec,a,Basic.tammat(1,j-1,j,k,a,vec)*w);
  }
}


public static void tfmprevec(double a[][], int n)
{
  int i,j,j1,k;
  double ab;

  j1=1;
  for (j=2; j<=n; j++) {
    for (i=1; i<=j1-1; i++) a[i][j1]=0.0;
    for (i=j; i<=n; i++) a[i][j1]=0.0;
    a[j1][j1]=1.0;
    ab=a[j][j];
    if (ab < 0)
      for (k=1; k<=j1; k++)
        Basic.elmcol(1,j1,k,j,a,a,Basic.tammat(1,j1,j,k,a,a)*ab);
    j1=j;
  }
  for (i=n-1; i>=1; i--) a[i][n]=0.0;
  a[n][n]=1.0;
}


public static void tfmsymtri1(double a[], int n, double d[], double b[],
                              double bb[], double em[])
{
  int i,j,r,r1,p,q,ti,tj;
  double s,w,x,a1,b0,bb0,norm,machtol;

  norm=0.0;
  tj=0;
  for (j=1; j<=n; j++) {
    w=0.0;
    for (i=1; i<=j; i++) w += Math.abs(a[i+tj]);
    tj += j;
    ti=tj+j;
    for (i=j+1; i<=n; i++) {
      w += Math.abs(a[ti]);
      ti += i;
    }
    if (w > norm) norm=w;
  }
  machtol=em[0]*norm;
  em[1]=norm;
  q=((n+1)*n)/2;
  r=n;
  for (r1=n-1; r1>=1; r1--) {
    p=q-r;
    d[r]=a[q];
    x=Basic.vecvec(p+1,q-2,0,a,a);
    a1=a[q-1];
    if (Math.sqrt(x) <= machtol) {
      b0=b[r1]=a1;
      bb[r1]=b0*b0;
      a[q]=1.0;
    } else {
      bb0=bb[r1]=a1*a1+x;
      b0 = (a1 > 0.0) ? -Math.sqrt(bb0) : Math.sqrt(bb0);
      a1=a[q-1]=a1-b0;
      w=a[q]=1.0/(a1*b0);
      tj=0;
      for (j=1; j<=r1; j++) {
        ti=tj+j;
        s=Basic.vecvec(tj+1,ti,p-tj,a,a);
        tj=ti+j;
        b[j]=(Basic.seqvec(j+1,r1,tj,p,a,a)+s)*w;
        tj=ti;
      }
      Basic.elmvec(1,r1,p,b,a,Basic.vecvec(1,r1,p,b,a)*w*0.5);
      tj=0;
      for (j=1; j<=r1; j++) {
        ti=tj+j;
        Basic.elmvec(tj+1,ti,p-tj,a,a,b[j]);
        Basic.elmvec(tj+1,ti,-tj,a,b,a[j+p]);
        tj=ti;
      }
      b[r1]=b0;
    }
    q=p;
    r=r1;
  }
  d[1]=a[1];
  a[1]=1.0;
  b[n]=bb[n]=0.0;
}


public static void baksymtri1(double a[], int n, int n1, int n2,
                              double vec[][])
{
  int j,j1,k,ti,tj;
  double w;
  double auxvec[] = new double[n+1];
  
  for (k=n1; k<=n2; k++) {
    for (j=1; j<=n; j++) auxvec[j]=vec[j][k];
    tj=j1=1;
    for (j=2; j<=n; j++) {
      ti=tj+j;
      w=a[ti];
      if (w < 0.0)
        Basic.elmvec(1,j1,tj,auxvec,a,Basic.vecvec(1,j1,tj,auxvec,a)*w);
      j1=j;
      tj=ti;
    }
    for (j=1; j<=n; j++) vec[j][k]=auxvec[j];
  }
}


public static void tfmreahes(double a[][], int n,
                             double em[], int index[])
{
  int i,j,j1,k,l;
  double s,t,machtol,macheps,norm;
  double b[] = new double[n];
  
  macheps=em[0];
  norm=0.0;
  for (i=1; i<=n; i++) {
    s=0.0;
    for (j=1; j<=n; j++) s += Math.abs(a[i][j]);
    if (s > norm) norm=s;
  }
  em[1]=norm;
  machtol=norm*macheps;
  index[1]=0;
  for (j=2; j<=n; j++) {
    j1=j-1;
    l=0;
    s=machtol;
    for (k=j+1; k<=n; k++) {
      t=Math.abs(a[k][j1]);
      if (t > s) {
        l=k;
        s=t;
      }
    }
    if (l != 0) {
      if (Math.abs(a[j][j1]) < s) {
        Basic.ichrow(1,n,j,l,a);
        Basic.ichcol(1,n,j,l,a);
      } else
        l=j;
      t=a[j][j1];
      for (k=j+1; k<=n; k++) a[k][j1] /=t;
    } else
      for (k=j+1; k<=n; k++) a[k][j1]=0.0;
    for (i=1; i<=n; i++)
      b[i-1] = a[i][j] +=
        ((l == 0) ? 0.0 : Basic.matmat(j+1,n,i,j1,a,a)) -
        Basic.matvec(1,(j1 < i-2) ? j1 : i-2,i,a,b);
    index[j]=l;
  }
}


public static void bakreahes1(double a[][], int n, int index[],
                              double v[])
{
  int i,l;
  double w;
  double x[] = new double[n+1];
  
  for (i=2; i<=n; i++) x[i-1]=v[i];
  for (i=n; i>=2; i--) {
    v[i] += Basic.matvec(1,i-2,i,a,x);
    l=index[i];
    if (l > i) {
      w=v[i];
      v[i]=v[l];
      v[l]=w;
    }
  }
}


public static void bakreahes2(double a[][], int n, int n1, int n2,
                              int index[], double vec[][])
{
  int i,l,k;
  double u[] = new double[n+1];
  
  for (i=n; i>=2; i--) {
    for (k=i-2; k>=1; k--) u[k+1]=a[i][k];
    for (k=n1; k<=n2; k++) vec[i][k] += Basic.tamvec(2,i-1,k,vec,u);
    l=index[i];
    if (l > i) Basic.ichrow(n1,n2,i,l,vec);
  }
}


public static void hshhrmtri(double a[][], int n, double d[], double b[],
                   double bb[], double em[], double tr[], double ti[])
{
  int i,j,j1,jm1,r,rm1;
  double nrm,w,tol2,x,ar,ai,h,k,t,q,ajr,arj,bj,bbj;
  double mod[] = new double[1];
  double c[] = new double[1];
  double s[] = new double[1];

  nrm=0.0;
  for (i=1; i<=n; i++) {
    w=Math.abs(a[i][i]);
    for (j=i-1; j>=1; j--) w += Math.abs(a[i][j])+Math.abs(a[j][i]);
    for (j=i+1; j<=n; j++) w += Math.abs(a[i][j])+Math.abs(a[j][i]);
    if (w > nrm) nrm=w;
  }
  t=em[0]*nrm;
  tol2=t*t;
  em[1]=nrm;
  r=n;
  for (rm1=n-1; rm1>=1; rm1--) {
    x=Basic.tammat(1,r-2,r,r,a,a)+Basic.mattam(1,r-2,r,r,a,a);
    ar=a[rm1][r];
    ai = -a[r][rm1];
    d[r]=a[r][r];
    Basic.carpol(ar,ai,mod,c,s);
    if (x < tol2) {
      a[r][r] = -1.0;
      b[rm1]=mod[0];
      bb[rm1]=mod[0]*mod[0];
    } else {
      h=mod[0]*mod[0]+x;
      k=Math.sqrt(h);
      t=a[r][r]=h+mod[0]*k;
      if (ar == 0.0 && ai == 0.0)
        a[rm1][r]=k;
      else {
        a[rm1][r]=ar+c[0]*k;
        a[r][rm1] = -ai-s[0]*k;
        s[0] = -s[0];
      }
      c[0] = -c[0];
      j=1;
      jm1=0;
      for (j1=2; j1<=r; j1++) {
        b[j]=(Basic.tammat(1,j,j,r,a,a)+Basic.matmat(j1,rm1,j,r,a,a)+
            Basic.mattam(1,jm1,j,r,a,a)-Basic.matmat(j1,rm1,r,j,a,a))/t;
        bb[j]=(Basic.matmat(1,jm1,j,r,a,a)-Basic.tammat(j1,rm1,j,r,a,a)-
            Basic.matmat(1,j,r,j,a,a)-Basic.mattam(j1,rm1,j,r,a,a))/t;
        jm1=j;
        j=j1;
      }
      q=(Basic.tamvec(1,rm1,r,a,b)-Basic.matvec(1,rm1,r,a,bb))/t/2.0;
      Basic.elmveccol(1,rm1,r,b,a,-q);
      Basic.elmvecrow(1,rm1,r,bb,a,q);
      j=1;
      for (j1=2; j1<=r; j1++) {
        ajr=a[j][r];
        arj=a[r][j];
        bj=b[j];
        bbj=bb[j];
        Basic.elmrowvec(j,rm1,j,a,b,-ajr);
        Basic.elmrowvec(j,rm1,j,a,bb,arj);
        Basic.elmrowcol(j,rm1,j,r,a,a,-bj);
        Basic.elmrow(j,rm1,j,r,a,a,bbj);
        Basic.elmcolvec(j1,rm1,j,a,b,-arj);
        Basic.elmcolvec(j1,rm1,j,a,bb,-ajr);
        Basic.elmcol(j1,rm1,j,r,a,a,bbj);
        Basic.elmcolrow(j1,rm1,j,r,a,a,bj);
        j=j1;
      }
      bb[rm1]=h;
      b[rm1]=k;
    }
    tr[rm1]=c[0];
    ti[rm1]=s[0];
    r=rm1;
  }
  d[1]=a[1][1];
}


public static void hshhrmtrival(double a[][], int n, double d[],
                                double bb[], double em[])
{
  int i,j,j1,jm1,r,rm1;
  double nrm,w,tol2,x,ar,ai,h,t,q,ajr,arj,dj,bbj,mod2;

  nrm=0.0;
  for (i=1; i<=n; i++) {
    w=Math.abs(a[i][i]);
    for (j=i-1; j>=1; j--) w += Math.abs(a[i][j])+Math.abs(a[j][i]);
    for (j=i+1; j<=n; j++) w += Math.abs(a[i][j])+Math.abs(a[j][i]);
    if (w > nrm) nrm=w;
  }
  t=em[0]*nrm;
  tol2=t*t;
  em[1]=nrm;
  r=n;
  for (rm1=n-1; rm1>=1; rm1--) {
    x=Basic.tammat(1,r-2,r,r,a,a)+Basic.mattam(1,r-2,r,r,a,a);
    ar=a[rm1][r];
    ai = -a[r][rm1];
    d[r]=a[r][r];
    if (x < tol2)
      bb[rm1]=ar*ar+ai*ai;
    else {
      mod2=ar*ar+ai*ai;
      if (mod2 == 0.0) {
        a[rm1][r]=Math.sqrt(x);
        t=x;
      } else {
        x += mod2;
        h=Math.sqrt(mod2*x);
        t=x+h;
        h=1.0+x/h;
        a[r][rm1] = -ai*h;
        a[rm1][r]=ar*h;
      }
      j=1;
      jm1=0;
      for (j1=2; j1<=r; j1++) {
        d[j]=(Basic.tammat(1,j,j,r,a,a)+Basic.matmat(j1,rm1,j,r,a,a)+
            Basic.mattam(1,jm1,j,r,a,a)-Basic.matmat(j1,rm1,r,j,a,a))/t;
        bb[j]=(Basic.matmat(1,jm1,j,r,a,a)-Basic.tammat(j1,rm1,j,r,a,a)-
            Basic.matmat(1,j,r,j,a,a)-Basic.mattam(j1,rm1,j,r,a,a))/t;
        jm1=j;
        j=j1;
      }
      q=(Basic.tamvec(1,rm1,r,a,d)-Basic.matvec(1,rm1,r,a,bb))/t/2.0;
      Basic.elmveccol(1,rm1,r,d,a,-q);
      Basic.elmvecrow(1,rm1,r,bb,a,q);
      j=1;
      for (j1=2; j1<=r; j1++) {
        ajr=a[j][r];
        arj=a[r][j];
        dj=d[j];
        bbj=bb[j];
        Basic.elmrowvec(j,rm1,j,a,d,-ajr);
        Basic.elmrowvec(j,rm1,j,a,bb,arj);
        Basic.elmrowcol(j,rm1,j,r,a,a,-dj);
        Basic.elmrow(j,rm1,j,r,a,a,bbj);
        Basic.elmcolvec(j1,rm1,j,a,d,-arj);
        Basic.elmcolvec(j1,rm1,j,a,bb,-ajr);
        Basic.elmcol(j1,rm1,j,r,a,a,bbj);
        Basic.elmcolrow(j1,rm1,j,r,a,a,dj);
        j=j1;
      }
      bb[rm1]=x;
    }
    r=rm1;
  }
  d[1]=a[1][1];
}


public static void bakhrmtri(double a[][], int n, int n1, int n2,
          double vecr[][], double veci[][], double tr[], double ti[])
{
  int i,j,r,rm1;
  double c,s,t,qr,qi;
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];

  for (i=1; i<=n; i++)
    for (j=n1; j<=n2; j++) veci[i][j]=0.0;
  c=1.0;
  s=0.0;
  for (j=n-1; j>=1; j--) {
    Basic.commul(c,s,tr[j],ti[j],tmp1,tmp2);
    c=tmp1[0];
    s=tmp2[0];
    Basic.comrowcst(n1,n2,j,vecr,veci,c,s);
  }
  rm1=2;
  for (r=3; r<=n; r++) {
    t=a[r][r];
    if (t > 0.0)
      for (j=n1; j<=n2; j++) {
        qr=(Basic.tammat(1,rm1,r,j,a,vecr)-
            Basic.matmat(1,rm1,r,j,a,veci))/t;
        qi=(Basic.tammat(1,rm1,r,j,a,veci)+
            Basic.matmat(1,rm1,r,j,a,vecr))/t;
        Basic.elmcol(1,rm1,j,r,vecr,a,-qr);
        Basic.elmcolrow(1,rm1,j,r,vecr,a,-qi);
        Basic.elmcolrow(1,rm1,j,r,veci,a,qr);
        Basic.elmcol(1,rm1,j,r,veci,a,-qi);
      }
    rm1=r;
  }
}


public static void hshcomhes(double ar[][], double ai[][], int n,
       double em[], double b[], double tr[], double ti[], double del[])
{
  int r,rm1,i,nm1;
  double tol,t,xr,xi;
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];
  double tmp3[] = new double[1];
  double tmp4[] = new double[1];
  boolean temp;

  nm1=n-1;
  t=em[0]*em[1];
  tol=t*t;
  rm1=1;
  for (r=2; r<=nm1; r++) {
    temp=Basic.hshcomcol(r,n,rm1,ar,ai,tol,tmp1,tmp2,tmp3,tmp4);
    b[rm1]=tmp1[0];
    tr[r]=tmp2[0];
    ti[r]=tmp3[0];
    t=tmp4[0];
    if (temp) {
      for (i=1; i<=n; i++) {
        xr=(Basic.matmat(r,n,i,rm1,ai,ai)-
            Basic.matmat(r,n,i,rm1,ar,ar))/t;
        xi=(-Basic.matmat(r,n,i,rm1,ar,ai)-
             Basic.matmat(r,n,i,rm1,ai,ar))/t;
        Basic.elmrowcol(r,n,i,rm1,ar,ar,xr);
        Basic.elmrowcol(r,n,i,rm1,ar,ai,xi);
        Basic.elmrowcol(r,n,i,rm1,ai,ar,xi);
        Basic.elmrowcol(r,n,i,rm1,ai,ai,-xr);
      }
      Basic.hshcomprd(r,n,r,n,rm1,ar,ai,ar,ai,t);
    }
    del[rm1]=t;
    rm1=r;
  }
  if (n > 1) {
    Basic.carpol(ar[n][nm1],ai[n][nm1],tmp1,tmp2,tmp3);
    b[nm1]=tmp1[0];
    tr[n]=tmp2[0];
    ti[n]=tmp3[0];
  }
  rm1=1;
  tr[1]=1.0;
  ti[1]=0.0;
  for (r=2; r<=n; r++) {
    Basic.commul(tr[rm1],ti[rm1],tr[r],ti[r],tmp1,tmp2);
    tr[r]=tmp1[0];
    ti[r]=tmp2[0];
    Basic.comcolcst(1,rm1,r,ar,ai,tr[r],ti[r]);
    Basic.comrowcst(r+1,n,r,ar,ai,tr[r],-ti[r]);
    rm1=r;
  }
}


public static void bakcomhes(double ar[][], double ai[][],
              double tr[], double ti[], double del[], double vr[][],
              double vi[][], int n, int n1, int n2)
{
  int i,r,rm1;
  double h;

  for (i=2; i<=n; i++) Basic.comrowcst(n1,n2,i,vr,vi,tr[i],ti[i]);
  r=n-1;
  for (rm1=n-2; rm1>=1; rm1--) {
    h=del[rm1];
    if (h > 0.0) Basic.hshcomprd(r,n,n1,n2,rm1,vr,vi,ar,ai,h);
    r=rm1;
  }
}


public static void hshreabid(double a[][], int m, int n, double d[],
                             double b[], double em[])
{
  int i,j,i1;
  double norm,machtol,w,s,f,g,h;

  norm=0.0;
  for (i=1; i<=m; i++) {
    w=0.0;
    for (j=1; j<=n; j++) w += Math.abs(a[i][j]);
    if (w > norm) norm=w;
  }
  machtol=em[0]*norm;
  em[1]=norm;
  for (i=1; i<=n; i++) {
    i1=i+1;
    s=Basic.tammat(i1,m,i,i,a,a);
    if (s < machtol)
      d[i]=a[i][i];
    else {
      f=a[i][i];
      s += f*f;
      d[i] = g = (f < 0.0) ? Math.sqrt(s) : -Math.sqrt(s);
      h=f*g-s;
      a[i][i]=f-g;
      for (j=i1; j<=n; j++)
        Basic.elmcol(i,m,j,i,a,a,Basic.tammat(i,m,i,j,a,a)/h);
    }
    if (i < n) {
      s=Basic.mattam(i1+1,n,i,i,a,a);
      if (s < machtol)
        b[i]=a[i][i1];
      else {
        f=a[i][i1];
        s += f*f;
        b[i] = g = (f < 0.0) ? Math.sqrt(s) : -Math.sqrt(s);
        h=f*g-s;
        a[i][i1]=f-g;
        for (j=i1; j<=m; j++)
          Basic.elmrow(i1,n,j,i,a,a,Basic.mattam(i1,n,i,j,a,a)/h);
      }
    }
  }
}


public static void psttfmmat(double a[][], int n,
                             double v[][], double b[])
{
  int i,i1,j;
  double h;

  i1=n;
  v[n][n]=1.0;
  for (i=n-1; i>=1; i--) {
    h=b[i]*a[i][i1];
    if (h < 0.0) {
      for (j=i1; j<=n; j++) v[j][i]=a[i][j]/h;
      for (j=i1; j<=n; j++)
        Basic.elmcol(i1,n,j,i,v,v,Basic.matmat(i1,n,i,j,a,v));
    }
    for (j=i1; j<=n; j++) v[i][j]=v[j][i]=0.0;
    v[i][i]=1.0;
    i1=i;
  }
}


public static void pretfmmat(double a[][], int m, int n, double d[])
{
  int i,i1,j;
  double g,h;

  for (i=n; i>=1; i--) {
    i1=i+1;
    g=d[i];
    h=g*a[i][i];
    for (j=i1; j<=n; j++) a[i][j]=0.0;
    if (h < 0.0) {
      for (j=i1; j<=n; j++)
        Basic.elmcol(i,m,j,i,a,a,Basic.tammat(i1,m,i,j,a,a)/h);
      for (j=i; j<=m; j++) a[j][i] /= g;
    } else
      for (j=i; j<=m; j++) a[j][i]=0.0;
    a[i][i] += 1.0;
  }
}


public static void valsymtri(double d[], double bb[], int n, int n1,
                             int n2, double val[], double em[])
{
  boolean extrapolate;
  int k,ext;
  double max,x,y,macheps,norm,re,machtol,lambda,
         c,fc,b,fb,a,fa,dd,fd,fdb,fda,w,mb,tol,m,p,q;
  int count[] = new int[1];
  double lb[] = new double[1];
  double ub[] = new double[1];

  fd=dd=0.0;
  macheps=em[0];
  norm=em[1];
  re=em[2];
  machtol=norm*macheps;
  max=norm/macheps;
  count[0]=0;
  ub[0]=1.1*norm;
  lb[0] = -ub[0];
  lambda=ub[0];
  for (k=n1; k<=n2; k++) {
    y=ub[0];
    lb[0] = -1.1*norm;
    x=lb[0];

    /* look for the zero of the polynomial function */

    b=x;
    fb=sturm(d,bb,n,x,k,machtol,max,count,lb,ub);
    a=x=y;
    fa=sturm(d,bb,n,x,k,machtol,max,count,lb,ub);
    c=a;
    fc=fa;
    ext=0;
    extrapolate=true;
    while (extrapolate) {
      if (Math.abs(fc) < Math.abs(fb)) {
        if (c != a) {
          dd=a;
          fd=fa;
        }
        a=b;
        fa=fb;
        b=x=c;
        fb=fc;
        c=a;
        fc=fa;
      }
      tol=Math.abs(x)*re+machtol;
      m=(c+b)*0.5;
      mb=m-b;
      if (Math.abs(mb) > tol) {
        if (ext > 2)
          w=mb;
        else {
          if (mb == 0.0)
            tol=0.0;
          else
            if (mb < 0.0) tol = -tol;
          p=(b-a)*fb;
          if (ext <= 1)
            q=fa-fb;
          else {
            fdb=(fd-fb)/(dd-b);
            fda=(fd-fa)/(dd-a);
            p *= fda;
            q=fdb*fa-fda*fb;
          }
          if (p < 0.0) {
            p = -p;
            q = -q;
          }
          w=(p<Double.MIN_VALUE || p<=q*tol) ? tol : ((p<mb*q) ? p/q : mb);
        }
        dd=a;
        fd=fa;
        a=b;
        fa=fb;
        x = b += w;
        fb=sturm(d,bb,n,x,k,machtol,max,count,lb,ub);
        if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
          c=a;
          fc=fa;
          ext=0;
        } else
          ext = (w == mb) ? 0 : ext+1;
      } else
        break;
    }
    y=c;

    /* end of the zero finding procedure */

    val[k] = lambda = (x > lambda) ? lambda : x;
    if (ub[0] > x)
      ub[0] = (x > y) ? x : y;
  }
  em[3]=count[0];
}


static private double sturm(double d[], double bb[], int n, double x,
                            int k, double machtol, double max,
                            int count[], double lb[], double ub[])
{
  /* this sturm sequence procedure is used internally by VALSYMTRI */

  int p,i;
  double f;

  (count[0])++;
  p=k;
  f=d[1]-x;
  for (i=2; i<=n; i++) {
    if (f <= 0.0) {
      p++;
      if (p > n) return ((p == n) ? f : (n-p)*max);
    } else
      if (p < i-1) {
        lb[0] = x;
        return ((p == n) ? f : (n-p)*max);
      }
    if (Math.abs(f) < machtol)
      f = (f <= 0.0) ? -machtol : machtol;
    f=d[i]-x-bb[i-1]/f;
  }
  if (p == n || f <= 0.0)
    if (x < ub[0]) ub[0] = x;
  else
    lb[0] = x;
  return ((p == n) ? f : (n-p)*max);
}


public static void vecsymtri(double d[], double b[], int n, int n1,
                   int n2, double val[], double vec[][], double em[])
{
  boolean iterate;
  int i,j,k,count,maxcount,countlim,orth,ind;
  double bi,bi1,u,w,y,mi1,lambda,oldlambda,ortheps,valspread,spr,
         res,maxres,norm,newnorm,oldnorm,machtol,vectol;
  boolean index[] = new boolean[n+1];
  double m[] = new double[n+1];
  double p[] = new double[n+1];
  double q[] = new double[n+1];
  double r[] = new double[n+1];
  double x[] = new double[n+1];
  
  oldlambda=res=0.0;
  norm=em[1];
  machtol=em[0]*norm;
  valspread=em[4]*norm;
  vectol=em[6]*norm;
  countlim=(int) em[8];
  ortheps=Math.sqrt(em[0]);
  maxcount=ind=0;
  maxres=0.0;
  if (n1 > 1) {
    orth=(int) em[5];
    oldlambda=val[n1-orth];
    for (k=n1-orth+1; k<=n1-1; k++) {
      lambda=val[k];
      spr=oldlambda-lambda;
      if (spr < machtol) lambda=oldlambda-machtol;
      oldlambda=lambda;
    }
  } else
    orth=1;
  for (k=n1; k<=n2; k++) {
    lambda=val[k];
    if (k > 1) {
      spr=oldlambda-lambda;
      if (spr < valspread) {
        if (spr < machtol) lambda=oldlambda-machtol;
        orth++;
      } else
        orth=1;
    }
    count=0;
    u=d[1]-lambda;
    bi=w=b[1];
    if (Math.abs(bi) < machtol) bi=machtol;
    for (i=1; i<=n-1; i++) {
      bi1=b[i+1];
      if (Math.abs(bi1) < machtol) bi1=machtol;
      if (Math.abs(bi) >= Math.abs(u)) {
        mi1=m[i+1]=u/bi;
        p[i]=bi;
        y=q[i]=d[i+1]-lambda;
        r[i]=bi1;
        u=w-mi1*y;
        w = -mi1*bi1;
        index[i]=true;
      } else {
        mi1=m[i+1]=bi/u;
        p[i]=u;
        q[i]=w;
        r[i]=0.0;
        u=d[i+1]-lambda-mi1*w;
        w=bi1;
        index[i]=false;
      }
      x[i]=1.0;
      bi=bi1;
    } /* transform */
    p[n] = (Math.abs(u) < machtol) ? machtol : u;
    q[n]=r[n]=0.0;
    x[n]=1.0;
    iterate=true;
    while (iterate) {
      u=w=0.0;
      for (i=n; i>=1; i--) {
        y=u;
        u=x[i]=(x[i]-q[i]*u-r[i]*w)/p[i];
        w=y;
      } /* next iteration */
      newnorm=Math.sqrt(Basic.vecvec(1,n,0,x,x));
      if (orth > 1) {
        oldnorm=newnorm;
        for (j=k-orth+1; j<=k-1; j++)
          Basic.elmveccol(1,n,j,x,vec,-Basic.tamvec(1,n,j,vec,x));
        newnorm=Math.sqrt(Basic.vecvec(1,n,0,x,x));
        if (newnorm < ortheps*oldnorm) {
          ind++;
          count=1;
          for (i=1; i<=ind-1; i++) x[i]=0.0;
          for (i=ind+1; i<=n; i++) x[i]=0.0;
          x[ind]=1.0;
          if (ind == n) ind=0;
          w=x[1];
          for (i=2; i<=n; i++) {
            if (index[i-1]) {
              u=w;
              w=x[i-1]=x[i];
            } else
              u=x[i];
            w=x[i]=u-m[i]*w;
          }
          continue; /* iterate on */
        } /* new start */
      } /* orthogonalization */
      res=1.0/newnorm;
      if (res > vectol || count == 0) {
        count++;
        if (count <= countlim) {
          for (i=1; i<=n; i++) x[i] *= res;
          w=x[1];
          for (i=2; i<=n; i++) {
            if (index[i-1]) {
              u=w;
              w=x[i-1]=x[i];
            } else
              u=x[i];
            w=x[i]=u-m[i]*w;
          }
        } else
          break;
      } else
        break;
    }
    for (i=1; i<=n; i++) vec[i][k]=x[i]*res;
    if (count > maxcount) maxcount=count;
    if (res > maxres) maxres=res;
    oldlambda=lambda;
  }
  em[5]=orth;
  em[7]=maxres;
  em[9]=maxcount;
}


public static int qrivalsymtri(double d[], double bb[],
                               int n, double em[])
{
  int i,i1,low,oldlow,n1,count,max;
  double bbtol,bbmax,bbi,bbn1,machtol,dn,delta,f,num,shift,g,h,
         t,p,r,s,c,oldg;

  t=em[2]*em[1];
  bbtol=t*t;
  machtol=em[0]*em[1];
  max=(int) em[4];
  bbmax=0.0;
  count=0;
  oldlow=n;
  n1=n-1;
  while (n > 0) {
    i=n;
    do {
      low=i;
      i--;
    } while ((i >= 1) ? bb[i] > bbtol : false);
    if (low > 1)
      if (bb[low-1] > bbmax) bbmax=bb[low-1];
    if (low == n)
      n=n1;
    else {
      dn=d[n];
      delta=d[n1]-dn;
      bbn1=bb[n1];
      if (Math.abs(delta) < machtol)
        r=Math.sqrt(bbn1);
      else {
        f=2.0/delta;
        num=bbn1*f;
        r = -num/(Math.sqrt(num*f+1.0)+1.0);
      }
      if (low == n1) {
        d[n]=dn+r;
        d[n1] -= r;
        n -= 2;
      } else {
        count++;
        if (count > max) break;
        if (low < oldlow) {
          shift=0.0;
          oldlow=low;
        } else
          shift=dn+r;
        h=d[low]-shift;
        if (Math.abs(h) < machtol)
          h = (h <= 0.0) ? -machtol : machtol;
        g=h;
        t=g*h;
        bbi=bb[low];
        p=t+bbi;
        i1=low;
        for (i=low+1; i<=n; i++) {
          s=bbi/p;
          c=t/p;
          h=d[i]-shift-bbi/h;
          if (Math.abs(h) < machtol)
            h = (h <= 0.0) ? -machtol : machtol;
          oldg=g;
          g=h*c;
          t=g*h;
          d[i1]=oldg-g+d[i];
          bbi = (i == n) ? 0.0 : bb[i];
          p=t+bbi;
          bb[i1]=s*p;
          i1=i;
        }
        d[n]=g+shift;
      }
    }
    n1=n-1;
  }
  em[3]=Math.sqrt(bbmax);
  em[5]=count;
  return n;
}


public static int qrisymtri(double a[][], int n, double d[],
                            double b[], double bb[], double em[])
{
  int j,j1,k,m,m1,count,max;
  double bbmax,r,s,sin,t,cos,oldcos,g,p,w,tol,tol2,lambda,dk1;

  g=0.0;
  tol=em[2]*em[1];
  tol2=tol*tol;
  count=0;
  bbmax=0.0;
  max=(int) em[4];
  m=n;
  do {
    k=m;
    m1=m-1;
    while (true) {
      k--;
      if (k <= 0) break;
      if (bb[k] < tol2) {
        if (bb[k] > bbmax) bbmax=bb[k];
        break;
      }
    }
    if (k == m1)
      m=m1;
    else {
      t=d[m]-d[m1];
      r=bb[m1];
      if (Math.abs(t) < tol)
        s=Math.sqrt(r);
      else {
        w=2.0/t;
        s=w*r/(Math.sqrt(w*w*r+1.0)+1.0);
      }
      if (k == m-2) {
        d[m] += s;
        d[m1] -= s;
        t = -s/b[m1];
        r=Math.sqrt(t*t+1.0);
        cos=1.0/r;
        sin=t/r;
        Basic.rotcol(1,n,m1,m,a,cos,sin);
        m -= 2;
      } else {
        count++;
        if (count > max) break;
        lambda=d[m]+s;
        if (Math.abs(t) < tol) {
          w=d[m1]-s;
          if (Math.abs(w) < Math.abs(lambda)) lambda=w;
        }
        k++;
        t=d[k]-lambda;
        cos=1.0;
        w=b[k];
        p=Math.sqrt(t*t+w*w);
        j1=k;
        for (j=k+1; j<=m; j++) {
          oldcos=cos;
          cos=t/p;
          sin=w/p;
          dk1=d[j]-lambda;
          t *= oldcos;
          d[j1]=(t+dk1)*sin*sin+lambda+t;
          t=cos*dk1-sin*w*oldcos;
          w=b[j];
          p=Math.sqrt(t*t+w*w);
          g=b[j1]=sin*p;
          bb[j1]=g*g;
          Basic.rotcol(1,n,j1,j,a,cos,sin);
          j1=j;
        }
        d[m]=cos*t+lambda;
        if (t < 0.0) b[m1] = -g;
      }
    }
  } while (m > 0);
  em[3]=Math.sqrt(bbmax);
  em[5]=count;
  return m;
}


public static void eigvalsym2(double a[][], int n, int numval,
                              double val[], double em[])
{
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  double d[] = new double[n+1];
  
  tfmsymtri2(a,n,d,b,bb,em);
  valsymtri(d,bb,n,1,numval,val,em);
}


public static void eigsym2(double a[][], int n, int numval, double val[],
                           double vec[][], double em[])
{
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  double d[] = new double[n+1];

  tfmsymtri2(a,n,d,b,bb,em);
  valsymtri(d,bb,n,1,numval,val,em);
  vecsymtri(d,b,n,1,numval,val,vec,em);
  baksymtri2(a,n,1,numval,vec);
}


public static void eigvalsym1(double a[], int n, int numval,
                              double val[], double em[])
{
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  double d[] = new double[n+1];

  tfmsymtri1(a,n,d,b,bb,em);
  valsymtri(d,bb,n,1,numval,val,em);
}


public static void eigsym1(double a[], int n, int numval, double val[],
                           double vec[][], double em[])
{
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  double d[] = new double[n+1];

  tfmsymtri1(a,n,d,b,bb,em);
  valsymtri(d,bb,n,1,numval,val,em);
  vecsymtri(d,b,n,1,numval,val,vec,em);
  baksymtri1(a,n,1,numval,vec);
}


public static int qrivalsym2(double a[][], int n, double val[],
                             double em[])
{
  int i;
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  
  tfmsymtri2(a,n,val,b,bb,em);
  i=qrivalsymtri(val,bb,n,em);
  return i;
}


public static int qrisym(double a[][], int n, double val[], double em[])
{
  int i;
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  
  tfmsymtri2(a,n,val,b,bb,em);
  tfmprevec(a,n);
  i=qrisymtri(a,n,val,b,bb,em);
  return i;
}


public static int qrivalsym1(double a[], int n, double val[], double em[])
{
  int i;
  double b[] = new double[n+1];
  double bb[] = new double[n+1];

  tfmsymtri1(a,n,val,b,bb,em);
  i=qrivalsymtri(val,bb,n,em);
  return i;
}


public static void mergesort(double a[], int p[], int low, int up)
{
  int i,lo,step,stap,umlp1,umsp1,rest,restv;
  int hp[] = new int[up+1];
  
  for (i=low; i<=up; i++) p[i]=i;
  restv=0;
  umlp1=up-low+1;
  step=1;
  do {
    stap=2*step;
    umsp1=up-stap+1;
    for (lo=low; lo<=umsp1; lo += stap)
      merge(lo,step,step,p,a,hp);
    rest=up-lo+1;
    if (rest > restv && restv > 0)
      merge(lo,rest-restv,restv,p,a,hp);
    restv=rest;
    step *= 2;
  } while (step < umlp1);
}


static private void merge(int lo, int ls, int rs, int p[],
                          double a[], int hp[])
{
  /* this procedure is used internally by MERGESORT */

  int l,r,i,pl,pr;
  boolean lout,rout;

  l=lo;
  r=lo+ls;
  lout=rout=false;
  i=lo;
  do {
    pl=p[l];
    pr=p[r];
    if (a[pl] > a[pr]) {
      hp[i]=pr;
      r++;
      rout = (r == lo+ls+rs);
    } else {
      hp[i]=pl;
      l++;
      lout = (l == lo+ls);
    }
    i++;
  } while (!(lout || rout));
  if (rout) {
    for (i=lo+ls-1; i>=l; i--) p[i+rs]=p[i];
    r=l+rs;
  }
  for (i=r-1; i>=lo; i--) p[i]=hp[i];
}


public static void vecperm(int perm[], int low, int upp, double vector[])
{
  int t,j,k;
  double a;
  boolean todo[] = new boolean[upp+1];
  
  for (t=low; t<=upp; t++) todo[t]=true;
  for (t=low; t<=upp; t++)
    if (todo[t]) {
      k=t;
      a=vector[k];
      j=perm[k];
      while (j != t) {
        vector[k]=vector[j];
        todo[k]=false;
        k=j;
        j=perm[k];
      }
      vector[k]=a;
      todo[k]=false;
    }
}


public static void rowperm(int perm[], int low, int upp,
                           int i, double mat[][])
{
  int t,j,k;
  double a;
  boolean todo[] = new boolean[upp+1];
  
  for (t=low; t<=upp; t++) todo[t]=true;
  for (t=low; t<=upp; t++)
    if (todo[t]) {
      k=t;
      a=mat[i][k];
      j=perm[k];
      while (j != t) {
        mat[i][k]=mat[i][j];
        todo[k]=false;
        k=j;
        j=perm[k];
      }
      mat[i][k]=a;
      todo[k]=false;
    }
}


public static void orthog(int n, int lc, int uc, double x[][])
{
  int i,j,k;
  double normx;

  for (j=lc; j<=uc; j++) {
    normx=Math.sqrt(Basic.tammat(1,n,j,j,x,x));
    for (i=1; i<=n; i++) x[i][j] /=normx;
    for (k=j+1; k<=uc; k++) 
      Basic.elmcol(1,n,k,j,x,x,-Basic.tammat(1,n,k,j,x,x));
  }
}


public static void symeigimp(int n, double a[][], double vec[][],
       double val[], double lbound[], double ubound[], double aux[])
{
  boolean stop;
  int k,i,j,i0,i1,i01,iter,maxitp1,n1,i0m1,i1p1;
  double s,max,tol,mateps,relerra,reltolr,norma,eps2,dl,dr,m1,dtemp;
  int itmp[] = new int[1];
  int perm[] = new int[n+1];
  double em[] = new double[6];
  double rq[] = new double[n+1];
  double eps[] = new double[n+1];
  double z[] = new double[n+1];
  double val3[] = new double[n+1];
  double eta[] = new double[n+1];
  double r[][] = new double[n+1][n+1];
  double p[][] = new double[n+1][n+1];
  double y[][] = new double[n+1][n+1];

  max=0.0;
  norma=Basic.infnrmmat(1,n,1,n,itmp,a);
  i=itmp[0];
  relerra=aux[0];
  reltolr=aux[2];
  maxitp1=(int) (aux[4]+1.0);
  mateps=relerra*norma;
  tol=reltolr*norma;
  for (iter=1; iter<=maxitp1; iter++) {
    if (iter == 1)
      stop=false;
    else
      stop=true;
    max=0.0;
    for (j=1; j<=n; j++)
      for (i=1; i<=n; i++) {
        dtemp = -(double)(vec[i][j])*(double)(val[j]);
        for (k=1; k<=n; k++)
          dtemp += (double)(a[i][k])*(double)(vec[k][j]);
        r[i][j]=dtemp;
        if (Math.abs(r[i][j]) > max) max=Math.abs(r[i][j]);
      }
    if (max > tol) stop=false;
    if ((!stop) && (iter < maxitp1)) {
      for (i=1; i<=n; i++) {
        dtemp=(double)(val[i]);
        for (k=1; k<=n; k++)
          dtemp += (double)(vec[k][i])*(double)(r[k][i]);
        rq[i]=dtemp;
      }
      for (j=1; j<=n; j++) {
        for (i=1; i<=n; i++)
          eta[i]=r[i][j]-(rq[j]-val[j])*vec[i][j];
        z[j]=Math.sqrt(Basic.vecvec(1,n,0,eta,eta));
      }
      mergesort(rq,perm,1,n);
      vecperm(perm,1,n,rq);
      for (i=1; i<=n; i++) {
        eps[i]=z[perm[i]];
        val3[i]=val[perm[i]];
        rowperm(perm,1,n,i,vec);
        rowperm(perm,1,n,i,r);
      }
      for (i=1; i<=n; i++)
        for (j=i; j<=n; j++)
          p[i][j]=p[j][i]=Basic.tammat(1,n,i,j,vec,r);
    }
    i0=1;
    do {
      j=i1=i0;
      j++;
      while ((j > n) ? false :
            (rq[j]-rq[j-1] <= Math.sqrt((eps[j]+eps[j-1])*norma))) {
        i1=j;
        j++;
      }
      if (stop || (iter == maxitp1)) {
        i=i0;
        do {
          j=i01=i;
          j++;
          while ((j>i1) ? false : rq[j]-rq[j-1] <= eps[j]+eps[j-1]) {
            i01=j;
            j++;
          }
          if (i == i01) {
            if (i < n) {
              if (i == 1)
                dl=dr=rq[i+1]-rq[i]-eps[i+1];
              else {
                dl=rq[i]-rq[i-1]-eps[i-1];
                dr=rq[i+1]-rq[i]-eps[i+1];
              }
            } else
              dl=dr=rq[i]-rq[i-1]-eps[i-1];
            eps2=eps[i]*eps[i];
            lbound[i]=eps2/dr+mateps;
            ubound[i]=eps2/dl+mateps;
          } else
            for (k=i; k<=i01; k++)
              lbound[k]=ubound[k]=eps[k]+mateps;
          i01++;
          i=i01;
        } while (i <= i1);  /* bounds */
      } else {
        if (i0 == i1) {
          for (k=1; k<=n; k++)
            if (k == i0)
              y[k][i0]=1.0;
            else
              r[k][i0]=p[k][i0];
          val[i0]=rq[i0];
        } else {
          n1=i1-i0+1;
          em[0]=em[2]=Double.MIN_VALUE;
          em[4]=10*n1;
          double val4[] = new double[n1+1];
          double pp[][] = new double[n1+1][n1+1];
          m1=0.0;
          for (k=i0; k<=i1; k++) m1 += val3[k];
          m1 /= n1;
          for (i=1; i<=n1; i++)
            for (j=1; j<=n1; j++) {
              pp[i][j]=p[i+i0-1][j+i0-1];
              if (i == j) pp[i][j] += val3[j+i0-1]-m1;
            }
          for (i=i0; i<=i1; i++) {
            val3[i]=m1;
            val[i]=rq[i];
          }
          qrisym(pp,n1,val4,em);
          mergesort(val4,perm,1,n1);
          for (i=1; i<=n1; i++)
            for (j=1; j<=n1; j++)
              p[i+i0-1][j+i0-1]=pp[i][perm[j]];
          i0m1=i0-1;
          i1p1=i1+1;
          for (j=i0; j<=i1; j++) {
            for (i=1; i<=i0m1; i++) {
              s=0.0;
              for (k=i0; k<=i1; k++) s += p[i][k]*p[k][j];
              r[i][j]=s;
            }
            for (i=i1p1; i<=n; i++) {
              s=0.0;
              for (k=i0; k<=i1; k++) s += p[i][k]*p[k][j];
              r[i][j]=s;
            }
            for (i=i0; i<=i1; i++) y[i][j]=p[i][j];
          }
        } /* innerblock */
      } /* not stop */
      i0=i1+1;
    } while (i0 <= n);  /* while i0 loop */
    if ((!stop) && (iter < maxitp1)) {
      for (j=1; j<=n; j++)
        for (i=1; i<=n; i++)
          if (val3[i] != val3[j])
            y[i][j]=r[i][j]/(val3[j]-val3[i]);
      for (i=1; i<=n; i++) {
        for (j=1; j<=n; j++) z[j]=Basic.matmat(1,n,i,j,vec,y);
        for (j=1; j<=n; j++) vec[i][j]=z[j];
      }
      orthog(n,1,n,vec);
    } else {
      aux[5]=iter-1;
      break;
    }
  } /* for iter loop */
  aux[1]=norma;
  aux[3]=max;
}


public static int reavalqri(double a[][], int n,
                            double em[], double val[])
{
  int n1,i,i1,q,max,count;
  double det,w,shift,kappa,nu,mu,r,tol,delta,machtol,s;

  nu=mu=0.0;
  machtol=em[0]*em[1];
  tol=em[1]*em[2];
  max=(int) em[4];
  count=0;
  r=0.0;
  do {
    n1=n-1;
    i=n;
    do{
      q=i;
      i--;
    } while ((i >= 1) ? (Math.abs(a[i+1][i]) > tol) : false);
    if (q > 1)
      if (Math.abs(a[q][q-1]) > r) r=Math.abs(a[q][q-1]);
    if (q == n) {
      val[n]=a[n][n];
      n=n1;
    } else {
      delta=a[n][n]-a[n1][n1];
      det=a[n][n1]*a[n1][n];
      if (Math.abs(delta) < machtol)
        s=Math.sqrt(det);
      else {
        w=2.0/delta;
        s=w*w*det+1.0;
        s = (s <= 0.0) ? -delta*0.5 : w*det/(Math.sqrt(s)+1.0);
      }
      if (q == n1) {
        val[n]=a[n][n]+s;
        val[n1]=a[n1][n1]-s;
        n -= 2;
      } else {
        count++;
        if (count > max) break;
        shift=a[n][n]+s;
        if (Math.abs(delta) < tol) {
          w=a[n1][n1]-s;
          if (Math.abs(w) < Math.abs(shift)) shift=w;
        }
        a[q][q] -= shift;
        for (i=q; i<=n-1; i++) {
          i1=i+1;
          a[i1][i1] -= shift;
          kappa=Math.sqrt(a[i][i]*a[i][i]+a[i1][i]*a[i1][i]);
          if (i > q) {
            a[i][i-1]=kappa*nu;
            w=kappa*mu;
          } else
            w=kappa;
          mu=a[i][i]/kappa;
          nu=a[i1][i]/kappa;
          a[i][i]=w;
          Basic.rotrow(i1,n,i,i1,a,mu,nu);
          Basic.rotcol(q,i,i,i1,a,mu,nu);
          a[i][i] += shift;
        }
        a[n][n-1]=a[n][n]*nu;
        a[n][n]=a[n][n]*mu+shift;
      }
    }
  } while (n > 0);
  em[3]=r;
  em[5]=count;
  return n;
}


public static void reaveches(double a[][], int n, double lambda,
                             double em[], double v[])
{
  int i,i1,j,count,max;
  double m,r,norm,machtol,tol;
  boolean p[] = new boolean[n+1];
  
  r=0.0;
  norm=em[1];
  machtol=em[0]*norm;
  tol=em[6]*norm;
  max=(int) em[8];
  a[1][1] -= lambda;
  for (i=1; i<=n-1; i++) {
    i1=i+1;
    r=a[i][i];
    m=a[i1][i];
    if (Math.abs(m) < machtol) m=machtol;
    p[i] = (Math.abs(m) <= Math.abs(r));
    if (p[i]) {
      a[i1][i] = m /= r;
      for (j=i1; j<=n; j++)
        a[i1][j]=((j > i1) ? a[i1][j] : a[i1][j]-lambda)-m*a[i][j];
    } else {
      a[i][i]=m;
      a[i1][i] = m = r/m;
      for (j=i1; j<=n; j++) {
        r = (j > i1) ? a[i1][j] : a[i1][j]-lambda;
        a[i1][j]=a[i][j]-m*r;
        a[i][j]=r;
      }
    }
  }
  if (Math.abs(a[n][n]) < machtol) a[n][n]=machtol;
  for (j=1; j<=n; j++) v[j]=1.0;
  count=0;
  do {
    count++;
    if (count > max) break;
    for (i=1; i<=n-1; i++) {
      i1=i+1;
      if (p[i])
        v[i1] -= a[i1][i]*v[i];
      else {
        r=v[i1];
        v[i1]=v[i]-a[i1][i]*r;
        v[i]=r;
      }
    }
    for (i=n; i>=1; i--)
      v[i]=(v[i]-Basic.matvec(i+1,n,i,a,v))/a[i][i];
    r=1.0/Math.sqrt(Basic.vecvec(1,n,0,v,v));
    for (j=1; j<=n; j++) v[j] *= r;
  } while (r > tol);
  em[7]=r;
  em[9]=count;
}


public static int reaqri(double a[][], int n, double em[],
                         double val[], double vec[][])
{
  int m1,i,i1,m,j,q,max,count;
  double w,shift,kappa,nu,mu,r,tol,s,machtol,elmax,t,delta,det;
  double tf[] = new double[n+1];
  
  nu=mu=0.0;
  machtol=em[0]*em[1];
  tol=em[1]*em[2];
  max=(int) em[4];
  count=0;
  elmax=0.0;
  m=n;
  for (i=1; i<=n; i++) {
    vec[i][i]=1.0;
    for (j=i+1; j<=n; j++) vec[i][j]=vec[j][i]=0.0;
  }
  do {
    m1=m-1;
    i=m;
    do {
      q=i;
      i--;
    } while ((i >= 1) ? (Math.abs(a[i+1][i]) > tol) : false);
    if (q > 1)
      if (Math.abs(a[q][q-1]) > elmax) elmax=Math.abs(a[q][q-1]);
    if (q == m) {
      val[m]=a[m][m];
      m=m1;
    } else {
      delta=a[m][m]-a[m1][m1];
      det=a[m][m1]*a[m1][m];
      if (Math.abs(delta) < machtol)
        s=Math.sqrt(det);
      else {
        w=2.0/delta;
        s=w*w*det+1.0;
        s = (s <= 0.0) ? -delta*0.5 : w*det/(Math.sqrt(s)+1.0);
      }
      if (q == m1) {
        val[m] = a[m][m] += s;
        val[q] = a[q][q] -= s;
        t = (Math.abs(s) < machtol) ? (s+delta)/a[m][q] : a[q][m]/s;
        r=Math.sqrt(t*t+1.0);
        nu=1.0/r;
        mu = -t*nu;
        a[q][m] -= a[m][q];
        Basic.rotrow(q+2,n,q,m,a,mu,nu);
        Basic.rotcol(1,q-1,q,m,a,mu,nu);
        Basic.rotcol(1,n,q,m,vec,mu,nu);
        m -= 2;
      } else {
        count++;
        if (count > max) {
          em[3]=elmax;
          em[5]=count;
          return m;
        }
        shift=a[m][m]+s;
        if (Math.abs(delta) < tol) {
          w=a[m1][m1]-s;
          if (Math.abs(w) < Math.abs(shift)) shift=w;
        }
        a[q][q] -= shift;
        for (i=q; i<=m1; i++) {
          i1=i+1;
          a[i1][i1] -= shift;
          kappa=Math.sqrt(a[i][i]*a[i][i]+a[i1][i]*a[i1][i]);
          if (i > q) {
            a[i][i-1]=kappa*nu;
            w=kappa*mu;
          } else
            w=kappa;
          mu=a[i][i]/kappa;
          nu=a[i1][i]/kappa;
          a[i][i]=w;
          Basic.rotrow(i1,n,i,i1,a,mu,nu);
          Basic.rotcol(1,i,i,i1,a,mu,nu);
          a[i][i] += shift;
          Basic.rotcol(1,n,i,i1,vec,mu,nu);
        }
        a[m][m1]=a[m][m]*nu;
        a[m][m]=a[m][m]*mu+shift;
      }
    }
  } while (m > 0);
  for (j=n; j>=2; j--) {
    tf[j]=1.0;
    t=a[j][j];
    for (i=j-1; i>=1; i--) {
      delta=t-a[i][i];
      tf[i]=Basic.matvec(i+1,j,i,a,tf)/
            ((Math.abs(delta) < machtol) ? machtol : delta);
    }
    for (i=1; i<=n; i++) vec[i][j]=Basic.matvec(1,j,i,vec,tf);
  }
  em[3]=elmax;
  em[5]=count;
  return m;
}


public static int comvalqri(double a[][], int n, double em[],
                            double re[], double im[])
{
  boolean b;
  int i,j,p,q,max,count,n1,p1,p2,imin1,i1,i2,i3;
  double disc,sigma,rho,g1,g2,g3,psi1,psi2,aa,e,k,s,norm,machtol2,tol,w;

  norm=em[1];
  w=em[0]*norm;
  machtol2=w*w;
  tol=em[2]*norm;
  max=(int) em[4];
  count=0;
  w=0.0;
  do {
    i=n;
    do {
      q=i;
      i--;
    } while ((i >= 1) ? (Math.abs(a[i+1][i]) > tol) : false);
    if (q > 1)
      if (Math.abs(a[q][q-1]) > w) w=Math.abs(a[q][q-1]);
    if (q >= n-1) {
      n1=n-1;
      if (q == n) {
        re[n]=a[n][n];
        im[n]=0.0;
        n=n1;
      } else {
        sigma=a[n][n]-a[n1][n1];
        rho = -a[n][n1]*a[n1][n];
        disc=sigma*sigma-4.0*rho;
        if (disc > 0.0) {
          disc=Math.sqrt(disc);
          s = -2.0*rho/(sigma+((sigma >= 0.0) ? disc : -disc));
          re[n]=a[n][n]+s;
          re[n1]=a[n1][n1]-s;
          im[n]=im[n1]=0.0;
        } else {
          re[n]=re[n1]=(a[n1][n1]+a[n][n])/2.0;
          im[n1]=Math.sqrt(-disc)/2.0;
          im[n] = -im[n1];
        }
        n -= 2;
      }
    } else {
      count++;
      if (count > max) break;
      n1=n-1;
      sigma=a[n][n]+a[n1][n1]+
            Math.sqrt(Math.abs(a[n1][n-2]*a[n][n1])*em[0]);
      rho=a[n][n]*a[n1][n1]-a[n][n1]*a[n1][n];
      i=n-1;
      do {
        p1=i1=i;
        i--;
      } while ((i-1 >= q) ? (Math.abs(a[i][i-1]*a[i1][i]*
          (Math.abs(a[i][i]+a[i1][i1]-sigma)+Math.abs(a[i+2][i1]))) >
          Math.abs(a[i][i]*((a[i][i]-sigma)+
            a[i][i1]*a[i1][i]+rho))*tol) : false);
      p=p1-1;
      p2=p+2;
      for (i=p; i<=n-1; i++) {
        imin1=i-1;
        i1=i+1;
        i2=i+2;
        if (i == p) {
          g1=a[p][p]*(a[p][p]-sigma)+a[p][p1]*a[p1][p]+rho;
          g2=a[p1][p]*(a[p][p]+a[p1][p1]-sigma);
          if (p1 <= n1) {
            g3=a[p1][p]*a[p2][p1];
            a[p2][p]=0.0;
          } else
            g3=0.0;
        } else {
          g1=a[i][imin1];
          g2=a[i1][imin1];
          g3 = (i2 <= n) ? a[i2][imin1] : 0.0;
        }
        k = (g1 >= 0.0) ? Math.sqrt(g1*g1+g2*g2+g3*g3) :
                    -Math.sqrt(g1*g1+g2*g2+g3*g3);
        b = (Math.abs(k) > machtol2);
        aa = (b ? g1/k+1.0 : 2.0);
        psi1 = (b ? g2/(g1+k) : 0.0);
        psi2 = (b ? g3/(g1+k) : 0.0);
        if (i != q)
          a[i][imin1] = (i == p) ? -a[i][imin1] : -k;
        for (j=i; j<=n; j++) {
          e=aa*(a[i][j]+psi1*a[i1][j]+
              ((i2 <= n) ? psi2*a[i2][j] : 0.0));
          a[i][j] -= e;
          a[i1][j] -= psi1*e;
          if (i2 <= n) a[i2][j] -= psi2*e;
        }
        for (j=q; j<=((i2 <= n) ? i2 : n); j++) {
          e=aa*(a[j][i]+psi1*a[j][i1]+
              ((i2 <= n) ? psi2*a[j][i2] : 0.0));
          a[j][i] -= e;
          a[j][i1] -= psi1*e;
          if (i2 <= n) a[j][i2] -= psi2*e;
        }
        if (i2 <= n1) {
          i3=i+3;
          e=aa*psi2*a[i3][i2];
          a[i3][i] = -e;
          a[i3][i1] = -psi1*e;
          a[i3][i2] -= psi2*e;
        }
      }
    }
  } while (n > 0);
  em[3]=w;
  em[5]=count;
  return n;
}


public static void comveches(double a[][], int n, double lambda,
              double mu,double em[], double u[], double v[])
{
  int i,i1,j,count,max;
  double aa,bb,d,m,r,s,w,x,y,norm,machtol,tol;
  boolean p[] = new boolean[n+1];
  double g[] = new double[n+1];
  double f[] = new double[n+1];
  
  w=0.0;
  norm=em[1];
  machtol=em[0]*norm;
  tol=em[6]*norm;
  max=(int) em[8];
  for (i=2; i<=n; i++) {
    f[i-1]=a[i][i-1];
    a[i][1]=0.0;
  }
  aa=a[1][1]-lambda;
  bb = -mu;
  for (i=1; i<=n-1; i++) {
    i1=i+1;
    m=f[i];
    if (Math.abs(m) < machtol) m=machtol;
    a[i][i]=m;
    d=aa*aa+bb*bb;
    p[i] = (Math.abs(m) < Math.sqrt(d));
    if (p[i]) {
      f[i]=r=m*aa/d;
      g[i] = s = -m*bb/d;
      w=a[i1][i];
      x=a[i][i1];
      a[i1][i]=y=x*s+w*r;
      a[i][i1]=x=x*r-w*s;
      aa=a[i1][i1]-lambda-x;
      bb = -(mu+y);
      for (j=i+2; j<=n; j++) {
        w=a[j][i];
        x=a[i][j];
        a[j][i]=y=x*s+w*r;
        a[i][j]=x=x*r-w*s;
        a[j][i1] = -y;
        a[i1][j] -= x;
      }
    } else {
      f[i]=r=aa/m;
      g[i]=s=bb/m;
      w=a[i1][i1]-lambda;
      aa=a[i][i1]-r*w-s*mu;
      a[i][i1]=w;
      bb=a[i1][i]-s*w+r*mu;
      a[i1][i] = -mu;
      for (j=i+2; j<=n; j++) {
        w=a[i1][j];
        a[i1][j]=a[i][j]-r*w;
        a[i][j]=w;
        a[j][i1]=a[j][i]-s*w;
        a[j][i]=0.0;
      }
    }
  }
  p[n]=true;
  d=aa*aa+bb*bb;
  if (d < machtol*machtol) {
    aa=machtol;
    bb=0.0;
    d=machtol*machtol;
  }
  a[n][n]=d;
  f[n]=aa;
  g[n] = -bb;
  for (i=1; i<=n; i++) {
    u[i]=1.0;
    v[i]=0.0;
  }
  count=0;
  do {
    if (count > max) break;
    for (i=1; i<=n; i++)
      if (p[i]) {
        w=v[i];
        v[i]=g[i]*u[i]+f[i]*w;
        u[i]=f[i]*u[i]-g[i]*w;
        if (i < n) {
          v[i+1] -= v[i];
          u[i+1] -= u[i];
        }
      } else {
        aa=u[i+1];
        bb=v[i+1];
        u[i+1]=u[i]-(f[i]*aa-g[i]*bb);
        u[i]=aa;
        v[i+1]=v[i]-(g[i]*aa+f[i]*bb);
        v[i]=bb;
      }
    for (i=n; i>=1; i--) {
      i1=i+1;
      u[i]=(u[i]-Basic.matvec(i1,n,i,a,u)+
          (p[i] ? Basic.tamvec(i1,n,i,a,v) : a[i1][i]*v[i1]))/a[i][i];
      v[i]=(v[i]-Basic.matvec(i1,n,i,a,v)-
          (p[i] ? Basic.tamvec(i1,n,i,a,u) : a[i1][i]*u[i1]))/a[i][i];
    }
    w=1.0/Math.sqrt(Basic.vecvec(1,n,0,u,u)+Basic.vecvec(1,n,0,v,v));
    for (j=1; j<=n; j++) {
      u[j] *= w;
      v[j] *= w;
    }
    count++;
  } while (w > tol);
  em[7]=w;
  em[9]=count;
}


public static int reaeigval(double a[][], int n, double em[], double val[])
{
  int i,j,k;
  double r;
  int ind[] = new int[n+1];
  int ind0[] = new int[n+1];
  double d[] = new double[n+1];
  
  eqilbr(a,n,em,d,ind0);
  tfmreahes(a,n,em,ind);
  k=reavalqri(a,n,em,val);
  for (i=k+1; i<=n; i++)
    for (j=i+1; j<=n; j++)
      if (val[j] > val[i]) {
        r=val[i];
        val[i]=val[j];
        val[j]=r;
      }
  return k;
}


public static int reaeig1(double a[][], int n, double em[],
                          double val[], double vec[][])
{
  int i,k,max,j,l;
  double residu,r,machtol;
  int ind[] = new int[n+1];
  int ind0[] = new int[n+1];
  double d[] = new double[n+1];
  double v[] = new double[n+1];
  double b[][] = new double[n+1][n+1];

  residu=0.0;
  max=0;
  eqilbr(a,n,em,d,ind0);
  tfmreahes(a,n,em,ind);
  for (i=1; i<=n; i++)
    for (j=((i == 1) ? 1 : i-1); j<=n; j++) b[i][j]=a[i][j];
  k=reavalqri(b,n,em,val);
  for (i=k+1; i<=n; i++)
    for (j=i+1; j<=n; j++)
      if (val[j] > val[i]) {
        r=val[i];
        val[i]=val[j];
        val[j]=r;
      }
  machtol=em[0]*em[1];
  for (l=k+1; l<=n; l++) {
    if (l > 1)
      if (val[l-1]-val[l] < machtol) val[l]=val[l-1]-machtol;
    for (i=1; i<=n; i++)
      for (j=((i == 1) ? 1 : i-1); j<=n; j++) b[i][j]=a[i][j];
    reaveches(b,n,val[l],em,v);
    if (em[7] > residu) residu=em[7];
    if (em[9] > max) max=(int) em[9];
    for (j=1; j<=n; j++) vec[j][l]=v[j];
  }
  em[7]=residu;
  em[9]=max;
  bakreahes2(a,n,k+1,n,ind,vec);
  baklbr(n,k+1,n,d,ind0,vec);
  Basic.reascl(vec,n,k+1,n);
  return k;
}


public static int reaeig3(double a[][], int n, double em[],
                          double val[], double vec[][])
{
  int i;
  int ind[] = new int[n+1];
  int ind0[] = new int[n+1];
  double d[] = new double[n+1];

  eqilbr(a,n,em,d,ind0);
  tfmreahes(a,n,em,ind);
  i=reaqri(a,n,em,val,vec);
  if (i == 0) {
    bakreahes2(a,n,1,n,ind,vec);
    baklbr(n,1,n,d,ind0,vec);
    Basic.reascl(vec,n,1,n);
  }
  return i;
}


public static int comeigval(double a[][], int n, double em[],
                            double re[], double im[])
{
  int i;
  int ind[] = new int[n+1];
  int ind0[] = new int[n+1];
  double d[] = new double[n+1];

  eqilbr(a,n,em,d,ind0);
  tfmreahes(a,n,em,ind);
  i=comvalqri(a,n,em,re,im);
  return i;
}


public static int comeig1(double a[][], int n, double em[],
                          double re[], double im[], double vec[][])
{
  boolean again;
  int i,j,k,ii,pj,itt;
  double x,y,max,neps,temp1,temp2;
  int ind[] = new int[n+1];
  int ind0[] = new int[n+1];
  double d[] = new double[n+1];
  double u[] = new double[n+1];
  double v[] = new double[n+1];
  double ab[][] = new double[n+1][n+1];

  eqilbr(a,n,em,d,ind0);
  tfmreahes(a,n,em,ind);
  for (i=1; i<=n; i++)
    for (j=((i == 1) ? 1 : i-1); j<=n; j++) ab[i][j]=a[i][j];
  k=comvalqri(ab,n,em,re,im);
  neps=em[0]*em[1];
  max=0.0;
  itt=0;
  for (i=k+1; i<=n; i++) {
    x=re[i];
    y=im[i];
    pj=0;
    again=true;
    do {
      for (j=k+1; j<=i-1; j++) {
        temp1=x-re[j];
        temp2=y-im[j];
        if (temp1*temp1+temp2*temp2 <= neps*neps) {
          if (pj == j)
            neps=em[2]*em[1];
          else
            pj=j;
          x += 2.0*neps;
          again = (!again);
          break;
        }
      }
      again = (!again);
    } while (again);
    re[i]=x;
    for (ii=1; ii<=n; ii++)
      for (j=((ii == 1) ? 1 : ii-1); j<=n; j++) ab[ii][j]=a[ii][j];
    if (y != 0.0) {
      comveches(ab,n,re[i],im[i],em,u,v);
      for (j=1; j<=n; j++) vec[j][i]=u[j];
      i++;
      re[i]=x;
    } else
      reaveches(ab,n,x,em,v);
    for (j=1; j<=n; j++) vec[j][i]=v[j];
    if (em[7] > max) max=em[7];
    if (itt < em[9]) itt=(int) em[9];
  }
  em[7]=max;
  em[9]=itt;
  bakreahes2(a,n,k+1,n,ind,vec);
  baklbr(n,k+1,n,d,ind0,vec);
  Basic.comscl(vec,n,k+1,n,im);
  return k;
}


public static void eigvalhrm(double a[][], int n, int numval,
                             double val[], double em[])
{
  double d[] = new double[n+1];
  double bb[] = new double[n];

  hshhrmtrival(a,n,d,bb,em);
  valsymtri(d,bb,n,1,numval,val,em);
}


public static void eighrm(double a[][], int n, int numval, double val[],
                          double vecr[][], double veci[][], double em[])
{
  double bb[] = new double[n];
  double tr[] = new double[n];
  double ti[] = new double[n];
  double d[] = new double[n+1];
  double b[] = new double[n+1];
  
  hshhrmtri(a,n,d,b,bb,em,tr,ti);
  valsymtri(d,bb,n,1,numval,val,em);
  b[n]=0.0;
  vecsymtri(d,b,n,1,numval,val,vecr,em);
  bakhrmtri(a,n,1,numval,vecr,veci,tr,ti);
}


public static int qrivalhrm(double a[][], int n, double val[], double em[])
{
  int i;
  double bb[] = new double[n+1];

  hshhrmtrival(a,n,val,bb,em);
  bb[n]=0.0;
  i=qrivalsymtri(val,bb,n,em);
  return i;
}


public static int qrihrm(double a[][], int n, double val[],
                         double vr[][], double vi[][], double em[])
{
  int i,j;
  double b[] = new double[n+1];
  double bb[] = new double[n+1];
  double tr[] = new double[n];
  double ti[] = new double[n];

  hshhrmtri(a,n,val,b,bb,em,tr,ti);
  for (i=1; i<=n; i++) {
    vr[i][i]=1.0;
    for (j=i+1; j<=n; j++) vr[i][j]=vr[j][i]=0.0;
  }
  b[n]=bb[n]=0.0;
  i=qrisymtri(vr,n,val,b,bb,em);
  bakhrmtri(a,n,i+1,n,vr,vi,tr,ti);
  return i;
}


public static int valqricom(double a1[][], double a2[][], double b[],
                  int n, double em[], double val1[], double val2[])
{
  int nm1,i,i1,q,q1,max,count;
  double r,z1,z2,dd1,dd2,cc,hc,a1nn,a2nn,aij1,aij2,
         ai1i,kappa,nui,mui1,mui2,muim11,muim12,nuim1,tol;
  double g1[] = new double[1];
  double g2[] = new double[1];
  double k1[] = new double[1];
  double k2[] = new double[1];

  hc=0.0;
  tol=em[1]*em[2];
  max=(int) em[4];
  count=0;
  r=0.0;
  if (n > 1) hc=b[n-1];
  do {
    nm1=n-1;
    i=n;
    do {
      q=i;
      i--;
    } while ((i >= 1) ? (Math.abs(b[i]) > tol) : false);
    if (q > 1)
      if (Math.abs(b[q-1]) > r) r=Math.abs(b[q-1]);
    if (q == n) {
      val1[n]=a1[n][n];
      val2[n]=a2[n][n];
      n=nm1;
      if (n > 1) hc=b[n-1];
    } else {
      dd1=a1[n][n];
      dd2=a2[n][n];
      cc=b[nm1];
      comkwd((a1[nm1][nm1]-dd1)/2.0,(a2[nm1][nm1]-dd2)/2.0,
          cc*a1[nm1][n],cc*a2[nm1][n],g1,g2,k1,k2);
      if (q == nm1) {
        val1[nm1]=g1[0]+dd1;
        val2[nm1]=g2[0]+dd2;
        val1[n]=k1[0]+dd1;
        val2[n]=k2[0]+dd2;
        n -= 2;
        if (n > 1) hc=b[n-1];
      } else {
        count++;
        if (count > max) break;
        z1=k1[0]+dd1;
        z2=k2[0]+dd2;
        if (Math.abs(cc) > Math.abs(hc)) z1 += Math.abs(cc);
        hc=cc/2.0;
        i=q1=q+1;
        aij1=a1[q][q]-z1;
        aij2=a2[q][q]-z2;
        ai1i=b[q];
        kappa=Math.sqrt(aij1*aij1+aij2*aij2+ai1i*ai1i);
        mui1=aij1/kappa;
        mui2=aij2/kappa;
        nui=ai1i/kappa;
        a1[q][q]=kappa;
        a2[q][q]=0.0;
        a1[q1][q1] -= z1;
        a2[q1][q1] -= z2;
        Basic.rotcomrow(q1,n,q,q1,a1,a2,mui1,mui2,nui);
        Basic.rotcomcol(q,q,q,q1,a1,a2,mui1,-mui2,-nui);
        a1[q][q] += z1;
        a2[q][q] += z2;
        for (i1=q1+1; i1<=n; i1++) {
          aij1=a1[i][i];
          aij2=a2[i][i];
          ai1i=b[i];
          kappa=Math.sqrt(aij1*aij1+aij2*aij2+ai1i*ai1i);
          muim11=mui1;
          muim12=mui2;
          nuim1=nui;
          mui1=aij1/kappa;
          mui2=aij2/kappa;
          nui=ai1i/kappa;
          a1[i1][i1] -= z1;
          a2[i1][i1] -= z2;
          Basic.rotcomrow(i1,n,i,i1,a1,a2,mui1,mui2,nui);
          a1[i][i]=muim11*kappa;
          a2[i][i] = -muim12*kappa;
          b[i-1]=nuim1*kappa;
          Basic.rotcomcol(q,i,i,i1,a1,a2,mui1,-mui2,-nui);
          a1[i][i] += z1;
          a2[i][i] += z2;
          i=i1;
        }
        aij1=a1[n][n];
        aij2=a2[n][n];
        kappa=Math.sqrt(aij1*aij1+aij2*aij2);
        if ((kappa < tol) ? true : (aij2*aij2 <= em[0]*aij1*aij1)) {
          b[nm1]=nui*aij1;
          a1[n][n]=aij1*mui1+z1;
          a2[n][n] = -aij1*mui2+z2;
        } else {
          b[nm1]=nui*kappa;
          a1nn=mui1*kappa;
          a2nn = -mui2*kappa;
          mui1=aij1/kappa;
          mui2=aij2/kappa;
          Basic.comcolcst(q,nm1,n,a1,a2,mui1,mui2);
          a1[n][n]=mui1*a1nn-mui2*a2nn+z1;
          a2[n][n]=mui1*a2nn+mui2*a1nn+z2;
        }
      }
    }
  } while (n > 0);
  em[3]=r;
  em[5]=count;
  return n;
}


public static int qricom(double a1[][], double a2[][], double b[],
                  int n, double em[], double val1[], double val2[],
                  double vec1[][], double vec2[][])
{
  int m,nm1,i,i1,j,q,q1,max,count;
  double r,z1,z2,dd1,dd2,cc,p1,p2,t1,t2,delta1,delta2,mv1,mv2,h,h1,
         h2,hc,aij12,aij22,a1nn,a2nn,aij1,aij2,ai1i,kappa,nui,mui1,
         mui2,muim11,muim12,nuim1,tol,machtol;
  double tf1[] = new double[n+1];
  double tf2[] = new double[n+1];
  double g1[] = new double[1];
  double g2[] = new double[1];
  double k1[] = new double[1];
  double k2[] = new double[1];
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];
  
  hc=0.0;
  tol=em[1]*em[2];
  machtol=em[0]*em[1];
  max=(int) em[4];
  count=0;
  r=0.0;
  m=n;
  if (n > 1) hc=b[n-1];
  for (i=1; i<=n; i++) {
    vec1[i][i]=1.0;
    vec2[i][i]=0.0;
    for (j=i+1; j<=n; j++)
      vec1[i][j]=vec1[j][i]=vec2[i][j]=vec2[j][i]=0.0;
  }
  do {
    nm1=n-1;
    i=n;
    do {
      q=i;
      i--;
    } while ((i >= 1) ? (Math.abs(b[i]) > tol) : false);
    if (q > 1)
      if (Math.abs(b[q-1]) > r) r=Math.abs(b[q-1]);
    if (q == n) {
      val1[n]=a1[n][n];
      val2[n]=a2[n][n];
      n=nm1;
      if (n > 1) hc=b[n-1];
    } else {
      dd1=a1[n][n];
      dd2=a2[n][n];
      cc=b[nm1];
      p1=(a1[nm1][nm1]-dd1)*0.5;
      p2=(a2[nm1][nm1]-dd2)*0.5;
      comkwd(p1,p2,cc*a1[nm1][n],cc*a2[nm1][n],g1,g2,k1,k2);
      if (q == nm1) {
        a1[n][n]=val1[n]=g1[0]+dd1;
        a2[n][n]=val2[n]=g2[0]+dd2;
        a1[q][q]=val1[q]=k1[0]+dd1;
        a2[q][q]=val2[q]=k2[0]+dd2;
        kappa=Math.sqrt(k1[0]*k1[0]+k2[0]*k2[0]+cc*cc);
        nui=cc/kappa;
        mui1=k1[0]/kappa;
        mui2=k2[0]/kappa;
        aij1=a1[q][n];
        aij2=a2[q][n];
        h1=mui1*mui1-mui2*mui2;
        h2=2.0*mui1*mui2;
        h = -nui*2.0;
        a1[q][n]=h*(p1*mui1+p2*mui2)-nui*nui*cc+aij1*h1+aij2*h2;
        a2[q][n]=h*(p2*mui1-p1*mui2)+aij2*h1-aij1*h2;
        Basic.rotcomrow(q+2,m,q,n,a1,a2,mui1,mui2,nui);
        Basic.rotcomcol(1,q-1,q,n,a1,a2,mui1,-mui2,-nui);
        Basic.rotcomcol(1,m,q,n,vec1,vec2,mui1,-mui2,-nui);
        n -= 2;
        if (n > 1) hc=b[n-1];
        b[q]=0.0;
      } else {
        count++;
        if (count > max) {
          em[3]=r;
          em[5]=count;
          return n;
        }
        z1=k1[0]+dd1;
        z2=k2[0]+dd2;
        if (Math.abs(cc) > Math.abs(hc)) z1 += Math.abs(cc);
        hc=cc/2.0;
        q1=q+1;
        aij1=a1[q][q]-z1;
        aij2=a2[q][q]-z2;
        ai1i=b[q];
        kappa=Math.sqrt(aij1*aij1+aij2*aij2+ai1i*ai1i);
        mui1=aij1/kappa;
        mui2=aij2/kappa;
        nui=ai1i/kappa;
        a1[q][q]=kappa;
        a2[q][q]=0.0;
        a1[q1][q1] -= z1;
        a2[q1][q1] -= z2;
        Basic.rotcomrow(q1,m,q,q1,a1,a2,mui1,mui2,nui);
        Basic.rotcomcol(1,q,q,q1,a1,a2,mui1,-mui2,-nui);
        a1[q][q] += z1;
        a2[q][q] += z2;
        Basic.rotcomcol(1,m,q,q1,vec1,vec2,mui1,-mui2,-nui);
        for (i=q1; i<=nm1; i++) {
          i1=i+1;
          aij1=a1[i][i];
          aij2=a2[i][i];
          ai1i=b[i];
          kappa=Math.sqrt(aij1*aij1+aij2*aij2+ai1i*ai1i);
          muim11=mui1;
          muim12=mui2;
          nuim1=nui;
          mui1=aij1/kappa;
          mui2=aij2/kappa;
          nui=ai1i/kappa;
          a1[i1][i1] -= z1;
          a2[i1][i1] -= z2;
          Basic.rotcomrow(i1,m,i,i1,a1,a2,mui1,mui2,nui);
          a1[i][i]=muim11*kappa;
          a2[i][i] = -muim12*kappa;
          b[i-1]=nuim1*kappa;
          Basic.rotcomcol(1,i,i,i1,a1,a2,mui1,-mui2,-nui);
          a1[i][i] += z1;
          a2[i][i] += z2;
          Basic.rotcomcol(1,m,i,i1,vec1,vec2,mui1,-mui2,-nui);
        }
        aij1=a1[n][n];
        aij2=a2[n][n];
        aij12=aij1*aij1;
        aij22=aij2*aij2;
        kappa=Math.sqrt(aij12+aij22);
        if ((kappa < tol) ? true : (aij22 <= em[0]*aij12)) {
          b[nm1]=nui*aij1;
          a1[n][n]=aij1*mui1+z1;
          a2[n][n] = -aij1*mui2+z2;
        } else {
          b[nm1]=nui*kappa;
          a1nn=mui1*kappa;
          a2nn = -mui2*kappa;
          mui1=aij1/kappa;
          mui2=aij2/kappa;
          Basic.comcolcst(1,nm1,n,a1,a2,mui1,mui2);
          Basic.comcolcst(1,nm1,n,vec1,vec2,mui1,mui2);
          Basic.comrowcst(n+1,m,n,a1,a2,mui1,-mui2);
          Basic.comcolcst(n,m,n,vec1,vec2,mui1,mui2);
          a1[n][n]=mui1*a1nn-mui2*a2nn+z1;
          a2[n][n]=mui1*a2nn+mui2*a1nn+z2;
        }
      }
    }
  } while (n > 0);
  for (j=m; j>=2; j--) {
    tf1[j]=1.0;
    tf2[j]=0.0;
    t1=a1[j][j];
    t2=a2[j][j];
    for (i=j-1; i>=1; i--) {
      delta1=t1-a1[i][i];
      delta2=t2-a2[i][i];
      Basic.commatvec(i+1,j,i,a1,a2,tf1,tf2,tmp1,tmp2);
      mv1=tmp1[0];
      mv2=tmp2[0];
      if (Math.abs(delta1) < machtol && Math.abs(delta2) < machtol) {
        tf1[i]=mv1/machtol;
        tf2[i]=mv2/machtol;
      } else {
        Basic.comdiv(mv1,mv2,delta1,delta2,tmp1,tmp2);
        tf1[i]=tmp1[0];
        tf2[i]=tmp2[0];
      }
    }
    for (i=1; i<=m; i++) {
      Basic.commatvec(1,j,i,vec1,vec2,tf1,tf2,tmp1,tmp2);
      vec1[i][j]=tmp1[0];
      vec2[i][j]=tmp2[0];
    }
  }
  em[3]=r;
  em[5]=count;
  return n;
}


public static int eigvalcom(double ar[][], double ai[][], int n,
                            double em[], double valr[], double vali[])
{
  int i;
  int ind[] = new int[n+1];
  double d[] = new double[n+1];
  double b[] = new double[n+1];
  double del[] = new double[n+1];
  double tr[] = new double[n+1];
  double ti[] = new double[n+1];
  
  eqilbrcom(ar,ai,n,em,d,ind);
  em[1]=Basic.comeucnrm(ar,ai,n-1,n);
  hshcomhes(ar,ai,n,em,b,tr,ti,del);
  i=valqricom(ar,ai,b,n,em,valr,vali);
  return i;
}


public static int eigcom(double ar[][], double ai[][], int n,
                         double em[], double valr[], double vali[],
                         double vr[][], double vi[][])
{
  int i;
  int ind[] = new int[n+1];
  double d[] = new double[n+1];
  double b[] = new double[n+1];
  double del[] = new double[n+1];
  double tr[] = new double[n+1];
  double ti[] = new double[n+1];

  eqilbrcom(ar,ai,n,em,d,ind);
  em[1]=Basic.comeucnrm(ar,ai,n-1,n);
  hshcomhes(ar,ai,n,em,b,tr,ti,del);
  i=qricom(ar,ai,b,n,em,valr,vali,vr,vi);
  if (i == 0) {
    bakcomhes(ar,ai,tr,ti,del,vr,vi,n,1,n);
    baklbrcom(n,1,n,d,ind,vr,vi);
    Basic.sclcom(vr,vi,n,1,n);
  }
  return i;
}


public static void qzival(int n, double a[][], double b[][],
                          double alfr[], double alfi[], double beta[],
                          int iter[], double em[])
{
  boolean stationary,goon,out;
  int i,q,m,m1,q1,j,k,k1,k2,k3,km1,l;
  double dwarf,eps,epsa,epsb,
         anorm,bnorm,ani,bni,constt,a10,a20,a30,b11,b22,b33,b44,a11,
         a12,a21,a22,a33,a34,a43,a44,b12,b34,old1,old2,
         an,bn,e,c,d,er,ei,a11r,a11i,a12r,a12i,a21r,a21i,a22r,a22i,
         cz,szr,szi,cq,sqr,sqi,ssr,ssi,tr,ti,bdr,bdi,r;
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];
  double tmp3[] = new double[1];

  old1=old2=0.0;
  dwarf=em[0];
  eps=em[1];
  hshdecmul(n,a,b,dwarf);
  hestgl2(n,a,b);
  anorm=bnorm=0.0;
  for (i=1; i<=n; i++) {
    bni=0.0;
    iter[i]=0;
    ani = (i > 1) ? Math.abs(a[i][i-1]) : 0.0;
    for (j=i; j<=n; j++) {
      ani += Math.abs(a[i][j]);
      bni += Math.abs(b[i][j]);
    }
    if (ani > anorm) anorm=ani;
    if (bni > bnorm) bnorm=bni;
  }
  if (anorm == 0.0) anorm=eps;
  if (bnorm == 0.0) bnorm=eps;
  epsa=eps*anorm;
  epsb=eps*bnorm;
  m=n;
  out=false;
  do {
    i=q=m;
    while ((i > 1) ? Math.abs(a[i][i-1]) > epsa : false) {
      q=i-1;
      i--;
    }
    if (q > 1) a[q][q-1]=0.0;
    goon=true;
    while (goon) {
      if (q >= m-1) {
        m=q-1;
        goon=false;
      } else {
        if (Math.abs(b[q][q]) <= epsb) {
          b[q][q]=0.0;
          q1=q+1;
          hsh2col(q,q,n,q,a[q][q],a[q1][q],a,b);
          a[q1][q]=0.0;
          q=q1;
        } else {
          goon=false;
          m1=m-1;
          q1=q+1;
          constt=0.75;
          (iter[m])++;
          stationary = (iter[m] == 1) ? true :
            (Math.abs(a[m][m-1]) >= constt*old1 &&
            Math.abs(a[m-1][m-2]) >= constt*old2);
          if (iter[m] > 30 && stationary) {
            for (i=1; i<=m; i++) iter[i] = -1;
            out=true;
            break;
          }
          if (iter[m] == 10 && stationary) {
            a10=0.0;
            a20=1.0;
            a30=1.1605;
          } else {
            b11=b[q][q];
            b22 = (Math.abs(b[q1][q1]) < epsb) ? epsb : b[q1][q1];
            b33 = (Math.abs(b[m1][m1]) < epsb) ? epsb : b[m1][m1];
            b44 = (Math.abs(b[m][m]) < epsb) ? epsb : b[m][m];
            a11=a[q][q]/b11;
            a12=a[q][q1]/b22;
            a21=a[q1][q]/b11;
            a22=a[q1][q1]/b22;
            a33=a[m1][m1]/b33;
            a34=a[m1][m]/b44;
            a43=a[m][m1]/b33;
            a44=a[m][m]/b44;
            b12=b[q][q1]/b22;
            b34=b[m1][m]/b44;
            a10=((a33-a11)*(a44-a11)-a34*a43+a43*b34*a11)/a21+
                a12-a11*b12;
            a20=(a22-a11-a21*b12)-(a33-a11)-(a44-a11)+a43*b34;
            a30=a[q+2][q1]/b22;
          }
          old1=Math.abs(a[m][m-1]);
          old2=Math.abs(a[m-1][m-2]);
          for (k=q; k<=m1; k++) {
            k1=k+1;
            k2=k+2;
            k3 = (k+3 > m) ? m : k+3;
            km1 = (k-1 < q) ? q : k-1;
            if (k != m1) {
              if (k == q)
                hsh3col(km1,km1,n,k,a10,a20,a30,a,b);
              else {
                hsh3col(km1,km1,n,k,a[k][km1],
                      a[k1][km1],a[k2][km1],a,b);
                a[k1][km1]=a[k2][km1]=0.0;
              }
              hsh3row2(1,k3,k,b[k2][k2],b[k2][k1],
                    b[k2][k],a,b);
              b[k2][k]=b[k2][k1]=0.0;
            } else {
              hsh2col(km1,km1,n,k,a[k][km1],a[k1][km1],a,b);
              a[k1][km1]=0.0;
            }
            hsh2row2(1,k3,k3,k,b[k1][k1],b[k1][k],a,b);
            b[k1][k]=0.0;
          }
        }
      }
    } /* goon loop */
    if (out) break;
  } while (m >= 3);

  m=n;
  do {
    if ((m > 1) ? (a[m][m-1] == 0) : true) {
      alfr[m]=a[m][m];
      beta[m]=b[m][m];
      alfi[m]=0.0;
      m--;
    } else {
      l=m-1;
      if (Math.abs(b[l][l]) <= epsb) {
        b[l][l]=0.0;
        hsh2col(l,l,n,l,a[l][l],a[m][l],a,b);
        a[m][l]=b[m][l]=0.0;
        alfr[l]=a[l][l];
        alfr[m]=a[m][m];
        beta[l]=b[l][l];
        beta[m]=b[m][m];
        alfi[m]=alfi[l]=0.0;
      } else
        if (Math.abs(b[m][m]) <= epsb) {
          b[m][m]=0.0;
          hsh2row2(1,m,m,l,a[m][m],a[m][l],a,b);
          a[m][l]=b[m][l]=0.0;
          alfr[l]=a[l][l];
          alfr[m]=a[m][m];
          beta[l]=b[l][l];
          beta[m]=b[m][m];
          alfi[m]=alfi[l]=0.0;
        } else {
          an=Math.abs(a[l][l])+Math.abs(a[l][m])+Math.abs(a[m][l])+
            Math.abs(a[m][m]);
          bn=Math.abs(b[l][l])+Math.abs(b[l][m])+Math.abs(b[m][m]);
          a11=a[l][l]/an;
          a12=a[l][m]/an;
          a21=a[m][l]/an;
          a22=a[m][m]/an;
          b11=b[l][l]/bn;
          b12=b[l][m]/bn;
          b22=b[m][m]/bn;
          e=a11/b11;
          c=((a22-e*b22)/b22-(a21*b12)/(b11*b22))/2.0;
          d=c*c+(a21*(a12-e*b12))/(b11*b22);
          if (d >= 0.0) {
            e += ((c < 0.0) ? c-Math.sqrt(d) : c+Math.sqrt(d));
            a11 -= e*b11;
            a12 -= e*b12;
            a22 -= e*b22;
            if (Math.abs(a11)+Math.abs(a12) >= Math.abs(a21)+Math.abs(a22))
              hsh2row2(1,m,m,l,a12,a11,a,b);
            else
              hsh2row2(1,m,m,l,a22,a21,a,b);
            if (an >= Math.abs(e)*bn)
              hsh2col(l,l,n,l,b[l][l],b[m][l],a,b);
            else
              hsh2col(l,l,n,l,a[l][l],a[m][l],a,b);
            a[m][l]=b[m][l]=0.0;
            alfr[l]=a[l][l];
            alfr[m]=a[m][m];
            beta[l]=b[l][l];
            beta[m]=b[m][m];
            alfi[m]=alfi[l]=0.0;
          } else {
            er=e+c;
            ei=Math.sqrt(-d);
            a11r=a11-er*b11;
            a11i=ei*b11;
            a12r=a12-er*b12;
            a12i=ei*b12;
            a21r=a21;
            a21i=0.0;
            a22r=a22-er*b22;
            a22i=ei*b22;
            if (Math.abs(a11r)+Math.abs(a11i)+Math.abs(a12r)+
                Math.abs(a12i) >= Math.abs(a21r)+Math.abs(a22r)+
                                  Math.abs(a22i)) {
              Basic.chsh2(a12r,a12i,-a11r,-a11i,tmp1,tmp2,tmp3);
              cz=tmp1[0];
              szr=tmp2[0];
              szi=tmp3[0];
            }
            else {
              Basic.chsh2(a22r,a22i,-a21r,-a21i,tmp1,tmp2,tmp3);
              cz=tmp1[0];
              szr=tmp2[0];
              szi=tmp3[0];
            }
            if (an >= (Math.abs(er)+Math.abs(ei))*bn) {
              Basic.chsh2(cz*b11+szr*b12,szi*b12,szr*b22,szi*b22,
                          tmp1,tmp2,tmp3);
              cq=tmp1[0];
              sqr=tmp2[0];
              sqi=tmp3[0];
            } 
            else {
              Basic.chsh2(cz*a11+szr*a12,szi*a12,cz*a21+szr*a22,
                    szi*a22,tmp1,tmp2,tmp3);
              cq=tmp1[0];
              sqr=tmp2[0];
              sqi=tmp3[0];
            }
            ssr=sqr*szr+sqi*szi;
            ssi=sqr*szi-sqi*szr;
            tr=cq*cz*a11+cq*szr*a12+sqr*cz*a21+ssr*a22;
            ti=cq*szi*a12-sqi*cz*a21+ssi*a22;
            bdr=cq*cz*b11+cq*szr*b12+ssr*b22;
            bdi=cq*szi*b12+ssi*b22;
            r=Math.sqrt(bdr*bdr+bdi*bdi);
            beta[l]=bn*r;
            alfr[l]=an*(tr*bdr+ti*bdi)/r;
            alfi[l]=an*(tr*bdi-ti*bdr)/r;
            tr=ssr*a11-sqr*cz*a12-cq*szr*a21+cq*cz*a22;
            ti = -ssi*a11-sqi*cz*a12+cq*szi*a21;
            bdr=ssr*b11-sqr*cz*b12+cq*cz*b22;
            bdi = -ssi*b11-sqi*cz*b12;
            r=Math.sqrt(bdr*bdr+bdi*bdi);
            beta[m]=bn*r;
            alfr[m]=an*(tr*bdr+ti*bdi)/r;
            alfi[m]=an*(tr*bdi-ti*bdr)/r;
          }
        }
      m -= 2;
    }
  } while (m > 0);
}


public static void qzi(int n, double a[][], double b[][],
                       double x[][], double alfr[], double alfi[],
                       double beta[], int iter[], double em[])
{
  boolean stationary,goon,out;
  int i,q,m,m1,q1,j,k,k1,k2,k3,km1,l,mr,mi,l1;
  double dwarf,eps,epsa,epsb,
         anorm,bnorm,ani,bni,constt,a10,a20,a30,b11,b22,b33,b44,a11,
         a12,a21,a22,a33,a34,a43,a44,b12,b34,old1,old2,
         an,bn,e,c,d,er,ei,a11r,a11i,a12r,a12i,a21r,a21i,a22r,a22i,
         cz,szr,szi,cq,sqr,sqi,ssr,ssi,tr,ti,bdr,bdi,r,
         betm,alfm,sl,sk,tkk,tkl,tlk,tll,almi,almr,slr,sli,skr,ski,
         dr,di,tkkr,tkki,tklr,tkli,tlkr,tlki,tllr,tlli,s;
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];
  double tmp3[] = new double[1];
  
  old1=old2=d=dr=di=0.0;
  dwarf=em[0];
  eps=em[1];
  hshdecmul(n,a,b,dwarf);
  hestgl3(n,a,b,x);
  anorm=bnorm=0.0;
  for (i=1; i<=n; i++) {
    bni=0.0;
    iter[i]=0;
    ani = (i > 1) ? Math.abs(a[i][i-1]) : 0.0;
    for (j=i; j<=n; j++) {
      ani += Math.abs(a[i][j]);
      bni += Math.abs(b[i][j]);
    }
    if (ani > anorm) anorm=ani;
    if (bni > bnorm) bnorm=bni;
  }
  if (anorm == 0.0) anorm=eps;
  if (bnorm == 0.0) bnorm=eps;
  epsa=eps*anorm;
  epsb=eps*bnorm;
  m=n;
  out=false;
  do {
    i=q=m;
    while ((i > 1) ? Math.abs(a[i][i-1]) > epsa : false) {
      q=i-1;
      i--;
    }
    if (q > 1) a[q][q-1]=0.0;
    goon=true;
    while (goon) {
      if (q >= m-1) {
        m=q-1;
        goon=false;
      } else {
        if (Math.abs(b[q][q]) <= epsb) {
          b[q][q]=0.0;
          q1=q+1;
          hsh2col(q,q,n,q,a[q][q],a[q1][q],a,b);
          a[q1][q]=0.0;
          q=q1;
        } else {
          goon=false;
          m1=m-1;
          q1=q+1;
          constt=0.75;
          (iter[m])++;
          stationary = (iter[m] == 1) ? true :
            (Math.abs(a[m][m-1]) >= constt*old1 &&
             Math.abs(a[m-1][m-2]) >= constt*old2);
          if (iter[m] > 30 && stationary) {
            for (i=1; i<=m; i++) iter[i] = -1;
            out=true;
            break;
          }
          if (iter[m] == 10 && stationary) {
            a10=0.0;
            a20=1.0;
            a30=1.1605;
          } else {
            b11=b[q][q];
            b22 = (Math.abs(b[q1][q1]) < epsb) ? epsb : b[q1][q1];
            b33 = (Math.abs(b[m1][m1]) < epsb) ? epsb : b[m1][m1];
            b44 = (Math.abs(b[m][m]) < epsb) ? epsb : b[m][m];
            a11=a[q][q]/b11;
            a12=a[q][q1]/b22;
            a21=a[q1][q]/b11;
            a22=a[q1][q1]/b22;
            a33=a[m1][m1]/b33;
            a34=a[m1][m]/b44;
            a43=a[m][m1]/b33;
            a44=a[m][m]/b44;
            b12=b[q][q1]/b22;
            b34=b[m1][m]/b44;
            a10=((a33-a11)*(a44-a11)-a34*a43+a43*b34*a11)/a21+
                a12-a11*b12;
            a20=(a22-a11-a21*b12)-(a33-a11)-(a44-a11)+a43*b34;
            a30=a[q+2][q1]/b22;
          }
          old1=Math.abs(a[m][m-1]);
          old2=Math.abs(a[m-1][m-2]);
          for (k=q; k<=m1; k++) {
            k1=k+1;
            k2=k+2;
            k3 = (k+3 > m) ? m : k+3;
            km1 = (k-1 < q) ? q : k-1;
            if (k != m1) {
              if (k == q)
                hsh3col(km1,km1,n,k,a10,a20,a30,a,b);
              else {
                hsh3col(km1,km1,n,k,a[k][km1],
                      a[k1][km1],a[k2][km1],a,b);
                a[k1][km1]=a[k2][km1]=0.0;
              }
              hsh3row3(1,k3,n,k,b[k2][k2],b[k2][k1],
                    b[k2][k],a,b,x);
              b[k2][k]=b[k2][k1]=0.0;
            } else {
              hsh2col(km1,km1,n,k,a[k][km1],a[k1][km1],a,b);
              a[k1][km1]=0.0;
            }
            hsh2row3(1,k3,k3,n,k,b[k1][k1],b[k1][k],a,b,x);
            b[k1][k]=0.0;
          }
        }
      }
    } /* goon loop */
    if (out) break;
  } while (m >= 3);

  m=n;
  do {
    if ((m > 1) ? (a[m][m-1] == 0) : true) {
      alfr[m]=a[m][m];
      beta[m]=b[m][m];
      alfi[m]=0.0;
      m--;
    } else {
      l=m-1;
      if (Math.abs(b[l][l]) <= epsb) {
        b[l][l]=0.0;
        hsh2col(l,l,n,l,a[l][l],a[m][l],a,b);
        a[m][l]=b[m][l]=0.0;
        alfr[l]=a[l][l];
        alfr[m]=a[m][m];
        beta[l]=b[l][l];
        beta[m]=b[m][m];
        alfi[m]=alfi[l]=0.0;
      } else
        if (Math.abs(b[m][m]) <= epsb) {
          b[m][m]=0.0;
          hsh2row3(1,m,m,n,l,a[m][m],a[m][l],a,b,x);
          a[m][l]=b[m][l]=0.0;
          alfr[l]=a[l][l];
          alfr[m]=a[m][m];
          beta[l]=b[l][l];
          beta[m]=b[m][m];
          alfi[m]=alfi[l]=0.0;
        } else {
          an=Math.abs(a[l][l])+Math.abs(a[l][m])+Math.abs(a[m][l])+
            Math.abs(a[m][m]);
          bn=Math.abs(b[l][l])+Math.abs(b[l][m])+Math.abs(b[m][m]);
          a11=a[l][l]/an;
          a12=a[l][m]/an;
          a21=a[m][l]/an;
          a22=a[m][m]/an;
          b11=b[l][l]/bn;
          b12=b[l][m]/bn;
          b22=b[m][m]/bn;
          e=a11/b11;
          c=((a22-e*b22)/b22-(a21*b12)/(b11*b22))/2.0;
          d=c*c+(a21*(a12-e*b12))/(b11*b22);
          if (d >= 0.0) {
            e += ((c < 0.0) ? c-Math.sqrt(d) : c+Math.sqrt(d));
            a11 -= e*b11;
            a12 -= e*b12;
            a22 -= e*b22;
            if (Math.abs(a11)+Math.abs(a12) >= Math.abs(a21)+Math.abs(a22))
              hsh2row3(1,m,m,n,l,a12,a11,a,b,x);
            else
              hsh2row3(1,m,m,n,l,a22,a21,a,b,x);
            if (an >= Math.abs(e)*bn)
              hsh2col(l,l,n,l,b[l][l],b[m][l],a,b);
            else
              hsh2col(l,l,n,l,a[l][l],a[m][l],a,b);
            a[m][l]=b[m][l]=0.0;
            alfr[l]=a[l][l];
            alfr[m]=a[m][m];
            beta[l]=b[l][l];
            beta[m]=b[m][m];
            alfi[m]=alfi[l]=0.0;
          } else {
            er=e+c;
            ei=Math.sqrt(-d);
            a11r=a11-er*b11;
            a11i=ei*b11;
            a12r=a12-er*b12;
            a12i=ei*b12;
            a21r=a21;
            a21i=0.0;
            a22r=a22-er*b22;
            a22i=ei*b22;
            if (Math.abs(a11r)+Math.abs(a11i)+Math.abs(a12r)+
                Math.abs(a12i) >= Math.abs(a21r)+Math.abs(a22r)+
                                  Math.abs(a22i)) {
              Basic.chsh2(a12r,a12i,-a11r,-a11i,tmp1,tmp2,tmp3);
              cz=tmp1[0];
              szr=tmp2[0];
              szi=tmp3[0];
            }
            else {
              Basic.chsh2(a22r,a22i,-a21r,-a21i,tmp1,tmp2,tmp3);
              cz=tmp1[0];
              szr=tmp2[0];
              szi=tmp3[0];
            }
            if (an >= (Math.abs(er)+Math.abs(ei))*bn) {
              Basic.chsh2(cz*b11+szr*b12,szi*b12,szr*b22,szi*b22,
                          tmp1,tmp2,tmp3);
              cq=tmp1[0];
              sqr=tmp2[0];
              sqi=tmp3[0];
            }
            else {
              Basic.chsh2(cz*a11+szr*a12,szi*a12,cz*a21+szr*a22,szi*a22,
                          tmp1,tmp2,tmp3);
              cq=tmp1[0];
              sqr=tmp2[0];
              sqi=tmp3[0];
            }
            ssr=sqr*szr+sqi*szi;
            ssi=sqr*szi-sqi*szr;
            tr=cq*cz*a11+cq*szr*a12+sqr*cz*a21+ssr*a22;
            ti=cq*szi*a12-sqi*cz*a21+ssi*a22;
            bdr=cq*cz*b11+cq*szr*b12+ssr*b22;
            bdi=cq*szi*b12+ssi*b22;
            r=Math.sqrt(bdr*bdr+bdi*bdi);
            beta[l]=bn*r;
            alfr[l]=an*(tr*bdr+ti*bdi)/r;
            alfi[l]=an*(tr*bdi-ti*bdr)/r;
            tr=ssr*a11-sqr*cz*a12-cq*szr*a21+cq*cz*a22;
            ti = -ssi*a11-sqi*cz*a12+cq*szi*a21;
            bdr=ssr*b11-sqr*cz*b12+cq*cz*b22;
            bdi = -ssi*b11-sqi*cz*b12;
            r=Math.sqrt(bdr*bdr+bdi*bdi);
            beta[m]=bn*r;
            alfr[m]=an*(tr*bdr+ti*bdi)/r;
            alfi[m]=an*(tr*bdi-ti*bdr)/r;
          }
        }
      m -= 2;
    }
  } while (m > 0);

  for (m=n; m>=1; m--)
    if (alfi[m] == 0.0) {
      alfm=alfr[m];
      betm=beta[m];
      b[m][m]=1.0;
      l1=m;
      for (l=m-1; l>=1; l--) {
        sl=0.0;
        for (j=l1; j<=m; j++)
          sl += (betm*a[l][j]-alfm*b[l][j])*b[j][m];
        if ((l != 1) ? (betm*a[l][l-1] == 0.0) : true) {
          d=betm*a[l][l]-alfm*b[l][l];
          if (d == 0.0) d=(epsa+epsb)/2.0;
          b[l][m] = -sl/d;
        } else {
          k=l-1;
          sk=0.0;
          for (j=l1; j<=m; j++)
            sk += (betm*a[k][j]-alfm*b[k][j])*b[j][m];
          tkk=betm*a[k][k]-alfm*b[k][k];
          tkl=betm*a[k][l]-alfm*b[k][l];
          tlk=betm*a[l][k];
          tll=betm*a[l][l]-alfm*b[l][l];
          d=tkk*tll-tkl*tlk;
          if (d == 0.0) d=(epsa+epsb)/2.0;
          b[l][m]=(tlk*sk-tkk*sl)/d;
          b[k][m] = (Math.abs(tkk) >= Math.abs(tlk)) ?
                -(sk+tkl*b[l][m])/tkk :
                -(sl+tll*b[l][m])/tlk;
          l--;
        }
        l1=l;
      }
    } else {
      almr=alfr[m-1];
      almi=alfi[m-1];
      betm=beta[m-1];
      mr=m-1;
      mi=m;
      b[m-1][mr]=almi*b[m][m]/(betm*a[m][m-1]);
      b[m-1][mi]=(betm*a[m][m]-almr*b[m][m])/(betm*a[m][m-1]);
      b[m][mr]=0.0;
      b[m][mi] = -1.0;
      l1=m-1;
      for (l=m-2; l>=1; l--) {
        slr=sli=0.0;
        for (j=l1; j<=m; j++) {
          tr=betm*a[l][j]-almr*b[l][j];
          ti = -almi*b[l][j];
          slr += tr*b[j][mr]-ti*b[j][mi];
          sli += tr*b[j][mi]+ti*b[j][mr];
        }
        if ((l != 1) ? (betm*a[l][l-1] == 0.0) : true) {
          dr=betm*a[l][l]-almr*b[l][l];
          di = -almi*b[l][l];
          Basic.comdiv(-slr,-sli,dr,di,tmp1,tmp2);
          b[l][mr]=tmp1[0];
          b[l][mi]=tmp2[0];
        } else {
          k=l-1;
          skr=ski=0.0;
          for (j=l1; j<=m; j++) {
            tr=betm*a[k][j]-almr*b[k][j];
            ti = -almi*b[k][j];
            skr += tr*b[j][mr]-ti*b[j][mi];
            ski += tr*b[j][mi]+ti*b[j][mr];
          }
          tkkr=betm*a[k][k]-almr*b[k][k];
          tkki = -almi*b[k][k];
          tklr=betm*a[k][l]-almr*b[k][l];
          tkli = -almi*b[k][l];
          tlkr=betm*a[l][k];
          tlki=0.0;
          tllr=betm*a[l][l]-almr*b[l][l];
          tlli = -almi*b[l][l];
          dr=tkkr*tllr-tkki*tlli-tklr*tlkr;
          di=tkkr*tlli+tkki*tllr-tkli*tlkr;
          if (dr == 0.0 && di == 0.0) dr=(epsa+epsb)/2.0;
          Basic.comdiv(tlkr*skr-tkkr*slr+tkki*sli,
                       tlkr*ski-tkkr*sli-tkki*slr,dr,di,tmp1,tmp2);
          b[l][mr]=tmp1[0];
          b[l][mi]=tmp2[0];
          if (Math.abs(tkkr)+Math.abs(tkki) >= Math.abs(tlkr)) {
            Basic.comdiv(-skr-tklr*b[l][mr]+tkli*b[l][mi],
                   -ski-tklr*b[l][mi]-tkli*b[l][mr],tkkr,tkki,tmp1,tmp2);
            b[k][mr]=tmp1[0];
            b[k][mi]=tmp2[0];
          }
          else {
            Basic.comdiv(-slr-tllr*b[l][mr]+tlli*b[l][mi],
                   -sli-tllr*b[l][mi]-tlli*b[l][mr],tlkr,tlki,tmp1,tmp2);
            b[k][mr]=tmp1[0];
            b[k][mi]=tmp2[0];
          }
          l--;
        }
        l1=l;
      }
      m--;
    }
  for (m=n; m>=1; m--)
    for (k=1; k<=n; k++) x[k][m]=Basic.matmat(1,m,k,m,x,b);
  for (m=n; m>=1; m--) {
    s=0.0;
    if (alfi[m] == 0.0) {
      for (k=1; k<=n; k++) {
        r=Math.abs(x[k][m]);
        if (r >= s) {
          s=r;
          d=x[k][m];
        }
      }
      for (k=1; k<=n; k++) x[k][m] /= d;
    } else {
      for (k=1; k<=n; k++) {
        r=Math.abs(x[k][m-1])+Math.abs(x[k][m]);
        an=x[k][m-1]/r;
        bn=x[k][m]/r;
        r *= Math.sqrt(an*an+bn*bn);
        if (r >= s) {
          s=r;
          dr=x[k][m-1];
          di=x[k][m];
        }
      }
      for (k=1; k<=n; k++) {
        Basic.comdiv(x[k][m-1],x[k][m],dr,di,tmp1,tmp2);
        x[k][m-1]=tmp1[0];
        x[k][m]=tmp2[0];
      }
      m--;
    }
  }
}


public static void hshdecmul(int n, double a[][],
                             double b[][], double dwarf)
{
  int j,k,k1,n1;
  double r,t,c;
  double v[] = new double[n+1];
  
  k=1;
  n1=n+1;
  for (k1=2; k1<=n1; k1++) {
    r=Basic.tammat(k1,n,k,k,b,b);
    if (r > dwarf) {
      r = (b[k][k] < 0.0) ? -Math.sqrt(r+b[k][k]*b[k][k]) :
              Math.sqrt(r+b[k][k]*b[k][k]);
      t=b[k][k]+r;
      c = -t/r;
      b[k][k] = -r;
      v[k]=1.0;
      for (j=k1; j<=n; j++) v[j]=b[j][k]/t;
      Basic.hshvecmat(k,n,k1,n,c,v,b);
      Basic.hshvecmat(k,n,1,n,c,v,a);
    }
    k=k1;
  }
}


public static void hestgl3(int n, double a[][], double b[][],
                           double x[][])
{
  int nm1,k,l,k1,l1;

  if (n > 2) {
    for (k=2; k<=n; k++)
      for (l=1; l<=k-1; l++) b[k][l]=0.0;
    nm1=n-1;
    k=1;
    for (k1=2; k1<=nm1; k1++) {
      l1=n;
      for (l=n-1; l>=k1; l--) {
        hsh2col(k,l,n,l,a[l][k],a[l1][k],a,b);
        a[l1][k]=0.0;
        hsh2row3(1,n,l1,n,l,b[l1][l1],b[l1][l],a,b,x);
        b[l1][l]=0.0;
        l1=l;
      }
      k=k1;
    }
  }
}


public static void hestgl2(int n, double a[][], double b[][])
{
  int nm1,k,l,k1,l1;

  if (n > 2) {
    for (k=2; k<=n; k++)
      for (l=1; l<=k-1; l++) b[k][l]=0.0;
    nm1=n-1;
    k=1;
    for (k1=2; k1<=nm1; k1++) {
      l1=n;
      for (l=n-1; l>=k1; l--) {
        hsh2col(k,l,n,l,a[l][k],a[l1][k],a,b);
        a[l1][k]=0.0;
        hsh2row2(1,n,l1,l,b[l1][l1],b[l1][l],a,b);
        b[l1][l]=0.0;
        l1=l;
      }
      k=k1;
    }
  }
}


public static void hsh2col(int la, int lb, int u, int i, double a1,
                           double a2, double a[][], double b[][])
{
  double d1,d2,s1,s2,r,d,c;

  if (a2 != 0.0) {
    double v[] = new double[i+2];
    d1=Math.abs(a1);
    d2=Math.abs(a2);
    s1 = (a1 >= 0.0) ? 1.0 : -1.0;
    s2 = (a2 >= 0.0) ? 1.0 : -1.0;
    if (d2 <= d1) {
      r=d2/d1;
      d=Math.sqrt(1.0+r*r);
      c = -1.0-1.0/d;
      v[i+1]=s1*s2*r/(1.0+d);
    } else {
      r=d1/d2;
      d=Math.sqrt(1.0+r*r);
      c = -1.0-r/d;
      v[i+1]=s1*s2/(r+d);
    }
    v[i]=1.0;
    Basic.hshvecmat(i,i+1,la,u,c,v,a);
    Basic.hshvecmat(i,i+1,lb,u,c,v,b);
  }
}


public static void hsh3col(int la, int lb, int u, int i, double a1,
                   double a2, double a3, double a[][], double b[][])
{
  double c,d1,d2,d3,s1,s2,s3,r1,r2,r3,d;

  if (a2 != 0.0 || a3 != 0.0) {
    double v[] = new double[i+3];
    d1=Math.abs(a1);
    d2=Math.abs(a2);
    d3=Math.abs(a3);
    s1 = (a1 >= 0.0) ? 1.0 : -1.0;
    s2 = (a2 >= 0.0) ? 1.0 : -1.0;
    s3 = (a3 >= 0.0) ? 1.0 : -1.0;
    if (d1 >= d2 && d1 >= d3) {
      r2=d2/d1;
      r3=d3/d1;
      d=Math.sqrt(1.0+r2*r2+r3*r3);
      c = -1.0-(1.0/d);
      d=1.0/(1.0+d);
      v[i+1]=s1*s2*r2*d;
      v[i+2]=s1*s3*r3*d;
    } else if (d2 >= d1 && d2 >= d3) {
      r1=d1/d2;
      r3=d3/d2;
      d=Math.sqrt(1.0+r1*r1+r3*r3);
      c = -1.0-(s1*r1/d);
      d=1.0/(r1+d);
      v[i+1]=s1*s2*d;
      v[i+2]=s1*s3*r3*d;
    } else {
      r1=d1/d3;
      r2=d2/d3;
      d=Math.sqrt(1.0+r1*r1+r2*r2);
      c = -1.0-(s1*r1/d);
      d=1.0/(r1+d);
      v[i+1]=s1*s2*r2*d;
      v[i+2]=s1*s3*d;
    }
    v[i]=1.0;
    Basic.hshvecmat(i,i+2,la,u,c,v,a);
    Basic.hshvecmat(i,i+2,lb,u,c,v,b);
  }
}


public static void hsh2row3(int l, int ua, int ub, int ux, int j,
       double a1, double a2, double a[][], double b[][], double x[][])
{
  double d1,d2,s1,s2,r,d,c;

  if (a2 != 0.0) {
    double v[] = new double[j+2];
    d1=Math.abs(a1);
    d2=Math.abs(a2);
    s1 = (a1 >= 0.0) ? 1.0 : -1.0;
    s2 = (a2 >= 0.0) ? 1.0 : -1.0;
    if (d2 <= d1) {
      r=d2/d1;
      d=Math.sqrt(1.0+r*r);
      c = -1.0-1.0/d;
      v[j]=s1*s2*r/(1.0+d);
    } else {
      r=d1/d2;
      d=Math.sqrt(1.0+r*r);
      c = -1.0-r/d;
      v[j]=s1*s2/(r+d);
    }
    v[j+1]=1.0;
    Basic.hshvectam(l,ua,j,j+1,c,v,a);
    Basic.hshvectam(l,ub,j,j+1,c,v,b);
    Basic.hshvectam(1,ux,j,j+1,c,v,x);
  }
}


public static void hsh2row2(int l, int ua, int ub, int j, double a1,
                            double a2, double a[][], double b[][])
{
  double d1,d2,s1,s2,r,d,c;

  if (a2 != 0.0) {
    double v[] = new double[j+2];
    d1=Math.abs(a1);
    d2=Math.abs(a2);
    s1 = (a1 >= 0.0) ? 1.0 : -1.0;
    s2 = (a2 >= 0.0) ? 1.0 : -1.0;
    if (d2 <= d1) {
      r=d2/d1;
      d=Math.sqrt(1.0+r*r);
      c = -1.0-1.0/d;
      v[j]=s1*s2*r/(1.0+d);
    } else {
      r=d1/d2;
      d=Math.sqrt(1.0+r*r);
      c = -1.0-r/d;
      v[j]=s1*s2/(r+d);
    }
    v[j+1]=1.0;
    Basic.hshvectam(l,ua,j,j+1,c,v,a);
    Basic.hshvectam(l,ub,j,j+1,c,v,b);
  }
}


public static void hsh3row3(int l, int u, int ux, int j, double a1,
       double a2, double a3, double a[][], double b[][], double x[][])
{
  double c,d1,d2,d3,s1,s2,s3,r1,r2,r3,d;

  if (a2 != 0.0 || a3 != 0.0) {
    double v[] = new double[j+3];
    d1=Math.abs(a1);
    d2=Math.abs(a2);
    d3=Math.abs(a3);
    s1 = (a1 >= 0.0) ? 1.0 : -1.0;
    s2 = (a2 >= 0.0) ? 1.0 : -1.0;
    s3 = (a3 >= 0.0) ? 1.0 : -1.0;
    if (d1 >= d2 && d1 >= d3) {
      r2=d2/d1;
      r3=d3/d1;
      d=Math.sqrt(1.0+r2*r2+r3*r3);
      c = -1.0-(1.0/d);
      d=1.0/(1.0+d);
      v[j+1]=s1*s2*r2*d;
      v[j]=s1*s3*r3*d;
    } else if (d2 >= d1 && d2 >= d3) {
      r1=d1/d2;
      r3=d3/d2;
      d=Math.sqrt(1.0+r1*r1+r3*r3);
      c = -1.0-(s1*r1/d);
      d=1.0/(r1+d);
      v[j+1]=s1*s2*d;
      v[j]=s1*s3*r3*d;
    } else {
      r1=d1/d3;
      r2=d2/d3;
      d=Math.sqrt(1.0+r1*r1+r2*r2);
      c = -1.0-(s1*r1/d);
      d=1.0/(r1+d);
      v[j+1]=s1*s2*r2*d;
      v[j]=s1*s3*d;
    }
    v[j+2]=1.0;
    Basic.hshvectam(l,u,j,j+2,c,v,a);
    Basic.hshvectam(l,u,j,j+2,c,v,b);
    Basic.hshvectam(l,ux,j,j+2,c,v,x);
  }
}


public static void hsh3row2(int l, int u, int j, double a1, double a2,
                            double a3, double a[][], double b[][])
{
  double c,d1,d2,d3,s1,s2,s3,r1,r2,r3,d;

  if (a2 != 0.0 || a3 != 0.0) {
    double v[] = new double[j+3];
    d1=Math.abs(a1);
    d2=Math.abs(a2);
    d3=Math.abs(a3);
    s1 = (a1 >= 0.0) ? 1.0 : -1.0;
    s2 = (a2 >= 0.0) ? 1.0 : -1.0;
    s3 = (a3 >= 0.0) ? 1.0 : -1.0;
    if (d1 >= d2 && d1 >= d3) {
      r2=d2/d1;
      r3=d3/d1;
      d=Math.sqrt(1.0+r2*r2+r3*r3);
      c = -1.0-(1.0/d);
      d=1.0/(1.0+d);
      v[j+1]=s1*s2*r2*d;
      v[j]=s1*s3*r3*d;
    } else if (d2 >= d1 && d2 >= d3) {
      r1=d1/d2;
      r3=d3/d2;
      d=Math.sqrt(1.0+r1*r1+r3*r3);
      c = -1.0-(s1*r1/d);
      d=1.0/(r1+d);
      v[j+1]=s1*s2*d;
      v[j]=s1*s3*r3*d;
    } else {
      r1=d1/d3;
      r2=d2/d3;
      d=Math.sqrt(1.0+r1*r1+r2*r2);
      c = -1.0-(s1*r1/d);
      d=1.0/(r1+d);
      v[j+1]=s1*s2*r2*d;
      v[j]=s1*s3*d;
    }
    v[j+2]=1.0;
    Basic.hshvectam(l,u,j,j+2,c,v,a);
    Basic.hshvectam(l,u,j,j+2,c,v,b);
  }
}


public static int qrisngvalbid(double d[], double b[], int n, double em[])
{
  int n1,k,k1,i,i1,count,max,rnk;
  double tol,bmax,z,x,y,g,h,f,c,s,min;

  tol=em[2]*em[1];
  count=0;
  bmax=0.0;
  max=(int) em[4];
  min=em[6];
  rnk=n;
  do {
    k=n;
    n1=n-1;
    while (true) {
      k--;
      if (k <= 0) break;
      if (Math.abs(b[k]) >= tol) {
        if (Math.abs(d[k]) < tol) {
          c=0.0;
          s=1.0;
          for (i=k; i<=n1; i++) {
            f=s*b[i];
            b[i] *= c;
            i1=i+1;
            if (Math.abs(f) < tol) break;
            g=d[i1];
            d[i1]=h=Math.sqrt(f*f+g*g);
            c=g/h;
            s = -f/h;
          }
          break;
        }
      } else {
        if (Math.abs(b[k]) > bmax) bmax=Math.abs(b[k]);
        break;
      }
    }
    if (k == n1) {
      if (d[n] < 0.0) d[n] = -d[n];
      if (d[n] <= min) rnk--;
      n=n1;
    } else {
      count++;
      if (count > max) break;
      k1=k+1;
      z=d[n];
      x=d[k1];
      y=d[n1];
      g = (n1 == 1) ? 0.0 : b[n1-1];
      h=b[n1];
      f=((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y);
      g=Math.sqrt(f*f+1.0);
      f=((x-z)*(x+z)+h*(y/((f < 0.0) ? f-g : f+g)-h))/x;
      c=s=1.0;
      for (i=k1+1; i<=n; i++) {
        i1=i-1;
        g=b[i1];
        y=d[i];
        h=s*g;
        g *= c;
        z=Math.sqrt(f*f+h*h);
        c=f/z;
        s=h/z;
        if (i1 != k1) b[i1-1]=z;
        f=x*c+g*s;
        g=g*c-x*s;
        h=y*s;
        y *= c;
        d[i1]=z=Math.sqrt(f*f+h*h);
        c=f/z;
        s=h/z;
        f=c*g+s*y;
        x=c*y-s*g;
      }
      b[n1]=f;
      d[n]=x;
    }
  } while (n > 0);
  em[3]=bmax;
  em[5]=count;
  em[7]=rnk;
  return n;
}


public static int qrisngvaldecbid(double d[], double b[], int m, int n,
                  double u[][], double v[][], double em[])
{
  int n0,n1,k,k1,i,i1,count,max,rnk;
  double tol,bmax,z,x,y,g,h,f,c,s,min;

  tol=em[2]*em[1];
  count=0;
  bmax=0.0;
  max=(int) em[4];
  min=em[6];
  rnk=n0=n;
  do {
    k=n;
    n1=n-1;
    while (true) {
      k--;
      if (k <= 0) break;
      if (Math.abs(b[k]) >= tol) {
        if (Math.abs(d[k]) < tol) {
          c=0.0;
          s=1.0;
          for (i=k; i<=n1; i++) {
            f=s*b[i];
            b[i] *= c;
            i1=i+1;
            if (Math.abs(f) < tol) break;
            g=d[i1];
            d[i1]=h=Math.sqrt(f*f+g*g);
            c=g/h;
            s = -f/h;
            Basic.rotcol(1,m,k,i1,u,c,s);
          }
          break;
        }
      } else {
        if (Math.abs(b[k]) > bmax) bmax=Math.abs(b[k]);
        break;
      }
    }
    if (k == n1) {
      if (d[n] < 0.0) {
        d[n] = -d[n];
        for (i=1; i<=n0; i++) v[i][n] = -v[i][n];
      }
      if (d[n] <= min) rnk--;
      n=n1;
    } else {
      count++;
      if (count > max) break;
      k1=k+1;
      z=d[n];
      x=d[k1];
      y=d[n1];
      g = (n1 == 1) ? 0.0 : b[n1-1];
      h=b[n1];
      f=((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y);
      g=Math.sqrt(f*f+1.0);
      f=((x-z)*(x+z)+h*(y/((f < 0.0) ? f-g : f+g)-h))/x;
      c=s=1.0;
      for (i=k1+1; i<=n; i++) {
        i1=i-1;
        g=b[i1];
        y=d[i];
        h=s*g;
        g *= c;
        z=Math.sqrt(f*f+h*h);
        c=f/z;
        s=h/z;
        if (i1 != k1) b[i1-1]=z;
        f=x*c+g*s;
        g=g*c-x*s;
        h=y*s;
        y *= c;
        Basic.rotcol(1,n0,i1,i,v,c,s);
        d[i1]=z=Math.sqrt(f*f+h*h);
        c=f/z;
        s=h/z;
        f=c*g+s*y;
        x=c*y-s*g;
        Basic.rotcol(1,m,i1,i,u,c,s);
      }
      b[n1]=f;
      d[n]=x;
    }
  } while (n > 0);
  em[3]=bmax;
  em[5]=count;
  em[7]=rnk;
  return n;
}


public static int qrisngval(double a[][], int m, int n,
                            double val[], double em[])
{
  int i;
  double b[] = new double[n+1];
  
  hshreabid(a,m,n,val,b,em);
  i=qrisngvalbid(val,b,n,em);
  return i;
}


public static int qrisngvaldec(double a[][], int m, int n,
                  double val[], double v[][], double em[])
{
  int i;
  double b[] = new double[n+1];
  
  hshreabid(a,m,n,val,b,em);
  psttfmmat(a,n,v,b);
  pretfmmat(a,m,n,val);
  i=qrisngvaldecbid(val,b,m,n,a,v,em);
  return i;
}


public static int zerpol(int n, double a[], double em[],
                         double re[], double im[], double d[])
{
  int i,totit,it,fail,start,up,max,giex,itmax,ih,m,split;
  double x,y,newf,oldf,maxrad,ae,tol,h1,h2,ln2,h,side,s1re,s1im,
         s2re,s2im,dx,dy,h3,h4,h5,h6;
  boolean btmp,control;
  int itmp[] = new int[1];
  double f[] = new double[6];
  double tries[] = new double[11];
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];

  oldf=maxrad=0.0;
  totit=it=fail=up=start=max=0;
  ln2=Math.log(2.0);
  newf=Double.MAX_VALUE;
  ae=Double.MIN_VALUE;
  giex=(int) (Math.log(newf)/ln2-40.0);
  tol=em[0];
  itmax=(int) em[1];
  for (i=0; i<=n; i++) d[i]=a[n-i];
  if (n <= 0)
    fail=1;
  else
    if (d[0] == 0.0) fail=2;
  if (fail > 0) {
    em[2]=fail;
    em[3]=start;
    em[4]=totit;
    for (i=(n-1)/2; i>=0; i--) {
      tol=d[i];
      d[i]=d[n-i];
      d[n-i]=tol;
    }
    return n;
  }
  while (d[n] == 0.0 && n > 0) {
    re[n]=im[n]=0.0;
    n--;
  }
  x=y=0.0;
  while (n > 2) {
    /* control */
    if (it > itmax) {
      totit += it;
      fail=3;
      em[2]=fail;
      em[3]=start;
      em[4]=totit;
      for (i=(n-1)/2; i>=0; i--) {
        tol=d[i];
        d[i]=d[n-i];
        d[n-i]=tol;
      }
      return n;
    } else
      if (it == 0) {
        maxrad=0.0;
        max=(int) ((giex-Math.log(Math.abs(d[0]))/ln2)/n);
        for (i=1; i<=n; i++) {
          h1 = (d[i] == 0.0) ? 0.0 : 
                        Math.exp(Math.log(Math.abs(d[i]/d[0]))/i);
          if (h1 > maxrad) maxrad=h1;
        }
        for (i=1; i<=n-1; i++)
          if (d[i] != 0.0) {
            ih=(int) ((giex-Math.log(Math.abs(d[i]))/ln2)/(n-i));
            if (ih < max) max=ih;
          }
        max=max*(int) (ln2/Math.log(n));
        side = -d[1]/d[0];
        side = (Math.abs(side) < tol) ? 0.0 :
              ((side > 0.0) ? 1.0 : -1.0);
        if (side == 0.0) {
          tries[7]=tries[2]=maxrad;
          tries[9] = -maxrad;
          tries[6]=tries[4]=tries[3]=maxrad/Math.sqrt(2.0);
          tries[5] = -tries[3];
          tries[10]=tries[8]=tries[1]=0.0;
        } else {
          tries[8]=tries[4]=maxrad/Math.sqrt(2.0);
          tries[1]=side*maxrad;
          tries[3]=tries[4]*side;
          tries[6]=maxrad;
          tries[7] = -tries[3];
          tries[9] = -tries[1];
          tries[2]=tries[5]=tries[10]=0.0;
        }
        if (Basic.comabs(x,y) > 2.0*maxrad) x=y=0.0;
        control=false;
      } else {
        if (it > 1 && newf >= oldf) {
          up++;
          if (up == 5 && start < 5) {
            start++;
            up=0;
            x=tries[2*start-1];
            y=tries[2*start];
            control=false;
          } else
            control=true;
        } else
          control=true;
      } /* end of control */
    if (control) {
      /* laguerre */
      if (Math.abs(f[0]) > Math.abs(f[1])) {
        h1=f[0];
        h6=f[1]/h1;
        h2=f[2]+h6*f[3];
        h3=f[3]-h6*f[2];
        h4=f[4]+h6*f[5];
        h5=f[5]-h6*f[4];
        h6=h6*f[1]+h1;
      } else {
        h1=f[1];
        h6=f[0]/h1;
        h2=h6*f[2]+f[3];
        h3=h6*f[3]-f[2];
        h4=h6*f[4]+f[5];
        h5=h6*f[5]-f[4];
        h6=h6*f[0]+f[1];
      }
      s1re=h2/h6;
      s1im=h3/h6;
      h2=s1re*s1re-s1im*s1im;
      h3=2.0*s1re*s1im;
      s2re=h2-h4/h6;
      s2im=h3-h5/h6;
      h1=s2re*s2re+s2im*s2im;
      h1 = (h1 != 0.0) ? (s2re*h2+s2im*h3)/h1 : 1.0;
      m = (h1 > n-1) ? ((n > 1) ? n-1 : 1) : ((h1 > 1.0) ? (int)h1 : 1);
      h1=(double)(n-m)/(double) m;
      Basic.comsqrt(h1*(n*s2re-h2),h1*(n*s2im-h3),tmp1,tmp2);
      h2=tmp1[0];
      h3=tmp2[0];
      if (s1re*h2+s1im*h3 < 0.0) {
        h2 = -h2;
        h3 = -h3;
      }
      h2 += s1re;
      h3 += s1im;
      h1=h2*h2+h3*h3;
      if (h1 == 0.0) {
        dx = -n;
        dy=n;
      } else {
        dx = -n*h2/h1;
        dy=n*h3/h1;
      }
      h1=Math.abs(x)*tol+ae;
      h2=Math.abs(y)*tol+ae;
      if (Math.abs(dx) < h1 && Math.abs(dy) < h2) {
        dx = (dx == 0.0) ? h1 : ((dx > 0.0) ? h1 : -h1);
        dy = (dy == 0.0) ? h2 : ((dy > 0.0) ? h2 : -h2);
      }
      x += dx;
      y += dy;
      if (Basic.comabs(x,y) > 2.0*maxrad) {
        h1 = (Math.abs(x) > Math.abs(y)) ? Math.abs(x) : Math.abs(y);
        h2=Math.log(h1)/ln2+1.0-max;
        if (h2 > 0.0) {
          h2=Math.pow(2.0,h2);
          x /= h2;
          y /= h2;
        }
      } /* end of laguerre */
    }
    oldf=newf;
    itmp[0]=it;
    tmp1[0]=newf;
    btmp=zerpolfunction(n,d,f,x,y,tol,itmp,tmp1);
    it=itmp[0];
    newf=tmp1[0];
    if (btmp) {
      if (y != 0.0 && Math.abs(y) < 0.1) {
        h=y;
        y=0.0;
        itmp[0]=it;
        tmp1[0]=newf;
        btmp=zerpolfunction(n,d,f,x,y,tol,itmp,tmp1);
        it=itmp[0];
        newf=tmp1[0];
        if (!btmp) y=h;
      }
      re[n]=x;
      im[n]=y;
      if (y != 0.0) {
        re[n-1]=x;
        im[n-1] = -y;
      }
      /* deflation */
      if (x == 0.0 && y == 0.0)
        n--;
      else {
        double b[] = new double[n];
        if (y == 0.0) {
          n--;
          b[n] = -d[n+1]/x;
          for (i=1; i<=n; i++) b[n-i]=(b[n-i+1]-d[n-i+1])/x;
          for (i=1; i<=n; i++) d[i] += d[i-1]*x;
        } else {
          h1 = -2.0*x;
          h2=x*x+y*y;
          n -= 2;
          b[n]=d[n+2]/h2;
          b[n-1]=(d[n+1]-h1*b[n])/h2;
          for (i=2; i<=n; i++)
            b[n-i]=(d[n-i+2]-h1*b[n-i+1]-b[n-i+2])/h2;
          d[1] -= h1*d[0];
          for (i=2; i<=n; i++) d[i] -= h1*d[i-1]+h2*d[i-2];
        }
        split=n;
        h2=Math.abs(d[n]-b[n])/(Math.abs(d[n])+Math.abs(b[n]));
        for (i=n-1; i>=0; i--) {
          h1=Math.abs(d[i])+Math.abs(b[i]);
          if (h1 > tol) {
            h1=Math.abs(d[i]-b[i])/h1;
            if (h1 < h2) {
              h2=h1;
              split=i;
            }
          }
        }
        for (i=split+1; i<=n; i++) d[i]=b[i];
        d[split]=(d[split]+b[split])/2.0;
      } /* end of deflation */
      totit += it;
      up=start=it=0;
    }
  }
  if (n == 1) {
    re[1] = -d[1]/d[0];
    im[1]=0.0;
  } else {
    h1 = -0.5*d[1]/d[0];
    h2=h1*h1-d[2]/d[0];
    if (h2 >= 0.0) {
      re[2] = (h1 < 0.0) ? h1-Math.sqrt(h2) : h1+Math.sqrt(h2);
      re[1]=d[2]/(d[0]*re[2]);
      im[2]=im[1]=0.0;
    } else {
      re[2]=re[1]=h1;
      im[2]=Math.sqrt(-h2);
      im[1] = -im[2];
    }
  }
  em[2]=fail;
  em[3]=start;
  em[4]=totit;
  return 0;
}


static private boolean zerpolfunction(int n, double d[], double f[],
               double x, double y, double tol, int it[], double newf[])
{
  /* this function is used internally by ZERPOL */

  int k,m1,m2;
  double p,q,qsqrt,f01,f02,f03,f11,f12,f13,f21,f22,f23,stop;

  (it[0])++;
  p=2.0*x;
  q = -(x*x+y*y);
  qsqrt=Math.sqrt(-q);
  f01=f11=f21=d[0];
  f02=f12=f22=0.0;
  m1=n-4;
  m2=n-2;
  stop=Math.abs(f01)*0.8;
  for (k=1; k<=m1; k++) {
    f03=f02;
    f02=f01;
    f01=d[k]+p*f02+q*f03;
    f13=f12;
    f12=f11;
    f11=f01+p*f12+q*f13;
    f23=f22;
    f22=f21;
    f21=f11+p*f22+q*f23;
    stop=qsqrt*stop+Math.abs(f01);
  }
  if (m1 < 0) m1=0;
  for (k=m1+1; k<=m2; k++) {
    f03=f02;
    f02=f01;
    f01=d[k]+p*f02+q*f03;
    f13=f12;
    f12=f11;
    f11=f01+p*f12+q*f13;
    stop=qsqrt*stop+Math.abs(f01);
  }
  if (n == 3) f21=0.0;
  f03=f02;
  f02=f01;
  f01=d[n-1]+p*f02+q*f03;
  f[0]=d[n]+x*f01+q*f02;
  f[1]=y*f01;
  f[2]=f01-2.0*f12*y*y;
  f[3]=2.0*y*(-x*f12+f11);
  f[4]=2.0*(-x*f12+f11)-8.0*y*y*(-x*f22+f21);
  f[5]=y*(6.0*f12-8.0*y*y*f22);
  stop=qsqrt*(qsqrt*stop+Math.abs(f01))+Math.abs(f[0]);
  newf[0]=f02=Basic.comabs(f[0],f[1]);
  return (f02 < (2.0*Math.abs(x*f01)-
                 8.0*(Math.abs(f[0])+Math.abs(f01)*qsqrt)+
                 10.0*stop)*tol*Math.pow(1.0+tol,4*n+3.0));
}


public static void bounds(int n, double a[], double re[], double im[],
                          double rele, double abse, double recentre[],
                          double imcentre[], double bound[])
{
  boolean goon;
  int i,j,k,index1,index2,place,clustin;
  double h,min,recent,imcent,xk,yk,zk,corr,boundin,temp1,temp2;
  double rc[] = new double[n+1];
  double c[] = new double[n+1];
  double rce[] = new double[n+1];
  double clust[] = new double[n+1];

  rc[0]=c[0]=a[n];
  rce[0]=Math.abs(c[0]);
  k=0;
  for (i=1; i<=n; i++) {
    rc[i]=rce[i]=0.0;
    c[i]=a[n-i];
  }
  while (k < n) {
    k++;
    xk=re[k];
    yk=im[k];
    zk=xk*xk+yk*yk;
    for (j=k; j>=1; j--) rce[j] += rce[j-1]*Math.sqrt(zk);
    if (yk == 0.0)
      for (j=k; j>=1; j--) rc[j] -= xk*rc[j-1];
    else {
      k++;
      if (k <= n && xk == re[k] && yk == -im[k]) {
        xk = -2.0*xk;
        for (j=k; j>=1; j--) rce[j] += rce[j-1]*Math.sqrt(zk);
        for (j=k; j>=2; j--) rc[j] += xk*rc[j-1]+zk*rc[j-2];
        rc[1] += xk*rc[0];
      }
    }
  }
  rc[0]=rce[0];
  corr=1.06*Double.MIN_VALUE;
  for (i=1; i<=n-1; i++)
    rc[i]=Math.abs(rc[i]-c[i])+rce[i]*corr*(n+i-2)+
          rele*Math.abs(c[i])+abse;
  rc[n]=Math.abs(rc[n]-c[n])+rce[n]*corr*(n-1)+rele*Math.abs(c[n])+abse;
  for (i=1; i<=n; i++)
    kcluster(1,i,n,rc,re,im,recentre,imcentre,bound,clust);
  goon=true;
  while (goon) {
    index1=index2=0;
    min=Double.MAX_VALUE;
    i=n-(int)(clust[n])+1;
    while (i >= 2) {
      j=i;
      recent=recentre[i];
      imcent=imcentre[i];
      while (j >= 2) {
        j -= clust[j-1];
        temp1=recent-recentre[j];
        temp2=imcent-imcentre[j];
        h=Math.sqrt(temp1*temp1+temp2*temp2);
        if (h < bound[i]+bound[j] && h <= min) {
          index1=j;
          index2=i;
          min=h;
        }
      }
      i -= clust[i-1];
    }
    if (index1 == 0)
      goon=false;
    else {
      if (imcentre[index1] == 0.0) {
        if (imcentre[index2] != 0.0) clust[index2] *= 2.0;
      }
      else
        if (imcentre[index2] == 0.0) clust[index1] *= 2.0;
      k=index1+(int)(clust[index1]);
      if (k != index2) {
        /*  shift */
        double wa1[] = new double[(int)(clust[index2])+1];
        double wa2[] = new double[(int)(clust[index2])+1];
        clustin=(int) clust[index2];
        boundin=bound[index2];
        imcent=imcentre[index2];
        recent=recentre[index2];
        for (j=1; j<=clustin; j++) {
          place=index2+j-1;
          wa1[j]=re[place];
          wa2[j]=im[place];
        }
        for (j=index2-1; j>=k; j--) {
          place=j+clustin;
          re[place]=re[j];
          im[place]=im[j];
          clust[place]=clust[j];
          bound[place]=bound[j];
          recentre[place]=recentre[j];
          imcentre[place]=imcentre[j];
        }
        for (j=k+clustin-1; j>=k; j--) {
          place=j+1-k;
          re[j]=wa1[place];
          im[j]=wa2[place];
          bound[j]=boundin;
          clust[j]=clustin;
          recentre[j]=recent;
          imcentre[j]=imcent;
        }
      } /* end of shift */
      k=(int) (clust[index1]+clust[k]);
      kcluster(k,index1,n,rc,re,im,recentre,imcentre,bound,clust);
    }
  }
}


static private void kcluster(int k, int m, int n, double rc[],
                    double re[], double im[], double recentre[],
                    double imcentre[], double bound[], double clust[])
{
  /* this function is used internally by BOUNDS */

  boolean nonzero;
  int i,stop,l;
  double recent,imcent,d,prod,rad,gr,r,s,h1,h2,temp1,temp2;
  double dist[] = new double[m+k];
  
  recent=re[m];
  imcent=im[m];
  stop=m+k-1;
  l = (imcent == 0.0) ? 0 : ((imcent > 0.0) ? 1 : -1);
  nonzero = (l != 0);
  for (i=m+1; i<=stop; i++) {
    recent += re[i];
    if (nonzero) {
      nonzero=(l == ((im[i] == 0.0) ? 0 : ((im[i]>0.0) ? 1 : -1)));
      imcent += im[i];
    }
  }
  recent /= k;
  imcent = (nonzero ? imcent/k : 0.0);
  d=0.0;
  rad=0.0;
  for (i=m; i<=stop; i++) {
    recentre[i]=recent;
    imcentre[i]=imcent;
    temp1=re[i]-recent;
    temp2=im[i]-imcent;
    dist[i]=Math.sqrt(temp1*temp1+temp2*temp2);
    if (d < dist[i]) d=dist[i];
  }
  s=Math.sqrt(recent*recent+imcent*imcent);
  h1=rc[1];
  h2=rc[0];
  for (i=2; i<=n; i++) h1=h1*s+rc[i];
  for (i=1; i<=m-1; i++) {
    temp1=re[i]-recent;
    temp2=im[i]-imcent;
    h2 *= Math.abs(Math.sqrt(temp1*temp1+temp2*temp2));
  }
  for (i=m+k; i<=n; i++) {
    temp1=re[i]-recent;
    temp2=im[i]-imcent;
    h2 *= Math.abs(Math.sqrt(temp1*temp1+temp2*temp2));
  }
  gr=Math.abs((h1 == 0.0) ? 0.0 : ((h2 == 0.0) ? 10.0 : h1/h2));
  if (gr > 0.0)
    do {
      r=rad;
      rad=d+Math.exp(Math.log(1.1*gr)/k);
      if (rad == r) rad *= Math.exp(Math.log(1.1)/k);
      s=Math.sqrt(recent*recent+imcent*imcent)+rad;
      h1=rc[1];
      h2=rc[0];
      for (i=2; i<=n; i++) h1=h1*s+rc[i];
      for (i=1; i<=m-1; i++) {
        temp1=re[i]-recent;
        temp2=im[i]-imcent;
        h2 *= Math.abs(Math.sqrt(temp1*temp1+temp2*temp2)-rad);
      }
      for (i=m+k; i<=n; i++) {
        temp1=re[i]-recent;
        temp2=im[i]-imcent;
        h2 *= Math.abs(Math.sqrt(temp1*temp1+temp2*temp2)-rad);
      }
      gr=(h1 == 0.0) ? 0.0 : ((h2 == 0.0) ? -10.0 : h1/h2);
      prod=1.0;
      for (i=m; i<=stop; i++) prod *= (rad-dist[i]);
    } while (prod <= gr);
  for (i=m; i<=stop; i++) {
    bound[i]=rad;
    clust[i]=k;
  }
}


public static void allzerortpol(int n, double b[], double c[],
                                double zer[], double em[])
{
  int i;
  double nrm;
  double bb[] = new double[n+1];
  
  nrm=Math.abs(b[0]);
  for (i=1; i<=n-2; i++)
    if (c[i]+Math.abs(b[i]) > nrm) nrm=c[i]+Math.abs(b[i]);
  if (n > 1)
    nrm = (nrm+1 >= c[n-1]+Math.abs(b[n-1])) ? nrm+1.0 :
          (c[n-1]+Math.abs(b[n-1]));
  em[1]=nrm;
  for (i=n; i>=1; i--) zer[i]=b[i-1];
  Basic.dupvec(1,n-1,0,bb,c);
  qrivalsymtri(zer,bb,n,em);
}


public static void lupzerortpol(int n, int m, double b[], double c[],
                                double zer[], double em[])
{
  boolean posdef,converge;
  int i,j,k,t;
  double nrm,dlam,eps,delta,e,ep,err,p,q,qp,r,s,tot;
  int itmp[] = new int[1];

  qp=0.0;
  nrm=Math.abs(b[0]);
  for (i=1; i<=n-2; i++)
    if (c[i]+Math.abs(b[i]) > nrm) nrm=c[i]+Math.abs(b[i]);
  if (n > 1)
    nrm = (nrm+1 >= c[n-1]+Math.abs(b[n-1])) ? nrm+1.0 :
          (c[n-1]+Math.abs(b[n-1]));
  em[1]=nrm;
  for (i=n; i>=1; i--) b[i]=b[i-1];
  for (i=n; i>=2; i--) c[i]=c[i-1];
  posdef = (em[6] == 1.0);
  dlam=em[2];
  eps=em[0];
  c[1]=err=q=s=0.0;
  tot=b[1];
  for (i=n; i>=1; i--) {
    p=q;
    q=Math.sqrt(c[i]);
    e=b[i]-p-q;
    if (e < tot) tot=e;
  }
  if (posdef && (tot < 0.0))
    tot=0.0;
  else
    for(i=1; i<=n; i++) b[i] -= tot;
  t=0;
  for (k=1; k<=m; k++) {
    converge=false;
    /* next qr transformation */
    do {
      t++;
      tot += s;
      delta=b[n]-s;
      i=n;
      e=Math.abs(eps*tot);
      if (dlam < e) dlam=e;
      if (delta <= dlam) {
        converge=true;
        break;
      }
      e=c[n]/delta;
      qp=delta+e;
      p=1.0;
      for (i=n-1; i>=k; i--) {
        q=b[i]-s-e;
        r=q/qp;
        p=p*r+1.0;
        ep=e*r;
        b[i+1]=qp+ep;
        delta=q-ep;
        if (delta <= dlam) {
          converge=true;
          break;
        }
        e=c[i]/q;
        qp=delta+e;
        c[i+1]=qp*ep;
      }
      if (converge) break;
      b[k]=qp;
      s=qp/p;
    } while (tot+s > tot);  /* end of qr transformation */
    if (!converge) {
      /* irregular end of iteration,
        deflate minimum diagonal element */
      s=0.0;
      i=k;
      delta=qp;
      for (j=k+1; j<=n; j++)
        if (b[j] < delta) {
          i=j;
          delta=b[j];
        }
    }
    /* convergence */
    if (i < n) c[i+1]=c[i]*e/qp;
    for (j=i-1; j>=k; j--) {
      b[j+1]=b[j]-s;
      c[j+1]=c[j];
    }
    b[k]=tot;
    c[k] = err += Math.abs(delta);
  }
  em[5]=t;
  em[3]=Basic.infnrmvec(1,m,itmp,c);
  Basic.dupvec(1,m,0,zer,b);
}


public static void selzerortpol(int n, int n1, int n2, double b[],
                                double c[], double zer[], double em[])
{
  int i;
  double nrm;
  double d[] =new double[n+1];
  
  nrm=Math.abs(b[0]);
  for (i=n-2; i>=1; i--)
    if (c[i]+Math.abs(b[i]) > nrm) nrm=c[i]+Math.abs(b[i]);
  if (n > 1)
    nrm = (nrm+1 >= c[n-1]+Math.abs(b[n-1])) ? nrm+1.0 :
          (c[n-1]+Math.abs(b[n-1]));
  em[1]=nrm;
  for (i=n; i>=1; i--) d[i]=b[i-1];
  valsymtri(d,c,n,n1,n2,zer,em);
  em[5]=em[3];
}


public static void alljaczer(int n, double alfa,
                             double beta, double zer[])
{
  int i,m;
  double sum,min,gamma,zeri;
  double em[] = new double[6];

  if (alfa == beta) {
    double a[] = new double[n/2+1];
    double b[] = new double[n/2+1];
    m=n/2;
    if (n != 2*m) {
      gamma=0.5;
      zer[m+1]=0.0;
    } else
      gamma = -0.5;
    min=0.25-alfa*alfa;
    sum=alfa+gamma+2.0;
    a[0]=(gamma-alfa)/sum;
    a[1]=min/sum/(sum+2.0);
    b[1]=4.0*(1.0+alfa)*(1.0+gamma)/sum/sum/(sum+1.0);
    for (i=2; i<=m-1; i++) {
      sum=i+i+alfa+gamma;
      a[i]=min/sum/(sum+2.0);
      sum *= sum;
      b[i]=4.0*i*(i+alfa+gamma)*(i+alfa)*(i+gamma)/sum/(sum-1.0);
    }
    em[0]=Double.MIN_VALUE;
    em[2]=Double.MIN_VALUE;
    em[4]=6*m;
    allzerortpol(m,a,b,zer,em);
    for (i=1; i<=m; i++) {
      zer[i] = zeri = -Math.sqrt((1.0+zer[i])/2.0);
      zer[n+1-i] = -zeri;
    }
  } else {
    double a[] = new double[n+1];
    double b[] = new double[n+1];
    min=(beta-alfa)*(beta+alfa);
    sum=alfa+beta+2.0;
    b[0]=0.0;
    a[0]=(beta-alfa)/sum;
    a[1]=min/sum/(sum+2.0);
    b[1]=4.0*(1.0+alfa)*(1.0+beta)/sum/sum/(sum+1.0);
    for (i=2; i<=n-1; i++) {
      sum=i+i+alfa+beta;
      a[i]=min/sum/(sum+2.0);
      sum *= sum;
      b[i]=4.0*i*(i+alfa+beta)*(i+alfa)*(i+beta)/(sum-1.0)/sum;
    }
    em[0]=Double.MIN_VALUE;
    em[2]=Double.MIN_VALUE;
    em[4]=6*n;
    allzerortpol(n,a,b,zer,em);
  }
}


public static void alllagzer(int n, double alfa, double zer[])
{
  int i;
  double em[] = new double[6];
  double a[] = new double[n+1];
  double b[] = new double[n+1];
  
  b[0]=0.0;
  a[n-1]=n+n+alfa-1.0;
  for (i=1; i<=n-1; i++) {
    a[i-1]=i+i+alfa-1.0;
    b[i]=i*(i+alfa);
  }
  em[0]=Double.MIN_VALUE;
  em[2]=Double.MIN_VALUE;
  em[4]=6*n;
  allzerortpol(n,a,b,zer,em);
}


public static void comkwd(double pr, double pi, double qr, double qi,
                  double gr[], double gi[], double kr[], double ki[])
{
  double tmp1,tmp2;
  double hr[] = new double[1];
  double hi[] = new double[1];

  if (qr == 0.0 && qi == 0.0) {
    kr[0] = ki[0] = 0.0;
    gr[0] = pr*2.0;
    gi[0] = pi*2.0;
    return;
  }
  if (pr == 0.0 && pi == 0.0) {
    Basic.comsqrt(qr,qi,gr,gi);
    kr[0] = -gr[0];
    ki[0] = -gi[0];
    return;
  }
  if (Math.abs(pr) > 1.0 || Math.abs(pi) > 1.0) {
    Basic.comdiv(qr,qi,pr,pi,hr,hi);
    Basic.comdiv(hr[0],hi[0],pr,pi,hr,hi);
    Basic.comsqrt(1.0+hr[0],hi[0],hr,hi);
    Basic.commul(pr,pi,hr[0]+1.0,hi[0],gr,gi);
  } else {
    Basic.comsqrt(qr+(pr+pi)*(pr-pi),qi+pr*pi*2.0,hr,hi);
    if (pr*hr[0]+pi*hi[0] > 0.0) {
      gr[0] = pr+hr[0];
      gi[0] = pi+hi[0];
    } else {
      gr[0] = pr-hr[0];
      gi[0] = pi-hi[0];
    }
  }
  tmp1=gr[0];
  tmp2=gi[0];
  Basic.comdiv(-qr,-qi,tmp1,tmp2,kr,ki);
}

}