using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace Downloader
{
    internal class ZaycevService : ITrackService
    {
        private readonly HttpClient _client = new HttpClient();
        private readonly string _group;

        public ZaycevService(string group)
        {
            _group = group;
        }

        public async Task<List<Track>> GetTracks(int amount)
        {
            var count = amount / 50;
            if (amount % 50 != 0) count++;

            var tracks = new List<Track>();

            for (var page = 1; page <= count; page++)
            {
                var url = $"http://zaycev.net/{_group}/more.html?page={page}";

                int index = 0;

                //var url = $"http://zaycev.net/new/more.html?page={page}";

                var response = await _client.GetAsync(url).ConfigureAwait(false);

                var html = await response.Content.ReadAsStringAsync().ConfigureAwait(false);

                const string pattern = @"(?<link>musicset/play/(?<guid>[\w]{32}?)/[\d]*.json?)";
                var regex = new Regex(pattern);
                var matches = regex.Matches(html);

                var artists = Regex.Matches(html, "\\/artist\\/\\d+\">(.+?)<\\/a>").Cast<Match>().Select(x => x.Groups[1].Value).ToList();
                var titles = Regex.Matches(html, "musicset-track__track-name\"><a.+?>(.+?)<\\/a>").Cast<Match>().Select(x => x.Groups[1].Value).ToList();
                var durations = Regex.Matches(html, "data-duration=\"(\\d+)").Cast<Match>().Select(x => x.Groups[1].Value).ToList();


                foreach (Match match in matches)
                {
                    var link = match.Groups["link"].Value;
                    var guid = Guid.Parse(match.Groups["guid"].Value);

                    tracks.Add(new Track
                    {
                        Guid = guid.ToString(),
                        Url = $"http://zaycev.net/{link}",
                        Artist = artists[index],
                        Title = titles[index],
                        Duration = durations[index]
                    });

                    index++;
                }
            }

            return tracks;
        }

        public async Task<byte[]> GetTrack(string url)
        {
            using (var r = await _client.GetAsync(url).ConfigureAwait(false))
            {
                var responseString = await r.Content.ReadAsStringAsync().ConfigureAwait(false);
                var json = JsonConvert.DeserializeObject<dynamic>(responseString);
                string s = json.url;

                using (var r2 = await _client.GetAsync(s).ConfigureAwait(false))
                {
                    return await r2.Content.ReadAsByteArrayAsync().ConfigureAwait(false);
                }
            }
        }

    }
}
