package bremoswing.graphik;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observer;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYDataset;

public abstract class AbstractModel implements Observable {
	
    File   inputfile ;
	String fileName ;
	String [] header ;
	
	private ArrayList<Observer> listObserver = new ArrayList<Observer>();
	
	public AbstractModel(File file) throws IOException {
		try {
			if (file == null)
				throw new FileNotFoundException();
			setInputFile(file);
			setFileName(file);
			setHeader(file);
		} catch (FileNotFoundException e) {
           System.err.println("File don´t exist at this path !");
		}
	}
	
	public void setInputFile(File file ) {
		inputfile = file;
	}
	
	public void setFileName (File file) {
		fileName = file.getName();
	}
	
	public void setHeader (File file) throws IOException {
		header = showHeaderOfFile(file);
	}
	/**
     * draw LineChart in  Panel with this Parameters:   
     * @param Titel
     * @param XLabel
     * @param YLabel
     * @param data      set von Daten zum zeichnen 
     * @return
     */
    public  ChartPanel createChartPanel(String Titel, String XLabel,String YLabel, XYDataset data ) {
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
    public   ChartPanel createChartPanel(String Titel, String XLabel,String YLabel1,String YLabel2, XYDataset data1 ,XYDataset data2) {
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
    public  ChartPanel createChartPanel(String Titel, String XLabel,String YLabel1,String YLabel2,String YLabel3, XYDataset data1 ,XYDataset data2, XYDataset data3) {
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
	public abstract ChartPanel createChartPanel (String Titel, String XLabel,String YLabel1,String YLabel2,String YLabel3,String YLabel4, XYDataset data1 ,XYDataset data2, XYDataset data3, XYDataset data4);
		
	/**
     * Build Graphic for the 1  Curve
     * @return
     * @throws IOException
     */
    public abstract ChartPanel auswahl_Diagramm(String selected) throws IOException ;
    

    /**
     * Build Graphic for 2 Curve
     * @return
     * @throws IOException
     */
    public  abstract ChartPanel auswahl_Diagramm(String selected1, String selected2) throws IOException ;
    

    /**
     * Build Graphic for 3  Curve
     * @return
     * @throws IOException
     */
    public abstract ChartPanel auswahl_Diagramm(String selected1, String selected2, String selected3) throws IOException ;
	
    /**
     * Build Graphic for 4  Curve
     * @return
     * @throws IOException
     */
    public abstract ChartPanel auswahl_Diagramm(String selected1, String selected2, String selected3, String selected4) throws IOException ; 
	
    /**
	 * add Observer to this Observable class
	 */
	public void addObserver(Observer obs) {
		    this.listObserver.add(obs);
		  }
	
	/**
	 * Notify Observer when a change of String  Occur
	 */
	public void notifyObserver(String str) {
		
		    for(Observer obs : listObserver)
		      obs.update(null, str);
		  }
	/**
	 * Notify Observer when a change of ChartPanel Occur
	 */
	public void notifyObserver(ChartPanel chart) {
		
		    for(Observer obs : listObserver)
		      obs.update(null, chart);
		  }
	/**
	 * Notify Observer when a change of Array of String Occur
	 */
	public void notifyObserver(String [] head) {
		
		    for(Observer obs : listObserver)
		      obs.update(null, head);
		  }
	/**
	 * Remove all the Observer	
	 */
	public void removeObserver() {
		    listObserver = new ArrayList<Observer>();
		  } 
	
	/**
	 * Reset the Model
	 */
	 public void Reset () {
		 fileName = null;
		 fileName = "";
		 header   = null;
	}
	
	
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

}
