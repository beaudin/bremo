/**
 * 
 */
package bremoswing.graphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.management.modelmbean.ModelMBean;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.geom.Shape;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import bremoExceptions.ParameterFileWrongInputException;
import bremoswing.util.FertigMeldungFrame;
import bremoswing.util.PdfFilePrinting;


/**
 * Abstract class Graphic  for all type of BremoModell 
 * @author Ngueneko Steve
 *
 */
public abstract class BremoModellGraphik extends JFrame{
	
	 private static final long serialVersionUID = 3116616946658017906L;
	 static JLabel KITLabel;
	 static JLabel IFKMLabel;
	 static JLabel TitelLabel;
     static JLabel datumLabel;
     JButton druckenButton;
     JButton LWAButton;
     JComboBox graphik2ComboBox1;
     JComboBox graphik2ComboBox2;
     JComboBox graphik3ComboBox1;
     JComboBox graphik3ComboBox2;
     static JLabel pathLabel;
     JButton speichernButton;
     static File  inputfile ;
     JPanel TitelPanel;
     JPanel GraphikPanel;
     JPanel GroupPanel;
     static JPanel TabellePanel;
     boolean is_P_V_Diagramm;
     boolean is_Verlustteilung_Digramm;
     boolean is_Wirkungsgrade_Diagramm;
     static boolean is_verlust_berechnen;
     boolean is_RestgasVorgabe_LWA;
     static JLabel Tabelle_InLabel;
     static JLabel Tabelle_PostLabel;
     String berechnungModell;
     String zeit_oder_KW;
     String [] header ;
     String Name;
   
     BremoModellGraphik (File file, String berechnungModell, boolean is_RestgasVorgabe_LWA ) throws ParameterFileWrongInputException, IOException {
    	 
    	 if (file == null) throw new FileNotFoundException();
         
     	 initComponents(file , berechnungModell, is_RestgasVorgabe_LWA);
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
 	 * @throws IOException 
 	 * @throws ParameterFileWrongInputException 
      */
	public void initComponents(File file, String berechnungModell, boolean is_RestgasVorgabe_LWA) throws ParameterFileWrongInputException, IOException {
		// TODO Auto-generated method stub
		
		/**  initialization of Variable ***********************************************/
    	
		TitelPanel   = new JPanel();
    	GraphikPanel = new JPanel();
    	TabellePanel = new JPanel();
    	GroupPanel   = new JPanel();
    	
    	KITLabel= new JLabel();
        TitelLabel = new JLabel();
        IFKMLabel= new JLabel();
        pathLabel  = new JLabel();
        datumLabel = new JLabel();
        
        druckenButton   = new JButton();
        speichernButton = new JButton();
        LWAButton = new JButton();
        
        graphik3ComboBox1 = new JComboBox();
        graphik3ComboBox2 = new JComboBox();
        graphik2ComboBox1 = new JComboBox();
        graphik2ComboBox2 = new JComboBox();
        
        inputfile = file;
        
        is_verlust_berechnen = false;
        
        this.berechnungModell = berechnungModell;
        this.is_RestgasVorgabe_LWA = is_RestgasVorgabe_LWA;
        zeit_oder_KW = zeit_oder_KW() ;
        
        Name = berechnungModell +"_"+ (inputfile.getName().replaceFirst(".txt", ""));
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
        
        
        /********************************************************************************/     
        
        /** TitelLabel Processing  ******************************************************/
        
        TitelPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.NONE;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.ipadx = 0;
		gc.ipady = 0;
		gc.weightx = 0;
    
        URL urlKit= getClass().getResource("/bremoswing/bild/KIT.png");
        ImageIcon iconKIT = new ImageIcon(urlKit);
        Image imageKit = iconKIT.getImage();
        imageKit  = imageKit.getScaledInstance(95, 45,java.awt.Image.SCALE_SMOOTH);
        iconKIT = new ImageIcon(imageKit);
        KITLabel.setIcon(iconKIT);
//        Border KitBorder = new LineBorder(Color.black, 1, true);
//        KITLabel.setBorder(KitBorder);
       
        
        TitelLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        TitelLabel.setText(Name);
        URL url = getClass().getResource("/bremoswing/bild/bremo2.png");
        ImageIcon icon = new ImageIcon(url);
        Image image = icon.getImage();
        image  = image.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);
        TitelLabel.setIcon(icon);
       
        
        URL urlIFKM = getClass().getResource("/bremoswing/bild/IFKM.png");
        ImageIcon iconIFKM = new ImageIcon(urlIFKM);
        Image imageIFKM = iconIFKM.getImage();
        imageIFKM  = imageIFKM.getScaledInstance(40, 40,java.awt.Image.SCALE_SMOOTH);
        iconIFKM = new ImageIcon(imageIFKM);
        IFKMLabel.setIcon(iconIFKM);
        
        int links = (TitelPanel.getPreferredSize().width / 2)-(TitelLabel.getPreferredSize().width/2)-KITLabel.getPreferredSize().width-5;
        int right = (TitelPanel.getPreferredSize().width / 2)-(TitelLabel.getPreferredSize().width/2)-IFKMLabel.getPreferredSize().width-5;
       // System.err.println(links+" "+ right);
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
        

      Tabelle_InLabel = TabelleInputFile();
      Tabelle_PostLabel = TabellePostFIle();
	
   
      Box b1 = Box.createHorizontalBox();
      b1.add(Box.createHorizontalStrut(7));
      b1.add(Tabelle_PostLabel);
      
      Box b2 = Box.createHorizontalBox();
      b2.add(Box.createHorizontalStrut(5));
      b2.add(Tabelle_InLabel);
      
      Box b3 = Box.createHorizontalBox();
      b3.add(Box.createHorizontalStrut(5));
     
      LWAButton.setText("LWA");
      if (berechnungModell.equals("DVA")|| berechnungModell.equals("APR")) {
    	  if (!is_RestgasVorgabe_LWA){
    	      LWAButton.setEnabled(false);
    	  }
      } else {
    	  LWAButton.setVisible(false);
    	  LWAButton.setEnabled(false);
      }
     
      b3.add(LWAButton);
      
      
      Box b4 = Box.createVerticalBox();
      b4.add(Box.createVerticalStrut(10));
      b4.add(b1);
      b4.add(Box.createVerticalStrut(60));
      b4.add(b2);
      b4.add(Box.createVerticalStrut(50));
      b4.add(b3);
      TabellePanel.add(b4);
        
        String str = "<html><b>\" Bitte W�hlen : \"</b></html>";
		graphik2ComboBox1.addItem(str);
		graphik2ComboBox1.addItem("p-V-Digramm");
		graphik2ComboBox1.addItem("Andere Diagramm");
		graphik2ComboBox1.setSelectedIndex(2);
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
		graphik2ComboBox2.setSelectedIndex(-1);
		graphik2ComboBox2
		.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					graphik2ComboBox2ActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		graphik3ComboBox1.addItem(str);
		graphik3ComboBox1.addItem("Andere Diagramm");
		graphik3ComboBox1.addItem("Verlustteilung BalkenDiagramm");
		graphik3ComboBox1.setSelectedIndex(1);
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
		graphik3ComboBox2.setSelectedItem(-1);
		graphik3ComboBox2
		.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					graphik3ComboBox2ActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
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
		
		LWAButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				
				try {
					new LWA_Graphik(inputfile, "LWA", false,BremoModellGraphik.this );
				} catch (ParameterFileWrongInputException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});

		datumLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		datumLabel.setText(df.format(dt));
		
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
        
        Container pane = getContentPane();
        pane.add(TitelPanel,BorderLayout.PAGE_START);
        pane.add(GraphikPanel,BorderLayout.CENTER);
        pane.add(TabellePanel,BorderLayout.LINE_END);
        pane.add(GroupPanel,BorderLayout.PAGE_END);
        
	}
	
	abstract void graphik2ComboBox1ActionPerformed(ActionEvent evt) throws IOException;
	
	abstract void graphik2ComboBox2ActionPerformed(ActionEvent evt) throws IOException;
	
	abstract void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException;
	
	abstract void graphik3ComboBox2ActionPerformed(ActionEvent evt) throws IOException;
	
	abstract ChartPanel Druckverlauf() throws IOException ;
	
	abstract ChartPanel p_V_Diagramm () throws IOException;
	
	
	
	/**
     * Build Graphic for the dQb and Qb/Qmax  Curve 
     * @param inputfile  InputputFile
     * @return
     * @throws IOException
     */
	public ChartPanel dQb_Qb_Qmax_Verlauf () throws IOException{
    	
    	
       
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
	    //chartVerlauf = createChartPanel(null, "[�KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, zeit_oder_KW, "[J/KW]", "[--]", dQbVerlauf, Qb_QmaxVerlauf);
	
		br.close();
		
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("dQb und Qb/Qmax Verlauf"));
		
		return chartVerlauf;
    	
    }
    
    /**
     * Build Graphic for the choice  Curve
     * @return
     * @throws IOException
     */
    public ChartPanel Auswahl_Diagramm(String selected) throws IOException{
    	BufferedReader br;
        if (this instanceof LWA_Graphik){
             br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName().replace(".txt", "_ERGEBNISSE_LW.txt")));
        } else {
            br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        } 
       XYSeries serie1 = new XYSeries(selected) ;
        XYDataset datasetVerlauf ;
        
        String zeile = null;
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
				serie1.setKey(selected );
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index]));//  KW  T_mittel
			}
			else {
				serie1.setKey(selected);
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index]));//  Zeit T_mittel
			}
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
		if (zeit_oder_KW.equals("KW")){
			//chartDruckVerlauf = createChartPanel(null, "[�KW]",null , datasetDruckVerlauf);
			chartVerlauf = createChartPanel(null, zeit_oder_KW, selected.split(" ")[1] , datasetVerlauf);
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
     * Build Graphic for the loss distribution
     * @return
     * @throws IOException
     */
	public ChartPanel Verlustteilung(String selected) throws IOException{
    	 
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
 					if (value[0].equals("pmi-Werte")){
 					    datasetVerlauf.addValue(Double.parseDouble(value[i]), value[0]+"[Pa]", header[i]);
 					    //System.err.println(Double.parseDouble(value[i])+" , "+ value[0]+" , "+header[i]);
 					}
 					else if (value[0].equals("Wirkungsgrade")){
 						datasetVerlauf.addValue(Double.parseDouble(value[i]), value[0]+"[-]", header[i]);
 					}
 				    }
 				}
 			}
 		}
