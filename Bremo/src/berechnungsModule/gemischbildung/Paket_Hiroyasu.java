package berechnungsModule.gemischbildung;


import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor;
import bremo.main.Bremo;
import bremo.parameter.CasePara;
import misc.PhysKonst;
import misc.VektorBuffer;

public class Paket_Hiroyasu {
	
	private Verdampfung_Hiroyasu verdampf;
	private Kraftstoff_Eigenschaften krstEigenschaften;
	private int radialPosIndex, axialPosIndex;
	public double mfl_Paket_0; //Kraftstoffmasse eines Pakets
	public double paketErzeugungszeit;
//	private double rhoL_0;
	private double pZyl_0; //Druck bei der Erzeugung des Pakets
	private double pInj; //in Pa
	private double Tkrst_0;
	private double tEinspritzbeginn;//in s nach Simulationsbeginn
	private double d0; //in m
    private double gasKonstanteLuft; //in J/(Kg*K) 
	private double gasKonstanteKrst; //in J/(Kg*K)
	private double anzTropfen;
	private double mfl_tropfen_0;			//in [kg]
	public double m_D_T []=new double [3];
	private double rhoZyl_0;
	private double deltaP;
	private double v0; //Austrittgeschwindigkeit in m/s

	private double breakupZeit;
	private double rhoK_fl;
	private double cRad;
	private final CasePara CP;
	private final double Cd;
	private final double CW; //Frickelfaktor für Erhioehung des air entrainment bei Wandkontakt
	private VektorBuffer ma_buffer;
	

	
    public Paket_Hiroyasu(	int ax,
    						int rad,
    						double mfl_Paket_Eingabe,
    						double paketErzeugungszeit,
    						double pInj, 
    						double tEinspritzbeginn,
    						double Tkrst_0,
    						double cRad,
    						double d0,
    						double cd,
    						Kraftstoff_Eigenschaften krstEigenschaften,
    						CasePara cp){
    	this.CP=cp;
    	this.axialPosIndex=ax;	
		this.radialPosIndex = rad;
		this.mfl_Paket_0=mfl_Paket_Eingabe;
		this.paketErzeugungszeit=paketErzeugungszeit;		
		this.pInj=pInj;//in Pa
		this.tEinspritzbeginn=tEinspritzbeginn;	
		this.cRad=cRad;
		this.Tkrst_0=Tkrst_0;
		this.d0=d0;	
		this.Cd=cd;
		this.CW=1.555; //aus paper Hiroyasu
		
		this.ma_buffer=new VektorBuffer(CP);
		
		this.krstEigenschaften=krstEigenschaften;
		gasKonstanteLuft=PhysKonst.get_R_Luft(); //in J/(Kg*K)
	    gasKonstanteKrst=PhysKonst.get_R_allg()/krstEigenschaften.get_M_krst(); //in J/(Kg*K)  		    
	    //Berechnung der Dichte des fluessigen Kraftstoffs
	    rhoK_fl=krstEigenschaften.get_rhoK(Tkrst_0);
	  //Kraftstoffverdampfung
		verdampf=new Verdampfung_Hiroyasu(krstEigenschaften);	    
				
	}
	
