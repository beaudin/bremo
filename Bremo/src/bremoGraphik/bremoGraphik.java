/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bremoGraphik;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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
    	if (file == null ) throw new FileNotFoundException();
        
    	initComponents(file);
    }

	/**
     * This method is called from within the constructor to initialize the form.
	 * @throws IOException 
	 * @throws ParameterFileWrongInputException 
     */
    @SuppressWarnings("unchecked")
   
    private void initComponents(File file) throws IOException, ParameterFileWrongInputException {

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
        graphik2ComboBox1 = new JComboBox();
        graphik2ComboBox2 = new JComboBox();
        inputfile = file;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new java.awt.Dimension(800, 800));
        
        chartBremoPanel.setSize(1280, 800);
        //chartBremoPanel.setBackground(Color.white);

        TitelLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        TitelLabel.setText(file.getName());
        Image image = new ImageIcon(getClass().getResource("/bremoswing/bild/bremo2.png")).getImage();
        Icon  icon = new ImageIcon(image);
        //TitelLabel.setIcon(icon);
        
        BufferedReader in = new BufferedReader(new FileReader(file));
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
        BufferedReader br = new  BufferedReader(new FileReader(file.getParent()+"/DVA_"+file.getName()));
        XYSeries serie1 = new XYSeries("p_soll") ;
        XYSeries serie2 = new XYSeries("p") ;
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
			//chartDruckVerlauf = createChartPanel(null, "[°KW]","[bar]" , datasetDruckVerlauf);
			chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf);
		}
		else {
			//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]","[bar]" , datasetDruckVerlauf);
			chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf);
        }
		br.close();
        
        druckverlaufPanel.setBorder(BorderFactory.createTitledBorder(null, "Druckverlauf", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
//        druckverlaufPanel.setSize(460, 270);
//        druckverlaufPanel.setLayout(new GridLayout(1, 1));
//        druckverlaufPanel.add(chartDruckVerlauf);
//        
        GroupLayout druckverlaufPanelLayout = new GroupLayout(druckverlaufPanel);
        druckverlaufPanel.setLayout(druckverlaufPanelLayout);
        druckverlaufPanelLayout.setHorizontalGroup(
            druckverlaufPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
            .addComponent(chartDruckVerlauf, 460, 460, 460)           
        );
        druckverlaufPanelLayout.setVerticalGroup(
            druckverlaufPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
            .addComponent(chartDruckVerlauf, 270, 270, 270)
        );
                                                                                                                                                                                                                                               
        graphik1Panel.setBorder(BorderFactory.createTitledBorder(null, "Graphik 1", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12)));

        GroupLayout graphik1PanelLayout = new GroupLayout(graphik1Panel);
        graphik1Panel.setLayout(graphik1PanelLayout);
        graphik1PanelLayout.setHorizontalGroup(
            graphik1PanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );
        graphik1PanelLayout.setVerticalGroup(
            graphik1PanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );

        graphik2Panel.setBorder(BorderFactory.createTitledBorder(null, "Graphik 2", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12)));

        GroupLayout graphik2PanelLayout = new GroupLayout(graphik2Panel);
        graphik2Panel.setLayout(graphik2PanelLayout);
        graphik2PanelLayout.setHorizontalGroup(
            graphik2PanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );
        graphik2PanelLayout.setVerticalGroup(
            graphik2PanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );

        graphik3Panel.setBorder(BorderFactory.createTitledBorder(null, "Graphik 3", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12)));
        graphik3Panel.setPreferredSize(new java.awt.Dimension(480, 300));

        GroupLayout graphik3PanelLayout = new GroupLayout(graphik3Panel);
        graphik3Panel.setLayout(graphik3PanelLayout);
        graphik3PanelLayout.setHorizontalGroup(
            graphik3PanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );
        graphik3PanelLayout.setVerticalGroup(
            graphik3PanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );

        tabelle_Post_File.setBorder(BorderFactory.createTitledBorder("Tabelle Post File"));
      //tabelle_Post_File.setPreferredSize(new java.awt.Dimension(250, 300));
        tabelle_Post_File.setRequestFocusEnabled(false);
        tabelle_Post_File.setVerifyInputWhenFocusTarget(false);
        tabelle_Post_File.setLayout(new GridLayout(1, 1));
        tabelle_Post_File.add(TabellePostFIle(file));
        
        

//        GroupLayout tabelle_Post_FileLayout = new GroupLayout(tabelle_Post_File);
//        tabelle_Post_File.setLayout(tabelle_Post_FileLayout);
//        tabelle_Post_FileLayout.setHorizontalGroup(
//            tabelle_Post_FileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGap(0, 240, Short.MAX_VALUE)
//        );
//        tabelle_Post_FileLayout.setVerticalGroup(
//            tabelle_Post_FileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGap(0, 270, Short.MAX_VALUE)
//        );

        tabelle_Input_File.setBorder(BorderFactory.createTitledBorder("Tabelle Input File"));
        tabelle_Input_File.setLayout(new GridLayout(1, 1));
        tabelle_Input_File.add(TabelleInputFile(file));

