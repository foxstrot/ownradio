package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Map;

import retrofit2.Response;

/**
 * Created by a.polunina on 24.10.2016.
 */

public class GetNextTrack extends AsyncTask<String, Void, Map<String, String>> {
	private Map<String, String> trackModel;
	Context mContext;

	public GetNextTrack(Context context){
		this.mContext = context;
	}

	protected Map<String, String> doInBackground(String... data) {
		try {
			//выполняем запрос к серверу getnexttrackid
			Response<Map<String, String>> response = ServiceGenerator.createService(APIService.class).getNextTrackID(data[0]).execute();
			if (response.code() == 200 && !response.body().isEmpty() && response.body().get("result").equals("true")) {
				new Utilites().SendInformationTxt(mContext, "GetNextTrackID(" + data[0]+ "): Information about next track is received.");
				return response.body();
			}else {
				new Utilites().SendInformationTxt(mContext, "GetNextTrackID(" + data[0]+ "): Error with response code: " + response.code());
				return null;
			}
		} catch (Exception ex) {
			new Utilites().SendInformationTxt(mContext, "GetNextTrackID(" + data[0]+ "): exception " + ex.getLocalizedMessage());
			return null;
		}
	}

	protected void onPostExecute(Map<String, String> result) {
		super.onPostExecute(result);
		trackModel = result;
	}
}