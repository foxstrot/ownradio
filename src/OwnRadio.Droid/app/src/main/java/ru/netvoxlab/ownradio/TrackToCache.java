package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Map;

import static ru.netvoxlab.ownradio.MainActivity.ActionProgressBarFirstTracksLoad;
import static ru.netvoxlab.ownradio.MainActivity.ActionTrackInfoUpdate;
import static ru.netvoxlab.ownradio.MainActivity.filePath;

/**
 * Created by a.polunina on 24.10.2016.
 */

public class TrackToCache {
	Context mContext;
	File pathToCache;
	private final int EXTERNAL_STORAGE_NOT_AVAILABLE = -1;
	private final int DOWNLOAD_FILE_TO_CACHE = 1;
	private final int DELETE_FILE_FROM_CACHE = 2;
	final String TAG = "ownRadio";

	public TrackToCache(Context context) {
		mContext = context;
		pathToCache = filePath;
	}

	public String SaveTrackToCache(String deviceId, int trackCount) {
		if (!new CheckConnection().CheckInetConnection(mContext))
			return "Подключение к интернету отсутствует";

		final TrackDataAccess trackDataAccess = new TrackDataAccess(mContext);

		APICalls apiCalls = new APICalls(mContext);

		for (int i = 0; i < trackCount; i++) {
			final String trackId;
			int flag = CheckCacheDoing();
			switch (flag) {
				case EXTERNAL_STORAGE_NOT_AVAILABLE:
					return "Директория на карте памяти недоступна";

				case DOWNLOAD_FILE_TO_CACHE: {
					try {
						final Map<String, String> trackMap = apiCalls.GetNextTrackID(deviceId);
						trackId = trackMap.get("id");
						if (trackDataAccess.CheckTrackExistInDB(trackId)) {
							Log.d(TAG, "Трек был загружен ранее. TrackID" + trackId);
							break;
						}
						new Utilites().SendInformationTxt(mContext, "Download track \"" + trackId + "\" is started");
						boolean res = new DownloadTracks(mContext).execute(trackMap).get();
					} catch (Exception ex) {
						Log.d(TAG, "Error in SaveTrackToCache at file download. Ex.mess:" + ex.getLocalizedMessage());
						return " " + ex.getLocalizedMessage();
					}
					break;
				}

				case DELETE_FILE_FROM_CACHE: {
					ContentValues track = trackDataAccess.GetTrackWithMaxCountPlay();
					if(track == null)
						return "Отсутствуют файлы для удаления";
					DeleteTrackFromCache(track);
					break;
				}
			}
		}
		Intent i = new Intent(ActionProgressBarFirstTracksLoad);
		i.putExtra("ProgressOn", false);
		mContext.sendBroadcast(i);
		return "Кеширование треков завершено";
	}

	//функция возвращает количество памяти, занимаемое кешированными треками
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

	//функция возвращает количество треков в папке
	public int TrackCountInFolder(File directory){
		int count = 0;
		try {
			if (directory.listFiles() != null) {
				for (File file : directory.listFiles()) {
					if (file.isFile())
						if (FilenameUtils.getExtension(file.getPath()).equals("mp3"))
							count ++;
					else
						count += FolderSize(file);
				}
			}
		} catch (Exception ex) {
			return ex.hashCode();
		}
		return count;
	}

	//функция возвращает свободное количество памяти
	public long FreeSpace(){
		long availableSpace = 0;
		if (Build.VERSION.SDK_INT <= 17) {
			StatFs stat = new StatFs(pathToCache.getPath());
			availableSpace = (long) stat.getFreeBlocks() * (long) stat.getBlockSize();
			Log.d(TAG, "availableSpace :" + availableSpace / 1048576);
		}
		if (Build.VERSION.SDK_INT >= 18) {
			availableSpace = new StatFs(pathToCache.getPath()).getAvailableBytes();
			Log.d(TAG, "availableSpace :" + availableSpace / 1048576);
		}
		return  availableSpace;
	}

	//функция сканирует директорию хранения треков, отсутствующие в БД - добавляет в неё
	public void ScanTrackToCache() {
		try {
			File directory = pathToCache;
			if (directory.listFiles() != null) {
				if(directory.listFiles().length == new TrackDataAccess(mContext).GetExistTracksCount())
					return;
				for (File file : directory.listFiles()) {
					if (file.isFile()) {
						if(!new TrackDataAccess(mContext).CheckTrackExistInDB(file.getName().substring(0, 36)))
							file.delete();
					}
				}
			}
		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}
	}

	public int CheckCacheDoing(){
		long cacheSize = FolderSize(pathToCache);


//		File[] externalStoragesPaths = ContextCompat.getExternalFilesDirs(mContext, null);
//		File externalStoragePath;
//		if (externalStoragesPaths == null)
//			return EXTERNAL_STORAGE_NOT_AVAILABLE;
//		externalStoragePath = externalStoragesPaths[0];


//		long availableSpace = FreeSpace(externalStoragePath);
		long availableSpace = FreeSpace();

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
			if (file.exists()) {
				if (file.delete()) {
					trackDataAccess.DeleteTrackFromCache(track);
					Log.d(TAG, "File is deleted");
					Intent in = new Intent(ActionTrackInfoUpdate);
					mContext.sendBroadcast(in);
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
