package ayp.aug.photogallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nutdanai on 8/19/2016.
 */
public class PhotoGalleryPreference {
    private static final String TAG = "PhotoGalleryPref";
    protected static final String PREF_SEARCH_KEY = "PhotoGalleryPreference";
    protected static final String PREF_LAST_ID = "PREF_LAST_ID";
    protected static final String PREF_IS_ALARM_ON = "PREF_IS_ALARM_ON";
    protected static final String PREF_USE_GPS = "use_gps";


    public static void setStoredIsAlarmOn(Context context, Boolean isAlarmOn) {
        mySharedPref(context).edit().putBoolean(PREF_IS_ALARM_ON, isAlarmOn).apply();
    }

    public static Boolean getStoredIsAlarmOn(Context context) {
        return mySharedPref(context).getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static Boolean getUseGPS(Context context) {
        return mySharedPref(context).getBoolean(PREF_USE_GPS, false);
    }

    public static void setUseGPS(Context ctx,boolean use_GPS){
        mySharedPref(ctx).edit().putBoolean(PREF_USE_GPS,use_GPS).apply();
    }

    public static SharedPreferences mySharedPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getStoredSearchKey(Context context) {
        return mySharedPref(context).getString(PREF_SEARCH_KEY, null);
    }

    public static void setStoredSearchKey(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PREF_SEARCH_KEY, key).apply(); // You can put more than one.
    }


    public static String getStoredLastId(Context context) {
        return mySharedPref(context).getString(PREF_LAST_ID, null);
    }

    public static void setStoredLastId(Context context, String lastId) {
        mySharedPref(context).edit().putString(PREF_LAST_ID, lastId).apply(); // You can put more than one.
    }


}
