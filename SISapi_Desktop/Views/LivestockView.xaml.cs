using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class LivestockView : UserControl
    {
        private readonly LivestockService _livestockService;
        private readonly CageService _cageService;
        private readonly BreedService _breedService;

        public LivestockView()
        {
            InitializeComponent();
            _livestockService = new LivestockService();
            _cageService = new CageService();
            _breedService = new BreedService();

            LoadDropdowns();
            LoadLivestocks();
        }

        private async void LoadDropdowns()
        {
            try
            {
                CmbCage.ItemsSource = await _cageService.GetAllAsync();
                CmbBreed.ItemsSource = await _breedService.GetAllAsync();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat dropdown: {ex.Message}");
            }
        }

        private async void LoadLivestocks()
        {
            try
            {
                var data = await _livestockService.GetAllAsync();
                GridLivestock.ItemsSource = data;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat data sapi: {ex.Message}");
            }
        }

        private async void BtnAdd_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (CmbCage.SelectedValue == null || CmbBreed.SelectedValue == null ||
                CmbGender.SelectedItem == null || DpBirthDate.SelectedDate == null ||
                DpEntryDate.SelectedDate == null || string.IsNullOrWhiteSpace(TxtInitialWeight.Text))
            {
                ShowError("Semua field wajib diisi.");
                return;
            }

            if (!decimal.TryParse(TxtInitialWeight.Text, NumberStyles.Any, CultureInfo.InvariantCulture, out decimal weight))
            {
                ShowError("Berat awal harus berupa angka.");
                return;
            }

            var request = new CreateLivestockRequest
            {
                CageId = (int)CmbCage.SelectedValue,
                BreedId = (int)CmbBreed.SelectedValue,
                Gender = ((ComboBoxItem)CmbGender.SelectedItem).Tag.ToString(),
                BirthDate = DpBirthDate.SelectedDate.Value.ToString("yyyy-MM-dd"),
                EntryDate = DpEntryDate.SelectedDate.Value.ToString("yyyy-MM-dd"),
                InitialWeight = weight
            };

            try
            {
                await _livestockService.CreateAsync(request);

                TxtInitialWeight.Clear();
                DpBirthDate.SelectedDate = null;
                DpEntryDate.SelectedDate = null;

                LoadLivestocks();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menambah sapi: {ex.Message}");
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }
    }
}