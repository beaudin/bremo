package io;

import java.awt.Color;

import bremoswing.SwingBremoModel;

/**
 * Die Klasse steuert die Ausgabe auf Der Konsole. Es gibt 3 mögliche Ausgabe.
 * Warning message im gelbe Farbe , Error Message im rote Farbe und Normale
 * Message im blau Farbe.Wenn Bremo lauft Ohne GUI Konsole dann alle Ausgabe
 * werden direkt an normale streamt geleitet.
 */
public class AusgabeSteurung {

	public AusgabeSteurung() {

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
	 * Print message als Simple/ Normale Information
	 * 
	 * @param message
	 */
	public static synchronized void Message(String message) {

		Ausgabe(message, Color.blue);
	}

	/**
	 * Wenn Bremo mit GUI lauft dann leitet auf die Konsole sonst auf normale
	 * ERROR Stream von JAVA.
	 */
	private static synchronized void Ausgabe(String message, Color color) {

		try {
			SwingBremoModel.outputStreamKleinArea.SetTextColor(color);
			SwingBremoModel.streamOut.println(message);
		} catch (Exception e) {
			System.err.println(message);
		}

	}

}