    public void initialise(Zone zn){
    	this.pZyl_0=zn.get_p();    	
    	this.rhoZyl_0=pZyl_0/zn.get_T()/zn.get_ggZone().get_R();
//		
		this.deltaP=this.pInj-this.pZyl_0;
		//Berechnung des mittleren Sauter-Durchmessers	nach Hiroyasu	
		double smd=2.39e-3*Math.pow(deltaP,-0.135)*Math.pow(rhoZyl_0,0.12)*Math.pow(mfl_Paket_0*250/rhoK_fl,0.131);

		//Formel nach Stiesch 
		double viskositaet_krst=krstEigenschaften.get_mue_krst();
		double kin_viskositaet_krst=viskositaet_krst/rhoK_fl;	
		smd=0.5*6156e-6*Math.pow(kin_viskositaet_krst,0.385)*Math.pow(rhoK_fl,0.737)*Math.pow(rhoZyl_0,0.06)*
            Math.pow(deltaP/1000,-0.54);

		//Kraftstoffmass in einem einzelnen Tropfen
		mfl_tropfen_0=Math.PI*rhoK_fl*smd*smd*smd/6;
		//Tropfenanzahl
		anzTropfen=mfl_Paket_0/mfl_tropfen_0;
		//Berechnung der Breakuptime	nach Hiroyasu	
		breakupZeit=28.65*rhoK_fl*d0/Math.sqrt(rhoZyl_0*deltaP);

		//Berechnung der Breakuptime	nach Jung und Assanis SAE 2001-01-1246
		breakupZeit=4.351*rhoK_fl*d0/Cd/Cd/Math.sqrt(rhoZyl_0*deltaP);	
		
		v0=Cd*Math.sqrt(2*this.deltaP/this.rhoK_fl);
		
    	m_D_T[0]=mfl_tropfen_0;
    	m_D_T[1]=smd;
    	m_D_T[2]=Tkrst_0;     
    	
    }
    
	public int get_axialPosIndex(){
		return axialPosIndex;
	}
	
	
	public int get_radialPosIndex(){
		return radialPosIndex;
	}
    
	public double get_lifetime(double time){
		//lifetime beginnt wenn die Paketspitze aus dem Düsenloch guckt 
		//die lifetime wird aber nur zurück gegeben wenn das Paket vollstaendig erzeugt ist!
	    double lifetime;
	    if(time>=CP.get_Auslassoeffnet()){
	    	//Fuer die LWA
	    	lifetime=time-axialPosIndex*paketErzeugungszeit-(tEinspritzbeginn+CP.SYS.DAUER_ASP_SEC);
	    }else
	    	lifetime=time-axialPosIndex*paketErzeugungszeit-tEinspritzbeginn;
	    if(lifetime>=paketErzeugungszeit)
		    return lifetime;
	    else
		    return -1;
	} 
	
	public double get_breakupZeit(){		
		return breakupZeit;
	}
	
	public double get_sStrahlspitze(double time){		
		double sStrahlspitze=0;
		double tb=this.breakupZeit;  
		double lifetime=get_lifetime(time);
		if(lifetime>0 && lifetime<tb)
			sStrahlspitze = v0*lifetime;
		else if(lifetime>=tb){
			sStrahlspitze = 2.95*Math.pow(deltaP/rhoZyl_0,0.25)*Math.sqrt(d0*lifetime);
		}
		return sStrahlspitze;
	}
	
	public double get_s(double time){
		double s;   // Eindringtiefe am r-te Paket, m	
		s = get_sStrahlspitze(time)*Math.exp(cRad*radialPosIndex*radialPosIndex);		
		return s;
	}
	

    private double get_vStrahlspitze(double time){
    	double v = 0;      //Austrittsgeschwingdigkeit, m/s    	
    	double lifetime=get_lifetime(time);
    	double tb=this.breakupZeit;
    	if(lifetime>0 && lifetime<tb){
    		v = v0;
    	}
    	else if(lifetime>=tb){
    		v=1.475*Math.pow(deltaP*d0*d0/rhoZyl_0,0.25)/Math.sqrt(lifetime);
    	}
    	return v;
    }
    
    public double get_v(double time){
    	double lifetime=get_lifetime(time);
    	double tb=this.breakupZeit;
    	double v=-1;
    	if(lifetime>=tb)
    		v= get_vStrahlspitze(time)*Math.exp(cRad*radialPosIndex*radialPosIndex);	
    	else
    		v= get_vStrahlspitze(time);
    	return v;
    }
    
