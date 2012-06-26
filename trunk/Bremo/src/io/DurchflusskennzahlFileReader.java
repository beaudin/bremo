package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import matLib.MatLibBase;
import misc.LinInterp;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;
/*Eine Durchflusskennzahl-Datei besteht aus drei Spalten: 
 * 1. Hub (Länge in [m] oder normalisiert auf L/D in [])
 * 2. alpha vorwärts
 * 3. alpha rückwärts
*/
public class DurchflusskennzahlFileReader {
	private static String ext = "txt";
	private String [] abszisseEinheiten={"[m]","[]"}; //Ventilhub entweder in m oder als L/D
	private String [] alphaEinheiten={"[]"};
	private String abszisseEinheit, einheit_alpha_Vor, einheit_alpha_Rueck, typ; 
	private int spalte_alpha_Vor;
	private int spalte_alpha_Rueck;
	private File file;
	private double abszisse [];
	private double alpha_Vor [];
	private double alpha_Rueck [];
	private double refDurchmesser;
	private int punkteProArbeitsspiel;
	private LinInterp L_Interp;
	private CasePara cp;

	
	public DurchflusskennzahlFileReader(CasePara CP, String neutyp){
		this.typ=neutyp;
		this.cp=CP;
		L_Interp = new LinInterp(CP);
		spalte_alpha_Vor = cp.SYS.KANAL_SPALTEN_NR_ALPHA_VOR;
		spalte_alpha_Rueck = cp.SYS.KANAL_SPALTEN_NR_ALPHA_RUECK;	
		 if(typ.equalsIgnoreCase("Einlass")){
			 if( !CP.SYS.DFKENNZAHLDATEI_EIN_FORMAT.equalsIgnoreCase(ext))
				 throw new IllegalArgumentException("TXT_datei: kein gültiges Dateiformat");
			 file = CP.SYS.DFKENNZAHL_EIN_FILE;
				if (!file.isFile())
					try{ throw new ParameterFileWrongInputException("Der für das " +
							"Durchflusskennzahlfile angegebene Pfad zeigt nicht auf eine Datei! \n"
							+ CP.SYS.DFKENNZAHL_EIN_FILE.getAbsolutePath());
					}catch(ParameterFileWrongInputException e){
						e.stopBremo();				
					}
		 }else if(typ.equalsIgnoreCase("Auslass")){
			 if( !CP.SYS.DFKENNZAHLDATEI_EIN_FORMAT.equalsIgnoreCase(ext))
				 throw new IllegalArgumentException("TXT_datei: kein gültiges Dateiformat");
			 file = CP.SYS.DFKENNZAHL_EIN_FILE;
				if (!file.isFile())
					try{ throw new ParameterFileWrongInputException("Der für das " +
							"Durchflusskennzahlfile angegebene Pfad zeigt nicht auf eine Datei! \n"
							+ CP.SYS.DFKENNZAHL_EIN_FILE.getAbsolutePath());
					}catch(ParameterFileWrongInputException e){
						e.stopBremo();				
					} 
		 }else{
			 throw new IllegalArgumentException("DurchflusskennzahlFileReader muss mit entweder \n" +
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
		boolean einheitenKorrekt=false;
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
						abszisseEinheit=theline[0];
						einheit_alpha_Vor=theline[spalte_alpha_Vor-1];
						einheit_alpha_Rueck=theline[spalte_alpha_Rueck-1];
						
						if(abszisseEinheit.equals(abszisseEinheiten[0])||abszisseEinheit.equals(abszisseEinheiten[1])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"des Durchflusskennzahl-Textfiles muss "+ abszisseEinheiten[0]+ 
									" oder " +abszisseEinheiten[1] +" (entspr. h/D) sein. Eingegeben wurde aber "+ abszisseEinheit);}							
						if(einheit_alpha_Vor.equals(alphaEinheiten[0])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der zweiten Spalte " +
									"des Durchflusskennzahl-Textfiles muss "+ alphaEinheiten[0]+ 
									" sein. Eingegeben wurde aber "+ einheit_alpha_Vor);}
						if(einheit_alpha_Rueck.equals(alphaEinheiten[0])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der dritten Spalte " +
									"des Durchflusskennzahl-Textfiles muss "+ alphaEinheiten[0]+ 
									" sein. Eingegeben wurde aber "+ einheit_alpha_Rueck);}
						
						einheitenKorrekt=true;
					}
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] kine zahl ist
					if(!einheitenKorrekt)
						throw new ParameterFileWrongInputException("Im angegebenen Durchflusskennzahlfile wurden keine Einheiten angegeben. \n" +
								"Diese muessen vor den eigentlicehn Durchflusskennzahl-Daten in eckigen Klammern angegeben werden: \n" +
								"[m] [] [] oder [] [] []\n" +
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
		   alpha_Vor = new double[(int)punkteProArbeitsspiel];
		   alpha_Rueck = new double[(int)punkteProArbeitsspiel];
		   
		   abszisse=data[0];
		   alpha_Vor=data[1];
		   alpha_Rueck=data[2];
		   		   
	}
		
	
	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
		if(abZeile<1){
			abZeile=1;
		}
		int anzSpalten=3;			
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel];
		int idx=1;
		int cnt=0;
		double t;
		if(typ.equalsIgnoreCase("Einlass")){
			if(abszisseEinheit.equalsIgnoreCase("[]")){
				refDurchmesser=cp.get_ReferenzDurchmesserEV();
			}
		 }else if(typ.equalsIgnoreCase("Auslass")){
			 if(abszisseEinheit.equalsIgnoreCase("[]")){
					refDurchmesser=cp.get_ReferenzDurchmesserAV();
				}
		 }
		
		
		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {

				if(idx>=abZeile){
					line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split("\t");
					t=Double.parseDouble(theline[0]);	//[m] oder [nix]
					if(abszisseEinheit.equalsIgnoreCase("[]")){
						data[0][cnt]=t*refDurchmesser;	//Wird in [m] verwendet, da Hubkurven in [m] sind
					   }else if(abszisseEinheit.equalsIgnoreCase("[m]")){
						   data[0][cnt]=t;
					   }else{
						   data[0][cnt]=Double.NaN;
					   }
					data[1][cnt]=Double.parseDouble(theline[spalte_alpha_Vor-1]);
					data[2][cnt]=Double.parseDouble(theline[spalte_alpha_Rueck-1]);
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"des Durchflusskennzahl-Files steht zwar eine richtige Einheit im Header, " +
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



	//TODO: Alpha werte müssen mit Ventilhuben gemacht werden...
public double get_alphaVorwaerts(double hub){
	return L_Interp.linInterPol(hub, abszisse, alpha_Vor);
}
public double get_alphaRueckwaerts(double hub){
	return L_Interp.linInterPol(hub, abszisse, alpha_Rueck);
}
public double[] get_Abszisse(){ 
	 return abszisse;
}
public int get_punkteProArbeitsspiel(){
	return punkteProArbeitsspiel;
}
	
}
