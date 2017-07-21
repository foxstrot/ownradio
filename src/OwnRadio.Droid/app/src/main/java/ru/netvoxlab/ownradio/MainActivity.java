package ru.netvoxlab.ownradio;

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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
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

import ru.netvoxlab.ownradio.receivers.NetworkStateReceiver;

import static ru.netvoxlab.ownradio.Constants.ALL_CONNECTION_TYPES;
import static ru.netvoxlab.ownradio.Constants.INTERNET_CONNECTION_TYPE;
import static ru.netvoxlab.ownradio.Constants.ONLY_WIFI;
import static ru.netvoxlab.ownradio.Constants.TAG;

public class MainActivity extends AppCompatActivity	implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener, NetworkStateReceiver.NetworkStateReceiverListener {
	
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
	ImageButton btnSkipTrack;
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
	
	SwitchCompat switchOnlyWIFI;
	
	Toolbar toolbar;
	
	LinearLayout layoutDevelopersInfo;
	public static File filePath;
	boolean flagDevInfo = false;
	
	
	
	public static final String ActionProgressBarUpdate = "ru.netvoxlab.ownradio.action.PROGRESSBAR_UPDATE";
	public static final String ActionProgressBarFirstTracksLoad = "ru.netvoxlab.ownradio.action.PROGRESSBAR_FIRST_UPDATE";
	public static final String ActionTrackInfoUpdate = "ru.netvoxlab.ownradio.action.TRACK_INFO_UPDATE";
	public static final String ActionButtonImgUpdate = "ru.netvoxlab.ownradio.action.BTN_PLAYPAUSE_IMG_UPDATE";
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";
	public static final String ActionStopPlayback = "ru.netvoxlab.ownradio.action.STOP_PLAYBACK";
	public static final String ActionCheckCountTracksAndDownloadIfNotEnought = "ru.netvoxlab.ownradio.action.CHECK_TRACKS_AND_DOWNLOAD";
	
	public static final String numListenedTracks = "NUM_TRACKS_LISTENED_IN_VERSION";
	public static final String version = "VERSION";
	ProgressDialog dialog;
	int numberOfTaps = 0;
	long lastTapTimeMs = 0;
	long touchDownMs = 0;
	PrefManager prefManager;
	private long lastClickTime = 0;
	
	private NetworkStateReceiver networkStateReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Меняем тему, используемую при запуске приложения, на основную
		setTheme(R.style.AppTheme);

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		prefManager = new PrefManager(getApplicationContext());
		
//		if (prefManager.isFirstTimeLaunch()) {
//			startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
//			finish();
//		}
		
		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
			@Override
			public void onDrawerStateChanged(int newState) {
				if (newState == DrawerLayout.STATE_SETTLING) {
					if (!drawer.isDrawerOpen(GravityCompat.START)) {
						String connectionType = prefManager.getPrefItem(INTERNET_CONNECTION_TYPE, ALL_CONNECTION_TYPES); //получаем настройки подключения
						if(connectionType.equals(ONLY_WIFI))
							switchOnlyWIFI.setChecked(true);
						else
							switchOnlyWIFI.setChecked(false);
					}
					invalidateOptionsMenu();
				}
			}
		};
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		navigationView.bringToFront();
		navigationView.requestLayout();
		try {
			//получаем настройки приложения по умолчанию
			PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		}catch (Exception ex){
			
		}
		Menu menu = navigationView.getMenu();

		
		switchOnlyWIFI= (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.app_bar_switch_only_wifi)).findViewById(R.id.switchWidget);
		switchOnlyWIFI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
				if (state)
					prefManager.setPrefItem(INTERNET_CONNECTION_TYPE, ONLY_WIFI);
				else
					prefManager.setPrefItem(INTERNET_CONNECTION_TYPE, ALL_CONNECTION_TYPES);
			}
		});
		//Z
		filePath = ((App)getApplicationContext()).getMusicDirectory();
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
		filter.addAction(ActionStopPlayback);
		filter.addAction(ActionCheckCountTracksAndDownloadIfNotEnought);
		registerReceiver(myReceiver, filter);
		
		this.registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		this.registerReceiver(remoteControlReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
		
		networkStateReceiver = new NetworkStateReceiver();
		networkStateReceiver.addListener(this);
		this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
		
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
			new Utilites().SendInformationTxt(getApplicationContext(), "Error by get app version " + ex.getLocalizedMessage());
		}

