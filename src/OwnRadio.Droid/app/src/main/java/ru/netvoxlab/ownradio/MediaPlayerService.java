package ru.netvoxlab.ownradio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static android.app.PendingIntent.getActivity;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {
	public static final String ActionPlay = "ru.netvoxlab.ownradio.action.PLAY";
	public static final String ActionPause = "ru.netvoxlab.ownradio.action.PAUSE";
	public static final String ActionNext = "ru.netvoxlab.ownradio.action.NEXT";
	public static final String ActionStop = "ru.netvoxlab.ownradio.action.STOP";
	public static final String ActionTogglePlayback = "ru.netvoxlab.ownradio.action.TOGGLEPLAYBACK";
	public static final String ActionSaveCurrentPosition = "ru.netvoxlab.ownradio.action.SAVE_CURRENT_POSITION";
	public static final String ActionProgressBarUpdate = "ru.netvoxlab.ownradio.action.PROGRESSBAR_UPDATE";
	public static final String ActionButtonImgUpdate = "ru.netvoxlab.ownradio.action.BTN_PLAYPAUSE_IMG_UPDATE";
	public static final String ActionSendInfoTxt = "ru.netvoxlab.ownradio.action.SEND_INFO_TXT";
	public static final String ActionUpdateNotification = "ru.netvoxlab.ownradio.action.UPDATE_NOTIFICATION";
	private RemoteControlClient remoteControlClient;
	public MediaPlayer player = null;
	private AudioManager audioManager;
	private WifiManager wifiManager;
	private WifiManager.WifiLock wifiLock;
	private ComponentName remoteComponentName;
	private boolean paused;
	private int NotificationId = 1;

	private MediaSessionCompat mediaSessionCompat;
	public MediaControllerCompat mediaControllerCompat;

	private Handler handler = new Handler();
	private Handler PlayingHandler;
	private java.lang.Runnable PlayingHandlerRunnable;
	String trackURL;
	TrackDataAccess trackDataAccess;
	String DeviceID;
	String UserID;
	String TrackID = "";
	Boolean FlagDownloadTrack = true;
	final String TAG = "ownRadio";

	Map<String, String> trackMap;
	public static List<Map<String,String>> queue = new ArrayList<>();
	public static int queueSize = 0;
	ContentValues track;
	int startPosition = 0;
	String startTrackID = "";


	public int GetMediaPlayerState() {
		return (mediaControllerCompat.getPlaybackState() != null ? mediaControllerCompat.getPlaybackState().getState() : PlaybackStateCompat.STATE_NONE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		trackDataAccess = new TrackDataAccess(getApplicationContext());
		DeviceID = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");
		UserID = DeviceID;//PreferenceManager.getDefaultSharedPreferences(this).getString("UserID", "");
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		remoteComponentName = new ComponentName(getPackageName(), new RemoteControlReceiver().ComponentName());
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if (player == null)
					InitializePlayer();

				//проверить возобновление проигрывания при переключении фокуса
				if (!player.isPlaying() && GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
					player.start();
				}

				player.setVolume(1.0f, 1.0f);
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				Pause();
//				Stop();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				//We have lost focus for a short time, but likely to resume so pause
				Pause();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				//We have lost focus but should till play at a muted 10% volume
				if (player.isPlaying())
					player.setVolume(.1f, .1f);
				break;
		}
	}

	public MediaPlayerService() {
//        PlayingHandler = new Handler ();
//
//        // Create a runnable, restarting itself if the status still is "playing"
//        PlayingHandlerRunnable = new java.lang.Runnable() {
//            @Override
//            public void run() {
//                OnPlaying(EventArgs);
//
//                if (GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
//                    PlayingHandler.postDelayed(PlayingHandlerRunnable, 250);
//                }
//            }
//        };
//
//
//        // On Status changed to PLAYING, start raising the Playing event
//        StatusChanged += (object sender, EventArgs e) => {
//            if(MediaPlayerState == PlaybackStateCompat.StatePlaying){
//                PlayingHandler.PostDelayed (PlayingHandlerRunnable, 0);
//            }
//        };
	}

	private void InitMediaSession() {
		try {
			if (mediaSessionCompat == null) {
				Intent nIntent = new Intent(getApplicationContext(), MainActivity.class);
				PendingIntent pIntent = getActivity(getApplicationContext(), 0, nIntent, 0);

				remoteComponentName = new ComponentName(getApplication().getPackageName(), new RemoteControlReceiver().ComponentName());

				mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "StreamingAudio", remoteComponentName, pIntent);
				mediaControllerCompat = new MediaControllerCompat(getApplicationContext(), mediaSessionCompat.getSessionToken());
			}

			mediaSessionCompat.setActive(true);
			mediaSessionCompat.setCallback(new MediaSessionCallback((MediaPlayerServiceBinder) binder));
			mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
		} catch (Exception ex) {
		}
	}


	IBinder binder;

	@Override
	public IBinder onBind(Intent intent) {
		binder = new MediaPlayerServiceBinder(this);
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		StopNotification();
		stopForeground(true);
		ReleaseWifiLock();
		UnregisterMediaSessionCompat();
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
		trackDataAccess = new TrackDataAccess(getApplicationContext());
		DeviceID = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");
		UserID = DeviceID;//PreferenceManager.getDefaultSharedPreferences(this).getString("UserID", "");
//        if((flags&START_FLAG_RETRY)==0){
//        }
//        else{
//        }
		String action = intent.getAction();

		switch (action) {
			case ActionPlay:
				Play();
				break;
			case ActionPause:
				Pause();
				break;
			case ActionNext:
				Next();
				break;
			case ActionStop:
				Stop();
				break;
			case ActionTogglePlayback:
				if (player == null)
					return Service.START_REDELIVER_INTENT;

				if (player.isPlaying())
					Pause();
				else
					Play();
				break;
			case ActionUpdateNotification:
				StartNotification();
				break;
//            case ActionSaveCurrentPosition: SaveLastPosition();
//                android.os.Process.killProcess(android.os.Process.myPid());
//                break;
		}
		return Service.START_REDELIVER_INTENT;
	}

	private void InitializePlayer() {
		player = new MediaPlayer();

		//Tell our player to stream music
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//Wake mode will be partial to keep the CPU still running under lock screen
		player.setWakeMode(getApplicationContext(), 1);// WakeLockFlags.Partial=1
		player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

			}
		});

		player.setOnCompletionListener(this);
		player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
				switch(what){
					case MediaPlayer.MEDIA_ERROR_UNKNOWN:
						Log.e(TAG, "unknown media error what=["+what+"] extra=["+extra+"]");
						break;
					case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
						Log.e(TAG, "Streaming source Server died what=["+what+"] extra=["+extra+"]");
						break;
					default:
						Log.e(TAG, "Default Problems what=["+ what +"] extra=["+extra+"]");
				}
				return false;
			}
		});
		player.setOnPreparedListener(this);
	}

	public void onCompletion(MediaPlayer mediaPlayer) {
		//Событие возникает при дослушивании трека до конца, плеер останавливается, отправляется статистика прослушивания
		//затем вызвается функция Play(), запускающая проигрывание следующего трека
		//Сохраняем информацию о прослушивании в локальную БД.
		int listedTillTheEnd = 1;
		SaveHistory(listedTillTheEnd, track);

		PlayNext();

		//Сохраняем информацию о прослушивании в локальную БД.
		new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				try {
					//Отправка на сервер накопленной истории прослушивания треков
					new APICalls(getApplicationContext()).SendHistory(DeviceID, 3);
					return;
				}catch (Exception ex){
					Log.d(TAG, " " + ex.getLocalizedMessage());
					Thread.currentThread().interrupt();
					return;
				}
			}
		}).start();
	}

	public void Play() {
		Log.d(TAG, "Play(): ");

		if (player != null && GetMediaPlayerState() == PlaybackStateCompat.STATE_PAUSED) {
			int focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
//				//mess
//				return;
//			}
			Log.d(TAG, "Play(): start");

			player.start();
//			UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
//			StartNotification();
//			UpdateButtonPlayPauseImg();
			UpdateMediaMetadataCompat();
		}

		if (player == null)
			InitializePlayer();

		Log.d(TAG, "Play(): InitializePlayer");

		if (mediaSessionCompat == null)
			InitMediaSession();

		Log.d(TAG, "Play(): InitMediaSession");

		if (player.isPlaying()) {
			UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
			Log.d(TAG, "Play(): UpdatePlaybackState");

//			StartNotification();
//			UpdateButtonPlayPauseImg();
			return;
		}

		try {
//			MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

			if(trackDataAccess.GetExistTracksCount() >= 3) {
				Log.d(TAG, "Play(): cache");
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				startPosition = settings.getInt("LastPosition", 0);
				startTrackID = settings.getString("LastTrackID", "");
				track = trackDataAccess.GetMostOldTrackNEW();

				if (track != null) {
					FlagDownloadTrack = false;
					TrackID = track.getAsString("id");
					trackURL = track.getAsString("trackurl");
					if (!new File(trackURL).exists()) {
						trackDataAccess.DeleteTrackFromCache(track);
						Play();
					}
					player.setDataSource(getApplicationContext(), Uri.parse(trackURL));
//					metaRetriever.setDataSource(trackURL);
					Intent i = new Intent(ActionSendInfoTxt);
					i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " Play(): cache");
					getApplicationContext().sendBroadcast(i);
				}
			} else {
				Log.d(TAG, "Play(): stream");
				trackMap = new APICalls((getApplicationContext())).GetNextTrackID(DeviceID);
				if(trackMap == null)
					return;
				Log.d(TAG, "Play(): GetNextTrackID: " + trackMap);
				track = new ContentValues();
				track.put("id", trackMap.get("id"));
				track.put("title", trackMap.get("name"));
				track.put("artist", trackMap.get("artist"));
				track.put("methodid", trackMap.get("methodid"));
				track.put("length", trackMap.get("length"));

				TrackID = trackMap.get("id");


			String uri = "http://api.ownradio.ru/v3/tracks/" + TrackID;
			Log.d(TAG, "Play(): URI: " + uri);

			player.setDataSource(getApplicationContext(), Uri.parse(uri));
//					metaRetriever.setDataSource(uri);

				Intent i = new Intent(ActionSendInfoTxt);
				i.putExtra("TEXTINFO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + " Play(): stream");
				getApplicationContext().sendBroadcast(i);
			}

			int focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			}

			UpdatePlaybackState(PlaybackStateCompat.STATE_BUFFERING);
			Log.d(TAG, "Play(): UpdatePlaybackState" );

			player.prepareAsync();
			Log.d(TAG, "Play(): prepareAsync" );

			AcquireWifiLock();
			                UpdateMediaMetadataCompat ();
//			if (GetMediaPlayerState() != PlaybackStateCompat.STATE_BUFFERING) {
//				StartNotification();
//				UpdateButtonPlayPauseImg();
//			}

			Intent i = new Intent(ActionProgressBarUpdate);
			sendBroadcast(i);
			Log.d(TAG, "Play(): sendBroadcast" );
		} catch (Exception ex) {
			Log.d(TAG, "Error in Play(): " + ex.getLocalizedMessage());
			player.reset();
			player.release();
			player = null;
			UpdatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
			ex.printStackTrace();
			return;
		}
