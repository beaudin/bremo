package bremoswing.util;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

/**
 * @author Steve Ngueneko
 */
public class ToolTipFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	// Variables declaration
	public JLabel textInfo;
	private JLabel title;
	public String PATH_BREMO_APP_ICON_18 = "/bremoswing/bild/bremo1_18.png";

	// End of variables declaration

	/**
	 * Creates new form ToolTipFrame
	 */
	public ToolTipFrame(String Titel, String message) {
		initComponents(Titel, message);
	}

	private void initComponents(String Titel, String message) {

		title = new JLabel();
		textInfo = new JLabel();

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setUndecorated(true);

		title.setFont(new Font("Comic Sans MS", 0, 18));
		title.setForeground(new java.awt.Color(255, 0, 0));
		title.setIcon(new ImageIcon(ToolTipFrame.class
				.getResource(PATH_BREMO_APP_ICON_18)));
		title.setText(Titel);
		title.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, true));

		textInfo.setFont(new Font("Comic Sans MS", 0, 14));
		textInfo.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0,
				0, 0)));
		textInfo.setText(message);
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(title, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(textInfo, GroupLayout.DEFAULT_SIZE, 200,
						Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addComponent(title)
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(textInfo, GroupLayout.PREFERRED_SIZE, 80,
								GroupLayout.PREFERRED_SIZE)));

		pack();
	}
}
