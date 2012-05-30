package BremoLogFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.io.*;
import java.io.ObjectInputStream.GetField;

import javax.swing.text.DateFormatter;
/**
 * @author Steve Ngueneko
 *
 */
public class LogFileWriter {
	// zwiechenspeicher zu prüfen ob eine meldung schon gelistet wuerde
	 static List<String> InfoListe = new LinkedList<String>() ;
	 static Date date ; 
	 static File file ; 
     static FileWriter fw ;
     static boolean fileIsCreate = false;
	 
	
     /**
	 * Die Methode schreibt jede Meldung in der Log File
	 * @param Message
	 */
	private static   void LogWriter_txt (String Message){
		date = new Date();
		file = new File ("src/OutputFiles/LogFile.txt"); 
		try {
			fw = new FileWriter (file,fileIsCreate);
			if (!fileIsCreate) {
				fw.write("+++++++++++++++++++++++++++++++++++++++++++++\n");
				fw.write("+    " + date + "          +\n");
				fw.write("+++++++++++++++++++++++++++++++++++++++++++++\n");
			}
			fw.write(System.getProperty("line.separator"));
			fw.write(Message);
			fw.write(System.getProperty("line.separator"));
			fw.close();
			fileIsCreate = true ;
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * die Methode fuege Die meldung in den zwieschenspeicher, schreibt
	 * und ruft LogWriter_txt() wenn die Meldung neu ist .
	 * @param Message
	 */
	public static  void addItemToLog (String Message) {
		
		if ( InfoListe.isEmpty()){
			 InfoListe.add(Message);
		}
		else if (!InfoListe.contains(Message)) {
			InfoListe.add(Message);
			LogWriter_txt("Item"+ InfoListe.indexOf(Message) +" : "+Message);
		}
		
	}
	
	public static void reinitialisierung() {
		InfoListe.clear();
		fileIsCreate = false ;
	}

}
