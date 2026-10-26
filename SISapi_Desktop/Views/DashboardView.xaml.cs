using LiveCharts;
using LiveCharts.Wpf;
using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace SISapi_Desktop.Views
{
    public partial class DashboardView : UserControl
    {
        private readonly DashboardService _dashboardService;
        public List<string> MonthLabels { get; set; } = new List<string>();

        public DashboardView()
        {
            InitializeComponent();
            DataContext = this;
            _dashboardService = new DashboardService();

            Loaded += OnViewLoaded;
        }

        private async void OnViewLoaded(object sender, RoutedEventArgs e)
        {
            try
            {
                var summary = await _dashboardService.GetSummaryAsync();
                if (summary != null)
                {
                    TxtTotalCattle.Text = summary.TotalCattle.ToString();
                    TxtAvgAdg.Text = $"{summary.AvgAdg30d:F2} kg/hr";
                    TxtReadyToSell.Text = summary.ReadyToSell.ToString();
                    TxtSickCattle.Text = summary.SickCattle.ToString();
                    TxtCriticalFeed.Text = summary.CriticalFeedCount.ToString();

                    if (DgUpcomingMedical != null)
                    {
                        DgUpcomingMedical.ItemsSource = summary.UpcomingMedicals;
                    }
                }

                var growthData = await _dashboardService.GetGrowthTrendAsync();
                if (growthData != null && growthData.Any())
                {
                    RenderGrowthChart(growthData);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Gagal memuat data dashboard: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void RenderGrowthChart(List<GrowthTrendDto> trends)
        {
            var weightValues = new ChartValues<double>();
            var targetValues = new ChartValues<double>();
            MonthLabels.Clear();

            foreach (var item in trends)
            {
                MonthLabels.Add(item.Period);
                weightValues.Add(Math.Round(item.AvgWeight, 1));
                targetValues.Add(450.0);
            }

            ChartWeightTrend.Series = new SeriesCollection
            {
                new LineSeries
                {
                    Title = "Rata-rata Bobot (kg)",
                    Values = weightValues,
                    PointGeometry = DefaultGeometries.Circle,
                    PointGeometrySize = 8,
                    Stroke = new SolidColorBrush(Color.FromRgb(37, 99, 235)),
                    Fill = new SolidColorBrush(Color.FromArgb(25, 37, 99, 235))
                },
                new LineSeries
                {
                    Title = "Target Siap Jual (450 kg)",
                    Values = targetValues,
                    PointGeometry = null,
                    Stroke = new SolidColorBrush(Color.FromRgb(239, 68, 68)),
                    StrokeDashArray = new DoubleCollection { 4, 4 },
                    Fill = Brushes.Transparent
                }
            };

            AxisXMonths.Labels = MonthLabels.ToArray();
        }
        private void BtnOpenExportDialog_Click(object sender, RoutedEventArgs e)
        {
            var dialog = new ExportReportDialog();
            dialog.Owner = Window.GetWindow(this);
            dialog.ShowDialog();
        }
    }
}
