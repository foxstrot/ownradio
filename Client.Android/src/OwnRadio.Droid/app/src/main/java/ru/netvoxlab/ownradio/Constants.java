package ru.netvoxlab.ownradio;

/**
 * Created by a.polunina on 20.06.2017.
 */

public class Constants {
	private Constants() {
		// restrict instantiation
	}
	public final static String TAG = "ownRadio";
	public static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
	public static final String DEVICE_ID = "DeviceID";
	
	public static final String ONLY_WIFI = "0";
	public static final String ALL_CONNECTION_TYPES = "1";

	public static final String INTERNET_CONNECTION_TYPE = "internet_connections_list";
	
	public static final String ACTION_UPDATE_TRYING_COUNT = "ru.netvoxlab.ownradio.action.ACTION_UPDATE_TRYING_COUNT";
	public static final String ACTION_START_PLAY = "ru.netvoxlab.ownradio.action.ACTION_START_PLAY";
	
	public static final String ACTION_GETNEXTTRACK = "ru.netvoxlab.ownradio.action.GETNEXTTRACK";
	public static final String ACTION_FILLCACHE = "ru.netvoxlab.ownradio.action.FILLCACHE";
	public static final String EXTRA_DEVICEID = "ru.netvoxlab.ownradio.extra.EXTRA_DEVICEID";
	public static final String EXTRA_COUNT = "ru.netvoxlab.ownradio.extra.COUNT";
	
	public static final String ACTION_UPDATE_FILLCACHE_PROGRESS = "ru.netvoxlab.ownradio.action.ACTION_UPDATE_FILLCACHE_PROGRESS";
	public static final String EXTRA_FILLCACHE_PROGRESS = "ru.netvoxlab.ownradio.extra.EXTRA_FILLCACHE_PROGRESS";
	
}
