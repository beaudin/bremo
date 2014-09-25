package berechnungsModule.Berechnung;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import matLib.MatLibBase;
import misc.HeizwertRechner;
import misc.LittleHelpers;
import misc.VektorBuffer;
import berechnungsModule.blowby.BlowBy;
import berechnungsModule.blowby.BlowByFabrik;
import berechnungsModule.gemischbildung.Frommelt;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.turbulence.TurbulenceModel; //für Bargende
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;


/**
 * <p>Klasse zur Durchfuehrung einer DVA mit zwei Zonen. Der Kraftstoff wird in Zone 0 eingespritzt und vermischt sich 
 * sofort homogen mit der Verbrennungsluft. Aus Zone 0 wird das Gemisch entnommen, verbrannt und dann mit der 
 * Temperatur der unverbrannten Zone in die verbrannte Zone zugemsicht. Die Verbrennungswaerme wird vollstaendig Zone 1
 * zugefuehrt.  Alternativ koennte auch jedesmal die adiabate Verbrennungstemperatur berechnet werden und dann das 
 * Abgas mit dieser Temperatur der verbrannten Zone zugemischt werden. Ein Waermestrome wuerde dann nicht mehr auftreten.
 * Diese Art der Rechnung ist gleichwertig, die Rechenzeit steigt aber wegen der Berechnung der adiabaten Flammentemperatur 
 * deutlich an. </p> 
 * 
 *<p>Bei Direkteinspritzung ist darauf zu achten, dass der Kraftstoff vollstaendig verdampft ist bevor die Verbrennung 
 *einsetzt. Um die Menge des verbrennenden Massenelementes zu bestimmen wird der Heizwert des GasGemischs aus
 *Zone 0 verwendet. Wenn noch nicht der gesamte Kraftstoff eingespritzt ist, ist der Heizwert zu gering und 
 *es besteht die Moeglichkeit, dass die Zone vollstaendig entleert wird. </p>
 * @author eichmeier
 *
 */
public class DVA_homogen_ZweiZonig extends DVA {

	private  WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private MasterEinspritzung masterEinspritzung;
	private BlowBy blowbyModell;
	private TurbulenceModel turb; //für Bargende
	private boolean bargende = false; //Nur wenn Bargende


	private final int ANZAHL_ZONEN;

	private double dQw, Qw=0, Qb=0,mb=0;
	private double Qwp=0, Qwh=0, Qwl=0;
	private double dmL, mL=0;
	double zonenMasseVerbrannt=0;
	
	public double mverbrannt=0, munverbrannt=0;
	
	
	
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
	Zone [] initialZonesWhileRunning;	

	private boolean esBrennt=false,initialiseBurntZone=false,burntZonesAlreadyInitialised=false;

	private double dQburnMAX=0;
	private double fortschritt=0;	


	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private VektorBuffer T_buffer ;
	private VektorBuffer dQb_buffer ;
	private VektorBuffer dQw_buffer ;
	private VektorBuffer dmb_buffer ;



