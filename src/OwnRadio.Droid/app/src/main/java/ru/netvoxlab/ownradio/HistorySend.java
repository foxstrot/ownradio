package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import java.net.HttpURLConnection;

import retrofit2.Response;

/**
 * Created by a.polunina on 04.04.2017.
 */

public class HistorySend extends AsyncTask <String, Void, Boolean>{
	private Boolean res;
	Context mContext;
	
	public HistorySend(Context context){
		this.mContext = context;
	}
	
	protected Boolean doInBackground(String... data) {
		final HistoryDataAccess historyDataAccess = new HistoryDataAccess(mContext);
		final ContentValues historyRecs = historyDataAccess.GetHistoryRec();
		
		if (historyRecs == null)
			return true;
		
		try {
				final ContentValues historyRec = historyDataAccess.GetHistoryRec();
				if (historyRec == null) //Если неотправленной статистики нет - выходим
					return true;
				
				if (historyRec.getAsString("trackid").equals("")) {
					historyDataAccess.DeleteHistoryRec(historyRec.getAsString("id"));
					return true;
				}

//				HistoryModel data = new HistoryModel("2016-11-16T13:15:15",1,1);
				HistoryModel historyData = new HistoryModel();
				historyData.setLastListen(historyRec.getAsString("lastListen"));
				historyData.setIsListen(historyRec.getAsInteger("isListen"));
				
				Response<Void> response = ServiceGenerator.createService(APIService.class).sendHistory(data[0], historyRec.getAsString("trackid"), historyData).execute();
				if (response.isSuccessful())
					if (response.code() == HttpURLConnection.HTTP_OK) {
						historyDataAccess.DeleteHistoryRec(historyRec.getAsString("id"));
						new Utilites().SendInformationTxt(mContext, "History is sending");
						
					} else {
						new Utilites().SendInformationTxt(mContext, "SendHistory: Server response: " + response.code());
					}
//							public void onFailure(Call<Void> call, Throwable t) {
//								new Utilites().SendInformationTxt(mContext, "SendHistory: An error occurred during networking");
//							}
		} catch (Exception ex) {
			ex.printStackTrace();
			new Utilites().SendInformationTxt(mContext, " " + ex.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		res = result;
	}
}
