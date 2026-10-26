using SISapi_Desktop.Services;
using System;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class FcrReportView : UserControl
    {
        private readonly FcrService _fcrService;

        public FcrReportView()
        {
            InitializeComponent();
            _fcrService = new FcrService();
        }

        private void UserControl_Loaded(object sender, RoutedEventArgs e)
        {
            LoadAllFcrData();
        }

        private void BtnRefresh_Click(object sender, RoutedEventArgs e)
        {
            LoadAllFcrData();
        }

        private async void LoadAllFcrData()
        {
            TxtError.Visibility = Visibility.Collapsed;
            TxtStatus.Visibility = Visibility.Visible;
            FcrItemsControl.ItemsSource = null;

            try
            {
                var results = await _fcrService.GetAllFcrAsync();

                if (results == null || results.Count == 0)
                {
                    TxtStatus.Text = "Belum ada data FCR / Kandang kosong.";
                }
                else
                {
                    FcrItemsControl.ItemsSource = results;
                    TxtStatus.Visibility = Visibility.Collapsed;
                }
            }
            catch (Exception ex)
            {
                TxtStatus.Visibility = Visibility.Collapsed;
                TxtError.Text = $"Gagal memuat data FCR: {ex.Message}";
                TxtError.Visibility = Visibility.Visible;
            }
        }
    }
}