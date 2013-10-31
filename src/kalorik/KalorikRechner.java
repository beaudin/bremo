package kalorik;


import java.util.Hashtable;

import misc.PhysKonst;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.main.Bremo;
import bremo.parameter.CasePara;



public class KalorikRechner {

	private GleichGewichtsRechner GGsolver;
	
	
	public KalorikRechner(CasePara cp) {
		GGsolver=cp.OHC_SOLVER;
	}
	
	
	public Hashtable<String, Double> berechneRauchgasKalorik_mass(double p,
			double T, 
			double lambda, 
			double cKrst , 
			double hKrst, 
			double oKrst, 
			double nKrst ) {

		double u, h,h_h298, u_temp, dudT, dudp, dudlambda, R, R_temp, dRdT, dRdp, dRdlambda, kappa, cv, cp, M;
		//Ergebnis HashTable
		Hashtable<String, Double> kalorikHash = new Hashtable<String, Double>(18);		
		GasGemisch rauchgas;		

		
		rauchgas =new GasGemisch(GGsolver.get_GG_molenBrueche(p, T, lambda, cKrst,hKrst,oKrst,nKrst),"rauchgas");
		
		u = rauchgas.get_u_mass(T);		
		h=rauchgas.get_h_mass(T);	
		h_h298=rauchgas.get_h_h298_mass(T);	
		R = rauchgas.get_R();			
		kappa = rauchgas.get_kappa(T);
		M=rauchgas.get_M();	
		cv=rauchgas.get_cv_mass(T);
		cp=rauchgas.get_cp_mass(T);

		double delta_T=1;
		// Berechnung von dudT und dRdT
		//if-Abfrage nur dann sinnvoll wenn Einfriertemperatur unter 1000K 
		//oder wenn Luft als Rauchgas mit sehr großem Lambda modelliert wird
		if (T > 1000 && T <= 1001) {
			delta_T=-1;
		} 		
		rauchgas.set_Gasmischung_molenBruch(
				GGsolver.get_GG_molenBrueche(p,
				T -delta_T, 
				lambda,
				cKrst,
				hKrst,
				oKrst,
				nKrst)
			);
		
		u_temp =  rauchgas.get_u_mass(T-delta_T);
		R_temp = rauchgas.get_R();	
		dudT = (u-u_temp ) / delta_T;
		dRdT = (R  - R_temp) / delta_T;			

		// Berechnung von dudp und dRdp
		double delta_p=5000;
		rauchgas.set_Gasmischung_molenBruch(
				GGsolver.get_GG_molenBrueche(p+delta_p,
				T, 
				lambda,
				cKrst,
				hKrst,
				oKrst,
				nKrst)
			);
		
		u_temp = rauchgas.get_u_mass(T);
		R_temp = rauchgas.get_R();
		dudp = (u_temp - u) / delta_p;
		dRdp = (R_temp - R) / delta_p;

		// Berechnung von dudlambda und dRdlambda
		// Formeln aus Diss. Grill
		double delta_L = 0.0002 * lambda * lambda;
		if (delta_L < 0.001)
			delta_L = 0.001;
		if (delta_L > 30)
			delta_L = 30;		
		rauchgas.set_Gasmischung_molenBruch(
				GGsolver.get_GG_molenBrueche(p,
				T, 
				lambda+delta_L,
				cKrst,
				hKrst,
				oKrst,
				nKrst)
			);
		
		u_temp = rauchgas.get_u_mass(T);
		R_temp = rauchgas.get_R();
		dudlambda = (u_temp - u) / delta_L;
		dRdlambda = (R_temp - R) / delta_L;

		kalorikHash.put("h", h);
		kalorikHash.put("h_h298",h_h298);
		kalorikHash.put("u", u);
		kalorikHash.put("dudT", dudT);
		kalorikHash.put("dudp", dudp);
		kalorikHash.put("dudlambda", dudlambda);
		kalorikHash.put("R", R);
		kalorikHash.put("dRdT", dRdT);
		kalorikHash.put("dRdp", dRdp);
		kalorikHash.put("dRdlambda", dRdlambda);
		kalorikHash.put("kappa", kappa);
		kalorikHash.put("M",M);
		kalorikHash.put("cv",cv);
		kalorikHash.put("cp",cp);
		return kalorikHash;


		
	}
	
	
	
