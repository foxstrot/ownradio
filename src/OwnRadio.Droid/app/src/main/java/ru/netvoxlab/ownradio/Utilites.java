package ru.netvoxlab.ownradio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.netvoxlab.ownradio.MainActivity.ActionProgressBarFirstTracksLoad;
import static ru.netvoxlab.ownradio.MainActivity.ActionSendInfoTxt;
import static ru.netvoxlab.ownradio.MainActivity.TAG;
import static ru.netvoxlab.ownradio.RequestAPIService.ACTION_GETNEXTTRACK;
import static ru.netvoxlab.ownradio.RequestAPIService.ACTION_SENDLOGS;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_COUNT;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_DEVICEID;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_LOGFILEPATH;

/**
 * Created by a.polunina on 11.01.2017.
 */

public class Utilites {

	public void SendInformationTxt(Context mContext, String message){
		Log.d(TAG, message);
		Intent i = new Intent(ActionSendInfoTxt);
		i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " " + message);
		mContext.sendBroadcast(i);
	}

	public boolean CheckCountTracksAndDownloadIfNotEnought(Context mContext, String DeviceId){
		if (new TrackDataAccess(mContext).GetExistTracksCount() < 1) {
			if (new CheckConnection().CheckInetConnection(mContext)) {
				new Utilites().SendInformationTxt(mContext, "Подождите пока наполнится кеш");
				Intent i = new Intent(ActionProgressBarFirstTracksLoad);
				i.putExtra("ProgressOn", true);
				mContext.sendBroadcast(i);
				//		Запускаем кеширование треков - 3 шт
				Intent downloaderIntent = new Intent(mContext, RequestAPIService.class);
				downloaderIntent.setAction(ACTION_GETNEXTTRACK);
				downloaderIntent.putExtra(EXTRA_DEVICEID, DeviceId);
				downloaderIntent.putExtra(EXTRA_COUNT, 3);
				mContext.startService(downloaderIntent);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Ошибка")
						.setMessage("Невозможно кешировать треки. Проверьте интернет подключение.")
						.setCancelable(false)
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alert.show();
			}
			return false;
		}else return true;
	}
	
	public boolean SendLogs(Context mContext, String deviceId){
		try {
			File appDirectory = mContext.getFilesDir();
			File logDirectory = new File( appDirectory + File.separator + "log" );
			// create app folder
			if ( !appDirectory.exists() ) {
				appDirectory.mkdir();
			}
			// create log folder
			if ( !logDirectory.exists() ) {
				logDirectory.mkdir();
			}
			File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );
			Runtime.getRuntime().exec("logcat -d -f " + logFile.getAbsolutePath());
			Intent logSenderIntent = new Intent(mContext, RequestAPIService.class);
			logSenderIntent.setAction(ACTION_SENDLOGS);
			logSenderIntent.putExtra(EXTRA_DEVICEID, deviceId); //getApplicationContext()).execute(sp.getString("DeviceID", "")
			logSenderIntent.putExtra(EXTRA_LOGFILEPATH, logFile.getAbsolutePath());
			mContext.startService(logSenderIntent);
			return true;
		}catch (Exception ex){
			new Utilites().SendInformationTxt(mContext, "Error in sendLogFile(): " + ex.getLocalizedMessage());
			return false;
		}
	}

}