package berechnungsModule;

import io.FileWriter_txt;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.internesRestgas.InternesRestgas;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import matLib.Integrator;
import matLib.MatLibBase;
import misc.VektorBuffer;

public class PostProcessor {
	

	private Integrator inti;
	private double [] Qw, mb;
	private final CasePara CP;
	private ErgebnisBuffer ergB;
	private double pmi = 0;
	
	public PostProcessor(VektorBuffer dm_buffer,VektorBuffer dQb_buffer,VektorBuffer dQw_buffer, CasePara cp){
			this.CP=cp;
			
			ergB=new ErgebnisBuffer(CP,"DVA_Post_");
			inti=new Integrator();
			double dt=CP.SYS.WRITE_INTERVAL_SEC;
			Qw=inti.get_IntegralVerlauf(dt,dQw_buffer.getValues());		
			
//			double matrix [][]=new double [2][];
//			matrix[0]=mb;
//			matrix[1]=Qw;
//			FileWriter_txt txtFile = new FileWriter_txt("mb.txt");
//			txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);			
			
			String Separator  ="\n**************************************************\n";
			System.err.println("DVA_Post_Ergebnisse:"+Separator);
	
			int i=0;
					
			IndizierDaten indi=new IndizierDaten(CP);
						
			double pmi=indi.get_pmi();

			ergB.buffer_EinzelErgebnis("pmi [bar]",pmi*1e-5,i);
			System.err.println("pmi = " + pmi*1e-5 + " [bar]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("pMax [bar]",indi.get_pZyl_MAX()*1e-5,i);
			System.err.println("pMax = " + indi.get_pZyl_MAX()*1e-5 + " [bar]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Offset Zylinderdruck [Pa]", indi.get_pOffset(), i);
			System.err.println("Offset Zylinderdruck = " + indi.get_pOffset() + " [Pa]");
			
			Motor motor=CP.MOTOR;
			if(motor.isHubKolbenMotor()){
				Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
				double versatz=Math.round(10*(hkm.get_OT_Versatz()))/10.0+CP.get_OT_Versatz();
				
			i+=1;
			ergB.buffer_EinzelErgebnis("OT-Versatz [KWnZOT]", versatz, i);
			System.err.println("OT-Versatz= " + versatz + " [KWnZOT]");	
			}
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Kappa Druckabgleich Polytropenmethode", indi.get_kappa_druckabgleich(), i);
			System.err.println("Kappa Druckabgleich Polytropenmethode = " + indi.get_kappa_druckabgleich() + " [-]");
			
			double mAGR_inter=CP.RESTGASMODELL.get_mInternesRestgas_ASP();
			mAGR_inter=CP.RESTGASMODELL.get_mInternesRestgas_ASP();
			i+=1;
			ergB.buffer_EinzelErgebnis("mAGR_intern [kg]",mAGR_inter,i);
			System.err.println("mAGR_intern = " + mAGR_inter + " [kg]");

			MasterEinspritzung me=	CP.MASTER_EINSPRITZUNG;
			double mKrst=me.get_mKrst_Sum_ASP();
			double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
			double mGes= mVerbrennungsLuft+mKrst;
			
			i+=1;
			ergB.buffer_EinzelErgebnis("AGR_intern [%]",100*mAGR_inter/mGes,i);	
			System.err.println("mAGR_intern = " + 100*mAGR_inter/mGes + " [%]");
			
			double temp = cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();
			double lambda_abgas = cp.get_mLuft_feucht_ASP() / (cp.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP() * cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst() );
			i+=1;
			ergB.buffer_EinzelErgebnis("Lambda_Abgas [-]", lambda_abgas,i);
			System.err.println("Lambda_Abgas = " + lambda_abgas + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Lambda_Brennraum [-]",(mAGR_inter/mGes - lambda_abgas) / (mAGR_inter/mGes - 1) ,i);	
			System.err.println("Lambda_Brennraum = " + (mAGR_inter/mGes - lambda_abgas) / (mAGR_inter/mGes - 1) + " [-]");
			
			double umsp[]=findUmsatzPunkte(dm_buffer);
			String s;
			if(CP.SYS.IS_KW_BASIERT)
				s="[KW]";
			else
				s="[s n. RechenBeginn]";
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_1 "+ s,umsp[0],i);
			System.err.println("X_1 = " + umsp[0] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_5 "+ s,umsp[1],i);
			System.err.println("X_5 = " + umsp[1] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_10 "+ s,umsp[2],i);
			System.err.println("X_10 = " + umsp[2] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_25 "+ s,umsp[3],i);
			System.err.println("X_25 = " + umsp[3] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_50 "+ s,umsp[4],i);
			System.err.println("X_50 = " + umsp[4] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_75 "+ s,umsp[5],i);
			System.err.println("X_75 = " + umsp[5] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_90 "+ s,umsp[6],i);
			System.err.println("X_90 = " + umsp[6] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_95 "+ s,umsp[7],i);
			System.err.println("X_95 = " + umsp[7] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" X_99 "+ s,umsp[8],i);
			System.err.println("X_99 = " + umsp[8] + " [-]");
			
			double umspQ[]=findUmsatzPunkte(dQb_buffer);			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_1 "+ s,umspQ[0],i);
			System.err.println("Q_1 = " + umspQ[0] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_5 "+ s,umspQ[1],i);
			System.err.println("Q_5 = " + umspQ[1] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_10 "+ s,umspQ[2],i);
			System.err.println("Q_10 = " + umspQ[2] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_25 "+ s,umspQ[3],i);
			System.err.println("Q_25 = " + umspQ[3] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_50 "+ s,umspQ[4],i);
			System.err.println("Q_50 = " + umspQ[4] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_75 "+ s,umspQ[5],i);
			System.err.println("Q_75 = " + umspQ[5] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_90 "+ s,umspQ[6],i);
			System.err.println("Q_90 = " + umspQ[6] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_95 "+ s,umspQ[7],i);
			System.err.println("Q_95 = " + umspQ[7] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis(" Q_99 "+ s,umspQ[8],i);
			System.err.println("Q_99 = " + umspQ[8] + " [-]");
					
			MasterEinspritzung masterEinspritzung;
			masterEinspritzung=CP.MASTER_EINSPRITZUNG;
			//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
			double Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
			i+=1;
			ergB.buffer_EinzelErgebnis("Q_MAX [J]",Qmax,i);
			System.err.println("Q_MAX = " + Qmax + " [J]");		
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Q_UV [J]",get_Q_HCCO(),i);
			System.err.println("Q_UV = " + get_Q_HCCO() + " [J]");	
			
			if(CP.is_Verlustteilung()){
			double x50;;
			if(CP.SYS.IS_KW_BASIERT)
				x50=CP.convert_KW2SEC(umspQ[4]);
			else
				x50=umspQ[4];			
		
			double h[]=calcVerlustteilung(x50,Qw[Qw.length-1],pmi);	
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Qw [J]",Qw[Qw.length-1],i);
			System.err.println("Qw = " + Qw[Qw.length-1] + " [J]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("h_therm [-]",h[0],i);
			System.err.println("h_therm = " + h[0] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("h_realeLadung [-]",h[1],i);
			System.err.println("h_realeLadung = " + h[1] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("h_realerSchwerpunkt [-]",h[2],i);
			System.err.println("h_realerSchwerpunkt = " + h[2] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis( "h_HC_CO [-]",h[3],i);
			System.err.println("h_HC_CO = " + h[3] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("h_WandWaerme [-]",h[4],i);
			System.err.println("h_WandWaerme = " + h[4] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("h_indiziert [-]",h[5],i);
			System.err.println("h_indiziert = " + h[5] + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("delta_h_Ladungswechsel [-]",h[6],i);
			System.err.println("delta_h_Ladungswechsel = " + h[4] + " [-]");
			}
			
			i+=1;
			ergB.buffer_EinzelErgebnis("L_St", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst(), i);
			System.err.println("L_St = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst() + " [-]");
						
	}
	
	//Constructor für APR
	public PostProcessor(VektorBuffer dm_buffer,VektorBuffer dQb_buffer,VektorBuffer dQw_buffer, VektorBuffer p_buffer, CasePara cp){
		this.CP=cp;
		
		ergB=new ErgebnisBuffer(CP,"APR_Post_");
		inti=new Integrator();
		double dt=CP.SYS.WRITE_INTERVAL_SEC;
		Qw=inti.get_IntegralVerlauf(dt,dQw_buffer.getValues());		
		
//		double matrix [][]=new double [2][];
//		matrix[0]=mb;
//		matrix[1]=Qw;
//		FileWriter_txt txtFile = new FileWriter_txt("mb.txt");
//		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);			

		System.err.println("APR_Post_Ergebnisse:");
		System.err.println("====================================");

		int i=0;
		
		double pmi = get_p_mi(p_buffer)*1e-5; //[bar]
		ergB.buffer_EinzelErgebnis("pmi [bar]",pmi,i);
		System.err.println("pmi = " + pmi + " [bar]");
		
		i+=1;
		double pMAX = get_p_MAX(p_buffer)*1e-5; //[bar]
		ergB.buffer_EinzelErgebnis("pMax [bar]",pMAX,i);
		System.err.println("pMax = " + pMAX + " [bar]");
		
		i+=1;
		double mAGR_inter=CP.RESTGASMODELL.get_mInternesRestgas_ASP();
		ergB.buffer_EinzelErgebnis("mAGR_intern [kg]",mAGR_inter,i);
		System.err.println("mAGR_intern = " + mAGR_inter + " [kg]");		

		MasterEinspritzung me=	CP.MASTER_EINSPRITZUNG;
		double mKrst=me.get_mKrst_Sum_ASP();
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
		double mGes= mVerbrennungsLuft+mKrst;
		
		i+=1;
		ergB.buffer_EinzelErgebnis("AGR_intern [%]",100*mAGR_inter/mGes,i);	
		System.err.println("mAGR_intern = " + 100*mAGR_inter/mGes + " [%]");
		
		double temp = cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();
		double lambda_abgas = cp.get_mLuft_feucht_ASP() / (cp.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP() * cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst() );
		i+=1;
		ergB.buffer_EinzelErgebnis("Lambda_Abgas [-]", lambda_abgas,i);
		System.err.println("Lambda_Abgas = " + lambda_abgas + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Lambda_Brennraum [-]",(mAGR_inter/mGes - lambda_abgas) / (mAGR_inter/mGes - 1) ,i);	
		System.err.println("Lambda_Brennraum = " + (mAGR_inter/mGes - lambda_abgas) / (mAGR_inter/mGes - 1) + " [-]");
		
		double umsp[]=findUmsatzPunkte(dm_buffer);
		String s;
		if(CP.SYS.IS_KW_BASIERT)
			s="[KW]";
		else
			s="[s n. RechenBeginn]";
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_1 "+ s,umsp[0],i);
		System.err.println("X_1 = " + umsp[0] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_5 "+ s,umsp[1],i);
		System.err.println("X_5 = " + umsp[1] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_10 "+ s,umsp[2],i);
		System.err.println("X_10 = " + umsp[2] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_25 "+ s,umsp[3],i);
		System.err.println("X_25 = " + umsp[3] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_50 "+ s,umsp[4],i);
		System.err.println("X_50 = " + umsp[4] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_75 "+ s,umsp[5],i);
		System.err.println("X_75 = " + umsp[5] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_90 "+ s,umsp[6],i);
		System.err.println("X_90 = " + umsp[6] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_95 "+ s,umsp[7],i);
		System.err.println("X_95 = " + umsp[7] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" X_99 "+ s,umsp[8],i);
		System.err.println("X_99 = " + umsp[8] + " [-]");
		
		double umspQ[]=findUmsatzPunkte(dQb_buffer);			
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_1 "+ s,umspQ[0],i);
		System.err.println("Q_1 = " + umspQ[0] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_5 "+ s,umspQ[1],i);
		System.err.println("Q_5 = " + umspQ[1] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_10 "+ s,umspQ[2],i);
		System.err.println("Q_10 = " + umspQ[2] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_25 "+ s,umspQ[3],i);
		System.err.println("Q_25 = " + umspQ[3] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_50 "+ s,umspQ[4],i);
		System.err.println("Q_50 = " + umspQ[4] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_75 "+ s,umspQ[5],i);
		System.err.println("Q_75 = " + umspQ[5] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_90 "+ s,umspQ[6],i);
		System.err.println("Q_90 = " + umspQ[6] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_95 "+ s,umspQ[7],i);
		System.err.println("Q_95 = " + umspQ[7] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis(" Q_99 "+ s,umspQ[8],i);
		System.err.println("Q_99 = " + umspQ[8] + " [-]");
		
		MasterEinspritzung masterEinspritzung;
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
		double Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
		i+=1;
		ergB.buffer_EinzelErgebnis("Q_MAX [J]",Qmax,i);
		System.err.println("Q_MAX = " + Qmax + " [J]");	
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Q_UV [J]",get_Q_HCCO(),i);
		System.err.println("Q_UV = " + get_Q_HCCO() + " [J]");	
		
		if(CP.is_Verlustteilung()){
		double x50;;
		if(CP.SYS.IS_KW_BASIERT)
			x50=CP.convert_KW2SEC(umspQ[4]);
		else
			x50=umspQ[4];			
	
		double h[]=calcVerlustteilung(x50,Qw[Qw.length-1],pmi);	
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Qw [J]",Qw[Qw.length-1],i);
		System.err.println("Qw = " + Qw[Qw.length-1] + " [J]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("h_therm [-]",h[0],i);
		System.err.println("h_therm = " + h[0] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("h_realeLadung [-]",h[1],i);
		System.err.println("h_realeLadung = " + h[1] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("h_realerSchwerpunkt [-]",h[2],i);
		System.err.println("h_realerSchwerpunkt = " + h[2] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis( "h_HC_CO [-]",h[3],i);
		System.err.println("h_HC_CO = " + h[3] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("h_WandWaerme [-]",h[4],i);
		System.err.println("h_WandWaerme = " + h[4] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("h_indiziert [-]",h[5],i);
		System.err.println("h_indiziert = " + h[5] + " [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("delta_h_Ladungswechsel [-]",h[6],i);
		System.err.println("delta_h_Ladungswechsel = " + h[4] + " [-]");
		}
		
		i+=1;
		ergB.buffer_EinzelErgebnis("L_St", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst(), i);
		System.err.println("L_St = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst() + " [-]");
					
}
	
	public void schreibeErgebnisFile(String name){	
		ergB.schreibeErgebnisFile(name);
	}
	

	
	private double [] calcVerlustteilung(double x50,double Qw,double pmi){
		MasterEinspritzung me=	CP.MASTER_EINSPRITZUNG;
		Motor m=CP.MOTOR;
		double [] h=new double [7];
		double Hu=me.get_spezKrstALL().get_Hu_mass();	
		double mKrstAll=me.get_mKrst_Sum_ASP();
		double Q=mKrstAll*Hu;
		double Qmax=me.get_mKrst_Sum_ASP()*me.get_spezKrstALL().get_Hu_mass();	
		///////////////////////////////////
		//thermischer Wirkungsgrad
		///////////////////////////////////
		double kappa=1.4;	
		double eps=m.get_Verdichtung();
		double h0=1-1/(Math.pow(eps, kappa-1));
		
		///////////////////////////////////
		//Reale Ladung
		///////////////////////////////////
		
		//Frischgemisch als Spezies Objekt erstellen
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
		kappa=frischGemisch.get_kappa(300);		
		
		double h1=1-1/(Math.pow(eps, kappa-1));		
		
		///////////////////////////////////
		//Schwerpunktlage
		///////////////////////////////////
		//Berechnung der fiktiven Verdichtung die sich bei der berechneten Schwerpunktlage ergibt		
		double Vc=m.get_V(x50);
		eps=m.get_V_MAX()/Vc;
		double h2=1-1/(Math.pow(eps, kappa-1));
		
		
		///////////////////////////////////
		//CO/HC-Verluste
		///////////////////////////////////
		GasGemisch abgas =new GasGemisch("abgas");
		abgas.set_Gasmischung_molenBruch(
				CP.OHC_SOLVER.get_GG_molenBrueche(1e5, 300, frischGemisch));
		
		double M=abgas.get_M();
		double hc=CP.get_HC();
		double co=CP.get_CO();
		double Hu_CO=282.9*1e3; //[J/mol] aus R. Pischinger S. 93
		double Hu_HC=600*1e3;	//[J/mol] aus R. Pischinger S. 93
		double Qcohc=mGes*(hc*Hu_HC+co*Hu_CO)/M;
		double h3=Qcohc/Qmax;		
		h3=h2-h3;	
		
		///////////////////////////////////
		//Wandwaerme
		///////////////////////////////////
		double h4=Qw/Qmax;
		h4=h3-h4;		
		
		///////////////////////////////////
		//indizierter Wirkungsgrad
		///////////////////////////////////
		double Vh=0;
		if(m.isHubKolbenMotor()){
			Vh=((Motor_HubKolbenMotor)m).get_Hubvolumen();		
		}
		
		double hi=pmi*Vh/Qmax;
		
		///////////////////////////////////
		//Ladungswechsel
		///////////////////////////////////
		//hier wird dafuer gesorgt, dass die Summe immer plausibel ist!
		double h5=h4-hi;			
		
		//Ausgabe
		h[0]=h0;
		h[1]=h1;
		h[2]=h2;
		h[3]=h3;
		h[4]=h4;
		h[5]=hi;
		h[6]=h5;

		return h;
	}	
	
	
	private double get_Q_HCCO(){
	
	GasGemisch frischGemisch=new GasGemisch("Frischgemisch");
	MasterEinspritzung me=	CP.MASTER_EINSPRITZUNG;
	double mKrst=me.get_mKrst_Sum_ASP();
	double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
	double mGes= mVerbrennungsLuft+mKrst;
	
	GasGemisch abgas =new GasGemisch("abgas");
	abgas.set_Gasmischung_molenBruch(
	CP.OHC_SOLVER.get_GG_molenBrueche(1e5, 300, frischGemisch));
	
	double Qmax = me.get_mKrst_Sum_ASP()*me.get_spezKrstALL().get_Hu_mass();
	
	double M=abgas.get_M();
	double hc=CP.get_HC();
	double co=CP.get_CO();
	if(hc!=0 && co!=0){
		double Hu_CO=282.9*1e3; //[J/mol] aus R. Pischinger S. 93
	double Hu_HC=600*1e3;	//[J/mol] aus R. Pischinger S. 93
	double Qcohc=mGes*(hc*Hu_HC+co*Hu_CO)/M;
	return Qcohc;
	}
	return Double.NaN;
	}
		
	private double get_p_MAX(VektorBuffer p_buffer){
        
        double pMAX=p_buffer.getValues()[0];
        for(int i=0; i<p_buffer.getValues().length; i++){
            if(p_buffer.getValues()[i]>pMAX){
                pMAX=p_buffer.getValues()[i];
            }
        }
        return pMAX;
	}
	
	private double get_p_mi(VektorBuffer p_buffer){
		double pmi=Double.NaN;
		//TODO pmi für APR Berechnen
		return pmi;
	}
		
	private double [] findUmsatzPunkte(VektorBuffer dm_buffer){
		double [] t=dm_buffer.getZeitachse();
		double dt=t[1]-t[0];
		double [] mb=inti.get_IntegralVerlauf(dt,dm_buffer.getValues());
		double [] mb_norm=MatLibBase.normierVec(mb);
		
//		double matrix [][]=new double [3][];		
//		matrix[0]=t;
//		matrix[1]=mb;
//		matrix[2]=mb_norm;		
//		FileWriter_txt txtFile = new FileWriter_txt("umsp.txt");
//		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);
		
		double ums[]=new double [9];
		int i=0;
		int a=-1;
		//1%-Umsatzpunkt
		boolean found=false;		
		do{		
			if(mb_norm[i]>=0.01){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];	
				
			}
			i+=1;
		}while(found==false&& i<mb_norm.length);
		i=i-1; //vermeidet IndexOutOfBoundsException
		
		//5%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.05){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);
		i=i-1; //vermeidet IndexOutOfBoundsException
			
		//10%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.1){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);
		i=i-1; //vermeidet IndexOutOfBoundsException
		
		//25%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.25){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);
		i=i-1; //vermeidet IndexOutOfBoundsException

		//50%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.5){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];	
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);
		i=i-1; //vermeidet IndexOutOfBoundsException
		
		//75%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.75){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);	
		i=i-1; //vermeidet IndexOutOfBoundsException
			
		//90%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.9){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);	
		i=i-1; //vermeidet IndexOutOfBoundsException
		
		//95%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.95){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);	
		i=i-1; //vermeidet IndexOutOfBoundsException
			
		//99%-Umsatzpunkt
		found=false;		
		do{	
			if(mb_norm[i]>=0.99){
				found=true;
				a=a+1;
				if(CP.SYS.IS_KW_BASIERT)
					ums[a]=CP.convert_SEC2KW(t[i]);
				else
					ums[a]=t[i];
			}
				i+=1;
		}while(found==false&& i<mb_norm.length);	
		i=i-1; //vermeidet IndexOutOfBoundsException
		
		return ums;		
	}	

}
