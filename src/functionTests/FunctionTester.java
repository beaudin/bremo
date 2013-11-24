package functionTests;



import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import KivaPostProcessing.KivaPostProcessor;
import berechnungsModule.Berechnung.APR_Cantera;
import berechnungsModule.Berechnung.CanteraCaller;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.chemEquilibriumSolver.ChemEquilibriumSolver;
import berechnungsModule.mixtureFormation.Evaporation;
import berechnungsModule.mixtureFormation.FuelProperties;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_reciprocatingPiston;
import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremo.parameter.IndicatorData;
import bremoExceptions.BirdBrainedProgrammerException;
import kalorik.GasMixture;
import kalorik.CoefficientsSpecies;
import kalorik.Spezies;
import kalorik.SpeciesFactory;
import io.*;
import matLib.DiefferetialEqationSys;
import matLib.Integrator;
import matLib.MatLibBase;
import matLib.RungeKutta;
import misc.LHV_Calculator;
import misc.LittleHelpers;
import misc.PhysConst;


public class FunctionTester {
	private final CasePara CP;

	public FunctionTester(CasePara cp){
		CP=cp;
	}
	
	public void estimateEGR(){
		double xO2Match=14.58216216/100;
		Spezies ron= CP.SPECIES_FACTROY.get_iC8H18();
		Spezies diesel= CP.SPECIES_FACTROY.get_nC7H16();
		double mGasoline=0.019465692/1000; //kg/cyc
		double mDiesel=0.001249111/1000; //kg/cyc	
		
		
		double mKrstTot=mDiesel+mGasoline;
		
		GasMixture krst=new GasMixture("krst");
		Hashtable<Spezies,Double> krst_MassenbruchHash=new Hashtable<Spezies,Double>();		
		krst_MassenbruchHash.put(ron, mGasoline/mKrstTot);
		krst_MassenbruchHash.put(diesel, mDiesel/mKrstTot);	
		krst.set_Gasmischung_massenBruch(krst_MassenbruchHash);
	
		
		Spezies luft=CP.SPECIES_FACTROY.get_spezLuft_trocken();
		Spezies wasser=CP.SPECIES_FACTROY.get_H2O();

		double lambda=1.719336482;

		double lst=krst.get_Lst();	
		double mk=1; //[kg]
		double mL_tr=lambda*mk*lst;
		double mW=mL_tr*0;
		double m_ges=mk+mL_tr+mW; 	



		//Gemisch erzeugen
		//feucht
		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(luft, mL_tr/m_ges);	
		gemisch_MassenbruchHash.put(wasser, mW/m_ges);	
		GasMixture gemisch=new GasMixture("gg");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);		
		//Verbrennen
		ChemEquilibriumSolver gg=CP.OHC_SOLVER;
		GasMixture abgas=new GasMixture("Abgas");			
		abgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(1e5, 1000, gemisch)); 
		
		Hashtable<Spezies,Double> air_WetHT=new Hashtable<Spezies,Double>();	
		double m_gesAir=mL_tr+mW;
		air_WetHT.put(luft, mL_tr/m_gesAir);	
		air_WetHT.put(wasser, mW/m_gesAir);	
		GasMixture airWet=new GasMixture("gg");
		airWet.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
		
		double mTot=1;
		double xEGRLow=0,xEGRHigh=1,xEGR=0.1;
		double xO2_new;
		int iter=0;
		
		do{
			double mEGR=xEGR*mTot;
			double mGemisch = mTot-mEGR;
			Hashtable<Spezies,Double> gemisch2_MassenbruchHash=new Hashtable<Spezies,Double>();	
			gemisch2_MassenbruchHash.put(airWet, mGemisch/mTot);
			gemisch2_MassenbruchHash.put(abgas, mEGR/mTot);		
			GasMixture newGemisch=new GasMixture("test");			
			newGemisch.set_Gasmischung_massenBruch(gemisch2_MassenbruchHash);
			xO2_new=newGemisch.get_speziesMolenBruecheDetailToIntegrate().get(CP.SPECIES_FACTROY.get_O2());
			if(xO2_new>xO2Match){
				xEGRLow=xEGR;
				xEGR=0.5*(xEGR+xEGRHigh);				
			}else if(xO2_new<xO2Match){
				xEGRHigh=xEGR;
				xEGR=0.5*(xEGR+xEGRLow);
				
			}
			iter+=1;
		}while(Math.abs((xO2_new-xO2Match)/xO2Match)>0.001||iter>555);
		
		System.out.println(xEGR);


		
	}
	
	public void getInitialConditions_2(){
		//determins if OH, H, CO are considered in the output file!
		boolean detailedOutput=false;
		//iterates over the residual fraction to match a specified temperature
		boolean iterT_set=true;
		double airTrappingRatio=1.0;


		//Inputs mode 3
//		
//		//		double residualFraction=0.0825; 
//		double residualFraction=0.045; //from GT-Power
//		double mAir=0.494735124/1000;	//kg/cyc
//		double mGasoline=0.007038168/1000; //kg/cyc
//		double mDiesel=0.002950016/1000; //kg/cyc	
//		double xrg=0.0;	//no unit
//		double p_Ivc=1.229459584E+05;	//Pa
//		double T_set=358; //K
				
//		//aus GTPower
//		mAir=0.000498507;
//		p_Ivc=122739.91;
//		T_set=366.55449;
		
		//mode 4
//		double mGasoline=0.012592969/1000; //kg/cyc
//		double mDiesel=0.001817083/1000; //kg/cyc	
//		double xrg=0.0; //Keine Unterscheidung zwischen Massen- und Molenbruch	
//		double p_Ivc=1.56*1e5;
//		double T_Ivc=402;	
		
		//mode 6
//		double mGasoline=1.27019E-05; //kg/cyc
//		double mDiesel=1.4106E-06; //kg/cyc	
//		double xrg=0.0; //Keine Unterscheidung zwischen Massen- und Molenbruch
//		double p_Ivc=1.49*1e5;
//		double T_Ivc=357;
		
//		mode 7
		double mGasoline=1.18803E-05; //kg/cyc
		double mDiesel=2.05577E-06; //kg/cyc	
		double xrg=0.0; //Keine Unterscheidung zwischen Massen- und Molenbruch
		double p_Ivc=1.515634497*1e5;
		double T_set=375;
		double mAir=0.000606971;	//kg/cyc
		double residualFraction=0.0555;
		
		//Inputs Mode8
//				double airTrappingRatio=1.0;
//				double residualFraction=0.05; 
//				double mAir=0.528391373/1000;	//kg/cyc
//				double mGasoline=0.019465692/1000; //kg/cyc
//				double mDiesel=0.001249111/1000; //kg/cyc	
//				double xrg=47.85415703/100; //Keine Unterscheidung zwischen Massen- und Molenbruch
//				double p_Ivc=1.9*1e5;
//				double T_set=375;

		boolean goOn=false;
		double residualFraction_low=0, residualFraction_high=1;
		GasMixture gg_Ivc=new GasMixture("ggIVC");
		double T_Ivc;
		do{		
			//calculated from the inputs
			double mEGR_Ex=xrg/(1-xrg)*mAir;
			//amount of Air that is trapped
			double mAirTrapped=mAir*airTrappingRatio;
			//residual fraction based on total mass at IVC
			double mEGR_Int=residualFraction/(1-residualFraction)*(mAirTrapped+mEGR_Ex+mGasoline);

			//Producing EGR
			double mTotCombust=mAirTrapped+mGasoline+mDiesel;
			Hashtable<Spezies,Double> ht=new Hashtable<Spezies,Double>();
			ht.put(CP.SPECIES_FACTROY.get_nC7H16(), mDiesel/mTotCombust);
			ht.put(CP.SPECIES_FACTROY.get_iC8H18(), mGasoline/mTotCombust);
			ht.put(CP.SPECIES_FACTROY.get_spezLuft_trocken(), mAirTrapped/mTotCombust);
			GasMixture gg=new GasMixture("gg");
			gg.set_Gasmischung_massenBruch(ht);
			ht=null;
			Hashtable<Spezies,Double> htGG;
			if(detailedOutput){
				htGG=CP.OHC_SOLVER.get_GG_molenBrueche(1e5, 1600, gg);	
			}else{
				htGG=LHV_Calculator.calcMolenBruechePerfekteVerbrennung(CP, gg);
			}

			GasMixture egr=new GasMixture("EGR");
			egr.set_Gasmixture_moleFracs(htGG);
			gg=null;

			//Gasmixture at IVC
			double mTotIvc=mAirTrapped+mGasoline+mEGR_Ex+mEGR_Int;		
			Hashtable<Spezies,Double> ht2=new Hashtable<Spezies,Double>();	
			ht2.put(CP.SPECIES_FACTROY.get_iC8H18(), mGasoline/mTotIvc);
			ht2.put(CP.SPECIES_FACTROY.get_spezLuft_trocken(), mAirTrapped/mTotIvc);
			ht2.put(egr, (mEGR_Ex+mEGR_Int)/mTotIvc);		
			gg_Ivc.set_Gasmischung_massenBruch(ht2);

			//Temperature at IVC
			//make sure the run starts at IVC
			double V_Ivc=CP.MOTOR.get_V(0); 
			T_Ivc=p_Ivc*V_Ivc/gg_Ivc.get_R()/mTotIvc;


			if(iterT_set){
				if(Math.abs((T_set-T_Ivc)/T_set)>0.0001){
					goOn=true;
					if(T_Ivc>=T_set)
						residualFraction_low=residualFraction;					
					else if(T_Ivc<T_set)
						residualFraction_high=residualFraction;	
						
					residualFraction=0.5*(residualFraction_high+residualFraction_low);					
				}else
					goOn=false;				
			}
		}while(goOn);


		double x_iC8H18=0;		
		double x_O2=0;		
		double x_CO2=0;		
		double x_H2O=0;		
		double x_H2=0;		
		double x_H=0;		
		double x_CO=0;		
		double x_OH=0;		
		double x_O=0;		
		double x_NO=0;		
		double x_N=0;		
		double x_N2=0;

		Hashtable<Spezies,Double> comp=gg_Ivc.get_speziesMolenBruecheDetailToIntegrate();		
		try{
			x_iC8H18=comp.get(CP.SPECIES_FACTROY.get_iC8H18());	
		}catch (NullPointerException npe){}
		try{
			x_O2=comp.get(CP.SPECIES_FACTROY.get_O2());	
		}catch (NullPointerException npe){}
		try{
			x_CO2=comp.get(CP.SPECIES_FACTROY.get_CO2());	
		}catch (NullPointerException npe){}
		try{
			x_H2O=comp.get(CP.SPECIES_FACTROY.get_H2O());	
		}catch (NullPointerException npe){}
		try{
			x_H2=comp.get(CP.SPECIES_FACTROY.get_H2());		
		}catch (NullPointerException npe){}
		try{
			x_H=comp.get(CP.SPECIES_FACTROY.get_H());		
		}catch (NullPointerException npe){}
		try{
			x_CO=comp.get(CP.SPECIES_FACTROY.get_CO());	
		}catch (NullPointerException npe){}
		try{
			x_OH=comp.get(CP.SPECIES_FACTROY.get_OH());	
		}catch (NullPointerException npe){}
		try{
			x_O=comp.get(CP.SPECIES_FACTROY.get_O());	
		}catch (NullPointerException npe){}
		try{
			x_NO=comp.get(CP.SPECIES_FACTROY.get_NO());	
		}catch (NullPointerException npe){}
		try{
			x_N=comp.get(CP.SPECIES_FACTROY.get_N());	
		}catch (NullPointerException npe){}
		try{
			x_N2=comp.get(CP.SPECIES_FACTROY.get_N2());
		}catch (NullPointerException npe){}	

		double sum=x_N2+x_N+x_NO+x_CO2+x_O2+x_O+x_H2O+x_OH+x_CO+x_H+x_H2+x_iC8H18;
		if((sum-1)>1e-5){
			try{
				throw new BirdBrainedProgrammerException("F**k Molfraction do not sum up to 1!!!");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		//runden damit das output fuer kiva passt --> es darf nur eine bestimmte Anzahl an Stellen geben		
		double exp=1e6;
		x_iC8H18=Math.round(exp*x_iC8H18)/exp;
		x_O2=Math.round(exp*x_O2)/exp;
		x_CO2=Math.round(exp*x_CO2)/exp;
		x_H2O=Math.round(exp*x_H2O)/exp;	
		x_H2=Math.round(exp*x_H2)/exp;	
		x_H=Math.round(exp*x_H)/exp;
		x_CO=Math.round(exp*x_CO)/exp;
		x_OH=Math.round(exp*x_OH)/exp;
		x_O=Math.round(exp*x_O)/exp;
		x_NO=Math.round(exp*x_NO)/exp;
		x_N=Math.round(exp*x_N)/exp;
		//		x_N2=Math.round(exp*x_N2)/exp;

		x_N2=1-(x_N+x_NO+x_CO2+x_O2+x_O+x_H2O+x_OH+x_CO+x_H+x_H2+x_iC8H18);
		x_N2=exp*x_N2;
		x_N2=Math.round(x_N2)/exp;		

		FileWriter_txt fw2=new FileWriter_txt(CP.get_workingDirectory()+"//ckinp");
		String [] line =new String [1];
		line[0]="DERC Engine, 0 percent EGR";
		fw2.writeTextLineToFile(line, false);
		line[0]="ic8h18           " + x_iC8H18;
		fw2.writeTextLineToFile(line, true);
		line[0]="o2               " + x_O2;
		fw2.writeTextLineToFile(line, true);
		line[0]="n2               " + x_N2;
		fw2.writeTextLineToFile(line, true);	
		if(x_CO2>0){
			line[0]="co2              " + x_CO2;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_H2O>0){
			line[0]="h2o              " + x_H2O;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_H2>0){
			line[0]="h2               " + x_H2;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_H>0){
			line[0]="h                " + x_H;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_CO>0){
			line[0]="co               " + x_CO;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_OH>0){
			line[0]="oh               " + x_OH;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_O>0){
			line[0]="o                " + x_O;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_NO>0){
			line[0]="no               " + x_NO;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_N>0){
			line[0]="n                " + x_N;
			fw2.writeTextLineToFile(line, true);
		}		
		line[0]="end";
		fw2.writeTextLineToFile(line, true);
		line[0]=p_Ivc/1e5 + "   ! p at IVC (bar)";
		fw2.writeTextLineToFile(line, true);
		line[0]=T_Ivc + "   ! t at IVC (K)";
		fw2.writeTextLineToFile(line, true);
		line[0]="0.e-4";
		fw2.writeTextLineToFile(line, true);
		line[0]="500.0";
		fw2.writeTextLineToFile(line, true);
		line[0]="0.0";
		fw2.writeTextLineToFile(line, true);
		line[0]="140.0";
		fw2.writeTextLineToFile(line, true);		

		fw2=null;
		line=null;

		FileWriter_txt fw3=new FileWriter_txt(CP.get_workingDirectory()+"//bremoIni.txt");
		String [] line3 =new String [1];
		line3[0]=" ";
		fw3.writeTextLineToFile(line3, false);
		line3[0]="iniMoleFrac_iC8H18\t[-]\t:=\t" + x_iC8H18;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_O2\t[-]\t:=\t" + x_O2;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_CO2\t[-]\t:=\t" + x_CO2;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_H2O\t[-]\t:=\t" + x_H2O;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_N2\t[-]\t:=\t" + x_N2;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_OH\t[-]\t:=\t" + x_OH;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_CO\t[-]\t:=\t" + x_CO;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_H\t[-]\t:=\t" + x_H;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_H2\t[-]\t:=\t" + x_H2;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_O\t[-]\t:=\t" + x_O;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_NO\t[-]\t:=\t" + x_NO;
		fw3.writeTextLineToFile(line3, true);
		line3[0]="iniMoleFrac_N\t[-]\t:=\t" + x_N;
		fw3.writeTextLineToFile(line3, true);

		line3[0]="p_Ini\t[Pa]\t:=\t" +p_Ivc;
		fw3.writeTextLineToFile(line3, true);	

		line3[0]="T_Ini\t[K]\t:=\t" +T_Ivc;
		fw3.writeTextLineToFile(line3, true);
		
		line3[0]="residualFraction\t[-]\t:=\t" +residualFraction;
		fw3.writeTextLineToFile(line3, true);
		
		System.out.println("done");			
	}
	
	public void getInitialconditions(){
		//Im Vergleich zum Matlab-skript von yifeng liefert diese 
		//Methode leicht abweichende Werte --> bei O2 bis max 0.24% 
		//Ursache ist die leicht unterschiedliche Zusammensetzung von Luft in Bremo im vgl. zu Yifengs Skript.
		//O2-Brmeo/O2-Yifeng=0.209476/0.21
		//Mit der selben Zusammensetzung sind die Ergebnisse identisch!
		
//		mode 3
//		double mGasoline=0.007038168/1000; //kg/cyc
//		double mDiesel=0.002950016/1000; //kg/cyc	
//		double xrg=0.0;
//		double p_Ivc=1.23*1e5;
//		double T_Ivc=351;	
		
//		mode 3 GT Power
		double mGasoline=0.007038168/1000; //kg/cyc
		double mDiesel=0.002950016/1000; //kg/cyc	
		double xrg=0.0;
		double p_Ivc=122739.91;
		double T_Ivc=366.55449;	
	
		//mode 4
//		double mGasoline=0.012592969/1000; //kg/cyc
//		double mDiesel=0.001817083/1000; //kg/cyc	
//		double xrg=0.0; //Keine Unterscheidung zwischen Massen- und Molenbruch	
//		double p_Ivc=1.56*1e5;
//		double T_Ivc=402;	
		
		//mode 6
//		double mGasoline=1.27019E-05; //kg/cyc
//		double mDiesel=1.4106E-06; //kg/cyc	
//		double xrg=0.0; //Keine Unterscheidung zwischen Massen- und Molenbruch
//		double p_Ivc=1.49*1e5;
//		double T_Ivc=357;
		
		//mode 7
//		double mGasoline=1.18803E-05; //kg/cyc
//		double mDiesel=2.05577E-06; //kg/cyc	
//		double xrg=0.0; //Keine Unterscheidung zwischen Massen- und Molenbruch
//		double p_Ivc=1.52*1e5;
//		double T_Ivc=370;
		
		//mode 8
//		double mGasoline=0.019465692/1000; //kg/cyc
//		double mDiesel=0.001249111/1000; //kg/cyc	
//		double xrg=0.406; //Keine Unterscheidung zwischen Massen- und Molenbruch
//		double p_Ivc=1.9*1e5;
//		double T_Ivc=407;


		//Hier muss darauf geachtet werden, dass im Inoputfile 
//		der richtige Motor definiert wurde, 
//		und dass die Rechnung bei IVC startet
		double V_Ivc=CP.MOTOR.get_V(0); 
		
		double moles_totIVC=p_Ivc*V_Ivc/PhysConst.get_R_allg()/T_Ivc;
		double moles_iC8H18=mGasoline/CP.SPECIES_FACTROY.get_iC8H18().get_M();
		double moles_nC7H16=mDiesel/CP.SPECIES_FACTROY.get_nC7H16().get_M();
		double moles_air_egr=moles_totIVC-moles_iC8H18;
		double moles_egr=moles_air_egr*xrg;
		double moles_air=moles_air_egr*(1-xrg);
		double mAir=moles_air*CP.SPECIES_FACTROY.get_spezLuft_trocken().get_M();
		
		double nft=moles_iC8H18+moles_nC7H16;
		double Mf=moles_iC8H18/nft*CP.SPECIES_FACTROY.get_iC8H18().get_M()+moles_nC7H16/nft*CP.SPECIES_FACTROY.get_nC7H16().get_M();
		
		
		//composition egr
		double moles_tot=moles_air+moles_nC7H16+moles_iC8H18;
		Hashtable<Spezies,Double> ht=new Hashtable<Spezies,Double>();
		ht.put(CP.SPECIES_FACTROY.get_nC7H16(), moles_nC7H16/moles_tot);
		ht.put(CP.SPECIES_FACTROY.get_iC8H18(), moles_iC8H18/moles_tot);
		ht.put(CP.SPECIES_FACTROY.get_spezLuft_trocken(), moles_air/moles_tot);
		GasMixture gg=new GasMixture("gg");
		gg.set_Gasmixture_moleFracs(ht);
		double phi=1/gg.get_lambda();
		double Lst=gg.get_Lst();
		
		Hashtable<Spezies,Double> htGG=CP.OHC_SOLVER.get_GG_molenBrueche(1e5, 1000, gg);
//		gg.set_Gasmischung_molenBruch(htGG);
//		double ggR=gg.get_M();
		
		//aC+bH+kO+dN --> eCO2+fH2O+gN2+iO2
		double a=gg.get_AnzC_Atome();
		double b=gg.get_AnzH_Atome();	
		double k=gg.get_AnzO_Atome();	
		double d=gg.get_AnzN_Atome();	
		//CO2
		double e=(a);
		double f= (0.5*b);
		double g=(0.5*d);
		double i=(0.5*k-a-0.25*b);

		double moleAbgas=e+f+g+i;
		double xCO2_EGR=e/moleAbgas;
		double xH2O_EGR=f/moleAbgas;
		double xN2_EGR=g/moleAbgas;
		double xO2_EGR=i/moleAbgas;
				
		double sum_EGR=xO2_EGR+xCO2_EGR+xH2O_EGR+xN2_EGR;
		
		//chk
		double xCO2_EGRCHK=htGG.get(CP.SPECIES_FACTROY.get_CO2());
		double xH2O_EGRCHK=htGG.get(CP.SPECIES_FACTROY.get_H2O());
		double xO2_EGRCHK=htGG.get(CP.SPECIES_FACTROY.get_O2());		
		double xN2_EGRCHK=htGG.get(CP.SPECIES_FACTROY.get_N2());
		
		double moles_o2=moles_air*PhysConst.get_vol_O2_Luft()+xO2_EGR*moles_egr;
		double moles_co2=moles_air*PhysConst.get_vol_CO2_Luft()+xCO2_EGR*moles_egr;
		double moles_h2o=xH2O_EGR*moles_egr;
		double moles_n2=moles_air*PhysConst.get_vol_N2_Luft()+xN2_EGR*moles_egr;		
//		double moles_n2=moles_air-moles_o2;
		
		double x_iC8H18=moles_iC8H18/moles_totIVC;
		double x_O2=moles_o2/moles_totIVC;
		double x_CO2=moles_co2/moles_totIVC;
		double x_H2O=moles_h2o/moles_totIVC;		
		double x_N2=moles_n2/moles_totIVC;		
		double sum=x_iC8H18+x_O2+x_CO2+x_H2O+x_N2;
		if((sum-1)>1e-5){
			try{
				throw new BirdBrainedProgrammerException("F**k Molfraction do not sum up to 1!!!");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		
		//runden damit das output fuer kiva passt --> es darf nur eine bestimmte Anzahl an Stellen geben
		double exp=1e6;
		x_iC8H18=exp*x_iC8H18;
		x_iC8H18=Math.round(x_iC8H18)/exp;
		
		x_O2=exp*x_O2;
		x_O2=Math.round(x_O2)/exp;
		
		x_CO2=exp*x_CO2;
		x_CO2=Math.round(x_CO2)/exp;
		
		x_H2O=exp*x_H2O;
		x_H2O=Math.round(x_H2O)/exp;		
		
		x_N2=1-x_O2-x_iC8H18-x_H2O-x_CO2;
		x_N2=exp*x_N2;
		x_N2=Math.round(x_N2)/exp;
		
		System.out.println("x_O2: " +x_O2 );
		System.out.println("x_N2: " +x_N2 );
		System.out.println("x_iC8H18: " +x_iC8H18 );
		System.out.println("Sum: " +(x_iC8H18+x_O2 +x_N2+x_H2O+x_CO2));
		
		FileWriter_txt fw2=new FileWriter_txt(CP.get_workingDirectory()+"//ckinp");
		String [] line =new String [1];
		line[0]="DERC Engine, 0 percent EGR";
		fw2.writeTextLineToFile(line, false);
		line[0]="ic8h18           " + x_iC8H18;
		fw2.writeTextLineToFile(line, true);
		line[0]="o2               " + x_O2;
		fw2.writeTextLineToFile(line, true);
		line[0]="n2               " + x_N2;
		fw2.writeTextLineToFile(line, true);	
		if(x_CO2>0){
			line[0]="co2               " + x_CO2;
			fw2.writeTextLineToFile(line, true);
		}
		if(x_H2O>0){
			line[0]="h2o               " + x_H2O;
			fw2.writeTextLineToFile(line, true);
		}
		line[0]="end";
		fw2.writeTextLineToFile(line, true);
		line[0]=p_Ivc/1e5 + "   ! p at IVC (bar)";
		fw2.writeTextLineToFile(line, true);
		line[0]=T_Ivc + "   ! t at IVC (K)";
		fw2.writeTextLineToFile(line, true);
		line[0]="0.e-4";
		fw2.writeTextLineToFile(line, true);
		line[0]="500.0";
		fw2.writeTextLineToFile(line, true);
		line[0]="0.0";
		fw2.writeTextLineToFile(line, true);
		line[0]="140.0";
		fw2.writeTextLineToFile(line, true);		

		System.out.println("done");	
	}

	public void testDieselMassFracion(){
		//creating the mixture without Diesel
		double [] y=new double [CP.SPECIES_FACTROY.get_nbrOfSpecies()];		
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_nC7H16())]=0.0;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_O2())]=0.1327011879;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N2())]=0.7312165251;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO2())]=0.0787026718;		
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H2O())]=0.0343273968;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_iC8H18())]=0.0230522184;	
		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
		double sum=0;
		for(int j=0;j<y.length;j++)sum=sum+y[j];
		for(int j=0;j<y.length;j++)y[j]=y[j]/sum;
		for(int j=0;j<y.length;j++) ht.put(CP.SPECIES_FACTROY.get_Spez(j), y[j]);			
		GasMixture gasMix=new GasMixture("mixGas");
		gasMix.set_Gasmixture_moleFracs(ht);
		//nbr of moles in gasMix
		double n0=gasMix.get_AnzC_Atome()
				+gasMix.get_AnzH_Atome()
				+gasMix.get_AnzN_Atome()
				+gasMix.get_AnzO_Atome();
		//molefractions of N and C in the mixture without Diesel aka gasMix
		double mfN_0=gasMix.get_AnzN_Atome()/n0;
		double mfC_0=gasMix.get_AnzC_Atome()/n0;	
		
		//creating the mixture with Diesel aka dieslGasMix
		Spezies nC7H16=CP.SPECIES_FACTROY.get_nC7H16();		
		double molD=0;		
		double molG=1;		
		double molTot=molG+molD;
		
		double mG=molG*gasMix.get_M();
		double mD=molD*nC7H16.get_M();
		double mTot=mD+mG;
		double massfG=mG/mTot;
		double massfD= mD/mTot;
		
		Hashtable<Spezies, Double> ht2=new Hashtable<Spezies,Double>();
		GasMixture dieslGasMix=new GasMixture("gg");
		ht2.put(gasMix, molG/molTot);
		ht2.put(nC7H16, molD/molTot);
		dieslGasMix.set_Gasmixture_moleFracs(ht2);			
		
		//dieselgasMix 1
		//nbr of moles of gasMix in dieslGasMix (N is used as tracer species)
		double nMix=dieslGasMix.get_AnzN_Atome()/mfN_0;
		//nbr of C-Atoms that come from Diesel
		double nC_Diesel=dieslGasMix.get_AnzC_Atome()-nMix*mfC_0;
		//nbr molefraction of Diesel in 1 mol of mixture
		double molFrac_Diesel=nC_Diesel/nC7H16.get_AnzC_Atome();		
		System.out.println("molFrac_Diesel: "+ molFrac_Diesel);	
		
		double massFracDiesel=molFrac_Diesel*nC7H16.get_M()/dieslGasMix.get_M();
		System.out.println("massFrac_Diesel: "+ massFracDiesel);
		System.out.println("Abweichung: "+ (massFracDiesel-massfD)/massfD*100);
		double mDiesel=massFracDiesel*mTot;
	}
	
	
	public void testTurbFileReader(){
		TurbulenceFileReader	tfr=new TurbulenceFileReader(CP);
		double t0=CP.SYS.SIMULATION_START_SEC;
		double t1=CP.SYS.SIMULATION_END_SEC;	
		double delta=(t1-t0)/CP.SYS.NBR_OF_CALCULATED_VALUES;
		double test=t0+delta*CP.SYS.NBR_OF_CALCULATED_VALUES;
		double t=t0;
		double data[][]=new double[4][CP.SYS.NBR_OF_CALCULATED_VALUES];
		for(int i=0;i<CP.SYS.NBR_OF_CALCULATED_VALUES;i++){
			data[0][i]=CP.convert_SEC2KW(t);
			data[1][i]=tfr.get_TKE(t) ;
			data[2][i]=tfr.get_EPS(t);
			data[3][i]=tfr.get_TI(t);
			t=t+delta;
		}
		String header[]=new String[4];
		header[0]="CAD";
		header[1]="TKE";
		header[2]="EPS";
		header[3]="INT";
		FileWriter_txt fw2=new FileWriter_txt(CP.get_workingDirectory()+"//chkTurb.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(MatLibBase.transp_2d_array(data), true);			
	}
	
	
	public void testIndiFileReader(){
		IndicatorData indi=new IndicatorData(CP);		
		double t0=CP.SYS.SIMULATION_START_SEC;
		double t1=CP.SYS.SIMULATION_END_SEC;	
		double delta=(t1-t0)/CP.SYS.NBR_OF_CALCULATED_VALUES;
		double test=t0+delta*CP.SYS.NBR_OF_CALCULATED_VALUES;
		double t=t0;
		double data[][]=new double[4][CP.SYS.NBR_OF_CALCULATED_VALUES];
		for(int i=0;i<CP.SYS.NBR_OF_CALCULATED_VALUES;i++){
			data[0][i]=CP.convert_SEC2KW(t);
			data[1][i]=indi.get_pZyl(t)*1e-5;
			data[2][i]=indi.get_pEin(t)*1e-5;
			data[3][i]=indi.get_pAus(t)*1e-5;
			t=t+delta;
		}
		String header[]=new String[4];
		header[0]="CAD";
		header[1]="pZyl";
		header[2]="pEin";
		header[3]="pAus";
		FileWriter_txt fw2=new FileWriter_txt(CP.get_workingDirectory()+"//chkIndi.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(MatLibBase.transp_2d_array(data), true);		
	}
	
	public void readMultiZoneInitInputFile(){
		String path= CP.get_workingDirectory();
				path=path+"\\100_zones.txt";
	MultiZoneInitFileReader mzr=new MultiZoneInitFileReader(path,CP);
	int i=mzr.get_nbrOfZonesInFile();
	double [] a=mzr.get_pVTmi(0);
	double [] b=mzr.get_pVTmi(4);	
	
	FileWriter_txt fw2=new FileWriter_txt(CP.get_workingDirectory()+"//Input.txt");
	fw2.writeMatrixToFile(mzr.get_allpVTmi(), false);
	}
	
	public void postProcessKiva(){
//		String path= "y:\\adam2johannes\\mode7-375k-new\\post\\multi_zone_kiva_dump.";
		String path="y:\\mode8Post\\multi_zone_kiva_dump.";
//		String path= CP.get_workingDirectory();
//		path=path+"\\multi_zone_kiva_dump.";
		KivaPostProcessor kpp=new KivaPostProcessor();			
		kpp.multiPostProcess(path,CP,46,7);
		
//		KivaPostProcessReader kpr=new KivaPostProcessReader(path);
//		FileWriter_txt fw2=new FileWriter_txt(CP.get_workingDirectory()+"//KivaCHK.txt");
//		fw2.writeMatrixToFile(kpr.get_data(), false);
		
	}
	
	public void testCrossCorr(){
		String path="c://Documents and Settings//eichmeier//jniWorkspace2//CallMeFromMatlab" +
				"//src//InputFiles//Mode7//Opti//APR_mode7Inp_1_3.txt";
		SimpleTXTFileReader sf=new SimpleTXTFileReader(path);
		double [][] data= sf.get_dataTransposed();
		double [] pExp=data[5];
		double [] pBremo=data[6];
		double xc=MatLibBase.crossCorr0(pExp, pBremo);
		System.out.println("xc: " +xc);
		
		double ac=MatLibBase.crossCorr0(pExp, pExp);
		System.out.println("ac: " +ac);
	}

	public void calcLambda4Comp(){	

		double [] y=new double [CP.SPECIES_FACTROY.get_nbrOfSpecies()];		
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_nC7H16())]=0.0;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_O2())]=0.1327011879;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N2())]=0.7312165251;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO2())]=0.0787026718;		
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H2O())]=0.0343273968;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_iC8H18())]=0.0230522184;	


		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();

		double sum=0;
		for(int j=0;j<y.length;j++)sum=sum+y[j];
		for(int j=0;j<y.length;j++)y[j]=y[j]/sum;

		for(int j=0;j<y.length;j++){			
			ht.put(CP.SPECIES_FACTROY.get_Spez(j), y[j]);
		}			
		GasMixture spec=new GasMixture("CanteraBurned_");
		spec.set_Gasmischung_massenBruch(ht);

		double mGes=8.4536686E-4;
		double mINIT=8.441248E-4;	
		double mDiesel=mGes-mINIT;

		Hashtable<Spezies, Double> compo=new Hashtable<Spezies,Double>();

		compo.put(spec, mINIT/mGes);
		compo.put(CP.SPECIES_FACTROY.get_nC7H16(), mDiesel/mGes);
		GasMixture mix=new GasMixture("mix");
		mix.set_Gasmischung_massenBruch(compo);		

		double lambda0=mix.get_lambda();		
		System.out.println("Lambda GT: "+ mix.get_lambda());

		mDiesel=mDiesel*0.225/0.070794;		
		mGes=mINIT+mDiesel;

		compo.put(spec, mINIT/mGes);
		compo.put(CP.SPECIES_FACTROY.get_nC7H16(), mDiesel/mGes);

		mix.set_Gasmischung_massenBruch(compo);

		double lambda1=mix.get_lambda();		
		System.out.println("Lambda mit mehr Diesel: "+ mix.get_lambda());
		System.out.println("Abweichung in Prozent: " +((lambda0-lambda1)/lambda0*100) );


	}


	public void testDruckausgleichCantera(){

		int a1=CP.SPECIES_FACTROY.get_nbrOfSpecies();

		int anzVol=3;	

		double mi=0.005;
		double p0[]=new double [anzVol];
		double V0[]=new double [anzVol];
		double T0[]=new double [anzVol];
		double m[]=new double [anzVol];
		GasMixture [] specs=new GasMixture[anzVol];
		Zone zones[]=new Zone[anzVol];

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();					
		ht.put(CP.SPECIES_FACTROY.get_CO2(), 1D);		
		specs[0]=new GasMixture("Gas1");
		specs[0].set_Gasmischung_massenBruch(ht);
		p0[0]=1e5;
		T0[0]=300;	
		m[0]=mi;
		V0[0]=m[0]*specs[0].get_R()*T0[0]/p0[0];		

		Hashtable<Spezies, Double> ht2=new Hashtable<Spezies,Double>();					
		ht2.put(CP.SPECIES_FACTROY.get_N2(), 1D);		
		specs[1]=new GasMixture("Gas2");
		specs[1].set_Gasmischung_massenBruch(ht2);
		p0[1]=10e5;
		T0[1]=500;	
		m[1]=mi;
		V0[1]=m[1]*specs[1].get_R()*T0[1]/p0[1];	

		Hashtable<Spezies, Double> ht3=new Hashtable<Spezies,Double>();					
		ht3.put(CP.SPECIES_FACTROY.get_O2(), 1D);		
		specs[2]=new GasMixture("Gas3");
		specs[2].set_Gasmischung_massenBruch(ht3);
		p0[2]=50e5;
		T0[2]=900;	
		m[2]=mi;
		V0[2]=m[2]*specs[2].get_R()*T0[2]/p0[2];	

		for(int i=0;i<anzVol;i++)
			zones[i]=new Zone(CP, p0[i], V0[i], T0[i], m[i], specs[i], false, i);

		CanteraCaller cc=new CanteraCaller(CP,anzVol);

		double dt=1e-5;
		double t=0;
		double tMax=0.005;
		int nbrSteps= (int)(tMax/dt);
		double results[][]=new double [nbrSteps][anzVol*3+3];		
		int step=0;	
		do{	
			double Vges=0;
			for(int i=0;i<anzVol;i++){

				results[step][i*3+1]=zones[i].get_p();
				results[step][i*3+2]=zones[i].get_V();
				results[step][i*3+3]=zones[i].get_T();		
				Vges=Vges+zones[i].get_V();
			}	
			results[step][0]=t;	
			results[step][anzVol*3+1]=Vges;	
			cc.calcTimeStepVec(zones, dt);
			t=t+dt;
			System.out.println("t:"+ t);
			step+=1;
		}while(step<nbrSteps);	

		String [] header=new String [anzVol*3+3];	
		header[0]="t";
		header[anzVol*3+1]="Vges";

		for(int i=0;i<anzVol;i++){
			header[i*3+1]="p_"+i;
			header[i*3+2]="V_"+i;
			header[i*3+3]="T_"+i;
		}

		FileWriter_txt fw2=new FileWriter_txt("DA_nZones.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(results, true);
	}


	public void testDruckausgleichDiffSysZones(){


		int anzVol=3;	

		double mi=0.005;
		double p0[]=new double [anzVol];
		double V0[]=new double [anzVol];
		double T0[]=new double [anzVol];
		double m[]=new double [anzVol];
		GasMixture [] specs=new GasMixture[anzVol];
		Zone zones[]=new Zone[anzVol];

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();					
		ht.put(CP.SPECIES_FACTROY.get_CO2(), 1D);		
		specs[0]=new GasMixture("Gas1");
		specs[0].set_Gasmischung_massenBruch(ht);
		p0[0]=1e5;
		T0[0]=300;	
		m[0]=mi;
		V0[0]=m[0]*specs[0].get_R()*T0[0]/p0[0];		

		Hashtable<Spezies, Double> ht2=new Hashtable<Spezies,Double>();					
		ht2.put(CP.SPECIES_FACTROY.get_N2(), 1D);		
		specs[1]=new GasMixture("Gas2");
		specs[1].set_Gasmischung_massenBruch(ht2);
		p0[1]=10e5;
		T0[1]=500;	
		m[1]=mi;
		V0[1]=m[1]*specs[1].get_R()*T0[1]/p0[1];	

		Hashtable<Spezies, Double> ht3=new Hashtable<Spezies,Double>();					
		ht3.put(CP.SPECIES_FACTROY.get_O2(), 1D);		
		specs[2]=new GasMixture("Gas3");
		specs[2].set_Gasmischung_massenBruch(ht3);
		p0[2]=50e5;
		T0[2]=900;	
		m[2]=mi;
		V0[2]=m[2]*specs[2].get_R()*T0[2]/p0[2];	

		for(int i=0;i<anzVol;i++)
			zones[i]=new Zone(CP, p0[i], V0[i], T0[i], m[i], specs[i], false, i);

		double a= 1e-6;
		DruckAusgleicher da=new DruckAusgleicher(a);		
		da.set_Zones(zones);

		double dt=1e-5;
		double t=0;
		double tMax=0.02;
		int nbrSteps= (int)(tMax/dt);
		double results[][]=new double [nbrSteps][anzVol*3+3];


		//InitialCOnditions
		double [] V_T=new double[anzVol*2];
		for(int i=0;i<anzVol;i++){
			V_T[i*2]=zones[i].get_V();
			V_T[i*2+1]=zones[i].get_T();				
		}

		RungeKutta rk=new RungeKutta();
		rk.setStepSize(dt);
		int step=0;
		do{			
			double U=0;
			double Vges=0;
			for(int i=0;i<anzVol;i++){
				double T=V_T[i*2+1];
				double V=V_T[i*2];
				double p=m[i]*zones[i].get_gasMixtureZone().get_R()*T/V;				
				results[step][i*3+1]=p;
				results[step][i*3+2]=V;
				results[step][i*3+3]=T;		
				U=U+zones[i].get_gasMixtureZone().get_u_mass(T)*m[i];
				Vges=Vges+V;
			}	
			results[step][0]=t;	
			results[step][anzVol*3+1]=Vges;	
			results[step][anzVol*3+2]=U;

			rk.setInitialValueOfX(t);
			t+=dt;
			step+=1;
			rk.setFinalValueOfX(t);			
			rk.setInitialValuesOfY(V_T);
			V_T=rk.cashKarp(da);		
			//V_T=rk.fourthOrder(da);
		}while(step<nbrSteps);

		String [] header=new String [anzVol*3+3];	
		header[0]="t";
		header[anzVol*3+1]="Vges";
		header[anzVol*3+2]="USys";	

		for(int i=0;i<anzVol;i++){
			header[i*3+1]="p_"+i;
			header[i*3+2]="V_"+i;
			header[i*3+3]="T_"+i;
		}

		FileWriter_txt fw2=new FileWriter_txt("DA_nZones.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(results, true);	


	}

	private class DruckAusgleicher implements DiefferetialEqationSys{
		private Zone[] zones;
		private int nbrOfZones;			
		private double [] m;
		private double expCoeff;

		public DruckAusgleicher(double expCoeff){
			this.expCoeff=expCoeff;
		}


		public void set_Zones(Zone [] z){
			this.zones=z;
			nbrOfZones=z.length;
			this.m=new double[nbrOfZones];
			for(int i=0;i<nbrOfZones;i++)m[i]=zones[i].get_m();
		}		


		@Override
		public double[] derivn(double x, double[] V_T) {
			double [] dV =new double[nbrOfZones];
			double [] dT=new double [nbrOfZones];
			double [] p=new double [nbrOfZones];			

			for(int i=0;i<nbrOfZones;i++)
				p[i]=m[i]*zones[i].get_gasMixtureZone().get_R()*V_T[i*2+1]/V_T[i*2];

			dV[0]=expCoeff*(p[0]-p[1]);			
			for(int i=1;i<nbrOfZones-1;i++)
				dV[i]=expCoeff*(p[i]-p[i+1]+p[i]-p[i-1]);			
			dV[nbrOfZones-1]=expCoeff*(p[nbrOfZones-1]-p[nbrOfZones-2]);

			for(int i=0;i<nbrOfZones;i++)
				dT[i]=-p[i]*dV[i]/zones[i].get_gasMixtureZone().get_cv_mass(V_T[i*2+1])/m[i];

			double dV_dT[]=new double [V_T.length];
			for(int i=0;i<nbrOfZones;i++){
				dV_dT[i*2]=dV[i];
				dV_dT[i*2+1]=dT[i];				
			}			
			return dV_dT;
		}		
	}


	public void testDruckausgleich(){		

		int anzVol=3;	

		double m=0.005;
		double p[]=new double [anzVol];
		double V[]=new double [anzVol];
		double T[]=new double [anzVol];
		GasMixture [] specs=new GasMixture[anzVol];

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();					
		ht.put(CP.SPECIES_FACTROY.get_CO2(), 1D);		
		specs[0]=new GasMixture("Gas1");
		specs[0].set_Gasmischung_massenBruch(ht);
		p[0]=1e5;
		T[0]=300;		
		V[0]=m*specs[0].get_R()*T[0]/p[0];		

		Hashtable<Spezies, Double> ht2=new Hashtable<Spezies,Double>();					
		ht2.put(CP.SPECIES_FACTROY.get_N2(), 1D);		
		specs[1]=new GasMixture("Gas2");
		specs[1].set_Gasmischung_massenBruch(ht2);
		p[1]=10e5;
		T[1]=500;		
		V[1]=m*specs[1].get_R()*T[1]/p[1];	

		Hashtable<Spezies, Double> ht3=new Hashtable<Spezies,Double>();					
		ht3.put(CP.SPECIES_FACTROY.get_O2(), 1D);		
		specs[2]=new GasMixture("Gas3");
		specs[2].set_Gasmischung_massenBruch(ht3);
		p[2]=50e5;
		T[2]=900;		
		V[2]=m*specs[2].get_R()*T[2]/p[2];		

		double U0=0, U1=0;
		for(int i=0;i<anzVol;i++)U0=U0+specs[i].get_u_mass(T[i])*m;

		double H0=0, H1=0;
		for(int i=0;i<anzVol;i++)H0=H0+specs[i].get_h_mass(T[i])*m;

		double Vges=0;
		for(int i=0;i<anzVol;i++) Vges=Vges+V[i];



		double pEquil=0;
		for(int i=0;i<anzVol;i++) pEquil=pEquil+p[i]*V[i];
		pEquil=pEquil/Vges;

		double dVges, VgesNeu=Vges;
		int idx=0;
		do{
			double G[]=new double [anzVol];
			double V_old[]=new double [anzVol];
			G[0]=p[0]*V[0]; //p1*V1
			V_old[0]=V[0];
			for(int i=1;i<anzVol;i++){
				V_old[i]=V[i];
				G[i]=G[i-1]+p[i]*V[i];//pi*Vi
			}

			pEquil=G[G.length-1]/Vges;


			double V_new[]=new double [anzVol];		
			for(int i=0;i<anzVol;i++)V_new[i]=V_old[i]*p[i]/pEquil;

			double dV[]=new double [anzVol];	
			for(int i=0;i<anzVol;i++) dV[i]=V_new[i]-V_old[i];		

			dVges=0;	
			for(int i=0;i<anzVol;i++) dVges=dVges+dV[i];
			//System.out.println("dVges="+dVges);		
			System.out.println("Vges-V1-V2-V3="+(Vges-V[0]-V[1]-V[2]));

			for(int i=0;i<anzVol;i++){				
				T[i]=T[i]-p[i]*dV[i]/m/specs[i].get_cv_mass(T[i]);
				p[i]=m*specs[i].get_R()*T[i]/V_new[i];	
				V[i]=V_new[i];				
			}
			VgesNeu=0;	
			for(int i=0;i<anzVol;i++) VgesNeu=VgesNeu+V[i];


			U1=0;
			H1=0;
			for(int i=0;i<anzVol;i++)U1=U1+specs[i].get_u_mass(T[i])*m;				
			for(int i=0;i<anzVol;i++)H1=H1+specs[i].get_h_mass(T[i])*m;	

			idx+=1;
		}while(idx<=55);		

		System.out.println("Vges-V1-V2-V3="+(Vges-VgesNeu));
		System.out.println("delta_Vges="+((Vges-VgesNeu))/Vges*100+"%");
		System.out.println("U0-U1="+(U0-U1));
		System.out.println("H0-H1="+(H0-H1));

		System.out.println("p0V0-mR0T0="+((p[0]*V[0]-m*specs[0].get_R()*T[0]))/p[0]/V[0]*100);
		System.out.println("p0V0-mR0T0="+((p[1]*V[1]-m*specs[1].get_R()*T[1]))/p[1]/V[1]*100);

		System.out.println("dVges="+dVges);
		for(int i=0;i<anzVol;i++) 
			System.out.println("p"+i+"="+(p[i]*1e-5)); 

		for(int i=0;i<anzVol;i++) 
			System.out.println("T"+i+"="+(T[i])); 			

	}

	public void testDruckAusgleichDiffSys(){
		int anzVol=2;	
		double m=0.005;
		double p[]=new double [anzVol];
		double V[]=new double [anzVol];
		double T[]=new double [anzVol];
		double R[]=new double [anzVol];
		double cv[]=new double [anzVol];

		GasMixture [] specs=new GasMixture[anzVol];

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();					
		ht.put(CP.SPECIES_FACTROY.get_CO2(), 1D);		
		specs[0]=new GasMixture("Gas1");
		specs[0].set_Gasmischung_massenBruch(ht);
		p[0]=1e5;
		T[0]=300;
		R[0]=specs[0].get_R();		
		V[0]=m*R[0]*T[0]/p[0];		

		Hashtable<Spezies, Double> ht2=new Hashtable<Spezies,Double>();					
		ht2.put(CP.SPECIES_FACTROY.get_N2(), 1D);		
		specs[1]=new GasMixture("Gas2");
		specs[1].set_Gasmischung_massenBruch(ht2);
		p[1]=10e5;
		T[1]=500;	
		R[1]=specs[1].get_R();
		V[1]=m*R[1]*T[1]/p[1];	

		double Vges=0,VgesNew;
		for(int i=1;i<anzVol;i++) Vges=Vges+V[i];

		double U0=0, U1=0;
		for(int i=0;i<anzVol;i++)U0=U0+specs[i].get_u_mass(T[i])*m;


		double a= 1;
		double dt=1e-4;
		double t=0;
		double tMax=0.25;
		int nbrSteps= (int)(tMax/dt);
		double results[][]=new double [nbrSteps][11];

		String [] header={"t","T0","T1","V0","V1","Vges","p0","p1","USys","Err0","Err1"};


		int step=0;
		do{

			U1=0;
			for(int i=0;i<anzVol;i++)U1=U1+specs[i].get_u_mass(T[i])*m;

			results[step][0]=t;		
			results[step][1]=T[0];
			results[step][2]=T[1];
			results[step][3]=V[0];
			results[step][4]=V[1];
			results[step][5]=V[1]+V[0];
			results[step][6]=p[0];
			results[step][7]=p[1];	
			results[step][8]=U1;
			results[step][9]=(p[0]*V[0]-m*R[0]*T[0])/(p[0]*V[0])*100;
			results[step][10]=(p[1]*V[1]-m*R[1]*T[1])/(p[1]*V[1])*100;



			double cv0=specs[0].get_cv_mass(T[0]);
			double cv1=specs[1].get_cv_mass(T[1]);

			double dt0=-1*p[0]*(p[0]-p[1])*a/m/cv0;
			double dp0=-1*p[0]*(p[0]-p[1])*a/V[0]*(R[0]/cv0+1);

			double dp1=p[1]*(p[0]-p[1])*a/V[1]*(R[1]/cv1+1);
			double dt1=p[1]*(p[0]-p[1])*a/m/cv1;

			T[0]=T[0]+dt0*dt;
			T[1]=T[1]+dt1*dt;

			V[0]=V[0]+(p[0]-p[1])*a*dt;
			V[1]=V[1]-(p[0]-p[1])*a*dt;	

			p[0]=p[0]+dp0*dt;
			p[1]=p[1]+dp1*dt;

			p[0]=m*R[0]*T[0]/V[0];
			p[1]=m*R[1]*T[1]/V[1];

			t+=dt;
			step+=1;

		}while(step<nbrSteps);


		System.out.println("U0-U1="+(U0-U1));

		for(int i=0;i<anzVol;i++) 
			System.out.println("p"+i+"="+(p[i]));

		for(int i=0;i<anzVol;i++) 
			System.out.println("V"+i+"="+(V[i]));

		for(int i=0;i<anzVol;i++) 
			System.out.println("T"+i+"="+(T[i]));


		FileWriter_txt fw2=new FileWriter_txt("Druckausgleich.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(results, true);			

	}


	public void testDruckAusgleich2(){
		int anzVol=2;	
		double m=0.005;
		double p[]=new double [anzVol];
		double V[]=new double [anzVol];
		double T[]=new double [anzVol];
		double R[]=new double [anzVol];
		double cv[]=new double [anzVol];

		GasMixture [] specs=new GasMixture[anzVol];

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();					
		ht.put(CP.SPECIES_FACTROY.get_CO2(), 1D);		
		specs[0]=new GasMixture("Gas1");
		specs[0].set_Gasmischung_massenBruch(ht);
		p[0]=1e5;
		T[0]=300;
		R[0]=specs[0].get_R();		
		V[0]=m*R[0]*T[0]/p[0];		

		Hashtable<Spezies, Double> ht2=new Hashtable<Spezies,Double>();					
		ht2.put(CP.SPECIES_FACTROY.get_N2(), 1D);		
		specs[1]=new GasMixture("Gas2");
		specs[1].set_Gasmischung_massenBruch(ht2);
		p[1]=10e5;
		T[1]=500;	
		R[1]=specs[1].get_R();
		V[1]=m*R[1]*T[1]/p[1];	

		double Vges0=0;
		for(int i=0;i<anzVol;i++) Vges0=Vges0+V[i];

		System.out.println(Vges0-V[1]-V[0]);

		double pEquil0=0;
		for(int i=0;i<anzVol;i++) pEquil0=pEquil0+p[i]*V[i];
		pEquil0=pEquil0/Vges0;


		double U0=0, U1=0;
		for(int i=0;i<anzVol;i++)U0=U0+specs[i].get_u_mass(T[i])*m;



		int nbrSteps= 555555;	

		int step=0;
		double Vges;
		double a=0.000000000001;
		do{
			Vges=0;
			for(int i=0;i<anzVol;i++) Vges=Vges+V[i];
			//System.out.println(Vges-Vges0);

			double pEquil=0;
			for(int i=0;i<anzVol;i++) pEquil=pEquil+p[i]*V[i];
			pEquil=pEquil/Vges;

			double V2[]=new double [anzVol];			
			for(int i=0;i<anzVol;i++) V2[i]=Math.pow((p[i]/pEquil),1/specs[i].get_kappa(T[i]))*V[i];
			double V_new[]=new double [anzVol];
			for(int i=0;i<anzVol;i++)V_new[i]=V2[i];
			//for(int i=0;i<anzVol;i++) dV[i]=V[i]*p[i]/pEquil;
			double dV[]=new double [anzVol];
			//for(int i=0;i<anzVol;i++) dV[i]=V_new[i]-V[i];	
			dV[0]=a*(p[0]-p[1]);
			dV[1]=a*(p[1]-p[0]);

			double sumdV=0;
			for(int i=0;i<anzVol;i++) sumdV=sumdV+dV[i];

			//System.out.println(sumdV);

			for(int i=0;i<anzVol;i++){				
				T[i]=T[i]-p[i]*dV[i]/m/specs[i].get_cv_mass(T[i]);
				V[i]=V[i]+dV[i];
				p[i]=m*specs[i].get_R()*T[i]/V[i];									
			}		

			U1=0;
			for(int i=0;i<anzVol;i++)U1=U1+specs[i].get_u_mass(T[i])*m;

			step+=1;

		}while(step<nbrSteps);


		System.out.println("U0-U1="+((U0-U1)/U0*100)+"%");
		System.out.println("Vges0-Vges="+((Vges0-Vges)/Vges0*100)+"%");

		for(int i=0;i<anzVol;i++) 
			System.out.println("p"+i+"="+(p[i]));

		for(int i=0;i<anzVol;i++) 
			System.out.println("V"+i+"="+(V[i]));

		for(int i=0;i<anzVol;i++) 
			System.out.println("T"+i+"="+(T[i]));
	}



	public void testCantera(){
		System.out.println(System.getProperty("java.library.path"));
		CanteraCaller cc=new CanteraCaller(CP,1);
		//System.out.println(cc.get_getNameOfSpecie(CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())));
		System.out.println(System.getProperty("java.library.path"));

		//System.out.println(cc.get_T(0));


		double [] y=new double [cc.get_NbrOfSpecies()];
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_nC7H16())]=0.0036814;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_O2())]=0.17458;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N2())]=0.74012;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO2())]=0.044454;		
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H2O())]=0.020201;
		y[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_iC8H18())]=0.016964;

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();

		double sum=0;
		for(int j=0;j<y.length;j++) sum=sum+y[j];
		for(int j=0;j<y.length;j++) y[j]=y[j]/sum;
		for(int j=0;j<y.length;j++) ht.put(CP.SPECIES_FACTROY.get_Spez(j), y[j]);


		GasMixture spec=new GasMixture("CanteraBurned_");
		spec.set_Gasmischung_massenBruch(ht);

		double mINIT=0.00096818;			
		double T_init=1000;
		double p_init=2.1169*1e5;
		double V_init=mINIT*spec.get_R()*T_init/p_init;
		System.out.println("V: "+ V_init);

		Zone [] zn=new Zone[1];
		zn[0]=new Zone(CP,p_init, V_init, T_init, 
				mINIT,spec , false,0);

		double dt=1.e-4;
		double t=0;
		double tMax=2*10e-4;
		int nbrOfIterations= (int)(tMax/dt);
		double result [][]=new double [nbrOfIterations][CP.SPECIES_FACTROY.get_nbrOfSpecies()+4];
		for(int i=0;i<nbrOfIterations;i++){			
			zn=cc.calcTimeStepVec(zn, dt);
			System.out.println(cc.get_T(0));

			result[i][0]=i*dt;
			result[i][1]=zn[0].get_p(); 
			result[i][2]=zn[0].get_V();
			result[i][3]=zn[0].get_T();
			double []mi=zn[0].get_mi();
			for(int idx=0;idx<mi.length;idx++)result[i][idx+4]=mi[idx];	

			System.out.println("time: " + i*dt );		
		}	


		String header[]=new String [CP.SPECIES_FACTROY.get_nbrOfSpecies()+4];
		header[0]="t";
		header[1]="p";
		header[2]="V";
		header[3]="T";
		for(int i=0; i<CP.SPECIES_FACTROY.get_nbrOfSpecies();i++) 
			header[i+4]=cc.get_getNameOfSpecie(i);

		FileWriter_txt fw2=new FileWriter_txt("canteraTest.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(result, true);	
		cc.releaseCantera();		
		System.out.println("done 2");	

	}


	public void printMolarMasses(){		
		int nsp=CP.SPECIES_FACTROY.get_nbrOfSpecies();
		double M[]=new double [nsp];


		for(int i=0; i<nsp;i++) 
			M[i]=CP.SPECIES_FACTROY.get_Spez(i).get_M()*1000;


		String header[] =new String[nsp];
		for(int i=0; i<nsp;i++)			
			header[i]= CP.SPECIES_FACTROY.get_Spez(i).get_name();	

		FileWriter_txt fw2=new FileWriter_txt("M.txt");
		fw2.writeTextLineToFile(header, false);
		fw2.writeLineToFile(M, true);

	}


	public void testCantera3Zone(){
		double t0=System.currentTimeMillis();
		System.out.println(System.getProperty("java.library.path"));
		int nbrOfZones=4;
		CanteraCaller cc=new CanteraCaller(CP,1);
		//System.out.println(cc.get_getNameOfSpecie(CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())));
		System.out.println(System.getProperty("java.library.path"));

		double m=0.005;	
		Zone [] zn=new Zone[nbrOfZones];		
		double [][] compVec = new double [nbrOfZones][cc.get_NbrOfSpecies()];
		double p[]=new double [nbrOfZones];
		double T[]=new double [nbrOfZones];

		p[0]=7e5;
		p[1]=10e5;
		p[2]=5e5;		

		T[0]=300;
		T[1]=500;
		T[2]=900;
		T[3]=1151;


		double [] y0=new double [cc.get_NbrOfSpecies()];
		y0[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO2())]=1.0;
		compVec[0]=y0;


		double [] y1=new double [cc.get_NbrOfSpecies()];
		y1[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N2())]=1.0;
		compVec[1]=y1;

		double [] y2=new double [cc.get_NbrOfSpecies()];
		y2[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_nC7H16())]=0.0036814;
		y2[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_O2())]=0.17458;
		y2[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N2())]=0.74012;
		y2[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO2())]=0.044454;		
		y2[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H2O())]=0.020201;
		y2[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_iC8H18())]=0.016964;
		//y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_soot())]=1;		
		compVec[2]=y2;
		compVec[3]=y2;
		


		for(int i=0;i<nbrOfZones;i++){
			//compVec[i]=y2;
			p[i]=p[2];
			//T[i]=T[2];
		}



		//		Hashtable<Spezies, Double> ht0=new Hashtable<Spezies,Double>();	
		//		double a=1.0/7.0;
		//		a=1.0;
		//		ht0.put(CP.SPEZIES_FABRIK.get_spezCO2(), a);
		////		ht0.put(CP.SPEZIES_FABRIK.get_spezO2(), a);
		////		ht0.put(CP.SPEZIES_FABRIK.get_spezN2(), a);
		////		ht0.put(CP.SPEZIES_FABRIK.get_spezH2O(), 1-3*a);
		//
		//		GasGemisch spec0=new GasGemisch("Gas2");
		//		spec0.set_Gasmischung_massenBruch(ht0);
		//		double p0=7e5;
		//		double T0=300;		
		//		double V0=m*spec0.get_R()*T0/p0;
		//		zn[0]=new Zone(CP,p0, V0, T0, 
		//				m,spec0 , false,0);
		//		
		//		Hashtable<Spezies, Double> ht1=new Hashtable<Spezies,Double>();					
		//		ht1.put(CP.SPEZIES_FABRIK.get_spezN2(), 1D);		
		//		GasGemisch spec1=new GasGemisch("Gas3");
		//		spec1.set_Gasmischung_massenBruch(ht1);
		//		double p1=10e5;
		//		double T1=500;		
		//		double V1=m*spec1.get_R()*T1/p1;
		//		zn[1]=new Zone(CP,p1, V1, T1, 
		//				m,spec1 , false,1);			


		for(int i=0;i<nbrOfZones;i++){

			double [] y=new double [cc.get_NbrOfSpecies()];	
			y=compVec[i];
			double sum=0;
			for(int j=0;j<y.length;j++) sum=sum+y[j];
			for(int j=0;j<y.length;j++) y[j]=y[j]/sum;

			Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
			for(int j=0;j<y.length;j++) ht.put(CP.SPECIES_FACTROY.get_Spez(j), y[j]);

			GasMixture spec=new GasMixture("CanteraBurned_"+i);
			spec.set_Gasmixture_moleFracs(ht);

			double T_=T[i];
			double p_=p[i];
			double m_=m/nbrOfZones;
			double V=m_*spec.get_R()*T_/p_;

			zn[i]=new Zone(CP,p_, V, T_,m_,spec , false,i);

		}					
		System.out.println("PHI zn2: "+ 1/zn[2].get_gasMixtureZone().get_lambda());
		System.out.println("Lambda zn2: "+zn[2].get_gasMixtureZone().get_lambda());
		System.out.println("Lambda zn3: "+zn[3].get_gasMixtureZone().get_lambda());

		double dt=0.5e-5;	
		double tMax=0.01;
		dt=tMax/1000;
		dt=0.01e-4;
		double tEnd=tMax-dt;
		int nbrOfIterations= (int)(tMax/dt);
		//nbrOfIterations=5;

		//cc.test();
		int totalSpecNbr=nbrOfZones*CP.SPECIES_FACTROY.get_nbrOfSpecies();
		double result [][]=new double [nbrOfIterations][nbrOfZones*3+1+2+totalSpecNbr];
		double massen[][]=new double [nbrOfIterations][nbrOfZones+4];
		for(int i=0;i<nbrOfIterations;i++){	
			System.out.println("still to go: " + (nbrOfIterations-i));
			result[i][0]=i*dt;
			massen[i][0]=i*dt;
			for(int z=0;z<zn.length;z++){
				result[i][z*3+1]=zn[z].get_p(); 
				result[i][z*3+2]=zn[z].get_V();
				result[i][z*3+3]=zn[z].get_T();	
				result[i][nbrOfZones*3+totalSpecNbr+1]=result[i][nbrOfZones*3+totalSpecNbr+1]+zn[z].get_V();				
				result[i][nbrOfZones*3+totalSpecNbr+1+1]=result[i][nbrOfZones*3+totalSpecNbr+1+1]
						+zn[z].get_gasMixtureZone().get_u_mass(zn[z].get_T())*zn[z].get_m();


				massen[i][z+1]=zn[z].get_m();
				massen[i][zn.length+1]=massen[i][zn.length+1]+zn[z].get_m();				
			}
			massen[i][zn.length+2]=zn[2].get_gasMixtureZone().get_lambda();
			massen[i][zn.length+3]=zn[3].get_gasMixtureZone().get_lambda();
			for(int idxZ=0;idxZ<nbrOfZones;idxZ++){
				double []mf=zn[idxZ].get_MassFractions();
				for(int idx=0;idx<mf.length;idx++)result[i][idx+nbrOfZones*3+1+idxZ*CP.SPECIES_FACTROY.get_nbrOfSpecies()]=mf[idx];
			}			

			//zn=cc.calcTimeStep(zn,tEnd );	
			for(int j=0;j<zn.length;j++){
				Zone [] znT=new Zone[1];
				znT[0]=zn[j];
				znT=cc.calcTimeStep(znT, dt);
				zn[j]=znT[0];
			}
			zn=APR_Cantera.equilibrate(zn, 0, 5);
		}	
		double t1=System.currentTimeMillis();
		System.out.println("t1-t0=" + (t1-t0));
		String header[]=new String [nbrOfZones*3+1+2+totalSpecNbr];
		header[0]="t";
		for(int z=0;z<zn.length;z++){
			header[z*3+1]="p_"+(z+1);
			header[z*3+2]="V_"+(z+1);
			header[z*3+3]="T_"+(z+1);
		}

		for(int idxZ=0;idxZ<nbrOfZones;idxZ++)
			for(int i=0; i<CP.SPECIES_FACTROY.get_nbrOfSpecies();i++) 
				header[nbrOfZones*3+1+idxZ*CP.SPECIES_FACTROY.get_nbrOfSpecies()+i]=  cc.get_getNameOfSpecie(i)+ "_"+(idxZ+1);		

		header[nbrOfZones*3+totalSpecNbr+1]="VGes";
		header[nbrOfZones*3+totalSpecNbr+2]="UGes";
		File f=new File("canteraTest_Z"+nbrOfZones+".txt");
		String s=f.getAbsolutePath();
		FileWriter_txt fw2=new FileWriter_txt(s);
		fw2.writeTextLineToFile(header, false);
		fw2.writeMatrixToFile(result, true);

		String []header3=new String [zn.length+2];
		header3[0]="t";
		for(int z=0;z<zn.length;z++){
			header3[z+1]="m_"+z;
		}
		header3[zn.length+1]="mGes";
		FileWriter_txt fw3=new FileWriter_txt("canteraTest_m.txt");
		fw3.writeTextLineToFile(header3, false);
		fw3.writeMatrixToFile(massen, true);

		//cc.releaseCantera();		
		System.out.println("done 2");	
	}


	public void test_BremoCanteraSpecies(){
		CanteraCaller cc=new CanteraCaller(CP,1);
		SpeciesFactory sf=CP.SPECIES_FACTROY;
		double TMin=300;
		double TMax=3000;
		int nbrIterations=27;
		double TIntervall=(TMax-TMin)/nbrIterations;
		double p=101325;
		double T;
		double h_Bremo, u_Bremo;
		double h_Cantera, u_Cantera;
		double erg[][]=new double [nbrIterations][cc.get_NbrOfSpecies()+1];
		double erg2[][]=new double [nbrIterations][cc.get_NbrOfSpecies()+1];

		for(int i=0; i<cc.get_NbrOfSpecies();i++){	
			System.out.println(i);
			for(int k=0;k<nbrIterations;k++){				
				T=TMin+k*TIntervall;
				h_Bremo=sf.get_Spez(i).get_h_mol(T);	
				u_Bremo=sf.get_Spez(i).get_u_mol(T);
				double pVTX[]=new double[cc.get_NbrOfSpecies()+3];
				pVTX[0]=p;
				pVTX[1]=0.1;
				pVTX[2]=T;
				pVTX[i+3]=1;
				cc.set_StatepVTX(pVTX, 0);			
				h_Cantera=cc.get_h_mol_Zone(0);		
				u_Cantera=cc.get_u_mol_Zone(0);			
				erg[k][0]=T;
				erg[k][i+1]=(h_Cantera-h_Bremo)/h_Cantera*100;	
				erg2[k][0]=T;
				erg2[k][i+1]=(u_Cantera-u_Bremo)/u_Cantera*100;	
			}		
		}



		String header[]=new String [cc.get_NbrOfSpecies()+1];
		header[0]="T";
		for(int i=0;i<cc.get_NbrOfSpecies();i++)
			header[i+1]=cc.get_getNameOfSpecie(i);

		String name ="BremoCanteraEnthalpyCheck.txt";
		FileWriter_txt txtFile=new FileWriter_txt(name);		
		txtFile.writeTextLineToFile(header, false);
		txtFile.writeMatrixToFile(erg, true);	

		FileWriter_txt txtFile2=new FileWriter_txt("BremoCanteraInEnergyCheck.txt");
		txtFile2.writeTextLineToFile(header, false);
		txtFile2.writeMatrixToFile(erg2, true);		

	}

	public void test_BremoCanteraSpeciesGasMix(){
		CanteraCaller cc=new CanteraCaller(CP,1);
		SpeciesFactory sf=CP.SPECIES_FACTROY;
		double TMin=300;
		double TMax=3000;
		int nbrIterations=27;
		double TIntervall=(TMax-TMin)/nbrIterations;
		double p=101325;
		double T;
		double h_Bremo, u_Bremo;
		double h_Cantera, u_Cantera;
		double erg[][]=new double [nbrIterations][3];	

		Hashtable<Spezies,Double> molFrac=new Hashtable<Spezies, Double>();
		int a1=34, a2=5,a3=15,a4=25;
		molFrac.put(sf.get_Spez(a1), 0.25);
		molFrac.put(sf.get_Spez(a2), 0.25);
		molFrac.put(sf.get_Spez(a3), 0.25);
		molFrac.put(sf.get_Spez(a4), 0.25);

		GasMixture gg=new GasMixture("gg");
		gg.set_Gasmixture_moleFracs(molFrac);

		double pVTX[]=new double[cc.get_NbrOfSpecies()+3];
		pVTX[0]=p;
		pVTX[1]=0.1;
		pVTX[3+a1]=0.25;
		pVTX[3+a2]=0.25;
		pVTX[3+a3]=0.25;
		pVTX[3+a4]=0.25;


		for(int k=0;k<nbrIterations;k++){				
			T=TMin+k*TIntervall;
			h_Bremo=gg.get_h_mol(T);	
			u_Bremo=gg.get_u_mol(T);		
			pVTX[2]=T;			
			cc.set_StatepVTX(pVTX, 0);			
			h_Cantera=cc.get_h_mol_Zone(0);		
			u_Cantera=cc.get_u_mol_Zone(0);			
			erg[k][0]=T;
			erg[k][1]=(h_Cantera-h_Bremo)/h_Cantera*100;				
			erg[k][2]=(u_Cantera-u_Bremo)/u_Cantera*100;	
		}		




		String header[]=new String [7];
		header[0]="T";
		header[1]="del_h [%]";
		header[2]="del_u [%]";	
		header[3]=cc.get_getNameOfSpecie(a1);
		header[4]=cc.get_getNameOfSpecie(a2);
		header[5]=cc.get_getNameOfSpecie(a3);
		header[6]=cc.get_getNameOfSpecie(a4);

		String name ="BremoCanteraGasMixCheck.txt";
		FileWriter_txt txtFile=new FileWriter_txt(name);		
		txtFile.writeTextLineToFile(header, false);
		txtFile.writeMatrixToFile(erg, true);		


	}


	public void test_BremoCanteraSpecies2(){
		CanteraCaller cc=new CanteraCaller(CP,1);
		SpeciesFactory sf=CP.SPECIES_FACTROY;
		double TMin=300;
		double TMax=3000;
		int nbrIterations=27;
		double TIntervall=(TMax-TMin)/nbrIterations;
		double p=101325;
		double T;
		double h_Bremo;
		double h_Cantera;
		double erg[][]=new double [nbrIterations][3];		

		for(int i=0; i<cc.get_NbrOfSpecies();i++){		
			for(int k=0;k<nbrIterations;k++){				
				T=TMin+k*TIntervall;
				h_Bremo=sf.get_Spez(i).get_h_mol(T);			
				double pVTX[]=new double[cc.get_NbrOfSpecies()+3];
				pVTX[0]=p;
				pVTX[1]=0.1;
				pVTX[2]=T;
				pVTX[i+3]=1;
				cc.set_StatepVTX(pVTX,0);				
				h_Cantera=cc.get_h_mol_Zone(0);				
				erg[k][0]=T;
				erg[k][1]=h_Cantera;
				erg[k][2]=h_Bremo;
			}	

			String name ="BremoCanteraEnthalpyCheck_" +
					cc.get_getNameOfSpecie(i)+ ".txt";
			FileWriter_txt txtFile=new FileWriter_txt(CP.get_workingDirectory()+"\\"+name);			
			String header[]={"T", "Cantera", "Bremo"};
			txtFile.writeTextLineToFile(header, false);
			txtFile.writeMatrixToFile(erg, true);				
		}		

	}	



	public void test_Motor(){
		Motor_reciprocatingPiston m= (Motor_reciprocatingPiston) CP.MOTOR;
		FileWriter_txt txtFile=new FileWriter_txt(CP.get_workingDirectory()+"//motor.txt");
		String [] header={"KW", "t", "s","s'","s''","dS_dt","d2S_d2t","V","dV","dV_dKW","V-Vc"};
		txtFile.writeTextLineToFile(header, false);
		double [][] erg=new double [3601][11];
		double t=0;
		for(int KWi=0;KWi<=3600;KWi++){
			int KW=KWi/10;
			t=CP.convert_KW2SEC(KW);
			erg[KW][0]=KW;
			erg[KW][1]=t;
			erg[KW][2]=m.get_S(t);
			erg[KW][3]=m.get_dS_dKW(t);
			erg[KW][4]=m.get_d2S_dKW(t);	
			erg[KW][5]=m.get_dS(t);
			erg[KW][6]=m.get_d2S(t);
			erg[KW][7]=m.get_V(t);
			erg[KW][8]=m.get_dV(t);
			erg[KW][9]=m.get_dV_dKW(t);
			erg[KW][10]=m.get_V(t)-m.get_Kompressionsvolumen();
		}			
		txtFile.writeMatrixToFile(erg, true);		
	}

	//	public static void test_Vektor(){
	//		double v []=new double [15];
	//		for(int i=0; i<15;i++) v[i]=i;
	//		
	//		double a[];
	//		
	//		a=MatLibBase.normierVec(v);
	//		
	//		Integrator inti=new Integrator();
	//		double c [];
	//		c=inti.get_IntegralVerlauf(1, v);
	//	
	//		 double [][] matrix=new double [3][];
	//		 
	//		 matrix[0]=v;
	//		 matrix[1]=a;
	//		 matrix[2]=c;
	//		 
	//		
	//
	//		FileWriter_txt txtFile = new FileWriter_txt("vektor");
	//		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);			
	//		
	//	}
	//	
	//	
	//	
	//	
	public  void test_get_lambda(){

		//		Spezies krst=KoeffizientenSpeziesFabrik.get_spezDiesel();
		//Spezies krst= CP.SPEZIES_FABRIK.get_spezDiesel();
		//CP.SPEZIES_FABRIK.integrierMich(krst);
		//		Spezies krst=KoeffizientenSpeziesFabrik.get_spezH2();
		Spezies krst= CP.SPECIES_FACTROY.get_nC7H16();
		Spezies luft=CP.SPECIES_FACTROY.get_spezLuft_trocken();
		Spezies wasser=CP.SPECIES_FACTROY.get_H2O();

		double lambda=0.4;

		double lst=krst.get_Lst();	
		double mk=1; //[kg]
		double mL_tr=lambda*mk*lst;
		double mW=mL_tr*0.1;
		double m_ges=mk+mL_tr+mW; 	
		double m_gesTrocken=mk+mL_tr;


		//Gemisch erzeugen
		//feucht
		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(luft, mL_tr/m_ges);	
		gemisch_MassenbruchHash.put(wasser, mW/m_ges);	
		GasMixture gemisch=new GasMixture("gg");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
		//trocken
		Hashtable<Spezies,Double> gemisch_MassenbruchHashTr=new Hashtable<Spezies,Double>();		
		gemisch_MassenbruchHashTr.put(krst, mk/m_gesTrocken);
		gemisch_MassenbruchHashTr.put(luft, mL_tr/m_gesTrocken);	
		GasMixture gemischTr=new GasMixture("gg");
		gemischTr.set_Gasmischung_massenBruch(gemisch_MassenbruchHashTr);
		
		
		//Verbrennen
		ChemEquilibriumSolver gg=CP.OHC_SOLVER;
		GasMixture abgas=new GasMixture("Abgas");			
		abgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(1e5, 1000, gemisch)); 
		GasMixture abgasTr=new GasMixture("Abgas");			
		abgasTr.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(1e5, 1000, gemischTr)); 

		System.out.println("Lambda Vorgabe: "+lambda);
		System.out.println("lambda feucht: "+gemisch.get_lambda());
		System.out.println("lambda feucht (aus Abgas):" +abgas.get_lambda());
		System.out.println("lambda trocken: "+gemischTr.get_lambda());
		System.out.println("lambda trocken (aus Abgas):" +abgasTr.get_lambda());
		System.out.println("==============");
		System.out.println("jetzt mit AGR");
		System.out.println("==============");
		double mAGR=0.4*m_ges;
		m_ges=m_ges+mAGR;
		Hashtable<Spezies,Double> gemisch_MassenbruchHash2=new Hashtable<Spezies,Double>();
		gemisch_MassenbruchHash2.put(krst, mk/m_ges);
		gemisch_MassenbruchHash2.put(luft, mL_tr/m_ges);	
		gemisch_MassenbruchHash2.put(wasser, mW/m_ges);	
		gemisch_MassenbruchHash2.put(abgas, mAGR/m_ges);	
		GasMixture gemisch2=new GasMixture("gmisch2");
		gemisch2.set_Gasmischung_massenBruch(gemisch_MassenbruchHash2);
		
		//Verbrennen
		GasMixture abgas2=new GasMixture("Abgas");	
		abgas2.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(1e5, 1000, gemisch2)); 

		System.out.println("Lambda Vorgabe: "+lambda);
		System.out.println("lambda: "+gemisch2.get_lambda());
		System.out.println("lambda (aus Abgas):" +abgas2.get_lambda());	

		System.out.println("fertig");			

	}	

	//	
	//	
	public void testKrstProps2(){		
		String krstFlag="tetradecan";
		FuelProperties krstProp =new FuelProperties(krstFlag);

		double erg [][]=new double[71][10];
		FileWriter_txt txtFile=new FileWriter_txt("krst_props.txt");
		String [] header={"t","L","rho","pS","cpl","cpd","waermeLeit","dynVis","waermeLeitLuft","dynVisLuft"};
		txtFile.writeTextLineToFile(header, false);

		double T=300;
		double deltaT=10;
		for(int i=0;i<71;i++){			
			erg [i][0]=T;
			erg [i][1]=krstProp.get_L(T);
			erg [i][2]=krstProp.get_rhoK(T);
			erg [i][3]=krstProp.get_pS(T);
			erg [i][4]=krstProp.get_cpl(T);
			erg [i][5]=krstProp.get_cp(T);
			erg [i][6]=krstProp.get_waermeLeit(T);
			erg [i][7]=krstProp.get_dynVis_krst(T);	
			erg [i][8]=krstProp.get_waermeLeitLuft(T);
			erg [i][9]=krstProp.get_dynVisLuft(T);	
			T=T+deltaT;
		}	
		txtFile.writeMatrixToFile(erg, true);
	}

	public void testSpeziesProp(){			

		Spezies spez=CP.SPECIES_FACTROY.get_spezC2H5OH();
		double T_start=300;
		double T=T_start;
		double T_End=3000;		
		double deltaT=100;
		int anzT= (int) ((T_End-T_start)/deltaT)+1;	

		double erg [][]=new double[anzT][4];
		FileWriter_txt txtFile=new FileWriter_txt(CP.get_workingDirectory()+"//spezProps.txt");
		String [] header={"T","cp_mol","h_h298_mol","h_mol"};
		txtFile.writeTextLineToFile(header, false);

		for(int i=0;i<anzT;i++){			
			erg [i][0]=T;
			erg [i][1]=spez.get_cp_mol(T);	
			erg [i][2]=spez.get_h_h298_mol(T);	
			erg [i][3]=spez.get_h_mol(T);	
			T=T+deltaT;
		}	
		txtFile.writeMatrixToFile(erg, true);
	}


	public  void testVerdampfung(){

		String krstFlag="tetradecan";
		FuelProperties krstProp =new FuelProperties(krstFlag);
		Evaporation verdampf=new Evaporation(krstProp);
		//		Verdampfung verdampf=new Verdampfung(krstProp);
		double []m_D_T=new double [3];
		double []m_D_T_0=new double [3];
		double p=50e5;
		double T=693;
		double V=1;
		double m=p*V/PhysConst.get_R_Luft()/T;

		Spezies luft=this.CP.SPECIES_FACTROY.get_spezLuft_trocken();
		Zone zn =new Zone(CP,p,V,T,m,luft,false,0);	
		double pInj=1200*1e5;
		double deltaP=pInj-p;
		double rhoZyl_0=p/zn.get_T()/zn.get_gasMixtureZone().get_R();

		double T0=293;
		double rhoK_fl=krstProp.get_rhoK(T);
		double viskositaet_krst=krstProp.get_dynVis_krst(T0);
		double kin_viskositaet_krst=viskositaet_krst/rhoK_fl;

		double smd=0.5*6156e-6*Math.pow(kin_viskositaet_krst,0.385)*Math.pow(rhoK_fl,0.737)*
				Math.pow(rhoZyl_0,0.06)*Math.pow(deltaP/1000,-0.54);
		smd=5.544838362252949E-6;

		double D0=smd;
		double V0=Math.PI*D0*D0*D0/6;
		double m0=krstProp.get_rhoK(T0)*V0;
		m_D_T[0]=m0;
		m_D_T[1]=D0;
		m_D_T[2]=T0;
		m_D_T_0[0]=m0;
		m_D_T_0[1]=D0;
		m_D_T_0[2]=1;
		double v_relativ=6.7;

		double erg []=new double[m_D_T.length+1];
		FileWriter_txt txtFile=new FileWriter_txt("m_D_T.txt");
		String [] header={"t","m/m0","D/D0","T"};
		txtFile.writeTextLineToFile(header, false);

		for(int t=0;t<600;t++){
			double delta_time=1/1e6d;
			double time=delta_time+t*delta_time;
			double a=time-0.048e-3;
			System.out.println(a);
			if(a>=0)
				System.out.println("Jetzt aber");

			System.out.println("time:" +time);
			double []temp=verdampf.get_m_D_T(zn,0,delta_time,v_relativ,m_D_T);
			m_D_T[0]=temp[0];
			m_D_T[1]=temp[1];
			m_D_T[2]=temp[2];

			erg[0]=time*1e3;
			for(int i=0;i<m_D_T.length;i++)	
				erg[1+i]=m_D_T[i]/m_D_T_0[i];			
			txtFile.writeLineToFile(erg,true);
		}		
	}
	//
	//	public static void calc_cp_Dilution(){		
	//		
	//		
	//		CasePara cp=Bremo.get_casePara(); //wenn die funktion mal wo anders stehen soll.....
	//		double T_Bezug=1700;
	//		//Daten aus 20110617_VergleichVerduennungAGR.xls
	//		double m_Otto=93.1e-6;
	//		double m_Diesel=4.33E-06;
	//		double T_feuchtMessung=18.1+273.12;
	//		double phi=14.4/100; //relative Luftfeuchte
	//
	////////////////////////////////////////////////////////////		
	//////////////////Verduennung mit Luft//////////////////////		
	////////////////////////////////////////////////////////////
	//
	//		double []mLuft={3549.053E-6,	3744.410E-6,	4018.985E-6,	
	//				4309.772E-6,	4497.438E-6,	4772.626E-6,	5125.771E-6}; //feuchte Luftmasse pro ASP
	//		double []p_feuchtMessung={2248.000E2,	2362.000E2,	
	//				2500.000E2,	2666.000E2,	2769.000E2,	2914.000E2,	3111.000E2}; //Ladedruck in Pa	
	//		double []mAGRex={43.811E-6,	34.723E-6,	31.756E-6,	27.389E-6,	19.534E-6,	22.736E-6,	28.496E-6};	
	//
	//
	//////////////////////////////////////////////////////////////		
	////////////////Verduennung mit AGR mit Lambda=ct/////////////		
	//////////////////////////////////////////////////////////////
	////		double []mLuft={3560.053E-6,	3546.717E-6,	3489.698E-6,	3546.978E-6,	3505.648E-6,
	////				3527.381E-6,	3505.386E-6}; //feuchte Luftmasse pro ASP
	////		double []p_feuchtMessung={2250.000E2,	2321.000E2,	2415.000E2,	2571.000E2,
	////				2704.000E2,	2893.000E2,	3069.000E2}; //Ladedruck in Pa	
	////		double []mAGRex={24.720E-6,	185.112E-6,	390.356E-6,	632.457E-6,	884.522E-6,	1195.913E-6,1527.627E-6};	
	//
	//////////////////////////////////////////////////////////////
	////////////////Ersatz von Luft durch  AGR ////////////////		
	//////////////////////////////////////////////////////////////		
	////	double []mLuft={3556.898E-6,3389.813E-6,3249.131E-6,3032.347E-6,
	////			2901.336E-6,2701.077E-6}; //feuchte Luftmasse pro ASP
	////	double []p_feuchtMessung={2246.0E2,	2246.0E2,	2246.0E2,	2246.0E2,
	////			2246.0E2,	2246.0E2}; //Druck am Gaszaheler in Pa
	////	double []mAGRex={44.144E-6,182.171E-6,363.495E-6,533.246E-6,
	////			733.545E-6,	907.850E-6};		
	//		
	//		
	//		
	//		double cp_mass[]=new double[mLuft.length];
	//		double kappa[]=new double[mLuft.length];
	//
	//		double mW;
	//		double mKrst=m_Otto+m_Diesel; 
	//		GasGemisch krst = null,abgas=null;
	//		for(int idx=0; idx<mLuft.length;idx++){
	//
	//			Spezies otto=SpeziesFabrik.get_spezRON_95();			
	//			Spezies diesel=SpeziesFabrik.get_spezDiesel();
	//			Hashtable<Spezies,Double>krstGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
	//			krstGemisch_MassenBruchHash.put(otto, m_Otto/mKrst);			
	//			krstGemisch_MassenBruchHash.put(diesel, m_Diesel/mKrst);
	//			krst=new GasGemisch("krs");
	//			krst.set_Gasmischung_massenBruch(krstGemisch_MassenBruchHash);
	//
	//
	//
	//			double pws=PhysKonst.get_saettigunsDampfdruck_H2O(T_feuchtMessung);			
	//			//Wasserbeladung
	//			double x=PhysKonst.get_R_Luft()/PhysKonst.get_R_H2O()*(pws/(p_feuchtMessung[idx]/phi-pws));
	//			mW=mLuft[idx]*(x/(1+x));
	//			double mLuft_tr=mLuft[idx]-mW; //trockene Luftmasse pro ASP
	//
	//			double mFG=mLuft_tr+mW+mKrst;	//gesamte Masse des Verbrennenden Gases
	//			Hashtable<Spezies,Double>frischGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
	//			//trockene Luft
	//			frischGemisch_MassenBruchHash.put(SpeziesFabrik.get_spezLuft_trocken(),mLuft_tr/mFG);
	//			//Wasser
	//			frischGemisch_MassenBruchHash.put(SpeziesFabrik.get_spezH2O(),mW/mFG);
	//			//Kraftstoff
	//			frischGemisch_MassenBruchHash.put(krst,mKrst/mFG);
	//
	//			//Erstellen der Frischgemischspezies
	//			GasGemisch frischGemsich=new GasGemisch("Frischgemisch");
	//			frischGemsich.set_Gasmischung_massenBruch(frischGemisch_MassenBruchHash);
	//
	//
	//			//Bestimmen der AGR-Zusammensetzung --> das Hinzufuegen von AGR hat keinen Einfluss auf die 
	//			//AGR-Zusammensetzung da die Anzahl der c/h/o-Atome gleich bleibt und nur vom Frischgemisch abhaengt
	//			GleichGewichtsRechner gg=GleichGewichtsRechner.get_Instance(cp); 
	//			abgas=new GasGemisch("Abgas");			
	//			abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(1e5, 1000, frischGemsich)); //T=1000K<T_Freeze
	//
	//			//Bestimmung der Verbrennungsluftzusammensetzung		
	//			double mAGRin=0; //wird nicht berücksichtigt
	//			double mAGR=mAGRex[idx]+mAGRin;
	//			double mGes=mLuft_tr+mW+mAGR+mKrst; //gesamte Masse im Zylinder
	//			Hashtable<Spezies,Double>gemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
	//			gemisch_MassenBruchHash.put(abgas, mAGR/mGes);
	//			gemisch_MassenBruchHash.put(SpeziesFabrik.get_spezLuft_trocken(),mLuft_tr/mGes);		
	//			gemisch_MassenBruchHash.put(SpeziesFabrik.get_spezH2O(),mW/mGes);
	//			gemisch_MassenBruchHash.put(krst,mKrst/mGes);
	//			GasGemisch gemisch=new GasGemisch("Verbrennungsluft");
	//			gemisch.set_Gasmischung_massenBruch(gemisch_MassenBruchHash);			
	//
	//			cp_mass[idx]=gemisch.get_cp_mass(T_Bezug);		
	//			kappa[idx]=gemisch.get_kappa(T_Bezug);
	//			
	//			System.out.println("cp Abgas: " +abgas.get_cp_mass(T_Bezug));
	//			System.out.println("cp Gemsich: " +gemisch.get_cp_mass(T_Bezug));
	//		}
	//
	//
	//		FileWriter_txt txtFile = new FileWriter_txt("cpDilution.txt");
	//		txtFile.writeLineToFile(cp_mass, true);	
	//		txtFile.writeLineToFile(kappa, true);	
	//		System.out.println(SpeziesFabrik.get_spezLuft_trocken().get_kappa(T_Bezug));
	//		System.out.println("cp Kraftstoff: "+krst.get_cp_mass(T_Bezug));
	//		
	//		System.out.println("h Otto: "+SpeziesFabrik.get_spezRON_95().get_h_mass(T_Bezug));
	//		System.out.println("cp Otto: "+SpeziesFabrik.get_spezRON_95().get_cp_mass(T_Bezug));
	//		
	//		System.out.println("h Diesel: "+SpeziesFabrik.get_spezDiesel().get_h_mass(T_Bezug));	
	//		System.out.println("cp Diesel: "+SpeziesFabrik.get_spezDiesel().get_cp_mass(T_Bezug));
	//		
	//		System.out.println("cp H2O: " +SpeziesFabrik.get_spezH2O().get_cp_mass(T_Bezug));
	//		System.out.println("cp CO2: " +SpeziesFabrik.get_spezCO2().get_cp_mass(T_Bezug));
	//
	//	}
	//
	//	
	//	
	//	
	//	
	//
	//	public static void test_Xialings_HiroYasu(){		
	//
	//		//		Spray_Hiroyasu spray=new Spray_Hiroyasu(0.05,100,0.001,0.25,10,20);
	//		//
	//		//		spray.sayHello();
	//		//		double s=spray.get_s(1, 10,0.8);
	//		//		System.out.println("der Wert von s beträgt :" + s);
	//
	//		System.out.println("fertig");		
	//	}
	//
	//	public static void test_neueSpezies(){
	//		Spezies test=SpeziesFabrik.get_spezCO2();
	//		System.out.println("CO2 wurde erzeugt und die Anzahl der Spezies ist :"+
	//				Spezies.get_NmbrOfAllSpez());
	//		System.out.println("Index von CO2: " + test.get_index());
	//
	//		Spezies test2=SpeziesFabrik.get_spezDiesel();
	//		System.out.println("Diesel wurde erzeugt und die Anzahl der Spezies ist :"+
	//				Spezies.get_NmbrOfAllSpez());
	//		System.out.println("Der Name von Spezies 1 ist: "
	//				+Spezies.get_Spez(1).get_name());
	//
	//
	//
	//		Spezies test3=SpeziesFabrik.get_spezCO2();
	//		System.out.println("Index von " + test3.get_name() + " ist: " + test3.get_index());
	//
	//		System.out.println("Der Name von Spezies 4 ist: "
	//				+Spezies.get_Spez(4).get_name());
	//
	//	}
	//
	//
	//
	//	public  static void test_HashAdd(){	
	//		Spezies CO2=SpeziesFabrik.get_spezCO2();
	//		Spezies CO2_2=CO2;
	//		Spezies CO2_3=SpeziesFabrik.get_spezCO2();	
	//		Spezies O2=SpeziesFabrik.get_spezO2();		
	//
	//		Hashtable<Spezies, Double> ht1 = new Hashtable<Spezies, Double>(15);
	//		ht1.put(CO2, 0.1); 
	//
	//		System.out.println("HashCode von CO2: " + CO2.hashCode());
	//		System.out.println("HashCode von CO2_2: " + CO2_2.hashCode());
	//
	//		Hashtable<Spezies, Double> ht2 = new Hashtable<Spezies, Double>(15);
	//		ht2.put(CO2_2, 1D);
	//
	//		System.out.println( ht2.containsKey (CO2));
	//
	//
	//		Hashtable<Spezies, Double> ht3=ht1;
	//		ht1=LittleHelpers.addiereHashs(ht1, ht2, 1);
	//
	//		GasGemisch gg=new GasGemisch(ht2,"GasGemisch");
	//		Hashtable<Spezies, Double> ht4=gg.get_speziesMassenBruecheDetail();
	//
	//		ht2=LittleHelpers.addiereHashs(ht3, ht3, 1);
	//
	//	}
	//
	//
	//
	//
	//	private static Hashtable<String, Vector<Double>> ergebnisHash=new Hashtable<String, Vector<Double>>();
	//	public static void test_ErgebnisHash(){
	//
	//		Vector<Double> vec =new Vector<Double>();
	//
	//		//ergebnisHash.put("Test",new Vector<Double>());
	//		String s="Test";
	//		vec.add(1d);
	//		vec.add(2d);
	//		System.out.println(vec.size());
	//		System.out.println(vec.get(1));
	//		double a=ergebnisHash.get("Test").lastElement();
	//		System.out.println(a);
	//	}
	//
	//	public static void test_HUbKolbenMotor(){
	//
	//		CasePara cp =Bremo.get_casePara();
	//		Motor m=cp.MOTOR;
	//
	//		double time,kw,test;
	//		double[][] values=new double [7200][4];
	//
	//		for(int i=0;i<7200;i++){		
	//			kw=(i-3600);
	//			kw=kw/10;
	//			values[i][0]=kw;
	//			time=cp.convert_KW2SEC(kw);
	//			values[i][1]=time;
	//			values[i][2]=m.get_V(time);
	//			values[i][3]=m.get_dV(time);
	//
	//		}
	//
	//		FileWriter_txt txtW= new FileWriter_txt(cp.get_workingDirectory()+"motorTest.txt");
	//		String [] header ={"KW","sec","V","dV"};
	//		txtW.writeTextLineToFile(header, false);
	//		txtW.writeMatrixToFile(values, true);
	//		System.out.println("MotorTest abgeschlossen");
	//
	//	}
	//
	//
	//
	//
	//
	//
	//	public static void test_IndizierDaten(){
	//		CasePara cp =Bremo.get_casePara();
	//		IndizierDaten indiD=new IndizierDaten(cp);
	//		System.out.println("Rechenbeginn in KW: " +cp.SYS.RECHNUNGS_BEGINN_DVA_KW);
	//		System.out.println("Rechenbeginn in s: " +cp.SYS.RECHNUNGS_BEGINN_DVA_SEC);		
	//		System.out.println("Rechenende in KW: " +cp.SYS.RECHNUNGS_ENDE_DVA_KW);
	//		System.out.println("Rechenende in s: " +cp.SYS.RECHNUNGS_ENDE_DVA_SEC);
	//
	//		System.out.println(	"\n	==============Los geht's==============\n"+
	//		"	======================================");
	//
	//		double time=0;		
	//		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Zylinderdruck betraegt: " + indiD.get_pZyl(time) + " [Pa]");
	//
	//		System.out.println(	"\n	==============Rundungsfehler==============\n"+
	//		"	==========================================");
	//
	//		System.out.println("Zeit gerundet");
	//		time=0.020888889;	
	//		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Einlassdruck betraegt: " + indiD.get_pEin(time) + " [Pa]");
	//
	//		System.out.println("Zeit exakt");
	//		time=cp.convert_KW2SEC(20.2);		
	//		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Einlassdruck betraegt: " + indiD.get_pEin(time) + " [Pa]");
	//
	//		System.out.println(	"\n	===========Interpolieren==================\n"+
	//		"	==========================================");	
	//
	//
	//
	//		time=(cp.convert_KW2SEC(-360)+cp.convert_KW2SEC(-359.9))/2;		
	//		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Einlassdruck betraegt: " + indiD.get_pEin(time) + " [Pa]");
	//
	//
	//
	//
	//		System.out.println(	"\n	=====Letzten und ersten Wert abfragen====\n"+
	//		"	=========================================");	
	//		System.out.println("letzter Wert");
	//		time=cp.convert_KW2SEC(359.9);		
	//		System.out.println("Der Zeitpunkt t="+ time+" [ms n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Zylinderdruck betraegt: " + indiD.get_pAus(time) + " [Pa]");
	//
	//		System.out.println("erster Wert");
	//		time=cp.convert_KW2SEC(-360);
	//		System.out.println("Der Zeitpunkt t="+ time + " [ms n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Zylinderdruck betraegt: " + indiD.get_pAus(time) + " [Pa]");
	//
	//		System.out.println("\n	==============Fehlermeldung===============\n"+
	//		"	==========================================");	
	//
	//		time=cp.convert_KW2SEC(360); //-360.1 geht auch ;-)			
	//		System.out.println("Der Zeitpunkt t="+ time+" [ms n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
	//				"Der Zylinderdruck betraegt: " + indiD.get_pZyl(time) + " [Pa]");
	//
	//	}
	//
	//	public static void test_pZylReader(){
	//		String pZylFile=("L://_DISS//Bremo//BremoXP//Bremo//src//pZyl.txt");
	//		IndizierFileReader_txt idfr=new IndizierFileReader_txt(pZylFile,2,720);
	//		System.out.println(idfr.get_pZylMAX());
	//	}
	//
	//	public static void testHeizwertRechner(){
	////
	////		Spezies krst=KoeffizientenSpeziesFabrik.get_spezDiesel();
	//		Spezies krst=SpeziesFabrik.get_spezH2();
	//		
	//		
	//		double lambda=1;	
	//		double lst=krst.get_Lst();
	//		double mk=1; //[kg]
	//		double mL_tr=lambda*mk*lst;
	//		double m_ges=mk+mL_tr;
	//
	//		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
	//
	//		gemisch_MassenbruchHash.put(krst, mk/m_ges);
	//		gemisch_MassenbruchHash.put(SpeziesFabrik.get_spezLuft_trocken(), mL_tr/m_ges);		
	//		GasGemisch gemisch=new GasGemisch("Gemisch");
	//		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
	//	
	//		System.out.println("direkte Ausgabe des KraftstoffHeizwerts/mGes: " +krst.get_Hu_mass()*mk/m_ges);
	//		System.out.println("direkte Ausgabe des GemischHeizwerts: " +gemisch.get_Hu_mass());
	//		
	//		System.out.println("Berechnung des GemischHeizwerts: " +
	//				HeizwertRechner.calcGemischHeizwert_chemGLGW_mass(gemisch, 298.15, 1e5));
	//		
	//		double Hu=HeizwertRechner.calcHup_chemGLGW(krst, 273.15 +25, lambda, 1e5);
	//		System.out.println("Der mittels des chemGleichgewichts und Uebergabe des Kraftstoffs berechnete GemischHeizwert HuG " +
	//				" von " + krst.get_name()+ " betraegt: "+ (Hu/krst.get_M()*mk/m_ges) + " [J/kg]");		
	//	
	//
	//		System.out.println("Der mittels des chemGleichgewichts und Uebergabe des Kraftstoffs berechnete Heizwert Hu " +
	//				" von " + krst.get_name()+ " betraegt: "+ (Hu/krst.get_M())*1e-6 + " [MJ/kg]");
	//		
	//		
	//		double Hu_0=HeizwertRechner.calcGemischHeizwert_chemGLGW_mass(gemisch, 273.15+25, 1e5);
	//
	//		System.out.println("Der mittels des GasGemsichs und chmGLGW berechnete Heizwert Hu " +
	//				" von " + krst.get_name()+ " betraegt: "+ Hu_0*m_ges*1e-6 + " [MJ/kg]");		
	//		
	//		
	//		
	//		//		System.out.println(PhysKonst.get_mass_O2_Luft());
	//		//		System.out.println("Lst von " + spez.get_name()+ " btraegt: "+spez.get_Lst() );		
	//	
	//
	//		double Hu_2=HeizwertRechner.calcHup_perfekt(krst);
	//		System.out.println("Der mittels perfektem Abgas berechnete Heizwert Hu " +
	//				" von " + krst.get_name()+ " betraegt: "+ Hu_2*1e-6 + " [MJ/mol]");
	//		
	//		System.out.println("Der hinterlegte  Heizwert Hu " +
	//				" von " + krst.get_name()+ " betraegt: "+ krst.get_Hu_mol()*1e-6 + " [MJ/mol]");
	//
	//		double Hu_3=HeizwertRechner.calcHup_perfekt(krst,298.15);
	//		System.out.println("Der mittels perfektem Abgas und bezugsTemp=298.15 berechnete Heizwert Hu " +
	//				" von " + krst.get_name()+ " betraegt: "+ Hu_3*1e-6 + " [MJ/mol]");
	//
	//		double Hu_4=HeizwertRechner.calcHup_perfekt(krst,1398.15);
	//		System.out.println("Der mittels perfektem Abgas und bezugsTemp=1398K berechnete Heizwert Hu " +
	//				" von " + krst.get_name()+ " betraegt: "+ Hu_4*1e-6 + " [MJ/mol]");
	//	}
	//
	//
	//	public static void testAdiabateVerbrennungsTemp(){
	//		Spezies krst=SpeziesFabrik.get_spezDiesel();
	////		Spezies krst=KoeffizientenSpeziesFabrik.get_spezH2();
	//		Spezies luft=SpeziesFabrik.get_spezLuft_trocken();
	//		double p=1e5; //Pa
	//		double T=298.15; //K	
	//		double lambda=1.00;
	//		double Tb;
	//		double lst=krst.get_Lst();	
	//		double mk=1; //[kg]
	//		double mL_tr=lambda*mk*lst;
	//		double m_ges=mk+mL_tr; 
	//		
	//
	//		//Gemisch erzeugen
	//		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
	//		gemisch_MassenbruchHash.put(krst, mk/m_ges);
	//		gemisch_MassenbruchHash.put(luft, mL_tr/m_ges);	
	//
	//		GasGemisch gemisch=new GasGemisch("Gemisch");
	//		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
	//		
	//		System.out.println(gemisch.get_O2_min());
	//		
	//		
	//		System.out.println("============================\n============================");		
	//		System.out.println("Berechnung der adiabaten Verbrennungstemperatur von "+krst.get_name()+ 
	//				" mit Luft bei Tu=" + T +" [K] und pu=" + p +" [Pa] sowie Lambda="+lambda);
	//		System.out.println("============================\n============================");
	//
	//		//Perfekte Verbrennung
	//		System.out.println("Perfekte Verbrennung mit Luft: " + HeizwertRechner.calcadiabateFlammenTempPerfekt(krst, T, lambda) + "[K]");
	//		
	//		System.out.println("Perfekte Verbrennung mit Luft (Uebergabe des Gemischs): " + 
	//				HeizwertRechner.calcadiabateFlammenTempPerfekt(gemisch, T) + "[K]");
	//
	//
	//		System.out.println("Chemische Gleichgewicht (Uebergabe des Krafstoffs): " + 
	//				HeizwertRechner.calcAdiabateFlammenTemp(krst, p, T, lambda));		
	//		
	//
	//		Tb=HeizwertRechner.calcAdiabateFlammenTemp(gemisch, p, T);
	//		System.out.println("Chemisches Gleichgewicht (Uebergabe des Luft/ Kraftstoffgemischs): "
	//				+ Tb +" [K]");
	//		
	//		
	//		
	//
	//		System.out.println("Literaturangabe fuer H2 nach Maas: 2380 [K]");
	//		System.out.println("============================\n============================");
	////		System.out.println("Berechnung der adiabaten Verbrennungstemperatur von "+krst.get_name()+ 
	////				" mit O2 bei Tu=" + T +" [K] und pu=" + p +" [Pa] und Lambda="+lambda);
	////
	////		double o2min=krst.get_O2_min();	
	////		double nKrst=1; //[mol]
	////		double schummelFaktor=1e-1;
	////		double nO2=lambda*nKrst*o2min-schummelFaktor; //[mol]
	////		double nN2=schummelFaktor*0.9;
	////		double nCO2=schummelFaktor*0.1;
	////		double nGes=nO2+nKrst+nN2+nCO2;
	////
	////		Spezies o2=KoeffizientenSpeziesFabrik.get_spezO2();
	////		Spezies n2=KoeffizientenSpeziesFabrik.get_spezN2();
	////		Spezies co2=KoeffizientenSpeziesFabrik.get_spezCO2();
	////
	////		Hashtable<Spezies,Double> gemisch_MolenbruchHash=new Hashtable<Spezies,Double>();		
	////		gemisch_MolenbruchHash.put(krst, nKrst/nGes);
	////		gemisch_MolenbruchHash.put(o2, nO2/nGes);
	////		gemisch_MolenbruchHash.put(n2, nN2/nGes);
	////		gemisch_MolenbruchHash.put(co2, nCO2/nGes);
	////
	////		GasGemisch gemischO2=new GasGemisch(gemisch_MolenbruchHash,"Gemish");
	////		Tb=HeizwertRechner.calcAdiabateFlammenTemp(gemischO2, p, T);
	////		System.out.println("Chemisches Gleichgewicht (Uebergabe des O2/ Kraftstoffgemischs): "
	////				+ Tb +" [K]");
	//	}
	//	
	//
	//	public static void testKalorikRechner(){
	//
	////		double [] pVec={1e5,10e5,100e5,200e5};
	////		double [] lambdaVec ={0.6,1,2,10e7};	
	//		double [] pVec={50e5,100e5,150e5,200e5};
	//		double [] lambdaVec ={1,2,3,10e7};
	//		double T_start=1000;
	//		double T_max=3500;
	//		double T_int=10;
	//		double p,lambda, T;
	//		double cKrst=8;
	//		double hKrst=15.63;
	//		double oKrst=0;
	//		double nKrst=0;			
	//		int anzT= (int) ((T_max-T_start)/T_int)+1;				
	//
	//		double [][]werte=new double[100][anzT];
	//
	//
	//
	//		Hashtable<String, Double> kalorikHash = new Hashtable<String, Double>(15);
	//		KalorikRechner kalorikRechner=new KalorikRechner();
	//
	//
	//		for (int i_p=0;i_p<4;i_p++){			
	//			p=pVec[i_p];			
	//			for (int i_L=0;i_L<4;i_L++){				
	//				lambda=lambdaVec[i_L];				
	//				int i=0;
	//				T=T_start;
	//				while(T<=T_max){		
	//					//					kalorikHash=kalorikRechner.berechneRauchgasKalorik_mass(p,
	//					//							T, lambda,krst);
	//					kalorikHash=kalorikRechner.berechneRauchgasKalorik_mass(p,
	//							T, lambda,cKrst,hKrst,oKrst,nKrst);
	//					//erzeugt eine Matrix mit allen auszugebenden Werten. 
	//					//Die Zahl der Spalten entspricht der Anzahl der Temperaturschritte 
	//					Enumeration<String> e = kalorikHash.keys();		
	//					int ikeys=0;
	//					while (e.hasMoreElements()) {
	//						String key=(String)e.nextElement();
	//						werte[ikeys][i]=kalorikHash.get(key);
	//						ikeys++;						 
	//					}					
	//					T=T+T_int;			
	//					i=i+1;
	//				}				
	//				Enumeration<String> e = kalorikHash.keys();
	//				int iter=0;
	//				System.out.println("schreibe files");
	//				while (e.hasMoreElements()) {
	//					String key=(String)e.nextElement()+ ".txt";
	//					FileWriter_txt txtFile = new FileWriter_txt(key);
	//					txtFile.writeLineToFile(werte[iter], true);	
	//					iter++;
	//				}		
	//
	//			}		
	//
	//		}		
	//
	//		System.out.println("testKalorikRechner -- fertig");
	//
	//
	//	}
	//
	//
	//
	//	public static void testGasGemisch(){
	//		System.out.println("////////////////////////////////////////////////");
	//		System.out.println("testGasGemisch...");
	//		Spezies CO2=SpeziesFabrik.get_spezCO2();
	//		Spezies CO2_2=CO2;
	//		Spezies CO2_3=SpeziesFabrik.get_spezCO2();	
	//		Spezies O2=SpeziesFabrik.get_spezO2();
	//		Spezies CO2_4=SpeziesFabrik.get_spezCO2();
	//		Spezies CO2_5=SpeziesFabrik.get_spezCO2();	
	//		Spezies CO2_6=SpeziesFabrik.get_spezCO2();	
	//
	//		double M_O2;
	//		M_O2=O2.get_M();
	//
	//		System.out.println("Gasgemisch aus Komponenten mit unterschiedlichem HashCode aber selben namen");
	//		Hashtable<Spezies, Double> molenBruchHash2 = new Hashtable<Spezies, Double>(15);
	//		molenBruchHash2.put(CO2, 0.0); //wird in nächster Zeile überschrieben da selber HashCode
	//		molenBruchHash2.put(CO2_2, 0.1);
	//		molenBruchHash2.put(CO2_3, 0.8);
	//		molenBruchHash2.put(CO2_4, 0.05);
	//		molenBruchHash2.put(CO2_5, 0.025);
	//		molenBruchHash2.put(CO2_6, 0.025);
	//
	//
	//
	//		GasGemisch gg2=new GasGemisch(molenBruchHash2,"CO2_aus_CO2_2_und_CO2_3");
	//		double u =gg2.get_u_mass(4000);
	//		double T=gg2.get_T4u_mass(u);
	//		double u2=gg2.get_u_mass(T);
	//		System.out.println((u-u2)/u);
	//		System.out.println("Zusammensetzung von gg2: " );
	//		Enumeration<Spezies>e1=gg2.get_speziesMolenBrueche().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("Molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg2.get_speziesMolenBrueche().get(spez1));
	//		}
	//		System.out.println("detaillierte Zusammensetzung von gg2: " );
	//		e1=gg2.get_speziesMolenBruecheDetail().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg2.get_speziesMolenBruecheDetail().get(spez1));
	//		}
	//
	//		System.out.println("\n"+"\n"+"einfache Gasmischung aus zwei unterschiedlichen Spezies");
	//		Hashtable<Spezies, Double> molenBruchHash3 = new Hashtable<Spezies, Double>(15);
	//		molenBruchHash3.put(CO2, 0.5);
	//		molenBruchHash3.put(O2, 0.5);
	//		GasGemisch gg3=new GasGemisch(molenBruchHash3,"O2_CO2");
	//		System.out.println("Zusammensetzung von gg3: " );
	//		e1=gg3.get_speziesMolenBrueche().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("Molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg3.get_speziesMolenBrueche().get(spez1));
	//		}
	//		System.out.println("detaillierte Zusammensetzung von gg3: " );
	//		e1=gg3.get_speziesMolenBruecheDetail().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg3.get_speziesMolenBruecheDetail().get(spez1));
	//		}
	//		System.out.println("Anzahl an C-Atomen: " +gg3.get_AnzC_Atome());
	//		System.out.println("Anzahl an O-Atomen: " +gg3.get_AnzO_Atome());
	//		System.out.println("Anzahl an H-Atomen: " +gg3.get_AnzH_Atome());
	//
	//
	//		System.out.println("\n"+"\n"+"Gasmischung aus einer Spezies (O2) und einer Gasmischung (O2_CO2=gg3)");
	//		Hashtable<Spezies, Double> molenBruchHash4 = new Hashtable<Spezies, Double>(15);
	//		double a,b;
	//		b=1.0/3.0;
	//		a=2.0/3.0;
	//		molenBruchHash4.put((Spezies)gg3, a);
	//		molenBruchHash4.put(O2, b);
	//		GasGemisch gg4=new GasGemisch(molenBruchHash4,"O2_CO2_O2");
	//		System.out.println("Zusammensetzung von gg4: " );
	//		e1=gg4.get_speziesMolenBrueche().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("Molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg4.get_speziesMolenBrueche().get(spez1));
	//		}
	//		System.out.println("detaillierte Zusammensetzung von gg4: " );
	//		e1=gg4.get_speziesMolenBruecheDetail().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg4.get_speziesMolenBruecheDetail().get(spez1));
	//		}	
	//		System.out.println("////////////////////////////////////////////////");
	//	}
	//
	//
	//	public static void testObjektKopie(){
	//		System.out.println("////////////////////////////////////////////////");
	//		System.out.println("testObjektKopie...");
	//		Spezies CO2=SpeziesFabrik.get_spezCO2();
	//		Spezies O2=SpeziesFabrik.get_spezO2();
	//		Spezies CO2_2=CO2; //zweimal das selbe objekt nur der name ist ein anderer
	//		Spezies CO2_3=SpeziesFabrik.get_spezCO2(); // neues Objekt
	//
	//		//GasGemisch aus Gasen mit selbem Hashcode
	//		Hashtable<Spezies, Double> molenBruchHash1 = new Hashtable<Spezies, Double>(15);
	//		//hat molenBruchHash1 drei oder zwei einträge?
	//		molenBruchHash1.put(CO2, 0.3);
	//		molenBruchHash1.put(CO2_2, 0.2);//CO2_2 hat den selben HashCode wie CO2
	//		molenBruchHash1.put(CO2_3, 0.5);
	//		System.out.println("Einträge von molenBruchHash1: " );
	//		Enumeration<Spezies>e1=molenBruchHash1.keys();
	//		Spezies spez3, spez4;
	//		while(e1.hasMoreElements()){
	//			System.out.println(molenBruchHash1.get(e1.nextElement()));
	//		}
	//
	//		Hashtable<Spezies, Double> molenBruchHash3 = new Hashtable<Spezies, Double>(15);
	//		molenBruchHash3.put(CO2, 0.5);
	//		molenBruchHash3.put(O2, 0.5);
	//		GasGemisch gg3=new GasGemisch(molenBruchHash3,"O2_CO2");
	//
	//		Hashtable<Spezies, Double> molenBruchHash4 = new Hashtable<Spezies, Double>(15);
	//		double a,b;
	//		b=1.0/3.0;
	//		a=2.0/3.0;
	//		molenBruchHash4.put((Spezies)gg3, a);
	//		molenBruchHash4.put(O2, b);
	//		GasGemisch gg4=new GasGemisch(molenBruchHash4,"O2_CO2_O2");			
	//		System.out.println("detaillierte Zusammensetzung von gg4 : " );
	//		e1=gg4.get_speziesMolenBruecheDetail().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg4.get_speziesMolenBruecheDetail().get(spez1));
	//		}	
	//
	//
	//		GasGemisch gg5=gg4;
	//		e1=gg5.get_speziesMolenBruecheDetail().keys();
	//		System.out.println("\n"+"detaillierte Zusammensetzung von gg5 : " );
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg5.get_speziesMolenBruecheDetail().get(spez1));
	//		}
	//		gg5.set_Gasmischung_molenBruch(molenBruchHash3);			
	//		System.out.println("\n"+"detaillierte Zusammensetzung von gg5 nach set_Gasmischung :" );
	//		e1=gg5.get_speziesMolenBruecheDetail().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg5.get_speziesMolenBruecheDetail().get(spez1));
	//		}
	//		System.out.println("\n"+"detaillierte Zusammensetzung von gg4 nachdem gg5=gg4 verändert wurde: " );
	//		e1=gg4.get_speziesMolenBruecheDetail().keys();
	//		while(e1.hasMoreElements()){
	//			Spezies spez1=e1.nextElement();
	//			System.out.println("molenbruch von "+spez1.get_name()+" :" );
	//			System.out.println(gg4.get_speziesMolenBruecheDetail().get(spez1));
	//		}
	//		System.out.println("////////////////////////////////////////////////");
	//	}
	//
	//	public static void testKalorikRechner_Luft(){		
	//
	//		double T;
	//		double T_start=1000;
	//		double T_max=3000;
	//		double T_int=10;	
	//		int anzT= (int) ((T_max-T_start)/T_int)+1;				
	//
	//		double[][]werte=new double[4][anzT];	
	//		Hashtable<String, double[]> kalorikHash = new Hashtable<String, double[]>(15);
	//
	//		Hashtable<Spezies, Double> molenBruchHash = new Hashtable<Spezies, Double>(15);
	//		Spezies CO2=SpeziesFabrik.get_spezCO2();		
	//		molenBruchHash.put(CO2,PhysKonst.get_vol_CO2_Luft());
	//		molenBruchHash.put(SpeziesFabrik.get_spezO2(), PhysKonst.get_vol_O2_Luft());
	//		molenBruchHash.put(SpeziesFabrik.get_spezN2(), PhysKonst.get_vol_N2_Luft());	
	//		molenBruchHash.put(SpeziesFabrik.get_spezAr(), PhysKonst.get_vol_Ar_Luft());
	//
	//		//		so gehts...
	//		System.out.println(molenBruchHash.get(CO2));
	//		//		so nicht...
	//		System.out.println(molenBruchHash.get(SpeziesFabrik.get_spezCO2()));
	//		//		hier wird ein neues Objekt mit neuem HashCode erzeugt deshalb klappts nicht!!
	//
	//		//		villeicht gehts ja so... ?
	//		Spezies CO2_2=CO2;
	//		System.out.println(molenBruchHash.get(CO2_2));
	//		if(CO2_2==CO2){
	//			System.out.println("CO2_2=CO2");
	//		}
	//		//		und wie sieht´s so aus...
	//		KoeffizientenSpezies CO2_3=(KoeffizientenSpezies)CO2;
	//		System.out.println(molenBruchHash.get(CO2_3));
	//
	//		//		Spezies spezB=new KrstApproxSpezies(1,1,1,1,1,1,1,"Test");
	//		//		molenBruchHash.put(spezB, 0.01);
	//
	//		//		hier wird Luft als direkte Mischung der Koeffizienten erzeugt
	//		//		Spezies luftKG = GasGemisch.Gasmischer.erzeugeKoeffSpeziesGemisch(molenBruchHash, "Luft");
	//
	//		//		hier wird Luft als Gasgemisch erzeugt, um die Ergebnisse mit denen des KoeffSpezies zu vergleichen
	//		Spezies luftGG = new GasGemisch(molenBruchHash, "LuftGG");
	//
	//
	//		//		hier wird Luft als Gemisch zweier Gasgemische erzeugt
	//		Hashtable<Spezies, Double> molenBruchHashA = new Hashtable<Spezies, Double>(15);
	//		double a=PhysKonst.get_vol_CO2_Luft()+PhysKonst.get_vol_O2_Luft();
	//		molenBruchHashA.put((KoeffizientenSpezies) SpeziesFabrik.get_spezCO2(), PhysKonst.get_vol_CO2_Luft()/a);
	//		molenBruchHashA.put((KoeffizientenSpezies) SpeziesFabrik.get_spezO2(), PhysKonst.get_vol_O2_Luft()/a);		
	//		GasGemisch A=new GasGemisch(molenBruchHashA, "LuftA");
	//
	//		Hashtable<Spezies, Double> molenBruchHashB = new Hashtable<Spezies, Double>(15);
	//		double b=PhysKonst.get_vol_N2_Luft()+PhysKonst.get_vol_Ar_Luft();
	//		molenBruchHashB.put((KoeffizientenSpezies) SpeziesFabrik.get_spezN2(), PhysKonst.get_vol_N2_Luft()/b);	
	//		molenBruchHashB.put((KoeffizientenSpezies) SpeziesFabrik.get_spezAr(), PhysKonst.get_vol_Ar_Luft()/b);		
	//		GasGemisch B=new GasGemisch(molenBruchHashB, "LuftB");		
	//
	//		Hashtable<Spezies, Double> molenBruchHashC = new Hashtable<Spezies, Double>(15);
	//		molenBruchHashC.put(A, a);	
	//		molenBruchHashC.put(B, 1-a);
	//		GasGemisch luft2GG=new GasGemisch(molenBruchHashC, "Luft2GG");		
	//
	//
	//		Spezies luft=luft2GG;
	//
	//		int i=0;
	//		T=T_start;
	//		while(T<=T_max){
	//			werte[0][i] =luft.get_cp_mol(T);
	//			werte[1][i] =luft.get_cp_mass(T);
	//			werte[2][i]=luft.get_h_h298_mol(T);
	//			werte[3][i]=luft.get_h_h298_mass(T);
	//
	//			T=T+T_int;			
	//			i=i+1;
	//		}	
	//		kalorikHash.put("cp_mol", werte[0]);
	//		kalorikHash.put("cp_mass", werte[1]);
	//		kalorikHash.put("h_h298_mol", werte[2]);
	//		kalorikHash.put("h_h298_mass", werte[3]);
	//
	//
	//
	//		Enumeration<String> e = kalorikHash.keys();
	//		int iter=0;
	//		System.out.println("schreibe files");
	//		while (e.hasMoreElements()) {
	//			String key=(String)e.nextElement();
	//			FileWriter_txt txtFile = new FileWriter_txt(key+ ".txt");
	//			txtFile.writeLineToFile(kalorikHash.get(key), true);	
	//			iter++;
	//		}	
	//
	//		System.out.println("testKalorikRechner_Luft -- fertig");
	//
	//	}	
	//	
	//	
	//	
	//	public static void kalorikAbgleichJanaf(){
	//		
	//		Spezies spez=SpeziesFabrik.get_spezNO();
	//		double Ta=300;
	//		double Te=3000;
	//		double Ti=100;
	//		int steps=(int) ((Te-Ta)/Ti+1);
	//		double values[][]=new double [(steps+3)][5];
	//		
	//		double T=0;
	//		int a=0;
	//		values[a][0]=T;
	//		values[a][1]=spez.get_cp_mol(T);
	//		values[a][2]=spez.get_h_h298_mol(T)/1000;
	//		values[a][3]=spez.get_h_mol(T)/1000;
	//		values[a][4]=spez.get_delta_hf298_mol()/1000;	
	//		
	//		T=200;
	//		a=1;
	//		values[a][0]=T;
	//		values[a][1]=spez.get_cp_mol(T);
	//		values[a][2]=spez.get_h_h298_mol(T)/1000;
	//		values[a][3]=spez.get_h_mol(T)/1000;
	//		values[a][4]=spez.get_delta_hf298_mol()/1000;
	//		
	//		T=298.15;
	//		a=2;
	//		values[a][0]=T;
	//		values[a][1]=spez.get_cp_mol(T);
	//		values[a][2]=spez.get_h_h298_mol(T)/1000;
	//		values[a][3]=spez.get_h_mol(T)/1000;
	//		values[a][4]=spez.get_delta_hf298_mol()/1000;
	//		
	//		for(int i=0;i<steps;i++){
	//			T=Ta+i*Ti;
	//			a=i+3;
	//			values[a][0]=T;
	//			values[a][1]=spez.get_cp_mol(T);
	//			values[a][2]=spez.get_h_h298_mol(T)/1000;
	//			values[a][3]=spez.get_h_mol(T)/1000;
	//			values[a][4]=spez.get_delta_hf298_mol()/1000;				
	//		}		
	//		
	//		FileWriter_txt txtFile = new FileWriter_txt(spez.get_name()+ "_KalDat.txt");
	//		String s[]={"T","cp","H-H298","H","deltaH"};
	//		txtFile.writeTextLineToFile(s, false);
	//		String []s2={"K","J/mol/K","kJ/mol","kJ/mol","kJ/mol"};
	//		txtFile.writeTextLineToFile(s2, true);
	//		txtFile.writeMatrixToFile(values, true);		
	//	}
	//	
	//	
	//
	//	/**
	//	 * berechnet das OHC-Gleichgewicht nach der von Grill vorgeschlagenen Methode und liefert fuer
	//	 * die einzelnen Spezies je ein txt-File mit der jeweiligen Konzentration in ppm fuer p= 1, 10, 100 und 200bar 
	//	 * sowie fuer Lambda=0.5, 1, 2, 10e7 
	//	 * jeweils in Abhaengigkeit der Temperatur. 
	//	 */
	//	public static void testGleichgewichtsRechner_A(){		
	//
	//
	//
	//		double [] pVec={1e5,10e5,100e5,200e5};
	//		double [] lambdaVec ={0.5,1,2.5,10e7};		
	//		double T_start=1600;
	//		double T_max=2500;
	//		double T_int=10;
	//		double p,lambda, T;
	//		int anzT= (int) ((T_max-T_start)/T_int)+1;				
	//
	//		double [][]werte=new double[100][anzT];
	//
	//		double cKrst=8;
	//		double hKrst=15.63;
	//		double oKrst=0;
	//		double nKrst=0;					
	//
	//		GleichGewichtsRechner GGsolver=GleichGewichtsRechner.get_Instance();
	//		//		GGsolver = new GleichGewichtsRechner_Olikara_Borman("Burcat", 1600);	
	//		//		GGsolver = new GleichGewichtsRechner_Grill("Burcat", 1600);			
	//
	//		Hashtable<Spezies, Double> gleichGewichtsMolenBruchHash=new Hashtable<Spezies, Double>(16);
	//
	//		for (int i_p=0;i_p<4;i_p++){			
	//			p=pVec[i_p];			
	//			for (int i_L=0;i_L<4;i_L++){				
	//				lambda=lambdaVec[i_L];				
	//				int i=0;
	//				T=T_start;
	//				while(T<=T_max){		
	//					gleichGewichtsMolenBruchHash =GGsolver.get_GG_molenBrueche(p, T, lambda, cKrst, hKrst, oKrst,nKrst);
	//					//erzeugt eine Matrix mit allen auszugebenden Werten. 
	//					//Die Zahl der Spalten entspricht der Anzahl der Temperaturschritte 
	//					Enumeration e = gleichGewichtsMolenBruchHash.keys();		
	//					int ikeys=0;
	//					while (e.hasMoreElements()) {
	//						Spezies key=(Spezies)e.nextElement();
	//						werte[ikeys][i]=gleichGewichtsMolenBruchHash.get(key)*1e6; //Umrechnung in ppm						 
	//						ikeys++;						 
	//					}					
	//					T=T+T_int;			
	//					i++;
	//				}				
	//				Enumeration e = gleichGewichtsMolenBruchHash.keys();
	//				int iter=0;
	//				System.out.println("schreibe files");
	//				while (e.hasMoreElements()) {
	//					Spezies spez =(Spezies)e.nextElement();
	//					String key=spez.get_name()+ ".txt";
	//					FileWriter_txt txtFile = new FileWriter_txt(key);
	//					txtFile.writeLineToFile(werte[iter], true);	
	//					iter++;
	//				}		
	//
	//			}		
	//
	//		}		
	//
	//		System.out.println("testGleichgewichtsRechner_A -- fertig");		
	//
	//	}
	//
	//	/**
	//	 * berechnet das OHC-Gleichgewicht nach der von Grill vorgeschlagenen Methode und liefert für
	//	 * die einzelnen Spezies je ein txt-File mit der jeweiligen Konzentration in ppm für p= 1, 10, 100 und 200bar 
	//	 * sowie für T=1600, 2000, 2500, 3000K 
	//	 * jeweils in Abhängigkeit von Lambda. 
	//	 */	
	//	public static void testGleichgewichtsRechner_B(){		
	//
	//
	//		double [] pVec={1e5,10e5,100e5,200e5};
	//		double [] TVec ={1600,2000,2500,3000};
	//		double lam_start=0.5;
	//		double lam_max=10;
	//		double lam_int=0.1; 
	//		double p,lambda, T;
	//		int anzLam= (int) ((lam_max-lam_start)/lam_int)+1;				
	//
	//		double [][]werte=new double[100][anzLam];
	//
	//		GleichGewichtsRechner GGsolver=null;;
	//		//		GGsolver = new GleichGewichtsRechner_Olikara_Borman("Burcat", 1600);	
	//		//		GGsolver = new GleichGewichtsRechner_Grill("Burcat", 1600);	
	//
	//		Hashtable<Spezies, Double> gleichGewichtsMolenBruchHash=new Hashtable<Spezies, Double>(16);
	//
	//		double cKrst=8;
	//		double hKrst=15.63;
	//		double oKrst=0;
	//		double nKrst=0;
	//
	//		for (int i_p=0;i_p<pVec.length;i_p++){			
	//			p=pVec[i_p];			
	//			for (int i_T=0;i_T<TVec.length;i_T++){				
	//				T=TVec[i_T];				
	//				int i_lam=0;
	//				lambda=lam_start;
	//
	//				while(lambda<=lam_max){		
	//					gleichGewichtsMolenBruchHash = GGsolver.get_GG_molenBrueche(p, T, lambda, cKrst, hKrst, oKrst,nKrst);
	//					//erzeugt eine Matrix mit allen auszugebenden Werten. 
	//					//Die Zahl der Spalten entspricht der Anzahl der Lambdaschritte 
	//					Enumeration e = gleichGewichtsMolenBruchHash.keys();		
	//					int ikeys=0;
	//					while (e.hasMoreElements()) {
	//						Spezies key=(Spezies)e.nextElement();					
	//						werte[ikeys][i_lam]=gleichGewichtsMolenBruchHash.get(key)*1e6; //Umrechnung in ppm						
	//						ikeys++;						 
	//					}					
	//					lambda=lambda+lam_int;			
	//					i_lam++;
	//				}
	//
	//				Enumeration e = gleichGewichtsMolenBruchHash.keys();
	//				int iter=0;
	//				System.out.println("schreibe files");
	//				while (e.hasMoreElements()) {
	//					Spezies spez =(Spezies)e.nextElement();
	//					String key=spez.get_name()+ ".txt";
	//					FileWriter_txt txtFile = new FileWriter_txt(key);
	//					txtFile.writeLineToFile(werte[iter], true);	
	//					iter++;
	//				}		
	//
	//			}		
	//
	//		}
	//
	//
	//		System.out.println("testGleichgewichtsRechner_B -- fertig");
	//
	//
	//	}
	//
	//	/**
	//	 * Diese Funktion dient dazu zu ueberpruefen ob die Zusammensetzung des Abgases sich aendert wenn AGR zugegeben wird. 
	//	 * Die Temperatur und der Druck werden bei der Verbrennung konstant gehalten. Das zugemischte AGR entsteht durch die Verbrennung
	//	 * der selben Mengen an Luft und Kraftstoff
	//	 * Ergebnis: Das die Atomzahlen aller Mischungen gleich sind haengt die Zusammensetzung des Abgases nicht von der AGR-Rate ab!
	//	 */
	//	public static void testSpeziesInputFuerOHCsolver(){		
	//
	//		double T=2000;
	//		double p=100e5; //100bar
	//		double lambda=1;
	//
	//		Hashtable<Spezies, Double> massenBruchHash = new Hashtable<Spezies, Double>(15);
	//		Spezies ron95=SpeziesFabrik.get_spezRON_95();	
	//		Spezies luft=SpeziesFabrik.get_spezLuft_trocken();
	//
	//		double lst=ron95.get_Lst();
	//
	//
	//		System.out.println(lst);
	//		double xsiKRST=1/(1+lambda*lst);
	//		double xsiLuft=1-xsiKRST;
	//		massenBruchHash.put(ron95,xsiKRST);
	//		massenBruchHash.put(luft, xsiLuft);
	//
	//		GasGemisch frischgemisch=new GasGemisch("gemisch");
	//		frischgemisch.set_Gasmischung_massenBruch(massenBruchHash);		
	//
	//
	//
	//		GleichGewichtsRechner gg=GleichGewichtsRechner.get_Instance();
	//		//Berechnung der Abgaszusammensetzung aus dem Frischgemisch bei T=T_Burn
	//		GasGemisch abgas_T_Burn=new GasGemisch(gg.get_GG_molenBrueche(p, T, frischgemisch),"Abgas_T_Burn");		
	//		//Zusammensetzung des Abgases ohne verbrennung mit AGR
	//		Hashtable<Spezies, Double> molenBruecheAbgasNurLuft = abgas_T_Burn.get_speziesMolenBruecheDetail();
	//
	//
	//		//Berechnung der Abgaszusammensetzung mittels lambda und der Krafstoffzusammensetzung
	//		GasGemisch abgas_T_Burn_2=new GasGemisch(gg.get_GG_molenBrueche(p, T, lambda, 
	//				ron95.get_AnzC_Atome(), ron95.get_AnzH_Atome(), ron95.get_AnzO_Atome(),
	//				ron95.get_AnzN_Atome()),"Abgas_T_Burn_2");		
	//		//Zusammensetzung des Abgases ohne verbrennung mit AGR
	//		Hashtable<Spezies, Double> molenBruecheAbgasNurLuft_2 = abgas_T_Burn_2.get_speziesMolenBruecheDetail();		
	//
	//
	//		Enumeration<Spezies> e0 = molenBruecheAbgasNurLuft_2.keys();		
	//		System.out.println("Spezies vs Lambda + Kraftstoffzusammensetzung");
	//		System.out.println("========================================");
	//
	//		System.out.println("Differenz der C-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzC_Atome()-abgas_T_Burn.get_AnzC_Atome()));
	//		System.out.println("Differenz der O-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzO_Atome()-abgas_T_Burn.get_AnzO_Atome()));
	//		System.out.println("Differenz der H-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzH_Atome()-abgas_T_Burn.get_AnzH_Atome()));
	//		System.out.println("Differenz der N-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzN_Atome()-abgas_T_Burn.get_AnzN_Atome()));
	//
	//
	//		while (e0.hasMoreElements()) {	
	//			Spezies key=e0.nextElement();
	//			String name=key.get_name();	
	//			//			if(molenBruecheAbgasNurLuft.containsKey(key))
	//			System.out.println(name+ ": Spezies--> "+molenBruecheAbgasNurLuft.get(key)+
	//					"   Lambda--> "+molenBruecheAbgasNurLuft_2.get(key));
	//
	//			if(molenBruecheAbgasNurLuft.containsKey(key))
	//				System.out.println(" --> Abweichung: "+ (molenBruecheAbgasNurLuft.get(key)-molenBruecheAbgasNurLuft_2.get(key)));
	//		}	
	//
	//
	//
	//
	//
	//
	//
	//
	//		//Erzeugung einer Mischung aus 80% Frischgemisch und 20% AGR 
	//		Hashtable<Spezies, Double> massenBruchHashAGRFrischGemisch = new Hashtable<Spezies, Double>(15);
	//		massenBruchHashAGRFrischGemisch.put(abgas_T_Burn, 0.2);
	//		massenBruchHashAGRFrischGemisch.put(frischgemisch, 0.8);
	//		GasGemisch frischGemisch_Mit_AGR_T_Burn=new GasGemisch("FG_AGR");
	//		frischGemisch_Mit_AGR_T_Burn.set_Gasmischung_massenBruch(massenBruchHashAGRFrischGemisch);
	//
	//
	//		//Berechnung der Abgaszusammensetzung mit dem eben erzeugten Gemisch
	//		GasGemisch abgas2=new GasGemisch(gg.get_GG_molenBrueche(p, T, frischGemisch_Mit_AGR_T_Burn),"Abgas2");
	//
	//
	//		Hashtable<Spezies, Double> molenBruecheAbgasMitAGR_T_Burn = abgas2.get_speziesMolenBruecheDetail();
	//
	//		Enumeration<Spezies> e = molenBruecheAbgasMitAGR_T_Burn.keys();		
	//		System.out.println("Abgas2");
	//		System.out.println("========================================");
	//
	//		System.out.println("Differenz der C-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzC_Atome()-abgas_T_Burn.get_AnzC_Atome()));
	//		System.out.println("Differenz der O-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzO_Atome()-abgas_T_Burn.get_AnzO_Atome()));
	//		System.out.println("Differenz der H-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzH_Atome()-abgas_T_Burn.get_AnzH_Atome()));
	//		System.out.println("Differenz der N-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzN_Atome()-abgas_T_Burn.get_AnzN_Atome()));
	//
	//
	//		while (e.hasMoreElements()) {	
	//			Spezies key=e.nextElement();
	//			String name=key.get_name();	
	//			//			if(molenBruecheAbgasNurLuft.containsKey(key))
	//			System.out.println(name+ ": 1--> "+molenBruecheAbgasNurLuft.get(key)+
	//					"   2--> "+molenBruecheAbgasMitAGR_T_Burn.get(key));
	//
	//			if(molenBruecheAbgasNurLuft.containsKey(key))
	//				System.out.println(" --> Abweichung: "+ (molenBruecheAbgasNurLuft.get(key)-molenBruecheAbgasMitAGR_T_Burn.get(key)));
	//		}	
	//
	//		//		 Erzeugung von Abgas mit T_Freeze
	//		GasGemisch abgas_T_Freeze=new GasGemisch(gg.get_GG_molenBrueche(p, 2800, frischgemisch),"Abgas_T_Freeze");
	//		//Erzeugung einer Mischung aus 80% Frischgemisch und 20% AGR_T_Freeze
	//		Hashtable<Spezies, Double> massenBruchHashAGR_Freeze_FrischGemisch = new Hashtable<Spezies, Double>(15);
	//		massenBruchHashAGR_Freeze_FrischGemisch.put(abgas_T_Freeze, 0.2);
	//		massenBruchHashAGR_Freeze_FrischGemisch.put(frischgemisch, 0.8);
	//		GasGemisch frischGemisch_Mit_AGR_T_Freeze=new GasGemisch("FG_AGR_T_Freeze");
	//		frischGemisch_Mit_AGR_T_Freeze.set_Gasmischung_massenBruch(massenBruchHashAGR_Freeze_FrischGemisch);		
	//		//Berechnung der Abgaszusammensetzung
	//		GasGemisch abgas3=new GasGemisch(gg.get_GG_molenBrueche(p, T, frischGemisch_Mit_AGR_T_Freeze),"Abgas3");
	//		Hashtable<Spezies, Double> molenBruecheAbgasMitAGR_T_Freeze = abgas3.get_speziesMolenBruecheDetail();
	//
	//		System.out.println("Abgas3");
	//		System.out.println("========================================");
	//
	//		System.out.println("Differenz der C-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzC_Atome()-abgas_T_Burn.get_AnzC_Atome()));
	//		System.out.println("Differenz der O-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzO_Atome()-abgas_T_Burn.get_AnzO_Atome()));
	//		System.out.println("Differenz der H-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzH_Atome()-abgas_T_Burn.get_AnzH_Atome()));
	//		System.out.println("Differenz der N-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzN_Atome()-abgas_T_Burn.get_AnzN_Atome()));
	//		Enumeration<Spezies> e1 =molenBruecheAbgasMitAGR_T_Freeze.keys();		
	//		while (e1.hasMoreElements()) {	
	//			Spezies key=e1.nextElement();
	//			String name=key.get_name();	
	//			//				if(molenBruecheAbgasNurLuft.containsKey(key))
	//			System.out.println(name+ ": 1--> "+molenBruecheAbgasNurLuft.get(key)+
	//					"   3--> "+molenBruecheAbgasMitAGR_T_Freeze.get(key));
	//			if(molenBruecheAbgasNurLuft.containsKey(key))
	//				System.out.println(" --> Abweichung: "+ (molenBruecheAbgasNurLuft.get(key)-molenBruecheAbgasMitAGR_T_Freeze.get(key)));
	//		}	
	//
	//
	//
	//		System.out.println("testSpeziesInputFuerOHCSolver -- fertig");
	//
	//	}

}




