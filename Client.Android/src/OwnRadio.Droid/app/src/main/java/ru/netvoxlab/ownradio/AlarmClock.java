package ru.netvoxlab.ownradio;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;

import static ru.netvoxlab.ownradio.Constants.ALARM_TIME;
import static ru.netvoxlab.ownradio.Constants.FRIDAY_DAY;
import static ru.netvoxlab.ownradio.Constants.IS_ALARM_WORK;
import static ru.netvoxlab.ownradio.Constants.MONDAY_DAY;
import static ru.netvoxlab.ownradio.Constants.SATURDAY_DAY;
import static ru.netvoxlab.ownradio.Constants.SUNDAY_DAY;
import static ru.netvoxlab.ownradio.Constants.THURSDAY_DAY;
import static ru.netvoxlab.ownradio.Constants.TUESDAY_DAY;
import static ru.netvoxlab.ownradio.Constants.WEDNESDAY_DAY;

public class AlarmClock extends AppCompatActivity {
	
	private final String[] DAYS_NAMES = {"Воскресенье", "Понедельник", "Вторник", "Среду", "Четверг", "Пятницу", "Субботу"};
	
	private final String[] HOURS = {"час", "часа", "часов"};
	private final String[] MINUTES = {"минуту", "минуты", "минут"};
	private final String[] DAYS = {"день", "дня", "дней"};
	
	private ImageView imageView;
	
	private TextView txtProgress;
	/*Days of week*/
	private TextView txtMonday;
	private TextView txtTuesday;
	private TextView txtWednesday;
	private TextView txtThursday;
	private TextView txtFriday;
	private TextView txtSaturday;
	private TextView txtSunday;
	
	private SharedPreferences prefs;
	
	private CountDownTimer timer;
	
	private NumberPicker numberHours;
	private NumberPicker numberMinutes;
	
