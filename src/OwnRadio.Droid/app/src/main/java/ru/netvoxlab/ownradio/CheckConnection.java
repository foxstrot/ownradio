package ru.netvoxlab.ownradio;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by a.polunina on 31.10.2016.
 */

public class CheckConnection {

	public boolean CheckWifiConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo inetInfo = connectivityManager.getActiveNetworkInfo();

		if(inetInfo == null)
			return false;

		if (!wifiInfo.isConnected())
			return false;
        else
			return true;
	}

	public boolean CheckInetConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
		NetworkInfo inetInfo = connectivityManager.getActiveNetworkInfo();
		if(inetInfo == null)
			return false;

		if (!inetInfo.isConnected())
			return false;
		else
			return true;
	}
}
