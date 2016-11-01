package ru.netvoxlab.ownradio;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by a.polunina on 24.10.2016.
 */

public class TrackToCache {
    public TrackToCache(){
    }

//    public long GetAvailableSpace(Context context)

    public String SaveTrackToCache(Context context, String deviceId, int trackCount) {
        TrackDataAccess trackDataAccess = new TrackDataAccess(context);
        CheckConnection checkConnection = new CheckConnection();
        boolean wifiConnect = checkConnection.CheckWifiConnection(context);
        if(!wifiConnect)
            return "Подключение к интернету отсутствует";

        String filePath;
        String trackId;
        long cacheSize = 0;
        long availableSpace = 0;
        long minAvailableSpace = 20 * 1048576;
        File[] externalStoragesPaths = ContextCompat.getExternalFilesDirs(context, null);
        File externalStoragePath;
        if (externalStoragesPaths == null) {
            return "Директория на карте памяти недоступна";
        }
        externalStoragePath = externalStoragesPaths[0];

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String maxCacheSize = sp.getString("MaxCacheSize","");
        if (maxCacheSize.isEmpty())
        {
            maxCacheSize = "100";
            sp.edit().putString("MaxCacheSize", maxCacheSize).commit();
        }

        ExecuteProcedurePostgreSQL executeProcedurePostgreSQL = new ExecuteProcedurePostgreSQL(context);
        GetTrack getTrack = new GetTrack();

        for(int i = 0; i < trackCount; i++) {
            cacheSize = FolderSize(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC));
            if (Build.VERSION.SDK_INT <= 17) {
                StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                availableSpace = (long) stat.getFreeBlocks() * (long)stat.getBlockSize();
//                Toast.makeText(context, "availableSpace :" + availableSpace , Toast.LENGTH_LONG).show();
            }
            if(Build.VERSION.SDK_INT >= 18){
                availableSpace = new StatFs(externalStoragePath.getPath()).getAvailableBytes();
//                Toast.makeText(context, "availableSpace :" + availableSpace / 1048576, Toast.LENGTH_LONG).show();
            }


            if (availableSpace > minAvailableSpace && cacheSize < Integer.parseInt(maxCacheSize)* 1048576) {
                try {
                    trackId = executeProcedurePostgreSQL.GetNextTrackID(deviceId);
                    //Загружаем трек и сохраняем информацию о нем в БД
                    getTrack.GetTrackDM(context, trackId);

                } catch (Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }
            else {
                ContentValues track = trackDataAccess.GetTrackForDel();
                if(track != null){
                    File file1 = new File(track.getAsString("trackurl"));
                    File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) +"/"+ file1.getName());
                    if(file.exists()) {
                        if(file.delete()) {
                            trackDataAccess.DeleteTrackFromCache(track);
                            Toast.makeText(context, "File is deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        trackDataAccess.DeleteTrackFromCache(track);
                    }
                    i--;
                }
                else {
                    return "Недостаточно свободного места для кеширования новых треков. \n Прослушанные треки для удаления отсутствуют. \n";
                }
            }
        }
        return "Кеширование треков";
    }

    public static long FolderSize(File directory) {
        long length = 0;
        try {
            if(directory.listFiles() != null) {
                for (File file : directory.listFiles()) {
                    if (file.isFile())
                        length += file.length();
                    else
                        length += FolderSize(file);
                }
            }
        }catch (Exception ex){
            return ex.hashCode();
        }
        return length;
    }
}
