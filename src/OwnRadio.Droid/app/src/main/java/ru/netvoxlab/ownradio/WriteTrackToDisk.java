package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by a.polunina on 11.12.2016.
 */

public class WriteTrackToDisk extends AsyncTask<ResponseBody, Long, Boolean> {
	Context mContext;
	String trackURL;

	public WriteTrackToDisk(Context context, String trackURL) {
		this.mContext = context;
		this.trackURL = trackURL;
	}
	@Override
	protected Boolean doInBackground(ResponseBody... body) {
		try {
			File futureMusicFile = new File(trackURL);

			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				byte[] fileReader = new byte[4096];

				long fileSize = body[0].contentLength();
				long fileSizeDownloaded = 0;

				inputStream = body[0].byteStream();
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

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
	}
}
