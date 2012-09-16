/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bremoGraphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bremoExceptions.ParameterFileWrongInputException;

import bremoGraphik.MyPrinter;

/**
 *
 * @author Ngueneko Steve
 */
public class bremoGraphik extends JFrame {

    /**
     * Creates new form bremoGaphik
     * @throws IOException 
     * @throws ParameterFileWrongInputException 
     */
    public bremoGraphik(File file) throws IOException, ParameterFileWrongInputException {
    	// TODO Auto-generated constructor stub
    	if (file == null) throw new FileNotFoundException();
        
    	initComponents(file);
    }

	/**
     * This method is called from within the constructor to initialize the form.
	 * @throws IOException 
	 * @throws ParameterFileWrongInputException 
     */
    @SuppressWarnings({ "unchecked", "static-access" })
   
    private void initComponents(File file) throws IOException, ParameterFileWrongInputException {
        
    	/**  initialization of Variable ***********************************************/
    	TitelPanel = new JPanel();
    	GraphikPanel = new JPanel();
    	TabellePanel = new JPanel();
    	GroupPanel  = new JPanel();
        chartBremoPanel = new JPanel();
        TitelLabel = new JLabel();
        druckverlaufPanel = new JPanel();
        graphik1Panel = new JPanel();
        graphik2Panel = new JPanel();
        graphik3Panel = new JPanel();
        tabelle_Post_File = new JPanel();
        tabelle_Input_File = new JPanel();
        datumLabel = new JLabel();
        druckenButton = new JButton();
        speichernButton = new JButton();
        pathLabel = new JLabel();
        graphik3ComboBox1 = new JComboBox();
        graphik3ComboBox2 = new JComboBox();
        graphik2ComboBox1 = new JComboBox();
        graphik2ComboBox2 = new JComboBox();
        inputfile = file;
        itemAction = new ItemAction();
        /********************************************************************************/
        
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension (1280,800));
        setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo2.png")).getImage());
        
        
        /** Size and Border of All Panel of the Frame************************************/
        
        TitelPanel.setPreferredSize(new Dimension(1280,50));
        TitelPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GraphikPanel.setPreferredSize(new Dimension (1010,675));
        GraphikPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        TabellePanel.setPreferredSize(new Dimension(270,675));
        TabellePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GroupPanel.setPreferredSize(new Dimension (1280,75));
        GroupPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        /********************************************************************************/     
        
        /** TitelLabel Processing  ******************************************************/
        
        TitelLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        TitelLabel.setText(inputfile.getName());
        URL url = getClass().getResource("/bremoswing/bild/bremo2.png");
        ImageIcon icon = new ImageIcon(url);
        Image image = icon.getImage();
        image  = image.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);
        TitelLabel.setIcon(icon);
        TitelPanel.add(TitelLabel, BorderLayout.CENTER);
        /********************************************************************************/
        
        /**  Layout of GraphikPanel  ****************************************************/
        
        GridLayout GraphikPanelLayout = new GridLayout();
        GraphikPanelLayout.setColumns(2);
        GraphikPanelLayout.setRows(2);
        GraphikPanelLayout.setHgap(10); 
        GraphikPanelLayout.setVgap(10); 
        GraphikPanel.setLayout(GraphikPanelLayout);
        /********************************************************************************/
        
        /** Layout of TabellePanel  and TabellePanel Processing *************************/ 
//        
//        GridLayout TabellePanelLayout = new GridLayout();
//        TabellePanelLayout.setColumns(1);
//        TabellePanelLayout.setRows(3);
//        TabellePanel.setLayout(TabellePanelLayout);
        JLabel TabellePostLabel = TabellePostFIle();
		JLabel TabelleInputLabel = TabelleInputFile();
		
        Box b1 = Box.createHorizontalBox();
        b1.add(Box.createHorizontalStrut(7));
        b1.add(TabellePostLabel);
        
        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalStrut(25));
        b2.add(TabelleInputLabel);
        
        Box b3 = Box.createVerticalBox();
        b3.add(Box.createVerticalStrut(20));
        b3.add(b1);
        b3.add(Box.createVerticalStrut(100));
        b3.add(b2);
        
        TabellePanel.add(b3);
        
        /********************************************************************************/
        
		/**  GraphikPanel Processing  **************************************************/
        
        GraphikPanel.add(Druckverlauf());
		GraphikPanel.add(dQb_Qb_Qmax_Verlauf());
		GraphikPanel.add(p_V_Diagramm ());
		is_P_V_Diagramm = true;
		GraphikPanel.add(T_mittel_Verlauf());
		is_Verlustteilung_Digramm = false;
		/*******************************************************************************/
		String str = "<html><b>\" Bitte Wählen : \"</b></html>";
		graphik2ComboBox1.addItem(str);
		graphik2ComboBox1.addItem("p-V-Digramm");
		graphik2ComboBox1.addItem("Andere Diagramm");
				 
