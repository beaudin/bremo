package misc;

import java.util.ArrayList;

import bremo.parameter.CasePara;

/**
 * Mit dieser Klasse können Werte in Vektoren gespeichert werden und
 * wieder aufgerufen werden. Das Wandwärmemodell von Hans benötigt
 * diese Funktionalität
 * @author steve
 *
 */

public class VectorBuffer {
	
		private ArrayList<Double> timeVekt;
		private ArrayList<Double> werteVekt;
		private LinInterp L_Interp;
		private double maxTime=Double.NEGATIVE_INFINITY;
		
		public VectorBuffer(CasePara CP){
			timeVekt = new ArrayList<Double>(2000);
			werteVekt = new ArrayList<Double>(2000);
			L_Interp = new LinInterp(CP);
		}
		
		/**
		 * Ein Zeitstempel und ein Wert werden den entsprechenden ArrayLists hinzugefügt
		 * @param timeToAdd
		 * @param valueToAdd
		 */
		public void addValue(double timeToAdd, double valueToAdd){
			if(timeToAdd>maxTime)
				maxTime=timeToAdd;
			if(timeVekt.contains(timeToAdd)){
				int index=timeVekt.indexOf(timeToAdd);
				werteVekt.set(index, valueToAdd);
			}else{
				timeVekt.add(timeToAdd);
				werteVekt.add(valueToAdd);
			}
		

		}
		/**
		 * 
		 * @param time
		 * @return den Wert, der bei der Zeit "time" zu finden ist
		 */
		public double getValue(double time){	
			if(time>this.maxTime)
				System.err.println("Hier sollte eine Exception kommen");
			
			Double[] t = new Double[timeVekt.size()];
			t=timeVekt.toArray(t);
			Double [] w = new Double[werteVekt.size()];
			w=werteVekt.toArray(w);
			double a=L_Interp.linInterPol(time, t, w);
			return a;

		}
		
		public double [] getValues(){	
			Double [] w = new Double[werteVekt.size()];
			double d[] =new double[werteVekt.size()];
			w=werteVekt.toArray(w);
			for(int i=0;i<w.length;i++) d[i]=w[i].doubleValue();
			return d;
		}
		
		public double [] getZeitachse(){	
			Double [] w = new Double[timeVekt.size()];
			double d[] =new double[timeVekt.size()];
			w=timeVekt.toArray(w);
			for(int i=0;i<w.length;i++) d[i]=w[i].doubleValue();
			return d;
		}
		
		
		
		
		/**
		 * Liefert die Zeit bei der zuletzt ein Wert eingegeben wurde
		 * @return
		 */
		public double get_maxTime(){
			return maxTime;
		}
		
		
		
		
		
	}	
	


