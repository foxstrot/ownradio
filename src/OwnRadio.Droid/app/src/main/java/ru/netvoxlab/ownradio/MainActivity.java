package ru.netvoxlab.ownradio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import static ru.netvoxlab.ownradio.MediaPlayerService.playbackWithHSisInterrupted;

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
	private Handler handler = new Handler();
	private Handler handlerEvent = new Handler();
	ProgressBar progressBar;
	TextView textInfo;

	TextView textVersionName;
	TextView textDeviceID;
	TextView textUserID;
	TextView textTrackID;
	TextView txtTrackCount;
	TextView txtTrackCountInFolder;
	TextView txtCountPlayTracks;

	TextView txtMemoryUsed;
	TextView txtTrackTitle;
	TextView txtTrackArtist;
	LinearLayout layoutDevelopersInfo;
	public static File filePath;
	boolean flagDevInfo = false;

	public final static String TAG = "ownRadio";

	public static final String ActionProgressBarUpdate = "ru.netvoxlab.ownradio.action.PROGRESSBAR_UPDATE";
	public static final String ActionProgressBarFirstTracksLoad = "ru.netvoxlab.ownradio.action.PROGRESSBAR_FIRST_UPDATE";
	public static final String ActionTrackInfoUpdate = "ru.netvoxlab.ownradio.action.TRACK_INFO_UPDATE";
	public static final String ActionButtonImgUpdate = "ru.netvoxlab.ownradio.action.BTN_PLAYPAUSE_IMG_UPDATE";
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";
	ProgressDialog dialog;
	int numberOfTaps = 0;
	long lastTapTimeMs = 0;
	long touchDownMs = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		filePath = new File(this.getFilesDir() + File.separator + "music");
		if(!filePath.exists())
			filePath.mkdirs();
		textUserID = (TextView) findViewById(R.id.userID);
		textVersionName = (TextView) findViewById(R.id.versionName);
		textDeviceID = (TextView) findViewById(R.id.deviceID);
		textUserID = (TextView) findViewById(R.id.userID);
		txtTrackCount = (TextView) findViewById(R.id.txtTrackCount);
		txtTrackCountInFolder = (TextView) findViewById(R.id.txtCountTrackInFolder);
		txtMemoryUsed = (TextView) findViewById(R.id.txtMemoryUsed);
		txtCountPlayTracks = (TextView) findViewById(R.id.txtCountPlayTracks);
		txtCountPlayTracks.setMovementMethod(new android.text.method.ScrollingMovementMethod());

		textInfo = (TextView) findViewById(R.id.textViewInfo);
		textInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());

		textTrackID = (TextView) findViewById(R.id.trackID);

		txtTrackTitle = (TextView) findViewById(R.id.trackTitle);
		txtTrackArtist = (TextView) findViewById(R.id.trackArtist);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ActionProgressBarUpdate);
		filter.addAction(ActionTrackInfoUpdate);
		filter.addAction(ActionButtonImgUpdate);
		filter.addAction(ActionSendInfoTxt);
		filter.addAction(ActionProgressBarFirstTracksLoad);
		registerReceiver(myReceiver, filter);

		this.registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		this.registerReceiver(remoteControlReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();

		trackDB = new TrackDB(MainActivity.this, 1);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			int currentVersion = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).versionCode;
			if(sp.getInt("lastVersion", -1) < 34){
				sp.edit().putInt("lastVersion", currentVersion).commit();
				FileUtils.cleanDirectory(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC));
				this.deleteDatabase("ownradiodb.db3");
			}
		}catch (Exception ex){
			new Utilites().SendInformationTxt(getApplicationContext(), " " + ex.getLocalizedMessage());
		}

//         полная очистка настроек
//         sp.edit().clear().commit();
		layoutDevelopersInfo = (LinearLayout) findViewById(R.id.linearLayoutDevelopersInfo);

		btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
		btnPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
