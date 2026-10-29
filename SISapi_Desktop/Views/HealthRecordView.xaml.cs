using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using SISapi.Desktop.Helper;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using SISapi_Desktop.Helpers;

namespace SISapi_Desktop.Views
{
    public partial class HealthRecordView : UserControl
    {
        private readonly HealthRecordService _healthRecordService;
        private readonly LivestockService _livestockService;
        private List<Livestock> _allLivestocks;

        public HealthRecordView()
        {
            InitializeComponent();
            _healthRecordService = new HealthRecordService();
            _livestockService = new LivestockService();
            _allLivestocks = new List<Livestock>();

            CheckRoleAccess();
            LoadLivestocks();
        }

        private void CheckRoleAccess()
        {
            if (RoleHelper.IsManager)
            {
                if (CardFormInput != null) CardFormInput.Visibility = Visibility.Collapsed;
            }
        }

        private async void LoadLivestocks()
        {
            try
            {
                var data = await _livestockService.GetAllAsync();
                _allLivestocks = data ?? new List<Livestock>();
                GridLivestock.ItemsSource = _allLivestocks;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat daftar sapi: {ex.Message}");
            }
        }

        private void TxtSearchCow_TextChanged(object sender, TextChangedEventArgs e)
        {
            string q = TxtSearchCow.Text.Trim().ToLower();
            if (string.IsNullOrEmpty(q))
            {
                GridLivestock.ItemsSource = _allLivestocks;
            }
            else
            {
                GridLivestock.ItemsSource = _allLivestocks.Where(c =>
                    (c.TagNumber != null && c.TagNumber.ToLower().Contains(q)) ||
                    (c.Cage?.Name != null && c.Cage.Name.ToLower().Contains(q)) ||
                    (c.Breed?.BreedName != null && c.Breed.BreedName.ToLower().Contains(q))
                ).ToList();
            }
        }

        private async void GridLivestock_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (GridLivestock.SelectedItem is Livestock cow)
            {
                TxtTagNumber.Text = cow.TagNumber;
                await LoadHistory(cow.TagNumber);
            }
        }

        private async Task LoadHistory(string tagNumber)
        {
            TxtError.Visibility = Visibility.Collapsed;
            try
            {
                var records = await _healthRecordService.GetByTagAsync(tagNumber);
                GridHistory.ItemsSource = records;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat riwayat medis {tagNumber}: {ex.Message}");
            }
        }

        private async void BtnAddRecord_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (string.IsNullOrWhiteSpace(TxtTagNumber.Text))
            {
                ShowError("Silakan pilih sapi di tabel atas terlebih dahulu.");
                return;
            }

            if (CmbType.SelectedItem == null || DpDate.SelectedDate == null)
            {
                ShowError("Tipe tindakan dan tanggal wajib diisi.");
                return;
            }

            var request = new CreateHealthRecordRequest
            {
                Type = ((ComboBoxItem)CmbType.SelectedItem).Tag.ToString(),
                Diagnosis = string.IsNullOrWhiteSpace(TxtDiagnosis.Text) ? null : TxtDiagnosis.Text.Trim(),
                Treatment = string.IsNullOrWhiteSpace(TxtTreatment.Text) ? null : TxtTreatment.Text.Trim(),
                Date = DpDate.SelectedDate.Value.ToString("yyyy-MM-dd"),
                NextDueDate = DpNextDue.SelectedDate.HasValue ? DpNextDue.SelectedDate.Value.ToString("yyyy-MM-dd") : null
            };

            try
            {
                await _healthRecordService.CreateAsync(TxtTagNumber.Text.Trim(), request);

                TxtDiagnosis.Clear();
                TxtTreatment.Clear();
                DpDate.SelectedDate = null;
                DpNextDue.SelectedDate = null;

                await LoadHistory(TxtTagNumber.Text.Trim());
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menyimpan rekam medis: {ex.Message}");
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }

        private void BtnViewPhoto_Click(object sender, RoutedEventArgs e)
        {
            if ((sender as Button)?.DataContext is HealthRecord record && !string.IsNullOrEmpty(record.PhotoUrl ?? record.PhotoPath))
            {
                string photoUrl = record.PhotoUrl;
                if (string.IsNullOrEmpty(photoUrl))
                {
                    photoUrl = $"http://sisapi.smkn1sumedang.sch.id/api/health-records/{record.Id}/photo";
                }

                try
                {
                    var photoWindow = new Window
                    {
                        Title = $"Foto Bukti Laporan Darurat - {record.Date:dd MMM yyyy}",
                        Width = 500,
                        Height = 560,
                        WindowStartupLocation = WindowStartupLocation.CenterOwner,
                        Owner = Window.GetWindow(this),
                        Background = System.Windows.Media.Brushes.White
                    };

                    var stack = new StackPanel { Margin = new Thickness(16) };
                    stack.Children.Add(new TextBlock
                    {
                        Text = $"Foto Laporan (Diagnosis: {record.Diagnosis ?? "-"})",
                        FontWeight = FontWeights.Bold,
                        FontSize = 14,
                        Margin = new Thickness(0, 0, 0, 8),
                        Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFrom("#1E3259")
                    });

                    var txtStatus = new TextBlock
                    {
                        Text = "Memuat foto...",
                        FontSize = 12,
                        Foreground = System.Windows.Media.Brushes.Gray,
                        Margin = new Thickness(0, 0, 0, 8)
                    };
                    stack.Children.Add(txtStatus);

                    var img = new Image
                    {
                        MaxHeight = 380,
                        Stretch = System.Windows.Media.Stretch.Uniform,
                        Visibility = Visibility.Collapsed
                    };

                    var bitmap = new System.Windows.Media.Imaging.BitmapImage();
                    bitmap.BeginInit();
                    bitmap.UriSource = new Uri(photoUrl);
                    bitmap.EndInit();

                    bitmap.DownloadCompleted += (s, args) =>
                    {
                        txtStatus.Visibility = Visibility.Collapsed;
                        img.Visibility = Visibility.Visible;
                    };

                    bitmap.DownloadFailed += (s, args) =>
                    {
                        txtStatus.Text = "Gagal memuat foto dari server sekolah (404 Not Found).\nServer sekolah belum menjalankan storage:link atau file tidak ditemukan.";
                        txtStatus.Foreground = System.Windows.Media.Brushes.Crimson;
                    };

                    img.Source = bitmap;
                    stack.Children.Add(img);

                    var btnOpenBrowser = new Button
                    {
                        Content = "🌐 Buka Link Foto di Browser",
                        Margin = new Thickness(0, 12, 0, 0),
                        Padding = new Thickness(10, 6, 10, 6),
                        Background = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFrom("#2563EB"),
                        Foreground = System.Windows.Media.Brushes.White,
                        BorderThickness = new Thickness(0),
                        Cursor = System.Windows.Input.Cursors.Hand
                    };
                    btnOpenBrowser.Click += (s, args) =>
                    {
                        try
                        {
                            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo(photoUrl) { UseShellExecute = true });
                        }
                        catch (Exception ex)
                        {
                            MessageBox.Show($"Tidak dapat membuka browser: {ex.Message}");
                        }
                    };
                    stack.Children.Add(btnOpenBrowser);

                    photoWindow.Content = stack;
                    photoWindow.ShowDialog();
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Gagal membuka gambar: {ex.Message}", "Error Foto", MessageBoxButton.OK, MessageBoxImage.Warning);
                }
            }
        }

    }
}
