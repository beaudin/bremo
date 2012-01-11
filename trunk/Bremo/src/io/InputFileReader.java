package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import berechnungsModule.wandwaerme.WandWaermeUebergang;
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




	private Hashtable<String, String []> read_ParameterFile(File inputFile) throws ParameterFileWrongInputException{	

		String blockName="irgendWas";
		boolean blockGefunden = false;		



		try {
			//Gepufferten Reader erstellen
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			//Erste Zeile einlesen
			String currentLine = br.readLine();
			//Zeilen der Datei durchsuchen
			while (currentLine != null) {
				//Werte zuweisen, nur wenn der Blockname in einer 
				//vorherigen Iteration gefunden wurde		      
				if (blockGefunden == true){
					//Ist diese Zeile der Anfang des nächsten Blocks?
					//Wenn ja, dann aussteigen
					if (currentLine.startsWith("[")){
						break;
					}else{

						//Leerzeichen entfernen
						String zeilenText=currentLine.trim();  
						zeilenText = zeilenText.replace(',','.');
						//Trennung zwischen Variablename und wert ist als := definiert
						int idxTrennung=currentLine.indexOf(":=");

						if(idxTrennung<0){
							//Zeilen ohne Zuordnung werden Übersprungen --> Leerzeilen und Komentare stellen kein Problem dar
							currentLine = br.readLine();
							continue;
						}	 

						//Einheiten müssen in rechteckigen Klammern eingegeben Werden
						int idxParamNameEnde=currentLine.indexOf("[");
						int idxParamEinheitEnde=currentLine.indexOf("]");

						if(idxParamNameEnde-1<0 || idxParamEinheitEnde<=idxParamNameEnde){
							String paramNameTemp=zeilenText.substring(0,idxTrennung-1);
							throw new ParameterFileWrongInputException("Fuer den Parameter " + paramNameTemp+ " wurden keine Einheiten angegeben \n" 
									+"Diese müssen in rechteckigen Klammern [] angegeben werden"); 
						}

						String paraEinheit=zeilenText.substring(idxParamNameEnde,idxParamEinheitEnde+1);		    		 
						String paraWert=zeilenText.substring(idxTrennung+2);
						String paramName=zeilenText.substring(0,idxParamNameEnde-1);

						String [] paraWertEinheit={paraWert,paraEinheit};
						if(eingabeParameter.containsKey(paramName)){
							try{
								throw new ParameterFileWrongInputException("Der Parameter \"" +paramName+"\" wurde mehrmals definiert\n"
										+ "Der Parameter \"" + paramName + "\" wurde auf folgenden Wert gesetzt: " + paraWert+ " " +paraEinheit );
							}catch(ParameterFileWrongInputException e){
								e.log_Message();
							}
						}

						eingabeParameter.put(paramName, paraWertEinheit);	    					
					}		    		 

				}
				//Schauen, ob die aktuelle Zeile der Anfang von dem nächsten Block ist.
				//Wenn ja, dann aussteigen.
				if(currentLine.startsWith("[") && blockGefunden){
					break;
				}

				//[blockName]-block suchen. Wenn gefunden, dann blockGefunden
				//auf True setzen, damit die nächste Iteration richtig abläuft
				if(currentLine.startsWith(blockName)){
					blockGefunden = true;
				}

				//Nächste Zeile einlesen
				currentLine = br.readLine();
			}  //End while
			if(blockGefunden!=true){
				throw new ParameterFileWrongInputException("Im angegebenen Inputfile gibt es den Parameterblock \""+ blockName +"\" nicht");
			}
			fr.close();
			br.close();	    


			//Exceptions fangen
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return eingabeParameter; 
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
								throw new ParameterFileWrongInputException("falsche Eingabe im Inputfile: "+ currentLine +"\n"+
										"Berechnungsmodule muessen wie folgt eingegeben werden: \n" +
										"[MODULKENNZEICHNUNG: MODULNAME] z.B.: \t " +
										" [" + WandWaermeUebergang.WANDWAERME_FLAG+": " + WandWaermeUebergang.WANDWAERMEMODELLE[0]+"] \n " +
								"Soll kein Berechnungsmodul definiert werden, duerfen eckige Klammern nicht am Zeilenanfang stehen!");								
							}catch(ParameterFileWrongInputException e){
								e.stopBremo();
							}

						}else{		    			 
							modulFlag=currentLine.substring(idxModulNameAnfang, idxModulNameEnde);

							//int idxModulAuswahlEnde=currentLine.indexOf("]")-1;
							modulWahl=currentLine.substring(idxModulNameEnde+1, currentLine.length()-1);
							if(berechnungsModule.containsKey(modulFlag)){	
								try{
									throw new ParameterFileWrongInputException("Das Berechnungsmodul \"" +modulFlag+"\" " +
											"wurde mehrmals definiert und auf folgenden Wert gesetzt: " + modulWahl );
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
								throw new ParameterFileWrongInputException("Fuer den Parameter " + paramNameTemp+ " wurden keine Einheiten angegeben \n" 
										+"Diese muessen in rechteckigen Klammern angegeben werden z.B. so: [bar]"); 
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
								throw new ParameterFileWrongInputException("Der Parameter \"" +paramName+"\" wurde mehrmals definiert " +
										"und auf folgenden Wert gesetzt: " + paraWert+ " " +paraEinheit );
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
