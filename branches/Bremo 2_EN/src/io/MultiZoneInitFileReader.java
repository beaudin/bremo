package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import kalorik.SpeciesFactory;

import bremo.parameter.CasePara;
import bremoExceptions.MiscException;
import bremoExceptions.ParameterFileWrongInputException;

public class MultiZoneInitFileReader {
	private String ext="txt";
	private File file;
	private SimpleTXTFileReader sfr;
	private double data[][];
	private int nbrOfZones;
	private final CasePara CP;

	public MultiZoneInitFileReader(String absolutePath, CasePara cp) {
		this.CP=cp;
		if( !absolutePath.endsWith(ext))
			throw new IllegalArgumentException("TXT_file: not the right format");

		file = new File(absolutePath);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException(
					"The specified path does not point to a file \n"
							+ absolutePath);
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();				
			}

		//Reading the header=1st line
		String[] header=null;	
		int skipCols=-5;//nbr of columns not to be read in
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);					
			header= br.readLine().split("\t");			
			br.close();
			fr.close();			
			if(!header[0].equalsIgnoreCase("Lambda [-]")||
					!header[1].equalsIgnoreCase("LHV [MJ/kg]")||
					!header[2].equalsIgnoreCase("mTot [kg]")||
					!header[3].equalsIgnoreCase("p [Pa]")||
					!header[4].equalsIgnoreCase("V [m^3]")||
					!header[5].equalsIgnoreCase("T [K]")){
				try{
					throw new ParameterFileWrongInputException("The multiZoneInitInputfile \"" +
							file.getName()+"\" does not have the right form or the units are wrong");
				}catch(ParameterFileWrongInputException pfi){
					pfi.stopBremo();
				}
			}else{
				skipCols=3;
			}
		}
		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		String specNamesFromFile[]=new String[header.length-skipCols-3];
		for(int i=0;i<specNamesFromFile.length;i++)specNamesFromFile[i]=header[i+skipCols+3];
		if(!this.chkSpeciesPosition(specNamesFromFile)){
			try{
				throw new ParameterFileWrongInputException("The species in the inputFile are not specified in the wright order" +
						" or the species names are not existent in Bremo");
			}catch(ParameterFileWrongInputException pfi){
				pfi.stopBremo();
			}
		}
		
		sfr=new SimpleTXTFileReader(absolutePath);
		double dataTemp[][]=sfr.get_data();		
		this.data=new double[dataTemp.length][dataTemp[0].length-skipCols];
		for(int row=0;row<data.length;row++)
			for(int col=0;col<data[0].length;col++)
				this.data[row][col]=dataTemp[row][col+skipCols];		
		this.nbrOfZones=data.length;		
	}
	public int get_nbrOfZonesInFile(){
		return this.nbrOfZones;
	}
	
	public double[] get_pVTmi(int zoneIdx){
		if(zoneIdx>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return null;
		}else {
			return data[zoneIdx];
		}
	}
	
	public double[][] get_allpVTmi(){
		return this.data;
	}
	
	private boolean chkSpeciesPosition(String [] specNamesFromFile){
		boolean chk=true;		
		SpeciesFactory spf=CP.SPECIES_FACTROY;
		for(int i=0;i<spf.get_nbrOfSpecies();i++){
			System.out.println("Bremo: " +spf.get_Spez(i).get_name());
			System.out.println("File: " +specNamesFromFile[i]);
			if(spf.get_Spez(i).get_name().equalsIgnoreCase(specNamesFromFile[i])==false)
				chk=false;
		}		
		return chk;
	}

}
