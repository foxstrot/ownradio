package ru.netvoxlab.ownradio;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by a.polunina on 31.10.2016.
 */

public class CheckConnection {

	public boolean CheckWifiConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifiInfo.isConnected()) {
            Toast.makeText(context, "Wifi disconnected", Toast.LENGTH_SHORT).show();
//            Log.d("Wi-Fi", "Wifi disconnected.");
//            AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext());
//            builder.setTitle("")
//                    .setMessage("Wifi disconnected.")
//                    .setCancelable(true)
//                    .setNegativeButton("OK",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.cancel();
//                                }
//                            });
//            AlertDialog alert = builder.create();
//            try {alert.show();    }catch(Exception ex){
//                ex.getLocalizedMessage();
//            }

            return false;
        }
        else
		return true;
	}

	public boolean CheckInetConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
		NetworkInfo inetInfo = connectivityManager.getActiveNetworkInfo();

		if (!inetInfo.isConnected()) {
			Toast.makeText(context, "Internet disconnected", Toast.LENGTH_SHORT).show();
			return false;
		} else
			return true;
	}
}
