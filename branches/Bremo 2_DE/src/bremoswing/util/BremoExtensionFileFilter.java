package bremoswing.util;

public class BremoExtensionFileFilter {
	
	ExtensionFileFilter[] BremoListExtentionFilter;
	
	public BremoExtensionFileFilter() {

		ExtensionFileFilter txt_filter = new ExtensionFileFilter("TXT", "txt");
		ExtensionFileFilter dva_filter = new ExtensionFileFilter("DVA", "dva");
		ExtensionFileFilter lwa_filter = new ExtensionFileFilter("LWA", "lwa");
		ExtensionFileFilter apr_filter = new ExtensionFileFilter("APR", "apr");
		ExtensionFileFilter dbd_filter = new ExtensionFileFilter("DBD", "dbd");
		ExtensionFileFilter dbl_filter = new ExtensionFileFilter("DBL", "dbl");
		ExtensionFileFilter vlt_filter = new ExtensionFileFilter("VLT", "vlt");
		ExtensionFileFilter pfd_filter = new ExtensionFileFilter("PFD", "pfd");
		
		BremoListExtentionFilter = new ExtensionFileFilter[] {
				txt_filter, dva_filter, lwa_filter, apr_filter, dbd_filter,
				dbl_filter, vlt_filter, pfd_filter };
	}
	
	public ExtensionFileFilter[] getBremoListExtentionFilter () {
		return BremoListExtentionFilter;
	}
}
