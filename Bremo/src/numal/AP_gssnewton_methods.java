package numal;

public interface AP_gssnewton_methods {
  
  boolean funct(int m, int n, double par[], double rv[]);
  void jacobian(int m, int n, double par[],
                  double rv[], double jac[][]);
}