package numal;

import numal.*;

public class Special_functions extends Object {


public static double arcsinh(double x)
{
	double y;

	if (Math.abs(x) > 1.0e10)
		return ((x > 0.0) ? 0.69314718055995+Math.log(Math.abs(x)) :
                       -0.69314718055995+Math.log(Math.abs(x)));
	else {
		y=x*x;
		return ((x == 0.0) ? 0.0 :	((x > 0.0) ?
					 logoneplusx(Math.abs(x)+y/(1.0+Math.sqrt(1.0+y))) :
					-logoneplusx(Math.abs(x)+y/(1.0+Math.sqrt(1.0+y)))));
	}
}


public static double arccosh(double x)
{
	return ((x <= 1.0) ? 0.0 : ((x > 1.0e10) ?
						0.69314718055995+Math.log(x) :
						Math.log(x+Math.sqrt((x-1.0)*(x+1.0)))));
}


public static double arctanh(double x)
{
	double ax;

	if (Math.abs(x) >= 1.0)
		return ((x > 0.0) ? Double.MAX_VALUE : -Double.MAX_VALUE);
	else {
		ax=Math.abs(x);
		return ((x == 0.0) ? 0.0 :	((x > 0.0) ?
					 0.5*logoneplusx(2.0*ax/(1.0-ax)) :
					-0.5*logoneplusx(2.0*ax/(1.0-ax))));
	}
}


public static double logoneplusx(double x)
{
	double y,z;

	if (x == 0.0)
		return 0.0;
	else if (x < -0.2928 || x > 0.4142)
		return Math.log(1.0+x);
	else {
		z=x/(x+2.0);
		y=z*z;
		return z*(2.0+y*(0.666666666663366+y*(0.400000001206045+y*
					(0.285714091590488+y*(0.22223823332791+y*
					(0.1811136267967+y*0.16948212488))))));
	}
}


public static double ei(double x)
{
	double p[] = new double[8];
	double q[] = new double[8];

	if (x > 24.0) {
		p[0]=  1.00000000000058;     q[1] =  1.99999999924131;
		p[1]=x-3.00000016782085;     q[2] = -2.99996432944446;
		p[2]=x-5.00140345515924;     q[3] = -7.90404992298926;
		p[3]=x-7.49289167792884;     q[4] = -4.31325836146628;
		p[4]=x-3.08336269051763e1;   q[5] =  2.95999399486831e2;
		p[5]=x-1.39381360364405;     q[6] = -6.74704580465832;
		p[6]=x+8.91263822573708;     q[7] =  1.04745362652468e3;
		p[7]=x-5.31686623494482e1;
    return Math.exp(x)*(1.0+Algebraic_eval.jfrac(7,q,p)/x)/x;
	} else if (x > 12.0) {
		p[0]=  9.99994296074708e-1;  q[1] =  1.00083867402639;
		p[1]=x-1.95022321289660;     q[2] = -3.43942266899870;
		p[2]=x+1.75656315469614;     q[3] =  2.89516727925135e1;
		p[3]=x+1.79601688769252e1;   q[4] =  7.60761148007735e2;
		p[4]=x-3.23467330305403e1;   q[5] =  2.57776384238440e1;
		p[5]=x-8.28561994140641;     q[6] =  5.72837193837324e1;
		p[6]=x-1.86545454883399e1;   q[7] =  6.95000655887434e1;
		p[7]=x-3.48334653602853;
    return Math.exp(x)*Algebraic_eval.jfrac(7,q,p)/x;
	} else if (x > 6.0) {
		p[0]=  1.00443109228078;     q[1] =  5.27468851962908e-1;
		p[1]=x-4.32531132878135e1;   q[2] =  2.73624119889328e3;
		p[2]=x+6.01217990830080e1;   q[3] =  1.43256738121938e1;
		p[3]=x-3.31842531997221e1;   q[4] =  1.00367439516726e3;
		p[4]=x+2.50762811293561e1;   q[5] = -6.25041161671876;
		p[5]=x+9.30816385662165;     q[6] =  3.00892648372915e2;
		p[6]=x-2.19010233854880e1;   q[7] =  3.93707701852715;
		p[7]=x-2.18086381520724;
    return Math.exp(x)*Algebraic_eval.jfrac(7,q,p)/x;
	} else if (x > 0.0) {
		double t,r,x0,xmx0;
		p[0]= -1.95773036904548e8;   q[0] = -8.26271498626055e7;
		p[1]=  3.89280421311201e6;   q[1] =  8.91925767575612e7;
		p[2]= -2.21744627758845e7;   q[2] = -2.49033375740540e7;
		p[3]= -1.19623669349247e5;   q[3] =  4.28559624611749e6;
		p[4]= -2.49301393458648e5;   q[4] = -4.83547436162164e5;
		p[5]= -4.21001615357070e3;   q[5] =  3.57300298058508e4;
		p[6]= -5.49142265521085e2;   q[6] = -1.60708926587221e3;
		p[7]= -8.66937339951070;     q[7] =  3.41718750000000e1;
		x0=0.372507410781367;
		t=x/3.0-1.0;
    r=Algebraic_eval.chepolsum(7,t,p)/Algebraic_eval.chepolsum(7,t,q);
		xmx0=(x-409576229586.0/1099511627776.0)-0.767177250199394e-12;
		if (Math.abs(xmx0) > 0.037)
			t=Math.log(x/x0);
		else {
			double z,z2;
			p[0] =  0.837207933976075e1;
			p[1] = -0.652268740837103e1;
			p[2] =  0.569955700306720;
			q[0] =  0.418603966988037e1;
			q[1] = -0.465669026080814e1;
			q[2] =  0.1e1;
			z=xmx0/(x+x0);
			z2=z*z;
      t=z*Algebraic_eval.pol(2,z2,p)/Algebraic_eval.pol(2,z2,q);
		}
		return t+xmx0*r;
	} else if (x > -1.0) {
		double y;
		p[0] = -4.41785471728217e4;  q[0]=7.65373323337614e4;
		p[1] =  5.77217247139444e4;  q[1]=3.25971881290275e4;
		p[2] =  9.93831388962037e3;  q[2]=6.10610794245759e3;
		p[3] =  1.84211088668000e3;  q[3]=6.35419418378382e2;
		p[4] =  1.01093806161906e2;  q[4]=3.72298352833327e1;
		p[5] =  5.03416184097568;    q[5]=1.0;
		y = -x;
    return Math.log(y)-Algebraic_eval.pol(5,y,p)/Algebraic_eval.pol(5,y,q);
	} else if (x > -4.0) {
		double y;
		p[0]=8.67745954838444e-8;  q[0]=1.0;
		p[1]=9.99995519301390e-1;  q[1]=1.28481935379157e1;
		p[2]=1.18483105554946e1;   q[2]=5.64433569561803e1;
		p[3]=4.55930644253390e1;   q[3]=1.06645183769914e2;
		p[4]=6.99279451291003e1;   q[4]=8.97311097125290e1;
		p[5]=4.25202034768841e1;   q[5]=3.14971849170441e1;
		p[6]=8.83671808803844;     q[6]=3.79559003762122;
		p[7]=4.01377664940665e-1;  q[7]=9.08804569188869e-2;
		y = -1.0/x;
    return -Math.exp(x)*Algebraic_eval.pol(7,y,p)/Algebraic_eval.pol(5,y,q);
	} else {
		double y;
		p[0] = -9.99999999998447e-1;  q[0]=1.0;
		p[1] = -2.66271060431811e1;   q[1]=2.86271060422192e1;
		p[2] = -2.41055827097015e2;   q[2]=2.92310039388533e2;
		p[3] = -8.95927957772937e2;   q[3]=1.33278537748257e3;
		p[4] = -1.29885688756484e3;   q[4]=2.77761949509163e3;
		p[5] = -5.45374158883133e2;   q[5]=2.40401713225909e3;
		p[6] = -5.66575206533869;     q[6]=6.31657483280800e2;
		y = -1.0/x;
    return -Math.exp(x)*y*(1.0+
            y*Algebraic_eval.pol(6,y,p)/Algebraic_eval.pol(5,y,q));
	}
}


public static void eialpha(double x, int n, double alpha[])
{
	int k;
	double a,b,c;

	c=1.0/x;
	a=Math.exp(-x);
	b=alpha[0]=a*c;
	for (k=1; k<=n; k++) alpha[k]=b=(a+k*b)*c;
}


public static void enx(double x, int n1, int n2, double a[])
{
	if (x <= 1.5) {
    int i;
		double w,e;
    e=0.0;
		w = -ei(-x);
		if (n1 == 1) a[1]=w;
		if (n2 > 1) e=Math.exp(-x);
		for (i=2; i<=n2; i++) {
			w=(e-x*w)/(i-1);
			if (i >= n1) a[i]=w;
		}
	} else {
		int i,n;
		double w,e,an;
		n=(int)Math.ceil(x);
		if (n <= 10) {
			double f,w1,t,h;
			double p[] = new double[20];
			p[2] =0.37534261820491e-1;  p[11]=0.135335283236613;
			p[3] =0.89306465560228e-2;  p[12]=0.497870683678639e-1;
			p[4] =0.24233983686581e-2;  p[13]=0.183156388887342e-1;
			p[5] =0.70576069342458e-3;  p[14]=0.673794699908547e-2;
			p[6] =0.21480277819013e-3;  p[15]=0.247875217666636e-2;
			p[7] =0.67375807781018e-4;  p[16]=0.911881965554516e-3;
			p[8] =0.21600730159975e-4;  p[17]=0.335462627902512e-3;
			p[9] =0.70411579854292e-5;  p[18]=0.123409804086680e-3;
			p[10]=0.23253026570282e-5;  p[19]=0.453999297624848e-4;
			f=w=p[n];
			e=p[n+9];
			w1=t=1.0;
			h=x-n;
			i=n-1;
			do {
				f=(e-i*f)/n;
				t = -h*t/(n-i);
				w1=t*f;
				w += w1;
				i--;
			} while (Math.abs(w1) > 1.0e-15*w);
		} else {
			double b[] = new double[n+1];
			nonexpenx(x,n,n,b);
			w=b[n]*Math.exp(-x);
		}
		if (n1 == n2 && n1 == n)
			a[n]=w;
		else {
			e=Math.exp(-x);
			an=w;
			if (n <= n2 && n >= n1) a[n]=w;
			for (i=n-1; i>=n1; i--) {
				w=(e-i*w)/x;
				if (i <= n2) a[i]=w;
			}
			w=an;
			for (i=n+1; i<=n2; i++) {
				w=(e-x*w)/(i-1);
				if (i >= n1) a[i]=w;
			}
		}
	}
}


public static void nonexpenx(double x, int n1, int n2, double a[])
{
	int i,n;
	double w,an;

	n = (x <= 1.5) ? 1 : (int)Math.ceil(x);
	if (n <= 10) {
		double b[] = new double[n+1];
		enx(x,n,n,b);
		w=b[n]*Math.exp(x);
	} else {
		int k,k1;
		double ue,ve,we,we1,uo,vo,wo,wo1,r,s;
		ue=1.0;
		ve=we=1.0/(x+n);
		we1=0.0;
		uo=1.0;
		vo = -n/(x*(x+n+1.0));
		wo1=1.0/x;
		wo=vo+wo1;
		w=(we+wo)/2.0;
		k1=1;
		k=k1;
		while (wo-we > 1.0e-15*w && we > we1 && wo < wo1) {
			we1=we;
			wo1=wo;
			r=n+k;
			s=r+x+k;
			ue=1.0/(1.0-k*(r-1.0)*ue/((s-2.0)*s));
			uo=1.0/(1.0-k*r*uo/(s*s-1.0));
			ve *= (ue-1.0);
			vo *= (uo-1.0);
			we += ve;
			wo += vo;
			w=(we+wo)/2.0;
			k1++;
			k=k1;
		}
	}
	an=w;
	if (n <= n2 && n >= n1) a[n]=w;
	for (i=n-1; i>=n1; i--) {
		w=(1.0-i*w)/x;
		if (i <= n2) a[i]=w;
	}
	w=an;
	for (i=n+1; i<=n2; i++) {
		w=(1.0-x*w)/(i-1);
		if (i >= n1) a[i]=w;
	}
}


public static void sincosint(double x, double si[], double ci[])
{
	double absx,z,gg;
	double f[] = new double[1];
  double g[] = new double[1];

	absx=Math.abs(x);
	if (absx <= 4.0) {
		double z2;
    double a[] = new double[11];
		a[0] =2.7368706803630e0;   a[1] = -1.1106314107894e0;
		a[2] =1.4176562194666e-1;  a[3] = -1.0252652579174e-2;
		a[4] =4.6494615619880e-4;  a[5] = -1.4361730896642e-5;
		a[6] =3.2093684948229e-7;  a[7] = -5.4251990770162e-9;
		a[8] =7.1776288639895e-11; a[9] = -7.6335493723482e-13;
		a[10]=6.6679958346983e-15;
		z=x/4.0;
		z2=z*z;
		gg=z2+z2-1.0;
    si[0] = z*Algebraic_eval.chepolsum(10,gg,a);
		a[0] =2.9659610400727e0;   a[1] = -9.4297198341830e-1;
		a[2] =8.6110342738169e-2;  a[3] = -4.7776084547139e-3;
		a[4] =1.7529161205146e-4;  a[5] = -4.5448727803752e-6;
		a[6] =8.7515839180060e-8;  a[7] = -1.2998699938109e-9;
		a[8] =1.5338974898831e-11; a[9] = -1.4724256070277e-13;
		a[10]=1.1721420798429e-15;
    ci[0] = 0.577215664901533+Math.log(absx)-
            z2*Algebraic_eval.chepolsum(10,gg,a);
	} else {
		double cx,sx;
		sincosfg(x,f,g);
		cx=Math.cos(x);
		sx=Math.sin(x);
		si[0] = 1.570796326794897;
		if (x < 0.0) si[0] = -si[0];
		si[0] -= f[0]*cx+g[0]*sx;
		ci[0] = f[0]*sx-g[0]*cx;
	}
}


public static void sincosfg(double x, double f[], double g[])
{
	double absx;
	double si[] = new double[1];
	double ci[] = new double[1];

	absx=Math.abs(x);
	if (absx <= 4.0) {
		double cx,sx;
		sincosint(x,si,ci);
		cx=Math.cos(x);
		sx=Math.sin(x);
		si[0] -= 1.570796326794897;
		f[0] = ci[0]*sx-si[0]*cx;
		g[0] = -ci[0]*cx-si[0]*sx;
	} else {
		double a[] = new double[24];
		a[0] =  9.6578828035185e-1;  a[1]  = -4.3060837778597e-2;
		a[2] = -7.3143711748104e-3;  a[3]  =  1.4705235789868e-3;
		a[4] = -9.8657685732702e-5;  a[5]  = -2.2743202204655e-5;
		a[6] =  9.8240257322526e-6;  a[7]  = -1.8973430148713e-6;
		a[8] =  1.0063435941558e-7;  a[9]  =  8.0819364822241e-8;
		a[10]= -3.8976282875288e-8;  a[11] =  1.0335650325497e-8;
		a[12]= -1.4104344875897e-9;  a[13] = -2.5232078399683e-10;
		a[14]=  2.5699831325961e-10; a[15] = -1.0597889253948e-10;
		a[16]=  2.8970031570214e-11; a[17] = -4.1023142563083e-12;
		a[18]= -1.0437693730018e-12; a[19] =  1.0994184520547e-12;
		a[20]= -5.2214239401679e-13; a[21] =  1.7469920787829e-13;
		a[22]= -3.8470012979279e-14;
    f[0] = Algebraic_eval.chepolsum(22,8.0/absx-1.0,a)/x;
		a[0] =  2.2801220638241e-1;  a[1]  = -2.6869727411097e-2;
		a[2] = -3.5107157280958e-3;  a[3]  =  1.2398008635186e-3;
		a[4] = -1.5672945116862e-4;  a[5]  = -1.0664141798094e-5;
		a[6] =  1.1170629343574e-5;  a[7]  = -3.1754011655614e-6;
		a[8] =  4.4317473520398e-7;  a[9]  =  5.5108696874463e-8;
		a[10]= -5.9243078711743e-8;  a[11] =  2.2102573381555e-8;
		a[12]= -5.0256827540623e-9;  a[13] =  3.1519168259424e-10;
		a[14]=  3.6306990848979e-10; a[15] = -2.2974764234591e-10;
		a[16]=  8.5530309424048e-11; a[17] = -2.1183067724443e-11;
		a[18]=  1.7133662645092e-12; a[19] =  1.7238877517248e-12;
		a[20]= -1.2930281366811e-12; a[21] =  5.7472339223731e-13;
		a[22]= -1.8415468268314e-13; a[23] =  3.5937256571434e-14;
    g[0] = 4.0*Algebraic_eval.chepolsum(23,8.0/absx-1.0,a)/absx/absx;
	}
}


public static double recipgamma(double x, double odd[], double even[])
{
	int i;
	double alfa,beta,x2;
	double b[] = new double[13];

	b[1] = -0.283876542276024;   b[2]  = -0.076852840844786;
	b[3] =  0.001706305071096;   b[4]  =  0.001271927136655;
	b[5] =  0.000076309597586;   b[6]  = -0.000004971736704;
	b[7] = -0.000000865920800;   b[8]  = -0.000000033126120;
	b[9] =  0.000000001745136;   b[10] =  0.000000000242310;
	b[11]=  0.000000000009161;   b[12] = -0.000000000000170;
	x2=x*x*8.0;
	alfa = -0.000000000000001;
	beta=0.0;
	for (i=12; i>=2; i -= 2) {
		beta = -(alfa*2.0+beta);
		alfa = -beta*x2-alfa+b[i];
	}
	even[0]=(beta/2.0+alfa)*x2-alfa+0.921870293650453;
	alfa = -0.000000000000034;
	beta=0.0;
	for (i=11; i>=1; i -= 2) {
		beta = -(alfa*2.0+beta);
		alfa = -beta*x2-alfa+b[i];
	}
	odd[0]=(alfa+beta)*2.0;
	return (odd[0])*x+(even[0]);
}


public static double gamma(double x)
{
	boolean inv;
	double y,s,f,g;
	double odd[] = new double[1];
	double even[] = new double[1];

  f=0.0;
	if (x < 0.5) {
		y=x-Math.floor(x/2.0)*2;
		s = Math.PI;
		if (y >= 1.0) {
			s = -s;
			y=2.0-y;
		}
		if (y >= 0.5) y=1.0-y;
		inv=true;
		x=1.0-x;
		f=s/Math.sin(Math.PI*y);
	} else
		inv=false;
	if (x > 22.0)
		g=Math.exp(loggamma(x));
	else {
		s=1.0;
		while (x > 1.5) {
			x=x-1.0;
			s *= x;
		}
		g=s/recipgamma(1.0-x,odd,even);
	}
	return (inv ? f/g : g);
}


public static double loggamma(double x)
{
	int i;
	double r,x2,y,f,u0,u1,u,z;
	double b[] = new double[19];

	if (x > 13.0) {
		r=1.0;
		while (x <= 22.0) {
			r /= x;
			x += 1.0;
		}
		x2 = -1.0/(x*x);
		r=Math.log(r);
		return Math.log(x)*(x-0.5)-x+r+0.918938533204672+
           (((0.595238095238095e-3*x2+0.793650793650794e-3)*x2+
           0.277777777777778e-2)*x2+0.833333333333333e-1)/x;
	} else {
		f=1.0;
		u0=u1=0.0;
		b[1]  = -0.0761141616704358;  b[2]  = 0.0084323249659328;
		b[3]  = -0.0010794937263286;  b[4]  = 0.0001490074800369;
		b[5]  = -0.0000215123998886;  b[6]  = 0.0000031979329861;
		b[7]  = -0.0000004851693012;  b[8]  = 0.0000000747148782;
		b[9]  = -0.0000000116382967;  b[10] = 0.0000000018294004;
		b[11] = -0.0000000002896918;  b[12] = 0.0000000000461570;
		b[13] = -0.0000000000073928;  b[14] = 0.0000000000011894;
		b[15] = -0.0000000000001921;  b[16] = 0.0000000000000311;
		b[17] = -0.0000000000000051;  b[18] = 0.0000000000000008;
		if (x < 1.0) {
			f=1.0/x;
			x += 1.0;
		} else
			while (x > 2.0) {
				x -= 1.0;
				f *= x;
			}
		f=Math.log(f);
		y=x+x-3.0;
		z=y+y;
		for (i=18; i>=1; i--) {
			u=u0;
			u0=z*u0+b[i]-u1;
			u1=u;
		}
		return (u0*y+0.491415393029387-u1)*(x-1.0)*(x-2.0)+f;
	}
}


public static void incomgam(double x, double a, double klgam[],
                            double grgam[], double gam, double eps)
{
	int n;
	double c0,c1,c2,d0,d1,d2,x2,ax,p,q,r,s,r1,r2,scf;

	s=Math.exp(-x+a*Math.log(x));
	scf=Double.MAX_VALUE;
	if (x <= ((a < 3.0) ? 1.0 : a)) {
		x2=x*x;
		ax=a*x;
		d0=1.0;
		p=a;
		c0=s;
		d1=(a+1.0)*(a+2.0-x);
		c1=((a+1.0)*(a+2.0)+x)*s;
		r2=c1/d1;
		n=1;
		do {
			p += 2.0;
			q=(p+1.0)*(p*(p+2.0)-ax);
			r=n*(n+a)*(p+2.0)*x2;
			c2=(q*c1+r*c0)/p;
			d2=(q*d1+r*d0)/p;
			r1=r2;
			r2=c2/d2;
			c0=c1;
			c1=c2;
			d0=d1;
			d1=d2;
			if (Math.abs(c1) > scf || Math.abs(d1) > scf) {
				c0 /= scf;
				c1 /= scf;
				d0 /= scf;
				d1 /= scf;
			}
			n++;
		} while (Math.abs((r2-r1)/r2) > eps);
		klgam[0] = r2/a;
		grgam[0] = gam-klgam[0];
	} else {
		c0=a*s;
		c1=(1.0+x)*c0;
		q=x+2.0-a;
		d0=x;
		d1=x*q;
		r2=c1/d1;
		n=1;
		do {
			q += 2.0;
			r=n*(n+1-a);
			c2=q*c1-r*c0;
			d2=q*d1-r*d0;
			r1=r2;
			r2=c2/d2;
			c0=c1;
			c1=c2;
			d0=d1;
			d1=d2;
			if (Math.abs(c1) > scf || Math.abs(d1) > scf) {
				c0 /= scf;
				c1 /= scf;
				d0 /= scf;
				d1 /= scf;
			}
			n++;
		} while (Math.abs((r2-r1)/r2) > eps);
		grgam[0] = r2/a;
		klgam[0] = gam-grgam[0];
	}
}


public static double incbeta(double x, double p, double q, double eps)
{
	int m,n;
	boolean neven,recur;
	double g,f,fn,fn1,fn2,gn,gn1,gn2,dn,pq;

	if (x == 0.0 || x == 1.0)
		return x;
	else {
		if (x > 0.5) {
			f=p;
			p=q;
			q=f;
			x=1.0-x;
			recur=true;
		} else
			recur=false;
		g=fn2=0.0;
		m=0;
		pq=p+q;
		f=fn1=gn1=gn2=1.0;
		neven=false;
		n=1;
		do {
			if (neven) {
				m++;
				dn=m*x*(q-m)/(p+n-1.0)/(p+n);
			} else
				dn = -x*(p+m)*(pq+m)/(p+n-1.0)/(p+n);
			g=f;
			fn=fn1+dn*fn2;
			gn=gn1+dn*gn2;
			neven=(!neven);
			f=fn/gn;
			fn2=fn1;
			fn1=fn;
			gn2=gn1;
			gn1=gn;
			n++;
		} while (Math.abs((f-g)/f) > eps);
		f=f*Math.pow(x,p)*Math.pow(1.0-x,q)*gamma(p+q)/gamma(p+1.0)/gamma(q);
		if (recur) f=1.0-f;
		return f;
	}
}


public static void ibpplusn(double x, double p, double q, int nmax,
                            double eps, double i[])
{
	int n;

	if (x == 0.0 || x == 1.0)
		for (n=0; n<=nmax; n++) i[n]=x;
	else {
		if (x <= 0.5)
			ixqfix(x,p,q,nmax,eps,i);
		else {
			ixpfix(1.0-x,q,p,nmax,eps,i);
			for (n=0; n<=nmax; n++) i[n]=1.0-i[n];
		}
	}
}


public static void ibqplusn(double x, double p, double q, int nmax,
                            double eps, double i[])
{
	int n;

	if (x == 0.0 || x == 1.0)
		for (n=0; n<=nmax; n++) i[n]=x;
	else {
		if (x <= 0.5)
			ixpfix(x,p,q,nmax,eps,i);
		else {
			ixqfix(1.0-x,q,p,nmax,eps,i);
			for (n=0; n<=nmax; n++) i[n]=1.0-i[n];
		}
	}
}


public static void ixqfix(double x, double p, double q, int nmax,
                          double eps, double i[])
{
	int m,mmax;
	double s,iq0,iq1,q0;

  iq1=0.0;
	m=(int)Math.floor(q);
	s=q-m;
	q0 = (s > 0.0) ? s : s+1.0;
	mmax = (s > 0.0) ? m : m-1;
	iq0=incbeta(x,p,q0,eps);
	if (mmax > 0) iq1=incbeta(x,p,q0+1.0,eps);
	double iq[] = new double[mmax+1];
	forward(x,p,q0,iq0,iq1,mmax,iq);
	backward(x,p,q,iq[mmax],nmax,eps,i);
}


public static void ixpfix(double x, double p, double q, int nmax,
                          double eps, double i[])
{
	int m,mmax;
	double s,p0,i0,i1,iq0,iq1;

	m=(int)Math.floor(p);
	s=p-m;
	p0 = (s > 0.0) ? s : s+1.0;
	mmax = (s > 0.0) ? m : m-1;
	i0=incbeta(x,p0,q,eps);
	i1=incbeta(x,p0,q+1.0,eps);
	double ip[] = new double[mmax+1];
	backward(x,p0,q,i0,mmax,eps,ip);
	iq0=ip[mmax];
	backward(x,p0,q+1.0,i1,mmax,eps,ip);
	iq1=ip[mmax];
	forward(x,p,q,iq0,iq1,nmax,i);
}


public static void forward(double x, double p, double q, double i0,
                           double i1, int nmax, double i[])
{
	int m,n;
	double y,r,s;

	i[0]=i0;
	if (nmax > 0) i[1]=i1;
	m=nmax-1;
	r=p+q-1.0;
	y=1.0-x;
	for (n=1; n<=m; n++) {
		s=(n+r)*y;
		i[n+1]=((n+q+s)*i[n]-s*i[n-1])/(n+q);
	}
}


public static void backward(double x, double p, double q, double i0,
                            int nmax, double eps, double i[])
{
	int m,n,nu;
	boolean finish;
	double r,pq,y,logx;
	double iapprox[] = new double[nmax+1];
	
	i[0]=i0;
	if (nmax > 0) {
		for (n=1; n<=nmax; n++) iapprox[n]=0.0;
		pq=p+q-1.0;
		logx=Math.log(x);
		r=nmax+(Math.log(eps)+q*Math.log(nmax))/logx;
		nu=(int)Math.floor(r-q*Math.log(r)/logx);
		while (true) {
			n=nu;
			r=x;
			while (true) {
				y=(n+pq)*x;
				r=y/(y+(n+p)*(1.0-r));
				if (n <= nmax) i[n]=r;
				n--;
				if (n < 1) break;
			}
			r=i0;
			for (n=1; n<=nmax; n++) r = i[n] *= r;
			finish=true;
			for (n=1; n<=nmax; n++)
				if (Math.abs((i[n]-iapprox[n])/i[n]) > eps) {
					for (m=1; m<=nmax; m++) iapprox[m]=i[m];
					nu += 5;
					finish=false;
					break;
				}
			if (finish) break;
		}
	}
}


public static void errorfunction(double x, double erf[], double erfc[])
{
	if (x > 26.0) {
		erf[0] = 1.0;
		erfc[0] = 0.0;
		return;
	} else if (x < -5.5) {
		erf[0] = -1.0;
		erfc[0] = 2.0;
		return;
	} else {
		double absx,c,p,q;
		absx=Math.abs(x);
		if (absx <= 0.5) {
			c=x*x;
			p=((-0.356098437018154e-1*c+0.699638348861914e1)*c+
					0.219792616182942e2)*c+0.242667955230532e3;
			q=((c+0.150827976304078e2)*c+0.911649054045149e2)*c+
					0.215058875869861e3;
			erf[0] = x*p/q;
			erfc[0] = 1.0-erf[0];
		} else {
			erfc[0] = Math.exp(-x*x)*nonexperfc(absx);
			erf[0] = 1.0-erfc[0];
			if (x < 0.0) {
				erf[0] = -erf[0];
				erfc[0] = 2.0-erfc[0];
			}
		}
	}
}


public static double nonexperfc(double x)
{
	double absx,c,p,q;
  double erf[] = new double[1];
  double erfc[] = new double[1];

	absx=Math.abs(x);
	if (absx <= 0.5) {
		errorfunction(x,erf,erfc);
		return Math.exp(x*x)*erfc[0];
	} else if (absx < 4.0) {
		c=absx;
		p=((((((-0.136864857382717e-6*c+0.564195517478974e0)*c+
				0.721175825088309e1)*c+0.431622272220567e2)*c+
				0.152989285046940e3)*c+0.339320816734344e3)*c+
				0.451918953711873e3)*c+0.300459261020162e3;
		q=((((((c+0.127827273196294e2)*c+0.770001529352295e2)*c+
				0.277585444743988e3)*c+0.638980264465631e3)*c+
				0.931354094850610e3)*c+0.790950925327898e3)*c+
				0.300459260956983e3;
		return ((x > 0.0) ? p/q : Math.exp(x*x)*2.0-p/q);
	} else {
		c=1.0/x/x;
		p=(((0.223192459734185e-1*c+0.278661308609648e0)*c+
				0.226956593539687e0)*c+0.494730910623251e-1)*c+
				0.299610707703542e-2;
		q=(((c+0.198733201817135e1)*c+0.105167510706793e1)*c+
				0.191308926107830e0)*c+0.106209230528468e-1;
		c=(c*(-p)/q+0.564189583547756)/absx;
		return ((x > 0.0) ? c : Math.exp(x*x)*2.0-c);
	}
}


public static void inverseerrorfunction(double x, double oneminx,
                                        double inverf[])
{
	double absx,p,betax;
	double a[] = new double[24];

	absx=Math.abs(x);
	if (absx > 0.8 && oneminx > 0.2) oneminx=0.0;
	if (absx <= 0.8) {
		a[0]  = 0.992885376618941;  a[1]  = 0.120467516143104;
		a[2]  = 0.016078199342100;  a[3]  = 0.002686704437162;
		a[4]  = 0.000499634730236;  a[5]  = 0.000098898218599;
		a[6]  = 0.000020391812764;  a[7]  = 0.000004327271618;
		a[8]  = 0.000000938081413;  a[9]  = 0.000000206734720;
		a[10] = 0.000000046159699;  a[11] = 0.000000010416680;
		a[12] = 0.000000002371501;  a[13] = 0.000000000543928;
		a[14] = 0.000000000125549;  a[15] = 0.000000000029138;
		a[16] = 0.000000000006795;  a[17] = 0.000000000001591;
		a[18] = 0.000000000000374;  a[19] = 0.000000000000088;
		a[20] = 0.000000000000021;  a[21] = 0.000000000000005;
    inverf[0] = Algebraic_eval.chepolsum(21,x*x/0.32-1.0,a)*x;
	} else if (oneminx >= 25.0e-4) {
		a[0]  =  0.912158803417554;  a[1]  = -0.016266281867664;
		a[2]  =  0.000433556472949;  a[3]  =  0.000214438570074;
		a[4]  =  0.000002625751076;  a[5]  = -0.000003021091050;
		a[6]  = -0.000000012406062;  a[7]  =  0.000000062406609;
		a[8]  = -0.000000000540125;  a[9]  = -0.000000001423208;
		a[10] =  0.000000000034384;  a[11] =  0.000000000033584;
		a[12] = -0.000000000001458;  a[13] = -0.000000000000810;
		a[14] =  0.000000000000053;  a[15] =  0.000000000000020;
		betax=Math.sqrt(-Math.log((1.0+absx)*oneminx));
		p = -1.54881304237326*betax+2.56549012314782;
    p=Algebraic_eval.chepolsum(15,p,a);
		inverf[0] = (x < 0.0) ? -betax*p : betax*p;
	} else if (oneminx >= 5.0e-16) {
		a[0]  =  0.956679709020493;  a[1]  = -0.023107004309065;
		a[2]  = -0.004374236097508;  a[3]  = -0.000576503422651;
		a[4]  = -0.000010961022307;  a[5]  =  0.000025108547025;
		a[6]  =  0.000010562336068;  a[7]  =  0.000002754412330;
		a[8]  =  0.000000432484498;  a[9]  = -0.000000020530337;
		a[10] = -0.000000043891537;  a[11] = -0.000000017684010;
		a[12] = -0.000000003991289;  a[13] = -0.000000000186932;
		a[14] =  0.000000000272923;  a[15] =  0.000000000132817;
		a[16] =  0.000000000031834;  a[17] =  0.000000000001670;
		a[18] = -0.000000000002036;  a[19] = -0.000000000000965;
		a[20] = -0.000000000000220;  a[21] = -0.000000000000010;
		a[22] =  0.000000000000014;  a[23] =  0.000000000000006;
		betax=Math.sqrt(-Math.log((1.0+absx)*oneminx));
		p = -0.559457631329832*betax+2.28791571626336;
    p=Algebraic_eval.chepolsum(23,p,a);
		inverf[0] = (x < 0.0) ? -betax*p : betax*p;
	} else if (oneminx >= Double.MIN_VALUE) {
		a[0]  =  0.988575064066189;  a[1]  =  0.010857705184599;
		a[2]  = -0.001751165102763;  a[3]  =  0.000021196993207;
		a[4]  =  0.000015664871404;  a[5]  = -0.000000519041687;
		a[6]  = -0.000000037135790;  a[7]  =  0.000000001217431;
		a[8]  = -0.000000000176812;  a[9]  = -0.000000000011937;
		a[10] =  0.000000000000380;  a[11] = -0.000000000000066;
		a[12] = -0.000000000000009;
		betax=Math.sqrt(-Math.log((1.0+absx)*oneminx));
		p = -9.19999235883015/Math.sqrt(betax)+2.79499082012460;
    p=Algebraic_eval.chepolsum(12,p,a);
		inverf[0] = (x < 0.0) ? -betax*p : betax*p;
	} else
		inverf[0] = (x > 0.0) ? 26.0 : -26.0;
}


public static void fresnel(double x, double c[], double s[])
{
	double absx,x3,x4,a,p,q,c1,s1;
	double f[] = new double[1];
  double g[] = new double[1];

	absx=Math.abs(x);
	if (absx <= 1.2) {
		a=x*x;
		x3=a*x;
		x4=a*a;
		p=(((5.47711385682687e-6*x4-5.28079651372623e-4)*x4+
				1.76193952543491e-2)*x4-1.99460898826184e-1)*x4+1.0;
		q=(((1.18938901422876e-7*x4+1.55237885276994e-5)*x4+
				1.09957215025642e-3)*x4+4.72792112010453e-2)*x4+1.0;
		c[0] = x*p/q;
		p=(((6.71748466625141e-7*x4-8.45557284352777e-5)*x4+
				3.87782123463683e-3)*x4-7.07489915144523e-2)*x4+
				5.23598775598299e-1;
		q=(((5.95281227678410e-8*x4+9.62690875939034e-6)*x4+
				8.17091942152134e-4)*x4+4.11223151142384e-2)*x4+1.0;
		s[0] = x3*p/q;
	} else if (absx <= 1.6) {
		a=x*x;
		x3=a*x;
		x4=a*a;
		p=((((-5.68293310121871e-8*x4+1.02365435056106e-5)*x4-
				6.71376034694922e-4)*x4+1.91870279431747e-2)*x4-
				2.07073360335324e-1)*x4+1.00000000000111e0;
		q=((((4.41701374065010e-10*x4+8.77945377892369e-8)*x4+
				1.01344630866749e-5)*x4+7.88905245052360e-4)*x4+
				3.96667496952323e-2)*x4+1.0;
		c[0] = x*p/q;
		p=((((-5.76765815593089e-9*x4+1.28531043742725e-6)*x4-
				1.09540023911435e-4)*x4+4.30730526504367e-3)*x4-
				7.37766914010191e-2)*x4+5.23598775598344e-1;
		q=((((2.05539124458580e-10*x4+5.03090581246612e-8)*x4+
				6.87086265718620e-6)*x4+6.18224620195473e-4)*x4+
				3.53398342767472e-2)*x4+1.0;
		s[0] = x3*p/q;
	} else if (absx < 1.0e15) {
		fg(x,f,g);
		a=x*x;
		a=(a-Math.floor(a/4.0)*4.0)*1.57079632679490;
		c1=Math.cos(a);
		s1=Math.sin(a);
		a = (x < 0.0) ? -0.5 : 0.5;
		c[0] = f[0]*s1-g[0]*c1+a;
		s[0] = -f[0]*c1-g[0]*s1+a;
	} else
		c[0] = s[0] = ((x > 0.0) ? 0.5 : -0.5);
}


public static void fg(double x, double f[], double g[])
{
	double absx,c1,s1,a,xinv,x3inv,c4,p,q;
	double c[] = new double[1];
  double s[] = new double[1];

	absx=Math.abs(x);
	if (absx <= 1.6) {
		fresnel(x,c,s);
		a=x*x*1.57079632679490;
		c1=Math.cos(a);
		s1=Math.sin(a);
		a = (x < 0.0) ? -0.5 : 0.5;
		p=a-c[0];
		q=a-s[0];
		f[0] = q*c1-p*s1;
		g[0] = p*c1+q*s1;
	} else if (absx <= 1.9) {
		xinv=1.0/x;
		a=xinv*xinv;
		x3inv=a*xinv;
		c4=a*a;
		p=(((1.35304235540388e1*c4+6.98534261601021e1)*c4+
				4.80340655577925e1)*c4+8.03588122803942e0)*c4+
				3.18309268504906e-1;
		q=(((6.55630640083916e1*c4+2.49561993805172e2)*c4+
				1.57611005580123e2)*c4+2.55491618435795e1)*c4+1.0;
		f[0] = xinv*p/q;
		p=((((2.05421432498501e1*c4+1.96232037971663e2)*c4+
				1.99182818678903e2)*c4+5.31122813480989e1)*c4+
				4.44533827550512e0)*c4+1.01320618810275e-1;
		q=((((1.01379483396003e3*c4+3.48112147856545e3)*c4+
				2.54473133181822e3)*c4+5.83590575716429e2)*c4+
				4.53925019673689e1)*c4+1.0;
		g[0] = x3inv*p/q;
	} else if (absx <= 2.4) {
		xinv=1.0/x;
		a=xinv*xinv;
		x3inv=a*xinv;
		c4=a*a;
		p=((((7.17703249365140e2*c4+3.09145161574430e3)*c4+
				1.93007640786716e3)*c4+3.39837134926984e2)*c4+
				1.95883941021969e1)*c4+3.18309881822017e-1;
		q=((((3.36121699180551e3*c4+1.09334248988809e4)*c4+
				6.33747155851144e3)*c4+1.08535067500650e3)*c4+
				6.18427138172887e1)*c4+1.0;
		f[0] = xinv*p/q;
		p=((((3.13330163068756e2*c4+1.59268006085354e3)*c4+
				9.08311749529594e2)*c4+1.40959617911316e2)*c4+
				7.11205001789783e0)*c4+1.01321161761805e-1;
		q=((((1.15149832376261e4*c4+2.41315567213370e4)*c4+
				1.06729678030581e4)*c4+1.49051922797329e3)*c4+
				7.17128596939302e1)*c4+1.0;
		g[0] = x3inv*p/q;
	} else {
		xinv=1.0/x;
		a=xinv*xinv;
		x3inv=a*xinv;
		c4=a*a;
		p=((((2.61294753225142e4*c4+6.13547113614700e4)*c4+
				1.34922028171857e4)*c4+8.16343401784375e2)*c4+
				1.64797712841246e1)*c4+9.67546032967090e-2;
		q=((((1.37012364817226e6*c4+1.00105478900791e6)*c4+
				1.65946462621853e5)*c4+9.01827596231524e3)*c4+
				1.73871690673649e2)*c4+1.0;
		f[0] = (c4*(-p)/q+0.318309886183791)*xinv;
		p=(((((1.72590224654837e6*c4+6.66907061668636e6)*c4+
				1.77758950838030e6)*c4+1.35678867813756e5)*c4+
				3.87754141746378e3)*c4+4.31710157823358e1)*c4+
				1.53989733819769e-1;
		q=(((((1.40622441123580e8*c4+9.38695862531635e7)*c4+
				1.62095600500232e7)*c4+1.02878693056688e6)*c4+
				2.69183180396243e4)*c4+2.86733194975899e2)*c4+1.0;
		g[0] = (c4*(-p)/q+0.101321183642338)*x3inv;
	}
}


public static double bessj0(double x)
{
	if (x == 0.0) return 1.0;
	if (Math.abs(x) < 8.0) {
		int i;
		double z,z2,b0,b1,b2;
		double ar[]={-0.75885e-15, 0.4125321e-13,
			-0.194383469e-11, 0.7848696314e-10, -0.267925353056e-8,
			0.7608163592419e-7, -0.176194690776215e-5,
			0.324603288210051e-4, -0.46062616620628e-3,
			0.48191800694676e-2, -0.34893769411409e-1,
			0.158067102332097, -0.37009499387265, 0.265178613203337,
			-0.872344235285222e-2};
		x /= 8.0;
		z=2.0*x*x-1.0;
		z2=z+z;
		b1=b2=0.0;
		for (i=0; i<=14; i++) {
			b0=z2*b1-b2+ar[i];
			b2=b1;
			b1=b0;
		}
		return z*b1-b2+0.15772797147489;
	} else {
		double c,cosx,sinx;
		double p0[] = new double[1];
		double q0[] = new double[1];
		x=Math.abs(x);
		c=0.797884560802865/Math.sqrt(x);
		cosx=Math.cos(x-0.706858347057703e1);
		sinx=Math.sin(x-0.706858347057703e1);
		besspq0(x,p0,q0);
		return c*(p0[0]*cosx-q0[0]*sinx);
	}
}


public static double bessj1(double x)
{
	if (x == 0.0) return 1.0;
	if (Math.abs(x) < 8.0) {
		int i;
		double z,z2,b0,b1,b2;
		double ar[]={-0.19554e-15, 0.1138572e-13,
			-0.57774042e-12, 0.2528123664e-10, -0.94242129816e-9,
			0.2949707007278e-7, -0.76175878054003e-6,
			0.158870192399321e-4, -0.260444389348581e-3,
			0.324027018268386e-2, -0.291755248061542e-1,
			0.177709117239728e0, -0.661443934134543e0,
			0.128799409885768e1, -0.119180116054122e1};
		x /= 8.0;
		z=2.0*x*x-1.0;
		z2=z+z;
		b1=b2=0.0;
		for (i=0; i<=14; i++) {
			b0=z2*b1-b2+ar[i];
			b2=b1;
			b1=b0;
		}
		return x*(z*b1-b2+0.648358770605265);
	} else {
		int sgnx;
		double c,cosx,sinx;
    double p1[] = new double[1];
    double q1[] = new double[1];
		sgnx = (x > 0.0) ? 1 : -1;
		x=Math.abs(x);
		c=0.797884560802865/Math.sqrt(x);
		cosx=Math.cos(x-0.706858347057703e1);
		sinx=Math.sin(x-0.706858347057703e1);
		besspq1(x,p1,q1);
		return sgnx*c*(p1[0]*sinx+q1[0]*cosx);
	}
}


public static void bessj(double x, int n, double j[])
{
	if (x == 0.0) {
		j[0]=1.0;
		for (; n>=1; n--) j[n]=0.0;
	} else {
		int l,m,nu,signx;
		double x2,r,s;
		signx = (x > 0.0) ? 1 : -1;
		x=Math.abs(x);
		r=s=0.0;
		x2=2.0/x;
		l=0;
		nu=start(x,n,0);
		for (m=nu; m>=1; m--) {
			r=1.0/(x2*m-r);
			l=2-l;
			s=r*(l+s);
			if (m <= n) j[m]=r;
		}
		j[0]=r=1.0/(1.0+s);
		for (m=1; m<=n; m++) r = j[m] *= r;
		if (signx < 0.0)
			for (m=1; m<=n; m += 2) j[m] = -j[m];
	}
}


public static void bessy01(double x, double y0[], double y1[])
{
	if (x < 8.0) {
		int i;
		double z,z2,c,lnx,b0,b1,b2;
		double ar1[]={0.164349e-14, -0.8747341e-13,
			0.402633082e-11, -0.15837552542e-9, 0.524879478733e-8,
			-0.14407233274019e-6, 0.32065325376548e-5,
			-0.563207914105699e-4, 0.753113593257774e-3,
			-0.72879624795521e-2, 0.471966895957634e-1,
			-0.177302012781143, 0.261567346255047,
			0.179034314077182, -0.274474305529745};
		double ar2[]={0.42773e-15, -0.2440949e-13,
			0.121143321e-11, -0.5172121473e-10, 0.187547032473e-8,
			-0.5688440039919e-7, 0.141662436449235e-5,
			-0.283046401495148e-4, 0.440478629867099e-3,
			-0.51316411610611e-2, 0.423191803533369e-1,
			-0.226624991556755, 0.675615780772188,
			-0.767296362886646, -0.128697384381350};
		c=0.636619772367581;
		lnx=c*Math.log(x);
		c /= x;
		x /= 8.0;
		z=2.0*x*x-1.0;
		z2=z+z;
		b1=b2=0.0;
		for (i=0; i<=14; i++) {
			b0=z2*b1-b2+ar1[i];
			b2=b1;
			b1=b0;
		}
		y0[0] = lnx*bessj0(8.0*x)+z*b1-b2-0.33146113203285e-1;
		b1=b2=0.0;
		for (i=0; i<=14; i++) {
			b0=z2*b1-b2+ar2[i];
			b2=b1;
			b1=b0;
		}
		y1[0] = lnx*bessj1(8.0*x)-c+x*(z*b1-b2+0.2030410588593425e-1);
	} else {
		double c,cosx,sinx;
    double p0[] = new double[1];
    double q0[] = new double[1];
    double p1[] = new double[1];
    double q1[] = new double[1];
		c=0.797884560802865/Math.sqrt(x);
		besspq0(x,p0,q0);
		besspq1(x,p1,q1);
		x -= 0.706858347057703e1;
		cosx=Math.cos(x);
		sinx=Math.sin(x);
		y0[0] = c*(p0[0]*sinx+q0[0]*cosx);
		y1[0] = c*(q1[0]*sinx-p1[0]*cosx);
	}
}


public static void bessy(double x, int n, double y[])
{
	int i;
	double y0,y1,y2;
	double tmp1[] = new double[1];
  double tmp2[] = new double[1];

	bessy01(x,tmp1,tmp2);
  y0 = tmp1[0];
  y1 = tmp2[0];
	y[0]=y0;
	if (n > 0) y[1]=y1;
	x=2.0/x;
	for (i=2; i<=n; i++) {
		y[i]=y2=(i-1)*x*y1-y0;
		y0=y1;
		y1=y2;
	}
}


public static void besspq0(double x, double p0[], double q0[])
{
	if (x < 8.0) {
		double b,cosx,sinx;
		double j0x[] = new double[1];
		double y0[] = new double[1];
		b=Math.sqrt(x)*1.25331413731550;
		bessy01(x,y0,j0x);
		j0x[0]=bessj0(x);
		x -= 0.785398163397448;
		cosx=Math.cos(x);
		sinx=Math.sin(x);
		p0[0] = b*(y0[0]*sinx+j0x[0]*cosx);
		q0[0] = b*(y0[0]*cosx-j0x[0]*sinx);
	} else {
		int i;
		double x2,b0,b1,b2,y;
		double ar1[]={-0.10012e-15, 0.67481e-15, -0.506903e-14,
			0.4326596e-13, -0.43045789e-12, 0.516826239e-11,
			-0.7864091377e-10, 0.163064646352e-8, -0.5170594537606e-7,
			0.30751847875195e-5, -0.536522046813212e-3};
		double ar2[]={-0.60999e-15, 0.425523e-14,
			-0.3336328e-13, 0.30061451e-12, -0.320674742e-11,
			0.4220121905e-10, -0.72719159369e-9, 0.1797245724797e-7,
			-0.74144984110606e-6, 0.683851994261165e-4};
		y=8.0/x;
		x=2.0*y*y-1.0;
		x2=x+x;
		b1=b2=0.0;
		for (i=0; i<=10; i++) {
			b0=x2*b1-b2+ar1[i];
			b2=b1;
			b1=b0;
		}
		p0[0] = x*b1-b2+0.99946034934752;
		b1=b2=0.0;
		for (i=0; i<=9; i++) {
			b0=x2*b1-b2+ar2[i];
			b2=b1;
			b1=b0;
		}
		q0[0] = (x*b1-b2-0.015555854605337)*y;
	}
}


public static void besspq1(double x, double p1[], double q1[])
{
	if (x < 8.0) {
		double b,cosx,sinx;
    double j1x[] = new double[1];
    double y1[] = new double[1];
		b=Math.sqrt(x)*1.25331413731550;
		bessy01(x,j1x,y1);
		j1x[0]=bessj1(x);
		x -= 0.785398163397448;
		cosx=Math.cos(x);
		sinx=Math.sin(x);
		p1[0] = b*(j1x[0]*sinx-y1[0]*cosx);
		q1[0] = b*(j1x[0]*cosx+y1[0]*sinx);
	} else {
		int i;
		double x2,b0,b1,b2,y;
		double ar1[]={0.10668e-15, -0.72212e-15, 0.545267e-14,
			-0.4684224e-13, 0.46991955e-12, -0.570486364e-11,
			0.881689866e-10, -0.187189074911e-8, 0.6177633960644e-7,
			-0.39872843004889e-5, 0.89898983308594e-3};
		double ar2[]={-0.10269e-15, 0.65083e-15, -0.456125e-14,
			0.3596777e-13, -0.32643157e-12, 0.351521879e-11,
			-0.4686363688e-10, 0.82291933277e-9, -0.2095978138408e-7,
			0.91386152579555e-6, -0.96277235491571e-4};
		y=8.0/x;
		x=2.0*y*y-1.0;
		x2=x+x;
		b1=b2=0.0;
		for (i=0; i<=10; i++) {
			b0=x2*b1-b2+ar1[i];
			b2=b1;
			b1=b0;
		}
		p1[0] = x*b1-b2+1.0009030408600137;
		b1=b2=0.0;
		for (i=0; i<=10; i++) {
			b0=x2*b1-b2+ar2[i];
			b2=b1;
			b1=b0;
		}
		q1[0] = (x*b1-b2+0.46777787069535e-1)*y;
	}
}


public static double bessi0(double x)
{
	if (x == 0.0) return 1.0;
	if (Math.abs(x) <= 15.0) {
		double z,denominator,numerator;
		z=x*x;
		numerator=(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*
			0.210580722890567e-22+0.380715242345326e-19)+
			0.479440257548300e-16)+0.435125971262668e-13)+
			0.300931127112960e-10)+0.160224679395361e-7)+
			0.654858370096785e-5)+0.202591084143397e-2)+
			0.463076284721000e0)+0.754337328948189e2)+
			0.830792541809429e4)+0.571661130563785e6)+
			0.216415572361227e8)+0.356644482244025e9)+
			0.144048298227235e10);
		denominator=(z*(z*(z-0.307646912682801e4)+
			0.347626332405882e7)-0.144048298227235e10);
		return -numerator/denominator;
	} else {
		return Math.exp(Math.abs(x))*nonexpbessi0(x);
	}
}


