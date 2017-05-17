package ru.netvoxlab.ownradio;

import android.app.IntentService;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p> Сервис, последовательно обрабатывающий поступающие к нему запросы в отдельном потоке.
 * helper methods.
 */
public class RequestAPIService extends IntentService {
	public static final String ACTION_SENDHISTORY = "ru.netvoxlab.ownradio.action.SENDHISTORY";
	public static final String ACTION_GETNEXTTRACK = "ru.netvoxlab.ownradio.action.GETNEXTTRACK";
	
	public static final String EXTRA_DEVICEID = "ru.netvoxlab.ownradio.extra.EXTRA_DEVICEID";
	public static final String EXTRA_COUNT = "ru.netvoxlab.ownradio.extra.COUNT";
	
	public RequestAPIService() {
		super("RequestAPIService");
	}
	
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_GETNEXTTRACK.equals(action)) {
				//Получение информации о следующем треке
				final String deviceId = intent.getStringExtra(EXTRA_DEVICEID);
				final Integer countTracks = intent.getIntExtra(EXTRA_COUNT, 3);
				new TrackToCache(getApplicationContext()).SaveTrackToCache(deviceId, countTracks);
			} else if (ACTION_SENDHISTORY.equals(action)) {
				//Отправка на сервер накопленной истории прослушивания треков
				final String deviceId = intent.getStringExtra(EXTRA_DEVICEID);
				for (int i = 0; i < 3; i++) {
					new APICalls(getApplicationContext()).SendHistory(deviceId);
				}
			}
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
