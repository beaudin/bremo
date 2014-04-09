package berechnungsModule.Berechnung;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Hashtable;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import bremoExceptions.NegativeMassException;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;



public class Zone {

	private double T_Zone=-5;			//[K]
	private double T_max_Zone=-5;		//[K]
	private double m_Zone=-5; 			//[kg]
	private double V_Zone=-5; 			//[m3]
	private double p_Zone=-5;			//[N/m2]	
	//		private double mi[];				//Einzelmassen der Zone in [kg]
	//		private double dmi[];				//Diferentiale der Einzelmassen in [kg/s]

	private double sumdQ,dudp,dudT, dRdT, dRdp,sumdH,sumdU;

	private final int ID; 
	private GasGemisch gg_Zone; 
	private final boolean burns;

	//		//Hashtable mit den Änderungen der Massenbrüche mit der Temperatur aufgrund von Dissoziation
	//		Hashtable<Spezies,Double> dmj_dT;
	//
	//		//Hashtable mit den Änderungen der Massenbrüche mit dem Druck aufgrund von Dissoziation
	//		Hashtable<Spezies,Double> dmj_dp;

	//Hashtable mit den Änderungen der Massenbrüche aufgrund von ein- und ausströmenden Massen
	Hashtable<Spezies,Double> dmj_ein,dm_aus;

	// gibt an ob es sich um eine Zone mit Rauchgas handelt
	boolean loeseChemGleichgwicht=false;

	private final GleichGewichtsRechner GG_SOLVER; 		
	private final CasePara CP;


	/**
	 * Erzeugt eine Zonen mit den angegebenen Startparametern
	 * @param p_init
	 * @param V_init
	 * @param T_init
	 * @param m_init
	 * @param gg GasGemsich oder Spezies
	 * @param burns
	 * @param id
	 */
	public Zone(CasePara cp,double p_init,double V_init, double T_init, double m_init,
			Spezies gg,boolean burns, int id ) {	

		CP=cp;

		//			if(Bremo.get_casePara().get_zonenVerwaltung().existsID(id))
		//				throw new IllegalArgumentException("Zone: " +
		//				"Es wurde versucht eine Zone mit einer bereits bestehenden ID zu erzeugen");	

		this.GG_SOLVER=cp.OHC_SOLVER;
		this.ID=id;//TODO ZonenID automatisch Verwalten?
		this.gg_Zone=new GasGemisch("ggZone"+id);
		this.burns=burns;

		Hashtable<Spezies, Double> einzelMassen =new Hashtable<Spezies, Double>();

		if(gg.isGasGemisch()&&!CP.SPEZIES_FABRIK.isToIntegrate(gg)){
			einzelMassen=this.get_einzelMassenHash(
					((GasGemisch)gg).get_speziesMassenBruecheDetailToIntegrate(), m_init);
		}else{				
			einzelMassen.put(gg, m_init);
		}
		this.set_p_V_T_mi(p_init, V_init, T_init,einzelMassen);
	}		

	/**
	 * Erzeugt eine Zone mit den angegebenen Werten fuer p,V,T und den Einzelmassen.
	 * Die Position der Einzelmassen ergibt sich aus der Klasse Spezies, hier wird jeder Spezies,
	 * die integreiert werden soll eien Position zugewiesen. 
	 * Diese funktion sollte nur vom Solver aufgerufen werden.
	 * @param p_V_T_mi Vektor mit p, V, T und den Einzelmassen
	 * @param burns gibt an ob chemGlgw geloest werden soll --> aka verbrannte Zone
	 * @param id 
	 */
	public Zone(CasePara cp, double [] p_V_T_mi,	boolean burns, int id) {
		this.CP=cp;
		this.ID=id;
		this.GG_SOLVER=CP.OHC_SOLVER;		
		this.gg_Zone=new GasGemisch("ggZone"+id);
		this.burns=burns;
		this.set_p_V_T_mi(p_V_T_mi);
	}

	public void set_p_V_T_mi(double [] p_V_T_mi){

		if(p_V_T_mi.length!=(CP.SPEZIES_FABRIK.get_nbrOfSpecies()+3)){
			try{
				throw new BirdBrainedProgrammerException("NmbrOfAllSpez " +
				"entspricht nicht der Anzahl der Spezies in dieser Zone");
			}catch(BirdBrainedProgrammerException e){
				e.stopBremo();
			}
		}

		double p_init=p_V_T_mi[0];
		double V_init=p_V_T_mi[1];
		double T_init=p_V_T_mi[2];
		Hashtable<Spezies, Double> miHT=new Hashtable<Spezies, Double>();

		for(int i=0; i<p_V_T_mi.length-3;i++) {
			if(p_V_T_mi[i+3]!=0){ //Abfrage ob mi <0 kommt in "set_p_V_T_mi(p_init,V_init,T_init,miHT)"
				miHT.put(CP.SPEZIES_FABRIK.get_Spez(i), p_V_T_mi[i+3]);	
			}
		}

		set_p_V_T_mi(p_init,V_init,T_init,miHT);				
	}





