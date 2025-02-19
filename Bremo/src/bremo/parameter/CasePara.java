package bremo.parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import misc.PhysKonst;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.SpeziesFabrik;
import kalorik.spezies.Spezies;
import io.InputFileReader;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.BerechnungsModellFabrik;
import berechnungsModule.Berechnung.CanteraCaller;
import berechnungsModule.gemischbildung.Kraftstoff_Eigenschaften;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.internesRestgas.InternesRestgas;
import berechnungsModule.internesRestgas.InternesRestgasFabrik;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.MotorFabrik;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechnerFabrik;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import berechnungsModule.wandwaerme.WandWaermeUebergangFabrik;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;




public class CasePara {	

	private final Hashtable<String,String[]> INPUTFILE_PARAMETER;
	public final Hashtable<String,String> MODUL_VORGABEN;	
	private  Vector<ErgebnisBuffer> alleErgBuffers=new Vector<ErgebnisBuffer>();
	private double aktuelle_RZ;	//Aktuelle Rechenzeit
	//	public final BerechnungsModule MODULE;
	public final SysPara SYS; //SystemParameter	
	private final String WD; //WorkingDirectory
	/**
	 * Name des Inputfiles OHNE Dateiendung
	 */
	private final String CASE_NAME;		

	public final Motor MOTOR;
	public final GleichGewichtsRechner OHC_SOLVER;
	public final WandWaermeUebergang WAND_WAERME;
	public final WandWaermeUebergang WAND_WAERME_LW;
	public final InternesRestgas RESTGASMODELL;
	public final BerechnungsModell BERECHNUNGS_MODELL;
	public final SpeziesFabrik SPEZIES_FABRIK;
	public final MasterEinspritzung MASTER_EINSPRITZUNG;
	protected final boolean callsCantera;

	public CasePara(File inputFile) throws ParameterFileWrongInputException {			
		InputFileReader	ifr =new InputFileReader(inputFile);
		INPUTFILE_PARAMETER=ifr.get_eingabeParameter();

		MODUL_VORGABEN=ifr.get_berechnungsModule();

		//		LittleHelpers.print_Hash(INPUTFILE_PARAMETER);
		System.out.println("Inputfile wurde eingelesen!");
		//WD wird in SYS verwendet, muss also vorher mit einem Wert belegt sein
		WD=inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().indexOf(inputFile.getName()));
		CASE_NAME=inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".")); 
