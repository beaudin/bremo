package berechnungsModule.gemischbildung;



import matLib.DglSysFunction;
import matLib.RungeKutta;
import misc.PhysKonst;

public class Verdampfung_Stiesch_Thoma implements DglSysFunction{
	
	private RungeKutta rk ;
	private Kraftstoff_Eigenschaften krstEigenschaften;
	private double diffusionsKoeffizient=4.6e-7;  //in m2/s;
	private double M_Luft;
	private double M_Krst;
    private double viskositaet_Krst;
	private int nODE=3;
	private double rhoZyl,cp_Luft,k_Luft,viskositaet_Luft,pZyl,TZyl,v_relativ;
	
	public Verdampfung_Stiesch_Thoma(String vergleichsKraftstoff){
		rk=new RungeKutta();
		krstEigenschaften=new Kraftstoff_Eigenschaften(vergleichsKraftstoff );		
		M_Luft=0.0289644;
		M_Krst=krstEigenschaften.get_M_krst();
		viskositaet_Krst=krstEigenschaften.get_dynVis_krst(300);
		
	}
	
	//
	private double get_xF(double pZyl,double T_tropfen){
		double xF;
		double pS=krstEigenschaften.get_pS(T_tropfen);
		xF=pS/2/pZyl;
		return xF;
	}
	
	private double get_M_gasGemisch(double pZyl,double T_tropfen){
    	double M_gasGemisch;
    	double xF=get_xF(pZyl,T_tropfen);
    	M_gasGemisch=(1-xF)*M_Luft+xF*M_Krst;
    	return M_gasGemisch;
    }

	private double get_Ts(double T,double T_tropfen){
		double Ts;
		Ts=0.5*(T+T_tropfen);
		return Ts;
	}
	
	private double get_rho_gasGemisch(double pZyl,double T,double T_tropfen){
		double rho_gasGemisch;
		double M_gasGemisch=get_M_gasGemisch(pZyl,T_tropfen);
		double Ts=get_Ts(T,T_tropfen);
		double R_allg=PhysKonst.get_R_allg();
		rho_gasGemisch=pZyl*M_gasGemisch/R_allg/Ts;
		return rho_gasGemisch;
	}
	
	/**Kraftstoffmassenbruch in der Grenzschicht 
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_wF(double pZyl,double T_tropfen){
		double wF;
		double M_gasGemisch=get_M_gasGemisch(pZyl,T_tropfen);
		double pS=krstEigenschaften.get_pS(T_tropfen);
		wF=pS/pZyl*M_Krst/M_gasGemisch;
		return wF;
	}
	
    /**Kraftstoffmassenbruch an der Tropfenoberflaeche
     * @param pZyl
     * @param T_tropfen
     * @return
     */
    private double get_wFS(double pZyl,double T_tropfen){
    	double wFS;
    	double wF=get_wF(pZyl,T_tropfen);
    	wFS=3/2*wF; //Woher kommt das?
    	return wFS;
    }
	
	private double get_cp_gasGemisch(double cp_Luft,double pZyl,double T_tropfen){
		double cp_gasGemisch;
		double wF=get_wF(pZyl,T_tropfen);
		double cpf_krst=krstEigenschaften.get_cp(T_tropfen);
		cp_gasGemisch=wF*cpf_krst+(1-wF)*cp_Luft;
		return cp_gasGemisch;
	}
	
	/**
     * Korrekturfaktor ergibt sich nach Mason und Saxena
     * @param viskositaet_Luft
     * @return
     */
    private double get_korrekturFaktor(double viskositaet_Luft){
    	double korrekturFaktor;
    	korrekturFaktor=0.5/Math.sqrt(2)/Math.sqrt(1+M_Krst/M_Luft)*
    	                Math.pow((1+Math.sqrt(viskositaet_Krst/viskositaet_Luft)*Math.pow(M_Luft/M_Krst,0.25)),2);
//    	System.out.println(korrekturFaktor);
    	return korrekturFaktor;
    }
    
    private double get_viskositaet_gasGemisch(double viskositaet_Luft,double pZyl,double T_tropfen){
    	double viskositaet_gasGemisch;
    	double xF=get_xF(pZyl,T_tropfen);
    	viskositaet_gasGemisch=(xF*viskositaet_Krst+(1-xF)*viskositaet_Luft)/
    	                       ((1-xF)*get_korrekturFaktor(viskositaet_Luft));
    	return viskositaet_gasGemisch;
    }
    
    private double get_k_gasGemisch(double k_Luft,double pZyl,double T_tropfen){
    	double k_gasGemisch;
    	double k_krst=krstEigenschaften.get_waermeLeit(T_tropfen);
    	double xF=get_xF(pZyl,T_tropfen);
    	k_gasGemisch=(xF*k_krst+(1-xF)*k_Luft)/((1-xF)*get_korrekturFaktor(viskositaet_Luft));
    	return k_gasGemisch;
    }
	

    
    private double get_bm(double pZyl,double T_tropfen){
    	double bm;
    	double wFS=get_wFS(pZyl,T_tropfen);
    	bm=wFS/(1-wFS);
    	return bm;
    }
    
