package numal;

public interface AP_efrk_methods {
  
  void derivative(int m0, int m, double t, double u[]);
	void output(int m0, int m, double t[], double te, double u[],
              double sigma[], double phi[], double diameter[],
              int k[], double step, int r, int l);
}