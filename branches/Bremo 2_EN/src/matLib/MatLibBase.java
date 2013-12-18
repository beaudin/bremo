package matLib;

import bremoExceptions.MiscException;
import numal.*;

//Die Klasse MatLib dient als CordonSanitaire für eine beliebige mathmatische Bibliothek. Alle aufrufe 
//in Bremo erfolgen über Funktionen die in Matlib definiert sind. Funktionen in Matlib rufen wiederum 
//Funktionen einer mathematischen Bibliothek auf 
//--> auf diese Weise ist sichergestellt, dass die math. Bib. jederzeit ausgetauscht werden kann 
//ohne den gesamten BremoCode nach aufrufen der math. Bib durchforsten zu müssen.

public class MatLibBase {
	
	private MatLibBase(){
	//von dieser Klasse darf es keine Objekte geben!	
	}

	// Initialisierung eines Vektors
	public static void inivec(double a[], double x) {
		Basic.inivec(0, a.length - 1, a, x);
	}

	// Initialisierung eines Vektors
	public static void inimat(double a[][], double x) {
		int row = a.length - 1;
		int col = a[0].length - 1;
		Basic.inimat(0, row, 0, col, a, x);
	}

	// Vektorprodukt
	public static double mult_vec_vec(double a[], double b[]) {
		double s = -555.555;
		int la = a.length - 1;		
		s = Basic.vecvec(0, la, 0, a, b);	
		return s;
	}
	
	/**
	 * Multipliziert Vektoren elementweise
	 */
	public static double [] mult_vec_vec_elm(int l, int u, double a[], double b[]){		

		double c[]=new double [a.length];		
		for (; l<=u;l++)c[l]=a[l]*b[l];
		return c;		
	}
	
	// Multiplikation eines Vektors mit einem konst. Wert
	public static double[] mul_vec_konst(double a[], double x) {
		double [] b=new double [a.length];
		for (int l = 0; l <a.length ; l++)
			b[l] = a[l] * x;
		return b;
	}
	
	
	/**
	 * elementweise Multiplikation einer Matrix mit einem konstanten Wert.
	 * Die Matrix muss nicht in jeder Zeile die gleiche Anzahl an Spalten besitzen
	 *
	 * @param mat = double [][] Matrix
	 * @param x = konstanter double Wert
	 * @return b= Matrix bei der jeder Eintrag mit b multipliziert wurde
	 */
	public static double[][] mul_mat_konst(double mat[][], double x) {
		
		double [][] b=new double [mat.length] [];
		
		for (int l = 0; l <mat.length ; l++){
			b [l]=mul_vec_konst(mat[l],x);				
		}	
		return b;
	}
	
	/**
	 * elementweise Addition der Werte zweier Matrizen.
	 * Die Elemente von matB werden vor der Addition mit mulB multipliziert 
	 * @param matA double [][] Matrix 
	 * @param matB double [][] Matrix 
	 * @param mulB double 
	 * @return matA+matB*mulB
	 */
	public static double[][] addMat(double matA[][],double matB[][], double mulB){
		
		if (matA.length!=matB.length){
			throw new IllegalArgumentException(
	                "Matrizen haben unterschiedliche Anzahl an Zeilen");
		}
		
		double [][] matC=new double [matA.length][];
		
		for (int l = 0; l <matA.length ; l++){
			
			if (matA.length!=matB.length){
				throw new IllegalArgumentException(
		                "Matrizen haben in Zeile " + l + " unterschiedliche Anzahl an Spalten");
			}
			matC[l]=add_vec(matA[l], matB[l],mulB);						
		}			
		return matC;
	}
	
	
	// // Multiplikation eines Bereichs Vektors mit einem konst. Wert genaue Beschreibung siehe numal doku
	public static void mul_vec_konst(int l, int u, int shift, double a[], double b[],
			double x) {
		Basic.mulvec(l, u, shift, a, b, x);
	}

	//Multiplikation einer Matrix mit einem Vektor
	public static void mult_mat_vec(int lr, int ur, int lc, int uc, double[][] a,
			double[] b, double[] c) {
		Basic.fulmatvec(lr, ur, lc, uc, a, b, c);
	}
		
