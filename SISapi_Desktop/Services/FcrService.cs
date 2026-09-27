using SISapi_Desktop.Models;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class FcrService
    {
        private readonly ApiClient _apiClient;

        public FcrService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<FcrReport> GetByCageAsync(int cageId)
        {
            var wrapper = await _apiClient.GetAsync<FcrReportWrapper>($"cages/{cageId}/fcr");
            return wrapper.Data;
        }
    }
}