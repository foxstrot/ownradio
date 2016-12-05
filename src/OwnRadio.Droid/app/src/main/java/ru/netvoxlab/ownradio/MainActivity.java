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
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

	private APICalls apiCalls;

	String DeviceId;
	String UserId;
	SharedPreferences sp;
	BroadcastReceiver headSetReceiver = new MusicBroadcastReceiver();
	BroadcastReceiver remoteControlReceiver = new RemoteControlReceiver();
	boolean isBound = false;
	private MediaPlayerService.MediaPlayerServiceBinder binder;
	MediaPlayerServiceConnection mediaPlayerServiceConnection;
	private Intent mediaPlayerServiceIntent;
	ImageButton btnPlayPause;
	ImageButton btnNext;
	TrackDB trackDB;
	final String TAG = "ownRadio";
	private Handler handler = new Handler();
	ProgressBar progressBar;
	TextView textInfo;

	TextView textVersionName;
	TextView textDeviceID;
	TextView textUserID;
	TextView textTrackID;
	TextView txtTrackCount;
	TextView txtMemoryUsed;
	TextView txtTrackTitle;
	TextView txtTrackArtist;


	public static final String ActionProgressBarUpdate = "ru.netvoxlab.ownradio.action.PROGRESSBAR_UPDATE";
	public static final String ActionTrackInfoUpdate = "ru.netvoxlab.ownradio.action.TRACK_INFO_UPDATE";
	public static final String ActionButtonImgUpdate = "ru.netvoxlab.ownradio.action.BTN_PLAYPAUSE_IMG_UPDATE";
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";

	int numberOfTaps = 0;
	long lastTapTimeMs = 0;
	long touchDownMs = 0;
