package funktionenTests;



import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.gemischbildung.Kraftstoff_Eigenschaften;
import berechnungsModule.gemischbildung.Verdampfung_Hiroyasu;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import kalorik.KalorikRechner;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.KoeffizientenSpezies;
import kalorik.spezies.Spezies;
import kalorik.spezies.KoeffizientenSpeziesFabrik;
import io.*;
import matLib.Integrator;
import matLib.MatLibBase;
import misc.HeizwertRechner;
import misc.LittleHelpers;
import misc.PhysKonst;


public class FunktionsTester {
	
	
	public static void test_Vektor(){
		double v []=new double [15];
		for(int i=0; i<15;i++) v[i]=i;
		
		double a[];
		
		a=MatLibBase.normierVec(v);
		
		Integrator inti=new Integrator();
		double c [];
		c=inti.get_IntegralVerlauf(1, v);
	
		 double [][] matrix=new double [3][];
		 
		 matrix[0]=v;
		 matrix[1]=a;
		 matrix[2]=c;
		 
		

		FileWriter_txt txtFile = new FileWriter_txt("vektor");
		txtFile.writeMatrixToFile(MatLibBase.transp_2d_array(matrix), false);			
		
	}
	
	
	
	
	public static void test_get_lambda(){
//		Spezies krst=KoeffizientenSpeziesFabrik.get_spezDiesel();
		Spezies krst=KoeffizientenSpeziesFabrik.get_spezRON_95();
//		Spezies krst=KoeffizientenSpeziesFabrik.get_spezH2();
		Spezies luft=KoeffizientenSpeziesFabrik.get_spezLuft_trocken();
		Spezies wasser=KoeffizientenSpeziesFabrik.get_spezH2O();
		
		double lambda=2;
	
		double lst=krst.get_Lst();	
		double mk=1; //[kg]
		double mL_tr=lambda*mk*lst;
		double mW=mL_tr*0.1;
		double m_ges=mk+mL_tr+mW; 
		
		
		

		//Gemisch erzeugen
		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(luft, mL_tr/m_ges);	
		gemisch_MassenbruchHash.put(wasser, Double.NaN/m_ges);	
		GasGemisch gemisch=new GasGemisch("gg");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
		//Verbrennen
		GleichGewichtsRechner gg=GleichGewichtsRechner.get_Instance(Bremo.get_casePara()); 
		GasGemisch abgas=new GasGemisch("Abgas");			
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(1e5, 1000, gemisch)); 
		
		System.out.println("Lambda Vorgabe: "+lambda);
		System.out.println("lambda: "+gemisch.get_lambda());
		System.out.println("lambda (aus Abgas):" +abgas.get_lambda());
		System.out.println("==============");
		System.out.println("jetzt mit AGR");
		System.out.println("==============");
		double mAGR=0.4*m_ges;
		m_ges=m_ges+mAGR;
		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(luft, mL_tr/m_ges);	
		gemisch_MassenbruchHash.put(wasser, mW/m_ges);	
		gemisch_MassenbruchHash.put(abgas, mAGR/m_ges);
		
