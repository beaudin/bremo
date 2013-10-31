package p2;

public class CPPCaller {	
	
	/**
	 * Objektreference on the C++ side */
	private final byte [] reference;	
	
	
	public CPPCaller(int i){
		reference=getCPPObjectReference();		
	}

	public CPPCaller(){		
		reference=getCPPObjectReference();	
	}

	static {
		System.loadLibrary("CPPCallerImp");
		}

	private native byte[] getCPPObjectReference();


	/**
	 * Set the depth value for the current shape
	 * @param depth The depth value to be set */
	private native void setDepth(byte[] reference, int depth);

	/**
	 * Get the depth of this shape
	 * @return Returns the depth of this shape */
	private native int getDepth(byte [] reference);
	
	private native void setDepthD(byte[] reference, double depth);
	
	private native double getDepthD(byte [] reference);
	
	private native double getDeepDepthD(byte [] reference, int i);
	
	private native double getDeepDepthDPtrRef(byte [] reference, int i);
	
	private native void setDeepDepthD(byte [] reference, double d, int i);

	private native void sagMalHallo(byte [] reference);
	
	
	
	//----- PUBLIC AREA ------------------
	public void sagMalHallo(){
		sagMalHallo(this.reference);
	}	
	
	
	/**
	 * Set the depth value for the current shape
	 * @param depth The depth value to be set */
	public void setDepth(int depth) {
		setDepth(reference,depth);
		
	}

	/**
	 * Get the depth value of this shape
	 * @return Returns the depth of this shape */
	public int getDepth() {
		return getDepth(reference);
	}   
	
	
	
	/**
	 * Set the depth value for the current shape
	 * @param depth The depth value to be set */
	public void setDepthD(double depth) {
		setDepthD(reference,depth);
	}

	/**
	 * Get the depth value of this shape
	 * @return Returns the depth of this shape */
	public double getDepthD() {
		return getDepthD(reference);
	}  
	
	public double getDeepDepthD(int i){
		return this.getDeepDepthD(reference,i);
	}
	
	public void setDeepDepthD(double d, int i){
		this.setDeepDepthD(reference, d, i);
	}
	
	public double getDeepDepthDPtrRef(int i){		
		return getDeepDepthDPtrRef(reference,i);
	}

}
