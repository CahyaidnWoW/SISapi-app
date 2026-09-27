using SISapi_Desktop.Services;
using System;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class CageView : UserControl
    {
        private readonly CageService _cageService;

        public CageView()
        {
            InitializeComponent();
            _cageService = new CageService();
            LoadCages();
        }

        private async void LoadCages()
        {
            try
            {
                var cages = await _cageService.GetAllAsync();
                GridCages.ItemsSource = cages;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat data kandang: {ex.Message}");
            }
        }

        private async void BtnAdd_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (string.IsNullOrWhiteSpace(TxtName.Text) || string.IsNullOrWhiteSpace(TxtCapacity.Text))
            {
                ShowError("Nama dan kapasitas wajib diisi.");
                return;
            }

            if (!int.TryParse(TxtCapacity.Text, out int capacity))
            {
                ShowError("Kapasitas harus berupa angka.");
                return;
            }

            try
            {
                await _cageService.CreateAsync(TxtName.Text.Trim(), capacity, TxtLocation.Text.Trim());

                TxtName.Clear();
                TxtCapacity.Clear();
                TxtLocation.Clear();

                LoadCages(); // refresh tabel
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menambah kandang: {ex.Message}");
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }

        private void GridCages_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {

        }
    }
}