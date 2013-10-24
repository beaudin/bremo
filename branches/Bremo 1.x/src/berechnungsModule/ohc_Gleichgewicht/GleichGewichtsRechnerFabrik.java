package berechnungsModule.ohc_Gleichgewicht;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class GleichGewichtsRechnerFabrik extends ModulFabrik {
	
	GleichGewichtsRechner ohcSolver=null;
	public static final String OHC_SOLVER_FLAG="OHC_Solver";
	public final static String[] OHC_SOLVER_TYPEN={"OlikaraBorman" , "Grill"};
	public final GleichGewichtsRechner OHC_SOLVER;	
	
	public GleichGewichtsRechnerFabrik(CasePara cp){
		super(cp);

		String ohcSolverVorgabe=get_ModulWahl(OHC_SOLVER_FLAG, OHC_SOLVER_TYPEN);

		if(ohcSolverVorgabe.equals(OHC_SOLVER_TYPEN[0]))	//"OlikaraBorman"
			OHC_SOLVER=
				new GleichGewichtsRechner_Olikara_Borman(cp,cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
		else if(ohcSolverVorgabe.equals(OHC_SOLVER_TYPEN[1])) // "Grill"
			OHC_SOLVER=
				new GleichGewichtsRechner_Grill(cp,cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);

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
					new GleichGewichtsRechner_Grill(cp,cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
			}
		}
	}	

}
