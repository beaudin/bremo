package bremo.main;

import io.FileWriter_txt;
import io.SimpleTXTFileReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observer;

import javax.swing.JOptionPane;

import matLib.ConstantsOptimizer;
import matLib.MatLibBase;
import matLib.Optimizer;
import KivaPostProcessing.KivaPostProcessor;
import berechnungsModule.IterativeBerechnung;
import berechnungsModule.Berechnung.CanteraCaller;
import berechnungsModule.Berechnung.Verlustteilung; //fuer Verlustteilung Frank Haertel
import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.graphik.Observable;
import bremoswing.manager.ManagerLanguage;
import bremoswing.util.BremoInfoFrame;

/**
 * @author eichmeier
 * @author Ngueneko
 * 
 */
public class Bremo extends Thread implements Observable {

	private CasePara casePara;
	private Rechnung r;
	private Verlustteilung v;
	private File inputFile;
	private boolean caseParaerzeugt = false;
	private boolean calledFromGUI;
	public static String LOCALE_GERMANY_LANGUAGE = "de, DE";
	public static String LOCALE_ENGLISH_LANGUAGE = "en, EN";

	private Observer observer;

	public final String STRING_VerlustTeilung = "verlustteilungsprozess lauft ...";
	public final String STRING_CalculEnd = "berechnungsprozess";
	public final String STRING_StopBremo = "stopsprozess";
	private final String STRING_SetBremoAsLife = "SetBremoAsLife";
	private final String STRING_SetDebbugingMode = "SetDebbugingMode";
	private final String STRING_PutInBremoThreadFertig = "PutInBremoThreadFertig";
	private final String STRING_FinishTime = "FinishTime";

	public Bremo(File inputFile) {
		this(inputFile, false);
	}

	public Bremo(File inputFile, boolean calledFromGUI) {
		super(inputFile.getName());
		this.inputFile = inputFile;
		this.calledFromGUI = calledFromGUI;
	}

	public Bremo() throws ParameterFileWrongInputException {
		System.out.println("Juchuu, es klappt");
		System.out.println("Checkt");
		// CPPCaller cppC=new CPPCaller();
		// cppC.sagMalHallo();
		// String a="ERCmech.cti";
		// CanteraCaller cc=new CanteraCaller(a,"gas",1);
		// test_BremoCanteraSpecies2();
		// File fileCP
		// // = new File("src//InputFiles//FunktionstesterCT.txt");
		// = new
		// File("c://Documents and Settings//eichmeier//jniWorkspace2//CallMeFromMatlab//src//InputFiles//FunktionstesterCT.txt");
		// CasePara privateCP = new CasePara(fileCP);
		// FunktionsTester ft=new FunktionsTester(privateCP);
		// ft.testCantera3Zone();

		File fileCP
		// = new File("src//InputFiles//FunktionstesterCT.txt");
		= new File(
				"c://Documents and Settings//eichmeier//jniWorkspace2//CallMeFromMatlab//src//InputFiles//Mode3//mode3.txt");
		Bremo bremo = new Bremo(fileCP, false);
		bremo.run();
		bremo = null;
	}

	public Bremo(String absolutePath2InputFile) {
		this(new File(absolutePath2InputFile), false);
		this.run();
	}

	/**
	 * Erzeugt eine Instanz vom Typ CasePara und startet die Berechnung
	 * 
	 * @see CasePara
	 */
	@Override
	public void run() {

		try {
			IterativeBerechnung iterRechnung = new IterativeBerechnung(
					inputFile);
			do {
				initialisierungProzess(iterRechnung);

				berechnungProzess(iterRechnung);

			} while (iterRechnung.isIterativ());

			iterRechnung.deleteFile();

			verlustteilungProzess();

		} catch (ParameterFileWrongInputException e) {

			if (calledFromGUI) {

				notifyObserver(STRING_SetBremoAsLife);

				new BremoInfoFrame(this.getName(), "<html>" + "<u>"
						+ ManagerLanguage.getString("thread") + "</u> : "
						+ ManagerLanguage.getString("bremo_error_message_1")
						+ "<b>" + this.getName() + "</b> "
						+ ManagerLanguage.getString("bremo_error_message_2")
						+ " <p> \n " + e.getMessage() + "</p>" + "</html>",
						JOptionPane.ERROR_MESSAGE);

			}
			e.printStackTrace();
			e.stopBremo();
		}
	}

