using Microsoft.Win32;
using SISapi_Desktop.Services;
using System;
using System.IO;
using System.Windows;

namespace SISapi_Desktop.Views
{
    public partial class ExportReportDialog : Window
    {
        private readonly DashboardService _dashboardService;

        public ExportReportDialog()
        {
            InitializeComponent();
            _dashboardService = new DashboardService();
        }

        private string GetSelectedPeriod()
        {
            if (RbWeekly.IsChecked == true) return "weekly";
            if (RbMonthly.IsChecked == true) return "monthly";
            return "daily";
        }

        private async void BtnDownloadPdf_Click(object sender, RoutedEventArgs e)
        {
            string period = GetSelectedPeriod();
            var dialog = new SaveFileDialog
            {
                FileName = $"Laporan_Peternakan_{period}_{DateTime.Now:yyyyMMdd}",
                Filter = "PDF Files (*.pdf)|*.pdf"
            };

            if (dialog.ShowDialog() == true)
            {
                try
                {
                    var bytes = await _dashboardService.DownloadReportAsync("pdf", period);
                    File.WriteAllBytes(dialog.FileName, bytes);

                    MessageBox.Show("Dokumen laporan berhasil diunduh!", "Sukses", MessageBoxButton.OK, MessageBoxImage.Information);
                    this.Close();
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Gagal mengunduh dokumen: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
        }

        private void BtnClose_Click(object sender, RoutedEventArgs e)
        {
            this.Close();
        }
    }
}
