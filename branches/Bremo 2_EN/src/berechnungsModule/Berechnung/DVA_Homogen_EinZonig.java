package berechnungsModule.Berechnung;

import java.util.Hashtable;
import matLib.MatLibBase;
import misc.LHV_Calculator;
import misc.VectorBuffer;
import kalorik.GasMixture;
import kalorik.Spezies;
import berechnungsModule.chemEquilibriumSolver.ChemEquilibriumSolver;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.motor.Motor;
import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;




/**
 * Klasse zur Durchfuehrung einer DVA mit einer Zone. Es wird zunaechst das Frischgemisch 
 * inForm eines Spezies-Objektes aud Frischluft, AGR und allen Kraftstoffen definiert. 
 * Der Heizwert dieses Gemsichs wird herangezogen um dQ in dm umzurechnen. 
 * Das verbrennende Massenelement wird der Zone entzogen verbrannt, d.h. es wird die Zusammensetzung 
 * des dissoziierenden Rauchgases bestimmt, und anschließend mit der Temperatur der Zone wieder beigemischt.
 * @author eichmeier
 *
 */
public class DVA_Homogen_EinZonig extends DVA{

	private  WallHeatTransfer wandWaermeModell;
	private Motor motor;
	private ChemEquilibriumSolver gg;
	private MasterInjection masterEinspritzung;
	private boolean krstVerbrannt=false;
	
	
	private final int ANZAHL_ZONEN;
	
	private double dQw, Qw=0, Qb=0;
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
	
	
	private boolean esBrennt=false;
	
	private double dQburnMAX=0;
	private double fortschritt=0;	
	
	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private misc.VectorBuffer T_buffer ;
	private misc.VectorBuffer dQb_buffer;
	private misc.VectorBuffer dQw_buffer;
	private misc.VectorBuffer dmb_buffer;
	
	

	protected DVA_Homogen_EinZonig(CasePara cp) {
		super(cp);	
		
		ANZAHL_ZONEN=1;
		
		motor = CP.MOTOR;
		wandWaermeModell=CP.WALL_HEAT_TRANSFER_MODEL;	
		masterEinspritzung=CP.MASTER_INJECTION;
		gg=CP.OHC_SOLVER;
		this.checkInjections(masterEinspritzung);
		
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

		initialZones[0]=new Zone(CP,p_init, V_init, T_init, 
				mINIT,ggZone_init, false,0);			
		
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mFuel_Sum_Cycle()*masterEinspritzung.get_spezKrstALL().get_LHV_mass();	
		
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



	public int get_nbrOfZones() {		
		return ANZAHL_ZONEN;
	}


	public Zone[] firstLawCombustionChamber(double time, Zone[] zonen_IN) {	
		
		Spezies frischGemisch=this.get_frischGemisch();
		
		double dQburn=super.get_dQburn();
		
		//aktueller Zustand in der Zone
		double p=zonen_IN[0].get_p();
		double T=zonen_IN[0].get_T();		
		
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
		dmZoneBurn=0;		
		if(esBrennt&&dQburn>0&&!krstVerbrannt) 
			dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,frischGemisch,T,p);
		
		//Verbrennendes Masseelement entnehmen
		if(dmZoneBurn>0){
			try {
				zonen_IN[0].set_dm_aus(dmZoneBurn,frischGemisch);
				GasMixture rauchgas;	
				rauchgas=new GasMixture("Rauchgas");
				rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche
						(p, T, frischGemisch));
				
				//Massenelement wieder zumischen
				zonen_IN[0].set_dm_in(dmZoneBurn, T, rauchgas);
				
			} catch (NegativeMassException nmE) {					
				nmE.log_Warning();
				krstVerbrannt=true;
			}	
		}				

		return zonen_IN;			
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
		
		
		//Berechnen integraler Werte
		zonenMasseVerbrannt=zonenMasseVerbrannt+dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
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
		super.buffer_singleResult("T_Zone_1 [K]",zn[0].get_T(),i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_singleResult("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);
		
		i+=1;
		double T_BurnAdiabat=LHV_Calculator.calcAdiabateFlammenTemp(super.CP,
				get_frischGemisch(), zn[0].get_p(), zn[0].get_T());		
//		double T_BurnAdiabat=0;
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
		super.buffer_singleResult("Zonenmasse [kg]", zn[0].get_m(),i);

		i+=1;
		double kontrolMasse=zn[0].get_p()*zn[0].get_V();		
		kontrolMasse=kontrolMasse/(zn[0].get_gasMixtureZone().get_R()*zn[0].get_T());
		super.buffer_singleResult("ZonenKontrollmasse [kg]", kontrolMasse,i);		
		
		i+=1;
		super.buffer_singleResult(" cv[J/kg]", zn[0].get_gasMixtureZone().get_cv_mass(zn[0].get_T()),i);	
		
		i+=1;
		super.buffer_singleResult(" cp [J/kg]", zn[0].get_gasMixtureZone().get_cp_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_singleResult(" kappa [-]", zn[0].get_gasMixtureZone().get_kappa(zn[0].get_T()),i);
		
		i+=1;
		super.buffer_singleResult(" U [J]", zn[0].get_gasMixtureZone().get_u_mass(zn[0].get_T())*zn[0].get_m(),i);			

		i+=1;
		int iter=i;
		double []mi=zn[0].get_mi();
		for(int idx=0;idx<mi.length;idx++)
			super.buffer_singleResult(CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+"_Massenbruch [kg]" ,mi[idx]/zn[0].get_m(),iter+idx);	

	}
	
	

	public Zone[] get_initialZonesWhileRunning() {
		try{
			throw new BirdBrainedProgrammerException(
					"Bei einem einzonigen Modell darf diese Methode nicht aufgerufen werden");
		}catch(BirdBrainedProgrammerException e){
			e.stopBremo();
		}
		return null;
	}



	public boolean initZonesWhileRunning() {
		return false;
	}

	
	public boolean initialiseBurntZone() {		
		return false;
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
//			if(me.get_AlleEinspritzungen()[i].get_EOI()>CP.get_verbrennungsBeginnSEC()){
//				try {
//					throw new ParameterFileWrongInputException("Für das gwaehlte Berechnungsmodell " +
//							"muessen die Einspritzungen eigentlich vor dem Verbrennungsbeginn liegen. \n" +
//							"für Einspritzung " +(i+1) +" trifft das aber nicht zu" );
//				} catch (ParameterFileWrongInputException e) {				
//					e.stopBremo();
//				}
//
//
//			}
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
