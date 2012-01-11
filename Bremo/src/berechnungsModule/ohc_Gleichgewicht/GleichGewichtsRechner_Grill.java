package berechnungsModule.ohc_Gleichgewicht;



import java.util.Hashtable;

import misc.PhysKonst;
import kalorik.spezies.Spezies;

public class GleichGewichtsRechner_Grill extends GleichGewichtsRechner{	
	
	double nN_=0;	
	double nO_nN;
	double nN_nO;
	double nC_nO;
	double nH_nO;

	
	//Gleichgewichtskonstanten
	double Kp1, Kp2, Kp3, Kp4 ,Kp5, Kp6, Kp7;		
	
	protected GleichGewichtsRechner_Grill(String gleichGewichtsKonstantenVorgabe, double T_freeze){
		super(gleichGewichtsKonstantenVorgabe,T_freeze);
	}		

		
	protected double[] calc_gg(	double p, 
								double T,
								double c_Atome, 
								double h_Atome, 
								double o_Atome, 
								double n_Atome){
			
			
		double [] adX=new double [14];	
			
//		double T_freeze=Bremo.get_casePara().SYS.T_FREEZE;
		if(T<T_FREEZE)T=T_FREEZE;	 //aus der Elternklasse geerbt	

			
		double nO_=0;
		double sqrt_pO2_start=0;

			
		double nO=o_Atome;
		double nN=n_Atome;
		double nC=c_Atome;
		double nH=h_Atome;
		
		nO_nN=nO/nN;
		nN_nO=nN/nO;
		nC_nO=nC/nO;
		nH_nO=nH/nO;


		//Berechnung der Gleichgewichtskonstanten	
		//Die Gleichgewichtskonstanten sind Dimensionsbehaftet die Janaf-Polynome liefern Werte für bar
		//hier erfolgt eine Umrechnung in Pa 
		double Kp []=new double [8];
		Kp=get_Kp(T);
		
		//CO2-->CO +1/2 O2
		Kp1=Kp[1]*Math.sqrt(1e5);	
		
		//H2+ 1/2 O2 --> H2O
		Kp2=Kp[2]/Math.sqrt(1e5);	
		
		//1/2 O2 +1/2 H2 --> OH
		Kp3=Kp[3];
		
		//1/2 H2 --> H
		Kp4=Kp[4]*Math.sqrt(1e5);	
		
		//1/2 O2 --> O
		Kp5=Kp[5]*Math.sqrt(1e5);			
		
		//1/2 N2 --> N
		Kp6=Kp[6]*Math.sqrt(1e5);
		
		//1/2 N2 + 1/2 O2 --> NO
		Kp7=Kp[7];			

		/////////////////////////////////////////////////////////////////////////////////////
		/////////////					Lösen des Gleichungssystems				/////////////
		/////////////////////////////////////////////////////////////////////////////////////
		
		
		//Startwert für N_N
		nN_=0.7*p;
		
		//alternativ
//		if (lambda>1){
//			nN_=0.95*(0.5*zeta*vol_N2_Luft/vol_O2_Luft)*p/
//			(krstAtomeC+0.5*krstAtomeH+0.5*zeta*((lambda-1)/lambda+vol_N2_Luft/vol_O2_Luft));			
//		} else{
//			nN_=0.95*(0.5*zeta*vol_N2_Luft/vol_O2_Luft)*p/
//			(krstAtomeC+0.5*krstAtomeH+0.5*zeta*vol_N2_Luft/vol_O2_Luft);	
//		}		
		//Startwert für 
		sqrt_pO2_start=0.02450*Math.sqrt(1e5);		
		
		nO_=nO_nN*nN_;
		
		//so würde es auch funktionieren!!
		//nO_=nO;

		/////////////////////////////////////////////////////////////////////////////////////
		/////////////					Newton Iteration 						/////////////
		/////////////////////////////////////////////////////////////////////////////////////
		//p_err ist eine Funktion von nO_ es wird die Nullstelle gesucht für F(nO_)=p_err=0
		double diff_nO_, nO_alt,p_err_alt,p_err;
		double delta;	
				
		nO_alt=0;
		p_err_alt=p;		

		int countIter=0;
		do {
			adX= pO2_iter (nO_,sqrt_pO2_start,p) ;
			p_err=adX[0];			
			diff_nO_=(p_err-p_err_alt)/(nO_-nO_alt);		
			delta=-p_err/diff_nO_;
			p_err_alt=p_err;
			nO_alt=nO_;
			nO_=nO_+delta;			
			
			sqrt_pO2_start=Math.sqrt(adX[8]*p);
			
			countIter++;
		}while(countIter<1000 && Math.abs(adX[0])>1E-6);		

		return adX;		
	}
	
	
	private double [] pO2_iter (double nO_, double sqrt_pO2, double p) {
		
		double nO__=0;
		double sqrt_pH2,sqrt_pN2,p_,p_err,delta_sqrt_pO2;
		double diff_no__,diff_pN2,diff_pH2;
		double pCO2=0;
		double pCO=0;
		double pO2=0;
		double pH2O=0;
		double pH2=0;
		double pOH=0;
		double pH=0;
		double pO=0;
		double pN2=0;
		double pN=0;
		double pNO=0;			
		double h1, h2, h3;
		double adX[]=new double [14];
	
		//Newtonverfahren für sqrt_pO2
		//pH2 und pN2 sind Funktionen von sqrt_pO2 damit wird auch no eine Funktion von sqrt_pO2
		//hier wird die Nullstelle gesucht für die Funktion: F(sqrt_pO2)=nO__-nO_=0
		int countIter=0;
		do{				
			h1=(Kp3*sqrt_pO2+Kp4)*(Kp3*sqrt_pO2+Kp4);				
			sqrt_pH2=(-Kp3*sqrt_pO2-Kp4+Math.sqrt(h1+8*(1+Kp2*sqrt_pO2)*nH_nO*nO_))/(4*(1+Kp2*sqrt_pO2));		
			
			diff_pH2=(-Kp3+(2*(Kp3*sqrt_pO2+Kp4)*Kp3+8*Kp2*nH_nO*nO_)/(2*Math.sqrt(h1+8*(1+Kp2*sqrt_pO2)*nH_nO*nO_)))*4*(1+Kp2*sqrt_pO2)
						-(-Kp3*sqrt_pO2-Kp4+Math.sqrt(h1+8*(1+Kp2*sqrt_pO2)*nH_nO*nO_))*4*Kp2;

			diff_pH2=diff_pH2/(16*(1+Kp2*sqrt_pO2)*(1+Kp2*sqrt_pO2));
			
			pH2=sqrt_pH2*sqrt_pH2;				
			
			h2=0.0625*(Kp6+Kp7*sqrt_pO2)*(Kp6+Kp7*sqrt_pO2);
			sqrt_pN2=-0.25*(Kp6+Kp7*sqrt_pO2)+Math.sqrt(h2+0.5*nN_nO*nO_);			
			
			diff_pN2=-0.25*Kp7+0.125*(Kp6+Kp7*sqrt_pO2)*Kp7/(2*Math.sqrt(h2+0.5*nN_nO*nO_));
			
			pO2=sqrt_pO2*sqrt_pO2;		
			
			nO__=nC_nO*nO_*(2*sqrt_pO2/Kp1+1)/(sqrt_pO2/Kp1+1)+2*pO2+Kp2*pH2*sqrt_pO2+Kp3*sqrt_pO2*sqrt_pH2
						+Kp5*sqrt_pO2+Kp7*sqrt_pN2*sqrt_pO2;
			
			h3=(sqrt_pO2/Kp1+1)*(sqrt_pO2/Kp1+1);				
			diff_no__=nC_nO*nO_*(2/Kp1*(sqrt_pO2/Kp1+1)-(2*sqrt_pO2/Kp1+1)/Kp1)/h3+4*sqrt_pO2+Kp2*(2*sqrt_pH2*diff_pH2*sqrt_pO2+pH2)
							+Kp3*(sqrt_pH2+sqrt_pO2*diff_pH2)+Kp5+Kp7*(diff_pN2*sqrt_pO2+sqrt_pN2);
							
			delta_sqrt_pO2=-(nO__-nO_)/diff_no__;	
			
			sqrt_pO2+=delta_sqrt_pO2;	
			
			if (sqrt_pO2<0){
				sqrt_pO2=0.00001;
			}

			countIter++;

		}while( countIter<1000 && Math.abs(nO__-nO_)>1E-6 );	
		
		pO2=sqrt_pO2*sqrt_pO2;
		pN2=sqrt_pN2*sqrt_pN2;
		pN=Kp6*sqrt_pN2;
		pNO=Kp7*sqrt_pN2*sqrt_pO2;
		pCO=nC_nO*nO_/(sqrt_pO2/Kp1+1);
		pCO2=pCO*sqrt_pO2/Kp1;
		pH2O=Kp2*pH2*sqrt_pO2;
		pOH=Kp3*sqrt_pO2*sqrt_pH2;
		pH=Kp4*sqrt_pH2;
		pO=Kp5*sqrt_pO2;
		
		p_=pCO2+pCO+pO2+pH2O+pH2+pOH+pH+pO+pN2+pN+pNO;
		p_err=p-p_;		
	
		adX[0]  = p_err;
		adX[1]  = pH/p;
		adX[2]  = pO/p;
		adX[3]  = pN/p;
		adX[4]  = pH2/p;
		adX[5]  = pOH/p;
		adX[6]  = pCO/p;
		adX[7]  = pNO/p;
		adX[8]  = pO2/p;
		adX[9]  = pH2O/p;
		adX[10] = pCO2/p;
		adX[11] = (pN2+p_err)/p; //damit die Summe der molenbrueche eins ergibt wird N2 angepasst!
		adX[12] = nO_;
		adX[13] = -555.55;				

		return adX;			
			
	}	
	

