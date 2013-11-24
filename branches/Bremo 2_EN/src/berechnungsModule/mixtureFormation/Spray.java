package berechnungsModule.mixtureFormation;

import misc.VectorBuffer;
import berechnungsModule.ResultsBuffer;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public class Spray extends Injection {

	private Packet_Hiroyasu [][] pakete;
	//	private Paket_Stiesch_Thoma[][] pakete;
	private double mfl_gesamt;
	private double paketErzeugungszeit;
	private double einspritzDauer;
	private double einspritzDruck;
	private double tEinspritzBeginn; //in s
	private int axialAnzahl,radialAnzahl;
	public static final String FLAG="Hiroyasu";
	private final double ANZ_SPL;
	private ResultsBuffer ergBuf;
	private VectorBuffer dmKRst_buffer;
	double rhoK_fl, cd;
	double m_dot;

	private boolean verdampfungAbgeschlossen=false;
	private boolean[][] verdampfungAbgeschlossenPaket;

	public Spray (CasePara cp,int idxOfInjection){
		super(cp,idxOfInjection);
		ergBuf=new ResultsBuffer(cp,"Hiroyasu_");
		dmKRst_buffer =new VectorBuffer(cp);

		this.ANZ_SPL=CP.get_anzSPL(idxOfInjection);
		this.axialAnzahl=CP.get_anzAxialPakete(idxOfInjection);
		this.radialAnzahl=CP.get_anzRadialPakete(idxOfInjection);
		String vergleichsKraftstoff=CP.get_fuelPropsForEvap(idxOfInjection);
		FuelProperties krstEigenschaften=new FuelProperties(vergleichsKraftstoff);
		double Tkrst_0=super.get_T_fuel_liq();
		rhoK_fl=krstEigenschaften.get_rhoK(Tkrst_0);
		einspritzDruck=CP.get_einspritzDruck(idxOfInjection);		
		mfl_gesamt=super.get_mFuel_Cycle(); //in Kg
		tEinspritzBeginn=super.get_BOI();
		einspritzDauer=super.get_EOI()-tEinspritzBeginn; 	 //in s
		paketErzeugungszeit=einspritzDauer/axialAnzahl;
		m_dot=mfl_gesamt/einspritzDauer;

		//Berechnet den Faktor cRad so, dass die die Geschwindigkeit des aeussersten Pakets
		//pF*100% von der Geschwindigkeit des Pakets auf der Strahlachse betraegt
		double pF=CP.get_profileFactor(super.INDEX); //15. Nov. 2012 War bislang 0.5 
		double cRad=Math.log(pF)/((radialAnzahl-1)*(radialAnzahl-1));	
		//Berechnung der Kraftstoffmasse in einem Paket
		double mfl_Paket_0=mfl_gesamt/radialAnzahl/axialAnzahl/ANZ_SPL;

		//SpritzlochDurchmesser
		double d0=CP.get_durchmSPL(super.INDEX);
		//CD-Faktor
		cd=CP.get_CdFactor(super.INDEX);

		double EntrainmentFaktor_C1=CP.get_entrainmentFactor(super.INDEX);

		verdampfungAbgeschlossenPaket=new boolean[axialAnzahl][radialAnzahl];
		for(int ax=0;ax<axialAnzahl;ax++)
			for(int rad=0;rad<radialAnzahl;rad++)
				verdampfungAbgeschlossenPaket[ax][rad]=false;


		if(super.boi<super.CP.SYS.SIMULATION_START_SEC){	//TODO &&!super.isLWA --> war das noetig?
			try {
				throw new ParameterFileWrongInputException("" +
						"Fuer das gewaehlte Einspritzmodell " +this.FLAG+ " der " +idxOfInjection+
						"ten Einspritzung lag der Einspritzbeginn vor dem Rechenbeginn der DVA. \n"+
						"Fuer das gewahlte Einspritzmodell ist dies nicht zulaessig");
			} catch (ParameterFileWrongInputException e) {				
				e.log_Warning();
			}
			super.mKrst_dampf.addValue(CP.SYS.SIMULATION_START_SEC,mfl_gesamt);
			super.mKrst_fluessig.addValue(CP.SYS.SIMULATION_START_SEC, 0);			
		}else{
			super.mKrst_dampf.addValue(CP.SYS.SIMULATION_START_SEC,0 );
			super.mKrst_fluessig.addValue(CP.SYS.SIMULATION_START_SEC, 0);			
		}		

		pakete=new Packet_Hiroyasu[axialAnzahl][radialAnzahl];

		for(int iL=0; iL<=axialAnzahl-1;iL++){
			for(int iR=0; iR<=radialAnzahl-1;iR++){					
				pakete[iL][iR]=new Packet_Hiroyasu(iL,iR,mfl_Paket_0,
						paketErzeugungszeit,											   
						einspritzDruck,
						tEinspritzBeginn,
						Tkrst_0,
						cRad,
						d0,
						cd,
						EntrainmentFaktor_C1,
						krstEigenschaften,
						cp);
			}
		}
		double a;
	}

	public int get_nbrOfRadPackets(){
		return this.radialAnzahl;
	}

	public int get_nbrOfAxPackets(){
		return this.axialAnzahl;
	}


	public int get_nbrOfPacketsAlive(double time){
		int nbrOfPacketsAlive=0;
		for(int i=0;i<pakete.length;i++) 
			for(int j=0;j<pakete[i].length;j++)
				if(pakete[i][j].get_lifetime(time)>0)
					nbrOfPacketsAlive+=1;

		return nbrOfPacketsAlive;		
	}


	public Packet_Hiroyasu get_packet(int axialPos,int radialPos ){
		return pakete[axialPos][radialPos];
	}	


	@Override
	public double get_T_fuelVapor(double time, Zone zn) {
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


	public double get_T_fuelVapor_Packet(double time, Zone zn, int axIndex, int radIndex) {

		//Es wird angenommen, dass das Paket die Temperatur der Zone hat
		double T_krst_dampf=this.pakete[axIndex][radIndex].get_TKrst_dampf(time,zn.get_T());	

		return T_krst_dampf;
	}



	@Override
	public double get_dQ_fuelVapor(double time, Zone zn) {
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

	public double get_dQ_fuelVapor_Packet(double time, Zone zn, int ax, int rad) {
		double dQ=0;
		if(this.verdampfungAbgeschlossenPaket[ax][rad]==false){			
			//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
			dQ=this.pakete[ax][rad].get_dQ(time, zn);	
		}
		return ANZ_SPL*dQ;
	}



	@Override
	public double get_diff_mFuel_vapor(double time, Zone zn) {
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

	public double get_diff_m_fuelVapor_Packet(double time, Zone zn, int axIndex, int radIndex) {
		double dm=0;
		if(this.verdampfungAbgeschlossenPaket[axIndex][radIndex]==false){	
			//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
			dm=this.pakete[axIndex][radIndex].get_dmKrst(time, zn);
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
	 * Liefert das Differential der zum Zeitpunkt "time" in alle Pakete aufgrund von "air eintrainment" eingesaugten Luft
	 * @param time
	 * @return
	 */
	public double get_dma_Packet(double time, int axIndex,  int radIndex) {
		double dma=0;		
		//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
		dma=this.pakete[axIndex][radIndex].get_dma_Paket(time);

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

	/**
	 * Liefgert die Menge des eingespritzten Krst fuer jedes Paket
	 * @param time
	 * @return
	 */
	private double calc_mKRstEingespritzt_Paket(double time, int axIndex,  int radIndex) {
		double mKRstFl=0;		
		//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
		mKRstFl=this.pakete[axIndex][radIndex].get_mKrsEingespritzt(time);		
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

	private double calc_mKRstFluessig_Paket(double time, int axIndex,  int radIndex) {
		double mKRstFl=0;
		//Wenn ein Paket noch nicht erzeugt wurde wird null zurückgegeben
		mKRstFl=this.pakete[axIndex][radIndex].get_mKrstFl(time);		
		return ANZ_SPL*mKRstFl;
	}

	/**
	 * Returns the flux of kinetic energy due to the injection
	 * dE_kin/dt [kg m^2/s^3]
	 */
	public double get_kineticEnergyFlux(double time, double pCyl) {
		if(time>=this.boi && time<=this.eoi){
			double deltaP=this.einspritzDruck-pCyl;
			double v0=cd*Math.sqrt(2*deltaP/this.rhoK_fl);
			return 0.5*m_dot*v0*v0;
		}else 
			return 0;
	}

	@Override
	public void calculateIntegralvalues(double time, Zone zn) {		

		//Die Integration des Verdampften Krst ist nicht ganz sauber!!
		//Der Zustand der Zone entspricht dem grade berechneten Zeitschrit 
		//--> dmKrst passt also zu time
		//m_D_T allerdings passt zu time-WRITE_INTERVAL_SEC 
		//--> es ergibt sich eine Abweichung zwischen der 
		//Masse an Krst die in der Zone ist und dem hier berechneten Wert
		dmKRst_buffer.addValue(time, get_diff_mFuel_vapor(time,zn));
		//		if(Math.abs(time-CP.convert_KW2SEC(-47.5))<0.5*CP.SYS.WRITE_INTERVAL_SEC){ //Rechnet bis KW und schreibt dann alle Werte ins txt-file	
		//			System.out.println("I am plotting...");
		//		}

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
		super.mKrst_fluessig.addValue(time, mKRstFl);	


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


	public void calculateIntegralvalues(double time, Zone zn, int axIndex,  int radIndex) {	

		if(dmKRst_buffer.get_maxTime()<time)
			//Fuer das erste Paket, das in diesem Zeitschritt eingetragen wird
			dmKRst_buffer.addValue(time, get_diff_m_fuelVapor_Packet(time,zn,axIndex,radIndex));
		else{
			dmKRst_buffer.addValue(time, dmKRst_buffer.getValue(time)+
					get_diff_m_fuelVapor_Packet(time,zn,axIndex,radIndex));
		}			


		if(time>mKrst_dampf.get_maxTime())
			//Fuer das erste Paket, das in diesem Zeitschritt eingetragen wird
			mKrst_dampf.addValue(time, 
					get_diff_m_fuelVapor_Packet(time,zn,axIndex,radIndex)*super.CP.SYS.WRITE_INTERVAL_SEC);
		else{
			double mKrst_verdampft=mKrst_dampf.getValue(time)
					+get_diff_m_fuelVapor_Packet(time,zn,axIndex,radIndex)*super.CP.SYS.WRITE_INTERVAL_SEC;
			mKrst_dampf.addValue(time, mKrst_verdampft);
		}				


		//double mKRstFl=this.calc_mKRstEingespritzt_Paket(time,axIndex,radIndex)-mKrst_dampf.getValue(time);
		double mKrstFlNeu=calc_mKRstFluessig_Paket(time,axIndex,radIndex);	
		if(time>mKrst_fluessig.get_maxTime())
			//Fuer das erste Paket, das in diesem Zeitschritt eingetragen wird
			mKrst_fluessig.addValue(time, mKrstFlNeu);
		else{
			mKrstFlNeu=mKrst_fluessig.getValue(time)+mKrstFlNeu;		
			super.mKrst_fluessig.addValue(time, mKrstFlNeu);	
		}

		//do this only once
		if(CP.SYS.DUBUGGING_MODE&&axIndex==this.axialAnzahl-1&&radIndex==this.radialAnzahl-1)
			bufferErgebnisse(time,zn);
		
		if(this.pakete[axIndex][radIndex].get_lifetime(time)<=0){	
			this.pakete[axIndex][radIndex].initialise(zn);//							
		}else{						
			//Es wird angenommen, dass das Paket die Temperatur der Zone hat
			this.pakete[axIndex][radIndex].berechneZeitschritt(zn, time, time+CP.SYS.WRITE_INTERVAL_SEC);

			if(this.pakete[axIndex][radIndex].verdampfungIsAbgeschlossen()==true)
				verdampfungAbgeschlossenPaket[axIndex][radIndex]=true;
			else
				verdampfungAbgeschlossenPaket[axIndex][radIndex]=false;
		}
	}

	public boolean evaporationFinished(){
		boolean a= true;
		for(int i=0;i<pakete.length;i++) 
			for(int j=0;j<pakete[i].length;j++)
				if(verdampfungAbgeschlossenPaket[i][j]==false)
					a=false;
		return a;

	}
	
	public boolean evaporationFinishedPaket(int axIndex,  int radIndex){		
		return verdampfungAbgeschlossenPaket[axIndex][radIndex];
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
			
			j=pakete[i].length-1;
			value=this.pakete[i][j].get_dma_Paket(time);
			name="dma"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
			j=0;
			value=this.pakete[i][j].get_s(time);
			name="s"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;
			
			j=pakete[i].length-1;
			value=this.pakete[i][j].get_s(time);
			name="s"+"_"+i+"_"+j;
			ergBuf.buffer_EinzelErgebnis(name, value, spalte);
			spalte=spalte+1;

		}		
	}


	private double get_paketLaenge(Packet_Hiroyasu paket,double time){
		double paketLaenge;
		//		paketLaenge=paket.get_s(time)-paket.get_s(time-paketErzeugungszeit);	
		paketLaenge=paket.get_s(time)-pakete[paket.get_axialPosIndex()-1][paket.get_radialPosIndex()].get_s(time);		
		if(paketLaenge<=0){
			try{
				throw new BirdBrainedProgrammerException("The length of a packet canno be zero or negativ!");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return paketLaenge;
	}

	private double get_paketDurchmesser(Zone zn,Packet_Hiroyasu paket,double time,double pZyl,double T_paket){
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

	private double get_xPos(Packet_Hiroyasu paket, double rhoL,double time,double pZyl){
		double xPos;
		xPos=paket.get_s(time)-get_paketLaenge(paket,time)/2;
		return xPos;    			
	}

	private double get_yPos(Zone zn, Packet_Hiroyasu paket,double rhoL,double time,double pZyl,double T){ 
		double yPos=0;
		double durchmesserInnen=0;
		if((paket.get_radialPosIndex()-1)>=0){
			Packet_Hiroyasu paketInnen=pakete[paket.get_axialPosIndex()][paket.get_radialPosIndex()-1];
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

