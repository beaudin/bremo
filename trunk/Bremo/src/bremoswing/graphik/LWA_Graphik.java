package bremoswing.graphik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bremoExceptions.ParameterFileWrongInputException;

public class LWA_Graphik extends BremoModellGraphik{

	private static final long serialVersionUID = -673633318746544253L;
    private String berechnungModell_Parent;
    
	LWA_Graphik(File file, String berechnungModell, boolean is_RestgasVorgabe_LWA, BremoModellGraphik Parent)throws ParameterFileWrongInputException, IOException {
		super(file, berechnungModell, is_RestgasVorgabe_LWA);
	    if(Parent instanceof DVA_ModellGraphik) {
	    	berechnungModell_Parent = "DVA";
	    } else if (Parent instanceof APR_ModellGraphik){
	    	berechnungModell_Parent = "APR";
	    }
		initComponentLWA();		
		
	}

	private void initComponentLWA() throws IOException {
		// TODO Auto-generated method stub
		GridLayout gl = new GridLayout(2,1);
		GraphikPanel.setLayout(gl);
		
		JPanel OberePanel = new JPanel();
		//OberePanel.setBackground(Color.red);
		//OberePanel.setBorder(BorderFactory.createTitledBorder("Panel_1"));
		
		GridLayout gl1 = new GridLayout(1,1);
		OberePanel.setLayout(gl1);
		OberePanel.add(Druckverlauf());
		GraphikPanel.add(OberePanel);
		
		JPanel UnterePanel = new JPanel();
		GridLayout ul = new GridLayout(1,2);
		ul.setVgap(10);
		ul.setHgap(10);
		UnterePanel.setLayout(ul);
		//UnterePanel.setBackground(Color.white);
		//JPanel panel1= new JPanel();
		//panel1.setBackground(Color.green);
		//panel1.setBorder(BorderFactory.createTitledBorder("panel_2"));
		//JPanel panel2= new JPanel();
		//panel2.setBackground(Color.blue);
		//panel2.setBorder(BorderFactory.createTitledBorder("panel_3"));
		UnterePanel.add(p_V_Diagramm());
		is_P_V_Diagramm = true;
		graphik2ComboBox1.setSelectedItem("p-V-Digramm");
		UnterePanel.add(p_V_Diagramm());
		
		GraphikPanel.add(UnterePanel);
		
		
			
		setSize(1280,800);
		setVisible(true);
	}