//        }

		if(queue.size()<5) {
//			queue.add(new APICalls(getApplicationContext()).GetNextTrackID(DeviceID));
//			queue.add(new APICalls(getApplicationContext()).GetNextTrackID(DeviceID));
//			queue.add(new APICalls(getApplicationContext()).GetNextTrackID(DeviceID));

//		Запускаем кеширование треков - 3 шт
			new Thread(new Runnable() {
				@Override
				public void run() {
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					try {
//						new GetTrackById(getApplicationContext()).GetTrack();
						TrackToCache trackToCache = new TrackToCache(getApplicationContext());
						trackToCache.SaveTrackToCache(DeviceID, 3);
						Thread.currentThread().interrupt();
						return;
					} catch (Exception ex) {
						Log.d(TAG, " " + ex.getLocalizedMessage());
						Thread.currentThread().interrupt();
						return;
					}
				}
			}).start();
		}
//			try {
//				if (thread.getState()== Thread.State.TERMINATED || thread.getState() == Thread.State.NEW) {
//					Log.d(TAG, "isAlive is false");
//					thread.run();
////					thread.start();
//				}
//			}catch (Exception ex){
//				Log.d(TAG, " " + ex.getLocalizedMessage());
//			}
		}
//	}

	Thread thread = new Thread(new Runnable() {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			try {
//						new GetTrackById(getApplicationContext()).GetTrack();
				TrackToCache trackToCache = new TrackToCache(getApplicationContext());
				trackToCache.SaveTrackToCache(DeviceID, 3);
				thread.interrupt();
				return;
			} catch (Exception ex) {
				Log.d(TAG, " " + ex.getLocalizedMessage());
				thread.interrupt();
				return;
			}
		}
	});

	public void Pause() {
		if (player == null)
			return;

		if (player.isPlaying())
			player.pause();
		UpdatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
//		UpdateButtonPlayPauseImg();
	}

	public void Stop() {
		if (player == null)
			return;

		SaveLastPosition();

		if (player.isPlaying()) {
			player.stop();
		}
//		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//		notificationManager.cancel( 1 );
//		Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
//		stopService( intent );

//		try{
//			BroadcastReceiver headSetReceiver = new MusicBroadcastReceiver();
//			BroadcastReceiver remoteControlReceiver = new RemoteControlReceiver();
//			unregisterReceiver(headSetReceiver);
//			unregisterReceiver(remoteControlReceiver);
//		}catch (Exception ex){
//			Log.d(TAG, " " + ex.getLocalizedMessage());
//		}

		try {
			UpdatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
//			UpdateButtonPlayPauseImg();
			player.reset();
//			StartNotification();
//			super.stopForeground(true);
			ReleaseWifiLock();
//			UnregisterMediaSessionCompat();
		}catch (Exception ex){
			Log.d(TAG, " " + ex.getLocalizedMessage());
		}
	}

	public void Next() {
		//Сохраняем информацию о прослушивании в локальную БД.
		int listedTillTheEnd = -1;
		SaveHistory(listedTillTheEnd, track);

		PlayNext();

		try {
//			apiCalls.SetStatusTrack(DeviceID, TrackID, ListedTillTheEnd, currentDateTime);

			new Thread(new Runnable() {
				@Override
				public void run() {
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					try {
						// Удаление пропущенного трека
						ContentValues trackForDel = trackDataAccess.GetPathById(TrackID);
						new TrackToCache(getApplicationContext()).DeleteTrackFromCache(trackForDel);
						//Отправка на сервер накопленной истории прослушивания треков
						new APICalls(getApplicationContext()).SendHistory(DeviceID, 3);
						Thread.currentThread().interrupt();
						return;
					}catch (Exception ex){
						Log.d(TAG, " " + ex.getLocalizedMessage());
						Thread.currentThread().interrupt();
						return;
					}
				}
			}).start();

		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}
	}

	private void SaveHistory(int listedTillTheEnd, ContentValues trackInstance){
		try {
			//история прослушивания
			//Добавление истории прослушивания в локальную БД
			if(trackInstance.getAsString("id") != null && !trackInstance.getAsString("id").isEmpty()) {

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//Time format UTC+0
				String currentDateTime = dateFormat.format(new Date()); // Find todays date
				ContentValues history = new ContentValues();
				history.put("trackid", trackInstance.getAsString("id"));
				history.put("userid", UserID);
				history.put("datetimelisten", currentDateTime);
				history.put("islisten", listedTillTheEnd);
				history.put("methodid", trackInstance.getAsInteger("methodid"));
				HistoryDataAccess historyDataAccess = new HistoryDataAccess(getApplicationContext());
				historyDataAccess.SaveHistoryRec(history);//сохраняет информацию в history для последующей отправки на сервер

				ContentValues track = new ContentValues();
				track.put("id", TrackID);
				track.put("datetimelastlisten", currentDateTime);
				track.put("islisten", listedTillTheEnd);
				trackDataAccess.SetStatusTrack(track);//сохраняет информацию в основную таблицу для локального использования
			}
		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}
	}

	private void AcquireWifiLock() {
		if (wifiLock == null) {
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "OwnRadioWiFiLock");
		}
		wifiLock.acquire();
	}

	private void ReleaseWifiLock() {
		if (wifiLock == null)
			return;

		wifiLock.release();
		wifiLock = null;
	}

	public void onPrepared(MediaPlayer mp) {
		//Mediaplayer is prepared start track playback
		if (startTrackID.equals(TrackID)) {
			player.seekTo(startPosition);
			startPosition = 0;
		}
		mp.start();
		UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
	}

	public int GetPosition() {
		if (player == null
				|| (GetMediaPlayerState() != PlaybackStateCompat.STATE_PLAYING
				&& GetMediaPlayerState() != PlaybackStateCompat.STATE_PAUSED))
			return -1;
		else
			return player.getCurrentPosition();
	}

	public int GetDuration() {
		if (player == null
				|| (GetMediaPlayerState() != PlaybackStateCompat.STATE_PLAYING
				&& GetMediaPlayerState() != PlaybackStateCompat.STATE_PAUSED))
			return 0;
		else
			return player.getDuration();
	}

	private void UpdatePlaybackState(int state) {
		if (mediaSessionCompat == null || player == null)
			return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//		UpdateButtonPlayPauseImg();


			try {
				PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
						.setActions(
								PlaybackStateCompat.ACTION_PAUSE |
										PlaybackStateCompat.ACTION_PLAY |
										PlaybackStateCompat.ACTION_PLAY_PAUSE |
										PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
										PlaybackStateCompat.ACTION_STOP
						)
						.setState(state, GetPosition(), 1.0f, SystemClock.elapsedRealtime());

//			if(state == PlaybackStateCompat.STATE_PLAYING)
//				getApplicationContext().
				mediaSessionCompat.setPlaybackState(stateBuilder.build());

				//Used for backwards compatibility
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

					if (mediaSessionCompat.getRemoteControlClient() != null && mediaSessionCompat.getRemoteControlClient().getClass().equals(RemoteControlClient.class)) {
						remoteControlClient = (RemoteControlClient) mediaSessionCompat.getRemoteControlClient();

						int flags =
// (int)PlaybackStateCompat.ACTION_PAUSE |
//							(int)PlaybackStateCompat.ACTION_PLAY |
//							(int)PlaybackStateCompat.ACTION_PLAY_PAUSE |
//							(int)PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
//							(int)PlaybackStateCompat.ACTION_STOP;
								RemoteControlClient.FLAG_KEY_MEDIA_PLAY
										| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
										| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
										| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
										| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
//					int flags2 = RemoteControlClient.PLAYSTATE_PLAYING
//							| RemoteControlClient.PLAYSTATE_PAUSED
//							| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
//							| RemoteControlClient.PLAYSTATE_SKIPPING_FORWARDS
//							| RemoteControlClient.PLAYSTATE_STOPPED;
						remoteControlClient.setTransportControlFlags(flags);
						remoteControlClient.setPlaybackState(state);
					}
				}

//            OnStatusChanged(EventArgs.Empty);

				if (state != PlaybackStateCompat.STATE_BUFFERING) {
					StartNotification();
					UpdateButtonPlayPauseImg();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.d(TAG, " " + ex.getLocalizedMessage());
			}
		}else {
			UpdateButtonPlayPauseImg();
		}
	}

	private void UpdateButtonPlayPauseImg(){
		Intent intent = new Intent(ActionButtonImgUpdate);
		sendBroadcast(intent);
	}

	private void StartNotification() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			//                MediaMetadataCompat currentTrack = mediaControllerCompat.getMediaMetadata();
			android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();
			style.setMediaSession(mediaSessionCompat.getSessionToken());

			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent pendingCancelIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MediaPlayerService.class), PendingIntent.FLAG_CANCEL_CURRENT);
			style.setShowCancelButton(true);
			style.setCancelButtonIntent(pendingCancelIntent);
			String trackTitle;
			String trackArtist;
			try {
				trackTitle = (track.getAsString("name") == null) ? "Unknown track" : track.getAsString("name");
				trackArtist = (track.getAsString("artist") == null) ? "Unknown artist" : track.getAsString("artist");

			} catch (Exception ex) {
				trackTitle = "Unknown track";
				trackArtist = "Unknown artist";
				Log.d(TAG, " " + ex.getLocalizedMessage());
			}

			Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
			intent.setAction(ActionPlay);
			RemoteViews contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);

			contentView.setViewVisibility(R.id.viewsIcon, View.VISIBLE);

			Intent mainIntent = new Intent(this, MainActivity.class);
			mainIntent.setAction(Intent.ACTION_MAIN);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			PendingIntent pmainIntent = PendingIntent.getActivity(getApplicationContext(), 0,
					mainIntent, 0);

			contentView.setTextViewText(R.id.viewsTitle, trackTitle);
			contentView.setTextViewText(R.id.viewsArtist, trackArtist);

			Intent playIntent = new Intent(this, MediaPlayerService.class);
			if (GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
				playIntent.setAction(ActionPause);
				contentView.setImageViewResource(R.id.viewsPlayPause, R.drawable.btn_pause_action);
			} else {
				playIntent.setAction(ActionPlay);
				contentView.setImageViewResource(R.id.viewsPlayPause, R.drawable.btn_play_action);
			}
			PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

			Intent nextIntent = new Intent(this, MediaPlayerService.class);
			nextIntent.setAction(ActionNext);
			PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);


			contentView.setOnClickPendingIntent(R.id.viewsNext, pnextIntent);
			contentView.setOnClickPendingIntent(R.id.viewsPlayPause, pplayIntent);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
					.setSmallIcon(R.drawable.logo)
					.setContentIntent(pendingIntent)
					.setCustomContentView(contentView)
					.setShowWhen(false)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

			if (GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
				builder.setOngoing(true);
			else
				builder.setOngoing(false);
//        style.setShowActionsInCompactView(0,1,2);
			NotificationManagerCompat.from(getApplicationContext()).notify(NotificationId, builder.build());

//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(2, notification);
//
//		if (Build.VERSION.SDK_INT >18 && Build.VERSION.SDK_INT < 21) {
////			 MediaMetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
//			if (mediaSessionCompat.getRemoteControlClient() != null){// && mediaSessionCompat.getRemoteControlClient().equals(RemoteControlClient.class)) {
//				RemoteControlClient remoteControlClient = (RemoteControlClient) mediaSessionCompat.getRemoteControlClient();
//				RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
//				String trackAlbum;
//				try{
//					metadataEditor.putString(METADATA_KEY_TITLE, track.getAsString("name"));
//					metadataEditor.putString(METADATA_KEY_ARTIST, track.getAsString("artist"));
//					metadataEditor.putString(METADATA_KEY_ALBUM, "ownRadio");
//				}catch (Exception ex){
//					metadataEditor.putString(METADATA_KEY_TITLE, "Unknown track");
//					metadataEditor.putString(METADATA_KEY_ARTIST, "Unknown artist");
//					metadataEditor.putString(METADATA_KEY_ALBUM, "ownRadio");
//				}
//
//				metadataEditor.apply();
////
////				metadataEditor.putString(METADATA_KEY_TITLE, track.getAsString("name"));
////				metadataEditor.putString(METADATA_KEY_ARTIST, track.getAsString("title"));
////				metadataEditor.putString(METADATA_KEY_ALBUM, "ownRadio");
////				int flags = RemoteControlClient.FLAG_KEY_MEDIA_PLAY
////						| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
////						| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
////						| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
////						| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
////				remoteControlClient.setPlaybackState().setTransportControlFlags(flags);
//			}
//		}
		}
	}


	public void StopNotification() {
		NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
		nm.cancelAll();
	}

	public void PlayNext() {
		if (player != null) {
			player.reset();
			player.release();
			player = null;
		}
		UpdatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
		Play();
	}

	private void UpdateMediaMetadataCompat ()
	{
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (mediaSessionCompat == null)
				return;

			MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

//		if (metaRetriever != null) {
			builder
					.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, GetDuration())//track.getAsLong("length"))
					.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "ownRadio")
					.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getAsString("artist"))
					.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getAsString("name"));
