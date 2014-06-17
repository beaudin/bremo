package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

/*Eine Brennverlauf-Datei besteht aus zwei Spalten: 
 * 1. Kurbelwinkel [KWnZOT] oder [s]
 * 2. Brennverlauf in [J/s]
 * In der ersten Zeile steht der Spaltenname, in der zweiten Zeile die EINHEIT!
*/


public class BrennverlaufFileReader {
	
	private static String ext = ".txt";
	private int spalte_dQburn;
	private int anzahlZyklen;
	private double dauerASP;
	protected CasePara CP;
	private File file;
	private String zeitEinheit;
	private boolean convertKW2SEC=false;
	private String einheitdQburn;
	private String [] zeitEinheiten={"[KWnZOT]","[s]","Kurbelwinkel[°KW]"};
	private int punkteProArbeitsspiel;
	private double  dQburn [];
	private double zeitAchse [];
	private int anzSpalten=2;
	
	public BrennverlaufFileReader( CasePara cp, String pfad,
			int kanal_dQburn,
			double dauerASP){
		
		this.CP=cp;
		spalte_dQburn = kanal_dQburn;
		this.dauerASP=dauerASP;
		anzahlZyklen=1; 
		 
		if( !pfad.endsWith(ext))
			 throw new IllegalArgumentException("TXT_datei: kein gültiges Dateiformat");
		
		file = new File(pfad);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException("Der für das " +
					"Brennverlaufsfile angegebene Pfad zeigt nicht auf eine Datei! \n"
					+ pfad);
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
		boolean EinheitEingelesen=false;
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

					if(theline[0].startsWith("[")|theline[0].equals("Kurbelwinkel[°KW]")){
						zeitEinheit=theline[0];
						if(zeitEinheit.contains("KW")|zeitEinheit.contains("CAD")) //steht irgendwo was von Kurbelwinkeln...
							convertKW2SEC=true; //...dann Kurbelwinkel in Zeit umrechnen
						
						einheitdQburn=theline[spalte_dQburn-1];
						
						
						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])||zeitEinheit.equals(zeitEinheiten[2])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"des Brennverlaufstextfiles muss "+ zeitEinheiten[0]+ 
									" oder " +zeitEinheiten[1] +" sein. Eingegeben wurde aber "+ zeitEinheit);}							
						
					//	kf_pZyl=set_kalibrierfaktor(einheitPZyl);
					//	kf_pEin=set_kalibrierfaktor(einheitPEin);
					//	kf_pAbg=set_kalibrierfaktor(einheitPAbg);
						EinheitEingelesen=true;
					}
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] keine Zahl ist
					if(!EinheitEingelesen)
						throw new ParameterFileWrongInputException("Im angegebenen Brennverlaufsfile wurden die Einheiten falsch angegeben. \n" +
								"Diese muessen vor den eigentlichen Daten in eckigen Klammern angegeben werden: \n" +
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
			punkteProArbeitsspiel = cnt; //Alles was unter den Ueberschrift und Einheit als double eingelesen werden konnte
			//punkteProArbeitsspiel = cnt-2; //ORIGINAL
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		    //File wird eingelesen
		   double[][] data = readFile(abZeile);
		   dQburn = new double[(int)punkteProArbeitsspiel];
		   
		 
		   zeitAchse=data[0];
		   dQburn= data[1];
		   
		   
		   
	}
	

		
	
/*private double set_kalibrierfaktor(String einheit) throws ParameterFileWrongInputException{
		if(einheit.equals("[bar]")){
			return 1E5;		
		}else if(einheit.equals("[Pa]")){
			return 1.0;
		}else{
			if(einheit.equals("")){
			throw new ParameterFileWrongInputException("Die Einheiten für die Druecke im Indizierdatentextfile " +
					"wurden nicht angegeben. \n Dies kann z.B. aus der Angabe einer flaschen Spaltennummer resultieren. \n" + 
					"Sollte die Spaltennummer richtig sein, muessen die Einheiten [bar] " +
					"oder [Pa] vor den Druckdaten angegeben werden."); 
			}else{
				throw new ParameterFileWrongInputException("Die Einheiten für die Drücke im Indizierdatentextfile" +
						" muessen in [bar] oder [Pa] erfolgen. \n " +
						"Die Eingabe erfolgte aber in: " +einheit); 
			}
		}	
	}	
	*/
	
	
	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
//		if(abZeile<3){ Wegen DVA-Datei direkt einlesen abgeschaltet...
//			abZeile=3;
//		}
				
					
		
			
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel];
		int idx=0;
		int cnt=0;
		double dQburn=-55.5;
		//double pZyl=-55.55,pZyl_MAX_TEMP=-1*Double.MAX_VALUE,t_pZyl_MAX_TEMP;
		double t_MIN=Double.MAX_VALUE, t_MAX=-1*Double.MAX_VALUE,t;
		
		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {
				idx++;
				if(idx>=abZeile){
					line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split("\t");
					t=Double.parseDouble(theline[0]);	//°KW oder s
					if(convertKW2SEC){
						data[0][cnt]=CP.convert_KW2SEC(t);
					}else{
						data[0][cnt]=t;
					}
					
					if(t>t_MAX){
						t_MAX= t;
					}	
					if(t<t_MIN){
						t_MIN= t;
				}					
					dQburn=Double.parseDouble(theline[spalte_dQburn-1]);
					//if(pZyl>pZyl_MAX_TEMP){
					//	pZyl_MAX_TEMP= pZyl;
					//	t_pZyl_MAX_TEMP=t;
										
					data[1][cnt]=dQburn;
				
					cnt++;
				}
				}
				//idx++;
			
			br.close();
			fr.close();
			
			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"des Brennverlaufsfiles steht zwar eine richtige Einheit im Header, " +
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
		
		//if(zeitEinheit.equals("[KWnZOT]")){
		//	if(dauerASP*(1d-1d/punkteProArbeitsspiel)-(t_MAX-t_MIN)!=0){
		//		try {
		//			throw new ParameterFileWrongInputException("Die Brennverlaufsdaten muessen ein vollstaendiges " +
		//					"Arbeitsspiel beinhalten. \n Eingegeben wurden Werte von " + t_MIN + " bis "+t_MAX+
		//					" --> BrennverlaufsdatenInputFile ueberpruefen.");
		//		} catch (ParameterFileWrongInputException e) {				
		//			e.stopBremo();
		//		}
		//	}
		//}
		
		
		return data;
	}  
	public double[] getZeitachse(){
		return zeitAchse;
	}
	public double[] getdQburn(){
		return dQburn;
	}
}
