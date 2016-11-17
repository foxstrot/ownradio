package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by a.polunina on 15.11.2016.
 */

public class RequestToAPI {

	Context mContext;

	void RequestToAPI(Context context){
		mContext = context;
	}
//	String DeviceID = PreferenceManager.getDefaultSharedPreferences(this).getString("DeviceID", "");

	APIService apiService = ServiceGenerator.createService(APIService.class);
	final String TAG = "ownRadio";

//	public String GetNextTrackID(String deviceId) {
//		Call<String> call = apiService.GetNextTrackID();
//		try {
//			String trackId = call.execute().body().substring(1, 37);
//			//UUID trackUuid = UUID.fromString(trackId).;
//			return trackId;
//		} catch (Exception ex) {
//			return null;
//		}
//	}




	public void DownloadTrackToCache(final String trackId){
		final APIService apiService = ServiceGenerator.createService(APIService.class);

		Call<ResponseBody> call = apiService.DownloadTrackToCache(trackId);//"http://www.montemagno.com/sample.mp3");
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Log.d(TAG, "server contacted and has file");

					boolean writtenToDisk = writeResponseBodyToDisk(response.body());

					Log.d(TAG, "file download was a success? " + writtenToDisk);
				} else {
					Log.d(TAG, "server contact failed");
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Log.e(TAG, "error");
			}
		});
	}

	private boolean writeResponseBodyToDisk(ResponseBody body) {
		try {
			// todo change the file location/name according to your needs
			File futureStudioIconFile = new File(mContext.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + "Future Studio Icon.png");

			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				byte[] fileReader = new byte[4096];

				long fileSize = body.contentLength();
				long fileSizeDownloaded = 0;

				inputStream = body.byteStream();
				outputStream = new FileOutputStream(futureStudioIconFile);

				while (true) {
					int read = inputStream.read(fileReader);

					if (read == -1) {
						break;
					}

					outputStream.write(fileReader, 0, read);

					fileSizeDownloaded += read;

					Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
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

//		Call<Void> call = apiService.DownloadTrackToCache(trackId);
//
//		call.enqueue(new Callback<Void>() {
//			@Override
//			public void onResponse(Call<Void> call, Response<Void> response) {
//				if(response.isSuccessful()) {
//					File file = new File(trackId + ".mp3");
//					try {
//						file.createNewFile();
//						Files.asByteSink(file).write(response.body().);
//					}catch (Exception ex){
//
//					}
//				}
//			}
//
//			@Override
//			public void onFailure(Call<Void> call, Throwable t) {
//
//			}
//		});
//	}


}
