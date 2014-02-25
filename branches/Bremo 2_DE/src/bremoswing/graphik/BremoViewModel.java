package bremoswing.graphik;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
/**
 * @author Steve Ngueneko
 *
 */
public class BremoViewModel implements Observable{
	
	private Observer bremoViewObserver ;
	/**
	 *  Storage is a store place for the model. At 1. place Item from Bremoview are stored and 2.place
	 *  Item from ItenChooseview are stored.
	 */
	private List<Object> Storage ;
	private File file;
	private ChartPanel Chart ;
	
	
	public BremoViewModel() {
		Storage = new ArrayList<Object>();
	}
	
	 	/**
		 * Notify Observer when a change of Titel  Occur
		 */
		public void notifyObserver(String str) {
			
			bremoViewObserver.update(null, str);
			
		}
		/**
		 * Notify Observer when a change of ChartPanel Occur
		 */
		public void notifyObserver(ChartPanel chart) {
			bremoViewObserver.update(null, chart);
		}
		/**
		 * Notify Observer when a change of Item in the X Combobox of String Occur
		 */
		public void notifyObserver(String [] head) {
			bremoViewObserver.update(null, head);
		}
		
		/**
		 * Notify Observer to show or hide itemchooseFrame
		 */
		public void notifyObserver(boolean bol) {
			   bremoViewObserver.update(null, bol);
			    
			  }
		
		
//		/**
//		 * Reset the Model
//		 */
//		 public void Reset () {
//			 fileName = null;
//			 fileName = "";
//			 header   = null;
//		}
		
		
		/**
	     * Load the Header of this fileName
	     * @param fileName
	     * @return
	     * @throws IOException
	     */
	    public  String [] showHeaderOfFile (File file) throws IOException {
		 		
		 		BufferedReader br = new  BufferedReader(new FileReader(file));
		        
		        String zeile = null;
		 		String [] header = null;
		 		if ((zeile = br.readLine()) != null){
		 			header = zeile.split("\t");		
		 		}
		 		br.close();
		 		return header;
		 	}

		@Override
		public void addObserver(Observer obs) {
			bremoViewObserver = obs;
		}

		@Override
		public Observer getObserver() {
			return bremoViewObserver;
			
		}

		@Override
		public void deleteObserver() {
			bremoViewObserver = null;
			
		}

		public void showChooseFrame() {
			notifyObserver(true);
		}
		
		public void hideChooseFrame() {
			notifyObserver(false);
		}

		public void storeItemFromBremoView(String[] allselectedItem) {
			Storage.add(0, allselectedItem);
			
		}

		public void storeItemFromChooseFrame(List<List<String>> selectedItemListlist) {
			Storage.add(1, selectedItemListlist);
			
		}

