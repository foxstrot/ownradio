package ru.netvoxlab.ownradio;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
	TextView textInfo;

	public static final String ActionProgressBarUpdate = "ru.netvoxlab.ownradio.action.PROGRESSBAR_UPDATE";
	public static final String ActionTrackInfoUpdate = "ru.netvoxlab.ownradio.action.TRACK_INFO_UPDATE";
	public static final String ActionButtonImgUpdate = "ru.netvoxlab.ownradio.action.BTN_PLAYPAUSE_IMG_UPDATE";
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ActionProgressBarUpdate);
		filter.addAction(ActionTrackInfoUpdate);
		filter.addAction(ActionButtonImgUpdate);
		filter.addAction(ActionSendInfoTxt);
		registerReceiver(myReceiver, filter);

		this.registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		this.registerReceiver(remoteControlReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();

		trackDB = new TrackDB(getApplicationContext(), 1);
		sp = PreferenceManager.getDefaultSharedPreferences(this);

//         полная очистка настроек
//         sp.edit().clear().commit();

		textInfo = (TextView) findViewById(R.id.textViewInfo);
		textInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());

		final TextView textTrackID = (TextView) findViewById(R.id.trackID);

		btnPlayPause = (Button) findViewById(R.id.btnPlayPause);
		btnPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
					binder.GetMediaPlayerService().Pause();
				else
					binder.GetMediaPlayerService().Play();
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

			if(intent.getAction() == ActionTrackInfoUpdate)
				SetTrackInfoText();

			if(intent.getAction() == ActionButtonImgUpdate){
				if(binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
					btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
				else
					btnPlayPause.setBackgroundResource(R.drawable.btn_play);
				TextView textTrackID = (TextView) findViewById(R.id.trackID);
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
			}

			if(intent.getAction() == ActionSendInfoTxt){
				textInfo.setText(intent.getStringExtra("TEXTINFO"));
			}

			if(intent.getAction() != ActionProgressBarUpdate)
				return;

			if (progressBar == null)
				progressBar = (ProgressBar) findViewById(R.id.progressBarPlayback);
				new Thread(new Runnable() {
						@Override
						public void run() {
							int duration = binder.GetMediaPlayerService().GetDuration();
							int currentPosition = 0;
							if(duration<=0)
								duration = 10000;

							progressBar.setMax(duration);
							progressBar.setSecondaryProgress(duration);

							while (currentPosition < duration) {
								try {
									Thread.sleep(1000);
									currentPosition = binder.GetMediaPlayerService().GetPosition();
									duration = binder.GetMediaPlayerService().GetDuration();
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.getLocalizedMessage();
								}

								// Update the progress bar
								handler.post(new Runnable() {
									@Override
									public void run() {
										int duration = binder.GetMediaPlayerService().GetDuration();
										progressBar.setMax(duration);
//										progressBar.setSecondaryProgress(duration);
										progressBar.setProgress(binder.GetMediaPlayerService().GetPosition());
									}
								});
							}
						}
					}).start();
		}
	};

	@Override
	public void onBackPressed(){
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

	private void InitilizeMedia() {
		mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
		mediaPlayerServiceConnection = new MediaPlayerServiceConnection(this);
		bindService(mediaPlayerServiceIntent, mediaPlayerServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();

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

		SetTrackInfoText();
	}

	public void SetTrackInfoText(){
		TrackDataAccess trackDataAccess = new TrackDataAccess(this);
		TrackToCache trackToCache = new TrackToCache(this);
		TextView txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
		txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
		TextView txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);
		txtMemoryUsed.setText("Cache size: " + trackToCache.FolderSize(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " MB.");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onDestroy(){
		try {
			unregisterReceiver(myReceiver);
		} catch (Exception ex) {
			Log.e(TAG, ex.getLocalizedMessage());
		}
		try {
			unregisterReceiver(headSetReceiver);
		} catch (Exception ex) {
			Log.e(TAG, ex.getLocalizedMessage());
		}
		try {
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
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Clear cache?")
                        .setMessage("All cached tracks will be removed.")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
										File dir = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
										if (dir.isDirectory()) {
											String[] children = dir.list();
											for (int j = 0; j < children.length; j++) {
												new File(dir, children[j]).delete();
											}
										}
										TrackDataAccess trackDataAccess = new TrackDataAccess(MainActivity.this);
										trackDataAccess.CleanTrackTable();
										HistoryDataAccess historyDataAccess = new HistoryDataAccess(MainActivity.this);
										historyDataAccess.CleanHistoryTable();

										TrackToCache trackToCache = new TrackToCache(MainActivity.this);
										TextView txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
										txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
										TextView txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);
										txtMemoryUsed.setText("Cache size: " + trackToCache.FolderSize(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " MB.");
										dialogInterface.cancel();
									}
                                })
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.cancel();
									}
								});
                AlertDialog alert = builder.create();
                alert.show();
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

