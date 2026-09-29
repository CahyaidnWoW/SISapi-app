using Newtonsoft.Json;

namespace SISapi_Desktop.Models
{
    public class Cage
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("capacity")]
        public int Capacity { get; set; }

        [JsonProperty("current_population")]
        public int CurrentPopulation { get; set; }

        [JsonProperty("location")]
        public string Location { get; set; }
    }

    public class CreateCageRequest
    {
        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("capacity")]
        public int Capacity { get; set; }

        [JsonProperty("location")]
        public string Location { get; set; }
    }

    public class UpdateCageRequest
    {
        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("capacity")]
        public int Capacity { get; set; }

        [JsonProperty("location")]
        public string Location { get; set; }
    }
}