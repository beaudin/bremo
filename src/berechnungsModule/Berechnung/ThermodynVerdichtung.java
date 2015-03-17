package berechnungsModule.Berechnung;

import java.util.Hashtable;

import matLib.MatLibBase;
import misc.HeizwertRechner;
import misc.VektorBuffer;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.blowby.BlowBy;
import berechnungsModule.gemischbildung.Frommelt;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.turbulence.TurbulenceModel;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;




/**
 * Klasse zur Durchfuehrung einer DVA mit einer Zone für den Schleppbetrieb. Anschließend erfolgt die Berechnung des
 * thermodynamischen Verdichtungsverhältnisses.
 * 
 * <br> Inputfileparameter: whtfMult [-], mAGR_extern [kg/h], mLuft_feucht [kg/h] 
 * @author neurohr
 *
 */
public class ThermodynVerdichtung extends DVA{

	private  WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private BlowBy blowbyModell;
	private TurbulenceModel turb; //für Bargende
	private boolean krstVerbrannt=false;
	private boolean bargende = false; //Nur wenn Bargende
	
	
	private final int ANZAHL_ZONEN;
	
	private double dQw, Qw=0, Qb=0;
	private double Qwp=0, Qwh=0, Qwl=0;
	private double dmL, mL=0;
	double zonenMasseVerbrannt=0;
	
	private double whtfMult=CP.get_whtfMult();
	
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
	
	
	private double dQburnMAX=0;
	private double fortschritt=0;	
	private double x,y,V_c_thermo = -5.55;
	private double phi_S,phi_th,p_alt=0,dXv_alt=0;
	
	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private misc.VektorBuffer T_buffer ;
	private misc.VektorBuffer dQb_buffer;
	private misc.VektorBuffer dQw_buffer;
	private misc.VektorBuffer dmb_buffer;
	private misc.VektorBuffer kappa_buffer;
	
	

	protected ThermodynVerdichtung(CasePara cp) {
		super(cp);	
		
		ANZAHL_ZONEN=1;
		
		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;
		gg=CP.OHC_SOLVER;
		blowbyModell = CP.BLOW_BY_MODELL;
		
		if(CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")||
		   CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("BargendeFVV")||
		   CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("BargendeHeinle")){
			bargende = true;
		}
		if(bargende){ //Nur wenn Bargende oder BargendeFVV
			turb = CP.TURB_FACTORY.get_TurbulenceModel();
		}
		
		T_buffer = new misc.VektorBuffer(cp);
		dQb_buffer = new misc.VektorBuffer(cp);
		dQw_buffer = new misc.VektorBuffer(cp);
		dmb_buffer = new misc.VektorBuffer(cp);
		kappa_buffer = new misc.VektorBuffer(cp);

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[ANZAHL_ZONEN];

		
		this.mINIT=CP.get_mVerbrennungsLuft_ASP();
		double anteil=1/(1+cp.get_mWasser_Luft_ASP()/cp.get_mLuft_trocken_ASP());
		Hashtable<Spezies,Double>verbrennungsLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		verbrennungsLuft_MassenBruchHash.put(cp.SPEZIES_FABRIK.get_spezLuft_trocken(),anteil);		
		verbrennungsLuft_MassenBruchHash.put(cp.SPEZIES_FABRIK.get_spezH2O(),1-anteil);
		GasGemisch verbrennungsLuft=new GasGemisch("Verbrennungsluft");
		verbrennungsLuft.set_Gasmischung_massenBruch(verbrennungsLuft_MassenBruchHash);
		
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, 1.0);

		GasGemisch gemischINIT=new GasGemisch("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
		
	
		//Anfangsbedingungen Setzen
		//p Init
		double p_init=super.indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//V Init
		double V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//T Init		
		Spezies ggZone_init=gemischINIT;
		double R=ggZone_init.get_R();
		double T_init=(p_init*V_init)/(mINIT*R);		

		initialZones[0]=new Zone(CP,p_init, V_init, T_init, 
				mINIT,ggZone_init, false,0);
		
		if(bargende){ //Nur wenn Bargende
			turb.initialize(initialZones, 0);
		}
	}
	
