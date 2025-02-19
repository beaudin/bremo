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

public class SimpleTXTFileReader {
	private final String ext = ".txt";
	private File file;	
	private int nbrOfDataPoints;
	private double [][] data;
	private String delimiter="\t";
	private int headerRows;
	boolean givenHeaderRows=false;

	public SimpleTXTFileReader(String path){	

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
		read_file();		
	}
	
	public SimpleTXTFileReader(String path, String delimiter, int headerRows){	

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
		this.headerRows=headerRows;
		this.delimiter=delimiter;
		this.givenHeaderRows=true;
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
				if(!this.delimiter.equalsIgnoreCase(" "))
					line=line.replaceAll(" ", "");				
				line = line.replace(',','.');	
				
				try {					
					String[] theline = line.split(delimiter);			
					Double.parseDouble(theline[0]); //wirft eine Exception wenn theline[0] kine zahl ist							
					countRows=true; //Wenn numerische Daten in den Zeilen stehen sollen diese gezaehlt werden
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
		if(this.givenHeaderRows)
			data=readFile(this.headerRows);
		else
			data = readFile(fromRow);
	}	

	private double[][] readFile(int fromRow) {
		//Datei wird ab Zeile abZeile gelesen. 1 w�re die erste Zeile...		
		if(fromRow<1){
			fromRow=1;
		}
		double[][] data=null;
		int idx=1;
		int cnt=0;		
		double t;

		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {
				if(idx>=fromRow){
					if(!this.delimiter.equalsIgnoreCase(" "))
						line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split(this.delimiter);
					if (data==null)
						data=new double[theline.length][this.nbrOfDataPoints];
					
					for(int i=0; i<theline.length;i++){
						t=Double.parseDouble(theline[i]);					
						data[i][cnt]=t;					
					}
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();			

		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("I tried to read double values but got something different");
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
	 * Returns columns as vectors
	 * @return
	 */
	public double [][] get_dataTransposed(){
		return this.data;
	}
	
	/**
	 * Returns rows as vectors
	 * @return
	 */
	public double [][] get_data(){
		return MatLibBase.transp_2d_array(this.data);
	}
	
}