//         полная очистка настроек
//         sp.edit().clear().commit();
		layoutDevelopersInfo = (LinearLayout) findViewById(R.id.linearLayoutDevelopersInfo);
		
		btnPlayPause = (ImageButton) findViewById(R.id.btnPlayPause);
		btnPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
					binder.GetMediaPlayerService().Pause();
				} else {
					binder.GetMediaPlayerService().Play();
					MediaPlayerService.playbackWithHSisInterrupted = false;
				}
				textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
			}
		});
		
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
					Log.e(TAG, "Next return");
					return;
				}
				
				lastClickTime = SystemClock.elapsedRealtime();
				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() != PlaybackStateCompat.STATE_SKIPPING_TO_NEXT && binder.GetMediaPlayerService().GetMediaPlayerState() != PlaybackStateCompat.STATE_BUFFERING) {
					binder.GetMediaPlayerService().Next();
					textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
					Log.e(TAG, "Next");
				}
			}
		});
		
		btnSkipTrack = (ImageButton) findViewById(R.id.btnSkipTrack);
		btnSkipTrack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(binder.GetMediaPlayerService().player != null)
					binder.GetMediaPlayerService().onCompletion(binder.GetMediaPlayerService().player);
			}
		});
		
		Button btnTest = (Button) findViewById(R.id.btnTest);
		btnTest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new Utilites().SendLogs(getApplicationContext(), DeviceId);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
//			super.onBackPressed();
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
		}
	}
	
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

	//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//
