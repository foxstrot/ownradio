package ru.netvoxlab.ownradio;


import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

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
	
	public void onCreate() {
		super.onCreate();
		
		if ( isExternalStorageWritable() ) {
			appDirectory = this.getFilesDir();// new File (Environment.getExternalStorageDirectory().getAbsolutePath());//this.getFilesDir();
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
			try {
				Process process = Runtime.getRuntime().exec("logcat -c");
				process = Runtime.getRuntime().exec("logcat -v time -f " + logFile);
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			
//		} else if ( isExternalStorageReadable() ) {
//			// only readable
//		} else {
//			// not accessible
		}
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
}