	public double get_turbFaktor(Zone [] zonen_IN, double time){
		return turb.get_k(zonen_IN, time);
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
		double anteil=1/(1+CP.get_mWasser_Luft_ASP()/CP.get_mLuft_trocken_ASP());
		Hashtable<Spezies,Double>verbrennungsLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		verbrennungsLuft_MassenBruchHash.put(CP.SPEZIES_FABRIK.get_spezLuft_trocken(),anteil);		
		verbrennungsLuft_MassenBruchHash.put(CP.SPEZIES_FABRIK.get_spezH2O(),1-anteil);
		GasGemisch verbrennungsLuft=new GasGemisch("Verbrennungsluft");
		verbrennungsLuft.set_Gasmischung_massenBruch(verbrennungsLuft_MassenBruchHash);
		
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, 1.0);	

		GasGemisch frischGemisch=new GasGemisch("Frischgemisch");	
		frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
		
		return frischGemisch;		
	}



	public int get_anzZonen() {		
		return ANZAHL_ZONEN;
	}


	public Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {	
		
		Spezies frischGemisch=this.get_frischGemisch();
		
		double dQburn=super.get_dQburn();
		
		//aktueller Zustand in der Zone
		double p=zonen_IN[0].get_p();
		double T=zonen_IN[0].get_T();		
		
		//Wandwaermestrom bestimmen	
		dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
		dQw=whtfMult*dQw;
		//Wandwaermestrom abfuehren
		zonen_IN[0].set_dQ_ein_aus(-1*dQw);	
		
		//Leckagestrom abführen
		dmL = blowbyModell.get_mLeckage(time, zonen_IN)*CP.SYS.WRITE_INTERVAL_SEC;
		if(dmL>=0){
			try{
				zonen_IN[0].set_dm_aus(dmL);
			}catch(NegativeMassException nme){
				nme.log_Warning("BlowBy führte zu einer Entleerung der Zone ! \n" +
						"BlowBy-Eingaben überprüfen.");
				nme.stopBremo();
			}
		}else{
			zonen_IN[0].set_dm_ein(-dmL, T, zonen_IN[0].get_ggZone());
		}
		
		//Verbrennungswaerme zufuehren
		zonen_IN[0].set_dQ_ein_aus(dQburn);
		
		//Verbrennendes Masseelement entnehmen
		if(dmZoneBurn>0){
			try {
				zonen_IN[0].set_dm_aus(dmZoneBurn,frischGemisch);
				
				//Massenelement wieder zumischen
				zonen_IN[0].set_dm_ein(dmZoneBurn, T, frischGemisch);
				
			} catch (NegativeMassException nmE) {					
				nmE.log_Warning();
				krstVerbrannt=true;
			}	
		}
		if(bargende){ //Nur wenn Bargende
			this.turb.update(zonen_IN, time);
		}
		//Thermodynamisches Verdichtungsverhältnis, erweitert um Wandwärmeverluste und Leckage:
		y = 0;
		x = 0;
		if(CP.BERECHNUNGS_MODELL.isDVA()){
			T = zonen_IN[0].get_T();
			double kappa = zonen_IN[0].get_ggZone().get_kappa(T);
			x = kappa * (CP.MOTOR.get_V(time)-CP.MOTOR.get_V(time-CP.SYS.WRITE_INTERVAL_SEC)) / 
					(indiD.get_pZyl(time)-indiD.get_pZyl(time-CP.SYS.WRITE_INTERVAL_SEC));
			
			y = y + (dQw + zonen_IN[0].get_ggZone().get_cp_mass(T)*T*dmL) * CP.SYS.WRITE_INTERVAL_SEC * (kappa-1) /
					(indiD.get_pZyl(time)-indiD.get_pZyl(time-CP.SYS.WRITE_INTERVAL_SEC)); //dXv
			y = y - zonen_IN[0].get_p()* x; //pdV
			y = y - (CP.MOTOR.get_V(time) - CP.MOTOR.get_V_MAX()/CP.MOTOR.get_Verdichtung()); //Vs
		}
		
		return zonen_IN;			
	}
	
	public double[] kompressionsVolumen(){
		double[] ret = new double[2];
		ret[0] = x;
		ret[1] = y;
		return ret;
	}


	 
	public Zone[] get_initialZones() {		
		return initialZones;
	}


	

	public void bufferErgebnisse(double time, Zone[] zn) {			
		double dQburn=super.get_dQburn();
		dQb_buffer.addValue(time, dQburn);	
		dQw_buffer.addValue(time, dQw);	

		if(dQburn>dQburnMAX)dQburnMAX=dQburn;
		
//		Für Thermodynamisches Verdichtungsverhältnis:
		if(indiD.get_pZyl(time)>p_alt){
			p_alt = indiD.get_pZyl(time);
			phi_th = time;
		}
		double T = zn[0].get_T();
		double dXv = dQw + zn[0].get_ggZone().get_cp_mass(T) * T * dmL;
		if(dXv>dXv_alt){
			dXv_alt = dXv;
			phi_S = time;
		}
		
		//Berechnen integraler Werte
		zonenMasseVerbrannt=zonenMasseVerbrannt+dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		fortschritt=zonenMasseVerbrannt/mINIT;
		Qb=Qb+dQburn*super.CP.SYS.WRITE_INTERVAL_SEC;
		Qw=Qw+dQw*super.CP.SYS.WRITE_INTERVAL_SEC;
		mL=mL+dmL*super.CP.SYS.WRITE_INTERVAL_SEC;
		double xQ=Qb/Qmax;

		int i=-1;
		i+=1;
//		super.buffer_EinzelErgebnis("Kurbelwinkel [°KW]",super.CP.convert_SEC2KW(time),i);
		super.buffer_EinzelErgebnis("Kurbelwinkel [°KW]",Math.round(10*super.CP.convert_SEC2KW(time))/10.0,i);

		i+=1;
		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_EinzelErgebnis("Brennraumvolumen [m3]",motor.get_V(time),i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		double pSoll=indiD.get_pZyl(time);
		i+=1;
		super.buffer_EinzelErgebnis("p_soll [bar]", pSoll*1E-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("p [bar]",zn[0].get_p()*1e-5,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("dXv [-]", dXv, i);
		
		i+=1;
		super.buffer_EinzelErgebnis("dQh [J/s]",dQburn-dQw,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("dQh [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn-dQw),i);
		
		i+=1;		
		super.buffer_EinzelErgebnis("Qh [J]", Qb-Qw,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/s]",dQburn,i);
		CP.set_ParaInputfile("spalte_dQburn", "[-]", i+1);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn),i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qb [J]", Qb,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("dQw [J/s]",dQw,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);	
		
		i+=1;		
		super.buffer_EinzelErgebnis("Qw [J]",Qw,i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qb/Qmax [-]", xQ,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("Qmax [J]", Qmax,i);	
		
		i+=1;
		super.buffer_EinzelErgebnis("dmL [kg/s]", dmL, i);
		
		i+=1;
		super.buffer_EinzelErgebnis("dmL [kg/KW]", super.CP.convert_ProSEC_2_ProKW(dmL), i);
		
		i+=1;
		super.buffer_EinzelErgebnis("mL [kg]", mL, i);
		
		i+=1;
		double Hu=zn[0].get_ggZone().get_Hu_mass();
		super.buffer_EinzelErgebnis("Hu Zone [J/kg]",Hu,i );	
		
		i+=1;
		super.buffer_EinzelErgebnis("Hu frischGemisch [J/kg]", 
				this.get_frischGemisch().get_Hu_mass(),i);		
		
		i+=1;
		super.buffer_EinzelErgebnis("Phi_Delay [°KW]",10*fortschritt*1000/(CP.get_DrehzahlInUproSec()*60),i);

		i+=1;
		double alpha=wandWaermeModell.get_WaermeUebergangsKoeffizient(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("Alpha [W/(m^2K)]", alpha, i);
		
		if(bargende){ //Nur wenn Bargende
			i+=1;
			double k=turb.get_k(zn, time);
			super.buffer_EinzelErgebnis("k_turb [m^2/s^2]", k, i);
		}
		
		//Schleppdruck in bar
		i+=1;
		//super.buffer_EinzelErgebnis("Schleppdruck [bar]",wandWaermeModell.get_Schleppdruck(time, zn)*1E-5,i);
		super.buffer_EinzelErgebnis("pSchleppWHT [bar]",wandWaermeModell.get_Schleppdruck()*1E-5,i);		
		
		i+=1;
		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD [MW/m^2]",HeatFlux*1E-6,i);
		
		i+=1;
		double HeatFluxPiston = wandWaermeModell.get_WandWaermeStromDichtePiston(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD Kolben [MW/m^2]",HeatFluxPiston*1E-6,i);
		
		i+=1;
		double HeatFluxHead = wandWaermeModell.get_WandWaermeStromDichteHead(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD Head [MW/m^2]",HeatFluxHead*1E-6,i);
		
		i+=1;
		double HeatFluxCyl = wandWaermeModell.get_WandWaermeStromDichteCyl(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD Liner [MW/m^2]",HeatFluxCyl*1E-6,i);
		
		i+=1;
		double whtp = wandWaermeModell.get_WandWaermeStromPiston(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("dQw Kolben [J/s]",whtp,i);
		Qwp=Qwp+whtp*super.CP.SYS.WRITE_INTERVAL_SEC; //Kommt einen Zeitschrit zu spät?
		
		i+=1;
		double whth = wandWaermeModell.get_WandWaermeStromHead(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("dQw Head [J/s]",whth,i);	
		Qwh=Qwh+whth*super.CP.SYS.WRITE_INTERVAL_SEC; //Kommt einen Zeitschrit zu spät?
		
		i+=1;
		double whtl = wandWaermeModell.get_WandWaermeStromCyl(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("dQw Liner [J/s]",whtl,i);
		Qwl=Qwl+whtl*super.CP.SYS.WRITE_INTERVAL_SEC; //Kommt einen Zeitschrit zu spät?
		
		i+=1;
		super.buffer_EinzelErgebnis("Qw Kolben [J]",Qwp,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("Qw Head [J]",Qwh,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("Qw Liner [J]",Qwl,i);
		
//		i+=1;
//		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt, T_buffer);
//		super.buffer_EinzelErgebnis("Wandwärmestromdichte [MW/m^2]",HeatFlux*1E-6,i);
//		
//		i+=1;		
//		super.buffer_EinzelErgebnis("PQw [W]",HeatFlux*motor.get_BrennraumFlaeche(time),i);	
		
		i+=1;		
		super.buffer_EinzelErgebnis("Brennraumfläche [m^2]",wandWaermeModell.get_BrennraumFlaeche(time),i);		

		i+=1;
		super.buffer_EinzelErgebnis("Zonenmasse [kg]", zn[0].get_m(),i);

		i+=1;
		double kontrolMasse=zn[0].get_p()*zn[0].get_V();		
		kontrolMasse=kontrolMasse/(zn[0].get_ggZone().get_R()*zn[0].get_T());
		super.buffer_EinzelErgebnis("ZonenKontrollmasse [kg]", kontrolMasse,i);		
		
		i+=1;
		super.buffer_EinzelErgebnis(" cv[J/kg]", zn[0].get_ggZone().get_cv_mass(zn[0].get_T()),i);	
		
		i+=1;
		super.buffer_EinzelErgebnis(" cp [J/kg]", zn[0].get_ggZone().get_cp_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_EinzelErgebnis(" kappa [-]", zn[0].get_ggZone().get_kappa(zn[0].get_T()),i);
		kappa_buffer.addValue(time,zn[0].get_ggZone().get_kappa(zn[0].get_T()));
		
		i+=1;
		super.buffer_EinzelErgebnis(" lambda [-]", zn[0].get_ggZone().get_lambda(), i);
		
		i+=1;
		super.buffer_EinzelErgebnis(" U [J]", zn[0].get_ggZone().get_u_mass(zn[0].get_T())*zn[0].get_m(),i);			

		i+=1;
		int iter=i;
		double []mi=zn[0].get_mi();
		for(int idx=0;idx<mi.length;idx++)
			super.buffer_EinzelErgebnis(CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+"_Massenbruch [kg]" ,mi[idx]/zn[0].get_m(),iter+idx);	

		
		if(CP.ITERATIVE_BERECHNUNG.isIterativ()){
			double wert = super.get_ErgebnisBuffer().get_bufferedErgebnis(time, CP.ITERATIVE_BERECHNUNG.get_Parameter2Save());
			CP.ITERATIVE_BERECHNUNG.bufferParameter(time, wert);
		}
		
		if(time>=CP.convert_KW2SEC(0) && time <CP.convert_KW2SEC(0)+CP.SYS.WRITE_INTERVAL_SEC){
			berechne_V_c_thermo();
		}
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
	protected void checkEinspritzungen(MasterEinspritzung me) {
		for(int i=0;i<me.get_AllInjections().length;i++){
			if(me.get_AllInjections()[i].get_ID_Zone()!=0){
				try {
					throw new ParameterFileWrongInputException("Für das gwaehlte Berechnungsmodell " +
							"koennen die Einspritzungen " +
							"nur in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone "+ 
							me.get_AllInjections()[i].get_ID_Zone());
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
	public VektorBuffer get_dQw_buffer() {
		return this.dQw_buffer;
	}
	
	@Override
	public VektorBuffer get_dQb_buffer() {
		return this.dQb_buffer;
	}


	@Override
	public VektorBuffer get_dm_buffer() {
		return this.dmb_buffer;
	}

	//fuer Verlustteilung Frank Haertel
	@Override 
	public VektorBuffer get_p_buffer() {  
		return null; 
		}
	
	private void berechne_V_c_thermo(){
		if(V_c_thermo==-5.55){
			double[] xArray = new double[40]; //lt. Burkhardt
			double[] yArray = new double[40]; //lt. Burkhardt
			
			double phi_plus,phi_minus;
			double[] dV=new double[2],dp=new double[2],V_s=new double[2],p_s=new double[2];
			double V_c_input = motor.get_V_MAX() / motor.get_Verdichtung();
			double kappa = kappa_buffer.getValue(phi_S);
			double schritt = CP.SYS.WRITE_INTERVAL_SEC;
			double time_to_OT = CP.convert_KW2SEC(0)-phi_S;
			
			for(int i=0;i<40;i++){ //TODO VOM ZOT schrittweise nach spät, dann "ployfit"
				phi_minus = phi_S-time_to_OT-(i+1)*schritt;
				phi_plus = phi_S+time_to_OT+(i+1)*schritt;
				
				dV[0] = (motor.get_V(phi_minus)-motor.get_V(phi_minus-schritt))/schritt;
				dV[1] = (motor.get_V(phi_plus)-motor.get_V(phi_plus-schritt))/schritt;
				dp[0] = (indiD.get_pZyl(phi_minus)-indiD.get_pZyl(phi_minus-schritt))/schritt;
				dp[1] = (indiD.get_pZyl(phi_plus)-indiD.get_pZyl(phi_plus-schritt))/schritt;
				
				xArray[i] = -kappa * (dV[0]-dV[1]) / (dp[1] - dp[0]); //x-Wert
				
				V_s[0] = motor.get_V(phi_minus)-V_c_input;
				V_s[1] = motor.get_V(phi_plus)-V_c_input;
				p_s[0] = indiD.get_pZyl(phi_minus);
				p_s[1] = indiD.get_pZyl(phi_plus);
				
				yArray[i] = (kappa * (p_s[0]*dV[0]-p_s[1]*dV[1]) + V_s[0]*dp[0]-V_s[1]*dp[1]) / (dp[1] - dp[0]); // y-Wert
			}
			double[] coeff = matLib.VectorTools.lineareRegression(xArray, yArray);
			V_c_thermo = coeff[1];
			((Motor_HubKolbenMotor) motor).set_Epsilon_thermo_sym(V_c_thermo);
		}
	}
	
	/**
	 * Schlepp-Verlustwinkel für thermodyn. Verd.
	 * @author neurohr
	 * @return
	 */
	public double get_phi_S(){
		return CP.convert_SEC2KW(phi_S);
	}
	/**
	 * Thermodynamischer Verlustwinkel
	 * @author neurohr
	 * @return
	 */
	public double get_phi_th(){
		return CP.convert_SEC2KW(phi_th);
	}
}

