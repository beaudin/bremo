package io;
import java.io.*;

public class FileWriter_txt {
	
	File outputFile;
	String filename;
	PrintWriter pw;
	
	public FileWriter_txt(String filename, String [] header){
		this.filename=filename;
		writeTextLineToFile(header,false);
	}
	
	public FileWriter_txt(String filename){
		this.filename=filename;		
		
	}	
	
	

	
	
	
	
	public void writeLineToFile( double[] dataToWrite, boolean appendToFile) {

		PrintWriter pw = null;
		
		try {
		
			if (appendToFile) {		
			//If the file already exists, start writing at the end of it.
				pw = new PrintWriter(new FileWriter(filename, true));		
			}
			else {
			
				pw = new PrintWriter(new FileWriter(filename));
				//this is equal to:
				//pw = new PrintWriter(new FileWriter(filename, false));		
				}
			
			for (int i = 0; i < dataToWrite.length-1; i++) {		
				pw.print(dataToWrite[i]+"\t ");		
			}
			pw.println(dataToWrite[dataToWrite.length-1]);	
			pw.flush();
			
			}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {		
			//Close the PrintWriter
			if (pw != null)
				pw.close();		
		}

	}

	public void writeMatrixToFile( double[][] dataToWrite, boolean appendToFile) {
		PrintWriter pw = null;

		try {

			if (appendToFile) {		
				//If the file already exists, start writing at the end of it.
				pw = new PrintWriter(new FileWriter(filename, true));		
			}
			else {

				pw = new PrintWriter(new FileWriter(filename));
				//this is equal to:
				//pw = new PrintWriter(new FileWriter(filename, false));		
			}
			
			for (int i = 0; i < dataToWrite.length; i++) {	
				int a=dataToWrite[i].length;
				for (int x = 0; x < a; x++) {		
					pw.print(dataToWrite[i][x]+"\t ");		
				}
				pw.print("\n");	
				pw.flush();
			}

		}catch (IOException e) {
			e.printStackTrace();
		}finally {		
			//Close the PrintWriter
			if (pw != null)
				pw.close();		
		}	

	}

	
	
	
	
	
	public void writeTextLineToFile( String [] headerToWrite, boolean appendToFile) {

		PrintWriter pw = null;
		
		try {
		
			if (appendToFile) {		
			//If the file already exists, start writing at the end of it.
				pw = new PrintWriter(new FileWriter(filename, true));		
			}
			else {
			
				pw = new PrintWriter(new FileWriter(filename));
				//this is equal to:
				//pw = new PrintWriter(new FileWriter(filename, false));		
				}
			
			
			for (int i = 0; i < headerToWrite.length-1; i++) {	
				
				pw.print(headerToWrite[i]+"\t");	
			}		
			pw.println(headerToWrite[headerToWrite.length-1]);	
			pw.flush();
			
			}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {		
			//Close the PrintWriter
			if (pw != null)
			pw.close();		
		}
	}


	public void writeTestLinesToFile() {
		
		
		String filename= "TESTfile.txt";
		String[][] linesToWrite=new  String[2][2];
		
		
		String []line1={"Line 1 C1","L1 C2"};
		String []line2={"Line 2 C1","L2 C2"};
		linesToWrite[0]=line1;
		linesToWrite[1]=line2;
		
		
		
		boolean appendToFile=true;

		PrintWriter pw = null;
		
		try {
		
		if (appendToFile) {		
		//If the file already exists, start writing at the end of it.
			pw = new PrintWriter(new FileWriter(filename, true));		
		}
		else {		
			pw = new PrintWriter(new FileWriter(filename));
			//this is equal to:
			//pw = new PrintWriter(new FileWriter(filename, false));		
		}
		
		for (int i = 0; i < linesToWrite.length; i++) {	
			for (int j = 0; j < linesToWrite[i].length-1; j++) {
				pw.print(linesToWrite[i][j]+"\t ");	
			}
			pw.println(linesToWrite[i][linesToWrite[i].length-1]);
			System.out.println(i);
		}
		
		//pw.println(linesToWrite);
		
		pw.flush();
		
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {		
			//Close the PrintWriter
			if (pw != null)
			pw.close();		
		}
	
	}


	public void testAusgabe(){	
		
		double vektor []={1,2,3,4,5};
		double matrixLine1 []={11,12,13,14,15};
		double matrixLine2 []={21,22,23,24,25};
		double matrix [][] ={matrixLine1,matrixLine2};
		String [] header = {"Spalte 1", "Spalte 2"};		
		this.writeTextLineToFile(header, false);
		this.writeLineToFile(vektor, true);
		this.writeMatrixToFile(matrix, true);
		
	}


}
