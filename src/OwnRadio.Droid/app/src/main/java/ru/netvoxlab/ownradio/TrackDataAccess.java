package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static ru.netvoxlab.ownradio.TrackDB.DB_VER;

/**
 * Created by a.polunina on 26.10.2016.
 */

public class TrackDataAccess {
	Context mContext;
	SQLiteDatabase db;
	TrackDB trackDB;
	private final String TrackTableName = "track";

	public TrackDataAccess(Context context) {
		mContext = context;
		trackDB = new TrackDB(mContext, DB_VER);
	}

	public void SaveTrack(ContentValues trackInstance) {
		db = trackDB.getWritableDatabase();
		//Получаем текущее время - 10 лет
		Cursor userCursor  = db.rawQuery("SELECT strftime('%Y-%m-%dT%H:%M:%S', 'now','-10 year')", null);
		if(userCursor.moveToFirst())
			trackInstance.put("datetimelastlisten", userCursor.getString(0));
		trackInstance.put("countplay", 0);
		db.insert(TrackTableName, null, trackInstance);
		//db.close();
	}

	public boolean CheckTrackExistInDB(String trackId) {
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT * FROM track WHERE id = ?", new String[]{String.valueOf(trackId)});
		boolean result = true;
		//db.close();
		if (userCursor.moveToFirst())
			result = true;
		else
			result = false;
		userCursor.close();
		return result;
	}

	public ContentValues GetMostOldTrack() {
		ContentValues result = new ContentValues();
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT id, trackurl, title, artist, length FROM track WHERE isexist = ? ORDER BY datetimelastlisten", new String[]{String.valueOf(1)});
		if (userCursor.moveToFirst()) {
			result.put("id", userCursor.getString(0));
			result.put("trackurl", userCursor.getString(1));
			result.put("title", userCursor.getString(2));
			result.put("artist", userCursor.getString(3));
			result.put("length", userCursor.getInt(4));
		} else {
			result = null;
		}
		//db.close();
		userCursor.close();
		return result;
	}

	public ContentValues GetTrackWithMaxCountPlay() {
		ContentValues result = new ContentValues();
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT id, trackurl FROM track WHERE countplay != ? AND isexist = ? ORDER BY countplay DESC LIMIT 1", new String[]{String.valueOf(0), String.valueOf(1)});
		if (userCursor.moveToFirst()) {
			result.put("id", userCursor.getString(0));
			result.put("trackurl", userCursor.getString(1));
		} else {
			result = null;
		}
		//db.close();
		userCursor.close();
		return result;
	}

	public int GetExistTracksCount() {
		int result;
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT COUNT(*) FROM track WHERE isexist = ? ORDER BY datetimelastlisten", new String[]{String.valueOf(1)});
		if (userCursor.moveToFirst()) {
			result = userCursor.getInt(0);
		} else {
			result = 0;
		}
		//db.close();
		userCursor.close();
		return result;
	}

	//Функция сохраняет время начала попытки проигрывания трека и увеличивает счетчик попыток проигрывания на 1
	public void SetTimeAndCountStartPlayback(ContentValues trackInstance){
		int countPlay = 1;
		String currentDatetime = "2000-01-01T12:00:00";

		db = trackDB.getWritableDatabase();

		//Получаем из базы количетво попыток прослушивания трека
		Cursor userCursorCount = db.rawQuery("SELECT countplay FROM track WHERE id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		if (userCursorCount.moveToFirst()) {
			countPlay += userCursorCount.getInt(0);
		}
		trackInstance.put("countplay", countPlay);
		userCursorCount.close();
		//Получаем текущее время
		Cursor userCursorTime = db.rawQuery("SELECT strftime('%Y-%m-%dT%H:%M:%S', 'now')", null);
		if(userCursorTime.moveToFirst())
			currentDatetime = userCursorTime.getString(0);
		userCursorTime.close();
			trackInstance.put("datetimelastlisten", currentDatetime);
//		db.rawQuery("UPDATE track SET datetimelastlisten=?, countplay=? WHERE id = ?", new String[]{String.valueOf(currentDatetime), String.valueOf(trackInstance.get("id")), String.valueOf(countPlay)});
		int row = db.update(TrackTableName, trackInstance, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		//db.close();
	}

	public String GetCountPlayTracksTable(){
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT countplay, COUNT(*) AS tracks FROM track GROUP BY countplay ORDER BY countplay DESC", null);

		String tableString = "";
		if (userCursor.moveToFirst() ){
			String[] columnNames = userCursor.getColumnNames();
//			tableString = columnNames[0] + " | " + columnNames[1] + "\n";
			do {
				tableString += userCursor.getInt(userCursor.getColumnIndex(columnNames[0])) + " : \t" +
						userCursor.getInt(userCursor.getColumnIndex(columnNames[1]));
				tableString += "\n";

			} while (userCursor.moveToNext());
		} else
			tableString = mContext.getResources().getString(R.string.count_tracks_listened_table);
		userCursor.close();
		//db.close();
		return tableString;
	}
	
	//возвращает количество прослушанных треков
	public long GetCountPlayTracks(){
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT COUNT(*) FROM track WHERE countplay > ?", new String[]{String.valueOf(0)});
		long listeningTracksCount = 0;
		if (userCursor.moveToFirst() ){
			listeningTracksCount = userCursor.getLong(0);
		} else
			listeningTracksCount = 0;
		userCursor.close();
		//db.close();
		return listeningTracksCount;
	}
	
	//возвращает список id прослушанных треков
	public List<File> GetUuidsListeningTracks(){
		db = trackDB.getReadableDatabase();
		Cursor userCursor = db.rawQuery("SELECT id FROM track WHERE countplay > ?", new String[]{String.valueOf(0)});
		List<File> listListeningTracks = new ArrayList<>();
		if(userCursor.moveToFirst()){
			do {
				listListeningTracks.add(new File (((App)mContext).getMusicDirectory() + "/" + userCursor.getString(0) + ".mp3"));
			}while (userCursor.moveToNext());
		}else {
			return null;
		}
		return listListeningTracks;
	}
	
	//Функция удаляет запись о треке из БД
	public int DeleteTrackFromCache(ContentValues trackInstance) {
		db = trackDB.getWritableDatabase();
		return db.delete(TrackTableName, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		//db.close();
	}
	
	//Функция удаляет записи обо всех треках
	public int DeleteAllTracksFromCache() {
		db = trackDB.getWritableDatabase();
		return db.delete(TrackTableName, null, null);
		//db.close();
	}
	
	//Функция удаляет записи о прослушанных треках
	public int DeleteListenedTracksFromCache() {
		db = trackDB.getWritableDatabase();
		return db.delete(TrackTableName, "countplay > ?", new String[]{String.valueOf(0)});
		//db.close();
	}
}
