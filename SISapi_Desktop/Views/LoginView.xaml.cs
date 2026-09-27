using SISapi.Desktop.Helper;
using SISapi_Desktop.Services;
using System;
using System.Windows;

namespace SISapi_Desktop.Views
{
    public partial class LoginView : Window
    {
        private readonly AuthService _authService;

        public LoginView()
        {
            InitializeComponent();
            _authService = new AuthService();
        }

        private async void BtnLogin_Click(object sender, RoutedEventArgs e)
        {
            TxtError.Visibility = Visibility.Collapsed;
            BtnLogin.IsEnabled = false;
            BtnLogin.Content = "Memproses...";

            string email = TxtEmail.Text.Trim();
            string password = TxtPassword.Password;

            if (string.IsNullOrEmpty(email) || string.IsNullOrEmpty(password))
            {
                ShowError("Email dan password wajib diisi.");
                ResetButton();
                return;
            }

            try
            {
                bool success = await _authService.LoginAsync(email, password);

                if (success)
                {
                    MessageBox.Show($"Selamat datang, {SessionManager.CurrentUser.Name} ({SessionManager.CurrentUser.Role})!",
                                    "Login Berhasil", MessageBoxButton.OK, MessageBoxImage.Information);

                    new MainShellView().Show();
                    this.Close();
                  
                }
                else
                {
                    ShowError("Login gagal. Periksa kembali email dan password.");
                }
            }
            catch (Exception ex)
            {
                ShowError($"Gagal terhubung ke API: {ex.Message}");
            }
            finally
            {
                ResetButton();
            }
        }

        private void ShowError(string message)
        {
            TxtError.Text = message;
            TxtError.Visibility = Visibility.Visible;
        }

        private void ResetButton()
        {
            BtnLogin.IsEnabled = true;
            BtnLogin.Content = "Login";
        }
    }
}