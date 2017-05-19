package ru.netvoxlab.ownradio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
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
import android.widget.RemoteViews;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.app.PendingIntent.getActivity;
import static ru.netvoxlab.ownradio.MainActivity.ActionButtonImgUpdate;
import static ru.netvoxlab.ownradio.MainActivity.ActionProgressBarFirstTracksLoad;
import static ru.netvoxlab.ownradio.MainActivity.ActionProgressBarUpdate;
import static ru.netvoxlab.ownradio.MainActivity.ActionTrackInfoUpdate;
import static ru.netvoxlab.ownradio.RequestAPIService.ACTION_GETNEXTTRACK;
import static ru.netvoxlab.ownradio.RequestAPIService.ACTION_SENDHISTORY;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_COUNT;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_DEVICEID;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {
	public static final String ActionPlay = "ru.netvoxlab.ownradio.action.PLAY";
	public static final String ActionPause = "ru.netvoxlab.ownradio.action.PAUSE";
	public static final String ActionNext = "ru.netvoxlab.ownradio.action.NEXT";
	public static final String ActionStop = "ru.netvoxlab.ownradio.action.STOP";
	public static final String ActionTogglePlayback = "ru.netvoxlab.ownradio.action.TOGGLEPLAYBACK";
	public static final String ActionUpdateNotification = "ru.netvoxlab.ownradio.action.UPDATE_NOTIFICATION";
	public static MediaPlayer player = null;
	private AudioManager audioManager;
	private WifiManager wifiManager;
	private WifiManager.WifiLock wifiLock;

	private PowerManager pm;
	private PowerManager.WakeLock wl;

	private ComponentName remoteComponentName;
	private int NotificationId = 1;

	private MediaSessionCompat mediaSessionCompat;
	public MediaControllerCompat mediaControllerCompat;

	private Handler handler = new Handler();
	public static boolean playbackWithHSisInterrupted = false;
	public static boolean isHSConnected = false;
	String trackURL;
	TrackDataAccess trackDataAccess;
	Utilites utilites;
	String DeviceID;
	String UserID;
	String TrackID = "";
	Boolean FlagDownloadTrack = true;
	final String TAG = "ownRadio";

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
		utilites = new Utilites();
		DeviceID = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");
		UserID = DeviceID;//PreferenceManager.getDefaultSharedPreferences(this).getString("UserID", "");
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		remoteComponentName = new ComponentName(getPackageName(), new RemoteControlReceiver().ComponentName());
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if(playbackWithHSisInterrupted && !isHSConnected)
					return;

				if (player == null)
					InitializePlayer();

				//проверить возобновление проигрывания при переключении фокуса
				if (!player.isPlaying() && GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
					player.start();
				}

				player.setVolume(1.0f, 1.0f);
				playbackWithHSisInterrupted = false;
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				Pause();
				if(isHSConnected && player.isPlaying())
					playbackWithHSisInterrupted = true;
//				Stop();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				//We have lost focus for a short time, but likely to resume so pause
				Pause();
				if(isHSConnected && player.isPlaying())
					playbackWithHSisInterrupted = true;
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				//We have lost focus but should till play at a muted 10% volume
				if (player.isPlaying())
					player.setVolume(.1f, .1f);
				break;
		}
	}

	public MediaPlayerService() {
	}

	private void InitMediaSession() {
		try {
			if (mediaSessionCompat == null) {
				Intent nIntent = new Intent(getApplicationContext(), MainActivity.class);
				PendingIntent pIntent = getActivity(getApplicationContext(), 0, nIntent, 0);

				remoteComponentName = new ComponentName(getApplication().getPackageName(), new RemoteControlReceiver().ComponentName());

				mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "StreamingAudio", remoteComponentName, null);
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
		if(wl != null && wl.isHeld())
			wl.release();
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
		return Service.START_NOT_STICKY;
//		return Service.START_REDELIVER_INTENT;
	}

	private void InitializePlayer() {
		utilites.SendInformationTxt(getApplicationContext(), "Play(): InitializePlayer");
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
		utilites.SendInformationTxt(getApplicationContext(), "Completion playback");
		Intent historySenderIntent = new Intent(this, RequestAPIService.class);
		historySenderIntent.setAction(ACTION_SENDHISTORY);
		historySenderIntent.putExtra(EXTRA_DEVICEID, DeviceID);
		startService(historySenderIntent);
	}

	public void Play() {
		Log.d(TAG, "Play(): ");
		playbackWithHSisInterrupted = false;

		if (player != null && GetMediaPlayerState() == PlaybackStateCompat.STATE_PAUSED) {
			int focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
				utilites.SendInformationTxt(getApplicationContext(), "Не удалось получить аудиофокус");
				return;
			}

			player.start();
			utilites.SendInformationTxt(getApplicationContext(), "Play(): start");

//			UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
//			StartNotification();
//			UpdateButtonPlayPauseImg();
			UpdateMediaMetadataCompat();
		}

		if (player == null)
			InitializePlayer();

		if (mediaSessionCompat == null)
			InitMediaSession();

		if (player.isPlaying()) {
			UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
			return;
		}

		try {

			if (!new Utilites().CheckCountTracksAndDownloadIfNotEnought(getApplicationContext(), DeviceID))
				return;
//			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//			startPosition = settings.getInt("LastPosition", 0);
//			startTrackID = settings.getString("LastTrackID", "");
			
			if(getTrackForPlayFromDB()){
//				player.setDataSource(getApplicationContext(), Uri.parse(trackURL));
				player.setDataSource(trackURL);
				Intent i = new Intent(ActionTrackInfoUpdate);
				getApplicationContext().sendBroadcast(i);
			} else {
				new Utilites().CheckCountTracksAndDownloadIfNotEnought(getApplicationContext(), DeviceID);
				return;
			}

			int focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			}

			UpdatePlaybackState(PlaybackStateCompat.STATE_BUFFERING);
			Log.d(TAG, "Play(): UpdatePlaybackState");

			player.prepareAsync();
			Log.d(TAG, "Play(): prepareAsync");

			//записываем время начала проигрывания трека
			new TrackDataAccess(getApplicationContext()).SetTimeAndCountStartPlayback(track);
			new Utilites().SendInformationTxt(getApplicationContext(), "SetTimeAndCountStartPlayback for track" + track.getAsString("id"));

			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "OwnRadioPlayback");
			if(!wl.isHeld())
				wl.acquire();

