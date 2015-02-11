package berechnungsModule.motor;

import io.EngineFileReader;
import bremo.parameter.CasePara;

public class Motor_VolumenFunktionAusDatei extends Motor{
	EngineFileReader mvfr;
	double vMax = -5.55;
	double vMin = -5.55;
	
	protected Motor_VolumenFunktionAusDatei(CasePara cp){
		super(cp);
		mvfr = new EngineFileReader(cp,
				cp.get_ColumnToRead("spalte_Volumen",false),
				cp.get_ColumnToRead("spalte_Flaeche",false),
				cp.get_FileToRead("motorDatei"));
	
	}

	public double get_V(double time) {
		return mvfr.getV_Motor(time);
	}

	public double get_dV(double time) {
		double dV = mvfr.getV_Motor(time+CP.SYS.WRITE_INTERVAL_SEC) - mvfr.getV_Motor(time);
		return dV;
	}


	public boolean isHubKolbenMotor() {
		return false;
	}

	@Override
	public double get_BrennraumFlaeche(double time) {
		return mvfr.getA_Motor(time);
	}

	@Override
	public double get_Verdichtung() {
		return get_V_MAX()/get_V_MIN();
	}

	@Override
	public double get_V_MAX() {
		if(vMax == -5.55){
			double[] timeline = mvfr.getZeitachse();
			for(int i=0;i<timeline.length;i++){
				if(vMax < mvfr.getV_Motor(timeline[i])){
					vMax = mvfr.getV_Motor(timeline[i]);
				}
			}
		}
		return vMax;
	}
	public double get_V_MIN() {
		if(vMin == -5.55){
			vMin = Double.MAX_VALUE;
			double[] timeline = mvfr.getZeitachse();
			for(int i=0;i<timeline.length;i++){
				if(vMin < mvfr.getV_Motor(timeline[i])){
					vMin = mvfr.getV_Motor(timeline[i]);
				}
			}
		}
		return vMin;
	}
}
