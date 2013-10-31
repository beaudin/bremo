package numal;

public interface AP_eft_methods {

  double sigma(double t[], int m0, int m);
  double diameter(double t[], int m0, int m);
  void derivative(double t[], int m0, int m, int i, double a[]);
  double aeta(double t[], int m0, int m);
  double reta(double t[], int m0, int m);
	void out(double t[], double te, int m0, int m, double u[], 
           int k[], double eta[], double rho[]);
}