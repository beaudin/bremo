package bremoExceptions;

public class ParameterFileWrongInputException extends StopBremoException{

	public ParameterFileWrongInputException(String message) {
		super(message);		
	}
//hier k�nnte man verscuhen den fehler abzufangen indem man so wtwas aufruft wie 
	//set_ParameterFile();	
}
