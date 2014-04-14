package berechnungsModule.blowby;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class BlowByFabrik extends ModulFabrik{

	public static final String BLOWBY_FLAG = "BlowByModell";
	public static final String[] BLOWBY_MODELLE = {
		"ohne", "Leckage", "FromFile"};
	public final BlowBy BLOW_BY_MODELL;
	
	public BlowByFabrik(CasePara cp) {
		super(cp);
		
		String blowBy_ModellVorgabe=
				get_ModulWahl(BLOWBY_FLAG, BLOWBY_MODELLE);
		
		if(blowBy_ModellVorgabe.equals(BLOWBY_MODELLE[0])){
			BLOW_BY_MODELL = new leckagefrei(cp);
		}else if(blowBy_ModellVorgabe.equals(BLOWBY_MODELLE[1])){
			BLOW_BY_MODELL = new LeckageMassenstrom(cp);
		}else if(blowBy_ModellVorgabe.equals(BLOWBY_MODELLE[2])){
			BLOW_BY_MODELL = new FromFile(cp);
		}else {
			BLOW_BY_MODELL = null;
			try{
				throw new BirdBrainedProgrammerException("Das ausgewaehlte BlowByModell \"" + blowBy_ModellVorgabe + 
							" \" wurde im InputFile " +
							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
							"Es fehlt der entsprechende else-if-Block oder das  BlowByModell wurde noch nicht implementiert");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
		}
	}

}
