package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class APICalls {
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";
	final String TAG = "ownRadio";
	Context MainContext;
	String serverPath = "http://api.ownradio.ru/v3/";

	public APICalls(Context context) {
		this.MainContext = context;
	}

	//Получает ID пользователя по DeviceID
	public String GetUserId(String deviceID) {
		//Пока не реализована привязка пользователей
		//userid=deviceid
		return deviceID;
	}

	//Получает ID следующего трека
	public Map<String, String> GetNextTrackID(String deviceId) {
		CheckConnection checkConnection = new CheckConnection();
		boolean internetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!internetConnect){
			Log.d(TAG, "Internet is disconnected");
			Intent i = new Intent(ActionSendInfoTxt);
			i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " Internet is disconnected");
			MainContext.sendBroadcast(i);
			return null;
		}

		try {
			Map<String, String> result = new GetNextTrack(MainContext).execute(deviceId).get();
			UUID.fromString(result.get("id")).toString();
			return result;
		}catch (Exception ex) {
			return null;
		}
	}

	public void SendHistory(String deviceId, int count){
		CheckConnection checkConnection = new CheckConnection();
		boolean internetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!internetConnect) {
			Intent i = new Intent(ActionSendInfoTxt);
			i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " Internet is disconnected");
			MainContext.sendBroadcast(i);
			return; //Подключение к интернету отсутствует
		}

		final HistoryDataAccess historyDataAccess = new HistoryDataAccess(MainContext);
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
								Log.i(TAG, "History is sending");
								Intent i = new Intent(ActionSendInfoTxt);
								i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " Connection is successful");
								MainContext.sendBroadcast(i);
							} else {
								Log.i(TAG, "SendHistory: Server response: " + response.code());
								Intent i = new Intent(ActionSendInfoTxt);
								i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "Server response: " + response.code());
								MainContext.sendBroadcast(i);
							}
						}
					}
					@Override
					public void onFailure(Call<Void> call, Throwable t) {
						Log.i(TAG, "An error occurred during networking");
					}
				});
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
}