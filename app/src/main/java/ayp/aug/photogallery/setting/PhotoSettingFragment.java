package ayp.aug.photogallery.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

import ayp.aug.photogallery.PhotoGalleryPreference;
import ayp.aug.photogallery.R;

/**
 * Created by Nutdanai on 9/5/2016.
 */
public class PhotoSettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "PhotoSettingFragment";

    public static PhotoSettingFragment newInstance() {

        Bundle args = new Bundle();

        PhotoSettingFragment fragment = new PhotoSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Boolean useGPS = PhotoGalleryPreference.getUseGPS(getActivity());
        Log.d(TAG,"Use GPS = " +useGPS);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_photo_setting);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        onSharedPreferenceChanged(sharedPreferences,"use_gps");

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG,"On Share Preference changed");
        Preference p = findPreference(key);
        if( p instanceof SwitchPreferenceCompat) {
            boolean value = sharedPreferences.getBoolean(key, false);
            p.setSummary(value ? "ON" : "OFF");
        }else{
            p.setSummary(sharedPreferences.getString(key,""));
        }

    }
}
