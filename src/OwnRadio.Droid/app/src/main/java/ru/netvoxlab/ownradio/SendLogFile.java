package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by a.polunina on 16.05.2017.
 */

public class SendLogFile extends AsyncTask<String, Void, Boolean> {
	private Boolean res;
	Context mContext;
	
	public SendLogFile(Context context){
		this.mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(String... data) {
		try {
			File file = FileUtils.getFile(data[1]);
			// create RequestBody instance from file
			RequestBody requestFile =
					RequestBody.create(
							MediaType.parse("text/*"),
							file.getAbsolutePath()
					);
			
			// MultipartBody.Part is used to send also the actual file name
			MultipartBody.Part body =
					MultipartBody.Part.createFormData("logFile", file.getName(), requestFile);
			
			// add another part within the multipart request
//		String descriptionString = "hello, this is description speaking";
//		RequestBody description =
//				RequestBody.create(
//						okhttp3.MultipartBody.FORM, descriptionString);
			
			// finally, execute the request
			Response<ResponseBody> response = ServiceGenerator.createService(APIService.class).sendLogFile(data[0], body).execute();
			if (response.isSuccessful()) {
				if (response.code() == HttpURLConnection.HTTP_CREATED || response.code() == 208) {
					if (file.exists())
						file.delete();
					new Utilites().SendInformationTxt(mContext, "LogFile " + file.getName() + " is sending and deleted");
					
				} else {
					new Utilites().SendInformationTxt(mContext, "LogFile " + file.getName() + " is not send: Server response: " + response.code());
				}
			}else
				new Utilites().SendInformationTxt(mContext, "LogFile " + file.getName() + " is not send: Server response: " + response.code());
		}catch (Exception ex){
 			ex.printStackTrace();
			new Utilites().SendInformationTxt(mContext, "Error in sendLogFile(): " + ex.getLocalizedMessage());
			return false;
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
		res = aBoolean;
	}
}
