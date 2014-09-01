package bremo.parameter;


import io.BrennverlaufFileReader;
import io.FileWriter_txt;

import java.io.File;

import matLib.MatLibBase;
import misc.LinInterp;
import misc.VektorBuffer;
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
		
		//TODO: interne Verarbeitung des Brennverlaufes aus der vorangegangenen DVA-Rechnung einbauen?
		
		if(art=="Vorgabe"){
			File burnFile=CP.get_FileToRead("burnFileName");
		    int indexOf=burnFile.getName().indexOf(".");
			String EINGABEDATEI_FORMAT_BURN=burnFile.getName().substring(indexOf+1); //Dateiendung
			int dQburnNr=CP.get_ColumnToRead("spalte_dQburn");
//			File burnFile= CP.SYS.BRENNVERLAUF_FILE;		
			String fileName=burnFile.getName();
					
			double dauerASP=CP.SYS.DAUER_ASP_KW;
//			int dQburnNr=CP.SYS.KANAL_SPALTEN_NR_DQBURN;
			
			BrennverlaufFileReader burnReader = null;
			
			if (fileName.endsWith("txt") || fileName.endsWith("TXT") || fileName.endsWith("dva"))
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
			dQburn[0] = 0; //ersten Wert auf NULL ziehen
			dQburn[dQburn.length-1] = 0; //letzten Wert auf NULL ziehen
			zeitAchse= burnReader.getZeitachse();
			zeitAchse[0] = CP.convert_KW2SEC(-180); //ersten Wert auf -180°KW ziehen
			zeitAchse[zeitAchse.length-1] = CP.convert_KW2SEC(180); //letzten Wert auf 180°KW ziehen
		}
		
		//NEUROHR 08.2014
		if(art.equalsIgnoreCase("Vibe")){
			double anzVibe = cp.get_anzVibe();
			
			int anzSimWerte = cp.SYS.ANZ_BERECHNETER_WERTE;
			double[] zeitAchseTemp = new double[anzSimWerte];
			zeitAchseKW = new double[anzSimWerte];
			double[] dQburnTemp = new double [anzSimWerte];
			int idx_start=0, idx_ende=0;
			int idx_start_alt=anzSimWerte+1, idx_ende_alt=-1;
			for(int v=0;v<anzVibe;v++){
				boolean start=false, ende=false;
				double[] vibePara = cp.get_vibe_parameter(v); // [brennbeginn, brenndauer, formfaktor, energie]
				for(int i=0;i<anzSimWerte-1;i++){
					if(v==0){ //Zeitachsen nur einmal füllen --> gemeinsame Zeitachse definieren
						zeitAchseTemp[i] = cp.SYS.RECHNUNGS_BEGINN_DVA_SEC + i*cp.SYS.WRITE_INTERVAL_SEC;
						zeitAchseKW[i] = cp.convert_SEC2KW(zeitAchseTemp[i]);
					}
					double alpha_vibe = (zeitAchseKW[i] - vibePara[0]) / vibePara[1];
					if(alpha_vibe>=0){
						if(!start){
							idx_start = i-2;
							start = true;
						}
						dQburnTemp[i] = dQburnTemp[i] + cp.convert_ProKW_2_ProSEC(vibePara[3] / vibePara[1] 
								* 6.9 * (1 + vibePara[2]) * Math.pow(alpha_vibe, vibePara[2])
								* Math.exp(-6.9 * Math.pow(alpha_vibe, 1+vibePara[2])));
//						dQburn[alpha]=QB_ges/alpha_ges*6.9*(1+m)*(alpha_vibe^m)*EXP(-6.9*(alpha_vibe^(1+m)))
						if(dQburnTemp[i]<1e-6 && zeitAchseTemp[i]>cp.convert_KW2SEC(vibePara[0])){
							dQburnTemp[i] = 0;
							if(!ende){
								idx_ende = i+2;
								ende = true;
							}
						}
					}
				}
				if(idx_start<idx_start_alt){
					idx_start_alt = idx_start;
				}
				if(idx_ende>idx_ende_alt){
					idx_ende_alt = idx_ende;
				}
			}
			
//			Zur Verkleinerung des Arrays und damit Sparen von Speicher und Beschleunigen der Rechnung -- Neurohr
			zeitAchse = new double[idx_ende_alt - idx_start_alt];
			dQburn = new double[idx_ende_alt - idx_start_alt];
			for(int i=0;i<zeitAchse.length;i++){
				zeitAchse[i] = zeitAchseTemp[i+idx_start_alt];
				dQburn[i] = dQburnTemp[i+idx_start_alt];
			}
			zeitAchse[0]=0;
			zeitAchse[zeitAchse.length-1]=cp.SYS.RECHNUNGS_ENDE_DVA_SEC;
			dQburn[0] = 0;
			dQburn[zeitAchse.length-1] = 0;
		}
		

