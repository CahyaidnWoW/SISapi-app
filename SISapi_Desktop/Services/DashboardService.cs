using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class DashboardService
    {
        private readonly ApiClient _apiClient;

        public DashboardService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<DashboardSummaryDto> GetSummaryAsync()
        {
            var response = await _apiClient.GetAsync<DashboardSummaryResponse>("dashboard/summary");
            return response?.Data;
        }

        public async Task<List<GrowthTrendDto>> GetGrowthTrendAsync()
        {
            var response = await _apiClient.GetAsync<GrowthTrendResponse>("dashboard/growth-trend");
            return response?.Data ?? new List<GrowthTrendDto>();
        }

        public async Task<byte[]> DownloadReportAsync(string format, string period)
        {
            return await _apiClient.GetByteArrayAsync($"reports/{format}?period={period}");
        }

    }
}
