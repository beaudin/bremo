package KivaPostProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import bremoExceptions.MiscException;


public class KivaPostProcessReader {
	private File file;	
	private double [][] data;
	private int volCol, TCol,rhoCol, specCol=-5, liqFuelCol;
	private Hashtable<String,Integer> specNamesCol;
	private double p=0, ca=-5555;
	private boolean convertToMassFracs=false;
	private int nbrOfCells;
	private Vector <String> speciesNamesVec=null;
	private String speciesNames[];
	private double mTot=-5;

	public KivaPostProcessReader(String path) {
		file = new File(path);
		if (!file.isFile())
			try{ throw new IllegalArgumentException(
					"The specified path does not point to a file \n"
							+ path);
			}catch(IllegalArgumentException e){
				e.getMessage();
			}
		specNamesCol=new Hashtable<String, Integer>();
		speciesNamesVec=new Vector<String>();
		read_file();		
	}

	//Daten Einlesen
	protected void read_file(){			
		//Erste Datenzeile wird automatisch gesucht...
		String line = "";				
		Vector<Double []> vRow =new Vector<Double[]>();
		try {
			//finde die erste Zeile, die als Zahlen eingelesen werden kann
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null ) {//&& stop != true
				try {	
					Vector<String> v=new Vector<String>();	
					String[] theline = line.split(" ");	
					//delete obsolete spaces
					for(int i=0; i<theline.length;i++){		
						if(	!theline[i].equalsIgnoreCase("")){
							v.add(theline[i]);	
						}
					}		

					if(v.get(0).equalsIgnoreCase("i4")){
						for(int i=0;i<v.size();i++){
							if (v.get(i).contains("vol_"))
								volCol=i;
							if (v.get(i).contains("Temp_"))
								TCol=i;
							if (v.get(i).contains("ro_"))
								rhoCol=i;							
							if (v.get(i).contains("Liq.Fuel_"))
								liqFuelCol=i;
							if (i>0&&v.get(i).contains("[")==false){
								if(specCol==-5)specCol=i;
								specNamesCol.put(v.get(i), i);	
								speciesNamesVec.add(v.get(i));
							}
						}
						if(!v.get(volCol).contains("[cc]")||!v.get(rhoCol).contains("[g/cc]")||!v.get(liqFuelCol).contains("[g]")){
							try{
								throw new MiscException("The given units in the KivaResultsFile are not correct! \n" +
										"Possible units are: [cc] for volume and [g/cc] for density");
							}catch(MiscException me){
								me.stopBremo();
							}								
						}
					}
					//getting the crank angle
					try {	
						if(v.get(0).equalsIgnoreCase("Crank"))
							if(v.get(1).contains("[deg.")){
								ca=Double.parseDouble(v.lastElement());
							}
							else{
								try{
									throw new MiscException("The unit for the crank angle must be specified in [deg. ATDC]. " +
											"But given was: "+ v.get(2));
								}catch(MiscException me){
									me.stopBremo();
								}		
									
							}
					}catch(Exception e) {
						try{
							throw new MiscException("While getting the crank angle a value could not be parsed to a double\n"
									+ "Check if the KivaResultsFile has the right form");
						}catch(MiscException me){
							me.stopBremo();
						}
					}
					//getting the mean pressure
					try {	
						if(v.get(0).equalsIgnoreCase("Avg."))
							if(v.get(2).contains("[MPa]")){
								p=Double.parseDouble(v.lastElement())*1e6;
							}else{
								try{
									throw new MiscException("The unit for the pressure must be specified in MPa. " +
											"But given was: "+ v.get(2));
								}catch(MiscException me){
									me.stopBremo();
								}							
							}
					}catch(Exception e) {
						try{
							throw new MiscException("While getting the mean pressure a value could not be parsed to a double\n"
									+ "Check if the KivaResultsFile has the right form");
						}catch(MiscException me){
							me.stopBremo();
						}
					}
					//check if species density or mass fractions are given
					if(v.get(0).equalsIgnoreCase("Species"))
						if(v.get(1).equalsIgnoreCase("Density"))
							if(v.get(2).contains("[g/cc]"))
								convertToMassFracs=true;
							else{
								try{
									throw new MiscException("The unit for the species density must be specified in g/cc. " +
											"But given was: "+ v.get(2));
								}catch(MiscException me){
									me.stopBremo();
								}								
							}
						else if(v.get(1).equalsIgnoreCase("Mass"))
							convertToMassFracs=false;
						else{
							try{
								throw new MiscException("From the KivaResultFile it could not be determined " +
										"if mass fractions or species densities are specified.\n" +
										"Check the KivaResultFile format!");
							}catch(MiscException me){
								me.stopBremo();
							}								
						}						
					Double [] vCol=new Double [v.size()];	
					for(int i=0;i<v.size();i++)
						vCol[i]=Double.parseDouble(v.get(i)); //wirft eine Exception wenn theline[0] kine zahl ist
					vRow.add(vCol);											

				}catch(Exception e) {
					//	Double.parseDouble throws an exception if it is fed with something that can be parsed to a double						
					//that's ok. Do nothing
				}
			}
			br.close();
			fr.close();					
		}
		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		this.data=new double[vRow.size()][vRow.get(0).length];
		for(int row=0;row<vRow.size();row++)
			for(int col=0;col<vRow.get(0).length;col++)
				this.data[row][col]=vRow.get(row)[col];

