package ru.netvoxlab.ownradio;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import  android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ExecuteProcedurePostgreSQL executeProcedurePostgreSQL;

    String DeviceId;
    String UserId;
    SharedPreferences sp;
    String trackId;

    boolean isBound = false;
    private MediaPlayerService.MediaPlayerServiceBinder binder;
//    MediaPlayerServiceConnection mediaPlayerServiceConnection;
    private Intent mediaPlayerServiceIntent;

    private long enqueue;
    private DownloadManager dm;

    TrackDB trackDB;
    SQLiteDatabase db;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Приложение запущено впервые или восстановлено из памяти?
        if ( savedInstanceState == null )   // приложение запущено впервые
        {
        }
        else // приложение восстановлено из памяти
        {
        }


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        trackDB = new TrackDB(getApplicationContext(),1);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
//         полная очистка настроек
//         sp.edit().clear().commit();

        TextView textInfo = (TextView) findViewById(R.id.textViewInfo);
        textInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());

//        ToggleButton btnPlayPause = (ToggleButton) findViewById(R.id.btnPlayPause);
//        btnPlayPause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        ImageButton btnPlay = (ImageButton) findViewById(R.id.imgBtnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
                intent.setAction("ru.netvoxlab.ownradio.action.PLAY");
                startService(intent);
            }
        });

        ImageButton btnPause = (ImageButton) findViewById(R.id.imgBtnPause);
        btnPause.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
                intent.setAction("ru.netvoxlab.ownradio.action.PAUSE");
                startService(intent);
            }
        }));

        ImageButton btnNext = (ImageButton) findViewById(R.id.imgBtnNext);
        btnNext.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
                intent.setAction("ru.netvoxlab.ownradio.action.NEXT");
                startService(intent);
            }
        }));

//        Button btnShowDownload = (Button) findViewById(R.id.btnShowDownload);
//        btnShowDownload.setOnClickListener(((new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent();
//                i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
//                startActivity(i);
//            }
//        })));
    }

    @Override
    public void onStart() {
        super.onStart();

        TextView textVersionName = (TextView) findViewById(R.id.versionName);
        TextView textDeviceID = (TextView) findViewById(R.id.deviceID);
        TextView textUserID = (TextView) findViewById(R.id.userID);

        try {
            String info =  this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionName;
            textVersionName.setText("Version name: " + info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(MainActivity.this);
        try {
            DeviceId = sp.getString("DeviceID", "");
            if(DeviceId.isEmpty()){
                DeviceId = UUID.randomUUID().toString();
                String UserName = "NewUser";
                String DeviceName= Build.BRAND;
                executeProcedurePostgreSQL.RegisterDevice(DeviceId, UserName, DeviceName);
                UserId = executeProcedurePostgreSQL.GetUserId(DeviceId);
                sp.edit().putString("DeviceID", DeviceId).commit();
                sp.edit().putString("UserID", UserId);
                sp.edit().putString("UserName", UserName);
                sp.edit().putString("DeviceName", DeviceName);
                sp.edit().commit();
            }
            else
            {
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
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_exit:
//                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
//                intent.setAction("ru.netvoxlab.ownradio.action.SAVE_CURRENT_POSITION");
//                startService(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    class MediaPlayerServiceConnection extends java.lang.Object implements ServiceConnection
//    {
//        MainActivity instance;
//
//        public MediaPlayerServiceConnection (MainActivity pl)
//        {
//            this.instance = player;
//        }

//    public void OnServiceConnected (ComponentName name, IBinder service)
//    {
//        MediaPlayerService.MediaPlayerServiceBinder mediaPlayerServiceBinder = service  MediaPlayerService.MediaPlayerServiceBinder;
//        if (mediaPlayerServiceBinder != null) {
//            MediaPlayerService.MediaPlayerServiceBinder binder = (MediaPlayerService.MediaPlayerServiceBinder)service;
//            instance.binder = binder;
//            instance.isBound = true;

//            binder.GetMediaPlayerService().CoverReloaded += (object sender, EventArgs e) => { if (instance.CoverReloaded != null) instance.CoverReloaded(sender, e); };
//            binder.GetMediaPlayerService().StatusChanged += (object sender, EventArgs e) => { if (instance.StatusChanged != null) instance.StatusChanged(sender, e); };
//            binder.GetMediaPlayerService().Playing += (object sender, EventArgs e) => { if (instance.Playing != null) instance.Playing(sender, e); };
//            binder.GetMediaPlayerService().Buffering += (object sender, EventArgs e) => { if (instance.Buffering != null) instance.Buffering(sender, e); };
//        }
//    }


//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            instance.isBound = false;
//        }
//    }

    @SuppressLint("NewApi")
    public void AddNotification (){
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        @SuppressWarnings("deprecation")
        Notification notification = builder.getNotification();
        notification.when = System.currentTimeMillis();
        notification.tickerText = "TrackID";
        notification.icon = R.drawable.icon;

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
//        setListeners(contentView);
        notification.contentView = contentView;
      //  notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(3, notification);
    }
//
//    public void setListeners(RemoteViews view){
//        Context context = getApplicationContext();
//
//        //radio listener
//        Intent radio=new Intent(context,HelperActivity.class);
//        radio.putExtra("DO", "radio");
//        PendingIntent pRadio = PendingIntent.getActivity(ctx, 0, radio, 0);
//        view.setOnClickPendingIntent(R.id.radio, pRadio);
//
//        //volume listener
//        Intent volume=new Intent(ctx, HelperActivity.class);
//        volume.putExtra("DO", "volume");
//        PendingIntent pVolume = PendingIntent.getActivity(ctx, 1, volume, 0);
//        view.setOnClickPendingIntent(R.id.volume, pVolume);
//
//        //reboot listener
//        Intent reboot=new Intent(ctx, HelperActivity.class);
//        reboot.putExtra("DO", "reboot");
//        PendingIntent pReboot = PendingIntent.getActivity(ctx, 5, reboot, 0);
//        view.setOnClickPendingIntent(R.id.reboot, pReboot);
//
//        //top listener
//        Intent top=new Intent(ctx, HelperActivity.class);
//        top.putExtra("DO", "top");
//        PendingIntent pTop = PendingIntent.getActivity(ctx, 3, top, 0);
//        view.setOnClickPendingIntent(R.id.top, pTop);*/
//
//        //app listener
//        Intent app=new Intent(ctx, com.example.demo.HelperActivity.class);
//        app.putExtra("DO", "app");
//        PendingIntent pApp = PendingIntent.getActivity(ctx, 4, app, 0);
//        view.setOnClickPendingIntent(R.id.btn1, pApp);
//    }
}

