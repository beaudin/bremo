package bremoswing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;

import bremo.main.Bremo;
import bremoswing.*;

/*
 * SwingBremo.java
 *
 */

/**
 * 
 * @author Ngueneko Steve
 */ 
public class SwingBremo extends javax.swing.JFrame {

	/** Creates new form SwingBremo */
	public SwingBremo() {
		initComponents();
		placeFrame();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	@SuppressWarnings("unchecked")
	private void initComponents() {

		System.setOut(outStream);
		System.setErr(errStream);

		manager = new javax.swing.JLayeredPane();
		berechnen = new javax.swing.JButton();
		wahlFile = new javax.swing.JButton();
		stop = new javax.swing.JButton();
		textFile = new javax.swing.JTextField();
        konsole2 = new JPanel();
		grosArea = new javax.swing.JTextArea();
		jScrollPane1 = new javax.swing.JScrollPane();
		kleinArea = new javax.swing.JTextArea();
		jScrollPane2 = new javax.swing.JScrollPane();
		
		

		setTitle("Bremo");
		setBackground(new java.awt.Color(255, 255, 255));
		setIconImage(new javax.swing.ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo1.png")).getImage());
		// setLocationByPlatform(true);
		setName("BremoGraphik"); // NOI18N
		setResizable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		 this.addWindowListener(new java.awt.event.WindowAdapter() { 
			 public void windowClosing(java.awt.event.WindowEvent e) { 
				 closeFrame();
				 }
			 });
			 
		 
		manager.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Manager",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		berechnen.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/bremoswing/bild/play.png"))); // NOI18N
		berechnen.setToolTipText("berechnung ausführen");
		berechnen.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0,
				0, 0));
		berechnen.setBorderPainted(false);
		berechnen.setContentAreaFilled(false);
		berechnen
				.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		berechnen.setVerifyInputWhenFocusTarget(false);
		berechnen.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				wahlFile.setVisible(false);
				berechnen.setVisible(false);
				stop.setVisible(true);
				BerechnungPush(evt);

			}
		});
		berechnen.setBounds(10, 18, 33, 33);
		manager.add(berechnen, javax.swing.JLayeredPane.DEFAULT_LAYER);

		wahlFile.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/bremoswing/bild/folder_smart.png"))); // NOI18N
		wahlFile.setToolTipText("InputFile auswählen");
		wahlFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0,
				0));
		wahlFile.setBorderPainted(false);
		wahlFile.setContentAreaFilled(false);
		wahlFile.setOpaque(false);
		wahlFile.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				wahlPush(evt);
			}
		});
		wahlFile.setBounds(70, 18, 33, 33);
		manager.add(wahlFile, javax.swing.JLayeredPane.DEFAULT_LAYER);

		stop.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/bremoswing/bild/stop 2.png"))); // NOI18N
		stop.setToolTipText("Stop Bremo");
		stop.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		stop.setContentAreaFilled(false);
		stop.setMaximumSize(null);
		stop.setMinimumSize(null);
		stop.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				wahlFile.setVisible(true);
				berechnen.setVisible(true);
				stop.setVisible(false);
				stopPush(evt);
			}
		});
		stop.setVisible(false);
		stop.setBounds(130, 18, 33, 33);
		manager.add(stop, javax.swing.JLayeredPane.DEFAULT_LAYER);

		textFile.setEditable(false);
		textFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0,
				0));
		textFile.setEnabled(false);
		textFile.setMaximumSize(new java.awt.Dimension(0, 0));
		textFile.setMinimumSize(new java.awt.Dimension(0, 0));
		textFile.setPreferredSize(new java.awt.Dimension(0, 0));
		textFile.setBounds(180, 20, 0, 0);
		manager.add(textFile, javax.swing.JLayeredPane.DEFAULT_LAYER);

		konsole2.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Konsole",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		grosArea.setColumns(20);
		grosArea.setEditable(false);
		grosArea.setFont(new java.awt.Font("Tahoma", 3, 16)); // NOI18N
		grosArea.setRows(5);
		grosArea.setText("Programm läuft... \nWählen Sie die Input Datei Und Dann Einfach die Berechnung Ausführen .");
		grosArea.setMinimumSize(new java.awt.Dimension(76, 22));
		//grosArea.setPreferredSize(new  Dimension(670, 430));
		
		konsole2.setLayout(new GridBagLayout());
		GridBagConstraints  c  = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 660;
		c.weighty = 420;
	
		jScrollPane1.setViewportView(grosArea);
		jScrollPane1.setBounds(13, 20, 660, 420);
		jScrollPane1.setPreferredSize(new Dimension(800, 600));
		konsole2.add(jScrollPane1, c);

		kleinArea.setColumns(20);
		kleinArea.setEditable(false);
		kleinArea.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
		kleinArea.setForeground(new java.awt.Color(255, 0, 0));
		kleinArea.setRows(5);
		//kleinArea.setPreferredSize(new Dimension(660, 140));
		jScrollPane2.setViewportView(kleinArea);
        c.gridwidth = 1;
		c.weighty = 180;
		jScrollPane2.setBounds(13, 450, 660, 180);
		konsole2.add(jScrollPane2, c);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(
														manager,
														javax.swing.GroupLayout.Alignment.LEADING,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														685, Short.MAX_VALUE)
												.addComponent(
														konsole2,
														javax.swing.GroupLayout.Alignment.LEADING,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														685, Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(manager,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										60,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(konsole2,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										644, Short.MAX_VALUE).addContainerGap()));
		pack();
	}
	

	//Button Setting To Run Bremo Thread / to Run the calcul
	private void BerechnungPush(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
		grosArea.setText("");
		kleinArea.setText("");
		if (textFile.getText().equals("")) {
			javax.swing.JOptionPane.showMessageDialog(this,
					"Überprüfen Sie Bitte die InputDatei !!!", "Achtung",
					javax.swing.JOptionPane.WARNING_MESSAGE);
			wahlFile.setVisible(true);
			berechnen.setVisible(true);
			stop.setVisible(false);
		} else {
			stop.setVisible(true);
			control = true;
			t1 = System.currentTimeMillis();
			bremo = new Bremo(file);
			bremo.start();
			t2 = System.currentTimeMillis();
			// play();

		}
	}

	public void play() {
		do {

		} while (!bremo.isInterrupted());
		wahlFile.setVisible(true);
		berechnen.setVisible(true);
		stop.setVisible(false);
	}
    
	//Button Setting To Stop Running of Bremo Thread
	@SuppressWarnings("static-access")
	private void stopPush(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
		if (!control) {
			javax.swing.JOptionPane.showMessageDialog(this,
					"Starten Sie Zu erst die Berechnung.", "Achtung",
					javax.swing.JOptionPane.WARNING_MESSAGE);
		} else {
			if (bremo.isAlive()) {
				bremo.interrupt();
				control = false;
			    kleinArea.append(new Timer(t2-t1).toString());
			}

		}
	}
    
	//Button Setting To Choose a File
	private void wahlPush(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
		grosArea.setText("");
		kleinArea.setText("");
		berechnen.setEnabled(false);
		javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(
				path);
		fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
		// fileChooser.showOpenDialog(null);
		// File[] files = chooser.getSelectedFiles();

		ExtensionFileFilter txtFilter = new ExtensionFileFilter(null,
				new String[] { "txt" });

		fileChooser.addChoosableFileFilter(txtFilter);
		try {
			int status = fileChooser.showOpenDialog(null);

			if (status == javax.swing.JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null)
					;
				file = fileChooser.getSelectedFile();
				textFile.setText(file.getPath());
				path = file.getParent();
			} else if (status == javax.swing.JFileChooser.CANCEL_OPTION) {
				fileChooser.cancelSelection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		berechnen.setEnabled(true);
	}
    
	// place Frame to the center
	private void placeFrame() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle window = getBounds();
		this.setLocation((screen.width - window.width) / 2,
				(screen.height - window.height) / 2);
	}
    
	// close the Frame 
	public void closeFrame() {
		final JOptionPane optionPane = new JOptionPane(
				"Wollen Sie Wirklich das Program beendet ?", JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION);

		final JDialog dialog = optionPane.createDialog(this, "Exit");

		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();

				if (dialog.isVisible() && (e.getSource() == optionPane)
						&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
					// If you were going to check something
					// before closing the window, you'd do
					// it here.
					dialog.setVisible(false);
				}
			}
		});
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// dialog.pack();
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
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
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
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(SwingBremo.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
				new SwingBremo().setVisible(true);
			}
		});
	}

	// Variables declaration
	private boolean control = false;
	private java.io.File file;
	private String path = ".";
	private Bremo bremo;
	private javax.swing.JButton berechnen;
	private javax.swing.JTextArea grosArea;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JTextArea kleinArea;
	private JPanel konsole2;
	private javax.swing.JLayeredPane manager;
	private javax.swing.JButton stop;
	private javax.swing.JTextField textFile;
	private javax.swing.JButton wahlFile;
	private double t1;
	private double t2;
	// End of variables declaration

	PrintStream outStream = new PrintStream(System.out) {

		@Override
		public void println(String s) {

			grosArea.append(s + "\n");
			grosArea.setCaretPosition(grosArea.getDocument().getLength());
		}

		@Override
		public void print(String s) {

			grosArea.append(s);
			grosArea.setCaretPosition(grosArea.getDocument().getLength());

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
}