public static double bessi1(double x)
{
	if (x == 0.0) return 0.0;
	if (Math.abs(x) <= 15.0) {
		double z,denominator,numerator;
		z=x*x;
		denominator=z*(z-0.222583674000860e4)+0.136293593052499e7;
		numerator=(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*(z*
			0.207175767232792e-26+0.257091905584414e-23)+
			0.306279283656135e-20)+0.261372772158124e-17)+
			0.178469361410091e-14)+0.963628891518450e-12)+
			0.410068906847159e-9)+0.135455228841096e-6)+
			0.339472890308516e-4)+0.624726195127003e-2)+
			0.806144878821295e0)+0.682100567980207e2)+
			0.341069752284422e4)+0.840705772877836e5)+
			0.681467965262502e6);
		return x*(numerator/denominator);
	} else {
		return Math.exp(Math.abs(x))*nonexpbessi1(x);
	}
}


public static void bessi(double x, int n, double i[])
{
	if (x == 0.0) {
		i[0]=1.0;
		for (; n>=1; n--) i[n]=0.0;
	} else {
		double expx;
		expx=Math.exp(Math.abs(x));
		nonexpbessi(x,n,i);
		for (; n>=0; n--) i[n] *= expx;
	}
}


public static void bessk01(double x, double k0[], double k1[])
{
	if (x <= 1.5) {
		int k;
		double c,d,r,sum0,sum1,t,term,t0,t1;
		sum0=d=Math.log(2.0/x)-0.5772156649015328606;
		sum1 = c = -1.0-2.0*d;
		r=term=1.0;
		t=x*x/4.0;
		k=1;
		do {
			term *= t*r*r;
			d += r;
			c -= r;
			r=1.0/(k+1);
			c -= r;
			t0=term*d;
			t1=term*c*r;
			sum0 += t0;
			sum1 += t1;
			k++;
		} while (Math.abs(t0/sum0)+Math.abs(t1/sum1) > 1.0e-15);
		k0[0] = sum0;
		k1[0] = (1.0+t*sum1)/x;
	} else {
		double expx;
		expx=Math.exp(-x);
		nonexpbessk01(x,k0,k1);
		k1[0] *= expx;
		k0[0] *= expx;
	}
}


