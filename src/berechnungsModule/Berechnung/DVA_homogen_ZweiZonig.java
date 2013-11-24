package berechnungsModule.Berechnung;

import java.util.Hashtable;

import kalorik.GasMixture;
import kalorik.Spezies;
import matLib.MatLibBase;
import misc.LHV_Calculator;
import misc.LittleHelpers;
import misc.VectorBuffer;
import berechnungsModule.chemEquilibriumSolver.ChemEquilibriumSolver;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.motor.Motor;
import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import bremo.parameter.CasePara;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;


/**
 * <p>Klasse zur Durchfuehrung einer DVA mit zwei Zonen. Der Kraftstoff wird in Zone 0 eingespritzt und vermischt sich 
 * sofort homogen mit der Verbrennungsluft. Aus Zone 0 wird das Gemsich entnommen, verbrannt und dann mit der 
 * Temperatur der unverbrannten Zone in die verbrannte Zone zugemsicht. Die Verbrennungswaerme wird vollstaendig Zone 1
 * zugefuehrt.  Alternativ koennte auch jedesmal die adiabate Verbrennungstemperatur berechnet werden und dann das 
 * Abgas mit dieser Temperatur der verbrannten Zone zugemischt werden. Ein Waermestrome wuerde dann nicht mehr auftreten.
 * Diese Art der Rechnung ist gleichwertig, die Rechenzeit steigt aber wegen der Berechnung der adiabaten Flammentemperatur 
 * deutlich an. </p> 
 * 
 *<p>Bei Direkteinspritzung ist darauf zu schten, dass der Kraftstoff vollstaenmdig verdampft ist bevor die Verbrennung 
 *einsetzt. Um die Menge des verbrennenden Massenelementes zu bestimmen wird der Heizwert des GasGemischs aus
 *Zone 0 verwendet. Wenn noch nicht der gesamte Kraftstoff eingespritzt ist, ist der Heizwert zu gering und 
 *es besteht die Moeglichkeit, dass die Zone vollstaendig entleert wird. </p>
 * @author eichmeier
 *
 */
public class DVA_homogen_ZweiZonig extends DVA {

	private  WallHeatTransfer wandWaermeModell;
	private Motor motor;
	private ChemEquilibriumSolver gg;
	private MasterInjection masterEinspritzung;


	private final int ANZAHL_ZONEN;

	private double dQw, Qw=0, Qb=0,mb=0;
	double zonenMasseVerbrannt=0;

	/**
	 * <p>
	 * Liefert die sich zum Zeitpunkt time "Rechenbeginn" in der Zone befindliche  
	 * Gasmasse, bestehend aus Kraftstoffdampf (von allen Einspritzungen) und Verbrennungsluft 
	 * </p>
	 * mINIT=Masse aus mVerbrennungsluft+mKrst_dampf [kg]
	 */
	double mINIT=-5.55;	
	double dmZoneBurn=0, Qmax;

	Zone []  initialZones;	
	Zone [] initialZonesWhileRunning;	

	private boolean esBrennt=false,initialiseBurntZone=false,burntZonesAlreadyInitialised=false;

	private double dQburnMAX=0;
	private double fortschritt=0;	


	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private VectorBuffer T_buffer ;
	private VectorBuffer dQb_buffer ;
	private VectorBuffer dQw_buffer ;
	private VectorBuffer dmb_buffer ;



