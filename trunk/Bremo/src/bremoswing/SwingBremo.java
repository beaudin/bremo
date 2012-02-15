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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
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

	
	// Variables declaration
		private boolean control = false;
		private File file;
		private File [] files;
		private String path = ".";
		private Bremo bremo;
		public static Bremo [] bremoThread;
		public static JButton berechnen;
		private JTextArea grosArea;
		private JScrollPane jScrollPane1;
		private JScrollPane jScrollPane2;
		private JTextArea kleinArea;
		private JPanel konsole2;
		private JLayeredPane manager;
		public static  JButton stop;
		private JTextField textFile;
		public static  JButton wahlFile;
		private double t1;
		private double t2;
		PrintStream outStream = new PrintStream(System.out) {

			@Override
			public void  println(String s) {

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
	// End of variables declaration
		
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

		manager = new JLayeredPane();
		berechnen = new JButton();
		wahlFile = new JButton();
		stop = new JButton();
		textFile = new JTextField();
        konsole2 = new JPanel();
		grosArea = new JTextArea();
		jScrollPane1 = new JScrollPane();
		kleinArea = new JTextArea();
		jScrollPane2 = new JScrollPane();
		
		

		setTitle("Bremo 1.0");
		setBackground(new Color(255, 255, 255));
		setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo1.png")).getImage());
		// setLocationByPlatform(true);
		setName("BremoGraphik"); // NOI18N
		setResizable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		 this.addWindowListener(new WindowAdapter() { 
			 public void windowClosing(WindowEvent e) { 
				 closeFrame();
				 }
			 });
			 
		 
		manager.setBorder(BorderFactory.createTitledBorder(null,
				"Manager",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("Tahoma", 1, 12))); // NOI18N

		berechnen.setIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/play.png"))); // NOI18N
		berechnen.setToolTipText("berechnung ausführen");
		berechnen.setBorder(BorderFactory.createEmptyBorder(0, 0,
				0, 0));
		berechnen.setBorderPainted(false);
		berechnen.setContentAreaFilled(false);
		berechnen
				.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		berechnen.setVerifyInputWhenFocusTarget(false);
		berechnen.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				wahlFile.setVisible(false);
				berechnen.setVisible(false);
				stop.setVisible(true);
				BerechnungPush(evt);

			}
		});
		berechnen.setBounds(10, 18, 33, 33);
		manager.add(berechnen, JLayeredPane.DEFAULT_LAYER);

		wahlFile.setIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/folder_smart.png"))); // NOI18N
		wahlFile.setToolTipText("InputFile auswählen");
		wahlFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
				0));
		wahlFile.setBorderPainted(false);
		wahlFile.setContentAreaFilled(false);
		wahlFile.setOpaque(false);
		wahlFile.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				wahlPush(evt);
			}
		});
		wahlFile.setBounds(70, 18, 33, 33);
		manager.add(wahlFile, JLayeredPane.DEFAULT_LAYER);

		stop.setIcon(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/stop 2.png"))); // NOI18N
		stop.setToolTipText("Stop Bremo");
		stop.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		stop.setContentAreaFilled(false);
		stop.setMaximumSize(null);
		stop.setMinimumSize(null);
		stop.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				wahlFile.setVisible(true);
				berechnen.setVisible(true);
				stop.setVisible(false);
				stopPush(evt);
			}
		});
		stop.setVisible(false);
		stop.setBounds(130, 18, 33, 33);
		manager.add(stop, JLayeredPane.DEFAULT_LAYER);

		textFile.setEditable(false);
		textFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
				0));
		textFile.setEnabled(false);
		textFile.setMaximumSize(new Dimension(0, 0));
		textFile.setMinimumSize(new Dimension(0, 0));
		textFile.setPreferredSize(new Dimension(0, 0));
		textFile.setBounds(180, 20, 0, 0);
		manager.add(textFile, JLayeredPane.DEFAULT_LAYER);

		konsole2.setBorder(BorderFactory.createTitledBorder(null,
				"Konsole",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("Tahoma", 1, 12))); // NOI18N

		grosArea.setColumns(20);
		grosArea.setEditable(false);
		grosArea.setFont(new Font("Tahoma", 3, 16)); // NOI18N
		grosArea.setRows(5);
		grosArea.setText("Programm läuft... \nWählen Sie die Input Datei Und Dann Einfach die Berechnung Ausführen .");
		grosArea.setMinimumSize(new Dimension(76, 22));
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
		kleinArea.setFont(new Font("Tahoma", 3, 13)); // NOI18N
		kleinArea.setForeground(new Color(255, 0, 0));
		kleinArea.setRows(5);
		//kleinArea.setPreferredSize(new Dimension(660, 140));
		jScrollPane2.setViewportView(kleinArea);
        c.gridwidth = 1;
		c.weighty = 180;
		jScrollPane2.setBounds(13, 450, 660, 180);
		konsole2.add(jScrollPane2, c);

		GroupLayout layout = new GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.TRAILING)
												.addComponent(
														manager,
														GroupLayout.Alignment.LEADING,
														GroupLayout.DEFAULT_SIZE,
														685, Short.MAX_VALUE)
												.addComponent(
														konsole2,
														GroupLayout.Alignment.LEADING,
														GroupLayout.DEFAULT_SIZE,
														685, Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(manager,
										GroupLayout.PREFERRED_SIZE,
										60,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(konsole2,
										GroupLayout.DEFAULT_SIZE,
										644, Short.MAX_VALUE).addContainerGap()));
		pack();
	}
	

	//Button Setting To Run Bremo Thread / to Run the calcul
	private void BerechnungPush(MouseEvent evt) {
		// TODO add your handling code here:
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
			t1 = System.currentTimeMillis();
			//bremo = new Bremo(file);
			//bremo.start();
			bremoThread = new Bremo [files.length];
			for (int i = 0 ; i < files.length ; i++) {
				bremoThread[i] = new Bremo(files[i]);
				bremoThread[i].start();
				}
			t2 = System.currentTimeMillis();
			// play();

		}
	}

	//
	public static  void ActiveIcon() {
		wahlFile.setVisible(true);
		berechnen.setVisible(true);
		stop.setVisible(false);
	}
    
	
	public static void StateBremoThread () {
		
		if (bremoThread.length == 1) {
			ActiveIcon();
		} else {
			
			int i = 0 ;
			
			while (!bremoThread[i].getState().equals("TERMINATED") && i < bremoThread.length) {
				i++;
			}
			
			if (i == bremoThread.length) {
				ActiveIcon();
			}
		}
	}
	
	//Button Setting To Stop Running of Bremo Thread
	@SuppressWarnings("static-access")
	private void stopPush(MouseEvent evt) {
		// TODO add your handling code here:
		if (!control) {
			JOptionPane.showMessageDialog(this,
					"Starten Sie Zu erst die Berechnung.", "Achtung",
					JOptionPane.WARNING_MESSAGE);
		} else {
			/*if (bremo.isAlive()) {
				bremo.interrupt();
				control = false;
			    kleinArea.append(new Timer(t2-t1).toString());
			}*/
			for (int i = 0 ; i < bremoThread.length; i++) {
				if (bremoThread[i].isAlive()) {
					bremoThread[i].interrupt();
				}
			}
			control = false;
		}
	}
    
	//Button Setting To Choose a File
	private void wahlPush(MouseEvent evt) {
		// TODO add your handling code here:
		grosArea.setText("");
		kleinArea.setText("");
		berechnen.setEnabled(false);
		JFileChooser fileChooser = new JFileChooser(
				path);
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
				//file = fileChooser.getSelectedFile();
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
