package ru.netvoxlab.ownradio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

import static ru.netvoxlab.ownradio.MainActivity.filePath;

/**
 * Created by a.polunina on 25.10.2016.
 */

public class TrackDB extends SQLiteOpenHelper {
	public final static int DB_VER = 8;
	public static final String TABLE_NAME_TRACK = "track";
	static final String CREATE_TABLE_TRACK = "CREATE TABLE " + TABLE_NAME_TRACK + "(id TEXT NOT NULL UNIQUE, trackurl TEXT," +
			" title TEXT, artist TEXT, length INTEGER NOT NULL DEFAULT 1000, " +
			" datetimelastlisten TEXT NOT NULL, isexist INTEGER NOT NULL DEFAULT 0, countplay INTEGER NOT NULL DEFAULT 0)";

	public static final String TABLE_NAME_HISTORY = "history";
	static final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_NAME_HISTORY + "(id TEXT NOT NULL UNIQUE, trackid TEXT NOT NULL, userid TEXT NOT NULL," +
			"datetimelisten TEXT NOT NULL, islisten INTEGER NOT NULL DEFAULT 0)";

	public static final String DB_NAME = "ownradiodb34.db3";
	Context mContext;

	public TrackDB(Context context, int dbVer) {
		super(context, DB_NAME, null, dbVer);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_TRACK);
		db.execSQL(CREATE_TABLE_HISTORY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//проверяем какая версия сейчас и делаете апдейт
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TRACK);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_HISTORY);
		onCreate(db);


		//Чистим все треки
		try {
			File directory = filePath;//mContext.getFilesDir();
//			File directory = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
			if (directory.listFiles() != null) {
				for (File file : directory.listFiles()) {
					file.delete();
				}
			}

			File directory2 = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
			if (directory2.listFiles() != null) {
				for (File file : directory2.listFiles()) {
					file.delete();
				}
			}
		} catch (Exception ex) {
			ex.getLocalizedMessage();
		}
	}
}