    private double get_Re(double viskositaet_Luft,double pZyl,double T,
    		              double T_tropfen,double d_tropfen,double v_relativ){
    	double Re;
    	Re=get_rho_gasGemisch(pZyl,T,T_tropfen)*v_relativ*d_tropfen/
    	   get_viskositaet_gasGemisch(viskositaet_Luft,pZyl,T_tropfen);
    	
    	return Re;
    }
    
    private double get_Pr(double cp_Luft,double k_Luft,double viskositaet_Luft,double pZyl,double T_tropfen){
    	double Pr;
    	Pr=get_cp_gasGemisch(cp_Luft,pZyl,T_tropfen)*get_viskositaet_gasGemisch(viskositaet_Luft,pZyl,T_tropfen)/
    	   get_k_gasGemisch(k_Luft,pZyl,T_tropfen);
    	return Pr;
    }
    
    private double get_Sc(double viskositaet_Luft,double pZyl,double T,double T_tropfen){
    	double Sc;
    	Sc=get_viskositaet_gasGemisch(viskositaet_Luft,pZyl,T_tropfen)/
    	   get_rho_gasGemisch(pZyl,T,T_tropfen)/
    	   diffusionsKoeffizient;
    	return Sc;
    }
    
    private double get_Nu(double cp_Luft,double k_Luft,double viskositaet_Luft,
    		              double pZyl,double T,
    		              double T_tropfen,double d_tropfen,double v_relativ){
    	double Nu;
    	Nu=2+0.6*Math.sqrt(get_Re(viskositaet_Luft,pZyl,T,T_tropfen,d_tropfen,v_relativ))*
    	   Math.pow(get_Pr(cp_Luft,k_Luft,viskositaet_Luft,pZyl,T_tropfen),1/3d);
    	return Nu;
    }
    
    private double get_Sh(double viskositaet_Luft,double pZyl,double T,
    		              double T_tropfen,double d_tropfen,double v_relativ){
    	double Sh;
    	Sh=2+0.6*Math.sqrt(get_Re(viskositaet_Luft,pZyl,T,T_tropfen,d_tropfen,v_relativ))*
    	   Math.pow(get_Sc(viskositaet_Luft,pZyl,T,T_tropfen),1/3d);
    	return Sh;
    }
    
    public double[] derivn(double time, double[] yd) {
    	double[] dydx=new double[nODE];
    	
    	double rhoK=krstEigenschaften.get_rhoK(yd[2]);
    	double diff_rhoK=krstEigenschaften.get_diff_rhoK(yd[2]);
    	double cpl_krst=krstEigenschaften.get_cpl(yd[2]);
    	double cpf_krst=krstEigenschaften.get_cp(yd[2]);
        double bm=get_bm(pZyl,yd[2]);
        double L=krstEigenschaften.get_L(yd[2]);
    	double rho_gasGemisch=get_rho_gasGemisch(pZyl,TZyl,yd[2]);
    	double k_gasGemisch=get_k_gasGemisch(k_Luft,pZyl,yd[2]);
    	double Sh=get_Sh(viskositaet_Luft,pZyl,TZyl,yd[2],yd[1],v_relativ);
    	double Nu=get_Nu(cp_Luft,k_Luft,viskositaet_Luft,pZyl,TZyl,yd[2],yd[1],v_relativ);    	
    	
    	double dm=Math.PI*yd[1]*rho_gasGemisch*diffusionsKoeffizient*Sh*Math.log(1+bm);
//    	double dD=-2/(Math.PI*yd[1]*yd[1]*rhoK)*dm;
        double z=cpf_krst*dm/Math.PI/yd[1]/k_gasGemisch/Nu;
        double diff_Q=Math.PI*yd[1]*k_gasGemisch*(TZyl-yd[2])*Nu*z/Math.expm1(z);
        double dT=1/(yd[0]*cpl_krst)*(diff_Q-dm*L);
        
    	double dD=2/(Math.PI*yd[1]*yd[1]*rhoK)*(-dm-Math.PI*yd[1]*yd[1]*yd[1]/6*diff_rhoK*dT);
    	
    	dydx[0]=-dm;
    	dydx[1]=dD;
    	dydx[2]=dT;
    	
    	System.out.println(dydx[1]);
    	return dydx;
	}
    
    public double []get_m_D_T_2(double rhoL,
    		                    double cp_Luft,
    		                    double k_Luft,
    		                    double viskositaet_Luft,
                                double startTime,
    		                    double finalTime,//Zeit zwischen zwei Berechnungspunkten --> Schrittweite
	                            double pZyl,
	                            double T,
	                            double v_relativ,
	                            double [] m_D_T_2)//Anfangswerte zum Zeitpunkt Null
    {this.rhoZyl=rhoL;
        this.cp_Luft=cp_Luft;
    	this.k_Luft=k_Luft;
    	this.viskositaet_Luft=viskositaet_Luft;
        this.pZyl=pZyl;
        this.TZyl=T;
        this.v_relativ=v_relativ;

            double stepSize=finalTime/10;
            rk.setStepSize(stepSize);

            rk.setInitialValueOfX(startTime);    		
            rk.setInitialValueOfY(m_D_T_2);

            rk.setFinalValueOfX(finalTime);

        double y[]=rk.fourthOrder(this);
        return y;
        }    
}

