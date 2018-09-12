package ru.netvoxlab.ownradio;


import android.app.Application;
import android.os.Environment;

import org.solovyev.android.checkout.Billing;

import java.io.File;

/**
 * Created by a.polunina on 26.04.2017.
 */

public class App extends Application {
	/**
	 * Called when the application is starting, before any activity, service, or receiver objects (excluding content providers) have been created.
	 */
	private File appDirectory;
	private File logDirectory;
	private File logFile;
	private File musicDirectory;
	private Process process;
	private Integer countDownloadTrying;
	private Boolean autoPlay;
	private Boolean isFillingCacheActive;
	
	private static App instance;
	
	private final Billing mBilling = new Billing(this,  new Billing.DefaultConfiguration(){
		@Override
		public String getPublicKey(){
			return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyvzn+k5/eXZdtnHoO/zly7/hHZXVwihn6NRUCT06w597WJpT5GhTTi3SE8Ve4+kcaIAR20Au9bAje+XYWAtGckxNGK7q0RG5cBw05UEn0C/ZvWGunIIJw4rnMlMMu4Q3kNyHyvQhso3tVJCWdqdj3nZkj29MZJ6fU5EaNlxGEX5G7y6pvbevj8qikJhzIsKIxhVNwPfRPu5dalK0Ftan/7fzzsgq9DC3mh5AF6000I4oeocgBhlZQB4znOoJ9omWmcQ6PdUNGul8ny+TNYbQpl/aF9/09DsXlkHZSZ83bJYPsYUogw3AR356siyEOks3Dim5or8pC298NLH+XiNu6QIDAQAB";
		}
	});
	
	public App(){
		instance = this;
	}
	
	public static App get(){
		return instance;
	}
	
	public Billing getBilling() {
		return mBilling;
	}
	
	
	public void onCreate() {
		super.onCreate();
		countDownloadTrying = 0;
		//			if ( isExternalStorageWritable() ) {
		appDirectory = this.getFilesDir();// new File (Environment.getExternalStorageDirectory().getAbsolutePath());//this.getFilesDir();
		musicDirectory = new File(appDirectory + File.separator + "music");
		if(!musicDirectory.exists())
			musicDirectory.mkdirs();
		
			logDirectory = new File( appDirectory + File.separator + "log" );
			logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

			// create app folder
			if ( !appDirectory.exists() ) {
				appDirectory.mkdir();
			}

			// create log folder
			if ( !logDirectory.exists() ) {
				logDirectory.mkdir();
			}

			// clear the previous logcat and then write the new one to the file
//			try {
//				if (process != null) {
//					process.destroy();
//				}
//				process = Runtime.getRuntime().exec("logcat -c");
//				process = Runtime.getRuntime().exec("logcat -d time -f " + logFile);
//			} catch ( IOException e ) {
//				e.printStackTrace();
//			}

//		} else if ( isExternalStorageReadable() ) {
//			// only readable
//		} else {
//			// not accessible
//		}
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
			return true;
		}
		return false;
	}
	
	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
			return true;
		}
		return false;
	}
	

	
	public File getAppDirectory(){
		return appDirectory;
	}
	
	public File getLogDirectory(){
		return logDirectory;
	}
	
	public File getLogFile(){
		return logFile;
	}
	
	public File getMusicDirectory() { return musicDirectory; }
	
	public Integer getCountDownloadTrying() {
		return countDownloadTrying;
	}
	
	public void setCountDownloadTrying(Integer countDownloadTrying) {
		this.countDownloadTrying = countDownloadTrying;
	}
	
	public Boolean getAutoPlay() {
		if(autoPlay == null)
			autoPlay = false;
		return autoPlay;
	}
	
	public void setAutoPlay(Boolean autoPlay){
		this.autoPlay = autoPlay;
	}
	
	public Boolean getFillingCacheActive() {
		return isFillingCacheActive;
	}
	
	public void setFillingCacheActive(Boolean fillingCacheActive) {
		isFillingCacheActive = fillingCacheActive;
	}
}
