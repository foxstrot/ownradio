using Newtonsoft.Json;

namespace AudioWebApi
{
    public class ExternalId
    {
        [JsonProperty("isrc")]
        public string ISRC { get; set; }
        [JsonProperty("upc")]
        public string UPC { get; set; }
    }
}