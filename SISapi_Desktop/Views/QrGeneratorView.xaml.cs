using Microsoft.Win32;
using SISapi_Desktop.Helpers;
using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Imaging;

namespace SISapi_Desktop.Views
{
    public class QrPreviewItem
    {
        public string TagNumber { get; set; }
        public BitmapImage QrImage { get; set; }
    }

    public partial class QrGeneratorView : UserControl
    {
        private readonly LivestockService _livestockService;
        private List<Livestock> _allLivestock;

        public QrGeneratorView()
        {
            InitializeComponent();
            _livestockService = new LivestockService();
            LoadLivestocks();
        }

        private async void LoadLivestocks()
        {
            try
            {
                _allLivestock = await _livestockService.GetAllAsync();
                ListLivestock.ItemsSource = _allLivestock;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat data sapi: {ex.Message}");
            }
        }

        private void BtnGenerate_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            var selected = ListLivestock.SelectedItems.Cast<Livestock>().ToList();

            if (!selected.Any())
            {
                ShowError("Pilih minimal 1 sapi.");
                return;
            }

            var previewItems = selected.Select(l => new QrPreviewItem
            {
                TagNumber = l.TagNumber,
                QrImage = QrCodeGenerator.GenerateBitmap(l.TagNumber)
            }).ToList();

            QrPreviewPanel.ItemsSource = previewItems;
        }

        private void BtnSaveAll_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            var selected = ListLivestock.SelectedItems.Cast<Livestock>().ToList();

            if (!selected.Any())
            {
                ShowError("Pilih minimal 1 sapi.");
                return;
            }

            var dialog = new System.Windows.Forms.FolderBrowserDialog();
            if (dialog.ShowDialog() != System.Windows.Forms.DialogResult.OK)
                return;

            try
            {
                foreach (var l in selected)
                {
                    string filePath = Path.Combine(dialog.SelectedPath, $"{l.TagNumber}.png");
                    QrCodeGenerator.SaveAsPng(l.TagNumber, filePath);
                }

                MessageBox.Show($"{selected.Count} label QR berhasil disimpan.", "Sukses",
                    MessageBoxButton.OK, MessageBoxImage.Information);
            }
            catch (Exception ex)
            {
                ShowError($"Gagal menyimpan QR: {ex.Message}");
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }
    }
}