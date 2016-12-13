package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static ru.netvoxlab.ownradio.MainActivity.ActionTrackInfoUpdate;
import static ru.netvoxlab.ownradio.MediaPlayerService.queue;
import static ru.netvoxlab.ownradio.MediaPlayerService.queueSize;

/**
 * Created by a.polunina on 13.12.2016.
 */

public class DownloadTracks extends AsyncTask<Map<String, String> , Void, Boolean> {
	Context mContext;
	Boolean flag;

	public DownloadTracks(Context context) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(Map<String, String>... trackMap) {


		try {
			Response<ResponseBody> response = ServiceGenerator.createService(APIService.class).getTrackById(trackMap[0].get("id")).execute();
			if (response.isSuccessful()) {
				Log.d(TAG, "server contacted and has file");
				final String trackURL = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + trackMap[0].get("id") + ".mp3";
				boolean writtenToDisk = WriteTrackToDisk2(trackURL, response.body());
//										File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + trackId + ".mp3");
				if (writtenToDisk == true) {
					ContentValues track = new ContentValues();
					track.put("id", trackMap[0].get("id"));
					track.put("trackurl", trackURL);
					track.put("datetimelastlisten", "");
					track.put("islisten", "0");
					track.put("isexist", "1");
					try {
						track.put("title", trackMap[0].get("name"));
						track.put("artist", trackMap[0].get("artist"));
						track.put("length", trackMap[0].get("length"));
						track.put("methodid", trackMap[0].get("methodid"));
					} catch (Exception ex) {
						Log.d(TAG, " " + ex.getLocalizedMessage());
					}
					new TrackDataAccess(mContext).SaveTrack(track);
					Log.d(TAG, "File " + trackMap[0].get("id") + " is load, queque size = " + queue.size());
					queue.remove(trackMap[0]);
					queueSize--;

					Intent in = new Intent(ActionTrackInfoUpdate);
					mContext.sendBroadcast(in);
					return true;
				}
			} else {
				Log.d("", "server contact failed");
				queue.remove(trackMap[0]);
				queueSize--;
			}
		} catch (Exception ex) {
			queueSize--;
		}
		return false;
	}


	@Override
	protected void onPostExecute(Boolean o) {
		super.onPostExecute(o);
	}

	public boolean WriteTrackToDisk2(String trackURL, ResponseBody body) {

		try {
			File futureMusicFile = new File(trackURL);

			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				byte[] fileReader = new byte[4096];

				long fileSize = body.contentLength();
				long fileSizeDownloaded = 0;

				inputStream = body.byteStream();
				outputStream = new FileOutputStream(futureMusicFile);

				while (true) {
					int read = inputStream.read(fileReader);

					if (read == -1) {
						break;
					}

					outputStream.write(fileReader, 0, read);

					fileSizeDownloaded += read;

//					Log.d("", "file download: " + fileSizeDownloaded + " of " + fileSize);
				}

				outputStream.flush();

				return true;
			} catch (IOException e) {
				return false;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}

				if (outputStream != null) {
					outputStream.close();
				}
			}
		} catch (IOException e) {
			return false;
		}
	}
}