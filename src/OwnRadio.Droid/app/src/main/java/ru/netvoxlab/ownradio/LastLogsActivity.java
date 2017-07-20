package ru.netvoxlab.ownradio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static ru.netvoxlab.ownradio.Constants.TAG;

public class LastLogsActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	TextView textLastLogs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_last_logs);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		textLastLogs = (TextView) findViewById(R.id.textLastLogs);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		try {
			Process process = Runtime.getRuntime().exec("logcat -t 200");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(TAG))
					log.append(line + "\n");
			}
			textLastLogs.setText(log);
		}catch (Exception ex){
			
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