	private void initialisierungProzess(IterativeBerechnung iterRechnung)
			throws ParameterFileWrongInputException {
		casePara = new CasePara(inputFile);
		caseParaerzeugt = true;
		casePara.set_CalledFromGUI(calledFromGUI);
		if(calledFromGUI)
			notifyObserver(STRING_SetDebbugingMode);
		iterRechnung.initialisieren(casePara);
		casePara.set_IterativeBerechnung(iterRechnung);
		r = new Rechnung(casePara);
	}

	private void berechnungProzess(IterativeBerechnung iterRechnung) {

		casePara.set_CalledFromGUI(calledFromGUI);
		r.berechnungDurchfuehren();
		iterRechnung.auswerten();
		if (calledFromGUI && !iterRechnung.isIterativ()) {
			List<String> message = new ArrayList<String>();
			message.add(this.getName());
			message.add(STRING_CalculEnd);
			message.add(STRING_PutInBremoThreadFertig);
			notifyObserver(message);

		}
	}

	private void verlustteilungProzess() {

		if (casePara.is_Verlustteilung()
				&& !casePara.get_CaseName().toString().contains("Weltformel")) {

			if (calledFromGUI) {
				List<String> message = new ArrayList<String>();
				message.add(this.getName());
				message.add(STRING_VerlustTeilung);
				message.add("");
				notifyObserver(message);

				v = new Verlustteilung(casePara);
				v.berechneVerluste();
				// SwingBremo.label.setText(ManagerLanguage
				// .getString("swingbremo_label_13"));
				if (calledFromGUI) {
					notifyObserver(STRING_FinishTime);
				}
			} else {
				if (calledFromGUI) {
					notifyObserver(STRING_FinishTime);
				}
			}
		}
	}

	/**
	 * Gibt ein Objekt vom Typ CasePara zurueck. Wenn diese noch nicht erzeugt
	 * sind wird ein Fehler ausgegeben.
	 * 
	 * @return CasePara
	 * @see CasePara
	 */
	public CasePara get_myCase() {
		if (caseParaerzeugt)
			return casePara;
		else {
			try {
				throw new BirdBrainedProgrammerException(
						"Es wurde versucht auf die Klasse CasePara zuzugreifen. "
								+ "Diese wurde aber noch nicht erzeugt. Volldeppprogrammierer");
			} catch (BirdBrainedProgrammerException e) {
				// e.stopBremo();
			}
			return null;
		}
	}

	/**
	 * Gibt an ob eine Instanz vom Typ CasePara bereits erzeugt wurde
	 * 
	 * @return
	 * @see CasePara
	 */
	public boolean get_myCaseState() {

		return caseParaerzeugt;
	}

	/**
	 * Gibt die InputFile Von Bremo
	 * 
	 * @return inpuFile
	 */
	public File get_myFile() {

		return inputFile;
	}

	@Override
	public void addObserver(Observer obs) {
		observer = obs;

	}

	@Override
	public void deleteObserver() {
		observer = null;

	}

	@Override
	public Observer getObserver() {

		return observer;
	}

	public void notifyObserver(String str) {
		observer.update(null, str);
	}

	public void notifyObserver(List<String> str) {
		observer.update(null, str);
	}

	public void forceToStop() {
		if (r != null) {
			r.destroy();
		} else if (v != null) {
			v.destroy();
		} else {
			casePara = null;
		}
		// r.stop();
		// v.stop();

	}

