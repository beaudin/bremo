package matLib;

public class Integrator {
	
	public Integrator(){
		
	}
	
	public double get_Integral(double dx, double [] y){
		return trapezIntegration(dx,y)[y.length-1];		
	}
	
	public double [] get_IntegralVerlauf(double dx, double [] y){
		return trapezIntegration(dx,y);		
	}
	
	
	private double[] trapezIntegration(double dx, double [] y){
		double [] integral=new double [y.length];
		for(int i=1; i<y.length;i++) integral[i]=integral[i-1]+dx*0.5*(y[i]+y[i-1]);
		return integral;
	}
	
	
}
