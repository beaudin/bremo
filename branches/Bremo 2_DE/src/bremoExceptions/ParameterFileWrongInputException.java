package bremoExceptions;

public class ParameterFileWrongInputException extends StopBremoException{

	public ParameterFileWrongInputException(String message) {
		super(message);		
	}
//hier k�nnte man versuchen den Fehler abzufangen indem man so etwas aufruft wie 
	//set_ParameterFile();	
}
