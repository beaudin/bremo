package bremo.sys;


import java.util.Hashtable;
import java.util.Vector;

import kalorik.Spezies;
import berechnungsModule.SolverConnector;
import berechnungsModule.Berechnung.SimulationModel;
import berechnungsModule.Berechnung.DVA_homogen_ZweiZonig;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import matLib.DiefferetialEqationSys;
import matLib.RungeKutta;


public class Solver implements DiefferetialEqationSys {


	private final CasePara CP;
	private Zone [] znSOL=null;
	private Zone [] znOut=null;
	private SimulationModel bmi;
	private Vector<SolverConnector> vecMPI;


	RungeKutta rk;
	



	public void setInitialValueOfX(double x0){
		rk.setInitialValueOfX(x0);
	}

	public void setFinalValueOfX(double xn){
		rk.setFinalValueOfX(xn);
	}	

	public void setStepSize(double step){
		rk.setStepSize(step);
	}
	
	
	public Solver(CasePara cp){			
		CP=cp;		
		rk=new RungeKutta();
		vecMPI=new Vector<SolverConnector>();
	}
	
	public void set_BerechnungsModell(SimulationModel bmi){
		this.bmi=bmi;
	}
	
	public Zone[] solveRK(Zone [] znIN){		
		int numOfValues=3+CP.SPECIES_FACTROY.get_nbrOfSpecies(); //p,T,V + Speziesmassen
		int nbrOfParameters=0;
		for(int i=0;i<vecMPI.size();i++)
			nbrOfParameters+=vecMPI.get(i).get_nbrOfParameters();				
				
		double p_V_T_mi_znIN[],p_V_T_mi_Paras_RK[] ;
		double p_V_T_mi_Paras_INIT[]=new double[(znIN.length*numOfValues)+nbrOfParameters];

		if(znSOL==null){//damit nicht bei jedem Aufruf znSOL neu erzeugt wird
			znSOL=new Zone[znIN.length];
			for(int idx=0;idx<znIN.length;idx++){
				p_V_T_mi_znIN=znIN[idx].get_p_V_T_mi();
				znSOL[idx]=new Zone(CP,p_V_T_mi_znIN,znIN[idx].isBurnt(),znIN[idx].getID());
				for(int i=0;i<numOfValues;i++) p_V_T_mi_Paras_INIT[idx*numOfValues+i]=p_V_T_mi_znIN[i];
			}		
		}else{	
			for(int idx=0;idx<znIN.length;idx++){
				{
					p_V_T_mi_znIN=znIN[idx].get_p_V_T_mi();	

					for(int i=0;i<numOfValues;i++) p_V_T_mi_Paras_INIT[idx*numOfValues+i]=p_V_T_mi_znIN[i];
				}			
			}
		}
		
		int startIdx=znIN.length*numOfValues;
		for(int i=0;i<vecMPI.size();i++){
			double parameters[]=vecMPI.get(i).get_modelParameters2Integrate();
			for(int idx=0;idx<parameters.length;idx++){
				p_V_T_mi_Paras_INIT[startIdx+idx]=parameters[idx];
			}
			startIdx=startIdx+parameters.length;
		}
		

		this.rk.setInitialValueOfY(p_V_T_mi_Paras_INIT);
		//Rungekutta aufrufen und znIN mit den berechneten Werten aktualisieren und wieder ausgeben
				p_V_T_mi_Paras_RK=this.rk.fourthOrder(this);
//				p_V_T_mi_RK=this.rk.cashKarp(this);
//				p_V_T_mi_RK=this.rk.fehlberg(this);

		double [] p_V_T_mi=new double [numOfValues];

			znOut=new Zone[znIN.length];//wird jedesmal neu erzeugt da es sonst nicht funktioniert!
			for(int idx=0;idx<znIN.length;idx++){
				for(int i=0;i<numOfValues;i++) p_V_T_mi[i]=p_V_T_mi_Paras_RK[idx*numOfValues+i];				
				znOut[idx]=new Zone(CP,p_V_T_mi,znIN[idx].isBurnt(),znIN[idx].getID());				
			}
			znIN=null;
			
			startIdx=znOut.length*numOfValues;
			for(int i=0;i<vecMPI.size();i++){
				int maxIdx=vecMPI.get(i).get_nbrOfParameters();
				for(int idx=0;idx<maxIdx;idx++){
					vecMPI.get(i).set_modelParameter2Integrate(p_V_T_mi_Paras_RK[startIdx+idx], idx);
				}
				startIdx=startIdx+maxIdx;
			}
			
		return znOut;
	}	
	
