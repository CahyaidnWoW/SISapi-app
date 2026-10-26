using Newtonsoft.Json;
using System.Collections.Generic;

namespace SISapi_Desktop.Models
{
    public class FcrReport
    {
        [JsonProperty("cage_id")]
        public int CageId { get; set; }

        [JsonProperty("cage_name")]
        public string CageName { get; set; }

        [JsonProperty("total_livestocks")]
        public int TotalLivestocks { get; set; }

        [JsonProperty("total_feed_consumed_kg")]
        public decimal TotalFeedConsumed { get; set; }

        [JsonProperty("total_weight_gained_kg")]
        public decimal TotalWeightGain { get; set; }

        [JsonProperty("fcr")]
        public decimal Fcr { get; set; }
    }

    public class FcrListWrapper
    {
        [JsonProperty("status")]
        public string Status { get; set; }

        [JsonProperty("data")]
        public List<FcrReport> Data { get; set; }
    }
}