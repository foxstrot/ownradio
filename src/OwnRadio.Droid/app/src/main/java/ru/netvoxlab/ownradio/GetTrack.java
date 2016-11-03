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
import android.widget.Toast;

import java.io.File;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class GetTrack {
	private long downloadReference;
	private DownloadManager downloadManager;
	TrackDB trackDB;
	SQLiteDatabase db;
	String TrackID;
	TrackDataAccess trackDataAccess;

	public GetTrack() {
	}

	public void GetTrackDM(Context context, String trackId) {
//        context.registerReceiver(receiver, new IntentFilter(
//                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		trackDataAccess = new TrackDataAccess(context);
		TrackID = trackId;
		context.registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		try {
			//Локальное имя трека
			String fileName = "trackid_" + trackId + ".mp3";

			downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
			context.registerReceiver(receiver, new IntentFilter(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse("http://ownradio.ru/api/track/GetTrackByID/" + trackId));
			//Загрузка треков осуществляется только через Wi-Fi
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
			//Заголовок для вывода в уведомление
			request.setTitle(fileName);
//            request.allowScanningByMediaScanner();
			//Задаем директорию для кэширования треков во внешней памяти в папке приложения
			request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, fileName);
			downloadReference = downloadManager.enqueue(request);
//                    trackURL = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + fileName;
		} catch (Exception ex) {
			Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));


//доработать сохранение пути загрузки
						File file1 = new File(uriString);
						File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + file1.getName());


						ContentValues track = new ContentValues();
						track.put("id", TrackID);
						track.put("trackurl", file.getAbsolutePath());
						track.put("datetimelastlisten", "");
						track.put("islisten", "0");
						track.put("isexist", "1");
						trackDataAccess.SaveTrack(track);
					}
				}
			}
		}
	};
}




