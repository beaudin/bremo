package bremo.main; 

import java.io.File;
import java.net.Inet4Address;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoExceptions.StopBremoException;
import bremoswing.SwingBremo;


/**
 * @author eichmeier
 * @author Ngueneko
 * 
 */
public class Bremo extends Thread {

	private  CasePara casePara; //TODO Make me final
	private Rechnung r;
	private File inputFile;
	private boolean caseParaerzeugt=false;

	
	public Bremo( ThreadGroup group , File inputFile) {
		super(group,inputFile.getName());
		this.inputFile=inputFile;
//		try {
//			casePara = new CasePara(inputFile);
//			r = new Rechnung(casePara);
//		} catch (ParameterFileWrongInputException e) {				
//			e.stopBremo();
//		}		
	}
	
	
	public Bremo( File inputFile) {	
		super(inputFile.getName());
		this.inputFile=inputFile;
		
//		try {
//			casePara = new CasePara(inputFile);
//			r = new Rechnung(casePara);
//		} catch (ParameterFileWrongInputException e) {				
//			e.stopBremo();
//		}		
	}
	
	

	public void run() {
		try {
			casePara = new CasePara(inputFile);
			SwingBremo.SetDebbugingMode(casePara.SYS.DUBUGGING_MODE) ; 
			caseParaerzeugt=true;
			r = new Rechnung(casePara);
		} catch (ParameterFileWrongInputException e) {
			SwingBremo.setNrOfBremoAlive();
			JFrame popup = new JFrame();
			JOptionPane.showMessageDialog(popup,
					"Thread : Eine Fehler ist in der File "+this.getName()+" \n aufgetreten !!!" +
							"\n"+e.getMessage(), this.getName(),
					JOptionPane.ERROR_MESSAGE);
			//SwingBremo.NrOfFile--;
			SwingBremo.StateBremoThread();
			e.stopBremo();			
		}
		try {
			r.berechnungDurchfuehren();
			//SwingBremo.StateBremoThread();
			//System.err.println("Thread : "+this.getName()+"   is Fertig !!!");
			JFrame popup = new JFrame();
			JOptionPane.showMessageDialog(popup,
					"Thread : "+this.getName()+"   is Fertig !!!", this.getName(),
					JOptionPane.INFORMATION_MESSAGE);
			SwingBremo.PutInBremoThreadFertig("Thread : "+this.getName()+"   ist Fertig !!!");
			SwingBremo.StateBremoThread();
		} catch (Exception  e) {
			this.interrupt();
			e.printStackTrace();
		}
	}	

	public CasePara get_myCase() {
		if(caseParaerzeugt)
			return casePara;
		else{
			try {
				throw new BirdBrainedProgrammerException("Es wurde versucht auf die CasePara zuzugreifen. " +
						"Diese wurden aber noch nicht erzeugt. Volldeppprogrammierer");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}			
			return null;
		}
	}
	
	public boolean get_myCaseState () {
		
		return caseParaerzeugt;
	}
	

