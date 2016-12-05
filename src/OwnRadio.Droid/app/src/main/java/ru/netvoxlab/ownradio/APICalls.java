package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class APICalls {
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";
	final String TAG = "ownRadio";
	Context MainContext;
	String serverPath = "http://api.ownradio.ru/v3/";
//	String serverPath = "http://java.ownradio.ru/api/v2/";

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
	public JSONObject GetNextTrackID(String deviceId) {
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
			URL URLRequest = new URL(serverPath + "tracks/" + deviceId + "/next");
			String result = new GetRequest().execute(URLRequest).get();

			try {
				JSONObject jsonObject = new JSONObject(result);
				UUID.fromString(jsonObject.getString("id")).toString();

				return jsonObject;
//				return UUID.fromString(trackId).toString(); //сделать адекватный парсинг
			}catch (Exception ex) {
				Log.d(TAG, "GetNextTrackID() was return " + result);
				Intent i = new Intent(ActionSendInfoTxt);
				i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " " + result);
				MainContext.sendBroadcast(i);
				return null;
			}
		} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
			Log.d(TAG, " " + ex.getLocalizedMessage());
			return null;
		} catch (IOException ex) {
			Log.d(TAG, " " + ex.getLocalizedMessage());
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

		String historyRecID;
		int result;
		URL urlRequest;
		HistoryDataAccess historyDataAccess = new HistoryDataAccess(MainContext);
		try {
			for( int rec = 0; rec < count; rec++) {
				ContentValues historyRec = historyDataAccess.GetHistoryRec();

				if(historyRec == null) //Если неотправленной статистики нет - выходим
					return;

				if(historyRec.getAsString("trackid").equals("")){
					historyRecID = historyRec.getAsString("id");
					historyDataAccess.DeleteHistoryRec(historyRecID);
					break;
				}

				urlRequest = new URL(serverPath + "histories/" + deviceId + "/" + historyRec.getAsString("trackid"));

				result = new PostRequest(MainContext).execute(urlRequest.toString(), historyRec.getAsString("data"), historyRec.getAsString("id")).get();
				if (result == HttpURLConnection.HTTP_OK) {
					historyRecID = historyRec.getAsString("id");
					historyDataAccess.DeleteHistoryRec(historyRecID);
				} else {
					Intent i = new Intent(ActionSendInfoTxt);
					i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "Server response: " + result);
					MainContext.sendBroadcast(i);
				}
			}
		}catch (Exception ex){
			ex.getStackTrace();
		}
	}
}