//			.putString (MediaMetadataCompat.METADATA_KEY_ALBUM, metaRetriever.extractMetadata (METADATA_KEY_ALBUM))
//					.putString (MediaMetadataCompat.METADATA_KEY_ARTIST, metaRetriever.extractMetadata (METADATA_KEY_ARTIST))
//					.putString (MediaMetadataCompat.METADATA_KEY_TITLE, metaRetriever.extractMetadata (METADATA_KEY_TITLE));
//		} else {
//			builder
//					.putString (MediaMetadataCompat.METADATA_KEY_ALBUM, mediaSessionCompat.get(METADATA_KEY_ALBUM)
//					.putString (MediaMetadataCompat.METADATA_KEY_ARTIST, mediaSessionCompat.Controller.Metadata.GetString (METADATA_KEY_ARTIST)
//					.putString (MediaMetadataCompat.METADATA_KEY_TITLE, mediaSessionCompat.Controller.Metadata.GetString (METADATA_KEY_TITLE));
//		}
//		track.getAsString("name"));
//					metadataEditor.putString(METADATA_KEY_ARTIST, track.getAsString("artist"));
//					metadataEditor.putString(METADATA_KEY_ALBUM, "ownRadio");
			mediaSessionCompat.setMetadata(builder.build());
//		}
	}

	private void RegisterRemoteClient()
	{
		try{

			if(remoteControlClient == null)
			{
				audioManager.registerMediaButtonEventReceiver(remoteComponentName);
				//Create a new pending intent that we want triggered by remote control client
				Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
				mediaButtonIntent.setComponent(remoteComponentName);
				// Create new pending intent for the intent
				PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
				// Create and register the remote control client
				remoteControlClient = new RemoteControlClient(mediaPendingIntent);
				audioManager.registerRemoteControlClient(remoteControlClient);
			}


			//add transport control flags we can to handle
			remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
						| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
						| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
						| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
						| RemoteControlClient.FLAG_KEY_MEDIA_STOP);


		}catch(Exception ex){
			Log.d(TAG, " " + ex.getLocalizedMessage());
		}
	}

	public class MediaSessionCallback extends MediaSessionCompat.Callback {

		private MediaPlayerServiceBinder mediaPlayerService;

		public MediaSessionCallback(MediaPlayerServiceBinder service) {
			mediaPlayerService = service;
		}

		@Override
		public void onPause() {
			mediaPlayerService.GetMediaPlayerService().Pause();
			super.onPause();
		}

		@Override
		public void onPlay() {
			mediaPlayerService.GetMediaPlayerService().Play();
			super.onPlay();
		}

		@Override
		public void onSkipToNext() {
			mediaPlayerService.GetMediaPlayerService().Next();
			super.onSkipToNext();
		}

		@Override
		public void onStop() {
			mediaPlayerService.GetMediaPlayerService().Stop();
			super.onStop();
		}

//		@Override
//		public boolean onMediaButtonEvent(Intent mediaButtonEvent){
//			final String intentAction = mediaButtonEvent.getAction();
//			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
////				if (PrefUtils.isHeadsetPause(getBaseContext())) {
////					Log.d(LOG_TAG, "Headset disconnected");
////					pause();
////				}
//			} else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
//				final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//				if (event == null) return super.onMediaButtonEvent(mediaButtonEvent);
//				final int keycode = event.getKeyCode();
//				final int action = event.getAction();
//				final long eventTime = event.getEventTime();
//				if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
//					switch (keycode) {
////						case KeyEvent.KEYCODE_HEADSETHOOK:
////							if (eventTime - mLastClickTime < DOUBLE_CLICK) {
////								playNext(mSongNumber);
////								mLastClickTime = 0;
////							} else {
////								if (isPlaying())
////									pause();
////								else resume();
////								mLastClickTime = eventTime;
////							}
////							break;
//						case KeyEvent.KEYCODE_HEADSETHOOK:
//						case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//							Play();
//							break;
//						case KeyEvent.KEYCODE_MEDIA_PLAY:
//							Play();
//							break;
//						case KeyEvent.KEYCODE_MEDIA_PAUSE:
//							Pause();
//							break;
//						case KeyEvent.KEYCODE_MEDIA_STOP:
//							Stop();
//							break;
//						case KeyEvent.KEYCODE_MEDIA_NEXT:
//							Next();
//							break;
//					}
//				}
//			}
//			return super.onMediaButtonEvent(mediaButtonEvent);
//		}
	}

	public class MediaPlayerServiceBinder extends Binder {
		private MediaPlayerService service;

		public MediaPlayerServiceBinder(MediaPlayerService service) {
			this.service = service;
		}

		public MediaPlayerService GetMediaPlayerService() {
			return service;
		}
	}

	private void UnregisterMediaSessionCompat() {
		try {
			if (mediaSessionCompat != null) {
				mediaSessionCompat.release();
				mediaSessionCompat = null;
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (player != null) {
			SaveLastPosition();

			player.release();
			player = null;

			StopNotification();
			stopForeground(true);
			ReleaseWifiLock();
			UnregisterMediaSessionCompat();
		}
	}

	public void SaveLastPosition() {
		if (player != null) {
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putInt("LastPosition", player.getCurrentPosition());
			editor.putString("LastTrackID", TrackID);
			editor.putBoolean("StatePlay", player.isPlaying());
			editor.commit();
		}
	}
}