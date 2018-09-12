package ru.netvoxlab.ownradio;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static ru.netvoxlab.ownradio.Constants.DEVICE_ID;
import static ru.netvoxlab.ownradio.Constants.IS_FIRST_TIME_LAUNCH;

/**
 * Created by a.polunina on 13.06.2017.
 */

public class PrefManager {
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	Context _context;
	
	// shared pref mode
	int PRIVATE_MODE = 0;
	
	// Shared preferences file name
//	private static final String PREF_NAME = "android-ownradio-pref";
	
	public PrefManager(Context context) {
		this._context = context;
//		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		editor = pref.edit();
	}
	
	public void setFirstTimeLaunch(boolean isFirstTime) {
		editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
		editor.commit();
	}
	
	public boolean isFirstTimeLaunch() {
		return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
	}
	
	public void setDeviceId(String deviceId){
		editor.putString(DEVICE_ID, deviceId);
		editor.commit();
	}
	
	public String getDeviceId(){
		return pref.getString(DEVICE_ID,"");
	}
	
	public void setPrefItem(String key, String item){
		editor.putString(key, item);
		editor.commit();
	}

	public String getPrefItem(String key){
		return pref.getString(key, "");
	}
	
	public String getPrefItem(String key, String defaultItem){
		return pref.getString(key, defaultItem);
	}
	
	public int getPrefItemInt(String key, int defaultItem){
		return pref.getInt(key, defaultItem);
	}
	
	public void setPrefItemInt(String key, int item){
		editor.putInt(key, item);
		editor.commit();
	}
	
	public boolean getPrefItemBool(String key, boolean defaultItem) {
		return  pref.getBoolean(key, defaultItem);
	}
	
	public void setPrefItemBool(String key, boolean item){
		editor.putBoolean(key, item);
		editor.commit();
	}
}