	private Toolbar toolbar;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Меняем тему, используемую при запуске приложения, на основную
		setTheme(R.style.AppTheme);
		
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_alarm_clock);
		
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		imageView = findViewById(R.id.imageView);
		
		txtProgress = findViewById(R.id.txtProgress);
		
		txtMonday = findViewById(R.id.txtMonday);
		txtTuesday = findViewById(R.id.txtTuesday);
		txtWednesday = findViewById(R.id.txtWednesday);
		txtThursday = findViewById(R.id.txtThursday);
		txtFriday = findViewById(R.id.txtFriday);
		txtSaturday = findViewById(R.id.txtSaturday);
		txtSunday = findViewById(R.id.txtSunday);
		
		numberHours = findViewById(R.id.numberHours);
		numberMinutes = findViewById(R.id.numberMinutes);
		
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		initialNumbers();
		
		initialComponents();
		
		
		//	numberHours.setOnValueChangedListener();
		
		numberHours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker numberPicker, int i, int i1) {
				String time = "";
				
				String addMinutes = numberMinutes.getValue() < 10 ? "0" + String.valueOf(numberMinutes.getValue()) : String.valueOf(numberMinutes.getValue());
				String addHours = numberHours.getValue() < 10 ? "0" + String.valueOf(numberHours.getValue()) : String.valueOf(numberHours.getValue());
				time = addHours + ":" + addMinutes;
				
				
				if (prefs.getBoolean(IS_ALARM_WORK, false)) {
					
					if (prefs.getString(ALARM_TIME, "") != time) // время отличается, необходимо все таймеры перезапустить
					{
						restartAlarm(time); // перезапускаем таймеры
						SharedPreferences.Editor prefEditor = prefs.edit();
						prefEditor.putString(ALARM_TIME, time); // сохраняем новое время
						prefEditor.apply();
						Toast.makeText(AlarmClock.this, "Будильник успешно перезапущен", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		numberMinutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker numberPicker, int i, int i1) {
				String time = "";
				
				String addMinutes = numberMinutes.getValue() < 10 ? "0" + String.valueOf(numberMinutes.getValue()) : String.valueOf(numberMinutes.getValue());
				String addHours = numberHours.getValue() < 10 ? "0" + String.valueOf(numberHours.getValue()) : String.valueOf(numberHours.getValue());
				time = addHours + ":" + addMinutes;
				
				if (prefs.getBoolean(IS_ALARM_WORK, false)) {
					
					if (prefs.getString(ALARM_TIME, "") != time) // время отличается, необходимо все таймеры перезапустить
					{
						restartAlarm(time); // перезапускаем таймеры
						SharedPreferences.Editor prefEditor = prefs.edit();
						prefEditor.putString(ALARM_TIME, time); // сохраняем новое время
						prefEditor.apply();
						Toast.makeText(AlarmClock.this, "Будильник успешно перезапущен", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}
	
	
	public void OnSelected(View view) {
		TextView textView = (TextView) view;
		boolean isSelected = false;
		
		
		String time = numberHours.getValue() + ":" + numberMinutes.getValue();
		
		if (textView.getBackground() == null) {
			textView.setBackgroundResource(R.drawable.bottom_selected);
			isSelected = true;
		} else {
			textView.setBackgroundResource(0);
		}
		
		int dayWeek = Integer.valueOf(textView.getTag().toString());
		
		// сохраняем дни в настройках дни выбранные
		setPreference(dayWeek, isSelected);
		
		
		if (!prefs.getBoolean(IS_ALARM_WORK, false))
			return;
		
		if (textView.getBackground() != null) {
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			AlarmReceiver.setTime(getApplicationContext(), time, dayWeek);
			Toast.makeText(this, "Будильник успешно добавлен на " + DAYS_NAMES[dayWeek - 1], Toast.LENGTH_SHORT).show();
		} else {
			AlarmReceiver.stopAlarm(this, dayWeek);
			
			if (isNotSelected()) {
				
				stopAlarm();
				if (timer != null)
					timer.cancel();
				
				txtProgress.setText("Будильник остановлен");
				
				SharedPreferences.Editor prefEditor = prefs.edit();
				
				prefEditor.putBoolean(IS_ALARM_WORK, false);
				prefEditor.apply();
			}
			
		}
		//пн=2, вт=3, ср=4, чт=5, пт=6, сб=7, вс=1
	}
	
	private void initialComponents() {
		String time = prefs.getString(ALARM_TIME, "");
		
		numberHours.setValue(TimePreference.getHour(time));
		numberMinutes.setValue(TimePreference.getMinute(time));
		
		if (prefs.getBoolean(MONDAY_DAY, false)) {
			txtMonday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtMonday.setBackgroundResource(0);
		}
		
		
		if (prefs.getBoolean(TUESDAY_DAY, false)) {
			txtTuesday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtTuesday.setBackgroundResource(0);
		}
		
		if (prefs.getBoolean(WEDNESDAY_DAY, false)) {
			txtWednesday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtWednesday.setBackgroundResource(0);
		}
		
		if (prefs.getBoolean(THURSDAY_DAY, false)) {
			txtThursday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtThursday.setBackgroundResource(0);
		}
		
		if (prefs.getBoolean(FRIDAY_DAY, false)) {
			txtFriday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtFriday.setBackgroundResource(0);
		}
		
		if (prefs.getBoolean(SATURDAY_DAY, false)) {
			txtSaturday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtSaturday.setBackgroundResource(0);
		}
		
		if (prefs.getBoolean(SUNDAY_DAY, false)) {
			txtSunday.setBackgroundResource(R.drawable.bottom_selected);
		} else {
			txtSunday.setBackgroundResource(0);
		}
		
		if (prefs.getBoolean(IS_ALARM_WORK, false)) {
			setTime();
			startTimer(prefs.edit());
			imageView.setImageResource(R.drawable.ic_blu_bud);
		} else {
			imageView.setImageResource(R.drawable.ic_grey_bud);
		}
	}
	
	
	private void initialNumbers() {
		String[] hourValues = new String[25];
		
		for (int i = 0; i < 25; i++) {
			String zero = "";
			if (i < 10)
				zero = "0";
			hourValues[i] = zero + String.valueOf(i);
		}
		
		numberHours.setMinValue(0);
		numberHours.setMaxValue(23);
		numberHours.setDisplayedValues(hourValues);
		
		
		String[] minValues = new String[61];
		
		for (int i = 0; i < 61; i++) {
			String zero = "";
			if (i < 10)
				zero = "0";
			minValues[i] = zero + String.valueOf(i);
		}
		
		numberMinutes.setMinValue(0);
		numberMinutes.setMaxValue(60);
		numberMinutes.setDisplayedValues(minValues);
	}
	
	private void setAlarmDay(String timesCurrent, String time) {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		int hours = TimePreference.getHour(time);
		int mins = TimePreference.getMinute(time);
		
		
		String[] curTimes = timesCurrent.split(":");
		
		int day = Integer.valueOf(curTimes[0]); // день недели
		if (day == 7) {
			day = 1;
		} else {
			day += 1;
		}
		
		int currentHours = Integer.valueOf(curTimes[1]);
		int currentMins = Integer.valueOf(curTimes[2]);
		
		int dayWeek = day;
		
		if (currentHours > hours) {
			if (dayWeek == 7) {
				dayWeek = 1;
			} else {
				dayWeek++;
			}
		} else if (hours == currentHours) {
			if (currentMins > mins) {
				if (dayWeek == 7) {
					dayWeek = 1;
				} else {
					dayWeek++;
				}
			}
		}
		
		setPreference(dayWeek, true);
		
		
		switch (dayWeek) {
			case 1:
				txtSunday.setBackgroundResource(R.drawable.bottom_selected);
				break;
			case 2:
				txtMonday.setBackgroundResource(R.drawable.bottom_selected);
				break;
			case 3:
				txtTuesday.setBackgroundResource(R.drawable.bottom_selected);
				break;
			case 4:
				txtWednesday.setBackgroundResource(R.drawable.bottom_selected);
				
				break;
			case 5:
				txtThursday.setBackgroundResource(R.drawable.bottom_selected);
				break;
			case 6:
				txtFriday.setBackgroundResource(R.drawable.bottom_selected);
				break;
			case 7:
				txtSaturday.setBackgroundResource(R.drawable.bottom_selected);
				break;
		}
		
		AlarmReceiver.setTime(getApplicationContext(), time, dayWeek);
		
	}
	
	private void startTimer(final SharedPreferences.Editor prefEditor) {
		String time = numberHours.getValue() + ":" + numberMinutes.getValue();
		boolean isRestart = restartAlarm(time);
		
		String timesCurrent = new SimpleDateFormat("u:HH:mm").format(Calendar.getInstance().getTime());
		
		String[] curTimes = timesCurrent.split(":");
		
		int day = Integer.valueOf(curTimes[0]); // день недели
		if (day == 7) {
			day = 1;
		} else {
			day += 1;
		}
		
		int currentHours = Integer.valueOf(curTimes[1]);
		int currentMins = Integer.valueOf(curTimes[2]);
		
		// если ни стоит ни 1 день
		if (!isRestart) {
			setAlarmDay(timesCurrent, time);
			//Toast.makeText(this, "Установите дни для будильника", Toast.LENGTH_SHORT).show();
			//return;
		}
		
		if (!prefs.getBoolean(IS_ALARM_WORK, false)) {
			Toast.makeText(this, "Будильник успешно запущен", Toast.LENGTH_SHORT).show();
		}
		
		imageView.setImageResource(R.drawable.ic_blu_bud);
		prefEditor.putBoolean(IS_ALARM_WORK, true);
		
		
		int hours = TimePreference.getHour(time);
		int mins = TimePreference.getMinute(time);
		
		
		boolean isNewDay = false;
		
		if (currentHours > hours)
			isNewDay = true;
		else if (hours == currentHours) {
			if (currentMins > mins) {
				isNewDay = true;
			}
		}
		
		hours += getHoursToDay(day, isNewDay);
		
		mins -= currentMins;
		
		if (mins < 0) {
			mins += 60;
			hours -= 1;
		}
		
		hours -= currentHours;
		
		if (hours < 0) {
			hours += 24 * 7;
		}
		
		long mlsec = (hours * 3600 + mins * 60) * 1000;
		
		timer = new CountDownTimer(mlsec, 1000) {
			@Override
			public void onTick(long l) {
				if (prefs.getBoolean(IS_ALARM_WORK, false)) {
					setTime();
				}
			}
			
			@Override
			public void onFinish() {
				stopAlarm();
			}
		};
		
		timer.start();
	}
	
	public void btnStartClock(View view) {
		
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		if (prefs.getBoolean(IS_ALARM_WORK, false)) {
			stopAlarm();
			imageView.setImageResource(R.drawable.ic_grey_bud);
			prefEditor.putBoolean(IS_ALARM_WORK, false);
			Toast.makeText(this, "Будильник успешно остановлен", Toast.LENGTH_SHORT).show();
			if (timer != null) {
				timer.cancel();
			}
			txtProgress.setText("Будильник остановлен");
		} else {
			startTimer(prefEditor);
		}
		
		prefEditor.apply();
	}
	
	
	boolean isNotSelected() {
		boolean isNotSelected = true;
		
		if (prefs.getBoolean(SUNDAY_DAY, false)) {
			isNotSelected = false;
		} else if (prefs.getBoolean(MONDAY_DAY, false)) {
			isNotSelected = false;
		} else if (prefs.getBoolean(TUESDAY_DAY, false)) {
			isNotSelected = false;
		} else if (prefs.getBoolean(WEDNESDAY_DAY, false)) {
			isNotSelected = false;
		} else if (prefs.getBoolean(THURSDAY_DAY, false)) {
			isNotSelected = false;
		} else if (prefs.getBoolean(FRIDAY_DAY, false)) {
			isNotSelected = false;
		} else if (prefs.getBoolean(SATURDAY_DAY, false)) {
			isNotSelected = false;
		}
		
		return isNotSelected;
	}
	
	private void stopAlarm() {
		imageView.setImageResource(R.drawable.ic_grey_bud);
		
		// если работает Alarm, то останавливаем
		if (prefs.getBoolean(IS_ALARM_WORK, false)) {
			for (int i = 1; i <= 7; i++) {
				AlarmReceiver.stopAlarm(getApplicationContext(), i);
			}
		}
		
	}
	
	private boolean restartAlarm(String time) {
		boolean isRestart = false;
		
		stopAlarm();
		
		if (prefs.getBoolean(SUNDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 1);
			isRestart = true;
		}
		
		if (prefs.getBoolean(MONDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 2);
			isRestart = true;
		}
		
		if (prefs.getBoolean(TUESDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 3);
			isRestart = true;
		}
		
		if (prefs.getBoolean(WEDNESDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 4);
			isRestart = true;
		}
		
		if (prefs.getBoolean(THURSDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 5);
			isRestart = true;
		}
		
		if (prefs.getBoolean(FRIDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 6);
			isRestart = true;
		}
		
		if (prefs.getBoolean(SATURDAY_DAY, false)) {
			AlarmReceiver.setTime(this, time, 7);
			isRestart = true;
		}
		
		if(isRestart)
			imageView.setImageResource(R.drawable.ic_blu_bud);
		
		return isRestart;
	}
	
	private void setPreference(int tag, boolean isChecked) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		switch (tag) {
			case 1:
				prefEditor.putBoolean(SUNDAY_DAY, isChecked);
				break;
			case 2:
				prefEditor.putBoolean(MONDAY_DAY, isChecked);
				break;
			case 3:
				prefEditor.putBoolean(TUESDAY_DAY, isChecked);
				break;
			case 4:
				prefEditor.putBoolean(WEDNESDAY_DAY, isChecked);
				break;
			case 5:
				prefEditor.putBoolean(THURSDAY_DAY, isChecked);
				break;
			case 6:
				prefEditor.putBoolean(FRIDAY_DAY, isChecked);
				break;
			case 7:
				prefEditor.putBoolean(SATURDAY_DAY, isChecked);
				break;
		}
		
		prefEditor.apply();
	}
	
	private void setTime() {
		
		String time = numberHours.getValue() + ":" + numberMinutes.getValue();
		
		int hours = TimePreference.getHour(time);
		int mins = TimePreference.getMinute(time);
		
		String timesCurrent = new SimpleDateFormat("u:HH:mm").format(Calendar.getInstance().getTime());
		
		String[] curTimes = timesCurrent.split(":");
		
		int day = Integer.valueOf(curTimes[0]); // день недели
		if (day == 7) {
			day = 1;
		} else {
			day += 1;
		}
		
		int currentHours = Integer.valueOf(curTimes[1]);
		int currentMins = Integer.valueOf(curTimes[2]);
		
		
		boolean isNewDay = false;
		
		if (currentHours > hours)
			isNewDay = true;
		else if (hours == currentHours) {
			if (mins < currentMins) {
				isNewDay = true;
			}
		}
		
		int hoursToDay = getHoursToDay(day, isNewDay);
		
		
		hours += hoursToDay;
		
		int resHours, resMins;
		
		resMins = mins - currentMins;
		
		if (resMins < 0) {
			resMins += 60;
			hours -= 1;
		}
		
		resHours = hours - currentHours;
		
		if (resHours < 0) {
			
			
			resHours += 24 * 7;
		}
		
		String resTime = resHours + ":" + resMins;
		
		String strTime = "Будильник зазвонит через";
		
		if (resHours > 0) {
			
			if (resHours > 24) {
				int resDays = resHours / 24;
				resHours -= resDays * 24;
				
				strTime += " " + resDays + " " + getCase(resDays, DAYS);
			}
			
			strTime += " " + resHours + " " + getCase(resHours, HOURS);
		}
		
		strTime += " " + resMins + " " + getCase(resMins, MINUTES);
		
		txtProgress.setText(strTime);
	}
	
	private int getHoursToDay(int dayWeek, boolean isNewDay) {
		
		Dictionary<Integer, String> dist = new Hashtable<Integer, String>();
		
		dist.put(1, SUNDAY_DAY);
		dist.put(2, MONDAY_DAY);
		dist.put(3, TUESDAY_DAY);
		dist.put(4, WEDNESDAY_DAY);
		dist.put(5, THURSDAY_DAY);
		dist.put(6, FRIDAY_DAY);
		dist.put(7, SATURDAY_DAY);
		
		
		int count = 0;
		
		int curDay = dayWeek;
		
		while (count != 7) {
			
			if (prefs.getBoolean(dist.get(curDay), false)) {
				
				if (curDay == dayWeek && isNewDay) {
					curDay += 1;
					continue;
				}
				
				if (curDay == dayWeek && !isNewDay)
					return 0;
				
				int add = 0;
				if (dayWeek > curDay) {
					add = 7;
				}
				
				return (curDay + add - dayWeek) * 24;
			}
			
			curDay++;
			
			if (curDay > 7) {
				curDay = 1;
			}
			
			count++;
		}
		
		return 0;
	}
	
	private String getCase(Integer value, String[] options) {
		
		value = Math.abs(value) % 100;
		
		if (value > 10 && value < 15)
			return options[2];
		
		value %= 10;
		if (value == 1) return options[0];
		if (value > 1 && value < 5) return options[1];
		return options[2];
	}
}
