package bremoswing.graphik.old;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * New View To compare 1-4 Graph  
 * @author Ngueneko Steve
 *
 */
public class BremoUltimateView implements ActionListener {

	JPanel GraphikPanel;
    JPanel GroupPanel;
    
    JLabel  x_achse_label;
    JLabel  y_achse_1_label;
    JLabel  y_achse_2_label;
    JLabel  y_achse_3_label;
    JLabel  y_achse_4_label;
    JLabel  achse_to_log_label;
    JLabel nbr_of_Achse_label;
    JLabel datum;
    
    JComboBox<String> x_achse;
    JComboBox<String> y_achse_1;
    JComboBox<String> y_achse_2;
    JComboBox<String> y_achse_3;
    JComboBox<String> y_achse_4;
    JComboBox<String> achse_to_log;
    JComboBox<Integer> nbr_of_Achse;
    
    
    File  file ;
    
    String[] Header ;
    
  
    
    
	BremoUltimateView (File file ) throws IOException {
    	
    	this.file = file;
    	
    	BufferedReader br = new  BufferedReader(new FileReader(file));
        
        String zeile = null;
 		Header = null;
 		if ((zeile = br.readLine()) != null){
 			Header = zeile.split("\t");		
 		}
 		br.close();
    	
    	GraphikPanel = new JPanel();
    	GroupPanel   = new JPanel();
    	
    	x_achse_label   = new JLabel("X-Achse :");
        y_achse_1_label = new JLabel("1. Y-Achse :") ;
        y_achse_2_label = new JLabel("2. Y-Achse :");
        y_achse_3_label = new JLabel("3. Y-Achse :");
        y_achse_4_label = new JLabel("4. Y-Achse :");
        achse_to_log_label = new JLabel("Achse :");
        nbr_of_Achse_label = new JLabel("Nbr. Y-Achse :");
        datum = new JLabel(BremoModellGraphik.datumLabel.getText());
        datum.setFont(BremoModellGraphik.datumLabel.getFont());
        
        x_achse   = new JComboBox<String>();
        y_achse_1 = new JComboBox<String>();
        y_achse_2 = new JComboBox<String>();
        y_achse_3 = new JComboBox<String>();
        y_achse_4 = new JComboBox<String>();
        achse_to_log = new JComboBox<String>();
        nbr_of_Achse = new JComboBox<Integer>();
        
        BremoModellGraphik.addItemToComboBox(x_achse, Header);
        BremoModellGraphik.addItemToComboBox(y_achse_1, Header);
        BremoModellGraphik.addItemToComboBox(y_achse_2, Header);
        BremoModellGraphik.addItemToComboBox(y_achse_3, Header);
        BremoModellGraphik.addItemToComboBox(y_achse_4, Header);
        BremoModellGraphik.addItemToComboBox(achse_to_log, new String [] {"No Log","Log auf X-Achse","Log auf Y-Achse","Doppel Log"});
        BremoModellGraphik.addItemToComboBox(nbr_of_Achse, new Integer [] {1,2,3,4});
        
        y_achse_2.setEnabled(false);
        y_achse_3.setEnabled(false);
        y_achse_4.setEnabled(false);
        
        x_achse.setBackground(Color.white);
        y_achse_1.setBackground(Color.red);
        y_achse_2.setBackground(Color.blue);
        y_achse_3.setBackground(Color.cyan);
        y_achse_4.setBackground(Color.magenta);
        nbr_of_Achse.setBackground(Color.white);
        achse_to_log.setBackground(Color.white);
        
        x_achse.addActionListener(this);
        
        nbr_of_Achse.addActionListener(this);
        
        achse_to_log.addActionListener(this);
        
        y_achse_1.addActionListener(this);
        
        y_achse_2.addActionListener(this);
       
        y_achse_3.addActionListener(this);
       
        y_achse_4.addActionListener(this);
        
        GraphikPanel.setPreferredSize(new Dimension (1010,675));
        //GraphikPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GroupPanel.setPreferredSize(new Dimension (1280,75));
        //GroupPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        //GroupPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        /**  Layout of GraphikPanel  ****************************************************/
        GridLayout GraphikPanelLayout = new GridLayout();
        GraphikPanelLayout.setColumns(1);
        GraphikPanelLayout.setRows(1);
        GraphikPanelLayout.setHgap(10); 
        GraphikPanelLayout.setVgap(10); 
        GraphikPanel.setLayout(GraphikPanelLayout);
        GraphikPanel.add(auswahl_Diagramm(y_achse_1.getSelectedItem().toString()));
		
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(GroupPanel);
        GroupPanel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(y_achse_1_label)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(13, 13, 13)
                            .addComponent(x_achse_label)
                            .addGap(6, 6, 6)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(x_achse, 0, 180, Short.MAX_VALUE)
                        .addComponent(y_achse_1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(10, 10, 10)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(y_achse_2_label, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(nbr_of_Achse_label))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(nbr_of_Achse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(y_achse_2, 0, 180, Short.MAX_VALUE))
                    .addGap(10, 10, 10)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(y_achse_3_label)
                        .addComponent(achse_to_log_label))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(y_achse_3, 0, 180, Short.MAX_VALUE)
                        .addComponent(achse_to_log, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(10, 10, 10)
                    .addComponent(y_achse_4_label, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(y_achse_4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
                .addComponent(datum, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0,0,0)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(x_achse_label)
                        .addComponent(x_achse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(achse_to_log_label)
                        .addComponent(achse_to_log, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(nbr_of_Achse_label)
                        .addComponent(nbr_of_Achse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(8,8,8)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(y_achse_1_label)
                        .addComponent(y_achse_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y_achse_2_label)
                        .addComponent(y_achse_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y_achse_3_label)
                        .addComponent(y_achse_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y_achse_4_label)
                        .addComponent(y_achse_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(datum))
                    //.addGap(5,5,5)
                        )
            );
    	
    }

	/**
	 * @return the graphikPanel
	 */
	public JPanel getGraphikPanel() {
		return GraphikPanel;
	}

	/**
	 * @param graphikPanel the graphikPanel to set
	 */
	public void setGraphikPanel(JPanel graphikPanel) {
		GraphikPanel = graphikPanel;
	}

	/**
	 * @return the groupPanel
	 */
	public JPanel getGroupPanel() {
		return GroupPanel;
	}

	/**
	 * @param groupPanel the groupPanel to set
	 */
	public void setGroupPanel(JPanel groupPanel) {
		GroupPanel = groupPanel;
	}
	
    public void setPanelBorderToBlack () {
    	GraphikPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GroupPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    }
    
    public void setPanelBorderToNull () {
        GraphikPanel.setBorder(null);
        GroupPanel.setBorder(null);
    }

    /**
     * Build Graphic for the 1  Curve
     * @return
     * @throws IOException
     */
    public ChartPanel auswahl_Diagramm(String selected) throws IOException {
    	BufferedReader br;
            br = new  BufferedReader(new FileReader(file));
       
       XYSeries serie1 = new XYSeries(selected,false,true) ;
        XYDataset datasetVerlauf ;
        
        String zeile = null;
		String [] header = null;
		String [] value = null;
		int y_index = -1;
		int x_index = -1;
		if ((zeile = br.readLine()) != null){
			header = zeile.split("\t");
			for (int i = 0; i < header.length; i++){
				if (header[i].equals(selected)){
					y_index = i;
				}
				if (header[i].equals(x_achse.getSelectedItem().toString())){
					x_index = i;
				}
			}
		}
		while ((zeile = br.readLine()) != null){
			value = zeile.split(" ");
			serie1.setKey(selected );
			if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(1)){
				serie1.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index]));
			}
			else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(2)){
				serie1.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index])));
			}
			else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(3)){
				serie1.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index])));
			}
			else {
				serie1.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index]));
			}
			
		}
		XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
		collectionVerlauf.addSeries(serie1);
		datasetVerlauf = collectionVerlauf;
		ChartPanel chartVerlauf = null;
		
		chartVerlauf = BremoModellGraphik.createChartPanel(null, x_achse.getSelectedItem().toString(), selected.split(" ")[1] , datasetVerlauf);
		
		br.close();
		selected = selected.substring(0, selected.indexOf("["));
		chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected+" verlauf"));
		
		return chartVerlauf;
    	
    }
	
	
    /**
     * Build Graphic for 2 Curve
     * @return
     * @throws IOException
     */
    public ChartPanel auswahl_Diagramm(String selected1, String selected2) throws IOException {
    	BufferedReader br;
        br = new  BufferedReader(new FileReader(file));
   
   XYSeries serie1 = new XYSeries(selected1,false,true) ;
   XYSeries serie2 = new XYSeries(selected2,false,true) ;
   
   XYDataset datasetVerlauf ;
   XYDataset datasetVerlauf2 ;
    
    String zeile = null;
	String [] header = null;
	String [] value = null;
	int y_index = -1;
	int y_index2 = -1;
	int x_index = -1;
	if ((zeile = br.readLine()) != null){
		header = zeile.split("\t");
		for (int i = 0; i < header.length; i++){
			if (header[i].equals(selected1)){
				y_index = i;
			}
			if (header[i].equals(selected2)){
				y_index2 = i;
			}
			if (header[i].equals(x_achse.getSelectedItem().toString())){
				x_index = i;
			}
		}
	}
	while ((zeile = br.readLine()) != null) {
			value = zeile.split(" ");
			
			serie1.setKey(selected1);
			serie2.setKey(selected2);
			
			if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(1)){
				serie1.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index]));
				serie2.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index2]));
			}
			else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(2)){
				serie1.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index])));
				serie2.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index2])));
			}
			else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(3)){
				serie1.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index])));
				serie2.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index2])));
			}
			else {
				serie1.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index]));
				serie2.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index2]));
			}
		}
	XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
	collectionVerlauf.addSeries(serie1);
	datasetVerlauf = collectionVerlauf;
	
	XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
	collectionVerlauf2.addSeries(serie2);
	datasetVerlauf2 = collectionVerlauf2;
	
	ChartPanel chartVerlauf = null;
	
    chartVerlauf = BremoModellGraphik.createChartPanel(null, x_achse.getSelectedItem().toString(), selected1.split(" ")[1], selected2.split(" ")[1], datasetVerlauf ,datasetVerlauf2);
	
    br.close();
	selected1 = selected1.substring(0, selected1.indexOf("["));
	selected2 = selected2.substring(0, selected2.indexOf("["));
	chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected1+" ,"+ selected2 +" verlauf"));
	
	return chartVerlauf;
	
    }
    
   
    /**
     * Build Graphic for 3  Curve
     * @return
     * @throws IOException
     */
    public ChartPanel auswahl_Diagramm(String selected1, String selected2, String selected3) throws IOException {
    	BufferedReader br;
        br = new  BufferedReader(new FileReader(file));
   
   XYSeries serie1 = new XYSeries(selected1,false,true) ;
   XYSeries serie2 = new XYSeries(selected2,false,true) ;
   XYSeries serie3 = new XYSeries(selected3,false,true) ;
   
   XYDataset datasetVerlauf;
   XYDataset datasetVerlauf2;
   XYDataset datasetVerlauf3;
    
    String zeile = null;
	String [] header = null;
	String [] value = null;
	int y_index = -1;
	int y_index2 = -1;
	int y_index3 = -1;
	int x_index = -1;
	if ((zeile = br.readLine()) != null){
		header = zeile.split("\t");
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals(selected1)){
				y_index = i;
			}
			if (header[i].equals(selected2)) {
				y_index2 = i;
			}
			if (header[i].equals(selected3)) {
				y_index3 = i;
			}
			if (header[i].equals(x_achse.getSelectedItem().toString())){
				x_index = i;
			}
		}
	}
	while ((zeile = br.readLine()) != null){
		value = zeile.split(" ");
		
		serie1.setKey(selected1);
		serie2.setKey(selected2);
		serie3.setKey(selected3);
		
		if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(1)){
			serie1.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index]));
			serie2.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index2]));
			serie3.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index3]));
		}
		else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(2)){
			serie1.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index])));
			serie2.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index2])));
			serie3.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index3])));
		}
		else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(3)){
			serie1.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index])));
			serie2.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index2])));
			serie3.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index3])));
		}
		else {
			serie1.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index]));
			serie2.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index2]));
			serie3.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index3]));
		}
	}
	XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
	collectionVerlauf.addSeries(serie1);
	datasetVerlauf = collectionVerlauf;
	
	XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
	collectionVerlauf2.addSeries(serie2);
	datasetVerlauf2 = collectionVerlauf2;
	
	XYSeriesCollection collectionVerlauf3 = new XYSeriesCollection();
	collectionVerlauf3.addSeries(serie3);
	datasetVerlauf3 = collectionVerlauf3;
	
	ChartPanel chartVerlauf = null;
	
    chartVerlauf = BremoModellGraphik.createChartPanel(null, x_achse.getSelectedItem().toString(), selected1.split(" ")[1], selected2.split(" ")[1],selected3.split(" ")[1], datasetVerlauf, datasetVerlauf2, datasetVerlauf3);
	
    br.close();
	selected1 = selected1.substring(0, selected1.indexOf("["));
	selected2 = selected2.substring(0, selected2.indexOf("["));
	selected3 = selected3.substring(0, selected3.indexOf("["));
	chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected1+" ,"+ selected2 +" ,"+ selected3 +" verlauf"));
	
	return chartVerlauf;
	
    }
    
    /**
     * Build Graphic for 4  Curve
     * @return
     * @throws IOException
     */
    public ChartPanel auswahl_Diagramm(String selected1, String selected2, String selected3, String selected4) throws IOException {
    	BufferedReader br;
        br = new  BufferedReader(new FileReader(file));
   
   XYSeries serie1 = new XYSeries(selected1,false,true) ;
   XYSeries serie2 = new XYSeries(selected2,false,true) ;
   XYSeries serie3 = new XYSeries(selected3,false,true) ;
   XYSeries serie4 = new XYSeries(selected4,false,true) ;
   
   XYDataset datasetVerlauf;
   XYDataset datasetVerlauf2;
   XYDataset datasetVerlauf3;
   XYDataset datasetVerlauf4;
    
    String zeile = null;
	String [] header = null;
	String [] value = null;
	int y_index = -1;
	int y_index2 = -1;
	int y_index3 = -1;
	int y_index4 = -1;
	int x_index = -1;
	if ((zeile = br.readLine()) != null){
		header = zeile.split("\t");
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals(selected1)){
				y_index = i;
			}
			if (header[i].equals(selected2)) {
				y_index2 = i;
			}
			if (header[i].equals(selected3)) {
				y_index3 = i;
			}
			if (header[i].equals(selected4)) {
				y_index4 = i;
			}
			if (header[i].equals(x_achse.getSelectedItem().toString())){
				x_index = i;
			}
		}
	}
	while ((zeile = br.readLine()) != null){
		value = zeile.split(" ");
		
		serie1.setKey(selected1);
		serie2.setKey(selected2);
		serie3.setKey(selected3);
		serie4.setKey(selected4);
		
		if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(1)){
			serie1.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index]));
			serie2.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index2]));
			serie3.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index3]));
			serie4.add(Math.log(Double.parseDouble(value[x_index])),Double.parseDouble(value[y_index4]));
		}
		else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(2)){
			serie1.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index])));
			serie2.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index2])));
			serie3.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index3])));
			serie4.add(Double.parseDouble(value[x_index]),Math.log(Double.parseDouble(value[y_index4])));
		}
		else if (achse_to_log.getSelectedItem() == achse_to_log.getItemAt(3)){
			serie1.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index])));
			serie2.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index2])));
			serie3.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index3])));
			serie4.add(Math.log(Double.parseDouble(value[x_index])),Math.log(Double.parseDouble(value[y_index4])));
		}
		else {
			serie1.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index]));
			serie2.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index2]));
			serie3.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index3]));
			serie4.add(Double.parseDouble(value[x_index]),Double.parseDouble(value[y_index4]));
		}
	}
	XYSeriesCollection collectionVerlauf = new XYSeriesCollection();
	collectionVerlauf.addSeries(serie1);
	datasetVerlauf = collectionVerlauf;
	
	XYSeriesCollection collectionVerlauf2 = new XYSeriesCollection();
	collectionVerlauf2.addSeries(serie2);
	datasetVerlauf2 = collectionVerlauf2;
	
	XYSeriesCollection collectionVerlauf3 = new XYSeriesCollection();
	collectionVerlauf3.addSeries(serie3);
	datasetVerlauf3 = collectionVerlauf3;
	
	XYSeriesCollection collectionVerlauf4 = new XYSeriesCollection();
	collectionVerlauf4.addSeries(serie4);
	datasetVerlauf4 = collectionVerlauf4;
	
	ChartPanel chartVerlauf = null;
	
    chartVerlauf = BremoModellGraphik.createChartPanel(null, x_achse.getSelectedItem().toString(), selected1.split(" ")[1], selected2.split(" ")[1],selected3.split(" ")[1], selected4.split(" ")[1], datasetVerlauf, datasetVerlauf2, datasetVerlauf3, datasetVerlauf4);
	
    br.close();
	selected1 = selected1.substring(0, selected1.indexOf("["));
	selected2 = selected2.substring(0, selected2.indexOf("["));
	selected3 = selected3.substring(0, selected3.indexOf("["));
	selected4 = selected4.substring(0, selected4.indexOf("["));
	chartVerlauf.setBorder(BorderFactory.createTitledBorder(selected1 +" ,"+ selected2 +" ,"+ selected3 +" ,"+ selected4 +" verlauf"));
	
	return chartVerlauf;
	
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(nbr_of_Achse.getSelectedItem() == nbr_of_Achse.getItemAt(1)) {
			if (e.getSource().equals(nbr_of_Achse)){
			   y_achse_2.setEnabled(true);
			   y_achse_3.setEnabled(false);
			   y_achse_4.setEnabled(false);
			}
			try {
			    GraphikPanel.removeAll();
				GraphikPanel.add(auswahl_Diagramm(y_achse_1.getSelectedItem().toString(),y_achse_2.getSelectedItem().toString()));
			} catch (IOException exp) {
				exp.printStackTrace();
			}
			GraphikPanel.revalidate();
		} else if (nbr_of_Achse.getSelectedItem() == nbr_of_Achse.getItemAt(2)) {
			if (e.getSource().equals(nbr_of_Achse)){
			    y_achse_2.setEnabled(true);
			    y_achse_3.setEnabled(true);
			    y_achse_4.setEnabled(false);
			}
			try {
					GraphikPanel.removeAll();
					GraphikPanel.add(auswahl_Diagramm(y_achse_1.getSelectedItem().toString(),y_achse_2.getSelectedItem().toString(), y_achse_3.getSelectedItem().toString()));
				} catch (IOException exp) {
					exp.printStackTrace();
				}
				GraphikPanel.revalidate();
			
		} else if (nbr_of_Achse.getSelectedItem() == nbr_of_Achse.getItemAt(3)) {
			if (e.getSource().equals(nbr_of_Achse)){
			   y_achse_2.setEnabled(true);
			   y_achse_3.setEnabled(true);
			   y_achse_4.setEnabled(true);
			}
			GraphikPanel.removeAll();
				try {
					GraphikPanel.add(auswahl_Diagramm(y_achse_1.getSelectedItem().toString(),y_achse_2.getSelectedItem().toString(), y_achse_3.getSelectedItem().toString(), y_achse_4.getSelectedItem().toString()));
				} catch (IOException exp) {
					exp.printStackTrace();
				}
				GraphikPanel.revalidate();
		} else {
			if (e.getSource().equals(nbr_of_Achse)){
			   y_achse_2.setEnabled(false);
	           y_achse_3.setEnabled(false);
	           y_achse_4.setEnabled(false);
			}
	        try {
	        	GraphikPanel.removeAll();
				GraphikPanel.add(auswahl_Diagramm(y_achse_1.getSelectedItem().toString()));
			} catch (IOException exp) {
				exp.printStackTrace();
			}
	        GraphikPanel.revalidate();
	        
		}
	}
}
