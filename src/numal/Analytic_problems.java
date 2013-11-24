package numal;

import java.util.*;
import numal.*;

public class Analytic_problems extends Object {


public static boolean zeroin(double x[], double y[],
                             AP_zeroin_methods method)
{
	boolean extrapolate;
	int ext;
	double c,fc,b,fb,a,fa,d,fd,fdb,fda,w,mb,tol,m,p,q;

	fd=d=0.0;
	b = x[0];
	fb=method.fx(x);
	a = x[0] = y[0];
	fa=method.fx(x);
	c=a;
	fc=fa;
	ext=0;
	extrapolate=true;
	while (extrapolate) {
		if (Math.abs(fc) < Math.abs(fb)) {
			if (c != a) {
				d=a;
				fd=fa;
			}
			a=b;
			fa=fb;
			b = x[0] =c;
			fb=fc;
			c=a;
			fc=fa;
		}
		tol=method.tolx(x);
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
					fdb=(fd-fb)/(d-b);
					fda=(fd-fa)/(d-a);
					p *= fda;
					q=fdb*fa-fda*fb;
				}
				if (p < 0.0) {
					p = -p;
					q = -q;
				}
				w=(p<Double.MIN_VALUE || p<=q*tol) ? tol :
                             ((p<mb*q) ? p/q : mb);
			}
			d=a;
			fd=fa;
			a=b;
			fa=fb;
			x[0] = b += w;
			fb=method.fx(x);
			if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
				c=a;
				fc=fa;
				ext=0;
			} else
				ext = (w == mb) ? 0 : ext+1;
		} else
			break;
	}
	y[0] = c;
	return ((fc >= 0.0) ? (fb <= 0.0) : (fb >= 0.0));
}


public static boolean zeroinrat(double x[], double y[],
                                AP_zeroinrat_methods method)
{
	boolean extrapolate, first;
	int ext;
	double b,fb,a,fa,d,fd,c,fc,fdb,fda,w,mb,tol,m,p,q;

  fd=d=0.0;
	b = x[0];
	fb=method.fx(x);
	a = x[0] = y[0];
	fa=method.fx(x);
	first=true;
	c=a;
	fc=fa;
	ext=0;
	extrapolate=true;
	while (extrapolate) {
		if (Math.abs(fc) < Math.abs(fb)) {
			if (c != a) {
				d=a;
				fd=fa;
			}
			a=b;
			fa=fb;
			b = x[0] =c;
			fb=fc;
			c=a;
			fc=fa;
		}
		tol=method.tolx(x);
		m=(c+b)*0.5;
		mb=m-b;
		if (Math.abs(mb) > tol) {
			if (ext > 3)
				w=mb;
			else {
				if (mb == 0.0)
					tol=0.0;
				else
					if (mb < 0.0) tol = -tol;
				p=(b-a)*fb;
				if (first) {
					q=fa-fb;
					first=false;
				} else {
					fdb=(fd-fb)/(d-b);
					fda=(fd-fa)/(d-a);
					p *= fda;
					q=fdb*fa-fda*fb;
				}
				if (p < 0.0) {
					p = -p;
					q = -q;
				}
				if (ext == 3) p *= 2.0;
				w=(p<Double.MIN_VALUE || p<=q*tol) ? tol :
                             ((p<mb*q) ? p/q : mb);
			}
			d=a;
			fd=fa;
			a=b;
			fa=fb;
			x[0] = b += w;
			fb=method.fx(x);
			if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
				c=a;
				fc=fa;
				ext=0;
			} else
				ext = (w == mb) ? 0 : ext+1;
		} else
			break;
	}
	y[0] = c;
	return ((fc >= 0.0) ? (fb <= 0.0) : (fb >= 0.0));
}


public static boolean zeroinder(double x[], double y[],
                                AP_zeroinder_methods method)
{
	boolean extrapolate;
	int ext;
	double b,fb,dfb,a,fa,dfa,c,fc,dfc,d,w,mb,tol,m,p,q;

	b = x[0];
	fb=method.fx(x);
	dfb=method.dfx(x);
	a = x[0] = y[0];
	fa=method.fx(x);
	dfa=method.dfx(x);
	c=a;
	fc=fa;
	dfc=dfa;
	ext=0;
	extrapolate=true;
	while (extrapolate) {
		if (Math.abs(fc) < Math.abs(fb)) {
			a=b;
			fa=fb;
			dfa=dfb;
			b = x[0] =c;
			fb=fc;
			dfb=dfc;
			c=a;
			fc=fa;
			dfc=dfa;
		}
		tol=method.tolx(x);
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
				d = (ext == 2) ? dfa : (fb-fa)/(b-a);
				p=fb*d*(b-a);
				q=fa*dfb-fb*d;
				if (p < 0.0) {
					p = -p;
					q = -q;
				}
				w=(p<Double.MIN_VALUE || p<=q*tol) ? tol :
                             ((p<mb*q) ? p/q : mb);
			}
			a=b;
			fa=fb;
			dfa=dfb;
			x[0] = b += w;
			fb=method.fx(x);
			dfb=method.dfx(x);
			if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
				c=a;
				fc=fa;
				dfc=dfa;
				ext=0;
			} else
				ext = (w == mb) ? 0 : ext+1;
		} else
			break;
	}
	y[0] = c;
	return ((fc >= 0.0) ? (fb <= 0.0) : (fb >= 0.0));
}


public static void quanewbnd(int n, int lw, int rw,
                   double x[], double f[], double jac[],
                   AP_quanewbnd_method method,
                   double in[], double out[])
{
	int l,it,fcnt,fmax,err,b,i,j,k,r,m;
	double macheps,reltol,abstol,tolres,nd,mz,res,mul,crit;
  double aux[] = new double[6];
  double delta[] = new double[n+1];
  
	nd=0.0;
	macheps=in[0];
	reltol=in[1];
	abstol=in[2];
	tolres=in[3];
	fmax=(int)in[4];
	mz=macheps*macheps;
	it=fcnt=0;
	b=lw+rw;
	l=(n-1)*b+n;
	b++;
	res=Math.sqrt(Basic.vecvec(1,n,0,f,f));
	err=0;
	while (true) {
		if (err != 0 || (res < tolres &&
			Math.sqrt(nd) < 
			  Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol)) break;
		it++;
		if (it != 1) {
			/* update jac */
			double pp[] = new double[n+1];
			double s[] = new double[n+1];
			crit=nd*mz;
			for (i=1; i<=n; i++) pp[i]=delta[i]*delta[i];
			r=k=1;
			m=rw+1;
			for (i=1; i<=n; i++) {
				mul=0.0;
				for (j=r; j<=m; j++) mul += pp[j];
				j=r-k;
				if (Math.abs(mul) > crit)
				  Basic.elmvec(k,m-j,j,jac,delta,f[i]/mul);
				k += b;
				if (i > lw)
					r++;
				else
					k--;
				if (m < n) m++;
			}
		}
		/* direction */
		double lu[] = new double[l+1];
		aux[2]=macheps;
		Basic.mulvec(1,n,0,delta,f,-1.0);
		Basic.dupvec(1,l,0,lu,jac);
		Linear_algebra.decsolbnd(lu,n,lw,rw,aux,delta);
		if (aux[3] != n) {
			err=3;
			break;
		} else {
			Basic.elmvec(1,n,0,x,delta,1.0);
			nd=Basic.vecvec(1,n,0,delta,delta);
			/* evaluate */
			fcnt += n;
			if (!(method.funct(n,1,n,x,f))) {
				err=2;
				break;
			}
			if (fcnt > fmax) err=1;
			res=Math.sqrt(Basic.vecvec(1,n,0,f,f));
		}
	}
	out[1]=Math.sqrt(nd);
	out[2]=res;
	out[3]=fcnt;
	out[4]=it;
	out[5]=err;
}


public static void quanewbnd1(int n, int lw, int rw,
                              double x[], double f[],
                              AP_quanewbnd_method method,
                              double in[], double out[])
{
  int kk,i,j,k,l,u,t,b;
  double aid,stepi,s;
	double jac[] = new double[(lw+rw)*(n-1)+n+1];
	
	method.funct(n,1,n,x,f);
  s=in[5];
	kk=(lw+rw)*(n-1)+n*2-((lw-1)*lw+(rw-1)*rw)/2;
	in[4] -= kk;
  l=1;
  u=lw+1;
  t=rw+1;
  b=lw+rw;
  for (i=1; i<=n; i++) {
    double f1[] = new double[u+1];
    stepi=s;
    aid=x[i];
    x[i]=aid+stepi;
    method.funct(n,l,u,x,f1);
    x[i]=aid;
    k = i+((i <= t) ? 0 : i-t)*b;
    for (j=l; j<=u; j++) {
      jac[k]=(f1[j]-f[j])/stepi;
      k += b;
    }
    if (i >= t) l++;
    if (u < n) u++;
  }
	quanewbnd(n,lw,rw,x,f,jac,method,in,out);
	in[4] += kk;
	out[3] += kk;
}


public static double minin(double x[], double a[], double b[],
                           AP_minin_methods method)
{
	double z,c,d,e,m,p,q,r,tol,t,u,v,w,fu,fv,fw,fz;

  d=0.0;
	c=(3.0-Math.sqrt(5.0))/2.0;
	if (a[0] > b[0]) {
		z = a[0];
		a[0] = b[0];
		b[0]=z;
	}
	w = x[0] = a[0];
	fw=method.fx(x);
	z = x[0] = b[0];
	fz=method.fx(x);
	if (fz > fw) {
		z=w;
		w = x[0];
		v=fz;
		fz=fw;
		fw=v;
	}
	v=w;
	fv=fw;
	e=0.0;
	while (true) {
		m=(a[0] + b[0])*0.5;
		tol=method.tolx(x);
		t=tol*2.0;
		if (Math.abs(z-m) <= t-(b[0] - a[0])*0.5) break;
		p=q=r=0.0;
		if (Math.abs(e) > tol) {
			r=(z-w)*(fz-fv);
			q=(z-v)*(fz-fw);
			p=(z-v)*q-(z-w)*r;
			q=(q-r)*2.0;
			if (q > 0.0)
				p = -p;
			else
				q = -q;
			r=e;
			e=d;
		}
		if (Math.abs(p) < Math.abs(q*r*0.5) && p > 
                      (a[0]-z)*q && p < (b[0]-z)*q) {
			d=p/q;
			u=z+d;
			if (u-a[0] < t || b[0]-u < t) d = ((z < m) ? tol : -tol);
		} else {
			e = ((z < m) ? b[0] : a[0]) - z;
			d=c*e;
		}
		u = x[0] = z + ((Math.abs(d) >= tol) ? d :
                       ((d>0.0) ? tol : -tol));
		fu=method.fx(x);
		if (fu <= fz) {
			if (u < z)
				b[0]=z;
			else
				a[0]=z;
			v=w;
			fv=fw;
			w=z;
			fw=fz;
			z=u;
			fz=fu;
		} else {
			if (u < z)
				a[0]=u;
			else
				b[0]=u;
			if (fu <= fw) {
				v=w;
				fv=fw;
				w=u;
				fw=fu;
			} else
				if (fu <= fv || v == w) {
					v=u;
					fv=fu;
				}
		}
	}
	x[0]=z;
	return fz;
}


public static double mininder(double x[], double y[],
                              AP_mininder_methods method)
{
	int sgn;
	double a,b,c,fa,fb,fu,dfa,dfb,dfu,e,d,tol,ba,z,p,q,s;

	if (x[0] <= y[0]) {
		a = x[0];
		fa=method.fx(x);
		dfa=method.dfx(x);
		b = x[0] = y[0];
		fb=method.fx(x);
		dfb=method.dfx(x);
	} else {
		b = x[0];
		fb=method.fx(x);
		dfb=method.dfx(x);
		a = x[0] = y[0];
		fa=method.fx(x);
		dfa=method.dfx(x);
	}
	c=(3.0-Math.sqrt(5.0))/2.0;
	d=b-a;
	e=d*2.0;
	z=e*2.0;
	while (true) {
		ba=b-a;
		tol=method.tolx(x);
		if (ba < tol*3.0) break;
		if (Math.abs(dfa) <= Math.abs(dfb)) {
			x[0]=a;
			sgn=1;
		} else {
			x[0]=b;
			sgn = -1;
		}
		if (dfa <= 0.0 && dfb >= 0.0) {
			z=(fa-fb)*3.0/ba+dfa+dfb;
			s=Math.sqrt(z*z-dfa*dfb);
			p = (sgn == 1) ? dfa-s-z : dfb+s-z;
			p *= ba;
			q=dfb-dfa+s*2.0;
			z=e;
			e=d;
			d = (Math.abs(p) <= Math.abs(q)*tol) ? tol*sgn : -p/q;
		} else
			d=ba;
		if (Math.abs(d) >= Math.abs(z*0.5) || 
                       Math.abs(d) > ba*0.5) {
			e=ba;
			d=c*ba*sgn;
		}
		x[0] += d;
		fu=method.fx(x);
		dfu=method.dfx(x);
		if (dfu >= 0.0 || (fu >= fa && dfa <= 0.0)) {
			b = x[0];
			fb=fu;
			dfb=dfu;
		} else {
			a = x[0];
			fa=fu;
			dfa=dfu;
		}
	}
	if (fa <= fb) {
		x[0]=a;
		y[0]=b;
		return fa;
	} else {
		x[0]=b;
		y[0]=a;
		return fb;
	}
}


public static void linemin(int n, double x[], double d[],
              double nd, double alfa[], double g[],
              AP_linemin_method method, double f0,
              double f1[], double df0, double df1[],
              int evlmax[], boolean strongsearch, double in[])
{
	boolean notinint;
	int evl;
	double f,oldf,df,olddf,mu,alfa0,q,w,y,z,reltol,abstol,eps,aid;
	double x0[] = new double[n+1];
	
	reltol=in[1];
	abstol=in[2];
	mu=in[3];
	evl=0;
	alfa0=0.0;
	oldf=f0;
	olddf=df0;
	y = alfa[0];
	notinint=true;
	Basic.dupvec(1,n,0,x0,x);
	eps=(Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol)/nd;
	q=(f1[0]-f0)/(alfa[0]*df0);
	while (true) {
		if (notinint) notinint = (df1[0] < 0.0 && q > mu);
		aid = alfa[0];
		if (df1[0] >= 0.0) {
			/* cubic interpolation */
			z=3.0*(oldf-f1[0])/alfa[0]+olddf+df1[0];
			w=Math.sqrt(z*z-olddf*df1[0]);
			alfa[0] = alfa[0]*(1.0-(df1[0]+w-z)/(df1[0]-olddf+w*2.0));
			if (alfa[0] < eps)
				alfa[0]=eps;
			else
				if (aid-alfa[0] < eps) alfa[0]=aid-eps;
		} else
			if (notinint) {
				alfa0 = alfa[0] = y;
				olddf = df1[0];
				oldf = f1[0];
			} else
				alfa[0] *= 0.5;
		y = alfa[0]+alfa0;
		Basic.dupvec(1,n,0,x,x0);
		Basic.elmvec(1,n,0,x,d,y);
		eps=(Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol)/nd;
		f=method.funct(n,x,g);
		evl++;
		df=Basic.vecvec(1,n,0,d,g);
		q=(f-f0)/(y*df0);
		if (!(((notinint || strongsearch) ? true : 
         (q < mu || q > 1.0-mu)) && (evl < evlmax[0]))) break;
		if (notinint || df > 0.0 || q < mu) {
			df1[0]=df;
			f1[0]=f;
		} else {
			alfa0=y;
			alfa[0]=aid-alfa[0];
			olddf=df;
			oldf=f;
		}
		if (alfa[0] <= eps*2.0) break;
	}
	alfa[0]=y;
	evlmax[0]=evl;
	df1[0]=df;
	f1[0]=f;
}


public static void rnk1upd(double h[], int n, double v[], double c)
{
	int j,k;

	k=0;
	j=1;
	do {
		k++;
		Basic.elmvec(j,j+k-1,1-j,h,v,v[k]*c);
		j += k;
	} while (k < n);
}


public static void davupd(double h[], int n, double v[], double w[],
                          double c1, double c2)
{
	int i,j,k;
	double vk,wk;

	k=0;
	j=1;
	do {
		k++;
		vk=v[k]*c1;
		wk=w[k]*c2;
		for (i=0; i<=k-1; i++) h[i+j] += v[i+1]*vk-w[i+1]*wk;
		j += k;
	} while (k < n);
}


public static void fleupd(double h[], int n, double v[], double w[],
                          double c1, double c2)
{
	int i,j,k;
	double vk,wk;

	k=0;
	j=1;
	do {
		k++;
		vk = -w[k]*c1+v[k]*c2;
		wk=v[k]*c1;
		for (i=0; i<=k-1; i++) h[i+j] += v[i+1]*vk-w[i+1]*wk;
		j += k;
	} while (k < n);
}


public static void praxis(int n, double x[],
                          AP_praxis_method method,
                          double in[], double out[])
{
	boolean illc,emergency;
	int i,j,k,k2,maxf,kl,kt,ktm;
	double s,sl,dn,dmin,f1,lds,ldt,sf,df,qf1,qd0,qd1,m2,m4,
         small,vsmall,large,vlarge,scbd,ldfac,t2,macheps,
         reltol,abstol,h,l;
  int nl[] = new int[1];
  int nf[] = new int[1];
  double em[] = new double[8];
	double d[] = new double[n+1];
	double y[] = new double[n+1];
	double z[] = new double[n+1];
	double q0[] = new double[n+1];
	double q1[] = new double[n+1];
	double v[][] = new double[n+1][n+1];
	double a[][] = new double[n+1][n+1];
  double qa[] = new double[1];
  double qb[] = new double[1];
  double qc[] = new double[1];
  double fx[] = new double[1];
	double tmp1[] = new double[1];
  double tmp2[] = new double[1];
  double tmp3[] = new double[1];

  Random ran = new Random(1);
	macheps=in[0];
	reltol=in[1];
	abstol=in[2];
	maxf=(int)in[5];
	h=in[6];
	scbd=in[7];
	ktm=(int)in[8];
	illc = in[9] < 0.0;
	small=macheps*macheps;
	vsmall=small*small;
	large=1.0/small;
	vlarge=1.0/vsmall;
	m2=reltol;
	m4=Math.sqrt(m2);
	ldfac = (illc ? 0.1 : 0.01);
	kt=nl[0]=0;
	nf[0]=1;
	out[3]=qf1=fx[0]=method.funct(n,x);
	abstol=t2=small+Math.abs(abstol);
	dmin=small;
	if (h < abstol*100.0) h=abstol*100;
	ldt=h;
	Basic.inimat(1,n,1,n,v,0.0);
	for (i=1; i<=n; i++) v[i][i]=1.0;
	d[1]=qd0=qd1=0.0;
	Basic.dupvec(1,n,0,q1,x);
	Basic.inivec(1,n,q0,0.0);
	emergency=false;

	while (true) {
		sf=d[1];
		d[1]=s=0.0;
    tmp1[0]=d[1];
    tmp2[0]=s;
    praxismin(1,2,tmp1,tmp2,fx,false,n,x,v,qa,qb,qc,
              qd0,qd1,q0,q1,nf,nl,fx,m2,m4,dmin,ldt,
              reltol,abstol,small,h,method);
    d[1]=tmp1[0];
    s=tmp2[0];
		if (s <= 0.0) Basic.mulcol(1,n,1,1,v,v,-1.0);
		if (sf <= 0.9*d[1] || 0.9*sf >= d[1]) Basic.inivec(2,n,d,0.0);
		for (k=2; k<=n; k++) {
			Basic.dupvec(1,n,0,y,x);
			sf=fx[0];
			illc = (illc || kt > 0);
			while (true) {
				kl=k;
				df=0.0;
				if (illc) {
					/* random stop to get off resulting valley */
					for (i=1; i<=n; i++) {
						s=z[i]=(0.1*ldt+t2*Math.pow(10.0,kt))*
                   (ran.nextDouble()-0.5);
						Basic.elmveccol(1,n,i,x,v,s);
					}
					fx[0]=method.funct(n,x);
					nf[0]++;
				}
				for (k2=k; k2<=n; k2++) {
					sl=fx[0];
					s=0.0;
          tmp1[0]=d[k2];
          tmp2[0]=s;
					praxismin(k2,2,tmp1,tmp2,fx,false,n,x,v,qa,qb,qc,
                    qd0,qd1,q0,q1,nf,nl,fx,m2,m4,dmin,ldt,
                    reltol,abstol,small,h,method);
          d[k2]=tmp1[0];
          s=tmp2[0];
					s = illc ? d[k2]*(s+z[k2])*(s+z[k2]) : sl-fx[0];
					if (df < s) {
						df=s;
						kl=k2;
					}
				}
				if (!illc && df < Math.abs(100.0*macheps*fx[0]))
					illc=true;
				else
					break;
			}
			for (k2=1; k2<=k-1; k2++) {
				s=0.0;
        tmp1[0]=d[k2];
        tmp2[0]=s;
				praxismin(k2,2,tmp1,tmp2,fx,false,n,x,v,qa,qb,qc,
                  qd0,qd1,q0,q1,nf,nl,fx,m2,m4,dmin,ldt,
                  reltol,abstol,small,h,method);
        d[k2]=tmp1[0];
        s=tmp2[0];
			}
			f1=fx[0];
			fx[0]=sf;
			lds=0.0;
			for (i=1; i<=n; i++) {
				sl=x[i];
				x[i]=y[i];
				y[i] = sl -= y[i];
				lds += sl*sl;
			}
			lds=Math.sqrt(lds);
			if (lds > small) {
				for (i=kl-1; i>=k; i--) {
					for (j=1; j<=n; j++) v[j][i+1]=v[j][i];
					d[i+1]=d[i];
				}
				d[k]=0.0;
				Basic.dupcolvec(1,n,k,v,y);
				Basic.mulcol(1,n,k,k,v,v,1.0/lds);
        tmp1[0]=d[k];
        tmp2[0]=lds;
        tmp3[0]=f1;
        praxismin(k,4,tmp1,tmp2,tmp3,true,n,x,v,qa,qb,qc,
                  qd0,qd1,q0,q1,nf,nl,fx,m2,m4,dmin,ldt,
                  reltol,abstol,small,h,method);
        d[k]=tmp1[0];
        lds=tmp2[0];
        f1=tmp3[0];
				if (lds <= 0.0) {
					lds = -lds;
					Basic.mulcol(1,n,k,k,v,v,-1.0);
				}
			}
			ldt *= ldfac;
			if (ldt < lds) ldt=lds;
			t2=m2*Math.sqrt(Basic.vecvec(1,n,0,x,x))+abstol;
			kt = (ldt > 0.5*t2) ? 0 : kt+1;
			if (kt > ktm) {
				out[1]=0.0;
				emergency=true;
			}
		}
		if (emergency) break;
		/* quad */
		s=fx[0];
		fx[0]=qf1;
		qf1=s;
		qd1=0.0;
		for (i=1; i<=n; i++) {
			s=x[i];
			x[i]=l=q1[i];
			q1[i]=s;
			qd1 += (s-l)*(s-l);
		}
		l=qd1=Math.sqrt(qd1);
		s=0.0;
		if ((qd0*qd1 > Double.MIN_VALUE) && (nl[0] >=3*n*n)) {
      tmp1[0]=s;
      tmp2[0]=l;
      tmp3[0]=qf1;
      praxismin(0,2,tmp1,tmp2,tmp3,true,n,x,v,qa,qb,qc,
                qd0,qd1,q0,q1,nf,nl,fx,m2,m4,dmin,ldt,
                reltol,abstol,small,h,method);
      s=tmp1[0];
      l=tmp2[0];
      qf1=tmp3[0];
			qa[0]=l*(l-qd1)/(qd0*(qd0+qd1));
			qb[0]=(l+qd0)*(qd1-l)/(qd0*qd1);
			qc[0]=l*(l+qd0)/(qd1*(qd0+qd1));
		} else {
			fx[0]=qf1;
			qa[0]=qb[0]=0.0;
			qc[0]=1.0;
		}
		qd0=qd1;
		for (i=1; i<=n; i++) {
			s=q0[i];
			q0[i]=x[i];
			x[i]=qa[0]*s+qb[0]*x[i]+qc[0]*q1[i];
		}
		/* end of quad */
		dn=0.0;
		for (i=1; i<=n; i++) {
			d[i]=1.0/Math.sqrt(d[i]);
			if (dn < d[i]) dn=d[i];
		}
		for (j=1; j<=n; j++) {
			s=d[j]/dn;
			Basic.mulcol(1,n,j,j,v,v,s);
		}
		if (scbd > 1.0) {
			s=vlarge;
			for (i=1; i<=n; i++) {
				sl=z[i]=Math.sqrt(Basic.mattam(1,n,i,i,v,v));
				if (sl < m4) z[i]=m4;
				if (s > sl) s=sl;
			}
			for (i=1; i<=n; i++) {
				sl=s/z[i];
				z[i]=1.0/sl;
				if (z[i] > scbd) {
					sl=1.0/scbd;
					z[i]=scbd;
				}
				Basic.mulrow(1,n,i,i,v,v,sl);
			}
		}
		for (i=1; i<=n; i++) Basic.ichrowcol(i+1,n,i,i,v);
		em[0]=em[2]=macheps;
		em[4]=10*n;
		em[6]=vsmall;
		Basic.dupmat(1,n,1,n,a,v);
		if (Linear_algebra.qrisngvaldec(a,n,n,d,v,em) != 0) {
			out[1]=2.0;
			emergency=true;
		}
		if (emergency) break;
		if (scbd > 1.0) {
			for (i=1; i<=n; i++) Basic.mulrow(1,n,i,i,v,v,z[i]);
			for (i=1; i<=n; i++) {
				s=Math.sqrt(Basic.tammat(1,n,i,i,v,v));
				d[i] *= s;
				s=1.0/s;
				Basic.mulcol(1,n,i,i,v,v,s);
			}
		}
		for (i=1; i<=n; i++) {
			s=dn*d[i];
			d[i] = (s > large) ? vsmall :
						((s < small) ? vlarge : 1.0/(s*s));
		}
		/* sort */
		for (i=1; i<=n-1; i++) {
			k=i;
			s=d[i];
			for (j=i+1; j<=n; j++)
				if (d[j] > s) {
					k=j;
					s=d[j];
				}
			if (k > i) {
				d[k]=d[i];
				d[i]=s;
				for (j=1; j<=n; j++) {
					s=v[j][i];
					v[j][i]=v[j][k];
					v[j][k]=s;
				}
			}
		}
		/* end of sort */
		dmin=d[n];
		if (dmin < small) dmin=small;
		illc = (m2*d[1]) > dmin;
		if (nf[0] >= maxf) {
			out[1]=1.0;
			break;
		}
	}
	out[2]=fx[0];
	out[4]=nf[0];
	out[5]=nl[0];
	out[6]=ldt;
}


static private void praxismin(int j, int nits, double d2[],
               double x1[], double f1[], boolean fk, int n,
               double x[], double v[][], double qa[],
               double qb[], double qc[], double qd0,
               double qd1, double q0[], double q1[],
               int nf[], int nl[], double fx[], double m2,
               double m4, double dmin, double ldt,
               double reltol, double abstol, double small,
               double h, AP_praxis_method method)
{
	/* this function is internally used by PRAXIS */

  boolean loop,dz;
	int k;
	double x2,xm,f0,f2,fm,d1,t2,s,sf1,sx1;

  f2=x2=0.0;
	sf1 = f1[0];
	sx1 = x1[0];
	k=0;
	xm=0.0;
	f0 = fm = fx[0];
	dz = d2[0] < reltol;
	s=Math.sqrt(Basic.vecvec(1,n,0,x,x));
	t2=m4*Math.sqrt(Math.abs(fx[0])/(dz ? dmin : d2[0])+s*ldt)+m2*ldt;
	s=s*m4+abstol;
	if (dz && (t2 > s)) t2=s;
	if (t2 < small) t2=small;
	if (t2 > 0.01*h) t2=0.01*h;
	if (fk && (f1[0] <= fm)) {
		xm = x1[0];
		fm = f1[0];
	}
	if (!fk || (Math.abs(x1[0]) < t2)) {
		x1[0] = (x1[0] > 0.0) ? t2 : -t2;
    f1[0]=praxisflin(x1[0],j,n,x,v,qa,qb,qc,qd0,qd1,q0,q1,nf,method);
	}
	if (f1[0] <= fm) {
		xm = x1[0];
		fm = f1[0];
	}
	loop=true;
	while (loop) {
		if (dz) {
			/* evaluate praxisflin at another point and
				estimate the second derivative */
			x2 = (f0 < f1[0]) ? -x1[0] : x1[0]*2.0;
      f2=praxisflin(x2,j,n,x,v,qa,qb,qc,qd0,qd1,q0,q1,nf,method);
			if (f2 <= fm) {
				xm=x2;
				fm=f2;
			}
			d2[0]=(x2*(f1[0]-f0)-x1[0]*(f2-f0))/(x1[0]*x2*(x1[0]-x2));
		}
		/* estimate first derivative at 0 */
		d1=(f1[0]-f0)/x1[0]-x1[0]*d2[0];
		dz=true;
		x2 = (d2[0] <= small) ? ((d1 < 0.0) ? h : -h) : -0.5*d1/d2[0];
		if (Math.abs(x2) > h)	x2 = (x2 > 0.0) ? h : -h;
		while (true) {
      f2=praxisflin(x2,j,n,x,v,qa,qb,qc,qd0,qd1,q0,q1,nf,method);
			if (k < nits && f2 > f0) {
				k++;
				if (f0 < f1[0] && x1[0]*x2 > 0.0) break;
				x2=0.5*x2;
			} else {
				loop=false;
				break;
			}
		}
	}
	nl[0]++;
	if (f2 > fm)
		x2=xm;
	else
		fm=f2;
	d2[0] = (Math.abs(x2*(x2-x1[0])) > small) ?
				((x2*(f1[0]-f0)-x1[0]*(fm-f0))/(x1[0]*x2*(x1[0]-x2))) :
				((k > 0) ? 0.0 : d2[0]);
	if (d2[0] <= small) d2[0]=small;
	x1[0]=x2;
	fx[0]=fm;
	if (sf1 < fx[0]) {
		fx[0]=sf1;
		x1[0]=sx1;
	}
	if (j > 0) Basic.elmveccol(1,n,j,x,v,x1[0]);
}


static private double praxisflin(double l, int j, int n,
               double x[], double v[][], double qa[], double qb[],
               double []qc, double qd0, double qd1, double q0[],
               double q1[], int nf[], AP_praxis_method method)
{
	/* this function is internally used by PRAXISMIN */

	int i;
	double result;
	double t[] = new double[n+1];

	if (j > 0)
		for (i=1; i<=n; i++) t[i]=x[i]+l*v[i][j];
	else {
		/* search along parabolic space curve */
		qa[0]=l*(l-qd1)/(qd0*(qd0+qd1));
		qb[0]=(l+qd0)*(qd1-l)/(qd0*qd1);
		qc[0]=l*(l+qd0)/(qd1*(qd0+qd1));
		for (i=1; i<=n; i++) t[i]=qa[0]*q0[i]+qb[0]*x[i]+qc[0]*q1[i];
	}
	nf[0]++;
	result=method.funct(n,t);
	return result;
}


public static double rnk1min(int n, double x[], double g[],
                     double h[], AP_linemin_method method,
                     double in[], double out[])
{
	boolean ok;
	int i,it,n2,cntl,cnte,evl,evlmax;
	double f,f0,fmin,mu,dg,dg0,ghg,gs,nrmdelta,alfa,macheps,
         reltol,abstol,eps,tolg,orth,aid,temp1,temp2;
  double em[] = new double[10];
  double xtmp1[] = new double[1];
  double xtmp2[] = new double[1];
  double xtmp3[] = new double[1];
  int itmp[] = new int[1];
	double v[] = new double[n+1];
	double delta[] = new double[n+1];
	double gamma[] = new double[n+1];
	double s[] = new double[n+1];
	double p[] = new double[n+1];
	double vec[][] = new double[n+1][n+1];

	macheps=in[0];
	reltol=in[1];
	abstol=in[2];
	mu=in[3];
	tolg=in[4];
	fmin=in[5];
	it=0;
	alfa=in[6];
	evlmax=(int)in[7];
	orth=in[8];
	n2=(n*(n+1))/2;
	cntl=cnte=0;
	if (alfa > 0.0) {
		Basic.inivec(1,n2,h,0.0);
		Basic.inisymd(1,n,0,h,alfa);
	}
	f=method.funct(n,x,g);
	evl=1;
	dg=Math.sqrt(Basic.vecvec(1,n,0,g,g));
	for (i=1; i<=n; i++) delta[i] = -Basic.symmatvec(1,n,i,h,g);
	nrmdelta=Math.sqrt(Basic.vecvec(1,n,0,delta,delta));
	dg0=Basic.vecvec(1,n,0,delta,g);
	ok = dg0 < 0.0;
	eps=Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol;
	it++;
	while ((nrmdelta > eps || dg > tolg || !ok) && (evl < evlmax)) {
		if (!ok) {
			/* calculating greenstadts direction */
			double th[] = new double[n2+1];
			em[0]=macheps;
			em[2]=aid=Math.sqrt(macheps*reltol);
			em[4]=orth;
			em[6]=aid*n;
			em[8]=5.0;
			cnte++;
			Basic.dupvec(1,n2,0,th,h);
			Linear_algebra.eigsym1(th,n,n,v,vec,em);
			for (i=1; i<=n; i++) {
				aid = -Basic.tamvec(1,n,i,vec,g);
				s[i]=aid*Math.abs(v[i]);
				v[i]=((v[i] == 0.0) ? 0.0 : ((v[i] > 0.0) ? aid : -aid));
			}
			for (i=1; i<=n; i++) {
				delta[i]=Basic.matvec(1,n,i,vec,s);
				p[i]=Basic.matvec(1,n,i,vec,v);
			}
			dg0=Basic.vecvec(1,n,0,delta,g);
			nrmdelta=Math.sqrt(Basic.vecvec(1,n,0,delta,delta));
		}
		Basic.dupvec(1,n,0,s,x);
		Basic.dupvec(1,n,0,v,g);
		if (it > n)
			alfa=1.0;
		else {
			if (it != 1)
				alfa /= nrmdelta;
			else {
				alfa=2.0*(fmin-f)/dg0;
				if (alfa > 1.0) alfa=1.0;
			}
		}
		Basic.elmvec(1,n,0,x,delta,alfa);
		f0=f;
		f=method.funct(n,x,g);
		evl++;
		dg=Basic.vecvec(1,n,0,delta,g);
		if (it == 1 || f0-f < -mu*dg0*alfa) {
			/* line minimization */
			i=evlmax-evl;
			cntl++;
			xtmp1[0]=alfa;
			xtmp2[0]=f;
			xtmp3[0]=dg;
			itmp[0]=i;
			linemin(n,s,delta,nrmdelta,xtmp1,g,method,f0,xtmp2,
						  dg0,xtmp3,itmp,false,in);
			alfa=xtmp1[0];
			f=xtmp2[0];
			dg=xtmp3[0];
			i=itmp[0];
			evl += i;
			Basic.dupvec(1,n,0,x,s);
		}
		Basic.dupvec(1,n,0,gamma,g);
		Basic.elmvec(1,n,0,gamma,v,-1.0);
		if (!ok) Basic.mulvec(1,n,0,v,p,-1.0);
		dg -= dg0;
		if (alfa != 1.0) {
			Basic.mulvec(1,n,0,delta,delta,alfa);
			Basic.mulvec(1,n,0,v,v,alfa);
			nrmdelta *= alfa;
			dg *= alfa;
		}
		Basic.dupvec(1,n,0,p,gamma);
		Basic.elmvec(1,n,0,p,v,1.0);
		for (i=1; i<=n; i++) v[i]=Basic.symmatvec(1,n,i,h,gamma);
		Basic.dupvec(1,n,0,s,delta);
		Basic.elmvec(1,n,0,s,v,-1.0);
		gs=Basic.vecvec(1,n,0,gamma,s);
		ghg=Basic.vecvec(1,n,0,v,gamma);
		aid=dg/gs;
		temp1=Basic.vecvec(1,n,0,delta,p);
		temp2=orth*nrmdelta;
		if (temp1*temp1 > Basic.vecvec(1,n,0,p,p)*temp2*temp2)
			rnk1upd(h,n,s,1.0/gs);
		else
			if (aid >= 0.0)
				fleupd(h,n,delta,v,1.0/dg,(1.0+ghg/dg)/dg);
			else
				davupd(h,n,delta,v,1.0/dg,1.0/ghg);
		for (i=1; i<=n; i++) delta[i] = -Basic.symmatvec(1,n,i,h,g);
		alfa=nrmdelta;
		nrmdelta=Math.sqrt(Basic.vecvec(1,n,0,delta,delta));
		eps=Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol;
		dg=Math.sqrt(Basic.vecvec(1,n,0,g,g));
		dg0=Basic.vecvec(1,n,0,delta,g);
		ok = dg0 <= 0.0;
		it++;
	}
	out[0]=nrmdelta;
	out[1]=dg;
	out[2]=evl;
	out[3]=cntl;
	out[4]=cnte;
	return f;
}


public static double flemin(int n, double x[], double g[],
                     double h[], AP_linemin_method method,
                     double in[], double out[])
{
	int i,it,cntl,evl,evlmax;
	double f,f0,fmin,mu,dg,dg0,nrmdelta,alfa,reltol,abstol,
         eps,tolg,aid;
  double xtmp1[] = new double[1];
  double xtmp2[] = new double[1];
  double xtmp3[] = new double[1];
  int itmp[] = new int[1];  
	double v[] = new double[n+1];
	double delta[] = new double[n+1];
	double s[] = new double[n+1];
	
	reltol=in[1];
	abstol=in[2];
	mu=in[3];
	tolg=in[4];
	fmin=in[5];
	alfa=in[6];
	evlmax=(int)in[7];
	out[4]=0.0;
	it=0;
	f=method.funct(n,x,g);
	evl=1;
	cntl=0;
	if (alfa > 0.0) {
		Basic.inivec(1,(n*(n+1))/2,h,0.0);
		Basic.inisymd(1,n,0,h,alfa);
	}
	for (i=1; i<=n; i++) delta[i] = -Basic.symmatvec(1,n,i,h,g);
	dg=Math.sqrt(Basic.vecvec(1,n,0,g,g));
	nrmdelta=Math.sqrt(Basic.vecvec(1,n,0,delta,delta));
	eps=Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol;
	dg0=Basic.vecvec(1,n,0,delta,g);
	it++;
	while ((nrmdelta > eps || dg > tolg) && (evl < evlmax)) {
		Basic.dupvec(1,n,0,s,x);
		Basic.dupvec(1,n,0,v,g);
		if (it >= n)
			alfa=1.0;
		else {
			if (it != 1)
				alfa /= nrmdelta;
			else {
				alfa=2.0*(fmin-f)/dg0;
				if (alfa > 1.0) alfa=1.0;
			}
		}
		Basic.elmvec(1,n,0,x,delta,alfa);
		f0=f;
		f=method.funct(n,x,g);
		evl++;
		dg=Basic.vecvec(1,n,0,delta,g);
		if (it == 1 || f0-f < -mu*dg0*alfa) {
			/* line minimization */
			i=evlmax-evl;
			cntl++;
			xtmp1[0]=alfa;
			xtmp2[0]=f;
			xtmp3[0]=dg;
			itmp[0]=i;
			linemin(n,s,delta,nrmdelta,xtmp1,g,method,f0,xtmp2,
						  dg0,xtmp3,itmp,false,in);
			alfa=xtmp1[0];
			f=xtmp2[0];
			dg=xtmp3[0];
			i=itmp[0];
			evl += i;
			Basic.dupvec(1,n,0,x,s);
		}
		if (alfa != 1.0) Basic.mulvec(1,n,0,delta,delta,alfa);
		Basic.mulvec(1,n,0,v,v,-1.0);
		Basic.elmvec(1,n,0,v,g,1.0);
		for (i=1; i<=n; i++) s[i]=Basic.symmatvec(1,n,i,h,v);
		aid=Basic.vecvec(1,n,0,v,s);
		dg=(dg-dg0)*alfa;
		if (dg > 0.0)
			if (dg >= aid)
				fleupd(h,n,delta,s,1.0/dg,(1.0+aid/dg)/dg);
			else
				davupd(h,n,delta,s,1.0/dg,1.0/aid);
		for (i=1; i<=n; i++) delta[i] = -Basic.symmatvec(1,n,i,h,g);
		alfa *= nrmdelta;
		nrmdelta=Math.sqrt(Basic.vecvec(1,n,0,delta,delta));
		eps=Math.sqrt(Basic.vecvec(1,n,0,x,x))*reltol+abstol;
		dg=Math.sqrt(Basic.vecvec(1,n,0,g,g));
		dg0=Basic.vecvec(1,n,0,delta,g);
		if (dg0 > 0.0) {
			out[4] = -1.0;
			break;
		}
		it++;
	}
	out[0]=nrmdelta;
	out[1]=dg;
	out[2]=evl;
	out[3]=cntl;
	return f;
}