//        GroupLayout tabelle_Input_FileLayout = new GroupLayout(tabelle_Input_File);
//        tabelle_Input_File.setLayout(tabelle_Input_FileLayout);
//        tabelle_Input_FileLayout.setHorizontalGroup(
//            tabelle_Input_FileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGap(0, 240, Short.MAX_VALUE)
//        );
//        tabelle_Input_FileLayout.setVerticalGroup(
//            tabelle_Input_FileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGap(0, 270, Short.MAX_VALUE)
//        );

        datumLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        datumLabel.setText(df.format( dt ));
        druckenButton.setText("Drucken");
        druckenButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                druckenButtonActionPerformed(evt);
            }
        });
        speichernButton.setText("Speichern");
        speichernButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speichernButtonActionPerformed(evt);
            }
        });

        pathLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        pathLabel.setText(file.getParent());

        graphik3ComboBox1.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        graphik2ComboBox1.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        graphik2ComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphik2ComboBox1ActionPerformed(evt);
            }
        });

        graphik2ComboBox2.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        GroupLayout chartBremoPanelLayout = new GroupLayout(chartBremoPanel);
        chartBremoPanel.setLayout(chartBremoPanelLayout);
        chartBremoPanelLayout.setHorizontalGroup(
            chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(chartBremoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(graphik2ComboBox1, GroupLayout.PREFERRED_SIZE, 178, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(graphik2ComboBox2, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(graphik3ComboBox1, GroupLayout.PREFERRED_SIZE, 234, GroupLayout.PREFERRED_SIZE)
                .addGap(1203, 1203, 1203))
            .addGroup(chartBremoPanelLayout.createSequentialGroup()
                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
                        .addGap(477, 477, 477)
                        .addComponent(TitelLabel, GroupLayout.PREFERRED_SIZE, 58, 800))
                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addGroup(chartBremoPanelLayout.createSequentialGroup()
                                .addComponent(druckverlaufPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(graphik1Panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tabelle_Post_File, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(chartBremoPanelLayout.createSequentialGroup()
                                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
                                        .addComponent(pathLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(81, 81, 81)
                                        .addComponent(speichernButton)
                                        .addGap(186, 186, 186)
                                        .addComponent(druckenButton)
                                        .addGap(81, 81, 81))
                                    .addGroup(chartBremoPanelLayout.createSequentialGroup()
                                        .addComponent(graphik2Panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(graphik3Panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(datumLabel, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tabelle_Input_File, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );

        chartBremoPanelLayout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {graphik2ComboBox1, graphik2ComboBox2, graphik3ComboBox1});

        chartBremoPanelLayout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {druckverlaufPanel, graphik1Panel, graphik2Panel, graphik3Panel});

        chartBremoPanelLayout.setVerticalGroup(
            chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, chartBremoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TitelLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(graphik1Panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addComponent(tabelle_Post_File, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                    .addComponent(druckverlaufPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(graphik2Panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                    .addComponent(graphik3Panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                    .addComponent(tabelle_Input_File, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(graphik3ComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(graphik2ComboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(graphik2ComboBox1, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(chartBremoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speichernButton)
                    .addComponent(druckenButton)
                    .addComponent(datumLabel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
                .addGap(93, 93, 93))
        );

        chartBremoPanelLayout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {druckverlaufPanel, graphik1Panel, graphik2Panel, graphik3Panel});

        chartBremoPanelLayout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {graphik2ComboBox1, graphik2ComboBox2, graphik3ComboBox1});

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chartBremoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chartBremoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>

    private void speichernButtonActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    	saveImage(chartBremoPanel);
    }

    private void graphik2ComboBox1ActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    }
    
    private void druckenButtonActionPerformed( ActionEvent evt){
    	// TODO add your handling code here:
    	MyPrinter pr = new  MyPrinter(chartBremoPanel);
    	pr.setOrientation(0);
    	pr.setLRMargins(150);
    	pr.setTBMargins(150);
    	pr.setFitIntoPage(true);
    	pr.print();
    }
    
    /**
     * Build the Table for Post File 
     * @param file
     * @return Label as Table
     * @throws IOException
     */
    public JLabel TabellePostFIle(File file) throws IOException {
    	
    	String parent = file.getParent();
    	
    	BufferedReader in = new BufferedReader(new FileReader(parent+"/DVA_Post_"+file.getName()));
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
		                "<TR>"+
		                "<TH></TH>"+
		                "<TD></TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH></TH>"+
		                "<TD></TD>"+
		                "</TR>"+
		                "<TR>"+
		                "<TH></TH>"+
		                "<TD></TD>"+
		                "</TR>"+
		                "</TABLE>"+
		                "</html>";
		
		JLabel post = new JLabel(tabelle);
		//post.setText(tabelle);
		
		//System.err.println(tabelle);
		
		//post.setText("le voici ici");
		return post ;
			
	}
    
    public JLabel TabelleInputFile (File file) throws ParameterFileWrongInputException, IOException  {
    	
    //	CasePara cp = new CasePara(file);
    	BufferedReader in = new BufferedReader(new FileReader(file.getPath()));
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
                
     tabelle = tabelle + "<TR>"+
                "<TH></TH>"+
                "<TD></TD>"+
                "</TR>"+
                "<TR>"+
                "<TH></TH>"+
                "<TD></TD>"+
                "</TR>"+
                "<TR>"+
                "<TH></TH>"+
                "<TD></TD>"+
                "</TR>"+
                "<TR>"+
                "<TH></TH>"+
                "<TD></TD>"+
                "</TR>"+
                "<TR>"+
                "<TH></TH>"+
                "<TD></TD>"+
                "</TR>"+
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

private void saveImage(JPanel panel) {
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
    private JPanel graphik3Panel;
    private JLabel pathLabel;
    private JButton speichernButton;
    private JPanel tabelle_Input_File;
    private JPanel tabelle_Post_File;
    private File  inputfile ;
    // End of variables declaration
}
