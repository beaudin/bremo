package numal;

public interface AP_diffsys_methods {
  
  void derivative(int n, double x, double y[], double dy[]);
  void output(int n, double x[], double xe, double y[], double s[]);
}