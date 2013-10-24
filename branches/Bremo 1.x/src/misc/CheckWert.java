package misc;

public class CheckWert {
	/**
	 *Author: busch
	 */
	private CheckWert() {
		//Keine Instanz kann erzeugt werden
	}
	
	public static boolean check(double wert, double min, double max){
		if(wert>max){
			System.out.println("Fehler: Wert > "+ max + "!!!");
			return false;
		}
		else if(wert<min){
			System.out.println("Fehler: Wert < " + min + "!!!");
			return false;
		}else{
			return true;
		}
	}
	
	public static boolean check(String name, double wert, double min, double max){
		if(wert>max){
			System.out.println("Fehler: " + name + " > " + max + "!!!");
			return false;
		}
		else if(wert<min){
			System.out.println("Fehler: " + name + " < " + min + "!!!");
			return false;
		}else{
			return true;
		}
	}
}