//		graphik2ComboBox1.setModel(new DefaultComboBoxModel(new String[] {
//				"p-V-Digramm",
//				"Andere Diagramm",
//				 }));
		graphik2ComboBox1
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						try {
							graphik2ComboBox1ActionPerformed(evt);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		graphik2ComboBox2.setPreferredSize(new Dimension(150,30));
		
		graphik3ComboBox1.addItem(str);
		graphik3ComboBox1.addItem("Andere Diagramm");
		graphik3ComboBox1.addItem("Verlustteilung BalkenDiagramm");
		graphik3ComboBox1
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						try {
							graphik3ComboBox1ActionPerformed(evt);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		
		graphik3ComboBox2.setPreferredSize(new Dimension(150,30));
		
		pathLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		pathLabel.setText(inputfile.getParent());

		speichernButton.setText("Speichern");
		speichernButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				speichernButtonActionPerformed(evt);
			}
		});

		druckenButton.setText(" Drucken ");
		druckenButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				druckenButtonActionPerformed(evt);
			}
		});

		datumLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		datumLabel.setText(df.format(dt));
		
	    Box box1 = Box.createHorizontalBox();
	    box1.add(graphik2ComboBox1);
	    box1.add(graphik2ComboBox2);
	    box1.add(Box.createHorizontalStrut(110));
	    box1.add(graphik3ComboBox1);
	    box1.add(graphik3ComboBox2);
	    box1.add(Box.createHorizontalStrut(290));
	    
	    Box box2 = Box.createHorizontalBox();
	    box2.add(pathLabel);
	    box2.add(Box.createHorizontalStrut(255));  
	    box2.add(speichernButton);
	    box2.add(Box.createHorizontalStrut(125));
	    box2.add(druckenButton);
	    box2.add(Box.createHorizontalStrut(155));
	    box2.add(datumLabel);
	    
	    Box box3 = Box.createVerticalBox();
	    box3.add(box1);
	    box3.add(Box.createVerticalStrut(8));
	    box3.add(box2);
	    
	    GroupPanel.add(box3);
		
		
//		GroupLayout layout = new GroupLayout(GroupPanel);
//		GroupPanel.setLayout(layout);
//		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
//		
//		
//		layout.setHorizontalGroup(
//				   layout.createSequentialGroup()
//				   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//						   .addGroup(layout.createSequentialGroup()
//								   .addComponent(graphik2ComboBox1, GroupLayout.PREFERRED_SIZE, 225, 300)
//								   .addComponent(graphik2ComboBox2, GroupLayout.PREFERRED_SIZE, 225, 300))
//				           .addComponent(pathLabel))
//				   .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//				   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//				           .addComponent(graphik3ComboBox1, GroupLayout.PREFERRED_SIZE, 225, 300)
//				           .addGroup(layout.createSequentialGroup()
//								   .addComponent(speichernButton)
//								   .addComponent(druckenButton)))     
//				   .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//				   .addComponent(datumLabel)
//				);
//				layout.setVerticalGroup(
//				   layout.createSequentialGroup()
//				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//				           .addComponent(graphik2ComboBox1,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
//				           .addComponent(graphik2ComboBox2,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
//				           .addComponent(graphik3ComboBox1,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
//				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//				           .addComponent(pathLabel,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
//				           .addComponent(speichernButton,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
//				           .addComponent(druckenButton)
//				           .addComponent(datumLabel))
//				);

