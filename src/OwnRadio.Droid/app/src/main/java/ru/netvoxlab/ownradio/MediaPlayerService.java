package ru.netvoxlab.ownradio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private static final String ActionPlay = "ru.netvoxlab.ownradio.action.PLAY";
    private static final String ActionPause = "ru.netvoxlab.ownradio.action.PAUSE";
    private static final String ActionNext = "ru.netvoxlab.ownradio.action.NEXT";
    private static final String ActionStop = "ru.netvoxlab.ownradio.action.STOP";
    private static final String ActionTogglePlayback = "ru.netvoxlab.ownradio.action.TOGGLEPLAYBACK";


    private MediaPlayer player = null;
    private AudioManager audioManager;
    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    private ComponentName remoteComponentName;
    private boolean paused;
    private int NotificationId = 1;

    private Track trackInstanse;
  //  ITrackToCache trackToCache = new TrackToCache();

    private class TrackInfo
    {
        public String Title;
        public String Artist;
        public String AlbumArtist;
        public String Album;
        public String Duration;
        public TrackInfo() { }
        public TrackInfo(String title, String artist, String albumArtist, String album, String duration)
        {
            Title = title;
            Artist = artist;
            AlbumArtist = albumArtist;
            Album = album;
            Duration = duration;
        }
    };

    TrackInfo trackInfo = new TrackInfo();


    public MediaPlayerService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

//        if((flags&START_FLAG_RETRY)==0){
//        }
//        else{
//        }
        String action = intent.getAction();

        switch(action){
            case ActionPlay :
                Play();
//                StartNotification();
                break;
            case ActionPause: Pause(); break;
            case ActionNext: Next(); break;
            case ActionStop: Stop(); break;
            case ActionTogglePlayback:
                if(player == null)
                    return Service.START_STICKY;

                if(player.isPlaying())
                    Pause();
                else
                    Play();
                break;
        }
//        StartNotification();

        return  Service.START_STICKY;
    }

    private void InitializePlayer()
    {
        //Intent iStatus = Application.Context.RegisterReceiver(HSReceiver, new IntentFilter(Intent.ActionHeadsetPlug));
        player = new MediaPlayer();

        //Tell our player to stream music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //Wake mode will be partial to keep the CPU still running under lock screen
        player.setWakeMode(getApplicationContext(), 1);// WakeLockFlags.Partial=1

//        player.setOnBufferingUpdateListener(getApplicationContext());
        player.setOnCompletionListener (this);
//        player.setOnErrorListener (this);
        player.setOnPreparedListener (this);
    }

    //When we have prepared the song start playback
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
    }

    public  void onCompletion(MediaPlayer mediaPlayer)
    {
        Toast.makeText(getApplicationContext(), "Completion", Toast.LENGTH_LONG).show();
        //Событие возникает при дослушивании трека до конца, плеер останавливается, отправляется статистика прослушивания
        //затем вызвается функция Play(), запускающая проигрывание следующего трека
            int ListedTillTheEnd = 1;

            try
            {
                //история прослушивания
//                var DeviceID = Plugin.Settings.CrossSettings.Current.GetValueOrDefault<Guid>("DeviceID");
//                TrackDataAccess trackDataAccess = new TrackDataAccess();
//                trackDataAccess.SetTrackStatus(new Track { ID = trackInstanse.ID, DateTimeLastListen = DateTime.Now.ToString("dd.MM.yyyy HH:mm:sszz"), IsListen = ListedTillTheEnd, Path = trackInstanse.Path, IsExist = 1 });
//                ISetStatusTrack statusTrack = new StatusTrack();
//                statusTrack.SetStatusTrack(DeviceID, trackInstanse.ID, ListedTillTheEnd, DateTime.Now);
//                //скачиваем новые треки
//                trackToCache.SaveTrackToCache(DeviceID, "новых", 2);
            }
            catch (Exception ex)
            {
            }
            PlayNext ();
        }


    private void Play()
    {
Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_LONG).show();
//        if (player == null)
//            InitializePlayer();
//
//        if (player.isPlaying()) {
////            UpdatePlaybackState(PlaybackStateCompat.StatePlaying);
//            return;
//        }
//
//        try {
//            ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(getApplicationContext());
        String deviceId = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID","");
//        MediaPlayer player = new MediaPlayer();
        InitializePlayer();
        try {
            ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(getApplicationContext());
            String trackId = executeProcedurePostgreSQL.GetNextTrackID(deviceId);
            Uri trackURI = Uri.parse("http://ownradio.ru/api/track/GetTrackByID/" + trackId);

            ////            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever ();

            player.setDataSource (getApplicationContext(), trackURI);
////            metaRetriever.setDataSource(trackURI, new Dictionary<String,String>());
//            AudioManager focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioFocus.Gain);
////            if (focusResult !=  AudioFocusRequest.Granted) {
////                could not get audio focus
////            }

////            UpdatePlaybackState(PlaybackStateCompat.StateBuffering);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            player.prepareAsync();
//            AcquireWifiLock ();
//           // UpdateMediaMetadataCompat (metaRetriever);
            StartNotification ();
        }catch (IOException ex){
            ////            UpdatePlaybackState(PlaybackStateCompat.StateStopped);
//
//            player.reset();
//            player.release();
//            player = null;
        }



