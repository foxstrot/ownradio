package ru.netvoxlab.ownradio;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ru.netvoxlab.ownradio.Constants.ACTION_UPDATE_FILLCACHE_PROGRESS;
import static ru.netvoxlab.ownradio.Constants.EXTRA_COUNT;
import static ru.netvoxlab.ownradio.Constants.EXTRA_DEVICEID;
import static ru.netvoxlab.ownradio.Constants.EXTRA_FILLCACHE_PROGRESS;
import static ru.netvoxlab.ownradio.Constants.TAG;
import static ru.netvoxlab.ownradio.MainActivity.ActionStopPlayback;
import static ru.netvoxlab.ownradio.MainActivity.version;
import static ru.netvoxlab.ownradio.RequestAPIService.ACTION_GETNEXTTRACK;

/**
 * A {@link AppCompatPreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	final static double bytesInGB = 1073741824.0d;
	final static double bytesInMB = 1048576.0d;
	static boolean isCachingStarted = false;
	static IInAppBillingService mBillingService;
	static boolean subscribeStatus = false;

	ServiceConnection mBillingServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBillingService = IInAppBillingService.Stub.asInterface(service);
			subscribeStatus = SubscribeManager.CheckSubscribeStatus(getApplicationContext(), mBillingService, true);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBillingService = null;
		}
	};

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			
			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				
				// Set the summary to reflect the new value.
				preference.setSummary(
						index >= 0
								? listPreference.getEntries()[index]
								: null);
				
			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);
					
				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));
					
					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};
	
	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
	
	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
				PreferenceManager
						.getDefaultSharedPreferences(preference.getContext())
						.getString(preference.getKey(), ""));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {



		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		bindService(serviceIntent, mBillingServiceConn, Context.BIND_AUTO_CREATE);


		//Меняем тему, используемую при запуске приложения, на основную
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		//подключаем тулбар
		getLayoutInflater().inflate(R.layout.app_bar, (ViewGroup) findViewById(android.R.id.content));
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//добавляем стрелку "назад" в тулбар
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
//		setupActionBar();
		int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 30, getResources().getDisplayMetrics());
		getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mBillingService != null) {
			unbindService(mBillingServiceConn	);
		}
	}

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
			//добавляем оступы чтобы настройки не скрывались под тулбаром
			int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
			int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
			int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 30, getResources().getDisplayMetrics());
			getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
		}
		//подключаем тулбар
//		getLayoutInflater().inflate(R.layout.app_bar, (ViewGroup) findViewById(android.R.id.content));
//		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//		setSupportActionBar(toolbar);
//		//добавляем стрелку "назад" в тулбар
////		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////		getSupportActionBar().setDisplayShowHomeEnabled(true);
//				//добавляем оступы чтобы настройки не скрывались под тулбаром
//		int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
//		int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
//		int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 30, getResources().getDisplayMetrics());
//		getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
//		//добавляем обработчик нажатий на унопку навигации тулбара (стрелку "назад")
//		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onBackPressed();
//			}
//		});
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}
	
	/**
	 * This method stops fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	protected boolean isValidFragment(String fragmentName) {
		return GeneralPreferenceFragment.class.getName().equals(fragmentName)
//				|| NotificationPreferenceFragment.class.getName().equals(fragmentName)
				;
	}


	
	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {

		private File pathToCache;

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			final Context context = getActivity().getApplicationContext();
			pathToCache = ((App)context.getApplicationContext()).getMusicDirectory();

			final PrefManager prefManager = new PrefManager(context);
			addPreferencesFromResource(R.xml.pref_general);
			setHasOptionsMenu(true);
			final TrackToCache memoryUtil = new TrackToCache(getActivity().getApplicationContext());
			final TrackDataAccess trackInfo = new TrackDataAccess(getActivity().getApplicationContext());

			final double freeSpace = memoryUtil.FreeSpace();
			final double tracksSpace = memoryUtil.FolderSize(((App) getActivity().getApplicationContext()).getMusicDirectory());

			double listeningTracksSpace = memoryUtil.ListeningTracksSize();
			//TODO удаляем раздел настроек "Слушать только свои треки". Вернуть, когда будет готова эта фича и ее описание: preferenceScreen.addPreference(somePreference);
			Preference listenOwnTracks = findPreference("pref_key_listen_own_tracks");
			PreferenceScreen preferenceScreen = getPreferenceScreen();
			preferenceScreen.removePreference(listenOwnTracks);
			//делаем недоступным для пользователей без подписки
			Preference ownTracksSwitch = findPreference("own_tracks_switch");
			if(ownTracksSwitch!=null)
				ownTracksSwitch.setEnabled(false);
			
			Preference freeMemorySize = findPreference("free_memory_size");
			if (freeSpace / bytesInGB > 0.1d)
				freeMemorySize.setTitle(getResources().getString(R.string.pref_free_memory_size) + " " + BigDecimal.valueOf(freeSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb");
			else
				freeMemorySize.setTitle(getResources().getString(R.string.pref_free_memory_size) + " " + BigDecimal.valueOf(freeSpace / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb");
			

			Preference listeningTracksMemorySize = findPreference("listening_tracks_size");
			if (listeningTracksSpace / bytesInGB > 0.1d)
				listeningTracksMemorySize.setTitle(getResources().getString(R.string.pref_listening_tracks_size_size) + " " + BigDecimal.valueOf(listeningTracksSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb (" + trackInfo.GetCountPlayTracks() + " " +  getResources().getString(R.string.tracks) + ")");
			else
				listeningTracksMemorySize.setTitle(getResources().getString(R.string.pref_listening_tracks_size_size) + " " + BigDecimal.valueOf(listeningTracksSpace / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb (" + trackInfo.GetCountPlayTracks() + " " +  getResources().getString(R.string.tracks) + ")");
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
//			bindPreferenceSummaryToValue(findPreference("example_text"));
			bindPreferenceSummaryToValue(findPreference("internet_connections_list"));

			Preference sendLogs = findPreference("pref_send_logs");
			sendLogs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new Utilites().SendLogs(context, prefManager.getDeviceId());
					return true;
				}
			});

			final NumberPickerPreference maxMemorySize = (NumberPickerPreference) findPreference("key_number");
			maxMemorySize.setTitle(getResources().getString(R.string.pref_max_memory_size) + " " + maxMemorySize.getValue() * 10 + "%");
			if(freeSpace / bytesInGB > 0.1d){
				maxMemorySize.setSummary(getResources().getString(R.string.from_free_memory) + " " + BigDecimal.valueOf((freeSpace + listeningTracksSpace) / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb");
			}
			else{
				maxMemorySize.setSummary(getResources().getString(R.string.from_free_memory) + " " + BigDecimal.valueOf((freeSpace + listeningTracksSpace) / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb");
			}
			maxMemorySize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object value) {
					maxMemorySize.setValue(Integer.valueOf((Integer) value));
					maxMemorySize.setTitle(getResources().getString(R.string.pref_max_memory_size) + " " + maxMemorySize.getValue() * 10 + "%");
//					int index = maxMemorySize.findIndexOfValue(stringValue);
//
//					// Set the summary to reflect the new value.
//					preference.setTitle(
//							index >= 0
//									? getResources().getString(R.string.pref_max_memory_size) + " " + maxMemorySize.getEntries()[index]
//									: getResources().getString(R.string.pref_max_memory_size));
					Preference allTracksMemorySize = findPreference("all_tracks_size");
					//TODO менять окончания в зависимости от числа. Выводить в Мб/Гб если число небольшое
					if (tracksSpace / bytesInGB > 0.1d) {
						if ((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInGB > 0.1d) {
//					allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb (" + trackInfo.GetExistTracksCount() + " " + getResources().getString(R.string.tracks) + ")");
							allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb (из " + BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb)");
						} else {
							allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb (из " +  BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb)");
						}
					}
					else{
						if ((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInMB > 0.1d) {
							allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb (из " + BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb)");
						} else {
							allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb (из " +  BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb)");
						}
					}
					allTracksMemorySize.setSummary(trackInfo.GetExistTracksCount() + " треков");
					return true;
				}
			});

			Preference allTracksMemorySize = findPreference("all_tracks_size");
			//TODO менять окончания в зависимости от числа. Выводить в Мб/Гб если число небольшое
			if (tracksSpace / bytesInGB > 0.1d) {
				if ((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInGB > 0.1d) {
					allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb (из " + BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb)");
				} else {
					allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb (из " +  BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb)");
				}
			}
			else{
				if ((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInMB > 0.1d) {
					allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb (из " + BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInGB).setScale(2, BigDecimal.ROUND_DOWN) + "Gb)");
				} else {
					allTracksMemorySize.setTitle(getResources().getString(R.string.pref_all_tracks_size) + " " + BigDecimal.valueOf(tracksSpace / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb (из " +  BigDecimal.valueOf((freeSpace * (((float) maxMemorySize.getValue()) / 10)) / bytesInMB).setScale(2, BigDecimal.ROUND_DOWN) + "Mb)");
				}
			}
			allTracksMemorySize.setSummary(trackInfo.GetExistTracksCount() + " треков");

			Preference storageSettings = findPreference("key_number");
			if(prefManager.getPrefItemBool("is_subscribed", false) || subscribeStatus  || (android.os.Build.VERSION.SDK_INT < 24 && android.os.Build.VERSION.SDK_INT >= 19)){
				storageSettings.setEnabled(true);
			}
			else {
				storageSettings.setEnabled(false);
			}

			Preference sysInfo = findPreference("sys_info");
			sysInfo.setTitle("Version: " + prefManager.getPrefItem(version) + "\nDeviceID: " + prefManager.getDeviceId());// + "\nTrackID:" + MediaPlayerService.player.get);
			
			Preference countTracksTable = findPreference("pref_count_tracks_table");
			countTracksTable.setTitle(trackInfo.GetCountPlayTracksTable());
			
			Preference aboutApp = findPreference("about_app");
			aboutApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent appInfoActivity = new Intent(context, AppInfoActivity.class);
					startActivity(appInfoActivity);
					return true;
				}
			});
			
			Preference lastLogs = findPreference("pref_last_log_recs");
			lastLogs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent lastLogsActivity = new Intent(context, LastLogsActivity.class);
					startActivity(lastLogsActivity);
					return true;
				}
			});
			
			//Пункт меню "свободная память" открывает системную информацию
			Preference freeMemory = findPreference("free_memory_size");
			freeMemory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivityForResult(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS), 0);
					return true;
				}
			});
			
			//Пункт меню "удалить прослушанные треки"
			Preference deleteListenedTracks = findPreference("delete_listening_tracks");
			deleteListenedTracks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					try {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(R.string.title_dialog_clear_listening_cache)
								.setMessage(R.string.dialog_clear_listening_cache)
								.setCancelable(true)
								.setPositiveButton(R.string.button_ok,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											//Если нажата ОК - запускаем удаление треков, пересоздаем активность
											memoryUtil.DeleteListenedTracksFromCache();
											getActivity().recreate();
											dialogInterface.cancel();
										}
									})
								.setNegativeButton(R.string.button_cancel,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											dialogInterface.cancel();
										}
									});
						AlertDialog alert = builder.create();
						alert.show();
					}catch (Exception ex){
						
					}
					return true;
				}
			});
			
			//Пункт меню "удалить все треки"
			Preference deleteAllTracks = findPreference("delete_all_tracks");
			deleteAllTracks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.title_dialog_clear_all_cache)
							.setMessage(R.string.dialog_clear_all_cache)
							.setCancelable(true)
							.setPositiveButton(R.string.button_ok,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											Intent intent = new Intent(ActionStopPlayback);
											context.sendBroadcast(intent);
											memoryUtil.DeleteAllTracksFromCache();
											getActivity().recreate();
											dialogInterface.cancel();
										}
									})
							.setNegativeButton(R.string.button_cancel,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											dialogInterface.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
					return true;
				}
			});

			//Пункт меню "Купить подписку"
			Preference buySubscription = findPreference("buy_subscription");
			if(android.os.Build.VERSION.SDK_INT >= 24) {
				if (!prefManager.getPrefItemBool("is_subscribed", false) || !subscribeStatus) {
					buySubscription.setEnabled(true);
					buySubscription.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							try {

								byte[] packageNameBytes = context.getPackageName().getBytes();
								String packageNameBase64 = Base64.encodeToString(packageNameBytes, Base64.DEFAULT);


								Bundle buyIntentBundle = mBillingService.getBuyIntent(3, context.getPackageName(),
										"test_subscribe_1", "subs", packageNameBase64);
								PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
								try {
									startIntentSenderForResult(pendingIntent.getIntentSender(),
											1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
											Integer.valueOf(0), buyIntentBundle);


								} catch (IntentSender.SendIntentException ex) {
									Log.d(TAG, " " + ex.getLocalizedMessage());
								}
							} catch (RemoteException e) {
								Log.d(TAG, " " + e.getLocalizedMessage());
							}
							return true;
						}
					});
				} else {
					buySubscription.setEnabled(false);
				}
			}
			else{
				buySubscription.setEnabled(false);
			}

			//Пункт меню "Заполнить кэш" - забивает доступный для приложения объем памяти треками (ограничения задаются настройками)
			final Preference fillCache = findPreference("fill_cache");
			if(prefManager.getPrefItemBool("is_subscribed", false) || subscribeStatus || (android.os.Build.VERSION.SDK_INT < 24 && android.os.Build.VERSION.SDK_INT >= 19)){
				fillCache.setEnabled(true);
				fillCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						final CheckConnection checkConnection = new CheckConnection();
						if (!checkConnection.CheckInetConnection(context)) {
							Toast.makeText(context.getApplicationContext(), "Подключение к интернету отсутствует", Toast.LENGTH_LONG).show();
							return true;
						}
						if(isCachingStarted) {
							Toast.makeText(context, "Заполнение кэша уже началось", Toast.LENGTH_LONG).show();
							return true;
						}
						if(memoryUtil.CheckCacheDoing() == 0 && trackInfo.GetCountPlayTracks() <= 0)
							return true;
						((App)context.getApplicationContext()).setCountDownloadTrying(0);
						final LongRequestAPIService longRequestAPIService = new LongRequestAPIService();
						isCachingStarted = true;
						((App)context.getApplicationContext()).setFillingCacheActive(true);
						Toast.makeText(context, "Заполнение кэша началось", Toast.LENGTH_LONG).show();

						try{
							new Thread(new Runnable() {
								@Override
								public void run() {
									while (memoryUtil.CheckCacheDoing() == 1 && checkConnection.CheckInetConnection(context)) {
										Log.d(TAG, "waitingIntentCount" + longRequestAPIService.getWaitingIntentCount());
										Intent downloaderIntent = new Intent(context.getApplicationContext(), LongRequestAPIService.class);
										downloaderIntent.setAction(ACTION_GETNEXTTRACK);
										downloaderIntent.putExtra(EXTRA_DEVICEID, prefManager.getDeviceId());
										downloaderIntent.putExtra(EXTRA_COUNT, 3);
										context.getApplicationContext().startService(downloaderIntent);
										try {
											Thread.sleep(30000);
										} catch (Exception ex) {

										}
									}
									Intent progressIntent = new Intent(ACTION_UPDATE_FILLCACHE_PROGRESS);
									progressIntent.putExtra(EXTRA_FILLCACHE_PROGRESS, 0);
									context.sendBroadcast(progressIntent);
									isCachingStarted = false;
									((App)context.getApplicationContext()).setFillingCacheActive(false);
								}
							}).start();
						}catch (Exception ex){

						}
						return true;
					}
				});
			}
			else {
				fillCache.setEnabled(false);
			}

		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}



	public static class MyPrefListener implements Preference.OnPreferenceClickListener {
		private Context mContext;
		
		public MyPrefListener(Context context) {
			this.mContext = context;
		}
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			return true;
		}
	}
}