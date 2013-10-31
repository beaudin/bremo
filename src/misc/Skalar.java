package misc;

public class Skalar {
	
	private double skalar;
	private String Einheit;
	
	public Skalar(double neuerWert, String neueEinheit){
		skalar=neuerWert;
		Einheit=neueEinheit;
	}
	
	public double getWert(){
		return skalar;
	}
	
	public String getEinheit(){
		return Einheit;
	}

}