		public void createChart() throws IOException {
			if (Storage.get(0) == null && Storage.get(1)== null)
				throw new NullPointerException();
			
			String [] dataBremoview = getDataFromBremoView();
			List<List<String>> dataListFromchooseFrame = getDataFromChooseFrame();
			
			String nbr = dataBremoview[0];
			String x = dataBremoview[2];
						
			switch (nbr) {
			case "1":
				Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0));
				break;
            case "2":
            	Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),dataListFromchooseFrame.get(1));
				break;
            case "3":
            	Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),dataListFromchooseFrame.get(1),dataListFromchooseFrame.get(2));
	            break;
			default:
				Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),dataListFromchooseFrame.get(1),dataListFromchooseFrame.get(2),dataListFromchooseFrame.get(3));
	            break;
			}
			
			notifyObserver(Chart);

		}
		/**
		 * get Data from BremoView stored in Storage Variable
		 * @return
		 */
		public String [] getDataFromBremoView() {
			return (String[]) Storage.get(0);
			
		}
		
		/**
		 * get Data from ChooseFrame stored in Storage Variable
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public List<List<String>> getDataFromChooseFrame() {
			return (List<List<String>>) Storage.get(1);
			
		}

		public void setInputFile(File file) throws IOException {
			this.file = file;
			notifyObserver(file.getName());
			notifyObserver(showHeader());
		}
		/**
	      * Load the Header of this fileName
	      * @param fileName
	      * @return
	      * @throws IOException
	      */
	     public  String [] showHeader () throws IOException {
		 		
		 		BufferedReader br = new  BufferedReader(new FileReader(file));
		        
		        String zeile = null;
		 		String [] header = null;
		 		if ((zeile = br.readLine()) != null){
		 			header = zeile.split("\t");		
		 		}
		 		br.close();
		 		return header;
		 	}
		
		/**
	     * Build Graphic for the 1  Curve
	     * @return
	     * @throws IOException
	     */
	    public ChartPanel Build_Diagramm(String x_selected, List<String> y_selected) throws IOException {
	    	BufferedReader br;
	            br = new  BufferedReader(new FileReader(file));
	       
	       List<XYSeries> listSerie = new ArrayList<XYSeries>();
	       
	       for (int i = 0; i < y_selected.size(); i++){
	    	   listSerie.add(new XYSeries(y_selected.get(i),false,true));
	       }
	    
	        XYDataset datasetVerlauf ;
	        
	        String zeile = null;
			List<String> header = new ArrayList<String>();
			
			String [] value = null;
			int []  y_index = new int[y_selected.size()];
			int x_index = -1;
			if ((zeile = br.readLine()) != null){
				header = Arrays.asList(zeile.split("\t"));
				
				x_index = header.indexOf(x_selected);
				
				for (int i = 0 ; i < header.size(); i++) {
					if (y_selected.contains(header.get(i))){
						y_index[y_selected.indexOf(header.get(i))] = i;
					}
				}
			}
			while ((zeile = br.readLine()) != null){
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index.length; i++){
					listSerie.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
			}	
				
			XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
			for (int i = 0; i < listSerie.size(); i++){
				collectionVerlauf.addSeries(listSerie.get(i));
			}
			datasetVerlauf = collectionVerlauf;
			ChartPanel chartVerlauf = null;
			String [] yLabel =  y_selected.toArray(new String[y_selected.size()]);
			for (int i = 0; i < yLabel.length ; i++ ){
				yLabel[i] = yLabel[i].split(" ")[1];
       		}
			
			
			chartVerlauf = createChartPanel(null, x_selected, yLabel , datasetVerlauf);
			
			br.close();
//			selected = selected.substring(0, selected.indexOf("["));
//			chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected+" verlauf"));
			
			return chartVerlauf;
	    	
	    }
		
		
	    /**
	     * Build Graphic for 2 Curve
	     * @return
	     * @throws IOException
	     */
	    public ChartPanel Build_Diagramm(String x_selected,List<String> y_selected1, List<String> y_selected2) throws IOException {
	    	BufferedReader br;
	        br = new  BufferedReader(new FileReader(file));
	        
	        List<XYSeries> listSerie1 = new ArrayList<XYSeries>();
	        List<XYSeries> listSerie2 = new ArrayList<XYSeries>();
	        
	        for (int i = 0; i < y_selected1.size(); i++){
		    	   listSerie1.add(new XYSeries(y_selected1.get(i),false,true));
		       }
	        for (int i = 0; i < y_selected2.size(); i++){
		    	   listSerie2.add(new XYSeries(y_selected2.get(i),false,true));
		       }
	    XYDataset datasetVerlauf1 ;
	    XYDataset datasetVerlauf2 ;
	    
	    String zeile = null;
	    List<String> header = new ArrayList<String>();
	    
		String [] value = null;
		int [] y_index1 = new int [y_selected1.size()];
		int [] y_index2 = new int [y_selected2.size()];
		int x_index = -1;
		if ((zeile = br.readLine()) != null){
			header = Arrays.asList(zeile.split("\t"));
			
			x_index = header.indexOf(x_selected);
			
			for (int i = 0 ; i < header.size(); i++) {
				if (y_selected1.contains(header.get(i))){
					y_index1[y_selected1.indexOf(header.get(i))] = i;
				}
				if (y_selected2.contains(header.get(i))){
					y_index2[y_selected2.indexOf(header.get(i))] = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null) {
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index1.length; i++){
					listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
				for (int i = 0 ; i < y_index2.length; i++){
					listSerie2.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
			}
		XYSeriesCollection collectionVerlauf1 = new XYSeriesCollection();
		for (int i = 0; i < listSerie1.size(); i++){
			collectionVerlauf1.addSeries(listSerie1.get(i));
		}
		datasetVerlauf1 = collectionVerlauf1;
		
		XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
		for (int i = 0; i < listSerie2.size(); i++){
			collectionVerlauf2.addSeries(listSerie2.get(i));
		}
		datasetVerlauf2 = collectionVerlauf2;
		
		ChartPanel chartVerlauf = null;
		
		String [] yLabel1 = y_selected1.toArray(new String[y_selected1.size()]) ;
		for (int i = 0; i < yLabel1.length ; i++ ){
			yLabel1[i] = yLabel1[i].split(" ")[1];
   		}
		
		String [] yLabel2 = y_selected2.toArray(new String[y_selected2.size()]) ;
		for (int i = 0; i < yLabel2.length ; i++ ){
			yLabel2[i] = yLabel2[i].split(" ")[1];
   		}
		
	    chartVerlauf = createChartPanel(null, x_selected, yLabel1, yLabel2 ,datasetVerlauf1, datasetVerlauf2);
		
	    br.close();
//		selected1 = selected1.substring(0, selected1.indexOf("["));
//		selected2 = selected2.substring(0, selected2.indexOf("["));
//		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected1+" ,"+ selected2 +" verlauf"));
		
		return chartVerlauf;
		
	    }
	    
	   
	    /**
	     * Build Graphic for 3  Curve
	     * @return
	     * @throws IOException
	     */
	    public ChartPanel Build_Diagramm(String x_selected, List<String> y_selected1, List<String> y_selected2, List<String> y_selected3) throws IOException {
	    	BufferedReader br;
	        br = new  BufferedReader(new FileReader(file));
	        
	        List<XYSeries> listSerie1 = new ArrayList<XYSeries>();
	        List<XYSeries> listSerie2 = new ArrayList<XYSeries>();
	        List<XYSeries> listSerie3 = new ArrayList<XYSeries>();
	        
	        for (int i = 0; i < y_selected1.size(); i++){
		    	   listSerie1.add(new XYSeries(y_selected1.get(i),false,true));
		       }
	        for (int i = 0; i < y_selected2.size(); i++){
		    	   listSerie2.add(new XYSeries(y_selected2.get(i),false,true));
		       }
	        for (int i = 0; i < y_selected3.size(); i++){
		    	   listSerie3.add(new XYSeries(y_selected3.get(i),false,true));
		       }
	   
	   XYDataset datasetVerlauf1;
	   XYDataset datasetVerlauf2;
	   XYDataset datasetVerlauf3;
	    
	   String zeile = null;
	    List<String> header = new ArrayList<String>();
	    
		String [] value = null;
		int [] y_index1 = new int [y_selected1.size()];
		int [] y_index2 = new int [y_selected2.size()];
		int [] y_index3 = new int [y_selected3.size()];
		int x_index = -1;
		if ((zeile = br.readLine()) != null){
			header = Arrays.asList(zeile.split("\t"));
			
			x_index = header.indexOf(x_selected);
			
			for (int i = 0 ; i < header.size(); i++) {
				if (y_selected1.contains(header.get(i))){
					y_index1[y_selected1.indexOf(header.get(i))] = i;
				}
				if (y_selected2.contains(header.get(i))){
					y_index2[y_selected2.indexOf(header.get(i))] = i;
				}
				if (y_selected2.contains(header.get(i))){
					y_index2[y_selected2.indexOf(header.get(i))] = i;
				}
				if (y_selected3.contains(header.get(i))){
					y_index3[y_selected3.indexOf(header.get(i))] = i;
				}
			}
			
		}
		while ((zeile = br.readLine()) != null) {
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index1.length; i++){
					listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
				for (int i = 0 ; i < y_index2.length; i++){
					listSerie2.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
				for (int i = 0 ; i < y_index3.length; i++){
					listSerie3.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
			}
		XYSeriesCollection collectionVerlauf1 = new XYSeriesCollection();
		for (int i = 0; i < listSerie1.size(); i++){
			collectionVerlauf1.addSeries(listSerie1.get(i));
		}
		datasetVerlauf1 = collectionVerlauf1;
		
		XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
		for (int i = 0; i < listSerie2.size(); i++){
			collectionVerlauf2.addSeries(listSerie2.get(i));
		}
		datasetVerlauf2 = collectionVerlauf2;
		
		XYSeriesCollection collectionVerlauf3 = new XYSeriesCollection();
		for (int i = 0; i < listSerie3.size(); i++){
			collectionVerlauf3.addSeries(listSerie3.get(i));
		}
		datasetVerlauf3 = collectionVerlauf3;
		
		ChartPanel chartVerlauf = null;
		
		String [] yLabel1 = y_selected1.toArray(new String[y_selected1.size()]) ;
		for (int i = 0; i < yLabel1.length ; i++ ){
			yLabel1[i] = yLabel1[i].split(" ")[1];
   		}
		
		String [] yLabel2 = y_selected2.toArray(new String[y_selected2.size()]) ;
		for (int i = 0; i < yLabel2.length ; i++ ){
			yLabel2[i] = yLabel2[i].split(" ")[1];
   		}
		
		String [] yLabel3 = y_selected3.toArray(new String[y_selected3.size()]) ;
		for (int i = 0; i < yLabel3.length ; i++ ){
			yLabel3[i] = yLabel3[i].split(" ")[1];
   		}
		
	    chartVerlauf = createChartPanel(null, x_selected, yLabel1, yLabel2, yLabel3, datasetVerlauf1, datasetVerlauf2, datasetVerlauf3);
		
	    br.close();
//		selected1 = selected1.substring(0, selected1.indexOf("["));
//		selected2 = selected2.substring(0, selected2.indexOf("["));
//		selected3 = selected3.substring(0, selected3.indexOf("["));
//		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected1+" ,"+ selected2 +" ,"+ selected3 +" verlauf"));
		
		return chartVerlauf;
		
	    }
	    
	    /**
	     * Build Graphic for 4  Curve
	     * @return
	     * @throws IOException
	     */
	    public ChartPanel Build_Diagramm(String x_selected, List<String> y_selected1, List<String> y_selected2, List<String> y_selected3, List<String> y_selected4) throws IOException {
	    	BufferedReader br;
	        br = new  BufferedReader(new FileReader(file));
	   
	        List<XYSeries> listSerie1 = new ArrayList<XYSeries>();
	        List<XYSeries> listSerie2 = new ArrayList<XYSeries>();
	        List<XYSeries> listSerie3 = new ArrayList<XYSeries>();
	        List<XYSeries> listSerie4 = new ArrayList<XYSeries>();
	        
	        for (int i = 0; i < y_selected1.size(); i++){
		    	   listSerie1.add(new XYSeries(y_selected1.get(i),false,true));
		       }
	        for (int i = 0; i < y_selected2.size(); i++){
		    	   listSerie2.add(new XYSeries(y_selected2.get(i),false,true));
		       }
	        for (int i = 0; i < y_selected3.size(); i++){
		    	   listSerie3.add(new XYSeries(y_selected3.get(i),false,true));
		       }
	        for (int i = 0; i < y_selected4.size(); i++){
		    	   listSerie4.add(new XYSeries(y_selected4.get(i),false,true));
		       }
	   
	   XYDataset datasetVerlauf1;
	   XYDataset datasetVerlauf2;
	   XYDataset datasetVerlauf3;
	   XYDataset datasetVerlauf4; 
	   String zeile = null;
	    List<String> header = new ArrayList<String>();
	    
		String [] value = null;
		int [] y_index1 = new int [y_selected1.size()];
		int [] y_index2 = new int [y_selected2.size()];
		int [] y_index3 = new int [y_selected3.size()];
		int [] y_index4 = new int [y_selected4.size()];
		int x_index = -1;
		if ((zeile = br.readLine()) != null){
			header = Arrays.asList(zeile.split("\t"));
			
			x_index = header.indexOf(x_selected);
			
			for (int i = 0 ; i < header.size(); i++) {
				if (y_selected1.contains(header.get(i))){
					y_index1[y_selected1.indexOf(header.get(i))] = i;
				}
				if (y_selected2.contains(header.get(i))){
					y_index2[y_selected2.indexOf(header.get(i))] = i;
				}
				if (y_selected2.contains(header.get(i))){
					y_index2[y_selected2.indexOf(header.get(i))] = i;
				}
				if (y_selected3.contains(header.get(i))){
					y_index3[y_selected3.indexOf(header.get(i))] = i;
				}
				if (y_selected4.contains(header.get(i))){
					y_index4[y_selected4.indexOf(header.get(i))] = i;
				}
			}
			
		}
		while ((zeile = br.readLine()) != null) {
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index1.length; i++){
					listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
				for (int i = 0 ; i < y_index2.length; i++){
					listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
				for (int i = 0 ; i < y_index3.length; i++){
					listSerie3.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
				for (int i = 0 ; i < y_index4.length; i++){
					listSerie4.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[i]));
				}
			}
		XYSeriesCollection collectionVerlauf1 = new XYSeriesCollection();
		for (int i = 0; i < listSerie1.size(); i++){
			collectionVerlauf1.addSeries(listSerie1.get(i));
		}
		datasetVerlauf1 = collectionVerlauf1;
		
		XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
		for (int i = 0; i < listSerie2.size(); i++){
			collectionVerlauf2.addSeries(listSerie2.get(i));
		}
		datasetVerlauf2 = collectionVerlauf2;
		
		XYSeriesCollection collectionVerlauf3 = new XYSeriesCollection();
		for (int i = 0; i < listSerie3.size(); i++){
			collectionVerlauf3.addSeries(listSerie3.get(i));
		}
		datasetVerlauf3 = collectionVerlauf3;
		
		XYSeriesCollection collectionVerlauf4 = new XYSeriesCollection();
		for (int i = 0; i < listSerie4.size(); i++){
			collectionVerlauf4.addSeries(listSerie4.get(i));
		}
		datasetVerlauf4 = collectionVerlauf4;
		
		ChartPanel chartVerlauf = null;
		
		String [] yLabel1 = y_selected1.toArray(new String[y_selected1.size()]) ;
		for (int i = 0; i < yLabel1.length ; i++ ){
			yLabel1[i] = yLabel1[i].split(" ")[1];
   		}
		
		String [] yLabel2 = y_selected2.toArray(new String[y_selected2.size()]) ;
		for (int i = 0; i < yLabel2.length ; i++ ){
			yLabel2[i] = yLabel2[i].split(" ")[1];
   		}
		
		String [] yLabel3 = y_selected3.toArray(new String[y_selected3.size()]) ;
		for (int i = 0; i < yLabel3.length ; i++ ){
			yLabel3[i] = yLabel3[i].split(" ")[1];
   		}
		
		String [] yLabel4 = y_selected4.toArray(new String[y_selected4.size()]) ;
		for (int i = 0; i < yLabel4.length ; i++ ){
			yLabel4[i] = yLabel4[i].split(" ")[1];
   		}
		
		
	    chartVerlauf = createChartPanel(null, x_selected, yLabel1, yLabel2, yLabel3, yLabel4, datasetVerlauf1, datasetVerlauf2, datasetVerlauf3, datasetVerlauf4);
		
	    br.close();
//		selected1 = selected1.substring(0, selected1.indexOf("["));
//		selected2 = selected2.substring(0, selected2.indexOf("["));
//		selected3 = selected3.substring(0, selected3.indexOf("["));
//		selected4 = selected4.substring(0, selected4.indexOf("["));
//		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected1 +" ,"+ selected2 +" ,"+ selected3 +" ,"+ selected4 +" verlauf"));
		
		return chartVerlauf;
		
	    }
	    /**
	     * draw LineChart in  Panel with this Parameters:   
	     * @param Titel
	     * @param XLabel
	     * @param YLabel array
	     * @param data      set von Daten zum zeichnen 
	     * @return
	     */
	    public static ChartPanel createChartPanel(String Titel, String XLabel,String [] YLabel, XYDataset data ) {
	       		
	    		return createChartPanel(Titel, XLabel, YLabel, null, null, null, data, null, null, null);
	    	}

	    /**
	     * draw LineChart with 2 Axis in  Panel with this Parameters:   
	     * @param Titel
	     * @param XLabel
	     * @param YLabel
	     * @param data      set von Daten zum zeichnen 
	     * @return
	     */
	    public static ChartPanel createChartPanel(String Titel, String XLabel,String[] YLabel1,String[] YLabel2, XYDataset data1 ,XYDataset data2) {
	    				
	    		return createChartPanel(Titel, XLabel, YLabel1, YLabel2, null, null, data1, data2, null, null);
	    	}
	    /**
	     * draw LineChart with 3 Axis in  Panel with this Parameters:   
	     * @param Titel
	     * @param XLabel
	     * @param YLabel
	     * @param data      set von Daten zum zeichnen 
	     * @return
	     */
	    public static ChartPanel createChartPanel(String Titel, String XLabel,String[] YLabel1,String[] YLabel2,String[] YLabel3, XYDataset data1 ,XYDataset data2, XYDataset data3) {
	    				
	    		return createChartPanel(Titel, XLabel, YLabel1, YLabel2, YLabel3, null, data1, data2, data3, null);
	    	}

	    /**
	     * draw LineChart with 4 Axis in  Panel with this Parameters:   
	     * @param Titel
	     * @param XLabel
	     * @param YLabel
	     * @param data      set von Daten zum zeichnen 
	     * @return
	     */
		@SuppressWarnings("deprecation")
		public static ChartPanel createChartPanel(String Titel, String XLabel,String[] YLabel1,String[] YLabel2,String[] YLabel3,String[] YLabel4, XYDataset data1 ,XYDataset data2, XYDataset data3, XYDataset data4) {

			XYDataset data_1, data_2, data_3, data_4;

			data_1 = data1;
			data_2 = data2;
			data_3 = data3;
			data_4 = data4;

			int nbr_of_curve = 0;

			if (data1 != null) {
				nbr_of_curve = 1;
				if (data2 != null) {
					nbr_of_curve = 2;
					if (data3 != null) {
						nbr_of_curve = 3;
						if (data4 != null) {
							nbr_of_curve = 4;
						}
					}
				}
			}

			JFreeChart chart = ChartFactory.createXYLineChart(Titel, XLabel,
					YLabel1[0], data_1, PlotOrientation.VERTICAL, true, true, false);
			XYPlot xyplot = (XYPlot) chart.getPlot();

			/** Setting of the X Axis **/
			NumberAxis numberaxisX = (NumberAxis) xyplot.getDomainAxis();
			numberaxisX.setLabelPaint(Color.black);
			numberaxisX.setTickLabelPaint(Color.black);
			numberaxisX.setLabelFont(new Font("Arial", Font.BOLD, 10));
			numberaxisX.setTickLabelFont(new Font("Arial", Font.BOLD, 10));
			numberaxisX.setPositiveArrowVisible(false);
			if (XLabel != null
					&& (XLabel.equals("KW") || XLabel.equals("Kurbelwinkel [°KW]"))) {
				numberaxisX.setTickUnit(new NumberTickUnit(30));
			}

			/** Setting of the Y Axis **/
			if (nbr_of_curve > 0) {
				NumberAxis numberaxis0 = (NumberAxis) xyplot.getRangeAxis();
				numberaxis0.setLabelPaint(Color.red);
				numberaxis0.setTickLabelPaint(Color.red);
				numberaxis0.setLabelFont(new Font("Arial", Font.BOLD, 10));
				numberaxis0.setTickLabelFont(new Font("Arial", Font.BOLD, 10));
				numberaxis0.setPositiveArrowVisible(false);

				if (nbr_of_curve > 1) {
					NumberAxis numberaxis1 = new NumberAxis();
					numberaxis1.setLabelPaint(Color.blue);
					numberaxis1.setTickLabelPaint(Color.blue);
					numberaxis1.setLabelFont(new Font("Arial", Font.PLAIN, 10));
					numberaxis1.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
					numberaxis1.setPositiveArrowVisible(false);
					numberaxis1.setLabel(YLabel2[0]);

					xyplot.setRangeAxis(1, numberaxis1);
					xyplot.setDataset(1, data_2);
					xyplot.setRangeAxis(1, numberaxis1);
					xyplot.mapDatasetToRangeAxis(1, 1);
					XYItemRenderer xyitemrenderer = new StandardXYItemRenderer();
					// xyitemrenderer.setSeriesPaint(0, new Color(16,78,139));
					xyitemrenderer.setSeriesPaint(0, Color.blue);

					XYToolTipGenerator generator = new StandardXYToolTipGenerator();
					xyitemrenderer.setToolTipGenerator(generator);
					xyplot.setRenderer(1, xyitemrenderer);

					if (nbr_of_curve > 2) {
						NumberAxis numberaxis2 = new NumberAxis();
						numberaxis2.setLabelPaint(Color.cyan);
						numberaxis2.setTickLabelPaint(Color.cyan);
						numberaxis2.setLabelFont(new Font("Arial", Font.PLAIN, 10));
						numberaxis2.setTickLabelFont(new Font("Arial", Font.PLAIN,
								10));
						numberaxis2.setPositiveArrowVisible(false);
						numberaxis2.setLabel(YLabel3[0]);

						xyplot.setRangeAxis(2, numberaxis2);
						xyplot.setDataset(2, data_3);
						xyplot.setRangeAxis(2, numberaxis2);
						xyplot.setRangeAxisLocation(1,
								org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT);
						xyplot.mapDatasetToRangeAxis(2, 2);
						XYItemRenderer xyitemrenderer2 = new StandardXYItemRenderer();
						// xyitemrenderer.setSeriesPaint(0, new Color(16,78,139));
						xyitemrenderer2.setSeriesPaint(0, Color.cyan);

						XYToolTipGenerator generator2 = new StandardXYToolTipGenerator();
						xyitemrenderer2.setToolTipGenerator(generator2);
						xyplot.setRenderer(2, xyitemrenderer2);

						if (nbr_of_curve > 3) {
							NumberAxis numberaxis3 = new NumberAxis();
							numberaxis3.setLabelPaint(Color.magenta);
							numberaxis3.setTickLabelPaint(Color.magenta);
							numberaxis3.setLabelFont(new Font("Arial", Font.PLAIN,
									10));
							numberaxis3.setTickLabelFont(new Font("Arial",
									Font.PLAIN, 10));
							numberaxis3.setPositiveArrowVisible(false);
							numberaxis3.setLabel(YLabel4[0]);

							xyplot.setRangeAxis(3, numberaxis3);
							xyplot.setDataset(3, data_4);
							xyplot.setRangeAxis(3, numberaxis3);
							xyplot.mapDatasetToRangeAxis(3, 3);
							XYItemRenderer xyitemrenderer3 = new StandardXYItemRenderer();
							// xyitemrenderer.setSeriesPaint(0, new
							// Color(16,78,139));
							xyitemrenderer3.setSeriesPaint(0, Color.magenta);

							XYToolTipGenerator generator3 = new StandardXYToolTipGenerator();
							xyitemrenderer3.setToolTipGenerator(generator3);
							xyplot.setRenderer(3, xyitemrenderer3);

						}
					}
				}
			}
	    		//writeChartToPDF(chart, 600, 400, "src/bremoGraphik/pdf/Verlauf von "+Titel+".pdf");
	    		
	    		final ChartPanel chartPanel = new ChartPanel(chart);
	    		return chartPanel;
	    	}

}
