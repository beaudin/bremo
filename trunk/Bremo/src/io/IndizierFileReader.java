package io;

import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;


public abstract class IndizierFileReader {
//File gehört zum Objekt, kann privat sein.
	protected double  pZyl [];
	protected double  pEin [];
	protected double  pAbg  [];
	/**
	 * die im Eingabefile angegebene Zeitachse in [s nach Rechenbeginn]
	 */
	protected double zeitAchse []; 
	
	protected int anzahlZyklen;
	protected int punkteProArbeitsspiel;
	protected double pZylMAX=-5.55;
	protected double dauerASP;
	protected final CasePara CP;
	
	public IndizierFileReader(CasePara cp){
		CP=cp;
	}
	
	//Routine, um die Daten einzulesen. Innerhalb dieser Methode müssen 
	//pZyl, pSaug, pAbg, Drehzahl, anzahlZyklen und punkteProArbeitsspiel 
	//Werte bekommen.
	protected abstract void datei_Einlesen();
	
	// gibt pZyl zurück
	public double[] get_pZyl(){
		return pZyl;
	}

	// gibt pSaug zurück
	public double[] get_pEin(){
		try{
			double a=pEin[0];
		}catch(NullPointerException e){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht auf den Einlassdruck zuzugreifen. \n " +
						"Dieser wurde aber nicht eingelesen --> InputFile Checken ");
			} catch (ParameterFileWrongInputException e1) {
				e1.stopBremo();
			}
		}		
		return pEin;
	}

	// gibt pAbg zurück
	public double[] get_pAbg(){ 
		
		try{
			double a=pAbg[0];
		}catch(NullPointerException e){
			try {
				throw new ParameterFileWrongInputException("Es wurde versucht auf den Einlassdruck zuzugreifen. \n " +
						"Dieser wurde aber nicht eingelesen --> InputFile Checken ");
			} catch (ParameterFileWrongInputException e1) {
				e1.stopBremo();
			}
		}		
		 return pAbg;
	}
	// gibt pAbg zurück
	public double[] get_Zeitachse(){ 
		 return zeitAchse;
	}

	// gibt die Anzahl der Zyklen zurück
	public int get_anzahlZyklen(){
		return anzahlZyklen;
	}
	
	public int get_punkteProArbeitsspiel(){
		return punkteProArbeitsspiel;
	}
	public double get_pZylMAX(){
		return pZylMAX;
	}
	
}
