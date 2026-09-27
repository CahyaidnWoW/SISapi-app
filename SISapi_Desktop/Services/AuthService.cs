using SISapi.Desktop.Helper;
using SISapi.Desktop.Models;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class AuthService
    {
        private readonly ApiClient _apiClient;

        public AuthService()
        {
            _apiClient = new ApiClient();
        }

        public async Task<bool> LoginAsync(string email, string password)
        {
            var request = new LoginRequest
            {
                Email = email,
                Password = password
            };

            var response = await _apiClient.PostAsync<LoginResponse>("login", request);

            if (response != null && !string.IsNullOrEmpty(response.Token))
            {
                SessionManager.Token = response.Token;
                SessionManager.CurrentUser = response.User;
                return true;
            }

            return false;
        }
    }
}