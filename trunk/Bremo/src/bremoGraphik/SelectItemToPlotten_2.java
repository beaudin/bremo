package bremoGraphik;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import bremoswing.SwingBremo;

/**
 * 
 * @author Ngueneko Steve
 */
public class SelectItemToPlotten_2 extends JFrame {
	
	// Variables declaration - do not modify
		private JComboBox fileComboBox;
		private JLabel fileToSelect;
		private JLabel kurveVergleich;
		private JLabel option;
		private JSeparator separator;
		private JCheckBox vergleich;
		private JLabel xAchse;
		private JComboBox xAchseComboBox;
		private JLabel y1Achse;
		private JComboBox y1AchseComboBox;
		private JLabel y2Achse;
		private JComboBox y2AchseComboBox;
		private JButton grafikButton;
		String[] header;
		static String[] selectedItem;
		// End of variables declaration

	/**
	 * Creates new form SelectItemToPlotten
	 */
	public SelectItemToPlotten_2() {
		initComponents();
		SwingBremo.placeFrame(this);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {

		fileToSelect = new JLabel();
		fileComboBox = new JComboBox();
		xAchse = new JLabel();
		xAchseComboBox = new JComboBox();
		y1Achse = new JLabel();
		y1AchseComboBox = new JComboBox();
		separator = new JSeparator();
		option = new JLabel();
		kurveVergleich = new JLabel();
		vergleich = new JCheckBox();
		y2Achse = new JLabel();
		y2AchseComboBox = new JComboBox();
		grafikButton = new JButton();
		selectedItem = new String[4];
		
		setTitle("BremoGrafik");
		setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo2.png")).getImage());
		setName("BremoGraphik");
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		
		setResizable(true);

		fileToSelect.setText("Ergebniss Datei Auswählen :");

		String[] tmp = SwingBremo.bremoThreadFertig;

		

		addFileItemToComboBox(fileComboBox, tmp);
		fileComboBox = new JComboBox(tmp);

		fileComboBox.addActionListener(new ActionListener() {

			@SuppressWarnings("resource")
			@Override
			public void actionPerformed(ActionEvent ac) {

				try {

					String fileName = fileComboBox.getSelectedItem().toString();
					selectedItem[0] = fileName;
					BufferedReader in = new BufferedReader(new FileReader(
							"src/InputFiles/" + fileName));
					String zeile = null;
					if ((zeile = in.readLine()) != null) {
						header = zeile.split("\t");
						addItemToComboBox(xAchseComboBox, header);
					}
					in.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		xAchse.setText("Spalte für die X-Achse :");

		xAchseComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JComboBox cb = (JComboBox) arg0.getSource();
				try {
					selectedItem[1] = cb.getSelectedItem().toString();
				} catch (NullPointerException npe) {

				}
				addItemToComboBox(y1AchseComboBox, header);
				y1AchseComboBox.removeItem(selectedItem[1]);
				
			}
		});

		y1Achse.setText("Spalte für die Y-Achse :");
		y1AchseComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JComboBox cb = (JComboBox) e.getSource();
				try {
					selectedItem[2] = cb.getSelectedItem().toString();
				} catch (NullPointerException npe) {

				}
				addItemToComboBox(y2AchseComboBox, header);
				y2AchseComboBox.removeItem(selectedItem[1]);
				y2AchseComboBox.removeItem(selectedItem[2]);
				y2AchseComboBox.updateUI();
			}
		});

		option.setFont(new java.awt.Font("Dialog", 1, 12));
		option.setText("<html><u>Option:</u></<html> ");

		kurveVergleich.setText("2 Kurve vergleichen :");

