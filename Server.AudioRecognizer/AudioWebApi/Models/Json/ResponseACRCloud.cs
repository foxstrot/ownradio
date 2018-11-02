using Newtonsoft.Json;

namespace AudioWebApi
{
    public class ResponseACRCloud
    {
        [JsonProperty("status")]
        public Status Status { get; set; }

        [JsonProperty("metadata")]
        public Metadata Metadata { get; set; }


        [JsonProperty("cost_time")]
        public double CostTime { get; set; }
        [JsonProperty("result_type")]
        public int ResultType { get; set; }
    }
}