public static void marquardt(int m, int n, double par[],
                             double g[], double v[][],
                             AP_marquardt_methods method,
                             double in[], double out[])
{
  boolean emergency;
	int maxfe,fe,it,i,j,err;
	double vv,ww,w,mu,res,fpar,fparpres,lambda,lambdamin,p,pw,
         reltolres,abstolres,temp;
  double em[] = new double[8];
	double val[] = new double[n+1];
	double b[] = new double[n+1];
	double bb[] = new double[n+1];
	double parpres[] = new double[n+1];
	double jac[][] = new double[m+1][n+1];

  lambda=0.0;
	vv=10.0;
	w=0.5;
	mu=0.01;
	ww = (in[6] < 1.0e-7) ? 1.0e-8 : 1.0e-1*in[6];
	em[0]=em[2]=em[6]=in[0];
	em[4]=10*n;
	reltolres=in[3];
	abstolres=in[4]*in[4];
	maxfe=(int)in[5];
	err=0;
	fe=it=1;
	p=fpar=res=0.0;
	pw = -Math.log(ww*in[0])/2.30;
	if (!method.funct(m,n,par,g)) {
		err=3;
		out[4]=fe;
		out[5]=it-1;
		out[1]=err;
		return;
	}
	fpar=Basic.vecvec(1,m,0,g,g);
	out[3]=Math.sqrt(fpar);
	emergency=false;
	it=1;
	do {
		method.jacobian(m,n,par,g,jac);
		i=Linear_algebra.qrisngvaldec(jac,m,n,val,v,em);
		if (it == 1)
			lambda=in[6]*Basic.vecvec(1,n,0,val,val);
		else
			if (p == 0.0) lambda *= w;
		for (i=1; i<=n; i++) b[i]=val[i]*Basic.tamvec(1,m,i,jac,g);
		while (true) {
			for (i=1; i<=n; i++) bb[i]=b[i]/(val[i]*val[i]+lambda);
			for (i=1; i<=n; i++)
        parpres[i]=par[i]-Basic.matvec(1,n,i,v,bb);
			fe++;
			if (fe >= maxfe)
				err=1;
			else
				if (!method.funct(m,n,parpres,g)) err=2;
			if (err != 0) {
				emergency=true;
				break;
			}
			fparpres=Basic.vecvec(1,m,0,g,g);
			res=fpar-fparpres;
			if (res < mu*Basic.vecvec(1,n,0,b,bb)) {
				p += 1.0;
				lambda *= vv;
				if (p == 1.0) {
					lambdamin=ww*Basic.vecvec(1,n,0,val,val);
					if (lambda < lambdamin) lambda=lambdamin;
				}
				if (p >= pw) {
					err=4;
					emergency=true;
					break;
				}
			} else {
				Basic.dupvec(1,n,0,par,parpres);
				fpar=fparpres;
				break;
			}
		}
		if (emergency) break;
		it++;
	} while (fpar > abstolres && res > reltolres*fpar+abstolres);
	for (i=1; i<=n; i++)
    Basic.mulcol(1,n,i,i,jac,v,1.0/(val[i]+in[0]));
	for (i=1; i<=n; i++)
		for (j=1; j<=i; j++)
      v[i][j]=v[j][i]=Basic.mattam(1,n,i,j,jac,jac);
	lambda=lambdamin=val[1];
	for (i=2; i<=n; i++)
		if (val[i] > lambda)
			lambda=val[i];
		else
			if (val[i] < lambdamin) lambdamin=val[i];
	temp=lambda/(lambdamin+in[0]);
	out[7]=temp*temp;
	out[2]=Math.sqrt(fpar);
	out[6]=Math.sqrt(res+fpar)-out[2];
	out[4]=fe;
	out[5]=it-1;
	out[1]=err;
}


public static void gssnewton(int m, int n, double par[],
                             double rv[], double jjinv[][],
                             AP_gssnewton_methods method,
                             double in[], double out[])
{
	boolean dampingon,fail,testthf,conv;
	int i,j,inr,mit,text,it,itmax,inrmax,tim,feval,fevalmax;
	double rho,res1,res2,rn,reltolpar,abstolpar,abstolres,stap,normx;
  double aux[] = new double[6];
	int ci[] = new int[n+1];
	double pr[] = new double[n+1];
	double aid[] = new double[n+1];
	double sol[] = new double[n+1];
	double fu2[] = new double[m+1];
	double jac[][] = new double[m+2][n+1];
	
  res1=rho=0.0;
  conv=true;
	itmax=fevalmax=(int)in[5];
	aux[2]=n*in[0];
	tim=(int)in[7];
	reltolpar=in[1]*in[1];
	abstolpar=in[2]*in[2];
	abstolres=in[4]*in[4];
	inrmax=(int)in[6];
	Basic.dupvec(1,n,0,pr,par);
	if (m < n)
		for (i=1; i<=n; i++) jac[m+1][i]=0.0;
	text=4;
	mit=0;
	testthf=true;
	res2=stap=out[5]=out[6]=out[7]=0.0;
	method.funct(m,n,par,fu2);
	rn=Basic.vecvec(1,m,0,fu2,fu2);
	out[3]=Math.sqrt(rn);
	feval=1;
	dampingon=false;
	fail=false;
	it=1;
	do {
		out[5]=it;
		method.jacobian(m,n,par,fu2,jac);
		if (!testthf) {
			text=7;
			fail=true;
			break;
		}
		Linear_algebra.lsqortdec(jac,m,n,aux,aid,ci);
		if (aux[3] != n) {
			text=5;
			fail=true;
			break;
		}
		Linear_algebra.lsqsol(jac,m,n,aid,ci,fu2);
		Basic.dupvec(1,n,0,sol,fu2);
		stap=Basic.vecvec(1,n,0,sol,sol);
		rho=2.0;
		normx=Basic.vecvec(1,n,0,par,par);
		if (stap > reltolpar*normx+abstolpar || it == 1 && stap > 0.0) {
			inr=0;
			do {
				rho /= 2.0;
				if (inr > 0) {
					res1=res2;
					Basic.dupvec(1,m,0,rv,fu2);
					dampingon = inr > 1;
				}
				for (i=1; i<=n; i++) pr[i]=par[i]-sol[i]*rho;
				feval++;
				if (!method.funct(m,n,pr,fu2)) {
					text=6;
					fail=true;
					break;
				}
				res2=Basic.vecvec(1,m,0,fu2,fu2);
				conv = inr >= inrmax;
				inr++;
			} while ((inr == 1) ? (dampingon || res2 >= rn) :
						(!conv && (rn <= res1 || res2 < res1)));
			if (fail) break;
			if (conv) {
				mit++;
				if (mit < tim) conv=false;
			} else
				mit=0;
			if (inr > 1) {
				rho *= 2.0;
				Basic.elmvec(1,n,0,par,sol,-rho);
				rn=res1;
				if (inr > 2) out[7]=it;
			} else {
				Basic.dupvec(1,n,0,par,pr);
				rn=res2;
				Basic.dupvec(1,m,0,rv,fu2);
			}
			if (rn <= abstolres) {
				text=1;
				itmax=it;
			} else
				if (conv && inrmax > 0) {
					text=3;
					itmax=it;
				} else
					Basic.dupvec(1,m,0,fu2,rv);
		} else {
			text=2;
			rho=1.0;
			itmax=it;
		}
		it++;
	} while (it <= itmax && feval < fevalmax);
	if (!fail) {
		Linear_algebra.lsqinv(jac,n,aid,ci);
		for (i=1; i<=n; i++) {
			jjinv[i][i]=jac[i][i];
			for (j=i+1; j<=n; j++) jjinv[i][j]=jjinv[j][i]=jac[i][j];
		}
	}
	out[6]=Math.sqrt(stap)*rho;
	out[2]=Math.sqrt(rn);
	out[4]=feval;
	out[1]=text;
	out[8]=aux[3];
	out[9]=aux[5];
}


public static void rk1(double x[], double a, double b,
                       double y[], double ya, AP_rk1_method method,
                       double e[], double d[], boolean fi)
{
  boolean last,first,reject,test,ta,tb;
	double e1,e2,xl,yl,h,ind,hmin,absh,k0,k1,k2,k3,k4,k5,discr,
         tol,mu,mu1,fh,hl;

  last=true;
  hl=mu1=0.0;
	if (fi) {
		d[3]=a;
		d[4]=ya;
	}
	d[1]=0.0;
	xl=d[3];
	yl=d[4];
	if (fi) d[2]=b-d[3];
	absh=h=Math.abs(d[2]);
	if (b-xl < 0.0) h = -h;
	ind=Math.abs(b-xl);
	hmin=ind*e[1]+e[2];
	e1=e[1]/ind;
	e2=e[2]/ind;
	first=true;
	test=true;
	if (fi) {
		last=true;
		test=false;
	}
	while (true) {
		if (test) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (h > 0.0) ? hmin : -hmin;
				absh=hmin;
			}
			ta=(h >= b-xl);
			tb=(h >= 0.0);
			if ((ta && tb) || (!(ta || tb))) {
				d[2]=h;
				last=true;
				h=b-xl;
				absh=Math.abs(h);
			} else
				last=false;
		}
		test=true;
		x[0]=xl;
		y[0]=yl;
		k0=method.fxy(x,y)*h;
		x[0]=xl+h/4.5;
		y[0]=yl+k0/4.5;
		k1=method.fxy(x,y)*h;
		x[0]=xl+h/3.0;
		y[0]=yl+(k0+k1*3.0)/12.0;
		k2=method.fxy(x,y)*h;
		x[0]=xl+h*0.5;
		y[0]=yl+(k0+k2*3.0)/8.0;
		k3=method.fxy(x,y)*h;
		x[0]=xl+h*0.8;
		y[0]=yl+(k0*53.0-k1*135.0+k2*126.0+k3*56.0)/125.0;
		k4=method.fxy(x,y)*h;
		x[0] = (last ? b : xl+h);
		y[0]=yl+(k0*133.0-k1*378.0+k2*276.0+k3*112.0+k4*25.0)/168.0;
		k5=method.fxy(x,y)*h;
		discr=Math.abs(k0*21.0-k2*162.0+k3*224.0-k4*125.0+k5*42.0)/14.0;
		tol=Math.abs(k0)*e1+absh*e2;
		reject = discr > tol;
		mu=tol/(tol+discr)+0.45;
		if (reject) {
			if (absh <= hmin) {
				d[1] += 1.0;
				y[0]=yl;
				first=true;
				if (b == x[0]) break;
				xl = x[0];
				yl = y[0];
			} else
				h *= mu;
		} else {
			if (first) {
				first=false;
				hl=h;
				h *= mu;
			} else {
				fh=mu*h/hl+mu-mu1;
				hl=h;
				h *= fh;
			}
			mu1=mu;
			y[0]=yl+(-k0*63.0+k1*189.0-k2*36.0-k3*112.0+k4*50.0)/28.0;
			k5=method.fxy(x,y)*hl;
			y[0]=yl+(k0*35.0+k2*162.0+k4*125.0+k5*14.0)/336.0;
			if (b == x[0]) break;
			xl = x[0];
			yl = y[0];
		}
	}
	if (!last) d[2]=h;
	d[3] = x[0];
	d[4] = y[0];
}


public static void rke(double x[], double xe[], int n,
                       double y[], AP_rke_methods method,
                       double data[], boolean fi)
{
  boolean last,first,reject,test,ta,tb;
	int j;
	double xt,h,hmin,ind,hl,ht,absh,fhm,discr,tol,mu,mu1,fh,e1,e2;
	double k0[] = new double[n+1];
	double k1[] = new double[n+1];
	double k2[] = new double[n+1];
	double k3[] = new double[n+1];
	double k4[] = new double[n+1];
	
  last=true;
  hl=mu1=0.0;
	if (fi) {
		data[3]=xe[0]-x[0];
		data[4]=data[5]=data[6]=0.0;
	}
	absh=h=Math.abs(data[3]);
	if (xe[0] < x[0]) h = -h;
	ind=Math.abs(xe[0]-x[0]);
	hmin=ind*data[1]+data[2];
	e1=12.0*data[1]/ind;
	e2=12.0*data[2]/ind;
	first=true;
	reject=false;
	test=true;
	if (fi) {
		last=true;
		test=false;
	}
	while (true) {
		if (test) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (xe[0] == x[0]) ? 0.0 : ((xe[0] > x[0]) ? hmin : -hmin);
				absh=hmin;
			}
			ta=(h >= xe[0]-x[0]);
			tb=(h >= 0.0);
			if ((ta && tb) || (!(ta || tb))) {
				last=true;
				h=xe[0]-x[0];
				absh=Math.abs(h);
			} else
				last=false;
		}
		test=true;
		if (!reject) {
			for (j=1; j<=n; j++) k0[j]=y[j];
			method.der(n,x[0],k0);
		}
		ht=0.184262134833347*h;
		xt = x[0]+ht;
		for (j=1; j<=n; j++) k1[j]=k0[j]*ht+y[j];
		method.der(n,xt,k1);
		ht=0.690983005625053e-1*h;
		xt=4.0*ht+x[0];
		for (j=1; j<=n; j++) k2[j]=(3.0*k1[j]+k0[j])*ht+y[j];
		method.der(n,xt,k2);
		xt=0.5*h+x[0];
		ht=0.1875*h;
		for (j=1; j<=n; j++)
			k3[j]=((1.74535599249993*k2[j]-k1[j])*2.23606797749979+
						k0[j])*ht+y[j];
		method.der(n,xt,k3);
		xt=0.723606797749979*h+x[0];
		ht=0.4*h;
		for (j=1; j<=n; j++)
			k4[j]=(((0.517595468166681*k0[j]-k1[j])*0.927050983124840+
						k2[j])*1.46352549156242+k3[j])*ht+y[j];
		method.der(n,xt,k4);
		xt = (last ? xe[0] : x[0]+h);
		ht=2.0*h;
		for (j=1; j<=n; j++)
			k1[j]=((((2.0*k4[j]+k2[j])*0.412022659166595+k1[j])*
						2.23606797749979-k0[j])*0.375-k3[j])*ht+y[j];
		method.der(n,xt,k1);
		reject=false;
		fhm=0.0;
		for (j=1; j<=n; j++) {
			discr=Math.abs((1.6*k3[j]-k2[j]-k4[j])*5.0+k0[j]+k1[j]);
			tol=Math.abs(k0[j])*e1+e2;
			reject = (discr > tol || reject);
			fh=discr/tol;
			if (fh > fhm) fhm=fh;
		}
		mu=1.0/(1.0+fhm)+0.45;
		if (reject) {
			data[5] += 1.0;
			if (absh <= hmin) {
				data[6] += 1.0;
				hl=h;
				reject=false;
				first=true;
				data[3]=hl;
				data[4] += 1.0;
				x[0]=xt;
				method.out(n,x,xe,y,data);
				if (x[0] == xe[0]) break;
			} else
				h *= mu;
		} else {
			if (first) {
				first=false;
				hl=h;
				h *= mu;
			} else {
				fh=mu*h/hl+mu-mu1;
				hl=h;
				h *= fh;
			}
			mu1=mu;
			ht=hl/12.0;
			for (j=1; j<=n; j++)
				y[j]=((k2[j]+k4[j])*5.0+k0[j]+k1[j])*ht+y[j];
			data[3]=hl;
			data[4] += 1.0;
			x[0]=xt;
			method.out(n,x,xe,y,data);
			if (x[0] == xe[0]) break;
		}
	}
}


public static void rk4a(double x[], double xa,
                        AP_rk4a_methods method, double y[],
                        double ya, double e[], double d[],
                        boolean fi, boolean xdir, boolean pos)
{
  boolean extrapolate,iv,first,next,fir,rej,t;
	int i,ext;
	double fhm,absh,s,xl,cond0,s1,cond1,yl,hmin,h,zl,tol,hl,mu,
         mu1,fzero,c,fc,bb,fb,a,fa,dd,fd,fdb,fda,w,mb,m,p,q;
  double e1[] = new double[3];
  double k0[] = new double[1];
  double k1[] = new double[1];
  double k2[] = new double[1];
  double k3[] = new double[1];
  double k4[] = new double[1];
  double k5[] = new double[1];
  double discr[] = new double[1];

  mu=hl=mu1=cond0=fd=dd=0.0;
	if (fi) {
		d[3]=xa;
		d[4]=ya;
		d[0]=1.0;
	}
	d[1]=0.0;
	x[0]=xl=d[3];
	y[0]=yl=d[4];
	iv = d[0] > 0.0;
	first=fir=true;
	hmin=e[0]+e[1];
	h=e[2]+e[3];
	if (h < hmin) hmin=h;
	while (true) {
		zl=method.fxy(x,y);
		if (Math.abs(zl) <= 1.0) {
			if (!iv) {
				d[2] = h /= zl;
				d[0]=1.0;
				iv=first=true;
			}
			if (fir) {
				t=(((iv && xdir) || (!(iv || xdir))) ? h : h*zl) < 0.0;
				if (fi ? ((t && pos) || (!(t || pos))) : (h*d[2] < 0))
					h = -h;
			}
			i=1;
		} else {
			if (iv) {
				if (!fir) d[2] = h *= zl;
				d[0] = -1.0;
				iv=false;
				first=true;
			}
			if (fir) {
				h=e[0]+e[1];
				t=(((iv && xdir) || (!(iv || xdir))) ? h : h*zl) < 0.0;
				if (fi ? ((t && pos) || (!(t || pos))) : (h*d[2] < 0))
					h = -h;
			}
			i=1;
		}
		next=false;
		while (true) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (h == 0.0) ? 0.0 : ((h > 0.0) ? hmin : -hmin);
				absh=hmin;
			}
			if (iv) {
				rk4arkstep(x,xl,h,y,yl,zl,method,i,false,
                   k0,k1,k2,k3,k4,k5,discr,mu);
				tol=e[2]*Math.abs(k0[0])+e[3]*absh;
			} else {
				rk4arkstep(y,yl,h,x,xl,1.0/zl,method,i,true,
                   k0,k1,k2,k3,k4,k5,discr,mu);
				tol=e[0]*Math.abs(k0[0])+e[1]*absh;
			}
			rej = discr[0] > tol;
			mu=tol/(tol+discr[0])+0.45;
			if (!rej) break;
			if (absh <= hmin) {
				if (iv) {
					x[0]=xl+h;
					y[0]=yl+k0[0];
				} else {
					x[0]=xl+k0[0];
					y[0]=yl+h;
				}
				d[1] += 1.0;
				first=true;
				next=true;
				break;
			}
			h *= mu;
			i=0;
		}
		if (!next) {
			if (first) {
				first=fir;
				hl=h;
				h *= mu;
			} else {
				fhm=mu*h/hl+mu-mu1;
				hl=h;
				h *= fhm;
			}
			if (iv)
				rk4arkstep(x,xl,hl,y,yl,zl,method,2,false,
                   k0,k1,k2,k3,k4,k5,discr,mu);
			else
				rk4arkstep(y,yl,hl,x,xl,zl,method,2,true,
                   k0,k1,k2,k3,k4,k5,discr,mu);
			mu1=mu;
		}
		if (fir) {
			fir=false;
			cond0=method.b(x,y);
			if (!(fi || rej)) h=d[2];
		} else {
			d[2]=h;
			cond1=method.b(x,y);
			if (cond0*cond1 <= 0.0) break;
			cond0=cond1;
		}
		d[3]=xl=x[0];
		d[4]=yl=y[0];
	}
	e1[1]=e[4];
	e1[2]=e[5];
	s1 = iv ? x[0] : y[0];
	s = iv ? xl : yl;
	/* find zero */
	bb=s;
	if (iv) {
		if (s == xl)
			fzero=cond0;
		else
			if (s == s1)
				fzero=cond1;
			else {
				rk4arkstep(x,xl,s-xl,y,yl,zl,method,3,false,
                   k0,k1,k2,k3,k4,k5,discr,mu);
				fzero=method.b(x,y);
			}
	} else {
		if (s == yl)
			fzero=cond0;
		else
			if (s == s1)
				fzero=cond1;
			else {
				rk4arkstep(y,yl,s-yl,x,xl,zl,method,3,true,
                   k0,k1,k2,k3,k4,k5,discr,mu);
				fzero=method.b(x,y);
			}
	}
	fb=fzero;
	a=s=s1;
	if (iv) {
		if (s == xl)
			fzero=cond0;
		else
			if (s == s1)
				fzero=cond1;
			else {
				rk4arkstep(x,xl,s-xl,y,yl,zl,method,3,false,
                   k0,k1,k2,k3,k4,k5,discr,mu);
				fzero=method.b(x,y);
			}
	} else {
		if (s == yl)
			fzero=cond0;
		else
			if (s == s1)
				fzero=cond1;
			else {
				rk4arkstep(y,yl,s-yl,x,xl,zl,method,3,true,
                   k0,k1,k2,k3,k4,k5,discr,mu);
				fzero=method.b(x,y);
			}
	}
	fa=fzero;
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
			a=bb;
			fa=fb;
			bb=s=c;
			fb=fc;
			c=a;
			fc=fa;
		}
		tol=Math.abs(e1[1]*s)+Math.abs(e1[2]);
		m=(c+bb)*0.5;
		mb=m-bb;
		if (Math.abs(mb) > tol) {
			if (ext > 2)
				w=mb;
			else {
				if (mb == 0.0)
					tol=0.0;
				else
					if (mb < 0.0) tol = -tol;
				p=(bb-a)*fb;
				if (ext <= 1)
					q=fa-fb;
				else {
					fdb=(fd-fb)/(dd-bb);
					fda=(fd-fa)/(dd-a);
					p *= fda;
					q=fdb*fa-fda*fb;
				}
				if (p < 0.0) {
					p = -p;
					q = -q;
				}
				w=(p<Double.MIN_VALUE || p<=q*tol) ? tol :
                              ((p<mb*q) ? p/q : mb);
			}
			dd=a;
			fd=fa;
			a=bb;
			fa=fb;
			s = bb += w;
			if (iv) {
				if (s == xl)
					fzero=cond0;
				else
					if (s == s1)
						fzero=cond1;
					else {
						rk4arkstep(x,xl,s-xl,y,yl,zl,method,3,false,
                       k0,k1,k2,k3,k4,k5,discr,mu);
						fzero=method.b(x,y);
					}
			} else {
				if (s == yl)
					fzero=cond0;
				else
					if (s == s1)
						fzero=cond1;
					else {
						rk4arkstep(y,yl,s-yl,x,xl,zl,method,3,true,
                       k0,k1,k2,k3,k4,k5,discr,mu);
						fzero=method.b(x,y);
					}
			}
			fb=fzero;
			if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
				c=a;
				fc=fa;
				ext=0;
			} else
				ext = (w == mb) ? 0 : ext+1;
		} else
			break;
	}
	/* end of finding zero */
	s1 = iv ? x[0] : y[0];
	if (iv)
		rk4arkstep(x,xl,s-xl,y,yl,zl,method,3,false,
               k0,k1,k2,k3,k4,k5,discr,mu);
	else
		rk4arkstep(y,yl,s-yl,x,xl,zl,method,3,true,
               k0,k1,k2,k3,k4,k5,discr,mu);
	d[3]=x[0];
	d[4]=y[0];
}


