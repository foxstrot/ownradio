package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by a.polunina on 26.10.2016.
 */

public class TrackDataAccess {
	Context mContext;
	SQLiteDatabase db;
	TrackDB trackDB;
	int dbVer = R.string.db_ver;
	private final String TrackTableName = "track";

	public TrackDataAccess(Context context) {
		mContext = context;
		trackDB = new TrackDB(mContext, dbVer);

	}

	public void CleanTrackTable() {
		db = trackDB.getWritableDatabase();
		db.execSQL("DELETE FROM " + TrackTableName);
//        db.close();
	}

	public void SaveTrack(ContentValues trackInstance) {
		db = trackDB.getWritableDatabase();
		long rowID = db.insert(TrackTableName, null, trackInstance);
//        db.close();
	}

	public void UpdateTrack(ContentValues trackInstance) {
		db = trackDB.getWritableDatabase();
		long rowID = db.update(TrackTableName, trackInstance, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
	}

	public boolean CheckTrackExistInDB(String trackId) {
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT * FROM track WHERE id = ?", new String[]{String.valueOf(trackId)});
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
		db = trackDB.getReadableDatabase();
		Cursor userCursor = db.rawQuery("SELECT id, trackurl FROM track WHERE isexist = ? ORDER BY datetimelastlisten", new String[]{String.valueOf(1)});
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

	public ContentValues GetTrackForDel() {
		ContentValues result = new ContentValues();
		db = trackDB.getReadableDatabase();
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

	public int GetExistTracksCount() {
		int result;
		db = trackDB.getReadableDatabase();
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

	public void SetStatusTrack(ContentValues trackInstance) {
		int isListen = Integer.parseInt(trackInstance.get("islisten").toString());
		db = trackDB.getWritableDatabase();
		Cursor userCursor = db.rawQuery("SELECT islisten FROM track WHERE id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		if (userCursor.moveToFirst()) {
			isListen += userCursor.getInt(0);
		}
		trackInstance.put("islisten", isListen);
		long rowID = db.update(TrackTableName, trackInstance, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
		userCursor.close();
//        db.close();
	}

	public void DeleteTrackFromCache(ContentValues trackInstance) {
		int isExist = 0;
		trackInstance.put("isexist", isExist);
		db = trackDB.getWritableDatabase();
		db.update(TrackTableName, trackInstance, "id = ?", new String[]{String.valueOf(trackInstance.get("id"))});
//        db.close();
	}
}
