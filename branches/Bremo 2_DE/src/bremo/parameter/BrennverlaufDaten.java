package bremo.parameter;


import io.BrennverlaufFileReader;
import io.FileWriter_txt;
import java.io.File;
import matLib.MatLibBase;
import misc.LinInterp;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import bremoExceptions.ParameterFileWrongInputException;



public class BrennverlaufDaten {
	
	private double [] zeitAchse;
	double [] zeitAchse_LW;
	private final CasePara CP;
	private LinInterp L_Interp;
	private double [] dQburn;
	private double dQmax;
	private MasterEinspritzung masterEinspritzung;
	private double [] zeitAchseKW;
	private double zeitwertKW;
	private double zeitwertSEC;
	
	public BrennverlaufDaten(CasePara cp,String art,double wertQ, double startQ){
		CP=cp;
		L_Interp = new LinInterp(CP);
		this.createMe(cp, art,wertQ,startQ);
	}
	
	public BrennverlaufDaten(CasePara cp,String art,double wertQ){
		CP=cp;
		L_Interp = new LinInterp(CP);
		this.createMe(cp, art,wertQ,0);
	}
	
	public BrennverlaufDaten(CasePara cp,String art){
		CP=cp;
		L_Interp = new LinInterp(CP);
		this.createMe(cp, art, 0,0);
	}
	
	public BrennverlaufDaten(CasePara cp){
		CP=cp;
		L_Interp = new LinInterp(CP);
		this.createMe(cp, "Vorgabe",0,0);
	}
	
