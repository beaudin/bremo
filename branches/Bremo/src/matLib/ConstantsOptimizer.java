package matLib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

import bremo.main.Bremo;
import io.FileWriter_txt;
import io.SimpleTXTFileReader;
import numal.AP_praxis_method;
import numal.Analytic_problems;

public class ConstantsOptimizer implements AP_praxis_method{
	private int n;
	private double [] x0;
	double [] in =new double[10];
	double [] out=new double [7];
	private String inputFileName;
	private int run, mixingProcess;
	private FileWriter_txt corrDataWriter;

	
	public ConstantsOptimizer(double [] x0, String  inputFileName, int mixingProcess){
		this.x0=x0;
		n=x0.length-1;		//the values are read from index 1 not 0 --> Why????
		in[0] = 1.0e-6;   	// the machine's precision
		in[1]=2.0e-3;		//relative tolerance
		in[2]=2.0e-2;		//absolute tolerance
		in[5]=100000;		//max number of funct calls
		in[6]=0.1;			//max step size
		in[7]=1;			//max scaling factor
		in[8]=0;			//number of iterations used to proof the found solution
		in[9]=5;			//if ill conditioned  in[9]<0 else  in[9]>0
		this.inputFileName=inputFileName;
		run=1;
		this.mixingProcess=mixingProcess;
		File newFile=new File("src//InputFiles//" +inputFileName);
		String absPath=newFile.getAbsolutePath();
		String fileName=newFile.getName();
		String temp=absPath.substring(0, newFile.getAbsolutePath().lastIndexOf(fileName));
		String p=temp+"xcorr_"+fileName;
		this.corrDataWriter=new FileWriter_txt(p);		
	}

	
	@Override
	public double funct(int n, double[] x) {
		
		Path source=Paths.get("src//InputFiles//" +inputFileName);
		String nameWithoutExt2=inputFileName.substring(0, inputFileName.lastIndexOf("."));
		String newName=nameWithoutExt2+"_"+run+".txt";

		Path target=Paths.get("src//InputFiles//" + newName);
		

		try {
			Files.copy(source, target);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File newFile=new File(target.toString());
		FileWriter_txt fw2=new FileWriter_txt(newFile.getAbsolutePath());	

		String [] constants={"mixingProcess\t [-]\t :=\t"+ mixingProcess,
//				"EntrainmentFaktor\t[-]\t :=\t "+x[1], 
//				"ProfilFaktor\t	[-]\t	:=\t " +x[2],
//				"EntrainmentFaktor_2\t[-]\t :=\t "+x[1], 
//				"ProfilFaktor_2\t	[-]\t	:=\t "+x[2],
				"C_Mix\t	[-]\t	:=\t " +x[1],
				"C_MixRad\t	[-]\t	:=\t "+x[2],};
		
		
		for (int u=0;u<constants.length;u++){				
			String[] s=new String [1];
			s[0]=constants[u];
			fw2.writeTextLineToFile(s, true);
		}
		
		Bremo bremo=new Bremo(newFile,false);
		bremo.run();
		bremo=null;		
			
		String absPath=newFile.getAbsolutePath();
		String fileName=newFile.getName();
		String temp=absPath.substring(0, newFile.getAbsolutePath().lastIndexOf(fileName));
		String p=temp+"APR_"+fileName;
		SimpleTXTFileReader sf=new SimpleTXTFileReader(p);
		double [][] data= sf.get_dataTransposed();
		int iter=0;
		int indexStart=0, indexEnd=0;		
		do{
			if(data[0][iter]<=-15)
				indexStart=indexStart+1;
			if(data[0][iter]<=14)
				indexEnd=indexEnd+1;
			
			iter=iter+1;
		}while(iter<data[0].length);
		
		int l=indexEnd-indexStart+1;		
		double [] pExp=new double [l];
		double [] pBremo=new double [l];
		for(int i=0;i<l;i++){
			pExp[i]=data[5][i+indexStart];
			pBremo[i]=data[6][i+indexStart];
		}				
	
		double xc=MatLibBase.crossCorr0(pExp, pBremo);
		
		double sToWrite[]=new double[2];
		sToWrite[0]=1.0*run;
		sToWrite[1]=(1-xc);
		corrDataWriter.writeLineToFile(sToWrite, true);
		run=run+1;
		return 1-xc;		
		
	}
	
	
	public double[] optimize(){
		Analytic_problems.praxis(n, x0, this, in, out);		
		return out;		
	}	

}
