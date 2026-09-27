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

            try
            {
                await _feedService.CreateAsync(TxtFeedName.Text.Trim(), unit);
                TxtFeedName.Clear();
                LoadFeeds();
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menambah jenis pakan: {ex.Message}");
            }
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