public static void bessk(double x, int n, double k[])
{
	int i;
	double k0,k1,k2;
	double tmp1[] = new double[1];
  double tmp2[] = new double[1];

	bessk01(x,tmp1,tmp2);
  k0 = tmp1[0];
  k1 = tmp2[0];
	k[0]=k0;
	if (n > 0) k[1]=k1;
	x=2.0/x;
	for (i=2; i<=n; i++) {
		k[i]=k2=k0+x*(i-1)*k1;
		k0=k1;
		k1=k2;
	}
}


public static double nonexpbessi0(double x)
{
	if (x == 0.0) return 1.0;
	if (Math.abs(x) <= 15.0) {
		return Math.exp(-Math.abs(x))*bessi0(x);
	} else {
		int i;
		double sqrtx,br,br1,br2,z,z2,numerator,denominator;
		double ar1[]={0.2439260769778, -0.115591978104435e3,
			0.784034249005088e4, -0.143464631313583e6};
		double ar2[]={1.0, -0.325197333369824e3,
			0.203128436100794e5, -0.361847779219653e6};
		x=Math.abs(x);
		sqrtx=Math.sqrt(x);
		br1=br2=0.0;
		z=30.0/x-1.0;
		z2=z+z;
		for (i=0; i<=3; i++) {
			br=z2*br1-br2+ar1[i];
			br2=br1;
			br1=br;
		}
		numerator=z*br1-br2+0.346519833357379e6;
		br1=br2=0.0;
		for (i=0; i<=3; i++) {
			br=z2*br1-br2+ar2[i];
			br2=br1;
			br1=br;
		}
		denominator=z*br1-br2+0.865665274832055e6;
		return (numerator/denominator)/sqrtx;
	}
}


