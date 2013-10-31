package numal;

public interface AP_efsirk_methods {
  
  void derivative(int m, double a[], double delta[]);
  void jacobian(int m, double j[][], double y[], double delta[]);
	void output(double x[], double xe, int m, double y[],
              double delta[], double j[][], int n[]);
}