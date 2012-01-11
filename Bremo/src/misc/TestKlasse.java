package misc;

import berechnungsModule.ModulPrototyp;

public class TestKlasse extends ModulPrototyp {	
	protected String message;
	public TestKlasse(){		
	}
	public TestKlasse(String s){
		super();
		message=s;
	}
	
	public void tellMessage(){

		System.out.println(message);
	}
	
	public static TestKlasse getInstance(int i){
		TestKlasse a;
		if(i==1){
			a=new OhneModifierTest();			
		}else{
			a=new ProtectedKonstruktorTest();
		}
		
		return	a; 
	}
	
	
	

}
