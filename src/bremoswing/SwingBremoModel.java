package bremoswing;

import funktionenTests.ShowTable;
import gui.AppView;
import io.AusgabeSteurung;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import bremo.main.Bremo;
import bremoswing.graphik.SelectItemToPlotten;
import bremoswing.manager.ManagerLanguage;
import bremoswing.util.BremoExtensionFileFilter;
import bremoswing.util.BremoInfoFrame;
import bremoswing.util.BremoSwingUtil;
import bremoswing.util.ExtensionFileFilter;
import bremoswing.util.TextPaneOutputStream;

/*
 * Die Klasse SwingBremoModel implementiert die Funktionen oder die Methoden die werden aufgeruft 
 * für die realisierung der Arbeit von Bremo. Die Ergenniss sind an der der View weitergeletet und 
 *  sagt auch an der View wie soll er sich verhalten abhängige der Situation.
 *  Die Klasse implements der Interface Observer weil sie informationen oder änderungen von die Objekten
 *  von type BREMO bekommen.
 */
public class SwingBremoModel implements Observer {

	/************************** Variables declaration ****************************************/

	/**
	 * pfad der letzte ausgewählte File
	 */
	public static String path;

	public static ThreadGroup group;

	/**
	 * Array von ausgewahlte File
	 */
	private static File[] files;
	/**
	 * Array von Bremo
	 */
	public static Bremo[] bremoThread;
	/**
	 * Array enthält nur Name von fertige Bremo
	 */
	public static String[] bremoThreadFertig;
	/**
	 * Pfad der Ordner für die file (.path und .fav) nötig für den applet.
	 */
	public static File BremoAppletDirectory;
	/**
	 * Boolean sagt ob der Debbug Mode activ oder nicht
	 */
	public static boolean DebuggingMode;

	/**
	 * Anzahl von ausgewählten Bremo File bereit zu laufen
	 */
	public static int NrOfFile;
	/**
	 * Anzahl Bremo, die noch laufen.
	 */
	public static int NrBremoAlive;

	// variable "percent" für die Progressbar
	public static int percent;
	/**
	 * Timer,die die Prozent von Progressbar berechnet
	 */
	public static Timer timerProgress;
	// speichern die Startzeit von die Berechnung in ms
	public static double startTime;

	/**
	 * Outputstream,die Text mit Farbe auf die Kleine Konsole Streamt
	 */
	public static TextPaneOutputStream outputStreamKleinArea;
	/**
	 * replace the Standard System OutStream
	 */
	PrintStream outStream;
	/**
	 * replace the Standard System ErrorStream Active in DEBUGMODE for
	 * Programmer
	 */
	PrintStream errStream;
	/**
	 * SwingBremo Stream for Warning Information. Can be use in another Class to
	 * stream Message.
	 */
	public static PrintStream streamOut;
	/**
	 * SwingBremo Stream for Error Information. Can be use in another Class to
	 * stream Message.
	 */
	public static PrintStream erroOut;

	public static String PATH_BREMO_FILEPATH = "src/bremoswing/utilFile/bremo.path";
	public static String PATH_BREMO_FILE_SVN = "/bremoswing/properties/bremo.svnversion";

	/*
	 * Enstellung für die Sprache
	 */
	public String LOCALE_GERMANY_LANGUAGE = "de, DE";
	public String LOCALE_ENGLISH_LANGUAGE = "en, EN";

	public String USER_PROPERTY = "user.home";
	/*
	 * Informationen der Bremo an diese Klasse mitteilen kann.
	 */
	private final String STRING_VerlustTeilung = "verlustteilungsprozess lauft ...";
	private final String STRING_CalculEnd = "berechnungsprozess";
	private final String STRING_SetDebbugingMode = "SetDebbugingMode";
	private final String STRING_PutInBremoThreadFertig = "PutInBremoThreadFertig";
	private final String STRING_FinishTime = "FinishTime";

	// Kontroler: kommunication mit der View erfolgt nur über Kontroller
	private static SwingBremoController controller;

