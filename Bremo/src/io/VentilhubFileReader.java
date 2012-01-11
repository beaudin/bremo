package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import misc.LinInterp;

import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class VentilhubFileReader {
	private static String ext = "txt";
	private String [] zeitEinheiten=new String [2];
	private String zeitEinheit, einheitHub; 
	private double kf_hub;//Faktor für die Umrechnung von mm nach m
	private File file;
	private CasePara cp;
	private LinInterp L_Interp;
	private int punkteProArbeitsspiel;
	private double[] zeitAchse;
	private double[] hub;
	private boolean convertKW2SEC=false;
	private double offsetKW;
	private double offsetSEC;

	
	public VentilhubFileReader(CasePara CP, String typ){		
		this.cp=CP;
		L_Interp = new LinInterp(CP);
		
		if(typ.equalsIgnoreCase("Einlass")){
			zeitEinheiten[0]="[KWnEO]";
			zeitEinheiten[1]="[snEO]";
			offsetKW=cp.get_Einlassoeffnet_KW();
			offsetSEC=cp.get_Einlassoeffnet();
			 if( !CP.SYS.VENTILHUB_EIN_FORMAT.equalsIgnoreCase(ext))
				 throw new IllegalArgumentException("TXT_datei: kein gültiges Dateiformat");
			 file = CP.SYS.VENTILHUB_EIN_FILE;
				if (!file.isFile())
					try{ throw new ParameterFileWrongInputException("Der für das " +
							"Ventilhubfile angegebene Pfad zeigt nicht auf eine Datei! \n"
							+ CP.SYS.VENTILHUB_EIN_FILE.getAbsolutePath());
					}catch(ParameterFileWrongInputException e){
						e.stopBremo();				
					}
		 }else if(typ.equalsIgnoreCase("Auslass")){
				zeitEinheiten[0]="[KWnAO]";
				zeitEinheiten[1]="[snEO]";
				offsetKW=cp.get_Auslassoeffnet_KW();
				offsetSEC=cp.get_Auslassoeffnet();
			 if( !CP.SYS.VENTILHUB_AUS_FORMAT.equalsIgnoreCase(ext))
				 throw new IllegalArgumentException("TXT_datei: kein gültiges Dateiformat");
			 file = CP.SYS.VENTILHUB_AUS_FILE;
				if (!file.isFile())
					try{ throw new ParameterFileWrongInputException("Der für das " +
							"Ventilhubfile angegebene Pfad zeigt nicht auf eine Datei! \n"
							+ CP.SYS.VENTILHUB_AUS_FILE.getAbsolutePath());
					}catch(ParameterFileWrongInputException e){
						e.stopBremo();				
					} 
		 }else{
			 throw new IllegalArgumentException("VentilhubFileReader muss mit entweder \n" +
			 		"\"Auslass\" oder \"Einlass\" aufgerufen werden");
		 }
		datei_Einlesen();		
	 }

	//Daten Einlesen
	public void datei_Einlesen(){
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
					if(theline[0].startsWith("[")){
						zeitEinheit=theline[0];						
						if(zeitEinheit.contains("KW"));
							convertKW2SEC=true;
						einheitHub=theline[1];
						
						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"des Ventilhubtextfiles muss "+ zeitEinheiten[0]+ 
									" oder " +zeitEinheiten[1] +" sein. \n Eingegeben wurde aber "+ zeitEinheit +
									"\n Es bedeuten: " +
									"\n KWnAO: KW nach Auslassoeffnet " +
									"\n KWnEO: KW nach Einlassoeffnet");}							
						
						kf_hub=set_kalibrierfaktor(einheitHub);
						kalibrierungErfolgreich=true;
					}
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] keine zahl ist
					if(!kalibrierungErfolgreich)
						throw new ParameterFileWrongInputException("Im angegebenen Ventilhubfile wurden keine Einheiten angegeben. \n" +
								"Diese muessen vor den eigentlicehn Hubdaten in eckigen Klammern angegeben werden z.B.: \n" +
								"[KWnAO] [mm] oder [KWnEO] [m] oder [snEO] [mm] oder ...\n" +
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
			punkteProArbeitsspiel = cnt;
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		    //File wird eingelesen
		   double[][] data = readFile(abZeile);
		   zeitAchse=data[0];			
		   hub = misc.LittleHelpers.concat(data[1],data[1]);
	}	
	
	private double set_kalibrierfaktor(String einheit) throws ParameterFileWrongInputException{
		if(einheit.equals("[mm]")){
			return 1E-3;		
		}else if(einheit.equals("[m]")){
			return 1.0;
		}else{
			if(einheit.equals("")){
			throw new ParameterFileWrongInputException("Die Einheiten für die Ventilhübe im Ventilhubtextfile " +
					"wurden nicht angegeben. \n Dies kann z.B. aus der Angabe einer flaschen Spaltennummer resultieren. \n" + 
					"Sollte die Spaltennummer richtig sein, muessen die Einheiten [mm] " +
					"oder [m] vor den Hubdaten angegeben werden."); 
			}else{
				throw new ParameterFileWrongInputException("Die Einheiten für die Ventilhübe im Ventilhubtextfile" +
						" muessen in [mm] oder [m] erfolgen. \n " +
						"Die Eingabe erfolgte aber in: " +einheit); 
			}
		}	
	}	
	
	
	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
		if(abZeile<1){
			abZeile=1;
		}
		int anzSpalten=2;		
			
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel];
		int idx=1;
		int cnt=0;
		double t;		
		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {

				if(idx>=abZeile){
					line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split("\t");
					if(convertKW2SEC){						
						t=cp.convert_KW2SEC(Double.parseDouble(theline[0])+offsetKW);	
					}else{
						t=Double.parseDouble(theline[0])+offsetSEC;
					}
					data[0][cnt]=t; // in s	nach Rechenbeginn				
					data[1][cnt]=Double.parseDouble(theline[1])*kf_hub; // in m
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();
			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"des Ventilhubfiles steht zwar eine richtige Einheit im Header, " +
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
		return data;
	}

	public double get_Hub(double time) {
		double letzterWert=0, ersterWert=0;
		double dauerASP_SEC=cp.SYS.DAUER_ASP_SEC;
		letzterWert=zeitAchse[zeitAchse.length-1];
		ersterWert=zeitAchse[0];
		
		if(time>=(ersterWert+dauerASP_SEC) && time<=(letzterWert+dauerASP_SEC)){
			return L_Interp.linInterPol(time-dauerASP_SEC, zeitAchse, hub);
		}else if(time>=ersterWert && time<=letzterWert){
			return L_Interp.linInterPol(time, zeitAchse, hub);
		}else{
			return 0;
		}
	}
}
