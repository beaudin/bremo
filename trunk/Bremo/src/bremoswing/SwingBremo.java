package bremoswing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import java.lang.ThreadGroup;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import bremo.main.Bremo;

/*
 * SwingBremo.java
 *
 */

/**
 * 
 * @author Ngueneko Steve
 */
public class SwingBremo extends JFrame {

	private static final long serialVersionUID = 1L;
	public static  boolean ConsloseModeActive = false;
	/************************** Variables declaration ****************************************/
	private boolean control = false;
	private File[] files;
	public Double [] percentBremo;
	private String path;
	public static ThreadGroup group;
	public static Bremo[] bremoThread;
	public static JButton berechnen;
	//public JCheckBox konsole;
	private JTextArea grosArea;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JTextArea kleinArea;
	private JPanel konsole2;
	private JPanel manager;
	public static JButton stop;
	private JTextField textFile;
	public static JButton wahlFile;
	public static boolean  DebuggingMode ;
	public static int NrOfFile ; 
	public static JProgressBar progressBar;
	public static JButton pb;
	public static JLabel label;
	public static Timer timerUpdate;
	public static Timer timerCalcul;
	public static int percent;
	PrintStream outStream = new PrintStream(System.out) {

		@Override
		public void println(String s) {
			if (DebuggingMode) {
				ActiveConsole();
				grosArea.append(s + "\n");
				grosArea.setCaretPosition(grosArea.getDocument().getLength());
			} else {
				outStream.flush();
				ActiveConsole();
			}
		}

		@Override
		public void print(String s) {
			if (DebuggingMode) {
				ActiveConsole();
				grosArea.append(s);
				grosArea.setCaretPosition(grosArea.getDocument().getLength());
			} else {
				outStream.flush();
                ActiveConsole();
			}
		}
	};
	
	PrintStream errStream = new PrintStream(System.err) {

		@Override
		public void println(String s) {
			kleinArea.append(s + "\n");
			kleinArea.setCaretPosition(kleinArea.getDocument().getLength());

		}

		@Override
		public void print(String s) {
			kleinArea.append(s);
			kleinArea.setCaretPosition(kleinArea.getDocument().getLength());
		}
	};

	/************************** End of variables declaration ****************************************/

	/***** Creates new form SwingBremo *************************************************************/
	public SwingBremo() {
		initComponents();
		placeFrame();
	}

	/***
	 * This method is called from within the constructor to initialize the form.
	 ***********************/
	private void initComponents() {		

		System.setOut(outStream);
		System.setErr(errStream);

		manager = new JPanel();

		berechnen = new JButton();
		wahlFile = new JButton();
		stop = new JButton();
		pb= new JButton();
		
		textFile = new JTextField();

		//konsole = new JCheckBox();
		konsole2 = new JPanel();

		kleinArea = new JTextArea();
		grosArea = new JTextArea();

		jScrollPane1 = new JScrollPane();
		jScrollPane2 = new JScrollPane();
	
		progressBar = new JProgressBar(0, 100);
		label = new JLabel();
		percent = 0;
		path = ".";
        DebuggingMode = false ;
		NrOfFile = 0;
        /****** HAUPT FRAME ****************************************************************/
		setTitle("Bremo 1.0");
		setBackground(new Color(255, 255, 255));
		setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo1.png")).getImage());
		setName("BremoGraphik");
		setResizable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeFrame();
			}
		});

		manager.setBorder(BorderFactory.createTitledBorder(null, "Manager",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 12)));
		manager.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.insets = new Insets(0,0,0,27);
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.ipadx = -30;
		gc.ipady = -12 ;
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 0;
		
		
		/************ BUTTON BERECHNEN ************************************/
		ImageIcon beri = new ImageIcon(getClass().getResource(
				"/bremoswing/bild/play.png"));
		berechnen.setIcon(beri); // NOI18N
		berechnen.setRolloverIcon(new RolloverIcon(beri));
		berechnen.setToolTipText("Berechnung ausführen");
		berechnen.setContentAreaFilled(false);
		berechnen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		berechnen.setVerifyInputWhenFocusTarget(false);
		berechnen.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				wahlFile.setVisible(false);
				berechnen.setVisible(false);
				stop.setVisible(true);
				percent = 0;
				BerechnungPush(evt);
				
			}
		});
		//berechnen.setBounds(10, 18, 33, 33);
		berechnen.setSize(20, 25);
		manager.add(berechnen, gc);

		/************ BUTTON WAHLFILE ************************************/
		ImageIcon wfi = new ImageIcon(getClass().getResource(
				"/bremoswing/bild/folder_smart.png")); //folder_smart.png
		wahlFile.setIcon(wfi); // NOI18N
		wahlFile.setRolloverEnabled(true);
		ImageIcon wfiRO = new ImageIcon(getClass().getResource(
				"/bremoswing/bild/folder_smart_Rollover.png"));
		wahlFile.setRolloverIcon(wfiRO);
		wahlFile.setPressedIcon(wfi);
		wahlFile.setToolTipText("InputFile auswählen");
		//wahlFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		//wahlFile.setBorderPainted(true);
		wahlFile.setContentAreaFilled(false);
		wahlFile.setOpaque(false);
		wahlFile.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				wahlPush(evt);
			}
		});
		//wahlFile.setBounds(70, 18, 33, 33);
		gc.fill = GridBagConstraints.BOTH;
		gc.insets = new Insets(0,0,0,80);
		gc.weightx = 0.0001;
		gc.gridx = 1;
		gc.gridy = 0;
		gc.ipadx = -28;
		manager.add(wahlFile, gc);
      
		/************ BUTTON STOP ************************************/
		stop.setIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/stop 2.png")));
		stop.setToolTipText("Stop Bremo");
		//stop.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		stop.setRolloverIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/stop_alt.png")));
		stop.setContentAreaFilled(false);
		stop.setMaximumSize(null);
		stop.setMinimumSize(null);
		stop.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {

				stopPush(evt);

			}
		});
		stop.setVisible(false);
		//stop.setBounds(130, 18, 33, 33);
		gc.fill = GridBagConstraints.BOTH;
		gc.insets = new Insets(0,0,0,140);
		gc.weightx = 0.0001;
		gc.gridx = 2;
		gc.gridy = 0;
		gc.ipadx = -30;
		manager.add(stop, gc);

		
		/************ CHECKBOX KONSOLE ************************************/
