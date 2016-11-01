package ru.netvoxlab.ownradio;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

/**
 * Created by a.polunina on 21.10.2016.
 */

public class ExecuteProcedurePostgreSQL {
    Context MainContext;

    public ExecuteProcedurePostgreSQL(Context context) {
        this.MainContext = context;
    }

    //Сохраняет нового пользователя и устройство
    public void RegisterDevice(String deviceID, String userName, String deviceName)
    {
        CheckConnection checkConnection = new CheckConnection();
        boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
        if(!inetConnect)
            return; // "Подключение к интернету отсутствует";
        try
        {
            URL URLRequest = new URL("http://ownradio.ru/api/ExecuteProcedurePostgreSQL/RegisterDevice/" + deviceID + "," + userName + "," + deviceName);
            String str = new GetRequest().execute(URLRequest).get();
//            Toast.makeText(MainContext, str, Toast.LENGTH_LONG).show();
        }
        catch (MalformedURLException | InterruptedException | ExecutionException ex) {
        }
    }

    //Выполняет слияние статистики прослушивания треков на разных устройствах по двум User ID одного пользователя
    public String MergeUserID(String userIDOld, String userIDNew) {
        CheckConnection checkConnection = new CheckConnection();
        boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
        if(!inetConnect)
            return "Подключение к интернету отсутствует";
        try {
            URL URLRequest = new URL("http://ownradio.ru/api/ExecuteProcedurePostgreSQL/MergeUserID/" + userIDOld + "," + userIDNew);
            String result = new GetRequest().execute(URLRequest).get();
            return result;

        } catch (MalformedURLException | InterruptedException | ExecutionException ex) {
            return ex.getLocalizedMessage();
        } catch (IOException ex) {
            return ex.getLocalizedMessage();
        }
    }


    //Получает ID пользователя по DeviceID
    public String GetUserId(String deviceID)
    {
        CheckConnection checkConnection = new CheckConnection();
        boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
        if(!inetConnect)
            return "Подключение к интернету отсутствует";
        try {
            URL URLRequest = new URL("http://ownradio.ru/api/ExecuteProcedurePostgreSQL/GetUserId/" + deviceID);
            String result = new GetRequest().execute(URLRequest).get();
            String userID = result.substring(1, 37); //сделать адекватный парсинг
            return userID;
//            return deviceID;
        } catch (MalformedURLException | InterruptedException | ExecutionException ex) {
            return ex.getLocalizedMessage();
        } catch (IOException ex) {
            return ex.getLocalizedMessage();
        }
    }

    //Переименовывает пользователя
    public String RenameUser(String userID, String newUserName)
    {
        CheckConnection checkConnection = new CheckConnection();
        boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
        if(!inetConnect)
            return "Подключение к интернету отсутствует";
        try {
            URL URLRequest = new URL("http://ownradio.ru/api/ExecuteProcedurePostgreSQL/RenameUser/" + userID + "," + newUserName);
            String result = new GetRequest().execute(URLRequest).get();
            return result;
        } catch (MalformedURLException | InterruptedException | ExecutionException ex) {
            return ex.getLocalizedMessage();
        } catch (IOException ex) {
            return ex.getLocalizedMessage();
        }
    }

    //Получает ID следующего трека
    public String GetNextTrackID(String deviceId)
    {
        CheckConnection checkConnection = new CheckConnection();
        boolean wifiConnect = checkConnection.CheckWifiConnection(MainContext);
        if(!wifiConnect)
            return "Подключение к интернету отсутствует";
        try {
            URL URLRequest = new URL("http://ownradio.ru/api/track/GetNextTrackID/" + deviceId);
String result = new GetRequest().execute(URLRequest).get();
            String trackID = result.substring(1, 37); //сделать адекватный парсинг
            return trackID;
        } catch (MalformedURLException | InterruptedException | ExecutionException ex) {
            return ex.getLocalizedMessage();
        } catch (IOException ex) {
            return ex.getLocalizedMessage();
        }
    }

    //Отправляет статистику на сервер
    public String SetStatusTrack(String deviceId, String trackId, int IsListen, String DateTimeListen)
    {
        CheckConnection checkConnection = new CheckConnection();
        boolean inetConnect = checkConnection.CheckInetConnection(MainContext);
        if(!inetConnect)
            return "Подключение к интернету отсутствует";
        try {
            URL URLRequest = new URL("http://ownradio.ru/api/track/SetStatusTrack/" + deviceId + "," + trackId + "," + IsListen + "," + DateTimeListen);//("dd.MM.yyyy HH:mm:sszz"));
            String result = new GetRequest().execute(URLRequest).get();
            String trackID = result.substring(1, 37); //сделать адекватный парсинг
            return trackID;
        } catch (MalformedURLException | InterruptedException | ExecutionException ex) {
            return ex.getLocalizedMessage();
        } catch (IOException ex) {
            return ex.getLocalizedMessage();
        }
    }
}