	public Hashtable<Spezies, Double> get_GG_molenBrueche(	double p, 
															double T, 
															Spezies frischGemisch){
		
		Hashtable<Spezies, Double> molenBruecheHash=new Hashtable<Spezies, Double>(15);
		
		double [] adX;				
		
		adX=calc_gg(p,T,
				frischGemisch.get_AnzC_Atome(),
				frischGemisch.get_AnzH_Atome(),
				frischGemisch.get_AnzO_Atome(),
				frischGemisch.get_AnzN_Atome());		
	
		molenBruecheHash.put(spezH,adX[1]);
		molenBruecheHash.put(spezO,adX[2]);
		molenBruecheHash.put(spezN,adX[3]);
		molenBruecheHash.put(spezH2,adX[4]);
		molenBruecheHash.put(spezOH,adX[5]);
		molenBruecheHash.put(spezCO,adX[6]);
		molenBruecheHash.put(spezNO,adX[7]);
		molenBruecheHash.put(spezO2,adX[8]);
		molenBruecheHash.put(spezH2O,adX[9]);
		molenBruecheHash.put(spezCO2,adX[10]);
		molenBruecheHash.put(spezN2,adX[11]);	
		
		return molenBruecheHash;
		
	}
		

	/**
	 * berechnet die Gleichgewichtskonzentration von dissoziierendem Rauchgas
	 * @param p_
	 * @param T_
	 * @param lambda 
	 * @param cAtomeKrst
	 * @param hAtomeKrst
	 * @param oAtomeKrst
	 */
	
