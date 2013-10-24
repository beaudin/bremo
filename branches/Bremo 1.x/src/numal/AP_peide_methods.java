package numal;

public interface AP_peide_methods {

  boolean deriv(int n, int m, double par[], double y[],
            double t[], double df[]);
  boolean jacdfdy(int n, int m, double par[], double y[],
              double t[], double fy[][]);
  boolean jacdfdp(int n, int m, double par[], double y[],
              double t[], double fp[][]);
  void callystart(int n, int m, double par[], double y[],
                  double ymax[]);
  void data(int nobs, double tobs[], double obs[], int cobs[]);
  void monitor(int post, int ncol, int nrow, double par[],
               double rv[], int weight, int nis[]);
}