package numal;

public interface AP_liniger2_methods {
  
  double f(int m, double y[], int i, double sigma1[], double sigma2[]);
  boolean evaluate(int itnum);
  void jacobian(int m, double j[][], double y[],
                double sigma1[], double sigma2[]);
	void output(double x[], double xe, int m, double y[],
              double sigma1[], double sigma2[], double j[][], int k[]);
}