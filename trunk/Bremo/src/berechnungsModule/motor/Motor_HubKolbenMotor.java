package berechnungsModule.motor;

import bremo.parameter.CasePara;


/**
 * @author   hoenl, busch
 */
public class Motor_HubKolbenMotor extends Motor{
		
	/**
	 * @uml.property  name="Pleuellaenge"
	 */
	private final double PLEUELLAENGE;			//m
	/**
	 * @uml.property  name="bohrung"
	 */
	private final double BOHRUNG ;				//m
	/**
	 * @uml.property  name="hub"
	 */
	private final double HUB ;					//m
	private final double KURBELRADIUS;			//m, nicht gleich 0,5xHub bei einer Desachsierung bzw. einer Schränkung...
	private final double EPS ;  		//-
	private final double KOLBENFLÄCHE ; 		//m²
	private final double BRENNRAUM_DACH_FLAECHE; //m²
	private final double FEUERSTEGHOEHE;		//m
	private final double SCHRAENKUNG ;			//m
	private final double DESACHSIERUNG;			//m	
	private final double AUSLASSSCHLUSS ; 		//°KWnZOT
	private final double AUSLASSOEFFNET ; 		//°KWnZOT
	private final double EINLASSSCHLUSS ; 		//°KWnZOT
	private final double EINLASSOEFFNET ; 		//°KWnZOT
	private final double EV_HUB;				//m
	private final double EV_HUB_MAX;			//m
	private final double QUETSCHSPALTHOEHE;		//m
	private final CasePara CP;	
	
	
	public Motor_HubKolbenMotor(CasePara cp){
		super(cp);
		CP=super.CP;
		PLEUELLAENGE =CP.get_Pleuellaenge();			//m
		BOHRUNG= CP.get_Bohrung();						//m
		KURBELRADIUS = CP.get_Kurbelradius();		//m
		EPS=CP.get_Verdichtung();  					//-
		KOLBENFLÄCHE =CP.get_Kolbenflaeche(); 		//m^2
		BRENNRAUM_DACH_FLAECHE =CP.get_Brennraumdachflaeche(); //m^2
		FEUERSTEGHOEHE =CP.get_Feuersteghoehe();		//m
		SCHRAENKUNG =CP.get_Schraenkung();			//m
		DESACHSIERUNG =CP.get_Desachsierung();		//m	
		AUSLASSSCHLUSS =CP.get_Auslassoeffnet(); 		//°KWnZOT
		AUSLASSOEFFNET =CP.get_Auslassschluss(); 		//°KWnZOT
		EINLASSSCHLUSS =CP.get_Einlassschluss(); 		//°KWnZOT
		EINLASSOEFFNET =CP.get_Einlassoeffnet(); 		//°KWnZOT	
		EV_HUB=CP.get_EV_Hub();							//m
		EV_HUB_MAX=CP.get_EV_Hub_max();					//m
		QUETSCHSPALTHOEHE=CP.get_Quetschspalthoehe();	//m
	
		//Hub wird aus dem Kolbenweg berechnet
		double sMin=0;
		double sMax=0;
		double toleranz=1E-8; //in °KW
		double a0=0;
		double a1=10;	//10° KW nOT
		int idx=0;
		//Minimum des Hubes suchen
		while(Math.abs(a0-a1)>Math.PI/180*toleranz){ 
			a0=a1;
			a1=-get_dS_dKW(CP.convert_KW2SEC(a0))/get_d2S_dKW(CP.convert_KW2SEC(a0))+a0;
			idx++;
		}
		if(get_d2S_dKW(CP.convert_KW2SEC(a1))<=0){
			//zweite Ableitung negativ --> Maximum wurde gefunden (sollte nicht passieren können)
			sMax=get_Kolbenweg(CP.convert_KW2SEC(a1));
		}else{
			sMin=get_Kolbenweg(CP.convert_KW2SEC(a1));
		}
		
		a0=0;
		a1=190;	//190° KW nOT
		//Maximum des Hubes suchen
		while(Math.abs(a0-a1)>Math.PI/180*toleranz){
			a0=a1;
			a1=-get_dS_dKW(CP.convert_KW2SEC(a0))/get_d2S_dKW(CP.convert_KW2SEC(a0))+a0;
		}
		if(get_d2S_dKW(CP.convert_KW2SEC(a1))<=0){
			//zweite Ableitung negativ --> Maximum wurde gefunden
			sMax=get_Kolbenweg(CP.convert_KW2SEC(a1));
		}else{
			sMin=get_Kolbenweg(CP.convert_KW2SEC(a1));
		}
		HUB=sMax-sMin;
		
		
	}
	
	
	///////////////////////////////////////////////////////////////////////
	//Get-Funktionen. Diese sind public, damit jeder beliebige Funktion darauf
	//zugreifen kann
	public double get_Schraenkung() {
		return SCHRAENKUNG;
	}

