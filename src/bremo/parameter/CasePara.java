package bremo.parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.plaf.synth.SynthViewportUI;

import com.sun.org.apache.xerces.internal.parsers.CachingParserPool.SynchronizedGrammarPool;

import misc.PhysKonst;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.SpeziesFabrik;
import kalorik.spezies.Spezies;
import io.InputFileReader;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.IterativeBerechnung;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.BerechnungsModellFabrik;
import berechnungsModule.Berechnung.CanteraCaller;
import berechnungsModule.blowby.BlowBy;
import berechnungsModule.blowby.BlowByFabrik;
import berechnungsModule.gemischbildung.Kraftstoff_Eigenschaften;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.internesRestgas.InternesRestgas;
import berechnungsModule.internesRestgas.InternesRestgasFabrik;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.MotorFabrik;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechnerFabrik;
import berechnungsModule.turbulence.TurbulenceModelFactory;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import berechnungsModule.wandwaerme.WandWaermeUebergangFabrik;
import bremo.sys.Solver;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.manager.ManagerLanguage;




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
	public final Solver SOLVER;
	public final TurbulenceModelFactory TURB_FACTORY;
	public final BlowBy BLOW_BY_MODELL;
	public IterativeBerechnung ITERATIVE_BERECHNUNG;
	protected final boolean callsCantera;
	private boolean calledFromGUI;
	private boolean compareToExpIni=false;
	private boolean compareToExp=false;
	
	//Kerrom: Heizwerte (Werte überprüfen/anpassen/vereinheitlichen)
	//eventuell an andere Stelle, neue Klasse Abgas erstellen
	//verwendet in PostProcessor, Verlustteilung
	public final double HU_CO = 282.9*1e3; //[J/mol] aus R. Pischinger S. 93
	public final double HU_HC = 2044.2*1e3;	//Ge Liu	// Alternativ für C3H8: 406.9*1e3 aus Merker/Schwarz/Teichmann (S.353) oder 2041*1e3 aus Screenshot von Philipp
	public final double HU_H2 = 241.1*1e3; // [J/mol] aus Screenshot von Philipp
	public final double HU_C = 2041.367*1e3;	// [J/mol] aus Merker/Schwarz/Teichmann "Grundlagen Verbrennungsmotoren" S.353
	

	public CasePara(File inputFile) throws ParameterFileWrongInputException {			
		InputFileReader	ifr =new InputFileReader(inputFile);
		INPUTFILE_PARAMETER=ifr.get_eingabeParameter();

		MODUL_VORGABEN=ifr.get_berechnungsModule();

		//		LittleHelpers.print_Hash(INPUTFILE_PARAMETER);
		String separator  =ManagerLanguage.getString("separator");
		System.err.println(separator+"Inputfile wurde eingelesen!");
		//WD wird in SYS verwendet, muss also vorher mit einem Wert belegt sein
		WD=inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().indexOf(inputFile.getName()));
		CASE_NAME=inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".")); 
		System.err.println(WD+CASE_NAME + ".txt"+separator);
//		//Is doof aber geht jetzt nicht besser
		callsCantera=BerechnungsModellFabrik.callsCantera(this);	
		
		SYS=new SysPara(INPUTFILE_PARAMETER);	
		
		//SpeziesFabrik	
		SPEZIES_FABRIK=new SpeziesFabrik(this, new MakeMeUnique());	 
		//MakeMeUnique sorgt dafür, dass es nur eine Instanz der Speziesfabrik geben kann. 
		//Ansonsten wuerde es Probleme mit der INtegration der einzelnen Speziesmassen geben. 
		//CO2 ist zwar CO2 aber bei verschiedenen Objekt IDs weiss Bremo das nicht!			

		//Solver
		SOLVER=new Solver(this);
		
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

		//BlowBy
		BlowByFabrik bbf = new BlowByFabrik(this);
		BLOW_BY_MODELL = bbf.BLOW_BY_MODELL;
		
		//Wandwaerme
		WandWaermeUebergangFabrik wwf=new WandWaermeUebergangFabrik(this);
		WAND_WAERME=wwf.WAND_WAERME_MODUL;
		//Die LWA soll mit einem anderen WW-Modell durchgefuehrt werden koennen	
		//this needs the internal EGR model
		WAND_WAERME_LW=wwf.WAND_WAERME_MODUL_LW; 	
		
		//Turbulence
		TURB_FACTORY=new TurbulenceModelFactory(this, new MakeMeUnique());
		
