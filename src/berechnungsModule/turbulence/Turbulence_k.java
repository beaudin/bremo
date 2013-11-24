package berechnungsModule.turbulence;

import java.util.Hashtable;

import kalorik.GasMixture;
import kalorik.Spezies;
import berechnungsModule.SolverConnector;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.motor.Motor_reciprocatingPiston;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class Turbulence_k extends TurbulenceModel{
	
	SolverConnector solCon;
	private Motor_reciprocatingPiston mot;
	private MasterInjection me;
	private final double C_rd, C_diss, C_lt,C_inj;
	private double C_k0;
	
	public Turbulence_k(CasePara cp) {
		super(cp);
		solCon=new SolverConnector(1);	
		super.CP.SOLVER.connect2Solver(solCon);
		
		if(super.CP.MOTOR.isHubKolbenMotor()){
			mot=((Motor_reciprocatingPiston)super.CP.MOTOR);
		}else{
			try{
				throw new ParameterFileWrongInputException("The chosen turbulence model is only vaild for " +
						"engines with reciprocating pistons! ");
			}catch(ParameterFileWrongInputException pfwe){
				pfwe.stopBremo();
			}
		}
		me=super.CP.MASTER_INJECTION;
		
		C_rd=0.5; //factor for rapid distortion war 65
//		C1=0.55; //factor for dissipation	
		C_diss=0.3; //war 35
		//C2 hat ziemlich grossen Einfluss
//		C2=0.011371; //for integral lengthscale
		C_lt=0.25073;
//		C4=0.066595; //for injection	
		C_inj=0.01492;
		
		C_k0=1;
		
		solCon.set_modelParameter2Integrate(5.55, 0);
	}
	
	//initialCOnditions
	public void initialize(Zone zn[], double time){
		double m=0;
		for(int i=0; i<zn.length;i++)m=m+zn[i].get_m();
		double turbKinEnergyINIT=-5;
		if(time!=CP.get_IVC()){//simulation does not start at IVC
			turbKinEnergyINIT=CP.get_turbKineticEnergy_Ini();
		}else if(CP.get_turbKineticEnergy_Ini()==-5.55){
			//Initialisation for k taken from Kiva 3V (rinputnewsplit-ck2.f)
			C_k0=1; 
			double stroke=((Motor_reciprocatingPiston)CP.MOTOR).get_Hub();
			double rps=CP.get_rotSpeedInRotperSec();
			turbKinEnergyINIT=C_k0*2.0*(stroke*rps)*(stroke*rps);
			
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
		double drho_rho=dm/mCyl-this.mot.get_dV(time)/this.mot.get_V(time);
		
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
		
		double dk=0;
		//dk due to rapid distortion
		dk=dk+C_rd*2d/3d*k*drho_rho;
		//dk due dissipation
		dk=dk-C_diss*eps;	
		//dk due to injections
		dk=dk+C_inj*dk_Inj;
		//dk due to squish flow
		dk=dk+dksq;
		
		return dk;
	}	
	
	public double get_turbulenceIntegralLengthScale(Zone zn[], double time){
		double timeTDC=CP.convert_KW2SEC(0);
		if(time>timeTDC)
			time=timeTDC;
		double bore=mot.get_bore();
		double l= C_lt*mot.get_V(time)*4/Math.PI/bore/bore;		
		if(l>bore/9)
			l=bore/9;
		
		return l;
	}
	
	
	public void update(Zone zn[], double time){
		solCon.set_diff_modelParameter2Integrate(this.calc_dk(zn, time), 0);
		
	}
	
}