	public double get_Desachsierung() {
			return DESACHSIERUNG;
		}
	
	public double get_Kurbelradius() {
			return KURBELRADIUS;
		}
	
	public double get_Hub() {
		return HUB;
	}
	
	public double get_Pleuellaenge() {
			return PLEUELLAENGE;
		}
	
	public double get_Bohrung() {
			return BOHRUNG;
		}
	
	public double get_Verdichtung() {
		return EPS;
	}

	public double get_Brennraumdachflaeche() {
		return BRENNRAUM_DACH_FLAECHE;
	}
	
	public double get_Feuersteghoehe(){
		return FEUERSTEGHOEHE;
	}
	
	public double get_Quetschspalthoehe(){
		return QUETSCHSPALTHOEHE;
	}
	
	public double get_Kolbenflaeche() {
		return KOLBENFLÄCHE;
	}
	
	public double get_Einlass_oeffnet(){
		return EINLASSOEFFNET;
	}
	
	public double get_Einlass_schliesst(){
		return EINLASSSCHLUSS;
	}
	
	public double get_Auslass_oeffnet(){
		return AUSLASSOEFFNET;
	}
	
	public double get_Auslass_schliesst(){
		return AUSLASSSCHLUSS;
	}
	
	public double get_EV_Hub(){
		return EV_HUB;
	}
	
	public double get_EV_Hub_max(){
		return EV_HUB_MAX;
	}
	
	//Ab hier müssen die Ausgaben berechnet werden
	public double get_Hubvolumen() {
		double Hubvolumen = HUB * BOHRUNG / 2 * BOHRUNG / 2 *Math.PI; 
		return Hubvolumen;
	}
	
	public double get_Kompressionsvolumen() {
		double V_c  = get_Hubvolumen() / (EPS - 1);
		return V_c;
	}
	@Override
	public double get_V_MAX() {		
		return this.get_Kompressionsvolumen()+this.get_Hubvolumen();
	}


	/**
	 * @param time
	 * @return liefert den aktuellen Kolbenweg in [m]
	 */	
	public double get_Kolbenweg(double time) {
		//Kolbenweg
		//S = r(1-cos(phi)) + [ L-sqrt(L^2-(d+s-r*sin(phi))^2) ]
		//[m]
		double phi=convertTime_TO_KW(time)* Math.PI / 180.0;
		double l = PLEUELLAENGE;
		double r = get_Kurbelradius();
		double d = DESACHSIERUNG;
		double s = SCHRAENKUNG;
		double w = 0;	//Kolbenweg
		w=r*(1-Math.cos(phi))+(l - Math.pow(Math.pow(l,2)-Math.pow(d+s-r*Math.sin(phi), 2), 0.5));	
		return w;
	}
		
	/**
	 * Liefert die momentane Kolbengeschwindigkeit in [m/s]
	 * @param time
	 * @return
	 */	
	public double get_Kolbengeschwindigkeit(double time) {
		
//		double kurbelwinkel=convertTime_TO_KW(time);
		//Momentane Kolbengeschwindigkeit = erste Ableitung des Kolbenwegs
		//Ausgabe in m/s
		return convert_dKW_TO_dTime(get_dS_dKW(time));
	}
	/**
	 * Liefert dS/dKW, die erste Ableitung vom Kolbenweg, in [m/KW]
	 * @param time
	 * @return
	 */	
	public double get_dS_dKW(double time){
		//dS = r*sin(phi)-r*cos(phi)*(d+s-r*sin(phi))/sqrt[L^2-(d+s-r*sin(phi))^2]
		//oder: dS/dAlpha=4/(pi*B^2)*dV/dAlpha
		double B = BOHRUNG;
		double dS=4/(Math.pow(B,2)*Math.PI)*get_dV_dKW(time); //dKolbenweg [m/°KW]
		return dS;
	}
	/**
	 * @param time
	 * @return liefert d2S/dKW, die zweite Ableitung vom Kolbenweg, in [m^2/KW]
	 */
	public double get_d2S_dKW(double time){
		double B=BOHRUNG;
		double d2S=4/(Math.pow(B,2)*Math.PI)*get_d2V_dKW(time);
		return d2S;
	}
	
