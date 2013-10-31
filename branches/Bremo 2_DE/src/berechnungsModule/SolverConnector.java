package berechnungsModule;

import bremoExceptions.BirdBrainedProgrammerException;

public  class SolverConnector {
	double [] modelParameters2Integrate;
	double [] diff_modelParameters2Integrate;
	private int nbrOfParameters;
	
	public  SolverConnector(int nbrOfParameters) {
		this.nbrOfParameters=nbrOfParameters;
		modelParameters2Integrate=new double [nbrOfParameters];
		diff_modelParameters2Integrate=new double [nbrOfParameters];		
	}
	
	public int get_nbrOfParameters(){
		return this.nbrOfParameters;
	}
	
	public void set_modelParameters2Integrate(double [] parametersIN){
		if(parametersIN.length!=modelParameters2Integrate.length){
			try {
				throw new BirdBrainedProgrammerException("The vector does not have the riht dimension");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		for (int i=0; i<parametersIN.length;i++)
			modelParameters2Integrate[i]=parametersIN[i];
	}
	
	public void set_modelParameter2Integrate(double parameterIN, int idx){
		if(idx>modelParameters2Integrate.length-1){
			try {
				throw new BirdBrainedProgrammerException("The given index is too large");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		modelParameters2Integrate[idx]=parameterIN;
	}
	
	
	
	
	
	public void set_diff_modelParameters2Integrate(double [] diff_parametersIN){
		if(diff_parametersIN.length!=diff_modelParameters2Integrate.length){
			try {
				throw new BirdBrainedProgrammerException("The vector does not have the riht dimension");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		for (int i=0; i<diff_parametersIN.length;i++)
			diff_modelParameters2Integrate[i]=diff_parametersIN[i];
	}
	
	public void set_diff_modelParameter2Integrate(double diff_parameterIN, int idx){
		if(idx>diff_modelParameters2Integrate.length-1){
			try {
				throw new BirdBrainedProgrammerException("The given index is too large");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		diff_modelParameters2Integrate[idx]=diff_parameterIN;
	}	
	
	
	
	

	public  double [] get_modelParameters2Integrate(){
		return modelParameters2Integrate;		
	}
	
	public  double get_modelParameter2Integrate(int idx){
		if(idx>modelParameters2Integrate.length-1){
			try {
				throw new BirdBrainedProgrammerException("The given index is too large");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return modelParameters2Integrate[idx];		
	}
	
	public  double get_diff_modelParameter2Integrate(int idx){
		if(idx>diff_modelParameters2Integrate.length-1){
			try {
				throw new BirdBrainedProgrammerException("The given index is too large");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return diff_modelParameters2Integrate[idx];		
	}
	
	public  double [] get_diff_modelParameters2Integrate(){
		return diff_modelParameters2Integrate;		
	}
}
