package bremoExceptions;

@SuppressWarnings("serial")
public class CaseParaNotInstantiatedException extends StopBremoException {	
	
	public CaseParaNotInstantiatedException(){
		super("Es wurde versucht eine Methode von CasePara aufzurufen, \n " +
				"ohne dass eine Instanz von CasePara existiert!");
		
	}
	
}
