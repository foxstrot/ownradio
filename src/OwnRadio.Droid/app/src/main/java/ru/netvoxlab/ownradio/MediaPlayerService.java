package ru.netvoxlab.ownradio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
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
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

	JSONObject trackJSON;

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

//	private class TrackInfo {
//		public String Title;
//		public String Artist;
//		public String AlbumArtist;
//		public String Album;
//		public String Duration;
//
//		public TrackInfo() {
//		}
//
//		public TrackInfo(String title, String artist, String albumArtist, String album, String duration) {
//			Title = title;
//			Artist = artist;
//			AlbumArtist = albumArtist;
//			Album = album;
//			Duration = duration;
//		}
//	};
//
//	TrackInfo trackInfo = new TrackInfo();

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
				PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, 0);

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
					return Service.START_STICKY;

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
		return Service.START_STICKY;
	}

	private void InitializePlayer() {
		player = new MediaPlayer();

		//Tell our player to stream music
//		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//Wake mode will be partial to keep the CPU still running under lock screen
		player.setWakeMode(getApplicationContext(), 1);// WakeLockFlags.Partial=1
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int listedTillTheEnd = 1;
					SaveHistory(listedTillTheEnd, track);
					Thread.currentThread().interrupt();
					return;
				}catch (Exception ex){
					Log.d(TAG, " " + ex.getLocalizedMessage());
					Thread.currentThread().interrupt();
					return;
				}
			}
		}).start();
//		SaveHistory(listedTillTheEnd);

		PlayNext();

		try {
			//Отправка на сервер накопленной истории прослушивания треков
//			APICalls apiCalls = new APICalls(getApplicationContext());
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						new APICalls(getApplicationContext()).SendHistory(DeviceID, 1);
						Thread.currentThread().interrupt();
						return;
					}catch (Exception ex){
						Log.d(TAG, "Error in history send" + ex.getLocalizedMessage());
						Thread.currentThread().interrupt();
						return;
					}
				}
			}).start();
		} catch (Exception ex) {
			Log.d(TAG, " " + ex.getLocalizedMessage());
		}
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

			if(trackDataAccess.GetExistTracksCount() >= 10) {
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
				}
			} else {
			Log.d(TAG, "Play(): stream");
//			String trackIdTmp = new APICalls(getApplicationContext()).GetNextTrackID(DeviceID);
			trackJSON = new APICalls((getApplicationContext())).GetNextTrackID(DeviceID);
			if(trackJSON == null)
				return;
			Log.d(TAG, "Play(): GetNextTrackID: " + trackJSON);
				track = new ContentValues();
				track.put("id", trackJSON.getString("id"));
				track.put("name", trackJSON.getString("name"));
				track.put("methodid", trackJSON.getString("methodid"));
				track.put("length", trackJSON.getString("length"));
//			String artist = trackJSON.getString("artist");
			TrackID = trackJSON.getString("id");
//			String title = trackJSON.getString("name");
//			String methodid = trackJSON.getString("methodid");
//			long length = trackJSON.getLong("length");




			String uri = "http://api.ownradio.ru/v3/tracks/" + TrackID;
//			String uri = "http://java.ownradio.ru/api/v2/tracks/" + TrackID;

			Log.d(TAG, "Play(): URI: " + uri);

			player.setDataSource(getApplicationContext(), Uri.parse(uri));
//					metaRetriever.setDataSource(uri);
			}

			int focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				//                could not get audio focus
			}

			UpdatePlaybackState(PlaybackStateCompat.STATE_BUFFERING);
			Log.d(TAG, "Play(): UpdatePlaybackState" );

			player.prepareAsync();
			Log.d(TAG, "Play(): prepareAsync" );

			AcquireWifiLock();
			//                UpdateMediaMetadataCompat (metaRetriever);
//			if (GetMediaPlayerState() != PlaybackStateCompat.STATE_BUFFERING) {
//				StartNotification();
//				UpdateButtonPlayPauseImg();
//			}

			Intent i = new Intent(ActionProgressBarUpdate);
			sendBroadcast(i);
			Log.d(TAG, "Play(): sendBroadcast" );


