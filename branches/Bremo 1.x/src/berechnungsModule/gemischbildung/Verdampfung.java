package berechnungsModule.gemischbildung;

import berechnungsModule.Berechnung.Zone;
import matLib.DglSysFunction;
import matLib.RungeKutta;
import misc.PhysKonst;

public class Verdampfung implements DglSysFunction{
	private RungeKutta rk ;
	private Kraftstoff_Eigenschaften krstEigenschaften;
	private double diffusionsKoeffizient;
	private double M_Krst;
	private double viskositaet_krst;
	private double k_Luft,viskositaet_Luft;

	public Verdampfung(Kraftstoff_Eigenschaften krstEigenschaften){
		rk=new RungeKutta();		
		this.krstEigenschaften=krstEigenschaften;		
		M_Krst=krstEigenschaften.get_M_krst();
		viskositaet_krst=krstEigenschaften.get_dynVis_krst(300);	
		//Da keine Daten zur Verfügung stehen wird erstmal mit luft gerechnet
		//TODO Spaeter sollten dei Waermeleitfaehigkeit und die viskositaet in die Klasse Spezies aufgenommen werden
		this.k_Luft=0.0684; //Waermeleitfaehigkeit in W/m/K
		this.viskositaet_Luft=4.355e-5;
		this.diffusionsKoeffizient=4.6e-7; //in m2/s;
	}


	/**
	 * 
	 * @param zn
	 * @param startTime
	 * @param finalTime
	 * @param v_relativ
	 * @param m_D_T
	 * @return
	 */
	public double []get_m_D_T( Zone zn,			
			double startTime,
			double finalTime,//Zeit zwischen zwei Berechnungspunkten --> Schrittweite
			double v_relativ,
			double [] m_D_T)//Anfangswerte zum Zeitpunkt Null
	{
		double m_D_T_p_T_cp_M_v[]=new double [8];
		m_D_T_p_T_cp_M_v[0]=m_D_T[0];
		m_D_T_p_T_cp_M_v[1]=m_D_T[1];
		m_D_T_p_T_cp_M_v[2]=m_D_T[2];
		m_D_T_p_T_cp_M_v[3]=zn.get_p();//Umgebungsdruck
		m_D_T_p_T_cp_M_v[4]=zn.get_T();//Umgebungstemp
		m_D_T_p_T_cp_M_v[5]=zn.get_ggZone().get_cp_mass(zn.get_T());
		m_D_T_p_T_cp_M_v[6]=zn.get_ggZone().get_M();
		m_D_T_p_T_cp_M_v[7]=v_relativ;

		double stepSize=finalTime/10;
		rk.setStepSize(stepSize);
		rk.setInitialValueOfX(startTime);    		
		rk.setInitialValueOfY(m_D_T_p_T_cp_M_v);
		rk.setFinalValueOfX(finalTime);
		double []y=rk.fourthOrder(this);
		if(y[0]<0||y[1]<0) {
			y[0]=0;
			y[1]=0;
		}	
		double m_D_T_Out []=new double [3];
		m_D_T_Out[0]=y[0];
		m_D_T_Out[1]=y[1];
		m_D_T_Out[2]=y[2];
		return m_D_T_Out;
	}


	public double [] get_dm_dD_dT(double time,double [] m_D_T, double v_relativ,Zone zn){
		double dm_dD_dT[]=new double [3];
		double m_D_T_p_T_cp_M_v[]=new double [8];
		m_D_T_p_T_cp_M_v[0]=m_D_T[0];
		m_D_T_p_T_cp_M_v[1]=m_D_T[1];
		m_D_T_p_T_cp_M_v[2]=m_D_T[2];
		m_D_T_p_T_cp_M_v[3]=zn.get_p();
		m_D_T_p_T_cp_M_v[4]=zn.get_T();
		m_D_T_p_T_cp_M_v[5]=zn.get_ggZone().get_cp_mass(zn.get_T());
		m_D_T_p_T_cp_M_v[6]=zn.get_ggZone().get_M();
		m_D_T_p_T_cp_M_v[7]=v_relativ;

		double temp[]=derivn(time,m_D_T_p_T_cp_M_v);
		dm_dD_dT[0]=temp[0];
		dm_dD_dT[1]=temp[1];
		dm_dD_dT[2]=temp[2];		

		return dm_dD_dT;
	}


