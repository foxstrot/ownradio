package ru.netvoxlab.ownradio;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.netvoxlab.ownradio.MainActivity.ActionSendInfoTxt;
import static ru.netvoxlab.ownradio.MainActivity.TAG;

/**
 * Created by a.polunina on 11.01.2017.
 */

public class Utilites {

	public void SendInformationTxt(Context mContext, String message){
		Log.d(TAG, message);
		Intent i = new Intent(ActionSendInfoTxt);
		i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " " + message);
		mContext.sendBroadcast(i);
	}
}