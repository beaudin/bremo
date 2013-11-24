package bremo.parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import misc.PhysConst;
import kalorik.GasMixture;
import kalorik.Spezies;
import kalorik.SpeciesFactory;
import io.InputFileReader;
import berechnungsModule.ResultsBuffer;
import berechnungsModule.Berechnung.SimulationModel;
import berechnungsModule.Berechnung.SimulationModelFactory;
import berechnungsModule.Berechnung.CanteraCaller;
import berechnungsModule.chemEquilibriumSolver.ChemEquilibriumSolver;
import berechnungsModule.chemEquilibriumSolver.ChemEquilibriumSolverFactory;
import berechnungsModule.mixtureFormation.FuelProperties;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.MotorFactory;
import berechnungsModule.residualGas.ResidualGas;
import berechnungsModule.residualGas.ResidualGasModelFactory;
import berechnungsModule.residualGas.ResidualGasUserInput.CallOnlyFromResidualGasUserInput;
import berechnungsModule.turbulence.TurbulenceModelFactory;
import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import berechnungsModule.wallHeatTransfer.WallHeatTransferModelFactory;
import bremo.sys.Solver;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;




public class CasePara {	

	private final Hashtable<String,String[]> INPUTFILE_PARAMETER;
	public final Hashtable<String,String> MODUL_VORGABEN;	
	private  Vector<ResultsBuffer> alleErgBuffers=new Vector<ResultsBuffer>();
	private double aktuelle_RZ;	//Aktuelle Rechenzeit
	//	public final BerechnungsModule MODULE;
	public final SysPara SYS; //SystemParameter	
	private final String WD; //WorkingDirectory
	/**
	 * Name des Inputfiles OHNE Dateiendung
	 */
	private final String CASE_NAME;		

	public final Motor MOTOR;
	public final ChemEquilibriumSolver OHC_SOLVER;
	public final WallHeatTransfer WALL_HEAT_TRANSFER_MODEL;
	public final WallHeatTransfer WALL_HEAT_TRANSFER_MODEL_FILL_EMPTY;
	public final ResidualGas RESIDUAL_GAS_MODEL;
	public final SimulationModel SIMULATION_MODEL;
	public final SpeciesFactory SPECIES_FACTROY;
	public final MasterInjection MASTER_INJECTION;
	public final Solver SOLVER;
	public final TurbulenceModelFactory TURB_FACTORY;
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
		callsCantera=SimulationModelFactory.callsCantera(this);	
		
		SYS=new SysPara(INPUTFILE_PARAMETER);	
		
		//SpeziesFabrik	
		SPECIES_FACTROY=new SpeciesFactory(this, new MakeMeUnique());	 
		//MakeMeUnique sorgt dafür, dass es nur eine Instanz der Speziesfabrik geben kann. 
		//Ansonsten wuerde es Probleme mit der INtegration der einzelnen Speziesmassen geben. 
		//CO2 ist zwar CO2 aber bei verschiedenen Objekt IDs weiss Bremo das nicht!			

		//Solver
		SOLVER=new Solver(this);
		
		//Motor
		MotorFactory mf=new MotorFactory(this);
		MOTOR=mf.MOTOR;

		//ohc-Solver
		ChemEquilibriumSolverFactory ohcf=new ChemEquilibriumSolverFactory(this);
		OHC_SOLVER=ohcf.OHC_SOLVER;

		//MasterEinspritzung
		MASTER_INJECTION=new MasterInjection(this);

		//Restgas benoetigt MASTER_EINSPRITZUNG
		ResidualGasModelFactory irgf=new ResidualGasModelFactory(this);
		RESIDUAL_GAS_MODEL=irgf.RESIDUAL_GAS_MODEL;
		
		//Wandwaerme
		WallHeatTransferModelFactory wwf=new WallHeatTransferModelFactory(this);
		WALL_HEAT_TRANSFER_MODEL=wwf.WHT_MODEL;
		//Die LWA soll mit einem anderen WW-Modell durchgefuehrt werden koennen	
		//this needs the internal EGR model
		WALL_HEAT_TRANSFER_MODEL_FILL_EMPTY=wwf.WHT_MODEL_FILL_EMPTY; 	
		
		//Turbulence
		TURB_FACTORY=new TurbulenceModelFactory(this, new MakeMeUnique());
		//Berechnungsmodell benoetigt MASTER_EINSPRITZUNG und RESTGAS_MODELL
		SimulationModelFactory bmf=new SimulationModelFactory(this);
		SIMULATION_MODEL=bmf.SIMULATION_MODEL;	
		SOLVER.set_BerechnungsModell(SIMULATION_MODEL);
		
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
	 * Für einen BerechnungsCase sind alle ErgebnisBuffer in einem Vector gespeichert. Hiermit wird dem 
	 * Vector der ErgebnisBuffer ergB hinzugefuegt und kann damit spaeter in ein txt-file geschreiben werden.
	 * Der checkIn erfolgt automatisch bei der Erzeugung eines {@link ResultsBuffer}
	 * @param ergB
	 */
	public void ergBufferCheckIN(ResultsBuffer ergB){
		if(!alleErgBuffers.contains(ergB))//nur wenn der ErgebnisBuffer noch nich tim Vector steht wird er eingecheckt
			this.alleErgBuffers.add(ergB);		
	}