//		//Berechnungsmodell benoetigt MASTER_EINSPRITZUNG und RESTGAS_MODELL
		BerechnungsModellFabrik bmf=new BerechnungsModellFabrik(this);
		BERECHNUNGS_MODELL=bmf.BERECHNUNGS_MODELL;	
		SOLVER.set_BerechnungsModell(BERECHNUNGS_MODELL);
		
		//ABBRUCH für Verlustteilung wenn nicht APR_1Zonig ODER DVA_1Zonig
		if( is_Verlustteilung()
			&!(
					MODUL_VORGABEN.get(ManagerLanguage.getString("berechnungsModell")).equals(ManagerLanguage.getString("DVA_1Zonig"))|MODUL_VORGABEN.get(ManagerLanguage.getString("berechnungsModell")).equals(ManagerLanguage.getString("APR_1Zonig")))
//					MODUL_VORGABEN.get("berechnungsModell").equals("DVA_1Zonig")|(MODUL_VORGABEN.get("berechnungsModell").equals("APR_1Zonig")&compareToExp()))
//					(MODUL_VORGABEN.get("berechnungsModell").equals("DVA_1Zonig")|MODUL_VORGABEN.get("berechnungsModell").equals("APR_1Zonig"))
//					&MODUL_VORGABEN.get("internesRestgasModell").equals("LWA"))
				){
			try{
				throw new BirdBrainedProgrammerException("Für die Verlsutteilung muss "+ManagerLanguage.getString("DVA_1Zonig")+" oder "+ManagerLanguage.getString("APR_1Zonig")+" gewählt sein");
			}catch(BirdBrainedProgrammerException bbp){
				bbp.stopBremo();
			}			
		}		
		
		/////////////// Kerrom, 06.06.14 //////// Plausibilitätsüberprüfung mit Lambda ////////////
				
		if (get_Lambda_Input() >= 0) { //nur, wenn lambda korrekt eingelesen wurde
			//lambda berechnen
			double lambda_berechnet = get_mLuft_feucht_ASP() / (MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP() * MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst());
			//Differenz (Betrag) zwischen berechnetem und eingelesenem lambda
			double diff = Math.abs(get_Lambda_Input() - lambda_berechnet);

			// Warnung, wenn die Differenz größer als 0.01 ist
			if (diff > 0.01) {
				System.err.println( separator 
						+ "ACHTUNG: Die Differenz zwischen eingelesenem Lambda und berechnetem Lambda beträgt " + diff
						+ ". \nEs wurden folgende Werte verwendet:\n"+ManagerLanguage.getString("mLuft_feucht")+" = " + get_mLuft_feucht_ASP()
						+ "\n"+ManagerLanguage.getString("mKrst")+" (Summe aller Einspritzungen) = "	+ MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP()
						+ "\n"+ManagerLanguage.getString("Lst")+" = " + MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst()
						+ "\n"+ManagerLanguage.getString("Lambda")+" (eingelesenes) = " + get_Lambda_Input()
						+ "\nberechnetes lambda = " + lambda_berechnet + separator);
			}
		}
		
		/////////////////////////////////////////////////////////////////
		
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
	
	public void set_IterativeBerechnung(IterativeBerechnung iR){
		ITERATIVE_BERECHNUNG = iR;	
	}

	/**
	 * Für einen BerechnungsCase sind alle ErgebnisBuffer in einem Vector gespeichert. Hiermit wird dem 
	 * Vector der ErgebnisBuffer ergB hinzugefuegt und kann damit spaeter in ein txt-file geschreiben werden.
	 * Der checkIn erfolgt automatisch bei der Erzeugung eines {@link ErgebnisBuffer}
	 * @param ergB
	 */
	public void ergBufferCheckIN(ErgebnisBuffer ergB){
		if(!alleErgBuffers.contains(ergB))//nur wenn der ErgebnisBuffer noch nicht im Vector steht wird er eingecheckt
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
	
	/** 
	 * Mit dieser Methode wird ausgewählt, ob bei der APR ein Vergleich mit einem 
	 * Experiment durchgeführt werden soll.
	 * Dazu müssen Indizierdaten angegeben sein.
	 */
	public boolean compareToExp(){		
		//boolean compareToExp = false;
		String s = null;
		String s2 []= {ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		if(!compareToExpIni){
		try {
			s=this.set_StringPara(INPUTFILE_PARAMETER, ManagerLanguage.getString("compareToExp"),s2);
		} catch (ParameterFileWrongInputException e){	
			compareToExp=false;
			e.log_Warning("Bei der APR wird kein Vergleich mit einem Experiment (\""+ManagerLanguage.getString("compareToExp")+"\") durchgeführt. "
					+ "Es müssen keine Indizierdaten vorgegeben werden. "
					+ "Dafür sind alle Daten zur Initialisierung von Hand vorzugeben.");
			s="nein";
		}
		compareToExpIni = true;
		if(s.equalsIgnoreCase("ja"))
			compareToExp=true;
		}
		return compareToExp;		
	}
	
	public boolean filterPressureData(){
		String [] yesno={ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		boolean filter=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,ManagerLanguage.getString("filtern"),yesno ).equalsIgnoreCase(ManagerLanguage.getString("ja")))
				filter=true;
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
		}
		return filter;		
	}
	
	public int get_savitzkyGolayOrder(){
		double tmpSgolayOrder=5;
		try{				
			tmpSgolayOrder=set_doublePara(INPUTFILE_PARAMETER,ManagerLanguage.getString("sgolayOrder"),"[-]", 2,12);
		}catch(ParameterFileWrongInputException e){
			e.log_Warning();
			e.log_Warning("The parameter \""+ManagerLanguage.getString("sgolayOrder")+"\" for the SavitzkyGolay-Filter has not been defined or was defined in a wrong way! \n" +
					"The filtering will be performed with a polynomial of order 5.\n");
		}
		return (int)tmpSgolayOrder;		
	}	
	
	public int get_savitzkyGolayHalfWidth(){			
		double tmpSgolayHalfWidth=2*get_savitzkyGolayOrder()+1;
		try{				
			tmpSgolayHalfWidth=set_doublePara(INPUTFILE_PARAMETER,ManagerLanguage.getString("sgolayHalfWidth"),"[-]", tmpSgolayHalfWidth,50);
		}catch(ParameterFileWrongInputException e){	
			e.log_Warning();
			e.log_Warning("The parameter \""+ManagerLanguage.getString("sgolayHalfWidth")+"\" for the SavitzkyGolay-Filter has not been defined or was defined in a wrong way! \n" +
					"The filtering will be performed with a half-witdh of "+tmpSgolayHalfWidth);
		}
		return (int)tmpSgolayHalfWidth;
	}
	
	public boolean shift_pInlet(){
		String [] yesno={ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		boolean shift_pIn=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,ManagerLanguage.getString("shift_pEin"),yesno ).equalsIgnoreCase(ManagerLanguage.getString("ja")))
				shift_pIn=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return shift_pIn;
	}
	
	public boolean shift_pOutlet(){
		String [] yesno={ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		boolean shift_pOut=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,ManagerLanguage.getString("shift_pAus"),yesno ).equalsIgnoreCase(ManagerLanguage.getString("ja")))
				shift_pOut=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return shift_pOut;
	}

	public String get_pressureAdjustmentMethod(String methodIdentifier){
		String method=null;
		try {		
			String []s1 ={ManagerLanguage.getString("polytropenMethode"), ManagerLanguage.getString("referenzWert"),
					      ManagerLanguage.getString("abgleichSaugrohr"),ManagerLanguage.getString("kanalMethode"),
					      ManagerLanguage.getString("abgleichKruemmer"),ManagerLanguage.getString("offset"),
					      ManagerLanguage.getString("summenbrennverlauf"),ManagerLanguage.getString("ohne")};			
			method=set_StringPara(this.INPUTFILE_PARAMETER,methodIdentifier,s1);
		}catch(ParameterFileWrongInputException e){
			e.stopBremo();
		}
		return method;
	}
	
	public double get_pressureOffset(){
		try{
			double offset = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("offset"), "["+ManagerLanguage.getString("Pa")+"]",-1e9,1e9);
			return offset;
		} catch (ParameterFileWrongInputException p) {
			p.log_Warning("Der Parameter \""+ManagerLanguage.getString("offset")+"\" wurde nicht gesetzt, es wird ohne gerechnet.");
			return 0;
		}
	}
	
	public int get_ColumnToRead(String columnIdentifier){		
		int column;
		try {
			column = (int)set_doublePara(INPUTFILE_PARAMETER, columnIdentifier,"",1,40);
			return column;
		} catch (ParameterFileWrongInputException e) {
			if(calledFromGUI){
				e.log_Warning(e.getMessage());
			}else{
				e.printStackTrace();
			}
			e.stopBremo();
			return 0;
		}		
	}
	
	public double get_relaxFactor(){
		double tmpRELAX;
		try{				
			tmpRELAX=set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("relaxFactor"),"[-]",0.1,1);	
		}catch(ParameterFileWrongInputException e){
			e.log_Warning("The parameter \""+ManagerLanguage.getString("relaxFactor")+"\" has not been defined!"+
					" The run will be performed with 0.7! \n");
			tmpRELAX=0.7;
		}
		return tmpRELAX;	
	}
	
	public double get_precissionPressureTraceAnalysis(){
		//Einlesen von PUBLIC FINAL doubleDaten
		double precission=0.0005*1e5;
		try {
			precission=1e5*set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("precissionPressureTraceAnalysis"),"["+ManagerLanguage.getString("bar")+"]",0,0.1);
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
			if(calledFromGUI){
				e.log_Warning(e.getMessage());
			}else{
				e.printStackTrace();
			}
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
	 * checks if file exists
	 * @param filename
	 * @return yesno
	 */
	public boolean fileExist(String filename){
		boolean exists;
		String fileName=null;
		try {
			fileName = set_FileName(this.INPUTFILE_PARAMETER,filename);
			exists = true;
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning(filename + " wurde nicht Angegeben. Es wird ohne weitergerechnet.");
			exists = false;
		}
		if(exists){
			File file = new File(WD+fileName);
			if(!file.exists()){
				try {
					throw new ParameterFileWrongInputException(
							"The specified path does not point to a file \n"
									+ (WD+fileName));
				} catch (ParameterFileWrongInputException e) {
					e.log_Warning("Der Angegebene Pfad zeigt nicht auf eine Datei: \n"
									+ (WD+filename) + ". Es wird ohne weitergerechnet");
					exists = false;
				}
			}
		}	
		
		return exists;
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
	 * Mit dieser Methode wird die aktuelle Rechenzeit zurückgegeben.
	 * Diese Zeit entspricht dem letzten ganzen Rechenzeitschritt und
	 * nicht einem vom RungeKutta-Verfahren verwendeten Zwischenschritt.
	 * @param time in [s]
	 */
	public double get_time(){
		return aktuelle_RZ;
	}
	
	/**
	 * Alle 4 Vibe-Parameter einlesen, 
	 * @return
	 */
	public double[] get_vibe_parameter(int index){
		double[] vibePara = new double[4];
		boolean fehler = false;
		try{
			vibePara[0] = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("brennbeginn_")+index, "["+ManagerLanguage.getString("KWnZOT")+"]", SYS.RECHNUNGS_BEGINN_DVA_KW+this.convert_SEC2KW(SYS.WRITE_INTERVAL_SEC),SYS.RECHNUNGS_ENDE_DVA_KW);
		}catch(ParameterFileWrongInputException e){
			e.log_Warning("Im Inputfile wurden nicht alle Vibe-Parameter angegeben." +
					"Bitte \""+ManagerLanguage.getString("brennbeginn_")+index+"\" in \"[KWnZOT]\" mit angeben.");
			fehler = true;
			vibePara[0] = 0;
		}
		try{
			vibePara[1] = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("brenndauer_")+index, "["+ManagerLanguage.getString("KW")+"]", 0,(SYS.RECHNUNGS_ENDE_DVA_KW - vibePara[0] - this.convert_SEC2KW(SYS.WRITE_INTERVAL_SEC)));
		}catch(ParameterFileWrongInputException e){
			e.log_Warning("Im Inputfile wurden nicht alle Vibe-Parameter angegeben." +
					"Bitte \""+ManagerLanguage.getString("brenndauer_")+index+"\" in \"["+ManagerLanguage.getString("KW")+"]\" mit angeben.");
			fehler = true;
		}
		try{
			vibePara[2] = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("vibeFormfaktor_")+index, "[-]", 0,10);
		}catch(ParameterFileWrongInputException e){
			e.log_Warning("Im Inputfile wurden nicht alle Vibe-Parameter angegeben." +
					"Bitte \"vibeFormfaktor_"+index+"\" in \"[-]\" mit angeben.");
			fehler = true;
		}
		try{
			vibePara[3] = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("vibeBrennEnergie_")+index, "["+ManagerLanguage.getString("J")+"]", 0,Double.POSITIVE_INFINITY);
		}catch(ParameterFileWrongInputException e){
			e.log_Warning("Im Inputfile wurden nicht alle Vibe-Parameter angegeben." +
					"Bitte \""+ManagerLanguage.getString("vibeBrennEnergie_")+index+"\" in \"["+ManagerLanguage.getString("J")+"]\" mit angeben.");
			fehler = true;
		}
		if(fehler){
			try{
				throw new ParameterFileWrongInputException(ManagerLanguage.getString("Vibe-Fehler"));
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();
			}
		}
		return vibePara;
	}
	
	/**
	 * Anzahl an Vibefunktionen angeben.
	 * @return
	 */
	public double get_anzVibe(){
		double anzVibe;
		try{
			anzVibe = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("anzVibe"), "[-]", 1, Double.POSITIVE_INFINITY);
			return anzVibe;
		}catch(ParameterFileWrongInputException e){
			e.stopBremo();
			return Double.NaN;
		}
	}

	/**
	 * Gibt an ab welchem Zeitpunkt die Verbrennung beginnen soll. Ab diesem Zeitpunkt wird 
	 * die freigesetzte Wärme in einen Massenumsatz umgerechnet.
	 * @return
	 */
	public double get_verbrennungsBeginnSEC(){
		double verbrennungsBeginn;	

		if(is_VerbrennungAutoDetect()){
			try{
				throw new BirdBrainedProgrammerException("Im InputFile wurde \""+ManagerLanguage.getString("VerbrennungsBeginnAutoDetect")+"\" auf \""+ManagerLanguage.getString("ja")+"\" " +
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
						INPUTFILE_PARAMETER, ManagerLanguage.getString("verbrennungsBeginn"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				verbrennungsBeginn=convert_KW2SEC(verbrennungsBeginn);
			}else {				
				verbrennungsBeginn =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("verbrennungsBeginn"),"["+ManagerLanguage.getString("s")+"]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return verbrennungsBeginn;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		

	}
	
	/**
	 * Gibt an, ab welchem Zeitpunkt in der LWA bei einer Zwischenkompression ein Kraftstoffumsatz erfolgen soll. Ab
	 * diesem Zeitpunkt wird die freigesetzte Wärme in einen Massenumsatz umgerechnet.
	 */
	private double umsatzBeginn=-5.55;
	public double get_verbrennungsBeginnLWASEC(){
		if(!(umsatzBeginn==-5.55)) return umsatzBeginn;
		try{
			if(SYS.IS_KW_BASIERT){				
				umsatzBeginn =set_doublePara(
						INPUTFILE_PARAMETER, ManagerLanguage.getString("verbrennungsBeginnLWA"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.RECHNUNGS_ENDE_DVA_KW,
						SYS.RECHNUNGS_BEGINN_DVA_KW+convert_SEC2KW(SYS.DAUER_ASP_SEC));
				umsatzBeginn=convert_KW2SEC(umsatzBeginn);
			}else {				
				umsatzBeginn =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("verbrennungsBeginnLWA"),"["+ManagerLanguage.getString("s")+"]",SYS.RECHNUNGS_ENDE_DVA_SEC,
						SYS.RECHNUNGS_BEGINN_DVA_SEC+SYS.DAUER_ASP_SEC);
			}				
			return umsatzBeginn;				
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Im Inputfile wurde \""+ManagerLanguage.getString("verbrennungsBeginnLWA")+"\" nicht angegeben!\n"+
					"Der Wärmeeintrag in der Zwischenkompression wird sofort in einen "+
					"Massenumsatz umgerechnet.");
			umsatzBeginn = 0;
			return umsatzBeginn;
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
		String s2 []= {ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		try {
			s=this.set_StringPara(INPUTFILE_PARAMETER, ManagerLanguage.getString("VerbrennungsBeginnAutoDetect"),s2);
		} catch (ParameterFileWrongInputException e) {		
			e.stopBremo();
		}

		if(s.equalsIgnoreCase(ManagerLanguage.getString("ja"))) autodetect=true;

//				if(autodetect){
//					try{
//						throw new BirdBrainedProgrammerException("Die AutodetectMethode wurde noch nicht Programmiert");
//					}catch(BirdBrainedProgrammerException bbp){
//						bbp.stopBremo();
//					}
//				}		

		return autodetect;		

	}

	/** 
	   * Gibt an ob eine Verlustteilung durchgeführt werden soll. 
	   * Die Auswahl erfolgt durch den Bnutzer 
	   * 
	   * @return 
	   */ 
	//fuer Verlustteilung Frank Haertel  
	public boolean is_Verlustteilung(){
	    boolean verlustteilung = false; 
	    String s = null; 
	    String s2 []= {ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")}; 
    	try { 
    		s=this.set_StringPara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Verlustteilung"),s2);
    		if(s.equalsIgnoreCase(ManagerLanguage.getString("ja")))
    			verlustteilung=true;
    	} catch (ParameterFileWrongInputException e) { 
    		e.log_Warning("Der Parameter \""+ManagerLanguage.getString("Verlustteilung")+"\" wurde nicht gesetzt, es wird keine durchgeführt."); //Die DVA muss ja nicht gleich abbrechen.
    	} 
	    return verlustteilung;
	}
	
	
	public boolean is_pKGH_indiziert(){
		boolean pKGH_indiziert = false;
		String s = null;
		String[] s2 = {ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		try{
			s=this.set_StringPara(INPUTFILE_PARAMETER, "pKGHindiziert", s2);
			if(s.equalsIgnoreCase(ManagerLanguage.getString("ja")))
				pKGH_indiziert = true;
		}catch (ParameterFileWrongInputException e){
			e.stopBremo();
		}
		return pKGH_indiziert;
	}




	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void xxx_AB_HIER_ALLGEMEINE_BETRIEBS_PARAMETER(){}	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////


	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of CO2
	 */	
	public double get_iniMoleFrac_CO2(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_CO2"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of O2
	 */	
	public double get_iniMoleFrac_O2(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_O2"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	
	
	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of H2O
	 */	
	public double get_iniMoleFrac_H2O(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_H2O"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	
	
	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of N2
	 */	
	public double get_iniMoleFrac_N2(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_N2"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of iC8H18
	 */	
	public double get_iniMoleFrac_iC8H18(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_iC8H18"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of nC7H16
	 */	
	public double get_iniMoleFrac_nC7H16(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_nC7H16"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of H2
	 */
	public double get_iniMoleFrac_H2() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_H2"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** benötigt in APR_CanteraMultiZoneRCCI
	 * @return initial mole fraction of H
	 */
	public double get_iniMoleFrac_H() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_H"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_CO() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_CO"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_OH() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_OH"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_O() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_O"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_NO() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_NO"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_N() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iniMoleFrac_N"),"[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	
	
	/** Init-Wert des Drucks bei Rechnungsbeginn für die APR, falls kein Indizierfile vorhanden ist.
	 * @return initial pressure [Pa]
	 */	
	public double get_p_ini(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("p_Ini"),"["+ManagerLanguage.getString("Pa")+"]",0,Double.POSITIVE_INFINITY);
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
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_Ini"),"["+ManagerLanguage.getString("K")+"]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	public double get_turbKineticEnergy_Ini(){	
			try {
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("TurbKineticEnergy_Ini"),"["+ManagerLanguage.getString("m^2/s^2")+"]",0,Double.POSITIVE_INFINITY);
			} catch (ParameterFileWrongInputException e) {
				e.log_Warning("Für die turbulente kinetische Energie wurde kein Vorgabewert angegeben. Dies ist z.B. nötig, wenn nicht bei Einlassschluss initialisiert werden kann."); 
				e.stopBremo();
				//return -5.55;
				return Double.NaN;
			}		
	}
	
	public double get_C_MixRad() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("C_MixRad"),"[-]",0.0001,1000); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	
	public double get_C_Mix() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("C_Mix"),"[-]",0,50000); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	/**
	 * Returns the ratio air r.p.m./crankshaft r.p.m. (as in Kiva)
	 * @return swirl ratio [-]
	 */
	public double get_swirlRatio() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("swirlRatio"),"[-]",0,50); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	
	public int get_mixingProcess() {
		int a=-1;
		try {			
			
			a=(int)set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("mixingProcess"),"[-]",0,12); 
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
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Drehzahl"),"["+ManagerLanguage.getString("min^-1")+"]",0,Double.POSITIVE_INFINITY)/60;
		} catch (ParameterFileWrongInputException e) {
			String[] parameter = {ManagerLanguage.getString("Drehzahl"), ManagerLanguage.getString("min^-1")};
			String vorgabe = "";
			e.eingabeErforderlich(this, parameter, vorgabe);
			
			//Warte, bis du das Signal bekommst weiterzumachen
			synchronized (this) {
				try {wait();}catch(InterruptedException e1){}
			}
			try {
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Drehzahl"),"["+ManagerLanguage.getString("min^-1")+"]",0,Double.POSITIVE_INFINITY)/60;
			}catch(ParameterFileWrongInputException f){
				e.stopBremo();
				return Double.NaN;
			}
			
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
		MasterEinspritzung me=this.MASTER_EINSPRITZUNG;//zirkuläre Referenz ;-( naja mal schauen...
		double mKrst=me.get_mKrst_Sum_ASP();
		Spezies krst=me.get_spezKrstALL(); //liefert die Mischung aller Krafstoffe				
		
		double mFG=mLuft_tr+mW+mKrst;	//gesamte Masse des Verbrennenden Gases
		Hashtable<Spezies,Double>frischGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
		//trockene Luft
		frischGemisch_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezLuft_trocken(),mLuft_tr/mFG);
		//Wasser
		frischGemisch_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezH2O(),mW/mFG);
		//Kraftstoff
		if(krst!=null&& mKrst!=0)
			frischGemisch_MassenBruchHash.put(krst,mKrst/mFG);

		//Erstellen der Frischgemischspezies
		GasGemisch frischGemsich=new GasGemisch(ManagerLanguage.getString("Frischgemisch"));
		frischGemsich.set_Gasmischung_massenBruch(frischGemisch_MassenBruchHash);


		//Bestimmen der AGR-Zusammensetzung --> das Hinzufuegen von AGR hat keinen Einfluss auf die 
		//AGR-Zusammensetzung da die Anzahl der c/h/o-Atome gleich bleibt und nur vom Frischgemisch abhaengt
		GleichGewichtsRechner gg=this.OHC_SOLVER; //zirkuläre Referenz ;-( naja mal schauen...
		GasGemisch abgas=new GasGemisch(ManagerLanguage.getString("Abgas"));
		//eigentlich muesste die GLGW-Zusammensetzung aus der DVA kommen der Druck wuerde auf den Wert festgelegt, 
		//den er hat wenn 1600K in der verbrannten Zone vorliegen
		//-->die Druckabhängigikeit bei niedrigen Temperaturen ist aber so gering, dass der Druck hier keine Rolle spielt!
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
		double mAGRin=this.RESTGASMODELL.get_mInternesRestgas_ASP();//zirkuläre Referenz ;-( naja mal schauen...
		double mAGR=mAGRex+mAGRin;
		double mGes=mLuft_tr+mW+mAGR; //gesamte Masse im Zylinder (ohne Kraftstoff)
		Hashtable<Spezies,Double>verbrennungsLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		verbrennungsLuft_MassenBruchHash.put(abgas, mAGR/mGes);
		verbrennungsLuft_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezLuft_trocken(),mLuft_tr/mGes);		
		verbrennungsLuft_MassenBruchHash.put(this.SPEZIES_FABRIK.get_spezH2O(),mW/mGes);
		GasGemisch verbrennungsLuft=new GasGemisch(ManagerLanguage.getString("Verbrennungsluft"));
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
		double mAGRin=this.RESTGASMODELL.get_mInternesRestgas_ASP();//zirkuläre Referenz ;-( naja mal schauen...
		double mAGR=mAGRex+mAGRin;		

		return mLuft_tr+mW+mAGR;
	}


//	/**
//	 * Liefert die interne AGR-Rate bei Einlassschluss bestehend aus: </br>
//	 * AGR intern / (trockene Luft + Wasser); Kraftstoff wird dabei nicht </br>
//	 * berücksichtigt.
//	 * @return double AGR-Rate
//	 */
//	public double get_interne_AGR_Rate(){
//		CasePara cp=this; //wenn die funktion mal wo anders stehen soll.....
//		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
//		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
//		double mAGRin=this.RESTGASMODELL.get_mInternesRestgas_ASP();//zirkuläre Referenz ;-( naja mal schauen...	
//
//		return mAGRin/(mLuft_tr+mW);  //TODO Kraftstoffmasse aus Einspritzung bei Einlassschluss beruecksichtigen
//	}


	/*** 
	 * @return liefert  die externe AGR-Masse pro ASP in [kg]
	 */	
	public double get_mAGR_extern_ASP(){
		try {
			return this.convert_ProStunde_2_ProASP(
					set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("mAGR_extern"),"["+ManagerLanguage.getString("kg/h")+"]",0,Double.POSITIVE_INFINITY));
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
		// ob eine LWA vorliegt und ggf der mit der LWA berechnete Wert zurückgegeben werden! 
		//Eine Abfrage über das Restgasmodell mach eher weniger sinnn
		//Dies gilt für alle Eingabegrößen die von der LWA ueberschrieben werden muessen....
		//Alternativ denkbar ist auch die Uebergabe aller Werte aus der LWA direkt in die Berechnungsmodule
		try {
			double mAGR;
			String[] einheiten = new String[2];
			einheiten[0] = "["+ManagerLanguage.getString("kg")+"]";
			einheiten[1] = "["+ManagerLanguage.getString("%")+"]";
			double agr = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("mAGR_intern_ASP"),einheiten,0,Double.POSITIVE_INFINITY);
			if(INPUTFILE_PARAMETER.get(ManagerLanguage.getString("mAGR_intern_ASP"))[1].equalsIgnoreCase("["+ManagerLanguage.getString("%")+"]")){
				mAGR = agr / (100-agr) * (this.get_mAGR_extern_ASP() + this.get_mLuft_feucht_ASP() + this.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP());
			}else{
				mAGR = agr;
			}
			return mAGR;
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
					set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("mLuft_feucht"),"["+ManagerLanguage.getString("kg/h")+"]",1e-6,Double.POSITIVE_INFINITY));
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
			phi= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("relative_Luftfeuchte"),"["+ManagerLanguage.getString("%")+"]",0,100);

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
		MasterEinspritzung me=this.MASTER_EINSPRITZUNG;//zirkuläre Referenz ;-( naja mal schauen...
		double mKrst=me.get_mKrst_Sum_ASP();
		Spezies krst=me.get_spezKrstALL(); //liefert die Mischung aller Krafstoffe			
		double Lst=krst.get_Lst();	
		return this.get_mLuft_trocken_ASP()/mKrst/Lst;
	}



	/** 
	 * @return liefert den Zündzeitpunkt in [s] nach Rechnungsbeginn 
	 */	
	public double get_ZZP(){
		double zzp;		
		try {
			if(SYS.IS_KW_BASIERT){				
				zzp =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("ZZP"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				zzp=convert_KW2SEC(zzp);
			}else {				
				zzp =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("ZZP"),"["+ManagerLanguage.getString("s")+"]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return zzp;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_pmi(){
		double std_pmi=-7.77;	//Standardwert für pmi (muss negativ sein, siehe z.B. das Wandwärmemodell "WoschniHuber"
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("pmi"),"["+ManagerLanguage.getString("bar")+"]",0,30); 
		} catch (ParameterFileWrongInputException e) {				
			e.stopBremo();
			return std_pmi;
		}
	}	

	//fuer Verlustteilung Frank Haertel
	public double get_pme(){ 
	    double std_pme=0; 
	    try { 
	      return set_doublePara(INPUTFILE_PARAMETER,ManagerLanguage.getString("pme"),"["+ManagerLanguage.getString("bar")+"]",0,30); 
	    } catch (ParameterFileWrongInputException e) { 
	      e.log_Warning("Keiner oder der falsche pme-Wert wurde angegeben. "+ 
	          "Dieser wird nicht in der Verlustteilung angezeigt"); 
	      return std_pme; 
	    } 
	  } 	
	
	/**
	 * Mehrfacheinspritzungen sind möglich --> über den Parameter  wird die Anzahl der Einspritzungen vorgegeben \br
	 * Wird dieser Parameter nicht angegeben, wird er auf eins gesetzt und eine Warnung wird ins LogFile geschrieben
	 * @param  
	 * @return liefert den Einspritzbeginn in [s] nach Rechnungsbeginn 
	 */	
	public int get_AnzahlEinspritzungen(){		
		try {
			return (int) set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("anzahlEinspritzungen"),"",0,Double.POSITIVE_INFINITY);
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
	public int get_indexOfMainInj(){		
		try {
			return (int) set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("indexOfMainInj"),"",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning();
			return -1;
		}		
	}

	/**
	 * Mehrfacheinspritzungen sind möglich --> über den Parameter  wird die Nummer der Einspritzung vorgegeben
	 * @param  
	 * @return liefert den Einspritzbeginn in [s nach Rechnungsbeginn]
	 */	
	public double get_BOI(int idxOfInjection){
		String  suchString;
		int anzEinspr=get_AnzahlEinspritzungen();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht den Einspritzbeginn der " +idxOfInjection 
						+"ten Einspritzung abzufragen. " +
						"\n Laut InputFile gibt es aber nur " +anzEinspr + " Einspritzungen!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}

		if (anzEinspr==1){
			suchString= ManagerLanguage.getString("BOI");
		}else{
			suchString= ManagerLanguage.getString("BOI_")+idxOfInjection;
		}
		try {
			if(SYS.IS_KW_BASIERT){	
				double boi;
				boi =set_doublePara(INPUTFILE_PARAMETER, suchString,"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.KW_UNTERGRENZE,SYS.KW_OBERGRENZE);
				return convert_KW2SEC(boi);				
			}else {					
				return set_doublePara(INPUTFILE_PARAMETER, suchString,"["+ManagerLanguage.getString("s")+"]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
			}

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	

	/**
	 * Da Mehrfacheinspritzungen sind möglich --> über den Parameter @param  
	 * wird die Nummer der Einspritzung vorgegeben
	 * @param int i 
	 * @return liefert das Einspritzende in [s] nach Rechnungsbeginn 
	 */	
	public double get_EOI(int idxOfInjection){
		String suchString;
		int anzEinspr=get_AnzahlEinspritzungen();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht den Einspritzbeginn der " +idxOfInjection 
						+"ten Einspritzung abzufragen. " +
						"\n Laut InputFile gibt es aber nur " +anzEinspr + " Einspritzungen!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}		

		if (anzEinspr==1){
			suchString= ManagerLanguage.getString("EOI");
		}else{
			suchString= ManagerLanguage.getString("EOI_")+idxOfInjection;
		}
		try {
			if(SYS.IS_KW_BASIERT){			
				double eoi;
				eoi =set_doublePara(INPUTFILE_PARAMETER, suchString,"["+ManagerLanguage.getString("KWnZOT")+"]",convert_SEC2KW(get_BOI(idxOfInjection)),get_Auslassoeffnet_KW());
				return convert_KW2SEC(eoi);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, suchString,"["+ManagerLanguage.getString("s")+"]",get_BOI(idxOfInjection),SYS.SEC_OBERGRENZE);
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
	public int get_InjZone(int idxOfInjection){
		String  suchString;
		int anzEinspr=get_AnzahlEinspritzungen();		
		int injZone=-1;
		if (anzEinspr==1){
			suchString= ManagerLanguage.getString("InjZone");
		}else{
			suchString= ManagerLanguage.getString("InjZone_")+idxOfInjection;
		}

		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht den Parameter " + suchString + 
						" der " +idxOfInjection 
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

	public Spezies get_kraftsoff(int idxOfInjection){
		int anzEinspr=this.get_AnzahlEinspritzungen();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection index ("+idxOfInjection+") " +
						"you asked the fuel for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		
		String krstFlag;
		if(anzEinspr==1){
			krstFlag= ManagerLanguage.getString("krst");	
		}else{
			krstFlag= ManagerLanguage.getString("krst_")+idxOfInjection;
		}		

		//Die Koeffizienten Speziesfabrik liefert eine hashtable mit allen moeglichen Krafstoffen
		Hashtable<String,Spezies> krstHash=this.SPEZIES_FABRIK.get_alleKrafstoffe();
		String moeglicheFlags []=new String[krstHash.size()+2];
		//Die Kraftsoffapproximationsspezies ist nicht in der Hashtable enthalten
		moeglicheFlags[0]=  ManagerLanguage.getString("ohc");  //jetzt schon ;-)
		//Wasser kann man ja auch einspritzen
		moeglicheFlags[1]=  ManagerLanguage.getString("Wasser");  //jetzt schon ;-)

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
			krstHash.put(this.SPEZIES_FABRIK.get_spezGrill().get_name(), this.SPEZIES_FABRIK.get_spezGrill());
		}else {}
			return krstHash.get(krstAngabe); //die Fehlerabfrage erfolgt bereits innerhalb der Methode 
		//"set_StringPara" da der hier uebergebene Vektor alle moeglichen krstAngaben enthaelt.

	}	

	/**
	 * returns the mass of fuel in [kg/cycle] for the injection 
	 * injection with the index: idxOfInjection
	 * @param idxOfInjection
	 * @return
	 */
	public double get_mKrst_ASP(int idxOfInjection){
		int anzEinspr=this.get_AnzahlEinspritzungen();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection index ("+idxOfInjection+") " +
						"you asked the mass of injected fuel for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String krstFlag;
		if(anzEinspr==1){
			krstFlag= ManagerLanguage.getString("mKrst");	
		}else{
			krstFlag= ManagerLanguage.getString("mKrst_")+idxOfInjection;
		}		

		try {
			return this.convert_ProStunde_2_ProASP(
					set_doublePara(INPUTFILE_PARAMETER, krstFlag,"["+ManagerLanguage.getString("kg/h")+"]",0,Double.POSITIVE_INFINITY));
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/**
	 * returns the specific evaporation time in [s] for the injection 
	 * injection with the index: idxOfInjection
	 * @param idxOfInjection
	 * @return time
	 */
	public double get_tau(int idxOfInjection){
		int anzEinspr=this.get_AnzahlEinspritzungen();
		double tau;
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection index ("
					+idxOfInjection+") " +"you asked the specific evaporation "
							+ "time for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String tauFlag;
		if(anzEinspr==1){
			tauFlag= ManagerLanguage.getString("tau");	
		}else{
			tauFlag= ManagerLanguage.getString("tau_")+idxOfInjection;
		}		

		try {
			tau =  this.set_doublePara(INPUTFILE_PARAMETER, tauFlag,"["+ManagerLanguage.getString("s")+"]",0,
					Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Es wurde im Parameterfile keine spezifische "
					+ "Verdampfungszeit tau fuer die Einspritzung "
					+idxOfInjection+ " angegeben. Der Wert wurde auf 0.5 ms gesetzt");
			tau =  0.5*Math.pow(10,-3);
		}	
		return tau;
	}


	/**
	 * Liefert die Anzahl axialer Pakete bei einem Einspritzmodell mit Paketansatz 
	 * (z.B. Hiroyasu)
	 * @param idxOfInjection
	 * @return
	 */
	public int get_anzAxialPakete(int idxOfInjection) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection index ("+idxOfInjection+") " +
						"you asked the nbrOfAxialPackets for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("anzAxialPakete");	
		}else{
			flag= ManagerLanguage.getString("anzAxialPakete_")+idxOfInjection;
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
	 * @param nbrOfInjection --> This is the index +1
	 * @return
	 */
	public int get_anzRadialPakete(int idxOfInjection) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection index ("+idxOfInjection+") " +
						"you asked the nbrOfRadialPackets for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("anzRadialPakete");	
		}else{
			flag= ManagerLanguage.getString("anzRadialPakete_")+idxOfInjection;
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
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the injection pressure for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("einspritzDruck");	
		}else{
			flag= ManagerLanguage.getString("einspritzDruck_")+einspritzungsNr;
		}			
		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"["+ManagerLanguage.getString("Pa")+"]",0,Double.MAX_VALUE);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}	



	public double get_anzSPL(int einspritzungsNr) {
		int anzEinspr=this.get_AnzahlEinspritzungen();
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the nbr of injector holes for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("anzSpritzloecher");	
		}else{
			flag= ManagerLanguage.getString("anzSpritzloecher_")+einspritzungsNr;
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
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the cd-factor for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("cdFaktor");	
		}else{
			flag= ManagerLanguage.getString("cdFaktor_")+einspritzungsNr;
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
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the entrainment factor for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("EntrainmentFaktor");	
		}else{
			flag= ManagerLanguage.getString("EntrainmentFaktor_")+einspritzungsNr;
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
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the profile factor for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag= ManagerLanguage.getString("ProfilFaktor");	
		}else{
			flag= ManagerLanguage.getString("ProfilFaktor_")+einspritzungsNr;
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
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the  injector hole diameter for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag=ManagerLanguage.getString("durchmSpritzloch");	
		}else{
			flag=ManagerLanguage.getString("durchmSpritzloch_")+einspritzungsNr;
		}			
		try {
			return 	set_doublePara(INPUTFILE_PARAMETER, flag,"["+ManagerLanguage.getString("m")+"]",0.00001,0.001);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}



	public String get_vergleichsKrstVerdampfung(int einspritzungsNr) {		
		int anzEinspr=this.get_AnzahlEinspritzungen();
		if(einspritzungsNr>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("The injection with index ("+einspritzungsNr+") " +
						"you asked the reference fuel for evaporation for does not exist");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}
		}
		String flag;
		if(anzEinspr==1){
			flag=ManagerLanguage.getString("vergleichsKrstVerdampfung");	
		}else{
			flag=ManagerLanguage.getString("vergleichsKrstVerdampfung_")+einspritzungsNr;
		}			
		try {
			return this.set_StringPara(INPUTFILE_PARAMETER, flag, Kraftstoff_Eigenschaften.kraftstoffe);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return null;
		}			

	}



	/** 
	 * @return liefert die Drallgeschwindigkeit  in [m/s]
	 */	
	public double get_DrallGeschwindigkeit(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("drallgeschwindigkeit"),"["+ManagerLanguage.getString("m/s")+"]",0,Double.POSITIVE_INFINITY);
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
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Hu_RON_91"),"["+ManagerLanguage.getString("MJ/mol")+"]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \""+ManagerLanguage.getString("RON_91")+"\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu + "["+ManagerLanguage.getString("J/mol")+"]");
			return stdHu;
		}		
	}



	/** 
	 * @return liefert den Heizwert fuer RON95 [J/mol]
	 */	
	public double get_Hu_RON_95(){
		double stdHu=4211277.132508449; //4.247052132508449*1e6; //berechnet  passend zu den Koeffizienten

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Hu_RON_95"),"["+ManagerLanguage.getString("MJ/mol")+"]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \""+ManagerLanguage.getString("RON_95")+"\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu + "["+ManagerLanguage.getString("J/mol")+"]");
			return stdHu;
		}		
	}

	/** 
	 * @return liefert den Heizwert fuer RON98 [J/mol]
	 */
	public double get_Hu_RON_98(){
		double stdHu=4108620.6027614996;//4.1520586027615*1e6; //gerechnet passend zu den Koeffizienten		

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Hu_RON_98"),"["+ManagerLanguage.getString("MJ/mol")+"]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \""+ManagerLanguage.getString("RON_98")+"\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu +  "["+ManagerLanguage.getString("J/mol")+"]");			
			return stdHu;
		}		
	}

	/** 
	 * @return liefert den Heizwert fuer Diesel [J/mol]
	 */	
	public double get_Hu_Diesel(){
		double stdHu=7849471.20749425;//7.803475207494249*1e6; //berechnet passend zu den Koeffizienten

		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Hu_Diesel"),"["+ManagerLanguage.getString("MJ/mol")+"]",0,Double.NaN); 
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Fuer den ausgewaehlten Kraftstoff \""+ManagerLanguage.getString("Diesel")+"\" wurde kein Heizwert angegeben. \n" +
					"Es wird mit dem Standardwert gerechnet: " +stdHu + "["+ManagerLanguage.getString("J/mol")+"]");
			return stdHu;
		}		
	}
	

	/** 
	 * @return liefert den Heizwert fuer eine Krstoffapproximationsspezies [J/mol]
	 */	
	public double get_Hu_ohc(){

		double stdHu=Double.NaN; //TODO Heizwert angeben
		try {
			return 1e6*set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Hu_ohc"),"["+ManagerLanguage.getString("MJ/mol")+"]",0,Double.NaN); 
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
			lhvCorr= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("lhvCorr"),"[-]",0,1); 
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
			whtfCorr= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("whtfMult"),"[-]",0,Double.MAX_VALUE); 
			return whtfCorr;
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning("Der Wert fuer \""+ManagerLanguage.getString("whtfMult")+"\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird ohne Multiplikator für die Anpassung des Wandwaermeverlustes gerechnet!");	
			return whtfCorr;
		}					
	}
	
	/**
	 * Returns a factor to multiply the wall heat transfer during combustion.
	 * e.g. for knocking, hcci
	 * @return
	 */
	public double get_whtfMult_burn(){
		double whtfCorr=1;
		try {
			whtfCorr= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("whtfMult_burn"),"[-]",0,Double.MAX_VALUE); 
			return whtfCorr;
		} catch (ParameterFileWrongInputException e) {	
			return whtfCorr;
		}
	}
	
	/**
	 * Returns a duration in CA when multiplier whtfMult_burn is active
	 * e.g. for knocking, hcci
	 * @return
	 */
	public double get_whtfMult_burn_duration(){
		double whtfDur=this.convert_KW2SEC(this.SYS.RECHNUNGS_ENDE_DVA_KW+this.SYS.RECHNUNGS_BEGINN_DVA_KW);
		//double whtfDur=this.convert_KW2SEC(50+this.SYS.RECHNUNGS_BEGINN_DVA_KW);
		try {
			whtfDur= this.convert_KW2SEC(set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("whtfMult_dauer"),"["+ManagerLanguage.getString("KW")+"]",0,180)+this.SYS.RECHNUNGS_BEGINN_DVA_KW); 
			return whtfDur;
		} catch (ParameterFileWrongInputException e) {
			System.err.println("**************************************************");
			System.err.println("ACHTUNG: \"whtfMult_dauer\" wird mit " + convert_ProKW_2_ProSEC(whtfDur) + " ["+ManagerLanguage.getString("KWnZOT")+"] angenommen!");
			System.err.println("**************************************************");
			return whtfDur;
		}
	}
		
	/**
	 * Returns the polytropic exponent needed for WHTF models like Woschni or Chang (this method 
	 * is only called when the simulation does not start at IVC)
	 * @return polyExp_WHT [-]
	 */
	public double get_polyExp_WHT(){
		double polyExp_WHT=-5;
		try {
			polyExp_WHT= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("polyExp_WHT"),"[-]",1,1.4); 
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();	
		}					
		return polyExp_WHT;
	}


	/** 
	 * @return liefert einen Vektor mit den o/h/c-Atomen fuer eine Krstoffapproximationsspezies 
	 */	
	public double [] get_krstApprox_ohc(){
		double ohc[]=new double [4];
		try {			
			ohc[0]= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("krst_oAtome"),"[-]",0,Double.POSITIVE_INFINITY); 
			ohc[1]= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("krst_hAtome"),"[-]",0,Double.POSITIVE_INFINITY);
			ohc[2]= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("krst_cAtome"),"[-]",0,Double.POSITIVE_INFINITY);
			ohc[3]= set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("krst_nAtome"),"[-]",0,Double.POSITIVE_INFINITY);
			return ohc;
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			ohc[0]=ohc[1]=ohc[2]=Double.NaN;
			return ohc;
		}		
	}
	
	/**
	 * @return liefert den Wert, der für den Zylinderdruckabgleich mit der referenzMethode verwendet werden soll
	 */
	public double get_Referenzwert(){
//		double std_rw=1.0;	//Standardwert für Referenzwert ca. Umgebungsdruck
//		try {
//			return set_doublePara(INPUTFILE_PARAMETER, "Referenzwert","["+ManagerLanguage.getString("bar")+"]",0,300); 
//		} catch (ParameterFileWrongInputException e) {			
//			e.log_Warning("Fuer die referenzMethode wurde kein Referenzwert angegeben. \n" +
//					"Es wird mit dem Referenzwert" +std_rw + "[bar] gerechnet.");
//			return std_rw;
//		}
		double std_rw=-7.77;	//Standardwert muss negativ
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("referenzWert"),"["+ManagerLanguage.getString("bar")+"]",0,300); 
		} catch (ParameterFileWrongInputException e) {				
			e.stopBremo();
			return std_rw;
		}	
	
	}
	
	
	/**
	 * @return liefert den Kanal, der für den Zylinderdruckabgleich mit der kanalMethode verwendet werden soll
	 */
	public boolean get_Referenzkanal(){
		String [] yesno={ManagerLanguage.getString("pEin"),ManagerLanguage.getString("pAbg")};
		boolean pIn=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"Referenzkanal",yesno ).equalsIgnoreCase(ManagerLanguage.getString("pEin")))
				pIn=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return pIn;
	}

	/**
	 * @return liefert den Zeitpunkt, bei dem der Zylinderdruckabgleich beginnen soll [s n. Rechenbegin]
	 */
	public double get_DruckabgleichBeginn(){
		double KW_beginnAbgleich;		
		try {
			if(SYS.IS_KW_BASIERT){
				//KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("KW_Beginn_Druckabgleich"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("KW_Beginn_Druckabgleich"),"["+ManagerLanguage.getString("KWnZOT")+"]",-720,720);
				KW_beginnAbgleich=convert_KW2SEC(KW_beginnAbgleich);
			}else {				
				KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("KW_Beginn_Druckabgleich"),"["+ManagerLanguage.getString("s")+"]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return KW_beginnAbgleich;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}	
	}
	/**
	 * @return liefert den Zeitpunkt, bei dem der Zylinderdruckabgleich aufhören soll [s n. Rechenbeginn]
	 */
	public double get_DruckabgleichEnde(){
		double KW_endeAbgleich;		
		try {
			if(SYS.IS_KW_BASIERT){
				//KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Ende_Druckabgleich","["+ManagerLanguage.getString("KWnZOT")+"]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("KW_Ende_Druckabgleich"),"["+ManagerLanguage.getString("KWnZOT")+"]",-720,720);
				KW_endeAbgleich=convert_KW2SEC(KW_endeAbgleich);
			}else {				
				KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("KW_Ende_Druckabgleich"),"["+ManagerLanguage.getString("s")+"]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
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
			kappa_Druckabgleich =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("kappa_Druckabgleich"),"[-]",1,1.4);
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
			hc =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("HC"),"["+ManagerLanguage.getString("ppm")+"]",0,Double.MAX_VALUE);
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
			co =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("CO"),"["+ManagerLanguage.getString("ppm")+"]",0,Double.MAX_VALUE);
			return co*1e-6;				
		} catch (ParameterFileWrongInputException e) {			
			return 0;
		}
	}
	
	/**
	 * Liefert den Volumenbruch der C-Emissionen
	 * <br>Wird bei der Berechnung der Wärmemenge der unverbrannten Abgasbestandteile gebraucht
	 * @return C oder 0 wenn im inputFile nicht angegeben
	 */
	public double get_C() {
		double c;		
		try {
			c =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("C"),"["+ManagerLanguage.getString("ppm")+"]",0,Double.MAX_VALUE);
			return c*1e-6;				
		} catch (ParameterFileWrongInputException e) {			
			return 0;
		}
	}
	
	/**
	 * Liefert den Volumenbruch der H2-Emissionen
	 * <brWird bei der Berechnung der Wärmemenge der unverbrannten Abgasbestandteile gebraucht
	 * @return H2 oder 0 wenn im inputFile nicht angegeben
	 */
	public double get_H2() {
		double h2;		
		try {
			h2 =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("H2"),"["+ManagerLanguage.getString("ppm")+"]",0,Double.MAX_VALUE);
			return h2*1e-6;				
		} catch (ParameterFileWrongInputException e) {			
			return 0;
		}
	}
	
	
	public String get_iterativeMethode(String[] mglMethoden){
		try{
			return set_StringPara(INPUTFILE_PARAMETER, ManagerLanguage.getString("iterativeMethode"), mglMethoden);
		}catch(ParameterFileWrongInputException e) {			
			return ManagerLanguage.getString("ohne");
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
private void Werte_und_Variablen_zur_Berechnung_des_WWÜ_Bargende(){}
////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Vorfaktor für Startwert k_esInit im k-epsilon Modell (Bargende)
	 * kann an jeweilige Motorgeometrie angepasst werden -> 
	 * siehe Noske G.: "Ein Quasidimensionales Modell..."
	 * Werte zwischen 0,01 und 0,6 üblich
	**/
	public double get_Konstante_TKE(){
		double konstante_TKE;
		try {
			konstante_TKE = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("c_k"),"[-]",0,1);
			return konstante_TKE;
		} catch (ParameterFileWrongInputException e) {		
			e.log_Warning("Konstante "+ManagerLanguage.getString("c_k")+" für Vorfaktor des Turbulenz-Startwerts fehlt. "
					+ "Es wird mit Default Wert von 0.125 gerechnet.");	
			return 0.125;
		}		
	}
	
	/**
	 * Anzahl der Einlassventile wird für die Berechnung des Turbulenz-Startwerts benötigt
	**/
	public double get_AnzahlEinlassventile(){
		double anz_EV;
		try {
			anz_EV = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("n_EV"),"[-]",1,10);
			return anz_EV;
		} catch (ParameterFileWrongInputException e) {		
			e.log_Warning("Konstante "+ManagerLanguage.getString("n_EV")+" zur Berechnung des Turbulenz-Startwerts fehlt. "
					+ "Es werden 2 Einlassventile angenommen.");
			return 2;
		}		
	}

	/**
	 *kappa ist im FVV-Zylindermodul konstant! Für Vergleichbarkeit wird dies auch hier ermöglicht 
	 **/
	public double get_Kappa_Bargende() {
		double kappa_B;
		try {
			kappa_B = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("kappa_Bargende"),"[-]",1,1.7);
			return kappa_B;
		} catch (ParameterFileWrongInputException e) {		
			e.log_Warning(ManagerLanguage.getString("kappa_Bargende")+" zur Berechnung des WWÜ nach BargendeFVV fehlt. "
					+ "Es wird mit dem von Bremo ermittelten kappa gerechnet.");	
			return -5.55;
		}		
	}
	
	/**
	*Konstante zum wechseln zwischen konservativem Bargende und BargendeFVV
	*mit 3 wird das Verfahren nach Grill verwendet, mit 1 original nach Bargende 
	**/
	public double get_W_k() {
		double w_k;
		try {
			w_k = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("w_k"),"[-]",1,1);
			return w_k;
		} catch (ParameterFileWrongInputException e) {		
			e.log_Warning("Kein "+ManagerLanguage.getString("w_k")+" angegeben! Für w_k = 1 wird konservativ nach Bargende gerechnet. "
					+ "Sonst wird die Modifikation von Grill verwendet");
			return 3;
		}		
	}
	
	/**
	 * Multiplikator für den Verbrennungsterm
	 **/
	public double get_Delta_k() {
		double delta_k;
		try {
			delta_k = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("delta_k"),"[-]",0,3);
			return delta_k;
		} catch (ParameterFileWrongInputException e) {		
			e.log_Warning("Konstante "+ManagerLanguage.getString("delta_k")+" als Multiplikator für Verbrennungsterm nicht angegeben. "
					+ "Es wird mit Default Wert von 1 gerechnet.");	
			return 1;
		}		
	}
	
	/**
	 *Der Liefergrad soll vom Anwender vorgegeben werden können. 
	 *Standardmäßig wird mit einem Liefergrad von 1 gerechnet. s. SAE Paper 2006-01-1107
	 **/
	public double get_Liefergrad(){
		double lambda_L;
		try {
			lambda_L = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Liefergrad"), "[-]", 0,1);
			return lambda_L;
		}catch (ParameterFileWrongInputException e) {		
			e.log_Warning("Es wurde kein "+ManagerLanguage.getString("Liefergrad")+" für die Berechnung des Turbulenzstartwerts angegeben."
					+ " Der Liefergrad wird als 1 angenommen.");	
			return 1;
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
	 * Returns the given temperature at IVC (Intake Valve Closing)
	 * (needed for WHT models like Woschni if simulation 
	 * does not start at IVC)
	 * @return T_IVC_WHT [K]
	 */
	public double get_T_IVC_WHT(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_IVC_WHT"),"["+ManagerLanguage.getString("K")+"]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Temperatur der Ladeluft in [K] zurück
	 */
	public double get_T_LadeLuft(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_Ladeluft"),"["+ManagerLanguage.getString("K")+"]",250,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Temperatur der Ladeluft in [K] zurück
	 */
	public double get_T_Abgas(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_Abgas"),"["+ManagerLanguage.getString("K")+"]",290,2000); //früher T_min 400, ist aber zu wenig wegen Schleppmessungen
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_T_FeuchteMessung(){

		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_FeuchteMessung"),"["+ManagerLanguage.getString("K")+"]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * Returns the temperature of the cylinder wall
	 * @return T_Cyl [K]
	 */
	public double get_T_Cyl(){	
		double T_Wand=-1e100;
		if(T_Wand==-1e100){
			try {
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_Cyl"),"["+ManagerLanguage.getString("K")+"]",1e-6,1000);//da macht wohl jeder Stahl schlapp
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
				return Double.NaN;
			}
		}else{
			return T_Wand;
		}
	}
	
	
	/** 
	 * Returns the temperature of the piston
	 * @return T_pist [K]
	 */
	public double get_T_Piston(){	
		double T_pist=-1e100;
		if(T_pist==-1e100){
			try {
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_Piston"),"["+ManagerLanguage.getString("K")+"]",1e-6,1000);//da macht wohl jeder Stahl schlapp
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
				return Double.NaN;
			}
		}else{
			return T_pist;
		}
	}
	
	/** 
	 * Returns the temperature of the cylinder head
	 * @return T_head [K]
	 */
	public double get_T_Head(){	
		double T_head=-1e100;
		if(T_head==-1e100){
			try {
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_Head"),"["+ManagerLanguage.getString("K")+"]",1e-6,1000);//da macht wohl jeder Stahl schlapp
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
				return Double.NaN;
			}
		}else{
			return T_head;
		}
	}
	
	

	/**
	 * Liefert die Temperatur des FLÜSSIGEN Kraftstoffs	
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
			suchString=ManagerLanguage.getString("T_Krst_fluessig");
		}else{
			suchString=ManagerLanguage.getString("T_Krst_fluessig_")+i;
		}
		try {
			return set_doublePara(INPUTFILE_PARAMETER, suchString,"["+ManagerLanguage.getString("K")+"]",-273.15,Double.POSITIVE_INFINITY);			

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
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("p_Ladeluft"),"["+ManagerLanguage.getString("Pa")+"]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/**
	 * @return liefert den Kurbelgehäusedruck aus dem Inputfile
	 */
	public double get_pKGH(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("p_KGH"), "["+ManagerLanguage.getString("Pa")+"]", 0, Double.POSITIVE_INFINITY);
		}catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}
	}
	
	/**
	 * Returns the pressure at IVC 
	 * (needed for WHT models like Woschni if simulation 
	 * does not start at IVC)
	 * @return p_IVC [N/m^2]
	 */
	public double get_p_IVC_WHT() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("p_IVC"),"["+ManagerLanguage.getString("Pa")+"]",0,Double.POSITIVE_INFINITY);
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
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("p_Abgas"),"["+ManagerLanguage.getString("Pa")+"]",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}
	}

	public double get_p_FeuchteMessung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("p_FeuchteMessung"),"["+ManagerLanguage.getString("Pa")+"]",0,Double.POSITIVE_INFINITY);
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
	 * @return liefert den Referenzpunkt [°KW nZOT], bei dem der Kappa Wert für das 
	 * WoschniHuber-Wandwärmemodell bzw. Changs Modell gerechnet wird. Hensels Modell
	 * verwendet den Punkt auch.
	 */
	public double get_refPunkt_WoschniHuber(){
		double KW_refPunkt;		
		try {
			if(SYS.IS_KW_BASIERT){				
				KW_refPunkt =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("refPunktWoschniHuber"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.RECHNUNGS_BEGINN_DVA_KW,SYS.RECHNUNGS_ENDE_DVA_KW);
				KW_refPunkt=convert_KW2SEC(KW_refPunkt);
			}else {				
				KW_refPunkt =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("refPunktWoschniHuber"),"["+ManagerLanguage.getString("s")+"]",SYS.RECHNUNGS_BEGINN_DVA_SEC,SYS.RECHNUNGS_ENDE_DVA_SEC);
			}				
			return KW_refPunkt;				
		} catch (ParameterFileWrongInputException e) {			
//			e.stopBremo();
//			return Double.NaN;
			e.log_Warning("Der Parameter \""+ManagerLanguage.getString("refPunktWoschniHuber")+"\" wurde nicht angegeben, es wird mit dem Parameter \"Einlassschluss\" gerechnet.");
			return get_Einlassschluss();
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
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("DruckKammerVolumen"),"["+ManagerLanguage.getString("m^3")+"]",1e-10,Double.MAX_VALUE); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Pleuellaenge in [m] zurück.	 * 
	 * */
	public double get_Pleuellaenge(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Pleuellaenge"),"["+ManagerLanguage.getString("m")+"]",0.01,10); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt den Bohrungsdurchmesser in [m] zurück.	 * 
	 * */
	public double get_Bohrung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Bohrung"),"["+ManagerLanguage.getString("m")+"]",0.005,2); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}	
	
	/**
	 * @return Gibt die BlowBy-Fläche in [m^2] zurück. Überlicherweise 0.002 bis 0.004 * Bohrung [m^2] zurück.
	 */
	public double get_FlaecheBB() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("FlaecheBB"),"["+ManagerLanguage.getString("m^2")+"]",0,0.01*get_Bohrung()); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}
	}


	/** 
	 * @return Gibt den Kurbelradius in [m] zurück.	Dieser ist nicht unbedingt
	 * gleich 0.5xHub, z.B. bei einer Desachsierung.* 
	 * */
	public double get_Kurbelradius() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Kurbelradius"),"["+ManagerLanguage.getString("m")+"]",0.0025,Double.POSITIVE_INFINITY); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	/**
	 * Mit dieser Methode wird ausgewählt, ob in den Indizierdaten der ZOT bei 0°Kurbelwellenwinkel angegeben ist.
	 * Wird der Parameter nicht gewählt, wird davon ausgegangen, dass der ZOT der Indizierdaten 
	 * bei 0°KW (senkrechter Stellung des Hubzapfens) liegt. Dies spielt bei Desachsierung/Schraenkung eine Rolle. 
	 */
	public boolean get_ZOTbeiKolbenOT(){		
		boolean ZOTbeiKolbenOT = false;
		String s = null;
		String s2 []= {ManagerLanguage.getString("ja"),ManagerLanguage.getString("nein")};
		try {
			s=this.set_StringPara(INPUTFILE_PARAMETER, "0KWbeiKolbenOT",s2);
		} catch (ParameterFileWrongInputException e){	
			ZOTbeiKolbenOT=false;
			if(this.get_Desachsierung()!=0|this.get_Schraenkung()!=0){
			e.log_Warning("Der Motor hat einen geschraenkten oder desachsierten Kurbeltrieb. "
					+ "Es wird davon ausgegangen, dass der Zeitpunkt \"0 [KWnZOT]\" der Indizierdaten "
					+ "bei 0° Kurbelwellenwinkel (Hubzapfen in Zylinderachse) liegt. "
					+ "Andernfalls \"0KWbeiKolbenOT [KWnZOT] := ja\" für automatische Korrektur setzen!");
			}
			s="nein";
		}
		if(s.equalsIgnoreCase("ja"))
			ZOTbeiKolbenOT=true;
		return ZOTbeiKolbenOT;		
	}

	/** 
	 * @return Gibt den OT-Versatz zurück um die Zeitachse der Indizierdaten 
	 * relativ zum Druckverlauf zu verschieben. Wird der Parameter nicht gewählt, 
	 * wird davon ausgegangen, dass der Zeitpunkt 0 [KWnZOT] der Indizierdaten bei 
	 * 0° Kurbelwellenwinkel (Hubzapfen in Zylinderachse) liegt.
	 * */
	public double get_OT_Versatz(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("OT_Versatz"),"["+ManagerLanguage.getString("KWnZOT")+"]",-360,360); 
		} catch (ParameterFileWrongInputException e) {
			return 0;
		}		
	}

	/** 
	 * @return Gibt die Verdichtung zurück.
	 * Die Verdichtung muss zwischen 1 und 30 liegen
	 * */
	public double get_Verdichtung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Verdichtung"),"",1,30); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return Gibt die Kolbenflaeche in [m^2]zurück.
	 * */
	public double get_pistonArea(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Kolbenflaeche"),"["+ManagerLanguage.getString("m^2")+"]",0.00002,5); //aus minimaler Bohrung und aus maximaler Bohrung berechnet
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/** 
	 * @return Gibt die Kolbenflaeche in [m^2]zurück.
	 * */
	public double get_fireDeckArea(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Brennraumdachflaeche"),"["+ManagerLanguage.getString("m^2")+"]",0.00002,5); //aus minimaler Bohrung und aus maximaler Bohrung berechnet
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Feuersteghoehe in [m]zurück.
	 * */
	public double get_Feuersteghoehe(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Feuersteghoehe"),"["+ManagerLanguage.getString("m")+"]",0,0.5); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_Quetschspalthoehe() {
		double Quetschspalthoehe;		
		try {
			Quetschspalthoehe =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Quetschspalthoehe"),"["+ManagerLanguage.getString("m")+"]",0,0.2);
			return Quetschspalthoehe;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}
	
	public double get_PistonBowlDiameter() {
		double pistonBowlDiameter;
		try {
			pistonBowlDiameter =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("PistonBowlDiameter"),"["+ManagerLanguage.getString("m")+"]",0,1);
			return pistonBowlDiameter;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}
	
	public double get_PistonBowlDepth() {
		double pistonBowlDepth;
		try {
			pistonBowlDepth =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("PistonBowlDepth"),"["+ManagerLanguage.getString("m")+"]",0,1);
			return pistonBowlDepth;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}

	/** 
	 * @return Gibt die Feuersteghoehe in [m]zurück.
	 * */
	public double get_Schraenkung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Schraenkung"),"["+ManagerLanguage.getString("m")+"]",-0.02,0.02); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return Gibt die Desachsierung in [m]zurück.
	 * */
	public double get_Desachsierung(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Desachsierung"),"["+ManagerLanguage.getString("m")+"]",-0.02,0.02); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}




	/** 
	 * @return Gibt den Zeitpunkt Auslassschluss in [s] nach Rechenbeginn zurück.
	 * */
	public double get_Auslassschluss(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC
		try {
			if(SYS.IS_KW_BASIERT){			
				double AV_S;
				AV_S =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Auslassschluss"),"["+ManagerLanguage.getString("KWnZOT")+"]",get_Auslassoeffnet_KW(),SYS.KW_OBERGRENZE);
				return convert_KW2SEC(AV_S);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Auslassschluss"),"["+ManagerLanguage.getString("s")+"]",get_Auslassoeffnet(),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	


	/** 
	 * @return Gibt den Zeitpunkt Auslassoeffnet  in [s] nach Rechenbeginn zurück.
	 * */
	public double get_Auslassoeffnet(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC
		try {
			if(SYS.IS_KW_BASIERT){					
				return  convert_KW2SEC(
						set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Auslassoeffnet"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.KW_UNTERGRENZE,SYS.KW_OBERGRENZE));

			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Auslassoeffnet"),"["+ManagerLanguage.getString("s")+"]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
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
	 * @return Gibt den Zeitpunkt Einlassoeffnet in [s] nach Rechenbeginn zurück.
	 * */
	public double get_Einlassoeffnet(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC
		try {
			if(SYS.IS_KW_BASIERT){			
				return convert_KW2SEC(
						set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Einlassoeffnet"),"["+ManagerLanguage.getString("KWnZOT")+"]",SYS.KW_UNTERGRENZE,SYS.KW_OBERGRENZE));

			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Einlassoeffnet"),"["+ManagerLanguage.getString("s")+"]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
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
	 * @return Gibt den Zeitpunkt Einlassschliesst in [s] nach Rechenbeginn zurück.
	 * */
	public double get_Einlassschluss(){		
		//		TODO Grenzwerte richtig setzen auf Basis des ZOT --> wird aber wieder schwierig mit den Zeitbasierten Angaben
		//		und dann noch die umrechnung der Zeit mit convert_KW2SEC. 
		//Ausserdem ist natuerlich Einlassoeffnet der untere Grenzwert dieser wird aber nur in sec zurueck gegeben
		try {
			if(SYS.IS_KW_BASIERT){			
				double AV_S;
				AV_S =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Einlassschluss"),"["+ManagerLanguage.getString("KWnZOT")+"]",get_Einlassoeffnet_KW(),SYS.KW_OBERGRENZE);
				return convert_KW2SEC(AV_S);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Einlassschluss"),"["+ManagerLanguage.getString("s")+"]",get_Einlassoeffnet(),SYS.SEC_OBERGRENZE);
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
			EV_Hub =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("EV_Hub"),"["+ManagerLanguage.getString("m")+"]",0,0.3);
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
			EV_Hub_max =set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("EV_Hub_max"),"["+ManagerLanguage.getString("m")+"]",0,0.3);
			return EV_Hub_max;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt die Referenzfläche zurück, die für die
	 * Durchflusskennzahlen des Auslassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Kolbenfläche gerechnet.
	 * */
	public double get_ReferenzflaecheAuslass() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("RefFlaecheAuslass"),"["+ManagerLanguage.getString("m^2")+"]",1e-6,100^0);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \""+ManagerLanguage.getString("RefFlaecheAuslass")+"\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \""+ManagerLanguage.getString("Kolbenflaeche")+"\" gerechnet!");
			return get_pistonArea();
		}		
	}
	/** 
	 * @return Gibt die Referenzfläche zurück, die für die RefDurchmesserEV
	 * Durchflusskennzahlen des Einlassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Kolbenfläche gerechnet.
	 * */
	public double get_ReferenzflaecheEinlass() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("RefFlaecheEinlass"),"["+ManagerLanguage.getString("m^2")+"]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \""+ManagerLanguage.getString("RefFlaecheEinlass")+"\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \""+ManagerLanguage.getString("Kolbenflaeche")+"\" gerechnet!");
			return get_pistonArea();
		}		
	}


	/** 
	 * @return Gibt den Referenzdurchmesser zurück, der für die
	 * Durchflusskennzahlen des Einlassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Bohrung gerechnet.
	 * */
	public double get_ReferenzDurchmesserEV() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("RefDurchmesserEV"),"["+ManagerLanguage.getString("m")+"]",1e-6,1);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \""+ManagerLanguage.getString("RefDurchmesserEV")+"\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \""+ManagerLanguage.getString("Bohrung")+"\" gerechnet!");
			return get_Bohrung();
		}	
	}
	
	/** 
	 * @return Gibt den Einlassventildurchmesser zurück, der für das
	 * Wandwärmemodell nach Bargende verwendet werden soll.
	 * */
	public double get_EV_Durchmesser() {
		double Durchmesser_EV;
		try {
			Durchmesser_EV = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("EV_Durchmesser"),"["+ManagerLanguage.getString("m")+"]",1e-3,0.1);
			return Durchmesser_EV;
		} catch (ParameterFileWrongInputException e) {		
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return Gibt den Referenzdurchmesser zurück, der für die
	 * Durchflusskennzahlen des Auslassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Bohrung gerechnet.
	 * */
	public double get_ReferenzDurchmesserAV() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("RefDurchmesserAV"),"["+ManagerLanguage.getString("m")+"]",1e-6,1);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \""+ManagerLanguage.getString("RefDurchmesserAV")+"\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \""+ManagerLanguage.getString("Bohrung")+"\" gerechnet!");
			return get_Bohrung();
		}	
	}	

	/////////////////////Kerrom, 06.06.14 /////////////////////
	/** Wird zur Überprüfung des berechneten Lambda verwendet.
	 * @return Gibt den im Input File angegebenen lambda Wert zurück. 
	 * Warnung, wenn kein lambda im Input File angegeben ist.
	 * 
	 */
	public double get_Lambda_Input() {
	
		try {
		return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("Lambda"), "[-]", 0,
					Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Lambda ist nicht im Input File angegeben oder besitzt einen ungültigen Wert. Es findet keine Plausibilitätsüberprüfung statt!");
			return -1;
		}
	}
	//////////////////////////////////////////////////////////
	// Kerrom: für kompletten Zyklus
	public double get_m_ini_APRkpl(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("m_ini"), "["+ManagerLanguage.getString("kg")+"]", 0, Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			return Double.NaN;
		}
	}

	// für kompletten Zyklus (eventuell nicht nötig)
	public double get_T_ini_APRkpl(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("T_ini_APRkpl"), "["+ManagerLanguage.getString("K")+"]", 0, Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {
			return Double.NaN;
		}
	}
	
	public double get_offsetTemperatur() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("offsetTemperatur"), "["+ManagerLanguage.getString("K")+"]", 0,
						Double.POSITIVE_INFINITY);
			} catch (ParameterFileWrongInputException e) {
				e.log_Warning("Im Input File wurde keine offsetTemperatur angegeben!");
				return 0;
			}
	}	
	
	public double get_AV_innerer_Ventilteller_Durchmesser(){ //"inner seat diameter"
		double Durchmesser_AV;
		try {
			Durchmesser_AV = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("AV_innerer_Ventilteller_Durchmesser"),"["+ManagerLanguage.getString("m")+"]",1e-3,0.1);
			return Durchmesser_AV;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}
	
	public double get_EV_innerer_Ventilteller_Durchmesser(){ //"inner seat diameter"
		double Durchmesser_EV;
		try {
			Durchmesser_EV = set_doublePara(INPUTFILE_PARAMETER, ManagerLanguage.getString("EV_innerer_Ventilteller_Durchmesser"),"["+ManagerLanguage.getString("m")+"]",1e-3,0.1);
			return Durchmesser_EV;
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

		//...und dann wird ueberprüft ob der Wert die richtige Einheit hat...
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
			throw new ParameterFileWrongInputException ("Der eingegebene Wert für \""	+ paraNameToSet + 
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
	 * Überprueft ob der angegebene Wert innerhalb der vorgegebenen Grenzen liegt
	 * @param wert
	 * @param min
	 * @param max
	 * @return gibt true zurück wenn der Wert innerhalb der Grenzen liegt
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
		public final double MINIMALE_ZONENMASSE;
		public final double T_FREEZE;

		public final double WRITE_INTERVAL_SEC;
		public final int ANZ_BERECHNETER_WERTE;

		//je nach gewählter Rechnungsart in [s] oder [KW]
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
		
		public final String THERMO_POLYNOM_KOEFF_VORGABE;
		public final String GLEICHGEWICHTSKONSTANTEN_VORGABE;	

		
		// Brennverlaufsvariablen 
		//fuer Verlustteilung Frank Haertel
//	    public File BRENNVERLAUF_FILE; 
//	    public int KANAL_SPALTEN_NR_DQBURN; 
//	    public String EINGABEDATEI_FORMAT_BURN; 

		public SysPara(Hashtable<String, String[]> parameterVorgaben) throws ParameterFileWrongInputException{

			PARAMETER=parameterVorgaben;				

			String [] yesno ={ManagerLanguage.getString("ja") , ManagerLanguage.getString("nein")};	
			if(set_StringPara(PARAMETER,ManagerLanguage.getString("debuggingMode"),yesno ).equalsIgnoreCase(ManagerLanguage.getString("ja")))
				DUBUGGING_MODE=true;
			else
				DUBUGGING_MODE=false;

			MINIMALE_ZONENMASSE=set_doublePara(PARAMETER, ManagerLanguage.getString("minimaleZonenMasse"),"["+ManagerLanguage.getString("kg")+"]",0,0.5); 

			//sollten sinnvolle Grenzen sein...Grill gibt 1600K an das alte Bremo verwendet 1700K
			T_FREEZE=set_doublePara(PARAMETER, ManagerLanguage.getString("T_freeze"),"["+ManagerLanguage.getString("K")+"]",1400,1900);	
			

			String [] s2 ={ManagerLanguage.getString("NASA9") , 
					       ManagerLanguage.getString("Burcat"),
					       ManagerLanguage.getString("ERC")};	
			THERMO_POLYNOM_KOEFF_VORGABE=set_StringPara(PARAMETER,ManagerLanguage.getString("polynomKoeffizienten"), s2);


			String [] s3 ={ManagerLanguage.getString("Janaf") , 
					       ManagerLanguage.getString("Burcat"),
					       ManagerLanguage.getString("Olikara")};	
			GLEICHGEWICHTSKONSTANTEN_VORGABE=set_StringPara(PARAMETER,ManagerLanguage.getString("gleichGewichtsKonstanten"), s3);	


			////////////////////////////////////////////////////////////////
			//Einlesen von Daten die Abhaengig von anderen Eingaben sind////			
			////////////////////////////////////////////////////////////////
			String []s0 ={"2T","4T"};	
			if(set_StringPara(PARAMETER,ManagerLanguage.getString("arbeitsverfahren"),s0).equalsIgnoreCase("4T")){
				DAUER_ASP_KW=720;
				UMD_ASP=2;
			}else{
				DAUER_ASP_KW=360;
				UMD_ASP=1;
			}		

			KW_UNTERGRENZE=-1*this.DAUER_ASP_KW; //gilt für die Definition aller KW Angaben in KWnZOT
			KW_OBERGRENZE=this.DAUER_ASP_KW;

			//TODO Die Zeitbasierten grenzwerte richtig Definieren
			SEC_UNTERGRENZE=Double.NEGATIVE_INFINITY;
			SEC_OBERGRENZE=Double.POSITIVE_INFINITY;


			String []s ={ManagerLanguage.getString("sec"),
					     ManagerLanguage.getString("sec_RechenBeginnInKW"), 
					     ManagerLanguage.getString("KW")};		
			if(set_StringPara(PARAMETER,ManagerLanguage.getString("zeit_oder_KW_Basiert"),s).equalsIgnoreCase("KW")){
				IS_KW_BASIERT=true;
				IS_ZEIT_BASIERT=false;
				IS_ZEIT_BASIERT_START_IN_KW=false;

			}else if (set_StringPara(PARAMETER,ManagerLanguage.getString("zeit_oder_KW_Basiert"),s).equalsIgnoreCase("sec")){
				IS_KW_BASIERT=false;
				IS_ZEIT_BASIERT=true;
				IS_ZEIT_BASIERT_START_IN_KW=false;
				if(MODUL_VORGABEN.get(MotorFabrik.MOTOR_FLAG)==ManagerLanguage.getString("HubKolbenMotor"))
					throw new ParameterFileWrongInputException("Eine vollstaendig zeitbasierte Rechnung kann " +
							"nur mit einem filebasierten Motorobjekt und einem Eingabefile der Form [ZEIT] [ZYLINDERVOLUMEN] erfolgen." +
							"Alternativ ist \""+ManagerLanguage.getString("sec_RechenBeginnInKW")+"\" zu wählen");

			}else{
				IS_KW_BASIERT=false;
				IS_ZEIT_BASIERT=false;
				IS_ZEIT_BASIERT_START_IN_KW=true;
			}				

			if(IS_KW_BASIERT){			
				double WRITE_INTERVAL_KW=set_doublePara(PARAMETER, "rechnungsSchrittweite","[KW]",1e-6,2);	
				//TODO Grenzwerte mittels Einlassoeffnet und Auslassschließt definieren. 
				//Fuer die Ladungswechselanalyse werden eigene Rechenbeginne und Rechenenden definiert.
				WRITE_INTERVAL_SEC= WRITE_INTERVAL_KW/(360*get_DrehzahlInUproSec());
				double temp;
				try{
					if(MODUL_VORGABEN.get("berechnungsModell").equals("APR_kompletterZyklus"))
						temp = set_doublePara(PARAMETER, ManagerLanguage.getString("Auslassoeffnet"), "["+ManagerLanguage.getString("KWnZOT")+"]",
								-1*DAUER_ASP_KW, DAUER_ASP_KW)-DAUER_ASP_KW;
					else
						temp = set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsBeginn"),"["+ManagerLanguage.getString("KWnZOT")+"]",
								KW_UNTERGRENZE,KW_OBERGRENZE);				
				}catch(ParameterFileWrongInputException p){
					temp = set_doublePara(PARAMETER, ManagerLanguage.getString("Einlassschluss"), "["+ManagerLanguage.getString("KWnZOT")+"]",
							-1*DAUER_ASP_KW, DAUER_ASP_KW);
				}
				RECHNUNGS_BEGINN_DVA_KW = temp;

				try{
					if(MODUL_VORGABEN.get("berechnungsModell").equals("APR_kompletterZyklus"))
						temp = set_doublePara(PARAMETER, ManagerLanguage.getString("Auslassoeffnet"), "["+ManagerLanguage.getString("KWnZOT")+"]",
								-1*DAUER_ASP_KW, DAUER_ASP_KW)-WRITE_INTERVAL_KW;
					else
						temp=set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsEnde"),"["+ManagerLanguage.getString("KWnZOT")+"]",
								RECHNUNGS_BEGINN_DVA_KW,KW_OBERGRENZE);
				}catch(ParameterFileWrongInputException p){
					temp = set_doublePara(PARAMETER, ManagerLanguage.getString("Auslassoeffnet"), "["+ManagerLanguage.getString("KWnZOT")+"]",
							-1*DAUER_ASP_KW, DAUER_ASP_KW);
				}
				RECHNUNGS_ENDE_DVA_KW = temp;
				//der Anfang zaehlt mit
				ANZ_BERECHNETER_WERTE=(int)((RECHNUNGS_ENDE_DVA_KW-RECHNUNGS_BEGINN_DVA_KW)/WRITE_INTERVAL_KW)+1;

				RECHNUNGS_ENDE_DVA_SEC=(ANZ_BERECHNETER_WERTE-1)*WRITE_INTERVAL_SEC;			

				RECHNUNGS_BEGINN_DVA_SEC=0;				

				if(DUBUGGING_MODE){
					double b=set_doublePara(PARAMETER, ManagerLanguage.getString("debuggingTime"),"["+ManagerLanguage.getString("KWnZOT")+"]",RECHNUNGS_BEGINN_DVA_KW,RECHNUNGS_ENDE_DVA_KW+DAUER_ASP_KW);

					DUBUGGING_TIME_SEC=(b-RECHNUNGS_BEGINN_DVA_KW)/(360*get_DrehzahlInUproSec());	

				}else{
					DUBUGGING_TIME_SEC=RECHNUNGS_BEGINN_DVA_SEC;
				}


			}else if(IS_ZEIT_BASIERT){
				//TODO um die gesamte zeitbasierte sache muss sich noch jemand kuemmern!!!

				//min-Wert --> ca. 0.01°KW bei 18000U/min
				//max-Wert --> ca .2°KW bei 50 U/min
				WRITE_INTERVAL_SEC=set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsSchrittweite"),"["+ManagerLanguage.getString("s")+"]",9.5e-7,6.7e-3);

				//muss eigentlich nur passend zum MotorFile und DruckFile sein
				double temp;
				try{
					temp = set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsBeginn"),"["+ManagerLanguage.getString("s")+"]",
						Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);	
				}catch(ParameterFileWrongInputException p){
					temp = get_Einlassschluss();
				}
				RECHNUNGS_BEGINN_DVA_SEC = temp;
				try{
					temp = set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsEnde"),"["+ManagerLanguage.getString("s")+"]",
						RECHNUNGS_BEGINN_DVA_SEC,Double.POSITIVE_INFINITY);
				}catch(ParameterFileWrongInputException p){
					temp = get_Auslassoeffnet();
				}
				RECHNUNGS_ENDE_DVA_SEC = temp;

				ANZ_BERECHNETER_WERTE=(int)(( RECHNUNGS_ENDE_DVA_SEC-RECHNUNGS_BEGINN_DVA_SEC)/WRITE_INTERVAL_SEC)+1;

				//final Variablen muessen mir Werten belegt werden
				RECHNUNGS_BEGINN_DVA_KW=555.555;
				RECHNUNGS_ENDE_DVA_KW=555.555;

				if(DUBUGGING_MODE){
					DUBUGGING_TIME_SEC=set_doublePara(PARAMETER, ManagerLanguage.getString("debuggingTime"),"["+ManagerLanguage.getString("s")+"]",RECHNUNGS_BEGINN_DVA_SEC,RECHNUNGS_ENDE_DVA_SEC);
				}else{
					DUBUGGING_TIME_SEC=RECHNUNGS_BEGINN_DVA_SEC;
				}				

			}else if(IS_ZEIT_BASIERT_START_IN_KW){
				//min-Wert --> ca. 0.01°KW bei 18000U/min
				//max-Wert --> ca .2°KW bei 50 U/min
				WRITE_INTERVAL_SEC=set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsSchrittweite"),"["+ManagerLanguage.getString("s")+"]",9.5e-5,6.7e-3);

				RECHNUNGS_BEGINN_DVA_KW=set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsBeginn"),"["+ManagerLanguage.getString("KWnZOT")+"]",
						KW_UNTERGRENZE,KW_OBERGRENZE);	

				RECHNUNGS_BEGINN_DVA_SEC=0;	

				RECHNUNGS_ENDE_DVA_SEC=set_doublePara(PARAMETER, ManagerLanguage.getString("rechnungsEnde"),"["+ManagerLanguage.getString("s")+"]",
						RECHNUNGS_BEGINN_DVA_SEC,Double.POSITIVE_INFINITY);	
				//die Zeitrechnung beginnt hier bei null!
				ANZ_BERECHNETER_WERTE=(int)(RECHNUNGS_ENDE_DVA_SEC/WRITE_INTERVAL_SEC)+1;

				RECHNUNGS_ENDE_DVA_KW=360*get_DrehzahlInUproSec()*RECHNUNGS_ENDE_DVA_SEC;			



				if(DUBUGGING_MODE){
					DUBUGGING_TIME_SEC=set_doublePara(PARAMETER, ManagerLanguage.getString("debuggingTime"),"["+ManagerLanguage.getString("s")+"]",RECHNUNGS_BEGINN_DVA_SEC,RECHNUNGS_ENDE_DVA_SEC);
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


			DAUER_ASP_SEC=(DAUER_ASP_KW)/(360*get_DrehzahlInUproSec());
			
			//fuer Verlustteilung Frank Haertel
//			try{ 
//				  File BRENNVERLAUF_FILE=get_FileToRead("burnFileName");
//				  int indexOf=BRENNVERLAUF_FILE.getName().indexOf(".");
//				  String EINGABEDATEI_FORMAT_BURN=BRENNVERLAUF_FILE.getName().substring(indexOf+1); //Dateiendung
//				  String burnFileName=BRENNVERLAUF_FILE.getName();
				  
//				  String burnFileName=set_FileName(PARAMETER,"burnFileName"); 
//			      int indexOf_burn=burnFileName.indexOf("."); 
//			      EINGABEDATEI_FORMAT_BURN=burnFileName.substring(indexOf_burn+1); 
//			      //Dateiendung 
//			      BRENNVERLAUF_FILE=new File(WD+burnFileName); 
//			      KANAL_SPALTEN_NR_DQBURN=(int)set_doublePara(PARAMETER, "spalte_dQburn","",1,40); //bis zu fünf kaskadierte CombiWF 
//				  KANAL_SPALTEN_NR_DQBURN=get_ColumnToRead("spalte_dQburn"); //bis zu fünf kaskadierte CombiWF
//			      } 
//			      catch (ParameterFileWrongInputException e) { 
//			        e.log_Warning("Es wurde kein Brennverlaufsfile (\"spalte_dQburn\") angegeben! Es kann keine APR durchgeführt werden!"); 
//			      } 

		}//Konstruktor SysPara	

	}//Klasse SysPara

	public void set_CalledFromGUI(boolean calledFromGUI) {
		this.calledFromGUI = calledFromGUI;
	}
	
	/**
	 * Zum Ändern eines Double-Wertes im Inputfile-Parametersatzes für iterative Rechnungen. 
	 * @param paraName
	 * @param paraEinheit -- Auf richtige Einheit achten!! 
	 * @param paraWert
	 */
	public void set_ParaInputfile(String paraName, String paraEinheit, double paraWert){
		String[] paraWertEinheit = {Double.toString(paraWert), paraEinheit};
		INPUTFILE_PARAMETER.put(paraName, paraWertEinheit);
	}
	
	/**
	 * Zum Ändern eines String-Wertes im Inputfile-Parametersatzes für iterative Rechnungen. 
	 * @param paraName
	 * @param paraEinheit -- Auf richtige Einheit achten!! 
	 * @param paraWert
	 */
	public void set_ParaInputfile(String paraName, String paraEinheit, String paraWert){
		String[] paraWertEinheit = {paraWert, paraEinheit};
		INPUTFILE_PARAMETER.put(paraName, paraWertEinheit);
	}
}//Klasse CasePara
