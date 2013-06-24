package bremo.sys;


import kalorik.spezies.Spezies;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.PostProcessor;
import berechnungsModule.Berechnung.APR;
import berechnungsModule.Berechnung.APR_Cantera;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.DVA;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse;
import bremo.parameter.CasePara;




public class Rechnung {		
				
	private final CasePara CP;



	public Rechnung(CasePara cp) {		
		CP=cp;		
	}


	public void berechnungDurchfuehren(){	
		double x0, xn,schrittweite;
		BerechnungsModell dglSys=CP.BERECHNUNGS_MODELL;
		Solver sol=new Solver(CP, dglSys);
		// initial value of x
		x0 =CP.SYS.RECHNUNGS_BEGINN_DVA_SEC; //sollte normalerweise null sein aber man weiss ja nie
		sol.setInitialValueOfX(x0);

		// final value of x
		xn = CP.SYS.RECHNUNGS_ENDE_DVA_SEC; //in [s]  
		sol.setFinalValueOfX(xn);

		schrittweite = CP.SYS.WRITE_INTERVAL_SEC; //in [s]
		sol.setStepSize(schrittweite);
		
		int anzGesamtIterationen=0;
		//Anzahl simWerte muss für eine APR (minus 1) angepasst werden um einen
		//Interpolationsfehler zu vermeiden
		int anzSimWerte;
		if(dglSys.isDVA()==true)
		anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
		else
		anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE-1;
		double time;
		Zone[] zn=dglSys.get_initialZones();
		dglSys.bufferErgebnisse(x0, zn);
		
		Zone [] znTemp = null;
	

		for(int i=1;i<anzSimWerte;i++){

			time=x0+i*CP.SYS.WRITE_INTERVAL_SEC;	
			
			if(CP.SYS.IS_KW_BASIERT)
				System.out.println("berechne Zeitschritt: " +CP.convert_SEC2KW(time)+ "[KW]");	
			else
				System.out.println("berechne Zeitschritt: " +time+ "[sec]");
			sol.setFinalValueOfX(time);		

			if(dglSys.isDVA()==true){				
				double pIst=Double.NaN;
				boolean isConverged=false;			 
				int idx=0;
				do{	
					
					znTemp=sol.solveRK(zn);					
					pIst=  znTemp[0].get_p();
					//Vergleich ob der Berechnete Druck mit dem gemessenen übereinstimmt 
					// wenn nicht muss set_dQ aufgerufen werden und die Rechnung erneut durchgeführt werden				  
					isConverged=((DVA) dglSys).is_pSoll_Gleich_pIst(pIst,zn,time);	
					idx++;						
				}while (isConverged==false&& idx<100);	
				
				if(isConverged==false){
					System.out.println("mangelnde Konvergenz im Zeitschritt: " +CP.convert_SEC2KW(time)+ "[KW]");
					((DVA) dglSys).schreibe_DUBUGGING_Ergebnisse("NO_KON_"+
							CP.get_CaseName()+"_"+CP.convert_SEC2KW(time)+".txt");
				}
				else
					System.out.println("DVA CON: "+ (idx-1));	
				
				anzGesamtIterationen+=idx-1;				
							
			}else{				
				zn=((APR)dglSys).calc_dQburn(zn);			
				znTemp=sol.solveRK(zn);				
			}		
					
			zn=znTemp;
			
			znTemp=null;
			
			dglSys.bufferErgebnisse(time,zn);
			sol.setInitialValueOfX(time);
			CP.set_aktuelle_Rechenzeit(time);
			
			if(dglSys.initialiseBurntZone()){
				zn=dglSys.get_initialZonesWhileRunning();
				//Damit die Anfangsbedingungen der Zonen im Ergebnisfile erscheinen
				//dglSys.bufferErgebnisse(time,zn); 
			}			

			if(CP.SYS.DUBUGGING_MODE){					
				if(Math.abs(time-CP.SYS.DUBUGGING_TIME_SEC)<0.5*CP.SYS.WRITE_INTERVAL_SEC){ //Rechnet bis KW und schreibt dann alle Werte ins txt-file
					CP.schreibeAlleErgebnisFiles("DEBUG_"+CP.get_CaseName()+".txt");	
					System.out.println("I am plotting...");
				}
			}
		}
		if(CP.SYS.DUBUGGING_MODE)
			CP.schreibeAlleErgebnisFiles(CP.get_CaseName()+".txt");
		else
			dglSys.schreibeErgebnisFile(CP.get_CaseName()+".txt");
		
		PostProcessor pp=new PostProcessor(dglSys.get_dm_buffer(),
							dglSys.get_dQb_buffer(),dglSys.get_dQw_buffer(),CP);
		pp.schreibeErgebnisFile(CP.get_CaseName()+".txt");
		//CP.CANTERA_CALLER.releaseCantera();
		//((APR_Cantera)dglSys).releaseCantera();
		System.out.println("Gesamtanzahl der benoetigeten Iterationen: " + anzGesamtIterationen );
	}	

}
