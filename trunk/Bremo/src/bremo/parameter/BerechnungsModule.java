//package bremo.parameter;
//
//import java.util.Hashtable;
//
//import berechnungsModule.Berechnung.DVA;
//import berechnungsModule.Berechnung.DVA_DualFuel;
//import berechnungsModule.Berechnung.DVA_Homogen_EinZonig;
//import berechnungsModule.Berechnung.DVA_homogen_ZweiZonig;
//import berechnungsModule.gemischbildung.Einspritzung;
//import berechnungsModule.gemischbildung.MasterEinspritzung;
//import berechnungsModule.gemischbildung.SaugrohrEinspritzungHomogen;
//import berechnungsModule.gemischbildung.SimpleDirektEinspritzung;
//import berechnungsModule.gemischbildung.Spray;
//import berechnungsModule.internesRestgas.InternesRestgas;
//import berechnungsModule.internesRestgas.LWA;
//import berechnungsModule.internesRestgas.RestgasVorgabe;
//import berechnungsModule.motor.Motor;
//import berechnungsModule.motor.Motor_HubKolbenMotor;
//import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
//import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner_Grill;
//import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner_Olikara_Borman;
//import berechnungsModule.wandwaerme.Chang;
//import berechnungsModule.wandwaerme.Hensel;
//import berechnungsModule.wandwaerme.Hohenberg;
//import berechnungsModule.wandwaerme.PerfektIsoliert;
//import berechnungsModule.wandwaerme.WandWaermeUebergang;
//import berechnungsModule.wandwaerme.Woschni;
//import berechnungsModule.wandwaerme.WoschniHuber;
//import bremoExceptions.BirdBrainedProgrammerException;
//import bremoExceptions.ParameterFileWrongInputException;
//
//public class BerechnungsModule {
//	private final CasePara cp;
//
//
//	//DVA
//	public static final String DVAmodulFlag="berechnungsModell";
//	public static final String [] DVA_MODULE={"DVA_1Zonig", "DVA_2Zonig","DVA_DualFuel"};
//	public  final DVA DVA_MODUL;
//
//	//Motor
//	public static final String MOTOR_FLAG="Motor";
//	public static final  String[] MOTORENTYPEN={"HubKolbenMotor" , "ausDatei"};
//	/**
//	 *  Liefert ein Objekt vom Typ Motor zurück. Die Art des Motors 
//	 *  entspricht der Auswahl im InputFile.
//	 */
//	public final Motor MOTOR_MODUL;
//
//	//WandWaerme
//	public static final String WANDWAERME_FLAG="Wandwaermemodell";
//	private static final String WANDWAERME_FLAG_LW="Wandwaermemodell_LW";
//	public  static final String[] WANDWAERMEMODELLE
//	={"WoschniHuber", "Woschni", "Hohenberg", "Hans", "Bargende", "Chang","ohne"};
//	public final  WandWaermeUebergang WAND_WAERME_MODUL;
//	public final  WandWaermeUebergang WAND_WAERME_MODUL_LW;
//	
//	//Restgas
//	public static final String INTERNES_RESTGAS_MODELL_FLAG="internesRestgasModell";
//	public  final static String[] INTERNES_RESTGAS_MODELLE={"RestgasVorgabe", 
//															"MuellerBertling",
//															"LWA"	};	
//	public final InternesRestgas INTERNES_RESTGAS_MODUL;
//	
//	//ohc-Solver
//	public static final String OHC_SOLVER_FLAG="OHC_Solver";
//	public final static String[] OHC_SOLVER={"OlikaraBorman" , "Grill"};
//	public final GleichGewichtsRechner OHC_SOLVER_MODUL;
//	
//	//Einspritzung
//	public static final String EINSPRITZ_MODELL_FLAG="einspritzModell_"; //gibt den Eintrag im inputfile an
//	public  final static String[] MOEGLICHE_EINSPRITZ_MODELLE={SaugrohrEinspritzungHomogen.FLAG,
//															SaugrohrEinspritzungHomogen.FLAG2, 
//															Spray.FLAG,
//															SimpleDirektEinspritzung.FLAG};	
//	
//	public final MasterEinspritzung  MASTER_EINSPRITZUNG;
//
//
//
//	private final Hashtable<String,String> MODUL_VORGABEN;
//
//	public BerechnungsModule(Hashtable<String,String> modul_vorgaben, CasePara cp){
//		MODUL_VORGABEN=modul_vorgaben;
//		this.cp=cp;
//
//		///////////////////////////////////////////////////////////////////////
//		/////////////////////////////DVA-Modul/////////////////////////////////
//		///////////////////////////////////////////////////////////////////////		
//		DVA_MODUL=initializeDVAModul();
//
//		///////////////////////////////////////////////////////////////////////
//		/////////////////////////////Motor-Modul///////////////////////////////
//		///////////////////////////////////////////////////////////////////////
//		MOTOR_MODUL=initializeMotorModul();
//
//		///////////////////////////////////////////////////////////////////////
//		///////////////////////////WandWaerme-Modul////////////////////////////
//		///////////////////////////////////////////////////////////////////////
//		WAND_WAERME_MODUL=initializeWandWaermeModul(WANDWAERME_FLAG, WANDWAERMEMODELLE);
//		
//		///////////////////////////////////////////////////////////////////////
//		///////////////////////////WandWaerme-Modul////////////////////////////
//		///////////////////////////////////////////////////////////////////////
//		WAND_WAERME_MODUL_LW=initializeWandWaermeModul(WANDWAERME_FLAG_LW, WANDWAERMEMODELLE);
//
//		///////////////////////////////////////////////////////////////////////
//		///////////////////////////InternesRestgas-Modul///////////////////////
//		///////////////////////////////////////////////////////////////////////
//		INTERNES_RESTGAS_MODUL=initializeInternesRestgas();
//		
//		///////////////////////////////////////////////////////////////////////
//		///////////////////////////InternesRestgas-Modul///////////////////////
//		///////////////////////////////////////////////////////////////////////
//		OHC_SOLVER_MODUL=initializeGleichGewichtsRechner();
//
//	}
//	
//	
//	private MasterEinspritzung initializeMasterEinspritzung(){
//		int anzEinspr=cp.get_AnzahlEinspritzungen(); //untere Grenze liegt bei eins!
//
//		Einspritzung [] einspritzungen= new Einspritzung [anzEinspr];	
//		for(int index=0;index<anzEinspr;index++){
//			String modulFlag=EINSPRITZ_MODELL_FLAG+index;
//			einspritzungen[index]=Einspritzung.get_Einzeleinspritzung(cp, index+1,modulFlag); //Im Inputfile beginnen die Einspritzugnen mit eins!
//		}
//		
//	}
//	
//	private  Einspritzung get_Einzeleinspritzung(CasePara cp, int index,String modulFlag){
//		Einspritzung einspritzung=null;	
//		
//		 String einspritzungsModellVorgabe=
//			get_ModulWahl(modulFlag, MOEGLICHE_EINSPRITZ_MODELLE);
//
//		if(einspritzungsModellVorgabe.equals(SaugrohrEinspritzungHomogen.FLAG)|| 
//				einspritzungsModellVorgabe.equals(SaugrohrEinspritzungHomogen.FLAG2)){
//			
//			einspritzung=(Einspritzung) new SaugrohrEinspritzungHomogen(cp, index);
//
//		}else if(einspritzungsModellVorgabe.equals(Spray.FLAG)){
//			einspritzung=new Spray(cp, index);	
//			
//		}else if(einspritzungsModellVorgabe.equals(SimpleDirektEinspritzung.FLAG)){			
//			einspritzung=new SimpleDirektEinspritzung(cp, index);			
//		}
//
//		if(einspritzung==null){
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Das Einspritzungsmodell \"" +einspritzungsModellVorgabe + 
//						" \" wurde im InputFile " +
//						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n" +
//				"Es fehlt der entsprechende else-if-Block oder das  Modell wurde noch nicht implementiert");
//			} catch (BirdBrainedProgrammerException e) {
//				e.stopBremo();
//			}
//		}
//
//		return einspritzung;		
//	}	
//	
//	private  GleichGewichtsRechner initializeGleichGewichtsRechner() {
//		GleichGewichtsRechner ohcSolver=null;
//		if(ohcSolver==null){
//
//			String ohcSolverVorgabe=get_ModulWahl(OHC_SOLVER_FLAG, OHC_SOLVER);
//	
//			if(ohcSolverVorgabe.equals(OHC_SOLVER[0]))	//"OlikaraBorman"
//				ohcSolver=
//					new GleichGewichtsRechner_Olikara_Borman(cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
//			else if(ohcSolverVorgabe.equals(OHC_SOLVER[1])) // "Grill"
//				ohcSolver=
//					new GleichGewichtsRechner_Grill(cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
//	
//			else if (ohcSolver==null){
//				try {
//					throw new BirdBrainedProgrammerException(
//							"Der ausgewaehlte OHC_Solver \"" +ohcSolverVorgabe + " \" wurde im InputFile " +
//							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
//							"Es fehlt der entsprechende else-if-Block  oder er wurde noch nicht implementiert\n" +
//							"Verwendet wird Grill");
//				} catch (BirdBrainedProgrammerException e) {
//					e.log_Message();
//					ohcSolver=
//						new GleichGewichtsRechner_Grill(cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
//				}
//			}
//		}
//		return ohcSolver;		
//	}	
//	
//	private  InternesRestgas initializeInternesRestgas(){
//		InternesRestgas restgasModell=null;
//		
//		if(restgasModell==null){
//			String internesRestgas_ModellVorgabe=
//				get_ModulWahl(INTERNES_RESTGAS_MODELL_FLAG, INTERNES_RESTGAS_MODELLE);
//			if(internesRestgas_ModellVorgabe.equals("RestgasVorgabe")){
//				restgasModell=(InternesRestgas) new RestgasVorgabe(cp);
//				
//			}else if(internesRestgas_ModellVorgabe.equals("MuellerBertling")){
////				restgasModell=(InternesRestgas) new MuellerBertling(cp);
//				
//			}else if(internesRestgas_ModellVorgabe.equals("LWA")){
//				restgasModell=(InternesRestgas) new LWA(cp);
//			}			
//
//			if(restgasModell==null){
//				try {
//					throw new BirdBrainedProgrammerException(
//							"Das ausgewaehlte Restgasmodell \"" +internesRestgas_ModellVorgabe + 
//							" \" wurde im InputFile " +
//							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
//					"Es fehlt der entsprechende else-if-Block oder das  Restgasmodell wurde noch nicht implementiert");
//				} catch (BirdBrainedProgrammerException e) {
//					e.stopBremo();
//				}
//			}
//		}
//		return restgasModell;		
//	}
//	
//	
//	private DVA initializeDVAModul(){
//		String berechnungsModellVorgabe=get_ModulWahl(DVAmodulFlag, DVA_MODULE);
//		DVA dva=null;
//		if(berechnungsModellVorgabe.equals("DVA_1Zonig")){			
//			dva=new DVA_Homogen_EinZonig(cp);
//
//		}else if(berechnungsModellVorgabe.equals("DVA_2Zonig")){
//			dva=new DVA_homogen_ZweiZonig(cp);	
//
//		}else if(berechnungsModellVorgabe.equals("DVA_DualFuel")){			
//			dva=new DVA_DualFuel(cp);		
//		}else{
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Das DVA-Modell \"" +berechnungsModellVorgabe + 
//						" \" wurde im InputFile " +
//						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n" +
//				"Es fehlt der entsprechende else-if-Block oder das  BerechnuingsModell wurde noch nicht implementiert");
//			} catch (BirdBrainedProgrammerException e) {
//				e.stopBremo();
//			}
//			dva=null;
//		}
//
//		return dva;	
//	}
//
//
//	private Motor initializeMotorModul(){
//		Motor m=null;
//		String motorVorgabe=get_ModulWahl(MOTOR_FLAG, MOTORENTYPEN);
//
//		if(motorVorgabe.equals("HubKolbenMotor"))
//			m=new Motor_HubKolbenMotor(cp) ;
//		else if(motorVorgabe.equals("ausDatei"))
//			m=null;
//		else if(motorVorgabe.equals("DruckKammer"))
//			m=null;
//		else{
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Der ausgewaehlte Motroentyp \"" +motorVorgabe + " \" wurde im InputFile " +
//						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n "  + 
//				"Es fehlt der entsprechende else-if-Block oder der Motortyp wurde noch nicht implementiert");
//			} catch (BirdBrainedProgrammerException e) {
//				e.stopBremo();
//			}
//			m=null;
//		}
//		return m;		
//	}	
//
//
//
//	private WandWaermeUebergang initializeWandWaermeModul(String flag,String [] modelle){
//		String wandwaermemodellVorgabe=get_ModulWahl(flag,modelle);
//		WandWaermeUebergang temp = null;
//		try{
//			if(wandwaermemodellVorgabe.equals("WoschniHuber")){
//				if(this.MOTOR_MODUL.isHubKolbenMotor()){
//					temp=new WoschniHuber(cp);
//				}else{					
//					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
//							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
//					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
//				}
//			}
//			else if(wandwaermemodellVorgabe.equals("Woschni")){
//				if(this.MOTOR_MODUL.isHubKolbenMotor()){
//					temp=new Woschni(cp);						
//				}else{
//					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
//							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
//					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
//				}
//			}
//			else if(wandwaermemodellVorgabe.equals("Hohenberg")){
//				if(this.MOTOR_MODUL.isHubKolbenMotor()){
//					temp=new Hohenberg(cp);						
//				}else{
//					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
//							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
//					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
//				}
//			}
//			else if(wandwaermemodellVorgabe.equals("Chang")){
//				if(this.MOTOR_MODUL.isHubKolbenMotor()){
//					temp=new Chang(cp);
//				}else{
//					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
//							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
//					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
//				}
//			}
//			else if(wandwaermemodellVorgabe.equals("Hans")){
//				if(this.MOTOR_MODUL.isHubKolbenMotor()){
//					temp=new Hensel(cp);
//				}else{
//					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
//							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
//					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
//				}
//			}
//			else if(wandwaermemodellVorgabe.equals("Bargende")){	
//				if(this.MOTOR_MODUL.isHubKolbenMotor()){
//
//					temp=null;// TODO Konstruktor des Wandwaermemodells einfuegen --> is das Modell schon implementiert
//				}else{
//					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
//							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
//					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
//				}
//			}
//			else if(wandwaermemodellVorgabe.equals("ohne")){
//				temp=new PerfektIsoliert(cp);				
//			}				
//		}
//		catch(ParameterFileWrongInputException e){
//			e.log_Warning();
//			temp=new PerfektIsoliert(cp);	
//		}
//
//		if(temp==null){			
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Das ausgewaehlte Waermeuebergangsmodell \"" + wandwaermemodellVorgabe + "\" wurde im InputFile " +
//						"zwar als valide akzeptiert, \nist im Programm aber nicht richtig eingepflegt worden. \n" +
//						"Es fehlt der entsprechende else-if-Block in der WandwaermeBasisKlasse" +
//						" oder es wurde noch nicht implementiert \n" +
//				"Die Rechnung erfolgt ohne Wandwaermeuebergangsmodell");
//			} catch (BirdBrainedProgrammerException e) {				
//				e.log_Warning();
//			}
//			temp=new PerfektIsoliert(cp);			
//		}
//		return temp;		
//	}
//
//
//	private  String get_ModulWahl(String modulFlag, String [] moeglicheModelle){			
//
//		if(!MODUL_VORGABEN.containsKey(modulFlag)){			
//			try {
//				String message="Im Inputfile wurde kein gueltiges \""+ modulFlag +"\"-Modul angegeben \n" +
//				"Die Definition erfolgt z.B. so: \n ["+ modulFlag + ": ModulName]";
//				throw new ParameterFileWrongInputException(message);
//			} catch (ParameterFileWrongInputException e) {				
//				e.stopBremo();
//			}
//		}			
//
//		String modul=null;
//		boolean found=false;
//
//		modul= MODUL_VORGABEN.get(modulFlag);
//		int u=0;
//
//		while(u<moeglicheModelle.length && found==false){
//			if(modul.equals(moeglicheModelle[u]))
//				found=true;
//			u++;			
//		}	
//
//		if(!found){
//			try {
//				String message="Fuer das \""+modulFlag+"\"-Modul wurde folgender Wert im InputFile angegeben: \"" +
//				modul+"\". Dies ist keine gueltige Auswahl! \n" +
//				"Moegliche Module sind: \n";
//
//				for( int i=0;i<moeglicheModelle.length;i++){
//					message= message+moeglicheModelle[i] + " \n" ;						
//				}			
//				throw new ParameterFileWrongInputException(message);
//			} catch (ParameterFileWrongInputException e) {				
//				e.stopBremo();
//			}
//		}
//		return modul;
//	}
//
//
//}
