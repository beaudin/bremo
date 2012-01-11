package numal;

public interface AP_eferk_methods {
  
  void derivative(int m, double y[]);
  void jacobian(int m, double j[][], double y[], double sigma[]);
	void output(double x[], double xe, int m, double y[],
              double j[][], int k[]);
}