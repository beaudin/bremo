package bremoswing.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class ImageBackgroundJOptionPane extends JOptionPane {

     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
     public void paintComponent(Graphics g) 
     {
       
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
}
