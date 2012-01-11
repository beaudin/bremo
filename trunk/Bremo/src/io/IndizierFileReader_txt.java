package io;


import java.io.*;

import bremo.main.Bremo;
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

	
	public IndizierFileReader_txt(String pfad,
									int kanal_pZyl,
									int kanal_pEin,
									int kanal_pAus, double dauerASP){		
		
		spalte_pZyl = kanal_pZyl;
		spalte_pEin = kanal_pEin;
		spalte_pAbg = kanal_pAus;	
		super.dauerASP=dauerASP;
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
	
	
	
	public IndizierFileReader_txt(String fileName,int kanal_pZyl, double dauerASP){			
		dreiDruecke=false;
		spalte_pZyl = kanal_pZyl;
		spalte_pEin = spalte_pZyl;
		spalte_pAbg = spalte_pZyl;
		super.dauerASP=dauerASP;
		 
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
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] kine zahl ist
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
			br.close();
			fr.close();		
			super.punkteProArbeitsspiel = cnt;
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		    //File wird eingelesen
		   double[][] data = readFile(abZeile);
		   super.pZyl = new double[(int)punkteProArbeitsspiel];
		   super.pEin = new double[(int)punkteProArbeitsspiel];
		   super.pAbg = new double[(int)punkteProArbeitsspiel];
		 
		   super.zeitAchse=data[0];
		   super.pZyl= data[1];
		   if(dreiDruecke){ 
			   super.pEin = data[2];
			   super.pAbg = data[3];
		   }
		   		   
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
			
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel];
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
						data[0][cnt]=Bremo.get_casePara().convert_KW2SEC(t);
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
			if(super.dauerASP*(1d-1d/super.punkteProArbeitsspiel)-(t_MAX-t_MIN)!=0){
				try {
					throw new ParameterFileWrongInputException("Die Indizierdaten muessen ein vollstaendiges " +
							"Arbeitsspiel beinhalten. \n Eingegeben wurden Werte von " + t_MIN + " bis "+t_MAX+
							" --> IndizierdatenInputFile ueberpruefen.");
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}
		}
		
		
		return data;
	}   

}