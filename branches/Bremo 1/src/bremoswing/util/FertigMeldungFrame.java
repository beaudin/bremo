package bremoswing.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import bremoswing.SwingBremo;

/**
 * @author Ngueneko  steve
 */
public class FertigMeldungFrame extends javax.swing.JFrame {


    // Variables declaration 
	private JPanel panel;
    private JButton OkButton;
    private JLabel infoLabel;
    // End of variables declaration   
    
    /**
     * Creates new form FertigMeldungFrame
     */
    public FertigMeldungFrame(String Titel,String Message, int MessageTyp) {
        initComponents(Titel, Message, MessageTyp);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")                        
    private void initComponents(String Titel, String Message, int MessageTyp) {
    	
    	panel = new JPanel(){
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
    	};
    	setTitle(Titel);
    	setResizable(false);
    	
    	setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo1.png")).getImage());
    	URL url;
    	ImageIcon icon;
        switch (MessageTyp) {
        case JOptionPane.WARNING_MESSAGE: 
            url = getClass().getResource("/bremoswing/bild/warningIcon.png");
    		icon = new ImageIcon(url);
    	    infoLabel =  new JLabel(Message,icon, SwingConstants.LEFT);
    	    break;
        case JOptionPane.INFORMATION_MESSAGE: 
        	url = getClass().getResource("/bremoswing/bild/informationIcon.png");
    		icon = new ImageIcon(url);
    	    infoLabel =  new JLabel(Message,icon, SwingConstants.LEFT);
    	    break;
        case JOptionPane.ERROR_MESSAGE:
        	url = getClass().getResource("/bremoswing/bild/errorIcon.png");
    		icon = new ImageIcon(url);
    	    infoLabel =  new JLabel(Message,icon, SwingConstants.LEFT);
    	    break;
        default :
        	url = getClass().getResource("/bremoswing/bild/questionIcon2.png");
    		icon = new ImageIcon(url);
    	    infoLabel =  new JLabel(Message,icon, SwingConstants.LEFT);
    	    break;
        }
        
	    infoLabel.setFont(new java.awt.Font("Tahoma", 0, 14));
       
	    OkButton  = new JButton("OK");
	    OkButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			   dispose();
			}
		});
        OkButton.addKeyListener(new KeyAdapter() {
            @Override 
        	public void keyPressed (KeyEvent e){
            	 
            	if (e.getKeyCode() == KeyEvent.VK_ENTER){
            		dispose();   
            	}
             }
		});
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 307))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(OkButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(OkButton)
                .addContainerGap())
        );
        setContentPane(panel);
        pack();
        SwingBremo.placeFrame(this);
        setVisible(true);
        
    }
    
}