	/**
	 * @param time
	 * @return liefert das aktuelle Zylindervolumen in [m^3]
	 */	
 	public double get_V(double time)
	{
		//Zylindervolumen		 		
		double Vc = get_Kompressionsvolumen();
		double B = BOHRUNG;
		
		double zylindervolumen = 0;
		// V=Vc + pi*B^2/4 * (L+r-r*cos(phi)-sqrt(L^2-(d+s-r*sin(phi))^2);
		//[m^3]
		zylindervolumen = Vc + Math.PI*Math.pow(B, 2)/4*get_Kolbenweg(time);
		return zylindervolumen;
	}
	
	public double get_dV_dKW(double time) {
		//Erste Ableitung von Zylindervolumen, dV/dAlpha
		double kurbelwinkel=convertTime_TO_KW(time);
		double B = BOHRUNG;
		double l = PLEUELLAENGE;
		double r = get_Kurbelradius();
		double d = DESACHSIERUNG;
		double s = SCHRAENKUNG;
		double dif_Zylindervolumen = 0;
		double phi = kurbelwinkel * Math.PI / 180.0;
		
		// dV/dAlpha = pi*B^2/4*(r*sin(phi)-r*cos(phi)*(d+s-r*sin(phi))/(sqrt(L^2-(d+s-r*sin(phi))^2)));
		// [m^3/rad]
		dif_Zylindervolumen = Math.PI*Math.pow(B,2)/4*(r*Math.sin(phi)-r*Math.cos(phi)*(d+s-r*Math.sin(phi))/
				(Math.pow(Math.pow(l,2)-Math.pow(d+s-r*Math.sin(phi),2), 0.5)));
		//Ausgabe in m^3/°KW
		dif_Zylindervolumen=dif_Zylindervolumen*Math.PI/180;
		return dif_Zylindervolumen;
	}
	
	/**
	 * Liefert die erste ABleitung des Zylindervolumens in [m^3/s]
	 * @param time
	 * @return dV 
	 */	
	public double get_dV(double time){		
		return convert_dKW_TO_dTime(get_dV_dKW(time));		
	}
	
	public double get_d2V_dKW(double time) {
		//Zweite Ableitung von Zylindervolumen, d^2V/dAlpha^2
		double kurbelwinkel=convertTime_TO_KW(time);
		double B = BOHRUNG;
		double l = PLEUELLAENGE;
		double r = get_Kurbelradius();
		double d = DESACHSIERUNG;
		double s = SCHRAENKUNG;
		double dif2_Zylindervolumen = 0;
		double phi = kurbelwinkel * Math.PI / 180.0;
		
		// d^2V/dAlpha^2 = pi*B^2/4*r*cos(phi)*[ 1 + (tan(phi)*(d+s-r*sin(phi))) / sqrt(L^2-(d+s-r*sin(phi))^2) +
		//		r*L^2*cos(phi)/(L^2-(d+s-r*sin(phi))^2)^(3/2) ]
		//[m^3/rad^2]
		dif2_Zylindervolumen = Math.PI*Math.pow(B,2)/4*r*Math.cos(phi)*(1 + Math.tan(phi)*(d+s-r*Math.sin(phi))/
				Math.pow(Math.pow(l,2)-Math.pow(d+s-r*Math.sin(phi),2), 0.5) + r*Math.cos(phi)*Math.pow(l,2)/
				Math.pow(Math.pow(l,2)-Math.pow(d+s-r*Math.sin(phi),2), 1.5) );
		//Ausgabe in m^3/°KW^2
		dif2_Zylindervolumen=dif2_Zylindervolumen*Math.pow(Math.PI/180, 2);
		return dif2_Zylindervolumen;

	}
	

	public double get_d2V(double time){		
		return convert_dKW2_TO_dTime2(get_d2V_dKW(time));		
	}


	public double get_BrennraumFlaeche(double time) {	
		
		// wie in Hohenberg und Bargende und Hensel -> "scheint ganz gut zu sein..."
		// A = A_ZK + A_kolben + A_ZLB + 0.25*A_Fst
		// A_Fst = 2 * d * pi * h_Fst
		double brennraumFlaeche = 0;
		double zylinderLaufflaeche = java.lang.Math.PI * get_Bohrung() * 
				(get_Kolbenweg(time)+get_Quetschspalthoehe());	
		double feuerstegFlaeche = 2*java.lang.Math.PI*get_Bohrung()*get_Feuersteghoehe();
		
		brennraumFlaeche = get_Brennraumdachflaeche() + get_Kolbenflaeche() + 
				zylinderLaufflaeche + 0.25*feuerstegFlaeche;
		return brennraumFlaeche;
	}	
	
	public boolean isHubKolbenMotor() {		
		return true;
	}


	
	
}