	/**
	 * Diese Funktion ermoeglicht das einfache schreiben aller ErgebnisBuffer, die 
	 * zuvor  mittels {@link ergBufferCheckIN} eingecheckt wurden.
	 * @param name
	 */
	public  void schreibeAlleErgebnisFiles(String name){		
		Iterator<ResultsBuffer> itr = alleErgBuffers.iterator();		
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
		String [] yesno={"yes","no"};
		boolean filter=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"filterExperimentalPressureData",yesno ).equalsIgnoreCase("yes"))
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
		String [] yesno={"yes","no"};
		boolean shift_pIn=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"shift_pIn",yesno ).equalsIgnoreCase("yes"))
				shift_pIn=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return shift_pIn;
	}
	
	public boolean shift_pOutlet(){
		String [] yesno={"yes","no"};
		boolean shift_pOut=false;
		try {
			if(set_StringPara(INPUTFILE_PARAMETER,"shift_pEx",yesno ).equalsIgnoreCase("yes"))
				shift_pOut=true;
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return shift_pOut;
	}

	public String get_pressureAdjustmentMethod(String availableMethods []){
		String method=null;
		String methodIdentifier="pressureAdjustmentMethod";
		try {					
			method=set_StringPara(this.INPUTFILE_PARAMETER,methodIdentifier,availableMethods);
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
			tmpRELAX=set_doublePara(INPUTFILE_PARAMETER, "relaxFactor","[-]",0.1,1);	
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
			precission=1e5*set_doublePara(INPUTFILE_PARAMETER, "precissionPressureTraceAnalysis","[bar]",0,0.1);
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
	 * Mit dieser Methode wird die aktuelle Rechenzeit zurückgegeben.
	 * Diese Zeit entspricht dem letzten ganzen Rechenzeitschritt und
	 * nicht einem vom RungeKutta-Verfahren verwendeten Zwischenschritt.
	 * @param time in [s]
	 */
	public double get_time(){
		return aktuelle_RZ;
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
			if(SYS.IS_CA_BASED){				
				verbrennungsBeginn =set_doublePara(
						INPUTFILE_PARAMETER, "verbrennungsBeginn","[KWnZOT]",SYS.SIMULATION_START_CA,SYS.SIMULATION_END_CA);
				verbrennungsBeginn=convert_KW2SEC(verbrennungsBeginn);
			}else {				
				verbrennungsBeginn =set_doublePara(INPUTFILE_PARAMETER, "verbrennungsBeginn","[s]",SYS.SIMULATION_START_SEC,SYS.SIMULATION_END_SEC);
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
	
	public double get_iniMoleFrac_H2() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_H2","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_H() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_H","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_CO() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_CO","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_OH() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_OH","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_O() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_O","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_NO() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_NO","[-]",0,1);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_iniMoleFrac_N() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "iniMoleFrac_N","[-]",0,1);
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
	
	public double get_turbKineticEnergy_Ini(){	
			try {
				return set_doublePara(INPUTFILE_PARAMETER, "TurbKineticEnergy_Ini","[m^2/s^2]",0,Double.POSITIVE_INFINITY);
			} catch (ParameterFileWrongInputException e) {			
				return -5.55;
			}		
	}
	
	public double get_C_MixRad() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, "C_MixRad","[-]",0.0001,1000); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	
	public double get_C_Mix() {		
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, "C_Mix","[-]",0,50000); 
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
			return 	 set_doublePara(INPUTFILE_PARAMETER, "swirlRatio","[-]",0,50); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}
	
	public int get_mixingProcess() {
		int a=-1;
		try {			
			
			a=(int)set_doublePara(INPUTFILE_PARAMETER, "mixingProcess","[-]",1,6); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
		}
		return a;
	}



	/** 
	 * @return returns the rotational speed in [Rot/sec]
	 */	
	public double get_rotSpeedInRotperSec(){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "rotationalSpeed","[min^-1]",0,Double.POSITIVE_INFINITY)/60;
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
		MasterInjection me=this.MASTER_INJECTION;//zirkuläre Referenz ;-( naja mal schauen...
		double mKrst=me.get_mFuel_Sum_Cycle();
		Spezies krst=me.get_spezKrstALL(); //liefert die Mischung aller Krafstoffe				
		
		double mFG=mLuft_tr+mW+mKrst;	//gesamte Masse des Verbrennenden Gases
		Hashtable<Spezies,Double>frischGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
		//trockene Luft
		frischGemisch_MassenBruchHash.put(this.SPECIES_FACTROY.get_spezLuft_trocken(),mLuft_tr/mFG);
		//Wasser
		frischGemisch_MassenBruchHash.put(this.SPECIES_FACTROY.get_H2O(),mW/mFG);
		//Kraftstoff
		if(krst!=null&& mKrst!=0)
			frischGemisch_MassenBruchHash.put(krst,mKrst/mFG);

		//Erstellen der Frischgemischspezies
		GasMixture frischGemsich=new GasMixture("Frischgemisch");
		frischGemsich.set_Gasmischung_massenBruch(frischGemisch_MassenBruchHash);


		//Bestimmen der AGR-Zusammensetzung --> das Hinzufuegen von AGR hat keinen Einfluss auf die 
		//AGR-Zusammensetzung da die Anzahl der c/h/o-Atome gleich bleibt und nur vom Frischgemisch abhaengt
		ChemEquilibriumSolver gg=this.OHC_SOLVER; //zirkuläre Referenz ;-( naja mal schauen...
		GasMixture abgas=new GasMixture("Abgas");
		//eigentlich muesste die GLGW-Zusammensetzung aus der DVA kommen der Druck wuerde auf den Wert festgelegt, 
		//den er hat wenn 1600K in der verbrannten Zone vorliegen
		//-->die Druckabhängigikeit bei niedrigen Temperaturen ist aber so gering, dass der Druck hier keine Rolle spielt!
		abgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(1e5, 1000, frischGemsich)); //T=1000K<T_Freeze
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
		double mAGRin=this.RESIDUAL_GAS_MODEL.get_mResidualGas_perCycle();//zirkuläre Referenz ;-( naja mal schauen...
		double mAGR=mAGRex+mAGRin;
		double mGes=mLuft_tr+mW+mAGR; //gesamte Masse im Zylinder (ohne Kraftstoff)
		Hashtable<Spezies,Double>verbrennungsLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		verbrennungsLuft_MassenBruchHash.put(abgas, mAGR/mGes);
		verbrennungsLuft_MassenBruchHash.put(this.SPECIES_FACTROY.get_spezLuft_trocken(),mLuft_tr/mGes);		
		verbrennungsLuft_MassenBruchHash.put(this.SPECIES_FACTROY.get_H2O(),mW/mGes);
		GasMixture verbrennungsLuft=new GasMixture("Verbrennungsluft");
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
		double mAGRin=this.RESIDUAL_GAS_MODEL.get_mResidualGas_perCycle();//zirkuläre Referenz ;-( naja mal schauen...
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
					set_doublePara(INPUTFILE_PARAMETER, "mAGR_extern","[kg/h]",0,Double.POSITIVE_INFINITY));
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/**This method is only used when the residual gas fraction is ot calculated with any of the other models
	 * but specified by the user.
	 * @return returns the mass of residual gas for one cycle [kg]
	 */	
	public double get_mRG_perCycle_FromInputFile(CallOnlyFromResidualGasUserInput cofrgi){
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "mRG_perCycle","[kg]",0,Double.POSITIVE_INFINITY);
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
		double pws=PhysConst.get_saettigunsDampfdruck_H2O(this.get_T_FeuchteMessung());
		double phi=this.get_relativeLuftFeuchte();
		double p=this.get_p_FeuchteMessung();
		return PhysConst.get_R_Luft()/PhysConst.get_R_H2O()*(pws/(p/phi-pws));	
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
		MasterInjection me=this.MASTER_INJECTION;//zirkuläre Referenz ;-( naja mal schauen...
		double mKrst=me.get_mFuel_Sum_Cycle();
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
			if(SYS.IS_CA_BASED){				
				zzp =set_doublePara(INPUTFILE_PARAMETER, "ZZP","[KWnZOT]",SYS.SIMULATION_START_CA,SYS.SIMULATION_END_CA);
				zzp=convert_KW2SEC(zzp);
			}else {				
				zzp =set_doublePara(INPUTFILE_PARAMETER, "ZZP","[s]",SYS.SIMULATION_START_SEC,SYS.SIMULATION_END_SEC);
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
			return set_doublePara(INPUTFILE_PARAMETER, "pmi","[bar]",0,30); 
		} catch (ParameterFileWrongInputException e) {				
			e.stopBremo();
			return std_pmi;
		}
	}	


	/**
	 * Mehrfacheinspritzungen sind möglich --> über den Parameter  wird die Anzahl der Einspritzungen vorgegeben \br
	 * Wird dieser Parameter nicht angegeben, wird er auf eins gesetzt und eine Warnung wird ins LogFile geschrieben
	 * @param  
	 * @return liefert den Einspritzbeginn in [s] nach Rechnungsbeginn 
	 */	
	public int get_nbrOfInjections(){		
		try {
			return (int) set_doublePara(INPUTFILE_PARAMETER, "nbrOfInjections","",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
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
			return (int) set_doublePara(INPUTFILE_PARAMETER, "indexOfMainInj","",0,Double.POSITIVE_INFINITY);
		} catch (ParameterFileWrongInputException e) {			
			e.log_Warning();
			return -1;
		}		
	}

	/**
	 * @param  idxOfInjection As multiple injections are possible this
	 * specifies the index of the injection
	 * @return returns the begin of injection in sec after start of simulation
	 */	
	public double get_BOI(int idxOfInjection){
		String  suchString;
		int anzEinspr=get_nbrOfInjections();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("I tried to set the BOI of injection " +
						"with the following index:  " +idxOfInjection 
						+". " +
						"\nBut following your inputFile there are only  " +anzEinspr + " injections!" +
								"\nSomething in the MasterInjection must be wrong! Go and take a look!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}

		if (anzEinspr==1){
			suchString="BOI";
		}else{
			suchString="BOI_"+idxOfInjection;
		}
		try {
			if(SYS.IS_CA_BASED){	
				double boi;
				boi =set_doublePara(INPUTFILE_PARAMETER, suchString,"[CAaTDCF]",SYS.CA_MAX,SYS.CA_MIN);
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
	 @param  idxOfInjection As multiple injections are possible this
	 * specifies the index of the injection
	 * @return returns the end of injection in sec after start of simulation
	 */	
	public double get_EOI(int idxOfInjection){
		String suchString;
		int anzEinspr=get_nbrOfInjections();
		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("I tried to set the EOI of injection " +
						"with the following index:  " +idxOfInjection 
						+". " +
						"\nBut following your inputFile there are only  " +anzEinspr + " injections!" +
								"\nSomething in the MasterInjection must be wrong! Go and take a look!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();				
			}			
		}		

		if (anzEinspr==1){
			suchString="EOI";
		}else{
			suchString="EOI_"+idxOfInjection;
		}
		try {
			if(SYS.IS_CA_BASED){			
				double eoi;
				eoi =set_doublePara(INPUTFILE_PARAMETER, suchString,"[CAaTDCF]",convert_SEC2KW(get_BOI(idxOfInjection)),get_Auslassoeffnet_KW());
				return convert_KW2SEC(eoi);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, suchString,"[s]",get_BOI(idxOfInjection),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/**
	 * Returns the ID of the zone the injection with the inndex {@code idxOfInjection} 
	 * injects to
	 * @param  idxOfInjection 
	 * @return zonenID 
	 */	
	public int get_InjZone(int idxOfInjection){
		String  suchString;
		int anzEinspr=get_nbrOfInjections();		
		int injZone=-1;
		if (anzEinspr==1){
			suchString="InjZone";
		}else{
			suchString="InjZone_"+idxOfInjection;
		}

		if(idxOfInjection>anzEinspr-1){
			try {
				throw new ParameterFileWrongInputException("I tried to set the parameter \"" + suchString + 
						"\" for the injection with the index " +idxOfInjection 
						+". " +
						"\nBut following your inputFile there are only " +anzEinspr + " injections!");
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
		int anzEinspr=this.get_nbrOfInjections();
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
			krstFlag="fuel";	
		}else{
			krstFlag="fuel_"+idxOfInjection;
		}		

		//Die Koeffizienten Speziesfabrik liefert eine hashtable mit allen moeglichen Krafstoffen
		Hashtable<String,Spezies> krstHash=this.SPECIES_FACTROY.get_alleKrafstoffe();
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
						"oder selber Hand ans Programm zu legen! Gruß Juwe");
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
	 * injection with the index: idxOfInjection
	 * @param idxOfInjection
	 * @return
	 */
	public double get_mKrst_ASP(int idxOfInjection){
		int anzEinspr=this.get_nbrOfInjections();
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
			krstFlag="mFuel";	
		}else{
			krstFlag="mFuel_"+idxOfInjection;
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
	 * @param idxOfInjection
	 * @return
	 */
	public int get_anzAxialPakete(int idxOfInjection) {
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="nbrOfAxialPackets";	
		}else{
			flag="nbrOfAxialPackets_"+idxOfInjection;
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
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="nbrOfRadialPackets";	
		}else{
			flag="nbrOfRadialPackets_"+idxOfInjection;
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
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="injectionPressure";	
		}else{
			flag="injectionPressure_"+einspritzungsNr;
		}			
		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"[Pa]",0,Double.MAX_VALUE);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}	



	public double get_anzSPL(int einspritzungsNr) {
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="nbrOfNozzleHoles";	
		}else{
			flag="nbrOfNozzleHoles_"+einspritzungsNr;
		}			
		try {
			return 	(int) set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1,Double.MAX_VALUE);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}


	public double get_CdFactor(int einspritzungsNr) {
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="cdFactor";	
		}else{
			flag="cdFactor_"+einspritzungsNr;
		}			
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",0.1,1);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	public double get_entrainmentFactor(int einspritzungsNr) {
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="entrainmentFactor";	
		}else{
			flag="entrainmentFactor_"+einspritzungsNr;
		}			
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1e-9,100); //beliebig gewaehlt
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	public double get_profileFactor(int einspritzungsNr) {
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="profilFactor";	
		}else{
			flag="profilFactor_"+einspritzungsNr;
		}			
		try {
			return 	 set_doublePara(INPUTFILE_PARAMETER, flag,"[-]",1e-9,100); //beliebig gewaehlt
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}

	public double get_durchmSPL(int einspritzungsNr) {
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="diameterNozzleHole";	
		}else{
			flag="diameterNozzleHole_"+einspritzungsNr;
		}			
		try {
			return 	set_doublePara(INPUTFILE_PARAMETER, flag,"[m]",0.00001,0.001);
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return 0;
		}			
	}



	public String get_fuelPropsForEvap(int einspritzungsNr) {		
		int anzEinspr=this.get_nbrOfInjections();
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
			flag="modelFuelForEvaporation";	
		}else{
			flag="modelFuelForEvaporation_"+einspritzungsNr;
		}			
		try {
			return this.set_StringPara(INPUTFILE_PARAMETER, flag, FuelProperties.modelFuels);
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
			e.log_Warning("Fuer den ausgewaehlten Kraftatoff \"RON_95\" wurde kein Heizwert angegeben. \n" +
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
			e.log_Warning("Fuer den ausgewaehlten Kraftatoff \"RON_98\" wurde kein Heizwert angegeben. \n" +
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
			e.log_Warning("Fuer den ausgewaehlten Kraftatoff \"Diesel\" wurde kein Heizwert angegeben. \n" +
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
	 * Returns the polytropic exponent needed for WHTF models like Woschni or Chang (this method 
	 * is only called when the simulation does not start at IVC)
	 * @return polyExp_WHT [-]
	 */
	public double get_polyExp_WHT(){
		double polyExp_WHT=-5;
		try {
			polyExp_WHT= set_doublePara(INPUTFILE_PARAMETER, "polyExp_WHT","[-]",1,1.4); 
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();	
		}					
		return polyExp_WHT;
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
			if(SYS.IS_CA_BASED){				
				KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Beginn_Druckabgleich","[KWnZOT]",SYS.SIMULATION_START_CA,SYS.SIMULATION_END_CA);
				KW_beginnAbgleich=convert_KW2SEC(KW_beginnAbgleich);
			}else {				
				KW_beginnAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Beginn_Druckabgleich","[s]",SYS.SIMULATION_START_SEC,SYS.SIMULATION_END_SEC);
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
			if(SYS.IS_CA_BASED){				
				KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Ende_Druckabgleich","[KWnZOT]",SYS.SIMULATION_START_CA,SYS.SIMULATION_END_CA);
				KW_endeAbgleich=convert_KW2SEC(KW_endeAbgleich);
			}else {				
				KW_endeAbgleich =set_doublePara(INPUTFILE_PARAMETER, "KW_Ende_Druckabgleich","[s]",SYS.SIMULATION_START_SEC,SYS.SIMULATION_END_SEC);
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
	 * Returns the temperature at IVC 
	 * (needed for WHT models like Woschni if simulation 
	 * does not start at IVC)
	 * @return T_IVC_WHT [K]
	 */
	public double get_T_IVC_WHT(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "T_IVC_WHT","[K]",1e-6,1000);
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
			return set_doublePara(INPUTFILE_PARAMETER, "T_Ladeluft","[K]",250,1000);
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
	 * As soon as the temperature reaches this level reaction kinetics are solved
	 * @return
	 */
	public double get_T_StartKinetics(){	
		double T_kin=-1e100;
		if(T_kin==-1e100){
			try {
				return set_doublePara(INPUTFILE_PARAMETER, "T_StartKinetics","[K]",0,1000);//da macht wohl jeder Stahl schlapp
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
				return Double.NaN;
			}
		}else{
			return T_kin;
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
				return set_doublePara(INPUTFILE_PARAMETER, "T_Cyl","[K]",1e-6,1000);//da macht wohl jeder Stahl schlapp
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
				return set_doublePara(INPUTFILE_PARAMETER, "T_Piston","[K]",1e-6,1000);//da macht wohl jeder Stahl schlapp
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
				return set_doublePara(INPUTFILE_PARAMETER, "T_Head","[K]",1e-6,1000);//da macht wohl jeder Stahl schlapp
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
		int anzEinspr=get_nbrOfInjections();
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
			suchString="T_Fuel_liquid";
		}else{
			suchString="T_Fuel_liquid_"+i;
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
	/**
	 * Returns the pressure at IVC 
	 * (needed for WHT models like Woschni if simulation 
	 * does not start at IVC)
	 * @return p_IVC [N/m^2]
	 */
	public double get_p_IVC_WHT() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "p_IVC","[Pa]",0,Double.POSITIVE_INFINITY);
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
	 * @return liefert den Referenzpunkt [°KW nZOT], bei dem der Kappa Wert für das 
	 * WoschniHuber-Wandwärmemodell bzw. Changs Modell gerechnet wird. Hensels Modell
	 * verwendet den Punkt auch.
	 */
	public double get_refTime_WoschniHuber(){
		double KW_refPunkt;		
		try {
			if(SYS.IS_CA_BASED){				
				KW_refPunkt =set_doublePara(INPUTFILE_PARAMETER, "refTimeWoschniHuber","[KWnZOT]",SYS.SIMULATION_START_CA,SYS.SIMULATION_END_CA);
				KW_refPunkt=convert_KW2SEC(KW_refPunkt);
			}else {				
				KW_refPunkt =set_doublePara(INPUTFILE_PARAMETER, "refTimeWoschniHuber","[s]",SYS.SIMULATION_START_SEC,SYS.SIMULATION_END_SEC);
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
	 * @return returns the length of the connecting rod in [m]. * 
	 * */
	public double get_conRodLength(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "conRodLength","[m]",0.01,10); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return returns the bore diameter in [m].	 * 
	 * */
	public double get_bore(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "bore","[m]",0.005,2); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}	


	/** 
	 * @return  returns the crank radius in [m].* 
	 * */
	public double get_crankRadius() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "crankRadius","[m]",0.0025,Double.POSITIVE_INFINITY); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/** 
	 * @return returns the compression ratio. Values between  1 and 30 are accepted
	 * */
	public double get_compressionRatio(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "compressionRatio","",1,30); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return returns the piston surface area in [m^2].
	 * */
	public double get_pistonSurfaceArea(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "pistonSurfaceArea","[m^2]",0.00002,5); //aus minimaler Bohrung und aus maximaler Bohrung berechnet
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}


	/** 
	 * @return returns the surface area of the combustion chamber roof in [m^2].
	 * */
	public double get_cmbChmbrRoofSurfaceArea(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "combustionChamberRoofSurfArea","[m^2]",0.00002,5); //aus minimaler Bohrung und aus maximaler Bohrung berechnet
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	/** 
	 * @return returns the height of the top ring land in [m].
	 * */
	public double get_topRingLandHeight(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "topRingLandHeight","[m]",0,0.5); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_squishHeight() {
		double Quetschspalthoehe;		
		try {
			Quetschspalthoehe =set_doublePara(INPUTFILE_PARAMETER, "squishHeight","[m]",0,0.2);
			return Quetschspalthoehe;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}	
	

	/** 
	 * @return returns the piston pin offset  in [m].
	 * */
	public double get_pistonPinOffset(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "pistonPinOffset","[m]",-0.005,0.005); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}



	/** 
	 * @return returns the axial offset between piston pin and crank shaft in [m].
	 * */
	public double get_axialOffset(){		
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "axialOffset","[m]",-0.005,0.005); 
		} catch (ParameterFileWrongInputException e) {
			e.stopBremo();
			return Double.NaN;
		}		
	}




	/** 
	 * @return returns the time when the exhaust valve closes in [s] after start of calculation.
	 * */
	public double get_EVC(){		
		try {
			if(SYS.IS_CA_BASED){			
				double AV_S;
				AV_S =set_doublePara(INPUTFILE_PARAMETER, "EVC","[CAaTDCF]",get_Auslassoeffnet_KW(),SYS.CA_MIN);
				return convert_KW2SEC(AV_S);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "EVC","[s]",get_EVO(),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}	


	/** 
	 * @return Gibt den Zeitpunkt Auslassoeffnet  in [s] nach Rechenbeginn zurück.
	 * */
	public double get_EVO(){		
		try {
			if(SYS.IS_CA_BASED){					
				return  convert_KW2SEC(
						set_doublePara(INPUTFILE_PARAMETER, "EVO","[CAaTDCF]",SYS.CA_MAX,SYS.CA_MIN));

			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "EVO","[s]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}		
	}

	public double get_Auslassoeffnet_KW(){
		return convert_SEC2KW(get_EVO());
	}


	/** 
	 * @return returns the time when the inlet valve opens in [s] after start of calculation.
	 * */
	public double get_IVO(){		
		try {
			if(SYS.IS_CA_BASED){			
				return convert_KW2SEC(
						set_doublePara(INPUTFILE_PARAMETER, "IVO","[CAaTDCF]",SYS.CA_MAX,SYS.CA_MIN));

			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "IVO","[s]",SYS.SEC_UNTERGRENZE,SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}	

	}

	/** 
	 * @return returns the time when the inlet valve closes in [CA] after start of calculation.
	 * */
	public double get_IVO_CA(){
		return convert_SEC2KW(get_IVO());
	}

	/** 
	 * @return returns the time when the inlet valve closes in [s] after start of calculation.
	 * */
	public double get_IVC(){		
		try {
			if(SYS.IS_CA_BASED){			
				double AV_S;
				AV_S =set_doublePara(INPUTFILE_PARAMETER, "IVC","[CAaTDCF]",get_IVO_CA(),SYS.CA_MIN);
				return convert_KW2SEC(AV_S);	
			}else {			
				return set_doublePara(INPUTFILE_PARAMETER, "IVC","[s]",get_IVO(),SYS.SEC_OBERGRENZE);
			}	

		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}
	}
	
	/** 
	 * @return returns the inlet valve stroke in [m]. 
	 * With variable valve trains this value can differ from the maximum value!
	 * */
	public double get_IV_Stroke(){		
		double EV_Hub;		
		try {
			EV_Hub =set_doublePara(INPUTFILE_PARAMETER, "IV_Stroke","[m]",0,0.3);
			return EV_Hub;				
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
			return Double.NaN;
		}

	}

	/** 
	 * @return returns the maximum inlet valve stroke in [m].
	 * With variable valve trains this value can differ from the actual value!
	 * */
	public double get_IV_Stroke_max(){		
		double EV_Hub_max;		
		try {
			EV_Hub_max =set_doublePara(INPUTFILE_PARAMETER, "IV_Stroke_max","[m]",0,0.3);
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
			return set_doublePara(INPUTFILE_PARAMETER, "RefFlaecheAuslass","[m^2]",1e-6,100^0);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefFlaecheAuslass\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Kolbenflaeche\" gerechnet!");
			return get_pistonSurfaceArea();
		}		
	}
	/** 
	 * @return Gibt die Referenzfläche zurück, die für die
	 * Durchflusskennzahlen des Einlassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Kolbenfläche gerechnet.
	 * */
	public double get_ReferenzflaecheEinlass() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefFlaecheEinlass","[m^2]",1e-6,1000);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefFlaecheEinlass\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Kolbenflaeche\" gerechnet!");
			return get_pistonSurfaceArea();
		}		
	}


	/** 
	 * @return Gibt den Referenzdurchmesser zurück, der für die
	 * Durchflusskennzahlen des Einlassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Bohrung gerechnet.
	 * */
	public double get_ReferenzDurchmesserEV() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefDurchmesserEV","[m]",1e-6,1);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefDurchmesserEV\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Bohrung\" gerechnet!");
			return get_bore();
		}	
	}

	/** 
	 * @return Gibt den Referenzdurchmesser zurück, der für die
	 * Durchflusskennzahlen des Auslassventils verwendet werden soll.
	 * Wenn nichts angegeben wird, dann wird mit der Bohrung gerechnet.
	 * */
	public double get_ReferenzDurchmesserAV() {
		try {
			return set_doublePara(INPUTFILE_PARAMETER, "RefDurchmesserAV","[m]",1e-6,1);
		} catch (ParameterFileWrongInputException e) {
			e.log_Warning("Der Wert fuer \"RefDurchmesserAV\" wurde im Inputfile nicht angegeben. \n " +
					"Es wird mit \"Bohrung\" gerechnet!");
			return get_bore();
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
			throw new ParameterFileWrongInputException ("The value for " + paraNameToSet + " has to be set as a number!");			

		}catch(NullPointerException e){
			throw new ParameterFileWrongInputException ("I tried to set the parameter \"" +  paraNameToSet + 
					"\" but I can't find it in the inputFile!");	
		}

		//...und dann wird ueberprüft ob der Wert die richtige Einheit hat...
		for( int i=0;i<moeglicheEinheiten.length;i++){		
			if(einheit.equalsIgnoreCase(moeglicheEinheiten[i]))
				found=true;			
		}		
		if(!found){			
			String message="The unit for the parameter \"" +  paraNameToSet + "\" must be one of the following: ";

			for( int i=0;i<moeglicheEinheiten.length;i++){
				message="\n" + message+moeglicheEinheiten[i] ;						
			}	
			message=message+ "\nThe unit you sepcified is: " + einheit;

			throw new ParameterFileWrongInputException (message);
		}

		//...zum Schluss wird ueberprueft ob der Wert innerhalb der zulaessigen Grenzen liegt.
		if(!checkMinMax(temp,min,max)){
			throw new ParameterFileWrongInputException ("The value for the parameter \""	+ paraNameToSet + 
					"\" is not inside the valid range: \n" + min + "<" + paraNameToSet + "<" + max);					
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
			throw new ParameterFileWrongInputException ("I tried to set the parameter \"" +  paraNameToSet + 
					"\" but I can't find it in the inputFile!");	
		}			

		for( int i=0;i<possibleParaValues.length;i++){		
			//if(temp.equalsIgnoreCase(possibleParaValues[i]))
			if(temp.equals(possibleParaValues[i]))
				found=true;			
		}

		if(!found){			
			String message="For the parameter \"" +  paraNameToSet + "\" you have the following options: ";

			for( int i=0;i<possibleParaValues.length;i++){
				message="\n" + message+possibleParaValues[i]+" " ;						
			}	
			message=message+"\nBut you have chosen: "+ temp ;
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
		return(zeitpunktKW-SYS.SIMULATION_START_CA)/(360*get_rotSpeedInRotperSec());		 	
	}


	/**
	 * Liefert den Kurbelwinkel zur angegebenen Zeit
	 * @param time in [s nach Rechenbeginn]
	 * @return liefert die Kurbelwinkelstellung zum Zeitpunkt time
	 */
	public double convert_SEC2KW(double time){
		return get_rotSpeedInRotperSec()*360*time+SYS.SIMULATION_START_CA;	
	}


	public double convert_ProSEC_2_ProKW(double valueToconvert){
		double time=1;
		return valueToconvert/(get_rotSpeedInRotperSec()*360*time);
	}

	public double convert_ProKW_2_ProSEC(double valueToconvert){		
		return valueToconvert*(get_rotSpeedInRotperSec()*360);
	}


	public double convert_ProStunde_2_ProASP(double valueToconvert){
		double faktor=3600/(SYS.CYCLE_DURATION_CA/(360*get_rotSpeedInRotperSec()));
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
		public final double MINIMUM_ZONE_MASS;
		public final double T_FREEZE;

		public final double WRITE_INTERVAL_SEC;
		public final int NBR_OF_CALCULATED_VALUES;

		//je nach gewählter Rechnungsart in [s] oder [KW]
		public final double SIMULATION_START_CA;
		public final double SIMULATION_END_CA;
		public final double SIMULATION_START_SEC;
		public final double SIMULATION_END_SEC;
		public final double CA_MAX,CA_MIN,SEC_UNTERGRENZE, SEC_OBERGRENZE;

		public final double CYCLE_DURATION_CA;
		public final double CYCLE_DURATION_SEC;
		/**
		 * Liefert die Umdrehungen pro Arbeitsspiel
		 */
		public final double ROT_PER_CYCLE;

		public final boolean IS_CA_BASED;	
		public final boolean IS_TIME_BASED;
		public final boolean IS_TIME_BASED_START_IN_CA;
		
		public final String THERMO_POLYNOMIALS_COEFFS_USERCHOICE;
		public final String CHEM_EQUILIBRIUM_CONSTANTS_USERCHOICE;	


		public SysPara(Hashtable<String, String[]> parameterVorgaben) throws ParameterFileWrongInputException{

			PARAMETER=parameterVorgaben;				

			String [] yesno ={"yes" , "no"};	
			if(set_StringPara(PARAMETER,"debuggingMode",yesno ).equalsIgnoreCase("yes"))
				DUBUGGING_MODE=true;
			else
				DUBUGGING_MODE=false;

			MINIMUM_ZONE_MASS=set_doublePara(PARAMETER, "minimumZoneMass","[kg]",0,0.5); 

			//sollten sinnvolle Grenzen sein...Grill gibt 1600K an das alte Bremo verwendet 1700K
			T_FREEZE=set_doublePara(PARAMETER, "T_freeze","[K]",1400,1900);	
			
			
			

			String [] s2 ={"NASA9" , "Burcat", "ERC"};	
			THERMO_POLYNOMIALS_COEFFS_USERCHOICE=set_StringPara(PARAMETER,"polynomialCoeffs", s2);


			String [] s3 ={"Janaf","Burcat","Olikara"};	
			CHEM_EQUILIBRIUM_CONSTANTS_USERCHOICE=set_StringPara(PARAMETER,"chemEquilibriumConstants", s3);	


			////////////////////////////////////////////////////////////////
			//Einlesen von Daten die Abhaengig von anderen Eingaben sind////			
			////////////////////////////////////////////////////////////////
			String []s0 ={"2Stroke","4Stroke"};	
			if(set_StringPara(PARAMETER,"twoOrFourStroke",s0).equalsIgnoreCase("4Stroke")){
				CYCLE_DURATION_CA=720;
				ROT_PER_CYCLE=2;
			}else{
				CYCLE_DURATION_CA=360;
				ROT_PER_CYCLE=1;
			}		

			CA_MAX=-1*this.CYCLE_DURATION_CA; //gilt für die Definition aller KW Angaben in KWnZOT
			CA_MIN=this.CYCLE_DURATION_CA;

			//TODO Die Zeitbasierten grenzwerte richtig Definieren
			SEC_UNTERGRENZE=Double.NEGATIVE_INFINITY;
			SEC_OBERGRENZE=Double.POSITIVE_INFINITY;


			String []s ={"sec","sec_SimulationStartInCA", "CA"};		
			if(set_StringPara(PARAMETER,"timeOrCrankAngleBased",s).equalsIgnoreCase("CA")){
				IS_CA_BASED=true;
				IS_TIME_BASED=false;
				IS_TIME_BASED_START_IN_CA=false;

			}else if (set_StringPara(PARAMETER,"timeOrCrankAngleBased",s).equalsIgnoreCase("sec")){
				IS_CA_BASED=false;
				IS_TIME_BASED=true;
				IS_TIME_BASED_START_IN_CA=false;
				if(MODUL_VORGABEN.get(MotorFactory.MOTOR_FLAG)=="reciprocatingPistonEngine")
					throw new ParameterFileWrongInputException("Eine vollstaendig zeitbasierte Rechnung kann " +
							"nur mit einem filebasierten Motorobjekt und einem Eingabefile der Form [ZEIT] [ZYLINDERVOLUMEN] erfolgen." +
							"Alternativ ist \"sec_SimulationStartInCA\" zu wählen");

			}else{
				IS_CA_BASED=false;
				IS_TIME_BASED=false;
				IS_TIME_BASED_START_IN_CA=true;
			}				

			String timeStepSize="timeStepSize";
			String simulationStart="simulationStart";
			String simulationEnd ="simulationEnd";
			if(IS_CA_BASED){			
				double WRITE_INTERVAL_CA=set_doublePara(PARAMETER, timeStepSize,"[CA]",1e-6,2);	
				//TODO Grenzwerte mittels Einlassoeffnet und Auslassschließt definieren. 
				//Fuer die Ladungswechselanalyse werden eigene Rechenbeginne und Rechenenden definiert.
				WRITE_INTERVAL_SEC= WRITE_INTERVAL_CA/(360*get_rotSpeedInRotperSec());

				SIMULATION_START_CA=set_doublePara(PARAMETER, simulationStart,"[CAaTDCF]",
						CA_MAX,CA_MIN);				

				SIMULATION_END_CA=set_doublePara(PARAMETER, simulationEnd,"[CAaTDCF]",
						SIMULATION_START_CA,CA_MIN);		
				//der Anfang zaehlt mit
				NBR_OF_CALCULATED_VALUES=(int)((SIMULATION_END_CA-SIMULATION_START_CA)/WRITE_INTERVAL_CA)+1;

				SIMULATION_END_SEC=(NBR_OF_CALCULATED_VALUES-1)*WRITE_INTERVAL_SEC;			

				SIMULATION_START_SEC=0;				

				if(DUBUGGING_MODE){
					double b=set_doublePara(PARAMETER, "debuggingTime","[CAaTDCF]",SIMULATION_START_CA,SIMULATION_END_CA+CYCLE_DURATION_CA);

					DUBUGGING_TIME_SEC=(b-SIMULATION_START_CA)/(360*get_rotSpeedInRotperSec());	

				}else{
					DUBUGGING_TIME_SEC=SIMULATION_START_SEC;
				}


			}else if(IS_TIME_BASED){
				//TODO um die gesamte zeitbasierte sache muss sich noch jemand kuemmern!!!

				//min-Wert --> ca. 0.01°KW bei 18000U/min
				//max-Wert --> ca .2°KW bei 50 U/min
				WRITE_INTERVAL_SEC=set_doublePara(PARAMETER, timeStepSize,"[s]",9.5e-7,6.7e-3);

				//muss eigentlich nur passend zum MotorFile und DruckFile sein
				SIMULATION_START_SEC=set_doublePara(PARAMETER, simulationStart,"[s]",
						Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);				

				SIMULATION_END_SEC=set_doublePara(PARAMETER, simulationEnd,"[s]",
						SIMULATION_START_SEC,Double.POSITIVE_INFINITY);		

				NBR_OF_CALCULATED_VALUES=(int)(( SIMULATION_END_SEC-SIMULATION_START_SEC)/WRITE_INTERVAL_SEC)+1;

				//final Variablen muessen mit Werten belegt werden
				SIMULATION_START_CA=555.555;
				SIMULATION_END_CA=555.555;

				if(DUBUGGING_MODE){
					DUBUGGING_TIME_SEC=set_doublePara(PARAMETER, "debuggingTime","[s]",SIMULATION_START_SEC,SIMULATION_END_SEC);
				}else{
					DUBUGGING_TIME_SEC=SIMULATION_START_SEC;
				}				

			}else if(IS_TIME_BASED_START_IN_CA){
				//min-Wert --> ca. 0.01°KW bei 18000U/min
				//max-Wert --> ca .2°KW bei 50 U/min
				WRITE_INTERVAL_SEC=set_doublePara(PARAMETER, timeStepSize,"[s]",9.5e-5,6.7e-3);

				SIMULATION_START_CA=set_doublePara(PARAMETER, simulationStart,"[CAaTDCF]",
						CA_MAX,CA_MIN);	

				SIMULATION_START_SEC=0;	

				SIMULATION_END_SEC=set_doublePara(PARAMETER, simulationEnd,"[s]",
						SIMULATION_START_SEC,Double.POSITIVE_INFINITY);	
				//die Zeitrechnung beginnt hier bei null!
				NBR_OF_CALCULATED_VALUES=(int)(SIMULATION_END_SEC/WRITE_INTERVAL_SEC)+1;

				SIMULATION_END_CA=360*get_rotSpeedInRotperSec()*SIMULATION_END_SEC;		

				if(DUBUGGING_MODE){
					DUBUGGING_TIME_SEC=set_doublePara(PARAMETER, "debuggingTime","[s]",SIMULATION_START_SEC,SIMULATION_END_SEC);
				}else{
					DUBUGGING_TIME_SEC=SIMULATION_START_SEC;
				}				
			}else{
				SIMULATION_START_SEC=0;
				SIMULATION_END_SEC=0;
				NBR_OF_CALCULATED_VALUES=-1;

				throw new ParameterFileWrongInputException("The flags: \"IS_TIME_BASED_START_IN_CA\", \" IS_TIME_BASED\" " +
						"oder \"IS_CA_BASED\" have not been specified in the correct way! ");			
			}

			if((IS_TIME_BASED && SIMULATION_END_SEC<SIMULATION_START_SEC )|| (IS_CA_BASED && SIMULATION_END_SEC<SIMULATION_START_SEC)||
					(IS_TIME_BASED_START_IN_CA && SIMULATION_END_CA<SIMULATION_START_CA ))
				throw new ParameterFileWrongInputException("The end of simulation is before the start of simulation!");			


			CYCLE_DURATION_SEC=(CYCLE_DURATION_CA)/(360*get_rotSpeedInRotperSec());

		}//Konstruktor SysPara	

	}//Klasse SysPara

}//Klasse CasePara