// 		String s = "First";
//        String s1 = "Second";
//        String s2 = "Third";
//        String s3 = "Category 1";
//        String s4 = "Category 2";
//        String s5 = "Category 3";
//        String s6 = "Category 4";
//        String s7 = "Category 5";
//        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
//        defaultcategorydataset.addValue(1.0D, s, s3);
//        defaultcategorydataset.addValue(4D, s, s4);
//        defaultcategorydataset.addValue(3D, s, s5);
//        defaultcategorydataset.addValue(5D, s, s6);
//        defaultcategorydataset.addValue(5D, s, s7);
//        defaultcategorydataset.addValue(5D, s1, s3);
//        defaultcategorydataset.addValue(7D, s1, s4);
//        defaultcategorydataset.addValue(6D, s1, s5);
//        defaultcategorydataset.addValue(8D, s1, s6);
//        defaultcategorydataset.addValue(4D, s1, s7);
//        defaultcategorydataset.addValue(4D, s2, s3);
//        defaultcategorydataset.addValue(3D, s2, s4);
//        defaultcategorydataset.addValue(2D, s2, s5);
//        defaultcategorydataset.addValue(3D, s2, s6);
//        defaultcategorydataset.addValue(6D, s2, s7);
 		
 		ChartPanel chartVerlauf = null;
 		//chartDruckVerlauf = createChartPanel(null, "[�KW]",null , datasetDruckVerlauf);
 		chartVerlauf = createBarChartPanel(null, null, null , datasetVerlauf);
         
 		br.close();
 		
 		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected+" BalkenDiagramm"));
 		
 		return chartVerlauf;
    	
    }
    
    /**
     * draw LineChart in  Panel with this Parameters:   
     * @param Titel
     * @param XLabel
     * @param YLabel
     * @param data      set von Daten zum zeichnen 
     * @return
     */
    public ChartPanel createChartPanel(String Titel, String XLabel,String YLabel, XYDataset data ) {
    				
    	JFreeChart chart = ChartFactory.createXYLineChart(
    				Titel, XLabel , YLabel ,  data,
    				PlotOrientation.VERTICAL, true, true, false);
    	
    	XYPlot xyplot = (XYPlot)chart.getPlot();
    	
    	NumberAxis numberaxisY = (NumberAxis) xyplot.getRangeAxis();
    	numberaxisY.setLabelPaint(Color.black);
		numberaxisY.setTickLabelPaint(Color.black);
		numberaxisY.setLabelFont(new Font("Arial", Font.BOLD, 10));
		numberaxisY.setTickLabelFont(new Font("Arial", Font.BOLD, 10));
		numberaxisY.setPositiveArrowVisible(false);
    	
		NumberAxis numberaxisX = (NumberAxis) xyplot.getDomainAxis();
    	numberaxisX.setLabelPaint(Color.black);
		numberaxisX.setTickLabelPaint(Color.black);
		numberaxisX.setLabelFont(new Font("Arial", Font.BOLD, 10));
		numberaxisX.setTickLabelFont(new Font("Arial", Font.BOLD, 10));
		numberaxisX.setPositiveArrowVisible(false);
   
		final ChartPanel chartPanel = new ChartPanel(chart);
    		
    		return chartPanel;
    	}

    /**
     * draw LineChart with 2 Axis in  Panel with this Parameters:   
     * @param Titel
     * @param XLabel
     * @param YLabel
     * @param data      set von Daten zum zeichnen 
     * @return
     */
    @SuppressWarnings("deprecation")
	public ChartPanel createChartPanel(String Titel, String XLabel,String YLabel1,String YLabel2, XYDataset data1 ,XYDataset data2) {
    				
    		JFreeChart chart = ChartFactory.createXYLineChart(
    				Titel, XLabel , YLabel1 ,  data1,
    				PlotOrientation.VERTICAL, true, true, false);
    		 XYPlot xyplot = (XYPlot)chart.getPlot();
    		 Font font = new Font("SansSerif", Font.BOLD, 8);
    		 
    		 NumberAxis numberaxis0 = (NumberAxis) xyplot.getRangeAxis();
    		 numberaxis0.setLabelPaint(Color.black);
    	     numberaxis0.setTickLabelPaint(Color.red);
    	     numberaxis0.setLabelFont(new Font("Arial", Font.BOLD, 10));
    	     numberaxis0.setTickLabelFont(new Font("Arial", Font.BOLD, 10));
    	     numberaxis0.setPositiveArrowVisible(false);
    	     
    	     NumberAxis numberaxisX = (NumberAxis) xyplot.getDomainAxis();
    	     numberaxisX.setLabelPaint(Color.black);
    		 numberaxisX.setTickLabelPaint(Color.black);
    		 numberaxisX.setLabelFont(new Font("Arial", Font.BOLD, 10));
    		 numberaxisX.setTickLabelFont(new Font("Arial", Font.BOLD, 10));
    		 numberaxisX.setPositiveArrowVisible(false);
    	   
    	     NumberAxis numberaxis1 = new NumberAxis();
    	     numberaxis1.setLabelPaint(Color.black);
    	     numberaxis1.setTickLabelPaint(Color.blue);
    	     numberaxis1.setLabelFont(new Font("Arial", Font.PLAIN, 10));
    	     numberaxis1.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
    	     numberaxis1.setPositiveArrowVisible(false);
    	     numberaxis1.setLabel(YLabel2);
    	     
    	     xyplot.setRangeAxis(1, numberaxis1);
    	     xyplot.setDataset(1, data2);
    	     xyplot.setRangeAxis(1, numberaxis1);
    	     xyplot.mapDatasetToRangeAxis(1, 1);
    	     XYItemRenderer xyitemrenderer = new StandardXYItemRenderer();
    	     //xyitemrenderer.setSeriesPaint(0, new Color(16,78,139));
    	     xyitemrenderer.setSeriesPaint(0, new Color(0,191,255));
    	     if (data2.getSeriesCount()> 1 ) {
    	    	 xyitemrenderer.setSeriesPaint(1, new Color(0,0,0));
    	     }
    	     XYToolTipGenerator generator = new StandardXYToolTipGenerator();
    	     xyitemrenderer.setToolTipGenerator(generator);
    	     xyplot.setRenderer(1,xyitemrenderer);

    		//writeChartToPDF(chart, 600, 400, "src/bremoGraphik/pdf/Verlauf von "+Titel+".pdf");
    		
    		final ChartPanel chartPanel = new ChartPanel(chart);
    		return chartPanel;
    	}
    /**
     * draw BarChart in  Panel with this Parameters:    
     * @param Titel
     * @param XLabel
     * @param YLabel
     * @param data      set von Daten zum zeichnen 
     * @return
     */
    public ChartPanel createBarChartPanel(String Titel, String XLabel,String YLabel,CategoryDataset data) {
    	
    	JFreeChart chart = ChartFactory.createBarChart(Titel, XLabel , YLabel ,  data , PlotOrientation.VERTICAL, true, true, false);
    	
        CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
        if (is_Wirkungsgrade_Diagramm){
            BarRenderer barrenderer = (BarRenderer)categoryplot.getRenderer();
            //GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.red, 0.0F, 0.0F, new Color(64, 0, 0));
            barrenderer.setSeriesPaint(0, Color.blue);
        }
        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.52359877559829882D));
        categoryaxis.setLabelFont(new Font("serif", Font.PLAIN, 10));
        