//					btnPlayPause.setImageResource(R.drawable.btn_play);
					btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					binder.GetMediaPlayerService().Pause();
				} else {
//					btnPlayPause.setImageResource(R.drawable.btn_pause);
					btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					binder.GetMediaPlayerService().Play();
					playbackWithHSisInterrupted = false;
				}
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
			}
		});

		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() != PlaybackStateCompat.STATE_SKIPPING_TO_NEXT && binder.GetMediaPlayerService().GetMediaPlayerState() != PlaybackStateCompat.STATE_BUFFERING) {
					binder.GetMediaPlayerService().Next();
					textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
					Log.e(TAG, "Next");
				}
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
				handlerEvent.removeCallbacksAndMessages(null);
				if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
					//it was not a tap
					numberOfTaps = 0;
					lastTapTimeMs = 0;
					break;
				}

				if (numberOfTaps > 0
						&& (System.currentTimeMillis() - lastTapTimeMs) < 2*ViewConfiguration.getDoubleTapTimeout()) {
					numberOfTaps += 1;
				} else {
					numberOfTaps = 1;
				}

				lastTapTimeMs = System.currentTimeMillis();

				//3 тапа - отобразить/скрыть инфрмацию для разработчиков
				if (numberOfTaps == 3) {
					flagDevInfo = !flagDevInfo;
					SetDevelopersInfo();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}
		return true;
	}

	public void SetDevelopersInfo(){
		if(!flagDevInfo)
			layoutDevelopersInfo.setVisibility(View.GONE);
		else
			layoutDevelopersInfo.setVisibility(View.VISIBLE);
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {

			if (intent.getAction() == ActionTrackInfoUpdate)
				SetTrackInfoText();

			if (intent.getAction() == ActionButtonImgUpdate) {
				String title;
				String artist;
				try {
					if (binder == null) {
						txtTrackTitle.setText("");
						txtTrackArtist.setText("ownRadio");
						return;
					}

					if (binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_STOPPED) {
						btnPlayPause.setImageResource(R.drawable.btn_play);
						btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
						return;
					}

					if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
						btnPlayPause.setImageResource(R.drawable.btn_pause);
						btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					} else {
						btnPlayPause.setImageResource(R.drawable.btn_play);
						btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
					}

					title = binder.GetMediaPlayerService().track.getAsString("title");
					artist = binder.GetMediaPlayerService().track.getAsString("artist");
					if (title == null || title.isEmpty() || title.equals("null"))
						title = "Unknown track";
					if (artist == null || artist.isEmpty() || artist.equals("null"))
						artist = "Unknown artist";
					txtTrackTitle.setText(title);
					txtTrackArtist.setText(artist);
					textTrackID = (TextView) findViewById(R.id.trackID);
					textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
				} catch (Exception ex) {
					new Utilites().SendInformationTxt(MainActivity.this, "ActionButtonImgUpdate error:" + ex.getLocalizedMessage());
					title = "Unknown track";
					artist = "Unknown artist";
					txtTrackTitle.setText(title);
					txtTrackArtist.setText(artist);
				}
			}

			if (intent.getAction() == ActionSendInfoTxt) {
				textInfo.append("Info: " + intent.getStringExtra("TEXTINFO") + "\n");
			}

			if (intent.getAction() == ActionProgressBarUpdate) {

				if (progressBar == null)
					progressBar = (ProgressBar) findViewById(R.id.progressBarPlayback);
				new Thread(new Runnable() {
					@Override
					public void run() {
						int duration;
						try {
							duration = binder.GetMediaPlayerService().track.getAsInteger("length");
						} catch (Exception ex) {
							duration = binder.GetMediaPlayerService().GetDuration() / 1000;
						}
						int currentPosition = 0;
						if (duration <= 0)
							duration = 1000000;

						progressBar.setMax(duration);
						progressBar.setSecondaryProgress(0);

						while (currentPosition < duration) {
							try {
								Thread.sleep(1000);
								currentPosition = binder.GetMediaPlayerService().GetPosition() / 1000;
								try {
									duration = binder.GetMediaPlayerService().track.getAsInteger("length");
								} catch (Exception ex) {
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
										duration = binder.GetMediaPlayerService().track.getAsInteger("length");
									} catch (Exception ex) {
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

			if(intent.getAction() == ActionProgressBarFirstTracksLoad){
				if(intent.getBooleanExtra("ProgressOn", false)) {
					btnPlayPause.setClickable(false);
					btnNext.setClickable(false);
					if(dialog == null)
						dialog = new ProgressDialog(MainActivity.this);
					dialog.setTitle("Caching...");
					dialog.setMessage("There are no tracks to play. Wait until the tracks are cached and try again.");
					dialog.setIndeterminate(true);
					dialog.setCancelable(false);
					dialog.show();
				}else {
					if (dialog != null)
						dialog.dismiss();
					btnPlayPause.setClickable(true);
					btnNext.setClickable(true);
				}
			}
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
		TrackToCache trackToCache = new TrackToCache(MainActivity.this);

		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();

		txtTrackTitle.setOnTouchListener(this);
		txtTrackArtist.setOnTouchListener(this);
		try {
			if (trackToCache.FreeSpace() + trackToCache.FolderSize(filePath) < 104857600) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Not enough free space on your device")
						.setMessage("This application requires at least 100MB of free space in the internal memory of the device.")
						.setCancelable(false)
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				btnPlayPause.setClickable(false);
				btnNext.setClickable(false);
			}

			SetDevelopersInfo();

			try {
				String info = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).versionName;
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

			trackToCache.ScanTrackToCache();

			SetTrackInfoText();

			new Utilites().CheckCountTracksAndDownloadIfNotEnought(MainActivity.this, DeviceId);

			return;
		}catch (Exception ex) {
			Log.d(TAG, " " + ex.getLocalizedMessage());
			return;
		}


	}

	public void SetTrackInfoText(){
		TrackDataAccess trackDataAccess = new TrackDataAccess(this);
		TrackToCache trackToCache = new TrackToCache(this);
		txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
		txtTrackCountInFolder.setText("Count files in folder: " + trackToCache.TrackCountInFolder(filePath));
		txtCountPlayTracks.setText(trackDataAccess.GetCountPlayTracks());
		txtMemoryUsed.setText("Cache size: " + trackToCache.FolderSize(filePath) / 1048576 + " MB.");
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
//                builder.setName("Clear cache?")
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
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			instance.isBound = false;
		}
	}
}

