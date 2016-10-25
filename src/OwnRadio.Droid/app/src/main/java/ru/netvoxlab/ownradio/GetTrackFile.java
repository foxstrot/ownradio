package ru.netvoxlab.ownradio;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by a.polunina on 24.10.2016.
 */

public class GetTrackFile extends AsyncTask<String, Void, String>{

    protected String doInBackground(String... params){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        try {
//            Uri downloadURL = Uri.parse("http://ownradio.ru/api/track/GetTrackByID/" + trackId);
//            File outputDir = context.getCacheDir();
            File outputFile = new File(params[1]);//File.createTempFile("trackid_", ".mp3", outputDir);
            URL url = new URL(params[0]);
            //
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setDoOutput(true);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                InputStream inputStream = urlConnection.getInputStream();

                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
            }
            urlConnection.disconnect();
            return  "0";
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);
    }
}
