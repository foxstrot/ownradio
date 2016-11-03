package ru.netvoxlab.ownradio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {
	public static final String ActionPlay = "ru.netvoxlab.ownradio.action.PLAY";
	public static final String ActionPause = "ru.netvoxlab.ownradio.action.PAUSE";
	public static final String ActionNext = "ru.netvoxlab.ownradio.action.NEXT";
	public static final String ActionStop = "ru.netvoxlab.ownradio.action.STOP";
	public static final String ActionTogglePlayback = "ru.netvoxlab.ownradio.action.TOGGLEPLAYBACK";
	public static final String ActionSaveCurrentPosition = "ru.netvoxlab.ownradio.action.SAVE_CURRENT_POSITION";


	public MediaPlayer player = null;
	private AudioManager audioManager;
	private WifiManager wifiManager;
	private WifiManager.WifiLock wifiLock;
	private ComponentName remoteComponentName;
	private boolean paused;
	private int NotificationId = 1;

	private MediaSessionCompat mediaSessionCompat;
	public MediaControllerCompat mediaControllerCompat;

	private Handler PlayingHandler;
	private java.lang.Runnable PlayingHandlerRunnable;

	SQLiteDatabase db;
	Cursor userCursor;
	TrackDB trackDB;
	String trackURL;
	TrackDataAccess trackDataAccess;
	String DeviceID;
	String TrackID;
	Boolean FlagDownloadTrack = true;

	int startPosition = 0;
	String startTrackID = "";

	public int GetMediaPlayerState() {
		return (mediaControllerCompat.getPlaybackState() != null ? mediaControllerCompat.getPlaybackState().getState() : PlaybackStateCompat.STATE_NONE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		remoteComponentName = new ComponentName(getPackageName(), new RemoteControlReceiver().ComponentName());
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		startPosition = settings.getInt("LastPosition", 0);
		startTrackID = settings.getString("LastTrackID", "");
		if (settings.getBoolean("StatePlay", false))
			Play();
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
				Stop();
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

	private class TrackInfo {
		public String Title;
		public String Artist;
		public String AlbumArtist;
		public String Album;
		public String Duration;

		public TrackInfo() {
		}

		public TrackInfo(String title, String artist, String albumArtist, String album, String duration) {
			Title = title;
			Artist = artist;
			AlbumArtist = albumArtist;
			Album = album;
			Duration = duration;
		}
	}

	;

	TrackInfo trackInfo = new TrackInfo();


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
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		trackDataAccess = new TrackDataAccess(getApplicationContext());
		DeviceID = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");

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
//            case ActionSaveCurrentPosition: SaveLastPosition();
//                android.os.Process.killProcess(android.os.Process.myPid());
//                break;
		}
		return Service.START_STICKY;
	}

	private void InitializePlayer() {
		player = new MediaPlayer();

		//Tell our player to stream music
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//Wake mode will be partial to keep the CPU still running under lock screen
		player.setWakeMode(getApplicationContext(), 1);// WakeLockFlags.Partial=1

//        player.setOnBufferingUpdateListener(getApplicationContext());
		player.setOnCompletionListener(this);
//        player.setOnErrorListener (this);
		player.setOnPreparedListener(this);
	}

	public void onCompletion(MediaPlayer mediaPlayer) {
//        Toast.makeText(getApplicationContext(), "Completion", Toast.LENGTH_LONG).show();
		//Событие возникает при дослушивании трека до конца, плеер останавливается, отправляется статистика прослушивания
		//затем вызвается функция Play(), запускающая проигрывание следующего трека
		int ListedTillTheEnd = 1;

		try {
			//история прослушивания
//                var DeviceID = Plugin.Settings.CrossSettings.Current.GetValueOrDefault<Guid>("DeviceID");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDateTime = dateFormat.format(new Date()); // Find todays date
			ContentValues track = new ContentValues();
			track.put("trackurl", trackURL);
			track.put("datetimelastlisten", currentDateTime);
			track.put("islisten", ListedTillTheEnd);
			trackDataAccess.SetStatusTrack(track);
			ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(getApplicationContext());
			executeProcedurePostgreSQL.SetStatusTrack(DeviceID, TrackID, ListedTillTheEnd, currentDateTime);
		} catch (Exception ex) {
		}
		PlayNext();
	}

	public void Play() {
//        Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_SHORT).show();
		if (player != null && GetMediaPlayerState() == PlaybackStateCompat.STATE_PAUSED) {
			player.start();
			UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
			StartNotification();

//            UpdateMediaMetadataCompat
		}

		if (player == null)
			InitializePlayer();

		if (mediaSessionCompat == null)
			InitMediaSession();

		if (player.isPlaying()) {
			UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
			StartNotification();
			return;
		}


//            ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(getApplicationContext());
//            String trackId = executeProcedurePostgreSQL.GetNextTrackID(deviceId);
//            Uri trackURI = Uri.parse("http://ownradio.ru/api/track/GetTrackByID/" + trackId);

//            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
//            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            if (!wifiInfo.isConnected()) {
//                Log.d("Wi-Fi", "Wifi disconnected.");
//                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle("")
//                        .setMessage("Подключение к интернету отсутствует.")
//                        .setCancelable(false)
//                        .setNegativeButton("OK",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        dialogInterface.cancel();
//                                    }
//                                });
//                AlertDialog alert = builder.create();
//                alert.show();
//            } else {
		try {
			MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
			ContentValues track = trackDataAccess.GetMostOldTrack();
			if (track != null) {
				FlagDownloadTrack = false;
				TrackID = track.getAsString("id");
				trackURL = track.getAsString("trackurl");
				if (!new File(trackURL).exists()) {
					trackDataAccess.DeleteTrackFromCache(track);
					Play();
				}
				player.setDataSource(getApplicationContext(), Uri.parse(trackURL));

				metaRetriever.setDataSource(trackURL);//, new Dictionary<String,String>());
				int focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
				if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
					//                could not get audio focus
				}

				UpdatePlaybackState(PlaybackStateCompat.STATE_BUFFERING);

				player.prepareAsync();
//                if (startTrackID == TrackID) {
//                    player.seekTo(startPosition);
//                    startPosition = 0;
//                }
				AcquireWifiLock();
				//                UpdateMediaMetadataCompat (metaRetriever);
				StartNotification();

			} else {
				if (FlagDownloadTrack) {
					startPosition = 0;
					FlagDownloadTrack = false;
					TrackToCache trackToCache = new TrackToCache();
					trackToCache.SaveTrackToCache(getApplicationContext(), DeviceID, 2);
					Play();
//                Log.d("MP", "DB is empty");
//                return;
				}
			}

		} catch (IOException ex) {
			UpdatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
			player.reset();
			player.release();
			player = null;
		}
//        }
	}

	public void Pause() {
//        Toast.makeText(getApplicationContext(), "Pause", Toast.LENGTH_LONG).show();
		if (player == null)
			return;

		if (player.isPlaying())
			player.pause();
		UpdatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
	}

	public void Stop() {
		if (player == null)
			return;

		if (player.isPlaying()) {
			player.stop();
		}

		UpdatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
		player.reset();
		StopNotification();
		super.stopForeground(true);
		ReleaseWifiLock();
		UnregisterMediaSessionCompat();
	}

	public void Next() {
//        Toast.makeText(getApplicationContext(), "Next", Toast.LENGTH_LONG).show();
		int ListedTillTheEnd = -1;

		try {
			//история прослушивания
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDateTime = dateFormat.format(new Date()); // Find todays date
			ContentValues track = new ContentValues();
			track.put("trackurl", trackURL);
			track.put("datetimelastlisten", currentDateTime);
			track.put("islisten", ListedTillTheEnd);
			trackDataAccess.SetStatusTrack(track);

			ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(getApplicationContext());
			executeProcedurePostgreSQL.SetStatusTrack(DeviceID, TrackID, ListedTillTheEnd, currentDateTime);
		} catch (Exception ex) {
		}
		PlayNext();
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
		mp.start();
		UpdatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
	}


	public int Position() {
		if (player == null
				|| (GetMediaPlayerState() != PlaybackStateCompat.STATE_PLAYING
				&& GetMediaPlayerState() != PlaybackStateCompat.STATE_PAUSED))
			return -1;
		else
			return player.getCurrentPosition();
	}

	private void UpdatePlaybackState(int state) {
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
					.setState(state, Position(), 1.0f, SystemClock.elapsedRealtime());

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

			if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
				StartNotification();
			}
		} catch (Exception ex) {
		}
	}


	private void StartNotification() {
		//                MediaMetadataCompat currentTrack = mediaControllerCompat.getMediaMetadata();
		android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();
		style.setMediaSession(mediaSessionCompat.getSessionToken());

		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pendingCancelIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MediaPlayerService.class), PendingIntent.FLAG_CANCEL_CURRENT);
		style.setShowCancelButton(true);
		style.setCancelButtonIntent(pendingCancelIntent);

		String trackTitle = TrackID;
		String trackArtist = "Ownradio";

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
			contentView.setImageViewResource(R.id.viewsPlayPause, android.R.drawable.ic_media_pause);
		} else {
			playIntent.setAction(ActionPlay);
			contentView.setImageViewResource(R.id.viewsPlayPause, android.R.drawable.ic_media_play);
		}
		PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

		Intent nextIntent = new Intent(this, MediaPlayerService.class);
		nextIntent.setAction(ActionNext);
		PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);


		contentView.setOnClickPendingIntent(R.id.viewsNext, pnextIntent);
		contentView.setOnClickPendingIntent(R.id.viewsPlayPause, pplayIntent);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
				.setSmallIcon(R.drawable.icon)
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

	public void PlayPause() {
		if (player == null || (player != null && GetMediaPlayerState() == PlaybackStateCompat.STATE_PAUSED)) {
			Play();
		} else {
			Pause();
		}
	}

	public void PlayNext() {
		if (player != null) {
			player.reset();
			player.release();
			player = null;
		}
		String deviceId = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");
		TrackToCache trackToCache = new TrackToCache();
		trackToCache.SaveTrackToCache(getApplicationContext(), deviceId, 2);
		UpdatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
		Play();
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
			mediaPlayerService.GetMediaPlayerService().PlayNext();
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
			super.stopForeground(true);
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
