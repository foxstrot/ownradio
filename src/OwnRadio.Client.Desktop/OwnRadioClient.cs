using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Web.Script.Serialization;
using System.Windows;
using Newtonsoft.Json.Linq;
using OwnRadio.Client.Desktop.Model;
using OwnRadio.Client.Desktop.Properties;

namespace OwnRadio.Client.Desktop
{
	public class OwnRadioClient
	{
		private readonly Uri _serviceUri = new Uri(Settings.Default.ServiceUri);
		private readonly HttpClient _httpClient = new HttpClient();

		public async Task<TrackInfo> GetNextTrack(Guid deviceId)
		{
			var response = await _httpClient.GetAsync(new Uri(_serviceUri, $"v3/tracks/{deviceId}/next"))
						.ConfigureAwait(false);

			if (response.StatusCode != HttpStatusCode.OK)
				throw new HttpRequestException($"Failed to get next track ID [{response.StatusCode}]");

			var track = new TrackInfo();
			var json = await response.Content.ReadAsStringAsync();
			dynamic obj = JObject.Parse(json);
			
			track.Id = Guid.Parse((string)obj.id);
			track.Uri = new Uri(_serviceUri, $"v3/tracks/{track.Id}");
			track.Artist = (string)obj.artist;
			track.Length = (string)obj.length;
			track.Name = (string)obj.name;
			track.MethodId = (string)obj.methodid;

			return track;
		}

		public async Task SendStatus(Guid deviceId, TrackInfo track)
		{
			var serializer = new JavaScriptSerializer();

			var data = new TrackHistory
			{
				lastListen = track.ListenEnd.ToString("yyyy-MM-ddTH:m:s"),
				isListen = ((int)track.Status).ToString(),
				methodid = track.MethodId
			};

			var json = serializer.Serialize(data);
			var content = new StringContent(json, Encoding.UTF8, "application/json");

			var response = await _httpClient.PostAsync($"{_serviceUri}v3/histories/{deviceId}/{track.Id}", content);

			if (response.StatusCode != HttpStatusCode.OK)
				throw new HttpRequestException($"Failed to get send track status [{response.StatusCode}]");
		}
	}

	public class TrackHistory
	{
		public string lastListen { get; set; }
		public string isListen { get; set; }
		public string methodid { get; set; }
	}
}
