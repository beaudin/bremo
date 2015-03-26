package io;

import java.awt.Color;

import bremoswing.SwingBremoModel;

public class AusgabeSteurung {

	public AusgabeSteurung() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Print message als Warning Information
	 * 
	 * @param message
	 */
	public static synchronized void Warning(String message) {

		Ausgabe(message, new Color(204, 102, 0));
	}

	/**
	 * Print message als Error Information
	 * 
	 * @param message
	 */
	public static synchronized void Error(String message) {

		Ausgabe(message, Color.red);
	}

	/**
	 * Print message als Simple Information
	 * 
	 * @param message
	 */
	public static synchronized void Message(String message) {

		Ausgabe(message, Color.blue);
	}

	private static synchronized void Ausgabe(String message, Color color) {

		try {
			SwingBremoModel.outputStreamKleinArea.SetTextColor(color);
			SwingBremoModel.streamOut.println(message);
		} catch (Exception e) {
			System.err.println(message);
		}

	}

}
