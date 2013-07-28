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
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bremoExceptions.ParameterFileWrongInputException;

public class LWA_Graphik extends BremoModellGraphik{

	private static final long serialVersionUID = -673633318746544253L;
    private String berechnungModell_Parent;
    JPanel OberePanel ;
    JPanel UnterePanel ;
    
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
		
		OberePanel = new JPanel();
		//OberePanel.setBackground(Color.red);
		//OberePanel.setBorder(BorderFactory.createTitledBorder("Panel_1"));
		
		GridLayout gl1 = new GridLayout(1,1);
		OberePanel.setLayout(gl1);
		OberePanel.add(Druckverlauf());
		GraphikPanel.add(OberePanel);
		
		UnterePanel = new JPanel();
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
		UnterePanel.add(Auswahl_Diagramm("T [K]"));
		is_Verlustteilung_Digramm = false;
		String path = inputfile.getName().replace(".txt", "_ERGEBNISSE_LW.txt");
		header = showHeaderOutFile(berechnungModell+"_"+path);
		addItemToComboBox(graphik3ComboBox2, header);
		graphik3ComboBox2.setSelectedItem("T [K]");
		GraphikPanel.revalidate();
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
	    			UnterePanel.remove(0);
	    			UnterePanel.add(p_V_Diagramm(),0);
	    			is_P_V_Diagramm = true;
	    			UnterePanel.revalidate();
	    		}
	    	}
	    	else if (selected.equals("Andere Diagramm")){
	    		   if (is_P_V_Diagramm) {
	    			   addItemToComboBox(graphik2ComboBox2, header);
	    		       UnterePanel.remove(0);
	    			   UnterePanel.add(Auswahl_Diagramm("pSaug [bar]"),0);
	    			   graphik2ComboBox2.setSelectedItem("pSaug [bar]");
	    			   is_P_V_Diagramm = false;
	    			   UnterePanel.revalidate();
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
	   	      UnterePanel.remove(0);
			  UnterePanel.add(Auswahl_Diagramm(selected),0);
		      UnterePanel.revalidate();
	          GroupPanel.revalidate();
	   	 }
	}

	@Override
	void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
		// TODO Auto-generated method stub
		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
    	if (selected.equals("Andere Diagramm")) {
    		if (is_Verlustteilung_Digramm) {
                addItemToComboBox(graphik3ComboBox2, header);
    			UnterePanel.remove(1);
    			UnterePanel.add(Auswahl_Diagramm("T [K]"),1);
    			graphik3ComboBox2.setSelectedItem("T [K]");
    			UnterePanel.revalidate();
    			is_Verlustteilung_Digramm = false;
    			
    		}
    	}
    	else if (selected.equals("Verlustteilung BalkenDiagramm")){
    		    if (! is_Verlustteilung_Digramm) {
    		    	if (! is_verlust_berechnen) {
    		    		UnterePanel.remove(1);
    		       URL url = getClass().getResource("/bremoswing/bild/balkenErrorIcon.png");
    			   ImageIcon icon = new ImageIcon(url);
    		      JLabel label =  new JLabel("Graphik nicht vorhanden !",icon, SwingConstants.LEFT);
    		      label.setFont(new java.awt.Font("Tahoma", 0, 16));
    		      label.setForeground(new Color(255,0,0));
    		      label.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
    		      JPanel panel = new JPanel();
    		      panel.add(BorderLayout.CENTER,label);
    		      panel.setBorder(BorderFactory.createTitledBorder("Verlustteilung Digramm"));
    		      UnterePanel.add(panel,1);
    		       //GraphikPanel.add(new JPanel());
    			   graphik3ComboBox2.removeAllItems();
    			   is_Verlustteilung_Digramm = true;
    			   UnterePanel.revalidate();
    		    }
    		    else {
    		    		String [] item  = new String [] {"pmi-Werte","Wirkungsgrade"};
    		    		addItemToComboBox(graphik3ComboBox2, item);
                        is_Verlustteilung_Digramm = true;
                        UnterePanel.remove(1);
            			try {
            				UnterePanel.add(Verlustteilung("pmi-Werte"),1);
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
				UnterePanel.remove(1);
				try {
					UnterePanel.add(Verlustteilung(selected),1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				is_P_V_Diagramm = false;
				UnterePanel.revalidate();
				GroupPanel.revalidate();
	      	}
	      	 
			  else if (selected != null) {
				  UnterePanel.remove(1);
					try {
						UnterePanel.add(Auswahl_Diagramm(selected),1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
					UnterePanel.revalidate();
					GroupPanel.revalidate();
		  	}
	      	  
	      	  } catch (NullPointerException npe) {
	      		  
	      	  }
	}

	@Override
	ChartPanel Druckverlauf() throws IOException {
		// TODO Auto-generated method stub
		  BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName().replace(".txt", "_ERGEBNISSE_LW.txt")));
	        XYSeries serie1 =  new XYSeries("pZyl-Berech_2") ;
	        XYSeries serie5 =  new XYSeries("pZyl-Berech_1") ;
	        XYSeries serie4 =  new XYSeries("p") ;
	        XYSeries serie2 = new XYSeries("Ventillehub_EV") ;
	        XYSeries serie3 = new XYSeries("Ventillehub_AV") ;
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
					
					double y_E = Double.parseDouble(value[index_2])*100;
					double y_A = Double.parseDouble(value[index_3])*100;
				
//					if (x <= 360){
//					    serie1.add(x,y);//  KW  p_soll
//					if (y_E > 0) {
//						 XYSerieCutLine(serie2,x,y_E);
//					}
//					if (y_A > 0) {
//						 XYSerieCutLine(serie3,x,y_A);
//					}
//					}else{
//					  if (x-720 < -165){
//				          serie5.add(x-720,y);
//					  }
//					  if (y_E > 0) {
//						  XYSerieCutLine(serie2,x-720,y_E);
//					  }
//					  if (y_A > 0) {
//						  XYSerieCutLine(serie3,x-720,y_A);
//					  }
//					}
					if (x <= 360){
						  serie1.add(x,y);//  KW  p_soll
						  if (y_E > 0 ) {
							    XYSerieCutLine(serie2,x-720,y_E);
							    XYSerieCutLine(serie2,x,y_E);
							}
						  if (y_A > 0 ) {
							  XYSerieCutLine(serie3,x,y_A);
							}
						}else{
						  if (x-720 < -165){
					          serie5.add(x-720,y);
						  }
						  if (y_E > 0) {
							  XYSerieCutLine(serie2,x-720,y_E);
						  }
						  if (y_A > 0) {
							  XYSerieCutLine(serie3,x-720,y_A);
						  }
						}
					//serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
				}
				else {
					//serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
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
						serie4.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  p_soll
						//serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
					}
					else {
						serie4.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
						//serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
					}
				}
				
			br.close();
			
			XYSeriesCollection collectionDruckVerlauf = new XYSeriesCollection();
			collectionDruckVerlauf.addSeries(serie4);
			collectionDruckVerlauf.addSeries(serie5);
			collectionDruckVerlauf.addSeries(serie1);
			XYSeriesCollection collectionDruckVerlauf2 = new XYSeriesCollection();
			collectionDruckVerlauf2.addSeries(serie3);
			collectionDruckVerlauf2.addSeries(serie2);
	
			datasetDruckVerlauf = collectionDruckVerlauf;
			datasetDruckVerlauf2 = collectionDruckVerlauf2;
			ChartPanel chartDruckVerlauf = null;
			if (zeit_oder_KW.equals("KW")){
				//chartDruckVerlauf = createChartPanel(null, "[°KW]",null , datasetDruckVerlauf);
				chartDruckVerlauf = createChartPanel(null, zeit_oder_KW , "Bar", "Cm" , datasetDruckVerlauf,datasetDruckVerlauf2);
			}
			else {
				//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
				chartDruckVerlauf = createChartPanel(null, null, null, null , datasetDruckVerlauf,datasetDruckVerlauf2);
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
	/**
	 * use  to cut a XYSerie
	 * @param s
	 * @param x
	 * @param y
	 */
	void XYSerieCutLine (XYSeries s , double x , double y ) {
		
//		if (s.getItemCount() >= 1) {
//			
//			double x_last = (Double) s.getX(s.getItemCount()-1);
//			
//			if (Math.abs(x-x_last) > 1){
//				s.add(x, null);
//				s.add(x, y);
//			}
//			else {
//				s.add(x, y);
//			}		
//		}
//		else {
//			s.add(x, y);
//		}
//		
		s.add(x, y);
		if (s.getItemCount() > 1) {
			int lenght = s.getItemCount() - 1;

			int index = s.indexOf(x);
			if (index == 0 ) {
				double x_r = (Double) s.getX(index + 1);
				if (Math.abs(x - x_r) > 1) {
					s.add(x + 0.5, null);
				}
			} else if (index == lenght) {
				double x_l = (Double) s.getX(index - 1);
				if (Math.abs(x - x_l) > 1) {
					s.add(x - 0.5, null);
				}
			} else {
				double x_r = (Double) s.getX(index + 1);
				if (Math.abs(x - x_r) > 1) {
					s.add(x + 0.5, null);
				}
				double x_l = (Double) s.getX(index - 1);
				if (Math.abs(x - x_l) > 1) {
					s.add(x - 0.5, null);
				}

			}
		}
	}

}
