/**
 * 
 */
package bremoswing.graphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
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
 * Graphic representation of APR Model
 * @author Ngueneko Steve
 *
 */
public class APR_ModellGraphik extends BremoModellGraphik {
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String [] header ;
	
	/**
	 * @param file
	 * @param berechnungModell
	 * @throws ParameterFileWrongInputException
	 * @throws IOException
	 */
	public APR_ModellGraphik(File file)throws ParameterFileWrongInputException, IOException {
		super(file, "APR");
		initComponentAPR();
		// TODO Auto-generated constructor stub
	}

	private void initComponentAPR() throws IOException {
		// TODO Auto-generated method stub
		/**  GraphikPanel Processing  **************************************************/
		
		GraphikPanel.add(Druckverlauf());
		GraphikPanel.add(dQb_Qb_Qmax_Verlauf());
		GraphikPanel.add(Auswahl_Diagramm("Brennraumvolumen [m3]"));
		is_P_V_Diagramm = false;
		GraphikPanel.add(Auswahl_Diagramm("Alpha [W/(m^2K)]"));
		is_Verlustteilung_Digramm = false;
		header = showHeaderOutFile();
		addItemToComboBox(graphik2ComboBox2, header);
		addItemToComboBox(graphik3ComboBox2, header);
		GraphikPanel.revalidate();
		graphik2ComboBox2.setSelectedItem("Brennraumvolumen [m3]");
		graphik3ComboBox2.setSelectedItem("Alpha [W/(m^2K)]");
		/*******************************************************************************/
		
		setVisible(true);
        pack();
	}

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellGraphik#graphik2ComboBox1ActionPerformed(java.awt.event.ActionEvent)
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
    			   addItemToComboBox(graphik2ComboBox2, header);
    		       GraphikPanel.remove(2);
    			   GraphikPanel.add(Auswahl_Diagramm("Brennraumvolumen [m3]"),2);
    			   graphik2ComboBox2.setSelectedItem("Brennraumvolumen [m3]");
    			   is_P_V_Diagramm = false;
    			   GraphikPanel.revalidate();
    	       }
    	}
	}
 
	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellGraphik#graphik2ComboBox2ActionPerformed(java.awt.event.ActionEvent)
	 */
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

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellGraphik#graphik3ComboBox1ActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("Andere Diagramm")) {
    		if (is_Verlustteilung_Digramm) {
                addItemToComboBox(graphik3ComboBox2, header);
    			GraphikPanel.remove(3);
    			GraphikPanel.add(Auswahl_Diagramm("Alpha [W/(m^2K)]"),3);
    			graphik3ComboBox2.setSelectedItem("Alpha [W/(m^2K)]");
    			GraphikPanel.revalidate();
    			is_Verlustteilung_Digramm = false;
    			
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
    		    		String [] item  = new String [] {"pmi-Werte","Wirkungsgrade"};
    		    		addItemToComboBox(graphik3ComboBox2, item);
                        is_Verlustteilung_Digramm = true;
                        GraphikPanel.remove(3);
            			try {
            			    	GraphikPanel.add(Verlustteilung("pmi-Werte"),3);
            				}
            			catch (IOException e1) {
            					// TODO Auto-generated catch block
            					e1.printStackTrace();
            				}
            			is_P_V_Diagramm = false;
    		    		GraphikPanel.revalidate();

    		    	}
    		}
    	}


	}

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellGraphik#graphik3ComboBox2ActionPerformed(java.awt.event.ActionEvent)
	 */
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
	      	  
	      	  } catch (NullPointerException npe) {
	      		  
	      	  }
	}

	/* (non-Javadoc)
	 * @see bremoswing.graphik.BremoModellGraphik#Druckverlauf()
	 */
	@Override
	ChartPanel Druckverlauf() throws IOException {
		// TODO Auto-generated method stub
		 BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
	        XYSeries serie2 = new XYSeries("p [bar] ") ;
	        XYDataset datasetDruckVerlauf ;
	        
	        String zeile = null;
			String [] header = null;
			String [] value = null;
			int index_2 = -1;
			if ((zeile = br.readLine()) != null){
				header = zeile.split("\t");
				for (int i = 0; i < header.length; i++){
					if (header[i].equals("p [bar]")){
						index_2 = i;
					}
				}
			}
			while ((zeile = br.readLine()) != null){
				value = zeile.split(" ");
				if (zeit_oder_KW.equals("KW")) {
					serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
				}
				else {
					serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
				}
			}
			XYSeriesCollection collectionDruckVerlauf = new XYSeriesCollection();
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
	 * @see bremoswing.graphik.BremoModellGraphik#p_V_Diagramm()
	 */
	@Override
	ChartPanel p_V_Diagramm() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
        //XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [log(m^3)]  Y -> [log(p_soll)]", false) ;
        XYSeries serie2 = new XYSeries("p-V-Diagramm (p)", false) ;
        XYDataset datasetVerlauf;
        String zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		int index_3 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("Brennraumvolumen [m3]")){
					index_1 = i;
				}
				else if (header[i].equals("p [bar]")){
					index_3 = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			serie2.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_3])));//  log(Brennraumvolumen)  log(p)
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie2);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
	
		br.close();
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm P"));
		
		return chartVerlauf;
	}
	
	public String [] showHeaderOutFile () throws IOException {
	 		
	 		BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/APR_"+inputfile.getName()));
	        
	         String zeile = null;
	 		String [] header = null;
	 		if ((zeile = br.readLine()) != null){
	 			header = zeile.split("\t");		
	 		}
	 		br.close();
	 		return header;
	 	}

}
