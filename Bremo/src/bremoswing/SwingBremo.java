package bremoswing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;

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
	/************************** Variables declaration ****************************************/
	private boolean control = false;
	private File[] files;
	private String path;
	public static ThreadGroup group;
	public static Bremo[] bremoThread;
	public static JButton berechnen;
	public JCheckBox konsole;
	private JTextArea grosArea;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JTextArea kleinArea;
	private JPanel konsole2;
	private JLayeredPane manager;
	public static JButton stop;
	private JTextField textFile;
	public static JButton wahlFile;
	public static JProgressBar pb;
	public static JLabel label;
	public static Timer timer;
	public static int percent;
	PrintStream outStream = new PrintStream(System.out) {

		@Override
		public void println(String s) {
			if (!konsole.isSelected()) {
				outStream.flush();
			} else {
				grosArea.append(s + "\n");
				grosArea.setCaretPosition(grosArea.getDocument().getLength());
			}
		}

		@Override
		public void print(String s) {
			if (!konsole.isSelected()) {
				outStream.flush();
			} else {
				grosArea.append(s);
				grosArea.setCaretPosition(grosArea.getDocument().getLength());
			}

		}
	};
	PrintStream errStream = new PrintStream(System.err) {

		@Override
		public void println(String s) {
			if (!konsole.isSelected()) {
				errStream.flush();
			} else {
				kleinArea.append(s + "\n");
				kleinArea.setCaretPosition(kleinArea.getDocument().getLength());
			}
		}

		@Override
		public void print(String s) {
			if (!konsole.isSelected()) {
				errStream.flush();
			} else {
				kleinArea.append(s);
				kleinArea.setCaretPosition(kleinArea.getDocument().getLength());
			}
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

		manager = new JLayeredPane();

		berechnen = new JButton();
		wahlFile = new JButton();
		stop = new JButton();

		textFile = new JTextField();

		konsole = new JCheckBox();
		konsole2 = new JPanel();

		kleinArea = new JTextArea();
		grosArea = new JTextArea();

		jScrollPane1 = new JScrollPane();
		jScrollPane2 = new JScrollPane();

		pb = new JProgressBar(0, 100);
		label = new JLabel();
		percent = 0;
		path = ".";

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
				label.setForeground(new Color(0, 180, 0));
				label.setText("Operation gestartet ....");
			}
		});
		berechnen.setBounds(10, 18, 33, 33);
		manager.add(berechnen, JLayeredPane.DEFAULT_LAYER);

		/************ BUTTON WAHLFILE ************************************/
		ImageIcon wfi = new ImageIcon(getClass().getResource(
				"/bremoswing/bild/folder_smart.png"));
		wahlFile.setIcon(wfi); // NOI18N
		wahlFile.setRolloverEnabled(true);
		ImageIcon wfiRO = new ImageIcon(getClass().getResource(
				"/bremoswing/bild/folder_smart_Rollover.png"));
		wahlFile.setRolloverIcon(wfiRO);
		wahlFile.setPressedIcon(wfi);
		wahlFile.setToolTipText("InputFile auswählen");
		wahlFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		wahlFile.setBorderPainted(true);
		wahlFile.setContentAreaFilled(false);
		wahlFile.setOpaque(false);
		wahlFile.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				wahlPush(evt);
			}
		});
		wahlFile.setBounds(70, 18, 33, 33);
		manager.add(wahlFile, JLayeredPane.DEFAULT_LAYER);

		/************ BUTTON STOP ************************************/
		stop.setIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/stop 2.png")));
		stop.setToolTipText("Stop Bremo");
		stop.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		stop.setContentAreaFilled(false);
		stop.setMaximumSize(null);
		stop.setMinimumSize(null);
		stop.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {

				stopPush(evt);

			}
		});
		stop.setVisible(false);
		stop.setBounds(130, 18, 33, 33);
		manager.add(stop, JLayeredPane.DEFAULT_LAYER);

		
		/************ CHECKBOX KONSOLE ************************************/
        konsole.setBounds(200, 18, 100, 40);
		konsole.setText("Konsole");
		konsole.setToolTipText("Konsole Panel Aktivieren/deaktivien");
		konsole.setSelected(true);
		konsole.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {

				if (!berechnen.isVisible()) {

					messsage();

				} else {
					if (evt.getStateChange() == ItemEvent.DESELECTED) {
						konsole2.setVisible(false);
						setSize(700, 115);
					} else {
						konsole2.setVisible(true);
						setSize(700, 750);
					}
				}
			}
		});
		manager.add(konsole, JLayeredPane.DEFAULT_LAYER);

		/************ LABEL ************************************/
		label.setBounds(330, 27, 160, 20);
		label.setFont(new Font("Tahoma", 3, 13)); // NOI18N
		manager.add(label, JLayeredPane.DEFAULT_LAYER);

		/************ PROGESSBAR ************************************/
		pb.setBounds(520, 21, 150, 30);
		pb.setValue(0);
		pb.setStringPainted(true);
		manager.add(pb, JLayeredPane.DEFAULT_LAYER);

		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (pb.getValue() >= 99) {
					percent = (int) (99 * java.lang.Math.random());
				}
				percent = percent + 4;
				pb.setValue(percent);
			}
		});

		/************ TEXTFIELD TEXTFILE ************************************/
		textFile.setEditable(false);
		textFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		textFile.setEnabled(false);
		textFile.setMaximumSize(new Dimension(0, 0));
		textFile.setMinimumSize(new Dimension(0, 0));
		textFile.setPreferredSize(new Dimension(0, 0));
		textFile.setBounds(180, 20, 0, 0);
		manager.add(textFile, JLayeredPane.DEFAULT_LAYER);

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
		jScrollPane1.setPreferredSize(new Dimension(800, 600));
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
										GroupLayout.PREFERRED_SIZE, 60,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(konsole2,
										GroupLayout.DEFAULT_SIZE, 644,
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

			group = new ThreadGroup("BremoFamily");
			for (int i = 0; i < files.length; i++) {
				System.out
						.println("Nicht einmal ich werde auf der Konsole ausgegeben. Warum nicht?");
				new Bremo(group, files[i]).start();
			}
			timer.start();
		}
	}

	/*********** Button Setting To Choose a File *****************************************/
	private void wahlPush(MouseEvent evt) {
		grosArea.setText("");
		kleinArea.setText("");
		berechnen.setEnabled(false);
		label.setText("");
		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		ExtensionFileFilter txtFilter = new ExtensionFileFilter(null,
				new String[] { "txt" });

		fileChooser.addChoosableFileFilter(txtFilter);
		try {
			int status = fileChooser.showOpenDialog(null);

			if (status == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null)
					;
				files = fileChooser.getSelectedFiles();
				textFile.setText(files[0].getPath());
				path = files[0].getParent();
			} else if (status == JFileChooser.CANCEL_OPTION) {
				fileChooser.cancelSelection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		berechnen.setEnabled(true);
		pb.setValue(0);
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
			label.setForeground(new Color(255, 0, 0));
			label.setText("Operation beendet.");
			ActiveIcon();
			timer.stop();
			pb.setValue(0);
		}
	}

	/****** Active Icon *********************************/
	public static void ActiveIcon() {
		wahlFile.setVisible(true);
		berechnen.setVisible(true);
		stop.setVisible(false);
	}

	/****** SEE IF THE RUNNING OPERATION ARE FINISHED *******************************/
	public static void StateBremoThread() {

		if (group.activeCount() <= 1) {
			timer.stop();
			pb.setValue(100);
			JFrame popup = new JFrame();
			JOptionPane.showMessageDialog(popup,
					"Die Berechnung ist Fertig !!!", "Zustand Berechnung",
					JOptionPane.INFORMATION_MESSAGE);
			label.setForeground(new Color(255, 0, 0));
			label.setText("Operation beendet.");
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