static private void rk4arkstep(double x[], double xl, double h,
                    double y[], double yl, double zl,
                    AP_rk4a_methods method, int d, boolean invf,
                    double k0[], double k1[], double k2[],
                    double k3[], double k4[], double k5[],
                    double discr[], double mu)
{
	/* this function is internally used by RK4A */

	if (d != 2) {
		if (d == 3) {
			x[0]=xl;
			y[0]=yl;
			k0[0]=(invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
		} else
			if (d == 1)
				k0[0]=zl*h;
			else
				k0[0] *= mu;
		x[0]=xl+h/4.5;
		y[0]=yl+k0[0]/4.5;
		k1[0]=(invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
		x[0]=xl+h/3.0;
		y[0]=yl+(k0[0]+k1[0]*3.0)/12.0;
		k2[0]=(invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
		x[0]=xl+h*0.5;
		y[0]=yl+(k0[0]+k2[0]*3.0)/8.0;
		k3[0]=(invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
		x[0]=xl+h*0.8;
		y[0]=yl+(k0[0]*53.0-k1[0]*135.0+k2[0]*126.0+k3[0]*56.0)/125.0;
		k4[0]=(invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
		if (d <= 1) {
			x[0]=xl+h;
			y[0]=yl+(k0[0]*133.0-k1[0]*378.0+k2[0]*276.0+
					 k3[0]*112.0+k4[0]*25.0)/168.0;
			k5[0]=(invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
			discr[0]=Math.abs(k0[0]*21.0-k2[0]*162.0+k3[0]*224.0-
						   k4[0]*125.0+k5[0]*42.0)/14.0;
			return;
		}
	}
	x[0]=xl+h;
	y[0]=yl+(-k0[0]*63.0+k1[0]*189.0-k2[0]*36.0-
			 k3[0]*112.0+k4[0]*50.0)/28.0;
	k5[0] = (invf ? (1.0/method.fxy(x,y)) : method.fxy(x,y))*h;
	y[0]=yl+(k0[0]*35.0+k2[0]*162.0+k4[0]*125.0+k5[0]*14.0)/336.0;
}


public static void rk4na(double x[], double xa[], 
                         AP_rk4na_methods method,
                         double e[], double d[], boolean fi,
                         int n, int l, boolean pos)
{
  boolean extrapolate,rej,change,next,first,fir,t;
	int j,i,iv,iv0,ext;
	double h,cond0,cond1,fhm,absh,tol,fh,max,x0,x1,s,hmin,hl,mu,
         mu1,p,fzero,c,fc,bb,fb,a,fa,dd,fd,fdb,fda,w,mb,m,q;
  double e1[] = new double[3];
	double xl[] = new double[n+1];
  double discr[] = new double[n+1];
  double y[] = new double[n+1];
	double k[][] = new double[6][n+1];
	
  hmin=mu=hl=mu1=cond0=x0=fd=dd=0.0;
	if (fi) {
		for (i=0; i<=n; i++) d[i+3]=xa[i];
		d[0]=d[2]=0.0;
	}
	d[1]=0.0;
	for (i=0; i<=n; i++) x[i]=xl[i]=d[i+3];
	iv=(int)d[0];
	h=d[2];
	first=fir=true;
	y[0]=1.0;
	next=false;
	change=true;
	while (true) {
		if (!change) {
			while (true) {
				absh=Math.abs(h);
				if (absh < hmin) {
					h = (h > 0.0) ? hmin : -hmin;
					absh=Math.abs(h);
				}
				rk4narkstep(h,i,n,iv,mu,method,x,xl,y,discr,k);
				rej=false;
				fhm=0.0;
				for (i=0; i<=n; i++)
					if (i != iv) {
						tol=e[2*i]*Math.abs(k[0][i])+e[2*i+1]*absh;
						rej=(tol < discr[i] || rej);
						fh=discr[i]/tol;
						if (fh > fhm) fhm=fh;
					}
				mu=1.0/(1.0+fhm)+0.45;
				if (!rej) break;
				if (absh <= hmin) {
					for (i=0; i<=n; i++)
						if (i != iv)
							x[i]=xl[i]+k[0][i];
						else
							x[i]=xl[i]+h;
					d[1] += 1.0;
					first=true;
					next=true;
					break;
				}
				h *= mu;
				i=0;
			}
			if (!next) {
				if (first) {
					first=fir;
					hl=h;
					h *= mu;
				} else {
					fh=mu*h/hl+mu-mu1;
					hl=h;
					h *= fh;
				}
				rk4narkstep(hl,2,n,iv,mu,method,x,xl,y,discr,k);
				mu1=mu;
			}
			next=false;
			if (fir) {
				fir=false;
				cond0=method.b(n,x);
				if (!(fi || rej)) h=d[2];
			} else {
				d[2]=h;
				cond1=method.b(n,x);
				if (cond0*cond1 <= 0.0) break;
				cond0=cond1;
			}
			for (i=0; i<=n; i++) d[i+3]=xl[i]=x[i];
		}
		change=false;
		iv0=iv;
		for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
		max=Math.abs(y[iv]);
		for (i=0; i<=n; i++)
			if (Math.abs(y[i]) > max) {
				max=Math.abs(y[i]);
				iv=i;
			}
		if (iv0 != iv) {
			first=true;
			d[0]=iv;
			d[2]=h=y[iv]/y[iv0]*h;
		}
		x0=xl[iv];
		if (fir) {
			hmin=e[0]+e[1];
			for (i=1; i<=n; i++) {
				h=e[2*i]+e[2*i+1];
				if (h < hmin) hmin=h;
			}
			h=e[2*iv]+e[2*iv+1];
			t=y[l]/y[iv]*h < 0.0;
			if ((fi && ((t && pos) || !(t || pos))) ||
					(!fi && d[2]*h < 0.0)) h = -h;
		}
		i=1;
	}
	e1[1]=e[2*n+2];
	e1[2]=e[2*n+3];
	x1=x[iv];
	s=x0;
	/* find zero */
	bb=s;
	if (s == x0)
		fzero=cond0;
	else
		if (s == x1)
			fzero=cond1;
		else {
			rk4narkstep(s-xl[iv],3,n,iv,mu,method,x,xl,y,discr,k);
			fzero=method.b(n,x);
		}
	fb=fzero;
	a=s=x1;
	if (s == x0)
		fzero=cond0;
	else
		if (s == x1)
			fzero=cond1;
		else {
			rk4narkstep(s-xl[iv],3,n,iv,mu,method,x,xl,y,discr,k);
			fzero=method.b(n,x);
		}
	fa=fzero;
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
			a=bb;
			fa=fb;
			bb=s=c;
			fb=fc;
			c=a;
			fc=fa;
		}
		tol=Math.abs(e1[1]*s)+Math.abs(e1[2]);
		m=(c+bb)*0.5;
		mb=m-bb;
		if (Math.abs(mb) > tol) {
			if (ext > 2)
				w=mb;
			else {
				if (mb == 0.0)
					tol=0.0;
				else
					if (mb < 0.0) tol = -tol;
				p=(bb-a)*fb;
				if (ext <= 1)
					q=fa-fb;
				else {
					fdb=(fd-fb)/(dd-bb);
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
			a=bb;
			fa=fb;
			s = bb += w;
			if (s == x0)
				fzero=cond0;
			else
				if (s == x1)
					fzero=cond1;
				else {
					rk4narkstep(s-xl[iv],3,n,iv,mu,method,x,xl,y,discr,k);
					fzero=method.b(n,x);
				}
			fb=fzero;
			if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
				c=a;
				fc=fa;
				ext=0;
			} else
				ext = (w == mb) ? 0 : ext+1;
		} else
			break;
	}
	/* end of finding zero */
	x0=s;
	x1=x[iv];
	rk4narkstep(x0-xl[iv],3,n,iv,mu,method,x,xl,y,discr,k);
	for (i=0; i<=n; i++) d[i+3]=x[i];
}


static private void rk4narkstep(double h, int d, int n,
                    int iv, double mu, AP_rk4na_methods method,
                    double x[], double xl[], double y[],
                    double discr[], double k[][])
{
	/* this function is internally used by RK4NA */

	int i,j;
	double p;

	if (d != 2) {
		if (d == 3) {
			for (i=0; i<=n; i++) x[i]=xl[i];
			for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
			p=h/y[iv];
			for (i=0; i<=n; i++)
				if (i != iv) k[0][i]=y[i]*p;
		} else
			if (d == 1) {
				p=h/y[iv];
				for (i=0; i<=n; i++)
					if (i != iv) k[0][i]=p*y[i];
			} else
				for (i=0; i<=n; i++)
					if (i != iv) k[0][i] *= mu;
		for (i=0; i<=n; i++)
			x[i]=xl[i]+((i == iv) ? h : k[0][i])/4.5;
		for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
		p=h/y[iv];
		for (i=0; i<=n; i++)
			if (i != iv) k[1][i]=y[i]*p;
		for (i=0; i<=n; i++)
			x[i]=xl[i]+((i == iv) ? h*4.0 : (k[0][i]+k[1][i]*3.0))/12.0;
		for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
		p=h/y[iv];
		for (i=0; i<=n; i++)
			if (i != iv) k[2][i]=y[i]*p;
		for (i=0; i<=n; i++)
			x[i]=xl[i]+
					((i == iv) ? h*0.5 : (k[0][i]+k[2][i]*3.0)/8.0);
		for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
		p=h/y[iv];
		for (i=0; i<=n; i++)
			if (i != iv) k[3][i]=y[i]*p;
		for (i=0; i<=n; i++)
			x[i]=xl[i]+((i == iv) ? h*0.8 : (k[0][i]*53.0-
				k[1][i]*135.0+k[2][i]*126.0+k[3][i]*56.0)/125.0);
		for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
		p=h/y[iv];
		for (i=0; i<=n; i++)
			if (i != iv) k[4][i]=y[i]*p;
		if (d <= 1) {
			for (i=0; i<=n; i++)
				x[i]=xl[i]+((i == iv) ? h : (k[0][i]*133.0-
					k[1][i]*378.0+k[2][i]*276.0+k[3][i]*112.0+
					k[4][i]*25.0)/168.0);
			for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
			p=h/y[iv];
			for (i=0; i<=n; i++)
				if (i != iv) k[5][i]=y[i]*p;
			for (i=0; i<=n; i++)
				if (i != iv)
					discr[i]=Math.abs(k[0][i]*21.0-k[2][i]*162.0+
                   k[3][i]*224.0-k[4][i]*125.0+k[5][i]*42.0)/14.0;
			return;
		}
	}
	for (i=0; i<=n; i++)
		x[i]=xl[i]+((i == iv) ? h :
			(-k[0][i]*63.0+k[1][i]*189.0-k[2][i]*36.0-k[3][i]*112.0+
				k[4][i]*50.0)/28.0);
	for (j=1; j<=n; j++) y[j]=method.fxj(n,j,x);
	p=h/y[iv];
	for (i=0; i<=n; i++)
		if (i != iv) k[5][i]=y[i]*p;
	for (i=0; i<=n; i++)
		if (i != iv)
			x[i]=xl[i]+(k[0][i]*35.0+k[2][i]*162.0+k[4][i]*125.0+
           k[5][i]*14.0)/336.0;
}


public static void rk5na(double x[], double xa[],
                         AP_rk5na_methods method, double e[],
                         double d[], boolean fi, int n,
                         int l, boolean pos)
{
  boolean extrapolate,first,fir,rej,t;
	int j,i,ext;
	double fhm,s,s0,cond0,s1,cond1,h,absh,tol,fh,hl,mu,mu1,
			   fzero,c,fc,bb,fb,a,fa,dd,fd,fdb,fda,w,mb,m,p,q;
  double e1[] = new double[3];
	double y[] = new double[n+1];
	double xl[] = new double[n+1];
	double discr[] = new double[n+1];
	double k[][] = new double[6][n+1];
	
  mu=hl=mu1=cond0=s0=fd=dd=0.0;
	if (fi) {
		for (i=0; i<=n; i++) d[i+3]=xa[i];
		d[1]=d[2]=0.0;
	}
	for (i=0; i<=n; i++) x[i]=xl[i]=d[i+3];
	s=d[1];
	first=fir=true;
	h=e[0]+e[1];
	for (i=1; i<=n; i++) {
		absh=e[2*i]+e[2*i+1];
		if (h > absh) h=absh;
	}
	if (fi) {
		j=l;
		t=method.fxj(n,j,x)*h < 0.0;
		if ((t && pos) || !(t || pos)) h = -h;
	} else
		if (d[2]*h < 0.0) h = -h;
	i=0;
	while (true) {
		rk5narkstep(h,i,n,mu,method,x,xl,y,discr,k);
		rej=false;
		fhm=0.0;
		absh=Math.abs(h);
		for (i=0; i<=n; i++) {
			tol=e[2*i]*Math.abs(k[0][i])+e[2*i+1]*absh;
			rej=(tol < discr[i] || rej);
			fh=discr[i]/tol;
			if (fh > fhm) fhm=fh;
		}
		mu=1.0/(1.0+fhm)+0.45;
		if (rej) {
			h *= mu;
			i=1;
		} else {
			if (first) {
				first=fir;
				hl=h;
				h *= mu;
			} else {
				fh=mu*h/hl+mu-mu1;
				hl=h;
				h *= fh;
			}
			rk5narkstep(hl,2,n,mu,method,x,xl,y,discr,k);
			mu1=mu;
			s += hl;
			if (fir) {
				cond0=method.b(n,x);
				fir=false;
				if (!fi) h=d[2];
			} else {
				d[2]=h;
				cond1=method.b(n,x);
				if (cond0*cond1 <= 0.0) break;
				cond0=cond1;
			}
			for (i=0; i<=n; i++) d[i+3]=xl[i]=x[i];
			d[1]=s0=s;
			i=0;
		}
	}
	e1[1]=e[2*n+2];
	e1[2]=e[2*n+3];
	s1=s;
	s=s0;
	/* find zero */
	bb=s;
	if (s == s0)
		fzero=cond0;
	else
		if (s == s1)
			fzero=cond1;
		else {
			rk5narkstep(s-s0,3,n,mu,method,x,xl,y,discr,k);
			fzero=method.b(n,x);
		}
	fb=fzero;
	a=s=s1;
	if (s == s0)
		fzero=cond0;
	else
		if (s == s1)
			fzero=cond1;
		else {
			rk5narkstep(s-s0,3,n,mu,method,x,xl,y,discr,k);
			fzero=method.b(n,x);
		}
	fa=fzero;
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
			a=bb;
			fa=fb;
			bb=s=c;
			fb=fc;
			c=a;
			fc=fa;
		}
		tol=Math.abs(e1[1]*s)+Math.abs(e1[2]);
		m=(c+bb)*0.5;
		mb=m-bb;
		if (Math.abs(mb) > tol) {
			if (ext > 2)
				w=mb;
			else {
				if (mb == 0.0)
					tol=0.0;
				else
					if (mb < 0.0) tol = -tol;
				p=(bb-a)*fb;
				if (ext <= 1)
					q=fa-fb;
				else {
					fdb=(fd-fb)/(dd-bb);
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
			a=bb;
			fa=fb;
			s = bb += w;
			if (s == s0)
				fzero=cond0;
			else
				if (s == s1)
					fzero=cond1;
				else {
					rk5narkstep(s-s0,3,n,mu,method,x,xl,y,discr,k);
					fzero=method.b(n,x);
				}
			fb=fzero;
			if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
				c=a;
				fc=fa;
				ext=0;
			} else
				ext = (w == mb) ? 0 : ext+1;
		} else
			break;
	}
	/* end of finding zero */
	rk5narkstep(s-s0,3,n,mu,method,x,xl,y,discr,k);
	for (i=0; i<=n; i++) d[i+3]=x[i];
	d[1]=s;
}


static private void rk5narkstep(double h, int d, int n,
                    double mu, AP_rk5na_methods method,
                    double x[], double xl[], double y[],
                    double discr[], double k[][])
{
	/* this function is internally used by RK5NA */

	int i,j;
	double p,s;

	if (d != 2) {
		if (d == 1)
			for (i=0; i<=n; i++) k[0][i] *= mu;
		else {
			for (i=0; i<=n; i++) x[i]=xl[i];
			for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
			s=0.0;
			for (j=0; j<=n; j++) s += y[j]*y[j];
			p=h/Math.sqrt(s);
			for (i=0; i<=n; i++) k[0][i]=y[i]*p;
		}
		for (i=0; i<=n; i++)	x[i]=xl[i]+k[0][i]/4.5;
		for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
		s=0.0;
		for (j=0; j<=n; j++) s += y[j]*y[j];
		p=h/Math.sqrt(s);
		for (i=0; i<=n; i++) k[1][i]=y[i]*p;
		for (i=0; i<=n; i++)	x[i]=xl[i]+(k[0][i]+k[1][i]*3.0)/12.0;
		for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
		s=0.0;
		for (j=0; j<=n; j++) s += y[j]*y[j];
		p=h/Math.sqrt(s);
		for (i=0; i<=n; i++) k[2][i]=y[i]*p;
		for (i=0; i<=n; i++)	x[i]=xl[i]+(k[0][i]+k[2][i]*3.0)/8.0;
		for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
		s=0.0;
		for (j=0; j<=n; j++) s += y[j]*y[j];
		p=h/Math.sqrt(s);
		for (i=0; i<=n; i++) k[3][i]=y[i]*p;
		for (i=0; i<=n; i++)
			x[i]=xl[i]+(k[0][i]*53.0-k[1][i]*135.0+k[2][i]*126.0+
							k[3][i]*56.0)/125.0;
		for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
		s=0.0;
		for (j=0; j<=n; j++) s += y[j]*y[j];
		p=h/Math.sqrt(s);
		for (i=0; i<=n; i++) k[4][i]=y[i]*p;
		if (d <= 1) {
			for (i=0; i<=n; i++)
				x[i]=xl[i]+(k[0][i]*133.0-k[1][i]*378.0+k[2][i]*276.0+
								k[3][i]*112.0+k[4][i]*25.0)/168.0;
			for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
			s=0.0;
			for (j=0; j<=n; j++) s += y[j]*y[j];
			p=h/Math.sqrt(s);
			for (i=0; i<=n; i++) k[5][i]=y[i]*p;
			for (i=0; i<=n; i++)
				discr[i]=Math.abs(k[0][i]*21.0-k[2][i]*162.0+
                 k[3][i]*224.0-k[4][i]*125.0+k[5][i]*42.0)/14.0;
			return;
		}
	}
	for (i=0; i<=n; i++)
		x[i]=xl[i]+(-k[0][i]*63.0+k[1][i]*189.0-k[2][i]*36.0-
         k[3][i]*112.0+k[4][i]*50.0)/28.0;
	for (j=0; j<=n; j++) y[j]=method.fxj(n,j,x);
	s=0.0;
	for (j=0; j<=n; j++) s += y[j]*y[j];
	p=h/Math.sqrt(s);
	for (i=0; i<=n; i++) k[5][i]=y[i]*p;
	for (i=0; i<=n; i++)
		x[i]=xl[i]+(k[0][i]*35.0+k[2][i]*162.0+k[4][i]*125.0+
         k[5][i]*14.0)/336.0;
}


public static boolean multistep(double x[], double xend, double y[],
              double hmin, double hmax, double ymax[], double eps,
              boolean first[], double save[],
              AP_multistep_methods method, double jacobian[][],
              boolean stiff, int n,
              boolean btmp[], int itmp[], double xtmp[])
{
	double adams1[] = {1.0, 1.0, 144.0, 4.0, 0.0, 0.5, 1.0,
		0.5, 576.0, 144.0, 1.0, 5.0/12.0, 1.0, 0.75, 1.0/6.0, 1436.0,
		576.0, 4.0, 0.375, 1.0, 11.0/12.0, 1.0/3.0, 1.0/24.0, 2844.0,
		1436.0, 1.0, 251.0/720.0, 1.0, 25.0/24.0, 35.0/72.0, 5.0/48.0,
		1.0/120.0, 0.0, 2844.0, 0.1};
	double adams2[] = {1.0, 1.0, 9.0, 4.0, 0.0, 2.0/3.0, 1.0,
		1.0/3.0, 36.0, 20.25, 1.0, 6.0/11.0, 1.0, 6.0/11.0, 1.0/11.0,
		84.028, 53.778, 0.25, 0.48, 1.0, 0.7, 0.2, 0.02, 156.25, 108.51,
		0.027778, 120.0/274.0, 1.0, 225.0/274.0, 85.0/274.0, 15.0/274.0,
		1.0/274.0, 0.0, 187.69, 0.0047361};
  boolean adams,withjacobian,conv;
	int m,same,kold;
	double xold,hold;
	boolean newstart,firsttime,trycurtiss,errortestok;
	int i,j,l,k,fails;
	double error,dfi,ss,aa;
  double a[] = new double[6];
  double aux[] = new double[4];
  double a0[] = new double[1];
  double tolup[] = new double[1];
  double tol[] = new double[1];
  double toldwn[] = new double[1];
  double tolconv[] = new double[1];
  int knew[] = new int[1];
  double c[] = new double[1];
  double h[] = new double[1];
  double ch[] = new double[1];
  double chnew[] = new double[1];
  
	int p[] = new int[n+1];
	double delta[] = new double[n+1];
  double lastdelta[] = new double[n+1];
  double df[] = new double[n+1];
  double fixy[] = new double[n+1];
  double fixdy[] = new double[n+1];
  double dy[] = new double[n+1];
	double jac[][] = new double[n+1][n+1];
  boolean evaluate[] = new boolean[1];
  boolean evaluated[] = new boolean[1];
  boolean decompose[] = new boolean[1];
  boolean decomposed[] = new boolean[1];

  adams=btmp[0];
  withjacobian=btmp[1];
  m=itmp[0];
  same=itmp[1];
  kold=itmp[2];
  xold=xtmp[0];
  hold=xtmp[1];
  a0[0]=xtmp[2];
  tolup[0]=xtmp[3];
  tol[0]=xtmp[4];
  toldwn[0]=xtmp[5];
  tolconv[0]=xtmp[6];
  k=0;
  error=0.0;
  conv=true;
  newstart=true;
  trycurtiss=false;
  errortestok=true;
	if (first[0]) {
    firsttime=true;
		first[0] = false;
		m=n;
		save[37]=save[36]=save[35]=0.0;
		method.out(0.0,0,n,x,y);
		adams=(!stiff);
		withjacobian=(!adams);
		if (withjacobian)
			multistepjacobian(n,x,y,eps,fixy,fixdy,dy,jacobian,
                        method,evaluate,decompose,evaluated);
		if (adams)
			for (j=0; j<=34; j++) save[j]=adams1[j];
		else
			for (j=0; j<=34; j++) save[j]=adams2[j];
	} else {
    firsttime=false;
		withjacobian=(!adams);
		ch[0]=1.0;
		k=kold;
		multistepreset(y,save,x,ch,c,h,decomposed,hmin,hmax,
							     hold,xold,m,k,n);
		multisteporder(a,save,tolup,tol,toldwn,tolconv,
							     a0,decompose,eps,k,n);
		decompose[0]=withjacobian;
	}
		
  while (newstart) {
    newstart=false;
    if (firsttime) {
      firsttime=false;
      k=1;
      same=2;
      multisteporder(a,save,tolup,tol,toldwn,tolconv,
                     a0,decompose,eps,k,n);
      method.deriv(df,n,x,y);
      if (!withjacobian)
        h[0]=hmin;
      else {
        ss=Double.MIN_VALUE;
        for (i=1; i<=n; i++) {
          aa=Basic.matvec(1,n,i,jacobian,df)/ymax[i];
          ss += aa*aa;
        }
        h[0]=Math.sqrt(2.0*eps/Math.sqrt(ss));
      }
      if (h[0] > hmax)
        h[0]=hmax;
      else
        if (h[0] < hmin) h[0]=hmin;
      xold=x[0];
      hold=h[0];
      kold=k;
      ch[0]=1.0;
      for (i=1; i<=n; i++) {
        save[i+38]=y[i];
        save[m+i+38]=y[m+i]=df[i]*h[0];
      }
      method.out(0.0,0,n,x,y);
    }
  	fails=0;
  	while (x[0] < xend) {
  		if (x[0]+h[0] <= xend)
  			x[0] += h[0];
  		else {
  			h[0]=xend-x[0];
  			x[0] = xend;
  			ch[0]=h[0]/hold;
  			c[0]=1.0;
  			for (j=m; j<=k*m; j+=m) {
  				c[0] *= ch[0];
  				for (i=j+1; i<=j+n; i++) y[i] *= c[0];
  			}
  			same = ((same < 3) ? 3 : same+1);
  		}
  		/* prediction */
  		for (l=1; l<=n; l++) {
  			for (i=l; i<=(k-1)*m+l; i+=m)
  				for (j=(k-1)*m+l; j>=i; j-=m) y[j] += y[j+m];
  			delta[l]=0.0;
  		}
  		evaluated[0]=false;
  		/* correction and estimation local error */
  		for (l=1; l<=3; l++) {
  			method.deriv(df,n,x,y);
  			for (i=1; i<=n; i++) df[i]=df[i]*h[0]-y[m+i];
  			if (withjacobian) {
  				if (evaluate[0])
  					multistepjacobian(n,x,y,eps,fixy,fixdy,dy,jacobian,
  						            method,evaluate,decompose,evaluated);
  				if (decompose[0]) {
  					/* decompose jacobian */
	  				decompose[0]=false;
		  			decomposed[0]=true;
			  		c[0] = -a0[0]*h[0];
				  	for (j=1; j<=n; j++) {
					  	for (i=1; i<=n; i++) jac[i][j]=jacobian[i][j]*c[0];
  						jac[j][j] += 1.0;
	  				}
		  			aux[2]=Double.MIN_VALUE;
            Linear_algebra.dec(jac,n,aux,p);
  				}
	  			Linear_algebra.sol(jac,n,p,df);
		  	}
  			conv=true;
	  		for (i=1; i<=n; i++) {
		  		dfi=df[i];
			  	y[i] += a0[0]*dfi;
				  y[m+i] += dfi;
  				delta[i] += dfi;
	  			conv=(conv && (Math.abs(dfi) < tolconv[0]*ymax[i]));
		  	}
			  if (conv) {
				  ss=Double.MIN_VALUE;
  				for (i=1; i<=n; i++) {
	  				aa=delta[i]/ymax[i];
		  			ss += aa*aa;
			  	}
  				error=ss;
	  			break;
		  	}
  		}
	  	/* acceptance or rejection */
		  if (!conv) {
			  if (!withjacobian) {
				  evaluate[0]=withjacobian=
                      ((same >= k) || (h[0] < 1.1*hmin));
	  			if (!withjacobian) ch[0] /= 4.0;
  			}
	  		else if (!decomposed[0]) decompose[0]=true;
		  	else if (!evaluated[0]) evaluate[0]=true;
			  else if (h[0] > 1.1*hmin) ch[0] /= 4.0;
  			else if (adams) {
          trycurtiss=true;
          adams=false;
          for (j=0; j<=34; j++) save[j]=adams2[j];
          k=kold=1;
          multistepreset(y,save,x,ch,c,h,decomposed,
                         hmin,hmax,hold,xold,m,k,n);
          multisteporder(a,save,tolup,tol,toldwn,
                         tolconv,a0,decompose,eps,k,n);
          same=2;
  			}
	  		else {
		  		save[37]=1.0;
			  	break;
  			}
        if (!trycurtiss)
          multistepreset(y,save,x,ch,c,h,decomposed,hmin,hmax,
                         hold,xold,m,k,n);
        trycurtiss=false;
      } else {
	  		if (error > tol[0]) {
          errortestok=false;
  				fails++;
	  			if (h[0] > 1.1*hmin) {
		  			if (fails > 2) {
			  			if (adams) {
  							adams=false;
	  						for (j=0; j<=34; j++) save[j]=adams2[j];
		  				}
			  			kold=0;
				  		multistepreset(y,save,x,ch,c,h,decomposed,
                             hmin,hmax,hold,xold,m,k,n);
              newstart=true;
  					} else {
	  					multistepstep(knew,chnew,tolup,tol,toldwn,delta,
                            error,lastdelta,y,ymax,fails,m,k,n);
			  			if (knew[0] != k) {
				  			k=knew[0];
					  		multisteporder(a,save,tolup,tol,toldwn,
						  				         tolconv,a0,decompose,eps,k,n);
  						}
	  					ch[0] *= chnew[0];
		  				multistepreset(y,save,x,ch,c,h,decomposed,
                             hmin,hmax,hold,xold,m,k,n);
				  	}
  				} else {
	  				if (adams) {
			  			adams=false;
				  		for (j=0; j<=34; j++) save[j]=adams2[j];
  					} else {
	  					if (k == 1) {
		  					/* violate eps criterion */
			  				c[0]=eps*Math.sqrt(error/tol[0]);
				  			if (c[0] > save[35]) save[35]=c[0];
					  		save[36] += 1.0;
						  	same=4;
                errortestok=true;
  						}
  				  }
            if(!errortestok) {
              k=kold=1;
              multistepreset(y,save,x,ch,c,h,decomposed,
                             hmin,hmax,hold,xold,m,k,n);
              multisteporder(a,save,tolup,tol,toldwn,
                             tolconv,a0,decompose,eps,k,n);
              same=2;
            }
	  			}
		  	}
        if (errortestok) {
  				fails=0;
	  			for (i=1; i<=n; i++) {
		  			c[0]=delta[i];
			  		for (l=2; l<=k; l++) y[l*m+i] += a[l]*c[0];
				  	if (Math.abs(y[i]) > ymax[i]) ymax[i]=Math.abs(y[i]);
  				}
	  			same--;
		  		if (same == 1)
			  		for (i=1; i<=n; i++) lastdelta[i]=delta[i];
				  else
  					if (same == 0) {
	  					multistepstep(knew,chnew,tolup,tol,toldwn,delta,
                            error,lastdelta,y,ymax,fails,m,k,n);
			  			if (chnew[0] > 1.1) {
				  			decomposed[0]=false;
					  		if (k != knew[0]) {
						  		if (knew[0] > k)
  									for (i=1; i<=n; i++)
	  									y[knew[0]*m+i]=delta[i]*a[k]/knew[0];
		  						k=knew[0];
			  					multisteporder(a,save,tolup,tol,toldwn,
				  							         tolconv,a0,decompose,eps,k,n);
  							}
	  						same=k+1;
		  					if (chnew[0]*h[0] > hmax) chnew[0]=hmax/h[0];
			  				h[0] *= chnew[0];
				  			c[0]=1.0;
					  		for (j=m; j<=k*m; j+=m) {
  								c[0] *= chnew[0];
	  							for (i=j+1; i<=j+n; i++) y[i] *= c[0];
		  					}
			  			} else
				  			same=10;
  					}
	  			if (x[0] != xend) {
		  			xold=x[0];
			  		hold=h[0];
				  	kold=k;
					  ch[0]=1.0;
  					for (i=k*m+n; i>=1; i--) save[i+38]=y[i];
	  				method.out(h[0],k,n,x,y);
		  		}
        }
        errortestok=true;
      }
    }  /* while (x[0] < xend) loop  */
  }   /*  while (newstart) loop  */
  btmp[0]=adams;
  btmp[1]=withjacobian;
  itmp[0]=m;
  itmp[1]=same;
  itmp[2]=kold;
  xtmp[0]=xold;
  xtmp[1]=hold;
  xtmp[2]=a0[0];
  xtmp[3]=tolup[0];
  xtmp[4]=tol[0];
  xtmp[5]=toldwn[0];
  xtmp[6]=tolconv[0];  
	save[38]=(adams ? 0.0 : 1.0);
	return ((save[37] == 0.0) && (save[36] == 0.0));
}


static private void multistepreset(double y[], double save[],
               double x[], double ch[], double c[], double h[],
               boolean decomposed[], double hmin, double hmax,
			         double hold, double xold, int m, int k, int n)
{
	/* this function is internally used by MULTISTEP */

	int i,j;

	if (ch[0] < hmin/hold)
		ch[0] = hmin/hold;
	else
		if (ch[0] > hmax/hold) ch[0] = hmax/hold;
	x[0] = xold;
	h[0] = hold*(ch[0]);
	c[0] = 1.0;
	for (j=0; j<=k*m; j+=m) {
		for (i=1; i<=n; i++) y[j+i]=save[j+i+38]*c[0];
		c[0] *= ch[0];
	}
	decomposed[0] = false;
}


static private void multisteporder(double a[], double save[],
               double tolup[], double tol[], double toldwn[],
               double tolconv[], double a0[], boolean decompose[],
               double eps, int k, int n)
{
	/* this function is internally used by MULTISTEP */

	int i,j;
	double c;

	c=eps*eps;
	j=(k-1)*(k+8)/2-38;
	for (i=0; i<=k; i++) a[i]=save[i+j+38];
	tolup[0] = c*save[j+k+39];
	tol[0] = c*save[j+k+40];
	toldwn[0] = c*save[j+k+41];
	tolconv[0] = eps/(2*n*(k+2));
	a0[0] = a[0];
	decompose[0] = true;
}


static private void multistepstep(int knew[], double chnew[],
               double tolup[], double tol[], double toldwn[],
               double delta[], double error, double lastdelta[],
               double y[], double ymax[], int fails, int m,
               int k, int n)
{
	/* this function is internally used by MULTISTEP */

	int i;
	double a1,a2,a3,aa,ss;

	if (k <= 1)
		a1=0.0;
	else {
		ss=Double.MIN_VALUE;
		for (i=1; i<=n; i++) {
			aa=y[k*m+i]/ymax[i];
			ss += aa*aa;
		}
		a1=0.75*Math.pow(toldwn[0]/ss,0.5/k);
	}
	a2=0.80*Math.pow(tol[0]/error,0.5/(k+1));
	if (k >= 5 || fails != 0)
		a3=0.0;
	else {
		ss=Double.MIN_VALUE;
		for (i=1; i<=n; i++) {
			aa=(delta[i]-lastdelta[i])/ymax[i];
			ss += aa*aa;
		}
		a3=0.70*Math.pow(tolup[0]/ss,0.5/(k+2));
	}
	if (a1 > a2 && a1 > a3) {
		knew[0] = k-1;
		chnew[0] = a1;
	} else
		if (a2 > a3) {
			knew[0] = k;
			chnew[0] = a2;
		} else {
			knew[0] = k+1;
			chnew[0] = a3;
		}
}


static private void multistepjacobian(int n, double x[],
               double y[], double eps, double fixy[],
               double fixdy[], double dy[],
               double jacobian[][],
               AP_multistep_methods method,
               boolean evaluate[], boolean decompose[],
               boolean evaluated[])
{
	/* this function is internally used by MULTISTEP */

	int i,j;
	double d;

	evaluate[0] = false;
	decompose[0] = evaluated[0] = true;
	if (!method.available(n,x,y,jacobian)) {
		for (i=1; i<=n; i++) fixy[i]=y[i];
		method.deriv(fixdy,n,x,y);
		for (j=1; j<=n; j++) {
			d=((eps > Math.abs(fixy[j])) ? eps*eps : 
			                  eps*Math.abs(fixy[j]));
			y[j] += d;
			method.deriv(dy,n,x,y);
			for (i=1; i<=n; i++) jacobian[i][j]=(dy[i]-fixdy[i])/d;
			y[j]=fixy[j];
		}
	}
}


public static void diffsys(double x[], double xe, int n,
              double y[], AP_diffsys_methods method,
              double aeta, double reta, double s[], double h0)
{
  boolean bh,last,next,b0,konv;
	int i,j,k,kk,jj,l,m,r,sr;
	double a,b,b1,c,g,h,u,v,ta,fc;
	double d[] = new double[7];
	double ya[] = new double[n+1];
  double yl[] = new double[n+1];
  double ym[] = new double[n+1];
  double dy[] = new double[n+1];
  double dz[] = new double[n+1];
	double dt[][] = new double[n+1][7];
	double yg[][] = new double[8][n+1];
  double yh[][] = new double[8][n+1];

	last=false;
	h=h0;
	do {
		next=false;
		if (h*1.1 >= xe-x[0]) {
			last=true;
			h0=h;
			h=xe-x[0]+Double.MIN_VALUE;
		}
		method.derivative(n,x[0],y,dz);
		bh=false;
		for (i=1; i<=n; i++) ya[i]=y[i];
		while (true) {
			a=h+x[0];
			fc=1.5;
			b0=false;
			m=1;
			r=2;
			sr=3;
			jj = -1;
			for (j=0; j<=9; j++) {
				if (b0) {
					d[1]=16.0/9.0;
					d[3]=64.0/9.0;
					d[5]=256.0/9.0;
				} else {
					d[1]=9.0/4.0;
					d[3]=9.0;
					d[5]=36.0;
				}
				konv=true;
				if (j > 6) {
					l=6;
					d[6]=64.0;
					fc *= 0.6;
				} else {
					l=j;
					d[l]=m*m;
				}
				m *= 2;
				g=h/m;
				b=g*2.0;
				if (bh && j < 8)
					for (i=1; i<=n; i++) {
						ym[i]=yh[j][i];
						yl[i]=yg[j][i];
					}
				else {
					kk=(m-2)/2;
					m--;
					for (i=1; i<=n; i++) {
						yl[i]=ya[i];
						ym[i]=ya[i]+g*dz[i];
					}
					for (k=1; k<=m; k++) {
						method.derivative(n,x[0]+k*g,ym,dy);
						for (i=1; i<=n; i++) {
							u=yl[i]+b*dy[i];
							yl[i]=ym[i];
							ym[i]=u;
							u=Math.abs(u);
							if (u > s[i]) s[i]=u;
						}
						if (k == kk && k != 2) {
							jj++;
							for (i=1; i<=n; i++) {
								yh[jj][i]=ym[i];
								yg[jj][i]=yl[i];
							}
						}
					}
				}
				method.derivative(n,a,ym,dy);
				for (i=1; i<=n; i++) {
					v=dt[i][0];
					ta=c=dt[i][0]=(ym[i]+yl[i]+g*dy[i])/2.0;
					for (k=1; k<=l; k++) {
						b1=d[k]*v;
						b=b1-c;
						u=v;
						if (b != 0.0) {
							b=(c-v)/b;
							u=c*b;
							c=b1*b;
						}
						v=dt[i][k];
						dt[i][k]=u;
						ta += u;
					}
					if (Math.abs(y[i]-ta) > reta*s[i]+aeta) konv=false;
					y[i]=ta;
				}
				if (konv) {
					next=true;
					break;
				}
				d[2]=4.0;
				d[4]=16.0;
				b0 = !b0;
				m=r;
				r=sr;
				sr=m*2;
			}
			if (next) break;
			bh = !bh;
			last=false;
			h /= 2.0;
		}
		h *= fc;
		x[0]=a;
		method.output(n,x,xe,y,s);
	} while (!last);
}


public static void ark(double t[], double te[], int m0[],
                       int m[], double u[],
                       AP_ark_methods method, double data[])
{
	double th1[] = {1.0, 0.5, 1.0/6.0, 1.0/3.0, 1.0/24.0,
                  1.0/12.0, 0.125, 0.25};
	double ec0,ec1,ec2,tau0,tau1,tau2,taus,t2;
  boolean start,step1,last;
	int p,n,q,i,j,k,l,n1,m00;
	double thetanm1,tau,betan,qinv,eta,ss,theta0,tauacc,taustab,
         aa,bb,cc,ec,mt,lt;
  double th[] = new double[9];
  double aux[] = new double[4];
  double s[] = new double[1];

	n=(int)data[1];
	m00=m0[0];
	double mu[] = new double[n+1];
  double lambda[] = new double[n+1];
  double thetha[] = new double[n+1];
  double ro[] = new double[m[0]+1];
  double r[] = new double[m[0]+1];
	double alfa[][] = new double[9][n+2];

  step1=true;
  tau2=tau1=tau0=taus=t2=ec0=0.0;
	p=(int)data[2];
	ec1=ec2=0.0;
	betan=data[3];
	thetanm1 = (p == 3) ? 0.75 : 1.0;
	theta0=1.0-thetanm1;
	s[0]=1.0;
	for (j=n-1; j>=1; j--) {
		s[0] = -s[0]*theta0+data[n+10-j];
		mu[j]=data[n+11-j]/s[0];
		lambda[j]=mu[j]-theta0;
	}
	for (i=1; i<=8; i++)
		for (j=0; j<=n; j++)
			if (i == 1) alfa[i][j+1]=1.0;
			else if (j == 0) alfa[i][j+1]=0.0;
			else if (i == 2 || i == 4 || i == 8)
					alfa[i][j+1]=Math.pow(arkmui(j,n,p,lambda),(i+2)/3);
			else if ((i == 3 || i == 6) && j > 1) {
				s[0]=0.0;
				for (l=1; l<=j-1; l++)
					s[0] += arklabda(j,l,n,p,lambda)*
								Math.pow(arkmui(l,n,p,lambda),i/3);
				alfa[i][j+1]=s[0];
			}
			else if (i == 5 && j > 2) {
				s[0]=0.0;
				for (l=2; l<=j-1; l++) {
					ss=0.0;
					for (k=1; k<=l-1; k++)
						ss += arklabda(l,k,n,p,lambda)*
									arkmui(k,n,p,lambda);
					s[0] += arklabda(j,l,n,p,lambda)*ss;
				}
				alfa[i][j+1]=s[0];
			}
			else if (i == 7 && j > 1) {
				s[0]=0.0;
				for (l=1; l<=j-1; l++)
					s[0] += arklabda(j,l,n,p,lambda)*arkmui(l,n,p,lambda);
				alfa[i][j+1]=s[0]*arkmui(j,n,p,lambda);
			}
			else alfa[i][j+1]=0.0;
	n1 = ((n < 4) ? n+1 : ((n < 7) ? 4 : 8));
	for (i=1; i<=8; i++) th[i]=th1[i-1];
	if (p == 3 && n < 7) th[1]=th[2]=0.0;
	aux[2]=Double.MIN_VALUE;
	Linear_algebra.decsol(alfa,n1,aux,th);
	Basic.inivec(0,n,thetha,0.0);
	Basic.dupvec(0,n1-1,1,thetha,th);
	if (!(p == 3 && n < 7)) {
		thetha[0] -= theta0;
		thetha[n-1] -= thetanm1;
		q=p+1;
	} else
		q=3;
	qinv=1.0/q;
	start=(data[8] == 0.0);
	data[10]=0.0;
	last=false;
	Basic.dupvec(m0[0],m[0],0,r,u);
	method.derivative(m0,m,t,r);
	do {
		/* stepsize */
		eta=Math.sqrt(Basic.vecvec(m0[0],m[0],0,u,u))*data[7]+data[6];
		if (eta > 0.0) {
			if (start) {
				if (data[8] == 0) {
					tauacc=data[5];
					step1=true;
				} else
					if (step1) {
						tauacc=Math.pow(eta/ec2,qinv);
						if (tauacc > 10.0*tau2)
							tauacc=10.0*tau2;
						else
							step1=false;
					} else {
						bb=(ec2-ec1)/tau1;
						cc = -bb*t2+ec2;
						ec=bb*t[0]+cc;
						tauacc = (ec < 0.0) ? tau2 : Math.pow(eta/ec,qinv);
						start=false;
					}
			} else {
				aa=((ec0-ec1)/tau0+(ec2-ec1)/tau1)/(tau1+tau0);
				bb=(ec2-ec1)/tau1-(2.0*t2-tau1)*aa;
				cc = -(aa*t2+bb)*t2+ec2;
				ec=(aa*t[0]+bb)*t[0]+cc;
				tauacc = ((ec < 0.0) ? taus : Math.pow(eta/ec,qinv));
				if (tauacc > 2.0*taus) tauacc=2.0*taus;
				if (tauacc < taus/2.0) tauacc=taus/2.0;
			}
		} else
			tauacc=data[5];
		if (tauacc < data[5]) tauacc=data[5];
		taustab=betan/data[4];
		if (taustab < data[5]) {
			data[10]=1.0;
			break;
		}
		tau = ((tauacc > taustab) ? taustab : tauacc);
		taus=tau;
		if (tau >= te[0]-t[0]) {
			tau=te[0]-t[0];
			last=true;
		}
		tau0=tau1;
		tau1=tau2;
		tau2=tau;
		/* difference scheme */
		Basic.mulvec(m0[0],m[0],0,ro,r,thetha[0]);
		if (p == 3) Basic.elmvec(m0[0],m[0],0,u,r,0.25*tau);
		for (i=1; i<=n-1; i++) {
			mt=mu[i]*tau;
			lt=lambda[i]*tau;
			for (j=m0[0]; j<=m[0]; j++) r[j]=lt*r[j]+u[j];
			s[0]=t[0]+mt;
			method.derivative(m0,m,s,r);
			if (thetha[i] != 0.0)
        Basic.elmvec(m0[0],m[0],0,ro,r,thetha[i]);
			if (i == n) {
				data[9]=Math.sqrt(Basic.vecvec(m0[0],m[0],0,ro,ro))*tau;
				ec0=ec1;
				ec1=ec2;
				ec2=data[9]/Math.pow(tau,q);
			}
		}
		Basic.elmvec(m0[0],m[0],0,u,r,thetanm1*tau);
		Basic.dupvec(m0[0],m[0],0,r,u);
		s[0]=t[0]+tau;
		method.derivative(m0,m,s,r);
		if (thetha[n] != 0.0) Basic.elmvec(m0[0],m[0],0,ro,r,thetha[n]);
		data[9]=Math.sqrt(Basic.vecvec(m0[0],m[0],0,ro,ro))*tau;
		ec0=ec1;
		ec1=ec2;
		ec2=data[9]/Math.pow(tau,q);
		t2=t[0];
		if (last) {
			last=false;
			t[0]=te[0];
		} else
			t[0] += tau;
		data[8] += 1.0;
		method.out(m0,m,t,te,u,data);
	} while (t[0] != te[0]);
}


static private double arkmui(int i, int n, int p, double lambda[])
{
	/* this function is internally used by ARK */

	return ((i==n) ? 1.0 : ((i<1 || i>n) ? 0.0 :
			((p<3) ? lambda[i] : ((p==3) ? lambda[i]+0.25 : 0.0))));
}


static private double arklabda(int i, int j, int n, int p,
                               double lambda[])
{
	/* this function is internally used by ARK */

	return ((p<3) ? ((j==i-1) ? arkmui(i,n,p,lambda) : 0.0) :
         ((p==3) ? ((i==n) ? ((j==0) ? 0.25 :
         ((j==n-1) ? 0.75 : 0.0)) :
         ((j==0) ? ((i==1) ? arkmui(1,n,p,lambda) : 0.25) :
         ((j==i-1) ? lambda[i] : 0.0))) : 0.0));
}


public static void efrk(double t[], double te, int m0, int m,
                   double u[], double sigma[], double phi[],
                   double diameter[], AP_efrk_methods method,
                   int k[], double step, int r, int l,
                   double beta[], boolean thirdorder, double tol)
{
  boolean first,last,change,complex;
	int n,i,j,c1,c3;
	double theta0,thetanm1,h,b,b0,phi0,phil,cosphi,sinphi,eps,
			   betar,dd,hstab,hstabint,c,temp,c2,e,b1,zi,cosiphi,
			   siniphi,cosphil,bb,mt,lt,tht;
  double aux[] = new double[4];
	int p[] = new int[l+1];
  double mu[] = new double[r+l];
  double labda[] = new double[r+l];
  double pt[] = new double[r+1];
  double fac[] = new double[l];
  double betac[] = new double[l];
	double rl[] = new double[m+1];
  double d[] = new double[l+1];
  double a[][] = new double[l+1][l+1];

  cosphi=sinphi=theta0=thetanm1=0.0;
	n=r+l;
	first=true;
	b0 = -1.0;
	betar=Math.pow(beta[r],1.0/r);
	last=false;
	eps=Double.MIN_VALUE;
	phi0=phil=Math.PI;
	do {
		/* stepsize */
		h=step;
		dd=Math.abs(sigma[0]*Math.sin(phi[0]));
		complex=(((l/2)*2 == l) && 2.0*dd > diameter[0]);
		if (diameter[0] > 0.0) {
			temp=sigma[0]*sigma[0]/(diameter[0]*(diameter[0]*0.25+dd));
			hstab=Math.pow(temp,l*0.5/r)/betar/sigma[0];
		} else
			hstab=h;
		dd = (thirdorder ? Math.pow(2.0*tol/eps/beta[r],1.0/(n-1))*
          Math.pow(4.0,(l-1.0)/(n-1.0)) :
          Math.pow(tol/eps,(1.0/r)/betar));
		hstabint=Math.abs(dd/sigma[0]);
		if (h > hstab) h=hstab;
		if (h > hstabint) h=hstabint;
		if (t[0]+h > te*(1.0-k[0]*eps)) {
			last=true;
			h=te-t[0];
		}
		b=h*sigma[0];
		dd=diameter[0]*0.1*h;
		dd *= dd;
		if (h < t[0]*eps) break;
		change=((b0 == -1.0) ||
				((b-b0)*(b-b0)+b*b0*(phi[0]-phi0)*(phi[0]-phi0) > dd));
		if (change) {
			/* coefficient */
			b0=b;
			phi0=phi[0];
			if (b >= 1.0) {
				if (complex) {
					/* solution of complex equations */
					if (phi0 != phil) {
						/* elements of matrix */
						phil=phi0;
						cosphi=Math.cos(phil);
						sinphi=Math.sin(phil);
						cosiphi=1.0;
						siniphi=0.0;
						for (i=0; i<=l-1; i++) {
							c1=r+1+i;
							c2=1.0;
							for (j=l-1; j>=1; j-=2) {
								a[j][l-i]=c2*cosiphi;
								a[j+1][l-i]=c2*siniphi;
								c2 *= c1;
								c1--;
							}
							cosphil=cosiphi*cosphi-siniphi*sinphi;
							siniphi=cosiphi*sinphi+siniphi*cosphi;
							cosiphi=cosphil;
						}
						aux[2]=0.0;
            Linear_algebra.dec(a,l,aux,p);
					}
					/* right hand side */
					e=Math.exp(b*cosphi);
					b1=b*sinphi-(r+1)*phil;
					cosiphi=e*Math.cos(b1);
					siniphi=e*Math.sin(b1);
					b1=1.0/b;
					zi=Math.pow(b1,r);
					for (j=l; j>=2; j-=2) {
						d[j]=zi*siniphi;
						d[j-1]=zi*cosiphi;
						cosphil=cosiphi*cosphi-siniphi*sinphi;
						siniphi=cosiphi*sinphi+siniphi*cosphi;
						cosiphi=cosphil;
						zi *= b;
					}
					cosiphi=zi=1.0;
					siniphi=0.0;
					for (i=r; i>=0; i--) {
						c1=i;
						c2=beta[i];
						c3=((2*i > l-2) ? 2 : l-2*i);
						cosphil=cosiphi*cosphi-siniphi*sinphi;
						siniphi=cosiphi*sinphi+siniphi*cosphi;
						cosiphi=cosphil;
						for (j=l; j>=c3; j-=2) {
							d[j] += zi*c2*siniphi;
							d[j-1] -= zi*c2*cosiphi;
							c2 *=c1;
							c1--;
						}
						zi *= b1;
					}
					Linear_algebra.sol(a,l,p,d);
					for (i=1; i<=l; i++) beta[r+i]=d[l+1-i]*b1;
				} else {
					/* form beta */
					if (first) {
						/* form constants */
						first=false;
						fac[0]=1.0;
						for (i=1; i<=l-1; i++) fac[i]=i*fac[i-1];
						pt[r]=l*fac[l-1];
						for (i=1; i<=r; i++) pt[r-i]=pt[r-i+1]*(l+i)/i;
					}
					if (l == 1) {
						c=1.0-Math.exp(-b);
						for (j=1; j<=r; j++) c=beta[j]-c/b;
						beta[r+1]=c/b;
					} else
						if (b > 40.0)
							for (i=r+1; i<=r+l; i++) {
								c=0.0;
								for (j=0; j<=r; j++)
									c=beta[j]*pt[j]/(i-j)-c/b;
								beta[i]=c/b/fac[l+r-i]/fac[i-r-1];
							}
						else {
							dd=c=Math.exp(-b);
							betac[l-1]=dd/fac[l-1];
							for (i=1; i<=l-1; i++) {
								c=b*c/i;
								dd += c;
								betac[l-1-i]=dd/fac[l-1-i];
							}
							bb=1.0;
							for (i=r+1; i<=r+l; i++) {
								c=0.0;
								for (j=0; j<=r; j++)
									c=(beta[j]-((j < l) ? betac[j] : 0.0))*
											pt[j]/(i-j)-c/b;
								beta[i]=c/b/fac[l+r-i]/fac[i-r-1]+
											((i < l) ? bb*betac[i] : 0.0);
								bb *= b;
							}
						}
				}
			}
			labda[0]=mu[0]=0.0;
			if (thirdorder) {
				theta0=0.25;
				thetanm1=0.75;
				if (b < 1.0) {
					c=mu[n-1]=2.0/3.0;
					labda[n-1]=5.0/12.0;
					for (j=n-2; j>=1; j--) {
						c=mu[j]=c/(c-0.25)/(n-j+1);
						labda[j]=c-0.25;
					}
				} else {
					c=mu[n-1]=beta[2]*4.0/3.0;
					labda[n-1]=c-0.25;
					for (j=n-2; j>=1; j--) {
						c=mu[j]=c/(c-0.25)*beta[n-j+1]/beta[n-j]/
									((j < l) ? b : 1.0);
						labda[j]=c-0.25;
					}
				}
			} else {
				theta0=0.0;
				thetanm1=1.0;
				if (b < 1.0)
					for (j=n-1; j>=1; j--) mu[j]=labda[j]=1.0/(n-j+1);
				else {
					labda[n-1]=mu[n-1]=beta[2];
					for (j=n-2; j>=1; j--)
						mu[j]=labda[j]=beta[n-j+1]/beta[n-j]/
											((j < l) ? b : 1.0);
				}
			}
		}
		k[0]++;
		/* difference scheme */
		i = -1;
		do {
			i++;
			mt=mu[i]*h;
			lt=labda[i]*h;
			for (j=m0; j<=m; j++) rl[j]=u[j]+lt*rl[j];
			method.derivative(m0,m,t[0]+mt,rl);
			if (i == 0 || i == n-1) {
				tht=((i == 0) ? theta0*h : thetanm1*h);
				Basic.elmvec(m0,m,0,u,rl,tht);
			}
		} while (i < n-1);
		t[0] += h;
		method.output(m0,m,t,te,u,sigma,phi,diameter,k,step,r,l);
	} while (!last);
}


public static void efsirk(double x[], double xe, int m,
                   double y[], double delta[],
                   AP_efsirk_methods method, double j[][],
                   int n[], double aeta, double reta,
                   double hmin, double hmax, boolean linear)
{
  boolean lin;
	int k,l;
	double step,h,mu0,mu1,mu2,theta0,theta1,nu1,nu2,nu3,yk,fk,
         c1,c2,d,discr,eta,s,z1,z2,e,alpha1,a,b;
  double aux[] = new double[8];
	int ri[] = new int[m+1];
	int ci[] = new int[m+1];
	double f[] = new double[m+1];
  double k0[] = new double[m+1];
  double labda[] = new double[m+1];
	double j1[][] = new double[m+1][m+1];

  nu3=h=z2=mu1=mu2=mu0=theta0=theta1=nu1=nu2=0.0;
	aux[2]=Double.MIN_VALUE;
	aux[4]=8.0;
	for (k=1; k<=m; k++) f[k]=y[k];
	n[0] = 0;
	method.output(x,xe,m,y,delta,j,n);
	step=0.0;
	do {
		n[0]++;
		/* difference scheme */
		method.derivative(m,f,delta);
		/* step size */
		if (linear)
			s=h=hmax;
		else
			if (n[0] == 1 || hmin == hmax)
				s=h=hmin;
			else {
				eta=aeta+reta*Math.sqrt(Basic.vecvec(1,m,0,y,y));
				c1=nu3*step;
				for (k=1; k<=m; k++) labda[k] += c1*f[k]-y[k];
				discr=Math.sqrt(Basic.vecvec(1,m,0,labda,labda));
				s=h=(eta/(0.75*(eta+discr))+0.33)*h;
				if (h < hmin)
					s=h=hmin;
				else
					if (h > hmax) s=h=hmax;
			}
		if (x[0]+s > xe) s=xe-x[0];
		lin=((step == s) && linear);
		step=s;
		if (!linear || n[0] == 1) method.jacobian(m,j,y,delta);
		if (!lin) {
			/* coefficient */
			z1=step*delta[0];
			if (n[0] == 1) z2=z1+z1;
			if (Math.abs(z2-z1) > 1.0e-6*Math.abs(z1) || z2 > -1.0) {
				a=z1*z1+12.0;
				b=6.0*z1;
				if (Math.abs(z1) < 0.1)
					alpha1=(z1*z1/140.0-1.0)*z1/30.0;
				else if (z1 < 1.0e-14)
					alpha1=1.0/3.0;
				else if (z1 < -33.0)
					alpha1=(a+b)/(3.0*z1*(2.0+z1));
				else {
					e=((z1 < 230.0) ? Math.exp(z1) : Double.MAX_VALUE);
					alpha1=((a-b)*e-a-b)/(((2.0-z1)*e-2.0-z1)*3.0*z1);
				}
				mu2=(1.0/3.0+alpha1)*0.25;
				mu1 = -(1.0+alpha1)*0.5;
				mu0=(6.0*mu1+2.0)/9.0;
				theta0=0.25;
				theta1=0.75;
				a=3.0*alpha1;
				nu3=(1.0+a)/(5.0-a)*0.5;
				a=nu3+nu3;
				nu1=0.5-a;
				nu2=(1.0+a)*0.75;
				z2=z1;
			}
			c1=step*mu1;
			d=step*step*mu2;
			for (k=1; k<=m; k++) {
				for (l=1; l<=m; l++)
          j1[k][l]=d*Basic.matmat(1,m,k,l,j,j)+c1*j[k][l];
				j1[k][k] += 1.0;
			}
      Linear_algebra.gsselm(j1,m,aux,ri,ci);
		}
		c1=step*step*mu0;
		d=step*2.0/3.0;
		for (k=1; k<=m; k++) {
			k0[k]=fk=f[k];
			labda[k]=d*fk+c1*Basic.matvec(1,m,k,j,f);
		}
    Linear_algebra.solelm(j1,m,ri,ci,labda);
		for (k=1; k<=m; k++) f[k]=y[k]+labda[k];
		method.derivative(m,f,delta);
		c1=theta0*step;
		c2=theta1*step;
		d=nu1*step;
		for (k=1; k<=m; k++) {
			yk=y[k];
			fk=f[k];
			labda[k]=yk+d*fk+nu2*labda[k];
			y[k]=f[k]=yk+c1*k0[k]+c2*fk;
		}
		x[0] += step;
		method.output(x,xe,m,y,delta,j,n);
	} while (x[0] < xe);
}


public static void eferk(double x[], double xe, int m,
                         double y[], double sigma[],
                         double phi, AP_eferk_methods method,
                         double j[][], int k[], int l,
                         boolean aut, double aeta, double reta,
                         double hmin, double hmax, boolean linear)
{
  boolean change,last;
	int m1,i,c1,q;
	double h,b,b0,phi0,cosphi,sinphi,eta,discr,fac,s,cos2phi,
         sina,e,zi,c2,cosiphi,siniphi,cosphil,emin1,b1,b2,a0,
         a1,a2,a3,c,ddd,betai,bethai;
  double aux[] = new double[4];
	int p[] = new int[l+1];
	double beta[] = new double[l+1];
	double betha[] = new double[l+1];
	double betac[] = new double[l+4];
	double k0[] = new double[m+1];
  double d[] = new double[m+1];
  double d1[] = new double[m+1];
  double d2[] = new double[m+1];
	double dd[] = new double[l+1];
	double a[][] = new double[l+1][l+1];

  h=cosphi=sinphi=0.0;
	b0 = phi0 = -1.0;
	betac[l]=betac[l+1]=betac[l+2]=betac[l+3]=0.0;
	beta[0]=1.0/6.0;
	betha[0]=0.5;
	fac=1.0;
	for (i=2; i<=l-1; i++) fac *= i;
	m1=(aut ? m : m-1);
	k[0] = 0;
	last=false;
	do {
		for (i=1; i<=m; i++) d[i]=y[i];
		method.derivative(m,d);
		if (!linear || k[0] == 0) method.jacobian(m,j,y,sigma);
		/* step size */
		eta=aeta+reta*Math.sqrt(Basic.vecvec(1,m1,0,y,y));
		if (k[0] == 0) {
			discr=Math.sqrt(Basic.vecvec(1,m1,0,d,d));
			h=eta/discr;
		} else {
			s=0.0;
			for (i=1; i<=m1; i++) {
				ddd=d[i]-d2[i];
				s += ddd*ddd;
			}
			discr=h*Math.sqrt(s)/eta;
			h *= (linear ? 4.0/(4.0+discr)+0.5 :
								4.0/(3.0+discr)+1.0/3.0);
		}
		if (h < hmin) h=hmin;
		if (h > hmax) h=hmax;
		b=Math.abs(h*sigma[0]);
		change=(Math.abs(1.0-b/b0) > 0.05 || phi != phi0);
		if (1.1*h >= xe-x[0]) {
			change=last=true;
			h=xe-x[0];
		}
		if (!change) h=h*b0/b;
		if (change) {
			/* coefficient */
			b0=b=Math.abs(h*sigma[0]);
			if (b >= 0.1) {
				if (phi != Math.PI && l == 2 || 
                   Math.abs(phi-Math.PI) > 0.01) {
					/* solution of complex equations */
					if (l == 2) {
						phi0=phi;
						cosphi=Math.cos(phi0);
						sinphi=Math.sin(phi0);
						e=Math.exp(b*cosphi);
						zi=b*sinphi-3.0*phi0;
						sina=((Math.abs(sinphi) < 1.0e-6) ? -e*(b+3.0) :
								e*Math.sin(zi)/sinphi);
						cos2phi=2.0*cosphi*cosphi-1.0;
						betha[2]=(0.5+(2.0*cosphi+(1.0+2.0*cos2phi+
										sina)/b)/b)/b/b;
						sina=((Math.abs(sinphi) < 1.0e-6) ? e*(b+4.0) :
								sina*cosphi-e*Math.cos(zi));
						betha[1] = -(cosphi+(1.0+2.0*cos2phi+
										(4.0*cosphi*cos2phi+sina)/b)/b)/b;
						beta[1]=betha[2]+2.0*cosphi*(betha[1]-1.0/6.0)/b;
						beta[2]=(1.0/6.0-betha[1])/b/b;
					} else {
						if (phi0 != phi) {
							/* elements of matrix */
							phi0=phi;
							cosphi=Math.cos(phi0);
							sinphi=Math.sin(phi0);
							cosiphi=1.0;
							siniphi=0.0;
							for (i=0; i<=l-1; i++) {
								c1=4+i;
								c2=1.0;
								for (q=l-1; q>=1; q-=2) {
									a[q][l-i]=c2*cosiphi;
									a[q+1][l-i]=c2*siniphi;
									c2 *= c1;
									c1--;
								}
								cosphil=cosiphi*cosphi-siniphi*sinphi;
								siniphi=cosiphi*sinphi+siniphi*cosphi;
								cosiphi=cosphil;
							}
							aux[2]=0.0;
              Linear_algebra.dec(a,l,aux,p);
						}
						/* right hand side */
						e=Math.exp(b*cosphi);
						zi=b*sinphi-4.0*phi0;
						cosiphi=e*Math.cos(zi);
						siniphi=e*Math.sin(zi);
						zi=1.0/b/b/b;
						for (q=l; q>=2; q-=2) {
							dd[q]=zi*siniphi;
							dd[q-1]=zi*cosiphi;
							cosphil=cosiphi*cosphi-siniphi*sinphi;
							siniphi=cosiphi*sinphi+siniphi*cosphi;
							cosiphi=cosphil;
							zi *= b;
						}
						siniphi=2.0*sinphi*cosphi;
						cosiphi=2.0*cosphi*cosphi-1.0;
						cosphil=cosphi*(2.0*cosiphi-1.0);
						dd[l] += sinphi*(1.0/6.0+(cosphi+(1.0+2.0*
									cosiphi*(1.0+2.0*cosphi/b))/b)/b);
						dd[l-1] -= cosphi/6.0+(0.5*cosiphi+(cosphil+
										(2.0*cosiphi*cosiphi-1.0)/b)/b)/b;
						dd[l-2] += sinphi*(0.5+(2.0*cosphi+
										(2.0*cosiphi+1.0)/b)/b);
						dd[l-3] -= 0.5*cosphi-(cosiphi+cosphil/b)/b;
						if (l >= 5) {
							dd[l-4] += sinphi+siniphi/b;
							dd[l-5] -= cosphi+cosiphi/b;
							if (l >= 7) {
								dd[l-6] += sinphi;
								dd[l-7] -= cosphi;
							}
						}
						Linear_algebra.sol(a,l,p,dd);
						zi=1.0/b;
						for (i=1; i<=l; i++) {
							beta[i]=dd[l+1-i]*zi;
							betha[i]=(i+3)*beta[i];
							zi /= b;
						}
					}
				} else {
					/* form beta */
					if (l == 1) {
						betha[1]=(0.5-(1.0-(1.0-Math.exp(-b))/b)/b)/b;
						beta[1]=(1.0/6.0-betha[1])/b;
					} else if (l == 2) {
						e=Math.exp(-b);
						emin1=e-1.0;
						betha[1]=(1.0-(3.0+e+4.0*emin1/b)/b)/b;
						betha[2]=(0.5-(2.0+e+3.0*emin1/b)/b)/b/b;
						beta[2]=(1.0/6.0-betha[1])/b/b;
						beta[1]=(1.0/3.0-(1.5-(4.0+e+5.0*emin1/b)/b)/b)/b;
					} else {
						betac[l-1]=c=ddd=Math.exp(-b)/fac;
						for (i=l-1; i>=1; i--) {
							c=i*b*c/(l-i);
							betac[i-1]=ddd=ddd*i+c;
						}
						b2=0.5-betac[2];
						b1=(1.0-betac[1])*(l+1)/b;
						b0=(1.0-betac[0])*(l+2)*(l+1)*0.5/b/b;
						a3=1.0/6.0-betac[3];
						a2=b2*(l+1)/b;
						a1=b1*(l+2)*0.5/b;
						a0=b0*(l+3)/3.0/b;
						ddd=l/b;
						for (i=1; i<=l; i++) {
							beta[i]=(a3/i-a2/(i+1)+a1/(i+2)-a0/(i+3))*ddd+
										betac[i+3];
							betha[i]=(b2/i-b1/(i+1)+b0/(i+2))*ddd+
										betac[i+2];
							ddd=ddd*(l-i)/i/b;
						}
					}
				}
			} else
				for (i=1; i<=l; i++) {
					betha[i]=beta[i-1];
					beta[i]=beta[i-1]/(i+3);
				}
		}
		method.output(x,xe,m,y,j,k);
		/* difference scheme */
		if (m1 < m) {
			d2[m]=1.0;
			k0[m]=y[m]+2.0*h/3.0;
			y[m] += 0.25*h;
		}
		for (q=1; q<=m1; q++) {
			k0[q]=y[q]+2.0*h/3.0*d[q];
			y[q] += 0.25*h*d[q];
			d1[q]=h*Basic.matvec(1,m,q,j,d);
			d2[q]=d1[q]+d[q];
		}
		for (i=0; i<=l; i++) {
			betai=4.0*beta[i]/3.0;
			bethai=betha[i];
			for (q=1; q<=m1; q++) d[q]=h*d1[q];
			for (q=1; q<=m1; q++) {
				k0[q] += betai*d[q];
				d1[q]=Basic.matvec(1,m1,q,j,d);
				d2[q] += bethai*d1[q];
			}
		}
		method.derivative(m,k0);
		for (q=1; q<=m; q++) y[q] += 0.75*h*k0[q];
		k[0]++;
		x[0] += h;
	} while (!last);
	method.output(x,xe,m,y,j,k);
}


public static void liniger1vs(double x[], double xe, int m,
                   double y[], double sigma[],
                   AP_liniger1vs_methods method, double j[][],
                   int itmax, double hmin, double hmax,
                   double aeta, double reta, double info[])
{
  boolean last,first,evaljac,evalcoef;
	int i,st,lastjac,k,q;
	double h,hnew,e,e1,eta,eta1,discr,hl,b;
  double aux[] = new double[4];
  double mu[] = new double[1];
  double mu1[] = new double[1];
  double beta[] = new double[1];
  double p[] = new double[1];
	int pi[] = new int[m+1];
	double dy[] = new double[m+1];
  double yl[] = new double[m+1];
  double yr[] = new double[m+1];
  double f[] = new double[m+1];
	double a[][] = new double[m+1][m+1];

  lastjac=0;
  h=discr=e=e1=0.0;
	first=evaljac=true;
	last=evalcoef=false;
	Basic.inivec(1,9,info,0.0);
	eta=reta*Math.sqrt(Basic.vecvec(1,m,0,y,y))+aeta;
	eta1=eta/Math.sqrt(Math.abs(reta));
	Basic.dupvec(1,m,0,f,y);
	method.derivative(m,f,sigma);
	info[2]=1.0;
	st=1;
	do {
		/* step size */
		if (eta < 0.0) {
			hl=h;
			h=hnew=hmax;
			info[5] += 1.0;
			if (1.1*hnew > xe-x[0]) {
				last=true;
				h=hnew=xe-x[0];
			}
			evalcoef=(h != hl);
		} else if (first) {
			h=hnew=hmin;
			first=false;
			info[4] += 1.0;
		} else {
			b=discr/eta;
			hl=h;
			if (b < 0.01) b=0.01;
			hnew = (b > 0.0) ? h*Math.pow(b,-1.0/p[0]) : hmax;
			if (hnew < hmin) {
				hnew=hmin;
				info[4] += 1.0;
			} else
				if (hnew > hmax)  {
					hnew=hmax;
					info[5] += 1.0;
				}
			if (1.1*hnew >= xe-x[0]) {
				last=true;
				h=hnew=xe-x[0];
			} else
				if (Math.abs(h/hnew-1.0) > 0.1) h=hnew;
			evalcoef=(h !=hl);
		}
		info[1] += 1.0;
		if (evaljac) {
			method.jacobian(m,j,y,sigma);
			info[3] += 1.0;
			h=hnew;
			liniger1vscoef(m,a,j,aux,pi,h,sigma,mu,mu1,beta,p);
			evaljac=false;
			lastjac=st;
		} else
			if (evalcoef)
				liniger1vscoef(m,a,j,aux,pi,h,sigma,mu,mu1,beta,p);
		i=1;
		do {
			/* iteration */
			if (reta < 0.0) {
				if (i == 1) {
					Basic.mulvec(1,m,0,dy,f,h);
					for (k=1; k<=m; k++) yl[k]=y[k]+mu[0]*dy[k];
					Linear_algebra.sol(a,m,pi,dy);
					e=1.0;
				} else {
					for (k=1; k<=m; k++) dy[k]=yl[k]-y[k]+mu1[0]*f[k];
					if (e*Math.sqrt(Basic.vecvec(1,m,0,y,y)) > e1*e1) {
						evaljac=(i >= 3);
						if (i > 3) {
							info[3] += 1.0;
							method.jacobian(m,j,y,sigma);
							for (q=1; q<=m; q++) {
								Basic.mulrow(1,m,q,q,a,j,-mu1[0]);
								a[q][q] += 1.0;
							}
							aux[2]=0.0;
              Linear_algebra.dec(a,m,aux,pi);
						}
					}
					Linear_algebra.sol(a,m,pi,dy);
				}
				e1=e;
				e=Math.sqrt(Basic.vecvec(1,m,0,dy,dy));
				Basic.elmvec(1,m,0,y,dy,1.0);
				eta=Math.sqrt(Basic.vecvec(1,m,0,y,y))*reta+aeta;
				discr=0.0;
				Basic.dupvec(1,m,0,f,y);
				method.derivative(m,f,sigma);
				info[2] += 1.0;
			} else {
				if (i == 1) {
					/* linearity */
					for (k=1; k<=m; k++) dy[k]=y[k]-mu1[0]*f[k];
					Linear_algebra.sol(a,m,pi,dy);
					Basic.elmvec(1,m,0,dy,y,-1.0);
					e=Math.sqrt(Basic.vecvec(1,m,0,dy,dy));
					if (e*(st-lastjac) > eta) {
						method.jacobian(m,j,y,sigma);
						lastjac=st;
						info[3] += 1.0;
						h=hnew;
						liniger1vscoef(m,a,j,aux,pi,h,sigma,mu,mu1,beta,p);
						/* linearity */
						for (k=1; k<=m; k++) dy[k]=y[k]-mu1[0]*f[k];
						Linear_algebra.sol(a,m,pi,dy);
						Basic.elmvec(1,m,0,dy,y,-1.0);
						e=Math.sqrt(Basic.vecvec(1,m,0,dy,dy));
					}
					evaljac=(e*(st+1-lastjac) > eta);
					Basic.mulvec(1,m,0,dy,f,h);
					for (k=1; k<=m; k++) yl[k]=y[k]+mu[0]*dy[k];
					Linear_algebra.sol(a,m,pi,dy);
					for (k=1; k<=m; k++)
            yr[k]=h*beta[0]*Basic.matvec(1,m,k,j,dy);
					Linear_algebra.sol(a,m,pi,yr);
					Basic.elmvec(1,m,0,yr,dy,1.0);
				} else {
					for (k=1; k<=m; k++) dy[k]=yl[k]-y[k]+mu1[0]*f[k];
					if (e > eta1 && discr > eta1) {
						info[3] += 1.0;
						method.jacobian(m,j,y,sigma);
						for (q=1; q<=m; q++) {
							Basic.mulrow(1,m,q,q,a,j,-mu1[0]);
							a[q][q] += 1.0;
						}
						aux[2]=0.0;
            Linear_algebra.dec(a,m,aux,pi);
					}
					Linear_algebra.sol(a,m,pi,dy);
					e=Math.sqrt(Basic.vecvec(1,m,0,dy,dy));
				}
				Basic.elmvec(1,m,0,y,dy,1.0);
				eta=Math.sqrt(Basic.vecvec(1,m,0,y,y))*reta+aeta;
				eta1=eta/Math.sqrt(reta);
				Basic.dupvec(1,m,0,f,y);
				method.derivative(m,f,sigma);
				info[2] += 1.0;
				for (k=1; k<=m; k++) dy[k]=yr[k]-h*f[k];
				discr=Math.sqrt(Basic.vecvec(1,m,0,dy,dy))/2.0;
			}
			if (i > info[6]) info[6]=i;
			i++;
		} while (e > Math.abs(eta) && discr > 1.3*eta && i <= itmax);
		info[7]=eta;
		info[8]=discr;
		x[0] += h;
		if (discr > info[9]) info[9]=discr;
		method.output(x,xe,m,y,sigma,j,info);
		st++;
	} while (!last);
}


static private void liniger1vscoef(int m, double a[][], double j[][],
               double aux[], int pi[], double h, double sigma[],
               double mu[], double mu1[], double beta[], double p[])
{
	/* this function is internally used by LINIGER1VS */

	int q;
	double b,e;

	b=Math.abs(h*sigma[0]);
	if (b > 40.0) {
		mu[0] = 1.0/b;
		beta[0] = 1.0;
		p[0] = 2.0+2.0/(b-2.0);
	} else if (b < 0.04) {
		e=b*b/30.0;
		p[0] = 3.0-e;
		mu[0] = 0.5-b/12.0*(1.0-e/2.0);
		beta[0] = 0.5+b/6.0*(1.0-e);
	} else {
		e=Math.exp(b)-1.0;
		mu[0] = 1.0/b-1.0/e;
		beta[0] = (1.0-b/e)*(1.0+1.0/e);
		p[0] = (beta[0]-mu[0])/(0.5-mu[0]);
	}
	mu1[0] = h*(1.0-mu[0]);
	for (q=1; q<=m; q++) {
		Basic.mulrow(1,m,q,q,a,j,-mu1[0]);
		a[q][q] += 1.0;
	}
	aux[2]=0.0;
  Linear_algebra.dec(a,m,aux,pi);
}


public static void liniger2(double x[], double xe, int m,
              double y[], double sigma1[], double sigma2[],
              AP_liniger2_methods method, double j[][],
              int k[], int itmax, double step, double aeta,
              double reta)
{
	boolean last;
	int i,itnum;
	double h,hl,jfl,eta,discr;
  double aux[] = new double[4];
  double c0[] = new double[1];
  double c1[] = new double[1];
  double c2[] = new double[1];
  double c3[] = new double[1];
  double c4[] = new double[1];
	int pi[] = new int[m+1];
	double dy[] = new double[m+1];
	double yl[] = new double[m+1];
	double fl[] = new double[m+1];
	double a[][] = new double[m+1][m+1];

	last=false;
	k[0] = 0;
	hl=0.0;
	do {
		k[0]++;
		/* step size */
		h=step;
		if (1.1*h >= xe-x[0]) {
			last=true;
			h=xe-x[0];
			x[0]=xe;
		} else
			x[0] += h;
		/* newton iteration */
		itnum=0;
		while (true) {
			itnum++;
			if (method.evaluate(itnum)) {
				method.jacobian(m,j,y,sigma1,sigma2);
				liniger2coef(m,j,a,aux,pi,h,sigma1,sigma2,c0,c1,c2,c3,c4);
			} else
				if (itnum == 1 && h != hl)
					liniger2coef(m,j,a,aux,pi,h,sigma1,sigma2,c0,c1,c2,c3,c4);
			for (i=1; i<=m; i++) fl[i]=method.f(m,y,i,sigma1,sigma2);
			if (itnum == 1)
				for (i=1; i<=m; i++) {
					jfl=Basic.matvec(1,m,i,j,fl);
					dy[i]=h*(fl[i]-c4[0]*jfl);
					yl[i]=y[i]+c2[0]*fl[i]+c3[0]*jfl;
				}
			else
				for (i=1; i<=m; i++)
					dy[i]=yl[i]-y[i]+c1[0]*fl[i]-c0[0]*Basic.matvec(1,m,i,j,fl);
			Linear_algebra.sol(a,m,pi,dy);
			for (i=1; i<=m; i++) y[i] += dy[i];
			if (itnum >= itmax) break;
			eta=Math.sqrt(Basic.vecvec(1,m,0,y,y))*reta+aeta;
			discr=Math.sqrt(Basic.vecvec(1,m,0,dy,dy));
			if (eta >= discr) break;
		}
		hl=h;
		method.output(x,xe,m,y,sigma1,sigma2,j,k);
	} while (!last);
}


static private void liniger2coef(int m, double j[][], double a[][],
                    double aux[], int pi[], double h, double sigma1[],
                    double sigma2[], double c0[], double c1[],
                    double c2[], double c3[], double c4[])
{
	/* this function is internally used by LINIGER2 */

	boolean out,doublefit;
	int i,k;
	double b1,b2,r,r1,r2,ex,zeta,eta,sinl,cosl,sinh,cosh,d,p,q;

  ex=p=q=0.0;
	out=false;
	doublefit=false;
	b1=h*sigma1[0];
	b2=h*sigma2[0];
	if (b1 < 0.1) {
		p=0.0;
		q=1.0/3.0;
		out=true;
	}
	if (!out) {
		if (b2 < 0.0) {
			/* complex */
			eta=Math.abs(b1*Math.sin(sigma2[0]));
			zeta=Math.abs(b1*Math.cos(sigma2[0]));
			if (eta < b1*b1*1.0e-6) {
				b1=b2=zeta;
				doublefit=true;
			}
			if (!doublefit)
				if (zeta > 40.0) {
					p=1.0-4.0*zeta/b1/b1;
					q=4.0*(1.0-zeta)/b1/b1+1.0;
				} else {
					ex=Math.exp(zeta);
					sinl=Math.sin(eta);
					cosl=Math.cos(eta);
					sinh=0.5*(ex-1.0/ex);
					cosh=0.5*(ex+1.0/ex);
					d=eta*(cosh-cosl)-0.5*b1*b1*sinl;
					p=(zeta*sinl+eta*sinh-
            4.0*zeta*eta/b1/b1*(cosh-cosl))/d;
					q=eta*((cosh-cosl-zeta*sinh-eta*sinl)*
            4.0/b1/b1+cosh+cosl)/d;
				}
		} else if (b1 < 1.0 || b2 < 0.1) {
			/* third order */
			q=1.0/3.0;
			if (b1 > 40.0)
				r=b1/(b1-2.0);
			else {
				ex=Math.exp(-b1);
				r=b1*(1.0-ex)/(b1-2.0+(b1+2.0)*ex);
			}
			p=r/3.0-2.0/b1;
		} else if (Math.abs(b1-b2) < b1*b1*1.0e-6)
			doublefit=true;
		else {
			if (b1 > 40.0)
				r=b1/(b1-2.0);
			else {
				ex=Math.exp(-b1);
				r=b1*(1.0-ex)/(b1-2.0+(b1+2.0)*ex);
			}
			r1=r*b1;
			if (b2 > 40.0)
				r=b2/(b2-2.0);
			else {
				ex=Math.exp(-b2);
				r=b2*(1.0-ex)/(b2-2.0+(b2+2.0)*ex);
			}
			r2=r*b2;
			d=b2*r1-b1*r2;
			p=2.0*(r2-r1)/d;
			q=2.0*(b2-b1)/d;
		}
		if (doublefit) {
			b1=0.5*(b1+b2);
			if (b1 > 40.0)
				r=b1/(b1-2.0);
			else {
				ex=Math.exp(-b1);
				r=b1*(1.0-ex)/(b1-2.0+(b1+2.0)*ex);
			}
			r1=r;
			if (b1 > 40.0) ex=0.0;
			r2=b1/(1.0-ex);
			r2=1.0-ex*r2*r2;
			q=1.0/(r1*r1*r2);
			p=r1*q-2.0/b1;
		}
	}
	c0[0] = 0.25*h*h*(p+q);
	c1[0] = 0.5*h*(1.0+p);
	c2[0] = h-c1[0];
	c3[0] = 0.25*h*h*(q-p);
	c4[0] = 0.5*h*p;
	for (i=1; i<=m; i++) {
		for (k=1; k<=m; k++)
			a[i][k]=c0[0]*Basic.matmat(1,m,i,k,j,j)-c1[0]*j[i][k];
		a[i][i] += 1.0;
	}
	aux[2]=0.0;
  Linear_algebra.dec(a,m,aux,pi);
}


public static void gms(double x[], double xe, int r, double y[],
                       double h, double hmin, double hmax,
                       double delta[], AP_gms_methods method,
                       double aeta, double reta, int n[], int jev[],
                       int lu[], int nsjev, boolean linear)
{
  boolean strategy,change;
	int k,l,count,count1,kchange;
	double a,x0,eta,h0,h1,discr;
  double aux[] = new double[10];
  boolean reeval[] = new boolean[1];
  boolean update[] = new boolean[1];
  int nsjev1[] = new int[1];
  double alfa[] = new double[1];
  double s1[] = new double[1];
  double s2[] = new double[1];
  double xl0[] = new double[1];
  double xl1[] = new double[1];
  double q1[] = new double[1];
  double q2[] = new double[1];
	int ri[] = new int[r+1];
	int ci[] = new int[r+1];
	double y1[] = new double[r+1];
	double y0[] = new double[r+1];
	double yl[] = new double[3*r+1];
	double fl[] = new double[3*r+1];
	double bd1[][] = new double[4][4];
	double bd2[][] = new double[4][4];
	double hjac[][] = new double[r+1][r+1];
	double h2jac2[][] = new double[r+1][r+1];
	double rqz[][] = new double[r+1][r+1];

	/* initialization */
  eta=0.0;
	lu[0]=jev[0]=n[0]=nsjev1[0]=kchange=0;
	x0=x[0];
	discr=0.0;
	k=1;
	h1=h0=h;
	count = -2;
	aux[2]=Double.MIN_VALUE;
	aux[4]=8.0;
	Basic.dupvec(1,r,0,yl,y);
	reeval[0]=change=true;
	strategy=((hmin != hmax) && !linear);
	q1[0] = -1.0;
	q2[0] = -2.0;
	count1=0;
	xl0[0]=xl1[0]=0.0;
	method.out(x,xe,r,y,delta,n,jev,lu);
	x[0] += h1;
	/* operator construction */
	gmsopconstruct(reeval,update,r,hjac,h2jac2,rqz,y,aux,ri,ci,
                 lu,jev,nsjev1,delta,alfa,h1,h0,s1,s2,method);
	bd1[1][1]=1.0;
	bd2[1][1] = -alfa[0]*0.5;
	if (!linear)
		gmscoefficient(xl1,xl0,x0,change,n,q1,q2,h1,
		               alfa,bd1,bd2,strategy);
	while (true) {
		gmsdiffscheme(k,count,r,fl,yl,n,nsjev1,y0,alfa,bd1,bd2,h1,
                  y,hjac,h2jac2,rqz,ri,ci,delta,method);
		if (strategy) count++;
		if (count == 1) {
			/* test accuracy */
			k=2;
			Basic.dupvec(1,r,0,y1,y);
			gmsdiffscheme(k,count,r,fl,yl,n,nsjev1,y0,alfa,bd1,bd2,h1,
                    y,hjac,h2jac2,rqz,ri,ci,delta,method);
			k=3;
			eta=aeta+reta*Math.sqrt(Basic.vecvec(1,r,0,y1,y1));
			Basic.elmvec(1,r,0,y,y1,-1.0);
			discr=Math.sqrt(Basic.vecvec(1,r,0,y,y));
			Basic.dupvec(1,r,0,y,y1);
		}
		method.out(x,xe,r,y,delta,n,jev,lu);
		if (x[0] >= xe) break;
		/* step size */
		x0=x[0];
		h0=h1;
		if ((n[0] <= 2) && !linear) (k)++;
		if (count == 1) {
			a=eta/(0.75*(eta+discr))+0.33;
			h1 = (a <= 0.9 || a >= 1.1) ? a*h0 : h0;
			count=0;
			reeval[0]=(a <= 0.9 && nsjev1[0] != 1);
			count1 = (a >= 1.0 || reeval[0]) ? 0 : count1+1;
			if (count1 == 10) {
				count1=0;
				reeval[0]=true;
				h1=a*h0;
			}
		} else {
			h1=h;
			reeval[0]=((nsjev == nsjev1[0]) && !strategy && !linear);
		}
		if (strategy)
			h1 = (h1 > hmax) ? hmax : ((h1 < hmin) ? hmin : h1);
		x[0] += h1;
		if (x[0] >= xe) {
			h1=xe-x0;
			x[0]=xe;
		}
		if ((n[0] <= 2) && !linear) reeval[0]=true;
		if (h1 != h0) {
			update[0]=true;
			kchange=3;
		}
		if (reeval[0]) update[0]=true;
		change=((kchange > 0) && !linear);
		kchange--;
		if (update[0])
			/* operator construction */
			gmsopconstruct(reeval,update,r,hjac,h2jac2,rqz,y,aux,ri,ci,lu,
                     jev,nsjev1,delta,alfa,h1,h0,s1,s2,method);
		if (!linear)
			gmscoefficient(xl1,xl0,x0,change,n,q1,q2,h1,alfa,
                     bd1,bd2,strategy);
		/* next integration step */
		for (l=2; l>=1; l--) {
			Basic.dupvec(l*r+1,(l+1)*r,-r,yl,yl);
			Basic.dupvec(l*r+1,(l+1)*r,-r,fl,fl);
		}
		Basic.dupvec(1,r,0,yl,y);
	}
}


static private void gmsopconstruct(boolean reeval[],
               boolean update[], int r, double hjac[][],
               double h2jac2[][], double rqz[][], double y[],
               double aux[], int ri[], int ci[], int lu[],
               int jev[], int nsjev1[], double delta[],
               double alfa[], double h1, double h0,
               double s1[], double s2[], AP_gms_methods method)
{
	/* this function is internally used by GMS */

	int i,j;
	double a,a1,z1,e,q;

	if (reeval[0]) {
		method.jacobian(r,hjac,y,delta);
		jev[0]++;
		nsjev1[0] = 0;
		if (delta[0] <= 1.0e-15)
			alfa[0]=1.0/3.0;
		else {
			z1=h1*delta[0];
			a=z1*z1+12.0;
			a1=6.0*z1;
			if (Math.abs(z1) < 0.1)
				alfa[0]=(z1*z1/140.0-1.0)*z1/30.0;
			else if (z1 < -33.0)
				alfa[0]=(a+a1)/(3.0*z1*(2.0+z1));
			else {
				e=Math.exp(z1);
				alfa[0]=((a-a1)*e-a-a1)/(((2.0-z1)*e-2.0-z1)*z1*3.0);
			}
		}
		s1[0] = -(1.0+alfa[0])*0.5;
		s2[0]=(alfa[0]*3.0+1.0)/12.0;
	}
	a=h1/h0;
	a1=a*a;
	if (reeval[0]) a=h1;
	if (a != 1.0)
		for (j=1; j<=r; j++) Basic.colcst(1,r,j,hjac,a);
	for (i=1; i<=r; i++) {
		for (j=1; j<=r; j++) {
			q=h2jac2[i][j]=(reeval[0] ? Basic.matmat(1,r,i,j,hjac,hjac) :
									h2jac2[i][j]*a1);
			rqz[i][j]=s2[0]*q;
		}
		rqz[i][i] += 1.0;
		Basic.elmrow(1,r,i,i,rqz,hjac,s1[0]);
	}
	Linear_algebra.gsselm(rqz,r,aux,ri,ci);
	lu[0]++;
	reeval[0]=update[0]=false;
}


static private void gmscoefficient(double xl1[], double xl0[],
               double x0, boolean change, int n[], double q1[],
               double q2[], double h1, double alfa[],
               double bd1[][], double bd2[][], boolean strategy)
{
	/* this function is internally used by GMS */

	double a,q12,q22,q1q2,xl2;

	xl2=xl1[0];
	xl1[0]=xl0[0];
	xl0[0]=x0;
	if (change) {
		if (n[0] > 2) {
			q1[0]=(xl1[0]-xl0[0])/h1;
			q2[0]=(xl2-xl0[0])/h1;
		}
		q12=q1[0]*q1[0];
		q22=q2[0]*q2[0];
		q1q2=q1[0]*q2[0];
		a = -(3.0*alfa[0]+1.0)/12.0;
		bd1[1][3]=1.0+(1.0/3.0-(q1[0]+q2[0])*0.5)/q1q2;
		bd1[2][3]=(1.0/3.0-q2[0]*0.5)/(q12-q1q2);
		bd1[3][3]=(1.0/3.0-q1[0]*0.5)/(q22-q1q2);
		bd2[1][3] = -alfa[0]*0.5+a*(1.0-q1[0]-q2[0])/q1q2;
		bd2[2][3]=a*(1.0-q2[0])/(q12-q1q2);
		bd2[3][3]=a*(1.0-q1[0])/(q22-q1q2);
		if (strategy || n[0] <= 2) {
			bd1[2][2]=1.0/(2.0*q1[0]);
			bd1[1][2]=1.0-bd1[2][2];
			bd2[2][2] = -(3.0*alfa[0]+1.0)/(12.0*q1[0]);
			bd2[1][2] = -bd2[2][2]-alfa[0]*0.5;
		}
	}
}


static private void gmsdiffscheme(int k, int count, int r,
               double fl[], double yl[], int n[], int nsjev1[],
               double y0[], double alfa[], double bd1[][],
               double bd2[][], double h1, double y[],
               double hjac[][], double h2jac2[][], double rqz[][],
               int ri[], int ci[], double delta[],
               AP_gms_methods method)
{
	/* this function is internally used by GMS */

	int i,l;

	if (count != 1) {
		Basic.dupvec(1,r,0,fl,yl);
		method.derivative(r,fl,delta);
		n[0]++;
		nsjev1[0]++;
	}
	Basic.mulvec(1,r,0,y0,yl,(1.0-alfa[0])/2.0-bd1[1][k]);
	for (l=2; l<=k; l++) Basic.elmvec(1,r,r*(l-1),y0,yl,-bd1[l][k]);
	for (l=1; l<=k; l++) Basic.elmvec(1,r,r*(l-1),y0,fl,h1*bd2[l][k]);
	for (i=1; i<=r; i++) y[i]=Basic.matvec(1,r,i,hjac,y0);
	Basic.mulvec(1,r,0,y0,yl,(1.0-3.0*alfa[0])/12.0-bd2[1][k]);
	for (l=2; l<=k; l++) Basic.elmvec(1,r,r*(l-1),y0,yl,-bd2[l][k]);
	for (i=1; i<=r; i++) y[i] += Basic.matvec(1,r,i,h2jac2,y0);
	Basic.dupvec(1,r,0,y0,yl);
	for (l=1; l<=k; l++) Basic.elmvec(1,r,r*(l-1),y0,fl,h1*bd1[l][k]);
	Basic.elmvec(1,r,0,y,y0,1.0);
	Linear_algebra.solelm(rqz,r,ri,ci,y);
}


public static void impex(int n, double t0, double tend, double y0[],
                         AP_impex_methods method, double h0,
                         double hmax, boolean presch, double eps,
                         double weights[], boolean fail[])
{
  boolean start,two,halv,gotoMstp;
  int i,k,eci;
  double t,h,h2,hnew,alf,lq,alf1,c0,c1,c2,c3,
         b0,b1,b2,b3,w,sl1,sn,lr;
  double err[] = new double[4];
  double e[] = new double[5];
  double d[] = new double[5];
  double t1[] = new double[1];
  double t2[] = new double[1];
  double t3[] = new double[1];
  double tp[] = new double[1];
  int ps1[] = new int[n+1];
  int ps2[] = new int[n+1];
  double y[] = new double[n+1];
  double z[] = new double[n+1];
  double s1[] = new double[n+1];
  double s2[] = new double[n+1];
  double s3[] = new double[n+1];
  double u1[] = new double[n+1];
  double u3[] = new double[n+1];
  double w1[] = new double[n+1];
  double w2[] = new double[n+1];
  double w3[] = new double[n+1];
  double ehr[] = new double[n+1];
  double r[][] = new double[6][n+1];
  double rf[][] = new double[6][n+1];
  double a1[][] = new double[n+1][n+1];
  double a2[][] = new double[n+1][n+1];
  double kof[][] = new double[5][5];

  eci=0;
  start=halv=two=true;
  if (presch)
    h=h0;
  else {
    if (h0 > hmax)
      h=hmax;
    else
      h=h0;
    if (h > (tend-t0)/4.0) h=(tend-t0)/4.0;
  }
  hnew=h;
  alf=0.0;
  t=tp[0]=t0;
  Basic.inivec(1,3,err,0.0);
  Basic.inivec(1,n,ehr,0.0);
  Basic.duprowvec(1,n,1,r,y0);
  method.control(tp,t,h,hnew,r,err,n,tend);

  Init:  for (;;) {

    /* initialization */
    h2=hnew;
    h=h2/2.0;
    Basic.dupvec(1,n,0,s1,y0);
    Basic.dupvec(1,n,0,s2,y0);
    Basic.dupvec(1,n,0,s3,y0);
    Basic.dupvec(1,n,0,w1,y0);
    Basic.duprowvec(1,n,1,r,y0);
    Basic.inivec(1,n,u1,0.0);
    Basic.inivec(1,n,w2,0.0);
    Basic.inimat(2,5,1,n,r,0.0);
    Basic.inimat(1,5,1,n,rf,0.0);
    t=t1[0]=t0;
    t2[0]=t0-2.0*h-1.0e-6;
    t3[0]=2.0*t2[0]+1.0;
    gotoMstp=false;
    if (impexrecomp(a1,h,t,s1,ps1,n,method)) {
      fail[0] = presch;
      if (fail[0]) return;
      if (eci > 1) t -= h2;
      halv=two=false;
      hnew=h2/2.0;
      if (start)
        continue Init;
      else {
        if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
        if (start) start=false;
        if (hnew == h2) t += h2;
        eci++;
        if (t >= tend+h2) return;
        gotoMstp=true;
      }
    }
    if (!gotoMstp) {
      if (impexrecomp(a2,h2,t,w1,ps2,n,method)) {
        fail[0] = presch;
        if (fail[0]) return;
        if (eci > 1) t -= h2;
        halv=two=false;
        hnew=h2/2.0;
        if (start)
          continue Init;
        else {
          if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
          if (start) start=false;
          if (hnew == h2) t += h2;
          eci++;
          if (t >= tend+h2) return;
        }
      } else {
        start=true;
        for (eci=0; eci<=3; eci++) {
          /* one large step */
          if (impexlargestep(n,y,t,t1,t2,t3,s1,s2,s3,h,h2,
                             z,u1,u3,w1,w2,w3,ps1,ps2,weights,
                             a1,a2,eps,method)) {
            fail[0] = presch;
            if (fail[0]) return;
            if (eci > 1) t -= h2;
            halv=two=false;
            hnew=h2/2.0;
            if (start)
              continue Init;
            else {
              if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
              if (start) start=false;
              if (hnew == h2) t += h2;
              eci++;
              if (t >= tend+h2) return;
              gotoMstp=true;
              break;
            }
          } else {
            t += h2;
            if (eci > 0) {
              /* backward differences */
              impexbackdiff(n,u1,u3,w1,w2,w3,s1,s2,s3,r,rf);
              method.update(weights,s2,n);
            }
          }
        }
        if (!gotoMstp)
          eci=4;
      }
    }
    Mstp:  for (;;) {
      if (hnew != h2) {
        eci=1;
        /* change of information */
        c1=hnew/h2;
        c2=c1*c1;
        c3=c2*c1;
        kof[2][2]=c1;
        kof[2][3]=(c1-c2)/2.0;
        kof[2][4]=c3/6.0-c2/2.0+c1/3.0;
        kof[3][3]=c2;
        kof[3][4]=c2-c3;
        kof[4][4]=c3;
        for (i=1; i<=n; i++) u1[i]=r[2][i]+r[3][i]/2.0+r[4][i]/3.0;
        alf1=Basic.matvec(1,n,1,rf,u1)/Basic.vecvec(1,n,0,u1,u1);
        alf=(alf+alf1)*c1;
        for (i=1; i<=n; i++) {
          e[1]=rf[1][i]-alf1*u1[i];
          e[2]=rf[2][i]-alf1*2.0*r[3][i];
          e[3]=rf[3][i]-alf1*4.0*r[4][i];
          e[4]=rf[4][i];
          d[1]=r[1][i];
          rf[1][i] = e[1] *= c2;
          for (k=2; k<=4; k++) {
            r[k][i]=d[k]=Basic.matmat(k,4,k,i,kof,r);
            rf[k][i]=e[k]=c2*Basic.matvec(k,4,k,kof,e);
          }
          s1[i]=d[1]+e[1];
          w1[i]=d[1]+4.0*e[1];
          s2[i]=s1[i]-(d[2]+e[2]/2.0);
         s3[i]=s2[i]-(d[2]+e[2])+(d[3]+e[3]/2.0);
        }
        t3[0]=t-hnew;
        t2[0]=t-hnew/2.0;
        t1[0]=t;
        h2=hnew;
        h=h2/2.0;
        err[1]=0.0;
        if (halv) {
          for (i=1; i<=n; i++) ps2[i]=ps1[i];
          Basic.dupmat(1,n,1,n,a2,a1);
        }
        if (two) {
          for (i=1; i<=n; i++) ps1[i]=ps2[i];
          Basic.dupmat(1,n,1,n,a1,a2);
        } else
          if (impexrecomp(a1,hnew/2.0,t,s1,ps1,n,method)) {
            fail[0] = presch;
            if (fail[0]) return;
            if (eci > 1) t -= h2;
            halv=two=false;
            hnew=h2/2.0;
            if (start)
              continue Init;
            else {
              if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
              if (start) start=false;
              if (hnew == h2) t += h2;
              eci++;
              if (t >= tend+h2) return;
              continue Mstp;
            }
          }
        if (!halv)
          if (impexrecomp(a2,hnew,t,w1,ps2,n,method)) {
            fail[0] = presch;
            if (fail[0]) return;
            if (eci > 1) t -= h2;
            halv=two=false;
            hnew=h2/2.0;
            if (start)
              continue Init;
            else {
              if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
              if (start) start=false;
              if (hnew == h2) t += h2;
              eci++;
              if (t >= tend+h2) return;
              continue Mstp;
            }
          }
        /* one large step */
        if (impexlargestep(n,y,t,t1,t2,t3,s1,s2,s3,h,h2,
                           z,u1,u3,w1,w2,w3,ps1,ps2,weights,
                           a1,a2,eps,method)) {
          fail[0] = presch;
          if (fail[0]) return;
          if (eci > 1) t -= h2;
          halv=two=false;
          hnew=h2/2.0;
          if (start)
            continue Init;
          else {
            if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
            if (start) start=false;
            if (hnew == h2) t += h2;
            eci++;
            if (t >= tend+h2) return;
            continue Mstp;
          }
        }
        t += h2;
        eci=2;
      }
      /* one large step */
      if (impexlargestep(n,y,t,t1,t2,t3,s1,s2,s3,h,h2,
                         z,u1,u3,w1,w2,w3,ps1,ps2,weights,
                         a1,a2,eps,method)) {
        fail[0] = presch;
        if (fail[0]) return;
        if (eci > 1) t -= h2;
        halv=two=false;
        hnew=h2/2.0;
        if (start)
          continue Init;
        else {
          if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
          if (start) start=false;
          if (hnew == h2) t += h2;
          eci++;
          if (t >= tend+h2) return;
          continue Mstp;
        }
      }
      /* backward differences */
      impexbackdiff(n,u1,u3,w1,w2,w3,s1,s2,s3,r,rf);
      method.update(weights,s2,n);
      /* error estimates */
      c0=c1=c2=c3=0.0;
      for (i=1; i<=n; i++) {
        w=weights[i]*weights[i];
        b0=rf[4][i]/36.0;
        c0 += b0*b0*w;
        lr=Math.abs(b0);
        b1=rf[1][i]+alf*r[2][i];
        c1 += b1*b1*w;
        b2=rf[3][i];
        c2 += b2*b2*w;
        sl1=Math.abs(rf[1][i]-rf[2][i]);
        sn = (sl1 < 1.0e-10) ? 1.0 :
                    Math.abs(rf[1][i]-r[4][i]/6.0)/sl1;
        if (sn > 1.0) sn=1.0;
        if (start) {
          sn *= sn*sn*sn;
          lr *= 4.0;
        }
        ehr[i]=b3=sn*ehr[i]+lr;
        c3 += b3*b3*w;
      }
      b0=err[1];
      err[1]=b1=Math.sqrt(c0);
      err[2]=Math.sqrt(c1);
      err[3]=Math.sqrt(c3)+Math.sqrt(c2)/2.0;
      lq=eps/((b0 < b1) ? b1 : b0);
      if (b0 < b1 && lq >= 80.0) lq=10.0;
      if (eci < 4 && lq > 80.0) lq=20.0;
      halv=two=false;
      if (!presch) {
        if (lq < 1.0) {
          /* reject */
          if (start) {
            hnew=Math.pow(lq,1.0/5.0)*h/2.0;
            continue Init;
          } else {
            for (k=1; k<=4; k++) Basic.elmrow(1,n,k,k+1,r,r,-1.0);
            for (k=1; k<=3; k++) Basic.elmrow(1,n,k,k+1,r,r,-1.0);
            for (k=1; k<=4; k++) Basic.elmrow(1,n,k,k+1,rf,rf,-1.0);
            t -= h2;
            halv=true;
            hnew=h;
            continue Mstp;
          }
        } else {
          /* step size */
          if (lq < 2.0) {
            halv=true;
            hnew=h;
          } else {
            if (lq > 80.0)
              hnew=((lq > 5120.0) ?
                   Math.pow(lq/5.0,1.0/5.0) : 2.0)*h2;
            if (hnew > hmax) hnew=hmax;
            if (tend > t && tend-t < hnew) hnew=tend-t;
            two=(hnew == 2.0*h2);
          }
        }
      }

      if (tp[0] <= t) method.control(tp,t,h,hnew,r,err,n,tend);
      if (start) start=false;
      if (hnew == h2) t += h2;
      eci++;
      if (t >= tend+h2) return;
      continue Mstp;
  
    }  /*  Mstp loop */
  
  }  /*  Init loop */

}


static private boolean impexrecomp(double a[][], double h,
                       double t, double y[], int ps[],
                       int n, AP_impex_methods method)
{
	/* this function is internally used by IMPEX */

	int i,j;
	double sl,ss;
	double aux[] = new double[4];

	sl=h/2.0;
	if (!method.available(t,y,a,n)) {
		double f1[] = new double[n+1];
		double f2[] = new double[n+1];
		method.deriv(t,y,f1,n);
		for (i=1; i<=n; i++) {
			ss=1.0e-6*y[i];
			if (Math.abs(ss) < 1.0e-6) ss=1.0e-6;
			y[i] += ss;
			method.deriv(t,y,f2,n);
			for (j=1; j<=n; j++) a[j][i]=(f2[j]-f1[j])/ss;
			y[i] -= ss;
		}
	}
	for (i=1; i<=n; i++) {
		Basic.mulrow(1,n,i,i,a,a,-sl);
		a[i][i] += 1.0;
	}
	aux[2]=1.0e-14;
  Linear_algebra.dec(a,n,aux,ps);
	if (aux[3] < n)
		return true;
	else
		return false;
}


static private boolean impexlargestep(int n, double y[], double t,
               double t1[], double t2[], double t3[], double s1[],
               double s2[], double s3[], double h, double h2,
               double z[], double u1[], double u3[], double w1[],
               double w2[], double w3[], int ps1[], int ps2[],
               double weights[], double a1[][], double a2[][],
               double eps, AP_impex_methods method)
{
	/* this function is internally used by IMPEX */

	double a,b,c;

	a=(t+h-t1[0])/(t1[0]-t2[0]);
	b=(t+h-t2[0])/(t1[0]-t3[0]);
	c=(t+h-t1[0])/(t2[0]-t3[0])*b;
	b *= a;
	a += 1.0+b;
	b=a+c-1.0;
	Basic.mulvec(1,n,0,z,s1,a);
	Basic.elmvec(1,n,0,z,s2,-b);
	Basic.elmvec(1,n,0,z,s3,c);
	if (impexiterate(z,s1,a1,h,t+h/2.0,weights,ps1,
                   n,eps,method)) return true;
	Basic.dupvec(1,n,0,y,z);
	a=(t+h2-t1[0])/(t1[0]-t2[0]);
	b=(t+h2-t2[0])/(t1[0]-t3[0]);
	c=(t+h2-t1[0])/(t2[0]-t3[0])*b;
	b *= a;
	a += 1.0+b;
	b=a+c-1.0;
	Basic.mulvec(1,n,0,z,s1,a);
	Basic.elmvec(1,n,0,z,s2,-b);
	Basic.elmvec(1,n,0,z,s3,c);
	if (impexiterate(z,y,a1,h,t+3.0*h/2.0,weights,ps1,
                   n,eps,method)) return true;
	Basic.dupvec(1,n,0,u3,u1);
	Basic.dupvec(1,n,0,u1,y);
	Basic.dupvec(1,n,0,s3,s2);
	Basic.dupvec(1,n,0,s2,s1);
	Basic.dupvec(1,n,0,s1,z);
	Basic.elmvec(1,n,0,z,w1,1.0);
	Basic.elmvec(1,n,0,z,s2,-1.0);
	if (impexiterate(z,w1,a2,h2,t+h,weights,ps2,
                   n,eps,method)) return true;
	t3[0]=t2[0];
	t2[0]=t1[0];
	t1[0]=t+h2;
	Basic.dupvec(1,n,0,w3,w2);
	Basic.dupvec(1,n,0,w2,w1);
	Basic.dupvec(1,n,0,w1,z);
	return false;
}


static private boolean impexiterate(double z[], double y[],
                       double a[][], double h, double t,
                       double weights[], int ps[], int n,
                       double eps, AP_impex_methods method)
{
	/* this function is internally used by IMPEXLARGESTEP (IMPEX) */

	int i,it,lit;
	double max,max1,conv,temp;
	double f1[] = new double[n+1];
	double dz[] = new double[n+1];

  max1=0.0;
	for (i=1; i<=n; i++) z[i]=(z[i]+y[i])/2.0;
	it=lit=1;
	conv=1.0;
	while (true) {
		method.deriv(t,z,f1,n);
		for (i=1; i<=n; i++) f1[i]=dz[i]=z[i]-h*f1[i]/2.0-y[i];
		Linear_algebra.sol(a,n,ps,dz);
		Basic.elmvec(1,n,0,z,dz,-1.0);
		max=0.0;
		for (i=1; i<=n; i++) {
			temp=weights[i]*dz[i];
			max += temp*temp;
		}
		max=Math.sqrt(max);
		if (max*conv < eps/10.0) break;
		it++;
		if (it != 2) {
			conv=max/max1;
			if (conv > 0.2) {
				if (lit == 0) {
					return true;
				}
				lit=0;
				conv=1.0;
				it=1;
				if (impexrecomp(a,h,t,z,ps,n,method)) {
					return true;
				}
			}
		}
		max1=max;
	}
  for (i=1; i<=n; i++) z[i]=2.0*z[i]-y[i];
	return false;
}


static private void impexbackdiff(int n, double u1[],
               double u3[], double w1[], double w2[],
               double w3[], double s1[], double s2[],
               double s3[], double r[][], double rf[][])
{
	/* this function is internally used by IMPEX */

	int i,k;
	double b0,b1,b2,b3;

	for (i=1; i<=n; i++) {
		b1=(u1[i]+2.0*s2[i]+u3[i])/4.0;
		b2=(w1[i]+2.0*w2[i]+w3[i])/4.0;
		b3=(s3[i]+2.0*u3[i]+s2[i])/4.0;
		b2=(b2-b1)/3.0;
		b0=b1-b2;
		b2 -= (s1[i]-2.0*s2[i]+s3[i])/16.0;
		b1=2.0*b3-(b2+rf[1][i])-(b0+r[1][i])/2.0;
		b3=0.0;
		for (k=1; k<=4; k++) {
			b1 -= b3;
			b3=r[k][i];
			r[k][i]=b0;
			b0 -= b1;
		}
		r[5][i]=b0;
		for (k=1; k<=4; k++) {
			b3=rf[k][i];
			rf[k][i]=b2;
			b2 -= b3;
		}
		rf[5][i]=b2;
	}
}


public static void modifiedtaylor(double t[], double te, int m0,
              int m, double u[], AP_modifiedtaylor_methods method,
              double taumin, int k[], int order, int accuracy,
              double data[], double alfa, int norm, double eta[],
              double rho[], double xtmp[])
{
  boolean last,start,step1;
	int i,n,p,q,j;
	double ec0,ec1,ec2,tau0,tau1,tau2,taus,t2,t0,tau,taui,tauec,
         ecl,betan,gamma,ifac,tauacc,taustab,aa,bb,cc,ec,s,x,b;

  tauec=ecl=0.0;
  ec0=xtmp[0];
  ec1=xtmp[1];
  ec2=xtmp[2];
  tau0=xtmp[3];
  tau1=xtmp[4];
  tau2=xtmp[5];
  taus=xtmp[6];
  t2=xtmp[7];
  step1=true;
  n=order;
	double beta[] = new double[n+1];
	double betha[] = new double[n+1];
	double c[] = new double[m+1];
	i=0;
	start=(k[0] == 0);
	t0=t[0];
	/* coefficient */
	ifac=1.0;
	gamma=0.5;
  p=accuracy;
	betan=data[0];
	q = (p < n) ? p+1 : n;
	for (j=1; j<=n; j++) {
		beta[j]=data[j];
		ifac /= j;
		betha[j]=ifac-beta[j];
	}
	if (p == n) betha[n]=ifac;
	last=false;
	do {
		/* step size */
		s=0.0;
		if (norm == 1)
			for (j=m0; j<=m; j++) {
				x=Math.abs(u[j]);
				if (x > s) s=x;
			}
		else
			s=Math.sqrt(Basic.vecvec(m0,m,0,u,u));
		/* local error bound */
		eta[0] = method.aeta(t,m0,m)+method.reta(t,m0,m)*s;
		if (eta[0] > 0.0) {
			if (start) {
				if (k[0] == 0) {
					for (j=m0; j<=m; j++) c[j]=u[j];
					i=1;
					method.derivative(t,m0,m,i,c);
					s=0.0;
					if (norm == 1)
						for (j=m0; j<=m; j++) {
							x=Math.abs(c[j]);
							if (x > s) s=x;
						}
					else
						s=Math.sqrt(Basic.vecvec(m0,m,0,c,c));
					tauacc=eta[0]/s;
					step1=true;
				} else if (step1) {
					tauacc=Math.pow(eta[0]/rho[0],1.0/q)*tau2;
					if (tauacc > 10.0*tau2)
						tauacc=10.0*tau2;
					else
						step1=false;
				} else {
					bb=(ec2-ec1)/tau1;
					cc=ec2-bb*t2;
					ec=bb*t[0]+cc;
					tauacc = (ec < 0.0) ? tau2 : Math.pow(eta[0]/ec,1.0/q);
					start=false;
				}
			} else {
				aa=((ec0-ec1)/tau0+(ec2-ec1)/tau1)/(tau1+tau0);
				bb=(ec2-ec1)/tau1-aa*(2.0*t2-tau1);
				cc=ec2-t2*(bb+aa*t2);
				ec=cc+t[0]*(bb+t[0]*aa);
				tauacc = (ec < 0.0) ? taus : Math.pow(eta[0]/ec,1.0/q);
				if (tauacc > alfa*taus) tauacc=alfa*taus;
				if (tauacc < gamma*taus) tauacc=gamma*taus;
			}
		} else
			tauacc=te-t[0];
		if (tauacc < taumin) tauacc=taumin;
		taustab=betan/method.sigma(t,m0,m);
		if (taustab < 1.0e-12*(t[0]-t0)) {
			method.out(t,te,m0,m,u,k,eta,rho);
			break;
		}
		tau = (tauacc > taustab) ? taustab : tauacc;
		taus=tau;
		if (tau >= te-t[0]) {
			tau=te-t[0];
			last=true;
		}
		tau0=tau1;
		tau1=tau2;
		tau2=tau;
		k[0]++;
		i=0;
		/* difference scheme */
		for (j=m0; j<=m; j++) c[j]=u[j];
		taui=1.0;
		do {
			i++;
			method.derivative(t,m0,m,i,c);
			taui *= tau;
			b=beta[i]*taui;
			if (eta[0] > 0.0 && i >= p) {
				/* local error construction */
				if (i == p) {
					ecl=0.0;
					tauec=1.0;
				}
				if (i > p+1) tauec *= tau;
				s=0.0;
				if (norm == 1)
					for (j=m0; j<=m; j++) {
						x=Math.abs(c[j]);
						if (x > s) s=x;
					}
				else
					s=Math.sqrt(Basic.vecvec(m0,m,0,c,c));
				ecl += Math.abs(betha[i])*tauec*s;
				if (i == n) {
					ec0=ec1;
					ec1=ec2;
					ec2=ecl;
					rho[0] = ecl*Math.pow(tau,q);
				}
			}
			for (j=m0; j<=m; j++) u[j] += b*c[j];
		} while (i < n);
		t2=t[0];
		if (last) {
			last=false;
			t[0]=te;
		} else
			t[0] += tau;
		method.out(t,te,m0,m,u,k,eta,rho);
	} while (t[0] != te);
	
  xtmp[0]=ec0;
  xtmp[1]=ec1;
  xtmp[2]=ec2;
  xtmp[3]=tau0;
  xtmp[4]=tau1;
  xtmp[5]=tau2;
  xtmp[6]=taus;
  xtmp[7]=t2;
}


public static void eft(double t[], double te, int m0, int m,
              double u[], AP_eft_methods method, double phi,
              int k[], double alfa, int norm, double eta[],
              double rho[], double hmin, double hstart[])
{
  boolean extrapolate,last,start;
	int kl,j,i,ext;
	double q,ec0,ec1,ec2,h,hi,h0,h1,h2,betan,t2,sigmal,phil,s,x,
         hacc,hstab,hcr,hmax,a,b,cc,b1,b2,bb,e,beta2,beta3,c0,
         fc,b0,fb,a0,fa,d0,fd,fdb,fda,w,mb,tol,mm,p0,q0;
  double beta[] = new double[4];
  double betha[] = new double[4];
	double c[] = new double[m+1];
	double ro[] = new double[m+1];

  kl=0;
  q=h2=h0=ec2=ec1=ec0=h1=t2=fd=d0=0.0;
	start=true;
	last=false;
	Basic.dupvec(m0,m,0,c,u);
	method.derivative(t,m0,m,1,c);
	if (k[0] == 0) {
		/* local error bound */
		s=0.0;
		if (norm == 1)
			for (j=m0; j<=m; j++) {
				x=Math.abs(u[j]);
				if (x > s) s=x;
			}
		else
			s=Math.sqrt(Basic.vecvec(m0,m,0,u,u));
		eta[0] = method.aeta(t,m0,m)+method.reta(t,m0,m)*s;
		s=0.0;
		if (norm == 1)
			for (j=m0; j<=m; j++) {
				x=Math.abs(c[j]);
				if (x > s) s=x;
			}
		else
			s=Math.sqrt(Basic.vecvec(m0,m,0,c,c));
		hstart[0] = eta[0]/s;
	}
	do {
		/* difference scheme */
		hi=1.0;
		sigmal=method.sigma(t,m0,m);
		phil=phi;
		/* step size */
		if (!start) {
			/* local error bound */
			s=0.0;
			if (norm == 1)
				for (j=m0; j<=m; j++) {
					x=Math.abs(u[j]);
					if (x > s) s=x;
				}
			else
				s=Math.sqrt(Basic.vecvec(m0,m,0,u,u));
			eta[0] = method.aeta(t,m0,m)+method.reta(t,m0,m)*s;
		}
		if (start) {
			h1=h2=hacc=hstart[0];
			ec2=ec1=1.0;
			kl=1;
			start=false;
		} else if (kl < 3) {
			hacc=Math.pow(eta[0]/rho[0],1.0/q)*h2;
			if (hacc > 10.0*h2)
				hacc=10.0*h2;
			else
				kl++;
		} else {
			a=(h0*(ec2-ec1)-h1*(ec1-ec0))/(h2*h0-h1*h1);
			h=h2*((eta[0] < rho[0]) ? 
           Math.pow(eta[0]/rho[0],1.0/q) : alfa);
			if (a > 0.0) {
				b=(ec2-ec1-a*(h2-h1))/h1;
				cc=ec2-a*h2-b*t2;
				hacc=0.0;
				hmax=h;
				/* find zero */
				b0=hacc;
				fb=Math.pow(hacc,q)*(a*hacc+b*t[0]+cc)-eta[0];
				a0=hacc=h;
				fa=Math.pow(hacc,q)*(a*hacc+b*t[0]+cc)-eta[0];
				c0=a0;
				fc=fa;
				ext=0;
				extrapolate=true;
				while (extrapolate) {
					if (Math.abs(fc) < Math.abs(fb)) {
						if (c0 != a0) {
							d0=a0;
							fd=fa;
						}
						a0=b0;
						fa=fb;
						b0=hacc=c0;
						fb=fc;
						c0=a0;
						fc=fa;
					}
					tol=1.0e-3*h2;
					mm=(c0+b0)*0.5;
					mb=mm-b0;
					if (Math.abs(mb) > tol) {
						if (ext > 2)
							w=mb;
						else {
							if (mb == 0.0)
								tol=0.0;
							else
								if (mb < 0.0) tol = -tol;
							p0=(b0-a0)*fb;
							if (ext <= 1)
								q0=fa-fb;
							else {
								fdb=(fd-fb)/(d0-b0);
								fda=(fd-fa)/(d0-a0);
								p0 *= fda;
								q0=fdb*fa-fda*fb;
							}
							if (p0 < 0.0) {
								p0 = -p0;
								q0 = -q0;
							}
							w=(p0<Double.MIN_VALUE || p0<=q0*tol) ? tol :
										((p0<mb*q0) ? p0/q0 : mb);
						}
						d0=a0;
						fd=fa;
						a0=b0;
						fa=fb;
						hacc = b0 += w;
						fb=Math.pow(hacc,q)*(a*hacc+b*t[0]+cc)-eta[0];
						if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
							c0=a0;
							fc=fa;
							ext=0;
						} else
							ext = (w == mb) ? 0 : ext+1;
					} else
						break;
				}
				h=c0;
				if (!((fc >= 0.0) ? (fb <= 0.0) : (fb >= 0.0)))
					hacc=hmax;
			} else
				hacc=h;
			if (hacc < 0.5*h2) hacc=0.5*h2;
		}
		if (hacc < hmin) hacc=hmin;
		h=hacc;
		if (h*sigmal > 1.0) {
			a=Math.abs(method.diameter(t,m0,m)/
                 sigmal+Double.MIN_VALUE)/2.0;
			b=2.0*Math.abs(Math.sin(phil));
			betan=((a > b) ? 1.0/a : 1.0/b)/a;
			hstab=Math.abs(betan/sigmal);
			if (hstab < 1.0e-14*t[0]) break;
			if (h > hstab) h=hstab;
		}
		hcr=h2*h2/h1;
		if (kl > 2 && Math.abs(h-hcr) < Double.MIN_VALUE*hcr)
			h = (h < hcr) ? hcr*(1.0-Double.MIN_VALUE) :
								hcr*(1.0+Double.MIN_VALUE);
		if (t[0]+h > te) {
			last=true;
			hstart[0] = h;
			h=te-t[0];
		}
		h0=h1;
		h1=h2;
		h2=h;
		/* coefficient */
		b=h*sigmal;
		b1=b*Math.cos(phil);
		bb=b*b;
		if (Math.abs(b) < 1.0e-3) {
			beta2=0.5-bb/24.0;
			beta3=1.0/6.0+b1/12.0;
			betha[3]=0.5+b1/3.0;
		} else if (b1 < -40.0) {
			beta2=(-2.0*b1-4.0*b1*b1/bb+1.0)/bb;
			beta3=(1.0+2.0*b1/bb)/bb;
			betha[3]=1.0/bb;
		} else {
			e=Math.exp(b1)/bb;
			b2=b*Math.sin(phil);
			beta2=(-2.0*b1-4.0*b1*b1/bb+1.0)/bb;
			beta3=(1.0+2.0*b1/bb)/bb;
			if (Math.abs(b2/b) < 1.0e-5) {
				beta2 -= e*(b1-3.0);
				beta3 += e*(b1-2.0)/b1;
				betha[3]=1.0/bb+e*(b1-1.0);
			} else {
				beta2 -= e*Math.sin(b2-3.0*phil)/b2*b;
				beta3 += e*Math.sin(b2-2.0*phil)/b2;
				betha[3]=1.0/bb+e*Math.sin(b2-phil)/b2*b;
			}
		}
		beta[1]=betha[1]=1.0;
		beta[2]=beta2;
		beta[3]=beta3;
		betha[2]=1.0-bb*beta3;
		b=Math.abs(b);
		q = (b < 1.5) ? 4.0-2.0*b/3.0 :
					((b < 6.0) ? (30.0-2.0*b)/9.0 : 2.0);
		for (i=1; i<=3; i++) {
			hi *= h;
			if (i > 1) method.derivative(t,m0,m,i,c);
			/* local error construction */
			if (i == 1) Basic.inivec(m0,m,ro,0.0);
			if (i < 4) Basic.elmvec(m0,m,0,ro,c,betha[i]*hi);
			if (i == 4) {
				Basic.elmvec(m0,m,0,ro,c,-h);
				s=0.0;
				if (norm == 1)
					for (j=m0; j<=m; j++) {
						x=Math.abs(ro[j]);
						if (x > s) s=x;
					}
				else
					s=Math.sqrt(Basic.vecvec(m0,m,0,ro,ro));
				rho[0]=s;
				ec0=ec1;
				ec1=ec2;
				ec2=rho[0]/Math.pow(h,q);
			}
			Basic.elmvec(m0,m,0,u,c,beta[i]*hi);
		}
		t2=t[0];
		k[0]++;
		if (last) {
			last=false;
			t[0]=te;
			start=true;
		} else
			t[0] += h;
		Basic.dupvec(m0,m,0,c,u);
		method.derivative(t,m0,m,1,c);
		/* local error construction */
		Basic.elmvec(m0,m,0,ro,c,-h);
		s=0.0;
		if (norm == 1)
			for (j=m0; j<=m; j++) {
				x=Math.abs(ro[j]);
				if (x > s) s=x;
			}
		else
			s=Math.sqrt(Basic.vecvec(m0,m,0,ro,ro));
		rho[0]=s;
		ec0=ec1;
		ec1=ec2;
		ec2=rho[0]/Math.pow(h,q);
		method.out(t,te,m0,m,u,k,eta,rho);
	} while (t[0] != te);
}


public static void rk2(double x[], double a, double b,
                       double y[], double ya, double z[],
                       double za, AP_rk2_method method,
                       double e[], double d[], boolean fi)
{
  boolean last,first,reject,test,ta,tb;
	double e1,e2,e3,e4,xl,yl,zl,h,ind,hmin,hl,absh,k0,k1,k2,
         k3,k4,k5,discry,discrz,toly,tolz,mu,mu1,fhy,fhz;

  last=true;
  mu1=0.0;
	if (fi) {
		d[3]=a;
		d[4]=ya;
		d[5]=za;
	}
	d[1]=0.0;
	xl=d[3];
	yl=d[4];
	zl=d[5];
	if (fi) d[2]=b-d[3];
	absh=h=Math.abs(d[2]);
	if (b-xl < 0.0) h = -h;
	ind=Math.abs(b-xl);
	hmin=ind*e[1]+e[2];
	hl=ind*e[3]+e[4];
	if (hl < hmin) hmin=hl;
	e1=e[1]/ind;
	e2=e[2]/ind;
	e3=e[3]/ind;
	e4=e[4]/ind;
	first=true;
	test=true;
	if (fi) {
		last=true;
		test=false;
	}
	while (true) {
		if (test) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (h > 0.0) ? hmin : -hmin;
				absh=hmin;
			}
			ta=(h >= b-xl);
			tb=(h >= 0.0);
			if ((ta && tb) || (!(ta || tb))) {
				d[2]=h;
				last=true;
				h=b-xl;
				absh=Math.abs(h);
			} else
				last=false;
		}
		test=true;
		x[0]=xl;
		y[0]=yl;
		z[0]=zl;
		k0=method.fxyz(x,y,z)*h;
		x[0]=xl+h/4.5;
		y[0]=yl+(zl*18.0+k0*2.0)/81.0*h;
		z[0]=zl+k0/4.5;
		k1=method.fxyz(x,y,z)*h;
		x[0]=xl+h/3.0;
		y[0]=yl+(zl*6.0+k0)/18.0*h;
		z[0]=zl+(k0+k1*3.0)/12.0;
		k2=method.fxyz(x,y,z)*h;
		x[0]=xl+h*0.5;
		y[0]=yl+(zl*8.0+k0+k2)/16.0*h;
		z[0]=zl+(k0+k2*3.0)/8.0;
		k3=method.fxyz(x,y,z)*h;
		x[0]=xl+h*0.8;
		y[0]=yl+(zl*100.0+k0*12.0+k3*28.0)/125.0*h;
		z[0]=zl+(k0*53.0-k1*135.0+k2*126.0+k3*56.0)/125.0;
		k4=method.fxyz(x,y,z)*h;
		x[0] = (last ? b : xl+h);
		y[0]=yl+(zl*336.0+k0*21.0+k2*92.0+k4*55.0)/336.0*h;
		z[0]=zl+(k0*133.0-k1*378.0+k2*276.0+k3*112.0+k4*25.0)/168.0;
		k5=method.fxyz(x,y,z)*h;
		discry=Math.abs((-k0*21.0+k2*108.0-k3*112.0+k4*25.0)/56.0*h);
		discrz=Math.abs(k0*21.0-k2*162.0+k3*224.0-k4*125.0+k5*42.0)/14.0;
		toly=absh*(Math.abs(zl)*e1+e2);
		tolz=Math.abs(k0)*e3+absh*e4;
		reject=(discry > toly || discrz > tolz);
		fhy=discry/toly;
		fhz=discrz/tolz;
		if (fhz > fhy) fhy=fhz;
		mu=1.0/(1.0+fhy)+0.45;
		if (reject) {
			if (absh <= hmin) {
				d[1] += 1.0;
				y[0]=yl;
				z[0]=zl;
				first=true;
				if (b == x[0]) break;
				xl = x[0];
				yl = y[0];
				zl = z[0];
			} else
				h *= mu;
		} else {
			if (first) {
				first=false;
				hl=h;
				h *= mu;
			} else {
				fhy=mu*h/hl+mu-mu1;
				hl=h;
				h *= fhy;
			}
			mu1=mu;
			y[0]=yl+(zl*56.0+k0*7.0+k2*36.0-k4*15.0)/56.0*hl;
			z[0]=zl+(-k0*63.0+k1*189.0-k2*36.0-k3*112.0+k4*50.0)/28.0;
			k5=method.fxyz(x,y,z)*hl;
			y[0]=yl+(zl*336.0+k0*35.0+k2*108.0+k4*25.0)/336.0*hl;
			z[0]=zl+(k0*35.0+k2*162.0+k4*125.0+k5*14.0)/336.0;
			if (b == x[0]) break;
			xl = x[0];
			yl = y[0];
			zl = z[0];
		}
	}
	if (!last) d[2]=h;
	d[3] = x[0];
	d[4] = y[0];
	d[5] = z[0];
}


public static void rk2n(double x[], double a, double b,
                        double y[], double ya[], double z[],
                        double za[], AP_rk2n_method method,
                        double e[], double d[], boolean fi, int n)
{
  boolean last,first,reject,test,ta,tb;
	int j,jj;
	double xl,h,ind,hmin,hl,absh,fhm,discry,discrz,toly,tolz,
         mu,mu1,fhy,fhz;
	double yl[] = new double[n+1];
  double zl[] = new double[n+1];
  double k0[] = new double[n+1];
  double k1[] = new double[n+1];
  double k2[] = new double[n+1];
  double k3[] = new double[n+1];
  double k4[] = new double[n+1];
  double k5[] = new double[n+1];
	double ee[] = new double[4*n+1];
	
  last=true;
  hl=mu1=0.0;
	if (fi) {
		d[3]=a;
		for (jj=1; jj<=n; jj++) {
			d[jj+3]=ya[jj];
			d[n+jj+3]=za[jj];
		}
	}
	d[1]=0.0;
	xl=d[3];
	for (jj=1; jj<=n; jj++) {
		yl[jj]=d[jj+3];
		zl[jj]=d[n+jj+3];
	}
	if (fi) d[2]=b-d[3];
	absh=h=Math.abs(d[2]);
	if (b-xl < 0.0) h = -h;
	ind=Math.abs(b-xl);
	hmin=ind*e[1]+e[2];
	for (jj=2; jj<=2*n; jj++) {
		hl=ind*e[2*jj-1]+e[2*jj];
		if (hl < hmin) hmin=hl;
	}
	for (jj=1; jj<=4*n; jj++) ee[jj]=e[jj]/ind;
	first=true;
	test=true;
	if (fi) {
		last=true;
		test=false;
	}
	while (true) {
		if (test) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (h > 0.0) ? hmin : -hmin;
				absh=Math.abs(h);
			}
			ta=(h >= b-xl);
			tb=(h >= 0.0);
			if ((ta && tb) || (!(ta || tb))) {
				d[2]=h;
				last=true;
				h=b-xl;
				absh=Math.abs(h);
			} else
				last=false;
		}
		test=true;
		x[0]=xl;
		for (jj=1; jj<=n; jj++) {
			y[jj]=yl[jj];
			z[jj]=zl[jj];
		}
		for (j=1; j<=n; j++)	k0[j]=method.fxyzj(n,j,x,y,z)*h;
		x[0]=xl+h/4.5;
		for (jj=1; jj<=n; jj++) {
			y[jj]=yl[jj]+(zl[jj]*18.0+k0[jj]*2.0)/81.0*h;
			z[jj]=zl[jj]+k0[jj]/4.5;
		}
		for (j=1; j<=n; j++) k1[j]=method.fxyzj(n,j,x,y,z)*h;
		x[0]=xl+h/3.0;
		for (jj=1; jj<=n; jj++) {
			y[jj]=yl[jj]+(zl[jj]*6.0+k0[jj])/18.0*h;
			z[jj]=zl[jj]+(k0[jj]+k1[jj]*3.0)/12.0;
		}
		for (j=1; j<=n; j++) k2[j]=method.fxyzj(n,j,x,y,z)*h;
		x[0]=xl+h*0.5;
		for (jj=1; jj<=n; jj++) {
			y[jj]=yl[jj]+(zl[jj]*8.0+k0[jj]+k2[jj])/16.0*h;
			z[jj]=zl[jj]+(k0[jj]+k2[jj]*3.0)/8.0;
		}
		for (j=1; j<=n; j++) k3[j]=method.fxyzj(n,j,x,y,z)*h;
		x[0]=xl+h*0.8;
		for (jj=1; jj<=n; jj++) {
			y[jj]=yl[jj]+(zl[jj]*100.0+k0[jj]*12.0+k3[jj]*28.0)/125.0*h;
			z[jj]=zl[jj]+(k0[jj]*53.0-k1[jj]*135.0+k2[jj]*126.0+
						k3[jj]*56.0)/125.0;
		}
		for (j=1; j<=n; j++) k4[j]=method.fxyzj(n,j,x,y,z)*h;
		x[0] = (last ? b : xl+h);
		for (jj=1; jj<=n; jj++) {
			y[jj]=yl[jj]+(zl[jj]*336.0+k0[jj]*21.0+k2[jj]*92.0+
						k4[jj]*55.0)/336.0*h;
			z[jj]=zl[jj]+(k0[jj]*133.0-k1[jj]*378.0+k2[jj]*276.0+
						k3[jj]*112.0+k4[jj]*25.0)/168.0;
		}
		for (j=1; j<=n; j++) k5[j]=method.fxyzj(n,j,x,y,z)*h;
		reject=false;
		fhm=0.0;
		for (jj=1; jj<=n; jj++) {
			discry=Math.abs((-k0[jj]*21.0+k2[jj]*108.0-k3[jj]*112.0+
                       k4[jj]*25.0)/56.0*h);
			discrz=Math.abs(k0[jj]*21.0-k2[jj]*162.0+k3[jj]*224.0-
                      k4[jj]*125.0+k5[jj]*42.0)/14.0;
			toly=absh*(Math.abs(zl[jj])*ee[2*jj-1]+ee[2*jj]);
			tolz=Math.abs(k0[jj])*ee[2*(jj+n)-1]+absh*ee[2*(jj+n)];
			reject=((discry > toly) || (discrz > tolz) || reject);
			fhy=discry/toly;
			fhz=discrz/tolz;
			if (fhz > fhy) fhy=fhz;
			if (fhy > fhm) fhm=fhy;
		}
		mu=1.0/(1.0+fhm)+0.45;
		if (reject) {
			if (absh <= hmin) {
				d[1] += 1.0;
				for (jj=1; jj<=n; jj++) {
					y[jj]=yl[jj];
					z[jj]=zl[jj];
				}
				first=true;
				if (b == x[0]) break;
				xl = x[0];
				for (jj=1; jj<=n; jj++) {
					yl[jj] = y[jj];
					zl[jj] = z[jj];
				}
			} else
				h *= mu;
		} else {
			if (first) {
				first=false;
				hl=h;
				h *= mu;
			} else {
				fhm=mu*h/hl+mu-mu1;
				hl=h;
				h *= fhm;
			}
			mu1=mu;
			for (jj=1; jj<=n; jj++) {
				y[jj]=yl[jj]+(zl[jj]*56.0+k0[jj]*7.0+k2[jj]*36.0-
							k4[jj]*15.0)/56.0*hl;
				z[jj]=zl[jj]+(-k0[jj]*63.0+k1[jj]*189.0-k2[jj]*36.0-
							k3[jj]*112.0+k4[jj]*50.0)/28.0;
			}
			for (j=1; j<=n; j++) k5[j]=method.fxyzj(n,j,x,y,z)*hl;
			for (jj=1; jj<=n; jj++) {
				y[jj]=yl[jj]+(zl[jj]*336.0+k0[jj]*35.0+k2[jj]*108.0+
							k4[jj]*25.0)/336.0*hl;
				z[jj]=zl[jj]+(k0[jj]*35.0+k2[jj]*162.0+k4[jj]*125.0+
							k5[jj]*14.0)/336.0;
			}
			if (b == x[0]) break;
			xl = x[0];
			for (jj=1; jj<=n; jj++) {
				yl[jj] = y[jj];
				zl[jj] = z[jj];
			}
		}
	}
	if (!last) d[2]=h;
	d[3] = x[0];
	for (jj=1; jj<=n; jj++) {
		d[jj+3]=y[jj];
		d[n+jj+3]=z[jj];
	}
}


