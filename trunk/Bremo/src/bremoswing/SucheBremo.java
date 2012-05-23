package bremoswing;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Ngueneko
 * SUCHEFUNKTION FUER BREMO JTEXTAREA
 */

public class SucheBremo extends JFrame{
	
	
	//Components
	
	JLabel label1 = new JLabel("Geben sie den zu suchenden Begrif unten ein:"); 
	JButton button1 = new JButton("Suchen");  
	JButton button2 = new JButton("next"); 
	JTextField field1 = new JTextField();
	int Occur = 0;	
	String  text = "";
	int start = 0;
	int end = 0;
	
	SucheBremo( final JTextArea area) {
		
		setSize(280,140);
	    setTitle("Text Suchen im Bremo");
	    setLocationRelativeTo(null);
	    setResizable(false);
	    setLayout(new GridLayout(3,1));
	    add(label1);
	    add(field1);
	    add(button1);
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
	
	public void suche(JTextArea area)  {
		
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
     	    area.select(start, end);
     	    Occur++;
     	   }
        	
        	System.err.println("JA! <<"+field1.getText()+ ">> Kommt für das "+Occur+". Mal Vor");
        }
        else
        {
            field1.setText("Keine ergebnisse gefunden.");
            text = null;
            Occur = 0;
        }
		
	}
 
}
