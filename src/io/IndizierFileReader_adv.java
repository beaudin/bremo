package io;



import java.io.*;

import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IndizierFileReader_adv extends IndizierFileReader{

	private static String ext = ".adv";
	private String [] zeitEinheiten={"[KW]","[s]"};
	private String zeitEinheit, einheitPZyl, einheitPEin,einheitPAbg; 
	private double kf_pZyl,kf_pEin,kf_pAbg; //Faktoren für die Umrechnung von bar nach Pa
	private int spalte_pZyl;
	private int spalte_pEin;
	private int spalte_pAbg;	
	private File file;
	private boolean dreiDruecke=true;

	private double Drehzahl;

	private int A,B,L1,C,L2,D,L3,E,L4;
	private double[] Setup;
	private byte[] buf;
	private double  Dateiarray,        Kanalanzahl,       BITS, 
	OT_Verschiebung,   Clocks,            Zyklen,     
	Motor_r,           hub,        bohrung,  
	verdichtung, pleuel_L,  pulseProAS,
	aZyk;

	private double []  OT, Messbereich, Verstaerkung, Drift;

	// DriftKompensationsVariabeln
	private double[]  Polytropenexponent, Referenzzeitpunkt,   Bereich, 
	Referenzwert;

	//Messbereiche. Variablen
	private double[] MR_MIN, MR_MAX;

	//Verstärkungsfaktor und offsetsVariablen
	private double []    ZLF = new double[1],   OZ = new double[1] ,    M = new double[1];
	private double ACC;

	//Variablen zu auslesen der messinformationen
	private double  Messdaten, Messinfo;

	//Variablen zum Auslesen der messdaten
	private double Datenoffset;
	private short [][]  Messinformation;
	private ByteBuffer buffer;
	// Rohdaten_matrix 
	private double[][][]  Rohdaten_Matrix;


	////Konstruktor mit drei Kanalnummern (pZyl, pEin, pAus)
	public IndizierFileReader_adv (CasePara cp,String fileName,
			int kanal_pZyl,
			int kanal_pEin,
			int kanal_pAus){

		super(cp);
		spalte_pZyl = kanal_pZyl;
		spalte_pEin = kanal_pEin;
		spalte_pAbg = kanal_pAus;		

		if( !fileName.endsWith(ext))
			throw new IllegalArgumentException(".adv-Datei: kein gültiges Dateiformat");

		file = new File(fileName);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException("Der für das " +
			"Indizierfile angegebene Pfad zeigt nicht auf eine Datei!");
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();				
			}
			datei_Einlesen();
	}

	///Konstruktor mit einer Kanalnummer (nur pZyl)
	public IndizierFileReader_adv(CasePara cp,String fileName,int kanal_pZyl){
		super(cp);
		dreiDruecke=false;
		spalte_pZyl = kanal_pZyl;
		spalte_pEin = spalte_pZyl;
		spalte_pAbg = spalte_pZyl;		

		if( !fileName.endsWith(ext))
			throw new IllegalArgumentException(".adv-Datei: kein gültiges Dateiformat");

		file = new File(fileName);
		if (!file.isFile())
			try{ throw new ParameterFileWrongInputException("Der für das " +
			"Indizierfile angegebene Pfad zeigt nicht auf eine Datei!");
			}catch(ParameterFileWrongInputException e){
				e.stopBremo();				
			}

			datei_Einlesen();
	}

	//Einleseroutine 
	protected void datei_Einlesen(){
/*		try{
			throw new BirdBrainedProgrammerException("This class seems not " +
					"to work properly. Please contact your local programmer!");			
		}catch(BirdBrainedProgrammerException bbp){
			bbp.stopBremo();			
		}
*/		
		try{
			FileInputStream fis = new FileInputStream(file);
			int ln = (int)file.length();
			buf = new byte[ln];
			fis.read(buf);
		}
		catch(FileNotFoundException c){c.printStackTrace();}
		catch(IOException e){e.printStackTrace();}

		buffer = ByteBuffer.wrap(buf); 	

		A = buffer.getInt(12);
		B = buffer.getInt(A);
		L1 = buffer.getInt(B);
		C=B+4+L1*2;
		L2 = buffer.getInt(C);
		D = C+4+L2*4;
		L3 = buffer.getInt(D);
		E = D+4+L3*4;
		buffer.position(E);
		L4 = buffer.getInt();
		Setup = new double[L4];

		for (int i =0; i<L4; i++){
			Setup[i] =  buffer.getDouble();
		}
		//Setup Daten auslesen
		Dateiarray = Setup[0];                   //Länge des Dateiarrays
		Kanalanzahl = (Setup[1])*8;                  //Anzahl der Kanäle
		//BITS = Setup[3];                         //Kartenspeichergrösse
		BITS = 14;
		OT_Verschiebung = Setup[4];              //Auslesen der OT-Verschiebung
		Clocks = Setup[81];                      //Anzahl Clocks pro Zyklus
		Zyklen = Setup[82];                      //Anzahl der Zyklen
		Motor_r=Setup[51]*1E-3;                  //Kurbelradius (m)
		hub=Setup[52]*1E-3;                //Hub (m)
		bohrung=Setup[53]*1E-3;            //Bohrung (m)
		verdichtung=Setup[54];             //Verdichtungsverhältnis (-)
		pleuel_L=Setup[67]*1E-3;           //Pleuellänge (m)
		Drehzahl = Setup[1311];     // (iLastpunkt)
		pulseProAS=Clocks;
		aZyk=Zyklen;

		// Initialisierung der variablen

		Verstaerkung = new double[(int)Kanalanzahl];
		Drift = new double[(int)Kanalanzahl];
		OT = new double[(int)Kanalanzahl];
		Messbereich =  new double [(int)Kanalanzahl]; 

		for ( int i = 0; i < Kanalanzahl; i++)
		{ 
			OT[i]=   Setup[914+(((i+1)*20)-20)];                 //OT-Vesatz für jeden Zylinder auslesen
			Messbereich[i] = Setup[912+(((i+1)*20)-20)];        //Messbereich für jeden Zylinder auslesen
			Verstaerkung[i] = Setup[112+(((i+1)*40)-40)];       //Verstärkungsfaktor für jeden Zylinder auslese
			Drift[i] = Setup[116+(((i+1)*40)-40)];              //Art der Driftkompensation auslesen

		}
		// Driftkompensation berechnen 
		Polytropenexponent =  new double[(int)Kanalanzahl];
		Referenzzeitpunkt =new double[(int)Kanalanzahl];
		Referenzzeitpunkt = new double[(int)Kanalanzahl];
		Bereich = new double[(int)Kanalanzahl];

		for ( int i = 0; i < Kanalanzahl; i++){
			switch ((int)Drift[i]){
			case 0:
				Polytropenexponent[i] = 0;
				Referenzzeitpunkt[i] = 0;
				Referenzzeitpunkt[i] = 0;
				Bereich[i] = 0;
				break;
			case 1:
				Referenzzeitpunkt[i] = Setup[118+(((i+1)*40)-40)];
				Bereich[i] = Setup[119+(((i+1)*40)-40)];
				break;
			case 2:
				Polytropenexponent[i] = Setup[119+(((i+1)*40)-40)];
				Referenzzeitpunkt[i] = Setup[120+(((i+1)*40)-40)];
				Referenzzeitpunkt[i] = Setup[121+(((i+1)*40)-40)];
				Bereich[i] = Setup[122+(((i+1)*40)-40)];
				break;
			case 3:
				Referenzwert[i] = Setup[122+(((i+1)*40)-40)]; break;
			}
		}

		// Zuordnung Messbereich
		MR_MIN = new double[(int)Kanalanzahl];
		MR_MAX = new double[(int)Kanalanzahl];

		for (int i = 0; i< Kanalanzahl; i++){
			switch((int)Messbereich[i]){

			case 0 : MR_MIN[i]=-1.0;MR_MAX[i]=1.0; break;
			case 1: MR_MIN[i]=-2.5;MR_MAX[i]=2.5 ;break;
			case 2:MR_MIN[i]=-5.0;MR_MAX[i]=5.0  ;break;
			case 3:MR_MIN[i]=-10.0;MR_MAX[i]=10.0;break;
			case 4:MR_MIN[i]=0.0;MR_MAX[i]=1.0;  break;
			case 5:MR_MIN[i]=0.0;MR_MAX[i]=2.5  ;break;
			case 6:MR_MIN[i]=0.0;MR_MAX[i]=5    ;break;
			case 7:MR_MIN[i]=0.0;MR_MAX[i]=10   ;break;
			case 8:MR_MIN[i]=-1.0;MR_MAX[i]=0.0 ;break;
			case 9:MR_MIN[i]=-2.5;MR_MAX[i]=0.0 ;break;
			case 10:MR_MIN[i]=-5.0;MR_MAX[i]=0.0;break;
			case 11:MR_MIN[i]=-10.0;MR_MAX[i]=0.0;break;
			}
		}
		ACC = (Math.pow(2,(BITS))) - 1;	 

		ZLF =  new double [(int)Kanalanzahl];
		OZ = new double [(int)Kanalanzahl];
		M = new double [(int)Kanalanzahl];

		for (int i = 0; i< Kanalanzahl; i++){

			Messbereich[i] = Math.abs(MR_MAX[i]-MR_MIN[i]);                                                                                                                                                                                                                               
			ZLF[i] = (Messbereich[i]- Math.abs(MR_MAX[i]))/Messbereich[i];
			OZ[i] = ACC * ZLF[i];
			M[i] = Messbereich[i] / ACC; 
		} // end for(int i =0; i<kanalanzahl...)

			//Messinformationsmatrix bewerten
			Messdaten = B+5+Dateiarray*2;
		Messinfo = Messdaten+3;              
		Messinformation = new short[2][(int)Zyklen+1];

		for(int i =0;i<2;i++){
			buffer.clear(); 
			buffer.position((int)Messinfo); // initially this was (long) but method doesn't support long values/////
			for (int j=0; j<Zyklen+1; j++){
				Messinformation[i][j]=buffer.getShort();
			}
			Messinfo = Messinfo+(Zyklen+1)*2;
		}


		//Rohrdaten_matrix bewerten        

		Rohdaten_Matrix = new double[(int)Kanalanzahl][(int)(Zyklen)][(int)Clocks];
		Datenoffset = B+6+(Messinformation[1][0]-3600)*2;



		buffer.order(ByteOrder.LITTLE_ENDIAN);
		for( int i = 0; i < Kanalanzahl; i++ ){	    			 
			buffer.clear(); 
			buffer.position((int) Datenoffset);
			for (int j = 0; j < Zyklen; j++ ){	    
				for (int k = 0; k < Clocks; k++ ){
					Rohdaten_Matrix[i][j][k] = (((buffer.getShort())-OZ[i])*M[i])*Verstaerkung[i];		 
				}
			}
			Datenoffset = Datenoffset+Messinformation[1][0]*2+Clocks*2*Zyklen;
		}  	     	   
		//TODO Mittelung der einzelnen zyklen einfuegen	 	 
		// 	super.pZyl = new double[(int)Kanalanzahl][(int)(Clocks*Zyklen)];
		// 	super.pEin = new double[(int)Zyklen][(int)Clocks];
		// 	super.pAbg = new double[(int)Zyklen][(int)Clocks];

		//     for(int j=0;j<Zyklen;j++){
		//    	 for(int k=0;k<Clocks;k++){
		//    		 super.pZyl[j][k] = Rohdaten_Matrix[spalte_pZyl-1][j][k]; 
		//    		 super.pEin[j][k]= Rohdaten_Matrix[spalte_pEin-1][j][k];
		//    		 super.pAbg[j][k] = Rohdaten_Matrix[spalte_pAbg-1][j][k];
		//    	 }
		//     }   

	} // end public double[] set_variables()




	{



		double  t1 = System.currentTimeMillis();


		double t2 = System.currentTimeMillis();
		System.out.println("Init:" + (t2-t1) + " ms");
	}
}
