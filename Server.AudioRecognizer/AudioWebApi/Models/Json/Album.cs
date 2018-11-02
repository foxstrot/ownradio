using Newtonsoft.Json;

namespace AudioWebApi
{
    public class Album
    {
        [JsonProperty("name")]
        public string Name { get; set; }
    }
}