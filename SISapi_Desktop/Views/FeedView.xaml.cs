using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class FeedView : UserControl
    {
        private readonly FeedService _feedService;
        private FeedStock _selectedFeedForEdit = null;

        public FeedView()
        {
            InitializeComponent();
            _feedService = new FeedService();
            LoadFeeds();
        }

        private async void LoadFeeds()
        {
            try
            {
                var feeds = await _feedService.GetAllAsync();
                GridFeeds.ItemsSource = feeds;
                CmbFeedForBatch.ItemsSource = feeds;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat data pakan: {ex.Message}");
            }
        }

        private async void BtnAddFeed_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (string.IsNullOrWhiteSpace(TxtFeedName.Text) || CmbUnit.SelectedItem == null)
            {
                ShowError("Nama pakan dan unit wajib diisi.");
                return;
            }

            string unit = ((ComboBoxItem)CmbUnit.SelectedItem).Content.ToString();
            string feedName = TxtFeedName.Text.Trim();

            try
            {
                if (_selectedFeedForEdit == null)
                {
                    // Tambah Jenis Pakan Baru
                    await _feedService.CreateAsync(feedName, unit);
                }
                else
                {
                    // Update Jenis Pakan Existing
                    await _feedService.UpdateAsync(_selectedFeedForEdit.Id, feedName, unit);
                }

                ResetFeedForm();
                LoadFeeds();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menyimpan jenis pakan: {ex.Message}");
            }
        }

        private void BtnEditFeed_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (sender is Button btn && btn.DataContext is FeedStock feed)
            {
                _selectedFeedForEdit = feed;

                TxtFeedName.Text = feed.FeedName;

                // Select unit di ComboBox
                foreach (ComboBoxItem item in CmbUnit.Items)
                {
                    if (item.Content?.ToString() == feed.Unit)
                    {
                        CmbUnit.SelectedItem = item;
                        break;
                    }
                }

                TxtFeedFormTitle.Text = $"Edit Jenis Pakan ({feed.FeedName})";
                BtnAddFeed.Content = "Simpan Perubahan";
                BtnCancelFeedEdit.Visibility = Visibility.Visible;
            }
        }

        private async void BtnDeleteFeed_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (sender is Button btn && btn.DataContext is FeedStock feed)
            {
                var result = MessageBox.Show($"Apakah Anda yakin ingin menghapus jenis pakan '{feed.FeedName}'?",
                                             "Konfirmasi Hapus", MessageBoxButton.YesNo, MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    try
                    {
                        await _feedService.DeleteAsync(feed.Id);
                        LoadFeeds();
                    }
                    catch (Exception ex)
                    {
                        ShowError($"Gagal menghapus jenis pakan: {ex.Message}");
                    }
                }
            }
        }

        private void BtnCancelFeedEdit_Click(object sender, RoutedEventArgs e)
        {
            ResetFeedForm();
        }

        private void ResetFeedForm()
        {
            _selectedFeedForEdit = null;
            TxtFeedName.Clear();
            CmbUnit.SelectedItem = null;

            TxtFeedFormTitle.Text = "Tambah Jenis Pakan Baru";
            BtnAddFeed.Content = "+ Tambah Jenis Pakan";
            BtnCancelFeedEdit.Visibility = Visibility.Collapsed;
            TxtError.Visibility = Visibility.Collapsed;
        }

        private async void BtnAddBatch_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (CmbFeedForBatch.SelectedValue == null || string.IsNullOrWhiteSpace(TxtQuantity.Text) ||
                DpEntryDate.SelectedDate == null)
            {
                ShowError("Semua field batch wajib diisi.");
                return;
            }

            if (!decimal.TryParse(TxtQuantity.Text, NumberStyles.Any, CultureInfo.InvariantCulture, out decimal quantity))
            {
                ShowError("Jumlah harus berupa angka.");
                return;
            }

            try
            {
                int feedId = (int)CmbFeedForBatch.SelectedValue;
                string entryDate = DpEntryDate.SelectedDate.Value.ToString("yyyy-MM-dd");

                await _feedService.AddBatchAsync(feedId, quantity, entryDate);

                TxtQuantity.Clear();
                DpEntryDate.SelectedDate = null;

                LoadFeeds(); // refresh total_stock
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menambah batch: {ex.Message}");
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }
    }
}