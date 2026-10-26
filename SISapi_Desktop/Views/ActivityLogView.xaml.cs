using SISapi_Desktop.Models;
using SISapi_Desktop.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace SISapi_Desktop.Views
{
    public partial class ActivityLogView : UserControl
    {
        private readonly ActivityLogService _logService;
        private List<ActivityLogDto> _allLogs = new List<ActivityLogDto>();

        public ActivityLogView()
        {
            InitializeComponent();
            _logService = new ActivityLogService();
            Loaded += async (s, e) => await LoadLogs();
        }

        private async System.Threading.Tasks.Task LoadLogs()
        {
            try
            {
                _allLogs = await _logService.GetLogsAsync(50);
                DgLogs.ItemsSource = _allLogs;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Gagal memuat log: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async void BtnRefresh_Click(object sender, RoutedEventArgs e)
        {
            await LoadLogs();
        }

        private void TxtSearch_TextChanged(object sender, TextChangedEventArgs e)
        {
            string keyword = TxtSearch.Text.Trim().ToLower();
            if (string.IsNullOrEmpty(keyword))
            {
                DgLogs.ItemsSource = _allLogs;
            }
            else
            {
                DgLogs.ItemsSource = _allLogs.Where(l =>
                    (l.User != null && l.User.Name.ToLower().Contains(keyword)) ||
                    (l.Module != null && l.Module.ToLower().Contains(keyword)) ||
                    (l.Description != null && l.Description.ToLower().Contains(keyword)) ||
                    (l.Platform != null && l.Platform.ToLower().Contains(keyword))
                ).ToList();
            }
        }
    }
}
