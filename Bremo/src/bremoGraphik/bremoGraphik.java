/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bremoGraphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
//import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import bremoExceptions.ParameterFileWrongInputException;

import bremoswing.util.PdfFilePrinting;

/**
 *
 * @author Ngueneko Steve
 */
public class bremoGraphik extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
   
    private void initComponents(File file) throws IOException, ParameterFileWrongInputException {
        
    	/**  initialization of Variable ***********************************************/
    	TitelPanel = new JPanel();
    	GraphikPanel = new JPanel();
    	TabellePanel = new JPanel();
    	GroupPanel  = new JPanel();
        new JPanel();
        TitelLabel = new JLabel();
        new JPanel();
        graphik1Panel = new JPanel();
        new JPanel();
        new JPanel();
        new JPanel();
        new JPanel();
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
        is_verlust_berechnen = false;
        berechnungModell = null;
        /********************************************************************************/
        
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension (1280,800));
        setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo2.png")).getImage());
       setResizable(false);
        
        /** Size and Border of All Panel of the Frame************************************/
        
        TitelPanel.setPreferredSize(new Dimension(1280,50));
        TitelPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GraphikPanel.setPreferredSize(new Dimension (1010,675));
        GraphikPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        TabellePanel.setPreferredSize(new Dimension(270,675));
        TabellePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GroupPanel.setPreferredSize(new Dimension (1280,75));
        GroupPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        TitelPanel.setOpaque(false);
        graphik1Panel.setOpaque(false);
        TabellePanel.setOpaque(false);
        GroupPanel.setOpaque(false);
//        Color Transparent = new Color (0,0,255,155);
//        TitelPanel.setBackground(Transparent);
//        GraphikPanel.setBackground(Transparent);
//        TabellePanel.setBackground(Transparent);
//        GroupPanel.setBackground(Transparent);
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
        
//        GridLayout TabellePanelLayout = new GridLayout();
//        TabellePanelLayout.setColumns(1);
//        TabellePanelLayout.setRows(3);
//        TabellePanel.setLayout(TabellePanelLayout);
        Tabelle_InLabel = TabelleInputFile();
        Tabelle_PostLabel = TabellePostFIle();
		TabellePanel.add(Tabelle_PostLabel);
		TabellePanel.add(Tabelle_InLabel);
        Box b1 = Box.createHorizontalBox();
        b1.add(Box.createHorizontalStrut(7));
        b1.add(Tabelle_PostLabel);
        
        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalStrut(5));
        b2.add(Tabelle_InLabel);
        
        Box b3 = Box.createVerticalBox();
        b3.add(Box.createVerticalStrut(10));
        b3.add(b1);
        b3.add(Box.createVerticalStrut(10));
        b3.add(b2);
        
        TabellePanel.add(b3);
        
        /********************************************************************************/
        
		/**  GraphikPanel Processing  **************************************************/
        
        GraphikPanel.add(Druckverlauf());
		GraphikPanel.add(dQb_Qb_Qmax_Verlauf());
		//GraphikPanel.add(WaermeStromDichteVerlauf());
		GraphikPanel.add(new JPanel());
		is_P_V_Diagramm = false;
		//GraphikPanel.add(T_mittel_Verlauf());
		GraphikPanel.add(new JPanel());
		is_Verlustteilung_Digramm = false;
		/*******************************************************************************/
		String str = "<html><b>\" Bitte Wählen : \"</b></html>";
		graphik2ComboBox1.addItem(str);
		graphik2ComboBox1.addItem("p-V-Digramm");
		graphik2ComboBox1.addItem("Andere Diagramm");
		graphik2ComboBox1.setPreferredSize(new java.awt.Dimension(240, 30));
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

		graphik2ComboBox2.setPreferredSize(new java.awt.Dimension(240, 30));
		graphik2ComboBox2.addActionListener(itemAction);
		
		graphik3ComboBox1.addItem(str);
		graphik3ComboBox1.addItem("Andere Diagramm");
		graphik3ComboBox1.addItem("Verlustteilung BalkenDiagramm");
		graphik3ComboBox1.setPreferredSize(new java.awt.Dimension(240, 30));
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
		
		graphik3ComboBox2.setPreferredSize(new java.awt.Dimension(240, 30));
		graphik3ComboBox2.addActionListener(itemAction);
		
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
		