//			AcquireWifiLock();
			UpdateMediaMetadataCompat();
//			if (GetMediaPlayerState() != PlaybackStateCompat.STATE_BUFFERING) {
//				StartNotification();
//				UpdateButtonPlayPauseImg();
//			}

			Intent progressIntent = new Intent(ActionProgressBarUpdate);
			sendBroadcast(progressIntent);
			Log.d(TAG, "Play(): sendBroadcast");
		} catch (Exception ex) {
			utilites.SendInformationTxt(getApplicationContext(), "Error in Play(): " + ex.getLocalizedMessage());
			//player.reset();
			player.release();
			player = null;
			UpdatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
			return;
		}
		
		Intent downloaderIntent = new Intent(this, RequestAPIService.class);
		downloaderIntent.setAction(ACTION_GETNEXTTRACK);
		downloaderIntent.putExtra(EXTRA_DEVICEID, DeviceID);
		downloaderIntent.putExtra(EXTRA_COUNT, 3);
		startService(downloaderIntent);
	}

	public void Pause() {
		if (player == null)
			return;

		if (player.isPlaying())
			player.pause();
		UpdatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
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
//			player.reset();
			player.release();
			player = null;
//			StartNotification();
//			super.stopForeground(true);
			ReleaseWifiLock();
			if(wl != null && wl.isHeld())
				wl.release();
//			UnregisterMediaSessionCompat();
		}catch (Exception ex){
			Log.d(TAG, " " + ex.getLocalizedMessage());
		}
	}

	public void Next() {
		if(new TrackDataAccess(getApplicationContext()).GetExistTracksCount() <3){
			Intent progressIntent = new Intent(ActionProgressBarFirstTracksLoad);
			getApplicationContext().sendBroadcast(progressIntent);
			return;
		}
		
		//Сохраняем информацию о прослушивании в локальную БД.
		int listedTillTheEnd = -1;
		SaveHistory(listedTillTheEnd, track);
		// Удаление пропущенного трека
		new TrackToCache(getApplicationContext()).DeleteTrackFromCache(track);
		PlayNext();
		utilites.SendInformationTxt(getApplicationContext(), "Skip track to next");
		
		Intent historySenderIntent = new Intent(this, RequestAPIService.class);
		historySenderIntent.setAction(ACTION_SENDHISTORY);
		historySenderIntent.putExtra(EXTRA_DEVICEID, DeviceID);
		startService(historySenderIntent);
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
				HistoryDataAccess historyDataAccess = new HistoryDataAccess(getApplicationContext());
				historyDataAccess.SaveHistoryRec(history);//сохраняет информацию в history для последующей отправки на сервер
			}
		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}
	}

	private boolean getTrackForPlayFromDB(){
		boolean flagFindTrack = false;
		do {
			track = trackDataAccess.GetMostOldTrack();
			new Utilites().SendInformationTxt(getApplicationContext(), "getTrackForPlayFromDB, id=" + track.getAsString("id"));
//			Intent i = new Intent(ActionTrackInfoUpdate);
//			getApplicationContext().sendBroadcast(i);
			if (track != null) {
				FlagDownloadTrack = false;
				TrackID = track.getAsString("id");
				trackURL = track.getAsString("trackurl");
				if (!new File(trackURL).exists()) {
					trackDataAccess.DeleteTrackFromCache(track);
				} else
					flagFindTrack = true;
			}else
				return false;
			}while(!flagFindTrack);
			return true;
//			return true;
//		}else
//			return false;
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
//		if (startTrackID.equals(TrackID)) {
//			player.seekTo(startPosition);
//			startPosition = 0;
//		}
		mp.start();
		utilites.SendInformationTxt(getApplicationContext(), "Start playback");
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
		if (mediaSessionCompat == null || player == null) {
			UpdateButtonPlayPauseImg();
			return;
		}
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

			mediaSessionCompat.setPlaybackState(stateBuilder.build());

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
		android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();
		style.setMediaSession(mediaSessionCompat.getSessionToken());

		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pendingCancelIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MediaPlayerService.class), PendingIntent.FLAG_CANCEL_CURRENT);
		style.setShowCancelButton(true);
		style.setCancelButtonIntent(pendingCancelIntent);
		String trackTitle;
		String trackArtist;
		try {
			trackTitle = (track.getAsString("title") == null) ? "Track" : track.getAsString("title");
			trackArtist = (track.getAsString("artist") == null) ? "Artist" : track.getAsString("artist");

		} catch (Exception ex) {
			trackTitle = "Track";
			trackArtist = "Artist";
			Log.d(TAG, " " + ex.getLocalizedMessage());
		}

		Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
		intent.setAction(ActionPlay);
		RemoteViews contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);

