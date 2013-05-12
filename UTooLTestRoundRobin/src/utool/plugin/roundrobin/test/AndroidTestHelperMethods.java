package utool.plugin.roundrobin.test;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Public helper methods for automated android tests
 * @author Justin Kreier
 * @version 1/20/2013
 */
public class AndroidTestHelperMethods {

	/**
	 * Wipes the application data (Similar to pressing "Clear Data" from apps settings)
	 * @param c The application context
	 */
	public static void clearApplicationData(Context c) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = settings.edit();
		e.clear();
		e.commit();
		
	}

}
