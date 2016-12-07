package ru.netvoxlab.ownradio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a.polunina on 07.11.2016.
 */

public class HistoryDB extends SQLiteOpenHelper {
	private final static int DB_VER = R.string.db_ver;
//	public static final String TABLE_NAME = "history";
//	static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(id TEXT NOT NULL UNIQUE, trackid TEXT NOT NULL, userid TEXT NOT NULL," +
//			"datetimelisten TEXT NOT NULL, islisten INTEGER NOT NULL DEFAULT 0, methodid INTEGER NOT NULL)";
	private static final String DB_NAME = "ownradio.db";
	Context mContext;

	public HistoryDB(Context context, int dbVer){
		super(context, DB_NAME, null, dbVer);
//		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//проверяем какая версия сейчас и делаете апдейт
//		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//		onCreate(db);
	}
}
