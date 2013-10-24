import p2.HalloSager2;




public class HalloSager {
public HalloSager(){
		
	}
	
	public void SayHallo(){
		System.out.println("Liebe Gruesse aus der java1.6 Welt ");
		HalloSager2 hs2=new HalloSager2();
		hs2.sagAuchMalHallo();
		//CPPCaller cppC=new CPPCaller();
	}
	
	public void callCpp(){
		CPPCaller cppC=new CPPCaller();
		cppC.setDeepDepthD(5, 55);
		System.out.println(cppC.getDeepDepthD(1));
		
	}
	
	public static void main(String[] args) {
		System.out.println("kacke oder doch nich...");
		CPPCaller cppC=new CPPCaller();
		//cppC.sagMalHallo();
}
	
}
