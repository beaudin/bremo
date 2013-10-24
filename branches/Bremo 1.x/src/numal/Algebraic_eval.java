package numal;

import numal.*;

public class Algebraic_eval extends Object {


public static double pol(int n, double x, double a[])
{
  double r;

  r=0.0;
  for (; n>=0; n--) r=r*x+a[n];
  return (r);
}


public static void taypol(int n, int k, double x, double a[])
{
  int i,j,nm1;
  double xj,aa,h;

  if (x != 0.0) {
    xj=1;
    for (j=1; j<=n; j++) {
      xj *= x;
      a[j] *= xj;
    }
    aa=a[n];
    nm1=n-1;
    for (j=0; j<=k; j++) {
      h=aa;
      for (i=nm1; i>=j; i--) h = a[i] += h;
    }
  } else {
    for (; k>=1; n--) a[k]=0;
  }
}


public static void norderpol(int n, int k, double x, double a[])
{
  int i,j,nm1;
  double xj,aa,h;

  if (x != 0.0) {
    double xx[] = new double[n+1];
    xj=1;
    for (j=1; j<=n; j++) {
      xx[j] = xj *= x;
      a[j] *= xj;
    }
    h=aa=a[n];
    nm1=n-1;
    for (i=nm1; i>=0; i--) h = a[i] += h;
    for (j=1; j<=k; j++) {
      h=aa;
      for (i=nm1; i>=j; i--) h = a[i] += h;
      a[j]=h/xx[j];
    }
  }
}


public static void derpol(int n, int k, double x, double a[])
{
  int j;
  double fac;

  fac=1.0;
  norderpol(n,k,x,a);
  for (j=2; j<=k; j++) {
    fac *= j;
    a[j] *=fac;
  }
}


public static double ortpol(int n, double x, double b[], double c[])
{
  int k,l;
  double r,s,h;

  if (n == 0) return (1.0);
  r=x-b[0];
  s=1.0;
  l=n-1;
  for (k=1; k<=l; k++) {
    h=r;
    r=(x-b[k])*r-c[k]*s;
    s=h;
  }
  return (r);
}


public static double ortpolsym(int n, double x, double c[])
{
  int k,l;
  double r,s,h;

  if (n == 0) return (1.0);
  r=x;
  s=1.0;
  l=n-1;
  for (k=1; k<=l; k++) {
    h=r;
    r=x*r-c[k]*s;
    s=h;
  }
  return (r);
}


public static void allortpol(int n, double x, double b[],
                             double c[], double p[])
{
  int k,k1;
  double r,s,h;

  if (n == 0) {
    p[0]=1.0;
    return;
  }
  r=p[1]=x-b[0];
  s=p[0]=1.0;
  k=1;
  for (k1=2; k1<=n; k1++) {
    h=r;
    p[k1]=r=(x-b[k])*r-c[k]*s;
    s=h;
    k=k1;
  }
}


public static void allortpolsym(int n, double x, double c[], double p[])
{
  int k;
  double r,s,h;

  if (n == 0) {
    p[0]=1.0;
    return;
  }
  r=p[1]=x;
  s=p[0]=1.0;
  for (k=2; k<=n; k++) {
    h=r;
    p[k]=r=x*r-c[k-1]*s;
    s=h;
  }
}


public static double sumortpol(int n, double x, double b[],
                              double c[], double a[])
{
  int k;
  double h,r,s;

  if (n == 0) return (a[0]);
  r=a[n];
  s=0.0;
  for (k=n-1; k>=1; k--) {
    h=r;
    r=a[k]+(x-b[k])*r+s;
    s = -c[k]*h;
  }
  return (a[0]+(x-b[0])*r+s);
}


public static double sumortpolsym(int n, double x, double c[], double a[])
{
  int k;
  double h,r,s;

  if (n == 0) return (a[0]);
  r=a[n];
  s=0.0;
  for (k=n-1; k>=1; k--) {
    h=r;
    r=a[k]+x*r+s;
    s = -c[k]*h;
  }
  return (a[0]+x*r+s);
}


public static double chepolsum(int n, double x, double a[])
{
  int k;
  double h,r,s,tx;

  if (n == 0) return (a[0]);
  if (n == 1) return (a[0]+a[1]*x);
  tx=x+x;
  r=a[n];
  h=a[n-1]+r*tx;
  for (k=n-2; k>=1; k--) {
    s=r;
    r=h;
    h=a[k]+r*tx-s;
  }
  return (a[0]-r+h*x);
}


public static double oddchepolsum(int n, double x, double a[])
{
  int k;
  double h,r,s,y;

  if (n == 0) return (x*a[0]);
  if (n == 1) return (x*(a[0]+a[1]*(4.0*x*x-3.0)));
  y=4.0*x*x-2.0;
  r=a[n];
  h=a[n-1]+r*y;
  for (k=n-2; k>=0; k--) {
    s=r;
    r=h;
    h=a[k]+r*y-s;
  }
  return (x*(h-r));
}


public static double chepol(int n, double x)
{
  int i;
  double t1,t2,h,x2;

  if (n == 0) return (1.0);
  if (n == 1) return (x);
  t2=x;
  t1=1.0;
  x2=x+x;
  h=0.0;
  for (i=2; i<=n; i++) {
    h=x2*t2-t1;
    t1=t2;
    t2=h;;
  }
  return (h);
}


public static void allchepol(int n, double x, double t[])
{
  int i;
  double t1,t2,h,x2;

  if (n == 0) {
    t[0]=1.0;
    return;
  }
  if (n == 1) {
    t[0]=1.0;
    t[1]=x;
    return;
  }
  t[0]=t1=1.0;
  t[1]=t2=x;
  x2=x+x;
  for (i=2; i<=n; i++) {
    t[i]=h=x2*t2-t1;
    t1=t2;
    t2=h;;
  }
}


public static double sinser(int n, double theta, double b[])
{
  int k;
  double c,cc,lambda,h,dun,un,un1,temp;

  c=Math.cos(theta);
  if (c < -0.5) {
    temp=Math.cos(theta/2.0);
    lambda=4.0*temp*temp;
    un=dun=0.0;
    for (k=n; k>=1; k--) {
      dun=lambda*un-dun+b[k];
      un=dun-un;
    }
  } else {
    if (c > 0.5) {
      temp=Math.sin(theta/2.0);
      lambda = -4.0*temp*temp;
      un=dun=0.0;
      for (k=n; k>=1; k--) {
        dun += lambda*un+b[k];
        un += dun;
      }
    } else {
      cc=c+c;
      un=un1=0.0;
      for (k=n; k>=1; k--) {
        h=cc*un-un1+b[k];
        un1=un;
        un=h;
      }
    }
  }
  return (un*Math.sin(theta));
}


public static double cosser(int n, double theta, double a[])
{
  int k;
  double c,cc,lambda,h,dun,un,un1,temp;

  c=Math.cos(theta);
  if (c < -0.5) {
    temp=Math.cos(theta/2.0);
    lambda=4.0*temp*temp;
    un=dun=0.0;
    for (k=n; k>=0; k--) {
      un=dun-un;
      dun=lambda*un-dun+a[k];
    }
    return (dun-lambda/2.0*un);
  } else {
    if (c > 0.5) {
      temp=Math.sin(theta/2.0);
      lambda = -4.0*temp*temp;
      un=dun=0.0;
      for (k=n; k>=0; k--) {
        un += dun;
        dun += lambda*un+a[k];
      }
      return (dun-lambda/2.0*un);
    } else {
      cc=c+c;
      un=un1=0.0;
      for (k=n; k>=1; k--) {
        h=cc*un-un1+a[k];
        un1=un;
        un=h;
      }
      return (a[0]+un*c-un1);
    }
  }
}


public static double fouser(int n, double theta, double a[])
{
  int k;
  double c,cc,lambda,h,dun,un,un1,c2,s2;

  c=Math.cos(theta);
  if (c < -0.5) {
    c2=Math.cos(theta/2.0);
    lambda=4.0*c2*c2;
    un=dun=0.0;
    for (k=n; k>=0; k--) {
      un=dun-un;
      dun=lambda*un-dun+a[k];
    }
    return (dun+2.0*c2*(Math.sin(theta/2.0)-c2)*un);
  } else {
    if (c > 0.5) {
      s2=Math.sin(theta/2.0);
      lambda = -4.0*s2*s2;
      un=dun=0.0;
      for (k=n; k>=0; k--) {
        un += dun;
        dun += lambda*un+a[k];
      }
      return (dun+2.0*s2*(s2+Math.cos(theta/2.0))*un);
    } else {
      cc=c+c;
      un=un1=0.0;
      for (k=n; k>=1; k--) {
        h=cc*un-un1+a[k];
        un1=un;
        un=h;
      }
      return (a[0]-un1+(c+Math.sin(theta))*un);
    }
  }
}


public static double fouser1(int n, double theta, double a[], double b[])
{
  int i;
  double r,s,h,co,si;

  r=s=0.0;
  co=Math.cos(theta);
  si=Math.sin(theta);
  for (i=n; i>=1; i--) {
    h=co*r+si*s+a[i];
    s=co*s-si*r+b[i];
    r=h;
  }
  return (co*r+si*s+a[0]);
}


public static double fouser2(int n, double theta, double a[], double b[])
{
  return (cosser(n,theta,a)+sinser(n,theta,b));
}


public static void comfouser(int n, double theta, double a[],
                             double rr[], double ri[])
{
  int k;
  double c,cc,lambda,h,dun,un,un1,temp;

  c=Math.cos(theta);
  if (c < -0.5) {
    temp=Math.cos(theta/2.0);
    lambda=4.0*temp*temp;
    un=dun=0.0;
    for (k=n; k>=0; k--) {
      un=dun-un;
      dun=lambda*un-dun+a[k];
    }
    rr[0]=dun-lambda/2.0*un;
  } else {
    if (c > 0.5) {
      temp=Math.sin(theta/2.0);
      lambda = -4.0*temp*temp;
      un=dun=0.0;
      for (k=n; k>=0; k--) {
        un += dun;
        dun += lambda*un+a[k];
      }
      rr[0]=dun-lambda/2.0*un;
    } else {
      cc=c+c;
      un=un1=0.0;
      for (k=n; k>=1; k--) {
        h=cc*un-un1+a[k];
        un1=un;
        un=h;
      }
      rr[0]=a[0]+un*c-un1;
    }
  }
  ri[0]=un*Math.sin(theta);
}


public static void comfouser1(int n, double theta, double ar[],
                              double ai[], double rr[], double ri[])
{
  int k;
  double h,hr,hi,co,si;

  hr=hi=0.0;
  co=Math.cos(theta);
  si=Math.sin(theta);
  for (k=n; k>=1; k--) {
    h=co*hr-si*hi+ar[k];
    hi=co*hi+si*hr+ai[k];
    hr=h;
  }
  rr[0]=co*hr-si*hi+ar[0];
  ri[0]=co*hi+si*hr+ai[0];
}


public static void comfouser2(int n, double theta, double ar[],
                              double ai[], double rr[], double ri[])
{
  double car[] = new double[1];
  double cai[] = new double[1];
  double sar[] = new double[1];
  double sai[] = new double[1];

  comfouser(n,theta,ar,car,sar);
  comfouser(n,theta,ai,cai,sai);
  rr[0]=car[0]-sai[0];
  ri[0]=cai[0]+sar[0];
}


public static double jfrac(int n, double a[], double b[])
{
  int i;
  double d;

  d=0.0;
  for (i=n; i>=1; i--) d=a[i]/(b[i]+d);
  return (d+b[0]);
}


public static void polchs(int n, double a[])
{
  int k,l,twopow;

  if (n > 1) {
    twopow=2;
    for (k=1; k<=n-2; k++) {
      a[k] /= twopow;
      twopow *= 2;
    }
    a[n-1]=2.0*a[n-1]/twopow;
    a[n] /= twopow;
    a[n-2] += a[n];
    for (k=n-2; k>=1; k--) {
      a[k-1] += a[k+1];
      a[k]=2.0*a[k]+a[k+2];
      for (l=k+1; l<=n-2; l++) a[l] += a[l+2];
    }
  }
}


public static void chspol(int n, double a[])
{
  int k,l,twopow;

  if (n > 1) {
    for (k=0; k<=n-2; k++) {
      for (l=n-2; l>=k; l--) a[l] -= a[l+2];
      a[k+1] /= 2.0;
    }
    twopow=2;
    for (k=1; k<=n-2; k++) {
      a[k] *= twopow;
      twopow *= 2;
    }
    a[n-1] *= twopow;
    a[n] *= twopow;
  }
}


public static void polshtchs(int n, double a[])
{
  lintfmpol(0.5,0.5,n,a);
  polchs(n,a);
}


public static void shtchspol(int n, double a[])
{
  chspol(n,a);
  lintfmpol(2.0,-1.0,n,a);
}


public static void grnnew(int n, double x[], double a[])
{
  int k,l;

  for (k=n-1; k>=0; k--)
    for (l=n-1; l>=n-1-k; l--) a[l] += a[l+1]*x[n-1-k];
}


public static void newgrn(int n, double x[], double a[])
{
  int k;

  for (k=n-1; k>=0; k--)
    Basic.elmvec(k,n-1,1,a,a,-x[k]);
}


public static void lintfmpol(double p, double q, int n, double a[])
{
  int k;
  double ppower;

  norderpol(n,n,q,a);
  ppower=p;
  for (k=1; k<=n; k++) {
    a[k] *= ppower;
    ppower *= p;
  }
}


public static void intchs(int n, double a[], double b[])
{
  int i;
  double h,l,dum;

  if (n == 0) {
    b[1]=a[0];
    return;
  }
  if (n == 1) {
    b[2]=a[1]/4.0;
    b[1]=a[0];
    return;
  }
  h=a[n];
  dum=a[n-1];
  b[n+1]=h/((n+1)*2);
  b[n]=dum/(n*2);
  for (i=n-1; i>=2; i--) {
    l=a[i-1];
    b[i]=(l-h)/(2*i);
    h=dum;
    dum=l;
  }
  b[1]=a[0]-h/2.0;
}

}