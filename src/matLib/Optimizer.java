package matLib;

import io.SimpleTXTFileReader;
import numal.AP_praxis_method;
import numal.Analytic_problems;

public class Optimizer implements AP_praxis_method{
	private int n;
	private double [] x0;
	double [] in =new double[10];
	double [] out=new double [7];
	private int count;
	
	public Optimizer(double [] x0){
		this.x0=x0;
		n=x0.length-1;
		in[0] = 1.0e-1;   	// the machine's precision
		in[1]=2.0e-1;		//relative tolerance
		in[2]=2.0e-2;		//absolute tolerance
		in[5]=110;		//max number of funct calls
		in[6]=0.1;			//max step size
		in[7]=1;			//max scaling factor
		in[8]=0;			//number of iterations used to proof the found solution
		in[9]=5;			//if ill conditioned  in[9]<0 else  in[9]>0
		count=0;
		

	}
private double valBuffer=0;
private double tol=1e-6;

	@Override
	public double funct(int n, double[] x) {
		double val=0;
//		for(int i=0;i<n;i++){
//			val+=x[i]*x[i];
//		}
		val=(x[1]-1)*(x[1]-1);
		val=val-1;	
		count=count+1;
		System.out.println("count: "+ count+" x[0]: " + x[0]+" x[1]: " +x[1]+" val: " +val);
		if(Math.abs(valBuffer-val)>tol){
				valBuffer=val;
				return val;	
		}else
			return valBuffer;		
	}
	

	
	
	
	public double[] optimize(){
		Analytic_problems.praxis(n, x0, this, in, out);		
		return out;		
	}
	
	

}