	public Hashtable<String, Double> berechneRauchgasKalorik_mol(double p,
			double T, 
			double lambda, 
			double cKrst , 
			double hKrst, 
			double oKrst, 
			double nKrst ) {

		double u, h,h_h298, u_temp, dudT, dudp, dudlambda, R, R_temp, dRdT, dRdp, dRdlambda, kappa, cv, cp, M;
		//Ergebnis HashTable
		Hashtable<String, Double> kalorikHash = new Hashtable<String, Double>(18);		
		GasGemisch rauchgas;		

		
		rauchgas =new GasGemisch(GGsolver.get_GG_molenBrueche(p, T, lambda, cKrst,hKrst,oKrst,nKrst),"rauchgas");
		
		u = rauchgas.get_u_mol(T);		
		h=rauchgas.get_h_mol(T);
		h_h298=rauchgas.get_h_h298_mol(T);
		R = rauchgas.get_R();			
		kappa = rauchgas.get_kappa(T);
		M=rauchgas.get_M();	
		cv=rauchgas.get_cv_mass(T);
		cp=rauchgas.get_cp_mass(T);

		double delta_T=1;
		// Berechnung von dudT und dRdT
		//if-Abfrage nur dann sinnvoll wenn Einfriertemperatur unter 1000K 
		//oder wenn Luft als Rauchgas mit sehr großem Lambda modelliert wird
		if (T > 1000 && T <= 1001) {
			delta_T=-1;
		} 		
		rauchgas.set_Gasmischung_molenBruch(
				GGsolver.get_GG_molenBrueche(p,
				T -delta_T, 
				lambda,
				cKrst,
				hKrst,
				oKrst,
				nKrst)
			);
		
		u_temp =  rauchgas.get_u_mol(T-delta_T);
		R_temp = rauchgas.get_R();	
		dudT = (u-u_temp ) / delta_T;
		dRdT = (R  - R_temp) / delta_T;			

		// Berechnung von dudp und dRdp
		double delta_p=5000;
		rauchgas.set_Gasmischung_molenBruch(
				GGsolver.get_GG_molenBrueche(p+delta_p,
				T, 
				lambda,
				cKrst,
				hKrst,
				oKrst,
				nKrst)
			);
		
		u_temp = rauchgas.get_u_mol(T);
		R_temp = rauchgas.get_R();
		dudp = (u_temp - u) / delta_p;
		dRdp = (R_temp - R) / delta_p;

		// Berechnung von dudlambda und dRdlambda
		// Formeln aus Diss. Grill
		double delta_L = 0.0002 * lambda * lambda;
		if (delta_L < 0.001)
			delta_L = 0.001;
		if (delta_L > 30)
			delta_L = 30;		
		rauchgas.set_Gasmischung_molenBruch(
				GGsolver.get_GG_molenBrueche(p,
				T, 
				lambda+delta_L,
				cKrst,
				hKrst,
				oKrst,
				nKrst)
			);
		
		u_temp = rauchgas.get_u_mol(T);
		R_temp = rauchgas.get_R();
		dudlambda = (u_temp - u) / delta_L;
		dRdlambda = (R_temp - R) / delta_L;

		kalorikHash.put("h", h);
		kalorikHash.put("h_h298",h_h298);
		kalorikHash.put("u", u);
		kalorikHash.put("dudT", dudT);
		kalorikHash.put("dudp", dudp);
		kalorikHash.put("dudlambda", dudlambda);
		kalorikHash.put("R", R);
		kalorikHash.put("dRdT", dRdT);
		kalorikHash.put("dRdp", dRdp);
		kalorikHash.put("dRdlambda", dRdlambda);
		kalorikHash.put("kappa", kappa);
		kalorikHash.put("M",M);
		kalorikHash.put("cv",cv);
		kalorikHash.put("cp",cp);
		return kalorikHash;
	}

	
	public Hashtable<String, Double> berechneRauchgasKalorik_mol(double p,
			double T, 
			double lambda, 
			Spezies krst ) {
		
		return berechneRauchgasKalorik_mol(p,T,lambda,
				krst.get_AnzC_Atome(),
				krst.get_AnzH_Atome(),
				krst.get_AnzO_Atome(),
				krst.get_AnzN_Atome());		
	}
	
