using SISapi_Desktop.Models;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class DashboardSummary
    {
        public int TotalLivestock { get; set; }
        public int TotalCages { get; set; }
        public int TotalPopulation { get; set; }
        public int TotalCapacity { get; set; }
        public List<HealthRecord> DueVaccinations { get; set; }
    }

    public class DashboardService
    {
        private readonly LivestockService _livestockService;
        private readonly CageService _cageService;
        private readonly HealthRecordService _healthRecordService;

        public DashboardService()
        {
            _livestockService = new LivestockService();
            _cageService = new CageService();
            _healthRecordService = new HealthRecordService();
        }

        public async Task<DashboardSummary> GetSummaryAsync()
        {
            var livestocks = await _livestockService.GetAllAsync();
            var cages = await _cageService.GetAllAsync();
            var due = await _healthRecordService.GetDueAsync();

            return new DashboardSummary
            {
                TotalLivestock = livestocks.Count,
                TotalCages = cages.Count,
                TotalPopulation = cages.Sum(c => c.CurrentPopulation),
                TotalCapacity = cages.Sum(c => c.Capacity),
                DueVaccinations = due
            };
        }
    }
}