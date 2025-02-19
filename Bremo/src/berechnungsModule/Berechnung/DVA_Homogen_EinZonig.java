package berechnungsModule.Berechnung;

import java.util.Hashtable;
import matLib.MatLibBase;
import misc.HeizwertRechner;
import misc.VektorBuffer;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;




/**
 * Klasse zur Durchfuehrung einer DVA mit einer Zone. Es wird zunaechst das Frischgemisch 
 * inForm eines Spezies-Objektes aud Frischluft, AGR und allen Kraftstoffen definiert. 
 * Der Heizwert dieses Gemsichs wird herangezogen um dQ in dm umzurechnen. 
 * Das verbrennende Massenelement wird der Zone entzogen verbrannt, d.h. es wird die Zusammensetzung 
 * des dissoziierenden Rauchgases bestimmt, und anschlie�end mit der Temperatur der Zone wieder beigemischt.
 * @author eichmeier
 *
 */
public class DVA_Homogen_EinZonig extends DVA{

	private  WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private MasterEinspritzung masterEinspritzung;
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
	
	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verf�gung stehen sollen
	private misc.VektorBuffer T_buffer ;
	private misc.VektorBuffer dQb_buffer;
	private misc.VektorBuffer dQw_buffer;
	private misc.VektorBuffer dmb_buffer;
	
	

	protected DVA_Homogen_EinZonig(CasePara cp) {
		super(cp);	
		
		ANZAHL_ZONEN=1;
		
		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;	
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		gg=CP.OHC_SOLVER;
		this.checkEinspritzungen(masterEinspritzung);
		
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
		
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25�C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();	
		
	}
	
	
	/**
	 * Erstellt eine Objetk vom Typ Spezies, als Mischung aus Verbrennungsluft (Luft + AGR) 
	 * und allen Kraftstoffen die in dem Arbeitsspiel verbrannt werden. Unabh�ngig von der 
	 * tatsaechlichen Gemischbildung verbrennt der Kraftstoff immer mit dem 
	 * selben Luftverh�ltnis
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
		//Wandwaermestrom abfuehren
		zonen_IN[0].set_dQ_ein_aus(-1*dQw);			
	
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

		return zonen_IN;			
	}


	 
	public Zone[] get_initialZones() {		
		return initialZones;
	}


	

	public void bufferErgebnisse(double time, Zone[] zn) {			
		double dQburn=super.get_dQburn();
		dQb_buffer.addValue(time, dQburn);	
		dQw_buffer.addValue(time, dQw);	
		dmb_buffer.addValue(time, dmZoneBurn);

		if(dQburn>dQburnMAX)dQburnMAX=dQburn;

		//bei jedem Rechenschritt wird der Buffer mit den dQb-Werten aufgef�llt
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
		super.buffer_EinzelErgebnis("Kurbelwinkel [�KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_EinzelErgebnis("Brennraumvolumen [m3]",motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("T_Zone_1 [K]",zn[0].get_T(),i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);
		
		i+=1;
		double T_BurnAdiabat=HeizwertRechner.calcAdiabateFlammenTemp(super.CP,
				get_frischGemisch(), zn[0].get_p(), zn[0].get_T());		
//		double T_BurnAdiabat=0;
		super.buffer_EinzelErgebnis("T_BurnAdiabat [K]", T_BurnAdiabat,i);

		double pSoll=indiD.get_pZyl(time);
		i+=1;
		super.buffer_EinzelErgebnis("p_soll [bar]", pSoll*1E-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("p [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/s]",dQburn,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn),i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qb [J]", Qb,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("dmb [kg/s]", dmZoneBurn,i);

		i+=1;
		super.buffer_EinzelErgebnis("dmb [kg/KW]", super.CP.convert_ProSEC_2_ProKW(dmZoneBurn),i);
		
		i+=1;	
		super.buffer_EinzelErgebnis("mb [kg]", zonenMasseVerbrannt,i);
		
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
		super.buffer_EinzelErgebnis("Qmax [J]", Qmax,i);	

		i+=1;
		double Hu=zn[0].get_ggZone().get_Hu_mass();
		super.buffer_EinzelErgebnis("Hu Zone [J/kg]",Hu,i );	
		
		i+=1;
		super.buffer_EinzelErgebnis("Hu frischGemsich [J/kg]", 
				this.get_frischGemisch().get_Hu_mass(),i);		
		
		i+=1;
		super.buffer_EinzelErgebnis("Phi_Delay [�KW]",10*fortschritt*1000/(CP.get_DrehzahlInUproSec()*60),i);

		i+=1;
		double alpha=wandWaermeModell.get_WaermeUebergangsKoeffizient(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("Alpha [W/(m^2K)]", alpha, i);
		
		
		//Polytropenexponent f�r die Schleppdruckberechnung ermitteln.
		//Dies wird in den 10�KW vorm Referenzpunkt gemacht...
		double refPunkt=CP.get_refPunkt_WoschniHuber();
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
		super.buffer_EinzelErgebnis("pSchlepp [bar]",Schleppdruck,i);
		
		///////////////////////////		
		
		i+=1;
		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("Wandw�rmestromdichte [MW/m^2]",HeatFlux*1E-6,i);
		
		i+=1;		
		super.buffer_EinzelErgebnis("PQw [W]",HeatFlux*motor.get_BrennraumFlaeche(time),i);	
		
		i+=1;		
		super.buffer_EinzelErgebnis("Brennraumfl�che [m^2]",motor.get_BrennraumFlaeche(time),i);		

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
		
		i+=1;
		super.buffer_EinzelErgebnis(" U [J]", zn[0].get_ggZone().get_u_mass(zn[0].get_T())*zn[0].get_m(),i);			

		i+=1;
		int iter=i;
		double []mi=zn[0].get_mi();
		for(int idx=0;idx<mi.length;idx++)
			super.buffer_EinzelErgebnis(CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
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
	protected void checkEinspritzungen(MasterEinspritzung me) {
		for(int i=0;i<me.get_AllInjections().length;i++){
			if(me.get_AllInjections()[i].get_ID_Zone()!=0){
				try {
					throw new ParameterFileWrongInputException("F�r das gwaehlte Berechnungsmodell " +
							"koennen die Einspritzungen " +
							"nur in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone "+ 
							me.get_AllInjections()[i].get_ID_Zone());
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}
//			if(me.get_AlleEinspritzungen()[i].get_EOI()>CP.get_verbrennungsBeginnSEC()){
//				try {
//					throw new ParameterFileWrongInputException("F�r das gwaehlte Berechnungsmodell " +
//							"muessen die Einspritzungen eigentlich vor dem Verbrennungsbeginn liegen. \n" +
//							"f�r Einspritzung " +(i+1) +" trifft das aber nicht zu" );
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


	@Override
	public VektorBuffer get_p_buffer() {
		// TODO Auto-generated method stub
		return null;
	}

}