	/**
	 * Liefert die in ein Paket eingestroemte Luftmasse (air entrainment)
	 * @param time
	 * @return
	 */
    public double get_ma_Paket(double time){
    	//Berechnung ist umständlich aber nur so kann die Erhoehung 
    	//des air entrainment durch Wandkontakt richti beruecksichtigt werden
    	//Diese Formel ist bei Wandkontakt falsch bzw. entspricht nicht dem Modell von Hiroyasu
//    	double ma_Paket_alt=mfl_Paket_0*(v0/get_v(time)-1);
    	double lastChristmas=ma_buffer.get_maxTime();
    	double ma_Paket=0;

    	if(time<=lastChristmas)
    		return ma_buffer.getValue(time);
    	else{
    		if(get_lifetime(time)>0){
    			ma_Paket=ma_buffer.getValue(lastChristmas);
    			double dt=(time-lastChristmas)/10;
    			double t=lastChristmas;
    			for(int i=1;i<=10;i++){
    				t=lastChristmas+i*dt;
    				ma_Paket=ma_Paket+dt*this.get_dma_Paket(t);
    			}
    		}     		
    		ma_buffer.addValue(time, ma_Paket);
    	}
    	return ma_Paket;
    }
	
	/**
	 * Liefert das Differential der in ein Paket eingestroemten Luftmasse (air entrainment)
	 * @param time
	 * @return
	 */
	public double get_dma_Paket(double time){
    	double dma_Paket = 0; 
    	if(get_lifetime(time)>this.breakupZeit){
    		
    		//Ableitung aus ma=mf*(v0/(ds/dt) -1) --> ableiten nach dt --> dma=mf*v0/([ds/dt]^2)*d2s/dt2
    		dma_Paket=mfl_Paket_0*v0/Math.sqrt(get_lifetime(time))/(2.95*Math.pow(deltaP/rhoZyl_0,0.25)*Math.sqrt(d0))
    								/Math.exp(cRad*radialPosIndex*radialPosIndex);
//    		Bei Wandkontakt erhoeht sich das air entrainment 
    		if(this.get_s(time)>=0.5*CP.get_Bohrung())
    			dma_Paket=CW*dma_Paket; 
    		

//    		double delta_t=0.05*Bremo.get_casePara().SYS.WRITE_INTERVAL_SEC;
//    		double m1=this.get_ma_Paket(time+delta_t);
//    		double m2=this.get_ma_Paket(time);
//    		dma_Paket=(m1-m2)/delta_t;
    	}
    	
    	//Faktor hingedreht, dass es ungefaehr zur CFD-Passt und dass bei SOID=15°KW (20100714_12)
		//die im Premixed Peak freigesetzt Energie zur eingesaugten Luftmasse passt 
    	return 2*dma_Paket;
	}
	
	private double get_gasKonstantePaket(double time){
		double gasKonstanteGemisch;
		double mfg_Paket=get_mfg_Paket();
		double ma_Paket=get_ma_Paket(time);
		double yF = 0;
		double lifetime=get_lifetime(time);
		if(lifetime>0)
		    yF=mfg_Paket/(ma_Paket+mfg_Paket);
		gasKonstanteGemisch=(1-yF)*gasKonstanteLuft+yF*gasKonstanteKrst;
		return gasKonstanteGemisch;
	}
	
	
	/**
	 * liefert das Volumen des gasfoermigen Pakets
	 * @param rhoL in kg/m3
	 * @param time in s
	 * @param pZyl in N/m2
	 * @param T_Paket in K
	 * @return
	 */
	public double get_paketVolumen(Zone zn,double time,double T_Paket){
		double paketVolumen=0;
		double pZyl=zn.get_p();
		double mfg_Paket=get_mfg_Paket();
		double ma_Paket=get_ma_Paket(time);
		double volumenGasGemisch;
		double volumenFluessigerKraftstoff;
		double rhoK=krstEigenschaften.get_rhoK(m_D_T[2]);
		if(get_lifetime(time)>0){
			volumenGasGemisch=(mfg_Paket+ma_Paket)*get_gasKonstantePaket(time)*T_Paket/pZyl;
			volumenFluessigerKraftstoff=(mfl_Paket_0-mfg_Paket)/rhoK;
			paketVolumen=volumenGasGemisch+volumenFluessigerKraftstoff;
		}
		return paketVolumen;
	}
	
