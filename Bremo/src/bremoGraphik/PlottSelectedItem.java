package bremoGraphik;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import bremoswing.SwingBremo;



public class PlottSelectedItem extends ApplicationFrame {
	
	public PlottSelectedItem(String title) throws IOException {

		super(title);
		
		setIconImage(new ImageIcon(getClass().getResource(
				"/bremoswing/bild/bremo2.png")).getImage());
        
		
		List<Double> XList = new LinkedList<Double>();
		List<Double> YList1 = new LinkedList<Double>();
		List<Double> YList2 = new LinkedList<Double>();
		String [] Selected  = SelectItemToPlotten.selectedItem;
		
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
     * Panel für  Teil ( Auswahl durch freq)  der Input Signal
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
	 * @uml.dependency   supplier="Test.PlottFIRFilter.InputSignalPanel"
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

			final ChartPanel chartPanel = new ChartPanel(chart);
			
			return chartPanel;
		}

		
		public VergleichPanel(List<Double> XList, List<Double> YList1, List<Double>YList2, String [] Selected) {

			super(new BorderLayout());
			VergleichData = createVergleichData(XList, YList1, YList2 , Selected);
			add(createChartPanel(Selected));

		}
	}
	/************************* close the Frame ********************************/
	public   void closeFrame() {
		final JOptionPane optionPane = new JOptionPane(
				"Wollen Sie Wirklich das Program beendet ?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		final JDialog dialog = optionPane.createDialog(this, "Exit");

		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();

				if (dialog.isVisible() && (e.getSource() == optionPane)
						&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
					dialog.setVisible(false);
				}
			}
		});
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);

		int value = ((Integer) optionPane.getValue()).intValue();
		if (value == JOptionPane.YES_OPTION) {
			System.exit(0);
		} else if (value == JOptionPane.NO_OPTION) {

		}
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