//        GroupLayout chartBremoPanelLayout = new GroupLayout(chartBremoPanel);
//        chartBremoPanel.setLayout(chartBremoPanelLayout);
//        chartBremoPanelLayout.setHorizontalGroup(
//            chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(graphik2ComboBox1, GroupLayout.PREFERRED_SIZE, 178, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addComponent(graphik2ComboBox2, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE)
//                .addGap(18, 18, 18)
//                .addComponent(graphik3ComboBox1, GroupLayout.PREFERRED_SIZE, 234, GroupLayout.PREFERRED_SIZE)
//                .addGap(1203, 1203, 1203))
//            .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                        .addGap(477, 477, 477)
//                        .addComponent(TitelLabel, GroupLayout.PREFERRED_SIZE, 58, Short.MAX_VALUE))
//                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                        .addContainerGap()
//                        .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                            .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                                .addComponent(druckverlaufPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(graphik1Panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(tabelle_Post_File, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
//                            .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                                        .addComponent(pathLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addGap(81, 81, 81)
//                                        .addComponent(speichernButton)
//                                        .addGap(186, 186, 186)
//                                        .addComponent(druckenButton)
//                                        .addGap(81, 81, 81))
//                                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
//                                        .addComponent(graphik2Panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)
//                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                        .addComponent(graphik3Panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
//                                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                    .addComponent(datumLabel, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
//                                    .addComponent(tabelle_Input_File, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
//                .addContainerGap())
//        );
//
//        chartBremoPanelLayout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {graphik2ComboBox1, graphik2ComboBox2, graphik3ComboBox1});
//
//        chartBremoPanelLayout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {druckverlaufPanel, graphik1Panel, graphik2Panel, graphik3Panel});
//
//        chartBremoPanelLayout.setVerticalGroup(
//            chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(GroupLayout.Alignment.TRAILING, chartBremoPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(TitelLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                        .addComponent(graphik1Panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
//                        .addComponent(tabelle_Post_File, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
//                    .addComponent(druckverlaufPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(graphik2Panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
//                    .addComponent(graphik3Panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
//                    .addComponent(tabelle_Input_File, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                        .addComponent(graphik3ComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addComponent(graphik2ComboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                    .addComponent(graphik2ComboBox1, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(pathLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                    .addComponent(speichernButton)
//                    .addComponent(druckenButton)
//                    .addComponent(datumLabel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
//                .addGap(93, 93, 93))
//        );

//       chartBremoPanelLayout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {druckverlaufPanel, graphik1Panel, graphik2Panel, graphik3Panel});

//        chartBremoPanelLayout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {graphik2ComboBox1, graphik2ComboBox2, graphik3ComboBox1});

//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addComponent(chartBremoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addGap(0, 0, Short.MAX_VALUE))
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addComponent(chartBremoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addContainerGap())
//        );
        Container pane = getContentPane();
        pane.add(TitelPanel,BorderLayout.PAGE_START);
        pane.add(GraphikPanel,BorderLayout.CENTER);
        pane.add(TabellePanel,BorderLayout.LINE_END);
        pane.add(GroupPanel,BorderLayout.PAGE_END);
        setVisible(true);
        pack();
    }// </editor-fold>

   

	protected void graphik3ComboBox2ActionPerformed(ActionEvent evt) {
		// TODO Auto-generated method stub
		
	}

	protected void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("Andere Diagramm")) {
    		if (is_Verlustteilung_Digramm) {
    			graphik3ComboBox2.removeActionListener(itemAction);
    			GraphikPanel.remove(3);
    			GraphikPanel.add(T_mittel_Verlauf(),3);
    			is_Verlustteilung_Digramm = false;
    			GraphikPanel.revalidate();
    			graphik3ComboBox2.addActionListener(itemAction);
    		}
    	}
    	else if (selected.equals("Verlustteilung BalkenDiagramm")){
    		    if (! is_Verlustteilung_Digramm) {
    		       graphik3ComboBox2.removeActionListener(itemAction);
    		       GraphikPanel.remove(3);
    			   //GraphikPanel.add(BalkenDiagramm(),3);
    		       GraphikPanel.add(new JPanel());
    			   graphik3ComboBox2.removeAllItems();
    			   is_Verlustteilung_Digramm = true;
    			   GraphikPanel.revalidate();
    			   graphik3ComboBox2.addActionListener(itemAction);
    		}
    	}
		
	}

	private void speichernButtonActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    	saveImage();
    }

    private void graphik2ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
        // TODO add your handling code here:
    	String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("p-V-Digramm")) {
    		if (! is_P_V_Diagramm) {
    			graphik2ComboBox2.removeActionListener(itemAction);
    			GraphikPanel.remove(2);
    			GraphikPanel.add(p_V_Diagramm(),2);
    			is_P_V_Diagramm = true;
    			graphik2ComboBox2.removeAllItems();
    			GraphikPanel.revalidate();
    			graphik2ComboBox2.addActionListener(itemAction);
    		}
    	}
    	else if (selected.equals("Andere Diagramm")){
    		    if (is_P_V_Diagramm) {
    		       graphik3ComboBox2.removeActionListener(itemAction);
    		       GraphikPanel.remove(2);
    			   GraphikPanel.add(WaermeStromDichteVerlauf(),2);
    			   is_P_V_Diagramm = false;
    			   GraphikPanel.revalidate();
    			   graphik2ComboBox2.addActionListener(itemAction);
    		}
    	}
    }
    