//        konsole.setBounds(200, 18, 100, 40);
//        konsole.setFont(new Font("Tahoma", 3, 13));
//		konsole.setText("Konsole");
//		konsole.setToolTipText("Konsole Panel Aktivieren/deaktivien");
//		
//		konsole.setSelected(true);
//		konsole.addItemListener(new ItemListener() {
//			@Override
//			public void itemStateChanged(ItemEvent evt) {
//
//				if (!berechnen.isVisible()) {
//
//					messsage();
//
//				} else {
//					if (evt.getStateChange() == ItemEvent.DESELECTED) {
//						//konsole2.setVisible(false);
//						jScrollPane1.setVisible(false);
//						setSize(710, 320);
//					} else {
//						//konsole2.setVisible(true);
//						jScrollPane1.setVisible(true);
//						setSize(710, 750);
//					}
//				}
//			}
//		});
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(0,0,0,27);
		gc.weightx = 0.005;
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.gridx = 3;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.CENTER;
		//manager.add(konsole, gc);

		/************ LABEL ************************************/
		//label.setBounds(330, 27, 160, 20);
		label.setFont(new Font("Tahoma", 3, 13)); // NOI18N
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(0,30,0,27);
		gc.weightx = 0.5;
		gc.gridx = 4;
		gc.gridy = 0;
		manager.add(label, gc);

		/************ PROGESSBAR ************************************/
		//progressBar.setBounds(520, 21, 100, 30);
		//progressBar.setIndeterminate(true);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(0,0,0,0);
		gc.weightx = 0.5;
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.gridx = 3;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.CENTER;
		manager.add(progressBar, gc);
		
	
		timerCalcul = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (bremoThread[0].get_myCaseState()) {
					double Sum = 0;
					pb.setVisible(false);
					for (int i = 0; i < bremoThread.length; i++) {

						Sum = Sum
								+ (bremoThread[i].get_myCase()
										.get_aktuelle_Rechenzeit() / bremoThread[i]
										.get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC);

					}
					percent = (int) ((Sum / NrOfFile) * 100);
					progressBar.setValue(percent);
					progressBar.repaint();
				}
			}
		});

//		timerUpdate = new Timer(1, new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				label.setText("Loading File...");
//				pb.setVisible(true);
//			}
//		});
		
		
		/***********************************************************/
		pb.setIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/progressbar4.gif")));
		//pb.setToolTipText("Stop Bremo");
		//stop.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		//pb.setSize(new Dimension (33,33));
		pb.setContentAreaFilled(false);
		pb.setVisible(false);
		
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(0,0,0,0);
		gc.weightx = 0.5;
		gc.gridx = 5;
		gc.gridy = 0;
		gc.ipadx = -15;
		gc.ipady = -15;
		//gc.anchor = GridBagConstraints.FIRST_LINE_START;
		manager.add(pb, gc);
		
