package berechnungsModule.blowby;

import io.BlowByFileReader;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public class FromFile extends BlowBy {

	private BlowByFileReader blowby;
	
	protected FromFile(CasePara CP) {
		super(CP);
		blowby = new BlowByFileReader(CP,
				CP.get_ColumnToRead("spalte_dmL"),
				CP.get_FileToRead("blowbyFileName"));
	}


	public double get_mLeckage(double time, Zone[] zonen_IN) {
		return blowby.getdmL(time);
	}


	public boolean is_Berechnet() {
		return false;
	}

}