	protected DVA_homogen_ZweiZonig(CasePara cp) {
		super(cp);	

		ANZAHL_ZONEN=2;

		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;	
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		checkEinspritzungen(masterEinspritzung); //checkt ob alle Einspritzungen in die unverbrennte Zone erfolgen
		gg=CP.OHC_SOLVER;
		blowbyModell = CP.BLOW_BY_MODELL;
		
		if(CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")||
				CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("BargendeFVV")){
			bargende = true;
		}
		if(bargende){ //Nur wenn Bargende oder BargendeFVV
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
		
		
		//Initialisierung der Zonen
		double m1=(1+1e-6)*CP.SYS.MINIMALE_ZONENMASSE;
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
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
		
		if(bargende){ //Nur wenn Bargende
		turb.initialize(initialZones, 0); //für Bargende
		}

	}

	public double get_turbFaktor(Zone [] zonen_IN, double time){
		return turb.get_k(zonen_IN, time);
	}
	
	public int get_anzZonen() {		
		return ANZAHL_ZONEN;
	}


	public Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {			

		double dQburn=super.get_dQburn();

		//aktueller Zustand in der Zone
		double p=zonen_IN[0].get_p();
		double Tu=zonen_IN[0].get_T();
		double Vu=zonen_IN[0].get_V();

		double Vb=zonen_IN[1].get_V();


		dmZoneBurn=0;	
		
		double m0 = zonen_IN[0].get_m();
		double m1 = zonen_IN[1].get_m();
		double mges = m0 + m1;
		dmL = blowbyModell.get_mLeckage(time, zonen_IN)*CP.SYS.WRITE_INTERVAL_SEC;
		if(dmL>=0){
			if((m0-dmL)<=CP.SYS.MINIMALE_ZONENMASSE){}else{
				try {
					zonen_IN[0].set_dm_aus(m0/mges*dmL);
				} catch (NegativeMassException e) {
				}
			}
			if((m1-dmL)<=CP.SYS.MINIMALE_ZONENMASSE){}else{
				try {
					zonen_IN[1].set_dm_aus(m1/mges*dmL);
				} catch (NegativeMassException e) {
				}
			}
		}else{
			zonen_IN[0].set_dm_ein(-m0/mges*dmL,Tu,zonen_IN[0].get_ggZone());
			if(burntZonesAlreadyInitialised){
				zonen_IN[1].set_dm_ein(-m1/mges*dmL,zonen_IN[1].get_T(),zonen_IN[1].get_ggZone());
			}
		}
		


		if(burntZonesAlreadyInitialised){
			if(esBrennt&&dQburn>0) 
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,this.get_frischGemisch(),Tu,p);

				//Verbrennungswaerme zufuehren
				zonen_IN[1].set_dQ_ein_aus(1*dQburn);

			if(dmZoneBurn>0){
				if(zonen_IN[0].get_m()-CP.SYS.MINIMALE_ZONENMASSE>dmZoneBurn*CP.SYS.WRITE_INTERVAL_SEC){
					try {
						zonen_IN[0].set_dm_aus(dmZoneBurn,this.get_frischGemisch());
					} catch (NegativeMassException nmE) {					
						nmE.log_Warning();
						dmZoneBurn=0;
					}	

					GasGemisch rauchgas;				
					rauchgas=new GasGemisch("Rauchgas");

					rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche
							(p, Tu, this.get_frischGemisch()));	
					//				Berechnung der Dissoziation erfolgt in der Zone automatisch				
					zonen_IN[1].set_dm_ein(dmZoneBurn,Tu,rauchgas);	

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
			dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
			dQw=whtfMult*dQw;

			//Wandwaermestrom abfuehren
			double dQwu=dQw*Vu/(Vu+Vb);
			//unverbrannte Zone
			zonen_IN[0].set_dQ_ein_aus(-1*dQwu);		
			//verbrannte Zone
			double dQwb=dQw-dQwu;
			zonen_IN[1].set_dQ_ein_aus(-1*dQwb);
			
			//BlowBy Massenstrom abführen

			//Verdampfungswaerme abfuehren
			zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

			//Einspritzung des Kraftstoffs
			zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

		}else{

			//Wandwaermestrom bestimmen	
			dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
			dQw=whtfMult*dQw;
			//Wandwaermestrom (aus Zone0) abfuehren
			zonen_IN[0].set_dQ_ein_aus(-1*dQw); 	

			//Verbrennungswaerme (Zone0) zufuehren
			zonen_IN[0].set_dQ_ein_aus(dQburn);

			//Verdampfungswaerme abfuehren
			zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

			//Einspritzung des Kraftstoffs
			zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

			//Verhindert das Aufsummieren des Fehlers wenn der Brennverlauf 
			//vor der eigentlichen Verbrennung etwas schwingt				
			//passt eigentlich nicht, da dann esBrennt=false
			if(esBrennt&&dQburn>0)
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,zonen_IN[0].get_ggZone(),Tu,p);


		}
		
		if(bargende){ //Nur wenn Bargende
			this.turb.update(zonen_IN, time); //für Bargende
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
		double mKrst=masterEinspritzung.get_mKrst_Sum_ASP();
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
		double mGes= mVerbrennungsLuft+mKrst;
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		

		GasGemisch frischGemisch=new GasGemisch("Frischgemisch");	
		frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
		
		return frischGemisch;		
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

		//bei jedem Rechenschritt wird der Buffer mit den dQb-Werten aufgefüllt
		esBrennt=super.verbrennungHatBegonnen(time, dQb_buffer);

		if(esBrennt==true){
			this.mb=this.mb+this.dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		}

		if(mb>=super.CP.SYS.MINIMALE_ZONENMASSE && burntZonesAlreadyInitialised==false){
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

		mL=mL+dmL*CP.SYS.WRITE_INTERVAL_SEC;
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
		double xQ=Qb/Qmax;

		int i=-1;
		i+=1;
		super.buffer_EinzelErgebnis("Kurbelwinkel [°KW]",Math.round(10*super.CP.convert_SEC2KW(time))/10.0,i);

		i+=1;
		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_EinzelErgebnis("Brennraumvolumen [m3]",motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("Vu [m3]",zn[0].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("Vb [m3]",zn[1].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("T_Zone_u [K]",zn[0].get_T(),i);

//		double T1=0, k1=0;
//		if(burntZonesAlreadyInitialised){
//		//if(esBrennt){
//			T1=zn[1].get_T();
//			k1=zn[1].get_ggZone().get_kappa(zn[1].get_T());
//			}
//		else{
//			T1=zn[0].get_T();
//			k1=zn[0].get_ggZone().get_kappa(zn[0].get_T());
//		}
		
		i+=1;
		super.buffer_EinzelErgebnis("T_Zone_b [K]",zn[1].get_T(),i);
//		super.buffer_EinzelErgebnis("T_Zone_b [K]",T1,i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		i+=1;
		double T_BurnAdiabat=HeizwertRechner.calcAdiabateFlammenTemp(super.CP,
				zn[0].get_ggZone(), zn[0].get_p(), zn[0].get_T());	

		super.buffer_EinzelErgebnis("T_BurnAdiabat [K]", T_BurnAdiabat,i);

		double pSoll=indiD.get_pZyl(time);
		i+=1;
		super.buffer_EinzelErgebnis("p_soll [bar]", pSoll*1E-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("p [bar]",zn[0].get_p()*1e-5,i);
		
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
		
//		i+=1;		
//		super.buffer_EinzelErgebnis("Init", burntZonesAlreadyInitialised?1.0:0.0,i);
//		
//		i+=1;		
//		super.buffer_EinzelErgebnis("brennt's", esBrennt?1.0:0.0,i);

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
		super.buffer_EinzelErgebnis("Hu frischGemsich [J/kg]", 
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
		

//		//Polytropenexponent für die Schleppdruckberechnung ermitteln.
//		//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
//		double refPunkt=CP.get_refPunkt_WoschniHuber();
//		double[] n_array = new double[10]; 
//		double pZyl_a = indiD.get_pZyl(refPunkt);
//		double Vol_a = motor.get_V(refPunkt);
//		double pZyl_b = 0;
//		double Vol_b = 0;
//		int cnt=0;
//
////		Schleifen-Abbruch nicht mit A<B sondern B-A>sehr kleinem Wert nahe Null!?
//		for(double kw=CP.convert_SEC2KW(refPunkt)-10; (CP.convert_SEC2KW(refPunkt)-kw) > 1E-6; kw++){
////		for(double kw=CP.convert_SEC2KW(refPunkt)-10; kw < CP.convert_SEC2KW(refPunkt); kw++){ //ORIGINAL
////		for(double kw=refPunkt-CP.convert_KW2SEC(10); kw < refPunkt; kw++){
//			pZyl_b=indiD.get_pZyl(CP.convert_KW2SEC(kw));
//			Vol_b=motor.get_V(CP.convert_KW2SEC(kw));
//			n_array[cnt]=Math.log10(pZyl_a/pZyl_b)/Math.log10(Vol_b/Vol_a);
//			cnt++;
//		}
//
//		double n=MatLibBase.mw_aus_1DArray(n_array); //Polytropenexponent
//		double Schleppdruck = pZyl_a*Math.pow((Vol_a/motor.get_V(time)),n)*1E-5; //[bar]
//		i+=1;
//		super.buffer_EinzelErgebnis("pSchleppRefPnktWH [bar]",Schleppdruck,i);
//
//		///////////////////////////		

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
		double HeatFluxU = wandWaermeModell.get_WandWaermeStromDichteUnverbrannt(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSDu [MW/m^2]",HeatFluxU*1E-6,i);
		
		i+=1;
		double HeatFluxB = wandWaermeModell.get_WandWaermeStromDichteVerbrannt(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSDb [MW/m^2]",HeatFluxB*1E-6,i);
		
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
		super.buffer_EinzelErgebnis("Brennraumfläche [m^2]",motor.get_BrennraumFlaeche(time),i);		

		i+=1;
		super.buffer_EinzelErgebnis("Gesamtmasse [kg]", zn[0].get_m()+zn[1].get_m(),i);

		i+=1;		
		super.buffer_EinzelErgebnis("m_u[kg]", zn[0].get_m(),i);

		i+=1;		
		super.buffer_EinzelErgebnis("m_b[kg]", zn[1].get_m(),i);

		i+=1;
		super.buffer_EinzelErgebnis(" cv_u[J/kg]", zn[0].get_ggZone().get_cv_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_EinzelErgebnis(" cp_u [J/kg]", zn[0].get_ggZone().get_cp_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_EinzelErgebnis(" kappa_u [-]", zn[0].get_ggZone().get_kappa(zn[0].get_T()),i);

		i+=1;
		super.buffer_EinzelErgebnis(" U_u [J]", zn[0].get_ggZone().get_u_mass(zn[0].get_T())*zn[0].get_m(),i);

		i+=1;
		super.buffer_EinzelErgebnis(" cv_b[J/kg]", zn[1].get_ggZone().get_cv_mass(zn[1].get_T()),i);	

		i+=1;
		super.buffer_EinzelErgebnis(" cp_b [J/kg]", zn[1].get_ggZone().get_cp_mass(zn[1].get_T()),i);	

		i+=1;
		super.buffer_EinzelErgebnis(" kappa_b [-]", zn[1].get_ggZone().get_kappa(zn[1].get_T()),i);
//		super.buffer_EinzelErgebnis(" kappa_b [-]", k1,i);

		i+=1;
		super.buffer_EinzelErgebnis(" U_b [J]", zn[1].get_ggZone().get_u_mass(zn[1].get_T())*zn[1].get_m(),i);


		i+=1;
		int iter=i;		
		double []mi=zn[0].get_mi();
		double m_ges=zn[0].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_EinzelErgebnis("Zu_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,i);			
		}

		i+=1;
		iter=i;	
		mi=zn[1].get_mi();
		m_ges=zn[1].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_EinzelErgebnis("Zb_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,iter+idx);
		}		
		
		// buffer mass of fuel and characteristic evaporation time for each injection
		int einspritzungen = CP.MASTER_EINSPRITZUNG.get_AllInjections().length;
		for(int index=0; index<einspritzungen; index++){
			if(CP.MASTER_EINSPRITZUNG.get_ModulWahl(CP.MASTER_EINSPRITZUNG.EINSPRITZ_MODELL_FLAG+index, CP.MASTER_EINSPRITZUNG.MOEGLICHE_EINSPRITZ_MODELLE).equals(Frommelt.FLAG)){ //Nur wenn Frommelt
				i+=1;
				super.buffer_EinzelErgebnis("Kraftstoffmasse_" + index + " [kg]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Mass(time), i);
				i+=1;
				super.buffer_EinzelErgebnis("Kraftstoffrate_" + index + " [kg/s]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Rate(time), i);
				i+=1;
				super.buffer_EinzelErgebnis("Tau_" + index + " [s]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Tau(time), i);
			}
			i+=1;
			super.buffer_EinzelErgebnis("Kraftstoffdampf_" + index + " [kg]", this.masterEinspritzung.get_Einspritzung(index).get_mKrst_verdampft(time), i);	
		}
		
		i+=1;
		double pV=zn[0].get_p()*zn[0].get_V();
		double mRT=zn[0].get_m()*zn[0].get_ggZone().get_R()*zn[0].get_T();
		super.buffer_EinzelErgebnis("pV-mRT zn0", (pV-mRT)/pV*100,i);
		
		i+=1;
		pV=zn[1].get_p()*zn[1].get_V();
		mRT=zn[1].get_m()*zn[1].get_ggZone().get_R()*zn[1].get_T();
		super.buffer_EinzelErgebnis("pV-mRT zn1", (pV-mRT)/pV*100,i);
		
		
		if(CP.ITERATIVE_BERECHNUNG.isIterativ()){
			double wert = super.get_ErgebnisBuffer().get_bufferedErgebnis(time, CP.ITERATIVE_BERECHNUNG.get_Parameter2Save());
			CP.ITERATIVE_BERECHNUNG.bufferParameter(time, wert);
		}

	}


	private void initBurnedZone(double time, Zone[] zonen_IN){
		//TODO mach mich public und ruf mich vom solver aus auf
		initialZonesWhileRunning=new Zone[zonen_IN.length];

		double T_BurnAdiabat=HeizwertRechner.calcAdiabateFlammenTemp(super.CP,
				zonen_IN[0].get_ggZone(), zonen_IN[0].get_p(), zonen_IN[0].get_T()); 
		double Tb=T_BurnAdiabat;	

		double p=zonen_IN[0].get_p();
		GasGemisch rauchgas=new GasGemisch("rg");
		rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche
				(p, Tb, zonen_IN[0].get_ggZone()));	


		double Vb_init=this.mb*rauchgas.get_R()*Tb/p;
		//Berechnen der Einzelmassen in der verbrannten Zone
		Hashtable<Spezies,Double> ht=new Hashtable<Spezies,Double>();		
		ht=LittleHelpers.addiereHashs(ht, 
				rauchgas.get_speziesMassenBruecheDetailToIntegrate(), mb);
		initialZonesWhileRunning[1]=new Zone(CP,zonen_IN[1].get_p_V_T_mi(),zonen_IN[1].isBurnt(),zonen_IN[1].getID());
		initialZonesWhileRunning[1].set_p_V_T_mi(p, Vb_init, Tb,ht);

		double mub=zonen_IN[0].get_m()+zonen_IN[1].get_m()-mb; 
		double Vub=motor.get_V(time)-Vb_init;
		double Tub=p*Vub/(mub*zonen_IN[0].get_ggZone().get_R());			

		//Berechnen der Einzelmassen in der unverbrannten Zone
		Hashtable<Spezies,Double> ht2=new Hashtable<Spezies,Double>();		
		ht2=LittleHelpers.addiereHashs(ht2, 
				zonen_IN[0].get_ggZone().get_speziesMassenBruecheDetailToIntegrate(), mub);


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
		// TODO: Auto-generated method stub 
		return null; 
	}
}