	public void set_p_V_T_mi(double p_init,double V_init, double T_init,Hashtable<Spezies, Double> mi){			
		this.p_Zone=p_init;
		this.V_Zone=V_init;
		this.T_Zone=T_init;
		if(p_Zone<=0||V_Zone<=0||T_Zone<=0||(((Double) p_Zone).isNaN())
				||(((Double) T_Zone).isNaN())||(((Double) V_Zone).isNaN())){				
			try{
				throw new NegativeMassException("Falsche Werte in Zone " + 
						this.ID + "\n V= " + V_Zone + "\n p= " + p_Zone+ "\n T= " + T_Zone );
			}catch(NegativeMassException nmE){
				nmE.log_Warning();	
			}
		}		
			if(T_Zone>T_max_Zone)
				T_max_Zone=T_Zone;

			//Überprüfen ob die Berechnung der Dissoziation Sinn macht
			if(T_max_Zone>=GG_SOLVER.get_T_Freeze()&& burns)
				loeseChemGleichgwicht=true; 



			//Berechnen der Gesamtmasse
			double mGes=0.0D;
			double mi_;
			Enumeration<Spezies> e=mi.keys();
			Spezies spez; 			
			while(e.hasMoreElements()){
				spez=e.nextElement();
				mi_=mi.get(spez);
				if(mi_<0){	
					if(Math.abs(mi_)>=0.00001*CP.SYS.MINIMALE_ZONENMASSE){
						try{
							throw new NegativeMassException(spez.get_name() +" in Zone " + 
									this.ID+" hatte eine negative Masse ("
									+mi_+"kg). Die Masse wurde auf null gesetzt!");
						}catch(NegativeMassException nmE){						
							nmE.log_Warning();	
						}											
					}
					mi_=0; 
					mi.put(spez,mi_);	
				}
				mGes=mGes+mi_;
			}
			spez=null;
			e=null;		
			this.m_Zone=mGes;			

			if(this.m_Zone>0){
				//Berechnen der Massenbrueche
				Hashtable<Spezies, Double> massenBrueche =new Hashtable<Spezies, Double>();
				e=mi.keys();
				double xi;
				while(e.hasMoreElements()){
					spez=e.nextElement();	
					xi=mi.get(spez)/mGes;
					massenBrueche.put(spez,xi );
				}		
				gg_Zone.set_Gasmischung_massenBruch(massenBrueche);				
				//Berechnen der Dissoziation			
				if(loeseChemGleichgwicht==true)
					gg_Zone.set_Gasmischung_molenBruch(GG_SOLVER.get_GG_molenBrueche(p_Zone, T_Zone, gg_Zone));	

			}else{
				Hashtable<Spezies, Double> massenBrueche =new Hashtable<Spezies, Double>();
				Spezies co2=CP.SPEZIES_FABRIK.get_spezCO2();
				massenBrueche.put(co2,1D); //TODO irgendwie uncool
				gg_Zone.set_Gasmischung_massenBruch(massenBrueche);	
			}
			
			double pV=p_Zone*V_Zone;						
			double mRT=m_Zone*gg_Zone.get_R()*T_Zone;  
			//This accounts for inaccuracy during the integration process. 
			//The differences in temperature should be very very small!!
			if(pV!=mRT&&m_Zone>0&&V_Zone>0){	
			//	System.out.println("(pV-mRT)/pV: "+(pV-mRT)/pV*100);
				double T_temp=pV/m_Zone/gg_Zone.get_R();				
				double chkPv=pV-m_Zone*gg_Zone.get_R()*T_temp;
				double chkT=Math.abs((T_Zone-T_temp)/T_temp);
				this.T_Zone=T_temp;
				if(chkT>7.555e-3&&m_Zone>CP.SYS.MINIMALE_ZONENMASSE){
					try{
						throw new MiscException("The temperature of  zone  "+ this.ID+" had to be adjusted too much! " +
								"chkT= "+chkT);
					}catch(MiscException me){
						me.log_Warning();
					}
				}				
			}
			
			dmj_ein=new Hashtable<Spezies, Double>();
			dm_aus=new Hashtable<Spezies, Double>();	
			sumdQ=0;
			sumdH=0;
			sumdU=0;
			dudp=this.calc_dudp_dRdp()[0];
			dRdp=this.calc_dudp_dRdp()[1];
			dudT=this.calc_dudT_dRdT()[0];
			dRdT=this.calc_dudT_dRdT()[1];				
	}	



	/**
	 * Berechnet den Massenverlust einer Zone in dem das gesamte Gas der Zone ausstroemt (z.B. Blow By)
	 * @param dm_aus [kg] muss groeser Null sein
	 * @throws NegativeMassException 
	 */
	public void set_dm_aus(double dm_aus) throws NegativeMassException{
		set_dm_aus(dm_aus,this.gg_Zone);
	}	