	public Hashtable<String, Double> berechneRauchgasKalorik_mol(double p,
			double T, 
			Spezies frischGemisch ) {
		
		double lambda=0,o2,n2,cKrst,hKrst;
		//Bei vernachlässigung von CO2 in der Luft kann der C-Anteil nur aus dem Kraftstoff kommen
		cKrst=frischGemisch.get_AnzC_Atome();
		//Alles was H enthält wird dem Kraftstoff zugerechnet
		hKrst=frischGemisch.get_AnzH_Atome();
		o2=0.5*frischGemisch.get_AnzO_Atome();
		n2=0.5*frischGemisch.get_AnzN_Atome();
				
//		ABSCHÄTZUNG des Lambda-Wertes aus der Zusammensetzung der Ladung
//		Wenn der Ladung beispielsweise Wasser beigemischt ist, wird 
//		dies behandelt wie eine WasserKraftstoffemulsion.
//		Dies gilt für alle Komponenten die c,h oder o-Atome enthalten
//		Berechnung erfolgt auf Basis der linken Seite der allgemeinen Verbrennungsgleichung
//		CxHyOz+LAM*(x+y/4-z/2)*(O2+3,773N2)-->...
//		CO2 wird nicht berücksichtigt
		double A=PhysKonst.get_vol_N2_Luft()/PhysKonst.get_vol_O2_Luft();
		lambda=n2/(A*(cKrst+hKrst/4+n2/A-o2));
		
		return berechneRauchgasKalorik_mol(p,T,lambda,
				frischGemisch.get_AnzC_Atome(),
				frischGemisch.get_AnzH_Atome(),
				frischGemisch.get_AnzO_Atome(),
				frischGemisch.get_AnzN_Atome());		
		}
	
	public Hashtable<String, Double> berechneRauchgasKalorik_mass(double p,
			double T, 
			Spezies frischGemisch ) {
		
		double lambda=0,o2,n2,cKrst,hKrst;
		cKrst=frischGemisch.get_AnzC_Atome();
		hKrst=frischGemisch.get_AnzH_Atome();
		o2=0.5*frischGemisch.get_AnzO_Atome();
		n2=0.5*frischGemisch.get_AnzN_Atome();
				
//		ABSCHÄTZUNG des Lambda-Wertes aus der Zusammensetzung der Ladung
//		Wenn der Ladung beispielsweise Wasser beigemischt ist, wird 
//		dies behandelt wie eine WasserKraftstoffemulsion.
//		Dies gilt für alle Komponenten die c,h oder o-Atome enthalten
//		Berechnung erfolgt auf Basis der linken Seite der allgemeinen Verbrennungsgleichung
//		CxHyOz+LAM*(x+y/4-z/2)*(O2+3,773N2)-->...
//		CO2 wird nicht berücksichtigt
		double A=PhysKonst.get_vol_N2_Luft()/PhysKonst.get_vol_O2_Luft();
		lambda=n2/(A*(cKrst+hKrst/4+n2/A-o2));
		
		return berechneRauchgasKalorik_mass(p,T,lambda,
				frischGemisch.get_AnzC_Atome(),
				frischGemisch.get_AnzH_Atome(),
				frischGemisch.get_AnzO_Atome(),
				frischGemisch.get_AnzN_Atome());		
		}
	
	
	public Hashtable<String, Double> berechneRauchgasKalorik_mass(double p,
			double T, 
			double lambda, 
			Spezies krst ) {
		
		return berechneRauchgasKalorik_mass(p,T,lambda,
				krst.get_AnzC_Atome(),
				krst.get_AnzH_Atome(),
				krst.get_AnzO_Atome(),
				krst.get_AnzN_Atome());		
	}
	
	public static void calc_Lambda(){
		
		
		
	}

	
}
