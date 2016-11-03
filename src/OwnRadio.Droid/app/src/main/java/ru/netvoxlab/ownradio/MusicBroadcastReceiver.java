package ru.netvoxlab.ownradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MusicBroadcastReceiver extends BroadcastReceiver {
	public MusicBroadcastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction() != AudioManager.ACTION_AUDIO_BECOMING_NOISY && intent.getAction() != Intent.ACTION_HEADSET_PLUG)
			return;

		String action = MediaPlayerService.ActionStop;

		if (intent.getAction() == Intent.ACTION_HEADSET_PLUG) {
			boolean isHSConnected = false;
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
					case 0:
						//отключение наушников
						if (isHSConnected) {
							isHSConnected = false;
							action = MediaPlayerService.ActionPause;
						}
						break;
					case 1:
						//подключение наушников
						isHSConnected = true;
						action = MediaPlayerService.ActionPlay;
						break;
				}
			}
		}

		if (intent.getAction() == AudioManager.ACTION_AUDIO_BECOMING_NOISY)
			//сигнал остановки сервиса
			action = MediaPlayerService.ActionStop;

		Intent intent1 = new Intent(context, MediaPlayerService.class);
		intent1.setAction(action);
		Intent remoteIntent = new Intent(action);
		context.startService(intent1);
	}
}