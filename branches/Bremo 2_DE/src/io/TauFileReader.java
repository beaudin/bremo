package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import misc.LinInterp;

import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public class TauFileReader {
	private static String ext = "txt";
	private String [] zeitEinheiten=new String [2];
	private String zeitEinheit, einheitTau; 
	private double tau_kalib;//Faktor fuer die Umrechnung von ms nach s
	private File file;
	private CasePara cp;
	private LinInterp L_Interp;
	private int punkteProArbeitsspiel;
	private double[] zeitAchse;
	private double[] tau;
	private boolean convertKW2SEC=false;
	private double offsetKW;
	private double offsetSEC;
	public boolean fileExists;
	public double tauInput;
	int spalte;

	
	public TauFileReader(CasePara CP, int index){		
		
		this.cp=CP;
		L_Interp = new LinInterp(CP);
		fileExists = CP.fileExist("TauFileName_"+index);
		if(fileExists){
			File RATE__FILE=CP.get_FileToRead("TauFileName_"+index);		
			int indexOf=RATE__FILE.getName().indexOf(".");
			String RATE_FORMAT=RATE__FILE.getName().substring(indexOf+1); //Dateiendung;
			this.spalte = cp.get_ColumnToRead("spalte_Tau"+index);
			
			zeitEinheiten[0]="[KWnBOI]";
			zeitEinheiten[1]="[snBOI]";
			offsetKW=cp.convert_SEC2KW(cp.get_BOI(index));
			offsetSEC=cp.get_BOI(index);
			if( !RATE_FORMAT.equalsIgnoreCase(ext))
				 throw new IllegalArgumentException("TXT_datei: kein gueltiges Dateiformat");
			file = RATE__FILE;
			if (!file.isFile())
				try{ throw new ParameterFileWrongInputException("Der fuer den " +
						"Tauverlauf angegebene Pfad zeigt nicht auf eine Datei! \n"
						+ RATE__FILE.getAbsolutePath());
				}catch(ParameterFileWrongInputException e){
					e.stopBremo();				
				}
			 
			datei_Einlesen();	
		}
		else{
			tauInput=this.cp.get_tau(index);
		}			
	 }

	//Daten Einlesen
	public void datei_Einlesen(){
		//Erste Datenzeile wird automatisch gesucht...
		String line = "";

		int idx=-1;
		int abZeile=1;
		boolean kalibrierungErfolgreich=false;
		
		try {
			//finde die erste Zeile, die als Zahlen eingelesen werden kann
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			boolean zeilenZaehlen = false;
			int cnt = 0;
			while((line = br.readLine()) != null ) {//&& stop != true
				//Leerzeichen entfernen
				line=line.replaceAll(" ", "");
				line = line.replace(',','.');	
				try {
					idx++;
					String[] theline = line.split("\t");
					if(theline[0].startsWith("[")){
						zeitEinheit=theline[0];						
						if(zeitEinheit.contains("KW"));
							convertKW2SEC=true;
						einheitTau=theline[1];
						
						if(zeitEinheit.equals(zeitEinheiten[0])||zeitEinheit.equals(zeitEinheiten[1])){}
						else{
							throw new ParameterFileWrongInputException("Die Einheit in der ersten Spalte " +
									"des Tauverlauffiles muss "+ zeitEinheiten[0]+ 
									" oder " +zeitEinheiten[1] +" sein. \n Eingegeben wurde aber "+ zeitEinheit +
									"\n Es bedeuten: " +
									"\n KWnBOI: KW nach Einspritzbegin (Begin of Injection) " +
									"\n snBOI: sec nach Einspritzbegin (Begin of Injection) ");}							
						
						tau_kalib=set_kalibrierfaktor(einheitTau);
						kalibrierungErfolgreich=true;
					}
					Double.parseDouble(theline[0]);//wirft eine Exception wenn theline[0] keine zahl ist
					if(!kalibrierungErfolgreich)
						throw new ParameterFileWrongInputException("Im angegebenen Taufile wurden keine "
								+ "Einheiten angegeben. \n" +"Diese muessen vor den eigentlichen Raten in "
										+ "eckigen Klammern angegeben werden z.B.: \n" +
								"[KWnBOI] [KW] oder [snBOI] [s] oder ...\n" +
								"Eine weiter Fehlermoeglichkeit: Die Spaltenangaben stimmen nicht!!");
					
					//Wenn numerische Daten in den Zeilen stehen sollen diese gezaehlt werden
					zeilenZaehlen=true; 
				}catch (ParameterFileWrongInputException pfwi){
					pfwi.stopBremo();
				}catch(Exception e) {
					abZeile++;
				}	
				if(zeilenZaehlen==true){
					cnt++; 
				}
			}
			br.close();
			fr.close();		
			punkteProArbeitsspiel = cnt;
		}

		catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		    //File wird eingelesen
		   double[][] data = readFile(abZeile);
		   zeitAchse=data[0];			
		   tau = misc.LittleHelpers.concat(data[1],data[1]);
	}	
	
	private double set_kalibrierfaktor(String einheit) 
			throws ParameterFileWrongInputException{
		if(einheit.equals("[s]")){
			return 1.0;		
		}else if(einheit.equals("[ms]")){
			return 1000.0;
		}else{
			if(einheit.equals("")){
			throw new ParameterFileWrongInputException("Die Einheiten fuer den Tauverlauf im "
					+ "Tautextfile " +"wurden nicht angegeben. \n Dies kann z.B. aus der Angabe "
							+ "einer flaschen Spaltennummer resultieren. \n" + 
					"Sollte die Spaltennummer richtig sein, muessen die Einheiten [s] " +
					"oder [ms] vor den Kraftstoffdaten angegeben werden."); 
			}else{
				throw new ParameterFileWrongInputException("Die Einheiten fuer den Tauverlauf im "
						+ "Tautextfile"+" muessen in [s] oder [ms] erfolgen. \n " +
						"Die Eingabe erfolgte aber in: " +einheit); 
			}
		}	
	}	
	
	
	private double[][] readFile(int abZeile) {
		//Datei wird ab Zeile abZeile gelesen. 1 waere die erste Zeile...		
		if(abZeile<1){
			abZeile=1;
		}
		int anzSpalten=2;		
			
		double[][] data = new double[anzSpalten][punkteProArbeitsspiel+1];
		int idx=1;
		int cnt=0;
		double t;		
		String line = "";
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) {

				if(idx>=abZeile){
					line=line.replaceAll(" ", "");
					line = line.replace(',','.');
					String[] theline = line.split("\t");
					if(convertKW2SEC){						
						t=cp.convert_KW2SEC(Double.parseDouble(theline[0])+offsetKW);	
					}else{
						t=Double.parseDouble(theline[0])+offsetSEC;
					}
					data[0][cnt]=t; // in s	nach Rechenbeginn				
					data[1][cnt]=Double.parseDouble(theline[1])*tau_kalib; // in s
					cnt++;
				}
				idx++;
			}
			br.close();
			fr.close();
			
		}catch(NumberFormatException nfe){
			try {
				throw new ParameterFileWrongInputException("In einer der angegebenen Spalten " +
						"des Taufiles steht zwar eine richtige Einheit im Header, " +
						"aber Daten sind darunter keine eingetragen... ");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
			
		}catch(FileNotFoundException fN) {
			fN.printStackTrace();
		}
		catch(IOException e) {
			System.out.println(e);
		}
		if(data[0][data[0].length-1]<cp.SYS.RECHNUNGS_ENDE_DVA_SEC){
			try {
				throw new BirdBrainedProgrammerException("Der Tau-Verlauf wurde nicht bis "
						+ "Rechenende angegeben. Der letzte angegebene Wert wird beibehalten !");
			} catch (BirdBrainedProgrammerException e) {
				e.log_Message();
			}
			data[0][data[0].length-1] = cp.SYS.RECHNUNGS_ENDE_DVA_SEC;
			data[1][data[1].length-1] = data[1][data[1].length-2];
		}
		else{
			data[0][data[0].length-1] = data[0][data[0].length-2];
			data[1][data[1].length-1] = data[1][data[1].length-2];
		}
		return data;
	}

	public double get_Tau(double time) {
		if(this.fileExists){
			double letzterWert=0, ersterWert=0;
			double dauerASP_SEC=cp.SYS.DAUER_ASP_SEC;
			letzterWert=zeitAchse[zeitAchse.length-1];
			ersterWert=zeitAchse[0];
			
			//Schleifenabbruch nicht direkt sondern ueber rechnungsSchrittweite
			//if(time>=(ersterWert+dauerASP_SEC) && time<=(letzterWert+dauerASP_SEC)){
			
			if(time-(ersterWert+dauerASP_SEC)>=cp.SYS.WRITE_INTERVAL_SEC 
					&& (letzterWert+dauerASP_SEC)-time>=cp.SYS.WRITE_INTERVAL_SEC){
				return L_Interp.linInterPol(time-dauerASP_SEC, zeitAchse, tau);
			}else if(time>=ersterWert && time<=letzterWert){
				return L_Interp.linInterPol(time, zeitAchse, tau); 
			}else{
				return this.tau[0];
			}
		}
		else{
			return this.tauInput;
		}
	}
}