		if(this.convertToMassFracs)
			this.convert2massfrac();	
		
		this.nbrOfCells=data.length;
	}			

	private void convert2massfrac(){	
		
		for(int i=0;i<data.length;i++){
			double mTot=0;
			double vol=data[i][volCol];				
			for(int col=specCol;col<data[i].length;col++) mTot=mTot+data[i][col]*vol;
			double check=Math.abs((mTot-vol*data[i][rhoCol])/mTot*100);
			System.out.println("check:"+check);
			mTot=vol*data[i][rhoCol];
			for(int col=specCol;col<data[i].length;col++) data[i][col]= data[i][col]*vol/mTot;					
			double checkSum=0;
			//make sure that mass fractions sum up to 1
			for(int col=specCol;col<data[i].length;col++) checkSum=checkSum+data[i][col];
			System.out.println("checkSum: "+checkSum);
			for(int col=specCol;col<data[i].length;col++)data[i][col]= data[i][col]/checkSum;
			checkSum=0;
			for(int col=specCol;col<data[i].length;col++) checkSum=checkSum+data[i][col];
			System.out.println("checkSum: "+ checkSum);			
		}			
	}

	/**
	 * Returns the mass fraction 
	 * @param speciesName (name of the species)
	 * @param index of the cell (or row in the KivaResutlFile)
	 * @return
	 */
	public double get_massFrac(String speciesName, int index){
		if(!specNamesCol.containsKey(speciesName)){
			try{
				throw new MiscException("The species " + speciesName +" you asked the mass fraction for was not " +
						"in the KivaResultFile");
			}catch(MiscException me){
				me.stopBremo();
			}
			return -5;				
		}else if(index>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return -5;
		}else {
			return this.data[index][specNamesCol.get(speciesName)];
		}
	}

	/**
	 * 
	 * @return pressure in [N/m^2]
	 */
	public double get_p(){
		return this.p;
	}

	public double get_crankAngle(){
		return this.ca;
	}
	/**
	 * Rerturns the density of the cell with the specified cell index (or row in the KivaResutlFile)
	 * @param cellIndex 
	 * @return density in [kg/m^3]
	 */
	public double get_rho(int cellIndex){
		if(cellIndex>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return -5;
		}else {
			return this.data[cellIndex][rhoCol]*1e3;
		}						
	}
	/**
	 * Returns the Temperature of the cell with the specified cell index (or row in the KivaResutlFile)
	 * @param cellIndex 
	 * @return Temperature [K]
	 */
	public double get_T(int cellIndex){
		if(cellIndex>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return -5;
		}else {
			return this.data[cellIndex][TCol];
		}						
	}

	/**
	 * Returns the mass of the cell with the specified cell index respectively row in the
	 * KivaResultsfile
	 * @param cellIndex respectively row 
	 * @return m in [kg]
	 */
	public double get_m(int cellIndex){
		if(cellIndex>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return -5;
		}else {
			return this.data[cellIndex][rhoCol]*data[cellIndex][volCol]*1e-3;
		}						
	}
	
	/**
	 * Returns the volume of the cell with the specified cell index respectively row in the
	 * KivaResultsfile
	 * @param cellIndex respectively row 
	 * @return V in [m^3]
	 */
	public double get_V(int cellIndex){
		if(cellIndex>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return -5;
		}else {
			return this.data[cellIndex][volCol]*1e-6;
		}						
	}
	/**
	 * Returns the mass of liquid fuel in the cell with the specified cell index respectively row in the
	 * KivaResultsfile
	 * @param cellIndex respectively row 
	 * @return V in [m^3]
	 */
	public double get_liquidFuelMass(int cellIndex){
		if(cellIndex>data.length){
			try{
				throw new MiscException("The given index doesn't exist in the data field");
			}catch(MiscException me){
				me.stopBremo();						
			}	
			return -5;
		}else {
			return this.data[cellIndex][this.liqFuelCol]*1e-3;
		}						
		
	}
	public double [][] get_data(){
		return this.data;
	}
	
	/**
	 * Returns the number of cells (respectively rows) in the given KivaResultFile 
	 * @return
	 */
	public int get_nbrOfCells(){
		return this.nbrOfCells;
	}
	
	public String [] get_SpeciesNames(){
		if(speciesNames==null)
			speciesNames=new String[speciesNamesVec.size()];		
		for(int i=0;i<speciesNamesVec.size();i++)speciesNames[i]=speciesNamesVec.get(i);		
		return speciesNames;
	}
	
	/**
	 * Returns the total mass of all cells
	 * @return mTot in [kg]
	 */
	public double get_mTot(){
		if(this.mTot==-5){
			mTot=0;
			for(int i=0;i<this.nbrOfCells;i++)mTot+=this.get_m(i);
		}		
		return this.mTot;		
	}
	
	/**
	 * Returns the total mass of all cells
	 * @return mTot in [kg]
	 */
	public double get_mLiqFuelTot(){
		double mFuelTot=0;
		for(int i=0;i<this.nbrOfCells;i++)mFuelTot+=this.get_liquidFuelMass(i);	
		return mFuelTot;		
	}

	/**
	 * Returns the volume of all cells (aka combustion chamber volume)
	 * @return total volume [m^3]
	 */
	public double get_V_Tot() {
		double volTot=0;
		for(int i=0;i<this.nbrOfCells;i++)volTot+=this.get_V(i);	
		return volTot;
	}
	
	
	
}
