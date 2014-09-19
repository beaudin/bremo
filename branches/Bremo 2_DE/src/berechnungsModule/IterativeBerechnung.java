package berechnungsModule;

import io.FileWriter_txt;
import io.InputFileReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import matLib.VectorTools;
import berechnungsModule.Berechnung.DVA;
import bremo.parameter.CasePara;
import bremo.sys.Rechnung;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

/**
 * Iterative Berechnung, 08/2014
 * @author neurohr
 */
public class IterativeBerechnung {
	
	public static final String[] mglMethoden={"ohne", 
											"summenbrennverlaufsmethode"};
	
	private boolean isIterativ, lastTurn=false, delete=false;
	private CasePara cp;
	private misc.VektorBuffer vekBuf;
	private String iterativeMethode;
	private double changedValue;
	private String fileName;
	private String eintrag;
	private File orgInputFile;
	private String rechnungsEnde, rechnungsEnde_ORG;
	
	public IterativeBerechnung(File input){
		isIterativ = true;																		//Es wird erst einmal von true ausgegangen
		File inputFile = input;
		fileName = inputFile.getAbsolutePath();													//Original-Inputfile wird umbenannt zu *.org 
		changedValue = 0;
		this.set_iterativeMethode(inputFile);
		if(isIterativ){
			try{
				orgInputFile = new File(fileName.substring(0, fileName.lastIndexOf(".")+1)+"org");
				inputFile.renameTo(orgInputFile);
				copyCasePara(orgInputFile);															//Original-Inputfile wird zeilenweise kopiert
				delete = true;
			}catch(IOException io){
				io.printStackTrace();
			}
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
				copyCasePara(orgInputFile);
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
		if(isIterativ){
			FileReader fr = new FileReader(file);
			PrintWriter pw = new PrintWriter(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			boolean found = false;
			while((line = br.readLine()) != null){
				String tmp = line.replace(" ", "").replace("\t", "");
				if(isIterativ){
					if(iterativeMethode.equalsIgnoreCase("summenbrennverlaufsmethode") && tmp.startsWith("offset[Pa]") && !found){
						pw.println(eintrag);
						found = true;
					}else if(iterativeMethode.equalsIgnoreCase("summenbrennverlaufsmethode") && tmp.startsWith("rechnungsEnde") && !lastTurn){
							pw.println(rechnungsEnde);
					}else if(tmp.startsWith("[Bremo::Input::Stop]") && !found){
						pw.println(eintrag);
						pw.println("[Bremo::Input::Stop]");
					}else{
						pw.println(line);
					}
				}else{
					pw.println(line);
				}
			}
			pw.flush();
			pw.close();
			br.close();
			fr.close();
		}else{
			File inputFile = new File(fileName);
			orgInputFile.renameTo(inputFile);
		}
	}
	
	/**
	 * Zum Setzen der "iterativenMethode" am Anfang, bevor die CasePara erstellt wurde. Wurde die Input-Datei schon eingelesen, wird die
	 * get-Methode verwendet.
	 */
	private void set_iterativeMethode(File file){
		InputFileReader	ifr =new InputFileReader(file);
		Hashtable<String,String[]> INPUTFILE_PARAMETER=ifr.get_eingabeParameter();
		iterativeMethode = "ohne";
		this.isIterativ = false;
		this.lastTurn = true;
		eintrag = "";
		
//		################################################################################################################
//		Für Summenbrennverlaufsmethode:
		try{
			String[] werte = INPUTFILE_PARAMETER.get("pressureAdjustmentMethod");
			if(werte[0].equalsIgnoreCase("summenbrennverlauf")){
				iterativeMethode = "summenbrennverlaufsmethode";
				this.isIterativ = true;
				this.lastTurn = false;
				try{
					werte = INPUTFILE_PARAMETER.get("offset");
					changedValue = Double.parseDouble(werte[0]);
				}catch(Exception e){
					werte = new String[2];
					werte[0] = Double.toString(0);
					werte[1] = "[Pa]";
				}
				eintrag = "offset "+werte[1]+" := "+werte[0];
				try{
					werte = INPUTFILE_PARAMETER.get("KW_Ende_Druckabgleich");
				}catch(Exception e){
					werte = new String[2];
					werte[0] = Double.toString(-50);
					werte[1] = "[KWnZOT]";
				}
				double plus;
				if(werte[1].contains("KWnZOT")){
					plus = 1;
				}else{
					if(INPUTFILE_PARAMETER.get("rechnungsSchrittweite")[1].contains("KW")){
						plus = Double.parseDouble(INPUTFILE_PARAMETER.get("rechnungsSchrittweite")[0]) / Double.parseDouble(INPUTFILE_PARAMETER.get("Drehzahl")[0]) / 6;
					}else{
						plus = Double.parseDouble(INPUTFILE_PARAMETER.get("rechnungsSchrittweite")[0]);
					}
				}
				rechnungsEnde = "rechnungsEnde "+werte[1]+" := "+(Double.parseDouble(werte[0])+plus);
				werte = INPUTFILE_PARAMETER.get("rechnungsEnde");
				rechnungsEnde_ORG = "rechnungsEnde "+werte[1]+" := "+werte[0];
			}
		}catch(Exception e){}
//		################################################################################################################
		
	}

	public void deleteFile() {
		if(delete){
			orgInputFile.delete();
		}
	}
}