//		if(art=="Punktuell-OT")
//		{
//			masterEinspritzung=CP.MASTER_EINSPRITZUNG;
//			double schrittweiteSEC = CP.SYS.WRITE_INTERVAL_SEC;
//			double schrittweiteKW = Math.round(CP.convert_ProKW_2_ProSEC(schrittweiteSEC)*10)/10.0;
//			double beginnKW = -180;
//			double endeKW = 180;
//			double beginnSEC = CP.convert_KW2SEC(beginnKW);
//			int anzSimWerte=(int)((CP.convert_KW2SEC(endeKW)-CP.convert_KW2SEC(beginnKW))/schrittweiteSEC);
//			zeitAchseKW= new double[anzSimWerte];
//			zeitAchseKW[0] = beginnKW;
//			for(int i=1; i<anzSimWerte; i++){
//				zeitAchseKW[i] = beginnKW+i*schrittweiteKW;
//			}
//			zeitAchse= new double[anzSimWerte];
//			zeitAchse[0] = beginnSEC;
//			for(int i=1; i<anzSimWerte; i++){
//				zeitAchse[i]=beginnSEC+i*schrittweiteSEC;	//Zeitachsen-Erzeugung wie in Klasse "Rechnung"
//			}
//			
//			dQmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass()/schrittweiteSEC;	
//			
//				dQburn= new double[anzSimWerte];
//
//			for(int k=0; k<anzSimWerte; k++){
//				double zeitwert2 = zeitAchseKW[k];
//				if(zeitwert2>0 & zeitwert2<=schrittweiteKW)
//				dQburn[k]= dQmax;
//				else
//				dQburn[k]= 0;
//			}
//			zeitAchse[0] = CP.convert_KW2SEC(-180); //ersten Wert auf -180°KW ziehen
//			zeitAchse[zeitAchse.length-1] = CP.convert_KW2SEC(180); //letzten Wert auf 180°KW ziehen		
		
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
					{
					cnt+=1;
					}
			}
			for(int k=0; k<anzSimWerte; k++){
				double zeitwert2 = zeitAchseKW[k];
				if(zeitwert2>-schrittweiteKW && zeitwert2<schrittweiteKW)
				dQburn[k]= dQmax/cnt;
				else
				dQburn[k]= 0;
			}
			zeitAchse[0] = CP.convert_KW2SEC(-180); //ersten Wert auf -180°KW ziehen
			zeitAchse[zeitAchse.length-1] = CP.convert_KW2SEC(180); //letzten Wert auf 180°KW ziehen
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
//				zeitwertKW = zeitAchseKW[i-1]+ schrittweiteKW;
//				zeitAchseKW[i] = zeitwertKW;
				zeitAchse[i]=beginnSEC+i*schrittweiteSEC;	//Zeitachsen-Erzeugung wie in Klasse "Rechnung"
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
				{
				cnt+=1;
				}
			}
			for(int k=0; k<anzSimWerte; k++){
				double zeitwert2 = zeitAchseKW[k];
				if(zeitwert2>-schrittweiteKW && zeitwert2<schrittweiteKW)
				dQburn[k]= dQmax/cnt;

				else
				dQburn[k]= 0;
			}
			zeitAchse[0] = CP.convert_KW2SEC(-180); //ersten Wert auf -180°KW ziehen
			zeitAchse[zeitAchse.length-1] = CP.convert_KW2SEC(180); //letzten Wert auf 180°KW ziehen
	/*	double[][] matrix ={zeitAchseKW,dQburn};
		FileWriter_txt txtFile = new FileWriter_txt("F://Workspace//Bremo//src//InputFiles//Brennverlauf-HC-CO-Verluste.txt");
		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);
	*/	}
		
		if(art=="Qneu-startNeu")
		{
			startQ = Math.round(startQ*10)/10.0;
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
//				zeitwertSEC=zeitwertSEC+schrittweiteSEC;
//				zeitAchse[i] = zeitwertSEC;
				zeitAchse[i]=beginnSEC+i*schrittweiteSEC;	//Zeitachsen-Erzeugung wie in Klasse "Rechnung"
			}
			
			dQmax=wertQ/schrittweiteSEC;	
			
				dQburn= new double[anzSimWerte];
				int cnt = 0;
			for(int j=0; j<anzSimWerte; j++){
					double zeitwert = zeitAchseKW[j];
					if(zeitwert>(startQ-schrittweiteKW) && zeitwert<(startQ+schrittweiteKW))
					{
					cnt+=1;
					}
			}
			for(int k=0; k<anzSimWerte; k++){
				double zeitwert2 = zeitAchseKW[k];
				if(zeitwert2>(startQ-schrittweiteKW) && zeitwert2<(startQ+schrittweiteKW))
				dQburn[k]= dQmax/cnt;
				else
				dQburn[k]= 0;
			}
			zeitAchse[0] = CP.convert_KW2SEC(-180); //ersten Wert auf -180°KW ziehen
			zeitAchse[zeitAchse.length-1] = CP.convert_KW2SEC(180); //letzten Wert auf 180°KW ziehen

//		double[][] matrix ={zeitAchseKW,dQburn};

	    //FileWriter_txt txtFile = new FileWriter_txt("F://Workspace//Bremo//src//InputFiles//Brennverlauf-startQ.txt");
		//txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);
		}
	}
				
		
		

	
			//0.0014153574610152278
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

