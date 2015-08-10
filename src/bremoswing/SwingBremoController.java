package bremoswing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/*
 * Die Klasse  SwingBremoController implement Kontroller zu steuer der View SwingBremo.
 * Es Dienst als Verbindung zwischen View und Model und überwacht interaktion zwischen 
 * die beide
 * 
 * @author Steve Ngueneko
 */
public class SwingBremoController {

	// Das Model
	private SwingBremoModel model;

	// Der View
	private SwingBremo view;

	// Time
	private final int delay = 500; // milliseconds

	// Diese ActionListener überwacht die Berechnung und sagt an der Model
	// was muss gemacht werden wenn Rechnung zu ende geht.
	private final ActionListener checkBremoState = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!SwingBremoModel.isBremoRunning()) {
				SwingBremoModel.calcul_end();
			}
		}
	};

	// Prüft ob die berechnung fertig ist jeder delay ms
	private final Timer checkTimer = new Timer(delay, checkBremoState);

	/**
	 * Konstruktor
	 * 
	 * @param SwingBremo
	 *            swBr
	 */
	public SwingBremoController(SwingBremo swBr) {
		// set SwingBremo
		SetView(swBr);
		// erzeugt ein Instanz der Model
		setModel(new SwingBremoModel(this));

	}

	/**
	 * Set SwingBremo als View für die KOntroller
	 * 
	 * @param SwBr
	 */
	public void SetView(SwingBremo SwBr) {

		view = SwBr;
	}

	/**
	 * Get de View von der Kontroller
	 * 
	 * @return
	 */
	public SwingBremo getView() {

		return view;
	}

	/**
	 * Set das Model von der Kontroller
	 * 
	 * @param Model
	 */
	public void setModel(SwingBremoModel Model) {

		model = Model;
	}

	/**
	 * Get das Model
	 * 
	 * @return
	 */
	public SwingBremoModel getModel() {

		return model;
	}

	/**
	 * Von Kontroller : Brechnung starten
	 */
	public void berechnen() {
		model.startCalcul();
	}

	/**
	 * Von Kontroller: starte Prozess file auszuwählen
	 */
	public void wahl() {

		model.chooseFile();

	}

	/**
	 * Von Kontroller: Bricht berechnung ab
	 */
	public void stop() {
		model.stopCalcul();
	}

	/**
	 * Von kontroller: Startet GUI für Graphik
	 */
	public void graphic() {
		model.showGraphicFrame();
	}

	/**
	 * Von Kontroller: Startet GUI für JTable
	 */
	public void table() {
		model.showTableFrame();
	}

	/**
	 * Von Kontroller: ruft URL für die Dokumentation
	 */
	public void help() {
		model.showHelp();
	}

	/**
	 * Von Kontroller: startet Timer wenn Berechnung startet
	 */
	public void checkTimerstart() {
		checkTimer.start();
	}

	/**
	 * Von Kontroller: Stopt Timer wenn Berechnung endet
	 */
	public void checkTimerstop() {
		checkTimer.stop();
	}

	/**
	 * From Kontrolle: Startet Den Bremo InputFile Editor
	 */
	public void FileEditor() {
		model.editor();

	}

}
