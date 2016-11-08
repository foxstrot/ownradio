package ru.netvoxlab.ownradio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a.polunina on 25.10.2016.
 */

public class TrackDB extends SQLiteOpenHelper {
	private final static int DB_VER = 2;
	public static final String TABLE_NAME_TRACK = "track";
	static final String CREATE_TABLE_TRACK = "CREATE TABLE " + TABLE_NAME_TRACK + "(id TEXT NOT NULL UNIQUE, trackurl TEXT NOT NULL," +
			" datetimelastlisten TEXT NOT NULL, islisten INTEGER NOT NULL DEFAULT 0, isexist INTEGER NOT NULL DEFAULT 0)";

	public static final String TABLE_NAME_HISTORY = "history";
	static final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_NAME_HISTORY + "(id TEXT NOT NULL UNIQUE, trackid TEXT NOT NULL, userid TEXT NOT NULL," +
			"datetimelisten TEXT NOT NULL, islisten INTEGER NOT NULL DEFAULT 0, method TEXT NOT NULL)";

	private static final String DB_NAME = "ownradio.db";
	Context mContext;

	public TrackDB(Context context, int dbVer) {
		super(context, DB_NAME, null, dbVer);
		mContext = context;
	}

	//    public  SQLiteC
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_TRACK);
		db.execSQL(CREATE_TABLE_HISTORY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//проверяем какая версия сейчас и делаете апдейт
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_TRACK);
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_TRACK);
		onCreate(db);
	}
}