	private double[] get_dp_dV_dT_dmi_dParas(Zone [] zonen, double time){	
		
		
		int numOfValues=3+CP.SPECIES_FACTROY.get_nbrOfSpecies();
		int nbrOfParameters=0;
		for(int i=0;i<vecMPI.size();i++)
			nbrOfParameters+=vecMPI.get(i).get_nbrOfParameters();	
		
		double []dp_dV_dT_dmi_dParas=new double [zonen.length*numOfValues+nbrOfParameters];

		double sumXsi=0;
		double sumEta=0;
		for(int i=0; i<zonen.length;i++){
			if(zonen[i].get_m()>CP.SYS.MINIMUM_ZONE_MASS){
				sumXsi=sumXsi+zonen[i].get_xsi();	
				sumEta=sumEta+zonen[i].get_eta();				
			}
		}

		double dVSys=CP.MOTOR.get_dV(time);

		double dp=(sumXsi-dVSys)/sumEta;	
		
//		if(Math.abs(time-CP.SYS.DUBUGGING_TIME_SEC)<0.5*CP.SYS.WRITE_INTERVAL_SEC){
//			System.out.println("dp: "+ dp);
//		}
//		System.out.println("sumXsi: "+sumXsi);
//		System.out.println("sumEta: " +sumEta);
//		System.out.println("dp: "+ dp);
		 
		double dmi[];
//		double dmdp[];
//		double dmdT[];

		for(int i=0; i<zonen.length;i++){	
			dmi=zonen[i].get_dmi();
//			dmdp=zonen[i].get_dmi_dp();
//			dmdT=zonen[i].get_dmi_dT();
			
			//ueberpruefen ob die Anzahl der Spezies uebereinstimmt
			if(dmi.length!=CP.SPECIES_FACTROY.get_nbrOfSpecies()){
				try{
					throw new BirdBrainedProgrammerException("NmbrOfAllSpez " +
							"entspricht nicht der Anzahl der Spezies in einer Zone");
				}catch(BirdBrainedProgrammerException e){
					e.stopBremo();
				}
			}
			// mZone=0 fuehrt zur Division durch Null!! 
//			--> ausblenden von Zonen die eine zu kleine Masse haben
			if(zonen[i].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
				//dp
				dp_dV_dT_dmi_dParas[i*numOfValues]=dp; 
				//dV				
				dp_dV_dT_dmi_dParas[i*numOfValues+1]=zonen[i].get_xsi()-zonen[i].get_eta()*dp; 
				//dT
				dp_dV_dT_dmi_dParas[i*numOfValues+2]=(zonen[i].get_aY()-
						zonen[i].get_ap()*dp-
						zonen[i].get_p()*dp_dV_dT_dmi_dParas[i*numOfValues+1])
						/zonen[i].get_aT();		
				
//				System.out.println("dp: " + dp_dV_dT_dmi[i*numOfValues]);
//				System.out.println("dV: " + dp_dV_dT_dmi[i*numOfValues+1]);
//				System.out.println("dT: " + dp_dV_dT_dmi[i*numOfValues+2]);
				//mi
				for(int idx=0;idx<dmi.length;idx++) dp_dV_dT_dmi_dParas[i*numOfValues+3+idx]=dmi[idx];
						//+dmdT[idx]*dp_dV_dT_dmi[i*numOfValues+2]+dmdp[idx]*dp;
						//Die Integration mit dm=dm*+dmdp*dp+dmdT*dT funktioniert nicht! Keine Ahnung warum
						//Dissoziation wird in der Zone berücksichtigt
			}else{
				//dp
				dp_dV_dT_dmi_dParas[i*numOfValues]=dp; 
				//dV
				dp_dV_dT_dmi_dParas[i*numOfValues+1]=0; 
				//dT
				dp_dV_dT_dmi_dParas[i*numOfValues+2]=0;
				//mi
				for(int idx=0;idx<dmi.length;idx++)	{
					if(dmi[idx]>0){
						dp_dV_dT_dmi_dParas[i*numOfValues+3+idx]=dmi[idx]; //so kann sich Masse in der Zone ansammeln 
					}//else entfaellt da, der Vektor mit nullen initialisiert wurde
				}					
			}			
		}	
		
		int startIdx=zonen.length*numOfValues;
		for(int i=0;i<vecMPI.size();i++){
			double diff_parameters[]=vecMPI.get(i).get_diff_modelParameters2Integrate();
			for(int idx=0;idx<diff_parameters.length;idx++){
				dp_dV_dT_dmi_dParas[startIdx+idx]=diff_parameters[idx];
			}
			startIdx=startIdx+diff_parameters.length;
		}

		return dp_dV_dT_dmi_dParas;
	}	

	
	public double[] derivn(double time, double[] y) {	
//		umrechnen der vektoriellen Daten auf die Zonen		
		int numOfValues=3+CP.SPECIES_FACTROY.get_nbrOfSpecies();		
		
		double [] p_V_T_mi=new double [numOfValues];		
		
		for(int idx=0;idx<znSOL.length;idx++){
			for(int i=0;i<numOfValues;i++) p_V_T_mi[i]=y[idx*numOfValues+i];			
			znSOL[idx].set_p_V_T_mi(p_V_T_mi);
		}
		
//		berechnen der Differentiale --> erster HS
		znSOL=bmi.aktualisiereDifferentiale(time, znSOL);		
//		umrechnen der Differentiale in Vektorform und Ausgabe		
		return this.get_dp_dV_dT_dmi_dParas(znSOL, time);
	}	
	
	/**
	 * Checks in a ModelParameterIntegrator from any model that wnats to integrate something.
	 * This method must be called from the constructor 
	 * of the model that wants to integrate something
	 * @param mpi
	 */
	public void connect2Solver(SolverConnector mpi){
		vecMPI.add(mpi);
	}
}
