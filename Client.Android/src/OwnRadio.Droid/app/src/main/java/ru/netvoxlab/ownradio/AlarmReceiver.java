package ru.netvoxlab.ownradio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static ru.netvoxlab.ownradio.Constants.ACTION_CLOSE_APP;
import static ru.netvoxlab.ownradio.Constants.ACTION_EXIT_APP;
import static ru.netvoxlab.ownradio.Constants.ACTION_START_APP;
import static ru.netvoxlab.ownradio.Constants.ALARM_TIME;
import static ru.netvoxlab.ownradio.Constants.TAG;

public class AlarmReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		int dayWeek = Integer.valueOf(intent.getAction());
		
		//restartAlarm(context, dayWeek);
		Log.d(TAG, "AlarmReceiver action Start");
		Intent i = new Intent(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP //Включает экран, но не снимает блокировку
				| PowerManager.ON_AFTER_RELEASE, "wakeup");
		wl.acquire();
		context.startActivity(i);
		Toast.makeText(context, "Start app", Toast.LENGTH_SHORT).show();
		wl.release();
	}
	
	public void restartAlarm(Context context, Integer dayWeek) {
		Log.d(TAG, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " " + "restartAlarm");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String start = prefs.getString(ALARM_TIME, "8:00");
		
		stopAlarm(context, dayWeek);
		setTime(context.getApplicationContext(), start, dayWeek);
		Log.d(TAG, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " " + "AlarmReceiver start " + start);
	}
	
	public static void stopAlarm(Context context, Integer requestCode) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(getPendingIntent(context, requestCode));
	}
	
	
	public static void setTime(Context context, String time, int dayWeek) {
		
		Log.d(TAG, "TIMEALARM:" + time + ", dayWeek:" + dayWeek);
		
		Calendar alarmTime = Calendar.getInstance();
		alarmTime.set(Calendar.DAY_OF_WEEK, dayWeek);
		alarmTime.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(time));
		alarmTime.set(Calendar.MINUTE, TimePreference.getMinute(time));
		alarmTime.set(Calendar.SECOND, 0);
		alarmTime.set(Calendar.MILLISECOND, 0);
		
		
		if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
			alarmTime.add(Calendar.DAY_OF_WEEK, dayWeek);
		}
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		
		Log.d(TAG,"TIMEALARM: mslsec: " + alarmTime.getTimeInMillis());
		
		
		if (Build.VERSION.SDK_INT >= 23) {
			am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
					alarmTime.getTimeInMillis(), getPendingIntent(context, dayWeek));
		} else if (Build.VERSION.SDK_INT >= 19) {
			am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), getPendingIntent(context, dayWeek));
		} else {
			am.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), getPendingIntent(context, dayWeek));
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putString(ALARM_TIME, time);
		prefEditor.apply();

		Log.d(TAG, " alarmTime=" + alarmTime.getTime().toString() + " type=" + dayWeek);
	}
	
	private static PendingIntent getPendingIntent(Context context, Integer requestCode) {
		Intent i = new Intent(context, AlarmReceiver.class);
		
		i.setAction(String.valueOf(requestCode));
		
		i.putExtra("type", requestCode);
		return (PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_CANCEL_CURRENT));
	}
}