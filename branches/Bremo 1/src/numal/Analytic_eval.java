package numal;

import numal.*;

public class Analytic_eval extends Object {


public static double euler(AE_euler_method method, double eps, int tim)
{
	int i,k,n,t;
	double mn,mp,ds,sum;
	double m[] = new double[16];

	n=t=i=0;
	m[0]=method.ai(i);
	sum=m[0]/2.0;
	do {
		i++;
		mn=method.ai(i);
		for (k=0; k<=n; k++) {
			mp=(mn+m[k])/2.0;
			m[k]=mn;
			mn=mp;
		}
		if (Math.abs(mn) < Math.abs(m[n]) && n < 15) {
			ds=mn/2.0;
			n++;
			m[n]=mn;
		} else
			ds=mn;
		sum += ds;
		t = (Math.abs(ds) < eps) ? t+1 : 0;
	} while (t < tim);
	return sum;
}


public static double sumposseries(AE_sumposseries_method method,
       int maxaddup, double maxzero, int maxrecurs, int machexp, int tim)
{
	int recurs,vl,vl2,vl4;

	recurs=0;
	vl=1000;
	vl2=2*vl;
	vl4=2*vl2;
	return sumposseriessumup(false,method,maxaddup,maxzero,maxrecurs,
					                 machexp,tim,recurs,vl,vl2,vl4,0);
}


static private double sumposseriessumup(boolean bjk,
              AE_sumposseries_method method,
			      	int maxaddup, double maxzero, int maxrecurs,
			      	int machexp, int tim, int recurs, int vl,
              int vl2, int vl4, int jj)
{
	/* this function is internally used by SUMPOSSERIES */

  boolean transform,jodd;
	int j,j2,k,n,t;
	double i,sum,nextterm,mn,mp,ds,esum,temp,vj;
  double m[] = new double[16];

	i=maxaddup+1;
	j=1;
	transform=false;
	while (true) {
		temp = (bjk) ? sumposseriesbjk(jj,i,machexp,method) : method.ai(i);
		if (temp <= maxzero) {
			if (j >= tim) break;
			j++;
			i++;
		} else {
			if (recurs != maxrecurs) transform=true;
			break;
		}
	}
	if (!transform) {
		sum=i=0.0;
		j=0;
		do {
			i++;
			nextterm = (bjk) ?
				sumposseriesbjk(jj,i,machexp,method) : method.ai(i);
			j = (nextterm <= maxzero) ? j+1 : 0;
			sum += nextterm;
		} while (j < tim);
		return sum;
	}
	/* transform series */
	double v[] = new double[vl+1];
	j2=0;
	jodd=true;
	/* euler */
	n=t=j=0;
	jj=j+1;
	if (jodd) {
		jodd=false;
		recurs++;
		temp=vj=sumposseriessumup(true,method,maxaddup,maxzero,
					maxrecurs,machexp,tim,recurs,vl,vl2,vl4,jj);
		recurs--;
		if (jj <= vl)
			v[jj]=temp;
		else
			if (jj <= vl2) v[jj-vl]=temp;
	} else {
		jodd=true;
		if (jj > vl4) {
			recurs++;
			vj = -sumposseriessumup(true,method,maxaddup,maxzero,
						maxrecurs,machexp,tim,recurs,vl,vl2,vl4,jj);
			recurs--;
		} else {
			j2++;
			i=j2;
			if (jj > vl2) {
				temp = (bjk) ?
							sumposseriesbjk(jj,i,machexp,method) : method.ai(i);
				vj = -(v[j2-vl]-temp)/2.0;
			}
			else {
				temp = (bjk) ?
							sumposseriesbjk(jj,i,machexp,method) : method.ai(i);
				temp=v[(jj <= vl) ? jj : jj-vl]=(v[j2]-temp)/2.0;
				vj = -temp;
			}
		}
	}
	m[0]=vj;
	esum=m[0]/2.0;
	do {
		j++;
		jj=j+1;
		if (jodd) {
			jodd=false;
			recurs++;
			temp=vj=sumposseriessumup(true,method,maxaddup,maxzero,
							maxrecurs,machexp,tim,recurs,vl,vl2,vl4,jj);
			recurs--;
			if (jj <= vl)
				v[jj]=temp;
			else
				if (jj <= vl2) v[jj-vl]=temp;
		} else {
			jodd=true;
			if (jj > vl4) {
				recurs++;
				vj = -sumposseriessumup(true,method,maxaddup,maxzero,
							maxrecurs,machexp,tim,recurs,vl,vl2,vl4,jj);
				recurs--;
			} else {
				j2++;
				i=j2;
				if (jj > vl2) {
					temp = (bjk) ?
								sumposseriesbjk(jj,i,machexp,method) : method.ai(i);
					vj = -(v[j2-vl]-temp)/2.0;
				} else {
					temp = (bjk) ?
								sumposseriesbjk(jj,i,machexp,method) : method.ai(i);
					temp=v[(jj <= vl) ? jj : jj-vl]=(v[j2]-temp)/2.0;
					vj = -temp;
				}
			}
		}
		mn=vj;
		for (k=0; k<=n; k++) {
			mp=(mn+m[k])/2.0;
			m[k]=mn;
			mn=mp;
		}
		if (Math.abs(mn) < Math.abs(m[n]) && n < 15) {
			ds=mn/2.0;
			n++;
			m[n]=mn;
		} else
			ds=mn;
		esum += ds;
		t = (Math.abs(ds) < maxzero) ? t+1 : 0;
	} while (t < tim);
	return esum;
}


static private double sumposseriesbjk(int j, double i, double machexp,
                              AE_sumposseries_method method)
{
	/* this function is internally used by SUMPOSSERIES */

	double coeff;

	if (i > machexp) return 0.0;
	coeff=Math.pow(2.0,i-1.0);
	return coeff*method.ai(j*coeff);
}


public static double qadrat(double a, double b,
                            AE_qadrat_method method, double e[])
{
	double x,f0,f2,f3,f5,f6,f7,f9,f14,hmin,hmax,re,ae,result;

	hmax=(b-a)/16.0;
	if (hmax == 0.0) return 0.0;
	re=e[1];
	ae=2.0*e[2]/Math.abs(b-a);
	e[3]=0.0;
	hmin=Math.abs(b-a)*re;
	x=a;
	f0=method.fx(x);
	x=a+hmax;
	f2=method.fx(x);
	x=a+2.0*hmax;
	f3=method.fx(x);
	x=a+4.0*hmax;
	f5=method.fx(x);
	x=a+6.0*hmax;
	f6=method.fx(x);
	x=a+8.0*hmax;
	f7=method.fx(x);
	x=b-4.0*hmax;
	f9=method.fx(x);
	x=b;
	f14=method.fx(x);
  result = lint(method,e,a,b,f0,f2,f3,f5,f6,f7,f9,f14,
						    hmin,hmax,re,ae)*16.0;
	return result;
}


static private double lint(AE_qadrat_method method, double e[],
               double x0, double xn, double f0, double f2, double f3,
               double f5, double f6, double f7, double f9, double f14,
               double hmin, double hmax, double re, double ae)
{
	/* this function is internally used by QADRAT */

	double x,v,w,h,xm,f1,f4,f8,f10,f11,f12,f13;

	xm=(x0+xn)/2.0;
	h=(xn-x0)/32.0;
	x=xm+4.0*h;
	f8=method.fx(x);
	x=xn-4.0*h;
	f11=method.fx(x);
	x=xn-2.0*h;
	f12=method.fx(x);
	v=0.330580178199226*f7+0.173485115707338*(f6+f8)+
		0.321105426559972*(f5+f9)+0.135007708341042*(f3+f11)+
		0.165714514228223*(f2+f12)+0.393971460638127e-1*(f0+f14);
	x=x0+h;
	f1=method.fx(x);
	x=xn-h;
	f13=method.fx(x);
	w=0.260652434656970*f7+0.239063286684765*(f6+f8)+
		0.263062635477467*(f5+f9)+0.218681931383057*(f3+f11)+
		0.275789764664284e-1*(f2+f12)+0.105575010053846*(f1+f13)+
		0.157119426059518e-1*(f0+f14);
	if (Math.abs(h) < hmin) e[3] += 1.0;
	if (Math.abs(v-w) < Math.abs(w)*re+ae || Math.abs(h) < hmin)
		return h*w;
	else {
		x=x0+6.0*h;
		f4=method.fx(x);
		x=xn-6.0*h;
		f10=method.fx(x);
		v=0.245673430093324*f7+0.255786258286921*(f6+f8)+
			0.228526063690406*(f5+f9)+0.500557131525460e-1*(f4+f10)+
			0.177946487736780*(f3+f11)+0.584014599347449e-1*(f2+f12)+
			0.874830942871331e-1*(f1+f13)+
			0.189642078648079e-1*(f0+f14);
		return ((Math.abs(v-w) < Math.abs(v)*re+ae) ? h*v :
      (lint(method,e,x0,xm,f0,f1,f2,f3,f4,f5,f6,f7,hmin,hmax,re,ae)-
      lint(method,e,xn,xm,f14,f13,f12,f11,f10,f9,f8,f7,hmin,hmax,re,ae)));
	}
}


public static double integral(double a, double b,
                              AE_integral_method method, double e[],
                              boolean ua, boolean ub)
{
  double re,ae,b1,x;
	double x0[] = new double[1];
  double x1[] = new double[1];
  double x2[] = new double[1];
  double f0[] = new double[1];
  double f1[] = new double[1];
  double f2[] = new double[1];

  b1=0.0;
	re=e[1];
	if (ub)
		ae=e[2]*180.0/Math.abs(b-a);
	else
		ae=e[2]*90.0/Math.abs(b-a);
	if (ua) {
		e[3]=e[4]=0.0;
		x=x0[0]=a;
		f0[0]=method.fx(x);
	} else {
		x=x0[0]=a=e[5];
		f0[0]=e[6];
	}
  e[5]=x=x2[0]=b;
	e[6]=f2[0]=method.fx(x);
	e[4] += integralqad(false,method,e,x0,x1,x2,f0,f1,f2,re,ae,b1);
	if (!ub) {
		if (a < b) {
			b1=b-1.0;
			x0[0]=1.0;
		} else {
			b1=b+1.0;
			x0[0] = -1.0;
		}
		f0[0]=e[6];
    e[5]=x2[0]=0.0;
    e[6]=f2[0]=0.0;
		ae=e[2]*90.0;
		e[4] -= integralqad(true,method,e,x0,x1,x2,f0,f1,f2,re,ae,b1);
	}
	return e[4];
}


static private double integralqad(boolean transf,
       AE_integral_method method, double e[], double x0[], double x1[],
       double x2[], double f0[], double f1[], double f2[], double re,
       double ae, double b1)
{
	/* this function is internally used by INTEGRAL */

	double hmin,x,z;
  double sum[] = new double[1];

	hmin=Math.abs(x0[0]-x2[0])*re;
	x=x1[0]=(x0[0]+x2[0])*0.5;
	if (transf) {
		z=1.0/x;
		x=z+b1;
		f1[0]=method.fx(x)*z*z;
	} else
		f1[0]=method.fx(x);
	sum[0]=0.0;
	integralint(transf,method,e,x0,x1,x2,f0,f1,f2,sum,re,ae,b1,hmin);
	return sum[0]/180.0;
}


static private void integralint(boolean transf,
       AE_integral_method method, double e[], double x0[], double x1[],
       double x2[], double f0[], double f1[], double f2[], double sum[],
       double re, double ae, double b1, double hmin)
{
	/* this function is internally used by INTEGRALQAD of INTEGRAL */

	boolean anew;
	double x3,x4,f3,f4,h,x,z,v,t;

	x4=x2[0];
	x2[0]=x1[0];
	f4=f2[0];
	f2[0]=f1[0];
	anew=true;
	while (anew) {
		anew=false;
		x=x1[0]=(x0[0]+x2[0])*0.5;
		if (transf) {
			z=1.0/x;
			x=z+b1;
			f1[0]=method.fx(x)*z*z;
		} else
			f1[0]=method.fx(x);
		x=x3=(x2[0]+x4)*0.5;
		if (transf) {
			z=1.0/x;
			x=z+b1;
			f3=method.fx(x)*z*z;
		} else
			f3=method.fx(x);
		h=x4-x0[0];
		v=(4.0*(f1[0]+f3)+2.0*f2[0]+f0[0]+f4)*15.0;
		t=6.0*f2[0]-4.0*(f1[0]+f3)+f0[0]+f4;
		if (Math.abs(t) < Math.abs(v)*re+ae)
			sum[0] += (v-t)*h;
		else if (Math.abs(h) < hmin)
			e[3] += 1.0;
		else {
			integralint(transf,method,e,x0,x1,x2,f0,f1,f2,sum,re,ae,b1,hmin);
			x2[0]=x3;
			f2[0]=f3;
			anew=true;
		}
		if (!anew) {
			x0[0]=x4;
			f0[0]=f4;
		}
	}
}


public static double tricub(double xi, double yi, double xj, double yj,
                     double xk, double yk, AE_tricub_method method,
                     double re, double ae)
{
	double surfmin,xz,yz,gi,gj,gk;
  double surf[] = new double[1];

	surf[0]=0.5*Math.abs(xj*yk-xk*yj+xi*yj-xj*yi+xk*yi-xi*yk);
	surfmin=surf[0]*re;
	re *= 30.0;
	ae=30.0*ae/surf[0];
	xz=(xi+xj+xk)/3.0;
	yz=(yi+yj+yk)/3.0;
	gi=method.g(xi,yi);
	gj=method.g(xj,yj);
	gk=method.g(xk,yk);
	xi *= 0.5;
	yi *= 0.5;
	xj *= 0.5;
	yj *= 0.5;
	xk *= 0.5;
	yk *= 0.5;
	return tricubint(xi,yi,gi,xj,yj,gj,xk,yk,gk,
						xj+xk,yj+yk,method.g(xj+xk,yj+yk),
						xk+xi,yk+yi,method.g(xk+xi,yk+yi),
						xi+xj,yi+yj,method.g(xi+xj,yi+yj),
						0.5*xz,0.5*yz,method.g(xz,yz),
            method,re,ae,surf,surfmin)/60.0;
}


static private double tricubint(double ax1, double ay1, double af1,
       double ax2, double ay2, double af2, double ax3, double ay3,
       double af3, double bx1, double by1, double bf1, double bx2,
       double by2, double bf2, double bx3, double by3, double bf3,
       double px, double py, double pf, AE_tricub_method method,
       double re, double ae, double surf[], double surfmin)
{
	/* this function is internally used by TRICUB */

	double e,i3,i4,i5,a,b,c,sx1,sy1,sx2,sy2,sx3,sy3,cx1,cy1,cf1,cx2,cy2,
	       cf2,cx3,cy3,cf3,dx1,dy1,df1,dx2,dy2,df2,dx3,dy3,df3,result;

	a=af1+af2+af3;
	b=bf1+bf2+bf3;
	i3=3.0*a+27.0*pf+8.0*b;
	e=Math.abs(i3)*re+ae;
	if (surf[0] < surfmin || Math.abs(5.0*a+45.0*pf-i3) < e)
		return i3*surf[0];
	else {
		cx1=ax1+px;
		cy1=ay1+py;
		cf1=method.g(cx1,cy1);
		cx2=ax2+px;
		cy2=ay2+py;
		cf2=method.g(cx2,cy2);
		cx3=ax3+px;
		cy3=ay3+py;
		cf3=method.g(cx3,cy3);
		c=cf1+cf2+cf3;
		i4=a+9.0*pf+4.0*b+12.0*c;
		if (Math.abs(i3-i4) < e)
			return i4*surf[0];
		else {
			sx1=0.5*bx1;
			sy1=0.5*by1;
			dx1=ax1+sx1;
			dy1=ay1+sy1;
			df1=method.g(dx1,dy1);
			sx2=0.5*bx2;
			sy2=0.5*by2;
			dx2=ax2+sx2;
			dy2=ay2+sy2;
			df2=method.g(dx2,dy2);
			sx3=0.5*bx3;
			sy3=0.5*by3;
			dx3=ax3+sx3;
			dy3=ay3+sy3;
			df3=method.g(dx3,dy3);
			i5=(51.0*a+2187.0*pf+276.0*b+972.0*c-768.0*(df1+df2+df3))/63.0;
			if (Math.abs(i4-i5) < e)
				return i5*surf[0];
			else {
				surf[0] *= 0.25;
				result=tricubint(sx1,sy1,bf1,sx2,sy2,bf2,sx3,sy3,bf3,
							dx1,dy1,df1,dx2,dy2,df2,dx3,dy3,df3,px,py,pf,
              method,re,ae,surf,surfmin)+
						tricubint(ax1,ay1,af1,sx3,sy3,bf3,sx2,sy2,bf2,
							dx1,dy1,df1,ax1+sx2,ay1+sy2,
							method.g(ax1+sx2,ay1+sy2),ax1+sx3,ay1+sy3,
							method.g(ax1+sx3,ay1+sy3),0.5*cx1,0.5*cy1,cf1,
              method,re,ae,surf,surfmin)+
						tricubint(ax2,ay2,af2,sx3,sy3,bf3,sx1,sy1,bf1,
							dx2,dy2,df2,ax2+sx1,ay2+sy1,
							method.g(ax2+sx1,ay2+sy1),ax2+sx3,ay2+sy3,
							method.g(ax2+sx3,ay2+sy3),0.5*cx2,0.5*cy2,cf2,
              method,re,ae,surf,surfmin)+
						tricubint(ax3,ay3,af3,sx1,sy1,bf1,sx2,sy2,bf2,
							dx3,dy3,df3,ax3+sx2,ay3+sy2,
							method.g(ax3+sx2,ay3+sy2),ax3+sx1,ay3+sy1,
							method.g(ax3+sx1,ay3+sy1),0.5*cx3,0.5*cy3,cf3,
              method,re,ae,surf,surfmin);
				surf[0] *= 4.0;
				return result;
			}
		}
	}
}


public static void reccof(int n, int m, AE_reccof_method method,
                          double b[], double c[], double l[],
                          boolean sym)
{
	int i,j,up;
	double x,r,s,pim,h,hh,arg,sa,temp;

	pim=4.0*Math.atan(1.0)/m;
	if (sym) {
		for (j=0; j<=n; j++) {
			r=b[j]=0.0;
			up=m/2;
			for (i=1; i<=up; i++) {
				arg=(i-0.5)*pim;
				x=Math.cos(arg);
        temp=Algebraic_eval.ortpol(j,x,b,c);
				r += Math.sin(arg)*method.wx(x)*temp*temp;
			}
			if (up*2 == m)
				l[j]=2.0*r*pim;
			else {
				x=0.0;
        temp=Algebraic_eval.ortpol(j,0.0,b,c);
				l[j]=(2.0*r+method.wx(x)*temp*temp)*pim;
			}
			c[j] = (j == 0) ? 0.0 : l[j]/l[j-1];
		}
	} else
		for (j=0; j<=n; j++) {
			r=s=0.0;
			up=m/2;
			for (i=1; i<=up; i++) {
				arg=(i-0.5)*pim;
				sa=Math.sin(arg);
				x=Math.cos(arg);
        temp=Algebraic_eval.ortpol(j,x,b,c);
				h=method.wx(x)*temp*temp;
				x = -x;
        temp=Algebraic_eval.ortpol(j,x,b,c);
				hh=method.wx(x)*temp*temp;
				r += (h+hh)*sa;
				s += (hh-h)*x*sa;
			}
			b[j]=s*pim;
			if (up*2 == m)
				l[j]=r*pim;
			else {
				x=0.0;
        temp=Algebraic_eval.ortpol(j,0.0,b,c);
				l[j]=(r+method.wx(x)*temp*temp)*pim;
			}
			c[j] = (j == 0) ? 0.0 : l[j]/l[j-1];
		}
}


public static void gsswts(int n, double zer[], double b[],
                          double c[], double w[])
{
	int j,k;
	double s;
	double p[] = new double[n];
	
	for (j=1; j<=n; j++) {
    Algebraic_eval.allortpol(n-1,zer[j],b,c,p);
		s=0.0;
		for (k=n-1; k>=1; k--) s=(s+p[k]*p[k])/c[k];
		w[j]=1.0/(1.0+s);
	}
}


public static void gsswtssym(int n, double zer[], double c[], double w[])
{
	int i,twoi,low,up;
	double s;
	double p[] = new double[n];
	
	low=1;
	up=n;
	while (low < up) {
    Algebraic_eval.allortpolsym(n-1,zer[low],c,p);
		s=p[n-1]*p[n-1];
		for (i=n-1; i>=1; i--) s=s/c[i]+p[i-1]*p[i-1];
		w[low]=1.0/s;
		low++;
		up--;
	}
	if (low == up) {
		s=1.0;
		for (twoi=n-1; twoi>=2; twoi -= 2) s=s*c[twoi-1]/c[twoi]+1.0;
		w[low]=1.0/s;
	}
}


public static void gssjacwghts(int n, double alfa, double beta,
                               double x[], double w[])
{
	int i,j,m;
	double r0,r1,r2,s,h0,alfa2,xi,min,sum,alfabeta,temp;

	if (alfa == beta) {
		double b[] = new double[n];
    Linear_algebra.alljaczer(n,alfa,alfa,x);
		alfa2=2.0*alfa;
    temp=Special_functions.gamma(1.0+alfa);
		h0=Math.pow(2.0,alfa2+1.0)*temp*temp/
		            Special_functions.gamma(alfa2+2.0);
		b[1]=1.0/Math.sqrt(3.0+alfa2);
		m=n-n/2;
		for (i=2; i<=n-1; i++)
			b[i]=Math.sqrt(i*(i+alfa2)/(4.0*(i+alfa)*(i+alfa)-1.0));
		for (i=1; i<=m; i++) {
			xi=Math.abs(x[i]);
			r0=1.0;
			r1=xi/b[1];
			s=1.0+r1*r1;
			for (j=2; j<=n-1; j++) {
				r2=(xi*r1-b[j-1]*r0)/b[j];
				r0=r1;
				r1=r2;
				s += r2*r2;
			}
			w[i]=w[n+1-i]=h0/s;
		}
	} else {
		double a[] = new double[n+1];
		double b[] = new double[n+1];
		alfabeta=alfa+beta;
		min=(beta-alfa)*alfabeta;
		b[0]=0.0;
		sum=alfabeta+2.0;
		a[0]=(beta-alfa)/sum;
		a[1]=min/sum/(sum+2.0);
		b[1]=2.0*Math.sqrt((1.0+alfa)*(1.0+beta)/(sum+1.0))/sum;
		for (i=2; i<=n-1; i++) {
			sum=i+i+alfabeta;
			a[i]=min/sum/(sum+2.0);
			b[i]=(2.0/sum)*Math.sqrt(i*(sum-i)*(i+alfa)*(i+beta)/
			      (sum*sum-1.0));
		}
		h0=Math.pow(2.0,alfabeta+1.0)*Special_functions.gamma(1.0+alfa)*
		   Special_functions.gamma(1.0+beta)/
		            Special_functions.gamma(2.0+alfabeta);
    Linear_algebra.alljaczer(n,alfa,beta,x);
		for (i=1; i<=n; i++) {
			xi=x[i];
			r0=1.0;
			r1=(xi-a[0])/b[1];
			sum=1.0+r1*r1;
			for (j=2; j<=n-1; j++) {
				r2=((xi-a[j-1])*r1-b[j-1]*r0)/b[j];
				sum += r2*r2;
				r0=r1;
				r1=r2;
			}
			w[i]=h0/sum;
		}
	}
}


public static void gsslagwghts(int n, double alfa,
                               double x[], double w[])
{
	int i,j;
	double h0,s,r0,r1,r2,xi;
	double a[] = new double[n+1];
	double b[] = new double[n+1];
	
	a[0]=1.0+alfa;
	a[1]=3.0+alfa;
	b[1]=Math.sqrt(a[0]);
	for (i=2; i<=n-1; i++) {
		a[i]=i+i+alfa+1.0;
		b[i]=Math.sqrt(i*(i+alfa));
	}
  Linear_algebra.alllagzer(n,alfa,x);
	h0=Special_functions.gamma(1.0+alfa);
	for (i=1; i<=n; i++) {
		xi=x[i];
		r0=1.0;
		r1=(xi-a[0])/b[1];
		s=1.0+r1*r1;
		for (j=2; j<=n-1; j++) {
			r2=((xi-a[j-1])*r1-b[j-1]*r0)/b[j];
			r0=r1;
			r1=r2;
			s += r2*r2;
		}
		w[i]=h0/s;
	}
}


public static void jacobnnf(int n, double x[], double f[],
                   double jac[][], AE_jacobnnf_methods method)
{
	int i,j;
	double step,aid;
	double f1[] = new double[n+1];
	
	for (i=1; i<=n; i++) {
		step=method.di(i,n);
		aid=x[i];
		x[i]=aid+step;
		step=1.0/step;
    method.funct(n,x,f1);
		for (j=1; j<=n; j++) jac[j][i]=(f1[j]-f[j])*step;
		x[i]=aid;
	}
}


public static void jacobnmf(int n, int m, double x[], double f[],
                            double jac[][], AE_jacobnmf_methods method)
{
	int i,j;
	double step,aid;
	double f1[] = new double[n+1];
	
	for (i=1; i<=m; i++) {
		step=method.di(i,m);
		aid=x[i];
		x[i]=aid+step;
		step=1.0/step;
		method.funct(n,m,x,f1);
		for (j=1; j<=n; j++) jac[j][i]=(f1[j]-f[j])*step;
		x[i]=aid;
	}
}


public static void jacobnbndf(int n, int lw, int rw, double x[],
       double f[], double jac[], AE_jacobnbndf_methods method)
{
	int i,j,k,l,u,t,b,ll;
	double aid,stepi;

	l=1;
	u=lw+1;
	t=rw+1;
	b=lw+rw;
	for (i=1; i<=n; i++) {
		ll=l;
		double f1[] = new double[u+1];
		stepi=method.di(i,n);
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
}

}