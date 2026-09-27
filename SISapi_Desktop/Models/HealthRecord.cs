using Newtonsoft.Json;
using System;

namespace SISapi_Desktop.Models
{
    public class HealthRecord
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("livestock_id")]
        public int LivestockId { get; set; }

        [JsonProperty("type")]
        public string Type { get; set; }

        [JsonProperty("diagnosis")]
        public string Diagnosis { get; set; }

        [JsonProperty("treatment")]
        public string Treatment { get; set; }

        [JsonProperty("photo_path")]
        public string PhotoPath { get; set; }

        [JsonProperty("next_due_date")]
        public DateTime? NextDueDate { get; set; }

        [JsonProperty("date")]
        public DateTime Date { get; set; }

        [JsonProperty("livestock")]
        public Livestock Livestock { get; set; }
    }

    public class CreateHealthRecordRequest
    {
        [JsonProperty("type")]
        public string Type { get; set; }

        [JsonProperty("diagnosis")]
        public string Diagnosis { get; set; }

        [JsonProperty("treatment")]
        public string Treatment { get; set; }

        [JsonProperty("next_due_date")]
        public string NextDueDate { get; set; }

        [JsonProperty("date")]
        public string Date { get; set; }
    }

    public class UpdateHealthRecordRequest
    {
        [JsonProperty("diagnosis")]
        public string Diagnosis { get; set; }

        [JsonProperty("treatment")]
        public string Treatment { get; set; }
    }
}