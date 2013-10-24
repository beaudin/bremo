package numal;

public interface AP_multistep_methods {
  
  void deriv(double df[], int n, double x[], double y[]);
  boolean available(int n, double x[], double y[], double jacobian[][]);
  void out(double h, int k, int n, double x[], double y[]);
}