//    protected void graphik2ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
//		// TODO Auto-generated method stub
//    	String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
//    	
//    	if (selected != null) {
//    		GraphikPanel.remove(2);
//			GraphikPanel.add(Auswahl_Diagramm(selected),2);
//			is_P_V_Diagramm = false;
//			GraphikPanel.revalidate();
//    	}
//	}
    private void druckenButtonActionPerformed(ActionEvent evt){
    	// TODO add your handling code here:
    	MyPrinter pr = new  MyPrinter(chartBremoPanel);
    	pr.setOrientation(0);
    	pr.setLRMargins(150);
    	pr.setTBMargins(150);
    	pr.setFitIntoPage(true);
    	pr.print();
    }
    
    private class ItemAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == graphik2ComboBox2) {
        	  
        	  String selected = graphik2ComboBox2.getSelectedItem().toString();
          	
          	if (selected != null) {
          		GraphikPanel.remove(2);
      			try {
					GraphikPanel.add(Auswahl_Diagramm(selected),2);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
      			is_P_V_Diagramm = false;
      			GraphikPanel.revalidate();
          	}
        	  
          }
          else {
        	  String selected = graphik3ComboBox2.getSelectedItem().toString();
            	
            	if (selected != null) {
            		GraphikPanel.remove(3);
        			try {
  					GraphikPanel.add(Auswahl_Diagramm(selected),3);
  				} catch (IOException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  				}
        			is_Verlustteilung_Digramm = false;
        			GraphikPanel.revalidate();
            	}
        	  
          }
        }               
     }
    
    /**
     * Build Graphic for the Pressure Curve (DruckVerlauf)
     * @param file  InputputFile
     * @return
     * @throws IOException
     */
    private ChartPanel Druckverlauf() throws IOException{
    	
    	BufferedReader in = new BufferedReader(new FileReader(inputfile));
        String zeile = null;
		String zeit_oder_KW = "";
		String [] St = null;
		while((zeile = in.readLine()) != null) {
			zeile=zeile.replaceAll(" ", "");
			zeile=zeile.replaceAll("\t", "");
        	 St = zeile.split(":=");
        	 if (St[0].equals("zeit_oder_KW_Basiert[-]")){
        		 if (St[1].equals("KW")){
        			 zeit_oder_KW = "KW";
        		 }
        		 else {
        			 zeit_oder_KW = "zeit";
        		 }
        	 }
        }
        in.close();
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("p_soll [bar]") ;
        XYSeries serie2 = new XYSeries("p [bar] ") ;
        XYDataset datasetDruckVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[15]));//  KW  p_soll
				serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[17])); // KW  p
			}
			else {
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[15]));//  Zeit p_soll
				serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[17])); // Zeit p
			}
		}
		XYSeriesCollection collectionDruckVerlauf = new XYSeriesCollection();
		collectionDruckVerlauf.addSeries(serie1);
		collectionDruckVerlauf.addSeries(serie2);
		datasetDruckVerlauf = collectionDruckVerlauf;
		ChartPanel chartDruckVerlauf = null;
		if (zeit_oder_KW.equals("KW")){
			//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
			chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf);
		}
		else {
			//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
			chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf);
        }
		br.close();
		
		chartDruckVerlauf.setBorder(BorderFactory.createTitledBorder("Druckverlauf"));
		
		return chartDruckVerlauf;
    	
    }
    
    /**
     * Build Graphic for the dQb and Qb/Qmax  Curve 
     * @param inputfile  InputputFile
     * @return
     * @throws IOException
     */
    private ChartPanel dQb_Qb_Qmax_Verlauf () throws IOException{
    	
    	
       
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("dQb [J/KW]") ;
        XYSeries serie2 = new XYSeries("Qb/Qmax [-]") ;
        XYDataset datasetVerlauf ;
        
        String zeile = null;
		String [] header = null;
		String [] value = null;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[19]));//  KW  dQb
			serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[30])); // KW  Qb/Qmax
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		collectionVerlauf.addSeries(serie2);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
	
		br.close();
		
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("dQb und Qb/Qmax Verlauf"));
		
		return chartVerlauf;
    	
    }
    
    /**
     * Build Graphic for the  p-V-Diagramm
     * @param file  InputputFile
     * @return
     * @throws IOException
     */
    private ChartPanel p_V_Diagramm () throws IOException{
    	
    	
       
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [m^3]  Y -> [log(p_soll)]") ;
        XYDataset datasetVerlauf ;
        
        String zeile = null;
		String [] header = null;
		String [] value = null;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			serie1.add(Double.parseDouble(value[2]),Math.log(Double.parseDouble(value[15])));//  Brennraumvolumen  log(p_soll)
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
	
		br.close();
		
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm"));
		
		return chartVerlauf;
    	
    }
    /**
     * Build Graphic for the WaermeStromDichte Curve
     * @param file  InputputFile
     * @return
     * @throws IOException
     */
    private ChartPanel WaermeStromDichteVerlauf() throws IOException{
    	
    	BufferedReader in = new BufferedReader(new FileReader(inputfile));
        String zeile = null;
		String zeit_oder_KW = "";
		String [] St = null;
		while((zeile = in.readLine()) != null) {
			zeile=zeile.replaceAll(" ", "");
			zeile=zeile.replaceAll("\t", "");
        	 St = zeile.split(":=");
        	 if (St[0].equals("zeit_oder_KW_Basiert[-]")){
        		 if (St[1].equals("KW")){
        			 zeit_oder_KW = "KW";
        		 }
        		 else {
        			 zeit_oder_KW = "zeit";
        		 }
        	 }
        }
        in.close();
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("WaermeStromDichte [MW/m^2]") ;
        XYDataset dataseVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			addItemToComboBox(graphik2ComboBox2, header);
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				serie1.setKey("WaermeStromDichte [MW/m^2]   X -> [°KW]");
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[33]));//  KW  WaermeStromDichte
			}
			else {
				serie1.setKey("WaermeStromDichte [MW/m^2]   X -> [s n. Rechenbeginn]");
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[33]));//  Zeit WaermeStromDichte
			}
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		dataseVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
		if (zeit_oder_KW.equals("KW")){
			//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, null, null , dataseVerlauf);
		}
		else {
			//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, null, null , dataseVerlauf);
        }
		br.close();
		
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("WaermeStromDichte verlauf"));
		
		return chartVerlauf;
    	
    }
    
    /**
     * Build Graphic for the T_mittel Curve 
     * @return
     * @throws IOException
     */
    private ChartPanel T_mittel_Verlauf() throws IOException{
    	
    	BufferedReader in = new BufferedReader(new FileReader(inputfile));
        String zeile = null;
		String zeit_oder_KW = "";
		String [] St = null;
		while((zeile = in.readLine()) != null) {
			zeile=zeile.replaceAll(" ", "");
			zeile=zeile.replaceAll("\t", "");
        	 St = zeile.split(":=");
        	 if (St[0].equals("zeit_oder_KW_Basiert[-]")){
        		 if (St[1].equals("KW")){
        			 zeit_oder_KW = "KW";
        		 }
        		 else {
        			 zeit_oder_KW = "zeit";
        		 }
        	 }
        }
        in.close();
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("T_mittel [K]") ;
        XYDataset datasetVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			addItemToComboBox(graphik3ComboBox2, header);
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				serie1.setKey("T_mittel [K]  X -> [°KW]");
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[9]));//  KW  T_mittel
			}
			else {
				serie1.setKey("T_mittel [K]  X -> [s n. Rechenbeginn]");
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[9]));//  Zeit T_mittel
			}
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
		if (zeit_oder_KW.equals("KW")){
			//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
		}
		else {
			//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
        }
		br.close();
		
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("T_mittel verlauf"));
		
		return chartVerlauf;
    	
    }
    
    /**
     * Build Graphic for the choice  Curve
     * @return
     * @throws IOException
     */
    private ChartPanel Auswahl_Diagramm(String selected) throws IOException{
    	
    	BufferedReader in = new BufferedReader(new FileReader(inputfile));
        String zeile = null;
		String zeit_oder_KW = "";
		String [] St = null;
		while((zeile = in.readLine()) != null) {
			zeile=zeile.replaceAll(" ", "");
			zeile=zeile.replaceAll("\t", "");
        	 St = zeile.split(":=");
        	 if (St[0].equals("zeit_oder_KW_Basiert[-]")){
        		 if (St[1].equals("KW")){
        			 zeit_oder_KW = "KW";
        		 }
        		 else {
        			 zeit_oder_KW = "zeit";
        		 }
        	 }
        }
        in.close();
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries(selected) ;
        XYDataset datasetVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		int index = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals(selected)){
					index = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				serie1.setKey(selected +"  X -> [°KW]");
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index]));//  KW  T_mittel
			}
			else {
				serie1.setKey(selected+"  X -> [s n. Rechenbeginn]");
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index]));//  Zeit T_mittel
			}
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
		if (zeit_oder_KW.equals("KW")){
			//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
		}
		else {
			//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
        }
		br.close();
		selected = selected.substring(0, selected.indexOf("["));
		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected+" verlauf"));
		
		return chartVerlauf;
    	
    }
    /**
     * Build the Table for Post File 
     * @param file
     * @return Label as Table
     * @throws IOException
     */
    public JLabel TabellePostFIle() throws IOException {
    	
    	String parent = inputfile.getParent();
    	
    	BufferedReader in = new BufferedReader(new FileReader(parent+"/DVA_Post_"+inputfile.getName()));
		String zeile = null;
		String [] header1 = null;
		String [] header2 = null;
		if ((zeile = in.readLine()) != null)
			header1 = zeile.split("\t");
		if ((zeile = in.readLine()) != null)
			header2 = zeile.split("\t");
		in.close();
		
		String tabelle ="<html>"+
				        "<TABLE BORDER>"+
				        "<TR>"+
		                "<TH colspan=\"2\">Tabelle Post File</TH>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH>"+header1[0] +"</TH>"+
		                "<TD>"+header2[0] +"</TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH>"+header1[1] +"</TH>"+
	   	                "<TD>"+header2[1] +"</TD>"+
	   	                "</TR>"+
	   	                "<TR>"+
		                "<TH>"+header1[6] +"</TH>"+
		                "<TD>"+header2[6] +"</TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH>"+header1[8] +"</TH>"+
		                "<TD>"+header2[8] +"</TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH>"+header1[10] +"</TH>"+
		                "<TD>"+header2[10] +"</TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH>"+header1[3] +"</TH>"+
		                "<TD>"+header2[3] +"</TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH>"+header1[22] +"</TH>"+
		                "<TD>"+header2[22] +"</TD>"+
		                "</TR>"+
		                "</TABLE>"+
		                "</html>";
		
		JLabel post = new JLabel(tabelle);
		//post.setText(tabelle);
		
		//System.err.println(tabelle);
		
		//post.setText("le voici ici");
		return post ;
			
	}
    
    public JLabel TabelleInputFile () throws ParameterFileWrongInputException, IOException  {
    	
    //	CasePara cp = new CasePara(file);
    	BufferedReader in = new BufferedReader(new FileReader(inputfile.getPath()));
		String zeile = null;
		String [] header1 = null;
		String [] header2 = new  String [10];
		while((zeile = in.readLine()) != null) {
			zeile=zeile.replaceAll(" ", "");
			zeile=zeile.replaceAll("\t", "");
			header1 = zeile.split(":=");
			if (header1[0].equals("Drehzahl[min^-1]")){
				header2[0]=header1[0].replace("[", " [");
				header2[1]=header1[1];
				
			}
			if (header1[0].equals("BOI[KWnZOT]")){
				header2[2]=header1[0].replace("[", " [");
				header2[3]=header1[1];
			}
			if (header1[0].equals("BOI_1[KWnZOT]")){
				header2[2]=header1[0].replace("[", " [");
				header2[3]=header1[1];
			}
			if (header1[0].equals("EOI[KWnZOT]")){
				header2[4]=header1[0].replace("[", " [");
				header2[5]=header1[1];
			}
			if (header1[0].equals("EOI_1[KWnZOT]")){
				header2[4]=header1[0].replace("[", " [");
				header2[5]=header1[1];
			}
			if (header1[0].equals("BOI_2[KWnZOT]")){
				header2[6]=header1[0].replace("[", " [");
				header2[7]=header1[1];
			}
			if (header1[0].equals("EOI_2[KWnZOT]")){
				header2[8]=header1[0].replace("[", " [");
				header2[9]=header1[1];
		    }
		}
		
		in.close();
    	
    	String tabelle ="<html>"+
		        "<TABLE BORDER>"+
		        "<TR>"+
                "<TH colspan=\"2\">Tabelle Input File</TH>"+
                "</TR>"+
                "<TR>"+
                "<TH>"+header2[0]+"</TH>"+
                "<TD>"+header2[1]+"</TD>"+
                "</TR>";
                
                for (int i = 2 ;i < header2.length;){
                	if (header2[i]!= null){
                		tabelle = tabelle + "<TR>"+
                                            "<TH>"+header2[i]+"</TH>"+
                	                        "<TD>"+header2[i+1]+"</TD>"+
                	                        "</TR>";
                	}
                	i = i+2;
                }
                
      tabelle = tabelle +
                "</TABLE>"+
                "</html>";

      JLabel input = new JLabel(tabelle);

      return input;	
	
   }
