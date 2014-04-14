package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import misc.LinInterp;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class BlowByFileReader {
	private final String ext = ".txt";
	private String [] zeitEinheiten={"[KWnZOT]","[s]"};
	private String zeitEinheit, dmLEinheit; 	
	private int spalte_bb;
	private File file;	
	private boolean convertKW2SEC=false;
	private int punkteProArbeitsspiel;
	private double [] dmL, zeitAchse;
	private final CasePara cp;
	private LinInterp L_Interp;
	
	public BlowByFileReader(CasePara CP, int columnToRead, File fileToRead){
		this.cp = CP;
		this.spalte_bb = columnToRead;
		
		String pfad = fileToRead.getAbsolutePath();
		if(!pfad.endsWith(ext))
			throw new IllegalArgumentException("TXT-Datei: Kein gültiges Dateiformat!");
		
		file = new File(pfad);
		
		if(!file.isFile()){
			try{
				throw new ParameterFileWrongInputException("Der für die BlowBy-Datei "+
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
						
						dmLEinheit=theline[spalte_bb-1];
						
						
						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])){}else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"der BlowBy-Datei muss "+ zeitEinheiten[0]+ 
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
		   dmL = new double[(int)punkteProArbeitsspiel];
		   
		 
		   zeitAchse=data[0];
		   dmL= data[1];
	}
	
	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
		if(abZeile<3){
			abZeile=3;
		}
				
		int anzSpalten=2;
		
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel];
		int idx=0;
		int cnt=0;
		double dml=-55.5;
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
					dml=Double.parseDouble(theline[spalte_bb-1]);
					data[1][cnt]=dml;
					cnt++;
				}
			}
			br.close();
			fr.close();
			
			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"in der BlowBy-Datei steht zwar eine richtige Einheit im Header, " +
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
	public double getdmL(double time){
		double lastValue=0, firstValue=0;		
		lastValue=zeitAchse[zeitAchse.length-1];
		firstValue=zeitAchse[0];			
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, zeitAchse, this.dmL);
		}else{
			try{
				throw new ParameterFileWrongInputException("The time " + time +
						" for which you asked for Wall Heat Transfer " +
						"is not given in data file");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			return 0;
		}
	}
}
