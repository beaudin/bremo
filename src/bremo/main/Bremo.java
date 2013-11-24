package bremo.main; 

import functionTests.FunctionTester;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bremo.parameter.CasePara;
import bremo.sys.SimulationRunner;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.SwingBremo;


/**
 * @author eichmeier
 * @author Ngueneko
 * 
 */
public class Bremo extends Thread {

	private  CasePara casePara; 
	private SimulationRunner r;
	private File inputFile;
	private boolean caseParaerzeugt=false;
	private boolean calledFromGUI;


	public Bremo( ThreadGroup group , File inputFile) {
		super(group,inputFile.getName());
		calledFromGUI=true;
		this.inputFile=inputFile;
	}

	public Bremo(File inputFile, boolean calledFromGUI) {
		super(inputFile.getName());
		this.inputFile=inputFile;
		this.calledFromGUI=calledFromGUI;	
	}

	public  Bremo(String absolutePath2InputFile) {
		File fileCP= new File(absolutePath2InputFile);
		Bremo bremo=new Bremo(fileCP,false);
		bremo.run();
		bremo=null;
	}
	

	/**
	 * Runs the simulation
	 * @see CasePara
	 */
	public void run() {
		try {
			casePara = new CasePara(inputFile);
			caseParaerzeugt=true;
			SwingBremo.SetDebbugingMode(casePara.SYS.DUBUGGING_MODE) ; 
			r = new SimulationRunner(casePara);
		} catch (ParameterFileWrongInputException e) {
			if(calledFromGUI){
				SwingBremo.setNrOfBremoAlive();
				JFrame popup = new JFrame();
				JOptionPane.showMessageDialog(popup,
						"Thread : Eine Fehler ist in der File "+this.getName()+" \n aufgetreten !!!" +
						"\n"+e.getMessage(), this.getName(),
						JOptionPane.ERROR_MESSAGE);
				//SwingBremo.NrOfFile--;
				SwingBremo.StateBremoThread();
			}
			e.printStackTrace();
			e.stopBremo();			
		}
		try {
			r.runSimulation();		
			
			if(calledFromGUI){
				JFrame popup = new JFrame();
				JOptionPane.showMessageDialog(popup,
						"Thread "+this.getName()+" ist fertig!", this.getName(),
						JOptionPane.INFORMATION_MESSAGE);
				SwingBremo.PutInBremoThreadFertig("Thread "+this.getName()+" ist fertig!");
				SwingBremo.StateBremoThread();
			}
		} catch (Exception  e) {
			this.interrupt();
			e.printStackTrace();
		}
	}	

	/**
	 * Returns the case parameters of the simulation run 
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
	 * Returns weather the case parameter object has alreasy been created
	 * @return 
	 * @see CasePara
	 */
	public boolean get_myCaseState () {

		return caseParaerzeugt;
	}


	/**
	 * Main method to start Bremo without GUI
	 * @param args path to the inputfile
	 * @throws ParameterFileWrongInputException
	 */
	public static void main(String[] args)
			throws ParameterFileWrongInputException {		
		
		long begTest = new java.util.Date().getTime();	
		System.out.println(System.getProperty("java.library.path"));		
//		To test some functions in Bremo use the calss functionTester 
//		this is the path to the inputfile needed for functionTester		
		File fileCP = new File("src//InputFiles//FunktionsTester//FunktionsTester.txt");	
//		creates the casePara object
		CasePara privateCP = new CasePara(fileCP);
		FunctionTester ft=new FunctionTester(privateCP);
//		ft.estimateEGR();
		ft.getInitialConditions_2();
//		ft.testDieselMassFracion();
	
		String[] inputFileNames;
		if(args.length>0)
			inputFileNames=args;
		else{
			String [] ifn={	

					"Final/Mode4/m4_MixApp2_dmFlux1.txt",
			};
			inputFileNames=ifn;
		}	
		multiFileRun(inputFileNames);		
		Double secs = new Double((new java.util.Date().getTime() - begTest)*0.001);
		System.out.println("run time " + secs + " secs");
		System.out.println("Done");
		System.exit(0);
	}	

	
	private static void multiFileRun(String [] inputFileNames){
		File file;	
		for(int i=0;i<inputFileNames.length;i++){
			file = new File("src//InputFiles//" +
					inputFileNames[i]);				
			Bremo bremo=new Bremo(file,false);
			bremo.run();
			bremo=null;
		}		
	}	
}
