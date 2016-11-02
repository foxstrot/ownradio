package ru.netvoxlab.ownradio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {
    String DeviceID;
    String UserID;
    TextView textSettingsInfo;
    TextView textViewLocalTrackCount;
    TextView textViewCurrentCacheSize;
    SharedPreferences sp;
    TrackDataAccess trackDataAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        trackDataAccess = new TrackDataAccess(getApplicationContext());
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        DeviceID = sp.getString("DeviceID", "");
        UserID = sp.getString("UserID", "");

        EditText editTextMaxCacheSize = (EditText) findViewById(R.id.editTextMaxCacheSize);
        editTextMaxCacheSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ( !editable.toString().isEmpty() && !editable.toString().equals("null"))
                   sp.edit().putString("MaxCacheSize", editable.toString()).commit();
            }
        });

        EditText countTrackToCache = (EditText) findViewById(R.id.editTextCountTrackToDownload);
        countTrackToCache.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ( !editable.toString().isEmpty() && !editable.toString().equals("null"))
                    sp.edit().putString("CountTrackToCache", editable.toString()).commit();
            }
        });

        textSettingsInfo = (TextView) findViewById(R.id.textViewInfo2);
        textSettingsInfo.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        countTrackToCache.setText(sp.getString("CountTrackToCache",""));
        if (countTrackToCache.getText().toString().isEmpty())
        {
            countTrackToCache.setText("100");
            sp.edit().putString("CountTrackToCache", countTrackToCache.getText().toString()).commit();
        }

        editTextMaxCacheSize.setText(sp.getString("MaxCacheSize", ""));
        if (editTextMaxCacheSize.getText().toString().isEmpty())
        {
            editTextMaxCacheSize.setText("100");
            sp.edit().putString("MaxCacheSize", editTextMaxCacheSize.getText().toString()).commit();
        }

//        textViewLocalTrackCount = (TextView) findViewById(R.id.textViewLocalTrackCount);
//        textViewLocalTrackCount.setText("The count of local track: " +  trackDataAccess.GetExistTracksCount() + ".");
//
//        TrackToCache trackToCache = new TrackToCache();
//        textViewCurrentCacheSize = (TextView)findViewById(R.id.textViewCurrentCacheSize);
//        textViewCurrentCacheSize.setText("Cache size: " + trackToCache.FolderSize(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " (MB).");

        Button btnMerge = (Button) findViewById(R.id.btnMergeUsers);
        btnMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(getApplicationContext());
                EditText editTextUserID = (EditText) findViewById(R.id.editTextUserID);
                if (editTextUserID.getText().toString().isEmpty())
                {
                    textSettingsInfo.append("Введите новый User ID для объединения пользователей \n");
                    return;
                }
                UUID UserIDNew = UUID.fromString(editTextUserID.getText().toString());
                if (!UserIDNew.equals("null"))
                {
                    String resMerge = executeProcedurePostgreSQL.MergeUserID(UserID, UserIDNew.toString());
                    if (resMerge != "-1")
                        textSettingsInfo.append("Невозможно произвести объединение пользователей по ID. Введеный UserID не существует. \n");
                }
                else
                    textSettingsInfo.append("Невозможно произвести объединение пользователей по ID. Введеный UserID имеет неверный формат. \n");
            }
        });

        Button btnDownloadTracks = (Button) findViewById(R.id.btnDownloadTrackToCache);
        btnDownloadTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText countTrackToDownload = (EditText) findViewById(R.id.editTextCountTrackToDownload);
                int countTracks = Integer.parseInt(countTrackToDownload.getText().toString());
                TrackToCache trackToCache = new TrackToCache();

                textSettingsInfo.append(trackToCache.SaveTrackToCache(getApplicationContext(), DeviceID.toString(), countTracks) + "\n");
                textViewLocalTrackCount.setText("The count of local track: " +  trackDataAccess.GetExistTracksCount() + ".");

            }
        });

        Button btnClearCache = (Button) findViewById(R.id.btnClearCache);
        btnClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
                if (dir.isDirectory())
                {
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(dir, children[i]).delete();
                    }
                }
                TrackDataAccess trackDataAccess = new TrackDataAccess(getApplicationContext());
                trackDataAccess.CleanTrackTable();
            }
        });


    }

    @Override
    protected void onResume(){
        super.onResume();

        textViewLocalTrackCount = (TextView) findViewById(R.id.textViewLocalTrackCount);
        textViewLocalTrackCount.setText("The count of local track: " +  trackDataAccess.GetExistTracksCount() + ".");

        TrackToCache trackToCache = new TrackToCache();
        textViewCurrentCacheSize = (TextView)findViewById(R.id.textViewCurrentCacheSize);
        textViewCurrentCacheSize.setText("Cache size: " + trackToCache.FolderSize(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)) / 1048576 + " MB.");
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
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                NavUtils.navigateUpTo(SettingsActivity.this, intent);//.navigateUpFromSameTask(this);
                finish();
                return true;

            case R.id.action_settings:
//                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_exit:
                System.exit(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
