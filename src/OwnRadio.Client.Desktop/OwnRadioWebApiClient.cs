using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net.Http;
using OwnRadio.Client.Desktop.Model;

namespace OwnRadio.Client.Desktop
{
    /// <summary>
    /// Class for interaction with OwnRadio's WebAPI service
    /// Because I am using MediaElement which can get media
    /// stream by URI and I don't need implement GetTrackById
    /// until no need to save track content locally
    /// </summary>
    public class OwnRadioWebApiClient
    {
        // Base service URI
        private Uri serviceUri = new Uri(Properties.Settings.Default.ServiceUri);
        // WebAPI's HttpClient object
        private HttpClient httpClient = new HttpClient();

        /// <summary>
        /// Gets next track Id
        /// </summary>
        /// <param name="deviceId">DeviceId for which we requesting track</param>
        /// <returns>Track info</returns>
        public async Task<Track> GetNextTrack(Guid deviceId)
        {
            HttpResponseMessage response = await httpClient.GetAsync(new Uri(serviceUri, "track/GetNextTrackID/" + deviceId.ToString())).ConfigureAwait(false);

            Track track = new Track();

            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                track.Id = await response.Content.ReadAsAsync<Guid>();
                track.Uri = new Uri(serviceUri, "track/GetTrackByID/" + track.Id.ToString());
            }
            else
                throw new HttpRequestException("Failed to get next track ID [" + response.StatusCode.ToString() + "]");

            return track;
        }

        /// <summary>
        /// Send track's listen status
        /// </summary>
        /// <param name="deviceId">DeviceId from which track was listened</param>
        /// <param name="track">Track info</param>
        public async void SendStatus(Guid deviceId, Track track)
        {
            HttpResponseMessage response = await httpClient.GetAsync(
                new Uri(serviceUri, "track/SetStatusTrack/" + 
                deviceId.ToString() + "," +
                track.Id.ToString() + "," +
                track.Status.ToString("D") + "," +
                track.ListenEnd.ToString("dd.MM.yyyy H:mm")
                )).ConfigureAwait(false);

            if (response.StatusCode != System.Net.HttpStatusCode.OK)
                throw new HttpRequestException("Failed to get send track status [" + response.StatusCode.ToString() + "]");
        }
    }
}
