package bremoswing;

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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import bremoswing.graphik.BremoView;
import bremoswing.graphik.BremoViewModel;
import bremoswing.manager.ManagerLanguage;
import bremoswing.util.BremoInfoFrame;
import bremoswing.util.BremoSwingUtil;
import bremoswing.util.PaintPanel;
import bremoswing.util.SucheBremo;

/*
 * SwingBremo.java
 *
 *

 /**
 * @author Ngueneko Steve
 */
public class SwingBremo extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	/************************** Variables declaration ****************************************/
	// private static SwingBremo instanceSwingBremo;

	// public String RevisionNumber = getRevisionNumber();
	private String title = "Bremo 2.0 rev ? Beta";

	private SucheBremo suche;

	private JButton berechnen;
	private JButton wahlFile;
	private JButton stop;
	private JButton sehen;
	private JButton docfile;
	private JButton help;
	private JButton table;

	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JTextPane kleinArea;
	private JTextPane grosArea;

	private PaintPanel konsole;
	private PaintPanel manager;

	private JTextField textFile;

	private JProgressBar progressBar;
	private JProgressBar progressBarInd;

	private JLabel label;

	public final String PATH_FROZE_BLUE = "/bremoswing/bild/Abstract_Frozen_Blue.jpg";
	public final String PATH_BREMO_APP_ICON = "/bremoswing/bild/bremo1.png";
	public final String PATH_BREMO_CHOOSEFILE_ICON = "/bremoswing/bild/folder_blue.png";
	public final String PATH_BREMO_PLAY_ICON = "/bremoswing/bild/play_blue.png";
	public final String PATH_BREMO_STOP_ICON = "/bremoswing/bild/stop_blue.png";
	public final String PATH_BREMO_SEE_ICON = "/bremoswing/bild/see_graphik.png";
	public final String PATH_BREMO_TABLE_ICON = "/bremoswing/bild/table-icon-1.png";
	public final String PATH_BREMO_HELP_ICON = "/bremoswing/bild/help.png";

	public final String PATH_BREMO_CHOOSEFILE_ROLLOVER_ICON = "/bremoswing/bild/folder_blue_2.png";
	public final String PATH_BREMO_PLAY_ROLLOVER_ICON = "/bremoswing/bild/play_blue_2.png";
	public final String PATH_BREMO_SEE_ROLLOVER_ICON = "/bremoswing/bild/see_graphik-2.png";
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

	private SwingBremoController controller;

	/************************** End of variables declaration ****************************************/

	/***** Creates new form SwingBremo *************************************************************/

	public SwingBremo() {
		initComponents();
		BremoSwingUtil.placeFrame(this);
	}

	// public static SwingBremo getInstance() {
	// if (SwingBremo.instanceSwingBremo == null) {
	// SwingBremo.instanceSwingBremo = new SwingBremo();
	// }
	// return SwingBremo.instanceSwingBremo;
	// }

	/***
	 * This method is called from within the constructor to initialize the form.
	 * 
	 * @throws IOException
	 ***********************/
	@SuppressWarnings("static-access")
	private void initComponents() {

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

		textFile = new JTextField();

		jScrollPane1 = new JScrollPane();
		jScrollPane2 = new JScrollPane();

		progressBar = new JProgressBar(0, 100);
		progressBarInd = new JProgressBar(0, 100);

		label = new JLabel(" ");

		title = title.replace("?", controller.getModel().getRevisionNumber());

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

		manager.setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();

		/************ BUTTON BERECHNEN ************************************/
		ImageIcon beri = new ImageIcon(getClass().getResource(
				PATH_BREMO_PLAY_ICON));
		berechnen.setIcon(beri);
		berechnen.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_PLAY_ROLLOVER_ICON)));
		berechnen.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_play")));

		berechnen.addActionListener(this);

		gc.insets = new Insets(0, 0, 0, 0);
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 0;

		manager.add(berechnen, gc);

		/************ BUTTON WAHLFILE ************************************/

		wahlFile.setIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_CHOOSEFILE_ICON)));
		wahlFile.setRolloverIcon(new ImageIcon(getClass().getResource(
				PATH_BREMO_CHOOSEFILE_ROLLOVER_ICON)));
		wahlFile.setToolTipText(getHtmlToolTip(ManagerLanguage
				.getString("swingbremo_ToolTip_choose")));

		wahlFile.addActionListener(this);

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
		stop.addActionListener(this);

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

		sehen.addActionListener(this);

		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.weightx = 0;
		gc.gridx = 3;
		gc.gridy = 0;
		gc.ipadx = 0;

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
		gc.gridx = 4;
		gc.gridy = 0;
		gc.ipadx = 0;

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

		/************ TEXTFIELD TEXTFILE ************************************/
		// textFile.setEditable(false);
		// textFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		// textFile.setEnabled(false);
		// textFile.setMaximumSize(new Dimension(0, 0));
		// textFile.setMinimumSize(new Dimension(0, 0));
		// textFile.setPreferredSize(new Dimension(0, 0));
		// textFile.setBounds(180, 20, 0, 0);

		/************ PANEL KONSOLE ************************************/
		// konsole2.setBorder(BorderFactory.createTitledBorder(null, "Konsole",
		// TitledBorder.DEFAULT_JUSTIFICATION,
		// TitledBorder.DEFAULT_POSITION, new Font("comic sans ms", 1, 14))); //
		// NOI18N

		/************ TEXTAREA GROSAREA OUTPUT ************************************/
		grosArea.setEditable(false);
		grosArea.setFont(new Font("comic sans ms", 1, 13)); // NOI18N
		grosArea.setMinimumSize(new Dimension(76, 22));
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
		// getContentPane().setBackground(new Color(225, 225, 225));

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
			new BremoInfoFrame(ManagerLanguage.getString("Error"),
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}

		view.setVisible(true);

	}

	/** Warning Message **********/
	protected void messsage() {
		new BremoInfoFrame(ManagerLanguage.getString("warning"),
				ManagerLanguage.getString("swingbremo_warning_message_1")
						+ ManagerLanguage.getString("swingbremo_point")
						+ ManagerLanguage
								.getString("swingbremo_warning_message_2"),
				JOptionPane.WARNING_MESSAGE);
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

	//
	// /**
	// * Change the Mode of The Console When DebbugMode is Active
	// */
	// public void ActiveConsole() {
	//
	// if (NrOfFile > 1) {
	// jScrollPane1.setVisible(false);
	// setSize(710, 320);
	// DebuggingMode = false;
	// } else if (NrOfFile == 1) {
	// if (!DebuggingMode) {
	// jScrollPane1.setVisible(false);
	// setSize(710, 320);
	// } else {
	// jScrollPane1.setVisible(true);
	// setSize(710, 750);
	// }
	// }
	// }

	public JTextPane getKleinArea() {
		return kleinArea;
	}

	public JTextPane getGrosArea() {
		return grosArea;
	}

	/**
	 * Set the Size of the Console
	 */
	public void setSizeConsole(int width, int height) {

		if (height <= 320) {
			jScrollPane1.setVisible(false);
			setSize(width, height);
		} else {
			jScrollPane1.setVisible(true);
			setSize(width, height);
			BremoSwingUtil.placeFrame(this);
		}
	}

	/**
	 * Change the GUI when Verlustteilung is Enable to the Process
	 */
	public void VerlustteilungModeEnable() {
		progressBar.setVisible(false);
		progressBarInd.setVisible(true);
		stop.setEnabled(false);
		label.setText(ManagerLanguage.getString("swingbremo_label_11"));
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
	 * to Change the text of the Label from the Controller
	 * 
	 * @param text
	 */
	public void ChangeTextLabel(String text) {
		label.setText(text);
	}

	public void setProgressValue(int percent) {
		progressBar.setValue(percent);
	}

	/**
	 * help function to print Text inside JTextPane with a particular Color.
	 * 
	 * @param pane
	 * @param Text
	 * @param color
	 */
	private synchronized void appendTextPane(JTextPane pane, String Text,
			Color color) {

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
	 * Get the Swingbremo View Controller
	 * 
	 * @return
	 */
	public SwingBremoController getController() {
		return controller;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
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
