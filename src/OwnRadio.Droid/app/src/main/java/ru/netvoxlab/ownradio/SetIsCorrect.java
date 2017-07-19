package ru.netvoxlab.ownradio;

import android.content.Context;
import android.os.AsyncTask;

import java.net.HttpURLConnection;

import retrofit2.Response;
import ru.netvoxlab.ownradio.models.TrackModel;


/**
 * Created by a.polunina on 13.07.2017.
 */

public class SetIsCorrect extends AsyncTask <String, Void, Boolean>  {
	private Boolean res;
	Context mContext;
	
	public SetIsCorrect(Context context){
		this.mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(String... data) {
		try {
			TrackModel trackModel = new TrackModel();
			trackModel.setIscorrect(0);
			Response<Void> response = ServiceGenerator.createService(APIService.class).setIsCorrect(data[1], data[0], trackModel).execute();
			if(response.isSuccessful()){
				if (response.code() == HttpURLConnection.HTTP_CREATED)
					new Utilites().SendInformationTxt(mContext, "Трек " + data[1] + "был помечен некорректным");
				else
					new Utilites().SendInformationTxt(mContext, "Ошибка в функции setIsCorrect. Сервер вернул код: " + response.code());
			}else {
				new Utilites().SendInformationTxt(mContext, "Ошибка в функции setIsCorrect. Сервер вернул код: " + response.code());
			}
		}catch (Exception ex){
			new Utilites().SendInformationTxt(mContext, "Ошибка в функции setIsCorrect");
		}
		return true;
	}
	
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		res = result;
	}
}