public static void rk3(double x[], double a, double b,
                       double y[], double ya, double z[],
                       double za, AP_rk3_method method,
                       double e[], double d[], boolean fi)
{
  boolean last,first,reject,test,ta,tb;
	double e1,e2,e3,e4,xl,yl,zl,h,ind,hmin,hl,absh,k0,k1,k2,
         k3,k4,k5,discry,discrz,toly,tolz,mu,mu1,fhy,fhz;

  k5=mu1=0.0;
  last=true;
	if (fi) {
		d[3]=a;
		d[4]=ya;
		d[5]=za;
	}
	d[1]=0.0;
	xl=d[3];
	yl=d[4];
	zl=d[5];
	if (fi) d[2]=b-d[3];
	absh=h=Math.abs(d[2]);
	if (b-xl < 0.0) h = -h;
	ind=Math.abs(b-xl);
	hmin=ind*e[1]+e[2];
	hl=ind*e[3]+e[4];
	if (hl < hmin) hmin=hl;
	e1=e[1]/ind;
	e2=e[2]/ind;
	e3=e[3]/ind;
	e4=e[4]/ind;
	first=reject=true;
	test=true;
	if (fi) {
		last=true;
		test=false;
	}
	while (true) {
		if (test) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (h > 0.0) ? hmin : -hmin;
				absh=hmin;
			}
			ta=(h >= b-xl);
			tb=(h >= 0.0);
			if ((ta && tb) || (!(ta || tb))) {
				d[2]=h;
				last=true;
				h=b-xl;
				absh=Math.abs(h);
			} else
				last=false;
		}
		test=true;
		if (reject) {
			x[0]=xl;
			y[0]=yl;
			k0=method.fxy(x,y)*h;
		} else
			k0=k5*h/hl;
		x[0]=xl+0.276393202250021*h;
		y[0]=yl+(zl*0.276393202250021+k0*0.038196601125011)*h;
		k1=method.fxy(x,y)*h;
		x[0]=xl+0.723606797749979*h;
		y[0]=yl+(zl*0.723606797749979+k1*0.261803398874989)*h;
		k2=method.fxy(x,y)*h;
		x[0]=xl+h*0.5;
		y[0]=yl+(zl*0.5+k0*0.046875+k1*0.079824155839840-
         k2*0.001699155839840)*h;
		k4=method.fxy(x,y)*h;
		x[0] = (last ? b : xl+h);
		y[0]=yl+(zl+k0*0.309016994374947+k2*0.190983005625053)*h;
		k3=method.fxy(x,y)*h;
		y[0]=yl+(zl+k0*0.083333333333333+k1*0.301502832395825+
         k2*0.115163834270842)*h;
		k5=method.fxy(x,y)*h;
		discry=Math.abs((-k0*0.5+k1*1.809016994374947+
           k2*0.690983005625053-k4*2.0)*h);
		discrz=Math.abs((k0-k3)*2.0-(k1+k2)*10.0+k4*16.0+k5*4.0);
		toly=absh*(Math.abs(zl)*e1+e2);
		tolz=Math.abs(k0)*e3+absh*e4;
		reject=(discry > toly || discrz > tolz);
		fhy=discry/toly;
		fhz=discrz/tolz;
		if (fhz > fhy) fhy=fhz;
		mu=1.0/(1.0+fhy)+0.45;
		if (reject) {
			if (absh <= hmin) {
				d[1] += 1.0;
				y[0]=yl;
				z[0]=zl;
				first=true;
				if (b == x[0]) break;
				xl = x[0];
				yl = y[0];
				zl = z[0];
			} else
				h *= mu;
		} else {
			if (first) {
				first=false;
				hl=h;
				h *= mu;
			} else {
				fhy=mu*h/hl+mu-mu1;
				hl=h;
				h *= fhy;
			}
			mu1=mu;
			z[0]=zl+(k0+k3)*0.083333333333333+(k1+k2)*0.416666666666667;
			if (b == x[0]) break;
			xl = x[0];
			yl = y[0];
			zl = z[0];
		}
	}
	if (!last) d[2]=h;
	d[3] = x[0];
	d[4] = y[0];
	d[5] = z[0];
}


