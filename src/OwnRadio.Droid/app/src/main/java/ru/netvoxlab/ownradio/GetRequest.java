package ru.netvoxlab.ownradio;

import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by a.polunina on 24.10.2016.
 */

public class GetRequest extends AsyncTask<URL, Void,  String> {
    private String strRes;


//    public RegisterDevice(String res){
//        this.strRes = res;
//    }

    protected String doInBackground(URL... url){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        try {
            URL URLRequest = url[0];
            HttpURLConnection urlConnection = (HttpURLConnection) URLRequest.openConnection();
//            request.UserAgent = "OwnRadioAndroidClient";
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //запрос успешно отправлен
                InputStream streamReader = new BufferedInputStream(urlConnection.getInputStream());
                StringBuffer sb = new StringBuffer();
                int ch;
                while ((ch = streamReader.read()) != -1) {
                    sb.append((char) ch);
                }
                urlConnection.disconnect();
                return sb.toString();
            }
            else
            {
                urlConnection.disconnect();
                return "1";

            }
//            urlConnection.disconnect();
//            return "Res";
        }
        catch (MalformedURLException ex){
            return ex.getLocalizedMessage();
        }
        catch(IOException ex){
            return ex.getLocalizedMessage();
        }
    }

    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);
        strRes = result;
//        strRes = result;
    }
}
