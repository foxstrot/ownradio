package ru.netvoxlab.ownradio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

	private ExecuteProcedurePostgreSQL executeProcedurePostgreSQL;

	String DeviceId;
	String UserId;
	SharedPreferences sp;
	BroadcastReceiver headSetReceiver = new MusicBroadcastReceiver();
	BroadcastReceiver remoteControlReceiver = new RemoteControlReceiver();
	boolean isBound = false;
	private MediaPlayerService.MediaPlayerServiceBinder binder;
	MediaPlayerServiceConnection mediaPlayerServiceConnection;
	private Intent mediaPlayerServiceIntent;

	TrackDB trackDB;
	final String TAG = "ownRadio";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent iStatus = this.registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		Intent iStatus2 = this.registerReceiver(remoteControlReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();
		// Приложение запущено впервые или восстановлено из памяти?
		if (savedInstanceState == null)   // приложение запущено впервые
		{
		} else // приложение восстановлено из памяти
		{
		}

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

		trackDB = new TrackDB(getApplicationContext(), 1);
		sp = PreferenceManager.getDefaultSharedPreferences(this);

//         полная очистка настроек
//         sp.edit().clear().commit();


		TextView textInfo = (TextView) findViewById(R.id.textViewInfo);
		textInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());

		ImageButton btnPlay = (ImageButton) findViewById(R.id.imgBtnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Press Play");
				Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
				intent.setAction("ru.netvoxlab.ownradio.action.PLAY");
				startService(intent);
//                if(binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
//                    binder.GetMediaPlayerService().Pause();
//                else
//                    binder.GetMediaPlayerService().Play();
			}
		});

		ImageButton btnPause = (ImageButton) findViewById(R.id.imgBtnPause);
		btnPause.setOnClickListener((new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Press Pause");
				Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
				intent.setAction("ru.netvoxlab.ownradio.action.PAUSE");
				startService(intent);
			}
		}));

		ImageButton btnNext = (ImageButton) findViewById(R.id.imgBtnNext);
		btnNext.setOnClickListener((new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Press Next");
				Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
				intent.setAction("ru.netvoxlab.ownradio.action.NEXT");
				startService(intent);
//                if(binder.GetMediaPlayerService().player != null)
//                    binder.GetMediaPlayerService().PlayNext();
			}
		}));
	}

	private void InitilizeMedia() {
		mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
		mediaPlayerServiceConnection = new MediaPlayerServiceConnection(this);
		bindService(mediaPlayerServiceIntent, mediaPlayerServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	public void onStart() {
		super.onStart();

		TextView textVersionName = (TextView) findViewById(R.id.versionName);
		TextView textDeviceID = (TextView) findViewById(R.id.deviceID);
		TextView textUserID = (TextView) findViewById(R.id.userID);

		try {
			String info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionName;
			textVersionName.setText("Version name: " + info);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(MainActivity.this);
		try {
			DeviceId = sp.getString("DeviceID", "");
			if (DeviceId.isEmpty()) {
				DeviceId = UUID.randomUUID().toString();
				String UserName = "NewUser";
				String DeviceName = Build.BRAND;
				executeProcedurePostgreSQL.RegisterDevice(DeviceId, UserName, DeviceName);
				UserId = executeProcedurePostgreSQL.GetUserId(DeviceId);
				sp.edit().putString("DeviceID", DeviceId).commit();
				sp.edit().putString("UserID", UserId);
				sp.edit().putString("UserName", UserName);
				sp.edit().putString("DeviceName", DeviceName);
				sp.edit().commit();
			} else {
				UserId = sp.getString("UserID", "");
				if (UserId.isEmpty()) {
					UserId = executeProcedurePostgreSQL.GetUserId(DeviceId);
					sp.edit().putString("UserID", UserId).commit();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ex.getLocalizedMessage();
		}
		textDeviceID.setText("Device ID: " + DeviceId);
		textUserID.setText("User ID: " + UserId);
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;

			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;

			case R.id.action_exit:
				try {
					unregisterReceiver(headSetReceiver);
					unregisterReceiver(remoteControlReceiver);
				} catch (Exception ex) {
					Log.e(TAG, ex.getLocalizedMessage());
				}
//                stopService(new Intent(this, MediaPlayerService.class));
//                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
//                intent.setAction("ru.netvoxlab.ownradio.action.SAVE_CURRENT_POSITION");
//                startService(intent);
				android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(0);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public void LogToTextView() {
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(TAG))
					log.append(line + "\n");
			}
			TextView textInfo = (TextView) findViewById(R.id.textViewInfo);
			textInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());
			textInfo.setText(log.toString());
		} catch (IOException e) {
		}
	}

	class MediaPlayerServiceConnection extends java.lang.Object implements ServiceConnection {
		MainActivity instance;

		public MediaPlayerServiceConnection(MainActivity player) {
			this.instance = player;
		}

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder = (MediaPlayerService.MediaPlayerServiceBinder) service;
			if (mediaPlayerServiceBinder != null) {
				MediaPlayerService.MediaPlayerServiceBinder binder = (MediaPlayerService.MediaPlayerServiceBinder) service;
				instance.binder = binder;
				instance.isBound = true;

//                binder.GetMediaPlayerService().CoverReloaded += (object sender, EventArgs e) => { if (instance.CoverReloaded != null) instance.CoverReloaded(sender, e); };
//                binder.GetMediaPlayerService().StatusChanged += (object sender, EventArgs e) => { if (instance.StatusChanged != null) instance.StatusChanged(sender, e); };
//                binder.GetMediaPlayerService().Playing += (object sender, EventArgs e) => { if (instance.Playing != null) instance.Playing(sender, e); };
//                binder.GetMediaPlayerService().Buffering += (object sender, EventArgs e) => { if (instance.Buffering != null) instance.Buffering(sender, e); };
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			instance.isBound = false;
		}
	}
}

