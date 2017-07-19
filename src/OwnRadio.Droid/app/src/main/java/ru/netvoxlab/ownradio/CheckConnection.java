package ru.netvoxlab.ownradio;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static ru.netvoxlab.ownradio.Constants.ALL_CONNECTION_TYPES;
import static ru.netvoxlab.ownradio.Constants.INTERNET_CONNECTION_TYPE;
import static ru.netvoxlab.ownradio.Constants.ONLY_WIFI;

/**
 * Created by a.polunina on 31.10.2016.
 */

public class CheckConnection {
	
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
		PrefManager prefManager = new PrefManager(mCcontext);
		String connectionType = prefManager.getPrefItem(INTERNET_CONNECTION_TYPE, ALL_CONNECTION_TYPES);
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
