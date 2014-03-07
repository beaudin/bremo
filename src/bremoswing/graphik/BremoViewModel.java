package bremoswing.graphik;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import javax.swing.JOptionPane;

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

import bremoswing.util.FertigMeldungFrame;
/**
 * @author Steve Ngueneko
 *
 */
public class BremoViewModel implements Observable{
	
	private Observer bremoViewObserver ;
	/**
	 *  ItemStore is a store place for the model. At 1. place Item from Bremoview are stored and 2.place
	 *  Item from ItenChooseview are stored.
	 */
	private List<Object> ItemStore ;
	/**
	 * IndexStore is a Store place for the model to store index of item from the Output file.
	 * 1.place x_index
	 * 2.place list y_index 
	 */
	private ArrayList<int[]> IndexStore ;
	private File file;
	private File favsFile;
	private ChartPanel Chart ;
	
	
	
	public BremoViewModel() {
		ItemStore = new ArrayList<Object>();
		IndexStore = new ArrayList<int[]>();
		
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
	     * Load the Header of this file
	     * @param file
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
			ItemStore.add(0, allselectedItem);
			
		}

		public void storeItemFromChooseFrame(List<List<String>> selectedItemListlist) {
			ItemStore.add(1, selectedItemListlist);
			
		}

		public void createChart() throws IOException {
			if (ItemStore.get(0) == null && ItemStore.get(1)== null)
				throw new NullPointerException();
			
			String [] dataBremoview = getDataFromBremoView();
			List<List<String>> dataListFromchooseFrame = getDataFromChooseFrame();
			
			String nbr = dataBremoview[0];
			String log = dataBremoview[1];
			String x = dataBremoview[2];
						
			switch (nbr) {
			case "1":
				Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),log);
				break;
            case "2":
            	Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),dataListFromchooseFrame.get(1),log);
				break;
            case "3":
            	Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),dataListFromchooseFrame.get(1),dataListFromchooseFrame.get(2),log);
	            break;
			default:
				Chart = Build_Diagramm(x, dataListFromchooseFrame.get(0),dataListFromchooseFrame.get(1),dataListFromchooseFrame.get(2),dataListFromchooseFrame.get(3),log);
	            break;
			}
			
			notifyObserver(Chart);

		}
		/**
		 * get Data from BremoView stored in ItemStore Variable
		 * @return
		 */
		public String [] getDataFromBremoView() {
			return (String[]) ItemStore.get(0);
			
		}
		
		/**
		 * get Data from ChooseFrame stored in ItemStore Variable
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public List<List<String>> getDataFromChooseFrame() {
			return (List<List<String>>) ItemStore.get(1);
			
		}

		public void setInputFile(File file) throws IOException {
			this.file = file;
			notifyObserver(file.getName());
			notifyObserver(showHeader());
			if (checkFavsfile()){
				String log_param = readFavsFile();
				Diagramm_From_Index(log_param);
			}
		}
		
		private void Diagramm_From_Index(String Log_Parameter) throws IOException {
			
			switch (IndexStore.size()) {
			case 2:
				notifyObserver(Build_Diagramm(1,Log_Parameter));
				break;
            case 3:
            	notifyObserver(Build_Diagramm(2,Log_Parameter));
				break;
            case 4:
            	notifyObserver(Build_Diagramm(3,Log_Parameter));
				break;
            case 5:
            	notifyObserver(Build_Diagramm(4,Log_Parameter));
				break;
			default:
				notifyObserver("Error !!!");
				break;
			}
			
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
		     * Build Graphic from favorite Index their User whit number of axe
		     *@param axe 
		     *            nbr of axe 
		     * @return
		     * @throws IOException
		     */
		    public ChartPanel Build_Diagramm(int axe, String Log_param) throws IOException {
		    	BufferedReader br;
		            br = new  BufferedReader(new FileReader(file));
		            
		       
		       List<XYSeries> listSerie1 = null;
			   List<XYSeries> listSerie2 = null ;
			   List<XYSeries> listSerie3 = null;
			   List<XYSeries> listSerie4 = null;
			  
			   switch (axe) {
			case 1:
				listSerie1 = new ArrayList<XYSeries>();
				break;
	        case 2:
	        	listSerie1 = new ArrayList<XYSeries>();
	        	listSerie2 = new ArrayList<XYSeries>();
				break;
	        case 3:
	        	listSerie1 = new ArrayList<XYSeries>();
	        	listSerie2= new ArrayList<XYSeries>();
	        	listSerie3 = new ArrayList<XYSeries>();
				break;
	        case 4:
	        	listSerie1 = new ArrayList<XYSeries>();
	        	listSerie2 = new ArrayList<XYSeries>();
	        	listSerie3 = new ArrayList<XYSeries>();
	        	listSerie4 = new ArrayList<XYSeries>();
		        break;
			default:
				break;
			}
			   
		       String zeile = null;
		       
			   List<String> header = new ArrayList<String>();
			   String [] value = null;
			   
			   int [] y_index_1 = null;
			   int [] y_index_2 = null;
			   int [] y_index_3 = null;
			   int [] y_index_4 = null;
			   
			   
			   
			    int x_index = IndexStore.get(0)[0];
			    
			    if (axe > 0){
			    	 y_index_1 = IndexStore.get(1);
			        if (axe > 1){
			        	y_index_2 = IndexStore.get(2);
			        	if (axe > 2) {
			        		y_index_3 = IndexStore.get(3);
			        		if (axe > 3) {
				               y_index_4 = IndexStore.get(4);
			        		}
			        	}
			        }
			    }
				
				String  x_selected = "";
				
				String []  y_selected_1 = null;
				String []  y_selected_2 = null;
				String []  y_selected_3 = null;
				String []  y_selected_4 = null;
				
				if (axe > 0){
					 y_selected_1 = new String[y_index_1.length];
			        if (axe > 1){
			        	 y_selected_2 = new String[y_index_2.length];
			        	if (axe > 2) {
			        		 y_selected_3 = new String[y_index_3.length];
			        		if (axe > 3) {
			        			 y_selected_4 = new String[y_index_4.length];
			        		}
			        	}
			        }
			    }
				
				if ((zeile = br.readLine()) != null) {
					
					header = Arrays.asList(zeile.split("\t"));
					
					x_selected = header.get(x_index);
					
					if (axe > 0){
						for (int i = 0 ; i < y_index_1.length; i++) {
							y_selected_1[i] = header.get(y_index_1[i]);
						}
						for (int i = 0; i < y_selected_1.length; i++){
					        listSerie1.add(new XYSeries(y_selected_1[i],false,true));
					    }
				        if (axe > 1){
				        	for (int i = 0 ; i < y_index_2.length; i++) {
								y_selected_2[i] = header.get(y_index_2[i]);
							}
							for (int i = 0; i < y_selected_2.length; i++){
						        listSerie2.add(new XYSeries(y_selected_2[i],false,true));
						    }
				        	if (axe > 2) {
				        		for (int i = 0 ; i < y_index_3.length; i++) {
									y_selected_3[i] = header.get(y_index_3[i]);
								}
								for (int i = 0; i < y_selected_3.length; i++){
							        listSerie3.add(new XYSeries(y_selected_3[i],false,true));
							    }
				        		if (axe > 3) {
				        			for (int i = 0 ; i < y_index_4.length; i++) {
				    					y_selected_4[i] = header.get(y_index_4[i]);
				    				}
				    				for (int i = 0; i < y_selected_4.length; i++){
				    			        listSerie4.add(new XYSeries(y_selected_4[i],false,true));
				    			    }
				        		}
				        	}
				        }
				    }
				}
		    
		        XYDataset datasetVerlauf1 = null;
		        XYDataset datasetVerlauf2 = null ;
		        XYDataset datasetVerlauf3 = null;
		        XYDataset datasetVerlauf4 = null;
		        			
				while ((zeile = br.readLine()) != null){
					value = zeile.split(" ");
					
					if (axe > 0){
						for (int i = 0 ; i < y_index_1.length; i++){
							switch (Log_param) {
							case "No Log":
								listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index_1[i]]));
								break;
		                    case "Log X-Axe":
		                    	listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index_1[i]]));
								break;
		                   case "Log Y-Axe":
		                	   listSerie1.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index_1[i]])));
								break;
		                   case "Double Log":
		                	   listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index_1[i]])));
			                    break;
							default:
								break;
							}
						}
				        if (axe > 1){
				        	for (int i = 0 ; i < y_index_2.length; i++){
				        		switch (Log_param) {
								case "No Log":
									listSerie2.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index_2[i]]));
									break;
			                    case "Log X-Axe":
			                    	listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index_2[i]]));
									break;
			                   case "Log Y-Axe":
			                	   listSerie2.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index_2[i]])));
									break;
			                   case "Double Log":
			                	   listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index_2[i]])));
				                    break;
								default:
									break;
								}
				        	}
				        	if (axe > 2) {
				        	    for (int i = 0 ; i < y_index_3.length; i++){
				        	    	switch (Log_param) {
									case "No Log":
										listSerie3.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index_3[i]]));
										break;
				                    case "Log X-Axe":
				                    	listSerie3.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index_3[i]]));
										break;
				                   case "Log Y-Axe":
				                	   listSerie3.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index_3[i]])));
										break;
				                   case "Double Log":
				                	   listSerie3.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index_3[i]])));
					                    break;
									default:
										break;
									}
				        	    }
				        		if (axe > 3) {
				        			for (int i = 0 ; i < y_index_4.length; i++){
				        				switch (Log_param) {
				    					case "No Log":
				    						listSerie4.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index_4[i]]));
				    						break;
				                        case "Log X-Axe":
				                        	listSerie4.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index_4[i]]));
				    						break;
				                       case "Log Y-Axe":
				                    	   listSerie4.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index_4[i]])));
				    						break;
				                       case "Double Log":
				                    	   listSerie4.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index_4[i]])));
				    	                    break;
				    					default:
				    						break;
				    					}
				        			}
				        		}
				        	}
				        }
					}
				}	
				
				if (axe > 0){
					XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
					for (int i = 0; i < listSerie1.size(); i++){
						collectionVerlauf.addSeries(listSerie1.get(i));
					}
					datasetVerlauf1 = collectionVerlauf;
			        if (axe > 1){
			        	XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
						for (int i = 0; i < listSerie2.size(); i++){
							collectionVerlauf2.addSeries(listSerie2.get(i));
						}
						datasetVerlauf2 = collectionVerlauf2;
			        	if (axe > 2) {
			        		XYSeriesCollection collectionVerlauf3 = new XYSeriesCollection();
			    			for (int i = 0; i < listSerie3.size(); i++){
			    				collectionVerlauf3.addSeries(listSerie3.get(i));
			    			}
			    			datasetVerlauf3 = collectionVerlauf3;
			        		if (axe > 3) {
			        			XYSeriesCollection collectionVerlauf4 = new XYSeriesCollection();
			        			for (int i = 0; i < listSerie4.size(); i++){
			        				collectionVerlauf4.addSeries(listSerie4.get(i));
			        			}
			        			datasetVerlauf4 = collectionVerlauf4;
			        		}
			        	}
			        }
			    }
				
				ChartPanel chartVerlauf = null;
				
				String [] yLabel1 =  y_selected_1;
				String [] yLabel2 =  y_selected_2;
				String [] yLabel3 =  y_selected_3;
				String [] yLabel4 =  y_selected_4;
				
				if (axe > 0){
					for (int i = 0; i < yLabel1.length ; i++ ){
						yLabel1[i] = yLabel1[i].split(" ")[1];
		       		}
			        if (axe > 1){
			        	for (int i = 0; i < yLabel2.length ; i++ ){
							yLabel2[i] = yLabel2[i].split(" ")[1];
			       		}
			        	if (axe > 2) {
			        		for (int i = 0; i < yLabel3.length ; i++ ){
			    				yLabel3[i] = yLabel3[i].split(" ")[1];
			           		}
			        		if (axe > 3) {
			        			for (int i = 0; i < yLabel4.length ; i++ ){
			        				yLabel4[i] = yLabel4[i].split(" ")[1];
			               		}
			        		}
			        	}
			        }
			    }
				
				 switch (axe) {
					case 1:
						chartVerlauf = createChartPanel(null, x_selected, yLabel1 , datasetVerlauf1);
						break;
			        case 2:
			        	chartVerlauf = createChartPanel(null, x_selected, yLabel1, yLabel2, datasetVerlauf1, datasetVerlauf2);
			    		break;
			        case 3:
			        	chartVerlauf = createChartPanel(null, x_selected, yLabel1, yLabel2, yLabel3, datasetVerlauf1, datasetVerlauf2, datasetVerlauf3);
			    		break;
			        case 4:
			        	chartVerlauf = createChartPanel(null, x_selected, yLabel1, yLabel2, yLabel3, yLabel4, datasetVerlauf1, datasetVerlauf2, datasetVerlauf3, datasetVerlauf4);
			    		break;
					default:
						break;
					}
				 
				br.close();
