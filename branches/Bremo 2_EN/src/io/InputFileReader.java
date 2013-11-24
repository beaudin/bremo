package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import berechnungsModule.wallHeatTransfer.WallHeatTransferModelFactory;
import bremoExceptions.ParameterFileWrongInputException;

public class InputFileReader {


	private Hashtable<String, String []> eingabeParameter = new Hashtable<String, String []>();
	private Hashtable<String, String > berechnungsModule = new Hashtable<String, String>();

	public InputFileReader(File inputFile){
		try {
			read_InpuFile(inputFile);			
			//read_ParameterFile(inputFile);
		} catch (ParameterFileWrongInputException e) {			
			e.stopBremo();
		}

	}


	public Hashtable<String, String []> get_eingabeParameter(){
		return eingabeParameter;
	}


	public Hashtable<String, String > get_berechnungsModule(){
		return berechnungsModule;
	}




	private void read_InpuFile(File inputFile) throws ParameterFileWrongInputException{	
		String modulFlag=null,modulWahl=null;
		String inputStartFlag="[Bremo::Input::Start]";
		String inputStopFlag="[Bremo::Input::Stop]";
		boolean inputBlockFound=false;

		try {
			//Gepufferten Reader erstellen
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			//Erste Zeile einlesen
			String currentLine = br.readLine();	
			

			//Zeilen der Datei durchsuchen
			while (currentLine != null) {
//				currentLine=URLDecoder.decode(currentLine, "UTF-8");
				//Leerzeichen entfernen
				currentLine=currentLine.replaceAll(" ", "");
				currentLine=currentLine.replaceAll("\t", "");
				if(inputBlockFound){
					//Ist diese Zeile der Anfang einer Modulvorgabe?
					if (currentLine.startsWith("[")  && !currentLine.equals(inputStopFlag) ){		    		  

						int idxModulNameAnfang=currentLine.indexOf("[")+1;
						int idxModulNameEnde=currentLine.indexOf(":");

						if(idxModulNameEnde-1<0 || idxModulNameEnde<=idxModulNameAnfang || currentLine.endsWith("]")==false) {
							try{
								throw new ParameterFileWrongInputException("Wrong input: "+ currentLine +"\n"+
										"Simulation modules must be defined as follows: \n" +
										"[MODULE_FLAG: MODULE_NAME] e.g.: \t " +
										" [" + WallHeatTransferModelFactory.WHT_MODEL_FLAG+": " + WallHeatTransferModelFactory.WHT_MODELS[0]+"] \n " +
								"If you do not want to specify a simulation module delete the square brackets at the beginning of the line!");								
							}catch(ParameterFileWrongInputException e){
								e.stopBremo();
							}

						}else{		    			 
							modulFlag=currentLine.substring(idxModulNameAnfang, idxModulNameEnde);

							//int idxModulAuswahlEnde=currentLine.indexOf("]")-1;
							modulWahl=currentLine.substring(idxModulNameEnde+1, currentLine.length()-1);
							if(berechnungsModule.containsKey(modulFlag)){	
								try{
									throw new ParameterFileWrongInputException("The simulation module \"" +modulFlag+"\" " +
											"has been defined multple times and was therfore set to: " + modulWahl );
								}catch(ParameterFileWrongInputException e){
									e.log_Message();
								}	
							}

							berechnungsModule.put(modulFlag, modulWahl);		    			  
						} 		  

					}else{
						//Trennung zwischen Variablename und wert ist als := definiert
						int idxTrennung=currentLine.indexOf(":=");

						if(idxTrennung<0){
							//Zeilen ohne Zuordnung werden Übersprungen --> Leerzeilen und Komentare stellen kein Problem dar
							currentLine = br.readLine();
							continue;
						}								
						//Leerzeichen entfernen
						currentLine = currentLine.replace(',','.');		    		 


						//Einheiten muessen in rechteckigen Klammern eingegeben Werden
						int idxParamNameEnde=currentLine.indexOf("[");
						int idxParamEinheitEnde=currentLine.indexOf("]")+1;

						if(idxParamNameEnde<0 || idxParamEinheitEnde<=idxParamNameEnde+1){
							try{
								String paramNameTemp=currentLine.substring(0,idxTrennung-1);
								throw new ParameterFileWrongInputException("No units have been entered for the parameter: " + paramNameTemp+ "\n" 
										+"Specify them with suare brackets: [bar]"); 
							}catch(ParameterFileWrongInputException e){
								e.stopBremo();
							}

						}

						String paraEinheit=currentLine.substring(idxParamNameEnde,idxParamEinheitEnde);		    		 
						String paraWert=currentLine.substring(idxTrennung+2);
						String paramName=currentLine.substring(0,idxParamNameEnde);		    		  
						String [] paraWertEinheit={paraWert,paraEinheit};

						if(eingabeParameter.containsKey(paramName))	{
							try{	
								throw new ParameterFileWrongInputException("The parameter \"" +paramName+"\" has been defined multiple times" +
										" and was therefore set to: " + paraWert+ " " +paraEinheit );
							}catch(ParameterFileWrongInputException e){
								e.log_Message();
							}
						}
						eingabeParameter.put(paramName, paraWertEinheit);	    					
					}		

				}

				if(currentLine.equals(inputStartFlag))
					inputBlockFound=true;
				//Naechste Zeile einlesen
						

				if(currentLine.equals(inputStopFlag))  
					break;
				
				currentLine = br.readLine();	
			}  //End while

			fr.close();
			br.close();	    

			//Exceptions fangen
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);		
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);		
		}
	}//Methode
}
