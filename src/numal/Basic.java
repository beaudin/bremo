package numal;

//import numal.*;

public class Basic extends Object {


static final int BASE = 100;


public static void inivec(int l, int u, double a[], double x)
{
  for (; l<=u; l++) a[l]=x;
}


public static void inimat(int lr, int ur, int lc, int uc,
                          double a[][], double x)
{
  int j;

  for (; lr<=ur; lr++)
    for (j=lc; j<=uc; j++) a[lr][j]=x;
}


public static void inimatd(int lr, int ur, int shift,
                           double a[][], double x)
{
  for (; lr<=ur; lr++) a[lr][lr+shift]=x;
}


public static void inisymd(int lr, int ur, int shift,
                           double a[], double x)
{
  shift=Math.abs(shift);
  ur += shift+1;
  shift += lr;
  lr += ((shift-3)*shift)/2;
  lr += shift;
  while (shift < ur) {
    a[lr]=x;
    shift++;
    lr += shift;
  }
}


public static void inisymrow(int l, int u, int i, double a[], double x)
{
  int k;

  if (l <= i) {
    k=((i-1)*i)/2;
    l += k;
    k += (u<i) ? u : i;
    for (; l<=k; l++) a[l]=x;
    l=i+1;
  }
  if (u > i) {
    k=((l-1)*l)/2+i;
    do {
      a[k]=x;
      l++;
      k += l-1;
    } while (l <= u);
  }
}


public static void dupvec(int l, int u, int shift, double a[], double b[])
{
  for (; l<=u; l++) a[l]=b[l+shift];
}


public static void dupvecrow(int l, int u, int i, double a[], double b[][])
{
  for (; l<=u; l++) a[l]=b[i][l];
}


public static void duprowvec(int l, int u, int i, double a[][], double b[])
{
  for (; l<=u; l++) a[i][l]=b[l];
}


public static void dupveccol(int l, int u, int j, double a[], double b[][])
{
  for (; l<=u; l++) a[l]=b[l][j];
}


public static void dupcolvec(int l, int u, int j, double a[][], double b[])
{
  for (; l<=u; l++) a[l][j]=b[l];
}


public static void dupmat(int l, int u, int i, int j,
                          double a[][], double b[][])
{
  int k;

  for (; l<=u; l++)
    for (k=i; k<=j; k++) a[l][k]=b[l][k];
}


public static void mulvec(int l, int u, int shift,
                          double a[], double b[], double x)
{
  for (; l<=u; l++) a[l]=b[l+shift]*x;
}


public static void mulrow(int l, int u, int i, int j,
                          double a[][], double b[][], double x)
{
  for (; l<=u; l++) a[i][l]=b[j][l]*x;
}


public static void mulcol(int l, int u, int i, int j, double a[][],
                          double b[][], double x)
{
  for (; l<=u; l++) a[l][i]=b[l][j]*x;
}


public static void colcst(int l, int u, int j, double a[][], double x)
{
  for (; l<=u; l++) a[l][j] *= x;
}


public static void rowcst(int l, int u, int i, double a[][], double x)
{
  for (; l<=u; l++) a[i][l] *= x;
}


public static double vecvec(int l, int u, int shift, double a[], double b[])
{
  int k;
  double s;

  s=0.0;
  for (k=l; k<=u; k++) s += a[k]*b[k+shift];
  return (s);
}


public static double matvec(int l, int u, int i, double a[][], double b[])
{
  int k;
  double s;

  s=0.0;
  for (k=l; k<=u; k++) s += a[i][k]*b[k];
  return (s);
}


public static double tamvec(int l, int u, int i, double a[][], double b[])
{
  int k;
  double s;

  s=0.0;
  for (k=l; k<=u; k++) s += a[k][i]*b[k];
  return (s);
}


public static double matmat(int l, int u, int i, int j,
                            double a[][], double b[][])
{
  int k;
  double s;

  s=0.0;
  for (k=l; k<=u; k++) s += a[i][k]*b[k][j];
  return (s);
}


public static double tammat(int l, int u, int i, int j,
                            double a[][], double b[][])
{
  int k;
  double s;

  s=0.0;
  for (k=l; k<=u; k++) s += a[k][i]*b[k][j];
  return (s);
}


public static double mattam(int l, int u, int i, int j,
                            double a[][], double b[][])
{
  int k;
  double s;

  s=0.0;
  for (k=l; k<=u; k++) s += a[i][k]*b[j][k];
  return (s);
}


public static double seqvec(int l, int u, int il, int shift,
                            double a[], double b[])
{
  double s;

  s=0.0;
  for (; l<=u; l++) {
    s += a[il]*b[l+shift];
    il += l;
  }
  return (s);
}


public static double scaprd1(int la, int sa, int lb, int sb, int n,
                             double a[], double b[])
{
  int k;
  double s;

  s=0.0;
  for (k=1; k<=n; k++) {
    s += a[la]*b[lb];
    la += sa;
    lb += sb;
  }
  return (s);
}


public static double symmatvec(int l, int u, int i, double a[], double b[])
{
  int k, m;

  m=(l>i) ? l : i;
  k=(m*(m-1))/2;
  return (vecvec(l, (i<=u) ? i-1 : u, k,b,a) + seqvec(m,u,k+i,0,a,b));
}


public static void fulmatvec(int lr, int ur, int lc, int uc,
                             double a[][], double b[], double c[])
{
  for (; lr<=ur; lr++) c[lr]=matvec(lc,uc,lr,a,b);
}


public static void fultamvec(int lr, int ur, int lc, int uc,
                             double a[][], double b[], double c[])
{
  for (; lc<=uc; lc++) c[lc]=tamvec(lr,ur,lc,a,b);
}


public static void fulsymmatvec(int lr, int ur, int lc, int uc,
                                double a[], double b[], double c[])
{
  for (; lr<=ur; lr++) c[lr]=symmatvec(lc,uc,lr,a,b);
}


public static void resvec(int lr, int ur, int lc, int uc, double a[][],
                          double b[], double c[], double x)
{
  for (; lr<=ur; lr++) c[lr]=matvec(lc,uc,lr,a,b)+c[lr]*x;
}


public static void symresvec(int lr, int ur, int lc, int uc, double a[],
                             double b[], double c[], double x)
{
  for (; lr<=ur; lr++) c[lr]=symmatvec(lc,uc,lr,a,b)+c[lr]*x;
}


public static void hshvecmat(int lr, int ur, int lc, int uc,
                             double x, double u[], double a[][])
{
  for (; lc<=uc; lc++) elmcolvec(lr,ur,lc,a,u,tamvec(lr,ur,lc,a,u)*x);
}


public static void hshcolmat(int lr, int ur, int lc, int uc, int i,
                             double x, double u[][], double a[][])
{
  for (; lc<=uc; lc++) elmcol(lr,ur,lc,i,a,u,tammat(lr,ur,lc,i,a,u)*x);
}


public static void hshrowmat(int lr, int ur, int lc, int uc, int i,
                             double x, double u[][], double a[][])
{
  for (; lc<=uc; lc++) elmcolrow(lr,ur,lc,i,a,u,matmat(lr,ur,i,lc,u,a)*x);
}


public static void hshvectam(int lr, int ur, int lc, int uc,
                             double x, double u[], double a[][])
{
  for (; lr<=ur; lr++) elmrowvec(lc,uc,lr,a,u,matvec(lc,uc,lr,a,u)*x);
}


public static void hshcoltam(int lr, int ur, int lc, int uc, int i,
                             double x, double u[][], double a[][])
{
  for (; lr<=ur; lr++) elmrowcol(lc,uc,lr,i,a,u,matmat(lc,uc,lr,i,a,u)*x);
}


public static void hshrowtam(int lr, int ur, int lc, int uc, int i,
                             double x, double u[][], double a[][])
{
  for (; lr<=ur; lr++) elmrow(lc,uc,lr,i,a,u,mattam(lc,uc,lr,i,a,u)*x);
}


public static void elmvec(int l, int u, int shift, double a[],
                          double b[], double x)
{
  for (; l<=u; l++) a[l] += b[l+shift]*x;
}


public static void elmcol(int l, int u, int i, int j, double a[][], 
		                      double b[][], double x)
{
	for (; l<=u; l++) a[l][i] += b[l][j]*x;
}


public static void elmrow(int l, int u, int i, int j, double a[][],
                          double b[][], double x)
{
  for (; l<=u; l++) a[i][l] += b[j][l]*x;
}


public static void elmveccol(int l, int u, int i, double a[],
                             double b[][], double x)
{
  for (; l<=u; l++) a[l] += b[l][i]*x;
}


public static void elmcolvec(int l, int u, int i, double a[][],
                             double b[], double x)
{
  for (; l<=u; l++) a[l][i] += b[l]*x;
}


public static void elmvecrow(int l, int u, int i, double a[],
                             double b[][], double x)
{
  for (; l<=u; l++) a[l] += b[i][l]*x;
}


public static void elmrowvec(int l, int u, int i, double a[][],
                             double b[], double x)
{
  for (; l<=u; l++) a[i][l] += b[l]*x;
}


public static void elmcolrow(int l, int u, int i, int j,
                             double a[][], double b[][], double x)
{
  for (; l<=u; l++) a[l][i] += b[j][l]*x;
}


public static void elmrowcol(int l, int u, int i, int j, double a[][],
                             double b[][], double x)
{
  for (; l<=u; l++) a[i][l] += b[l][j]*x;
}


public static int maxelmrow(int l, int u, int i, int j, double a[][],
                            double b[][], double x)
{
  int k;
  double r, s;

  s=0.0;
  for (k=l; k<=u; k++) {
    r=(a[i][k] += b[j][k]*x);
    if (Math.abs(r) > s) {
      s=Math.abs(r);
      l=k;
    }
  }
  return (l);
}


public static void ichvec(int l, int u, int shift, double a[])
{
  double r;

  for (; l<=u; l++) {
    r=a[l];
    a[l]=a[l+shift];
    a[l+shift]=r;
  }
}


public static void ichcol(int l, int u, int i, int j, double a[][])
{
  double r;

  for (; l<=u; l++) {
    r=a[l][i];
    a[l][i]=a[l][j];
    a[l][j]=r;
  }
}


public static void ichrow(int l, int u, int i, int j, double a[][])
{
  double r;

  for (; l<=u; l++) {
    r=a[i][l];
    a[i][l]=a[j][l];
    a[j][l]=r;
  }
}


public static void ichrowcol(int l, int u, int i, int j, double a[][])
{
  double r;

  for (; l<=u; l++) {
    r=a[i][l];
    a[i][l]=a[l][j];
    a[l][j]=r;
  }
}


public static void ichseqvec(int l, int u, int il, int shift, double a[])
{
  double r;

  for (; l<=u; l++) {
    r=a[il];
    a[il]=a[l+shift];
    a[l+shift]=r;
    il += l;
  }
}


public static void ichseq(int l, int u, int il, int shift, double a[])
{
  double r;

  for (; l<=u; l++) {
    r=a[il];
    a[il]=a[il+shift];
    a[il+shift]=r;
    il += l;
  }
}


public static void rotcol(int l, int u, int i, int j, double a[][],
                          double c, double s)
{
  double x, y;

  for (; l<=u; l++) {
    x=a[l][i];
    y=a[l][j];
    a[l][i]=x*c+y*s;
    a[l][j]=y*c-x*s;
  }
}


public static void rotrow(int l, int u, int i, int j, double a[][],
                          double c, double s)
{
  double x, y;

  for (; l<=u; l++) {
    x=a[i][l];
    y=a[j][l];
    a[i][l]=x*c+y*s;
    a[j][l]=y*c-x*s;
  }
}


public static double infnrmvec(int l, int u, int k[], double a[])
{
  double r, max;

  max=0.0;
  k[0]=l;
  for (; l<=u; l++) {
    r=Math.abs(a[l]);
    if (r > max) {
      max=r;
      k[0]=l;
    }
  }
  return (max);
}


public static double infnrmrow(int l, int u, int i, int k[], double a[][])
{
  double r, max;

  max=0.0;
  k[0]=l;
  for (; l<=u; l++) {
    r=Math.abs(a[i][l]);
    if (r > max) {
      max=r;
      k[0]=l;
    }
  }
  return (max);
}


public static double infnrmcol(int l, int u, int j, int k[], double a[][])
{
  double r, max;

  max=0.0;
  k[0]=l;
  for (; l<=u; l++) {
    r=Math.abs(a[l][j]);
    if (r > max) {
      max=r;
      k[0]=l;
    }
  }
  return (max);
}


public static double infnrmmat(int lr, int ur, int lc, int uc,
                               int kr[], double a[][])
{
  double r, max;

  max=0.0;
  kr[0]=lr;
  for (; lr<=ur; lr++) {
    r=onenrmrow(lc,uc,lr,a);
    if (r > max) {
      max=r;
      kr[0]=lr;
    }
  }
  return (max);
}


public static double onenrmvec(int l, int u, double a[])
{
  double sum;

  sum=0.0;
  for (; l<=u; l++) sum += Math.abs(a[l]);
  return (sum);
}


public static double onenrmrow(int l, int u, int i, double a[][])
{
  double sum;

  sum=0.0;
  for (; l<=u; l++) sum += Math.abs(a[i][l]);
  return (sum);
}


public static double onenrmcol(int l, int u, int j, double a[][])
{
  double sum;

  sum=0.0;
  for (; l<=u; l++) sum += Math.abs(a[l][j]);
  return (sum);
}


public static double onenrmmat(int lr, int ur, int lc, int uc,
                               int kc[], double a[][])
{
  double r, max;

  max=0.0;
  kc[0]=lc;
  for (; lc<=uc; lc++) {
    r=onenrmcol(lr,ur,lc,a);
    if (r > max) {
      max=r;
      kc[0]=lc;
    }
  }
  return (max);
}


public static double absmaxmat(int lr, int ur, int lc, int uc,
                               int i[], int j[], double a[][])
{
  int ii[] = new int[1];
  double r, max;

  max=0.0;
  i[0]=lr;
  j[0]=lc;
  for (; lc<=uc; lc++) {
    r=infnrmcol(lr,ur,lc,ii,a);
    if (r > max) {
      max=r;
      i[0]=ii[0];
      j[0]=lc;
    }
  }
  return (max);
}


public static void reascl(double a[][], int n, int n1, int n2)
{
  int i, j;
  double s;

  for (j=n1; j<=n2; j++) {
    s=0.0;
    for (i=1; i<=n; i++)
      if (Math.abs(a[i][j]) > Math.abs(s)) s=a[i][j];
    if (s != 0.0)
      for (i=1; i<=n; i++) a[i][j] /= s;
  }
}


public static void comcolcst(int l, int u, int j, double ar[][],
                             double ai[][], double xr, double xi)
{
  double br[] = new double[1];
  double bi[] = new double[1];

  for (; l<=u; l++) {
    commul(ar[l][j],ai[l][j],xr,xi,br,bi);
    ar[l][j] = br[0];
    ai[l][j] = bi[0];
  }
}


public static void comrowcst(int l, int u, int i, double ar[][],
                             double ai[][], double xr, double xi)
{
  double br[] = new double[1];
  double bi[] = new double[1];  
  
  for (; l<=u; l++) {
    commul(ar[i][l],ai[i][l],xr,xi,br,bi);
    ar[i][l] = br[0];
    ai[i][l] = bi[0];
  }
}


public static void commatvec(int l, int u, int i, double ar[][],
       double ai[][], double br[], double bi[], double rr[], double ri[])
{
  double mv;

  mv=matvec(l,u,i,ar,br)-matvec(l,u,i,ai,bi);
  ri[0]=matvec(l,u,i,ai,br)+matvec(l,u,i,ar,bi);
  rr[0]=mv;
}


public static boolean hshcomcol(int l, int u, int j, double ar[][],
                                double ai[][], double tol, double k[],
                                double c[], double s[], double t[])
{
  double vr, h, arlj, ailj;
  double mod[] = new double[1];

  vr=tammat(l+1,u,j,j,ar,ar)+tammat(l+1,u,j,j,ai,ai);
  arlj=ar[l][j];
  ailj=ai[l][j];
  carpol(arlj,ailj,mod,c,s);
  if (vr > tol) {
    vr += arlj*arlj+ailj*ailj;
    h = k[0] = Math.sqrt(vr);
    t[0]=vr+mod[0]*h;
    if (arlj == 0.0 && ailj == 0.0)
      ar[l][j]=h;
    else {
      ar[l][j]=arlj + c[0] * k[0];
      ai[l][j]=ailj + s[0] * k[0];
      s[0] = - s[0];
    }
    c[0] = - c[0];
    return (true);
  } else {
    k[0]=mod[0];
    t[0] = -1.0;
    return (false);
  }
}


public static void hshcomprd(int i, int ii, int l, int u, int j,
     double ar[][], double ai[][], double br[][], double bi[][], double t)
{
  for (; l<=u; l++)
    elmcomcol(i,ii,l,j,ar,ai,br,bi,
          (-tammat(i,ii,j,l,br,ar)-tammat(i,ii,j,l,bi,ai))/t,
          (tammat(i,ii,j,l,bi,ar)-tammat(i,ii,j,l,br,ai))/t);
}


public static void elmcomveccol(int l, int u, int j, double ar[],
        double ai[], double br[][], double bi[][], double xr, double xi)
{
  elmveccol(l,u,j,ar,br,xr);
  elmveccol(l,u,j,ar,bi,-xi);
  elmveccol(l,u,j,ai,br,xi);
  elmveccol(l,u,j,ai,bi,xr);
}


public static void elmcomcol(int l, int u, int i, int j, double ar[][], 
		double ai[][], double br[][], double bi[][], double xr, double xi)
{
	elmcol(l,u,i,j,ar,br,xr);
	elmcol(l,u,i,j,ar,bi,-xi);
	elmcol(l,u,i,j,ai,br,xi);
	elmcol(l,u,i,j,ai,bi,xr);
}


public static void elmcomrowvec(int l, int u, int i, double ar[][],
        double ai[][], double br[], double bi[], double xr, double xi)
{
  elmrowvec(l,u,i,ar,br,xr);
  elmrowvec(l,u,i,ar,bi,-xi);
  elmrowvec(l,u,i,ai,br,xi);
  elmrowvec(l,u,i,ai,bi,xr);
}


public static void rotcomcol(int l, int u, int i, int j, double ar[][],
                             double ai[][], double cr, double ci, double s)
{
  double arli,aili,arlj,ailj;

  for (; l<=u; l++) {
    arli=ar[l][i];
    aili=ai[l][i];
    arlj=ar[l][j];
    ailj=ai[l][j];
    ar[l][i]=cr*arli+ci*aili-s*arlj;
    ai[l][i]=cr*aili-ci*arli-s*ailj;
    ar[l][j]=cr*arlj-ci*ailj+s*arli;
    ai[l][j]=cr*ailj+ci*arlj+s*aili;
  }
}


public static void rotcomrow(int l, int u, int i, int j, double ar[][],
          double ai[][], double cr, double ci, double s)
{
  double aril,aiil,arjl,aijl;

  for (; l<=u; l++) {
    aril=ar[i][l];
    aiil=ai[i][l];
    arjl=ar[j][l];
    aijl=ai[j][l];
    ar[i][l]=cr*aril+ci*aiil+s*arjl;
    ai[i][l]=cr*aiil-ci*aril+s*aijl;
    ar[j][l]=cr*arjl-ci*aijl-s*aril;
    ai[j][l]=cr*aijl+ci*arjl-s*aiil;
  }
}


public static void chsh2(double a1r, double a1i, double a2r, double a2i,
                         double c[], double sr[], double si[])
{
  double r;

  if (a2r != 0.0 || a2i != 0.0) {
    if (a1r != 0.0 || a1i != 0.0) {
      r=Math.sqrt(a1r*a1r+a1i*a1i);
      c[0]=r;
      sr[0]=(a1r*a2r+a1i*a2i)/r;
      si[0]=(a1r*a2i-a1i*a2r)/r;
      r=Math.sqrt(c[0] * c[0] + sr[0] * sr[0] + si[0] * si[0]);
      c[0] /= r;
      sr[0] /= r;
      si[0] /= r;
    } else {
      si[0] = c[0] = 0.0;
      sr[0]=1.0;
    }
  } else {
    c[0]=1.0;
    sr[0] = si[0] = 0.0;
  }
}


public static double comeucnrm(double ar[][], double ai[][], int lw, int n)
{
  int i,l;
  double r;

  r=0.0;
  for (i=1; i<=n; i++) {
    l=(i>lw) ? i-lw : 1;
    r += mattam(l,n,i,i,ar,ar)+mattam(l,n,i,i,ai,ai);
  }
  return (Math.sqrt(r));
}


public static void comscl(double a[][], int n, int n1, int n2, double im[])
{
  int i,j,k;
  double s,u,v,w,aij,aij1;

  k = 0;
  for (j=n1; j<=n2; j++) {
    s=0.0;
    if (im[j] != 0.0) {
      for (i=1; i<=n; i++) {
        aij=a[i][j];
        aij1=a[i][j+1];
        u=aij*aij+aij1*aij1;
        if (u > s) {
          s=u;
          k=i;
        }
      }
      if (s != 0.0) {
        v=a[k][j]/s;
        w = -a[k][j+1]/s;
        for (i=1; i<=n; i++) {
          u=a[i][j];
          s=a[i][j+1];
          a[i][j]=u*v-s*w;
          a[i][j+1]=u*w+s*v;
        }
      }
      j++;
    } else {
      for (i=1; i<=n; i++)
        if (Math.abs(a[i][j]) > Math.abs(s)) s=a[i][j];
      if (s != 0.0)
        for (i=1; i<=n; i++)
          a[i][j] /= s;
    }
  }
}


public static void sclcom(double ar[][], double ai[][], 
                          int n, int n1, int n2)
{
  int i,j,k;
  double s,r,arij,aiij;

  k = 0;
  for (j=n1; j<=n2; j++) {
    s=0.0;
    for (i=1; i<=n; i++) {
      arij=ar[i][j];
      aiij=ai[i][j];
      r=arij*arij+aiij*aiij;
      if (r > s) {
        s=r;
        k=i;
      }
    }
    if (s != 0.0) comcolcst(1,n,j,ar,ai,ar[k][j]/s,-ai[k][j]/s);
  }
}


public static double comabs(double xr, double xi)
{
  double temp;

  xr=Math.abs(xr);
  xi=Math.abs(xi);
  if (xi > xr) {
    temp=xr/xi;
    return (Math.sqrt(temp*temp+1.0)*xi);
  }
  if (xi == 0.0)
    return (xr);
  else {
    temp=xi/xr;
    return (Math.sqrt(temp*temp+1.0)*xr);
  }
}


public static void comsqrt(double ar, double ai, double pr[], double pi[])
{
  double br,bi,h,temp;

  if (ar == 0.0 && ai == 0.0)
    pr[0] = pi[0] = 0.0;
  else {
    br=Math.abs(ar);
    bi=Math.abs(ai);
    if (bi < br) {
      temp=bi/br;
      if (br < 1.0)
        h=Math.sqrt((Math.sqrt(temp*temp+1.0)*0.5+0.5)*br);
      else
        h=Math.sqrt((Math.sqrt(temp*temp+1.0)*0.125+0.125)*br)*2;
    } else {
      if (bi < 1.0) {
        temp=br/bi;
        h=Math.sqrt((Math.sqrt(temp*temp+1.0)*bi+br)*2)*0.5;
      } else {
        if (br+1.0 == 1.0)
          h=Math.sqrt(bi*0.5);
        else {
          temp=br/bi;
          h=Math.sqrt(Math.sqrt(temp*temp+1.0)*bi*0.125+br*0.125)*2;
        }
      }
    }
    if (ar >= 0.0) {
      pr[0]=h;
      pi[0]=ai/h*0.5;
    } else {
      pi[0] = (ai >= 0.0) ? h : -h;
      pr[0] = bi/h*0.5;
    }
  }
}


public static void carpol(double ar, double ai, double r[],
                          double c[], double s[])
{
  double temp;

  if (ar == 0.0 && ai == 0.0) {
    c[0] = 1.0;
    r[0] = s[0] = 0.0;
  } else {
    if (Math.abs(ar) > Math.abs(ai)) {
      temp=ai/ar;
      r[0] = Math.abs(ar)*Math.sqrt(1.0+temp*temp);
    } else {
      temp=ar/ai;
      r[0] = Math.abs(ai)*Math.sqrt(1.0+temp*temp);
    }
    c[0] = ar / r[0];
    s[0] = ai / r[0];
  }
}


public static void commul(double ar, double ai, double br, double bi,
                          double rr[], double ri[])
{
  rr[0]=ar*br-ai*bi;
  ri[0]=ar*bi+ai*br;
}


public static void comdiv(double xr, double xi, double yr, double yi,
                          double zr[], double zi[])
{
  double h,d;

  if (Math.abs(yi) < Math.abs(yr)) {
    if (yi == 0.0) {
      zr[0]=xr/yr;
      zi[0]=xi/yr;
    } else {
      h=yi/yr;
      d=h*yi+yr;
      zr[0]=(xr+h*xi)/d;
      zi[0]=(xi-h*xr)/d;
    }
  } else {
    h=yr/yi;
    d=h*yr+yi;
    zr[0]=(xr*h+xi)/d;
    zi[0]=(xi*h-xr)/d;
  }
}


public static void lngintadd(int u[], int v[], int sum[])
{
  int lu,lv,diff,carry,i,t,max;

  lu=u[0];
  lv=v[0];
  if (lu >= lv) {
    max=lu;
    diff=lu-lv+1;
    carry=0;
    for (i=lu; i>=diff; i--) {
      t=u[i]+v[i-diff+1]+carry;
      carry = (t < BASE) ? 0 : 1;
      sum[i]=t-carry*BASE;
    }
    for (i=diff-1; i>=1; i--) {
      t=u[i]+carry;
      carry = (t < BASE) ? 0 : 1;
      sum[i]=t-carry*BASE;
    }
  } else {
    max=lv;
    diff=lv-lu+1;
    carry=0;
    for (i=lv; i>=diff; i--) {
      t=v[i]+u[i-diff+1]+carry;
      carry = (t < BASE) ? 0 : 1;
      sum[i]=t-carry*BASE;
    }
    for (i=diff-1; i>=1; i--) {
      t=v[i]+carry;
      carry = (t < BASE) ? 0 : 1;
      sum[i]=t-carry*BASE;
    }
  }
  if (carry == 1) {
    for (i=max; i>=1; i--) sum[i+1]=sum[i];
    sum[1]=1;
    max=max+1;
  }
  sum[0]=max;
}


public static void lngintsubtract(int u[], int v[], int difference[])
{
  int lu,lv,diff,i,t,j,carry;

  lu=u[0];
  lv=v[0];
  if ((lu < lv) || ((lu == lv) && (u[1] < v[1]))) {
    difference[0]=0;
    return;
  }
  diff=lu-lv+1;
  carry=0;
  for (i=lu; i>=diff; i--) {
    t=u[i]-v[i-diff+1]+carry;
    carry = (t < 0) ? -1 : 0;
    difference[i]=t-carry*BASE;
  }
  for (i=diff-1; i>=1; i--) {
    t=u[i]+carry;
    carry = (t < 0) ? -1 : 0;
    difference[i]=t-carry*BASE;
  }
  if (carry == -1) {
    difference[0]=0;
    return;
  }
  i=1;
  j=lu;
  while ((difference[i] == 0) && (j > 1)) {
    j--;
    i++;
  }
  difference[0]=j;
  if (j < lu)
    for (i=1; i<=j; i++) difference[i]=difference[lu+i-j];
}


public static void lngintmult(int u[], int v[], int product[])
{
  int lu,lv,luv,i,j,carry,t;

  lu=u[0];
  lv=v[0];
  luv=lu+lv;
  for (i=lu+1; i<=luv; i++) product[i]=0;
  for (j=lu; j>=1; j--) {
    carry=0;
    for (i=lv; i>=1; i--) {
      t=u[j]*v[i]+product[j+i]+carry;
      carry=t/BASE;
      product[j+i]=t-carry*BASE;
    }
    product[j]=carry;
  }
  if (product[1] == 0) {
    for (i=2; i<=luv; i++) product[i-1]=product[i];
    luv--;
  }
  product[0]=luv;
}


public static void lngintdivide(int u[], int v[], int quotient[],
                                int remainder[])
{
  int lu,lv,v1,diff,i,t,scale,d,q1,j,carry;

  lu=u[0];
  lv=v[0];
  v1=v[1];
  diff=lu-lv;

  if (lv == 1) {
    carry=0;
    for (i=1; i<=lu; i++) {
      t=carry*BASE+u[i];
      quotient[i]=t/v1;
      carry=t-quotient[i]*v1;
    }
    remainder[0]=1;
    remainder[1]=carry;
    if (quotient[1] == 0) {
      for (i=2; i<=lu; i++) quotient[i-1]=quotient[i];
      quotient[0]=lu - ((lu == 1) ? 0 : 1);
    } else
      quotient[0]=lu;
    return;
  }

  if (lu < lv) {
    quotient[0]=1;
    quotient[1]=0;
    for (i=0; i<=lu; i++) remainder[i]=u[i];
    return;
  }

  int uu[] = new int[lu+1];
  int a[] = new int[lv+1];
  for (i=0; i<=lu; i++) uu[i]=u[i];
  scale=BASE/(v1+1);
  if (scale > 1) {
    /* normalize u */
    carry=0;
    for (i=lu; i>=1; i--) {
      t=scale*uu[i]+carry;
      carry=t/BASE;
      uu[i]=t-carry*BASE;
    }
    uu[0]=carry;
    /* normalize v */
    carry=0;
    for (i=lv; i>=1; i--) {
      t=scale*v[i]+carry;
      carry=t/BASE;
      v[i]=t-carry*BASE;
    }
    v1=v[1];
  } else
    uu[0]=0;

  /* compute quotient and remainder */
  for (i=0; i<=diff; i++) {
    d=uu[i]*BASE+uu[i+1];
    q1 = (uu[i] == v1) ? BASE-1 : d/v1;
    if (v[2]*q1 > (d-q1*v1)*BASE+uu[i+2]) {
      q1--;
      if (v[2]*q1 > (d-q1*v1)*BASE+uu[i+2]) q1--;
    }
    /* uu[i:i+lv]=u[i:i+lv]-q1*v[1:lv] */
    carry=0;
    for (j=lv; j>=1; j--) {
      t=q1*v[j]+carry;
      carry=t/BASE;
      a[j]=t-carry*BASE;
    }
    a[0]=carry;
    carry=0;
    for (j=lv; j>=0; j--) {
      t=uu[i+j]-a[j]+carry;
      carry = (t < 0) ? -1 : 0;
      uu[i+j]=t-carry*BASE;
    }
    /* if carry=-1 then q1 is one too large,
      and v must be added back to uu[i:i+lv] */
    if (carry == -1) {
      q1--;
      carry=0;
      for (j=lv; j>=1; j--) {
        t=uu[i+j]+v[j]+carry;
        carry = (t < BASE) ? 0 :1;
        uu[i+j]=t-carry*BASE;
      }
    }
    quotient[i]=q1;
  }

  /* correct storage of quotient */
  if (quotient[0] != 0) {
    for (i=diff; i>=0; i--) quotient[i+1]=quotient[i];
    quotient[0]=diff+1;
  } else
    if (quotient[1] != 0)
      quotient[0]=diff;
    else {
      for (i=1; i<=diff-1; i++) quotient[i]=quotient[i+1];
      quotient[0]=diff-1;
    }

  /* remainder=uu[diff+1:lu]/scale */
  if (scale > 1) {
    carry=0;
    for (i=1; i<=lv; i++) {
      t=carry*BASE+uu[diff+i];
      remainder[i]=t/scale;
      carry=t-remainder[i]*scale;
    }
  } else
    for (i=1; i<=lv; i++) remainder[i]=uu[diff+i];

  /* correct storage of remainder */
  i=1;
  j=lv;
  while (remainder[i] == 0 && j > 1) {
    j--;
    i++;
  }
  remainder[0]=j;
  if (j < lv)
    for (i=1; i<=j; i++) remainder[i]=remainder[lv+i-j];

  /* unnormalize the divisor v */
  if (scale > 1) {
    carry=0;
    for (i=1; i<=lv; i++) {
      t=carry*BASE+v[i];
      v[i]=t/scale;
      carry=t-v[i]*scale;
    }
  }
}


public static void lngintpower(int u[], int exponent, int result[])
{
  int max,i,n,exp;

  exp=exponent;
  max=u[0]*exp;
  int y[] = new int[max+1];
  int z[] = new int[max+1];
  int h[] = new int[max+1];

  y[0]=y[1]=1;
  for (i=u[0]; i>=0; i--) z[i]=u[i];
  for (;;) {
    n=exp/2;
    if (n+n != exp) {
      lngintmult(y,z,h);
      for (i=h[0]; i>=0; i--) y[i]=h[i];
      if (n == 0) {
        for (i=y[0]; i>=0; i--) result[i]=y[i];
        return;
      }
    }
    lngintmult(z,z,h);
    for (i=h[0]; i>=0; i--) z[i]=h[i];
    exp=n;
  }
}


}