package berechnungsModule.internesRestgas;

import java.io.File;

import kalorik.spezies.Spezies;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse_geschleppt;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse_ohneQb;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse;
import berechnungsModule.LadungswechselAnalyse.MasterLWA;
import bremo.parameter.CasePara;
import bremo.sys.Solver;

public class LWA extends InternesRestgas {
	private double mAGRint=-5.55;
	private String artLWA;

	protected LWA(CasePara cp, String art) {
		super(cp);
		this.artLWA = art;
	}

	@Override
	public double get_mInternesRestgas_ASP() {		
		if(mAGRint==-5.55)
			mAGRint=ladungswechselAnalyseDurchfuehren(super.CP);
		
		return mAGRint;
	}		
	
	private double ladungswechselAnalyseDurchfuehren(CasePara cp){
		if(artLWA.equalsIgnoreCase("check")){
			if(cp.get_AnzahlEinspritzungen()==0)
				artLWA="schlepp";
			else
				artLWA="normal";
		}
		double x0_LW, xn_LW, schrittweite_LW;
		double time;
		double mLuftFeucht;
		double mLuft_tr=cp.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=cp.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		double mLuftFeucht_mess=mLuft_tr+mW;

		Zone[] zn_LW;
		BerechnungsModell dglSys_LW;
		if(artLWA.equalsIgnoreCase("zwiko")){
			dglSys_LW=new LadungsWechselAnalyse(cp);
		}else if(artLWA.equalsIgnoreCase("schlepp")){
			dglSys_LW=new LadungsWechselAnalyse_geschleppt(cp);
		}else{
			dglSys_LW=new LadungsWechselAnalyse_ohneQb(cp);
		}
		Solver LW_SOL= new Solver(cp);
		LW_SOL.set_BerechnungsModell(dglSys_LW);
		x0_LW=cp.get_Auslassoeffnet(); //initial value of x in [s]
		LW_SOL.setInitialValueOfX(x0_LW);
		xn_LW = cp.get_Einlassschluss()+cp.SYS.DAUER_ASP_SEC; //final value of x in [s]
		LW_SOL.setFinalValueOfX(xn_LW);
		schrittweite_LW = cp.SYS.WRITE_INTERVAL_SEC; //in [s]
		LW_SOL.setStepSize(schrittweite_LW);

		//While --> LW-Analyse wird wiederholt, bis die Masse im Zylinder konvergiert hat
		//Schleife über °KW (Auslassöffnet bis Einlassschluss)
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
		do{//Iterationsschleife um auf den selben Massenstrom zu kommen wie am PS gemessen --> Variation der Ladelufttemperatur
			double f_mInit=0.1;
			int idx=1;
			while(f_mInit>0.0005&&idx<=50){
				dglSys_LW.clearErgebnisBuffer();
				double TInit =pInit*VInit/(m_alt*abgas.get_R());
				//Zone wird jedes Mal neu erzeugt. Ihre Masse ist gleich die Masse, die beim Ende der letzen
				//Iteration (Einlassschluss) im Zylinder war.
				zn_LW[0]=new Zone(CP,pInit, VInit,TInit, m_alt,
						abgas, false, 0);
				
				if(artLWA.equalsIgnoreCase("zwiko")) {
					((LadungsWechselAnalyse)dglSys_LW).set_mInit(m_alt);
				}
				
				for(int i=1;i<anzSimWerte;i++){
					time=x0_LW+i*cp.SYS.WRITE_INTERVAL_SEC;		
					LW_SOL.setFinalValueOfX(time);
					if(artLWA.equalsIgnoreCase("zwiko")){
						double pIst=Double.NaN;
						boolean converged=false;
						int idxlwa = 0;
						do{
							znTemp=LW_SOL.solveRK(zn_LW);
							pIst = znTemp[0].get_p();
//							System.out.println(pIst);
							if(pIst<0 || Double.isNaN(pIst)){
								dglSys_LW.bufferErgebnisse(time,zn_LW);	
								dglSys_LW.schreibeErgebnisFile("DEBUG_ZONENFEHLER.txt");
								throw new NullPointerException();
							}
							converged = ((MasterLWA)dglSys_LW).is_pSoll_Gleich_pIst(pIst, zn_LW, time);
							idxlwa++;
						}while(converged==false && idxlwa<100);
					}else{
						znTemp = LW_SOL.solveRK(zn_LW);
					}
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
				System.out.println("Relative Abweichung der Gesamtmasse: " + f_mInit);

				m_alt=m_neu;
				idx++;
			}	

			System.out.println("Masse bei Iteration " + idx2 + " = "+ m_neu + " kg");
			if(artLWA.equalsIgnoreCase("zwiko")) {
				mLuftFeucht=((LadungsWechselAnalyse)dglSys_LW).get_mLuftFeucht(zn_LW);
				//Anpassung der Ladelufttemperatur um auf die gemessenen Luftmasse zu kommen
				((LadungsWechselAnalyse)dglSys_LW).set_TSaug(mLuftFeucht);				
			}else{
				mLuftFeucht=((LadungsWechselAnalyse_ohneQb)dglSys_LW).get_mLuftFeucht(zn_LW);
				//Anpassung der Ladelufttemperatur um auf die gemessenen Luftmasse zu kommen
				((LadungsWechselAnalyse_ohneQb)dglSys_LW).set_TSaug(mLuftFeucht);
			}
			idx2+=1;
		}while(Math.abs((mLuftFeucht_mess-mLuftFeucht)/mLuftFeucht_mess)>0.005&&idx2<=50);
//		 "_" + (idx-1) +
//		dglSys_LW.schreibeErgebnisFile(cp.get_CaseName()+ "_ERGEBNISSE_LW.txt");
		dglSys_LW.schreibeErgebnisFile(cp.get_CaseName()+ ".txt");		
		System.out.println("Gesamtanzahl der benoetigeten Iterationen: " + (idx2) );	
		double agrInt;
		if(artLWA.equalsIgnoreCase("zwiko")) {
			agrInt=((LadungsWechselAnalyse)dglSys_LW).get_mAGRintern(zn_LW);
		}else{
			agrInt=((LadungsWechselAnalyse_ohneQb)dglSys_LW).get_mAGRintern(zn_LW);
		}
		return agrInt;
	}

	@Override
	public boolean involvesGasExchangeCalc() {		
		return true;
	}
	
	
}