	/**
	 * Berechnet den Massenverlust bei entnahme einer BESTIMMTEN Spezies
	 * Dieser Aufruf sollte nur vom einzonigen Modell benoetigt werden
	 * @param dm_a [kg] muss groeser Null sein
	 * @throws NegativeMassException 
	 */
	public void set_dm_aus(double dm_a,Spezies spez) throws NegativeMassException{		
		if(dm_a<0)
			throw new IllegalArgumentException("\"set_dm_aus()\"  wurde fuer Zone "+ 
					ID+ " mit einem negativen Massenstrom aufgerufen");
		if(dm_a>0){				
			//Hinzufügen der Einzelmassen der entnommenen Spezies zur Hashtable dmj
			Hashtable<Spezies, Double>einzelMassenHash_aus = new Hashtable<Spezies,Double>();
			if(spez.isGasGemisch()&& CP.SPEZIES_FABRIK.isToIntegrate(spez)==false){
				Hashtable<Spezies, Double> massenBruchHash_aus=	
					((GasGemisch)spez).get_speziesMassenBruecheDetailToIntegrate();
								

				//Erstellen der Hashtable mit den Einzelmassen der Grundspezies (CO2, O2, usw....					
				einzelMassenHash_aus=this.get_einzelMassenHash(massenBruchHash_aus, dm_a);
				
			}else{
				einzelMassenHash_aus.put(spez,dm_a);
			}
			//Hinzufügen der Änderung der Grundspeziesmassen
			Enumeration<Spezies> e=einzelMassenHash_aus.keys();			
			Spezies spez1;
			while(e.hasMoreElements()){
				spez1=e.nextElement();
				if(!CP.SPEZIES_FABRIK.isToIntegrate(spez1)){
					try{
						throw new BirdBrainedProgrammerException("Aus Zone " +this.ID + " wurde eine Spezies "
								+"("+ spez1.get_name()+ ") entnommen die nicht integriert werden soll"+
						" (\"isToIntegrate()\" liefert false)");
					}catch(BirdBrainedProgrammerException bbpe){
						bbpe.stopBremo();
					}
				}
				if(!gg_Zone.get_speziesMassenBruecheDetailToIntegrate().containsKey(spez1)&&
						einzelMassenHash_aus.get(spez1)>0) //Es kann sein, dass Spezies mit einem Massenbruch von 0 in der Hashtable stehen
					throw new NegativeMassException("Aus Zone " +this.ID + " soll eine Spezies" +
							"("+ spez1.get_name()+ ") entnommen werden " +
					"die in der Zone nicht vorhanden ist");		
			}
			
			e=null;
			e=einzelMassenHash_aus.keys();	
			spez1=null;
			while(e.hasMoreElements()){
				spez1=e.nextElement();					
				if(this.dm_aus.containsKey(spez1)){
					this.dm_aus.put(spez1, einzelMassenHash_aus.get(spez1)+dm_aus.get(spez1));			
				}else{				
					this.dm_aus.put(spez1, einzelMassenHash_aus.get(spez1));	
				}
			}
			
//			e=null;
//			e=einzelMassenHash_aus.keys();	
//			spez1=null;
//			double dmCheck=0;
//			while(e.hasMoreElements()){
//				spez1=e.nextElement();	
//				dmCheck+=einzelMassenHash_aus.get(spez1);				
//			}
//			if(dmCheck!=dm_a)
//				System.out.println("Zone.set_dm_aus: dm_a !=dmCheck " + ((dmCheck-dm_a)) );
			
			// das hier muss nach der NegativeMassException stehen!! Wenn die Exception geworfen 
			// wird und dieser Code ist schon ausgefuehrt wuerde die Energie aber nicht dei Masse beruecksichtigt 
			double dU=spez.get_u_mass(T_Zone)*dm_a;
			sumdU=sumdU-dU;
			double dH=spez.get_h_mass(T_Zone)*dm_a;
			sumdH=sumdH-dH;			
		}			

	}


	public void set_dm_ein(double dm_zu, double T_zu, Spezies spez_zu){
		if(dm_zu<0){
			throw new IllegalArgumentException("Es wurde versucht die Methode \"set_massenstrom_ein()\" "
					+ "mit einen negativen Massenstrom aufzurufen");
		}
		if(dm_zu!=0){//wenn die Masse null ist mach garnichts				

			//Berechnen des Energiestroms
			double dU=spez_zu.get_u_mass(T_Zone)*dm_zu;			
			sumdU=sumdU+dU;
			double dH=spez_zu.get_h_mass(T_zu)*dm_zu;
			sumdH=sumdH+dH;	

			//Hinzufügen der Einzelmassen der zugefügten Spezies zur Hashtable dmj
			Hashtable<Spezies, Double>einzelMassenHash_zu = new Hashtable<Spezies,Double>();
			if(spez_zu.isGasGemisch()&& CP.SPEZIES_FABRIK.isToIntegrate(spez_zu)==false){

				Hashtable<Spezies, Double> massenBruchHash_zu=	
					((GasGemisch)spez_zu).get_speziesMassenBruecheDetailToIntegrate();
								
				//Erstellen der Hashtable mit den Einzelmassen der Grundspezies (CO2, O2, usw....					
				einzelMassenHash_zu=this.get_einzelMassenHash(massenBruchHash_zu, dm_zu);					

			}else{
				einzelMassenHash_zu.put(spez_zu,dm_zu);
			}

			//Hinzufügen der Änderung der Grundspeziesmassen
			Enumeration<Spezies> e=einzelMassenHash_zu.keys();			
			Spezies spez;
			while(e.hasMoreElements()){
				spez=e.nextElement();
				if(!CP.SPEZIES_FABRIK.isToIntegrate(spez)){
					try{
						throw new BirdBrainedProgrammerException("In Zone " +this.ID + " wurde eine Spezies "
								+"("+ spez.get_name()+ ") eingebracht die nicht integriert werden soll"+
						" (isToIntegrate() liefert false)");
					}catch(BirdBrainedProgrammerException bbpe){
						bbpe.stopBremo();
					}
				}
				if(dmj_ein.containsKey(spez)){
					dmj_ein.put(spez, einzelMassenHash_zu.get(spez)+dmj_ein.get(spez));			
				}else{				
					dmj_ein.put(spez, einzelMassenHash_zu.get(spez));	
				}
			}
//			e=null;
//			e=einzelMassenHash_zu.keys();			
//			spez=null;
//			double dmCheck=0;
//			while(e.hasMoreElements()){
//				spez=e.nextElement();
//				dmCheck+=einzelMassenHash_zu.get(spez);				
//			}
//			if(dmCheck!=dm_zu)
//				System.out.println("Zone.set_dm_ein: dm_zu !=dmCheck " +((dmCheck-dm_zu)));			
		}
	}



