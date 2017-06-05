package ru.netvoxlab.ownradio.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.netvoxlab.ownradio.R;

/**
 * Created by a.polunina on 11.05.2017.
 */

public class PlaybackControlsFragment extends Fragment {
	
	ImageButton btnPlayPause;
	ImageButton btnNext;
	TextView txtTrackTitle;
	TextView txtTrackArtist;
	ProgressBar progressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);
		
		btnPlayPause = (ImageButton) rootView.findViewById(R.id.btnPlayPause);
		btnPlayPause.setEnabled(true);
		btnPlayPause.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View view) {
//												if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() == PlaybackStateCompat.STATE_PLAYING) {
////					btnPlayPause.setImageResource(R.drawable.btn_play);
//													btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
//													binder.GetMediaPlayerService().Pause();
//												} else {
////					btnPlayPause.setImageResource(R.drawable.btn_pause);
//													btnPlayPause.setBackgroundResource(R.drawable.circular_button_selector);
//													binder.GetMediaPlayerService().Play();
//													playbackWithHSisInterrupted = false;
//												}
//												textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
											}
										});
				btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
		btnNext.setEnabled(true);
		btnNext.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
							//				if (binder.GetMediaPlayerService().player != null && binder.GetMediaPlayerService().GetMediaPlayerState() != PlaybackStateCompat.STATE_SKIPPING_TO_NEXT && binder.GetMediaPlayerService().GetMediaPlayerState() != PlaybackStateCompat.STATE_BUFFERING) {
							//					binder.GetMediaPlayerService().Next();
							//					textTrackID.setText("Track ID: " + binder.GetMediaPlayerService().TrackID);
							//					Log.e(TAG, "Next");
							//				}
										}
									});
		
		txtTrackTitle = (TextView) rootView.findViewById(R.id.trackTitle);
		txtTrackArtist = (TextView) rootView.findViewById(R.id.trackArtist);
//		rootView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(getActivity(), FullScreenPlayerActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				MediaControllerCompat controller = ((FragmentActivity) getActivity())
//						.getSupportMediaController();
//				MediaMetadataCompat metadata = controller.getMetadata();
//				if (metadata != null) {
//					intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION,
//							metadata.getDescription());
//				}
//				startActivity(intent);
//			}
//		});
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
//		LogHelper.d(TAG, "fragment.onStart");
//		MediaControllerCompat controller = ((FragmentActivity) getActivity())
//				.getSupportMediaController();
//		if (controller != null) {
//			onConnected();
//		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
//		log.d(TAG, "fragment.onStop");
//		MediaControllerCompat controller = ((FragmentActivity) getActivity())
//				.getSupportMediaController();
//		if (controller != null) {
//			controller.unregisterCallback(mCallback);
//		}
	}
	
}