//				TrackToCache trackToCache = new TrackToCache(getApplicationContext());
//				trackToCache.SaveTrackToCache(DeviceID, 3);
//			} else {
//				if (FlagDownloadTrack) {
//					startPosition = 0;
//					FlagDownloadTrack = false;
//					TrackToCache trackToCache = new TrackToCache(getApplicationContext());
//					trackToCache.SaveTrackToCache(DeviceID, 3);
//					Play();
////                Log.d("MP", "DB is empty");
////                return;
//				}
//			}
		} catch (Exception ex) {
			Log.d(TAG, "Error in Play(): " + ex.getLocalizedMessage());
			ex.printStackTrace();
			UpdatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
			player.reset();
			player.release();
			player = null;
			return;
		}
//        }
	}

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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//Сохраняем информацию о прослушивании в локальную БД.
					int listedTillTheEnd = -1;
					SaveHistory(listedTillTheEnd, track);

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
//		SaveHistory(listedTillTheEnd);

		PlayNext();

		try {
//			APICalls apiCalls = new APICalls(getApplicationContext());
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Thread.currentThread().interrupt();
//						return;
//					}catch (Exception ex){
//						Log.d(TAG, " " + ex.getLocalizedMessage());
//						Thread.currentThread().interrupt();
//						return;
//					}
//				}
//			}).start();
//			apiCalls.SetStatusTrack(DeviceID, TrackID, ListedTillTheEnd, currentDateTime);

//			сканирование директории с треками для обнаружения и добавления треков, отсутствующих в бд
//			new TrackToCache(getApplicationContext()).ScanTrackToCache();

			// Удаление пропущенного трека
			ContentValues trackForDel = trackDataAccess.GetPathById(TrackID);
			new TrackToCache(getApplicationContext()).DeleteTrackFromCache(trackForDel);
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
//		UpdateButtonPlayPauseImg();
		if (mediaSessionCompat == null || player == null)
			return;

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
				if (mediaSessionCompat.getRemoteControlClient() != null && mediaSessionCompat.getRemoteControlClient().equals(RemoteControlClient.class)) {
					RemoteControlClient remoteControlClient = (RemoteControlClient) mediaSessionCompat.getRemoteControlClient();

					int flags = RemoteControlClient.FLAG_KEY_MEDIA_PLAY
							| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
							| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
					remoteControlClient.setTransportControlFlags(flags);
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
	}

	private void UpdateButtonPlayPauseImg(){
		Intent intent = new Intent(ActionButtonImgUpdate);
		sendBroadcast(intent);
	}

	private void StartNotification() {
		//                MediaMetadataCompat currentTrack = mediaControllerCompat.getMediaMetadata();
		android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();
		style.setMediaSession(mediaSessionCompat.getSessionToken());

		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pendingCancelIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MediaPlayerService.class), PendingIntent.FLAG_CANCEL_CURRENT);
		style.setShowCancelButton(true);
		style.setCancelButtonIntent(pendingCancelIntent);
		String trackTitle;
		String trackArtist;
		MediaMetadataRetriever mMediaMetaDataRetriever = new MediaMetadataRetriever();
		try {
			trackTitle =(track.getAsString("name") == null) ?  "Unknown track" : track.getAsString("name");
			trackArtist =(track.getAsString("artist") == null) ?  "Unknown artist" : track.getAsString("artist");
			//данный блок для получения информации о треке
//			mMediaMetaDataRetriever.setDataSource(track.getAsString("trackurl"));
//			trackTitle = mMediaMetaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//			trackArtist = mMediaMetaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

		}catch (Exception ex){
//			trackTitle = track.getAsString("name");
//			trackArtist = track.getAsString("artist");
			trackTitle ="Unknown track";
			trackArtist = "Unknown artist";
			Log.d(TAG, " " + ex.getLocalizedMessage());
			ex.printStackTrace();
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

//		if(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("DevelopersInfo",false))
//			contentView.setViewVisibility(R.id.viewsTitle, View.GONE);
//		else
//			contentView.setViewVisibility(R.id.viewsTitle, View.VISIBLE);

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

//		String deviceId = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TrackToCache trackToCache = new TrackToCache(getApplicationContext());
					trackToCache.SaveTrackToCache(DeviceID, 3);

					Thread.currentThread().interrupt();
					return;
				}catch (Exception ex){
					Log.d(TAG, " " + ex.getLocalizedMessage());
					Thread.currentThread().interrupt();
					return;
				}
			}
		}).start();
//		TrackToCache trackToCache = new TrackToCache(getApplicationContext());
//		trackToCache.SaveTrackToCache(DeviceID, 3);
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