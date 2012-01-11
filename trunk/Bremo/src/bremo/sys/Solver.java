package bremo.sys;


import java.util.Hashtable;
import kalorik.spezies.Spezies;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.DVA_homogen_ZweiZonig;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import matLib.DglSysFunction;
import matLib.RungeKutta;


public class Solver implements DglSysFunction {


	private final CasePara CP;
	private Zone [] znSOL=null;
	private Zone [] znOut=null;
	private BerechnungsModell bmi;


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
	
	
	public Solver(CasePara cp,BerechnungsModell bmi){
		this.bmi=bmi;	
		CP=cp;		
		rk=new RungeKutta();
	}


	
	public Zone[] solveRK(Zone [] znIN){	
		
		int numOfValues=3+Spezies.get_NmbrOfAllSpez(); //p,T,V + Speziesmassen
		double p_V_T_mi_znIN[],p_V_T_mi_RK[] ;
		double p_V_T_mi_INIT[]=new double[(znIN.length*numOfValues)];

		if(znSOL==null){//damit nicht bei jedem Aufruf znSOL neu erzeugt wird
			znSOL=new Zone[znIN.length];
			for(int idx=0;idx<znIN.length;idx++){
				p_V_T_mi_znIN=znIN[idx].get_p_V_T_mi();
				znSOL[idx]=new Zone(p_V_T_mi_znIN,znIN[idx].isBurnt(),znIN[idx].getID());
				for(int i=0;i<numOfValues;i++) p_V_T_mi_INIT[idx*numOfValues+i]=p_V_T_mi_znIN[i];
			}		
		}else{	
			for(int idx=0;idx<znIN.length;idx++){				

				{
					p_V_T_mi_znIN=znIN[idx].get_p_V_T_mi();	

					for(int i=0;i<numOfValues;i++) p_V_T_mi_INIT[idx*numOfValues+i]=p_V_T_mi_znIN[i];
				}			
			}
		}

		this.rk.setInitialValueOfY(p_V_T_mi_INIT);
		//Rungekutta aufrufen und znIN mit den berechneten Werten aktualisieren und wieder ausgeben
				p_V_T_mi_RK=this.rk.fourthOrder(this);
//				p_V_T_mi_RK=this.rk.cashKarp(this);
//				p_V_T_mi_RK=this.rk.fehlberg(this);

		double [] p_V_T_mi=new double [numOfValues];

			znOut=new Zone[znIN.length];//wird jedesmal neu erzeugt da es sonst nicht funktioniert!
			for(int idx=0;idx<znIN.length;idx++){
				for(int i=0;i<numOfValues;i++) p_V_T_mi[i]=p_V_T_mi_RK[idx*numOfValues+i];				
				znOut[idx]=new Zone(p_V_T_mi,znIN[idx].isBurnt(),znIN[idx].getID());				
			}
			znIN=null;
		return znOut;
	}	
	
	private double[] get_dp_dV_dT_dmi(Zone [] zonen, double time){	
		
		
		int numOfValues=3+Spezies.get_NmbrOfAllSpez();
		
		double []dp_dV_dT_dmi=new double [zonen.length*numOfValues];

		double sumXsi=0;
		double sumEta=0;
		for(int i=0; i<zonen.length;i++){
			if(zonen[i].get_m()>CP.SYS.MINIMALE_ZONENMASSE){
				sumXsi=sumXsi+zonen[i].get_xsi();	
				sumEta=sumEta+zonen[i].get_eta();	
			}
		}

		double dVSys=Motor.get_Instance(CP).get_dV(time);

		double dp=(sumXsi-dVSys)/sumEta;
		 
		double dmi[];
//		double dmdp[];
//		double dmdT[];

		for(int i=0; i<zonen.length;i++){	
			dmi=zonen[i].get_dmi();
//			dmdp=zonen[i].get_dmi_dp();
//			dmdT=zonen[i].get_dmi_dT();
			
			//ueberpruefen ob die Anzahl der Spezies uebereinstimmt
			if(dmi.length!=Spezies.get_NmbrOfAllSpez()){
				try{
					throw new BirdBrainedProgrammerException("NmbrOfAllSpez " +
							"entspricht nicht der Anzahl der Spezies in einer Zone");
				}catch(BirdBrainedProgrammerException e){
					e.stopBremo();
				}
			}
			// mZone=0 fuehrt zur Division durch Null!! 
//			--> ausblenden von Zonen die eine zu kleine Masse haben
			if(zonen[i].get_m()>=CP.SYS.MINIMALE_ZONENMASSE){
				//dp
				dp_dV_dT_dmi[i*numOfValues]=dp; 
				//dV
				dp_dV_dT_dmi[i*numOfValues+1]=zonen[i].get_xsi()-zonen[i].get_eta()*dp; 
				//dT
				dp_dV_dT_dmi[i*numOfValues+2]=(zonen[i].get_aY()-
						zonen[i].get_ap()*dp-
						zonen[i].get_p()*dp_dV_dT_dmi[i*numOfValues+1])
						/zonen[i].get_aT();				
				//mi
				for(int idx=0;idx<dmi.length;idx++) dp_dV_dT_dmi[i*numOfValues+3+idx]=dmi[idx];
						//+dmdT[idx]*dp_dV_dT_dmi[i*numOfValues+2]+dmdp[idx]*dp;
						//Die Integration mit dm=dm*+dmdp*dp+dmdT*dT funktioniert nicht! Keine Ahnung warum
						//Dissoziation wird in der Zone berücksichtigt
			}else{
				//dp
				dp_dV_dT_dmi[i*numOfValues]=dp; 
				//dV
				dp_dV_dT_dmi[i*numOfValues+1]=0; 
				//dT
				dp_dV_dT_dmi[i*numOfValues+2]=0;
				//mi
				for(int idx=0;idx<dmi.length;idx++)	{
					if(dmi[idx]>0){
						dp_dV_dT_dmi[i*numOfValues+3+idx]=dmi[idx]; //so kann sich Masse in der Zone ansammeln 
					}//else entfaellt da, der Vektor mit nullen initialisiert wurde
				}					
			}			
		}		

		return dp_dV_dT_dmi;
	}	

	
	public double[] derivn(double time, double[] y) {	
//		umrechnen der vektoriellen Daten auf die Zonen		
		int numOfValues=3+Spezies.get_NmbrOfAllSpez();
		double [] p_V_T_mi=new double [numOfValues];		
		
		for(int idx=0;idx<znSOL.length;idx++){
			for(int i=0;i<numOfValues;i++) p_V_T_mi[i]=y[idx*numOfValues+i];			
			znSOL[idx].set_p_V_T_mi(p_V_T_mi);
		}
//		berechnen der Differentiale --> erster HS
		znSOL=bmi.aktualisiereDifferentiale(time, znSOL);		
//		umrechnen der Differentiale in Vektorform und Ausgabe		
		return this.get_dp_dV_dT_dmi(znSOL, time);
	}	
}
