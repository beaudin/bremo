package bremoswing.graphik;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartPanel;

import bremoswing.SwingBremo;
import bremoswing.util.BremoExtensionFileFilter;
import bremoswing.util.ExtensionFileFilter;
import bremoswing.util.FertigMeldungFrame;
import bremoswing.util.ImageBackgroundJPanel;
import bremoswing.graphik.BremoViewModel;

public  class BremoView extends JFrame implements ActionListener, Observer {

	static JLabel KITLabel;
	static JLabel IFKMLabel;
	static JLabel TitelLabel;
    static JLabel datumLabel;
    static JLabel pathLabel; 
    static JLabel GraphikLabel; 
    static JLabel Tabelle_InLabel;
    static JLabel Tabelle_PostLabel;
    
    JLabel  x_achse_label;
    JLabel  y_achse_1_label;
    JLabel  y_achse_2_label;
    JLabel  y_achse_3_label;
    JLabel  y_achse_4_label;
    JLabel  achse_to_log_label;
    JLabel nbr_of_Achse_label;
    
    JFileChooser fileChooser;
    
    JButton saveButton;
    JButton openFileButton;
    JButton refreshButton;
    JButton y_achseButton;
    JButton barChartButton;
    
    JButton save_Fav_1 ;
    JButton save_Fav_2 ;
    JButton save_Fav_3 ;
    JButton save_Fav_4 ;
    JButton save_Fav_5 ;
    JButton save_Fav_6 ;
    JButton save_Fav_7 ;
    JButton save_Fav_8 ;
    JButton load_Fav_1 ;
    JButton load_Fav_2 ;
    JButton load_Fav_3 ;
    JButton load_Fav_4 ;
    JButton load_Fav_5 ;
    JButton load_Fav_6 ;
    JButton load_Fav_7 ;
    JButton load_Fav_8 ;
    
    JComboBox<String> x_achse;
    JComboBox<String> y_achse_1;
    JComboBox<String> y_achse_2;
    JComboBox<String> y_achse_3;
    JComboBox<String> y_achse_4;
    JComboBox<String> achse_to_log;
    JComboBox<Integer> nbr_of_Achse;
    
    ImageBackgroundJPanel TitelPanel;
    ImageBackgroundJPanel GraphikPanel;
    ImageBackgroundJPanel GroupPanel;
    ImageBackgroundJPanel TabellePanel;
    
    Font font ;
    BremoViewResource resource ;
    BremoViewController controller ;
    ItemChooseFrame chooseFrame  ;
    
    boolean isBarChartMode;
    
    public BremoView (BremoViewModel Model) {
    	   initComponents (Model);
    }

