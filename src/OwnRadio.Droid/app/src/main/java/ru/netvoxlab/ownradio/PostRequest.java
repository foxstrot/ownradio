package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by a.polunina on 07.11.2016.
 */

public class PostRequest extends AsyncTask<String, Void, Integer> {
	int ResponseCode;
	public Context mContext;
	public PostRequest(Context context){
		mContext = context;
	}
	String recid;

	@Override
	protected Integer doInBackground(String... data) {
		recid = data[2];//id строки истории
		try {
			URL urlRequest = new URL(data[0]);
			HttpURLConnection urlConnection = (HttpURLConnection) urlRequest.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
			out.write(data[1]);
			out.flush();

			ResponseCode = urlConnection.getResponseCode();

		} catch (MalformedURLException e) {
			e.getLocalizedMessage();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	return ResponseCode;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
	}
}
