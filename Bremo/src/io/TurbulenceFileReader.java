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

public class TurbulenceFileReader {
	private final String ext = ".txt";
	private String [] timeUnits={"[CADaTDCF]","[s]"};
	private String timeUnit, unitTurbIntens, unitTurbKinEnergy,unitDissipationRate; 	
	private int column_TurbIntens;
	private int column_TurbKinEnergy;
	private int column_DissipationRate;	
	private File file;	
	private boolean convertKW2SEC=false;
	private int nbrOfDataPoints;
	private double [] tke, ti, timeLine,eps;
	private final CasePara CP;
	private LinInterp L_Interp;


	public TurbulenceFileReader(CasePara cp){			
		this.CP=cp;		
		this.column_TurbIntens = CP.get_ColumnToRead("column_TI");
		this.column_TurbKinEnergy = CP.get_ColumnToRead("column_TKE");
		this.column_DissipationRate = CP.get_ColumnToRead("column_EPS");	
		String path=CP.get_FileToRead("turbulenceFileName").getAbsolutePath(); 
		
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
						if(timeUnit.contains("CADaTDCF")) //=[CADaTDCF]
							convertKW2SEC=true;

						unitTurbIntens=theline[column_TurbIntens-1];
						unitTurbKinEnergy=theline[column_TurbKinEnergy-1];
						unitDissipationRate=theline[column_DissipationRate-1];

						if(timeUnit.equals(timeUnits[0])||timeUnit.equals(timeUnits[1])){}else{
							throw new ParameterFileWrongInputException("The unit of the first column must be " +
									timeUnits[0] + " or " +timeUnits[1] +
									" but the given unit is: " + timeUnit);}

						if(unitTurbIntens.equals("[m/s]")){}else{
							throw new ParameterFileWrongInputException(
									"The unit of turbulence intensity must be [m/s]" 
											+" but the given unit in the file is: "+ unitTurbIntens);}

						if(unitTurbKinEnergy.equals("[m^2/s^2]")){}else{
							throw new ParameterFileWrongInputException(
									"The unit of the turbulence kinetic energy must be [m^2/s^2]" 
											+" but the given unit in the file is: "+ unitTurbKinEnergy);}

						if(unitDissipationRate.equals("[m^2/s^3]")){}else{
							throw new ParameterFileWrongInputException(
									"The unit of the dissipation rate must be [m^2/s^3]" 
											+" but the given unit in the file is: "+ unitDissipationRate);}

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
		this.tke = new double[(int)nbrOfDataPoints];
		this.eps = new double[(int)nbrOfDataPoints];
		this.ti = new double[(int)nbrOfDataPoints];

		timeLine=data[0];		   
		ti=data[1];		//turbulence intensity
		tke=data[2];	//turbulence kinetic energy
		eps=data[3];	//dissipation rate
	}	

	private double[][] readFile(int fromRow) {
		//Datei wird ab Zeile abZeile gelesen. 1 wäre die erste Zeile...		
		if(fromRow<1){
			fromRow=1;
		}
		int nbrOfColumns=4;

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
					data[1][cnt]=Double.parseDouble(theline[column_TurbIntens-1]);					
					data[2][cnt]=Double.parseDouble(theline[column_TurbKinEnergy-1]);
					data[3][cnt]=Double.parseDouble(theline[column_DissipationRate-1]);					
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
	 * Returns turbulence kinetic energy for the specified time 
	 * @param time in [s]
	 * @return TKE in [m^2/s^2]
	 */
	public double get_TKE(double time) {
		double lastValue=0, firstValue=0;		
		lastValue=timeLine[timeLine.length-1];
		firstValue=timeLine[0];	
		
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, timeLine, this.tke);
		}else{
			try{
				throw new ParameterFileWrongInputException("The time " + time +
						" for which you asked for TKE " +
						"is not given in data file");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			return 0;
		}
	}
	
	/**
	 * Returns dissipation rate for the specified time
	 * @param time in [s]
	 * @return EPS in [m^2/s^3]
	 */
	public double get_EPS(double time) {
		double lastValue=0, firstValue=0;		
		lastValue=timeLine[timeLine.length-1];
		firstValue=timeLine[0];	
		
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, timeLine, this.eps);
		}else{
			try{
				throw new ParameterFileWrongInputException("The time " + time +
						" for which you asked for EPS " +
						"is not given in data file");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			return 0;
		}
	}
	
	/**
	 * Returns turbulence intensity for the specified time
	 * @param time in [s]
	 * @return EPS in [m/s]
	 */
	public double get_TI(double time) {
		double lastValue=0, firstValue=0;		
		lastValue=timeLine[timeLine.length-1];
		firstValue=timeLine[0];			
		if(time>=firstValue && time<=lastValue){
			return L_Interp.linInterPol(time, timeLine, this.ti);
		}else{
			try{
				throw new ParameterFileWrongInputException("The time " + time +
						" for which you asked for TI " +
						"is not given in data file");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			return 0;
		}
	}
	
}
