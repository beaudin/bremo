package io;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import misc.LinInterp;

import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class HeatFluxFileReader {
	private final String ext = ".txt";
	private String [] timeUnits={"[CADaTDCF]","[s]"};
	private String timeUnit, unitdQw; 	
	private int column_dQw;
	private File file;	
	private boolean convertKW2SEC=false;
	private boolean convert2proSec=false;
	private int nbrOfDataPoints;
	private double [] dQw, timeLine;
	private final CasePara CP;
	private LinInterp L_Interp;

	public HeatFluxFileReader(CasePara cp, int columnToRead, File fileToRead) {
		this.CP=cp;
		this.column_dQw = columnToRead;
		
		String path=fileToRead.getAbsolutePath(); 
		if( !path.endsWith(ext))
			throw new IllegalArgumentException("TXT_file: not the right format");

		file = new File(path);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException(
					"The specified path does not point to a file \n"
					+ path);
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();				
			}
		this.L_Interp=new LinInterp(CP);
		
		read_file();	
	}
	
	//Daten Einlesen
	protected void read_file(){
		//Erste Datenzeile wird automatisch gesucht...
		String line = "";	
		int fromRow=1;	
		try {
			//finde die erste Zeile, die als Zahlen eingelesen werden kann
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			boolean countRows = false;
			int cnt = 0;
			while((line = br.readLine()) != null ) {//&& stop != true
				//Leerzeichen entfernen
				line=line.replaceAll(" ", "");
				line = line.replace(',','.');	
				try {					
					String[] theline = line.split("\t");
					if(theline[0].startsWith("[")){
						timeUnit=theline[0];
						if(timeUnit.equals("[CADaTDCF]")) //=[CADaTDCF]
							convertKW2SEC=true;

						unitdQw=theline[column_dQw-1];						

						if(timeUnit.equals(timeUnits[0])||timeUnit.equals(timeUnits[1])){}else{
							throw new ParameterFileWrongInputException("The unit of the first column must be " +
									timeUnits[0] + " or " +timeUnits[1] +
									" but the given unit is: " + timeUnit);}

						if(unitdQw.equals("[J/CAD]") ||unitdQw.equals("[J/s]")){							
						}else{
							throw new ParameterFileWrongInputException(
									"The unit of turbulence intensity must be [m/s]" 
											+" but the given unit in the file is: "+ unitdQw);}
						
						if(unitdQw.equals("[J/CAD]" ))
								convert2proSec=true;						

					}
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] kine zahl ist							
					countRows=true; //Wenn numerische Daten in den Zeilen stehen sollen diese gezaehlt werden
				}catch (ParameterFileWrongInputException pfwi){
					pfwi.stopBremo();
				}catch(Exception e) {
					fromRow++;
				}	
				if(countRows==true){
					cnt++; 
				}
			}
			br.close();
			fr.close();		
			nbrOfDataPoints = cnt;
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		//File wird eingelesen
		double[][] data = readFile(fromRow);
		this.dQw = new double[(int)nbrOfDataPoints];

		timeLine=data[0];		   
		dQw=data[1];		//turbulence intensity

	}	

	private double[][] readFile(int fromRow) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
		if(fromRow<1){
			fromRow=1;
		}
		int nbrOfColumns=2;

		double[][] data = new double[nbrOfColumns][this.nbrOfDataPoints];
		int idx=1;
		int cnt=0;		
		double t;

		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {
				if(idx>=fromRow){
					line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split("\t");
					t=Double.parseDouble(theline[0]);	//CADaTDCF or s
					if(convertKW2SEC){
						data[0][cnt]=CP.convert_KW2SEC(t);
					}else{
						data[0][cnt]=t;
					}	
					if(convert2proSec){
						data[1][cnt]=CP.convert_ProKW_2_ProSEC(
								Double.parseDouble(theline[column_dQw-1]));
					}else
						data[1][cnt]=Double.parseDouble(theline[column_dQw-1]);	
					
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();			

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
		return data;
	}  
	
	
	
	
	
	/**
	 * Wall Heat Transfer for the specified time
	 * @param time in [s]
	 * @return EPS in [J/s]
	 */
	public double get_Value(double time) {
		double lastValue=0, firstValue=0;		
		lastValue=timeLine[timeLine.length-1];
		firstValue=timeLine[0];			
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, timeLine, this.dQw);
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
