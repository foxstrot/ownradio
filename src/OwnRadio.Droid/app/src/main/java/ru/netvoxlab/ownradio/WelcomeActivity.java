package ru.netvoxlab.ownradio;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static ru.netvoxlab.ownradio.RequestAPIService.ACTION_GETNEXTTRACK;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_COUNT;
import static ru.netvoxlab.ownradio.RequestAPIService.EXTRA_DEVICEID;

public class WelcomeActivity extends AppCompatActivity {
	
	private ViewPager viewPager;
	private MyViewPagerAdapter myViewPagerAdapter;
	private LinearLayout dotsLayout;
	private TextView[] dots;
	private int[] layouts;
	private Button btnSkip, btnNext;
	private PrefManager prefManager;
	private VideoView videoView;
	private Uri uriVideoView;
	private String deviceId;
	final Handler loadHandler = new Handler();;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Checking for first time launch - before calling setContentView()
		prefManager = new PrefManager(this);
		if (!prefManager.isFirstTimeLaunch()) {
			launchHomeScreen();
			finish();
		}
		
		//генерируем deviceId и регистрируем устройство
		try {
			deviceId = prefManager.getDeviceId();
			if (deviceId.isEmpty()) {
				deviceId = UUID.randomUUID().toString();
				prefManager.setDeviceId(deviceId);
				String UserName = "NewUser";
				String DeviceName = Build.BRAND + " " + Build.PRODUCT;
				new APICalls(getApplicationContext()).RegisterDevice(deviceId, DeviceName + " " + getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).versionName);
				String UserId = new APICalls(this).GetUserId(deviceId);
				prefManager.setPrefItem("UserID", UserId);
				prefManager.setPrefItem("UserName", UserName);
				prefManager.setPrefItem("DeviceName", DeviceName);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		//Запускаем загрузку трех треков
		Intent downloaderIntent = new Intent(this, RequestAPIService.class);
		downloaderIntent.setAction(ACTION_GETNEXTTRACK);
		downloaderIntent.putExtra(EXTRA_DEVICEID, deviceId);
		downloaderIntent.putExtra(EXTRA_COUNT, 3);
		startService(downloaderIntent);
		
		// Making notification bar transparent
		if (Build.VERSION.SDK_INT >= 21) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
		
		setContentView(R.layout.activity_welcome);
		
		viewPager = (ViewPager) findViewById(R.id.view_pager);
		dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
		btnSkip = (Button) findViewById(R.id.btn_skip);
		btnNext = (Button) findViewById(R.id.btn_next);
		
		videoView = (VideoView)findViewById(R.id.videoView);
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(true);
				videoView.requestFocus();
				videoView.start();
			}
		});
		try {
			uriVideoView = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_portrait);
			videoView.setVideoURI(uriVideoView);
			
			setDimension();
		}catch (Exception ex){
			new Utilites().SendInformationTxt(getApplicationContext(), "Ошибка при подключении фонового видео " + ex.getLocalizedMessage());
		}
		// layouts of all welcome sliders
		// add few more layouts if you want
		layouts = new int[]{
				R.layout.welcome_slide1,
				R.layout.welcome_slide2,
				R.layout.welcome_slide3,
				R.layout.welcome_slide4,
				R.layout.welcome_slide5};
		
		// adding bottom dots
		addBottomDots(0);
		
		// making notification bar transparent
		changeStatusBarColor();
		
		myViewPagerAdapter = new MyViewPagerAdapter();
		viewPager.setAdapter(myViewPagerAdapter);
		viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
		
		btnSkip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(new TrackDataAccess(getApplicationContext()).GetExistTracksCount() > 1)
					launchHomeScreen();
				else
					viewPager.setCurrentItem(layouts.length-1);
			}
		});
		
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// checking for last page
				// if last page home screen will be launched
				int current = getItem(+1);
				if (current < layouts.length) {
					//если хотя бы один трек успел загрузиться - закрываем слайдер и переходим к основному экрану
					if(current == layouts.length - 1 && new TrackDataAccess(getApplicationContext()).GetExistTracksCount() > 1){
						launchHomeScreen();
						return;
					}
					//иначе показываем последний слайд
						// move to next screen
						try {
							viewPager.setCurrentItem(current);
						} catch (Exception ex) {
							ex.getLocalizedMessage();
						}
				} else {
					launchHomeScreen();
				}
			}
		});
		final Timer timer = new Timer();
		final Handler handler = new Handler();
		final Runnable Update = new Runnable(){
			public void run(){
				int current = viewPager.getCurrentItem();
				if(current == layouts.length - 2 && new TrackDataAccess(getApplicationContext()).GetExistTracksCount() > 1){
					btnNext.setText(getString(R.string.start));
					btnSkip.setVisibility(View.GONE);
					timer.cancel();
					return;
				}
				if(current >= layouts.length-1){
					timer.cancel();
					return;
				}
				current++;
				viewPager.setCurrentItem(current);
			}
		};
		 // This will create a new Thread
		timer .schedule(new TimerTask() { // task to be scheduled
			@Override
			public void run() {
				handler.post(Update);
			}
		}, 5000, 5000);
		}
	
	private void addBottomDots(int currentPage) {
		dots = new TextView[layouts.length];
		
		int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
		int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);
		dotsLayout.removeAllViews();
		for (int i = 0; i < dots.length; i++) {
			dots[i] = new TextView(this);
			dots[i].setText(Html.fromHtml("&#8226;"));
			dots[i].setTextSize(35);
			dots[i].setTextColor(colorsInactive[currentPage]);
			dotsLayout.addView(dots[i]);
		}
		
		if (dots.length > 0)
			dots[currentPage].setTextColor(colorsActive[currentPage]);
	}
	
	private int getItem(int i) {
		return viewPager.getCurrentItem() + i;
	}
	
	private void launchHomeScreen() {
		prefManager.setFirstTimeLaunch(false);
		startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
		finish();
	}
	
	//  viewpager change listener
	ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int position) {
			addBottomDots(position);
			
			// changing the next button text 'NEXT' / 'GOT IT'
			if (position == layouts.length - 1) {
				// last page. make button text to GOT IT
//				btnNext.setText(getString(R.string.start));
				btnNext.setVisibility(View.GONE);
				btnSkip.setVisibility(View.GONE);
				final Timer loadTimer = new Timer();
				final Runnable LoadTrack = new Runnable(){
					public void run(){
						if(new TrackDataAccess(getApplicationContext()).GetExistTracksCount() > 1){
							launchHomeScreen();
							loadTimer.cancel();
							return;
						}
					}
				};
				loadTimer .schedule(new TimerTask() { // task to be scheduled
					@Override
					public void run() {
						loadHandler.post(LoadTrack);
					}
				}, 500, 3000);
			} else {
				// still pages are left
				btnNext.setText(getString(R.string.next));
				btnSkip.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}
		
		@Override
		public void onPageScrollStateChanged(int state) {
			
		}
	};
	
	/**
	 * Making notification bar transparent
	 */
	private void changeStatusBarColor() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
		}
	}
	
	/**
	 * View pager adapter
	 */
	public class MyViewPagerAdapter extends PagerAdapter {
		private LayoutInflater layoutInflater;
		
		public MyViewPagerAdapter() {
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View view = layoutInflater.inflate(layouts[position], container, false);
			container.addView(view);
			
			return view;
		}
		
		@Override
		public int getCount() {
			return layouts.length;
		}
		
		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}
		
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;
			container.removeView(view);
		}
	}
	
	
	// This method set dimension for video view
	private void setDimension() {
		// Adjust the size of the video
		// so it fits on the screen
		float videoProportion = getVideoProportion();
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int screenHeight = getResources().getDisplayMetrics().heightPixels;
		float screenProportion = (float) screenHeight / (float) screenWidth;
		android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();
		
		if (videoProportion < screenProportion) {
			lp.height= screenHeight;
			lp.width = (int) ((float) screenHeight / videoProportion);
		} else {
			lp.width = screenWidth;
			lp.height = (int) ((float) screenWidth * videoProportion);
		}
		videoView.setLayoutParams(lp);
	}
	
	// This method gets the proportion of the video that you want to display.
	private float getVideoProportion(){
			return 1.8f; //height/width video
		}
}
