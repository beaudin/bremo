package numal;

public interface AP_impex_methods {

  void deriv(double t, double y[], double f[], int n);
  boolean available(double t, double y[], double a[][], int n);
  void update(double weights[], double y2[], int n);
	void control(double tp[], double t, double h, double hnew,
               double y[][], double err[], int n, double tend);
}