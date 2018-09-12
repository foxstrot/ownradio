package ru.netvoxlab.ownradio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import static ru.netvoxlab.ownradio.MainActivity.version;


public class AppInfoActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	TextView textVersion;
	
	TextView aboutOwnradio;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Меняем тему, используемую при запуске приложения, на основную
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_app_info);
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
		textVersion = (TextView) findViewById(R.id.versionName);
		textVersion.setText(new PrefManager(getApplicationContext()).getPrefItem(version));
		aboutOwnradio = (TextView) findViewById(R.id.about_ownradio);
		aboutOwnradio.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
