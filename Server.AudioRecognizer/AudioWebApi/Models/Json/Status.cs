using Newtonsoft.Json;

namespace AudioWebApi
{
    public class Status
    {
        [JsonProperty("msg")]
        public string Message { get; set; }
        [JsonProperty("code")]
        public int Code { get; set; }
        [JsonProperty("version")]
        public string Version { get; set; }
    }
}