package bremoExceptions;

public class ParameterFileWrongInputException extends StopBremoException{

	public ParameterFileWrongInputException(String message) {
		super(message);		
	}
//hier könnte man verscuhen den fehler abzufangen indem man so wtwas aufruft wie 
	//set_ParameterFile();	
}