	/**
	 * Liefert die Waermemenge die dem Tropfen zugefuehrt wird.
	 * Der Wert ist positiv sein.
	 * @param time
	 * @param m_D_T
	 * @param v_relativ
	 * @param zn
	 * @return dQ [J]
	 */
	public double get_dQ(double time,double [] m_D_T, double v_relativ,Zone zn){
		//Langsam aber sicher
		double m_D_T_p_T_cp_M_v[]=new double [8];
		m_D_T_p_T_cp_M_v[0]=m_D_T[0];
		m_D_T_p_T_cp_M_v[1]=m_D_T[1];
		m_D_T_p_T_cp_M_v[2]=m_D_T[2];
		m_D_T_p_T_cp_M_v[3]=zn.get_p();
		m_D_T_p_T_cp_M_v[4]=zn.get_T();
		m_D_T_p_T_cp_M_v[5]=zn.get_ggZone().get_cp_mass(zn.get_T());
		m_D_T_p_T_cp_M_v[6]=zn.get_ggZone().get_M();
		m_D_T_p_T_cp_M_v[7]=v_relativ;
		double diff_Q=derivn(time,m_D_T_p_T_cp_M_v)[8];
		return diff_Q;
	}

	//unschoen beugt aber Fehlern vor!!
	public double[] derivn(double time, double[] m_D_T_p_T_cp_M_v) {
		double[] dydx=new double[9];

		double mtr=m_D_T_p_T_cp_M_v[0];
		double Dtr=m_D_T_p_T_cp_M_v[1];
		double Ttr=m_D_T_p_T_cp_M_v[2];
		//Wenn der Tropfen vollst. verdampft ist koennen negative Werte auftreten
		if(mtr>0&&Dtr>0&&Ttr>0){
			double pZyl=m_D_T_p_T_cp_M_v[3];
			double TZyl=m_D_T_p_T_cp_M_v[4];	
			double cp_Luft=m_D_T_p_T_cp_M_v[5];
			double M_ggZyl=m_D_T_p_T_cp_M_v[6];
			double v_relativ=m_D_T_p_T_cp_M_v[7];
			double RZyl=PhysKonst.get_R_allg()/M_ggZyl;
			double rhoZyl=pZyl/TZyl/RZyl;	

			double rhoK=krstEigenschaften.get_rhoK(Ttr);
			double diff_rhoK=krstEigenschaften.get_diff_rhoK(Ttr);
			double cpl_krst=krstEigenschaften.get_cpl(Ttr);
			double cpf_krst=krstEigenschaften.get_cp(Ttr); 
			double L=krstEigenschaften.get_L(Ttr);




			double k=get_k_Grenzschicht(k_Luft,viskositaet_Luft,pZyl,Ttr,M_ggZyl);
			double Nu=get_Nu(rhoZyl,cp_Luft,k_Luft,viskositaet_Luft,pZyl,Ttr,Dtr,v_relativ,M_ggZyl);
			double Sh=get_Sh(rhoZyl,viskositaet_Luft,pZyl,Ttr,Dtr,v_relativ,M_ggZyl);
			double bm=get_bm(pZyl,Ttr,M_ggZyl);   

			double dm=Math.PI*Dtr*get_rho_Grenzschicht(rhoZyl,pZyl,Ttr,M_ggZyl)*diffusionsKoeffizient*Sh*Math.log(1+bm);
			double z=cpf_krst*dm/Math.PI/Dtr/k/Nu;
			double diff_Q=Math.PI*Dtr*k*(TZyl-Ttr)*Nu*z/Math.expm1(z);
			double dT=1/(mtr*cpl_krst)*(diff_Q-dm*L);
			double dD=2/(Math.PI*Dtr*Dtr*rhoK)*(-dm-Math.PI*Dtr*Dtr*Dtr/6*diff_rhoK*dT);			
				
			dydx[0]=-dm;
			dydx[1]=dD;
			dydx[2]=dT;
			dydx[3]=0;
			dydx[4]=0;
			dydx[5]=0;
			dydx[6]=0;
			dydx[7]=0;
			dydx[8]=diff_Q;

		}
		return dydx;
	}