	public void set_dQ_ein_aus(double dQ_zu){
		sumdQ=sumdQ+dQ_zu;
	}


	private  double[] calc_dudT_dRdT(){
		double [] du_dt_dR_dT=new double [2];
		double delta_T=1;		
		//if-Abfrage nur dann sinnvoll wenn Einfriertemperatur unter 1000K 
		//oder wenn Luft als Rauchgas mit sehr gro§em Lambda modelliert wird
		if (T_Zone > 1000 && T_Zone <= 1001) {
			delta_T=-1;
		} 				
		//Berechnen der kalorischen Daten für Rauchgas
		if(loeseChemGleichgwicht==true && T_Zone>=GG_SOLVER.get_T_Freeze() ){
			GasGemisch zoneGG =
				new GasGemisch(GG_SOLVER.get_GG_molenBrueche(p_Zone, T_Zone, gg_Zone),"zoneGG"+ID);
			GasGemisch rauchgas =
				new GasGemisch(GG_SOLVER.get_GG_molenBrueche(p_Zone, T_Zone-delta_T, gg_Zone),"rauchgasZone"+ID);

			//ist zwar umstaendlich aber ich hab schon alles mit Java erlebt :-(
			double a=(zoneGG.get_u_mass(T_Zone)-rauchgas.get_u_mass(T_Zone-delta_T));
			du_dt_dR_dT[0]=a/delta_T;

			a=(zoneGG.get_R()-rauchgas.get_R());
			du_dt_dR_dT[1]=a/delta_T;
		}else{
			du_dt_dR_dT[0]=gg_Zone.get_cv_mass(T_Zone); //hier ändert sich die Zusammensetzung des Gases nicht mehr
			du_dt_dR_dT[1]=0;
		}			
		return du_dt_dR_dT;
	}	


	private double[] calc_dudp_dRdp(){
		double [] du_dp_dR_dp=new double [2];

		double delta_p=5000;				
		//Berechnen der kalorischen Daten für Rauchgas
		if(loeseChemGleichgwicht==true && T_Zone>=GG_SOLVER.get_T_Freeze()){
			GasGemisch zoneGG =
				new GasGemisch(GG_SOLVER.get_GG_molenBrueche(p_Zone, T_Zone, gg_Zone),"zoneGG"+ID);
			GasGemisch rauchgas =
				new GasGemisch(GG_SOLVER.get_GG_molenBrueche(p_Zone+delta_p, T_Zone, gg_Zone),"rauchgasZone"+ID);

			double a=(rauchgas.get_u_mass(T_Zone)-zoneGG.get_u_mass(T_Zone));
			du_dp_dR_dp[0]=a/delta_p;

			a=(rauchgas.get_R()-zoneGG.get_R());
			du_dp_dR_dp[1]=a/delta_p;

		}else{
			du_dp_dR_dp[0]=0;
			du_dp_dR_dp[1]=0;
		}			
		return du_dp_dR_dp;
	}


	private Hashtable<Spezies,Double> calc_dmj_dp(){
		Hashtable<Spezies,Double> massenBruchHash_Zone=gg_Zone.get_speziesMassenBruecheDetail();
		Hashtable<Spezies,Double> dmj_dp=new Hashtable<Spezies,Double>();

		Enumeration<Spezies> e=massenBruchHash_Zone.keys();
		Spezies spez;

		if(loeseChemGleichgwicht==true&&burns==true){
			double delta_p=5000;
			GasGemisch rauchgas =new GasGemisch(GG_SOLVER.get_GG_molenBrueche(p_Zone+delta_p,T_Zone, gg_Zone),"rauchgas_temp_"+ID);				
			Hashtable<Spezies,Double> rgSpezMassenBrueche=rauchgas.get_speziesMassenBruecheDetail();				

			while(e.hasMoreElements()){
				spez=e.nextElement();
				dmj_dp.put(spez,(m_Zone*(rgSpezMassenBrueche.get(spez)-massenBruchHash_Zone.get(spez)))/delta_p);
			}

		}else{

			while(e.hasMoreElements()){
				spez=e.nextElement();
				dmj_dp.put(spez,0.0);
			}
		}

		return dmj_dp;			
	}


	private Hashtable<Spezies,Double> calc_dmj_dT(){
		Hashtable<Spezies,Double> massenBruchHash_Zone=gg_Zone.get_speziesMassenBruecheDetail();
		Hashtable<Spezies,Double> dmj_dT=new Hashtable<Spezies,Double>();
		Enumeration<Spezies> e=massenBruchHash_Zone.keys();
		Spezies spez;

		//Berechnen der kalorischen Daten für Rauchgas
		if(loeseChemGleichgwicht==true&&burns==true){
			double delta_T=1;		
			//if-Abfrage nur dann sinnvoll wenn Einfriertemperatur unter 1000K 
			//oder wenn Luft als Rauchgas mit sehr gro§em Lambda modelliert wird
			if (T_Zone > 1000 && T_Zone <= 1001) {
				delta_T=-1;
			} 				

			GasGemisch rauchgas =new GasGemisch(GG_SOLVER.get_GG_molenBrueche(p_Zone, T_Zone-delta_T, gg_Zone),"rauchgas");					
			Hashtable<Spezies,Double> rgSpezMassenBrueche=rauchgas.get_speziesMassenBruecheDetail();	

			while(e.hasMoreElements()){
				spez=e.nextElement();
				dmj_dT.put(spez,(m_Zone*(massenBruchHash_Zone.get(spez)-rgSpezMassenBrueche.get(spez))/delta_T));
			}

		}else{

			while(e.hasMoreElements()){
				spez=e.nextElement();
				dmj_dT.put(spez,0.0);
			}
		}				
		return dmj_dT;
	}