	public Hashtable<Spezies, Double> get_GG_molenBrueche(	double p_, 
															double T_, 
															double lambda_, 
															double cAtomeKrst, 
															double hAtomeKrst, 
															double oAtomeKrst,
															double nAtomeKrst){		
		
		Hashtable<Spezies, Double> molenBruecheHash=new Hashtable<Spezies, Double>(15);
		double p, T,lambda;
		double [] adX;
		lambda=lambda_;
		T=T_;
		p=p_;				

		double krstAtomeC = cAtomeKrst;
		double krstAtomeH = hAtomeKrst;
		double krstAtomeO = oAtomeKrst;

		double O2min =krstAtomeC +krstAtomeH/4-krstAtomeO/2;

		double vol_CO2_Luft=PhysKonst.get_vol_CO2_Luft();
		double vol_O2_Luft=PhysKonst.get_vol_O2_Luft();
		double vol_N2_Luft=PhysKonst.get_vol_N2_Luft();
		double vol_Ar_Luft =PhysKonst.get_vol_Ar_Luft();			

		//Hilfsgröße		
		double zeta=2*lambda*O2min;		
		
		//Anzahl der Kohlenstoffatome im Frischgemisch
		//für die Vergleichbarkeit mit Grill muss "	0.5*zeta*vol_CO2_Luft/vol_O2_Luft" auskomentiert werden			
		double nC=krstAtomeC+0.5*zeta*vol_CO2_Luft/vol_O2_Luft;		

		//Anzahl der Wasserstoffatome im Frischgemisch
		//Wasserstoff aus Krst + Wasserstoff aus feuchter Luft + Wasserstoff aus zusaetz. Wasser
		double nH=krstAtomeH;	


		// Anzhal der Sauerstoffatome im Frischgemisch
		//Sauerstoff aus Krst + Sauerstoff aus O2-Gehalt der Luft + Sauerstoff aus CO2-Gehalt der Luft 
		//für die Vergleichbarkeit mit Grill muss "(1+vol_CO2_Luft/vol_O2_Luft)" auskomentiert werden		
		double nO=krstAtomeO+zeta*(1+vol_CO2_Luft/vol_O2_Luft);
		

		//Anzahl der Stickstoffatome im Frischgemisch
		double nN=nAtomeKrst+zeta*vol_N2_Luft/vol_O2_Luft;	
		//Damit die Rechenergebnisse mit Grill vergleichbar sind 
//		nN=3.773*zeta;

		adX=calc_gg(p,T,nC,nH,nO,nN);
		
		molenBruecheHash.put(spezH,adX[1]);
		molenBruecheHash.put(spezO,adX[2]);
		molenBruecheHash.put(spezN,adX[3]);
		molenBruecheHash.put(spezH2,adX[4]);
		molenBruecheHash.put(spezOH,adX[5]);
		molenBruecheHash.put(spezCO,adX[6]);
		molenBruecheHash.put(spezNO,adX[7]);
		molenBruecheHash.put(spezO2,adX[8]);
		molenBruecheHash.put(spezH2O,adX[9]);
		molenBruecheHash.put(spezCO2,adX[10]);
		molenBruecheHash.put(spezN2,adX[11]);	
		
		return molenBruecheHash;			
		
	}
	


}



	
	
