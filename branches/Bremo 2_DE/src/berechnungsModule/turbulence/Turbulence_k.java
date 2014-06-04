package berechnungsModule.turbulence;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.SolverConnector;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class Turbulence_k extends TurbulenceModel{
	
	SolverConnector solCon;
	private Motor_HubKolbenMotor mot;
	private MasterEinspritzung me;
	private double rho_IVC;
	private final double C0, C1, C2, C3,C4;

	
	public Turbulence_k(CasePara cp) {
		super(cp);
		solCon=new SolverConnector(1);	
		super.CP.SOLVER.connect2Solver(solCon);
		
		if(super.CP.MOTOR.isHubKolbenMotor()){
			mot=((Motor_HubKolbenMotor)super.CP.MOTOR);
		}else{
			try{
				throw new ParameterFileWrongInputException("The chosen turbulence model is only vaild for " +
						"engines with reciprocating pistons! ");
			}catch(ParameterFileWrongInputException pfwe){
				pfwe.stopBremo();
			}
		}
		me=super.CP.MASTER_EINSPRITZUNG;
		
		C0=0.5; //factor for rapid distortion war 65
//		C1=0.55; //factor for dissipation	
		C1=0.3; //war 35
		//C2 hat ziemlich grossen Einfluss
//		C2=0.011371; //for integral lengthscale
		C2=0.25073;
		C3=0; //for initial turbulence kinetic energy
//		C4=0.066595; //for injection	
		C4=0.01492;
		solCon.set_modelParameter2Integrate(5.55, 0);
	}
	
	//initialCOnditions
	public void initialize(Zone zn[], double time){
		double m=0;
		for(int i=0; i<zn.length;i++)m=m+zn[i].get_m();
		rho_IVC=m/this.mot.get_V(time);	
		double turbKinEnergyINIT=-5;
//		if(time!=CP.get_Einlassschluss()){//simulation does not start at IVC
//			turbKinEnergyINIT=CP.get_turbKineticEnergy_Ini();
//		}else if(CP.get_turbKineticEnergy_Ini()==-5.55){
////			Hashtable<Spezies, Double> ht=new Hashtable<Spezies, Double>();
////			for(int i=0; i<zn.length;i++) ht.put(zn[i].get_ggZone(), zn[i].get_m()/m);
////			GasGemisch gg=new GasGemisch("ggTurb");
////			gg.set_Gasmischung_massenBruch(ht);
////			double R=gg.get_R();
////			double delta_t_Intake=mot.get_Einlass_schliesst()-mot.get_Auslass_oeffnet();
////			double m_dot=m/delta_t_Intake;
////			double valveSurf=1;//will be corrected by a constant;
////			double rho_Intake=CP.get_p_LadeLuft()/R/CP.get_T_LadeLuft();
////			double 	v_Intake=m_dot/rho_Intake/valveSurf;
////			turbKinEnergyINIT=C3*v_Intake*v_Intake;			
//			
//			//Initialisation for k taken from Kiva 3V (rinputnewsplit-ck2.f)
//			turbKinEnergyINIT=1; 
//			double stroke=((Motor_HubKolbenMotor)CP.MOTOR).get_Hub();
//			double rps=CP.get_DrehzahlInUproSec();
//			turbKinEnergyINIT=turbKinEnergyINIT*2.0*(stroke*rps)*(stroke*rps);
		if(time==CP.get_Einlassschluss()){//simulation starts at IVC
			//Initialisation for k taken from Kiva 3V (rinputnewsplit-ck2.f)
			turbKinEnergyINIT=1; 
			double stroke=((Motor_HubKolbenMotor)CP.MOTOR).get_Hub();
			double rps=CP.get_DrehzahlInUproSec();
			turbKinEnergyINIT=turbKinEnergyINIT*2.0*(stroke*rps)*(stroke*rps);
		}else{
			turbKinEnergyINIT=CP.get_turbKineticEnergy_Ini();
		}
		solCon.set_modelParameter2Integrate(turbKinEnergyINIT, 0);	
	}

	@Override
	public double get_eps(Zone zn[],double time) {
		//eps~u^3/l~k*u/l
		return 2d/3d*this.get_k(zn,time)*get_intensity(zn,time)/
				this.get_turbulenceIntegralLengthScale(zn, time);
	}

	@Override
	public double get_intensity(Zone zn[],double time) {	
		return Math.sqrt((2.0/3.0)*this.get_k(zn,time));
	}

	@Override
	public double get_k(Zone zn[],double time) {		
		return solCon.get_modelParameter2Integrate(0);
	}
	
	private double calc_dk(Zone zn[], double time){
		double k=get_k(zn,time);
		double u=get_intensity(zn,time);
		double l=get_turbulenceIntegralLengthScale(zn,time);
		double eps=Math.pow(u, 3.0)/l;
		//total  mass in cylinder
		double mCyl=0;
		for(int i=0; i<zn.length;i++)mCyl=mCyl+zn[i].get_m();
		double dm=me.get_dm_krstDampf(time, zn);
		double rho_drho=dm/mCyl-this.mot.get_dV(time)/this.mot.get_V(time);
		
		//turb production due to squish flow		
		//radial velocity	
//		double dbowl; //bowl diameter [m]
//		double bowlDepth; //bowl depth [m]
//		dbowl=CP.get_PistonBowlDiameter();	
//		bowlDepth=CP.get_PistonBowlDepth();
//		double bore=mot.get_Bohrung();
//		double xPist=mot.get_combustionChamberHeight(time);
//		double dxPist=-1*mot.get_dS(time);
//		double wr=(bore*bore-dbowl*dbowl)/4/dbowl/xPist;
//		wr=wr*(dxPist+xPist*rho_drho);
//		//axial velocity
//		double surfBowl=Math.PI*dbowl*dbowl/4;
//		double volBowl=surfBowl*bowlDepth;
//		double volHead=0; //could be implemented later
//		double surfHead=0; //could be implemented later
//		double wax=(volBowl+volHead)/(surfBowl+surfHead);	
//		double wsq=1d/3d*(wr*(1+dbowl/bore)+wax*dbowl*dbowl/bore/bore);
//		double ksq=0.5*wsq*wsq;
		double dksq=0;
//		dksq=0.009*Math.pow(ksq,3d/2d)/l;
		
		
		//turb prduction due to injection		
		double pCyl=zn[0].get_p();
		double dk_Inj=this.me.get_kinEnergyFlux(time, pCyl)/mCyl;
		
//		dk_Inj=0;
		
		double dk=0;
		//dk due to rapid distortion
		dk=dk+C0*2d/3d*k*rho_drho;
		//dk due dissipation
		dk=dk-C1*eps;	
		//dk due to injections
		dk=dk+C4*dk_Inj;
		//dk due to squish flow
		dk=dk+dksq;
		
		return dk;
	}	
	
	public double get_turbulenceIntegralLengthScale(Zone zn[], double time){
//		double m=0;
//		for(int i=0; i<zn.length;i++)m=m+zn[i].get_m();
//		double rho=m/this.mot.get_V(time);
//		double l=C2*Math.pow(rho_IVC/rho, 1d/3d);
	
		double timeTDC=CP.convert_KW2SEC(0);
		if(time>timeTDC)
			time=timeTDC;
		double bore=mot.get_Bohrung();
		double l= C2*mot.get_V(time)*4/Math.PI/bore/bore;	
//		if(l>bore/2/4)
//			l=bore/2/4;
		
		if(l>bore/9)
			l=bore/9;
		
		
		return l;
	}
	
	
	public void update(Zone zn[], double time){
		solCon.set_diff_modelParameter2Integrate(this.calc_dk(zn, time), 0);
		
	}
	
}
