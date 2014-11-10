/**
 * 
 */
package berechnungsModule.Berechnung;

import java.util.Hashtable;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import misc.VektorBuffer;

/**
 * @author eichmeier
 *
 */
public abstract class DVA extends BerechnungsModell{

	private ErgebnisBuffer ergBuffDebug;
	private double dQburn=55.5;
	private double pDiff_alt,dQburn_alt=0;
	private int dQIters=0;
	private boolean foundVerbrennungsBeginn=false;
	private double t_VerbrennungsBeginn;
	protected final IndizierDaten indiD;
	private double precission, relaxFactor;

	protected DVA(CasePara cp) {		
		super(cp,new ErgebnisBuffer(cp,"dva"));
//		Für Verlustteilung burnFileName setzen. Die Spalte wird in den einzelnen DVA-Berechnungen definiert. -- Neurohr
//		cp.set_ParaInputfile("burnFileName", "[-]", "DVA_"+cp.get_CaseName()+".txt");
		cp.set_ParaInputfile("burnFileName", "[-]", "dva_"+cp.get_CaseName()+".dva");
		indiD=new IndizierDaten(cp);
		dQburn=0;
		ergBuffDebug=new ErgebnisBuffer(cp,"dbd"); //Debug DVA, früher "DVA_DEBUG_"
		precission=CP.get_precissionPressureTraceAnalysis();
		relaxFactor=CP.get_relaxFactor();
	}	

	public abstract Zone[] ersterHSBrennraum(double time, Zone[] zonen);	



	public  boolean is_pSoll_Gleich_pIst(double pIst, Zone [] zonen ,double time){			
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
						//dQburn=-V/(1-kappa)*(dpSoll+kappa/V*p*dV); //Komischerweise konvergiert es so auch???
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


	public double get_dQburn(){
		return dQburn;
	}

	public void reset(){
		this.ergBuffDebug.clearErgebnisBuffer();
		pDiff_alt=1;
		dQIters=0;
		//		System.out.println("reset");
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
		//ist eigentlich identisch Funktioniert aber nicht für fettes Gemsich
		//					return dQburn/1/frischGemisch.get_Hu_mass();
		if(dmBurn>0)
			return dmBurn;
		else
			return 0;

	}



	public boolean isDVA(){
		return true;
	}	


	protected boolean verbrennungHatBegonnen(double time, VektorBuffer dQ_in_){
		boolean burns=false;

		//Ermitteln des Verbrennungsbeginns 
		if(CP.is_VerbrennungAutoDetect()){	
			if(foundVerbrennungsBeginn==false){
				//Berechnung des Heizverlaufs
				//Frischgemisch als Spezies Objekt erstellen
				MasterEinspritzung me=CP.MASTER_EINSPRITZUNG;
				Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();		
				Spezies krst=me.get_spezKrstALL();	
				Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
				double mKrst=me.get_mKrst_Sum_ASP();
				double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
				double mGes= mVerbrennungsLuft+mKrst;
				frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
				frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		

				GasGemisch frischGemisch=new GasGemisch("Frischgemisch");	
				frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
				Motor motor = CP.MOTOR;
				double t0=CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;		
				double dQb_buffer[]=new double[CP.SYS.ANZ_BERECHNETER_WERTE];
				double dt=CP.SYS.WRITE_INTERVAL_SEC;
				double dQmax=0;				
				int index=0;
				int indexMAX=0;
				do{
					double p0=this.indiD.get_pZyl(t0);
					double dp=(this.indiD.get_pZyl(t0+dt)-p0)/dt;			
					double V0=motor.get_V(t0);
					double dV=motor.get_dV(t0);
					double R=frischGemisch.get_R();
					double T0=p0*V0/R/mGes;
					double k=frischGemisch.get_kappa(T0);

					double dQ_in=k/(k-1)*p0*dV+1/(k-1)*V0*dp;
					dQb_buffer[index]=dQ_in;
					if(dQ_in>dQmax){
						dQmax=dQ_in;
						indexMAX=index;				
					}
					t0=t0+dt;	
					index+=1;
				}while(t0<=CP.SYS.RECHNUNGS_ENDE_DVA_SEC);
				//Ermitteln des 1%-Umsatzpunktes
				index=indexMAX;
				t_VerbrennungsBeginn=indexMAX*dt;;
				double sw=0.01;
				do{					
					if(dQb_buffer[index]<=sw*dQmax){
						foundVerbrennungsBeginn=true;
						t_VerbrennungsBeginn=t_VerbrennungsBeginn+1*dt;
					}
					t_VerbrennungsBeginn=t_VerbrennungsBeginn-dt;
					index=index-1;					
				}while(foundVerbrennungsBeginn==false&&index>=0);
				double t_VerbrennungsBeginn_temp=t_VerbrennungsBeginn;
				//Jetzt wird  nach der ersten Nullstelle vor dem 1%-Umsatzpunkt gesucht
				//Das ist dann der Verbrennungsbeginn!
				foundVerbrennungsBeginn=false;
								
				do{
					if(index>=0){
					if(dQb_buffer[index]<=0){
						foundVerbrennungsBeginn=true;
						t_VerbrennungsBeginn=t_VerbrennungsBeginn+1*dt;
					}
					t_VerbrennungsBeginn=t_VerbrennungsBeginn-dt;
					index=index-1;
					}
				}while(foundVerbrennungsBeginn==false&&index>=0);
				
				//Falls es keine Nullstelle vor dem 1%-Umsatzpunkt gibt
				//scheint das noch ein halbwegs plausibler Wert zu sein
				if(foundVerbrennungsBeginn==false){
					t_VerbrennungsBeginn=t_VerbrennungsBeginn_temp;
					foundVerbrennungsBeginn=true;
				}				

//				System.out.println(CP.convert_SEC2KW(t_VerbrennungsBeginn));
			}

		}else{
			t_VerbrennungsBeginn=CP.get_verbrennungsBeginnSEC();			
		}

		if(time>=t_VerbrennungsBeginn)
			burns=true;		

		return burns;
	}
}
