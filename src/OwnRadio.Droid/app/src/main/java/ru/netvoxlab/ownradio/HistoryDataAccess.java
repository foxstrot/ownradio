package ru.netvoxlab.ownradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.UUID;

import static ru.netvoxlab.ownradio.TrackDB.DB_VER;

/**
 * Created by a.polunina on 07.11.2016.
 */

public class HistoryDataAccess {
	Context mContext;
	SQLiteDatabase db;
	TrackDB historyDB;

	private final String HistoryTableName = "history";

	public HistoryDataAccess(Context context) {
		mContext = context;
		historyDB = new TrackDB(mContext, DB_VER);
	}

	public void SaveHistoryRec(ContentValues historyInstance){
		db = historyDB.getWritableDatabase();
		historyInstance.put("id", UUID.randomUUID().toString());
		db.insert(HistoryTableName, null, historyInstance);
//		db.close();
	}

	public ContentValues GetHistoryRec(){
		db = historyDB.getReadableDatabase();
		ContentValues contentValues = new ContentValues();
		Cursor userCursor = db.rawQuery("SELECT * FROM " + HistoryTableName + " LIMIT 1", null);
		if (userCursor.moveToFirst()) {
//			String data;
//			data = "{";
//			data += "\"lastListen\":\"" + userCursor.getString(3) + "\",";
//			data += "\"isListen\":" + userCursor.getInt(4) + "," ;
//			data += "\"methodid\":" + userCursor.getInt(5);
//			data +="}";
			HistoryModel historyModel = new HistoryModel(userCursor.getString(3), userCursor.getInt(4), userCursor.getInt(5));
			contentValues.put("id", userCursor.getString(0));
			contentValues.put("trackid", userCursor.getString(1));
//			contentValues.put("data", data);
			contentValues.put("lastListen",userCursor.getString(3));
			contentValues.put("isListen",userCursor.getInt(4));
			contentValues.put("methodid",userCursor.getInt(5));
			userCursor.close();
//			db.close();
			return contentValues;
		}else {
			return null;
		}
	}

	public ContentValues[] GetHistoryRec(int count){
		db = historyDB.getReadableDatabase();
		ContentValues[] contentValues = new ContentValues[count];
		Cursor userCursor = db.rawQuery("SELECT * FROM " + HistoryTableName + " LIMIT " + count, null);
		try {
			int rows = userCursor.getCount();
			if (userCursor.moveToFirst()) {
				for(int i = 0; i<rows; i++) {
					contentValues[i]= new ContentValues();

//			String data;
//			data = "{";
//			data += "\"lastListen\":\"" + userCursor.getString(3) + "\",";
//			data += "\"isListen\":" + userCursor.getInt(4) + "," ;
//			data += "\"methodid\":" + userCursor.getInt(5);
//			data +="}";
					HistoryModel historyModel = new HistoryModel(userCursor.getString(3), userCursor.getInt(4), userCursor.getInt(5));
					contentValues[i].put("id", userCursor.getString(0));
					contentValues[i].put("trackid", userCursor.getString(1));
//			contentValues.put("data", data);
					contentValues[i].put("lastListen", userCursor.getString(3));
					contentValues[i].put("isListen", userCursor.getInt(4));
					contentValues[i].put("methodid", userCursor.getInt(5));
					userCursor.moveToNext();
				}

				userCursor.close();

//			db.close();
				return contentValues;
			} else {
				return null;
			}
		}catch (Exception ex) {
			return null;
		}
	}

	public void DeleteHistoryRec(String id){
		db = historyDB.getWritableDatabase();
		db.delete(HistoryTableName, "id = ?", new String[]{id});
//		db.close();
	}

	public void CleanHistoryTable(){
		db = historyDB.getWritableDatabase();
		db.execSQL("DELETE FROM " + HistoryTableName);
//		db.close();
	}
}
