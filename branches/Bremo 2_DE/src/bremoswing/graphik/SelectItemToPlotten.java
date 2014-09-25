package bremoswing.graphik;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.JComboBox;

import bremo.main.Bremo;
import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.SwingBremo;
import bremoswing.util.ExtensionFileFilter;
import bremoswing.util.FertigMeldungFrame;

/**
 *
 * @author Ngueneko Steve
 */
public  class SelectItemToPlotten extends JFrame {
   
	public static boolean pupUp ;
	
	private static final long serialVersionUID = 1L;
	/**
     * Creates new form SelectItemToPlotten
     */
    public SelectItemToPlotten() {
        initComponents();
        SwingBremo.placeFrame(this);
        pupUp = false;
    }

    private void initComponents() {

        jPanel = new JPanel(){
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
			    img = buffImage.getSubimage(200, 210, 500, 100);
				g.drawImage(img, 0, 0, null);
                
            } 
		};
        fileComboBox = new JComboBox();
        
        ButtonOK = new JButton("OK");
        ButtonOK.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				 plott();
			}
		});
        setVisible(true);
        setResizable(false);
        setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo1.png")).getImage());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose to plott");
        jPanel.setBorder(BorderFactory.createTitledBorder("Choose a File to plot !"));

       // fileComboBox.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        String[] tmp = SwingBremo.bremoThreadFertig;
		try {
			addFileItemToComboBox(fileComboBox, tmp);
		} catch (NullPointerException e) {
			setVisible(false);
			callBremoView();
		}
        fileComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				        plott();
			}
		});
        fileComboBox.addKeyListener(new KeyAdapter() {
            @Override 
        	public void keyPressed (KeyEvent e){
            	    	plott();
             }
		});
        
        GroupLayout jPanelLayout = new GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                //.addContainerGap()
                .addComponent(fileComboBox, GroupLayout.PREFERRED_SIZE, 297, GroupLayout.PREFERRED_SIZE)
                .addComponent(ButtonOK,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                //.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
            	.addGroup(jPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            	        .addComponent(fileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(ButtonOK,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>
    
    /**
	 * füge elemente von Array to JComboBox als Item hinzu. Nur für File
	 * ComboBox
	 */
	public void addFileItemToComboBox(JComboBox cb, String[] item) {
		cb.removeAllItems();
		if (item[0] == null) {
			item[0] = "Calculation not made! Choose a file to show ?";
		}
		cb.setModel(new JComboBox(item).getModel());
		  
	}
	
	public void plott(){
		    
		    try {
		    	File input = new File(SwingBremo.path+"/"+fileComboBox.getSelectedItem().toString());
		    	File output = null ;
		        String berechnungModell = "";

//		        boolean is_RestgasVorgabe_LWA = false;

		        BufferedReader in = new BufferedReader(new FileReader(input.getPath()));
				String zeile = null;
				String [] header = null;
				while((zeile = in.readLine()) != null) {
					zeile=zeile.replaceAll(" ", "");
					zeile=zeile.replaceAll("\t", "");
					header = zeile.split(":");
					header[0] = header[0].replace("[", "");
					if (header[0].equals("berechnungsModell")){
						String[] tmp =  header[1].split("_");
						if (tmp[0].equals("DVA")){
							berechnungModell = "DVA";

							output = new File(input.getParent()+"/"+input.getName().replace("txt","dva"));

						}
						else if (tmp[0].equals("APR")){
							berechnungModell = "APR";

							output = new File(input.getParent()+"/"+input.getName().replace("txt", "apr"));

						}
					}
//					if (header[0].equals("RestgasVorgabeLWA")){
//						is_RestgasVorgabe_LWA = true;
//					}
				}
				try {
				   callBremoViewWhitFile(output);
				} catch (NullPointerException e) {
					     callBremoView();
				}
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				new FertigMeldungFrame("Error", e.getMessage(), JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				new FertigMeldungFrame("Error", e.getMessage(), JOptionPane.ERROR_MESSAGE);
			} catch (NullPointerException e){
				new FertigMeldungFrame("Error", e.getMessage(), JOptionPane.ERROR_MESSAGE);
			}
	}
	public static void callBremoView() {
		
	       BremoView view = new BremoView(new BremoViewModel());
	       
	       view.getController().getModel().addObserver(view);
	       
	       view.setVisible(true);
			
		}
		
	public void callBremoViewWhitFile(File file) {
			
		       BremoView view = new BremoView(new BremoViewModel());
		       
		       view.getController().getModel().addObserver(view);
		       
			try {
				view.getController().SendFileModel(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				new FertigMeldungFrame("Error", e.getMessage(),JOptionPane.ERROR_MESSAGE);
			}
		       
		       view.setVisible(true);
				
			}

	

    // Variables declaration
    private JComboBox fileComboBox;
    private JPanel jPanel;
    private JButton ButtonOK;
    // End of variables declaration
}