//            player.setDataSource (getApplicationContext(), trackURI);
//
////            metaRetriever.setDataSource(trackURI, new Dictionary<String,String>());
//
////            AudioManager focusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioFocus.Gain);
////            if (focusResult !=  AudioFocusRequest.Granted) {
////                could not get audio focus
////            }
//            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                public void onPrepared(MediaPlayer mp) {
//                    mp.start();
//                }
//            });
////            UpdatePlaybackState(PlaybackStateCompat.StateBuffering);
//            player.prepareAsync();
//
//
//            AcquireWifiLock ();
//           // UpdateMediaMetadataCompat (metaRetriever);
//            StartNotification ();
//        } catch (Exception ex) {
////            UpdatePlaybackState(PlaybackStateCompat.StateStopped);
//
//            player.reset();
//            player.release();
//            player = null;
//        }



    }

    private void Pause()
    {
        Toast.makeText(getApplicationContext(), "Pause", Toast.LENGTH_LONG).show();
        if(player.isPlaying())
            player.pause();
    }
    private void Stop()
    {
        player.stop();
    }

    private void Next()
    {
        Toast.makeText(getApplicationContext(), "Next", Toast.LENGTH_LONG).show();
        int ListedTillTheEnd = -1;

        try
        {
            //история прослушивания
//                var DeviceID = Plugin.Settings.CrossSettings.Current.GetValueOrDefault<Guid>("DeviceID");
//                TrackDataAccess trackDataAccess = new TrackDataAccess();
//                trackDataAccess.SetTrackStatus(new Track { ID = trackInstanse.ID, DateTimeLastListen = DateTime.Now.ToString("dd.MM.yyyy HH:mm:sszz"), IsListen = ListedTillTheEnd, Path = trackInstanse.Path, IsExist = 1 });
//                ISetStatusTrack statusTrack = new StatusTrack();
//                statusTrack.SetStatusTrack(DeviceID, trackInstanse.ID, ListedTillTheEnd, DateTime.Now);
//                //скачиваем новые треки
//                trackToCache.SaveTrackToCache(DeviceID, "новых", 2);
        }
        catch (Exception ex)
        {
        }
        PlayNext ();
    }

    private void AcquireWifiLock()
    {
        if (wifiLock == null){
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "OwnRadioWiFiLock");
        }
        wifiLock.acquire();
    }

    private void ReleaseWifiLock()
    {
        if (wifiLock == null)
            return;

        wifiLock.release();
        wifiLock = null;
    }

    public void OnPrepared (MediaPlayer mp)
    {
        //Mediaplayer is prepared start track playback
        mp.start ();
//        UpdatePlaybackState(PlaybackStateCompat.StatePlaying);
    }


    private void StartNotification()
    {
        ////                MediaMetadataCompat currentTrack = mediaControllerCompat.getMediaMetadata();
        android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle();
//                style.setMediaSession(mediaSessionCompat.getSessionToken());
//        Context context = getApplicationContext();

//        Intent intent = new Intent(this, MainActivity.class);
//                intent.setAction("")

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingCancelIntent = PendingIntent.getService(getApplicationContext(), 1, new Intent(this, MediaPlayerService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        style.setShowCancelButton(true);
        style.setCancelButtonIntent(pendingCancelIntent);

//        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//        Notification notification = new NotificationCompat.Builder(context)
//                .setVisibility(Notification.VISIBILITY_PUBLIC)
//                .setSmallIcon(R.drawable.icon)
////                .addAction(android.R.drawable.ic_media_play, "Play", pIntent)
////                .addAction(android.R.drawable.ic_media_pause, "Pause", pIntent)
////                .addAction(android.R.drawable.ic_media_next, "Next", pIntent)
//                .setStyle((style)
//                        .setShowActionsInCompactView(1))
////                        .setMediaSession(mediaSessionCompat.getSessionToken()))
//                .setContentTitle("Title")
//                .setContentText("Text")
//                .build();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setStyle(style)
                .setContentTitle("Title")
                .setContentText("Text")
                .setContentInfo("Info")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
//                .setOngoing()
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        builder.addAction(GenerateActionCompat(android.R.drawable.ic_media_play, "Play", ActionPlay));
        builder.addAction(GenerateActionCompat(android.R.drawable.ic_media_pause, "Pause", ActionPause));
        builder.addAction(GenerateActionCompat(android.R.drawable.ic_media_next, "Next", ActionNext));
        style.setShowActionsInCompactView(0,1,2);
        NotificationManagerCompat.from(getApplicationContext()).notify(NotificationId, builder.build());

//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

//        notificationManager.notify(2, notification);
    }

    private NotificationCompat.Action GenerateActionCompat(int icon, String title, String intentAction)
    {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (intentAction.equals(ActionStop))
            flags = PendingIntent.FLAG_CANCEL_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, flags);

        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void AddPlayPauseActionCompat(NotificationCompat.Builder builder)
    {
//        if (MediaPlayerState == PlaybackStateCompat.STATE_PLAYING)
//            builder.addAction(GenerateActionCompat(android.R.drawable.ic_media_pause, "Pause", ActionPause));
//        else
//            builder.addAction(GenerateActionCompat(android.R.drawable.ic_media_play, "Play", ActionPlay));
    }

    public void PlayNext ()
    {
        if (player != null) {
            player.reset ();
            player.release ();
            player = null;
        }

//        UpdatePlaybackState(PlaybackStateCompat.StateSkippingToNext);

        Play();
    }

}
