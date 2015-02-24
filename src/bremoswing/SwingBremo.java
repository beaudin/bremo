package bremoswing;

import io.AusgabeSteurung;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.Locale;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import bremo.main.Bremo;
import bremoswing.graphik.BremoView;
import bremoswing.graphik.BremoViewModel;
import bremoswing.graphik.SelectItemToPlotten;
import bremoswing.manager.ManagerLanguage;
import bremoswing.util.BremoExtensionFileFilter;
import bremoswing.util.ExtensionFileFilter;
import bremoswing.util.FertigMeldungFrame;
import bremoswing.util.PaintPanel;
import bremoswing.util.SucheBremo;
import bremoswing.util.TextPaneOutputStream;
import bremoswing.util.ToolTipFrame;
import funktionenTests.ShowTable;

/*
 * SwingBremo.java
 *
 *

 /**
 * @author Ngueneko Steve
 */
public class SwingBremo extends JFrame {

	private static final long serialVersionUID = 1L;

	/************************** Variables declaration ****************************************/
	public String RevisionNumber = getRevisionNumber();
	private final String title = "Bremo 2.0 rev " + RevisionNumber + " Beta";
	public static boolean ConsloseModeActive = false;
	private boolean control = false;
	private static File[] files;
	public Double[] percentBremo;
	public static String path;
	private static SucheBremo suche;
	public static ThreadGroup group;
	public static Bremo[] bremoThread;
	public static String[] bremoThreadFertig;
	public static JButton berechnen;
	public static JButton sehen;
	public static JButton docfile;
	public static JButton help;
	public static JButton table;
	public static JTextPane grosArea;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	public static JTextPane kleinArea;
	private JPanel konsole;
	private PaintPanel manager;
	public static JButton stop;
	private static JTextField textFile;
	public static JButton wahlFile;
	public static boolean DebuggingMode;
	public static int NrOfFile;
	public static int NrBremoAlive;
	public static JProgressBar progressBar;
	public static JProgressBar progressBarInd;
	public static JLabel label;
	public static Timer timerUpdate;
	public static Timer timerCalcul;
	public static int percent;
	static SelectItemToPlotten plotten;
	public static double startTime;
	public static File BremoAppletDirectory;

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

	public static String PATH_FROZE_BLUE = "/bremoswing/bild/Abstract_Frozen_Blue.jpg";
	public static String PATH_BREMO_APP_ICON = "/bremoswing/bild/bremo1.png";
	public String PATH_BREMO_CHOOSEFILE_ICON = "/bremoswing/bild/folder_blue.png";
	public String PATH_BREMO_PLAY_ICON = "/bremoswing/bild/play_blue.png";
	public String PATH_BREMO_STOP_ICON = "/bremoswing/bild/stop_blue.png";
	public String PATH_BREMO_SEE_ICON = "/bremoswing/bild/see_graphik.png";
	public String PATH_BREMO_TABLE_ICON = "/bremoswing/bild/table-icon-1.png";
	public String PATH_BREMO_HELP_ICON = "/bremoswing/bild/help.png";

	public String PATH_BREMO_CHOOSEFILE_ROLLOVER_ICON = "/bremoswing/bild/folder_blue_2.png";
	public String PATH_BREMO_PLAY_ROLLOVER_ICON = "/bremoswing/bild/play_blue_2.png";
	public String PATH_BREMO_SEE_ROLLOVER_ICON = "/bremoswing/bild/see_graphik-2.png";
	public String PATH_BREMO_TABLE_ROLLOVER_ICON = "/bremoswing/bild/table-icon-2.png";
	public String PATH_BREMO_HELP_ROLLOVER_ICON = "/bremoswing/bild/help-2.png";

	public String PATH_BREMO_HELP_HTML = "src/bremoswing/help/BremoHelp.html";

	public String PATH_BREMO_CLOSE_FRAME_ICON = "/bremoswing/bild/questionIcon.png";
	public static String PATH_BREMO_FILEPATH = "src/bremoswing/utilFile/bremo.path";
	public static String PATH_BREMO_FILE_SVN = "/bremoswing/properties/bremo.svnversion";

	public String LOCALE_GERMANY_LANGUAGE = "de, DE";
	public String LOCALE_ENGLISH_LANGUAGE = "en, EN";
	public String USER_PROPERTY = "user.home";

	/************************** End of variables declaration ****************************************/

