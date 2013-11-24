package berechnungsModule.chemEquilibriumSolver;

import berechnungsModule.ModuleFactory;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class ChemEquilibriumSolverFactory extends ModuleFactory {
	
	ChemEquilibriumSolver ohcSolver=null;
	public static final String OHC_SOLVER_FLAG="chemEquilibriumSolver";
	public final static String[] OHC_SOLVER_TYPEN={"OlikaraBorman" , "Grill"};
	public final ChemEquilibriumSolver OHC_SOLVER;	
	
	public ChemEquilibriumSolverFactory(CasePara cp){
		super(cp);

		String ohcSolverVorgabe=get_ModulWahl(OHC_SOLVER_FLAG, OHC_SOLVER_TYPEN);

		if(ohcSolverVorgabe.equals(OHC_SOLVER_TYPEN[0]))	//"OlikaraBorman"
			OHC_SOLVER=
				new ChemEquilibriumSolver_Olikara_Borman(cp,cp.SYS.CHEM_EQUILIBRIUM_CONSTANTS_USERCHOICE,cp.SYS.T_FREEZE);
		else if(ohcSolverVorgabe.equals(OHC_SOLVER_TYPEN[1])) // "Grill"
			OHC_SOLVER=
				new ChemEquilibriumSolver_Grill(cp,cp.SYS.CHEM_EQUILIBRIUM_CONSTANTS_USERCHOICE,cp.SYS.T_FREEZE);

		else {
			try {
				throw new BirdBrainedProgrammerException(
						"Der ausgewaehlte OHC_Solver \"" +ohcSolverVorgabe + " \" wurde im InputFile " +
						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
						"Es fehlt der entsprechende else-if-Block  oder er wurde noch nicht implementiert\n" +
						"Verwendet wird Grill");
			} catch (BirdBrainedProgrammerException e) {
				e.log_Message();
				OHC_SOLVER=
					new ChemEquilibriumSolver_Grill(cp,cp.SYS.CHEM_EQUILIBRIUM_CONSTANTS_USERCHOICE,cp.SYS.T_FREEZE);
			}
		}
	}	

}
