package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

	public void CleanTrackTable() {
		db = trackDB.getWritableDatabase();
		db.execSQL("DELETE FROM " + TrackTableName);
//        db.close();
	}

	public void SaveTrack(ContentValues trackInstance) {
		db = trackDB.getWritableDatabase();
		//Получаем текущее время - 10 лет
		Cursor userCursor  = db.rawQuery("SELECT strftime('%Y-%m-%dT%H:%M:%S', 'now','-10 year')", null);
		if(userCursor.moveToFirst())
			trackInstance.put("datetimelastlisten", userCursor.getString(0));
		trackInstance.put("countplay", 0);
		db.insert(TrackTableName, null, trackInstance);
//        db.close();
	}

	public boolean CheckTrackExistInDB(String trackId) {
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT * FROM track WHERE id = ? AND isexist =?", new String[]{String.valueOf(trackId), String.valueOf(1)});
		boolean result = true;
//        db.close();
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
		Cursor userCursor = db.rawQuery("SELECT id, trackurl, title, artist, methodid, length FROM track WHERE isexist = ? ORDER BY datetimelastlisten", new String[]{String.valueOf(1)});
		if (userCursor.moveToFirst()) {
			result.put("id", userCursor.getString(0));
			result.put("trackurl", userCursor.getString(1));
			result.put("title", userCursor.getString(2));
			result.put("artist", userCursor.getString(3));
			result.put("methodid", userCursor.getInt(4));
			result.put("length", userCursor.getInt(5));
		} else {
			result = null;
		}
//        db.close();
		userCursor.close();
		return result;
	}

	public ContentValues GetTrackForDel() {
		ContentValues result = new ContentValues();
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT id, trackurl FROM track WHERE islisten != ? AND isexist = ? AND datetimelastlisten != ? ORDER BY datetimelastlisten", new String[]{String.valueOf(0), String.valueOf(1), String.valueOf("")});
		if (userCursor.moveToFirst()) {
			result.put("id", userCursor.getString(0));
			result.put("trackurl", userCursor.getString(1));
		} else {
			result = null;
		}
//        db.close();
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
//        db.close();
		userCursor.close();
		return result;
	}

	public ContentValues GetPathById(String trackid) {
		ContentValues result = new ContentValues();
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT id, trackurl FROM track WHERE id = ?", new String[]{String.valueOf(trackid)});
		if (userCursor.moveToFirst()) {
			result.put("id", userCursor.getString(0));
			result.put("trackurl", userCursor.getString(1));
		} else {
			result = null;
		}
//        db.close();
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
//        db.close();
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

	}

	public void SetStatusTrack(ContentValues trackInstance) {
		int isListen = Integer.parseInt(trackInstance.get("islisten").toString());
		int countPlay = 1;
		db = trackDB.getWritableDatabase();
		//Убрать islisten//
		Cursor userCursor = db.rawQuery("SELECT islisten FROM track WHERE id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		if (userCursor.moveToFirst()) {
			isListen += userCursor.getInt(0);
		}
		trackInstance.put("islisten", isListen);
		//Убрать islisten//

		long rowID = db.update(TrackTableName, trackInstance, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		userCursor.close();
//        db.close();
	}

	public String GetCountPlayTracks(){
		Cursor userCursor = db.rawQuery("SELECT COUNT(*) AS tracks, countplay FROM track WHERE isexist = ?GROUP BY countplay ORDER BY countplay DESC", new String[]{String.valueOf(1)});

		String tableString = null;
		if (userCursor.moveToFirst() ){
			String[] columnNames = userCursor.getColumnNames();
			tableString = columnNames[0] + " | " + columnNames[1] + "\n";
			do {
				tableString += userCursor.getInt(userCursor.getColumnIndex(columnNames[0])) + ": \t" +
						userCursor.getInt(userCursor.getColumnIndex(columnNames[1]));
				tableString += "\n";

			} while (userCursor.moveToNext());
		} else
			tableString = "Информация не найдена";
		return tableString;
	}

	//Функция меняет флаг isExist на 0 для удаленного трека
	public void DeleteTrackFromCache(ContentValues trackInstance) {
		int isExist = 0;
		trackInstance.put("isexist", isExist);
		db = trackDB.getWritableDatabase();
		db.update(TrackTableName, trackInstance, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
//        db.close();
	}
}
