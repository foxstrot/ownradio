package ru.netvoxlab.ownradio;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class GetTrack {
	public static final String ActionTrackInfoUpdate = "ru.netvoxlab.ownradio.action.TRACK_INFO_UPDATE";
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";

	final String TAG = "ownRadio";

	private long downloadReference;
	private DownloadManager downloadManager;
	TrackDB trackDB;
	SQLiteDatabase db;
	String TrackID;
	TrackDataAccess trackDataAccess;
	JSONObject TrackJSON;
	public GetTrack() {
	}

	public void GetTrackDM(Context context, JSONObject dataJSON) {
//        context.registerReceiver(receiver, new IntentFilter(
//                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		trackDataAccess = new TrackDataAccess(context);

		context.registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		try {
			TrackJSON = dataJSON;
			TrackID = dataJSON.getString("id");
			//Локальное имя трека
			String fileName = TrackID + ".mp3";

			downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
			context.registerReceiver(receiver, new IntentFilter(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse("http://api.ownradio.ru/v3/tracks/" + TrackID));//Java
//					Uri.parse("http://java.ownradio.ru/api/v2/tracks/" + trackId));//Java
//					Uri.parse("http://ownradio.ru/api/track/GetTrackByID/" + trackId));//Core
			//Загрузка треков осуществляется только через Wi-Fi
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
			//Отключаем уведомления о загрузке
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			//Заголовок для вывода в уведомление
			request.setTitle(fileName);
//            request.allowScanningByMediaScanner();
			//Задаем директорию для кэширования треков во внешней памяти в папке приложения
			request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, fileName);
			downloadReference = downloadManager.enqueue(request);
//                    trackURL = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + fileName;
		} catch (Exception ex) {
			Intent i = new Intent(ActionSendInfoTxt);
			i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + ex.getLocalizedMessage());
			context.sendBroadcast(i);
//			Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//            return -1;
		}
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
				long downloadId = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				DownloadManager.Query query = new DownloadManager.Query();
				query.setFilterById(downloadReference);
				Cursor c = downloadManager.query(query);
				if (c.moveToFirst()) {
					int columnIndex = c
							.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c
							.getInt(columnIndex)) {

//                        Toast.makeText(context, "D. Successful", Toast.LENGTH_LONG).show();

						String uriString = c
								.getString(c
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));//.COLUMN_LOCAL_FILENAME));


//доработать сохранение пути загрузки
						File file1 = new File(uriString);
						File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + file1.getName());


						ContentValues track = new ContentValues();
						track.put("id", TrackID);
						track.put("trackurl", file.getAbsolutePath());
						track.put("datetimelastlisten", "");
						track.put("islisten", "0");
						track.put("isexist", "1");
						try {
							track.put("title", TrackJSON.getString("name"));
							track.put("artist", TrackJSON.getString("artist"));
							track.put("length", TrackJSON.getString("length"));
							track.put("methodid", TrackJSON.getString("methodid"));
						}catch (Exception ex){
							Log.d(TAG, " " + ex.getLocalizedMessage());
						}
						trackDataAccess.SaveTrack(track);

						Intent i = new Intent(ActionTrackInfoUpdate);
						context.sendBroadcast(i);
					}
				}
			}
		}
	};
}




