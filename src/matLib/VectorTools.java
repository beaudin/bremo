package matLib;

import bremoExceptions.MiscException;

public class VectorTools {
	//reshape Variablen	
//	private double[][] ausgabematrix;
    //mittelwertMatrix Variablen
//    private double[][] mittelwertAusgabe;
    
	public static double[][] reshape(double[][] A_ein,int ausgabeZeilen,int ausgabeSpalten)throws MiscException{
		
		int eingabeZeilen = A_ein.length;
		int eingabeSpalten = A_ein[0].length;
		
		if( eingabeZeilen*eingabeSpalten != ausgabeZeilen*ausgabeSpalten){
			throw new MiscException("Das neue Array muss die gleiche Fläche wie das alte Array haben.\n" +
					"Anzahl der Zeilen (" + ausgabeZeilen +") x Anzahl der Spalten (" +
					ausgabeSpalten + ") = " + ausgabeZeilen*ausgabeSpalten +
					"\nTatsächliche Anzahl der Elemente im Array: " + eingabeZeilen * eingabeSpalten);
		}		
		double[][] ausgabematrix = new double[ausgabeZeilen][ausgabeSpalten];
		double[] matrix1D = new double[A_ein.length*A_ein[0].length];
		
		int index=0;
		for(int i=0;i<A_ein.length;i++){
			for(int j=0;j<A_ein[0].length;j++){
				matrix1D[index++] = A_ein[i][j];
			}
		}
		
		index=0;
		for(int i=0;i<ausgabeSpalten;i++){
			for(int j=0;j<ausgabeZeilen;j++){
				ausgabematrix[j][i]=matrix1D[index++];
			}
		}
		       
       return  ausgabematrix; 
   }
	

  public static double[] mittelwertUeberSpalten(double[][] eingabeMatrix){
	  int zeilen = eingabeMatrix.length;
	  int spalten  = eingabeMatrix[0].length;
	  double[] mittelwertAusgabe = new double[zeilen]; 
	  for(int i=0;i<zeilen;i++){
		   for(int j=0;j<spalten;j++){
				mittelwertAusgabe[i] +=eingabeMatrix[i][j]/spalten; 
			}
		}
      return mittelwertAusgabe; 
    }
  
   public static double[][] mittelwertUeberZeilen(double[][] eingabematrix){
	   int zeilen=eingabematrix.length;
		int spalten=eingabematrix[0].length;				
	    double[][] mittelwertAusgabe=new double[1][spalten];
		for(int i=0;i<spalten;i++){
			   for(int j=0;j<zeilen;j++){
		   		   mittelwertAusgabe[0][i]+=eingabematrix[j][i]/zeilen;
			   }
		}	   	 	   
    return mittelwertAusgabe; 
   }
   
   /**
    * Lineare Regression von Werten nach der Methode der kleinsten Fehlerquadrate -- neurohr 12/2014
    * @param xArray
    * @param yArray
    * @return coeff[] [0 Steigung, 1 y-AchsenAbschnitt]
    * @author neurohr
    */
    public static double[] lineareRegression(double[] xArray, double[] yArray){
//    	nach der Form y = a + b*x
		double a,b;
		int n;
		double[] coeff = new double[2];
		double sum_xi=0,sum_xis=0,sum_yi=0,sum_xiyi=0;
		if(xArray.length<=yArray.length){
			n = xArray.length;
		}else{
			n = yArray.length;
		}
		for(int i=0;i<n;i++){
			sum_xi+=xArray[i];
			sum_xis+=(xArray[i]*xArray[i]);
			sum_yi+=yArray[i];
			sum_xiyi+=(xArray[i]*yArray[i]);
		}
		a = (sum_yi * sum_xis - sum_xi * sum_xiyi) / (n * sum_xis - sum_xi * sum_xi);
		b = (n * sum_xiyi - sum_xi * sum_yi) / (n * sum_xis - sum_xi * sum_xi);
		coeff[0] = b; //Steigung
		coeff[1] = a; //y-Achsenabschnitt
		return coeff;
	}
}
