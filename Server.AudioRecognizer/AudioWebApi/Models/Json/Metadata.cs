using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace AudioWebApi
{
    public class Metadata
    {
        [JsonProperty("music")]
        public List<Music> Musics { get; set; }

        [JsonProperty("timestamp_utc")]
        public DateTime? TimestampUtc { get; set; }
    }
}