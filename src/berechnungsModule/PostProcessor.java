package berechnungsModule;

import io.FileWriter_txt;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.Berechnung.APR_kompletterZyklus;
import berechnungsModule.Berechnung.BerechnungsModellFabrik;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.internesRestgas.InternesRestgas;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.SwingBremo;
import matLib.Integrator;
import matLib.MatLibBase;
import misc.VektorBuffer;

public class PostProcessor {
	

	private Integrator inti;
	private double [] Qw, mb;
	private final CasePara CP;
	private ErgebnisBuffer ergB;
	private double pmi = 0;
	private double [] Qb;
	//private SwingBremo sb;

	
	public PostProcessor(VektorBuffer dm_buffer,VektorBuffer dQb_buffer,VektorBuffer dQw_buffer, CasePara cp){
			this.CP=cp;
					
			ergB=new ErgebnisBuffer(CP,"pfd"); // Post-File-DVA, früher "DVA_Post_"
			inti=new Integrator();
			double dt=CP.SYS.WRITE_INTERVAL_SEC;
			Qw=inti.get_IntegralVerlauf(dt,dQw_buffer.getValues());		
			//sb = new SwingBremo();
			
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
			double Qzu = masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass(); //Kerrom: Qmax in Qzu umbenannt
			i+=1;
			ergB.buffer_EinzelErgebnis("Q_zu [J]",Qzu,i);
			System.err.println("Q_zu = " + Qzu + " [J]");

//			i+=1;
//			ergB.buffer_EinzelErgebnis("Q_UV [J]",get_Q_HCCO(),i);
//			System.err.println("Q_UV = " + get_Q_HCCO() + " [J]");
//			stattdessen: get_Q_UV();		
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Q_UV [J]",get_Q_UV(),i);
			System.err.println("Q_UV = " + get_Q_UV() + " [J]");
			
			double Qmax = Qzu - get_Q_UV();
			i+=1;
			ergB.buffer_EinzelErgebnis("Q_max [J]",Qmax,i);
			System.err.println("Q_max = " + Qmax + " [J]");
			
		//if (CP.is_Verlustteilung()) {
			double x50;
			if (CP.SYS.IS_KW_BASIERT)
				x50 = CP.convert_KW2SEC(umspQ[4]);

			else
				x50 = umspQ[4];

			double h[] = calcVerlustteilung(x50, Qw[Qw.length - 1], pmi);

			i += 1;
			ergB.buffer_EinzelErgebnis("Qw [J]", Qw[Qw.length - 1], i);
			System.err.println("Qw = " + Qw[Qw.length - 1] + " [J]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_therm [-]", h[0], i);
			System.err.println("h_therm = " + h[0] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_realeLadung [-]", h[1], i);
			System.err.println("h_realeLadung = " + h[1] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_realerSchwerpunkt [-]", h[2], i);
			System.err.println("h_realerSchwerpunkt = " + h[2] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_HC_CO [-]", h[3], i);
			System.err.println("h_HC_CO = " + h[3] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_WandWaerme [-]", h[4], i);
			System.err.println("h_WandWaerme = " + h[4] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_indiziert [-]", h[5], i);
			System.err.println("h_indiziert = " + h[5] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("delta_h_Ladungswechsel [-]", h[6], i);
			System.err.println("delta_h_Ladungswechsel = " + h[4] + " [-]");
		//}
			
			i+=1;
			ergB.buffer_EinzelErgebnis("L_St", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst(), i);
			System.err.println("L_St = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst() + " [-]");
			
			Qb = inti.get_IntegralVerlauf(dt, dQb_buffer.getValues());
			double QbMAX = getMaximum(Qb);
			i+=1;
			ergB.buffer_EinzelErgebnis("QbMAX [J]", QbMAX, i);
			System.err.println("QbMAX = " + QbMAX + " [J]");
			
			double QwMAX =getMaximum(Qw);
			i+=1;
			ergB.buffer_EinzelErgebnis("QwMAX [J]", QwMAX, i);
			System.err.println("QwMAX = " + QwMAX + " [J]");
	
			i+=1;
			ergB.buffer_EinzelErgebnis("QhMAX [J]", QbMAX - QwMAX, i);
			System.err.println("QhMAX = " +  (QbMAX - QwMAX) + " [J]");
					
			i+=1;
			ergB.buffer_EinzelErgebnis("Anz C-Atome [-]", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzC_Atome(), i);
			System.err.println("Anz C-Atome = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzC_Atome() + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Anz H-Atome [-]", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzH_Atome(), i);
			System.err.println("Anz H-Atome = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzH_Atome() + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Anz O-Atome [-]", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzO_Atome(), i);
			System.err.println("Anz O-Atome = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzO_Atome() + " [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Anz N-Atome [-]", cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzN_Atome(), i);
			System.err.println("Anz N-Atome = " + cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzN_Atome() + " [-]");
			
			
			if (motor.isHubKolbenMotor()) {
			Motor_HubKolbenMotor hkm = ((Motor_HubKolbenMotor) motor);
			i+=1;
			ergB.buffer_EinzelErgebnis("KolbenhubMAX [m]", hkm.get_sMax(), i);
			System.err.println("KolbenhubMAX = " + hkm.get_sMax() + " [m]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Vh [m3]", hkm.get_Hubvolumen(), i);
			System.err.println("Vh = " + hkm.get_Hubvolumen() + " [m3]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Vc [m3]", hkm.get_Kompressionsvolumen(), i);
			System.err.println("Vc = " + hkm.get_Kompressionsvolumen() + " [m3]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Eps [-]", hkm.get_Verdichtung(), i);
			System.err.println("Eps=" +  hkm.get_Verdichtung()+" [-]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Eps_thermodynamisch [-]", hkm.get_Epsilon_thermo(), i);
			System.err.println("Eps_thermodynamisch=" +  hkm.get_Epsilon_thermo()+" [-]");
			}
			
			System.err.println("Berechnungs- und Steuerzeiten in KW:");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsbeginn [KW]", cp.SYS.RECHNUNGS_BEGINN_DVA_KW, i);
			System.err.println("Berechnungsbeginn = " + cp.SYS.RECHNUNGS_BEGINN_DVA_KW + " [KW]");

			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsende [KW]", cp.SYS.RECHNUNGS_ENDE_DVA_KW, i);
			System.err.println("Berechnungsende = " + cp.SYS.RECHNUNGS_ENDE_DVA_KW + " [KW]");
					
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass öffnet [KW]", cp.convert_SEC2KW(cp.get_Einlassoeffnet()), i);
			System.err.println("Einlass öffnet = " + cp.convert_SEC2KW(cp.get_Einlassoeffnet()) + " [KW]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass schließt [KW]", cp.convert_SEC2KW(cp.get_Einlassschluss()), i);
			System.err.println("Einlass schließt = " + cp.convert_SEC2KW(cp.get_Einlassschluss()) + " [KW]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass öffnet [KW]", cp.get_Auslassoeffnet_KW(), i);
			System.err.println("Auslass öffnet = " + cp.get_Auslassoeffnet_KW() + " [KW]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass schließt [KW]", cp.convert_SEC2KW(cp.get_Auslassschluss()), i);
			System.err.println("Auslass schließt = " + cp.convert_SEC2KW(cp.get_Auslassschluss()) + " [KW]");
			
			System.err.println("Berechnungs- und Steuerzeiten in s:");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsbeginn [s]", cp.SYS.RECHNUNGS_BEGINN_DVA_SEC, i);
			System.err.println("Berechnungsbeginn = " + cp.SYS.RECHNUNGS_BEGINN_DVA_SEC + " [s]");
						
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsende [s]", cp.SYS.RECHNUNGS_ENDE_DVA_SEC, i);
			System.err.println("Berechnungsende = " + cp.SYS.RECHNUNGS_ENDE_DVA_SEC + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass öffnet [s]", cp.get_Einlassoeffnet(), i);
			System.err.println("Einlass öffnet = " + cp.get_Einlassoeffnet() + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass schließt [s]", cp.get_Einlassschluss(), i);
			System.err.println("Einlass schließt = " + cp.get_Einlassschluss() + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass öffnet [s]", cp.get_Auslassoeffnet(), i);
			System.err.println("Auslass öffnet = " + cp.get_Auslassoeffnet() + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass schließt [s]", cp.get_Auslassschluss(), i);
			System.err.println("Auslass schließt = " + cp.get_Auslassschluss() + " [s]");
			
			BerechnungsModellFabrik bmf =new BerechnungsModellFabrik(cp);
			String berechnungsModellVorgabe=bmf.get_ModulWahl(bmf.DVAmodulFlag, bmf.DVA_MODULE);
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsmodell: " + berechnungsModellVorgabe, Double.NaN, i);
			System.err.println("Berechnungsmodell: " + berechnungsModellVorgabe);
			
			i += 1;
			ergB.buffer_EinzelErgebnis("bremo.svnversion: " + SwingBremo.getRevisionNumber(), Double.NaN, i);
			System.err.println("bremo.svnversion: " + SwingBremo.getRevisionNumber());					
	}


	// Constructor für APR
	public PostProcessor(VektorBuffer dm_buffer,VektorBuffer dQb_buffer,VektorBuffer dQw_buffer, VektorBuffer p_buffer, CasePara cp){
		this.CP=cp;
		
		ergB=new ErgebnisBuffer(CP,"pfa"); //Post-File-APR, früher "APR_Post_"
		inti=new Integrator();
		double dt=CP.SYS.WRITE_INTERVAL_SEC;
		Qw=inti.get_IntegralVerlauf(dt,dQw_buffer.getValues());
		//sb = new SwingBremo();

		// double matrix [][]=new double [2][];
		// matrix[0]=mb;
		// matrix[1]=Qw;
		// FileWriter_txt txtFile = new FileWriter_txt("mb.txt");
		// txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);

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
		
		double mGes, mAGR_inter;
		if(CP.BERECHNUNGS_MODELL.toString().contains("APR_kompletterZyklus")){
			double[] massen = ((APR_kompletterZyklus)CP.BERECHNUNGS_MODELL).get_massen();
			double mLuft = massen[0] * 1800 * CP.get_DrehzahlInUproSec();
			i+=1;
			ergB.buffer_EinzelErgebnis("mLuft [kg/h]",mLuft,i);
			System.err.println("mLuft = " + mLuft + " [kg/h]");
			
			i+=1;
			mGes = massen[1];
			mAGR_inter = massen[1] - massen[0] - CP.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP() - CP.get_mAGR_extern_ASP();
			ergB.buffer_EinzelErgebnis("mAGR_intern [kg]",mAGR_inter,i);
			System.err.println("mAGR_intern = " + mAGR_inter + " [kg]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("AGR_intern [%]",100*mAGR_inter/massen[1],i);	
			System.err.println("mAGR_intern = " + 100*mAGR_inter/massen[1] + " [%]");
			
		}else{
			
			i+=1;
			mAGR_inter=CP.RESTGASMODELL.get_mInternesRestgas_ASP();
			ergB.buffer_EinzelErgebnis("mAGR_intern [kg]",mAGR_inter,i);
			System.err.println("mAGR_intern = " + mAGR_inter + " [kg]");	
	
			MasterEinspritzung me=	CP.MASTER_EINSPRITZUNG;
			double mKrst=me.get_mKrst_Sum_ASP();
			double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();	
			mGes= mVerbrennungsLuft+mKrst;
			
			i+=1;
			ergB.buffer_EinzelErgebnis("AGR_intern [%]",100*mAGR_inter/mGes,i);	
			System.err.println("mAGR_intern = " + 100*mAGR_inter/mGes + " [%]");
		}
		
		double temp = cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();
		double lambda_abgas = cp.get_mLuft_feucht_ASP()
				/ (cp.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP() * cp.MASTER_EINSPRITZUNG
						.get_spezKrstALL().get_Lst());
		i += 1;
		ergB.buffer_EinzelErgebnis("Lambda_Abgas [-]", lambda_abgas, i);
		System.err.println("Lambda_Abgas = " + lambda_abgas + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis("Lambda_Brennraum [-]",
				(mAGR_inter / mGes - lambda_abgas) / (mAGR_inter / mGes - 1), i);
		System.err.println("Lambda_Brennraum = "
				+ (mAGR_inter / mGes - lambda_abgas) / (mAGR_inter / mGes - 1)
				+ " [-]");

		double umsp[] = findUmsatzPunkte(dm_buffer);
		String s;
		if (CP.SYS.IS_KW_BASIERT)
			s = "[KW]";
		else
			s = "[s n. RechenBeginn]";
		i += 1;
		ergB.buffer_EinzelErgebnis(" X_1 " + s, umsp[0], i);
		System.err.println("X_1 = " + umsp[0] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_5 " + s, umsp[1], i);
		System.err.println("X_5 = " + umsp[1] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_10 " + s, umsp[2], i);
		System.err.println("X_10 = " + umsp[2] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_25 " + s, umsp[3], i);
		System.err.println("X_25 = " + umsp[3] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_50 " + s, umsp[4], i);
		System.err.println("X_50 = " + umsp[4] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_75 " + s, umsp[5], i);
		System.err.println("X_75 = " + umsp[5] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_90 " + s, umsp[6], i);
		System.err.println("X_90 = " + umsp[6] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_95 " + s, umsp[7], i);
		System.err.println("X_95 = " + umsp[7] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" X_99 " + s, umsp[8], i);
		System.err.println("X_99 = " + umsp[8] + " [-]");

		double umspQ[] = findUmsatzPunkte(dQb_buffer);
		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_1 " + s, umspQ[0], i);
		System.err.println("Q_1 = " + umspQ[0] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_5 " + s, umspQ[1], i);
		System.err.println("Q_5 = " + umspQ[1] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_10 " + s, umspQ[2], i);
		System.err.println("Q_10 = " + umspQ[2] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_25 " + s, umspQ[3], i);
		System.err.println("Q_25 = " + umspQ[3] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_50 " + s, umspQ[4], i);
		System.err.println("Q_50 = " + umspQ[4] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_75 " + s, umspQ[5], i);
		System.err.println("Q_75 = " + umspQ[5] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_90 " + s, umspQ[6], i);
		System.err.println("Q_90 = " + umspQ[6] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_95 " + s, umspQ[7], i);
		System.err.println("Q_95 = " + umspQ[7] + " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis(" Q_99 " + s, umspQ[8], i);
		System.err.println("Q_99 = " + umspQ[8] + " [-]");

		MasterEinspritzung masterEinspritzung;
		masterEinspritzung = CP.MASTER_EINSPRITZUNG;
		// die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder
		// auf 25°C abgekuehlt wird
		double Qzu = masterEinspritzung.get_mKrst_Sum_ASP()
				* masterEinspritzung.get_spezKrstALL().get_Hu_mass(); // Kerrom:
																		// Qmax
																		// in
																		// Qzu
																		// umbenannt
		i += 1;
		ergB.buffer_EinzelErgebnis("Q_zu [J]", Qzu, i);
		System.err.println("Q_zu = " + Qzu + " [J]");

		// i+=1;
		// ergB.buffer_EinzelErgebnis("Q_UV [J]",get_Q_HCCO(),i);
		// System.err.println("Q_UV = " + get_Q_HCCO() + " [J]"); //stattdessen:
		// get_Q_UV() (berücksichtigt auch H2 und C)

		i += 1;
		ergB.buffer_EinzelErgebnis("Q_UV [J]", get_Q_UV(), i);
		System.err.println("Q_UV = " + get_Q_UV() + " [J]");

		double Qmax = Qzu - get_Q_UV();
		i += 1;
		ergB.buffer_EinzelErgebnis("Qmax [J]", Qmax, i);
		System.err.println("Qmax = " + Qmax + " [J]");

		if (CP.is_Verlustteilung()) {
			double x50;
			if (CP.SYS.IS_KW_BASIERT)
				x50 = CP.convert_KW2SEC(umspQ[4]);
			else
				x50 = umspQ[4];

			double h[] = calcVerlustteilung(x50, Qw[Qw.length - 1], pmi);

			i += 1;
			ergB.buffer_EinzelErgebnis("Qw [J]", Qw[Qw.length - 1], i);
			System.err.println("Qw = " + Qw[Qw.length - 1] + " [J]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_therm [-]", h[0], i);
			System.err.println("h_therm = " + h[0] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_realeLadung [-]", h[1], i);
			System.err.println("h_realeLadung = " + h[1] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_realerSchwerpunkt [-]", h[2], i);
			System.err.println("h_realerSchwerpunkt = " + h[2] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_HC_CO [-]", h[3], i);
			System.err.println("h_HC_CO = " + h[3] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_WandWaerme [-]", h[4], i);
			System.err.println("h_WandWaerme = " + h[4] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("h_indiziert [-]", h[5], i);
			System.err.println("h_indiziert = " + h[5] + " [-]");

			i += 1;
			ergB.buffer_EinzelErgebnis("delta_h_Ladungswechsel [-]", h[6], i);
			System.err.println("delta_h_Ladungswechsel = " + h[4] + " [-]");
		}

		i += 1;
		ergB.buffer_EinzelErgebnis("L_St", cp.MASTER_EINSPRITZUNG
				.get_spezKrstALL().get_Lst(), i);
		System.err.println("L_St = "
				+ cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst() + " [-]");

		Qb = inti.get_IntegralVerlauf(dt, dQb_buffer.getValues());
		double QbMAX = getMaximum(Qb);
		i += 1;
		ergB.buffer_EinzelErgebnis("QbMAX [J]", QbMAX, i);
		System.err.println("QbMAX = " + QbMAX + " [J]");

		double QwMAX = getMaximum(Qw);
		i += 1;
		ergB.buffer_EinzelErgebnis("QwMAX [J]", QwMAX, i);
		System.err.println("QwMAX = " + QwMAX + " [J]");

		i += 1;
		ergB.buffer_EinzelErgebnis("QhMAX [J]", QbMAX - QwMAX, i);
		System.err.println("QhMAX = " + (QbMAX - QwMAX) + " [J]");

		i += 1;
		ergB.buffer_EinzelErgebnis("Anz C-Atome [-]", cp.MASTER_EINSPRITZUNG
				.get_spezKrstALL().get_AnzC_Atome(), i);
		System.err.println("Anz C-Atome = "
				+ cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzC_Atome()
				+ " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis("Anz H-Atome [-]", cp.MASTER_EINSPRITZUNG
				.get_spezKrstALL().get_AnzH_Atome(), i);
		System.err.println("Anz H-Atome = "
				+ cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzH_Atome()
				+ " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis("Anz O-Atome [-]", cp.MASTER_EINSPRITZUNG
				.get_spezKrstALL().get_AnzO_Atome(), i);
		System.err.println("Anz O-Atome = "
				+ cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzO_Atome()
				+ " [-]");

		i += 1;
		ergB.buffer_EinzelErgebnis("Anz N-Atome [-]", cp.MASTER_EINSPRITZUNG
				.get_spezKrstALL().get_AnzN_Atome(), i);
		System.err.println("Anz N-Atome = "
				+ cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_AnzN_Atome()
				+ " [-]");

		Motor motor = CP.MOTOR;
		if (motor.isHubKolbenMotor()) {
			Motor_HubKolbenMotor hkm = ((Motor_HubKolbenMotor) motor);
			i += 1;
			ergB.buffer_EinzelErgebnis("KolbenhubMAX [m]", hkm.get_sMax(), i);
			System.err.println("KolbenhubMAX = " + hkm.get_sMax() + " [m]");

			i += 1;
			ergB.buffer_EinzelErgebnis("Vh [m]", hkm.get_Hubvolumen(), i);
			System.err.println("Vh = " + hkm.get_Hubvolumen() + " [m]");

			i += 1;
			ergB.buffer_EinzelErgebnis("Vc [m]", hkm.get_Kompressionsvolumen(),	i);
			System.err.println("Vc = " + hkm.get_Kompressionsvolumen() + " [m]");
		}	
				
			
			System.err.println("Berechnungs- und Steuerzeiten in KW:");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsbeginn [KW]", cp.SYS.RECHNUNGS_BEGINN_DVA_KW, i);
			System.err.println("Berechnungsbeginn = " + cp.SYS.RECHNUNGS_BEGINN_DVA_KW + " [KW]");

			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsende [KW]", cp.SYS.RECHNUNGS_ENDE_DVA_KW, i);
			System.err.println("Berechnungsende = " + cp.SYS.RECHNUNGS_ENDE_DVA_KW + " [KW]");
					
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass öffnet [KW]", cp.convert_SEC2KW(cp.get_Einlassoeffnet()), i);
			System.err.println("Einlass öffnet = " + cp.convert_SEC2KW(cp.get_Einlassoeffnet()) + " [KW]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass schließt [KW]", cp.convert_SEC2KW(cp.get_Einlassschluss()), i);
			System.err.println("Einlass schließt = " + cp.convert_SEC2KW(cp.get_Einlassschluss()) + " [KW]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass öffnet [KW]", cp.get_Auslassoeffnet_KW(), i);
			System.err.println("Auslass öffnet = " + cp.get_Auslassoeffnet_KW() + " [KW]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass schließt [KW]", cp.convert_SEC2KW(cp.get_Auslassschluss()), i);
			System.err.println("Auslass schließt = " + cp.convert_SEC2KW(cp.get_Auslassschluss()) + " [KW]");
			
			System.err.println("Berechnungs- und Steuerzeiten in s:");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsbeginn [s]", cp.SYS.RECHNUNGS_BEGINN_DVA_SEC, i);
			System.err.println("Berechnungsbeginn = " + cp.SYS.RECHNUNGS_BEGINN_DVA_SEC + " [s]");
						
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsende [s]", cp.SYS.RECHNUNGS_ENDE_DVA_SEC, i);
			System.err.println("Berechnungsende = " + cp.SYS.RECHNUNGS_ENDE_DVA_SEC + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass öffnet [s]", cp.get_Einlassoeffnet(), i);
			System.err.println("Einlass öffnet = " + cp.get_Einlassoeffnet() + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Einlass schließt [s]", cp.get_Einlassschluss(), i);
			System.err.println("Einlass schließt = " + cp.get_Einlassschluss() + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass öffnet [s]", cp.get_Auslassoeffnet(), i);
			System.err.println("Auslass öffnet = " + cp.get_Auslassoeffnet() + " [s]");
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Auslass schließt [s]", cp.get_Auslassschluss(), i);
			System.err.println("Auslass schließt = " + cp.get_Auslassschluss() + " [s]");
			
			BerechnungsModellFabrik bmf =new BerechnungsModellFabrik(cp);
			String berechnungsModellVorgabe=bmf.get_ModulWahl(bmf.DVAmodulFlag, bmf.DVA_MODULE);
			
			i+=1;
			ergB.buffer_EinzelErgebnis("Berechnungsmodell: " + berechnungsModellVorgabe, Double.NaN, i);
			System.err.println("Berechnungsmodell: " + berechnungsModellVorgabe);
			
			i += 1;
			ergB.buffer_EinzelErgebnis("bremo.svnversion: " + SwingBremo.getRevisionNumber(), Double.NaN, i);
			System.err.println("bremo.svnversion: " + SwingBremo.getRevisionNumber());
				
}
	
	public PostProcessor(VektorBuffer dQb_buffer,VektorBuffer dQw_buffer, CasePara cp){
		this.CP=cp;
				
		ergB=new ErgebnisBuffer(CP,"pfd"); // Post-File-DVA, früher "DVA_Post_"
		inti=new Integrator();
		double dt=CP.SYS.WRITE_INTERVAL_SEC;
		Qw=inti.get_IntegralVerlauf(dt,dQw_buffer.getValues());		

		//sb = new SwingBremo();
		
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
		
		Qb = inti.get_IntegralVerlauf(dt, dQb_buffer.getValues());
		double QbMAX = getMaximum(Qb);
		i+=1;
		ergB.buffer_EinzelErgebnis("QbMAX [J]", QbMAX, i);
		System.err.println("QbMAX = " + QbMAX + " [J]");
		
		double QwMAX =getMaximum(Qw);
		i+=1;
		ergB.buffer_EinzelErgebnis("QwMAX [J]", QwMAX, i);
		System.err.println("QwMAX = " + QwMAX + " [J]");

		i+=1;
		ergB.buffer_EinzelErgebnis("QhMAX [J]", QbMAX - QwMAX, i);
		System.err.println("QhMAX = " +  (QbMAX - QwMAX) + " [J]");
		
		if (motor.isHubKolbenMotor()) {
		Motor_HubKolbenMotor hkm = ((Motor_HubKolbenMotor) motor);
		i+=1;
		ergB.buffer_EinzelErgebnis("KolbenhubMAX [m]", hkm.get_sMax(), i);
		System.err.println("KolbenhubMAX = " + hkm.get_sMax() + " [m]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Vh [m3]", hkm.get_Hubvolumen(), i);
		System.err.println("Vh = " + hkm.get_Hubvolumen() + " [m3]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Vc [m3]", hkm.get_Kompressionsvolumen(), i);
		System.err.println("Vc = " + hkm.get_Kompressionsvolumen() + " [m3]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Eps [-]", hkm.get_Verdichtung(), i);
		System.err.println("Eps=" +  hkm.get_Verdichtung()+" [-]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Eps_thermodynamisch_kompr [-]", hkm.get_Epsilon_thermo(), i);
		System.err.println("Eps_thermodynamisch_kompr=" +  hkm.get_Epsilon_thermo()+" [-]");
		
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Eps_thermodynamisch_sym [-]", hkm.get_Epsilon_thermo(), i);
		System.err.println("Eps_thermodynamisch_sym=" +  hkm.get_Epsilon_thermo_sym()+" [-]");
		}
		
		System.err.println("Berechnungs- und Steuerzeiten in KW:");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Berechnungsbeginn [KW]", cp.SYS.RECHNUNGS_BEGINN_DVA_KW, i);
		System.err.println("Berechnungsbeginn = " + cp.SYS.RECHNUNGS_BEGINN_DVA_KW + " [KW]");

		i+=1;
		ergB.buffer_EinzelErgebnis("Berechnungsende [KW]", cp.SYS.RECHNUNGS_ENDE_DVA_KW, i);
		System.err.println("Berechnungsende = " + cp.SYS.RECHNUNGS_ENDE_DVA_KW + " [KW]");
				
		i+=1;
		ergB.buffer_EinzelErgebnis("Einlass öffnet [KW]", cp.convert_SEC2KW(cp.get_Einlassoeffnet()), i);
		System.err.println("Einlass öffnet = " + cp.convert_SEC2KW(cp.get_Einlassoeffnet()) + " [KW]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Einlass schließt [KW]", cp.convert_SEC2KW(cp.get_Einlassschluss()), i);
		System.err.println("Einlass schließt = " + cp.convert_SEC2KW(cp.get_Einlassschluss()) + " [KW]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Auslass öffnet [KW]", cp.get_Auslassoeffnet_KW(), i);
		System.err.println("Auslass öffnet = " + cp.get_Auslassoeffnet_KW() + " [KW]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Auslass schließt [KW]", cp.convert_SEC2KW(cp.get_Auslassschluss()), i);
		System.err.println("Auslass schließt = " + cp.convert_SEC2KW(cp.get_Auslassschluss()) + " [KW]");
		
		System.err.println("Berechnungs- und Steuerzeiten in s:");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Berechnungsbeginn [s]", cp.SYS.RECHNUNGS_BEGINN_DVA_SEC, i);
		System.err.println("Berechnungsbeginn = " + cp.SYS.RECHNUNGS_BEGINN_DVA_SEC + " [s]");
					
		i+=1;
		ergB.buffer_EinzelErgebnis("Berechnungsende [s]", cp.SYS.RECHNUNGS_ENDE_DVA_SEC, i);
		System.err.println("Berechnungsende = " + cp.SYS.RECHNUNGS_ENDE_DVA_SEC + " [s]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Einlass öffnet [s]", cp.get_Einlassoeffnet(), i);
		System.err.println("Einlass öffnet = " + cp.get_Einlassoeffnet() + " [s]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Einlass schließt [s]", cp.get_Einlassschluss(), i);
		System.err.println("Einlass schließt = " + cp.get_Einlassschluss() + " [s]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Auslass öffnet [s]", cp.get_Auslassoeffnet(), i);
		System.err.println("Auslass öffnet = " + cp.get_Auslassoeffnet() + " [s]");
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Auslass schließt [s]", cp.get_Auslassschluss(), i);
		System.err.println("Auslass schließt = " + cp.get_Auslassschluss() + " [s]");
		
		BerechnungsModellFabrik bmf =new BerechnungsModellFabrik(cp);
		String berechnungsModellVorgabe=bmf.get_ModulWahl(bmf.DVAmodulFlag, bmf.DVA_MODULE);
		
		i+=1;
		ergB.buffer_EinzelErgebnis("Berechnungsmodell: " + berechnungsModellVorgabe, Double.NaN, i);
		System.err.println("Berechnungsmodell: " + berechnungsModellVorgabe);
		
		i += 1;
		ergB.buffer_EinzelErgebnis("bremo.svnversion: " + SwingBremo.getRevisionNumber(), Double.NaN, i);
		System.err.println("bremo.svnversion: " + SwingBremo.getRevisionNumber());
					
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
		double Qzu=me.get_mKrst_Sum_ASP()*me.get_spezKrstALL().get_Hu_mass();	//Kerrom: Qmax in Qzu umbenannt
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

//		Kerrom: Heizwerte auskommentiert und in CasePara ausgelagert
//		double Hu_CO=282.9*1e3; //[J/mol] aus R. Pischinger S. 93 
//		double Hu_HC=600*1e3;	//[J/mol] aus R. Pischinger S. 93
//		double Hu_HC=2044.2*1e3;	//Ge Liu 
//		double Hu_HC=2222*1e3;	//Ho
		
		double Qcohc=mGes*(hc*CP.HU_HC+co*CP.HU_CO)/M;		//Kerrom: Heizwerte durch Parameter aus CasePara ersetzt
		double h3=Qcohc/Qzu;		
		h3=h2-h3;	
		
		///////////////////////////////////
		//Wandwaerme
		///////////////////////////////////
		double h4=Qw/Qzu;
		h4=h3-h4;		
		
		///////////////////////////////////
		//indizierter Wirkungsgrad
		///////////////////////////////////
		double Vh=0;
		if(m.isHubKolbenMotor()){
			Vh=((Motor_HubKolbenMotor)m).get_Hubvolumen();		
		}
		
		double hi=pmi*Vh/Qzu;
		
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

	// Kerrom: auskommentiert, stattdessen Berechnung von Q_UV (berücksichtigt
	// auch H2 und C)
	// private double get_Q_HCCO(){
	// GasGemisch frischGemisch = new GasGemisch("Frischgemisch");
	// MasterEinspritzung me = CP.MASTER_EINSPRITZUNG;
	// double mKrst = me.get_mKrst_Sum_ASP();
	// double mVerbrennungsLuft = CP.get_mVerbrennungsLuft_ASP();
	// double mGes = mVerbrennungsLuft + mKrst;
	//
	// GasGemisch abgas = new GasGemisch("abgas");
	// abgas.set_Gasmischung_molenBruch(CP.OHC_SOLVER.get_GG_molenBrueche(1e5,
	// 300, frischGemisch));
	//
	// // double Qmax =
	// // me.get_mKrst_Sum_ASP()*me.get_spezKrstALL().get_Hu_mass();
	//
	// double M = abgas.get_M();
	// double hc=CP.get_HC();
	// double co=CP.get_CO();
	// if (hc != 0 && co != 0) {
	// double Hu_CO = 282.9 * 1e3; // [J/mol] aus R. Pischinger S. 93
	// double Hu_HC=600*1e3; //[J/mol] aus R. Pischinger S. 93
	// double Hu_HC = 2044.2 * 1e3; // Ge Liu
	// double Hu_HC=2222*1e3; //Ho
	// double Qcohc = mGes * (hc * Hu_HC + co * Hu_CO) / M;
	// return Qcohc;
	// }
	// return Double.NaN;
	// }

	/** Wärmemenge der unverbrannten Abgasbestandteile 
	 * Q_UV = mGes * (co*Hu_CO + h2 * Hu_H2 + hc * Hu_HC+ c * Hu_C) / M in [J]
	 * 	<br> h2, c, co, hc =  Volumenbrüche, M = Molmasse
	 * @return Q_UV (oder 0, wenn alle Volumenbrüche = 0)
	 */
	private double get_Q_UV() {

		MasterEinspritzung me = CP.MASTER_EINSPRITZUNG;
		GasGemisch frischGemisch = new GasGemisch("Frischgemisch");
		GasGemisch abgas = new GasGemisch("abgas");
		abgas.set_Gasmischung_molenBruch(CP.OHC_SOLVER.get_GG_molenBrueche(1e5, 300, frischGemisch));
		
		double mGes = CP.get_mVerbrennungsLuft_ASP() + me.get_mKrst_Sum_ASP();
		double M = abgas.get_M(); // [kg/mol]
		double h2 = CP.get_H2();
		double c = CP.get_C();
		double hc = CP.get_HC();
		double co = CP.get_CO();
		
		return mGes * (co*CP.HU_CO + h2 * CP.HU_H2 + hc * CP.HU_HC+ c * CP.HU_C) / M; // [J]
	
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

	/**
	 * @param v Array
	 * @return Maximum des Arrays v
	 */
	private double getMaximum(double [] v){
		double max = v[0];
		for (int i=0; i < v.length; i++){
			if (v[i]> max){
				max = v[i];
			}
		}
		return max;
	}
}
