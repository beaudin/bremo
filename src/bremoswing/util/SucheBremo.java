package bremoswing.util;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * @author Ngueneko
 * SUCHEFUNKTION FUER BREMO JTEXTAREA
 */

public class SucheBremo extends JFrame{
	
	
	//Components
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel label1 = new JLabel("Suche :"); 
	JButton button1 = new JButton("Suchen");  
	JButton button2 = new JButton("next"); 
	JTextField field1 = new JTextField(20);
	FlowLayout experimentLayout = new FlowLayout();
	int Occur = 0;	
	String  text = "";
	int start = 0;
	int end = 0;
	
	public SucheBremo() {
		// TODO Auto-generated constructor stub
	}
	
	public SucheBremo( final JTextPane area) {
		
		setSize(370,70);
	    setTitle("Text Suchen im Bremo");
	    setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/search_blue.png")).getImage());
	    setLocationRelativeTo(null);
	    setResizable(false);
	   
	    setLayout(experimentLayout);
	    experimentLayout.setAlignment(FlowLayout.LEADING);
	   
	    getContentPane().add(label1);
	    getContentPane().add(field1);
	    getContentPane().add(button1);
	    field1.addKeyListener(new KeyAdapter() {			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					suche(area);
				}
			}
		});
	    button1.addActionListener( new ActionListener() {
	            	public void actionPerformed(ActionEvent evt) {
	            		b1Push(evt);
	            	}

					private void b1Push(ActionEvent evt) {
						// TODO Auto-generated method stub
						suche(area);
					}
	            });	    
	    setVisible(true);
		
	}
	
	public void suche(JTextPane area)  {
		
		if(area.getText().toLowerCase().contains(field1.getText().toLowerCase()))
        {    
   	     if (!text.equals(field1.getText())){
   	        start = area.getText().toLowerCase().indexOf(field1.getText().toLowerCase());
        	end = start + field1.getText().length();
        	area.select(start, end);
        	text = field1.getText();
        	Occur++;
   	     }
   	     else if (end < area.getText().length()){
   	    	start = area.getText().toLowerCase().indexOf(field1.getText().toLowerCase(), end+1);
     	    end = start + field1.getText().length();
     	    if (start == -1) {
     	    	start = 0;
     	    	end   = 0;
     	    	text="";
     	    	Occur = 0;
     	    	java.awt.Toolkit.getDefaultToolkit().beep();
     	    }
     	    else {
     	    area.select(start, end);
     	    Occur++;
     	    }
     	  }
        }
        else
        {
            field1.setText("Keine ergebnisse gefunden.");
            text = null;
            Occur = 0;
        }
		
	}
	
	void setTextArea(JTextPane area) {
		
	}
	
	void FrameClose () {
		 
	}
}
