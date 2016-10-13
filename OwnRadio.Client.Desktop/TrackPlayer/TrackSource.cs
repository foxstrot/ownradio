using NLog;
using System.Net.Http;
using System.Threading.Tasks;

namespace OwnRadio.Client.Desktop
{    
	class TrackSource
	{
		private string userId;
		private string serverAddress;
		Logger log;

		public TrackSource(Settings settings, Logger logger)
		{
			serverAddress = settings.serverAddress;
			userId = settings.userId;
			log = logger;
		}
	   
		public TrackInfo GetFirstTrackInfo()
		{
			string url = getTrackInfoRequest("-1", "", false);
			return getTrackInfo(url);
		}

		public TrackInfo GetNextTrackInfo(string lastTrackId, string lastTrackMethod, bool listedTillTheEnd)
		{
			string url = getTrackInfoRequest(lastTrackId, lastTrackMethod, listedTillTheEnd);
			return getTrackInfo(url);
		}

		public string GetTrackPlayUrl(string trackId)
		{
			return string.Format("{0}/api/TrackSource/Play?trackId={1}", serverAddress, trackId);
		}

		private string getTrackInfoRequest(string lastTrackId, string lastTrackMethod, bool listedTillTheEnd)
		{
			return string.Format("{0}/api/TrackSource/NextTrack?userId={1}&lastTrackId={2}&lastTrackMethod={3}&listedTillTheEnd={4}"
				, serverAddress, userId, lastTrackId, lastTrackMethod, listedTillTheEnd.ToString().ToLower());
		}

		private TrackInfo getTrackInfo(string url)
		{
			TrackInfo responce = null;
			bool received = false;
			using (HttpClient httpClient = new HttpClient())
			{                
				Task getTask = httpClient.GetAsync(url).ContinueWith(task =>
				{
					if (task.Status == TaskStatus.RanToCompletion)
					{
						var response = task.Result;
						if (response.IsSuccessStatusCode)
						{
							responce = response.Content.ReadAsAsync<TrackInfo>().Result;
							received = responce != null;
						}
					}
				});
				getTask.Wait();
			}
			log.Debug(received ? "получен трек" : "не получен трек");
			return responce;
		}
	}
}
