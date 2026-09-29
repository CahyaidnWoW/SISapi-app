using Newtonsoft.Json;
using System;

namespace SISapi_Desktop.Models
{
    public class Livestock
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("tag_number")]
        public string TagNumber { get; set; }

        [JsonProperty("cage_id")]
        public int CageId { get; set; }

        [JsonProperty("breed_id")]
        public int BreedId { get; set; }

        [JsonProperty("gender")]
        public string Gender { get; set; }

        [JsonProperty("birth_date")]
        public DateTime BirthDate { get; set; }

        [JsonProperty("entry_date")]
        public DateTime EntryDate { get; set; }

        [JsonProperty("initial_weight")]
        public decimal InitialWeight { get; set; }

        [JsonProperty("status")]
        public string Status { get; set; }

        [JsonProperty("cage")]
        public Cage Cage { get; set; }

        [JsonProperty("breed")]
        public LivestockBreed Breed { get; set; }
    }

    public class CreateLivestockRequest
    {
        [JsonProperty("cage_id")]
        public int CageId { get; set; }

        [JsonProperty("breed_id")]
        public int BreedId { get; set; }

        [JsonProperty("gender")]
        public string Gender { get; set; }

        [JsonProperty("birth_date")]
        public string BirthDate { get; set; }

        [JsonProperty("entry_date")]
        public string EntryDate { get; set; }

        [JsonProperty("initial_weight")]
        public decimal InitialWeight { get; set; }
    }

    public class UpdateLivestockRequest
    {
        [JsonProperty("tag_number")]
        public string TagNumber { get; set; }

        [JsonProperty("cage_id")]
        public int CageId { get; set; }

        [JsonProperty("breed")]
        public string Breed { get; set; }

        [JsonProperty("gender")]
        public string Gender { get; set; }

        [JsonProperty("birth_date")]
        public string BirthDate { get; set; }

        [JsonProperty("status")]
        public string Status { get; set; }
    }
}