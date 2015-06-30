package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import misc.LinInterp;

import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public class WandtemperaturFileReader {
	String [] zeitEinheiten={"[KW]",""};
	String zeitEinheit;
	private static String ext = "txt";
	private File file;
	private CasePara cp;
	private LinInterp L_Interp;
	private int punkteProArbeitsspiel;
	private double[] zeitAchse;
	private boolean convertKW2SEC=false;
	private double offsetKW;
	

	
	public WandtemperaturFileReader(CasePara CP){
	
		this.cp=CP;
		L_Interp = new LinInterp(CP);			

		File Wandtemperatur_FILE=CP.get_FileToRead("WandtemperaturFileName");		
		int indexOf=Wandtemperatur_FILE.getName().indexOf(".");
		String Wandtemperatur_FORMAT=Wandtemperatur_FILE.getName().substring(indexOf+1); //Dateiendung;
				
		
		 if( !Wandtemperatur_FORMAT.equalsIgnoreCase(ext))
			 throw new IllegalArgumentException("TXT_datei: kein gültiges Dateiformat");
		 file = Wandtemperatur_FILE;
			if (!file.isFile())
				try{ throw new ParameterFileWrongInputException("Der für das " +
						"Wandtemperaturfile angegebene Pfad zeigt nicht auf eine Datei! \n"
						+ Wandtemperatur_FILE.getAbsolutePath());
				}catch(ParameterFileWrongInputException e){
					e.stopBremo();				
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
			while((line = br.readLine()) != null ) {
				
				line=line.replaceAll(" ", "");
				line = line.replace(',','.');	
				try {
					idx++;
					String[] theline = line.split("\t");
					if(theline[0].startsWith("[")){
						zeitEinheit=theline[0];						
						
						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])){}
						else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"des Wandtemperaturtextfiles muss "+ zeitEinheiten[0]+ 
									" oder " +zeitEinheiten[1] +" sein. \n Eingegeben wurde aber "+ zeitEinheit
									);
							}
						kalibrierungErfolgreich=true;
					}
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] keine zahl ist
					if(!kalibrierungErfolgreich)
						throw new ParameterFileWrongInputException("Im angegebenen Wandtemperaturfile wurden keine Einheiten angegeben. \n" +
								"Diese muessen vor den eigentlichen Temperaturdaten in eckigen Klammern angegeben werden z.B.: \n" +
								"[KW] [K] oder ...\n" +
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
						t=Double.parseDouble(theline[0])+offsetKW;
					}
					data[0][cnt]=t; // in KW	nach Rechenbeginn				
					data[1][cnt]=Double.parseDouble(theline[1]); // in K
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();
			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"des Wandtemperaturfiles steht zwar eine richtige Einheit im Header, " +
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
}
