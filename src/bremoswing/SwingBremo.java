package bremoswing;

import gui.AppView;
import io.AusgabeSteurung;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import bremoswing.graphik.BremoView;
import bremoswing.graphik.BremoViewModel;
import bremoswing.manager.ManagerLanguage;
import bremoswing.util.BremoInfoFrame;
import bremoswing.util.BremoSwingUtil;
import bremoswing.util.PaintPanel;
import bremoswing.util.SucheBremo;

/* *
 *  Diese Klasse erzeugt die Hauptoberfläsche ( Swing GUI ) um Bremo Graphik zu steuert. 
 *  Das ganzen funktionniert mit der Prinzip von MVC Patter ( Modell - View - Controller )
 *  und Diese Klasse entspricht zu den View d.h implementiert nur der GUI und ein 
 *  paaren einfachen funktionen die der Komposanten  der GUI aktulisieren oder ändern.
 *  in der  gleiche package  sind " SwingBremoController" die der Kontroller implementiert und 
 *  "SwingBremoModel" die der Modell von der GUI implementiert.
 *
 * @author Ngueneko Steve
 */
public class SwingBremo extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	/************************** Variables declaration ****************************************/
	// private static SwingBremo instanceSwingBremo;

	// public String RevisionNumber = getRevisionNumber();

	// Der GUI hat die folgenden Komponent

	// ******************* KOMPONENT *******************************/

	private String title = "Bremo 2.0 rev ? Beta";

	private SucheBremo suche; // kleine GUI um text im TextPAne zu suchen

	private JButton berechnen; // starte die Berechnung in Bremo

	private JButton wahlFile; // ermöglicht ein File oder mehrere file für die
								// Berechnung auszuwählen

	private JButton stop; // Stop die laufende Berechnung im Bremo

	private JButton sehen; // ruf eine andere GUI für die Darstellung von
							// Ergebnis File als Kurven

	private JButton docfile; //

	private JButton help; // ruf der link wo steht die Dokumentation für Bremo

	private JButton table; // ruf eine andere GUI für die Darstellung von
							// InputFile oder output file als JTable

	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;

	private JTextPane kleinArea; // klein Konsole für den Outpout im Normale
									// Mode

	private JTextPane grosArea; // große Konsole für den Outpout im Debug Mode

	private PaintPanel konsole; // Panel für die Konsole

	private PaintPanel manager; // Panel für die Steuerung

	private JProgressBar progressBar; // Progessbar für die Berechnung

	private JProgressBar progressBarInd; // indeterministische Progressbar falls
											// der Progress nicht bekannt ist

	private JLabel label; // repräsentier den path für den aktuelle active
							// odner.

	// ******************* ENDE KOMPONENT *******************************/

	// ******************** Resource Für der GUI **************************/

	public final String PATH_FROZE_BLUE = "/bremoswing/bild/Abstract_Frozen_Blue.jpg";
	public final String PATH_BREMO_APP_ICON = "/bremoswing/bild/bremo1.png";
	public final String PATH_BREMO_CHOOSEFILE_ICON = "/bremoswing/bild/folder_blue.png";
	public final String PATH_BREMO_PLAY_ICON = "/bremoswing/bild/play_blue.png";
	public final String PATH_BREMO_STOP_ICON = "/bremoswing/bild/stop_blue.png";
	public final String PATH_BREMO_SEE_ICON = "/bremoswing/bild/see_graphik.png";
	public final String PATH_BREMO_DOC_FILE = "/bremoswing/bild/doc-icon.png";
	public final String PATH_BREMO_TABLE_ICON = "/bremoswing/bild/table-icon-1.png";
	public final String PATH_BREMO_HELP_ICON = "/bremoswing/bild/help.png";

	public final String PATH_BREMO_CHOOSEFILE_ROLLOVER_ICON = "/bremoswing/bild/folder_blue_2.png";
	public final String PATH_BREMO_PLAY_ROLLOVER_ICON = "/bremoswing/bild/play_blue_2.png";
	public final String PATH_BREMO_SEE_ROLLOVER_ICON = "/bremoswing/bild/see_graphik-2.png";
	public final String PATH_BREMO_DOC_FILE_ROLLOVER_ICON = "/bremoswing/bild/doc-icon2.png";
	public final String PATH_BREMO_TABLE_ROLLOVER_ICON = "/bremoswing/bild/table-icon-2.png";
	public final String PATH_BREMO_HELP_ROLLOVER_ICON = "/bremoswing/bild/help-2.png";

	public final String PATH_BREMO_HELP_HTML = "src/bremoswing/help/BremoHelp.html";

	public final String PATH_BREMO_CLOSE_FRAME_ICON = "/bremoswing/bild/questionIcon.png";

	public final String LOCALE_GERMANY_LANGUAGE = "de, DE";
	public final String LOCALE_ENGLISH_LANGUAGE = "en, EN";
	public final String USER_PROPERTY = "user.home";

	public final String BUTTON_BERCHNEN = "berechenen";
	public final String BUTTON_WAHLFILE = "wahlfile";
	public final String BUTTON_STOP = "stop";
	public final String BUTTON_GRAPHIC = "sehen";
	public final String BUTTON_DOCFILE = "docfile";
	public final String BUTTON_HELP = "help";
	public final String BUTTON_TABLE = "table";

	// ** ENDE Resource Für der GUI ************/

	private SwingBremoController controller; // Kontroller die als Verbindung
												// mit der Modell dienst

	private AppView EditorView;

	/************************** End of variables declaration ****************************************/

	/**
	 * Constructor to Creates new form SwingBremo
	 */
	public SwingBremo() {
		// initialisierung der Komponeneten
		initComponents();
		// stellt der GUI in der Mitte der Bildchirm
		BremoSwingUtil.placeFrame(this);
	}

	/***
	 * This method is called from within the constructor to initialize the form.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	private void initComponents() {

		// initilaisierung von Komponent
		// und für ein paar ein Name geben um später
		// komponent mit hilfe der Name zu identifizieren

		kleinArea = new JTextPane();
		grosArea = new JTextPane();

		controller = new SwingBremoController(this);

		manager = new PaintPanel(PATH_FROZE_BLUE, 200, 210, 750, 40);
		konsole = new PaintPanel(PATH_FROZE_BLUE, 200, 250, 750, 750);

		berechnen = new JButton();
		berechnen.setName(BUTTON_BERCHNEN);

		wahlFile = new JButton();
		wahlFile.setName(BUTTON_WAHLFILE);

		stop = new JButton();
		stop.setName(BUTTON_STOP);

		sehen = new JButton();
		sehen.setName(BUTTON_GRAPHIC);

		docfile = new JButton();
		docfile.setName(BUTTON_DOCFILE);

		help = new JButton();
		help.setName(BUTTON_HELP);

		table = new JButton();
		table.setName(BUTTON_TABLE);

		jScrollPane1 = new JScrollPane();
		jScrollPane2 = new JScrollPane();

		progressBar = new JProgressBar(0, 100);
		progressBarInd = new JProgressBar(0, 100);

		label = new JLabel(" ");

		title = title.replace("?", controller.getModel().getRevisionNumber());

		/****** HAUPT FRAME ****************************************************************/

		/* Einstellung der Fenster */
		// Title
		setTitle(title);
		// Farbe der Background
		setBackground(new Color(255, 255, 255));
		// icon der GUI
		setIconImage(new ImageIcon(getClass().getResource(PATH_BREMO_APP_ICON))
				.getImage());
		// Name
		setName(ManagerLanguage.getString("swingbremo_app_titel"));
		// size nicht ändern
		setResizable(false);
		// aktion um GUI zu schließen
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		// size der GUI
		Dimension size = new Dimension(720, 320);
		setPreferredSize(size);

		// listener wenn mann der GUI schließen will
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeFrame();
			}
		});

		/*
		 * Layout der Panel "Manager". Sie erfahren mehr über "GridBagLayout"
		 * wenn sie java layout einlesehen. Komponenete sind als Grid mit
		 * Koordonate X,Y eingepackt.
		 */
		manager.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		/************ BUTTON BERECHNEN ************************************/
		ImageIcon beri = new ImageIcon(getClass().getResource(
				PATH_BREMO_PLAY_ICON)); // set Icon
		berechnen.setIcon(beri);
		berechnen.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_PLAY_ROLLOVER_ICON))); // set reloverIcon
		berechnen.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_play"))); // set der TooltipText

		berechnen.addActionListener(this); // funge zu der liste der listener
											// der GUI inzu

		/* Layout der Panel " Manager " odnert komponent als Koordonate (X,Y) */
		gc.insets = new Insets(0, 0, 0, 0);
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.weightx = 0;
		gc.gridx = 0; // X
		gc.gridy = 0; // Y

		manager.add(berechnen, gc);

		/************ BUTTON WAHLFILE ************************************/

		wahlFile.setIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_CHOOSEFILE_ICON))); // set Icon
		wahlFile.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_CHOOSEFILE_ROLLOVER_ICON))); // set reloverIcon
		wahlFile.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_choose"))); // set der
															// TooltipText

		wahlFile.addActionListener(this); // funge zu der liste der listener der
											// GUI inzu

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 1; // X
		gc.gridy = 0; // y
		gc.ipadx = 0;

		manager.add(wahlFile, gc);

		/************ BUTTON STOP ************************************/
		stop.setIcon(new ImageIcon(getClass().getResource(PATH_BREMO_STOP_ICON)));
		stop.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_stop")));
		stop.setEnabled(false);
		stop.setMargin(new Insets(0, 0, 0, 0));
		stop.addActionListener(this);

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 2; // X
		gc.gridy = 0; // y
		gc.ipadx = 0;

		manager.add(stop, gc);

		/************ BUTTON Graphic ************************************/
		sehen.setIcon(new ImageIcon(getClass().getResource(PATH_BREMO_SEE_ICON)));
		sehen.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_SEE_ROLLOVER_ICON)));
		sehen.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_see")));

		sehen.addActionListener(this);

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 3; // X
		gc.gridy = 0; // y
		gc.ipadx = 0;

		manager.add(sehen, gc);

		/************ BUTTON File ************************************/
		docfile.setIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_DOC_FILE)));
		docfile.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_DOC_FILE_ROLLOVER_ICON)));
		docfile.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_doc")));

		docfile.addActionListener(this);

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 4; // X
		gc.gridy = 0; // y
		gc.ipadx = 0;

		manager.add(docfile, gc);

		/************ BUTTON File ************************************/
		table.setIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_TABLE_ICON)));
		table.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_TABLE_ROLLOVER_ICON)));
		table.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_table")));

		table.addActionListener(this);

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 5; // X
		gc.gridy = 0; // y
		gc.ipadx = 0;

		manager.add(table, gc);

		/** LABEL ************************************/
		label.setFont(new Font("comic sans ms", 0, 13));

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(5, 0, 0, 0);
		gc.weightx = 0.1;
		gc.gridx = 6; // X
		gc.gridy = 0; // y
		gc.ipadx = 0;
		gc.ipady = 0;

		manager.add(label, gc);

		/************ PROGESSBAR ************************************/
		progressBarInd.setIndeterminate(true);
		progressBarInd.setVisible(false);

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(10, 0, 5, 0);
		gc.weightx = 0.1;
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.gridx = 7;
		gc.gridy = 0;
		manager.add(progressBarInd, gc);

		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		manager.add(progressBar, gc);

		/************ HELP ************************************/
		help.setIcon(new ImageIcon(getClass().getResource(PATH_BREMO_HELP_ICON)));
		help.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_HELP_ROLLOVER_ICON)));
		help.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_help")));

		help.addActionListener(this);

		gc.fill = GridBagConstraints.LAST_LINE_END;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 8;
		gc.gridy = 0;

		manager.add(help, gc);

		/************ TEXTAREA GROSAREA OUTPUT ************************************/
		grosArea.setEditable(false);
		grosArea.setFont(new Font("comic sans ms", 1, 13)); // NOI18N
		grosArea.setMinimumSize(new Dimension(76, 22));

		// Mit der Kombinationstatste von " Strg + F"
		// kann man in diese TextPane Wörten suchen
		grosArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				searchInTextPane(e, grosArea);
			}
		});

		konsole.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 660;
		c.weighty = 420;

		jScrollPane1.setViewportView(grosArea);
		jScrollPane1.setBounds(13, 20, 660, 420);
		jScrollPane1.setVisible(false);
		konsole.add(jScrollPane1, c);

		/************ TEXTAREA KLEINAREA OUTPUT ************************************/
		kleinArea.setEditable(false);
		kleinArea.setFont(new Font("comic sans ms", 1, 13));

		// Mit der Kombinationstatste von " Strg + F"
		// kann man in diese TextPane Wörten suchen
		kleinArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				searchInTextPane(e, kleinArea);
			}
		});

		jScrollPane2.setViewportView(kleinArea);
		c.gridwidth = 1;
		c.weighty = 180;
		jScrollPane2.setBounds(13, 450, 660, 180);
		konsole.add(jScrollPane2, c);

		/************ FRAME LAYOUT EINSTELUNG ************************************/

		// Diese Layout ist sehr gut für komplexe darstellung aber kanna uch
		// komplex zu verstehen
		// wenn sie nicht gut verstehen, ich bitte sie um Java Doc über
		// "GroupLayout" zu lesen.

		GroupLayout layout = new GroupLayout(getContentPane());

		getContentPane().setLayout(layout);
		// Horizontal Group Einstellung
		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.TRAILING)
								.addComponent(manager,
										GroupLayout.Alignment.LEADING,
										GroupLayout.DEFAULT_SIZE, 685,
										Short.MAX_VALUE)
								.addComponent(konsole,
										GroupLayout.Alignment.LEADING,
										GroupLayout.DEFAULT_SIZE, 685,
										Short.MAX_VALUE))));
		// Verticale Group Einstellung
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addComponent(manager, GroupLayout.PREFERRED_SIZE, 38,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(konsole, GroupLayout.DEFAULT_SIZE, 180,
								Short.MAX_VALUE)));
		/*****************************************************************************************/
		pack();

	}

	/**
	 * Call the View Frame to plot Graphic
	 */
	public void callBremoView() {
		// geruft wenn mann vorher
		// keine rechnung gemacht hat

		BremoView view = new BremoView(new BremoViewModel());

		view.getController().getModel().addObserver(view);

		view.setVisible(true);

	}

	/**
	 * Call the View Frame to plot Graphic with the Speccific file
	 */
	public void callBremoViewWhitFile(File file) {
		// geruft wenn mann eine Berechnung durchgefuhrt hat
		// und ergebniss als Graphic anschauen will

		BremoView view = new BremoView(new BremoViewModel());

		view.getController().getModel().addObserver(view);

		try {
			view.getController().SendFileModel(file, true);
		} catch (IOException e) {
			new BremoInfoFrame(ManagerLanguage.getString("Error"),
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}

		view.setVisible(true);

	}

	/**
	 * Event To Search in TextPane
	 */
	private void searchInTextPane(KeyEvent e, JTextPane pane) {
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
			if (!pane.getText().equals("")) {
				if (suche != null) {
					suche.dispose();
				}
				suche = new SucheBremo(pane);
			}
		}
	}

	/**
	 * get the KleinArea
	 * 
	 * @return
	 */
	public JTextPane getKleinArea() {
		return kleinArea;
	}

	/**
	 * get the GrosArea
	 */
	public JTextPane getGrosArea() {
		return grosArea;
	}

	/**
	 * Set the Size of the Console
	 */
	public void setSizeConsole(int width, int height) {
		// Size von der Konsole ändern sich abhängig von der Mode.

		if (height <= 320) { // Normale Mode
			jScrollPane1.setVisible(false);
			setSize(width, height);
		} else { // Debug Mode
			jScrollPane1.setVisible(true);
			setSize(width, height);
			BremoSwingUtil.placeFrame(this);
		}
	}

	/**
	 * Change the Component of GUI when Verlustteilung is Enable to the Process
	 */
	public void VerlustteilungModeEnable() {
		progressBar.setVisible(false);
		progressBarInd.setVisible(true);
		stop.setEnabled(false);
		label.setText(ManagerLanguage.getString("swingbremo_label_11"));
	}

	/**
	 * close the Frame with the yes or No Dialogue
	 */
	public void closeFrame() {
		URL url = getClass().getResource(PATH_BREMO_CLOSE_FRAME_ICON);
		ImageIcon icon = new ImageIcon(url);
		final JOptionPane optionPane = new JOptionPane(
				ManagerLanguage.getString("swingbremo_warning_message_close"),
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, icon);

		final JDialog dialog = optionPane.createDialog(this, "Exit");

		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();

				if (dialog.isVisible() && (e.getSource() == optionPane)
						&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
					dialog.setVisible(false);
				}
			}
		});
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
		try {
			int value = ((Integer) optionPane.getValue()).intValue();
			if (value == JOptionPane.YES_OPTION) {
				System.exit(0);
			} else if (value == JOptionPane.NO_OPTION) {

			}
		} catch (NullPointerException npe) {

		}
	}

	/**
	 * Decorate simple Text to text whit Font and html tag only for Tooltip
	 * 
	 * @param Text
	 * @return
	 */
	public String getHtmlToolTip(String Text) {
		return "<html><font face =\"comic sans ms\">" + Text + "</font></html>";
	}

	/**
	 * Action to perform for Component when User Choosefile ( start )
	 */
	public void ActionComponent_wahlfile_start() {
		grosArea.setText("");
		kleinArea.setText("");
		label.setText("");
		progressBar.setValue(0);
	}

	/**
	 * Action to perform for Component when User start calcul ( start )
	 */
	public void ActionComponent_Berechnen_start() {
		ChangeTextLabel(ManagerLanguage.getString("swingbremo_label_3"));
		grosArea.setText("");
		kleinArea.setText("");
		System.currentTimeMillis();
		berechnen.setEnabled(false);
		wahlFile.setEnabled(false);
		stop.setEnabled(false);
		sehen.setEnabled(false);
		progressBarInd.setVisible(true);

	}

	/**
	 * Action to perform for Component when User start calcul ( End )
	 */
	public void ActionComponent_Berechnen_end() {

		berechnen.setEnabled(true);
		wahlFile.setEnabled(true);
		stop.setEnabled(false);
		sehen.setEnabled(true);
		progressBar.setVisible(false);
		progressBarInd.setVisible(false);
		ChangeTextLabel(ManagerLanguage.getString("swingbremo_label_9"));
	}

	/**
	 * Action to perform for Component when Bremo the calcul starting
	 */
	public void ActionComponent_BremoActiv() {
		progressBar.setVisible(true);
		progressBarInd.setVisible(false);
		stop.setEnabled(true);
		ChangeTextLabel(ManagerLanguage.getString("swingbremo_label_1"));
	}

	/**
	 * to Change the text of the Label from
	 * 
	 * @param text
	 */
	public void ChangeTextLabel(String text) {
		label.setText(text);
	}

	/**
	 * set the Value of the progressbar
	 * 
	 * @param percent
	 */
	public void setProgressValue(int percent) {
		progressBar.setValue(percent);
	}

	/**
	 * Get the Swingbremo View Controller
	 * 
	 * @return controller
	 */
	public SwingBremoController getController() {
		return controller;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Listener to perform click Buton action
		switch (((Component) e.getSource()).getName()) {

		case BUTTON_BERCHNEN:
			controller.berechnen();

			break;

		case BUTTON_WAHLFILE:
			controller.wahl();
			break;

		case BUTTON_STOP:
			controller.stop();
			break;

		case BUTTON_GRAPHIC:
			controller.graphic();
			break;

		case BUTTON_DOCFILE:
			controller.FileEditor();
			break;

		case BUTTON_TABLE:
			controller.table();
			break;

		case BUTTON_HELP:
			controller.help();
			break;
		default:
			AusgabeSteurung.Error("find no component list !");
			break;
		}

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {

		/* Set the Nimbus look and feel */

		// Tooltip Font ändern
		UIManager.put("ToolTip.font", new FontUIResource(new Font(
				"comic sans ms", 2, 20)));
		// TitledBorder Font ändern
		UIManager.put("TitledBorder.font", new FontUIResource(new Font(
				"comic sans ms", 2, 16)));
		// Label Font ändern
		UIManager.put("Label.font", new FontUIResource(new Font(
				"comic sans ms", 2, 14)));
		// Farbe der ProgressBar ändern
		UIManager.put("nimbusOrange", new ColorUIResource(28, 138, 224));
		try {
			for (UIManager.LookAndFeelInfo info : UIManager
					.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(SwingBremo.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(SwingBremo.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(SwingBremo.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(SwingBremo.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				SwingBremo swingBremo;
				swingBremo = new SwingBremo();
				SwingUtilities.updateComponentTreeUI(swingBremo);
				swingBremo.setVisible(true);
			}
		});

	}

}
