package bremo.main; 

import io.FileWriter_txt;
import io.SimpleTXTFileReader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import funktionenTests.FunktionsTester; 			//fuer Verlustteilung Frank Haertel
import berechnungsModule.Berechnung.Verlustteilung;	//fuer Verlustteilung Frank Haertel

import matLib.ConstantsOptimizer;
import matLib.MatLibBase;
import matLib.Optimizer;
import KivaPostProcessing.KivaPostProcessor;
import berechnungsModule.Berechnung.CanteraCaller;
import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.SwingBremo;
import bremoswing.util.FertigMeldungFrame;


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
	private boolean calledFromGUI;


	public Bremo( ThreadGroup group , File inputFile) {
		super(group,inputFile.getName());
		calledFromGUI=true;
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
	
	public Bremo(File inputFile, boolean calledFromGUI) {
		super(inputFile.getName());
		this.inputFile=inputFile;
		this.calledFromGUI=calledFromGUI;	
	}
	
//	public Bremo() {		
//		super("ModelAbgleichKivaTurbulence/KIVA_C1_0.1_C1_0.5.txt");
//		File file = new File("src//InputFiles//" +
//				"ModelAbgleichKivaTurbulence/KIVA_C1_0.1_C1_0.5.txt");			
//		this.inputFile=file;
//		this.calledFromGUI=false;		
//	}
	
	public Bremo() throws ParameterFileWrongInputException {		
	System.out.println("Juchuu, es klappt");
	System.out.println("Checkt");
//	CPPCaller cppC=new CPPCaller();
//	cppC.sagMalHallo();
//	String a="ERCmech.cti";
//	CanteraCaller cc=new CanteraCaller(a,"gas",1);	
//	test_BremoCanteraSpecies2();
//	File fileCP 
////	= new File("src//InputFiles//FunktionstesterCT.txt");	
//	= new File("c://Documents and Settings//eichmeier//jniWorkspace2//CallMeFromMatlab//src//InputFiles//FunktionstesterCT.txt");
//	CasePara privateCP = new CasePara(fileCP);
//	FunktionsTester ft=new FunktionsTester(privateCP);
//	ft.testCantera3Zone();
	
	File fileCP 
//	= new File("src//InputFiles//FunktionstesterCT.txt");	
	= new File("c://Documents and Settings//eichmeier//jniWorkspace2//CallMeFromMatlab//src//InputFiles//Mode3//mode3.txt");
	Bremo bremo=new Bremo(fileCP,false);
	bremo.run();
	bremo=null;
	}

	public  Bremo(String absolutePath2InputFile) {
		File fileCP= new File(absolutePath2InputFile);
		Bremo bremo=new Bremo(fileCP,false);
		bremo.run();
		bremo=null;
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
			if(calledFromGUI){
			SwingBremo.setNrOfBremoAlive();
			
			new FertigMeldungFrame(this.getName(),"<html><u>Thread</u> : Eine Fehler ist in der File <b>"+this.getName()+"</b> aufgetreten !!! <p>" +
					"\n "+e.getMessage()+"</p></html>", JOptionPane.ERROR_MESSAGE);
			SwingBremo.StateBremoThread();
		  } 
			e.stopBremo();			
		}
		try {
			r.berechnungDurchfuehren();
			if(calledFromGUI){
			new FertigMeldungFrame(this.getName(),"Thread "+this.getName()+" ist fertig!",JOptionPane.INFORMATION_MESSAGE);
			SwingBremo.PutInBremoThreadFertig(this.getName());
			}
			//fuer Verlustteilung Frank Haertel
//			boolean VERLUST=casePara.is_Verlustteilung();
			if(casePara.is_Verlustteilung() == true){ 
			      Verlustteilung verl = new Verlustteilung(casePara); 
			      verl.berechneVerluste(); 
			      System.out.println("Verlustteilung fertig"); 
			}
		  
		} catch (Exception  e) {
			this.interrupt();
			e.printStackTrace();
		}
		if (calledFromGUI) {
		    SwingBremo.StateBremoThread();
		    System.err.println("Rechungszeit:"+(System.currentTimeMillis()-SwingBremo.startTime)+" ms");
		}
		
//		//fuer Verlustteilung Frank Haertel
//		boolean VERLUST=casePara.is_Verlustteilung();
//		if(casePara.is_Verlustteilung() == true){ 
//		      Verlustteilung verl = new Verlustteilung(casePara); 
//		      verl.berechneVerluste(); 
//		      System.out.println("Verlustteilung fertig"); 
//		} 
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
		
		long begTest = new java.util.Date().getTime();	
		System.out.println(System.getProperty("java.library.path"));		
		//Um Funktionen zu testen gibt es die Klasse FunktionsTester
		//Hier einige Beisspile wie Funktionen getestet werden können
		System.out.println(System.getProperty("home"));
		File fileCP = new File("D://Daten//workspace//Bremo 2_DE//src//InputFiles//PhH//20140225/49//Inputfile_P_202_Huegel_20140225_0049-p_m.txt");
		double startTime = System.currentTimeMillis();
		Bremo bremo=new Bremo(fileCP,false);
		bremo.run();
		double finishTime = System.currentTimeMillis();
		bremo=null;
		System.out.println(finishTime-startTime+" ms");
//		FunktionsTester.testVerlustteilung(bremo);  //fuer Verlustteilung Frank Haertel
//		File fileCP = new File("src//InputFiles//Mode7_MultiZoneInitFromKiva//mode7InpFromKiva_0.txt");	
//		CasePara privateCP = new CasePara(fileCP);
//		FunktionsTester ft=new FunktionsTester(privateCP);
//		ft.estimateEGR();
//		ft.getInitialConditions_2();
//		ft.testDieselMassFracion();
	
		String[] inputFileNames;
		if(args.length>0)
			inputFileNames=args;
		else{
			String [] ifn={	

//					"PhH/Messpunkt_10_Schichtreferenz/Bremo_Inputfile_Messpunkt_10.txt"
//					"Mode3_MultiZoneInitFromKiva/INIT_11CAD_BTDC/withMixing/mode3_Mix.txt",
//					"Mode3_MultiZoneInitFromKiva/INIT_11CAD_BTDC/withDiffusion/KivaFixed/mode3_DiffusionInp.txt",
//					"Mode7/Rasterfahndung/mode7InpRF.txt"
//					"Mode7/mode7Inp_Mix1.txt"
//					"Mode3/RasterFahndung/mode3InpMix_T_IVC_351.txt",
//					"Mode7/mode7Inp_Mix6.txt",
//					"Mode7/InputFromGTPower/m7_GTPowerRed_Mix1_b_OldParameters.txt"					
//					"Mode7/InputFromGTPowerM7/m7_GT_T_INTAKE_35_Mix1_b.txt",
//					"Mode7/InputFromGTPowerM7/m7_GT_T_INTAKE_45_Mix1_b.txt",
//					"Mode7/InputFromGTPowerM7/m7_GT_T_INTAKE_54_Mix1_b.txt",
//					"Mode7/InputFromGTPowerM7/m7_GT_T_INTAKE_65_Mix1_b.txt",
//					"Mode7/InputFromGTPowerM7/m7_GT_T_INTAKE_65_Mix1_b_BestMatch.txt",
//					"Mode7/InputFromGTPowerM7/m7_GT_T_INTAKE_75_Mix1_b.txt",
//					"Mode4/InputFromGTPowerM4/m4_GT_T_INTAKE_40_Mix1_b.txt",
//					"Mode4/InputFromGTPowerM4/m4_GT_T_INTAKE_50_Mix1_b.txt",
//					"Mode4/InputFromGTPowerM4/m4_GT_T_INTAKE_60_Mix1_b.txt",
//					"Mode4/InputFromGTPowerM4/m4_GT_T_INTAKE_70_Mix1_b.txt",
//					"Mode4/InputFromGTPowerM4/m4_GT_T_INTAKE_90_Mix1_b.txt",
//					"Mode8/InputFromGTPowerM8/m8_GT_T_INTAKE_40_Mix1_b.txt",
//					"Mode8/InputFromGTPowerM8/m8_GT_T_INTAKE_50_Mix1_b.txt",
//					"Mode8/InputFromGTPowerM8/m8_GT_T_INTAKE_60_Mix1_b.txt",
//					"Mode8/InputFromGTPowerM8/m8_GT_T_INTAKE_70_Mix1_b.txt",
//					"Mode8/InputFromGTPowerM8/m8_GT_T_INTAKE_80_Mix1_b.txt",
////					"Mode6/InputFromGTPowerM6/m6_GT_30_Mix1_b.txt",
//					"Mode6/InputFromGTPowerM6/m6_GT_40_Mix1_b.txt",
//					"Mode6/InputFromGTPowerM6/m6_GT_51_Mix1_b.txt",
//					"Mode6/InputFromGTPowerM6/m6_GT_60_Mix1_b.txt",
//					"Mode6/InputFromGTPowerM6/m6_GT_70_Mix1_b.txt",
//					"Mode3/InputFromGTPowerM3/m3_GT_T_INTAKE_30_Mix1_b_BestMatch.txt",
//					"Mode3/VergleichMitChemkin/mode3_Compare2Chemkin.txt"
//					"Mode3/DERCMeeting/m3_T_IVC_351_Mix1_bTest.txt"
//					"Mode3/InputFromGT/m3_GTPowerReducedInput_Mix1_b_DERCConstants.txt",
//					"InputFromGT/Mode3/m3_GTPowerReducedInput_Mix6.txt",
//					"Mode3_MultiZoneInitFromKiva/INIT_11CAD_BTDC/VergleichMitChemkin/mode3_Compare2Chemkin.txt"
			};
			inputFileNames=ifn;
		}		
//		String paras[][]=new String[9][];	
//		String line[]={"multiZoneInitFile",	"[-]",	":=","01_zones.txt"};
//		paras[0]=line;	
//		String line2[]={"multiZoneInitFile",	"[-]",	":=","02_zones.txt"};
//		paras[1]=line2; 
//		String line3[]={"multiZoneInitFile",	"[-]",	":=","05_zones.txt"};
//		paras[2]=line3;
//		String line4[]={"multiZoneInitFile",	"[-]",	":=","10_zones.txt"};
//		paras[3]=line4;
//		String line5[]={"multiZoneInitFile",	"[-]",	":=","20_zones.txt"};
//		paras[4]=line5;
//		String line6[]={"multiZoneInitFile",	"[-]",	":=","30_zones.txt"};
//		paras[5]=line6;
//		String line7[]={"multiZoneInitFile",	"[-]",	":=","40_zones.txt"};
//		paras[6]=line7;
//		String line8[]={"multiZoneInitFile",	"[-]",	":=","50_zones.txt"};	
//		paras[7]=line8;
//		String line9[]={"multiZoneInitFile",	"[-]",	":=","100_zones.txt"};
//		paras[8]=line9;	
		String paras[][]=new String[11][];
		double step=0.5;
		for(int i=0;i<10;i++){
			double a=0.5+i*step;
			String line[]={"C_Mix","[-]",	":=",""+a};
			paras[i]=line;
		}
		String line[]={"C_Mix","[-]",	":=",""+8};
		paras[10]=line;
			
//		automaticRun2(inputFileNames,paras);
		
		postProcessKiva();		
//		automaticRun(inputFileNames);
//		rasterFahndung(inputFileNames);
		multiFileRun(inputFileNames);		
		Double secs = new Double((new java.util.Date().getTime() - begTest)*0.001);
		System.out.println("run time " + secs + " secs");
		System.out.println("Done");
		System.exit(0);
	}	

	
	private static void postProcessKiva(){
		String path[]= {
//				"y:\\kivaCoarse\\mode3\\TurbIncrease\\mode3_ReferenceSimplifiedStockPistonHoriz\\post\\multi\\multi_zone_kiva_dump.",				
//				"y:\\mode3Post\\test\\multi_zone_kiva_dump.",
//				"E:\\ERC Daten\\Kiva\\ReRunFixedMesh\\FixedMeshInitialsFromYifeng\\mode7\\mode7_370\\Post\\multi_zone_kiva_dump.",
//				"E:\\ERC Daten\\Kiva\\KivaCoarse\\mode3\\TurbIncrease\\mode3_ReferenceSimplifiedStockPistonHoriz\\20130725_Post\\multi_zone_kiva_dump."
//				"y:\\mode3Post\\multi_zone_kiva_dump.",
//				"y:\\mode3Post\\multi_zone_kiva_dump.",
//				"y:\\mode4Post\\multi_zone_kiva_dump.",
//				"y:\\mode6Post\\multi_zone_kiva_dump.",
//				"y:\\mode7Post\\multi_zone_kiva_dump.",
//				"y:\\mode8Post\\multi_zone_kiva_dump.",
				};
		for(int i=0; i<path.length;i++){
		File fileCP = new File("src//KivaPostProcessing//dummyInput.txt");			
		CasePara CP=null;
		try {
			CP = new CasePara(fileCP);
		} catch (ParameterFileWrongInputException e) {				
			e.stopBremo();
		}
		KivaPostProcessor kpp=new KivaPostProcessor();			
		kpp.multiPostProcess(path[i],CP,46,7);
		}
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
	
	private static void optiConstants(String [] inputFileNames){
		double x0[]=new double [2];
		x0[1]=5;
		Optimizer opt=new Optimizer(x0);
		double [] out=opt.optimize();
		File file;	
		for(int i=0;i<inputFileNames.length;i++){
			file = new File("src//InputFiles//" +
					inputFileNames[i]);			
		
			double [] x00={-5,2.2,1.48};
			ConstantsOptimizer co=new ConstantsOptimizer(x00,inputFileNames[i],1);
			double[] outPut=co.optimize();
		}		
	}
	
	private static void rasterFahndung(String [] inputFileNames){
		for(int fileIndex=0;fileIndex<inputFileNames.length;fileIndex++){			

			File file = new File("src//InputFiles//" +
					inputFileNames[fileIndex]);			
			FileWriter_txt fw2=new FileWriter_txt(file.getAbsolutePath());	

			String absPath=file.getAbsolutePath();
			String fileName=file.getName();
			String temp=absPath.substring(0, file.getAbsolutePath().lastIndexOf(fileName));
			String stfrName=temp+"APR_"+fileName;			
			String p=temp+"corr_"+fileName;
			FileWriter_txt corrDataWriter=new FileWriter_txt(p);	
			String header[]={"C1", "C2", "xcorr", "rms"};
			corrDataWriter.writeTextLineToFile(header, false);

			double c1[]=new double [6];
//			c1[0]=0.05;
//			c1[25]=2.95;
			for(int u=0;u<c1.length;u++){
				c1[u]=u*0.1+0.3;
			}		
//			double c2[]={0.01,0.05,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,0.99999};
//			double c2[]=new double [21];
//			for(int u=0;u<c2.length;u++){
//				c2[u]=u*0.05+0.1;
//			}	
//			double c2[]={1,2,5,10,15,25,55,100,155};
			double c2[]={0.05,0.15,0.25,0.35,0.45,0.55};
//			double c2[]=new double[15];
//			for(int u=0;u<c2.length;u++){
//				c2[u]=u*0.05+0.1;
//			}	
			for(int c1Idx=0;c1Idx<c1.length;c1Idx++){
				for(int c2Idx=0;c2Idx<c2.length; c2Idx++){
					String []s={"C_Mix\t[-]\t:=\t"+c1[c1Idx]+"\n", 
							"C_MixRad\t[-]\t:=\t"+ c2[c2Idx]+"\n\n"};
					fw2.writeTextLineToFile(s, true);				
					Bremo bremo=new Bremo(file,false);
					bremo.run();
					bremo=null;	
					System.gc();
					SimpleTXTFileReader sf=new SimpleTXTFileReader(stfrName);
					double [][] data= sf.get_dataTransposed();			
					int iter=0;				
					int indexStart=0, indexEnd=0;	
					double a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0;
					do{
						if(data[0][iter]<=-15)
							indexStart=indexStart+1;
						if(data[0][iter]<=14)
							indexEnd=indexEnd+1;	

						if(data[0][iter]<=-6)
							a1=data[5][iter]-data[6][iter];
						if(data[0][iter]<=-2)
							a2=data[5][iter]-data[6][iter];
						if(data[0][iter]<=1)
							a3=data[5][iter]-data[6][iter];
						if(data[0][iter]<=3)
							a4=data[5][iter]-data[6][iter];
						if(data[0][iter]<=5)
							a5=data[5][iter]-data[6][iter];
						if(data[0][iter]<=8)
							a6=data[5][iter]-data[6][iter];
						if(data[0][iter]<=13)
							a7=data[5][iter]-data[6][iter];						
						iter=iter+1;
					}while(iter<data[0].length);
					double rms=a1*a1+a2*a2+a3*a3+a4*a4+a5*a5+a6*a6+a7*a7;
					rms=Math.sqrt(rms);

					int l=indexEnd-indexStart+1;		
					double [] pExp=new double [l];
					double [] pBremo=new double [l];
					for(int i=0;i<l;i++){
						pExp[i]=data[5][i+indexStart];
						pBremo[i]=data[6][i+indexStart];
					}
					double xc=-5;
					xc=MatLibBase.crossCorr0(pExp, pBremo);

					double sToWrite[]=new double[4];
					sToWrite[0]=c1[c1Idx];
					sToWrite[1]=c2[c2Idx];			
					sToWrite[2]=(xc);
					sToWrite[3]=(rms);
					corrDataWriter.writeLineToFile(sToWrite, true);

				}

			}	


		}

	}
	
	private static void automaticRun2(String inputFileNames[],String inputParameter[][]){	
		for(int i=0;i<inputFileNames.length;i++){
			for (int u=0;u<inputParameter.length;u++){
				File f=new File("src//InputFiles//" +inputFileNames[i]);
				if(!f.isFile()){
					try{
						throw new BirdBrainedProgrammerException("This file: \n"+ f.getAbsolutePath() +
								"\n does not exist");
					}catch(BirdBrainedProgrammerException ipe){
						ipe.stopBremo();
					}
				}
				Path source=Paths.get(f.getAbsolutePath());
				String name=f.getName();
				String nameWithoutExt2=name.substring(0, name.lastIndexOf("."));			
				Path target=Paths.get(source.getParent()+"//" +nameWithoutExt2+"_"+u+".txt");				
				try {
					Files.copy(source, target);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				File newFile=new File(target.toString());
				FileWriter_txt fw2=new FileWriter_txt(newFile.getAbsolutePath());	
				fw2.writeTextLineToFile(inputParameter[u], true);
				Bremo bremo=new Bremo(newFile,false);
				bremo.run();
				bremo=null;
			}			

		}		
	}
	
	
	private static void automaticRun(String [] inputFileNames){

		String [][] runs=new String [7][];
//		String []s0={"mixingProcess\t [-]\t :=\t 0",	
//				"EntrainmentFaktor\t[-]\t :=\t 2.9000000000", 
//				"ProfilFaktor\t	[-]\t	:=\t	5.00000e-01",
//				"C_Mix\t	[-]\t	:=\t	15.0028700000",
//				"C_MixRad\t	[-]\t	:=\t	1.48000000000",	
//				"EntrainmentFaktor_2\t[-]\t :=\t 2.9000000000", 
//				"ProfilFaktor_2\t	[-]\t	:=\t	5.00000e-01",
//				"C_Mix_2\t	[-]\t	:=\t	15.0028700000",
//				"C_MixRad_2\t	[-]\t	:=\t	1.48000000000","\n\n"};
//		runs[0]=s0;	
		
		String []s1={"mixingProcess\t [-]\t :=\t 1",	
				"EntrainmentFaktor\t[-]\t :=\t 2.9", 
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t	1.76541345",
				"C_MixRad\t	[-]\t	:=\t	1.48e+00",	
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9", 
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t	1.76541345",
				"C_MixRad_2\t	[-]\t	:=\t	1.48","\n\n"};
		runs[1]=s1;
		
		
//		String []s2={"mixingProcess\t [-]\t :=\t 2",	
//				"EntrainmentFaktor\t[-]\t :=\t 2.9000000000", 
//				"ProfilFaktor\t	[-]\t	:=\t	5.00000e-01",
//				"C_Mix\t	[-]\t	:=\t	1.0580028700000",
//				"C_MixRad\t	[-]\t	:=\t	1.48e+00",	
//				"EntrainmentFaktor_2\t[-]\t :=\t 2.9000000000", 
//				"ProfilFaktor_2\t	[-]\t	:=\t	5.00000e-01",
//				"C_Mix_2\t	[-]\t	:=\t	1.0580028700000",
//				"C_MixRad_2\t	[-]\t	:=\t	1.48e+00","\n\n"};
//		runs[2]=s2;	
		
		String []s3={"mixingProcess\t [-]\t :=\t 3",	
				"EntrainmentFaktor\t[-]\t :=\t 2.9", 
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t	18.0028700000",
				"C_MixRad\t	[-]\t	:=\t	1.48",	
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9", 
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t	18.0028700000",
				"C_MixRad_2\t	[-]\t	:=\t	1.48","\n\n"};
		runs[3]=s3;
		
		String []s4={"mixingProcess\t [-]\t :=\t 4",	
				"EntrainmentFaktor\t[-]\t :=\t 2.9", 
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t	6.659247",
				"C_MixRad\t	[-]\t	:=\t	1.48",	
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9", 
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",
				"C_Mix_2\t	[-]\t	:=\t	6.659247",
				"C_MixRad_2\t	[-]\t	:=\t	1.48","\n\n"};
		runs[4]=s4;		

		String []s5={"mixingProcess\t [-]\t :=\t 5",	
				"EntrainmentFaktor\t[-]\t :=\t 0.65", 
				"ProfilFaktor\t	[-]\t	:=\t	0.7",	
				"EntrainmentFaktor_2\t[-]\t :=\t 0.65", 
				"ProfilFaktor_2\t	[-]\t	:=\t	0.75","\n\n"};
		runs[5]=s5;

		String []s6={"mixingProcess\t [-]\t :=\t 6",
				"EntrainmentFaktor\t[-]\t :=\t 2.9", 
				"ProfilFaktor\t	[-]\t	:=\t	0.5",
				"C_Mix\t	[-]\t	:=\t 0.4289197",
				"EntrainmentFaktor_2\t[-]\t :=\t 2.9", 
				"ProfilFaktor_2\t	[-]\t	:=\t	0.5",				
				"C_Mix_2\t	[-]\t	:=\t 0.4289197","\n\n"};
		runs[6]=s6;
		
		
		for(int i=0;i<inputFileNames.length;i++){
			for(int r=0;r<runs.length;r++){
				if(runs[r]!=null){

					//			File file = new File("src//InputFiles//" +
					//					inputFileNames[i]);	
					//			String nameWithoutExt=file.getName().substring(0, file.getName().lastIndexOf("."));
					//			String dir=file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(file.getName()));
					//			File newFile=new  File(dir +
					//					nameWithoutExt+"_"+r+".txt");
					//			
					//			file.renameTo(newFile);			
					//			FileWriter_txt fw2=new FileWriter_txt(newFile.getAbsolutePath());

					Path source=Paths.get("src//InputFiles//" +inputFileNames[i]);
					String nameWithoutExt2=inputFileNames[i].substring(0, inputFileNames[i].lastIndexOf("."));

					Path target=Paths.get("src//InputFiles//" +nameWithoutExt2+"_"+r+".txt");

					try {
						Files.copy(source, target);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					File newFile=new File(target.toString());
					FileWriter_txt fw2=new FileWriter_txt(newFile.getAbsolutePath());	


					for (int u=0;u<runs[r].length;u++){				
						String[] s=new String [1];
						s[0]=runs[r][u];
						fw2.writeTextLineToFile(s, true);
					}			
								Bremo bremo=new Bremo(newFile,false);
								bremo.run();
								bremo=null;
					//			newFile.renameTo(file);
				}
			}			
		}
		
		
		
		
	}
	
	public void test_BremoCanteraSpecies2(){
		CanteraCaller cc=new CanteraCaller(this.casePara,1);
		System.out.println("cc erzeugt");
//		cc.sprich();
		double TMin=300;
		double TMax=3000;
		int nbrIterations=27;
		double TIntervall=(TMax-TMin)/nbrIterations;
		double p=101325;
		double T;
		double h_Bremo;
		double h_Cantera;
		double erg[][]=new double [nbrIterations][3];		

		for(int i=0; i<cc.get_NbrOfSpecies();i++){		
			for(int k=0;k<nbrIterations;k++){				
				T=TMin+k*TIntervall;							
				double pVTX[]=new double[cc.get_NbrOfSpecies()+3];
				pVTX[0]=p;
				pVTX[1]=0.1;
				pVTX[2]=T;
				pVTX[i+3]=1;
				cc.set_StatepVTX(pVTX,0);				
				h_Cantera=cc.get_h_mol_Zone(0);				
				erg[k][0]=T;
				erg[k][1]=h_Cantera;
			}						
		}		
		System.out.println("hab was gemacht");
	}	

	
}
