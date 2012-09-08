package bremoGraphik;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import bremoswing.SwingBremo;



public class PlottSelectedItem extends ApplicationFrame {
	
	public PlottSelectedItem(String title) throws IOException {

		super(title);
		
		setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo2.png")).getImage());
        
		
		List<Double> XList = new LinkedList<Double>();
		List<Double> YList1 = new LinkedList<Double>();
		List<Double> YList2 = new LinkedList<Double>();
		String [] Selected  = SelectItemToPlotten_2.selectedItem;
		
		if (Selected[3].equals("")){
			
		try {
			int x = -1;
			int y = -1;
			BufferedReader in = new BufferedReader(new FileReader("src/InputFiles/"+Selected[0]));
			String zeile = null;
			if ((zeile = in.readLine()) != null){
				String [] header = zeile.split("\t");
				for (int i = 0; i<header.length; i++){
					if (header[i].equals(Selected[1])){
						x = i;
					}
				    if (header[i].equals(Selected[2])) {
						y = i;
					}
				}
			while((zeile = in.readLine()) != null) {
				header = zeile.split("\t");
				XList.add(Double.parseDouble(header[x]));
				YList1.add(Double.parseDouble(header[y]));				
			}
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		NormalPanel normalPanel = new NormalPanel( XList,  YList1 ,Selected);
		
		
		//JTabbedPane Tab = new JTabbedPane();
		
		//Tab.add("Normal",normalPanel);
		
		this.getContentPane().add(normalPanel, BorderLayout.CENTER);
		}
		else{
			try {
				int x  = -1;
				int y1 = -1;
				int y2 = -1;
				BufferedReader in = new BufferedReader(new FileReader("src/InputFiles/"+Selected[0]));
				String zeile = null;
				if ((zeile = in.readLine()) != null){
					String [] header = zeile.split("\t");
					for (int i = 0; i<header.length; i++){
						if (header[i].equals(Selected[1])){
							x = i;
						}
					    if (header[i].equals(Selected[2])) {
							y1 = i;
						}
					    if (header[i].equals(Selected[3])) {
							y2 = i;
						}
					}
				while((zeile = in.readLine()) != null) {
					header = zeile.split("\t");
					XList.add(Double.parseDouble(header[x]));
					YList1.add(Double.parseDouble(header[y1]));
					YList2.add(Double.parseDouble(header[y2]));
				}
					
				
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
			VergleichPanel vergleichPanel = new VergleichPanel( XList,  YList1,  YList2, Selected);
			
			
			//JTabbedPane Tab = new JTabbedPane();
			
			//Tab.add("InSignal", vergleichlPanel);
			
			this.getContentPane().add(vergleichPanel, BorderLayout.CENTER);
			
			
		}

	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		((ApplicationFrame)e.getSource()).dispose();
	}
	
	/**
     * Panel für Normal Eingang
     */
	static class NormalPanel extends JPanel {

		private XYDataset normalData;
		public XYSeries series1;

		private XYDataset createNormalData(List<Double> XList, List<Double> YList,String [] Selected) {
			
			String [] sb  = Selected[2].split(" ");
			series1 = new XYSeries(sb[0]);
			
			for (int i = 0 ; i< XList.size();i++){
		    	series1.add(XList.get(i), YList.get(i));
		    }
		    
			XYSeriesCollection xyseriescollection = new XYSeriesCollection();
			xyseriescollection.addSeries(series1);
			return xyseriescollection;
		}

		private ChartPanel createChartPanel( String [] Selected) {
			
			String [] sb  = Selected[1].split(" ");
			String Xlabel = sb[1];
		    sb  = Selected[2].split(" ");
			String Ylabel = sb[1];
			String Titel = sb[0];
			
			final JFreeChart chart = ChartFactory.createXYLineChart(
					"Verlauf von "+Titel, Xlabel , Ylabel , normalData,
					PlotOrientation.VERTICAL, true, true, false);
			
			//chart.setTitle(new TextTitle("XY Chart Sample, non default font", new java.awt.Font("Serif", Font.BOLD, 12)));
			chart.setBackgroundPaint(Color.white);
			chart.setBorderPaint(Color.black);
			chart.setBorderStroke(new BasicStroke(1));
			chart.setBorderVisible(true);
			
			writeChartToPDF(chart, 600, 400, "src/bremoGraphik/pdf/Verlauf von "+Titel+".pdf");
			
			final ChartPanel chartPanel = new ChartPanel(chart);
			// chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
			return chartPanel;
		}

		public NormalPanel(List<Double> XList, List<Double> YList, String [] Selected) {
			super(new BorderLayout());
			normalData = createNormalData( XList,  YList, Selected);
			add(createChartPanel(Selected));
		}
	}
	
	/**
	 * Panel Zur Signal Vergleich
	 * 
	 */
	static class VergleichPanel extends JPanel {

		private XYDataset VergleichData;
		public XYSeries series1;
		public XYSeries series2;

		
		private XYDataset createVergleichData(List<Double> XList, List<Double> YList1, List<Double> YList2,String [] Selected) {
            
			String [] sb  = Selected[2].split(" ");
			series1 = new XYSeries(sb[0]);
			sb = Selected[3].split(" ");			
			series2 = new XYSeries(sb[0]);
			
		    for (int i = 0 ; i< XList.size();i++){
		    	series1.add(XList.get(i), YList1.get(i));
		    	series2.add(XList.get(i), YList2.get(i));
		    }
		    
			XYSeriesCollection xyseriescollection = new XYSeriesCollection();
			xyseriescollection.addSeries(series1);
			xyseriescollection.addSeries(series2);
			return xyseriescollection;
		}
		
		private ChartPanel createChartPanel(String [] Selected) {
			
			String [] sb  = Selected[1].split(" ");
			String Xlabel = sb[1];
			sb  = Selected[2].split(" ");
			String Ylabel = sb[1];
			String Vergleich1 = sb[0];
		    sb  = Selected[3].split(" ");
			String Vergleich2 = sb[0];
			
			final JFreeChart chart = ChartFactory.createXYLineChart(
					"Vergleich "+Vergleich1+" und "+Vergleich2 , Xlabel, Ylabel, VergleichData,
					PlotOrientation.VERTICAL, true, true, false);

			//chart.setTitle(new TextTitle("XY Chart Sample, non default font", new java.awt.Font("Serif", Font.BOLD, 12)));
			chart.setBackgroundPaint(Color.white);
			chart.setBorderPaint(Color.black);
			chart.setBorderStroke(new BasicStroke(1));
			chart.setBorderVisible(true);
			
			writeChartToPDF(chart, 500, 400, "src/bremoGraphik/pdf/Vergleich "+Vergleich1+" und "+Vergleich2+".pdf");
			
			final ChartPanel chartPanel = new ChartPanel(chart);
			
			return chartPanel;
		}

		
		public VergleichPanel(List<Double> XList, List<Double> YList1, List<Double>YList2, String [] Selected) {

			super(new BorderLayout());
			VergleichData = createVergleichData(XList, YList1, YList2 , Selected);
			add(createChartPanel(Selected));

		}
		
	}
	/**
	 *  schreibt chart in pdf datei
	 */
	@SuppressWarnings("deprecation")
	public static void writeChartToPDF(JFreeChart chart, int width, int height, String fileName) {
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
	        Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
	                height);
	 
	        chart.draw(graphics2d, rectangle2d);
	         
	        graphics2d.dispose();
	        contentByte.addTemplate(template, 0, 200);
	 
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    document.close();
	}
	
	public static void BremoGrafik() throws  IOException
	{
		PlottSelectedItem demo = new PlottSelectedItem("Bremo_Ablauf_Grafik");
		demo.pack();
		demo.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		RefineryUtilities.centerFrameOnScreen(demo);
		
		demo.setVisible(true);
	}
}
