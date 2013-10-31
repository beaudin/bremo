package berechnungsModule.Berechnung;


import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Hashtable;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.wandwaerme.PerfektIsoliert;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.BrennverlaufDaten;
import bremo.parameter.IndizierDaten;
import misc.VektorBuffer;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import bremo.parameter.CasePara;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoExceptions.StopBremoException;


/**
 * Klasse zur Durchfuehrung einer APR mit einer Zone.
 * Dabei wird mit vorgegebenem Brennverlauf, der entsprechende Zylinderdruck berechnet.
 */ 

public class APR_homogen_EinZonig extends APR{
	
	private  WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private MasterEinspritzung masterEinspritzung;
	
	
	private final int ANZAHL_ZONEN;
	
	private double dQw, Qw=0, Qb=0;
	double zonenMasseVerbrannt=0;
	private boolean krstVerbrannt=false;
	private double t_VerbrennungsBeginn;
	private boolean verbrennungHatBegonnen;
	
	
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
	
	//protected final IndizierDaten indiD; hier mit vorgegebenem Brennverlauf, statt Druck
	protected final BrennverlaufDaten brennverlauf;  
	
	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private misc.VektorBuffer T_buffer ;
	private misc.VektorBuffer dQb_buffer;
	private misc.VektorBuffer dQw_buffer;
	private misc.VektorBuffer dmb_buffer;
	private misc.VektorBuffer p_buffer;
	
	protected APR_homogen_EinZonig (CasePara cp, boolean waermeVerluste, String brennverlaufsart, double wertQ , double startQ) {
		super(cp);
		brennverlauf=new BrennverlaufDaten(cp,brennverlaufsart, wertQ, startQ); 		
		//ergBuffDebug=new ErgebnisBuffer(cp,"DVA_DEBUG_");		
		ANZAHL_ZONEN=1;
		this.createMe(cp, waermeVerluste);
	}
	protected APR_homogen_EinZonig (CasePara cp, boolean waermeVerluste, String brennverlaufsart, double wertQ) {
		super(cp);
		brennverlauf=new BrennverlaufDaten(cp,brennverlaufsart, wertQ); 		
		//ergBuffDebug=new ErgebnisBuffer(cp,"DVA_DEBUG_");		
		ANZAHL_ZONEN=1;
		this.createMe(cp, waermeVerluste);
		
	}
	
	protected APR_homogen_EinZonig (CasePara cp, boolean waermeVerluste, String brennverlaufsart) {
		super(cp);
		brennverlauf=new BrennverlaufDaten(cp,brennverlaufsart); 		
		//ergBuffDebug=new ErgebnisBuffer(cp,"DVA_DEBUG_");		
		ANZAHL_ZONEN=1;
		this.createMe(cp, waermeVerluste);
		
	}
	
	
	protected APR_homogen_EinZonig (CasePara cp) {
		super(cp);
		brennverlauf=new BrennverlaufDaten(cp);		
		//ergBuffDebug=new ErgebnisBuffer(cp,"DVA_DEBUG_");	
		ANZAHL_ZONEN=1;
		this.createMe(cp, true);
	}
	
	private void createMe(CasePara cp,boolean waermeVerluste){		
		motor = CP.MOTOR;
		if(waermeVerluste)
			wandWaermeModell=CP.WAND_WAERME;	
		else
			wandWaermeModell=new PerfektIsoliert(CP);
		
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		gg=CP.OHC_SOLVER;
		this.checkEinspritzungen(masterEinspritzung);
		
		T_buffer = new misc.VektorBuffer(cp);
		dQb_buffer = new misc.VektorBuffer(cp);
		dQw_buffer= new misc.VektorBuffer(cp);
		dmb_buffer = new misc.VektorBuffer(cp);
		p_buffer = new misc.VektorBuffer(cp);
		
		// Initialisieren der Anfangsbedingungen (TODO unvollständig)
		
		initialZones= new Zone[ANZAHL_ZONEN];
		
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
		IndizierDaten indiD = new IndizierDaten(cp);
		double p_init= indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		
/*	        // Versuche Anfangsdruck aus dem indizierfile zu lesen, ansonsten Umgebungsdruck als Anfangsbedingung
		try{
		IndizierDaten indiD = new IndizierDaten(cp);
		p_init=indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		
				}catch(InvalidParameterException e){
				System.err.println("WARNING: Es wurde kein Indizierfile gefunden um den Anfangsdruck einzulesen ---> Es wird mit 1bar gerechnet---> es können ungenauere Ergebnisse auftreten!");
		    	p_init=100000; // in [Pa] besser mit Anfangsdruck aus DVA

			}
		
*/		
		
		
		//V Init
		double V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//T Init		
		Spezies ggZone_init=gemischINIT;
		double R=ggZone_init.get_R();
		double T_init=(p_init*V_init)/(mINIT*R);
		
		initialZones[0]=new Zone(CP,p_init, V_init, T_init, 
				mINIT,ggZone_init , false,0);
		
		
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();	
		
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
	
	
	public Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {
		
		Spezies frischGemisch=this.get_frischGemisch();
		
		// dU=dQ_b+dQ_w+dW_k+dm_e*h_e+dm_a*h_a
		
		double dQburn = brennverlauf.get_dQburn(time);
		double p=zonen_IN[0].get_p();
		double T=zonen_IN[0].get_T();
		//Verbrennungswärme zuführen
		zonen_IN[0].set_dQ_ein_aus(dQburn);
		
		//Wandwärme bestimmen und dann abführen
		dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
		zonen_IN[0].set_dQ_ein_aus(-1*dQw);
		
		//Verdampfungswärme abführen
		zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

		//Einspritzung des Kraftstoffs
		zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);	
		
		
		//Verhindert das Aufsummieren des Fehlers wenn der Brennverlauf 
		//vor der eigentlichen Verbrennung etwas schwingt		
		dmZoneBurn=0;		
		if(esBrennt&&dQburn>0&&!krstVerbrannt) 
			dmZoneBurn=convert_dQburn_2_dmKrstBurn(dQburn,frischGemisch,T,p);
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
		
		
		
		return zonen_IN;
		
	}
	
