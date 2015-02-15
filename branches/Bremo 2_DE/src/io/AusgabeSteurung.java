package io;

import java.awt.Color;

import bremoswing.SwingBremo;
import bremoswing.manager.ManagerLanguage;

public class AusgabeSteurung {

	public AusgabeSteurung() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Print message als Warning Information
	 * @param message
	 */
	public static void Warning(String message) {
		
		Ausgabe(message, new Color(204,102,0));		
	}
	
	/**
	 * Print message als Error Information
	 * @param message
	 */
	public static void Error(String message) {
	     
		Ausgabe(message, Color.red);
	}
	
	/**
	 * Print message als Simple Information
	 * @param message
	 */
	public static void Message (String message) {
		
		Ausgabe(message, Color.blue);
	}
	
	private static synchronized void Ausgabe(String message, Color color) {
		
		
		try {
			SwingBremo.outputStreamKleinArea.SetTextColor(color);
	    	SwingBremo.streamOut.println( message );
	    } catch (Exception e ){
	    	     System.err.println(message );
	    }
	}
	

}
