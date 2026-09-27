using SISapi_Desktop.Services;
using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class DashboardView : UserControl
    {
        private readonly DashboardService _dashboardService;

        public DashboardView()
        {
            InitializeComponent();
            _dashboardService = new DashboardService();
            LoadSummary();
        }

        private async void LoadSummary()
        {
            try
            {
                var summary = await _dashboardService.GetSummaryAsync();

                TxtTotalLivestock.Text = summary.TotalLivestock.ToString();
                TxtTotalCages.Text = summary.TotalCages.ToString();
                TxtPopulation.Text = $"{summary.TotalPopulation} / {summary.TotalCapacity}";

                if (summary.DueVaccinations.Any())
                {
                    GridDueVaccinations.ItemsSource = summary.DueVaccinations;
                    TxtNoDue.Visibility = Visibility.Collapsed;
                }
                else
                {
                    GridDueVaccinations.Visibility = Visibility.Collapsed;
                    TxtNoDue.Visibility = Visibility.Visible;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Gagal memuat dashboard: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }
}