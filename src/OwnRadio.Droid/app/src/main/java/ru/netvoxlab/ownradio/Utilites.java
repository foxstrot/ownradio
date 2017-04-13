package ru.netvoxlab.ownradio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.netvoxlab.ownradio.MainActivity.ActionProgressBarFirstTracksLoad;
import static ru.netvoxlab.ownradio.MainActivity.ActionSendInfoTxt;
import static ru.netvoxlab.ownradio.MainActivity.TAG;

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
				Intent downloaderIntent = new Intent(mContext, DownloadService.class);
				downloaderIntent.putExtra("DeviceID", DeviceId);
				downloaderIntent.putExtra("CountTracks", 3);
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

}