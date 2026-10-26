using Newtonsoft.Json;
using System;
using System.Collections.Generic;

namespace SISapi_Desktop.Models
{
    public class ActivityLogResponse
    {
        [JsonProperty("status")]
        public string Status { get; set; }

        [JsonProperty("data")]
        public List<ActivityLogDto> Data { get; set; } = new List<ActivityLogDto>();
    }

    public class ActivityLogDto
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("user_id")]
        public int? UserId { get; set; }

        [JsonProperty("user")]
        public UserLogDto User { get; set; }

        [JsonProperty("action")]
        public string Action { get; set; }

        [JsonProperty("module")]
        public string Module { get; set; }

        [JsonProperty("description")]
        public string Description { get; set; }

        [JsonProperty("platform")]
        public string Platform { get; set; }

        [JsonProperty("ip_address")]
        public string IpAddress { get; set; }

        [JsonProperty("created_at")]
        public DateTime CreatedAt { get; set; }
    }

    public class UserLogDto
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("role")]
        public string Role { get; set; }
    }
}
