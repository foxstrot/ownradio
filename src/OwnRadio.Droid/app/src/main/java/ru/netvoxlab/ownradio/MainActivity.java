package ru.netvoxlab.ownradio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
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
	Button btnPlayPause;
	Button btnNext;
	TrackDB trackDB;
	final String TAG = "ownRadio";
//	BroadcastReceiver receiver;
	private Handler handler = new Handler();
	ProgressBar progressBar;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarPlayback);
		IntentFilter filter = new IntentFilter("MY_ACTION");
		registerReceiver(myReceiver, filter);

//		receiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				String s = intent.getStringExtra(MediaPlayerService.PlaybackState);
//				if(s.equals("play"))
//					btnPlayPause.setBackgroundResource(R.drawable.btn_play);
//				if (s.equals("pause"))
//					btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
//			}
//		};

//		binder.GetMediaPlayerService().player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer mediaPlayer) {
//				Toast.makeText(MainActivity.this, "on complete", Toast.LENGTH_LONG).show();
//			}
//		});

		Intent iStatus = this.registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		Intent iStatus2 = this.registerReceiver(remoteControlReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

//		startService(new Intent(this, MediaPlayerService.class));

		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();

		trackDB = new TrackDB(getApplicationContext(), 1);
		sp = PreferenceManager.getDefaultSharedPreferences(this);

//         полная очистка настроек
//         sp.edit().clear().commit();

		TextView textInfo = (TextView) findViewById(R.id.textViewInfo);
		textInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());

		final TextView textTrackID = (TextView) findViewById(R.id.trackID);


//		Button btnPlayPause = (Button) findViewById(R.id.btnPlayPause);
//		btnPlayPause.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
//				{
////					btnPlayPause.setActivated(true);
//					binder.GetMediaPlayerService().Pause();
//				}
//				else
//				{
////					btnPlayPause.setActivated(true);
//					binder.GetMediaPlayerService().Play();
//				}
//			}
//		});

//		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarPlayback);

		btnPlayPause = (Button) findViewById(R.id.btnPlayPause);
		btnPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				ProgressBar progressBarPlayback = (ProgressBar) findViewById(R.id.progressBarPlayback);
//				ObjectAnimator anim = ObjectAnimator.ofInt(progressBarPlayback, "progress", 0, 100);
//				anim.setDuration(15000);
//				anim.setInterpolator(new DecelerateInterpolator());
//				anim.start();
				if(binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
				{
					btnPlayPause.setBackgroundResource(R.drawable.btn_play);
					binder.GetMediaPlayerService().Pause();
				}
				else {
					btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
					binder.GetMediaPlayerService().Play();
				}
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);

			}
		});

		btnNext = (Button) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                if(binder.GetMediaPlayerService().player != null)
                    binder.GetMediaPlayerService().Next();
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
			}
		});


	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			if(progressBar == null)
				progressBar = (ProgressBar) findViewById(R.id.progressBarPlayback);

//			progressBar.setMax(10000);//binder.GetMediaPlayerService().GetDuration());
////			int ola = intent.getIntExtra("PROGRESS", 0);
//			progressBar.setProgress(binder.GetMediaPlayerService().GetPosition());
////			Log.d("dddReceiver", String.valueOf(ola));


			new Thread(new Runnable() {
					@Override
					public void run() {
						int duration = binder.GetMediaPlayerService().GetDuration();
						int currentPosition = 0;
						progressBar.setMax(duration);
						progressBar.setSecondaryProgress(duration);

						while (currentPosition < duration) {
							try {
								Thread.sleep(1000);
								currentPosition = binder.GetMediaPlayerService().GetPosition();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.getLocalizedMessage();
							}

							// Update the progress bar
							handler.post(new Runnable() {
								@Override
								public void run() {
									progressBar.setProgress(binder.GetMediaPlayerService().GetPosition());
								}
							});
						}
					}
				}).start();

		}
	};

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


		TrackDataAccess trackDataAccess = new TrackDataAccess(this);
		TrackToCache trackToCache = new TrackToCache(this);
		TextView txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
		txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
		TextView txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);
		txtMemoryUsed.setText("Cache size: " + trackToCache.FolderSize(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " MB.");
//		if (mediaPlayerServiceConnection != null) {
//			InitilizeMedia();
//			if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
//				btnPlayPause.setBackgroundResource(R.drawable.btn_play);
//			else
//				btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
//		}
	}
//
//	@Override
//	public void onStop() {
////		super.onStop();
//	}

	@Override
	public void onRestart(){

//		if (mediaPlayerServiceConnection != null) {
//			InitilizeMedia();
			if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
				btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
			else
				btnPlayPause.setBackgroundResource(R.drawable.btn_play);
//		}
		super.onRestart();
	}

	@Override
	public void onDestroy(){
		try {
			unregisterReceiver(headSetReceiver);
			unregisterReceiver(remoteControlReceiver);
		} catch (Exception ex) {
			Log.e(TAG, ex.getLocalizedMessage());
		}
		binder.GetMediaPlayerService().SaveLastPosition();
		binder.GetMediaPlayerService().StopNotification();
//				unbindService(mediaPlayerServiceConnection);
		stopService(new Intent(this, MediaPlayerService.class));
//                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
//                intent.setAction("ru.netvoxlab.ownradio.action.SAVE_CURRENT_POSITION");
//                startService(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
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

//			case R.id.action_settings:
//				startActivity(new Intent(this, SettingsActivity.class));
//				return true;

			case R.id.clear_cache:
				File dir = new File(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
				if (dir.isDirectory()) {
					String[] children = dir.list();
					for (int i = 0; i < children.length; i++) {
						new File(dir, children[i]).delete();
					}
				}
				TrackDataAccess trackDataAccess = new TrackDataAccess(this);
				trackDataAccess.CleanTrackTable();
				HistoryDataAccess historyDataAccess = new HistoryDataAccess(this);
				historyDataAccess.CleanHistoryTable();

				TrackToCache trackToCache = new TrackToCache(this);
				TextView txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
				txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
				TextView txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);
				txtMemoryUsed.setText("Cache size: " + trackToCache.FolderSize(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " MB.");
				break;

			case R.id.action_exit:
				try {
					unregisterReceiver(headSetReceiver);
					unregisterReceiver(remoteControlReceiver);
				} catch (Exception ex) {
					Log.e(TAG, ex.getLocalizedMessage());
				}
				binder.GetMediaPlayerService().SaveLastPosition();
				binder.GetMediaPlayerService().StopNotification();
//				unbindService(mediaPlayerServiceConnection);
                stopService(new Intent(this, MediaPlayerService.class));
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