	// boolean sagt ob eine instanz von Bremo schon dabei zu laufen ist
	private boolean bremoActiv;

	// GUI um inputFile zu erzeugen oder editieren.
	private AppView EditorView;

	// Listener um Werten der Progressbar zu berechnen.
	private final ActionListener calculProgess = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent a) {

			if (bremoActiv) { // true = one Instance of Bremo hast already a
								// casepara activ
				double Sum = 0;
				for (int i = 0; i < bremoThread.length; i++) { // summe von
																// (aktuelle
																// Time /
																// gesamte Zeit)
																// für alle
																// bremo
					if (bremoThread[i].get_myCaseState()) {
						Sum = Sum
								+ (bremoThread[i].get_myCase().get_time() / bremoThread[i]
										.get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC);
					}
				}
				int last_percent = percent;
				percent = (int) ((Sum / NrBremoAlive) * 100);

				if (last_percent <= percent) { // sicher dass der Progress muss
												// nicht klein als vorher
					controller.getView().ActionComponent_BremoActiv();
					controller.getView().setProgressValue(percent);
				}

				if (percent >= 100) { // wenn progress ist max dann stopt Der
										// Timer
					timerProgress.stop();
				}

			} else {
				controller.getView().ChangeTextLabel(
						ManagerLanguage.getString("swingbremo_label_2"));
				isBremoactiv(); // pruft ob eine Instanz von Bremo activ ist
				if (bremoActiv) {
					int nbr = getNrBremoRunning();
					if (nbr < NrBremoAlive) {
						NrBremoAlive = nbr; // im lauf der ausführung kann der
											// Anzahl von lebende Bremo sinken.
											// ZB: fertig oder flasche parameter
											// im file
					}
				}
			}

		}
	};

	/************************** End of variables declaration ****************************************/

	public SwingBremoModel(SwingBremoController controller) { // erzeugt ein
																// Objekt von
																// type
																// SwingBremomodel
																// ausgehen von
																// sein
																// Kontroller

		setController(controller);

		init();

	}

	private void init() {
		// Name der Odner ist "BremoFile" im USER.HOME
		BremoAppletDirectory = new File(System.getProperty(USER_PROPERTY)
				+ File.separator + "BremoFile" + File.separator); // pfad für
																	// die File
																	// .path
																	// .fav für
																	// das
																	// Applet
		// manager um wörter im Deutsche zu laden
		ManagerLanguage.managerLanguage(new Locale(LOCALE_GERMANY_LANGUAGE));

		// die berechnung der prozent erfolgt jede 300 ms
		timerProgress = new Timer(300, calculProgess);

		// System.out ist umgeleitet in diese Printstream und schreibe auf
		// GrosArea
		outStream = new PrintStream(System.out, true) {

			@Override
			public void println(String s) {
				appendTextPane(controller.getView().getGrosArea(), s + "\n",
						Color.blue);
			}

			@Override
			public void print(String s) {
				appendTextPane(controller.getView().getGrosArea(), s,
						Color.blue);
			}
		};

		outputStreamKleinArea = new TextPaneOutputStream(controller.getView()
				.getKleinArea());

		streamOut = new PrintStream(outputStreamKleinArea, true) {

			@Override
			public void println(String s) {
				// outputStreamKleinArea.SetTextColor(new Color (215,120,11));
				super.println(s);
			}

			@Override
			public void print(String s) {
				// outputStreamKleinArea.SetTextColor(new Color (215,120,11));
				super.print(s);
			}
		};
		System.setOut(outStream);

		percent = 0;
		path = loadPathFromFile();
		DebuggingMode = false;
		NrOfFile = 0;
		NrBremoAlive = 0;
		bremoActiv = false;
	}

	/**
	 * Set the COntroller for enable communication
	 * 
	 * @param Controller
	 */
	public void setController(SwingBremoController Controller) {
		controller = Controller;
	}

	/**
	 * Startet der Ausfuhrung von Bremo
	 */
	public void startCalcul() {
		// prüft die ausgewählten Bremo File
		if (files == null || files[0] == null) { // die file sind nicht
													// vorhanden
			// Melde an der User
			new BremoInfoFrame(ManagerLanguage.getString("warning"),
					ManagerLanguage.getString("swingbremo_warning_message_3"),
					JOptionPane.WARNING_MESSAGE);
		} else { // file sind vorhanden.
			// Kontroller gebe signal für die Komponenten
			controller.getView().ActionComponent_Berechnen_start();
			// Bremo hat sein CasePara noch nicht erzeugt
			bremoActiv = false;
			try {
				for (int i = 0; i < files.length; i++) { // erzeugt alle
															// Intanzen von
															// Bremo und startet
															// jede instanz
					bremoThread[i].start();
				}
				// controller startet der Timer die alle Bremo Beobachtet
				// und prüft ob sind fertig sind
				controller.checkTimerstart();

				// Berechnung für Progressbar kann starten
				timerProgress.start();

			} catch (IllegalThreadStateException e1) { // Wenn eine fehler
														// vorkommt
				e1.printStackTrace();
				// aller Component der View wieder zum 1. Zustand bringen
				controller.getView().ActionComponent_Berechnen_end();
				controller.getView().ChangeTextLabel(
						ManagerLanguage.getString("swingbremo_label_4"));
			}

		}

	}

	/**
	 * Auswahl von Bremo file durch der filechooser from der EDITOR GUI
	 */
	public static void chooseFileFromEditor(File selection) {
		controller.getView().ChangeTextLabel(
				ManagerLanguage.getString("swingbremo_label_6"));
	}

	/**
	 * Auswahl von Bremo file durch der filechooser
	 */
	public void chooseFile() {
		// Kontroller gebe signal für die Komponenten
		controller.getView().ActionComponent_wahlfile_start();

		// DebbugingMode ausschalten
		SetDebbugingMode(false);

		JFileChooser fileChooser = BremoFileChooser();

		try {

			controller.getView().ChangeTextLabel(
					ManagerLanguage.getString("swingbremo_label_5"));

			int status = fileChooser.showOpenDialog(controller.getView());

			if (status == JFileChooser.APPROVE_OPTION) { // OK
				if (fileChooser.getSelectedFile() != null) {
					// selektierten file speichern
					files = fileChooser.getSelectedFiles();

					// Globale path ändernt und speichern
					updatePath(files[0].getParent());

					// Bremo Felder initialisieren und starten
					bremoThread = new Bremo[files.length];
					for (int i = 0; i < bremoThread.length; i++) {
						bremoThread[i] = new Bremo(files[i], true, this);
					}

					// Bremo Felder für fertige berechnung initialisieren
					bremoThreadFertig = new String[files.length];

					NrOfFile = files.length;
					NrBremoAlive = files.length;

					controller.getView().ChangeTextLabel(
							ManagerLanguage.getString("swingbremo_label_6"));

				}
			} else if (status == JFileChooser.CANCEL_OPTION) { // Abbrechen

				controller.getView().ChangeTextLabel(
						ManagerLanguage.getString("swingbremo_label_7"));

				fileChooser.cancelSelection();
			}
		} catch (Exception e) { // fehler aufgetretten
			controller.getView().ChangeTextLabel(
					ManagerLanguage.getString("swingbremo_label_8"));
			e.printStackTrace();
		}

	}

	/**
	 * Spezielle JFileChooser für Bremo mit File filter
	 * 
	 * @return JFileChooser
	 */
	public JFileChooser BremoFileChooser() {

		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);

		// filter zu speziale auswahlt von Bremo file wie ( .apr oder ...)
		BremoExtensionFileFilter bremoExtensionFileFilter = new BremoExtensionFileFilter();
		ExtensionFileFilter[] bremoListExtentionFilter = bremoExtensionFileFilter
				.getBremoListExtentionFilter();

		for (int i = 0; i < bremoListExtentionFilter.length; i++) {
			fileChooser.addChoosableFileFilter(bremoListExtentionFilter[i]);
			if (bremoListExtentionFilter[i].getDescription().toLowerCase()
					.equals("txt")) {
				fileChooser.setFileFilter(bremoListExtentionFilter[i]);
			}
		}
		return fileChooser;
	}

	/**
	 * Update the Path Variable
	 */
	public static void updatePath(String Path) {

		path = Path;
		savePathToFile(path);
	}

	/**
	 * Save the path Variable
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void savePathToFile(String path) {

		File f = new File(PATH_BREMO_FILEPATH);
		BufferedWriter wr;
		try {
			if (f.exists()) {// Der file existiert schon im lokal
				wr = new BufferedWriter(new FileWriter(f, false));
				wr.write(path);
				wr.close();
			} else { // Der file existiert im USER.HOME

				f = new File(BremoAppletDirectory.getAbsoluteFile()
						+ File.separator + "bremo.path");
				if (f.exists()) {
					wr = new BufferedWriter(new FileWriter(f, false));
					wr.write(path);
					wr.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * load the Path Variable
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String loadPathFromFile() {

		BufferedReader br;
		BufferedWriter wr;

		String zeile = null;
		File f_local = new File(PATH_BREMO_FILEPATH);
		;
		File f_home = new File(BremoAppletDirectory.getAbsoluteFile()
				+ File.separator + "bremo.path");
		try {
			if (f_local.exists()) { // file im local exist
				br = new BufferedReader(new FileReader(f_local));
				if ((zeile = br.readLine()) != null) {
					br.close();

				} else { // file leer
					br.close();
					zeile = ".";
				}
			} else if (BremoAppletDirectory.exists() && f_home.exists()) { // file
																			// im
																			// USER.HOME
																			// exist
				br = new BufferedReader(new FileReader(f_home));
				if ((zeile = br.readLine()) != null) {
					br.close();
				} else { // file leer
					br.close();
					zeile = ".";
				}
			} else { // file nicht im lokal und im USER.HOME. es muss jetzt neu
						// file erstellen
				try {
					// file im Lokal erstellen wenn möglich ist (für applet
					// nicht möglich)
					wr = new BufferedWriter(new FileWriter(f_local, false));
				} catch (FileNotFoundException e) { // nicht möglich wegen
													// Applet
					// im USER.HOME erstellen
					if (!BremoAppletDirectory.exists()) { // Ordner "BremoFile"
															// im USER.HOME
															// erstellen
						BremoAppletDirectory.mkdir();
					}
					wr = new BufferedWriter(new FileWriter(f_home, false));
				}
				wr.write(".");
				wr.close();
				zeile = ".";
			}
		} catch (IOException e) {
			zeile = ".";
		}
		return zeile;
	}

	public String getHtmlToolTip(String Text) {
		return "<html><font face =\"comic sans ms\">" + Text + "</font></html>";
	}

	/**
	 * Prüft ob mindesten 1 Instanz von Bremo ist noch dabei zu laufen
	 * 
	 * @return
	 */
	public static boolean isBremoRunning() {

		boolean live = false;

		for (Bremo b : bremoThread) {
			if (b.isAlive()) {
				live = true;
				break;
			}
		}
		return live;
	}

	/**
	 * gebe an wie viele Instanze von Bremo sind dabei zu laufen
	 * 
	 * @return
	 */
	public int getNrBremoRunning() {
		int count = 0;
		for (Bremo b : bremoThread) {
			if (b.isAlive())
				count++;
		}

		return count;
	}

	/**
	 * Set der DebbugingMode gegeben von eine Instanz von Bremo
	 */
	public void SetDebbugingMode() {

		if (NrOfFile == 1) { // wenn es gibt nur 1 Bremo input file dann debbug
								// mode in dies lesen
			if (bremoThread[0].get_myCase() == null) {
				DebuggingMode = false;
			} else {
				DebuggingMode = bremoThread[0].get_myCase().SYS.DUBUGGING_MODE;
			}

		} else { // wenn mehrere file dann automatisch debbug mode ausschalten
			DebuggingMode = false;
		}
		ChangeConsole();
	}

	/**
	 * Set Der DebbugingMode mit eine Boolean
	 * 
	 * @param mode
	 */
	public void SetDebbugingMode(boolean mode) {
		DebuggingMode = mode;
		ChangeConsole();
	}

	/**
	 * Prüft ob Casepara für mindesten 1 Instanz von Bremo schon verfügbar ist.
	 */
	public void isBremoactiv() {

		for (Bremo b : bremoThread) {
			if (b.get_myCaseState()) {
				bremoActiv = true;
				break;
			}
		}
	}

	/**
	 * Größe der Konsole ändern abhängig von DebuggingMode
	 */
	public void ChangeConsole() {

		if (DebuggingMode) {
			controller.getView().setSizeConsole(710, 750);
		} else {
			controller.getView().setSizeConsole(710, 320);
		}
	}

	/**
	 * Wenn ein Bremo input file zur ende geht oder gestop ist, ist der aANzahl
	 * von lebende zu eins reduziert
	 */
	public void setNrOfBremoAslife() {
		NrBremoAlive--;
	}

	/**
	 * Wenn ein Bremo input ist fertig d.h geht bist zum ende dann sein Name ist
	 * in ein Felder gespeichert
	 * 
	 * @param s
	 *            Name der Bremo File
	 */
	public void PutInBremoThreadFertig(String s) {
		for (int i = 0; i < bremoThreadFertig.length; i++) {
			if (bremoThreadFertig[i] != null)
				continue;
			else
				bremoThreadFertig[i] = s;
			break;

		}
	}

	/**
	 * get The revision number of the Project from SVN
	 * 
	 * @return Revision as String
	 */
	public static String getRevisionNumber() {

		BufferedReader br;
		String zeile = "???";
		try {
			URL url = SwingBremoModel.class.getResource(PATH_BREMO_FILE_SVN);
			br = new BufferedReader(new InputStreamReader(url.openStream()));

			if ((zeile = br.readLine()) != null) {
				br.close();
				zeile = zeile.split("=")[1];
				if (zeile.contains(":")) {
					zeile = zeile.split(":")[1];
				}
				zeile = zeile.replace("M", "");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {

		}
		return zeile;
	}

	/**
	 * Stop die Berechung im laufend
	 */
	public void stopCalcul() {

		do {
			for (Bremo b : bremoThread) {
				b.forceToStop();
			}
		} while (isBremoRunning());
	}

	/**
	 * help function to print Text inside JTextPane with a particular Color.
	 * 
	 * @param pane
	 * @param Text
	 * @param color
	 */
	private synchronized void appendTextPane(final JTextPane pane,
			final String Text, final Color color) {

		Document doc = pane.getDocument();
		Style style = pane.addStyle(null, null);
		StyleConstants.setForeground(style, color);
		try {
			doc.insertString(doc.getLength(), Text, style);
		} catch (BadLocationException e) {
		}
		pane.setCaretPosition(doc.getLength() - 1);
	}

	/**
	 * Starte den Graphic Mode
	 */
	public void showGraphicFrame() {

		if (bremoThreadFertig != null) {
			if (bremoThreadFertig[0] != null) { // starte mit der Ergebniss File
				SelectItemToPlotten selectItem = new SelectItemToPlotten(path,
						bremoThreadFertig);
				selectItem.setVisible(true);

			} else { // Starte ohne Erbegniss File
				SelectItemToPlotten.callBremoView();
			}
		} else { // Starte ohne Ergebniss File
			SelectItemToPlotten.callBremoView();
		}

	}

	/**
	 * Anzage inhalt der File im JTABLE MODE
	 */
	public void showTableFrame() {

		JFileChooser fileChooser = BremoFileChooser();

		try {
			controller.getView().ChangeTextLabel(
					ManagerLanguage.getString("swingbremo_label_5"));
			int status = fileChooser.showOpenDialog(controller.getView());

			if (status == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					File f = fileChooser.getSelectedFile();
					updatePath(f.getParent());
					controller.getView().ChangeTextLabel(
							ManagerLanguage.getString("swingbremo_label_6"));
					new ShowTable(f);
				}
			} else if (status == JFileChooser.CANCEL_OPTION) {

				controller.getView().ChangeTextLabel(
						ManagerLanguage.getString("swingbremo_label_7"));
				fileChooser.cancelSelection();
			}
		} catch (Exception e) {
			controller.getView().ChangeTextLabel(
					ManagerLanguage.getString("swingbremo_label_8"));
			e.printStackTrace();
		}

	}

	/**
	 * Zeige die Hilfe Mode an
	 */
	public void showHelp() {

		try {
			URI hilfe = new URI(
					"http://ifkmhp3.mach.uni-karlsruhe.de/Bremo/Hilfe/Hilfe.html");
			Desktop.getDesktop().browse(hilfe);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Kontroller sagt Berechnung ist fertig und starte Process für die
	 * Abschluss für jede Fertige Bremoo input file.
	 */
	public static void calcul_end() {

		timerProgress.stop();

		if (bremoThreadFertig[0] != null) {

			controller.getView().ChangeTextLabel(
					ManagerLanguage.getString("swingbremo_label_10"));
			BremoSwingUtil.PopUp(
					ManagerLanguage.getString("swingbremo_popup_titel"),
					ManagerLanguage.getString("swingbremo_popup_message"));
		}
		for (String str : bremoThreadFertig) {
			if (str != null) {
				AusgabeSteurung.Error(ManagerLanguage.getString("thread")
						+ " "
						+ str
						+ " "
						+ ManagerLanguage
								.getString("warning_message_terminate"));
			}
			controller.getView().ActionComponent_Berechnen_end();
			controller.checkTimerstop();
			files = null;
		}

	}

	/**
	 * berechnet die Zeit bis zu abschluss der Berechnung von Bremo
	 */
	private void FinishTime() {
		AusgabeSteurung.Error(ManagerLanguage.getString("calcul_time")
				+ ((System.currentTimeMillis() - startTime) / 1000) + " "
				+ ManagerLanguage.getString("time_s"));

	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {

		if (arg instanceof String) {

			update((String) arg);

		} else if (arg instanceof List) {

			update((List<Object>) arg);

		}

	}

	/**
	 * When calcul process at end than POPUP and write the name of Bremo file on
	 * the Array of ending process.
	 * 
	 * @param arg
	 */
	private void update(List<Object> arg) {

		String name = (String) arg.get(0);
		String message_1 = (String) arg.get(1);
		String message_2 = (String) arg.get(2);

		if (message_1.equals(STRING_CalculEnd)) {
			BremoSwingUtil.PopUp(
					ManagerLanguage.getString("info"),
					ManagerLanguage.getString("thread")
							+ " "
							+ name
							+ " "
							+ ManagerLanguage
									.getString("warning_message_terminate"));
		} else if (message_1.equals(STRING_VerlustTeilung)) {
			if (getNrBremoRunning() > 1 && percent < 100) {
				BremoSwingUtil.PopUp(name, STRING_VerlustTeilung);
			} else {
				timerProgress.stop();
				controller.getView().VerlustteilungModeEnable();
			}
		}

		if (message_2.equals(STRING_PutInBremoThreadFertig)) {
			PutInBremoThreadFertig(name);
		}
	}

	/**
	 * Update beim vergleich von String
	 * 
	 * @param arg
	 */
	private void update(String arg) {

		switch (arg) {

		case STRING_SetDebbugingMode:
			SetDebbugingMode();
			break;
		case STRING_FinishTime:
			FinishTime();
			break;

		default:
			break;
		}
	}

	/**
	 * Erzeugt eine GUI um Bremo InputFile zu erzeugen oder zu editieren
	 */
	public void editor() {
		EditorView = new AppView();
	}
}
