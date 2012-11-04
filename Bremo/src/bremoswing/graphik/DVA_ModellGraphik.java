/**
 * 
 */
package bremoswing.graphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bremoExceptions.ParameterFileWrongInputException;



/**
 * Graphic representation of DVA Model
 * @author Ngueneko Steve
 *
 */
public class DVA_ModellGraphik extends BremoModellGraphik {

	private static final long serialVersionUID = 1206707166817890511L;

	/**
	 * @param file
	 * @throws ParameterFileWrongInputException
	 * @throws IOException
	 */
	public DVA_ModellGraphik(File file) throws ParameterFileWrongInputException, IOException {
		// TODO Auto-generated constructor stub
		super(file,"DVA");
		initComponentDVA();
	}

	private void initComponentDVA() throws IOException {
		// TODO Auto-generated method stub
      /**  GraphikPanel Processing  **************************************************/
        
        GraphikPanel.add(Druckverlauf());
		GraphikPanel.add(dQb_Qb_Qmax_Verlauf());
		GraphikPanel.add(WaermeStromDichteVerlauf());
		is_P_V_Diagramm = false;
		GraphikPanel.add(T_mittel_Verlauf());
		is_Verlustteilung_Digramm = false;
		GraphikPanel.revalidate();
		
		/*******************************************************************************/
				
		setVisible(true);
        pack();
	}

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellBerechnung#graphik2ComboBox1ActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	void graphik2ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("p-V-Digramm")) {
    		if (! is_P_V_Diagramm) {
    			graphik2ComboBox2.removeAllItems();
    			GraphikPanel.remove(2);
    			GraphikPanel.add(p_V_Diagramm(),2);
    			is_P_V_Diagramm = true;
    			GraphikPanel.revalidate();
    		}
    	}
    	else if (selected.equals("Andere Diagramm")){
    		   if (is_P_V_Diagramm) {
    		       graphik2ComboBox2.removeAllItems();
    		       GraphikPanel.remove(2);
    			   GraphikPanel.add(WaermeStromDichteVerlauf(),2);
    			   is_P_V_Diagramm = false;
    			   GraphikPanel.revalidate();
    	       }
    	}

	}

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellBerechnung#graphik3ComboBox1ActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("Andere Diagramm")) {
    		if (is_Verlustteilung_Digramm) {
    			graphik3ComboBox2.removeAllItems();
    			GraphikPanel.remove(3);
    			GraphikPanel.add(T_mittel_Verlauf(),3);
    			GraphikPanel.revalidate();
    			
    		}
    	}
    	else if (selected.equals("Verlustteilung BalkenDiagramm")){
    		    if (! is_Verlustteilung_Digramm) {
    		    	if (! is_verlust_berechnen) {
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
    		    }
    		    else {
    		    		graphik3ComboBox2.removeAllItems();
                        graphik3ComboBox2.addItem("pmi-Werte");
                        graphik3ComboBox2.addItem("Wirkungsgrade");
    		    		is_Verlustteilung_Digramm = true;
    		    		GraphikPanel.revalidate();

    		    	}
    		}
    	}

	}
	
	@Override
	void graphik2ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
    	String selected = null;
    	 try{
   		  
       	  selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
       	  
       	  } catch (NullPointerException npe) {
       		  
       	  }
    	 if (selected != null && !is_P_V_Diagramm) {
    	      GraphikPanel.remove(2);
		      GraphikPanel.add(Auswahl_Diagramm(selected),2);
	          GraphikPanel.revalidate();
              GroupPanel.revalidate();
    	 }
	}

	@Override
	void graphik3ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = null;
   	 try{
  		  
      	  selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
      	
      	  if (selected.equals("Wirkungsgrade")){
			is_Wirkungsgrade_Diagramm = true ;
		} else {
			is_Wirkungsgrade_Diagramm = false ;
		}
      	  } catch (NullPointerException npe) {
      		  
      	  }
  
	  if ((selected.equals("pmi-Werte")|| selected.equals("Wirkungsgrade")) && is_Verlustteilung_Digramm) {
		GraphikPanel.remove(3);
		try {
			GraphikPanel.add(Verlustteilung(selected),3);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		is_P_V_Diagramm = false;

		GraphikPanel.revalidate();
		GroupPanel.revalidate();
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
			GroupPanel.revalidate();
  	}
	}


	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellBerechnung#Druckverlauf()
	 */
	@Override
	ChartPanel Druckverlauf() throws IOException {
		// TODO Auto-generated method stub
		
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        XYSeries serie1 =  new XYSeries("p_soll [bar]") ;
        XYSeries serie2 = new XYSeries("p [bar] ") ;
        XYDataset datasetDruckVerlauf ;
        
        String zeile = null;
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
				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  p_soll
				serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
			}
			else {
				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
				serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
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

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellBerechnung#p_V_Diagramm()
	 */
	@Override
	ChartPanel p_V_Diagramm() throws IOException {
		// TODO Auto-generated method stub

        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        //XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [log(m^3)]  Y -> [log(p_soll)]", false) ;
        XYSeries  serie1 = new XYSeries("p-V-Diagramm (p_soll)", false) ;
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
			serie1.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_2])));// log(Brennraumvolumen)  log(p_soll)
			serie2.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_3])));//  log(Brennraumvolumen)  log(p)
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		collectionVerlauf.addSeries(serie2);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
	
		br.close();
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm P_soll und P"));
		
		return chartVerlauf;
	}
	
	/**
     * Build Graphic for the WaermeStromDichte Curve
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
        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/DVA_"+inputfile.getName()));
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
}