public static void rk3n(double x[], double a, double b,
                        double y[], double ya[], double z[],
                        double za[], AP_rk3n_method method,
                        double e[], double d[], boolean fi, int n)
{
  boolean last,first,reject,test,ta,tb;
	int j,jj;
	double xl,h,hmin,ind,hl,absh,fhm,discry,discrz,toly,tolz,mu,
         mu1,fhy,fhz;
	double yl[] = new double[n+1];
  double zl[] = new double[n+1];
  double k0[] = new double[n+1];
  double k1[] = new double[n+1];
  double k2[] = new double[n+1];
  double k3[] = new double[n+1];
  double k4[] = new double[n+1];
  double k5[] = new double[n+1];
	double ee[] = new double[4*n+1];

  hl=mu1=0.0;
  last=true;
	if (fi) {
		d[3]=a;
		for (jj=1; jj<=n; jj++) {
			d[jj+3]=ya[jj];
			d[n+jj+3]=za[jj];
		}
	}
	d[1]=0.0;
	xl=d[3];
	for (jj=1; jj<=n; jj++) {
		yl[jj]=d[jj+3];
		zl[jj]=d[n+jj+3];
	}
	if (fi) d[2]=b-d[3];
	absh=h=Math.abs(d[2]);
	if (b-xl < 0.0) h = -h;
	ind=Math.abs(b-xl);
	hmin=ind*e[1]+e[2];
	for (jj=2; jj<=2*n; jj++) {
		hl=ind*e[2*jj-1]+e[2*jj];
		if (hl < hmin) hmin=hl;
	}
	for (jj=1; jj<=4*n; jj++) ee[jj]=e[jj]/ind;
	first=reject=true;
	test=true;
	if (fi) {
		last=true;
		test=false;
	}
	while (true) {
		if (test) {
			absh=Math.abs(h);
			if (absh < hmin) {
				h = (h > 0.0) ? hmin : -hmin;
				absh=hmin;
			}
			ta=(h >= b-xl);
			tb=(h >= 0.0);
			if ((ta && tb) || (!(ta || tb))) {
				d[2]=h;
				last=true;
				h=b-xl;
				absh=Math.abs(h);
			} else
				last=false;
		}
		test=true;
		if (reject) {
			x[0]=xl;
			for (jj=1; jj<=n; jj++) y[jj]=yl[jj];
			for (j=1; j<=n; j++) k0[j]=method.fxyj(n,j,x,y)*h;
		} else {
			fhy=h/hl;
			for (jj=1; jj<=n; jj++) k0[jj]=k5[jj]*fhy;
		}
		x[0]=xl+0.276393202250021*h;
		for (jj=1; jj<=n; jj++)
			y[jj]=yl[jj]+(zl[jj]*0.276393202250021+
            k0[jj]*0.038196601125011)*h;
		for (j=1; j<=n; j++) k1[j]=method.fxyj(n,j,x,y)*h;
		x[0]=xl+0.723606797749979*h;
		for (jj=1; jj<=n; jj++)
			y[jj]=yl[jj]+(zl[jj]*0.723606797749979+
            k1[jj]*0.261803398874989)*h;
		for (j=1; j<=n; j++) k2[j]=method.fxyj(n,j,x,y)*h;
		x[0]=xl+h*0.5;
		for (jj=1; jj<=n; jj++)
			y[jj]=yl[jj]+(zl[jj]*0.5+k0[jj]*0.046875+k1[jj]*
            0.079824155839840-k2[jj]*0.001699155839840)*h;
		for (j=1; j<=n; j++) k4[j]=method.fxyj(n,j,x,y)*h;
		x[0] = (last ? b : xl+h);
		for (jj=1; jj<=n; jj++)
			y[jj]=yl[jj]+(zl[jj]+k0[jj]*0.309016994374947+
            k2[jj]*0.190983005625053)*h;
		for (j=1; j<=n; j++) k3[j]=method.fxyj(n,j,x,y)*h;
		for (jj=1; jj<=n; jj++)
			y[jj]=yl[jj]+(zl[jj]+k0[jj]*0.083333333333333+k1[jj]*
            0.301502832395825+k2[jj]*0.115163834270842)*h;
		for (j=1; j<=n; j++) k5[j]=method.fxyj(n,j,x,y)*h;
		reject=false;
		fhm=0.0;
		for (jj=1; jj<=n; jj++) {
			discry=Math.abs((-k0[jj]*0.5+k1[jj]*1.809016994374947+
                       k2[jj]*0.690983005625053-k4[jj]*2.0)*h);
			discrz=Math.abs((k0[jj]-k3[jj])*2.0-(k1[jj]+k2[jj])*10.0+
                       k4[jj]*16.0+k5[jj]*4.0);
			toly=absh*(Math.abs(zl[jj])*ee[2*jj-1]+ee[2*jj]);
			tolz=Math.abs(k0[jj])*ee[2*(jj+n)-1]+absh*ee[2*(jj+n)];
			reject=((discry > toly) || (discrz > tolz) || reject);
			fhy=discry/toly;
			fhz=discrz/tolz;
			if (fhz > fhy) fhy=fhz;
			if (fhy > fhm) fhm=fhy;
		}
		mu=1.0/(1.0+fhm)+0.45;
		if (reject) {
			if (absh <= hmin) {
				d[1] += 1.0;
				for (jj=1; jj<=n; jj++) {
					y[jj]=yl[jj];
					z[jj]=zl[jj];
				}
				first=true;
				if (b == x[0]) break;
				xl = x[0];
				for (jj=1; jj<=n; jj++) {
					yl[jj]=y[jj];
					zl[jj]=z[jj];
				}
			} else
				h *= mu;
		} else {
			if (first) {
				first=false;
				hl=h;
				h *= mu;
			} else {
				fhy=mu*h/hl+mu-mu1;
				hl=h;
				h *= fhy;
			}
			mu1=mu;
			for (jj=1; jj<=n; jj++)
				z[jj]=zl[jj]+(k0[jj]+k3[jj])*0.083333333333333+
              (k1[jj]+k2[jj])*0.416666666666667;
			if (b == x[0]) break;
			xl = x[0];
			for (jj=1; jj<=n; jj++) {
				yl[jj]=y[jj];
				zl[jj]=z[jj];
			}
		}
	}
	if (!last) d[2]=h;
	d[3] = x[0];
	for (jj=1; jj<=n; jj++) {
		d[jj+3]=y[jj];
		d[n+jj+3]=z[jj];
	}
}