	//nimmt ein 2D-Array, transponiert es, und gibt das transponierte Array zurück
	public static double[][] transp_2d_array(double[][] arrayIn){
		int a_r=arrayIn.length; //Anzahl Reihen
		int a_c=arrayIn[0].length; //Anzahl Spalten
		double[][] arrayOut = new double[a_c][a_r];
		for (int r = 0; r < a_r; r++) {
			for (int c = 0; c < a_c; c++) {
				arrayOut[c][r] = arrayIn[r][c];
			}
		}
		return arrayOut;
	}
	
	public static double[] mw_aus_2DArray(double[][] arrayIn, boolean ueberDim1){
		int ln;
		double[][] temp;
		
		if(ueberDim1){
			temp=arrayIn;
		} else {
			
			temp=transp_2d_array(arrayIn);
		}
		ln = temp.length;
		
		double[] arrayOut = new double[ln];
		for(int i=0; i<ln; i++){
			arrayOut[i]=mw_aus_1DArray(temp[i]);
		}
		
		return arrayOut;
		
	}
	
	public static double mw_aus_1DArray(double[] arrayIn){
		int ln = arrayIn.length;
		double summe = 0;
		
		for(int i=0; i<ln; i++){
			summe+=arrayIn[i];
		}
		return summe/ln;
	}
	
	//Berechnet die Summe aller Elemente eines Vektors
	public static double vecsum(double a[]) {
		double s = 0;
		for (int i = 0; i <= a.length - 1; i++)
			s += a[i];
		return s;
	}

	//Berechnet die Summe eines Teils der Elemente eines Vektors
	public static double vecsum(double v[], int a, int b) {
		double s = 0;
		if (a >= 0 && b < v.length) {
			for (int i = a; i <= b; i++)
				s += v[i];
		}
		return s;
	}
	
	/**
	 * Multipliziert Vektor b Elementweise mit 
	 * mulb und addiert die Vektoren a und b Elementweise und gibt das Ergebnis zurück
	 * @param a double [] Vektor 
	 * @param b double [] Vektor 
	 * @param mulb double
	 * @return double [] c
	 */
	public static double [] add_vec(double a[], double b[],double mulb){
		
		if (a.length!=b.length){
			throw new IllegalArgumentException(
	                "Vektoren haben unterschiedliche Anzahl an Elementen");
		}
		double [] c=new double[a.length];
		for (int i=0; i<a.length;i++)c[i]=a[i]+mulb*b[i];
		return c;		
	}
	
	// DEEPCOPY A ONE DIMENSIONAL ARRAY OF Double
    public static double[] copy(double[] array){
        if(array==null)return null;
        int n = array.length;
        double[] copy = new double[n];
        for(int i=0; i<n; i++){
            copy[i] = array[i];
        }
        return copy;
    }
    
    public static double [] normierVec(double v[]){
    	double r[]=new double [v.length];
    	double max=Double.NEGATIVE_INFINITY;
    	for(int i=0;i<v.length;i++) if(v[i]>max) max=v[i];
    	for(int i=0;i<v.length;i++) r[i]=v[i]/max;
    	return r;
    }
    
    public static double crossCorr0(double [] v1, double [] v2){
    	if(v1.length!=v2.length)    	
    		throw new IllegalArgumentException("The Vectors must have the same length");
 
    		double cc=0;
    		
    		double cv1=0;
    		for(int i=0; i<v1.length;i++) cv1+=v1[i]*v1[i];
    		cv1=cv1/v1.length;
    		
    		double cv2=0;
    		for(int i=0; i<v1.length;i++) cv2+=v2[i]*v2[i];
    		cv2=cv2/v2.length;
    		
    		double cv12=0;
    		for(int i=0; i<v1.length;i++) cv12+=v1[i]*v2[i];
    		cv12=cv12/v2.length;
    		
    		cc=cv12/Math.sqrt(cv1*cv2);
    		
    		return cc;    	
    	
    }  
    
    public static double rms(double []v1, double[] v2){
    	if(v1.length!=v2.length)    	
    		throw new IllegalArgumentException("The Vectors must have the same length");
    	
    	double rms=0;
    	for(int i=0; i<v1.length;i++) rms=rms+(v1[i]-v2[i])*(v1[i]-v2[i]);
    	return Math.sqrt(rms);
    }

}
