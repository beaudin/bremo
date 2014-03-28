/**
 * 
 */
package kalorik.spezies;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import berechnungsModule.Berechnung.CanteraCaller;
import bremo.parameter.CasePara;
import bremo.parameter.CasePara.MakeMeUnique;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import misc.PhysKonst;

/**
 * @author eichmeier
 *
 */
public class SpeziesFabrik {		
	//damit der Vektor mit den zu integrierenden Spezies nicht ueberfluessiferweise mit 
	//doppelten Grundspezies gefuellt ist wird jede Spezies nur einmal erzeugt!	
	private Spezies spezH;
	private Spezies spezO;
	private Spezies spezN;
	private Spezies spezH2;
	private Spezies spezOH;
	private Spezies spezCO;
	private Spezies spezNO;
	private Spezies spezO2;
	private Spezies spezH2O;
	private Spezies spezC2H5OH;
	private Spezies spezCO2;
	private Spezies spezN2;
	private Spezies spezAr;
	private Spezies spezLuftTr;
	private Spezies spezRON_91;
	private Spezies spezRON_95;
	private Spezies spezRON_98;
	private Spezies spezDiesel;
	private Spezies nC14H30;	
	private Spezies nC7H16;	
	private Spezies H2O2;
	private Spezies HO2;
	private Spezies CH3O;
	private Spezies CH2O;
	private Spezies HCO;
	private Spezies CH2;
	private Spezies CH3;
	private Spezies CH4;
	private Spezies C2H2;
	private Spezies C2H3;
	private Spezies C2H4;
	private Spezies C2H5;
	private Spezies C3H4;
	private Spezies C3H5;
	private Spezies C3H6;
	private Spezies C3H7;
	private Spezies C7H15_2;
	private Spezies C7H15O2;
	private Spezies C7KET12;
	private Spezies C5H11CO;
	private Spezies N2O;
	private Spezies NO2;
	private Spezies iC8H18;
	private Spezies C8H17;
	private Spezies C8H17OO;
	private Spezies iC8KET21;
	private Spezies C6H13CO;
	private Spezies C4H9;
	private Spezies C2H6;
	private Spezies CH2CHO;
	private Spezies CH2CO;
	private Spezies CH3O2;
	private Spezies CH3O2H;
	private Spezies soot;
	private Spezies NOx;
	private Spezies spezOHCGrill;
	

	private  int nmbrOfSpez=0; //dann wirds bei der ersten Spezies null

	private final CasePara CP;
	private Hashtable<Spezies,Integer> allSpez;
	private Vector<Spezies> allSpezVec;
	private Hashtable<String,Spezies> speciesByName;
	private CanteraCaller cc;
	boolean callsCantera,canteraInitialized;	

	
	public SpeziesFabrik(CasePara cp, MakeMeUnique mmu){
		callsCantera=cp.callsCantera(); 
		canteraInitialized=false;	
		CP=cp;		
		allSpez=new Hashtable<Spezies,Integer>();
		allSpezVec =new Vector<Spezies>();	
		speciesByName=new Hashtable<String,Spezies>();
		if(callsCantera){
			cc=new CanteraCaller(CP,1);
			//cc=new CanteraCaller("ERCmechLHVAdapted.cti","gas",1);
			Spezies spez;
			for(int i=0; i<cc.get_NbrOfSpecies();i++){
				spez=this.get_SpeciesByName(cc.get_getNameOfSpecie(i));	
				this.integrierMich(spez);
				speciesByName.put(spez.get_name(), spez);
			}		
			this.checkCanteraBremoIndex();	
			canteraInitialized=true;
		}else{			
			String[] names={"H","O","N","H2","OH","CO","NO","O2","H2O","CO2","N2"};
			Spezies spez;
			for(int i=0; i<names.length;i++){
				spez=this.get_SpeciesByName(names[i]);	
				this.integrierMich(spez);
				speciesByName.put(spez.get_name(), spez);
			}			
		}		
	}
	
	public void checkCanteraBremoIndex(){

		for(int i=0; i<cc.get_NbrOfSpecies();i++)
			if(!cc.get_getNameOfSpecie(i).equalsIgnoreCase(get_Spez(i).name)){			
				try{
					throw new BirdBrainedProgrammerException("For index " + i  +
							" Species in Cantera and Bremo are not the same!");
				}catch (BirdBrainedProgrammerException e){
					e.stopBremo();
				}
			}		
	}
			
	public void integrierMich(Spezies spez){	
		if(allSpez.containsKey(spez)==false){
			if(callsCantera&&canteraInitialized){
				try{
					throw new BirdBrainedProgrammerException("If Bremo calls Cantera no additional " +
							"Species can be integrated. Please don't use "+ spez.get_name());
				}catch (BirdBrainedProgrammerException e){
					e.stopBremo();
				}
			}
			nmbrOfSpez=nmbrOfSpez+1;
			int index=nmbrOfSpez-1;
			allSpez.put(spez,index); //Speichern der grade erzeugten Spezies im Vektor
			allSpezVec.add(index,spez);
			spez.set_isTointegrate(true);			
		}			
	}	

	
	public boolean isToIntegrate(Spezies spez){
		if(allSpez.containsKey(spez)&&spez.isToIntegrate)
			return true;
		else if(!allSpez.containsKey(spez)&&!spez.isToIntegrate) 
			return false;
		else{
			try{
				throw new MiscException("Bei Spezies "+ spez.get_name()+" stimmt die " +
						"Belegung von isToIntegrate nicht+ \n" +
						"SpeziesFabrik und Spezis haben unterschiedliche Werte");
			}catch(MiscException me){
				me.stopBremo();				
			}
			return false;
		}
	}
	
	public  int get_nbrOfSpecies(){
		if(nmbrOfSpez!=allSpez.size()){
			try{
				throw new BirdBrainedProgrammerException("The speciesFactory doesn't work properly." +
						"The nbr of Species is not the same as the size of the vector holding all species!"
						);
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
				return -5;
			}
		}else 			
			return nmbrOfSpez;
	}
	
	public int get_indexOf(Spezies spez){		
		return allSpez.get(spez);
	}
	
	public  Spezies get_Spez(int i){		
		if(i>nmbrOfSpez){
			try{
				throw new BirdBrainedProgrammerException(i + " is not a valid Index of any Species");
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			} 
		}			
		return allSpezVec.get(i);		
	}
	
	/**
	 * 
	 * @param thermodynKoeffs
	 * @return Heat of Formation in J/mol
	 */
	private double calc_deltaHf(double [][] thermodynKoeffs){		
		double T=298.15;
		double h = thermodynKoeffs[0][0] * (-1 / (T));
		h = h + thermodynKoeffs[0][1] * Math.log(T);
		h = h + thermodynKoeffs[0][2] * T;
		h = h + thermodynKoeffs[0][3] * T * T / 2;
		h = h + thermodynKoeffs[0][4] * T * T * T / 3;
		h = h + thermodynKoeffs[0][5] * T * T * T * T / 4;
		h = h + thermodynKoeffs[0][6] * T * T * T * T * T / 5;
		h = h + thermodynKoeffs[0][7];
		return h*PhysKonst.get_R_allg();
	}
	

