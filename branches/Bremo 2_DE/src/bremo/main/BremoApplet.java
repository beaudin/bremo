/**
 * 
 */
package bremo.main;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.UIManager;



import bremoswing.SwingBremo;

/**
 * @author Ngueneko
 * 
 */
public class BremoApplet extends JApplet {

	private static final long serialVersionUID = 1L;

	public void start() {
		
		UIManager.put("nimbusOrange", new Color(28, 138, 224));
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
}

	