public static void arkmat(double t[], double te, int m, int n,
                          double u[][], AP_arkmat_methods method,
                          int type, int order[], double spr[])
{
  boolean last,ta,tb;
	int sig,l,i;
	double tau,mlt;
	double lbd1[]={1.0/9.0, 1.0/8.0, 1.0/7.0, 1.0/6.0,
                 1.0/5.0, 1.0/4.0, 1.0/3.0, 1.0/2.0, 4.3};
	double lbd2[]={0.1418519249e-2, 0.3404154076e-2,
                 0.0063118569, 0.01082794375, 0.01842733851,
                 0.03278507942, 0.0653627415, 0.1691078577, 156.0};
	double lbd3[]={0.3534355908e-2, 0.8532600867e-2,
                 0.015956206, 0.02772229155, 0.04812587964,
                 0.08848689452, 0.1863578961, 0.5, 64.0};
	double lbd4[]={1.0/8.0, 1.0/20.0, 5.0/32.0, 2.0/17.0,
                 17.0/80.0, 5.0/22.0, 11.0/32.0, 1.0/2.0, 8.0};
  double lambda[] = new double[10];
	double uh[][] = new double[n+1][m+1];
  double du[][] = new double[n+1][m+1];

	/* initialize */
	if (type != 2 && type != 3) type=1;
	if (type != 2)
		order[0] = 2;
	else
		if (order[0] != 2) order[0] = 1;
	switch ((type == 1) ? 1 : type+order[0]-1) {
		case 1:  for (i=0; i<=8; i++) lambda[i+1]=lbd1[i]; break;
		case 2:  for (i=0; i<=8; i++) lambda[i+1]=lbd2[i]; break;
		case 3:  for (i=0; i<=8; i++) lambda[i+1]=lbd3[i]; break;
		case 4:  for (i=0; i<=8; i++) lambda[i+1]=lbd4[i]; break;
	}
	sig = ((te == t[0]) ? 0 : ((te > t[0]) ? 1 : -1));
	last=false;
	do {
		tau=((spr[0] == 0.0) ? Math.abs(te-t[0]) :
					Math.abs(lambda[9]/spr[0]))*sig;
		ta = t[0]+tau >= te;
		tb = tau >= 0.0;
		if ((ta && tb) || (!(ta || tb))) {
			tau=te-t[0];
			last=true;
		}
		/* difference scheme */
		method.der(m,n,t[0],u,du);
		for (i=1; i<=8; i++) {
			mlt=lambda[i]*tau;
			Basic.dupmat(1,n,1,m,uh,u);
			for (l=1; l<=m; l++) Basic.elmcol(1,n,l,l,uh,du,mlt);
			method.der(m,n,t[0]+mlt,uh,du);
		}
		for (l=1; l<=m; l++) Basic.elmcol(1,n,l,l,u,du,tau);
		t[0] = (last ? te : t[0]+tau);
		method.out(t,te,m,n,u,type,order,spr);
	} while (!last);
}


public static void femlagsym(double x[], double y[], int n,
                             AP_femlagsym_methods method,
                             int order, double e[])
{
	int l,l1;
	double xl1,xl,h,a12,b1,b2,tau1,tau2,ch,tl,g,yl,pp,p1,p2,
         p3,p4,r1,r2,r3,r4,f1,f2,f3,f4,e1,e2,e3,e4,e5,e6,
         h2,x2,h6,h15,b3,tau3,c12,c32,a13,a22,a23,x3,h12,
         h24,det,c13,c42,c43,a14,a24,a33,a34,b4,tau4,aux;
	double t[] = new double[n];
  double sub[] = new double[n];
  double chi[] = new double[n];
  double gi[] = new double[n];

  p2=p3=p4=r2=r3=r4=f2=f3=f4=ch=g=tl=yl=0.0;
	l=1;
	xl=x[0];
	e1=e[1];
	e2=e[2];
	e3=e[3];
	e4=e[4];
	e5=e[5];
	e6=e[6];
	while (l <= n) {
		l1=l-1;
		xl1=xl;
		xl=x[l];
		h=xl-xl1;
		if (order == 2) {
			/* element mat vec evaluation 1 */
			if (l == 1) {
				p2=method.p(xl1);
				r2=method.r(xl1);
				f2=method.f(xl1);
			}
			p1=p2;
			p2=method.p(xl);
			r1=r2;
			r2=method.r(xl);
			f1=f2;
			f2=method.f(xl);
			h2=h/2.0;
			b1=h2*f1;
			b2=h2*f2;
			tau1=h2*r1;
			tau2=h2*r2;
			a12 = -0.5*(p1+p2)/h;
		} else if (order == 4) {
			/* element mat vec evaluation 2 */
			if (l == 1) {
				p3=method.p(xl1);
				r3=method.r(xl1);
				f3=method.f(xl1);
			}
			x2=(xl1+xl)/2.0;
			h6=h/6.0;
			h15=h/1.5;
			p1=p3;
			p2=method.p(x2);
			p3=method.p(xl);
			r1=r3;
			r2=method.r(x2);
			r3=method.r(xl);
			f1=f3;
			f2=method.f(x2);
			f3=method.f(xl);
			b1=h6*f1;
			b2=h15*f2;
			b3=h6*f3;
			tau1=h6*r1;
			tau2=h15*r2;
			tau3=h6*r3;
			a12 = -(2.0*p1+p3/1.5)/h;
			a13=(0.5*(p1+p3)-p2/1.5)/h;
			a22=(p1+p3)/h/0.375+tau2;
			a23 = -(p1/3.0+p3)*2.0/h;
			c12 = -a12/a22;
			c32 = -a23/a22;
			a12=a13+c32*a12;
			b1 += c12*b2;
			b2=b3+c32*b2;
			tau1 += c12*tau2;
			tau2=tau3+c32*tau2;
		} else {
			/* element mat vec evaluation 3 */
			if (l == 1) {
				p4=method.p(xl1);
				r4=method.r(xl1);
				f4=method.f(xl1);
			}
			x2=xl1+0.27639320225*h;
			x3=xl-x2+xl1;
			h12=h/12.0;
			h24=h/2.4;
			p1=p4;
			p2=method.p(x2);
			p3=method.p(x3);
			p4=method.p(xl);
			r1=r4;
			r2=method.r(x2);
			r3=method.r(x3);
			r4=method.r(xl);
			f1=f4;
			f2=method.f(x2);
			f3=method.f(x3);
			f4=method.f(xl);
			b1=h12*f1;
			b2=h24*f2;
			b3=h24*f3;
			b4=h12*f4;
			tau1=h12*r1;
			tau2=h24*r2;
			tau3=h24*r3;
			tau4=h12*r4;
			a12 = -(4.04508497187450*p1+0.57581917135425*p3+
						0.25751416197911*p4)/h;
			a13=(1.5450849718747*p1-1.5075141619791*p2+
           0.6741808286458*p4)/h;
			a14=((p2+p3)/2.4-(p1+p4)/2.0)/h;
			a22=(5.454237476562*p1+p3/0.48+0.79576252343762*p4)/h+tau2;
			a23 = -(p1+p4)/(h*0.48);
			a24=(0.67418082864575*p1-1.50751416197910*p3+
					1.54508497187470*p4)/h;
			a33=(0.7957625234376*p1+p2/0.48+5.454237476562*p4)/h+tau3;
			a34 = -(0.25751416197911*p1+0.57581917135418*p2+
              4.0450849718747*p4)/h;
			det=a22*a33-a23*a23;
			c12=(a13*a23-a12*a33)/det;
			c13=(a12*a23-a13*a22)/det;
			c42=(a23*a34-a24*a33)/det;
			c43=(a24*a23-a34*a22)/det;
			tau1 += c12*tau2+c13*tau3;
			tau2=tau4+c42*tau2+c43*tau3;
			a12=a14+c42*a12+c43*a13;
			b1 += c12*b2+c13*b3;
			b2=b4+c42*b2+c43*b3;
		}
		if (l == 1 || l == n) {
			/* boundary conditions */
			if (l == 1 && e2 == 0.0) {
				tau1=1.0;
				b1=e3/e1;
				b2 -= a12*b1;
				tau2 -= a12;
				a12=0.0;
			} else if (l == 1 && e2 != 0.0) {
				aux=p1/e2;
				tau1 -= aux*e1;
				b1 -= e3*aux;
			} else if (l == n && e5 == 0.0) {
				tau2=1.0;
				b2=e6/e4;
				b1 -= a12*b2;
				tau1 -= a12;
				a12=0.0;
			} else if (l == n && e5 != 0.0) {
				aux=p2/e5;
				tau2 += aux*e4;
				b2 += aux*e6;
			}
		}
		/* forward babushka */
		if (l == 1) {
			chi[0]=ch=tl=tau1;
			t[0]=tl;
			gi[0]=g=yl=b1;
			y[0]=yl;
			sub[0]=a12;
			pp=a12/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			tl=tau2;
			yl=b2;
		} else {
			chi[l1] = ch += tau1;
			gi[l1] = g += b1;
			sub[l1]=a12;
			pp=a12/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			t[l1]=tl+tau1;
			tl=tau2;
			y[l1]=yl+b1;
			yl=b2;
		}
		l++;
	}
	/* backward babushka */
	pp=yl;
	y[n]=g/ch;
	g=pp;
	ch=tl;
	l=n-1;
	while (l >= 0) {
		pp=sub[l];
		pp /= (ch-pp);
		tl=t[l];
		ch=tl-ch*pp;
		yl=y[l];
		g=yl-g*pp;
		y[l]=(gi[l]+g-yl)/(chi[l]+ch-tl);
		l--;
	}
}


public static void femlag(double x[], double y[], int n,
                          AP_femlag_methods method,
                          int order, double e[])
{
	int l,l1;
	double xl1,xl,h,a12,b1,b2,tau1,tau2,ch,tl,g,yl,pp,e1,e2,e3,e4,e5,
         e6,f2,r2,r1,f1,h2,r3,f3,x2,h6,h15,b3,tau3,c12,a13,a22,a23,
         r4,f4,x3,h12,h24,det,c13,c42,c43,a14,a24,a33,a34,b4,tau4;

	double t[] = new double[n];
  double sub[] = new double[n];
  double chi[] = new double[n];
  double gi[] = new double[n];

  r2=r3=r4=f2=f3=f4=ch=g=tl=yl=0.0;
	l=1;
	xl=x[0];
	e1=e[1];
	e2=e[2];
	e3=e[3];
	e4=e[4];
	e5=e[5];
	e6=e[6];
	while (l <= n) {
		l1=l-1;
		xl1=xl;
		xl=x[l];
		h=xl-xl1;
		if (order == 2) {
			/* element mat vec evaluation 1 */
			if (l == 1) {
				f2=method.f(xl1);
				r2=method.r(xl1);
			}
			a12 = -1.0/h;
			h2=h/2.0;
			r1=r2;
			r2=method.r(xl);
			f1=f2;
			f2=method.f(xl);
			b1=h2*f1;
			b2=h2*f2;
			tau1=h2*r1;
			tau2=h2*r2;
		} else if (order == 4) {
			/* element mat vec evaluation 2 */
			if (l == 1) {
				r3=method.r(xl1);
				f3=method.f(xl1);
			}
			x2=(xl1+xl)/2.0;
			h6=h/6.0;
			h15=h/1.5;
			r1=r3;
			r2=method.r(x2);
			r3=method.r(xl);
			f1=f3;
			f2=method.f(x2);
			f3=method.f(xl);
			b1=h6*f1;
			b2=h15*f2;
			b3=h6*f3;
			tau1=h6*r1;
			tau2=h15*r2;
			tau3=h6*r3;
			a12 = a23 = -8.0/h/3.0;
			a13 = -a12/8.0;
			a22 = -2.0*a12+tau2;
			c12 = -a12/a22;
			a12=a13+c12*a12;
			b2 *= c12;
			b1 += b2;
			b2 += b3;
			tau2 *= c12;
			tau1 += tau2;
			tau2=tau3+tau2;
		} else {
			/* element mat vec evaluation 3 */
			if (l == 1) {
				r4=method.r(xl1);
				f4=method.f(xl1);
			}
			x2=xl1+0.27639320225*h;
			x3=xl-x2+xl1;
			r1=r4;
			r2=method.r(x2);
			r3=method.r(x3);
			r4=method.r(xl);
			f1=f4;
			f2=method.f(x2);
			f3=method.f(x3);
			f4=method.f(xl);
			h12=h/12.0;
			h24=h/2.4;
			b1=h12*f1;
			b2=h24*f2;
			b3=h24*f3;
			b4=h12*f4;
			tau1=h12*r1;
			tau2=h24*r2;
			tau3=h24*r3;
			tau4=h12*r4;
			a12 = a34 = -4.8784183052078/h;
			a13=a24=0.7117516385412/h;
			a14 = -0.16666666666667/h;
			a23=25.0*a14;
			a22 = -2.0*a23+tau2;
			a33 = -2.0*a23+tau3;
			det=a22*a33-a23*a23;
			c12=(a13*a23-a12*a33)/det;
			c13=(a12*a23-a13*a22)/det;
			c42=(a23*a34-a24*a33)/det;
			c43=(a24*a23-a34*a22)/det;
			tau1 += c12*tau2+c13*tau3;
			tau2=tau4+c42*tau2+c43*tau3;
			a12=a14+c42*a12+c43*a13;
			b1 += c12*b2+c13*b3;
			b2=b4+c42*b2+c43*b3;
		}
		if (l == 1 || l == n) {
			/* boundary conditions */
			if (l == 1 && e2 == 0.0) {
				tau1=1.0;
				b1=e3/e1;
				b2 -= a12*b1;
				tau2 -= a12;
				a12=0.0;
			} else if (l == 1 && e2 != 0.0) {
				tau1 -= e1/e2;
				b1 -= e3/e2;
			} else if (l == n && e5 == 0.0) {
				tau2=1.0;
				b2=e6/e4;
				b1 -= a12*b2;
				tau1 -= a12;
				a12=0.0;
			} else if (l == n && e5 != 0.0) {
				tau2 += e4/e5;
				b2 += e6/e5;
			}
		}
		/* forward babushka */
		if (l == 1) {
			chi[0]=ch=tl=tau1;
			t[0]=tl;
			gi[0]=g=yl=b1;
			y[0]=yl;
			sub[0]=a12;
			pp=a12/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			tl=tau2;
			yl=b2;
		} else {
			chi[l1] = ch += tau1;
			gi[l1] = g += b1;
			sub[l1]=a12;
			pp=a12/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			t[l1]=tl+tau1;
			tl=tau2;
			y[l1]=yl+b1;
			yl=b2;
		}
		l++;
	}
	/* backward babushka */
	pp=yl;
	y[n]=g/ch;
	g=pp;
	ch=tl;
	l=n-1;
	while (l >= 0) {
		pp=sub[l];
		pp /= (ch-pp);
		tl=t[l];
		ch=tl-ch*pp;
		yl=y[l];
		g=yl-g*pp;
		y[l]=(gi[l]+g-yl)/(chi[l]+ch-tl);
		l--;
	}
}


public static void femlagspher(double x[], double y[],
                               int n, int nc,
                               AP_femlagspher_methods method,
                               int order, double e[])
{
	int l,l1;
	double xl1,xl,h,a12,b1,b2,tau1,tau2,ch,tl,g,yl,pp,tau3,b3,
         a13,a22,a23,c32,c12,e1,e2,e3,e4,e5,e6,xm,vl,vr,wl,
         wr,pr,rm,fm,xl2,xlxr,xr2,xlm,xrm,vlm,vrm,wlm,wrm,
         flm,frm,rlm,rrm,pl1,pl2,pl3,pr1,pr2,pr3,ql1,ql2,ql3,
         rlmpl1,rlmpl2,rrmpr1,rrmpr2,vlmql1,vlmql2,vrmqr1,
         vrmqr2,qr1,qr2,qr3,a,a2,a3,a4,b,b4,p4h,p2,p3,p4,aux1,
         aux2,a5,a6,a7,a8,b5,b6,b7,b8,ab4,a2b3,a3b2,a4b,p5,p8,
         p8h,aux,plm,prm;
	double t[] = new double[n];
  double sub[] = new double[n];
  double chi[] = new double[n];
  double gi[] = new double[n];

  pl1=pl2=pl3=pr1=pr2=pr3=ql1=ql2=ql3=qr1=qr2=qr3=ch=g=tl=yl=0.0;
	l=1;
	xl=x[0];
	e1=e[1];
	e2=e[2];
	e3=e[3];
	e4=e[4];
	e5=e[5];
	e6=e[6];
	while (l <= n) {
		l1=l-1;
		xl1=xl;
		xl=x[l];
		h=xl-xl1;
		if (order == 2) {
			/* element mat vec evaluation 1 */
			if (nc == 0)
				vl=vr=0.5;
			else if (nc == 1) {
				vl=(xl1*2.0+xl)/6.0;
				vr=(xl1+xl*2.0)/6.0;
			} else {
				xl2=xl1*xl1/12.0;
				xlxr=xl1*xl/6.0;
				xr2=xl*xl/12.0;
				vl=3.0*xl2+xlxr+xr2;
				vr=3.0*xr2+xlxr+xl2;
			}
			wl=h*vl;
			wr=h*vr;
			pr=vr/(vl+vr);
			xm=xl1+h*pr;
			fm=method.f(xm);
			rm=method.r(xm);
			tau1=wl*rm;
			tau2=wr*rm;
			b1=wl*fm;
			b2=wr*fm;
			a12 = -(vl+vr)/h+h*(1.0-pr)*pr*rm;
		} else {
			/* element mat vec evaluation 2 */
			if (nc == 0) {
				xlm=xl1+h*0.2113248654052;
				xrm=xl1+xl-xlm;
				vlm=vrm=0.5;
				pl1=pr3=0.45534180126148;
				pl3=pr1 = -0.12200846792815;
				pl2=pr2=1.0-pl1-pl3;
				ql1 = -2.15470053837925;
				ql3 = -0.15470053837925;
				ql2 = -ql1-ql3;
				qr1 = -ql3;
				qr3 = -ql1;
				qr2 = -ql2;
			} else if (nc == 1) {
				a=xl1;
				a2=a*a;
				a3=a*a2;
				a4=a*a3;
				b=xl;
				b2=b*b;
				b3=b*b2;
				b4=b*b3;
				p2=10.0*(a2+4.0*a*b+b2);
				p3=6.0*(a3+4.0*(a2*b+a*b2)+b3);
				p4=Math.sqrt(6.0*(a4+10.0*(a*b3+a3*b)+28.0*a2*b2+b4));
				p4h=p4*h;
				xlm=(p3-p4h)/p2;
				xrm=(p3+p4h)/p2;
				aux1=(a+b)/4.0;
				aux2=h*(a2+7.0*a*b+b2)/6.0/p4;
				vlm=aux1-aux2;
				vrm=aux1+aux2;
			} else {
				a=xl1;
				a2=a*a;
				a3=a*a2;
				a4=a*a3;
				a5=a*a4;
				a6=a*a5;
				a7=a*a6;
				a8=a*a7;
				b=xl;
				b2=b*b;
				b3=b*b2;
				b4=b*b3;
				b5=b*b4;
				b6=b*b5;
				b7=b*b6;
				b8=b*b7;
				ab4=a*b4;
				a2b3=a2*b3;
				a3b2=a3*b2;
				a4b=a4*b;
				p4=15.0*(a4+4.0*(a3*b+a*b3)+10.0*a2*b2+b4);
				p5=10.0*(a5+4.0*(a4b+ab4)+10.0*(a3b2+a2b3)+b5);
				p8=Math.sqrt(10.0*(a8+10.0*(a7*b+a*b7)+55.0*(a2*b6+a6*b2)+
                     164.0*(a5*b3+a3*b5)+290.0*a4*b4+b8));
				aux1=(a2+a*b+b2)/6.0;
				p8h=p8*h;
				aux2=(h*(a5+7.0*(a4b+ab4)+28.0*(a3b2+a2b3)+b5))/4.8/p8;
				xlm=(p5-p8h)/p4;
				xrm=(p5+p8h)/p4;
				vlm=aux1-aux2;
				vrm=aux1+aux2;
			}
			if (nc > 0) {
				plm=(xlm-xl1)/h;
				prm=(xrm-xl1)/h;
				aux=2.0*plm-1.0;
				pl1=aux*(plm-1.0);
				pl3=aux*plm;
				pl2=1.0-pl1-pl3;
				aux=2.0*prm-1.0;
				pr1=aux*(prm-1.0);
				pr3=aux*prm;
				pr2=1.0-pr1-pr3;
				aux=4.0*plm;
				ql1=aux-3.0;
				ql3=aux-1.0;
				ql2 = -ql1-ql3;
				aux=4.0*prm;
				qr1=aux-3.0;
				qr3=aux-1.0;
				qr2 = -qr1-qr3;
			}
			wlm=h*vlm;
			wrm=h*vrm;
			vlm /= h;
			vrm /= h;
			flm=method.f(xlm)*wlm;
			frm=wrm*method.f(xrm);
			rlm=method.r(xlm)*wlm;
			rrm=wrm*method.r(xrm);
			tau1=pl1*rlm+pr1*rrm;
			tau2=pl2*rlm+pr2*rrm;
			tau3=pl3*rlm+pr3*rrm;
			b1=pl1*flm+pr1*frm;
			b2=pl2*flm+pr2*frm;
			b3=pl3*flm+pr3*frm;
			vlmql1=ql1*vlm;
			vrmqr1=qr1*vrm;
			vlmql2=ql2*vlm;
			vrmqr2=qr2*vrm;
			rlmpl1=rlm*pl1;
			rrmpr1=rrm*pr1;
			rlmpl2=rlm*pl2;
			rrmpr2=rrm*pr2;
			a12=vlmql1*ql2+vrmqr1*qr2+rlmpl1*pl2+rrmpr1*pr2;
			a13=vlmql1*ql3+vrmqr1*qr3+rlmpl1*pl3+rrmpr1*pr3;
			a22=vlmql2*ql2+vrmqr2*qr2+rlmpl2*pl2+rrmpr2*pr2;
			a23=vlmql2*ql3+vrmqr2*qr3+rlmpl2*pl3+rrmpr2*pr3;
			c12 = -a12/a22;
			c32 = -a23/a22;
			a12=a13+c32*a12;
			b1 += c12*b2;
			b2=b3+c32*b2;
			tau1 += c12*tau2;
			tau2=tau3+c32*tau2;
		}
		if (l == 1 || l == n) {
			/* boundary conditions */
			if (l == 1 && e2 == 0.0) {
				tau1=1.0;
				b1=e3/e1;
				b2 -= a12*b1;
				tau2 -= a12;
				a12=0.0;
			} else if (l == 1 && e2 != 0.0) {
				aux=((nc == 0) ? 1.0 : Math.pow(x[0],nc))/e2;
				b1 -= e3*aux;
				tau1 -= e1*aux;
			} else if (l == n && e5 == 0.0) {
				tau2=1.0;
				b2=e6/e4;
				b1 -= a12*b2;
				tau1 -= a12;
				a12=0.0;
			} else if (l == n && e5 != 0.0) {
				aux=((nc == 0) ? 1.0 : Math.pow(x[n],nc))/e5;
				tau2 += aux*e4;
				b2 += aux*e6;
			}
		}
		/* forward babushka */
		if (l == 1) {
			chi[0]=ch=tl=tau1;
			t[0]=tl;
			gi[0]=g=yl=b1;
			y[0]=yl;
			sub[0]=a12;
			pp=a12/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			tl=tau2;
			yl=b2;
		} else {
			chi[l1] = ch += tau1;
			gi[l1] = g += b1;
			sub[l1]=a12;
			pp=a12/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			t[l1]=tl+tau1;
			tl=tau2;
			y[l1]=yl+b1;
			yl=b2;
		}
		l++;
	}
	/* backward babushka */
	pp=yl;
	y[n]=g/ch;
	g=pp;
	ch=tl;
	l=n-1;
	while (l >= 0) {
		pp=sub[l];
		pp /= (ch-pp);
		tl=t[l];
		ch=tl-ch*pp;
		yl=y[l];
		g=yl-g*pp;
		y[l]=(gi[l]+g-yl)/(chi[l]+ch-tl);
		l--;
	}
}


