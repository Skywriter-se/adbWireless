package siir.es.adbWireless;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("unused")
public class ManagePreferences extends PreferenceActivity
{

	private CheckBoxPreference mPreferenceVibrate;
	private CheckBoxPreference mPreferenceSound;
	private CheckBoxPreference mPreferenceNoti;
	private CheckBoxPreference mPreferenceHaptic;
	private CheckBoxPreference mPreferenceWiFiOff;
	private CheckBoxPreference mPreferenceWiFiOn;
	
	private SharedPreferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mPreferenceVibrate = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_vibrate_key));
		mPreferenceSound = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_sound_key));
		mPreferenceNoti = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_noti_key));
		mPreferenceHaptic = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_haptic_key));
		mPreferenceWiFiOff = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_wifi_off_key));
		mPreferenceWiFiOn = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_wifi_on_key));
		
	}
}