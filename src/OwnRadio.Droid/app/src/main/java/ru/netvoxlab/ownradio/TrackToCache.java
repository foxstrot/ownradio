package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.Map;

import static ru.netvoxlab.ownradio.MediaPlayerService.queue;
import static ru.netvoxlab.ownradio.MediaPlayerService.queueSize;


/**
 * Created by a.polunina on 24.10.2016.
 */

public class TrackToCache {
	Context mContext;
	private final int EXTERNAL_STORAGE_NOT_AVAILABLE = -1;
	private final int DOWNLOAD_FILE_TO_CACHE = 1;
	private final int DELETE_FILE_FROM_CACHE = 2;
	final String TAG = "ownRadio";
//	public List<Map<String,String>> queue;

	public TrackToCache(Context context) {
		mContext = context;
	}

	public String SaveTrackToCache(String deviceId, int trackCount) {
		final TrackDataAccess trackDataAccess = new TrackDataAccess(mContext);

		APICalls apiCalls = new APICalls(mContext);

		for (int i = 0; i < trackCount; i++) {
			final String trackId;
			int flag = CheckCacheDoing();
			switch (flag) {
				case EXTERNAL_STORAGE_NOT_AVAILABLE:
					return "Директория на карте памяти недоступна";

				case DOWNLOAD_FILE_TO_CACHE: {
					CheckConnection checkConnection = new CheckConnection();
					boolean internetConnect = checkConnection.CheckInetConnection(mContext);
//					boolean wifiConnect = checkConnection.CheckWifiConnection(mContext);
					if (!internetConnect)
						return "Подключение к интернету отсутствует";

					try {
						final Map<String, String> trackMap = apiCalls.GetNextTrackID(deviceId);


						trackId = trackMap.get("id");
						if (trackDataAccess.CheckTrackExistInDB(trackId)) {
							queueSize--;
							Log.d(TAG, "Трек был загружен ранее. TrackID" + trackId);
							break;
						}
						queue.add(trackMap);

						boolean res = new DownloadTracks(mContext).execute(trackMap).get();
						//Загружаем трек и сохраняем информацию о нем в БД
//						final String trackURL = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + trackId + ".mp3";


//						ServiceGenerator.createService(APIService.class).getTrackById(trackId).enqueue(new Callback<ResponseBody>() {
//							@Override
//							public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//								if (response.isSuccessful()) {
//									Log.d(TAG, "server contacted and has file");
//									try {
//										boolean writtenToDisk = new WriteTrackToDisk(mContext, trackURL).execute(response.body()).get();
////										File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + trackId + ".mp3");
//										if(writtenToDisk == true){
//											ContentValues track = new ContentValues();
//											track.put("id", trackId);
//											track.put("trackurl", trackURL);
//											track.put("datetimelastlisten", "");
//											track.put("islisten", "0");
//											track.put("isexist", "1");
//											try {
//												track.put("title", trackMap.get("name"));
//												track.put("artist", trackMap.get("artist"));
//												track.put("length", trackMap.get("length"));
//												track.put("methodid", trackMap.get("methodid"));
//											}catch (Exception ex){
//												Log.d(TAG, " " + ex.getLocalizedMessage());
//											}
//											trackDataAccess.SaveTrack(track);
//											Log.d(TAG, "File " + trackId + " is load, queque size = " + queue.size());
//											queue.remove(queue.indexOf(trackMap));
//											queueSize--;
//
//											Intent i = new Intent(ActionTrackInfoUpdate);
//											mContext.sendBroadcast(i);
//										}
//									} catch (Exception ex) {
//										queueSize--;
//									}
//								} else {
//									Log.d("", "server contact failed");
//									queue.remove(queue.indexOf(trackMap));
//									queueSize--;
//								}
//							}
//
//							@Override
//							public void onFailure(Call<ResponseBody> call, Throwable t) {
//								queue.remove(queue.indexOf(trackMap));
//								queueSize--;
//								if (call.isCanceled()) {
//									Log.e(TAG, "request was cancelled");
//								}
//								else {
//									Log.e(TAG, "other larger issue, i.e. no network connection?");
//								}
//							}
//						});


						Log.d(TAG, "Кеширование начато");
					} catch (Exception ex) {
						Log.d(TAG, "Error in SaveTrackToCache at file download. Ex.mess:" + ex.getLocalizedMessage());
						return ex.getLocalizedMessage();
					}
					break;
				}

				case DELETE_FILE_FROM_CACHE: {
					ContentValues track = trackDataAccess.GetTrackForDel();
					if(track == null)
						return "Отсутствуют файлы для удаления";
					if(DeleteTrackFromCache(track))
						i--;
					break;
				}
			}
		}
		return "Кеширование треков";
	}

