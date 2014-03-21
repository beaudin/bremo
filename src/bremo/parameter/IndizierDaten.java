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
	private final double PZYL_MAX=0;//vorher ohne NULL
	private  double pmi=0;
	private Motor motor;
	private LinInterp L_Interp;
	private SavitzkyGolayFilter sgol;
	private final boolean filternBitte;
	private double pOffset; //Offset Zylinderdruck für PostProcessor
	
	
	
	/**
	 * <p>Klasse die Indizierdaten wie pZyl, pEin, und pAus 
	 * fuer alle uebrigen Module zur verfuegung stellt. </p>
	 * <p>Der Zylinderdruck wird mit einer der Vorgabe im Parameterfile entsprechenden Methode
	 * abgeglichen. Auch Saugrohr und Abgasgegenndruck koennen mit die langsam messenden Sensoren 
	 * abgeglichen werden.</p>
	 * Weiterhin wird fuer HubKolbenMotoren in dieser Klasse pmi berechnet.
	 * @param  CasePara cp
	 */
	public IndizierDaten(CasePara cp){			

		CP=cp;
		
		filternBitte=CP.filterPressureData();
		//filternBitte=CP.SYS.FILTERN; 							//fuer Verlustteilung Frank Haertel
	    createMe(cp,false); 									//fuer Verlustteilung Frank Haertel
	 } 															//fuer Verlustteilung Frank Haertel
	
	 public IndizierDaten(CasePara cp, boolean gemittelt){ 		//fuer Verlustteilung Frank Haertel
	    CP=cp; 													//fuer Verlustteilung Frank Haertel
	    filternBitte=CP.filterPressureData();
	    //filternBitte=CP.SYS.FILTERN; 							//fuer Verlustteilung Frank Haertel
	    createMe(cp,gemittelt); 								//fuer Verlustteilung Frank Haertel
	 }															//fuer Verlustteilung Frank Haertel 	
	 
	 public void createMe(CasePara cp, boolean gemittelt){ 		//fuer Verlustteilung Frank Haertel
		
		File indiFile=CP.get_FileToRead("indizierFileName");	
		int indexOf=indiFile.getName().indexOf(".");
		String EINGABEDATEI_FORMAT=indiFile.getName().substring(indexOf+1); //Dateiendung		
		int pZylNr=CP.get_ColumnToRead("spalte_pZyl");				
		int pEinNr=CP.get_ColumnToRead("spalte_pEin");		
		int pAusNr=CP.get_ColumnToRead("spalte_pAbg");	
		if(pZylNr==pEinNr || pZylNr==pAusNr || pEinNr==pAusNr){
			try {
				throw new ParameterFileWrongInputException("Die angegebenen Kanalnummern bzw Spaltennummern " +
						"für pZyl, pEin und pAbg sind teilweise identisch");
			} catch (ParameterFileWrongInputException e1) {
				// TODO Auto-generated catch block
				e1.stopBremo();
			}
		}		
		
		String fileName=indiFile.getName();
		L_Interp = new LinInterp(CP);
		motor=CP.MOTOR;		
		
		//filternBitte=CP.filterPressureData(); //fuer Verlustteilung Frank Haertel
		if(filternBitte){
			int halfWidth=cp.get_savitzkyGolayHalfWidth();
			sgol=new SavitzkyGolayFilter(halfWidth,halfWidth,cp.get_savitzkyGolayOrder());
		}		

		IndizierFileReader indiReader = null;

		if (fileName.endsWith("txt") || fileName.endsWith("TXT")){
			if(pZylNr==1|| pEinNr==1 || pAusNr==1){
				String kanal=null;
				if(pZylNr==1)kanal="spalte_pZyl";
				if(pEinNr==1)kanal="spalte_pAbg";
				if(pAusNr==1)kanal="spalte_pEin";
				try {
					throw new ParameterFileWrongInputException("Der Wert für \"" + kanal+ "\" wurde auf die erste Spalte des Druckeingabefiles gesetzt. \n " +
							"In der ersten spalte muss aber immer die Zeit bzw. der Kurbelwinkel stehen.");
				} catch (ParameterFileWrongInputException e) {					
					e.stopBremo();
				}	
			}			
			indiReader=new IndizierFileReader_txt(CP,indiFile.getAbsolutePath(),pZylNr,pEinNr,pAusNr);
		}
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
		
		if(gemittelt == false){ //fuer Verlustteilung Frank Haertel
		pEinRoh=indiReader.get_pEin();
		//Anpassendes Mittelwertes von Saugrohrdrucksensor und Piezo		
		if(CP.shift_pInlet()){
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
		if(CP.shift_pOutlet()){
			double offset=CP.get_p_Abgas()-matLib.MatLibBase.mw_aus_1DArray(pAusRoh);
			pAusRoh=this.shiftMe(pAusRoh, offset);
		}
		//Filtern
		if(this.filternBitte)
			pAus=sgol.filterData(pAusRoh);
		else
			pAus=pAusRoh;
		pAus=misc.LittleHelpers.concat(pAus,pAus);		
		
		//fuer Verlustteilung Frank Haertel
		} 
	    if(gemittelt == true){ 
	      pEinRoh=indiReader.get_pEin(); 
	      pAusRoh=indiReader.get_pAbg(); 						//fuer Verlustteilung Frank Haertel
	      double summePein=0; 
	      double summePaus=0; 
	      for (int i = 0; i < pEinRoh.length; i++) { 
	          summePein += pEinRoh[i]; 
	      } 
	      for (int i = 0; i < pAusRoh.length; i++) { 			//fuer Verlustteilung Frank Haertel
	          summePaus += pAusRoh[i]; 
	      } 
	      double pEinWert=summePein/pEinRoh.length; 
	      double pAusWert=summePaus/pAusRoh.length; 
	      pEin = new double[zeitAchse.length]; 
	      pAus = new double[zeitAchse.length]; 
	      for (int i = 0; i < zeitAchse.length; i++) { 			//fuer Verlustteilung Frank Haertel
	          pEin [i]= pEinWert; 
	          pAus [i]= pAusWert; 
	      } 
	    } 
	    //fuer Verlustteilung Frank Haertel
		
		////////////////////////////////////
		//Definieren des Zylidnerdrucks
		///////////////////////////////////
		pZylRoh=indiReader.get_pZyl();
		//Abgleich der Zylinderdruckkorrektur
		String nlm=CP.get_pressureAdjustmentMethod("pressureAdjustmentMethod");
		if(nlm.equalsIgnoreCase("polytropenMethode")){
			pZylRoh=this.polytropenMethode(pZylRoh);	
		}else if(nlm.equalsIgnoreCase("abgleichSaugrohr")){
			pZylRoh=this.kanalMethodeSaugrohr(pZylRoh,pEinRoh);	
		}else if(nlm.equalsIgnoreCase("kanalMethode")){
			pZylRoh=this.kanalMethode(pZylRoh,pEinRoh,pAusRoh);	
		}else if(nlm.equalsIgnoreCase("abgleichKruemmer")){
			pZylRoh=this.kanalMethodeKruemmer(pZylRoh,pAusRoh);	
		}else if(nlm.equalsIgnoreCase("referenzWert")){
			//pZylRoh=this.referenzWertMethode();	
			pZylRoh=this.referenzWertMethode(pZylRoh);	
		}else if(nlm.equalsIgnoreCase("offset")){
			double offset = CP.get_pressureOffset();
			pZylRoh = this.shiftMe(pZylRoh, offset);
		}else if(nlm.equalsIgnoreCase("ohne")){
			
		}else{
			try {
				throw new ParameterFileWrongInputException(
						"Die gewaehlte Methode zur Nullinienkorrektur des Zylinderdrucks " +
						"ist nicht zulaessig. Moegliche Methoden sind: \n" +
						"polytropenMethode \n abgleichSaugrohr \n kanalMethode \n abgleichKruemmer \n " +
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
		//PZYL_MAX=indiReader.get_pZylMAX();		
		
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
	 * Diese Methode fuert einen Nulllinienabgleich nach der KanalMethode durch (Einlass- oder Aulasskanal, Zeitpunkt variabel)
	 * @param pZyl
	 * @param pEinlass
	 * @param pAbgas
	 * @return
	 */
	private double [] kanalMethode(double [] pZyl, double[] pEinlass, double[] pAbgas){	
		if(motor.isHubKolbenMotor()){
			double deltat=zeitAchse[0]-zeitAchse[1];
			double dab=CP.get_DruckabgleichBeginn();
			double dae=CP.get_DruckabgleichEnde();
			double abgleichDauer=dae-dab;
			double schrittZahl=(-abgleichDauer)/deltat;
			if (schrittZahl>1){
			double [] pAbgleich=pAbgas;
			if(CP.get_Referenzkanal()){
				pAbgleich=pEinlass;
			}
			double pZyl_temp []=new double [(int)Math.round(schrittZahl)];
			double pEin_temp []=new double [(int)Math.round(schrittZahl)];
			for(int i=0;i<=schrittZahl;i++){
				double time=dab-(i*deltat);			
				pZyl_temp[i]=L_Interp.linInterPol(time, zeitAchse, pZyl);
				pEin_temp[i]=L_Interp.linInterPol(time, zeitAchse, pAbgleich);
			}
			pOffset=MatLibBase.mw_aus_1DArray(pZyl_temp)-
								MatLibBase.mw_aus_1DArray(pEin_temp);

			pZyl = this.shiftMe(pZyl, pOffset);
			
			}else {
	    		try{
	    			throw new ParameterFileWrongInputException(
	    					"Beginn und Ende des Druckabgleiches dürfen nicht identisch sein. Der Beginn muss vor dem Ende liegen.");
	    		}catch(ParameterFileWrongInputException pfwie){
	    			pfwie.stopBremo();

	    		}
			
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
	 * Diese Methode fuert einen Nulllinienabgleich nach der KanalMethode durch (Saugrohr)
	 * @param pZyl
	 * @param pAbgleich
	 * @return
	 */
	private double [] kanalMethodeSaugrohr(double [] pZyl, double[] pAbgleich){	
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
			pOffset=MatLibBase.mw_aus_1DArray(pZyl_temp)-
								MatLibBase.mw_aus_1DArray(pEin_temp);

			pZyl = this.shiftMe(pZyl, pOffset);
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
	 * Diese Methode fuert einen Nulllinienabgleich nach der KanalMethode durch (Kruemmer)
	 * @param pZyl
	 * @param pAbgleich
	 * @return
	 */
	private double [] kanalMethodeKruemmer(double [] pZyl, double[] pAbgleich){	
		if(motor.isHubKolbenMotor()){
			double deltat=zeitAchse[0]-zeitAchse[1];
			double as=((Motor_HubKolbenMotor) motor).get_Auslass_schliesst();
			double ao=((Motor_HubKolbenMotor) motor).get_Auslass_oeffnet();
			double tAbgleich=(as+ao)/2;
			double pZyl_temp []=new double [11];
			double pEin_temp []=new double [11];
			for(int i=0;i<11;i++){
				double time=tAbgleich+(i-5)*deltat;				
				pZyl_temp[i]=L_Interp.linInterPol(time, zeitAchse, pZyl);
				pEin_temp[i]=L_Interp.linInterPol(time, zeitAchse, pAbgleich);
			}
			pOffset=MatLibBase.mw_aus_1DArray(pZyl_temp)-
								MatLibBase.mw_aus_1DArray(pEin_temp);

			pZyl = this.shiftMe(pZyl, pOffset);
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
        
        if (t_Beginn-t_Ende<0){
        
        double deltat=zeitAchse[0]-zeitAchse[1];
		
		double v1= motor.get_V(t_Beginn);
		double v2= motor.get_V(t_Ende);
		
		//Berechnen des Zylinderdrucks auf Basis mehrerer Werte --> Rauschunterdrückung
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
		
		double kappa;
		
		if(CP.RESTGASMODELL.involvesGasExchangeCalc()) {
			kappa = CP.get_Kappa_Druckabgleich();//(polyIdy)
		} else {
			
//		//Frischgemisch als Spezies Objekt erstellen
//		//Ersatz des Verbrennungsluftaufrufes mit AGRintern=0
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();
//		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuftPolytropenmethode();	
		MasterEinspritzung me=CP.MASTER_EINSPRITZUNG;
		Spezies krst=me.get_spezKrstALL();	
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		double mKrst=me.get_mKrst_Sum_ASP();
//		//Aufruf ohne zirkuläre Referenz
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
//		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP_Polytropenmethode();	
		double mGes= mVerbrennungsLuft+mKrst;
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		
//
		GasGemisch frischGemisch=new GasGemisch("Frischgemisch");	
		frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);
//		//double T=300; //Temperatur statisch bei 300K, gewählt von Juwe
		double T=pZyl_temp_1*v1/(mGes*frischGemisch.get_R()); //Temperatur zu Beginn des Abgleichs aus idealer Gasgleichung
		kappa = frischGemisch.get_kappa(T); //CP.get_Kappa_Druckabgleich();//(polyIdy)

		}
		
		double pZyl_temp_1_ABS = 
		(pZyl_temp_2-pZyl_temp_1)/(Math.pow((v1/v2),kappa)-1);
		
		pOffset = pZyl_temp_1_ABS-pZyl_temp_1;

		pZyl = this.shiftMe(pZyl, pOffset);
    	}
    	else {
    		try{
    			throw new ParameterFileWrongInputException(
    					"Beginn und Ende des Druckabgleiches dürfen nicht identisch sein. Der Beginn muss vor dem Ende liegen.");
    		}catch(ParameterFileWrongInputException pfwie){
    			pfwie.stopBremo();

    		}
    	}
		 
		return pZyl;

	}

	/**
	 * Diese Methode fuert einen Nulllinienabgleich nach der referenzWertMethode durch
	 * @return pZyl
	 */
	private double[] referenzWertMethode(double [] pZyl) {
//		try{
//			throw new BirdBrainedProgrammerException(
//					"Diese Methode wurde leider noch nicht implementiert --> NICHT aufregen! \n" +
//					" Selber Hand anlegen!! \n " +
//					"Gruß Juwe");
//		}catch(BirdBrainedProgrammerException bbp){
//			bbp.stopBremo();
//		}
//		return null;
//	}

		if(motor.isHubKolbenMotor()){
			double deltat=zeitAchse[0]-zeitAchse[1];
			double dab=CP.get_DruckabgleichBeginn();
			double dae=CP.get_DruckabgleichEnde();
			double abgleichDauer=dae-dab;
			double schrittZahl=(-abgleichDauer)/deltat;
			if (schrittZahl>1){
			double pZyl_temp []=new double [(int)Math.round(schrittZahl)];
			double pRef_temp =1e5*CP.get_Referenzwert(); //Referenzwert einlesen und von [bar] nach [Pa] konvertieren
			for(int i=0;i<=schrittZahl;i++){
				double time=dab-(i*deltat);			
				pZyl_temp[i]=L_Interp.linInterPol(time, zeitAchse, pZyl);
			}
			pOffset=MatLibBase.mw_aus_1DArray(pZyl_temp)-pRef_temp;

			pZyl = this.shiftMe(pZyl, pOffset);
			}
			else {
				try{
					throw new ParameterFileWrongInputException(
							"Beginn und Ende des Druckabgleiches dürfen nicht identisch sein. Der Beginn muss vor dem Ende liegen.");
				}catch(ParameterFileWrongInputException pfwie){
					pfwie.stopBremo();

				}
			}
		}else{
			try{
				throw new ParameterFileWrongInputException(
						"Die Zylinderdruckkorrektur mit der Methode \"referenzWert\" ist nur mit Motoren" +
				"vom Typ HubKolbenMotor moeglich!");
			}catch(ParameterFileWrongInputException pfwie){
				pfwie.stopBremo();

			}
		}
		return pZyl;
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
	
	public double get_pOffset(){
		return pOffset;
	}

}
