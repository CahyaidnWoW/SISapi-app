using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SISapi.Desktop.Models;

namespace SISapi.Desktop.Helper
{
    public static class SessionManager
    {
        public static string Token { get; set; }
        public static User CurrentUser { get; set; }

        public static bool IsLoggedIn => !string.IsNullOrEmpty(Token);

        public static void Logout()
        {
            Token = null;
            CurrentUser = null;
        }
    }
}