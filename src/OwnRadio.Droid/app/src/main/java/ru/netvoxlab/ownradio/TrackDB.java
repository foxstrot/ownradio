package ru.netvoxlab.ownradio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a.polunina on 25.10.2016.
 */

public class TrackDB extends SQLiteOpenHelper {
	private final static int DB_VER = 1;
	public static final String TABLE_NAME = "track";
	static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(id TEXT NOT NULL UNIQUE, trackurl TEXT NOT NULL," +
			" datetimelastlisten TEXT NOT NULL, islisten INTEGER NOT NULL DEFAULT 0, isexist INTEGER NOT NULL DEFAULT 0)";
	private static final String DB_NAME = "ownradio.db";
	Context mContext;

	public TrackDB(Context context, int dbVer) {
		super(context, DB_NAME, null, dbVer);
		mContext = context;
	}

	//    public  SQLiteC
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//проверяем какая версия сейчас и делаете апдейт
		db.execSQL("DROP TABLE IF EXISTS+" + TABLE_NAME);
		onCreate(db);
	}
}
