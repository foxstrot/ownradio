using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace AudioWebApi
{
    public class Music
    {
        [JsonProperty("external_ids")]
        public ExternalId ExternalIds { get; set; }

        [JsonProperty("play_offset_ms")]
        public int PlayOffsetMs { get; set; }

        [JsonProperty("release_date")]
        public DateTime ReleaseDate { get; set; }

        [JsonProperty("artists")]
        public List<Artist> Artists { get; set; }

        [JsonProperty("title")]
        public string Title { get; set; }
        [JsonProperty("duration_ms")]
        public int DurationMs { get; set; }

        [JsonProperty("album")]
        public Album Almub { get; set; }

        [JsonProperty("acrid")]
        public string Acrid { get; set; }

        [JsonProperty("result_from")]
        public int ResultFrom { get; set; }

        [JsonProperty("score")]
        public int Score { get; set; }
    }
}