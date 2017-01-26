package ru.netvoxlab.ownradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import static ru.netvoxlab.ownradio.MediaPlayerService.playbackWithHSisInterrupted;

public class RemoteControlReceiver extends BroadcastReceiver {
	public RemoteControlReceiver() {
	}

	public String ComponentName() {
		return getClass().getName();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != Intent.ACTION_MEDIA_BUTTON)
			return;

		// Событие будет срабатывать дважды, при нажатии (down) и отпускании (up).
		// Необходимо обработать только нажатие.
		KeyEvent key = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (key.getAction() != KeyEvent.ACTION_DOWN)
			return;

		String action = MediaPlayerService.ActionPlay;

		switch (key.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				action = MediaPlayerService.ActionTogglePlayback;
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				action = MediaPlayerService.ActionPlay;
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				action = MediaPlayerService.ActionPause;
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				action = MediaPlayerService.ActionStop;
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				action = MediaPlayerService.ActionNext;
				break;
//            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
//                action = MediaPlayerService.ActionPrevious;
//                break;
			default:
				return;
		}

		playbackWithHSisInterrupted = false;
		Intent remoteIntent = new Intent(action);
		context.startService(remoteIntent);
	}
}