		vergleich.setText("ja");
		vergleich.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (vergleich.isSelected()) {
					setSize(getSize().width, 341);
					// System.err.println(getSize().getHeight());
					y2Achse.setVisible(true);
					y2AchseComboBox.setVisible(true);
				} else {
					setSize(getSize().width, 316);
					// System.err.println(getSize().getHeight());
					y2Achse.setVisible(false);
					y2AchseComboBox.setVisible(false);
					y2AchseComboBox.setSelectedItem(0);
					y2AchseComboBox.updateUI();
				}
			}
		});

		y2Achse.setText("2.Spalte für die Y-Achse :");
		y2Achse.setVisible(false);

		y2AchseComboBox.setVisible(false);
		y2AchseComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JComboBox cb = (JComboBox) arg0.getSource();
				try {
					selectedItem[3] = cb.getSelectedItem().toString();
				} catch (NullPointerException npe) {

				}
			}
		});

		grafikButton.setText("Graphik");
		grafikButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (!vergleich.isSelected()) {
					selectedItem[3] = "";

					drawGraphic();
				} else {

					if (!selectedItem[2]
							.substring(selectedItem[2].indexOf("[")).equals(
									selectedItem[3].substring(selectedItem[3]
											.indexOf("[")))) {
						JFrame jf = new JFrame();
						JOptionPane
								.showMessageDialog(
										jf,
										"Bitte wählen Sie Spalten mit gleiche Einheiten !",
										"Falsche Einheit",
										JOptionPane.ERROR_MESSAGE);
					} else {
						drawGraphic();
					}
				}
			}
		});

		/************ FRAME LAYOUT EINSTELUNG *****************************************************************************************************************/
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING,
												false)
												.addComponent(
														option,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														80,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														kurveVergleich,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(separator)
												.addGroup(
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(
																						layout.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																								.addComponent(
																										fileToSelect,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addComponent(
																										xAchse,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addComponent(
																										y1Achse,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE))
																				.addComponent(
																						y2Achse,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addComponent(
																						grafikButton)
																				.addGroup(
																						layout.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																								.addComponent(
																										vergleich,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(
																										fileComboBox,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										144,
																										Short.MAX_VALUE)
																								.addComponent(
																										xAchseComboBox,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addComponent(
																										y1AchseComboBox,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addComponent(
																										y2AchseComboBox,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)))))
								.addContainerGap(21, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(24, 24, 24)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														fileComboBox,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														fileToSelect,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(19, 19, 19)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														xAchse,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														xAchseComboBox,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(23, 23, 23)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														y1Achse,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														y1AchseComboBox,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(separator,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										10,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.TRAILING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		option,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		kurveVergleich,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(
														vergleich,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(19, 19, 19)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(
														y2Achse,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														y2AchseComboBox,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(22, 22, 22)
								.addComponent(grafikButton)
								.addContainerGap(
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));
		/********************************************************************************************************************************************************************/


		pack();
	}

	/**
	 * füge elemente von Array to JComboBox als Item hinzu. Nur für Spalte
	 * ComboBox
	 */
	public void addItemToComboBox(JComboBox cb, String[] item) {

		cb.removeAllItems();
		// cb.addItem("");

		for (int i = 0; i < item.length; i++) {

			cb.addItem(item[i]);

		}

	}

	/**
	 * füge elemente von Array to JComboBox als Item hinzu. Nur für File
	 * ComboBox
	 */
	public void addFileItemToComboBox(JComboBox cb, String[] item) {

		// cb.addItem("");
		for (int i = 0; i < item.length; i++) {

			cb.addItem(item[i]);
		}

	}

	public void drawGraphic() {

		try {
			PlottSelectedItem.BremoGrafik();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/*
		 * Set the Nimbus look and feel
		 */
		// desc=" Look and feel setting code (optional) ">
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
			java.util.logging.Logger.getLogger(
					SelectItemToPlotten_2.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(
					SelectItemToPlotten_2.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(
					SelectItemToPlotten_2.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(
					SelectItemToPlotten_2.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}

		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
				new SelectItemToPlotten_2().setVisible(true);
			}
		});
	}
}
