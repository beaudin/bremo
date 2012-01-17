package bremo.main;



import java.io.File;

import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.CaseParaNotInstantiatedException;
import bremoExceptions.ParameterFileWrongInputException;
import funktionenTests.FunktionsTester;

/**
 * @author eichmeier
 *
 */
public class Bremo extends Thread {	
	

	private static CasePara casePara;		

	
	public static CasePara get_casePara(){
		if(casePara!=null){
			return casePara;
		}else{
			try {
				throw new CaseParaNotInstantiatedException();
			} catch (CaseParaNotInstantiatedException e) {
				e.stopBremo();
				return null;			
			}			
		}
	}
	
	public Bremo(File inputFile){
		casePara=CasePara.erzeuge_EINZIGE_Instanz(inputFile);		
	}
	
	
	
	 public void run() {	
		try {
		Rechnung r=new Rechnung(casePara);
		r.berechnungDurchfuehren();
		}
		catch (Exception e) {
			this.interrupt();
		}
	}
  
	// info
//	public static void main(String[] args) throws ParameterFileWrongInputException {	
		
//		TestKlasse tt=new TestKlasse("original");
//		TestKlasse tt_1=TestKlasse.getInstance(1);
//		tt_1.tellMessage();
//		System.out.println("Nachricht der originalklasse: ");
//		tt.tellMessage();
//		TestKlasse tt_2=TestKlasse.getInstance(2);
//		tt_2.tellMessage();
		
//		OhneModifierTest om=new OhneModifierTest();
//		ProtectedKonstruktorTest pk= new ProtectedKonstruktorTest();
//		
//		File inputFile=new File(args[0]);
//		Bremo bremo=new Bremo(inputFile);
//		String test []=BerechnungsModell.get_benoetigteModelle();
		
//		System.out.println(get_casePara().get_rechenBeginn());
		
//		File file;
	
//		file = new File("//Volumes//Cruzer//_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA.txt");		
//		file = new File("//Volumes//Cruzer//_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");	
//		file = new File("//Users//juwe//Dropbox//Bremo//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");	
//		file = new File("L://_DISS//Bremo//BremoXP//Bremo//src//InputDatei_ZonenTester.txt");	
//		file = new File("L://_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA.txt");	
//		file = new File("L://_DISS//Bremo//BremoXP//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");	
//		file = new File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100.txt");
//		file = new File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo//src//InputDatei_DVA_Dahnz.txt");
		//		file = new File("//Users//juwe//Dropbox//Eclipse//Bremo//src//InputDatei_DVA_VGL_Kuni.txt");
//		file = new File("//Users//juwe/Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100_DF.txt");
//		file = new File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100_DF.txt");
		
//		file=new File("D://Daten//IFKM//__Projekte//Dual Fuel//Diss//Bremo//BremoXP//Bremo//src//InputDatei_2.txt");
		
//		file = new File("D://Daten//IFKM//__Projekte//Dual Fuel//AbgleichAnalyseModell//DVA//20111110_8_DVAParameter.txt");
//		file = new File("D://Daten//IFKM//__Projekte//Dual Fuel//AbgleichAnalyseModell//DVA//EXEC_20111110_8_DVAParameter.txt");
//		file = new File("//Users//juwe/Dropbox//Eclipse//Bremo//src//InputDatei_DualFuel_20101213_100_DF.txt");
//		file = new File("D://Daten//IFKM//__Projekte//Dual Fuel//MTU EinzylBR2000//experimentelle Untersuchungen" +
//				"//DualFuel//Basisuntersuchungen//20080110//Export//20111107_20_DVAParameter.txt");
//		casePara=CasePara.erzeuge_EINZIGE_Instanz(file);
//		FunktionsTester.test_Vektor();
	
//		FunktionsTester.test_get_lambda();
//		FunktionsTester.test_HashAdd();
//		FunktionsTester.test_neueSpezies();
//		FunktionsTester.calc_cp_Dilution();
//		FunktionsTester.testHeizwertRechner();
//		FunktionsTester.testHeizwertRechner();
//		FunktionsTester.testAdiabateVerbrennungsTemp();
//		FunktionsTester.kalorikAbgleichJanaf();
//		FunktionsTester.testVerdampfung();

	
//		FunktionsTester.test_HUbKolbenMotor();

//		FunktionsTester.testAdiabateVerbrennungsTemp();
//		FunktionsTester.test_IndizierDaten();
//		FunktionsTester.test_ErgebnisHash();
//		System.out.println(casePara.get_rechenBeginn());
//		FunktionsTester.test_pZylReader();
//		String a="5.55";
//		Double.parseDouble(a);
//		double b=Double.parseDouble(a);
//		FunktionsTester.test_Xialings_HiroYasu();
		
//		BetriebsParameter betriebsPara=new BetriebsParameter();
//		Motor motor= new Motor();
//		SysPara sysPara=new SysPara();	
//		
//		ParameterFile F = new ParameterFile();
//	
//		CasePara.erzeuge_EINZIGE_Instanz(betriebsPara, sysPara, motor, F);

//		FunktionsTester.testGleichgewichtsRechner_A();
//		FunktionsTester.testGleichgewichtsRechner_B();
//		FunktionsTester.testKalorikRechner();
//		FunktionsTester.testKalorikRechner_Luft();
//		FunktionsTester.testSpezies();
//		testJanafKoeffs();
////		test_Gg();
//		FunktionsTester.testGasGemisch();
//		FunktionsTester.testObjektKopie();	
//		FunktionsTester.testSpeziesInputFuerOHCsolver();
		
//		Rechnung r=new Rechnung(casePara);
//		r.berechnungDurchfuehren();
		
//	}

	


}