//	    Box box1 = Box.createHorizontalBox();
//	    box1.add(graphik2ComboBox1);
//	    box1.add(graphik2ComboBox2);
//	    box1.add(Box.createHorizontalStrut(110));
//	    box1.add(graphik3ComboBox1);
//	    box1.add(graphik3ComboBox2);
//	    box1.add(Box.createHorizontalStrut(290));
//	    
//	    Box box2 = Box.createHorizontalBox();
//	    box2.add(pathLabel);
//	    box2.add(Box.createHorizontalStrut(255));  
//	    box2.add(speichernButton);
//	    box2.add(Box.createHorizontalStrut(125));
//	    box2.add(druckenButton);
//	    box2.add(Box.createHorizontalStrut(155));
//	    box2.add(datumLabel);
//	    
//	    Box box3 = Box.createVerticalBox();
//	    box3.add(box1);
//	    box3.add(Box.createVerticalStrut(8));
//	    box3.add(box2);
//	    
//	    GroupPanel.add(box3);
		
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(GroupPanel);
        GroupPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pathLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(graphik2ComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, 320)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(graphik2ComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, 320)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(graphik3ComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, 320)
                    .addComponent(speichernButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(graphik3ComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, 320)
                    .addComponent(druckenButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                .addComponent(datumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(graphik2ComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(graphik2ComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(graphik3ComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(graphik3ComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(speichernButton)
                    .addComponent(druckenButton)
                    .addComponent(datumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
		
		
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
  
    }

   

//	protected void graphik3ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
//		// TODO Auto-generated method stub
//		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
//		if (selected.equals("pmi-Werte")) {
//			
//		}
//		else if (selected.equals("Wirkungsgrade")) {
//			
//		}
//		if (selected.equals("pmi-Werte")|| selected.equals("Wirkungsgrade")) {
//    		GraphikPanel.remove(3);
//			GraphikPanel.add(Verlustteilung(selected),3);
//			is_P_V_Diagramm = false;
//			GraphikPanel.revalidate();
//    	}
//	}

	protected void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("Andere Diagramm")) {
    		if (is_Verlustteilung_Digramm) {
    			graphik3ComboBox2.removeActionListener(itemAction);
    			graphik3ComboBox2.removeAllItems();
    			GraphikPanel.remove(3);
    			GraphikPanel.add(T_mittel_Verlauf(),3);
    			graphik3ComboBox2.addActionListener(itemAction);
    			GraphikPanel.revalidate();
    			
    		}
    	}
    	else if (selected.equals("Verlustteilung BalkenDiagramm")){
    		    if (! is_Verlustteilung_Digramm) {
    		    	if (! is_verlust_berechnen) {
    		       graphik3ComboBox2.removeActionListener(itemAction);
    		       GraphikPanel.remove(3);
    		       URL url = getClass().getResource("/bremoswing/bild/balkenErrorIcon.png");
    			   ImageIcon icon = new ImageIcon(url);
    		      JLabel label =  new JLabel("Graphik nicht vorhanden !",icon, SwingConstants.LEFT);
    		      label.setFont(new java.awt.Font("Tahoma", 0, 16));
    		      label.setForeground(new Color(255,0,0));
    		      label.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
    		      JPanel panel = new JPanel();
    		      panel.add(BorderLayout.CENTER,label);
    		      panel.setBorder(BorderFactory.createTitledBorder("Verlustteilung Digramm"));
    		      GraphikPanel.add(panel,3);
    		       //GraphikPanel.add(new JPanel());
    			   graphik3ComboBox2.removeAllItems();
    			   is_Verlustteilung_Digramm = true;
    			   GraphikPanel.revalidate();
    			  // graphik3ComboBox2.addActionListener(itemAction);
    		    }
    		    else {
    		    		graphik3ComboBox2.removeActionListener(itemAction);
    		    		//String [] item = new  String [] {"pmi-Werte","Wirkungsgrade"};
    		    		graphik3ComboBox2.removeAllItems();
                        graphik3ComboBox2.addItem("pmi-Werte");
                        graphik3ComboBox2.addItem("Wirkungsgrade");
    		    		is_Verlustteilung_Digramm = true;
    		    		graphik3ComboBox2.addActionListener(itemAction);
    		    		GraphikPanel.revalidate();
//    		    		graphik3ComboBox2.removeActionListener(itemAction);
//    	    		    GraphikPanel.remove(3);
//    	    		       URL url = getClass().getResource("/bremoswing/bild/balkenErrorIcon.png");
//    	    			   ImageIcon icon = new ImageIcon(url);
//    	    		      JLabel label =  new JLabel("Graphik nicht vorhanden !",icon, SwingConstants.LEFT);
//    	    		      label.setFont(new java.awt.Font("Tahoma", 0, 16));
//    	    		      label.setForeground(new Color(255,0,0));
//    	    		      JPanel panel = new JPanel();
//    	    		      
//    	    			  GraphikPanel.add(new JPanel().add(BorderLayout.CENTER,label),3);
//    	    		       //GraphikPanel.add(new JPanel());
//    	    			   graphik3ComboBox2.removeAllItems();
//    	    			   is_Verlustteilung_Digramm = true;
//    	    			   GraphikPanel.revalidate();
//    	    			  // graphik3ComboBox2.addActionListener(itemAction);
    		    		
    		    	}
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
    			graphik2ComboBox2.removeAllItems();
    			GraphikPanel.remove(2);
    			GraphikPanel.add(p_V_Diagramm(),2);
    			is_P_V_Diagramm = true;
    			GraphikPanel.revalidate();
    			//graphik2ComboBox2.addActionListener(itemAction);
    		}
    	}
    	else if (selected.equals("Andere Diagramm")){
    		   if (is_P_V_Diagramm) {
    		       graphik3ComboBox2.removeActionListener(itemAction);
    		       graphik2ComboBox2.removeAllItems();
    		       GraphikPanel.remove(2);
    			   GraphikPanel.add(WaermeStromDichteVerlauf(),2);
    			   is_P_V_Diagramm = false;
    			   graphik2ComboBox2.addActionListener(itemAction);
    			   GraphikPanel.revalidate();
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
//    	MyPrinter pr = new  MyPrinter((JPanel) getContentPane());
//    	pr.setOrientation(0);
//    	pr.setLRMargins(150);
//    	pr.setTBMargins(150);
//    	pr.setFitIntoPage(true);
//    	pr.print();
    	int j = GraphikPanel.getComponentCount();
    	Object [] Graphik_Chart = new Object [j];
    	for (int i = 0 ; i < Graphik_Chart.length; i++) {
    		Graphik_Chart[i] =  GraphikPanel.getComponent(i);
    		try {
    		  Graphik_Chart[i] = ((ChartPanel)Graphik_Chart[i]).getChart();
    		}catch (ClassCastException e) {
    			Graphik_Chart[i] = null;
    		}
    	}
    ChartPanel	chartpanel = (ChartPanel) GraphikPanel.getComponent(0);
    chartpanel.getChart();
    writeChartToPDF(Graphik_Chart, 320, 230, "src/bremoGraphik/pdf/Graphik"+TitelLabel.getText()+".pdf");
    }
    
    public class ItemAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == graphik2ComboBox2) {
        	  
        	  String selected = null;
        	  try{
        		  
        	  selected = graphik2ComboBox2.getSelectedItem().toString();
        	  
        	  } catch (NullPointerException npe) {
        		  
        	  }
          	
          	if (selected != null) {
          		GraphikPanel.remove(2);
      			try {
					GraphikPanel.add(Auswahl_Diagramm(selected),2);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
      			GraphikPanel.revalidate();
          	}
        	  
          }
          else {
        	  String selected = graphik3ComboBox2.getSelectedItem().toString();
            	
        	  if (selected.equals("pmi-Werte")|| selected.equals("Wirkungsgrade")) {
          		GraphikPanel.remove(3);
      			try {
					GraphikPanel.add(Verlustteilung(selected),3);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
      			is_P_V_Diagramm = false;
      			GraphikPanel.revalidate();
          	}
        	  
        	  
        	  else if (selected != null) {
            		GraphikPanel.remove(3);
        			try {
  					GraphikPanel.add(Auswahl_Diagramm(selected),3);
  				} catch (IOException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  				}
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
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        XYSeries serie1 = null;
         if (berechnungModell.equals("DVA")){
        	 serie1 =  new XYSeries("p_soll [bar]") ;
         }
        XYSeries serie2 = new XYSeries("p [bar] ") ;
        XYDataset datasetDruckVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		int index_2 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("p_soll [bar]")){
					index_1 = i;
				}
				else if (header[i].equals("p [bar]")){
					index_2 = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				if (berechnungModell.equals("DVA")) {
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  p_soll
				}
				serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
			}
			else {
				if (berechnungModell.equals("DVA")){
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
			    }
				serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
			}
		}
		XYSeriesCollection collectionDruckVerlauf = new XYSeriesCollection();
		if (berechnungModell.equals("DVA")) {
		    collectionDruckVerlauf.addSeries(serie1);
		}
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
    	
    	
       
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("dQb [J/KW]") ;
        XYSeries serie2 = new XYSeries("Qb/Qmax [-]") ;
        XYDataset dQbVerlauf ;
        XYDataset Qb_QmaxVerlauf;
        String zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		int index_2 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("dQb [J/KW]")){
					index_1 = i;
				}
				else if (header[i].equals("Qb/Qmax [-]")){
					index_2 = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  dQb
			serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  Qb/Qmax
		}
		XYSeriesCollection collectionVerlauf1 = new XYSeriesCollection();
		XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
		collectionVerlauf1.addSeries(serie1);
		collectionVerlauf2.addSeries(serie2);
		dQbVerlauf = collectionVerlauf1;
		Qb_QmaxVerlauf = collectionVerlauf2;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , dQbVerlauf, Qb_QmaxVerlauf);
	
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
    	
    	
       
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        //XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [log(m^3)]  Y -> [log(p_soll)]", false) ;
        XYSeries serie1 = null;
        if (berechnungModell.equals("DVA")) {
             serie1 = new XYSeries("p-V-Diagramm (p_soll)", false) ;
        }
        XYSeries serie2 = new XYSeries("p-V-Diagramm (p)", false) ;
        XYDataset datasetVerlauf;
        String zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		int index_2 = -1;
		int index_3 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("Brennraumvolumen [m3]")){
					index_1 = i;
				}
				else if (header[i].equals("p_soll [bar]")){
					index_2 = i;
				}
				else if (header[i].equals("p [bar]")){
					index_3 = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (berechnungModell.equals("DVA")) {
			serie1.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_2])));// log(Brennraumvolumen)  log(p_soll)
			}
			serie2.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_3])));//  log(Brennraumvolumen)  log(p)
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		if (berechnungModell.equals("DVA")) {
		    collectionVerlauf.addSeries(serie1);
		}
		collectionVerlauf.addSeries(serie2);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
	
		br.close();
		if (berechnungModell.equals("DVA")) {
		    chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm P_soll und P"));
		}
		else {
			chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm P"));
		}
		
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
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("WaermeStromDichte [MW/m^2]") ;
        XYDataset dataseVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("Wandwärmestromdichte [MW/m^2]")){
					index_1 = i;
				}
			}
			addItemToComboBox(graphik2ComboBox2, header);
			
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				serie1.setKey("WaermeStromDichte [MW/m^2]   X -> [°KW]");
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  WaermeStromDichte
			}
			else {
				serie1.setKey("WaermeStromDichte [MW/m^2]   X -> [s n. Rechenbeginn]");
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit WaermeStromDichte
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
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        XYSeries serie1 = new XYSeries("T_mittel [K]") ;
        XYDataset datasetVerlauf ;
        
        zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("T_mittel [K]")){
					index_1 = i;
				}
			}
			addItemToComboBox(graphik3ComboBox2, header);
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			if (zeit_oder_KW.equals("KW")) {
				serie1.setKey("T_mittel [K]  X -> [°KW]");
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  T_mittel
			}
			else {
				serie1.setKey("T_mittel [K]  X -> [s n. Rechenbeginn]");
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit T_mittel
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
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
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
    
    private ChartPanel Verlustteilung(String selected) throws IOException{
    	 
    	BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/Verlustteilung-Wirkungsgrade_"+inputfile.getName()));
         //XYSeries serie1 = new XYSeries(selected) ;
         DefaultCategoryDataset datasetVerlauf = new DefaultCategoryDataset();
         
        String  zeile = null;
 		String [] header = null;
 		String [] value = null;
 		//int index = -1;
 		if ((zeile = br.readLine()) != null){
 			header = zeile.split("\t");
 		}
 		
 		while ((zeile = br.readLine()) != null){
 			value = zeile.split(" ");
 			if (value[0].equals(selected)) {
 				for(int i = 1 ;i < value.length; i++){
 					if (!value[i].equals("NaN")){
 					    datasetVerlauf.addValue(Double.parseDouble(value[i]), value[0], header[i]);
 					}
 				}
 			}
 		}
 		String s = "First";
        String s1 = "Second";
        String s2 = "Third";
        String s3 = "Category 1";
        String s4 = "Category 2";
        String s5 = "Category 3";
        String s6 = "Category 4";
        String s7 = "Category 5";
        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
        defaultcategorydataset.addValue(1.0D, s, s3);
        defaultcategorydataset.addValue(4D, s, s4);
        defaultcategorydataset.addValue(3D, s, s5);
        defaultcategorydataset.addValue(5D, s, s6);
        defaultcategorydataset.addValue(5D, s, s7);
        defaultcategorydataset.addValue(5D, s1, s3);
        defaultcategorydataset.addValue(7D, s1, s4);
        defaultcategorydataset.addValue(6D, s1, s5);
        defaultcategorydataset.addValue(8D, s1, s6);
        defaultcategorydataset.addValue(4D, s1, s7);
        defaultcategorydataset.addValue(4D, s2, s3);
        defaultcategorydataset.addValue(3D, s2, s4);
        defaultcategorydataset.addValue(2D, s2, s5);
        defaultcategorydataset.addValue(3D, s2, s6);
        defaultcategorydataset.addValue(6D, s2, s7);
 		
 		ChartPanel chartVerlauf = null;
 		//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
 		chartVerlauf = createBarChartPanel(null, "category", "value" , defaultcategorydataset);
         
 		br.close();
 		
 		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected+" BalkenDiagramm"));
 		
 		return chartVerlauf;
    	
    }
    /**
     * Build the Table for Post File 
     * @param file
     * @return Label as Table
     * @throws IOException
     */
    public static JLabel TabellePostFIle() throws IOException {
    	
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
				        "<table border = \"1\" cellpadding=\"0\"  cellspacing=\"0\" style= \"border-collapse: collapse;\">"+
				        "<tr>"+
		                "<th colspan=\"2\">Tabelle Post File</th>"+
		                "</tr>"+
		                "<tr>"+
		                "<th>"+header1[0] +"</th>"+
		                "<td>"+header2[0] +"</td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th>"+header1[1] +"</th>"+
	   	                "<td>"+header2[1] +"</td>"+
	   	                "</tr>"+
	   	                "<tr>"+
		                "<th>"+header1[6] +"</th>"+
		                "<td>"+header2[6] +"</td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th>"+header1[8] +"</th>"+
		                "<td>"+header2[8] +"</td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th>"+header1[10] +"</th>"+
		                "<td>"+header2[10] +"</td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th>"+header1[3] +"</th>"+
		                "<td>"+header2[3] +"</td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th>"+header1[22] +"</th>"+
		                "<td>"+header2[22] +"</td>"+
		                "</tr>"+
		                "</table>"+
		                "</html>";
		
		JLabel post = new JLabel(tabelle);
		//post.setText(tabelle);
		
		//System.err.println(tabelle);
		
		//post.setText("le voici ici");
		return post ;
			
	}
    
    public static JLabel TabelleInputFile () throws ParameterFileWrongInputException, IOException  {
    	
    //	CasePara cp = new CasePara(file);
    	BufferedReader in = new BufferedReader(new FileReader(inputfile.getPath()));
		String zeile = null;
		String [] header1 = null;
		String [] header2 = new  String [26];
		String [] header3 = null;
		while((zeile = in.readLine()) != null) {
			zeile=zeile.replaceAll(" ", "");
			zeile=zeile.replaceAll("\t", "");
			header1 = zeile.split(":=");
			
			if (header1[0].equals("Einlassschluss[KWnZOT]")){
				header2[0]=header1[0].replace("[", " [");
				header2[1]=header1[1];
				
			}
			if (header1[0].equals("Einlassoeffnet[KWnZOT]")){
				header2[2]=header1[0].replace("[", " [");
				header2[3]=header1[1];
				
			}
			if (header1[0].equals("Auslassoeffnet[KWnZOT]")){
				header2[4]=header1[0].replace("[", " [");
				header2[5]=header1[1];
				
			}
			if (header1[0].equals("Auslassschluss[KWnZOT]")){
				header2[6]=header1[0].replace("[", " [");
				header2[7]=header1[1];
				
			}
			if (header1[0].equals("Drehzahl[min^-1]")){
				header2[8]=header1[0].replace("[", " [");
				header2[9]=header1[1];
				
			}
			if (header1[0].equals("BOI[KWnZOT]")){
				header2[10]=header1[0].replace("[", " [");
				header2[11]=header1[1];
			}
			if (header1[0].equals("BOI_1[KWnZOT]")){
				header2[12]=header1[0].replace("[", " [");
				header2[13]=header1[1];
			}
			if (header1[0].equals("EOI[KWnZOT]")){
				header2[14]=header1[0].replace("[", " [");
				header2[15]=header1[1];
			}
			if (header1[0].equals("EOI_1[KWnZOT]")){
				header2[16]=header1[0].replace("[", " [");
				header2[17]=header1[1];
			}
			if (header1[0].equals("BOI_2[KWnZOT]")){
				header2[18]=header1[0].replace("[", " [");
				header2[19]=header1[1];
			}
			if (header1[0].equals("EOI_2[KWnZOT]")){
				header2[20]=header1[0].replace("[", " [");
				header2[21]=header1[1];
		    }
			if (header1[0].equals("rechnungsBeginn[KWnZOT]")){
				header2[22]=header1[0].replace("[", " [");
				header2[23]=header1[1];
				
			}
			if (header1[0].equals("rechnungsEnde[KWnZOT]")){
				header2[24]=header1[0].replace("[", " [");
				header2[25]=header1[1];
			}
			if(header1[0].equals("Verlustteilung[-]")){
				if (header1[1].equals("ja")){
					is_verlust_berechnen = true;
				}
				else {
					is_verlust_berechnen = false;
				}
			}
			header3 = zeile.split(":");
			header3[0] = header3[0].replace("[", "");
			if (header3[0].equals("berechnungsModell")){
				String[] tmp =  header3[1].split("_");
				if (tmp[0].equals("DVA")){
					berechnungModell = "DVA";
				}
				else if (tmp[0].equals("APR")){
					berechnungModell = "APR";
				}
			}
			
		}
		
		in.close();
    	
    	String tabelle ="<html>"+
		        "<table border = \"1\" cellpadding=\"0\"  cellspacing=\"0\">"+
		        "<tr>"+
                "<th colspan=\"2\">Tabelle Input File</th>"+
                "</tr>"+
                "<tr>"+
                "<th>"+header2[0]+"</th>"+
                "<td>"+header2[1]+"</td>"+
                "</tr>";
                
                for (int i = 2 ;i < header2.length;){
                	if (header2[i]!= null){
                		tabelle = tabelle + "<tr>"+
                                            "<th>"+header2[i]+"</th>"+
                	                        "<td>"+header2[i+1]+"</td>"+
                	                        "</tr>";
                	}
                	i = i+2;
                }
                
      tabelle = tabelle +
                "</table>"+
                "</html>";

      JLabel input = new JLabel(tabelle);

      return input;	
	
   }