public static double nonexpbessi1(double x)
{
	if (x == 0.0) return 0.0;
	if (Math.abs(x) > 15.0) {
		int i,signx;
		double br,br1,br2,z,z2,sqrtx,numerator,denominator;
		double ar1[]={0.1494052814740e1, -0.362026420242263e3,
			0.220549722260336e5, -0.408928084944275e6};
		double ar2[]={1.0, -0.631003200551590e3,
			0.496811949533398e5, -0.100425428133695e7};
		signx = (x > 0.0) ? 1 : -1;
		x=Math.abs(x);
		sqrtx=Math.sqrt(x);
		z=30.0/x-1.0;
		z2=z+z;
		br1=br2=0.0;
		for (i=0; i<=3; i++) {
			br=z2*br1-br2+ar1[i];
			br2=br1;
			br1=br;
		}
		numerator=z*br1-br2+0.102776692371524e7;
		br1=br2=0.0;
		for (i=0; i<=3; i++) {
			br=z2*br1-br2+ar2[i];
			br2=br1;
			br1=br;
		}
		denominator=z*br1-br2+0.26028876789105e7;
		return ((numerator/denominator)/sqrtx)*signx;
	} else {
		return Math.exp(-Math.abs(x))*bessi1(x);
	}
}


public static void nonexpbessi(double x, int n, double i[])
{
	if (x == 0.0) {
		i[0]=1.0;
		for (; n>=1; n--) i[n]=0.0;
	} else {
		int k;
		boolean negative;
		double x2,r,s;
		negative = (x < 0.0);
		x=Math.abs(x);
		r=s=0.0;
		x2=2.0/x;
		k=start(x,n,1);
		for (; k>=1; k--) {
			r=1.0/(r+x2*k);
			s=r*(2.0+s);
			if (k <= n) i[k]=r;
		}
		i[0]=r=1.0/(1.0+s);
		if (negative)
			for (k=1; k<=n; k++) r = i[k] *= (-r);
		else
			for (k=1; k<=n; k++) r = i[k] *= r;
	}
}


