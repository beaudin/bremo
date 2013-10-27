package numal;

public interface AP_liniger1vs_methods {
  
  void derivative(int m, double y[], double sigma[]);
  void jacobian(int m, double j[][], double y[], double sigma[]);
	void output(double x[], double xe, int m, double y[],
              double sigma[], double j[][], double info[]);
}