	/**
	 * Main-Methode um Bremo ohne das GUI aufzurufen.
	 * 
	 * @param args
	 *            Pfad der auf das InputFile verweist
	 * @throws ParameterFileWrongInputException
	 */
	public static void main(String[] args)
			throws ParameterFileWrongInputException {

		ManagerLanguage.managerLanguage(new Locale(LOCALE_GERMANY_LANGUAGE));

		long begTest = new java.util.Date().getTime();
		System.out.println(System.getProperty("java.library.path"));
		// Um Funktionen zu testen gibt es die Klasse FunktionsTester
		// Hier einige Beisspile wie Funktionen getestet werden können
		// System.out.println(System.getProperty("home"));
		// File fileCP = new File(
		// "D://Daten//Bremo//Referenzmessungen//iterativ//bremo_setup_140702_00002_zyklus0.txt");
		// // TODO
		// Filename
		// File fileCP = new
		// File("D://Daten//Studenten//Carolin Sturm//Studentenordner//von_philipp//BREMO//20140205//05_neu//PhH//Inputfile_P_202_Huegel_20140205_0005-p_m.txt");
		// File fileCP = new
		// File("D://Daten//workspace//Bremo 2_DE//src//InputFiles//PhH//20140225//19//Inputfile_P_202_Huegel_20140225_0019-p_m.txt");
		// //TODO Filename
		// File fileCP = new
		// File("D://Daten//workspace//Bremo 2_DE//src//InputFiles//PhH//Messpunkt_10_Schichtreferenz//Bremo_Inputfile_Messpunkt_99.txt");
		// File fileCP = new
		// File("d://Daten//FVV_CFD_BSZ_II//Auswertung//Bremo//140123//SIS_Z2_5_7//"+
		// "bremo_setup_140123_00009_zyklus163.txt"); //TODO Filename
		// File fileCP = new
		// File("d://Daten//bremo//java_ws//bremo 2_de//src//inputfiles//apr//bremo_setup.txt");
		// double startTime = System.currentTimeMillis();
		// Bremo bremo = new Bremo(fileCP, false);
		// bremo.run();
		// double finishTime = System.currentTimeMillis();
		// bremo = null;
		// System.out.println(finishTime - startTime + " ms");
		// // FunktionsTester.testVerlustteilung(bremo); //fuer Verlustteilung
		// Frank Haertel
		// File fileCP = new
		// File("src//InputFiles//Mode7_MultiZoneInitFromKiva//mode7InpFromKiva_0.txt");
		// CasePara privateCP = new CasePara(fileCP);
		// FunktionsTester ft=new FunktionsTester(privateCP);
		// ft.estimateEGR();
		// ft.getInitialConditions_2();
		// ft.testDieselMassFracion();

		String[] inputFileNames;
		if (args.length > 0)
			inputFileNames = args;
		else {
			String[] ifn = {
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus4.txt"	, 	// FEHLER, erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus7.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus22.txt"	, 	// FEHLER, whtfMult auf 1.57 erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus33.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus49.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus60.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus61.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus63.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus80.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus81.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus89.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus96.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus119.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus121.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus145.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus150.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus177.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus186.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus205.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus216.txt"	, 	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus224.txt" ,	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus1.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus2.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus3.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus5.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus6.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus8.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus9.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus10.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus11.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus12.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus13.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus14.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus15.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus16.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus17.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus18.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus19.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus20.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus21.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus23.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus24.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus25.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus26.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus27.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus28.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus29.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus30.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus31.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus32.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus34.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus35.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus36.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus37.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus38.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus39.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus40.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus41.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus42.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus43.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus44.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus45.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus46.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus47.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus48.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus50.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus51.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus52.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus53.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus54.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus55.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus56.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus57.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus58.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus59.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus62.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus64.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus65.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus66.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus67.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus68.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus69.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus70.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus71.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus72.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus73.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus74.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus75.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus76.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus77.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus78.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus79.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus82.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus83.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus84.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus85.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus86.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus87.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus88.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus90.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus91.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus92.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus93.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus94.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus95.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus97.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus98.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus99.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus100.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus101.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus102.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus103.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus104.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus105.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus106.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus107.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus108.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus109.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus110.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus111.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus112.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus113.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus114.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus115.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus116.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus117.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus118.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus120.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus122.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus123.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus124.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus125.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus126.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus127.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus128.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus129.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus130.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus131.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus132.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus133.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus134.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus135.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus136.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus137.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus138.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus139.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus140.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus141.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus142.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus143.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus144.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus146.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus147.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus148.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus149.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus151.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus152.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus153.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus154.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus155.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus156.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus157.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus158.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus159.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus160.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus161.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus162.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus163.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus164.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus165.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus166.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus167.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus168.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus169.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus170.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus171.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus172.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus173.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus174.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus175.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus176.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus178.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus179.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus180.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus181.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus182.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus183.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus184.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus185.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus187.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus188.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus189.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus190.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus191.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus192.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus193.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus194.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus195.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus196.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus197.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus198.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus199.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus200.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus201.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus202.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus203.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus204.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus206.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus207.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus208.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus209.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus210.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus211.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus212.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus213.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus214.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus215.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus217.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus218.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus219.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus220.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus221.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus222.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus223.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus225.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus226.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus227.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus228.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus229.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus230.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus231.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus232.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00001_zyklus233.txt",

					
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus1.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus10.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus100.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus101.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus102.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus103.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus104.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus105.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus106.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus107.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus108.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus109.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus11.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus110.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus111.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus112.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus113.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus114.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus115.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus116.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus117.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus118.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus119.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus12.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus120.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus121.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus122.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus123.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus124.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus125.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus126.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus127.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus128.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus129.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus13.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus130.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus131.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus132.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus133.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus134.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus135.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus136.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus137.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus138.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus139.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus14.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus140.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus141.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus142.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus143.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus144.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus145.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus146.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus147.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus148.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus149.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus15.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus150.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus151.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus152.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus153.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus154.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus155.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus156.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus157.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus158.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus159.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus16.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus160.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus161.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus162.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus163.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus164.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus165.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus166.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus167.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus168.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus169.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus17.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus170.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus171.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus172.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus173.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus174.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus175.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus176.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus177.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus178.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus179.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus18.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus180.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus181.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus182.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus183.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus184.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus185.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus186.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus187.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus188.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus189.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus19.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus190.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus191.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus192.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus193.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus194.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus195.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus196.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus197.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus198.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus199.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus2.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus20.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus200.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus201.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus202.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus203.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus204.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus205.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus206.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus207.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus208.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus209.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus21.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus210.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus211.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus212.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus213.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus214.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus215.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus216.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus217.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus218.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus219.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus22.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus220.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus221.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus222.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus223.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus224.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus225.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus226.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus23.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus24.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus25.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus26.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus27.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus28.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus29.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus3.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus30.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus31.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus32.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus33.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus34.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus35.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus36.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus37.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus38.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus39.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus4.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus40.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus41.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus42.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus43.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus44.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus45.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus46.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus47.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus48.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus49.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus5.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus50.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus51.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus52.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus53.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus54.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus55.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus56.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus57.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus58.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus59.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus6.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus60.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus61.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus62.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus63.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus64.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus65.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus66.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus67.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus68.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus69.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus7.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus70.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus71.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus72.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus73.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus74.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus75.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus76.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus77.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus78.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus79.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus8.txt",  // FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus80.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus81.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus82.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus83.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus84.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus85.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus86.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus87.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus88.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus89.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus9.txt",	// FEHLER, 1Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus90.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus91.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus92.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus93.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus94.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus95.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus96.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus97.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus98.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00002_zyklus99.txt"
					
					//whtfMult = 1.57 und 1Zonig
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus1.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus10.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus100.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus101.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus102.txt",	// FEHLER, 2Zonig erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus103.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus104.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus105.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus106.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus107.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus108.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus109.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus11.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus110.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus111.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus112.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus113.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus114.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus115.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus116.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus117.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus118.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus119.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus12.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus120.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus121.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus122.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus123.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus124.txt",	// FEHLER, whtf=1.55 erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus125.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus126.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus127.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus128.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus129.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus13.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus130.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus131.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus132.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus133.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus134.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus135.txt",	// FEHLER, whtf=1.55 erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus136.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus137.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus138.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus139.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus14.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus140.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus141.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus142.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus143.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus144.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus145.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus146.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus147.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus148.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus149.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus15.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus150.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus151.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus152.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus153.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus154.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus155.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus156.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus157.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus158.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus159.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus16.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus160.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus161.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus162.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus163.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus164.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus165.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus166.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus167.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus168.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus169.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus17.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus170.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus171.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus172.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus173.txt",	// FEHLER, whtf=1.55 erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus174.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus175.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus176.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus177.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus178.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus179.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus18.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus180.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus181.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus182.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus183.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus184.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus185.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus186.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus187.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus188.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus189.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus19.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus190.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus191.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus192.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus193.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus194.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus195.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus196.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus197.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus198.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus199.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus2.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus20.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus200.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus201.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus202.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus203.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus204.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus205.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus206.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus207.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus208.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus209.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus21.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus210.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus211.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus212.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus213.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus214.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus215.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus216.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus217.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus218.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus219.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus22.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus220.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus221.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus222.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus223.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus224.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus225.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus226.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus227.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus228.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus229.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus23.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus230.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus231.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus24.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus25.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus26.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus27.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus28.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus29.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus3.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus30.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus31.txt",	// FEHLER! WHT_LWA Hohenberg
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus32.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus33.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus34.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus35.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus36.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus37.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus38.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus39.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus4.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus40.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus41.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus42.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus43.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus44.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus45.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus46.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus47.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus48.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus49.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus5.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus50.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus51.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus52.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus53.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus54.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus55.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus56.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus57.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus58.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus59.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus6.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus60.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus61.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus62.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus63.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus64.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus65.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus66.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus67.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus68.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus69.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus7.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus70.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus71.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus72.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus73.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus74.txt",	// FEHLER, whtf=1.55 erledigt
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus75.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus76.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus77.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus78.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus79.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus8.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus80.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus81.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus82.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus83.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus84.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus85.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus86.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus87.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus88.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus89.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus9.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus90.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus91.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus92.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus93.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus94.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus95.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus96.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus97.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus98.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00003_zyklus99.txt"
					
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus1.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus10.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus100.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus101.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus102.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus103.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus104.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus105.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus106.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus107.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus108.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus109.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus11.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus110.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus111.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus112.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus113.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus114.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus115.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus116.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus117.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus118.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus119.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus12.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus120.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus121.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus122.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus123.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus124.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus125.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus126.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus127.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus128.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus129.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus13.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus130.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus131.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus132.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus133.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus134.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus135.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus136.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus137.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus138.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus139.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus14.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus140.txt",	// FEHLER, 1Zonig&whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus141.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus142.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus143.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus144.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus145.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus146.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus147.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus148.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus149.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus15.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus150.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus151.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus152.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus153.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus154.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus155.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus156.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus157.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus158.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus159.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus16.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus160.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus161.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus162.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus163.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus164.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus165.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus166.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus167.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus168.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus169.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus17.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus170.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus171.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus172.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus173.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus174.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus175.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus176.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus177.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus178.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus179.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus18.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus180.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus181.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus182.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus183.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus184.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus185.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus186.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus187.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus188.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus189.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus19.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus190.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus191.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus192.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus193.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus194.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus195.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus196.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus197.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus198.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus199.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus2.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus20.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus200.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus201.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus202.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus203.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus204.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus205.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus206.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus207.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus208.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus209.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus21.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus210.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus211.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus212.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus213.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus214.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus215.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus216.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus217.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus218.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus219.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus22.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus220.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus221.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus222.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus223.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus224.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus225.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus226.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus227.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus228.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus229.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus23.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus230.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus231.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus232.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus233.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus234.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus24.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus25.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus26.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus27.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus28.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus29.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus3.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus30.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus31.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus32.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus33.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus34.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus35.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus36.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus37.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus38.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus39.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus4.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus40.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus41.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus42.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus43.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus44.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus45.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus46.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus47.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus48.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus49.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus5.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus50.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus51.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus52.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus53.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus54.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus55.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus56.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus57.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus58.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus59.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus6.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus60.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus61.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus62.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus63.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus64.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus65.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus66.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus67.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus68.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus69.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus7.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus70.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus71.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus72.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus73.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus74.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus75.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus76.txt",	// FEHLER, 1Zonig&whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus77.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus78.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus79.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus8.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus80.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus81.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus82.txt",	// FEHLER, whtf=1.55
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus83.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus84.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus85.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus86.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus87.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus88.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus89.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus9.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus90.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus91.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus92.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus93.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus94.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus95.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus96.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus97.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus98.txt",
//					"D://Daten//FVV_CFD_BSZ_II//Messungen//mat//150310//bremo_setup_150310_00004_zyklus99.txt"
			};
			inputFileNames = ifn;
		}
		// String paras[][]=new String[9][];
		// String line[]={"multiZoneInitFile", "[-]", ":=","01_zones.txt"};
		// paras[0]=line;
		// String line2[]={"multiZoneInitFile", "[-]", ":=","02_zones.txt"};
		// paras[1]=line2;
		// String line3[]={"multiZoneInitFile", "[-]", ":=","05_zones.txt"};
		// paras[2]=line3;
		// String line4[]={"multiZoneInitFile", "[-]", ":=","10_zones.txt"};
		// paras[3]=line4;
		// String line5[]={"multiZoneInitFile", "[-]", ":=","20_zones.txt"};
		// paras[4]=line5;
		// String line6[]={"multiZoneInitFile", "[-]", ":=","30_zones.txt"};
		// paras[5]=line6;
		// String line7[]={"multiZoneInitFile", "[-]", ":=","40_zones.txt"};
		// paras[6]=line7;
		// String line8[]={"multiZoneInitFile", "[-]", ":=","50_zones.txt"};
		// paras[7]=line8;
		// String line9[]={"multiZoneInitFile", "[-]", ":=","100_zones.txt"};
		// paras[8]=line9;
		String paras[][] = new String[11][];
		double step = 0.5;
		for (int i = 0; i < 10; i++) {
			double a = 0.5 + i * step;
			String line[] = { "C_Mix", "[-]", ":=", "" + a };
			paras[i] = line;
		}
		String line[] = { "C_Mix", "[-]", ":=", "" + 8 };
		paras[10] = line;

		// automaticRun2(inputFileNames,paras);

		postProcessKiva();
		// automaticRun(inputFileNames);
		// rasterFahndung(inputFileNames);
		multiFileRun(inputFileNames);
		Double secs = new Double(
				(new java.util.Date().getTime() - begTest) * 0.001);
		System.out.println("run time " + secs + " secs");
		System.out.println("Done");
		System.exit(0);
	}