public static void nonexpbessk01(double x, double k0[], double k1[])
{
	if (x <= 1.5) {
		double expx;
		expx=Math.exp(x);
		bessk01(x,k0,k1);
		k0[0] *= expx;
		k1[0] *= expx;
	} else if (x <= 5.0) {
		int i,r;
		double t2,s1,s2,term1,term2,sqrtexpr,exph2,x2;
		double fac[]={0.90483741803596, 0.67032004603564,
			0.40656965974060, 0.20189651799466, 0.82084998623899e-1,
			0.27323722447293e-1, 0.74465830709243e-2,
			0.16615572731739e-2, 0.30353913807887e-3,
			0.45399929762485e-4, 0.55595132416500e-5,
			0.55739036926944e-6, 0.45753387694459e-7,
			0.30748798795865e-8, 0.16918979226151e-9,
			0.76218651945127e-11, 0.28111852987891e-12,
			0.84890440338729e-14, 0.2098791048793e-15,
			0.42483542552916e-17};
		s1=0.5;
		s2=0.0;
		r=0;
		x2=x+x;
		exph2=1.0/Math.sqrt(5.0*x);
		for (i=0; i<=19; i++) {
			r += 1.0;
			t2=r*r/10.0;
			sqrtexpr=Math.sqrt(t2/x2+1.0);
			term1=fac[i]/sqrtexpr;
			term2=fac[i]*sqrtexpr*t2;
			s1 += term1;
			s2 += term2;
		}
		k0[0] = exph2*s1;
		k1[0] = exph2*s2*2.0;
	} else {
		int r,i;
		double br,br1,br2,cr,cr1,cr2,ermin1,erplus1,er,f0,f1,
				   expx,y,y2;
		double dr[]={0.27545e-15, -0.172697e-14,
				0.1136042e-13, -0.7883236e-13, 0.58081063e-12,
				-0.457993633e-11, 0.3904375576e-10, -0.36454717921e-9,
				0.379299645568e-8, -0.450473376411e-7,
				0.63257510850049e-6, -0.11106685196665e-4,
				0.26953261276272e-3, -0.11310504646928e-1};
		y=10.0/x-1.0;
		y2=y+y;
		r=30;
		br1=br2=cr1=cr2=erplus1=er=0.0;
		for (i=0; i<=13; i++) {
			r -= 2;
			br=y2*br1-br2+dr[i];
			cr=cr1*y2-cr2+er;
			ermin1=r*dr[i]+erplus1;
			erplus1=er;
			er=ermin1;
			br2=br1;
			br1=br;
			cr2=cr1;
			cr1=cr;
		}
		f0=y*br1-br2+0.9884081742308258;
		f1=y*cr1-cr2+er/2.0;
		expx=Math.sqrt(1.5707963267949/x);
		k0[0] = f0 *=expx;
		k1[0] = (1.0+0.5/x)*f0+(10.0/x/x)*expx*f1;
	}
}


