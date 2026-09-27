using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class FeedService
    {
        private readonly ApiClient _apiClient;

        public FeedService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<List<FeedStock>> GetAllAsync()
        {
            return await _apiClient.GetAsync<List<FeedStock>>("feeds");
        }

        public async Task<FeedStock> CreateAsync(string feedName, string unit)
        {
            var request = new CreateFeedStockRequest { FeedName = feedName, Unit = unit };
            return await _apiClient.PostAsync<FeedStock>("feeds", request);
        }

        public async Task AddBatchAsync(int feedId, decimal quantity, string entryDate)
        {
            var request = new AddFeedBatchRequest
            {
                FeedId = feedId,
                QuantityIn = quantity,
                EntryDate = entryDate
            };
            await _apiClient.PostAsync<object>("feeds/batches", request);
        }
    }
}