package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import misc.LinInterp;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class EngineFileReader {
	private final String ext = ".txt";
	private String [] zeitEinheiten={"[KWnZOT]","[s]"};
	private String zeitEinheit, v_Einheit, a_Einheit;
	private double kf_vol, kf_fl;
	private int spalte_vol, spalte_fl;
	private File file;	
	private boolean convertKW2SEC=false;
	private int punkteProArbeitsspiel;
	private double [] vol, flaeche, zeitAchse;
	private final CasePara cp;
	private LinInterp L_Interp;
	
	public EngineFileReader(CasePara CP, int volColumnToRead, int flColumnToRead, File fileToRead){
		this.cp = CP;
		this.spalte_vol = volColumnToRead;
		this.spalte_fl = flColumnToRead;
		
		String pfad = fileToRead.getAbsolutePath();
		if(!pfad.endsWith(ext))
			throw new IllegalArgumentException("TXT-Datei: Kein gültiges Dateiformat!");
		
		file = new File(pfad);
		
		if(!file.isFile()){
			try{
				throw new ParameterFileWrongInputException("Der für die Motor-Datei "+
						"angegebene Pfad zeigt nicht auf eine Datei! \n"+
						pfad);
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();
			}
		}
		L_Interp = new LinInterp(cp);
		
		datei_einlesen();
	}
	
//	Daten einlesen
	private void datei_einlesen(){
		String line = "";
		
		int idx=-1;
		int abZeile=1;
		
		try{
//			Finde erste Zeile zum Einlesen
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			boolean zeilenZaehlen=false;
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
						if(zeitEinheit.contains("KW")) //=[KWnZOT]
							convertKW2SEC=true;
						
						v_Einheit=theline[spalte_vol-1];
						a_Einheit=theline[spalte_fl-1];
						
						kf_vol = set_kalibrierfaktor(v_Einheit);
						kf_fl = set_kalibrierfaktor(a_Einheit);
						
						
						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"der Motor-Datei muss "+ zeitEinheiten[0]+ 
									" oder " +zeitEinheiten[1] +" sein. Eingegeben wurde aber "+ zeitEinheit);}							
					}
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
			punkteProArbeitsspiel = cnt-2;
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		    //File wird eingelesen
		   double[][] data = readFile(abZeile);
		   vol = new double[(int)punkteProArbeitsspiel];
		   flaeche = new double[(int)punkteProArbeitsspiel];
		   
		 
		   zeitAchse=data[0];
		   vol= data[1];
		   flaeche= data[2];
	}
	
	private double set_kalibrierfaktor(String einheit) throws ParameterFileWrongInputException{
		if(einheit.equals("[m^3]") || einheit.equals("[m^2]")){
			return 1;	
		}else if(einheit.equals("[dm^2]")){
			return 100;
		}else if(einheit.equals("[l]") || einheit.equals("[dm^3]")){
			return 1000;
		}else if(einheit.equals("[cm^3]")){
			return 1000000;
		}else if(einheit.equals("[mm^3]")){
			return 1000000000;
		}else if(einheit.equals("[cm^2]")){
			return 10000;
		}else if(einheit.equals("[mm^2]")){
			return 1000000;
		}else{
			if(einheit.equals("")){
				throw new ParameterFileWrongInputException("Die Einheiten im Motortextfile " +
						"wurden nicht angegeben. \n Dies kann z.B. aus der Angabe einer flaschen Spaltennummer resultieren. \n" + 
						"Sollte die Spaltennummer richtig sein, muessen die Einheiten für Volumen und Flaechen" +
						"korrekt angegeben werden."); 
			}else{
				throw new ParameterFileWrongInputException("Die Einheiten für die Daten im Motortextfile" +
						" muessen in Volumen (z.B. [m^3]) bzw. Flaeche (z.B. [m^2]) erfolgen. \n " +
						"Die Eingabe erfolgte aber in: " +einheit); 
			}
		}	
	}

	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
		if(abZeile<3){
			abZeile=3;
		}
				
		int anzSpalten=3;
		
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel];
		int idx=0;
		int cnt=0;
		double v_zeile=-55.5;
		double a_zeile=-55.5;
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
						data[0][cnt]=cp.convert_KW2SEC(t);
					}else{
						data[0][cnt]=t;
					}
					
					if(t>t_MAX){
						t_MAX= t;
					}	
					if(t<t_MIN){
						t_MIN= t;
				}					
					v_zeile=Double.parseDouble(theline[spalte_vol-1])*kf_vol;
					a_zeile=Double.parseDouble(theline[spalte_fl-1])*kf_fl;
					data[1][cnt]=v_zeile;
					data[2][cnt]=a_zeile;
					cnt++;
				}
			}
			br.close();
			fr.close();
			
			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"in der Motor-Datei steht zwar eine richtige Einheit im Header, " +
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
	public double[] getZeitachse(){
		return zeitAchse;
	}
	public double getV_Motor(double time){
		double lastValue=0, firstValue=0;		
		lastValue=zeitAchse[zeitAchse.length-1];
		firstValue=zeitAchse[0];			
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, zeitAchse, this.vol);
		}else{
			try{
				throw new ParameterFileWrongInputException("The time " + time +
						"(" + cp.convert_SEC2KW(time) + ")" +
						" for which you asked for Volume " +
						"is not given in data file");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			return 0;
		}
	}
	
	public double getA_Motor(double time){
		double lastValue=0, firstValue=0;		
		lastValue=zeitAchse[zeitAchse.length-1];
		firstValue=zeitAchse[0];			
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, zeitAchse, this.flaeche);
		}else{
			try{
				throw new ParameterFileWrongInputException("The time " + time +
						"(" + cp.convert_SEC2KW(time) + ")" +
						" for which you asked for Wall-Area " +
						"is not given in data file");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			return 0;
		}
	}
}
