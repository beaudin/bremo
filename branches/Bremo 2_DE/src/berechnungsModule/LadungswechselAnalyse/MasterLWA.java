package berechnungsModule.LadungswechselAnalyse;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import misc.VektorBuffer;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;

/**Masterklasse für die LWA, genau wie DVA. Für die Berechnung des Brennverlaufs in der Zwischenkompression.
 * @author neurohr
 *
 */
public abstract class MasterLWA extends BerechnungsModell{

	protected final IndizierDaten indiD;
	private double dQburn;
	private double pDiff_alt,dQburn_alt=0;
	private int dQIters;
	private ErgebnisBuffer ergBuffDebug;
	private double precission, relaxFactor;

	
	protected MasterLWA(CasePara cp, ErgebnisBuffer ergBuf) {
		super(cp, ergBuf);
		indiD = new IndizierDaten(cp);
		dQburn = 0;
		ergBuffDebug=new ErgebnisBuffer(cp,"LWA_DEBUG_");
		precission=CP.get_precissionPressureTraceAnalysis();
		relaxFactor=CP.get_relaxFactor();
	}

	public boolean isDVA() {
		return false;
	}


	protected abstract Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN);

	
	public boolean is_pSoll_Gleich_pIst(double pIst, Zone [] zonen ,double time){
		
		if(time<=CP.get_Auslassschluss() || time>=CP.get_Einlassoeffnet()+CP.SYS.DAUER_ASP_SEC){
			this.dQburn = 0;
			return true;
		}
		boolean converged;
		double pSoll=indiD.get_pZyl(time);
		double pDiff=Math.abs(pSoll-pIst);

		if(pDiff<=precission){				
			converged= true;
		}else{	
			converged= false;
			Motor m=CP.MOTOR;
			double delta_time=CP.SYS.WRITE_INTERVAL_SEC;				

			//Berechnung des Startwerts
			if(this.dQIters==0){
				dQburn=0;
				dQburn_alt=0;
				for(int znIdx=0;znIdx<zonen.length;znIdx++){
					if(zonen[znIdx].get_m()>CP.SYS.MINIMALE_ZONENMASSE){
						Spezies gg=zonen[znIdx].get_ggZone();
						double T = zonen[znIdx].get_T();
						double kappa = gg.get_kappa(T);						
						double V=m.get_V(time); //[m^3]
						double dV=m.get_dV(time)/1; //[m^3/s]
						double p=zonen[znIdx].get_p();
						double dpSoll=(pSoll-p)/delta_time;
						dQburn+=(-V/(1-kappa)*(dpSoll+kappa/V*p*dV))*V/m.get_V(time);					
					}
				}
			}else{

				double dpDiff=(pDiff-pDiff_alt)/(dQburn-dQburn_alt);
				dQburn_alt=dQburn;
				pDiff_alt=pDiff;

				dQburn=dQburn-relaxFactor*pDiff/dpDiff;
			}
			dQIters+=1;
		}	

		if(CP.SYS.DUBUGGING_MODE){
			int i=-1;
			i+=1;
			ergBuffDebug.buffer_EinzelErgebnis("Zeit", CP.convert_SEC2KW(time), i);	
			i+=1;
			ergBuffDebug.buffer_EinzelErgebnis("dQIters", dQIters, i);	
			i+=1;
			ergBuffDebug.buffer_EinzelErgebnis("relaxationsFaktor",relaxFactor, i);
			i+=1;
			ergBuffDebug.buffer_EinzelErgebnis("pSoll", pSoll, i);
			i+=1;
			ergBuffDebug.buffer_EinzelErgebnis(	"pIst", pIst, i);
			i+=1;
			ergBuffDebug.buffer_EinzelErgebnis("dQ", dQburn, i);
		}


		if(converged){
			if(Math.abs(time-CP.SYS.DUBUGGING_TIME_SEC)<0.5*CP.SYS.WRITE_INTERVAL_SEC){ 
				System.out.println("I am plotting...");
				schreibe_DUBUGGING_Ergebnisse(CP.get_CaseName()+
						"_Iter_"+CP.convert_SEC2KW(time)+".txt"	);
			}
			this.reset();
		}			

		return converged;
	}
	
	public void schreibe_DUBUGGING_Ergebnisse(String name) {
		ergBuffDebug.schreibeErgebnisFile(name);
		dQIters=0;
		this.reset();
	}
	
	
	public void reset(){
		this.ergBuffDebug.clearErgebnisBuffer();
		pDiff_alt=1;
		dQIters=0;
	}
	
	public double get_dQburn(){
		return dQburn;
	}
	
	/**
	 * 
	 * @param dQburn
	 * @param frischGemisch
	 * @param Tu
	 * @param Tv
	 * @param p
	 * @return
	 */
	protected double convert_dQburn_2_dmKrstBurn(double dQburn, Spezies frischGemisch,
			double Tu,												
			double p){	
		double dmBurn=0;
		GleichGewichtsRechner gg=CP.OHC_SOLVER;
		GasGemisch abgas=new GasGemisch("Abgas");	
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, Tu, frischGemisch));

		//1.HS fuer durchstroemten Kontrollraum:
		//m*he-m*ha-Qb=0
		double he=frischGemisch.get_h_mass(Tu);
		double ha=abgas.get_h_mass(Tu);
		double delta_h=he-ha;

		dmBurn= 1*dQburn/delta_h;
		
		if(dmBurn>0)
			return dmBurn;
		else
			return 0;

	}
}
