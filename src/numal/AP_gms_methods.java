package numal;

public interface AP_gms_methods {

  void derivative(int r, double y[], double delta[]);
  void jacobian(int r, double j[][], double y[], double delta[]);
  void out(double x[], double xe, int r, double y[],
           double delta[], int n[], int jev[], int lu[]);
}