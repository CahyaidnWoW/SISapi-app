using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class CageService
    {
        private readonly ApiClient _apiClient;

        public CageService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<List<Cage>> GetAllAsync()
        {
            return await _apiClient.GetAsync<List<Cage>>("cages");
        }

        public async Task<Cage> CreateAsync(string name, int capacity, string location)
        {
            var request = new CreateCageRequest
            {
                Name = name,
                Capacity = capacity,
                Location = location
            };

            return await _apiClient.PostAsync<Cage>("cages", request);
        }
    }
}