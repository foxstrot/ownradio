package ru.netvoxlab.ownradio;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import retrofit2.Response;

import static ru.netvoxlab.ownradio.MainActivity.ActionSendInfoTxt;

/**
 * Created by a.polunina on 24.10.2016.
 */

public class GetNextTrack extends AsyncTask<String, Void, Map<String, String>> {
	private Map<String, String> trackModel;
	final String TAG = "ownRadio";
	Context mContext;

	public GetNextTrack(Context context){
		this.mContext = context;
	}

	protected Map<String, String> doInBackground(String... data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
		try {
//			Call<Map<String, String>> res = ServiceGenerator.createService(APIService.class).getNextTrackID(data[0]);

			Response<Map<String, String>> response = ServiceGenerator.createService(APIService.class).getNextTrackID(data[0]).execute();
			if (response.code() == 200) {
				Intent i = new Intent(ActionSendInfoTxt);
				i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " Connection is successful");
				mContext.sendBroadcast(i);
				return response.body();
			}else {
				Log.i(TAG, "GetNextTrackID(): Response code: " + response.code());
				Intent i = new Intent(ActionSendInfoTxt);
				i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) +"GetNextTrackID(): Response code: " + response.code());
				mContext.sendBroadcast(i);
				return null;
			}
		} catch (Exception ex) {
			Log.i(TAG, "GetNextTrackID(): exception " + ex.getLocalizedMessage());
			Intent i = new Intent(ActionSendInfoTxt);
			i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) +"GetNextTrackID(): exception " + ex.getLocalizedMessage());
			mContext.sendBroadcast(i);
			return null;
		}
	}

	protected void onPostExecute(Map<String, String> result) {
		super.onPostExecute(result);
		trackModel = result;
	}
}
