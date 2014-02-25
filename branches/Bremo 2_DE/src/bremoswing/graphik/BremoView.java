package bremoswing.graphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;

import bremoswing.util.ExtensionFileFilter;

public  class BremoView extends JFrame implements ActionListener, Observer {

	static JLabel KITLabel;
	static JLabel IFKMLabel;
	static JLabel TitelLabel;
    static JLabel datumLabel;
    static JLabel pathLabel; 
    static JLabel Tabelle_InLabel;
    static JLabel Tabelle_PostLabel;
    
    JLabel  x_achse_label;
    JLabel  y_achse_1_label;
    JLabel  y_achse_2_label;
    JLabel  y_achse_3_label;
    JLabel  y_achse_4_label;
    JLabel  achse_to_log_label;
    JLabel nbr_of_Achse_label;
    
    
    JButton saveButton;
    JButton openFileButton;
    JButton favoriteButton;
    JButton y_achse_button;
    
    JComboBox<String> x_achse;
    JComboBox<String> y_achse_1;
    JComboBox<String> y_achse_2;
    JComboBox<String> y_achse_3;
    JComboBox<String> y_achse_4;
    JComboBox<String> achse_to_log;
    JComboBox<Integer> nbr_of_Achse;
    
    JPanel TitelPanel;
    JPanel GraphikPanel;
    JPanel GroupPanel;
    JPanel TabellePanel;
    
    Font font ;
    BremoViewResource resource ;
    BremoViewController controller ;
    ItemChooseFrame chooseFrame  ;
    
    String path;
    
    public BremoView (BremoViewModel Model) {
    	   initComponents (Model);
    }

	private void initComponents(BremoViewModel Model) {
     
		/**  initialization of Variable ***********************************************/
    	
		controller = new BremoViewController(Model);
		
		path = ".";
		
		TitelPanel   = new JPanel();
    	GraphikPanel = new JPanel();
    	TabellePanel = new JPanel();
    	GroupPanel   = new JPanel();
    	
    	font = new Font("Tahoma", 0, 14);
    	
    	KITLabel= new JLabel();
        TitelLabel = new JLabel();
        IFKMLabel= new JLabel();
        pathLabel  = new JLabel();
        datumLabel = new JLabel();
                      
        saveButton = new JButton("Save");
        openFileButton = new JButton(" Open ");
        favoriteButton = new JButton("Favs");
        y_achse_button = new JButton("Y-Axe");
        
        openFileButton.setName("openFileButton");
        favoriteButton.setName("favoriteButon");
        y_achse_button.setName("y_achse_button");
        
        x_achse_label   = new JLabel("X-Axe :");
        y_achse_1_label = new JLabel("1. Y-Axe :") ;
        y_achse_2_label = new JLabel("2. Y-Axe :");
        y_achse_3_label = new JLabel("3. Y-Axe :");
        y_achse_4_label = new JLabel("4. Y-Axe :");
        achse_to_log_label = new JLabel("Axe With : ");
        nbr_of_Achse_label = new JLabel("Nbr. of Axe : ");
                
        x_achse_label.setFont(font);
        y_achse_1_label.setFont(font);
        y_achse_2_label.setFont(font);
        y_achse_3_label.setFont(font);
        y_achse_4_label.setFont(font);
        achse_to_log_label.setFont(font);
        nbr_of_Achse_label.setFont(font);
        pathLabel.setFont(font);
        datumLabel.setFont(font);
        
        x_achse   = new JComboBox<String>();
        y_achse_1 = new JComboBox<String>();
        y_achse_2 = new JComboBox<String>();
        y_achse_3 = new JComboBox<String>();
        y_achse_4 = new JComboBox<String>();
        nbr_of_Achse = new JComboBox<Integer>(new Integer [] {1,2,3,4});
        achse_to_log = new JComboBox<String>(new String [] {"No Log","Log auf X-Achse","Log auf Y-Achse","Doppel Log"});
       
        x_achse.setName("x_achse"); 
        nbr_of_Achse.setName("nbr_of_Achse");
        achse_to_log.setName("achse_to_log");
        
        achse_to_log.setPreferredSize(new Dimension(140, 22));
        x_achse.setPreferredSize(new Dimension(180, 22));
        y_achse_button.setPreferredSize(new Dimension(80, 25));

        pathLabel.setText("Path");
        
        controller = new BremoViewController(new BremoViewModel());
        resource = new BremoViewResource();
        chooseFrame = new ItemChooseFrame(this);
      
        openFileButton.addActionListener(this);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension (1280,800));
        setIconImage(new ImageIcon(getClass().getResource(
				resource.getIconBremoSwing())).getImage());
        setResizable(true);
        
