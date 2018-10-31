using Newtonsoft.Json;

namespace AudioWebApi
{
    public class Artist
    {
        [JsonProperty("name")]
        public string Name { get; set; }
    }
}