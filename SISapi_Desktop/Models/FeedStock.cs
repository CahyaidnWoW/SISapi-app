using Newtonsoft.Json;

namespace SISapi_Desktop.Models
{
    public class FeedStock
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("feed_name")]
        public string FeedName { get; set; }

        [JsonProperty("unit")]
        public string Unit { get; set; }

        [JsonProperty("total_stock")]
        public decimal? TotalStock { get; set; }
    }

    public class CreateFeedStockRequest
    {
        [JsonProperty("feed_name")]
        public string FeedName { get; set; }

        [JsonProperty("unit")]
        public string Unit { get; set; }
    }

    public class UpdateFeedStockRequest
    {
        [JsonProperty("feed_name")]
        public string FeedName { get; set; }

        [JsonProperty("unit")]
        public string Unit { get; set; }
    }

    public class AddFeedBatchRequest
    {
        [JsonProperty("feed_id")]
        public int FeedId { get; set; }

        [JsonProperty("quantity_in")]
        public decimal QuantityIn { get; set; }

        [JsonProperty("entry_date")]
        public string EntryDate { get; set; }
    }
}