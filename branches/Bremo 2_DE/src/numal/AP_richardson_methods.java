package numal;

public interface AP_richardson_methods {

  void residual(int lj, int uj, int ll, int ul, double u[][]);
	void out(double u[][], int lj, int uj, int ll, int ul,
           int n[], double discr[], int k[], double rateconv[],
           double domeigval[]);
}