package ru.netvoxlab.ownradio;

import android.app.IntentService;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class HistoryService extends IntentService {


	public HistoryService() {
		super("HistoryService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			//Отправка на сервер накопленной истории прослушивания треков
			for (int i = 0; i < 3; i++) {
				new APICalls(getApplicationContext()).SendHistory(intent.getStringExtra("DeviceID"));
			}
		}
	}
}
