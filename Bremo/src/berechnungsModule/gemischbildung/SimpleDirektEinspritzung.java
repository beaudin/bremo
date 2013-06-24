package berechnungsModule.gemischbildung;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.ParameterFileWrongInputException;

public class SimpleDirektEinspritzung extends Einspritzung {
	
	public static final String FLAG="simpleDI"; //Eingabe im Inputfile um dieses Modell zu verwenden	
	private double mKrst_verdampft=0;
	double dm_krst;	
	private Kraftstoff_Eigenschaften krstProp;

	public SimpleDirektEinspritzung(CasePara cp,int index) {
		super(cp,index);
		
		String vergleichsKraftstoff=CP.get_vergleichsKrstVerdampfung(index);
		krstProp =new Kraftstoff_Eigenschaften(vergleichsKraftstoff);
		
//		if(super.boi<super.CP.get_Auslassoeffnet()+CP.SYS.DAUER_ASP_SEC){ //TODO &&!super.isLWA --> war das noetig?
//			try {
//				throw new ParameterFileWrongInputException("" +
//						"Fuer das gewaehlte Einspritzmodell" +this.FLAG+ " der " +index+
//						"en Einspritzung lag der Einspritzbeginn vor dem Rechenbeginn der LWA. \n"+
//				"Der Eingespritzte Kraftstoff wird als vollkommen homogen angenommen");
//			} catch (ParameterFileWrongInputException e) {				
//				e.log_Warning();
//			}
//			dm_krst=0;
//			mKrst_verdampft=super.mKrst;								
//		}else 
		if(super.boi<super.CP.SYS.RECHNUNGS_BEGINN_DVA_SEC&&!super.IS_LWA_EINSPRITZUNG){	
			try {
				throw new ParameterFileWrongInputException("" +
						"Fuer das gewaehlte Einspritzmodell " +this.FLAG+ " der " +index+
						"en Einspritzung lag der Einspritzbeginn vor dem Rechenbeginn. \n"+
				"Die Einspritzung wird wie eine Saugrohreinspritzung behandelt");
			} catch (ParameterFileWrongInputException e) {				
				e.log_Warning();
			}
			dm_krst=0;
			mKrst_verdampft=super.mKrst;
		}else{
			//der Kraftstoff der eingespritzt wird verdampft sofort dm_krst=dm_dampf	
			dm_krst=super.mKrst/(super.eoi-super.boi); 
		}		
		super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,mKrst_verdampft );
		//Krst verdampft sofort
		super.mKrst_fluessig.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, 0);
	}
	
	
	private double get_cp_mass_fl(double T){
		return krstProp.get_cpl(T); 
	}		

	public double get_dQ_krstDampf(double time,Zone zn) {
		//Vereinfachte Darstellung: Der Kraftstoff wird sofort 
		//auf die Temperatur der Zone erwärmt und verdampft dann mit dieser Temperatur
		//Physikalisch ist das nicht richtig da der Krst eine Saettigungstemperatur erreicht
		double dm=this.get_diff_mKrst_dampf(time, zn);
		double  Tkrstfl=this.get_T_fuel_liq();
		double dQ=0;
		if(dm>0){
			dQ=dm*krstProp.get_L(Tkrstfl);
			// Vereinfachung zu Krass -->Aufheizung des Krst wird erstmal nicht Beruecksichtigt
//			+dm*this.get_cp_mass_fl(Tkrstfl)*(get_Tkrst_dampf(time,zn)-get_T_Krst_fl());
		}
		return dQ; 	
	}
	
	public double get_diff_mKrst_dampf(double time,Zone zn) {	
		if(time<super.eoi&&time>=super.boi){			
			return dm_krst;
		}else if(time<super.eoi+CP.SYS.DAUER_ASP_SEC&&time>=super.boi+CP.SYS.DAUER_ASP_SEC){
			//fuer die Ladungswechselanalyse
			return dm_krst;
		}else
			return 0;
	}
		
	public void berechneIntegraleGroessen(double time, Zone zn) {		
		mKrst_verdampft=mKrst_verdampft+this.get_diff_mKrst_dampf(time, zn)*super.CP.SYS.WRITE_INTERVAL_SEC;		
		super.mKrst_dampf.addValue(time,mKrst_verdampft );
		
		//Der Kraftstoff verdampft sofort!
		super.mKrst_fluessig.addValue(time, 0);
	}

	public double get_Tkrst_dampf(double time, Zone zn) {
		//Der Kraftstoff verdampft bei der Temperatur des flueissigen Krst und kommt dann als Dampf mit dieser Temp in die Zone 
		//--> ist nicht ganz  richtig passt aber zur bestimmung der Verdampfungsenthalpie
		return this.get_T_fuel_liq(); 

	}

}
