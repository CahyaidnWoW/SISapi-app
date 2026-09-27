using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class BreedService
    {
        private readonly ApiClient _apiClient;

        public BreedService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<List<LivestockBreed>> GetAllAsync()
        {
            return await _apiClient.GetAsync<List<LivestockBreed>>("breeds");
        }
    }
}