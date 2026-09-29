using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

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

                LoadCages();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menambah kandang: {ex.Message}");
            }
        }

        private void BtnEdit_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if ((sender as Button)?.DataContext is Cage selectedCage)
            {
                var editWindow = new Window
                {
                    Title = $"Edit Kandang - {selectedCage.Name}",
                    Width = 360,
                    Height = 360,
                    WindowStartupLocation = WindowStartupLocation.CenterOwner,
                    Owner = Window.GetWindow(this),
                    ResizeMode = ResizeMode.NoResize,
                    Background = Brushes.White
                };

                var stack = new StackPanel { Margin = new Thickness(20) };

                stack.Children.Add(new TextBlock
                {
                    Text = "Edit Data Kandang",
                    FontSize = 16,
                    FontWeight = FontWeights.Bold,
                    Foreground = (Brush)new BrushConverter().ConvertFrom("#1E3259"),
                    Margin = new Thickness(0, 0, 0, 15)
                });

                stack.Children.Add(new TextBlock { Text = "Nama Kandang:", FontWeight = FontWeights.Bold, Margin = new Thickness(0, 4, 0, 4) });
                var txtEditName = new TextBox { Text = selectedCage.Name, Height = 32, VerticalContentAlignment = VerticalAlignment.Center, Margin = new Thickness(0, 0, 0, 10) };
                stack.Children.Add(txtEditName);

                stack.Children.Add(new TextBlock { Text = "Kapasitas:", FontWeight = FontWeights.Bold, Margin = new Thickness(0, 4, 0, 4) });
                var txtEditCapacity = new TextBox { Text = selectedCage.Capacity.ToString(), Height = 32, VerticalContentAlignment = VerticalAlignment.Center, Margin = new Thickness(0, 0, 0, 10) };
                stack.Children.Add(txtEditCapacity);

                stack.Children.Add(new TextBlock { Text = "Lokasi:", FontWeight = FontWeights.Bold, Margin = new Thickness(0, 4, 0, 4) });
                var txtEditLocation = new TextBox { Text = selectedCage.Location, Height = 32, VerticalContentAlignment = VerticalAlignment.Center, Margin = new Thickness(0, 0, 0, 16) };
                stack.Children.Add(txtEditLocation);

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
                    if (string.IsNullOrWhiteSpace(txtEditName.Text) || string.IsNullOrWhiteSpace(txtEditCapacity.Text))
                    {
                        MessageBox.Show("Nama dan kapasitas wajib diisi.", "Peringatan", MessageBoxButton.OK, MessageBoxImage.Warning);
                        return;
                    }

                    if (!int.TryParse(txtEditCapacity.Text, out int capacity))
                    {
                        MessageBox.Show("Kapasitas harus berupa angka.", "Peringatan", MessageBoxButton.OK, MessageBoxImage.Warning);
                        return;
                    }

                    try
                    {
                        await _cageService.UpdateAsync(selectedCage.Id, txtEditName.Text.Trim(), capacity, txtEditLocation.Text.Trim());
                        MessageBox.Show("Data kandang berhasil diperbarui!", "Sukses", MessageBoxButton.OK, MessageBoxImage.Information);
                        editWindow.DialogResult = true;
                        editWindow.Close();
                        LoadCages();
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show($"Gagal memperbarui kandang: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
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

            if ((sender as Button)?.DataContext is Cage selectedCage)
            {
                var result = MessageBox.Show(
                    $"Apakah Anda yakin ingin menghapus kandang ({selectedCage.Name})?",
                    "Konfirmasi Hapus",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    try
                    {
                        await _cageService.DeleteAsync(selectedCage.Id);
                        MessageBox.Show("Kandang berhasil dihapus.", "Sukses", MessageBoxButton.OK, MessageBoxImage.Information);
                        LoadCages();
                    }
                    catch (Exception ex)
                    {
                        ShowError($"Gagal menghapus kandang: {ex.Message}");
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