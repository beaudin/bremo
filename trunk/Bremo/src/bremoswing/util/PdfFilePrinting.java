package bremoswing.util;

import java.io.*;
import javax.print.*;
import javax.print.attribute.*;

public class PdfFilePrinting {
	public static  void callExcecutablePrint(File file){
	     try{
	             Executable ex = new Executable();
	             //ex.openDocument(file);
	             ex.printDocument(file);
	     }catch(IOException e){
	             e.printStackTrace();
	     }
	}
}