//	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

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

		textTrackID = (TextView) findViewById(R.id.trackID);

		txtTrackTitle = (TextView) findViewById(R.id.trackTitle);
		txtTrackArtist = (TextView) findViewById(R.id.trackArtist);

		btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
		btnPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
					btnPlayPause.setImageResource(R.drawable.btn_play);
					btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					binder.GetMediaPlayerService().Pause();
				} else {
					btnPlayPause.setImageResource(R.drawable.btn_pause);
					btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					binder.GetMediaPlayerService().Play();
				}
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);

			}
		});

		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				if (binder.GetMediaPlayerService().player != null)
					binder.GetMediaPlayerService().Next();
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: // нажатие
				touchDownMs = System.currentTimeMillis();
				break;
			case MotionEvent.ACTION_MOVE: // движение
				break;
			case MotionEvent.ACTION_UP: // отпускание
				handler.removeCallbacksAndMessages(null);
				if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
					//it was not a tap
					numberOfTaps = 0;
					lastTapTimeMs = 0;
					break;
				}

				if (numberOfTaps > 0
						&& (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
					numberOfTaps += 1;
				} else {
					numberOfTaps = 1;
				}

				lastTapTimeMs = System.currentTimeMillis();

				//5 тапов - отобразить инфрмацию для разработчиков
				if (numberOfTaps == 5) {
					sp.edit().putBoolean("DevelopersInfo", true).commit();
					SetDevelopersInfo();
//					Toast.makeText(getApplicationContext(), "five", Toast.LENGTH_SHORT).show();
					//handle triple tap
				} else if (numberOfTaps == 2) { //2 тапав - скрыть инфрмацию для разработчиков
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							sp.edit().putBoolean("DevelopersInfo", false).commit();
							SetDevelopersInfo();
							//handle double tap
//							Toast.makeText(getApplicationContext(), "double", Toast.LENGTH_SHORT).show();
						}
					}, ViewConfiguration.getDoubleTapTimeout());
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}
		return true;
	}

	public void SetDevelopersInfo(){
		if(!sp.getBoolean("DevelopersInfo",false)) {
			textVersionName.setVisibility(View.GONE);
			textDeviceID.setVisibility(View.GONE);
			textUserID.setVisibility(View.GONE);
			textTrackID.setVisibility(View.GONE);
			txtTrackCount.setVisibility(View.GONE);
			txtMemoryUsed.setVisibility(View.GONE);
			textInfo.setVisibility(View.GONE);
		}
		else {
			textVersionName.setVisibility(View.VISIBLE);
			textDeviceID.setVisibility(View.VISIBLE);
			textUserID.setVisibility(View.VISIBLE);
			textTrackID.setVisibility(View.VISIBLE);
			txtTrackCount.setVisibility(View.VISIBLE);
			txtMemoryUsed.setVisibility(View.VISIBLE);
			textInfo.setVisibility(View.VISIBLE);
		}
//		Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
//		intent.setAction("ru.netvoxlab.ownradio.action.UPDATE_NOTIFICATION");
//		startService(intent);
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){

			if(intent.getAction() == ActionTrackInfoUpdate)
				SetTrackInfoText();

			if(intent.getAction() == ActionButtonImgUpdate){
				try {
					if (binder == null)
						return;

//					if (binder.GetMediaPlayerService().player == null)
					if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
						btnPlayPause.setImageResource(R.drawable.btn_pause);
						btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					}
					else{
						btnPlayPause.setImageResource(R.drawable.btn_play);
						btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					}

					txtTrackTitle.setText(binder.GetMediaPlayerService().trackJSON.getString("name"));
					txtTrackArtist.setText(binder.GetMediaPlayerService().trackJSON.getString("artist"));
					textTrackID = (TextView) findViewById(R.id.trackID);
					textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
				}catch (Exception ex){
					Log.d(TAG, "ActionButtonImgUpdate error:" + ex.getLocalizedMessage());
				}
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
							int duration;
							try {
								duration = binder.GetMediaPlayerService().trackJSON.getInt("length");
							}catch (Exception ex){
								duration = binder.GetMediaPlayerService().GetDuration() / 1000;
							}
							int currentPosition = 0;
							if(duration<=0)
								duration = 1000000;

							progressBar.setMax(duration);

							while (currentPosition < duration) {
								try {
									Thread.sleep(1000);
									currentPosition = binder.GetMediaPlayerService().GetPosition() / 1000;
									try {
										duration = binder.GetMediaPlayerService().trackJSON.getInt("length");
									}catch (Exception ex){
										duration = binder.GetMediaPlayerService().GetDuration() / 1000;
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.getLocalizedMessage();
								}

								// Update the progress bar
								handler.post(new Runnable() {
									@Override
									public void run() {
										int duration;
										try {
											duration = binder.GetMediaPlayerService().trackJSON.getInt("length");
										}catch (Exception ex){
											duration = binder.GetMediaPlayerService().GetDuration() / 1000;
										}
										progressBar.setMax(duration);
										progressBar.setProgress(binder.GetMediaPlayerService().GetPosition() / 1000);
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

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
////		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
////			createHorizontalalLayout();
////		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
////			createVerticalLayout();
////		}
//	}

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

		textUserID = (TextView) findViewById(R.id.userID);
		textVersionName = (TextView) findViewById(R.id.versionName);
		textDeviceID = (TextView) findViewById(R.id.deviceID);
		textUserID = (TextView) findViewById(R.id.userID);
		txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
		txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);

		txtTrackTitle.setOnTouchListener(this);

//		sp.edit().putBoolean("DevelopersInfo",false).commit();
		SetDevelopersInfo();

		try {
			String info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionName;
			textVersionName.setText("Version name: " + info);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		apiCalls = new APICalls(MainActivity.this);
		try {
			DeviceId = sp.getString("DeviceID", "");
			if (DeviceId.isEmpty()) {
				DeviceId = UUID.randomUUID().toString();
				String UserName = "NewUser";
				String DeviceName = Build.BRAND;
//				APICalls.RegisterDevice(DeviceId, UserName, DeviceName);
				UserId = apiCalls.GetUserId(DeviceId);
				sp.edit().putString("DeviceID", DeviceId).commit();
				sp.edit().putString("UserID", UserId);
				sp.edit().putString("UserName", UserName);
				sp.edit().putString("DeviceName", DeviceName);
				sp.edit().commit();
			} else {
				UserId = sp.getString("UserID", "");
				if (UserId.isEmpty()) {
					UserId = apiCalls.GetUserId(DeviceId);
					sp.edit().putString("UserID", UserId).commit();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ex.getLocalizedMessage();
		}
		textDeviceID.setText("Device ID: " + DeviceId);
		textUserID.setText("User ID: " + UserId);


//		textUserID.setText("ownRadio");
		SetTrackInfoText();
	}

	public void SetTrackInfoText(){
		TrackDataAccess trackDataAccess = new TrackDataAccess(this);
		TrackToCache trackToCache = new TrackToCache(this);
		txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
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
			Log.e(TAG, " " + ex.getLocalizedMessage());
		}
		try {
			unregisterReceiver(headSetReceiver);
		} catch (Exception ex) {
			Log.e(TAG, " " + ex.getLocalizedMessage());
		}
		try {
			unregisterReceiver(remoteControlReceiver);
		} catch (Exception ex) {
			Log.e(TAG, " " + ex.getLocalizedMessage());
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

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case android.R.id.home:
//				NavUtils.navigateUpFromSameTask(this);
//				return true;
//
////			case R.id.action_settings:
////				startActivity(new Intent(this, SettingsActivity.class));
////				return true;
//
//			case R.id.clear_cache:
//				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("Clear cache?")
//                        .setMessage("All cached tracks will be removed.")
//                        .setCancelable(false)
//                        .setPositiveButton("OK",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//										File dir = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
//										if (dir.isDirectory()) {
//											String[] children = dir.list();
//											for (int j = 0; j < children.length; j++) {
//												new File(dir, children[j]).delete();
//											}
//										}
//										TrackDataAccess trackDataAccess = new TrackDataAccess(MainActivity.this);
//										trackDataAccess.CleanTrackTable();
//										HistoryDataAccess historyDataAccess = new HistoryDataAccess(MainActivity.this);
//										historyDataAccess.CleanHistoryTable();
//
//										TrackToCache trackToCache = new TrackToCache(MainActivity.this);
//										TextView txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
//										txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
//										TextView txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);
//										txtMemoryUsed.setText("Cache size: " + trackToCache.FolderSize(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " MB.");
//										dialogInterface.cancel();
//									}
//                                })
//						.setNegativeButton("Cancel",
//								new DialogInterface.OnClickListener(){
//									@Override
//									public void onClick(DialogInterface dialogInterface, int i) {
//										dialogInterface.cancel();
//									}
//								});
//                AlertDialog alert = builder.create();
//                alert.show();
//				break;
//
//			case R.id.action_exit:
//				try {
//					unregisterReceiver(headSetReceiver);
//					unregisterReceiver(remoteControlReceiver);
//				} catch (Exception ex) {
//					Log.e(TAG, " " + ex.getLocalizedMessage());
//				}
//				binder.GetMediaPlayerService().SaveLastPosition();
//				binder.GetMediaPlayerService().StopNotification();
////				unbindService(mediaPlayerServiceConnection);
//                stopService(new Intent(this, MediaPlayerService.class));
////                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
////                intent.setAction("ru.netvoxlab.ownradio.action.SAVE_CURRENT_POSITION");
////                startService(intent);
//				android.os.Process.killProcess(android.os.Process.myPid());
////                System.exit(0);
//				return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}


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

