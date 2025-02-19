package io;


import java.io.*;
import java.util.Vector;

import misc.LinInterp;

import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class IndizierFileReader_txt extends IndizierFileReader{

	private static String ext = ".txt";
	private String [] zeitEinheiten={"[KWnZOT]","[s]"};
	private String zeitEinheit, einheitPZyl, einheitPEin,einheitPAbg; 
	private double kf_pZyl,kf_pEin,kf_pAbg;//Faktoren f�r die Umrechnung von bar nach Pa
	private int spalte_pZyl;
	private int spalte_pEin;
	private int spalte_pAbg;	
	private File file;
	private boolean dreiDruecke=true;
	private boolean convertKW2SEC=false;
	private int linesInFile;


	public IndizierFileReader_txt(CasePara cp,String pfad,
			int kanal_pZyl,
			int kanal_pEin,
			int kanal_pAus){	
		super(cp);

		spalte_pZyl = kanal_pZyl;
		spalte_pEin = kanal_pEin;
		spalte_pAbg = kanal_pAus;			
		anzahlZyklen=1; //Bei txt-Files kann erstmal nur ein Zyklus eingelesen werden!

		if( !pfad.endsWith(ext))
			throw new IllegalArgumentException("TXT_datei: kein g�ltiges Dateiformat");

		file = new File(pfad);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException("Der f�r das " +
					"Indizierfile angegebene Pfad zeigt nicht auf eine Datei! \n"
					+ pfad);
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();				
			}
		datei_Einlesen();		
	}



	public IndizierFileReader_txt(CasePara cp,String fileName,int kanal_pZyl){		
		super(cp);
		dreiDruecke=false;
		spalte_pZyl = kanal_pZyl;
		spalte_pEin = spalte_pZyl;
		spalte_pAbg = spalte_pZyl;
		

		if( !fileName.endsWith(ext))
			throw new IllegalArgumentException("TXT_datei: kein g�ltiges Dateiformat");

		file = new File(fileName);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException("Der f�r das " +
					"Indizierfile angegebene Pfad zeigt nicht auf eine Datei!");
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();				
			}			
		datei_Einlesen();
	}



	//Daten Einlesen
	protected void datei_Einlesen(){
		//Erste Datenzeile wird automatisch gesucht...
		String line = "";

		int idx=-1;
		int abZeile=1;
		boolean kalibrierungErfolgreich=false;
		try {
			//finde die erste Zeile, die als Zahlen eingelesen werden kann
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			boolean zeilenZaehlen = false;
			int cnt = 0;
			while((line = br.readLine()) != null ) {//&& stop != true
				//Leerzeichen entfernen
				line=line.replaceAll(" ", "");
				line = line.replace(',','.');	
				try {
					idx++;
					String[] theline = line.split("\t");
					if(theline.length==4){
						//was ist wenn nur der ZylinderDruckeingelesen werden soll
						//vielleicht noch eine Abfrage bez�glich der eingegebenen Spalten 
						//						wenn eine gr��er ist als die gesamte Spalten zahl gibts einen Fehler!
					}
					if(theline[0].startsWith("[")){
						zeitEinheit=theline[0];
						if(zeitEinheit.contains("KW")) //=[KWnZOT]
							convertKW2SEC=true;

						einheitPZyl=theline[spalte_pZyl-1];
						einheitPEin=theline[spalte_pEin-1];
						einheitPAbg=theline[spalte_pAbg-1];

						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"des Indizierdatentextfiles muss "+ zeitEinheiten[0]+ 
									" oder " +zeitEinheiten[1] +" sein. Eingegeben wurde aber "+ zeitEinheit);}							

						kf_pZyl=set_kalibrierfaktor(einheitPZyl);
						kf_pEin=set_kalibrierfaktor(einheitPEin);
						kf_pAbg=set_kalibrierfaktor(einheitPAbg);
						kalibrierungErfolgreich=true;
					}
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] keine zahl ist
					if(!kalibrierungErfolgreich)
						throw new ParameterFileWrongInputException("Im angegebenen Indizierfile wurden keine Einheiten angegeben. \n" +
								"Diese muessen vor den eigentlicehn Druckdaten in eckigen Klammern angegeben werden: \n" +
								"[KW] [bar] oder [KW] [Pa] oder [s] [Pa] oder ...\n" +
								"Eine weiter Fehlermoeglichkeit: Die Spaltenangaben stimmen nicht!!");

					zeilenZaehlen=true; //Wenn numerische Daten in den Zeilen stehen sollen diese gezaehlt werden
				}catch (ParameterFileWrongInputException pfwi){
					pfwi.stopBremo();
				}catch(Exception e) {
					abZeile++;
				}	
				if(zeilenZaehlen==true){
					cnt++; 
				}
			}
			linesInFile=cnt;
			br.close();
			fr.close();				
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		//File wird eingelesen
		double[][] data = readFile(abZeile);	


		//check if timeline is not equally distributed
		double [] zeitAchseTemp=data[0];		  
		double [] delta_t=new double[zeitAchseTemp.length-1];
		for(int i=0; i<zeitAchseTemp.length-1;i++){
			delta_t[i]=zeitAchseTemp[i+1]-zeitAchseTemp[i];
		}

		double delta_min=Double.MAX_VALUE, delta_max=0;		   
		for(int i=0; i<delta_t.length;i++){
			if(delta_t[i]<delta_min)
				delta_min=delta_t[i];
			if(delta_t[i]>delta_max)
				delta_max=delta_t[i];
		}
		if(Math.abs(delta_min-delta_max)>1e-5){
			if(delta_min==0)
				delta_min=CP.SYS.WRITE_INTERVAL_SEC;
			LinInterp linInt=new LinInterp(CP); 			  
			double t=zeitAchseTemp[0];
			double tMax=zeitAchseTemp[zeitAchseTemp.length-1];
			int nbrOfValues=0;
			do{				  
				t=t+delta_min;
				nbrOfValues=nbrOfValues+1;
			}while(t<=tMax);
			zeitAchse=new double[nbrOfValues];
			pZyl=new double[nbrOfValues];
			if(dreiDruecke){ 
				pEin=new double[nbrOfValues];
				pAbg=new double[nbrOfValues];
			}
			int i=0;
			t=zeitAchseTemp[0];
			do{
				super.zeitAchse[i]=t;
				super.pZyl[i]=linInt.linInterPol(t, zeitAchseTemp, data[1]);
				if(dreiDruecke){ 
					super.pEin[i] = linInt.linInterPol(t, zeitAchseTemp, data[2]);
					super.pAbg[i] = linInt.linInterPol(t, zeitAchseTemp, data[3]);
				}	
				t=t+delta_min;
				i=i+1;
			}while(t<=tMax);			   
		}else{	
			super.zeitAchse=data[0];
			super.pZyl= data[1];
			if(dreiDruecke){ 
				super.pEin = data[2];
				super.pAbg = data[3];
			}
		}
		//interpolate if given values are not equally distributed		   
		super.punkteProArbeitsspiel = pZyl.length;  
	}




	private double set_kalibrierfaktor(String einheit) throws ParameterFileWrongInputException{
		if(einheit.equals("[bar]")){
			return 1E5;		
		}else if(einheit.equals("[Pa]")){
			return 1.0;
		}else{
			if(einheit.equals("")){
				throw new ParameterFileWrongInputException("Die Einheiten f�r die Druecke im Indizierdatentextfile " +
						"wurden nicht angegeben. \n Dies kann z.B. aus der Angabe einer flaschen Spaltennummer resultieren. \n" + 
						"Sollte die Spaltennummer richtig sein, muessen die Einheiten [bar] " +
						"oder [Pa] vor den Druckdaten angegeben werden."); 
			}else{
				throw new ParameterFileWrongInputException("Die Einheiten f�r die Dr�cke im Indizierdatentextfile" +
						" muessen in [bar] oder [Pa] erfolgen. \n " +
						"Die Eingabe erfolgte aber in: " +einheit); 
			}
		}	
	}	

	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 w�re die erste Zeile...		
		if(abZeile<1){
			abZeile=1;
		}
		int anzSpalten=2;		
		if(dreiDruecke){ 
			anzSpalten=4;			
		}

		double[][] data = new double[anzSpalten][linesInFile];
		int idx=1;
		int cnt=0;
		double pZyl=-55.55,pZyl_MAX_TEMP=-1*Double.MAX_VALUE,t_pZyl_MAX_TEMP;
		double t_MIN=Double.MAX_VALUE, t_MAX=-1*Double.MAX_VALUE,t;

		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {

				if(idx>=abZeile){
					line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split("\t");
					t=Double.parseDouble(theline[0]);	//�KW oder s
					if(convertKW2SEC){
						data[0][cnt]=super.CP.convert_KW2SEC(t);
					}else{
						data[0][cnt]=t;
					}

					if(t>t_MAX){
						t_MAX= t;
					}	
					if(t<t_MIN){
						t_MIN= t;
					}					
					pZyl=Double.parseDouble(theline[spalte_pZyl-1])*kf_pZyl;
					if(pZyl>pZyl_MAX_TEMP){
						pZyl_MAX_TEMP= pZyl;
						t_pZyl_MAX_TEMP=t;
					}					
					data[1][cnt]=pZyl;
					if(dreiDruecke){
						data[2][cnt]=Double.parseDouble(theline[spalte_pEin-1])*kf_pEin;
						data[3][cnt]=Double.parseDouble(theline[spalte_pAbg-1])*kf_pAbg;
					}
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();
			super.pZylMAX=pZyl_MAX_TEMP;

		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"des Indizierfiles steht zwar eine richtige Einheit im Header, " +
						"aber Daten sind darunter keine eingetragen... ");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}

		}catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			System.out.println(e);
		}		

		if(zeitEinheit.equals("[KWnZOT]")){
			//if(super.dauerASP*(1d-1d/super.punkteProArbeitsspiel)-(t_MAX-t_MIN)!=0){
			if((t_MAX-t_MIN)-CP.SYS.DAUER_ASP_KW>2){
				try {
					throw new ParameterFileWrongInputException("The given pressure file only contains values from: \n " +
							t_MIN + " CAD " +" to "+t_MAX+ " CAD \n" +
							" --> Reduce the gap to less than 2 CAD.");
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}
		}		
		return data;
	}   
}