///**
// * 
// */
//package bremoswing.graphik.old;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.event.ActionEvent;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.net.URL;
//
//import javax.swing.BorderFactory;
//import javax.swing.ImageIcon;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.SwingConstants;
//
//import org.jfree.chart.ChartPanel;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//
//import bremoExceptions.ParameterFileWrongInputException;
//
//
//
///**
// * Graphic representation of DVA Model
// * @author Ngueneko Steve
// *
// */
//public class DVA_ModellGraphik extends BremoModellGraphik {
//
//	private static final long serialVersionUID = 1206707166817890511L;
//
//	/**
//	 * @param file
//	 * @throws ParameterFileWrongInputException
//	 * @throws IOException
//	 */
//	public DVA_ModellGraphik(File file,boolean is_RestgasVorgabe_LWA) throws ParameterFileWrongInputException, IOException {
//		// TODO Auto-generated constructor stub
//		super(file,"DVA",is_RestgasVorgabe_LWA);
//		initComponentDVA();
//		PopUp();
//	}
//
//	private void initComponentDVA() throws IOException {
//		// TODO Auto-generated method stub
//      /**  GraphikPanel Processing  **************************************************/
//        header = showHeaderOutFile(berechnungModell+"_"+inputfile.getName());
//        GraphikPanel.add(Druckverlauf());
//		GraphikPanel.add(dQb_Qb_Qmax_Verlauf());
//		
//		Object [] random_item_charPanel = RandomChartPanel_Left();
//		GraphikPanel.add((ChartPanel)random_item_charPanel[1]);
//		graphik2ComboBox2.setSelectedItem((String)random_item_charPanel[0]);
//		is_P_V_Diagramm = false;
//		
//		random_item_charPanel = RandomChartPanel_Right();
//		GraphikPanel.add((ChartPanel)random_item_charPanel[1]);
//		graphik3ComboBox2.setSelectedItem((String)random_item_charPanel[0]);
//		is_Verlustteilung_Digramm = false;
//		GraphikPanel.revalidate();
//		
//		/*******************************************************************************/
//				
//		setVisible(true);
//        pack();
//	}
//
//	/* (non-Javadoc)
//	 * @see bremoswing.graphik.BremoModellBerechnung#graphik2ComboBox1ActionPerformed(java.awt.event.ActionEvent)
//	 */
//	@Override
//	void graphik2ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
//		// TODO Auto-generated method stub
//		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
//    	if (selected.equals("p-V-Digramm")) {
//    		if (! is_P_V_Diagramm) {
//    			graphik2ComboBox2.removeAllItems();
//    			GraphikPanel.remove(2);
//    			GraphikPanel.add(p_V_Diagramm(),2);
//    			is_P_V_Diagramm = true;
//    			GraphikPanel.revalidate();
//    		}
//    	}
//    	else if (selected.equals("Andere Diagramm")){
//    		   if (is_P_V_Diagramm) {
//    		       graphik2ComboBox2.removeAllItems();
//    		       GraphikPanel.remove(2);
//    		       Object [] random_item_charPanel = RandomChartPanel_Left();
//    			   GraphikPanel.add((ChartPanel)random_item_charPanel[1],2);
//    			   graphik2ComboBox2.setSelectedItem((String)random_item_charPanel[0]);
//    			   is_P_V_Diagramm = false;
//    			   GraphikPanel.revalidate();
//    	       }
//    	}
//
//	}
//
//	/* (non-Javadoc)
//	 * @see bremoswing.graphik.BremoModellBerechnung#graphik3ComboBox1ActionPerformed(java.awt.event.ActionEvent)
//	 */
//	@Override
//	void graphik3ComboBox1ActionPerformed(ActionEvent evt) throws IOException {
//		// TODO Auto-generated method stub
//		String selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
//    	if (selected.equals("Andere Diagramm")) {
//    		if (is_Verlustteilung_Digramm) {
//    			GraphikPanel.remove(3);
//    			Object [] random_item_charPanel = RandomChartPanel_Right();
// 			   GraphikPanel.add((ChartPanel)random_item_charPanel[1],3);
// 			   graphik3ComboBox2.setSelectedItem((String)random_item_charPanel[0]);
//    			GraphikPanel.revalidate();
//    			is_Verlustteilung_Digramm = false;
//    		}
//    	}
//    	else if (selected.equals("Verlustteilung BalkenDiagramm")){
//		    if (! is_Verlustteilung_Digramm) {
//		    	if (! is_verlust_berechnen) {
//		       GraphikPanel.remove(3);
//		       URL url = getClass().getResource("/bremoswing/bild/balkenErrorIcon.png");
//			   ImageIcon icon = new ImageIcon(url);
//		      JLabel label =  new JLabel("Verlustteilung wurde nicht berechnet!",icon, SwingConstants.LEFT);
//		      label.setFont(new java.awt.Font("Tahoma", 0, 16));
//		      label.setForeground(new Color(255,0,0));
//		      label.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
//		      JPanel panel = new JPanel();
//		      panel.add(BorderLayout.CENTER,label);
//		      panel.setBorder(BorderFactory.createTitledBorder("Verlustteilung Digramm"));
//		      GraphikPanel.add(panel,3);
//		       //GraphikPanel.add(new JPanel());
//			   graphik3ComboBox2.removeAllItems();
//			   is_Verlustteilung_Digramm = true;
//			   GraphikPanel.revalidate();
//		    }
//		    else {
//		    		String [] item  = new String [] {"pmi-Werte","Wirkungsgrade"};
//		    		addItemToComboBox(graphik3ComboBox2, item);
//                    is_Verlustteilung_Digramm = true;
//                    GraphikPanel.remove(3);
//        			try {
//        			    	GraphikPanel.add(Verlustteilung("pmi-Werte"),3);
//        				}
//        			catch (IOException e1) {
//        					// TODO Auto-generated catch block
//        					e1.printStackTrace();
//        				}
//        			is_P_V_Diagramm = false;
//       	    		GraphikPanel.revalidate();
//		    	}
//		}
//	}
//
//	}
//	
//	@Override
//	void graphik2ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
//		// TODO Auto-generated method stub
//    	String selected = null;
//    	 try{
//   		  
//       	  selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
//       	  
//       	  } catch (NullPointerException npe) {
//       		  
//       	  }
//		if (selected != null && !is_P_V_Diagramm) {
//			    GraphikPanel.remove(2);
//				GraphikPanel.add(Auswahl_Diagramm(selected), 2);
//				GraphikPanel.revalidate();
//				GroupPanel.revalidate();
//			
//		}
//	}
//
//	@Override
//	void graphik3ComboBox2ActionPerformed(ActionEvent evt) throws IOException {
//		// TODO Auto-generated method stub
//		String selected = null;
//	   	 try{
//	  		  
//	      	  selected = ((JComboBox) evt.getSource()).getSelectedItem().toString();
//	      	  
//	      	if (selected.equals("Wirkungsgrade")){
//				is_Wirkungsgrade_Diagramm = true ;
//			} else {
//				is_Wirkungsgrade_Diagramm = false ;
//			}
//	      	
//	      	 if ((selected.equals("pmi-Werte")|| selected.equals("Wirkungsgrade")) && is_Verlustteilung_Digramm) {
//				GraphikPanel.remove(3);
//				try {
//					GraphikPanel.add(Verlustteilung(selected),3);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				is_P_V_Diagramm = false;
//				GraphikPanel.revalidate();
//				GroupPanel.revalidate();
//	      	}
//	      	 
//			  else if (selected != null) {
//				
//					GraphikPanel.remove(3);
//					try {
//						GraphikPanel.add(Auswahl_Diagramm(selected), 3);
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					GraphikPanel.revalidate();
//					GroupPanel.revalidate();
//				}
//	      	  
//	      	  } catch (NullPointerException npe) {
//	      		  
//	      	  }
//	}
//   	 
//
//
//	/* (non-Javadoc)
//	 * @see bremoswing.graphik.BremoModellBerechnung#Druckverlauf()
//	 */
//	@Override
//	ChartPanel Druckverlauf() throws IOException {
//		// TODO Auto-generated method stub
//		
//        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
//        XYSeries serie1 =  new XYSeries("p_soll [bar]") ;
//        XYSeries serie2 = new XYSeries("p [bar] ") ;
//        XYDataset datasetDruckVerlauf ;
//        
//        String zeile = null;
//		String [] header = null;
//		String [] value = null;
//		int index_1 = -1;
//		int index_2 = -1;
//		if ((zeile = br.readLine()) != null){
//			header = zeile.split("\t");
//			for (int i = 0; i < header.length; i++){
//				if (header[i].equals("p_soll [bar]")){
//					index_1 = i;
//				}
//				else if (header[i].equals("p [bar]")){
//					index_2 = i;
//				}
//			}
//		}
//		while ((zeile = br.readLine()) != null){
//			value = zeile.split(" ");
//			if (zeit_oder_KW.equals("KW")) {
//				serie1.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_1]));//  KW  p_soll
//				serie2.add(Double.parseDouble(value[0]),Double.parseDouble(value[index_2])); // KW  p
//			}
//			else {
//				serie1.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_1]));//  Zeit p_soll
//				serie2.add(Double.parseDouble(value[1]),Double.parseDouble(value[index_2])); // Zeit p
//			}
//		}
//		XYSeriesCollection collectionDruckVerlauf = new XYSeriesCollection();
//		collectionDruckVerlauf.addSeries(serie1);
//		collectionDruckVerlauf.addSeries(serie2);
//		datasetDruckVerlauf = collectionDruckVerlauf;
//		ChartPanel chartDruckVerlauf = null;
//		if (zeit_oder_KW.equals("KW")){
//			//chartDruckVerlauf = createChartPanel(null, "[grdKW]",null , datasetDruckVerlauf);
//			chartDruckVerlauf = createChartPanel(null, zeit_oder_KW, "[bar]" , datasetDruckVerlauf);
//		}
//		else {
//			//chartDruckVerlauf = createChartPanel(null, "[s n. Rechenbeginn]",null , datasetDruckVerlauf);
//			chartDruckVerlauf = createChartPanel(null, null, null , datasetDruckVerlauf);
//        }
//		br.close();
//		
//		chartDruckVerlauf.setBorder(BorderFactory.createTitledBorder("Druckverlauf"));
//		
//		return chartDruckVerlauf;
//	}
//
//	/* (non-Javadoc)
//	 * @see bremoswing.graphik.BremoModellBerechnung#p_V_Diagramm()
//	 */
//	@Override
//	ChartPanel p_V_Diagramm() throws IOException {
//		// TODO Auto-generated method stub
//
//        BufferedReader br = new  BufferedReader(new FileReader(inputfile.getParent()+"/"+berechnungModell+"_"+inputfile.getName()));
//        //XYSeries serie1 = new XYSeries("p-V-Diagramm ( X -> [log(m^3)]  Y -> [log(p_soll)]", false) ;
//        XYSeries  serie1 = new XYSeries("p-V-Diagramm (p_soll)", false) ;
//        XYSeries serie2 = new XYSeries("p-V-Diagramm (p)", false) ;
//        XYDataset datasetVerlauf;
//        String zeile = null;
//		String [] header = null;
//		String [] value = null;
//		int index_1 = -1;
//		int index_2 = -1;
//		int index_3 = -1;
//		if ((zeile = br.readLine()) != null){
//			header = zeile.split("\t");
//			for (int i = 0; i < header.length; i++){
//				if (header[i].equals("Brennraumvolumen [m3]")){
//					index_1 = i;
//				}
//				else if (header[i].equals("p_soll [bar]")){
//					index_2 = i;
//				}
//				else if (header[i].equals("p [bar]")){
//					index_3 = i;
//				}
//			}
//		}
//		while ((zeile = br.readLine()) != null){
//			value = zeile.split(" ");
//			serie1.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_2])));// log(Brennraumvolumen)  log(p_soll)
//			serie2.add((Math.log(Double.parseDouble(value[index_1]))),Math.log(Double.parseDouble(value[index_3])));//  log(Brennraumvolumen)  log(p)
//		}
//		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
//		collectionVerlauf.addSeries(serie1);
//		collectionVerlauf.addSeries(serie2);
//		datasetVerlauf = collectionVerlauf;
//		ChartPanel chartVerlauf = null;
//	    //chartVerlauf = createChartPanel(null, "[grdKW]",null , datasetVerlauf);
//		chartVerlauf = createChartPanel(null, null, null , datasetVerlauf);
//	
//		br.close();
//		chartVerlauf.setBorder(BorderFactory.createTitledBorder("p-V-Diagramm P_soll und P"));
//		
//		return chartVerlauf;
//	}
//	
//}