	/**
	 * Liefert mass fraction of fuel vapour near the droplet surface
	 * (siehe: ELSEVIER:3327-3340)
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_yFS(double pZyl,double T_tropfen,double M_ggZyl){
		double yFS;//mass fraction of fuel vapour near the droplet surface
		double pS=krstEigenschaften.get_pS(T_tropfen);
		yFS=1/(1+(pZyl/pS-1)*M_ggZyl/M_Krst);
		return yFS;
	}


	private double get_yF_unendlich(){
		double yF_unendlich;
		yF_unendlich=0;
		return yF_unendlich;
	}

	/**
	 * Liefert Kraftstoffmassenbruch in der Grenzschicht
	 * (siehe J.Heat Mass Transfer. Vol.32,No.9,pp.1605-1618,1989)
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_yF(double pZyl,double T_tropfen,double M_ggZyl){
		double yF; //average fuel mass fraction
		yF=get_yFS(pZyl,T_tropfen,M_ggZyl)+(get_yF_unendlich()-get_yFS(pZyl,T_tropfen,M_ggZyl))/3;
		return yF;
	}


	/**
	 * Liefert die Dichte des Gasgemisches in der Grenzschicht in kg/m3 
	 * (siehe J.Heat Mass Transfer. Vol.32,No.9,pp.1605-1618,1989)
	 * @param rhoL
	 * @param pZyl
	 * @return
	 */
	private double get_rho_Grenzschicht(double rhoL,double pZyl,double T_tropfen,double M_ggZyl){
	
		double rho_Grenzschicht; 
//		double T_ref=(T_tropfen+693)/2;
//		double M=this.get_M(pZyl, T_tropfen, M_ggZyl);
//		rho_Grenzschicht=pZyl*M/PhysKonst.get_R_allg()/T_ref;
		//Mit fluessigem Kraftstoff gerechnet?
		double rhoK=krstEigenschaften.get_rhoK(T_tropfen);
		rho_Grenzschicht=1/(get_yF(pZyl,T_tropfen,M_ggZyl)/rhoK+(1-get_yF(pZyl,T_tropfen,M_ggZyl))/rhoL);
		return rho_Grenzschicht;
	}


	/**
	 * Liefert die Waermekapazitaet in der Grenzschicht
	 * (siehe J.Heat Mass Transfer. Vol.32,No.9,pp.1605-1618,1989)
	 * @param cp_Luft
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_cp_Grenzschicht(double cp_Luft,double pZyl,double T_tropfen,double M_ggZyl){
		double cp_Grenzschicht;
		double cpf_krst=krstEigenschaften.get_cp(T_tropfen);
		double yF=get_yF(pZyl,T_tropfen,M_ggZyl);
		cp_Grenzschicht=cpf_krst*yF+cp_Luft*(1-yF);
		return cp_Grenzschicht;
	}


	/**
	 * Liefert mittlere molare Masse in der Grenzschicht
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_M(double pZyl,double T_tropfen,double M_ggZyl){
		double M;
		M=1/(get_yF(pZyl,T_tropfen,M_ggZyl)/M_Krst+(1-get_yF(pZyl,T_tropfen,M_ggZyl))/M_ggZyl);
		return M;
	}

	/**
	 * Liefert Stoffmengenanteil von Kraftstoff
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_xF(double pZyl,double T_tropfen,double M_ggZyl){
		double xF;
		xF=get_yF(pZyl,T_tropfen,M_ggZyl)*get_M(pZyl,T_tropfen,M_ggZyl)/M_Krst;
		return xF;
	}

	/**
	 * Korrekturfaktor ergibt sich nach Mason und Saxena
	 * @param viskositaet_Luft
	 * @return
	 */
	private double get_korrekturFaktor(double viskositaet_Luft,double M_ggZyl){
		//Wo kommt diese Formel her?
		double korrekturFaktor;
		korrekturFaktor=0.5/Math.sqrt(2)/Math.sqrt(1+M_Krst/M_ggZyl)*
		Math.pow((1+Math.sqrt(viskositaet_krst/viskositaet_Luft)*Math.pow(M_ggZyl/M_Krst,0.25)),2);
		return korrekturFaktor;
	}