	protected DVA_homogen_ZweiZonig(CasePara cp) {
		super(cp);	

		ANZAHL_ZONEN=2;

		motor = CP.MOTOR;
		wandWaermeModell=CP.WALL_HEAT_TRANSFER_MODEL;	
		masterEinspritzung=CP.MASTER_INJECTION;
		checkInjections(masterEinspritzung); //checkt ob alle Einspritzungen in die unverbrennte Zone erfolgen
		gg=CP.OHC_SOLVER;

		T_buffer = new misc.VectorBuffer(cp);
		dQb_buffer = new misc.VectorBuffer(cp);	
		dQw_buffer = new misc.VectorBuffer(cp);	
		dmb_buffer = new misc.VectorBuffer(cp);	

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[ANZAHL_ZONEN];


		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
		double mKrstDampfINIT=masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.SIMULATION_START_SEC,0); 
		this.mINIT= mVerbrennungsLuft+mKrstDampfINIT;
		Spezies krst=masterEinspritzung.get_spezKrst_verdampft(CP.SYS.SIMULATION_START_SEC,0);
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	

		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();

		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mINIT);
		frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mINIT);		

		GasMixture gemischINIT=new GasMixture("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	


	
		//Anfangsbedingungen Setzen
		//p Init
		double p_init=super.indiD.get_pZyl(CP.SYS.SIMULATION_START_SEC);
		//V Init
		double V_init=motor.get_V(CP.SYS.SIMULATION_START_SEC);
		//T Init		
		Spezies ggZone_init=gemischINIT;
		double R=ggZone_init.get_R();
		double T_init=(p_init*V_init)/(mINIT*R);
		
		
		//Initialisierung der Zonen
		double m1=(1+1e-6)*CP.SYS.MINIMUM_ZONE_MASS;
		//m1=0.5*mINIT;
		double m0=mINIT-m1;
		//unverbrannte Zone
		double V0=m0*R*T_init/p_init;		
		this.initialZones[0]=new Zone(CP,p_init, V0, T_init, 
				m0,ggZone_init , false,0);

		//verbrannte Zone--> wird spaeter nochmal initialisiert
		double V1=m1*R*T_init/p_init;	
		this.initialZones[1]=new Zone(CP,p_init, V1, T_init, 
				m1,ggZone_init, true,1);

		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mFuel_Sum_Cycle()*masterEinspritzung.get_spezKrstALL().get_LHV_mass();	

	}


	public int get_nbrOfZones() {		
		return ANZAHL_ZONEN;
	}


	public Zone[] firstLawCombustionChamber(double time, Zone[] zonen_IN) {			

		double dQburn=super.get_dQburn();

		//aktueller Zustand in der Zone
		double p=zonen_IN[0].get_p();
		double Tu=zonen_IN[0].get_T();
		double Vu=zonen_IN[0].get_V();

		double Vb=zonen_IN[1].get_V();


		dmZoneBurn=0;	


		if(burntZonesAlreadyInitialised){
			if(esBrennt&&dQburn>0) 
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,this.get_frischGemisch(),Tu,p);

			//Verbrennungswaerme zufuehren
			zonen_IN[1].set_dQ_in_out(1*dQburn);

			if(dmZoneBurn>0){
				if(zonen_IN[0].get_m()-CP.SYS.MINIMUM_ZONE_MASS>dmZoneBurn*CP.SYS.WRITE_INTERVAL_SEC){
					try {
						zonen_IN[0].set_dm_aus(dmZoneBurn,this.get_frischGemisch());
					} catch (NegativeMassException nmE) {					
						nmE.log_Warning();
						dmZoneBurn=0;
					}	

					GasMixture rauchgas;				
					rauchgas=new GasMixture("Rauchgas");

					rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche
							(p, Tu, this.get_frischGemisch()));	
					//				Berechnung der Dissoziation erfolgt in der Zone automatisch				
					zonen_IN[1].set_dm_in(dmZoneBurn,Tu,rauchgas);	

					//Wenn nicht dQb sondern die heiße verbrannte Masse zuigefuehrt werden soll
					//				double T_BurnAdiabat=HeizwertRechner.calcAdiabateFlammenTemp(
					//						this.get_frischGemisch(), p, Tu); 
					//				rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche
					//				(p, T_BurnAdiabat, this.get_frischGemisch()));	
					////				Berechnung der Dissoziation erfolgt in der Zone automatisch				
					//				zonen_IN[1].set_dm_ein(dmZoneBurn,T_BurnAdiabat,rauchgas);		
				}
			}

			//Wandwaermestrom bestimmen			
			dQw=wandWaermeModell.get_wallHeatFlux(time, zonen_IN, fortschritt, T_buffer);			

			//Wandwaermestrom abfuehren
			double dQwu=dQw*Vu/(Vu+Vb);
			//unverbrannte Zone
			zonen_IN[0].set_dQ_in_out(-1*dQwu);		
			//verbrannte Zone
			double dQwb=dQw-dQwu;
			zonen_IN[1].set_dQ_in_out(-1*dQwb);				

			//Verdampfungswaerme abfuehren
			zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

			//Einspritzung des Kraftstoffs
			zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

		}else{

			//Wandwaermestrom bestimmen	
			dQw=wandWaermeModell.get_wallHeatFlux(time, zonen_IN, fortschritt, T_buffer);
			//Wandwaermestrom abfuehren
			zonen_IN[0].set_dQ_in_out(-1*dQw);	

			//Verbrennungswaerme zufuehren
			zonen_IN[0].set_dQ_in_out(dQburn);

			//Verdampfungswaerme abfuehren
			zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

			//Einspritzung des Kraftstoffs
			zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

			//Verhindert das Aufsummieren des Fehlers wenn der Brennverlauf 
			//vor der eigentlichen Verbrennung etwas schwingt				
			if(esBrennt&&dQburn>0) 
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,zonen_IN[0].get_gasMixtureZone(),Tu,p);


		}
		return zonen_IN;			
	}
	
	/**
	 * Erstellt eine Objetk vom Typ Spezies, als Mischung aus Verbrennungsluft (Luft + AGR) 
	 * und allen Kraftstoffen die in dem Arbeitsspiel verbrannt werden. Unabhängig von der 
	 * tatsaechlichen Gemischbildung verbrennt der Kraftstoff immer mit dem 
	 * selben Luftverhältnis
	 * @param time
	 * @param fortschritt
	 * @return
	 */
	private Spezies get_frischGemisch(){		
		//Frischgemisch als Spezies Objekt erstellen
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();		
		Spezies krst=masterEinspritzung.get_spezKrstALL();	
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		double mKrst=masterEinspritzung.get_mFuel_Sum_Cycle();
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
		double mGes= mVerbrennungsLuft+mKrst;
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		

		GasMixture frischGemisch=new GasMixture("Frischgemisch");	
		frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
		
		return frischGemisch;		
	}





	public Zone[] get_initialZones() {		
		return initialZones;
	}




	public void bufferResults(double time, Zone[] zn) {			
		double dQburn=super.get_dQburn();
		dQb_buffer.addValue(time, dQburn);
		dQw_buffer.addValue(time, dQw);
		dmb_buffer.addValue(time, dmZoneBurn);

		if(dQburn>dQburnMAX)dQburnMAX=dQburn;

		//bei jedem Rechenschritt wird der Buffer mit den dQb-Werten aufgefüllt
		esBrennt=super.verbrennungHatBegonnen(time, dQb_buffer);

		if(esBrennt==true){
			this.mb=this.mb+this.dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		}

		if(mb>=super.CP.SYS.MINIMUM_ZONE_MASS && burntZonesAlreadyInitialised==false){
			double ke=super.CP.convert_SEC2KW(time);
			this.initBurnedZone(time, zn);
			
			initialiseBurntZone=true;
		}

		//Berechnen integraler Werte
//		zonenMasseVerbrannt=zonenMasseVerbrannt+dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		zonenMasseVerbrannt=zn[1].get_m();
		fortschritt=zonenMasseVerbrannt/mINIT;
		Qb=Qb+dQburn*super.CP.SYS.WRITE_INTERVAL_SEC;
		Qw=Qw+dQw*super.CP.SYS.WRITE_INTERVAL_SEC; 
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
		double xQ=Qb/Qmax;

		int i=-1;
		i+=1;
		super.buffer_singleResult("Kurbelwinkel [°KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_singleResult("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_singleResult("Brennraumvolumen [m3]",motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("Vu [m3]",zn[0].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("Vb [m3]",zn[1].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("T_Zone_1 [K]",zn[0].get_T(),i);

		i+=1;
		super.buffer_singleResult("T_Zone_2 [K]",zn[1].get_T(),i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_singleResult("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		i+=1;
		double T_BurnAdiabat=LHV_Calculator.calcAdiabateFlammenTemp(super.CP,
				zn[0].get_gasMixtureZone(), zn[0].get_p(), zn[0].get_T());	

		super.buffer_singleResult("T_BurnAdiabat [K]", T_BurnAdiabat,i);

		double pSoll=indiD.get_pZyl(time);
		i+=1;
		super.buffer_singleResult("p_soll [bar]", pSoll*1E-5,i);

		i+=1;
		super.buffer_singleResult("p [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_singleResult("dQb [J/s]",dQburn,i);

		i+=1;
		super.buffer_singleResult("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn),i);

		i+=1;		
		super.buffer_singleResult("Qb [J]", Qb,i);

		i+=1;
		super.buffer_singleResult("dmb [kg/s]", dmZoneBurn,i);

		i+=1;
		super.buffer_singleResult("dmb [kg/KW]", super.CP.convert_ProSEC_2_ProKW(dmZoneBurn),i);

		i+=1;	
		super.buffer_singleResult("mb [kg]", zonenMasseVerbrannt,i);

		i+=1;
		super.buffer_singleResult("dQw [J/s]",dQw,i);

		i+=1;
		super.buffer_singleResult("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);	

		i+=1;		
		super.buffer_singleResult("Qw [J]",Qw,i);		

		i+=1;		
		super.buffer_singleResult("Xb[-]", fortschritt,i);


		i+=1;		
		super.buffer_singleResult("Qb/Qmax [-]", xQ,i);

		i+=1;
		super.buffer_singleResult("Qmax [J]", Qmax,i);	

		i+=1;
		double Hu=zn[0].get_gasMixtureZone().get_LHV_mass();
		super.buffer_singleResult("Hu Zone [J/kg]",Hu,i );	

		i+=1;
		super.buffer_singleResult("Hu frischGemsich [J/kg]", 
				this.get_frischGemisch().get_LHV_mass(),i);		

		i+=1;
		super.buffer_singleResult("Phi_Delay [°KW]",10*fortschritt*1000/(CP.get_rotSpeedInRotperSec()*60),i);

		i+=1;
		double alpha=wandWaermeModell.get_WaermeUebergangsKoeffizient(time, zn, fortschritt);
		super.buffer_singleResult("Alpha [W/(m^2K)]", alpha, i);


		//Polytropenexponent für die Schleppdruckberechnung ermitteln.
		//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
		double refPunkt=CP.get_refTime_WoschniHuber();
		double[] n_array = new double[10]; 
		double pZyl_a = indiD.get_pZyl(refPunkt);
		double Vol_a = motor.get_V(refPunkt);
		double pZyl_b = 0;
		double Vol_b = 0;
		int cnt=0;

		for(double kw=CP.convert_SEC2KW(refPunkt)-10; kw < CP.convert_SEC2KW(refPunkt); kw++){
			pZyl_b=indiD.get_pZyl(CP.convert_KW2SEC(kw));
			Vol_b=motor.get_V(CP.convert_KW2SEC(kw));
			n_array[cnt]=Math.log10(pZyl_a/pZyl_b)/Math.log10(Vol_b/Vol_a);
			cnt++;
		}

		double n=MatLibBase.mw_aus_1DArray(n_array); //Polytropenexponent
		double Schleppdruck = pZyl_a*Math.pow((Vol_a/motor.get_V(time)),n)*1E-5; //[bar]
		i+=1;
		super.buffer_singleResult("pSchlepp [bar]",Schleppdruck,i);

		///////////////////////////		

//		i+=1;
//		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt, T_buffer);
//		super.buffer_EinzelErgebnis("Wandwärmestromdichte [MW/m^2]",HeatFlux*1E-6,i);
//
//		i+=1;		
//		super.buffer_EinzelErgebnis("PQw [W]",HeatFlux*motor.get_BrennraumFlaeche(time),i);	

		i+=1;		
		super.buffer_singleResult("Brennraumfläche [m^2]",motor.get_BrennraumFlaeche(time),i);		

		i+=1;
		super.buffer_singleResult("Gesamtmasse [kg]", zn[0].get_m()+zn[1].get_m(),i);

		i+=1;		
		super.buffer_singleResult("m_u[kg]", zn[0].get_m(),i);

		i+=1;		
		super.buffer_singleResult("m_v[kg]", zn[1].get_m(),i);


		i+=1;
		super.buffer_singleResult(" cv_u[J/kg]", zn[0].get_gasMixtureZone().get_cv_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_singleResult(" cp_u [J/kg]", zn[0].get_gasMixtureZone().get_cp_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_singleResult(" kappa_u [-]", zn[0].get_gasMixtureZone().get_kappa(zn[0].get_T()),i);

		i+=1;
		super.buffer_singleResult(" U_u [J]", zn[0].get_gasMixtureZone().get_u_mass(zn[0].get_T())*zn[0].get_m(),i);

		i+=1;
		super.buffer_singleResult(" cv_b[J/kg]", zn[1].get_gasMixtureZone().get_cv_mass(zn[1].get_T()),i);	

		i+=1;
		super.buffer_singleResult(" cp_b [J/kg]", zn[1].get_gasMixtureZone().get_cp_mass(zn[1].get_T()),i);	

		i+=1;
		super.buffer_singleResult(" kappa_b [-]", zn[1].get_gasMixtureZone().get_kappa(zn[1].get_T()),i);

		i+=1;
		super.buffer_singleResult(" U_b [J]", zn[1].get_gasMixtureZone().get_u_mass(zn[1].get_T())*zn[1].get_m(),i);


		i+=1;
		int iter=i;		
		double []mi=zn[0].get_mi();
		double m_ges=zn[0].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_singleResult("Zu_"+CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,i);			
		}

		i+=1;
		iter=i;	
		mi=zn[1].get_mi();
		m_ges=zn[1].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_singleResult("Zb_"+CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,iter+idx);
		}		
		i+=1;		
		super.buffer_singleResult(" mKraftstoffdampf [kg]",
				this.masterEinspritzung.get_injection(0).get_mKrst_verdampft(time),i);
		
		i+=1;
		double pV=zn[0].get_p()*zn[0].get_V();
		double mRT=zn[0].get_m()*zn[0].get_gasMixtureZone().get_R()*zn[0].get_T();
		super.buffer_singleResult("pV-mRT zn0", (pV-mRT)/pV*100,i);
		
		i+=1;
		pV=zn[1].get_p()*zn[1].get_V();
		mRT=zn[1].get_m()*zn[1].get_gasMixtureZone().get_R()*zn[1].get_T();
		super.buffer_singleResult("pV-mRT zn1", (pV-mRT)/pV*100,i);	
	}








	private void initBurnedZone(double time, Zone[] zonen_IN){
		//TODO mach mich public und ruf mich vom solver aus auf
		initialZonesWhileRunning=new Zone[zonen_IN.length];

		double T_BurnAdiabat=LHV_Calculator.calcAdiabateFlammenTemp(super.CP,
				zonen_IN[0].get_gasMixtureZone(), zonen_IN[0].get_p(), zonen_IN[0].get_T()); 
		double Tb=T_BurnAdiabat;	

		double p=zonen_IN[0].get_p();
		GasMixture rauchgas=new GasMixture("rg");
		rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche
				(p, Tb, zonen_IN[0].get_gasMixtureZone()));	


		double Vb_init=this.mb*rauchgas.get_R()*Tb/p;
		//Berechnen der Einzelmassen in der verbrannten Zone
		Hashtable<Spezies,Double> ht=new Hashtable<Spezies,Double>();		
		ht=LittleHelpers.addiereHashs(ht, 
				rauchgas.get_speziesMassenBruecheDetailToIntegrate(), mb);
		initialZonesWhileRunning[1]=new Zone(CP,zonen_IN[1].get_p_V_T_mi(),zonen_IN[1].isBurnt(),zonen_IN[1].getID());
		initialZonesWhileRunning[1].set_p_V_T_mi(p, Vb_init, Tb,ht);

		double mub=zonen_IN[0].get_m()+zonen_IN[1].get_m()-mb; 
		double Vub=motor.get_V(time)-Vb_init;
		double Tub=p*Vub/(mub*zonen_IN[0].get_gasMixtureZone().get_R());			

		//Berechnen der Einzelmassen in der unverbrannten Zone
		Hashtable<Spezies,Double> ht2=new Hashtable<Spezies,Double>();		
		ht2=LittleHelpers.addiereHashs(ht2, 
				zonen_IN[0].get_gasMixtureZone().get_speziesMassenBruecheDetailToIntegrate(), mub);


		initialZonesWhileRunning[0]=new Zone(CP,zonen_IN[0].get_p_V_T_mi(),zonen_IN[0].isBurnt(),zonen_IN[0].getID());
		initialZonesWhileRunning[0].set_p_V_T_mi(p,Vub, Tub,ht2);	
		burntZonesAlreadyInitialised=true;		
	}

	public Zone[] get_initialZonesWhileRunning() {	
		initialiseBurntZone=false; //Beim naechsten mal soll es nicht mehr abgefragt werden
		return initialZonesWhileRunning;

	}


	public boolean initialiseBurntZone() {		
		return initialiseBurntZone;
	}


	/**
	 * Diese Methode ueberprueft ob alle Einspritzungen in die Zone null erfolgen. Wenn nicht bricht das Programm ab
	 */
	protected void checkInjections(MasterInjection me) {
		for(int i=0;i<me.get_allInjections().length;i++){			
			if(me.get_allInjections()[i].get_ID_Zone()!=0){
				try {
					throw new ParameterFileWrongInputException("Für das gwaehlte Berechnungsmodell " +
							"koennen die Einspritzungen " +
							"nur in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone "+ 
							me.get_allInjections()[i].get_ID_Zone());
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}	
		}	

	}


	@Override
	public VectorBuffer get_dQw_buffer() {
		return this.dQw_buffer;
	}
	
	@Override
	public VectorBuffer get_dQb_buffer() {
		return this.dQb_buffer;
	}


	@Override
	public VectorBuffer get_dm_buffer() {
		return this.dmb_buffer;
	}

}
