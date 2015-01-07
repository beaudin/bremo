package berechnungsModule.Berechnung;

import java.io.File;
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
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.turbulence.TurbulenceModel;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;




/**
 * Klasse zur Durchfuehrung einer DVA mit einer Zone. Es wird zunaechst das Frischgemisch 
 * inForm eines Spezies-Objektes aus Frischluft, AGR und allen Kraftstoffen definiert. 
 * Der Heizwert dieses Gemischs wird herangezogen um dQ in dm umzurechnen. 
 * Das verbrennende Massenelement wird der Zone entzogen verbrannt, d.h. es wird die Zusammensetzung 
 * des dissoziierenden Rauchgases bestimmt, und anschließend mit der Temperatur der Zone wieder beigemischt.
 * @author eichmeier
 *
 */
public class Weltformel extends DVA{

	private  WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private MasterEinspritzung masterEinspritzung;
	private BlowBy blowbyModell;
	private TurbulenceModel turb; //für Bargende
	private boolean krstVerbrannt=false;
	private boolean bargende = false; //Nur wenn Bargende  
	private boolean fvv = false;	//bzw. BargendeFVV
	
	
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
	
	
	private boolean esBrennt=false;
	
	private double dQburnMAX=0;
	private double fortschritt=0;	
	private double x,y;	
	
	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private misc.VektorBuffer T_buffer ;
	private misc.VektorBuffer dQb_buffer;
	private misc.VektorBuffer dQw_buffer;
	private misc.VektorBuffer dmb_buffer;
	
	

	protected Weltformel(CasePara cp) {
		super(cp);	
		
		ANZAHL_ZONEN=1;
		
		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;	
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		gg=CP.OHC_SOLVER;
		this.checkEinspritzungen(masterEinspritzung);
		blowbyModell = CP.BLOW_BY_MODELL;
		if(CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")){
			bargende = true;
		}
		if(CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("BargendeFVV")){
			fvv = true;
		}
		if(bargende||fvv){ //Nur wenn Bargende oder BargendeFVV
			turb = CP.TURB_FACTORY.get_TurbulenceModel();
		}
		T_buffer = new misc.VektorBuffer(cp);
		dQb_buffer = new misc.VektorBuffer(cp);
		dQw_buffer = new misc.VektorBuffer(cp);
		dmb_buffer = new misc.VektorBuffer(cp);

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[ANZAHL_ZONEN];

		
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
		double mKrstDampfINIT=masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0); 
		this.mINIT= mVerbrennungsLuft+mKrstDampfINIT;
		Spezies krst=masterEinspritzung.get_spezKrst_verdampft(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0);
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	
		
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mINIT);
		frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mINIT);		

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
		
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();	
		
		if(bargende||fvv){ //Nur wenn Bargende
			turb.initialize(initialZones, 0);
		}
		
		if(cp.RESTGASMODELL.involvesGasExchangeCalc()){
			String test = cp.get_workingDirectory();
			String pfadFinal=cp.get_workingDirectory()+cp.get_CaseName()+".lwa";
			File toDelete = new File(pfadFinal);
			toDelete.delete();
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
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();		
		Spezies krst=masterEinspritzung.get_spezKrstALL();	
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		double mKrst=masterEinspritzung.get_mKrst_Sum_ASP();
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
		double mGes= mVerbrennungsLuft+mKrst;
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		

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
				GasGemisch rauchgas;	
				rauchgas=new GasGemisch("Rauchgas");
				rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche
						(p, T, frischGemisch));
				
				//Massenelement wieder zumischen
				zonen_IN[0].set_dm_ein(dmZoneBurn, T, rauchgas);
				
			} catch (NegativeMassException nmE) {					
				nmE.log_Warning();
				krstVerbrannt=true;
			}	
		}
		if(bargende||fvv){ //Nur wenn Bargende
			this.turb.update(zonen_IN, time);
		}
		
		//Thermodynamisches Verdichtungsverhältnis, erweitert um Wandwärmeverluste und Leckage:
		y = 0;
		x = 0;
		if(CP.BERECHNUNGS_MODELL.isDVA()){
			T = zonen_IN[0].get_T(); //TODO ZEITSCHRITTE BERÜCKSICHTIGEN
			double kappa = zonen_IN[0].get_ggZone().get_kappa(T);
			y = y + (dQw/CP.SYS.WRITE_INTERVAL_SEC + zonen_IN[0].get_ggZone().get_cp_mass(T)*T*dmL) * (kappa-1) /
					super.get_dp(time); //dXv
			y = y - (kappa * zonen_IN[0].get_p()*
					(CP.MOTOR.get_V(time)-CP.MOTOR.get_V(time-CP.SYS.WRITE_INTERVAL_SEC) / CP.SYS.WRITE_INTERVAL_SEC) /
					super.get_dp(time)); //pdV
			y = y - (CP.MOTOR.get_V(time) - CP.MOTOR.get_V_MAX()/CP.MOTOR.get_Verdichtung()); //Vs
			
			x = kappa * (CP.MOTOR.get_V(time)-CP.MOTOR.get_V(time-CP.SYS.WRITE_INTERVAL_SEC) / CP.SYS.WRITE_INTERVAL_SEC) / 
					super.get_dp(time);
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


	
	boolean geschrieben = false;
	public void bufferErgebnisse(double time, Zone[] zn) {			
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
		mL=mL+dmL*super.CP.SYS.WRITE_INTERVAL_SEC;
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
		double xQ=Qb/Qmax;
		if(!geschrieben){
			super.buffer_EinzelErgebnis("Ergebnis",42,0);
			geschrieben = true;
		}
		
		double Tm=wandWaermeModell.get_Tmb(zn);
		T_buffer.addValue(time, Tm);
		double T_BurnAdiabat=HeizwertRechner.calcAdiabateFlammenTemp(super.CP,
				get_frischGemisch(), zn[0].get_p(), zn[0].get_T());	
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
		// TODO Auto-generated method stub 
		return null; 
		}
}