	@Override
	void graphik2ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		try {
			String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
	    	if (selected.equals("p-V-Digramm")) {
	    		if (! is_P_V_Diagramm) {
	    			graphik2ComboBox2.removeAllItems();
	    			GraphikPanel.remove(1);
	    			GraphikPanel.add(p_V_Diagramm(),1);
	    			is_P_V_Diagramm = true;
	    			GraphikPanel.revalidate();
	    		}
	    	}
	    	else if (selected.equals("Andere Diagramm")){
	    		   if (is_P_V_Diagramm) {
	    			   addItemToComboBox(graphik2ComboBox2, header);
	    		       GraphikPanel.remove(1);
	    			   GraphikPanel.add(Auswahl_Diagramm("Brennraumvolumen [m3]"),1);
	    			   graphik2ComboBox2.setSelectedItem("Brennraumvolumen [m3]");
	    			   is_P_V_Diagramm = false;
	    			   GraphikPanel.revalidate();
	    	       }
	    	}
			
		} catch (Exception e){
			e.printStackTrace();
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
	   	      GraphikPanel.remove(1);
			  GraphikPanel.add(Auswahl_Diagramm(selected),1);
		      GraphikPanel.revalidate();
	          GroupPanel.revalidate();
	   	 }
	}

	@Override
	void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	void graphik3ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	ChartPanel Druckverlauf() throws IOException {
		// TODO Auto-generated method stub
		  BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName().replace(".txt", "_ERGEBNISSE_LW.txt")));
	        XYSeries serie1 =  new XYSeries("p + pZyl-Berech [bar]") ;
	        XYSeries serie2 = new XYSeries("Ventillehub_EV [m]") ;
	        XYSeries serie3 = new XYSeries("Ventillehub_AV [m]") ;
	        XYDataset datasetDruckVerlauf ;
	        XYDataset datasetDruckVerlauf2 ;
	        
	        String zeile = null;
			String [] header = null;
			String [] value = null;
			int index_1 = -1;
			int index_2 = -1;
			int index_3 = -1;
			if ((zeile = br.readLine()) != null){
				header = zeile.split("\t");
				for (int i = 0; i < header.length; i++){
					if (header[i].equals("pZyl_berech [bar]")){
						index_1 = i;
					}
					else if (header[i].equals("Hub_E [m]")){
						index_2 = i;
					}
					else if (header[i].equals("Hub_A [m]")){
						index_3 = i;
					}
				}
			}
			while ((zeile = br.readLine()) != null){
				value = zeile.split(" ");
				if (zeit_oder_KW.equals("KW")) {
					double x = Double.parseDouble(value[0]);
					double y = Double.parseDouble(value[index_1]);
					
					double y_E = Double.parseDouble(value[index_2]);
					double y_A = Double.parseDouble(value[index_3]);
				
					if (x <= 360){
					serie1.add(x,y);//  KW  p_soll
					serie2.add(x,y_E);
					serie3.add(x,y_A);
					}else{
					  if (x-720 < -165){
				          serie1.add(x-720,y);
					  }
					  serie2.add(x-720,y_E);
					  serie3.add(x-720,y_A);
					}
					//serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
				}
				else {
					serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
					//serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
				}
			}
			br.close();
		    br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell_Parent+"_"+inputfile.getName()));
		   
		        
		         zeile = null;
				 header = null;
				 value = null;
				 index_1 = -1;
				//int index_2 = -1;
				if ((zeile = br.readLine()) != null){
					header = zeile.split("\t");
					for (int i = 0; i < header.length; i++){
						if (header[i].equals("p [bar]")){
							index_1 = i;
						}
//						else if (header[i].equals("p [bar]")){
//							index_2 = i;
//						}
					}
				}
				while ((zeile = br.readLine()) != null){
					value = zeile.split(" ");
					if (zeit_oder_KW.equals("KW")) {
						serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  p_soll
						//serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
					}
					else {
						serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
						//serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
					}
				}
			br.close();
			
			XYSeriesCollection collectionDruckVerlauf = new XYSeriesCollection();
			collectionDruckVerlauf.addSeries(serie1);
			
			XYSeriesCollection collectionDruckVerlauf2 = new XYSeriesCollection();
			collectionDruckVerlauf2.addSeries(serie2);
			collectionDruckVerlauf2.addSeries(serie3);
	
			datasetDruckVerlauf = collectionDruckVerlauf;
			datasetDruckVerlauf2 = collectionDruckVerlauf2;
			ChartPanel chartDruckVerlauf = null;
			if (zeit_oder_KW.equals("KW")){
				//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
				chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf,datasetDruckVerlauf2);
			}
			else {
				//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
				chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf,datasetDruckVerlauf2);
	        }
			br.close();
			
			chartDruckVerlauf.setBorder(BorderFactory.createTitledBorder("Druckverlauf"));
			
			return chartDruckVerlauf;
	}

	@Override
	ChartPanel p_V_Diagramm() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell_Parent+"_"+inputfile.getName()));
        //XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [log(m^3)]  Y -> [log(p_soll)]", false) ;
       // XYSeries  serie1 = new XYSeries("p-V-Diagramm (p_soll)", false) ;
        XYSeries  serie2 = new XYSeries("p-V-Diagramm (p)", false) ;
        XYDataset datasetVerlauf;
        String zeile = null;
		String [] header = null;
		String [] value = null;
		int index_1 = -1;
		//int index_2 = -1;
		int index_3 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("Brennraumvolumen [m3]")){
					index_1 = i;
				}
//				else if (header[i].equals("p_soll [bar]")){
//					index_2 = i;
//				}
				else if (header[i].equals("p [bar]")){
					index_3 = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			//serie1.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_2])));// log(Brennraumvolumen)  log(p_soll)
			serie2.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_3])));//  log(Brennraumvolumen)  log(p)
		}
		br.close();
		
		 br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName().replace(".txt", "_ERGEBNISSE_LW.txt")));
		 //XYSeries  serie3 = new XYSeries("p-V-Diagramm (pZyl_mess)", false) ;
	     XYSeries  serie4 = new XYSeries("p-V-Diagramm (pZyl_berech)", false) ;
		 //XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [log(m^3)]  Y -> [log(p_soll)]", false) ;
        XYDataset datasetVerlauf2;
         zeile = null;
		 header = null;
		 value = null;
		 index_1 = -1;
		 //index_2 = -1;
		 index_3 = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals("V [m3]")){
					index_1 = i;
				}
//				else if (header[i].equals("pZyl_mess [bar]")){
//					index_2 = i;
//				}
				else if (header[i].equals("pZyl_berech [bar]")){
					index_3 = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			//serie3.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_2])));// log(V)  log(pZyl_mess)
			serie4.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_3])));//  log(V)  log(pZyl_berech)
		}
		
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		//collectionVerlauf.addSeries(serie1);
		collectionVerlauf.addSeries(serie2);
		XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
		//collectionVerlauf2.addSeries(serie3);
		collectionVerlauf.addSeries(serie4);
		datasetVerlauf = collectionVerlauf;
		//datasetVerlauf2 = collectionVerlauf2;
		ChartPanel chartVerlauf = null;
	    //chartVerlauf = createChartPanel(null, "[°KW]",null , datasetVerlauf);
		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
	
		br.close();
		chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm P_soll und P"));
		
		return chartVerlauf;
	}

}
