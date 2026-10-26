using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace SISapi_Desktop.Models
{
    public class DashboardSummaryResponse
    {
        [JsonProperty("status")]
        public string Status { get; set; }

        [JsonProperty("data")]
        public DashboardSummaryDto Data { get; set; }
    }

    public class GrowthTrendResponse
    {
        [JsonProperty("status")]
        public string Status { get; set; }

        [JsonProperty("data")]
        public List<GrowthTrendDto> Data { get; set; } = new List<GrowthTrendDto>();
    }

    public class DashboardSummaryDto
    {
        [JsonProperty("total_cattle")]
        public int TotalCattle { get; set; }

        [JsonProperty("ready_to_sell")]
        public int ReadyToSell { get; set; }

        [JsonProperty("sick_cattle")]
        public int SickCattle { get; set; }

        [JsonProperty("critical_feed_count")]
        public int CriticalFeedCount { get; set; }

        [JsonProperty("avg_adg_30d")]
        public double AvgAdg30d { get; set; }

        [JsonProperty("upcoming_medicals")]
        public List<UpcomingMedicalDto> UpcomingMedicals { get; set; } = new List<UpcomingMedicalDto>();
    }

    public class UpcomingMedicalDto
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("cattle_tag")]
        public string CattleTag { get; set; }

        [JsonProperty("treatment_type")]
        public string TreatmentType { get; set; }

        [JsonProperty("scheduled_date")]
        public DateTime ScheduledDate { get; set; }

        [JsonProperty("status")]
        public string Status { get; set; }
    }

    public class GrowthTrendDto
    {
        [JsonProperty("period")]
        public string Period { get; set; }

        [JsonProperty("avg_weight")]
        public double AvgWeight { get; set; }

        [JsonProperty("avg_adg")]
        public double AvgAdg { get; set; }
    }
}