        /** Size and Border of All Panel of the Frame************************************/
        
        TitelPanel.setPreferredSize(new Dimension(1280,50));
        //TitelPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GraphikPanel.setPreferredSize(new Dimension (1010,675));
        GraphikPanel.setBorder(BorderFactory.createTitledBorder(""));
        TabellePanel.setPreferredSize(new Dimension(270,675));
        TabellePanel.setBorder(BorderFactory.createTitledBorder(""));
        GroupPanel.setPreferredSize(new Dimension (1280,75));
        GroupPanel.setBorder(BorderFactory.createTitledBorder(""));

        /********************************************************************************/ 
        
        /** TitelLabel Processing  ******************************************************/
        
        URL urlKit= getClass().getResource(resource.getIconKIT());
        ImageIcon iconKIT = new ImageIcon(urlKit);
        Image imageKit = iconKIT.getImage();
        imageKit  = imageKit.getScaledInstance(95, 45,java.awt.Image.SCALE_SMOOTH);
        iconKIT = new ImageIcon(imageKit);
        KITLabel.setIcon(iconKIT);
       
        TitelLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); 
        //TitelLabel.setText(Name);
        URL url = getClass().getResource(resource.getIconBremoSwing());
        ImageIcon icon = new ImageIcon(url);
        Image image = icon.getImage();
        image  = image.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);
        TitelLabel.setIcon(icon);
       
        
        URL urlIFKM = getClass().getResource(resource.getIconIFKM());
        ImageIcon iconIFKM = new ImageIcon(urlIFKM);
        Image imageIFKM = iconIFKM.getImage();
        imageIFKM  = imageIFKM.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        iconIFKM = new ImageIcon(imageIFKM);
        IFKMLabel.setIcon(iconIFKM);
        
        TitelPanelInitialisation() ;
        //TitelPanelUpdate();
        
        /********************************************************************************/
        /**  Layout of GraphikPanel  ****************************************************/
        
        GridLayout GraphikPanelLayout = new GridLayout();
        GraphikPanelLayout.setColumns(1);
        GraphikPanelLayout.setRows(1);
        GraphikPanel.setLayout(GraphikPanelLayout);
        
        JLabel TestLabel = new JLabel();
        URL urlTest = getClass().getResource(resource.getIconBackgroungWater());
        ImageIcon iconTest = new ImageIcon(urlTest);
        Image imageTest = iconTest.getImage();
        //imageTest  = imageTest.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        iconTest = new ImageIcon(imageTest);
        TestLabel.setIcon(iconTest);
        GraphikPanel.add(TestLabel);
        
        /********************************************************************************/
        /** Layout of TabellePanel  and TabellePanel Processing *************************/ 
      
      JPanel virtualPanel = new JPanel(new GridLayout(2, 2, 5, 10));
      
      openFileButton.setToolTipText("Open other file to show on the Graphic.");
      favoriteButton.setToolTipText("Save this Graphic Setting as Favorite.");
      
      virtualPanel.add(openFileButton);
      virtualPanel.add(favoriteButton);
            
      TabellePanel.add(virtualPanel);
        
        
        /********************************************************************************/
        datumLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		datumLabel.setText(df.format(dt));
		
		y_achse_button.addActionListener(this);
			
		
		GrouPanelInitialisation() ;
        
        Container pane = getContentPane();
        
        pane.add(TitelPanel,BorderLayout.PAGE_START);
        pane.add(GraphikPanel,BorderLayout.CENTER);
        pane.add(GroupPanel,BorderLayout.PAGE_END);
        pane.add(TabellePanel,BorderLayout.LINE_END);
		
        setVisible(true);
        pack();
	}
	
	 private void TitelPanelInitialisation() {
		
		 javax.swing.GroupLayout layout = new javax.swing.GroupLayout(TitelPanel);
	        TitelPanel.setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addComponent(KITLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 547, Short.MAX_VALUE)
	                .addComponent(TitelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 547, Short.MAX_VALUE)
	                .addComponent(IFKMLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                    .addGap(0,2,2)
	                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                           .addComponent(KITLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
	                           .addComponent(IFKMLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
	                           .addComponent(TitelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
	        );
		
	}

	private void GrouPanelInitialisation() {
		 
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(GroupPanel);
        GroupPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nbr_of_Achse_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nbr_of_Achse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 30, 30)
                        .addComponent(achse_to_log_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(achse_to_log, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 100, Short.MAX_VALUE)
                        .addComponent(x_achse_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(x_achse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 30, 30)
                        .addComponent(y_achse_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 100, 270))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(datumLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nbr_of_Achse_label)
                    .addComponent(nbr_of_Achse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(achse_to_log_label)
                    .addComponent(achse_to_log, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(x_achse_label)
                    .addComponent(x_achse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y_achse_button, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(datumLabel))
                .addContainerGap())
        );
		
	}

	/**
     * Update the TitelPanel
     */
    void TitelPanelUpdate () {
    	
    	TitelPanel.removeAll();
    	TitelPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.weightx = 0;
		
        int links = (TitelPanel.getPreferredSize().width / 2)-(TitelLabel.getPreferredSize().width/2)-KITLabel.getPreferredSize().width-5;
        int right = (TitelPanel.getPreferredSize().width / 2)-(TitelLabel.getPreferredSize().width/2)-IFKMLabel.getPreferredSize().width-5;
      
        gc.insets = new Insets(0, 0, 0, links);
        gc.gridx = 0;
		gc.gridy = 0;
        TitelPanel.add(KITLabel,gc);
        
        gc.insets = new Insets(0, 0, 0, 0);
        gc.gridx = 1;
		gc.gridy = 0;
        TitelPanel.add(TitelLabel,gc);
        
        gc.insets = new Insets(0,right , 0, 0);
        gc.gridx = 2;
		gc.gridy = 0;
        TitelPanel.add(IFKMLabel,gc);
    	TitelPanel.revalidate();
    }
    
   
	public void update( boolean bol) {
		chooseFrame.setVisible(bol);
	}
	
	public void update(String str) {
		TitelLabel.setText(str);
		TitelPanel.revalidate();
	}
	public void update(ChartPanel chart) {
		GraphikPanel.removeAll();
		GraphikPanel.add(chart);
		GraphikPanel.revalidate();
	}
	public void update(String [] item) {
		addItemToComboBox(x_achse, item);
		chooseFrame.addItemtoListBox(item);
		GroupPanel.revalidate();
	}
	
	/**
	 * Give all necessary item who are selected
	 * @return 
	 *         Array of item : 1. nbr of Axe
	 *                         2. axe whit log 
	 *                         3. X Axe 
	 */
	public  String []  getAllselectedItem () {
		
		return new String [] { nbr_of_Achse.getSelectedItem().toString(),
				               achse_to_log.getSelectedItem().toString(),
				               x_achse.getSelectedItem().toString()};
				
	}
	
	/**
	    * open a FileChoser to select result file to show on the view
	    */
	   void OpenFileToShowOnBremoView() {
		   
		    JFileChooser fileChooser = new JFileChooser(path);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			ExtensionFileFilter txtFilter = new ExtensionFileFilter(null,
					new String[] { "txt" });

			fileChooser.addChoosableFileFilter(txtFilter);
			try {
				int status = fileChooser.showOpenDialog(getRootPane());

				if (status == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.getSelectedFile() != null) {
						File file = fileChooser.getSelectedFile();
						controller.SendFileModel(file);
						path = file.getParent();
											}
				} else if (status == JFileChooser.CANCEL_OPTION) {

					fileChooser.cancelSelection();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	   
	   /**
		 * add Item from Array to JComboBox 
		 * @param cb
		 * @param item
		 */
		
		@SuppressWarnings("unchecked")
		public  void addItemToComboBox(JComboBox<String> cb, String[] item) {

			cb.removeAllItems();
			cb.setModel(new JComboBox<String>(item).getModel());
				
	    }
		
		/**
		 * add Item from Array to JComboBox 
		 * @param cb
		 * @param item
		 */
		@SuppressWarnings("unchecked")
		public  void addItemToComboBox(JComboBox<Integer> cb, Integer [] item) {

			cb.removeAllItems();
			cb.setModel(new JComboBox<Integer>(item).getModel());
				
	    }

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (((Component) e.getSource()).getName()) {

		case "nbr_of_Achse":

			break;

		case "achse_to_log":
			break;

		case "x_achse":
			break;

		case "y_achse_button":
			controller.bremoViewData(getAllselectedItem());
			chooseFrame.lockAxe(nbr_of_Achse.getSelectedItem().toString());
			controller.show();
			break;

		case "openFileButton":
			OpenFileToShowOnBremoView();
			break;

		case "favoriteButon":
			break;

		default:
			System.err.println("find no component list !");
			break;
		}

	}

	@Override
	public void update(Observable o, Object arg) {

        if (arg instanceof String) {
        	update((String)arg);
        }
        if (arg instanceof ChartPanel) {
        	update((ChartPanel)arg);
        }
        if (arg instanceof Boolean) {
        	update((boolean)arg);
        }
        if (arg instanceof String [] ) {
        	update((String[])arg);
        }
	}

	public BremoViewController getController() {
		return controller;
	}
}
