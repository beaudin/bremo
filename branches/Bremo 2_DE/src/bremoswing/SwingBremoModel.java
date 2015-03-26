package bremoswing;

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
import bremoswing.util.SucheBremo;
import bremoswing.util.TextPaneOutputStream;
import funktionenTests.ShowTable;

public class SwingBremoModel implements Observer {

	/************************** Variables declaration ****************************************/

	private Observer SwinwgBremoObserver;

	public static String path;

	private static SucheBremo suche;
	public static ThreadGroup group;

	private static File[] files;
	public static Bremo[] bremoThread;
	public static String[] bremoThreadFertig;

	public static File BremoAppletDirectory;

	public static boolean DebuggingMode;

	public static int NrOfFile;
	public static int NrBremoAlive;
	public static int percent;

	public static Timer timerCalcul;

	public static double startTime;

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

	public String LOCALE_GERMANY_LANGUAGE = "de, DE";
	public String LOCALE_ENGLISH_LANGUAGE = "en, EN";
	public String USER_PROPERTY = "user.home";

	private final String STRING_VerlustTeilung = "verlustteilungsprozess lauft ...";
	private final String STRING_CalculEnd = "berechnungsprozess";
	private final String STRING_StopBremo = "stopsprozess";
	private final String STRING_SetBremoAsLife = "SetBremoAsLife";
	private final String STRING_SetDebbugingMode = "SetDebbugingMode";
	private final String STRING_PutInBremoThreadFertig = "PutInBremoThreadFertig";
	private final String STRING_FinishTime = "FinishTime";

	private static SwingBremoController controller;

	private boolean bremoActiv;

	private final ActionListener calculProgess = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent a) {

			if (bremoActiv) { // true = one Instance of Bremo hast already a
								// casepara activ
				double Sum = 0;
				for (int i = 0; i < bremoThread.length; i++) {
					if (bremoThread[i].get_myCaseState()) {
						Sum = Sum
								+ (bremoThread[i].get_myCase().get_time() / bremoThread[i]
										.get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC);
					}
				}
				int last_percent = percent;
				percent = (int) ((Sum / NrBremoAlive) * 100);

				if (last_percent <= percent) {
					controller.getView().ActionComponent_BremoActiv();
					controller.getView().setProgressValue(percent);
				}

				if (percent >= 100) {
					timerCalcul.stop();
				}

			} else {
				controller.getView().ChangeTextLabel(
						ManagerLanguage.getString("swingbremo_label_2"));
				isBremoactiv();
				if (bremoActiv) {
					int nbr = getNrBremoRunning();
					if (nbr < NrBremoAlive) {
						NrBremoAlive = nbr;
					}
				}
			}

		}
	};

	/************************** End of variables declaration ****************************************/

	public SwingBremoModel(SwingBremoController controller) {

		setController(controller);

		init();

	}

	private void init() {

		BremoAppletDirectory = new File(System.getProperty(USER_PROPERTY)
				+ File.separator + "BremoFile" + File.separator);

		ManagerLanguage.managerLanguage(new Locale(LOCALE_GERMANY_LANGUAGE));

		timerCalcul = new Timer(300, calculProgess);

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

	public void startCalcul() {

		if (files == null || files[0] == null) {
			new BremoInfoFrame(ManagerLanguage.getString("warning"),
					ManagerLanguage.getString("swingbremo_warning_message_3"),
					JOptionPane.WARNING_MESSAGE);
		} else {
			controller.getView().ActionComponent_Berechnen_start();
			bremoActiv = false;
			try {
				for (int i = 0; i < files.length; i++) {
					bremoThread[i].addObserver(this);
					bremoThread[i].start();
				}

				controller.checkTimerstart();

				timerCalcul.start();

			} catch (IllegalThreadStateException e1) {
				e1.printStackTrace();
				controller.getView().ActionComponent_Berechnen_end();
				controller.getView().ChangeTextLabel(
						ManagerLanguage.getString("swingbremo_label_4"));
			}

		}

	}

	public void chooseFile() {

		controller.getView().ActionComponent_wahlfile_start();

		SetDebbugingMode(false);

		JFileChooser fileChooser = BremoFileChooser();

		try {
			controller.getView().ChangeTextLabel(
					ManagerLanguage.getString("swingbremo_label_5"));

			int status = fileChooser.showOpenDialog(controller.getView());

			if (status == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					files = fileChooser.getSelectedFiles();
					updatePath(files[0].getParent());
					bremoThread = new Bremo[files.length];
					// group = new ThreadGroup("BremoFamily");
					for (int i = 0; i < bremoThread.length; i++) {
						bremoThread[i] = new Bremo(files[i], true);
					}
					bremoThreadFertig = new String[files.length];
					NrOfFile = files.length;
					NrBremoAlive = files.length;
					controller.getView().ChangeTextLabel(
							ManagerLanguage.getString("swingbremo_label_6"));

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

	public JFileChooser BremoFileChooser() {

		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);

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
			if (f.exists()) {
				wr = new BufferedWriter(new FileWriter(f, false));
				wr.write(path);
				wr.close();
			} else {
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
			if (f_local.exists()) {
				br = new BufferedReader(new FileReader(f_local));
				if ((zeile = br.readLine()) != null) {
					br.close();

				} else {
					br.close();
					zeile = ".";
				}
			} else if (BremoAppletDirectory.exists() && f_home.exists()) {
				br = new BufferedReader(new FileReader(f_home));
				if ((zeile = br.readLine()) != null) {
					br.close();
				} else {
					br.close();
					zeile = ".";
				}
			} else {
				try {
					wr = new BufferedWriter(new FileWriter(f_local, false));
				} catch (FileNotFoundException e) {

					if (!BremoAppletDirectory.exists()) {
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

	public int getNrBremoRunning() {
		int count = 0;
		for (Bremo b : bremoThread) {
			if (b.isAlive())
				count++;
		}

		return count;
	}

	public void SetDebbugingMode() {

		if (NrOfFile == 1) {
			if (bremoThread[0].get_myCase() == null) {
				DebuggingMode = false;
			} else {
				DebuggingMode = bremoThread[0].get_myCase().SYS.DUBUGGING_MODE;
			}

		} else {
			DebuggingMode = false;
		}
		ChangeConsole();
	}

	public void SetDebbugingMode(boolean mode) {
		DebuggingMode = mode;
		ChangeConsole();
	}

	public void isBremoactiv() {

		for (Bremo b : bremoThread) {
			if (b.get_myCaseState()) {
				bremoActiv = true;
				break;
			}
		}
	}

	public void ChangeConsole() {

		if (DebuggingMode) {
			controller.getView().setSizeConsole(710, 750);
		} else {
			controller.getView().setSizeConsole(710, 320);
		}
	}

	public void setNrOfBremoAslife() {
		NrBremoAlive--;
	}

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

	public void showGraphicFrame() {

		if (bremoThreadFertig != null) {
			if (bremoThreadFertig[0] != null) {
				SelectItemToPlotten selectItem = new SelectItemToPlotten(path,
						bremoThreadFertig);
				selectItem.setVisible(true);

			} else {
				SelectItemToPlotten.callBremoView();
			}
		} else {
			SelectItemToPlotten.callBremoView();
		}

	}

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

	public static void calcul_end() {

		timerCalcul.stop();

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
				timerCalcul.stop();
				controller.getView().VerlustteilungModeEnable();
			}
		}

		if (message_2.equals(STRING_PutInBremoThreadFertig)) {
			PutInBremoThreadFertig(name);
		}
	}

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
}
