package bremoswing.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PaintPanel extends JPanel {
    

	private static final long serialVersionUID = 942657684289947210L;
	
	String path ;
	int x;
	int y;
	int width;
	int height;
	
	public PaintPanel(String path, int x , int y , int width, int height) {
	
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
	}
	
	
	 public void paintComponent(Graphics g)
     {
      
                         URL url = getClass().getResource(path);
                         ImageIcon icon = new ImageIcon(url);
                     Image img = icon.getImage();
                     BufferedImage buffImage =
                               new BufferedImage(
                                   img.getWidth(null),
                                   img.getHeight(null),
                                   BufferedImage.TYPE_INT_ARGB);
                     Graphics gr = buffImage.getGraphics();
                     gr.drawImage(img, 0, 0, null);
                     img = buffImage.getSubimage(x, y, width, height);
                         g.drawImage(img, 0, 0, null);
        
     }

}
