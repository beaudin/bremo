package berechnungsModule.mixtureFormation;

import java.util.Enumeration;
import java.util.Hashtable;

import berechnungsModule.Berechnung.Zone;
import matLib.DiefferetialEqationSys;
import matLib.RungeKutta;
import misc.PhysConst;

public class Evaporation implements DiefferetialEqationSys{
	private RungeKutta rk ;
	private FuelProperties krstEigenschaften;
	private double diffusionsKoeffizient;
	private double M_Krst;
	private double grenzschichtFaktor; 


	public Evaporation(FuelProperties krstEigenschaften){
		rk=new RungeKutta();		
		this.krstEigenschaften=krstEigenschaften;		
		M_Krst=krstEigenschaften.get_M_krst();
		//Da keine Daten zur Verfügung stehen wird erstmal mit luft gerechnet
		//TODO Spaeter sollten dei Waermeleitfaehigkeit und die viskositaet in die Klasse Spezies aufgenommen werden
		this.diffusionsKoeffizient=4.6e-7; //in m2/s nach Diss Haas
		grenzschichtFaktor=2/3d; //Eindrittelregel nach Habil. C. Baumgarten Formel 4.190
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
		double T_grenz=this.get_T_grenzschicht(m_D_T[2], zn.get_T());
		m_D_T_p_T_cp_M_v[0]=m_D_T[0];
		m_D_T_p_T_cp_M_v[1]=m_D_T[1];
		m_D_T_p_T_cp_M_v[2]=m_D_T[2];
		m_D_T_p_T_cp_M_v[3]=zn.get_p();//Umgebungsdruck
		m_D_T_p_T_cp_M_v[4]=zn.get_T();//Umgebungstemp
		m_D_T_p_T_cp_M_v[5]=zn.get_gasMixtureZone().get_cp_mass(T_grenz);
		m_D_T_p_T_cp_M_v[6]=zn.get_gasMixtureZone().get_M();
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
		double T_grenz=this.get_T_grenzschicht(m_D_T[2], zn.get_T());
		m_D_T_p_T_cp_M_v[0]=m_D_T[0];
		m_D_T_p_T_cp_M_v[1]=m_D_T[1];
		m_D_T_p_T_cp_M_v[2]=m_D_T[2];
		m_D_T_p_T_cp_M_v[3]=zn.get_p();
		m_D_T_p_T_cp_M_v[4]=zn.get_T();
		m_D_T_p_T_cp_M_v[5]=zn.get_gasMixtureZone().get_cp_mass(T_grenz);
		m_D_T_p_T_cp_M_v[6]=zn.get_gasMixtureZone().get_M();
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
		double T_grenz=this.get_T_grenzschicht(m_D_T[2], zn.get_T());
		m_D_T_p_T_cp_M_v[0]=m_D_T[0];
		m_D_T_p_T_cp_M_v[1]=m_D_T[1];
		m_D_T_p_T_cp_M_v[2]=m_D_T[2];
		m_D_T_p_T_cp_M_v[3]=zn.get_p();
		m_D_T_p_T_cp_M_v[4]=zn.get_T();
		m_D_T_p_T_cp_M_v[5]=zn.get_gasMixtureZone().get_cp_mass(T_grenz);
		m_D_T_p_T_cp_M_v[6]=zn.get_gasMixtureZone().get_M();
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
			double cp_Zyl=m_D_T_p_T_cp_M_v[5]; //cp des ZylinderGases bei T_grenzschicht!
			double M_ggZyl=m_D_T_p_T_cp_M_v[6];
			double v_relativ=m_D_T_p_T_cp_M_v[7];
			double RZyl=PhysConst.get_R_allg()/M_ggZyl;
			double rhoZyl=pZyl/TZyl/RZyl;
			
			//Eigenschaften im Tropfen
			double rhoK=krstEigenschaften.get_rhoK(Ttr);
			double diff_rhoK=krstEigenschaften.get_diff_rhoK(Ttr);
			double cpl_krst=krstEigenschaften.get_cpl(Ttr);
			double L=krstEigenschaften.get_L(Ttr);
			
			double T_grenzschicht=this.get_T_grenzschicht(Ttr, TZyl);
			double cpf_krst=krstEigenschaften.get_cp(T_grenzschicht); 		

			
			//Stoffwerte in der Grenzschicht
			//Waremeleitfaehigkeit 
			double k_krst=krstEigenschaften.get_waermeLeit(T_grenzschicht);
			double k_Luft=krstEigenschaften.get_waermeLeitLuft(T_grenzschicht);			
			
			//dynamische Viskosität
			double dynVis_krst=krstEigenschaften.get_dynVis_krst(T_grenzschicht);
			double dynVis_Luft=krstEigenschaften.get_dynVisLuft(T_grenzschicht);
			
			
			Double dynVis_waermeLeit_M_Luft[]={dynVis_Luft,k_Luft,M_ggZyl};
			Double dynVis_waermeLeit_M_Krst []={dynVis_krst,k_krst,M_Krst};			
			
			//Molenbruch von Krst in der Grenzschicht
			double xF=this.get_xF(pZyl, Ttr);
			Hashtable<Double,Double[]> molenBruch_dynVis_waermeLeit_M=new Hashtable<Double,Double[]>();
			molenBruch_dynVis_waermeLeit_M.put(xF,dynVis_waermeLeit_M_Krst);
			molenBruch_dynVis_waermeLeit_M.put(1-xF,dynVis_waermeLeit_M_Luft);
			
			
			double dynVis_waermeLeit_Grenz []=get_dynVis_WaermeLeit_Grenzschicht(molenBruch_dynVis_waermeLeit_M);
			
			double dynVis_Grenzschicht=dynVis_waermeLeit_Grenz[0];
			double k_Grenzschicht=dynVis_waermeLeit_Grenz[1];			

			double yF=this.get_yF(pZyl, Ttr, M_ggZyl);
			double cp_Grenzschicht=yF*cpf_krst+(1-yF)*cp_Zyl; 
			
			//Liefert fast identische Ergebnisse wie die komplizierte Berechnung!
			double dynVis_Grenzschicht_vgl=xF*dynVis_krst+
			(1-xF)*krstEigenschaften.get_dynVisLuft(T_grenzschicht);
	
			double k_Grenzschicht_vgl=xF*k_krst+
				(1-xF)*krstEigenschaften.get_waermeLeitLuft(T_grenzschicht);
			

			double rho_Grenzschicht=this.get_rho_Grenzschicht(pZyl, Ttr, TZyl, M_ggZyl);

			double Nu=this.get_Nu(rhoZyl, cp_Grenzschicht, k_Grenzschicht, dynVis_Grenzschicht, Dtr, v_relativ);
			double Sh=this.get_Sh(rhoZyl, dynVis_Grenzschicht, Dtr, v_relativ, rho_Grenzschicht);
			
			double yF_unendlich=this.get_yF_unendlich();
			double yFS=this.get_yFS(pZyl, Ttr, M_ggZyl);
			double bm=this.get_bm(yFS, yF_unendlich); 
			double dm=Math.PI*Dtr*rho_Grenzschicht*diffusionsKoeffizient*Sh*Math.log(1+bm);
			
			double z=cp_Grenzschicht*dm/Math.PI/Dtr/k_Grenzschicht/Nu;
			double diff_Q=Math.PI*Dtr*k_Grenzschicht*(TZyl-Ttr)*Nu*z/Math.expm1(z);
			double dT=(1/(mtr*cpl_krst))*(diff_Q-dm*L);
			
			double dD=(2/(Math.PI*Dtr*Dtr*rhoK))*(-dm-Math.PI*Dtr*Dtr*Dtr/6*diff_rhoK*dT);			
				
			dydx[0]=-dm;
			dydx[1]=dD;
			dydx[2]=dT;
			dydx[3]=0;
			dydx[4]=0;
			dydx[5]=0;
			dydx[6]=0;
			dydx[7]=0;
			dydx[8]=diff_Q;	//Wird zur Ausgabe der Verdampfungsenthalpie benoetigt
		}
		return dydx;	
	}

	
	/**
	 * Liefert die Temperatur in der Grenzschicht
	 * @return
	 */
	private double get_T_grenzschicht(double T_tropfen, double T_umgebung){
		return grenzschichtFaktor*T_tropfen+(1-grenzschichtFaktor)*T_umgebung;		
	}
	
	/**
	 * Liefert Stoffmengenanteil von Kraftstoff auf der Tropfenoberflaeche
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_xFS(double pZyl,double T_tropfen){
		double xF;
		double pS=krstEigenschaften.get_pS(T_tropfen);		
		xF=pS/pZyl;
		return xF;
	}
	
	/**
	 * Liefert Stoffmengenanteil von Kraftstoff im umgebenden Gas.
	 * (Wird auf Null gesetzt)
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_xF_unendlich(double pZyl,double T_tropfen){
		return 0;
	}
	
	/**
	 * Liefert Stoffmengenanteil von Kraftstoff in der Grenzschicht
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_xF(double pZyl,double T_tropfen){
		double xF;
		xF=grenzschichtFaktor*this.get_xFS(pZyl, T_tropfen)+
		(1-grenzschichtFaktor)*this.get_xF_unendlich(pZyl, T_tropfen);
		return xF;
	}
	
	/**
	 * Liefert mittlere molare Masse in der Grenzschicht
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_M(double pZyl,double T_tropfen,double M_ggZyl){
		double M;
		double xF=this.get_xF(pZyl, T_tropfen);
		M=xF*this.M_Krst+(1-xF)*M_ggZyl; 
		return M;
	}


	/**
	 * Liefert den Massenbruch von Kraftstoff an der Tropfenoberflaeche
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_yFS(double pZyl,double T_tropfen,double M_ggZyl){
		double yFS;//mass fraction of fuel vapour near the droplet surface
		double M=this.get_M(pZyl, T_tropfen, M_ggZyl);
		yFS=this.get_xFS(pZyl, T_tropfen)*M_Krst/M;
		return yFS;
	}

	/**
	 * Liefert Massenbruch von Kraftstoff im umgebenden Gas.
	 * Ist zu null gesetzt
	 * @return
	 */
	private double get_yF_unendlich(){
		double yF_unendlich;
		yF_unendlich=0;
		return yF_unendlich;
	}

	/**
	 * Liefert Kraftstoffmassenbruch in der Grenzschicht
	 * @param pZyl
	 * @param T_tropfen
	 * @return
	 */
	private double get_yF(double pZyl,double T_tropfen,double M_ggZyl){
		double yF; //average fuel mass fraction
		double M=this.get_M(pZyl, T_tropfen, M_ggZyl);
		yF=get_xF(pZyl,T_tropfen)*M_Krst/M; 
		//identisch mit: 
//		yF=grenzschichtFaktor*get_xFS(pZyl,T_tropfen)*M_Krst/M; 
		return yF;
	}
	
	/**
	 * Korrekturfaktor  nach Mason und Saxena  
	 * entnommen aus VDI Waermeatlas 10. Auflage 2006 Seite Da 23 Formel 98a 
	 * bzw. Seite Da 26 Formel 108a
	 * @return
	 */
	private double get_F_ij(double dynVis_i, double M_i, double dynVis_j,double M_j  ){
		double korrekturFaktor;
		korrekturFaktor=
		Math.pow((1+Math.sqrt(dynVis_i/dynVis_j)*Math.pow(M_j/M_i,0.25)),2)/Math.sqrt(8*(1+M_i/M_j));
		return korrekturFaktor;
	}
	
	private double get_sumF_ij(Hashtable <Double,Double []> molenbruch_dynVis_waermeLeit_M, double dynVis_i, double M_i){
		double sumF_ij=0;
		Enumeration<Double> e = molenbruch_dynVis_waermeLeit_M.keys();			
		while(e.hasMoreElements() ){	
			double molenbruch_j=e.nextElement();
			double dynVis_j=molenbruch_dynVis_waermeLeit_M.get(molenbruch_j)[0];
			double M_j=molenbruch_dynVis_waermeLeit_M.get(molenbruch_j)[2];
				sumF_ij=sumF_ij+molenbruch_j*this.get_F_ij(dynVis_i, M_i,dynVis_j,M_j);
			}				
		return sumF_ij;		
	}
	
	/**
	 * Formel aus VDI Waermeatlas 10. Auflage 2006 Seite Da 23 Formel 98 
	 * bzw. Seite Da 26 Formel 108
	 * @param molenBruch_dynVis_waermeLeit_M
	 * @return
	 */
	private double[] get_dynVis_WaermeLeit_Grenzschicht(Hashtable <Double,Double[]> molenBruch_dynVis_waermeLeit_M){
		double dynVisGrenz=0;
		double waermeLeitGrenz=0;	
		
		Enumeration<Double> e2 = molenBruch_dynVis_waermeLeit_M.keys();	
		while(e2.hasMoreElements() ){	
			double molenbruch_i=e2.nextElement();
			double dynVis_i=molenBruch_dynVis_waermeLeit_M.get(molenbruch_i)[0];
			double waermeLeit_i=molenBruch_dynVis_waermeLeit_M.get(molenbruch_i)[1];
			double M_i=molenBruch_dynVis_waermeLeit_M.get(molenbruch_i)[2];
			double 	sumF_ij=this.get_sumF_ij(molenBruch_dynVis_waermeLeit_M, dynVis_i, M_i);
			
			dynVisGrenz=dynVisGrenz+molenbruch_i*dynVis_i/(sumF_ij);
			
			waermeLeitGrenz=waermeLeitGrenz+molenbruch_i*waermeLeit_i/sumF_ij;
			}				
		double [] dynVis_wl={dynVisGrenz,waermeLeitGrenz};
		return dynVis_wl;
	}
	
	


	/**
	 * Liefert die Dichte des Gasgemisches in der Grenzschicht in kg/m3 
	 * (siehe J.Heat Mass Transfer. Vol.32,No.9,pp.1605-1618,1989)
	 * @param rhoL
	 * @param pZyl
	 * @return
	 */
	private double get_rho_Grenzschicht(double pZyl,double T_tropfen,double T_umgebung,
			double M_ggZyl){		
		double rho_Grenzschicht;  
		double T_ref=this.get_T_grenzschicht(T_tropfen, T_umgebung);
		double M=this.get_M(pZyl, T_tropfen, M_ggZyl);
		rho_Grenzschicht=pZyl*M/PhysConst.get_R_allg()/T_ref;
		return rho_Grenzschicht;
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
	private double get_Re(double rhoZyl,double v_relativ,
			double d_tropfen,double dynViskositaetGrenz){
		double Re;
		Re=rhoZyl*v_relativ*d_tropfen/dynViskositaetGrenz;
		return Re;
	}

	private double get_Pr(double dynViskositaetGrenz,double cpGrenz,double waermeleitGrenz){
		double Pr;
		Pr=cpGrenz*dynViskositaetGrenz/waermeleitGrenz;
		return Pr;
	}

	private double get_Sc(double rhoGrenzschicht,double dynViskositaetGrenz){
		double Sc;
		Sc=dynViskositaetGrenz/rhoGrenzschicht/diffusionsKoeffizient;
		return Sc;
	}

	private double get_Nu(double rhoZyl,double cpGrenz,double waermeleitGrenz,double dynViskositaetGrenz,
			double d_tropfen,double v_relativ){
		double Nu;
		double Re=get_Re(rhoZyl,v_relativ,d_tropfen,dynViskositaetGrenz);
		double Pr=get_Pr(dynViskositaetGrenz,cpGrenz,waermeleitGrenz);
		Nu=2+0.6*Math.sqrt(Re)*Math.pow(Pr,1/3d);
		return Nu;
	}

	private double get_Sh(double rhoZyl,double dynViskositaetGrenz,
			double d_tropfen,double v_relativ, double rhoGrenzschicht){
		double Sh;
		double Re=this.get_Re(rhoZyl, v_relativ, d_tropfen, dynViskositaetGrenz);
		double Sc=this.get_Sc(rhoGrenzschicht, dynViskositaetGrenz);
		Sh=2+0.6*Math.sqrt(Re)*Math.pow(Sc,1/3d);
		return Sh;
	}

	private double get_bm(double yFS,double yF_unendlich){
		double bm; //mass transfer number
		bm=(yFS-yF_unendlich)/(1-yFS);
		return bm;
	}    

}