public static void femlagskew(double x[], double y[], int n,
                              AP_femlagskew_methods method,
                              int order, double e[])
{
	int l,l1;
	double xl1,xl,h,a12,a21,b1,b2,tau1,tau2,ch,tl,g,yl,pp,e1,
         e2,e3,e4,e5,e6,q2,r2,f2,q1,r1,f1,h2,s12,q3,r3,f3,
         s13,s22,x2,h6,h15,c12,c32,a13,a31,a22,a23,a32,b3,
         tau3,q4,r4,f4,s14,s23,x3,h12,h24,det,c13,c42,c43,
         a14,a24,a33,a34,a41,a42,a43,b4,tau4;
	double t[] = new double[n];
  double ssuper[] = new double[n];
  double sub[] = new double[n];
  double chi[] = new double[n];
  double gi[] = new double[n];

  q2=q3=q4=r2=r3=r4=f2=f3=f4=ch=g=tl=yl=0.0;
	l=1;
	xl=x[0];
	e1=e[1];
	e2=e[2];
	e3=e[3];
	e4=e[4];
	e5=e[5];
	e6=e[6];
	while (l <= n) {
		xl1=xl;
		l1=l-1;
		xl=x[l];
		h=xl-xl1;
		if (order == 2) {
			/* element mat vec evaluation 1 */
			if (l == 1) {
				q2=method.q(xl1);
				r2=method.r(xl1);
				f2=method.f(xl1);
			}
			h2=h/2.0;
			s12 = -1.0/h;
			q1=q2;
			q2=method.q(xl);
			r1=r2;
			r2=method.r(xl);
			f1=f2;
			f2=method.f(xl);
			b1=h2*f1;
			b2=h2*f2;
			tau1=h2*r1;
			tau2=h2*r2;
			a12=s12+q1/2.0;
			a21=s12-q2/2.0;
		} else if (order == 4) {
			/* element mat vec evaluation 2 */
			if (l == 1) {
				q3=method.q(xl1);
				r3=method.r(xl1);
				f3=method.f(xl1);
			}
			x2=(xl1+xl)/2.0;
			h6=h/6.0;
			h15=h/1.5;
			q1=q3;
			q2=method.q(x2);
			q3=method.q(xl);
			r1=r3;
			r2=method.r(x2);
			r3=method.r(xl);
			f1=f3;
			f2=method.f(x2);
			f3=method.f(xl);
			b1=h6*f1;
			b2=h15*f2;
			b3=h6*f3;
			tau1=h6*r1;
			tau2=h15*r2;
			tau3=h6*r3;
			s12 = -1.0/h/0.375;
			s13 = -s12/8.0;
			s22 = -2.0*s12;
			a12=s12+q1/1.5;
			a13=s13-q1/6.0;
			a21=s12-q2/1.5;
			a23=s12+q2/1.5;
			a22=s22+tau2;
			a31=s13+q3/6.0;
			a32=s12-q3/1.5;
			c12 = -a12/a22;
			c32 = -a32/a22;
			a12=a13+c12*a23;
			a21=a31+c32*a21;
			b1 += c12*b2;
			b2=b3+c32*b2;
			tau1 += c12*tau2;
			tau2=tau3+c32*tau2;
		} else {
			/* element mat vec evaluation 3 */
			if (l == 1) {
				q4=method.q(xl1);
				r4=method.r(xl1);
				f4=method.f(xl1);
			}
			x2=xl1+0.27639320225*h;
			x3=xl-x2+xl1;
			h12=h/12.0;
			h24=h/2.4;
			q1=q4;
			q2=method.q(x2);
			q3=method.q(x3);
			q4=method.q(xl);
			r1=r4;
			r2=method.r(x2);
			r3=method.r(x3);
			r4=method.r(xl);
			f1=f4;
			f2=method.f(x2);
			f3=method.f(x3);
			f4=method.f(xl);
			s12 = -4.8784183052080/h;
			s13=0.7117516385414/h;
			s14 = -0.16666666666667/h;
			s23=25.0*s14;
			s22 = -2.0*s23;
			b1=h12*f1;
			b2=h24*f2;
			b3=h24*f3;
			b4=h12*f4;
			tau1=h12*r1;
			tau2=h24*r2;
			tau3=h24*r3;
			tau4=h12*r4;
			a12=s12+0.67418082864578*q1;
			a13=s13-0.25751416197912*q1;
			a14=s14+q1/12.0;
			a21=s12-0.67418082864578*q2;
			a22=s22+tau2;
			a23=s23+0.93169499062490*q2;
			a24=s13-0.25751416197912*q2;
			a31=s13+0.25751416197912*q3;
			a32=s23-0.93169499062490*q3;
			a33=s22+tau3;
			a34=s12+0.67418082864578*q3;
			a41=s14-q4/12.0;
			a42=s13+0.25751416197912*q4;
			a43=s12-0.67418082864578*q4;
			det=a22*a33-a23*a32;
			c12=(a13*a32-a12*a33)/det;
			c13=(a12*a23-a13*a22)/det;
			c42=(a32*a43-a42*a33)/det;
			c43=(a42*a23-a43*a22)/det;
			tau1 += c12*tau2+c13*tau3;
			tau2=tau4+c42*tau2+c43*tau3;
			a12=a14+c12*a24+c13*a34;
			a21=a41+c42*a21+c43*a31;
			b1 += c12*b2+c13*b3;
			b2=b4+c42*b2+c43*b3;
		}
		if (l == 1 || l == n) {
			/* boundary conditions */
			if (l == 1 && e2 == 0.0) {
				tau1=1.0;
				b1=e3/e1;
				a12=0.0;
			} else if (l == 1 && e2 != 0.0) {
				tau1 -= e1/e2;
				b1 -= e3/e2;
			} else if (l == n && e5 == 0.0) {
				tau2=1.0;
				a21=0.0;
				b2=e6/e4;
			} else if (l == n && e5 != 0.0) {
				tau2 += e4/e5;
				b2 += e6/e5;
			}
		}
		/* forward babushka */
		if (l == 1) {
			chi[0]=ch=tl=tau1;
			t[0]=tl;
			gi[0]=g=yl=b1;
			y[0]=yl;
			sub[0]=a21;
			ssuper[0]=a12;
			pp=a21/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			tl=tau2;
			yl=b2;
		} else {
			chi[l1] = ch += tau1;
			gi[l1] = g += b1;
			sub[l1]=a21;
			ssuper[l1]=a12;
			pp=a21/(ch-a12);
			ch=tau2-ch*pp;
			g=b2-g*pp;
			t[l1]=tl+tau1;
			tl=tau2;
			y[l1]=yl+b1;
			yl=b2;
		}
		l++;
	}
	/* backward babushka */
	pp=yl;
	y[n]=g/ch;
	g=pp;
	ch=tl;
	l=n-1;
	while (l >= 0) {
		pp=ssuper[l]/(ch-sub[l]);
		tl=t[l];
		ch=tl-ch*pp;
		yl=y[l];
		g=yl-g*pp;
		y[l]=(gi[l]+g-yl)/(chi[l]+ch-tl);
		l--;
	}
}


public static void femhermsym(double x[], double y[], int n,
                              AP_femhermsym_methods method,
                              int order, double e[])
{
	int l,n2,v,w;
	double ya,yb,za,zb,d1,d2,e1,r1,r2,xl1;
  double em[] = new double[4];
	double a[] = new double[8*(n-1)+1];
	double a11[] = new double[1];  double a12[] = new double[1];
	double a13[] = new double[1];  double a14[] = new double[1];
	double a22[] = new double[1];  double a23[] = new double[1];
	double a24[] = new double[1];  double a33[] = new double[1];
	double a34[] = new double[1];  double a44[] = new double[1];
	double b1[] = new double[1];   double b2[] = new double[1];
	double b3[] = new double[1];   double b4[] = new double[1];
	double xl[] = new double[1];
	double p3[] = new double[1];   double p4[] = new double[1];
	double p5[] = new double[1];   double q3[] = new double[1];
	double q4[] = new double[1];   double q5[] = new double[1];
	double r3[] = new double[1];   double r4[] = new double[1];
	double r5[] = new double[1];   double f3[] = new double[1];
	double f4[] = new double[1];   double f5[] = new double[1];
	
	l=1;
	w=v=0;
	n2=n+n-2;
	xl1=x[0];
	xl[0]=x[1];
	ya=e[1];
	za=e[2];
	yb=e[3];
	zb=e[4];
	/* element matvec evaluation */
	femhermsymeval(order,l,method,a11,a12,a13,a14,a22,a23,
                 a24,a33,a34,a44,b1,b2,b3,b4,xl,xl1,
                 p3,p4,p5,q3,q4,q5,r3,r4,r5,f3,f4,f5);
	em[2]=Double.MIN_VALUE;
	r1=b3[0]-a13[0]*ya-a23[0]*za;
	d1=a33[0];
	d2=a44[0];
	r2=b4[0]-a14[0]*ya-a24[0]*za;
	e1=a34[0];
	l++;
	while (l < n) {
		xl1=xl[0];
		xl[0]=x[l];
		/* element matvec evaluation */
		femhermsymeval(order,l,method,a11,a12,a13,a14,a22,a23,
                   a24,a33,a34,a44,b1,b2,b3,b4,xl,xl1,
                   p3,p4,p5,q3,q4,q5,r3,r4,r5,f3,f4,f5);
		a[w+1]=d1+a11[0];
		a[w+4]=e1+a12[0];
		a[w+7]=a13[0];
		a[w+10]=a14[0];
		a[w+5]=d2+a22[0];
		a[w+8]=a23[0];
		a[w+11]=a24[0];
		a[w+14]=0.0;
		y[v+1]=r1+b1[0];
		y[v+2]=r2+b2[0];
		r1=b3[0];
		r2=b4[0];
		v += 2;
		w += 8;
		d1=a33[0];
		d2=a44[0];
		e1=a34[0];
		l++;
	}
	l=n;
	xl1=xl[0];
	xl[0]=x[l];
	/* element matvec evaluation */
	femhermsymeval(order,l,method,a11,a12,a13,a14,a22,a23,
                 a24,a33,a34,a44,b1,b2,b3,b4,xl,xl1,
                 p3,p4,p5,q3,q4,q5,r3,r4,r5,f3,f4,f5);
	y[n2-1]=r1+b1[0]-a13[0]*yb-a14[0]*zb;
	y[n2]=r2+b2[0]-a23[0]*yb-a24[0]*zb;
	a[w+1]=d1+a11[0];
	a[w+4]=e1+a12[0];
	a[w+5]=d2+a22[0];
	Linear_algebra.chldecsolbnd(a,n2,3,em,y);
}


static private void femhermsymeval(int order, int l,
               AP_femhermsym_methods method, double a11[],
               double a12[], double a13[], double a14[],
               double a22[], double a23[], double a24[],
               double a33[], double a34[], double a44[],
               double b1[], double b2[], double b3[],
               double b4[], double xl[], double xl1,
               double pp3[], double pp4[], double pp5[],
               double qq3[], double qq4[], double qq5[],
               double rr3[], double rr4[], double rr5[],
               double ff3[], double ff4[], double ff5[])
{
	/* this function is internally used by FEMHERMSYM */

	if (order == 4) {
		double x2,h,h2,h3,p1,p2,q1,q2,r1,r2,f1,f2,b11,b12,b13,b14,
           b22,b23,b24,b33,b34,b44,s11,s12,s13,s14,s22,s23,s24,
           s33,s34,s44,m11,m12,m13,m14,m22,m23,m24,m33,m34,m44;
		h=xl[0]-xl1;
		h2=h*h;
		h3=h*h2;
		x2=(xl1+xl[0])/2.0;
		if (l == 1) {
			pp3[0]=method.p(xl1);
			qq3[0]=method.q(xl1);
			rr3[0]=method.r(xl1);
			ff3[0]=method.f(xl1);
		}
		/* element bending matrix */
		p1=pp3[0];
		p2=method.p(x2);
		pp3[0]=method.p(xl[0]);
		b11=6.0*(p1+pp3[0]);
		b12=4.0*p1+2.0*pp3[0];
		b13 = -b11;
		b14=b11-b12;
		b22=(4.0*p1+p2+pp3[0])/1.5;
		b23 = -b12;
		b24=b12-b22;
		b33=b11;
		b34 = -b14;
		b44=b14-b24;
		/* element stiffness matrix */
		q1=qq3[0];
		q2=method.q(x2);
		qq3[0]=method.q(xl[0]);
		s11=1.5*q2;
		s12=q2/4.0;
		s13 = -s11;
		s14=s12;
		s24=q2/24.0;
		s22=q1/6.0+s24;
		s23 = -s12;
		s33=s11;
		s34 = -s12;
		s44=s24+qq3[0]/6.0;
		/* element mass matrix */
		r1=rr3[0];
		r2=method.r(x2);
		rr3[0]=method.r(xl[0]);
		m11=(r1+r2)/6.0;
		m12=r2/24.0;
		m13=r2/6.0;
		m14 = -m12;
		m22=r2/96.0;
		m23 = -m14;
		m24 = -m22;
		m33=(r2+rr3[0])/6.0;
		m34=m14;
		m44=m22;
		/* element load vector */
		f1=ff3[0];
		f2=method.f(x2);
		ff3[0]=method.f(xl[0]);
		b1[0]=h*(f1+2.0*f2)/6.0;
		b3[0]=h*(ff3[0]+2.0*f2)/6.0;
		b2[0]=h2*f2/12.0;
		b4[0] = -b2[0];
		a11[0]=b11/h3+s11/h+m11*h;
		a12[0]=b12/h2+s12+m12*h2;
		a13[0]=b13/h3+s13/h+m13*h;
		a14[0]=b14/h2+s14+m14*h2;
		a22[0]=b22/h+s22*h+m22*h3;
		a23[0]=b23/h2+s23+m23*h2;
		a24[0]=b24/h+s24*h+m24*h3;
		a34[0]=b34/h2+s34+m34*h2;
		a33[0]=b33/h3+s33/h+m33*h;
		a44[0]=b44/h+s44*h+m44*h3;
	} else if (order == 6) {
		double h,h2,h3,x2,x3,p1,p2,p3,q1,q2,q3,r1,r2,r3,f1,f2,f3,b11,
           b12,b13,b14,b15,b22,b23,b24,b25,b33,b34,b35,b44,b45,
           b55,s11,s12,s13,s14,s15,s22,s23,s24,s25,s33,s34,s35,
           s44,s45,s55,m11,m12,m13,m14,m15,m22,m23,m24,m25,m33,
           m34,m35,m44,m45,m55,a15,a25,a35,a45,a55,c1,c2,c3,c4,b5;
		if (l == 1) {
			pp4[0]=method.p(xl1);
			qq4[0]=method.q(xl1);
			rr4[0]=method.r(xl1);
			ff4[0]=method.f(xl1);
		}
		h=xl[0]-xl1;
		h2=h*h;
		h3=h*h2;
		x2=0.27639320225*h+xl1;
		x3=xl1+xl[0]-x2;
		/* element bending matrix */
		p1=pp4[0];
		p2=method.p(x2);
		p3=method.p(x3);
		pp4[0]=method.p(xl[0]);
		b11=4.0333333333333e1*p1+1.1124913866738e-1*p2+
				1.4422084194664e1*p3+8.3333333333333e0*pp4[0];
		b12=1.4666666666667e1*p1-3.3191425091659e-1*p2+
				2.7985809175818e0*p3+1.6666666666667e0*pp4[0];
		b13=1.8333333333333e1*(p1+pp4[0])+1.2666666666667e0*(p2+p3);
		b15 = -(b11+b13);
		b14 = -(b12+b13+b15/2.0);
		b22=5.3333333333333e0*p1+9.9027346441674e-1*p2+
				5.4305986891624e-1*p3+3.3333333333333e-1*pp4[0];
		b23=6.6666666666667e0*p1-3.7791278464167e0*p2+
				2.4579451308295e-1*p3+3.6666666666667e0*pp4[0];
		b25 = -(b12+b23);
		b24 = -(b22+b23+b25/2.0);
		b33=8.3333333333333e0*p1+1.4422084194666e1*p2+
				1.1124913866726e-1*p3+4.0333333333333e1*pp4[0];
		b35 = -(b13+b33);
		b34 = -(b23+b33+b35/2.0);
		b45 = -(b14+b34);
		b44 = -(b24+b34+b45/2.0);
		b55 = -(b15+b35);
		/* element stiffness matrix */
		q1=qq4[0];
		q2=method.q(x2);
		q3=method.q(x3);
		qq4[0]=method.q(xl[0]);
		s11=2.8844168389330e0*q2+2.2249827733448e-2*q3;
		s12=2.5671051872498e-1*q2+3.2894812749994e-3*q3;
		s13=2.5333333333333e-1*(q2+q3);
		s14 = -3.7453559925005e-2*q2-2.2546440074988e-2*q3;
		s15 = -(s13+s11);
		s22=8.3333333333333e-2*q1+2.2847006554164e-2*q2+
				4.8632677916445e-4*q3;
		s23=2.2546440075002e-2*q2+3.7453559924873e-2*q3;
		s24 = -3.3333333333333e-3*(q2+q3);
		s25 = -(s12+s23);
		s33=2.2249827733471e-2*q2+2.8844168389330e0*q3;
		s34 = -3.2894812750127e-3*q2-2.5671051872496e-1*q3;
		s35 = -(s13+s33);
		s44=4.8632677916788e-4*q2+2.2847006554161e-2*q3+
				8.3333333333338e-2*qq4[0];
		s45 = -(s14+s34);
		s55 = -(s15+s35);
		/* element mass matrix */
		r1=rr4[0];
		r2=method.r(x2);
		r3=method.r(x3);
		rr4[0]=method.r(xl[0]);
		m11=8.3333333333333e-2*r1+1.0129076086083e-1*r2+
				7.3759058058380e-3*r3;
		m12=1.3296181273333e-2*r2+1.3704853933353e-3*r3;
		m13 = -2.7333333333333e-2*(r2+r3);
		m14=5.0786893258335e-3*r2+3.5879773408333e-3*r3;
		m15=1.3147987115999e-1*r2-3.5479871159991e-2*r3;
		m22=1.7453559925000e-3*r2+2.5464400750059e-4*r3;
		m23 = -3.5879773408336e-3*r2-5.0786893258385e-3*r3;
		m24=6.6666666666667e-4*(r2+r3);
		m25=1.7259029213333e-2*r2-6.5923625466719e-3*r3;
		m33=7.3759058058380e-3*r2+1.0129076086083e-1*r3+
				8.3333333333333e-2*rr4[0];
		m34 = -1.3704853933333e-3*r2-1.3296181273333e-2*r3;
		m35 = -3.5479871159992e-2*r2+1.3147987115999e-1*r3;
		m44=2.5464400750008e-4*r2+1.7453559924997e-3*r3;
		m45=6.5923625466656e-3*r2-1.7259029213330e-2*r3;
		m55=0.17066666666667e0*(r2+r3);
		/* element load vector */
		f1=ff4[0];
		f2=method.f(x2);
		f3=method.f(x3);
		ff4[0]=method.f(xl[0]);
		b1[0]=8.3333333333333e-2*f1+2.0543729868749e-1*f2-
				5.5437298687489e-2*f3;
		b2[0]=2.6967233145832e-2*f2-1.0300566479175e-2*f3;
		b3[0] = -5.5437298687489e-2*f2+2.0543729868749e-1*f3+
				8.3333333333333e-2*ff4[0];
		b4[0]=1.0300566479165e-2*f2-2.6967233145830e-2*f3;
		b5=2.6666666666667e-1*(f2+f3);
		a11[0]=h2*(h2*m11+s11)+b11;
		a12[0]=h2*(h2*m12+s12)+b12;
		a13[0]=h2*(h2*m13+s13)+b13;
		a14[0]=h2*(h2*m14+s14)+b14;
		a15=h2*(h2*m15+s15)+b15;
		a22[0]=h2*(h2*m22+s22)+b22;
		a23[0]=h2*(h2*m23+s23)+b23;
		a24[0]=h2*(h2*m24+s24)+b24;
		a25=h2*(h2*m25+s25)+b25;
		a33[0]=h2*(h2*m33+s33)+b33;
		a34[0]=h2*(h2*m34+s34)+b34;
		a35=h2*(h2*m35+s35)+b35;
		a44[0]=h2*(h2*m44+s44)+b44;
		a45=h2*(h2*m45+s45)+b45;
		a55=h2*(h2*m55+s55)+b55;
		/* static condensation */
		c1=a15/a55;
		c2=a25/a55;
		c3=a35/a55;
		c4=a45/a55;
		b1[0]=(b1[0]-c1*b5)*h;
		b2[0]=(b2[0]-c2*b5)*h2;
		b3[0]=(b3[0]-c3*b5)*h;
		b4[0]=(b4[0]-c4*b5)*h2;
		a11[0]=(a11[0]-c1*a15)/h3;
		a12[0]=(a12[0]-c1*a25)/h2;
		a13[0]=(a13[0]-c1*a35)/h3;
		a14[0]=(a14[0]-c1*a45)/h2;
		a22[0]=(a22[0]-c2*a25)/h;
		a23[0]=(a23[0]-c2*a35)/h2;
		a24[0]=(a24[0]-c2*a45)/h;
		a33[0]=(a33[0]-c3*a35)/h3;
		a34[0]=(a34[0]-c3*a45)/h2;
		a44[0]=(a44[0]-c4*a45)/h;
	} else {
		double x2,x3,x4,h,h2,h3,p1,p2,p3,p4,q1,q2,q3,q4,r1,r2,r3,r4,
				f1,f2,f3,f4,b11,b12,b13,b14,b15,b16,b22,b23,b24,b25,b26,
				b33,b34,b35,b36,b44,b45,b46,b55,b56,b66,s11,s12,s13,s14,
				s15,s16,s22,s23,s24,s25,s26,s33,s34,s35,s36,s44,s45,s46,
				s55,s56,s66,m11,m12,m13,m14,m15,m16,m22,m23,m24,m25,m26,
				m33,m34,m35,m36,m44,m45,m46,m55,m56,m66,c15,c16,c25,c26,
				c35,c36,c45,c46,b5,b6,a15,a16,a25,a26,a35,a36,a45,a46,
				a55,a56,a66,det;
		if (l == 1) {
			pp5[0]=method.p(xl1);
			qq5[0]=method.q(xl1);
			rr5[0]=method.r(xl1);
			ff5[0]=method.f(xl1);
		}
		h=xl[0]-xl1;
		h2=h*h;
		h3=h*h2;
		x2=xl1+h*0.172673164646;
		x3=xl1+h/2.0;
		x4=xl1+xl[0]-x2;
		/* element bending matrix */
		p1=pp5[0];
		p2=method.p(x2);
		p3=method.p(x3);
		p4=method.p(x4);
		pp5[0]=method.p(xl[0]);
		b11=105.8*p1+9.8*pp5[0]+7.3593121303513e-2*p2+
				2.2755555555556e1*p3+7.0565656088553e0*p4;
		b12=27.6*p1+1.4*pp5[0]-3.41554824811e-1*p2+
				2.8444444444444e0*p3+1.0113960946522e0*p4;
		b13 = -32.2*(p1+pp5[0])-7.2063492063505e-1*(p2+p4)+
				2.2755555555556e1*p3;
		b14=4.6*p1+8.4*pp5[0]+1.0328641222944e-1*p2-
				2.8444444444444e0*p3-3.3445562534992e0*p4;
		b15 = -(b11+b13);
		b16 = -(b12+b13+b14+b15/2.0);
		b22=7.2*p1+0.2*pp5[0]+1.5851984028581e0*p2+
				3.5555555555556e-1*p3+1.4496032730059e-1*p4;
		b23 = -8.4*p1-4.6*pp5[0]+3.3445562534992e0*p2+
				2.8444444444444e0*p3-1.0328641222944e-1*p4;
		b24=1.2*(p1+pp5[0])-4.7936507936508e-1*(p2+p4)-
				3.5555555555556e-1*p3;
		b25 = -(b12+b23);
		b26 = -(b22+b23+b24+b25/2.0);
		b33=7.0565656088553e0*p2+2.2755555555556e1*p3+
				7.3593121303513e-2*p4+105.8*pp5[0]+9.8*p1;
		b34 = -1.4*p1-27.6*pp5[0]-1.0113960946522e0*p2-
				2.8444444444444e0*p3+3.4155482481100e-1*p4;
		b35 = -(b13+b33);
		b36 = -(b23+b33+b34+b35/2.0);
		b44=7.2*pp5[0]+p1/5.0+1.4496032730059e-1*p2+
				3.5555555555556e-1*p3+1.5851984028581e0*p4;
		b45 = -(b14+b34);
		b46 = -(b24+b34+b44+b45/2.0);
		b55 = -(b15+b35);
		b56 = -(b16+b36);
		b66 = -(b26+b36+b46+b56/2.0);
		/* element stiffness matrix */
		q1=qq5[0];
		q2=method.q(x2);
		q3=method.q(x3);
		q4=method.q(x4);
		qq5[0]=method.q(xl[0]);
		s11=3.0242424037951e0*q2+3.1539909130065e-2*q4;
		s12=1.2575525581744e-1*q2+4.1767169716742e-3*q4;
		s13 = -3.0884353741496e-1*(q2+q4);
		s14=4.0899041243062e-2*q2+1.2842455355577e-2*q4;
		s15 = -(s13+s11);
		s16=5.9254861177068e-1*q2+6.0512612719116e-2*q4;
		s22=5.2292052865422e-3*q2+5.5310763862796e-4*q4+q1/20.0;
		s23 = -1.2842455355577e-2*q2-4.0899041243062e-2*q4;
		s24=1.7006802721088e-3*(q2+q4);
		s25 = -(s12+s23);
		s26=2.4639593097426e-2*q2+8.0134681270641e-3*q4;
		s33=3.1539909130065e-2*q2+3.0242424037951e0*q4;
		s34 = -4.1767169716742e-3*q2-1.2575525581744e-1*q4;
		s35 = -(s13+s33);
		s36 = -6.0512612719116e-2*q2-5.9254861177068e-1*q4;
		s44=5.5310763862796e-4*q2+5.2292052865422e-3*q4+qq5[0]/20.0;
		s45 = -(s14+s34);
		s46=8.0134681270641e-3*q2+2.4639593097426e-2*q4;
		s55 = -(s15+s35);
		s56 = -(s16+s36);
		s66=1.1609977324263e-1*(q2+q4)+3.5555555555556e-1*q3;
		/* element mass matrix */
		r1=rr5[0];
		r2=method.r(x2);
		r3=method.r(x3);
		r4=method.r(x4);
		rr5[0]=method.r(xl[0]);
		m11=9.7107020727310e-2*r2+1.5810259199180e-3*r4+r1/20.0;
		m12=8.2354889460254e-3*r2+2.1932154960071e-4*r4;
		m13=1.2390670553936e-2*(r2+r4);
		m14 = -1.7188466249968e-3*r2-1.0508326752939e-3*r4;
		m15=5.3089789712119e-2*r2+6.7741558661060e-3*r4;
		m16 = -1.7377712856076e-2*r2+2.2173630018466e-3*r4;
		m22=6.9843846173145e-4*r2+3.0424512029349e-5*r4;
		m23=1.0508326752947e-3*r2+1.7188466249936e-3*r4;
		m24 = -1.4577259475206e-4*(r2+r4);
		m25=4.5024589679127e-3*r2+9.3971790283374e-4*r4;
		m26 = -1.4737756452780e-3*r2+3.0759488725998e-4*r4;
		m33=1.5810259199209e-3*r2+9.7107020727290e-2*r4+rr5[0]/20.0;
		m34 = -2.1932154960131e-4*r2-8.2354889460354e-3*r4;
		m35=6.7741558661123e-3*r2+5.3089789712112e-2*r4;
		m36 = -2.2173630018492e-3*r2+1.7377712856071e-2*r4;
		m44=3.0424512029457e-5*r2+6.9843846173158e-4*r4;
		m45 = -9.3971790283542e-4*r2-4.5024589679131e-3*r4;
		m46=3.0759488726060e-4*r2-1.4737756452778e-3*r4;
		m55=2.9024943310657e-2*(r2+r4)+3.5555555555556e-1*r3;
		m56=9.5006428402050e-3*(r4-r2);
		m66=3.1098153547125e-3*(r2+r4);
		/* element load vector */
		f1=ff5[0];
		f2=method.f(x2);
		f3=method.f(x3);
		f4=method.f(x4);
		ff5[0]=method.f(xl[0]);
		b1[0]=1.6258748099336e-1*f2+2.0745852339969e-2*f4+f1/20.0;
		b2[0]=1.3788780589233e-2*f2+2.8778860774335e-3*f4;
		b3[0]=2.0745852339969e-2*f2+1.6258748099336e-1*f4+ff5[0]/20.0;
		b4[0] = -2.8778860774335e-3*f2-1.3788780589233e-2*f4;
		b5=(f2+f4)/11.25+3.5555555555556e-1*f3;
		b6=2.9095718698132e-2*(f4-f2);
		a11[0]=h2*(h2*m11+s11)+b11;
		a12[0]=h2*(h2*m12+s12)+b12;
		a13[0]=h2*(h2*m13+s13)+b13;
		a14[0]=h2*(h2*m14+s14)+b14;
		a15=h2*(h2*m15+s15)+b15;
		a16=h2*(h2*m16+s16)+b16;
		a22[0]=h2*(h2*m22+s22)+b22;
		a23[0]=h2*(h2*m23+s23)+b23;
		a24[0]=h2*(h2*m24+s24)+b24;
		a25=h2*(h2*m25+s25)+b25;
		a26=h2*(h2*m26+s26)+b26;
		a33[0]=h2*(h2*m33+s33)+b33;
		a34[0]=h2*(h2*m34+s34)+b34;
		a35=h2*(h2*m35+s35)+b35;
		a36=h2*(h2*m36+s36)+b36;
		a44[0]=h2*(h2*m44+s44)+b44;
		a45=h2*(h2*m45+s45)+b45;
		a46=h2*(h2*m46+s46)+b46;
		a55=h2*(h2*m55+s55)+b55;
		a56=h2*(h2*m56+s56)+b56;
		a66=h2*(h2*m66+s66)+b66;
		/* static condensation */
		det = -a55*a66+a56*a56;
		c15=(a15*a66-a16*a56)/det;
		c16=(a16*a55-a15*a56)/det;
		c25=(a25*a66-a26*a56)/det;
		c26=(a26*a55-a25*a56)/det;
		c35=(a35*a66-a36*a56)/det;
		c36=(a36*a55-a35*a56)/det;
		c45=(a45*a66-a46*a56)/det;
		c46=(a46*a55-a45*a56)/det;
		a11[0]=(a11[0]+c15*a15+c16*a16)/h3;
		a12[0]=(a12[0]+c15*a25+c16*a26)/h2;
		a13[0]=(a13[0]+c15*a35+c16*a36)/h3;
		a14[0]=(a14[0]+c15*a45+c16*a46)/h2;
		a22[0]=(a22[0]+c25*a25+c26*a26)/h;
		a23[0]=(a23[0]+c25*a35+c26*a36)/h2;
		a24[0]=(a24[0]+c25*a45+c26*a46)/h;
		a33[0]=(a33[0]+c35*a35+c36*a36)/h3;
		a34[0]=(a34[0]+c35*a45+c36*a46)/h2;
		a44[0]=(a44[0]+c45*a45+c46*a46)/h;
		b1[0]=(b1[0]+c15*b5+c16*b6)*h;
		b2[0]=(b2[0]+c25*b5+c26*b6)*h2;
		b3[0]=(b3[0]+c35*b5+c36*b6)*h;
		b4[0]=(b4[0]+c45*b5+c46*b6)*h2;
	}
}


public static void nonlinfemlagskew(double x[], double y[],
                   int n, AP_nonlinfemlagskew_methods method,
                   int nc, double e[])
{
	int l,l1,it;
	double xl1,xl,h,a12,a21,b1,b2,tau1,tau2,ch,tl,g,yl,pp,
         zl1,zl,e1,e2,e4,e5,eps,rho,xm,vl,vr,wl,wr,pr,qm,
         rm,fm,xl12,xl1xl,xl2,zm,zaccm;
	double t[] = new double[n];
  double ssuper[] = new double[n];
  double sub[] = new double[n];
  double chi[] = new double[n];
  double gi[] = new double[n];
  double z[] = new double[n+1];

  ch=g=tl=yl=0.0;
	Basic.dupvec(0,n,0,z,y);
	e1=e[1];
	e2=e[2];
	e4=e[4];
	e5=e[5];
	it=1;
	do {
		l=1;
		xl=x[0];
		zl=z[0];
		while (l <= n) {
			xl1=xl;
			l1=l-1;
			xl=x[l];
			h=xl-xl1;
			zl1=zl;
			zl=z[l];
			/* element mat vec evaluation 1 */
			if (nc == 0)
				vl=vr=0.5;
			else if (nc == 1) {
				vl=(xl1*2.0+xl)/6.0;
				vr=(xl1+xl*2.0)/6.0;
			} else {
				xl12=xl1*xl1/12.0;
				xl1xl=xl1*xl/6.0;
				xl2=xl*xl/12.0;
				vl=3.0*xl12+xl1xl+xl2;
				vr=3.0*xl2+xl1xl+xl12;
			}
			wl=h*vl;
			wr=h*vr;
			pr=vr/(vl+vr);
			xm=xl1+h*pr;
			zm=pr*zl+(1.0-pr)*zl1;
			zaccm=(zl-zl1)/h;
			qm=method.fz(xm,zm,zaccm);
			rm=method.fy(xm,zm,zaccm);
			fm=method.f(xm,zm,zaccm);
			tau1=wl*rm;
			tau2=wr*rm;
			b1=wl*fm-zaccm*(vl+vr);
			b2=wr*fm+zaccm*(vl+vr);
			a12 = -(vl+vr)/h+vl*qm+(1.0-pr)*pr*rm*(wl+wr);
			a21 = -(vl+vr)/h-vr*qm+(1.0-pr)*pr*rm*(wl+wr);
         if (l == 1 || l == n) {
				/* boundary conditions */
				if (l == 1 && e2 == 0.0) {
					tau1=1.0;
					b1=a12=0.0;
				} else if (l == 1 && e2 != 0.0) {
					tau1 -= e1/e2;
				} else if (l == n && e5 == 0.0) {
					tau2=1.0;
					b2=a21=0.0;
				} else if (l == n && e5 != 0.0) {
					tau2 += e4/e5;
				}
			}
			/* forward babushka */
			if (l == 1) {
				chi[0]=ch=tl=tau1;
				t[0]=tl;
				gi[0]=g=yl=b1;
				y[0]=yl;
				sub[0]=a21;
				ssuper[0]=a12;
				pp=a21/(ch-a12);
				ch=tau2-ch*pp;
				g=b2-g*pp;
				tl=tau2;
				yl=b2;
			} else {
				chi[l1] = ch += tau1;
				gi[l1] = g += b1;
				sub[l1]=a21;
				ssuper[l1]=a12;
				pp=a21/(ch-a12);
				ch=tau2-ch*pp;
				g=b2-g*pp;
				t[l1]=tl+tau1;
				tl=tau2;
				y[l1]=yl+b1;
				yl=b2;
			}
			l++;
		}
		/* backward babushka */
		pp=yl;
		y[n]=g/ch;
		g=pp;
		ch=tl;
		l=n-1;
		while (l >= 0) {
			pp=ssuper[l]/(ch-sub[l]);
			tl=t[l];
			ch=tl-ch*pp;
			yl=y[l];
			g=yl-g*pp;
			y[l]=(gi[l]+g-yl)/(chi[l]+ch-tl);
			l--;
		}
		eps=0.0;
		rho=1.0;
		for (l=0; l<=n; l++) {
			rho += Math.abs(z[l]);
			eps += Math.abs(y[l]);
			z[l] -= y[l];
		}
		rho *= 1.0e-14;
		it++;
	} while (eps > rho);
	Basic.dupvec(0,n,0,y,z);
}


public static void richardson(double u[][], int lj, int uj,
              int ll, int ul, boolean inap,
              AP_richardson_methods method, double a, double b,
              int n[], double discr[], int k[],
              double rateconv[], double domeigval[])
{
	int j,l;
	double x,y,z,y0,c,d,alfa,omega,omega0,eigmax,eigeucl,euclres,
	       maxres,rcmax,rceucl,maxres0,euclres0,auxres0,auxv,auxu,
         auxres,eucluv,maxuv;
	double v[][] = new double[uj+1][ul+1];
	double res[][] = new double[uj+1][ul+1];

	alfa=2.0;
	omega=4.0/(b+a);
	y0=(b+a)/(b-a);
	x=0.5*(b+a);
	y=(b-a)*(b-a)/16.0;
	z=4.0*y0*y0;
	c=a*b;
	c=Math.sqrt(c);
	d=Math.sqrt(a)+Math.sqrt(b);
	d=d*d;
	if (!inap)
		for (j=lj; j<=uj; j++)
			for (l=ll; l<=ul; l++) u[j][l]=1.0;
	k[0]=0;
	for (j=lj; j<=uj; j++)
		for (l=ll; l<=ul; l++) res[j][l]=u[j][l];
	method.residual(lj,uj,ll,ul,res);
	omega0=2.0/(b+a);
	maxres0=euclres0=0.0;
	for (j=lj; j<=uj; j++)
		for (l=ll; l<=ul; l++) {
			auxres0=res[j][l];
			v[j][l]=u[j][l]-omega0*auxres0;
			auxres0=Math.abs(auxres0);
			maxres0 = (maxres0 < auxres0) ? auxres0 : maxres0;
			euclres0 += auxres0*auxres0;
		}
	euclres0=Math.sqrt(euclres0);
	discr[1]=euclres0;
	discr[2]=maxres0;
	method.out(u,lj,uj,ll,ul,n,discr,k,rateconv,domeigval);
	while (k[0] < n[0]) {
		k[0]++;
		/* calculate parameters alfa and omega for each iteration */
		alfa=z/(z-alfa);
		omega=1.0/(x-omega*y);
		/* iteration */
		eucluv=euclres=maxuv=maxres=0.0;
		for (j=lj; j<=uj; j++)
			for (l=ll; l<=ul; l++) res[j][l]=v[j][l];
		method.residual(lj,uj,ll,ul,res);
		for (j=lj; j<=uj; j++)
			for (l=ll; l<=ul; l++) {
				auxv=u[j][l];
				auxu=v[j][l];
				auxres=res[j][l];
				auxv=alfa*auxu-omega*auxres+(1.0-alfa)*auxv;
				v[j][l]=auxv;
				u[j][l]=auxu;
				auxu=Math.abs(auxu-auxv);
				auxres=Math.abs(auxres);
				maxuv = (maxuv < auxu) ? auxu : maxuv;
				maxres = (maxres < auxres) ? auxres : maxres;
				eucluv += auxu*auxu;
				euclres += auxres*auxres;
			}
		eucluv=Math.sqrt(eucluv);
		euclres=Math.sqrt(euclres);
		discr[1]=euclres;
		discr[2]=maxres;
		maxuv=maxres/maxuv;
		eucluv=euclres/eucluv;
		eigmax=maxuv*(c-maxuv)/(0.25*d-maxuv);
		eigeucl=eucluv*(c-eucluv)/(0.25*d-eucluv);
		domeigval[0]=0.5*(eigmax+eigeucl);
		rceucl = -Math.log(euclres/euclres0)/k[0];
		rcmax = -Math.log(maxres/maxres0)/k[0];
		rateconv[0]=0.5*(rceucl+rcmax);
		method.out(u,lj,uj,ll,ul,n,discr,k,rateconv,domeigval);
	}
}