	private static void postProcessKiva() {
		String path[] = {
		// "y:\\kivaCoarse\\mode3\\TurbIncrease\\mode3_ReferenceSimplifiedStockPistonHoriz\\post\\multi\\multi_zone_kiva_dump.",
		// "y:\\mode3Post\\test\\multi_zone_kiva_dump.",
		// "E:\\ERC Daten\\Kiva\\ReRunFixedMesh\\FixedMeshInitialsFromYifeng\\mode7\\mode7_370\\Post\\multi_zone_kiva_dump.",
		// "E:\\ERC Daten\\Kiva\\KivaCoarse\\mode3\\TurbIncrease\\mode3_ReferenceSimplifiedStockPistonHoriz\\20130725_Post\\multi_zone_kiva_dump."
		// "y:\\mode3Post\\multi_zone_kiva_dump.",
		// "y:\\mode3Post\\multi_zone_kiva_dump.",
		// "y:\\mode4Post\\multi_zone_kiva_dump.",
		// "y:\\mode6Post\\multi_zone_kiva_dump.",
		// "y:\\mode7Post\\multi_zone_kiva_dump.",
		// "y:\\mode8Post\\multi_zone_kiva_dump.",
		};
		for (int i = 0; i < path.length; i++) {
			File fileCP = new File("src//KivaPostProcessing//dummyInput.txt");
			CasePara CP = null;
			try {
				CP = new CasePara(fileCP);
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
			}
			KivaPostProcessor kpp = new KivaPostProcessor();
			kpp.multiPostProcess(path[i], CP, 46, 7);
		}
	}