/**
 * zeichen Chart auf Panel mit folgenden Parameter   
 * @param Titel
 * @param XLabel
 * @param YLabel
 * @param data      set von Daten zum zeichnen 
 * @return
 */
private ChartPanel createChartPanel(String Titel, String XLabel,String YLabel, XYDataset data ) {
				
		JFreeChart chart = ChartFactory.createXYLineChart(
				Titel, XLabel , YLabel , data,
				PlotOrientation.VERTICAL, true, true, false);
		
		//chart.setTitle(new TextTitle("XY Chart Sample, non default font", new java.awt.Font("Serif", Font.BOLD, 12)));
//		chart.setBackgroundPaint(Color.white);
//		chart.setBorderPaint(Color.black);
//		chart.setBorderStroke(new BasicStroke(1));
//		chart.setBorderVisible(true);
		//chart.removeLegend();
		//writeChartToPDF(chart, 600, 400, "src/bremoGraphik/pdf/Verlauf von "+Titel+".pdf");
		
		final ChartPanel chartPanel = new ChartPanel(chart);
		// chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

/**
 * füge elemente von Array to JComboBox als Item hinzu. Nur für Spalte
 * ComboBox
 */
public void addItemToComboBox(JComboBox cb, String[] item) {

	cb.removeAllItems();
	// cb.addItem("");

	for (int i = 0; i < item.length; i++) {

		cb.addItem(item[i]);

	}

}

private void saveImage() {
	Container c = getContentPane();
	BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
	c.paint(im.getGraphics());
	 
    try {
        ImageIO.write(im, "png", new File(inputfile.getParent()+"/panel.png"));
       // System.err.println("panel saved as image");
        JOptionPane.showMessageDialog(this,
        		"panel saved as image", "Save",
				JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
       // System.err.println("panel not saved " + e.getMessage());
        JOptionPane.showMessageDialog(this,
        		"panel not Saved ", "Save",
				JOptionPane.WARNING_MESSAGE);

    }
}


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(bremoGraphik.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(bremoGraphik.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(bremoGraphik.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(bremoGraphik.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                new bremoGraphik().setVisible(true);
//            }
//        });
    }
    // Variables declaration - do not modify
    private JPanel chartBremoPanel;
    private JLabel TitelLabel;
    private JLabel datumLabel;
    private JButton druckenButton;
    private JPanel druckverlaufPanel;
    private JPanel graphik1Panel;
    private JComboBox graphik2ComboBox1;
    private JComboBox graphik2ComboBox2;
    private JPanel graphik2Panel;
    private JComboBox graphik3ComboBox1;
    private JComboBox graphik3ComboBox2;
    private JPanel graphik3Panel;
    private JLabel pathLabel;
    private JButton speichernButton;
    private JPanel tabelle_Input_File;
    private JPanel tabelle_Post_File;
    private File  inputfile ;
    private JPanel TitelPanel;
    private JPanel GraphikPanel;
    private JPanel GroupPanel;
    private JPanel TabellePanel;
    private boolean is_P_V_Diagramm;
    private boolean is_Verlustteilung_Digramm;
    private ItemAction itemAction ;
    // End of variables declaration
}
