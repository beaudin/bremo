package berechnungsModule.wallHeatTransfer;

import io.HeatFluxFileReader;
import misc.VectorBuffer;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;



public class FromFile extends WallHeatTransfer {
	
	private HeatFluxFileReader whtR;

	public FromFile(CasePara cp) {
		super(cp);		
		 whtR= new HeatFluxFileReader(cp,
				 cp.get_ColumnToRead("column_dQw"),
				 cp.get_FileToRead("htfFileName"));
	}

	@Override
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN,
			double fortschritt) {		
		return 0;
	}

	@Override
	public double get_BrennraumFlaeche(double time) {	
		return 0;
	}
	
	@Override
	public double get_wallHeatFlux(double time, Zone[] zonen_IN, double fortschritt, VectorBuffer tBuffer){
		return whtR.get_Value(time);
	}
	

}
