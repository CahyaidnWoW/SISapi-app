using SISapi_Desktop.Services;
using System;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class FcrReportView : UserControl
    {
        private readonly FcrService _fcrService;
        private readonly CageService _cageService;

        public FcrReportView()
        {
            InitializeComponent();
            _fcrService = new FcrService();
            _cageService = new CageService();
            LoadCages();
        }

        private async void LoadCages()
        {
            try
            {
                CmbCage.ItemsSource = await _cageService.GetAllAsync();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat data kandang: {ex.Message}");
            }
        }

        private async void BtnCalculate_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;
            ResultPanel.Visibility = Visibility.Collapsed;

            if (CmbCage.SelectedValue == null)
            {
                ShowError("Pilih kandang terlebih dahulu.");
                return;
            }

            try
            {
                int cageId = (int)CmbCage.SelectedValue;
                var result = await _fcrService.GetByCageAsync(cageId);

                TxtFcrValue.Text = result.Fcr.ToString("0.00");
                TxtTotalFeed.Text = $"Total pakan dikonsumsi: {result.TotalFeedConsumed} kg";
                TxtWeightGain.Text = $"Total kenaikan berat: {result.TotalWeightGain} kg";

                ResultPanel.Visibility = Visibility.Visible;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menghitung FCR: {ex.Message}");
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }
    }
}