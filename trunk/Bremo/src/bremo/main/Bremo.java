package bremo.main; 

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
//import funktionenTests.FunktionsTester;
import berechnungsModule.Berechnung.Verlustteilung;

import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
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
	}


	public Bremo( File inputFile) {	
		super(inputFile.getName());
		this.inputFile=inputFile;	
	}


	/**
	 * Erzeugt eine Instanz vom Typ CasePara und startet die Berechnung
	 * @see CasePara
	 */
	public void run() {
		try {
			casePara = new CasePara(inputFile);
			caseParaerzeugt=true;
			SwingBremo.SetDebbugingMode(casePara.SYS.DUBUGGING_MODE) ; 
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
					"Thread "+this.getName()+" ist fertig!", this.getName(),
					JOptionPane.INFORMATION_MESSAGE);
			SwingBremo.PutInBremoThreadFertig(this.getName());
		} catch (Exception  e) {
			this.interrupt();
			e.printStackTrace();
			
		}
		if(casePara.is_Verlustteilung()){
			SwingBremo.VerlustteilungThread();
			Verlustteilung verl = new Verlustteilung(casePara);
			verl.berechneVerluste();
			System.err.println("Verlustteilung fertig");
			SwingBremo.StateBremoThread();
			}
		else {
			SwingBremo.StateBremoThread();
		}
	}	

	/**
	 * Gibt ein Objekt vom Typ CasePara zurueck. 
	 * Wenn diese noch nicht erzeugt sind wird ein Fehler ausgegeben. 
	 * @return CasePara
	 * @see CasePara
	 */
	public CasePara get_myCase() {
		if(caseParaerzeugt)
			return casePara;
		else{
			try {
				throw new BirdBrainedProgrammerException("Es wurde versucht auf die Klasse CasePara zuzugreifen. " +
				"Diese wurde aber noch nicht erzeugt. Volldeppprogrammierer");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}			
			return null;
		}
	}

	/**
	 * Gibt an ob eine Instanz vom Typ CasePara bereits erzeugt wurde
	 * @return 
	 * @see CasePara
	 */
	public boolean get_myCaseState () {

		return caseParaerzeugt;
	}
	/**
	 * Gibt die InputFile Von Bremo
	 * @return inpuFile
	 */
	public File get_myFile() {
		
		return inputFile;
	}


	/**
	 * Main-Methode um Bremo ohne das GUI aufzurufen. 
	 * @param args Pfad der auf das InputFile verweist
	 * @throws ParameterFileWrongInputException
	 */
	public static void main(String[] args)
	throws ParameterFileWrongInputException {		
		//Um Funktionen zu testen gibt es die Klasse FunktionsTester
		//Hier einige Beisspile wie Funktionen getestet werden können
		//File fileCP 
		//= new File("D://Daten//Eichmeier//Dropbox//Dropbox//Eclipse//BremoGC//src//InputFiles//funktionsTester.txt");
		//CasePara privateCP = new CasePara(fileCP);
		//FunktionsTester ft=new FunktionsTester(privateCP);
		//ft.test_Motor();

		//Bremo kann auch ohne GUI ausgefuehrt werden!
		//Das InputFile muss dann manuell erzeugt oder mittels "args" uebergeben werden
		File file;	
		if(args.length>0)
			file = new File(args[0]);
		else
			file = new File("d://Daten//workspace//Bremo//src//InputFiles//Frank//120702_Einzonig.txt");

		Bremo bremo=new Bremo(file);
		bremo.run();	
	}

}