	public Hashtable<Spezies, Double> get_dmi_dp_Hash() {
		return this.calc_dmj_dp();
	}

	public double [] get_dmi_dp() {

		Hashtable<Spezies, Double> dmdp=this.calc_dmj_dp();
		double dmi []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		Enumeration<Spezies> e=dmdp.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			dmi[CP.SPEZIES_FABRIK.get_indexOf(spez)]=dmdp.get(spez);				
		}	
		return dmi;
	}


	public Hashtable<Spezies, Double> get_dmi_dT_Hash() {
		return this.calc_dmj_dT();
	}

	public double [] get_dmi_dT() {		
		Hashtable<Spezies, Double> dmdT=this.calc_dmj_dp();
		double dmi []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		Enumeration<Spezies> e=dmdT.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			dmi[CP.SPEZIES_FABRIK.get_indexOf(spez)]=dmdT.get(spez);				
		}	
		return dmi;
	}

	public double [] get_mi(){
		//TODO mach mich mi[] zur Klassenvariablen, dann duerfte es etwas schneller rechnen
		Hashtable<Spezies, Double> mb=gg_Zone.get_speziesMassenBruecheDetailToIntegrate();
		double mi []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		Enumeration<Spezies> e=mb.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			mi[CP.SPEZIES_FABRIK.get_indexOf(spez)]=mb.get(spez)*m_Zone;				
		}			
		return mi;
	}	
	
	
	/**Returns a vector holding the concentration of each species.
	 * The positions in the vector follows the index of the species in 
	 * the speciesFactory
	 * @return speciesConcentration in [mol/m3]
	 */
	public double [] get_ci(){
		
		Hashtable<Spezies, Double> mb=gg_Zone.get_speziesMassenBruecheDetailToIntegrate();
		double ci []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];		
		Enumeration<Spezies> e=mb.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			ci[CP.SPEZIES_FABRIK.get_indexOf(spez)]=mb.get(spez)*m_Zone/spez.get_M()/this.V_Zone;				
		}			
		return ci;
	}	
	
	public double [] get_dmi(){
		//TODO mach mich dmi[] zur Klassenvariablen
		double dmi1 []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		Enumeration<Spezies> e=dmj_ein.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			dmi1[CP.SPEZIES_FABRIK.get_indexOf(spez)]=dmj_ein.get(spez);				
		}
		double dmi2 []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		e=null;		
		e=dm_aus.keys();
		while(e.hasMoreElements()){
			spez=e.nextElement();
			dmi2[CP.SPEZIES_FABRIK.get_indexOf(spez)]=this.dm_aus.get(spez);
//			dmi[CP.SPEZIES_FABRIK.get_indexOf(spez)]=
//				dmi[CP.SPEZIES_FABRIK.get_indexOf(spez)]-this.dm_aus.get(spez);	
		}
		double dmi []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		for(int i=0;i<dmi.length;i++)dmi[i]=dmi1[i]-dmi2[i];	
		double a=dmi2[1];
		double b=dmi1[1];		
		
//		BigDecimal ba=new BigDecimal(a);
//		BigDecimal bb=new BigDecimal(b);
//		BigDecimal bc=ba.subtract(bb);
		//System.out.println(bc.toString());
		double c=a-b;
		double d=4.688457271404381E45-4.688457271404382E45;
			
		double sumdmi=0;
		for(int i=0;i<dmi.length;i++)sumdmi+=dmi[i];