public static void nonexpbessk(double x, int n, double k[])
{
	int i;
	double k0,k1,k2;
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];

	nonexpbessk01(x,tmp1,tmp2);
  k0 = tmp1[0];
  k1 = tmp2[0];
	k[0]=k0;
	if (n > 0) k[1]=k1;
	x=2.0/x;
	for (i=2; i<=n; i++) {
		k[i]=k2=k0+x*(i-1)*k1;
		k0=k1;
		k1=k2;
	}
}


public static void bessjaplusn(double a, double x, int n, double ja[])
{
	if (x == 0.0) {
		ja[0] = (a == 0.0) ? 1.0 : 0.0;
		for (; n>=1; n--) ja[n]=0.0;
	} else if (a == 0.0) {
		bessj(x,n,ja);
	} else if (a == 0.5) {
		double s;
		s=Math.sqrt(x)*0.797884560802865;
		spherbessj(x,n,ja);
		for (; n>=0; n--) ja[n] *= s;
	} else {
		int k,m,nu;
		double a2,x2,r,s,l,labda;
		l=1.0;
		nu=start(x,n,0);
		for (m=1; m<=nu; m++) l=l*(m+a)/(m+1);
		r=s=0.0;
		x2=2.0/x;
		k = -1;
		a2=a+a;
		for (m=nu+nu; m>=1; m--) {
			r=1.0/(x2*(a+m)-r);
			if (k == 1)
				labda=0.0;
			else {
				l=l*(m+2)/(m+a2);
				labda=l*(m+a);
			}
			s=r*(labda+s);
			k = -k;
			if (m <= n) ja[m]=r;
		}
		ja[0]=r=1.0/gamma(1.0+a)/(1.0+s)/Math.pow(x2,a);
		for (m=1; m<=n; m++) r = ja[m] *= r;
	}
}


public static void bessya01(double a, double x,
                            double ya[], double ya1[])
{
	if (a == 0.0) {
		bessy01(x,ya,ya1);
	} else {
		int n,na;
		boolean rec,rev;
		double b,c,d,e,f,g,h,p,q,r,s;
    double tmp1[] = new double[1];
    double tmp2[] = new double[1];
    double tmp3[] = new double[1];
    double tmp4[] = new double[1];
		na=(int)Math.floor(a+0.5);
		rec = (a >= 0.5);
		rev = (a < -0.5);
		if (rev || rec) a -= na;
		if (a == -0.5) {
			p=Math.sqrt(2.0/Math.PI/x);
			f=p*Math.sin(x);
			g = -p*Math.cos(x);
		} else if (x < 3.0) {
			b=x/2.0;
			d = -Math.log(b);
			e=a*d;
			c = (Math.abs(a) < 1.0e-8) ? 1.0/Math.PI : a/Math.sin(a*Math.PI);
			s = (Math.abs(e) < 1.0e-8) ? 1.0 : sinh(e)/e;
			e=Math.exp(e);
			g=recipgamma(a,tmp1,tmp2)*e;
			p=tmp1[0];
			q=tmp2[0];
			e=(e+1.0/e)/2.0;
			f=2.0*c*(p*e+q*s*d);
			e=a*a;
			p=g*c;
			q=1.0/g/Math.PI;
			c=a*Math.PI/2.0;
			r = (Math.abs(c) < 1.0e-8) ? 1.0 : Math.sin(c)/c;
			r *= Math.PI*c*r;
			c=1.0;
			d = -b*b;
			ya[0] = f+r*q;
			ya1[0] = p;
			n=1;
			do {
				f=(f*n+p+q)/(n*n-e);
				c=c*d/n;
				p /= (n-a);
				q /= (n+a);
				g=c*(f+r*q);
				h=c*p-n*g;
				ya[0] += g;
				ya1[0] += h;
				n++;
			} while (Math.abs(g/(1.0+Math.abs(ya[0])))+
			         Math.abs(h/(1.0+Math.abs(ya1[0]))) > 1.0e-15);
			f = -ya[0];
			g = -ya1[0]/b;
		} else {
			b=x-Math.PI*(a+0.5)/2.0;
			c=Math.cos(b);
			s=Math.sin(b);
			d=Math.sqrt(2.0/x/Math.PI);
      besspqa01(a,x,tmp1,tmp2,tmp3,tmp4);
			p=tmp1[0];
      q=tmp2[0];
      b=tmp3[0];
      h=tmp4[0];
			f=d*(p*s+q*c);
			g=d*(h*s-b*c);
		}
		if (rev) {
			x=2.0/x;
			na = -na-1;
			for (n=0; n<=na; n++) {
				h=x*(a-n)*f-g;
				g=f;
				f=h;
			}
		} else if (rec) {
			x=2.0/x;
			for (n=1; n<=na; n++) {
				h=x*(a+n)*g-f;
				f=g;
				g=h;
			}
		}
		ya[0] = f;
		ya1[0] = g;
	}
}


public static double sinh(double x)
{
  return 0.5*(Math.exp(x)-Math.exp(-x));
}


public static double cosh(double x)
{
  return 0.5*(Math.exp(x)+Math.exp(-x));
}
    
    
public static double tanh(double x)
{
  double p = Math.exp(x);
  double q = Math.exp(-x);
  return (p-q)/(p+q);
}


public static void bessyaplusn(double a, double x, int nmax, double yan[])
{
	int n;
	double y1;
	double tmp1[] = new double[1];
  double tmp2[] = new double[1];

	bessya01(a,x,tmp1,tmp2);
  yan[0] = tmp1[0];
  y1 = tmp2[0];
	a -= 1.0;
	x=2.0/x;
	if (nmax > 0) yan[1]=y1;
	for (n=2; n<=nmax; n++) yan[n] = -yan[n-2]+(a+n)*x*yan[n-1];
}


public static void besspqa01(double a, double x, double pa[],
                             double qa[], double pa1[], double qa1[])
{
	if (a == 0.0) {
		besspq0(x,pa,qa);
		besspq1(x,pa1,qa1);
	} else {
		int n,na;
		boolean rec,rev;
		double b,p0,q0;
    na=0;
		rev = (a < -0.5);
		if (rev) a = -a-1.0;
		rec = (a >= 0.5);
		if (rec) {
			na=(int)Math.floor(a+0.5);
			a -= na;
		}
		if (a == -0.5) {
			pa[0] = pa1[0] = 1.0;
			qa[0] = qa1[0] = 0.0;
		} else if (x >= 3.0) {
			double c,d,e,f,g,p,q,r,s,temp;
			c=0.25-a*a;
			b=x+x;
			f=r=1.0;
			g = -x;
			s=0.0;
			temp=x*Math.cos(a*Math.PI)/Math.PI*1.0e15;
			e=temp*temp;
			n=2;
			do {
				d=(n-1+c/n);
				p=(2*n*f+b*g-d*r)/(n+1);
				q=(2*n*g-b*f-d*s)/(n+1);
				r=f;
				f=p;
				s=g;
				g=q;
				n++;
			} while ((p*p+q*q)*n*n < e);
			e=f*f+g*g;
			p=(r*f+s*g)/e;
			q=(s*f-r*g)/e;
			f=p;
			g=q;
			n--;
			while (n > 0) {
				r=(n+1)*(2.0-p)-2.0;
				s=b+(n+1)*q;
				d=(n-1+c/n)/(r*r+s*s);
				p=d*r;
				q=d*s;
				e=f;
				f=p*(e+1.0)-g*q;
				g=q*(e+1.0)+p*g;
				n--;
			}
			f += 1.0;
			d=f*f+g*g;
			pa[0] = f/d;
			qa[0] = -g/d;
			d=a+0.5-p;
			q += x;
			pa1[0] = (pa[0]*q-qa[0]*d)/x;
			qa1[0] = (qa[0]*q+pa[0]*d)/x;
		} else {
			double c,s,chi;
			double ya[] = new double[1];
			double ya1[] = new double[1];
			double ja[] = new double[2];
			b=Math.sqrt(Math.PI*x/2.0);
			chi=x-Math.PI*(a/2.0+0.25);
			c=Math.cos(chi);
			s=Math.sin(chi);
			bessya01(a,x,ya,ya1);
			bessjaplusn(a,x,1,ja);
			pa[0] = b*(ya[0]*s+c*ja[0]);
			qa[0] = b*(c*ya[0]-s*ja[0]);
			pa1[0] = b*(s*ja[1]-c*ya1[0]);
			qa1[0] = b*(c*ja[1]+s*ya1[0]);
		}
		if (rec) {
			x=2.0/x;
			b=(a+1.0)*x;
			for (n=1; n<=na; n++) {
				p0=pa[0]-qa1[0]*b;
				q0=qa[0]+pa1[0]*b;
				pa[0] = pa1[0];
				pa1[0] = p0;
				qa[0] = qa1[0];
				qa1[0] = q0;
				b += x;
			}
		}
		if (rev) {
			p0 = pa1[0];
			pa1[0] = pa[0];
			pa[0] = p0;
			q0 = qa1[0];
			qa1[0] = qa[0];
			qa[0] = q0;
		}
	}
}


