package ru.netvoxlab.ownradio;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar;

public class TimerSleep extends AppCompatActivity {

    private  int TimeSeconds;
    private int CurrentTime;
    private final String[] HOURS = { "час", "часа", "часов" };
    private final String[] MINUTES = { "минута", "минуты", "минут" };

    private boolean isEnableTimer = false;
    private CountDownTimer timer;


    private ImageView btnGo;
    private TextView timeDuration;
    private TextView txtProgress;
    private Toolbar toolbar;
    private HoloCircleSeekBar picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Меняем тему, используемую при запуске приложения, на основную
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_sleep);

        picker = (HoloCircleSeekBar ) findViewById(R.id.picker);
        picker.setMax(4 * 60);

        // init all comptonents
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnGo = (ImageView) findViewById(R.id.btnGo);
        timeDuration = (TextView) findViewById(R.id.timeDuration);
        txtProgress = (TextView) findViewById(R.id.txtProgress);


        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnableTimer) {
                    timeDuration.setText("Таймер выключен");
                    isEnableTimer = false;
                    timer.cancel();
                    Toast.makeText(getApplicationContext(),"Таймер успешно остановлен",Toast.LENGTH_SHORT).show();
                }
                else {
                    StartTimer();
                    Toast.makeText(getApplicationContext(),"Таймер успешно запущен",Toast.LENGTH_SHORT).show();
                }

            }
        });

        picker.setOnSeekBarChangeListener(new HoloCircleSeekBar.OnCircleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(HoloCircleSeekBar holoCircleSeekBar, int progress, boolean b) {
                SetTextTime(progress);
            }

            @Override
            public void onStartTrackingTouch(HoloCircleSeekBar holoCircleSeekBar) {
                int progress = holoCircleSeekBar.getValue();
                SetTextTime(progress);
            }

            @Override
            public void onStopTrackingTouch(HoloCircleSeekBar holoCircleSeekBar) {
                RestartTimer();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void RestartTimer()
    {
        if(CurrentTime != TimeSeconds && isEnableTimer) {
            timer.cancel();
            StartTimer();
            Toast.makeText(getApplicationContext(), "Таймер успешно перезапущен", Toast.LENGTH_SHORT).show();
        }
    }

    private void SetTextTime(int progress)
    {
        int _hours = progress / 60;
        int _min = progress - _hours * 60;

        String hours = _hours > 9 ? String.valueOf(_hours) : "0" + _hours;
        String min = _min > 9 ? String.valueOf(_min) : "0" + _min;
        String time = hours + ":" + min;

        txtProgress.setText(time);

        hours += " " + GetCase(_hours, HOURS);
        min += " " + GetCase(_min, MINUTES);

        time = hours + " " + min;

        int max = 4 * 60;
        int percent = progress * 100 / max;

        TimeSeconds = progress * 60; // min * 60

    }

    private void StartTimer()
    {
        CurrentTime = TimeSeconds;

        timer = new CountDownTimer(TimeSeconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                int _seconds =  (int) ( millisUntilFinished / 1000 );
                int _hours = _seconds / 3600;
                _seconds -= _hours * 3600;
                int _min = _seconds / 60 ;
                _seconds -= _min * 60;
                _min += _seconds > 0 ? 1 : 0;

                String hours = _hours > 9 ? String.valueOf(_hours) : "0" + _hours;
                hours += " " + GetCase(_hours, HOURS);
                String min = _min > 9 ? String.valueOf(_min) : "0" + _min;
                min += " " + GetCase(_min, MINUTES);
                String time = hours + " " + min;

                timeDuration.setText("Таймер выключится через " + time);

            }

            public void onFinish() {
                setResult(RESULT_OK, new Intent());
                finish();
            }
        };
        timer.start();
        isEnableTimer = true;
    }

    private String GetCase(Integer value, String [] options) {

        value = Math.abs(value) % 100;

        if (value > 10 && value < 15)
            return options[2];

        value %= 10;
        if (value == 1) return options[0];
        if (value > 1 && value < 5) return options[1];
        return options[2];
    }
}

