using SISapi.Desktop.Helper;
using SISapi_Desktop.Helpers;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace SISapi_Desktop.Views
{
    public partial class MainShellView : Window
    {
        private readonly SolidColorBrush _activeBg = new SolidColorBrush((Color)ColorConverter.ConvertFromString("#294579"));
        private readonly SolidColorBrush _activeFg = Brushes.White;
        private readonly SolidColorBrush _defaultFg = new SolidColorBrush((Color)ColorConverter.ConvertFromString("#D0DCEE"));

        public MainShellView()
        {
            InitializeComponent();
            SetupMenuByRole();
            TxtUserInfo.Text = $"{SessionManager.CurrentUser.Name} ({SessionManager.CurrentUser.Role})";

            SetActiveButton(BtnDashboard);
            ContentArea.Content = new DashboardView();
        }

        private void SetupMenuByRole()
        {
            if (RoleHelper.IsVet)
            {
                BtnCage.Visibility = Visibility.Collapsed;
                BtnFeed.Visibility = Visibility.Collapsed;
                BtnQr.Visibility = Visibility.Collapsed;
                BtnFcr.Visibility = Visibility.Collapsed;
            }
        }

        private void Nav_Click(object sender, RoutedEventArgs e)
        {
            if (sender is Button btn)
            {
                ResetSidebarButtons();
                SetActiveButton(btn);

                switch (btn.Name)
                {
                    case "BtnDashboard":
                        ContentArea.Content = new DashboardView();
                        break;
                    case "BtnLivestock":
                        ContentArea.Content = new LivestockView();
                        break;
                    case "BtnCage":
                        ContentArea.Content = new CageView();
                        break;
                    case "BtnFeed":
                        ContentArea.Content = new FeedView();
                        break;
                    case "BtnHealth":
                        ContentArea.Content = new HealthRecordView();
                        break;
                    case "BtnQr":
                        ContentArea.Content = new QrGeneratorView();
                        break;
                    case "BtnFcr":
                        ContentArea.Content = new FcrReportView();
                        break;
                }
            }
        }

        private void ResetSidebarButtons()
        {
            Button[] buttons = { BtnDashboard, BtnLivestock, BtnCage, BtnFeed, BtnHealth, BtnQr, BtnFcr };

            foreach (var button in buttons)
            {
                button.Background = Brushes.Transparent;
                button.Foreground = _defaultFg;
            }
        }

        private void SetActiveButton(Button button)
        {
            button.Background = _activeBg;
            button.Foreground = _activeFg;
        }

        private void BtnLogout_Click(object sender, RoutedEventArgs e)
        {
            SessionManager.Logout();
            new LoginView().Show();
            this.Close();
        }
    }
}