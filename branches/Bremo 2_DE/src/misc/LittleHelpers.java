package misc;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import kalorik.spezies.Spezies;

//fuer pmi-Berechnung/Verlustteilung Frank Haertel
import berechnungsModule.motor.Motor; 
import berechnungsModule.motor.Motor_HubKolbenMotor; 
import bremo.parameter.CasePara; 

public class LittleHelpers {
	
	/**
	 * Beim Aufruf dieser Funktion wird ein Objekt kopiert, so dass das neue Objekt dem alten entspricht aber 
	 * vollkommen unabhängig davon ist 
	 * @param Object --> Objejkt das kopiert werden soll
	 */
	  public static Object deepCopy(final Object source) throws IOException,ClassNotFoundException {
		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		    
		    objectOutputStream.writeObject(source);
		    objectOutputStream.close();
		    
		    byte[] buffer = byteArrayOutputStream.toByteArray();
		    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
		    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		    return (Object) objectInputStream.readObject();
		  }
	  
	  
	  public static void print_Hash(Hashtable<String, String[]> ht){
		 Enumeration<String> e =ht.keys();
		 while(e.hasMoreElements()){
			String s= e.nextElement();
			 System.out.println(s);
		 }
		  
	  }
	  
	
	  /**
	   * Multipliziert die Werte in ht2 mit m und addiert diese zu ht1
	   * @param ht1
	   * @param ht2
	   * @param m
	   * @return
	   */
	  public static Hashtable<Spezies,Double> addiereHashs(	Hashtable<Spezies,Double> ht1 , 
			  												Hashtable<Spezies,Double>  ht2, 
			  												double m){

		  Hashtable<Spezies,Double> mi_neu=new Hashtable<Spezies, Double>();	 
		  Enumeration<Spezies> e_ht1=ht1.keys();
		  Spezies spez1;
		  //Hinzufuegen der Werte die ggf. schon in der Hashtable vorhanden sind
		  while(e_ht1.hasMoreElements()){
			  spez1=e_ht1.nextElement();
			  if(ht2.containsKey(spez1)){
				  double m1=ht1.get(spez1)+ht2.get(spez1)*m;
				  mi_neu.put(spez1, m1);
			  }else{
				  mi_neu.put(spez1, ht1.get(spez1));
			  }   					
		  }
		  Spezies spez2;
		  //Hinzufuegen der Werte die noch nicht in der Hashtable waren
		  Enumeration<Spezies> e_ht2=ht2.keys();
		  while(e_ht2.hasMoreElements()){
			  spez2=e_ht2.nextElement();
			  if(mi_neu.containsKey(spez2)){				 
			  } else {//d.h. er wurde bei der ersten Iteration nicht gefunden   
				  mi_neu.put(spez2, ht2.get(spez2)*m);
			  }
		  }
		  
		//Überprüfen ob spezies mit dem selben namen aber mit unterschiedlichen Hashcodes vorhanden sind
		//Vergleich des molenbruchDetailHash mit sich selbst!		  
		  Enumeration<Spezies> e3,e4;
			e3=mi_neu.keys();
			e4=mi_neu.keys();
			Spezies spez3, spez4;
			while(e3.hasMoreElements()){
				spez3=e3.nextElement();			
				while(e4.hasMoreElements()){
					spez4=e4.nextElement();
					//verhindert den Vergleich einer Spezies mit sich selber
					if(spez3!=spez4){
						if(spez3.get_name().equals(spez4.get_name())){
							double temp=mi_neu.get(spez3)+mi_neu.get(spez4);
							mi_neu.put(spez3, temp);
							mi_neu.remove(spez4);
						}						
					}					
				}			
			}	  
		  
		  return mi_neu; 		  
	  } 	
	  
	  public static double [] concat(double[] first, double[] second) {
		  double[] result = Arrays.copyOf(first, first.length + second.length);
		  System.arraycopy(second, 0, result, first.length, second.length);
		  return result;
		}
	  
	  //fuer Verlustteilung von Frank Haertel
	  public static double berechnePmi (CasePara CP, double [] pZyl ){ 
	        Motor motor=CP.MOTOR; 
	      //Schleife über Wert 0 bis n-2 
	      int pktProAS = pZyl.length; 
	      double kw=0.0; 
	      double pmi=0; 
	      double schrittweiteSEC = CP.SYS.WRITE_INTERVAL_SEC; 
	      double schrittweiteKW = CP.convert_ProKW_2_ProSEC(schrittweiteSEC); 
	      for(int i=0; i < pktProAS-1; i++){ 
	        kw = CP.SYS.RECHNUNGS_BEGINN_DVA_KW+i*schrittweiteKW; 
	     
	  pmi+=0.5*(pZyl[i]+pZyl[i+1])*(motor.get_V(CP.convert_KW2SEC(kw+schrittweiteKW)) 
	            -motor.get_V(CP.convert_KW2SEC(kw))); 
	      } 
	      double pmiAus=pmi/((Motor_HubKolbenMotor) motor).get_Hubvolumen();
	  // Wert wird in [Pa] ausgegeben 
	      return pmiAus; 
	      } 

}
