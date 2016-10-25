package ru.netvoxlab.ownradio;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import static android.support.v7.widget.AppCompatDrawableManager.get;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class GetTrack {
    private long downloadReference;
    private DownloadManager downloadManager;


    public GetTrack() {
    }

    public String GetTrackByID(Context context, String trackId) {
        try {
            Uri downloadURL = Uri.parse("http://ownradio.ru/api/track/GetTrackByID/" + trackId);
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("trackid_", ".mp3", outputDir);
            String inStr[] = {downloadURL.toString(), outputFile.toString()};
//            String str =  new GetTrackFile().execute(inStr).get();
         //  new GetTrackFile().execute(inStr);

            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            context.registerReceiver(onComplete, filter);

            DownloadManager.Request request = new DownloadManager.Request(downloadURL);
            //Загрузка треков осуществляется только через Wi-Fi
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            //Заголовок для вывода в уведомление
            request.setTitle("Кэширование треков");
            //Локальное имя трека
            String fileName = "trackid_" + trackId + ".mp3";
            //File fileName = File.createTempFile("trackid_",".mp3", new File(Environment.DIRECTORY_DOWNLOADS));
            //Задаем директорию для кэширования треков во внешней памяти в папке приложения
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);
            //Ставим запрос на загрузку в очередь
            downloadReference = downloadManager.enqueue(request);


            return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();// + "/" + fileName;
//            return  outputFile.getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Download OK", Toast.LENGTH_LONG).show();
        }
    };
}




