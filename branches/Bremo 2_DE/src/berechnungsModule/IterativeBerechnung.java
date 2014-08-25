package berechnungsModule;

import io.FileWriter_txt;

import java.io.*;
import java.util.ArrayList;

import matLib.VectorTools;
import berechnungsModule.Berechnung.DVA;
import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.ParameterFileWrongInputException;

/**
 * Iterative Berechnung, 08/2014
 * @author neurohr
 */
public class IterativeBerechnung {
	
	public static final String[] mglMethoden={"ohne", 
											"summenbrennverlaufsmethode"};
	
	private boolean isIterativ, lastTurn=false;
	private CasePara cp;
	private misc.VektorBuffer vekBuf;
	private String iterativeMethode;
	private double changedValue;
	private String fileName;
	private String eintrag;
	private File newInputFile;
	private String rechnungsEnde, rechnungsEnde_ORG;
	
	public IterativeBerechnung(File input){
		isIterativ = true;																		//Es wird erst einmal von true ausgegangen
		File inputFile = input;
		fileName = inputFile.getAbsolutePath();
		newInputFile = new File(fileName.substring(0, fileName.lastIndexOf(".")+1)+"org");
		inputFile.renameTo(newInputFile);														//Original-Inputfile wird umbenannt zu *.org 
		changedValue = 0;
		this.set_iterativeMethode();
		try{
			copyCasePara(newInputFile);															//Original-Inputfile wird zeilenweise kopiert 
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	
	/**
	 * Aktuelle CasePara wird übergeben
	 * @param CP
	 */
	public void initialisieren(CasePara CP){
		this.cp = CP;
		vekBuf = new misc.VektorBuffer(cp);
	}
	
	/**
	 * Hier wird der zu speichernde Parameter definiert
	 * @return
	 */
	public String get_Parameter2Save(){
		if(iterativeMethode.equalsIgnoreCase("summenbrennverlaufsmethode")){
			return "Qb [J]";
		}
		return null;
	}
	
	/**
	 * Gibt zurück, ob überhaupt iterativ gerechnet werden muss.
	 * @return boolean isIerativ
	 */
	public boolean isIterativ(){
		return isIterativ;
	}
	
	/**
	 * Auszuwertenden Wert in Vektor speichern
	 * @param time
	 * @param wert
	 */
	public void bufferParameter(double time, double wert){
		vekBuf.addValue(time, wert);
	}
	
	/**
	 * Auswerten der iterativen Berechnung und setzen der entsprechenden CasePara-Daten. Anschließend wird Original-Datei erneut
	 * mit aktualisierten Daten kopiert.
	 */
	public void auswerten(){
		if(!lastTurn){
			if(iterativeMethode==null)
				iterativeMethode = cp.get_iterativeMethode(mglMethoden);
			
			if(iterativeMethode.equalsIgnoreCase("ohne")){
				isIterativ = false;
				return;
			}else if(iterativeMethode.equalsIgnoreCase("summenbrennverlaufsmethode")){
				isIterativ = true;
				changedValue = this.get_offsetSummenbrennverlauf();
			}
			
			try {
				copyCasePara(newInputFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			isIterativ = false;
		}
	}
	
	/**
	 * Offset für die Summenbrennverlaufsmethode errechnen.
	 * @return
	 */
	private double get_offsetSummenbrennverlauf(){
		double offset = cp.get_pressureOffset();
		double startAbgleich = cp.get_DruckabgleichBeginn();
		double endeAbgleich = cp.get_DruckabgleichEnde();
		
		double[] QburnVec = new double[21];
		double[] timeVec = new double[QburnVec.length];
		double schrittweite = (endeAbgleich - startAbgleich) / (QburnVec.length-1);
		
		for(int i=0;i<QburnVec.length;i++){
			double time = startAbgleich + i*schrittweite;
			timeVec[i] = time;
			QburnVec[i] = vekBuf.getValue(time);
		}
		double[] coeff = VectorTools.lineareRegression(timeVec,QburnVec); //lineare Regression in den angegebenen Grenzen.
		if(coeff[0]>5e-2){
			offset += Math.abs(3*coeff[0]);
		}else if(coeff[0]<-5e-2){
			offset -= Math.abs(3*coeff[0]);
		}else{
			lastTurn = true; //Ende der Iteration durch Konvergenz, danach nur noch ein kompletter Durchlauf
		}
		eintrag = "offset [Pa] := "+Double.toString(offset);
		return offset;
	}
	
	/**
	 * Kopieren der CasePara-Datei mit iterativ veränderten Werten.
	 * @param file
	 * @throws IOException
	 */
	private void copyCasePara(File file) throws IOException{
		FileReader fr = new FileReader(file);
		PrintWriter pw = new PrintWriter(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line;
		boolean found = false;
		while((line = br.readLine()) != null){
			String tmp = line.replace(" ", "");
			if(iterativeMethode.equalsIgnoreCase("summenbrennverlaufsmethode") && tmp.startsWith("offset[Pa]") && !found){
				pw.println(eintrag);
				found = true;
			}else if(iterativeMethode.equalsIgnoreCase("summenbrennverlaufsmethode") && tmp.startsWith("rechnungsEnde") && !lastTurn){
					pw.println(rechnungsEnde);
			}else{
				pw.println(line);
			}
			if(tmp.startsWith("[Bremo::Input::Stop]") && !found){
				pw.println(eintrag);
				pw.println("[Bremo::Input::Stop]");
			}
		}
		pw.flush();
		pw.close();
		br.close();
		fr.close();
	}
	
	/**
	 * Zum Setzen der "iterativenMethode" am Anfang, bevor die CasePara erstellt wurde. Wurde die Input-Datei schon eingelesen, wird die
	 * get-Methode verwendet.
	 */
	private void set_iterativeMethode(){
		if(iterativeMethode==null){
			try{
				iterativeMethode = cp.get_iterativeMethode(mglMethoden);
			}catch(Exception e){
				try{
					FileReader fr = new FileReader(newInputFile);
					BufferedReader br = new BufferedReader(fr);
					String line;
					boolean schleife = true;
					double offset = 0;
					while((line = br.readLine()) != null && schleife){
						if(line.toLowerCase().contains("rechnungsende")){
							rechnungsEnde_ORG = line;
						}else if(line.toLowerCase().contains("pressureadjustmentmethod") && line.contains("summenbrennverlauf")){
							iterativeMethode = "summenbrennverlaufsmethode";
						}else if(line.replace(" ", "").contains("offset[Pa]")){
//							String xx  =line.replace(" ", "").substring(line.lastIndexOf(":="));
							offset = Double.parseDouble(line.replace(" ", "").substring(line.lastIndexOf(":=")));
						}else if(line.replace(" ", "").toLowerCase().contains(("KW_Ende_Druckabgleich").toLowerCase())){
							double xx = Double.parseDouble(line.replace("\t","").replace(" ", "").substring(line.lastIndexOf(":=")-1));
							rechnungsEnde = "rechnungsEnde [KWnZOT] := " + (1+Double.parseDouble(line.replace("\t","").replace(" ", "").substring(line.lastIndexOf(":=")-1)));
						}
					}
					switch(iterativeMethode){
					case "summenbrennverlaufsmethode":
						changedValue = offset;
						eintrag = "offset [Pa] := "+Double.toString(changedValue);
					}
					
					br.close();
					fr.close();
				}catch(Exception f){}
			}
		}
	}

	public void deleteFile() {
		newInputFile.delete();
	}
}