/**
 * zeichen LineChart auf Panel mit folgenden Parameter   
 * @param Titel
 * @param XLabel
 * @param YLabel
 * @param data      set von Daten zum zeichnen 
 * @return
 */
private ChartPanel createChartPanel(String Titel, String XLabel,String YLabel, XYDataset data ) {
				
		JFreeChart chart = ChartFactory.createXYLineChart(
				Titel, XLabel , YLabel ,  data,
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
 * zeichen LineChart mit 2 Axis  auf Panel mit folgenden Parameter   
 * @param Titel
 * @param XLabel
 * @param YLabel
 * @param data      set von Daten zum zeichnen 
 * @return
 */
private ChartPanel createChartPanel(String Titel, String XLabel,String YLabel, XYDataset data1 ,XYDataset data2) {
				
		JFreeChart chart = ChartFactory.createXYLineChart(
				Titel, XLabel , YLabel ,  data1,
				PlotOrientation.VERTICAL, true, true, false);
		 XYPlot xyplot = (XYPlot)chart.getPlot();
		 NumberAxis numberaxis0 = (NumberAxis) xyplot.getRangeAxis();
	     numberaxis0.setLabelPaint(Color.red);
	     numberaxis0.setTickLabelPaint(Color.red);
	     NumberAxis numberaxis1 = new NumberAxis();
	     numberaxis1.setLabelPaint(Color.blue);
	     numberaxis1.setTickLabelPaint(Color.blue);
	     xyplot.setRangeAxis(1, numberaxis1);
	     xyplot.setDataset(1, data2);
	     xyplot.setRangeAxis(1, numberaxis1);
	     xyplot.mapDatasetToRangeAxis(1, 1);
	     StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
	     standardxyitemrenderer.setSeriesPaint(0, Color.blue);
	     xyplot.setRenderer(1, standardxyitemrenderer);

		//writeChartToPDF(chart, 600, 400, "src/bremoGraphik/pdf/Verlauf von "+Titel+".pdf");
		
		final ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}
/**
 * zeichen BarChart  auf Panel mit folgenden Parameter   
 * @param Titel
 * @param XLabel
 * @param YLabel
 * @param data      set von Daten zum zeichnen 
 * @return
 */
private ChartPanel createBarChartPanel(String Titel, String XLabel,String YLabel,CategoryDataset data) {
	
	JFreeChart chart = ChartFactory.createBarChart(Titel, XLabel , YLabel ,  data , PlotOrientation.VERTICAL, true, true, false);
	
	chart.setBackgroundPaint(Color.white);
    CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
    categoryplot.setBackgroundPaint(Color.lightGray);
    categoryplot.setDomainGridlinePaint(Color.white);
    categoryplot.setDomainGridlinesVisible(true);
    categoryplot.setRangeGridlinePaint(Color.white);
    NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
    numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    BarRenderer barrenderer = (BarRenderer)categoryplot.getRenderer();
    barrenderer.setDrawBarOutline(false);
    GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.blue, 0.0F, 0.0F, new Color(0, 0, 64));
    GradientPaint gradientpaint1 = new GradientPaint(0.0F, 0.0F, Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
    //GradientPaint gradientpaint2 = new GradientPaint(0.0F, 0.0F, Color.red, 0.0F, 0.0F, new Color(64, 0, 0));
    barrenderer.setSeriesPaint(0, gradientpaint);
    barrenderer.setSeriesPaint(1, gradientpaint1);
   // barrenderer.setSeriesPaint(2, gradientpaint2);
    CategoryAxis categoryaxis = categoryplot.getDomainAxis();
    categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.52359877559829882D));
	
    final ChartPanel chartPanel = new ChartPanel(chart);
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
 *  schreibt chart in pdf datei
 */
@SuppressWarnings("deprecation")
public  static void writeChartToPDF(Object [] Graphik_chart, int width, int height, String fileName) {
    PdfWriter writer = null;
 
    Document document = new Document();
 
    try {
        writer = PdfWriter.getInstance(document, new FileOutputStream(
                fileName));
        document.open();
//        BufferedImage bi = chart.createBufferedImage(300, 200);
//        try {
//        	ImageIO.write(bi, "png", new File("temp.png"));
//        } catch (Exception e){
//        	e.printStackTrace();
//        }
//        com.itextpdf.text.Image image =  com.itextpdf.text.Image.getInstance("temp.png");
//        image.setRotationDegrees(90.0f);
//        document.add(image);
//        File fi = new File("temp.png");
//        fi.delete();
        PdfContentByte contentByte = writer.getDirectContent();
        PdfTemplate template = contentByte.createTemplate(width, height);
        Graphics2D graphics2d = template.createGraphics(width, height,
                new DefaultFontMapper()); 
        Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0,width,
                height);
        ((JFreeChart)Graphik_chart[0]).draw(graphics2d, rectangle2d);
        graphics2d.dispose();

        AffineTransform transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(0, -(height + TitelLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
        
        template = contentByte.createTemplate(width, height);
        graphics2d = template.createGraphics(width, height,
                new DefaultFontMapper()); 
         rectangle2d = new Rectangle2D.Double(0, 0,width,
                height);
        ((JFreeChart)Graphik_chart[1]).draw(graphics2d, rectangle2d);
        graphics2d.dispose();
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(width, -(height + TitelLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
        
        template = contentByte.createTemplate(width, height);
        graphics2d = template.createGraphics(width, height,
                new DefaultFontMapper()); 
         rectangle2d = new Rectangle2D.Double(0, 0,width,
                height);
        ((JFreeChart)Graphik_chart[2]).draw(graphics2d, rectangle2d);
        graphics2d.dispose();
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(0, -(2*height + TitelLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
        
        template = contentByte.createTemplate(width, height);
        graphics2d = template.createGraphics(width, height,
                new DefaultFontMapper()); 
         rectangle2d = new Rectangle2D.Double(0, 0,width,
                height);
         
        if (Graphik_chart[3] != null) {
        		 ((JFreeChart)Graphik_chart[3]).draw(graphics2d, rectangle2d);
        	 }
        	 else {
        		 JLabel label = new JLabel("VerlustteilungsDiagramm nicht Vorhanden !");
            	 label.setForeground(Color.red);
            	 label.setFont(new java.awt.Font("Tahoma", 0, 16));
            	 label.paint(graphics2d);
            	 label.addNotify();
                 label.validate();
        	 }
        graphics2d.dispose();
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(width, -(2*height + TitelLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
         
   
        
        template = contentByte.createTemplate(TitelLabel.getWidth(), TitelLabel.getHeight());
        graphics2d = template.createGraphics(TitelLabel.getWidth(), TitelLabel.getHeight(),
                new DefaultFontMapper());
        TitelLabel.paint(graphics2d);
        TitelLabel.addNotify();
        TitelLabel.validate();
        graphics2d.dispose();
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate((document.getPageSize().getHeight()-TitelLabel.getWidth())/2, -TitelLabel.getHeight());
        contentByte.addTemplate(template, transformation);
        
        
        template = contentByte.createTemplate(Tabelle_PostLabel.getWidth(), Tabelle_PostLabel.getHeight());
        graphics2d = template.createGraphics(Tabelle_PostLabel.getWidth(), Tabelle_PostLabel.getHeight(),
                new DefaultFontMapper());
        
        Tabelle_PostLabel.paint(graphics2d);
        Tabelle_PostLabel.addNotify();
        Tabelle_PostLabel.validate();
        graphics2d.dispose();
        template.setHorizontalScaling(242/Tabelle_PostLabel.getWidth());
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(2*width, -Tabelle_PostLabel.getHeight());
        contentByte.addTemplate(template, transformation);
        
       
        template = contentByte.createTemplate(Tabelle_InLabel.getWidth(), Tabelle_InLabel.getHeight());
        graphics2d = template.createGraphics(Tabelle_InLabel.getWidth(), Tabelle_InLabel.getHeight(),
                new DefaultFontMapper());
       
        Tabelle_InLabel.paint(graphics2d);
        Tabelle_InLabel.addNotify();
        Tabelle_InLabel.validate();
        graphics2d.dispose();
        template.setHorizontalScaling(242/Tabelle_InLabel.getWidth());
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(2*width, -(Tabelle_PostLabel.getWidth()+Tabelle_InLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
        
        template = contentByte.createTemplate(pathLabel.getWidth(), pathLabel.getHeight());
        graphics2d = template.createGraphics(pathLabel.getWidth(), pathLabel.getHeight(),
                new DefaultFontMapper());
        pathLabel.paint(graphics2d);
        pathLabel.addNotify();
        pathLabel.validate();
        graphics2d.dispose();
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate(0, -(document.getPageSize().getWidth()-pathLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
        
//        System.err.println(document.getPageSize().getWidth());
//        System.err.println(document.getPageSize().getHeight());
        
        template = contentByte.createTemplate(datumLabel.getWidth(), datumLabel.getHeight());
        graphics2d = template.createGraphics(datumLabel.getWidth(), datumLabel.getHeight(),
                new DefaultFontMapper());
        datumLabel.paint(graphics2d);
        datumLabel.addNotify();
        datumLabel.validate();
        graphics2d.dispose();
        transformation = new AffineTransform();
        transformation.rotate(Math.PI/2);
        transformation.translate((document.getPageSize().getHeight()-datumLabel.getWidth()), -(document.getPageSize().getWidth()-datumLabel.getHeight()));
        contentByte.addTemplate(template, transformation);
   
        File file = new File(fileName);
        PdfFilePrinting.callExcecutablePrint(file);
        file.delete();
    } catch (Exception e) {
        e.printStackTrace();
    }
    document.close();
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
    private static JLabel TitelLabel;
    private static JLabel datumLabel;
    private JButton druckenButton;
    private JPanel graphik1Panel;
    private JComboBox graphik2ComboBox1;
    private JComboBox graphik2ComboBox2;
    private JComboBox graphik3ComboBox1;
    private JComboBox graphik3ComboBox2;
    private static JLabel pathLabel;
    private JButton speichernButton;
    private static File  inputfile ;
    private JPanel TitelPanel;
    private JPanel GraphikPanel;
    private JPanel GroupPanel;
    private static JPanel TabellePanel;
    private boolean is_P_V_Diagramm;
    private boolean is_Verlustteilung_Digramm;
    private static boolean is_verlust_berechnen;
    private ItemAction itemAction ;
    private static JLabel Tabelle_InLabel;
    private static JLabel Tabelle_PostLabel;
    public static String berechnungModell;
    // End of variables declaration
}
