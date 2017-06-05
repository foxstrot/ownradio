package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.AsyncTask;

import java.net.HttpURLConnection;

import retrofit2.Response;

/**
 * Created by a.polunina on 17.04.2017.
 */

public class RegisterDevice extends AsyncTask <String, Void, Boolean> {
	private Boolean res;
	Context mContext;
	
	public RegisterDevice(Context context){
		this.mContext = context;
	}
	
	protected Boolean doInBackground(String... data) {
		try{
			Response<Void> response = ServiceGenerator.createService(APIService.class).registerDevice(data[0], data[1]).execute();
			if (response.isSuccessful())
				if (response.code() == HttpURLConnection.HTTP_OK) {
					
					new Utilites().SendInformationTxt(mContext, "Device is register");
					
				} else {
					new Utilites().SendInformationTxt(mContext, "Device is not register - Server response: " + response.code());
				}
		} catch (Exception ex) {
			ex.printStackTrace();
			new Utilites().SendInformationTxt(mContext, "Device is not register -  " + ex.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		res = result;
	}
}
