package ru.netvoxlab.ownradio;

import android.app.IntentService;
import android.content.Intent;

import static ru.netvoxlab.ownradio.Constants.ACTION_FILLCACHE;
import static ru.netvoxlab.ownradio.Constants.ACTION_GETNEXTTRACK;
import static ru.netvoxlab.ownradio.Constants.EXTRA_COUNT;
import static ru.netvoxlab.ownradio.Constants.EXTRA_DEVICEID;

/**
 * Created by a.polunina on 20.07.2017.
 */

public class LongRequestAPIService extends IntentService {
	public LongRequestAPIService() {
		super("LongRequestAPIService");
	}
	
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			
			if(!new CheckConnection().CheckInetConnection(getApplicationContext()))
				return;
			
			final String action = intent.getAction();
			if (ACTION_GETNEXTTRACK.equals(action)) {
				//Получение информации о следующем треке и его загрузка
				final String deviceId = intent.getStringExtra(EXTRA_DEVICEID);
				final Integer countTracks = intent.getIntExtra(EXTRA_COUNT, 3);
				new TrackToCache(getApplicationContext()).SaveTrackToCache(deviceId, countTracks);
			} else if(ACTION_FILLCACHE.equals(action)) {
				new TrackToCache(getApplicationContext()).FillCache();
			}
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
