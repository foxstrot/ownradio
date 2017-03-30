package ru.netvoxlab.ownradio;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Created by a.polunina on 15.11.2016.
 */

public interface APIService {

	@GET("v4/tracks/{deviceid}/next")
	@Headers("Content-Type: application/json")
	Call<Map<String, String>> getNextTrackID(@Path("deviceid") String deviceId);

	@Streaming
	@GET("v4/tracks/{trackid}")
	Call<ResponseBody> getTrackById(@Path("trackid") String trackId);

	@POST("v4/histories/{deviceid}/{trackid}")
	@Headers("Content-Type: application/json")
	Call<Void> sendHistory(@Path("deviceid") String deviceId, @Path("trackid") String trackId, @Body HistoryModel data);

}
