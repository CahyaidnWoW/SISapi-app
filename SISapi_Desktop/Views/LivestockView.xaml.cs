using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace SISapi_Desktop.Views
{
    public partial class LivestockView : UserControl
    {
        private readonly LivestockService _livestockService;
        private readonly CageService _cageService;
        private readonly BreedService _breedService;
        private List<Livestock> _allLivestocks;

        public LivestockView()
        {
            InitializeComponent();
            _livestockService = new LivestockService();
            _cageService = new CageService();
            _breedService = new BreedService();
            _allLivestocks = new List<Livestock>();

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
                _allLivestocks = data?.ToList() ?? new List<Livestock>();
                ApplyFilter();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat data sapi: {ex.Message}");
            }
        }

        // --- FILTER STATUS ---
        private void CmbFilterStatus_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ApplyFilter();
        }

        private void ApplyFilter()
        {
            if (GridLivestock == null || _allLivestocks == null) return;

            if (CmbFilterStatus?.SelectedItem is ComboBoxItem selectedItem)
            {
                string status = selectedItem.Content?.ToString();
                if (string.IsNullOrEmpty(status) || status.Equals("Semua", StringComparison.OrdinalIgnoreCase))
                {
                    GridLivestock.ItemsSource = _allLivestocks;
                }
                else
                {
                    GridLivestock.ItemsSource = _allLivestocks
                        .Where(x => string.Equals(x.Status, status, StringComparison.OrdinalIgnoreCase))
                        .ToList();
                }
            }
            else
            {
                GridLivestock.ItemsSource = _allLivestocks;
            }
        }

        // --- TAMBAH SAPI ---
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

        private async void BtnEdit_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if ((sender as Button)?.DataContext is Livestock selectedLivestock)
            {
                var editWindow = new Window
                {
                    Title = $"Edit Data Sapi - {selectedLivestock.TagNumber}",
                    Width = 380,
                    Height = 360,
                    WindowStartupLocation = WindowStartupLocation.CenterOwner,
                    Owner = Window.GetWindow(this),
                    ResizeMode = ResizeMode.NoResize,
                    Background = Brushes.White
                };

                var stack = new StackPanel { Margin = new Thickness(20) };

                stack.Children.Add(new TextBlock
                {
                    Text = $"Edit Status & Kandang ({selectedLivestock.TagNumber})",
                    FontSize = 15,
                    FontWeight = FontWeights.Bold,
                    Foreground = (Brush)new BrushConverter().ConvertFrom("#1E3259"),
                    Margin = new Thickness(0, 0, 0, 15)
                });

                stack.Children.Add(new TextBlock { Text = "Status Sapi:", FontWeight = FontWeights.Bold, Margin = new Thickness(0, 4, 0, 4) });
                var cmbStatus = new ComboBox { Height = 32, Margin = new Thickness(0, 0, 0, 12) };
                cmbStatus.Items.Add("aktif");
                cmbStatus.Items.Add("terjual");
                cmbStatus.Items.Add("mati");
                cmbStatus.Items.Add("afkir");
                cmbStatus.SelectedItem = selectedLivestock.Status ?? "aktif";
                stack.Children.Add(cmbStatus);

                stack.Children.Add(new TextBlock { Text = "Kandang:", FontWeight = FontWeights.Bold, Margin = new Thickness(0, 4, 0, 4) });
                var cmbCageEdit = new ComboBox { Height = 32, DisplayMemberPath = "Name", SelectedValuePath = "Id", Margin = new Thickness(0, 0, 0, 16) };
                cmbCageEdit.ItemsSource = await _cageService.GetAllAsync();
                cmbCageEdit.SelectedValue = selectedLivestock.CageId;
                stack.Children.Add(cmbCageEdit);

                var btnSave = new Button
                {
                    Content = "Simpan Perubahan",
                    Height = 36,
                    Background = (Brush)new BrushConverter().ConvertFrom("#294579"),
                    Foreground = Brushes.White,
                    FontWeight = FontWeights.Bold,
                    Cursor = System.Windows.Input.Cursors.Hand
                };

                btnSave.Click += async (s, args) =>
                {
                    if (cmbCageEdit.SelectedValue == null || cmbStatus.SelectedItem == null)
                    {
                        MessageBox.Show("Semua field edit wajib diisi.", "Peringatan", MessageBoxButton.OK, MessageBoxImage.Warning);
                        return;
                    }

                    var updateReq = new UpdateLivestockRequest
                    {
                        TagNumber = selectedLivestock.TagNumber,
                        CageId = (int)cmbCageEdit.SelectedValue,
                        Breed = selectedLivestock.Breed?.BreedName ?? "Limousin",
                        Gender = selectedLivestock.Gender,
                        BirthDate = selectedLivestock.BirthDate.ToString("yyyy-MM-dd"),
                        Status = cmbStatus.SelectedItem.ToString()
                    };

                    try
                    {
                        await _livestockService.UpdateAsync(selectedLivestock.Id, updateReq);
                        MessageBox.Show("Data sapi berhasil diperbarui!", "Sukses", MessageBoxButton.OK, MessageBoxImage.Information);
                        editWindow.DialogResult = true;
                        editWindow.Close();
                        LoadLivestocks();
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show($"Gagal memperbarui data sapi: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                };

                stack.Children.Add(btnSave);
                editWindow.Content = stack;
                editWindow.ShowDialog();
            }
        }

        private async void BtnDelete_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if ((sender as Button)?.DataContext is Livestock selectedLivestock)
            {
                var result = MessageBox.Show(
                    $"Apakah Anda yakin ingin menghapus data sapi ({selectedLivestock.TagNumber})?\n\nCatatan: Pembatalan hanya diizinkan untuk kesalahan input data baru yang belum memiliki riwayat medis/pakan.",
                    "Konfirmasi Hapus",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    try
                    {
                        await _livestockService.DeleteAsync(selectedLivestock.Id);
                        MessageBox.Show("Data sapi berhasil dihapus.", "Sukses", MessageBoxButton.OK, MessageBoxImage.Information);
                        LoadLivestocks();
                    }
                    catch (Exception ex)
                    {
                        ShowError($"Gagal menghapus sapi: {ex.Message}");
                        MessageBox.Show(ex.Message, "Peringatan / Proteksi Industri", MessageBoxButton.OK, MessageBoxImage.Warning);
                    }
                }
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }
    }
}