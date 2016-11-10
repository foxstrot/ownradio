using System;
using System.Configuration;
using System.Globalization;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using OwnRadio.Client.Desktop.Model;
using System.Linq;

namespace OwnRadio.Client.Desktop
{
	public class OwnRadioRestClient
	{
		private readonly Uri _serviceUri = new Uri("http://java.ownradio.ru/");
		private readonly HttpClient _httpClient = new HttpClient();

		public async Task<Track> GetNextTrack(Guid deviceId)
		{
			var response = await _httpClient.GetAsync(new Uri(_serviceUri, $"api/v2/tracks/{deviceId}/next"))
						.ConfigureAwait(false);

			if (response.StatusCode != HttpStatusCode.OK)
				throw new HttpRequestException($"Failed to get next track ID [{response.StatusCode}]");

			var track = new Track();
			track.Id = await response.Content.ReadAsAsync<Guid>();
			track.Uri = new Uri(_serviceUri, "/api/v2/tracks/" + track.Id);

			return track;
		}

		public async void SendStatus(Guid deviceId, Track track)
		{
			var form = new MultipartFormDataContent
			{
				{new StringContent(track.ListenEnd.ToString("dd/MM/yyyy", CultureInfo.InvariantCulture)), "lastListen"},
				{new StringContent(track.Status.ToString("D")), "isListen"},
				{new StringContent("случайный"), "method"}
			};

			var response = await _httpClient.PostAsync($"{_serviceUri}api/v2/histories/{deviceId}/{track.Id}", form);

			if (response.StatusCode != HttpStatusCode.OK)
				throw new HttpRequestException($"Failed to get send track status [{response.StatusCode}]");
		}
	}
}
