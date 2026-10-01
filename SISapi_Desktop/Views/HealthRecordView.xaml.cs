using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class HealthRecordView : UserControl
    {
        private readonly HealthRecordService _healthRecordService;

        public HealthRecordView()
        {
            InitializeComponent();
            _healthRecordService = new HealthRecordService();
        }

        private async void BtnLoadHistory_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (string.IsNullOrWhiteSpace(TxtTagNumber.Text))
            {
                ShowError("Masukkan tag number sapi.");
                return;
            }

            try
            {
                var records = await _healthRecordService.GetByTagAsync(TxtTagNumber.Text.Trim());
                GridHistory.ItemsSource = records;
            }
            catch (Exception ex)
            {
                ShowError($"Gagal memuat riwayat: {ex.Message}");
            }
        }

        private async void BtnAddRecord_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;

            if (string.IsNullOrWhiteSpace(TxtTagNumber.Text) || CmbType.SelectedItem == null || DpDate.SelectedDate == null)
            {
                ShowError("Tag number, tipe, dan tanggal wajib diisi.");
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

                BtnLoadHistory_Click(sender, e); 
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
    }
}