		//Verbrennen
		gg=GleichGewichtsRechner.get_Instance(Bremo.get_casePara()); 		
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(1e5, 1000, gemisch)); 
		
		System.out.println("Lambda Vorgabe: "+lambda);
		System.out.println("lambda: "+gemisch.get_lambda());
		System.out.println("lambda (aus Abgas):" +abgas.get_lambda());	

		System.out.println("fertig");
		
		
	}
	
	
	public static void testVerdampfung(){
		String krstFlag="ndodecan";
		Kraftstoff_Eigenschaften krstProp =new Kraftstoff_Eigenschaften(krstFlag);
		Verdampfung_Hiroyasu verdampf=new Verdampfung_Hiroyasu(krstProp);
		double []m_D_T=new double [3];
		double []m_D_T_0=new double [3];
		double p=1.5e5;
		double T=293;
		double V=1;
		double m=p*V/PhysKonst.get_R_Luft()/T;
		Spezies luft=KoeffizientenSpeziesFabrik.get_spezLuft_trocken();
		Zone zn =new Zone(p,V,T,m,luft,false,0);	
		double pInj=1200*1e5;
		double deltaP=pInj-p;
		double rhoZyl_0=p/zn.get_T()/zn.get_ggZone().get_R();
		
		double rhoK_fl=krstProp.get_rhoK(T);
		double viskositaet_krst=krstProp.get_mue_krst();
		double kin_viskositaet_krst=viskositaet_krst/rhoK_fl;
		
		double smd=0.5*6156e-6*Math.pow(kin_viskositaet_krst,0.385)*Math.pow(rhoK_fl,0.737)*
					Math.pow(rhoZyl_0,0.06)*Math.pow(deltaP/1000,-0.54);

		double T0=293;
		double D0=smd;
		double V0=Math.PI*D0*D0*D0/6;
		double m0=krstProp.get_rhoK(T0)*V0;
		m_D_T[0]=m0;
		m_D_T[1]=D0;
		m_D_T[2]=T0;
		m_D_T_0[0]=m0;
		m_D_T_0[1]=D0;
		m_D_T_0[2]=1;
		double v_relativ=6.7;

		double erg []=new double[m_D_T.length+1];
		FileWriter_txt txtFile=new FileWriter_txt("m_D_T.txt");
		String [] header={"t","m/m0","D/D0","T"};
		txtFile.writeTextLineToFile(header, false);

		for(int t=0;t<600;t++){
			double delta_time=1/1e6d;
			double time=delta_time+t*delta_time;
			System.out.println("time:" +time);
		

			double []temp=verdampf.get_m_D_T(zn,0,delta_time,v_relativ,m_D_T);
			m_D_T[0]=temp[0];
			m_D_T[1]=temp[1];
			m_D_T[2]=temp[2];
			
			erg[0]=time*1e3;
			for(int i=0;i<m_D_T.length;i++)	
				erg[1+i]=m_D_T[i]/m_D_T_0[i];
			
			txtFile.writeLineToFile(erg,true);

		}
		
	}

	public static void calc_cp_Dilution(){		
		
		
		CasePara cp=Bremo.get_casePara(); //wenn die funktion mal wo anders stehen soll.....
		double T_Bezug=1700;
		//Daten aus 20110617_VergleichVerduennungAGR.xls
		double m_Otto=93.1e-6;
		double m_Diesel=4.33E-06;
		double T_feuchtMessung=18.1+273.12;
		double phi=14.4/100; //relative Luftfeuchte

//////////////////////////////////////////////////////////		
////////////////Verduennung mit Luft//////////////////////		
//////////////////////////////////////////////////////////

		double []mLuft={3549.053E-6,	3744.410E-6,	4018.985E-6,	
				4309.772E-6,	4497.438E-6,	4772.626E-6,	5125.771E-6}; //feuchte Luftmasse pro ASP
		double []p_feuchtMessung={2248.000E2,	2362.000E2,	
				2500.000E2,	2666.000E2,	2769.000E2,	2914.000E2,	3111.000E2}; //Ladedruck in Pa	
		double []mAGRex={43.811E-6,	34.723E-6,	31.756E-6,	27.389E-6,	19.534E-6,	22.736E-6,	28.496E-6};	


////////////////////////////////////////////////////////////		
//////////////Verduennung mit AGR mit Lambda=ct/////////////		
////////////////////////////////////////////////////////////
//		double []mLuft={3560.053E-6,	3546.717E-6,	3489.698E-6,	3546.978E-6,	3505.648E-6,
//				3527.381E-6,	3505.386E-6}; //feuchte Luftmasse pro ASP
//		double []p_feuchtMessung={2250.000E2,	2321.000E2,	2415.000E2,	2571.000E2,
//				2704.000E2,	2893.000E2,	3069.000E2}; //Ladedruck in Pa	
//		double []mAGRex={24.720E-6,	185.112E-6,	390.356E-6,	632.457E-6,	884.522E-6,	1195.913E-6,1527.627E-6};	

////////////////////////////////////////////////////////////
//////////////Ersatz von Luft durch  AGR ////////////////		
////////////////////////////////////////////////////////////		
//	double []mLuft={3556.898E-6,3389.813E-6,3249.131E-6,3032.347E-6,
//			2901.336E-6,2701.077E-6}; //feuchte Luftmasse pro ASP
//	double []p_feuchtMessung={2246.0E2,	2246.0E2,	2246.0E2,	2246.0E2,
//			2246.0E2,	2246.0E2}; //Druck am Gaszaheler in Pa
//	double []mAGRex={44.144E-6,182.171E-6,363.495E-6,533.246E-6,
//			733.545E-6,	907.850E-6};		
		
		
		
		double cp_mass[]=new double[mLuft.length];
		double kappa[]=new double[mLuft.length];

		double mW;
		double mKrst=m_Otto+m_Diesel; 
		GasGemisch krst = null,abgas=null;
		for(int idx=0; idx<mLuft.length;idx++){

			Spezies otto=KoeffizientenSpeziesFabrik.get_spezRON_95();			
			Spezies diesel=KoeffizientenSpeziesFabrik.get_spezDiesel();
			Hashtable<Spezies,Double>krstGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
			krstGemisch_MassenBruchHash.put(otto, m_Otto/mKrst);			
			krstGemisch_MassenBruchHash.put(diesel, m_Diesel/mKrst);
			krst=new GasGemisch("krs");
			krst.set_Gasmischung_massenBruch(krstGemisch_MassenBruchHash);



			double pws=PhysKonst.get_saettigunsDampfdruck_H2O(T_feuchtMessung);			
			//Wasserbeladung
			double x=PhysKonst.get_R_Luft()/PhysKonst.get_R_H2O()*(pws/(p_feuchtMessung[idx]/phi-pws));
			mW=mLuft[idx]*(x/(1+x));
			double mLuft_tr=mLuft[idx]-mW; //trockene Luftmasse pro ASP

			double mFG=mLuft_tr+mW+mKrst;	//gesamte Masse des Verbrennenden Gases
			Hashtable<Spezies,Double>frischGemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
			//trockene Luft
			frischGemisch_MassenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezLuft_trocken(),mLuft_tr/mFG);
			//Wasser
			frischGemisch_MassenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezH2O(),mW/mFG);
			//Kraftstoff
			frischGemisch_MassenBruchHash.put(krst,mKrst/mFG);

			//Erstellen der Frischgemischspezies
			GasGemisch frischGemsich=new GasGemisch("Frischgemisch");
			frischGemsich.set_Gasmischung_massenBruch(frischGemisch_MassenBruchHash);


			//Bestimmen der AGR-Zusammensetzung --> das Hinzufuegen von AGR hat keinen Einfluss auf die 
			//AGR-Zusammensetzung da die Anzahl der c/h/o-Atome gleich bleibt und nur vom Frischgemisch abhaengt
			GleichGewichtsRechner gg=GleichGewichtsRechner.get_Instance(cp); 
			abgas=new GasGemisch("Abgas");			
			abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(1e5, 1000, frischGemsich)); //T=1000K<T_Freeze

			//Bestimmung der Verbrennungsluftzusammensetzung		
			double mAGRin=0; //wird nicht berücksichtigt
			double mAGR=mAGRex[idx]+mAGRin;
			double mGes=mLuft_tr+mW+mAGR+mKrst; //gesamte Masse im Zylinder
			Hashtable<Spezies,Double>gemisch_MassenBruchHash=new Hashtable<Spezies,Double>();
			gemisch_MassenBruchHash.put(abgas, mAGR/mGes);
			gemisch_MassenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezLuft_trocken(),mLuft_tr/mGes);		
			gemisch_MassenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezH2O(),mW/mGes);
			gemisch_MassenBruchHash.put(krst,mKrst/mGes);
			GasGemisch gemisch=new GasGemisch("Verbrennungsluft");
			gemisch.set_Gasmischung_massenBruch(gemisch_MassenBruchHash);			

			cp_mass[idx]=gemisch.get_cp_mass(T_Bezug);		
			kappa[idx]=gemisch.get_kappa(T_Bezug);
			
			System.out.println("cp Abgas: " +abgas.get_cp_mass(T_Bezug));
			System.out.println("cp Gemsich: " +gemisch.get_cp_mass(T_Bezug));
		}


		FileWriter_txt txtFile = new FileWriter_txt("cpDilution.txt");
		txtFile.writeLineToFile(cp_mass, true);	
		txtFile.writeLineToFile(kappa, true);	
		System.out.println(KoeffizientenSpeziesFabrik.get_spezLuft_trocken().get_kappa(T_Bezug));
		System.out.println("cp Kraftstoff: "+krst.get_cp_mass(T_Bezug));
		
		System.out.println("h Otto: "+KoeffizientenSpeziesFabrik.get_spezRON_95().get_h_mass(T_Bezug));
		System.out.println("cp Otto: "+KoeffizientenSpeziesFabrik.get_spezRON_95().get_cp_mass(T_Bezug));
		
		System.out.println("h Diesel: "+KoeffizientenSpeziesFabrik.get_spezDiesel().get_h_mass(T_Bezug));	
		System.out.println("cp Diesel: "+KoeffizientenSpeziesFabrik.get_spezDiesel().get_cp_mass(T_Bezug));
		
		System.out.println("cp H2O: " +KoeffizientenSpeziesFabrik.get_spezH2O().get_cp_mass(T_Bezug));
		System.out.println("cp CO2: " +KoeffizientenSpeziesFabrik.get_spezCO2().get_cp_mass(T_Bezug));

	}

	
	
	
	

	public static void test_Xialings_HiroYasu(){		

		//		Spray_Hiroyasu spray=new Spray_Hiroyasu(0.05,100,0.001,0.25,10,20);
		//
		//		spray.sayHello();
		//		double s=spray.get_s(1, 10,0.8);
		//		System.out.println("der Wert von s beträgt :" + s);

		System.out.println("fertig");		
	}

	public static void test_neueSpezies(){
		Spezies test=KoeffizientenSpeziesFabrik.get_spezCO2();
		System.out.println("CO2 wurde erzeugt und die Anzahl der Spezies ist :"+
				Spezies.get_NmbrOfAllSpez());
		System.out.println("Index von CO2: " + test.get_index());

		Spezies test2=KoeffizientenSpeziesFabrik.get_spezDiesel();
		System.out.println("Diesel wurde erzeugt und die Anzahl der Spezies ist :"+
				Spezies.get_NmbrOfAllSpez());
		System.out.println("Der Name von Spezies 1 ist: "
				+Spezies.get_Spez(1).get_name());



		Spezies test3=KoeffizientenSpeziesFabrik.get_spezCO2();
		System.out.println("Index von " + test3.get_name() + " ist: " + test3.get_index());

		System.out.println("Der Name von Spezies 4 ist: "
				+Spezies.get_Spez(4).get_name());

	}



	public  static void test_HashAdd(){	
		Spezies CO2=KoeffizientenSpeziesFabrik.get_spezCO2();
		Spezies CO2_2=CO2;
		Spezies CO2_3=KoeffizientenSpeziesFabrik.get_spezCO2();	
		Spezies O2=KoeffizientenSpeziesFabrik.get_spezO2();		

		Hashtable<Spezies, Double> ht1 = new Hashtable<Spezies, Double>(15);
		ht1.put(CO2, 0.1); 

		System.out.println("HashCode von CO2: " + CO2.hashCode());
		System.out.println("HashCode von CO2_2: " + CO2_2.hashCode());

		Hashtable<Spezies, Double> ht2 = new Hashtable<Spezies, Double>(15);
		ht2.put(CO2_2, 1D);

		System.out.println( ht2.containsKey (CO2));


		Hashtable<Spezies, Double> ht3=ht1;
		ht1=LittleHelpers.addiereHashs(ht1, ht2, 1);

		GasGemisch gg=new GasGemisch(ht2,"GasGemisch");
		Hashtable<Spezies, Double> ht4=gg.get_speziesMassenBruecheDetail();

		ht2=LittleHelpers.addiereHashs(ht3, ht3, 1);

	}




	private static Hashtable<String, Vector<Double>> ergebnisHash=new Hashtable<String, Vector<Double>>();
	public static void test_ErgebnisHash(){

		Vector<Double> vec =new Vector<Double>();

		//ergebnisHash.put("Test",new Vector<Double>());
		String s="Test";
		vec.add(1d);
		vec.add(2d);
		System.out.println(vec.size());
		System.out.println(vec.get(1));
		double a=ergebnisHash.get("Test").lastElement();
		System.out.println(a);
	}

	public static void test_HUbKolbenMotor(){

		CasePara cp =Bremo.get_casePara();
		Motor m=Motor.get_Instance();

		double time,kw,test;
		double[][] values=new double [7200][4];

		for(int i=0;i<7200;i++){		
			kw=(i-3600);
			kw=kw/10;
			values[i][0]=kw;
			time=cp.convert_KW2SEC(kw);
			values[i][1]=time;
			values[i][2]=m.get_V(time);
			values[i][3]=m.get_dV(time);

		}

		FileWriter_txt txtW= new FileWriter_txt(cp.get_workingDirectory()+"motorTest.txt");
		String [] header ={"KW","sec","V","dV"};
		txtW.writeTextLineToFile(header, false);
		txtW.writeMatrixToFile(values, true);
		System.out.println("MotorTest abgeschlossen");

	}






	public static void test_IndizierDaten(){
		CasePara cp =Bremo.get_casePara();
		IndizierDaten indiD=new IndizierDaten(cp);
		System.out.println("Rechenbeginn in KW: " +cp.SYS.RECHNUNGS_BEGINN_DVA_KW);
		System.out.println("Rechenbeginn in s: " +cp.SYS.RECHNUNGS_BEGINN_DVA_SEC);		
		System.out.println("Rechenende in KW: " +cp.SYS.RECHNUNGS_ENDE_DVA_KW);
		System.out.println("Rechenende in s: " +cp.SYS.RECHNUNGS_ENDE_DVA_SEC);

		System.out.println(	"\n	==============Los geht's==============\n"+
		"	======================================");

		double time=0;		
		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Zylinderdruck betraegt: " + indiD.get_pZyl(time) + " [Pa]");

		System.out.println(	"\n	==============Rundungsfehler==============\n"+
		"	==========================================");

		System.out.println("Zeit gerundet");
		time=0.020888889;	
		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Einlassdruck betraegt: " + indiD.get_pEin(time) + " [Pa]");

		System.out.println("Zeit exakt");
		time=cp.convert_KW2SEC(20.2);		
		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Einlassdruck betraegt: " + indiD.get_pEin(time) + " [Pa]");

		System.out.println(	"\n	===========Interpolieren==================\n"+
		"	==========================================");	



		time=(cp.convert_KW2SEC(-360)+cp.convert_KW2SEC(-359.9))/2;		
		System.out.println("Der Zeitpunkt t="+ time+" [s n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Einlassdruck betraegt: " + indiD.get_pEin(time) + " [Pa]");




		System.out.println(	"\n	=====Letzten und ersten Wert abfragen====\n"+
		"	=========================================");	
		System.out.println("letzter Wert");
		time=cp.convert_KW2SEC(359.9);		
		System.out.println("Der Zeitpunkt t="+ time+" [ms n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Zylinderdruck betraegt: " + indiD.get_pAus(time) + " [Pa]");

		System.out.println("erster Wert");
		time=cp.convert_KW2SEC(-360);
		System.out.println("Der Zeitpunkt t="+ time + " [ms n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Zylinderdruck betraegt: " + indiD.get_pAus(time) + " [Pa]");

		System.out.println("\n	==============Fehlermeldung===============\n"+
		"	==========================================");	

		time=cp.convert_KW2SEC(360); //-360.1 geht auch ;-)			
		System.out.println("Der Zeitpunkt t="+ time+" [ms n. RB] entspricht " + cp.convert_SEC2KW(time) + " [KW] \n" +
				"Der Zylinderdruck betraegt: " + indiD.get_pZyl(time) + " [Pa]");

	}

	public static void test_pZylReader(){
		String pZylFile=("L://_DISS//Bremo//BremoXP//Bremo//src//pZyl.txt");
		IndizierFileReader_txt idfr=new IndizierFileReader_txt(pZylFile,2,720);
		System.out.println(idfr.get_pZylMAX());
	}

	public static void testHeizwertRechner(){
//
//		Spezies krst=KoeffizientenSpeziesFabrik.get_spezDiesel();
		Spezies krst=KoeffizientenSpeziesFabrik.get_spezH2();
		
		
		double lambda=1;	
		double lst=krst.get_Lst();
		double mk=1; //[kg]
		double mL_tr=lambda*mk*lst;
		double m_ges=mk+mL_tr;

		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();

		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(KoeffizientenSpeziesFabrik.get_spezLuft_trocken(), mL_tr/m_ges);		
		GasGemisch gemisch=new GasGemisch("Gemisch");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
	
		System.out.println("direkte Ausgabe des KraftstoffHeizwerts/mGes: " +krst.get_Hu_mass()*mk/m_ges);
		System.out.println("direkte Ausgabe des GemischHeizwerts: " +gemisch.get_Hu_mass());
		
		System.out.println("Berechnung des GemischHeizwerts: " +
				HeizwertRechner.calcGemischHeizwert_chemGLGW_mass(gemisch, 298.15, 1e5));
		
		double Hu=HeizwertRechner.calcHup_chemGLGW(krst, 273.15 +25, lambda, 1e5);
		System.out.println("Der mittels des chemGleichgewichts und Uebergabe des Kraftstoffs berechnete GemischHeizwert HuG " +
				" von " + krst.get_name()+ " betraegt: "+ (Hu/krst.get_M()*mk/m_ges) + " [J/kg]");		
	

		System.out.println("Der mittels des chemGleichgewichts und Uebergabe des Kraftstoffs berechnete Heizwert Hu " +
				" von " + krst.get_name()+ " betraegt: "+ (Hu/krst.get_M())*1e-6 + " [MJ/kg]");
		
		
		double Hu_0=HeizwertRechner.calcGemischHeizwert_chemGLGW_mass(gemisch, 273.15+25, 1e5);

		System.out.println("Der mittels des GasGemsichs und chmGLGW berechnete Heizwert Hu " +
				" von " + krst.get_name()+ " betraegt: "+ Hu_0*m_ges*1e-6 + " [MJ/kg]");		
		
		
		
		//		System.out.println(PhysKonst.get_mass_O2_Luft());
		//		System.out.println("Lst von " + spez.get_name()+ " btraegt: "+spez.get_Lst() );		
	

		double Hu_2=HeizwertRechner.calcHup_perfekt(krst);
		System.out.println("Der mittels perfektem Abgas berechnete Heizwert Hu " +
				" von " + krst.get_name()+ " betraegt: "+ Hu_2*1e-6 + " [MJ/mol]");
		
		System.out.println("Der hinterlegte  Heizwert Hu " +
				" von " + krst.get_name()+ " betraegt: "+ krst.get_Hu_mol()*1e-6 + " [MJ/mol]");

		double Hu_3=HeizwertRechner.calcHup_perfekt(krst,298.15);
		System.out.println("Der mittels perfektem Abgas und bezugsTemp=298.15 berechnete Heizwert Hu " +
				" von " + krst.get_name()+ " betraegt: "+ Hu_3*1e-6 + " [MJ/mol]");

		double Hu_4=HeizwertRechner.calcHup_perfekt(krst,1398.15);
		System.out.println("Der mittels perfektem Abgas und bezugsTemp=1398K berechnete Heizwert Hu " +
				" von " + krst.get_name()+ " betraegt: "+ Hu_4*1e-6 + " [MJ/mol]");
	}


	public static void testAdiabateVerbrennungsTemp(){
		Spezies krst=KoeffizientenSpeziesFabrik.get_spezDiesel();
//		Spezies krst=KoeffizientenSpeziesFabrik.get_spezH2();
		Spezies luft=KoeffizientenSpeziesFabrik.get_spezLuft_trocken();
		double p=1e5; //Pa
		double T=298.15; //K	
		double lambda=1.00;
		double Tb;
		double lst=krst.get_Lst();	
		double mk=1; //[kg]
		double mL_tr=lambda*mk*lst;
		double m_ges=mk+mL_tr; 
		

		//Gemisch erzeugen
		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(luft, mL_tr/m_ges);	

		GasGemisch gemisch=new GasGemisch("Gemisch");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);
		
		System.out.println(gemisch.get_O2_min());
		
		
		System.out.println("============================\n============================");		
		System.out.println("Berechnung der adiabaten Verbrennungstemperatur von "+krst.get_name()+ 
				" mit Luft bei Tu=" + T +" [K] und pu=" + p +" [Pa] sowie Lambda="+lambda);
		System.out.println("============================\n============================");

		//Perfekte Verbrennung
		System.out.println("Perfekte Verbrennung mit Luft: " + HeizwertRechner.calcadiabateFlammenTempPerfekt(krst, T, lambda) + "[K]");
		
		System.out.println("Perfekte Verbrennung mit Luft (Uebergabe des Gemischs): " + 
				HeizwertRechner.calcadiabateFlammenTempPerfekt(gemisch, T) + "[K]");


		System.out.println("Chemische Gleichgewicht (Uebergabe des Krafstoffs): " + 
				HeizwertRechner.calcAdiabateFlammenTemp(krst, p, T, lambda));		
		

		Tb=HeizwertRechner.calcAdiabateFlammenTemp(gemisch, p, T);
		System.out.println("Chemisches Gleichgewicht (Uebergabe des Luft/ Kraftstoffgemischs): "
				+ Tb +" [K]");
		
		
		

		System.out.println("Literaturangabe fuer H2 nach Maas: 2380 [K]");
		System.out.println("============================\n============================");