public static void elimination(double u[][], int lj, int uj,
              int ll, int ul, AP_richardson_methods method,
              double a, double b, int n[], double discr[],
              int k[], double rateconv[], double domeigval[])
{
	boolean extrapolate;
	int ext;
	double auxcos,c,d,cc,fc,bb,fb,aa,fa,dd,fd,fdb,fda,w,mb,tol,m,p,q;

  fd=dd=0.0;
	c=1.0;
	if (optpol(c,a,b,domeigval) < 0.0) {
		d=0.5*Math.PI*Math.sqrt(Math.abs(b/domeigval[0]));
		while (true) {
			d += d;
			/* finding zero */
			bb=c;
			fb=optpol(c,a,b,domeigval);
			aa=c=d;
			fa=optpol(c,a,b,domeigval);
			cc=aa;
			fc=fa;
			ext=0;
			extrapolate=true;
			while (extrapolate) {
				if (Math.abs(fc) < Math.abs(fb)) {
					if (cc != aa) {
						dd=aa;
						fd=fa;
					}
					aa=bb;
					fa=fb;
					bb=c=cc;
					fb=fc;
					cc=aa;
					fc=fa;
				}
				tol=c*1.0e-3;
				m=(cc+bb)*0.5;
				mb=m-bb;
				if (Math.abs(mb) > tol) {
					if (ext > 2)
						w=mb;
					else {
						if (mb == 0.0)
							tol=0.0;
						else
							if (mb < 0.0) tol = -tol;
						p=(bb-aa)*fb;
						if (ext <= 1)
							q=fa-fb;
						else {
							fdb=(fd-fb)/(dd-bb);
							fda=(fd-fa)/(dd-aa);
							p *= fda;
							q=fdb*fa-fda*fb;
						}
						if (p < 0.0) {
							p = -p;
							q = -q;
						}
						w=(p<Double.MIN_VALUE || p<=q*tol) ? tol :
								((p<mb*q) ? p/q : mb);
					}
					dd=aa;
					fd=fa;
					aa=bb;
					fa=fb;
					c = bb += w;
					fb=optpol(c,a,b,domeigval);
					if ((fc >= 0.0) ? (fb >= 0.0) : (fb <= 0.0)) {
						cc=aa;
						fc=fa;
						ext=0;
					} else
						ext = (w == mb) ? 0 : ext+1;
				} else
					break;
			}
			d=cc;
			if ((fc >= 0.0) ? (fb <= 0.0) : (fb >= 0.0)) {
				n[0]=(int)Math.floor(c+0.5);
				break;
			}
		}
	} else
		n[0]=1;
	auxcos=Math.cos(0.5*Math.PI/n[0]);
	richardson(u,lj,uj,ll,ul,true,method,
    (2.0*domeigval[0]+b*(auxcos-1.0))/(auxcos+1.0),
    b,n,discr,k,rateconv,domeigval);
}


static private double optpol(double x, double a, double b,
                             double domeigval[])
{
	/* this function is internally used by ELIMINATION */

	double w,y;

	w=(b*Math.cos(0.5*Math.PI/x)+domeigval[0])/(b-domeigval[0]);
	if (w < -1.0) w = -1.0;
	if (Math.abs(w) <= 1.0) {
		y=Math.acos(w);
		return 2.0*Math.sqrt(a/b)+Math.tan(x*y)*(y-b*Math.PI*
		       Math.sin(0.5*Math.PI/x)*0.5/(x*(b-domeigval[0])*
		       Math.sqrt(Math.abs(1.0-w*w))));
	} else {
		y=Math.log(w+Math.sqrt(Math.abs(w*w-1.0)));
		return 2.0*Math.sqrt(a/b)-Special_functions.tanh(x*y)*
           (y+b*Math.PI*Math.sin(0.5*Math.PI/x)*0.5/
           (x*(b-domeigval[0])*Math.sqrt(Math.abs(w*w-1.0))));
	}
}


public static void peide(int n, int m, int nobs, int nbp[],
                   double par[], double res[], int bp[],
                   double jtjinv[][], double in[], double out[],
                   AP_peide_methods method)
{
  boolean emergency,first,clean;
	int i,j,weight,ncol,nrow,away,nfe,nbpold,maxfe,
      fe,it,err;
	double eps1,res1,in3,in4,fac3,fac4,w,temp,vv,ww,w2,mu,res2,fpar,
         fparpres,lambda,lambdamin,p,pw,reltolres,abstolres;
	double save1[]={1.0, 1.0, 9.0, 4.0, 0.0, 2.0/3.0, 1.0,
			1.0/3.0, 36.0, 20.25, 1.0, 6.0/11.0, 1.0, 6.0/11.0,
			1.0/11.0, 84.028, 53.778, 0.25, 0.48, 1.0, 0.7, 0.2,
			0.02, 156.25, 108.51, 0.027778, 120.0/274.0, 1.0,
			225.0/274.0, 85.0/274.0, 15.0/274.0, 1.0/274.0, 0.0,
			187.69, 0.0047361};
  double aux[] = new double[4];
  double em[] = new double[8];
  boolean sec[] = new boolean[1];
  int max[] = new int[1];
  int nis[] = new int[1];

	nbpold=nbp[0];
	int cobs[] = new int[nobs+1];
	double obs[] = new double[nobs+1];
	double tobs[] = new double[nobs+1];
	double save[] = new double[6*n+39];
	double ymax[] = new double[n+1];
	double y[] = new double[6*n*(nbpold+m+1)+1];
	double yp[][] = new double[nbpold+nobs+1][nbpold+m+1];
	double fy[][] = new double[n+1][n+1];
	double fp[][] = new double[n+1][m+nbpold+1];
	double aid[][] = new double[m+nbpold+1][m+nbpold+1];

  nfe=0;
  res1=fac3=fac4=lambda=0.0;
	for (i=0; i<=34; i++) save[i]=save1[i];
	method.data(nobs,tobs,obs,cobs);
	weight=1;
	first=sec[0]=false;
	clean=(nbp[0] > 0);
	aux[2]=Double.MIN_VALUE;
	eps1=1.0e10;
	out[1]=0.0;
	bp[0]=max[0]=0;
	/* smooth integration without break-points */
	if (!peidefunct(nobs,m,par,res,n,m,nobs,nbp,first,sec,max,
                  nis,eps1,weight,bp,save,ymax,y,yp,fy,fp,
                  cobs,tobs,obs,in,aux,clean,method)) {
  	if (save[35] != 0.0) out[1]=save[35];
  	out[3]=res1;
  	out[4]=nfe;
  	out[5]=max[0];
		return;
  }
	res1=Math.sqrt(Basic.vecvec(1,nobs,0,res,res));
	nfe=1;
	if (in[5] == 1.0) {
		out[1]=1.0;
  	if (save[35] != 0.0) out[1]=save[35];
  	out[3]=res1;
  	out[4]=nfe;
  	out[5]=max[0];
		return;
	}
	if (clean) {
		first=true;
		clean=false;
		fac3=Math.sqrt(Math.sqrt(in[3]/res1));
		fac4=Math.sqrt(Math.sqrt(in[4]/res1));
		eps1=res1*fac4;
		if (!peidefunct(nobs,m,par,res,n,m,nobs,nbp,first,sec,max,
                    nis,eps1,weight,bp,save,ymax,y,yp,fy,fp,
                    cobs,tobs,obs,in,aux,clean,method)) {
    	if (out[1] == 3.0)
    		out[1]=2.0;
    	else
    		if (out[1] == 4.0) out[1]=6.0;
    	if (save[35] != 0.0) out[1]=save[35];
    	out[3]=res1;
    	out[4]=nfe;
    	out[5]=max[0];
      return;
    }
		first=false;
	} else
		nfe=0;
	ncol=m+nbp[0];
	nrow=nobs+nbp[0];
	sec[0]=true;
	in3=in[3];
	in4=in[4];
	in[3]=res1;
	weight=away=0;
	out[4]=out[5]=w=0.0;
	temp=Math.sqrt(weight)+1.0;
	weight=(int)(temp*temp);
	while (weight != 16 && nbp[0] > 0) {
		if (away == 0 && w != 0.0) {
			/* if no break-points were omitted then one function
				function evaluation is saved */
			w=weight/w;
			for (i=nobs+1; i<=nrow; i++) {
				for (j=1; j<=ncol; j++) yp[i][j] *= w;
				res[i] *= w;
			}
			sec[0]=true;
			nfe--;
		}
		in[3] *= fac3*weight;
		in[4]=eps1;
		method.monitor(2,ncol,nrow,par,res,weight,nis);
		/* marquardt's method */
		double val[] = new double[ncol+1];
		double b[] = new double[ncol+1];
		double bb[] = new double[ncol+1];
		double parpres[] = new double[ncol+1];
		double jaco[][] = new double[nrow+1][ncol+1];
		vv=10.0;
		w2=0.5;
		mu=0.01;
		ww = (in[6] < 1.0e-7) ? 1.0e-8 : 1.0e-1*in[6];
		em[0]=em[2]=em[6]=in[0];
		em[4]=10*ncol;
		reltolres=in[3];
		abstolres=in[4]*in[4];
		maxfe=(int)in[5];
		err=0;
		fe=it=1;
		p=fpar=res2=0.0;
		pw = -Math.log(ww*in[0])/2.30;
		if (!peidefunct(nrow,ncol,par,res,n,m,nobs,nbp,first,sec,
                    max,nis,eps1,weight,bp,save,ymax,y,yp,fy,
                    fp,cobs,tobs,obs,in,aux,clean,method))
			err=3;
		else {
			fpar=Basic.vecvec(1,nrow,0,res,res);
			out[3]=Math.sqrt(fpar);
			emergency=false;
			it=1;
			do {
				Basic.dupmat(1,nrow,1,ncol,jaco,yp);
				i=Linear_algebra.qrisngvaldec(jaco,nrow,ncol,val,aid,em);
				if (it == 1)
					lambda=in[6]*Basic.vecvec(1,ncol,0,val,val);
				else
					if (p == 0.0) lambda *= w2;
				for (i=1; i<=ncol; i++)
					b[i]=val[i]*Basic.tamvec(1,nrow,i,jaco,res);
				while (true) {
					for (i=1; i<=ncol; i++)
						bb[i]=b[i]/(val[i]*val[i]+lambda);
					for (i=1; i<=ncol; i++)
						parpres[i]=par[i]-Basic.matvec(1,ncol,i,aid,bb);
					fe++;
					if (fe >= maxfe)
						err=1;
					else
						if (!peidefunct(nrow,ncol,parpres,res,n,m,nobs,
                            nbp,first,sec,max,nis,eps1,weight,
                            bp,save,ymax,y,yp,fy,fp,cobs,tobs,
                            obs,in,aux,clean,method))
							err=2;
					if (err != 0) {
						emergency=true;
						break;
					}
					fparpres=Basic.vecvec(1,nrow,0,res,res);
					res2=fpar-fparpres;
					if (res2 < mu*Basic.vecvec(1,ncol,0,b,bb)) {
						p += 1.0;
						lambda *= vv;
						if (p == 1.0) {
							lambdamin=ww*Basic.vecvec(1,ncol,0,val,val);
							if (lambda < lambdamin) lambda=lambdamin;
						}
						if (p >= pw) {
							err=4;
							emergency=true;
							break;
						}
					} else {
						Basic.dupvec(1,ncol,0,par,parpres);
						fpar=fparpres;
						break;
					}
				}
				if (emergency) break;
				it++;
			} while (fpar>abstolres && res2>reltolres*fpar+abstolres);
			for (i=1; i<=ncol; i++)
				Basic.mulcol(1,ncol,i,i,jaco,aid,1.0/(val[i]+in[0]));
			for (i=1; i<=ncol; i++)
				for (j=1; j<=i; j++)
					aid[i][j]=aid[j][i]=Basic.mattam(1,ncol,i,j,jaco,jaco);
			lambda=lambdamin=val[1];
			for (i=2; i<=ncol; i++)
				if (val[i] > lambda)
					lambda=val[i];
				else
					if (val[i] < lambdamin) lambdamin=val[i];
			temp=lambda/(lambdamin+in[0]);
			out[7]=temp*temp;
			out[2]=Math.sqrt(fpar);
			out[6]=Math.sqrt(res2+fpar)-out[2];
		}
		out[4]=fe;
		out[5]=it-1;
		out[1]=err;
		if (out[1] > 0.0) {
    	if (out[1] == 3.0)
    		out[1]=2.0;
    	else
    		if (out[1] == 4.0) out[1]=6.0;
    	if (save[35] != 0.0) out[1]=save[35];
    	out[3]=res1;
    	out[4]=nfe;
    	out[5]=max[0];
			return;
    }
		/* the relative starting value of lambda is adjusted
			to the last value of lambda used */
		away=(int)(out[4]-out[5]-1.0);
		in[6] *= Math.pow(5.0,away)*Math.pow(2.0,away-out[5]);
		nfe += out[4];
		w=weight;
		temp=Math.sqrt(weight)+1.0;
		eps1=temp*temp*in[4]*fac4;
		away=0;
		/* omit useless break-points */
		for (j=1; j<=nbp[0]; j++)
			if (Math.abs(obs[bp[j]]+res[bp[j]]-par[j+m]) < eps1) {
				nbp[0]--;
				for (i=j; i<=nbp[0]; i++) bp[i]=bp[i+1];
				Basic.dupvec(j+m,nbp[0]+m,1,par,par);
				j--;
				away++;
				bp[nbp[0]+1]=0;
			}
		ncol -= away;
		nrow -= away;
		temp=Math.sqrt(weight)+1.0;
		weight=(int)(temp*temp);
	}
	in[3]=in3;
	in[4]=in4;
	nbp[0]=0;
	weight=1;
	method.monitor(2,m,nobs,par,res,weight,nis);
	/* marquardt's method */
	double val[] = new double[m+1];
	double b[] = new double[m+1];
	double bb[] = new double[m+1];
	double parpres[] = new double[m+1];
	double jaco[][] = new double[nobs+1][m+1];
	vv=10.0;
	w2=0.5;
	mu=0.01;
	ww = (in[6] < 1.0e-7) ? 1.0e-8 : 1.0e-1*in[6];
	em[0]=em[2]=em[6]=in[0];
	em[4]=10*m;
	reltolres=in[3];
	abstolres=in[4]*in[4];
	maxfe=(int)in[5];
	err=0;
	fe=it=1;
	p=fpar=res2=0.0;
	pw = -Math.log(ww*in[0])/2.30;
	if (!peidefunct(nobs,m,par,res,n,m,nobs,nbp,first,sec,max,
                  nis,eps1,weight,bp,save,ymax,y,yp,fy,fp,cobs,
                  tobs,obs,in,aux,clean,method))
		err=3;
	else {
		fpar=Basic.vecvec(1,nobs,0,res,res);
		out[3]=Math.sqrt(fpar);
		emergency=false;
		it=1;
		do {
			Basic.dupmat(1,nobs,1,m,jaco,yp);
			i=Linear_algebra.qrisngvaldec(jaco,nobs,m,val,jtjinv,em);
			if (it == 1)
				lambda=in[6]*Basic.vecvec(1,m,0,val,val);
			else
				if (p == 0.0) lambda *= w2;
			for (i=1; i<=m; i++)
				b[i]=val[i]*Basic.tamvec(1,nobs,i,jaco,res);
			while (true) {
				for (i=1; i<=m; i++)
					bb[i]=b[i]/(val[i]*val[i]+lambda);
				for (i=1; i<=m; i++)
					parpres[i]=par[i]-Basic.matvec(1,m,i,jtjinv,bb);
				fe++;
				if (fe >= maxfe)
					err=1;
				else
					if (!peidefunct(nobs,m,parpres,res,n,m,nobs,nbp,first,
                          sec,max,nis,eps1,weight,bp,save,ymax,
                          y,yp,fy,fp,cobs,tobs,obs,in,aux,clean,
                          method))
						err=2;
				if (err != 0) {
					emergency=true;
					break;
				}
				fparpres=Basic.vecvec(1,nobs,0,res,res);
				res2=fpar-fparpres;
				if (res2 < mu*Basic.vecvec(1,m,0,b,bb)) {
					p += 1.0;
					lambda *= vv;
					if (p == 1.0) {
						lambdamin=ww*Basic.vecvec(1,m,0,val,val);
						if (lambda < lambdamin) lambda=lambdamin;
					}
					if (p >= pw) {
						err=4;
						emergency=true;
						break;
					}
				} else {
					Basic.dupvec(1,m,0,par,parpres);
					fpar=fparpres;
					break;
				}
			}
			if (emergency) break;
			it++;
		} while (fpar>abstolres && res2>reltolres*fpar+abstolres);
		for (i=1; i<=m; i++)
			Basic.mulcol(1,m,i,i,jaco,jtjinv,1.0/(val[i]+in[0]));
		for (i=1; i<=m; i++)
			for (j=1; j<=i; j++)
				jtjinv[i][j]=jtjinv[j][i]=Basic.mattam(1,m,i,j,jaco,jaco);
		lambda=lambdamin=val[1];
		for (i=2; i<=m; i++)
			if (val[i] > lambda)
				lambda=val[i];
			else
				if (val[i] < lambdamin) lambdamin=val[i];
		temp=lambda/(lambdamin+in[0]);
		out[7]=temp*temp;
		out[2]=Math.sqrt(fpar);
		out[6]=Math.sqrt(res2+fpar)-out[2];
	}
	out[4]=fe;
	out[5]=it-1;
	out[1]=err;
	nfe += out[4];

	if (out[1] == 3.0)
		out[1]=2.0;
	else
		if (out[1] == 4.0) out[1]=6.0;
	if (save[35] != 0.0) out[1]=save[35];
	out[3]=res1;
	out[4]=nfe;
	out[5]=max[0];
}

                	
static private boolean peidefunct(int nrow, int ncol, double par[],
               double res[], int n, int m, int nobs, int nbp[],
               boolean first, boolean sec[], int max[], int nis[],
               double eps1, int weight, int bp[], double save[],
               double ymax[], double y[], double yp[][],
               double fy[][], double fp[][], int cobs[],
               double tobs[], double obs[], double in[],
               double aux[], boolean clean, AP_peide_methods method)
{
	/* this function is internally used by PEIDE */

  boolean evaluate,evaluated,conv,newstart,errortestok;
	int l,k,fails,same,kpold,n6,nnpar,j5n,cobsii,extra,npar,i,j,jj,ii;
	double xold,hold,error,dfi,tobsdif,xend,hmax,hmin,eps,s,aa,t,c;
  double a[] = new double[6];
  boolean decompose[] = new boolean[1];
  int knew[] = new int[1];
  double ch[] = new double[1];
  double x[] = new double[1];
  double h[] = new double[1];
  double a0[] = new double[1];
  double tolup[] = new double[1];
  double tol[] = new double[1];
  double toldwn[] = new double[1];
  double tolconv[] = new double[1];
  double chnew[] = new double[1];

	int p[] = new int[n+1];
	double delta[] = new double[n+1];
	double lastdelta[] = new double[n+1];
	double df[] = new double[n+1];
	double y0[] = new double[n+1];
	double jacob[][] = new double[n+1][n+1];

  conv=true;
  error=tobsdif=0.0;
	if (sec[0]) {
		sec[0]=false;
  	if (save[36] > max[0]) max[0]=(int)save[36];
  	if (!first) method.monitor(1,ncol,nrow,par,res,weight,nis);
  	return (save[37] <= 40.0 && save[35] == 0.0);
	}
	xend=tobs[nobs];
	eps=in[2];
	npar=m;
	extra=nis[0]=0;
	ii=1;
	jj = (nbp[0] == 0) ? 0 : 1;
	n6=n*6;
	Basic.inivec(35,37,save,0.0);
	Basic.inivec(n6+1,(6+m)*n,y,0.0);
	Basic.inimat(1,nobs+nbp[0],1,m+nbp[0],yp,0.0);
	t=tobs[1];
	x[0]=tobs[0];
	method.callystart(n,m,par,y,ymax);
	hmax=tobs[1]-tobs[0];
	hmin=hmax*in[1];
	/* evaluate jacobian */
	evaluate=false;
	decompose[0]=evaluated=true;
	if (!method.jacdfdy(n,m,par,y,x,fy)) {
		save[35]=4.0;
  	if (save[36] > max[0]) max[0]=(int)save[36];
  	if (!first) method.monitor(1,ncol,nrow,par,res,weight,nis);
  	return (save[37] <= 40.0 && save[35] == 0.0);
	}
	nnpar=n*npar;

	newstart=true;
	while (newstart) {
		newstart=false;
  	k=1;
  	kpold=0;
  	same=2;
  	peideorder(n,k,eps,a,save,tol,tolup,
               toldwn,tolconv,a0,decompose);
  	if (!method.deriv(n,m,par,y,x,df)) {
  		save[35]=3.0;
    	if (save[36] > max[0]) max[0]=(int)save[36];
    	if (!first)
        method.monitor(1,ncol,nrow,par,res,weight,nis);
    	return (save[37] <= 40.0 && save[35] == 0.0);
  	}
	  s=Double.MIN_VALUE;
  	for (i=1; i<=n; i++) {
	  	aa=Basic.matvec(1,n,i,fy,df)/ymax[i];
		  s += aa*aa;
  	}
  	h[0]=Math.sqrt(2.0*eps/Math.sqrt(s));
  	if (h[0] > hmax)
  		h[0]=hmax;
  	else
	  	if (h[0] < hmin) h[0]=hmin;
  	xold=x[0];
  	hold=h[0];
  	ch[0]=1.0;
  	for (i=1; i<=n; i++) {
  		save[i+38]=y[i];
  		save[n+i+38]=y[n+i]=df[i]*h[0];
  	}
  	fails=0;
  	while (x[0] < xend && !newstart) {
	  	if (x[0]+h[0] <= xend)
		  	x[0] += h[0];
  		else {
	  		h[0]=xend-x[0];
		  	x[0]=xend;
			  ch[0]=h[0]/hold;
  			c=1.0;
	  		for (j=n; j<=k*n; j += n) {
		  		c *= ch[0];
			  	for (i=j+1; i<=j+n; i++) y[i] *= c;
  			}
	  		same = (same < 3) ? 3 : same+1;
		  }
  		/* prediction */
	  	for (l=1; l<=n; l++) {
		  	for (i=l; i<=(k-1)*n+l; i += n)
			  	for (j=(k-1)*n+l; j>=i; j -= n) y[j] += y[j+n];
  			delta[l]=0.0;
	  	}
  		evaluated=false;
	  	/* correction and estimation local error */
  		for (l=1; l<=3; l++) {
	  		if (!method.deriv(n,m,par,y,x,df)) {
		  		save[35]=3;
        	if (save[36] > max[0]) max[0]=(int)save[36];
      	  if (!first)
            method.monitor(1,ncol,nrow,par,res,weight,nis);
        	return (save[37] <= 40.0 && save[35] == 0.0);
  			}
  			for (i=1; i<=n; i++) df[i]=df[i]*h[0]-y[n+i];
	  		if (evaluate) {
		  		/* evaluate jacobian */
			  	evaluate=false;
  				decompose[0]=evaluated=true;
	  			if (!method.jacdfdy(n,m,par,y,x,fy)) {
		  			save[35]=4.0;
          	if (save[36] > max[0]) max[0]=(int)save[36];
          	if (!first)
              method.monitor(1,ncol,nrow,par,res,weight,nis);
          	return (save[37] <= 40.0 && save[35] == 0.0);
	  			}
		  	}
			  if (decompose[0]) {
  				/* decompose jacobian */
	  			decompose[0]=false;
		  		c = -a0[0]*h[0];
			  	for (j=1; j<=n; j++) {
				  	for (i=1; i<=n; i++) jacob[i][j]=fy[i][j]*c;
					  jacob[j][j] += 1.0;
  				}
          Linear_algebra.dec(jacob,n,aux,p);
		  	}
			  Linear_algebra.sol(jacob,n,p,df);
  			conv=true;
	  		for (i=1; i<=n; i++) {
		  		dfi=df[i];
			  	y[i] += a0[0]*dfi;
				  y[n+i] += dfi;
  				delta[i] += dfi;
	  			conv=(conv && (Math.abs(dfi) < tolconv[0]*ymax[i]));
		  	}
  			if (conv) {
	  			s=Double.MIN_VALUE;
		  		for (i=1; i<=n; i++) {
			  		aa=delta[i]/ymax[i];
  					s += aa*aa;
	  			}
		  		error=s;
			  	break;
  			}
	  	}
  		/* acceptance or rejection */
	  	if (!conv) {
		  	if (!evaluated)
  				evaluate=true;
	  		else {
		  		ch[0] /= 4.0;
			  	if (h[0] < 4.0*hmin) {
				  	save[37] += 10.0;
					  hmin /= 10.0;
  					if (save[37] > 40.0) {
            	if (save[36] > max[0]) max[0]=(int)save[36];
            	if (!first)
                method.monitor(1,ncol,nrow,par,res,weight,nis);
            	return (save[37] <= 40.0 && save[35] == 0.0);
            }
	  			}
		  	}
  			peidereset(n,k,hmin,hmax,hold,xold,y,
                   save,ch,x,h,decompose);
      } else {
      	errortestok=true;
      	if (error > tol[0]) {
      		errortestok=false;
  		  	fails++;
	  		  if (h[0] > 1.1*hmin) {
  	  			if (fails > 2) {
	  	  			peidereset(n,k,hmin,hmax,hold,xold,y,
                         save,ch,x,h,decompose);
	  		  		newstart=true;
  			  	} else {
	  			  	/* calculate step and order */
		  			  peidestep(n,k,fails,tolup,toldwn,tol,error,delta,
                        lastdelta,y,ymax,knew,chnew);
    					if (knew[0] != k) {
	    					k=knew[0];
		    				peideorder(n,k,eps,a,save,tol,tolup,
                           toldwn,tolconv,a0,decompose);
    					}
	    				ch[0] *= chnew[0];
		    			peidereset(n,k,hmin,hmax,hold,xold,y,
                         save,ch,x,h,decompose);
			    	}
    			} else {
	    			if (k == 1) {
		    			/* violate eps criterion */
			    		save[36] += 1.0;
				    	same=4;
				    	errortestok=true;
    				} else {
              k=1;
    		  		peidereset(n,k,hmin,hmax,hold,xold,y,
                         save,ch,x,h,decompose);
	    		  	peideorder(n,k,eps,a,save,tol,tolup,
                         toldwn,tolconv,a0,decompose);
    	  			same=2;
            }
	    		}
  		  }
  		  if (errortestok && !newstart) {
  	  		fails=0;
	  	  	for (i=1; i<=n; i++) {
		  	  	c=delta[i];
			  	  for (l=2; l<=k; l++) y[l*n+i] += a[l]*c;
  				  if (Math.abs(y[i]) > ymax[i]) ymax[i]=Math.abs(y[i]);
    			}
	    		same--;
		    	if (same == 1)
			    	Basic.dupvec(1,n,0,lastdelta,delta);
    			else if (same == 0) {
	    			/* calculate step and order */
		    		peidestep(n,k,fails,tolup,toldwn,tol,error,delta,
                      lastdelta,y,ymax,knew,chnew);
	  		  	if (chnew[0] > 1.1) {
		  		  	if (k != knew[0]) {
			  		  	if (knew[0] > k)
  				  			Basic.mulvec(knew[0]*n+1,knew[0]*n+n,
                               -knew[0]*n,y,delta,a[k]/knew[0]);
		  				  k=knew[0];
  		  				peideorder(n,k,eps,a,save,tol,tolup,
                           toldwn,tolconv,a0,decompose);
  		  			}
	  		  		same=k+1;
		  		  	if (chnew[0]*h[0] > hmax) chnew[0]=hmax/h[0];
			  		  h[0] *= chnew[0];
  			  		c=1.0;
	  			  	for (j=n; j<=k*n; j += n) {
  		  				c *= chnew[0];
	  		  			Basic.mulvec(j+1,j+n,0,y,y,c);
		  		  	}
  		  			decompose[0]=true;
	  		  	} else
		  		  	same=10;
  			  }
    			nis[0]++;
	    		/* start of an integration step of yp */
  		  	if (clean) {
	  		  	hold=h[0];
		  		  xold=x[0];
  		  		kpold=k;
	  		  	ch[0]=1.0;
  		  		Basic.dupvec(39,k*n+n+38,-38,save,y);
	  		  } else {
		  		  if (h[0] != hold) {
			  		  ch[0]=h[0]/hold;
  			  		c=1.0;
	  			  	for (j=n6+nnpar; j<=kpold*nnpar+n6; j += nnpar) {
  		  				c *= ch[0];
	  		  			for (i=j+1; i<=j+nnpar; i++) y[i] *= c;
		  		  	}
			  		  hold=h[0];
    				}
	    			if (k > kpold)
		    			Basic.inivec(n6+k*nnpar+1,n6+k*nnpar+nnpar,y,0.0);
			    	xold=x[0];
				    kpold=k;
    				ch[0]=1.0;
	    			Basic.dupvec(39,k*n+n+38,-38,save,y);
		    		/* evaluate jacobian */
			    	evaluate=false;
				    decompose[0]=evaluated=true;
    				if (!method.jacdfdy(n,m,par,y,x,fy)) {
	    				save[35]=4.0;
            	if (save[36] > max[0]) max[0]=(int)save[36];
            	if (!first)
                method.monitor(1,ncol,nrow,par,res,weight,nis);
            	return (save[37] <= 40.0 && save[35] == 0.0);
    				}
	    			/* decompose jacobian */
  		  		decompose[0]=false;
	  		  	c = -a0[0]*h[0];
		  		  for (j=1; j<=n; j++) {
			  		  for (i=1; i<=n; i++) jacob[i][j]=fy[i][j]*c;
    					jacob[j][j] += 1.0;
	    			}
            Linear_algebra.dec(jacob,n,aux,p);
			    	if (!method.jacdfdp(n,m,par,y,x,fp)) {
				    	save[35]=5.0;
          	  if (save[36] > max[0]) max[0]=(int)save[36];
            	if (!first)
                method.monitor(1,ncol,nrow,par,res,weight,nis);
            	return (save[37] <= 40.0 && save[35] == 0.0);
		    		}
			    	if (npar > m) Basic.inimat(1,n,m+1,npar,fp,0.0);
    				/* prediction */
	    			for (l=0; l<=k-1; l++)
		    			for (j=k-1; j>=l; j--)
			    			Basic.elmvec(j*nnpar+n6+1,j*nnpar+n6+nnpar,nnpar,
                             y,y,1.0);
    				/* correction */
    				for (j=1; j<=npar; j++) {
	    				j5n=(j+5)*n;
		    			Basic.dupvec(1,n,j5n,y0,y);
  			  		for (i=1; i<=n; i++)
	  			  		df[i]=h[0]*(fp[i][j]+Basic.matvec(1,n,i,fy,y0))-
		  			  				y[nnpar+j5n+i];
  		  			Linear_algebra.sol(jacob,n,p,df);
	  		  		for (l=0; l<=k; l++) {
		  		  		i=l*nnpar+j5n;
			  		  	Basic.elmvec(i+1,i+n,-i,y,df,a[l]);
  				  	}
    				}
	    		}
		    	while (x[0] >= t) {
  			  	/* calculate a row of the jacobian matrix and an
	  			  	element of the residual vector */
  	  			tobsdif=(tobs[ii]-x[0])/h[0];
	  	  		cobsii=cobs[ii];
		  	  	res[ii]=peideinterpol(cobsii,n,k,tobsdif,y)-obs[ii];
  			  	if (!clean) {
	  			  	for (i=1; i<=npar; i++)
		  			  	yp[ii][i]=peideinterpol(cobsii+(i+5)*n,nnpar,k,
                                        tobsdif,y);
    					/* introducing break-points */
	    				if (bp[jj] != ii) {
		    			} else if (first && Math.abs(res[ii]) < eps1) {
			    			nbp[0]--;
				    		for (i=jj; i<=nbp[0]; i++) bp[i]=bp[i+1];
					    	bp[nbp[0]+1]=0;
  					  } else {
	  					  extra++;
  		  				if (first) par[m+jj]=obs[ii];
	  		  			/* introducing a jacobian row and a residual
		  		  			vector element for continuity requirements */
  		  				yp[nobs+jj][m+jj] = -weight;
	  		   			Basic.mulrow(1,npar,nobs+jj,ii,yp,yp,weight);
  		  				res[nobs+jj]=weight*(res[ii]+obs[ii]-par[m+jj]);
    					}
	    			}
		    		if (ii == nobs) {
            	if (save[36] > max[0]) max[0]=(int)save[36];
            	if (!first)
                method.monitor(1,ncol,nrow,par,res,weight,nis);
          	  return (save[37] <= 40.0 && save[35] == 0.0);
  	  			}
	  	  		else {
		  	  		t=tobs[ii+1];
			  	  	if (bp[jj] == ii && jj < nbp[0]) jj++;
  					  hmax=t-tobs[ii];
    					hmin=hmax*in[1];
	    				ii++;
		    		}
  			  }
  	  		/* break-points introduce new initial values for y & yp */
	  	  	if (extra > 0) {
		  	  	for (i=1; i<=n; i++) {
			  	  	y[i]=peideinterpol(i,n,k,tobsdif,y);
				  	  for (j=1; j<=npar; j++)
  				  		y[i+(j+5)*n]=
                       peideinterpol(i+(j+5)*n,nnpar,k,tobsdif,y);
  		  		}
    				for (l=1; l<=extra; l++) {
	    				cobsii=cobs[bp[npar-m+l]];
		    			y[cobsii]=par[npar+l];
			    		for (i=1; i<=npar+extra; i++) y[cobsii+(5+i)*n]=0.0;
				    	Basic.inivec(1+nnpar+(l+5)*n,nnpar+(l+6)*n,y,0.0);
					    y[cobsii+(5+npar+l)*n]=1.0;
    				}
	    			npar += extra;
		    		extra=0;
			    	x[0]=tobs[ii-1];
				    /* evaluate jacobian */
  				  evaluate=false;
  	  			decompose[0]=evaluated=true;
	  	  		if (!method.jacdfdy(n,m,par,y,x,fy)) {
		  	  		save[35]=4.0;
            	if (save[36] > max[0]) max[0]=(int)save[36];
          	  if (!first)
                method.monitor(1,ncol,nrow,par,res,weight,nis);
            	return (save[37] <= 40.0 && save[35] == 0.0);
  	  			}
	  	  		nnpar=n*npar;
		  	  	newstart=true;
  		  	}
  	  	}  /*  errortestok */
      }
  	}  /* while (x[0] < xend && !newstart) loop */
  }  /* newstart loop */

	if (save[36] > max[0]) max[0]=(int)save[36];
	if (!first) method.monitor(1,ncol,nrow,par,res,weight,nis);
	return (save[37] <= 40.0 && save[35] == 0.0);
}


static private void peidereset(int n, int k, double hmin,
               double hmax, double hold, double xold,
               double y[], double save[], double ch[],
               double x[], double h[], boolean decompose[])
{
	/* this function is internally used by PEIDEFUNCT of PEIDE */

	int i,j;
	double c;

	if (ch[0] < hmin/hold)
		ch[0] = hmin/hold;
	else
		if (ch[0] > hmax/hold) ch[0] = hmax/hold;
	x[0] = xold;
	h[0] = hold*ch[0];
	c=1.0;
	for (j=0; j<=k*n; j += n) {
		for (i=1; i<=n; i++) y[j+i]=save[j+i+38]*c;
		c *= ch[0];
	}
	decompose[0] = true;
}


static private void peideorder(int n, int k, double eps,
               double a[], double save[], double tol[],
               double tolup[], double toldwn[], double tolconv[],
               double a0[], boolean decompose[])
{
	/* this function is internally used by PEIDEFUNCT of PEIDE */

	int i,j;
	double c;

	c=eps*eps;
	j=((k-1)*(k+8))/2-38;
	for (i=0; i<=k; i++) a[i]=save[i+j+38];
	j += k+1;
	tolup[0] = c*save[j+38];
	tol[0] = c*save[j+39];
	toldwn[0] = c*save[j+40];
	tolconv[0] = eps/(2*n*(k+2));
	a0[0] = a[0];
	decompose[0] = true;
}


static private void peidestep(int n, int k, int fails,
                    double tolup[], double toldwn[],
                    double tol[], double error, double delta[],
                    double lastdelta[], double y[],
                    double ymax[], int knew[], double chnew[])
{
	/* this function is internally used by PEIDEFUNCT of PEIDE */

	int i;
	double a1,a2,a3,aa,s;

	if (k <= 1)
		a1=0.0;
	else {
		s=Double.MIN_VALUE;
		for (i=1; i<=n; i++) {
			aa=y[k*n+i]/ymax[i];
			s += aa*aa;
		}
		a1=0.75*Math.pow(toldwn[0]/s,0.5/k);
	}
	a2=0.80*Math.pow(tol[0]/error,0.5/(k+1));
	if (k >= 5 || fails != 0)
		a3=0.0;
	else {
		s=Double.MIN_VALUE;
		for (i=1; i<=n; i++) {
			aa=(delta[i]-lastdelta[i])/ymax[i];
			s += aa*aa;
		}
		a3=0.70*Math.pow(tolup[0]/s,0.5/(k+2));
	}
	if (a1 > a2 && a1 > a3) {
		knew[0] = k-1;
		chnew[0] = a1;
	} else if (a2 > a3) {
		knew[0] = k;
		chnew[0] = a2;
	} else {
		knew[0] = k+1;
		chnew[0] = a3;
	}
}


static private double peideinterpol(int startindex, int jump,
                      int k, double tobsdif, double y[])
{
	/* this function is internally used by PEIDEFUNCT of PEIDE */

	int i;
	double s,r;

	s=y[startindex];
	r=tobsdif;
	for (i=1; i<=k; i++) {
		startindex += jump;
		s += y[startindex]*r;
		r *= tobsdif;
	}
	return s;
}

}