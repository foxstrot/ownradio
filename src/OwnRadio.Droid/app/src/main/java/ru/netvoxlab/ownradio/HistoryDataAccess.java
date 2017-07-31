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

	private final String HistoryTableName = "history";

	public HistoryDataAccess(Context context) {
		mContext = context;
		historyDB = TrackDB.getInstance(mContext);
//		historyDB = new TrackDB(mContext, DB_VER);
	}

	public void SaveHistoryRec(ContentValues historyInstance){
		historyDB.openDatabase();
		db = historyDB.database();
		historyInstance.put("id", UUID.randomUUID().toString());
		db.insert(HistoryTableName, null, historyInstance);
		historyDB.close();
//db.close();
	}

	public ContentValues[] GetHistoryRec(int count){
		historyDB.openDatabase();
		db = historyDB.database();
		ContentValues[] contentValues = new ContentValues[count];
		Cursor userCursor = db.rawQuery("SELECT * FROM " + HistoryTableName + " LIMIT " + count, null);
		try {
			int rows = userCursor.getCount();
			if (userCursor.moveToFirst()) {
				for(int i = 0; i<rows; i++) {
					contentValues[i]= new ContentValues();

//					HistoryModel historyModel = new HistoryModel(userCursor.getString(3), userCursor.getInt(4));
					contentValues[i].put("id", userCursor.getString(0));
					contentValues[i].put("trackid", userCursor.getString(1));
					contentValues[i].put("lastListen", userCursor.getString(3));
					contentValues[i].put("isListen", userCursor.getInt(4));
					userCursor.moveToNext();
				}
				userCursor.close();
				//db.close();
				historyDB.close();
				return contentValues;
			} else {
				historyDB.close();
				return null;
			}
		}catch (Exception ex) {
			historyDB.close();
			return null;
		}
	}
	
	
	public ContentValues GetHistoryRec(){
		historyDB.openDatabase();
		db = historyDB.database();
		ContentValues contentValues = new ContentValues();
		Cursor userCursor = db.rawQuery("SELECT * FROM " + HistoryTableName + " LIMIT 1", null);
		try {
			if (userCursor.moveToFirst()) {
				contentValues = new ContentValues();
//					HistoryModel historyModel = new HistoryModel(userCursor.getString(3), userCursor.getInt(4));
				contentValues.put("id", userCursor.getString(0));
				contentValues.put("trackid", userCursor.getString(1));
				contentValues.put("lastListen", userCursor.getString(3));
				contentValues.put("isListen", userCursor.getInt(4));
				userCursor.moveToNext();

				userCursor.close();
				//db.close();
				historyDB.close();
				return contentValues;
			} else {
				historyDB.close();
				return null;
			}
		}catch (Exception ex) {
			historyDB.close();
			return null;
		}
	}
	
	public void DeleteHistoryRec(String id){
		historyDB.openDatabase();
		db = historyDB.database();
		db.delete(HistoryTableName, "id = ?", new String[]{id});
		historyDB.close();
//db.close();
	}

	public void CleanHistoryTable(){
		historyDB.openDatabase();
		db = historyDB.database();
		db.execSQL("DELETE FROM " + HistoryTableName);
		historyDB.close();
//		db.close();
	}
}
