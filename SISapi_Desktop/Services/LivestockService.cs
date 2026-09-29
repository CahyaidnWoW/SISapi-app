using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class LivestockService
    {
        private readonly ApiClient _apiClient;

        public LivestockService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<List<Livestock>> GetAllAsync()
        {
            return await _apiClient.GetAsync<List<Livestock>>("livestocks");
        }

        public async Task<Livestock> CreateAsync(CreateLivestockRequest request)
        {
            return await _apiClient.PostAsync<Livestock>("livestocks", request);
        }

        public async Task<Livestock> UpdateAsync(int id, UpdateLivestockRequest request)
        {
            return await _apiClient.PutAsync<Livestock>($"livestocks/{id}", request);
        }

        public async Task DeleteAsync(int id)
        {
            await _apiClient.DeleteAsync($"livestocks/{id}");
        }
    }
}