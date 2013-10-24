package numal;

public interface LA_conjgrad_methods {
  
  void matvec(double p[], double q[]);
  boolean goon(int iterate[], double norm2[]);
}