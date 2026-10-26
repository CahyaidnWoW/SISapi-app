using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class ActivityLogService
    {
        private readonly ApiClient _apiClient;

        public ActivityLogService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<List<ActivityLogDto>> GetLogsAsync(int limit = 50)
        {
            var response = await _apiClient.GetAsync<ActivityLogResponse>($"logs/activity?limit={limit}");
            return response?.Data ?? new List<ActivityLogDto>();
        }
    }
}