	/**
	 * calculates the NASA-polynomial without using the factor b1.
	 * This will not return a physically meaningful value!
	 * @param thermodynKoeffs
	 * @param a low=0, high=1
	 * @param T
	 * @return
	 */
	private double calc_Poly(double [][] thermodynKoeffs,int a, double T){
		double h=0;
		if(a==0||a==1){
		h = thermodynKoeffs[a][0] * (-1 / (T));
		h = h + thermodynKoeffs[a][1] * Math.log(T);
		h = h + thermodynKoeffs[a][2] * T;
		h = h + thermodynKoeffs[a][3] * T * T / 2;
		h = h + thermodynKoeffs[a][4] * T * T * T / 3;
		h = h + thermodynKoeffs[a][5] * T * T * T * T / 4;
		h = h + thermodynKoeffs[a][6] * T * T * T * T * T / 5;
		}else{
			try{
				throw new BirdBrainedProgrammerException("a must be 1 or 0 but it is: "+a);
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return h;		
	}


	/**
	 * Recalculates the factor b1 of the NASA-Polynomials for lower temperatures
	 * in respect to the given LHV
	 * (respectively the heat of formation is changed) 
	 * @param thermodynKoeffs
	 * @param lhv in J/mol
	 * @param cAtoms
	 * @param hAtoms
	 * @param oAtoms
	 * @return
	 */
	private double calc_b1_low(double [][] thermodynKoeffs, double lhv, 
			double cAtoms, double hAtoms, double oAtoms){		
		double T=298.15;
		double h=calc_Poly(thermodynKoeffs,0,T);
		
		//Adapting the heat of formation
		double hf_Adapted=this.calc_hf4LHV(cAtoms, hAtoms, oAtoms, lhv);
		
		double b1=hf_Adapted/PhysKonst.get_R_allg()-h;

		return b1;
	}
	
	/**
	 * Recalculates the factor b1 of the NASA-Polynomials for higher temperatures
	 * in respect to the given LHV
	 * (respectively the heat of formation is changed) 
	 * @param thermodynKoeffs
	 * @param lhv in J/mol
	 * @param cAtoms
	 * @param hAtoms
	 * @param oAtoms
	 * @return
	 */
	private double calc_b1_high(double [][] thermodynKoeffs, double lhv, 
			double cAtoms, double hAtoms, double oAtoms){	
		double R_allg=PhysKonst.get_R_allg();
		double TGrenz=thermodynKoeffs[2][1];
		double poly_298 = this.calc_Poly(thermodynKoeffs,0,298.15);
		double poly_TGrenz_l = this.calc_Poly(thermodynKoeffs,0,TGrenz);
		double poly_TGrenz_h = this.calc_Poly(thermodynKoeffs,1,TGrenz);
		
		//Adapting the heat of formation
		double hf_Adapted=this.calc_hf4LHV(cAtoms, hAtoms, oAtoms, lhv);
		// h/R at TGrenz
		double h=poly_TGrenz_l-poly_298+hf_Adapted/R_allg;
		double b1=h-poly_TGrenz_h;
		return b1;
	}
	
	
	
	
	

	/**
	 * 
	 * @param cAtoms
	 * @param hAtoms
	 * @param oAtoms
	 * @param delta_hf_fuel
	 * @return Lower Heating Value in J/mol
	 */
	private double calc_LHV(double cAtoms, double hAtoms, 
			double oAtoms, double delta_hf_fuel){
		double x=cAtoms;
		double y=hAtoms;		
		double hf_O2=get_spezO2().get_delta_hf298_mol();
		double hf_CO2=get_spezCO2().get_delta_hf298_mol();
		double hf_H2O=get_spezH2O().get_delta_hf298_mol();
		double o2min=x+0.25*y-0.5*oAtoms;
		double LHV=0;
		double bb=o2min*hf_O2-x*hf_CO2-0.5*y*hf_H2O;
		if(o2min>0)
			LHV=(delta_hf_fuel+o2min*hf_O2-x*hf_CO2-0.5*y*hf_H2O);

		return LHV;
	}

	private double calc_hf4LHV(double c, double h, double o, double hu ){
		double x=c;
		double y=h;		
		double z=o;
		double hf_O2=get_spezO2().get_delta_hf298_mol();
		double hf_CO2=get_spezCO2().get_delta_hf298_mol();
		double hf_H2O=get_spezH2O().get_delta_hf298_mol();	
		double o2min=x+0.25*y-0.5*z;
		double bb=-o2min*hf_O2+x*hf_CO2+0.5*y*hf_H2O;

		return hu-o2min*hf_O2+x*hf_CO2+0.5*y*hf_H2O;	
	}
	

	private void initializeCanteraSpecies(String speciesName){
		
		if(speciesName.equalsIgnoreCase("H"))
			speciesByName.put(get_spezH().get_name(), get_spezH());		
		else if(speciesName.equalsIgnoreCase("O"))
			speciesByName.put(get_spezO().get_name(), get_spezO());
		else if(speciesName.equalsIgnoreCase("N"))
			speciesByName.put(get_spezN().get_name(), get_spezN());
		else if(speciesName.equalsIgnoreCase("H2"))
			speciesByName.put(get_spezH2().get_name(), get_spezH2());
		else if(speciesName.equalsIgnoreCase("OH"))
			speciesByName.put(get_spezOH().get_name(), get_spezOH());
		else if(speciesName.equalsIgnoreCase("CO"))
			speciesByName.put(get_spezCO().get_name(), get_spezCO());
		else if(speciesName.equalsIgnoreCase("NO"))
			speciesByName.put(get_spezNO().get_name(), get_spezNO());
		else if(speciesName.equalsIgnoreCase("O2"))
			speciesByName.put(get_spezO2().get_name(), get_spezO2());
		else if(speciesName.equalsIgnoreCase("H2O"))
			speciesByName.put(get_spezH2O().get_name(), get_spezH2O());
		else if(speciesName.equalsIgnoreCase("C2H5OH"))
			speciesByName.put(get_spezC2H5OH().get_name(), get_spezC2H5OH());
		else if(speciesName.equalsIgnoreCase("CO2"))
			speciesByName.put(get_spezCO2().get_name(), get_spezCO2());
		else if(speciesName.equalsIgnoreCase("N2"))
			speciesByName.put(get_spezN2().get_name(), get_spezN2());
		else if(speciesName.equalsIgnoreCase("Ar"))
			speciesByName.put(get_spezAr().get_name(), get_spezAr());
		else if(speciesName.equalsIgnoreCase("LuftTr"))
			speciesByName.put(get_spezLuft_trocken().get_name(), get_spezLuft_trocken());
		else if(speciesName.equalsIgnoreCase("RON_91"))
			speciesByName.put(get_spezRON_91().get_name(), get_spezRON_91());
		else if(speciesName.equalsIgnoreCase("RON_95"))
			speciesByName.put(get_spezRON_95().get_name(), get_spezRON_95());
		else if(speciesName.equalsIgnoreCase("RON_98"))
			speciesByName.put(get_spezRON_98().get_name(), get_spezRON_98());
		else if(speciesName.equalsIgnoreCase("Diesel"))
			speciesByName.put(get_spezDiesel().get_name(), get_spezDiesel());
		else if(speciesName.equalsIgnoreCase("nC14H30"))
			speciesByName.put(get_nC7H16().get_name(), get_nC14H30());
		else if(speciesName.equalsIgnoreCase("nC7H16"))
			speciesByName.put(get_nC7H16().get_name(), get_nC7H16());
		else if(speciesName.equalsIgnoreCase("H2O2"))
			speciesByName.put(get_H2O2().get_name(), get_H2O2());
		else if(speciesName.equalsIgnoreCase("HO2"))
			speciesByName.put(get_HO2().get_name(), get_HO2());
		else if(speciesName.equalsIgnoreCase("CH3O"))
			speciesByName.put(get_CH3O().get_name(), get_CH3O());
		else if(speciesName.equalsIgnoreCase("CH2O"))
			speciesByName.put(get_CH2O().get_name(), get_CH2O());
		else if(speciesName.equalsIgnoreCase("HCO"))
			speciesByName.put(get_HCO().get_name(), get_HCO());
		else if(speciesName.equalsIgnoreCase("CH2"))
			speciesByName.put(get_CH2().get_name(), get_CH2());
		else if(speciesName.equalsIgnoreCase("CH3"))
			speciesByName.put(get_CH3().get_name(), get_CH3());
		else if(speciesName.equalsIgnoreCase("CH4"))
			speciesByName.put(get_CH4().get_name(), get_CH4());
		else if(speciesName.equalsIgnoreCase("C2H2"))
			speciesByName.put(get_C2H2().get_name(), get_C2H2());
		else if(speciesName.equalsIgnoreCase("C2H3"))
			speciesByName.put(get_C2H3().get_name(), get_C2H3());
		else if(speciesName.equalsIgnoreCase("C2H4"))
			speciesByName.put(get_C2H4().get_name(), get_C2H4());
		else if(speciesName.equalsIgnoreCase("C2H5"))
			speciesByName.put(get_C2H5().get_name(), get_C2H5());
		else if(speciesName.equalsIgnoreCase("C3H4"))
			speciesByName.put(get_C3H4().get_name(), get_C3H4());
		else if(speciesName.equalsIgnoreCase("C3H5"))
			speciesByName.put(get_C3H5().get_name(), get_C3H5());
		else if(speciesName.equalsIgnoreCase("C3H6"))
			speciesByName.put(get_C3H6().get_name(), get_C3H6());
		else if(speciesName.equalsIgnoreCase("C3H7"))
			speciesByName.put(get_C3H7().get_name(), get_C3H7());
		else if(speciesName.equalsIgnoreCase("C7H15-2"))
			speciesByName.put(get_C7H15_2().get_name(), get_C7H15_2());
		else if(speciesName.equalsIgnoreCase("C7H15O2"))
			speciesByName.put(get_C7H15O2().get_name(), get_C7H15O2());
		else if(speciesName.equalsIgnoreCase("C7KET12"))
			speciesByName.put(get_C7KET12().get_name(), get_C7KET12());
		else if(speciesName.equalsIgnoreCase("C5H11CO"))
			speciesByName.put(get_C5H11CO().get_name(), get_C5H11CO());		
		else if(speciesName.equalsIgnoreCase("N2O"))
			speciesByName.put(get_N2O().get_name(), get_N2O());
		else if(speciesName.equalsIgnoreCase("NO2"))
			speciesByName.put(get_NO2().get_name(), get_NO2());
		else if(speciesName.equalsIgnoreCase("iC8H18"))
			speciesByName.put(get_iC8H18().get_name(), get_iC8H18());
		else if(speciesName.equalsIgnoreCase("C8H17"))
			speciesByName.put(get_C8H17().get_name(), get_C8H17());
		else if(speciesName.equalsIgnoreCase("C8H17OO"))
			speciesByName.put(get_C8H17OO().get_name(), get_C8H17OO());
		else if(speciesName.equalsIgnoreCase("iC8KET21"))
			speciesByName.put(get_iC8KET21().get_name(), get_iC8KET21());
		else if(speciesName.equalsIgnoreCase("C6H13CO"))
			speciesByName.put(get_C6H13CO().get_name(), get_C6H13CO());
		else if(speciesName.equalsIgnoreCase("C4H9"))
			speciesByName.put(get_C4H9().get_name(), get_C4H9());
		else if(speciesName.equalsIgnoreCase("C2H6"))
			speciesByName.put(get_C2H6().get_name(), get_C2H6());
		else if(speciesName.equalsIgnoreCase("CH2CHO"))
			speciesByName.put(get_CH2CHO().get_name(), get_CH2CHO());
		else if(speciesName.equalsIgnoreCase("CH2CO"))
			speciesByName.put(get_CH2CO().get_name(), get_CH2CO());
		else if(speciesName.equalsIgnoreCase("CH3O2"))
			speciesByName.put(get_CH3O2().get_name(), get_CH3O2());
		else if(speciesName.equalsIgnoreCase("CH3O2H"))
			speciesByName.put(get_CH3O2H().get_name(), get_CH3O2H());
		else if(speciesName.equalsIgnoreCase("soot"))
			speciesByName.put(get_soot().get_name(), get_soot());
		else if(speciesName.equalsIgnoreCase("ohc"))
			speciesByName.put(get_spezGrill().get_name(), get_spezGrill());
		else {
			try{
				throw new BirdBrainedProgrammerException("" +
						"This species has not been programmed yet!");
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		
	}	
	
public Spezies get_SpeciesByName(String speciesName){
		
		if(speciesName.equalsIgnoreCase("H"))
			return get_spezH();		
		else if(speciesName.equalsIgnoreCase("O"))
			return get_spezO();
		else if(speciesName.equalsIgnoreCase("N"))
			return get_spezN();
		else if(speciesName.equalsIgnoreCase("H2"))
			return get_spezH2();
		else if(speciesName.equalsIgnoreCase("OH"))
			return get_spezOH();
		else if(speciesName.equalsIgnoreCase("CO"))
			return get_spezCO();
		else if(speciesName.equalsIgnoreCase("NO"))
			return get_spezNO();
		else if(speciesName.equalsIgnoreCase("O2"))
			return get_spezO2();
		else if(speciesName.equalsIgnoreCase("H2O"))
			return get_spezH2O();
		else if(speciesName.equalsIgnoreCase("C2H5OH"))
			return get_spezC2H5OH();
		else if(speciesName.equalsIgnoreCase("CO2"))
			return get_spezCO2();
		else if(speciesName.equalsIgnoreCase("N2"))
			return get_spezN2();
		else if(speciesName.equalsIgnoreCase("Ar"))
			return get_spezAr();
		else if(speciesName.equalsIgnoreCase("LuftTr"))
			return get_spezLuft_trocken();
		else if(speciesName.equalsIgnoreCase("RON_91"))
			return get_spezRON_91();
		else if(speciesName.equalsIgnoreCase("RON_95"))
			return get_spezRON_95();
		else if(speciesName.equalsIgnoreCase("RON_98"))
			return get_spezRON_98();
		else if(speciesName.equalsIgnoreCase("Diesel"))
			return get_spezDiesel();
		else if(speciesName.equalsIgnoreCase("nC14H30"))
			return get_nC14H30();
		else if(speciesName.equalsIgnoreCase("nC7H16"))
			return get_nC7H16();
		else if(speciesName.equalsIgnoreCase("H2O2"))
			return get_H2O2();
		else if(speciesName.equalsIgnoreCase("HO2"))
			return get_HO2();
		else if(speciesName.equalsIgnoreCase("CH3O"))
			return get_CH3O();
		else if(speciesName.equalsIgnoreCase("CH2O"))
			return get_CH2O();
		else if(speciesName.equalsIgnoreCase("HCO"))
			return get_HCO();
		else if(speciesName.equalsIgnoreCase("CH2"))
			return get_CH2();
		else if(speciesName.equalsIgnoreCase("CH3"))
			return get_CH3();
		else if(speciesName.equalsIgnoreCase("CH4"))
			return get_CH4();
		else if(speciesName.equalsIgnoreCase("C2H2"))
			return get_C2H2();
		else if(speciesName.equalsIgnoreCase("C2H3"))
			return get_C2H3();
		else if(speciesName.equalsIgnoreCase("C2H4"))
			return get_C2H4();
		else if(speciesName.equalsIgnoreCase("C2H5"))
			return get_C2H5();
		else if(speciesName.equalsIgnoreCase("C3H4"))
			return get_C3H4();
		else if(speciesName.equalsIgnoreCase("C3H5"))
			return get_C3H5();
		else if(speciesName.equalsIgnoreCase("C3H6"))
			return get_C3H6();
		else if(speciesName.equalsIgnoreCase("C3H7"))
			return get_C3H7();
		else if(speciesName.equalsIgnoreCase("C7H15-2"))
			return get_C7H15_2();
		else if(speciesName.equalsIgnoreCase("C7H15O2"))
			return get_C7H15O2();
		else if(speciesName.equalsIgnoreCase("C7KET12"))
			return get_C7KET12();
		else if(speciesName.equalsIgnoreCase("C5H11CO"))
			return get_C5H11CO();
		else if(speciesName.equalsIgnoreCase("N2O"))
			return get_N2O();		
		else if(speciesName.equalsIgnoreCase("NO2"))
			return get_NO2();
		else if(speciesName.equalsIgnoreCase("iC8H18"))
			return get_iC8H18();
		else if(speciesName.equalsIgnoreCase("C8H17"))
			return get_C8H17();
		else if(speciesName.equalsIgnoreCase("C8H17OO"))
			return get_C8H17OO();
		else if(speciesName.equalsIgnoreCase("iC8KET21"))
			return get_iC8KET21();
		else if(speciesName.equalsIgnoreCase("C6H13CO"))
			return get_C6H13CO();
		else if(speciesName.equalsIgnoreCase("C4H9"))
			return get_C4H9();
		else if(speciesName.equalsIgnoreCase("C2H6"))
			return get_C2H6();
		else if(speciesName.equalsIgnoreCase("CH2CHO"))
			return get_CH2CHO();
		else if(speciesName.equalsIgnoreCase("CH2CO"))
			return get_CH2CO();
		else if(speciesName.equalsIgnoreCase("CH3O2"))
			return get_CH3O2();
		else if(speciesName.equalsIgnoreCase("CH3O2H"))
			return get_CH3O2H();
		else if(speciesName.equalsIgnoreCase("soot"))
			return get_soot();
		else if(speciesName.equalsIgnoreCase("NOx"))
			return get_NOx();
		else if(speciesName.equalsIgnoreCase("ohc"))
			return get_spezGrill();
		else {
			try{
				throw new BirdBrainedProgrammerException("Trying to create " + speciesName+ 
						". This species has not been programmed yet!");
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
			return null;
		}
		
	}	
	
	
	

	/**
	 * <p>Diese Methode erstellt eine Hashtable mit allen Kraftstoffen (KoeffSpezies mit Heizwert Hu>0). 
	 * Die KrstApproxSpezies ist nicht enthalten! 
	 * Soll ein neuer Kraftstoff fuer die Rechnung zur Verfuegung stehen,  MUSS er hier eingefuegt werden! </p>	
	 * Diese Art der Kraftstoffzuweisung ist nicht schnell aber sicher, da  
	 * ein neuer Krafstoff nur an EINER Stelle, naemlich HIER,  eingebunden werden muss.
	 * --> safty first
	 * @return Hashtable<String,Spezies> krstHash
	 */
	public Hashtable<String,Spezies> get_alleKrafstoffe(){		
		Hashtable<String,Spezies> krstHash=new Hashtable<String,Spezies>();
		for(int i=0;i<this.get_nbrOfSpecies();i++){
			if(this.get_Spez(i).get_Hu_mol()>0)
				krstHash.put(this.get_Spez(i).get_name(),this.get_Spez(i));
		}
		//hier noch alle Kraftstoffe die nicht im ERC-Mech enthalten sind!
		krstHash.put(get_spezRON_91().get_name(), get_spezRON_91());
		krstHash.put(get_spezRON_95().get_name(), get_spezRON_95());
		krstHash.put(get_spezRON_98().get_name(), get_spezRON_98());
		krstHash.put(get_spezDiesel().get_name(), get_spezDiesel());
		krstHash.put(get_spezC2H5OH().get_name(), get_spezC2H5OH());

		return krstHash;
	}

	/**
	 * Spezies H wird automatisch integriert 
	 * @return KoeffizientenSpezies spezH
	 */
	public Spezies get_spezH() {	

		if(spezH==null){		
			
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_H_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_H_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_H_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_H_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_H();
			}
			
			double hf=this.calc_deltaHf(koeffs);
			
			double lhv=calc_LHV(0, 1, 0, hf);
			
			spezH=new KoeffizientenSpezies(koeffs,hf,lhv,0,0,1,0,"H");	
			
//			this.integrierMich(spezH);
		}
		
		return spezH;
	}

	/**
	 * Spezies O wird automatisch integriert 
	 * @return KoeffizientenSpezies spezO
	 */
	public Spezies get_spezO() {	
		
		if(spezO==null){			

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_O_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_O_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_O_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_O_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_O();
			}	
			
			double hf=this.calc_deltaHf(koeffs);			
			spezO=new KoeffizientenSpezies(koeffs,								
					hf,0,1,0,0,0,"O"); 
//			this.integrierMich(spezO);
		}
		
		return spezO;
	}

	/**
	 * Spezies N wird automatisch integriert 
	 * @return KoeffizientenSpezies spezN
	 */
	public Spezies get_spezN() {

		if(spezN==null){		

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_N_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_N_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_N_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_N_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_N();
			}
			double hf=this.calc_deltaHf(koeffs);
			spezN=new KoeffizientenSpezies(	koeffs,								
					hf,0,0,0,0,1,"N");
			
//			this.integrierMich(spezN);
		}
		return spezN;
	}

	/**
	 * Spezies H2 wird automatisch integriert 
	 * @return KoeffizientenSpezies spezH2
	 */
	public Spezies get_spezH2() {
		
		if(spezH2==null){			

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_H2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_H2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_H2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_H2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_H2();				
			}
			
			double hf=this.calc_deltaHf(koeffs);
			
			double lhv=calc_LHV(0, 2, 0, hf);	
			
			spezH2=new KoeffizientenSpezies(koeffs,hf,lhv,0,0,2,0,"H2");
			
//			this.integrierMich(spezH2);
		}
		return spezH2;
	}

	/**
	 * Spezies OH wird automatisch integriert 
	 * @return KoeffizientenSpezies spezOH
	 */
	public Spezies get_spezOH() {
		
		if(spezOH==null){
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_OH_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_OH_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_OH_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_OH_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_OH();				
			}
			double hf=this.calc_deltaHf(koeffs);			
			spezOH=new KoeffizientenSpezies(koeffs, hf, 0, 1, 0, 1, 0, "OH");			
//			this.integrierMich(spezOH);
		}
		return spezOH;
	}