	/**
	 * 
	 * @param dQburn
	 * @param frischGemisch
	 * @param Tu
	 * @param Tv
	 * @param p
	 * @return
	 */
	protected double convert_dQburn_2_dmKrstBurn(double dQburn, Spezies frischGemisch,
			double Tu,												
			double p){	
		double dmBurn=0;
		GleichGewichtsRechner gg=CP.OHC_SOLVER;
		GasGemisch abgas=new GasGemisch("Abgas");	
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, Tu, frischGemisch));

		//1.HS fuer durchstroemten Kontrollraum:
		//m*he-m*ha-Qb=0
		double he=frischGemisch.get_h_mass(Tu);
		double ha=abgas.get_h_mass(Tu);
		double delta_h=he-ha;

		dmBurn= 1*dQburn/delta_h;
		//ist eigentlich identisch Funktioniert aber nicht für fettes Gemsich
		//					return dQburn/1/frischGemisch.get_Hu_mass();
		if(dmBurn>0)
			return dmBurn;
		else
			return 0;

	}
	
	
	
	
	public boolean isDVA(){
		return false;
	}


	@Override
	public boolean initialiseBurntZone() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void bufferErgebnisse(double time, Zone[] zn) {
		double dQburn = brennverlauf.get_dQburn(time);
		double p = zn[0].get_p();
		dQb_buffer.addValue(time, dQburn);	
		dQw_buffer.addValue(time, dQw);	
		dmb_buffer.addValue(time, dmZoneBurn);
		p_buffer.addValue(time, p);
		

		if(dQburn>dQburnMAX)dQburnMAX=dQburn;

		if (CP.is_VerbrennungAutoDetect()==false)
		t_VerbrennungsBeginn=CP.get_verbrennungsBeginnSEC();
		else
			t_VerbrennungsBeginn= t_VerbrennungsBeginn ();
		if(time>=t_VerbrennungsBeginn){
			verbrennungHatBegonnen=true;}
		esBrennt=verbrennungHatBegonnen;
		
		
		//Berechnen integraler Werte
		zonenMasseVerbrannt=zonenMasseVerbrannt+dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		fortschritt=zonenMasseVerbrannt/mINIT;
		Qb=Qb+dQburn*super.CP.SYS.WRITE_INTERVAL_SEC;
		Qw=Qw+dQw*super.CP.SYS.WRITE_INTERVAL_SEC; 
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
		double xQ=Qb/Qmax;

		int i=-1;
		i+=1;
		super.buffer_EinzelErgebnis("Kurbelwinkel [°KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_EinzelErgebnis("Brennraumvolumen [m3]",motor.get_V(time),i);

		
		i+=1;
		super.buffer_EinzelErgebnis("p [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/s]",dQburn,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn),i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qb [J]", Qb,i);
		
		i+=1;
		super.buffer_EinzelErgebnis(" kappa [-]", zn[0].get_ggZone().get_kappa(zn[0].get_T()),i);
		
				
		i+=1;
		super.buffer_EinzelErgebnis("dQw [J/s]",dQw,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);	
		
		i+=1;		
		super.buffer_EinzelErgebnis("Qw [J]",Qw,i);		
		
		i+=1;		
		super.buffer_EinzelErgebnis("Xb[-]", fortschritt,i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qb/Qmax [-]", xQ,i);
		
		i+=1;
		double alpha=wandWaermeModell.get_WaermeUebergangsKoeffizient(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("Alpha [W/(m^2K)]", alpha, i);
		
		
		
				
	}


	@Override
	public Zone[] get_initialZones() {
		
		return initialZones;
	}


	@Override
	public Zone[] get_initialZonesWhileRunning() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int get_anzZonen() {
		return 1;
	}


	@Override
	public VektorBuffer get_dm_buffer() {
		
		return dmb_buffer;
	}


	@Override
	public VektorBuffer get_dQw_buffer() {
		
		return dQw_buffer;
	}


	@Override
	public VektorBuffer get_dQb_buffer() {
		
		return dQb_buffer;
	}


	@Override
	protected void checkEinspritzungen(MasterEinspritzung me) {
		// TODO Auto-generated method stub
		
	}
	
public VektorBuffer get_p_buffer() {
		
		return p_buffer;
	}

public double t_VerbrennungsBeginn () {
	double x0 =CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
	double time=0;
	int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
	boolean verbrennungsbeginnGefunden=false;
	
//	while(verbrennungsbeginnGefunden==false){
	for(int i=1;i<anzSimWerte;i++){
	time=x0+i*CP.SYS.WRITE_INTERVAL_SEC;
	double dQburn = brennverlauf.get_dQburn(time);
	Qb=Qb+dQburn*super.CP.SYS.WRITE_INTERVAL_SEC;
	double xQ=Qb/Qmax;
	if(xQ>0.05){
		verbrennungsbeginnGefunden=true;
	return time;}
	}
//	}
	
	return 0;
}
@Override
public Zone[] calc_dQburn(Zone[] zonen) {
	// TODO Auto-generated method stub
	return zonen;
}

}
