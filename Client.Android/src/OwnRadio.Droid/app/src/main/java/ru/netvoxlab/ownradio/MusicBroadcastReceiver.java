package ru.netvoxlab.ownradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import static ru.netvoxlab.ownradio.MediaPlayerService.isHSConnected;
import static ru.netvoxlab.ownradio.MediaPlayerService.playbackWithHSisInterrupted;
import static ru.netvoxlab.ownradio.MediaPlayerService.player;

public class MusicBroadcastReceiver extends BroadcastReceiver {
	public MusicBroadcastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction() != AudioManager.ACTION_AUDIO_BECOMING_NOISY && intent.getAction() != Intent.ACTION_HEADSET_PLUG)
			return;

		String action = MediaPlayerService.ActionPause;

		if (intent.getAction() == Intent.ACTION_HEADSET_PLUG) {

			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
					case 0:
						//отключение наушников
						if (isHSConnected) {
							isHSConnected = false;
							action = MediaPlayerService.ActionPause;
							if(player != null && player.isPlaying())
								playbackWithHSisInterrupted = true;
						}
						break;
					case 1:
						//подключение наушников
						isHSConnected = true;
						if(!playbackWithHSisInterrupted)
							return;
						action = MediaPlayerService.ActionPlay;
						playbackWithHSisInterrupted = false;
						break;
				}
			}
		}

		if (intent.getAction() == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
			//сигнал остановки сервиса
			action = MediaPlayerService.ActionPause;
			if(player.isPlaying())
				playbackWithHSisInterrupted = true;
		}

		Intent remoteIntent = new Intent(context, MediaPlayerService.class);
		remoteIntent.setAction(action);
		context.startService(remoteIntent);
	}
}