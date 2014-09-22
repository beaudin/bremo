package bremoExceptions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import bremo.parameter.CasePara;


public class eingabeFeld extends JFrame {
	
	JTextField tfWert;
	JButton buttonOK;
	
	public eingabeFeld(final CasePara cp, final String[] parameter, String vorgabe){
		setTitle("Fehlender Parameter im Inputfile");
		setSize(400, 200);
        JPanel panel = new JPanel();
        // Leeres JLabel-Objekt wird erzeugt
        JLabel label = new JLabel();
        label.setText("Der Parameter \""+parameter[0]+"\" fehlt im Inputfile.\n Bitte in der Einheit \""+parameter[1]+"\" angeben:");
        //Textfeld erzeugen:
        tfWert = new JTextField(vorgabe, 15);
        //OK-Button hinzufügen
        buttonOK = new JButton("OK"); 
        //Button wird dem Listener zugeordnet
        buttonOK.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
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
