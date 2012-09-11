package bremo.parameter;

import java.io.File;
import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;

import matLib.MatLibBase;
import matLib.filter.SavitzkyGolayFilter;
import misc.LinInterp;

import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

import io.IndizierFileReader;
import io.IndizierFileReader_adv;
import io.IndizierFileReader_txt;

public class IndizierDaten {


	private double [] zeitAchse;
	private double [] pZyl,pZylRoh;
	private double [] pEin,pEinRoh;
	private double [] pAus,pAusRoh;
	double [] zeitAchse_LW;
	private final CasePara CP;
	private double PZYL_MAX;
	private  double pmi=0;
	private Motor motor;
	private LinInterp L_Interp;
	private SavitzkyGolayFilter sgol;
	private final boolean filternBitte;
	
	
	
	/**
	 * <p>Klasse die Indizeirdaten wie pZyl, pEin, und pAus 
	 * fuer alle uebrigen Module zur verfuegung stellt. </p>
	 * <p>Der Zylinderdruck wird mit einer der Vorgabe im Parameterfile entsprechenden Methode
	 * abgeglichen. Auch Saugrohr und Abgasgegenndruck koennen mit die langsam messenden Sensoren 
	 * abgeglichen werden.</p>
	 * Weiterhin wird fuer HubKolbenMotoren in dieser Klasse pmi berechnet.
	 * @param  CasePara cp
	 */
	public IndizierDaten(CasePara cp){			

		CP=cp;
		filternBitte=CP.SYS.FILTERN;
		createMe(cp,false);
		}
		public IndizierDaten(CasePara cp, boolean gemittelt){
		CP=cp;
		filternBitte=CP.SYS.FILTERN;
		createMe(cp,gemittelt);
		}
		public void createMe(CasePara cp, boolean gemittelt){
		File indiFile= CP.SYS.INDIZIER_FILE;		
		String fileName=indiFile.getName();
		L_Interp = new LinInterp(CP);
		motor=CP.MOTOR;		
		
	
		if(filternBitte){
			int halbeBreite=(CP.SYS.SGOLAY_BREITE-1)/2;
			sgol=new SavitzkyGolayFilter(halbeBreite,halbeBreite,CP.SYS.SGOLAY_ORDNUNG);
		}
		
		double dauerASP=CP.SYS.DAUER_ASP_KW;
		int pZylNr=CP.SYS.KANAL_SPALTEN_NR_PZYL;
		int pEinNr=CP.SYS.KANAL_SPALTEN_NR_PEIN;
		int pAusNr=CP.SYS.KANAL_SPALTEN_NR_PABG;		

		IndizierFileReader indiReader = null;

		if (fileName.endsWith("txt") || fileName.endsWith("TXT"))
			indiReader=new IndizierFileReader_txt(CP,indiFile.getAbsolutePath(),pZylNr,pEinNr,pAusNr,dauerASP);
		if(fileName.endsWith("adv")||fileName.endsWith("ADV"))
			indiReader=new IndizierFileReader_adv(CP,indiFile.getAbsolutePath(),pZylNr,pEinNr,pAusNr);
		if(indiReader==null){
			try{
				throw new ParameterFileWrongInputException("Der Dateityp des Indizierdatenfiles \""+fileName +"\"" +
				"kann nicht verarbeitet werden" );
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();

			}
		}
		////////////////////////////////////
		//Definieren der Zeitachse
		///////////////////////////////////	
		//die Zeitachse wird verdoppelt damit fuer die 
		//LWA das zweite Arbeitsspiel zur Verfuegung steht
		zeitAchse=indiReader.get_Zeitachse();
		double temp=zeitAchse[zeitAchse.length-1]-zeitAchse[0]+zeitAchse[1]-zeitAchse[0]; //max-min+Auflösung -->Werte für den nächsten Zyklus
		double [] temp2=new double[zeitAchse.length];
		for(int i=0; i<temp2.length; i++){
			temp2[i]=zeitAchse[i]+temp;
		}		   
		zeitAchse=misc.LittleHelpers.concat(zeitAchse, temp2);
		
		////////////////////////////////////
		//Definieren des Einlassdrucks
		///////////////////////////////////		
		if(gemittelt == false){
		pEinRoh=indiReader.get_pEin();
		//Anpassendes Mittelwertes von Saugrohrdrucksensor und Piezo
		if(CP.SYS.SHIFT_pEIN){
			double offset=CP.get_p_LadeLuft()-matLib.MatLibBase.mw_aus_1DArray(pEinRoh);
			pEinRoh=this.shiftMe(pEinRoh, offset);
		}
		//Filtern
		if(this.filternBitte)
			pEin=sgol.filterData(pEinRoh);
		else
			pEin=pEinRoh;
		
		//Anpassen fuer die LWA (doppelte laenge fuer zweites ASP)
		pEin=misc.LittleHelpers.concat(pEin, pEin);	
		
		////////////////////////////////////
		//Definieren des Auslassdrucks
		///////////////////////////////////	
		pAusRoh=indiReader.get_pAbg();
		if(CP.SYS.SHIFT_pAUS){
			double offset=CP.get_p_Abgas()-matLib.MatLibBase.mw_aus_1DArray(pAusRoh);
			pAusRoh=this.shiftMe(pAusRoh, offset);
		}
		//Filtern
		if(this.filternBitte)
			pAus=sgol.filterData(pAusRoh);
		else
			pAus=pAusRoh;
		pAus=misc.LittleHelpers.concat(pAus,pAus);		
		}
		if(gemittelt == true){
		pEinRoh=indiReader.get_pEin();
		pAusRoh=indiReader.get_pAbg();
		double summePein=0;
		double summePaus=0;
		for (int i = 0; i < pEinRoh.length; i++) {
		summePein += pEinRoh[i];
		}
		for (int i = 0; i < pAusRoh.length; i++) {
		summePaus += pAusRoh[i];
		}
		double pEinWert=summePein/pEinRoh.length;
		double pAusWert=summePaus/pAusRoh.length;
		pEin = new double[zeitAchse.length];
		pAus = new double[zeitAchse.length];
		for (int i = 0; i < zeitAchse.length; i++) {
		pEin [i]= pEinWert;
		pAus [i]= pAusWert;
		}
		}
		////////////////////////////////////
		//Definieren des Zylidnerdrucks
		///////////////////////////////////
		pZylRoh=indiReader.get_pZyl();
		//Abgleich der Zylinderdruckkorrektur
		if(CP.SYS.NULLLINIEN_METHODE.equalsIgnoreCase("polytropenMethode")){
			pZylRoh=this.polytropenMethode(pZylRoh);	
		}else if(CP.SYS.NULLLINIEN_METHODE.equalsIgnoreCase("abgleichSaugrohr")){
			pZylRoh=this.kanalMethode(pZylRoh,pEinRoh);	
		}else if(CP.SYS.NULLLINIEN_METHODE.equalsIgnoreCase("abgleichKruemmer")){
			pZylRoh=this.kanalMethode(pZylRoh,pAusRoh);	
		}else if(CP.SYS.NULLLINIEN_METHODE.equalsIgnoreCase("referenzWert")){
			pZylRoh=this.referenzWertMethode();	
		}else if(CP.SYS.NULLLINIEN_METHODE.equalsIgnoreCase("ohne")){
			
		}else{
			try {
				throw new ParameterFileWrongInputException(
						"Die gewaehlte Methode zur Nullinienkorrektur des Zylinderdrucks " +
						"ist nicht zulaessig. Moegliche Methoden sind: \n" +
						"polytropenMethode \n abgleichSaugrohr \n abgleichKruemmer \n " +
						"referenzWert \n ohne");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
		}		
		
		
		/////////////////////////////////
		//pmi-Berechnung
		////////////////////////////////
		if(motor.isHubKolbenMotor()){
			//Schleife über Wert 0 bis n-2
			int pktProAS = pZylRoh.length;
			double kw=0.0;
			pmi=0;
			for(int i=0; i < pktProAS-1; i++){
				kw = i*CP.SYS.DAUER_ASP_KW/pktProAS-360;
				pmi+=0.5*(pZylRoh[i]+pZylRoh[i+1])*(motor.get_V(CP.convert_KW2SEC(kw+CP.SYS.DAUER_ASP_KW/pktProAS))
						-motor.get_V(CP.convert_KW2SEC(kw)));
			}
			pmi=pmi/((Motor_HubKolbenMotor) motor).get_Hubvolumen();	// Wert wird in [Pa] ausgegeben	
		}
		
		//Filtern
		if(this.filternBitte)
			pZyl=sgol.filterData(pZylRoh);
		else
			pZyl=pZylRoh;
		//Verdoppeln des Zylinderdrucks damit dieser fuer die LWA das naechste ASP umfasst
		pZyl=misc.LittleHelpers.concat(pZyl, pZyl);
		PZYL_MAX=indiReader.get_pZylMAX();		
		
		L_Interp.set_lastsearchedIndex(0); 		

	}

	/**
	 * Liefert den maximalen Zylinderdruck
	 * @return pZylMax [Pa]
	 */
	public double get_pZyl_MAX(){
		return PZYL_MAX;
	}

	/**
	 * Liefert den Zylinderdruck in [Pa] zum Zeitpunkt time in [s nach Rechenbeginn].
	 * Trifft die Zeit nicht exakt einen gemesenen Wert wird linear interpoliert.
	 * @param time in [s nach Rechenbeginn]
	 * @return Zylinderdruck in [Pa]
	 */
	public double get_pZyl(double time){
		return L_Interp.linInterPol(time, zeitAchse, pZyl);
	}	
	
	/**
	 * Liefert den ungefilterten des Zylinderdrucks in [Pa] zum Zeitpunkt time in [s nach Rechenbeginn].
	 * Trifft die Zeit nicht exakt einen gemesenen Wert wird linear interpoliert.
	 * @param time in [s nach Rechenbeginn]
	 * @return Zylinderdruck in [Pa]
	 */
	public double get_pZylRoh(double time){
		return L_Interp.linInterPol(time, zeitAchse, pZylRoh);
	}	

	/**
	 * Liefert den Einlassdruck in [Pa] zum Zeitpunkt time in [s nach Rechenbeginn].
	 * Trifft die Zeit nicht exakt einen gemesenen Wert wird linear interpoliert.
	 * @param time in [s nach Rechenbeginn]
	 * @return Zylinderdruck in [Pa]
	 */
	public double get_pEin(double time){
		return L_Interp.linInterPol(time, zeitAchse, pEin);
	}	

	/**
	 * Liefert den Auslassdruck in [Pa] zum Zeitpunkt time in [s nach Rechenbeginn].
	 * Trifft die Zeit nicht exakt einen gemesenen Wert wird linear interpoliert.
	 * @param time in [s nach Rechenbeginn]
	 * @return Auslassdruck in [Pa]
	 */
	public double get_pAus(double time){
		return L_Interp.linInterPol(time, zeitAchse, pAus);
	}

	/** 
	 * Berechnet pmi in [Pa]
	 */
	public double get_pmi(){
		if(motor.isHubKolbenMotor()){
			return pmi;
		}else{
			try{
				throw new ParameterFileWrongInputException(
						"pmi kann nur fuer Motoren" +
				"vom Typ HubKolbenMotor berechnet werden!");
			}catch(ParameterFileWrongInputException pfwie){
				pfwie.stopBremo();

			}
			return 0;
		}
	}

	/**
	 * Diese Methode fuert einen Nulllinienabgleich nach der KanalMethode durch
	 * @param pZyl
	 * @param pAbgleich
	 * @return
	 */
	private double [] kanalMethode(double [] pZyl, double[] pAbgleich){	
		if(motor.isHubKolbenMotor()){
			double deltat=zeitAchse[0]-zeitAchse[1];
			double es=((Motor_HubKolbenMotor) motor).get_Einlass_schliesst();
			double eo=((Motor_HubKolbenMotor) motor).get_Einlass_oeffnet();
			double tAbgleich=(es+eo)/2;
			double pZyl_temp []=new double [11];
			double pEin_temp []=new double [11];
			for(int i=0;i<11;i++){
				double time=tAbgleich+(i-5)*deltat;				
				pZyl_temp[i]=L_Interp.linInterPol(time, zeitAchse, pZyl);
				pEin_temp[i]=L_Interp.linInterPol(time, zeitAchse, pAbgleich);
			}
			double pOffset=MatLibBase.mw_aus_1DArray(pZyl_temp)-
								MatLibBase.mw_aus_1DArray(pEin_temp);

			for(int xx=0;xx<pZyl.length;xx++){
				pZyl[xx]=pZyl[xx]-pOffset;
			}
		}else{
			try{
				throw new ParameterFileWrongInputException(
						"Die Zylinderdruckkorrektur nach der Kanalmethode ist nur mit Motoren" +
				"vom Typ HubKolbenMotor moeglich!");
			}catch(ParameterFileWrongInputException pfwie){
				pfwie.stopBremo();

			}
		}
		return pZyl;
	}

	/**
	 * Diese Methode fuert einen Nulllinienabgleich nach der polytropenMethode durch
	 * @param pZyl
	 * @return
	 */
	private double [] polytropenMethode(double [] pZyl){

		double t_Beginn = CP.get_DruckabgleichBeginn();
        double t_Ende = CP.get_DruckabgleichEnde();
        double deltat=zeitAchse[0]-zeitAchse[1];
		
		double v1= motor.get_V(t_Beginn);
		double v2= motor.get_V(t_Ende);
		
		//Berechnen des Zylinderdrucks auf Basis mehrerer Wert --> Rauschunterdrückung
		double pZyl_temp_1_ []=new double [5];
		double pZyl_temp_2_ []=new double [5];
		
		for(int i=0;i<5;i++){
			double time=t_Beginn+(i-2)*deltat;				
			pZyl_temp_1_[i]=L_Interp.linInterPol(time, zeitAchse, pZyl);
			time=t_Ende+(i-2)*deltat;
			pZyl_temp_2_[i]=L_Interp.linInterPol(time, zeitAchse, pZyl);
		}
		double pZyl_temp_1=MatLibBase.mw_aus_1DArray(pZyl_temp_1_);
		
		double pZyl_temp_2=MatLibBase.mw_aus_1DArray(pZyl_temp_2_);
		
		//Frischgemisch als Spezies Objekt erstellen
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	
		MasterEinspritzung me=CP.MASTER_EINSPRITZUNG;
		Spezies krst=me.get_spezKrstALL();	
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		double mKrst=me.get_mKrst_Sum_ASP();
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
		double mGes= mVerbrennungsLuft+mKrst;
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		

		GasGemisch frischGemisch=new GasGemisch("Frischgemisch");	
		frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);
		double T=300;
		double kappa = frischGemisch.get_kappa(T); //CP.get_Kappa_Druckabgleich();//(polyIdy)
		
        
		double pZyl_temp_1_ABS = 
		(pZyl_temp_2-pZyl_temp_1)/(Math.pow((v1/v2),kappa)-1);
		
		double pDelta = pZyl_temp_1_ABS-pZyl_temp_1;

		 for(int i=0;i<pZyl.length;i++){pZyl[i]= pZyl[i]+pDelta;}  
	
		return pZyl;
	}

	private double[] referenzWertMethode() {
		try{
			throw new BirdBrainedProgrammerException(
					"Diese Methode wurde leider noch nicht implementiert --> NICHT aufregen! \n" +
					" Selber Hand anlegen!! \n " +
					"Gruß Juwe");
		}catch(BirdBrainedProgrammerException bbp){
			bbp.stopBremo();
		}
		return null;
	}

	/**
	 * Addiert den angegebenen Offset zu jedem Wert des Vektors
	 * @param pVec
	 * @param offset
	 * @return
	 */
	private double [] shiftMe(double [] pVec, double offset){

		for(int xx=0;xx<pVec.length;xx++){
			pVec[xx]=pVec[xx]+offset;
		}
		return pVec;
	}

}
