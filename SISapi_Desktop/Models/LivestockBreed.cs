using Newtonsoft.Json;

namespace SISapi_Desktop.Models
{
    public class LivestockBreed
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("breed_name")]
        public string BreedName { get; set; }

        [JsonProperty("ideal_fcr_range")]
        public string IdealFcrRange { get; set; }
    }
}