	public void createMe(CasePara cp, String art, double wertQ , double startQ){
		
		
		
		if(art=="Vorgabe"){
		
		File burnFile=CP.get_FileToRead("burnFileName");
	    int indexOf=burnFile.getName().indexOf(".");
		String EINGABEDATEI_FORMAT_BURN=burnFile.getName().substring(indexOf+1); //Dateiendung
		int dQburnNr=CP.get_ColumnToRead("spalte_dQburn");
//		File burnFile= CP.SYS.BRENNVERLAUF_FILE;		
		String fileName=burnFile.getName();
				
		double dauerASP=CP.SYS.DAUER_ASP_KW;
//		int dQburnNr=CP.SYS.KANAL_SPALTEN_NR_DQBURN;
		
		BrennverlaufFileReader burnReader = null;
		
		if (fileName.endsWith("txt") || fileName.endsWith("TXT"))
			burnReader=new BrennverlaufFileReader(CP,burnFile.getAbsolutePath(),dQburnNr,dauerASP);
		if(burnReader==null){
			try{
				throw new ParameterFileWrongInputException("Der Dateityp des Brennverlaufdatenfiles \""+fileName +"\"" +
				"kann nicht verarbeitet werden" );
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();
				}
	

			}
		
		dQburn = burnReader.getdQburn();
		zeitAchse= burnReader.getZeitachse();
		}
		if(art=="Punktuell-OT")
		{
			masterEinspritzung=CP.MASTER_EINSPRITZUNG;
			int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
			double schrittweiteSEC = CP.SYS.WRITE_INTERVAL_SEC;
			double schrittweiteKW = CP.convert_ProKW_2_ProSEC(schrittweiteSEC);
			double beginnKW = CP.SYS.RECHNUNGS_BEGINN_DVA_KW;
			double beginnSEC = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
			zeitAchseKW= new double[anzSimWerte];
			zeitAchseKW[0] = beginnKW;
			for(int i=1; i<anzSimWerte; i++){
				zeitwertKW = zeitAchseKW[i-1]+ schrittweiteKW;
				zeitAchseKW[i] = zeitwertKW;
			}
			zeitAchse= new double[anzSimWerte];
			zeitAchse[0] = beginnSEC;
			zeitwertSEC = beginnSEC;
			for(int i=1; i<anzSimWerte; i++){
				zeitwertSEC=zeitwertSEC+schrittweiteSEC;
				zeitAchse[i] = zeitwertSEC;
			}
			
			dQmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass()/schrittweiteSEC;	
			
				dQburn= new double[anzSimWerte];
				int cnt = 0;
			for(int j=0; j<anzSimWerte; j++){
					double zeitwert = zeitAchseKW[j];
					if(zeitwert>-schrittweiteKW && zeitwert<schrittweiteKW)
					{cnt+=1;}
			}
			for(int k=0; k<anzSimWerte; k++){
				double zeitwert2 = zeitAchseKW[k];
				if(zeitwert2>-schrittweiteKW && zeitwert2<schrittweiteKW)
				dQburn[k]= dQmax/cnt;
				else
				dQburn[k]= 0;
			}
	/*	double[][] matrix ={zeitAchseKW,dQburn};
		FileWriter_txt txtFile = new FileWriter_txt("F://Workspace//Bremo//src//InputFiles//Brennverlauf-punktuell.txt");
		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);
	*/
		}
		if(art=="QneueVorgabe-OT")
		{
			masterEinspritzung=CP.MASTER_EINSPRITZUNG;
			int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
			double schrittweiteSEC = CP.SYS.WRITE_INTERVAL_SEC;
			double schrittweiteKW = CP.convert_ProKW_2_ProSEC(schrittweiteSEC);
			double beginnKW = CP.SYS.RECHNUNGS_BEGINN_DVA_KW;
			double beginnSEC = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
			zeitAchseKW= new double[anzSimWerte];
			zeitAchseKW[0] = beginnKW;
			for(int i=1; i<anzSimWerte; i++){
				zeitwertKW = zeitAchseKW[i-1]+ schrittweiteKW;
				zeitAchseKW[i] = zeitwertKW;
			}
			zeitAchse= new double[anzSimWerte];
			zeitAchse[0] = beginnSEC;
			zeitwertSEC = beginnSEC;
			for(int i=1; i<anzSimWerte; i++){
				zeitwertSEC=zeitwertSEC+schrittweiteSEC;
				zeitAchse[i] = zeitwertSEC;
			}
			
			dQmax=wertQ/schrittweiteSEC;	
			
				dQburn= new double[anzSimWerte];
				int cnt = 0;
			for(int j=0; j<anzSimWerte; j++){
					double zeitwert = zeitAchseKW[j];
					if(zeitwert>-schrittweiteKW && zeitwert<schrittweiteKW)
					{cnt+=1;}
			}
			for(int k=0; k<anzSimWerte; k++){
				double zeitwert2 = zeitAchseKW[k];
				if(zeitwert2>-schrittweiteKW && zeitwert2<schrittweiteKW)
				dQburn[k]= dQmax/cnt;
				else
				dQburn[k]= 0;
			}
	/*	double[][] matrix ={zeitAchseKW,dQburn};
		FileWriter_txt txtFile = new FileWriter_txt("F://Workspace//Bremo//src//InputFiles//Brennverlauf-HC-CO-Verluste.txt");
		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);
	*/	}
		
		if(art=="Qneu-startNeu")
		{
			
			int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
			double schrittweiteSEC = CP.SYS.WRITE_INTERVAL_SEC;
			double schrittweiteKW = CP.convert_ProKW_2_ProSEC(schrittweiteSEC);
			double beginnKW = CP.SYS.RECHNUNGS_BEGINN_DVA_KW;
			double beginnSEC = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
			zeitAchseKW= new double[anzSimWerte];
			zeitAchseKW[0] = beginnKW;
			for(int i=1; i<anzSimWerte; i++){
				zeitwertKW = zeitAchseKW[i-1]+ schrittweiteKW;
				zeitAchseKW[i] = zeitwertKW;
			}
			zeitAchse= new double[anzSimWerte];
			zeitAchse[0] = beginnSEC;
			zeitwertSEC = beginnSEC;
			for(int i=1; i<anzSimWerte; i++){
				zeitwertSEC=zeitwertSEC+schrittweiteSEC;
				zeitAchse[i] = zeitwertSEC;
			}
			
			dQmax=wertQ/schrittweiteSEC;	
			
				dQburn= new double[anzSimWerte];
				int cnt = 0;
			for(int j=0; j<anzSimWerte; j++){
					double zeitwert = zeitAchseKW[j];
					if(zeitwert>(startQ-schrittweiteKW) && zeitwert<(startQ+schrittweiteKW))
					{cnt+=1;}
			}
			for(int k=0; k<anzSimWerte; k++){
				double zeitwert2 = zeitAchseKW[k];
				if(zeitwert2>(startQ-schrittweiteKW) && zeitwert2<(startQ+schrittweiteKW))
				dQburn[k]= dQmax/cnt;
				else
				dQburn[k]= 0;
			}
		double[][] matrix ={zeitAchseKW,dQburn};
	    //FileWriter_txt txtFile = new FileWriter_txt("F://Workspace//Bremo//src//InputFiles//Brennverlauf-startQ.txt");
		//txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);
		}
	}
				
		
		

	
			
		public double get_dQburn (double time){
		return L_Interp.linInterPol(time, zeitAchse, dQburn);		
	}
	
}
/*	public double [][] erzeugeBrennverlauf (CasePara cp, String Art){
		
		double[][] matrix= new double [2][];
		double[] brennverlauf = null;
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		zeitAchseKW= new double[zeitAchse.length];
		for(int i=0; i<zeitAchse.length; i++){
			double zeitwertKW = CP.convert_SEC2KW(zeitAchse[i]);
			zeitAchseKW[i] = zeitwertKW;
		}
		
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();	
		if (Art=="Punktuell")
			matrix[0]=zeitAchseKW;
			brennverlauf= new double[zeitAchse.length];
		for(int j=0; j<zeitAchse.length; j++){
			double zeitwert = zeitAchseKW[j];
			if(zeitwert<0 || zeitwert>0)
			brennverlauf[j]= 0;
			else
			brennverlauf[j]= Qmax;
				
		}
			
			matrix[1]=brennverlauf;
			
		
			return matrix;
		
	}
*/

