package bremoswing.manager;

import java.util.Locale;
import java.util.ResourceBundle;

public  class ManagerLanguage {
private static Locale locale;
private static ResourceBundle rb;

	public static void managerLanguage(Locale locale) {

		setRb(ResourceBundle.getBundle("bremoswing.RessourceBundle.LabelsBundle", locale));
	}

	public static ResourceBundle getRb() {
		return rb;
	}

	public static void setRb(ResourceBundle rb) {
		ManagerLanguage.rb = rb;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		ManagerLanguage.locale = locale;
	}
	    
}