	private double get_viskositaet_Grenzschicht(double viskositaet_Luft,double pZyl,double T_tropfen,double M_ggZyl){
		double viskositaet_Grenzschicht;
		double xF=get_xF(pZyl,T_tropfen,M_ggZyl);
		viskositaet_Grenzschicht=(xF*viskositaet_krst+(1-xF)*viskositaet_Luft)/
		((1-xF)*get_korrekturFaktor(viskositaet_Luft,M_ggZyl));
		return viskositaet_Grenzschicht;
	}

	/**
	 * Waermeleitfaehigkeit in der Grenzschicht
	 * @param k_Luft
	 * @param viskositaet_Luft
	 * @param pZyl
	 * @param T_tropfen
	 * @param M_ggZyl
	 * @return
	 */
	private double get_k_Grenzschicht(double k_Luft,double viskositaet_Luft,double pZyl,
			double T_tropfen,double M_ggZyl){
		//Wo kommt diese Formel her??
		double k_Grenzschicht;
		double k_krst=krstEigenschaften.get_waermeLeit(T_tropfen);
		double xF=get_xF(pZyl,T_tropfen,M_ggZyl);
		k_Grenzschicht=(xF*k_krst+(1-xF)*k_Luft)/
		((1-xF)*get_korrekturFaktor(viskositaet_Luft,M_ggZyl));
		return k_Grenzschicht;
	}


	/**
	 * 
	 * @param rhoL
	 * @param viskositaet_Luft
	 * @param pZyl
	 * @param T_tropfen
	 * @param d_tropfen
	 * @param v_relativ=paketGeschwindigkeit-tropfenGeschwindigkeit
	 * @return
	 */
	private double get_Re(double rhoL,double viskositaet_Luft,double pZyl,
			double T_tropfen,double d_tropfen,double v_relativ,double M_ggZyl){
		double Re;
		Re=rhoL*v_relativ*d_tropfen/get_viskositaet_Grenzschicht(viskositaet_Luft,pZyl,T_tropfen,M_ggZyl);
		return Re;
	}

	private double get_Pr(double cp_Luft,double k_Luft,double viskositaet_Luft,
			double pZyl,double T_tropfen, double M_ggZyl){
		double Pr;
		Pr=get_cp_Grenzschicht(cp_Luft,pZyl,T_tropfen,M_ggZyl)*
		get_viskositaet_Grenzschicht(viskositaet_Luft,pZyl,T_tropfen,M_ggZyl)/
		get_k_Grenzschicht(k_Luft,viskositaet_Luft,pZyl,T_tropfen,M_ggZyl);
		return Pr;
	}

	private double get_Sc(double rhoL,double viskositaet_Luft,double pZyl,double T_tropfen,double M_ggZyl){
		double Sc;
		Sc=get_viskositaet_Grenzschicht(viskositaet_Luft,pZyl,T_tropfen,M_ggZyl)/
		get_rho_Grenzschicht(rhoL,pZyl,T_tropfen,M_ggZyl)/diffusionsKoeffizient;
		return Sc;
	}

	private double get_Nu(double rhoL,double cp_Luft,double k_Luft,double viskositaet_Luft,
			double pZyl,double T_tropfen,double d_tropfen,double v_relativ,double M_ggZyl){
		double Nu;
		Nu=2+0.6*Math.sqrt(get_Re(rhoL,viskositaet_Luft,pZyl,T_tropfen,d_tropfen,v_relativ,M_ggZyl))*
		Math.pow(get_Pr(cp_Luft,k_Luft,viskositaet_Luft,pZyl,T_tropfen,M_ggZyl),1/3d);
		return Nu;
	}

	private double get_Sh(double rhoL,double viskositaet_Luft,double pZyl,
			double T_tropfen,double d_tropfen,double v_relativ,double M_ggZyl){
		double Sh;
		Sh=2+0.6*Math.sqrt(get_Re(rhoL,viskositaet_Luft,pZyl,T_tropfen,d_tropfen,v_relativ,M_ggZyl))*
		Math.pow(get_Sc(rhoL,viskositaet_Luft,pZyl,T_tropfen,M_ggZyl),1/3d);
		return Sh;
	}

	private double get_bm(double pZyl,double T_tropfen,double M_ggZyl){
		double bm; //mass transfer number
		bm=(get_yFS(pZyl,T_tropfen,M_ggZyl)-get_yF_unendlich())/(1-get_yFS(pZyl,T_tropfen,M_ggZyl));
		return bm;
	}    

}
