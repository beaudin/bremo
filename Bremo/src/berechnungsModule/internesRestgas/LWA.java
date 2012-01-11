package berechnungsModule.internesRestgas;

import bremo.parameter.CasePara;
import bremo.sys.Rechnung;

public class LWA extends InternesRestgas {
	private double mAGRint;

	protected LWA(CasePara cp) {
		super(cp);
		//Aufruf im Konstruktor damit nur einmal gerechnet wird
		mAGRint=Rechnung.ladungswechselAnalyseDurchfuehren(super.CP);
	}

	@Override
	public double get_mInternesRestgas_ASP() {		
		return mAGRint;
	}
}
