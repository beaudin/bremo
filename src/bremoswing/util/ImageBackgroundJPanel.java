package bremoswing.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImageBackgroundJPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String imgPath;
	private int xPoint ;
	private int yPoint ;
	  public ImageBackgroundJPanel(String imagePath) {
	    this.imgPath = imagePath;
	    xPoint = -1;
		yPoint = -1;
	    //repaint();
	  }
	  public ImageBackgroundJPanel(String imagePath, int x, int y) {
		    this.imgPath = imagePath;
		    xPoint=x;
		    yPoint=y;
		//    repaint();
		  }
	 
	  @Override
	  protected void paintComponent(Graphics g) {
		  URL url = getClass().getResource(imgPath);
			ImageIcon icon = new ImageIcon(url);
		    Image img = icon.getImage();
		    BufferedImage buffImage = 
		    	      new BufferedImage(
		    	          img.getWidth(null), 
		    	          img.getHeight(null), 
		    	          BufferedImage.TYPE_INT_ARGB);
		    Graphics gr = buffImage.getGraphics();
		    gr.drawImage(img, 0, 0, null);
		    if (xPoint < 0 || yPoint < 0 ){
		    	 img = buffImage.getSubimage(0, 0, img.getWidth(null), img.getHeight(null));
		    } 
		    else {
		    	 img = buffImage.getSubimage(xPoint, yPoint, img.getWidth(null)-xPoint, img.getHeight(null)-yPoint);
		    }
		    
			g.drawImage(img, 0, 0, null);
	  }
}