//				selected = selected.substring(0, selected.indexOf("["));
//				chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected+" verlauf"));
				
				return chartVerlauf;
		    	
		    }
		    
		/**
	     * Build Graphic for the 1  Curve
	     * @return
	     * @throws IOException
	     */
	    public ChartPanel Build_Diagramm(String x_selected, List<String> y_selected, String log) throws IOException {
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
			indexStoreReset();
			IndexStore.add(0, new int[] {x_index});
			IndexStore.add(1, y_index);
			
			while ((zeile = br.readLine()) != null){
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index.length; i++){
					
				switch (log) {
					case "No Log":
						listSerie.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index[i]])));
						break;
                   case "Double Log":
                	   listSerie.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index[i]])));
	                    break;
					default:
						break;
					}
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
	    public ChartPanel Build_Diagramm(String x_selected,List<String> y_selected1, List<String> y_selected2, String log) throws IOException {
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
		indexStoreReset();
		IndexStore.add(0, new int[] {x_index});
		IndexStore.add(1, y_index1);
		IndexStore.add(2, y_index2);
		
		while ((zeile = br.readLine()) != null) {
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index1.length; i++){
					switch (log) {
					case "No Log":
						listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index1[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index1[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie1.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index1[i]])));
						break;
                   case "Double Log":
                	   listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index1[i]])));
	                    break;
					default:
						break;
					}
				}
				for (int i = 0 ; i < y_index2.length; i++){
					switch (log) {
					case "No Log":
						listSerie2.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index2[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index2[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie2.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index2[i]])));
						break;
                   case "Double Log":
                	   listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index2[i]])));
	                    break;
					default:
						break;
					}
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
	    public ChartPanel Build_Diagramm(String x_selected, List<String> y_selected1, List<String> y_selected2, List<String> y_selected3, String log) throws IOException {
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
		indexStoreReset();
		IndexStore.add(0, new int[] {x_index});
		IndexStore.add(1, y_index1);
		IndexStore.add(2, y_index2);
		IndexStore.add(3, y_index3);
		
		while ((zeile = br.readLine()) != null) {
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index1.length; i++){
					switch (log) {
					case "No Log":
						listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index1[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index1[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie1.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index1[i]])));
						break;
                   case "Double Log":
                	   listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index1[i]])));
	                    break;
					default:
						break;
					}
				}
				for (int i = 0 ; i < y_index2.length; i++){
					switch (log) {
					case "No Log":
						listSerie2.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index2[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index2[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie2.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index2[i]])));
						break;
                   case "Double Log":
                	   listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index2[i]])));
	                    break;
					default:
						break;
					}
				}
				for (int i = 0 ; i < y_index3.length; i++){
					switch (log) {
					case "No Log":
						listSerie3.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index3[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie3.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index3[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie3.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index3[i]])));
						break;
                   case "Double Log":
                	   listSerie3.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index3[i]])));
	                    break;
					default:
						break;
					}
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
	    public ChartPanel Build_Diagramm(String x_selected, List<String> y_selected1, List<String> y_selected2, List<String> y_selected3, List<String> y_selected4, String log) throws IOException {
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
				if (y_selected3.contains(header.get(i))){
					y_index3[y_selected3.indexOf(header.get(i))] = i;
				}
				if (y_selected4.contains(header.get(i))){
					y_index4[y_selected4.indexOf(header.get(i))] = i;
				}
			}
			
		}
		indexStoreReset();
		IndexStore.add(0, new int[] {x_index});
		IndexStore.add(1, y_index1);
		IndexStore.add(2, y_index2);
		IndexStore.add(3, y_index3);
		IndexStore.add(4, y_index3);
		
		while ((zeile = br.readLine()) != null) {
				value = zeile.split(" ");
				
				for (int i = 0 ; i < y_index1.length; i++){
					switch (log) {
					case "No Log":
						listSerie1.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index1[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index1[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie1.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index1[i]])));
						break;
                   case "Double Log":
                	   listSerie1.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index1[i]])));
	                    break;
					default:
						break;
					}
				}
				for (int i = 0 ; i < y_index2.length; i++){
					switch (log) {
					case "No Log":
						listSerie2.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index2[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index2[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie2.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index2[i]])));
						break;
                   case "Double Log":
                	   listSerie2.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index2[i]])));
	                    break;
					default:
						break;
					}
				}
				for (int i = 0 ; i < y_index3.length; i++){
					switch (log) {
					case "No Log":
						listSerie3.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index3[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie3.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index3[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie3.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index3[i]])));
						break;
                   case "Double Log":
                	   listSerie3.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index3[i]])));
	                    break;
					default:
						break;
					}
				}
				for (int i = 0 ; i < y_index4.length; i++){
					switch (log) {
					case "No Log":
						listSerie4.get(i).add(Double.parseDouble(value[x_index]), Double.parseDouble(value[y_index4[i]]));
						break;
                    case "Log X-Axe":
                    	listSerie4.get(i).add(Math.log(Double.parseDouble(value[x_index])), Double.parseDouble(value[y_index4[i]]));
						break;
                   case "Log Y-Axe":
                	   listSerie4.get(i).add(Double.parseDouble(value[x_index]), Math.log(Double.parseDouble(value[y_index4[i]])));
						break;
                   case "Double Log":
                	   listSerie4.get(i).add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index4[i]])));
	                    break;
					default:
						break;
					}
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
		
        /**
         * save the index of selected item  as favs for the user
         */
		public void saveFavs() {
			FertigMeldungFrame info;
			try {
		          File f = new File(file.getParent()+"/FAVS_"+file.getName());
		          BufferedWriter output = new BufferedWriter(new FileWriter(f));
		          output.write(file.getName());
		          output.newLine();
		          output.write("Log := "+ getDataFromBremoView()[1]);
		          output.newLine();
		          output.write("x_index := "+ Arrays.toString(IndexStore.get(0)));
		          output.newLine();
		          for(int i = 1 ; i < IndexStore.size(); i++ ) {
		        	  output.write("y_index_"+i+ " := "+ Arrays.toString(IndexStore.get(i)));
		        	  output.newLine();
		          }  
		          output.close();
		          info = new FertigMeldungFrame("favorite", "index-Data has been successfully saved as Favorite", JOptionPane.INFORMATION_MESSAGE);
		        } catch ( IOException e ) {
		           e.printStackTrace();
		           info = new FertigMeldungFrame("favorite", "an error occured while saving favorites", JOptionPane.ERROR_MESSAGE);
		        }
		    }
		
		/**
		 * check if the favs file of the input file exist
		 */
		public boolean checkFavsfile() {
			
			File  f = new File(file.getParent()+"/FAVS_"+file.getName());
			if (f.exists()) {
				favsFile = f;
				return true;
				}
			else
				return false;
		}
		
		/**
		 * remove all element of IndexStore
		 */
		public void indexStoreReset() {
			IndexStore.clear();
		}
		
		public String  readFavsFile() throws IOException {
			BufferedReader br = new BufferedReader(new FileReader(favsFile));
			indexStoreReset();
			String zeile = null;
			boolean bo = false;
			String log = "";
			String value = null;
			int []  index =  null;
			if ((zeile = br.readLine()) != null) {
			     bo = zeile.equals(file.getName());
			}
			if ((zeile = br.readLine()) != null) {
			     log =  zeile.split(" := ")[1];
			}
			
			while ((zeile = br.readLine()) != null && bo == true) {
				value = zeile.split(" := ")[1];
				value = value.replace("[", "");
				value = value.replace("]", "");
				value = value.replaceAll(" ", "");
				String [] val = value.split(",");
				index = new int [val.length] ;
				for (int i = 0 ; i < val.length; i++) {
					index [i] = Integer.parseInt(val[i]);
				}
				IndexStore.add(index);
			}
			br.close();
			
			return log;
		}
			
		

}
