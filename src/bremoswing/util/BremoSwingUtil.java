package bremoswing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;

public class BremoSwingUtil {

	public BremoSwingUtil() {

	}

	/**
	 * place Component to the center
	 */
	public static void placeFrame(Component frame) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle window = frame.getBounds();
		frame.setLocation((screen.width - window.width) / 2,
				(screen.height - window.height) / 2);
	}

	/**
	 * Show PopUp to prevent the User.
	 * 
	 * @param icon
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

		tooltip.addMouseListener(new MouseAdapter() {
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
		// Hide popup in 4 seconds
		Timer timer = new Timer(4000, hider);
		timer.start();

	}

	/**
	 * Show PopUp to prevent the User.
	 */
	public static void PopUp(String Titel, String message, ImageIcon icon) {

		// JLabel label = new JLabel();
		// label.setOpaque(true);
		// label.setBorder(BorderFactory.createTitledBorder(null, Titel, 0, 0,
		// new java.awt.Font("comic sans ms", 2, 18), Color.red));
		// label.setBackground(new Color (102,178,255));

		String head = "<html>" + message + "</html>";

		ToolTipFrame tooltip = new ToolTipFrame(Titel, head, icon);
		// panel.add(label,BorderLayout.CENTER);

		PopupFactory factory = PopupFactory.getSharedInstance();
		Random random = new Random();
		int x = random.nextInt(500);
		int y = random.nextInt(100);
		Container container = tooltip.getContentPane();

		final Popup popup = factory.getPopup(null, container, x, y);
		popup.show();

		container.addMouseListener(new MouseAdapter() {
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
		// Hide popup in 4 seconds
		Timer timer = new Timer(2000, hider);
		timer.start();

	}

}
