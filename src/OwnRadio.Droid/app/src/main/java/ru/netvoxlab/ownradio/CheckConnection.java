package ru.netvoxlab.ownradio;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by a.polunina on 31.10.2016.
 */

public class CheckConnection {
	public static final String ONLY_WIFI = "0";
	public static final String ALL_CONNECTION_TYPES = "1";
	
	public boolean CheckInetConnection(Context mCcontext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mCcontext.getSystemService(mCcontext.CONNECTIVITY_SERVICE);
		NetworkInfo inetInfo = connectivityManager.getActiveNetworkInfo();
		//Если никакого интернет подключения нет - возвращаем false
		if(inetInfo == null || !inetInfo.isConnected()){
			new Utilites().SendInformationTxt(mCcontext, "Internet is disconnected");
			return false;
		}
		NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		//Считывем используемый тип подключения из настроек
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCcontext);
		String connectionType = prefs.getString("internet_connections_list", ONLY_WIFI);
		switch (connectionType){
			case ONLY_WIFI:
				if (!wifiInfo.isConnected()) {
					new Utilites().SendInformationTxt(mCcontext, "WiFi is disconnected");
					return false;
				}
				else {
					new Utilites().SendInformationTxt(mCcontext, "WiFi is connected");
					return true;
				}
			case ALL_CONNECTION_TYPES:
				new Utilites().SendInformationTxt(mCcontext, "Internet is connected");
				return true;
			default:
				new Utilites().SendInformationTxt(mCcontext, "WiFi is disconnected");
				return false;
		}
	}
}
