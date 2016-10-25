package ru.netvoxlab.ownradio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.support.v4.media.session.MediaSessionCompat;
import  android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;
    private ExecuteProcedurePostgreSQL executeProcedurePostgreSQL;
    private MediaSessionCompat mediaSessionCompat;
    public MediaMetadataCompat mediaControllerCompat;

    String DeviceId;
    String UserId;
    SharedPreferences sp;

    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        // полная очистка настроек
//         sp.edit().clear().commit();


        /////
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);  //startActivityForResult(settingsIntent, 1);

//
//                Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
//                intent.setAction("ru.netvoxlab.ownradio.action.PLAY");
//
//                startService(intent);

               // startService(new Intent(MainActivity.this, MediaPlayerService.class));
//                MusicPlayerService.StartN
            }
        });

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

        Button btnDownloadTrack = (Button) findViewById(R.id.btnDownload);
        btnDownloadTrack.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetTrack getTrack = new GetTrack();
               path = getTrack.GetTrackByID(MainActivity.this, "00119cc6-2d9d-4f77-b05a-d5ab9c92c894");
                List<String> paths = new ArrayList<String>();
                File directory = new File(path);

                File[] files = directory.listFiles();

                for (int i = 0; i < files.length; ++i) {
                    paths.add(files[i].getAbsolutePath());
                }

//
//                File file = new File(path);
                Toast.makeText(MainActivity.this, "File exists: "  + files[0].exists(), Toast.LENGTH_LONG).show();
            }
        }));

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

//         ATTENTION: This was auto-generated to implement the App Indexing API.
//         See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
        executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(MainActivity.this);
      //  sp = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            DeviceId = sp.getString("DeviceID", "");
            if(DeviceId.isEmpty()){
//                DeviceId == UUID.fromString( "00000000-0000-0000-0000-000000000000")) {
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
    }

//    protected void onResume() {
//
//    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
    }
//
//    class MediaPlayerServiceConnection extends Object, ServiceConnection
//    {
//        MainActivity instance;
//
//        public MediaPlayerServiceConnection (MainActivity mediaPlayer)
//        {
//            this.instance = mediaPlayer;
//        }
//
//        public void OnServiceConnected (ComponentName name, IBinder service)
//        {
//
//        }
//
//        public void OnServiceDisconnected(ComponentName name)
//        {
//            instance.isBound = false;
//        }
//    }

}