//		if(sumdmi!=0)
//			System.out.println("sumdmi in Zone " +this.ID +": " +sumdmi);
		return dmi;
	}

	public double[] get_MassFractions(){
		double mi[]=this.get_mi();		
		for(int i=0;i<mi.length;i++)
			mi[i]=mi[i]/this.m_Zone;		
		return mi;		
	}
	
	public double [] get_MoleFractions(){
		Hashtable<Spezies, Double> mf=gg_Zone.get_speziesMolenBruecheDetailToIntegrate();
		double x []=new double[CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		Enumeration<Spezies> e=mf.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			x[CP.SPEZIES_FABRIK.get_indexOf(spez)]=mf.get(spez);				
		}			
		return x;
	}
	
	

	public double [] get_p_V_T_mi(){			
		double[]p_V_T_mi=new double [3+CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		p_V_T_mi[0]=this.p_Zone;
		p_V_T_mi[1]=this.V_Zone;
		p_V_T_mi[2]=this.T_Zone;
		double [] mi=this.get_mi();
		for(int idx=0;idx<mi.length;idx++){
			p_V_T_mi[idx+3]=mi[idx];
		}
		return p_V_T_mi;
	}

	public double get_bT(){	
		double bT=-m_Zone*gg_Zone.get_R()-m_Zone*T_Zone*dRdT;
		return bT;		
	}




	public double get_bp(){	
		double bp= V_Zone-m_Zone*T_Zone*dRdp;	
		return	bp;		
	}




	public double get_bY(){	

		Enumeration<Spezies>e;		
		Spezies spez;
		
		double sum_dmj_Rj=0D;
		e=dmj_ein.keys();
		while(e.hasMoreElements()){
			spez=e.nextElement();
			sum_dmj_Rj=sum_dmj_Rj+dmj_ein.get(spez)*spez.get_R();
		}
		e=null;
		e=dm_aus.keys();
		while(e.hasMoreElements()){
			spez=e.nextElement();
			sum_dmj_Rj=sum_dmj_Rj-dm_aus.get(spez)*spez.get_R();
		}		

		double bY=T_Zone*sum_dmj_Rj;		
		return 	bY;	
	}




	public double get_aT(){			
		return m_Zone*dudT;			
	}



	public double get_ap(){			
		return  m_Zone*dudp;				
	}



	public double get_aY(){			

		//einstroemende minus ausstroemende Energie
		//		return sumdQ+sum_dmRT;	
		return sumdQ+sumdH-sumdU;	
	}



	public double get_xsi(){	
		double aT=get_aT();
		double bT=get_bT();
		double by=get_bY();
		double ay=get_aY();
		double xsi=ay-by*(aT/bT);
		xsi=xsi/(p_Zone*(1-(aT/bT)));			

		return xsi;		
	}



	//vielleicht sollte man das hier noch so umprogrammieren, dass es etwas schneller läuft....vielleicht ;-)
	public double get_eta(){

		double aT=get_aT();
		double bT=get_bT();
		double bp=get_bp();			
		double ap=get_ap();
		double eta=ap-bp*(aT/bT);			
		eta=eta/(p_Zone*(1-(aT/bT)));
		return eta;	

	}


	/**
	 * Gibt an ob eine Zone eine verbrannte Zone ist. Nur fuer
	 * verbrannte Zonen wird die dissoziation berechnet!
	 * @return boolean 
	 */
	public boolean isBurnt() {

		return burns;
	}
	
	public int getID(){
		return ID;
	}

	/**
	 * Liefert die aktuelle Temperatur in der Zone
	 * @return T_Zone in [K]
	 */
	public double get_T() {
		return T_Zone;
	}
	
	/**
	 * Returns the maximum temperature of this Zone 
	 * @return T_max_Zone
	 */
	public double get_T_Max() {
		return this.T_max_Zone;
	}


	/**
	 * @return m in [kg]
	 */
	public double get_m() {
		return m_Zone;
	}		

	/**
	 * @return V in [m³]
	 */
	public double get_V() {
		return V_Zone;
	}	
	
	/**
	 * @return rho in [kg/m³]
	 */
	public double get_rho() {
		return m_Zone/V_Zone;
	}	

	/**
	 * @return p in [N/m²]
	 */
	public double get_p() {
		return p_Zone;
	}

	/**
	 * Liefert das momentane GasGemisch der Zone
	 * @return
	 */
	public GasGemisch get_ggZone() {
		return gg_Zone;
	}


	//	public Hashtable<Spezies, Double> get_dmi_DetailHash() {
	//		return LittleHelpers.addiereHashs
	//		(dmj_ein, gg_Zone.get_speziesMassenBruecheDetail(), -1*dm_aus);	
	//	}




	/**
	 * Liefert die einzelnen Massen der in der Zone vorhandenen Spezies.
	 * Es werden nur reine Spezies zureuckgegeben (CO2, CO, ...)
	 * 
	 * @return Hashtable<Spezies,Double> mi
	 */
	public Hashtable<Spezies,Double> get_mi_DetailToIntegrate() {
		Hashtable<Spezies,Double> massenBrueche_Zone=this.gg_Zone.get_speziesMassenBruecheDetailToIntegrate();

		return get_einzelMassenHash(massenBrueche_Zone,m_Zone);			
	}





	private Hashtable<Spezies, Double> get_einzelMassenHash( 
			Hashtable<Spezies, Double>massenBruchHash, double mGes){

		Hashtable<Spezies, Double>einzelMassenHash=
			new Hashtable<Spezies, Double>(massenBruchHash.size());

		//Erstellen der Hashtable mit den Einzelmassen der Grundspezies (CO2, O2, usw....
		Enumeration<Spezies> e=massenBruchHash.keys();
		Spezies spez;
		while(e.hasMoreElements()){
			spez=e.nextElement();
			double m_temp=massenBruchHash.get(spez)*mGes;				
			einzelMassenHash.put(spez,m_temp);
		}			
		return einzelMassenHash;
	}




	/**
	 * Mit dieser Methode kann ein Massenelement eines Gases der Zone zugemischt werden.
	 * --> es wird nur die Zusammensetzung und die Gesamtmasse der Zone geändert nicht aber die Temperatur
	 * 	
	 * @param idx --> Rechenindex
	 * @param mZu --> zugemischtes Massenelement [kg]
	 * @param sZu --> SpeziesObjekt des Massenelements
	 */
	public void massenElementZumischen(double mZu, Spezies sZu){

		Hashtable<Spezies,Double> massenBruchHash_Zone=gg_Zone.get_speziesMassenBruecheDetail();

		Hashtable <Spezies, Double> massenBruchHashNeu= new Hashtable<Spezies, Double>(4);			
		double m_Zone_neu=mZu+m_Zone;	
		if(m_Zone_neu<0){
			try{
				throw new MiscException("ERROR: Aus Zone " + ID +" wird mehr Masse entnommen als vorhanden ist");

			}catch(MiscException miscE){
				miscE.stopBremo();
			}
		}
		Hashtable <Spezies, Double> einzelMassenHashIn=new Hashtable<Spezies, Double>();
		//einzelmassen der zugefuehrten Spezies bestimmen
		if(mZu<0){ 
			if(sZu.isGasGemisch()){				
				einzelMassenHashIn	= //enthaelt negative Werte!!
					this.get_einzelMassenHash(((GasGemisch)sZu).get_speziesMassenBruecheDetail(), mZu);	
			}else{
				einzelMassenHashIn.put(sZu, mZu);
			}
			//einzelmassen der Zone bestimmen	
			Hashtable <Spezies, Double> einzelMassenHashZone=	
				this.get_einzelMassenHash(massenBruchHash_Zone, m_Zone);

			//Addieren der einzelnen Massen von Zone und sZu
			Enumeration<Spezies> e = einzelMassenHashZone.keys();
			Spezies spez;
			double mTemp;

			while(e.hasMoreElements()){
				spez=e.nextElement();
				mTemp=einzelMassenHashZone.get(spez);

				if(einzelMassenHashIn.containsKey(spez))
					mTemp=mTemp+einzelMassenHashIn.get(spez); //Wert in Hashtable ist negativ

				double massenBruch=mTemp/m_Zone_neu;					
				massenBruchHashNeu.put(spez, massenBruch);

				if(massenBruch<0){//kommt vor wenn eine Masse entnommen werden soll die gar nicht in der Zone ist!
					try{
						throw new MiscException("ERROR: In einer Zone tritt ein negativer Massenbruch auf \n" +
								spez.get_name()+": "+massenBruch );

					}catch(MiscException miscE){
						massenBruchHashNeu.put(spez, 0d);
						//							miscE.stopBremo(); //TODO check mich 
					}
				}						
			}
		}else{	
			massenBruchHashNeu.put(gg_Zone, m_Zone/m_Zone_neu);
			if(massenBruchHashNeu.containsKey(sZu)){ //==True wenn der Zone eine Masse von sich selbst zugefuehrt wird
				massenBruchHashNeu.put(gg_Zone, 1D);
			}else
				massenBruchHashNeu.put(sZu, mZu/m_Zone_neu);
		}			


		Hashtable <Spezies, Double> molenBruchHash= new Hashtable<Spezies, Double>(4);	
		molenBruchHash=GasGemisch.Gasmischer.massenBruch2molenBruch(massenBruchHashNeu);

		GasGemisch gasGemisch = new GasGemisch(molenBruchHash,"");	
		//würde man hier die 
		gg_Zone.set_Gasmischung_molenBruch(gasGemisch.get_speziesMolenBruecheDetail());
		m_Zone=m_Zone_neu;

	}



	/**
	 * Mit dieser Methode kann ein Massenelement eines Gases mit der Temperatur T_m 
	 * der Zone mit der Temperatur T_Zone zugemischt werden. Die sich ergebende Mischungstemperatur 
	 * wird für VERÄNDERLICHE CVs  und ein adiabates System nach dem ersten HS berechnet.
	 * Das Volumen der Zone bleibt Konstant --> das zugemischte Massenelement leistet Verschiebearbeit!
	 * 
	 * @param m_Zu --> zugemischtes Massenelement [kg]
	 * @param T_Zu --> Temperatur des zugemischten Massenelements [K]
	 * @param s_Zu --> Zusammensetzung des Massenelements		  
	 * 
	 */		
	public void massenElementZumischenKonstVol(double m_Zu,double T_Zu, Spezies s_Zu){
		//adiabate Mischungstemperatur nach dem ersten Hauptsatz
		double Tm=278.15, U1,U_zu,U1m,U_zum,Cvm1,Cv_zum, F, dF,Tm_buffer;
		U1=gg_Zone.get_u_mass(T_Zone)*m_Zone;
		U_zu=s_Zu.get_u_mass(T_Zu)*m_Zu;
		int idx=0;
		//Newtonverfahren für F(Tm)= U1(T1)+U2(T2)-U1(Tm)-U2(Tm)=0 
		//mit dF/dT=-m1*Cv1(Tm)-m2*Cv2(Tm)
		do{	
			Tm_buffer=Tm;
			U1m=gg_Zone.get_u_mass(Tm)*m_Zone;
			U_zum=s_Zu.get_u_mass(Tm)*m_Zu;
			Cvm1=gg_Zone.get_cv_mass(Tm)*m_Zone;
			Cv_zum=s_Zu.get_cv_mass(Tm)*m_Zu;
			F=U1+U_zu-U1m-U_zum;
			dF=-Cvm1-Cv_zum;
			Tm=Tm-F/dF;			
			idx++;
		}while(idx<1000 && Math.abs(Tm-Tm_buffer) >0.1);
		if(idx>=1000){
			try{
				throw new MiscException("t");
			}catch(MiscException me){
				me.log_Warning("Mangelnde Konvergenz bei der Berechnung der Mischungstemperatur");
			}
		}	
		T_Zone=Tm;
		//dieser Aufruf muss nach der Temperaturberechnung erfolgen da sich hier die Masse der Zone ändert
		massenElementZumischen( m_Zu, s_Zu);			
	}



	/**
	 * Mit dieser Methode kann ein Massenelement eines Gases mit der Temperatur T_m 
	 * der Zone mit der Temperatur T_Zone zugemischt werden. Die sich ergebende Mischungstemperatur 
	 * wird für VERÄNDERLICHE CVs  und ein adiabates System nach dem ersten HS berechnet.
	 * Das Volumen der Zone bleibt Konstant --> das zugemischte Massenelement leistet Verschiebearbeit!
	 * 
	 * @param m_Zu --> zugemischtes Massenelement [kg]
	 * @param T_Zu --> Temperatur des zugemischten Massenelements [K]
	 * @param s_Zu --> Zusammensetzung des Massenelements		  
	 * 
	 */		
	public void massenElementZumischenKonstVolEinfacher(double m_Zu,double T_Zu, Spezies s_Zu){
		//Nach dem 1HS gilt: dm*h=dU
		double dH=m_Zu*s_Zu.get_h_mass(T_Zu);	
		double U_Zone_mass_temp=m_Zone*gg_Zone.get_u_mass(T_Zone)+dH;
		//dieser Aufruf muss vor der Temperaturberechnung erfolgen da sich hier die Masse und die Zusammensetzung
		//der Zone ändert
		massenElementZumischen( m_Zu, s_Zu);
		//Berechnen der Mischungstemperatur
		U_Zone_mass_temp=U_Zone_mass_temp/m_Zone;
		T_Zone=gg_Zone.get_T4u_mass(U_Zone_mass_temp);			
	}



	/**
	 * Mit dieser Methode kann eine Zone mit einer anderen gemischt werden. 
	 * Das Volumen der beiden Zonen wird addiert entsprechent wird keine Verschiebearbeit geleistet.
	 * Temperaur, Masse und Zusammensetzung werden entsprechend {@link massenElementZumischen} berechnet 
	 * 
	 * @param Zone zone2 --> die Zone die zugemischt werden soll			  
	 * 
	 */		
	public void zoneZumischen(Zone zone2){

		double m_Zu=zone2.get_m();
		double T_Zu=zone2.get_T();
		double V_Zu=zone2.get_V();
		Spezies s_Zu=zone2.get_ggZone(); 

		//Nach dem 1HS gilt: -pdV+dm*h=dU --> -p*deltaV+deltaH=deltaU=U2-U1 --> U2=U1+deltaH-p*deltaV
		double H_zu=m_Zu*s_Zu.get_h_mass(T_Zu);
		//deltaV=V_zu  außerdem ist der Druck in beiden Zonen identisch
		double U2_Zone_mass=m_Zone*gg_Zone.get_u_mass(T_Zone)+H_zu-p_Zone*V_Zu;
		//dieser Aufruf muss vor der Temperaturberechnung erfolgen da sich hier die Masse und die Zusammensetzung
		//der Zone ändert
		massenElementZumischen( m_Zu, s_Zu);
		//Berechnen der Mischungstemperatur
		T_Zone=gg_Zone.get_T4u_mass(U2_Zone_mass/m_Zone);			
	}


	public static Zone zonenMischen(CasePara cp,Zone z1, Zone z2,boolean burns, int ID){
		double p=z1.get_p();//fuer beide Zonen gleich
		double m1=z1.get_m();
		double T1=z1.get_T();
		double V1=z1.get_V();
		Spezies s1=z1.get_ggZone(); 

		double m2=z2.get_m();
		double T2=z2.get_T();
		double V2=z2.get_V();
		Spezies s2=z2.get_ggZone(); 

		//Erzeugen einer Spezies die durch die Mischung der beiden ZonenSpezies entsteht
		GasGemisch s0=new GasGemisch("zonenGemisch");
		Hashtable<Spezies,Double> ht0=new Hashtable<Spezies,Double >(3);
		double mGes=m1+m2;
		ht0.put(s1, m1/mGes);
		ht0.put(s2, m2/mGes);
		s0.set_Gasmischung_massenBruch(ht0);
		//Berechnung der Mischungstemperatur
		//1.HS --> Kontrollraumgrenze wird um beide Zonen gelegt
		//Dann gilt: Umisch=U1+U2;
		double Umisch=m1*s1.get_u_mass(T1)+m2*s2.get_u_mass(T2);
		//Berechnung der Temperatur bei der die innere Energie von Spezies s0=Umisch ist.
		//Dies entspricht der Mischungstemperatur
		double T0=s0.get_T4u_mass(Umisch/mGes);
		//Das Volumen beider Zonen wird addiert
		double V0=V1+V2;	
		
		//Test ob alles stimmt
		double pV=z1.get_p()*V0;			
		double mRT=mGes*T0*s0.get_R();		
		double T=(m1*s1.get_cv_mass(T1)*T1+m2*s2.get_cv_mass(T2)*T2)/(m1*s1.get_cv_mass(T1)+m2*s2.get_cv_mass(T2));
		double deltaT=(T-T0)/T0*100;	
		double T3=pV/mGes/s0.get_R();		
		double mRT2=mGes*T*s0.get_R();
		double mRT3=mGes*T3*s0.get_R();
		//Fazit ueber die Gasgleichung geht es auch und zwar viel einfacher und genauer!

		Zone z0=new Zone(cp,p, V0, T0, mGes, s0, burns, ID);		
		return z0;
	}


}



