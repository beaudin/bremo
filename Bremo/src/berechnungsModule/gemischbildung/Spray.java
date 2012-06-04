package berechnungsModule.gemischbildung;

import misc.VektorBuffer;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class Spray extends Einspritzung {
	
	private Paket_Hiroyasu [][] pakete;
	//	private Paket_Stiesch_Thoma[][] pakete;
	private double mfl_gesamt;
	private double paketErzeugungszeit;
	private double einspritzDauer;
	private double einspritzDruck;
	private double tEinspritzBeginn; //in s
	private int axialAnzahl,radialAnzahl;
	public static final String FLAG="Hiroyasu";
	private final double ANZ_SPL;
	private ErgebnisBuffer ergBuf;
	private VektorBuffer dmKRst_buffer;;
	

	private boolean verdampfungAbgeschlossen=false;

	public Spray (CasePara cp,int index){
		super(cp,index);
		ergBuf=new ErgebnisBuffer(cp,"Hiroyasu_");
		dmKRst_buffer =new VektorBuffer(cp);

		this.ANZ_SPL=CP.get_anzSPL(index);
		this.axialAnzahl=CP.get_anzAxialPakete(index);
		this.radialAnzahl=CP.get_anzRadialPakete(index);
		String vergleichsKraftstoff=CP.get_vergleichsKrstVerdampfung(index);
		Kraftstoff_Eigenschaften krstEigenschaften=new Kraftstoff_Eigenschaften(vergleichsKraftstoff);
		einspritzDruck=CP.get_einspritzDruck(index);		
		mfl_gesamt=super.get_mKrst_ASP(); //in Kg		
		double Tkrst_0=super.get_T_Krst_fl();

		tEinspritzBeginn=super.get_BOI();
		einspritzDauer=super.get_EOI()-tEinspritzBeginn; 	 //in s
		paketErzeugungszeit=einspritzDauer/axialAnzahl;

		//Berechnet den Faktor cRad so, dass die die Geschwindigkeit des aeussersten Pakets
		//50% von der Geschwindigkeit des Pakets auf der Strahlachse betraegt
		double cRad=Math.log(0.5)/((radialAnzahl-1)*(radialAnzahl-1));	
		//Berechnung der Kraftstoffmasse in einem Paket
		double mfl_Paket_0=mfl_gesamt/radialAnzahl/axialAnzahl/ANZ_SPL;

		//SpritzlochDurchmesser
		double d0=CP.get_durchmSPL(super.INDEX);
		//CD-Faktor
		double cd=CP.get_CdFaktor(super.INDEX);




		if(super.boi<super.CP.SYS.RECHNUNGS_BEGINN_DVA_SEC){	//TODO &&!super.isLWA --> war das noetig?
			try {
				throw new ParameterFileWrongInputException("" +
						"Fuer das gewaehlte Einspritzmodell " +this.FLAG+ " der " +index+
						"ten Einspritzung lag der Einspritzbeginn vor dem Rechenbeginn der DVA. \n"+
				"Fuer das gewahlte Einspritzmodell ist dies nicht zulaessig");
			} catch (ParameterFileWrongInputException e) {				
				e.log_Warning();
			}
			super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,mfl_gesamt);
			super.mKrst_fluesssig.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, 0);			
		}else{
			super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0 );
			super.mKrst_fluesssig.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, 0);			
		}



		pakete=new Paket_Hiroyasu[axialAnzahl][radialAnzahl];

		for(int iL=0; iL<=axialAnzahl-1;iL++){
			for(int iR=0; iR<=radialAnzahl-1;iR++){					
				pakete[iL][iR]=new Paket_Hiroyasu(iL,iR,mfl_Paket_0,
						paketErzeugungszeit,											   
						einspritzDruck,
						tEinspritzBeginn,
						Tkrst_0,
						cRad,
						d0,
						cd,
						krstEigenschaften,
						cp);
			}
		}
	}	



	public Paket_Hiroyasu get_paket(int axialPos,int radialPos ){
		return pakete[axialPos][radialPos];
	}	

	@Override
	public double get_Tkrst_dampf(double time, Zone zn) {
		double sumdm=0,sumdmT=0,T_krst_dampf=-5.55;
		if(this.verdampfungAbgeschlossen==false){
			for(int i=0;i<pakete.length;i++){ 
				for(int j=0;j<pakete[i].length;j++){
					//Es wird angenommen, dass das Paket die Temperatur der Zone hat
					double Tkrst=this.pakete[i][j].get_TKrst_dampf(time,zn.get_T());
					double dmpaket=this.pakete[i][j].get_dmKrst(time, zn);
					sumdmT+=dmpaket*Tkrst;
					sumdm+=dmpaket;
				}		
			}
			//Bei der Berechnung der Mischungstemperatur nach dem 1.HS kuerzen sich die cv-Werte heraus
			if(sumdm!=0)
				T_krst_dampf= sumdmT/sumdm; 

		}		
		return T_krst_dampf;
	}



	@Override
	public double get_dQ_krstDampf(double time, Zone zn) {
		double dQ=0;
		if(this.verdampfungAbgeschlossen==false){
			for(int i=0;i<pakete.length;i++){ 
				for(int j=0;j<pakete[i].length;j++)
					//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
					dQ+=this.pakete[i][j].get_dQ(time, zn);				
			}
		}
		return ANZ_SPL*dQ;
	}



	@Override
	public double get_diff_mKrst_dampf(double time, Zone zn) {
		double dm=0;
		if(this.verdampfungAbgeschlossen==false){
			for(int i=0;i<pakete.length;i++){ 
				for(int j=0;j<pakete[i].length;j++)
					//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
					dm+=this.pakete[i][j].get_dmKrst(time, zn);
			}
		}		
		return ANZ_SPL*dm;
	}



	/**
	 * Liefert die Masse der zum Zeitpunkt "time" aufgrund von "air entrainment" insgesamt in alle Pakete eingesaugten Luft 	 
	 * @param time
	 * @return
	 */
	public double get_ma(double time) {
		double ma=0;
		for(int i=0;i<pakete.length;i++){ 
			for(int j=0;j<pakete[i].length;j++)
				//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
				ma+=this.pakete[i][j].get_ma_Paket(time);
		}
		return ANZ_SPL*ma;
	}



	/**
	 * Liefert das Differential der zum Zeitpunkt "time" in alle Pakete aufgrund von "air eintrainment" eingesaugten Luft
	 * @param time
	 * @return
	 */
	public double get_dma(double time) {
		double dma=0;
		for(int i=0;i<pakete.length;i++){ 
			for(int j=0;j<pakete[i].length;j++)
				//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
				dma+=this.pakete[i][j].get_dma_Paket(time);
		}
		return ANZ_SPL*dma;
	}
	
	/**
	 * Liefgert die Menge des eingespritzten Krst
	 * @param time
	 * @return
	 */
	private double calc_mKRstEingespritzt(double time) {
		double mKRstFl=0;
		for(int i=0;i<pakete.length;i++){ 
			for(int j=0;j<pakete[i].length;j++)
				//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
				mKRstFl+=this.pakete[i][j].get_mKrsEingespritzt(time);
		}
		return ANZ_SPL*mKRstFl;
	}
	
	private double calc_mKRstFluessig(double time) {
		double mKRstFl=0;
		for(int i=0;i<pakete.length;i++){ 
			for(int j=0;j<pakete[i].length;j++)
				//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
				mKRstFl+=this.pakete[i][j].get_mKrstFl(time);
		}
		return ANZ_SPL*mKRstFl;
	}
	

	@Override
	public void berechneIntegraleGroessen(double time, Zone zn) {		
		
		//Die Integration des Verdampften Krst ist nicht ganz sauber!!
		//Der Zustand der Zone entspricht dem grade berechneten Zeitschrit 
		//--> dmKrst passt also zu time
		//m_D_T allerdings passt zu time-WRITE_INTERVAL_SEC 
		//--> es ergibt sich eine Abweichung zwischen der 
		//Masse an Krst die in der Zone ist und dem hier berechneten Wert
		dmKRst_buffer.addValue(time, get_diff_mKrst_dampf(time,zn));
		if(Math.abs(time-CP.convert_KW2SEC(-47.5))<0.5*CP.SYS.WRITE_INTERVAL_SEC){ //Rechnet bis KW und schreibt dann alle Werte ins txt-file	
			System.out.println("I am plotting...");
		}
		
		double mKrst_verdampft=mKrst_dampf.getValue(mKrst_dampf.get_maxTime());
		if(time>=CP.SYS.WRITE_INTERVAL_SEC){
			double t=mKrst_dampf.get_maxTime();
			if(time>mKrst_dampf.get_maxTime()){ //time-CP.SYS.WRITE_INTERVAL_SEC=t liefert einen Fehler da der Wert numerisch zu ungenau ist!!
				mKrst_verdampft=mKrst_dampf.getValue(t)
				+this.dmKRst_buffer.getValue(time)*super.CP.SYS.WRITE_INTERVAL_SEC;
			}
		}		
		super.mKrst_dampf.addValue(time,mKrst_verdampft );
		double mKRstFl=this.calc_mKRstEingespritzt(time)-mKrst_dampf.getValue(time);
		mKRstFl=calc_mKRstFluessig(time);
		super.mKrst_fluesssig.addValue(time, mKRstFl);	

		
		if(CP.SYS.DUBUGGING_MODE)
			bufferErgebnisse(time,zn);

		//Nachdem alle Werte berechnet sind können die Tropfen 
		//auf den nächsten Wert integriert werden
		this.verdampfungAbgeschlossen=true;
		for(int i=0;i<pakete.length;i++){ 
			for(int j=0;j<pakete[i].length;j++){
				//Die Pakete werden so lange mit den aktuellen werten in der Zone aktualisiert 
				//bis sie in den Brennraum eingedrungen sind --> dann haben Sie ihre Startwerte
				if(this.pakete[i][j].get_lifetime(time)<=0){	
					this.pakete[i][j].initialise(zn);//							
				}else{						
					//Es wird angenommen, dass das Paket die Temperatur der Zone hat
					this.pakete[i][j].berechneZeitschritt(zn, time, time+CP.SYS.WRITE_INTERVAL_SEC);
					if(this.pakete[i][j].verdampfungIsAbgeschlossen()==false)
						verdampfungAbgeschlossen=false;
				}
			}		
		}
		

	}



	private void bufferErgebnisse(double time, Zone zn ){
		ergBuf.buffer_EinzelErgebnis("KW", CP.convert_SEC2KW(time), 0);
		int spalte=1;
		for(int i=0;i<pakete.length;i++){	
			int j=0;
			double value=this.pakete[i][j].get_smd();
			String name="SMD"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
		
			j=pakete[i].length-1;
			value=this.pakete[i][j].get_smd();
			name="SMD"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=0;
			value=this.pakete[i][j].get_T_krstTropfen();
			name="T_krstTr"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=pakete[i].length-1;
			value=this.pakete[i][j].get_T_krstTropfen();
			name="T_krstTr"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=0;
			value=this.pakete[i][j].get_m_krstTropfen();
			name="m_krstTr"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=pakete[i].length-1;
			value=this.pakete[i][j].get_m_krstTropfen();
			name="m_krstTr"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
			j=0;
			value=this.pakete[i][j].get_v(time);
			name="v"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=pakete[i].length-1;
			value=this.pakete[i][j].get_v(time);
			name="v"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
			j=0;
			value=this.pakete[i][j].get_mKrstFl(time);
			name="mfl"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=pakete[i].length-1;
			value=this.pakete[i][j].get_mKrstFl(time);
			name="mfl"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
			j=0;
			value=this.pakete[i][j].get_dQ(time, zn);
			name="dQvap"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

			j=pakete[i].length-1;
			value=this.pakete[i][j].get_dQ(time, zn);
			name="dQvap"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
			j=0;
			value=this.pakete[i][j].get_dma_Paket(time);
			name="dma"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
		}		
	}


	private double get_paketLaenge(Paket_Hiroyasu paket,double time){
		double paketLaenge;
		//		paketLaenge=paket.get_s(time)-paket.get_s(time-paketErzeugungszeit);
		paketLaenge=paket.get_s(time)-pakete[paket.get_axialPosIndex()-1][paket.get_radialPosIndex()].get_s(time);		
		return paketLaenge;
	}

	private double get_paketDurchmesser(Zone zn,Paket_Hiroyasu paket,double time,double pZyl,double T_paket){
		int axialPosPaket=paket.get_axialPosIndex();
		int radialPosPaket=paket.get_radialPosIndex();
		double paketDurchmesser=0;
		double paketDurchmesserInnen,l_1,vol_1;
		if( paket.get_lifetime(time)>0){
			for(int axialIndex=0; axialIndex<=axialPosPaket; axialIndex++){				
				for(int radialIndex=0; radialIndex<=radialPosPaket; radialIndex++){				
					paketDurchmesserInnen=paketDurchmesser;
					l_1=get_paketLaenge(pakete[axialIndex][radialIndex],time);
					vol_1=pakete[axialIndex][radialIndex].get_paketVolumen(zn,time,T_paket);	
					if(radialIndex==0){
						paketDurchmesser=Math.sqrt(4/Math.PI*(vol_1/l_1));
					}
					else{
						paketDurchmesser=Math.sqrt(4/Math.PI*(vol_1/l_1)+paketDurchmesserInnen*paketDurchmesserInnen);										
					}
				}		
			} 	
		}
		else
			paketDurchmesser=0;  
		return paketDurchmesser;		
	}

	private double get_xPos(Paket_Hiroyasu paket, double rhoL,double time,double pZyl){
		double xPos;
		xPos=paket.get_s(time)-get_paketLaenge(paket,time)/2;
		return xPos;    			
	}

	private double get_yPos(Zone zn, Paket_Hiroyasu paket,double rhoL,double time,double pZyl,double T){ 
		double yPos=0;
		double durchmesserInnen=0;
		if((paket.get_radialPosIndex()-1)>=0){
			Paket_Hiroyasu paketInnen=pakete[paket.get_axialPosIndex()][paket.get_radialPosIndex()-1];
			durchmesserInnen=get_paketDurchmesser(zn,paketInnen,time,pZyl,T);
		}
		yPos=(get_paketDurchmesser(zn,paket,time,pZyl,T)+durchmesserInnen)/4;
		return yPos;
	}

	/**
	 * liefert Xpos	
	 * @param rhoL in kg/m3
	 * @param time in s
	 * @param pZyl in N/m2
	 * @param T in K
	 * @return
	 */
	public double [] get_alleXpos(double rhoL,double time,double pZyl){
		double [] alleXpos=new double [axialAnzahl*radialAnzahl];
		for(int axialPos=0; axialPos<axialAnzahl; axialPos++){	
			for(int radialPos=0; radialPos<radialAnzahl; radialPos++){
				alleXpos[axialPos*radialAnzahl+radialPos]=
					get_xPos(pakete[axialPos][radialPos],rhoL,time,pZyl);
			}
		}
		return alleXpos;
	}

	/**
	 * liefert Ypos
	 * @param rhoL in kg/m3
	 * @param time in s
	 * @param pZyl in N/m2
	 * @param T in K
	 * @return
	 */
	public double [] get_alleYpos(Zone zn, double rhoL,double time,double pZyl,double T){
		double [] alleYpos=new double [axialAnzahl*radialAnzahl];
		for(int axialPos=0; axialPos<axialAnzahl; axialPos++){	
			for(int radialPos=0; radialPos<radialAnzahl; radialPos++){			
				alleYpos[axialPos*radialAnzahl+radialPos]=
					get_yPos(zn,pakete[axialPos][radialPos],rhoL,time,pZyl,T);
			}
		}
		return alleYpos;
	}

	//    private double get_Eindringtiefe(Paket_Hiroyasu paket, double time){
	//    	double Eindringtiefe;
	//    	Eindringtiefe=paket.get_s(time);
	//		return Eindringtiefe;    			
	//    }

	public double [] get_alleEindringtiefe(double rhoL,double time,double pZyl){
		double [] alleEindringtiefe=new double [axialAnzahl*radialAnzahl];
		for(int axialPos=0; axialPos<axialAnzahl; axialPos++){	
			for(int radialPos=0; radialPos<radialAnzahl; radialPos++){
				alleEindringtiefe[axialPos*radialAnzahl+radialPos]=
					pakete[axialPos][radialPos].get_s(time);
			}
		}
		return alleEindringtiefe;
	}

}

