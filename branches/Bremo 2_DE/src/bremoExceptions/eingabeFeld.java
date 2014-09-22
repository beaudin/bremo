package bremoExceptions;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.*;

import bremo.parameter.CasePara;


public class eingabeFeld extends JFrame {
	
	JTextField tfWert;
	JButton buttonOK;
	
	public eingabeFeld(final CasePara cp, final String[] parameter, String vorgabe){
		setTitle("Fehlender Parameter im Inputfile");
		setSize(350, 100);
		addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {
		        cp.notify();
		      }
		});
        JPanel panel = new JPanel(){
        	public void paintComponent(Graphics g) {
	  	      
	   	   	 URL url = getClass().getResource("/bremoswing/bild/Abstract_Frozen_Blue.jpg");
	   			 ImageIcon icon = new ImageIcon(url);
	   			 Image img = icon.getImage();
	   			 BufferedImage buffImage = 
	   			    	      new BufferedImage(
	   			    	          img.getWidth(null), 
	   			    	          img.getHeight(null), 
	   			    	          BufferedImage.TYPE_INT_ARGB);
	   			 Graphics gr = buffImage.getGraphics();
	   			 gr.drawImage(img, 0, 0, null);
	   			 img = buffImage.getSubimage(200, 250, getWidth(), getHeight());
	   		     g.drawImage(img, 0, 0, null);
	   	    }
        };
        // Leeres JLabel-Objekt wird erzeugt
        JLabel label = new JLabel();
        label.setText("<html><body>Der Parameter \""+parameter[0]+"\" fehlt im Inputfile.<br> Bitte in der Einheit \""+
        		parameter[1]+"\" angeben:</body></html>");
        //Textfeld erzeugen:
        tfWert = new JTextField(vorgabe, 15);
        //OK-Button hinzufügen
        buttonOK = new JButton("OK"); 
        //Button wird dem Listener zugeordnet
        buttonOK.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!tfWert.getText().replace(" ", "").equals("")){
					cp.set_ParaInputfile(parameter[0], parameter[1], tfWert.getText());
				}
				synchronized (cp) {
					cp.notify();
				}
				dispose();
			}
		});
        //Label und Button werden dem JPanel hinzugefügt
        panel.add(label);
        panel.add(tfWert);
        panel.add(buttonOK);
        add(panel);
        setVisible(true);
	}
}
