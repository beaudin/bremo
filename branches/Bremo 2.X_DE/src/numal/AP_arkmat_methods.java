package numal;

public interface AP_arkmat_methods {

  void der(int m, int n, double t, double v[][], double ftv[][]);
	void out(double t[], double te, int m, int n, double u[][],
           int type, int order[], double spr[]);
}