package bremoExceptions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import bremo.parameter.CasePara;


public class eingabeFeld extends JFrame implements ActionListener{
	
	CasePara CP;
	JFrame exceptionFrame;
	JTextField tfWert;
	JButton buttonOK;
	String[] para;
	
	public eingabeFeld(CasePara cp, String[] parameter, String vorgabe){
		this.CP = cp;
		this.para = parameter;
		exceptionFrame = new JFrame();
		exceptionFrame.setTitle("Fehlender Parameter im Inputfile");
		this.setSize(400, 200);
        JPanel panel = new JPanel();
        // Leeres JLabel-Objekt wird erzeugt
        JLabel label = new JLabel();
        label.setText("Der Parameter \""+parameter[0]+"\" fehlt im Inputfile.\n Bitte in der Einheit \""+parameter[1]+"\" angeben:");
        //Textfeld erzeugen:
        tfWert = new JTextField(vorgabe, 15);
        //OK-Button hinzufügen
        buttonOK = new JButton("OK"); 
        //Button wird dem Listener zugeordnet
        buttonOK.addActionListener(this);
        //Label und Button werden dem JPanel hinzugefügt
        panel.add(label);
        panel.add(tfWert);
        panel.add(buttonOK);
        this.add(panel);
        exceptionFrame.pack();
        this.setVisible(true);
	}
	public void actionPerformed(ActionEvent ae) {
		if(!tfWert.getText().replace(" ", "").equals("")){
			CP.set_ParaInputfile(para[0], para[1], tfWert.getText());
		}
		this.setVisible(false);
		CP.notify();
	}
	

}