	/**
	 * Spezies CO wird automatisch integriert 
	 * @return KoeffizientenSpezies spezCO
	 */
	public Spezies get_spezCO() {		
		if(spezCO==null){
			
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_CO_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_CO_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_CO_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_CO_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else {
				koeffs=Koeffs_ERC.get_coeffs_CO();
			}
			
			double hf=this.calc_deltaHf(koeffs);
			double hu=this.calc_LHV(1, 0, 1, hf);
			spezCO=new KoeffizientenSpezies(koeffs,hf,hu,1,1,0,0,"CO"); 			
//			this.integrierMich(spezCO);
		}
		return spezCO;
	}

	/**
	 * Spezies NO wird automatisch integriert 
	 * @return KoeffizientenSpezies spezNO
	 */
	public Spezies get_spezNO() {

		if(spezNO==null){			
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_NO_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_NO_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_NO_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_NO_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}	else{
				koeffs=Koeffs_ERC.get_coeffs_NO();
			}
			double hf=this.calc_deltaHf(koeffs);			
			spezNO=new KoeffizientenSpezies(koeffs,hf,0,1,0,0,1,"NO");			
//			this.integrierMich(spezNO);
		}
		return spezNO;
	}

	/**
	 * Spezies CO2 wird automatisch integriert 	
	 * @return KoeffizientenSpezies spezO2
	 */
	public Spezies get_spezO2() {
		
		if(spezO2==null){

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_O2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_O2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_O2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_O2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_O2();
			}
			double hf=this.calc_deltaHf(koeffs);
			spezO2=new KoeffizientenSpezies(koeffs,hf,0,2,0,0,0,"O2");			
//			this.integrierMich(spezO2);
		}
		return spezO2;
	}

	/**
	 * Spezies H2O wird automatisch integriert 
	 * @return KoeffizientenSpezies spezH2O
	 */
	public Spezies get_spezH2O() {
		
		if(spezH2O==null){
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_H2O_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_H2O_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_H2O_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_H2O_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_H2O();
			}
			
			double hf=this.calc_deltaHf(koeffs);
			double lhv=0;
			spezH2O=new KoeffizientenSpezies(koeffs,hf,lhv,1,0,2,0,"H2O");			
//			this.integrierMich(spezH2O);
		}
		return spezH2O;
	}

	/**
	 * Spezies wird automatisch integriert 
	 * @return KoeffizientenSpezies spezCO2
	 */
	public Spezies get_spezCO2() {
		
		if(spezCO2==null){
			
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_CO2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_CO2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if (CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_CO2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_CO2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_CO2();
			}
			double hf=this.calc_deltaHf(koeffs);
			double lhv=0;			
			spezCO2=new KoeffizientenSpezies(koeffs,hf,lhv,2,1,0,0,"CO2");			
//			this.integrierMich(spezCO2);
		}
		return spezCO2;
	}

	/**
	 * Spezies N2 wird automatisch integriert 	
	 * @return KoeffizientenSpezies spezN2
	 */
	public Spezies get_spezN2() {
		
		if(spezN2==null){
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_N2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_N2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else if (CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("NASA9")){
				koeffs[0] = Koeffs_NASA.get_koeffs_N2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_N2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}else{
				koeffs=Koeffs_ERC.get_coeffs_N2();
			}
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(0, 0, 0, hf);
			spezN2=new KoeffizientenSpezies(koeffs,hf,lhv,0,0,0,2,"N2");			
//			this.integrierMich(spezN2);
		}
		return spezN2;
	}
	
	/**
		 * Ar wird NICHT automatisch integriert  
		 * @return KoeffizientenSpezies spezAr
		 */
		public Spezies get_spezAr() {			
			if(spezAr==null){	
				double koeffs [][]=new double [3][];	
				if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
					koeffs[0] = Koeffs_Burcat.get_koeffs_Ar_l();
					koeffs[1]= Koeffs_Burcat.get_koeffs_Ar_h();
					koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
				}else{
					koeffs[0] = Koeffs_NASA.get_koeffs_Ar_l();
					koeffs[1]= Koeffs_NASA.get_koeffs_Ar_h();
					koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
				}	
				double hf=this.calc_deltaHf(koeffs);
				double lhv=this.calc_LHV(0, 0, 0, hf);
//				spezAr=new KoeffizientenSpezies(koeffs,hf,lhv,0,0,0,0,"Ar");
			}
			return spezAr;
		}


	/**
		 * Gibt trockene Luft als Gasgemisch von CO2, N2 und O2 zurueck. 
		 * Luft wird NICHT automatisch integriert
		 * @return GasGemisch spezAir
		 */
		public Spezies get_spezLuft_trocken() {
			if(spezLuftTr==null){
				Hashtable<Spezies, Double> koeffSpeziesMolenBruchHash= new Hashtable<Spezies,Double>();
	
				double vol_O2=PhysKonst.get_vol_O2_Luft();
				koeffSpeziesMolenBruchHash.put((KoeffizientenSpezies) get_spezO2(),vol_O2 );
				double vol_CO2=PhysKonst.get_vol_CO2_Luft();
				koeffSpeziesMolenBruchHash.put((KoeffizientenSpezies) get_spezCO2(),vol_CO2 );
				double vol_N2=1-vol_O2-vol_CO2;
	//			Ar wird nicht beruecksichtigt, da der Gleichgewichtssolver es nicht beruecksichtigt und die Beruecksichtigung
	//			von Ar dann zu unstimmigen Massenbruechen fuehren wuerde
				koeffSpeziesMolenBruchHash.put((KoeffizientenSpezies) get_spezN2(), vol_N2);	
				spezLuftTr=new GasGemisch("Luft");
				((GasGemisch)spezLuftTr).set_Gasmischung_molenBruch(koeffSpeziesMolenBruchHash);
			}
			return spezLuftTr;
		}


	/**
	 * Erzeugt Ethanol
	 * spezC2H5OH wird NICHT automatisch integriert!
	 * @return KoeffizientenSpezies spezC2H5OH
	 */
	public Spezies get_spezC2H5OH() {		
		if(spezC2H5OH==null){			
			double koeffs [][]=new double [3][];			
				koeffs[0] = Koeffs_NASA.get_koeffs_C2H5OH_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_C2H5OH_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2, 6, 1, hf);
	
			spezC2H5OH=new KoeffizientenSpezies(koeffs,hf,lhv,1,2,6,0,"Ethanol");
		}
		return spezC2H5OH;
	}
	
	public Spezies get_nC7H16() {		
		if(nC7H16==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_nC7H16();
				
			double hf=this.calc_deltaHf(koeffs); //orig:-188498.08792308983 
			double lhv=this.calc_LHV(7,16,0,hf); //orig: 4500842.684176453
			//adaption of delta_hf to get the proper lhv
			//lhv_ex_mass=lhv_sim_mass [J/kg] --> lhv_ex_mol/M_ex*M_sim=lhv_sim_mol [J/mol]

			//usi this code to adopt delta_hf accordding to the experimental LHV
//			double M_sim=0.10020404000000001;
//			lhv=42.5*1e6*M_sim; //4258671.7
//			hf=this.calc_hf4LHV(7, 16, 0, lhv);
			
			double hf_Check=this.calc_hf4LHV(7, 16, 0, lhv);//orig: -188498.0879230895
			hf_Check=(hf_Check-hf)/hf*100;
			nC7H16=new KoeffizientenSpezies(koeffs,hf,lhv,0,7,16,0,"nC7H16");
//			this.integrierMich(nC7H16);
			
			//adapting factor b1 in the NASA-Polinomials to generate the appropriate LHV			
			double lhvMJkg=nC7H16.get_Hu_mass()*1e-6; //orig: 44.91677864661398
			double M=nC7H16.get_M();
			double lhvNew=42.5*1e6*M; //4258671.7	
			double hf_Adapted=this.calc_hf4LHV(7, 16, 0, lhvNew); //-430669.0720995427		
			double lhv_adaptedCheck=this.calc_LHV(7, 16, 0, hf_Adapted); //4258671.7
			lhv_adaptedCheck=(lhv_adaptedCheck-lhvNew)/lhvNew*100;
			double b1_low=this.calc_b1_low(koeffs,lhvNew,7, 16, 0);
			//-54786.75178222912			
			double b1Check=this.calc_b1_low(koeffs,lhv,7, 16, 0);
			double b1L=-2.565865650E+004;
			b1Check=(b1Check-b1L)/b1L*100;
			b1Check=b1Check;
			
			double b1_high=this.calc_b1_high(koeffs,lhvNew,7, 16, 0);
			//-63404.10344444763	
			double b1CheckH=this.calc_b1_high(koeffs,lhv,7, 16, 0);
			double b1H=-3.427600810E+004;
			b1CheckH=(b1CheckH- (b1H))/b1H*100;
			b1CheckH=b1CheckH;

		}	
		return nC7H16;
	}
	
	public Spezies get_nC14H30() {	
		try{
			throw new MiscException("C14H30 has not been checked yet! Check it and delete this Mesage or do NOT use it!");
		}catch(MiscException me){
			me.stopBremo();
//			me.log_Warning();
		}	
		if(nC14H30==null){			
			double koeffs [][]=new double [3][];	
			koeffs[0] = Koeffs_Burcat.get_koeffs_nC14H30_l();
			koeffs[1]= Koeffs_Burcat.get_koeffs_nC14H30_h();
			koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,5,0,hf);	
			nC14H30=new KoeffizientenSpezies(koeffs,hf,lhv,0,14,30,0,"nC14H30");
			//this.integrierMich(nC14H30);
		}
		return nC14H30;
	}
	
	
	
	public Spezies get_H2O2() {		
		if(H2O2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_H2O2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(0,2,2,hf);	
			H2O2=new KoeffizientenSpezies(koeffs,hf,lhv,2,0,2,0,"H2O2");
//			this.integrierMich(H2O2);
		}
		return H2O2;
	}
	
	public Spezies get_HO2() {		
		if(HO2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_HO2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(0,1,2,hf);	
			HO2=new KoeffizientenSpezies(koeffs,hf,lhv,2,0,1,0,"HO2");
//			this.integrierMich(HO2);
		}
		return HO2;
	}
	
	public Spezies get_CH3O() {		
		if(CH3O==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH3O();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,3,1,hf);	
			CH3O=new KoeffizientenSpezies(koeffs,hf,lhv,1,1,3,0,"CH3O");
//			this.integrierMich(CH3O);
		}
		return CH3O;
	}
	
	public Spezies get_CH2O() {		
		if(CH2O==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH2O();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,2,1,hf);	
			CH2O=new KoeffizientenSpezies(koeffs,hf,lhv,1,1,2,0,"CH2O");
//			this.integrierMich(CH2O);
		}
		return CH2O;
	}	
	
	public Spezies get_HCO() {		
		if(HCO==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_HCO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,1,1,hf);	
			HCO=new KoeffizientenSpezies(koeffs,hf,lhv,1,1,1,0,"HCO");
//			this.integrierMich(HCO);
		}
		return HCO;
	}
	
	public Spezies get_CH2() {		
		if(CH2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,2,0,hf);	
			CH2=new KoeffizientenSpezies(koeffs,hf,lhv,0,1,2,0,"CH2");
//			this.integrierMich(CH2);
		}
		return CH2;
	}
	
	public Spezies get_CH3() {		
		if(CH3==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH3();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,3,0,hf);	
			CH3=new KoeffizientenSpezies(koeffs,hf,lhv,0,1,3,0,"CH3");
//			this.integrierMich(CH3);
		}
		return CH3;
	}
	
	public Spezies get_CH4() {		
		if(CH4==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH4();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,4,0,hf);	
			CH4=new KoeffizientenSpezies(koeffs,hf,lhv,0,1,4,0,"CH4");
//			this.integrierMich(CH4);
		}
		return CH4;
	}
	
	public Spezies get_C2H2() {		
		if(C2H2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C2H2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,2,0,hf);	
			C2H2=new KoeffizientenSpezies(koeffs,hf,lhv,0,2,2,0,"C2H2");
//			this.integrierMich(C2H2);
		}
		return C2H2;
	}
	
	public Spezies get_C2H3() {		
		if(C2H3==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C2H3();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,3,0,hf);	
			C2H3=new KoeffizientenSpezies(koeffs,hf,lhv,0,2,3,0,"C2H3");
//			this.integrierMich(C2H3);
		}
		return C2H3;
	}
	
	public Spezies get_C2H4() {		
		if(C2H4==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C2H4();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,4,0,hf);	
			C2H4=new KoeffizientenSpezies(koeffs,hf,lhv,0,2,4,0,"C2H4");
//			this.integrierMich(C2H4);
		}
		return C2H4;
	}
	
	public Spezies get_C2H5() {		
		if(C2H5==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C2H5();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,5,0,hf);	
			C2H5=new KoeffizientenSpezies(koeffs,hf,lhv,0,2,5,0,"C2H5");
//			this.integrierMich(C2H5);
		}
		return C2H5;
	}
	
	public Spezies get_C3H4() {		
		if(C3H4==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C3H4();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(3,4,0,hf);	
			C3H4=new KoeffizientenSpezies(koeffs,hf,lhv,0,3,4,0,"C3H4");
//			this.integrierMich(C3H4);
		}
		return C3H4;
	}
	
	public Spezies get_C3H5() {		
		if(C3H5==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C3H5();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(3,5,0,hf);	
			C3H5=new KoeffizientenSpezies(koeffs,hf,lhv,0,3,5,0,"C3H5");
//			this.integrierMich(C3H5);
		}
		return C3H5;
	}
	
	public Spezies get_C3H6() {		
		if(C3H6==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C3H6();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(3,6,0,hf);	
			C3H6=new KoeffizientenSpezies(koeffs,hf,lhv,0,3,6,0,"C3H6");
//			this.integrierMich(C3H6);
		}
		return C3H6;
	}
	
	public Spezies get_C3H7() {		
		if(C3H7==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C3H7();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(3,7,0,hf);	
			C3H7=new KoeffizientenSpezies(koeffs,hf,lhv,0,3,7,0,"C3H7");
//			this.integrierMich(C3H7);
		}
		return C3H7;
	}
	
	public Spezies get_C7H15_2() {		
		if(C7H15_2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C7H15_2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(7,15,0,hf);	
			C7H15_2=new KoeffizientenSpezies(koeffs,hf,lhv,0,7,15,0,"C7H15-2");
//			this.integrierMich(C7H15_2);
		}
		return C7H15_2;
	}
	
	public Spezies get_C7H15O2() {		
		if(C7H15O2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C7H15O2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(7,15,2,hf);	
			C7H15O2=new KoeffizientenSpezies(koeffs,hf,lhv,2,7,15,0,"C7H15O2");
//			this.integrierMich(C7H15O2);
		}
		return C7H15O2;
	}
	
	public Spezies get_C7KET12() {		
		if(C7KET12==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C7KET12();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(7,14,3,hf);	
			C7KET12=new KoeffizientenSpezies(koeffs,hf,lhv,3,7,14,0,"C7KET12");
//			this.integrierMich(C7KET12);
		}
		return C7KET12;
	}
	
	public Spezies get_C5H11CO() {		
		if(C5H11CO==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C5H11CO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(6,11,1,hf);	
			C5H11CO=new KoeffizientenSpezies(koeffs,hf,lhv,1,6,11,0,"C5H11CO");
//			this.integrierMich(C5H11CO);
		}
		return C5H11CO;
	}
	
	public Spezies get_N2O() {		
		if(N2O==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_N2O();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(0,0,1,hf);	
			N2O=new KoeffizientenSpezies(koeffs,hf,lhv,1,0,0,2,"N2O");
//			this.integrierMich(N2O);
		}
		return N2O;
	}
	
	public Spezies get_NO2() {		
		if(NO2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_NO2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(0,0,2,hf);	
			NO2=new KoeffizientenSpezies(koeffs,hf,lhv,2,0,0,1,"NO2");
//			this.integrierMich(NO2);
		}
		return NO2;
	}
	
	public Spezies get_iC8H18() {		
		if(iC8H18==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_iC8H18();	
			
			double hf=this.calc_deltaHf(koeffs);			
			double lhv=this.calc_LHV(8,18,0,hf);
			double hf_Check=(this.calc_hf4LHV(8,18,0, lhv)-hf)/hf*100;				
			
			//adaption of delta_hf to get the proper lhv
			//lhv_ex_mass=lhv_sim_mass [J/kg] --> lhv_ex_mol/M_ex*M_sim=lhv_sim_mol [J/mol]
//			double M_sim=0.11423092000000001;
//			lhv=43.2*1e6*M_sim; 
//			hf=this.calc_hf4LHV(8,18,0, lhv);
			iC8H18=new KoeffizientenSpezies(koeffs,hf,lhv,0,8,18,0,"iC8H18");			
//			this.integrierMich(iC8H18);
			
			//adapting factor b1 in the NASA-Polinomials to generate the appropriate LHV
			double lhvMJkg=iC8H18.get_Hu_mass()*1e-6;
			double M=iC8H18.get_M();
			double lhvNew=43.217*1e6*M;			
				
			double b1_low=this.calc_b1_low(koeffs,lhvNew,8, 18, 0);
			//-49720.94130043444			
			double b1Check=this.calc_b1_low(koeffs,lhv,8, 18, 0);
			double b1=-2.994468750E+004;
			b1Check=(b1Check- b1)/b1*100;				
			
			double b1_high=this.calc_b1_high(koeffs,lhvNew,8, 18, 0);				
			//-60572.071545029
			double b1CheckH=this.calc_b1_high(koeffs,lhv,8, 18, 0);
			double b1H=-4.079581770E+004;
			b1CheckH=(b1CheckH- b1H)/b1H*100;
			b1Check=b1Check;
		}
	
		return iC8H18;
	}
	
	
	public Spezies get_C8H17() {		
		if(C8H17==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C8H17();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(8,17,0,hf);	
			C8H17=new KoeffizientenSpezies(koeffs,hf,lhv,0,8,17,0,"C8H17");
//			this.integrierMich(C8H17);
		}
		return C8H17;
	}
	
	public Spezies get_C8H17OO() {		
		if(C8H17OO==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C8H17OO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(8,17,2,hf);	
			C8H17OO=new KoeffizientenSpezies(koeffs,hf,lhv,2,8,17,0,"C8H17OO");
//			this.integrierMich(C8H17OO);
		}
		return C8H17OO;
	}
	
	public Spezies get_iC8KET21() {		
		if(iC8KET21==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_iC8KET21();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(8,16,3,hf);	
			iC8KET21=new KoeffizientenSpezies(koeffs,hf,lhv,3,8,16,0,"iC8KET21");
//			this.integrierMich(iC8KET21);
		}
		return iC8KET21;
	}
	
	public Spezies get_C6H13CO() {		
		if(C6H13CO==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C6H13CO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(7,13,1,hf);	
			C6H13CO=new KoeffizientenSpezies(koeffs,hf,lhv,1,7,13,0,"C6H13CO");
//			this.integrierMich(C6H13CO);
		}
		return C6H13CO;
	}	
	
	public Spezies get_C4H9() {		
		if(C4H9==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C4H9();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(4,9,0,hf);	
			C4H9=new KoeffizientenSpezies(koeffs,hf,lhv,0,4,9,0,"C4H9");
//			this.integrierMich(C4H9);
		}
		return C4H9;
	}
	
	
	public Spezies get_C2H6() {		
		if(C2H6==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_C2H6();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,6,0,hf);	
			C2H6=new KoeffizientenSpezies(koeffs,hf,lhv,0,2,6,0,"C2H6");
//			this.integrierMich(C2H6);
		}
		return C2H6;
	}
	
	
	
	public Spezies get_CH2CHO() {		
		if(CH2CHO==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH2CHO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,3,1,hf);	
			CH2CHO=new KoeffizientenSpezies(koeffs,hf,lhv,1,2,3,0,"CH2CHO");
//			this.integrierMich(CH2CHO);
		}
		return CH2CHO;
	}
	
	public Spezies get_CH2CO() {		
		if(CH2CO==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH2CO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(2,2,1,hf);	
			CH2CO=new KoeffizientenSpezies(koeffs,hf,lhv,1,2,2,0,"CH2CO");
//			this.integrierMich(CH2CO);
		}
		return CH2CO;
	}
	
	public Spezies get_CH3O2() {		
		if(CH3O2==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH3O2();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,3,2,hf);	
			CH3O2=new KoeffizientenSpezies(koeffs,hf,lhv,2,1,3,0,"CH3O2");
//			this.integrierMich(CH3O2);
		}
		return CH3O2;
	}
	
	
	public Spezies get_CH3O2H() {		
		if(CH3O2H==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_CH3O2H();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(1,4,2,hf);	
			CH3O2H=new KoeffizientenSpezies(koeffs,hf,lhv,2,1,4,0,"CH3O2H");
//			this.integrierMich(CH3O2H);
		}
		return CH3O2H;
	}
	
	
	public Spezies get_soot() {		
		if(soot==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_soot();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(80,0,0,hf);	
			soot=new KoeffizientenSpezies(koeffs,hf,lhv,0,80,0,0,"soot");
//			this.integrierMich(soot);
		}
		return soot;
	}	
	
	public Spezies get_NOx() {		
		if(NOx==null){			
			double koeffs [][]=new double [3][];			
			koeffs=Koeffs_ERC.get_coeffs_NO();				
			double hf=this.calc_deltaHf(koeffs);
			double lhv=this.calc_LHV(0,0,0,hf);	
			NOx=new KoeffizientenSpezies(koeffs,hf,lhv,1,0,0,1,"NOx");
//			this.integrierMich(soot);
		}
		return NOx;
	}	
	
	

	
	

	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Normalbenzin nach FVV-Zylindermodul zurueck.
	 *  RON_91 wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezRON_91
	 */
	public Spezies get_spezRON_91() {	
		//TODO ohc Anteile als Eingabe im Inputfile definieren
		if(spezRON_91==null){			
			double lhv=CP.get_Hu_RON_91();				
			double hf; 
			hf=this.calc_hf4LHV(7.317412935,14.19104478,0,lhv );			
			double koeffs [][]=new double [3][];
			koeffs[0]=Koeffs_KrstFVV.get_koeffs_RON_91_l();
			koeffs[1]=Koeffs_KrstFVV.get_koeffs_RON_91_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();
			
			spezRON_91=new KoeffizientenSpezies(koeffs,
					hf,lhv,0,7.317412935,14.19104478,0,"RON_91"); 
		}
		return spezRON_91;
	}


	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Superbenzin nach FVV-Zylindermodul zurueck.
	 *  RON_95 wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezRON_95
	 */
	public Spezies get_spezRON_95() {
		//TODO ohc Anteile als Eingabe im Inputfile definieren
		if(spezRON_95==null){
			double lhv=CP.get_Hu_RON_95();
			double hf;			
			hf=this.calc_hf4LHV(6.96292482,12.46549949,0,lhv);			
			double koeffs [][]=new double [3][];
			koeffs[0]=Koeffs_KrstFVV.get_koeffs_RON_95_l();
			koeffs[1]=Koeffs_KrstFVV.get_koeffs_RON_95_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();

			spezRON_95=new KoeffizientenSpezies(koeffs,								
					hf,lhv,0,6.96292482,12.46549949,0,"RON_95");	
		}
		return spezRON_95;
	}

	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Superplusbenzin nach FVV-Zylindermodul zurueck.
	 *  RON_98 wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezRON_98
	 */
	public Spezies get_spezRON_98() {
		//TODO ohc Anteile als Eingabe im Inputfile definieren
		if(spezRON_98==null){
			double lhv=CP.get_Hu_RON_98();
			double hf;			
			hf=this.calc_hf4LHV(6.895209581,11.9001996,0,lhv);				
			double koeffs [][]=new double [3][];
			koeffs[0] = Koeffs_KrstFVV.get_koeffs_RON_98_l();
			koeffs[1] = Koeffs_KrstFVV.get_koeffs_RON_98_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();

			spezRON_98=new KoeffizientenSpezies(koeffs,	
					hf,lhv,0,6.895209581,11.9001996,0,"RON_98");
		}
		return spezRON_98;
	}

	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Diesel nach FVV-Zylindermodul zurueck.
	 *  Diesel wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezDiesel
	 */
	public Spezies get_spezDiesel() {
		//TODO Molare Masse als Eingabeparameter inm Inputfile deklarieren
		if(spezDiesel==null){
			double lhv=CP.get_Hu_Diesel();			
			double hf;
			hf=this.calc_hf4LHV(12.60179278,23.52813985,0,lhv );
			double koeffs [][]=new double [3][];
			koeffs[0] = Koeffs_KrstFVV.get_koeffs_Diesel_l();
			koeffs[1] = Koeffs_KrstFVV.get_koeffs_Diesel_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();
			spezDiesel=new KoeffizientenSpezies(koeffs,hf,
					lhv,0,12.60179278,23.52813985,0,"Diesel");
			
		}
		return spezDiesel;
	}
	
	public Spezies get_spezGrill() {
		if(spezOHCGrill==null){
			double lhv = CP.get_Hu_ohc();
			double[] anzAtome = CP.get_krstApprox_ohc();
			double anzO_Atome = anzAtome[0];
			double anzH_Atome = anzAtome[1];
			double anzC_Atome = anzAtome[2];
			double anzN_Atome = anzAtome[3];
			
			spezOHCGrill = new KrstApproxSpezies(lhv, anzO_Atome, anzC_Atome, anzH_Atome, anzN_Atome, "ohc");
		}
	return spezOHCGrill;
	}
	
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////	

	private static class Koeffs_NASA {

		// Koeffizienten nach den Janaf-Tables --> http://cea.grc.nasa.gov/

		static double[] get_koeffs_H_l() {
			double[] H_l = { 0.000000000E+00, 0.000000000E+00, 2.500000000E+00,
					0.000000000E+00, 0.000000000E+00, 0.000000000E+00,
					0.000000000E+00, 2.547370801E+04, -4.466828530E-01 };
			return H_l;
		}

		static double[] get_koeffs_H_h() {
			double[] H_h = { 6.078774250E+01, -1.819354417E-01, 2.500211817E+00,
					-1.226512864E-07, 3.732876330E-11, -5.687744560E-15,
					3.410210197E-19, 2.547486398E+04, -4.481917770E-01 };
			return H_h;
		}

		// ///////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_O_l() {
			double[] O_l = { -7.953611300E+03, 1.607177787E+02, 1.966226438E+00,
					1.013670310E-03, -1.110415423E-06, 6.517507500E-10,
					-1.584779251E-13, 2.840362437E+04, 8.404241820E+00 };
			return O_l;
		}

		static double[] get_koeffs_O_h() {
			double[] O_h = { 2.619020262E+05, -7.298722030E+02, 3.317177270E+00,
					-4.281334360E-04, 1.036104594E-07, -9.438304330E-12,
					2.725038297E-16, 3.392428060E+04, -6.679585350E-01 };
			return O_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_N_l() {
			double[] N_l = { 0.000000000E+00, 0.000000000E+00, 2.500000000E+00,
					0.000000000E+00, 0.000000000E+00, 0.000000000E+00,
					0.000000000E+00, 5.610463780E+04, 4.193905036E+00 };
			return N_l;
		}

		static double[] get_koeffs_N_h() {
			double[] N_h = { 8.876501380E+04, -1.071231500E+02, 2.362188287E+00,
					2.916720081E-04, -1.729515100E-07, 4.012657880E-11,
					-2.677227571E-15, 5.697351330E+04, 4.865231506E+00 };
			return N_h;
		}

		// ////////////////////////////////////////////////////////////////////////		


		static double[] get_koeffs_H2_l() {
			double[] H2_l = { 4.078323210E+04, -8.009186040E+02, 8.214702010E+00,
					-1.269714457E-02, 1.753605076E-05, -1.202860270E-08,
					3.368093490E-12, 2.682484665E+03, -3.043788844E+01 };
			return H2_l;
		}

		static double[] get_koeffs_H2_h() {
			double[] H2_h = { 5.608128010E+05, -8.371504740E+02, 2.975364532E+00,
					1.252249124E-03, -3.740716190E-07, 5.936625200E-11,
					-3.606994100E-15, 5.339824410E+03, -2.202774769E+00 };
			return H2_h;
		}

		// //////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_OH_l() {
			double[] OH_l = { -1.998858990E+03, 9.300136160E+01, 3.050854229E+00,
					1.529529288E-03, -3.157890998E-06, 3.315446180E-09,
					-1.138762683E-12, 2.991214235E+03, 4.674110790E+00 };
			return OH_l;
		}

		static double[] get_koeffs_OH_h() {
			double[] OH_h = { 1.017393379E+06, -2.509957276E+03, 5.116547860E+00,
					1.305299930E-04, -8.284322260E-08, 2.006475941E-11,
					-1.556993656E-15, 2.019640206E+04, -1.101282337E+01 };
			return OH_h;
		}

		// ///////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_CO_l() {
			double[] CO_l = { 1.489045326E+04, -2.922285939E+02, 5.724527170E+00,
					-8.176235030E-03, 1.456903469E-05, -1.087746302E-08,
					3.027941827E-12, -1.303131878E+04, -7.859241350E+00 };
			return CO_l;
		}

		static double[] get_koeffs_CO_h() {
			double[] CO_h = { 4.619197250E+05, -1.944704863E+03, 5.916714180E+00,
					-5.664282830E-04, 1.398814540E-07, -1.787680361E-11,
					9.620935570E-16, -2.466261084E+03, -1.387413108E+01 };
			return CO_h;
		}

		// ///////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_NO_l() {
			double[] NO_l = { -1.143916503E+04, 1.536467592E+02, 3.431468730E+00,
					-2.668592368E-03, 8.481399120E-06, -7.685111050E-09,
					2.386797655E-12, 9.098214410E+03, 6.728725490E+00 };
			return NO_l;
		}

		static double[] get_koeffs_NO_h() {
			double[] NO_h = { 2.239018716E+05, -1.289651623E+03, 5.433936030E+00,
					-3.656034900E-04, 9.880966450E-08, -1.416076856E-11,
					9.380184620E-16, 1.750317656E+04, -8.501669090E+00 };
			return NO_h;
		}

		// //////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_O2_l() {
			double[] O2_l = { -3.425563420E+04, 4.847000970E+02, 1.119010961E+00,
					4.293889240E-03, -6.836300520E-07, -2.023372700E-09,
					1.039040018E-12, -3.391454870E+03, 1.849699470E+01 };
			return O2_l;
		}

		static double[] get_koeffs_O2_h() {
			double[] O2_h = { -1.037939022E+06, 2.344830282E+03, 1.819732036E+00,
					1.267847582E-03, -2.188067988E-07, 2.053719572E-11,
					-8.193467050E-16, -1.689010929E+04, 1.738716506E+01 };
			return O2_h;
		}

		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_H2O_l() {
			double[] H2O_l = { -3.947960830E+04, 5.755731020E+02, 9.317826530E-01,
					7.222712860E-03, -7.342557370E-06, 4.955043490E-09,
					-1.336933246E-12, -3.303974310E+04, 1.724205775E+01 };
			return H2O_l;
		}

		static double[] get_koeffs_H2O_h() {
			double[] H2O_h = { 1.034972096E+06, -2.412698562E+03, 4.646110780E+00,
					2.291998307E-03, -6.836830480E-07, 9.426468930E-11,
					-4.822380530E-15, -1.384286509E+04, -7.978148510E+00 };
			return H2O_h;
		}

		////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_CO2_l() {
			double[] CO2_l = { 4.943650540E+04, -6.264116010E+02, 5.301725240E+00,
					2.503813816E-03, -2.127308728E-07, -7.689988780E-10,
					2.849677801E-13, -4.528198460E+04, -7.048279440E+00 };
			return CO2_l;
		}

		

		static double[] get_koeffs_CO2_h() {
			double[] CO2_h = { 1.176962419E+05, -1.788791477E+03, 8.291523190E+00,
					-9.223156780E-05, 4.863676880E-09, -1.891053312E-12,
					6.330036590E-16, -3.908350590E+04, -2.652669281E+01 };
			return CO2_h;
		}

		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_N2_l() {
			double[] N2_l = { 2.210371497E+04, -3.818461820E+02, 6.082738360E+00,
					-8.530914410E-03, 1.384646189E-05, -9.625793620E-09,
					2.519705809E-12, 7.108460860E+02, -1.076003744E+01 };
			return N2_l;
		}

		static double[] get_koeffs_N2_h() {
			double[] N2_h = { 5.877124060E+05, -2.239249073E+03, 6.066949220E+00,
					-6.139685500E-04, 1.491806679E-07, -1.923105485E-11,
					1.061954386E-15, 1.283210415E+04, -1.586640027E+01 };
			return N2_h;
		}

		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_Ar_l() {
			double[] Ar_l = { 0.000000000E+00, 0.000000000E+00, 2.500000000E+00,
					0.000000000E+00, 0.000000000E+00, 0.000000000E+00,
					0.000000000E+00, -7.453750000E+02, 4.379674910E+00 };
			return Ar_l;
		}

		static double[] get_koeffs_Ar_h() {
			double[] Ar_h = { 2.010538475E+01, -5.992661070E-02, 2.500069401E+00,
					-3.992141160E-08, 1.205272140E-11, -1.819015576E-15,
					1.078576636E-19, -7.449939610E+02, 4.379180110E+00 };
			return Ar_h;
		}
		
		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_C2H5OH_l() {
			double[] C2H5OH_l = { -2.342791392E+05, 4.479180550E+03, -2.744817302E+01,
						1.088679162E-01,-1.305309334E-04, 8.437346400E-08,
						-2.234559017E-11, -5.022229000E+04, 1.764829211E+02 };
			return C2H5OH_l;
		}

		static double[] get_koeffs_C2H5OH_h() {
			double[] C2H5OH_h = {4.694817650E+06, -1.929798213E+04, 3.447584040E+01,
					-3.236165980E-03, 5.784947720E-07, -5.564600270E-11,
					2.226226400E-15, 8.601622710E+04, -2.034801732E+02};
		return C2H5OH_h;
		} 

		static double [] get_TemperaturGrenze(){
			double [] T={300,1000,5000};
			return T;	
		}		

	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	private static class Koeffs_Burcat {	

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_H_l() {
			double[] H_l = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,2.54736600E+004,-4.46682850E-001};
			return H_l;
		}

		static double[] get_koeffs_H_h() {
			double[] H_h = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,2.54736600E+004,-4.46682850E-001};
			return H_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_O_l() {
			double[] O_l = {0.00000000E+000,0.00000000E+000,3.16826710E+000,
					-3.27931884E-003,6.64306396E-006,-6.12806624E-009,
					2.11265971E-012,2.91222592E+004,2.05193346E+000};
			return O_l;
		}

		static double[] get_koeffs_O_h() {
			double[] O_h = {0.00000000E+000,0.00000000E+000,2.54363697E+000,
					-2.73162486E-005,-4.19029520E-009,4.95481845E-012,
					-4.79553694E-016,2.92260120E+004,4.92229457E+000};
			return O_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_N_l() {
			double[] N_l = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,5.61046380E+004,4.19390880E+000};
			return N_l;
		}

		static double[] get_koeffs_N_h() {
			double[] N_h = {0.00000000E+000,0.00000000E+000,2.41594290E+000,
					1.74890650E-004,-1.19023690E-007,3.02262440E-011,
					-2.03609830E-015,5.61337750E+004,4.64960950E+000};
			return N_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_H2_l() {
			double[] H2_l = {0.00000000E+000,0.00000000E+000,2.34433112E+000,
					7.98052075E-003,-1.94781510E-005,2.01572094E-008,
					-7.37611761E-012,-9.17935173E+002,6.83010238E-001};
			return H2_l;
		}

		static double[] get_koeffs_H2_h() {
			double[] H2_h = {0.00000000E+000,0.00000000E+000,2.93286575E+000,
					8.26608026E-004,-1.46402364E-007,1.54100414E-011,
					-6.88804800E-016,-8.13065581E+002,-1.02432865E+000};
			return H2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_OH_l() {
			double[] OH_l = {0.00000000E+000,0.00000000E+000,3.99198424E+000,
					-2.40106655E-003,4.61664033E-006,-3.87916306E-009,
					1.36319502E-012,3.36889836E+003,-1.03998477E-001};
			return OH_l;
		}

		static double[] get_koeffs_OH_h() {
			double[] OH_h = {0.00000000E+000,0.00000000E+000,2.83853033E+000,
					1.10741289E-003,-2.94000209E-007,4.20698729E-011,
					-2.42289890E-015,3.69780808E+003,5.84494652E+000};
			return OH_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_CO_l() {
			double[] CO_l = {0.00000000E+000,0.00000000E+000,3.57953350E+000,
					-6.10353690E-004,1.01681430E-006,9.07005860E-010,
					-9.04424490E-013,-1.43440860E+004,3.50840930E+000};
			return CO_l;
		}

		static double[] get_koeffs_CO_h() {
			double[] CO_h = {0.00000000E+000,0.00000000E+000,3.04848590E+000,
					1.35172810E-003,-4.85794050E-007,7.88536440E-011,
					-4.69807460E-015,-1.42661170E+004,6.01709770E+000};
			return CO_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_NO_l() {
			double[] NO_l = {0.00000000E+000,0.00000000E+000,4.21859896E+000,
					-4.63988124E-003,1.10443049E-005,-9.34055507E-009,
					2.80554874E-012,9.84509964E+003,2.28061001E+000};
			return NO_l;
		}

		static double[] get_koeffs_NO_h() {
			double[] NO_h = {0.00000000E+000,0.00000000E+000,3.26071234E+000,
					1.19101135E-003,-4.29122646E-007,6.94481463E-011,
					-4.03295681E-015,9.92143132E+003,6.36900518E+000};
			return NO_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_O2_l() {
			double[] O2_l = {0.00000000E+000,0.00000000E+000,3.78245636E+000,
					-2.99673416E-003,9.84730201E-006,-9.68129509E-009,
					3.24372837E-012,-1.06394356E+003,3.65767573E+000};
			return O2_l;
		}

		static double[] get_koeffs_O2_h() {
			double[] O2_h = {0.00000000E+000,0.00000000E+000,3.66096065E+000,
					6.56365811E-004,-1.41149627E-007,2.05797935E-011,
					-1.29913436E-015,-1.21597718E+003,3.41536279E+000};
			return O2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_H2O_l() {
			double[] H2O_l = {0.00000000E+000,0.00000000E+000,4.19863520E+000,
					-2.03640170E-003,6.52034160E-006,-5.48792690E-009,
					1.77196800E-012,-3.02937260E+004,-8.49009010E-001};
			return H2O_l;
		}

		static double[] get_koeffs_H2O_h() {
			double[] H2O_h = {0.00000000E+000,0.00000000E+000,2.67703890E+000,
					2.97318160E-003,-7.73768890E-007,9.44335140E-011,
					-4.26899910E-015,-2.98858940E+004,6.88255000E+000};
			return H2O_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_CO2_l() {
			double[] CO2_l = {0.00000000E+000,0.00000000E+000,2.35681300E+000,
					8.98412990E-003,-7.12206320E-006,2.45730080E-009,
					-1.42885480E-013,-4.83719710E+004,9.90090350E+000};
			return CO2_l;
		}

		static double[] get_koeffs_CO2_h() {
			double[] CO2_h = {0.00000000E+000,0.00000000E+000,4.63651110E+000,
					2.74145690E-003,-9.95897590E-007,1.60386660E-010,
					-9.16198570E-015,-4.90249040E+004,-1.93489550E+000};
			return CO2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_N2_l() {
			double[] N2_l = {0.00000000E+000,0.00000000E+000,3.53100528E+000,
					-1.23660988E-004,-5.02999433E-007,2.43530612E-009,
					-1.40881235E-012,-1.04697628E+003,2.96747038E+000};
			return N2_l;
		}

		static double[] get_koeffs_N2_h() {
			double[] N2_h = {0.00000000E+000,0.00000000E+000,2.95257637E+000,
					1.39690040E-003,-4.92631603E-007,7.86010195E-011,
					-4.60755204E-015,-9.23948688E+002,5.87188762E+000};
			return N2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_Ar_l() {
			double[] Ar_l = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,-7.45375000E+002,4.37967491E+000};
			return Ar_l;
		}

		static double[] get_koeffs_Ar_h() {
			double[] Ar_h = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,-7.45375000E+002,4.37967491E+000};
			return Ar_h;
		}
		
		//Taken from new database 
		static double[] get_koeffs_nC14H30_l() {

			
			double[] C14H30_l = {-1.691930679E+06, 3.079060429E+04,-1.928895104E+02, 
					7.073264000E-01,
					-8.651750660E-04, 5.564301820E-07, -1.466757718E-10 ,-1.883644650E+05, 1.100191071E+03};
			return C14H30_l;
		}

		static double[] get_koeffs_nC14H30_h() {
			double[] C14H30_h = {1.504040242E+06,-3.585004240E+04, 1.209602445E+02, 1.701511451E-03,
					-2.195485192E-07, 1.964713332E-11, -9.452395710E-16, 1.355806368E+05,-7.109002900E+02};
			return C14H30_h;
		}
		
		
		
		
		
		

		// ////////////////////////////////////////////////////////////////////////

		static double [] get_TemperaturGrenze(){
			double [] T ={300,1000,5000};
			return T;	
		}
	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	private static class Koeffs_KrstFVV{		

		//Koeffizienten aus FVV-Zylindermodul --> ExcelSheet
		//siehe SAE-Paper 2007-01-0936 und FVV Zylinder Modul
		static double[] get_koeffs_RON_91_l() {


			double[] koeffs = {-1.9943761E+05, 3.7460241E+03,
					-2.2353473E+01,	1.3280568E-01, -1.1338874E-04,	
					5.5097532E-08,	-1.2257157E-11,	-2.2028224E+04,	1.4353132E+02};			
			return koeffs;
		}		

		static double[] get_koeffs_RON_91_h() {

			double[] koeffs = {4.0700881E+06,	-1.7225060E+04,
					3.3417377E+01,	3.4175622E-02,	-3.7652645E-06,
					-1.1938575E-08,	4.7162362E-12,	9.7471786E+04,	-1.9863604E+02};			
			return koeffs;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_RON_95_l() {
			double[] koeffs= {-2.3187103E+05,	4.5594879E+03,
					-2.9297345E+01,	1.4729421E-01,	-1.3928231E-04,
					7.2818013E-08,	-1.6327979E-11,	-2.7776078E+04,	1.8254742E+02};
			return koeffs;
		}

		static double[] get_koeffs_RON_95_h() {
			double[] koeffs= {5.5279312E+06,	-2.3723969E+04,
					4.5763687E+01,	1.8537447E-02,	-3.1901522E-06,
					-5.7417747E-09,	2.3590005E-12,	1.3321476E+05,	-2.7913386E+02};
			return koeffs;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_RON_98_l() {
			double[] koeffs= {-2.2095119E+05,	4.3222367E+03,
					-2.7488154E+01,	1.3936959E-01,	-1.2919894E-04,
					6.6384283E-08,	-1.4693411E-11,	-2.7573592E+04,	1.7038527E+02};
			return koeffs;
		}

		static double[] get_koeffs_RON_98_h() {
			double[] koeffs= {5.0073479E+06,	-2.1941778E+04,
					4.2387020E+01,	2.0242401E-02,	-4.0170939E-06,
					-5.5459546E-09,	2.3426894E-12,	1.2161657E+05,	-2.5969122E+02};
			return koeffs;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_Diesel_l() {
			double[] koeffs= {-5.5125885E+04,	1.1888903E+03,
					-5.8565879E+00,	1.2919006E-01,	-4.2765568E-05,
					-1.7715249E-08,	1.0701745E-11,	-5.0147461E+03,	5.3334288E+01};
			return koeffs;
		}	

		static double[] get_koeffs_Diesel_h() {
			double[] koeffs= {1.1909130E+06,	-5.8205100E+03,
					1.5458792E+01,	9.1059407E-02,	-4.8837993E-06,
					-3.6398609E-08,	1.4081972E-11,	3.3768000E+04,	-7.5721020E+01};
			return koeffs;
		}	


		// ////////////////////////////////////////////////////////////////////////

		static double [] get_TemperaturGrenze(){
			double [] T={300,1000,5000};
			return T;	
		}		

	}
	

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	
	private static class Koeffs_ERC {		
		
		static double[][] get_coeffs_nC7H16(){			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,-1.268361870E+000,  8.543558200E-002, 
	              -5.253467860E-005,  1.629457210E-008, -2.023949250E-012,
	              -2.565865650E+004, //orig. value 
	              //-54786.75178222912, //adapted for LHV
	              3.537329120E+001};
			
			//high
			double[] coeffs_h={0,0,2.221489690E+001,  3.476757500E-002, 
		              -1.184071290E-005,  1.832984780E-009, -1.061302660E-013,
		              -3.427600810E+004, //orig. value
		              //-63404.10344444763, //adapted for LHV
		              -9.230401960E+001};
			
			double [] T_threshold={300.0,  1391.0, 5000.0};				
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			return coeffs;
		}		
		
		static double[][] get_coeffs_O2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.212936000E+000, 1.127486000E-003, 
		              -5.756150000E-007,  1.313877000E-009, -8.768554000E-013,
		              -1.005249000E+003,  6.034738000E+000};
			
			//high
			double[] coeffs_h={0,0,3.697578000E+000,  6.135197000E-004, 
		              -1.258842000E-007,  1.775281000E-011, -1.136435000E-015,
		              -1.233930000E+003,  3.189166000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_N2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.298677000E+000,  1.408240000E-003, 
		              -3.963222000E-006,  5.641515000E-009, -2.444855000E-012,
		              -1.020900000E+003,  3.950372000E+000};
			
			//high
			double[] coeffs_h={0,0,2.926640000E+000,  1.487977000E-003, 
		              -5.684761000E-007,  1.009704000E-010, -6.753351000E-015,
		              -9.227977000E+002,  5.980528000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_CO2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.275725000E+000,  9.922072000E-003, 
		              -1.040911000E-005,  6.866687000E-009, -2.117280000E-012,
		              -4.837314000E+004,  1.018849000E+001};
			
			//high
			double[] coeffs_h={0,0,4.453623000E+000,  3.140169000E-003, 
		              -1.278411000E-006,  2.393997000E-010, -1.669033000E-014,
		              -4.896696000E+004, -9.553959000E-001};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_H2O(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.386842000E+000,  3.474982000E-003, 
		              -6.354696000E-006,  6.968581000E-009, -2.506588000E-012,
		              -3.020811000E+004,  2.590233000E+000};
			
			//high
			double[] coeffs_h={0,0,2.672146000E+000,  3.056293000E-003, 
		              -8.730260000E-007,  1.200996000E-010, -6.391618000E-015,
		              -2.989921000E+004,  6.862817000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_CO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.262452000E+000,  1.511941000E-003, 
		              -3.881755000E-006,  5.581944000E-009, -2.474951000E-012,
		              -1.431054000E+004,  4.848897000E+000};
			
			//high
			double[] coeffs_h={0,0,3.025078000E+000,  1.442689000E-003, 
		              -5.630828000E-007,  1.018581000E-010, -6.910952000E-015,
		              -1.426835000E+004,  6.108218000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_H2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.298124000E+000,  8.249442000E-004, 
		              -8.143015000E-007, -9.475434000E-011,  4.134872000E-013,
		              -1.012521000E+003, -3.294094000E+000};
			
			//high
			double[] coeffs_h={0,0,2.991423000E+000,  7.000644000E-004, 
		              -5.633829000E-008, -9.231578000E-012,  1.582752000E-015,
		              -8.350340000E+002, -1.355110000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_OH(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.637266000E+000,  1.850910000E-004, 
		              -1.676165000E-006,  2.387203000E-009, -8.431442000E-013,
		               3.606782000E+003,  1.358860000E+000};
			
			//high
			double[] coeffs_h={0,0,2.882730000E+000,  1.013974000E-003, 
		              -2.276877000E-007,  2.174684000E-011, -5.126305000E-016,
		               3.886888000E+003,  5.595712000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}	
		
		
		
		static double[][] get_coeffs_H2O2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,  3.388754000E+000,  6.569226000E-003, 
		              -1.485013000E-007, -4.625806000E-009,  2.471515000E-012,
		              -1.766315000E+004,  6.785363000E+000};
			
			//high
			double[] coeffs_h={0,0,4.573167000E+000,  4.336136000E-003, 
		              -1.474689000E-006,  2.348904000E-010, -1.431654000E-014,
		              -1.800696000E+004,  5.011370000E-001};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_HO2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,  2.979963000E+000,  4.996697000E-003, 
		              -3.790997000E-006,  2.354192000E-009, -8.089024000E-013,
		               1.762274000E+002,  9.222724000E+000};
			
			//high
			double[] coeffs_h={0,0,4.072191000E+000,  2.131296000E-003, 
		              -5.308145000E-007,  6.112269000E-011, -2.841165000E-015,
		              -1.579727000E+002,  3.476029000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_H(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.500000000E+000,  0.000000000E+000, 
		               0.000000000E+000,  0.000000000E+000,  0.000000000E+000,
		               2.547163000E+004, -4.601176000E-001};
			
			//high
			double[] coeffs_h={0,0,2.500000000E+000,  0.000000000E+000, 
		               0.000000000E+000,  0.000000000E+000,  0.000000000E+000,
		               2.547163000E+004, -4.601176000E-001};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_O(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.946429000E+000, -1.638166000E-003, 
		               2.421032000E-006, -1.602843000E-009,  3.890696000E-013,
		               2.914764000E+004,  2.963995000E+000};
			
			//high
			double[] coeffs_h={0,0, 2.542060000E+000, -2.755062000E-005, 
		              -3.102803000E-009,  4.551067000E-012, -4.368052000E-016,
		               2.923080000E+004,  4.920308000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_CH3O(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.106204000E+000,  7.216595000E-003, 
		               5.338472000E-006, -7.377636000E-009,  2.075611000E-012,
		               9.786011000E+002,  1.315218000E+001};
			
			//high
			double[] coeffs_h={0,0, 3.770800000E+000,  7.871497000E-003, 
		              -2.656384000E-006,  3.944431000E-010, -2.112616000E-014,
		               1.278325000E+002,  2.929575000E+000};
			
			double [] T_threshold={300.0,  1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_CH2O(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 1.652731000E+000,  1.263144000E-002, 
		              -1.888168000E-005,  2.050031000E-008, -8.413237000E-012,
		              -1.486540000E+004,  1.378482000E+001};
			
			//high
			double[] coeffs_h={0,0, 2.995606000E+000,  6.681321000E-003, 
		              -2.628955000E-006,  4.737153000E-010, -3.212517000E-014,
		              -1.532037000E+004,  6.912572000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_HCO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.898330000E+000,  6.199147000E-003, 
		              -9.623084000E-006,  1.089825000E-008, -4.574885000E-012,
		               4.159922000E+003,  8.983614000E+000};
			
			//high
			double[] coeffs_h={0,0, 3.557271000E+000,  3.345573000E-003, 
		              -1.335006000E-006,  2.470573000E-010, -1.713851000E-014,
		               3.916324000E+003,  5.552299000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_CH2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.762237000E+000,  1.159819000E-003, 
		               2.489585000E-007,  8.800836000E-010, -7.332435000E-013,
		               4.536791000E+004,  1.712578000E+000};			
			//high
			double[] coeffs_h={0,0, 3.636408000E+000,  1.933057000E-003, 
		              -1.687016000E-007, -1.009899000E-010,  1.808256000E-014,
		               4.534134000E+004,  2.156561000E+000};
			
			double [] T_threshold={250.0, 1000.0, 4000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_CH3(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.430443000E+000,  1.112410000E-002, 
		              -1.680220000E-005,  1.621829000E-008, -5.864953000E-012,
		               1.642378000E+004,  6.789794000E+000};			
			//high
			double[] coeffs_h={0,0,2.844052000E+000,  6.137974000E-003, 
		              -2.230345000E-006,  3.785161000E-010, -2.452159000E-014,
		               1.643781000E+004,  5.452697000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_CH4(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 7.787415000E-001,  1.747668000E-002, 
		              -2.783409000E-005,  3.049708000E-008, -1.223931000E-011,
		              -9.825229000E+003,  1.372219000E+001};			
			//high
			double[] coeffs_h={0,0,1.683479000E+000,  1.023724000E-002, 
		              -3.875129000E-006,  6.785585000E-010, -4.503423000E-014,
		              -1.008079000E+004,  9.623395000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}		
		
		static double[][] get_coeffs_C2H2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.013562000E+000,  1.519045000E-002, 
		              -1.616319000E-005,  9.078992000E-009, -1.912746000E-012,
		               2.612444000E+004,  8.805378000E+000};			
			//high
			double[] coeffs_h={0,0,4.436770000E+000,  5.376039000E-003, 
		              -1.912817000E-006,  3.286379000E-010, -2.156710000E-014,
		               2.566766000E+004, -2.800338000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C2H3(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.459276000E+000,  7.371476000E-003, 
		               2.109873000E-006, -1.321642000E-009, -1.184784000E-012,
		               3.335225000E+004,  1.155620000E+001};			
			//high
			double[] coeffs_h={0,0,5.933468000E+000,  4.017746000E-003, 
		              -3.966740000E-007, -1.441267000E-010,  2.378644000E-014,
		               3.185435000E+004, -8.530313000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C2H4(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, -8.614880000E-001,  2.796163000E-002, 
		              -3.388677000E-005,  2.785152000E-008, -9.737879000E-012,
		               5.573046000E+003,  2.421149000E+001};			
			//high
			double[] coeffs_h={0,0,3.528419000E+000,  1.148518000E-002, 
		              -4.418385000E-006,  7.844601000E-010, -5.266848000E-014,
		               4.428289000E+003,  2.230389000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}		
		
		static double[][] get_coeffs_C2H5(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.690702000E+000,  8.719133000E-003, 
		               4.419839000E-006,  9.338703000E-010, -3.927773000E-012,
		               1.287040000E+004,  1.213820000E+001};			
			//high
			double[] coeffs_h={0,0, 7.190480000E+000,  6.484077000E-003, 
		              -6.428065000E-007, -2.347879000E-010,  3.880877000E-014,
		               1.067455000E+004, -1.478089000E+001};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C3H4(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.613074870E+000,  1.212233710E-002, 
		               1.854054000E-005, -3.452584750E-008,  1.533533890E-011,
		               2.154156420E+004,  1.025033190E+001};			
			//high
			double[] coeffs_h={0,0, 6.316948690E+000,  1.113362620E-002, 
		              -3.962890180E-006,  6.356337750E-010, -3.787498850E-014,
		               2.011746170E+004, -1.097188620E+001};
			
			double [] T_threshold={200.0, 1000.0, 6000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C3H5(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.787946930E+000,  9.484143350E-003, 
		               2.423433680E-005, -3.656040100E-008,  1.485923560E-011,
		               1.862612180E+004,  7.828224990E+000};			
			//high
			double[] coeffs_h={0,0, 6.547611320E+000,  1.331522460E-002, 
		              -4.783331000E-006,  7.719498140E-010, -4.619308080E-014,
		               1.727147070E+004, -9.274868410E+000};
			
			double [] T_threshold={200.0, 1000.0, 6000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		
		static double[][] get_coeffs_C3H6(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.946154440E-001,  2.891076620E-002, 
		              -1.548868080E-005,  3.888142090E-009, -3.378903520E-013,
		               1.066881640E+003,  2.190037360E+001};			
			//high
			double[] coeffs_h={0,0, 8.015959580E+000,  1.370236340E-002, 
		              -4.662497330E-006,  7.212544020E-010, -4.173701260E-014,
		              -1.878212710E+003, -2.001606680E+001};
			
			double [] T_threshold={300.0, 1388.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C3H7(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 1.051551800E+000,  2.599198000E-002, 
		               2.380054000E-006, -1.960956900E-008,  9.373247000E-012,
		               1.063186300E+004,  2.112255900E+001};			
			//high
			double[] coeffs_h={0,0,7.702698700E+000,  1.604420300E-002, 
		              -5.283322000E-006,  7.629859000E-010, -3.939228400E-014,
		               8.298433600E+003, -1.548018000E+001};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_C7H15_2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, -3.791557670E-002,  7.567265700E-002, 
		              -4.074736340E-005,  9.326789430E-009, -4.923607450E-013,
		              -2.356053030E+003,  3.373215060E+001};			
			//high
			double[] coeffs_h={0,0, 2.163688420E+001,  3.233248040E-002, 
		              -1.092738070E-005,  1.683570600E-009, -9.717740910E-014,
		              -1.058736160E+004, -8.522096530E+001};
			
			double [] T_threshold={300.0, 1382.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C7H15O2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.374993340E+000,  8.346519060E-002, 
		              -5.138973200E-005,  1.642176620E-008, -2.195052160E-012,
		              -1.992379610E+004,  2.530673420E+001};			
			//high
			double[] coeffs_h={0,0, 2.490236890E+001,  3.507169200E-002, 
		              -1.204403060E-005,  1.874648220E-009, -1.089477910E-013,
		              -2.829760500E+004, -9.739235420E+001};
			
			double [] T_threshold={300.0, 1390.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_C7KET12(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 5.824336970E-001,  1.012078690E-001, 
		              -7.658559960E-005,  3.007386060E-008, -4.829027920E-012,
		              -4.680544190E+004,  3.333314490E+001};			
			//high
			double[] coeffs_h={0,0, 2.974729060E+001,  3.066222940E-002, 
		              -1.055635900E-005,  1.646273430E-009, -9.581716750E-014,
		              -5.668568280E+004, -1.224324900E+002};
			
			double [] T_threshold={300.0, 1396.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C5H11CO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 2.144790690E+000,  6.178635630E-002, 
		              -3.741346900E-005,  1.132837950E-008, -1.369176980E-012,
		              -1.434511720E+004,  2.231280450E+001};			
			//high
			double[] coeffs_h={0,0, 1.947838120E+001,  2.504660290E-002, 
		              -8.548613460E-006,  1.325579440E-009, -7.685032960E-014,
		              -2.079239370E+004, -7.219955780E+001};
			
			double [] T_threshold={300.0, 1383.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		static double[][] get_coeffs_N(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,2.503071000E+000, -2.180018000E-005, 
		               5.420529000E-008, -5.647560000E-011,  2.099904000E-014,
		               5.609890000E+004,  4.167566000E+000};			
			//high
			double[] coeffs_h={0,0, 2.450268000E+000,  1.066146000E-004, 
		              -7.465337000E-008,  1.879652000E-011, -1.025984000E-015,
		               5.611604000E+004,  4.448758000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_N2O(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,2.543058000E+000,  9.492193000E-003, 
		              -9.792775000E-006,  6.263845000E-009, -1.901826000E-012,
		               8.765100000E+003,  9.511222000E+000};			
			//high
			double[] coeffs_h={0,0,  4.718977000E+000,  2.873714000E-003, 
		              -1.197496000E-006,  2.250552000E-010, -1.575337000E-014,
		               8.165811000E+003, -1.657250000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_NO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,3.376542000E+000,  1.253063000E-003, 
		              -3.302751000E-006,  5.217810000E-009, -2.446263000E-012,
		               9.817961000E+003,  5.829590000E+000};	

			//high
			double[] coeffs_h={0,0, 3.245435000E+000,  1.269138000E-003, 
		              -5.015890000E-007,  9.169283000E-011, -6.275419000E-015,
		               9.800840000E+003,  6.417294000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		
		static double[][] get_coeffs_NO2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,2.670600000E+000,  7.838501000E-003, 
		              -8.063865000E-006,  6.161715000E-009, -2.320150000E-012,
		               2.896291000E+003,  1.161207000E+001};			
			//high
			double[] coeffs_h={0,0, 4.682859000E+000,  2.462429000E-003, 
		              -1.042259000E-006,  1.976902000E-010, -1.391717000E-014,
		               2.261292000E+003,  9.885985000E-001};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_iC8H18(){
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,-4.208688930E+000,  1.114405810E-001, 
		              -7.913465820E-005,  2.924062420E-008, -4.437431910E-012,
		              -2.994468750E+004, //original Value 
		              //-49720.94130043444, //adapted for LHV
		              4.495217010E+001};			
			//high
			double[] coeffs_h={0,0, 2.713735900E+001,  3.790048900E-002, 
		              -1.294373580E-005,  2.007603720E-009, -1.164005800E-013,
		              -4.079581770E+004, //original Value  
		              //-60572.071545029, //adapted for LHV
		              -1.232774950E+002};
			
			double [] T_threshold={300.0, 1396.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C8H17(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,-3.091042620E+000,  1.023188960E-001, 
		              -6.848588730E-005,  2.301839400E-008, -3.070130800E-012,
		              -6.628290690E+003,  4.311739320E+001};			
			//high
			double[] coeffs_h={0,0, 2.645691790E+001,  3.554207520E-002, 
		              -1.205209840E-005,  1.860893570E-009, -1.075718940E-013,
		              -1.703927910E+004, -1.162455110E+002};
			
			double [] T_threshold={300.0, 1393.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C8H17OO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,-3.070023560E+000,  1.226404380E-001, 
		              -9.722027640E-005,  4.091276250E-008, -7.091025470E-012,
		              -2.640143080E+004,  4.403456910E+001};			
			//high
			double[] coeffs_h={0,0,3.093516150E+001,  3.741025640E-002, 
		              -1.290709700E-005,  2.015448500E-009, -1.173989110E-013,
		              -3.774577530E+004, -1.367308000E+002};
			
			double [] T_threshold={300.0, 1394.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_iC8KET21(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,-3.657225670E+000,  1.271596770E-001, 
		              -1.007326220E-004,  4.133679430E-008, -6.923865750E-012,
		              -5.322146680E+004,  5.138865940E+001};			
			//high
			double[] coeffs_h={0,0,3.368802610E+001,  3.515308150E-002, 
		              -1.224496320E-005,  1.924592290E-009, -1.126259300E-013,
		              -6.575011900E+004, -1.475500240E+002};
			
			double [] T_threshold={300.0, 1393.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C6H13CO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 7.181291740E-001,  7.978140210E-002, 
		              -5.308785350E-005,  1.831059640E-008, -2.616739620E-012,
		              -1.869093660E+004,  2.711267250E+001};			
			//high
			double[] coeffs_h={0,0, 2.324129910E+001,  2.940139460E-002, 
		              -1.012108710E-005,  1.578122010E-009, -9.183499760E-014,
		              -2.679732040E+004, -9.474602880E+001};
			
			double [] T_threshold={300.0, 1393.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C4H9(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.548852350E+000,  1.787476380E-002, 
		               5.007828250E-005, -7.944750710E-008,  3.358023540E-011,
		               4.740115880E+003,  1.118493820E+001};			
			//high
			double[] coeffs_h={0,0, 9.430406070E+000,  2.342713490E-002, 
		              -8.535991820E-006,  1.397483550E-009, -8.440574560E-014,
		               2.142148620E+003, -2.422079940E+001};
			
			double [] T_threshold={200.0, 1000.0, 6000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_C2H6(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 1.462539000E+000,  1.549467000E-002, 
		               5.780507000E-006, -1.257832000E-008,  4.586267000E-012,
		               -1.123918000E+004,  1.443229000E+001};			
			//high
			double[] coeffs_h={0,0, 4.825938000E+000,  1.384043000E-002, 
		              -4.557259000E-006,  6.724967000E-010, -3.598161000E-014,
		              -1.271779000E+004, -5.239507000E+000};
			
			double [] T_threshold={300.0, 1000.0, 4000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		
		static double[][] get_coeffs_CH2CHO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0, 3.409062000E+000,  1.073857000E-002, 
		               1.891492000E-006, -7.158583000E-009,  2.867385000E-012,
		               1.521477000E+003,  9.558290000E+000};			
			//high
			double[] coeffs_h={0,0, 5.975670000E+000,  8.130591000E-003, 
		              -2.743624000E-006,  4.070304000E-010, -2.176017000E-014,
		               4.903218000E+002, -5.045251000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		
		
		static double[][] get_coeffs_CH2CO(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,2.974971000E+000,  1.211871000E-002, 
		              -2.345046000E-006, -6.466685000E-009,  3.905649000E-012,
		              -7.632637000E+003,  8.673553000E+000};			
			//high
			double[] coeffs_h={0,0,6.038817000E+000,  5.804840000E-003, 
		              -1.920954000E-006,  2.794485000E-010, -1.458868000E-014,
		              -8.583402000E+003, -7.657581000E+000};
			
			double [] T_threshold={300.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}	
		
		
		static double[][] get_coeffs_CH3O2(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,4.261469060E+000,  1.008735990E-002, 
		              -3.215061840E-006,  2.094092670E-010,  4.183391030E-014,
		               4.731296530E+002,  6.345990670E+000};			
			//high
			double[] coeffs_h={0,0,5.957878910E+000,  7.907286260E-003, 
		              -2.682462340E-006,  4.138913370E-010, -2.390073300E-014,
		              -3.782244680E+002, -3.536951390E+000};
			
			double [] T_threshold={300.0, 1385.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_CH3O2H(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,3.234428170E+000,  1.901297670E-002, 
		              -1.133862870E-005,  3.403066530E-009, -4.118302220E-013,
		              -1.771979260E+004,  9.759510750E+000};			
			//high
			double[] coeffs_h={0,0,8.431170910E+000,  8.068179090E-003, 
		              -2.770949210E-006,  4.313322430E-010, -2.506921460E-014,
		              -1.966787710E+004, -1.861379160E+001};
			
			double [] T_threshold={300.0, 1390.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}
		
		
		static double[][] get_coeffs_soot(){		
			
			double [][] coeffs=new double[3][];
			//low
			double[] coeffs_l={0,0,-3.108720700E-001,  4.403536900E-003, 
		               1.903941200E-006, -6.385469700E-009,  2.989642500E-012,
		               -1.086507900E+002,  1.113829500E+000};			
			//high
			double[] coeffs_h={0,0,1.455692400E+000,  1.717063800E-003, 
		              -6.975841000E-007,  1.352831600E-010, -9.676490500E-015,
		              -6.951280400E+002, -8.525684200E+000};
			
			double [] T_threshold={200.0, 1000.0, 5000.0};	
			
			coeffs[0]=coeffs_l;
			coeffs[1]=coeffs_h;
			coeffs[2]=T_threshold;			
			
			return coeffs;
		}		
	
	}

}