//        NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
//        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 
//        barrenderer.setDrawBarOutline(false);
//        GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.blue, 0.0F, 0.0F, new Color(0, 0, 64));
//        GradientPaint gradientpaint1 = new GradientPaint(0.0F, 0.0F, Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
//        GradientPaint gradientpaint2 = new GradientPaint(0.0F, 0.0F, Color.red, 0.0F, 0.0F, new Color(64, 0, 0));
//        barrenderer.setSeriesPaint(0, gradientpaint2);
//        barrenderer.setSeriesPaint(2, gradientpaint2);
//        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
//        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.52359877559829882D));
    	
        final ChartPanel chartPanel = new ChartPanel(chart);
    	return chartPanel;
    }

	public void speichernButtonActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    	PrintMode();
		//saveImage();
    }
	/**
	 * Save Image of Panel from Frame
	 */
	public void saveImage() {
		Container c = getContentPane();
		BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
		c.paint(im.getGraphics());
		String str = "<html>Bild wurde unter <p><b>\" "+inputfile.getParent()+File.separator+Name+".png \" </b></p> gespeichert.</html>";
		
		//String str = "<html><font color : blue >"+inputfile.getParent()+"</font></html>";
	    try {
	        ImageIO.write(im, "png", new File(inputfile.getParent()+File.separator+Name+".png"));
	       // System.err.println("panel saved as image");
	        new FertigMeldungFrame("Speichern",str,JOptionPane.INFORMATION_MESSAGE);
//	        JOptionPane.showMessageDialog(this,
//	        		str , "Speichern",
//					JOptionPane.INFORMATION_MESSAGE);

	    } catch (Exception e) {
	       // System.err.println("panel not saved " + e.getMessage());
	    	 new FertigMeldungFrame("Speichern","Fehler: Bild wurde nicht gespeichert ",JOptionPane.WARNING_MESSAGE);
//	        JOptionPane.showMessageDialog(this,
//	        		"Fehler: Bild wurde nicht gespeichert ", "Speichern",
//					JOptionPane.WARNING_MESSAGE);

	    }
	}
	
	public void onclick() {
		Desktop desktop = Desktop.getDesktop();
	    try {
			desktop.open(inputfile.getParentFile());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * add Item from Array to JComboBox 
	 * @param cb
	 * @param item
	 */
	public void addItemToComboBox(JComboBox cb, String[] item) {

		cb.removeAllItems();
		cb.setModel(new JComboBox(item).getModel());
		
    }
	
    /**
     * Build the Table for desired parameter in InputFile
     * @return JLabel
     * @throws ParameterFileWrongInputException
     * @throws IOException
     */
	public static JLabel TabelleInputFile () throws ParameterFileWrongInputException, IOException  {
    	
	        
	    	BufferedReader in = new BufferedReader(new FileReader(inputfile.getPath()));
			String zeile = null;
			String [] header1 = null;
			String [] header2 = new  String [26];
			//String [] header3 = null;
			while((zeile = in.readLine()) != null) {
				zeile=zeile.replaceAll(" ", "");
				zeile=zeile.replaceAll("\t", "");
				header1 = zeile.split(":=");
				
				if (header1[0].equals("Einlassschluss[KWnZOT]")){
					header2[0]=header1[0].replace("[", " [");
					header2[1]=NumberCuter(header1[1]);
					
				}
				if (header1[0].equals("Einlassoeffnet[KWnZOT]")){
					header2[2]=header1[0].replace("[", " [");
					header2[3]=NumberCuter(header1[1]);
					
				}
				if (header1[0].equals("Auslassoeffnet[KWnZOT]")){
					header2[4]=header1[0].replace("[", " [");
					header2[5]=NumberCuter(header1[1]);
					
				}
				if (header1[0].equals("Auslassschluss[KWnZOT]")){
					header2[6]=header1[0].replace("[", " [");
					header2[7]=NumberCuter(header1[1]);
					
				}
				if (header1[0].equals("Drehzahl[min^-1]")){
					header2[8]=header1[0].replace("[", " [");
					header2[9]=NumberCuter(header1[1]);
					
				}
				if (header1[0].equals("BOI[KWnZOT]")){
					header2[10]=header1[0].replace("[", " [");
					header2[11]=NumberCuter(header1[1]);
				}
				if (header1[0].equals("BOI_1[KWnZOT]")){
					header2[12]=header1[0].replace("[", " [");
					header2[13]=NumberCuter(header1[1]);
				}
				if (header1[0].equals("EOI[KWnZOT]")){
					header2[14]=header1[0].replace("[", " [");
					header2[15]=NumberCuter(header1[1]);
				}
				if (header1[0].equals("EOI_1[KWnZOT]")){
					header2[16]=header1[0].replace("[", " [");
					header2[17]=NumberCuter(header1[1]);
				}
				if (header1[0].equals("BOI_2[KWnZOT]")){
					header2[18]=header1[0].replace("[", " [");
					header2[19]=NumberCuter(header1[1]);
				}
				if (header1[0].equals("EOI_2[KWnZOT]")){
					header2[20]=header1[0].replace("[", " [");
					header2[21]=NumberCuter(header1[1]);
			    }
				if (header1[0].equals("rechnungsBeginn[KWnZOT]")){
					header2[22]=header1[0].replace("[", " [");
					header2[23]=NumberCuter(header1[1]);
					
				}
				if (header1[0].equals("rechnungsEnde[KWnZOT]")){
					header2[24]=header1[0].replace("[", " [");
					header2[25]=NumberCuter(header1[1]);
				}
				if(header1[0].equals("Verlustteilung[-]")){
					if (header1[1].equals("ja")){
						is_verlust_berechnen = true;
					}
					else {
						is_verlust_berechnen = false;
					}
				}
//				header3 = zeile.split(":");
//				header3[0] = header3[0].replace("[", "");
//				if (header3[0].equals("berechnungsModell")){
//					String[] tmp =  header3[1].split("_");
//					if (tmp[0].equals("DVA")){
//						berechnungModell = "DVA";
//					}
//					else if (tmp[0].equals("APR")){
//						berechnungModell = "APR";
//					}
//				}
				
			}
			
			in.close();
	    		    	
			String tabelle ="<html>"+
			        "<table border = 0  width = 200>"+
			        "<tr>"+
	                "<th colspan=\"2\" bgcolor=#848484><font size =-1>Tabelle Input File</font></th>"+
	                "</tr>";
	                String color = "bgcolor=#BDBDBD";
	                for (int i = 0 ;i < header2.length;){
	                	if (header2[i]!= null){
	                		tabelle = tabelle + "<tr>"+
	                                            "<th "+color+"><font size =-2>"+header2[i]+"</font></th>"+
	                	                        "<td "+color+"><font size =-2>"+header2[i+1]+"</font></td>"+
	                	                        "</tr>";
	                		if (color.equals("bgcolor=#BDBDBD")){
		                		color = "bgcolor=#A4A4A4";
		                	} else {
		                		color = "bgcolor=#BDBDBD";
		                	}
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
	  * Build the Table for desired Paramter in Post Result File
	  * @return
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
		try {
		    for (int i = 0; i < header2.length; i++){
			     header2 [i] = NumberCuter(header2[i]);
		    }
		} catch (NumberFormatException e){
			
		}
		in.close();
		String tabelle ="<html>"+
				        "<table border = 0 width = 200 >"+ /*bgcolor=#FFFFFF*/
				        "<tr>"+
		                "<th colspan=\"2\" bgcolor=#848484><font size =-1>Tabelle Post File</font></th>"+
		                "</tr>"+
		                "<tr>"+
		                "<th bgcolor=#BDBDBD><font size =-2>"+header1[0] +"</font></th>"+
		                "<td bgcolor=#BDBDBD><font size =-2>"+header2[0] +"</font></td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th bgcolor=#A4A4A4><font size =-2>"+header1[1] +"</font></th>"+
	   	                "<td bgcolor=#A4A4A4><font size =-2>"+header2[1] +"</font></td>"+
	   	                "</tr>"+
	   	                "<tr>"+
		                "<th bgcolor=#BDBDBD><font size =-2>"+header1[6] +"</font></th>"+
		                "<td bgcolor=#BDBDBD><font size =-2>"+header2[6] +"</font></td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th bgcolor=#A4A4A4><font size =-2>"+header1[8] +"</font></th>"+
		                "<td bgcolor=#A4A4A4><font size =-2>"+header2[8] +"</font></td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th bgcolor=#BDBDBD><font size =-2>"+header1[10] +"</font></th>"+
		                "<td bgcolor=#BDBDBD><font size =-2>"+header2[10] +"</font></td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th bgcolor=#A4A4A4><font size =-2>"+header1[3] +"</font></th>"+
		                "<td bgcolor=#A4A4A4><font size =-2>"+header2[3] +"</font></td>"+
		                "</tr>"+
		                "<tr>"+
		                "<th bgcolor=#BDBDBD><font size =-2>"+header1[22] +"</font></th>"+
		                "<td bgcolor=#BDBDBD><font size =-2>"+header2[22] +"</font></td>"+
		                "</tr>"+
		                "</table>"+
		                "</html>";
		
		JLabel post = new JLabel(tabelle);
		
		return post ;
			
	}
     public String zeit_oder_KW() throws IOException{
    	 
    	 @SuppressWarnings("resource")
		BufferedReader in = new BufferedReader(new FileReader(inputfile));
         String zeile = null;
 		 String [] St = null;
 		 while((zeile = in.readLine()) != null) {
 			zeile=zeile.replaceAll(" ", "");
 			zeile=zeile.replaceAll("\t", "");
         	 St = zeile.split(":=");
         	 if (St[0].equals("zeit_oder_KW_Basiert[-]")){
         		 if (St[1].equals("KW")) return "KW" ;
         		 
         		 else   return "zeit" ;
         	 }
         }
 		 return null ;
     }
     
     public void druckenButtonActionPerformed(ActionEvent evt) {
 		// TODO add your handling code here:

 		int j = GraphikPanel.getComponentCount();
 		Paint color = null;
 		Object[] Graphik_Chart = new Object[j];
 		for (int i = 0; i < Graphik_Chart.length; i++) {
 			Graphik_Chart[i] = GraphikPanel.getComponent(i);
 			try {
 				Graphik_Chart[i] = ((ChartPanel) Graphik_Chart[i]).getChart();
 				Plot plot =((JFreeChart) Graphik_Chart[i]).getPlot();
 				color = plot.getBackgroundPaint();
 				plot.setBackgroundPaint(Color.white);
 			} catch (ClassCastException e) {
 				Graphik_Chart[i] = null;
 			}
 		}
 		ChartPanel chartpanel = (ChartPanel) GraphikPanel.getComponent(0);
 		//TitledBorder border = (TitledBorder) chartpanel.getBorder();
 		//String Titel = border.getTitle();
 		chartpanel.getChart();
 		writeChartToPDF(Graphik_Chart, 320, 230, "src/bremoGraphik/pdf/Graphik"
 				+ TitelLabel.getText() + ".pdf");
 		
 		for (int i = 0; i < Graphik_Chart.length; i++) {
 			Graphik_Chart[i] = GraphikPanel.getComponent(i);
 			try {
 				Graphik_Chart[i] = ((ChartPanel) Graphik_Chart[i]).getChart();
 				Plot plot =((JFreeChart) Graphik_Chart[i]).getPlot();
 				plot.setBackgroundPaint(color);
 			} catch (ClassCastException e) {
 				Graphik_Chart[i] = null;
 			}
 		}
 	}
     
     /**
      *  Write chart in pdf File
      */
     @SuppressWarnings("deprecation")
	public  static void writeChartToPDF(Object [] Graphik_chart, int width, int height, String fileName) {
         PdfWriter writer = null;
      
         Document document = new Document();
      
         try {
             writer = PdfWriter.getInstance(document, new FileOutputStream(
                     fileName));
             document.open();

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
              
             
             template = contentByte.createTemplate(KITLabel.getWidth(), KITLabel.getHeight());
             graphics2d = template.createGraphics(KITLabel.getWidth(), KITLabel.getHeight(),
                     new DefaultFontMapper());
             KITLabel.paint(graphics2d);
             KITLabel.addNotify();
             KITLabel.validate();
             graphics2d.dispose();
             transformation = new AffineTransform();
             transformation.rotate(Math.PI/2);
             transformation.translate(0 , -KITLabel.getHeight());
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
             transformation.translate((document.getPageSize().getHeight()-IFKMLabel.getHeight()-Tabelle_PostLabel.getWidth()-TitelLabel.getWidth())/2, -TitelLabel.getHeight());
             contentByte.addTemplate(template, transformation);
             
             
             template = contentByte.createTemplate(IFKMLabel.getWidth(), IFKMLabel.getHeight());
             graphics2d = template.createGraphics(IFKMLabel.getWidth(), IFKMLabel.getHeight(),
                     new DefaultFontMapper());
             IFKMLabel.paint(graphics2d);
             IFKMLabel.addNotify();
             IFKMLabel.validate();
             graphics2d.dispose();
             transformation = new AffineTransform();
             transformation.rotate(Math.PI/2);
             transformation.translate(2 * width - IFKMLabel.getWidth(), -IFKMLabel.getHeight());
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
             transformation.translate(2*width, -(Tabelle_PostLabel.getHeight()+10));
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
             transformation.translate(2*width, -(Tabelle_PostLabel.getHeight()+Tabelle_InLabel.getHeight()+20));
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
             
//             System.err.println(document.getPageSize().getWidth());
//             System.err.println(document.getPageSize().getHeight());
             
             template = contentByte.createTemplate(datumLabel.getWidth(), datumLabel.getHeight());
             graphics2d = template.createGraphics(datumLabel.getWidth(), datumLabel.getHeight(),
                     new DefaultFontMapper());
             datumLabel.paint(graphics2d);
             datumLabel.addNotify();
             datumLabel.validate();
             graphics2d.dispose();
             transformation = new AffineTransform();
             transformation.rotate(Math.PI/2);
             transformation.translate((document.getPageSize().getHeight()-datumLabel.getWidth()-100), -(document.getPageSize().getWidth()-datumLabel.getHeight()));
             contentByte.addTemplate(template, transformation);
        
             File file = new File(fileName);
             PdfFilePrinting.callExcecutablePrint(file);
             file.delete();
         } catch (Exception e) {
             e.printStackTrace();
         }
         document.close();
     }
     
     public Boolean  PrintMode (){ 
    	 
    	 Color panel = TitelPanel.getBackground();
    	 Color white = Color.white;
    	 Color chart = Color.gray;
    	 TitelPanel.setBackground(white);
    	 GraphikPanel.setBackground(white);
    	 GroupPanel.setBackground(white);
    	 TabellePanel.setBackground(white);
    	 
    	 TitelPanel.setBorder(BorderFactory.createLineBorder(Color.white));
         GraphikPanel.setBorder(BorderFactory.createLineBorder(Color.white));
         TabellePanel.setBorder(BorderFactory.createLineBorder(Color.white));
         GroupPanel.setBorder(BorderFactory.createLineBorder(Color.white));
         
         Object[] Graphik_Chart;
         Paint color = null;
    	 
    	int j = GraphikPanel.getComponentCount();
    	if (this instanceof LWA_Graphik){
    		
		    Graphik_Chart = new Object[3];
			Graphik_Chart[0] = ((JPanel)GraphikPanel.getComponent(0)).getComponent(0);
			Graphik_Chart[1] = ((JPanel)GraphikPanel.getComponent(1)).getComponent(0);
			Graphik_Chart[2] = ((JPanel)GraphikPanel.getComponent(1)).getComponent(1);
			
			((JPanel)GraphikPanel.getComponent(1)).setBackground(white);
			
			for (int i = 0; i < Graphik_Chart.length; i++) {
				try {
					chart = ((ChartPanel) Graphik_Chart[i]).getBackground();
					((ChartPanel) Graphik_Chart[i]).setBackground(Color.white);
					Graphik_Chart[i] = ((ChartPanel) Graphik_Chart[i])
							.getChart();
					Plot plot = ((JFreeChart) Graphik_Chart[i]).getPlot();
					color = plot.getBackgroundPaint();
					plot.setBackgroundPaint(Color.white);
				} catch (ClassCastException e) {
					((JPanel) Graphik_Chart[i]).setBackground(Color.white);
				}
			}
    	}
        else {
			Graphik_Chart = new Object[j];
			for (int i = 0; i < Graphik_Chart.length; i++) {
				Graphik_Chart[i] = GraphikPanel.getComponent(i);
				try {
					chart = ((ChartPanel) Graphik_Chart[i]).getBackground();
					((ChartPanel) Graphik_Chart[i]).setBackground(Color.white);
					Graphik_Chart[i] = ((ChartPanel) Graphik_Chart[i])
							.getChart();
					Plot plot = ((JFreeChart) Graphik_Chart[i]).getPlot();
					color = plot.getBackgroundPaint();
					plot.setBackgroundPaint(Color.white);
				} catch (ClassCastException e) {
					((JPanel) Graphik_Chart[i]).setBackground(Color.white);
				}
			}
		}
  		String textIn = Tabelle_InLabel.getText();
  		String textOut = Tabelle_PostLabel.getText();
  		
  		changeColorLineTabelleLabel(Tabelle_InLabel, "#FFFFFF");
  		changeColorLineTabelleLabel(Tabelle_PostLabel, "#FFFFFF");
  		  		  		
 		changeBorderTabelleLabel(Tabelle_InLabel, true);
 		changeBorderTabelleLabel(Tabelle_PostLabel, true);
 	
  		saveImage();
  		
		TitelPanel.setBackground(panel);
		GraphikPanel.setBackground(panel);
		GroupPanel.setBackground(panel);
		TabellePanel.setBackground(panel);
		
		TitelPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GraphikPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        TabellePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GroupPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        if (this instanceof LWA_Graphik){
    		
		    Graphik_Chart = new Object[3];
			Graphik_Chart[0] = ((JPanel)GraphikPanel.getComponent(0)).getComponent(0);
			Graphik_Chart[1] = ((JPanel)GraphikPanel.getComponent(1)).getComponent(0);
			Graphik_Chart[2] = ((JPanel)GraphikPanel.getComponent(1)).getComponent(1);
			
			((JPanel)GraphikPanel.getComponent(1)).setBackground(panel);
			
			for (int i = 0; i < Graphik_Chart.length; i++) {
	 			try {
	 				((ChartPanel) Graphik_Chart[i]).setBackground(chart);
	 				Graphik_Chart[i] = ((ChartPanel) Graphik_Chart[i]).getChart();
	 				Plot plot =((JFreeChart) Graphik_Chart[i]).getPlot();
	 				plot.setBackgroundPaint(color);
	 			} catch (ClassCastException e) {
	 				((JPanel)Graphik_Chart[i]).setBackground(panel);
	  			}
	 		}
    	} 
        else {
			for (int i = 0; i < Graphik_Chart.length; i++) {
				Graphik_Chart[i] = GraphikPanel.getComponent(i);
				try {
					((ChartPanel) Graphik_Chart[i]).setBackground(chart);
					Graphik_Chart[i] = ((ChartPanel) Graphik_Chart[i])
							.getChart();
					Plot plot = ((JFreeChart) Graphik_Chart[i]).getPlot();
					plot.setBackgroundPaint(color);
				} catch (ClassCastException e) {
					((JPanel) Graphik_Chart[i]).setBackground(panel);
				}
			}
		}
		changeBorderTabelleLabel(Tabelle_InLabel, false);
  		changeBorderTabelleLabel(Tabelle_PostLabel, false);
  		
  		Tabelle_InLabel.setText(textIn);
  		Tabelle_PostLabel.setText(textOut);
    	 
    	 return true;
    	
     }
     /**
      * Round a Number 2 digit after comma
      * @param number
      * @return
      */
     public static String NumberCuter(String number) {
    	 
    	 double d = Double.parseDouble(number);
    	 long i = Math.round(d*100);
    	 d = (double)i/100;
    	 return  Double.toString(d);
    
     }
     
     /**
      * Show PopUp to prevent the User for more Choose.
      */
     public void PopUp (){
 		
 		JLabel label = new JLabel();
 		label.setOpaque(true);
 		label.setBorder(BorderFactory.createLineBorder(Color.black));
 		label.setBackground(Color.WHITE);
 		
 		String head = "<html><h2><font color=\"red\"><u>Tipps</u>:</font></h2>" +
 				      "<h3>For More Option make a right click</h3></html>";
 	
 		label.setText(head);
 	     // panel.add(label,BorderLayout.CENTER);
 	      
 	      PopupFactory factory = PopupFactory.getSharedInstance();
 	      Random random = new Random();
 	      int x = random.nextInt(1000);
 	      int y = random.nextInt(1000);
 	      final Popup popup = factory.getPopup(this, label, 800, 50);
 	      if (!SelectItemToPlotten.pupUp) {
 	          popup.show();
 	          SelectItemToPlotten.pupUp = true;
 	      }
 	      label.addMouseListener(new MouseAdapter() {
 	    	 public void mouseClicked(MouseEvent e) {
 				// TODO Auto-generated method stub
 				popup.hide();
 			}
		});
 	      ActionListener hider = new ActionListener() {
 	        public void actionPerformed(ActionEvent e) {
 	          popup.hide();
 	        }
 	      };
 	      // Hide popup in 15 seconds
 	      Timer timer = new Timer(10000, hider);
 	      timer.start();
 	    
 	}
     /**
      * Change the Color of Table
      * @param TabelleLabel
      * @param ColorLine1  
      *                   Color in Hexadecimal ohne '#' zu vergessen
      * @param ColorLine2
      *                   Color in Hexadecimal ohne '#' zu vergessen
      */
     public void changeColorLineTabelleLabel(JLabel TabelleLabel , String ColorLine){
     
    	 String text = TabelleLabel.getText();
    	 
    	 text = text.replaceAll("bgcolor=#.{6}",  "bgcolor="+ColorLine);
    	
    	 TabelleLabel.setText(text);
    	 
    	 TabellePanel.revalidate();
     }
     /**
      * Change Border of Table
      * @param TabelleLabel
      * @param withBorder
      */
     public void changeBorderTabelleLabel(JLabel TabelleLabel, boolean withBorder){
    	 
    	 String text = TabelleLabel.getText();
    	 
    	 if (withBorder){
    	     text =  text.replaceAll("border = 0", "border = 1");
    		 }
    	 else {
    		 text = text.replaceAll("border = 1", "border = 0");
    	 }
    	 
    	 TabelleLabel.setText(text);
    	 
    	 TabellePanel.validate();
    	 
     }
     /**
      * Load the Header of this fileName
      * @param fileName
      * @return
      * @throws IOException
      */
     public String [] showHeaderOutFile (String fileName) throws IOException {
	 		
	 		BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+fileName));
	        
	        String zeile = null;
	 		String [] header = null;
	 		if ((zeile = br.readLine()) != null){
	 			header = zeile.split("\t");		
	 		}
	 		br.close();
	 		return header;
	 	}

}
	