	private static void multiFileRun(String[] inputFileNames) {
		File file;
		for (int i = 0; i < inputFileNames.length; i++) {
			// file = new File("src//InputFiles//" + inputFileNames[i]);
			file = new File(inputFileNames[i]);
			Bremo bremo = new Bremo(file, false);
			// try{
			bremo.run();
			bremo = null;
			// }catch(ErrorZoneException e){
			// bremo = null;
			// }
		}
	}

	private static void optiConstants(String[] inputFileNames) {
		double x0[] = new double[2];
		x0[1] = 5;
		Optimizer opt = new Optimizer(x0);
		double[] out = opt.optimize();
		File file;
		for (int i = 0; i < inputFileNames.length; i++) {
			file = new File("src//InputFiles//" + inputFileNames[i]);

			double[] x00 = { -5, 2.2, 1.48 };
			ConstantsOptimizer co = new ConstantsOptimizer(x00,
					inputFileNames[i], 1);
			double[] outPut = co.optimize();
		}
	}

	private static void rasterFahndung(String[] inputFileNames) {
		for (int fileIndex = 0; fileIndex < inputFileNames.length; fileIndex++) {

			File file = new File("src//InputFiles//"
					+ inputFileNames[fileIndex]);
			FileWriter_txt fw2 = new FileWriter_txt(file.getAbsolutePath());

			String absPath = file.getAbsolutePath();
			String fileName = file.getName();
			String temp = absPath.substring(0, file.getAbsolutePath()
					.lastIndexOf(fileName));
			String stfrName = temp + "APR_" + fileName;
			String p = temp + "corr_" + fileName;
			FileWriter_txt corrDataWriter = new FileWriter_txt(p);
			String header[] = { "C1", "C2", "xcorr", "rms" };
			corrDataWriter.writeTextLineToFile(header, false);

			double c1[] = new double[6];
			// c1[0]=0.05;
			// c1[25]=2.95;
			for (int u = 0; u < c1.length; u++) {
				c1[u] = u * 0.1 + 0.3;
			}
			// double
			// c2[]={0.01,0.05,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,0.99999};
			// double c2[]=new double [21];
			// for(int u=0;u<c2.length;u++){
			// c2[u]=u*0.05+0.1;
			// }
			// double c2[]={1,2,5,10,15,25,55,100,155};
			double c2[] = { 0.05, 0.15, 0.25, 0.35, 0.45, 0.55 };
			// double c2[]=new double[15];
			// for(int u=0;u<c2.length;u++){
			// c2[u]=u*0.05+0.1;
			// }
			for (int c1Idx = 0; c1Idx < c1.length; c1Idx++) {
				for (int c2Idx = 0; c2Idx < c2.length; c2Idx++) {
					String[] s = { "C_Mix\t[-]\t:=\t" + c1[c1Idx] + "\n",
							"C_MixRad\t[-]\t:=\t" + c2[c2Idx] + "\n\n" };
					fw2.writeTextLineToFile(s, true);
					Bremo bremo = new Bremo(file, false);
					bremo.run();
					bremo = null;
					System.gc();
					SimpleTXTFileReader sf = new SimpleTXTFileReader(stfrName);
					double[][] data = sf.get_dataTransposed();
					int iter = 0;
					int indexStart = 0, indexEnd = 0;
					double a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0, a6 = 0, a7 = 0;
					do {
						if (data[0][iter] <= -15)
							indexStart = indexStart + 1;
						if (data[0][iter] <= 14)
							indexEnd = indexEnd + 1;

						if (data[0][iter] <= -6)
							a1 = data[5][iter] - data[6][iter];
						if (data[0][iter] <= -2)
							a2 = data[5][iter] - data[6][iter];
						if (data[0][iter] <= 1)
							a3 = data[5][iter] - data[6][iter];
						if (data[0][iter] <= 3)
							a4 = data[5][iter] - data[6][iter];
						if (data[0][iter] <= 5)
							a5 = data[5][iter] - data[6][iter];
						if (data[0][iter] <= 8)
							a6 = data[5][iter] - data[6][iter];
						if (data[0][iter] <= 13)
							a7 = data[5][iter] - data[6][iter];
						iter = iter + 1;
					} while (iter < data[0].length);
					double rms = a1 * a1 + a2 * a2 + a3 * a3 + a4 * a4 + a5
							* a5 + a6 * a6 + a7 * a7;
					rms = Math.sqrt(rms);

					int l = indexEnd - indexStart + 1;
					double[] pExp = new double[l];
					double[] pBremo = new double[l];
					for (int i = 0; i < l; i++) {
						pExp[i] = data[5][i + indexStart];
						pBremo[i] = data[6][i + indexStart];
					}
					double xc = -5;
					xc = MatLibBase.crossCorr0(pExp, pBremo);

					double sToWrite[] = new double[4];
					sToWrite[0] = c1[c1Idx];
					sToWrite[1] = c2[c2Idx];
					sToWrite[2] = (xc);
					sToWrite[3] = (rms);
					corrDataWriter.writeLineToFile(sToWrite, true);

				}

			}

		}

	}