//		//Is doof aber geht jetzt nicht besser
		callsCantera=BerechnungsModellFabrik.callsCantera(this);	
		
		SYS=new SysPara(INPUTFILE_PARAMETER);	
		
		//SpeziesFabrik	
		SPEZIES_FABRIK=new SpeziesFabrik(this, new MakeMeUnique());	 
		//MakeMeUnique sorgt daf�r, dass es nur eine Instanz der Speziesfabrik geben kann. 
		//Ansonsten wuerde es Probleme mit der INtegration der einzelnen Speziesmassen geben. 
		//CO2 ist zwar CO2 aber bei verschiedenen Objekt IDs weiss Bremo das nicht!			

		//Motor
		MotorFabrik mf=new MotorFabrik(this);
		MOTOR=mf.MOTOR;

		//ohc-Solver
		GleichGewichtsRechnerFabrik ohcf=new GleichGewichtsRechnerFabrik(this);
		OHC_SOLVER=ohcf.OHC_SOLVER;
		
		//MasterEinspritzung
		MASTER_EINSPRITZUNG=new MasterEinspritzung(this);
				
		//Restgas benoetigt MASTER_EINSPRITZUNG
		InternesRestgasFabrik irgf=new InternesRestgasFabrik(this);
		RESTGASMODELL=irgf.RESTGAS_MODELL;
		
		//Wandwaerme
		WandWaermeUebergangFabrik wwf=new WandWaermeUebergangFabrik(this);
		WAND_WAERME=wwf.WAND_WAERME_MODUL;
		//Die LWA soll mit einem anderen WW-Modell durchgefuehrt werden koennen
		WAND_WAERME_LW=wwf.WAND_WAERME_MODUL_LW; 
		
		//Berechnungsmodell benoetigt MASTER_EINSPRITZUNG und RESTGAS_MODELL
		BerechnungsModellFabrik bmf=new BerechnungsModellFabrik(this);
		BERECHNUNGS_MODELL=bmf.BERECHNUNGS_MODELL;	

	}		

	/**
	 * Dirty Trick damit die Klasse SpeziesFabrik nur ein einziges Mal erzeugt werden kann. 
	 * @author eichmeier
	 *
	 */
	public class MakeMeUnique{
		private MakeMeUnique (){

		}
	}

	/**
	 * F�r einen BerechnungsCase sind alle ErgebnisBuffer in einem Vector gespeichert. Hiermit wird dem 
	 * Vector der ErgebnisBuffer ergB hinzugefuegt und kann damit spaeter in ein txt-file geschreiben werden.
	 * Der checkIn erfolgt automatisch bei der Erzeugung eines {@link ErgebnisBuffer}
	 * @param ergB
	 */
	public void ergBufferCheckIN(ErgebnisBuffer ergB){
		if(!alleErgBuffers.contains(ergB))//nur wenn der ErgebnisBuffer noch nich tim Vector steht wird er eingecheckt
			this.alleErgBuffers.add(ergB);		
	}

	/**
	 * Diese Funktion ermoeglicht das einfache schreiben aller ErgebnisBuffer, die 
	 * zuvor  mittels {@link ergBufferCheckIN} eingecheckt wurden.
	 * @param name
	 */
	public  void schreibeAlleErgebnisFiles(String name){		
		Iterator<ErgebnisBuffer> itr = alleErgBuffers.iterator();		
		while(itr.hasNext())
			itr.next().schreibeErgebnisFile(name);	
	}


	public String get_workingDirectory(){
		return WD;
	}

	public String get_CaseName(){
		return CASE_NAME;
	}
	public boolean callsCantera(){
		return this.callsCantera;
	}
	
	public boolean compareToExp(){		
		boolean compareToExp = false;
		String s = null;
		String s2 []= {"yes","no"};
		try {
			s=this.set_StringPara(INPUTFILE_PARAMETER, "compareToExp",s2);
		} catch (ParameterFileWrongInputException e){	
			compareToExp=false;
			s="no";
		}
		if(s.equalsIgnoreCase("yes"))
			compareToExp=true;		
		return compareToExp;		
	}
	
	public boolean filterPressureData(){
		String [] yesno={"ja","nein"};
		boolean filter=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"filtern",yesno ).equalsIgnoreCase("ja"))
				filter=true;
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
		}
		return filter;		
	}
	
	
	public int get_savitzkyGolayOrder(){
		double tmpSgolayOrder=5;
		try{				
			tmpSgolayOrder=set_doublePara(INPUTFILE_PARAMETER,"sgolayOrder","[-]", 2,12);
		}catch(ParameterFileWrongInputException e){
			e.log_Warning();
			e.log_Warning("The parameter \"sgolayOrder\" for the SavitzkyGolay-Filter has not been defined or was defined in a wrong way! \n" +
					"The filtering will be performed with a polynomial of order 5.\n");
		}
		return (int)tmpSgolayOrder;		
	}	
	
	public int get_savitzkyGolayHalfWidth(){			
		double tmpSgolayHalfWidth=2*get_savitzkyGolayOrder()+1;
		try{				
			tmpSgolayHalfWidth=set_doublePara(INPUTFILE_PARAMETER,"sgolayHalfWidth","[-]", tmpSgolayHalfWidth,50);
		}catch(ParameterFileWrongInputException e){	
			e.log_Warning();
			e.log_Warning("The parameter \"sgolayHalfWidth\" for the SavitzkyGolay-Filter has not been defined or was defined in a wrong way! \n" +
					"The filtering will be performed with a half-witdh of "+tmpSgolayHalfWidth);
		}
		return (int)tmpSgolayHalfWidth;
	}
	
	public boolean shift_pInlet(){
		String [] yesno={"ja","nein"};
		boolean shift_pIn=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"shift_pEin",yesno ).equalsIgnoreCase("ja"))
				shift_pIn=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return shift_pIn;
	}
	
	public boolean shift_pOutlet(){
		String [] yesno={"ja","nein"};
		boolean shift_pOut=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"shift_pAus",yesno ).equalsIgnoreCase("ja"))
				shift_pOut=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return shift_pOut;
	}

	public String get_pressureAdjustmentMethod(String methodIdentifier){
		String method=null;
		try {		
			String []s1 ={"polytropenMethode", "referenzWert","abgleichSaugrohr","abgleichKruemmer","ohne"};			
			method=set_StringPara(this.INPUTFILE_PARAMETER,methodIdentifier,s1);
		}catch(ParameterFileWrongInputException e){
			e.stopBremo();
		}
		return method;
	}

	public int get_ColumnToRead(String columnIdentifier){		
		int column;
		try {
			column = (int)set_doublePara(INPUTFILE_PARAMETER, columnIdentifier,"",1,40);
			return column;
		} catch (ParameterFileWrongInputException e) {			
			e.printStackTrace();
			e.stopBremo();
			return 0;
		}		
	}
	
	public double get_relaxFactor(){
		double tmpRELAX;
		try{				
			tmpRELAX=set_doublePara(INPUTFILE_PARAMETER, "relaxFaktor","[-]",0.1,1);	
		}catch(ParameterFileWrongInputException e){
			e.log_Warning("The parameter \"relaxFactor\" has not been defined!"+
					" The run will be performed with 0.7! \n");
			tmpRELAX=0.7;
		}
		return tmpRELAX;	
	}
	
	public double get_precissionPressureTraceAnalysis(){
		//Einlesen von PUBLIC FINAL doubleDaten
		double precission=0.0005*1e5;
		try {
			precission=1e5*set_doublePara(INPUTFILE_PARAMETER, "rechenGenauigkeit_DVA","[bar]",0,0.1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
		}	
		return precission;
	}
	

	public File get_FileToRead(String fileIdentifier){		
		String htfFileName;
		try {
			htfFileName = set_FileName(this.INPUTFILE_PARAMETER,fileIdentifier);
			File file=new File(WD+htfFileName);
			if (!file.isFile())
				try{ throw new ParameterFileWrongInputException(
						"The specified path does not point to a file \n"
								+ (WD+htfFileName));
				}catch(ParameterFileWrongInputException e){
					e.stopBremo();				
				}
			return file;
		} catch (ParameterFileWrongInputException e) {			
			e.printStackTrace();
			e.stopBremo();
			return null;
		}		
	}
	
	public String get_FileNameToRead(String fileIdentifier){
		String fileName=null;
		try {
			fileName = set_FileName(this.INPUTFILE_PARAMETER,fileIdentifier);
		}catch(ParameterFileWrongInputException e){
			e.stopBremo();
		}
		return fileName;
	}
	
	

	/**
	 * Mit dieser Methode wird die aktuelle Rechenzeit in CasePara gespeichert.
	 * Diese Zeit entspricht einem ganzen Rechenzeitschritt und nicht einem vom
	 * RungeKutta-Verfahren verwendeten Zwischenschritt.
	 * @param time in [s]
	 */
	public void set_aktuelle_Rechenzeit(double time){
		aktuelle_RZ=time;
	}
	/**
	 * Mit dieser Methode wird die aktuelle Rechenzeit zur�ckgegeben.
	 * Diese Zeit entspricht dem letzten ganzen Rechenzeitschritt und
	 * nicht einem vom RungeKutta-Verfahren verwendeten Zwischenschritt.
	 * @param time in [s]
	 */
	public double get_aktuelle_Rechenzeit(){
		return aktuelle_RZ;
	}
	/**
	 * Mit dieser Methode wird die n�chste Rechenzeit zur�ckgegeben.
	 * Diese Zeit entspricht dem n�chsten ganzen Rechenzeitschritt und
	 * nicht einem vom RungeKutta-Verfahren verwendeten Zwischenschritt.
	 */
	public double get_naechste_Rechenzeit(){
		return aktuelle_RZ+this.SYS.WRITE_INTERVAL_SEC;
	}	
	

	/**
	 * Gibt an ab welchem Zeitpunkt die Verbrennung beginnen soll. Ab diesem Zeitpunkt wird 
	 * die freigesetzte W�rme in einen Massenumsatz umgerechnet.
	 * @return
	 */
	public double get_verbrennungsBeginnSEC(){
		double verbrennungsBeginn;	

		if(is_VerbrennungAutoDetect()){
			try{
				throw new BirdBrainedProgrammerException("Im InputFile wurde \"VerbrennungsBeginnAutoDetect\" auf \"ja\" " +
						" gesetzt und diese Methode wurde trotzdem aufgerufen. VollDeppProgrammierer!" +
						" \n Diese Methode darf NIE wirklich NIE direkt aufgerufen werden, " +
						"sondern muss immer zusammen mit der Abfrage: \n " +
						"\"isAutoDetect==false\" \n verwendet werden" );
			}catch(BirdBrainedProgrammerException bbp){
				bbp.stopBremo();
			}			
		}		

		try {
			if(SYS.IS_KW_BASIERT){				
				verbrennungsBeginn =set_doublePara(
						INPUTFILE_PARAMETER, "verbrennungsBeginn","[KWnZOT]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				verbrennungsBeginn=convert_KW2SEC(verbrennungsBeginn);
			}else {				
				verbrennungsBeginn =set_doublePara(INPUTFILE_PARAMETER, "verbrennungsBeginn","[s]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return verbrennungsBeginn;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		

	}

	/**
	 * Gibt an ob der Zeitpunkt ab dem die Verbrennungbeginnt automatisch ermittelt 
	 * werden soll oder ob eine Vorgabe vom User erfolgt
	 * 
	 * @return
	 */
	public boolean is_VerbrennungAutoDetect(){			

		boolean autodetect = false;
		String s = null;
		String s2 []= {"ja","nein"};
		try {
			s=this.set_StringPara(INPUTFILE_PARAMETER, "VerbrennungsBeginnAutoDetect",s2);
		} catch (ParameterFileWrongInputException e) {		
			e.stopBremo();
		}

		if(s.equalsIgnoreCase("ja")) autodetect=true;

		//		if(autodetect){
		//			try{
		//				throw new BirdBrainedProgrammerException("Die AutodetectMethode wurde noch nicht Programmiert");
		//			}catch(BirdBrainedProgrammerException bbp){
		//				bbp.stopBremo();
		//			}
		//		}		

		return autodetect;		
}
	/**
	* Gibt an ob eine Verlustteilung durchgef�hrt werden soll.
	* Die Auswahl erfolgt durch den Bnutzer
	*
	* @return
	*/
	public boolean is_Verlustteilung(){
	boolean verlustteilung = false;
	String s = null;
	String s2 []= {"ja","nein"};
	try {
	s=this.set_StringPara(INPUTFILE_PARAMETER, "Verlustteilung",s2);
	} catch (ParameterFileWrongInputException e) {
	e.stopBremo();
	}
	if(s.equalsIgnoreCase("ja")) verlustteilung=true;
	return verlustteilung;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
					private void xxx_AB_HIER_ALLGEMEINE_BETRIEBS_PARAMETER(){}	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////


	/** 
	 * @return initial mole fraction of CO2
	 */	
	public double get_iniMoleFrac_CO2(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_CO2","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return initial mole fraction of O2
	 */	
	public double get_iniMoleFrac_O2(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_O2","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	
	
	/** 
	 * @return initial mole fraction of H2O
	 */	
	public double get_iniMoleFrac_H2O(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_H2O","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	
	
	/** 
	 * @return initial mole fraction of N2
	 */	
	public double get_iniMoleFrac_N2(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_N2","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return initial mole fraction of iC8H18
	 */	
	public double get_iniMoleFrac_iC8H18(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_iC8H18","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return initial mole fraction of nC7H16
	 */	
	public double get_iniMoleFrac_nC7H16(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_nC7H16","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	
	/** 
	 * @return initial pressure [Pa]
	 */	
	public double get_p_ini(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "p_Ini","[Pa]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	
	/** 
	 * @return initial temperature in [K]
	 */	
	public double get_T_ini(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "T_Ini","[K]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	public double get_C_MixRad() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, "C_MixRad","[-]",0.001,10); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	
	public double get_C_Mix() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, "C_Mix","[-]",0,500); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	
	public int get_mixingProcess() {
		int a=-1;
		try {			
			
			a=(int)set_doublePara(INPUTFILE_PARAMETER, "mixingProcess","[-]",0,6); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return a;
	}



	/** 
	 * @return liefert die Drehzahl in [U/sec]
	 */	
	public double get_DrehzahlInUproSec(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Drehzahl","[min^-1]",0,Double.POSITIVE_INFINITY)/60;
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}		


	/**
	 * Berechnet die Zusammensetzung des Abgases auf Basis des chem. Gleichgewichts
	 * @return abgas vom Typ Spezies Objekt  
	 */
	public Spezies get_spezAbgas(){
		CasePara cp=this; //wenn die funktion mal wo anders stehen soll.....
		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		MasterEinspritzung me=this.MASTER_EINSPRITZUNG;//zirkul�re Referenz ;-( naja mal schauen...
		double mKrst=me.get_mKrst_Sum_ASP();
		Spezies krst=me.get_spezKrstALL(); //liefert die Mischung aller Krafstoffe		
		double mFG=mLuft_tr+mW+mKrst;	//gesamte Masse des Verbrennenden Gases
		Hashtable<Spezies,Double>frischGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
		//trockene Luft
		frischGemisch_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezLuft_trocken(),mLuft_tr/mFG);
		//Wasser
		frischGemisch_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezH2O(),mW/mFG);
		//Kraftstoff
		frischGemisch_MassenBruchHash.put(krst,mKrst/mFG);

		//Erstellen der Frischgemischspezies
		GasGemisch frischGemsich=new GasGemisch("Frischgemisch");
		frischGemsich.set_Gasmischung_massenBruch(frischGemisch_MassenBruchHash);


		//Bestimmen der AGR-Zusammensetzung --> das Hinzufuegen von AGR hat keinen Einfluss auf die 
		//AGR-Zusammensetzung da die Anzahl der c/h/o-Atome gleich bleibt und nur vom Frischgemisch abhaengt
		GleichGewichtsRechner gg=this.OHC_SOLVER; //zirkul�re Referenz ;-( naja mal schauen...
		GasGemisch abgas=new GasGemisch("Abgas");
		//eigentlich muesste die GLGW-Zusammensetzung aus der DVA kommen der Druck wuerde auf den Wert festgelegt, 
		//den er hat wenn 1600K in der verbrannten Zone vorliegen
		//-->die Druckabh�ngigikeit bei niedrigen Temperaturen ist aber so gering, dass der Druck hier keine Rolle spielt!
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(1e5, 1000, frischGemsich)); //T=1000K<T_Freeze
		return abgas;
	}




	/*** 
	 * @return liefert die Frischladung bei Einlassschluss(AGR extern +AGR intern + trockene Luft + Wasser)  
	 * als Speziesobjekt 
	 */		
	public Spezies get_spezVerbrennungsLuft(){

		CasePara cp=this; //wenn die funktion mal wo anders stehen soll.....
		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		Spezies abgas=this.get_spezAbgas();
		//Bestimmung der Verbrennungsluftzusammensetzung		
		double mAGRex=cp.get_mAGR_extern_ASP();
		double mAGRin=this.RESTGASMODELL.get_mInternesRestgas_ASP();//zirkul�re Referenz ;-( naja mal schauen...
		double mAGR=mAGRex+mAGRin;
		double mGes=mLuft_tr+mW+mAGR; //gesamte Masse im Zylinder (ohne Kraftstoff)
		Hashtable<Spezies,Double>verbrennungsLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		verbrennungsLuft_MassenBruchHash.put(abgas, mAGR/mGes);
		verbrennungsLuft_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezLuft_trocken(),mLuft_tr/mGes);		
		verbrennungsLuft_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezH2O(),mW/mGes);
		GasGemisch verbrennungsLuft=new GasGemisch("Verbrennungsluft");
		verbrennungsLuft.set_Gasmischung_massenBruch(verbrennungsLuft_MassenBruchHash);		

		return verbrennungsLuft;
	}


	/**
	 * Liefert die Masse der Verbrennungsluft bei Einlassschluss bestehend aus: </br>
	 * AGR extern +AGR intern + trockene Luft + Wasser
	 * @return
	 */
	public double get_mVerbrennungsLuft_ASP(){

		CasePara cp=this; //wenn die funktion mal wo anders stehen soll.....
		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		double mAGRex=cp.get_mAGR_extern_ASP();
		double mAGRin=this.RESTGASMODELL.get_mInternesRestgas_ASP();//zirkul�re Referenz ;-( naja mal schauen...
		double mAGR=mAGRex+mAGRin;		

		return mLuft_tr+mW+mAGR;
	}


	/**
	 * Liefert die interne AGR-Rate bei Einlassschluss bestehend aus: </br>
	 * AGR intern / (trockene Luft + Wasser); Kraftstoff wird dabei nicht </br>
	 * ber�cksichtigt.
	 * @return double AGR-Rate
	 */
	public double get_interne_AGR_Rate(){
		CasePara cp=this; //wenn die funktion mal wo anders stehen soll.....
		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		double mAGRin=this.RESTGASMODELL.get_mInternesRestgas_ASP();//zirkul�re Referenz ;-( naja mal schauen...	

		return mAGRin/(mLuft_tr+mW);  //TODO Kraftstoffmasse aus Einspritzung bei Einlassschluss beruecksichtigen
	}


	/*** 
	 * @return liefert  die externe AGR-Masse pro ASP in [kg]
	 */	
	public double get_mAGR_extern_ASP(){
		try {
			return this.convert_ProStunde_2_ProASP(
					set_doublePara(INPUTFILE_PARAMETER, "mAGR_extern","[kg/h]",0,Double.POSITIVE_INFINITY));
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/**Diese Methode wird benoetigt wenn ohne Modell fuer das interne Restgas gerechnet werden soll
	 * @return liefert die interne AGR-Masse pro ASP in [kg]
	 */	
	public double get_mAGR_intern_ASP_aus_InputFile(){
		//TODO Wenn eine Ladungswechselanalyse durchgefuehrt werden soll muss hier die Abfrage erfolgen 
		// ob eine LWA vorliegt und ggf der mit der LWA berechnete Wert zur�ckgegeben werden! 
		//Eine Abfrage �ber das Restgasmodell mach eher weniger sinnn
		//Dies gilt f�r alle Eingabegr��en die von der LWA ueberschrieben werden muessen....
		//Alternativ denkbar ist auch die Uebergabe aller Werte aus der LWA direkt in die Berechnungsmodule
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "mAGR_intern_ASP","[kg]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/*** 
	 * @return liefert die feuchte Luftmasse pro Arbeitsspiel in [kg]
	 */	
	public double get_mLuft_feucht_ASP(){

		try {
			return this.convert_ProStunde_2_ProASP(
					set_doublePara(INPUTFILE_PARAMETER, "mLuft_feucht","[kg/h]",1e-6,Double.POSITIVE_INFINITY));
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/*** 
	 * @return liefert die trockene Luftmasse pro Arbeitsspiel in [kg]
	 */	
	public double get_mLuft_trocken_ASP(){

		return this.get_mLuft_feucht_ASP()-this.get_mWasser_Luft_ASP();			
	}

	/*** 
	 * @return liefert die Wassermasse in der Ansaugluft pro Arbeitsspiel in [kg]
	 */	
	public double get_mWasser_Luft_ASP(){
		double x=this.get_WasserbeladungLadeLuft();
		return this.get_mLuft_feucht_ASP()*x/(1+x);			
	}





	/*** 
	 * @return liefert die Wasserbeladung X=mWasser/mluftTrocken der Ansaugluft in [kg/kg]
	 */	
	public double get_WasserbeladungLadeLuft(){
		//Nach Baehr Thermodynamik Seite 289
		double pws=PhysKonst.get_saettigunsDampfdruck_H2O(this.get_T_FeuchteMessung());
		double phi=this.get_relativeLuftFeuchte();
		double p=this.get_p_FeuchteMessung();
		return PhysKonst.get_R_Luft()/PhysKonst.get_R_H2O()*(pws/(p/phi-pws));	
	}


	/*** 
	 * @return liefert die relative Luftfeuchte in [-]
	 */	
	public double get_relativeLuftFeuchte(){
		double phi=-1;
		try {
			phi= set_doublePara(INPUTFILE_PARAMETER, "relative_Luftfeuchte","[%]",0,100);

		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();			
		}	
		if(phi<=3){
			try {
				throw new ParameterFileWrongInputException("Ein relative Luftfeuchte von: "+
						phi+ "% ist ungweoehnlich!");

			} catch (ParameterFileWrongInputException e) {
				e.log_Warning();			
			}		
		}

		if(phi>100){
			try {
				throw new ParameterFileWrongInputException("Eine relative Luftfeuchte von: "+
						phi+ "% ist unmoeglich!");

			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();			
			}		
		}
		return phi/100;

	}

	/***
	 * @return liefert den trockenen globalen Lambda-Wert aus den angegebenen Luftstrom und Massenstr
	 */
	public double get_Lambda_trocken(){
		
		CasePara cp=this; //wenn die funktion mal wo anders stehen soll.....		
		MasterEinspritzung me=this.MASTER_EINSPRITZUNG;//zirkul�re Referenz ;-( naja mal schauen...
		double mKrst=me.get_mKrst_Sum_ASP();
		Spezies krst=me.get_spezKrstALL(); //liefert die Mischung aller Krafstoffe			
		double Lst=krst.get_Lst();	
		return this.get_mLuft_trocken_ASP()/mKrst/Lst;
	}



	/** 
	 * @return liefert den Z�ndzeitpunkt in [s] nach Rechnungsbeginn 
	 */	
	public double get_ZZP(){
		double zzp;		
		try {
			if(SYS.IS_KW_BASIERT){				
				zzp =set_doublePara(INPUTFILE_PARAMETER, "ZZP","[KWnZOT]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				zzp=convert_KW2SEC(zzp);
			}else {				
				zzp =set_doublePara(INPUTFILE_PARAMETER, "ZZP","[s]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return zzp;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_pmi(){
		double std_pmi=-7.77;	//Standardwert f�r pmi (muss negativ sein, siehe z.B. das Wandw�rmemodell "WoschniHuber"
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "pmi","[bar]",0,30); 
		} catch (ParameterFileWrongInputException e) {				
			e.stopBremo();
			return std_pmi;
		}
	}
	
	public double get_pme(){
		double std_pme=0;
		try {
		return set_doublePara(INPUTFILE_PARAMETER, "pme","[bar]",0,30);
		} catch (ParameterFileWrongInputException e) {
		e.log_Warning("Keiner oder der falsche pme-Wert wurde angegeben. "+
		"Dieser wird nicht in der Verlustteilung angezeigt");
		return std_pme;
		}
		}
	
	
	/**
	 * Mehrfacheinspritzungen sind m�glich --> �ber den Parameter  wird die Anzahl der Einspritzungen vorgegeben \br
	 * Wird dieser Parameter nicht angegeben, wird er auf eins gesetzt und eine Warnung wird ins LogFile geschrieben
	 * @param  
	 * @return liefert den Einspritzbeginn in [s] nach Rechnungsbeginn 
	 */	
	public int get_AnzahlEinspritzungen(){		
		try {
			return (int) set_doublePara(INPUTFILE_PARAMETER, "anzahlEinspritzungen","",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning();
			return -1;
		}		
	}
	
	/**
	 * returns the index of the injection that will be used to form the 
	 * zones for the RCCI multi Zone Combustion model
	 * @return
	 */
	public int get_nbrOfMainInj(){		
		try {
			return (int) set_doublePara(INPUTFILE_PARAMETER, "nbrOfMainInj","",1,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning();
			return -1;
		}		
	}

	/**
	 * Mehrfacheinspritzungen sind m�glich --> �ber den Parameter  wird die Nummer der Einspritzung vorgegeben
	 * @param  
	 * @return liefert den Einspritzbeginn in [s nach Rechnungsbeginn]
	 */	
	public double get_BOI(int einspritzungsNr){
		String  suchString;
		int anzEinspr=get_AnzahlEinspritzungen();
		if(einspritzungsNr>anzEinspr){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht den Einspritzbeginn der " +einspritzungsNr 
						+"ten Einspritzung abzufragen. " +
						"\n Laut InputFile gibt es aber nur " +anzEinspr + " Einspritzungen!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}

		if (anzEinspr==1){
			suchString="BOI";
		}else{
			suchString="BOI_"+einspritzungsNr;
		}
		try {
			if(SYS.IS_KW_BASIERT){	
				double boi;
				boi =set_doublePara(INPUTFILE_PARAMETER, suchString,"[KWnZOT]",SYS.KW_UNTERGRENZE,SYS.KW_OBERGRENZE);
				return convert_KW2SEC(boi);				
			}else {					
				return set_doublePara(INPUTFILE_PARAMETER, suchString,"[s]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
			}

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	

	/**
	 * Da Mehrfacheinspritzungen sind m�glich --> �ber den Parameter @param  
	 * wird die Nummer der Einspritzung vorgegeben
	 * @param int i 
	 * @return liefert das Einspritzende in [s] nach Rechnungsbeginn 
	 */	
	public double get_EOI(int i){
		//TODO Fehlerquelle Einspritzindex und Einspritzanzahl sind nicht gekoppelt
		String suchString;
		int anzEinspr=get_AnzahlEinspritzungen();
		if(i>anzEinspr){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht den Einspritzbeginn der " +i 
						+"ten Einspritzung abzufragen. " +
						"\n Laut InputFile gibt es aber nur " +anzEinspr + " Einspritzungen!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}		

		if (anzEinspr==1){
			suchString="EOI";
		}else{
			suchString="EOI_"+i;
		}
		try {
			if(SYS.IS_KW_BASIERT){			
				double eoi;
				eoi =set_doublePara(INPUTFILE_PARAMETER, suchString,"[KWnZOT]",convert_SEC2KW(get_BOI(i)),get_Auslassoeffnet_KW());
				return convert_KW2SEC(eoi);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, suchString,"[s]",get_BOI(i),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/**
	 * Liefert die ID der Zone in die die Einspritzung mit der  Nummer einspritzungsNr eingespritzt 
	 * @param  
	 * @return zonenID
	 */	
	public int get_InjZone(int einspritzungsNr){
		String  suchString;
		int anzEinspr=get_AnzahlEinspritzungen();
		int injZone=-1;

		if (anzEinspr==1){
			suchString="InjZone";
		}else{
			suchString="InjZone_"+einspritzungsNr;
		}

		if(einspritzungsNr>anzEinspr){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht den Parameter " + suchString + 
						" der " +einspritzungsNr 
						+"ten Einspritzung abzufragen. " +
						"\n Laut InputFile gibt es aber nur " +anzEinspr + " Einspritzungen!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}
		try {	

			injZone =(int)(set_doublePara(INPUTFILE_PARAMETER, suchString,"[-]",
					0,Integer.MAX_VALUE));			
			return injZone;
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return -1;
		}		
	}

	public Spezies get_kraftsoff(int einspritzungsNr){
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String krstFlag;
		if(anzEinspr==1){
			krstFlag="krst";	
		}else{
			krstFlag="krst_"+einspritzungsNr;
		}		

		//Die Koeffizienten Speziesfabrik liefert eine hashtable mit allen moeglichen Krafstoffen
		Hashtable<String,Spezies> krstHash=this.SPEZIES_FABRIK.get_alleKrafstoffe();
		String moeglicheFlags []=new String[krstHash.size()+2];
		//Die Kraftsoffapproximationsspezies ist nicht in der Hashtable enthalten
		moeglicheFlags[0]="ohc";  //jetzt schon ;-)
		//Wasser kann man ja auch einspritzen
		moeglicheFlags[1]="Wasser";  //jetzt schon ;-)

		int iter=2;
		Enumeration<String> e=krstHash.keys();		
		while(e.hasMoreElements()){
			moeglicheFlags[iter]=e.nextElement();
			iter++;
		}		


		String krstAngabe=null;
		try{
			krstAngabe=set_StringPara(INPUTFILE_PARAMETER,krstFlag,moeglicheFlags);
		} catch (ParameterFileWrongInputException ex) {				
			ex.stopBremo();
		}



		if (krstAngabe.equals(moeglicheFlags[0])){
			//TODO Programmieren der Eingabe fuer die Krst-ApproxSpezies nach Grill
			try{
				throw new BirdBrainedProgrammerException("Die Eingabe fuer die Rechnung mit der Approximation " +
						"der Krafstoffkalorik nach Grill wurde noch nicht implementiert." +
						"Es wird wohl noetig sein, einen der vordefinierten Krafstoffe zu nehmen, " +
						"oder selber Hand ans Programm zu legen! Gru� Juwe");
			}catch(BirdBrainedProgrammerException ex){
				ex.stopBremo();
			}
			return null;
		}else 
			return krstHash.get(krstAngabe); //die Fehlerabfrage erfolgt bereits innerhalb der Methode 
		//"set_StringPara" da der hier uebergebene Vektor alle moeglichen krstAngaben enthaelt.

	}	

	/**
	 * returns the mass of fuel in [kg/cycle] for the injection 
	 * injection with the index: einspritzungsNr
	 * @param einspritzungsNr
	 * @return
	 */
	public double get_mKrst_ASP(int einspritzungsNr){
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String krstFlag;
		if(anzEinspr==1){
			krstFlag="mKrst";	
		}else{
			krstFlag="mKrst_"+einspritzungsNr;
		}		

		try {
			return this.convert_ProStunde_2_ProASP(
					set_doublePara(INPUTFILE_PARAMETER, krstFlag,"[kg/h]",0,Double.POSITIVE_INFINITY));
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/**
	 * Liefert die Anzahl axialer Pakete bei einem Einspritzmodell mit Paketansatz 
	 * (z.B. Hiroyasu)
	 * @param einspritzungsNr
	 * @return
	 */
	public int get_anzAxialPakete(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="anzAxialPakete";	
		}else{
			flag="anzAxialPakete_"+einspritzungsNr;
		}		

		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	/**
	 * Liefert die Anzahl radialer Pakete bei einem Einspritzmodell mit Paketansatz 
	 * (z.B. Hiroyasu)
	 * @param einspritzungsNr
	 * @return
	 */
	public int get_anzRadialPakete(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="anzRadialPakete";	
		}else{
			flag="anzRadialPakete_"+einspritzungsNr;
		}		

		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	/**
	 * Liefert die Anzahl radialer Pakete bei einem Einspritzmodell mit Paketansatz 
	 * (z.B. Hiroyasu)
	 * @param einspritzungsNr
	 * @return
	 */
	public int get_einspritzDruck(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="einspritzDruck";	
		}else{
			flag="einspritzDruck_"+einspritzungsNr;
		}			
		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"[Pa]",0,Double.MAX_VALUE);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}	



	public double get_anzSPL(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="anzSpritzloecher";	
		}else{
			flag="anzSpritzloecher_"+einspritzungsNr;
		}			
		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1,Double.MAX_VALUE);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}


	public double get_CdFaktor(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="cdFaktor";	
		}else{
			flag="cdFaktor_"+einspritzungsNr;
		}			
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",0.1,1);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	public double get_EntrainmentFaktor(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="EntrainmentFaktor";	
		}else{
			flag="EntrainmentFaktor_"+einspritzungsNr;
		}			
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1e-9,100); //beliebig gewaehlt
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	public double get_ProfilFaktor(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="ProfilFaktor";	
		}else{
			flag="ProfilFaktor_"+einspritzungsNr;
		}			
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1e-9,100); //beliebig gewaehlt
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	public double get_durchmSPL(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="durchmSpritzloch";	
		}else{
			flag="durchmSpritzloch_"+einspritzungsNr;
		}			
		try {
			return 	set_doublePara(INPUTFILE_PARAMETER, flag,"[m]",0.00001,0.001);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}



	public String get_vergleichsKrstVerdampfung(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		String flag;
		if(anzEinspr==1){
			flag="vergleichsKrstVerdampfung";	
		}else{
			flag="vergleichsKrstVerdampfung_"+einspritzungsNr;
		}			
		try {
			return this.set_StringPara(INPUTFILE_PARAMETER, flag, Kraftstoff_Eigenschaften.kraftstoffe);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return null;
		}			

	}
	
	
	
	/**
	 * Liefert die gesamte pro ASP eingespritzte Kraftsoffmasse
	 * @return
	 */
	public double get_mKrst_ASP(){
		if(this.get_AnzahlEinspritzungen()==1)
			return get_mKrst_ASP(1);
		else{
			double sumKrstASP=0;
			for(int i=0;i<get_AnzahlEinspritzungen();i++){
				//Bie mehreren Einspritzungen laeuft die Nummerierung im Inputfile ab 1
				sumKrstASP+=get_mKrst_ASP(i+1); 
			}
			return sumKrstASP;
		}			
	}
	
	
	/** 
	 * @return liefert die Drallgeschwindigkeit  in [m/s]
	 */	
	public double get_DrallGeschwindigkeit(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "drallgeschwindigkeit","[m/s]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/** 
	 * @return liefert den Heizwert fuer RON91 [J/mol]
	 */	
	public double get_Hu_RON_91(){
		double stdHu=4581231.2590484;//4.5951702590484*1e6; //berechnet passend zu den Koeffizienten

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, "Hu_RON_91","[MJ/mol]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \"RON_91\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu + "[J/mol]");
			return stdHu;
		}		
	}



	/** 
	 * @return liefert den Heizwert fuer RON95 [J/mol]
	 */	
	public double get_Hu_RON_95(){
		double stdHu=4211277.132508449; //4.247052132508449*1e6; //berechnet  passend zu den Koeffizienten

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, "Hu_RON_95","[MJ/mol]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \"RON_95\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu + "[J/mol]");
			return stdHu;
		}		
	}

	/** 
	 * @return liefert den Heizwert fuer RON98 [J/mol]
	 */
	public double get_Hu_RON_98(){
		double stdHu=4108620.6027614996;//4.1520586027615*1e6; //gerechnet passend zu den Koeffizienten		

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, "Hu_RON_98","[MJ/mol]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \"RON_98\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu +  "[J/mol]");			
			return stdHu;
		}		
	}

	/** 
	 * @return liefert den Heizwert fuer Diesel [J/mol]
	 */	
	public double get_Hu_Diesel(){
		double stdHu=7849471.20749425;//7.803475207494249*1e6; //berechnet passend zu den Koeffizienten

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, "Hu_Diesel","[MJ/mol]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \"Diesel\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu + "[J/mol]");

			return stdHu;
		}		
	}



	/** 
	 * @return liefert den Heizwert fuer eine Krstoffapproximationsspezies [J/mol]
	 */	
	public double get_Hu_ohc(){

		double stdHu=Double.NaN; //TODO Heizwertangeben
		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, "Hu_ohc","[MJ/mol]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return stdHu;
		}		
	}
	/**
	 * When Cantera is used this method returns
	 * a factor to adopt the LHV of model fuel(s)
	 * to the ones of the the experimental fuel(s)
	 * @return
	 */
	public double get_lhvCorr(){
		double lhvCorr=1;
		try {
			lhvCorr= set_doublePara(INPUTFILE_PARAMETER, "lhvCorr","[-]",0,1); 
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();	
		}					
		return lhvCorr;
	}

	/**
	 * Returns a factor to multiply the wall heat transfer 
	 * density with and thus the actual wall heat transfer 
	 * @return
	 */
	public double get_whtfMult(){
		double whtfCorr=1;
		try {
			whtfCorr= set_doublePara(INPUTFILE_PARAMETER, "whtfMult","[-]",0,Double.MAX_VALUE); 
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();	
		}					
		return whtfCorr;
	}


	/** 
	 * @return liefert einen Vektor mit den o/h/c-Atomen fuer eine Krstoffapproximationsspezies 
	 */	
	public double [] get_krstApprox_ohc(){
		double ohc[]=new double [3];
		try {			
			ohc[0]= 1e6*set_doublePara(INPUTFILE_PARAMETER, "krst_oAtome","[-]",0,Double.POSITIVE_INFINITY); 
			ohc[1]= 1e6*set_doublePara(INPUTFILE_PARAMETER, "krst_hAtome","[-]",0,Double.POSITIVE_INFINITY);
			ohc[2]= 1e6*set_doublePara(INPUTFILE_PARAMETER, "krst_cAtome","[-]",0,Double.POSITIVE_INFINITY);
			return ohc;
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			ohc[0]=ohc[1]=ohc[2]=Double.NaN;
			return ohc;
		}		
	}




	/**
	 * @return liefert den Zeitpunkt, bei dem der Zylinderdruckabgleich beginnen soll [s n. Rechenbegin]
	 */
	public double get_DruckabgleichBeginn(){
		double KW_beginnAbgleich;		
		try {
			if(SYS.IS_KW_BASIERT){				
				KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Beginn_Druckabgleich","[KWnZOT]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				KW_beginnAbgleich=convert_KW2SEC(KW_beginnAbgleich);
			}else {				
				KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Beginn_Druckabgleich","[s]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return KW_beginnAbgleich;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}	
	}
	/**
	 * @return liefert den Zeitpunkt, bei dem der Zylinderdruckabgleich aufh�ren soll [s n. Rechenbeginn]
	 */
	public double get_DruckabgleichEnde(){
		double KW_endeAbgleich;		
		try {
			if(SYS.IS_KW_BASIERT){				
				KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Ende_Druckabgleich","[KWnZOT]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				KW_endeAbgleich=convert_KW2SEC(KW_endeAbgleich);
			}else {				
				KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Ende_Druckabgleich","[s]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return KW_endeAbgleich;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}
	/**
	 * @return liefert den Kappa Wert, mit dem der Zylinderdruckabgleich gemacht wird, falls die Polytropenmethode verwendet wird
	 */
	public double get_Kappa_Druckabgleich(){
		double kappa_Druckabgleich;		
		try {
			kappa_Druckabgleich =set_doublePara(INPUTFILE_PARAMETER, "kappa_Druckabgleich","[-]",1,1.4);
			return kappa_Druckabgleich;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}	

	/**
	 * Liefert den Volumenbruch der HC-Emissionen
	 * @return HC oder 0 wenn im inputFile nicht angegeben
	 */
	public double get_HC() {
		double hc;		
		try {
			hc =set_doublePara(INPUTFILE_PARAMETER, "HC","[ppm]",0,Double.MAX_VALUE);
			return hc*1e-6;				
		} catch (ParameterFileWrongInputException e) {			
			return 0;
		}
	}


	/**
	 * Liefert den Volumenbruch der CO-Emissionen
	 * @return CO oder 0 wenn im inputFile nicht angegeben
	 */
	public double get_CO() {
		double co;		
		try {
			co =set_doublePara(INPUTFILE_PARAMETER, "CO","[ppm]",0,Double.MAX_VALUE);
			return co*1e-6;				
		} catch (ParameterFileWrongInputException e) {			
			return 0;
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void xxx_AB_HIER_TEMPERATUREN(){}
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/** 
	 * @return Gibt die Temperatur im Zylinder bei Einlassschluss in [K] zur�ck. 
	 * Wird diese Temperatur nicht vorgegeben so wird automatisch T_Ladeluft zur�ck gegeben
	 */
	public double get_T_EinlassSchluss(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "T_Einlassschluss","[K]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"T_Einlassschluss\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"T_Ladeluft\" gerechnet!");
			return get_T_LadeLuft();
		}		
	}


	/** 
	 * @return Gibt die Temperatur der Ladeluft in [K] zur�ck
	 */
	public double get_T_LadeLuft(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, "T_Ladeluft","[K]",250,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Temperatur der Ladeluft in [K] zur�ck
	 */
	public double get_T_Abgas(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, "T_Abgas","[K]",400,2000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_T_FeuchteMessung(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, "T_FeuchteMessung","[K]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return Gibt die Temperatur bei Einlassschluss in [K] zur�ck
	 */
	public double get_T_Wand(){	
		double T_Wand=-1e100;
		if(T_Wand==-1e100){
			try {
				return set_doublePara(INPUTFILE_PARAMETER, "T_Wand","[K]",1e-6,1000);//da macht wohl jeder Stahl schlapp
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
				return Double.NaN;
			}
		}else{
			return T_Wand;
		}
	}

	/**
	 * Liefert die Temperatur des FL�SSIGEN Kraftstoffs	
	 * @return
	 */
	public double get_T_Krst_fl(int i) {
		String suchString;
		int anzEinspr=get_AnzahlEinspritzungen();
		if(i>anzEinspr){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht die Kraftstofftemperatur der " +i 
						+"ten Einspritzung abzufragen. " +
						"\n Laut InputFile gibt es aber nur " +anzEinspr + " Einspritzungen!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}		

		if (anzEinspr==1){
			suchString="T_Krst_fluessig";
		}else{
			suchString="T_Krst_fluessig_"+i;
		}
		try {
			return set_doublePara(INPUTFILE_PARAMETER, suchString,"[K]",-273.15,Double.POSITIVE_INFINITY);			

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}




	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void xxx_AB_HIER_DRUECKE(){}
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/*** 
	 * @return liefert den Druck vor den Einlassventilen in [Pa]
	 */	
	public double get_p_LadeLuft(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, "p_Ladeluft","[Pa]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}	

	/*** 
	 * @return liefert den Druck vor den Auslassventilen in [Pa]
	 */	
	public double get_p_Abgas() {

		try {
			return set_doublePara(INPUTFILE_PARAMETER, "p_Abgas","[Pa]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}
	}

	public double get_p_FeuchteMessung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "p_FeuchteMessung","[Pa]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}	


	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void xxx_AB_HIER_WANDWAERMEUEBERGANG_PARAMETER(){}
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return liefert den Referenzpunkt [�KW nZOT], bei dem der Kappa Wert f�r das 
	 * WoschniHuber-Wandw�rmemodell bzw. Changs Modell gerechnet wird. Hensels Modell
	 * verwendet den Punkt auch.
	 */
	public double get_refPunkt_WoschniHuber(){
		double KW_refPunkt;		
		try {
			if(SYS.IS_KW_BASIERT){				
				KW_refPunkt =set_doublePara(INPUTFILE_PARAMETER, "refPunktWoschniHuber","[KWnZOT]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				KW_refPunkt=convert_KW2SEC(KW_refPunkt);
			}else {				
				KW_refPunkt =set_doublePara(INPUTFILE_PARAMETER, "refPunktWoschniHuber","[s]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return KW_refPunkt;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}







	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void xxx_AB_HIER_MOTOR_PARAMETER(){}
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Wenn der Motortyp "DruckKammer" augewaehlt wurde, wird mit dieser Funktion das 
	 * Kammervolumen vorgegeben
	 */
	public double get_DruckKammerVolumen() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "DruckKammerVolumen","[m^3]",1e-10,Double.MAX_VALUE); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Pleuellaenge in [m] zur�ck.	 * 
	 * */
	public double get_Pleuellaenge(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Pleuellaenge","[m]",0.01,10); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt den Bohrungsdurchmesser in [m] zur�ck.	 * 
	 * */
	public double get_Bohrung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Bohrung","[m]",0.005,2); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}	


	/** 
	 * @return Gibt den Kurbelradius in [m] zur�ck.	Dieser ist nicht unbedingt
	 * gleich 0.5xHub, z.B. bei einer Desachsierung.* 
	 * */
	public double get_Kurbelradius() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Kurbelradius","[m]",0.0025,Double.POSITIVE_INFINITY); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/** 
	 * @return Gibt die Verdichtung zur�ck.
	 * Die Verdichtung muss zwischen 1 und 30 liegen
	 * */
	public double get_Verdichtung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Verdichtung","",1,30); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return Gibt die Kolbenflaeche in [m^2]zur�ck.
	 * */
	public double get_pistonArea(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Kolbenflaeche","[m^2]",0.00002,5); //aus minimaler Bohrung und aus maximaler Bohrung berechnet
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/** 
	 * @return Gibt die Kolbenflaeche in [m^2]zur�ck.
	 * */
	public double get_fireDeckArea(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Brennraumdachflaeche","[m^2]",0.00002,5); //aus minimaler Bohrung und aus maximaler Bohrung berechnet
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Feuersteghoehe in [m]zur�ck.
	 * */
	public double get_Feuersteghoehe(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Feuersteghoehe","[m]",0,0.5); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_Quetschspalthoehe() {
		double Quetschspalthoehe;		
		try {
			Quetschspalthoehe =set_doublePara(INPUTFILE_PARAMETER, "Quetschspalthoehe","[m]",0,0.2);
			return Quetschspalthoehe;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}	

	/** 
	 * @return Gibt die Schr�nkung in [m]zur�ck.
	 * */
	public double get_Schraenkung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Schraenkung","[m]",-0.005,0.005); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return Gibt die Desachsierung in [m]zur�ck.
	 * */
	public double get_Desachsierung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "Desachsierung","[m]",-0.005,0.005); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}




	/** 
	 * @return Gibt den Zeitpunkt Auslassschluss in [s] nach Rechenbeginn zur�ck.
	 * */
	public double get_Auslassschluss(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC
		try {
			if(SYS.IS_KW_BASIERT){			
				double AV_S;
				AV_S =set_doublePara(INPUTFILE_PARAMETER, "Auslassschluss","[KWnZOT]",get_Auslassoeffnet_KW(),SYS.KW_OBERGRENZE);
				return convert_KW2SEC(AV_S);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "Auslassschluss","[s]",get_Auslassoeffnet(),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	


	/** 
	 * @return Gibt den Zeitpunkt Auslassoeffnet  in [s] nach Rechenbeginn zur�ck.
	 * */
	public double get_Auslassoeffnet(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC
		try {
			if(SYS.IS_KW_BASIERT){					
				return  convert_KW2SEC(
						set_doublePara(INPUTFILE_PARAMETER, "Auslassoeffnet","[KWnZOT]",SYS.KW_UNTERGRENZE,SYS.KW_OBERGRENZE));

			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "Auslassoeffnet","[s]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_Auslassoeffnet_KW(){
		return convert_SEC2KW(get_Auslassoeffnet());
	}


	/** 
	 * @return Gibt den Zeitpunkt Einlassoeffnet in [s] nach Rechenbeginn zur�ck.
	 * */
	public double get_Einlassoeffnet(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC
		try {
			if(SYS.IS_KW_BASIERT){			
				return convert_KW2SEC(
						set_doublePara(INPUTFILE_PARAMETER, "Einlassoeffnet","[KWnZOT]",SYS.KW_UNTERGRENZE,SYS.KW_OBERGRENZE));

			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "Einlassoeffnet","[s]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}	

	}

	public double get_Einlassoeffnet_KW(){
		return convert_SEC2KW(get_Einlassoeffnet());
	}

	/** 
	 * @return Gibt den Zeitpunkt Einlassschliesst in [s] nach Rechenbeginn zur�ck.
	 * */
	public double get_Einlassschluss(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC. 
		//Ausserdem ist natuerlich Einlassoeffnet der untere Grenzwert dieser wird aber nur in sec zurueck gegeben
		try {
			if(SYS.IS_KW_BASIERT){			
				double AV_S;
				AV_S =set_doublePara(INPUTFILE_PARAMETER, "Einlassschluss","[KWnZOT]",get_Einlassoeffnet_KW(),SYS.KW_OBERGRENZE);
				return convert_KW2SEC(AV_S);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "Einlassschluss","[s]",get_Einlassoeffnet(),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}	

	}
	/** 
	 * @return Gibt den Einlassventilhub in [m]
	 * */
	public double get_EV_Hub(){		
		double EV_Hub;		
		try {
			EV_Hub =set_doublePara(INPUTFILE_PARAMETER, "EV_Hub","[m]",0,0.3);
			return EV_Hub;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}

	}

	/** 
	 * @return Gibt den maximalen erlaubten Einlassventilhub in [m]
	 * */
	public double get_EV_Hub_max(){		
		double EV_Hub_max;		
		try {
			EV_Hub_max =set_doublePara(INPUTFILE_PARAMETER, "EV_Hub_max","[m]",0,0.3);
			return EV_Hub_max;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return Gibt den maximalen erlaubten Auslassventilhub in [m]
	 * */	
	public double get_AV_Hub_max(){
		double AV_Hub_max;
		try{
			AV_Hub_max =set_doublePara(INPUTFILE_PARAMETER, "AV_Hub_max","[m]",0,0.3);
			return AV_Hub_max;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return Gibt den inneren Sitzdurchmesser des Auslassventils in [m]
	 * */
	public double get_AV_durchmesser(){
		double AV_durchmesser;
		try{
			AV_durchmesser =set_doublePara(INPUTFILE_PARAMETER, "AV_durchmesser","[m]",0,0.3);
			return AV_durchmesser;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return Gibt den inneren Sitzdurchmesser des Einlassventils in [m]
	 * */
	public double get_EV_durchmesser(){		
		double EV_durchmesser;		
		try {
			EV_durchmesser =set_doublePara(INPUTFILE_PARAMETER, "EV_durchmesser","[m]",0,0.3);
			return EV_durchmesser;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return Gibt die Ventil�berschneidung von Einlass �ffnet (Hub 0,15mm) bis Auslass schlie�t (Hub 0,15mm) in [KW]
	 * */
	public double get_Ventilueberschneidung(){		
		double ventilueberschneidung;		
		try {
			ventilueberschneidung =set_doublePara(INPUTFILE_PARAMETER, "Ventilueberschneidung","[KW]",0,100);
			return ventilueberschneidung;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** 
	 * @return Gibt die Referenzfl�che zur�ck, die f�r die
	 * Durchflusskennzahlen des Auslassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Kolbenfl�che gerechnet.
	 * */
	public double get_ReferenzflaecheAuslass() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefFlaecheAuslass","[m^2]",1e-6,100^0);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefFlaecheAuslass\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Kolbenflaeche\" gerechnet!");
			return get_pistonArea();
		}		
	}
	/** 
	 * @return Gibt die Referenzfl�che zur�ck, die f�r die
	 * Durchflusskennzahlen des Einlassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Kolbenfl�che gerechnet.
	 * */
	public double get_ReferenzflaecheEinlass() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefFlaecheEinlass","[m^2]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefFlaecheEinlass\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Kolbenflaeche\" gerechnet!");
			return get_pistonArea();
		}		
	}


	/** 
	 * @return Gibt den Referenzdurchmesser zur�ck, der f�r die
	 * Durchflusskennzahlen des Einlassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Bohrung gerechnet.
	 * */
	public double get_ReferenzDurchmesserEV() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefDurchmesserEV","[m]",1e-6,1);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefDurchmesserEV\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Bohrung\" gerechnet!");
			return get_Bohrung();
		}	
	}

	/** 
	 * @return Gibt den Referenzdurchmesser zur�ck, der f�r die
	 * Durchflusskennzahlen des Auslassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Bohrung gerechnet.
	 * */
	public double get_ReferenzDurchmesserAV() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefDurchmesserAV","[m]",1e-6,1);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefDurchmesserAV\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Bohrung\" gerechnet!");
			return get_Bohrung();
		}	
	}	

	/**
	 * 
	 * @param ventil
	 * @return Gibt das Ventilspiel des ensprechenden Ventils zur�ck.
	 */
	public double get_Ventilspiel(String ventil) {
		double tmpSpiel = 0;
		if(ventil.equalsIgnoreCase("Einlass")) {
			try {
				tmpSpiel = set_doublePara(INPUTFILE_PARAMETER, "ventilspielEV", "[m]",0,0.001);
			} catch (ParameterFileWrongInputException e) {
				e.log_Warning("Der Wert f�r \"ventilspielEV\" wurde im Inputfile nicht oder falsch angegeben. \n " +
						"Es wird ohne Spiel gerechnet!");
			}
		}
		if(ventil.equalsIgnoreCase("Auslass")) {
			try {
				tmpSpiel = set_doublePara(INPUTFILE_PARAMETER, "ventilspielAV", "[m]",0,0.001);
			} catch (ParameterFileWrongInputException e) {
				e.log_Warning("Der Wert f�r \"ventilspielAV\" wurde im Inputfile nicht oder falsch angegeben. \n " +
						"Es wird ohne Spiel gerechnet!");
			}
		}
		return tmpSpiel;
	}
	/**
	 * @param
	 * @return liefert den Abstand zwischen dem Brennraumdach und dem Kolben im oberen Totpunkt [m]. Dieser wird
	 * Brennraumdachh�he genannt und wird in dem Wandw�rmemodell von Chang (HCCI) verwendet.
	 */
	public double get_BrennraumdachHoehe(){
		double brennraumDachHoehe;		
		try {
			brennraumDachHoehe =set_doublePara(INPUTFILE_PARAMETER, "brennraumDachHoehe","[m]",0,0.2);
			return brennraumDachHoehe;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}	


	private double set_doublePara(Hashtable<String,String []> parameterInHash, 
			String paraNameToSet, 
			String [] moeglicheEinheiten, 
			double min, double max) throws ParameterFileWrongInputException{

		Double temp;
		boolean found=false;

		String einheit;

		//Zuerst wird geprueft ob der Wert ueberhaupt im Inputfile ist...
		try{
			String s=parameterInHash.get(paraNameToSet)[0];	
			einheit=parameterInHash.get(paraNameToSet)[1];
			temp=Double.parseDouble(s);
			temp=temp.doubleValue();

		}catch(NumberFormatException e){
			throw new ParameterFileWrongInputException ("Der Wert fuer " + paraNameToSet + " wurde nicht als double eingegeben!");			

		}catch(NullPointerException e){
			throw new ParameterFileWrongInputException ("Es wurde versucht, den Parameter \"" +  paraNameToSet + "\" zu setzen. \n " +
					"Dieser ist im Parameterfile aber nicht vorhanden.");	
		}

		//...und dann wird ueberpr�ft ob der Wert die richtige Einheit hat...
		for( int i=0;i<moeglicheEinheiten.length;i++){		
			if(einheit.equalsIgnoreCase(moeglicheEinheiten[i]))
				found=true;			
		}		
		if(!found){			
			String message="Der Parameter \"" +  paraNameToSet + "\" muss in einer der folgenden Einheiten angegeben werden: ";

			for( int i=0;i<moeglicheEinheiten.length;i++){
				message="\n" + message+moeglicheEinheiten[i] ;						
			}	
			message=message+ "\nDie angegebene Einheit ist aber: " + einheit;

			throw new ParameterFileWrongInputException (message);
		}

		//...zum Schluss wird ueberprueft ob der Wert innerhalb der zulaessigen Grenzen liegt.
		if(!checkMinMax(temp,min,max)){
			throw new ParameterFileWrongInputException ("Der eingegebene Wert f�r \""	+ paraNameToSet + 
					"\" liegt nicht innerhalb der zulaessigen Grenzen \n" + min + "<" + paraNameToSet + "<" + max);					
		}		

		return temp;
	}






	private double set_doublePara(Hashtable<String,String []> parameterInHash, 
			String paraNameToSet, 
			String moeglicheEinheit, 
			double min, double max) throws ParameterFileWrongInputException{

		String [] einheiten;

		//Wenn keine Einheiten eingegeben werden..
		if(moeglicheEinheit.equals("")){
			einheiten=new String [2];
			einheiten[0]="[-]";
			einheiten[1]="[]";
		}else{		
			einheiten=new String [1];
			einheiten[0]=moeglicheEinheit;
		}

		return set_doublePara(parameterInHash,paraNameToSet, einheiten, min, max);

	}	


	private String set_StringPara(Hashtable<String, String[]> parameterInHash, 
			String paraNameToSet,String [] possibleParaValues) throws ParameterFileWrongInputException{
		String temp = null;
		boolean found=false;
		try{
			temp=parameterInHash.get(paraNameToSet)[0];			
		}catch(NullPointerException e){
			throw new ParameterFileWrongInputException ("Es wurde versucht, den Parameter \"" +  paraNameToSet + 
					"\" zu setzen. \n" +
					"Dieser ist im Parameterfile aber nicht vorhanden.");	
		}			

		for( int i=0;i<possibleParaValues.length;i++){		
			//if(temp.equalsIgnoreCase(possibleParaValues[i]))
			if(temp.equals(possibleParaValues[i]))
				found=true;			
		}

		if(!found){			
			String message="Der Parameter \"" +  paraNameToSet + "\" muss einen der folgenden Werte annehmen: ";

			for( int i=0;i<possibleParaValues.length;i++){
				message="\n" + message+possibleParaValues[i]+" " ;						
			}			
			throw new ParameterFileWrongInputException (message);
		}		
		return temp;
	}	


	private String set_FileName(Hashtable<String, String[]> parameterInHash, 
			String paraNameToSet) throws ParameterFileWrongInputException{
		String temp = null;
		try{
			temp=parameterInHash.get(paraNameToSet)[0];			
		}catch(NullPointerException e){
			throw new ParameterFileWrongInputException ("Es wurde versucht, den Parameter \"" +  paraNameToSet + 
					"\" zu setzen. \n" +
					"Dieser ist im Parameterfile aber nicht vorhanden.");	
		}			
		return temp;
	}	


	/**
	 * �berprueft ob der angegebene Wert innerhalb der vorgegebenen Grenzen liegt
	 * @param wert
	 * @param min
	 * @param max
	 * @return gibt true zur�ck wenn der Wert innerhalb der Grenzen liegt
	 */
	private boolean checkMinMax(double wert, double min, double max){
		if(wert>max){			
			return false;
		}
		else if(wert<min){			
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 
	 * @param zeitpunktKW Zeitpunkt in [KWnZOT]
	 * @return deltaSEC Zeitspanne in [s] --> Der Rechenbeginn liegt bei 0s
	 */
	public double convert_KW2SEC(double zeitpunktKW){		
		return(zeitpunktKW-SYS.RECHNUNGS_BEGINN_DVA_KW)/(360*get_DrehzahlInUproSec());		 	
	}


	/**
	 * Liefert den Kurbelwinkel zur angegebenen Zeit
	 * @param time in [s nach Rechenbeginn]
	 * @return liefert die Kurbelwinkelstellung zum Zeitpunkt time
	 */
	public double convert_SEC2KW(double time){
		return get_DrehzahlInUproSec()*360*time+SYS.RECHNUNGS_BEGINN_DVA_KW;	
	}


	public double convert_ProSEC_2_ProKW(double valueToconvert){
		double time=1;
		return valueToconvert/(get_DrehzahlInUproSec()*360*time);
	}

	public double convert_ProKW_2_ProSEC(double valueToconvert){		
		return valueToconvert*(get_DrehzahlInUproSec()*360);
	}


	public double convert_ProStunde_2_ProASP(double valueToconvert){
		double faktor=3600/(SYS.DAUER_ASP_KW/(360*get_DrehzahlInUproSec()));
		return valueToconvert/faktor;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////	

	public  class SysPara{
		private final Hashtable<String,String[]> PARAMETER;


		public final boolean DUBUGGING_MODE;
		public final double DUBUGGING_TIME_SEC;
		
		public final double RECHENGENAUIGKEIT_DVA;
		public final double MINIMALE_ZONENMASSE;
		public final double T_FREEZE;
		public final double RELAX; //Relaxationsfaktor fuer Iteration
		public final int SGOLAY_ORDNUNG;
		public final int SGOLAY_BREITE;
		
		public final double WRITE_INTERVAL_SEC;
		public final int ANZ_BERECHNETER_WERTE;

		//je nach gew�hlter Rechnungsart in [s] oder [KW]
		public final double RECHNUNGS_BEGINN_DVA_KW;
		public final double RECHNUNGS_ENDE_DVA_KW;
		public final double RECHNUNGS_BEGINN_DVA_SEC;
		public final double RECHNUNGS_ENDE_DVA_SEC;
		public final double KW_UNTERGRENZE,KW_OBERGRENZE,SEC_UNTERGRENZE, SEC_OBERGRENZE;

		public final double DAUER_ASP_KW;
		public final double DAUER_ASP_SEC;
		/**
		 * Liefert die Umdrehungen pro Arbeitsspiel
		 */
		public final double UMD_ASP;

		public final boolean IS_KW_BASIERT;	
		public final boolean IS_ZEIT_BASIERT;
		public final boolean IS_ZEIT_BASIERT_START_IN_KW;
		public final boolean SHIFT_pEIN,SHIFT_pAUS;
		public final boolean FILTERN;

		//vielleicht besser als get methode	
		public final File INDIZIER_FILE;
		public final int KANAL_SPALTEN_NR_PZYL;
		public final int KANAL_SPALTEN_NR_PEIN;
		public final int KANAL_SPALTEN_NR_PABG;
		public final String EINGABEDATEI_FORMAT;
		public final String NULLLINIEN_METHODE;
		// Brennverlaufsvariablen
		public File BRENNVERLAUF_FILE;
		public int KANAL_SPALTEN_NR_DQBURN;
		public String EINGABEDATEI_FORMAT_BURN;
		
		public final File DFKENNZAHL_AUS_FILE;
		public final File DFKENNZAHL_EIN_FILE;
		public final String DFKENNZAHLDATEI_EIN_FORMAT;
		public final String DFKENNZAHLDATEI_AUS_FORMAT;
		public final int KANAL_SPALTEN_NR_ALPHA_VOR;
		public final int KANAL_SPALTEN_NR_ALPHA_RUECK;
		
		public final File VENTILHUB_EIN_FILE;
		public final File VENTILHUB_AUS_FILE;
		public final String VENTILHUB_EIN_FORMAT;
		public final String VENTILHUB_AUS_FORMAT;
		
		public final String THERMO_POLYNOM_KOEFF_VORGABE;
		public final String GLEICHGEWICHTSKONSTANTEN_VORGABE;
		
		/**
		 * Gibt an ob der Nullpunkt der absoluten Enthalpie fuer die Verbindungen in ihrem stabilsten Zustand 
		 * bei 298.15K definiert wird (Standard in der Chemie) 
		 * oder ob die absolute Enthalie fuer Rauchgas mit Lambda >1 auf Null gesetzt wird. 
		 * Ture wenn Standard gewaehlt wird! </br>
		 * vgl. Diss. Grill oder deJaegher 
		 */
		public final boolean STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD;

		public SysPara(Hashtable<String, String[]> parameterVorgaben) throws ParameterFileWrongInputException{

			PARAMETER=parameterVorgaben;				

			String [] yesno ={"ja" , "nein"};	
			if(set_StringPara(PARAMETER,"debuggingMode",yesno ).equalsIgnoreCase("ja"))
				DUBUGGING_MODE=true;
			else
				DUBUGGING_MODE=false;
			if(set_StringPara(PARAMETER,"shift_pEin",yesno ).equalsIgnoreCase("ja"))
				SHIFT_pEIN=true;
			else
				SHIFT_pEIN=false;			
			if(set_StringPara(PARAMETER,"shift_pAus",yesno ).equalsIgnoreCase("ja"))
				SHIFT_pAUS=true;
			else
				SHIFT_pAUS=false;
			
			
			
			//Einlesen von PUBLIC FINAL doubleDaten
			RECHENGENAUIGKEIT_DVA=1e5*set_doublePara(PARAMETER, "rechenGenauigkeit_DVA","[bar]",0,0.1);

			MINIMALE_ZONENMASSE=set_doublePara(PARAMETER, "minimaleZonenMasse","[kg]",0,0.5); 

			//sollten sinnvolle Grenzen sein...Grill gibt 1600K an das alte Bremo verwendet 1700K
			T_FREEZE=set_doublePara(PARAMETER, "T_freeze","[K]",1400,1900);	
			
			double tmpRELAX;
			try{				
				tmpRELAX=set_doublePara(PARAMETER, "relaxFaktor","[-]",0.1,1);	
			}catch(ParameterFileWrongInputException e){
				e.log_Warning("Es wurde kein Relaxationsfaktor angegeben. " +
						"Es wird mit 0.7 gerechnet. \n" +
						"Der Relaxationsfaktor kann �ber den Parameter \"relaxFaktor\" definiert werden.");
				tmpRELAX=0.7;
			}
			
			RELAX=tmpRELAX;	
			
			if(set_StringPara(PARAMETER,"filtern",yesno ).equalsIgnoreCase("ja"))
				FILTERN=true;
			else
				FILTERN=false;
			
			double tmpSGOLAY_ORDNUNG=5;
			double tmpSGOLAY_BREITE=2*tmpSGOLAY_ORDNUNG+1;
			if(FILTERN){
				try{				
					tmpSGOLAY_ORDNUNG=set_doublePara(PARAMETER,"sgolayOrdnung","[-]", 2,12);
				}catch(ParameterFileWrongInputException e){
					e.log_Warning("Die Ordnung fuer den SavitzkyGolay-Filter wurde nicht angegeben. \n" +
					"Es wird mit einem Polynom 5ter Ordnung gerechnet.\n" +
					"Eine hoehere Ordnung kann mittles der Vorgabe des Parameters \"sgolayOrdnung\" erfolgen.");
					tmpSGOLAY_ORDNUNG=5;
				}
				
				try{				
					tmpSGOLAY_BREITE=set_doublePara(PARAMETER,"sgolayBreite","[-]", 2*tmpSGOLAY_ORDNUNG+1,50);
				}catch(ParameterFileWrongInputException e){
					tmpSGOLAY_BREITE=(2*tmpSGOLAY_ORDNUNG+1)*2; //zweifache Mindestbreite
					e.log_Warning("Die Breite fuer den SavitzkyGolay-Filter wurde nicht angegeben. \n" +
					"Es wird mit einer Breite von " + tmpSGOLAY_BREITE + " gerechnet. \n" +
					"Eine andere Breite kann mittles der Vorgabe des Parameters \"sgolayBreite\" erfolgen.");
					
				}
			}
			
			SGOLAY_ORDNUNG=(int)tmpSGOLAY_ORDNUNG;
			SGOLAY_BREITE=(int)tmpSGOLAY_BREITE;
			
			//Einlesen von PUBLIC FINAL StringDaten
			String []s1 ={"polytropenMethode", "referenzWert","abgleichSaugrohr","abgleichKruemmer","ohne"};			
			NULLLINIEN_METHODE=set_StringPara(PARAMETER,"nullLinienMethode",s1);
				

			String [] s2 ={"Janaf", "NASA9" , "Burcat", "ERC"};	
			THERMO_POLYNOM_KOEFF_VORGABE=set_StringPara(PARAMETER,"polynomKoeffizienten", s2);


			String [] s3 ={"Janaf" , "Burcat","Olikara"};	
			GLEICHGEWICHTSKONSTANTEN_VORGABE=set_StringPara(PARAMETER,"gleichGewichtsKonstanten", s3);			
			
			
			String temp;
			String []sDeltaHf={"ChemieStandard", "CO2_H2O_NULL"};
			try{				
				temp=set_StringPara(PARAMETER,"standardBildungsEnthalpien", sDeltaHf);
			}catch(ParameterFileWrongInputException e){
				e.log_Warning("Die Vorgehensweise zur bestimmung der Standardbildungsenthalpien der Elemente" +
						"wurden nicht defieniert. \n" +
						"Es wird mit \"" +sDeltaHf[1]+
						"\" gerechnet! \n" +
						"Alternativ kann auch \"standerdBildungsEnthalpien [-] := " +sDeltaHf[0]+
						"\" vorgegeben werden.");
				temp=sDeltaHf[1];
			}	
			if(temp.equals("ChemieStandard"))
				STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD=true;
			else
				STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD=false;
			
			////////////////////////////////////////////////////////////////
			//Einlesen von Daten die Abhaengig von anderen Eingaben sind////			
			////////////////////////////////////////////////////////////////
			String []s0 ={"2T","4T"};	
			if(set_StringPara(PARAMETER,"arbeitsverfahren",s0).equalsIgnoreCase("4T")){
				DAUER_ASP_KW=720;
				UMD_ASP=2;
			}else{
				DAUER_ASP_KW=360;
				UMD_ASP=1;
			}		

			KW_UNTERGRENZE=-1*this.DAUER_ASP_KW; //gilt f�r die Definition aller KW Angaben in KWnZOT
			KW_OBERGRENZE=this.DAUER_ASP_KW;

			//TODO Die Zeitbasierten grenzwerte richtig Definieren
			SEC_UNTERGRENZE=Double.NEGATIVE_INFINITY;
			SEC_OBERGRENZE=Double.POSITIVE_INFINITY;


			String []s ={"sec","sec_RechenBeginnInKW", "KW"};		
			if(set_StringPara(PARAMETER,"zeit_oder_KW_Basiert",s).equalsIgnoreCase("KW")){
				IS_KW_BASIERT=true;
				IS_ZEIT_BASIERT=false;
				IS_ZEIT_BASIERT_START_IN_KW=false;

			}else if (set_StringPara(PARAMETER,"zeit_oder_KW_Basiert",s).equalsIgnoreCase("sec")){
				IS_KW_BASIERT=false;
				IS_ZEIT_BASIERT=true;
				IS_ZEIT_BASIERT_START_IN_KW=false;
				if(MODUL_VORGABEN.get(MotorFabrik.MOTOR_FLAG)=="HubKolbenMotor")
					throw new ParameterFileWrongInputException("Eine vollstaendig zeitbasierte Rechnung kann " +
							"nur mit einem filebasierten Motorobjekt und einem Eingabefile der Form [ZEIT] [ZYLINDERVOLUMEN] erfolgen." +
							"Alternativ ist \"sec_RechenBeginnInKW\" zu w�hlen");

			}else{
				IS_KW_BASIERT=false;
				IS_ZEIT_BASIERT=false;
				IS_ZEIT_BASIERT_START_IN_KW=true;
			}				

			if(IS_KW_BASIERT){			
				double WRITE_INTERVAL_KW=set_doublePara(PARAMETER, "rechnungsSchrittweite","[KW]",0.01,2);	
				//TODO Grenzwerte mittels Einlassoeffnet und Auslassschlie�t definieren. 
				//Fuer die Ladungswechselanalyse werden eigene Rechenbeginne und Rechenenden definiert.
				WRITE_INTERVAL_SEC= WRITE_INTERVAL_KW/(360*get_DrehzahlInUproSec());

				RECHNUNGS_BEGINN_DVA_KW=set_doublePara(PARAMETER, "rechnungsBeginn","[KWnZOT]",
						KW_UNTERGRENZE,KW_OBERGRENZE);				

				RECHNUNGS_ENDE_DVA_KW=set_doublePara(PARAMETER, "rechnungsEnde","[KWnZOT]",
						RECHNUNGS_BEGINN_DVA_KW,KW_OBERGRENZE);		
				//der Anfang zaehlt mit
				ANZ_BERECHNETER_WERTE=(int)((RECHNUNGS_ENDE_DVA_KW-RECHNUNGS_BEGINN_DVA_KW)/WRITE_INTERVAL_KW)+1;

				RECHNUNGS_ENDE_DVA_SEC=(ANZ_BERECHNETER_WERTE-1)*WRITE_INTERVAL_SEC;			

				RECHNUNGS_BEGINN_DVA_SEC=0;				

				if(DUBUGGING_MODE){
					double b=set_doublePara(PARAMETER, "debuggingTime","[KWnZOT]",RECHNUNGS_BEGINN_DVA_KW,RECHNUNGS_ENDE_DVA_KW+DAUER_ASP_KW);

					DUBUGGING_TIME_SEC=(b-RECHNUNGS_BEGINN_DVA_KW)/(360*get_DrehzahlInUproSec());	

				}else{
					DUBUGGING_TIME_SEC=RECHNUNGS_BEGINN_DVA_SEC;
				}


			}else if(IS_ZEIT_BASIERT){
				//TODO um die gesamte zeitbasierte sache muss sich noch jemand kuemmern!!!

				//min-Wert --> ca. 0.01�KW bei 18000U/min
				//max-Wert --> ca .2�KW bei 50 U/min
				WRITE_INTERVAL_SEC=set_doublePara(PARAMETER, "rechnungsSchrittweite","[s]",9.5e-7,6.7e-3);

				//muss eigentlich nur passend zum MotorFile und DruckFile sein
				RECHNUNGS_BEGINN_DVA_SEC=set_doublePara(PARAMETER, "rechnungsBeginn","[s]",
						Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);				

				RECHNUNGS_ENDE_DVA_SEC=set_doublePara(PARAMETER, "rechnungsEnde","[s]",
						RECHNUNGS_BEGINN_DVA_SEC,Double.POSITIVE_INFINITY);		

				ANZ_BERECHNETER_WERTE=(int)(( RECHNUNGS_ENDE_DVA_SEC-RECHNUNGS_BEGINN_DVA_SEC)/WRITE_INTERVAL_SEC)+1;

				//final Variablen muessen mir Werten belegt werden
				RECHNUNGS_BEGINN_DVA_KW=555.555;
				RECHNUNGS_ENDE_DVA_KW=555.555;

				if(DUBUGGING_MODE){
					DUBUGGING_TIME_SEC=set_doublePara(PARAMETER, "debuggingTime","[s]",RECHNUNGS_BEGINN_DVA_SEC,RECHNUNGS_ENDE_DVA_SEC);
				}else{
					DUBUGGING_TIME_SEC=RECHNUNGS_BEGINN_DVA_SEC;
				}				

			}else if(IS_ZEIT_BASIERT_START_IN_KW){
				//min-Wert --> ca. 0.01�KW bei 18000U/min
				//max-Wert --> ca .2�KW bei 50 U/min
				WRITE_INTERVAL_SEC=set_doublePara(PARAMETER, "rechnungsSchrittweite","[s]",9.5e-5,6.7e-3);

				RECHNUNGS_BEGINN_DVA_KW=set_doublePara(PARAMETER, "rechnungsBeginn","[KWnZOT]",
						KW_UNTERGRENZE,KW_OBERGRENZE);	

				RECHNUNGS_BEGINN_DVA_SEC=0;	

				RECHNUNGS_ENDE_DVA_SEC=set_doublePara(PARAMETER, "rechnungsEnde","[s]",
						RECHNUNGS_BEGINN_DVA_SEC,Double.POSITIVE_INFINITY);	
				//die Zeitrechnung beginnt hier bei null!
				ANZ_BERECHNETER_WERTE=(int)(RECHNUNGS_ENDE_DVA_SEC/WRITE_INTERVAL_SEC)+1;

				RECHNUNGS_ENDE_DVA_KW=360*get_DrehzahlInUproSec()*RECHNUNGS_ENDE_DVA_SEC;			



				if(DUBUGGING_MODE){
					DUBUGGING_TIME_SEC=set_doublePara(PARAMETER, "debuggingTime","[s]",RECHNUNGS_BEGINN_DVA_SEC,RECHNUNGS_ENDE_DVA_SEC);
				}else{
					DUBUGGING_TIME_SEC=RECHNUNGS_BEGINN_DVA_SEC;
				}				
			}else{
				RECHNUNGS_BEGINN_DVA_SEC=0;
				RECHNUNGS_ENDE_DVA_SEC=0;
				ANZ_BERECHNETER_WERTE=-1;

				throw new ParameterFileWrongInputException("Die Flags: \"IS_ZEIT_BASIERT_START_IN_KW\", \" IS_ZEIT_BASIERT\" " +
						"oder \"IS_KW_BASIERT\" wurden nicht richtig gesetzt ");			
			}

			if((IS_ZEIT_BASIERT && RECHNUNGS_ENDE_DVA_SEC<RECHNUNGS_BEGINN_DVA_SEC )|| (IS_KW_BASIERT && RECHNUNGS_ENDE_DVA_SEC<RECHNUNGS_BEGINN_DVA_SEC)||
					(IS_ZEIT_BASIERT_START_IN_KW && RECHNUNGS_ENDE_DVA_KW<RECHNUNGS_BEGINN_DVA_KW ))
				throw new ParameterFileWrongInputException("Das Rechnungsende liegt vor dem Rechnungsbeginn");			


			String indizierFileName=set_FileName(PARAMETER,"indizierFileName");
			int indexOf=indizierFileName.indexOf(".");
			EINGABEDATEI_FORMAT=indizierFileName.substring(indexOf+1); //Dateiendung
			INDIZIER_FILE=new File(WD+indizierFileName);

			KANAL_SPALTEN_NR_PZYL=(int)set_doublePara(PARAMETER, "spalte_pZyl","",1,40); //bis zu f�nf kaskadierte CombiWF
			KANAL_SPALTEN_NR_PEIN=(int)set_doublePara(PARAMETER, "spalte_pEin","",1,40);
			KANAL_SPALTEN_NR_PABG=(int)set_doublePara(PARAMETER, "spalte_pAbg","",1,40);


			if (EINGABEDATEI_FORMAT.equalsIgnoreCase("txt")){	

				if(KANAL_SPALTEN_NR_PZYL==1|| KANAL_SPALTEN_NR_PABG==1 || KANAL_SPALTEN_NR_PEIN==1){
					String kanal=null;
					if(KANAL_SPALTEN_NR_PZYL==1)kanal="spalte_pZyl";
					if(KANAL_SPALTEN_NR_PABG==1)kanal="spalte_pAbg";
					if(KANAL_SPALTEN_NR_PEIN==1)kanal="spalte_pEin";
					throw new ParameterFileWrongInputException("Der Wert f�r \"" + kanal+ "\" wurde auf die erste Spalte des Druckeingabefiles gesetzt. \n " +
					"In der ersten spalte muss aber immer die Zeit bzw. der Kurbelwinkel stehen.");	
				}
			}

			if(KANAL_SPALTEN_NR_PZYL==KANAL_SPALTEN_NR_PEIN || KANAL_SPALTEN_NR_PZYL==KANAL_SPALTEN_NR_PABG || KANAL_SPALTEN_NR_PEIN==KANAL_SPALTEN_NR_PABG)
				throw new ParameterFileWrongInputException("Die angegebenen Kanalnummern bzw Spaltennummern " +
				"f�r pZyl, pEin und pAbg sind teilweise identisch");
			try{
				String burnFileName=set_FileName(PARAMETER,"burnFileName");
				int indexOf_burn=burnFileName.indexOf(".");
				EINGABEDATEI_FORMAT_BURN=burnFileName.substring(indexOf_burn+1); //Dateiendung
				BRENNVERLAUF_FILE=new File(WD+burnFileName);

				KANAL_SPALTEN_NR_DQBURN=(int)set_doublePara(PARAMETER, "spalte_dQburn","",1,40); //bis zu f�nf kaskadierte CombiWF
				}
				catch (ParameterFileWrongInputException e) {
					e.log_Warning("Es wurde kein Brennverlaufsfile angegeben! Es kann keine APR durchgef�hrt werden!");
					
				
		
				}
		
			
			DAUER_ASP_SEC=(DAUER_ASP_KW)/(360*get_DrehzahlInUproSec());

		
			String durchflusskennzahlEinFileName=set_FileName(PARAMETER,"durchflusskennzahlEinFileName");
			String durchflusskennzahlAusFileName=set_FileName(PARAMETER,"durchflusskennzahlAusFileName");		
	
			indexOf=durchflusskennzahlEinFileName.indexOf(".");
			DFKENNZAHLDATEI_EIN_FORMAT=durchflusskennzahlEinFileName.substring(indexOf+1); //Dateiendung
			indexOf=durchflusskennzahlAusFileName.indexOf(".");
			DFKENNZAHLDATEI_AUS_FORMAT=durchflusskennzahlEinFileName.substring(indexOf+1); //Dateiendung
			DFKENNZAHL_AUS_FILE=new File(WD+durchflusskennzahlEinFileName);
			DFKENNZAHL_EIN_FILE=new File(WD+durchflusskennzahlAusFileName);
			KANAL_SPALTEN_NR_ALPHA_VOR=(int)set_doublePara(PARAMETER, "spalte_alphaVor","",1,3);
			KANAL_SPALTEN_NR_ALPHA_RUECK=(int)set_doublePara(PARAMETER, "spalte_alphaRueck","",1,3);

			if (DFKENNZAHLDATEI_EIN_FORMAT.equalsIgnoreCase("txt")){
				if(KANAL_SPALTEN_NR_PZYL==1|| KANAL_SPALTEN_NR_PABG==1 || KANAL_SPALTEN_NR_PEIN==1){
					String kanal=null;
					if(KANAL_SPALTEN_NR_ALPHA_VOR==1)kanal="spalte_alpha_Vor";
					if(KANAL_SPALTEN_NR_ALPHA_RUECK==1)kanal="spalte_alpha_Rueck";
					throw new ParameterFileWrongInputException("Der Wert f�r \"" + kanal+ "\" wurde auf die erste Spalte des Durchflusskennzahleingabefiles gesetzt. \n " +
					"In der ersten spalte muss aber immer Ventilhub als absoluter Wert oder als L/D stehen");	
				}
			}
			if(KANAL_SPALTEN_NR_ALPHA_VOR==KANAL_SPALTEN_NR_ALPHA_RUECK)
				throw new ParameterFileWrongInputException("Die angegebenen Kanalnummern bzw Spaltennummern " +
				"f�r alpha_Ein und alpha_Aus sind identisch");

			String ventilhubEinFileName=set_FileName(PARAMETER,"VentilhubEinFileName");
			String ventilhubAusFileName=set_FileName(PARAMETER,"VentilhubAusFileName");				
			
			VENTILHUB_EIN_FILE=new File(WD+ventilhubEinFileName);
			VENTILHUB_AUS_FILE=new File(WD+ventilhubAusFileName);
			indexOf=ventilhubEinFileName.indexOf(".");
			VENTILHUB_EIN_FORMAT=ventilhubEinFileName.substring(indexOf+1); //Dateiendung;
			indexOf=ventilhubAusFileName.indexOf(".");
			VENTILHUB_AUS_FORMAT=ventilhubAusFileName.substring(indexOf+1); //Dateiendung;	
			
		}//Konstruktor SysPara	
		
	}//Klasse SysPara

}//Klasse CasePara