	public double get_smd(){
		return m_D_T[1] ;
	}	
	
	public double get_T_krstTropfen(){
		return m_D_T[2];
	}
	
	/**
	 * Liefert die Masse eines einzelnen Kraftstofftrofens
	 * @return
	 */
	public double get_m_krstTropfen(){
		return m_D_T[0];
	}
	

	public double get_mKrsEingespritzt(double time){
    	double mfl_Paket=0;
    	if(get_lifetime(time)>0)
    		mfl_Paket=mfl_Paket_0;
//    		mfl_Paket=m_D_T[0]*anzTropfen;
    	return mfl_Paket;
    }
	
	public double get_mKrstFl(double time){
    	double mfl_Paket=0;
    	if(get_lifetime(time)>0)
    		mfl_Paket=m_D_T[0]*anzTropfen;
    	return mfl_Paket;
    }
	
	
	private double get_mfg_Paket(){
    	double mfg_Paket=0;    	
    	return mfg_Paket;
    }

	
	public double get_dmKrst(double time,Zone zn){
		double dm=0;
		if(get_lifetime(time)>0&&this.verdampfungIsAbgeschlossen()==false){			
			double v_relativ=this.get_v(time)/3;
			dm=-1*this.verdampf.get_dm_dD_dT(time, m_D_T, v_relativ, zn)[0];
			if(((Double) dm).isNaN()) dm=0; //Passiert wennder Krst fast vollst. verdampft ist
			if(dm<0) 
				System.err.println("FUCK"); 
		}
		return dm*anzTropfen;
	}
	
	public double get_TKrst_dampf(double time, double TPaket){
		double Tkrst=0;
		if(get_lifetime(time)>0){
			//Formel nach Baumgarten Mixture Formation....Formel 4.190
			Tkrst= TPaket/3+m_D_T[2]*2/3;
			if(((Double) Tkrst).isNaN()) Tkrst=TPaket; //Passiert wenn der Krst fast vollst. verdampft ist
		}
			return Tkrst;
	}
	
	public double get_dQ(double time,Zone zn){
		double dQ=0;
		if(get_lifetime(time)>0&&this.verdampfungIsAbgeschlossen()==false){
			double v_relativ=this.get_v(time)/3;
			dQ= this.verdampf.get_dQ(time, m_D_T, v_relativ, zn);
			if(((Double) dQ).isNaN()) 
				dQ=0;
		}
		return dQ*anzTropfen;
	}
	
	
//	public double get_zuendVerzug(double time,double pZyl,double T){
//		double zuendVerzug;
//		zuendVerzug=4e-3*Math.pow(pZyl,-2.5)*Math.pow(get_mfg_Paket()/
//				    get_ma_Paket(time),-1.04)*Math.exp(4000/T);
//		return zuendVerzug;		
//	}
	
	
	public void berechneZeitschritt(Zone zn, double time, double nextTime){
		double delta_time=nextTime-time;	
		//Konvention: Zur Berechnung der Verdampfung wird meist ein Drittel der Paketgeschwindigkeit genommen
		double v_relativ=this.get_v(time)/3;
		if(get_lifetime(time)>0&&this.verdampfungIsAbgeschlossen()==false){			
			m_D_T=verdampf.get_m_D_T(zn,0,delta_time,v_relativ,m_D_T);	
			//kommt am Ende der Verdampfung schon mal vor
			if(((Double)m_D_T[0]).isNaN()||
					((Double)m_D_T[1]).isNaN()||
						((Double)m_D_T[2]).isNaN()){
				m_D_T[0]=m_D_T[1]=m_D_T[2]=0;
				System.err.println("FUCK");
			}
		}
	}
	
	/**
	 * Gibt true zurueck wenn die Verdampfung abgeschlossen ist
	 * @return
	 */
	public boolean verdampfungIsAbgeschlossen(){
		boolean a=true;
		if(m_D_T[0]>0&&m_D_T[1]>0)
			a=false;
		return a;
	}
	

	

	
	
}