public static void besszeros(double a, int n, double z[], int d)
{
	int j,s;
	double aa,a2,b,bb,c,chi,co,mu,mu2,mu3,mu4,p,p0,p1,pp1,
			   q,q1,qq1,ro,si,t,tt,u,v,w,x,xx,x4,y,yy,fi;
  double pa[] = new double[1];
  double pa1[] = new double[1];
  double qa[] = new double[1];
  double qa1[] = new double[1];

	aa=a*a;
	mu=4.0*aa;
	mu2=mu*mu;
	mu3=mu*mu2;
	mu4=mu2*mu2;
	if (d < 3) {
		p=7.0*mu-31.0;
		p0=mu-1.0;
		p1=4.0*(253.0*mu2-3722.0*mu+17869.0)/15.0/p*p0;
		q1=8.0*(83.0*mu2-982.0*mu+3779.0)/5.0/p;
	} else {
		p=7.0*mu2+82.0*mu-9.0;
		p0=mu+3.0;
		p1=(4048.0*mu4+131264.0*mu3-221984.0*mu2-
			417600.0*mu+1012176.0)/60.0/p;
		q1=1.6*(83.0*mu3+2075.0*mu2-3039.0*mu+3537.0)/p;
	}
	t = (d == 1 || d == 4) ? 0.25 : 0.75;
	tt=4.0*t;
	if (d < 3) {
		pp1=5.0/48.0;
		qq1 = -5.0/36.0;
	} else {
		pp1 = -7.0/48.0;
		qq1=35.0/288.0;
	}
	y=3.0*Math.PI/8.0;
	bb = (a >= 3.0) ? Math.pow(a,-2.0/3.0) : 0.0;
	for (s=1; s<=n; s++) {
		if (a == 0.0 && s == 1 && d == 3) {
			x=0.0;
			j=0;
		} else {
			if (s >= 3.0*a-8.0) {
				b=(s+a/2.0-t)*Math.PI;
				c=1.0/b/b/64.0;
				x=b-1.0/b/8.0*(p0-p1*c)/(1.0-q1*c);
			} else {
				if (s == 1)
					x = ((d == 1) ? -2.33811 : ((d == 2) ? -1.17371 :
							((d == 3) ? -1.01879 : -2.29444)));
				else {
					x=y*(4.0*s-tt);
					v=1.0/x/x;
					x = -Math.pow(x,2.0/3.0)*(1.0+v*(pp1+qq1*v));
				}
				u=x*bb;
				yy=2.0/3.0*Math.pow(-u,1.5);
				if (yy == 0.0)
					fi=0.0;
				else if (yy > 1.0e5)
					fi=1.570796;
				else {
					double r,pp;
					if (yy <1.0) {
						p=Math.pow(3.0*yy,1.0/3.0);
						pp=p*p;
						p *= (1.0+pp*(-210.0+pp*(27.0-2.0*pp))/1575.0);
					} else {
						p=1.0/(yy+1.570796);
						pp=p*p;
						p=1.570796-p*(1.0+pp*(2310.0+pp*(3003.0+pp*
							(4818.0+pp*(8591.0+pp*16328.0))))/3465.0);
					}
					pp=(yy+p)*(yy+p);
					r=(p-Math.atan(p+yy))/pp;
					fi=p-(1.0+pp)*r*(1.0+r/(p+yy));
				}
				v=fi;
				w=1.0/Math.cos(v);
				xx=1.0-w*w;
				c=Math.sqrt(u/xx);
				x=w*(a+c/a/u*((d < 3) ?
					-5.0/48.0/u-c*(-5.0/24.0/xx+1.0/8.0) :
					7.0/48.0/u+c*(-7.0/24.0/xx+3.0/8.0)));
			}
			j=0;
			do {
				xx=x*x;
				x4=xx*xx;
				a2=aa-xx;
				besspqa01(a,x,pa,qa,pa1,qa1);
				chi=x-Math.PI*(a/2.0+0.25);
				si=Math.sin(chi);
				co=Math.cos(chi);
				ro = ((d == 1) ? (pa[0]*co-qa[0]*si)/(pa1[0]*si+qa1[0]*co) :
						((d == 2) ? (pa[0]*si+qa[0]*co)/(qa1[0]*si-pa1[0]*co) :
						((d == 3) ? a/x-(pa1[0]*si+qa1[0]*co)/(pa[0]*co-qa[0]*si) :
										a/x-(qa1[0]*si-pa1[0]*co)/(pa[0]*si+qa[0]*co))));
				j++;
				if (d < 3) {
					u=ro;
					p=(1.0-4.0*a2)/6.0/x/(2.0*a+1.0);
					q=(2.0*(xx-mu)-1.0-6.0*a)/3.0/x/(2.0*a+1.0);
				} else {
					u = -xx*ro/a2;
					v=2.0*x*a2/(aa+xx)/3.0;
					w=a2*a2*a2;
					q=v*(1.0+(mu2+32.0*mu*xx+48.0*x4)/32.0/w);
					p=v*(1.0+(-mu2+40.0*mu*xx+48.0*x4)/64.0/w);
				}
				w=u*(1.0+p*ro)/(1.0+q*ro);
				x += w;
			} while (Math.abs(w/x) > 1.0e-13 && j < 5);
		}
		z[s]=x;
	}
}


public static int start(double x, int n, int t)
{
	int s;
	double p,q,r,y;

	s=2*t-1;
	p=36.0/x-t;
	r=n/x;
	if (r > 1.0 || t == 1) {
		q=Math.sqrt(r*r+s);
		r=r*Math.log(q+r)-q;
	} else
		r=0.0;
	q=18.0/x+r;
	r = (p > q) ? p : q;
	p=Math.sqrt(2.0*(t+r));
	p=x*((1.0+r)+p)/(1.0+p);
	y=0.0;
	q=y;
	do {
		y=p;
		p /= x;
		q=Math.sqrt(p*p+s);
		p=x*(r+q)/Math.log(p+q);
		q=y;
	} while (p > q || p < q-1.0);
	return ((t == 1) ? (int)Math.floor(p+1.0) : -(int)Math.floor(-p/2.0)*2);
}


public static void bessiaplusn(double a, double x, int n, double ia[])
{
	if (x == 0.0) {
		ia[0] = (a == 0.0) ? 1.0 : 0.0;
		for (; n>=1; n--) ia[n]=0.0;
	} else if (a == 0.0) {
		bessi(x,n,ia);
	} else if (a == 0.5) {
		double c;
		c=0.797884560802865*Math.sqrt(Math.abs(x))*Math.exp(Math.abs(x));
		nonexpspherbessi(x,n,ia);
		for (; n>=0; n--) ia[n] *= c;
	} else {
		double expx;
		expx=Math.exp(Math.abs(x));
		nonexpbessiaplusn(a,x,n,ia);
		for (; n>=0; n--) ia[n] *= expx;
	}
}


public static void besska01(double a, double x,
                            double ka[], double ka1[])
{
	if (a == 0.0) {
		bessk01(x,ka,ka1);
	} else {
		int n,na;
		boolean rec,rev;
		double f,g,h;
		na=0;
		rev = (a < -0.5);
		if (rev) a = -a-1.0;
		rec = (a >= 0.5);
		if (rec) {
			na=(int)Math.floor(a+0.5);
			a -= na;
		}
		if (a == 0.5)
			f=g=Math.sqrt(Math.PI/x/2.0)*Math.exp(-x);
		else if (x < 1.0) {
			double a1,b,c,d,e,p,q,s;
			double tmp1[] = new double[1];
      double tmp2[] = new double[1];
			b=x/2.0;
			d = -Math.log(b);
			e=a*d;
			c=a*Math.PI;
			c = (Math.abs(c) < 1.0e-15) ? 1.0 : c/Math.sin(c);
			s = (Math.abs(e) < 1.0e-15) ? 1.0 : sinh(e)/e;
			e=Math.exp(e);
			a1=(e+1.0/e)/2.0;
			g=recipgamma(a,tmp1,tmp2)*e;
			p=tmp1[0];
			q=tmp2[0];
			ka[0] = f = c*(p*a1+q*s*d);
			e=a*a;
			p=0.5*g*c;
			q=0.5/g;
			c=1.0;
			d=b*b;
			ka1[0] = p;
			n=1;
			do {
				f=(f*n+p+q)/(n*n-e);
				c=c*d/n;
				p /= (n-a);
				q /= (n+a);
				g=c*(p-n*f);
				h=c*f;
				ka[0] += h;
				ka1[0] += g;
				n++;
			} while (h/ka[0]+Math.abs(g)/ka1[0] > 1.0e-15);
			f=ka[0];
			g=ka1[0]/b;
		} else {
			double expon;
			expon=Math.exp(-x);
			nonexpbesska01(a,x,ka,ka1);
			f=expon*ka[0];
			g=expon*ka1[0];
		}
		if (rec) {
			x=2.0/x;
			for (n=1; n<=na; n++) {
				h=f+(a+n)*x*g;
				f=g;
				g=h;
			}
		}
		if (rev) {
			ka1[0] = f;
			ka[0] = g;
		} else {
			ka[0] = f;
			ka1[0] = g;
		}
	}
}


public static void besskaplusn(double a, double x, int nmax, double kan[])
{
	int n;
	double k1;
	double tmp1[] = new double[1];
  double tmp2[] = new double[1];

	besska01(a,x,tmp1,tmp2);
  kan[0]=tmp1[0];
  k1=tmp2[0];
	a -= 1.0;
	x=2.0/x;
	if (nmax > 0) kan[1]=k1;
	for (n=2; n<=nmax; n++) kan[n]=kan[n-2]+(a+n)*x*kan[n-1];
}


public static void nonexpbessiaplusn(double a, double x,
                                     int n, double ia[])
{
	if (x == 0.0) {
		ia[0] = (a == 0.0) ? 1.0 : 0.0;
		for (; n>=1; n--) ia[n]=0.0;
	} else if (a == 0.0) {
		nonexpbessi(x,n,ia);
	} else if (a == 0.5) {
		double c;
		c=0.797884560802865*Math.sqrt(x);
		nonexpspherbessi(x,n,ia);
		for (; n>=0; n--) ia[n] *= c;
	} else {
		int m,nu;
		double r,s,labda,l,a2,x2;
		a2=a+a;
		x2=2.0/x;
		l=1.0;
		nu=start(x,n,1);
		r=s=0.0;
		for (m=1; m<=nu; m++) l=l*(m+a2)/(m+1);
		for (m=nu; m>=1; m--) {
			r=1.0/(x2*(a+m)+r);
			l=l*(m+1)/(m+a2);
			labda=l*(m+a)*2.0;
			s=r*(labda+s);
			if (m <= n) ia[m]=r;
		}
		ia[0]=r=1.0/(1.0+s)/gamma(1.0+a)/Math.pow(x2,a);
		for (m=1; m<=n; m++) r = ia[m] *= r;
	}
}


public static void nonexpbesska01(double a, double x,
                                  double ka[], double ka1[])
{
	if (a == 0.0) {
		nonexpbessk01(x,ka,ka1);
	} else {
		int n,na;
		boolean rec,rev;
		double f,g,h;
		na=0;
		rev = (a < -0.5);
		if (rev) a = -a-1.0;
		rec = (a >= 0.5);
		if (rec) {
			na=(int)Math.floor(a+0.5);
			a -= na;
		}
		if (a == -0.5)
			f=g=Math.sqrt(Math.PI/x/2.0);
		else if (x < 1.0) {
			double expon;
			expon=Math.exp(x);
			besska01(a,x,ka,ka1);
			f=expon*ka[0];
			g=expon*ka1[0];
		} else {
			double b,c,e,p,q;
			c=0.25-a*a;
			b=x+x;
			g=1.0;
			f=0.0;
			e=Math.cos(a*Math.PI)/Math.PI*x*1.0e15;
			n=1;
			do {
				h=(2.0*(n+x)*g-(n-1+c/n)*f)/(n+1);
				f=g;
				g=h;
				n++;
			} while (h*n < e);
			p=q=f/g;
			e=b-2.0;
			do {
				p=(n-1+c/n)/(e+(n+1)*(2.0-p));
				q=p*(1.0+q);
				n--;
			} while (n > 0);
			f=Math.sqrt(Math.PI/b)/(1.0+q);
			g=f*(a+x+0.5-p)/x;
		}
		if (rec) {
			x=2.0/x;
			for (n=1; n<=na; n++) {
				h=f+(a+n)*x*g;
				f=g;
				g=h;
			}
		}
		if (rev) {
			ka1[0] = f;
			ka[0] = g;
		} else {
			ka[0] = f;
			ka1[0] = g;
		}
	}
}