//		timerCalcul = new Timer(1000, new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				double Sum = 0;
//				for (int i = 0; i < bremoThread.length ; i++) {
//					Sum = bremoThread[0].get_myCase().get_aktuelle_Rechenzeit()/
//							bremoThread[0].get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC;
//				}
//				percent = (int) (Sum/bremoThread.length)*100;
//				
//			}
//		});
//
//		timerUpdate = new Timer(1, new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				label.setText("Loading File...");
//				pb.setVisible(true);
//			}
//		});

		/************ TEXTFIELD TEXTFILE ************************************/
		textFile.setEditable(false);
		textFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		textFile.setEnabled(false);
		textFile.setMaximumSize(new Dimension(0, 0));
		textFile.setMinimumSize(new Dimension(0, 0));
		textFile.setPreferredSize(new Dimension(0, 0));
		textFile.setBounds(180, 20, 0, 0);
		//manager.add(textFile, JLayeredPane.DEFAULT_LAYER);

		/************ PANEL KONSOLE ************************************/
		konsole2.setBorder(BorderFactory.createTitledBorder(null, "Konsole",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 12))); // NOI18N

		/************ TEXTAREA GROSAREA OUTPUT ************************************/
		grosArea.setColumns(20);
		grosArea.setEditable(false);
		grosArea.setFont(new Font("Tahoma", 3, 16)); // NOI18N
		grosArea.setRows(5);
		grosArea.setText("Programm läuft... \nWählen Sie die Input Datei Und Dann Einfach die Berechnung Ausführen .");
		grosArea.setMinimumSize(new Dimension(76, 22));

		konsole2.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 660;
		c.weighty = 420;

		jScrollPane1.setViewportView(grosArea);
		jScrollPane1.setBounds(13, 20, 660, 420);
		jScrollPane1.setVisible(false);
		konsole2.add(jScrollPane1, c);

		/************ TEXTAREA KLEINAREA OUTPUT ************************************/
		kleinArea.setColumns(20);
		kleinArea.setEditable(false);
		kleinArea.setFont(new Font("Tahoma", 3, 13)); // NOI18N
		kleinArea.setForeground(new Color(255, 0, 0));
		kleinArea.setRows(5);

		jScrollPane2.setViewportView(kleinArea);
		c.gridwidth = 1;
		c.weighty = 180;
		jScrollPane2.setBounds(13, 450, 660, 180);
		konsole2.add(jScrollPane2, c);

		/************ LAYOUT EINSTELUNG ************************************/
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								layout.createParallelGroup(
										GroupLayout.Alignment.TRAILING)
										.addComponent(manager,
												GroupLayout.Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE, 685,
												Short.MAX_VALUE)
										.addComponent(konsole2,
												GroupLayout.Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE, 685,
												Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(manager,
										GroupLayout.PREFERRED_SIZE, 70,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(konsole2,
										GroupLayout.DEFAULT_SIZE, 180,
										Short.MAX_VALUE).addContainerGap()));
		/*****************************************************************************************/
		pack();
		
	}

	/** Warning Massage **********/
	protected void messsage() {
		JOptionPane.showMessageDialog(this,
				"Berechnung läuf gerade... Warten Sie bis Ende ", "Achtung",
				JOptionPane.WARNING_MESSAGE);
	}

	/**** Button Setting To Run Bremo Thread / to Run the calcul ***********/
	private void BerechnungPush(MouseEvent evt) {
		grosArea.setText("");
		kleinArea.setText("");
		if (textFile.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					"Überprüfen Sie Bitte die InputDatei !!!", "Achtung",
					JOptionPane.WARNING_MESSAGE);
			wahlFile.setVisible(true);
			berechnen.setVisible(true);
			stop.setVisible(false);
		} else {
			stop.setVisible(true);
			control = true;

			//group.enumerate(bremoThread);
			//bremoThread=new Bremo[files.length];
			

			try {
			for (int i = 0; i < files.length; i++) {

				bremoThread[i].start();
					
				}
			
			pb.setIcon(new ImageIcon(getClass().getResource(
					"/bremoswing/bild/progressbar3.gif")));
			pb.setVisible(true);
			label.setForeground(new Color(0,150,60));
			label.setText("Operation gestartet ....");
			timerCalcul.start();
			
			} catch (IllegalThreadStateException e) {
				e.printStackTrace();
				ActiveIcon();
				label.setForeground(new Color(255,0,0));
				label.setText("Bitte InputFile Neu Auswälen !!! ");
			}
//				bremoThread[i]=new Bremo(group, files[i]);
//				bremoThread[i].get_myCase().get_aktuelle_Rechenzeit();
//				double dauer=bremoThread[i].get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC;
//				double progress=bremoThread[i].get_myCase().get_aktuelle_Rechenzeit()/
//									bremoThread[i].get_myCase().SYS.RECHNUNGS_ENDE_DVA_SEC;
			}
	    
	}

	/*********** Button Setting To Choose a File *****************************************/
	private void wahlPush(MouseEvent evt) {
		grosArea.setText("");
		kleinArea.setText("");
		berechnen.setEnabled(false);
		label.setText("");
		DebuggingMode = false;
		ActiveConsole();
		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		ExtensionFileFilter txtFilter = new ExtensionFileFilter(null,
				new String[] { "txt" });

		fileChooser.addChoosableFileFilter(txtFilter);
		try {
			label.setForeground(new Color (165,42,42));
			label.setText("Datei Werden geladen ...");
			pb.setIcon(new ImageIcon(getClass().getResource(
					"/bremoswing/bild/loadFile.gif")));
			pb.setVisible(true);
			int status = fileChooser.showOpenDialog(null);

			if (status == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					files = fileChooser.getSelectedFiles();
					textFile.setText(files[0].getPath());
					path = files[0].getParent();
					bremoThread = new Bremo[files.length];
					group = new ThreadGroup("BremoFamily");
					for (int i = 0; i < bremoThread.length; i++) {
						bremoThread[i] = new Bremo(group, files[i]);
					}
					NrOfFile = files.length;
					if (NrOfFile == 1) SetDebbugingMode(true);
					label.setForeground(new Color(0,0,255));
					label.setText("Datei mit Erfolg Importiert !");
					pb.setVisible(false);
					ActiveConsole();
				}
			} else if (status == JFileChooser.CANCEL_OPTION) {
				label.setForeground(new Color (165,42,42));
				label.setText(" Import von Datei Unterbrechen !");
				pb.setVisible(false);
				fileChooser.cancelSelection();
			}
		} catch (Exception e) {
			label.setForeground(new Color (255,0,0));
			label.setText("Fehler aufgetreten !!!");
			e.printStackTrace();
		}
		berechnen.setEnabled(true);
	}

	/********** Button Setting To Stop Running of Bremo Thread **************/
	private void stopPush(MouseEvent evt) {
		if (!control) {
			JOptionPane.showMessageDialog(this,
					"Starten Sie Zu erst die Berechnung.", "Achtung",
					JOptionPane.WARNING_MESSAGE);
		} else {

			group.interrupt();
			control = false;
			pb.setVisible(false);
			ActiveIcon();
		}
	}

	/****** Active Icon *********************************/
	public static void ActiveIcon() {
		wahlFile.setVisible(true);
		berechnen.setVisible(true);
		stop.setVisible(false);
		label.setForeground(new Color(255, 0, 0));
		label.setText("Operation beendet.");
		pb.setVisible(false);
	}
	
	/****** Change the Mode of The Console When DebbugMode is  Active ***********/
	public  void ActiveConsole () {

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
	
	public static void SetDebbugingMode (Boolean mode) {
		if (NrOfFile == 1)  { DebuggingMode = mode ;}
	}

	/****** SEE IF THE RUNNING OPERATION ARE FINISHED *******************************/
	public static void StateBremoThread() {

		if (group.activeCount() <= 1) {
			timerCalcul.stop();
			pb.setVisible(false);
			JFrame popup = new JFrame();
			JOptionPane.showMessageDialog(popup,
					"Die Berechnung ist Fertig !!!", "Zustand Berechnung",
					JOptionPane.INFORMATION_MESSAGE);
			ActiveIcon();
		}
	}

	/************************ place Frame to the center ************************/
	private void placeFrame() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle window = getBounds();
		this.setLocation((screen.width - window.width) / 2,
				(screen.height - window.height) / 2);
	}

	/************************* close the Frame ********************************/
	public void closeFrame() {
		final JOptionPane optionPane = new JOptionPane(
				"Wollen Sie Wirklich das Program beendet ?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		final JDialog dialog = optionPane.createDialog(this, "Exit");

		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
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

		int value = ((Integer) optionPane.getValue()).intValue();
		if (value == JOptionPane.YES_OPTION) {
			System.exit(0);
		} else if (value == JOptionPane.NO_OPTION) {

		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase
		 * /tutorial/uiswing/lookandfeel/plaf.html
		 */
		UIManager.put("nimbusOrange", new Color(0,180,0));
		
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

			public void run() {
				new SwingBremo().setVisible(true);
				
			}
		});
	}

}
