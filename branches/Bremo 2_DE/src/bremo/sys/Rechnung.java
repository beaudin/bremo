package bremo.sys;


import kalorik.spezies.Spezies;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.PostProcessor;
import berechnungsModule.Berechnung.APR;
import berechnungsModule.Berechnung.APR_Cantera;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.DVA;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse_ohneQb;
import bremo.parameter.CasePara;




public class Rechnung {		
				
	private final CasePara CP;
	private double turbulence = 0; //Turbulenzfaktor unten (falls Bargende) negativ initialisiert, muss aber immer positiv sein. Falls nicht verändert wird Fehler abgefangen 
	private boolean bargende=false;
	
	
	public Rechnung(CasePara cp) {		
		CP=cp;		
		if (CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")){ //vor der do-while-Schleife, sonst würde bei jedem Schritt "equals("Bargende")" im CP-File abgefragt werden
			bargende=true;
		}
	}


	public void berechnungDurchfuehren(){	
		double x0, xn, schrittweite;
//		BerechnungsModell dglSys=CP.BERECHNUNGS_MODELL;		
//		Solver sol=new Solver(CP, dglSys);
		BerechnungsModell dglSys=CP.BERECHNUNGS_MODELL;
		Solver sol=CP.SOLVER;
		// initial value of x
		x0 =CP.SYS.RECHNUNGS_BEGINN_DVA_SEC; //sollte normalerweise null sein aber man weiss ja nie
		sol.setInitialValueOfX(x0);

		// final value of x
		xn = CP.SYS.RECHNUNGS_ENDE_DVA_SEC; //in [s]  
		sol.setFinalValueOfX(xn);

		schrittweite = CP.SYS.WRITE_INTERVAL_SEC; //in [s]
		sol.setStepSize(schrittweite);
		
		int anzGesamtIterationen=0;
		
		//Anzahl simWerte muss für eine APR (minus 1) angepasst werden um einen Interpolationsfehler zu vermeiden 
		int anzSimWerte; 								//fuer Verlustteilung Frank Haertel
		if(dglSys.isDVA()==true){ 						//fuer Verlustteilung Frank Haertel
		
			anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
		//int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE; 
		}else 											 //fuer Verlustteilung Frank Haertel
		    anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE-1;  //fuer Verlustteilung Frank Haertel
		
		double time;
		Zone[] zn=dglSys.get_initialZones();
		dglSys.bufferErgebnisse(x0, zn);
		Zone [] znTemp = null;
	

		for(int i=1;i<anzSimWerte;i++){

			time=x0+i*CP.SYS.WRITE_INTERVAL_SEC;	
			
			if(CP.SYS.IS_KW_BASIERT)
//				System.out.println("berechne Zeitschritt: " +CP.convert_SEC2KW(time)+ "[KW]");	//ORIGINAL
				System.out.println("berechne Zeitschritt: " +Math.round(10*CP.convert_SEC2KW(time))/10.0+ "[KW]");	
			else
				System.out.println("berechne Zeitschritt: " +time+ "[sec]");
			sol.setFinalValueOfX(time);		

			
			if(dglSys.isDVA()==true){				
				
				double pIst=Double.NaN;
				boolean isConverged=false;			 
				int idx=0;
				
				if (bargende){
					turbulence = -1;
				}
				
				//Erste Rechnung vor der do-while Schleife
				znTemp=sol.solveRK(zn);					
				pIst = znTemp[0].get_p();	
				idx++;
				//Nach der ersten Rechnung wird der Turbulenzfaktor mit turbulence festgehalten, sonst wird er mit jedem weiteren Rechenschritt hoch gerechnet 
				//Später soll turbulence den entsprechenden Wert überschreiben (bisher nur für WWÜ Bargende nötig)
//				if (CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")){ //vor der do-while-Schleife, sonst würde bei jedem Schritt "equals("Bargende")" im CP-File abgefragt werden
				if (bargende){
					turbulence = CP.TURB_FACTORY.get_TurbulenceModel().get_k(zn, time);
				}
				
				//Vergleich ob der Berechnete Druck mit dem gemessenen übereinstimmt 
				// wenn nicht muss set_dQ aufgerufen werden und die Rechnung erneut durchgeführt werden
				isConverged = ((DVA) dglSys).is_pSoll_Gleich_pIst(pIst,zn,time);
			
				do{	
					znTemp = sol.solveRK(zn);					
					pIst = znTemp[0].get_p();				  
					isConverged = ((DVA) dglSys).is_pSoll_Gleich_pIst(pIst,zn,time);	
					idx++;	
					
				//Überschreiben von Turbulenzfaktor k (siehe oben) turbulence ist mit -1 initialisiert
				if (turbulence >= 0) {
//				if (turbulence >= -1E-12) {
					CP.TURB_FACTORY.get_TurbulenceModel().set_k(turbulence, 0);
				//Falls set_k nicht funktioniert oder Schmarrn zurück gegeben wird
				}else{
					System.err.println("Vorsicht, negative Turbulenz aufgetreten - sollte nicht passieren!");
					}
				}while (isConverged == false && idx<100);
				
				if(isConverged==false){
					System.err.println("mangelnde Konvergenz im Zeitschritt: " +CP.convert_SEC2KW(time)+ "[KW]");
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
		if(CP.BERECHNUNGS_MODELL.isDVA()){ //Nur wenn DVA
			PostProcessor pp=new PostProcessor(dglSys.get_dm_buffer(),					//war bei Juwe auskommentiert
								dglSys.get_dQb_buffer(),dglSys.get_dQw_buffer(),CP);	//war bei Juwe auskommentiert
			pp.schreibeErgebnisFile(CP.get_CaseName()+".txt");							//war bei Juwe auskommentiert
		}
		else{ //Wenn APR eigener Konstruktor in PostProcessor mit p_buffer
			PostProcessor pp=new PostProcessor(dglSys.get_dm_buffer(),					//war bei Juwe auskommentiert
					dglSys.get_dQb_buffer(),dglSys.get_dQw_buffer(),dglSys.get_p_buffer(),CP);	//war bei Juwe auskommentiert
			pp.schreibeErgebnisFile(CP.get_CaseName()+".txt");		
		}
		//CP.CANTERA_CALLER.releaseCantera();
		//((APR_Cantera)dglSys).releaseCantera();
		System.out.println("Gesamtanzahl der benoetigeten Iterationen: " + anzGesamtIterationen );
	}	

}
