package ru.netvoxlab.ownradio;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Created by a.polunina on 15.11.2016.
 */

public interface APIService {

	@GET("v2/tracks/11111111-1111-1111-1111-111111111111/next")
	Call<String> GetNextTrackID();

	@Streaming
	@GET("v2/tracks/{trackId}")
	Call<ResponseBody> DownloadTrackToCache(@Path("trackId") String trackId);

}