	public static void main(String[] args)
			throws ParameterFileWrongInputException {

		// TestKlasse tt=new TestKlasse("original");
		// TestKlasse tt_1=TestKlasse.getInstance(1);
		// tt_1.tellMessage();
		// System.out.println("Nachricht der originalklasse: ");
		// tt.tellMessage();
		// TestKlasse tt_2=TestKlasse.getInstance(2);
		// tt_2.tellMessage();

		// OhneModifierTest om=new OhneModifierTest();
		// ProtectedKonstruktorTest pk= new ProtectedKonstruktorTest();
		//		
		// File inputFile=new File(args[0]);
		// Bremo bremo=new Bremo(inputFile);
		// String test []=BerechnungsModell.get_benoetigteModelle();

		// System.out.println(get_casePara().get_rechenBeginn());
		File file;

		// file = new
		// File("//Volumes//Cruzer//_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA.txt");
		// file = new
		// File("//Volumes//Cruzer//_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");
		// file = new
		// File("//Users//juwe//Dropbox//Bremo//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");
		// file = new
		// File("L://_DISS//Bremo//BremoXP//Bremo//src//InputDatei_ZonenTester.txt");
		// file = new
		// File("L://_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA.txt");
		// file = new
		// File("L://_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");
		// file = new
		// File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100.txt");
		// file = new
		// File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo//src//InputDatei_DVA_Dahnz.txt");
		// file = new
		// File("//Users//juwe//Dropbox//Eclipse//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");
		// file = new
		// File("//Users//juwe/Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100_DF.txt");
		// file = new
		// File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100_DF.txt");

		// file=new
		// File("D://Daten//IFKM//__Projekte//Dual Fuel//Diss//Bremo//BremoXP//Bremo//src//InputDatei_2.txt");

		// file = new
		// File("D://Daten//IFKM//__Projekte//Dual Fuel//AbgleichAnalyseModell//DVA//20111110_8_DVAParameter.txt");
		// file = new
		// File("D://Daten//IFKM//__Projekte//Dual Fuel//AbgleichAnalyseModell//DVA//20111110_12_DVAParameter.txt");
		// file = new
		// File("//Users//juwe/Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100_DF.txt");
		// file = new
		// File("D://Daten//IFKM//__Projekte//Dual Fuel//MTU EinzylBR2000//experimentelle Untersuchungen"
		// +
		// "//DualFuel//Basisuntersuchungen//20080110//Export//20111107_20_DVAParameter.txt");

		// file = new
		// File("J://optische Untersuchungen BR1600//ZweiFarbenMethode//20101020//Export//20101020_69_DVAParameter.txt");

		// file = new
		// File("D://Daten//IFKM//__Projekte//Dual Fuel//MTU EinzylBR2000//" +
		// "experimentelle Untersuchungen//DualFuel//Einspritzdüsenvergleich//"
		// +
		// "200804-Charge A//20080416 - 6loch//Export//20111211_10_DVAParameter.txt");
		// file = new
//		 File("D://Daten//IFKM//__Projekte//Dual Fuel//AbgleichAnalyseModell//AbgleichDieselPeakMethode//20111110_11_DVAParameter.txt");
//		file = new File("//Users//juwe//Documents//Transfer//VergleichAGRVerdŸnnungTEMP//" 
//					+ "//20101213//Export//AGR_LAM//20111220_103_DVAParameter.txt");
//		file = new File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo4//src//_Export//20111110_11_DVAParameter.txt");
		file = new File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//BremoGC//src//InputFiles//20101020_40_DVAParameter.txt");

		Bremo bremo=new Bremo(file);
		bremo.run();
		
		// FunktionsTester.test_Vektor();

		// FunktionsTester.test_get_lambda();
		// FunktionsTester.test_HashAdd();
		// FunktionsTester.test_neueSpezies();
		// FunktionsTester.calc_cp_Dilution();
		// FunktionsTester.testHeizwertRechner();
		// FunktionsTester.testHeizwertRechner();
		// FunktionsTester.testAdiabateVerbrennungsTemp();
		// FunktionsTester.kalorikAbgleichJanaf();
		// FunktionsTester.testVerdampfung();

		// FunktionsTester.test_HUbKolbenMotor();

		// FunktionsTester.testAdiabateVerbrennungsTemp();
		// FunktionsTester.test_IndizierDaten();
		// FunktionsTester.test_ErgebnisHash();
		// System.out.println(casePara.get_rechenBeginn());
		// FunktionsTester.test_pZylReader();
		// String a="5.55";
		// Double.parseDouble(a);
		// double b=Double.parseDouble(a);
		// FunktionsTester.test_Xialings_HiroYasu();

		// BetriebsParameter betriebsPara=new BetriebsParameter();
		// Motor motor= new Motor();
		// SysPara sysPara=new SysPara();
		//		
		// ParameterFile F = new ParameterFile();
		//	
		// CasePara.erzeuge_EINZIGE_Instanz(betriebsPara, sysPara, motor, F);

		// FunktionsTester.testGleichgewichtsRechner_A();
		// FunktionsTester.testGleichgewichtsRechner_B();
		// FunktionsTester.testKalorikRechner();
		// FunktionsTester.testKalorikRechner_Luft();
		// FunktionsTester.testSpezies();
		// testJanafKoeffs();
		// // test_Gg();
		// FunktionsTester.testGasGemisch();
		// FunktionsTester.testObjektKopie();
		// FunktionsTester.testSpeziesInputFuerOHCsolver();


	}

}