	public static long FolderSize(File directory) {
		long length = 0;
		try {
			if (directory.listFiles() != null) {
				for (File file : directory.listFiles()) {
					if (file.isFile())
						length += file.length();
					else
						length += FolderSize(file);
				}
			}
		} catch (Exception ex) {
			return ex.hashCode();
		}
		return length;
	}

	public void ScanTrackToCache() {
		try {
			File directory = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
			if (directory.listFiles() != null) {
				ContentValues track = new ContentValues();
				for (File file : directory.listFiles()) {
					if (file.isFile()) {
						track.put("id", file.getName().substring(0, 36));
						track.put("trackurl", file.getAbsolutePath());
						track.put("datetimelastlisten", "");
						track.put("islisten", "0");
						track.put("isexist", "1");
						new TrackDataAccess(mContext).SaveTrack(track);
					}
				}
//				Intent i = new Intent(ActionTrackInfoUpdate);
//				mContext.sendBroadcast(i);
			}
		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}
	}

	public int CheckCacheDoing(){
			File[] externalStoragesPaths = ContextCompat.getExternalFilesDirs(mContext, null);
			File externalStoragePath;
			if (externalStoragesPaths == null)
				return EXTERNAL_STORAGE_NOT_AVAILABLE;

			externalStoragePath = externalStoragesPaths[0];
			long availableSpace = 0;
			long cacheSize = FolderSize(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC));
			if (Build.VERSION.SDK_INT <= 17) {
				StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
				availableSpace = (long) stat.getFreeBlocks() * (long) stat.getBlockSize();
				Log.d(TAG, "availableSpace :" + availableSpace / 1048576);
			}
			if (Build.VERSION.SDK_INT >= 18) {
				availableSpace = new StatFs(externalStoragePath.getPath()).getAvailableBytes();
				Log.d(TAG, "availableSpace :" + availableSpace / 1048576);
			}

			if (cacheSize < (cacheSize + availableSpace) * 0.3)
				return DOWNLOAD_FILE_TO_CACHE;
			else
				return DELETE_FILE_FROM_CACHE;
	}

	public boolean DeleteTrackFromCache(ContentValues track){
		TrackDataAccess trackDataAccess = new TrackDataAccess(mContext);
		try {
			if (track == null) {
				Log.d(TAG, "Отсутствует файл для удаления.");
				return false;
			}
				File file = new File(track.getAsString("trackurl"));
//				File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + file1.getName());
				if (file.exists()) {
					if (file.delete()) {
						trackDataAccess.DeleteTrackFromCache(track);
//						Intent i = new Intent(ActionTrackInfoUpdate);
//						mContext.sendBroadcast(i);
						Log.d(TAG, "File is deleted");
						return true;
					}
					Log.d(TAG, "File not deleted. Something error");
					return false;
				} else {
					trackDataAccess.DeleteTrackFromCache(track);
					Log.d(TAG, "File for delete is not exist. Rec about track deleted from DB");
					return false;
				}

		} catch (Exception ex) {
			Log.d(TAG, "Error in SaveTrackToCache at file delete. Ex.mess:" + ex.getLocalizedMessage());
			return false;
		}
	}
}
