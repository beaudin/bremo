package KivaPostProcessing;

import java.io.File;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;
import io.FileWriter_txt;

public class KivaPostProcessor {
	

	public KivaPostProcessor() {
		
	} 
	
		public void  multiPostProcess(String path, CasePara cp,int nbrOfFiles, int nbrOfInjHoles){	
//		KivaPostProcessingThread[] kpp=new KivaPostProcessingThread[nbrOfFiles];	
//		for(int i=0; i<nbrOfFiles;i++)kpp[i]=new KivaPostProcessingThread(path+(i+1),cp);	
//		for(int i=0; i<nbrOfFiles;i++)kpp[i].postProcess();	
//		List<Future<KivaPostProcessor>> futuresList = new ArrayList<Future<KivaPostProcessor>>();
//		int nbrOfThreads=Runtime.getRuntime().availableProcessors();
//		nbrOfThreads=1;
//		ExecutorService eservice = Executors.newFixedThreadPool(nbrOfThreads);
//
//		for(int index = 0; index < kpp.length; index++)
//			futuresList.add(eservice.submit(kpp[index]));
//
//		Object taskResult;
//		for(Future<KivaPostProcessor> future:futuresList) {
//			try {
//				taskResult = future.get();                        
//			}
//			catch (InterruptedException e) {
//				System.out.println("F**K");
//				System.exit(0);}
//			catch (ExecutionException e) {
//				System.out.println("F**K");
//				e.getCause();
//				e.getMessage();
//				System.exit(0);
//				}
//		}
//		//if this is not called there will be too many threads alive --> out of memory
//		eservice.shutdownNow();
//		for(int i=0;i<kpp.length;i++)System.out.println(kpp[i].get_CA());		
		
			
		//output the lambda file	
//		int nbrOfBins=kpp[0].get_lambdaClasses().length;
//		//creating header for mass and volume
//		String [] header=new String[nbrOfBins+1];
//		header[0]="CA/ Lambda";		
//		for(int i=0;i<nbrOfBins-1;i++)header[i+1]=
//				kpp[0].get_lambdaClasses()[i]+"<Lam<"+ kpp[0].get_lambdaClasses()[i+1];
//		header[nbrOfBins]="Lam>"+kpp[0].get_lambdaClasses()[nbrOfBins-1];
//		double mBin[][]=new double [kpp.length][nbrOfBins+1];
//		double volBin[][]=new double [kpp.length][nbrOfBins+1];
//		double doh[][]=new double [kpp.length][2];
//		for(int i=0;i<kpp.length;i++){
//			doh[0][i]=kpp[i].get_CA();
//			doh[1][i]=kpp[i].get_DOH();
//			mBin[i][0]=kpp[i].get_CA();
//			volBin[i][0]=kpp[i].get_CA();
//			for(int binIdx=0;binIdx<nbrOfBins;binIdx++){
//				mBin[i][binIdx+1]=kpp[i].get_mBin()[binIdx];
//				volBin[i][binIdx+1]=kpp[i].get_volBin()[binIdx];
//			}
//		}
	String header[]=null;
	int nbrOfBins=-5;
	double mBin[][]=null, volBin[][]=null;
	double doh[][]=new double [nbrOfFiles][3];
	for(int idxFile=0; idxFile<nbrOfFiles;idxFile++){
		KivaPostProcessingThread kpp=new KivaPostProcessingThread(path+(idxFile+1),cp,nbrOfInjHoles);
		kpp.postProcess();
		
		if(header==null&&nbrOfBins==-5){
			nbrOfBins=kpp.get_lambdaClasses().length;
			//creating header for mass and volume
			header=new String[nbrOfBins+1];
			header[0]="CA/ Lambda";	
			for(int i=0;i<nbrOfBins-1;i++)header[i+1]=
					kpp.get_lambdaClasses()[i]+"<Lam<"+ kpp.get_lambdaClasses()[i+1];
			header[nbrOfBins]="Lam>"+kpp.get_lambdaClasses()[nbrOfBins-1];			
			mBin=new double[nbrOfFiles][nbrOfBins+1];
			volBin=new double[nbrOfFiles][nbrOfBins+1];
		} 		
		
			doh[idxFile][0]=kpp.get_CA();
			doh[idxFile][1]=kpp.get_DOH();
			doh[idxFile][2]=kpp.get_LambdaTot();
			mBin[idxFile][0]=kpp.get_CA();
			volBin[idxFile][0]=kpp.get_CA();
			for(int binIdx=0;binIdx<nbrOfBins;binIdx++){
				mBin[idxFile][binIdx+1]=kpp.get_mBin()[binIdx];
				volBin[idxFile][binIdx+1]=kpp.get_volBin()[binIdx];
			}		
	}
	
	
		String fs=File.separator;
		int a=path.lastIndexOf(fs);
		String path1=path.substring(0, a)+"\\lambdaHist_m.txt";	
		FileWriter_txt fw1=new FileWriter_txt(path1);
		fw1.writeTextLineToFile(header, false);
		fw1.writeMatrixToFile(mBin, true);
		
		String path2=path.substring(0, a)+"\\lambdaHist_Vol.txt";	
		FileWriter_txt fw2=new FileWriter_txt(path2);
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(volBin, true);			

		String path3=path.substring(0, a)+"\\DOH.txt";	
		FileWriter_txt fw3=new FileWriter_txt(path3);
		String h[]={"CA","DOH [-]"};
		fw3.writeTextLineToFile(h, false);
		fw3.writeMatrixToFile(doh, true);			
	}		

}
