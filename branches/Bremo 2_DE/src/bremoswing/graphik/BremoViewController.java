package bremoswing.graphik;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.EventListener;
import java.util.List;

import javax.swing.JFileChooser;

import org.jfree.chart.JFreeChart;

import bremoswing.util.ExtensionFileFilter;

public class BremoViewController  {
    
	BremoViewModel model ;
	
	
	public BremoViewController(BremoViewModel Model) {
		model = Model;
	}
    
	/**
	 * Controller tell to the Model to notify the view to show ChooseFrame an the User
	 */
	public void show() {
		model.showChooseFrame();
	}
    
	/**
	 * Controller tell to the Model to notify the view to hide ChooseFrame an the User
	 */
	public void hide() {
		model.hideChooseFrame();
	}
    /**
     * Controller control Data and send it to the model to store
     * @param allselectedItem
     */
	public void bremoViewData(String[] allselectedItem) {
		model.storeItemFromBremoView(allselectedItem);
	}
    
	

	public void chooseFrameData(List<List<String>> selectedItemListlist) {
		model.storeItemFromChooseFrame(selectedItemListlist);		
	}
    /**
     * Controller tell to Model to create Chart 
     * @throws IOException 
     */
	public void Chart() throws IOException {
		model.createChart();
		
	}

	public void SendFileModel(File file) throws IOException {
		model.readInputFile(file);
		
	}

	public BremoViewModel getModel() {
		return model;
	}

	public void savefavs(int i) {
		// TODO Auto-generated method stub
		model.saveFavs(i);
	}

	public void loadfavs(int i)  {
		// TODO Auto-generated method stub
		model.loadFavs(i);
	}
	
	
	
}
