package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class APICalls {
	Context mContext;

	public APICalls(Context context) {
		this.mContext = context;
	}

	//Возращает ID пользователя по DeviceID
	public String GetUserId(String deviceID) {
		//Пока не реализована привязка пользователей
		//userid=deviceid
		return deviceID;
	}

	//Возращает ID следующего трека
	public Map<String, String> GetNextTrackID(String deviceId) {
		CheckConnection checkConnection = new CheckConnection();
		boolean internetConnect = checkConnection.CheckInetConnection(mContext);
		if (!internetConnect)
			return null;

		try {
			Map<String, String> result = new GetNextTrack(mContext).execute(deviceId).get();
			UUID.fromString(result.get("id")).toString();
			return result;
		}catch (Exception ex) {
			new Utilites().SendInformationTxt(mContext, " " + ex.getLocalizedMessage());
			return null;
		}
	}

	//Пытается отправить count записей истории прослушивания треков
	public void SendHistory(String deviceId, int count){
		CheckConnection checkConnection = new CheckConnection();
		if (!checkConnection.CheckInetConnection(mContext)) {
			return;
		}

		final HistoryDataAccess historyDataAccess = new HistoryDataAccess(mContext);
		final ContentValues[] historyRecs = historyDataAccess.GetHistoryRec(count);

		if(historyRecs == null)
			return;

		try {
			for( int rec = 0; rec < count; rec++) {
//				final ContentValues historyRec = historyDataAccess.GetHistoryRec();
				final ContentValues historyRec = historyRecs[rec];
				if(historyRec == null) //Если неотправленной статистики нет - выходим
					return;

				if(historyRec.getAsString("trackid").equals("")){
					historyDataAccess.DeleteHistoryRec(historyRec.getAsString("id"));
					break;
				}

//				HistoryModel data = new HistoryModel("2016-11-16T13:15:15",1,1);
				HistoryModel data = new HistoryModel();
				data.setLastListen(historyRec.getAsString("lastListen"));
				data.setIsListen(historyRec.getAsInteger("isListen"));
				data.setMethodid(historyRec.getAsInteger("methodid"));

				ServiceGenerator.createService(APIService.class).sendHistory(deviceId,historyRec.getAsString("trackid"), data)
				.enqueue(new Callback<Void>() {
					@Override
					public void onResponse(Call<Void> call, Response<Void> response) {
						if(response.isSuccessful()){
							if(response.code() == HttpURLConnection.HTTP_OK){
								historyDataAccess.DeleteHistoryRec(historyRec.getAsString("id"));
								new Utilites().SendInformationTxt(mContext, "History is sending");
							} else {
								new Utilites().SendInformationTxt(mContext, "SendHistory: Server response: " + response.code());
							}
						}
					}
					@Override
					public void onFailure(Call<Void> call, Throwable t) {
						new Utilites().SendInformationTxt(mContext, "SendHistory: An error occurred during networking");
					}
				});
			}
		}catch (Exception ex){
			ex.printStackTrace();
			new Utilites().SendInformationTxt(mContext, " " + ex.getLocalizedMessage());
		}
	}
}