//		System.out.println("Berechnung der adiabaten Verbrennungstemperatur von "+krst.get_name()+ 
//				" mit O2 bei Tu=" + T +" [K] und pu=" + p +" [Pa] und Lambda="+lambda);
//
//		double o2min=krst.get_O2_min();	
//		double nKrst=1; //[mol]
//		double schummelFaktor=1e-1;
//		double nO2=lambda*nKrst*o2min-schummelFaktor; //[mol]
//		double nN2=schummelFaktor*0.9;
//		double nCO2=schummelFaktor*0.1;
//		double nGes=nO2+nKrst+nN2+nCO2;
//
//		Spezies o2=KoeffizientenSpeziesFabrik.get_spezO2();
//		Spezies n2=KoeffizientenSpeziesFabrik.get_spezN2();
//		Spezies co2=KoeffizientenSpeziesFabrik.get_spezCO2();
//
//		Hashtable<Spezies,Double> gemisch_MolenbruchHash=new Hashtable<Spezies,Double>();		
//		gemisch_MolenbruchHash.put(krst, nKrst/nGes);
//		gemisch_MolenbruchHash.put(o2, nO2/nGes);
//		gemisch_MolenbruchHash.put(n2, nN2/nGes);
//		gemisch_MolenbruchHash.put(co2, nCO2/nGes);
//
//		GasGemisch gemischO2=new GasGemisch(gemisch_MolenbruchHash,"Gemish");
//		Tb=HeizwertRechner.calcAdiabateFlammenTemp(gemischO2, p, T);
//		System.out.println("Chemisches Gleichgewicht (Uebergabe des O2/ Kraftstoffgemischs): "
//				+ Tb +" [K]");
	}
	

	public static void testKalorikRechner(){

//		double [] pVec={1e5,10e5,100e5,200e5};
//		double [] lambdaVec ={0.6,1,2,10e7};	
		double [] pVec={50e5,100e5,150e5,200e5};
		double [] lambdaVec ={1,2,3,10e7};
		double T_start=1000;
		double T_max=3500;
		double T_int=10;
		double p,lambda, T;
		double cKrst=8;
		double hKrst=15.63;
		double oKrst=0;
		double nKrst=0;			
		int anzT= (int) ((T_max-T_start)/T_int)+1;				

		double [][]werte=new double[100][anzT];



		Hashtable<String, Double> kalorikHash = new Hashtable<String, Double>(15);
		KalorikRechner kalorikRechner=new KalorikRechner();


		for (int i_p=0;i_p<4;i_p++){			
			p=pVec[i_p];			
			for (int i_L=0;i_L<4;i_L++){				
				lambda=lambdaVec[i_L];				
				int i=0;
				T=T_start;
				while(T<=T_max){		
					//					kalorikHash=kalorikRechner.berechneRauchgasKalorik_mass(p,
					//							T, lambda,krst);
					kalorikHash=kalorikRechner.berechneRauchgasKalorik_mass(p,
							T, lambda,cKrst,hKrst,oKrst,nKrst);
					//erzeugt eine Matrix mit allen auszugebenden Werten. 
					//Die Zahl der Spalten entspricht der Anzahl der Temperaturschritte 
					Enumeration<String> e = kalorikHash.keys();		
					int ikeys=0;
					while (e.hasMoreElements()) {
						String key=(String)e.nextElement();
						werte[ikeys][i]=kalorikHash.get(key);
						ikeys++;						 
					}					
					T=T+T_int;			
					i=i+1;
				}				
				Enumeration<String> e = kalorikHash.keys();
				int iter=0;
				System.out.println("schreibe files");
				while (e.hasMoreElements()) {
					String key=(String)e.nextElement()+ ".txt";
					FileWriter_txt txtFile = new FileWriter_txt(key);
					txtFile.writeLineToFile(werte[iter], true);	
					iter++;
				}		

			}		

		}		

		System.out.println("testKalorikRechner -- fertig");


	}



	public static void testGasGemisch(){
		System.out.println("////////////////////////////////////////////////");
		System.out.println("testGasGemisch...");
		Spezies CO2=KoeffizientenSpeziesFabrik.get_spezCO2();
		Spezies CO2_2=CO2;
		Spezies CO2_3=KoeffizientenSpeziesFabrik.get_spezCO2();	
		Spezies O2=KoeffizientenSpeziesFabrik.get_spezO2();
		Spezies CO2_4=KoeffizientenSpeziesFabrik.get_spezCO2();
		Spezies CO2_5=KoeffizientenSpeziesFabrik.get_spezCO2();	
		Spezies CO2_6=KoeffizientenSpeziesFabrik.get_spezCO2();	

		double M_O2;
		M_O2=O2.get_M();

		System.out.println("Gasgemisch aus Komponenten mit unterschiedlichem HashCode aber selben namen");
		Hashtable<Spezies, Double> molenBruchHash2 = new Hashtable<Spezies, Double>(15);
		molenBruchHash2.put(CO2, 0.0); //wird in nächster Zeile überschrieben da selber HashCode
		molenBruchHash2.put(CO2_2, 0.1);
		molenBruchHash2.put(CO2_3, 0.8);
		molenBruchHash2.put(CO2_4, 0.05);
		molenBruchHash2.put(CO2_5, 0.025);
		molenBruchHash2.put(CO2_6, 0.025);



		GasGemisch gg2=new GasGemisch(molenBruchHash2,"CO2_aus_CO2_2_und_CO2_3");
		double u =gg2.get_u_mass(4000);
		double T=gg2.get_T4u_mass(u);
		double u2=gg2.get_u_mass(T);
		System.out.println((u-u2)/u);
		System.out.println("Zusammensetzung von gg2: " );
		Enumeration<Spezies>e1=gg2.get_speziesMolenBrueche().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("Molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg2.get_speziesMolenBrueche().get(spez1));
		}
		System.out.println("detaillierte Zusammensetzung von gg2: " );
		e1=gg2.get_speziesMolenBruecheDetail().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg2.get_speziesMolenBruecheDetail().get(spez1));
		}

		System.out.println("\n"+"\n"+"einfache Gasmischung aus zwei unterschiedlichen Spezies");
		Hashtable<Spezies, Double> molenBruchHash3 = new Hashtable<Spezies, Double>(15);
		molenBruchHash3.put(CO2, 0.5);
		molenBruchHash3.put(O2, 0.5);
		GasGemisch gg3=new GasGemisch(molenBruchHash3,"O2_CO2");
		System.out.println("Zusammensetzung von gg3: " );
		e1=gg3.get_speziesMolenBrueche().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("Molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg3.get_speziesMolenBrueche().get(spez1));
		}
		System.out.println("detaillierte Zusammensetzung von gg3: " );
		e1=gg3.get_speziesMolenBruecheDetail().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg3.get_speziesMolenBruecheDetail().get(spez1));
		}
		System.out.println("Anzahl an C-Atomen: " +gg3.get_AnzC_Atome());
		System.out.println("Anzahl an O-Atomen: " +gg3.get_AnzO_Atome());
		System.out.println("Anzahl an H-Atomen: " +gg3.get_AnzH_Atome());


		System.out.println("\n"+"\n"+"Gasmischung aus einer Spezies (O2) und einer Gasmischung (O2_CO2=gg3)");
		Hashtable<Spezies, Double> molenBruchHash4 = new Hashtable<Spezies, Double>(15);
		double a,b;
		b=1.0/3.0;
		a=2.0/3.0;
		molenBruchHash4.put((Spezies)gg3, a);
		molenBruchHash4.put(O2, b);
		GasGemisch gg4=new GasGemisch(molenBruchHash4,"O2_CO2_O2");
		System.out.println("Zusammensetzung von gg4: " );
		e1=gg4.get_speziesMolenBrueche().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("Molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg4.get_speziesMolenBrueche().get(spez1));
		}
		System.out.println("detaillierte Zusammensetzung von gg4: " );
		e1=gg4.get_speziesMolenBruecheDetail().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg4.get_speziesMolenBruecheDetail().get(spez1));
		}	
		System.out.println("////////////////////////////////////////////////");
	}


	public static void testObjektKopie(){
		System.out.println("////////////////////////////////////////////////");
		System.out.println("testObjektKopie...");
		Spezies CO2=KoeffizientenSpeziesFabrik.get_spezCO2();
		Spezies O2=KoeffizientenSpeziesFabrik.get_spezO2();
		Spezies CO2_2=CO2; //zweimal das selbe objekt nur der name ist ein anderer
		Spezies CO2_3=KoeffizientenSpeziesFabrik.get_spezCO2(); // neues Objekt

		//GasGemisch aus Gasen mit selbem Hashcode
		Hashtable<Spezies, Double> molenBruchHash1 = new Hashtable<Spezies, Double>(15);
		//hat molenBruchHash1 drei oder zwei einträge?
		molenBruchHash1.put(CO2, 0.3);
		molenBruchHash1.put(CO2_2, 0.2);//CO2_2 hat den selben HashCode wie CO2
		molenBruchHash1.put(CO2_3, 0.5);
		System.out.println("Einträge von molenBruchHash1: " );
		Enumeration<Spezies>e1=molenBruchHash1.keys();
		Spezies spez3, spez4;
		while(e1.hasMoreElements()){
			System.out.println(molenBruchHash1.get(e1.nextElement()));
		}

		Hashtable<Spezies, Double> molenBruchHash3 = new Hashtable<Spezies, Double>(15);
		molenBruchHash3.put(CO2, 0.5);
		molenBruchHash3.put(O2, 0.5);
		GasGemisch gg3=new GasGemisch(molenBruchHash3,"O2_CO2");

		Hashtable<Spezies, Double> molenBruchHash4 = new Hashtable<Spezies, Double>(15);
		double a,b;
		b=1.0/3.0;
		a=2.0/3.0;
		molenBruchHash4.put((Spezies)gg3, a);
		molenBruchHash4.put(O2, b);
		GasGemisch gg4=new GasGemisch(molenBruchHash4,"O2_CO2_O2");			
		System.out.println("detaillierte Zusammensetzung von gg4 : " );
		e1=gg4.get_speziesMolenBruecheDetail().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg4.get_speziesMolenBruecheDetail().get(spez1));
		}	


		GasGemisch gg5=gg4;
		e1=gg5.get_speziesMolenBruecheDetail().keys();
		System.out.println("\n"+"detaillierte Zusammensetzung von gg5 : " );
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg5.get_speziesMolenBruecheDetail().get(spez1));
		}
		gg5.set_Gasmischung_molenBruch(molenBruchHash3);			
		System.out.println("\n"+"detaillierte Zusammensetzung von gg5 nach set_Gasmischung :" );
		e1=gg5.get_speziesMolenBruecheDetail().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg5.get_speziesMolenBruecheDetail().get(spez1));
		}
		System.out.println("\n"+"detaillierte Zusammensetzung von gg4 nachdem gg5=gg4 verändert wurde: " );
		e1=gg4.get_speziesMolenBruecheDetail().keys();
		while(e1.hasMoreElements()){
			Spezies spez1=e1.nextElement();
			System.out.println("molenbruch von "+spez1.get_name()+" :" );
			System.out.println(gg4.get_speziesMolenBruecheDetail().get(spez1));
		}
		System.out.println("////////////////////////////////////////////////");
	}

	public static void testKalorikRechner_Luft(){		

		double T;
		double T_start=1000;
		double T_max=3000;
		double T_int=10;	
		int anzT= (int) ((T_max-T_start)/T_int)+1;				

		double[][]werte=new double[4][anzT];	
		Hashtable<String, double[]> kalorikHash = new Hashtable<String, double[]>(15);

		Hashtable<Spezies, Double> molenBruchHash = new Hashtable<Spezies, Double>(15);
		Spezies CO2=KoeffizientenSpeziesFabrik.get_spezCO2();		
		molenBruchHash.put(CO2,PhysKonst.get_vol_CO2_Luft());
		molenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezO2(), PhysKonst.get_vol_O2_Luft());
		molenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezN2(), PhysKonst.get_vol_N2_Luft());	
		molenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezAr(), PhysKonst.get_vol_Ar_Luft());

		//		so gehts...
		System.out.println(molenBruchHash.get(CO2));
		//		so nicht...
		System.out.println(molenBruchHash.get(KoeffizientenSpeziesFabrik.get_spezCO2()));
		//		hier wird ein neues Objekt mit neuem HashCode erzeugt deshalb klappts nicht!!

		//		villeicht gehts ja so... ?
		Spezies CO2_2=CO2;
		System.out.println(molenBruchHash.get(CO2_2));
		if(CO2_2==CO2){
			System.out.println("CO2_2=CO2");
		}
		//		und wie sieht´s so aus...
		KoeffizientenSpezies CO2_3=(KoeffizientenSpezies)CO2;
		System.out.println(molenBruchHash.get(CO2_3));

		//		Spezies spezB=new KrstApproxSpezies(1,1,1,1,1,1,1,"Test");
		//		molenBruchHash.put(spezB, 0.01);

		//		hier wird Luft als direkte Mischung der Koeffizienten erzeugt
		//		Spezies luftKG = GasGemisch.Gasmischer.erzeugeKoeffSpeziesGemisch(molenBruchHash, "Luft");

		//		hier wird Luft als Gasgemisch erzeugt, um die Ergebnisse mit denen des KoeffSpezies zu vergleichen
		Spezies luftGG = new GasGemisch(molenBruchHash, "LuftGG");


		//		hier wird Luft als Gemisch zweier Gasgemische erzeugt
		Hashtable<Spezies, Double> molenBruchHashA = new Hashtable<Spezies, Double>(15);
		double a=PhysKonst.get_vol_CO2_Luft()+PhysKonst.get_vol_O2_Luft();
		molenBruchHashA.put((KoeffizientenSpezies) KoeffizientenSpeziesFabrik.get_spezCO2(), PhysKonst.get_vol_CO2_Luft()/a);
		molenBruchHashA.put((KoeffizientenSpezies) KoeffizientenSpeziesFabrik.get_spezO2(), PhysKonst.get_vol_O2_Luft()/a);		
		GasGemisch A=new GasGemisch(molenBruchHashA, "LuftA");

		Hashtable<Spezies, Double> molenBruchHashB = new Hashtable<Spezies, Double>(15);
		double b=PhysKonst.get_vol_N2_Luft()+PhysKonst.get_vol_Ar_Luft();
		molenBruchHashB.put((KoeffizientenSpezies) KoeffizientenSpeziesFabrik.get_spezN2(), PhysKonst.get_vol_N2_Luft()/b);	
		molenBruchHashB.put((KoeffizientenSpezies) KoeffizientenSpeziesFabrik.get_spezAr(), PhysKonst.get_vol_Ar_Luft()/b);		
		GasGemisch B=new GasGemisch(molenBruchHashB, "LuftB");		

		Hashtable<Spezies, Double> molenBruchHashC = new Hashtable<Spezies, Double>(15);
		molenBruchHashC.put(A, a);	
		molenBruchHashC.put(B, 1-a);
		GasGemisch luft2GG=new GasGemisch(molenBruchHashC, "Luft2GG");		


		Spezies luft=luft2GG;

		int i=0;
		T=T_start;
		while(T<=T_max){
			werte[0][i] =luft.get_cp_mol(T);
			werte[1][i] =luft.get_cp_mass(T);
			werte[2][i]=luft.get_h_h298_mol(T);
			werte[3][i]=luft.get_h_h298_mass(T);

			T=T+T_int;			
			i=i+1;
		}	
		kalorikHash.put("cp_mol", werte[0]);
		kalorikHash.put("cp_mass", werte[1]);
		kalorikHash.put("h_h298_mol", werte[2]);
		kalorikHash.put("h_h298_mass", werte[3]);



		Enumeration<String> e = kalorikHash.keys();
		int iter=0;
		System.out.println("schreibe files");
		while (e.hasMoreElements()) {
			String key=(String)e.nextElement();
			FileWriter_txt txtFile = new FileWriter_txt(key+ ".txt");
			txtFile.writeLineToFile(kalorikHash.get(key), true);	
			iter++;
		}	

		System.out.println("testKalorikRechner_Luft -- fertig");

	}	
	
	
	
	public static void kalorikAbgleichJanaf(){
		
		Spezies spez=KoeffizientenSpeziesFabrik.get_spezNO();
		double Ta=300;
		double Te=3000;
		double Ti=100;
		int steps=(int) ((Te-Ta)/Ti+1);
		double values[][]=new double [(steps+3)][5];
		
		double T=0;
		int a=0;
		values[a][0]=T;
		values[a][1]=spez.get_cp_mol(T);
		values[a][2]=spez.get_h_h298_mol(T)/1000;
		values[a][3]=spez.get_h_mol(T)/1000;
		values[a][4]=spez.get_delta_hf298_mol()/1000;	
		
		T=200;
		a=1;
		values[a][0]=T;
		values[a][1]=spez.get_cp_mol(T);
		values[a][2]=spez.get_h_h298_mol(T)/1000;
		values[a][3]=spez.get_h_mol(T)/1000;
		values[a][4]=spez.get_delta_hf298_mol()/1000;
		
		T=298.15;
		a=2;
		values[a][0]=T;
		values[a][1]=spez.get_cp_mol(T);
		values[a][2]=spez.get_h_h298_mol(T)/1000;
		values[a][3]=spez.get_h_mol(T)/1000;
		values[a][4]=spez.get_delta_hf298_mol()/1000;
		
		for(int i=0;i<steps;i++){
			T=Ta+i*Ti;
			a=i+3;
			values[a][0]=T;
			values[a][1]=spez.get_cp_mol(T);
			values[a][2]=spez.get_h_h298_mol(T)/1000;
			values[a][3]=spez.get_h_mol(T)/1000;
			values[a][4]=spez.get_delta_hf298_mol()/1000;				
		}		
		
		FileWriter_txt txtFile = new FileWriter_txt(spez.get_name()+ "_KalDat.txt");
		String s[]={"T","cp","H-H298","H","deltaH"};
		txtFile.writeTextLineToFile(s, false);
		String []s2={"K","J/mol/K","kJ/mol","kJ/mol","kJ/mol"};
		txtFile.writeTextLineToFile(s2, true);
		txtFile.writeMatrixToFile(values, true);		
	}
	
	

	/**
	 * berechnet das OHC-Gleichgewicht nach der von Grill vorgeschlagenen Methode und liefert fuer
	 * die einzelnen Spezies je ein txt-File mit der jeweiligen Konzentration in ppm fuer p= 1, 10, 100 und 200bar 
	 * sowie fuer Lambda=0.5, 1, 2, 10e7 
	 * jeweils in Abhaengigkeit der Temperatur. 
	 */
	public static void testGleichgewichtsRechner_A(){		



		double [] pVec={1e5,10e5,100e5,200e5};
		double [] lambdaVec ={0.5,1,2.5,10e7};		
		double T_start=1600;
		double T_max=2500;
		double T_int=10;
		double p,lambda, T;
		int anzT= (int) ((T_max-T_start)/T_int)+1;				

		double [][]werte=new double[100][anzT];

		double cKrst=8;
		double hKrst=15.63;
		double oKrst=0;
		double nKrst=0;					

		GleichGewichtsRechner GGsolver=GleichGewichtsRechner.get_Instance();
		//		GGsolver = new GleichGewichtsRechner_Olikara_Borman("Burcat", 1600);	
		//		GGsolver = new GleichGewichtsRechner_Grill("Burcat", 1600);			

		Hashtable<Spezies, Double> gleichGewichtsMolenBruchHash=new Hashtable<Spezies, Double>(16);

		for (int i_p=0;i_p<4;i_p++){			
			p=pVec[i_p];			
			for (int i_L=0;i_L<4;i_L++){				
				lambda=lambdaVec[i_L];				
				int i=0;
				T=T_start;
				while(T<=T_max){		
					gleichGewichtsMolenBruchHash =GGsolver.get_GG_molenBrueche(p, T, lambda, cKrst, hKrst, oKrst,nKrst);
					//erzeugt eine Matrix mit allen auszugebenden Werten. 
					//Die Zahl der Spalten entspricht der Anzahl der Temperaturschritte 
					Enumeration e = gleichGewichtsMolenBruchHash.keys();		
					int ikeys=0;
					while (e.hasMoreElements()) {
						Spezies key=(Spezies)e.nextElement();
						werte[ikeys][i]=gleichGewichtsMolenBruchHash.get(key)*1e6; //Umrechnung in ppm						 
						ikeys++;						 
					}					
					T=T+T_int;			
					i++;
				}				
				Enumeration e = gleichGewichtsMolenBruchHash.keys();
				int iter=0;
				System.out.println("schreibe files");
				while (e.hasMoreElements()) {
					Spezies spez =(Spezies)e.nextElement();
					String key=spez.get_name()+ ".txt";
					FileWriter_txt txtFile = new FileWriter_txt(key);
					txtFile.writeLineToFile(werte[iter], true);	
					iter++;
				}		

			}		

		}		

		System.out.println("testGleichgewichtsRechner_A -- fertig");		

	}

	/**
	 * berechnet das OHC-Gleichgewicht nach der von Grill vorgeschlagenen Methode und liefert für
	 * die einzelnen Spezies je ein txt-File mit der jeweiligen Konzentration in ppm für p= 1, 10, 100 und 200bar 
	 * sowie für T=1600, 2000, 2500, 3000K 
	 * jeweils in Abhängigkeit von Lambda. 
	 */	
	public static void testGleichgewichtsRechner_B(){		


		double [] pVec={1e5,10e5,100e5,200e5};
		double [] TVec ={1600,2000,2500,3000};
		double lam_start=0.5;
		double lam_max=10;
		double lam_int=0.1; 
		double p,lambda, T;
		int anzLam= (int) ((lam_max-lam_start)/lam_int)+1;				

		double [][]werte=new double[100][anzLam];

		GleichGewichtsRechner GGsolver=null;;
		//		GGsolver = new GleichGewichtsRechner_Olikara_Borman("Burcat", 1600);	
		//		GGsolver = new GleichGewichtsRechner_Grill("Burcat", 1600);	

		Hashtable<Spezies, Double> gleichGewichtsMolenBruchHash=new Hashtable<Spezies, Double>(16);

		double cKrst=8;
		double hKrst=15.63;
		double oKrst=0;
		double nKrst=0;

		for (int i_p=0;i_p<pVec.length;i_p++){			
			p=pVec[i_p];			
			for (int i_T=0;i_T<TVec.length;i_T++){				
				T=TVec[i_T];				
				int i_lam=0;
				lambda=lam_start;

				while(lambda<=lam_max){		
					gleichGewichtsMolenBruchHash = GGsolver.get_GG_molenBrueche(p, T, lambda, cKrst, hKrst, oKrst,nKrst);
					//erzeugt eine Matrix mit allen auszugebenden Werten. 
					//Die Zahl der Spalten entspricht der Anzahl der Lambdaschritte 
					Enumeration e = gleichGewichtsMolenBruchHash.keys();		
					int ikeys=0;
					while (e.hasMoreElements()) {
						Spezies key=(Spezies)e.nextElement();					
						werte[ikeys][i_lam]=gleichGewichtsMolenBruchHash.get(key)*1e6; //Umrechnung in ppm						
						ikeys++;						 
					}					
					lambda=lambda+lam_int;			
					i_lam++;
				}

				Enumeration e = gleichGewichtsMolenBruchHash.keys();
				int iter=0;
				System.out.println("schreibe files");
				while (e.hasMoreElements()) {
					Spezies spez =(Spezies)e.nextElement();
					String key=spez.get_name()+ ".txt";
					FileWriter_txt txtFile = new FileWriter_txt(key);
					txtFile.writeLineToFile(werte[iter], true);	
					iter++;
				}		

			}		

		}


		System.out.println("testGleichgewichtsRechner_B -- fertig");


	}

	/**
	 * Diese Funktion dient dazu zu ueberpruefen ob die Zusammensetzung des Abgases sich aendert wenn AGR zugegeben wird. 
	 * Die Temperatur und der Druck werden bei der Verbrennung konstant gehalten. Das zugemischte AGR entsteht durch die Verbrennung
	 * der selben Mengen an Luft und Kraftstoff
	 * Ergebnis: Das die Atomzahlen aller Mischungen gleich sind haengt die Zusammensetzung des Abgases nicht von der AGR-Rate ab!
	 */
	public static void testSpeziesInputFuerOHCsolver(){		

		double T=2000;
		double p=100e5; //100bar
		double lambda=1;

		Hashtable<Spezies, Double> massenBruchHash = new Hashtable<Spezies, Double>(15);
		Spezies ron95=KoeffizientenSpeziesFabrik.get_spezRON_95();	
		Spezies luft=KoeffizientenSpeziesFabrik.get_spezLuft_trocken();

		double lst=ron95.get_Lst();


		System.out.println(lst);
		double xsiKRST=1/(1+lambda*lst);
		double xsiLuft=1-xsiKRST;
		massenBruchHash.put(ron95,xsiKRST);
		massenBruchHash.put(luft, xsiLuft);

		GasGemisch frischgemisch=new GasGemisch("gemisch");
		frischgemisch.set_Gasmischung_massenBruch(massenBruchHash);		



		GleichGewichtsRechner gg=GleichGewichtsRechner.get_Instance();
		//Berechnung der Abgaszusammensetzung aus dem Frischgemisch bei T=T_Burn
		GasGemisch abgas_T_Burn=new GasGemisch(gg.get_GG_molenBrueche(p, T, frischgemisch),"Abgas_T_Burn");		
		//Zusammensetzung des Abgases ohne verbrennung mit AGR
		Hashtable<Spezies, Double> molenBruecheAbgasNurLuft = abgas_T_Burn.get_speziesMolenBruecheDetail();


		//Berechnung der Abgaszusammensetzung mittels lambda und der Krafstoffzusammensetzung
		GasGemisch abgas_T_Burn_2=new GasGemisch(gg.get_GG_molenBrueche(p, T, lambda, 
				ron95.get_AnzC_Atome(), ron95.get_AnzH_Atome(), ron95.get_AnzO_Atome(),
				ron95.get_AnzN_Atome()),"Abgas_T_Burn_2");		
		//Zusammensetzung des Abgases ohne verbrennung mit AGR
		Hashtable<Spezies, Double> molenBruecheAbgasNurLuft_2 = abgas_T_Burn_2.get_speziesMolenBruecheDetail();		


		Enumeration<Spezies> e0 = molenBruecheAbgasNurLuft_2.keys();		
		System.out.println("Spezies vs Lambda + Kraftstoffzusammensetzung");
		System.out.println("========================================");

		System.out.println("Differenz der C-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzC_Atome()-abgas_T_Burn.get_AnzC_Atome()));
		System.out.println("Differenz der O-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzO_Atome()-abgas_T_Burn.get_AnzO_Atome()));
		System.out.println("Differenz der H-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzH_Atome()-abgas_T_Burn.get_AnzH_Atome()));
		System.out.println("Differenz der N-Atome mit AGR/ohne AGR: "+ (abgas_T_Burn_2.get_AnzN_Atome()-abgas_T_Burn.get_AnzN_Atome()));


		while (e0.hasMoreElements()) {	
			Spezies key=e0.nextElement();
			String name=key.get_name();	
			//			if(molenBruecheAbgasNurLuft.containsKey(key))
			System.out.println(name+ ": Spezies--> "+molenBruecheAbgasNurLuft.get(key)+
					"   Lambda--> "+molenBruecheAbgasNurLuft_2.get(key));

			if(molenBruecheAbgasNurLuft.containsKey(key))
				System.out.println(" --> Abweichung: "+ (molenBruecheAbgasNurLuft.get(key)-molenBruecheAbgasNurLuft_2.get(key)));
		}	








		//Erzeugung einer Mischung aus 80% Frischgemisch und 20% AGR 
		Hashtable<Spezies, Double> massenBruchHashAGRFrischGemisch = new Hashtable<Spezies, Double>(15);
		massenBruchHashAGRFrischGemisch.put(abgas_T_Burn, 0.2);
		massenBruchHashAGRFrischGemisch.put(frischgemisch, 0.8);
		GasGemisch frischGemisch_Mit_AGR_T_Burn=new GasGemisch("FG_AGR");
		frischGemisch_Mit_AGR_T_Burn.set_Gasmischung_massenBruch(massenBruchHashAGRFrischGemisch);


		//Berechnung der Abgaszusammensetzung mit dem eben erzeugten Gemisch
		GasGemisch abgas2=new GasGemisch(gg.get_GG_molenBrueche(p, T, frischGemisch_Mit_AGR_T_Burn),"Abgas2");


		Hashtable<Spezies, Double> molenBruecheAbgasMitAGR_T_Burn = abgas2.get_speziesMolenBruecheDetail();

		Enumeration<Spezies> e = molenBruecheAbgasMitAGR_T_Burn.keys();		
		System.out.println("Abgas2");
		System.out.println("========================================");

		System.out.println("Differenz der C-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzC_Atome()-abgas_T_Burn.get_AnzC_Atome()));
		System.out.println("Differenz der O-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzO_Atome()-abgas_T_Burn.get_AnzO_Atome()));
		System.out.println("Differenz der H-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzH_Atome()-abgas_T_Burn.get_AnzH_Atome()));
		System.out.println("Differenz der N-Atome mit AGR/ohne AGR: "+ (abgas2.get_AnzN_Atome()-abgas_T_Burn.get_AnzN_Atome()));


		while (e.hasMoreElements()) {	
			Spezies key=e.nextElement();
			String name=key.get_name();	
			//			if(molenBruecheAbgasNurLuft.containsKey(key))
			System.out.println(name+ ": 1--> "+molenBruecheAbgasNurLuft.get(key)+
					"   2--> "+molenBruecheAbgasMitAGR_T_Burn.get(key));

			if(molenBruecheAbgasNurLuft.containsKey(key))
				System.out.println(" --> Abweichung: "+ (molenBruecheAbgasNurLuft.get(key)-molenBruecheAbgasMitAGR_T_Burn.get(key)));
		}	

		//		 Erzeugung von Abgas mit T_Freeze
		GasGemisch abgas_T_Freeze=new GasGemisch(gg.get_GG_molenBrueche(p, 2800, frischgemisch),"Abgas_T_Freeze");
		//Erzeugung einer Mischung aus 80% Frischgemisch und 20% AGR_T_Freeze
		Hashtable<Spezies, Double> massenBruchHashAGR_Freeze_FrischGemisch = new Hashtable<Spezies, Double>(15);
		massenBruchHashAGR_Freeze_FrischGemisch.put(abgas_T_Freeze, 0.2);
		massenBruchHashAGR_Freeze_FrischGemisch.put(frischgemisch, 0.8);
		GasGemisch frischGemisch_Mit_AGR_T_Freeze=new GasGemisch("FG_AGR_T_Freeze");
		frischGemisch_Mit_AGR_T_Freeze.set_Gasmischung_massenBruch(massenBruchHashAGR_Freeze_FrischGemisch);		
		//Berechnung der Abgaszusammensetzung
		GasGemisch abgas3=new GasGemisch(gg.get_GG_molenBrueche(p, T, frischGemisch_Mit_AGR_T_Freeze),"Abgas3");
		Hashtable<Spezies, Double> molenBruecheAbgasMitAGR_T_Freeze = abgas3.get_speziesMolenBruecheDetail();

		System.out.println("Abgas3");
		System.out.println("========================================");

		System.out.println("Differenz der C-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzC_Atome()-abgas_T_Burn.get_AnzC_Atome()));
		System.out.println("Differenz der O-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzO_Atome()-abgas_T_Burn.get_AnzO_Atome()));
		System.out.println("Differenz der H-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzH_Atome()-abgas_T_Burn.get_AnzH_Atome()));
		System.out.println("Differenz der N-Atome mit AGR/ohne AGR: "+ (abgas3.get_AnzN_Atome()-abgas_T_Burn.get_AnzN_Atome()));
		Enumeration<Spezies> e1 =molenBruecheAbgasMitAGR_T_Freeze.keys();		
		while (e1.hasMoreElements()) {	
			Spezies key=e1.nextElement();
			String name=key.get_name();	
			//				if(molenBruecheAbgasNurLuft.containsKey(key))
			System.out.println(name+ ": 1--> "+molenBruecheAbgasNurLuft.get(key)+
					"   3--> "+molenBruecheAbgasMitAGR_T_Freeze.get(key));
			if(molenBruecheAbgasNurLuft.containsKey(key))
				System.out.println(" --> Abweichung: "+ (molenBruecheAbgasNurLuft.get(key)-molenBruecheAbgasMitAGR_T_Freeze.get(key)));
		}	



		System.out.println("testSpeziesInputFuerOHCSolver -- fertig");

	}

}




