package berechnungsModule.internesRestgas;

import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class RestgasVorgabe extends InternesRestgas{
	private double mAGRint=-5.55;
	
	protected RestgasVorgabe(CasePara cp){
		super(cp);		
	}


	public double get_mInternesRestgas_ASP() {	
		if(mAGRint==-5.55)
			mAGRint = get_ausInputfile();
		return mAGRint;
	}
	
	private double get_ausInputfile(){
		try{
			super.CP.get_mAGR_intern_ASP_aus_InputFile();
		}catch (Exception e){
			try{
				throw new ParameterFileWrongInputException("mAGRinter_ASP nicht angegeben.");
			}catch(ParameterFileWrongInputException p){
				p.stopBremo();
			}
		}
		return Double.NaN;
	}


	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