//		contentView.setViewVisibility(R.id.viewsIcon, View.VISIBLE);

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
			contentView.setImageViewResource(R.id.viewsPlayPause, R.drawable.btn_pause);
		} else {
			playIntent.setAction(ActionPlay);
			contentView.setImageViewResource(R.id.viewsPlayPause, R.drawable.btn_play);
		}
		PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

		Intent nextIntent = new Intent(this, MediaPlayerService.class);
		nextIntent.setAction(ActionNext);
		PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

		contentView.setOnClickPendingIntent(R.id.viewsNext, pnextIntent);
		contentView.setOnClickPendingIntent(R.id.viewsPlayPause, pplayIntent);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(getNotificationIcon())
				.setContentIntent(pendingIntent)
				.setCustomContentView(contentView)
				.setContentTitle(trackTitle)
				.setContentText(trackArtist)
				.setShowWhen(false)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

		if (GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING)
			builder.setOngoing(true);
		else
			builder.setOngoing(false);
//        style.setShowActionsInCompactView(0,1,2);
		NotificationManagerCompat.from(this).notify(NotificationId, builder.build());
		
	}

	private int getNotificationIcon() {
		boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
		return useWhiteIcon ? R.drawable.logo_white : R.drawable.logo;
	}

	public void StopNotification() {
		NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
		nm.cancelAll();
	}

	public void PlayNext() {
		if (player != null) {
//			player.reset();
			player.release();
			player = null;
		}
		UpdatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
		Play();
	}

	private void UpdateMediaMetadataCompat (){
		if (mediaSessionCompat == null)
			return;

		MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

		builder
				.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, GetDuration())//track.getAsLong("length"))
				.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.icon))
//				.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.drawable.icon))
//				.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.drawable.icon))
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getAsString("title"))
				.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, track.getAsString("name"))
				.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, track.getAsString("artist"))
				.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getAsString("artist"))
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,  track.getAsString("artist"))
//				.putString(MediaMetadataCompat.METADATA_KEY_AUTHOR,  track.getAsString("artist"))
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "ownRadio");

		mediaSessionCompat.setMetadata(builder.build());
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

			Intent intent = new Intent(ActionButtonImgUpdate);
			sendBroadcast(intent);
			StopNotification();
			stopForeground(true);
			ReleaseWifiLock();
			if(wl != null && wl.isHeld())
				wl.release();
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