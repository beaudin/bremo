package berechnungsModule;

import io.FileWriter_txt;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import matLib.MatLibBase;
import misc.LinInterp;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class ErgebnisBuffer {


	private Hashtable<String, Vector<Double>> ergebnisHash=new Hashtable<String, Vector<Double>>();
	private Hashtable<String, Integer> ergebnisSpalteHash=new Hashtable<String, Integer>();
	private String praefix;
	
	private CasePara cp;


	public ErgebnisBuffer(CasePara cp,String praefix){	
		this.cp=cp;
		this.praefix=praefix;
		cp.ergBufferCheckIN(this);	
	}	


	public void buffer_EinzelErgebnis(String nameIN, double valueIN, int spalteIN){		
		try{
			ergebnisHash.get(nameIN).add(valueIN);
			ergebnisSpalteHash.put(nameIN, spalteIN);
		}catch(NullPointerException e){
			ergebnisHash.put(nameIN,new Vector<Double>()); //Wenn es den namen noch nicht gibt wird ein Vector angelegt
			ergebnisHash.get(nameIN).add(valueIN);
			ergebnisSpalteHash.put(nameIN, spalteIN);
		}		
	}

	/**
	 * Methode welche die ErgebnisBuffer loescht. Dies wird fuer die LWA benoetigt, 
	 * da heir mehrere Rechnungsdurchlaeufe mit ein und dem selben Objekt vom durchgefuehrt werden.
	 */
	public void clearErgebnisBuffer(){
		ergebnisHash=new Hashtable<String, Vector<Double>>();
		ergebnisSpalteHash=new Hashtable<String, Integer>();		
	}

	private double []vector2double(Vector<Double> vecIn){
		double[] vecOut=new double[vecIn.size()];

		for(int i=0;i<vecIn.size();i++){
			vecOut[i]=vecIn.get(i);
		}		
		return vecOut;
	}


	private String [] headerFromHash( Hashtable<String, Vector<Double>> hash, Hashtable<String, Integer> ergebnisSpalteHashIN ){
		if(hash.size()!=ergebnisSpalteHashIN.size()){
			try{//Umwandeln der Exception, um eine entsprechende Nachricht an den BirdBrainedProgrammer auszugeben!
				throw new BirdBrainedProgrammerException("Die Hashtables fuer die Ergebnisgroessen und fuer die entsprechenden Eintraege im " +
						"Header haben " +
				"nicht die selbe Anzahl an Eintraegen");
			}catch (BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}			
		}			
		String header []= new String[hash.size()];
		Enumeration<String> e = hash.keys();
		for(int i=0; i<hash.size(); i++){
			String name=(String)e.nextElement();
			int spalte=ergebnisSpalteHashIN.get(name);
			try{
				header[spalte]=name;
			}catch (ArrayIndexOutOfBoundsException aoobe){
				try{//Umwandeln der Exception, um eine entsprechende Nachricht an den BirdBrainedProgrammer auszugeben!
					throw new BirdBrainedProgrammerException("In der Methode \"buffer_EinzelErgebnis\" wurde eine Vektorposition " +
							"die nicht vorhanden ist mit einem Wert belegt \n z.B. dQ, dmb,... --> 0,2.. Die 2 gibt nicht die " +
							"richtige Position von dmb im Vektor an und es kommt zur ArrayOutOfBoundsException. \n " +
							"Vielleicht wurde auch zweimal der selbe " +
							"Name fuer eine Ergebnisvariable gewaehlt. \n Das fuehrt dazu, " +
							"dass im Ergebnisvektor mehr Verte stehen als Ergebnisnamen vorhanden sind --> ArrayOutOfBoundsException \n"
							+"Es muessen die Variablen \"ergebnisSpalteHash\" und \"ergebnisHash\" kontrolliert werden. ");
				}catch (BirdBrainedProgrammerException bbpe){
					bbpe.stopBremo();
				}
			}
		}

		return header;
	}		



	public  void schreibeErgebnisFile(String name){
		String pfadFinal=cp.get_workingDirectory()+this.praefix+name;
		String Separator  ="\n**************************************************\n";
//		System.err.println(Separator);
		System.err.println(Separator+"Schreibe "+praefix+"Datei:\n"+pfadFinal+Separator);
//		System.err.println(pfadFinal);
//		System.err.println(Separator);
		if(!ergebnisHash.isEmpty()&& !ergebnisSpalteHash.isEmpty()) //Verhindert einen Fehler wenn die Ergebnisse schon geschrieben wurden (siehe LWA)
			this.schreibeErgebnisFile(this.ergebnisHash, this.ergebnisSpalteHash, pfadFinal);
	}
	

	public  void schreibeErgebnisFile(Hashtable<String, Vector<Double>> ergebnisHashIN, 
			Hashtable<String, Integer> ergebnisSpalteHashIN, String pfad){

		FileWriter_txt txtW=new FileWriter_txt(pfad);
		String [] header=headerFromHash(ergebnisHashIN,ergebnisSpalteHashIN);		
		double [][]values =new double[ergebnisHashIN.size()][];

		Enumeration<String> e = ergebnisHashIN.keys();
		for(int i=0; i<ergebnisHashIN.size(); i++){
			String name=(String)e.nextElement();
			int spalte=ergebnisSpalteHashIN.get(name);
			values[spalte]=vector2double(ergebnisHashIN.get(name));
		}		

		txtW.writeTextLineToFile(header, false);
		txtW.writeMatrixToFile(MatLibBase.transp_2d_array(values), true);
	}
	
	/**
	 * Gibt den gebufferten Wert zu einem bestimmten Zeitpunkt zurück. Gibt es keinen Wert, wird null zurückgegeben.
	 * @param time [sec n. Rechenbeginn]
	 * @param name (wie er in der Ausgabedatei steht)
	 * @return value
	 */
	@SuppressWarnings("null")
	public double get_bufferedErgebnis(double time, String name){
		double value = 0;
		try{
			double[] zeit = vector2double(this.ergebnisHash.get("Zeit [s n. Rechenbeginn]"));
			double[] values = vector2double(this.ergebnisHash.get(name));
			LinInterp linInterp = new LinInterp(this.cp);
			value = linInterp.linInterPol(time, zeit, values);
			return value;
		}catch(Exception e){
			return (Double) null;
		}
		
	}
}
