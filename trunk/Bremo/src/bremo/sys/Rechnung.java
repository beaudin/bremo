package bremo.sys;


import kalorik.spezies.Spezies;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.PostProcessor;
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
		BerechnungsModell dglSys=BerechnungsModell.get_Instance(CP);
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
		int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
		double time;
		Zone[] zn=dglSys.get_initialZones();
		dglSys.bufferErgebnisse(x0, zn);
		
		Zone [] znTemp = null;

		for(int i=1;i<anzSimWerte;i++){

			time=x0+i*CP.SYS.WRITE_INTERVAL_SEC;	
			
			System.out.println("berechne Zeitschritt: " +CP.convert_SEC2KW(time)+ "[KW]");			

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
				}while (isConverged==false&& idx<1100);	
				
				if(isConverged==false){
					System.out.println("mangelnde Knvergenz im Zeitschritt: " +CP.convert_SEC2KW(time)+ "[KW]");
					((DVA) dglSys).schreibe_DUBUGGING_Ergebnisse("DEBUG_NO_KON_"+
							CP.get_CaseName()+"_"+CP.convert_SEC2KW(time)+".txt");
				}
				else
					System.out.println("DVA CON: "+ (idx-1));	
				
				anzGesamtIterationen+=idx-1;				
							
			}else{
				znTemp=sol.solveRK(zn);
			}			

			CP.set_aktuelle_Rechenzeit(time);
					
			zn=znTemp;
			
			znTemp=null;
			
			dglSys.bufferErgebnisse(time,zn);
			sol.setInitialValueOfX(time);
			
			if(dglSys.initialiseBurntZone()){
				zn=dglSys.get_initialZonesWhileRunning();
				//Damit die Anfangsbedingungen der Zonen im Ergebnisfile erscheinen
				dglSys.bufferErgebnisse(time,zn); 
			}
			
			
			if(CP.SYS.DUBUGGING_MODE){					
				if(Math.abs(time-CP.SYS.DUBUGGING_TIME_SEC)<0.5*CP.SYS.WRITE_INTERVAL_SEC){ //Rechnet bis KW und schreibt dann alle Werte ins txt-file
					ErgebnisBuffer.schreibeAlleErgebnisFiles("DEBUG_"+CP.get_CaseName()+".txt");	
					System.out.println("I am plotting...");
				}
			}
		}
		if(CP.SYS.DUBUGGING_MODE)
			ErgebnisBuffer.schreibeAlleErgebnisFiles(CP.get_CaseName()+".txt");
		else
			dglSys.schreibeErgebnisFile(CP.get_CaseName()+".txt");
		
		PostProcessor pp=new PostProcessor(dglSys.get_dm_buffer(),
							dglSys.get_dQb_buffer(),dglSys.get_dQw_buffer(),CP);
		pp.schreibeErgebnisFile(CP.get_CaseName()+".txt");
		
		System.out.println("Gesamtanzahl der benoetigeten Iterationen: " + anzGesamtIterationen );
	}
	

	public static  double ladungswechselAnalyseDurchfuehren(CasePara cp){
		double x0_LW, xn_LW, schrittweite_LW;
		double time;
		double mLuftFeucht;
		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		double mLuftFeucht_mess=mLuft_tr+mW;

		Zone[] zn_LW;
		BerechnungsModell dglSys_LW;
		dglSys_LW=new LadungsWechselAnalyse(cp);
		Solver LW_SOL= new Solver(cp,dglSys_LW);
		x0_LW=cp.get_Auslassoeffnet(); //initial value of x in [s]
		LW_SOL.setInitialValueOfX(x0_LW);
		xn_LW = cp.get_Einlassschluss()+cp.SYS.UMD_ASP/cp.get_DrehzahlInUproSec(); //final value of x in [s]
		LW_SOL.setFinalValueOfX(xn_LW);
		schrittweite_LW = cp.SYS.WRITE_INTERVAL_SEC; //in [s]
		LW_SOL.setStepSize(schrittweite_LW);

		//While --> LW-Analyse wird wiederholt, bis die Masse im Zylinder konvergiert hat
		//Schliefe über °KW (Auslassöffnet bis Einlassschluss)
		int anzSimWerte=(int) ((xn_LW-x0_LW)/schrittweite_LW+1);
		Zone [] znTemp = null;
		zn_LW=dglSys_LW.get_initialZones();

		dglSys_LW.bufferErgebnisse(x0_LW, zn_LW);	
		double m_neu = 0;
		double m_alt=zn_LW[0].get_m();

		double pInit=zn_LW[0].get_p();
		double VInit=zn_LW[0].get_V(); 
		Spezies abgas=zn_LW[0].get_ggZone();//CP.get_spezAbgas();
		int idx2=0;	
		do{//Iterationsschleife um auf den sleben Massenstrom zu kommen wie am PS gemessen --> Variation der Ladelufttemperatur
			double f_mInit=0.1;
			int idx=1;
			while(f_mInit>0.0005&&idx<=50){
				dglSys_LW.clearErgebnisBuffer();
				double TInit =pInit*VInit/(m_alt*abgas.get_R());
				//Zone wird jedes Mal neu erzeugt. Ihre Masse ist gleich die Masse, die beim Ende der letzen
				//Iteration (Einlassschluss) im Zylinder war.
				zn_LW[0]=new Zone(pInit, VInit,TInit, m_alt,
						abgas, false, 0);
				for(int i=1;i<anzSimWerte;i++){
					time=x0_LW+i*cp.SYS.WRITE_INTERVAL_SEC;		
					LW_SOL.setFinalValueOfX(time);
					znTemp=LW_SOL.solveRK(zn_LW);
					cp.set_aktuelle_Rechenzeit(time);
					zn_LW=znTemp;
					znTemp=null;	
					dglSys_LW.bufferErgebnisse(time,zn_LW);		
					LW_SOL.setInitialValueOfX(time);

					if(cp.SYS.DUBUGGING_MODE){						
						if(Math.abs(time-cp.SYS.DUBUGGING_TIME_SEC)<0.5*cp.SYS.WRITE_INTERVAL_SEC){ //Rechnet bis KW und schreibt dann alle Werte ins txt-file
							dglSys_LW.schreibeErgebnisFile("DEBUG_"+cp.get_CaseName()+".txt");	
							System.out.println("I am plotting...");
						}
					}				
				}
				if(cp.SYS.DUBUGGING_MODE){					
					dglSys_LW.schreibeErgebnisFile(cp.get_CaseName()+ "_" + idx + ".txt");	
				}		

				m_neu=zn_LW[0].get_m();

				f_mInit=Math.abs(m_neu-m_alt)/(m_alt);
				System.out.println(f_mInit);

				m_alt=m_neu;					
				idx++;
			}	

			System.out.println("Masse bei Iteration " + idx2 + " = "+ m_neu + " kg");
			mLuftFeucht=((LadungsWechselAnalyse)dglSys_LW).get_mLuftFeucht(zn_LW);
			//Anpassung der Laelufttemperatur um auf die gemessenen Luftmasse zu kommen
			((LadungsWechselAnalyse)dglSys_LW).set_TSaug(mLuftFeucht);				
			idx2+=1;
		}while(Math.abs((mLuftFeucht_mess-mLuftFeucht)/mLuftFeucht_mess)>0.005&&idx2<=50);
//		 "_" + (idx-1) +
		dglSys_LW.schreibeErgebnisFile(cp.get_CaseName()+ "_ERGEBNISSE_LW.txt");		
		System.out.println("Gesamtanzahl der benoetigeten Iterationen: " + (idx2) );	
		double agrInt=((LadungsWechselAnalyse)dglSys_LW).get_mAGRintern(zn_LW);
		return agrInt;
	}

}
