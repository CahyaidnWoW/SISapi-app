using Newtonsoft.Json;
using SISapi.Desktop.Helper;
using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace SISapi_Desktop.Services
{
    public class ApiClient
    {
        private readonly HttpClient _client;
        private const string BaseUrl = "http://127.0.0.1:8000/api/";

        public ApiClient()
        {
            _client = new HttpClient
            {
                BaseAddress = new Uri(BaseUrl)
            };
            _client.DefaultRequestHeaders.Accept.Clear();
            _client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        }

        private void AttachAuthToken()
        {
            if (!string.IsNullOrEmpty(SessionManager.Token))
            {
                _client.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", SessionManager.Token);
            }
        }

        public async Task<T> GetAsync<T>(string endpoint)
        {
            AttachAuthToken();
            var response = await _client.GetAsync(endpoint);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new Exception($"API Error ({response.StatusCode}): {json}");

            return JsonConvert.DeserializeObject<T>(json);
        }

        public async Task<T> PostAsync<T>(string endpoint, object data)
        {
            AttachAuthToken();
            var jsonContent = new StringContent(JsonConvert.SerializeObject(data), Encoding.UTF8, "application/json");
            var response = await _client.PostAsync(endpoint, jsonContent);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new Exception($"API Error ({response.StatusCode}): {json}");

            return JsonConvert.DeserializeObject<T>(json);
        }

        public async Task<T> PatchAsync<T>(string endpoint, object data)
        {
            AttachAuthToken();
            var jsonContent = new StringContent(JsonConvert.SerializeObject(data), Encoding.UTF8, "application/json");
            var request = new HttpRequestMessage(new HttpMethod("PATCH"), endpoint) { Content = jsonContent };
            var response = await _client.SendAsync(request);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new Exception($"API Error ({response.StatusCode}): {json}");

            return JsonConvert.DeserializeObject<T>(json);
        }

        public async Task<T> PutAsync<T>(string endpoint, object data)
        {
            AttachAuthToken();
            var jsonContent = new StringContent(JsonConvert.SerializeObject(data), Encoding.UTF8, "application/json");
            var response = await _client.PutAsync(endpoint, jsonContent);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new Exception($"API Error ({(int)response.StatusCode}): {json}");

            return JsonConvert.DeserializeObject<T>(json);
        }

        public async Task DeleteAsync(string endpoint)
        {
            AttachAuthToken();
            var response = await _client.DeleteAsync(endpoint);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new Exception($"API Error ({(int)response.StatusCode}): {json}");
        }
    }
}