	private static void automaticRun2(String inputFileNames[],
			String inputParameter[][]) {
		for (int i = 0; i < inputFileNames.length; i++) {
			for (int u = 0; u < inputParameter.length; u++) {
				File f = new File("src//InputFiles//" + inputFileNames[i]);
				if (!f.isFile()) {
					try {
						throw new BirdBrainedProgrammerException(
								"This file: \n" + f.getAbsolutePath()
										+ "\n does not exist");
					} catch (BirdBrainedProgrammerException ipe) {
						ipe.stopBremo();
					}
				}
				Path source = Paths.get(f.getAbsolutePath());
				String name = f.getName();
				String nameWithoutExt2 = name.substring(0,
						name.lastIndexOf("."));
				Path target = Paths.get(source.getParent() + "//"
						+ nameWithoutExt2 + "_" + u + ".txt");
				try {
					Files.copy(source, target);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				File newFile = new File(target.toString());
				FileWriter_txt fw2 = new FileWriter_txt(
						newFile.getAbsolutePath());
				fw2.writeTextLineToFile(inputParameter[u], true);
				Bremo bremo = new Bremo(newFile, false);
				bremo.run();
				bremo = null;
			}

		}
	}

	private static void automaticRun(String[] inputFileNames) {

		String[][] runs = new String[7][];
		// String []s0={"mixingProcess\t [-]\t :=\t 0",
		// "EntrainmentFaktor\t[-]\t :=\t 2.9000000000",
		// "ProfilFaktor\t	[-]\t	:=\t	5.00000e-01",
		// "C_Mix\t	[-]\t	:=\t	15.0028700000",
		// "C_MixRad\t	[-]\t	:=\t	1.48000000000",
		// "EntrainmentFaktor_2\t[-]\t :=\t 2.9000000000",
		// "ProfilFaktor_2\t	[-]\t	:=\t	5.00000e-01",
		// "C_Mix_2\t	[-]\t	:=\t	15.0028700000",
		// "C_MixRad_2\t	[-]\t	:=\t	1.48000000000","\n\n"};
		// runs[0]=s0;

		String[] s1 = { "mixingProcess\t [-]\t :=\t 1",
				"EntrainmentFaktor\t[-]\t :=\t 2.9",
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t	1.76541345",
				"C_MixRad\t	[-]\t	:=\t	1.48e+00",
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9",
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t	1.76541345",
				"C_MixRad_2\t	[-]\t	:=\t	1.48", "\n\n" };
		runs[1] = s1;

		// String []s2={"mixingProcess\t [-]\t :=\t 2",
		// "EntrainmentFaktor\t[-]\t :=\t 2.9000000000",
		// "ProfilFaktor\t	[-]\t	:=\t	5.00000e-01",
		// "C_Mix\t	[-]\t	:=\t	1.0580028700000",
		// "C_MixRad\t	[-]\t	:=\t	1.48e+00",
		// "EntrainmentFaktor_2\t[-]\t :=\t 2.9000000000",
		// "ProfilFaktor_2\t	[-]\t	:=\t	5.00000e-01",
		// "C_Mix_2\t	[-]\t	:=\t	1.0580028700000",
		// "C_MixRad_2\t	[-]\t	:=\t	1.48e+00","\n\n"};
		// runs[2]=s2;

		String[] s3 = { "mixingProcess\t [-]\t :=\t 3",
				"EntrainmentFaktor\t[-]\t :=\t 2.9",
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t	18.0028700000",
				"C_MixRad\t	[-]\t	:=\t	1.48",
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9",
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t	18.0028700000",
				"C_MixRad_2\t	[-]\t	:=\t	1.48", "\n\n" };
		runs[3] = s3;

		String[] s4 = { "mixingProcess\t [-]\t :=\t 4",
				"EntrainmentFaktor\t[-]\t :=\t 2.9",
				"ProfilFaktor\t	[-]\t	:=\t	0.5", "C_Mix\t	[-]\t	:=\t	6.659247",
				"C_MixRad\t	[-]\t	:=\t	1.48",
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9",
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t	6.659247",
				"C_MixRad_2\t	[-]\t	:=\t	1.48", "\n\n" };
		runs[4] = s4;

		String[] s5 = { "mixingProcess\t [-]\t :=\t 5",
				"EntrainmentFaktor\t[-]\t :=\t 0.65",
				"ProfilFaktor\t	[-]\t	:=\t	0.7",
				"EntrainmentFaktor_2\t[-]\t :=\t 0.65",
				"ProfilFaktor_2\t	[-]\t	:=\t	0.75", "\n\n" };
		runs[5] = s5;

		String[] s6 = { "mixingProcess\t [-]\t :=\t 6",
				"EntrainmentFaktor\t[-]\t :=\t 2.9",
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t 0.4289197",
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9",
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t 0.4289197", "\n\n" };
		runs[6] = s6;

		for (int i = 0; i < inputFileNames.length; i++) {
			for (int r = 0; r < runs.length; r++) {
				if (runs[r] != null) {

					// File file = new File("src//InputFiles//" +
					// inputFileNames[i]);
					// String nameWithoutExt=file.getName().substring(0,
					// file.getName().lastIndexOf("."));
					// String dir=file.getAbsolutePath().substring(0,
					// file.getAbsolutePath().indexOf(file.getName()));
					// File newFile=new File(dir +
					// nameWithoutExt+"_"+r+".txt");
					//
					// file.renameTo(newFile);
					// FileWriter_txt fw2=new
					// FileWriter_txt(newFile.getAbsolutePath());

					Path source = Paths.get("src//InputFiles//"
							+ inputFileNames[i]);
					String nameWithoutExt2 = inputFileNames[i].substring(0,
							inputFileNames[i].lastIndexOf("."));

					Path target = Paths.get("src//InputFiles//"
							+ nameWithoutExt2 + "_" + r + ".txt");

					try {
						Files.copy(source, target);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					File newFile = new File(target.toString());
					FileWriter_txt fw2 = new FileWriter_txt(
							newFile.getAbsolutePath());

					for (int u = 0; u < runs[r].length; u++) {
						String[] s = new String[1];
						s[0] = runs[r][u];
						fw2.writeTextLineToFile(s, true);
					}
					Bremo bremo = new Bremo(newFile, false);
					bremo.run();
					bremo = null;
					// newFile.renameTo(file);
				}
			}
		}

	}

	public void test_BremoCanteraSpecies2() {
		CanteraCaller cc = new CanteraCaller(this.casePara, 1);
		System.out.println("cc erzeugt");
		// cc.sprich();
		double TMin = 300;
		double TMax = 3000;
		int nbrIterations = 27;
		double TIntervall = (TMax - TMin) / nbrIterations;
		double p = 101325;
		double T;
		double h_Bremo;
		double h_Cantera;
		double erg[][] = new double[nbrIterations][3];

		for (int i = 0; i < cc.get_NbrOfSpecies(); i++) {
			for (int k = 0; k < nbrIterations; k++) {
				T = TMin + k * TIntervall;
				double pVTX[] = new double[cc.get_NbrOfSpecies() + 3];
				pVTX[0] = p;
				pVTX[1] = 0.1;
				pVTX[2] = T;
				pVTX[i + 3] = 1;
				cc.set_StatepVTX(pVTX, 0);
				h_Cantera = cc.get_h_mol_Zone(0);
				erg[k][0] = T;
				erg[k][1] = h_Cantera;
			}
		}
		System.out.println("hab was gemacht");
	}

}
