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
}
