using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class HealthRecordService
    {
        private readonly ApiClient _apiClient;

        public HealthRecordService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<List<HealthRecord>> GetByTagAsync(string tagNumber)
        {
            return await _apiClient.GetAsync<List<HealthRecord>>($"livestocks/{tagNumber}/health-records");
        }

        public async Task<HealthRecord> CreateAsync(string tagNumber, CreateHealthRecordRequest request)
        {
            return await _apiClient.PostAsync<HealthRecord>($"livestocks/{tagNumber}/health-report", request);
        }

        public async Task<List<HealthRecord>> GetDueAsync()
        {   
            return await _apiClient.GetAsync<List<HealthRecord>>("health-records/due");
        }

        public async Task<HealthRecord> UpdateAsync(int id, UpdateHealthRecordRequest request)
        {
            return await _apiClient.PatchAsync<HealthRecord>($"health-records/{id}", request);
        }
    }
}