package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class ExecuteProcedurePostgreSQL {
	Context MainContext;
	String serverPath = "http://java.ownradio.ru/api/v2/";//"http://ownradio.ru/api/";
	String serverPathCore = "http://ownradio.ru/api/";

	public ExecuteProcedurePostgreSQL(Context context) {
		this.MainContext = context;
	}

	//Сохраняет нового пользователя и устройство
	public void RegisterDevice(String deviceID, String userName, String deviceName) {
		CheckConnection checkConnection = new CheckConnection();
		boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!inetConnect)
			return; // "Подключение к интернету отсутствует";
		try {
			URL URLRequest = new URL(serverPathCore +"ExecuteProcedurePostgreSQL/RegisterDevice/" + deviceID + "," + userName + "," + deviceName);
			String str = new GetRequest().execute(URLRequest).get();
//            Toast.makeText(MainContext, str, Toast.LENGTH_LONG).show();
		} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
		}
	}

	//Выполняет слияние статистики прослушивания треков на разных устройствах по двум User ID одного пользователя
	public String MergeUserID(String userIDOld, String userIDNew) {
		CheckConnection checkConnection = new CheckConnection();
		boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!inetConnect)
			return "Подключение к интернету отсутствует";
		try {
			URL URLRequest = new URL(serverPathCore + "ExecuteProcedurePostgreSQL/MergeUserID/" + userIDOld + "," + userIDNew);
			String result = new GetRequest().execute(URLRequest).get();
			return result;

		} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
			return ex.getLocalizedMessage();
		} catch (IOException ex) {
			return ex.getLocalizedMessage();
		}
	}

	//Получает ID пользователя по DeviceID
	public String GetUserId(String deviceID) {
		CheckConnection checkConnection = new CheckConnection();
		boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!inetConnect)
			return "Подключение к интернету отсутствует";
		try {
			URL URLRequest = new URL(serverPathCore + "ExecuteProcedurePostgreSQL/GetUserId/" + deviceID);
			String result = new GetRequest().execute(URLRequest).get();
			String userID = result.substring(1, 37); //сделать адекватный парсинг
			return userID;
//            return deviceID;
		} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
			return ex.getLocalizedMessage();
		} catch (IOException ex) {
			return ex.getLocalizedMessage();
		}
	}

	//Переименовывает пользователя
	public String RenameUser(String userID, String newUserName) {
		CheckConnection checkConnection = new CheckConnection();
		boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!inetConnect)
			return "Подключение к интернету отсутствует";
		try {
			URL URLRequest = new URL(serverPathCore + "ExecuteProcedurePostgreSQL/RenameUser/" + userID + "," + newUserName);
			String result = new GetRequest().execute(URLRequest).get();
			return result;
		} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
			return ex.getLocalizedMessage();
		} catch (IOException ex) {
			return ex.getLocalizedMessage();
		}
	}

	//Получает ID следующего трека
	public String GetNextTrackID(String deviceId) {
		CheckConnection checkConnection = new CheckConnection();
		boolean wifiConnect = checkConnection.CheckWifiConnection(MainContext);
		if (!wifiConnect)
			return "Подключение к интернету отсутствует";
		try {
			URL URLRequest = new URL(serverPath + "tracks/" + deviceId + "/next");
//			URL URLRequest = new URL(serverPathCore + "track/GetNextTrackID/" + deviceId);
			String result = new GetRequest().execute(URLRequest).get();
			String trackID = result.substring(1, 37); //сделать адекватный парсинг
			return trackID;
		} catch (MalformedURLException | InterruptedException | ExecutionException ex) {
			return ex.getLocalizedMessage();
		} catch (IOException ex) {
			return ex.getLocalizedMessage();
		}
	}

	//Отправляет статистику на сервер
	public int SetStatusTrack(String deviceId, String trackId, int isListen, String dateTimeListen) {
		CheckConnection checkConnection = new CheckConnection();
		boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!inetConnect)
			return -1; //Подключение к интернету отсутствует
		try {
			URL urlRequest = new URL(serverPath + "histories/" + deviceId + "/" + trackId);
			String data = "lastListen=" + URLEncoder.encode(dateTimeListen, "UTF-8");
			data += "&" + "isListen=" + URLEncoder.encode(String.valueOf(isListen), "UTF-8");
			data += "&" + "method=" + "случайный"; //URLEncoder.encode("случайный", "UTF-8");
//			data += "&" + "device=" + URLEncoder.encode(deviceId, "UTF-8");
//			data = "user=" + URLEncoder.encode(deviceId, "UTF-8");
//			data += "&" + "track=" + URLEncoder.encode(trackId, "UTF-8");

			int result = new PostRequest(MainContext).execute(urlRequest.toString(), data).get();
			return result;
		} catch (Exception ex) {
			return ex.hashCode();
		}
	}

	public void SendHistory(String deviceId, int count){
		CheckConnection checkConnection = new CheckConnection();
		boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
		if (!inetConnect)
			return; //Подключение к интернету отсутствует
		String historyRecID;
		int result;
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

				result = -1;
				URL urlRequest = new URL(serverPath + "histories/" + deviceId + "/" + historyRec.getAsString("trackid"));

				try{
					UUID.fromString(deviceId);
					urlRequest = new URL(serverPath + "histories/" + deviceId + "/" + historyRec.getAsString("trackid"));

				}catch (IllegalArgumentException ex){
					urlRequest = new URL(serverPath + "histories/" + "11111111-1111-1111-1111-111111111111" + "/" + historyRec.getAsString("trackid"));

				}
				result = new PostRequest(MainContext).execute(urlRequest.toString(), historyRec.getAsString("data"), historyRec.getAsString("id")).get(1, TimeUnit.SECONDS);
				if (result == HttpURLConnection.HTTP_OK) {
					historyRecID = historyRec.getAsString("id");
					historyDataAccess.DeleteHistoryRec(historyRecID);
				}
			}
		}catch (Exception ex){
			ex.getStackTrace();
		}
	}
}