//		//noinspection SimplifiableIfStatement
////		if (id == R.id.action_settings) {
////			Intent settingsActivity = new Intent(getBaseContext(),
////					SettingsActivity.class);
////			startActivity(settingsActivity);
////			return true;
////		}
//
//		return super.onOptionsItemSelected(item);
//	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();
//		PrefManager prefManager = new PrefManager(getApplicationContext());
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

		switch(id){
			case R.id.app_bar_switch_only_wifi:
				if(switchOnlyWIFI.isChecked())
					switchOnlyWIFI.setChecked(false);
				else
					switchOnlyWIFI.setChecked(true);
				break;
			case R.id.app_bar_write_to_developers:
				startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.me/ownradio")));
				break;
			case R.id.app_bar_rate_app:
				final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
				}
				break;
			case R.id.app_bar_settings:
				Intent settingsActivity = new Intent(getBaseContext(),
						SettingsActivity.class);
				settingsActivity.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
				settingsActivity.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
				startActivity(settingsActivity);
				drawer.closeDrawer(GravityCompat.START);
				break;
		}
		return true;
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
			if(intent.getAction() == ActionCheckCountTracksAndDownloadIfNotEnought) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
				alert.show();
			}
			
			if(intent.getAction() == ActionStopPlayback) {
				binder.GetMediaPlayerService().Stop();
				binder.GetMediaPlayerService().StopNotification();
			}
			
			if (intent.getAction() == ActionTrackInfoUpdate)
				SetTrackInfoText();
			
			if (intent.getAction() == ActionButtonImgUpdate) {
				String title;
				String artist;
				try {
					if (binder == null || binder.GetMediaPlayerService().player == null) {
						txtTrackTitle.setText("");
						txtTrackArtist.setText("");
						btnPlayPause.setImageResource(R.drawable.btn_play);
						return;
					}
					
					if (binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_STOPPED) {
						btnPlayPause.setImageResource(R.drawable.btn_play);
						return;
					}
					
					if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
						btnPlayPause.setImageResource(R.drawable.btn_pause);
					} else {
						btnPlayPause.setImageResource(R.drawable.btn_play);
					}
					
					if(binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().flagIsCorrect) {
						Log.d(TAG, "flagIsCorrect="+binder.GetMediaPlayerService().flagIsCorrect);
						title = binder.GetMediaPlayerService().track.getAsString("title");
						artist = binder.GetMediaPlayerService().track.getAsString("artist");
						if (title == null || title.isEmpty() || title.equals("null"))
							title = "Track";
						if (artist == null || artist.isEmpty() || artist.equals("null"))
							artist = "Artist";
						txtTrackTitle.setText(title);
						txtTrackArtist.setText(artist);
						textTrackID = (TextView) findViewById(R.id.trackID);
						textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
					}
				} catch (Exception ex) {
					new Utilites().SendInformationTxt(MainActivity.this, "ActionButtonImgUpdate error:" + ex.getLocalizedMessage());
					title = "Track";
					artist = "Artist";
					txtTrackTitle.setText(title);
					txtTrackArtist.setText(artist);
				}
			}
			
			if (intent.getAction() == ActionSendInfoTxt) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					textInfo.append(Html.fromHtml("<p><b>Info:</b> " + intent.getStringExtra("TEXTINFO") + "<br/></p>", Html.FROM_HTML_MODE_LEGACY));
				}else{
					textInfo.append(Html.fromHtml("<p><b>Info:</b> " + intent.getStringExtra("TEXTINFO") + "<br/></p>"));
				}
			}
			
			if (intent.getAction() == ActionProgressBarUpdate) {
				
				if (progressBar == null)
					progressBar = (ProgressBar) findViewById(R.id.progressBar);
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(binder.GetMediaPlayerService().player == null){
							progressBar.setMax(10000);
							progressBar.setProgress(0);
							return;
						}
						int duration = 0;
						try {
							duration = binder.GetMediaPlayerService().GetDuration();
//							duration = binder.GetMediaPlayerService().track.getAsInteger("length");
						} catch (Exception ex) {
							duration = binder.GetMediaPlayerService().track.getAsInteger("length") * 1000;

//							duration = binder.GetMediaPlayerService().GetDuration() / 1000;
						}
						int currentPosition = 0;
						if (duration > 0)
							progressBar.setMax(duration);
						progressBar.setSecondaryProgress(0);
						
						while (currentPosition < duration) {
							try {
								Thread.sleep(1000);
								currentPosition = binder.GetMediaPlayerService().GetPosition();
//								currentPosition = binder.GetMediaPlayerService().GetPosition() / 1000;
								try {
									duration = binder.GetMediaPlayerService().GetDuration();
//									duration = binder.GetMediaPlayerService().track.getAsInteger("length");
								} catch (Exception ex) {
									duration = binder.GetMediaPlayerService().track.getAsInteger("length") * 1000;
//									duration = binder.GetMediaPlayerService().GetDuration() / 1000;
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
										duration = binder.GetMediaPlayerService().GetDuration();
//										duration = binder.GetMediaPlayerService().track.getAsInteger("length");
									} catch (Exception ex) {
										duration = binder.GetMediaPlayerService().track.getAsInteger("length") * 1000;
//										duration = binder.GetMediaPlayerService().GetDuration() / 1000;
									}
									progressBar.setMax(duration);
									progressBar.setProgress(binder.GetMediaPlayerService().GetPosition());
//									progressBar.setProgress(binder.GetMediaPlayerService().GetPosition() / 1000);
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
					btnNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.ColorPrimaryBtnDisable));
					if(dialog == null)
						dialog = new ProgressDialog(MainActivity.this);
					dialog.setTitle(getResources().getString(R.string.is_caching));
					dialog.setMessage(getResources().getString(R.string.wait_caching));
					dialog.setIndeterminate(true);
					dialog.setCancelable(false);
					dialog.show();
				}else {
					if (dialog != null)
						dialog.dismiss();
					btnPlayPause.setClickable(true);
					
					if(new TrackDataAccess(getApplicationContext()).GetExistTracksCount()<1) {
						btnNext.setClickable(false);
						btnNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.ColorPrimaryBtnDisable));
					}else {
						btnNext.setClickable(true);
						btnNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.ColorPrimaryBtnPlay));
					}
				}
			}
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
		TrackToCache trackToCache = new TrackToCache(MainActivity.this);
		
		if (mediaPlayerServiceConnection == null)
			InitilizeMedia();
		
		txtTrackTitle.setOnTouchListener(this);
		txtTrackArtist.setOnTouchListener(this);
		toolbar.setOnTouchListener(this);
		try {
			//запускаем загрузку треков, если кеш пуст
			if(new TrackDataAccess(getApplicationContext()).GetExistTracksCount()<1) {
				if (trackToCache.FreeSpace() + trackToCache.FolderSize(filePath) < 104857600) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle(getResources().getString(R.string.not_enough_free_space))
							.setMessage(getResources().getString(R.string.requires_free_space))
							.setCancelable(false)
							.setNegativeButton(getResources().getString(R.string.button_ok),
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
					btnNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.ColorPrimaryBtnDisable));
				}
			}
			
			SetDevelopersInfo();
			
			try {
				String info = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).versionName;
				if(!info.equals(sp.getString("version", ""))){
					sp.edit().putString(version, info).commit();
					sp.edit().putString(numListenedTracks, "0").commit();
				}
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
					String DeviceName = Build.BRAND + " " + Build.PRODUCT;
					new APICalls(getApplicationContext()).RegisterDevice(DeviceId, DeviceName + " " + getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).versionName);
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
	
	
	@Override
	public void onResume(){
		super.onResume();
		networkStateReceiver.addListener(this);
		this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	public void onPause() {
		networkStateReceiver.removeListener(this);
		this.unregisterReceiver(networkStateReceiver);
		super.onPause();
	}
	
	public void SetTrackInfoText(){
		TrackDataAccess trackDataAccess = new TrackDataAccess(this);
		TrackToCache trackToCache = new TrackToCache(this);
		txtTrackCount.setText("Track count: " + trackDataAccess.GetExistTracksCount() + ".");
		txtTrackCountInFolder.setText("Count files in folder: " + trackToCache.TrackCountInFolder(filePath));
		txtCountPlayTracks.setText(trackDataAccess.GetCountPlayTracksTable());
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
	
	@Override
	public void networkAvailable() {
		new Utilites().SendInformationTxt(getApplicationContext(), "Интернет подключен");
		//Если треков в кеше мало - при подключении  интернета запускаем запуск треков
//		if(new TrackDataAccess(getApplicationContext()).GetExistTracksCount() < 3) {
//			//		Запускаем кеширование треков - 3 шт
//			Intent downloaderIntent = new Intent(getApplicationContext(), LongRequestAPIService.class);
//			downloaderIntent.setAction(ACTION_GETNEXTTRACK);
//			downloaderIntent.putExtra(EXTRA_DEVICEID, DeviceId);
//			downloaderIntent.putExtra(EXTRA_COUNT, 1);
//			startService(downloaderIntent);
//		}
    /* TODO: Your connection-oriented stuff here */
	}
	
	@Override
	public void networkUnavailable() {
		new Utilites().SendInformationTxt(getApplicationContext(), "Интернет отключен");
    /* TODO: Your disconnection-oriented stuff here */
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