	/***** Creates new form SwingBremo *************************************************************/

	public SwingBremo() {
		initComponents();
		placeFrame(this);
	}

	/***
	 * This method is called from within the constructor to initialize the form.
	 * 
	 * @throws IOException
	 ***********************/
	private void initComponents() {

		BremoAppletDirectory = new File(System.getProperty(USER_PROPERTY)
				+ File.separator + "BremoFile" + File.separator);

		ManagerLanguage.managerLanguage(new Locale(LOCALE_GERMANY_LANGUAGE));

		kleinArea = new JTextPane();

		grosArea = new JTextPane();

		outStream = new PrintStream(System.out, true) {

			@Override
			public void println(String s) {
				if (DebuggingMode) {
					ActiveConsole();
					appendTextPane(grosArea, s + "\n", Color.blue);
				} else {
					ActiveConsole();
				}
			}

			@Override
			public void print(String s) {
				if (DebuggingMode) {
					ActiveConsole();
					appendTextPane(grosArea, s, Color.blue);
				} else {
					ActiveConsole();
				}
			}
		};

		errStream = new PrintStream(System.err, true) {

			@Override
			public void println(String s) {
				isDebugMode();
				appendTextPane(kleinArea, s + "\n", Color.RED);
			}

			@Override
			public void print(String s) {
				isDebugMode();
				appendTextPane(kleinArea, s, Color.RED);
			}
		};

		outputStreamKleinArea = new TextPaneOutputStream(kleinArea);

		streamOut = new PrintStream(outputStreamKleinArea, true) {

			@Override
			public synchronized void println(String s) {
				// outputStreamKleinArea.SetTextColor(new Color (215,120,11));
				super.println(s);
			}

			@Override
			public synchronized void print(String s) {
				// outputStreamKleinArea.SetTextColor(new Color (215,120,11));
				super.print(s);
			}
		};
		System.setOut(outStream);
		// System.setErr(errStream); //Hier auskommentieren um Konsole
		// umzuschalten zwischen Eclipse/Bremo-GUI

		System.setErr(System.err);

		// JMenu m = new JMenu("Datei");
		// m.add(new JMenuItem("Item 1.1"));
		// m.add(new JMenuItem("Item 1.2"));
		// m.add(new JMenuItem("Item 1.3"));
		// JMenuBar mb = new JMenuBar();
		// mb.add(m);
		// setJMenuBar(mb);

		manager = new PaintPanel(PATH_FROZE_BLUE, 200, 210, 750, 40);
		konsole = new PaintPanel(PATH_FROZE_BLUE, 200, 250, 750, 750);

		berechnen = new JButton();
		wahlFile = new JButton();
		stop = new JButton();
		sehen = new JButton();
		docfile = new JButton();
		help = new JButton();
		table = new JButton();
		textFile = new JTextField();

		jScrollPane1 = new JScrollPane();
		jScrollPane2 = new JScrollPane();

		progressBar = new JProgressBar(0, 100);
		progressBarInd = new JProgressBar(0, 100);

		label = new JLabel(" ");
		percent = 0;
		path = loadPathFromFile();
		DebuggingMode = false;
		NrOfFile = 0;
		NrBremoAlive = 0;

		/****** HAUPT FRAME ****************************************************************/
		setTitle(title);
		setBackground(new Color(255, 255, 255));
		setIconImage(new ImageIcon(getClass().getResource(PATH_BREMO_APP_ICON))
				.getImage());
		setName(ManagerLanguage.getString("swingbremo_app_titel"));
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Dimension size = new Dimension(720, 320);
		setPreferredSize(size);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeFrame();
			}
		});

		// manager.setBorder(BorderFactory.createTitledBorder(null, "Manager",
		// TitledBorder.DEFAULT_JUSTIFICATION,
		// TitledBorder.DEFAULT_POSITION, new Font("comic sans ms", 1, 14)));

		// manager.setLayout(new GridBagLayout());
		// FlowLayout experimentLayout = new FlowLayout();
		// experimentLayout.setAlignment(FlowLayout.LEADING);
		// manager.setLayout(experimentLayout);

		manager.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 0;

		/************ BUTTON BERECHNEN ************************************/
		ImageIcon beri = new ImageIcon(getClass().getResource(
				PATH_BREMO_PLAY_ICON));
		berechnen.setIcon(beri);
		berechnen.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_PLAY_ROLLOVER_ICON)));
		berechnen.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_play")));
		berechnen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop.setVisible(true);
				sehen.setEnabled(true);
				docfile.setEnabled(false);
				percent = 0;
				BerechnungPush(e);

			}
		});

		manager.add(berechnen, gc);

		/************ BUTTON WAHLFILE ************************************/
		ImageIcon wfi = new ImageIcon(getClass().getResource(
				PATH_BREMO_CHOOSEFILE_ICON));
		wahlFile.setIcon(wfi);
		wahlFile.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_CHOOSEFILE_ROLLOVER_ICON)));
		wahlFile.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_choose")));
		wahlFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				wahlPush(e);
			}

		});
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 1;
		gc.gridy = 0;
		gc.ipadx = 0;

		manager.add(wahlFile, gc);

		/************ BUTTON STOP ************************************/
		stop.setIcon(new ImageIcon(getClass().getResource(PATH_BREMO_STOP_ICON)));
		stop.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_stop")));
		stop.setEnabled(false);
		stop.setMargin(new Insets(0, 0, 0, 0));
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				stopPush(e);
			}
		});
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 2;
		gc.gridy = 0;
		gc.ipadx = 0;

		manager.add(stop, gc);

		/************ BUTTON Graphic ************************************/
		sehen.setIcon(new ImageIcon(getClass().getResource(PATH_BREMO_SEE_ICON)));
		sehen.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_SEE_ROLLOVER_ICON)));
		sehen.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_see")));
		sehen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (bremoThreadFertig != null) {
					if (bremoThreadFertig[0] != null) {
						plotten = new SelectItemToPlotten();
					} else {
						SelectItemToPlotten.callBremoView();
					}
				} else {
					SelectItemToPlotten.callBremoView();
				}
			}

		});
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 3;
		gc.gridy = 0;
		gc.ipadx = 0;
		sehen.setEnabled(true);
		manager.add(sehen, gc);

		// /************ BUTTON File ************************************/
		// docfile.setIcon(new ImageIcon(getClass().getResource(
		// "/bremoswing/bild/doc-icon.png")));
		// docfile.setRolloverIcon(new ImageIcon(getClass().getResource(
		// "/bremoswing/bild/doc-icon2.png")));
		// docfile.setToolTipText(ManagerLanguage.getString("swingbremo_ToolTip_doc"));
		// //
		// docfile.setToolTipText(languageManager.getJButtonTextByID("swingbremo_ToolTip_doc"));
		// //swingbremo_ToolTip_doc
		// docfile.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// }
		// });
		// gc.fill = GridBagConstraints.NONE;
		// gc.insets = new Insets(0, 0, 0, 0);
		// gc.weightx = 0;
		// gc.gridx = 4;
		// gc.gridy = 0;
		// gc.ipadx = 0;
		// docfile.setEnabled(true);
		// manager.add(docfile, gc);

		/************ BUTTON File ************************************/
		String PATH_BREMO_TABLE_ICON = "/bremoswing/bild/table-icon-1.png";
		table.setIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_TABLE_ICON)));
		table.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_TABLE_ROLLOVER_ICON)));
		table.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_table")));
		table.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				tablePush();
			}
		});
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 4;
		gc.gridy = 0;
		gc.ipadx = 0;
		docfile.setEnabled(true);
		manager.add(table, gc);

		/************************* COMBO BOX LANGUAGE ****************************/
		// language.setEditable(false);
		// language.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// int index = language.getSelectedIndex();
		// if (index == 0) {
		//
		// ManagerLanguage.setLocale(new Locale("de, DE"));
		// ManagerLanguage.managerLanguage(Locale.GERMANY);
		// updateLanguage();
		//
		// } else if(index == 1) {
		//
		// ManagerLanguage.setLocale(new Locale("en, EN"));
		// ManagerLanguage.managerLanguage(Locale.ENGLISH);
		// updateLanguage();
		//
		//
		// } else if (index == 2) {
		// ManagerLanguage.setLocale(new Locale("fr, FR"));
		// ManagerLanguage.managerLanguage(Locale.FRANCE);
		// updateLanguage();
		// }
		// }
		// });
		// gc.fill = GridBagConstraints.NONE;
		// gc.insets = new Insets(0, 0, 0, 0);
		// gc.weightx = 0;
		// gc.gridx = 5;
		// gc.gridy = 0;
		// gc.ipadx = 0;
		// manager.add(language, gc);

		/** LABEL ************************************/
		label.setFont(new Font("comic sans ms", 0, 13));

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(5, 0, 0, 0);
		gc.weightx = 0.1;
		gc.gridx = 6;
		gc.gridy = 0;
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

		timerCalcul = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				boolean bremoActiv = false;
				for (Bremo bremo : bremoThread) {
					if (bremo.get_myCaseState())
						bremoActiv = true;
				}
				if (bremoActiv) {
					isDebugMode();
					label.setText(ManagerLanguage
							.getString("swingbremo_label_1"));
					progressBar.setVisible(true);
					double Sum = 0;
					progressBarInd.setVisible(false);
					stop.setEnabled(true);
					for (int i = 0; i < bremoThread.length; i++) {
						try {
							Sum = Sum
									+ (bremoThread[i].get_myCase().get_time() / bremoThread[i]
											.get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC);
						} catch (Exception e) {

						}
					}
					percent = (int) ((Sum / NrBremoAlive) * 100);
					progressBar.setValue(percent);
					progressBar.repaint();
				} else {
					label.setText(ManagerLanguage
							.getString("swingbremo_label_2"));
				}
			}
		});

		gc.fill = GridBagConstraints.LAST_LINE_END;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 8;
		gc.gridy = 0;
		ImageIcon hilfe = new ImageIcon(getClass().getResource(
				PATH_BREMO_HELP_ICON));
		help.setIcon(hilfe);
		help.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_HELP_ROLLOVER_ICON)));
		help.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_help")));
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		});

		manager.add(help, gc);

		/************ TEXTFIELD TEXTFILE ************************************/
		textFile.setEditable(false);
		textFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		textFile.setEnabled(false);
		textFile.setMaximumSize(new Dimension(0, 0));
		textFile.setMinimumSize(new Dimension(0, 0));
		textFile.setPreferredSize(new Dimension(0, 0));
		textFile.setBounds(180, 20, 0, 0);
		// gc.fill = GridBagConstraints.LAST_LINE_END;
		// gc.insets = new Insets(0, 0, 0, 0);
		// gc.weightx = 0.5;
		// //gc.gridx = 4;
		// gc.gridy = 0;
		// gc.ipadx = 0;
		// gc.ipady = 0;

		/************ PANEL KONSOLE ************************************/
		// konsole2.setBorder(BorderFactory.createTitledBorder(null, "Konsole",
		// TitledBorder.DEFAULT_JUSTIFICATION,
		// TitledBorder.DEFAULT_POSITION, new Font("comic sans ms", 1, 14))); //
		// NOI18N

		/************ TEXTAREA GROSAREA OUTPUT ************************************/
		// grosArea.setColumns(20);
		grosArea.setEditable(false);
		grosArea.setFont(new Font("comic sans ms", 1, 13)); // NOI18N
		// grosArea.setRows(5);
		// grosArea.setText("Programm läuft... \nWählen Sie die Input Datei Und Dann Einfach die Berechnung Ausführen .");
		grosArea.setMinimumSize(new Dimension(76, 22));
		grosArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				searchInTextPane(e);
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
		kleinArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
					if (!kleinArea.getText().equals("")) {
						if (suche != null) {
							suche.dispose();
						}
						suche = new SucheBremo(kleinArea);
					}
				}
			}
		});

		jScrollPane2.setViewportView(kleinArea);
		c.gridwidth = 1;
		c.weighty = 180;
		jScrollPane2.setBounds(13, 450, 660, 180);
		konsole.add(jScrollPane2, c);

		// JToolBar toolBar = new JToolBar("BremoManager");
		// toolBar.setFloatable(true);
		// toolBar.setRollover( true);
		// toolBar.setMargin(new Insets(5,10 , 5, 10));
		//
		// toolBar.add(berechnen);
		// toolBar.add(wahlFile);
		// toolBar.add(stop);
		// toolBar.addSeparator(new Dimension(100,0));
		// toolBar.add(label);
		// toolBar.addSeparator(new Dimension(100,0));
		//
		// toolBar.add(progressBarInd);
		// toolBar.add(progressBar);
		getContentPane().setBackground(new Color(225, 225, 225));

		// manager.add(toolBar, BorderLayout.PAGE_START);
		/************ FRAME LAYOUT EINSTELUNG ************************************/
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup()
				// .addContainerGap()
						.addGroup(
								layout.createParallelGroup(
										GroupLayout.Alignment.TRAILING)
										.addComponent(manager,
												GroupLayout.Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE, 685,
												Short.MAX_VALUE)
										.addComponent(konsole,
												GroupLayout.Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE, 685,
												Short.MAX_VALUE))
		/* .addContainerGap() */));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						// .addContainerGap()
						.addComponent(manager, GroupLayout.PREFERRED_SIZE, 38,
								GroupLayout.PREFERRED_SIZE)
						// .addPreferredGap(
						// LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(konsole, GroupLayout.DEFAULT_SIZE, 180,
								Short.MAX_VALUE)/* .addContainerGap() */));
		/*****************************************************************************************/
		pack();

	}

	/**
	 * Call the View Frame to plot Graphic
	 */
	public void callBremoView() {

		BremoView view = new BremoView(new BremoViewModel());

		view.getController().getModel().addObserver(view);

		view.setVisible(true);

	}

	/**
	 * Call the View Frame to plot Graphic with the Speccific file
	 */
	public void callBremoViewWhitFile(File file) {

		BremoView view = new BremoView(new BremoViewModel());

		view.getController().getModel().addObserver(view);

		try {
			view.getController().SendFileModel(file, true);
		} catch (IOException e) {
			new FertigMeldungFrame(ManagerLanguage.getString("Error"),
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}

		view.setVisible(true);

	}

	/** Warning Message **********/
	protected void messsage() {
		new FertigMeldungFrame(ManagerLanguage.getString("warning"),
				ManagerLanguage.getString("swingbremo_warning_message_1")
						+ ManagerLanguage.getString("swingbremo_point")
						+ ManagerLanguage
								.getString("swingbremo_warning_message_2"),
				JOptionPane.WARNING_MESSAGE);
	}

	/**** Button Setting To Run Bremo Thread / to Run the calcul ***********/
	private void BerechnungPush(ActionEvent e) {
		grosArea.setText("");
		kleinArea.setText("");
		if (textFile.getText().equals("")) {
			new FertigMeldungFrame(ManagerLanguage.getString("warning"),
					ManagerLanguage.getString("swingbremo_warning_message_3"),
					JOptionPane.WARNING_MESSAGE);
			wahlFile.setVisible(true);
			berechnen.setVisible(true);
			docfile.setVisible(true);
		} else {
			startTime = System.currentTimeMillis();
			berechnen.setEnabled(false);
			wahlFile.setEnabled(false);
			stop.setEnabled(false);
			control = true;

			try {
				for (int i = 0; i < files.length; i++) {

					bremoThread[i].start();

				}

				progressBarInd.setVisible(true);

				label.setText(ManagerLanguage.getString("swingbremo_label_3"));
				timerCalcul.start();
				stop.setEnabled(false);

			} catch (IllegalThreadStateException e1) {
				e1.printStackTrace();

				ActiveIcon();
				label.setText(ManagerLanguage.getString("swingbremo_label_4"));
			}

		}

	}

	/**
	 * Event To Search in TextPane
	 */
	private void searchInTextPane(KeyEvent e) {
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
			if (!grosArea.getText().equals("")) {
				if (suche != null) {
					suche.dispose();
				}
				suche = new SucheBremo(grosArea);
			}
		}
	}

	/**
	 * Event To Choose a File
	 */
	private void wahlPush(ActionEvent e2) {
		grosArea.setText("");
		kleinArea.setText("");
		berechnen.setEnabled(false);
		label.setText("");
		progressBar.setValue(0);
		SetDebbugingMode(false);
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
		try {
			label.setText(ManagerLanguage.getString("swingbremo_label_5"));

			int status = fileChooser.showOpenDialog(getRootPane());

			if (status == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					files = fileChooser.getSelectedFiles();
					textFile.setText(files[0].getPath());
					updatePath(files[0].getParent());
					bremoThread = new Bremo[files.length];
					group = new ThreadGroup("BremoFamily");
					for (int i = 0; i < bremoThread.length; i++) {
						bremoThread[i] = new Bremo(group, files[i]);
					}
					bremoThreadFertig = new String[files.length];
					NrOfFile = files.length;
					NrBremoAlive = files.length;
					label.setText(ManagerLanguage
							.getString("swingbremo_label_6"));

					ActiveConsole();
				}
			} else if (status == JFileChooser.CANCEL_OPTION) {

				label.setText(ManagerLanguage.getString("swingbremo_label_7"));

				fileChooser.cancelSelection();
			}
		} catch (Exception e) {
			label.setText(ManagerLanguage.getString("swingbremo_label_8"));

			e.printStackTrace();
		}
		berechnen.setEnabled(true);
	}

	/**
	 * Event To stop the Bremo Process
	 */
	private void stopPush(ActionEvent a) {
		if (control) {
			streamOut.flush();
			group.interrupt();
			streamOut.close();
			outStream.close();
			control = false;
			ActiveIcon();
		}
	}

	/**
	 * Event To Show a JTABLE
	 */
	private void tablePush() {

		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
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
		try {
			label.setText(ManagerLanguage.getString("swingbremo_label_5"));
			int status = fileChooser.showOpenDialog(getRootPane());

			if (status == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					File f = fileChooser.getSelectedFile();
					textFile.setText(f.getPath());
					updatePath(f.getParent());
					new ShowTable(f);
					label.setText(ManagerLanguage
							.getString("swingbremo_label_6"));
				}
			} else if (status == JFileChooser.CANCEL_OPTION) {

				label.setText(ManagerLanguage.getString("swingbremo_label_7"));
				fileChooser.cancelSelection();
			}
		} catch (Exception e) {
			label.setText(ManagerLanguage.getString("swingbremo_label_8"));
			e.printStackTrace();
		}
	}

	/**
	 * Enable the Icon on the GUI
	 */
	public static void ActiveIcon() {
		timerCalcul.stop();
		wahlFile.setEnabled(true);
		berechnen.setEnabled(true);
		stop.setEnabled(false);
		label.setText(ManagerLanguage.getString("swingbremo_label_9"));
		progressBar.setValue(0);
		progressBar.setVisible(false);
		progressBarInd.setVisible(false);
		textFile.setText("");
	}

	/**
	 * Change the Mode of The Console When DebbugMode is Active
	 */
	public void ActiveConsole() {

		if (NrOfFile > 1) {
			jScrollPane1.setVisible(false);
			setSize(710, 320);
			DebuggingMode = false;
		} else if (NrOfFile == 1) {
			if (!DebuggingMode) {
				jScrollPane1.setVisible(false);
				setSize(710, 320);
			} else {
				jScrollPane1.setVisible(true);
				setSize(710, 750);
			}
		}
	}

	public static void SetDebbugingMode(Boolean mode) {
		if (NrOfFile == 1) {
			DebuggingMode = mode;
		}
	}

	public static void setNrOfBremoAlive() {
		NrBremoAlive--;
	}

	public static void PutInBremoThreadFertig(String s) {
		for (int i = 0; i < bremoThreadFertig.length; i++) {
			if (bremoThreadFertig[i] != null)
				continue;
			else
				bremoThreadFertig[i] = s;
			break;

		}
	}

	/**
	 * SEE IF THE RUNNING OPERATION ARE FINISHED
	 */
	public static void StateBremoThread() {

		if (group.activeCount() <= 1) {
			timerCalcul.stop();

			if (bremoThreadFertig[0] != null) {

				label.setText(ManagerLanguage.getString("swingbremo_label_10"));
				PopUp(ManagerLanguage.getString("swingbremo_popup_titel"),
						ManagerLanguage.getString("swingbremo_popup_message"));
			}
			for (String str : bremoThreadFertig) {
				if (str != null)
					AusgabeSteurung.Error(ManagerLanguage.getString("thread")
							+ " "
							+ str
							+ " "
							+ ManagerLanguage
									.getString("warning_message_terminate"));
			}
			ActiveIcon();
			progressBar.setVisible(false);
			progressBarInd.setVisible(false);
			docfile.setEnabled(true);
			if (bremoThreadFertig.length > 0) {
				sehen.setEnabled(true);
			}
		}
	}

	/**
	 * Change the GUI when Verlustteilung is Enable to the Process
	 */
	public static void VerlustteilungModeEnable() {
		timerCalcul.stop();
		progressBar.setVisible(false);
		progressBarInd.setVisible(true);
		stop.setEnabled(false);
		label.setText(ManagerLanguage.getString("swingbremo_label_11"));
	}

	/**
	 * Interface to Extern choose of the InputFile
	 * 
	 * @throws IOException
	 */
	public static void ExtAuswahlFile(File[] fileSaver) throws IOException {
		grosArea.setText("");
		kleinArea.setText("");
		berechnen.setEnabled(false);
		label.setText("");
		DebuggingMode = false;
		progressBar.setValue(0);

		files = fileSaver;
		textFile.setText(files[0].getPath());
		updatePath(files[0].getParent());
		bremoThread = new Bremo[files.length];
		group = new ThreadGroup("BremoFamily");
		for (int i = 0; i < bremoThread.length; i++) {
			bremoThread[i] = new Bremo(group, files[i]);
		}
		bremoThreadFertig = new String[files.length];
		NrOfFile = files.length;
		NrBremoAlive = files.length;
		if (NrOfFile == 1)
			SetDebbugingMode(true);
		label.setText(ManagerLanguage.getString("swingbremo_label_12"));
	}

	/**
	 * place Frame to the center
	 */
	public static void placeFrame(JFrame frame) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle window = frame.getBounds();
		frame.setLocation((screen.width - window.width) / 2,
				(screen.height - window.height) / 2);
	}

	/**
	 * close the Frame
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
	 * Update the Path Variable
	 */
	public static void updatePath(String path) {

		SwingBremo.path = path;
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

	/**
	 * get The revision number of the Project from SVN
	 * 
	 * @return Revision as String
	 */
	public static String getRevisionNumber() {

		BufferedReader br;
		String zeile = "???";
		try {
			URL url = SwingBremo.class.getResource(PATH_BREMO_FILE_SVN);
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
	 * help function to print Text inside JTextPane with a particular Color.
	 * 
	 * @param pane
	 * @param Text
	 * @param color
	 */
	private void appendTextPane(JTextPane pane, String Text, Color color) {

		Document doc = pane.getDocument();
		Style style = pane.addStyle(null, null);
		StyleConstants.setForeground(style, color);
		try {
			doc.insertString(doc.getLength(), Text, style);
		} catch (BadLocationException e) {
		}
		pane.setCaretPosition(doc.getLength() - 1);

	}

	// /**
	// * SwingBremo function to print Warning message for the User.
	// * Can be use in any Class to Stream Message for the User Console.
	// * @param s
	// */
	// public static void printlnWarning(final String s) {
	// try {
	// warningOut.println(s);
	// } catch (Exception e) {
	// System.err.println(s);
	// }
	// }

	/**
	 * Show PopUp to prevent the User.
	 */
	public static void PopUp(String Titel, String message) {

		// JLabel label = new JLabel();
		// label.setOpaque(true);
		// label.setBorder(BorderFactory.createTitledBorder(null, Titel, 0, 0,
		// new java.awt.Font("comic sans ms", 2, 18), Color.red));
		// label.setBackground(new Color (102,178,255));

		String head = "<html>" + message + "</html>";

		ToolTipFrame tooltip = new ToolTipFrame(Titel, head);
		// panel.add(label,BorderLayout.CENTER);

		PopupFactory factory = PopupFactory.getSharedInstance();
		Random random = new Random();
		int x = random.nextInt(1000);
		int y = random.nextInt(1000);
		final Popup popup = factory.getPopup(null, tooltip.getContentPane(), x,
				y);
		popup.show();
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				popup.hide();
			}
		});
		ActionListener hider = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popup.hide();
			}
		};
		// Hide popup in 15 seconds
		Timer timer = new Timer(4000, hider);
		timer.start();

	}

	public String getHtmlToolTip(String Text) {
		return "<html><font face =\"comic sans ms\">" + Text + "</font></html>";
	}

	/**
	 * test if debug Mode muss Active
	 */
	public void isDebugMode() {
		if (DebuggingMode) {
			System.setErr(errStream);
		} else {
			System.setErr(System.err);
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// Tahoma , Font.plaint,
		UIManager.put("ToolTip.font", new FontUIResource(new Font(
				"comic sans ms", 2, 20)));
		UIManager.put("TitledBorder.font", new FontUIResource(new Font(
				"comic sans ms", 2, 16)));
		UIManager.put("Label.font", new FontUIResource(new Font(
				"comic sans ms", 2, 14)));
		UIManager.put("nimbusOrange", new ColorUIResource(28, 138, 224)); // (25,49,187));//Color(110,170,0));
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