public static void nonexpbesskaplusn(double a, double x,
                                     int nmax, double kan[])
{
	int n;
	double k1;
  double tmp1[] = new double[1];
  double tmp2[] = new double[1];

	nonexpbesska01(a,x,tmp1,tmp2);
  kan[0]=tmp1[0];
  k1=tmp2[0];
	a -= 1.0;
	x=2.0/x;
	if (nmax > 0) kan[1]=k1;
	for (n=2; n<=nmax; n++) kan[n]=kan[n-2]+(a+n)*x*kan[n-1];
}


public static void spherbessj(double x, int n, double j[])
{
	if (x == 0.0) {
		j[0]=1.0;
		for (; n>=1; n--) j[n]=0.0;
	} else if (n == 0) {
		double x2;
		if (Math.abs(x) < 0.015) {
			x2=x*x/6.0;
			j[0]=1.0+x2*(x2*0.3-1.0);
		} else
			j[0]=Math.sin(x)/x;
	} else {
		int m;
		double r,s;
		r=0.0;
		m=start(x,n,0);
		for (; m>=1; m--) {
			r=1.0/((m+m+1)/x-r);
			if (m <= n) j[m]=r;
		}
		if (x < 0.015) {
			s=x*x/6.0;
			j[0]=r=s*(s*0.3-1.0)+1.0;
		} else
			j[0]=r=Math.sin(x)/x;
		for (m=1; m<=n; m++) r = j[m] *= r;
	}
}


public static void spherbessy(double x, int n, double y[])
{
	if (n == 0)
		y[0] = -Math.cos(x)/x;
	else {
		int i;
		double yi,yi1,yi2;
		yi2 = y[0] = -Math.cos(x)/x;
		yi1=y[1]=(yi2-Math.sin(x))/x;
		for (i=2; i<=n; i++) {
			y[i] = yi = -yi2+(i+i-1)*yi1/x;
			yi2=yi1;
			yi1=yi;
		}
	}
}


public static void spherbessi(double x, int n, double i[])
{
	if (x == 0.0) {
		i[0]=1.0;
		for (; n>=1; n--) i[n]=0.0;
	} else {
		double expx;
		expx=Math.exp(x);
		nonexpspherbessi(x,n,i);
		for (; n>=0; n--) i[n] *= expx;
	}
}


public static void spherbessk(double x, int n, double k[])
{
	double expx;
	expx=Math.exp(-x);
	nonexpspherbessk(x,n,k);
	for (; n>=0; n--) k[n] *= expx;
}


public static void nonexpspherbessi(double x, int n, double i[])
{
	if (x == 0.0) {
		i[0]=1.0;
		for (; n>=1; n--) i[n]=0.0;
	} else {
		int m;
		double x2,r;
		x2=x+x;
		i[0] = x2 = ((x == 0.0) ? 1.0 : ((x2 < 0.7) ?
           sinh(x)/(x*Math.exp(x)) : (1.0-Math.exp(-x2))/x2));
		if (n != 0) {
			r=0.0;
			m=start(x,n,1);
			for (; m>=1; m--) {
				r=1.0/((m+m+1)/x+r);
				if (m <= n) i[m]=r;
			}
			for (m=1; m<=n; m++) x2 = i[m] *= x2;
		}
	}
}


public static void nonexpspherbessk(double x, int n, double k[])
{
	int i;
	double ki,ki1,ki2;
	x=1.0/x;
	k[0]=ki2=x*1.5707963267949;
	if (n != 0) {
		k[1]=ki1=ki2*(1.0+x);
		for (i=2; i<=n; i++) {
			k[i]=ki=ki2+(i+i-1)*x*ki1;
			ki2=ki1;
			ki1=ki;
		}
	}
}


public static void airy(double z, double ai[], double aid[],
       double bi[], double bid[], double expon[],
       boolean first, double xtmp[])
{
	int n,l;
	double s,t,u,v,sc,tc,uc,vc,x,k1,k2,k3,k4,c,zt,si,co,expzt,
         sqrtz,wwl,pl,pl1,pl2,pl3;

	if (first) {
		xtmp[1] =1.4083081072180964e1;
		xtmp[2] =1.0214885479197331e1;
		xtmp[3] =7.4416018450450930;
		xtmp[4] =5.3070943061781927;
		xtmp[5] =3.6340135029132462;
		xtmp[6] =2.3310652303052450;
		xtmp[7] =1.3447970842609268;
		xtmp[8] =6.4188858369567296e-1;
		xtmp[9] =2.0100345998121046e-1;
		xtmp[10]=8.0594359172052833e-3;
		xtmp[11] =3.1542515762964787e-14;
		xtmp[12] =6.6394210819584921e-11;
		xtmp[13] =1.7583889061345669e-8;
		xtmp[14] =1.3712392370435815e-6;
		xtmp[15] =4.4350966639284350e-5;
		xtmp[16] =7.1555010917718255e-4;
		xtmp[17] =6.4889566103335381e-3;
		xtmp[18] =3.6440415875773282e-2;
		xtmp[19] =1.4399792418590999e-1;
		xtmp[20]=8.1231141336261486e-1;
    xtmp[21]=0.355028053887817;
    xtmp[22]=0.258819403792807;
    xtmp[23]=1.73205080756887729;
    xtmp[24]=0.78539816339744831;
    xtmp[25]=0.56418958354775629;
	}
	expon[0]=0.0;
	if (z >= -5.0 && z <= 8.0) {
		u=v=t=uc=vc=tc=1.0;
		s=sc=0.5;
		n=3;
		x=z*z*z;
		while (Math.abs(u)+Math.abs(v)+Math.abs(s)+Math.abs(t) > 1.0e-18) {
			u=u*x/(n*(n-1));
			v=v*x/(n*(n+1));
			s=s*x/(n*(n+2));
			t=t*x/(n*(n-2));
			uc += u;
			vc += v;
			sc += s;
			tc += t;
			n += 3;
		}
		bi[0]=xtmp[23]*(xtmp[21]*uc+xtmp[22]*z*vc);
		bid[0]=xtmp[23]*(xtmp[21]*z*z*sc+xtmp[22]*tc);
		if (z < 2.5) {
			ai[0]=xtmp[21]*uc-xtmp[22]*z*vc;
			aid[0]=xtmp[21]*sc*z*z-xtmp[22]*tc;
			return;
		}
	}
	k1=k2=k3=k4=0.0;
	sqrtz=Math.sqrt(Math.abs(z));
	zt=0.666666666666667*Math.abs(z)*sqrtz;
	c=xtmp[25]/Math.sqrt(sqrtz);
	if (z < 0.0) {
		z = -z;
		co=Math.cos(zt-xtmp[24]);
		si=Math.sin(zt-xtmp[24]);
		for (l=1; l<=10; l++) {
			wwl=xtmp[l+10];
			pl=xtmp[l]/zt;
			pl2=pl*pl;
			pl1=1.0+pl2;
			pl3=pl1*pl1;
			k1 += wwl/pl1;
			k2 += wwl*pl/pl1;
			k3 += wwl*pl*(1.0+pl*(2.0/zt+pl))/pl3;
			k4 += wwl*(-1.0-pl*(1.0+pl*(zt-pl))/zt)/pl3;
		}
		ai[0]=c*(co*k1+si*k2);
		aid[0]=0.25*ai[0]/z-c*sqrtz*(co*k3+si*k4);
		bi[0]=c*(co*k2-si*k1);
		bid[0]=0.25*bi[0]/z-c*sqrtz*(co*k4-si*k3);
	} else {
		if (z < 9.0)
			expzt=Math.exp(zt);
		else {
			expzt=1.0;
			expon[0]=zt;
		}
		for (l=1; l<=10; l++) {
			wwl=xtmp[l+10];
			pl=xtmp[l]/zt;
			pl1=1.0+pl;
			pl2=1.0-pl;
			k1 += wwl/pl1;
			k2 += wwl*pl/(zt*pl1*pl1);
			k3 += wwl/pl2;
			k4 += wwl*pl/(zt*pl2*pl2);
		}
		ai[0]=0.5*c*k1/expzt;
		aid[0]=ai[0]*(-0.25/z-sqrtz)+0.5*c*sqrtz*k2/expzt;
		if (z >= 8.0) {
			bi[0]=c*k3*expzt;
			bid[0]=bi[0]*(sqrtz-0.25/z)-c*k4*sqrtz*expzt;
		}
	}
}


public static double airyzeros(int n, int d, double zai[], double vai[])
{
	boolean a,found;
	int i;
	double c,e,r,zaj,zak,vaj,daj,kaj,zz;
  double statictemp[] = new double[26];
	double tmp1[] = new double[1];
  double tmp2[] = new double[1];
  double tmp3[] = new double[1];
  double tmp4[] = new double[1];
  double tmp5[] = new double[1];

	a=((d == 0) || (d == 2));
	r = (d == 0 || d == 3) ? -1.17809724509617 : -3.53429173528852;
  airy(0.0,tmp1,tmp2,tmp3,tmp4,tmp5,true,statictemp);
  zaj=tmp1[0];
  vaj=tmp2[0];
  daj=tmp3[0];
  kaj=tmp4[0];
  zz=tmp5[0];
	for (i=1; i<=n; i++) {
		r += 4.71238898038469;
		zz=r*r;
		zaj = (i == 1 && d == 1) ? -1.01879297 :
				((i == 1 && d == 2) ? -1.17371322 :
				Math.pow(r,0.666666666666667)*
				(a ? -(1.0+(5.0/48.0-(5.0/36.0-(77125.0/82944.0-
				(108056875.0/6967296.0-(162375596875.0/334430208.0)/
				zz)/zz)/zz)/zz)/zz) : -(1.0-(7.0/48.0-(35.0/288.0-
				(181223.0/207360.0-(18683371.0/1244160.0-
				(91145884361.0/191102976.0)/zz)/zz)/zz)/zz)/zz)));
		if (d <= 1.0) {
      airy(zaj,tmp1,tmp2,tmp3,tmp4,tmp5,false,statictemp);
      vaj=tmp1[0];
      daj=tmp2[0];
      c=tmp3[0];
      e=tmp4[0];
      zz=tmp5[0];
		}
		else {
      airy(zaj,tmp1,tmp2,tmp3,tmp4,tmp5,false,statictemp);
      c=tmp1[0];
      e=tmp2[0];
      vaj=tmp3[0];
      daj=tmp4[0];
      zz=tmp5[0];
		}
		found=(Math.abs(a ? vaj : daj) < 1.0e-12);
		while (!found) {
			if (a) {
				kaj=vaj/daj;
				zak=zaj-kaj*(1.0+zaj*kaj*kaj);
			} else {
				kaj=daj/(zaj*vaj);
				zak=zaj-kaj*(1.0+kaj*(kaj*zaj+1.0/zaj));
			}
			if (d <= 1) {
        airy(zak,tmp1,tmp2,tmp3,tmp4,tmp5,false,statictemp);
        vaj=tmp1[0];
        daj=tmp2[0];
        c=tmp3[0];
        e=tmp4[0];
        zz=tmp5[0];
			}
			else {
        airy(zak,tmp1,tmp2,tmp3,tmp4,tmp5,false,statictemp);
        c=tmp1[0];
        e=tmp2[0];
        vaj=tmp3[0];
        daj=tmp4[0];
        zz=tmp5[0];
			}
			found=(Math.abs(zak-zaj) < 1.0e-14*Math.abs(zak) ||
					Math.abs(a ? vaj : daj) < 1.0e-12);
			zaj=zak;
		}
		vai[i]=(a ? daj : vaj);
		zai[i]=zaj;
	}
	return zai[n];
}

}