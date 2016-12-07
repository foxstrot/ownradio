package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.UUID;

/**
 * Created by a.polunina on 07.11.2016.
 */

public class HistoryDataAccess {
	Context mContext;
	SQLiteDatabase db;
	TrackDB historyDB;
	int dbVer = R.string.db_ver;

	private final String HistoryTableName = "history";

	public HistoryDataAccess(Context context) {
		mContext = context;
		historyDB = new TrackDB(mContext, dbVer);
	}

	public void SaveHistoryRec(ContentValues historyInstance){
		db = historyDB.getWritableDatabase();
		historyInstance.put("id", UUID.randomUUID().toString());
		db.insert(HistoryTableName, null, historyInstance);
	}

	public ContentValues GetHistoryRec(){
		db = historyDB.getReadableDatabase();
		ContentValues contentValues = new ContentValues();
		Cursor userCursor = db.rawQuery("SELECT * FROM " + HistoryTableName + " LIMIT 1", null);
		if (userCursor.moveToFirst()) {
			String data;
//			String result = "id=" + userCursor.getString(0);
//			result += "&" + "trackid=" + userCursor.getString(1);
//			result += "&" + "userid=" + userCursor.getString(2);
			data = "{";
			data += "\"lastListen\":\"" + userCursor.getString(3) + "\",";
			data += "\"isListen\":" + userCursor.getInt(4) + "," ;
			data += "\"methodid\":" + userCursor.getInt(5);
			data +="}";
			contentValues.put("id", userCursor.getString(0));
			contentValues.put("trackid", userCursor.getString(1));
			contentValues.put("data", data);

			userCursor.close();
			return contentValues;
		}else {
			return null;
		}
	}

	public void DeleteHistoryRec(String id){
		db = historyDB.getWritableDatabase();
		db.delete(HistoryTableName, "id = ?", new String[]{id});
	}

	public void CleanHistoryTable(){
		db = historyDB.getWritableDatabase();
		db.execSQL("DELETE FROM " + HistoryTableName);
	}
}