	private void initComponents(BremoViewModel Model) {
     
		/**  initialization of Variable ***********************************************/
		resource = new BremoViewResource();
		controller = new BremoViewController(Model);		
		isBarChartMode = false ;
		
		TitelPanel   = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,20,50);
		//TitelPanel   = new ImageBackgroundJPanel(resource.iconPaintBremoSwingHD,300,310);
    	GraphikPanel = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,100,100);
    	TabellePanel = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,300,50);
    	GroupPanel   = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,20,70);
    	
    	font = new Font("Tahoma", 0, 14);
    	
    	KITLabel= new JLabel();
        TitelLabel = new JLabel();
        IFKMLabel= new JLabel();
        pathLabel  = new JLabel();
        datumLabel = new JLabel();
                      
        saveButton = new JButton("Save");
        openFileButton = new JButton();
        refreshButton =  new JButton();
        y_achseButton = new JButton("Y-Axe");
        barChartButton= new JButton();
        
        
        openFileButton.setName("openFileButton");
        refreshButton.setName("refreshButton");
        y_achseButton.setName("y_achseButton");
        barChartButton.setName("barChartButton");
        
        save_Fav_1 = new JButton("1");
        save_Fav_2 = new JButton("2");
        save_Fav_3 = new JButton("3");
        save_Fav_4 = new JButton("4");
        save_Fav_5 = new JButton("5");
        save_Fav_6 = new JButton("6");
        save_Fav_7 = new JButton("7");
        save_Fav_8 = new JButton("8");
               
        load_Fav_1 = new JButton("1");
        load_Fav_2 = new JButton("2");
        load_Fav_3 = new JButton("3");
        load_Fav_4 = new JButton("4");
        load_Fav_5 = new JButton("5");
        load_Fav_6 = new JButton("6");
        load_Fav_7 = new JButton("7");
        load_Fav_8 = new JButton("8");
        
        save_Fav_1.setName("s_favs_1");
        save_Fav_2.setName("s_favs_2");
        save_Fav_3.setName("s_favs_3");
        save_Fav_4.setName("s_favs_4");
        save_Fav_5.setName("s_favs_5");
        save_Fav_6.setName("s_favs_6");
        save_Fav_7.setName("s_favs_7");
        save_Fav_8.setName("s_favs_8");
        
        load_Fav_1.setName("l_favs_1");
        load_Fav_2.setName("l_favs_2");
        load_Fav_3.setName("l_favs_3");
        load_Fav_4.setName("l_favs_4");
        load_Fav_5.setName("l_favs_5");
        load_Fav_6.setName("l_favs_6");
        load_Fav_7.setName("l_favs_7");
        load_Fav_8.setName("l_favs_8");
        
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
        achse_to_log = new JComboBox<String>(new String [] {"No Log","Log X-Axe","Log Y-Axe","Double Log"});
       
        x_achse.setName("x_achse"); 
        nbr_of_Achse.setName("nbr_of_Achse");
        achse_to_log.setName("achse_to_log");
        
        achse_to_log.setPreferredSize(new Dimension(140, 22));
        x_achse.setPreferredSize(new Dimension(180, 22));
        y_achseButton.setPreferredSize(new Dimension(80, 25));

        pathLabel.setText(SwingBremo.loadPathFromFile()+File.separator);
              
        controller = new BremoViewController(new BremoViewModel());
        chooseFrame = new ItemChooseFrame(this);
        
        nbr_of_Achse.addItemListener( new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				chooseFrame.lockAxe(nbr_of_Achse.getSelectedItem().toString());
			}
		});
        nbr_of_Achse.addActionListener(this);
        achse_to_log.addActionListener(this);
        x_achse.addActionListener(this);
        openFileButton.addActionListener(this);
        refreshButton.addActionListener(this);
        barChartButton.addActionListener(this);
        
        save_Fav_1.addActionListener(this);
        save_Fav_2.addActionListener(this);
        save_Fav_3.addActionListener(this);
        save_Fav_4.addActionListener(this);
        save_Fav_5.addActionListener(this);
        save_Fav_6.addActionListener(this);
        save_Fav_7.addActionListener(this);
        save_Fav_8.addActionListener(this);
        load_Fav_1.addActionListener(this);
        load_Fav_2.addActionListener(this);
        load_Fav_3.addActionListener(this);
        load_Fav_4.addActionListener(this);
        load_Fav_5.addActionListener(this);
        load_Fav_6.addActionListener(this);
        load_Fav_7.addActionListener(this);
        load_Fav_8.addActionListener(this);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension (1280,800));
        setIconImage(new ImageIcon(getClass().getResource(
				resource.getIconBremoSwing())).getImage());
        setResizable(true);
        
        /** Size and Border of All Panel of the Frame************************************/
        
        TitelPanel.setPreferredSize(new Dimension(1280,50));
        //TitelPanel.setBorder(BorderFactory.createTitledBorder(""));
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
       
        TitelLabel.setFont(new Font("Tahoma", 1, 18)); 
        //TitelLabel.setText(Name);
        URL url = getClass().getResource(resource.getIconBremoSwing());
        ImageIcon icon = new ImageIcon(url);
        Image image = icon.getImage();
        image  = image.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);
        //TitelLabel.setIcon(icon);
       
        
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
        
        GraphikLabel = new JLabel();
        URL urlTest = getClass().getResource(resource.getIconBackgroungChart());
        ImageIcon iconTest = new ImageIcon(urlTest);
        Image imageTest = iconTest.getImage();
        imageTest  = imageTest.getScaledInstance(600, 600,java.awt.Image.SCALE_SMOOTH);
        iconTest = new ImageIcon(imageTest);
        GraphikLabel.setIcon(iconTest);
        ImageBackgroundJPanel panel = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,100,100);
	    panel.add(GraphikLabel);
	    update(panel);
        /********************************************************************************/
        /** Layout of TabellePanel  and TabellePanel Processing *************************/ 
      
      ImageBackgroundJPanel virtualPanel = new ImageBackgroundJPanel(resource.getIconBackgroungColored_1(),100,140);
      virtualPanel.setLayout(new GridLayout(2, 2, 5, 5));
      
      openFileButton.setToolTipText("Open a file to plot the Graphic.");
      refreshButton.setToolTipText("Refresh the Chart.");
      barChartButton.setToolTipText("Enable BarChart Mode"); 
      
      openFileButton.setIcon(new ImageIcon(getClass().getResource(resource.getIconFolder()))); 
	  openFileButton.setRolloverIcon(new ImageIcon(getClass().getResource(resource.getIconFolderRollover())));
      
      refreshButton.setIcon(new ImageIcon(getClass().getResource(resource.getIconRefresh()))); 
	  refreshButton.setRolloverIcon(new ImageIcon(getClass().getResource(resource.getIconRefreshRollover())));
	  
	  barChartButton.setIcon(new ImageIcon(getClass().getResource(resource.getIconBarChart()))); 
	 // barChartButton.setRolloverIcon(new ImageIcon(getClass().getResource(resource.getIconBarChartOut())));
	 // barChartButton.setEnabled(false);
	  
	  openFileButton.setText(" ");
	  refreshButton.setText(" ");
	  
      virtualPanel.add(openFileButton);
      virtualPanel.add(refreshButton);
      virtualPanel.add(barChartButton);
      virtualPanel.setBorder(BorderFactory.createTitledBorder("Manager"));      
      TabellePanel.add(virtualPanel);
      
      ImageBackgroundJPanel virtualPanel2 = new ImageBackgroundJPanel(resource.getIconBackgroungColored_1(),100,140);
      virtualPanel2.setLayout(new GridLayout(8, 2, 5, 5));
      ImageIcon favAddIcon = new ImageIcon(getClass().getResource(resource.getIconFavAdd()));
      ImageIcon favLoadIcon = new ImageIcon(getClass().getResource(resource.getIconFavLoad()));
      
      save_Fav_1.setIcon(favAddIcon);
      save_Fav_2.setIcon(favAddIcon);
      save_Fav_3.setIcon(favAddIcon);
      save_Fav_4.setIcon(favAddIcon);
      save_Fav_5.setIcon(favAddIcon);
      save_Fav_6.setIcon(favAddIcon);
      save_Fav_7.setIcon(favAddIcon);
      save_Fav_8.setIcon(favAddIcon);
      
      load_Fav_1.setIcon(favLoadIcon);
      load_Fav_2.setIcon(favLoadIcon);
      load_Fav_3.setIcon(favLoadIcon);
      load_Fav_4.setIcon(favLoadIcon);
      load_Fav_5.setIcon(favLoadIcon);
      load_Fav_6.setIcon(favLoadIcon);
      load_Fav_7.setIcon(favLoadIcon);
      load_Fav_8.setIcon(favLoadIcon);
      
      save_Fav_1.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 1");
      save_Fav_2.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 2");
      save_Fav_3.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 3");
      save_Fav_4.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 4");
      save_Fav_5.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 5");
      save_Fav_6.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 6");
      save_Fav_7.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 7");
      save_Fav_8.setToolTipText("Store the Index of All Curve on the Graphic as Favorite 8");
      
      load_Fav_1.setToolTipText("load the Index on Favorite 1 to show on the Graphic");
      load_Fav_2.setToolTipText("load the Index on Favorite 2 to show on the Graphic");
      load_Fav_3.setToolTipText("load the Index on Favorite 3 to show on the Graphic");
      load_Fav_4.setToolTipText("load the Index on Favorite 4 to show on the Graphic");
      load_Fav_5.setToolTipText("load the Index on Favorite 5 to show on the Graphic");
      load_Fav_6.setToolTipText("load the Index on Favorite 6 to show on the Graphic");
      load_Fav_7.setToolTipText("load the Index on Favorite 7 to show on the Graphic");
      load_Fav_8.setToolTipText("load the Index on Favorite 8 to show on the Graphic");
      
      virtualPanel2.add(save_Fav_1);
      virtualPanel2.add(load_Fav_1);
      
      virtualPanel2.add(save_Fav_2);
      virtualPanel2.add(load_Fav_2);
      
      virtualPanel2.add(save_Fav_3);
      virtualPanel2.add(load_Fav_3);
      
      virtualPanel2.add(save_Fav_4);
      virtualPanel2.add(load_Fav_4);
      
      virtualPanel2.add(save_Fav_5);
      virtualPanel2.add(load_Fav_5);
      
      virtualPanel2.add(save_Fav_6);
      virtualPanel2.add(load_Fav_6);
      
      virtualPanel2.add(save_Fav_7);
      virtualPanel2.add(load_Fav_7);
      
      virtualPanel2.add(save_Fav_8);
      virtualPanel2.add(load_Fav_8);
      
      virtualPanel2.setBorder(BorderFactory.createTitledBorder("Favorite"));
      
      TabellePanel.add(virtualPanel2);
        
        
        /********************************************************************************/
        refreshDate();
		
		y_achseButton.addActionListener(this);
			
		
		GrouPanelInitialisation() ;
        
        Container pane = getContentPane();
        
        pane.add(TitelPanel,BorderLayout.PAGE_START);
        pane.add(GraphikPanel,BorderLayout.CENTER);
        pane.add(GroupPanel,BorderLayout.PAGE_END);
        pane.add(TabellePanel,BorderLayout.LINE_END);
        pack();
        setVisible(true);
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
		Font font = new Font("Tahoma", Font.BOLD, 16);
		
		nbr_of_Achse_label.setFont(font);
		achse_to_log_label.setFont(font);
		x_achse_label.setFont(font);
		
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
                        .addComponent(y_achseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(y_achseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(datumLabel))
                .addContainerGap())
        );
		
	}
    
    /**
     * Update the Visibility of the Y-axe ChooseFrame
     * @param bol
     */
	private void update( boolean bol) {
		chooseFrame.setVisible(bol);
	}
	/**
	 * Update the Titel 
	 * @param str
	 */
	private void update(String str) {
		setTitle(str);
		//TitelLabel.setText(str.replace(".txt", ""));
		//TitelPanel.revalidate();
	}
	/**
	 * Update the chart of the GraphicPanel
	 * @param chart
	 */
	private void update( Component chart) {
				GraphikPanel.removeAll();;
				GraphikPanel.add(chart);
				GraphikPanel.revalidate();
	
	}
	/**
	 * Update the X_achse Combobox
	 * @param item
	 */
	private void update(String [] item) {
		addItemToComboBox(x_achse, item);
		chooseFrame.addItemtoListBox(item);
		GroupPanel.revalidate();
	}
	/**
	 * Update the path of the Inputfile
	 * @param stb
	 */
	private void update(StringBuilder stb) {
		pathLabel.setText(stb.toString());
		GroupPanel.revalidate();
	}
	/**
	 * Select Item to Combobox and List in Bremoview and ItemChooseFrame
	 * @param list
	 */
	private void update(List<Object> list) {
		
		int indexAxe =  (int) list.get(0);
		nbr_of_Achse.setSelectedIndex(indexAxe);
		
		String indexLog = (String) list.get(1);
		achse_to_log.setSelectedItem(indexLog);
		
		int indexX = (int) list.get(2);
		x_achse.setSelectedIndex(indexX);
		
		int axe = indexAxe + 1;
		
		if (axe > 0 ) {
			int [] y_index_1 = (int[]) list.get(3);
			chooseFrame.y_achse_1.setSelectedIndices(y_index_1);
		}
		if (axe > 1) {
			int [] y_index_2 = (int[]) list.get(4);
			chooseFrame.y_achse_2.setSelectedIndices(y_index_2);
		}
		if (axe > 2) {
			int [] y_index_3 = (int[]) list.get(5);
			chooseFrame.y_achse_3.setSelectedIndices(y_index_3);
		}
		if (axe > 3) {
			int [] y_index_4 = (int[]) list.get(6);
			chooseFrame.y_achse_4.setSelectedIndices(y_index_4);
		}
		chooseFrame.sendDataItemToModel();
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
		   
			fileChooser = new JFileChooser(SwingBremo.loadPathFromFile());
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			BremoExtensionFileFilter bremoExtensionFileFilter = new BremoExtensionFileFilter();
			ExtensionFileFilter [] bremoListExtentionFilter = bremoExtensionFileFilter.getBremoListExtentionFilter();
	        for (int i = 0 ; i < bremoListExtentionFilter.length; i++) {
	        	fileChooser.addChoosableFileFilter(bremoListExtentionFilter[i]);
	        }
	       
			try {
				int status = fileChooser.showOpenDialog(getRootPane());

				if (status == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.getSelectedFile() != null) {
						File file = fileChooser.getSelectedFile();
						if (!isBarChartMode){
							controller.SendFileModel(file,true);
						}
						else {
							controller.SendFileModel(file,false);
						}
						SwingBremo.savePathToFile(file.getParent());
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
			 refreshChart();
			break;

		case "achse_to_log":
			refreshChart();
			break;

		case "x_achse":
			refreshChart();
			break;

		case "y_achseButton":
			try {
			controller.bremoViewData(getAllselectedItem());
			controller.show();
			} catch (NullPointerException excp) {
				new FertigMeldungFrame("Bremo Error", "No Input file found !",
						JOptionPane.ERROR_MESSAGE);
			}
			break;

		case "openFileButton":
				 OpenFileToShowOnBremoView();	
			break;
			
		case "refreshButton":
			
			refreshChart();
			break;
			
		case "barChartButton":
			if (!isBarChartMode) {
				barChartModeEnable();
			}
			else {
				barChartModeDisable();
			}
			break;

			
		case "s_favs_1":
			controller.savefavs(1);
			break;
			
		case "s_favs_2":
			controller.savefavs(2);
			break;
			
		case "s_favs_3":
			controller.savefavs(3);
			break;
			
		case "s_favs_4":
			controller.savefavs(4);
			break;
			
		case "s_favs_5":
			controller.savefavs(5);
			break;
			
		case "s_favs_6":
			controller.savefavs(6);
			break;
			
		case "s_favs_7":
			controller.savefavs(7);
			break;
			
		case "s_favs_8":
			controller.savefavs(8);
			break;
			
		case "l_favs_1":
			controller.loadfavs(1);
			break;
			
		case "l_favs_2":
			controller.loadfavs(2);
			break;
			
		case "l_favs_3":
			controller.loadfavs(3);
			break;
			
		case "l_favs_4":
			controller.loadfavs(4);
			break;
			
		case "l_favs_5":
			controller.loadfavs(5);
			break;
			
		case "l_favs_6":
			controller.loadfavs(6);
			break;
			
		case "l_favs_7":
			controller.loadfavs(7);
			break;
			
		case "l_favs_8":
			controller.loadfavs(8);
			break;
			
			
		default:
			System.err.println("find no component list !");
			break;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {

        if (arg instanceof String) {
        	update((String)arg);
        }
        else if (arg instanceof ChartPanel) {
        	update((ChartPanel)arg);
        }
        else if (arg instanceof Boolean) {
        	update((boolean)arg);
        }
        else if (arg instanceof String [] ) {
        	update((String[])arg);
        }
        else if (arg instanceof StringBuilder ) {
        	update((StringBuilder)arg);
        }
        else if (arg instanceof List ) {
        	update((List<Object>)arg);
        }
	}

	public BremoViewController getController() {
		return controller;
	}
	
	/**
	 * refresh the Chart
	 */
	public void refreshChart() {
		try {
			if (!isBarChartMode) {
				controller.bremoViewData(getAllselectedItem());
				chooseFrame.getChartForBremoView();
				refreshDate();
			}else {
				controller.getModel().readFileToBarChart();
				refreshDate();
			}
		} catch (NullPointerException e) {
             e.printStackTrace();
		}
	}
	/**
	 * refresh the Date
	 */
	public void refreshDate() {
		datumLabel.setFont(new Font("Tahoma",Font.PLAIN, 14)); // NOI18N
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy   HH:mm:ss");
		datumLabel.setText(df.format(dt));
	}
	/**
	 * Active the Mode to show BarChart
	 */
	public void barChartModeEnable() {
		 
		 barChartButton.setIcon(new ImageIcon(getClass().getResource(resource.getIconButtonChart())));
	     barChartButton.setToolTipText("Enable LineChart Mode");
	     DeactiveComponent();	     
	     isBarChartMode = true ;
		 URL urlTest = getClass().getResource(resource.getIconBarChartBig());
	     ImageIcon iconTest = new ImageIcon(urlTest);
	     Image imageTest = iconTest.getImage();
	     imageTest  = imageTest.getScaledInstance(600, 600,java.awt.Image.SCALE_SMOOTH);
	     iconTest = new ImageIcon(imageTest);
	     GraphikLabel.setIcon(iconTest);
	     GraphikLabel.revalidate();
	     ImageBackgroundJPanel panel = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,100,100);
	     panel.add(GraphikLabel);
	     update(panel);
	     update("BarChart Mode");
	}
	
	/**
	 * Active the Mode to show BarChart
	 */
	public void barChartModeDisable() {
		 
		 barChartButton.setIcon(new ImageIcon(getClass().getResource(resource.getIconBarChart())));
	     barChartButton.setToolTipText("Enable BarChart Mode");
	     ActiveComponent();
	     isBarChartMode = false;
		 URL urlTest = getClass().getResource(resource.getIconBackgroungChart());
	     ImageIcon iconTest = new ImageIcon(urlTest);
	     Image imageTest = iconTest.getImage();
	     imageTest  = imageTest.getScaledInstance(600, 600,java.awt.Image.SCALE_SMOOTH);
	     iconTest = new ImageIcon(imageTest);
	     GraphikLabel.setIcon(iconTest);
	     GraphikLabel.revalidate();
	     ImageBackgroundJPanel panel = new ImageBackgroundJPanel(resource.iconBackgroungColored_1,100,100);
	     panel.add(GraphikLabel);
	     update(panel);
	     update("LineChart Mode");
	     
	}
	
	protected void ActiveComponent() {
		x_achse.setEnabled(true);
	    nbr_of_Achse.setEnabled(true);
	    achse_to_log.setEnabled(true);
	    y_achseButton.setEnabled(true);
	    save_Fav_1.setEnabled(true);
		save_Fav_2.setEnabled(true);
		save_Fav_3.setEnabled(true);
		save_Fav_4.setEnabled(true);
		save_Fav_5.setEnabled(true);
		save_Fav_6.setEnabled(true);
		save_Fav_7.setEnabled(true);
		save_Fav_8.setEnabled(true);
		load_Fav_1.setEnabled(true);
		load_Fav_2.setEnabled(true);
		load_Fav_3.setEnabled(true);
		load_Fav_4.setEnabled(true);
		load_Fav_5.setEnabled(true);
		load_Fav_6.setEnabled(true);
		load_Fav_7.setEnabled(true);
		load_Fav_8.setEnabled(true);
	     
	}
    protected void DeactiveComponent() {
		x_achse.setEnabled(false);
		nbr_of_Achse.setEnabled(false);
		achse_to_log.setEnabled(false);
		y_achseButton.setEnabled(false);
	    save_Fav_1.setEnabled(false);
		save_Fav_2.setEnabled(false);
		save_Fav_3.setEnabled(false);
		save_Fav_4.setEnabled(false);
		save_Fav_5.setEnabled(false);
		save_Fav_6.setEnabled(false);
		save_Fav_7.setEnabled(false);
		save_Fav_8.setEnabled(false);
		load_Fav_1.setEnabled(false);
		load_Fav_2.setEnabled(false);
		load_Fav_3.setEnabled(false);
		load_Fav_4.setEnabled(false);
		load_Fav_5.setEnabled(false);
		load_Fav_6.setEnabled(false);
		load_Fav_7.setEnabled(false);
		load_Fav_8.setEnabled(false);
	}
}
